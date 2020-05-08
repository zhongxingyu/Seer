 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.esper.service;
 
 import ca.uhn.hl7v2.model.Message;
 import com.abada.esper.EsperLoader;
 import com.abada.esper.configuration.model.Statement;
 import com.abada.esper.configuration.model.Statements;
 import com.abada.esper.listener.EsperListener;
 import com.abada.esper.lock.service.LockService;
 import com.abada.eva.historic.dao.HistoricDao;
 import com.abada.eva.historic.entities.HistoricEvent;
 import com.espertech.esper.client.EPAdministrator;
 import com.espertech.esper.client.EPRuntime;
 import com.espertech.esper.client.EPRuntimeIsolated;
 import com.espertech.esper.client.EPServiceProviderIsolated;
 import com.espertech.esper.client.EPStatement;
 import com.espertech.esper.client.time.CurrentTimeEvent;
 import com.thoughtworks.xstream.XStream;
 import java.io.ByteArrayInputStream;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import org.springframework.scheduling.annotation.Async;
 
 /**
  *
  * @author mmartin
  */
 public class EsperService {
 
     private static final String RECOVER_NAME = "recover";
     /**
      * loader for esper
      */
     private EsperLoader loader;
     /**
      * esper core
      */
     private EPRuntime runtime;
     /**
      * Session lock service
      */
     private LockService lockService;
     /**
      * Num max of items per recover task
      */
     private int numMax;
     /**
      * Queue for recover task
      */
     private ExecutorService es;
     /**
      * Dao to recover the historic events
      */
     private HistoricDao historicDao;
    private boolean recovering;
 
     public EsperService(URL url, EsperLoader loader, LockService lockService, HistoricDao historicDao, int nThreads, int numMaxItems) throws Exception {
         Statements statements = this.getConfiguration(url);
         this.historicDao = historicDao;
         this.es = Executors.newFixedThreadPool(nThreads);
         this.loader = loader;
         this.lockService = lockService;
         this.runtime = this.loader.getEPRuntime();
         this.numMax = numMaxItems;
         this.loadStatements(statements);
         if (!lockService.isLastLocked()) {
             lockService.addNewLock();
         } else {
            synchronized(this){
                recovering=true;
            }
             this.recover();
         }
     }
 
     /**
      * Return true if core accept messages
      *
      * @return
      */
     public boolean canSend() {
        synchronized(this){
            if (recovering) return false;
         }
        return lockService.isLocked();        
     }
 
     /**
      * Send Message HL7 to Esper core
      *
      * @param message
      */
     public void send(Message message) {
         runtime.sendEvent(message);
     }
 
     private Statements getConfiguration(URL url) {
         XStream x = new XStream();
         x.processAnnotations(Statements.class);
         Statements s = (Statements) x.fromXML(url);
 
         return s;
     }
 
     private void loadStatements(Statements statements) throws Exception {
         List<Statement> ls = statements.getStatements();
         EPStatement stmt;
         if (ls != null) {
             EPAdministrator adm = loader.getEPAdministrator();
             
             for (Statement s : ls) {                
                 stmt = adm.createEPL(s.getEPL());      
 
                 EsperListener el=new EsperListener(new ByteArrayInputStream(s.getSpringContext().getBytes()));
                 stmt.addListener(el);
                 
                 stmt.start();                
             }
         }
     }
 
     public void finalice() {
         lockService.releaseLastLock();
         loader.destroy();
     }
 
     @Async
     private void recover() throws Exception {
         //Adding task
         Long total = historicDao.getCount();
         if (total != null && total>0) {
             //Add statemests to isolated service
             EPServiceProviderIsolated isolatedService = loader.getEPServiceIsolated(RECOVER_NAME);
 
             for (String sn : loader.getEPAdministrator().getStatementNames()) {
                 isolatedService.getEPAdministrator().addStatement(loader.getEPAdministrator().getStatement(sn));
             }
 
             for (long i = 1L; i <= total; i += numMax) {
                 RecoverTask r = new RecoverTask();
                 r.setInitItem(i);
                 r.setMaxNumItem(numMax);
                 r.setHistoricDao(historicDao);
                 r.setLoader(loader);
 
                 es.submit(r);
             }
 
             es.shutdown();
             es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
 
             //Remove from isolated service            
             for (String sn : loader.getEPAdministrator().getStatementNames()) {
                 isolatedService.getEPAdministrator().removeStatement(loader.getEPAdministrator().getStatement(sn));
             }
         }
        synchronized(this){
            recovering=false;
        }
         this.lockService.releaseLastLock();
         this.lockService.addNewLock();
 
     }
 
     private class RecoverTask implements Callable {
 
         private long initItem;
         private long maxNumItem;
         private HistoricDao historicDao;
         private EsperLoader loader;
 
         public void setLoader(EsperLoader loader) {
             this.loader = loader;
         }
 
         public void setInitItem(long initItem) {
             this.initItem = initItem;
         }
 
         public void setMaxNumItem(long maxNumItem) {
             this.maxNumItem = maxNumItem;
         }
 
         public void setHistoricDao(HistoricDao historicDao) {
             this.historicDao = historicDao;
         }
 
         public Object call() throws Exception {
             EPServiceProviderIsolated isolatedService = loader.getEPServiceIsolated(RECOVER_NAME);
             List<HistoricEvent> he = historicDao.getHistoricEvents(initItem, maxNumItem);
 
             if (he != null) {
                 EPRuntimeIsolated isolatedRuntime = isolatedService.getEPRuntime();
                 for (HistoricEvent h : he) {
                     isolatedRuntime.sendEvent(new CurrentTimeEvent(h.getRun()));
                     isolatedRuntime.sendEvent(h.getTrace());
                 }
             }
             return null;
         }
     }
 }
