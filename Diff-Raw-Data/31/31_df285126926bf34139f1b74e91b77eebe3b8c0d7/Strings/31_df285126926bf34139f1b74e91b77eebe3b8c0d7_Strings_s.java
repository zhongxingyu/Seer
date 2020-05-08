 package org.moon.utils;
 
 import java.io.InputStream;
 import java.nio.charset.Charset;
 
 /**
  * 字符串处理类
  * @author Gavin
  * @Date Dec 30, 2013 3:06:04 PM
  */
 public class Strings {
     
     /**
      * 首字母小写
      * @param src
      * @return
      */
     public static String lowerFirst(String src){
         if(src==null||src.length()==0){
             return "";
         }
         return src.substring(0, 1).toLowerCase()+src.substring(1);
     }
     
     /**
      * 从输入流捕获内容
      * @param in
      * @return
      */
     public static String getContentFromInputStream(InputStream in){
         return getContentFromInputStream(in,Charset.defaultCharset().name());
     }
     
     public static String getContentFromInputStream(InputStream in,String charset){
         StringBuffer dist = new StringBuffer();
         byte[] data = new byte[1024];
         int readNum = -1;
         try{
             while((readNum=in.read(data))!=-1){
                 dist.append(new String(data,0,readNum, charset));
             }
             in.close();
         }catch(Exception e){
             e.printStackTrace();
         }
         return dist.toString();
     }
 }
