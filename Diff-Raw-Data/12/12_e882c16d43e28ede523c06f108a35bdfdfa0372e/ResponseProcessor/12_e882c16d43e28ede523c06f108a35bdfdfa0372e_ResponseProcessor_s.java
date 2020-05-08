 package org.tophat.android.networking;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.util.EntityUtils;
 import org.tophat.android.exceptions.Forbidden;
 import org.tophat.android.exceptions.HttpException;
 import org.tophat.android.exceptions.NotFound;
 import org.tophat.android.exceptions.Unauthorised;
 
 public class ResponseProcessor {
 
 	public ResponseProcessor()
 	{}
 	
 	public String process(HttpResponse response) throws HttpException, ParseException, IOException
 	{
 		HttpEntity entity = response.getEntity();
 		
 		if( response.getStatusLine().getStatusCode() < 300 )
 		{
 			return entityToString(entity);
 		}
 		else if ( response.getStatusLine().getStatusCode() == 401 )
 		{
 			throw new Unauthorised(entityToString(entity));
 		}
 		else if ( response.getStatusLine().getStatusCode() == 403 )
 		{
 			throw new Forbidden(entityToString(entity));
 		}
 		else if ( response.getStatusLine().getStatusCode() == 404 )
 		{
 			throw new NotFound(entityToString(entity));
 		}
 		else
 		{
 			throw new HttpException("HTTP Error"+response.getStatusLine().getStatusCode(), entityToString(entity));
 		}
 	}
 	
 	private String entityToString(HttpEntity entity) throws ParseException, IOException
 	{
 		return EntityUtils.toString(entity).replaceAll("\\p{Cntrl}", "");
 	}
 }
