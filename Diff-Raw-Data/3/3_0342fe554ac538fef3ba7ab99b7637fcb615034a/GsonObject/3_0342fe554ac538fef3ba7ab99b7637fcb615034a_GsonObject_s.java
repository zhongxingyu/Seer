 /*
  * Copyright (c) 2011 Patrick Pollet France
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
  * associated documentation files (the "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
  * following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial
  * portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
  * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
  * USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  * Contributor(s): 
  */
 package net.patrickpollet.gson;
 
 import java.lang.reflect.Field;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class GsonObject {
 	
 	public GsonObject fromJSON (String jString) {		
 		GsonBuilder gsonb=new GsonBuilder();
 		//added for real booelan returned by some WS 
		gsonb.registerTypeAdapter(Boolean.class, new JsonBooleanDeserializer());
 		//gsonb.registerTypeAdapter(boolean.class, new JsonBooleanDeserializer());
 		Gson gson=gsonb.create();
 		return gson.fromJson(jString,this.getClass());
 	}
 	
 	public String toJSON () {
 		GsonBuilder gsonb=new GsonBuilder();
 		Gson gson=gsonb.create();
 		return gson.toJson(this);
 	}
 	
 	
 	/**
 	 * mostly used as a debugging method to dump data returned by the WS
 	 */
 	public String toString() {
 		
 		StringBuilder sb=new StringBuilder("{");
 		Field[] fields=this.getClass().getDeclaredFields();
 		boolean first=true;
 		for (Field field :fields){
 			try {
 				field.setAccessible(true); //MUST DO 
 				Object value=field.get(this);
 				if (!first)
 					sb.append(',');
 				if (value !=null) 
 					sb.append(field.getName()).append(" = ").append(value.toString());
 				else 
 					sb.append(field.getName()).append(" = null");
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 			first=false;
 		}
 		
 		//getFieldValue
 		sb.append("}");
 		return sb.toString();
 	}
 	
 	
 
 }
