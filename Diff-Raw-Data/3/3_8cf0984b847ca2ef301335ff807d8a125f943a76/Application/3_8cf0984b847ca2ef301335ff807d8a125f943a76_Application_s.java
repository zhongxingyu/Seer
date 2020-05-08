 package controllers;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import models.AppProps;
 import models.Bundle;
 import models.History;
 import models.OutResult;
 import models.Repo;
 import models.Service;
 import models.Status;
 import play.Logger;
import play.cache.CacheFor;
 import play.libs.IO;
 import play.mvc.Before;
 import play.mvc.Finally;
 import util.Utils;
 import util.XStreamHelper;
 import bundle.BundleRegistry;
 
 /**
  * The main application controller 
  * 
  * @author Paolo Di Tommaso
  *
  */
 
 public class Application extends CommonController {
 	
 	static List<String> PAGES = Arrays.asList("index", "history", "references", "help", "contacts");
 
 	static private ThreadLocal<Bundle> bundle = new ThreadLocal<Bundle>();
 	
 	@Before
 	static void before(String bundle) { 
 		Logger.trace("Application#before(%s)", bundle);
 
 		/* 
 		 * some sanity checks
 		 */
 		if( Utils.isEmpty(bundle)) { 
 			error("Missing bundle argument");
 		}
 		
 		Bundle _bundle = BundleRegistry.instance().get(bundle);
 		if( _bundle == null ) { 
 			error("Cannot find bundle named: " + bundle);
 		}
 		
 		/* 
 		 * 1) save the bundle instance in the current context 
 		 * 2) save as route argument
 		 * 3) inject implict variables 
 		 */
 		Application.bundle.set(_bundle);
 		routeArgs.put("bundle", bundle);
 		injectImplicitVars(bundle);
 	}
 	
 	@Finally
 	static void release() {
 		Logger.trace("Application#release()");
 		Service.release();
 		Application.bundle.remove();
 	}
 	
 	/** 
 	 * Renders the main application index page
 	 */
     public static void index() {
     	redirect("Application.html", "index.html");
     }
     
     /**
      * Handle request to display the <i>result</i> page
      * 
      * @param rid the unique request identifier 
      */
     public static void result(String rid, Boolean ... cached ) {		
     	
     	final Repo ctx = new Repo(rid,false);
     	final Status status = ctx.getStatus();
 
     	if( status.isDone()) {
     		// touch it to update the last access time 
     		ctx.touch(); 
 		
     		// if the file exists load the result object and show it
 			OutResult result = ctx.getResult();
 			renderArgs.put("rid", rid);
 			renderArgs.put("ctx", ctx);
 			renderArgs.put("result", result);
 			renderArgs.put("cached", cached);
 	
     		renderBundlePage("result.html");
 		}
 		else if( status.isFailed() ) {
 			OutResult result = ctx.getResult();
 	    	render("Application/failed.html", rid, ctx, result, cached);
 		}
 		else if( status.isRunning() ) {
 			responseNoCache();
 			render("Application/wait.html", rid );
 		}
 		else {
 			int maxDays = AppProps.instance().getDataCacheDuration() / 60 / 60 / 24;
 	    	render("Application/oops.html", rid, maxDays);
 		}
  
    }
 	
 	/**
 	 * Renders the history html table  
 	 */
 	public static void historyTable() {	
 		String contextPath = AppProps.instance().getContextPath();
 		if( contextPath == null ) contextPath = "/";
 		else if( !contextPath.startsWith("/") ) { 
 			contextPath = "/" + contextPath;
 		}
 		
 		List<History> recent = History.findAll();
 		Collections.sort(recent, History.DescBeginTimeSort.INSTANCE);
 		responseNoCache();
 		render(recent, contextPath);
 	}
 
 
 	/**
 	 * Check the current status for the alignment request specified by <code>rid</code>
 	 * 
 	 * @param rid the request unique identifier
 	 */
 	public static void status(String rid) {
 		Repo ctx = new Repo(rid,false);
 		renderText(ctx.getStatus().toString());
 	}
 	
 	public static void replay( String rid ) {
 	
 		/* 
 		 * 1. check if a result exists 
 		 */
 		Repo repo = new Repo(rid);
 		if( !repo.hasResult() ) {
 			notFound(String.format("The specified request ID does not exist (%s)", rid));
 		}
 		
 		/* 
 		 * create the service and bind the stored values 
 		 */
 		String mode = repo.getResult().service;
 		Service service = service(bundle.get().name,mode);
 		service = service.copy();
 		Service.current(service);
 		service.input = XStreamHelper.fromXML(repo.getInputFile());
 		
 		/* 
 		 * 3. show the input form ('main.html')
 		 */
 		renderArgs.put("service", service);
 		render("Application/main.html");		
 	}
 	
 	public static void submit( String rid ) {
 		/*
 		 * 1. check and load the repo context object 
 		 */
 		Repo repo = new Repo(rid);
 		if( !repo.hasResult() ) {
 			notFound(String.format("The specified request ID does not exist (%s)", rid));
 		}
 
 		/* 
 		 * 2. create and bind the stored input values 
 		 */
 		OutResult result = repo.getResult(); 
 		Service service = service(bundle.get().name,result.service).copy();
 		Service.current(service);
 		service.input = XStreamHelper.fromXML(repo.getInputFile());
 		
 		/* 
 		 * 4. re-execute with caching feature disabled
 		 */
 		exec(bundle.get().name,service,false);
 	}
 	
 	/**
 	 * Renders a generic t-coffee 'service' i.e. a specific configuration defined in the main application file 
 	 * 
 	 * @param name the <i>service</i> name i.e. is unique identifier
 	 */
 	public static void main(String name) {
 		
 		if( isGET() ) {
 			/* if the name is missing use the first as default */
 			if( Utils.isEmpty(name) && bundle.get().services!=null && bundle.get().services.size()>0 ) { 
 				name = bundle.get().services.get(0).name;
 			}
 			
 			if( Utils.isEmpty(name) ) { 
 				error("Missing service for name for bundle: " + bundle);
 			}
 			
 			Service service = service(bundle.get().name,name);
 			render(service);
 			return;
 		}
 
 		/*
 		 * process the submitted data
 		 */
 		Service service = service(bundle.get().name,name).copy();
 		Service.current(service);
 
 		if( !service.validate(params) ) {
 			/* if the validation FAIL go back to the service page */
 			renderArgs.put("service", service);
 			render();
 			return;
 		} 
 
 		exec(bundle.get().name,service, true);
 	}
 	
 	static void exec( String bundle, Service service, boolean enableCaching ) {
 		
 		/*
 		 * 1. prepare for the execution
 		 */
 		service.init(enableCaching);
 		
 		
 		/*
 		 * 2. check if this request has already been processed in some way 
 		 */
 		Status status = service.repo().getStatus();
 		if( !status.isReady() ) {
 	    	Logger.debug("Current request status: '%s'. Forward to result page with rid: %s", status, service.rid());
 	    	result(service.rid(), service.repo().cached);
 	    	return;
 		}
 
 		/*
 		 * 3. fire the job 
 		 */
 		if( service.start() ) {
 	    	
 			/*
 	    	 * 4. store the current request-id in a cookie
 	    	 */
 	    	History history = new History(service.rid());
 	    	history.setBundle(bundle);
 	    	history.setLabel(service.title);
 	    	history.save();		
 		}
 		
 
     	/*
     	 * 5. forwards to the result page 
     	 */
     	Logger.debug("Forward to result page with rid: %s", service.rid());
     	result(service.rid());			
 	}
 
 
 	public static void servePublic( String path ) { 
 		renderStaticResponse();
 		renderFile(bundle.get().publicPath, path);
 	}
 	
 	/**
 	 * Renders a generic page provided the by bundle 
 	 * 
 	 * @param bundle 
 	 * @param path
 	 */
 	public static void html( String page ) { 
 		renderBundlePage(page);
 	}
 	
 	/**
 	 * Try to load the specied page template in the bundle context 
 	 * if does not exists fallback on the application scope 
 	 * 
 	 * @param page
 	 * @param args
 	 */
 	static void renderBundlePage( String page, Object... args) { 
 		Bundle bundle = Application.bundle.get();
 		if( bundle != null && bundle.pagesPath != null && bundle.pagesPath.child(page) .exists()) { 
 			renderArgs.put("_page", page);
 			render("Application/_wrapper.html", args);
 		}
 		else { 
 			render("Application/" + page, args);
 		}
 
 	}
 
 
 	/** 
 	 * Render the bundle css content
 	 */
	@CacheFor("10d")
 	public static void css() { 
 		File file = Application.bundle.get().cssPath;
 		if( file == null || !file.exists() ) { 
 			notFound("Cannot render CSS file for bundle: ", Application.bundle.get().name );
 		}
 		
 		response.contentType = "text/css";
 		renderText( IO.readContentAsString(file) );
 	}
 	
 }
