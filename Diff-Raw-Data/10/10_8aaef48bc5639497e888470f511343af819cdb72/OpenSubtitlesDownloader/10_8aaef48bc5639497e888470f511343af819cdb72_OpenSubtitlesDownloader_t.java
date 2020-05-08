 package fr.dz.opensubtitles;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import fr.dz.opensubtitles.exception.OpenSubtitlesException;
 
 public class OpenSubtitlesDownloader {
 	
 	// Constantes
 	public static final String OPEN_SUBTITLES_DOMAIN = "http://www.opensubtitles.org";
 	private static final String QUERY_URL_START = OPEN_SUBTITLES_DOMAIN + "/fr/search2";
 	private static final String PARAM_NAME_VALUE_SEPARATOR = "-";
 	private static final String PARAM_SEPARATOR = "/";
 	private static final String LANGUAGE_PARAM_NAME = "sublanguageid";
 	private static final String SEASON_PARAM_NAME = "season";
 	private static final String EPISODE_PARAM_NAME = "episode";
 	private static final String FORMAT_PARAM_NAME = "subformat";
 	private static final String FORMAT_PARAM_VALUE = "srt";
 	private static final String MOVIE_PARAM_NAME = "moviename";
 	private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:12.0) Gecko/20100101 Firefox/12.0";
 	private static final String NO_RESULT_STRING = "<div class=\"msg warn\"><b>Aucun résultat</b> trouvé";
 	private static final String SUBTITLES_PAGE_TEXT = "<fieldset><legend>Détails des sous-titres</legend><div";
 	private static final String SUBTITLE_URL_PREFIX = "/fr/subtitles/";
 	private static final String SUBTITLE_ID_START = "</tr><tr onclick=\"servOC(";
 	private static final String SUBTITLE_ID_END = ",";
 	
 	// La requète
 	private OpenSubtitlesRequest request;
 	private URL queryURL;
 	private String queryResultPage;
 	private List<OpenSubtitlesResult> subtitlesResults;
 	
 	/**
 	 * Constructeur à partir d'une recherche dans OpenSubtitles
 	 * @param request
 	 * @throws OpenSubtitlesException
 	 */
 	public OpenSubtitlesDownloader(OpenSubtitlesRequest request) throws OpenSubtitlesException {
 		setRequest(request);
 	}
 
 	/**
 	 * Exécute la recherche de sous-titres et retourne true si au moins un résultat a été trouvé, false sinon
 	 * @return
 	 * @throws OpenSubtitlesException
 	 */
 	public boolean hasSubtitles() throws OpenSubtitlesException {
 		
 		OpenSubtitles.LOGGER.debug("#####################################################################");
 		OpenSubtitles.LOGGER.debug("# Exécution de la requète : "+queryURL);
 		OpenSubtitles.LOGGER.debug("#####################################################################");
 		
         // Récupération de la page de résultat de la requète
         this.queryResultPage = getURLContent(queryURL);
         
         // Recherche de la chaîne indiquant qu'il n'y a pas de résultat
         boolean trouve = queryResultPage.indexOf(NO_RESULT_STRING) != -1;
         
         return ! trouve; 
 	}
 
 	/**
 	 * Télécharge les premiers sous titres correspondants
 	 * @throws OpenSubtitlesException
 	 */
	public boolean downloadFirstSubtitles() throws OpenSubtitlesException {
 		if ( queryResultPage == null ) {
 			throw new OpenSubtitlesException("Appeler hasSubtitles() avant d'appeler downloadFirstSubtitles()");
 		}
 		
 		// Récupération de la liste des URLs de sous-titres 
 		this.subtitlesResults = getSubtitlesURLs();
 		
 		// Choix du meilleur scoring (le plus petit)
 		OpenSubtitlesResult bestResult = null;
 		for ( OpenSubtitlesResult result : subtitlesResults ) {
 			if ( bestResult == null || result.getScoring() < bestResult.getScoring() ) {
 				bestResult = result;
 			}
 		}
 		
 		// Téléchargement du meilleur résultat
		if ( bestResult != null ) {
			download(bestResult);
			return true;
		} else {
			return false;
		}
 	}
 
 	/**
 	 * Retourne le contenu correspondant à une URL
 	 * @param url
 	 * @return
 	 * @throws OpenSubtitlesException 
 	 */
 	public static String getURLContent(URL url) throws OpenSubtitlesException {
 		return getURLContent(url, null);
 	}
 	
 	/**
 	 * Retourne le contenu correspondant à une URL et stocke le résultat dans un fichier à des fins de logs
 	 * @param url
 	 * @param resultFile
 	 * @return
 	 * @throws OpenSubtitlesException 
 	 */
 	public static String getURLContent(URL url, File resultFile) throws OpenSubtitlesException {
 		BufferedReader reader = null;
 		BufferedWriter writer = null;
 		try {
 			// Création de la connexion
 			URLConnection connection = url.openConnection();
 	        connection.setRequestProperty("User-Agent", USER_AGENT);
 	        connection.connect();
 	        
 	        // Récupération de la page de résultat de la requète
 	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
 	        if ( resultFile != null ) {
 	        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));
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
 			throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+url, e);
 		} finally {
 			if ( reader != null ) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+url, e);
 				}
 			}
 			if ( writer != null ) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 					throw new OpenSubtitlesException("Erreur pendant l'exécution de la requète : "+url, e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Retourne le contenu d'un fichier sous forme de chaîne
 	 * @param fileName
 	 * @return
 	 * @throws OpenSubtitlesException 
 	 */
 	public static String getFileContent(String fileName) throws OpenSubtitlesException {
 		BufferedReader reader = null;
         try {
 			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
 			String line = null;
 	        StringBuffer fileBuffer = new StringBuffer();
 	        while ( (line=reader.readLine()) != null ) {
 	        	fileBuffer.append(line+"\n");
 	        }
 	        
 	        return fileBuffer.toString();
 		} catch (IOException e) {
 			throw new OpenSubtitlesException("Erreur pendant la lecture du fichier : "+fileName, e);
 		} finally {
 			if ( reader != null ) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					throw new OpenSubtitlesException("Erreur pendant la lecture du fichier : "+fileName, e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Sauvegarde le contenu d'une URL dans un fichier
 	 * @param url
 	 * @param file
 	 * @return
 	 * @throws OpenSubtitlesException 
 	 */
 	public static void saveURL(URL url, File file) throws OpenSubtitlesException {
 		BufferedInputStream in = null;
 		BufferedOutputStream out = null;
         try {
         	// Création de la connexion
 			URLConnection connection = url.openConnection();
 	        connection.setRequestProperty("User-Agent", USER_AGENT);
 	        connection.connect();
         	
 	        // Enregistrement du fichier
 			saveInput(connection.getInputStream(), file);
 		} catch (Exception e) {
 			throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
 		} finally {
 			if ( in != null ) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
 				}
 			}
 			if ( out != null ) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+url+"' dans le fichier : "+file, e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Sauvegarde le contenu d'un input stream dans un fichier
 	 * @param in
 	 * @param file
 	 * @return
 	 * @throws OpenSubtitlesException 
 	 */
 	public static void saveInput(InputStream in, File file) throws OpenSubtitlesException {
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
 			throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 		} finally {
 			if ( in != null ) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 				}
 			}
 			if ( out != null ) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					throw new OpenSubtitlesException("Erreur pendant la sauvegarde dans le fichier : "+file, e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Télécharge le résultat passé en paramètre
 	 * @param bestResult
 	 * @throws OpenSubtitlesException 
 	 */
 	private void download(OpenSubtitlesResult bestResult) throws OpenSubtitlesException {
 		try {
 			// Enregistrement du zip dans un fichier temporaire
 			File file = File.createTempFile("opensubtitle", ".zip");
 			saveURL(bestResult.getDownloadURL(), file);
 			
 			// Extraction du fichier SRT
 			ZipFile zip = new ZipFile(file);
 			Enumeration<? extends ZipEntry> entries = zip.entries();
 			while ( entries.hasMoreElements() ) {
 				ZipEntry entry = entries.nextElement();
 				if ( entry.getName().endsWith(FORMAT_PARAM_VALUE) ) {
 					String fileName = request.getFolder() + File.separator + request.getFilename().substring(
 							request.getFilename().lastIndexOf(File.separator) + 1,
 							request.getFilename().lastIndexOf(".")) + "." + FORMAT_PARAM_VALUE;
 					saveInput(zip.getInputStream(entry), new File(fileName));
 					
 					OpenSubtitles.LOGGER.info("#####################################################################");
 					OpenSubtitles.LOGGER.info("# Sous-titre sauvegardé : "+fileName);
 					OpenSubtitles.LOGGER.info("#####################################################################");
 				}
 			}
 		} catch (IOException e) {
 			throw new OpenSubtitlesException("Erreur pendant la sauvegarde de l'URL '"+bestResult.getDownloadURL()+"'", e);
 		}
 	}
 	
 	/**
 	 * Récupère la liste des sous titres depuis la page de résultat de requète
 	 * @return
 	 * @throws OpenSubtitlesException 
 	 */
 	private List<OpenSubtitlesResult> getSubtitlesURLs() throws OpenSubtitlesException {
 		List<OpenSubtitlesResult> result = new ArrayList<OpenSubtitlesResult>();
 		
 		// Cas 1 : un seul résultat, on est déjà sur la bonne page
 		if ( isSubtitlesPage(queryResultPage) ) {
 			result.add(new OpenSubtitlesResult(request, queryResultPage));
 		}
 		// Cas 2 : plusieurs résultats, on est sur la liste des fichiers de sous-titres
 		else {
 			// Recherche des occurences du préfixe d'URL de sous-titre
 			int start = 0;
 			while ( (start = queryResultPage.indexOf(SUBTITLE_ID_START, start)) != -1 ) {
 				start += SUBTITLE_ID_START.length();
 				int end = queryResultPage.indexOf(SUBTITLE_ID_END, start);
 				String subtitleId = queryResultPage.substring(start, end);
 				try {
 					result.add(new OpenSubtitlesResult(request, new URL(OPEN_SUBTITLES_DOMAIN+SUBTITLE_URL_PREFIX+subtitleId)));
 				} catch (MalformedURLException e) {
 					throw new OpenSubtitlesException("URL invalide : "+OPEN_SUBTITLES_DOMAIN+SUBTITLE_URL_PREFIX+subtitleId);
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Retourne true si le contenu de la page passé en paramètres correspond à une page de sous-titres
 	 * @param pageContent
 	 * @return
 	 */
 	private boolean isSubtitlesPage(String pageContent) {
 		return pageContent.indexOf(SUBTITLES_PAGE_TEXT) != -1;
 	}
 
 	/**
 	 * Renseigne les différents champs à partir de la requète
 	 * @param request the request to set
 	 */
 	private void setRequest(OpenSubtitlesRequest request) throws OpenSubtitlesException {
 		this.request = request;
 		
 		// Construction de l'URL de la requète
 		StringBuffer queryURLBuffer = new StringBuffer();
 		queryURLBuffer.append(QUERY_URL_START);
 		if ( request.getLang() != null ) {
 			appendParameter(queryURLBuffer, LANGUAGE_PARAM_NAME, request.getLang());
 		} else {
 			throw new OpenSubtitlesException("La langue est obligatoire pour le fichier "+request.getFilename());
 		}
 		if ( request.getSeason() != null ) {
 			appendParameter(queryURLBuffer, SEASON_PARAM_NAME, request.getSeason());
 		}
 		if ( request.getEpisode() != null ) {
 			appendParameter(queryURLBuffer, EPISODE_PARAM_NAME, request.getEpisode());
 		}
 		appendParameter(queryURLBuffer, FORMAT_PARAM_NAME, FORMAT_PARAM_VALUE);
 		if ( request.getQuery() != null ) {
 			try {
 				appendParameter(queryURLBuffer, MOVIE_PARAM_NAME, URLEncoder.encode(request.getQuery(), "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				throw new OpenSubtitlesException("Erreur pendant l'encodage de '"+request.getQuery()+"' pour l'URL", e);
 			}
 		} else {
 			throw new OpenSubtitlesException("La requète est obligatoire pour le fichier "+request.getFilename());
 		}
 		try {
 			this.queryURL = new URL(queryURLBuffer.toString());
 		} catch (MalformedURLException e) {
 			throw new OpenSubtitlesException("URL générée invalide : "+queryURLBuffer.toString(), e);
 		}
 	}
 	
 	/**
 	 * Ajoute un paramètre à la requète
 	 * @param buf
 	 * @param param
 	 * @param value
 	 */
 	private void appendParameter(StringBuffer buf, String param, Object value) {
 		buf.append(PARAM_SEPARATOR);
 		buf.append(param);
 		buf.append(PARAM_NAME_VALUE_SEPARATOR);
 		buf.append(value);
 	}
 	
 	/*
 	 * GETTERS & SETTERS
 	 */
 	
 	/**
 	 * @return the request
 	 */
 	public OpenSubtitlesRequest getRequest() {
 		return request;
 	}
 
 	/**
 	 * @return the queryURL
 	 */
 	public URL getQueryURL() {
 		return queryURL;
 	}
 
 	/**
 	 * @return the queryResultPage
 	 */
 	public String getQueryResultPage() {
 		return queryResultPage;
 	}
 
 	/**
 	 * @return the subtitlesResults
 	 */
 	public List<OpenSubtitlesResult> getSubtitlesResults() {
 		return subtitlesResults;
 	}
 }
