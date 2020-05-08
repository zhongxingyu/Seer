 package com.gentics.cr.rest;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.CRBinaryRequestBuilder;
 import com.gentics.cr.util.response.IResponseTypeSetter;
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class RESTBinaryContainer{
 
 	private RequestProcessor rp;
 	private String response_encoding;
 	private String contenttype="";
 	private static Logger log = Logger.getLogger(RESTBinaryContainer.class);
 	CRConfigUtil crConf;
 
 	private final static String LIVEEDITORXHTML_KEY="container.liveeditorXHTML";
 	/**
 	 * get conten type as string
 	 * @return
 	 */
 	public String getContentType()
 	{
 		return(this.contenttype);
 	}
 	
 	/**
 	 * Finalize the Container
 	 */
 	public void finalize()
 	{
 		if(this.rp!=null)this.rp.finalize();
 	}
 	
 	/**
 	 * Create new instance
 	 * @param crConf
 	 */
 	public RESTBinaryContainer(CRConfigUtil crConf)
 	{
 		this.response_encoding = crConf.getEncoding();
 		this.crConf = crConf;
 		try {
 			this.rp = crConf.getNewRequestProcessorInstance(1);
 		} catch (CRException e) {
 			CRException ex = new CRException(e);
 			log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+ex.getStringStackTrace());
 		}
 	}
 	
 	private void respondWithError(OutputStream stream, CRException ex, boolean debug)
 	{
 		String ret = ""+ex.getMessage();
 		if(debug)
 			ret+= " - "+ ex.getStringStackTrace();
 		try {
 			OutputStreamWriter wr = new OutputStreamWriter(stream, this.response_encoding);
 			wr.write(ret);
 			wr.flush();
 			wr.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Process the whole Service
 	 * @param reqBuilder
 	 * @param wrappedObjectsToDeploy
 	 * @param stream
 	 * @param responsetypesetter
 	 */
 	public void processService(CRBinaryRequestBuilder reqBuilder, Map<String,Resolvable> wrappedObjectsToDeploy, OutputStream stream, IResponseTypeSetter responsetypesetter)
 	{
 		CRBinaryRequestBuilder myReqBuilder = reqBuilder;
 		CRResolvableBean crBean = null;
 		CRRequest req;
 		try {
 			req = myReqBuilder.getBinaryRequest();
 			//DEPLOY OBJECTS TO REQUEST
 			for (Iterator<Map.Entry<String, Resolvable>> i = wrappedObjectsToDeploy.entrySet().iterator() ; i.hasNext() ; ) {
 				Map.Entry<String,Resolvable> entry = (Entry<String,Resolvable>) i.next();
 				req.addObjectForFilterDeployment((String)entry.getKey(), entry.getValue());
 			}
 			if(this.crConf.usesContentidUrl())
 			{
 				if(req.getContentid()==null)
 				{
 					Object obj = reqBuilder.getRequest();
 					if(obj instanceof HttpServletRequest) {
 						String[] reqURI = ((HttpServletRequest)obj).getRequestURI().split("/");
 						ArrayList<String> reqList = new ArrayList<String>(Arrays.asList(reqURI));
 						int index = reqList.indexOf(((HttpServletRequest)obj).getServletPath().replaceAll("/",""));
 						if(reqList.size()>=index+1)
 						{
 							req.setRequestFilter("object.contentid=="+reqList.get(index+1).toString());
 						}
 					}
 					
 					//contentid=request.getRequestURI().replaceFirst(request.getContextPath()+request.getServletPath()+"/","").replaceAll("/","");
 				}
 			}
			req.setAttributeArray(new String[]{"mimetype"});
 			// load by url if no contentid
 			if (req.isUrlRequest()) {
 				crBean = rp.getContentByUrl(req);
 			} else {
 				crBean = rp.getContent(req);
 			}
 			if(crBean!=null)
 			{
 				// set mimetype.
 				if(crBean.getMimetype()==null)
 				{
 					
 					CRConfigUtil rpConf = crConf.getRequestProcessorConfig(1);
 					if(crBean.getObj_type().equals(rpConf.getPageType()))
 					{
 						this.contenttype="text/html; charset="+this.response_encoding;
 						log.info("Responding with mimetype: text/html");
 					}
 					else
 					{
 						log.info("Mimetype has not been set, using standard instead. ("+crBean.getObj_type()+"!="+rpConf.getPageType()+")");
 					}
 				}
 				else
 				{
 					
 					this.contenttype=crBean.getMimetype()+"; charset="+this.response_encoding;
 					
 					log.info("Responding with mimetype: "+crBean.getMimetype());
 				}
 				
 				responsetypesetter.setContentType(this.getContentType());
 				// output data.
 				if (crBean.isBinary()) {
 					log.debug("Size of content: "+crBean.getBinaryContent().length);
 					stream.write(crBean.getBinaryContent());
 					
 				} else {
 					OutputStreamWriter wr = new OutputStreamWriter(stream, this.response_encoding);
 					String content = crBean.getContent(this.response_encoding);
 					if(Boolean.parseBoolean((String) crConf.get(LIVEEDITORXHTML_KEY))){
 						//Gentics Content.Node Liveeditor produces non XHTML brakes. Therefore we must replace them before we return the code
 						content = content.replace("<BR>", "</ br>");
 					}
 					wr.write(content);
 					wr.flush();
 					wr.close();
 				}
 			}
 			else
 			{
 				CRException crex = new CRException("NoDataFound","Data could not be found.");
 				this.respondWithError(stream,crex,myReqBuilder.isDebug());
 			}
 			stream.flush();
 			stream.close();
 		} catch (CRException e1) {
 			this.contenttype="text/html; charset="+this.response_encoding;
 			respondWithError((OutputStream)stream,e1,myReqBuilder.isDebug());
 			e1.printStackTrace();
 		}
 		catch(Exception e)
 		{
 			log.error("Error while processing service (RESTBinaryContainer)",e);
 			CRException crex = new CRException(e);
 			this.respondWithError(stream,crex,myReqBuilder.isDebug());
 		}
 		
 	}
 }
