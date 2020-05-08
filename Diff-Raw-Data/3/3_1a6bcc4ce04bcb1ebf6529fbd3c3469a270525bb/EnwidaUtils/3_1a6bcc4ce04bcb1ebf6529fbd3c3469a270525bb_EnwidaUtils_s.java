 package de.enwida.web.utils;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import de.enwida.transport.Aspect;
 import de.enwida.transport.DataResolution;
 
 public class EnwidaUtils {
 
 	public static DataResolution getDataResolution(String legacyDataResolution) {
 		
 		DataResolution dR = null;		
 		switch (legacyDataResolution.trim()) {
 		case "15min":
 			dR = DataResolution.QUATER_HOURLY;
 			break;
 		case "1h":
 			dR = DataResolution.HOURLY;
 			break;
 		case "1d":
 			dR = DataResolution.DAILY;
 			break;
 		case "1w":
 			dR = DataResolution.WEEKLY;
 			break;
 		case "1m":
 			dR = DataResolution.MONTHLY;
 			break;
 		case "1a":
 			dR = DataResolution.YEARLY;
 			break;		
 		}
 		
 		return dR;
 	}
 	
 	public static String getTableNameByAspect(Aspect aspect) {
 		
 		String tableName = null;
 		switch (aspect) {
 		case CR_ACTIVATION_FREQUENCY:
 		case CR_POWERPRICE_MIN:
 		case CR_POWERPRICE_MAX:
 		case CR_POWERPRICE_MID:
 		case CR_DEGREE_OF_ACTIVATION:
 		case CR_VOL_ACCEPTED:
 		case CR_VOL_ACTIVATION:
 		case CR_VOL_ACTIVATION2:	
 		case CR_VOL_OFFERED:
 		case CR_WORKPRICE_ACC_MIN:
 		case CR_WORKPRICE_ACC_MAX:
 		case CR_WORKPRICE_ACC_MID:
 		case CR_WORKPRICE_MARG_MAX:
 		case CR_WORKPRICE_MARG_MID:
 			tableName = "analysis";
 			break;
 			
 		case CR_POWERPRICE_ACCEPTED:
 		case CR_POWERPRICE_REJECTED:
 		case CR_WORKPRICE_ACCEPTED:
 		case CR_WORKPRICE_REJECTED:
 			tableName = "auction_results";
 			break;
 		
 		case CR_REVENUES:			
 			break;
 		default:
 			break;
 		}
 		
 		return tableName;
 	}
 	
 	public static String getStringFromInputStream(InputStream is) {
 		 
 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();
  
 		String line;
 		try {
  
 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
  
 		return sb.toString();
  
 	}
 	
 	private SecureRandom random = new SecureRandom();
 
     public String getActivationId()
     {
       return new BigInteger(130, random).toString(32);
     }
 
 	/**
 	 * Writes given content to a file.
 	 * 
 	 * @param file
 	 *            the file to write to
 	 * @param text
 	 *            the text to write.
 	 */
 	public static void writeAllText(File file, String text) throws Exception {
 		BufferedWriter out = new BufferedWriter(new FileWriter(file));
 		out.write(text);
 		out.close();
 	}
 
 	/**
 	 * Creates a new directory depending on the input path given
 	 * 
 	 * @param path
 	 *            directory path that needs to be created
 	 * @return success status of directory creation
 	 */
 	public static boolean createDirectory(String path) {
 		boolean status = false;
 
 		if (path != null && !path.trim().isEmpty()) {
 			File f = new File(path);
 			f.setWritable(true);
 
 			if (!f.exists()) {
 				status = f.mkdirs();
 			}
 		}
 		return status;
 	}
 
 	/**
 	 * @param completeName
 	 * @return
 	 */
 	public static String extractFileFormat(String completeName) {
 		String[] fnameParts = completeName.split("\\\\");
 		// String fname = fnameParts[fnameParts.length - 1];
 		// this.displayFileName = fname;
 
 		fnameParts = completeName.split("\\.");
 		if (fnameParts.length < 2) {
 			// file without format.
 			return null;
 		}
 
 		return fnameParts[fnameParts.length - 1];
 	}
 
 	/**
 	 * @param completeName
 	 * @return
 	 */
 	public static String extractFileName(String completeName) {
 		String[] fnameParts = completeName.split("\\\\");
 		return fnameParts[fnameParts.length - 1];
 	}
 
 	/**
 	 * @param fileTORemove
 	 * @return
 	 * @throws Exception
 	 */
 	public static boolean removeTemporaryFile(File fileTORemove)
 			throws Exception {
 		fileTORemove.delete();
 		return true;
 	}
 	
 	public static String md5(String input) {
         
         String md5 = null;
          
         if(null == input) return null;
          
         try {
              
         //Create MessageDigest object for MD5
         MessageDigest digest = MessageDigest.getInstance("MD5");
          
         //Update input string in message digest
         digest.update(input.getBytes(), 0, input.length());
  
         //Converts message digest value in base 16 (hex) 
         md5 = new BigInteger(1, digest.digest()).toString(16);
  
         } catch (NoSuchAlgorithmException e) {
  
             e.printStackTrace();
         }
         return md5;
     }
 	
 	/*
 	 * Returns input string with environment variable references expanded, e.g. $SOME_VAR or ${SOME_VAR}
 	 */
 	public static String resolveEnvVars(String input)
 	{
 	    if (null == input) {
 	        return null;
 	    }
 
 	    // match ${ENV_VAR_NAME} or $ENV_VAR_NAME
 	    final Pattern p = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)");
 	    final Matcher m = p.matcher(input); // get a matcher object
 		final StringBuffer sb = new StringBuffer();
 		while (m.find()) {
 	        String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
 	        String envVarValue = System.getenv(envVarName);
 			// System.out.println(envVarValue + " : " + envVarValue.length());
 			m.appendReplacement(
 					sb,
 					(null == envVarValue ? "" : Matcher
 							.quoteReplacement(envVarValue)));
 		}
 		m.appendTail(sb);
 		String result = sb.toString();
 		result = result.replaceAll("/",
 				Matcher.quoteReplacement(File.separator));
 		// System.err.println(result);
 		return result;
 	}
 }
