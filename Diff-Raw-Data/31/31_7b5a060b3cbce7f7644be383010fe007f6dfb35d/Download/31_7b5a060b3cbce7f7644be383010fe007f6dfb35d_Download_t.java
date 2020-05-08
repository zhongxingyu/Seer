 package controllers;
 
 import com.openseedbox.Config;
 import com.openseedbox.code.Util;
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import play.Logger;
 import play.libs.MimeTypes;
 
 public class Download extends BaseController {
 
 	public static void downloadFile(String hash, String name, String type, String debug) {
 		if (type != null && type.equals("zip")) {
 			downloadFileZip(hash, name);
 			return;
 		}
 		String location = Config.getTorrentsCompletePath();
 		if (!(new File(location).exists())) {
 			notFound(String.format("File %s doesnt exist, it appears the download link was constructed incorrectly.", location));
 		}
 		if (Config.isXSendfileEnabled()) {
 			location = Config.getXSendfilePath();
 		}
		String filePath = String.format("%s/%s/%s", location, hash, name);		
 		if (Config.isXSendfileEnabled()) {
 			String fileName = new File(filePath).getName();				
			response.setHeader("X-Accel-Charset", "utf-8");			
			if (StringUtils.isEmpty(debug)) {
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				response.setContentTypeIfNotSet(MimeTypes.getContentType(new File(filePath).getAbsolutePath()));
				response.setHeader(Config.getXSendfileHeader(), filePath);
			} else {
				renderText("FilePath: " + filePath);
			}
 		} else {
 			File f = new File(filePath);
 			renderBinary(f, Util.URLDecode(f.getName()));
 		}
 	}
 
 	protected static void downloadFileZip(String hash, String name) {
 		if (!Config.isNgxZipEnabled()) {
 			renderText("Error: Zip files are not enabled.");
 		}
 		if (StringUtils.isEmpty(hash)) {
 			notFound();
 		}
 		File baseDirectory = new File(Config.getTorrentsCompletePath(), hash);
 		if (!baseDirectory.exists()) {
 			notFound();
 		}
 		if (name == null) {
 			name = hash;
 		}		
 		if (!Config.isNgxZipManifestOnly()) {
 			response.setHeader("X-Archive-Files", "zip");
 			response.setHeader("Content-Disposition", "attachment; filename=\"" + name + ".zip" + "\"");
 		}		
 		response.setHeader("Last-Modified", Util.getLastModifiedHeader(baseDirectory.lastModified())); //this is so nginx mod_zip will resume the zip file
 		String nginxPath = String.format("%s/%s/", Config.getNgxZipPath(), hash);
 		renderText(generateNgxZipFile(baseDirectory, nginxPath, "/", ""));	
 	}
 	
 	private static String generateNgxZipFile(File baseDirectory, String nginxPath, String prefix, String all) {		
 		for (File f : baseDirectory.listFiles()) {
 			if (!prefix.endsWith("/")) {
 				prefix += "/";
 			}			
 			if (f.isDirectory()) {
 				all = generateNgxZipFile(f, nginxPath, prefix + f.getName(), all);
 				continue;
 			}					
 			String name = prefix + f.getName();
 			String path = nginxPath + name;
 			String crc32 = "-";
 			try {
 				crc32 = Long.toHexString(FileUtils.checksumCRC32(f)).toUpperCase();
 			} catch (IOException ex) {
 				Logger.warn(ex, "Unable to generate CRC32 for file: %s", path);
 			}
 			all = addNgxZipEntry(crc32, path, name, f.length(), all);
 		}
 		return all;
 	}
 	
 	private static String addNgxZipEntry(String crc32, String path, String name, long sizeBytes, String all) {
 		return all + String.format("%s %s %s %s\n", crc32, sizeBytes, Util.URLEncode(path), name);		
 	}
 }
