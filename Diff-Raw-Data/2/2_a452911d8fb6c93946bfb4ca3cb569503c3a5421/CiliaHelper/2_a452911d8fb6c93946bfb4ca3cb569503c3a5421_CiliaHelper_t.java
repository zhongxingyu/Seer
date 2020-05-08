 /*
  * Copyright Adele Team LIG
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.helper;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import fr.liglab.adele.cilia.model.Component;
 import junit.framework.Assert;
 
 import org.apache.felix.ipojo.test.helpers.OSGiHelper;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.liglab.adele.cilia.ApplicationRuntime;
 import fr.liglab.adele.cilia.CiliaContext;
 import fr.liglab.adele.cilia.builder.Architecture;
 import fr.liglab.adele.cilia.builder.Builder;
 import fr.liglab.adele.cilia.exceptions.CiliaException;
 import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
 import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
 import fr.liglab.adele.cilia.framework.ICollector;
 import fr.liglab.adele.cilia.helper.impl.ProcessorHelperImpl;
 import fr.liglab.adele.cilia.helper.impl.SchedulerHelperCreator;
 import fr.liglab.adele.cilia.model.Adapter;
 import fr.liglab.adele.cilia.model.Chain;
 import fr.liglab.adele.cilia.model.Mediator;
 import fr.liglab.adele.cilia.runtime.CiliaInstance;
 import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
 import fr.liglab.adele.cilia.util.CiliaFileManager;
 import fr.liglab.adele.cilia.util.Const;
 
 /**
  * 
  * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
  *         Team</a>
  * 
  */
 public class CiliaHelper {
 
 	private static final Logger logger = LoggerFactory.getLogger(Const.LOGGER_CORE);
 
 	protected OSGiHelper ohelper;
 	
 	private static final String NAMESPACE = "fr.liglab.adele.cilia.test";
 	
 	private volatile static int initial = 0;
 	
 	private Object lock = new Object();
 	
 	public CiliaHelper(BundleContext bc) {
 		ohelper = new OSGiHelper(bc);
 	}
 	/**
 	 * Get the mediator model.
 	 * @param chain The chain where the mediator is.
 	 * @param mm The mediator id.
 	 * @return The mediator model object.
 	 */
 	public Mediator getMediatorModel(String chain, String mm) {
 		CiliaContext context = getCiliaContext();
 		try {
 			return context.getApplicationRuntime().getChain(chain).getMediator(mm);
 		} catch (CiliaIllegalParameterException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	/**
 	 * 
 	 * @param chain
 	 * @param am
 	 * @return
 	 */
 	public Adapter getAdapterModel(String chain, String am) {
 		CiliaContext context = getCiliaContext();
 		try {
 			return context.getApplicationRuntime().getChain(chain).getAdapter(am);
 		} catch (CiliaIllegalParameterException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public MediatorTestHelper instrumentMediatorInstance(String chainId,
 			String mediator, String inputports[], String exitports[]) {
 		
 		if ((exitports == null) && (inputports == null) ) {
 			System.err.println("Ilegal port parameters");
 			return null;
 		}
 		String id;
 		synchronized (lock) {
 			id= chainId+"_"+mediator + initial++;
 		}
 		CiliaContext ccontext = (CiliaContext) ohelper.getServiceObject(
 				CiliaContext.class.getName(), null);
 		Builder builder = ccontext.getBuilder();
 		Architecture arch = null;
 		try {
 			arch = builder.get(chainId);
 			arch.create().adapter().type("cilia-adapter-helper")
 			.namespace(NAMESPACE).id(id).configure().key("identifier").value(id);
 			for (int i = 0; inputports != null && i < inputports.length; i++) {
 				arch.bind().from(id+":unique")
 				.to(mediator + ":" + inputports[i]);
 			}
 			for (int i = 0; exitports != null && i < exitports.length; i++) {
 				arch.bind().from(mediator + ":" + exitports[i])
 				.to(id+":unique");
 			}
 			builder.done();
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			return null;
 		}
 		ohelper.waitForService(MediatorTestHelper.class.getName(), "(identifier="+id+")", 8000);
 		MediatorTestHelper helper = (MediatorTestHelper)ohelper.getServiceObject(MediatorTestHelper.class.getName(), "(identifier="+id+")");
 		return helper;
 	}
 
 	public CiliaInstance createInstance(String factory){
 		return new CiliaInstanceWrapper(ohelper.getContext(), "0", "(factory.name="+factory+")", null, null);
 	}
 
 	public CiliaInstance createInstance(String factory, Dictionary props){
 		return new CiliaInstanceWrapper(ohelper.getContext(), "0", "(factory.name="+factory+")", props, null);
 	}
 
 	public MediatorTestHelper instrumentChain(String chainId,
 			String firstMediatorWithPort, String lastMediatorWithPort) {
 		CiliaContext ccontext = (CiliaContext) ohelper.getServiceObject(
 				CiliaContext.class.getName(), null);
 		String id; 
 		synchronized (lock) {
 			id = chainId + "-" + "helper-" + initial++;
 		}
 		Builder builder = ccontext.getBuilder();
 		Architecture arch = null;
 		try {
 			arch = builder.get(chainId);
 			logger.debug("Getting chain {}", chainId);
 			arch.create().adapter().type("cilia-adapter-helper")
 			.namespace(NAMESPACE).id(id).configure().key("identifier").value(id);
 			arch.bind().from(id+":unique").to(firstMediatorWithPort);
 			arch.bind().from(lastMediatorWithPort).to(id+":unique");
 			logger.debug("Chain will be modified");
 			builder.done();
 			logger.debug("Chain is ready");
 		} catch (CiliaException e) {
 			e.printStackTrace();
 			return null;
 		}
 		waitSomeTime(3000);
 		ohelper.waitForService(MediatorTestHelper.class.getName(), "(identifier="+id+")", 5000);
 		MediatorTestHelper helper = (MediatorTestHelper)ohelper.getServiceObject(MediatorTestHelper.class.getName(), "(identifier="+id+")");
 		return helper;
 	}
 
 	public void dispose() {
 		CiliaContext context = getCiliaContext(); 
 		String ids[] = context.getApplicationRuntime().getChainId();
 		for (int i=0; ids != null && i < ids.length; i ++) {
 			try {
 				System.out.println("Removing Chain " + ids[i]);
 				context.getBuilder().remove(ids[i]).done();
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 		}
 		ohelper.dispose();
 	}
 
 	public CiliaContext getCiliaContext() {
 		ohelper.waitForService(CiliaContext.class.getName(), null, 8000);
 		CiliaContext context = (CiliaContext)ohelper.getServiceObject(CiliaContext.class.getName(), null);
 		return context;
 	}
 
 	public Chain getChain(String chain){
 		CiliaContext cc = getCiliaContext();
 		try {
 			return cc.getApplicationRuntime().getChain(chain);
 		} catch (CiliaIllegalParameterException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public CollectorHelper getCollectorHelper(ICollector ic){
 		return new CollectorHelper(ic);
 	}
 
 	public ProcessorHelper getProcessorHelper(String processorname, String processornamespace){
 		return getProcessorHelper(processorname, processornamespace,null);
 	}
 	
 	public ProcessorHelper getProcessorHelper(String processorname, String processornamespace, Hashtable properties){
 		ProcessorHelperImpl proc = new ProcessorHelperImpl(this, processorname, processornamespace, properties);
 		proc.start();
 		return proc;
 	}
 	
 	public MediatorTestHelper getSchedulerHelper(String schedulername, String schedulernamespace, Hashtable properties){
 		SchedulerHelperCreator proc = new SchedulerHelperCreator(this, schedulername, schedulernamespace, properties);
 		proc.start();
 		System.out.println("Waiting for helper:" + proc.getId());
 		ohelper.waitForService(MediatorTestHelper.class.getName(), "(identifier="+proc.getId()+")", 8000);
 		MediatorTestHelper helper = (MediatorTestHelper)ohelper.getServiceObject(MediatorTestHelper.class.getName(), "(identifier="+proc.getId()+")");
 		return helper;
 	}
 	
 	public void loadFileFromResource(String filename){
 		URL url = ohelper.getContext().getBundle().getResource("remoteTest.dscilia");
 		load(url);
 	}
 	
 	public void load(URL url) {
 		InputStream fis = null;
 		String filename;
 		try {
 			fis = url.openStream();
 		} catch (IOException e) {
 			e.printStackTrace();
 
 		}
 		synchronized (lock) {
 			filename = "temporal-file-" + initial++;
 		}
 		File file = null;
 		try {
 			
 			file = File.createTempFile(filename, ".dscilia");
 			OutputStream out=new FileOutputStream(file);
 			byte buf[]=new byte[1024];
 			int len;
 			while((len=fis.read(buf))>0)
 				out.write(buf,0,len);
 			out.close();
 			fis.close();
 
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		System.out.println("chain to be loaded...");
 		ohelper.waitForService(CiliaFileManager.class.getName(), null, 8000);
 		CiliaFileManager service = (CiliaFileManager)ohelper.getServiceObject(CiliaFileManager.class.getName(), null);
 		
 		service.loadChain(file);
 		System.out.println("chain loaded");
 	}
 
 	public BundleContext getBundleContext(){
 		return ohelper.getContext();
 	}
 	
 	public Builder getBuilder(){
 		return getCiliaContext().getBuilder();
 	}
 	
 	public ApplicationRuntime getApplicationRuntime() {
 		return getCiliaContext().getApplicationRuntime();
 	}
 	
 	public void startChain(String id){
 		try {
 			getCiliaContext().getApplicationRuntime().startChain(id);
 		} catch (CiliaIllegalParameterException e) {
 			e.printStackTrace();
 			Assert.fail("Unable to start Chain: " +  id);
 		} catch (CiliaIllegalStateException e) {
 			e.printStackTrace();
 			Assert.fail("Unable to start Chain: " +  id);
 		}
 	}
 	
 	public void unload(String url){
 
 	}
 
 	public boolean waitToChain(String chainId, long time) {
 		boolean found = false;
 		long current = System.currentTimeMillis();
 		long finalTime = current + time;
 		while(!found && current <= finalTime) {
 			waitSomeTime(50);
 			Chain ch = null;
 			try {
 				ch = getCiliaContext().getApplicationRuntime().getChain(chainId);
 			} catch (CiliaIllegalParameterException e) {
 				e.printStackTrace();
 			}
 			if (ch != null){
 				found = true;
 			}
 			current = System.currentTimeMillis();
 		}
 		return found;
 	}
 
     public boolean waitToComponent(String chainId, String componentId, long time) {
         if(!waitToChain(chainId,5000)){
             return false;
         }
        Chain chain = getChain(chainId);
         Component component = null;
         boolean found = false;
         long current = System.currentTimeMillis();
         long finalTime = current + time;
         while(!found && current <= finalTime) {
             waitSomeTime(500);
             if(chain.getMediator(componentId) != null || chain.getAdapter(componentId)!=null){
                found = true;
             }
             current = System.currentTimeMillis();
         }
         return found;
     }
 
 	public static void waitSomeTime(int l) {
 		try {
 			Thread.sleep(l);//wait to be registered
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public OSGiHelper getOSGIHelper() {
 		return ohelper;
 	}
 
     public static boolean checkReceived(MediatorTestHelper helper, int count, long timeout){
         boolean found = false;
         long current = System.currentTimeMillis();
         long finalTime = current + timeout;
         while(!found && current <= finalTime) {
             waitSomeTime(20);
             int amount = helper.getAmountData();
             if (amount >= count){
                 found = true;
             }
             current = System.currentTimeMillis();
         }
         return found;
     }
 
     public static boolean checkReceived(CollectorHelper helper, int count, long timeout){
         boolean found = false;
         long current = System.currentTimeMillis();
         long finalTime = current + timeout;
         while(!found && current <= finalTime) {
             waitSomeTime(20);
             int amount = helper.countReceived();
             if (amount >= count){
                 found = true;
             }
             current = System.currentTimeMillis();
         }
         return found;
     }
 	
 }
