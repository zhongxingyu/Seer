 package fr.dz.envo.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.mozilla.universalchardet.UniversalDetector;
 
 import fr.dz.envo.exception.EnVOException;
 
 public class IOUtils {
 	
 	// Constantes
 	private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:12.0) Gecko/20100101 Firefox/12.0";
 	private static final String DEFAULT_ENCODING = "UTF-8";
 
 	/**
 	 * Sauvegarde un input stream dans un fichier en utilisant l'encoding donné
 	 * @param file
 	 * @param srcEncoding
 	 * @param targetEncoding
 	 * @throws EnVOException 
 	 */
 	public static void encodeInputStream(InputStream in, File file, String targetEncoding) throws EnVOException {
 		String sourceEncoding = IOUtils.detectEncodingAndSave(in, file);
 		if ( ! StringUtils.isEmpty(sourceEncoding) && ! sourceEncoding.equals(targetEncoding) ) {
 			IOUtils.changeEncoding(file, sourceEncoding, targetEncoding);
 		}
 	}
 	
 	/**
 	 * Change l'encoding d'un fichier
 	 * @param file
 	 * @param srcEncoding
 	 * @param targetEncoding
 	 * @throws EnVOException 
 	 */
 	public static void changeEncoding(File file, String srcEncoding, String targetEncoding) throws EnVOException {
 		try {
 			String content = FileUtils.readFileToString(file, srcEncoding);
 			
 			// Hack pour supprimer le ? en début de fichier qui apparaît dans certains cas
 			String targetContent = new String(content.getBytes(targetEncoding), targetEncoding);
 			if ( targetContent.startsWith("?") ) {
 				targetContent = targetContent.substring(1);
 			}
 			
 	        FileUtils.write(file, targetContent, targetEncoding);
 		} catch(IOException e) {
 			throw new EnVOException("Erreur pendant la conversion du fichier "+file+" en "+targetEncoding, e);
 		}
 	}
 
 	/**
 	 * Sauvegarde le contenu d'un input stream dans un fichier
 	 * @param in
 	 * @param file
 	 * @return
 	 * @throws EnVOException 
 	 */
 	public static void saveInputStream(InputStream in, File file) throws EnVOException {
 		BufferedOutputStream out = null;
 	    try {
 	    	// Enregistrement du fichier
 	        in = new BufferedInputStream(in);
 			out = new BufferedOutputStream(new FileOutputStream(file));
 			
 			// Enregistrement
 			byte[] buffer = new byte[1024];
 			int nbRead = 0;
 			while ( (nbRead = in.read(buffer)) != -1 ) {
 				out.write(buffer, 0, nbRead);
 			}
 		} catch (IOException e) {
 			throw new EnVOException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 		} finally {
 			if ( in != null ) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 				}
 			}
 			if ( out != null ) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sauvegarde le contenu d'une URL dans un fichier
 	 * @param url
 	 * @param file
 	 * @return
 	 * @throws EnVOException 
 	 */
 	public static void saveURL(URL url, File file) throws EnVOException {
 		BufferedInputStream in = null;
 		BufferedOutputStream out = null;
 	    try {
 	    	// Création de la connexion
 			URLConnection connection = url.openConnection();
 	        connection.setRequestProperty("User-Agent", USER_AGENT);
 	        connection.connect();
 	    	
 	        // Enregistrement du fichier
 			saveInputStream(connection.getInputStream(), file);
 		} catch (Exception e) {
 			throw new EnVOException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
 		} finally {
 			if ( in != null ) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
 				}
 			}
 			if ( out != null ) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Retourne le contenu correspondant à une URL et stocke le résultat dans un fichier à des fins de logs
 	 * @param url
 	 * @param resultFile
 	 * @return
 	 * @throws EnVOException 
 	 */
 	public static String getURLContent(URL url, File resultFile) throws EnVOException {
 		BufferedReader reader = null;
 		BufferedWriter writer = null;
 		try {
 			// Création de la connexion
 			URLConnection connection = url.openConnection();
 	        connection.setRequestProperty("User-Agent", USER_AGENT);
 	        connection.connect();
 	        
 	        // Récupération de la page de résultat de la requète
 	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), DEFAULT_ENCODING));
 	        if ( resultFile != null ) {
 	        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), DEFAULT_ENCODING));
 	        }
 	        String line = null;
 	        StringBuffer pageBuffer = new StringBuffer();
 	        while ( (line=reader.readLine()) != null ) {
 	        	if ( writer != null ) {
 		        	writer.write(line);
 		        	writer.newLine();
 	        	}
 	        	pageBuffer.append(line+"\n");
 	        }
 	        
 	        return pageBuffer.toString();
 		} catch(IOException e) {
 			throw new EnVOException("Erreur pendant l'exécution de la requète : "+url, e);
 		} finally {
 			if ( reader != null ) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant l'exécution de la requète : "+url, e);
 				}
 			}
 			if ( writer != null ) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant l'exécution de la requète : "+url, e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Détecte l'encoding d'un InputStream et l'enregistre dans un fichier
 	 * @param in
 	 * @param file
 	 * @return L'encoding détecté
 	 * @throws EnVOException 
 	 */
 	protected static String detectEncodingAndSave(InputStream in, File file) throws EnVOException {
 		byte[] buffer = new byte[4096];
 		OutputStream out = null;
 		try {
 			// Création du flux de sortie
 			out = new BufferedOutputStream(new FileOutputStream(file)); 
 		
 			// Lecture du fichier via un detecteur d'encoding
 			UniversalDetector detector = new UniversalDetector(null);
 		    int nread;
		    while ((nread = in.read(buffer)) > 0 && !detector.isDone()) {
 		    	
 		    	// Passage des données au détecteur
 		    	detector.handleData(buffer, 0, nread);
 		    	
 		    	// Sauvegarde dans le fichier
 		    	out.write(buffer, 0, nread);
 		    }
 		    
 		    // Retour de l'encoding détecté
 		    detector.dataEnd();
 		    String encoding = detector.getDetectedCharset();
 		    detector.reset();
 		    return encoding;
 		} catch(IOException e) {
 			throw new EnVOException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 		} finally {
 			if ( in != null ) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 				}
 			}
 			if ( out != null ) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					throw new EnVOException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 				}
 			}
 		}
 	}
 }
