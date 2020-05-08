 /*<Dynamic Refactoring Plugin For Eclipse 2.0 - Plugin that allows to perform refactorings 
 on Java code within Eclipse, as well as to dynamically create and manage new refactorings>
 
 Copyright (C) 2009  Laura Fuente De La Fuente
 
 This file is part of Foobar
 
 Foobar is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
 
 package dynamicrefactoring.util.io;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.osgi.framework.Bundle;
 
 import dynamicrefactoring.RefactoringPlugin;
 
 /**
  * Proporciona funciones de acceso a los ficheros y directorios del sistema de
  * archivos sobre el que se trabaja.
  * 
  * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
  * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
  * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
  */
 public class FileManager {
 
 	/**
 	 * Vaca un directorio y todos sus subdirectorios.
 	 * 
 	 * @param rootDirectory
 	 *            ruta o nombre del directorio raz.
 	 * 
 	 * @return <code>true</code> si se pudo completar la operacin con xito.
 	 *         <code>false</code> en caso contrario.
 	 */
 	public static boolean emptyDirectories(String rootDirectory){
 		boolean completed = true;
 		
 		File root = new File(rootDirectory);
 		
 		if(root.exists() && root.isDirectory()){
 			File [] contents = root.listFiles();
 			for (int i=0; i < contents.length; i++){
 				if (contents[i].isFile()){
 					if(!contents[i].delete())
 						completed = false;
 				}
 				else if(contents[i].isDirectory()){
 					if(!emptyDirectories(contents[i].getAbsolutePath()))
 						completed = false;
 				}					
 			}
 		}
 		else
 			completed = false;
 		
 		return completed;
 	}
 
 	/**
 	 * Borra un directorio y todos sus subdirectorios, siempre y cuando estn
 	 * vacos.
 	 * 
 	 * @param rootDirectory
 	 *            el nombre o la ruta del directorio.
 	 * @param deleteRoot
 	 *            si se debe borrar el directorio raz al acabar.
 	 * 
 	 * @return <code>true</code> si se consigui completar el borrado; <code>
 	 * false</code> si alguno de los directorios no pudo ser borrado.
 	 */
 	public static boolean deleteDirectories(String rootDirectory, boolean deleteRoot) {
 		boolean completed = true;
 		File temp;
 		
 		try {				
 			File root = new File(rootDirectory);
 					
 			File[] fileList = root.listFiles();
 				
 			for(int i = 0; i < fileList.length; i++){
 				temp = fileList[i];
 				if(temp.isDirectory())
 					if(!deleteDirectories(temp.getAbsolutePath(), true))
 						completed = false;
 			}
 			
 			if(deleteRoot)
 				if(!root.delete())
 					completed = false;						
 		}
 		catch (Exception e){
 			return false;
 		}
 		
 		return completed;
 	}
 
 	/**
 	 * Borrado de un archivo a partir de su ruta.
 	 *
 	 * @param path la ruta del fichero que se desea borrar.
 	 * 
 	 * @return <code>true</code> si se pudo borrar el fichero; <code>false</code> en
 	 * caso contrario (si no se encuentra la ruta especificada, o no se trata de un
 	 * fichero, por ejemplo).
 	 */
 	public static boolean deleteFile(String path){
 		try {			
 			File file = new File(path);
 			
 			if(file.exists() & file.isFile())
 				return file.delete();
 		
 			return false;
 		}
 		catch (Exception e){
 			return false;
 		}
 	}
 	
 	/**
 	 * Intenta crear un nuevo fichero en una ruta determinada. Si ya existe un
 	 * fichero con el mismo nombre en dicha ruta, simplemente no hace nada.
 	 * 
 	 * @param path la ruta del nuevo fichero que se quiere crear.
 	 */
 	public static void createFile(String path){
 		try {
 			File file = new File(path);
 			
 			file.createNewFile();
 		}
 		catch (IOException ioe){
 			;
 		}
 	}
 	
 	/**
 	 * Intenta crear un nuevo directorio en una ruta determinada.
 	 * 
 	 * @param path la ruta del nuevo directorio que se quiere crear.
 	 * 
 	 * @return <code>true</code> si se pudo crear el directorio; <code>false</code>
 	 * en caso contrario. 
 	 */
 	public static boolean createDir(String path){
 		try {
 			File file = new File(path);
 			
 			return file.mkdir();
 		}
 		catch (SecurityException se){
 			return false;
 		}
 	}
 
 	/**
 	 * Crea una copia de un fichero.
 	 * 
 	 * @param in
 	 *            el fichero original.
 	 * @param out
 	 *            el fichero que se deber crear como copia.
 	 * 
 	 * @throws IOException
 	 *             si no se encuentra alguno de los dos ficheros.
 	 */
 	public static void copyFile(File in, File out) throws IOException {
 		
 		if (!out.exists())
 		    out.createNewFile();
 		
 		FileInputStream input  = new FileInputStream(in);
 		FileOutputStream output  = new FileOutputStream(out);
 		
 		try {
 			byte[] buf = new byte[1024];
 			int i = 0;
 			while ((i = input.read(buf)) != -1)
 				output.write(buf, 0, i);
 		}
 		finally {
 			if (input != null) input.close();
 			if (output != null) output.close();
 		}
 	}
 
 	/**
 	 * Crea una copia de un directorio en una ruta de destino determinada.
 	 * 
 	 * @param original
 	 *            directorio que se debe copiar.
 	 * @param destination
 	 *            directorio en que se crear la copia del original y todo su
 	 *            contenido.
 	 * 
 	 * @return <code>true</code> si se consigue realizar la copia completa con
 	 *         xito; <code>false</code> en caso contrario.
 	 * 
 	 * @throws IOException
 	 *             si se produce un error al crear el directorio destino o al
 	 *             copiar el contenido.
 	 */
 	public static boolean copyFolder(String original, String destination) 
 		throws IOException {
 		
 		File input = new File(original);
 		File output = new File(destination);
 		
 		// Si ambos directorios existen y son directorios.
 		if (input.exists() && input.isDirectory())
 			if (output.exists() && output.isDirectory()){
 				
 				// Se obtiene la ruta del directorio que se debe crear en destino.
 				String newPath = output.getCanonicalPath() + 
 					System.getProperty("file.separator") + input.getName(); //$NON-NLS-1$
 				// Si ya existe o se consigue crear.
 				if (new File(newPath).exists() || createDir(newPath)){
 					File[] files = input.listFiles();
 					for(File next : files){
 						// Se copian sus ficheros uno a uno.
 						if(next.isFile())
 						copyFile(next, new File((newPath + 
 							System.getProperty("file.separator") + next.getName())));
 						else if(next.isDirectory())
 							copyFolder(next.getPath(),newPath );
 					}	
 					return true;
 				}
 			}
 		return false; 
 	}
 
 	/**
 	 * Obtiene la ruta completa de un fichero determinado, sin la extensin del
 	 * fichero.
 	 * 
 	 * @param filePath
 	 *            la ruta del fichero.
 	 * 
 	 * @return la ruta del fichero con el nombre del fichero sin extensin.
 	 */
     public static String getFilePathWithoutExtension(String filePath) {
     	
     	String temp = new String();
     	if(filePath.lastIndexOf(".") >= 0) //$NON-NLS-1$
     		temp = temp.concat(
     			filePath.substring(0, filePath.lastIndexOf("."))); //$NON-NLS-1$
 
 		// Si es un fichero sin extensin.
     	else
       		temp = temp.concat(filePath);
     	
     	return temp;
     }
 
 	/**
 	 * Obtiene la ruta del directorio donde est contenido un fichero
 	 * determinado.
 	 * 
 	 * @param filePath
 	 *            la ruta del fichero.
 	 * 
 	 * @return la ruta del directorio donde est contenido el fichero.
 	 */
     public static String getDirectoryPath(String filePath) {
     	
     	String temp = new String();
     	if(filePath.lastIndexOf(System.getProperty("file.separator")) >= 0) { //$NON-NLS-1$
     		temp = temp.concat(filePath.substring(0, filePath.lastIndexOf(
     			System.getProperty("file.separator")))); //$NON-NLS-1$
     		if(temp.lastIndexOf(".." + System.getProperty("file.separator")) >= 0) //$NON-NLS-1$ //$NON-NLS-2$
         		temp = temp.substring(temp.lastIndexOf(
         			".." + System.getProperty("file.separator")) + 3, temp.length()); //$NON-NLS-1$ //$NON-NLS-2$
 
        		temp = temp.substring(temp.indexOf(
        			System.getProperty("file.separator")) + 1,temp.length()); //$NON-NLS-1$
     	}
     	
     	return temp;
     }
     
     /**
      * Genera un nombre comprensible para un fichero determinado.
      * 
      * @param fileName el nombre del fichero.
      * 
      * @return el nombre comprensible generado.
      */
     public static String getReadableName(String fileName) {
     	
     	String temp = new String();
     	if(fileName.lastIndexOf(".") >= 0) { //$NON-NLS-1$
     		temp = temp.concat(fileName.substring(0, fileName.lastIndexOf("."))); //$NON-NLS-1$
     		temp = temp.concat(" (" + fileName + ")"); //$NON-NLS-1$ //$NON-NLS-2$
     		
     	}
     	else
     		temp = temp.concat(fileName);
     	return temp;
     }
 
 	/**
 	 * Obtiene el nombre de un fichero determinado, sin la ruta hasta l.
 	 * 
 	 * @param filePath
 	 *            la ruta del fichero.
 	 * 
 	 * @return el nombre del fichero sin la ruta para llegar hasta l.
 	 */
     public static String getFileName(String filePath) {
     	String temp = new String();
     	if(filePath.lastIndexOf(System.getProperty("file.separator")) >= 0) //$NON-NLS-1$
     		temp = temp.concat(
     			filePath.substring(
     				filePath.lastIndexOf(System.getProperty("file.separator")) + 1, filePath.length())); //$NON-NLS-1$
 
 		// Si el fichero no est en un directorio.
     	else
       		temp = filePath;
 
       	return temp;
     }
 
 	/**
 	 * Permite obtener la ruta relativa de un fichero a partir de su ruta
 	 * absoluta.
 	 * 
 	 * @param rutaAbsoluta
 	 *            ruta absoluta de un fichero.
 	 * @return devuelve la ruta relativa del fichero cuya rutaAbsoluta se ha
 	 *         introducido en al funcin.
 	 */
     public static String AbsoluteToRelative(String rutaAbsoluta){
     	String rutaRelativa ="";
     	int contador=0;
     	boolean comun=false;
     	Object absolute=null;
     	Object actual=null;
     	String rutaActual = new File(".").getAbsolutePath();
 
     	rutaAbsoluta=rutaAbsoluta.replace("/",File.separator);
     	rutaAbsoluta=rutaAbsoluta.replace("" + File.separatorChar + "",File.separator);
 
     	StringTokenizer st_absolute = new StringTokenizer(rutaAbsoluta,File.separator);
     	StringTokenizer st_actual = new StringTokenizer(rutaActual,File.separator);
     	while(st_absolute.hasMoreTokens() && st_actual.hasMoreElements() ){
     		absolute =st_absolute.nextElement(); 
     		actual= st_actual.nextElement();
     		if(absolute.toString().equals(actual.toString())){
     			comun=true;
     		}else{
     			break;
     		}		
     	}
 
     	while(st_actual.hasMoreElements()){
     		st_actual.nextElement();
     		contador++;
     	}
     	contador++;
 
     	if(comun==true){
     		if(contador>0){
     			for(int i=1; i<contador; i++){
     				rutaRelativa=rutaRelativa+".." + File.separator;
     			}
     		}else if(contador==0){
     			rutaRelativa=rutaRelativa+"." + File.separator;
     		}
     		while(st_absolute.hasMoreElements()){
     			rutaRelativa=rutaRelativa+absolute.toString()
     			+ File.separator;
     			absolute=st_absolute.nextElement();
     		}
     		rutaRelativa=rutaRelativa+absolute.toString();
     		rutaRelativa=rutaRelativa.replace("" + File.separatorChar + "","/" );
     	}else{
     		rutaRelativa=rutaAbsoluta; //estan en distinta unidad y por
     		//tanto no se puede obtener su ruta relativa.
     	}
 
     	return rutaRelativa;
 
 
     }
 
 	/**
 	 * Copia un directorio empaquetado en el plugin en un directorio del sistema
 	 * de ficheros.
 	 * 
 	 * @param bundleDir
 	 * @param fileSystemDir
 	 * @throws IOException
 	 */
 	public static void copyBundleDirToFileSystem(String bundleDir,
 			String fileSystemDir) {
 		// TODO: Test para este mtodo
 		final Bundle bundle = Platform.getBundle(RefactoringPlugin.BUNDLE_NAME);
		final Enumeration<?> entries = bundle.findEntries(bundleDir, "*", true);
		for (Object entrada : Collections.list(entries)) {
 			URL entry = (URL) entrada;
 			File fichero = new File(entry.getFile());
 			if (!entry.toString().endsWith("/")) {
 				InputStream inputStream = null;
 				OutputStream outputStream = null;
 				try {
 					FileUtils.forceMkdir(new File(fileSystemDir
 							+ File.separator + fichero.getParent()));
 					inputStream = entry.openStream();
 					outputStream = new FileOutputStream(new File(fileSystemDir
 							+ entry.getFile()));
 					IOUtils.copy(inputStream, outputStream);
 					inputStream.close();
 					outputStream.close();
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					IOUtils.closeQuietly(inputStream);
 					IOUtils.closeQuietly(outputStream);
 				}
 	
 			}
 		}
 	}
 
 	/**
 	 * Obtiene la URL que permite acceder a un recurso contenido dentro
 	 * de un plugin.
 	 * 
 	 * @param pluginId Identificador del plugin
 	 * @param fullPath Ruta del recurso dentro del contenedor del plugin
 	 * @return URL con la ruta de acceso al recurso
 	 */
 	public static URL getURLForPluginResource(String pluginId, String fullPath) {
 		Bundle bundle = Platform.getBundle(pluginId);
 		Path path = new Path(fullPath);
 		URL fileURL = FileLocator.find(bundle, path, null);
 		return fileURL;
 	}
 
 	public static void copyResourceToDir(String resourcePath, String dirPath) throws IOException {
 		FileUtils.copyInputStreamToFile(
 				dynamicrefactoring.util.io.FileManager.class
 						.getResourceAsStream(resourcePath), new File(dirPath
 						+ resourcePath));
 	}
 }
