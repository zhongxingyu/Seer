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
    
    /**
     * 截取字符串，当截取长度大于最大能截取的长度时，直接返回最大长度的子字符串。该方法返回的结果的长度应该小于或等于count
     * @param src 需要截取的原字符串
     * @param startIndex 开始位置
     * @param count 截取的最大长度
     * @return
     */
    public static String subString(String src,int startIndex,int count){
    	if(src==null){
    		return "";
    	}
    	byte[] stringBytes = src.getBytes();
    	if(startIndex<0){
    		startIndex = 0;
    	}
    	if(count>stringBytes.length-startIndex){
    		count = stringBytes.length-startIndex;
    	}
    	return new String(stringBytes,startIndex,count);
    }
 }
