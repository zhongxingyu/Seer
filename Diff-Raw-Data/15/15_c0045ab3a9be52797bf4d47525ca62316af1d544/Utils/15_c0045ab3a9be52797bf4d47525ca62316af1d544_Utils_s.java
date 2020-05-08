 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipException;
 
 import models.ServerNode;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.Play;
 import play.cache.Cache;
 import play.i18n.Lang;
 import play.libs.Time;
 import play.mvc.Http;
 import server.exceptions.ServerException;
 
 /**
  * This class provides different static utility methods.
  * 
  * @author Igor Goldenberg
  */
 public class Utils
 {
 	final static String RECIPE_FOLDER = Play.application().path().getPath() + "/recipes";
 	private static Logger logger = LoggerFactory.getLogger( Utils.class );
 
 	/**
 	 * Download the archived recipe from url, save file it to local directory and unzip it.
 	 * NOTE: The recipe file must be archived in zip format.
 	 * 
 	 * @param recUrl the recipe url.
 	 * @param apiKey the apiKey this recipe belongs to
 	 * @return the local path to a recipe unzipped directory
 	 */
 	static public File downloadAndUnzip(String recUrl, String apiKey)
 	{
 		// create a unique directory for recipe zip file
 		File recDir = new File(RECIPE_FOLDER + File.separator + apiKey + File.separator + System.nanoTime());
 		if ( !recDir.mkdirs() )
 			throw new ServerException("Failed to create recipe directory: " + recDir);
 
 		try
 		{
 			URL recipeURL = new URL(recUrl);
 			File recipeLocalURL = new File(recDir, new File(recipeURL.getFile()).getName());
 
 			logger.info("Downloading recipe: {} tp {}",  recUrl, recipeLocalURL);
 
 			FileUtils.copyURLToFile(recipeURL, recipeLocalURL );
 
 			logger.info("Starting to extract recipe to: {}" , recipeLocalURL);
 
 			ZipUtils.unzipArchive( recipeLocalURL, recDir );
 
 			// returns the unzipped directory path, sometimes a zip file compressed without a root directory
 			File unzippedDir = new File(recipeLocalURL.getPath().substring(0, recipeLocalURL.getPath().lastIndexOf(".")));
 			if ( !unzippedDir.exists() )
 				return recDir;
 			else
 				return unzippedDir;
 		}catch(MalformedURLException e)
 		{
 			throw new ServerException("Wrong recipe url format: " + recUrl, e);
 		}catch(ZipException ex)
 		{
 			throw new ServerException(ex.getMessage());
 		}
 		catch (IOException e)
 		{
 			throw new ServerException("Failed to download recipe.", e);
 		}
 	}
 
 
 	// TODO : guy - need to use "future" or "promise" with "await"
 	// in play 2.0 this is called async result : https://github.com/playframework/Play20/wiki/JavaAsync
 	public static void threadSleep( long time )
 	{
 		try
 		{
 			Thread.sleep(time);
 		} catch (InterruptedException e) {}
 	}
 
 
 	public static List<String> split( String content, String regex )
 	{
 		if ( content == null )
 			return new ArrayList<String>(0);
 
 		String[] splitStr = content.split( regex );
 		return Arrays.asList( splitStr );
 	}
 
 	public static void addAllTrimmed( Collection<String> result, String[] values ){
 		if ( !CollectionUtils.isEmpty( values )){
 			for ( String value : values ) {
 				if ( !StringUtils.isEmptyOrSpaces( value )){
 					result.add( value.trim() );
 				}
 			}
 		}
 	}
 
 
 
      // guy - TODO _ this is an ugly method. it has 2 loops and an "if/else" pattern. We need to organize this somehow.
     // lets use a prototype bean that has a list (order matters) of filters.
     // each filter can return null - which means, remove this line.
     // the filters can have state - as they can be prototypes as well.
     // we can unite the "filterOutputLines" and "filterOutputStrings" to a regex.
 	/** Format string output by different patters */
 	public static List<String> formatOutput( String str, String substringPrefix , Collection<String> filterOutputLines, Collection<String> filterOutputStrings )
 	{
 		List<String> list = split(str, "\n");
 		for( int i=0; i < list.size(); i++ )
 		{
 			// remove by filter
 			for( String f : filterOutputLines )
 			{
 				if ( list.get(i).contains(f) )
 					list.set(i, "");
 			}
 
 			for( String f : filterOutputStrings )
 			{
 				String s = list.get(i);
 				if ( s.contains( f ) )
 					list.set(i, StringUtils.replace(s, f, "").trim());
 			}
 
 			// remove by pattern
 			String s = list.get(i);
 			if ( s.contains(substringPrefix) )
 			{
 				int start = s.indexOf(substringPrefix);
 				String afterStr = s.substring(start + substringPrefix.length(), s.length()).trim();
 				list.set(i, afterStr);
 			}
 		}// for
 
 		// trim empty lines or with a single dot
 		List<String> newList = new ArrayList<String>();
 		for( String s : list )
 		{

             if ( s.toLowerCase().contains("operation failed")){
                 logger.debug("detected operation failed");
                 newList.add("Operation Failed");
                 break;
             }
             if ( s.startsWith("[") ){ // guy - ugly formatting logic..
                 String substr = s.substring( 1, s.indexOf("]"));
                 if ( substr.split("\\.").length > 2 ){ // detect lines that have [ip]
                     logger.debug("removing ip square-brackets from start of line");
                     continue;
                 }
             }
			if ( !s.equals("") && !s.equals(".") )
 				newList.add( s );
 		}
 
 		return newList;
 	}
 
 	public static File getFileByRelativePath( String relativePath ){
 		return Play.application().getFile( relativePath );
 	}
 
 	public static long parseTimeToMillis( String timeExpression ){
 		return Time.parseDuration( timeExpression ) * 1000 ;
 	}
 
 	//http://stackoverflow.com/questions/10381354/how-to-manipulate-session-request-and-response-for-test-in-play2-0
 	//https://groups.google.com/forum/#!msg/play-framework/2voRHg2ZXUY/7hPR7dV-V3AJ
 	public static void setupDummyContext(){
 		final Http.Request request = new Http.Request() {
 			@Override
 			public Http.RequestBody body()
 			{
 				return null;
 			}
 
 			@Override
 			public String uri()
 			{
 				return null;
 			}
 
 			@Override
 			public String method()
 			{
 				return null;
 			}
 
 			@Override
 			public String remoteAddress()
 			{
 				return null;
 			}
 
 			@Override
 			public String host()
 			{
 				return null;
 			}
 
 			@Override
 			public String path()
 			{
 				return null;
 			}
 
 			@Override
 			public List<Lang> acceptLanguages()
 			{
 				return null;
 			}
 
 			@Override
 			public List<String> accept()
 			{
 				return null;
 			}
 
 			@Override
 			public boolean accepts( String s )
 			{
 				return false;
 			}
 
 			@Override
 			public Map<String, String[]> queryString()
 			{
 				return null;
 			}
 
 			@Override
 			public Http.Cookies cookies()
 			{
 				return null;
 			}
 
 			@Override
 			public Map<String, String[]> headers()
 			{
 				return null;
 			}
 		};
 		Http.Context.current.set(new Http.Context(request, new HashMap <String, String>(),
 				new HashMap<String, String>()));
 	}
 
 	public static String extractIpFromBootstrapOutput(String output) {
 
 		String restUrlRegex= "Rest service is available at: (http[s]*://(.*):8100)";
 		Pattern restPattern = Pattern.compile(restUrlRegex);
 		Matcher restMatcher = restPattern.matcher(output);
 		if (!restMatcher.find()) {
 			return null;
 		}
 		String restUrl = restMatcher.group(1);
 		return restUrl.substring(restUrl.indexOf("//") + 2, restUrl.lastIndexOf(':'));
 	}
 
 	public static String requestToString( Http.RequestHeader requestHeader )
 	{
 		//        StringBuilder sb = new StringBuilder(  );
 		//        return sb.append( requestHeader.toString() )
 		//                .toString();
 		return requestHeader.toString();
 	}
 
 	public static String requestToString(){
 		Http.Context context = Http.Context.current();
 		if ( context != null && context.request() != null ){
 			return requestToString( context.request() );
 		}
 		return "N/A";
 	}
 
     public static void deleteCachedOutput( ServerNode serverNode ){
         Cache.set( "output-" + serverNode.getId() , null );
     }
 	
 	public static String getCachedOutput( ServerNode serverNode ) {
         StringBuilder stringBuilder = (StringBuilder) Cache.get("output-" + serverNode.getId() );
         String output = stringBuilder == null ? null : stringBuilder.toString();
 		return output;
 	}
 
     // return result if not null; otherwise return default
     public static <T> T getOrDefault(T result, T defaultResult) {
         return result == null ? defaultResult : result;
     }
 }
