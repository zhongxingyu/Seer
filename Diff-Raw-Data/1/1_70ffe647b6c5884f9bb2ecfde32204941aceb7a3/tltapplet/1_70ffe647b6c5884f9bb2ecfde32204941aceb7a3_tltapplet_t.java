 import gimbalcom.rpc.RemoteFactory;
 import gimbalcom.rpc.RpcRemoteIntegerFactory;
 
 import java.applet.Applet;
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Label;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Timer;
 
 import org.mbed.RPC.HTTPRPC;
 
 public class tltapplet extends Applet implements ActionListener {
 
     HTTPRPC mbed;
     boolean threadSuspended;
     Timer refresh_timer;
     private static final long serialVersionUID = 1L;
 
     // setup local and rpc variables
     RemoteFactory.Integer CtrlAction;
     RemoteFactory.Integer LEDStatus;
     RemoteFactory.Integer SynthFrequencyActual;
     RemoteFactory.Integer SynthFrequencyUpdate;
     RemoteFactory.Integer AttenuatorActual;
     RemoteFactory.Integer AttenuatorUpdate;
 
     RemoteFactory.Integer minFrequencyMHz;
     RemoteFactory.Integer maxFrequencyMHz;
 
     RemoteFactory.Integer ipAddrRpc;
     RemoteFactory.Integer ipMaskRpc;
 
     // screen position coordinates for drawing LEDs, Btns and text
     int LED1_x = 20;
     int LED2_x = 80;
     int LED3_x = 140;
     int LED4_x = 200;
     int LED1_y = 21;
     int LED2_y = 191;
     int LED3_y = 241;
     int LED4_y = 271;
     int LED5_y = 321;
     int LED6_y = 371;
     int LED_dx = 40;
     int LED_dy = 28;
     int LED_r = 6;
 
     boolean frontPanelControlled = false;
     boolean isPLO = false;
     int SynthLockLED_i = 0;
 
     int PSU1Alarm_i = 0;
     int SynthType_i = 0;
     int AttType_i = 0;
     int SerialAlarm_i = 0;
     private boolean ploOscAlarm;
 
     int SynthFrequency;
     int FreqUpdateIcon = 0; // displayed if the frequency has been changed
     int F_INC = 25; // increment value in MHz
     int SYNTH_FREQ_MIN;
     int SYNTH_FREQ_MAX;
 
     int Attenuation;
     int AttUpdateIcon = 0; // displayed if the attenuator has been changed
     int A_INC = 1; // increment value in bits
     int A_INCx = 4; // increment value in bits
     int A_INCxx = 40; // increment value in bits
     int ATT_MIN = 0;
     int attenuationMax;
 
     int CtrlStatusData = 0;
     int CommsOpenFlag = 0;
     int comms_active = 0;
     int connection_ctr = 0;
     int update_ctr = 0;
 
     int ipAddr;
     int ipMask;
 
     // Button Inactive_ALBtn;
 
     Button LocalActive_ALBtn;
     Button Enter_ALBtn;
     Button Enter2_ALBtn;
     Button Increase_ALBtn;
     Button Decrease_ALBtn;
     Button AttInc_ALBtn;
     Button AttDec_ALBtn;
     Button AttIncx_ALBtn;
     Button AttDecx_ALBtn;
     Button AttIncxx_ALBtn;
     Button AttDecxx_ALBtn;
     Button Refresh_ALBtn;
 
     Label ipAddrLabel = new Label("IP Address");
     TextField ipAddrField = new TextField(20);
     Label ipMaskLabel = new Label("IP Mask");
     TextField ipMaskField = new TextField(20);
     Label ipErrorLabel = new Label();
     Button ipSet = new Button("Update IP");
 
     // **************************************************************************
     // * function to initialise
     // *
     @Override
     public void init() {
 
         setLayout(null);
         String url = getParameter("url");
 
         if (url == null) {
             System.out.println("Applet starting on this");
             mbed = new HTTPRPC(this);
         } else {
             System.out.println("Applet starting on " + url);
             mbed = new HTTPRPC(url);
         }
 
         RemoteFactory factory = new RpcRemoteIntegerFactory(mbed);
                 // Use this for off line experiments: new DummyRemoteIntegerFactory(); 
 
         LEDStatus = factory.create("RemoteLEDStatus");
         int ledStatusI = LEDStatus.read_int();
         System.out.format("LEDStatus %04X\n", ledStatusI);
         CommsOpenFlag = (ledStatusI >> 12) & 0x01;
 
         if (CommsOpenFlag == 0) {
             CtrlAction = factory.create("RemoteCtrlAction");
             CtrlAction.write(0x01); // 01=Set Remote Comms Open/Active
             comms_active = 1;
 
             SynthFrequencyActual = factory.create("RemoteSynthFrequencyActual");
             SynthFrequencyUpdate = factory.create("RemoteSynthFrequencyUpdate");
             AttenuatorActual = factory.create("RemoteAttenuatorActual");
             AttenuatorUpdate = factory.create("RemoteAttenuatorUpdate");
 
             minFrequencyMHz = factory.create("RemoteMinFreqMHz");
             maxFrequencyMHz = factory.create("RemoteMaxFreqMHz");
 
             ipAddrRpc = factory.create("IPAddr");
             ipMaskRpc = factory.create("IPMask");
 
             int rate;
             try {
                 rate = Integer.parseInt(getParameter("rate"));
             } catch (Exception e) {
                 System.err.println("No parameter found");
                 rate = 1000;
             }
             refresh_timer = new Timer(rate, timerListener);
             refresh_timer.start();
 
             LocalActive_ALBtn = new Button("Local / Remote");
             Enter_ALBtn = new Button("Enter");
             Enter2_ALBtn = new Button("Enter");
             Increase_ALBtn = new Button("Inc");
             Decrease_ALBtn = new Button("Dec");
             AttInc_ALBtn = new Button("^");
             AttDec_ALBtn = new Button("v");
             AttIncx_ALBtn = new Button("^");
             AttDecx_ALBtn = new Button("v");
             AttIncxx_ALBtn = new Button("^");
             AttDecxx_ALBtn = new Button("v");
             Refresh_ALBtn = new Button("Update Connection Data");
 
             LocalActive_ALBtn.setBounds(80, 20, 160, 30);
             Enter_ALBtn.setBounds(20, 120, 60, 30);
             Enter2_ALBtn.setBounds(20, 240, 60, 60);
             Increase_ALBtn.setBounds(100, 120, 60, 30);
             Decrease_ALBtn.setBounds(180, 120, 60, 30);
             AttInc_ALBtn.setBounds(110, 240, 30, 20);
             AttDec_ALBtn.setBounds(110, 280, 30, 20);
             AttIncx_ALBtn.setBounds(160, 240, 30, 20);
             AttDecx_ALBtn.setBounds(160, 280, 30, 20);
             AttIncxx_ALBtn.setBounds(210, 240, 30, 20);
             AttDecxx_ALBtn.setBounds(210, 280, 30, 20);
             Refresh_ALBtn.setBounds(20, 420, 220, 30);
 
             ipAddrLabel.setBounds(40, 470, 70, 20);
             ipAddrField.setBounds(110, 470, 120, 20);
 
             ipMaskLabel.setBounds(40, 500, 70, 20);
             ipMaskField.setBounds(110, 500, 120, 20);
 
             ipSet.setBounds(160, 530, 60, 20);
             ipErrorLabel.setBounds(20, 560, 220, 20);
 
             add(LocalActive_ALBtn);
             add(Enter_ALBtn);
             add(Enter2_ALBtn);
             add(Increase_ALBtn);
             add(Decrease_ALBtn);
             add(AttInc_ALBtn);
             add(AttDec_ALBtn);
             add(AttIncx_ALBtn);
             add(AttDecx_ALBtn);
             add(AttIncxx_ALBtn);
             add(AttDecxx_ALBtn);
             add(Refresh_ALBtn);
             add(ipAddrLabel);
             add(ipAddrField);
             add(ipMaskLabel);
             add(ipMaskField);
             add(ipErrorLabel);
             add(ipSet);
 
             LocalActive_ALBtn.addActionListener(this);
             Enter_ALBtn.addActionListener(this);
             Enter2_ALBtn.addActionListener(this);
             Increase_ALBtn.addActionListener(this);
             Decrease_ALBtn.addActionListener(this);
             AttInc_ALBtn.addActionListener(this);
             AttDec_ALBtn.addActionListener(this);
             AttIncx_ALBtn.addActionListener(this);
             AttDecx_ALBtn.addActionListener(this);
             AttIncxx_ALBtn.addActionListener(this);
             AttDecxx_ALBtn.addActionListener(this);
             Refresh_ALBtn.addActionListener(this);
             ipSet.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     try {
                         int addr = ipIntFromTextField(ipAddrField);
                         int mask = ipIntFromTextField(ipMaskField);
 
                         ipAddrField.setText("IP address updating");
                         ipMaskField.setText("IP mask updating");
                         ipErrorLabel.setText("");
                         try {
                             Thread.sleep(1000);  // give time for repaint
                         } catch (InterruptedException x) {
                             System.out.println("This is surprising... Thread.sleep() raised InterruptedException");
                             Thread.currentThread().interrupt();
                         }
 
                         ipAddrRpc.write(addr);
                         ipMaskRpc.write(mask);
                         CtrlAction.write(7); // notify IP change
                     } catch (IllegalArgumentException x) {
                         ipErrorLabel.setText(x.getMessage());
                     }
                 }});
 
             get_data();
             SynthFrequency = SynthFrequencyActual.read_int();
             Attenuation = AttenuatorActual.read_int();
 
             // get limits on user entered frequency from mbed (no longer using SynthType_i)
             SYNTH_FREQ_MIN = minFrequencyMHz.read_int();
             SYNTH_FREQ_MAX = maxFrequencyMHz.read_int();
 
             System.out.format("Synth frequency: actual = %d, limits: min = %d  max = %d\n",
                             SynthFrequency, SYNTH_FREQ_MIN, SYNTH_FREQ_MAX);
         } else {
             comms_active = 0;
         }
     }
 
     // **************************************************************************
     // * functions for timer and memory control
     // *
     @Override
     public void start() {
         if (comms_active == 1) {
             refresh_timer.start();
         }
     }
 
     @Override
     public void stop() {
         if (comms_active == 1) {
             CtrlAction.write(0x02); // 01=Set Remote Comms off
             refresh_timer.stop();
         }
         mbed.delete();
         super.destroy();
     }
 
     @Override
     public void destroy() {
         if (comms_active == 1) {
             CtrlAction.write(0x02); // 01=Set Remote Comms off
         }
         super.destroy();
         mbed.delete();
 
     }
 
     // **************************************************************************
     // * function to get data from mbed RPC variable
     // *
     public void get_data() {
 
         int LEDStatus_i = LEDStatus.read_int();
 
         ploOscAlarm = ((LEDStatus_i >> 1) & 0x00000001) != 0;
         SynthLockLED_i = ((LEDStatus_i >> 2) & 0x00000001);
         frontPanelControlled = (LEDStatus_i & 0x10) != 0;
         isPLO = (LEDStatus_i & 0x20) != 0;
         PSU1Alarm_i = ((LEDStatus_i >> 7) & 0x00000001);
         SynthType_i = ((LEDStatus_i >> 9) & 0x00000001);
         AttType_i = ((LEDStatus_i >> 10) & 0x00000001);
         SerialAlarm_i = ((LEDStatus_i >> 11) & 0x00000001);
 
         if (frontPanelControlled) {
             SynthFrequency = SynthFrequencyActual.read_int();
             Attenuation = AttenuatorActual.read_int();
         }
         
         Enter_ALBtn.setVisible(!isPLO);
         Increase_ALBtn.setVisible(!isPLO);
         Decrease_ALBtn.setVisible(!isPLO);
 
         attenuationMax = AttType_i < 1 ? 127 : 255;
 
         ipAddrField.setEnabled(!frontPanelControlled);
         ipMaskField.setEnabled(!frontPanelControlled);
         ipSet.setEnabled(!frontPanelControlled);
         setIpTextField(ipAddrField, ipAddrRpc);
         setIpTextField(ipMaskField, ipMaskRpc);
 
         System.out.format("LEDStatus %04X SynthLockLED_i %d, frontPanelControlled %s, PSU1Alarm_i %d, SynthType_i %d, AttType_i %d, SerialAlarm_i %d\n" +
                 "SynthFrequency %d, Attenuation %d, attenuationMax %d\n", 
                 LEDStatus_i, SynthLockLED_i, Boolean.toString(frontPanelControlled), PSU1Alarm_i, SynthType_i, AttType_i, SerialAlarm_i,
                 SynthFrequency, Attenuation, attenuationMax);
     }
 
     // **************************************************************************
     // * function to be called for each refresh iteration
     // *
     ActionListener timerListener = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent ev) {
 
             connection_ctr = connection_ctr + 1;
             if (connection_ctr >= 999) {
                 connection_ctr = 0;
             }
             // get_data();
             if ((FreqUpdateIcon == 1) | (AttUpdateIcon == 1)) {
                 update_ctr = update_ctr + 1;
                 if (update_ctr > 5) {
                     FreqUpdateIcon = 0;
                     SynthFrequency = SynthFrequencyActual.read_int();
                     AttUpdateIcon = 0;
                     Attenuation = AttenuatorActual.read_int();
                     update_ctr = 0;
                 }
             }
             repaint();
         }
     };
 
     // **************************************************************************
     // * function to setup graphics and paint (empty)
     // *
     @Override
     public void paint(Graphics g) {
         g.setColor(Color.blue);
         g.drawRoundRect(1, 1, 260, 590, 20, 20);
 
         Font smallFont = new Font("Arial", Font.PLAIN, 13);
         Font bigFont = new Font("Arial", Font.PLAIN, 32);
 
         if (comms_active == 1) {
             g.setFont(smallFont);
             g.setColor(Color.black);
             g.drawString("      0.25       1.0        10", 90, 275);
             g.drawString("Osc. Lock Detected", 100, 342);
             g.drawString("PSU Healthy", 100, 391);
 
             g.setFont(bigFont);
             g.setColor(Color.black);
             if (SerialAlarm_i >= 1) {
                 g.drawString("Synth ALM", 35, 96);
             } else {
                 g.drawString(String.valueOf(SynthFrequency), 35, 96);
                 g.drawString("MHz", 140, 96);
                 if (FreqUpdateIcon >= 1) {
                     g.drawString("X", 225, 96);
                 }
             }
 
             g.drawString(String.valueOf(Attenuation * 0.25), 35, 216);
             g.drawString("dB", 140, 216);
             if (AttUpdateIcon >= 1) {
                 g.drawString("X", 225, 216);
             }
 
             // Draw Local/Remote LED and fill if active
             if (!frontPanelControlled) {
                 g.setColor(Color.orange);
             } else {
                 g.setColor(Color.white);
             }
             g.fillRoundRect(LED1_x, LED1_y, LED_dx, LED_dy, LED_r, LED_r);
             g.setColor(Color.orange);
             g.drawRoundRect(LED1_x, LED1_y, LED_dx, LED_dy, LED_r, LED_r);
 
             // Draw & fill Synth Lock LED
             boolean isAlarm = (!isPLO && SynthLockLED_i == 0) || (isPLO && ploOscAlarm);
             g.setColor(isAlarm ? Color.red : Color.green);
             g.fillRoundRect(LED1_x, LED5_y, LED_dx, LED_dy, LED_r, LED_r);
            g.drawRoundRect(LED1_x, LED5_y, LED_dx, LED_dy, LED_r, LED_r);
 
             // Draw PSU1 Alarm LED and fill if alive
             if (PSU1Alarm_i <= 0) {
                 g.setColor(Color.green);
             } else {
                 g.setColor(Color.white);
             }
             g.fillRoundRect(LED1_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r);
             g.setColor(Color.green);
             g.drawRoundRect(LED1_x, LED6_y, LED_dx, LED_dy, LED_r, LED_r);
 
             g.setColor(Color.gray);
             g.setFont(smallFont);
             g.drawString(String.valueOf(connection_ctr), 270, 590);
         } else {
             g.setFont(smallFont);
             g.setColor(Color.black);
             g.drawString("Connection Error:", 50, 80);
             g.drawString("Comms Already In Use", 50, 100);
         }
     }
 
     // Here we ask which component called this method
     @Override
     public void actionPerformed(ActionEvent evt) {
         if (evt.getSource() == LocalActive_ALBtn) {
             CtrlAction.write(0x03); // LR on
             CtrlAction.write(0x04); // LR off
             get_data();
             repaint();
         }
 
         if (evt.getSource() == Refresh_ALBtn) {
             get_data();
             repaint();
         }
 
         if (!frontPanelControlled) {
             if (evt.getSource() == Enter_ALBtn) {
                 SynthFrequencyUpdate.write(SynthFrequency);
                 AttenuatorUpdate.write(Attenuation);
                 CtrlAction.write(0x05); // Enter on
                 CtrlAction.write(0x06); // Enter off
                 FreqUpdateIcon = 0;
                 AttUpdateIcon = 0;
                 update_ctr = 0;
                 get_data();
                 repaint();
             }
             if (evt.getSource() == Enter2_ALBtn) {
                 SynthFrequencyUpdate.write(SynthFrequency);
                 AttenuatorUpdate.write(Attenuation);
                 CtrlAction.write(0x05); // Enter on
                 CtrlAction.write(0x06); // Enter off
                 AttUpdateIcon = 0;
                 FreqUpdateIcon = 0;
                 update_ctr = 0;
                 get_data();
                 repaint();
             }
             if (evt.getSource() == Increase_ALBtn) {
                 SynthFrequency = SynthFrequency + F_INC;
                 if (SynthFrequency >= SYNTH_FREQ_MAX) {
                     SynthFrequency = SYNTH_FREQ_MAX;
                 }
                 FreqUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
             if (evt.getSource() == Decrease_ALBtn) {
                 SynthFrequency = SynthFrequency - F_INC;
                 if (SynthFrequency <= SYNTH_FREQ_MIN) {
                     SynthFrequency = SYNTH_FREQ_MIN;
                 }
                 FreqUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
             if (evt.getSource() == AttInc_ALBtn) {
                 Attenuation = Attenuation + A_INC;
                 if (Attenuation >= attenuationMax) {
                     Attenuation = attenuationMax;
                 }
                 AttUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
             if (evt.getSource() == AttDec_ALBtn) {
                 Attenuation = Attenuation - A_INC;
                 if (Attenuation <= ATT_MIN) {
                     Attenuation = ATT_MIN;
                 }
                 AttUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
             if (evt.getSource() == AttIncx_ALBtn) {
                 Attenuation = Attenuation + A_INCx;
                 if (Attenuation >= attenuationMax) {
                     Attenuation = attenuationMax;
                 }
                 AttUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
             if (evt.getSource() == AttDecx_ALBtn) {
                 Attenuation = Attenuation - A_INCx;
                 if (Attenuation <= ATT_MIN) {
                     Attenuation = ATT_MIN;
                 }
                 AttUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
             if (evt.getSource() == AttIncxx_ALBtn) {
                 Attenuation = Attenuation + A_INCxx;
                 if (Attenuation >= attenuationMax) {
                     Attenuation = attenuationMax;
                 }
                 AttUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
             if (evt.getSource() == AttDecxx_ALBtn) {
                 Attenuation = Attenuation - A_INCxx;
                 if (Attenuation <= ATT_MIN) {
                     Attenuation = ATT_MIN;
                 }
                 AttUpdateIcon = 1;
                 update_ctr = 0;
                 repaint();
             }
         }
     }
 
     private void setIpTextField(TextField ipField, RemoteFactory.Integer ipRpc) {
         // NOTE: Byte order for char[4] read from mbed
         int value = ipRpc.read_int();
         int a = value >>  0 & 0xFF;
         int b = value >>  8 & 0xFF;
         int c = value >> 16 & 0xFF;
         int d = value >> 24 & 0xFF;
         ipField.setText(String.format("%d.%d.%d.%d", a, b, c, d));
     }
 
     protected int ipIntFromTextField(TextField ipField) throws IllegalArgumentException {
         // NOTE: Byte order for char[4] written to mbed
         String text = ipField.getText();
         String[] split = text.split("\\.");
         if (split.length != 4) {
             throw new IllegalArgumentException("Expected 4 numbers in " + text);
         }
         int a = parseInt(text, split[0]);
         int b = parseInt(text, split[1]);
         int c = parseInt(text, split[2]);
         int d = parseInt(text, split[3]);
 
         int value =
                  (a & 0xFF) <<  0;
         value |= (b & 0xFF) <<  8;
         value |= (c & 0xFF) << 16;
         value |= (d & 0xFF) << 24;
 
         return value;
     }
 
     private int parseInt(String whole, String value) {
         try {
             int result = Integer.parseInt(value);
             if (result < 0 || result > 255) {
                 throw new IllegalArgumentException(
                         String.format("Invalid number %s in %s", value, whole));
             }
             return result;
         } catch (NumberFormatException x) {
             throw new IllegalArgumentException(
                     String.format("Invalid number %s in %s", value, whole));
         }
     }
 }
