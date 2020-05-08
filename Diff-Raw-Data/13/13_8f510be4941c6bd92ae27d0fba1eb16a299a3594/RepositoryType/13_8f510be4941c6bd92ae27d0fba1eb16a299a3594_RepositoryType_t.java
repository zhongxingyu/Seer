 package org.kalibro.core.model.enums;
 
 import java.io.File;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.kalibro.KalibroError;
 import org.kalibro.core.loaders.ProjectLoader;
 import org.kalibro.core.model.Repository;
 import org.kalibro.core.util.Identifier;
 
 public enum RepositoryType {
 
 	LOCAL_DIRECTORY, LOCAL_TARBALL, LOCAL_ZIP, BAZAAR, CVS("CVS"), GIT, MERCURIAL, REMOTE_ZIP, REMOTE_TARBALL,
 	SUBVERSION;
 
 	public static Set<RepositoryType> supportedTypes() {
 		Set<RepositoryType> supported = new TreeSet<RepositoryType>();
 		for (RepositoryType type : values())
 			if (type.isSupported())
 				supported.add(type);
 		return supported;
 	}
 
 	private String name;
 	private ProjectLoader loader;
 
 	private RepositoryType() {
 		this("");
 		name = Identifier.fromConstant(name()).asText();
 	}
 
 	private RepositoryType(String name) {
 		this.name = name;
 		initializeLoader();
 	}
 
 	private void initializeLoader() {
 		String className = Identifier.fromConstant(name()).asClassName() + "Loader";
 		try {
 			loader = (ProjectLoader) Class.forName("org.kalibro.core.loaders." + className).newInstance();
 		} catch (Exception exception) {
 			throw new KalibroError("Error creating loader for " + this, exception);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	public boolean isLocal() {
 		return name().startsWith("LOCAL");
 	}
 
 	public boolean isSupported() {
 		return loader.validate();
 	}
 
 	public boolean supportsAuthentication() {
 		return loader.supportsAuthentication();
 	}
 
 	public void load(Repository repository, File loadDirectory) {
 		loader.load(repository, loadDirectory);
 	}
 }
