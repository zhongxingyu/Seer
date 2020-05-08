 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.provider.portletui.beans.BaseComponentBean;
 
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 /**
  * The abstract <code>BaseComponentTag</code> is used by all UI tags to provide CSS support and general
  * name, value attributes
  */
 public abstract class BaseComponentTag extends BaseBeanTag   {
 
     protected String name = null;
     protected String value = null;
     protected boolean readonly = false;
     protected boolean disabled = false;
     protected String cssStyle = null;
     protected boolean supportsJS = false;
 
     /**
      * Sets the name of the bean
      *
      * @param name the name of the bean
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Gets the name of the bean
      *
      * @return name of the bean
      */
     public String getName() {
         return this.name;
     }
 
     /**
      * Sets the tag value
      *
      * @param value the tag value
      */
     public void setValue(String value) {
         this.value = value;
     }
 
     /**
      * Returns the tag value
      *
      * @return the tag value
      */
     public String getValue() {
         return value;
     }
 
     /**
      * Returns true if bean is in disabled state
      *
      * @return the tag state
      */
     public boolean isDisabled() {
         return disabled;
     }
 
     /**
      * Sets the disabled attribute of the bean to be in the 'flag' state.
      *
      * @param flag is true if this tag/bean is disabled
      */
     public void setDisabled(boolean flag) {
         this.disabled = flag;
     }
 
     /**
      * Returns disabled String if bean is disabled
      *
      * @return a String depending if bean is disabled
      */
     protected String checkDisabled() {
         if (disabled) {
             return " disabled='disabled' ";
         } else {
             return "";
         }
     }
 
     /**
      * Sets the bean to read-only
      *
      * @param flag is true if bean is read-only, false otherwise
      */
     public void setReadonly(boolean flag) {
         this.readonly = flag;
     }
 
     /**
      * Returns the read-only status of the bean
      *
      * @return read-only status of the bean
      */
     public boolean isReadonly() {
         return readonly;
     }
 
     /**
      * Returns a String to use in markup if the bean is read-only
      *
      * @return a String to use in markup if the bean is read-only
      */
     protected String checkReadonly() {
         if (readonly) {
            return " readonly='readonly' ";
         } else {
             return "";
         }
     }
 
     /**
      * Returns the CSS style name of the beans
      *
      * @return the name of the css style
      */
     public String getCssStyle() {
         return cssStyle;
     }
 
     /**
      * Sets the CSS style of the beans
      *
      * @param style css style name to set for the beans
      */
     public void setCssStyle(String style) {
         this.cssStyle = style;
     }
 
     /**
      * Sets the base component properties of the bean
      *
      * @param componentBean a ui bean
      */
     protected void setBaseComponentBean(BaseComponentBean componentBean) {
         componentBean.setBeanId(beanId);
         if (cssStyle != null) componentBean.setCssStyle(cssStyle);
         componentBean.setDisabled(disabled);
         if (name != null) componentBean.setName(name);
         if (value != null) componentBean.setValue(value);
         if (supportsJavaScript()) {
             supportsJS = true;
         } else {
             supportsJS = false;
         }
         componentBean.setSupportsJS(supportsJS);
         componentBean.setReadOnly(readonly);
     }
 
     /**
      * Updates the base component bean properties
      *
      * @param componentBean a ui bean
      */
     protected void updateBaseComponentBean(BaseComponentBean componentBean) {
         if ((cssStyle != null) && (!componentBean.getCssStyle().equals(""))) {
             componentBean.setCssStyle(cssStyle);
         }
         if ((name != null) && (componentBean.getName() == null)) {
             componentBean.setName(name);
         }
         if ((value != null) && (componentBean.getValue() == null)) {
             componentBean.setValue(value);
         }
         if (supportsJavaScript()) {
             supportsJS = true;
         } else {
             supportsJS = false;
         }
     }
 
     protected String getLocalizedText(String key) {
         PortletRequest req = (PortletRequest)pageContext.getAttribute("portletRequest");
         User user = req.getUser();
         String loc = (String)user.getAttribute(User.LOCALE);
         Locale locale = null;
         if (loc != null) {
             locale = new Locale(loc, "", "");
         } else {
             locale = pageContext.getRequest().getLocale();
         }
         ResourceBundle bundle = ResourceBundle.getBundle("Portlet", locale);
         return bundle.getString(key);
     }
 
 }
