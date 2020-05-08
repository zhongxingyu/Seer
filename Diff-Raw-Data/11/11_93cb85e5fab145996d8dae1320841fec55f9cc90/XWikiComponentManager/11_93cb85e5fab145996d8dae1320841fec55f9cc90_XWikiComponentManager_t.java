 package com.monochromeroad.grails.plugins.xwiki;
 
 import com.monochromeroad.grails.plugins.xwiki.macro.DefaultXWikiMacro;
 import org.xwiki.component.descriptor.DefaultComponentDescriptor;
 import org.xwiki.component.embed.EmbeddableComponentManager;
 import org.xwiki.component.manager.ComponentLookupException;
 import org.xwiki.component.phase.InitializationException;
 import org.xwiki.properties.BeanManager;
 import org.xwiki.rendering.macro.Macro;
 
 /**
  * @author Masatoshi Hayashi
  */
 public class XWikiComponentManager {
 
     private final EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
 
    public XWikiComponentManager(ClassLoader classLoader) {
        initialize(classLoader);
    }

     /**
      * For the Grails Default XWiki Rendering System.
      * It need to be initialized after construction.
      */
     XWikiComponentManager() {}
 
    void initialize(ClassLoader classLoader) {
         componentManager.initialize(classLoader);
     }
 
     public <T> T getInstance(Class<T> componentType, String hint) {
         try {
             return componentManager.getInstance(componentType, hint);
         } catch (ComponentLookupException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public <T> T getInstance(Class<T> componentType) {
         try {
             return componentManager.getInstance(componentType);
         } catch (ComponentLookupException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public <T extends DefaultXWikiMacro> void registerMacro(Class<T> macroClass) {
         DefaultXWikiMacro macroInstance = createMacroInstance(macroClass);
         macroInstance.setBeanManager(getInstance(BeanManager.class));
         try {
             macroInstance.initialize();
         } catch (InitializationException e) {
             throw new IllegalStateException(e);
         }
         registerMacro(macroInstance.getMacroName(), macroInstance);
     }
 
     public void registerMacro(String name, Macro macroInstance) {
         DefaultComponentDescriptor<Macro> macroDescriptor = new DefaultComponentDescriptor<Macro>();
         macroDescriptor.setImplementation(macroInstance.getClass());
         macroDescriptor.setRoleType(Macro.class);
         macroDescriptor.setRoleHint(name);
         componentManager.unregisterComponent(Macro.class, name);
         componentManager.registerComponent(macroDescriptor, macroInstance);
     }
 
     private <T extends DefaultXWikiMacro> T createMacroInstance(Class<T> macroClass) {
         try {
             return macroClass.newInstance();
         } catch (Exception e) {
             throw new IllegalStateException(e);
         }
     }
 }
