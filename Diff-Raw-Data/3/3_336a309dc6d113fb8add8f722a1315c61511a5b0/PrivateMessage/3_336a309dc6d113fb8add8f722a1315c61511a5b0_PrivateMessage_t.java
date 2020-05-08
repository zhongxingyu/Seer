 package com.flowdock.jenkins;
 
 import hudson.model.AbstractBuild;
 import hudson.scm.ChangeLogSet;
 
 import java.io.UnsupportedEncodingException;
 import java.text.MessageFormat;
 
 public class PrivateMessage extends FlowdockMessage {
 
     private String apiUrl;
     private String recipient;
 
     public PrivateMessage(String token) {
         setToken(token);
     }
 
     @Override
     public String asPostData() throws UnsupportedEncodingException {
         StringBuffer postData = new StringBuffer();
         postData.append("event=").append("message");
         postData.append("&content=").append(urlEncode(content));
 
         return postData.toString();
     }
 
     @Override
     public String getApiUrl() {
         return this.apiUrl;
     }
 
     @Override
     public void setApiUrl() {
         this.apiUrl = MessageFormat.format("https://{0}@api.flowdock.com/private/{1}/messages", this.token, this.recipient);
     }
 
     @Override
     protected void setContentFromBuild(AbstractBuild build, BuildResult buildResult) {
         setBuildAndResult(build, buildResult);
     }
 
     protected String getAuthor(ChangeLogSet<? extends ChangeLogSet.Entry> changes) {
         return "foo";
     }
 
     protected void setRecipient(String recipient) {
         this.recipient = recipient;
        this.setApiUrl();
     }
 
 }
