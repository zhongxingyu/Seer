 package com.gentics.cr.rest.php;
 /*
  * PHPSerializer.java
  *
  * Author : Ludovic Martin <ludovic DOT martin AT laposte DOT net>
  * This code is free for any use and modification but comes without any warranty.
  */
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * PHP data serializer and unserializer
  * @author Ludovic Martin <ludovic.martin@laposte.net>
  * @author Haymo Meran <h.meran@gentics.com>
  *
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class PHPSerializer{
     
     /** Data charset */
     private String charset;
     /** Private string pointer */
     private int position=0;
     
     /** Creates a new instance of PHPSerializer
      */
     public PHPSerializer(){
         this("utf-8");
     }
     
     /** Creates a new instance of PHPSerializer
      * @param charset charset of target PHP page
      */
     public PHPSerializer(String charset){
         this.charset=charset;
     }
     
     /** Serialize to PHP format into a string
      * @param object java object to serialize
      * @return serialized object
      * @throws UnsupportedEncodingException 
      */
     public String serialize(Object object)throws UnsupportedEncodingException{
         StringBuffer output=new StringBuffer();
         serialize(object,output);
         return output.toString();
     }
     
     /** Serialize to PHP format into a string
      * @param object java object to serialize
      * @param output
      * @throws UnsupportedEncodingException 
      */
     public void serialize(Object object,StringBuffer output)throws UnsupportedEncodingException{
         doSerialize(object,new StringBufferSDW(output));
     }
 
     /** Serialize to PHP format into a stream
      * @param object java object to serialize
      * @param output stream for output
      * @throws UnsupportedEncodingException 
      */
     public void serialize(Object object,PrintStream output)throws UnsupportedEncodingException{
         doSerialize(object,new PrintStreamSDW(output));
     }
 
 
     private void doSerialize(double object,SerializedDataWrite result)throws UnsupportedEncodingException{
     	doSerialize(new Double(object).toString(), result);
     }
 
     private void doSerialize(long object,SerializedDataWrite result)throws UnsupportedEncodingException{
     	doSerialize(new Long(object).toString(), result);
     }
 
     private void doSerialize(int object,SerializedDataWrite result)throws UnsupportedEncodingException{
     	doSerialize(new Integer(object).toString(), result);
     }
 
     	/** Serialize to PHP format into a string
      * @param object object to serialize
      * @param result output buffer for PHP format
      */
     @SuppressWarnings("unchecked")
 	private void doSerialize(Object object,SerializedDataWrite result)throws UnsupportedEncodingException{
     	if(object==null){//Null
             result.put("N;");
         }else if(object instanceof Boolean){//Boolean
             result.put("b:");
             result.put((object instanceof Boolean)?"1":"0");
             result.put(";");
         }else if(object instanceof Integer||object instanceof Long||object instanceof Byte||object instanceof Short){//Integer, Long, Byte, Short
             result.put("i:");
             result.put(object.toString());
             result.put(";");
         }else if(object instanceof Double||object instanceof Float){//Double, Float
             result.put("d:");
             result.put(object.toString());
             result.put(";");
         }else if(object instanceof Map){//Map
             result.put("a:");
             int size = ((Map<Object,Object>)object).size();
             result.put(size);
             result.put(":{");
             Iterator<Object> it = ((Map<Object,Object>)object).keySet().iterator();
 			while (it.hasNext()) {
 				Object item = it.next();
                 doSerialize(item,result);
                 doSerialize(((Map<Object,Object>)object).get(item),result);
             }
             result.put("}");
         }else if(object instanceof List){//List
             result.put("a:");
             int size = ((List<Object>)object).size();
             result.put((new Integer(size).toString()));
             result.put(":{");
             int i=0;
             Iterator<Object> it = ((List<Object>)object).iterator();
 			while (it.hasNext()) {
 				Object item = it.next();
                 doSerialize(i++,result);
                 doSerialize(item,result);
             }
             result.put("}");
         }else if(object instanceof Object[]){//Object[]
             result.put("a:");
             result.put(((Object[])object).length);
             result.put(":{");
             Object[] o = (Object[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof Boolean[] || object instanceof boolean[]){//boolean[]
             result.put("a:");
             result.put(((Boolean[])object).length);
             result.put(":{");
             Boolean[] o = (Boolean[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof int[]){//int[]
             result.put("a:");
             result.put(((int[])object).length);
             result.put(":{");
             int[] o = (int[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof long[]){//long[]
             result.put("a:");
             result.put(((long[])object).length);
             result.put(":{");
             long[] o = (long[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof byte[]){//byte[]
             result.put("a:");
             result.put(((byte[])object).length);
             result.put(":{");
             byte[] o = (byte[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof short[]){//short[]
             result.put("a:");
             result.put(((short[])object).length);
             result.put(":{");
             short[] o = (short[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof double[]){//double[]
             result.put("a:");
             result.put(((double[])object).length);
             result.put(":{");
             double[] o = (double[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof float[]){//float[]
             result.put("a:");
             result.put(((float[])object).length);
             result.put(":{");
             float[] o = (float[])object;
 			for (int i = 0; i < o.length; i++) {
                doSerialize(i,result);
                 doSerialize(o[i],result);
             }
             result.put("}");
         }else if(object instanceof PHPSerializable){//PHPSerializable
             result.put(((PHPSerializable)object).phpSerialize());
         }else{//String or any thing else
         	String objectAsString="";
         	if(object.getClass()==String.class)
         	{
         		objectAsString=(String)object;
         	}
         	else
         	{
         		try {
 					objectAsString = new String(getBytes(object),charset);
 					
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         	}
             result.put("s:");
             result.put(objectAsString.getBytes(charset).length);
             result.put(":\"");
             result.put(objectAsString);
             result.put("\";");
 
         }
     }
     
     /** Unserialize from PHP data string
      * @param str PHP serialized data string
      * @return unserialized object
      * @throws BadFormatException 
      * @throws UnsupportedEncodingException 
      */
     public Object unserialize(String str)throws BadFormatException,UnsupportedEncodingException{
         if(str==null)
             throw(new BadFormatException("input string cannot be null"));
         if(str.length()<2)
             throw(new BadFormatException("input string too short"));
         position=0;
         return doUnserialize(new String(str.getBytes(charset)));
     }
     
     /** Unserialize from PHP data item
      * @param str PHP serialized data string
      * @return unserialized object
      */
     private Object doUnserialize(String str)throws BadFormatException,UnsupportedEncodingException{
         if(position>=str.length())
             throw(new BadFormatException("input string truncated"));
         char type=str.charAt(position);
         if(type=='N'){//Null
             position+=2;
             return null;
         }else if(type=='b'){//Boolean
             char value=str.charAt(position+2);
             position+=4;
             if(value=='1')
                 return new Boolean(true);
             if(value=='0')
                 return new Boolean(false);
             throw(new BadFormatException("bad value '"+value+"' for boolean"));
         }else if(type=='i'){//Integer
             int start=position+2,end=str.indexOf(';',start);
             position=end+1;
             return new Long(Long.parseLong(str.substring(start,end)));
         }else if(type=='d'){//Double
             int start=position+2,end=str.indexOf(';',start);
             position=end+1;
             return new Double (Double.parseDouble(str.substring(start,end)));
         }else if(type=='s'){//String
             position+=2;
             int start=str.indexOf(':',position);
             int length=Integer.parseInt(str.substring(position,start));
             start+=2;
             position=start+length+2;
             return new String(str.substring(start,start+length).getBytes(),charset);
         }else if(type=='a'){//Array
             position+=2;
             int start=str.indexOf(':',position);
             int length=Integer.parseInt(str.substring(position,start));
             position=start+2;
             LinkedHashMap<Object,Object> value=new LinkedHashMap<Object,Object>();
             for(int i=0;i<length;i++)
                 value.put(doUnserialize(str),doUnserialize(str));
             position++;
             return value;
         }
         throw(new BadFormatException("unknown data type '"+type+"'"));
     }
     
     /** Abstraction interface for serialized output data */
     private interface SerializedDataWrite{
         /**
          * @param data
          */
         public void put(Object data);
         /**
          * @param data
          */
         public void put(int data);
     }
     /** Implementation of SerializedDataWrite using a StringBuffer */
     private class StringBufferSDW implements SerializedDataWrite{
         private StringBuffer outputObject;
         /**
          * @see com.gentics.cr.rest.php.PHPSerializer.SerializedDataWrite#put(java.lang.Object)
          */
         public void put(Object data){
             outputObject.append(data);
         }
         /**
          * @param data 
          * @see com.gentics.cr.rest.php.PHPSerializer.SerializedDataWrite#put(java.lang.Object)
          */
         public void put(int data){
             outputObject.append(new Integer(data).toString());
         }
         /**
          * 
          * @param outputObject
          */
         public StringBufferSDW(StringBuffer outputObject){
             this.outputObject=outputObject;
         }
     }
     /** Implementation of SerializedDataWrite using a PrintStream */
     private class PrintStreamSDW implements SerializedDataWrite{
         private PrintStream outputObject;
         /**
          * @param data 
          * 
          */
         public void put(Object data){
             outputObject.print(data);
         }
         /**
          * @param data 
          * 
          */
         public void put(int data){
             outputObject.append(new Integer(data).toString());
         }
         /**
          * 
          * @param outputObject
          */
         public PrintStreamSDW(PrintStream outputObject){
             this.outputObject=outputObject;
         }
     }
     
     /**
      * Get bytes
      * @param obj
      * @return
      * @throws java.io.IOException
      */
     public byte[] getBytes(Object obj) throws java.io.IOException{
 	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	      ObjectOutputStream oos = new ObjectOutputStream(bos);
 	      oos.writeObject(obj);
 	      oos.flush();
 	      oos.close();
 	      bos.close();
 	      byte [] data = bos.toByteArray();
 	      return data;
 	  }
 }
