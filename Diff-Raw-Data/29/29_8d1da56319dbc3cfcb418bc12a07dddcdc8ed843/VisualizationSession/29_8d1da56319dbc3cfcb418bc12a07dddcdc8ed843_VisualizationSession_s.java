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
     serverModel.setX(330);
     serverModel.setY(255);
     Map<String, SensorModel> map = serverModel.getSensors();
     
     SensorModel sensorModel = new SensorModel();
     sensorModel.setRadius(10);
     sensorModel.setX(95);
     sensorModel.setY(55);
     sensorModel.setColor(new int[]{ 90, 255,255, 255});
     map.put("1", sensorModel);
 
     sensorModel = new SensorModel();
     sensorModel.setRadius(10);
     sensorModel.setX(415);
     sensorModel.setY(55);
     sensorModel.setColor(new int[]{ 0, 255,255, 255});
     map.put("2", sensorModel);
 
     sensorModel = new SensorModel();
     sensorModel.setRadius(10);
     sensorModel.setX(375);
     sensorModel.setY(335);
     sensorModel.setColor(new int[]{ 0, 255,255, 255});
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
