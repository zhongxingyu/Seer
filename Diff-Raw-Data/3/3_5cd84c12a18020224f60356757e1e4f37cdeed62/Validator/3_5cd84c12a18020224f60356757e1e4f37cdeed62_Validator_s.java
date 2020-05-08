 /**
  * 
  */
 package com.google.gwt.chrome.crx.linker.emiter;
 
 import java.util.List;
 
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.UnableToCompleteException;
 
 /**
  * @author webdizz
  * 
  */
 public class Validator<M> {
 
 	private TreeLogger logger;
 
 	private String artifactName;
 
 	private String typeName;
 
 	public Validator(final TreeLogger logger, final String artifactName, final String typeName) {
 		super();
 		this.logger = logger;
 		this.artifactName = artifactName;
 		this.typeName = typeName;
 	}
 
 	public void ensureAnnotatedWithManifest(final M manifest) throws UnableToCompleteException {
 		if (manifest == null) {
			logger.log(TreeLogger.ERROR, artifactName + " (" + typeName + ") must be annotated with a Specificaiton.");
 			throw new UnableToCompleteException();
 		}
 	}
 
 	public void ensureActionHasIcon(List<String> iconFiles) throws UnableToCompleteException {
 		if (iconFiles.isEmpty()) {
 			logger.log(TreeLogger.ERROR, artifactName + " must have at least one Icon (" + typeName + ")");
 			throw new UnableToCompleteException();
 		}
 	}
 
 }
