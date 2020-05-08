 package se.arcticblue.raven.server.controller;
 
 import org.springframework.mobile.device.Device;
 import org.springframework.mobile.device.site.SitePreference;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class WorkspaceController extends AbstractController {
 
    @RequestMapping(value = "/", method = RequestMethod.GET)
     public String workspace(Device device, SitePreference sitePreference) {
         return redirect(device, sitePreference).concat("/workspace");
     }


    // expression="currentSitePreference.mobile"
 }
