 package edu.cmu.cs.diamond.rsna2007;
 
 public class Truth {
     public enum MassMargin {
         MASS_MARGIN_SPICULATED, MASS_MARGIN_ILLDEFINED, MASS_MARGIN_MICROLOBULATED, MASS_MARGIN_CIRCUMSCRIBED, MASS_MARGIN_OBSCURED
     }
 
     public enum MassShape {
         MASS_SHAPE_ROUND, MASS_SHAPE_OVAL, MASS_SHAPE_LOBULATED, MASS_SHAPE_IRREGULAR
     }
 
     public enum Biopsy {
         BIOPSY_MALIGNANT, BIOPSY_BENIGN
     }
 
     final private int birad;
 
     final private int subtlety;
 
     final private int density;
 
     final private int age;
 
     final private Biopsy biopsy;
 
     final private MassShape shape;
 
     final private MassMargin margin;
 
     final private ROI roi;

    public Truth(int birad, int subtlety, int density, int age,
            Biopsy biopsy, MassShape shape, MassMargin margin, ROI roi) {
         this.birad = birad;
         this.subtlety = subtlety;
         this.density = density;
         this.age = age;
         this.biopsy = biopsy;
         this.shape = shape;
         this.margin = margin;
         this.roi = roi;
     }
 
     public int getAge() {
         return age;
     }
 
     public Biopsy getBiopsy() {
         return biopsy;
     }
 
     public int getBirad() {
         return birad;
     }
 
     public int getDensity() {
         return density;
     }
 
     public MassMargin getMargin() {
         return margin;
     }
 
     public MassShape getShape() {
         return shape;
     }
 
     public int getSubtlety() {
         return subtlety;
     }

     public ROI getROI() {
         return roi;
     }
 }
