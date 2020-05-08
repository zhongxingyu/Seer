 /**
  * 
  */
 package com.google.gwt.chrome.crx.linker.emiter;
 
 import com.google.gwt.chrome.crx.client.GwtContentScript;
 import com.google.gwt.chrome.crx.linker.artifact.GwtContentScriptArtifact;
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.UnableToCompleteException;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 
 /**
  * {@link GwtContentScriptEmiter} is responsible for creation of content script
  * from Java source code using GWT compiler and including it into manifest.json
  * file.
  * 
  * @author Izzet_Mustafayev
  * 
  */
 public class GwtContentScriptEmiter extends AbstractEmiter {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.google.gwt.chrome.crx.linker.emiter.Emiter#emit(com.google.gwt.core
 	 * .ext.TreeLogger, com.google.gwt.core.ext.GeneratorContext,
 	 * com.google.gwt.core.ext.typeinfo.JClassType, java.lang.String)
 	 */
 	@Override
 	public String emit(TreeLogger logger, GeneratorContext context, JClassType userType, String typeName)
 			throws UnableToCompleteException {
 		GwtContentScript.ManifestInfo spec = userType.getAnnotation(GwtContentScript.ManifestInfo.class);
 		Validator<GwtContentScript.ManifestInfo> validator;
 		validator = new Validator<GwtContentScript.ManifestInfo>(logger, GwtContentScript.class.getName(), typeName);
 
 		validator.ensureAnnotatedWithManifest(spec);
 
 		String subclassName = createSubclassName(userType.getQualifiedSourceName());
 		GwtContentScriptArtifact artifact;
		artifact = new GwtContentScriptArtifact(subclassName, spec.matches(), spec.runAt(), spec.allFrames());
 		context.commitArtifact(logger, artifact);
 		return null;
 	}
 }
