 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package structurevis.ui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import structurevis.data.SparseVector;
 import structurevis.structures.Structure;
 import structurevis.structures.StructureParser;
 
 /**
  *
  * @author Michael Golden
  */
 public class FullGenomeDrawPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
 
     Point2D.Double[] fullCoordinates;
     ArrayList<CoordinatesAndNucleotidePosition> coordinatesAndPosMinList; // left-most x-coordinates in ascending order of index, for optimal binary search
     ArrayList<Double> sortedXPositionMinList;
     ArrayList<CoordinatesAndNucleotidePosition> coordinatesAndPosMaxList; // right-most x-coordinates in ascending order of index, for optimal binary search
     ArrayList<Double> sortedXPositionMaxList;
     int[] structures;
     double width = 0;
     double basePosY = 0;
     double lowestPosY = Double.MIN_VALUE;
     boolean forceRepaint = true;
     final static BasicStroke normalStroke = new BasicStroke(2.5f);
     double nucleotideRadius = 6;
     double nucleotideDiameter = nucleotideRadius * 2;
     //boolean useThresholds = true;
 
     public static File getCacheFile(File collectionFolder, int maxSubstructureSize) {
         return new File(collectionFolder.getPath() + "/" + maxSubstructureSize + "_fullgenome.cache");
     }
 
     public void cache(int maxSubstructureSize) {
         File cacheFile = getCacheFile(mainapp.collectionFolder, maxSubstructureSize);
         try {
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
             out.writeInt(fullCoordinates.length);
             for (int i = 0; i < fullCoordinates.length; i++) {
                 if (fullCoordinates[i] != null) {
                     out.writeDouble(fullCoordinates[i].x);
                     out.writeDouble(fullCoordinates[i].y);
                 } else {
                     out.writeDouble(Double.MIN_VALUE);
                     out.writeDouble(Double.MIN_VALUE);
                 }
             }
             out.writeInt(coordinatesAndPosMinList.size());
             for (int i = 0; i < coordinatesAndPosMinList.size(); i++) {
                 out.writeDouble(coordinatesAndPosMinList.get(i).p.x);
                 out.writeDouble(coordinatesAndPosMinList.get(i).p.y);
                 out.writeInt(coordinatesAndPosMinList.get(i).nucleotidePosition);
             }
             out.writeInt(sortedXPositionMinList.size());
             for (int i = 0; i < sortedXPositionMinList.size(); i++) {
                 out.writeDouble(sortedXPositionMinList.get(i));
             }
             out.writeInt(coordinatesAndPosMaxList.size());
             for (int i = 0; i < coordinatesAndPosMaxList.size(); i++) {
                 out.writeDouble(coordinatesAndPosMaxList.get(i).p.x);
                 out.writeDouble(coordinatesAndPosMaxList.get(i).p.y);
                 out.writeInt(coordinatesAndPosMaxList.get(i).nucleotidePosition);
             }
             out.writeInt(sortedXPositionMaxList.size());
             for (int i = 0; i < sortedXPositionMaxList.size(); i++) {
                 out.writeDouble(sortedXPositionMaxList.get(i));
             }
             out.writeInt(structures.length);
             for (int i = 0; i < structures.length; i++) {
                 out.writeInt(structures[i]);
             }
             out.writeDouble(width);
             out.writeDouble(basePosY);
             out.writeDouble(lowestPosY);
             out.close();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public boolean load(int maxSubstructureSize) {
         File cacheFile = getCacheFile(mainapp.collectionFolder, maxSubstructureSize);
         if (cacheFile.exists()) {
             try {
                 DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
 
                 fullCoordinates = new Point2D.Double[in.readInt()];
                 for (int i = 0; i < fullCoordinates.length; i++) {
                     double x = in.readDouble();
                     double y = in.readDouble();
                     if (!(x == Double.MIN_VALUE && y == Double.MIN_VALUE)) {
                         fullCoordinates[i] = new Point2D.Double(x, y);
                     }
                 }
                 int len = in.readInt();
                 for (int i = 0; i < len; i++) {
                     Point2D.Double p = new Point2D.Double(in.readDouble(), in.readDouble());
                     coordinatesAndPosMinList.add(new CoordinatesAndNucleotidePosition(p, in.readInt()));
                 }
                 len = in.readInt();
                 for (int i = 0; i < len; i++) {
                     sortedXPositionMinList.add(in.readDouble());
                 }
                 len = in.readInt();
                 for (int i = 0; i < len; i++) {
                     Point2D.Double p = new Point2D.Double(in.readDouble(), in.readDouble());
                     coordinatesAndPosMaxList.add(new CoordinatesAndNucleotidePosition(p, in.readInt()));
                 }
                 len = in.readInt();
                 for (int i = 0; i < len; i++) {
                     sortedXPositionMaxList.add(in.readDouble());
                 }
                 structures = new int[in.readInt()];
                 for (int i = 0; i < structures.length; i++) {
                     structures[i] = in.readInt();
                 }
                 width = in.readDouble();
                 basePosY = in.readDouble();
                 lowestPosY = in.readDouble();
                 in.close();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
             return true;
         }
         return false;
     }
     JPopupMenu popupMenu = new JPopupMenu();
     JMenuItem gotoStructureItem = new JMenuItem();
     int gotoStructure = -1;
 
     public FullGenomeDrawPanel() {
         addMouseListener(this);
         addMouseMotionListener(this);
 
         gotoStructureItem.addActionListener(this);
         popupMenu.add(gotoStructureItem);
     }
     MainApp mainapp = null;
     boolean initialised = false;
 
     public int getMaxEndPosition(ArrayList<Structure> substructures) {
         int max = 0;
         for (int i = 0; i < substructures.size(); i++) {
             max = Math.max(substructures.get(i).getEndPosition(), max);
         }
         return max;
     }
 
     public void initialise(MainApp mainapp, int maxSubstructureSize) {
         this.mainapp = mainapp;
 
         if (mainapp != null && mainapp.structureCollection != null) {
             String consensusStructure = mainapp.structureCollection.dotBracketStructure;
             ArrayList<Structure> substructures = StructureParser.enumerateAdjacentSubstructures(consensusStructure, 0, maxSubstructureSize, mainapp.structureCollection.circularGenome);
             fullCoordinates = new Point2D.Double[Math.max(consensusStructure.length(), getMaxEndPosition(substructures))];
             structures = new int[fullCoordinates.length];
 
             //fullCoordinates = new Point2D.Double[consensusStructure.length()];
             //structures = new int[consensusStructure.length()];
             coordinatesAndPosMinList = new ArrayList<CoordinatesAndNucleotidePosition>();
             coordinatesAndPosMaxList = new ArrayList<CoordinatesAndNucleotidePosition>();
             sortedXPositionMinList = new ArrayList<Double>();
             sortedXPositionMaxList = new ArrayList<Double>();
             Arrays.fill(structures, -1);
             double offsetX = 0;
             int lastEndIndex = 0;
             double pairedDistance = 15;
             double unpairedLength = 15;
             double maxXcoordinate = 0;
 
             // determine full genome structure coordinates
             if (!load(maxSubstructureSize)) {
                 for (int i = 0; i < substructures.size(); i++) {
                    ArrayList<Point2D.Double> coordinates = mainapp.getStructureCoordinates(substructures.get(i).getDotBracketString());
                     Point2D.Double[] normalisedCoordinates = MainApp.normaliseStructureCoordinates(coordinates);
                     int startIndex = substructures.get(i).startPosition - 1;
 
                     Arrays.fill(structures, startIndex, startIndex + substructures.get(i).length, i);
                     offsetX = maxXcoordinate + unpairedLength; // offset for next substructure
                     double maxY = normalisedCoordinates[0].y;
 
                     // get substructure coordinates
                     for (int j = 0; j < normalisedCoordinates.length; j++) {
                         fullCoordinates[startIndex + j] = new Point2D.Double();
                         fullCoordinates[startIndex + j].x = offsetX + normalisedCoordinates[j].x;
                         fullCoordinates[startIndex + j].y = normalisedCoordinates[j].y - maxY;
                         maxXcoordinate = Math.max(maxXcoordinate, fullCoordinates[startIndex + j].x);
                         width = Math.max(width, fullCoordinates[startIndex + j].x);
                         basePosY = Math.max(basePosY, normalisedCoordinates[j].y);
                         lowestPosY = Math.max(fullCoordinates[startIndex + j].y, lowestPosY);
 
                         coordinatesAndPosMinList.add(new CoordinatesAndNucleotidePosition(fullCoordinates[startIndex + j], startIndex + j));
                         coordinatesAndPosMaxList.add(new CoordinatesAndNucleotidePosition(fullCoordinates[startIndex + j], startIndex + j));
                         lastEndIndex = startIndex + j;
                         //System.out.println("A"+(startIndex + j));
                     }
                     lastEndIndex += 1;
 
                     int c = 1;
                     for (int j = lastEndIndex; (i + 1) < substructures.size() && j < substructures.get(i + 1).startPosition - 1; j++) {
                         fullCoordinates[j] = new Point2D.Double();
                         fullCoordinates[j].x = fullCoordinates[startIndex].x + (c * pairedDistance);
                         maxXcoordinate = Math.max(maxXcoordinate, fullCoordinates[j].x);
                         fullCoordinates[j].y = 0;
                         c++;
                         coordinatesAndPosMinList.add(new CoordinatesAndNucleotidePosition(fullCoordinates[j], j));
                         coordinatesAndPosMaxList.add(new CoordinatesAndNucleotidePosition(fullCoordinates[j], j));
                         //System.out.println("B"+j);
                     }
                 }
 
 
                 basePosY += pairedDistance;
                 lowestPosY += pairedDistance;
 
                 for (int i = 0; i < coordinatesAndPosMinList.size(); i++) {
                     for (int j = i + 1; j < coordinatesAndPosMinList.size(); j++) {
                         if (coordinatesAndPosMinList.get(i).p.x >= coordinatesAndPosMinList.get(j).p.x) {
                             coordinatesAndPosMinList.remove(j);
                             j--;
                         }
                     }
                 }
 
 
                 for (int i = 0; i < coordinatesAndPosMaxList.size(); i++) {
                     for (int j = i + 1; j < coordinatesAndPosMaxList.size(); j++) {
                         if (coordinatesAndPosMaxList.get(coordinatesAndPosMaxList.size() - i - 1).p.x <= coordinatesAndPosMaxList.get(coordinatesAndPosMaxList.size() - j - 1).p.x) {
                             coordinatesAndPosMaxList.remove(coordinatesAndPosMaxList.size() - j - 1);
                             j--;
                         }
                     }
                 }
 
                 for (int i = 0; i < coordinatesAndPosMinList.size(); i++) {
                     sortedXPositionMinList.add(coordinatesAndPosMinList.get(i).p.x);
                 }
 
                 for (int i = 0; i < coordinatesAndPosMaxList.size(); i++) {
                     sortedXPositionMaxList.add(coordinatesAndPosMaxList.get(i).p.x);
                 }
 
                 cache(maxSubstructureSize);
             }
 
             setPreferredSize(new Dimension((int) (width + 1), (int) (basePosY + lowestPosY + 1)));
             initialised = true;
         }
     }
 
     public void redraw() {
         forceRepaint = true;
         repaint();
     }
 
     @Override
     public void paintComponent(Graphics graphics) {
         super.paintComponent(graphics);
         Graphics2D g = (Graphics2D) graphics;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
         if (initialised) {
             Rectangle viewableArea = g.getClipBounds();
 
             setPreferredSize(new Dimension((int) (width + 1), (int) (basePosY + lowestPosY + 1)));
 
             g.setColor(Color.white);
             g.fillRect(viewableArea.x, viewableArea.y, viewableArea.width, viewableArea.height);
 
             int lessThanVisibleX = Collections.binarySearch(sortedXPositionMinList, (double) viewableArea.x);
 
 
             if (lessThanVisibleX < 0) {
                 lessThanVisibleX = -lessThanVisibleX - 1;
             }
             lessThanVisibleX = Math.min(lessThanVisibleX, coordinatesAndPosMinList.size() - 1);
             int startDrawingFromNucleotide = Math.max(coordinatesAndPosMinList.get(Math.max(lessThanVisibleX, 0)).nucleotidePosition - 1, 0);
 
             int moreThanVisibleX = Collections.binarySearch(sortedXPositionMaxList, (double) (viewableArea.x + viewableArea.width));
             if (moreThanVisibleX < 0) {
                 moreThanVisibleX = -moreThanVisibleX - 1;
             }
             int endDrawingAtNucleotide = Math.min(coordinatesAndPosMaxList.get(Math.min(moreThanVisibleX, coordinatesAndPosMaxList.size() - 1)).nucleotidePosition + 1, fullCoordinates.length);
 
             if (mainapp.data2D != null) {
                 g.setStroke(normalStroke);
                 for (int posi = startDrawingFromNucleotide; posi < endDrawingAtNucleotide - 1; posi++) {
                     if (posi < mainapp.data2D.matrix.rows.length) {
                         SparseVector row = mainapp.data2D.matrix.rows[posi];
                         if (row != null) {
                             Enumeration<Integer> en = row.table.keys();
                             while (en.hasMoreElements()) {
                                 int posj = en.nextElement(); // TODO order matrix j-indices for efficient iteration
                                 double val = row.get(posj);
                                 if (val != mainapp.data2D.matrix.emptyValue) {
                                     if (mainapp.maxDistance == -1 || Math.abs(posj - posi) <= mainapp.maxDistance) {
                                         if (((!mainapp.useLowerThreshold2D || val >= mainapp.thresholdMin2D) && (!mainapp.useUpperThreshold2D || val <= mainapp.thresholdMax2D))) {
                                             if (fullCoordinates[posj] != null) {
                                                 Line2D.Double line = new Line2D.Double(fullCoordinates[posi].x, fullCoordinates[posi].y + basePosY, fullCoordinates[posj].x, fullCoordinates[posj].y + basePosY);
                                                 Color c = mainapp.data2D.colorGradientSecondary.getColor((float) mainapp.data2D.dataTransform.transform(val));
                                                 g.setColor(c);
                                                 g.draw(line);
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             g.setStroke(new BasicStroke());
 
             // matrix transpose - allows efficient (ordered) iteration through j-indices of matrix
             if (mainapp.data2D != null) {
                 g.setStroke(normalStroke);
                 for (int posi = startDrawingFromNucleotide; posi < endDrawingAtNucleotide - 1; posi++) {
                     if (posi < mainapp.data2D.matrix.rows.length) {
                         SparseVector row = mainapp.data2D.matrixTranspose.rows[posi];
                         if (row != null) {
                             Enumeration<Integer> en = row.table.keys();
                             while (en.hasMoreElements()) {
                                 int posj = en.nextElement();
                                 if (posj < startDrawingFromNucleotide || posj >= endDrawingAtNucleotide - 1) { // don't redraw over what's already been drawn above (affects transparent colours)
                                     double val = row.get(posj);
                                     if (val != mainapp.data2D.matrixTranspose.emptyValue) {
                                         if (mainapp.maxDistance == -1 || Math.abs(posj - posi) <= mainapp.maxDistance) {
                                             if (((!mainapp.useLowerThreshold2D || val >= mainapp.thresholdMin2D) && (!mainapp.useUpperThreshold2D || val <= mainapp.thresholdMax2D))) {
                                                 //if (val >= mainapp.thresholdMin2D && val <= mainapp.thresholdMax2D) {
                                                 if (fullCoordinates[posj] != null) {
                                                     Line2D.Double line = new Line2D.Double(fullCoordinates[posi].x, fullCoordinates[posi].y + basePosY, fullCoordinates[posj].x, fullCoordinates[posj].y + basePosY);
                                                     Color c = mainapp.data2D.colorGradientSecondary.getColor((float) mainapp.data2D.dataTransform.transform(val));
                                                     g.setColor(c);
                                                     g.draw(line);
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             g.setStroke(new BasicStroke());
 
             /*
              * if (mainapp.data2D != null) { g.setStroke(normalStroke); for (int
              * posi = startDrawingFromNucleotide; posi < endDrawingAtNucleotide
              * - 1; posi++) { for (int posj = startDrawingFromNucleotide; posj <
              * endDrawingAtNucleotide - 1; posj++) { double val =
              * mainapp.data2D.matrix.get(posi, posj); if (val !=
              * mainapp.data2D.matrix.emptyValue) { if (val >=
              * mainapp.thresholdMin2D && val <= mainapp.thresholdMax2D) { if
              * (fullCoordinates[posj] != null) { Line2D.Double line = new
              * Line2D.Double(fullCoordinates[posi].x, fullCoordinates[posi].y +
              * basePosY, fullCoordinates[posj].x, fullCoordinates[posj].y +
              * basePosY); Color c =
              * mainapp.data2D.colorGradient.getColor((float)
              * mainapp.data2D.dataTransform.transform(val));
              * //System.out.println(posi + "\t" + posj + "\t" + val);
              * g.setColor(c); g.draw(line); } } } } } } g.setStroke(new
              * BasicStroke());
              */
 
             //int lastPositionDrawn = 0;
             //int nucleotidePositionSpacing = 10;
 
             // 1D and nucleotide data
             for (int pos = startDrawingFromNucleotide; pos < endDrawingAtNucleotide; pos++) {
                 int posWrap = pos % mainapp.structureCollection.genomeLength;
 
                 // draw nucleotide circle
                 if (fullCoordinates[pos] != null && pos + 1 < fullCoordinates.length && fullCoordinates[pos + 1] != null) {
                     if (pos + 1 < structures.length && structures[pos] != structures[pos + 1]) {
                         Line2D.Double line = new Line2D.Double(fullCoordinates[pos].x, fullCoordinates[pos].y + basePosY, fullCoordinates[pos + 1].x, fullCoordinates[pos + 1].y + basePosY);
                         g.setColor(Color.black);
                         g.draw(line);
                     }
                     Color nucleotideBackgroundColor = mainapp.missingDataColor;
 
                     if (mainapp.data1D != null) {
                         double p = mainapp.data1D.data[posWrap];
                         if (mainapp.data1D.used[posWrap] && ((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                             nucleotideBackgroundColor = mainapp.data1D.colorGradientSecondary.getColor(mainapp.data1D.dataTransform.transform((float) p));
                         } else if (!((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                             nucleotideBackgroundColor = mainapp.filteredDataColor;
                         }
                     }
                     g.setColor(nucleotideBackgroundColor);
 
                     Ellipse2D.Double nucleotide = new Ellipse2D.Double(fullCoordinates[pos].x - nucleotideRadius, (fullCoordinates[pos].y + basePosY - nucleotideRadius), nucleotideDiameter, nucleotideDiameter);
                     g.fill(nucleotide);
                     g.setColor(Color.black);
                     g.draw(nucleotide);
 
                     // draw nucleotide position
                     if (fullCoordinates[pos].y == 0 && (pos + 1) % 10 == 0) {
 
                         Line2D.Double line = new Line2D.Double(fullCoordinates[pos].x, fullCoordinates[pos].y + basePosY + nucleotideRadius - 1, fullCoordinates[pos].x, fullCoordinates[pos].y + basePosY + nucleotideRadius + 1);
                         g.setColor(Color.black);
                         g.draw(line);
                         StructureDrawPanel.drawStringCentred(g, fullCoordinates[pos].x, fullCoordinates[pos].y + basePosY + nucleotideDiameter, (posWrap + 1) + "");
                     }
 
                     if (mainapp.nucleotideComposition != null) {
                         if (pos < fullCoordinates.length && posWrap < mainapp.nucleotideComposition.consensus.length()) {
                             g.setColor(ColorTools.selectBestForegroundColor(nucleotideBackgroundColor, Color.white, Color.black));
                             StructureDrawPanel.drawStringCentred(g, fullCoordinates[pos].x, fullCoordinates[pos].y + basePosY - 1, mainapp.nucleotideComposition.consensus.charAt(posWrap) + "");
                         }
                     }
                 }
             }
         }
     }
 
     public void mouseClicked(MouseEvent e) {
         int lessThanVisibleX = Collections.binarySearch(sortedXPositionMinList, (double) e.getX() - nucleotideDiameter);
 
         if (lessThanVisibleX < 0) {
             lessThanVisibleX = -lessThanVisibleX - 1;
         }
         int startDrawingFromNucleotide = Math.max(coordinatesAndPosMinList.get(Math.max(lessThanVisibleX, 0)).nucleotidePosition - 1, 0);
 
         int moreThanVisibleX = Collections.binarySearch(sortedXPositionMaxList, (double) e.getX() + nucleotideDiameter);
         if (moreThanVisibleX < 0) {
             moreThanVisibleX = -moreThanVisibleX - 1;
         }
         int endDrawingAtNucleotide = Math.min(coordinatesAndPosMaxList.get(Math.min(moreThanVisibleX, coordinatesAndPosMaxList.size() - 1)).nucleotidePosition + 1, fullCoordinates.length);
 
 
         Point2D.Double mousePosition = new Point2D.Double(e.getX(), e.getY());
 
         for (int posi = startDrawingFromNucleotide; posi < endDrawingAtNucleotide; posi++) {
             Point2D.Double nucleotide = new Point2D.Double(fullCoordinates[posi].x, fullCoordinates[posi].y + basePosY);
             if (mousePosition.distance(nucleotide) <= nucleotideRadius) {
                 System.out.println("User clicked at " + (posi % mainapp.structureCollection.genomeLength + 1));
             }
         }
     }
 
     public void mousePressed(MouseEvent e) {
     }
 
     public void mouseReleased(MouseEvent e) {
         if (SwingUtilities.isRightMouseButton(e)) {
             int nucleotide = getNucleotideAtPosition(e.getX(), e.getY());
             gotoStructure = mainapp.getStructureIndexAtPosition(nucleotide);
             if (gotoStructure != -1) {
                 this.gotoStructureItem.setText("Open structure " + gotoStructure);
                 this.popupMenu.show(this, e.getX(), e.getY());
             }
         }
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void mouseDragged(MouseEvent e) {
     }
 
     public void mouseMoved(MouseEvent e) {
         // System.out.println("----");
     }
 
     public int getNucleotideAtPosition(double x, double y) {
         int lessThanVisibleX = Collections.binarySearch(sortedXPositionMinList, x - nucleotideDiameter);
 
         if (lessThanVisibleX < 0) {
             lessThanVisibleX = -lessThanVisibleX - 1;
         }
         int startDrawingFromNucleotide = Math.max(coordinatesAndPosMinList.get(Math.max(lessThanVisibleX, 0)).nucleotidePosition - 1, 0);
 
         int moreThanVisibleX = Collections.binarySearch(sortedXPositionMaxList, x + nucleotideDiameter);
         if (moreThanVisibleX < 0) {
             moreThanVisibleX = -moreThanVisibleX - 1;
         }
         int endDrawingAtNucleotide = Math.min(coordinatesAndPosMaxList.get(Math.min(moreThanVisibleX, coordinatesAndPosMaxList.size() - 1)).nucleotidePosition + 1, fullCoordinates.length);
 
 
         Point2D.Double mousePosition = new Point2D.Double(x, y);
 
         for (int posi = startDrawingFromNucleotide; posi < endDrawingAtNucleotide; posi++) {
             Point2D.Double nucleotide = new Point2D.Double(fullCoordinates[posi].x, fullCoordinates[posi].y + basePosY);
             if (mousePosition.distance(nucleotide) <= nucleotideRadius) {
                 return (posi + 1);
             }
         }
 
         return -1;
     }
 
     public void actionPerformed(ActionEvent e) {
 
         if (e.getSource().equals(gotoStructureItem)) {
             if (gotoStructure != -1) {
                 mainapp.openStructure(gotoStructure);
             }
         }
     }
 
     class CoordinatesAndNucleotidePosition implements Comparable {
 
         Point2D.Double p;
         int nucleotidePosition;
 
         public CoordinatesAndNucleotidePosition(Point2D.Double p, int nucleotidePos) {
             this.p = p;
             this.nucleotidePosition = nucleotidePos;
         }
 
         // compare p.x values
         public int compareTo(Object o) {
             CoordinatesAndNucleotidePosition other = (CoordinatesAndNucleotidePosition) o;
 
             if (p.x < other.p.x) {
                 return -1;
             } else if (p.x > other.p.x) {
                 return 1;
             }
 
             return 0;
         }
     }
 }
