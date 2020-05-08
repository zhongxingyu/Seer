 package com.dianping.wizard.utils;
 
 import com.dianping.wizard.exception.WizardExeption;
 
 import java.io.*;
 import java.util.Collection;
 import java.util.regex.Pattern;
 
 /**
  * @author ltebean
  */
 public class FileUtils {
 
     public static String readFileOnClassPath(final String fileName,final String extensions){
        String pattern=".*[/\\\\]"+fileName+"."+extensions;
         try {
             Collection<String> paths=ResourceList.getResources(Pattern.compile(pattern));
             if(paths.size()>1){
                 throw new WizardExeption("no distinct file: "+pattern);
             }
             if(paths.size()==0){
                 return "";
             }
 
             String jarPath = JarUtils.jarPath();
             if (jarPath == null) {
                 File file = new File(paths.iterator().next());
                 String result=org.apache.commons.io.FileUtils.readFileToString(file,"UTF-8");
                 return result;
             }
 
             InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(paths.iterator().next());
             StringBuilder builder = new StringBuilder();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             String line = null;
             try {
                 while ((line = reader.readLine()) != null) {
                     builder.append(line);
                     builder.append("\n"); //appende a new line
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 inputStream.close();
             }
             return builder.toString();
         } catch (IOException e) {
             throw new WizardExeption("reading file error: " + pattern ,e);
         }
     }
 
     public static String readFileOnClassPath(final String fileName, final String extensions, final String defaultValue){
         try {
             return readFileOnClassPath(fileName,extensions);
         } catch(Exception e) {
             return defaultValue;
         }
     }
 }
