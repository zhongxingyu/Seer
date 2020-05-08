 package com.arcao.wherigoservice.datamapper;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.stream.JsonWriter;
 
 public class JsonResponseDataMapper implements ResponseDataMapper {
 	protected JsonWriter writer;
 	protected OutputStream os;
 	
 	public JsonResponseDataMapper(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		writer = getJsonWriter(req, resp);
 		os = resp.getOutputStream();
 	}
 	
 	protected static JsonWriter getJsonWriter(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		try {
 			JsonWriter w = new JsonWriter(new OutputStreamWriter(resp.getOutputStream(), "UTF-8"));
 			
 			if (req.getParameter("debug") != null)
 				w.setIndent("    ");
 			
 			return w;
 		} catch (UnsupportedEncodingException e) {
 			throw new IOException(e);
 		}
 	}
 	
 	protected void writeResponseCode(ResponseCode r, Object... responseTextParam) throws IOException {
		writer.name("Status")
 			.beginObject()
 				.name("Code").value(r.getResponseCode())
 				.name("Text").value(String.format(r.getResponseText(), responseTextParam))
 			.endObject();
 	}
 	
 	public void writeErrorResponse(ResponseCode r, Object... responseTextParam) throws IOException {
 		writer.beginObject();
 		writeResponseCode(r, responseTextParam);
 		writer.endObject();
 	}
 	
 	public void writeLoginResponse(String session) throws IOException {
 		writer.beginObject();
 		writeResponseCode(ResponseCode.Ok);
 		
 		writer.name("LoginResult")
 			.beginObject()
 				.name("Session").value(session)
 			.endObject();
 		
 		writer.endObject();
 	}
 	
 	public void writeGetCartridgeResponse(String cartridgeGuid) throws IOException {
 		writer.beginObject();
 		writeResponseCode(ResponseCode.Ok);
 		
 		writer.name("CartridgeResult")
 			.beginObject()
 				.name("CartridgeGUID").value(cartridgeGuid)
 			.endObject();
 		
 		writer.endObject();				
 	}
 	
 	public void writeGetCartridgeDownloadDataResponse(Map<String, String> formData, String session) throws IOException {
 		writer.beginObject();
 		writeResponseCode(ResponseCode.Ok);
 		
 		writer.name("CartridgeDownloadDataResult")
 			.beginObject()
 				.name("FormData");
 			
 			writer.beginObject();
 			for (Entry<String, String> entry : formData.entrySet()) {
 				writer.name(entry.getKey()).value(entry.getValue());
 			}
 			writer.endObject();
 			
 			writer.name("Session").value(session);
 			writer.endObject();
 		
 		writer.endObject();
 	}
 	
 	public void writeGetCacheCodeFromGuidResponse(String cacheCode) throws IOException {
 		writer.beginObject();
 		writeResponseCode(ResponseCode.Ok);
 		
 		writer.name("CacheResult")
 			.beginObject()
 				.name("CacheCode").value(cacheCode)
 			.endObject();
 		
 		writer.endObject();						
 	}
 	
 	public void flush() throws IOException {
 		writer.flush();
 	}
 }
