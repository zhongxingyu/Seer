 /*
  * Copylight (C) 2009, Shunichi Yamamoto, tkrworks.net
  *
  * This file is part of PICnomeSerial.
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
  * PicnomeCommunication.java,v.1.1.4 2009/09/09
  */
 
 // RXTX
 import gnu.io.*;
 
 // JavaOSC
 import com.illposed.osc.*;
 import com.illposed.osc.utility.*;
 
 // JavaMIDI
 import promidi.*;
 
 import javax.swing.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 public class PicnomeCommunication
 {
   Vector<String> device_list = new Vector<String>();
   JButton openclose_b;
   JComboBox protocol_cb, device_cb, cable_cb;
   JTextField hostaddress_tf, prefix_tf, hostport_tf, listenport_tf, hex_tf;
   JSpinner startcolumn_s, startrow_s;
   JCheckBox[] adc_ck = new JCheckBox[7];
   JButton hex_b, update_b;
   JProgressBar update_pb;
 
 /* for DEBUG
   JTextField debug_tf;
   JTextField debug2_tf;
 */
   CommPortIdentifier[] portId = new CommPortIdentifier[2];
   SerialPort[] port = new SerialPort[2];
   InputStream[] in = new InputStream[2];
   InputStreamReader[] inr = new InputStreamReader[2];
   OutputStream[] out = new OutputStream[2];
 
   OSCPortIn oscpin;
   OSCPortOut oscpout;
 
   //for Mac OS X
   MidiIO midiio;
   MidiOut midiout;
   int midi_in_port, midi_out_port, midi_pgm_number;
 
   String[] device = new String[2];
   String[] connect_state = new String[2];
   String[] cable_orientation = new String[2];
   String[] address_pattern_prefix = new String[2];
   int[] starting_column = new int[2];
   int[] starting_row = new int[2];
   boolean[][] adc_enable = new boolean[2][7];
 
   PicnomeCommunication()
   {
     this.in[0] = null;
     this.out[0] = null;
     this.in[1] = null;
     this.out[1] = null;
     this.initDeviceList();
 
     this.connect_state[0] = "Open";
     this.connect_state[1] = "Open";
     this.cable_orientation[0] = "Left";
     this.cable_orientation[1] = "Left";
     this.address_pattern_prefix[0] = "/test";
     this.address_pattern_prefix[1] = "/test";
     this.starting_column[0] = 0;
     this.starting_column[1] = 0;
     this.starting_row[0] = 0;
     this.starting_row[1] = 0;
     for(int i = 0; i < 7; i++)
     {
       this.adc_enable[0][i] = false;
       this.adc_enable[1][i] = false;
     }
   }
 
   ArrayList<String> getUsbInfo(String name)
   {
     String id = "none";
     String iousbdevices = new String();
 
     try
     {
       ProcessBuilder pb = new ProcessBuilder("ioreg", "-w", "0", "-S", "-p", "IOUSB", "-n", name, "-r");
       Process p = pb.start();
       InputStream is = p.getInputStream();
 
       int c;
       while((c = is.read()) != -1)
         iousbdevices += (new Character((char)c)).toString();
       is.close();
     }catch(IOException e){}
 
     ArrayList<String> sfx = new ArrayList<String>();
     ArrayList<String> vid = new ArrayList<String>();
     ArrayList<String> pid = new ArrayList<String>();
     while(iousbdevices.indexOf(name) != -1)
     {
       int pos_start = iousbdevices.indexOf(name);
       int pos_end = iousbdevices.indexOf(" }");
       String iousbdevice = iousbdevices.substring(pos_start, pos_end);
 
       if(iousbdevice.indexOf(name + "@") != -1)
       {
         pos_start = iousbdevice.indexOf(name + "@") + name.length() + 1;
         pos_end = pos_start +4;
         if(pos_start != -1)
           id = iousbdevice.substring(pos_start, pos_end);
         sfx.add(id);
       }
 
       if((pos_start = iousbdevice.indexOf("idVendor")) != -1)
       {
         pos_end = iousbdevice.length();
       
         iousbdevice = iousbdevice.substring(pos_start, pos_end);
         pos_end = iousbdevice.indexOf("\n");
         id = iousbdevice.substring(iousbdevice.indexOf(" = ") + 3, pos_end);
         vid.add(id);
       }
 
       iousbdevice = iousbdevices.substring(iousbdevices.indexOf(name), iousbdevices.indexOf(" }"));
       if((pos_start = iousbdevice.indexOf("idProduct")) != -1)
       {
         pos_end = iousbdevice.length();
       
         iousbdevice = iousbdevice.substring(pos_start, pos_end);
         pos_end = iousbdevice.indexOf("\n");
         id = iousbdevice.substring(iousbdevice.indexOf(" = ") + 3, pos_end);
         pid.add(id);
       }
       iousbdevices = iousbdevices.substring(iousbdevices.indexOf(" }") + 2, iousbdevices.length());
     }
 
     return sfx;
   }
 
   void initDeviceList()
   {
     int dev_num = 0;
     String device_name;
     ArrayList<String> suffix0 = this.getUsbInfo("IOUSBDevice");
     ArrayList<String> suffix1 = this.getUsbInfo("PICnome");
     Enumeration e = CommPortIdentifier.getPortIdentifiers();
     while(e.hasMoreElements())
     {
       device_name = ((CommPortIdentifier)e.nextElement()).getName();
 
       if(device_name.indexOf("/dev/cu.usbmodem") != -1)
       {
         for(int i = 0; i < suffix0.size(); i++)
         {
           if(device_name.indexOf(suffix0.get(i)) != -1)
           {
             this.device[dev_num] = "tkrworks-PICnome-" + suffix0.get(i);
             dev_num++;
             this.device_list.add("tkrworks-PICnome-" + suffix0.get(i));
           }
         }
         for(int i = 0; i < suffix1.size(); i++)
         {
           if(device_name.indexOf(suffix1.get(i)) != -1)
           {
             this.device[dev_num] = "tkrworks-PICnome-" + suffix1.get(i);
             dev_num++;
             this.device_list.add("tkrworks-PICnome-" + suffix1.get(i));
           }
         }
       }
     }
   }
 
   void changeDeviceSettings(int index)
   {
     this.connect_state[1 - index] = this.openclose_b.getText();
 
     this.openclose_b.setText(this.connect_state[index]);
     this.cable_cb.setSelectedItem(this.cable_orientation[index]);
     this.prefix_tf.setText(this.address_pattern_prefix[index]);
     this.startcolumn_s.setValue(this.starting_column[index]);
     this.startrow_s.setValue(this.starting_row[index]);
 
     for(int i = 0; i < 7; i++)
     {
       this.adc_ck[i].setSelected(this.adc_enable[index][i]);
     }
   }
 
   boolean openSerialPort(int index)
   {
     try
     {
      this.portId[index] = CommPortIdentifier.getPortIdentifier((String)this.device_cb.getSelectedItem());
       this.port[index] = (SerialPort)portId[index].open("PICnomeSerial", 2000);
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
       this.in[index] = this.port[index].getInputStream();
       this.inr[index] = new InputStreamReader(this.in[index]);
       this.out[index] = this.port[index].getOutputStream();
       this.initSerialListener(index);
 
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control"))
       {
         this.initOSCPort();
         this.initOSCListener("all");
       }
       else//for MIDI
        //sy init();
         this.initMIDIPort();
     }
     catch(IOException e){}
     return true;
   }
 
   boolean setSerialPort(int index)
   {
     try
     {
       this.port[index].setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
       this.port[index].setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
     }
     catch (UnsupportedCommOperationException e)
     {
       e.printStackTrace();
       return false;
     }
     this.port[index].setDTR(true);
     this.port[index].setRTS(false);
     return true;
   }
 
   boolean closeSerialPort(int index)
   {
     try
     {
       if(((String)this.protocol_cb.getSelectedItem()).equals("MIDI"))
       {
         //sy this.midiio.closeInput(this.midi_in_port);
       }
 
       this.inr[index].close();
       this.in[index].close();
       this.out[index].flush();
       this.out[index].close();
       this.port[index].close();
 
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
 
   //sy MIDI Setup
   public void initMIDIPort()
   {
     midiio = MidiIO.getInstance();
     midiio.printDevices();
     int in_num = midiio.numberOfInputDevices();
     for(int i = 0; i < in_num; i++)
     {
       String in_str = midiio.getInputDeviceName(i);
       if(in_str.indexOf("IAC Bus 1") != -1)
       {
         this.midi_in_port = i;
         midiio.plug(this, "enableMIDILed", i, 0);
         System.out.println("port : " + i + " " + in_str);
       }
     }
     int out_num = midiio.numberOfOutputDevices();
     for(int i = 0; i < out_num; i++)
     {
       String out_str = midiio.getOutputDeviceName(i);
       if(out_str.indexOf("IAC Bus 2") != -1)
       {
         this.midi_out_port = i;
         this.midiout = this.midiio.getMidiOut(0, i);
         System.out.println("port : " + i + " " + out_str);
       }
     }
   }
 
   boolean checkAddressPatternPrefix(OSCMessage message, int index)
   {
     boolean b;
     String address = message.getAddress();
     int location = address.lastIndexOf("/");
     String prefix = address.substring(0, location);
 
     if(this.address_pattern_prefix[index] != null && prefix.equals(this.address_pattern_prefix[index]))
       b = true;
     else
       b = false;
 
     return b;
   }
 
   void sendOSCMessageFromHw(int index, String str)
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
 
         int sc = this.starting_column[index];
         int sr = this.starting_row[index];
 
         if(this.cable_orientation[index].equals("Left"))
         {
           args[0] = Integer.valueOf(st.nextToken()) + sc; // X
           args[1] = Integer.valueOf(st.nextToken()) + sr; // Y
         }
         else if(this.cable_orientation[index].equals("Right"))
         {
           args[0] = 7 - Integer.valueOf(st.nextToken()) + sc; // X
           args[1] = 7 - Integer.valueOf(st.nextToken()) + sr; // Y
         }
         else if(this.cable_orientation[index].equals("Up"))
         {
           args[1] = 7 - Integer.valueOf(st.nextToken()) + sr; // Y
           args[0] = Integer.valueOf(st.nextToken()) + sc;     // X
         }
         else if(this.cable_orientation[index].equals("Down"))
         {
           args[1] = Integer.valueOf(st.nextToken()) + sr;     // Y
           args[0] = 7 - Integer.valueOf(st.nextToken()) + sc; // X
         }
         args[2] = Integer.valueOf(st.nextToken()); // State
 
         msg = new OSCMessage(this.address_pattern_prefix[index] + "/press", args);
         try
         {
           this.oscpout.send(msg);
         }
         catch(IOException e){}
       }
       else// for MIDI
       {
         int notex = Integer.valueOf(st.nextToken());
         int notey = Integer.valueOf(st.nextToken());
         int state = Integer.valueOf(st.nextToken());
         int note_number = notex + (notey * 8);
 
         System.out.println(notex + " " + notey + " " + state);
 
         Note note;
         if(state == 1)
         {
           note = new Note(note_number, 127, 30000);
         }
         else
         {
           note = new Note(note_number, 0, 1);
         }
         this.midiout.sendNote(note);
       }
     }
     else if(token.equals("input"))
     {
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control"))
       {
         args = new Object[2];
 
         args[0] = Integer.valueOf(st.nextToken()); // Pin
         args[1] = Integer.valueOf(st.nextToken()); // State
 
         msg = new OSCMessage(this.address_pattern_prefix[index] + "/input", args);
         try
         {
           this.oscpout.send(msg);
         }
         catch(IOException e){}
       }
       else//for MIDI
       {
         int pin = Integer.valueOf(st.nextToken()); // Pin
         int state = Integer.valueOf(st.nextToken()); // State
 
         if(pin == 0 && state == 1)
           this.midi_pgm_number++;
         else if(pin == 1 && state == 1)
           this.midi_pgm_number--;
 
         if(this.midi_pgm_number > 127)
           this.midi_pgm_number = 127;
         else if(this.midi_pgm_number < 0)
           this.midi_pgm_number = 0;
 
         this.midiout.sendProgramChange(new ProgramChange(this.midi_pgm_number));
       }
     }
     else if(token.equals("adc"))
     {
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control"))
       {
         args = new Object[2];
 
         args[0] = Integer.valueOf(st.nextToken()); // Pin
         args[1] = Float.valueOf(st.nextToken());   // Value
 
         msg = new OSCMessage(this.address_pattern_prefix[index] + "/adc", args);
         try
         {
           this.oscpout.send(msg);
         }
         catch(IOException e){}
       }
       else//for MIDI
       {
         int ctrl_number = Integer.valueOf(st.nextToken()); // Pin
         int ctrl_value = (int)(127.0 * Float.valueOf(st.nextToken()));   // Value
         Controller cc = new Controller(ctrl_number, ctrl_value);
 
         this.midiout.sendController(new Controller(ctrl_number, ctrl_value));
       }
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
           Object[] args = message.getArguments();
 
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           for(int i = 0; i < 2; i++)
           {
             if(!checkAddressPatternPrefix(message, i))
               continue;
 
             sc[i] = starting_column[i];
             sr[i] = starting_row[i];
 
             if(cable_orientation[i].equals("Left"))
             {
               sc[i] = (Integer)args[0] - sc[i];
               sr[i] = (Integer)args[1] - sr[i];
             }
             else if(cable_orientation[i].equals("Right"))
             {
               sc[i] = 7 - (Integer)args[0] + sc[i];
               sr[i] = 7 - (Integer)args[1] + sr[i];
             }
             else if(cable_orientation[i].equals("Up"))
             {
               int sc1 = 7 - (Integer)args[1] + sr[i];
               int sr1 = (Integer)args[0] - sc[i];
               sc[i] = sc1;
               sr[i] = sr1;
             }
             else if(cable_orientation[i].equals("Down"))
             {
               int sc1 = (Integer)args[1] - sr[i];
               int sr1 = 7 - (Integer)args[0] + sc[i];
               sc[i] = sc1;
               sr[i] = sr1;
             }
             
             if(sc[i] < 0 || sr[i] < 0) continue ;
             
             try
             {
               String str =new String("led " + sc[i] + " " + sr[i] + " " + (Integer)args[2] + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[i] != null && portId[i].isCurrentlyOwned())
                 out[i].write(str.getBytes());
             }
             catch(IOException e){}
           }//end for
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/led", listener);
   }
 
   public void enableMIDILed(Note note)
   {
     int pitch = note.getPitch();
     int velocity = note.getVelocity();
 
     if(pitch > 64) return;
 
     int sc = (pitch % 8);
     int sr = (pitch / 8);
 
     try
     {
       String str;
       if(velocity == 0)
         str =new String("led " + sc + " " + sr + " " + 0 + (char)0x0D);
       else
         str =new String("led " + sc + " " + sr + " " + 1 + (char)0x0D);
       this.out[0].write(str.getBytes());
     }
     catch(IOException e){}
   }
 
   public void enableMsgLedCol()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           for(int j = 0; j < 2; j++)
           {
             if(!checkAddressPatternPrefix(message, j))
               continue;
 
             if(cable_orientation[j].equals("Left"))
               sc[j] = (Integer)args[0] - starting_column[j];
             else if(cable_orientation[j].equals("Right"))
               sc[j] = 7 - (Integer)args[0] + starting_column[j];
             else if(cable_orientation[j].equals("Up"))
               sc[j] = (Integer)args[0] - starting_column[j];
             else if(cable_orientation[j].equals("Down"))
               sc [j]= 7 - (Integer)args[0] + starting_column[j];
 
             if(sc[j] < 0) continue ;
 
             int shift = starting_row[j] % 16;
 
             if(cable_orientation[j].equals("Left"))
               sr[j] = (short)(((Integer)args[1]).shortValue() >> shift);
             else if(cable_orientation[j].equals("Right"))
             {
               short sr0 = ((Integer)args[1]).shortValue();
               short sr1 = 0;
               for(int i = 0; i < 8; i++)
                 if((sr0 & (0x01 << i)) == (0x01 << i))
                   sr1 |= (0x01 << (7 - i));
               sr[j] = (short)(sr1 << shift);
             }
             else if(cable_orientation[j].equals("Up"))
             {
               short sr0 = ((Integer)args[1]).shortValue();
               short sr1 = 0;
               for(int i = 0; i < 8; i++)
                 if((sr0 & (0x01 << i)) == (0x01 << i))
                   sr1 |= (0x01 << (7 - i));
               sr[j] = (short)(sr1 << shift);
             }
             else if(cable_orientation[j].equals("Down"))
               sr[j] = (short)(((Integer)args[1]).shortValue() >> shift);
 
             try
             {
               String str;
               if(cable_orientation[j].equals("Left") || cable_orientation[j].equals("Right"))
                 str =new String("led_col " + sc[j] + " " + sr[j] + (char)0x0D);
               else
                 str =new String("led_row " + sc[j] + " " + sr[j] + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[j] != null && portId[j].isCurrentlyOwned())
                 out[j].write(str.getBytes());
             }
             catch(IOException e){}
           }//end for
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
           Object[] args = message.getArguments();
 
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           for(int j = 0; j < 2; j++)
           {
             if(!checkAddressPatternPrefix(message, j))
               continue;
 
             if(cable_orientation[j].equals("Left"))
               sr[j] = (Integer)args[0] - starting_row[j];
             else if(cable_orientation[j].equals("Right"))
               sr[j] = 7 - (Integer)args[0] + starting_row[j];
             else if(cable_orientation[j].equals("Up"))
               sr[j] = 7 - (Integer)args[0] + starting_row[j];
             else if(cable_orientation[j].equals("Down"))
               sr[j] = (Integer)args[0] - starting_row[j];
             
             if(sr[j] < 0) continue;
             
             int shift = starting_column[j] % 16;
             
             if(cable_orientation[j].equals("Left"))
               sc[j] = (short)(((Integer)args[1]).shortValue() >> shift);
             else if(cable_orientation[j].equals("Right"))
             {
               short sc0 = ((Integer)args[1]).shortValue();
               short sc1 = 0;
               for(int i = 0; i < 8; i++)
                 if((sc0 & (0x01 << i)) == (0x01 << i))
                   sc1 |= (0x01 << (7 - i));
               sc[j] = (short)(sc1 << shift);
             }
             else if(cable_orientation[j].equals("Up"))
               sc[j] = (short)(((Integer)args[1]).shortValue() >> shift);
             else if(cable_orientation[j].equals("Down"))
             {
               short sc0 = ((Integer)args[1]).shortValue();
               short sc1 = 0;
               for(int i = 0; i < 8; i++)
                 if((sc0 & (0x01 << i)) == (0x01 << i))
                   sc1 |= (0x01 << (7 - i));
               sc[j] = (short)(sc1 << shift);
             }
 
             try
             {
               String str;
               if(cable_orientation[j].equals("Left") || cable_orientation[j].equals("Right"))
                 str =new String("led_row " + sr[j] + " " + sc[j] + (char)0x0D);
               else
                 str =new String("led_col " + sr[j] + " " + sc[j] + (char)0x0D);
 
               //debug debug_tf.setText(str);
               if(portId[j] != null && portId[j].isCurrentlyOwned())
                 out[j].write(str.getBytes());
             }
             catch(IOException e){}
           }//end for
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
           Object[] args = message.getArguments();
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           for(int k = 0; k < 2; k++)
           {
             if(!checkAddressPatternPrefix(message, k))
               continue;
 
             int shift = starting_column[k] % 16;
 
             for(int i = 0; i < 8; i++)
             {
               if(cable_orientation[k].equals("Left"))
                 sr[k] = i - starting_row[k];
               else if(cable_orientation[k].equals("Right"))
                 sr[k] = 7 - i + starting_row[k];
               else if(cable_orientation[k].equals("Up"))
                 sr[k] = 7 - i + starting_row[k];
               else if(cable_orientation[k].equals("Down"))
                 sr[k] = i - starting_row[k];
 
               if(i < starting_row[k]) continue;
 
               if(cable_orientation[k].equals("Left"))
                 sc[k] = (short)(((Integer)args[i]).shortValue() >> shift);
               else if(cable_orientation[k].equals("Right"))
               {
                 short sc0 = ((Integer)args[i]).shortValue();
                 short sc1 = 0;
                 for(int j = 0; j < 8; j++)
                   if((sc0 & (0x01 << j)) == (0x01 << j))
                     sc1 |= (0x01 << (7 - j));
                 sc[k] = (short)(sc1 << shift);
               }
               else if(cable_orientation[k].equals("Up"))
                 sc[k] = (short)(((Integer)args[i]).shortValue() >> shift);
               else if(cable_orientation[k].equals("Down"))
               {
                 short sc0 = ((Integer)args[i]).shortValue();
                 short sc1 = 0;
                 for(int j = 0; j < 8; j++)
                   if((sc0 & (0x01 << j)) == (0x01 << j))
                     sc1 |= (0x01 << (7 - j));
                 sc[k] = (short)(sc1 << shift);
               }
               
               try
               {
                 String str;
                 if(cable_orientation[k].equals("Left") || cable_orientation[k].equals("Right"))
                   str =new String("led_row " + sr[k] + " " + sc[k] + (char)0x0D);
                 else
                   str =new String("led_col " + sr[k] + " " + sc[k] + (char)0x0D);
                 
                 //debug debug_tf.setText(str);
                 if(portId[k] != null && portId[k].isCurrentlyOwned())
                   out[k].write(str.getBytes());
               }
               catch(IOException e){}
             }//end for i
           }//end for j
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
           Object[] args = message.getArguments();
           for(int j = 0; j < 2; j++)
           {
             if(!checkAddressPatternPrefix(message, j))
               continue;
 
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
                 if(portId[j] != null && portId[j].isCurrentlyOwned())
                   out[j].write(str.getBytes());
               }
               catch(IOException e){}
             }
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
 /*
           if(!checkAddressPatternPrefix(message))
             return ;
 */
 
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("adc_enable " + (Integer)args[0] + " " + (Integer)args[1] + (char)0x0D);
             //debug debug_tf.setText(str);
             if(portId[0] != null && portId[0].isCurrentlyOwned())
               out[0].write(str.getBytes());
             if(portId[1] != null && portId[1].isCurrentlyOwned())
               out[1].write(str.getBytes());
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
             if(portId[0] != null && portId[0].isCurrentlyOwned())
               out[0].write(str.getBytes());
             if(portId[1] != null && portId[1].isCurrentlyOwned())
               out[1].write(str.getBytes());
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
           PicnomeCommunication.this.initOSCListener("prefix");
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
 
           for(int i = 0; i < 2; i++)
           {
             try
             {
               String str =new String("intensity " + (Integer)args[0] + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[i] != null && portId[i].isCurrentlyOwned())
                 out[i].write(str.getBytes());
             }
             catch(IOException e){}
           }
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
 
           for(int i = 0; i < 2; i++)
           {
             try
             {
               String str =new String("test " + (Integer)args[0] + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[i] != null && portId[i].isCurrentlyOwned())
                 out[i].write(str.getBytes());
             }
             catch(IOException e){}
           }
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
 
           for(int i = 0; i < 2; i++)
           {
             try
             {
               String str =new String("shutdown " + (Integer)args[0] + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[i] != null && portId[i].isCurrentlyOwned())
                 out[i].write(str.getBytes());
             }
             catch(IOException e){}
           }
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
             if(portId[0] != null && portId[0].isCurrentlyOwned())
               out[0].write(str.getBytes());
             if(portId[1] != null && portId[1].isCurrentlyOwned())
               out[1].write(str.getBytes());
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
 
   public void initOSCListener(String str)
   {
     if(((String)this.device_cb.getSelectedItem()).equals(this.device[0]))
       this.address_pattern_prefix[0] = this.prefix_tf.getText();
     else if(((String)this.device_cb.getSelectedItem()).equals(this.device[1]))
       this.address_pattern_prefix[1] = this.prefix_tf.getText();
 
     if(str.equals("all") || str.equals("prefix"))
     {
       this.enableMsgLed();
       this.enableMsgLedCol();
       this.enableMsgLedRow();
       this.enableMsgLedFrame();
       this.enableMsgClear();
       this.enableMsgAdcEnable();
       this.enableMsgPwm();
       //sy this.enableMsgOutput();
     }
     if(str.equals("all"))
     {
       this.enableMsgPrefix();
       this.enableMsgIntensity();
       this.enableMsgTest();
       this.enableMsgShutdown();
       this.enableMsgReport();
       this.enableMsgOffset();
       this.enableMsgCable();
 
       this.oscpin.startListening();
     }
   }
 
   class SerialPortListener implements SerialPortEventListener
   {
     private int index;
     private InputStreamReader inr;
 
     SerialPortListener(int index, InputStreamReader inr)
     {
       this.index = index;
       this.inr = inr;
     }
 
     public void serialEvent(SerialPortEvent event)
     {
       if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE)
       {
         try
         {
           int buffer = 0;
           StringBuffer sb = new StringBuffer();
           while((buffer = this.inr.read()) != -1)
           {
             if(buffer != 0x0A || buffer != 0x0D)
               sb.append((char)buffer);
             if(buffer == 0x0A || buffer == 0x0D)
               break;
           }
 
          //DEBUG debug_tf.setText(this.index + " / " + sb.toString());
            
           if(sb.length() > 0)
            sendOSCMessageFromHw(this.index, sb.toString());
         }
         catch(IOException e){}
       }
     }
   }
 
   void initSerialListener(int index)
   {
     try
     {
       this.port[index].addEventListener(new SerialPortListener(index, this.inr[index]));
       this.port[index].notifyOnDataAvailable(true);
     }
     catch (TooManyListenersException e){}
   }
 }
