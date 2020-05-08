 package com.eagerlogic.cubee.client.style.styles;
 
 import com.google.gwt.dom.client.Element;
 
 /**
  *
  * @author dipacs
  */
 public final class ImageBackground extends ABackground {
 
     private final Image image;
     private final Double width;
     private final Double height;
     private final EHAlign hAlign;
     private final EVAlign vAlign;
     private final boolean hRepeat;
     private final boolean vRepeat;
 
     public ImageBackground(Image image) {
         this(image, null, null, null, null, false, false);
     }
 
     public ImageBackground(Image image, Double width, Double height) {
         this(image, width, height, null, null, false, false);
     }
 
     public ImageBackground(Image image, EHAlign hAlign, EVAlign vAlign) {
         this(image, null, null, hAlign, vAlign, false, false);
     }
 
     public ImageBackground(Image image, boolean hRepeat, boolean vRepeat) {
         this(image, null, null, null, null, hRepeat, vRepeat);
     }
 
     public ImageBackground(Image image, Double width, Double height, EHAlign hAlign, EVAlign vAlign) {
         this(image, width, height, hAlign, vAlign, false, false);
     }
 
     public ImageBackground(Image image, Double width, Double height, boolean hRepeat, boolean vRepeat) {
         this(image, width, height, null, null, hRepeat, vRepeat);
     }
 
     public ImageBackground(Image image, EHAlign hAlign, EVAlign vAlign, boolean hRepeat, boolean vRepeat) {
         this(image, null, null, hAlign, vAlign, hRepeat, vRepeat);
     }
 
     public ImageBackground(Image image, Double width, Double height, EHAlign hAlign, EVAlign vAlign, boolean hRepeat, boolean vRepeat) {
         this.image = image;
         this.width = width;
         this.height = height;
         this.hAlign = hAlign;
         this.vAlign = vAlign;
         this.hRepeat = hRepeat;
         this.vRepeat = vRepeat;
     }
 
	public Image getImage() {
         return image;
     }
 
     @Override
     public void apply(Element element) {
         applyImage(element);
         applySize(element);
         applyAlign(element);
         applyRepeat(element);
     }
 
     private void applyImage(Element element) {
         if (image == null) {
             element.getStyle().clearBackgroundImage();
         } else {
        	if (image.isFromResource()) {
        		element.getStyle().setBackgroundImage("url('" + image.getImageResource().getSafeUri().asString() + "')");
            } else {
            	element.getStyle().setBackgroundImage("url('" + image.getUrl() + "')");
            }
         }
     }
 
     private void applySize(Element element) {
         String size = "auto";
         if (width != null && height != null) {
             size = sizeToStr(width) + " " + sizeToStr(height);
         } else if (width != null) {
             size = sizeToStr(width) + " auto";
         } else if (height != null) {
             size = "auto " + sizeToStr(height);
         }
         element.getStyle().setProperty("backgroundSize", size);
     }
 
     private String sizeToStr(double value) {
         if (value < 1.0 && value > -1.0) {
             return "" + value + "%";
         } else {
             return ((int) value) + "px";
         }
     }
 
     private void applyAlign(Element element) {
         String value = "left top";
         if (hAlign != null && vAlign != null) {
             value = hAlign.getCssValue() + " " + vAlign.getCssValue();
         } else if (hAlign != null) {
             value = hAlign.getCssValue() + " top";
         } else if (vAlign != null) {
             value = "left " + vAlign.getCssValue();
         }
         element.getStyle().setProperty("backgroundPosition", value);
     }
 
     private void applyRepeat(Element element) {
         String value = "no-repeat";
         if (hRepeat && vRepeat) {
             value = "repeat";
         } else if (hRepeat) {
             value = "repeat-x";
         } else if (vRepeat) {
             value = "repeat-y";
         }
         element.getStyle().setProperty("backgroundRepeat", value);
     }
 
 }
