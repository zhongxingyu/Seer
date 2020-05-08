 package com.tort.trade.journals;
 
 import com.google.gson.Gson;
 
 public class JsonView<Model> {
	public byte[] render(Model objects) {
 		return new Gson().toJson(objects).getBytes();
 	}
 	
 }
