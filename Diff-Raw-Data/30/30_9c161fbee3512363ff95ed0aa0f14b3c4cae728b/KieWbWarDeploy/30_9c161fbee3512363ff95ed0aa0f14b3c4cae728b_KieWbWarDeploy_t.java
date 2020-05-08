 package org.kie.tests.wb.eap.deploy;
 
 import static org.kie.tests.wb.base.methods.TestConstants.projectVersion;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.UUID;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.auth.AuthScheme;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.AuthState;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HttpContext;
 import org.jboss.resteasy.client.ClientExecutor;
 import org.jboss.resteasy.client.ClientRequestFactory;
 import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.importer.ZipImporter;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.maven.Maven;
 import org.kie.tests.wb.base.deploy.TestKjarDeploymentLoader;
 
 public class KieWbWarDeploy {
 
     protected static final String PROCESS_ID = "org.jbpm.humantask";
     
     protected static WebArchive createWarWithTestDeploymentLoader(String classifier) {
         // Import kie-wb war
         File [] warFile = 
                 Maven.resolver()
                 .loadPomFromFile("pom.xml")
                 .resolve("org.kie:kie-wb-distribution-wars:war:" + classifier + ":" + projectVersion )
                 .withoutTransitivity()
                 .asFile();
         ZipImporter zipWar = ShrinkWrap.create(ZipImporter.class, "test.war").importFrom(warFile[0]);
         
         WebArchive war = zipWar.as(WebArchive.class);
         
         // Add kjar deployer
         war.addClass(TestKjarDeploymentLoader.class);
         
         // Replace kie-services-remote jar with the one we just generated
         war.delete("WEB-INF/lib/kie-services-remote-" + projectVersion + ".jar");
         war.delete("WEB-INF/lib/kie-services-client-" + projectVersion + ".jar");
         File [] kieRemoteDeps = Maven.resolver()
                 .loadPomFromFile("pom.xml")
                 .resolve("org.kie.remote:kie-services-remote", "org.kie.remote:kie-services-client")
                 .withoutTransitivity()
                 .asFile();
         war.addAsLibraries(kieRemoteDeps);
        
         // Add data service resource for tests
         war.addPackage("org/kie/tests/wb/base/services/data");
         
         // Deploy test deployment
         TestKjarDeploymentLoader.deployKjarToMaven();
         
         return war;
     }
     
     protected static ClientRequestFactory createBasicAuthRequestFactory(URL deploymentUrl, String user, String password) throws URISyntaxException { 
         BasicHttpContext localContext = new BasicHttpContext();
         HttpClient preemptiveAuthClient = createPreemptiveAuthHttpClient(user, password, 5, localContext);
         ClientExecutor clientExecutor = new ApacheHttpClient4Executor(preemptiveAuthClient, localContext);
         return new ClientRequestFactory(clientExecutor, deploymentUrl.toURI());
     }
     
     protected static DefaultHttpClient createPreemptiveAuthHttpClient(String userName, String password, int timeout, BasicHttpContext localContext) {
         BasicHttpParams params = new BasicHttpParams();
         int timeoutMilliSeconds = timeout * 1000;
         HttpConnectionParams.setConnectionTimeout(params, timeoutMilliSeconds);
         HttpConnectionParams.setSoTimeout(params, timeoutMilliSeconds);
         DefaultHttpClient client = new DefaultHttpClient(params);
 
         if (userName != null && !"".equals(userName)) {
             client.getCredentialsProvider().setCredentials(
                     new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                     new UsernamePasswordCredentials(userName, password));
             // Generate BASIC scheme object and stick it to the local execution context
             BasicScheme basicAuth = new BasicScheme();
             String contextId = UUID.randomUUID().toString();
             localContext.setAttribute(contextId, basicAuth);
 
             // Add as the first request interceptor
             client.addRequestInterceptor(new PreemptiveAuth(contextId), 0);
         }
 
         // set the following user agent with each request
         String userAgent = "ArtifactoryBuildClient/" + 1;
         HttpProtocolParams.setUserAgent(client.getParams(), userAgent);
         return client;
     }
     
     static class PreemptiveAuth implements HttpRequestInterceptor {
         
         private final String contextId;
         public PreemptiveAuth(String contextId) { 
             this.contextId = contextId;
         }
         
         public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
 
             AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
 
             // If no auth scheme available yet, try to initialize it preemptively
             if (authState.getAuthScheme() == null) {
                 AuthScheme authScheme = (AuthScheme) context.getAttribute(contextId);
                 CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                 HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                 if (authScheme != null) {
                     Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                     if (creds == null) {
                         throw new HttpException("No credentials for preemptive authentication");
                     }
                     authState.setAuthScheme(authScheme);
                     authState.setCredentials(creds);
                 }
             }
         }
     }
 }
