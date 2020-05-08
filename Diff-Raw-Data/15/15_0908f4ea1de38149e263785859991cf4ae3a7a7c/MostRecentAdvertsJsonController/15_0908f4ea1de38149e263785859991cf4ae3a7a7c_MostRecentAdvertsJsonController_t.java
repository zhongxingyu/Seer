 package com.gumvision.web.controller;
 
 import com.google.gson.Gson;
 import com.gumtree.api.entity.Advert;
 import com.gumtree.api.service.advertdistributer.impl.AdvertDistributerImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.InputStreamResource;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: markkelly
  * Date: 12/09/2011
  * Time: 17:12
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class MostRecentAdvertsJsonController {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GumVisionController.class);
 
     @Autowired
     private AdvertDistributerImpl advertDistributer;
 
     @Value("${gumtree.recentAdverts.stub:false}")
     private boolean useStubData;
 
     @Autowired
     @Qualifier("stubFile")
     private InputStreamResource stubFile;
 
     private String datFromFile;
 
     private static final Gson gson = new Gson();
 
     @PostConstruct
     public void startup() {
         advertDistributer.start();
     }
 
     @RequestMapping(value="/recentAdverts", method = RequestMethod.GET)
     public void getMostRecentAdvertsToDisplayJson(HttpServletRequest request, HttpServletResponse response) {
 
        LOGGER.info("Most Recent Adverts requested");

         List<Advert> ads;
 
         try {
 
             response.setContentType("application/json;charset=UTF-8");
 
             if(useStubData) {
 
                  LOGGER.info("Stub Data!");
 
                  if(datFromFile == null)
                     datFromFile = readFromFile(stubFile.getInputStream());
 
                 response.getWriter().write(datFromFile);
 
             } else {
 
                 ads = advertDistributer.getCurrentAdverts();
 
                 //Note the client javascript pops from the wrong end of the Queue
                 // We want the reverse (So use a cheat of reversing the List before sending it)
                 Collections.reverse(ads);
 
                 //add the json response
                 response.getWriter().write(gson.toJson(ads));
             }
 
             response.getWriter().close();
 
         } catch (IOException e) {
             LOGGER.error(e.getMessage());
         }
     }
 
 
     private static String readFromFile(InputStream is) throws IOException
     {
         if (is != null) {
             StringBuilder sb = new StringBuilder();
             String line;
 
             try {
                 BufferedReader r1 = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                 while ((line = r1.readLine()) != null) {
                     sb.append(line).append("\n");
                 }
             } finally {
                 is.close();
             }
             return sb.toString();
         } else {
             return "";
         }
 
     }
 }
