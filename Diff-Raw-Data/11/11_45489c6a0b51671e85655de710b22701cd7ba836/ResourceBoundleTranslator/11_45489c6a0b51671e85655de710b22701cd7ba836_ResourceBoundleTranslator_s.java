 package com.tracktopell.util;
 
 import com.google.gson.Gson;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.util.*;
 import java.util.Map.Entry;
 
 
 /**
  * ResourceBoundleTranslator
  *
  */
 public class ResourceBoundleTranslator {
 
 	private static final String BABELFISH_TRANSLATE_SERVICE_URL = "http://babelfish.yahoo.com/translate_txt";
 	private static final String MICROSOFTTRASNLATOR_TRANSLATE_MAIN_URL = "http://www.microsofttranslator.com";
 	private static final String MICROSOFTTRASNLATOR_TRANSLATE_SERVICE_URL = "http://api.microsofttranslator.com/v2/ajax.svc/TranslateArray";
   	private static       String MICROSOFTTRASNLATOR_APPID = "TkYkjmWgZci6n3xLJw-E9OqM0VROIlzTHPNYC0Awd0VI*";
 
 	public static void main(String[] args) {
 
 		if (args.length != 3 && (args.length < 4 || args.length > 6)) {
 			printUsageAndExit();
 		}
 
 		String rbSource = null;
 		String rbTarget = null;
 		String sourceLang = null;
 		String targetLang = null;
 
 		//boolean twoWaysComplement = false;
 		if (args.length == 3) {
 			if (args[2].equals("-printDiferences")) {
 				try {
 					printDiferencesInKeys(rbSource, rbTarget);
 					System.exit(0);
 				} catch (IOException ex) {
 					ex.printStackTrace(System.err);
 					System.exit(2);
 				}
 			} else {
 				System.err.println("Unknow argument: " + args[2]);
 				printUsageAndExit();
 			}
 		}
 
 
 		rbSource = args[0];
 		rbTarget = args[1];
 		sourceLang = args[2];
 		targetLang = args[3];
 
 		String[] preferedSynonimsArr = new String[0];
 		String[] rangeArr = new String[0];
 
 		if (args.length == 5 || args.length == 6) {
 			int ax = 4;
 			for (ax = 4; ax < args.length; ax++) {
 
 				if (args[ax].startsWith("-preferredSynonims=")) {
 					preferedSynonimsArr = args[ax].split("=")[1].replace("[", "").replace("]", "").split(",");
				} else if (args[ax].startsWith("-ofSet=")) {
 					rangeArr = args[ax].split("=")[1].replace("[", "").replace("]", "").split(",");
 				} else {
 					System.err.println("Unknow option: " + args[ax]);
 					printUsageAndExit();
 				}
 			}
 
 		}
 
 		try {
 			Hashtable<String, String> preferedSyninims = new Hashtable<String, String>();
 			for (String pfkv : preferedSynonimsArr) {
 				String[] kv = pfkv.split(":");
 				preferedSyninims.put(kv[0], kv[1]);
 			}
 
 			List<PropertiesRange> rangeList = new ArrayList<PropertiesRange>();
 			int totalLines = countLines(rbSource);
 			for (String rangeExpr : rangeArr) {
 				PropertiesRange rangeScanned = PropertiesRange.parse(totalLines, rangeExpr);
 				rangeList.add(rangeScanned);
 				//System.err.println("-->> + rangeScanned ="+rangeScanned);
 			}
 
 			translateResourceBoundle(rbSource,rbTarget, sourceLang, targetLang,preferedSyninims,rangeList);
 
 		} catch (IOException ex) {
 			ex.printStackTrace(System.err);
 			System.exit(3);
 		}
 	}
 
 	private static void printUsageAndExit() {
 		System.err.println("\t\t\t\t\t\tResourceBoundleTranslator");
 		System.err.println("\tTool for automated Java ResourceBundle Translation using MicrosoftTrasnlator(GET Http Service URL).");
 		System.err.println("\t@Author: tracktopell@gmail.com");
 		System.err.println("Usage: ");
 		System.err.println("\t${JVM}  com.tracktopell.util.ResourceBoundleTranslator   ResurceBoundleSource  [ResurceBoundleTarget|-stdout]   -printDiferences ");
 		System.err.println("\t${JVM}  com.tracktopell.util.ResourceBoundleTranslator   ResurceBoundleSource  [ResurceBoundleTarget|-stdout]   sourceLang   targetLang  [ options ] ");		
 		System.err.println();
 		System.err.println(" Where options are :");
 		System.err.println("\t  -preferredSynonimsArr=[key_1:value_1,key_2:value_2,...] :  List of phrases that prefer instead of the original translated word.");
		System.err.println("\t  -ofSet=[from:to,+first,-last,...] :  List of ranges or limit cuantifiers.");
 		//System.err.println("\t  -2WaysCompleemnt");
 		System.err.println("");
 		System.err.println(" Usage Examples:");
		System.err.println("\t${JVM}  com.tracktopell.util.ResourceBoundleTranslator   messages_en.properties  messages_es.properties en es -preferredSynonimsArr=[fichero:archivo,ordenador:computadora] -ofSet=[1:5,-10]");
 		System.err.println("\t");
 		System.err.println("\t${JVM}  com.tracktopell.util.ResourceBoundleTranslator   messages_de.properties  messages_en.properties de en -printDiferences");
 		System.err.println("\t");
		System.err.println("\t${JVM}  com.tracktopell.util.ResourceBoundleTranslator   messages_fr.properties  -stdout                fr es -ofSet=[-25]");
 		System.err.println("\t");
 		System.err.println();
 		System.exit(1);
 	}
 
 	private static void printDiferencesInKeys(final String rbSource, final String rbTarget) throws IOException {
 		Properties propertiesSource = new Properties();
 		Properties propertiesTarget = new Properties();
 
 		propertiesSource.load(new FileInputStream(rbSource));
 		if(!rbTarget.equalsIgnoreCase("-stdout")) {
 			propertiesTarget.load(new FileInputStream(rbTarget));
 		}
 
 		Properties propertiesMerge = new Properties();
 
 		propertiesMerge.putAll(propertiesSource);
 		propertiesMerge.putAll(propertiesTarget);
 
 		Set mergeKeySet = propertiesMerge.keySet();
 
 		for (Object ok : mergeKeySet) {
 			String key = ok.toString();
 
 			if (propertiesSource.containsKey(key) && !propertiesTarget.containsKey(key)) {
 				System.out.println(" >-- " + key);
 			} else if (!propertiesSource.containsKey(key) && propertiesTarget.containsKey(key)) {
 				System.out.println(" --< " + key);
 			}
 		}
 
 
 	}
 
 	private static void refreshAjaxApiId() throws IOException {
 		SimplePostRequest postMain = new SimplePostRequest(MICROSOFTTRASNLATOR_TRANSLATE_MAIN_URL);
 		
 		String mainHtml = postMain.getGETFullResponseAsString();
 		int appIdBI = mainHtml.indexOf("Default.Constants.AjaxApiAppId");
 		if(appIdBI >0){
 			int appIdES = mainHtml.indexOf("'" ,appIdBI);
 			int appIdEI = mainHtml.indexOf("';",appIdBI);
 			String loadedAppId = mainHtml.substring(appIdES+1, appIdEI);
 			//System.err.println("-->> dynamicLoadAppId: ->"+loadedAppId+"<-");
 			MICROSOFTTRASNLATOR_APPID = loadedAppId;
 		} else {
 			//System.err.println("--->> Read , but not founf AppId: from "+MICROSOFTTRASNLATOR_TRANSLATE_MAIN_URL+" : "+mainHtml);
 			throw new IllegalStateException("Read , but not founf AppId: from "+MICROSOFTTRASNLATOR_TRANSLATE_MAIN_URL);
 		}
 	}
 
 	private static void translateResourceBoundle(
 			final String rbSource, final String rbTarget, final String sourceLang, final String targetLang,
 			final Hashtable<String, String> preferedSyninims, final List<PropertiesRange> rangeList) throws IOException {
 
 		LinkedHashMap<String, String> propetiesSource = new LinkedHashMap<String, String>();
 		Properties propetiesTarget = new Properties();
 		LinkedHashMap<String, String> propetiesResult = new LinkedHashMap<String, String>();
 
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rbSource)));
 
 		//System.err.println("======>> ranges="+rangeList+", explore the source:");
 
 		String line = null;
 		for (int lineNumber = 1; (line = br.readLine()) != null; lineNumber++) {
 			String[] kv = line.split("=");
 			if (kv != null && kv.length == 2) {
 				String k = kv[0].trim();
 				String v = kv[1].trim();
 
 				boolean validInRange = false;
 
 				for (PropertiesRange pr : rangeList) {
 					validInRange = pr.isValidLineNumber(lineNumber);
 					if (validInRange) {
 						break;
 					}
 				}
 				if (validInRange) {
 					propetiesSource.put(k, v);
 				}
 			}
 		}
 		br.close();
 		if(!rbTarget.equalsIgnoreCase("-stdout")) {
 			propetiesTarget.load(new FileInputStream(rbTarget));
 		}
 
 		Set<Entry<String, String>> sourceEntrySet = propetiesSource.entrySet();
 		StringBuffer phrasesToTranslate = new StringBuffer();
 		ArrayList<String> phrasesToTranslateArr = new ArrayList<String>();
 		int numPhrasesToTranslate = 0;
 		for (Entry<String, String> e : sourceEntrySet) {			
 			if (numPhrasesToTranslate > 0) {
 				phrasesToTranslate.append(" # ");
 			}
 			phrasesToTranslate.append(e.getValue().toString());
 			phrasesToTranslateArr.add(e.getValue().toString());
 			numPhrasesToTranslate++;
 		}
 		String translatedResult = translateUsingMicrosoftTranslator(phrasesToTranslate.toString(), sourceLang, targetLang);		
 		String[] trasnlatedPhrases = translatedResult.split("#");
 
 		if(sourceEntrySet.size() != trasnlatedPhrases.length) {
 			throw new IllegalStateException("Num Source phrases not equal to result num phrases: "+sourceEntrySet.size()+" != "+trasnlatedPhrases.length+
 											"\n\tphrasesToTranslate->"+phrasesToTranslate.toString().replace("#", "\n") +"<- \ntranslatedResult->"+translatedResult.toString().replace("#", "\n")+"<-, ");
 		}
 		
 		numPhrasesToTranslate = 0;
 		//System.err.println("-->> sourceEntrySet.size(): "+sourceEntrySet.size()+", trasnlatedPhrases.length="+trasnlatedPhrases.length);
 		for (Entry<String, String> e : sourceEntrySet) {
 			String phraseTrasnletedToPut = trasnlatedPhrases[numPhrasesToTranslate].trim();
 
 			Set<String> keySyns = preferedSyninims.keySet();
 			if(keySyns.size()>0){
 				System.err.print("-->> Searching for replace Synonim: ");
 				for (String s : keySyns) {
 					System.err.print("-->> "+phraseTrasnletedToPut+".contains("+s+") ?");
 					if (phraseTrasnletedToPut.contains(s)) {
 						System.err.print("OK :<"+preferedSyninims.get(s)+"> ");
 						phraseTrasnletedToPut = phraseTrasnletedToPut.replace(s, preferedSyninims.get(s));
 					}
 				}
 				//System.err.println();
 			}
 			propetiesResult.put(e.getKey().toString(), phraseTrasnletedToPut);
 			numPhrasesToTranslate++;
 		}
 
 		//System.err.println("==>>> propetiesResult="+propetiesResult.toString().replace(", ", ", \n"));
 
 		serializeLike(rbSource, rbTarget, propetiesResult, rangeList);
 	}
 
 	/**
 	 * Babelfish moves to Microsoft Translator
 	 * @deprecated
 	 */
 	private static String trasnlateUsingBabelfish(String phrase, String sourceLang, String targetLang) throws IOException {
 
 		SimplePostRequest post = new SimplePostRequest(BABELFISH_TRANSLATE_SERVICE_URL);
 
 		post.add("btnTrTxt", "Translate");
 		post.add("doit", "done");
 		//post.add("ei", "ISO-8859-1");
 		post.add("ei", "UTF-8");
 		post.add("fr", "bf-home");
 		post.add("intl", "1");
 		post.add("lp", sourceLang + "_" + targetLang);
 		post.add("trtext", phrase);
 		post.add("tt", "urltext");
 
 		//System.err.println("-->> trasnlateUsingBabelfish: lp=\""+sourceLang + "_" + targetLang+"\", trtext=\""+phrase+"\"");
 
 		String respuesta = post.getPOSTFullResponseAsString();
 
 		String divResult = "<div id=\"result\">";
 
 		int divBegin = respuesta.indexOf(divResult);
 		if (divBegin < 0) {
 			System.err.println(respuesta);
 			System.err.println("-->> trasnlateUsingBabelfish: lp=\""+sourceLang + "_" + targetLang+"\", trtext=\""+phrase+"\"");
 			throw new IOException("Error in translation: result div not found !");
 		}
 		int divEnd = respuesta.indexOf("</div>", divBegin);
 		String divBody = respuesta.substring(divBegin, divEnd);
 		String translatedText = divBody.substring(divBody.lastIndexOf(">") + 1);
 
 		return translatedText;
 	}
 	
 	public static String translateUsingMicrosoftTranslator(String phrase, String sourceLang, String targetLang) throws IOException {
 		
 		refreshAjaxApiId();
 		
 		SimplePostRequest post = new SimplePostRequest(MICROSOFTTRASNLATOR_TRANSLATE_SERVICE_URL);
 
 		post.add("appId","\""+MICROSOFTTRASNLATOR_APPID+"\"");
 		post.add("from","\""+sourceLang+"\"");
 		post.add("texts","	[\""+phrase+"\"]");
 		post.add("to","\""+targetLang+"\"");
 
 		String respuesta = post.getGETFullResponseAsString();
 		
 		Gson gson = new Gson();
 		MicrosoftTranslatorResult[] mrArray = new MicrosoftTranslatorResult[1];
 		
 		//System.err.println("-->> trasnlateUsingMicrosoftTrasnlator: respuesta=->"+respuesta + "<-");
 		
 		mrArray = gson.fromJson(respuesta, mrArray.getClass());
 		
 		//System.err.println("-->MicrosoftTranslatorResult: "+mrArray[0].TranslatedText);
 
 		String translatedText = mrArray[0].TranslatedText;
 
 		return translatedText;
 	}
 
 
 	private static void serializeLike(String rbSource, String rbTarget, LinkedHashMap<String, String> propetiesResult,final List<PropertiesRange> rangeList) throws IOException {
 		Properties originalTargetProperties = new Properties();
 		PrintStream ps = null;
 		
 		BufferedReader brSource = new BufferedReader(new InputStreamReader(new FileInputStream(rbSource)));		
 		
 		if(!rbTarget.equalsIgnoreCase("-stdout")) {
 			originalTargetProperties.load(new FileInputStream(rbTarget));
 			ps = new PrintStream(rbTarget);
 		} else {
 			ps = System.out;
 		}
 		
 		
 		String lineSource = null;
 
 		for(int lineNumber = 1;(lineSource = brSource.readLine()) != null; lineNumber++ ) {
 			String[] kv = lineSource.split("=");
 			if (kv != null && kv.length == 2) {
 				String k = kv[0].trim();
 				String v = kv[1].trim();
 				
 				boolean validInRange = false;
 
 				for (PropertiesRange pr : rangeList) {
 					validInRange = pr.isValidLineNumber(lineNumber);
 					if (validInRange) {
 						break;
 					}
 				}
 				if (validInRange && propetiesResult.containsKey(k)) {
 					lineSource = k + "=" + encodeToJavaLiteral(propetiesResult.get(k).toString());
 				} else if (originalTargetProperties.containsKey(k)) {
 					String originalValue = originalTargetProperties.getProperty(k);
 					encodeToJavaLiteral(originalValue);
 					lineSource = k + "=" + originalValue;
 				} else {
 					lineSource = k + "=" + encodeToJavaLiteral(v);					
 				}
 			}
 			ps.println(lineSource);
 		}
 		brSource.close();
 		ps.close();
 	}
 
 	private static int countLines(String fileName) throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
 
 		String line = null;
 		int numLines = 0;
 		while ((line = br.readLine()) != null) {
 			numLines++;
 		}
 		br.close();
 		return numLines;
 	}
 
 	private static String encodeToJavaLiteral(String str) {
 		String charEncoded = null;
 		byte[] bytes = str.getBytes();
 		int byteEncoded = 0;
 		StringBuffer result = new StringBuffer();
 
 		for (byte b : bytes) {
 
 			if (b < 0) {
 
 				if (byteEncoded == 0) {
 					byteEncoded = b;
 				} else {
 
 					String s1 = Integer.toHexString(byteEncoded).substring(6);
 					String s2 = Integer.toHexString(b).substring(6);
 
 					int byteEncodedJoin = Integer.parseInt(s1 + s2, 16);
 
 					byteEncoded = byteEncodedJoin;
 
 					charEncoded = "\\u00" + Integer.toHexString(byteEncoded - 0xC2C0).toUpperCase();
 					result.append(charEncoded);
 					charEncoded = null;
 					byteEncoded = 0;
 				}
 			} else {
 				result.append((char) b);
 			}
 		}
 		return result.toString();
 	}
 }
 class PropertiesRange {
 
 	private int total;
 	private int from;
 	private int to;
 	private int first;
 	private int last;
 
 	private PropertiesRange(int total) {
 		this.total = total;
 	}
 
 	public boolean isValidLineNumber(int lineNumber) {
 		return lineNumber >= from && lineNumber <= to;
 	}
 
 	public static PropertiesRange parse(int total, String expression) {
 		PropertiesRange parsed = new PropertiesRange(total);
 
 		if (expression.contains(":")) {
 			String[] ft = expression.split(":");
 			parsed.from = Integer.parseInt(ft[0]);
 			parsed.to   = Integer.parseInt(ft[1]);
 			if (parsed.from <= parsed.to && parsed.from <= total) {
 				return parsed;
 			} else {
 				throw new IndexOutOfBoundsException("Range expression : total=" + total + ",expression=" + expression);
 			}
 
 		} else if (expression.startsWith("+")) {
 			parsed.first = Integer.parseInt(expression.substring(1));
 			parsed.from  = 1;
 			parsed.to    = parsed.first;
 			if (parsed.from <= parsed.to && parsed.from <= total) {
 				return parsed;
 			} else {
 				throw new IndexOutOfBoundsException("First expression : total=" + total + ",expression=" + expression);
 			}
 		} else if (expression.startsWith("-")) {
 			parsed.last = Integer.parseInt(expression.substring(1));
 			parsed.from = (parsed.total - parsed.last)+1;
 			parsed.to = parsed.total;
 			if (parsed.from <= parsed.to && parsed.from <= total) {
 				return parsed;
 			} else {
 				throw new IndexOutOfBoundsException("Last expression : total=" + total + ",expression=" + expression);
 			}
 		} else {
 			throw new IllegalArgumentException("Unknow expression : total=" + total + ",expression=" + expression);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "PropertiesRange {[" + from + "," + to + "]:" + total + "}";
 	}
 }
