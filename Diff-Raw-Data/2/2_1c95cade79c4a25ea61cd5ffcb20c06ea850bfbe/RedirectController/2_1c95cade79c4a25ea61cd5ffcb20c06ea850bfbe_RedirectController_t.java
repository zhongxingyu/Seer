 package com.computas.sublima.app.controller;
 
 import com.computas.sublima.app.service.LanguageService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 
 
 /**
  * This will just redirect using a 303 to a DESCRIBE of the URI
  * User: kkj
  * Date: Oct 17, 2008
  * Time: 2:11:13 PM
  */
 public class RedirectController implements StatelessAppleController {
 
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     LanguageService langServ = new LanguageService();
     String language = langServ.checkLanguage(req, res);
 
     Request r = req.getCocoonRequest();
     String uri = r.getScheme() + "://" + r.getServerName();
     if (r.getServerPort() != 80) {
       uri = uri + ":" + r.getServerPort();
     }
     uri = uri + r.getRequestURI();
    String url = getProperty("sublima.sparql.directendpoint") + "?query=" +
             "DESCRIBE <" + uri + ">";
     res.getCocoonResponse().addHeader("Location", url);
     res.sendStatus(303);
   }
 
 }
