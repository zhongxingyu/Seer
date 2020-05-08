 package com.gentics.cr.rest;
 
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.CRNavigationRequestBuilder;
 import com.gentics.cr.util.ContentRepositoryConfig;
 import com.gentics.cr.util.response.IResponseTypeSetter;
 
 /**
  * TODO javadoc.
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class RESTNavigationContainer {
 
 	/**
 	 * {@link RequestProcessor} to get the objects from.
 	 */
 	private RequestProcessor rp;
 
 	/**
 	 * Encoding of the reponse.
 	 */
 	private String responseEncoding;
 	/**
 	 * TODO javadoc.
 	 */
 	private String contenttype = "";
 	/**
 	 * Log4j logger for error and debug messages.
 	 */
 	private static Logger log = Logger.getLogger(RESTNavigationContainer.class);
 	/**
 	 * TODO javadoc.
 	 */
 	private CRConfigUtil conf;
 
 	/**
 	 * Create new instance.
 	 * @param crConf TODO javadoc
 	 */
 	public RESTNavigationContainer(final CRConfigUtil crConf) {
 		responseEncoding = crConf.getEncoding();
 		conf = crConf;
 		try {
 			rp = crConf.getNewRequestProcessorInstance(1);
 		} catch (CRException e) {
 			CRException ex = new CRException(e);
 			log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... ", ex);
 		}
 	}
 
 	/**
 	 * Get the content type to set for the stream.
 	 * @return TODO javadoc
 	 */
 	public final String getContentType() {
 		return this.contenttype + "; charset=" + this.responseEncoding;
 	}
 
 	/**
 	 * Finalize the Container.
 	 */
 	public final void finalize() {
 		if (this.rp != null) {
 			this.rp.finalize();
 		}
 	}
 
 	/**
 	 * Process the whole service.
 	 * @param reqBuilder TODO javadoc
 	 * @param wrappedObjectsToDeploy TODO javadoc
 	 * @param stream TODO javadoc
 	 * @param responsetypesetter TODO javadoc
 	 */
 	public final void processService(final CRNavigationRequestBuilder reqBuilder, final Map<String, Resolvable> wrappedObjectsToDeploy,
 			final OutputStream stream, final IResponseTypeSetter responsetypesetter) {
 		Collection<CRResolvableBean> coll;
 		CRNavigationRequestBuilder myReqBuilder = reqBuilder;
 		ContentRepository cr = null;
		ContentRepositoryConfig contentRepository = myReqBuilder.getContentRepositoryConfig();
 		try {
 			cr = contentRepository.getContentRepository(responseEncoding, conf);
 			contenttype = cr.getContentType();
 			responsetypesetter.setContentType(getContentType());
 			CRRequest req = myReqBuilder.getNavigationRequest();
 			//DEPLOY OBJECTS TO REQUEST
 			for (Map.Entry<String, Resolvable> entry : wrappedObjectsToDeploy.entrySet()) {
 				req.addObjectForFilterDeployment(entry.getKey(), entry.getValue());
 			}
 			// Query the Objects from RequestProcessor
 			coll = rp.getNavigation(req);
 			// add the objects to repository as serializeable beans
 			if (coll != null) {
 				for (CRResolvableBean bean : coll) {
 					cr.addObject(bean);
 				}
 			}
 			cr.toStream(stream);
 		} catch (CRException e1) {
 			//CR Error Handling
 			//CRException is passed down from methods that want to post 
 			//the occured error to the client
 			cr.respondWithError((OutputStream) stream, e1, myReqBuilder.isDebug());
 			log.debug(e1.getMessage(), e1);
 		} catch (Exception ex) {
 			CRException crex = new CRException(ex);
 			if (cr != null) {
 				cr.respondWithError((OutputStream) stream, crex, myReqBuilder.isDebug());
 				log.debug(ex.getMessage(), ex);
 			} else {
 				log.error("Cannot initialize ContentRepository", ex);
 			}
 
 		}
 
 	}
 
 }
