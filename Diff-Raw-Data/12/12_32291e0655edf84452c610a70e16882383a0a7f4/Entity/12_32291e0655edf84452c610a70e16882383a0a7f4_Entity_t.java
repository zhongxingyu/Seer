 package br.com.developer.redu.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
 
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
 
 
 public class Entity {
 	private MultipartEntity entity;
 	
 	public Entity() {
 		this.entity = new MultipartEntity();
 	}
 
 	public void addPart(String key, String stringBody) throws UnsupportedEncodingException {
		this.entity.addPart(key, new StringBody(stringBody, Charset.forName(HTTP.UTF_8)));
 	}
 
 	public String getContentType() {
 		return this.entity.getContentType().getValue();
 	}
 
 	public void addPart(String key, File file, String mimetype) {
 		FileBody fileBody = new FileBody(file, mimetype);
 		this.entity.addPart(key, fileBody);
 	}
 
 	public byte[] toByteArray() throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		this.entity.writeTo(out);
 		return out.toByteArray();
 	}
 
 }
