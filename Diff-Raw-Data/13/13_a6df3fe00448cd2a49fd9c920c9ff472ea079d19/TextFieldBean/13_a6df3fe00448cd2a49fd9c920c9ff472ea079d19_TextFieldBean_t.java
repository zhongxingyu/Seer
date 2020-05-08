 /**
  * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.ui.beans;
 
 public class TextFieldBean extends ReadOnlyBaseBean implements Input {
 
     protected int size;
     protected String inputtype = "text";
     protected int maxlength;
 
     public TextFieldBean() {
         super();
     }
 
     public TextFieldBean(String name, String value) {
         super();
         this.name = name;
         this.value = value;
     }
 
     public TextFieldBean(String name, String value, boolean disabled, boolean readonly, int size, int maxlength) {
         //  super(name, value, disabled, readonly);
         super();
         this.name = name;
         this.value = value;
         this.disabled = disabled;
         this.readonly = readonly;
         this.size = size;
         this.maxlength = maxlength;
     }
 
     /**
      * Returns the (html) size of the field.
      * @return size of the field
      */
     public int getSize() {
         return size;
     }
 
     /**
      * Sets the (html) size of the field
      * @param size size of the field
      */
     public void setSize(int size) {
         this.size = size;
     }
 
     /**
      * Returns the (html) inputtype of the field.
      * @return inputtype of the field
      */
     public String getInputtype() {
         return inputtype;
     }
 
     /**
      * Sets the (html) inputtype of the field
      * @param ioputtype (html) inputtype of the field
      *
      */
     public void setInputtype(String inputtype) {
         this.inputtype = inputtype;
     }
 
     /**
      * Returns the (html) maxlength of the field
      * @return maxlength of the field
      */
     public int getMaxlength() {
         return maxlength;
     }
 
     /**
      * Sets the (html) maxlnegth of the field
      * @param maxlength maxlength of the field
      */
     public void setMaxlength(int maxlength) {
         this.maxlength = maxlength;
     }
 
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("<input type='");
         buffer.append(inputtype);
         buffer.append("' name='");
         buffer.append(getTagName());
         buffer.append(name);
         buffer.append("' value='");
         buffer.append(value);
         buffer.append("' ");
         if (maxlength > 0) {
             buffer.append("maxlength='");
             buffer.append(maxlength);
             buffer.append("' ");
         }
         if (size > 0) {
             buffer.append("size='");
             buffer.append(size);
             buffer.append("' ");
         }
         buffer.append(checkReadonly());
         buffer.append(checkDisabled());
         buffer.append("/>");
         return getCSS(buffer.toString());
/**
         return getCSS("<input type='" + inputtype + "' name='" + getTagName() + name + "' value='" + value + "' size='"
                 + size + "' maxlength='" + maxlength +"'"+checkReadonly()+checkDisabled()+"/>");
 **/
     }
 
 }
