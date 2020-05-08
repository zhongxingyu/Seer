 package utils.minifymod;
 
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ETAG;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import play.Logger;
 import play.Play;
 import play.Play.Mode;
 import play.PlayPlugin;
 import play.cache.Cache;
 import play.libs.MimeTypes;
 import play.mvc.Http;
 import play.utils.Utils;
 import play.vfs.VirtualFile;
 import controllers.minifymod.Compression;
 
 /**
  * Based on https://gist.github.com/2882360 which seems to be based on minifymod :)
  */
 public class StaticGzipPlugin extends PlayPlugin {
 
 	private static boolean gzipDisabled = false;
 
 	private static boolean minifyDisabled = false;
 
 	private static boolean memcacheDisabled = false;
 
 	static {
 		String prop = Play.configuration.getProperty("minifymod.statics.gzip.disabled", "false");
 		gzipDisabled = Boolean.parseBoolean(prop);
 		prop = Play.configuration.getProperty("minifymod.statics.minify.disabled", "false");
 		minifyDisabled = Boolean.parseBoolean(prop);
 		prop = Play.configuration.getProperty("minifymod.statics.memcache.disabled", "false");
 		memcacheDisabled = Boolean.parseBoolean(prop);
 	}
 
 
 	@Override
 	public boolean serveStatic(VirtualFile file, Http.Request request, Http.Response response) {
 		try {
 			final File localFile = file.getRealFile();
 			String contentType = MimeTypes.getContentType(localFile.getName(), "text/plain");
 			if (contentType.contains("image")) {
 				return false; // You don't want to minify or gzip images
 			}
 			response.setContentTypeIfNotSet(contentType);
 			response = addEtag(request, response, localFile);
 			// minify / cache on prod
 			String content = null;
 			if (memcacheDisabled) { // don't look in cache is disabled
 				content = minify(request, response, localFile);
 			} else {
 				String key = request.path + localFile.getAbsolutePath();
 				Cache.get(key, String.class);
 				if (content == null) {
 					content = minify(request, response, localFile);
 					if (Play.mode == Mode.PROD) {
 						Cache.set(key, content, "24h");
 					}
 				}
 			}
 
 			// gzip only if supported and not excluded or disabled
 			if (Compression.isGzipSupported(request) && !Compression.isExcludedAction(request)
 					&& !gzipDisabled) {
 				final ByteArrayOutputStream gzip = Compression.getGzipStream(content);
 				// set response header
 				response.setHeader("Content-Encoding", "gzip");
 				response.setHeader("Content-Length", gzip.size() + "");
 				response.out = gzip;
 				return true;
 			} else {
 				response.out = new ByteArrayOutputStream(content.length());
 				response.out.write(content.getBytes());
 				return true;
 			}
 		} catch (Exception e) {
 			Logger.error(e, "Error when Gzipping response: %s", e.getMessage());
 
 		}
 		return false;
 	}
 
 
 	private String minify(Http.Request request, Http.Response response, File file)
 			throws IOException {
 		boolean minified = file.getName().contains(".min.");
 		String content = VirtualFile.open(file).contentAsString();
 		if (minified || minifyDisabled) {
 			return content;
 		} else if (!Compression.isExcludedAction(request)) {
 			// select compression method by contentType
 			if (response.contentType.contains("text/html")) {
 				return Compression.compressHTML(content);
 			} else if (response.contentType.contains("text/xml")) {
 				return Compression.compressXML(content);
 			} else if (response.contentType.contains("text/css")) {
 				return Compression.compressCSS(content);
 			} else if (response.contentType.contains("text/less")) {
 				return Compression.compressCSS(content);
 			} else if (response.contentType.contains("text/javascript")
 					|| response.contentType.contains("application/javascript")) {
 				return Compression.compressJS(content);
 			}
 		}
 		return content;
 	}
 
 
 	private static Http.Response addEtag(Http.Request request, Http.Response response, File file) {
 		if (Play.mode == Play.Mode.DEV) {
 			response.setHeader(CACHE_CONTROL, "no-cache");
 		} else {
 			String maxAge = Play.configuration.getProperty("http.cacheControl", "3600");
 			if (maxAge.equals("0")) {
 				response.setHeader(CACHE_CONTROL, "no-cache");
 			} else {
 				response.setHeader(CACHE_CONTROL, "max-age=" + maxAge);
 			}
 		}
 		boolean useEtag = Play.configuration.getProperty("http.useETag", "true").equals("true");
		long last = file.lastModified();
 		final String etag = "\"" + last + "-" + file.hashCode() + "\"";
 		if (!request.isModified(etag, last)) {
 			if (request.method.equals("GET")) {
 				response.status = Http.StatusCode.NOT_MODIFIED;
 			}
 			if (useEtag) {
 				response.setHeader(ETAG, etag);
 			}
 		} else {
 			response.setHeader(LAST_MODIFIED, Utils.getHttpDateFormatter().format(new Date(last)));
 			if (useEtag) {
 				response.setHeader(ETAG, etag);
 			}
 		}
 		return response;
 	}
 
 }
