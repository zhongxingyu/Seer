 package models;
 
 import java.io.File;
 import java.io.Serializable;
 import java.net.URLDecoder;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.internet.InternetAddress;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import play.Logger;
 import play.data.validation.Validation;
 import play.db.jpa.JPAPlugin;
 import play.db.jpa.NoTransaction;
 import play.jobs.Job;
 import play.mvc.Http.Request;
 import play.mvc.Router;
 import play.mvc.Scope.Params;
 import play.mvc.Scope.Session;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
  
 /** 
  * Defines an runnable service contained by a {@link Bundle}
  * 
  * @author Paolo Di Tommaso
  *
  */
 @XStreamAlias("service")
 public class Service implements Serializable {
 
 	static ThreadLocal<Service> CURRENT = new ThreadLocal<Service>();
 	
 	/** the parent configuration object */
 	@XStreamOmitField
 	public Bundle bundle; 
 
 	/**  a string indicating the origin of this request e.g. <code>web</code>, <code>email</code>, etc */
 	@XStreamOmitField
 	public String source = "web";
 	
 	@XStreamOmitField
 	public String sessionId;
 
 	@XStreamOmitField
 	public String userEmail;
 	
 	
 	@XStreamOmitField ContextHolder fContextHolder;
 	@XStreamOmitField String fRid;
 	@XStreamOmitField Repo fRepo;
 	@XStreamOmitField Date fStartTime;
 	@XStreamOmitField String fRemoteAddress;
 	@XStreamOmitField String fLocation;
 	
 	/**
 	 * The unique service name
 	 */
 	@XStreamAsAttribute
 	public String name; 
 
 	/** A label used to describe the group to which this service belongs to */
 	public String group;
 	
 	/**
 	 * The service main <i>title</i>. It will be displayed in the index page and on top of the service form input data
 	 */
 	public String title;
 
 	/**
 	 * The service <i>description</i>. It will be displayed in the index page and on top of the service form input data
 	 */
 	public String description;
 
 	/**
 	 * The reference to the related ncbi articles related to the selected tcoffee service
 	 */
 	public String cite; 
 	
 	
 	/**
 	 * The input data model
 	 */
 	public Input input;
 	
 
 	/**
 	 * Define the main job to be executed in this service
 	 */
 	public ProcessCommand process;
 	
 	
 	/**
 	 * Defines the output of this service execution 
 	 */
 	public Output output;
 	
 	
 	/** 
 	 * The action method that will handle this service
 	 */
 	public String action;
 	
 	public String getAction() { 
 		return Utils.isEmpty(action) ? "Application.main" : action;
 	}
 	
 	/**
 	 * The defualt constructor. Initialize the class to empty 
 	 */
 	public Service() {
 		
 	}
 	
 	public Service( String name ) {
 		this.name = name;
 	}
 	
 	public Service( final Bundle bundle ) {
 		this.bundle = bundle;
 	}
 	
 	
 	/** 
 	 * Module cony constructor. Creates a copy of <code>that</code> instance
 	 */
 	public Service( Service that ) {
 		this.bundle = that.bundle; // <-- be aware the - parent - configuration must NOT be copied 
 		this.name = Utils.copy(that.name); 
 		this.group = Utils.copy(that.group);
 		this.title = Utils.copy(that.title); 
 		this.description = Utils.copy(that.description); 
 		this.cite = Utils.copy(that.cite);
 		this.input = Utils.copy(that.input);
 		this.process = Utils.copy(that.process);
 		this.output = Utils.copy(that.output);
 		this.action = that.action;
 	}
 	
 	/**
 	 * @return a cloned instance of the current service
 	 */
 	public Service copy() {
 		return new Service(this);
 	}
 	
 	public String getTitle() {
 		return Utils.isNotEmpty(title) ? title : name;
 	}
 	
 	public String rid() {
 		if( fRid != null ) {
 			return fRid;
 		}
 
 		return fRid = getRid();
 	}
 	
 	private String getRid() {
 		return getRid(true);
 	}
 	
 	private String getRid( boolean enableCaching ) {
 		
 		int hash = input != null ? input.hashFields() : Utils.hash();
 		hash = Utils.hash(hash, this.name);
 		hash = Utils.hash(hash, this.sessionId);
 		hash = Utils.hash(hash, this.bundle.getLastModified());
 		hash = Utils.hash(hash, this.userEmail);
 
 		/* 
 		 * Avoid clash on existing folder with unknown status, 
 		 * so basically check if for the current hash (rid) already exists 
 		 * a folder, if so loop until a non existing hash(<--> folder) is found 
 		 */
 		String result = Integer.toHexString(hash);
 		Repo check = new Repo(result,false);
 		while( check.getFile().exists() ) {
 			Status status = check.getStatus();
 			if( !enableCaching || check.isExpired() || status .isUnknown() ) {
 				// force a new hash id 
 				hash = Utils.hash(hash,result);
 				result = Integer.toHexString(hash);
 				check = new Repo(result,false);
 			}
 			else {
 				Logger.info("Re-using and existing request-id: '%s' - caching: %s - status: '%s' - expired: '%s'", result, enableCaching, status, check.isExpired());
 				break;
 			}
 		}
 		
 		return fRid = result;
 		
 	} 
 
 	@Deprecated
 	public File folder() {
 		return fRepo != null ? fRepo.getFile() : null;
 	}
 	
 	public Repo repo() {
 		return fRepo;
 	}
 	
 	public static Service current() {
 		return CURRENT.get();
 	}
 	
 	public static Service current(Service service) {
 		CURRENT.set(service);
 		return service;
 	}
 	
 	public static void release() {
 		CURRENT.set(null);
 	}
 
 	/**
 	 * Validate all input fields against the specified http parameters 
 	 * @param params the parameters on the current http request  
 	 * @return <code>true</code> if validation is OK <code>false</code> otherwise 
 	 */
 	public boolean validate(Params params) {
 		fRid = null; // <-- invalidate the current 'request-id' if exists 
 		input.bind(params);
 		input.validate();
 		return !Validation.hasErrors();
 	} 
 	
 	/*
 	 * set a variable into the binding context
 	 */
 	void setVariable( Field field ) {
 		Object value = null;
 		
 		/* just skip empty fields */
 		if( Utils.isEmpty(field.value) ) {
 			return;
 		}
 		
 		/*
 		 * memo fields are store as temporary files and in the context is passed that file 
 		 */
 		if( "memo".equals(field.type) ) {
 			value = field.hasFile() 
 				  ? field.getFile()
 				  : repo().store( field.value ); // <-- create a temporary file 
 
 		}
 
 		/* 
 		 * file are are managed in a similar way that 'memo' field
 		 */
 		else if( "file".equals(field.type) ) {
 			if( !field.hasFile() || field.getFile().length() == 0 ) {
 				/* empty file - do not put this variable on the context */
 				return;
 			}
 			
 			value = field.getFile();
 			
 		}
 		else {
 			value = field.value;
 		}
 
 		setVariable(field.name, value);
 	}
 
 	void setVariable( String key, Object value ) {
 		
 		if( fContextHolder.map.containsKey(key) ) {
 			/* if an entry already exist with this key, repack it as a list */
 			Object item = fContextHolder.map.get(key);
 			if( item instanceof List ) {
 				((List)item) .add(value);
 				return;
 			}
 			else {
 				List<Object> list = new ArrayList<Object>();
 				list.add(item);
 				list.add(value);
 				value = list;
 			} 
 		}
 		
 		/* 
 		 * put on the context 
 		 */
 		fContextHolder.map.put(key, value);
 		
 	}
 	
 
 
 	public ContextHolder getContext() {
 		return fContextHolder;
 	} 
 	
 	/**
 	 * Evaluate the string replacing variables in the form ${varname}
 	 * 
 	 * @param raw the string containing variables to replace
 	 * @return the string wioth resolved variables 
 	 */
 	public String eval(String raw) { 
 		Eval evaluator = new Eval(raw);
 		return evaluator.eval(fContextHolder.map);
 	}
 	
 	public void init() {
 		init(true);
 	}
 	
 	/**
 	 * Prepare the <i>service</i> to be executed 
 	 */
 	public void init( boolean enableCaching ) {
 		
 		/*
 		 * 0. session ID
 		 */
 		if( sessionId == null && Session.current() != null ) { 
 			sessionId = Session.current().getId();
 		}		
 		
 		/*
 		 * 1. request information
 		 *    - IP 
 		 *    - location info
 		 */
 		Request req = Request.current();
 		fStartTime = new Date();
 		
 		if( req != null ) {
 			// Get the remote IP address
 			if( fRemoteAddress == null ) { 
 				fRemoteAddress = Request.current().remoteAddress;
 			}
 			
 			// Fetch the location information for the cookie 
 			if( req.cookies.get("location") != null && (fLocation=req.cookies.get("location").value) != null ) {
 				try {
 					fLocation = URLDecoder.decode(fLocation, "utf-8");
 				} 
 				catch (Exception e) {
 					Logger.warn(e, "Unable to decode location cookie: %s", fLocation);
 				}
 			}
 			else { 
 				Logger.warn("Missing 'location' cookie for session '%s'", sessionId);
 			}
 		}
 
 		
 		if( userEmail == null && input != null ) { 
 			/* Try to discover the user email looking on the email field, 
 			 * anyway this is really a dirty trick because it is tied to the field name used in the 
 			 * bundle.xml configuration. If that name chage, this link will be broken 
 			 * TODO find something better 
 			 */
 			try { 
 				Field field = input.getField("email");
 				if( field != null ) { 
 					List<InternetAddress> list = Mail.asList(field.value);
 					userEmail = list != null && list.size()>0 ? list.get(0).getAddress() : null;
 				}
 				
 			} catch ( Exception e) { 
 				Logger.warn(e, "Unable to parse user email from input fields");
 			}
 		}
 		
 		/*
 		 * 1. create the context repository folder for this execution 
 		 */
 		fRid = getRid(enableCaching);
 		fRepo = new Repo(fRid,true);
 	
 		/*
 		 * 2. initialize the context for the expression evaluation 
 		 */
 		fContextHolder = new ContextHolder();
 		fContextHolder.input = input;
		fContextHolder.result = new OutResult();
 		
 		AppProps props = AppProps.instance();
 		for( String key : props.getNames() ) { 
 			String val;
 			if( (val=props.getString(key,null)) != null ) { 
				fContextHolder.map.put(key, val);
 			}
 		}
 		
 		/* add the bundle properties content */
 		for( Object key : bundle.properties.keySet() ) {
			fContextHolder.map.put( key.toString(), bundle.properties.getProperty(key.toString()));
 		}
 
 		/* the private folder for this service */
		fContextHolder.map.put( "data.path", fRepo.getPath() );
 
 		/* some 'special' variables */
		fContextHolder.map.put("_rid", rid());
		fContextHolder.map.put("_result_url", getResultURL());
 		
 		
 		/* 
 		 * 3. put all fields value as context variables 
 		 */
 		for( Field field : input.fields() ) {
 			field.consolidate( fRepo.getFile() );
 			setVariable(field);
 		}
 		
 		/*
 		 * 4. store the input so that can be used to re-submit job execution
 		 */
 		input.save( fRepo.getInputFile() );
 	}
 
 	
 	public String getResultURL() {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("bundle", bundle.name);
 		params.put("rid", fRid);
 		
 		String host = AppProps.instance().getHostName();
 		String path = Router.reverse("Application.result", params).toString();
 		return "http://" + host + path;
 	}
 	
 	public boolean start() {
 
 		/* check if a main process is defined otherwise skip it */
 		if( process == null ) { 
 			Logger.warn("Nothing to process");
 			return false;
 		};
 
 		
 		/* try to lock this context for execution */
 		if( !fRepo.lock() ) {
 			// if cannot be lock it mean that the job is still running 
 			return false;
 		}
 		
 		
 		/* 
 		 * create an aysnc execution context and fire the execution 
 		 */
     	new ServiceJob().now();
     	return true;
 	}
 	
 	@NoTransaction
 	class ServiceJob extends Job  {
 		
 		@Override
 		public void doJob() throws Exception {
 			/*
 			 * run the alignment job
 			 */
 			Service.current(Service.this);
 			
 			UsageLog log = safeTrace(null);
 			Long logid = log != null ? log.id : null;
 			
 			try {
 				/* run the job */
 				Service.this.run();
 			}
 			finally  {
 				try { Service.this.fRepo.unlock(); } catch( Exception e ) { Logger.error(e, "Failure on context unlock"); }
 				if( logid != null ) { 
 					safeTrace(logid);
 				}
 				Service.release();
 			}
 		}
 	}; 
 	
 	/**
 	 * Append a line in the server requests log with the following format 
 	 * 
 	 * <start time>, <user ip>, <bundle name>, <service name>, <request id>, <elapsed time>, <status> 
 	 */
 	UsageLog trace( Long id ) {
 		
 		UsageLog usage = null;
 		if( id == null ) { 
 			usage = new UsageLog();
 			usage.creation = new Timestamp(this.fStartTime.getTime());
 			usage.ip = this.fRemoteAddress;
 			usage.bundle = this.bundle.name;
 			usage.service = this.name;
 			usage.requestId = this.fRid;
 			usage.sessionId = this.sessionId;
 			usage.status = "RUNNING";
 			usage.source = this.source;
 			usage.email = this.userEmail;
 			// add location data
 			fetchLocationData(usage, fLocation);
 			
 			Logger.debug("Creating usage log for request # %s; sessionid: %s; email: %s", this.fRid, this.sessionId, this.userEmail );
 		}
 		else if( id >=0 ){ 
 			usage = UsageLog.findById( id );
 
 			if( fContextHolder != null && fContextHolder.result != null ) { 
 				usage.duration = Utils.asDuration(fContextHolder.result.elapsedTime);
 				usage.status = fContextHolder.result.status.name() ;
 				usage.elapsed = fContextHolder.result.elapsedTime/1000;
 			}
 
 			Logger.debug("Updating usage log for request # %s", this.fRid );
 		}
 
 		return usage.save();
 	}
 	
 	private void fetchLocationData(UsageLog usage, String location) {
 
 		if( StringUtils.isBlank(location) ) {  
 			return; 
 		}
 		
 		JSONObject loc = (JSONObject) JSONValue.parse(location);
 		if( loc == null ) {
 			return;
 		}
 		
 		// the source of this info
 		usage.locationProvider = Utils.asString(loc.get("source"));
 
 		// coordinates 
 		usage.lng = Utils.asString(loc.get("longitude"));
 		usage.lat = Utils.asString(loc.get("latitude"));
 		
 		// location specific data
 		if( "ipinfodb".equalsIgnoreCase(usage.locationProvider)) {
 			usage.country = WordUtils.capitalizeFully((String) loc.get("countryName"));
 			usage.countryCode = WordUtils.capitalizeFully((String) loc.get("countryCode"));
 			usage.city = WordUtils.capitalizeFully((String) loc.get("cityName"));
 			usage.ip = (String) loc.get("ipAddress");
 		}
 		
 		// fallback on default (google)
 		else  {
 			loc = (JSONObject) loc.get("address");
 			if( loc != null ) {
 				usage.country = (String) loc.get("country");
 				usage.countryCode = (String) loc.get("country_code");
 				usage.city = (String) loc.get("city");
 			}
 		}
 		
 	}
 
 	UsageLog safeTrace( Long id ) { 
 		UsageLog result=null;
 		JPAPlugin.startTx(false);
 		try { 
 			result = trace(id);
 			JPAPlugin.closeTx(false);
 			return result;
 		} 
 		catch( Exception e ) { 
 			Logger.error(e, "Error on tracing request-id: %s ", fRid);
 			try { JPAPlugin.closeTx(true); } catch( Exception fail ) { Logger.warn(fail, "Error rolling back transaction"); } 
 			return result;
 		}
 	}
 
 	void run() {
 		
 		OutResult fOutResult = fContextHolder.getResult();
 		try {
 			/* 
 			 * initialize the process 
 			 */
 
 			process.init(fContextHolder); // <-- pass to the command context the save variables
 			
 			/* 
 			 * the main execution 
 			 */
 			boolean success = false; 
 
 			
 			/* run the main job */
 			try {
 				success = process.execute();
 			}
 			finally {
 				
 				/*
 				 * if result is OK handle the commands for valid case  
 				 */
 				OutSection branch = getOutSection(success);
 				fOutResult.addAll( branch.result );
 				
 				fOutResult.status = success ? Status.DONE : Status.FAILED;
 				fOutResult.bundle = bundle.name;
 				fOutResult.service = this.name;
 				fOutResult.title = this.title;
 				fOutResult.cite = this.cite;
 				
 				/*
 				 * execute the result events 
 				 */
 				if( branch.hasEvents() ) {
 					branch.events.init(fContextHolder);	// init with the current context
 					branch.events.execute();
 				}	
 				
 				/*
 				 * normalize path on result items 
 				 */
 				resolveOutFilesPath(fOutResult);
 			}
 
 		}
 		catch( Exception e ) {
 			/* trace the error in the log file */
 			Logger.error(e, "Error processing request # %s", fRid);
 			fOutResult.status = Status.FAILED;
 			fOutResult.addError( e.getMessage() );
 			
 		}
 		finally {
 			/* garantee to save the result object in any case */
 			fRepo.saveResult(fOutResult);
 		}
 
 
 	}
 	
 	/**
 	 * Resolve file system and web paths for files in {@link OutItem} instances 
 	 */
 	void resolveOutFilesPath( OutResult fOutResult ) { 
 		for( OutItem item : fOutResult.getItems() ) { 
 			if( item.file == null && item.name != null) { 
 				item.file = this.repo().getFile(item.name);
 			}
 			
 			if( item.webpath == null && item.file != null ) { 
 				item.webpath = webPathFor(item.file);
 			}
 			
 		}
 	}
 
 
 	
 	private static String webPathFor( File file ) {
 		if( file == null ) { 
 			return null;
 		}
 		/*
 		 * the file path have to be published under the framework root, 
 		 * being so the 'framework path' is the prefix of the file full path
 		 */
 		String context = AppProps.instance().getContextPath();
 		String path = file.getAbsolutePath();
 		String root = AppProps.instance().getDataPath();
 		
 		String result = null;
 		int p = path.indexOf(root);
 		if( p==0 ) {
 			result = path.substring(root.length());
 			if( result.charAt(0) != '/' ) {
 				result = "/" + result;
 			}
 			result = "/data" + result;
 			
 			if( Utils.isNotEmpty(context)) {
 				result = context + result;
 			}
 			
 		}
 		
 		return result;
 	}	
 	
 	OutSection getOutSection(boolean status) {
 		/* initialize the standard output if it has not been specified */
 		OutSection section = defaultOutSection(status); 
 		
 		if( output != null ) {
 			section.addAll( status ? output.valid : output.fail );
 		}
 
 		/* garantee a result object */
 		if( section.result == null ) {
 			section.result = new OutResult();
 		}
 
 		return section;
 	} 
 	
 	OutSection defaultOutSection(boolean success) {
 		OutSection out = null;
 		
 		if( bundle != null && bundle.def != null ) {
 			if( success && bundle.def.validResult != null ) {
 				out = new OutSection(bundle.def.validResult);
 			}
 			else if( !success && bundle.def.failResult != null ){
 				out = new OutSection(bundle.def.failResult);
 			}
 		}
 		
 		if( out == null ) {
 			out = new OutSection();
 			out.result = new OutResult();
 		}
 		
 		return out;
 	}
 	
 	
 	/**
 	 * Replace all variables in the environment with the specified context and return it
 	 * 
 	 * @return
 	 */
 	public Map<String,String> defaultEnvironment() {
 
 		if( bundle == null || bundle.environment == null ) { 
 			return null;
 		}
 	
 		
 		Map<String,String> result = new HashMap<String,String>();
 		
         // Resolve ${..}
         Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
         for (Object key : bundle.environment.keySet()) {
             String value = bundle.environment.getProperty(key.toString());
             Matcher matcher = pattern.matcher(value);
             StringBuffer newValue = new StringBuffer();
             while (matcher.find()) {
                 String var = matcher.group(1);
                 String replace = null;
                 if( var != null && var.startsWith("env.")) { 
                 	replace = System.getenv(var.substring(4));
                 }
                 else if( fContextHolder.map != null ) { 
                 	replace = fContextHolder.map.get(var) != null ? fContextHolder.map.get(var).toString() : null; 
                 }
                 
                 if (replace == null) {
                     Logger.warn("Cannot replace variable \"%s\" in entry \"%s\" = \"%s\"", var, key, value);
                     replace = "";
                 }
                 matcher.appendReplacement(newValue, replace.replaceAll("\\\\", "\\\\\\\\"));
             }
             matcher.appendTail(newValue);
             result.put(key.toString(), newValue.toString());
         }		
         
         return result;
 	}		
 
 	
 	
 }
