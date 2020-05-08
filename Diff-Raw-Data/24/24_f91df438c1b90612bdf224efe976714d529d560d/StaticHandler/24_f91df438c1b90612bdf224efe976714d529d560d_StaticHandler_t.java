 package com.tantaman.ferox.route_middelware;
 
 import io.netty.channel.ChannelFutureListener;
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpResponseStatus;
 import io.netty.handler.codec.http.LastHttpContent;
 import io.netty.handler.stream.ChunkedFile;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.URLDecoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.activation.MimetypesFileTypeMap;
 
 import com.tantaman.ferox.api.request_response.IHttpContent;
 import com.tantaman.ferox.api.request_response.IRequestChainer;
 import com.tantaman.ferox.api.request_response.IResponse;
 import com.tantaman.ferox.api.router.RouteHandlerAdapter;
 
 public class StaticHandler extends RouteHandlerAdapter {
 	private final String path;
 	private final String rootVerifier;
	public static final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
	
	static {
		mimeTypesMap.addMimeTypes("text/html html HTML htm HTM");
		mimeTypesMap.addMimeTypes("txt/css css CSS");
		mimeTypesMap.addMimeTypes("text/plain txt text TXT");
		mimeTypesMap.addMimeTypes("application/json json JSON");
	}
 	
 	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
     public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
     public static final int HTTP_CACHE_SECONDS = 60;
 	
 	public StaticHandler(String fsPath) {
 		this.path = fsPath;
 		rootVerifier = new File(path).getAbsolutePath();
 	}
 	
 	@Override
 	public void lastContent(IHttpContent content, IResponse response,
 			IRequestChainer next) {
 		try {
 			sendFile(content, response, next);
 		} catch (ParseException | IOException e) {
 			response.send("Error", HttpResponseStatus.INTERNAL_SERVER_ERROR);
 			content.dispose();
 		}
 	}
 	
 	public void sendFile(IHttpContent content, IResponse response,
 			IRequestChainer next) throws ParseException, IOException {
 		String path = URLDecoder.decode(content.getCatchall(), "UTF-8");
 
        File file = new File(this.path + "/" + path);
         
         if (!file.getAbsolutePath().startsWith(rootVerifier)) {
         	sendForbidden(response);
         	return;
         }
         
         if (file.isHidden() || !file.exists()) {
             sendNotFound(response);
             return;
         }
 
         if (!file.isFile()) {
             sendForbidden(response);
             return;
         }
 
         // Cache Validation
         String ifModifiedSince = content.getHeaders().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
         if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
             SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
             Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
 
             // Only compare up to the second because the datetime format we send to the client
             // does not have milliseconds
             long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
             long fileLastModifiedSeconds = file.lastModified() / 1000;
             if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                 sendNotModified(response);
                 return;
             }
         }
 
         RandomAccessFile raf;
         try {
             raf = new RandomAccessFile(file, "r");
         } catch (FileNotFoundException fnfe) {
             sendNotFound(response);
             return;
         }
         
         long fileLength = raf.length();
 
         response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fileLength);
         response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "");
         setContentTypeHeader(response, file);
         setDateAndCacheHeaders(response, file);
 
     
         response.fineGrained().addResponseHeaders();
         response.fineGrained().add(new ChunkedFile(raf, 0, fileLength, 8192));
         response.fineGrained().add(LastHttpContent.EMPTY_LAST_CONTENT);
 
         response.fineGrained().write();
         content.dispose();
 	}
 	
 	// TODO: custom error pages
 	private void sendForbidden(IResponse response) {
 		response.send("Access Denied", HttpResponseStatus.FORBIDDEN);
 	}
 	
 	private void sendNotFound(IResponse response) {
 		response.send("Not found", HttpResponseStatus.NOT_FOUND);
 	}
 	
 	private static void sendNotModified(IResponse response) {
         setDateHeader(response);
 
         response.fineGrained().addResponseHeaders(HttpResponseStatus.NOT_MODIFIED);
         response.fineGrained().add(LastHttpContent.EMPTY_LAST_CONTENT);
         response.fineGrained().writePartial().addListener(ChannelFutureListener.CLOSE);
     }
 	
 	private static void setContentTypeHeader(IResponse response, File file) {
        
         response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
     }
 	
 	private static void setDateHeader(IResponse response) {
         SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
         dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
 
         Calendar time = new GregorianCalendar();
         response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
     }
 	
 	private static void setDateAndCacheHeaders(IResponse response, File fileToCache) {
         SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
         dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
 
         // Date header
         Calendar time = new GregorianCalendar();
         response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
 
         // Add cache headers
         time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
         response.headers().set(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
         response.headers().set(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
         response.headers().set(
                 HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
     }
 }
