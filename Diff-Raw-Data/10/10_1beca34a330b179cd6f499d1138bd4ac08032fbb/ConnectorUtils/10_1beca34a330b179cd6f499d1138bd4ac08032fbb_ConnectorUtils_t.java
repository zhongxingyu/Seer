 package net.es.oscars.pss.util;
 
 import net.es.oscars.pss.api.TemplateDeviceConfigGenerator;
 import net.es.oscars.pss.beans.config.TemplateConfig;
 import org.apache.log4j.Logger;
 
 import net.es.oscars.pss.api.Connector;
 import net.es.oscars.pss.api.DeviceAddressResolver;
 import net.es.oscars.pss.api.DeviceConfigGenerator;
 import net.es.oscars.pss.beans.PSSAction;
 import net.es.oscars.pss.beans.PSSCommand;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.beans.definitions.DeviceModelDefinition;
 import net.es.oscars.pss.beans.definitions.DeviceModelService;
 import net.es.oscars.pss.config.ConfigHolder;
 
 public class ConnectorUtils {
     
     private static Logger log = Logger.getLogger(ConnectorUtils.class);
     public static DeviceConfigGenerator getDeviceConfigGenerator(String deviceId, String serviceId) throws PSSException {
         String modelId = ClassFactory.getInstance().getDeviceModelMap().getDeviceModel(deviceId);
         if (modelId == null) {
            throw new PSSException("no model defined for device "+deviceId);
 
         }
         DeviceModelDefinition dmd = ConfigHolder.getInstance().getDeviceModelDefinition(modelId);
         if (dmd == null) { 
             throw new PSSException("no device model definition for model "+modelId);
         }
         DeviceModelService dms = dmd.getService(serviceId);
         if (dms == null) { 
             throw new PSSException("no device model service for model "+modelId+" and service "+serviceId);
         }
         String configGenCN = dms.getConfigGenerator();
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         try {
             Class<?> aClass = cl.loadClass(configGenCN);
             DeviceConfigGenerator cg;
             TemplateConfig tcfg = dms.getTemplateConfig();
             if (tcfg != null) {
                if (TemplateDeviceConfigGenerator.class.isInstance(aClass.newInstance())) {
                    System.err.println("found template config generator: "+configGenCN);
                     TemplateDeviceConfigGenerator tcg = (TemplateDeviceConfigGenerator) aClass.newInstance();
                     tcg.setTemplateConfig(tcfg);
                     cg = tcg;
 
                 } else {
                    System.err.println("found template config but it does not apply: "+configGenCN);
                     cg = (DeviceConfigGenerator) aClass.newInstance();
                 }
             } else {
                System.err.println("no template config found for: "+configGenCN);
                 cg = (DeviceConfigGenerator) aClass.newInstance();
 
             }
 
             return cg;
         } catch (Exception e) {
             throw new PSSException(e);
         }
         
     }
     
     public static String getDeviceAddress(String deviceId) throws PSSException {
         log.debug("finding address for "+deviceId);
         DeviceAddressResolver res = ClassFactory.getInstance().getDeviceResolver();
         String deviceAddress = res.getDeviceAddress(deviceId);
         log.debug("address for "+deviceId+" was:"+deviceAddress);
         return deviceAddress;
     }
     public static String sendAction(PSSAction action, String deviceId, String serviceId) throws PSSException {
         DeviceConfigGenerator cg = ConnectorUtils.getDeviceConfigGenerator(deviceId, serviceId);
         
         String deviceCommand = cg.getConfig(action, deviceId);
         String deviceAddress = ConnectorUtils.getDeviceAddress(deviceId);
         
         Connector conn = ClassFactory.getInstance().getDeviceConnectorMap().getDeviceConnector(deviceId);
         PSSCommand comm = new PSSCommand();
         comm.setDeviceCommand(deviceCommand);
         comm.setDeviceAddress(deviceAddress);
         conn.sendCommand(comm);
         String resultString = conn.sendCommand(comm);
         return resultString;
     }
 }
