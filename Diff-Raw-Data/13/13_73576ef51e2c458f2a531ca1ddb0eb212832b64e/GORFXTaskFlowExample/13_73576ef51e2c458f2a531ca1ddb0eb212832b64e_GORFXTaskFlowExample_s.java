 package de.zib.gndms.gndmc.gorfx;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import de.zib.gndms.common.kit.application.AbstractApplication;
 import de.zib.gndms.common.kit.security.SetupSSL;
 import de.zib.gndms.common.model.gorfx.types.Quote;
 import de.zib.gndms.common.model.gorfx.types.io.ContractConverter;
 import de.zib.gndms.common.model.gorfx.types.io.ContractPropertyReader;
 import de.zib.gndms.common.model.gorfx.types.io.ContractStdoutWriter;
 import org.kohsuke.args4j.Option;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.UUID;
 
 import static de.zib.gndms.stuff.misc.StringAux.isEmpty;
 
 /**
  * @author try ma ik jo rr a zib
  * @date 14.03.11 11:46
  * @brief A test run for the gorfx taskflow execution.
  */
 public abstract class GORFXTaskFlowExample extends AbstractApplication {
 
 	@Option(name = "-uri", required = true, usage = "URL of GORFX-Endpoint", metaVar = "URI")
 	protected String gorfxEpUrl;
 	@Option(name = "-dn", required = true, usage = "DN")
 	protected String dn;
     @Option( name="-props", required=true, usage="taskflow order properties" )
     protected String orderPropFile;
     @Option( name="-con-props", usage="contract.properties" )
     protected String conPropFile;
     //@Option( name="-proxyfile", usage="grid-proxy-file to lead", metaVar="proxy-file" )
     //protected String proxyFile = null;
     @Option( name="-cancel", required = false, usage = "ms to wait before destroying taskClient.")
     protected Long cancel = null;
     @Option( name="-myProxyLogin", required = false, usage = "login name for the MyProxyServer." )
     protected String myProxyLogin;
     @Option( name="-myProxyPasswd", required = false, usage = "password name for the " +
                                                               "MyProxyServer." )
     protected String myProxyPasswd;
 
     @Option( name="-keystorePasswd", required = true, usage = "Password for keystore." )
     protected String keystorePasswd;
     @Option( name="-privkeyPasswd", required = true, usage = "Password for private key in keystore." )
     protected String privkeyPasswd;
     @Option( name="-keystore", required = true, usage = "Keystore to use." )
     protected String keyStoreLocation;
 
     @Option( name="-truststorePasswd", required = false, usage = "Password for truststore." )
     protected String truststorePasswd;
     @Option( name="-truststore", required = false, usage = "truststore to use." )
     protected String trustStoreLocation = null;
 
 	protected String wid = UUID.randomUUID().toString();
     private ApplicationContext context;
     private FullGORFXClient gorfxClient;
     private TaskFlowClient tfClient;
     private TaskClient taskClient;
     private AbstractTaskFlowExecClient etfc;
     private Quote desiredQuote;
 
     private final boolean requireMyProxy;
 
 
     protected GORFXTaskFlowExample() {
         requireMyProxy = false;
     }
 
 
     protected GORFXTaskFlowExample( final boolean requireMyProxy ) {
 
         this.requireMyProxy = requireMyProxy;
     }
 
 
     @Override
     public void run() throws Exception {
 
         if ( requireMyProxy )
             requiresMyProxy();
 
         System.out.println( "Running TaskFlow test with: " );
         System.out.println("connection to: \"" + gorfxEpUrl + "\"");
 
         context = new ClassPathXmlApplicationContext(
                 "classpath:META-INF/client-context.xml");
         gorfxClient = createBean( FullGORFXClient.class );
 		gorfxClient.setServiceURL(gorfxEpUrl);
 
         tfClient = createBean( TaskFlowClient.class );
         tfClient.setServiceURL(gorfxEpUrl);
 
         taskClient = createBean( TaskClient.class );
         taskClient.setServiceURL(gorfxEpUrl);
 
         etfc = provideTaskFlowClient();
         etfc.setGorfxClient( gorfxClient );
         etfc.setTfClient( tfClient );
         etfc.setTaskClient( taskClient );
 
         SetupSSL setupSSL = new SetupSSL();
         setupSSL.setKeyStoreLocation( keyStoreLocation );
         setupSSL.prepareKeyStore( keystorePasswd );
         if( null == trustStoreLocation ) {
             trustStoreLocation = keyStoreLocation;
             truststorePasswd = keystorePasswd;
         }
        else if( null == truststorePasswd )
            throw new IllegalArgumentException( "Missing truststore password." );
 
         setupSSL.setTrustStoreLocation( trustStoreLocation );
         setupSSL.prepareTrustStore( truststorePasswd );
         setupSSL.setupDefaultSSLContext( privkeyPasswd );
         
         System.out.println( "SSL Context set." );
         
         normalRun();
         failingRun();
 	}
 
 
     public <T> T createBean( final Class<T> beanClass ) {
 
         return (T) context
                 .getAutowireCapableBeanFactory().createBean(
                         beanClass,
                         AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true );
     }
 
 
     protected abstract AbstractTaskFlowExecClient provideTaskFlowClient( );
 
     protected abstract void normalRun() throws Exception;
 
     protected abstract void failingRun();
 
 
     // Load SFR property file
     protected Properties loadOrderProps( final String orderPropsFilename ) throws IOException {
 
         InputStream is;
         if ( orderPropsFilename.trim().equals( "-" ) ) {
             System.out.println( "Reading props von stdin" );
             is = System.in;
             System.out.println( "done" );
         } else
             is = new FileInputStream(  orderPropsFilename );
 
         Properties orderProps = new Properties( );
 
         try {
             orderProps.load( is );
         } finally {
             is.close();
         }
         return orderProps;
     }
 
     protected Quote loadAndPrintDesiredQuote() throws IOException {
         if( conPropFile !=  null ) {
             desiredQuote = ContractPropertyReader.readFromFile( conPropFile );
 
             // Print initial contract
             System.out.println("# Requested contract");
             ContractConverter contractConv = new ContractConverter(new ContractStdoutWriter(),
                     desiredQuote );
             contractConv.convert();
         }
         return desiredQuote;
     }
 
 
     public ApplicationContext getContext() {
 
         return context;
     }
 
 
     public FullGORFXClient getGorfxClient() {
 
         return gorfxClient;
     }
 
 
     public TaskFlowClient getTfClient() {
 
         return tfClient;
     }
 
 
     public TaskClient getTaskClient() {
 
         return taskClient;
     }
 
 
     public AbstractTaskFlowExecClient getEtfc() {
 
         return etfc;
     }
 
 
     public Quote getDesiredQuote() {
 
         return desiredQuote;
     }
 
 
     /**
      * Checks if the required myProxy information are given.
      *
      * Checks it the command line args for myProxy{Login,Passwd} are present and not empty.
      * Exits with exit status 1, if they are missing.
      */
     protected void requiresMyProxy( ) {
         if( isEmpty( myProxyLogin ) || isEmpty( myProxyPasswd ) ) {
             System.out.println( "-myProxy{Login,Passwd} information required" );
             getCmdLineParser().printUsage( System.out );
             System.exit( 1 );
         }
     }
 }
