 /******************************************************************************
  * ResponderMEDIA.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2004, 2008, 2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.servlets.responders;
 
 import java.io.*;
 import java.util.Properties;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletOutputStream;
 import org.openlaszlo.cache.MediaCache;
 import org.openlaszlo.cache.RequestCache;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.data.*;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.utils.LZHttpUtils;
 
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.methods.*;
 import org.apache.commons.httpclient.util.*;
 import org.apache.log4j.*;
 
 import org.apache.log4j.Logger;
 
 /* The Media responder will just request the data from the target URL,
  * and forward the data verbatim to the client, preserving the content-type MIME
  * header
  *
  */
 public final class ResponderMEDIA extends Responder
 {
     private static Logger mLogger = Logger.getLogger(ResponderMEDIA.class);
 
     private static DataSource mHTTPDataSource = new HTTPDataSource();
 
     
     protected void respondImpl(HttpServletRequest req, HttpServletResponse res) {
 
         String path = req.getServletPath();
         String url;
         try {
             url  = DataSource.getURL(req);
         } catch (java.net.MalformedURLException e) {
             respondWithErrorSWF(res, "bad url: " + e.getMessage());
             return;
         }
 
         if ( ! LPS.configuration.optionAllows(path, "proxy-security-urls", url) ) {
             String err = "Forbidden url: " +  url;
             respondWithError(res, err, HttpServletResponse.SC_FORBIDDEN);
             mLogger.error(err);
             return;
         }
 
         try { 
 
             String app = LZHttpUtils.getRealPath(mContext, req);
             HttpData mdata = (HttpData) mHTTPDataSource.getData(app, req, res, -1);
             res.setContentType(mdata.getMimeType());
             long size = mdata.size();
             if (size != -1) {
                 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="setting content length: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DataSource.class.getName(),"051018-266", new Object[] {new Long(size)})
 );
                 res.setContentLength((int)size);
             }
 
             InputStream instream = mdata.getInputStream();
             OutputStream outstream = res.getOutputStream();
             try {
                 long n = FileUtils.sendToStream(instream, outstream);
             } catch (FileUtils.StreamWritingException e) {
                 mLogger.warn(
                     /* (non-Javadoc)
                      * @i18n.test
                      * @org-mes="StreamWritingException while responding: " + p[0]
                      */
                     org.openlaszlo.i18n.LaszloMessages.getMessage(
                         DataSource.class.getName(),"051018-201", new Object[] {e.getMessage()})
                              );
             } finally {
                 if (mdata != null) {
                     mdata.release();
                 }
                 FileUtils.close(outstream);
                 FileUtils.close(instream);
             }
         } catch (Throwable e) { 
            respondWithErrorSWF(res, e.getMessage());
         } 
     }
 
 }
