 package com.eucsoft.beeper.http;
 
 import java.io.File;
 
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

 import android.os.AsyncTask;
 
import com.eucsoft.beeper.application.ApplicationContext;
 import com.eucsoft.beeper.file.FileUtil;
 
 public class SendMessageTask extends AsyncTask<String, Void, File> {
 
 	@Override
 	protected File doInBackground(String... params) {

 		HttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(ApplicationContext.serverUrl + "/"+ ApplicationContext.uploadUrl);
 
 		String filePath = params[0];
 		File file = new File(filePath);
 		String userId = params[1];
 		String clientId = params[2];
 
/*		try {
 			MultipartEntity entity = new MultipartEntity();
 			FileBody fileBody = new FileBody(file);
 			entity.addPart("file", fileBody);
 			entity.addPart("userId", new StringBody(userId, Charset.forName("UTF-8")));
 			entity.addPart("channelId", new StringBody(clientId, Charset.forName("UTF-8")));
 
 			httpPost.setEntity(entity);
 			httpClient.execute(httpPost);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
*/
 		return file;
 	}
 
 	@Override
 	protected void onPostExecute(File file) {
 		super.onPostExecute(file);
 		FileUtil.deleteFile(file);
 	}
 
 }
