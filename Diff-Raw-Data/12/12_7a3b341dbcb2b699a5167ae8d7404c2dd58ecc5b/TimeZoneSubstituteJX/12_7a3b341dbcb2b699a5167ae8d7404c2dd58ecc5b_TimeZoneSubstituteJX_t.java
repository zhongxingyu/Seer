 package org.mulgara.store.jxunit;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import net.sourceforge.jxunit.JXProperties;
 import net.sourceforge.jxunit.JXTestCase;
 import net.sourceforge.jxunit.JXTestStep;
 
 public class TimeZoneSubstituteJX implements JXTestStep {
 	
 	public final static String TIMEDATE = "timedate";
 	public final static String TOKEN = "token";
 	public final static String TEMPLATE = "template";
 	public final static String QUERYRESULT = "queryResult";
 	public final static String PROPERTY= "returnProperty";
 
 	public void eval(JXTestCase testCase) throws Throwable {
 		JXProperties props = testCase.getProperties();
 
 		boolean success = false;
 		
 		String timedate = checkGetValue( props, TIMEDATE );
 		String token = checkGetValue( props, TOKEN );
 		String template = checkGetValue( props, TEMPLATE );
 		String queryResult = checkGetValue( props, QUERYRESULT );
 		String returnProperty = checkGetValue( props, PROPERTY );
 		
		System.out.println("timedate: " + timedate );
		System.out.println("token: " + token );
		System.out.println("template: " + template );
		System.out.println("queryResult: " + queryResult );
		
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 		Date d = sdf.parse( timedate );
 		sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
 		String localTime = sdf.format(d);
 		
		System.out.println("localTime: " + localTime );
 		
		success = queryResult.equals( template.replace( token, localTime ) );

		System.out.println("Success: " + success );
 		if( success ) {
 			props.put( returnProperty, "true" );
 		}
 	}
 
 	private String checkGetValue( JXProperties props, String propertyName ) {
 		String retValue = props.getString( propertyName );
 		
 		if( retValue == null ) {
 			throw new IllegalStateException("Missing expected property: " + propertyName );
 		}
 		
 		return retValue;
 	}
 }
