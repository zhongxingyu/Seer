 package pro.kornev.kcontrol.service.joystick;
 
 import com.centralnexus.input.Joystick;
 import com.centralnexus.input.JoystickListener;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
import pro.kornev.kcontrol.service.SettingService;
 import pro.kornev.kcontrol.service.SettingsListener;
import pro.kornev.kcontrol.service.network.ProxyServiceListener;
 import pro.kornev.kcontrol.service.network.ProxyService;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vkornev
  * Date: 13.11.13
  * Time: 15:51
  */
 public class JoystickService implements JoystickListener, SettingsListener {
     private ProxyService proxyService;
     private boolean liveLed = false;
 
     public JoystickService() {
     }
 
     @Override
     public void changeJoystick(KJoystick j) {
         if (proxyService == null) return;
         byte leftMotor = (byte)(j.getY() * 50);
         byte rightMotor = (byte)(j.getY() * 50);
 
         if (leftMotor >= 0) {
             leftMotor += 50;
         }
         if (rightMotor >= 0) {
             rightMotor += 50;
         }
 
         Data data = new Data();
         data.cmd = Protocol.Cmd.autoLMS();
         data.bData = leftMotor;
         proxyService.send(data);
 
         data = new Data();
         data.cmd = Protocol.Cmd.autoRMS();
         data.bData = rightMotor;
         proxyService.send(data);
     }
 
     @Override
     public void changeProxy(ProxyService ps) {
         proxyService = ps;
     }
 
     @Override
     public void joystickAxisChanged(Joystick joystick) {
 
     }
 
     @Override
     public void joystickButtonChanged(Joystick joystick) {
         int buttons = joystick.getButtons();
         if ((buttons & Joystick.BUTTON1) == Joystick.BUTTON1) {
             Data data = new Data();
             data.cmd = Protocol.Cmd.autoTriggerLed();
             data.bData = (byte) (liveLed ? 0 : 1);
             proxyService.send(data);
             liveLed = !liveLed;
         }
     }
 }
