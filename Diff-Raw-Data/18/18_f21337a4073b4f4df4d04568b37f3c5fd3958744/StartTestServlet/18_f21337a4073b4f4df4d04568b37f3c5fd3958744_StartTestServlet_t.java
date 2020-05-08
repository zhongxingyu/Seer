 package li.rudin.rt.test;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 
 import li.rudin.rt.api.filter.RTFilter;
 import li.rudin.rt.api.handler.RTHandler;
 import li.rudin.rt.api.handler.RTSender;
 import li.rudin.rt.api.listener.RTListener;
 import li.rudin.rt.core.RT;
 import li.rudin.rt.servlet.RTServlet;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class StartTestServlet
 {
 	public static void main(String[] args) throws Exception
 	{
 		Server server = new Server(8080);
 		
 		ServletContextHandler handler = new ServletContextHandler();
 		server.setHandler(handler);
 		
 		ServletHolder holder = new ServletHolder(RTServlet.class);
 		holder.setInitOrder(1);
 		holder.setAsyncSupported(false);
 		handler.addServlet(holder, "/rt");
 		
 		server.start();
 		
 		RTHandler rt = RT.getInstance();
 		rt.setFilter(new MyFilter());
 		rt.addListener(new MyListener());
 		
 		ScheduledExecutorService pool = Executors.newScheduledThreadPool(10);
		//pool.scheduleAtFixedRate(new DateDispatcher(), 0, 500, TimeUnit.MILLISECONDS);
		//pool.scheduleAtFixedRate(new FastCountDispatcher(), 0, 1, TimeUnit.MILLISECONDS);
		//pool.scheduleAtFixedRate(new ObserverDispatcher(), 0, 250, TimeUnit.MILLISECONDS);
 		
 		//Wait for key
 		System.in.read();
 
 		//Shutdown pool
 		pool.shutdown();
 		
 		//stop server
 		server.stop();
 	}
 	
 	/**
 	 * Custom listener
 	 * @author user
 	 *
 	 */
 	public static class MyListener implements RTListener
 	{
 		private static final Logger logger = LoggerFactory.getLogger(MyListener.class);
 
 		@Override
 		public void onConnect(RTSender client, RTHandler handler)
 		{
 			logger.debug("onConnect({}, {})", client, handler);
 		}
 
 		@Override
 		public void onClose(RTHandler handler)
 		{
 			logger.debug("onClose({})", handler);
 		}
 		
 	}
 	
 	/**
 	 * Custom filter
 	 * @author user
 	 *
 	 */
 	public static class MyFilter implements RTFilter
 	{
 
 		private static final Logger logger = LoggerFactory.getLogger(MyFilter.class);
 		
 		@Override
 		public boolean accept(String type, Object data)
 		{
 			logger.trace("Intercepted: type: '{}' data: '{}'", type, data);
 			return true;
 		}
 		
 	}
 }
