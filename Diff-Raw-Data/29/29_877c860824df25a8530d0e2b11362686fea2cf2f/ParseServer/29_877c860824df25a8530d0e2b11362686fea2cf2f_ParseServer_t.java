 package com.zibea.parser.core;
 
 import com.zibea.parser.core.task.Task;
 import com.zibea.parser.core.workers.*;
 import com.zibea.parser.dao.RealtyDao;
 import com.zibea.parser.model.domain.*;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * @author: Mikhail Bragin
  */
 public class ParseServer {
 
     private static final String rootUrl = "http://www.zap.com.br/imoveis/";
 
     private RealtyDao dao;
 
     private PageSearchWorker searchPageWorker;
 
     private OfferArchiver offerArchiver;
 
     private ScheduledExecutorService scheduledPool;
 
     private MonitorThread monitor;
 
     public ParseServer() throws Exception {
         this.dao = new RealtyDao();
 
         scheduledPool = Executors.newScheduledThreadPool(2, new CustomThreadFactory("scheduled-pool-worker"));
         offerArchiver = new OfferArchiver(dao);
         OfferParseWorker offerParseWorker = new OfferParseWorker(offerArchiver);
         PageParseWorker pageParseWorker = new PageParseWorker(offerParseWorker);
 
         this.searchPageWorker = new PageSearchWorker(pageParseWorker);
 
         monitor = new MonitorThread(searchPageWorker, pageParseWorker, offerParseWorker, offerArchiver);
     }
 
     public void start() {
         scheduledPool.scheduleAtFixedRate(offerArchiver, 0, 30, TimeUnit.SECONDS);
         scheduledPool.scheduleAtFixedRate(monitor, 0, 30, TimeUnit.SECONDS);
         prepareTasks();
     }
 
     private void prepareTasks() {
         List<State> states = dao.getAllStates();
         List<Apartment> apartments = dao.getAllApartments();
         List<Transaction> transactions = dao.getAllTransactions();
 
        int tasks = 0;
         if (states != null && !states.isEmpty()) {
             for (State state : states) {
                 List<City> cities = dao.getCityByState(state);
                 for (City city : cities) {
                     //stateMap.put(state, city);
 
                     for (Apartment apartment : apartments) {
                         for (Transaction transaction : transactions) {
                             List<District> districts = dao.getDistrictsByCity(city);
 
                             for (District district : districts) {
                                tasks++;
                                 Task task = new Task(state, city, apartment, transaction, district,
                                         constructPageSearchUrl(state, city, apartment, transaction));
                                 searchPageWorker.addTask(task);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private String constructPageSearchUrl(State state, City city, Apartment apartment, Transaction transaction) {
         return rootUrl
                 + state.getUrlParam()
                 + "+" + city.getUrlParam()
                 + "/" + apartment.getUrlParam()
                 + "/" + transaction.getUrlParam() +
                 "/";
     }
 
     public Queue<Proxy> prepareProxies() throws IOException {
         Queue<Proxy> proxies = new ConcurrentLinkedQueue();
 
         BufferedReader br = new BufferedReader(new FileReader("C:\\Proxies\\reliable_list.txt"));
 
         try {
             String line = br.readLine();
             String[] hostAndPort = line.split(":");
             Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
             proxies.offer(proxy);
 
             while (line != null) {
                 line = br.readLine();
                 if (line != null) {
                     hostAndPort = line.split(":");
                     proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
                     proxies.offer(proxy);
                 }
             }
         } finally {
             br.close();
         }
         return proxies;
     }
 
 }
