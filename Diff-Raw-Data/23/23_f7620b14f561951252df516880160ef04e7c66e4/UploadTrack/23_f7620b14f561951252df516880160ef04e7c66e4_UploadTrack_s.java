 package fm.audiobox.core.models;
 
 import java.io.IOException;
 
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.content.FileBody;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.AudioBoxClient.AudioBoxConnector;
 
 public class UploadTrack extends Track {
 
     public UploadTrack() {
         super();
     }
 
     public void setFileBody( FileBody fb ){
         this.fileBody = fb;
     }
 
 
     public void upload() throws IOException, ServiceException, LoginException {
     	String result[] = this.pConnector.execute(Tracks.END_POINT , null, null, this, HttpPost.METHOD_NAME);
     	this.setUuid( result[ AudioBoxConnector.RESPONSE_BODY ]  );
     }
     
     
 
 }
