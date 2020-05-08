 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nava.structurevis.data;
 
 import java.awt.Color;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import nava.structurevis.StructureVisController;
 import nava.utils.Mapping;
 import org.biojava.bio.BioException;
 import org.biojava.bio.symbol.Location;
 import org.biojavax.Namespace;
 import org.biojavax.RichObjectFactory;
 import org.biojavax.bio.seq.RichSequence;
 import org.biojavax.bio.seq.RichSequenceIterator;
 
 /**
  *
  * @author Michael
  */
 public class AnnotationSource implements Serializable {
 
     public static Color[] featureColours = {
         new Color(255, 190, 190),
         new Color(190, 255, 255),
         new Color(190, 190, 255),
         new Color(255, 190, 255),
         new Color(200, 255, 190),
         new Color(255, 255, 190),
         new Color(255, 200, 100),
         new Color(220, 180, 210)
     };
     public int sequenceLength;
     public ArrayList<Feature> features = new ArrayList<>();
     public int mappedSequenceLength;
     public ArrayList<Feature> mappedFeatures = new ArrayList<>();
 
     @Override
     public String toString() {
         return features.toString();
     }
 
     /**
      * Automatically assign a different colour to each feature.
      */
     public void assignColors() {
         for (int i = 0; i < features.size(); i++) {
             Feature f = features.get(i);
             for (int j = 0; j < f.blocks.size(); j++) {
                 f.blocks.get(j).color = featureColours[i % featureColours.length];
             }
         }
     }
 
     /**
      * Returns an AnnotationData object, containing a natural stacking of
      * sequence features, i.e. places the features on different rows so that
      * they are non-overlapping.
      *
      * @param annotationData
      * @return
      */
     public static AnnotationSource stackFeatures(AnnotationSource annotationData) {
         AnnotationSource ret = new AnnotationSource();
         ret.sequenceLength = annotationData.sequenceLength;
         ret.features.addAll(annotationData.features);
         Collections.sort(ret.features);
         Collections.reverse(ret.features);
 
         ArrayList<Feature> addedFeatures = new ArrayList<>();
         int maxRow = 0;
         for (int row = 0; row < ret.features.size(); row++) {
             Feature currentFeature = ret.features.get(row);
             int currentRow = 0;
             for (currentRow = 0; currentRow <= maxRow + 1; currentRow++) {
                 currentFeature.row = currentRow;
                 if (isOverlap(currentFeature, addedFeatures)) {
                 } else {
                     break;
                 }
             }
             maxRow = Math.max(maxRow, currentRow);
             currentFeature.row = currentRow;
             addedFeatures.add(currentFeature);
         }
         ret.features = addedFeatures;
 
         ret.mappedFeatures.addAll(annotationData.mappedFeatures);
         Collections.sort(ret.mappedFeatures);
         Collections.reverse(ret.mappedFeatures);
         ArrayList<Feature> addedMappedFeatures = new ArrayList<>();
         maxRow = 0;
         for (int row = 0; row < ret.mappedFeatures.size(); row++) {
             Feature currentFeature = ret.mappedFeatures.get(row);
             int currentRow = 0;
             for (currentRow = 0; currentRow <= maxRow + 1; currentRow++) {
                 currentFeature.row = currentRow;
                 if (isOverlap(currentFeature, addedMappedFeatures)) {
                 } else {
                     break;
                 }
             }
             maxRow = Math.max(maxRow, currentRow);
             currentFeature.row = currentRow;
             addedMappedFeatures.add(currentFeature);
         }
         ret.mappedFeatures = addedMappedFeatures;
 
 
         return ret;
     }
 
     /**
      * Tests whether a given feature overlaps any features in a provided list of
      * features.
      *
      * @param f
      * @param otherFeatures
      * @return
      */
     public static boolean isOverlap(Feature f, ArrayList<Feature> otherFeatures) {
         for (int i = 0; i < otherFeatures.size(); i++) {
             if (Feature.isOverlap(f, otherFeatures.get(i))) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Given a list of features, return a list of those features which are
      * visible.
      *
      * @param annotationData
      * @return
      */
     public static AnnotationSource getVisible(AnnotationSource annotationData) {
         AnnotationSource ret = new AnnotationSource();
         ret.sequenceLength = annotationData.sequenceLength;
         for (int i = 0; i < annotationData.features.size(); i++) {
             if (annotationData.features.get(i).visible) {
                 ret.features.add(annotationData.features.get(i));
             }
         }
         return ret;
     }
 
     /**
      * Given a GenBank file, return an AnnotationData object containing the
      * annotations.
      *
      * @param genBankFile
      * @return
      * @throws BioException
      */
     public static AnnotationSource readAnnotations(File genBankFile) throws BioException, IOException {
         AnnotationSource annotationData = new AnnotationSource();
         annotationData.sequenceLength = 0;
 
         BufferedReader br = new BufferedReader(new FileReader(genBankFile));
         Namespace ns = RichObjectFactory.getDefaultNamespace();
         RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(br, ns);
 
         while (seqs.hasNext()) {
             RichSequence rs = seqs.nextRichSequence();
             Iterator<org.biojava.bio.seq.Feature> it = rs.features();
             MappingSource mappingSequence = new MappingSource(rs.seqString());
             while (it.hasNext()) {
                 org.biojava.bio.seq.Feature ft = it.next();
 
                 Feature feature = new Feature();
                 feature.mappingSource = mappingSequence;
                 feature.min = ft.getLocation().getMin();
                 feature.max = ft.getLocation().getMax();
                 if (ft.getType().equalsIgnoreCase("source")) {
                     annotationData.sequenceLength = Math.max(annotationData.sequenceLength, ft.getLocation().getMax());
                 }
 
                 if (ft.getAnnotation().containsProperty("gene")) {
                     feature.name = ft.getAnnotation().getProperty("gene").toString();
                 } else if (ft.getAnnotation().containsProperty("product")) {
                     feature.name = ft.getAnnotation().getProperty("product").toString();
                 } else {
                     feature.name = ft.getType();
                     feature.visible = false;
                 }
 
                 Iterator<Location> blocks = ft.getLocation().blockIterator();
                 while (blocks.hasNext()) {
                     Location lt = blocks.next();
                     Block block = new Block(feature, lt.getMin(), lt.getMax());
                     feature.blocks.add(block);
                 }
 
                 annotationData.features.add(feature);
                 getMappedAnnotations(annotationData, null, null);
             }
         }
         br.close();
 
         annotationData.assignColors();
         return annotationData;
     }
 
     public static AnnotationSource getMappedAnnotations(AnnotationSource annotationSource, StructureOverlay structureSource, StructureVisController structureVisController) {
         if (annotationSource == null) {
             annotationSource = new AnnotationSource();
         }
         annotationSource.mappedFeatures = new ArrayList<>();
         ArrayList<Feature> features = annotationSource.features;
         annotationSource.mappedSequenceLength = 0;
         for (Feature feature : features) {
             Feature mappedFeature = feature.clone();
             if (structureSource != null && structureVisController != null) {
                 Mapping mapping = structureVisController.getMapping(structureSource.mappingSource, mappedFeature.mappingSource);
 
                 if (mapping != null) {
                    annotationSource.mappedSequenceLength = Math.max(annotationSource.mappedSequenceLength, mapping.getBLength());
                     mappedFeature.min = mapping.bToANearest(feature.min - 1);
                     mappedFeature.max = mapping.bToANearest(feature.max - 1);
                     if (mappedFeature.min == -1) {
                         mappedFeature.min = 0;
                     }
                     if (mappedFeature.max == -1) {
                         mappedFeature.max = mappedFeature.min + 1;
                     }
                     for (Block block : mappedFeature.blocks) {
                         block.min = mapping.bToANearest(block.min - 1);
                         block.max = mapping.bToANearest(block.max - 1);
                         if (block.min == -1) {
                             block.min = 0;
                         }
                         if (block.max == -1) {
                             block.max = block.min + 1;
                         }
                     }
                 }
             }
             if (annotationSource.mappedSequenceLength == 0) {
                 annotationSource.mappedSequenceLength = annotationSource.sequenceLength;
             }
 
             annotationSource.mappedFeatures.add(mappedFeature);
         }
 
         annotationSource.assignColors();
         return annotationSource;
     }
 
     /**
      * Add annotations from a2 to this, adjusting the first row of a2 to start
      * after the last row of this.
      *
      * @param a1
      * @param a2
      */
     public void addAnnotations(AnnotationSource a2) {
         if (a2 != null) {
             int maxRow = -1;
             for (Feature feature : this.features) {
                 maxRow = Math.max(feature.row, maxRow);
             }
             maxRow++;
             for (Feature feature : a2.features) {
                 Feature clone = feature.clone();
                 clone.row += maxRow;
                 this.features.add(clone);
             }
             for (Feature feature : a2.mappedFeatures) {
                 Feature clone = feature.clone();
                 clone.row += maxRow;
                 this.mappedFeatures.add(clone);
             }
         }
     }
 
     public static void main(String[] args) throws IOException {
         try {
             System.out.println(readAnnotations(new File("examples/annotations/refseq.gb")));
             System.out.println(stackFeatures(readAnnotations(new File("examples/annotations/refseq.gb"))));
         } catch (BioException ex) {
             Logger.getLogger(AnnotationSource.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
