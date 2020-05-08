 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 /**
  * The <code>TextFieldBean</code> represents a text field element
  */
 public class TextFieldBean extends InputBean implements TagBean {
 
     public static final String NAME = "tf";
 
     /**
      * Constructs a default text field bean
      */
     public TextFieldBean() {
         super(NAME);
         this.inputtype = "text";
     }
 
     /**
      * Constructs a text field bean using the supplied bean name
      *
     * @param name the bean name
      */
     public TextFieldBean(String beanId) {
         super(NAME);
         this.beanId = beanId;
         this.inputtype = "text";
     }
 
     /**
      * Constructs a text field bean using the supplied bean name and identifier
      *
     * @param name   the bean name
      * @param beanId the bean identifier
      */
     public TextFieldBean(String vbName, String beanId) {
         super(vbName);
         this.inputtype = "text";
         this.beanId = beanId;
     }
 
     public String toStartString() {
         return super.toStartString();
     }
 
 }
