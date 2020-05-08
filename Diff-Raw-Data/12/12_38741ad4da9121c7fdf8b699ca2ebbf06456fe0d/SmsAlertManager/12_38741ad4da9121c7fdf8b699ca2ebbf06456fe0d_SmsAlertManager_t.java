 package pe.edu.pucp.teleprocesamiento.server.sms;
 
 import javax.microedition.io.Connector;
 import javax.wireless.messaging.MessageConnection;
 import javax.wireless.messaging.TextMessage;
 import pe.edu.pucp.teleprocesamiento.server.form.ServerForm;
 import pe.edu.pucp.teleprocesamiento.server.status.RoomStatus;
 
 /**
  *
  * Sends SMS Temperature alerts.
  *
  * @author Carlos G. Gavidia
  */
 public class SmsAlertManager extends Thread {
 
     private static final String CLIENT_NUMBER = "+5550000";
    private static final String PORT = "50000";
     public static final int SMS_PERIOD = 5000;
     private static final int MAXIMUM_TEMP = 27;
     private static final String ALERT_MESSAGE =
             "La temperatura ha superado los "
             + MAXIMUM_TEMP + " grados centígrados";
     private final ServerForm serverForm;
 
     public SmsAlertManager(ServerForm serverForm) {
         this.serverForm = serverForm;
     }
 
     public void run() {
         while (true) {
             String address = "sms://" + CLIENT_NUMBER + ":" + PORT;
             try {
                 RoomStatus roomStatus = RoomStatus.getInstance();
                 final int temperature = roomStatus.getTemperature();
                 System.out.println("temperature: " + temperature);
                 serverForm.refreshForm();
                 if (temperature > MAXIMUM_TEMP) {
                     MessageConnection connection = (MessageConnection) Connector.open(address);
                     TextMessage message = (TextMessage) connection.newMessage(
                             MessageConnection.TEXT_MESSAGE);
                     message.setPayloadText(ALERT_MESSAGE);
                     connection.send(message);
                     System.out.println("message: " + message);
                     connection.close();
                 }
                 Thread.sleep(SMS_PERIOD);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
 
         }
     }
 }
