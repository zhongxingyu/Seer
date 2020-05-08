 package com.crashnote.play2.reporter;
 
 import com.crashnote.play2.collect.Play2RequestCollector;
 import com.crashnote.play2.collect.Play2SessionCollector;
 import com.crashnote.play2.config.Play2Config;
 import com.crashnote.web.collect.RequestCollector;
 import com.crashnote.web.collect.SessionCollector;
 import com.crashnote.web.report.WebReporter;
 
 /**
 * Customized implementation of the core {@link WebReporter}. Adds servlet-specific functionality.
  */
 public class Play2Reporter
     extends WebReporter<Play2Config, ReqHeader> {
 
 
 
     // SETUP ======================================================================================
 
     public Play2Reporter(final Play2Config config) {
         super(config);
     }
 
 
     // SHARED =====================================================================================
 
     @Override
     protected boolean ignoreRequest(ReqHeader req) {
         if (ignoreLocalhost && isLocalRequest(req.host())) {
             //getLogger.debug("error for '{} {}' is ignored (local requests are disabled in config)", req.method(), req.uri());
             return false;
         } else
             return true;
     }
 
     @Override
     protected RequestCollector<ReqHeader> getRequestCollector(Play2Config config) {
         return new Play2RequestCollector(config);
     }
 
     @Override
     protected SessionCollector<ReqHeader> getSessionCollector(Play2Config config) {
         return new Play2SessionCollector(config);
     }
 }
 
