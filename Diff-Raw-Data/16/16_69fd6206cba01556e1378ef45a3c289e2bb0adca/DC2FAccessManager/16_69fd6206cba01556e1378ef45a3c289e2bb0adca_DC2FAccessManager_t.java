 package com.dc2f.backend.gwt.server;
 
 import java.io.File;
 import java.util.Collections;
 
 import com.dc2f.contentrepository.BranchAccess;
 import com.dc2f.contentrepository.CRSession;
 import com.dc2f.contentrepository.ContentRepository;
 import com.dc2f.contentrepository.filejson.JsonContentRepositoryProvider;
 
 public class DC2FAccessManager {
 
 	static BranchAccess readAccess;
 	
 	static BranchAccess writeAccess;
 	
 	public static BranchAccess getAccess() {
 		if (readAccess == null) {
			File crdir = null;
			String givenCrDir = System.getProperty("crdir");
			if (givenCrDir == null) {
				crdir = new File("../../../../design/src/main/resources/example");
				if (!crdir.exists()) {
					throw new IllegalArgumentException("Unable to find crdir. please set a 'crdir' system property to the path of a content repository.");
				}
			} else {
				crdir = new File(givenCrDir);
				if (!crdir.exists()) {
					throw new IllegalArgumentException("Invalid crdir given as system property {" + givenCrDir + "} {" + crdir.getAbsolutePath() + "}");
				}
			}
 			ContentRepository cr = new JsonContentRepositoryProvider().getContentRepository("simplejsonfile", Collections.singletonMap("directory", (Object)crdir.getAbsolutePath()));
 			CRSession conn = cr.authenticate(null);
 			readAccess = conn.openTransaction(null);
 		}
 		return readAccess;
 	}
 	
 	public static BranchAccess getWriteAccess() {
 		if (writeAccess == null) {
 			File crdir = new File(System.getProperty("crdir", "/Users/bigbear3001/Documents/dc2f/design/example"));
 			//ContentRepository cr = ContentRepositoryFactory.getInstance().getContentRepository("simplejsonfile", Collections.singletonMap("directory", (Object)crdir.getAbsolutePath()));
 			ContentRepository cr = new JsonContentRepositoryProvider().getContentRepository("writeablejsonfile", Collections.singletonMap("directory", (Object)crdir.getAbsolutePath()));
 			CRSession conn = cr.authenticate(null);
 			writeAccess = conn.openTransaction(null);
 		}
 		return writeAccess;
 	}
 	
 	
 
 }
