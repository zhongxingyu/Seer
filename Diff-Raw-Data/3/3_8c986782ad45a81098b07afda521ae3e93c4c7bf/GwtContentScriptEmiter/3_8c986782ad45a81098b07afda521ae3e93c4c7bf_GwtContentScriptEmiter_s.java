 /**
  * 
  */
 package com.google.gwt.chrome.crx.linker.emiter;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import com.google.gwt.chrome.crx.client.GwtContentScript;
 import com.google.gwt.chrome.crx.linker.ContentScriptGeneratedResource;
 import com.google.gwt.chrome.crx.linker.GwtContentScriptGenerator;
 import com.google.gwt.chrome.crx.linker.artifact.GwtContentScriptArtifact;
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.UnableToCompleteException;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.dev.cfg.ModuleDef;
 
 /**
  * {@link GwtContentScriptEmiter} is responsible for creation of content script
  * from Java source code using GWT compiler and including it into manifest.json
  * file.
  * 
  * @author Izzet_Mustafayev
  * 
  */
 public class GwtContentScriptEmiter extends AbstractEmiter {
 
 	private static final String CLEAR_CACHE_GIF = "clear.cache.gif";
 
 	private static final String WEB_INF = "WEB-INF/";
 
 	private ModuleDefinitionLoader loader;
 
 	public GwtContentScriptEmiter(final ModuleDefinitionLoader loader) {
 		this.loader = loader;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.google.gwt.chrome.crx.linker.emiter.Emiter#emit(com.google.gwt.core
 	 * .ext.TreeLogger, com.google.gwt.core.ext.GeneratorContext,
 	 * com.google.gwt.core.ext.typeinfo.JClassType, java.lang.String)
 	 */
 	@Override
 	public String emit(final TreeLogger logger, final GeneratorContext context, final JClassType userType,
 			final String typeName) throws UnableToCompleteException {
 		GwtContentScript.ManifestInfo spec = userType.getAnnotation(GwtContentScript.ManifestInfo.class);
 		Validator<GwtContentScript.ManifestInfo> validator;
 		validator = new Validator<GwtContentScript.ManifestInfo>(logger, GwtContentScript.class.getName(), typeName);
 
 		validator.ensureAnnotatedWithManifest(spec);
 
 		String moduleName = spec.module();
 		ModuleDef moduleDef = loader.loadModule(logger, moduleName);
 
 		if (null == moduleDef) {
 			notifyFailure(logger, "Module was not loaded: " + moduleName);
 		}
 
 		emitModuleResources(logger, context, moduleDef);
 		String moduleJavaScriptFile = moduleDef.getName() + ".js";
 		emitScriptDef(logger, context, spec, moduleJavaScriptFile);
 		return typeName;
 	}
 
 	private void emitScriptDef(final TreeLogger logger, final GeneratorContext context,
 			GwtContentScript.ManifestInfo spec, String moduleJavaScriptFile) throws UnableToCompleteException {
 		GwtContentScriptArtifact artifact;
 		artifact = new GwtContentScriptArtifact(moduleJavaScriptFile, spec.matches(), spec.runAt(), spec.allFrames());
 		context.commitArtifact(logger, artifact);
 	}
 
 	private void emitModuleResources(final TreeLogger logger, final GeneratorContext context, final ModuleDef moduleDef)
 			throws UnableToCompleteException {
 		// TODO: (webdizz) Need to have more independent logic
 		URL path = Thread.currentThread().getContextClassLoader().getResource("./");
 		String moduleName = moduleDef.getName();
 		String pathToModule = null;
 		if (null != path && path.getPath().contains(WEB_INF)) {
 			String modulePath = path.getPath();
 			pathToModule = modulePath.substring(0, modulePath.indexOf(WEB_INF)) + moduleName + "/";
 		}
 		if (null == pathToModule) {
			notifyFailure(logger, "Unable to resolve path to module : " + moduleName);
 		}
 		File moduleDir = new File(pathToModule);
 		if (!moduleDir.exists()) {
 			notifyFailure(logger, "Module directory does not exist : " + moduleName);
 		}
 		File[] files = moduleDir.listFiles();
 		processResources(logger, context, moduleName, files);
 	}
 
 	protected void notifyFailure(final TreeLogger logger, String message) throws UnableToCompleteException {
 		logger.log(TreeLogger.ERROR, message);
 		throw new UnableToCompleteException();
 	}
 
 	private void processResources(final TreeLogger logger, final GeneratorContext context, String moduleName,
 			File[] files) throws UnableToCompleteException {
 		if (null != files) {
 			Set<ContentScriptGeneratedResource> resources = new LinkedHashSet<ContentScriptGeneratedResource>();
 			for (File file : files) {
 				if (CLEAR_CACHE_GIF.equals(file.getName())) {
 					continue;
 				}
 				readAndCreateResource(logger, context, resources, file.getName(), file.getAbsolutePath());
 			}
 			if (resources.isEmpty()) {
 				notifyFailure(logger, "Module does not contain any resource : " + moduleName);
 			}
 			for (ContentScriptGeneratedResource resource : resources) {
 				context.commitArtifact(logger, resource);
 			}
 		}
 	}
 
 	private void readAndCreateResource(final TreeLogger logger, final GeneratorContext context,
 			Set<ContentScriptGeneratedResource> resources, final String fileName, final String pathToFile) {
 		try {
 			BufferedReader reader;
 			reader = new BufferedReader(new FileReader(pathToFile));
 			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
 			try {
 				boolean eof = false;
 				while (!eof) {
 					int input = reader.read();
 					if (input == -1) {
 						eof = true;
 						break;
 					}
 					out.write(input);
 				}
 			} finally {
 				reader.close();
 			}
 			byte[] data = out.toByteArray();
 			if (data.length > 0) {
 				ContentScriptGeneratedResource resource;
 				resource = new ContentScriptGeneratedResource(GwtContentScriptGenerator.class, fileName, data);
 				resources.add(resource);
 			}
 		} catch (FileNotFoundException e) {
 			logger.log(TreeLogger.WARN, "Unable to find generated javascript file: " + pathToFile);
 		} catch (IOException e) {
 			logger.log(TreeLogger.WARN, "Unable to read generated javascript file: " + pathToFile);
 		}
 	}
 }
