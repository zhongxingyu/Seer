 package org.kalibro;
 
 import java.util.SortedSet;
 
 import org.kalibro.core.Identifier;
 import org.kalibro.dao.DaoFactory;
 
 /**
  * Type of version control system to retrieve the source code of a {@link Repository}.
  * 
  * @author Carlos Morais
  */
 public enum RepositoryType {
 
	BAZAAR, CVS("CVS"), GIT, LOCAL_DIRECTORY, LOCAL_TARBALL, LOCAL_ZIP, MERCURIAL, REMOTE_TARBALL, REMOTE_ZIP,
 	SUBVERSION;
 
 	public static SortedSet<RepositoryType> supportedTypes() {
 		return DaoFactory.getRepositoryDao().supportedTypes();
 	}
 
 	private String name;
 
 	private RepositoryType() {
 		name = Identifier.fromConstant(name()).asText();
 	}
 
 	private RepositoryType(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	public boolean isLocal() {
 		return name().startsWith("LOCAL");
 	}
 }
