 package com.pagesociety.web.module.resource;
 
 
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import javax.activation.MimetypesFileTypeMap;
 
 
 import com.pagesociety.persistence.Entity;
 import com.pagesociety.persistence.PersistenceException;
 import com.pagesociety.persistence.Types;
 import com.pagesociety.util.FileInfo;
 import com.pagesociety.util.Text;
 import com.pagesociety.web.UserApplicationContext;
 import com.pagesociety.web.WebApplication;
 import com.pagesociety.web.exception.InitializationException;
 import com.pagesociety.web.exception.SyncException;
 import com.pagesociety.web.exception.WebApplicationException;
 import com.pagesociety.web.gateway.RawCommunique;
 import com.pagesociety.web.module.Export;
 import com.pagesociety.web.module.Module;
 import com.pagesociety.web.module.WebStoreModule;
 import com.pagesociety.web.upload.MultipartForm;
 import com.pagesociety.web.upload.MultipartFormConstants;
 import com.pagesociety.web.upload.MultipartFormException;
 import com.pagesociety.web.upload.UploadProgressInfo;
 
 public class ResourceModule extends WebStoreModule 
 {
 
 	private static final String PARAM_UPLOAD_TEMP_DIR		 = "upload-temp-dir";
 	private static final String PARAM_UPLOAD_MAX_FILE_SIZE	 = "upload-max-file-size";	
 	private static final String KEY_PENDING_UPLOAD_EXCEPTION = "_resource_pending_upload_expection";
 
 	private static final String KEY_CURRENT_UPLOAD_MAP   = "_current_upload_map_";
 	private static final String FORM_ELEMENT_CHANNEL     = "channel";
 	private static final String FORM_ELEMENT_RESOURCE_ID = "resource_id";
 
 	protected String SLOT_PATH_PROVIDER = "resource-path-provider";
 	
 	private File				 	upload_temp_dir;
 	private long				 	upload_max_file_size;
 	public    IResourcePathProvider 	path_provider;
 
 
 	/* look at the useage of this. this is a trempory work around to let
 	 * other modules subclass this and not have to rewrite the multipart methods
 	 * and still provide their own entity.see PosteraSystemsMoudle. heritence in
 	 * store would probably clean this up a lot!!! */
 	private String resource_entity_name;
 	
 	private static final List<UploadProgressInfo> EMPTY_UPLOAD_PROGRESS_LIST = new ArrayList<UploadProgressInfo>(0);
 
 	public void init(WebApplication app, Map<String,Object> config) throws InitializationException
 	{
 		if(resource_entity_name == null)
 			resource_entity_name = RESOURCE_ENTITY;
 		super.init(app,config);	
 		path_provider = (IResourcePathProvider)getSlot(SLOT_PATH_PROVIDER);
 		//this may have been set by some one extending the class//
 		//it needs to happen before init so that it is set when define
 		// entities is called etc...//
 		set_parameters(config);
 	}
 
 	protected void defineSlots()
 	{
 		super.defineSlots();
 		DEFINE_SLOT(SLOT_PATH_PROVIDER,IResourcePathProvider.class,true);
 	}	
 	
 	protected void setResourceEntityName(String name)
 	{
 		System.out.println("SETTING RESOURCE ENTITY NAME TO "+name);
 		resource_entity_name = name;
 	}
 	
 	public String getResourceEntityName()
 	{
 		return resource_entity_name;
 	}
 	
 	private void set_parameters(Map<String,Object> config) throws InitializationException
 	{
 
 		String temp_dir = (String)config.get(PARAM_UPLOAD_TEMP_DIR);
 		if(temp_dir == null)
 			upload_temp_dir 	= new File(System.getProperty("java.io.tmpdir"));
 		else
 		{
 			try{
 				upload_temp_dir = new File(temp_dir);
 				upload_temp_dir.mkdirs();
 			}catch(Exception e)
 			{
 				throw new InitializationException("RESOURCE MODULE UNABLE TO INIT UPLOAD TEMP DIR "+upload_temp_dir);
 			}
 		}
 		
 		String s_upload_max_file_size = (String)config.get(PARAM_UPLOAD_MAX_FILE_SIZE);
 		if(s_upload_max_file_size == null)
 		{
 			upload_max_file_size = 0;//0 means unlimited upload size
 		}
 		else
 		{
 			try{
 				upload_max_file_size = Long.parseLong(s_upload_max_file_size);
 			}catch(Exception e)
 			{
 				throw new InitializationException(PARAM_UPLOAD_MAX_FILE_SIZE+" MUST BE SPECIFIED AS AN INTEGER. NUMBER OF BYTES.");
 			}
 		}
 	
 	
 	}
 
 	public static final String CAN_CREATE_RESOURCE   = "CAN_CREATE_RESOURCE";
 	public static final String CAN_READ_RESOURCE 	 = "CAN_READ_RESOURCE";
 	public static final String CAN_UPDATE_RESOURCE 	 = "CAN_UPDATE_RESOURCE";
 	public static final String CAN_DELETE_RESOURCE   = "CAN_DELETE_RESOURCE";
 	public static final String CAN_GET_RESOURCE_URL  = "CAN_GET_RESOURCE_URL";
 	
 	protected void exportPermissions()
 	{
 		EXPORT_PERMISSION(CAN_CREATE_RESOURCE); 
 		EXPORT_PERMISSION(CAN_READ_RESOURCE); 	
 		EXPORT_PERMISSION(CAN_UPDATE_RESOURCE); 
 		EXPORT_PERMISSION(CAN_DELETE_RESOURCE);  
 		EXPORT_PERMISSION(CAN_GET_RESOURCE_URL); 
 
 	}
 	
 	/////////////////BEGIN  M O D U L E   F U N C T I O N S/////////////////////////////////////////
 
 
 	
 	@Export(ParameterNames={"upload"})
 	public boolean CreateResource(UserApplicationContext uctx,MultipartForm upload) throws WebApplicationException,PersistenceException
 	{	
 		Entity user = (Entity)uctx.getUser();
 		try{
 			GUARD(user, CAN_CREATE_RESOURCE,GUARD_TYPE,resource_entity_name);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, e);
 			return false;
 		}
 		return do_upload(uctx, upload, false, null);
 	}
 	
 
 	@Export(ParameterNames={"upload"})
 	public boolean UpdateResource(UserApplicationContext uctx,MultipartForm upload) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 
 		Entity update_resource;
 		long resource_id = upload.getLongParameter(FORM_ELEMENT_RESOURCE_ID);
 		if(resource_id == -1)
 		{
 			Exception e = new WebApplicationException("UPDATING RESOURCE FILE REQUIRES A PARAMETER NAMED 'resource_id' IN THE QUERY STRING OF THE POST URL.");
 			e.printStackTrace();
 			uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, e);
 			return false;	
 		}
 		update_resource = GET(resource_entity_name,resource_id);
 		
 		try{
 			GUARD(user, CAN_UPDATE_RESOURCE, 
 						GUARD_INSTANCE,update_resource);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, e);
 			return false;
 		}
 		return do_upload(uctx, upload, true, update_resource);
 	}
 	
 	@Export(ParameterNames={"resource_id"})
 	public Entity DeleteResource(UserApplicationContext uctx,long resource_id) throws WebApplicationException,PersistenceException
 	{	
 		//check to make sure it exists//
 		Entity user = (Entity)uctx.getUser();
 		Entity resource = GET(resource_entity_name,resource_id);
 		GUARD(user,CAN_DELETE_RESOURCE,
 				   GUARD_INSTANCE,resource);
 		return deleteResource(resource);
 	}
 	
 	
 	public Entity deleteResource(Entity resource) throws WebApplicationException,PersistenceException
 	{
 		if(resource == null)
 			return null;
 		resource = EXPAND(resource);
 		String path_token = (String)resource.getAttribute(RESOURCE_FIELD_PATH_TOKEN);
 		if(path_token == null)
 			throw new WebApplicationException("THE RESOURCE EXISTS BUT HAS NO PATH TOKEN.");
 		
 		DELETE(resource);
 		path_provider.delete(path_token);			
 		return resource; 
 	}
 	
 	@Export(ParameterNames={"resource_id"})
 	public Entity DeleteResourcePreviews(UserApplicationContext uctx,long resource_id) throws WebApplicationException,PersistenceException
 	{	
 		//check to make sure it exists//
 		Entity user = (Entity)uctx.getUser();
 		Entity resource = GET(resource_entity_name,resource_id);
 		GUARD(user,CAN_DELETE_RESOURCE,
 				   GUARD_INSTANCE,resource);
 		return deleteResourcePreviews(resource);
 	}
 	
 	
 	public Entity deleteResourcePreviews(Entity resource) throws WebApplicationException,PersistenceException
 	{
 		if(resource == null)
 			return null;
 		resource = EXPAND(resource);
 		String path_token = (String)resource.getAttribute(RESOURCE_FIELD_PATH_TOKEN);
 		if(path_token == null)
 			throw new WebApplicationException("THE RESOURCE EXISTS BUT HAS NO PATH TOKEN.");
 		
 		path_provider.deletePreviews(path_token);			
 		return resource; 
 	}
 	
 	@Export(ParameterNames={"resource_id"})
 	public String GetResourceURL(UserApplicationContext uctx,long resource_id) throws WebApplicationException,PersistenceException
 	{	
 		//check to make sure it exists//
 		Entity user = (Entity)uctx.getUser();
 		Entity resource = GET(resource_entity_name,resource_id);
 		GUARD(user, CAN_GET_RESOURCE_URL,
 				  GUARD_INSTANCE,resource);
 		return getResourceURL(resource);
 	}
 	
 	
 	public String getResourceURL(Entity resource) throws WebApplicationException
 	{
 		String path_token = (String)resource.getAttribute(RESOURCE_FIELD_PATH_TOKEN);
 		if(path_token == null)
 			throw new WebApplicationException("THE RESOURCE EXISTS BUT HAS NO PATH TOKEN.");
 		return path_provider.getUrl(path_token);		
 	}
 	
 	@Export(ParameterNames={"resource_ids"})
 	public List<String> GetResourceURLs(UserApplicationContext uctx,List<Long> resource_ids) throws WebApplicationException,PersistenceException
 	{	
 		//check to make sure it exists//
 		Entity user = (Entity)uctx.getUser();
 		List<Entity> resources = IDS_TO_ENTITIES(resource_entity_name,resource_ids);
 		int s = resources.size();
 		List<String> urls = new ArrayList<String>(s);
 		//move the file to the right place
 		for (int i=0; i<s; i++)
 		{
 			Entity resource = GET(resource_entity_name,resources.get(i).getId());
 			GUARD(user, CAN_GET_RESOURCE_URL,
 					  GUARD_INSTANCE,resource);
 			urls.add( getResourceURL(resource));
 		}
 		return urls;
 	}
 
 
 	
 	@Export(ParameterNames={"resource_id", "w", "h"})
 	public String GetResourcePreviewURLWithDim(UserApplicationContext uctx,long resource_id,int w, int h) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity resource = GET(resource_entity_name,resource_id);
 		GUARD(user, CAN_GET_RESOURCE_URL,
 				 GUARD_INSTANCE,resource);
 		return getResourcePreviewUrlWithDim( resource, w, h);
 	
 	}
 	 
 	//@Export 
 	public void TestPreviewConcurrency(UserApplicationContext uctx,RawCommunique c) throws WebApplicationException,PersistenceException
 	{
 		System.out.println("TESTING 1 2 3");
 		final Entity resource = GET("Resource",1);
 		System.out.println("RESOURCE IS: "+resource);
 		final int w = (int)((new Random().nextDouble()*100))+100;
 		final int h = 350;
 		
 		Thread[] tt = new Thread[16];
 		for(int i = 0;i < 16;i++)
 		{
 			tt[i] = new Thread("TEST THREAD-"+i)
 			{
 				public void run()
 				{
 					try{
 						getResourcePreviewUrlWithDim(resource, w, h);
 					}catch(Exception e)
 					{
 						e.printStackTrace();
 					}
 					System.out.println(Thread.currentThread().getName()+" FINISHED");
 				}
 			};
 		}
 		
 		for(int i = 0;i < 16;i++)
 		{
 			tt[i].start();
 		}
 	}
 	
 	public String getResourcePreviewUrlWithDim(Entity resource,int w,int h) throws WebApplicationException
 	{
 		String path_token = (String)resource.getAttribute(RESOURCE_FIELD_PATH_TOKEN);
 		if(path_token == null)
 			throw new WebApplicationException("THE RESOURCE EXISTS BUT HAS NO PATH TOKEN.");
 
 		// FIXME handle previews for TIFFs (& PDFs) too
 		// TODO if its a TIF or PDF, the previews should be converted to jpg
 		//		if(!resource.getAttribute(RESOURCE_FIELD_SIMPLE_TYPE).equals(FileInfo.SIMPLE_TYPE_IMAGE_STRING))
 		//			throw new WebApplicationException("RESOURCE "+resource+" IS NOT OF SIMPLE TYPE IMAGE. CAN'T RESIZE.");
 		
 		String url = null;
 		try{
 			url = path_provider.getPreviewUrl(path_token,w,h);
 		}catch(WebApplicationException wae)
 		{
 			ERROR(wae); 
 		}
 		return url;
 	}
 
 
 
 	@Export(ParameterNames={"resources", "w", "h"})
 	public List<String> GetResourcePreviewURLSWithDim(UserApplicationContext uctx,List<Entity> resources,int w, int h) throws WebApplicationException,PersistenceException
 	{
 
 		//check to make sure it exists//
 		return GetResourcePreviewURLsWithDim(uctx, ENTITIES_TO_IDS(resources), w, h);
 		
 	}
 	@Export(ParameterNames={"resource_ids", "w", "h"})
 	public List<String> GetResourcePreviewURLsWithDim(UserApplicationContext uctx,List<Long> resource_ids,int w, int h) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		//check to make sure it exists//
 		List<Entity> resources = IDS_TO_ENTITIES(resource_entity_name,resource_ids);
 		int s = resources.size();
 		List<String> urls = new ArrayList<String>(s);
 		//move the file to the right place
 		for (int i=0; i<s; i++)
 		{
 			Entity resource = GET(resource_entity_name,resources.get(i).getId());
 			GUARD(user, CAN_GET_RESOURCE_URL,
 					  GUARD_INSTANCE,resource);
 			urls.add( getResourcePreviewUrlWithDim(resource, w, h));
 		}
 		return urls;
 	}
 	
 	@Export(ParameterNames={"channel_name"})
 	public UploadProgressInfo GetUploadProgress(UserApplicationContext uctx,String channel_name) throws PersistenceException,WebApplicationException
 	{
 		//System.out.println("GET PROGRESS SESSION ID IS "+uctx.getId());
 		check_exceptions(uctx);
 		Entity user = (Entity)uctx.getUser();
 		GUARD(user, CAN_CREATE_RESOURCE,GUARD_TYPE,resource_entity_name);
 		
 		Map<String,MultipartForm> channel_upload_map = (Map<String,MultipartForm>)uctx.getProperty(KEY_CURRENT_UPLOAD_MAP);
 		if(channel_upload_map == null)
 			return null;
 		
 		MultipartForm channel_upload = channel_upload_map.get(channel_name);
 		if(channel_upload == null)
 			return null;
 		
 		UploadProgressInfo ret = channel_upload.getUploadProgressInfo();
 
 		if(ret.getCompletionObject() != null)
 		{
 			System.out.println("UPLOAD IS COMPLETE FOR SESSION"+uctx.getId());
 			channel_upload_map.remove(channel_name);
 		}
 		return ret;
 	}
 	
 	@Export(ParameterNames={"channel_name"})
 	public UploadProgressInfo CancelUpload(UserApplicationContext ctx,String channel_name)throws PersistenceException,WebApplicationException
 	{
 		check_exceptions(ctx);
 		Entity user = (Entity)ctx.getUser();
 		GUARD(user, CAN_CREATE_RESOURCE,GUARD_TYPE,resource_entity_name);
 		
 		Map<String,MultipartForm> channel_upload_map = (Map<String,MultipartForm>)ctx.getProperty(KEY_CURRENT_UPLOAD_MAP);
 		if(channel_upload_map == null)
 			return null;
 		
 		MultipartForm channel_upload = channel_upload_map.get(channel_name);
 		if(channel_upload == null)
 			return null;
 	
 		UploadProgressInfo ret = channel_upload.getUploadProgressInfo();
 		try{
 			channel_upload.cancel();
 		}catch(IOException e)
 		{
 			//throw new WebApplicationException("COULDN'T CANCEL");
 			ERROR(e);
 		}
		channel_upload_map.remove(KEY_CURRENT_UPLOAD_MAP);
 		return ret;
 	}
 	
 	
 	
 	@Export(ParameterNames={"resource_id"})
 	public Entity GetResource(UserApplicationContext uctx,long resource_id) throws WebApplicationException,PersistenceException
 	{
 		Entity user = (Entity)uctx.getUser();
 		Entity resource = GET(resource_entity_name,resource_id);	 
 		GUARD(user, CAN_READ_RESOURCE,
 					GUARD_INSTANCE,resource);
 		return resource;
 	}
 	
 	public Entity getResource(long resource_id) throws WebApplicationException,PersistenceException
 	{
 		return GET(resource_entity_name,resource_id);	 
 	}
 	
 	public File getFile(Entity resource) throws WebApplicationException
 	{
 		String path_token = (String)resource.getAttribute(RESOURCE_FIELD_PATH_TOKEN);
 		if(path_token == null)
 			throw new WebApplicationException("THE RESOURCE EXISTS BUT HAS NO PATH TOKEN.");
 		return path_provider.getFile(path_token);		
 	}
 	
 	public String getResourceBaseURL() throws WebApplicationException
 	{
 		return path_provider.getBaseUrl();
 	}
 	
 	//TODO:GUARD
 	//this should ultimately live in the application...not a module
 	@Export
 	public List<Map<String,Object>> GetAppResourceInfo(UserApplicationContext ctx) throws WebApplicationException
 	{
 		List<Module> modules = getApplication().getModules();
 		List ret 			 = new ArrayList<Map<String,Object>>();
 
 		
 		for(int i = 0;i < modules.size();i++)
 		{
 			Module m = modules.get(i);
 			if(m instanceof ResourceModule)
 			{
 				ResourceModule rm 		= (ResourceModule)m;
 				String modulename 		= m.getName();
 				String resource_entity 		= rm.getResourceEntityName();
 				String resource_base_url 	= rm.getResourceBaseURL();
 				ret.add(new OBJECT(	"resource_module_name",modulename,
 									"resource_entity_name",resource_entity,
 									"resource_base_url",resource_base_url));
 			}
 		}
 		return ret;
 	}
 	
 	///////////////////////////////////END MODULE FUNCTIONS ///////
 	private boolean do_upload(UserApplicationContext uctx,MultipartForm upload,boolean update,Entity update_resource) throws WebApplicationException,PersistenceException
 	{
 		System.out.println("INITIATING UPLOAD FOR SESSION"+uctx.getId());
 		Entity user = (Entity)uctx.getUser();		
 		String channel_name = upload.getParameter(FORM_ELEMENT_CHANNEL);
 	
 		if(channel_name == null)
 		{
 			Exception e = new WebApplicationException("UPLOAD MUST SPECIFY CHANNEL NAME AS A PARAMETER NAMED 'channel' IN THE QUERY STRING OF THE POST URL.");
 			e.printStackTrace();
 			uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, e);
 			return false;
 		}
 		
 		Map<String,MultipartForm> current_upload_map = (Map<String,MultipartForm>)uctx.getProperty(KEY_CURRENT_UPLOAD_MAP);
 		if(current_upload_map == null)
 		{
 			current_upload_map = new HashMap<String,MultipartForm>();
 			uctx.setProperty(KEY_CURRENT_UPLOAD_MAP,current_upload_map);
 		}
 		
 		MultipartForm current_upload = current_upload_map.get(channel_name);
 		if(current_upload != null && !current_upload.isComplete() && !current_upload.isCancelled())
 		{
 		
 			Exception e = new WebApplicationException("CURRENT UPLOAD IN CHANNEL "+channel_name+" IS NOT FINISHED. CANCEL EXISITING UPLOAD FIRST.");
 			e.printStackTrace();
 			uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, e);
 			return false;
 		}
 		
 		String path_token = path_provider.getPathToken(user, Text.makeUrlSafe(upload.getFileName()));
 
 		System.out.println("UPLOADING PATH TOKEN IS "+path_token);
 		upload.setMaxUploadItemSize(upload_max_file_size);		
 		current_upload = upload;
 		current_upload_map.put(channel_name, current_upload);
 		
 		UploadProgressInfo uploaded_file_info = current_upload.getUploadProgressInfo();
 		String content_type	 		 = current_upload.getContentType();
 		String simple_type	 		 = FileInfo.getSimpleTypeAsString(current_upload.getFileName());
 		long file_size				 = current_upload.getFileSize();
 		String file_name 			 = Text.makeUrlSafe(current_upload.getFileName());	
 		String ext 					 = FileInfo.getExtension(file_name);		
 
 		System.out.println("UPLOAD CONTENT TYPE: "+content_type);
 		System.out.println("UPLOAD SIMPLE TYPE: "+simple_type);
 		System.out.println("UPLOAD FILE SIZE: "+file_size);
 		System.out.println("UPLOAD FILE NAME: "+file_name);
 		System.out.println("UPLOAD EXT: "+ext);
 
 		OutputStream[] outs=null;
 		boolean bulk_upload = false;
 		File tmp_zip_file = null;
 		//DONT NEED THIS ANYMORE UNLESS WE WANT BULK UPLOAD FROM ZIP OF RESOURCES//
 		//probably need a paramter treat-zips-as-bulk-uploads
 		
 		if(false)//ext != null && ext.toLowerCase().equals("zip"))
 		{
 			if(update)
 			{
 				Exception ee = new WebApplicationException("YOU CANT UPDATE A RESOURCE FILE WITH A ZIP. IT NEEDS TO BE A SINGLE FILE.SINCE A RESOURCE HOLDS ONE FILE."); 
 				ee.printStackTrace();
 				uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, ee);
 				return false;
 			}
 			
 			bulk_upload 		= true;
 			tmp_zip_file 		= new File(System.getProperty("java.io.tmpdir")+File.separator+file_name);
 			try{
 				outs = new OutputStream[]{new FileOutputStream(tmp_zip_file)};
 			}catch(FileNotFoundException fnf)
 			{
 				//this should not happen ever//
 				fnf.printStackTrace(); 
 			}
 		}
 		if(!bulk_upload)
 			outs = path_provider.getOutputStreams(path_token,content_type,file_size); 
 		String throw_exception_in_parse_upload = upload.getParameter("throw_exception_in_parse_upload");
 		try{
 			current_upload.parse(outs);
 			if(throw_exception_in_parse_upload != null)
 				throw new Exception("YOU TOLD ME TO THROW THIS.");
 			path_provider.endParse(path_token);
 		}catch(Exception e)
 		{
 			path_provider.delete(path_token);
 			if(current_upload.isCancelled())
 				return false;
 			
 			e.printStackTrace();
 			current_upload_map.remove(channel_name);
 			
 			Exception ee = new WebApplicationException("PROBLEM PARSING FORM ON CHANNEL "+channel_name+" "+e.getMessage(),e);
 			uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, ee);
 			return false;
 		}
 		if(bulk_upload)
 		{
 			List<Entity> resources;
 			//zip insert will set filename and title
 			try{
 				resources = add_resources_from_zip(upload,user,tmp_zip_file);
 			}catch(Exception e)
 			{
 				e.printStackTrace();
 				uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, e);
 				return false;
 			}
 			uploaded_file_info.setCompletionObject(resources);
 			System.out.println("ADDED RESOURCES "+resources);
 		}
 		else
 		{
 			String throw_exception_in_add_resource = upload.getParameter("throw_exception_in_add_resource");
 			
 			Entity resource = null;
 			try{
 				if(update)
 				{
 					do_update_resource(upload,update_resource, content_type, simple_type, file_name, ext, file_size, path_token);
 					uploaded_file_info.setCompletionObject(update_resource);
 				}
 				else
 				{
 					resource = do_add_resource(upload,user,content_type,simple_type,file_name,ext,file_size,path_token);					  
 					if(throw_exception_in_add_resource != null)
 						throw new PersistenceException("I AM THROWING THIS BECAUSE YOU TOLD ME TO.");
 					uploaded_file_info.setCompletionObject(resource);
 					
 				}
 			
 			}catch(PersistenceException pe)
 			{
 				/* if we cant create a resource we need to delete the upload */
 				try{
 					if(!update)
 						path_provider.delete(path_token);
 				}catch(Exception e){e.printStackTrace();}//swallow this one
 				
 				ERROR(pe);
 				Exception ee = new WebApplicationException("COULDNT CREATE RESOURCE FOR FILE UPLOAD",pe);
 				uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, ee);
 				return false;
 			}
 			
 			if(update)
 				System.out.println("UPDATED RESOURCE "+update_resource);
 			else
 				System.out.println("ADDED RESOURCE "+resource);
 		}
 		
 		return true;	
 	}
 	
 	private List<Entity> add_resources_from_zip(MultipartForm upload,Entity creator,File zip) throws WebApplicationException,PersistenceException
 	{
 		ZipFile zipFile = null;
 		try {
 			Enumeration entries = null;
 			zipFile = new ZipFile(zip);
 			entries = zipFile.entries();
 			List<Entity> resources = new ArrayList<Entity>();
 			while(entries.hasMoreElements())
 			{
 				ZipEntry entry = (ZipEntry)entries.nextElement();
 				if(entry.isDirectory()) {
 					// Assume directories are stored parents first then children.
 					System.err.println("Ignoring directory: " + entry.getName());
 					// This is not robust, just for demonstration purposes.
 					//  (new File(entry.getName())).mkdir();
 					continue;
 	        }
 
 	        System.err.println("Extracting file: " + entry.getName());
 		    String filename = Text.makeUrlSafe(entry.getName());
 	        if(filename.startsWith(".") || filename.startsWith("__macosx"))
 	        	continue;
 	        
 	        String ext = FileInfo.getExtension(filename);
 	        File uncompressed_entry = new File (upload_temp_dir,filename);
 	        copy_input_stream(zipFile.getInputStream(entry),
 	           new BufferedOutputStream(new FileOutputStream(uncompressed_entry)));
 	       
 	        String content_type = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
 	        String simple_type 	= FileInfo.getSimpleTypeAsString(uncompressed_entry);
 	        long file_size 		= uncompressed_entry.length();
 	        String path_token 	= path_provider.getPathToken(creator, filename);
 	        OutputStream[] os 	= path_provider.getOutputStreams(path_token,content_type,file_size);
 	      
 	        //here are getting each file from the zip and creating a resource 
 	        //for each one 
 	        FileInputStream fis = new FileInputStream(uncompressed_entry);
 	        int l = 0;
 	        byte[] buf = new byte[16384];
 	        for(int i = 0;i < os.length;i++)
 	        {
 	        	while((l = fis.read(buf)) != -1)
 	        		os[i].write(buf, 0, l);
 	        	os[i].flush();
 	        }
 	        for(int i =0;i < os.length;i++)
 	        	os[i].close();
 	        fis.close();
 	        path_provider.endParse(path_token);
 	        
 	        Entity resource 	= do_add_resource(upload,creator, content_type,simple_type, filename, ext, file_size, path_token);
 	        resources.add(resource);
 	      }
 
 	      zipFile.close();
 	      zip.delete();
 	      return resources;
 		} catch (ZipException e) {
 			e.printStackTrace();
 			try{
 			  zipFile.close();
 		      zip.delete();
 			}catch(Exception ee)
 			{
 				ee.printStackTrace();
 			}
 			throw new WebApplicationException("PROBLEM WITH ZIP. ZIP EXCEPTION. NO RESOURCES WERE CREATED");
 			
 		} catch (IOException e) {
 			 e.printStackTrace();
 			try{  
 				zipFile.close();
 				zip.delete();
 		    }catch(Exception ee)
 		    {
 		    	ee.printStackTrace();
 		    }
 		     
 			throw new WebApplicationException("PROBLEM WITH ZIP. IO EXCEPTION. NO RESOURCES WERE CREATED");
 			
 		}	
 	}
 
 	public Entity createResource(Entity creator,File f, boolean delete) throws WebApplicationException,PersistenceException
 	{
 		try{
 			INFO("CREATING RESOURCE FROM FILE "+f.getAbsolutePath()+" ("+(f.length()/1024)+" kb)");
 			String filename = f.getName();
 			String path_token 	   = path_provider.getPathToken(creator, Text.makeUrlSafe(filename));
 			String content_type = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
 			INFO("CONTENT TYPE IS "+content_type);
 			OutputStream[] os 	   = path_provider.getOutputStreams(path_token, content_type, f.length()) ;
 		
 			byte[] buf = new byte[4096];
 			FileInputStream fis = new FileInputStream(f);
 			
 			int l = 0;
 			while((l = fis.read(buf)) != -1)
 			{
 				for(int i = 0;i < os.length;i++)
 					os[i].write(buf,0,l);
 			}
 			for(int i = 0;i < os.length;i++)
 				os[i].close();
 			if(delete)
 				f.delete();
 			
 			return do_add_resource(null, creator, content_type, FileInfo.getSimpleTypeAsString(filename), filename, FileInfo.getExtension(filename), f.length(), path_token);
 		}catch(Exception e)
 		{
 			ERROR(e);
 			throw new WebApplicationException("PROBLEM ADDING RESOURCE FROM FILE");
 		}
 	}
 	
 	public Entity createResource(Entity creator,String filename,byte[] content,String mime_type) throws WebApplicationException,PersistenceException
 	{
 		try{
 			int length = content.length;
 			String path_token 	   = path_provider.getPathToken(creator, Text.makeUrlSafe(filename));
 			String content_type 	= mime_type;
 			OutputStream[] os 	   = path_provider.getOutputStreams(path_token, content_type, length);
 
 			for(int i = 0;i < os.length;i++)
 				os[i].write(content);
 
 			for(int i = 0;i < os.length;i++)
 				os[i].close();
 			INFO("CREATING RESOURCE FROM TEXT ("+(length/1024)+" kb) "+path_token);
 			
 			return do_add_resource(null, creator, content_type, FileInfo.getSimpleTypeAsString(filename), filename, FileInfo.getExtension(filename), length, path_token);
 		}catch(Exception e)
 		{
 			ERROR(e);
 			throw new WebApplicationException("PROBLEM ADDING RESOURCE FROM FILE");
 		}
 	}
 	
 	
 	protected Entity do_add_resource(MultipartForm upload,Entity creator,String content_type,String simple_type,String filename,String ext,long file_size,String path_token) throws WebApplicationException,PersistenceException
 	{
 
 		Object[] annotations = annotate(upload,creator,content_type,simple_type,filename,ext,file_size);
 		Entity resource = NEW(resource_entity_name,
 				creator,
 				RESOURCE_FIELD_CONTENT_TYPE,content_type,
 				RESOURCE_FIELD_SIMPLE_TYPE,simple_type,
 				RESOURCE_FIELD_FILENAME,filename,
 				RESOURCE_FIELD_EXTENSION,ext,
 				RESOURCE_FIELD_FILE_SIZE,file_size,
 				RESOURCE_FIELD_PATH_TOKEN,path_token,
 				annotations);
 
 			
 		return resource;
 	}
 	
 	protected Entity do_update_resource(MultipartForm upload,Entity resource,String content_type,String simple_type,String filename,String ext,long file_size,String path_token) throws WebApplicationException,PersistenceException
 	{
 		//DONT NEED THIS ANYMORE SINCE THE STREAM SHOULD WRITE RIGHT OVER THE FILE
 		// path_provider.delete((String)resource.getAttribute(RESOURCE_FIELD_PATH_TOKEN));
 		Object[] annotations = annotate(upload, (Entity)resource.getAttribute(FIELD_CREATOR), content_type, simple_type, filename, ext, file_size);
 		Entity r = UPDATE(resource,
 				RESOURCE_FIELD_CONTENT_TYPE,content_type,
 				RESOURCE_FIELD_SIMPLE_TYPE,simple_type,
 				RESOURCE_FIELD_FILENAME,filename,
 				RESOURCE_FIELD_EXTENSION,ext,
 				RESOURCE_FIELD_FILE_SIZE,file_size,
 				RESOURCE_FIELD_PATH_TOKEN,path_token,
 				annotations);
 			
 		return r;
 	}
 	
 	/* this gives a subclass a chance to add some extra fields to the resource...this
 	 * is sort of a special case */
 	private static final Object[] EMPTY_ANNOTATIONS = new Object[0];
 	protected Object[] annotate(MultipartForm upload,
 		    Entity creator, String content_type, String simple_type,
 		    String filename, String ext, long file_size) throws WebApplicationException
 	{
 		return EMPTY_ANNOTATIONS;
 	}
 
 
 	private static final void copy_input_stream(InputStream in, OutputStream out) throws IOException
 	{
 	    byte[] buffer = new byte[1024];
 	    int len;
 	
 	    while((len = in.read(buffer)) >= 0)
 	      out.write(buffer, 0, len);
 	
 	    in.close();
 	    out.close();
 	}
 
 	public static void check_exceptions(UserApplicationContext uctx) throws WebApplicationException
 	{
 		WebApplicationException e = (WebApplicationException)uctx.getProperty(KEY_PENDING_UPLOAD_EXCEPTION);
 		if(e != null)
 		{
 			uctx.setProperty(KEY_PENDING_UPLOAD_EXCEPTION, null);
 			throw e;
 		}
 	}
 	
 	/////////////////E N D  M O D U L E   F U N C T I O N S/////////////////////////////////////////
 	//BEGIN UTIL FUNTIONS
 	
 	
 	//END UTIL FUNCTIONS //
 	
 	//ENTITY BOOTSTRAP STUFF //
 	public static String RESOURCE_ENTITY 				= 	"Resource";
 	public static String RESOURCE_FIELD_CONTENT_TYPE 	= 	"content-type";
 	public static String RESOURCE_FIELD_SIMPLE_TYPE 	= 	"simple-type";
 	public static String RESOURCE_FIELD_FILENAME 		=   "filename";
 	public static String RESOURCE_FIELD_EXTENSION 		=   "extension";
 	public static String RESOURCE_FIELD_FILE_SIZE 		=   "filesize";
 	public static String RESOURCE_FIELD_PATH_TOKEN 		=   "path-token";
 
 
 	protected void defineEntities(Map<String,Object> config) throws PersistenceException,InitializationException
 	{
 		DEFINE_ENTITY(resource_entity_name,
 					  RESOURCE_FIELD_CONTENT_TYPE,	Types.TYPE_STRING,null, 
 					  RESOURCE_FIELD_SIMPLE_TYPE,	Types.TYPE_STRING,null, 
 					  RESOURCE_FIELD_FILENAME,		Types.TYPE_STRING,null,
 					  RESOURCE_FIELD_EXTENSION, 	Types.TYPE_STRING,null,
 					  RESOURCE_FIELD_FILE_SIZE,		Types.TYPE_LONG,null,
 					  RESOURCE_FIELD_PATH_TOKEN,	Types.TYPE_STRING,null);
 	}
 
 }
