 package com.mathieubolla;
 
 import static com.google.inject.name.Names.named;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.inject.Named;
 
 import com.amazonaws.ClientConfiguration;
 import com.amazonaws.Protocol;
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Provides;
 import com.mathieubolla.processing.WorkUnit;
 import com.mathieubolla.ui.BatchUi;
 import com.mathieubolla.ui.SwingUi;
 import com.mathieubolla.ui.Ui;
 
 public class MassUpload {
 	public static void main(String[] args) throws Exception {
 		Date startDate = new Date();
 		
 		if (System.getProperty("batch.path") != null) {
 			Guice.createInjector(new BatchModule()).getInstance(ModularUpload.class).start(startDate);
 		} else {
 			Guice.createInjector(new GraphicalModule()).getInstance(ModularUpload.class).start(startDate);
 		}
 	}
 
 	public static class GraphicalModule extends BaseModule {
 		@Override
 		protected void configureSpecialized() {
 			bind(Ui.class).to(SwingUi.class);
 		}
 	}
 	
 	public static class BatchModule extends BaseModule {
 		@Override
 		protected void configureSpecialized() {
 			bindConstant().annotatedWith(named("batch.path")).to(System.getProperty("batch.path"));
 			bind(Ui.class).to(BatchUi.class);
 		}
 	}
 	
 	public abstract static class BaseModule extends AbstractModule {
 		@Override
 		protected final void configure() {
 			bindConstant().annotatedWith(named("ec2credentials")).to(System.getProperty("user.home") + "/.ec2/credentials.properties");
 			bind(Queue.class).toInstance(new ConcurrentLinkedQueue<WorkUnit>());
			configureSpecialized();
 		}
 		
 		protected abstract void configureSpecialized();
 		
 		@Provides
 		protected AmazonS3 configureS3Client(@Named("ec2credentials") String credentialsPath) {
 			try {
 				return new AmazonS3Client(new PropertiesCredentials(new File(credentialsPath)), new ClientConfiguration().withProtocol(Protocol.HTTP).withConnectionTimeout(5000).withMaxErrorRetry(5).withMaxConnections(10));
 			} catch (Throwable t) {
 				throw new IllegalArgumentException("Can't configure Amazon S3 Client. Properties file might be missing.", t);
 			}
 		}
 	}
 }
