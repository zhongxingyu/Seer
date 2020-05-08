 package edu.rice.rubis;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 
 import edu.rice.rubis.servlets.AboutMe;
 import edu.rice.rubis.servlets.BrowseCategories;
 import edu.rice.rubis.servlets.BrowseRegions;
 import edu.rice.rubis.servlets.BuyNow;
 import edu.rice.rubis.servlets.BuyNowAuth;
 import edu.rice.rubis.servlets.Config;
 import edu.rice.rubis.servlets.PutBid;
 import edu.rice.rubis.servlets.PutBidAuth;
 import edu.rice.rubis.servlets.PutComment;
 import edu.rice.rubis.servlets.PutCommentAuth;
 import edu.rice.rubis.servlets.RegisterItem;
 import edu.rice.rubis.servlets.RegisterUser;
 import edu.rice.rubis.servlets.SearchItemsByCategory;
 import edu.rice.rubis.servlets.SearchItemsByRegion;
 import edu.rice.rubis.servlets.SellItemForm;
 import edu.rice.rubis.servlets.ServletPrinter;
 import edu.rice.rubis.servlets.StoreBid;
 import edu.rice.rubis.servlets.StoreBuyNow;
 import edu.rice.rubis.servlets.StoreComment;
 import edu.rice.rubis.servlets.ViewBidHistory;
 import edu.rice.rubis.servlets.ViewItem;
 import edu.rice.rubis.servlets.ViewUserInfo;
 import eu.ascens_ist.cloud.appengine.app.AppInterface;
 import eu.ascens_ist.cloud.connectivity.msg.Message;
 import eu.ascens_ist.cloud.knowledge.model.Snapshot;
 
 public class Activator implements BundleActivator, AppInterface {
 
 	private static BundleContext context;
 
 	static BundleContext getContext() {
 		return context;
 	}
 
 	private ServiceRegistration<?> registration;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
 	 */
 	@Override
 	public void start(BundleContext bundleContext) throws Exception {
 		Activator.context= bundleContext;
 		registration= context.registerService(AppInterface.class.getName(), this, null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext bundleContext) throws Exception {
 		registration.unregister();
 		Activator.context= null;
 	}
 
 	@Override
 	public void handleUI(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		// case of basic home 
 		if (target.isEmpty()){
 			ServletPrinter.printFile(response.getWriter(), Config.HTMLFilesPath+"/index.html");
 		// manual mapping of url-servlets
 		}else if (target.startsWith("servlet/")){
 			HttpServlet hs = null;
 			if (target.startsWith("servlet/AboutMe"))
 				hs = new AboutMe();
 			else if (target.startsWith("servlet/BrowseCategories"))
 				hs = new BrowseCategories();
 			else if (target.startsWith("servlet/BrowseRegions"))
 				hs = new BrowseRegions();		
 			else if (target.startsWith("servlet/StoreBuyNow"))
 					hs = new StoreBuyNow();
 			else if (target.startsWith("servlet/BuyNowAuth"))
 				hs = new BuyNowAuth();
 			else if (target.startsWith("servlet/BuyNow"))
 					hs = new BuyNow();
 			else if (target.startsWith("servlet/PutBidAuth"))
 				hs = new PutBidAuth();
 			else if (target.startsWith("servlet/PutBid"))
 					hs = new PutBid();
 			else if (target.startsWith("servlet/PutCommentAuth"))
 				hs = new PutCommentAuth();
 			else if (target.startsWith("servlet/PutComment"))
 				hs = new PutComment();
 			else if (target.startsWith("servlet/RegisterItem"))
 				hs = new RegisterItem();
 			else if (target.startsWith("servlet/RegisterUser"))
 				hs = new RegisterUser();
 			else if (target.startsWith("servlet/SearchItemsByCategory"))
 				hs = new SearchItemsByCategory();
 			else if (target.startsWith("servlet/SearchItemsByRegion"))
 				hs = new SearchItemsByRegion();
 			else if (target.startsWith("servlet/SellItemForm"))
 				hs = new SellItemForm();
 			else if (target.startsWith("servlet/StoreBid"))
 				hs = new StoreBid();
 			else if (target.startsWith("servlet/StoreComment"))
 				hs = new StoreComment();
 			else if (target.startsWith("servlet/ViewBidHistory"))
 				hs = new ViewBidHistory();
 			else if (target.startsWith("servlet/ViewItem"))
 				hs = new ViewItem();
 			else if (target.startsWith("servlet/ViewUserInfo"))
 				hs = new ViewUserInfo();
 			// if there is a match in the mapping
 			if (hs != null){
 				// initializes the servlet (e.g. db entry, hibernate)
 				hs.init();
 				// supplies the request and response to the servlet for execution    
 				hs.service(request, response);
 			}
 		// raw file printing
 		}else{
 			ServletPrinter.printFile(response.getWriter(), Config.HTMLFilesPath+"/"+target);
 		}
 	}
 
 	@Override
 	public Snapshot createSnapshot() {
 		return null;
 	}
 
 	@Override
	public void resumeFromSnapshot(Object snapshot) {
		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void messageReceived(Message message) {
 		// TODO Auto-generated method stub
 		
 	}
 }
