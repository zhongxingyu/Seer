 package com.freakz.hokan_ng.common.engine;
 
 
 import com.freakz.hokan_ng.common.entity.IrcServerConfig;
 import com.freakz.hokan_ng.common.exception.HokanException;
 
 /**
  * User: petria
  * Date: 11/4/13
  * Time: 7:17 PM
  *
  * @author Petri Airio (petri.j.airio@gmail.com)
  */
 public interface EngineConnector {
 
   void engineConnectorGotOnline(Connector connector, HokanCore engine) throws HokanException;
 
   void engineConnectorTooManyConnectAttempts(Connector connector, IrcServerConfig configuredServer);
 
   void engineConnectorDisconnected(HokanCore engine);
 
  void engineConnectorPingTimeout(HokanCore hokanCore);
 }
