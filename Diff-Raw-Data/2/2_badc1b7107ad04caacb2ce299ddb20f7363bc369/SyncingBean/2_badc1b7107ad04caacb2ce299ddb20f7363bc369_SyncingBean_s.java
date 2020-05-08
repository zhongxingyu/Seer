 package com.laurinka.skga.server.controller;
 
 import com.laurinka.skga.server.job.CgfNumbersJob;
 import com.laurinka.skga.server.job.NamesJob;
 import com.laurinka.skga.server.job.SkgaNumbersJob;
 import com.laurinka.skga.server.model.CgfNumber;
 import com.laurinka.skga.server.services.WebsiteService;
 import com.laurinka.skga.server.utils.Utils;
 
 import javax.ejb.Stateful;
 import javax.enterprise.inject.Model;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 @Stateful
 @Model
 public class SyncingBean {
 
     @Inject
     SkgaNumbersJob skgaNumbersJob;
     @Inject
     CgfNumbersJob cgfNumbersJob;
     @Inject
     NamesJob namesJob;
     @Inject
     EntityManager em;
     @Inject
     private Logger log;
     @Inject
     private WebsiteService service;
 
     public void syncCgfNumbers() throws IOException {
         log.info("updating cgf numbers...start");
         cgfNumbersJob.updateNumbers();
         log.info("updating cgf numbers...end");
     }
 
     public void syncNumbers() throws IOException {
         log.info("updating numbers...start");
         skgaNumbersJob.updateNumbers();
         log.info("updating numbers...end");
     }
 
     public void syncNames() throws IOException {
         log.info("updating names...start");
         namesJob.updateNames();
         log.info("updating names...end");
     }
 
     public void save() throws IOException {
         InputStream resourceAsStream = SyncingBean.class.getResourceAsStream("/cgf.csv");
         BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
 
         String line;
         int i = 0;
         while ((line = br.readLine()) != null) {
             i++;
             process(line);
             if (i % 10 == 0) {
                 em.flush();
                 log.info("processing " + i);
             }
         }
     }
 
     private void process(String s) {
         StringTokenizer t = new StringTokenizer(s, ",");
         String name = t.nextToken();
         name = name.replaceAll("\"", "");
 
         String nr = t.nextToken();
         nr = nr.replaceAll("\"", "");
        Query namedQuery = em.createNamedQuery("select m from CgfNumber where nr=:nr");
         namedQuery.setParameter("nr", nr);
         if (!namedQuery.getResultList().isEmpty()) {
             return;
         }
 
         CgfNumber cgfNumber = new CgfNumber();
         cgfNumber.setName(name);
         cgfNumber.setNr(nr);
         cgfNumber.setName2(Utils.stripAccents(name));
         em.persist(cgfNumber);
     }
 
 }
