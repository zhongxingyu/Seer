 import com.amazonaws.*;
 import com.amazonaws.auth.*;
 import com.amazonaws.services.s3.*;
 import com.amazonaws.services.s3.model.*;
 import com.amazonaws.services.sqs.*;
 import com.amazonaws.services.sqs.model.*;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * Parameters needed to work with credentials.
  * @author Kyle Deewey
  */
public class CredentialParameters extends ParametersBase {
     // begin constants
     public static final String ACCESS_KEY_ID = "accessKey";
     public static final String SECRET_KEY_ID = "secretKey";
     // end constants
 
     // begin instance variables
     private final AWSCredentials credentials;
     private final AmazonS3 s3;
     private final AmazonSQS sqs;
     // end instance variables
 
     public CredentialParameters( Map< String, String > input ) throws ParameterException {
 	super( input );
 	credentials = makeCredentials();
 	s3 = makeS3();
 	sqs = makeSQS();
     }
 
     private static final Set< String > NEEDED_PARAMS =
 	new HashSet< String >() {
 	{
  	    add( ACCESS_KEY_ID );
 	    add( SECRET_KEY_ID );
 	}
     };
 
     public Set< String > getNeededParams() {
 	return NEEDED_PARAMS;
     }
 
     public Map< String, String > getOptionalParams() {
 	return new HashMap< String, String >();
     }
 
     private AWSCredentials makeCredentials() {
 	return new BasicAWSCredentials( param( ACCESS_KEY_ID ),
 					param( SECRET_KEY_ID ) );
     }
 
     public AWSCredentials getCredentials() {
 	return credentials;
     }
 
     private AmazonS3 makeS3() {
 	return new AmazonS3Client( getCredentials() );
     }
 
     public AmazonS3 getS3() {
 	return s3;
     }
 
     private AmazonSQS makeSQS() {
 	return new AmazonSQSClient( getCredentials() );
     }
     
     public AmazonSQS getSQS() {
 	return sqs;
     }
 
     public static CredentialParameters makeParameters()
 	throws IOException, ParameterException {
	return new CredentialParameters( ParametersBase.readMapFromFile() );
     }
 }
 	    
