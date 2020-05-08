 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.bnl.racf.ps.dashboard.db.object_manipulators;
 
 
 import gov.bnl.racf.ps.dashboard.db.data_objects.PsHost;
 import gov.bnl.racf.ps.dashboard.db.data_objects.PsService;
 import gov.bnl.racf.ps.dashboard.db.data_objects.PsServiceType;
 import gov.racf.bnl.ps.dashboard.PsApi.PsApi;
 import java.util.Date;
 import java.util.TreeMap;
 import org.hibernate.Session;
 
 
 /**
  * creates PsService objects
  *
  * @author tomw
  */
 public class PsServiceFactory {
 
     private static int DEFAULT_CHECKINTERVAL = 1200;
     private static int DEFAULT_TIMEOUT = 60;
     /**
      * create service of type defined by typeId running on host 
      * @param typeId
      * @param host
      * @return 
      */
   
 
     /**
      * create primitive service of given type running on given host
      *
      * @param type
      * @param host
      */
     public static PsService createService(Session session,PsServiceType type, PsHost host) {
       
         PsService service = null;
         if (PsApi.BWCTL_PORT_4823.equals(type.getServiceTypeId())) {
             service = create_BWCTL_PORT_4823(session,type, host);
         }
         if (PsApi.BWCTL_PORT_8570.equals(type.getServiceTypeId())) {
             service = create_BWCTL_PORT_8570(session,type, host);
         }
         if (PsApi.CHECK_LOOKUP_SERVICE.equals(type.getServiceTypeId())) {
             service = create_CHECK_LOOKUP_SERVICE(session,type, host);
         }
         if (PsApi.NDT_PORT_3001.equals(type.getServiceTypeId())) {
             service = create_NDT_PORT_3001(session,type, host);
         }
         if (PsApi.NDT_PORT_7123.equals(type.getServiceTypeId())) {
             service = create_NDT_PORT_7123(session,type, host);
         }
         if (PsApi.NPAD_PORT_8000.equals(type.getServiceTypeId())) {
             service = create_NPAD_PORT_8000(session,type, host);
         }
         if (PsApi.NPAD_PORT_8001.equals(type.getServiceTypeId())) {
             service = create_NPAD_PORT_8001(session,type, host);
         }
         if (PsApi.OWP_861.equals(type.getServiceTypeId())) {
             service = create_OWP_861(session,type, host);
         }
         if (PsApi.OWP_8569.equals(type.getServiceTypeId())) {
             service = create_OWP_8569(session,type, host);
         }
         if (PsApi.PERFSONAR_PSB.equals(type.getServiceTypeId())) {
             service = create_PERFSONAR_PSB(session,type, host);
         }
         
         host.addService(service);
         
         return service;
     }
 
     /**
      * create a two host service of given type running between source host and
      * destination host
      *
      * @param type
      * @param source
      * @param destination
      */
     public static PsService createService(Session session, PsServiceType type,
             PsHost source, PsHost destination) {
       
         PsService service = null;
         if (PsApi.TRACEROUTE.equals(type.getId())) {
             service=create_TRACEROUTE(session,type,source,destination);
                     
         }
         return service;
     }
 
     /**
      * create a three host service of given type between source and destination
      * hosts, monitored from monitor host
      *
      * @param type
      * @param source
      * @param destination
      * @param monitor
      * @return
      */
     public static PsService createService(Session session,
             PsServiceType type,
             PsHost source, PsHost destination, PsHost monitor) {
         PsService service = null;
         if (PsApi.LATENCY.equals(type.getServiceTypeId())) {
             service = create_LATENCY(session,type,source,destination,monitor);
         }
         if (PsApi.THROUGHPUT.equals(type.getServiceTypeId())) {
             service = create_THROUGHPUT(session,type,source,destination,monitor);
         }
         return service;
     }
 
     private static PsService create_BWCTL_PORT_4823(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(4823));
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_BWCTL_PORT_8570(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(4823));
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_CHECK_LOOKUP_SERVICE(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         int port = 9995;
         parameters.put("port", new Integer(port));
         String url = "http://" + host.getHostname() + ":" + port + "/perfSONAR_PS/services/hLS";
         parameters.put("url", url);
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_NDT_PORT_3001(Session session, 
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(3001));
         
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_NDT_PORT_7123(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(7123));
         
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_NPAD_PORT_8000(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(8000));
         
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_NPAD_PORT_8001(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(8001));
         
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_OWP_861(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(861));
         
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
     
     private static PsService create_OWP_8569(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         parameters.put("port", new Integer(8569));
         
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
 
     private static PsService create_PERFSONAR_PSB(Session session,
             PsServiceType type, PsHost host) {
         PsService service = PsObjectCreator.createNewService(session);
         service.setType(type.getServiceTypeId());
         service.setName(type.getServiceTypeId() + "_on_" + host.getHostname());
         service.setDescription(type.getName());
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
 
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         parameters.put("host-id", host.getId());
         parameters.put("host", host.getHostname());
         String url = "http://" + host.getIpv4() + ":8085/perfSONAR_PS/services/pSB";
         parameters.put("url", url);
         parameters.put("template", "2");
         
         service.setParameters(parameters);
 
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
     
     private static PsService create_LATENCY(Session session,
             PsServiceType type,
             PsHost source, PsHost destination, PsHost monitor) {
         PsService service = PsObjectCreator.createNewService(session);
         
         service.setType(type.getServiceTypeId());
         String name = "packet loss_between_" + source.getHostname()+
                 "_and_"+destination.getHostname()+
                 "mon_"+monitor.getHostname();
         service.setName(name);
         service.setDescription(name);
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
         
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         
         parameters.put("source-host-id",source.getId());
         parameters.put("destination-host-id",destination.getId());
         parameters.put("ma-host-id",monitor.getId());
         
         parameters.put("source",source.getHostname());
         parameters.put("destination",destination.getHostname());
         parameters.put("monitor",monitor.getHostname());
         String port = "8085";
         String url =  "http://"+monitor.getHostname()+":"+port+"/perfSONAR_PS/services/pSB";
         parameters.put("url",url);
         
         parameters.put("timeRange","1800");
         parameters.put("loss",new Integer(1));
         parameters.put("warningThreshold",":2");
         parameters.put("criticalThreshold",":10");
         
         service.setParameters(parameters);
         
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
         
     }
     
     private static PsService create_THROUGHPUT(Session session,PsServiceType type,
             PsHost source, PsHost destination, PsHost monitor){
         
         PsService service = PsObjectCreator.createNewService(session);
         
         service.setType(type.getServiceTypeId());
         String name = "throughput_between_" + source.getHostname()+
                 "_and_"+destination.getHostname()+
                 "mon_"+monitor.getHostname();
         service.setName(name);
         service.setDescription(name);
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
         
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         
         parameters.put("source-host-id",source.getId());
         parameters.put("destination-host-id",destination.getId());
         parameters.put("ma-host-id",monitor.getId());
         
         parameters.put("source",source.getHostname());
         parameters.put("destination",destination.getHostname());
         parameters.put("monitor",monitor.getHostname());
         String port = "8085";
         String url =  "http://"+monitor.getHostname()+":"+port+"/perfSONAR_PS/services/pSB";
         parameters.put("url",url);
         
         parameters.put("timeRange","86400");
        parameters.put("warningThreshold",".1");
        parameters.put("criticalThreshold",".01");
         
         service.setParameters(parameters);
         
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
     
     
     private static PsService create_TRACEROUTE(Session session, 
             PsServiceType type,
             PsHost source, PsHost destination){
         PsService service = PsObjectCreator.createNewService(session);
         
         service.setType(type.getServiceTypeId());
         String name = "traceroute_" + source.getHostname()+
                 "_to_"+destination.getHostname();            
         service.setName(name);
         service.setDescription(name);
         service.setCheckInterval(DEFAULT_CHECKINTERVAL);
         
         TreeMap<String, Object> parameters = new TreeMap<String, Object>();
         
         parameters.put("source-host-id",source.getId());
         parameters.put("destination-host-id",destination.getId());
        
         parameters.put("source",source.getHostname());
         parameters.put("destination",destination.getHostname());
         
         String port = "8086";
         String url =  "http://"+source.getHostname()+":"+port+"/perfSONAR_PS/services/tracerouteMA";
         parameters.put("url",url);
         
         parameters.put("timeRange","3600");
         parameters.put("warningThreshold","1:");
         parameters.put("criticalThreshold","1:");
         
         service.setParameters(parameters);
         
         service.setPrevCheckTime(new Date(1));
         service.setNextCheckTime(new Date());
 
         service.setRunning(false);
         service.setRunningSince(null);
         service.setTimeout(DEFAULT_TIMEOUT);
 
         service.setResult(null);
 
         return service;
     }
     
 }
