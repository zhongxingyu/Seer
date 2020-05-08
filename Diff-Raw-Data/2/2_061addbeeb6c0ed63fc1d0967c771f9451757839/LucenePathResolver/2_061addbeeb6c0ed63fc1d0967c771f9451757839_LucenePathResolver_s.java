 package com.gentics.cr.plink;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.api.lib.datasource.DatasourceException;
 import com.gentics.api.lib.datasource.DatasourceNotAvailableException;
 import com.gentics.api.lib.exception.ParserException;
 import com.gentics.api.lib.expressionparser.Expression;
 import com.gentics.api.lib.expressionparser.ExpressionParserException;
 import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.api.portalnode.connector.PortalConnectorFactory;
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigFileLoader;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRDatabaseFactory;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.indexing.IndexController;
 
 /**
  * This class is used to resolve URLs to objects an vice versa.
  *
  * 
  * Last changed: $Date: 2010-01-18 15:57:21 +0100 (Mo, 18 Jän 2010) $
  * @version $Revision: 401 $
  * @author $Author: bigbear.ap $
  *
  */
 public class LucenePathResolver {
 
 	private CRConfig conf = null;
 	
 	private static Logger log = Logger.getLogger(LucenePathResolver.class);
 	
 	private static final String INDEXER_CONFIG_FILE_KEY="indexconfig";
 	private static final String SEARCH_CONFIG_FILE_KEY="searchconfig";
 		
 	private IndexController idx = null;
 	
 	private RequestProcessor rp = null;
 		
 
 	/**
 	 * Initialize the expression needed to resolve Objects from passed URLs. As
 	 * this uses a lot of time initalization in the constructor improves
 	 * performance. Initialize PathResolver once on Server startup.
 	 * @param conf 
 	 * @param appRule 
 	 * 
 	 */
 	public LucenePathResolver(CRConfig conf) {
 
 		this.conf = conf;
 		String cx_name = this.conf.getString(INDEXER_CONFIG_FILE_KEY);
 		if(cx_name==null)cx_name="indexer";		
 		CRConfigUtil idxConfig = new CRConfigFileLoader(cx_name,null);
 		
 		String cs_name = this.conf.getString(SEARCH_CONFIG_FILE_KEY);
 		if(cs_name==null)cs_name="searcher";		
 		CRConfigUtil srcConfig = new CRConfigFileLoader(cs_name,null);
 		
 		idx = new IndexController(idxConfig);
 		try {
 			rp = srcConfig.getNewRequestProcessorInstance(1);
 		} catch (CRException e) {
 			log.error("Could not initialize searcher request processor. Check your searcher config.", e);
 		}
 	}
 	
 	/**
 	 * Destroys the PathResolver
 	 */
 	public void destroy()
 	{
		this.idx.toString();
 	}
 	
 	
 
 	/**
 	 * The Method looks in the repository with the expression initialized in the
 	 * constructor and tries to find a corrisponding object. This only works
 	 * correctly when only one node is in the repository, otherwise there may
 	 * bee more object with the same URL in the repository.
 	 * @param request 
 	 * 
 	 * @param url
 	 * @return a Resolvalbe Object based on the passed URL.
 	 */
 	@SuppressWarnings("unchecked")
 	public CRResolvableBean getObject(CRRequest request) {
 		CRResolvableBean contentObject = null;
 		String url = request.getUrl();
 		if (url != null) {
 			
 				CRRequest r = new CRRequest();
 				
 				PathBean pb = new PathBean(request.getUrl());
 				String path = pb.getPath();
 				
 				String filter = "";
 				if(path==null || "".equals(path))
 					filter = "(pub_dir:(/)) AND filename:("+pb.getFilename()+")";
 				else
 					filter = "(pub_dir:("+pb.getPath()+") OR pub_dir:("+pb.getPath()+"/)) AND filename:("+pb.getFilename()+")";
 				log.debug("Using filter: "+filter);
 				r.setRequestFilter(filter);
 				try {
 					contentObject = rp.getFirstMatchingResolvable(r);
 				} catch (CRException e) {
 					log.error("Could not load object from path "+url, e);
 				}
 			
 		}
 		
 		return contentObject;
 	}
 
 	/**
 	 * initializes a Resolvable Object an calls getPath(Resolvable linkedObject)
 	 * 
 	 * @param contentid
 	 * @return
 	 * @see public String getPath(Resolvable linkedObject)
 	 */
 	public String getPath(String contentid) {
 
 		Resolvable linkedObject = null;
 		Datasource ds = null;
 		try {
 			ds = this.conf.getDatasource();
 			// initialize linked Object
 			linkedObject = PortalConnectorFactory.getContentObject(contentid,ds);
 			return getPath(linkedObject);
 
 		} catch (DatasourceNotAvailableException e) {
 			log.error("Datasource error generating url for " + contentid);
 		}
 		finally{
 			CRDatabaseFactory.releaseDatasource(ds);
 		}
 
 		// if the linked Object cannot be initialized return a dynamic URL
 //		this.log.info("Use dynamic url for " + contentid);
 //		return getDynamicUrl(contentid);
 		return(null);
 	}
 
 	/**
 	 * @param linkedObject
 	 *            must be a Resolvable
 	 * @return a the URL from Gentics Content.Node. This expects the attributes
 	 *         filename and folder_id to be set for the passed object and the
 	 *         attribute pub_dir to be set for folders.
 	 */
 	public String getPath(Resolvable linkedObject) {
 
 		String filename = "";
 		String pub_dir = "";
 
 		if (linkedObject != null) {
 
 			// get filename from linkedObject
 			filename = (String) linkedObject.get("filename");
 
 			// Get folder Object from attribute folder_id and attribute pub_dir
 			// from it
 			Resolvable folder = (Resolvable) linkedObject.get("folder_id");
 			if (folder != null) {
 				pub_dir = (String) folder.get("pub_dir");
 			}
 		}
 
 		// If filename is empty or not set, no need to return an path
 		if (filename != null && !"".equals(filename)) {
 
 			return pub_dir + filename;
 		} else {
 
 			// return a dynamic URL instead
 			log.warn("Object " + linkedObject.get("contentid")
 					+ " has no filename.");
 			//this.log.info("Use dynamic url for "
 			//		+ linkedObject.get("contentid"));
 
 			//return getDynamicUrl((String) linkedObject.get("contentid"));
 			return(null);
 		}
 	}
 
 	/**
 	 * @param contentid
 	 * @return a dynamic URL to suitable for CRServlet.
 	 */
 	public String getDynamicUrl(String contentid) {
 
 		return "?contentid=" + contentid;
 		
 	}
 	
 	/**
 	 * Get the alternate URL for the request. This is used if the object cannot be resolved dynamically.
 	 * @param contentid String with the identifier of the object
 	 * @return String with the configured servlet / portal url.
 	 */
 	public String getAlternateUrl(String contentid){
 		String url = null;
 		String obj_type = contentid.split("\\.")[0];
 		if(obj_type != null){
 			//try to get a specific URL for the objecttype
 			url = conf.getString(CRConfig.ADVPLR_HOST + "." +obj_type);
 		}
 		if(url == null){
 			//if we didn't get a specific URL take the generic one
 			url = conf.getString(CRConfig.ADVPLR_HOST);
 		}
 		return url + contentid;
 	}
 
 	/**
 	 * @param contentid
 	 * @param config 
 	 * @param request 
 	 * @return a dynamic URL to suitable for CRServlet.
 	 * Supports beautiful URLs, therefore it needs to load DB Objects and Attributes 
 	 */
 	public String getDynamicUrl(String contentid, CRConfig config, CRRequest request) {
 		String url = null;
 		if(request != null){
 			url = (String) request.get("url");
 			if(url == null)
 				return getDynamicUrl(contentid);
 			else{
 				Datasource ds = null;
 				String ret = null;
 				try
 				{
 					//if there is an attribute URL the servlet was called with a beautiful URL so give back a beautiful URL					
 					//check if valid local link
 					String applicationrule = (String)config.get("applicationrule");
 					ds = config.getDatasource();
 					Expression expression = null;
 					try {
 						expression = PortalConnectorFactory.createExpression("object.contentid == '" + contentid + "' && " + applicationrule);
 					} catch (ParserException exception) {
 						log.error("Error while building expression object for " + contentid, exception);
 						System.out.println("Error while building expression object for " + contentid);
 						ret =  getDynamicUrl(contentid);
 					}
 					
 					DatasourceFilter filter = null;
 					try {
 						filter = ds.createDatasourceFilter(expression);
 					} catch (ExpressionParserException e) {
 						log.error("Error while building filter object for " + contentid, e);
 						ret =  getDynamicUrl(contentid);
 					}
 					int count = 0;
 					try {
 						count = ds.getCount(filter);
 					} catch (DatasourceException e) {
 						log.error("Error while querying for " + contentid, e);
 						ret =  getDynamicUrl(contentid);
 					}
 					
 					if(count == 0 || "true".equals(config.getString(CRConfig.ADVPLR_HOST_FORCE))){ //not permitted or forced, build link
 						ret =  getAlternateUrl(contentid);
 					}
 					else {
 					
 						Resolvable plinkObject;
 						try {
 									
 							plinkObject = PortalConnectorFactory.getContentObject(contentid, ds);
 							//TODO: make this more beautiful and compatible with portlets
 							String filename_attribute = (String) config.get(CRConfig.ADVPLR_FN_KEY);
 							String pub_dir_attribute = (String) config.get(CRConfig.ADVPLR_PB_KEY);
 							String filename = (String) plinkObject.get(filename_attribute);
 							String pub_dir = (String) plinkObject.get(pub_dir_attribute);
 							HttpServletRequest servletRequest = (HttpServletRequest) request.get("request");
 							String contextPath = servletRequest.getContextPath();
 							String servletPath = servletRequest.getServletPath();
 							ret =  contextPath + servletPath + pub_dir + filename;
 						} catch (DatasourceNotAvailableException e) {
 							log.error("Error while getting object for "+contentid, e);
 							ret = getDynamicUrl(contentid);
 						}
 					}
 				}
 				catch(Exception e)
 				{
 					log.error("Error while processing dynamic url",e);
 				}
 				finally{
 					CRDatabaseFactory.releaseDatasource(ds);
 				}
 				return ret;
 			}
 		}
 		else{
 			return getAlternateUrl(contentid);
 		}
 
 	}
 
 }
