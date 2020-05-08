 package app.config;
 
 /**
  * The callback interface exposed to the javascript configuration file.
  */
 public interface ScriptServices {
     /**
      * Register a plugin
      * @param pluginName The plugin classname
      */
     void register(String pluginName);
 
     /**
      * Do a two-phase load of the registered plugins, first "load" all, then let all resolve their dependencies.
      */
     void loadAll();
 
     /**
      * Get hold of a named layout area
      * @param str The name of the layout area
      * @return A layout area that can be populated with widgets.
      */
     LayoutArea getLayout(String str);
 }
