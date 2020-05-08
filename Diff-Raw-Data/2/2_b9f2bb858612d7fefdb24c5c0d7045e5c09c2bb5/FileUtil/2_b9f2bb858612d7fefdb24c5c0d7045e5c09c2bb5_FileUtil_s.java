 package com.linet.util.file;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import javax.activation.MimetypesFileTypeMap;
 
 public class FileUtil {
 
     public static String getPathFromWhereApplicationIsRunning() throws Exception {
         String path = null;
         try {
             path = new File(".").getCanonicalPath();
         } catch (IOException ex) {
         	throw new Exception("Error when try to obtain general path of current application.",ex);
         }
         return path;
     }
 
     public static String getContentTypeFromPathFile(String pathFile) {
     	File file = new File(pathFile);
         return new MimetypesFileTypeMap().getContentType(file);
     }
 
     public static String getFormatFromPathFile(String pathFile) {
 
         int ind_separador = pathFile.lastIndexOf(".");
         return pathFile.substring(ind_separador + 1, pathFile.length());
     }
 
     public static String getNameFromPathFile(String pathFile) {
     	
     	File file = new File(pathFile);
     	
        int slahIndex = file.getAbsolutePath().lastIndexOf(File.separator);
         int puntoIndex = file.getAbsolutePath().lastIndexOf(".", slahIndex);
         if (puntoIndex == -1) {
             return file.getAbsolutePath().substring(slahIndex + 1);
         } else {
             return file.getAbsolutePath().substring(slahIndex + 1, puntoIndex);
         }
 
     }
 
     public static String getOnlyPathFromPathFile(String pathFile) {
 
         int ind_separador = pathFile.lastIndexOf(File.separator);
         return pathFile.substring(0, ind_separador);
     }
 
     public static void serializeObject(String nameFile, Object obj) {
         ObjectOutputStream salida = null;
 
         try {
             salida = new ObjectOutputStream(new FileOutputStream(nameFile));
             salida.writeObject(obj);
         } catch (IOException ex) {
             ex.printStackTrace();
         } finally {
             if (salida != null) {
                 try {
                     salida.close();
                 } catch (IOException ex) {
                 }
             }
         }
     }
 
     public static Object deserializeObject(String nameFile) {
         ObjectInputStream entrada = null;
         Object out = null;
 
         try {
             entrada = new ObjectInputStream(new FileInputStream(nameFile));
             out = entrada.readObject();
             entrada.close();
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         } finally {
             if (entrada != null) {
                 try {
                     entrada.close();
 
                 } catch (IOException ex1) {
                 }
 
             }
 
         }
         return out;
     }
 
     public static ArrayList<String> simulateMSDOSCommand_dir(String path, String dateFormat, String tokenSeparator) {
 
         File directorio = new File(path);
         String[] ficheros = directorio.list();
         ArrayList<String> filas = new ArrayList<String>();
         int nivel = 0;
 
         SimpleDateFormat format = new SimpleDateFormat(dateFormat);
 
         for (int i = 0; i < ficheros.length; i++) {
             try {
 
                 File tmp = new File(path + File.separator + ficheros[i]);
                 if (tmp.isFile()) {
                     Date d = new Date();
                     d.setTime(tmp.lastModified());
                     filas.add(getOnlyPathFromPathFile(tmp.getAbsolutePath()) + tokenSeparator + tmp.getName() + tokenSeparator + getFormatFromPathFile(tmp.getAbsolutePath()) + tokenSeparator + format.format(d));
                 } else if (tmp.isDirectory()) {
                     listFilesOfDirectory(tmp, path + File.separator + ficheros[i], nivel, format, tokenSeparator, filas);
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         return filas;
     }
 
     public static void listFilesOfDirectory(File directory, String path, int nivel, SimpleDateFormat format, String tokenSeparador, ArrayList<String> rows) {
         nivel++;
         String[] ficheros = directory.list();
 
         for (int i = 0; i < ficheros.length; i++) {
             try {
 
                 File tmp = new File(path + File.separator + ficheros[i]);
                 if (tmp.isFile()) {
                     Date d = new Date();
                     d.setTime(tmp.lastModified());
                     rows.add(getOnlyPathFromPathFile(tmp.getAbsolutePath()) + tokenSeparador + tmp.getName() + tokenSeparador + getFormatFromPathFile(tmp.getAbsolutePath()) + tokenSeparador + format.format(d));
                 } else if (tmp.isDirectory()) {
                     listFilesOfDirectory(tmp, path + File.separator + ficheros[i], nivel, format, tokenSeparador, rows);
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
    
 
     public static String backLevelsOfPath(String path, int levelsToBack) {
         int ind = 0;
         if (path.charAt(path.length() - 1) == File.separator.charAt(0)) {// encontro que el path enviado tiene un separator al final
             ind = path.lastIndexOf(File.separator);
             path = path.substring(0, ind);
         }
 
         for (int n = 0; n < levelsToBack; n++) {
             ind = path.lastIndexOf(File.separator);
             if (ind > 0) {// encontro el ultimo separator
                 path = path.substring(0, ind);
             }
         }
         return path;
     }
 
     public static boolean compareFilesUsingBufferedInputStream(String filePath1, String filePath2) {
 
         File f1 = null;
         File f2 = null;
 
         BufferedInputStream bis1 = null;
         BufferedInputStream bis2 = null;
 
 
         try {
             f1 = new File(filePath1);
             f2 = new File(filePath2);
             if (f1.length() == f2.length()) {
                 bis1 = new BufferedInputStream(new FileInputStream(f1));
                 bis2 = new BufferedInputStream(new FileInputStream(f2));
 
                 while (true) {
                     int a = bis1.read();
                     int b = bis2.read();
                     if (a != b) {
                         return false;
                     }
                     if (a == -1) {
                         return true;
                     }
                 }
             } else {
                 return false;
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (bis1 != null) {
                 try {
                     bis1.close();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
             if (bis2 != null) {
                 try {
                     bis2.close();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
         }
         return false;
     }
 
     public static boolean compareFilesUsingFileInputStream(String filePath1, String filePath2) {
     	FileInputStream fis1=null;
     	FileInputStream fis2=null;
     	try {
             File f1 = new File(filePath1);
             File f2 = new File(filePath2);
             if (f1.length() == f2.length()) {
                 fis1 = new FileInputStream(f1);
                 fis2 = new FileInputStream(f2);
 
                 while (true) {
                     int a = fis1.read();
                     int b = fis2.read();
                     if (a != b) {
                         return false;
                     }
                     if (a == -1) {
                         return true;
                     }
                 }
             } else {
                 return false;
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally{
         	if(fis1!=null){
         		try {
 					fis1.close();
 				} catch (IOException e) {
 					//ignore
 				}
         	}
         	if(fis2!=null){
         		try {
 					fis2.close();
 				} catch (IOException e) {
 					//ignore
 				}
         	}
         }
         return false;
     }
 
     public static void deleteFile(String filePath) {
         File f = new File(filePath);
         if (f.exists()) {
             f.delete();
         }
 
     }
     
     public static Collection<String> readFileAsStringCollection(String url) throws Exception {
     	Collection<String> temp = new ArrayList<String>();
         FileReader fr = null;
         BufferedReader entrada = null;
         try {
             fr = new FileReader(url);
             entrada = new BufferedReader(fr);
             String s;
             while ((s = entrada.readLine()) != null) {
                 temp.add(s);
             }
         } catch (IOException ex) {
         	throw new Exception("Error when try to read file to collection of strings.",ex);
         } finally {
             try {
                 fr.close();
                 entrada.close();
             } catch (IOException ex) {
             	throw new Exception("Error when try to close stream file.",ex);
             }
 
 
         }
         return temp;
     }    
     
     public static void writeFileFromStringCollection(String url, Collection<String> stringsCollection) throws Exception {
         FileWriter fw = null;
         BufferedWriter bw = null;
 
         try {
             fw = new FileWriter(url);
             bw = new BufferedWriter(fw);
             for (String fila : stringsCollection) {
                 bw.write(fila + "\n");
             }
 
         } catch (java.io.IOException ioex) {
         	throw new Exception("Error when try to write collection of strings to file.",ioex);
         } finally {
 
             if (bw != null) {
                 try {
                     bw.close();
                 } catch (IOException ex) {
                 	throw new Exception("Error when try to close stream file.",ex);
                 }
             }
 
             if (fw != null) {
                 try {
                     fw.close();
                 } catch (IOException ex) {
                 	throw new Exception("Error when try to close stream file.",ex);
                 }
             }
 
 
         }
     }    
 
     public static void writeFileFromSimpleString(String url, String content) throws Exception {
         FileWriter fw = null;
         BufferedWriter bw = null;
 
         try {
             fw = new FileWriter(url);
             bw = new BufferedWriter(fw);
             bw.write(content);
 
         } catch (java.io.IOException ioex) {
         	throw new Exception("Error when try to write collection of strings to file.",ioex);
         } finally {
 
             if (bw != null) {
                 try {
                     bw.close();
                 } catch (IOException ex) {
                 	throw new Exception("Error when try to close stream file.",ex);
                 }
             }
 
             if (fw != null) {
                 try {
                     fw.close();
                 } catch (IOException ex) {
                 	throw new Exception("Error when try to close stream file.",ex);
                 }
             }
 
 
         }
     }       
     
     public static void readContentFileSystemOut(String url) {
         FileReader fr = null;
         BufferedReader entrada = null;
         try {
             fr = new FileReader(url);
             entrada = new BufferedReader(fr);
             String s;
             while ((s = entrada.readLine()) != null) {
                 System.out.println("" + s);
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         } finally {
             try {
                 fr.close();
                 entrada.close();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
 
     }
 
     public static File inputStreamToFile(InputStream entrada,String path) {
         
         File f=null;
         try {
             f = new File(path);//Aqui le dan el nombre y/o con la ruta del archivo salida
             
             OutputStream salida = new FileOutputStream(f);
             byte[] buf = new byte[1024];//Actualizado me olvide del 1024
             int len;
             while ((len = entrada.read(buf)) > 0) {
                 salida.write(buf, 0, len);
             }
             salida.close();
             entrada.close();
             
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         return f;
     }
     
     public static File inputStreamToTempFile(InputStream entrada,String path,String suffix) {
         
         File f=null;
         try {
 
             f = File.createTempFile(path, "."+suffix );
             
             OutputStream salida = new FileOutputStream(f);
             byte[] buf = new byte[1024];//Actualizado me olvide del 1024
             int len;
             while ((len = entrada.read(buf)) > 0) {
                 salida.write(buf, 0, len);
             }
             salida.close();
             entrada.close();
             
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         return f;
     }    
 }
