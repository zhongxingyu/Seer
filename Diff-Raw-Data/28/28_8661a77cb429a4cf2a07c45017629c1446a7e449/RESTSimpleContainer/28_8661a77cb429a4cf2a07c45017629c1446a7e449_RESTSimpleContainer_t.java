 package com.gentics.cr.rest;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.CRRequestBuilder;
 import com.gentics.cr.util.response.IResponseTypeSetter;
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class RESTSimpleContainer{
 
 	private RequestProcessor rp;
 	private String response_encoding;
 	private String contenttype="";
 	private static Logger log = Logger.getLogger(RESTSimpleContainer.class);
 	private CRConfigUtil config;
 	
 	/**
 	 * Get the content type as String
 	 * @return
 	 */
 	public String getContentType()
 	{
 		return(this.contenttype+"; charset="+this.response_encoding);
 	}
 	
 	/**
 	 * Create new instance
 	 * @param crConf
 	 */
 	public RESTSimpleContainer(CRConfigUtil crConf)
 	{
 		this.response_encoding = crConf.getEncoding();
 		this.config = crConf;
 		try {
 			
 			this.rp = crConf.getNewRequestProcessorInstance(1);
 		} catch (CRException e) {
 			
 			log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+e.getStringStackTrace());
 		}
 	}
 	
 	/**
 	 * Process the whole service
 	 * @param reqBuilder
 	 * @param wrappedObjectsToDeploy
 	 * @param stream
 	 * @param responsetypesetter
 	 */
 	public void processService(CRRequestBuilder reqBuilder, Map<String,Resolvable> wrappedObjectsToDeploy, OutputStream stream, IResponseTypeSetter responsetypesetter)
 	{
 		Collection<CRResolvableBean> coll;
 		CRRequestBuilder myReqBuilder = reqBuilder;
 		ContentRepository cr = null;
 		try {
 			cr = myReqBuilder.getContentRepository(this.response_encoding,this.config);
 			this.contenttype = cr.getContentType();
 			if (responsetypesetter != null) {
 				responsetypesetter.setContentType(this.getContentType());
 			}
 			CRRequest req = myReqBuilder.getCRRequest();
 			boolean deploy_metaresolvable = Boolean.parseBoolean((String) config.get(ContentRepository.DEPLOYMETARESOLVABLE_KEY));
 			if(deploy_metaresolvable){
 				req.set(RequestProcessor.META_RESOLVABLE_KEY, true);
 			}
 			//DEPLOY OBJECTS TO REQUEST
 			for (Iterator<Map.Entry<String, Resolvable>> i = wrappedObjectsToDeploy.entrySet().iterator() ; i.hasNext() ; ) {
 				Map.Entry<String,Resolvable> entry = (Entry<String,Resolvable>) i.next();
 				req.addObjectForFilterDeployment((String)entry.getKey(), entry.getValue());
 			}
 			// Query the Objects from RequestProcessor
 			coll = rp.getObjects(req);
 			
 			// add the objects to repository as serializeable beans
 			if (coll != null) {
 				for (Iterator<CRResolvableBean> it = coll.iterator(); it.hasNext();) {
 					cr.addObject(it.next());
 				}
 			}
 			cr.toStream(stream);
 			
 		} catch (CRException ex) {
 			//CR Error Handling
 			//CRException is passed down from methods that want to post 
 			//the occured error to the client
 			cr.respondWithError((OutputStream) stream,ex,myReqBuilder.isDebug());
			log.error(ex.getMessage(),ex);
 			
 		}
 		catch(Exception ex)
 		{
 			CRException crex = new CRException(ex);
			ex.printStackTrace();
			System.out.println(""+myReqBuilder+stream+crex);
 			cr.respondWithError((OutputStream) stream,crex,myReqBuilder.isDebug());
			log.error(ex.getMessage(),crex);
 			
 		} finally {
 			try {
 				stream.flush();
 				stream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
