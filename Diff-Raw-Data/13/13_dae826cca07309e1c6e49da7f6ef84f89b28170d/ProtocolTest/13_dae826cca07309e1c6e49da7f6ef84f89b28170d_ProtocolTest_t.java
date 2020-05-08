 package ru.kcode.kcontrol.sevice;
 
import org.junit.Ignore;
 import org.junit.Test;
 
 import ru.kcode.service.Protocol;
 import ru.kcode.service.drivers.USBDebugDriver;
 
 public class ProtocolTest {
 
    @Ignore
     @Test
     public void testCrateProtocol() {
         Protocol p = new Protocol();
         p.addParam(Protocol.MOTOR_1, 12345);
         System.out.println(p.toString());
 
         p = new Protocol();
         p.addParam(Protocol.MOTOR_2, 54321);
         p.addParam(Protocol.MESSAGE, "1 2 a b");
         System.out.println(p.toString());
         System.out.println(p.getMess().length);
     }

    @Ignore
     @Test
    public void testDriver() throws Exception {
         Protocol p = new Protocol();
        p.addParam(Protocol.MESSAGE, "s");
         USBDebugDriver dr = new USBDebugDriver();
         dr.start();
         dr.sendData(p);
         dr.stop();
     }
 }
