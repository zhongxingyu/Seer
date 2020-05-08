 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.portletui.beans;
 
 /**
  * The abstract <code>BaseComponentBean</code> defines the visual bean properties of all ui tag beans
  */
 public abstract class BaseComponentBean extends BaseBean {
 
     protected String name = null;
     protected String value = null;
     protected boolean readonly = false;
     protected boolean disabled = false;
     protected String cssStyle = "";
     protected String key = null;
     protected boolean visible = true;
     protected boolean supportsJS = false;
 
     /**
      * Constructs a default base component bean
      */
     public BaseComponentBean() {
         super();
     }
 
     /**
      * Constructs a base component bean using the supplied visual bean type identifier
      *
      * @param vbName the supplied visual bean type identifier
      */
     public BaseComponentBean(String vbName) {
         super(vbName);
     }
 
     /**
      * Sets the name of the bean
      *
      * @param name the name of the bean
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Returns the name of the bean
      *
      * @return name of the bean
      */
     public String getName() {
         return this.name;
     }
 
     /**
      * Sets the bean value
      *
      * @param value the bean value
      */
     public void setValue(String value) {
         this.value = value;
     }
 
     /**
      * Returns the bean value
      *
      * @return the bean value
      */
     public String getValue() {
         return value;
     }
 
     /**
      * Returns the key used to identify localized text
      *
      * @return the key used to identify localized text
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Sets the key used to identify localized text
      * @param key the key used to identify localized text
      */
     public void setKey(String key) {
         this.key = key;
     }
 
     /**
      * Returns true if bean is in disabled state.
      * @return state
      */
     public boolean isDisabled() {
         return disabled;
     }
 
     /**
      * Sets the disabled attribute of the bean to be 'flag' state
      *
      * @param flag is true if the bean is to be disabled, false otherwise
      */
     public void setDisabled(boolean flag) {
         this.disabled = flag;
     }
 
     /**
      * Returns disabled String if bean is disabled
      *
      * @return disabled String if bean is disabled or blank otherwise
      */
     protected String checkDisabled() {
         if (disabled) {
             return " disabled='disabled' ";
         } else {
             return "";
         }
     }
 
     /**
      * Sets the bean to readonly
      *
      * @param flag is true if the bean is read-only, false otherwise
      */
     public void setReadOnly(boolean flag) {
         this.readonly = flag;
     }
 
     /**
      * Returns the read-only status of the bean
      *
      * @return true if bean is read-only, false otherwise
      */
     public boolean isReadOnly() {
         return readonly;
     }
 
     /**
      * Returns 'disabled' string if bean is read-only, blank string otherwise
      *
      * @return 'disabled' string if bean is read-only, blank string otherwise
      */
     protected String checkReadOnly() {
         if (readonly) {
            return " disabled='disabled' ";
         } else {
             return "";
         }
     }
 
     /**
      * Returns the CSS style name of the bean
      *
      * @return the name of the css style
      */
     public String getCssStyle() {
         return cssStyle;
     }
 
     /**
      * Sets the CSS style of the bean
      *
      * @param style css style name to set for the bean
      */
     public void setCssStyle(String style) {
         this.cssStyle = style;
     }
 
     /**
      * Sets the bean visibility
      *
      * @param visible is true if the bean shoudl be visible, false otherwise
      */
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
     /**
      * Returns the bean visibility
      *
      * @return the bean visibility
      */
     public boolean getVisible() {
         return visible;
     }
 
     /**
      * Indicates if JavaScript is supported in the client's browser
      *
      * @return true if JavaScript is supported in the client's browser, false otherwise
      */
     public boolean supportsJS() {
         return supportsJS;
     }
 
     /**
      * Indicates if JavaScript is supported in the client's browser
      *
      * @param supportsJS is true if JavaScript is supported in the client's browser, false otherwise
      */
     public void setSupportsJS(boolean supportsJS) {
         this.supportsJS = supportsJS;
     }
 
 }
 
