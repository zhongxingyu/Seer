 package com.oshmidt;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.egit.github.core.Gist;
 import org.eclipse.egit.github.core.GistFile;
 
 public class GistLocalFileManager extends GistLocalRepository {
 
 	public static final String DEFAULT_PATH = "localRepository/";
 	public static final String GIST_FILE_EXT = ".gist";
 	public static Logger glfmLogger = Logger.getLogger("logfile");
 
 	void loadDefaultRepoPath() {
 		setRepoPath(DEFAULT_PATH);
 	}
 
 	public List<Gist> readGists() {
 		ArrayList<Gist> gists = new ArrayList<Gist>();
		checkPath();
 		if (!new File(getRepoPath()).exists()) {
 			return null;
 		}
 
 		FileFilter filter = new FileFilter() {
 			public boolean accept(File f) {
 				if (f.isFile() && f.getName().endsWith(GIST_FILE_EXT))
 					return true;
 				else
 					return false;
 			}
 		};
 		try {
 			for (File gst : new File(getRepoPath()).listFiles(filter)) {
 				FileInputStream fis = new FileInputStream(gst);
 				ObjectInputStream oin = new ObjectInputStream(fis);
 				Gist tsd;
 				tsd = (Gist) oin.readObject();
 				gists.add(tsd);
 				oin.close();
 			}
 		} catch (IOException e) {
 			glfmLogger.error(e);
 		} catch (ClassNotFoundException e) {
 			glfmLogger.error(e);
 		}
 		return gists;
 	}
 
 	public void writeGists(List<Gist> gists) {
 
 		try {
 			for (Gist gist : gists) {
 				FileOutputStream fos;
				preparePath();
 				fos = new FileOutputStream(getRepoPath() + gist.getId()
 						+ GIST_FILE_EXT);
 				ObjectOutputStream oos = new ObjectOutputStream(fos);
 				oos.writeObject(gist);
 				oos.flush();
 				oos.close();
 			}
 		} catch (IOException e) {
 			glfmLogger.error(e);
 		}
 	}
 
	public void preparePath() {
		checkPath();
 		if (!new File(getRepoPath()).exists()) {
 			new File(getRepoPath()).mkdirs();
 		}
 	}
 
	public void checkPath() {
 		if (getRepoPath() == null) {
 			loadDefaultRepoPath();
 		}
 	}
 
 	public void writeFiles(Gist gist) {
 
 		Map<String, GistFile> gistFiles = gist.getFiles();
 		Set<String> set = gistFiles.keySet();
 		for (String s : set) {
 			GistFile gf = gistFiles.get(s);
 			System.out.println(gf.getFilename());
 			URL website;
 			try {
 				website = new URL(gf.getRawUrl());
 
 				ReadableByteChannel rbc = Channels.newChannel(website
 						.openStream());
 
 				setRepoPath(getRepoPath() + File.separator + gist.getId()
 						+ File.separator);
				preparePath();
 				FileOutputStream fos = new FileOutputStream(new File(
 						getRepoPath(), gf.getFilename()));
 				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 				fos.close();
 				loadDefaultRepoPath();
 			} catch (MalformedURLException e) {
 				glfmLogger.error(e);
 			} catch (IOException e) {
 				glfmLogger.error(e);
 			}
 		}
 	}
 
 	public void writeFiles(List<Gist> gists) {
 		for (Gist gist : gists) {
 			writeFiles(gist);
 		}
 
 	}
 
 	public Map<Gist, List<GistFile>> readFiles() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
