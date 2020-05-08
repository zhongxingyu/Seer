 package com.redshape.servlet.core.context.support;
 
 import com.redshape.renderer.IRenderersFactory;
 import com.redshape.servlet.core.IHttpRequest;
 import com.redshape.servlet.core.IHttpResponse;
 import com.redshape.servlet.core.SupportType;
 import com.redshape.servlet.core.context.AbstractResponseContext;
 import com.redshape.servlet.core.context.ContextId;
 import com.redshape.servlet.core.controllers.ProcessingException;
 import com.redshape.servlet.views.IView;
 import net.sf.json.JSONObject;
 import net.sf.json.JsonConfig;
 import net.sf.json.util.CycleDetectionStrategy;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 /**
  * @author nikelin
  * @date 14:01
  */
 public class AjaxContext extends AbstractResponseContext {
     private static final Logger log = Logger.getLogger( AjaxContext.class );
     private static final String MARKER_HEADER = "XMLHttpRequest";
     private static final String DISABLE_PARAM = "Disable";
 
     private Collection<String> blackList = new HashSet<String>();
     private IRenderersFactory renderersFactory;
 
     public AjaxContext() {
         this(null);
     }
 
     public AjaxContext( IRenderersFactory renderersFactory ) {
         super( ContextId.AJAX );
 
         this.renderersFactory = renderersFactory;
     }
 
     public IRenderersFactory getRenderersFactory() {
         return renderersFactory;
     }
 
     public Collection<String> getBlackList() {
         return blackList;
     }
 
     public void setBlackList(Collection<String> blackList) {
         this.blackList = blackList;
     }
 
     @Override
     public boolean doExceptionsHandling() {
         return true;
     }
 
     @Override
     public boolean doRedirectionHandling() {
         return false;
     }
 
     @Override
     public SupportType isSupported(IView request) {
         return SupportType.NO;
     }
 
     @Override
     public SupportType isSupported(IHttpRequest request) {
         String headerValue = request.getHeader("X-Requested-With");
         if (  headerValue != null && headerValue.equals(AjaxContext.MARKER_HEADER) ) {
             if ( !request.getParameter("_servletContextParam").equals(AjaxContext.DISABLE_PARAM) ) {
                 return SupportType.SHOULD;
             } else {
                 return SupportType.NO;
             }
         } else {
             return SupportType.NO;
         }
     }
 
     protected Map<String, Object> prepareResult( Map<String, Object> params ) {
         Map<String, Object> result = new HashMap<String, Object>();
         for ( String key : params.keySet() ) {
             if ( this.getBlackList().contains(key) ) {
                 continue;
             }
 
             result.put( key, params.get(key) );
         }
 
         return result;
     }
 
     @Override
     public void proceedResponse(IView view, IHttpRequest request, IHttpResponse response) throws ProcessingException {
         try {
             JsonConfig config = new JsonConfig();
             config.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
 
             String responseContent = "";
             if ( this.getRenderersFactory() == null ) {
                 JSONObject result = new JSONObject();
                 result.put("response", JSONObject.fromObject(view, config) );
                 responseContent = result.toString();
             } else {
                 responseContent = this.getRenderersFactory().
                     <IView, String>forEntity(view)
                     .render(view);
             }
 
            log.info("Response: " + responseContent);

             this.writeResponse( responseContent, response);
         } catch ( IOException e ) {
             throw new ProcessingException( e.getMessage(), e );
         }
     }
 
     private void writeResponse(String responseData, HttpServletResponse response) throws IOException {
         response.setContentType("application/json");
         response.setHeader("Cache-Control", "no-cache");
         response.getWriter().write(responseData);
         response.getWriter().flush();
         response.getWriter().close();
     }
 
 }
