 /*
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  * <p/>
  * The software source code is proprietary and confidential information of GigaSpaces.
  * You may use the software source code solely under the terms and limitations of
  * the license agreement granted to you by GigaSpaces.
  */
 
 package beans.cloudify;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.libs.WS;
 
 /**
  * User: guym
  * Date: 4/24/13
  * Time: 12:38 PM
  *
  * Implements REST calls to cloudify
  */
 public class CloudifyRestClient {
 
     public String TEST_REST_FORMAT = "http://%s:8100/service/testrest";
     public String GET_REST_VERSION_FORMAT = "http://%s:8100/service/testrest"; // we are using the test rest error message to parse the version
     public String LIST_APPLICATIONS_FORMAT = "http://%s:8100/service/applications";
     public String LIST_SERVICES_FORMAT = "http://%s:8100/service/applications/%s/services";
     public String DESCRIBE_SERVICES_FORMAT = "http://%s:8100/service/applications/%s/services/description";
     public String GET_PUBLIC_IP_FORMAT= "http://%s:8100/admin/ProcessingUnits/Names/%s.%s/Instances/0/ServiceDetailsByServiceId/USM/Attributes/Cloud%%20Public%%20IP";
 
     private static Logger logger = LoggerFactory.getLogger( CloudifyRestClient.class );
 
     public CloudifyRestResult.TestRest testRest( String ip )
     {
         return getResult( CloudifyRestResult.TestRest.class, TEST_REST_FORMAT, ip  );
     }
 
     public CloudifyRestResult.GetVersion getVersion( String ip )
     {
        return parse( CloudifyRestResult.GetVersion.class, getBody( WS.url( String.format( GET_REST_VERSION_FORMAT, ip ) ).setHeader( "cloudify-api-version", CloudifyRestResult.GetVersion.DUMMY_VERSION ) )  );
     }
 
     public CloudifyRestResult.ListApplications listApplications( String ip ){
         return getResult( CloudifyRestResult.ListApplications.class, LIST_APPLICATIONS_FORMAT, ip );
     }
 
     public CloudifyRestResult.ListServices listServices( String ip, String application ){
         return getResult( CloudifyRestResult.ListServices.class, LIST_SERVICES_FORMAT, ip, application );
     }
 
     public CloudifyRestResult.GetPublicIpResult getPublicIp( String ip, String application, String service ){
         return getResult( CloudifyRestResult.GetPublicIpResult.class, GET_PUBLIC_IP_FORMAT, ip, application, service );
     }
 
     private <T> T getResult ( Class<T>  clzz, String format, String ... args ){
         return parse(clzz, getBody( format, args ));
     }
 
     private String getBody( String format, String ... args ){
        String url = String.format( format, args );
        return getBody( WS.url( url ) );
     }
 
     private String getBody(WS.WSRequestHolder requestHolder ){
         return requestHolder.get().get().getBody();
     }
 
     private <T> T parse( Class<T> clzz, String body ){
         try{
         if ( org.apache.commons.lang3.StringUtils.isEmpty( body ) ) {
             logger.info( "body is empty" );
             return null;
         } else {
             ObjectMapper mapper = new ObjectMapper(  );
             return mapper.readValue( body, clzz );
         }
         }catch(Exception e){
             logger.error( "problems parsing class [{}] json [{}] ", new Object[]{clzz, body, e} );
             throw new RuntimeException( String.format("problems parsing class [%s] json [%s]", clzz, body), e );
         }
 
     }
 
 
 
 }
