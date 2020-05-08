 package controllers;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import models.AppProps;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOCase;
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.Play;
 import play.exceptions.UnexpectedException;
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import play.mvc.Util;
 import play.templates.JavaExtensions;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.DropboxAPI.DropboxInputStream;
 import com.dropbox.client2.DropboxAPI.Entry;
 import com.dropbox.client2.RESTUtility;
 import com.dropbox.client2.exception.DropboxException;
 import com.dropbox.client2.exception.DropboxServerException;
 
 import exception.FileSizeLimitException;
 
 /**
  * Controller to handle the action for the advanced file choose 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class FileChooser extends Controller {
 
 	
 	static final File publicRepo;
 	
 	static { 
 		
 		/* 
 		 * initialize the 'publicRepo' path
 		 */
 		String path = AppProps.instance().getString("settings.path.public.data", Play.getFile("public/bb3").getAbsolutePath());
 		publicRepo = new File(path);
 		
 		if( !publicRepo.exists() ) { 
 			Logger.warn("The public data root does not exist: '%s'", publicRepo);
 		}
 		
 	}
 	
 	/**
 	 * The main file chooser page. 
 	 * 
 	 * Parameters: 
 	 * - fieldId: it defines the input field name to which the filechooser dialog is binded.
 	 *   It will be used by the callback function 'tb_select' defined in the 'main.html' page  
 	 * 
 	 * 
 	 */
 	public static void index() { 
 		
 		renderArgs.put("fieldId", params.get("fieldId"));
 		renderArgs.put("dropbox", Play.configuration.getProperty("settings.dropbox.key") != null &&  Play.configuration.getProperty("settings.dropbox.secret") != null);
 		renderArgs.put("file_size_limit", getFileSizeLimit() );
 		render("FileChooser/filechooser.html");
 	}
 	
 
 	@Util
 	private static long getFileSizeLimit() {
 		return AppProps.instance().getLong("settings.filechooser.file_size_limit", 20*1024*1024);
 	}
 
 
 	@Util
 	static void renderTreeItems( List<FileEntry> folders , List<FileEntry> files, boolean isRoot ) { 
 		
 		if( isRoot && ( folders==null || folders.size()==0 ) && (files==null || files.size()==0 )) {
 			renderText("(nothing found)");
 		} 
 		
 		renderArgs.put("folders", folders);
 	    renderArgs.put("files", files);
 	    render("FileChooser/treeitem.html");
 	} 
 
 	/**
 	 * List files in the 'Public' file repository 
 	 * 
 	 * @param dir the directory to list 
 	 * @param query the to filter query to search into the repo
 	 */
 	public static void listPublicData(String dir, String query) { 
 		Logger.info("FileChooser#listPublicRepo - dir: '%s'; query: '%s'", dir, query);
 		final int MAX_FOLDER_ITEMS = AppProps.instance().getInteger("settings.filechooser.max_folder_items", 500);
 		
 	    /* 
 	     * normalize the specified dir 
 	     */
 
 	    if (dir == null) {
 	    	dir = "/";
 	    }
 		
 		List<FileEntry> _files = new ArrayList<FileChooser.FileEntry>();
 		List<FileEntry> _folders = new ArrayList<FileChooser.FileEntry>();
 		
 		final File path = new File(publicRepo, dir);
 	    if (path.exists()) {
 	    	
 	    	// define the filter 
 	    	// search and sort the result
 			;
     		List<FileEntry> result = new ArrayList<FileEntry>();
 	    	if( "/".equals(dir) && !StringUtils.isEmpty(query) && query.length()>=3 )  {
 	    		String sQuery = query.contains("*") ? query : "*" + query + "*";
 	    		searchIntoPublicRepo(path, sQuery, result, MAX_FOLDER_ITEMS);
 	    	}
 	    	else { 
 				FilenameFilter filter = new FilenameFilter() {
 				    public boolean accept(File dir, String name) {
 						return name.charAt(0) != '.';
 				    }
 				};
 				File[] files = path.listFiles(filter);
 				for( File ff : files ) { 
 					result.add(wrap(ff,publicRepo,false));
 				}
 	    	}
 	    	
 	    	Collections.sort(result);
 
 			// All dirs
 			for (FileEntry item: result ) {
 				if (item.isDir) {
 					_folders.add(item);
 			    }
 				else { 
 					_files.add(item);
 				}
 			}
 	    }		
 
 	    renderTreeItems(_folders, _files, "/".equals(dir));
 	}	
 	
 	/**
 	 * Show the list of files available the linked 'Dropbox' account 
      *
 	 * @param dir the directory to list 
 	 * @param query the to filter query to search into the repo
 	 */
 	public static void listDropboxData( String dir, String query ) { 
 		Logger.info("FileChooser#listDropboxData - dir: '%s'; query: '%s'", dir, query);
 		final int MAX_FOLDER_ITEMS = AppProps.instance().getInteger("settings.filechooser.max_folder_items", 500);
 
 		/*
 		 * check if connected otherwise shows Dropbox connection box
 		 */
 		if( !Dropbox.isLinked() ) { 
 			errorJson("Your Dropbox account is unlinked. Re-try reconnecting to Dropbox refreshing this page.");
 		}
 		
 	    if (dir == null) {
 	    	dir = "/";
 	    }
 	
 		try {
 			boolean usePathForName;
 			List<Entry> result; 
 			/* 
 			 * Search into dropbox folder if a query has provided 
 			 * */
 			if( "/".equals(dir) && StringUtils.isNotEmpty(query) && query.length()>= 3 ) { 
 				result = Dropbox.get().search("/", query, MAX_FOLDER_ITEMS, false);
 				usePathForName = true;
 			}
 			/* 
 			 * .. or navigate into the folder 
 			 */
 			else { 
 				DropboxAPI.Entry entry = Dropbox.get().metadata(dir, MAX_FOLDER_ITEMS, null, true, null);
 				result = entry.contents;
 				usePathForName = false;
 			}
 
 			/* 
 			 * split folders from files 
 			 */
 			List<FileEntry> _files = new ArrayList<FileChooser.FileEntry>();
 			List<FileEntry> _folders = new ArrayList<FileChooser.FileEntry>();
 
 			for( Entry item : result ) { 
 				if( item.isDir ) { 
 					_folders.add( wrap(item,usePathForName) );
 				}
 				else { 
 					_files.add( wrap(item,usePathForName) );
 				}
 			}
 			
 			Collections.sort(_files);
 			Collections.sort(_folders);
 		    
 			renderTreeItems(_folders, _files, "/".equals(dir));
 		} 
 		catch( DropboxServerException e ) {
 			Logger.error(e, "Error accessing Dropbox server for dir: '%s' - query: '%s' ", dir, query);
 			errorJson("Cannot access the requested path: '%s'", dir);
 
 		} 
 		catch (DropboxException e) {
 			Logger.error(e,"Cannot connect Dropbox");
 			Dropbox.unlink();
 			errorJson("Error accessing your Dropbox account");
 		}
 	
 		
 	}
 	
 	/**
 	 * Copy the a file from the Dropbox account to the user local storage 
 	 * 
 	 * @param filePath the file in the 'Dropbox' storage to be copied locally 
 	 */
 	public static void copyDropboxFile( String filePath ) { 
 		Logger.info("FileChooser#copyDropboxFile - '%s'", filePath);
 		long MAX = getFileSizeLimit();
 		
 		if( !Dropbox.isLinked() ) { 
 			errorJson("Your Dropbox account is unlinked. Re-try reconnecting to Dropbox refreshing this page");
 		}
 		
 		request.format = "json";
 		String fileName = FilenameUtils.getName(filePath);
 		File target = Data.newUserFile(fileName);
 		try {
 			DropboxInputStream in = Dropbox.get().getFileStream(filePath, null);
 			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target, false));
 			copy(in, out, MAX);
 			// ^ Stream closed by the method
 
 			String result = String.format( "{" +
 					"\"success\":true, " +
 					"\"name\": \"%s\"," +
 					"\"size\": \"%s\" }", 
 					JavaExtensions.escapeJavaScript(target.getName()),
 					JavaExtensions.formatSize( target.length() ));
 			renderJSON(result);
 		} 
 		catch( FileSizeLimitException e ) {
 			target.delete();
 			Logger.warn("The following dropbox download is too big to be downloaded: '%s'", filePath);
 			errorJson("The file '%s' cannot be downloaded because exceed the size limit (%s)", fileName, JavaExtensions.formatSize(MAX));
 		}
 		catch( Exception e ) { 
 			Logger.error(e, "Cannot get the following file from dropbox: '%s'", filePath);
 			Dropbox.unlink();
 			errorJson("Cannot access the request file from Dropbox");
 		}
 
 	}
 	
 	/**
 	 * Ajax action to retrieve the Dropbox link status 
 	 */
 	public static void isDropboxLinked() { 
 		request.format = "json";
 		try { 
 			String result = String.format("{\"linked\":%s }", Dropbox.isLinked());
 			renderJSON(result);
 		}
 		catch( Exception e ) { 
 			Logger.error(e,"Error verifying Dropbox link status");
 			Dropbox.unlink();
 			errorJson("Cannot verify link status to your Dropbox account");
 		}
 	}
 	
 	/**
 	 * Download the specified URL document to the user local storage
 	 * 
 	 * @param url
 	 * @throws InterruptedException 
 	 */
 	public static void copyUrlFile( String url ) throws InterruptedException { 
 		Logger.info("FileChooser#copyUrlFile - '%s'", url);
 		final long MAX = getFileSizeLimit();		
 		
 		request.format = "json";
 		String fileName = FilenameUtils.getName(url);
 		File target = Data.newUserFile(fileName);
 		
 		try {
 			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
 			copy(new URL(url).openStream(), out, MAX);
 
 			
 			String result = String.format(
 					"{" +
 					"\"success\":true, " +
 					"\"name\": \"%s\"," +
 					"\"size\": \"%s\" " +
 					"}", 
 					JavaExtensions.escapeJavaScript(target.getName()),
 					JavaExtensions.formatSize( target.length() )
 					);
 			renderJSON(result);
 		} 
 		catch( FileSizeLimitException e ) {
 			target.delete();
 			Logger.warn("The following dropbox download is too big to be downloaded: '%s'", fileName);
 			errorJson("The file '%s' cannot be downloaded because exceed the size limit (%s)", fileName, JavaExtensions.formatSize(MAX));
 		}
 		catch( MalformedURLException e ) { 
 			Logger.warn(e, "Not a valid URL: '%s'", url);
 			errorJson(StatusCode.BAD_REQUEST, "Malformed '\"URL");
 		}
 		catch (IOException e) {
 			Logger.error(e, "Cannot download the specified URL: '%s'", url);
 			errorJson("Cannot download the specified URL");
 		}
 		
 	}
 	
 	/**
 	 * Show in the file chooser dialog the list of rencent used files for the current user 
 	 * 
 	 * @param dir (not used)
 	 * @param query string to filter the result list 
 	 */
 	public static void listRecentData(String dir, String query) { 
 		Logger.info("FileChooser#listRecentData - dir: '%s'; query: '%s'", dir, query);
 		
 		final File path = Data.getUserTempPath();
 		final List<FileEntry> result = new ArrayList<FileEntry>();
 		FilenameFilter filter;
 		
 		/*
 		 * define the selection filter 
 		 */
 		if( !StringUtils.isEmpty(query) && query.length()>=3 )  {
     		final String sQuery = query.contains("*") ? query : "*" + query + "*";
     		filter = new FilenameFilter() {
 
 				@Override
 				public boolean accept(File dir, String name) {
 					return FilenameUtils.wildcardMatch(name, sQuery, IOCase.INSENSITIVE);
 				}}; 
     	}
 		/*
 		 * select all except the '.' file
 		 */
     	else { 
 			filter = new FilenameFilter() {
 			    public boolean accept(File dir, String name) {
 					return name.charAt(0) != '.';
 			    }
 			};
     	}
     	
 		File[] files = path.listFiles(filter);
 		for( File ff : files ) { 
 			result.add(wrap(ff,path,false));
 		}
     	Collections.sort(result);		
 		
 		/* 
 		 * render the result
 		 */
 	    renderTreeItems(null, result, "/".equals(dir));
 	}
 
 	/**
 	 * Copy the specified file in the user local storage 
 	 * 
 	 * @param filePath the path on the local file system to copy 
 	 */
 	public static void copyPublicDataFile( String filePath ) { 
 		Logger.info("FileChooser#copyPublicDataFile - '%s'", filePath);
 		/*
 		 * filename is not normalized because we assume that 'public' data filenames are well formated 
 		 * (does not contains blanks and special caracters
 		 */
 		File targetPath = Data.getUserTempPath();
 		File item = new File(publicRepo, filePath);
		String cmd = String.format("ln -s %s %s", item.getAbsolutePath(), item.getName());
 		try {
 			Runtime.getRuntime().exec(cmd, null, targetPath).waitFor();
 			renderJSON("{\"success\":true }");
 		} 
 		catch( Exception e ) {
 			Logger.error("Error creating symlink: '%s'", cmd);
 			renderJSON("{\"success\":false }");
 		} 
 	} 
 	
 	@Util
 	static boolean searchIntoPublicRepo( File path, String query, List<FileEntry> result, long max ) { 
 
 		if( path == null ) { return true; }
 		
 		boolean continueTraverse = true;
 		if( path.isDirectory() ) { 
 			for( File file : path.listFiles() ) { 
 				if( FilenameUtils.wildcardMatch(file.getName(), query, IOCase.INSENSITIVE) ) { 
 					result.add( wrap(file,publicRepo,true)  );
 					continueTraverse = (result.size() <= max);
 				}
 
 				if( continueTraverse && file.isDirectory() ) { 
 					continueTraverse = searchIntoPublicRepo(file,query,result,max);
 				}
 				
 				if( !continueTraverse ) { 
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	/*
 	 * Wrap a Java File object to out common rapresentation 
 	 * 
 	 */
 	@Util
 	protected static FileEntry wrap( File file, File root, boolean usePathForName ) { 
 		if( file == null ) return null;
 		
 		FileEntry result = new FileEntry();
 		result.path = FilenameUtils.normalize(file.getAbsolutePath());
 		result.ext = FilenameUtils.getExtension(result.name);
 		result.size = JavaExtensions.formatSize(file.length());
 		result.length = file.length();
 		result.modified = new Date(file.lastModified());
 		result.isDir = file.isDirectory();
 		
 		// fix the path 
 		if( root != null ) { 
 			String sRoot = FilenameUtils.normalizeNoEndSeparator(root.getAbsolutePath());
 			if( result.path.startsWith(sRoot) ) { 
 				result.path = result.path.substring(sRoot.length());
 				if( !result.path.startsWith("/") ) { 
 					result.path = "/" + result.path;
 				}
 			}
 		}
 
 		// the 'name' to be visualized
 		result.name = usePathForName ? result.path : file.getName();
 		if( result.name.startsWith("/") ) { 
 			result.name = result.name.substring(1);
 		}
 		
 		// the below fix is requied by jQueryFileTree component
 		if( result.isDir && !result.path.endsWith("/")) { 
 			result.path += "/";
 		}
 		return result;
 	}
 	
 	/*
 	 * Wrap a dropbox file entry to our file representation  
 	 */
 	@Util
 	protected static FileEntry wrap( Entry file, boolean usePathForName ) { 
 		if( file == null ) return null;
 		
 		FileEntry result = new FileEntry();
 		result.path = file.path;
 		result.name = usePathForName ? file.path : FilenameUtils.getName(result.path);
 		result.ext = FilenameUtils.getExtension(result.name);
 		result.size = JavaExtensions.formatSize(file.bytes);
 		result.length = file.bytes;
 		result.isDir = file.isDir;
 		result.modified = StringUtils.isNotEmpty(file.modified) ? RESTUtility.parseDate(file.modified) : null;
 
 		if( result.name.startsWith("/") ) { 
 			result.name = result.name.substring(1);
 		}
 		
 		if( result.isDir && result.path != null && !result.path.endsWith("/")) { 
 			result.path += "/";
 		}
 
 		return result;
 		
 	}
 	
 	/*
 	 * Common wrapper to items to be rendered in the tree view 
 	 */
 	public static class FileEntry implements Serializable, Comparable<FileEntry> { 
 		public String path;
 		public String name;
 		public String ext;
 		public String size;
 		public long length;
 		public boolean isDir;
 		public Date modified;
 		
 		@Override
 		public int compareTo(FileEntry o) {
 			return name.compareTo(o.name);
 		}
 
 		@Override
 		public String toString() {
 			return "FileEntry [path=" + path + ", name=" + name + ", ext="
 					+ ext + ", size=" + size + ", length=" + length
 					+ ", isDir=" + isDir + ", modified=" + modified + "]";
 		}
 	}
 	
 	
 	@Util 
 	static void errorJson( int errcode, String message, Object... args ) {
 		request.format = "json";
 		error(errcode, String.format(message, args));
 	} 
 	
 	
 	@Util
 	static void errorJson( String message, Object ... args ) {
 		request.format = "json";
 		error(String.format(message, args));
 	} 
 	
 	
 	@Util
     public static void copy(InputStream is, OutputStream os, long max) throws FileSizeLimitException {
         try {
         	long tot=0;
             int read = 0;
             byte[] buffer = new byte[50*1024];
             while ((read = is.read(buffer)) > 0) {
                 os.write(buffer, 0, read);
                 tot+=read;
                 if( tot > max ) {
                 	throw new FileSizeLimitException();
                 }
             }
         } catch(IOException e) {
             throw new UnexpectedException(e);
         } finally {
             try { is.close(); } catch(Exception e) {  }
             try { os.close(); } catch(Exception e) { }
         }
     }	
 
 }
