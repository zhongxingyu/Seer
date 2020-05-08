 /**
  * 
  */
 package eu.fusepool.java.silk.client.impl;
 
 import java.io.File;
 import java.io.InputStream;
 
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Service;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.component.ComponentContext;
 
 import eu.fusepool.java.silk.client.SilkClient;
 import eu.fusepool.scala.silk.Silk;
 
 
 
 
 /**
  * @author giorgio
  *
  */
 @Component(immediate = true, metatype = true)
 @Service(SilkClient.class)
 public class SilkClientImpl implements SilkClient {
 
 	protected BundleContext ctx ;
 	
 	protected ComponentContext componentContext ;
 	
 	/* (non-Javadoc)
 	 * @see eu.fusepool.java.silk.client.SilkClient#excute()
 	 */
 	@Override
 	public void excute() {
 		InputStream configStream = SilkClientImpl.class.getResourceAsStream("/config/silk-bundle-config2.xml") ;
 		Silk.executeStream(configStream, null, 1, true) ;
 		/*
 		File file = ctx.getDataFile("SilkClientFile.txt") ;
 		if(file!=null) {
 			System.out.println("\n\n######################\n"+file.getAbsolutePath()+"\n###########");
 		} else {
 			System.out.println("\n\n######################\n"+"nessun file creato"+"\n###########");
 		}
 		*/
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.fusepool.java.silk.client.SilkClient#executeStream(java.io.InputStream, java.lang.String, int, boolean)
 	 */
 	@Override
 	public void executeStream(InputStream config, String linkSpecId,
 			int numThreads, boolean reload)  {
 		Silk.executeStream(config, linkSpecId, numThreads, reload) ;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.fusepool.java.silk.client.SilkClient#executeFile(java.io.File, java.lang.String, int, boolean)
 	 */
 	@Override
 	public void executeFile(File config, String linkSpecId, int numThreads,
 			boolean reload) {
 		Silk.executeFile(config, linkSpecId, numThreads, reload) ;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.fusepool.java.silk.client.SilkClient#ExecuteConfig()
 	 */
 	//@Override
 	public void ExecuteConfig() throws Exception {
 		// TODO Auto-generated method stub
 		throw new Exception("not implemented yet!") ;
 	}
 
 	
 	   @Activate
 	   protected void activate(final ComponentContext componentContext) {
 		   this.componentContext = componentContext ;
		   this.excute() ;
 	
 	   }
 	
 
 	
 		/**
 		 * @param args
 		 * @throws Exception 
 		 */
 		public static void main(String[] args) throws Exception {
 			SilkClient client = new SilkClientImpl() ;
 			client.excute() ;
 
 		}
 
 		public BundleContext getCtx() {
 			return ctx;
 		}
 
 		public void setCtx(BundleContext ctx) {
 			this.ctx = ctx;
 		}
 	
 	
 }
