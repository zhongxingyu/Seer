 /******************************************************************************
  * ResponderEVAL.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2004, 2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.servlets.responders;
 
 import java.io.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletOutputStream;
 import org.openlaszlo.compiler.Compiler;
 import org.openlaszlo.compiler.CompilationEnvironment;
 import org.openlaszlo.media.MimeType;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.utils.FileUtils;
 import org.apache.log4j.Logger;
 
 public final class ResponderEVAL extends Responder
 {
     private static Logger mLogger = Logger.getLogger(ResponderEVAL.class);
 
     protected void respondImpl(HttpServletRequest req, HttpServletResponse res)
         throws IOException
     {
         ServletOutputStream out = res.getOutputStream();
 
         String script = req.getParameter("lz_script");
         boolean logmsg = false;
 
         String seqnum = req.getParameter("lzrdbseq");
 
         String lz_log = req.getParameter("lz_log");
 
         if ((lz_log != null) && lz_log.equals("true")) {
             logmsg = true;
         }
 
         if (logmsg) {
             // Just write to the log and let the output connection close
             mLogger.info(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="CLIENT_LOG " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResponderEVAL.class.getName(),"051018-50", new Object[] {script})
 );
             byte[] action = new byte[0];
             int swfversion = 6;
             ScriptCompiler.writeScriptToStream(action, out, swfversion);          
             out.flush();
             FileUtils.close(out);
         } else {
             mLogger.info(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="doEval for " + p[0] + ", seqnum=" + p[1]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResponderEVAL.class.getName(),"051018-64", new Object[] {script, seqnum})
 );
             try {
                 res.setContentType(MimeType.SWF);
                 Compiler compiler = new Compiler();
                 String swfversion = req.getParameter("lzr");
                 if ("swf9".equals(swfversion)) {
                     compiler.compileAndWriteToSWF9(script, seqnum, out);
                 } else {
                     compiler.compileAndWriteToSWF(script, seqnum, out, swfversion);
                 }
             } catch (Exception e) {
                 mLogger.info(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="LZServlet got error compiling/writing SWF!" + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 ResponderEVAL.class.getName(),"051018-83", new Object[] {e})
 );
                 StringWriter err = new StringWriter();
                 e.printStackTrace(new PrintWriter(err));
                 mLogger.info(err.toString());
             }
         }
     }
 
     public int getMimeType()
     {
         return MIME_TYPE_SWF;
     }
 }
