 package maw.jit.ipcam;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.Authenticator;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 public class PTZCameraControl {
     // errors
     static int ERROR_TIMEOUT = -3;
     static int ERROR_CANCEL = -5;
 
     // privs
     static int PRI_REFUSE = 0;
     static int PRI_VISITOR = 1;
     static int PRI_OPERATOR = 2;
     static int PRI_ADMINISTRATOR = 3;
 
     static int R160_120 = 2;
     static int R320_240 = 8;
     static int R640_480 = 32;
 
     // ptz
     static int PTZ_STOP = 1;
 
     // tilting
     static int TILT_UP = 0;
     static int TILT_UP_STOP = 1;
     static int TILT_DOWN = 2;
     static int TILT_DOWN_STOP = 3;
 
     // panning
     static int PAN_LEFT = 4;
     static int PAN_LEFT_STOP = 5;
     static int PAN_RIGHT = 6;
     static int PAN_RIGHT_STOP = 7;
 
     // ptz
     static int PTZ_LEFT_UP = 90;
     static int PTZ_RIGHT_UP = 91;
     static int PTZ_LEFT_DOWN = 92;
     static int PTZ_RIGHT_DOWN = 93;
     static int PTZ_CENTER = 25;
     static int PTZ_VPATROL = 26;
     static int PTZ_VPATROL_STOP = 27;
     static int PTZ_HPATROL = 28;
     static int PTZ_HPATROL_STOP = 29;
     static int PTZ_PELCO_D_HPATROL = 20;
     static int PTZ_PELCO_D_HPATROL_STOP = 21;
 
     // on / off
     static int IO_ON = 94;
     static int IO_OFF = 95;
 
     int record_osd = 1;
     boolean ptz_type = false;
 
     int current = 0;
     int resolution;
     int brightness;
     int contrast;
     int refreshMode;
 
     // flip = 0 ;// normal vert , normal horz
     // flip = 1 ;// flipped ver, normal horz
     // flip = 2 ;// normal vert, flipped horz
     // flip = 3 ;// flip horz, flip vert
     int flip;
     int fps;
 
     int led_mode;
     boolean ptz_center_onstart;
     int ptz_auto_patrol_interval;
     int ptz_auto_patrol_type;
     int ptz_patrol_h_rounds;
     int ptz_patrol_v_rounds;
     int ptz_patrol_rate;
     int ptz_preset_rate;
     int ptz_patrol_up_rate;
     int ptz_patrol_down_rate;
     int ptz_patrol_left_rate;
     int ptz_patrol_right_rate;
     boolean ptz_disable_preset;
    boolean ptz_preset_onstart;
 
     String user;
     String pwd;
     int pri;
 
     String id;
     String sys_ver;
     String app_ver;
     String alias;
     long now;
     int tz;
     int alarm_status;
     int ddns_status;
     String ddns_host;
     int oray_type;
     int upnp_status;
     int p2p_status;
     int p2p_local_port;
    int msn_status;
    int wifi_status;
    
     boolean isMovingRight = false;
     boolean isMovingLeft = false;
     boolean isMovingUp = false;
     boolean isMovingDown = false;
     boolean isOn = false;
 
     boolean verticalPatrol = false;
     boolean horizontalPatrol = false;
 
     BasicHTTPAuthenticator auth;
     URI uri;
 
     boolean didInit = false;
     boolean didAuthorize = false;
 
     int currentPreset = -1;
     int lastStoredPreset = -1;
 
     public boolean isConnected() {
         return didInit && didAuthorize;
     }
 
     public static void main(String[] args) throws Exception {
 
         BasicHTTPAuthenticator auth = new BasicHTTPAuthenticator("admin", "");
         URI uri = null;
         try {
             uri = new URI("http://134.84.229.45:8002/");
         } catch (URISyntaxException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         PTZCameraControl ptz = new PTZCameraControl(uri, auth);
 
         // boolean a = ptz.callPreset(10);
         // System.out.println(a);
 
         // ptz.storePreset(10);
         // ptz.callPreset(10);
         // ptz.setAlias("MAW CAM");
 
         /*
          ptz.setVerticalPatrol(true);
          Thread.sleep(4000);
          ptz.setVerticalPatrol(false);
         */
 
         /*
         ptz.getCameraParams();
         ptz.printCameraParams();
         
         ptz.getStatusParams();
         ptz.printStatus();
         
         ptz.getUserParams();
         ptz.printUserParams();
         
         ptz.getMiscParams();
         ptz.printMiscParams();
         */
         /*
         System.out.println("Setting Speed=10 " + ptz.setSpeed(10));
         System.out.println("Starting Down: " + ptz.startRight());
         
         Thread.sleep(1000);
         System.out.println("Setting Speed=10 " + ptz.setSpeed(0));
         Thread.sleep(1000);
         
         System.out.println("Stopping Down: " + ptz.stopRight());
         */
 
         /*
         System.out.println("Set Mirror: " + ptz.setMirror(false, false));
         System.out.println("Set Brightness: " + ptz.setBrightness(15));
 
         Thread.sleep(3000);
 
         System.out.println("Set Brightness: " + ptz.setBrightness(7));
 
         System.out.println("Set Contrast: " + ptz.setContast(15));
 
         Thread.sleep(3000);
 
         System.out.println("Set Contrast: " + ptz.setContast(7));
 
         System.out.println("Set Resolution: " + ptz.setResolution(2));
         Thread.sleep(3000);
         System.out.println("Set Resolution: " + ptz.setResolution(8));
         Thread.sleep(3000);
         System.out.println("Set Resolution: " + ptz.setResolution(32));
         Thread.sleep(3000);
         */
 
         /*
         System.out.println("Setting Speed=0 " + ptz.setSpeed(0));
         System.out.println("Starting Up: " + ptz.startUp());
         Thread.sleep(2000);
         System.out.println("Stopping Up: " + ptz.stopUp());
 
         System.out.println("Setting Speed=10 " + ptz.setSpeed(10));
         System.out.println("Starting Down: " + ptz.startDown());
         /'>/
         Thread.sleep(4000);
         System.out.println("Stopping Down: " + ptz.stopDown());
         */
 
         /*
         System.out.println("Starting Up: " + ptz.startUp());
         Thread.sleep(2000);
         System.out.println("Stopping Up: " + ptz.stopUp());
 
         System.out.println("Starting Down: " + ptz.startDown());
         Thread.sleep(2000);
         System.out.println("Stopping Down: " + ptz.stopDown());
 
         System.out.println("Starting Right: " + ptz.startRight());
         Thread.sleep(2000);
         System.out.println("Stopping Right: " + ptz.stopRight());
 
         System.out.println("Starting Left: " + ptz.startLeft());
         Thread.sleep(2000);
         System.out.println("Stopping Left: " + ptz.stopLeft());
         */
         /*
         Thread.sleep(3000);
         System.out.println("Starting Down: " + ptz.startDown());
         */
 
         /*
         
         System.out.println("Set Freq 0: " + ptz.setFrequency(0));
         Thread.sleep(2000);
         
         System.out.println("Set Freq 1: " + ptz.setFrequency(1));
         Thread.sleep(2000);
 
         System.out.println("Set Freq 2: " + ptz.setFrequency(2));
         Thread.sleep(2000);
 
         System.out.println("Set Freq 1: " + ptz.setFrequency(1));
 
         */
 
         /*
         for (int i = 0; i <= 32; i++) {
 
             System.out.println(i + " " + ptz.setResolution(i));
             Thread.sleep(500);
 
         }
         */
 
         // int i = 4;
         // System.out.println(i + " " + ptz.setResolution(i));
 
         /*
         System.out.println("Set Rez 16: " + ptz.setResolution(16));
         Thread.sleep(1000);
 
         System.out.println("Set Rez 32: " + ptz.setResolution(32));
         Thread.sleep(1000);
         */
 
     }
 
     public PTZCameraControl(URI uri, BasicHTTPAuthenticator auth) {
         this.uri = uri;
         this.auth = auth;
     }
 
     private boolean getUrlCheckOK(String s) {
 
         String result = getURL(s);
         if (result == null) {
             System.err.println(s + " ERROR: exception.");
             return false;
         }
 
         if (result.compareToIgnoreCase("ok.") != 0) {
             System.err.println(s + " ERROR: " + result);
             return false;
         }
 
         return true;
     }
 
     private String getURL(String s) {
         return getURL(s, false);
     }
 
     private String getURL(String s, boolean skipInit) {
         // BufferedReader br = new BufferedReader(uri.toURL().)
 
         if (!didAuthorize) {
             authorize();
         }
 
         if (!skipInit && !didInit) {
             init();
         }
 
         URI url = null;
         HttpURLConnection hurlc = null;
 
         InputStream is = null;
         BufferedReader br = null;
         StringBuffer sb = new StringBuffer();
 
         try {
             url = new URI(s);
             hurlc = (HttpURLConnection) url.toURL().openConnection();
             hurlc.connect();
 
             is = hurlc.getInputStream();
 
             br = new BufferedReader(new InputStreamReader(is));
             String line = null;
             while ((line = br.readLine()) != null) {
                 sb.append(line);
             }
 
             br.close();
 
         } catch (URISyntaxException e) {
             System.err.println("URL Syntax failure: " + s);
             e.printStackTrace();
             return null;
         } catch (MalformedURLException e1) {
             System.err.println("URL Malformed: " + s);
             e1.printStackTrace();
             return null;
         } catch (IOException e1) {
             System.err.println("IO Exception: " + s);
             e1.printStackTrace();
             return null;
         }
 
         return sb.toString();
     }
 
     private void authorize() {
         if (auth != null && !didAuthorize) {
             Authenticator.setDefault(auth);
         }
 
         didAuthorize = true;
 
     }
 
     public void init() {
 
         authorize();
 
         getStatusParams();
         getUserParams();
         getMiscParams();
         getCameraParams();
 
         didInit = true;
     }
 
     public boolean getCameraParams() {
         // http://134.84.229.45:8002/get_camera_params.cgi
         String result = getURL(uri.toString() + "get_camera_params.cgi", true);
         if (result == null) return false;
         parseCameraParams(result);
         return true;
     }
 
     public boolean getStatusParams() {
         // http://134.84.229.45:8002/get_status.cgi
         String result = getURL(uri.toString() + "get_status.cgi", true);
         if (result == null) return false;
         parseStatus(result);
         return true;
     }
 
     public boolean getUserParams() {
         // http://134.84.229.45:8002/check_user.cgi
         String result = getURL(uri.toString() + "check_user.cgi", true);
         if (result == null) return false;
         parseUserParams(result);
         return true;
     }
 
     public boolean getMiscParams() {
         // http://134.84.229.45:8002/get_misc.cgi
         String result = getURL(uri.toString() + "get_misc.cgi", true);
         if (result == null) return false;
         parseMiscParams(result);
         return true;
     }
 
     public boolean setCenterOnStart(boolean value) {
 
         String s = uri.toString() + "set_misc.cgi?ptz_center_onstart=" + (value ? "1" : "0");
         boolean success = getUrlCheckOK(s);
         if (success) {
             this.ptz_center_onstart = value;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean disablePreset(boolean value) {
         String s = uri.toString() + "set_misc.cgi?ptz_disable_preset=" + (value ? "1" : "0");
         boolean success = getUrlCheckOK(s);
         if (success) {
             this.ptz_disable_preset = value;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean setPresetRate(int nValue) {
         if (nValue >= 0 && nValue <= 100) {
             String s = uri.toString() + "set_misc.cgi?ptz_preset_rate=" + nValue;
             boolean result = getUrlCheckOK(s);
             if (result) {
                 ptz_preset_rate = nValue;
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public boolean setPatrolUpRate(int nValue) {
         if (nValue >= 0 && nValue <= 100) {
             String s = uri.toString() + "set_misc.cgi?ptz_patrol_up_rate=" + nValue;
             boolean result = getUrlCheckOK(s);
             if (result) {
                 ptz_patrol_up_rate = nValue;
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public boolean setPatrolDownRate(int nValue) {
         if (nValue >= 0 && nValue <= 100) {
             String s = uri.toString() + "set_misc.cgi?ptz_patrol_down_rate=" + nValue;
             boolean result = getUrlCheckOK(s);
             if (result) {
                 ptz_patrol_down_rate = nValue;
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public boolean setPatrolLeftRate(int nValue) {
         if (nValue >= 0 && nValue <= 100) {
             String s = uri.toString() + "set_misc.cgi?ptz_patrol_left_rate=" + nValue;
             boolean result = getUrlCheckOK(s);
             if (result) {
                 ptz_patrol_left_rate = nValue;
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public boolean setPatrolRightRate(int nValue) {
         if (nValue >= 0 && nValue <= 100) {
             String s = uri.toString() + "set_misc.cgi?ptz_patrol_right_rate=" + nValue;
             boolean result = getUrlCheckOK(s);
             if (result) {
                 ptz_patrol_right_rate = nValue;
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public boolean setPatrolRate(int nValue) {
         if (nValue >= 0 && nValue <= 100) {
             String s = uri.toString() + "set_misc.cgi?ptz_patrol_rate=" + nValue;
             boolean result = getUrlCheckOK(s);
             if (result) {
                 ptz_patrol_rate = nValue;
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public boolean setLEDMode(int nValue) {
         // 0 : non-connected network out
         // 1 : non-connected network out with more slow-frequencey flicker
         // 2 : been extinguished
 
         if (nValue >= 0 && nValue <= 2) {
             String s = uri.toString() + "set_misc.cgi?led_mode=" + nValue;
             boolean result = getUrlCheckOK(s);
             if (result) {
                 led_mode = nValue;
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     private boolean decoder_control(int command) {
         String s = uri.toString() + "decoder_control.cgi?command=" + command;
         return getUrlCheckOK(s);
     }
 
     private boolean camera_control(int param, int value) {
         return camera_control(param, Integer.toString(value));
     }
 
     private boolean camera_control(int param, String value) {
         String s = uri.toString() + "camera_control.cgi?param=" + param + "&value=" + value;
 
         //System.out.println(s);
 
         return getUrlCheckOK(s);
 
     }
 
     public boolean setAlias(String alias) {
 
         String u = null;
         String p = null;
         String a = null;
         try {
             u = URLEncoder.encode(auth.getUsername(), "UTF-8");
             p = URLEncoder.encode(auth.getPassword(), "UTF-8");
             a = URLEncoder.encode(alias, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
 
         String s = uri.toString() + "set_alias.cgi?user=" + u + "&pwd=" + p + "&alias=" + a;
         boolean result = getUrlCheckOK(s);
         if (result) {
             this.alias = alias;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean setRefreshMode(int i) {
         // freq
         // 0 : 50hz
         // 1 : 60hz
         // 2 : outdoor
         if ((i >= 0 && i <= 2) && camera_control(3, i)) {
             refreshMode = i;
             return true;
         }
         return false;
 
     }
 
     public boolean setResolution(int i) {
         // res must be:
         // 2 : 160 x 120
         // 4 : 176 x 144
         // 8 : 320 x 240
         // 16 : 352 x 288
         // 32 : 640 x 480
 
         if ((i == 2 || i == 4 || i == 8 || i == 16 || i == 32) && camera_control(0, i)) {
             resolution = i;
             return true;
         }
 
         return false;
     }
 
     public boolean setBrightness(int i) {
         if ((i >= 0 && i <= 15) && camera_control(1, i * 16)) {
             brightness = i;
             return true;
         }
         return false;
     }
 
     public boolean setContrast(int i) {
         if ((i >= 0 && i <= 32) && camera_control(2, i)) {
             contrast = i;
             return true;
         }
         return false;
     }
 
     public boolean setMirror(boolean mirrorHorizontal, boolean mirrorVertical) {
         // flip = 0 ;// normal vert , normal horz
         // flip = 1 ;// flipped ver, normal horz
         // flip = 2 ;// normal vert, flipped horz
         // flip = 3 ;// flip vert, flip horz
 
         int tempFlip = flip;
 
         if (mirrorHorizontal) {
             if (mirrorVertical) {
                 tempFlip = 3;
             } else {
                 tempFlip = 2;
             }
         } else {
             if (mirrorVertical) {
                 tempFlip = 1;
             } else {
                 tempFlip = 0;
             }
         }
 
         if (camera_control(5, tempFlip)) {
             flip = tempFlip;
             return true;
         }
 
         return false;
     }
 
     public boolean isMirroredHorizontally() {
         return (flip & 0x02) != 0;
     }
 
     public boolean isMirroredVertically() {
         return (flip & 0x01) != 0;
     }
 
     public boolean setMirrorHorizontal(boolean horz) {
         if (isMirroredHorizontally() == horz) return true;
         return toggleHorizontalMirror();
 
     }
 
     public boolean setMirrorVertical(boolean vert) {
         if (isMirroredVertically() == vert) return true;
         return toggleVerticalMirror();
     }
 
     public boolean toggleVerticalMirror() {
         int tempFlip = flip;
 
         if (isMirroredVertically()) { // is vertical flip?
             tempFlip &= 0x02;
         } else
             tempFlip |= 0x01;
 
         if (camera_control(5, tempFlip)) {
             flip = tempFlip;
             return true;
         }
 
         return false;
     }
 
     public boolean toggleHorizontalMirror() {
         int tempFlip = flip;
 
         if (isMirroredHorizontally()) { // is vertical flipped?
             tempFlip &= 0x01;
         } else {
             tempFlip |= 0x02;
         }
 
         if (camera_control(5, tempFlip)) {
             flip = tempFlip;
             return true;
         }
 
         return false;
     }
 
     public boolean callPreset(int presetNumber) {
         if (presetNumber >= 1 && presetNumber <= 15) {
             presetNumber += (29 + presetNumber * 2);
             // 58 is max
             if (decoder_control(presetNumber)) {
                 currentPreset = presetNumber;
                 return true;
             }
             return false;
         } else {
             return false;
         }
     }
 
     public boolean storePreset(int presetNumber) {
         if (presetNumber >= 1 && presetNumber <= 15) {
             presetNumber += (28 + presetNumber * 2);
             // 58
             if (decoder_control(presetNumber)) {
                 lastStoredPreset = presetNumber;
                 return true;
             }
             return false;
         } else {
             return false;
         }
     }
 
     public boolean startVerticalPatrol() {
         if (!ptz_type && decoder_control(PTZ_VPATROL)) {
             verticalPatrol = true;
             return true;
         }
         return false;
     }
 
     public boolean stopVerticalPatrol() {
         if (!ptz_type && decoder_control(PTZ_VPATROL_STOP)) {
             verticalPatrol = false;
             return true;
         }
         return false;
     }
 
     public boolean startHorizontalPatrol() {
         boolean result = false;
         if (ptz_type) {
             result = decoder_control(PTZ_PELCO_D_HPATROL);
         } else {
             result = decoder_control(PTZ_HPATROL);
         }
         if (result) {
             horizontalPatrol = true;
             return true;
         }
         return false;
     }
 
     public boolean stopHorizontalPatrol() {
         boolean result = false;
         if (ptz_type) {
             result = decoder_control(PTZ_PELCO_D_HPATROL_STOP);
         } else {
             result = decoder_control(PTZ_HPATROL_STOP);
         }
 
         if (result) {
             horizontalPatrol = false;
             return true;
 
         }
 
         return false;
     }
 
     public boolean setHorizontalPatrol(boolean enable) {
         return enable ? startHorizontalPatrol() : stopHorizontalPatrol();
     }
 
     public boolean setVerticalPatrol(boolean enable) {
         return enable ? startVerticalPatrol() : stopVerticalPatrol();
     }
 
     public boolean center() {
         return !ptz_type && decoder_control(PTZ_CENTER);
     }
 
     public boolean setMoveUp(boolean value) {
         return value ? startUp() : stopUp();
     }
 
     public boolean setMoveDown(boolean value) {
         return value ? startDown() : stopDown();
     }
 
     public boolean setMoveLeft(boolean value) {
         return value ? startLeft() : stopLeft();
     }
 
     public boolean setMoveRight(boolean value) {
         return value ? startRight() : stopRight();
     }
 
     public boolean startUp() {
         boolean result = false;
         if ((flip & 0x01) > 0) {
             result = decoder_control(TILT_DOWN);
         } else {
             result = decoder_control(TILT_UP);
         }
         if (result) {
             isMovingUp = true;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean stopUp() {
         boolean result = false;
         if (!ptz_type) {
             result = decoder_control(PTZ_STOP);
         } else if ((flip & 0x01) > 0) {
             result = decoder_control(TILT_DOWN_STOP);
         } else {
             result = decoder_control(TILT_UP_STOP);
         }
 
         if (result) {
             isMovingUp = false;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean startDown() {
         boolean result = false;
 
         if ((flip & 0x01) > 0) {
             result = decoder_control(TILT_UP);
         } else {
             result = decoder_control(TILT_DOWN);
         }
 
         if (result) {
             isMovingDown = true;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean stopDown() {
         boolean result = false;
         if (!ptz_type) {
             result = decoder_control(PTZ_STOP);
         } else if ((flip & 0x01) > 0) {
             result = decoder_control(TILT_UP_STOP);
         } else {
             result = decoder_control(TILT_DOWN_STOP);
         }
 
         if (result) {
             isMovingDown = false;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean startLeft() {
         boolean result = false;
         if ((flip & 0x02) > 0) {
             result = decoder_control(PAN_RIGHT);
         } else {
             result = decoder_control(PAN_LEFT);
         }
         if (result) {
             isMovingLeft = true;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean stopLeft() {
         boolean result = false;
         if (!ptz_type) {
             result = decoder_control(PTZ_STOP);
         } else if ((flip & 0x02) > 0) {
             result = decoder_control(PAN_RIGHT_STOP);
         } else {
             result = decoder_control(PAN_LEFT_STOP);
         }
 
         if (result) {
             isMovingLeft = false;
             return true;
         } else {
             return false;
         }
     }
 
     public boolean startRight() {
         boolean result = false;
         if ((flip & 0x02) > 0) {
             result = decoder_control(PAN_LEFT);
         } else {
             result = decoder_control(PAN_RIGHT);
         }
 
         if (result) {
             isMovingRight = true;
             return true;
         } else {
             return false;
         }
 
     }
 
     public boolean stopRight() {
         boolean result = false;
         if (!ptz_type) {
             result = decoder_control(PTZ_STOP);
         } else if ((flip & 0x02) > 0) {
             result = decoder_control(PAN_LEFT_STOP);
         } else {
             result = decoder_control(PAN_RIGHT_STOP);
         }
 
         if (result) {
             isMovingRight = false;
             return true;
         } else {
             return false;
         }
     }
 
     public boolean setCameraOn(boolean value) {
         return value ? turnOn() : turnOff();
     }
 
     public boolean turnOn() {
         if (decoder_control(IO_ON)) {
             isOn = true;
             return true;
         } else {
             return false;
         }
     }
 
     public boolean turnOff() {
         if (decoder_control(IO_OFF)) {
             isOn = false;
             return true;
         } else {
             return false;
         }
 
     }
 
     public void printStatus() {
         StringBuffer sb = new StringBuffer();
 
         sb.append("id: " + id + "\n");
         sb.append("sys_ver: " + sys_ver + "\n");
         sb.append("app_ver: " + app_ver + "\n");
         sb.append("alias: " + alias + "\n");
         sb.append("now: " + now + "\n");
         sb.append("tz: " + tz + "\n");
         sb.append("alarm_status: " + alarm_status + "\n");
         sb.append("ddns_status: " + ddns_status + "\n");
         sb.append("ddns_host: " + ddns_host + "\n");
         sb.append("oray_type: " + oray_type + "\n");
         sb.append("upnp_status: " + upnp_status + "\n");
         sb.append("p2p_status: " + p2p_status + "\n");
         sb.append("p2p_local_port: " + p2p_local_port + "\n");
 
         System.out.println("STATUS==========");
         System.out.println(sb.toString());
 
     }
 
     public void parseStatus(String s) {
         // http://134.84.229.45:8002/get_status.cgi
 
         // var id='00A8F6006B4B';
         // var sys_ver='4.22.2.27';
         // var app_ver='4.3.3.42';
         // var alias='IPCAM-127';
         // var now=1296702234;
         // var tz=0;
         // var alarm_status=0;
         // var ddns_status=22;
         // var ddns_host='';
         // var oray_type=0;
         // var upnp_status=0;
         // var p2p_status=4;
         // var p2p_local_port=28122;
        // var msn_status=0;
        // var wifi_status=0;
 
         NameValuePair[] nvp = parseParameters(s);
 
         for (NameValuePair n : nvp) {
             if (n.isParam("id")) {
                 id = n.getValue();
             } else if (n.isParam("sys_ver")) {
                 sys_ver = n.getValue();
             } else if (n.isParam("app_ver")) {
                 app_ver = n.getValue();
             } else if (n.isParam("alias")) {
                 alias = n.getValue();
             } else if (n.isParam("now")) {
                 now = n.getLongValue();
             } else if (n.isParam("tz")) {
                 tz = n.getIntValue();
             } else if (n.isParam("alarm_status")) {
                 alarm_status = n.getIntValue();
             } else if (n.isParam("ddns_status")) {
                 ddns_status = n.getIntValue();
             } else if (n.isParam("ddns_host")) {
                 ddns_host = n.getValue();
             } else if (n.isParam("oray_type")) {
                 oray_type = n.getIntValue();
             } else if (n.isParam("upnp_status")) {
                 upnp_status = n.getIntValue();
             } else if (n.isParam("p2p_status")) {
                 p2p_status = n.getIntValue();
             } else if (n.isParam("p2p_local_port")) {
                 p2p_local_port = n.getIntValue();
            } else if (n.isParam("msn_status")) {    
                msn_status = n.getIntValue();
            } else if (n.isParam("wifi_status")) {
                wifi_status = n.getIntValue();
             } else {
                 System.err.println("UNPARSED: " + n.toString());
             }
         }
 
     }
 
     public void printUserParams() {
         StringBuffer sb = new StringBuffer();
 
         sb.append("user: " + user + "\n");
         sb.append("pwd: " + pwd + "\n");
         sb.append("pri: " + pri + "\n");
 
         System.out.println("USER PARAMS==========");
         System.out.println(sb.toString());
 
     }
 
     public void parseUserParams(String s) {
         // http://134.84.229.45:8002/check_user.cgi
         // var user='admin';
         // var pwd='';
         // var pri=3;
 
         NameValuePair[] nvp = parseParameters(s);
 
         for (NameValuePair n : nvp) {
             if (n.isParam("user")) {
                 user = n.getValue();
             } else if (n.isParam("pwd")) {
                 pwd = n.getValue();
             } else if (n.isParam("pri")) {
                 pri = n.getIntValue();
             } else {
                 System.err.println("UNPARSED: " + n.toString());
             }
         }
     }
 
     public void printMiscParams() {
 
         StringBuffer sb = new StringBuffer();
 
         sb.append("led_mode: " + led_mode + "\n");
         sb.append("pwd: " + pwd + "\n");
         sb.append("ptz_center_onstart: " + ptz_center_onstart + "\n");
         sb.append("ptz_auto_patrol_interval: " + ptz_auto_patrol_interval + "\n");
         sb.append("ptz_auto_patrol_type: " + ptz_auto_patrol_type + "\n");
         sb.append("ptz_patrol_h_rounds: " + ptz_patrol_h_rounds + "\n");
         sb.append("ptz_patrol_v_rounds: " + ptz_patrol_v_rounds + "\n");
         sb.append("ptz_patrol_rate: " + ptz_patrol_rate + "\n");
         sb.append("ptz_patrol_up_rate" + ptz_patrol_up_rate + "\n");
         sb.append("ptz_patrol_down_rate: " + ptz_patrol_down_rate + "\n");
         sb.append("ptz_patrol_left_rate: " + ptz_patrol_left_rate + "\n");
         sb.append("ptz_patrol_right_rate: " + ptz_patrol_right_rate + "\n");
         sb.append("ptz_patrol_right_rate: " + ptz_patrol_right_rate + "\n");
         sb.append("ptz_disable_preset: " + ptz_disable_preset + "\n");
 
         System.out.println("MISC PARAMS==========");
         System.out.println(sb.toString());
 
     }
 
     public void parseMiscParams(String s) {
         // http://134.84.229.45:8002/get_misc.cgi
         // var led_mode=0;
         // var ptz_center_onstart=0;
         // var ptz_auto_patrol_interval=0;
         // var ptz_auto_patrol_type=0;
         // var ptz_patrol_h_rounds=0;
         // var ptz_patrol_v_rounds=0;
         // var ptz_patrol_rate=5;
         // var ptz_preset_rate=1;
         // var ptz_patrol_up_rate=10;
         // var ptz_patrol_down_rate=10;
         // var ptz_patrol_left_rate=10;
         // var ptz_patrol_right_rate=10;
         // var ptz_disable_preset=0;
 
         NameValuePair[] nvp = parseParameters(s);
 
         for (NameValuePair n : nvp) {
             if (n.isParam("led_mode")) {
                 led_mode = n.getIntValue();
             } else if (n.isParam("ptz_center_onstart")) {
                 ptz_center_onstart = n.getBooleanValue();
             } else if (n.isParam("ptz_auto_patrol_interval")) {
                 ptz_auto_patrol_interval = n.getIntValue();
             } else if (n.isParam("ptz_auto_patrol_type")) {
                 ptz_auto_patrol_type = n.getIntValue();
             } else if (n.isParam("ptz_patrol_h_rounds")) {
                 ptz_patrol_h_rounds = n.getIntValue();
             } else if (n.isParam("ptz_patrol_v_rounds")) {
                 ptz_patrol_v_rounds = n.getIntValue();
             } else if (n.isParam("ptz_patrol_rate")) {
                 ptz_patrol_rate = n.getIntValue();
             } else if (n.isParam("ptz_preset_rate")) {
                 ptz_preset_rate = n.getIntValue();
             } else if (n.isParam("ptz_patrol_up_rate")) {
                 ptz_patrol_up_rate = n.getIntValue();
             } else if (n.isParam("ptz_patrol_down_rate")) {
                 ptz_patrol_down_rate = n.getIntValue();
             } else if (n.isParam("ptz_patrol_left_rate")) {
                 ptz_patrol_left_rate = n.getIntValue();
             } else if (n.isParam("ptz_patrol_right_rate")) {
                 ptz_patrol_right_rate = n.getIntValue();
             } else if (n.isParam("ptz_disable_preset")) {
                 ptz_disable_preset = n.getBooleanValue();
            } else if (n.isParam("ptz_preset_onstart")) {
                ptz_preset_onstart = n.getBooleanValue();
             } else {
                 System.err.println("UNPARSED: " + n.toString());
             }
         }
 
     }
 
     public void printCameraParams() {
         StringBuffer sb = new StringBuffer();
 
         sb.append("resolution: " + resolution + "\n");
         sb.append("brightness: " + brightness + "\n");
         sb.append("contrast: " + contrast + "\n");
         sb.append("mode: " + refreshMode + "\n");
         sb.append("flip: " + flip + "\n");
         sb.append("fps: " + fps + "\n");
 
         System.out.println("CAMERA PARAMS==========");
         System.out.println(sb.toString());
 
     }
 
     public void parseCameraParams(String s) {
         // var resolution=2;
         // var brightness=112;
         // var contrast=4;
         // var mode=2;
         // var flip=2;
         // var fps=0;
 
         NameValuePair[] nvp = parseParameters(s);
 
         for (NameValuePair n : nvp) {
             if (n.isParam("resolution")) {
                 resolution = n.getIntValue();
             } else if (n.isParam("brightness")) {
                 brightness = n.getIntValue();
             } else if (n.isParam("contrast")) {
                 contrast = n.getIntValue();
             } else if (n.isParam("mode")) {
                 refreshMode = n.getIntValue();
             } else if (n.isParam("flip")) {
                 flip = n.getByteValue();
             } else if (n.isParam("fps")) {
                 fps = n.getIntValue();
             } else {
                 System.err.println("UNPARSED: " + n.toString());
             }
         }
 
     }
 
     private NameValuePair[] parseParameters(String rawParams) {
 
         String[] lines = rawParams.split(";");
         ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
         for (int i = 0; i < lines.length; i++) {
             String line = lines[i].trim();
 
             line = line.replace(";", "");
             line = line.replace("var ", "");
             line = line.trim();
 
             String[] nameValue = line.split("=");
 
             // skip it if needed
             if (nameValue.length != 2) {
                 System.err.println("Param length != 2 : " + rawParams);
                 continue;
             }
 
             String name = nameValue[0].trim();
             String value = nameValue[1].trim();
 
             nvp.add(new NameValuePair(name, value));
         }
 
         NameValuePair[] nvpa = new NameValuePair[nvp.size()];
         nvpa = nvp.toArray(nvpa);
         return nvpa;
 
     }
 
     private class NameValuePair {
         String name;
         String value;
 
         public NameValuePair(String name, String value) {
             this.name = name;
             this.value = value;
         }
 
         public int getIntValue() {
             return Integer.parseInt(value);
         }
 
         public float getFloatValue() {
             return Float.parseFloat(value);
         }
 
         public long getLongValue() {
             return Long.parseLong(value);
         }
 
         public boolean getBooleanValue() {
             return value.compareTo("1") == 0;
         }
 
         public byte getByteValue() {
             return Byte.parseByte(value);
         }
 
         public String getValue() {
 
             if (value.startsWith("'") && value.endsWith("'")) {
                 value = value.substring(1, value.length() - 1);
             }
 
             if (value.length() == 0) value = null;
 
             return value;
         }
 
         public String getName() {
             return name;
         }
 
         public boolean isParam(String s) {
             return name.compareToIgnoreCase(s) == 0;
         }
 
         public String toString() {
             return "name: " + name + " value: " + value;
         }
     }
 
 }
