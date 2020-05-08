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
 import com.gentics.cr.CRException;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRRequestProcessor;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.util.CRBinaryRequestBuilder;
 import com.gentics.cr.util.response.IResponseTypeSetter;
 
 public class RESTBinaryContainer{
 
 	public CRRequestProcessor rp;
 	public String response_encoding;
 	private String contenttype="";
 	private Logger log;
 	CRConfigUtil crConf;
 	
 	public String getContentType()
 	{
 		return(this.contenttype);
 	}
 	
 	public RESTBinaryContainer(CRConfigUtil crConf)
 	{
 		this.log = Logger.getLogger(this.getClass().getName());
 		this.response_encoding = crConf.getEncoding();
 		this.crConf = crConf;
 		try {
 			this.rp = new CRRequestProcessor(crConf.getRequestProcessorConfig("1"));
 		} catch (CRException e) {
 			CRException ex = new CRException(e);
 			this.log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+ex.getStringStackTrace());
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
 					if(obj instanceof HttpServletRequest)
 					{
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
 					if(crBean.getObj_type().equals(crConf.getPageType()))
 					{
 						this.contenttype="text/html; charset="+this.response_encoding;
 						this.log.info("Responding with mimetype: text/html");
 					}
 					else
 					{
 						this.log.info("Mimetype has not been set, using standard instead.");
 					}
 				}
 				else
 				{
 					
 					this.contenttype=crBean.getMimetype()+"; charset="+this.response_encoding;
 					
 					this.log.info("Responding with mimetype: "+crBean.getMimetype());
 				}
 				
 				responsetypesetter.setContentType(this.getContentType());
 				// output data.
 				if (crBean.isBinary()) {
 					this.log.debug("Size of content: "+crBean.getBinaryContent().length);
 					stream.write(crBean.getBinaryContent());
 					
 				} else {
 					OutputStreamWriter wr = new OutputStreamWriter(stream, this.response_encoding);
 					wr.write(crBean.getContent(this.response_encoding));
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
			e.printStackTrace();
 			CRException crex = new CRException(e);
 			this.respondWithError(stream,crex,myReqBuilder.isDebug());
 		}
 		
 	}
 }
