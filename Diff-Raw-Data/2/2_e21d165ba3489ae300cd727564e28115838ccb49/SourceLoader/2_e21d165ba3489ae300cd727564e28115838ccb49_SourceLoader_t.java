 package info.plagiatsjaeger;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.mozilla.universalchardet.UniversalDetector;
 
 
 /**
  * Klasse zum Laden von Daten.
  * 
  * @author Andreas
  */
 public class SourceLoader
 {
 	public static final Logger	_logger				= Logger.getLogger(SourceLoader.class.getName());
 	private static final String	DEFAULT_CONTENTTYPE	= ConfigReader.getPropertyString("DEFAULTCONTENTTYPE");
 	private static String		_detectedCharset	= DEFAULT_CONTENTTYPE;
 
 	/**
 	 * Laedt den Text einer Webseite.
 	 * 
 	 * @param strURL
 	 * @return
 	 */
 	public String loadURL(String strURL)
 	{
 		return loadURL(strURL, true);
 	}
 
 	/**
 	 * Laed den Text einer Webseite.
 	 * 
 	 * @param strUrl
 	 * @return
 	 */
 	public String loadURL(String strUrl, boolean cleanUrl)
 	{
 		String result = "";
 		try
 		{
 			if (cleanUrl)
 			{
 				strUrl = cleanUrl(strUrl);
 			}
 			URL url = new URL(strUrl);
 
 			BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
 			byte[] array = IOUtils.toByteArray(inputStream);
 
 			_detectedCharset = guessEncoding(array);
 			result = Jsoup.parse(url.openStream(), _detectedCharset, strUrl).text();
 
 		}
 		catch (MalformedURLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			return "FAIL MalformedURLException";
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			return "FAIL UnsupportedEncodingException";
 		}
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 			return "FAIL IOException";
 		}
 		return result;
 	}
 
 	/**
 	 * Laed eine Datei.
 	 * 
 	 * @param filePath
 	 * @return
 	 */
 	public static String loadFile(String filePath)
 	{
 		String result = "";
 		FileInputStream inputStream = null;
 		DataInputStream dataInputStream = null;
 		StringBuilder stringBuilder = new StringBuilder();
 		String charset = "";
 		try
 		{
 			inputStream = new FileInputStream(filePath);
 			String line = "";
 			byte[] array = IOUtils.toByteArray(inputStream);
 			charset = guessEncoding(array);
 
			if (charset.equalsIgnoreCase("WINDOWS-1252"))
 			{
 				charset = "CP1252";
 			}
 			inputStream = new FileInputStream(filePath);
 			dataInputStream = new DataInputStream(inputStream);
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream, charset));
 			while ((line = bufferedReader.readLine()) != null)
 			{
 				if (stringBuilder.length() > 0)
 				{
 					stringBuilder.append("\n");
 				}
 				stringBuilder.append(line);
 			}
 
 		}
 		catch (FileNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			if (dataInputStream != null)
 			{
 				try
 				{
 					dataInputStream.close();
 				}
 				catch (IOException e)
 				{
 					_logger.fatal(e.getMessage(), e);
 					e.printStackTrace();
 				}
 				result = stringBuilder.toString();
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Bereinigt eine Url, sodass sie immer vollstaendig ist
 	 * 
 	 * @param dirtyUrl
 	 * @return result
 	 */
 	public static String cleanUrl(String dirtyUrl)
 	{
 		String result = "";
 		//dirtyUrl = dirtyUrl.replaceAll("www.", "");
 		dirtyUrl = dirtyUrl.replaceAll("http://", "");
 		dirtyUrl = dirtyUrl.replaceAll("https://", "");
 		result = "http://" + dirtyUrl;
 //		result = "http://www." + dirtyUrl;
 		_logger.info("Dirty-URL: " + dirtyUrl);
 		_logger.info("Clean-URL: " + result);
 		return result;
 	}
 
 	/**
 	 * Versucht Charset herauszufinden
 	 * 
 	 * @param bytes
 	 * @return Charset als String
 	 */
 	public static String guessEncoding(byte[] bytes)
 	{
 		UniversalDetector detector = new UniversalDetector(null);
 		detector.handleData(bytes, 0, bytes.length);
 		detector.dataEnd();
 		String encoding = detector.getDetectedCharset();
 		detector.reset();
 		if (encoding == null)
 		{
 			encoding = DEFAULT_CONTENTTYPE;
 		}
 		return encoding;
 	}
 
 }
