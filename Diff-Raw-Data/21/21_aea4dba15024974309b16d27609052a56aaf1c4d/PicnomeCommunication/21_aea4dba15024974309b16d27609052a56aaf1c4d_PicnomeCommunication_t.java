 /*
  * Copylight (C) 2009, Shunichi Yamamoto, tkrworks.net
  *
  * This file is part of PicnomeSerial.
  *
  * PicnomeSerial is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option ) any later version.
  *
  * PicnomeSerial is distributed in the hope that it will be useful,
  * but WITHIOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.   See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with PicnomeSerial. if not, see <http:/www.gnu.org/licenses/>.
  *
 * PicnomeCommunication.java,v.1.0.3 2009/08/06
  */
 
 // RXTX
 import gnu.io.*;
 
 // JavaOSC
 import com.illposed.osc.*;
 import com.illposed.osc.utility.*;
 
 // JavaMIDI
 import javax.sound.midi.*;
 import de.humatic.mmj.*;
 
 import javax.swing.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 class PicnomeCommunication
 {
   Vector<String> device_vec = new Vector<String>();
   JComboBox protocol_cb, device_cb, cable_cb;
   JTextField hostaddress_tf, prefix_tf, hostport_tf, listenport_tf, hex_tf;
   JSpinner startcolumn_s, startrow_s;
   JCheckBox adc0_cb, adc1_cb, adc2_cb, adc3_cb, adc4_cb, adc5_cb, adc6_cb;
   JButton hex_b, update_b;
   JProgressBar update_pb;
 
 /* for DEBUG
   JTextField debug_tf;
   JTextField debug2_tf;
 */
   CommPortIdentifier portId;
   SerialPort port;
   InputStream in;
   InputStreamReader inr;
   OutputStream out;
 
   OSCPortIn oscpin;
   OSCPortOut oscpout;
 
   //for Mac OS X
   MidiInput midiin;
   MidiOutput midiout;
 
   PicnomeCommunication()
   {
     this.in = null;
     this.out = null;
     this.initDeviceList();
   }
 
   void initDeviceList()
   {
     String device_name;
     Enumeration e = CommPortIdentifier.getPortIdentifiers();
     while(e.hasMoreElements())
     {
       device_name = ((CommPortIdentifier)e.nextElement()).getName();
       if(System.getProperty("os.name").startsWith("Mac OS X"))
       {
         if(device_name.indexOf("/dev/cu.") != -1)
           this.device_vec.add(device_name);
       }
       else if(System.getProperty("os.name").startsWith("Windows"))
       {
         this.device_vec.add(device_name);
       }
     }
   }
 
   boolean openSerialPort()
   {
     try
     {
       this.portId = CommPortIdentifier.getPortIdentifier((String)this.device_cb.getSelectedItem());
       this.port = (SerialPort)portId.open("PICnomeSerial", 2000);
     }
     catch (NoSuchPortException e)
     {
       e.printStackTrace();
       return false;
     }
     catch (PortInUseException e)
     {
       e.printStackTrace();
       return false;
     }
 
     try
     {
       this.in = this.port.getInputStream();
       this.inr = new InputStreamReader(this.in);
       this.out = this.port.getOutputStream();
       this.initSerialListener();
 
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control"))
       {
         this.initOSCPort();
         this.initOSCListener();
       }
       else
       {
         de.humatic.mmj.MidiSystem.initMidiSystem("PICnome In", "PICnome Out");
         int in_num = de.humatic.mmj.MidiSystem.getNumberOfInputs();
         int out_num = de.humatic.mmj.MidiSystem.getNumberOfOutputs();
         this.midiin = de.humatic.mmj.MidiSystem.openMidiInput(in_num - 1);
         this.midiout = de.humatic.mmj.MidiSystem.openMidiOutput(out_num - 1);
 	
         this.initMidiListener();
       }
     }
     catch(IOException e){}
     return true;
   }
 
   boolean setSerialPort()
   {
     try
     {
       this.port.setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
       this.port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
     }
     catch (UnsupportedCommOperationException e)
     {
       e.printStackTrace();
       return false;
     }
     this.port.setDTR(true);
     this.port.setRTS(false);
     return true;
   }
 
   boolean closeSerialPort()
   {
     try
     {
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control"))
         this.oscpin.stopListening();
       else
       {
         this.midiin.close();
         this.midiout.close();
       }
       this.inr.close();
       this.in.close();
       this.out.flush();
       this.out.close();
       this.port.close();
 
     }
     catch(Exception e)
     {
       e.printStackTrace();
       return false;
     }
     return true;
   }
 
   void initOSCPort()
   {
     byte[] hostaddress = new byte[4];
     String ha_str = this.hostaddress_tf.getText();
 
     int idx = 0, idx2;
     for(int i = 0; i < 3; i++)
     {
       idx2 = ha_str.indexOf(".", idx);
       hostaddress[i] = Byte.parseByte(ha_str.substring(idx, idx2));
       idx = idx2 + 1;
     }
     hostaddress[3] = Byte.parseByte(ha_str.substring(idx, ha_str.length()));
 
     try
     {
       this.oscpin = new OSCPortIn(Integer.parseInt(this.listenport_tf.getText()));
       this.oscpout = new OSCPortOut(InetAddress.getByAddress(hostaddress), Integer.parseInt(this.hostport_tf.getText()));
     }
     catch(UnknownHostException e){}
     catch(SocketException e){}
   }
 
   boolean checkAddressPatternPrefix(OSCMessage message)
   {
     boolean b;
     String address = message.getAddress();
     int location = address.lastIndexOf("/");
     String prefix = address.substring(0, location);
     if(prefix.equals(prefix_tf.getText()))
       b = true;
     else
       b = false;
     return b;
   }
 
   void sendOSCMessageFromHw(String str)
   {
     StringTokenizer st = new StringTokenizer(str);
     Object[] args;
     OSCMessage msg;
     String token = st.nextToken();
     if(token.equals("press"))
     {
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control"))
       {
         args = new Object[3];
 
         int sc = (Integer)startcolumn_s.getValue();
         int sr = (Integer)startrow_s.getValue();
 
         if(((String)this.cable_cb.getSelectedItem()).equals("Left"))
         {
           args[0] = Integer.valueOf(st.nextToken()) + sc; // X
           args[1] = Integer.valueOf(st.nextToken()) + sr; // Y
         }
         else if(((String)this.cable_cb.getSelectedItem()).equals("Right"))
         {
           args[0] = 7 - Integer.valueOf(st.nextToken()) + sc; // X
           args[1] = 7 - Integer.valueOf(st.nextToken()) + sr; // Y
         }
         else if(((String)this.cable_cb.getSelectedItem()).equals("Up"))
         {
           args[1] = 7 - Integer.valueOf(st.nextToken()) + sr; // Y
           args[0] = Integer.valueOf(st.nextToken()) + sc;     // X
         }
         else if(((String)this.cable_cb.getSelectedItem()).equals("Down"))
         {
           args[1] = Integer.valueOf(st.nextToken()) + sr;     // Y
           args[0] = 7 - Integer.valueOf(st.nextToken()) + sc; // X
         }
         args[2] = Integer.valueOf(st.nextToken()); // State
 
 
         msg = new OSCMessage(this.prefix_tf.getText() + "/press", args);
         try
         {
           this.oscpout.send(msg);
         }
         catch(IOException e){}
       }
       else // for MIDI
       {
         int notex = Integer.valueOf(st.nextToken());
         int notey = Integer.valueOf(st.nextToken());
         int state = Integer.valueOf(st.nextToken());
         int note = notex + (notey * 8);
 
         if(state == 1)
           this.midiout.sendMidi(new byte[]{(byte)ShortMessage.NOTE_ON, (byte)note, 127});
         else
           this.midiout.sendMidi(new byte[]{(byte)ShortMessage.NOTE_ON, (byte)note, 0});
       }
     }
     else if(token.equals("input"))
     {
       args = new Object[2];
 
       args[0] = Integer.valueOf(st.nextToken()); // Pin
       args[1] = Integer.valueOf(st.nextToken()); // State
 
       msg = new OSCMessage(this.prefix_tf.getText() + "/input", args);
       try
       {
         this.oscpout.send(msg);
       }
       catch(IOException e){}
     }
     else if(token.equals("adc"))
     {
       args = new Object[2];
 
       args[0] = Integer.valueOf(st.nextToken()); // Pin
       args[1] = Float.valueOf(st.nextToken());   // Value
 
       msg = new OSCMessage(this.prefix_tf.getText() + "/adc", args);
       try
       {
         this.oscpout.send(msg);
       }
       catch(IOException e){}
     }
     else if(token.equals("report"))
     {
       int v1,v2;
 
       v1 = Integer.valueOf(st.nextToken());
       v2 = Integer.valueOf(st.nextToken());
 
       if(v2 == 1)
       {
         this.hex_tf.setEnabled(false);
         this.hex_b.setEnabled(false);
         this.update_b.setEnabled(false);
       }
     }
   }
 
   public void enableMsgLed()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           if(!checkAddressPatternPrefix(message))
             return ;
 
           Object[] args = message.getArguments();
           int sc = (Integer)startcolumn_s.getValue();
           int sr = (Integer)startrow_s.getValue();
 
           if(((String)cable_cb.getSelectedItem()).equals("Left"))
           {
             sc = (Integer)args[0] - sc;
             sr = (Integer)args[1] - sr;
           }
           else if(((String)cable_cb.getSelectedItem()).equals("Right"))
           {
             sc = 7 - (Integer)args[0] + sc;
             sr = 7 - (Integer)args[1] + sr;
           }
           else if(((String)cable_cb.getSelectedItem()).equals("Up"))
           {
             int sc1 = 7 - (Integer)args[1] + sr;
             int sr1 = (Integer)args[0] - sc;
             sc = sc1;
             sr = sr1;
           }
           else if(((String)cable_cb.getSelectedItem()).equals("Down"))
           {
             int sc1 = (Integer)args[1] - sr;
             int sr1 = 7 - (Integer)args[0] + sc;
             sc = sc1;
             sr = sr1;
           }
 
           if(sc < 0) sc = 0;
           if(sr < 0) sr = 0;
 
           try
           {
             String str =new String("led " + sc + " " + sr + " " + (Integer)args[2] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/led", listener);
   }
 
   public void enableMidiLed()
   {
     MidiListener listener = new MidiListener()
       {
         public void midiInput(byte[] data)
         {
           if((256 + data[0]) == 144 || (256 + data[0]) == 128)// NOTE_ON -> 144, NOTE_OFF -> 128
           {
             int sc = (Integer)startcolumn_s.getValue();
             int sr = (Integer)startrow_s.getValue();
             sc = (data[1] % 8) - sc;
             sr = (data[1] / 8) - sr;
             if(sc < 0) sc = 0;
             if(sr < 0) sr = 0;
 
             try
             {
               String str;
               if(data[2] == 0)
                 str =new String("led " + sc + " " + sr + " " + 0 + (char)0x0D);
               else
                 str =new String("led " + sc + " " + sr + " " + 1 + (char)0x0D);
               out.write(str.getBytes());
             }
             catch(IOException e){}
           }
         }
       };
     this.midiin.addMidiListener(listener);
   }
 
 
   public void enableMsgLedCol()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           if(!checkAddressPatternPrefix(message))
             return ;
 
           Object[] args = message.getArguments();
           int sc = 0, sr = 0;
 
           if(((String)cable_cb.getSelectedItem()).equals("Left"))
             sc = (Integer)args[0] - (Integer)startcolumn_s.getValue();
           else if(((String)cable_cb.getSelectedItem()).equals("Right"))
             sc = 7 - (Integer)args[0] + (Integer)startcolumn_s.getValue();
           else if(((String)cable_cb.getSelectedItem()).equals("Up"))
             sc = (Integer)args[0] - (Integer)startcolumn_s.getValue();
           else if(((String)cable_cb.getSelectedItem()).equals("Down"))
             sc = 7 - (Integer)args[0] + (Integer)startcolumn_s.getValue();
 
           if(sc < 0) sc = 0;
 
           int shift = (Integer)startrow_s.getValue() % 8;
 
           if(((String)cable_cb.getSelectedItem()).equals("Left"))
             sr = (short)(((Integer)args[1]).shortValue() >> shift);
           else if(((String)cable_cb.getSelectedItem()).equals("Right"))
           {
             short sr0 = ((Integer)args[1]).shortValue();
             short sr1 = 0;
             for(int i = 0; i < 8; i++)
               if((sr0 & (0x01 << i)) == (0x01 << i))
                 sr1 |= (0x01 << (7 - i));
             sr = (short)(sr1 << shift);
           }
           else if(((String)cable_cb.getSelectedItem()).equals("Up"))
           {
             short sr0 = ((Integer)args[1]).shortValue();
             short sr1 = 0;
             for(int i = 0; i < 8; i++)
               if((sr0 & (0x01 << i)) == (0x01 << i))
                 sr1 |= (0x01 << (7 - i));
             sr = (short)(sr1 << shift);
           }
           else if(((String)cable_cb.getSelectedItem()).equals("Down"))
             sr = (short)(((Integer)args[1]).shortValue() >> shift);
 
           try
           {
             String str;
             if(((String)cable_cb.getSelectedItem()).equals("Left") || ((String)cable_cb.getSelectedItem()).equals("Right"))
               str =new String("led_col " + sc + " " + sr + (char)0x0D);
             else
               str =new String("led_row " + sc + " " + sr + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/led_col", listener);
   }
 
   public void enableMsgLedRow()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           if(!checkAddressPatternPrefix(message))
             return ;
 
           Object[] args = message.getArguments();
           int sc = 0, sr = 0;
 
           if(((String)cable_cb.getSelectedItem()).equals("Left"))
             sr = (Integer)args[0] - (Integer)startrow_s.getValue();
           else if(((String)cable_cb.getSelectedItem()).equals("Right"))
             sr = 7 - (Integer)args[0] + (Integer)startrow_s.getValue();
           else if(((String)cable_cb.getSelectedItem()).equals("Up"))
             sr = 7 - (Integer)args[0] + (Integer)startrow_s.getValue();
           else if(((String)cable_cb.getSelectedItem()).equals("Down"))
             sr = (Integer)args[0] - (Integer)startrow_s.getValue();
 
           if(sr < 0) sr = 0;
 
           int shift = (Integer)startcolumn_s.getValue() % 8;
 
           if(((String)cable_cb.getSelectedItem()).equals("Left"))
             sc = (short)(((Integer)args[1]).shortValue() >> shift);
           else if(((String)cable_cb.getSelectedItem()).equals("Right"))
           {
             short sc0 = ((Integer)args[1]).shortValue();
             short sc1 = 0;
             for(int i = 0; i < 8; i++)
               if((sc0 & (0x01 << i)) == (0x01 << i))
                 sc1 |= (0x01 << (7 - i));
             sc = (short)(sc1 << shift);
           }
           else if(((String)cable_cb.getSelectedItem()).equals("Up"))
             sc = (short)(((Integer)args[1]).shortValue() >> shift);
           else if(((String)cable_cb.getSelectedItem()).equals("Down"))
           {
             short sc0 = ((Integer)args[1]).shortValue();
             short sc1 = 0;
             for(int i = 0; i < 8; i++)
               if((sc0 & (0x01 << i)) == (0x01 << i))
                 sc1 |= (0x01 << (7 - i));
             sc = (short)(sc1 << shift);
           }
 
           try
           {
             String str;
             if(((String)cable_cb.getSelectedItem()).equals("Left") || ((String)cable_cb.getSelectedItem()).equals("Right"))
               str =new String("led_row " + sr + " " + sc + (char)0x0D);
             else
               str =new String("led_col " + sr + " " + sc + (char)0x0D);
 
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/led_row", listener);
   }
 
   public void enableMsgLedFrame()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           if(!checkAddressPatternPrefix(message))
             return ;
 
           Object[] args = message.getArguments();
           int sc = 0, sr = 0;
 
           int shift = (Integer)startcolumn_s.getValue() % 8;
 
           for(int i = 0; i < 8; i++)
           {
             if(((String)cable_cb.getSelectedItem()).equals("Left"))
               sr = i - (Integer)startrow_s.getValue();
             else if(((String)cable_cb.getSelectedItem()).equals("Right"))
               sr = 7 - i + (Integer)startrow_s.getValue();
             else if(((String)cable_cb.getSelectedItem()).equals("Up"))
               sr = 7 - i + (Integer)startrow_s.getValue();
             else if(((String)cable_cb.getSelectedItem()).equals("Down"))
               sr = i - (Integer)startrow_s.getValue();
 
             if(i < (Integer)startrow_s.getValue()) return;
 
             if(((String)cable_cb.getSelectedItem()).equals("Left"))
               sc = (short)(((Integer)args[i]).shortValue() >> shift);
             else if(((String)cable_cb.getSelectedItem()).equals("Right"))
             {
               short sc0 = ((Integer)args[i]).shortValue();
               short sc1 = 0;
               for(int j = 0; j < 8; j++)
                 if((sc0 & (0x01 << j)) == (0x01 << j))
                   sc1 |= (0x01 << (7 - j));
               sc = (short)(sc1 << shift);
             }
             else if(((String)cable_cb.getSelectedItem()).equals("Up"))
               sc = (short)(((Integer)args[i]).shortValue() >> shift);
             else if(((String)cable_cb.getSelectedItem()).equals("Down"))
             {
               short sc0 = ((Integer)args[i]).shortValue();
               short sc1 = 0;
               for(int j = 0; j < 8; j++)
                 if((sc0 & (0x01 << j)) == (0x01 << j))
                   sc1 |= (0x01 << (7 - j));
               sc = (short)(sc1 << shift);
             }
 
             try
             {
               String str;
               if(((String)cable_cb.getSelectedItem()).equals("Left") || ((String)cable_cb.getSelectedItem()).equals("Right"))
                 str =new String("led_row " + sr + " " + sc + (char)0x0D);
               else
                 str =new String("led_col " + sr + " " + sc + (char)0x0D);
 
               //debug debug_tf.setText(str);
               out.write(str.getBytes());
             }
             catch(IOException e){}
           }
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/frame", listener);
   }
 
   public void enableMsgClear()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           if(!checkAddressPatternPrefix(message))
             return ;
 
           Object[] args = message.getArguments();
           for(int i = 0; i < 8; i++)
           {
             short state;
             if(((Integer)args[0]).intValue() == 0)
               state = (short)0x00;
             else
               state = (short)0xFF;
 
             try
             {
               String str =new String("led_row " + i + " " + state + (char)0x0D);
               //debug debug_tf.setText(str);
               out.write(str.getBytes());
             }
             catch(IOException e){}
           }
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/clear", listener);
   }
 
   public void enableMsgAdcEnable()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           if(!checkAddressPatternPrefix(message))
             return ;
 
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("adc_enable " + (Integer)args[0] + " " + (Integer)args[1] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/adc_enable", listener);
   }
 
   public void enableMsgPwm()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("pwm " + (Integer)args[0] + " " + (Integer)args[1] + " " + (Float)args[2] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/pwm", listener);
   }
 
 /*sy
   public void enableMsgOutput()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("output " + (Integer)args[0] + " " + (Integer)args[1] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/output", listener);
   }
 */
 
   public void enableMsgPrefix()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
           prefix_tf.setText((String)args[0]);
           PicnomeCommunication.this.enableMsgLed();
           PicnomeCommunication.this.enableMsgLedCol();
           PicnomeCommunication.this.enableMsgLedRow();
           PicnomeCommunication.this.enableMsgLedFrame();
           PicnomeCommunication.this.enableMsgClear();
           PicnomeCommunication.this.enableMsgAdcEnable();
           PicnomeCommunication.this.enableMsgPwm();
         }
       };
     this.oscpin.addListener("/sys/prefix", listener);
   }
 
   public void enableMsgIntensity()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("intensity " + (Integer)args[0] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener("/sys/intensity", listener);
   }
 
   public void enableMsgTest()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("test " + (Integer)args[0] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener("/sys/test", listener);
   }
 
   public void enableMsgShutdown()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("shutdown " + (Integer)args[0] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener("/sys/shutdown", listener);
   }
 
   public void enableMsgReport()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("report " + (Integer)args[0] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener("/sys/report", listener);
   }
 
 
   public void enableMsgOffset()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
           startcolumn_s.setValue((Integer)args[0]);
           startrow_s.setValue((Integer)args[1]);
         }
       };
     this.oscpin.addListener("/sys/offset", listener);
   }
 
   public void enableMsgCable()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
           cable_cb.setSelectedItem(((String)args[0]));
         }
       };
     this.oscpin.addListener("/sys/cable", listener);
   }
 
   public void initOSCListener()
   {
     this.enableMsgLed();
     this.enableMsgLedCol();
     this.enableMsgLedRow();
     this.enableMsgLedFrame();
     this.enableMsgClear();
     this.enableMsgAdcEnable();
     this.enableMsgPwm();
     //sy this.enableMsgOutput();
     this.enableMsgPrefix();
     this.enableMsgIntensity();
     this.enableMsgTest();
     this.enableMsgShutdown();
     this.enableMsgReport();
     this.enableMsgOffset();
     this.enableMsgCable();
     this.oscpin.startListening();
   }
 
   public void initMidiListener()
   {
     this.enableMidiLed();
   }
 
   class SerialPortListener implements SerialPortEventListener
   {
     public void serialEvent(SerialPortEvent event)
     {
       if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
       {
         int buffer = 0;
         try
         {
           StringBuffer sb = new StringBuffer();
           while((buffer = inr.read()) != -1)
           {
             if(buffer != 0x0A || buffer != 0x0D)
               sb.append((char)buffer);
             if(buffer == 0x0A || buffer == 0x0D)
               break;
           }
           //sy debug2_tf.setText(sb.toString());
           sendOSCMessageFromHw(sb.toString());
         }
         catch(IOException e){}
       }
     }
   }
 
   void initSerialListener()
   {
     try
     {
       this.port.addEventListener(new SerialPortListener());
       this.port.notifyOnDataAvailable(true);
     }
     catch (TooManyListenersException e){}
   }
 }
