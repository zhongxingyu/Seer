 package app.config;
 
 /**
  * The interface exposed to javascript to add widgets to a layout
  */
 public interface LayoutArea {
     /**
      * Add a widget by name
      * @param widgetName The name of the widget
      */
     void add(String widgetName);
 
     /**
      * Add a widget by name with parameters
      * @param widgetName The widget name
     * @param param A comma separated string with parameters, ex: "a=1,b=2"
      */
     void add(String widgetName, String param);
 
 }
