 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.ui;
 
 import java.awt.geom.AffineTransform;
 
 /**
  *
  * @author epsilon
  */
 public class ModelTransform extends AffineTransform {
 
     public static final double SCALE_LOWER_LIMITE = 1e-15;
    /**
     *
     * @author epsilon
     */
    public static int DEFAULT_ZOOM_ALL_MARGIN = 4;
    private double defaultOriginX;
    private double defaultOriginY;
     private double defaultScale;
    int zoomAllMargin = MouseDrivenModelTransform.DEFAULT_ZOOM_ALL_MARGIN;
 
    public void unitScaleAndSetOrigin(double originX, double originY) {
         setToIdentity();
         translate(originX, originY);
         scale(1, -1);
     }
 
    void translateOrigin(double dx, double dy) {
         preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
     }
 
    public void scaleByCenter(double centerX, double centerY, double scale) {
         AffineTransform tranformBack = new AffineTransform(this);
         setToTranslation(centerX, centerY);
         scale(scale, scale);
         translate(-centerX, -centerY);
         if (Math.abs(getScaleX()) < SCALE_LOWER_LIMITE || Math.abs(getScaleY()) < SCALE_LOWER_LIMITE) {
             setTransform(tranformBack);
         } else {
             concatenate(tranformBack);
         }
     }
 
    public void setDefaultOriginAndScale(double originX, double originY, double scale) {
        defaultOriginX = originX;
        defaultOriginY = originY;
         defaultScale = scale;
     }
 
     public void resetToDefault() {
        unitScaleAndSetOrigin(defaultOriginX, defaultOriginY);
        scale(defaultScale, defaultScale);
     }
 }
