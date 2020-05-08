 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 /**
  * A <code>ImageBean</code> represents an image element
  */
 public class ImageBean extends BaseComponentBean implements TagBean {
 
     public static final String NAME = "im";
     public String src = "";
     public String alt = null;
     public String title = null;
     public String border = "0";
     protected String width = null;
     protected String height = null;
     protected String align = null;
 
     /**
      * Constructs a default image bean
      */
     public ImageBean() {
         super(NAME);
     }
 
     /**
      * Constructs an image bean using a supplied  bean identifier
      *
      * @param beanId the bean identifier
      */
     public ImageBean(String beanId) {
        super(NAME);
        this.beanId = beanId;
     }
 
     /**
      * Returns the image source
      *
      * @return the image source
      */
     public String getSrc() {
         return src;
     }
 
     /**
      * Sets the image source
      *
      * @param src the image source
      */
     public void setSrc(String src) {
         this.src = src;
     }
 
     /**
      * Returns the image alt tag
      *
      * @return the image alt tag
      */
     public String getAlt() {
         return alt;
     }
 
     /**
      * Sets the image alt tag
      *
      * @param alt the image alt tag
      */
     public void setAlt(String alt) {
         this.alt = alt;
     }
 
     /**
      * Returns the image title
      *
      * @return the image title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Sets the image title
      *
      * @param title the image title
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * Sets the table alignment e.g. "left", "top", "bottom" or "right"
      *
      * @param align the table alignment
      */
     public void setAlign(String align) {
         this.align = align;
     }
 
     /**
      * Returns the table alignment e.g. "left", "top", "bottom" or "right"
      *
      * @return the table alignment
      */
     public String getAlign() {
         return align;
     }
 
     /**
      * Sets the table cell width
      *
      * @param width the table cell width
      */
     public void setWidth(String width) {
         this.width = width;
     }
 
     /**
      * Returns the table cell width
      *
      * @return the table cell width
      */
     public String getWidth() {
         return width;
     }
 
     /**
      * Sets the table cell height
      *
      * @param height the table cell height
      */
     public void setHeight(String height) {
         this.height = height;
     }
 
     /**
      * Returns the table cell height
      *
      * @return the table cell height
      */
     public String getHeight() {
         return height;
     }
 
     /**
      * Return the image title border
      *
      * @return the image title border
      */
     public String getBorder() {
         return border;
     }
 
     /**
      * Sets the image title border
      *
      * @param border the image title border
      */
     public void setBorder(String border) {
         this.border = border;
     }
 
     public String toStartString() {
         if (src.equals("")) return "";
         StringBuffer sb = new StringBuffer();
         sb.append("<img src=\"" + this.src + "\" border=\"" + border + "\"");
         if (width != null) sb.append(" width=\"" + width + "\"");
         if (height != null) sb.append(" height=\"" + height + "\"");
         if (align != null) sb.append(" align=\"" + align + "\"");
         if (alt != null) sb.append(" alt=\"" + alt + "\"");
         if (title != null) sb.append(" title=\"" + title + "\"");
         sb.append("/>");
         return sb.toString();
     }
 
     public String toEndString() {
         return "";
     }
 }
 
 
 
