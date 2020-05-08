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
 	
 	public static void view( String path ) { 
 		assertNotEmpty(path, "Missing 'path' argument on #view action");
 
 		if( path.startsWith("file://") ) { 
 			path = path.substring(7);
 		}
 		
 		File file = new File(path);
 		if( !file.exists() ) { 
 			Logger.error("Error opening '%s'. File does not exist", path);
 			notFound("File not found: %s", file.getName());
 		}
 
 		renderStaticResponse();
 		String content = MimeTypes.getMimeType(path);
 		response.contentType = content;
 		renderBinary(file);
 	}
 	
 	public static void preview( String path ) throws IOException { 
 
 		if( path.startsWith("file://") ) { 
 			path = path.substring(7);
 		}
 
 		File file = new File(path);
 		if( !file.exists() ) { 
 			Logger.error("Error opening '%s'. File does not exist", path);
 			notFound("File not found: %s", file.getName());
 		}
 
		GuessContentType ctype = new GuessContentType(file);
 
		if( ctype.isBinary() ) { 
 			renderText("(this file cannot be previewed)");
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
 		File zip = File.createTempFile("download", ".zip", repo.getFile());
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
 		File zip = File.createTempFile("folder", ".zip");
 		
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
 	public static File getUserTempPath() { 
 		File file = new File(USERDATA, Session.current().getId());
 		if( !file.exists() && !file.mkdirs()) { 
 			throw new QuickException("Cannot create User temporary path: '%s'", file);
 		}
 		return file;
 	}
 	
 	/*
 	 * Create a new 'File' under the user temporay folder 
 	 */
 	@Util
 	public static File newUserFile( String fileName ) { 
 		File result = new File( getUserTempPath(), fileName);
 		return result;
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
 		
 		if( qqfile.startsWith("-") ) { 
 			renderText(JsonHelper.error("The file name cannot start with a minus (-) character."));
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
 	
 
 }
