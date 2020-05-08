 package com.tort.trade.journals;
 
import java.util.Collection;

 import com.google.gson.Gson;
 
 public class JsonView<Model> {
	public byte[] render(Collection<Model> objects) {
 		return new Gson().toJson(objects).getBytes();
 	}
 	
 }
