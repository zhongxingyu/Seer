 package uk.co.ioko.tapestry.swfObject.services;
 
 import org.apache.tapestry5.ioc.Configuration;
 import org.apache.tapestry5.services.LibraryMapping;
 
 /**
  * Created by IntelliJ IDEA. User: ben Date: Jun 8, 2009 Time: 4:13:00 PM
  *
  * It is delibrate we have not mapped the library as now js files are assembled it
  * doesn't add much.
  */
 public class SwfModule {
 
 	public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration) {
 		configuration.add(new LibraryMapping("ioko", "uk.co.ioko.tapestry.swfObject"));
 	}

	public static void contributeResponseCompressionAnalyzer(Configuration<String> configuration){
		configuration.add("application/x-shockwave-flash");

	}
 }
