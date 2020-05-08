 
 package com.bradmcevoy.http;
 
 import com.bradmcevoy.http.LockInfo.LockScope;
 import com.bradmcevoy.http.LockInfo.LockType;
 import com.bradmcevoy.http.Request.Method;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 public class LockHandler extends ExistingEntityHandler {
 
     private Logger log = LoggerFactory.getLogger(LockHandler.class);
 
     public LockHandler(HttpManager manager) {
         super(manager);
     }
     
     @Override
     protected void process(HttpManager milton, Request request, Response response, Resource resource) {
         LockableResource r = (LockableResource) resource;
         LockTimeout timeout = LockTimeout.parseTimeout(request);
         String ifHeader = request.getIfHeader();
         if( ifHeader == null || ifHeader.length() == 0  ) {
             processNewLock(milton,request,response,r,timeout);
         } else {
             processRefresh(milton,request,response,r,timeout,ifHeader);
         }        
     }
     
     @Override
     public Request.Method method() {
         return Method.LOCK;
     }   
     
     @Override
     protected boolean isCompatible(Resource handler) {
         return handler instanceof LockableResource;
     }
 
     protected void processNewLock(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout) {
         LockInfo lockInfo;        
         try {
             lockInfo = LockInfo.parseLockInfo(request);            
         } catch (SAXException ex) {
             throw new RuntimeException("Exception reading request body", ex);
         } catch (IOException ex) {
             throw new RuntimeException("Exception reading request body", ex);
         }
         log.debug("locking: " + r.getName());
         LockToken tok = r.lock(timeout, lockInfo);
         log.debug("..locked: " + tok.tokenId);
        response.setLockTokenHeader("(<opaquelocktoken:" + tok.tokenId + ">)");  // spec says to set response header. See 8.10.1
        respondWithToken(tok, response);
     }
 
     protected void processRefresh(HttpManager milton, Request request, Response response, LockableResource r, LockTimeout timeout, String ifHeader) {
         String token = parseToken(ifHeader);
         log.debug("refreshing lock: " + token);
         LockToken tok = r.refreshLock(token);
        respondWithToken(tok, response);
     }
 
    protected void respondWithToken(LockToken tok, Response response) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         XmlWriter writer = new XmlWriter(out);
         writer.writeXMLHeader();        
         writer.open("D:prop  xmlns:D=\"DAV:\"");
         writer.newLine();
         writer.open("D:lockdiscovery");
         writer.newLine();
         writer.open("D:activelock");
         writer.newLine();        
         appendType(writer, tok.info.type);
         appendScope(writer, tok.info.scope);
         appendDepth(writer, tok.info.depth);
         appendOwner(writer, tok.info.owner);
         appendTimeout(writer, tok.timeout.seconds);
         appendTokenId(writer, tok.tokenId);
         writer.close("D:activelock");
         writer.close("D:lockdiscovery");
         writer.close("D:prop");
         writer.flush();
         
         log.debug("lock response: " + out.toString());
         try {
             response.getOutputStream().write(out.toByteArray());
         } catch (IOException ex) {
             log.warn("exception writing to outputstream", ex);
         }
         response.close();
 
     }
 
     static String parseToken(String ifHeader) {
         String token = ifHeader;
         int pos = token.indexOf(":");
         if( pos >= 0 ) {
             token = token.substring(pos+1);
             pos = token.indexOf(">");
             if( pos >= 0 ) {
                 token = token.substring(0, pos-1);
             }
         }
         return token;
     }
 
     private void appendDepth(XmlWriter writer, LockInfo.LockDepth depthType) {
         String s = "Infinity";
         if( depthType != null ) {
             if( depthType.equals(LockInfo.LockDepth.INFINITY)) s = depthType.name().toUpperCase();
         }
         writer.writeProperty(null, "D:depth", s);
 
     }
 
     private void appendOwner(XmlWriter writer, String owner) {
         XmlWriter.Element el = writer.begin("D:owner").open();
         XmlWriter.Element el2 = writer.begin("D:href").open();
         if( owner != null ){
             el2.writeText(owner);
         }
         el2.close();        
         el.close();                
     }
 
     private void appendScope(XmlWriter writer, LockScope scope) {
         writer.writeProperty(null, "D:locktype", "<D:" + scope.toString().toLowerCase() + "/>");   
     }
 
     private void appendTimeout(XmlWriter writer, Long seconds) {        
         if( seconds != null && seconds > 0 ) {
             writer.writeProperty(null, "D:timeout", "Second-" + seconds);
         }
     }
 
     private void appendTokenId(XmlWriter writer, String tokenId) {
         XmlWriter.Element el = writer.begin("D:locktoken").open();
         writer.writeProperty(null, "D:href", "opaquelocktoken:" + tokenId);
         el.close(); 
     }
 
     private void appendType(XmlWriter writer, LockType type) {
         writer.writeProperty(null, "D:locktype", "<D:" + type.toString().toLowerCase() + "/>");
     }
 

     
 }
