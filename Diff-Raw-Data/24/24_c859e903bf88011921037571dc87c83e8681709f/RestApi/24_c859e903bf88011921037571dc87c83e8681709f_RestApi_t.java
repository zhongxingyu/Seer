 package com.usulful.jenkins;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 public class RestApi {
 
     private final String mainUrl;
 
     public RestApi(String mainUrl) {
         this.mainUrl = mainUrl;
     }
 
     public void deleteJob(final String jobName) throws IOException {
        delete(mainUrl + "job/" + jobName + "/doDelete");
    }

    public void stopBuild(final String jobName, final int number) throws IOException {
        delete(mainUrl + "job/" + jobName + "/" + number + "/stop");
    }

    private void delete(final String url) throws IOException {
        HttpPost delete = new HttpPost(url);
         HttpResponse response = new DefaultHttpClient().execute(delete);
        LoggerFactory.getLogger(this.getClass()).debug("DELETE " + url + " response:" + response.getStatusLine().getStatusCode());
     }
 }
