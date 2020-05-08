 package org.sunspotworld;
 
 import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
 import com.sun.spot.peripheral.radio.RadioFactory;
 import java.io.IOException;
 import javax.microedition.io.Connector;
 import javax.microedition.io.Datagram;
 
 public class Connection {
     private static final int HOST_PORT = 67;
     public static final int SEND = 0;
     public static final int RECEIVE = 1;
 
 
     private RadiogramConnection conn = null;
     private Datagram dg;
     private long mac;
 
     public Connection (int val) {
         try {
             mac = RadioFactory.getRadioPolicyManager().getIEEEAddress();
             if (val == SEND)
                 conn = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
             else conn = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
         } catch (Exception e) {
             System.err.println("Caught " + e + " in connection initialization.");
             System.exit(1);
         }
     }
 
     public void send (int value) {
         try {
             dg = conn.newDatagram(conn.getMaximumLength());
             // iniciar datagrama
             dg.writeLong(mac);                         // escrever mac
             dg.writeLong(System.currentTimeMillis());  // escrever o tempo actual
             dg.writeInt(0);                         // escrever o tipo
             dg.writeInt(value);                        // escrever o valor
             conn.send(dg);                             // enviar o pacote
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public void send (double value) {
         try {
             dg = conn.newDatagram(conn.getMaximumLength());
             // iniciar datagrama
             dg.writeLong(mac);                         // escrever mac
             dg.writeLong(System.currentTimeMillis());  // escrever o tempo actual
             dg.writeInt(1);                         // escrever o tipo
             dg.writeDouble(value);                     // escrever o valor
             conn.send(dg);                             // enviar o pacote
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public void send (double value1, double value2) {
         try {
             dg = conn.newDatagram(conn.getMaximumLength());
             // iniciar datagrama
             dg.writeLong(mac);                         // escrever mac
             dg.writeLong(System.currentTimeMillis());  // escrever o tempo actual
             dg.writeInt(2);                         // escrever o tipo
             dg.writeDouble(value1);                    // escrever o valor
             dg.writeDouble(value2);                    // escrever o valor
             conn.send(dg);                             // enviar o pacote
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public void hostSend (int type, int newValue) {
         try {
             dg = conn.newDatagram(conn.getMaximumLength());
             dg.writeInt(type);
             dg.writeInt(newValue);
             conn.send(dg);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
    public void hostReceive () {
         try {
             dg = conn.newDatagram(conn.getMaximumLength());
             conn.receive(dg);
             System.out.println(dg.getAddress());
             System.out.print("From: "+dg.getAddress());
             System.out.print(", timestamp "+dg.readLong());
 
             switch(dg.readInt()){
                 case 0:
                     System.out.println(" luminosity is "+dg.readInt());
                     break;
                 case 1:
                     System.out.println(" temperature is "+dg.readDouble());
                     break;
                 case 2:
                     System.out.println(" X acceleration is "+dg.readDouble());
                     break;
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
    public void spotReceive (LuminosityReader l, TemperatureReader t, MovementReader m) {
         try {
             dg = conn.newDatagram(conn.getMaximumLength());
             conn.receive(dg);
             conn.setTimeout(1000);
             switch (dg.readInt()) {
                 case 0:
                     l.setTaskPeriod(dg.readInt());
                     break;
                 case 1:
                     t.setTaskPeriod(dg.readInt());
                     break;
                 case 2:
                     m.setTaskPeriod(dg.readInt());
                     break;
             }
         }catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 }
