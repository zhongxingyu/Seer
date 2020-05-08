 package org.wattdepot.visualization;
 
 import java.util.Map;
 import org.apache.wicket.Session;
 import org.apache.wicket.request.ClientInfo;
 import org.apache.wicket.request.Request;
 import org.wattdepot.visualization.data.sensor.SensorModel;
 import org.wattdepot.visualization.data.server.ServerModel;
 
 /**
  * Defines the custom session for our visualization application.
  * 
  * @author Bret K. Ikehara
  */
 public class VisualizationSession extends Session {
 
   /**
    * Serial ID.
    */
   private static final long serialVersionUID = -758160945813555627L;
 
   private ServerModel serverModel;
 
   /**
    * Defines the visualization app's session.
    * 
    * @param request {@link Request}
    */
   public VisualizationSession(Request request) {
     super(request);
     serverModel = new ServerModel();
     serverModel.setLatitude(21.297541420671582);
     serverModel.setLongitude(-157.81622171401978);
     Map<String, SensorModel> map = serverModel.getSensors();
 
     SensorModel sensorModel = new SensorModel();
     sensorModel.setLatitude(21.297851);
     sensorModel.setLongitude(-157.820835);
     sensorModel.setColor(new int[] { 90, 255, 255, 255 });
     map.put("1", sensorModel);
     
     sensorModel = new SensorModel();
     sensorModel.setLatitude(21.30058);
     sensorModel.setLongitude(-157.81618);
     sensorModel.setColor(new int[] { 0, 255, 255, 255 });
     map.put("2", sensorModel);
 
     sensorModel = new SensorModel();
     sensorModel.setLatitude(21.29639);
     sensorModel.setLongitude(-157.81734);
     sensorModel.setPulse(true);
     sensorModel.setColor(new int[] { 0, 255, 255, 255 });
     map.put("3", sensorModel);
   }
 
   @Override
   public void cleanupFeedbackMessages() {
     // TODO Auto-generated method stub
   }
 
   @Override
   public ClientInfo getClientInfo() {
     // TODO Auto-generated method stub
     return null;
   }
 
   /**
    * Gets the server model.
    * 
    * @return {@link ServerModel}
    */
   public ServerModel getServerModel() {
     return serverModel;
   }
 }
