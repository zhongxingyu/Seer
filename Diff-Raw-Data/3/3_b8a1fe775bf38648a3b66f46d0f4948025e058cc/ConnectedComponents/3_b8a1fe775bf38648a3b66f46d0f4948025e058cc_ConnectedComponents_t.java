 package plugins.adufour.connectedcomponents;
 
 import icy.file.xls.XlsManager;
 import icy.image.IcyBufferedImage;
 import icy.image.colormap.FireColorMap;
 import icy.main.Icy;
 import icy.roi.ROI2D;
 import icy.roi.ROI2DArea;
 import icy.sequence.Sequence;
 import icy.sequence.VolumetricImage;
 import icy.swimmingPool.SwimmingObject;
 import icy.type.DataType;
 import icy.type.collection.array.Array1DUtil;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3i;
 
 import plugins.adufour.blocks.lang.Block;
 import plugins.adufour.blocks.util.VarList;
 import plugins.adufour.ezplug.EzGroup;
 import plugins.adufour.ezplug.EzLabel;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzVar;
 import plugins.adufour.ezplug.EzVarBoolean;
 import plugins.adufour.ezplug.EzVarEnum;
 import plugins.adufour.ezplug.EzVarFile;
 import plugins.adufour.ezplug.EzVarInteger;
 import plugins.adufour.ezplug.EzVarListener;
 import plugins.adufour.ezplug.EzVarSequence;
 import plugins.adufour.vars.lang.VarGenericArray;
 import plugins.adufour.vars.lang.VarSequence;
 import plugins.nchenouard.spot.DetectionResult;
 import plugins.nchenouard.spot.Spot;
 
 public class ConnectedComponents extends EzPlug implements Block
 {
     /**
      * List of extraction methods suitable for the Connected Components plugin
      * 
      * @author Alexandre Dufour
      * 
      */
     public enum ExtractionType
     {
         /**
          * The user defined value is considered as the background, and all components with a
          * different intensity should be extracted (regardless of intensity variations)
          */
         BACKGROUND,
         /**
          * The user defined value is considered as the background, and all components with a
          * different intensity should be extracted, accounting for intensity variations to
          * distinguish touching components
          */
         BACKGROUND_LABELED,
         /**
          * Extracts components with pixel intensities matching the user defined value
          */
         VALUE
     }
     
     public enum Sorting
     {
         /**
          * Components are not sorted
          */
         ARBITRARY(null),
         /**
          * Components are sorted by ascending depth value
          */
         DEPTH_ASC(new Comparator<ConnectedComponent>()
         {
             @Override
             public int compare(ConnectedComponent o1, ConnectedComponent o2)
             {
                 return (int) Math.signum(o1.getZ() - o2.getZ());
             }
         }),
         /**
          * Components are sorted by descending depth value
          */
         DEPTH_DESC(new Comparator<ConnectedComponent>()
         {
             @Override
             public int compare(ConnectedComponent o1, ConnectedComponent o2)
             {
                 return (int) Math.signum(o2.getZ() - o1.getZ());
             }
         });
         
         /**
          * The comparator which can be used to sort an array of connected components
          */
         public final Comparator<ConnectedComponent> comparator;
         
         Sorting(Comparator<ConnectedComponent> comparator)
         {
             this.comparator = comparator;
         }
     }
     
     protected EzVarSequence                         input                  = new EzVarSequence("Input");
     
     protected EzVarEnum<ExtractionType>             extractionMethod       = new EzVarEnum<ExtractionType>("Extraction mode", ExtractionType.values());
     
     protected EzLabel                               extractionMethodDetail = new EzLabel("Description");
     
     protected EzVarInteger                          background             = new EzVarInteger("Value", 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
     
     protected EzVarBoolean                          discardEdgesX          = new EzVarBoolean("  along X", true);
     protected EzVarBoolean                          discardEdgesY          = new EzVarBoolean("  along Y", true);
     protected EzVarBoolean                          discardEdgesZ          = new EzVarBoolean("  along Z", true);
     
     protected EzVarBoolean                          boundSize              = new EzVarBoolean("Filter objects by size", false);
     
     protected EzVarInteger                          minSize                = new EzVarInteger("Min. size", 1, 1, Integer.MAX_VALUE, 1);
     protected EzVarInteger                          maxSize                = new EzVarInteger("Max. size", 10000, 1, Integer.MAX_VALUE, 1);
     
     protected EzVarBoolean                          labeledExtraction;
     
     protected EzLabel                               objectCount;
     
     protected EzVarBoolean                          exportSequence         = new EzVarBoolean("Labeled sequence", true);
     
     protected EzVarEnum<Sorting>                    labelSorting           = new EzVarEnum<ConnectedComponents.Sorting>("Sort components", Sorting.values(), Sorting.ARBITRARY);
     
     protected EzVarBoolean                          exportSwPool           = new EzVarBoolean("Swimming pool", false);
     protected EzVarBoolean                          exportROI              = new EzVarBoolean("ROI (2D only)", false);
     protected EzVarBoolean                          exportExcel            = new EzVarBoolean("Export to Excel", false);
     protected EzVarFile                             exportExcelFile        = new EzVarFile("Excel file", "");
     
     protected VarSequence                           outputSequence         = new VarSequence("output", null);
     protected VarGenericArray<ConnectedComponent[]> outputCCs              = new VarGenericArray<ConnectedComponent[]>("components", ConnectedComponent[].class, null);
     
     @Override
     protected void initialize()
     {
         addEzComponent(input);
         input.addVarChangeListener(new EzVarListener<Sequence>()
         {
             @Override
             public void variableChanged(EzVar<Sequence> source, Sequence newValue)
             {
                 if (newValue != null)
                 {
                     boolean doZ = newValue.getSizeZ() > 1;
                     discardEdgesZ.setValue(doZ);
                     discardEdgesZ.setVisible(doZ);
                 }
             }
         });
         
         addEzComponent(extractionMethod);
         extractionMethod.addVarChangeListener(new EzVarListener<ExtractionType>()
         {
             @Override
             public void variableChanged(EzVar<ExtractionType> source, ExtractionType newValue)
             {
                 switch (newValue)
                 {
                     case BACKGROUND:
                         extractionMethodDetail.setText("Standard mode: extracts all pixels different than the given background value, regardless of their intensity");
                     break;
                     case BACKGROUND_LABELED:
                         extractionMethodDetail.setText("Standard labeled mode: extracts all pixels different than the background, however different intensities are seen as different objects");
                     break;
                     case VALUE:
                         extractionMethodDetail.setText("Value-extraction mode: extracts all pixels with the specified value");
                 }
             }
         });
         
         addEzComponent(extractionMethodDetail);
         addEzComponent(background);
         
         addEzComponent(new EzGroup("Discard edges", discardEdgesX, discardEdgesY, discardEdgesZ));
         
         addEzComponent(boundSize);
         addEzComponent(minSize);
         addEzComponent(maxSize);
         boundSize.addVisibilityTriggerTo(minSize, true);
         boundSize.addVisibilityTriggerTo(maxSize, true);
         
         addEzComponent(new EzGroup("Export", exportSequence, labelSorting, exportSwPool, exportROI, exportExcel, exportExcelFile));
         exportSequence.addVisibilityTriggerTo(labelSorting, true);
         exportExcel.addVisibilityTriggerTo(exportExcelFile, true);
         
         addEzComponent(objectCount = new EzLabel(""));
     }
     
     @Override
     public void declareInput(VarList inputMap)
     {
         inputMap.add("input", input.getVariable());
         inputMap.add("method", extractionMethod.getVariable());
         inputMap.add("value", background.getVariable());
         inputMap.add("no edge (X)", discardEdgesX.getVariable());
         inputMap.add("no edge (Y)", discardEdgesY.getVariable());
         inputMap.add("no edge (Z)", discardEdgesZ.getVariable());
         inputMap.add("size filter", boundSize.getVariable());
         inputMap.add("min. size", minSize.getVariable());
         inputMap.add("max. size", maxSize.getVariable());
     }
     
     @Override
     public void declareOutput(VarList outputMap)
     {
         outputMap.add("labeled sequence", outputSequence);
         outputMap.add("objects", outputCCs);
     }
     
     @Override
     protected void execute()
     {
         ArrayList<ConnectedComponent> componentsList = new ArrayList<ConnectedComponent>();
         Map<Integer, List<ConnectedComponent>> componentsMap = new TreeMap<Integer, List<ConnectedComponent>>();
         
         int min = boundSize.getValue() ? minSize.getValue() : 0;
         int max = boundSize.getValue() ? maxSize.getValue() : Integer.MAX_VALUE;
         
         Sequence output = new Sequence();
         
         componentsMap = extractConnectedComponents(input.getValue(true), background.getValue(), extractionMethod.getValue(), discardEdgesX.getValue(), discardEdgesY.getValue(), discardEdgesZ.getValue(),
                 min, max, output);
         
         outputSequence.setValue(output);
         
         int nbObjects = 0;
         for (List<ConnectedComponent> ccs : componentsMap.values())
         {
             nbObjects += ccs.size();
             componentsList.addAll(ccs);
         }
         
         outputCCs.setValue(componentsList.toArray(new ConnectedComponent[nbObjects]));
         
         if (getUI() != null)
         {
             objectCount.setText("Total: " + nbObjects + " components");
             
             if (exportSequence.getValue())
             {
                 createLabeledSequence(output, componentsMap, labelSorting.getValue().comparator);
                 
                 addSequence(output);
             }
             
             if (exportSwPool.getValue())
             {
                 DetectionResult result = convertToDetectionResult(componentsMap, input.getValue());
                 SwimmingObject object = new SwimmingObject(result, "Set of " + nbObjects + " connected components");
                 Icy.getMainInterface().getSwimmingPool().add(object);
             }
             
             if (exportROI.getValue() && output.getSizeZ() == 1)
             {
                 Sequence in = input.getValue();
                 
                 in.beginUpdate();
                 
                 for (ROI2D roi : input.getValue().getROI2Ds())
                     if (roi instanceof ROI2DArea) in.removeROI(roi);
                 
                 for (List<ConnectedComponent> ccs : componentsMap.values())
                     for (ConnectedComponent cc : ccs)
                     {
                         ROI2DArea area = new ROI2DArea();
                        area.beginUpdate();
                         for (Point3i pt : cc)
                             area.addPoint(pt.x, pt.y);
                         area.setT(cc.getT());
                        area.endUpdate();
                         in.addROI(area);
                     }
                 
                 in.endUpdate();
             }
         }
         
         if (exportExcel.getValue())
         {
             XlsManager xlsManager;
             
             int page = 1;
             
             try
             {
                 xlsManager = new XlsManager(exportExcelFile.getValue());
                 xlsManager.createNewPage("Page " + page);
             }
             catch (Exception e)
             {
                 e.printStackTrace();
                 return;
             }
             
             xlsManager.setLabel(0, 0, "#");
             xlsManager.setLabel(1, 0, "t");
             xlsManager.setLabel(2, 0, "x");
             xlsManager.setLabel(3, 0, "y");
             xlsManager.setLabel(4, 0, "z");
             xlsManager.setLabel(5, 0, "area");
             xlsManager.setLabel(6, 0, "sphericity");
             xlsManager.setLabel(7, 0, "eccentricity");
             xlsManager.setLabel(8, 0, "M100");
             xlsManager.setLabel(9, 0, "M010");
             xlsManager.setLabel(10, 0, "M001");
             xlsManager.setLabel(11, 0, "M110");
             xlsManager.setLabel(12, 0, "M101");
             xlsManager.setLabel(13, 0, "M011");
             xlsManager.setLabel(14, 0, "M111");
             xlsManager.setLabel(15, 0, "M200");
             xlsManager.setLabel(16, 0, "M020");
             xlsManager.setLabel(17, 0, "M002");
             xlsManager.setLabel(18, 0, "M220");
             xlsManager.setLabel(19, 0, "M202");
             xlsManager.setLabel(20, 0, "M022");
             xlsManager.setLabel(21, 0, "M222");
             
             ConnectedComponentDescriptor shapeDescriptor = new ConnectedComponentDescriptor();
             int cpt = 1;
             for (Integer time : componentsMap.keySet())
                 for (ConnectedComponent cc : componentsMap.get(time))
                 {
                     boolean is2D = shapeDescriptor.is2D(cc);
                     Point3d center = cc.getMassCenter();
                     xlsManager.setNumber(0, cpt, cpt);
                     xlsManager.setNumber(1, cpt, time);
                     xlsManager.setNumber(2, cpt, center.x);
                     xlsManager.setNumber(3, cpt, center.y);
                     xlsManager.setNumber(4, cpt, center.z);
                     xlsManager.setNumber(5, cpt, cc.getSize());
                     xlsManager.setNumber(6, cpt, shapeDescriptor.computeSphericity(cc));
                     xlsManager.setNumber(7, cpt, shapeDescriptor.computeEccentricity(cc));
                     xlsManager.setNumber(8, cpt, shapeDescriptor.computeGeometricMoment(cc, 1, 0, 0));
                     xlsManager.setNumber(9, cpt, shapeDescriptor.computeGeometricMoment(cc, 0, 1, 0));
                     if (!is2D) xlsManager.setNumber(10, cpt, shapeDescriptor.computeGeometricMoment(cc, 0, 0, 1));
                     xlsManager.setNumber(11, cpt, shapeDescriptor.computeGeometricMoment(cc, 1, 1, 0));
                     if (!is2D) xlsManager.setNumber(12, cpt, shapeDescriptor.computeGeometricMoment(cc, 1, 0, 1));
                     if (!is2D) xlsManager.setNumber(13, cpt, shapeDescriptor.computeGeometricMoment(cc, 0, 1, 1));
                     if (!is2D) xlsManager.setNumber(14, cpt, shapeDescriptor.computeGeometricMoment(cc, 1, 1, 1));
                     xlsManager.setNumber(15, cpt, shapeDescriptor.computeGeometricMoment(cc, 2, 0, 0));
                     xlsManager.setNumber(16, cpt, shapeDescriptor.computeGeometricMoment(cc, 0, 2, 0));
                     if (!is2D) xlsManager.setNumber(17, cpt, shapeDescriptor.computeGeometricMoment(cc, 0, 0, 2));
                     xlsManager.setNumber(18, cpt, shapeDescriptor.computeGeometricMoment(cc, 2, 2, 0));
                     if (!is2D) xlsManager.setNumber(19, cpt, shapeDescriptor.computeGeometricMoment(cc, 2, 0, 2));
                     if (!is2D) xlsManager.setNumber(20, cpt, shapeDescriptor.computeGeometricMoment(cc, 0, 2, 2));
                     if (!is2D) xlsManager.setNumber(21, cpt, shapeDescriptor.computeGeometricMoment(cc, 2, 2, 2));
                     cpt++;
                     if (cpt == Short.MAX_VALUE)
                     {
                         page++;
                         xlsManager.createNewPage("Page " + page);
                         cpt = 1;
                     }
                 }
             
             xlsManager.SaveAndClose();
         }
     }
     
     @Override
     public void clean()
     {
     }
     
     private static class Label
     {
         final double    imageValue;
         
         /**
          * final label that should replace the current label if fusion is needed
          */
         int             targetLabelValue;
         
         /**
          * if non-null, indicates the parent object with which the current object should be fused
          */
         Label           targetLabel;
         
         int             size;
         
         private boolean onEdgeX;
         
         private boolean onEdgeY;
         
         private boolean onEdgeZ;
         
         /**
          * Creates a new label with the given value. If no parent is set to this label, the given
          * value will be the final one
          * 
          * @param value
          *            the pixel value
          * @param label
          *            the label value
          * @param onEdgeX
          *            true if the pixel is on the image edge along X
          * @param onEdgeY
          *            true if the pixel is on the image edge along Y
          * @param onEdgeZ
          *            true if the pixel is on the image edge along Z
          */
         Label(double value, int label)
         {
             this.imageValue = value;
             this.targetLabelValue = label;
         }
         
         /**
          * Retrieves the final object label (recursively)
          * 
          * @return
          */
         int getFinalLabelValue()
         {
             return targetLabel == null ? targetLabelValue : targetLabel.getFinalLabelValue();
         }
     }
     
     /**
      * Extracts the connected components in the given sequence with specified size bounds. Note that
      * the method works on both binary gray-scale images. If the input is not binary, any value
      * other than 0 will be extracted as a component.
      * 
      * @param inputSequence
      *            A binary or gray-scale sequence. If the sequence is gray-scale, any value other
      *            than 0 will be extracted as a component
      * @param labeledSequence
      *            a single-band integer sequence that will be filled with the labeled components
      * @return a map with extracted components, indexed by the sequence time points
      * @see ExtractionType
      * @see ConnectedComponent
      */
     public static Map<Integer, List<ConnectedComponent>> extractConnectedComponents(Sequence inputSequence, Sequence labeledSequence)
     {
         return extractConnectedComponents(inputSequence, false, 0, Integer.MAX_VALUE, labeledSequence);
     }
     
     /**
      * Extracts the connected components in the given sequence with specified size bounds. Note that
      * the method works on both binary gray-scale images. If the input is not binary, any value
      * other than 0 will be extracted as a component.
      * 
      * @param inputSequence
      *            A binary or gray-scale sequence. If the sequence is gray-scale, any value other
      *            than 0 will be extracted as a component
      * @param minSize
      *            the minimum size of the objects to extract
      * @param maxSize
      *            the maximum size of the objects to extract
      * @param labeledSequence
      *            a single-band integer sequence that will be filled with the labeled components
      * @return a map with extracted components, indexed by the sequence time points
      * @see ExtractionType
      * @see ConnectedComponent
      */
     public static Map<Integer, List<ConnectedComponent>> extractConnectedComponents(Sequence inputSequence, int minSize, int maxSize, Sequence labeledSequence)
     {
         return extractConnectedComponents(inputSequence, false, minSize, maxSize, labeledSequence);
     }
     
     /**
      * Extracts the connected components in the given sequence with specified size bounds. Note that
      * the method works on both binary gray-scale images. If the input is not binary, an option can
      * be set to distinguish touching objects which have a different intensity value, or to extract
      * a specific intensity value
      * 
      * @param inputSequence
      *            A binary or gray-scale sequence.
      * @param isInputLabeled
      *            if true, touching components with different pixel values are extracted separately
      * @param minSize
      *            the minimum size of the objects to extract
      * @param maxSize
      *            the maximum size of the objects to extract
      * @param labeledSequence
      *            a single-band integer sequence that will be filled with the labeled components
      * @return a map with extracted components, indexed by the sequence time points
      * @see ExtractionType
      * @see ConnectedComponent
      */
     public static Map<Integer, List<ConnectedComponent>> extractConnectedComponents(Sequence inputSequence, boolean isInputLabeled, int minSize, int maxSize, Sequence labeledSequence)
     {
         return extractConnectedComponents(inputSequence, 0, isInputLabeled ? ExtractionType.BACKGROUND_LABELED : ExtractionType.BACKGROUND, minSize, maxSize, labeledSequence);
     }
     
     /**
      * Extracts the connected components in the given sequence with specified size bounds. Note that
      * the method works on both binary gray-scale images. If the input is not binary, an option can
      * be set to distinguish touching objects which have a different intensity value, or to extract
      * a specific intensity value
      * 
      * @param inputSequence
      *            A binary or gray-scale sequence.
      * @param value
      *            the user value to be interpreted depending on the type parameter
      * @param type
      *            the extraction method (or how to interpret the value parameter)
      * @param minSize
      *            the minimum size of the objects to extract
      * @param maxSize
      *            the maximum size of the objects to extract
      * @param labeledSequence
      *            a single-band integer sequence that will be filled with the labeled components
      * @return a map with extracted components, indexed by the sequence time points
      * @see ExtractionType
      * @see ConnectedComponent
      */
     public static Map<Integer, List<ConnectedComponent>> extractConnectedComponents(Sequence inputSequence, double value, ExtractionType type, int minSize, int maxSize, Sequence labeledSequence)
     {
         return extractConnectedComponents(inputSequence, value, type, false, false, false, minSize, maxSize, labeledSequence);
     }
     
     /**
      * Extracts the connected components in the given sequence with specified size bounds. Note that
      * the method works on both binary gray-scale images. If the input is not binary, an option can
      * be set to distinguish touching objects which have a different intensity value, or to extract
      * a specific intensity value
      * 
      * @param inputSequence
      *            A binary or gray-scale sequence.
      * @param value
      *            the user value to be interpreted depending on the type parameter
      * @param type
      *            the extraction type (or how to interpret the value parameter)
      * @param noEdgeX
      *            set to true if components touching the image edge along X should be discarded
      *            during the extraction
      * @param noEdgeY
      *            set to true if components touching the image edge along Y should be discarded
      *            during the extraction
      * @param noEdgeZ
      *            set to true if components touching the image edge along Z should be discarded
      *            during the extraction
      * @param minSize
      *            the minimum size of the objects to extract
      * @param maxSize
      *            the maximum size of the objects to extract
      * @param labeledSequence
      *            a single-band integer sequence that will be filled with the labeled components
      * @return a map with extracted components, indexed by the sequence time points
      * @see ExtractionType
      * @see ConnectedComponent
      */
     public static Map<Integer, List<ConnectedComponent>> extractConnectedComponents(Sequence inputSequence, double value, ExtractionType type, boolean noEdgeX, boolean noEdgeY, boolean noEdgeZ,
             int minSize, int maxSize, Sequence labeledSequence)
     {
         int width = inputSequence.getSizeX();
         int height = inputSequence.getSizeY();
         
         if (labeledSequence == null) labeledSequence = new Sequence();
         
         Map<Integer, List<ConnectedComponent>> componentsMap = new TreeMap<Integer, List<ConnectedComponent>>();
         
         for (int t = 0; t < inputSequence.getSizeT(); t++)
         {
             for (int z = 0; z < inputSequence.getSizeZ(); z++)
                 labeledSequence.setImage(t, z, new IcyBufferedImage(width, height, 1, DataType.UINT));
             
             VolumetricImage volIN = inputSequence.getVolumetricImage(t);
             VolumetricImage volOUT = labeledSequence.getVolumetricImage(t);
             
             List<ConnectedComponent> components = extractConnectedComponents(volIN, value, type, noEdgeX, noEdgeY, noEdgeZ, minSize, maxSize, volOUT);
             
             // int cpt=1;
             for (ConnectedComponent cc : components)
             {
                 cc.setT(t);
                 // System.out.println(t + "\t" + cpt++ + "\t" + (int)cc.getMassCenter().x + "\t" +
                 // (int)cc.getMassCenter().y);
             }
             
             componentsMap.put(t, components);
         }
         
         labeledSequence.updateChannelsBounds(true);
         labeledSequence.getColorModel().setColormap(0, new FireColorMap());
         
         return componentsMap;
     }
     
     /**
      * Extracts the connected components in the given volumetric image with specified size bounds.
      * The the method works on both binary gray-scale images. If the input is not binary, an option
      * can be set to distinguish touching objects which have a different intensity value, or to
      * extract a specific intensity value
      * 
      * @param stack
      *            A binary or gray-scale volumetric image.
      * @param value
      *            the user value to be interpreted depending on the type parameter
      * @param type
      *            the extraction type (or how to interpret the value parameter)
      * @param noEdgeX
      *            set to true if components touching the image edge along X should be discarded
      *            during the extraction
      * @param noEdgeY
      *            set to true if components touching the image edge along Y should be discarded
      *            during the extraction
      * @param noEdgeZ
      *            set to true if components touching the image edge along Z should be discarded
      *            during the extraction
      * @param minSize
      *            the minimum size of the objects to extract
      * @param maxSize
      *            the maximum size of the objects to extract
      * @return a collection containing all components extracted.
      * @see ExtractionType
      * @see ConnectedComponent
      */
     public static List<ConnectedComponent> extractConnectedComponents(VolumetricImage stack, double value, ExtractionType type, boolean noEdgeX, boolean noEdgeY, boolean noEdgeZ, int minSize,
             int maxSize)
     {
         return extractConnectedComponents(stack, value, type, noEdgeX, noEdgeY, noEdgeZ, minSize, maxSize, new VolumetricImage());
     }
     
     /**
      * Extracts the connected components in the given volumetric image with specified size bounds.
      * The the method works on both binary gray-scale images. If the input is not binary, an option
      * can be set to distinguish touching objects which have a different intensity value, or to
      * extract a specific intensity value
      * 
      * @param stack
      *            A binary or gray-scale volumetric image.
      * @param value
      *            the user value to be interpreted depending on the type parameter
      * @param type
      *            the extraction type (or how to interpret the value parameter)
      * @param noEdgeX
      *            set to true if components touching the image edge along X should be discarded
      *            during the extraction
      * @param noEdgeY
      *            set to true if components touching the image edge along Y should be discarded
      *            during the extraction
      * @param noEdgeZ
      *            set to true if components touching the image edge along Z should be discarded
      *            during the extraction
      * @param minSize
      *            the minimum size of the objects to extract
      * @param maxSize
      *            the maximum size of the objects to extract
      * @param labeledStack
      *            a volumetric image where labeled information will be stored during the extraction.
      *            Note that this volumetric image should be non-null and filled with zero-values (no
      *            cleaning is performed to optimize recursive processes)
      * @return a collection containing all components extracted.
      * @throws NullPointerException
      *             is labeledStack is null
      * @see ExtractionType
      * @see ConnectedComponent
      */
     public static List<ConnectedComponent> extractConnectedComponents(VolumetricImage stack, double value, ExtractionType type, boolean noEdgeX, boolean noEdgeY, boolean noEdgeZ, int minSize,
             int maxSize, final VolumetricImage labeledStack) throws NullPointerException
     {
         int width = stack.getFirstImage().getSizeX();
         int height = stack.getFirstImage().getSizeY();
         int depth = stack.getSize();
         
         int[] neighborLabelValues = new int[13];
         int neighborhoodSize = 0;
         
         boolean extractUserValue = (type == ExtractionType.VALUE);
         
         Label[] labels = new Label[width * height * depth];
         
         // first image pass: naive labeling with simple backward neighborhood
         
         int highestKnownLabel = 0;
         
         boolean onEdgeX = false;
         boolean onEdgeY = false;
         boolean onEdgeZ = false;
         
         for (int z = 0; z < depth; z++)
         {
             onEdgeZ = (z == 0 || z == depth - 1);
             
             // retrieve the direct pointer to the current slice
             int[] _labelsInCurrentSlice = labeledStack.getImage(z).getDataXYAsInt(0);
             // retrieve a direct pointer to the previous slice
             int[] _labelsInUpperSlice = (z == 0) ? null : labeledStack.getImage(z - 1).getDataXYAsInt(0);
             
             int voxelOffset = 0;
             
             Object inputData = stack.getImage(z).getDataXY(0);
             DataType dataType = stack.getImage(z).getDataType_();
             
             for (int y = 0; y < height; y++)
             {
                 onEdgeY = (y == 0 || y == height - 1);
                 
                 for (int x = 0; x < width; x++, voxelOffset++)
                 {
                     onEdgeX = (x == 0 || x == width - 1);
                     
                     double pixelValue = Array1DUtil.getValue(inputData, voxelOffset, dataType);
                     
                     boolean pixelEqualsUserValue = (pixelValue == value);
                     
                     // do not process the current pixel if:
                     // - extractUserValue is true and pixelEqualsUserValue is false
                     // - extractUserValue is false and pixelEqualsUserValue is true
                     
                     if (extractUserValue != pixelEqualsUserValue) continue;
                     
                     // the current pixel should be labeled
                     
                     // -> look for existing labels in its neighborhood
                     
                     // 1) define the neighborhood of interest here
                     // NB: this is a single pass method, so backward neighborhood is sufficient
                     
                     // legend:
                     // e = edge
                     // x = current pixel
                     // n = valid neighbor
                     // . = other neighbor
                     
                     if (z == 0)
                     {
                         if (y == 0)
                         {
                             if (x == 0)
                             {
                                 // e e e
                                 // e x .
                                 // e . .
                                 
                                 // do nothing
                             }
                             else
                             {
                                 // e e e
                                 // n x .
                                 // . . .
                                 
                                 neighborLabelValues[0] = _labelsInCurrentSlice[voxelOffset - 1];
                                 neighborhoodSize = 1;
                             }
                         }
                         else
                         {
                             int north = voxelOffset - width;
                             
                             if (x == 0)
                             {
                                 // e n n
                                 // e x .
                                 // e . .
                                 
                                 neighborLabelValues[0] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[1] = _labelsInCurrentSlice[north + 1];
                                 neighborhoodSize = 2;
                             }
                             else if (x == width - 1)
                             {
                                 // n n e
                                 // n x e
                                 // . . e
                                 
                                 neighborLabelValues[0] = _labelsInCurrentSlice[north - 1];
                                 neighborLabelValues[1] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[2] = _labelsInCurrentSlice[voxelOffset - 1];
                                 neighborhoodSize = 3;
                             }
                             else
                             {
                                 // n n n
                                 // n x .
                                 // . . .
                                 
                                 neighborLabelValues[0] = _labelsInCurrentSlice[north - 1];
                                 neighborLabelValues[1] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[2] = _labelsInCurrentSlice[north + 1];
                                 neighborLabelValues[3] = _labelsInCurrentSlice[voxelOffset - 1];
                                 neighborhoodSize = 4;
                             }
                         }
                     }
                     else
                     {
                         if (y == 0)
                         {
                             int south = voxelOffset + width;
                             
                             if (x == 0)
                             {
                                 // e e e | e e e
                                 // e n n | e x .
                                 // e n n | e . .
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[1] = _labelsInUpperSlice[voxelOffset + 1];
                                 neighborLabelValues[2] = _labelsInUpperSlice[south];
                                 neighborLabelValues[3] = _labelsInUpperSlice[south + 1];
                                 neighborhoodSize = 4;
                             }
                             else if (x == width - 1)
                             {
                                 // e e e | e e e
                                 // n n e | n x e
                                 // n n e | . . e
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[voxelOffset - 1];
                                 neighborLabelValues[1] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[2] = _labelsInUpperSlice[south - 1];
                                 neighborLabelValues[3] = _labelsInUpperSlice[south];
                                 neighborLabelValues[4] = _labelsInCurrentSlice[voxelOffset - 1];
                                 neighborhoodSize = 5;
                             }
                             else
                             {
                                 // e e e | e e e
                                 // n n n | n x .
                                 // n n n | . . .
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[voxelOffset - 1];
                                 neighborLabelValues[1] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[2] = _labelsInUpperSlice[voxelOffset + 1];
                                 neighborLabelValues[3] = _labelsInUpperSlice[south - 1];
                                 neighborLabelValues[4] = _labelsInUpperSlice[south];
                                 neighborLabelValues[5] = _labelsInUpperSlice[south + 1];
                                 neighborLabelValues[6] = _labelsInCurrentSlice[voxelOffset - 1];
                                 neighborhoodSize = 7;
                             }
                         }
                         else if (y == height - 1)
                         {
                             int north = voxelOffset - width;
                             
                             if (x == 0)
                             {
                                 // e n n | e n n
                                 // e n n | e x .
                                 // e e e | e e e
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[north];
                                 neighborLabelValues[1] = _labelsInUpperSlice[north + 1];
                                 neighborLabelValues[2] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[3] = _labelsInUpperSlice[voxelOffset + 1];
                                 neighborLabelValues[4] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[5] = _labelsInCurrentSlice[north + 1];
                                 neighborhoodSize = 6;
                             }
                             else if (x == width - 1)
                             {
                                 // n n e | n n e
                                 // n n e | n x e
                                 // e e e | e e e
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[north - 1];
                                 neighborLabelValues[1] = _labelsInUpperSlice[north];
                                 neighborLabelValues[2] = _labelsInUpperSlice[voxelOffset - 1];
                                 neighborLabelValues[3] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[4] = _labelsInCurrentSlice[north - 1];
                                 neighborLabelValues[5] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[6] = _labelsInCurrentSlice[voxelOffset - 1];
                                 neighborhoodSize = 7;
                             }
                             else
                             {
                                 // n n n | n n n
                                 // n n n | n x .
                                 // e e e | e e e
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[north - 1];
                                 neighborLabelValues[1] = _labelsInUpperSlice[north];
                                 neighborLabelValues[2] = _labelsInUpperSlice[north + 1];
                                 neighborLabelValues[3] = _labelsInUpperSlice[voxelOffset - 1];
                                 neighborLabelValues[4] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[5] = _labelsInUpperSlice[voxelOffset + 1];
                                 neighborLabelValues[6] = _labelsInCurrentSlice[north - 1];
                                 neighborLabelValues[7] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[8] = _labelsInCurrentSlice[north + 1];
                                 neighborLabelValues[9] = _labelsInCurrentSlice[voxelOffset - 1];
                                 neighborhoodSize = 10;
                             }
                         }
                         else
                         {
                             int north = voxelOffset - width;
                             int south = voxelOffset + width;
                             
                             if (x == 0)
                             {
                                 // e n n | e n n
                                 // e n n | e x .
                                 // e n n | e . .
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[north];
                                 neighborLabelValues[1] = _labelsInUpperSlice[north + 1];
                                 neighborLabelValues[2] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[3] = _labelsInUpperSlice[voxelOffset + 1];
                                 neighborLabelValues[4] = _labelsInUpperSlice[south];
                                 neighborLabelValues[5] = _labelsInUpperSlice[south + 1];
                                 neighborLabelValues[6] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[7] = _labelsInCurrentSlice[north + 1];
                                 neighborhoodSize = 8;
                             }
                             else if (x == width - 1)
                             {
                                 int northwest = north - 1;
                                 int west = voxelOffset - 1;
                                 
                                 // n n e | n n e
                                 // n n e | n x e
                                 // n n e | . . e
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[northwest];
                                 neighborLabelValues[1] = _labelsInUpperSlice[north];
                                 neighborLabelValues[2] = _labelsInUpperSlice[west];
                                 neighborLabelValues[3] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[4] = _labelsInUpperSlice[south - 1];
                                 neighborLabelValues[5] = _labelsInUpperSlice[south];
                                 neighborLabelValues[6] = _labelsInCurrentSlice[northwest];
                                 neighborLabelValues[7] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[8] = _labelsInCurrentSlice[west];
                                 neighborhoodSize = 9;
                             }
                             else
                             {
                                 int northwest = north - 1;
                                 int west = voxelOffset - 1;
                                 int northeast = north + 1;
                                 int southwest = south - 1;
                                 int southeast = south + 1;
                                 
                                 // n n n | n n n
                                 // n n n | n x .
                                 // n n n | . . .
                                 
                                 neighborLabelValues[0] = _labelsInUpperSlice[northwest];
                                 neighborLabelValues[1] = _labelsInUpperSlice[north];
                                 neighborLabelValues[2] = _labelsInUpperSlice[northeast];
                                 neighborLabelValues[3] = _labelsInUpperSlice[west];
                                 neighborLabelValues[4] = _labelsInUpperSlice[voxelOffset];
                                 neighborLabelValues[5] = _labelsInUpperSlice[voxelOffset + 1];
                                 neighborLabelValues[6] = _labelsInUpperSlice[southwest];
                                 neighborLabelValues[7] = _labelsInUpperSlice[south];
                                 neighborLabelValues[8] = _labelsInUpperSlice[southeast];
                                 neighborLabelValues[9] = _labelsInCurrentSlice[northwest];
                                 neighborLabelValues[10] = _labelsInCurrentSlice[north];
                                 neighborLabelValues[11] = _labelsInCurrentSlice[northeast];
                                 neighborLabelValues[12] = _labelsInCurrentSlice[west];
                                 neighborhoodSize = 13;
                             }
                         }
                     }
                     
                     // 2) the neighborhood is ready, move to the labeling step
                     
                     int currentVoxelLabelValue = Integer.MAX_VALUE;
                     
                     // to avoid creating too many labels and fuse them later on,
                     // find the minimum non-zero label in the neighborhood
                     // and assign that minimum label right now
                     
                     for (int i = 0; i < neighborhoodSize; i++)
                     {
                         int neighborLabelValue = neighborLabelValues[i];
                         
                         // zero labels are not interesting...
                         if (neighborLabelValue == 0) continue;
                         
                         // neighbor labels should have the same/different ? image value
                         if (type == ExtractionType.BACKGROUND_LABELED && labels[neighborLabelValue].imageValue != pixelValue) continue;
                         
                         // here, the neighbor label is valid
                         // => check if it is lower
                         if (neighborLabelValue < currentVoxelLabelValue)
                         {
                             currentVoxelLabelValue = neighborLabelValue;
                         }
                     }
                     
                     if (currentVoxelLabelValue == Integer.MAX_VALUE)
                     {
                         // currentVoxelLabel didn't change
                         // => no lower neighbor value found
                         // => new label
                         highestKnownLabel++;
                         currentVoxelLabelValue = highestKnownLabel;
                         labels[currentVoxelLabelValue] = new Label(pixelValue, currentVoxelLabelValue);
                     }
                     else
                     {
                         // currentVoxelLabelValue has been modified
                         // -> browse its neighborhood again
                         // -> find all neighbors with a higher label value
                         // -> change their value to currentVoxelLabelValue
                         // -> change their target to currentVoxelLabel
                         
                         Label currentVoxelLabel = labels[currentVoxelLabelValue];
                         
                         for (int i = 0; i < neighborhoodSize; i++)
                         {
                             int neighborLabelValue = neighborLabelValues[i];
                             
                             if (neighborLabelValue > currentVoxelLabelValue)
                             {
                                 Label label = labels[neighborLabelValue];
                                 
                                 if (type == ExtractionType.BACKGROUND_LABELED && label.imageValue != pixelValue) continue;
                                 
                                 int finalLabelValue = label.getFinalLabelValue();
                                 Label finalLabel = labels[finalLabelValue];
                                 
                                 if (currentVoxelLabel.targetLabelValue == finalLabelValue) continue;
                                 
                                 if (currentVoxelLabelValue < finalLabelValue)
                                 {
                                     finalLabel.targetLabel = currentVoxelLabel;
                                     finalLabel.targetLabelValue = currentVoxelLabelValue;
                                 }
                                 else if (currentVoxelLabelValue > finalLabelValue)
                                 {
                                     currentVoxelLabel.targetLabel = finalLabel;
                                     currentVoxelLabel.targetLabelValue = finalLabelValue;
                                 }
                             }
                         }
                     }
                     
                     // -> store this label in the labeled image
                     _labelsInCurrentSlice[voxelOffset] = currentVoxelLabelValue;
                     labels[currentVoxelLabelValue].size++;
                     labels[currentVoxelLabelValue].onEdgeX |= onEdgeX;
                     labels[currentVoxelLabelValue].onEdgeY |= onEdgeY;
                     labels[currentVoxelLabelValue].onEdgeZ |= onEdgeZ;
                 }
             }
         }
         
         // end of the first pass, all pixels have a label
         // (though might not be unique within a given component)
         
         HashMap<Integer, ConnectedComponent> componentsMap = new HashMap<Integer, ConnectedComponent>();
         
         // fusion strategy: fuse higher labels with lower ones
         // "highestKnownLabel" holds the highest known label
         // -> loop backward from there to accumulate object size recursively
         
         int finalLabel = 0;
         
         for (int labelValue = highestKnownLabel; labelValue > 0; labelValue--)
         {
             Label label = labels[labelValue];
             
             int targetLabelValue = label.targetLabelValue;
             
             if (targetLabelValue < labelValue)
             {
                 // label should be fused to targetLabel
                 
                 Label targetLabel = labels[targetLabelValue];
                 
                 // -> add label's size to targetLabel
                 targetLabel.size += label.size;
                 
                 // -> mark targetLabel as onEdge if label is
                 targetLabel.onEdgeX |= label.onEdgeX;
                 targetLabel.onEdgeY |= label.onEdgeY;
                 targetLabel.onEdgeZ |= label.onEdgeZ;
                 
                 // -> mark label to fuse with targetLabel
                 label.targetLabel = labels[targetLabelValue];
             }
             else
             {
                 // label has same labelValue and targetLabelValue
                 // -> it cannot be fused to anything
                 // -> this is a terminal label
                 
                 // -> check if it obeys to user constraints
                 
                 if (label.size < minSize || label.size > maxSize)
                 {
                     // the component size is out of the given range
                     // -> mark the object for deletion
                     label.targetLabelValue = 0;
                 }
                 else if ((noEdgeX && label.onEdgeX) || (noEdgeY && label.onEdgeY) || (noEdgeZ && label.onEdgeZ))
                 {
                     // the component size is on an edge to discard
                     // -> mark the object for deletion
                     label.targetLabelValue = 0;
                 }
                 else
                 {
                     // the label is clean and user-valid
                     // -> assign its final labelValue (for the final image labeling pass)
                     finalLabel++;
                     label.targetLabelValue = finalLabel;
                     
                     // -> add this label to the list of valid labels
                     
                     ConnectedComponent component = new ConnectedComponent(label.size);
                     component.onEdgeX = label.onEdgeX;
                     component.onEdgeY = label.onEdgeY;
                     component.onEdgeZ = label.onEdgeZ;
                     componentsMap.put(finalLabel, component);
                 }
             }
         }
         
         // 3) second image pass: replace all labels by their final values
         
         for (int z = 0; z < depth; z++)
         {
             int[] _outputSlice = labeledStack.getImage(z).getDataXYAsInt(0);
             
             int pixelOffset = 0;
             
             for (int j = 0; j < height; j++)
             {
                 for (int i = 0; i < width; i++, pixelOffset++)
                 {
                     int targetLabelValue = _outputSlice[pixelOffset];
                     
                     if (targetLabelValue == 0) continue;
                     
                     // if a fusion was indicated, retrieve the final label value
                     targetLabelValue = labels[targetLabelValue].getFinalLabelValue();
                     
                     // assign the final label in the output image
                     _outputSlice[pixelOffset] = targetLabelValue;
                     
                     if (targetLabelValue == 0) continue;
                     
                     // store the current pixel in the component
                     componentsMap.get(targetLabelValue).addPointInternal(new Point3i(i, j, z));
                 }
             }
         }
         
         return new ArrayList<ConnectedComponent>(componentsMap.values());
     }
     
     public static DetectionResult convertToDetectionResult(Map<Integer, List<ConnectedComponent>> detections, Sequence sequence)
     {
         
         DetectionResult detectionResult = new DetectionResult();
         for (Integer t : detections.keySet())
             for (ConnectedComponent cc : detections.get(t))
             {
                 // TODO: add points information
                 Spot spot = new Spot(cc.getMassCenter().x, cc.getMassCenter().y, cc.getMassCenter().z);
                 detectionResult.addDetection(t, spot);
             }
         
         detectionResult.setSequence(sequence);
         
         return detectionResult;
     }
     
     /**
      * Fill the channel 0 of the given sequence with the list of components sorted using the given
      * comparator. The method does nothing if the given comparator is null
      * 
      * @param output
      *            a sequence of type INT
      * @param components
      * @param comparator
      */
     public static void createLabeledSequence(Sequence output, Map<Integer, List<ConnectedComponent>> components, Comparator<ConnectedComponent> comparator)
     {
         if (comparator == null) return;
         
         int width = output.getSizeX();
         
         for (Integer t : components.keySet())
         {
             int id = 1;
             
             ConnectedComponent[] ccs = components.get(t).toArray(new ConnectedComponent[] {});
             Arrays.sort(ccs, comparator);
             
             int[][] data = output.getDataXYZAsInt(t, 0);
             
             for (ConnectedComponent cc : ccs)
             {
                 for (Point3i pt : cc)
                 {
                     data[pt.z][pt.y * width + pt.x] = id;
                 }
                 id++;
             }
         }
     }
 }
