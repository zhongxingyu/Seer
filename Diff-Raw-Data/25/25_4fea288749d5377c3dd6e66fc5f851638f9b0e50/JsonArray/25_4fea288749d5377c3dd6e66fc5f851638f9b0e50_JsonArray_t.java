 package org.getcomposer.core.collection;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 
 import org.getcomposer.core.entities.AbstractJsonArray;
 
 public class JsonArray extends AbstractJsonArray<Object> {
 
 	public JsonArray() {
 	}
 	
 	public JsonArray(Object json) {
 		fromJson(json);
 	}
 	
 	public JsonArray(String json) {
 		fromJson(json);
 	}
 	
 	public JsonArray(File file) throws IOException {
 		fromJson(file);
 	}
 	
 	public JsonArray(Reader reader) throws IOException {
 		fromJson(reader);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		return values.equals(obj);
 	}
 }
