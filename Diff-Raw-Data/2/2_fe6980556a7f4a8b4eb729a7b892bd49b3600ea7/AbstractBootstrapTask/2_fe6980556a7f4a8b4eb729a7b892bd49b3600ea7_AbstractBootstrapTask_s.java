 package com.marklogic.ant.tasks;
 
 import com.google.common.base.Optional;
 import com.marklogic.ant.types.ConnectionImpl;
 import com.marklogic.ant.types.HttpSessionFactory;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.tools.ant.BuildException;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Bob Browning <bob.browning@pressassociation.com>
  */
 public abstract class AbstractBootstrapTask extends AbstractMarklogicTask {
 
     protected static final String XQUERY_PROLOG = "xquery version '1.0-ml';\n";
 
     protected static final String ML_ADMIN_MODULE_IMPORT = "import module namespace admin = 'http://marklogic.com/xdmp/admin' at '/MarkLogic/admin.xqy';\n";
 
     /**
      * The port used to bootstrap MarkLogic Server.
      */
     private Optional<Integer> bootstrapPort = Optional.of(8000);
 
     /**
      * The MarkLogic Installer XDBC server name.
      */
     protected String xdbcName = "MarkLogic-Installer-XDBC";
 
     /**
      * The MarkLogic Installer XDBC module root setting.
      */
     protected String xdbcModuleRoot = "/";
 
     public void setBootstrapPort(int bootstrapPort) {
         this.bootstrapPort = Optional.of(bootstrapPort);
     }
 
     public void setXdbcName(String xdbcName) {
         this.xdbcName = xdbcName;
     }
 
     public void setXdbcModuleRoot(String xdbcModuleRoot) {
         this.xdbcModuleRoot = xdbcModuleRoot;
     }
 
     @Override
     public void execute() throws BuildException {
         System.out.println("Executing " + getTaskName());
         executeBootstrapQuery(getBootstrapExecuteQuery());
     }
 
     public HttpResponse executeBootstrapQuery(final String query) throws BuildException {
         /*
          * Build Query Parameters
          */
         List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
         queryParameters.add(new BasicNameValuePair("queryInput", query));
 
         URI uri;
         try {
             ConnectionImpl connection = new ConnectionImpl(this.getConnection());
             connection.setPort(bootstrapPort.get());
 
             HttpSessionFactory factory = new HttpSessionFactory(connection);
             uri = factory.getURI("/use-cases/eval2.xqy", queryParameters);
         } catch (URISyntaxException e) {
             throw new BuildException("Invalid URI", e);
         }
 
         HttpPost httpPost = new HttpPost(uri);
 
         HttpResponse response;
         try {
             response = getHttpClient().execute(httpPost);
         } catch (Exception e) {
            throw new BuildException("Error executing post", e);
         }
 
         if (response.getStatusLine().getStatusCode() != 200) {
             throw new BuildException("Execute of bootstrap query failed - " + response.getStatusLine());
         }
 
         return response;
     }
 
     protected abstract String getBootstrapExecuteQuery() throws BuildException;
 
     protected HttpClient getHttpClient() {
         DefaultHttpClient httpClient = new DefaultHttpClient();
         httpClient.getCredentialsProvider().setCredentials(
                 AuthScope.ANY,
                 new UsernamePasswordCredentials(getConnection().getUsername(), getConnection().getPassword()));
         httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
 
         return httpClient;
     }
 }
