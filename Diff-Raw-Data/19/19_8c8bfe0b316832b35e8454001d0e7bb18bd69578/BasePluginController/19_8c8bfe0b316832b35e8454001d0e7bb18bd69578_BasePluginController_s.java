 /**
  * 
  */
 package net.frontlinesms.plugins;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import net.frontlinesms.Utils;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 /**
  * Base implementation of the {@link PluginController} annotation.
  * 
  * Implementers of this class *must* implement the {@link PluginControllerProperties} class.
  * 
  * This class includes default implementation of the text resource loading methods.  These attempt to load text resources
  * in the following way:
  * TODO properly document how this is done from the methods {@link #getDefaultTextResource()} and {@link #getTextResource(Locale)}.
  * 
  * @author Alex
  */
 public abstract class BasePluginController implements PluginController {
 //> STATIC CONSTANTS
 
 //> INSTANCE PROPERTIES
 	/** Logging object for this class */
 	protected final Logger log = Utils.getLogger(this.getClass());
 	/** Lazy-initialized singleton Thinlet Tab component for this instance of this plugin. */
 	private Object thinletTab;
 
 //> CONSTRUCTORS
 
 //> ACCESSORS
 	/** @see net.frontlinesms.plugins.PluginController#getTab(net.frontlinesms.ui.UiGeneratorController) */
 	public synchronized Object getTab(UiGeneratorController uiController) {
		if(this.thinletTab == null) {
 			this.thinletTab = this.initThinletTab(uiController);
 		}
 		return this.thinletTab;
 	}
 	
 	/**
	 * Initialise the Thinlet tab component for this plugin instance.
 	 * @param uiController {@link UiGeneratorController} instance that will be the parent of this tab.
 	 * @return a new instance of the thinlet tab component for this plugin.
 	 */
 	protected abstract Object initThinletTab(UiGeneratorController uiController);
 	
 	/** @see PluginController#getName() */
 	public String getName() {
 		assert(this.getClass().isAnnotationPresent(PluginControllerProperties.class)): "Implementers of this class *must* implement the PluginControllerProperties annotation.";
 		return this.getClass().getAnnotation(PluginControllerProperties.class).name();
 	}
 	
 	/** @see net.frontlinesms.plugins.PluginController#getDefaultTextResource() */
 	public Map<String, String> getDefaultTextResource() {
 		Map<String, String> defaultTextResource = getTextResource();
 		assert(defaultTextResource!=null):"The text resource for this plugin is not available.";
 		return defaultTextResource;
 	}
 	
 	/** @see net.frontlinesms.plugins.PluginController#getTextResource(java.util.Locale) */
 	public Map<String, String> getTextResource(Locale locale) {
 		if(locale.getVariant() != null) {
 			Map<String, String> textResource = getTextResource(locale.getLanguage(), locale.getCountry(), locale.getVariant());
 			if(textResource != null) return textResource;
 		}
 		if(locale.getCountry() != null) {
 			Map<String, String> textResource = getTextResource(locale.getLanguage(), locale.getCountry());
 			if(textResource != null) return textResource;
 		}
 		return getTextResource(locale.getLanguage());
 	}
 
 //> INSTANCE HELPER METHODS	
 	/**
 	 * Gets a text resource file from the classpath.
 	 * @param nameExtensions extensions added to the end of the standard filename.  These will be separated from the base name and each other by underscores.
 	 * @return The text resource, or <code>null</code> if the resource could not be found.
 	 */
 	private Map<String, String> getTextResource(String... nameExtensions) {
 		String resourceFilePath = getTextResourcePath(nameExtensions);
 		
 		// Attempt to load the text resource using relative path with local classloader
 		InputStream textResourceInputStream = this.getClass().getResourceAsStream(resourceFilePath);
 		
 		if(textResourceInputStream == null) {
 			// Resource could not be found, so return null
 			return null;
 		} else {
 			try {
 				return InternationalisationUtils.loadTextResources(resourceFilePath, textResourceInputStream);
 			} catch (IOException ex) {
 				log.info("There was a problem loading language bundle from " + resourceFilePath, ex);
 				return null;
 			}
 		}
 	}
 	
 	/**
 	 * Gets the path for a text resource bundle.
 	 * @param nameExtensions extensions added to the end of the standard filename.  These will be separated from the base name and each other by underscores.
 	 * @return classpath location of the text resource bundle
 	 */
 	private String getTextResourcePath(String... nameExtensions) {
 		String resourceFilePath = getTextResourceFilename(nameExtensions) + ".properties";
 		return resourceFilePath;
 	}
 	
 	/**
 	 * Gets the filename for the text resource bundle.
 	 * @param nameExtensions extensions added to the end of the standard filename.  These will be separated from the base name and each other by underscores.
 	 * @return File name for the text resource bundle
 	 */
 	private String getTextResourceFilename(String ... nameExtensions) {
 		// Construct the name of the .properties file
 		String fileName =  this.getClass().getSimpleName();
 		
 		// Add suffix "Text" to file name, BEFORE the language & country suffixes
 		fileName += "Text";
 		
 		// Append any required extensions to the filename
 		for(String extension : nameExtensions) {
 			if(extension != null) {
 				fileName += "_" + extension;
 			}
 		}
 		
 		return fileName;
 	}
 
 //> STATIC FACTORIES
 
 //> STATIC HELPER METHODS
 }
