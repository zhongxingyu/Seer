 package com.andrewreisner.tractorDrive;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.eclipse.jgit.api.*;
 import org.eclipse.jgit.api.errors.CheckoutConflictException;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.InvalidRefNameException;
 import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
 import org.eclipse.jgit.api.errors.RefNotFoundException;
 import org.eclipse.jgit.lib.*;
 import org.eclipse.jgit.storage.file.FileRepository;
 
 public class TractorRepo {
 
     private Repository repo;
     private Git git;
     private String version;
     private String repoPath;
     private ArrayList<String> updates;
 
     public TractorRepo(String path) throws Exception {
 		updates = null;
 		repo = new FileRepository(path + "/.git");
 		git = new Git(repo);
 		repoPath = path;
 		System.out.println(path);
 		String originUrl = repo.getConfig().getString("remote", "origin", "url");
 		if (!originUrl.equals("https://github.com/WartburgComputerClub/tractorDrive.git")) {
 		    System.out.println(originUrl);
 			throw new Exception("Invalid tractorDrive repository");
 		}
 		BufferedReader input = new BufferedReader(new FileReader(new File(repoPath + "/version")));
 		version = input.readLine();
 		input.close();
 		System.out.println(version);
 	}
 	
 	public void fetchUpdates(ProgressMonitor monitor) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException, IOException {
 		git.checkout().setName("release").call();
 		git.pull().call();
 		
 		updates = new ArrayList<String>();
 		BufferedReader input = new BufferedReader(new FileReader(new File(repoPath + "/releases")));
 		String line = null;
 		boolean isNew = false;
 		try {
 		while ((line = input.readLine()) != null) {
 		    if (line.equals(version))
 			isNew = true;
 		    else if (isNew)
 			updates.add(line);
 		}
 		} finally {
 		input.close();
 		}
 		git.checkout().setName("master").call();
 	}
 
     public String getReleaseNotes(String version) {
     	StringBuilder ret = new StringBuilder();
     	try {
 	    git.checkout().setName("release").call();
     	BufferedReader input = new BufferedReader(new FileReader(new File(repoPath + "/docs/release_notes/" + version)));
     	try {
     		String line = null;
     		while ((line = input.readLine()) != null) {
     			ret.append(line);
     			ret.append(System.getProperty("line.separator"));
     		}
     	} finally {
     		input.close();
     	}
 	git.checkout().setName("master").call();
     	} catch (Exception ex) {
     		ex.printStackTrace();
     	}
     	return ret.toString();
     }
     
     public String[] getUpdateVersions() {
     	if (updates == null) return null;
     	else {
     		String[] ret = new String[updates.size()];
     		return updates.toArray(ret);
     	}
     }
    
    public String getVersion() {
    	return version;
    }
 
 }
