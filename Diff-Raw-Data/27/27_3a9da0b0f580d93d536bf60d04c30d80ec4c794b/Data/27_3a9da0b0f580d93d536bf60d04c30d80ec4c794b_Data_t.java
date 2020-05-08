 package controllers;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import models.AppProps;
 import models.OutItem;
 import models.OutResult;
 import models.Repo;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import play.data.Upload;
 import play.libs.IO;
 import play.libs.MimeTypes;
 import play.mvc.Http.Request;
 import play.mvc.Scope.Session;
 import play.mvc.Util;
 import play.templates.JavaExtensions;
 import query.GlobeResult;
 import query.QueryHelper;
 import query.UsageFilter;
 import util.GuessContentType;
 import util.JsonHelper;
 import util.Utils;
 import exception.QuickException;
 
 
 /**
  * Controller that handles data download and upload 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class Data extends CommonController {
 
 	/** 
 	 * characters that have to be used into command line and file names
 	 */
 	public static final char[] COMMANDLINE_INVALID_CHARS = { ';','&','`',':','*','?','$','<','>','|' };
 	
 	/**
 	 * Keep the session up
 	 */
 	public static void heartbeat() {
 		/*
 		 * touch the user data path to prevent to make it evicted 
 		 */
 		File file =getUserTempPath(false);
 		if( file != null && file.exists() ) {
 			long now = System.currentTimeMillis();
 			file.setLastModified(now);
 			file.getParentFile().setLastModified(now); // <-- also the parent folder is update to reflect the change 
 		}
 	
 		// return a confirmation 
 		renderText("OK");
 	} 
 	
 	/**
 	 * This method let to download any file placed in the application 
 	 * data folder 
 	 * 
 	 * @param path
 	 */
 	@Deprecated
 	public static void resource(String path) {
 		assertNotEmpty(path, "Missing 'path' argument on #resource action");
 
 		renderStaticResponse();
 		String content = MimeTypes.getMimeType(path);
 		response.contentType = content;
 		renderFile(AppProps.WORKSPACE_FOLDER, path);
 	}
 	
 	/**
 	 * Download a user file available in the current session 
 	 * 
 	 * @param name the request file name (without any path specification, the file must exists in the user area)
 	 */
 	public static void view( String name ) { 
 		assertNotEmpty(name, "Missing 'name' argument on #view action");
 
 		File file = getUserFile(name);
 		if( !file.exists() ) { 
 			Logger.error("Error opening '%s'. File does not exist", name);
 			notFound("File not found: '%s'", file.getName());
 		}
 
 		renderStaticResponse();
 		String content = MimeTypes.getMimeType(name);
 		response.contentType = content;
 		renderBinary(file);
 	}
 	
 	public static void preview( String name ) throws IOException { 
 
 		File file = getUserFile(name);
 		if( !file.exists() ) { 
 			Logger.error("Error opening '%s'. File does not exist", name);
 			notFound("File not found: '%s'", name);
 		}
 
 		GuessContentType ctype = null;
 		try { 
 			ctype = new GuessContentType(file);
 		}
 		catch( Exception e ) {
 			Logger.error(e, "Cannot guess file type for: '%s'", file);
 		}
 
 		if( ctype == null || ctype.isBinary() ) { 
 			renderText("(preview not available)");
 		}
 		
 	
 		/*
 		 * render only the first 10K of text
 		 */
 		response.contentType = ctype.getMimeType();
 		byte[] buffer = new byte[10 * 1024];
 		FileInputStream in = new FileInputStream(file);
 		in.read(buffer);
 		
 		renderBinary(new ByteArrayInputStream(buffer));
 	}
 	
 	/**
 	 * Create a temporary zip file with all generated content and download it
 	 * 
 	 * @param rid the request identifier
 	 * @throws IOException 
 	 */
 	public static void zip( String rid ) throws IOException {
 		assertNotEmpty(rid, "Missing 'rid' argument on #zip action");
 		
 		Repo repo = new Repo(rid);
 		if( !repo.hasResult() ) {
 			notFound(String.format("The requested download is not available (%s) ", rid));
 			return;
 		}
 		
 		OutResult result = repo.getResult();
		File zip = File.createTempFile("all-files-", ".zip", getTempZipFolder());
 		// get the list of files to download 
 		List<File> files = new ArrayList<File>( result.getItems().size() );
 		for( OutItem item : result.getItems() ) { 
 			files.add(item.file);
 		}
 		// zip them and download
 		zipThemAll(files, zip, null);
 
 		String attachName = String.format("tcoffee-all-files-%s.zip",rid);
 		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachName+ "\"");
 		renderStaticResponse();
 		renderBinary(zip);
 	}


	/**
	 * The folder where store the temporary zip files 
	 */
	static File ZIP_FOLDER = new File( AppProps.TEMP_PATH, "zip-results" ); 

	@Util
	static File getTempZipFolder() {
		// check if the folder exists 
		if( !ZIP_FOLDER.exists() && !ZIP_FOLDER.mkdirs() ) {
			throw new QuickException("Cannot create temporary ZIP foler: '%s'", ZIP_FOLDER);
		}

		return ZIP_FOLDER;
	} 
 	
 	/**
 	 * Handy method to zip all datafolder content and download it
 	 * 
 	 * @param rid request identifier
 	 */
	
 	public static void zipDataFolder( String rid ) throws IOException { 
 		assertNotEmpty(rid, "Missing 'rid' argument on #zipDataFolder action");
		
 		File folder = new File(AppProps.instance().getDataPath(), rid);
 		if( !folder.exists() ) { 
 			notFound("Data path '%s' does not exist on the server", folder);
 		}
 		
 		Collection allFiles = FileUtils.listFiles(folder, null, true);
		File zip = File.createTempFile("result-", ".zip", getTempZipFolder() );
 		
 		String parent = folder.getAbsolutePath();
 		if( !parent.endsWith("/")) { 
 			parent += "/";
 		}
 		zipThemAll(allFiles, zip, parent);
 		
 		String attachName = String.format("all-data-files-%s.zip",rid);
 		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachName+ "\"");
 		renderStaticResponse();
 		renderBinary(zip);
 		
 	}
 
 	/**
 	 * Zip the collections of files as a unique zip file
 	 * 
 	 * @param items a collections of files to be zipped 
 	 * @param targetZip the target zip file
 	 * @param basePath if <code>null</code> all files are zipped in a plain archive (only file name is used) 
 	 * otherwise this string value will be considered the prefix of the files absulte path to be removed 
 	 */
 	static void zipThemAll( Collection<File> items, File targetZip, String basePath ) {
 
 		try {
 			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(targetZip));
 			
 			for( File item : items ) { 
 				if( item==null || !item.exists() ) { continue; }
 				
 				// add a new zip entry
 				String entryName; 
 				if( basePath == null ) { 
 					/* use just the file name as entry name (w/o path information) */
 					entryName = item.getName();
 				}
 				else { 
 					/* make relative to the basePath */
 					entryName = item.getAbsolutePath();
 					if( entryName.startsWith(basePath)) { 
 						entryName = entryName.substring(basePath.length());
 					}
 				}
 				
 				
 				zip.putNextEntry( new ZipEntry(entryName) );
 				
 				// append the file content
 				FileInputStream in = new FileInputStream(item);
 				IO.copy(in, zip);
 	 
 				// Complete the entry 
 				zip.closeEntry(); 
 			}
 			
 			zip.close();					
 		}
 		catch (IOException e) {
 			throw new QuickException(e, "Unable to zip content to file: '%s'", targetZip);
 		}
 	} 
 	
 	
 	/* 
 	 * User data (for example uploads) is store under the main TEMPORARY folder 
 	 * and organized in subfolder using the session ID
 	 */
 	static File USERDATA = new File( AppProps.TEMP_PATH, "userdata");
 
 	
 	@Util
 	public static File getUserTempPath(boolean create) { 
 		File file = new File(USERDATA, Session.current().getId());
 		if( !file.exists() && create ) { 
 			if( !file.mkdirs() ) { 
 				throw new QuickException("Cannot create User temporary path: '%s'", file);
 			}
 			Logger.info("Creating user dataspace: '%s'", file);
 		}
 		return file;
 	}
 
 	@Util
 	public static File getUserTempPath() { 
 		return getUserTempPath(true);
 	}
 	
 	/*
 	 * Create a new 'File' under the user temporay folder 
 	 */
 	@Util
 	public static File newUserFile( String fileName ) { 
 		File result = new File( getUserTempPath(), fileName);
 		return normalize(result);
 	}
 
 	/*
 	 * Retuns the list of 'temporary' files for the current user 
 	 */
 	@Util
 	public static List<File> getUserFiles() { 
 		File[] files = getUserTempPath().listFiles();
 		return (List<File>) (files != null ? Arrays.asList(files) : Collections.emptyList());
 	}
 	
 	/*
 	 * Retrieve a File for the specified file name . 
 	 * 
 	 * Path are always relative to the current user path 
 	 */
 	@Util 
 	public static File getUserFile( String name ) {
 		return new File( getUserTempPath(), name );
 	} 
 	
 	/**
 	 * Handle ajax upload 
 	 * 
 	 * This method is design to work with the ajax upload component provided by 'fileuploader.js'
 	 * See https://github.com/valums/file-uploader
 	 * 
 	 * @param qqfile
 	 */
 	public static void ajaxupload(String qqfile) { 
 
 		response.contentType = "text/html";  // <!-- also this reponse header is requried to make it work IE 
 		
 		/* 
 		 * Hack to handle upload from fucking IE7/8
 		 */
 		Upload __fileUpload = null;
 		if( "multipart/form-data".equals(request.contentType)) { 
 			List<Upload> __uploads = (List<Upload>) Request.current().args.get("__UPLOADS");
 			if( __uploads != null && __uploads.size()>0) { 
 				__fileUpload = __uploads.get(0);
 				qqfile = __fileUpload.getFileName();
 			}
 		}
 
 		
 		/* 
 		 * some integrity checks
 		 */
 		if( Utils.isEmpty(qqfile) ) { 
 			renderText(JsonHelper.error("The file name cannot be empty"));
 		}
 		
 		File newFile = null;
 		try  {
 			newFile = newUserFile(qqfile);
 			OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
 			InputStream input = __fileUpload == null 
 							  ? request.body
 							  : __fileUpload.asStream();
 
 			IO.write(new BufferedInputStream(input), out);
 			// ^ Stream closed by the write method
 			
 			String result = String.format(
 					"{\"success\":true, " +
 					"\"path\": \"%s\"," +
 					"\"name\": \"%s\"," +
 					"\"size\": \"%s\" }", 
 					JavaExtensions.escapeJavaScript(newFile.getAbsolutePath()),
 					newFile.getName(),
 					JavaExtensions.formatSize(newFile.length()));
 			renderText(result);
 		}
 		catch( Exception e ) { 
 			Logger.error(e, "Unable to store ajax file upload to: '%s'; file name: '%s'", newFile, qqfile);
 			renderText( JsonHelper.error(e) );
 		}
 
 	}
 	
 	/**
 	 * Normalized the file name using the following rules
 	 * - if a name starts with a '-' it is replace by '_'
 	 * - if a name contains blanks, are replaced by '_' 
 	 * 
 	 * @param file the source file location 
 	 * @return the normalized file name 
 	 */
 	@Util
 	public static File normalize(File file) {
 		String name = file.getName();
 		// replace 'hyphen' as first char 
 		name = name.replaceAll("^-", "_");
 		// remove all accented latin chars
 		name = Normalizer.normalize(name, Normalizer.Form.NFD) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
 		// we accept only alphatic chars, digits and few other characters (-=+ 
 		name = name.replaceAll("[^A-Za-z0-9\\.@\\-=+~]", "_");
 		
 		File parent = file.getParentFile();
 		return parent != null ? new File(parent,name) : new File(name);
 	} 
 	
 	/**
 	 * Render the JSON formatted data for the globe stats visualization 
 	 * 
 	 * @param filter
 	 * @param qtype 
 	 * @param query 
 	 */
 	public static void statsGlobe(UsageFilter filter, String qtype, String query ){
 		GlobeResult data = QueryHelper.findGlobeStat(filter, qtype, query);
 		
 		StringBuilder result = new StringBuilder("[");
 		int c=0;
 		for( Object[] fields : data.items ) {
 			
 			/*
 			 * long and lat 
 			 */
 			result .append( fields[0] ) .append(",") .append( fields[1] ) .append(",");
 
 			/*
 			 * magnitude 1-normalized
 			 */
 			double magnitude = (fields[2] instanceof BigInteger) ? ((BigInteger)fields[2]).doubleValue() : 0;
 			if( data.max>0 ) {
 				magnitude = magnitude / data.max; 
 			}
 			result.append( magnitude ).append(",");
 			
 			/*
 			 * color 0..15
 			 */
 			result.append( Math.round(magnitude * 15) );
 			
 			if( ++c != data.items.size()) {
 				result.append(",");
 			}
 			result.append("\n");
 		} 
 		result.append("]");
 		
 
 		renderJSON( result.toString() );
 	} 	
 
 }
