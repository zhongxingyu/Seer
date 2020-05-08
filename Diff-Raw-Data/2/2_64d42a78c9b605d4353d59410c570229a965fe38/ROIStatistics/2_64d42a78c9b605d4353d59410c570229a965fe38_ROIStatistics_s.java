 package plugins.adufour.roi;
 
 import icy.file.FileUtil;
 import icy.plugin.abstract_.Plugin;
 import icy.roi.BooleanMask2D;
 import icy.roi.ROI;
 import icy.roi.ROI2D;
 import icy.roi.ROI3D;
 import icy.sequence.Sequence;
 import icy.type.collection.array.Array1DUtil;
 import icy.type.rectangle.Rectangle3D;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.TreeMap;
 
 import org.apache.poi.hssf.usermodel.HSSFPalette;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.WorkbookUtil;
 import org.apache.poi.xssf.usermodel.XSSFCellStyle;
 import org.apache.poi.xssf.usermodel.XSSFColor;
 
 import plugins.adufour.blocks.tools.roi.ROIBlock;
 import plugins.adufour.blocks.util.VarList;
 import plugins.adufour.vars.lang.VarROIArray;
 import plugins.adufour.vars.lang.VarSequence;
 import plugins.adufour.vars.lang.VarWorkbook;
 
 public class ROIStatistics extends Plugin implements ROIBlock
 {
     public String getMainPluginClassName()
     {
         return ROIMeasures.class.getName();
     }
     
     private final int COL_NAME          = 0;
     private final int COL_X             = 1;
     private final int COL_Y             = 2;
     private final int COL_WIDTH         = 3;
     private final int COL_HEIGHT        = 4;
     private final int COL_SURFACE       = 5;
     private final int COL_VOLUME        = 6;
     private final int COL_MIN_INTENSITY = 7;
     private final int COL_AVG_INTENSITY = 8;
     private final int COL_MAX_INTENSITY = 9;
     private final int COL_SUM_INTENSITY = 10;
     
     VarROIArray       rois              = new VarROIArray("Regions of interest");
     VarSequence       sequence          = new VarSequence("Sequence", null);
     VarWorkbook       book              = new VarWorkbook("Workbook", (Workbook) null);
     
     @Override
     public void declareInput(VarList inputMap)
     {
         inputMap.add(rois);
         inputMap.add(sequence);
     }
     
     @Override
     public void declareOutput(VarList outputMap)
     {
         outputMap.add(book);
     }
     
     @Override
     public void run()
     {
         Workbook wb = book.getValue();
         if (wb == null) book.setValue(wb = new HSSFWorkbook());
         
         wb.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
         
         HSSFPalette palette = null;
         if (wb instanceof HSSFWorkbook)
         {
             palette = ((HSSFWorkbook) wb).getCustomPalette();
         }
         
         int roiID = 1;
         
         Sequence extraSequence = sequence.getValue();
         
         HashMap<Object, CellStyle> nameStyles = new HashMap<Object, CellStyle>(rois.size());
         
         for (ROI roi : rois.getValue())
         {
             CellStyle nameStyle;
             
             Color roiColor = roi.getColor();
             
             if (palette != null)
             {
                 Short colorIndex = palette.findSimilarColor(roiColor.getRed(), roiColor.getGreen(), roiColor.getBlue()).getIndex();
                 
                 nameStyle = nameStyles.get(colorIndex);
                 
                 if (nameStyle == null)
                 {
                     nameStyle = wb.createCellStyle();
                     nameStyle.setFillForegroundColor(colorIndex);
                     nameStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                     nameStyles.put(colorIndex, nameStyle);
                 }
             }
             else
             {
                 nameStyle = nameStyles.get(roiColor);
                 
                 if (nameStyle == null)
                 {
                     nameStyle = wb.createCellStyle();
                     ((XSSFCellStyle) nameStyle).setFillForegroundColor(new XSSFColor(roiColor));
                     nameStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                     nameStyles.put(roiColor, nameStyle);
                 }
             }
             
             ArrayList<Sequence> sequences = roi.getSequences();
             if (extraSequence != null && !sequences.contains(extraSequence)) sequences.add(extraSequence);
             
             if (sequences.size() == 0)
             {
                 String sheetName = "ROI Statistics";
                 
                 Sheet sheet = wb.getSheet(sheetName);
                 if (sheet == null) sheet = wb.createSheet(sheetName);
                 
                 Row header = sheet.getRow(0);
                 if (header == null)
                 {
                     header = sheet.createRow(0);
                     header.getCell(COL_NAME).setCellValue("Name");
                     header.getCell(COL_X).setCellValue("X");
                     header.getCell(COL_Y).setCellValue("Y");
                     header.getCell(COL_WIDTH).setCellValue("Width");
                     header.getCell(COL_HEIGHT).setCellValue("Height");
                     header.getCell(COL_SURFACE).setCellValue("Perimeter");
                     header.getCell(COL_VOLUME).setCellValue("Area");
                 }
                 
                 Row row = sheet.createRow(roiID);
                 
                 Cell name = row.getCell(COL_NAME);
                 name.setCellValue(roi.getName());
                 name.setCellStyle(nameStyle);
                 
                 if (roi instanceof ROI2D)
                 {
                     ROI2D r2 = (ROI2D) roi;
                     
                     Rectangle2D bounds = r2.getBounds2D();
                     
                     row.getCell(COL_X).setCellValue(bounds.getX());
                     row.getCell(COL_Y).setCellValue(bounds.getY());
                     row.getCell(COL_WIDTH).setCellValue(bounds.getWidth());
                     row.getCell(COL_HEIGHT).setCellValue(bounds.getHeight());
                 }
                 
                 row.getCell(COL_VOLUME).setCellValue(roi.getNumberOfPoints());
                 row.getCell(COL_SURFACE).setCellValue(roi.getNumberOfContourPoints());
                 
             }
             else for (Sequence sequence : sequences)
             {
                 int sizeC = sequence.getSizeC();
                 
                 String sheetName = FileUtil.getFileName(sequence.getFilename());
                 if (sheetName == null) sheetName = sequence.getName();
                 
                 sheetName = WorkbookUtil.createSafeSheetName(sheetName);
                 
                 Sheet sheet = wb.getSheet(sheetName);
                 if (sheet == null) sheet = wb.createSheet(sheetName);
                 
                 Row header = sheet.getRow(0);
                 if (header == null)
                 {
                     header = sheet.createRow(0);
                     header.getCell(COL_NAME).setCellValue("Name");
                     header.getCell(COL_X).setCellValue("Min. X");
                     header.getCell(COL_Y).setCellValue("Min. Y");
                     header.getCell(COL_WIDTH).setCellValue("Width");
                     header.getCell(COL_HEIGHT).setCellValue("Height");
                     header.getCell(COL_SURFACE).setCellValue("Perimeter");
                     header.getCell(COL_VOLUME).setCellValue("Area");
                     if (sizeC == 0)
                     {
                         header.getCell(COL_MIN_INTENSITY).setCellValue("Min. intensity");
                         header.getCell(COL_AVG_INTENSITY).setCellValue("Avg. intensity");
                         header.getCell(COL_MAX_INTENSITY).setCellValue("Max. intensity");
                         header.getCell(COL_SUM_INTENSITY).setCellValue("Sum. intensity");
                     }
                     else
                     {
                         for (int c = 0; c < sizeC; c++)
                         {
                             header.getCell(COL_MIN_INTENSITY + 4 * c).setCellValue("Min. (" + sequence.getChannelName(c) + ")");
                             header.getCell(COL_AVG_INTENSITY + 4 * c).setCellValue("Avg. (" + sequence.getChannelName(c) + ")");
                             header.getCell(COL_MAX_INTENSITY + 4 * c).setCellValue("Max. (" + sequence.getChannelName(c) + ")");
                             header.getCell(COL_SUM_INTENSITY + 4 * c).setCellValue("Sum. (" + sequence.getChannelName(c) + ")");
                         }
                     }
                 }
                 
                 Row row = sheet.createRow(roiID);
                 
                 Cell name = row.getCell(COL_NAME);
                 name.setCellValue(roi.getName());
                 name.setCellStyle(nameStyle);
                 
                 double[] min = new double[sequence.getSizeC()];
                 double[] max = new double[sequence.getSizeC()];
                 double[] sum = new double[sequence.getSizeC()];
                 double[] cpt = new double[sequence.getSizeC()];
                 
                 row.getCell(COL_SURFACE).setCellValue(roi.getNumberOfContourPoints());
                 row.getCell(COL_VOLUME).setCellValue(roi.getNumberOfPoints());
                 
                 if (roi instanceof ROI2D)
                 {
                     ROI2D r2 = (ROI2D) roi;
                     
                     // set x,y,w,h
                     Rectangle bounds = r2.getBounds();
                     row.getCell(COL_X).setCellValue(bounds.getX());
                     row.getCell(COL_Y).setCellValue(bounds.getY());
                     row.getCell(COL_WIDTH).setCellValue(bounds.getWidth());
                     row.getCell(COL_HEIGHT).setCellValue(bounds.getHeight());
                     
                     boolean[] mask = r2.getBooleanMask(true).mask;
                     Object[][] z_c_xy = (Object[][]) sequence.getDataXYCZ(sequence.getFirstViewer().getPositionT());
                     boolean signed = sequence.getDataType_().isSigned();
                     int width = sequence.getSizeX();
                     int height = sequence.getSizeY();
                     
                     int ioff = bounds.x + bounds.y * width;
                     int moff = 0;
                     
                     int minZ = r2.getZ(), maxZ = r2.getZ();
                     
                     if (r2.getZ() == -1)
                     {
                         minZ = 0;
                         maxZ = sequence.getSizeZ();
                     }
                     
                     for (int iy = bounds.y, my = 0; my < bounds.height; my++, iy++, ioff += sequence.getSizeX() - bounds.width)
                         for (int ix = bounds.x, mx = 0; mx < bounds.width; mx++, ix++, ioff++, moff++)
                         {
                             if (iy >= 0 && ix >= 0 && iy < height && ix < width && mask[moff])
                             {
                                for (int z = minZ; z <= maxZ; z++)
                                     for (int c = 0; c < sum.length; c++)
                                     {
                                         cpt[c]++;
                                         double val = Array1DUtil.getValue(z_c_xy[z][c], ioff, signed);
                                         sum[c] += val;
                                         if (val > max[c]) max[c] = val;
                                         if (val < min[c]) min[c] = val;
                                     }
                             }
                         }
                 }
                 else if (roi instanceof ROI3D)
                 {
                     ROI3D r3 = (ROI3D) roi;
                     
                     // set x,y,w,h
                     Rectangle3D.Integer bounds3 = r3.getBounds();
                     row.getCell(COL_X).setCellValue(bounds3.getX());
                     row.getCell(COL_Y).setCellValue(bounds3.getY());
                     row.getCell(COL_WIDTH).setCellValue(bounds3.getSizeX());
                     row.getCell(COL_HEIGHT).setCellValue(bounds3.getSizeY());
                     
                     TreeMap<Integer, BooleanMask2D> masks = r3.getBooleanMask(true).mask;
                     Object[][] z_c_xy = (Object[][]) sequence.getDataXYCZ(sequence.getFirstViewer().getPositionT());
                     boolean signed = sequence.getDataType_().isSigned();
                     int width = sequence.getSizeX();
                     int height = sequence.getSizeY();
                     
                     for (Integer z : masks.keySet())
                     {
                         boolean[] mask = masks.get(z).mask;
                         Rectangle bounds = masks.get(z).bounds;
                         int ioff = bounds.x + bounds.y * width;
                         int moff = 0;
                         
                         for (int iy = bounds.y, my = 0; my < bounds.height; my++, iy++, ioff += sequence.getSizeX() - bounds.width)
                             for (int ix = bounds.x, mx = 0; mx < bounds.width; mx++, ix++, ioff++, moff++)
                             {
                                 if (iy >= 0 && ix >= 0 && iy < height && ix < width && mask[moff])
                                 {
                                     for (int c = 0; c < sum.length; c++)
                                     {
                                         cpt[c]++;
                                         double val = Array1DUtil.getValue(z_c_xy[z][c], ioff, signed);
                                         sum[c] += val;
                                         if (val > max[c]) max[c] = val;
                                         if (val < min[c]) min[c] = val;
                                     }
                                 }
                             }
                     }
                 }
                 
                 for (int c = 0; c < sum.length; c++)
                 {
                     row.getCell(COL_MIN_INTENSITY + 4 * c).setCellValue(min[c]);
                     row.getCell(COL_MIN_INTENSITY + 4 * c + 1).setCellValue(sum[c] / cpt[c]);
                     row.getCell(COL_MIN_INTENSITY + 4 * c + 2).setCellValue(max[c]);
                     row.getCell(COL_MIN_INTENSITY + 4 * c + 3).setCellValue(sum[c]);
                 }
             }
             
             roiID++;
         }
         
         // this is mandatory since sheet creation cannot be detected
         book.valueChanged(book, null, wb);
     }
     
     // /**
     // * @param cc
     // * @return a triplet representing the radiuses of the best fitting ellipse (the third value is
     // 0
     // * for 2D objects)
     // */
     // public double[] computeEllipseDimensions(ConnectedComponent cc)
     // {
     // double[] axes = new double[3];
     //
     // try
     // {
     // if (is2D(cc))
     // {
     // Point2d radii = new Point2d();
     // computeEllipse(cc, null, radii, null, null);
     // axes[0] = radii.x;
     // axes[1] = radii.y;
     // }
     // else
     // {
     // Point3d radii = new Point3d();
     // computeEllipse(cc, null, radii, null, null);
     // axes[0] = radii.x;
     // axes[1] = radii.y;
     // axes[2] = radii.z;
     // }
     // }
     // catch (Exception e)
     // {
     // }
     //
     // return axes;
     // }
     //
     // /**
     // * Computes the bounding box of this component, and stores the result into the given arguments
     // *
     // * @param cc
     // * the input component
     // * @param bsCenter
     // * the computed center of the bounding sphere
     // * @param maxBounds
     // * the computed radius of the bounding sphere
     // */
     // public void computeBoundingSphere(ConnectedComponent cc, Point3d bsCenter, VarDouble
     // bsRadius)
     // {
     // bsCenter.set(cc.getMassCenter());
     // bsRadius.setValue(cc.getMaxDistanceTo(bsCenter));
     // }
     //
     // /**
     // * @return The 2D perimeter (or 3D surface) of this
     // */
     // public double computePerimeter(ROI roi)
     // {
     // double perimeter = 0;
     //
     // if (roi instanceof ROI2DArea)
     // {
     // ROI2DArea r2 = (ROI2DArea) roi;
     // BooleanMask2D mask = r2.getBooleanMask(true);
     // Point[] edge = mask.getEdgePoints();
     // boolean[] _mask = mask.mask;
     //
     // // count edges with 1, 2 or 3 neighbors
     // int aEdges = 0, bEdges = 0;
     // for (Point edgePoint : edge)
     // {
     // int xy = edgePoint.x + edgePoint.y * mask.bounds.width;
     // if (!_mask[xy - mask.bounds.width] || !_mask[xy - 1] || !_mask[xy + 1] || !_mask[xy +
     // mask.bounds.width])
     // {
     // aEdges++;
     // perimeter++;
     // }
     // else if (!_mask[xy - mask.bounds.width - 1] || !_mask[xy - mask.bounds.width + 1] ||
     // !_mask[xy + mask.bounds.width - 1] || !_mask[xy + mask.bounds.width + 1])
     // {
     // bEdges++;
     // perimeter++;
     // }
     // }
     //
     // // adjust the perimeter empirically according to the edge pixel distribution
     // double err = Math.log10(aEdges + bEdges) - Math.E;
     // perimeter *= (1 - (bEdges / aEdges / 2));
     // if (err > 0) perimeter -= (err * 20);
     // }
     // else if (roi instanceof ROI3D)
     // {
     // ROI3D r3 = (ROI3D) roi;
     // // TODO BooleanMask3D
     // }
     // else perimeter = roi.getPerimeter();
     //
     // return perimeter;
     // }
     //
     // /**
     // * Compute the sphericity (circularity in 2D) of the given ROI, using the ratio between its
     // * perimeter and volume.<br/>
     // * NOTE: this index uses a perimeter measure that is corrected for digitization artifacts (see
     // * the {@link #computePerimeter(ConnectedComponent, ArrayList, Sequence)} method)
     // *
     // * @param roi
     // * @return 1 for a perfect circle (or sphere), and lower than 1 otherwise
     // */
     // public double computeSphericity(ROI roi)
     // {
     // if (!(roi instanceof ROI2D) && !(roi instanceof ROI3D)) throw new
     // UnsupportedOperationException("Cannot compute the sphericity of a " +
     // roi.getClass().getSimpleName());
     //
     // double dim = roi instanceof ROI2D ? 2.0 : 3.0;
     //
     // double area = roi.getVolume();
     // double perimeter = computePerimeter(roi);
     //
     // // some verification code
     // //
     // // double peri_real = Math.PI * (maxBB.x - minBB.x + 1);
     // // System.out.println("p = " + perimeter + ", should be " + peri_real + " => " + (perimeter
     // // / peri_real));
     // //
     // // double surf = (perimeter * perimeter / (Math.PI * 4));
     // // double surf_real = (Math.PI * 0.25 * (maxBB.x - minBB.x + 1) * (maxBB.x - minBB.x + 1));
     // // System.out.println("surface = " + surf + ", should be " + surf_real + " => " + (surf_real
     // // / surf));
     // //
     // // end of the verification code
     //
     // return (Math.pow(Math.PI, 1.0 / dim) / perimeter) * Math.pow(area * dim * 2, (dim - 1) /
     // dim);
     // }
     //
     // /**
     // * Computes the eccentricity of the given component. This method fits an ellipse with radii a
     // * and b (in 2D) or an ellipsoid with radii a, b and c (in 3D) and returns in both cases the
     // * ratio b/a
     // *
     // * @param cc
     // * the input component
     // * @return the ratio b/a, where a and b are the two first largest ellipse radii (there are
     // only
     // * two in 2D)
     // */
     // public double computeEccentricity(ConnectedComponent cc)
     // {
     // if (is2D(cc))
     // {
     // try
     // {
     // Point2d radii = new Point2d();
     // computeEllipse(cc, null, radii, null, null);
     // return radii.x / radii.y;
     // }
     // catch (RuntimeException e)
     // {
     // // error during the ellipse computation
     // return Double.NaN;
     // }
     // }
     // else
     // {
     // Point3d radii = new Point3d();
     // try
     // {
     // computeEllipse(cc, null, radii, null, null);
     // return radii.x / radii.y;
     // }
     // catch (Exception e)
     // {
     // return Double.NaN;
     // }
     // }
     // }
     //
     // /**
     // * @param cc
     // * @return The hull ratio, measured as the ratio between the object volume and its convex hull
     // * (envelope)
     // */
     // public double computeHullRatio(ConnectedComponent cc)
     // {
     // double hull = 0.0;
     //
     // if (is2D(cc))
     // {
     // int i = 0, n = cc.getSize();
     // int[] xPoints = new int[n];
     // int[] yPoints = new int[n];
     // for (Point3i p : cc)
     // {
     // xPoints[i] = p.x;
     // yPoints[i] = p.y;
     // i++;
     // }
     //
     // if (n == 1)
     // hull = 1.0;
     // else
     // {
     // QuickHull2D qhull = new QuickHull2D(xPoints, yPoints, n);
     //
     // // formula: hull = 0.5 * sum( (x[i-1] * y[i]) - (y[i-1] * x[i]) )
     //
     // hull = (qhull.xPoints2[qhull.num - 1] * qhull.yPoints2[0]) - (qhull.xPoints2[0] *
     // qhull.yPoints2[qhull.num - 1]);
     // for (i = 1; i < qhull.num; i++)
     // hull += (qhull.xPoints2[i - 1] * qhull.yPoints2[i]) - (qhull.xPoints2[i] * qhull.yPoints2[i -
     // 1]);
     //
     // hull *= 0.5;
     // }
     // }
     // else
     // {
     // Point3d[] points = new Point3d[cc.getSize()];
     // int i = 0;
     // for (Point3i p : cc)
     // points[i++] = new Point3d(p.x, p.y, p.z);
     //
     // QuickHull3D qhull = new QuickHull3D(points);
     // int[][] hullFaces = qhull.getFaces();
     // Point3d[] hullPoints = qhull.getVertices();
     //
     // Vector3d v12 = new Vector3d();
     // Vector3d v13 = new Vector3d();
     // Vector3d cross = new Vector3d();
     //
     // for (int[] face : hullFaces)
     // {
     // Point3d p1 = hullPoints[face[0]];
     // Point3d p2 = hullPoints[face[1]];
     // Point3d p3 = hullPoints[face[2]];
     //
     // v12.sub(p2, p1);
     // v13.sub(p3, p1);
     // cross.cross(v12, v13);
     //
     // double surf = cross.length() * 0.5;
     //
     // cross.normalize();
     // hull += surf * cross.x * (p1.x + p2.x + p3.x);
     // }
     //
     // }
     //
     // return hull == 0.0 ? 0.0 : Math.min(1.0, cc.getSize() / hull);
     // }
     //
     // /**
     // * Computes the geometric moment of the given component
     // *
     // * @param roi
     // * the input component
     // * @param p
     // * the moment order along X
     // * @param q
     // * the moment order along Y
     // * @param r
     // * the moment order along Z (set to 0 if the object is 2D)
     // * @return the geometric moment
     // */
     // public double computeGeometricMoment(ROI roi, int p, int q, int r)
     // {
     // double moment = 0;
     //
     // Point3d center = roi.getMassCenter();
     //
     // if (roi instanceof ROI2D)
     // {
     // for (Point3i point : roi)
     // moment += Math.pow(point.x - center.x, p) * Math.pow(point.y - center.y, q);
     // }
     // else
     // {
     // for (Point3i point : roi)
     // moment += Math.pow(point.x - center.x, p) * Math.pow(point.y - center.y, q) *
     // Math.pow(point.z - center.z, r);
     // }
     // return moment;
     // }
 }
