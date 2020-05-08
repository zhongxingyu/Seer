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
 * PicnomeSerial.java,v.1.3.1 2009/10/30
  */
 
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 
 public class PicnomeSerial extends JFrame implements ActionListener, ChangeListener
 {
   PicnomeCommunication pserial = new PicnomeCommunication();
   MidiDetailFrame mdf = new MidiDetailFrame();
 
   JPanel psd_p;
   CardLayout psd_cl;
   File hex_f;
   FileReader hex_fr;
   Timer timer;
   int ch, size, count, bar;
 
   public static void main(String[] args)
   {
     PicnomeSerial psgui = new PicnomeSerial();
     psgui.init();
     psgui.setSize(450, 665);
     psgui.addWindowListener(
       new WindowAdapter(){
         public void windowClosing(WindowEvent e)
         {
           System.exit(0);
         }
       }
       );
 
     psgui.mdf.init();
     psgui.mdf.setBounds(450, 0, 890, 620);
 
     psgui.setVisible(true);
   }
 
   public void init()
   {
     SpringLayout sl = new SpringLayout();
     Container c = getContentPane();
     c.setLayout(sl);
 
     //Protocol Settings
     JPanel ps_p = new JPanel();
     SpringLayout ps_sl = new SpringLayout();
     ps_p.setLayout(ps_sl);
     SoftBevelBorder ps_inborder = new SoftBevelBorder(SoftBevelBorder.LOWERED);
     TitledBorder ps_outborder = new TitledBorder(ps_inborder, "Protocol Settings", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
     ps_p.setPreferredSize(new Dimension(430, 160));
     ps_p.setBorder(ps_outborder);
     sl.putConstraint(SpringLayout.NORTH, ps_p, 10, SpringLayout.NORTH, c);
     sl.putConstraint(SpringLayout.WEST, ps_p, 10, SpringLayout.WEST, c);
     c.add(ps_p);
 
     JLabel ioprotocol_l = new JLabel("I/O Protocol :");
     ps_sl.putConstraint(SpringLayout.NORTH, ioprotocol_l, 10, SpringLayout.NORTH, ps_p);
     ps_sl.putConstraint(SpringLayout.WEST, ioprotocol_l, 38, SpringLayout.WEST, ps_p);
     ps_p.add(ioprotocol_l);
     String[] protocol_str = {"Open Sound Control", "MIDI"};
     this.pserial.protocol_cb = new JComboBox(protocol_str);
     this.pserial.protocol_cb.setActionCommand("ProtocolChanged");
     this.pserial.protocol_cb.addActionListener(this);
     ps_sl.putConstraint(SpringLayout.NORTH, this.pserial.protocol_cb, -4, SpringLayout.NORTH, ioprotocol_l);
     ps_sl.putConstraint(SpringLayout.WEST, this.pserial.protocol_cb, 10, SpringLayout.EAST, ioprotocol_l);
     ps_p.add(this.pserial.protocol_cb);
 
     this.psd_p = new JPanel();
     this.psd_cl = new CardLayout();
     this.psd_p.setLayout(psd_cl);
 
     this.psd_p.setPreferredSize(new Dimension(400, 90));
     ps_sl.putConstraint(SpringLayout.NORTH, this.psd_p, 40, SpringLayout.NORTH, ps_p);
     ps_sl.putConstraint(SpringLayout.WEST, this.psd_p, 10, SpringLayout.WEST, ps_p);
     ps_p.add(this.psd_p);
 
     //Open Sound Control Setting
     JPanel osc_p = new JPanel();
     SpringLayout osc_sl = new SpringLayout();
     osc_p.setLayout(osc_sl);
     osc_p.setPreferredSize(new Dimension(400, 90));
     this.psd_p.add(osc_p, "osc");
 
     JLabel hostaddress_l = new JLabel("Host Address :");
     osc_sl.putConstraint(SpringLayout.NORTH, hostaddress_l, 5, SpringLayout.NORTH, osc_p);
     osc_sl.putConstraint(SpringLayout.WEST, hostaddress_l, 20, SpringLayout.WEST, osc_p);
     osc_p.add(hostaddress_l);
 
     this.pserial.hostaddress_tf = new JTextField("127.0.0.1", 10);
     osc_sl.putConstraint(SpringLayout.NORTH, this.pserial.hostaddress_tf, -6, SpringLayout.NORTH, hostaddress_l);
     osc_sl.putConstraint(SpringLayout.WEST, this.pserial.hostaddress_tf, 10, SpringLayout.EAST, hostaddress_l);
     osc_p.add(this.pserial.hostaddress_tf);
 
     JLabel hostport_l = new JLabel("Host Port :");
     osc_sl.putConstraint(SpringLayout.NORTH, hostport_l, 35, SpringLayout.NORTH, osc_p);
     osc_sl.putConstraint(SpringLayout.WEST, hostport_l, 46, SpringLayout.WEST, osc_p);
     osc_p.add(hostport_l);
 
     this.pserial.hostport_tf = new JTextField("8000", 3);
     osc_sl.putConstraint(SpringLayout.NORTH, this.pserial.hostport_tf, -6, SpringLayout.NORTH, hostport_l);
     osc_sl.putConstraint(SpringLayout.WEST, this.pserial.hostport_tf, 10, SpringLayout.EAST, hostport_l);
     osc_p.add(this.pserial.hostport_tf);
 
     JLabel listenport_l = new JLabel("Listen Port :");
     osc_sl.putConstraint(SpringLayout.NORTH, listenport_l, 65, SpringLayout.NORTH, osc_p);
     osc_sl.putConstraint(SpringLayout.WEST, listenport_l, 38, SpringLayout.WEST, osc_p);
     osc_p.add(listenport_l);
 
     this.pserial.listenport_tf = new JTextField("8080", 3);
     osc_sl.putConstraint(SpringLayout.NORTH, this.pserial.listenport_tf, -6, SpringLayout.NORTH, listenport_l);
     osc_sl.putConstraint(SpringLayout.WEST, this.pserial.listenport_tf, 10, SpringLayout.EAST, listenport_l);
     osc_p.add(this.pserial.listenport_tf);
 
     //MIDI Setting
     JPanel midi_p = new JPanel();
     SpringLayout midi_sl = new SpringLayout();
     midi_p.setLayout(midi_sl);
     midi_p.setPreferredSize(new Dimension(400, 90));
     this.psd_p.add(midi_p, "midi");
 
     JLabel midiinput_l = new JLabel("MIDI Input :");
     midi_sl.putConstraint(SpringLayout.NORTH, midiinput_l, 5, SpringLayout.NORTH, midi_p);
     midi_sl.putConstraint(SpringLayout.WEST, midiinput_l, 38, SpringLayout.WEST, midi_p);
     midi_p.add(midiinput_l);
     this.pserial.midiinput_cb = new JComboBox(this.pserial.midiinput_list);
     midi_sl.putConstraint(SpringLayout.NORTH, this.pserial.midiinput_cb, -4, SpringLayout.NORTH, midiinput_l);
     midi_sl.putConstraint(SpringLayout.WEST, this.pserial.midiinput_cb, 10, SpringLayout.EAST, midiinput_l);
     midi_p.add(this.pserial.midiinput_cb);
 
     JLabel midioutput_l = new JLabel("MIDI Output :");
     midi_sl.putConstraint(SpringLayout.NORTH, midioutput_l, 35, SpringLayout.NORTH, midi_p);
     midi_sl.putConstraint(SpringLayout.WEST, midioutput_l, 27, SpringLayout.WEST, midi_p);
     midi_p.add(midioutput_l);
     this.pserial.midioutput_cb = new JComboBox(this.pserial.midioutput_list);
     midi_sl.putConstraint(SpringLayout.NORTH, this.pserial.midioutput_cb, -4, SpringLayout.NORTH, midioutput_l);
     midi_sl.putConstraint(SpringLayout.WEST, this.pserial.midioutput_cb, 10, SpringLayout.EAST, midioutput_l);
     midi_p.add(this.pserial.midioutput_cb);
 
     this.pserial.mididetail_b = new JButton("Detail...");
     this.pserial.mididetail_b.addActionListener(this);
     midi_sl.putConstraint(SpringLayout.NORTH, this.pserial.mididetail_b, 60, SpringLayout.NORTH, midi_p);
     midi_sl.putConstraint(SpringLayout.WEST, this.pserial.mididetail_b, 300, SpringLayout.WEST, midi_p);
     midi_p.add(this.pserial.mididetail_b);
 
     //Device Settings
     JPanel ds_p = new JPanel();
     SpringLayout ds_sl = new SpringLayout();
     ds_p.setLayout(ds_sl);
     SoftBevelBorder ds_inborder = new SoftBevelBorder(SoftBevelBorder.LOWERED);
     TitledBorder ds_outborder = new TitledBorder(ds_inborder, "Device Settings", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
     ds_p.setPreferredSize(new Dimension(430, 350));
     ds_p.setBorder(ds_outborder);
     sl.putConstraint(SpringLayout.NORTH, ds_p, 180, SpringLayout.NORTH, c);
     sl.putConstraint(SpringLayout.WEST, ds_p, 10, SpringLayout.WEST, c);
     c.add(ds_p);
 
     JLabel device_l = new JLabel("Device :");
     ds_sl.putConstraint(SpringLayout.NORTH, device_l, 10, SpringLayout.NORTH, ds_p);
     ds_sl.putConstraint(SpringLayout.WEST, device_l, 22, SpringLayout.WEST, ds_p);
     ds_p.add(device_l);
     this.pserial.device_cb = new JComboBox(this.pserial.device_list);
     this.pserial.device_cb.setActionCommand("DeviceChanged");
     this.pserial.device_cb.addActionListener(this);
     ds_sl.putConstraint(SpringLayout.NORTH, this.pserial.device_cb, -4, SpringLayout.NORTH, device_l);
     ds_sl.putConstraint(SpringLayout.WEST, this.pserial.device_cb, 10, SpringLayout.EAST, device_l);
     ds_p.add(this.pserial.device_cb);
     this.pserial.openclose_b = new JButton("Open");
     this.pserial.openclose_b.addActionListener(this);
     ds_sl.putConstraint(SpringLayout.NORTH, this.pserial.openclose_b, -2, SpringLayout.NORTH, this.pserial.device_cb);
     ds_sl.putConstraint(SpringLayout.WEST, this.pserial.openclose_b, 10, SpringLayout.EAST, this.pserial.device_cb);
     ds_p.add(this.pserial.openclose_b);
 
     JLabel cable_l = new JLabel("Cable Orientation :");
     ds_sl.putConstraint(SpringLayout.NORTH, cable_l, 40, SpringLayout.NORTH, ds_p);
     ds_sl.putConstraint(SpringLayout.WEST, cable_l, 22, SpringLayout.WEST, ds_p);
     ds_p.add(cable_l);
     String[] cable_str = {"Left", "Right", "Up", "Down"};
     this.pserial.cable_cb = new JComboBox(cable_str);
     this.pserial.cable_cb.setActionCommand("CableChanged");
     this.pserial.cable_cb.addActionListener(this);
     ds_sl.putConstraint(SpringLayout.NORTH, this.pserial.cable_cb, -4, SpringLayout.NORTH, cable_l);
     ds_sl.putConstraint(SpringLayout.WEST, this.pserial.cable_cb, 10, SpringLayout.EAST, cable_l);
     ds_p.add(this.pserial.cable_cb);
 
 /* for DEBUG
     this.pserial.debug_tf = new JTextField("", 8);
     ds_sl.putConstraint(SpringLayout.NORTH, this.pserial.debug_tf, 35, SpringLayout.NORTH, ds_p);
     ds_sl.putConstraint(SpringLayout.WEST, this.pserial.debug_tf, 250, SpringLayout.WEST, ds_p);
     ds_p.add(this.pserial.debug_tf);
 
     this.pserial.debug2_tf = new JTextField("", 8);
     ds_sl.putConstraint(SpringLayout.NORTH, this.pserial.debug2_tf, 35, SpringLayout.NORTH, ds_p);
     ds_sl.putConstraint(SpringLayout.WEST, this.pserial.debug2_tf, 270, SpringLayout.WEST, ds_p);
     ds_p.add(this.pserial.debug2_tf);
 */
 
     JPanel dsps_p = new JPanel();
     SpringLayout dsps_sl = new SpringLayout();
     dsps_p.setLayout(dsps_sl);
     SoftBevelBorder dsps_inborder = new SoftBevelBorder(SoftBevelBorder.LOWERED);
     TitledBorder dsps_outborder = new TitledBorder(dsps_inborder, "Device-Specific Protocol Settings", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
     dsps_p.setPreferredSize(new Dimension(395, 130));
     dsps_p.setBorder(dsps_outborder);
     ds_sl.putConstraint(SpringLayout.NORTH, dsps_p, 70, SpringLayout.NORTH, ds_p);
     ds_sl.putConstraint(SpringLayout.WEST, dsps_p, 10, SpringLayout.WEST, ds_p);
     ds_p.add(dsps_p);
 
     JLabel prefix_l = new JLabel("Address Pattern Prefix :");
     dsps_sl.putConstraint(SpringLayout.NORTH, prefix_l, 10, SpringLayout.NORTH, dsps_p);
     dsps_sl.putConstraint(SpringLayout.WEST, prefix_l, 65, SpringLayout.WEST, dsps_p);
     dsps_p.add(prefix_l);
     this.pserial.prefix_tf = new JTextField("/test", 5);
     this.pserial.prefix_tf.addActionListener(this);
     dsps_sl.putConstraint(SpringLayout.NORTH, this.pserial.prefix_tf, -4, SpringLayout.NORTH, prefix_l);
     dsps_sl.putConstraint(SpringLayout.WEST, this.pserial.prefix_tf, 10, SpringLayout.EAST, prefix_l);
     dsps_p.add(this.pserial.prefix_tf);
 
     JLabel startcolumn_l = new JLabel("Starting Column :");
     dsps_sl.putConstraint(SpringLayout.NORTH, startcolumn_l, 40, SpringLayout.NORTH, dsps_p);
     dsps_sl.putConstraint(SpringLayout.WEST, startcolumn_l, 102, SpringLayout.WEST, dsps_p);
     dsps_p.add(startcolumn_l);
     SpinnerNumberModel startcolumn_m = new SpinnerNumberModel(0, 0, null, 1);
     this.pserial.startcolumn_s = new JSpinner(startcolumn_m);
     JSpinner.NumberEditor startcolumn_edit = new JSpinner.NumberEditor(this.pserial.startcolumn_s);
     this.pserial.startcolumn_s.setEditor(startcolumn_edit);
     JFormattedTextField startcolumn_text = startcolumn_edit.getTextField();
     startcolumn_text.setEditable(false);
     this.pserial.startcolumn_s.addChangeListener(this);
     this.pserial.startcolumn_s.setPreferredSize(new Dimension(50, 22));
     dsps_sl.putConstraint(SpringLayout.NORTH, this.pserial.startcolumn_s, -4, SpringLayout.NORTH, startcolumn_l);
     dsps_sl.putConstraint(SpringLayout.WEST, this.pserial.startcolumn_s, 10, SpringLayout.EAST, startcolumn_l);
     dsps_p.add(this.pserial.startcolumn_s);
 
     JLabel startrow_l = new JLabel("Starting Row :");
     dsps_sl.putConstraint(SpringLayout.NORTH, startrow_l, 70, SpringLayout.NORTH, dsps_p);
     dsps_sl.putConstraint(SpringLayout.WEST, startrow_l, 125, SpringLayout.WEST, dsps_p);
     dsps_p.add(startrow_l);
     SpinnerNumberModel startrow_m = new SpinnerNumberModel(0, 0, null, 1);
     this.pserial.startrow_s = new JSpinner(startrow_m);
     JSpinner.NumberEditor startrow_edit = new JSpinner.NumberEditor(this.pserial.startrow_s);
     this.pserial.startrow_s.setEditor(startrow_edit);
     JFormattedTextField startrow_text = startrow_edit.getTextField();
     startrow_text.setEditable(false);
     this.pserial.startrow_s.addChangeListener(this);
     this.pserial.startrow_s.setPreferredSize(new Dimension(50, 22));
     dsps_sl.putConstraint(SpringLayout.NORTH, this.pserial.startrow_s, -4, SpringLayout.NORTH, startrow_l);
     dsps_sl.putConstraint(SpringLayout.WEST, this.pserial.startrow_s, 10, SpringLayout.EAST, startrow_l);
     dsps_p.add(this.pserial.startrow_s);
 
     JPanel ais_p = new JPanel();
     SpringLayout ais_sl = new SpringLayout();
     ais_p.setLayout(ais_sl);
     SoftBevelBorder ais_inborder = new SoftBevelBorder(SoftBevelBorder.LOWERED);
     TitledBorder ais_outborder = new TitledBorder(ais_inborder, "Analog Input Enable Settings", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
     ais_p.setPreferredSize(new Dimension(395, 100));
     ais_p.setBorder(ais_outborder);
     ds_sl.putConstraint(SpringLayout.NORTH, ais_p, 210, SpringLayout.NORTH, ds_p);
     ds_sl.putConstraint(SpringLayout.WEST, ais_p, 10, SpringLayout.WEST, ds_p);
     ds_p.add(ais_p);
 
     this.pserial.adc_ck[0] = new JCheckBox(" adc 0");
     this.pserial.adc_ck[0].addActionListener(this);
     ais_sl.putConstraint(SpringLayout.NORTH, this.pserial.adc_ck[0], 10, SpringLayout.NORTH, ais_p);
     ais_sl.putConstraint(SpringLayout.WEST, this.pserial.adc_ck[0], 10, SpringLayout.WEST, ais_p);
     ais_p.add(this.pserial.adc_ck[0]);
     this.pserial.adc_ck[1] = new JCheckBox(" adc 1");
     this.pserial.adc_ck[1].addActionListener(this);
     ais_sl.putConstraint(SpringLayout.NORTH, this.pserial.adc_ck[1], 10, SpringLayout.NORTH, ais_p);
     ais_sl.putConstraint(SpringLayout.WEST, this.pserial.adc_ck[1], 100, SpringLayout.WEST, ais_p);
     ais_p.add(this.pserial.adc_ck[1]);
     this.pserial.adc_ck[2] = new JCheckBox(" adc 2");
     this.pserial.adc_ck[2].addActionListener(this);
     ais_sl.putConstraint(SpringLayout.NORTH, this.pserial.adc_ck[2], 10, SpringLayout.NORTH, ais_p);
     ais_sl.putConstraint(SpringLayout.WEST, this.pserial.adc_ck[2], 190, SpringLayout.WEST, ais_p);
     ais_p.add(this.pserial.adc_ck[2]);
     this.pserial.adc_ck[3] = new JCheckBox(" adc 3");
     this.pserial.adc_ck[3].addActionListener(this);
     ais_sl.putConstraint(SpringLayout.NORTH, this.pserial.adc_ck[3], 10, SpringLayout.NORTH, ais_p);
     ais_sl.putConstraint(SpringLayout.WEST, this.pserial.adc_ck[3], 280, SpringLayout.WEST, ais_p);
     ais_p.add(this.pserial.adc_ck[3]);
     this.pserial.adc_ck[4] = new JCheckBox(" adc 4");
     this.pserial.adc_ck[4].addActionListener(this);
     ais_sl.putConstraint(SpringLayout.NORTH, this.pserial.adc_ck[4], 40, SpringLayout.NORTH, ais_p);
     ais_sl.putConstraint(SpringLayout.WEST, this.pserial.adc_ck[4], 10, SpringLayout.WEST, ais_p);
     ais_p.add(this.pserial.adc_ck[4]);
     this.pserial.adc_ck[5] = new JCheckBox(" adc 5");
     this.pserial.adc_ck[5].addActionListener(this);
     ais_sl.putConstraint(SpringLayout.NORTH, this.pserial.adc_ck[5], 40, SpringLayout.NORTH, ais_p);
     ais_sl.putConstraint(SpringLayout.WEST, this.pserial.adc_ck[5], 100, SpringLayout.WEST, ais_p);
     ais_p.add(this.pserial.adc_ck[5]);
     this.pserial.adc_ck[6] = new JCheckBox(" adc 6");
     this.pserial.adc_ck[6].addActionListener(this);
     ais_sl.putConstraint(SpringLayout.NORTH, this.pserial.adc_ck[6], 40, SpringLayout.NORTH, ais_p);
     ais_sl.putConstraint(SpringLayout.WEST, this.pserial.adc_ck[6], 190, SpringLayout.WEST, ais_p);
     ais_p.add(this.pserial.adc_ck[6]);
 
     //Firmware Update
     JPanel fu_p = new JPanel();
     SpringLayout fu_sl = new SpringLayout();
     fu_p.setLayout(fu_sl);
     SoftBevelBorder fu_inborder = new SoftBevelBorder(SoftBevelBorder.LOWERED);
     TitledBorder fu_outborder = new TitledBorder(fu_inborder, "Firmware Update", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
     fu_p.setPreferredSize(new Dimension(430, 95));
     fu_p.setBorder(fu_outborder);
     sl.putConstraint(SpringLayout.NORTH, fu_p, 540, SpringLayout.NORTH, c);
     sl.putConstraint(SpringLayout.WEST, fu_p, 10, SpringLayout.WEST, c);
     c.add(fu_p);
 
     JLabel hex_l = new JLabel("HEX :");
     fu_sl.putConstraint(SpringLayout.NORTH, hex_l, 10, SpringLayout.NORTH, fu_p);
     fu_sl.putConstraint(SpringLayout.WEST, hex_l, 38, SpringLayout.WEST, fu_p);
     fu_p.add(hex_l);
     this.pserial.hex_tf = new JTextField("", 10);
     fu_sl.putConstraint(SpringLayout.NORTH, this.pserial.hex_tf, -4, SpringLayout.NORTH, hex_l);
     fu_sl.putConstraint(SpringLayout.WEST, this.pserial.hex_tf, 10, SpringLayout.EAST, hex_l);
     fu_p.add(this.pserial.hex_tf);
     this.pserial.hex_b = new JButton("Select");
     this.pserial.hex_b.addActionListener(this);
     fu_sl.putConstraint(SpringLayout.NORTH, this.pserial.hex_b, -2, SpringLayout.NORTH, this.pserial.hex_tf);
     fu_sl.putConstraint(SpringLayout.WEST, this.pserial.hex_b, 10, SpringLayout.EAST, this.pserial.hex_tf);
     fu_p.add(this.pserial.hex_b);
     this.pserial.update_b = new JButton("Update");
     this.pserial.update_b.addActionListener(this);
     fu_sl.putConstraint(SpringLayout.NORTH, this.pserial.update_b, 0, SpringLayout.NORTH, this.pserial.hex_b);
     fu_sl.putConstraint(SpringLayout.WEST, this.pserial.update_b, 8, SpringLayout.EAST, this.pserial.hex_b);
     fu_p.add(this.pserial.update_b);
     this.pserial.update_b.setEnabled(false);
     this.pserial.update_pb = new JProgressBar(0, 100);
     fu_sl.putConstraint(SpringLayout.NORTH, this.pserial.update_pb, 32, SpringLayout.NORTH, fu_p);
     fu_sl.putConstraint(SpringLayout.WEST, this.pserial.update_pb, 40, SpringLayout.WEST, fu_p);
     fu_p.add(this.pserial.update_pb);
     this.pserial.update_pb.setPreferredSize(new Dimension(350, 30));
     this.timer = new Timer(1, this);
   }
 
   PicnomeSerial()
   {
     super("PICnomeSerial");
   }
 
   public void actionPerformed(ActionEvent e)
   {
     String cmd = e.getActionCommand();
 
     if(cmd == null)
       cmd = "timer";
     else if(cmd.equals(this.pserial.prefix_tf.getText()))
       cmd = "Prefix";
 
     //DEBUG this.pserial.debug_tf.setText(cmd);
 
     if(cmd.equals("DeviceChanged"))
     {
       if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[0]))
         this.pserial.changeDeviceSettings(0);
       else if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[1]))
         this.pserial.changeDeviceSettings(1);
     }
     else if(cmd.equals("ProtocolChanged"))
     {
       if(((String)this.pserial.protocol_cb.getSelectedItem()).equals("Open Sound Control"))
         this.psd_cl.first(this.psd_p);
       else if(((String)this.pserial.protocol_cb.getSelectedItem()).equals("MIDI"))
         this.psd_cl.last(this.psd_p);
     }
     else if(cmd.equals("CableChanged"))
     {
       if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[0]))
         this.pserial.cable_orientation[0] = (String)this.pserial.cable_cb.getSelectedItem();
       else if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[1]))
         this.pserial.cable_orientation[1] = (String)this.pserial.cable_cb.getSelectedItem();
     }
     else if(cmd.equals("Open"))
     {
       boolean b;
 
       if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[0]))
       {
         b = this.pserial.openSerialPort(0);
         b = this.pserial.setSerialPort(0);
         if(this.pserial.co_max_num[0] == 7)
           this.mdf.setHalfVisible();
       }
       else if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[1]))
       {
         b = this.pserial.openSerialPort(1);
         b = this.pserial.setSerialPort(1);
         if(this.pserial.co_max_num[1] == 7)
           this.mdf.setHalfVisible();
       }
       this.pserial.openclose_b.setText("Close");
     }
     else if(cmd.equals("Close"))
     {
       boolean b;
 
       if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[0]))
         b = this.pserial.closeSerialPort(0);
       else if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[1]))
         b = this.pserial.closeSerialPort(1);
       this.pserial.openclose_b.setText("Open");
     }
     else if(cmd.equals("Detail..."))
       this.mdf.setVisible(true);
     else if(cmd.equals("Select"))
     {
       JFileChooser fc = new JFileChooser();
       int selected = fc.showOpenDialog(this);
       if (selected == JFileChooser.APPROVE_OPTION)
       {
         this.hex_f = fc.getSelectedFile();
         this.pserial.hex_tf.setText(this.hex_f.getName());
         this.pserial.update_b.setEnabled(true);
       }
     }
     else if(cmd.equals("Update"))
     {
       try
       {
         this.hex_fr = new FileReader(this.hex_f);
         this. size = 0;
         this.count = 0;
         this.bar = 0;
         while(this.hex_fr.read() != -1)
           this.size++;
         this.timer.start();
         this.hex_fr = new FileReader(this.hex_f);
       }
       catch(IOException ioe){}
     }
     else if(cmd.equals("Prefix"))
       this.pserial.initOSCListener("prefix");
     else if(cmd.equals(" adc 0") || cmd.equals(" adc 1") || cmd.equals(" adc 2") || cmd.equals(" adc 3") ||
             cmd.equals(" adc 4") || cmd.equals(" adc 5") || cmd.equals(" adc 6"))
     {
       try
       {
         int adc_id = Integer.parseInt(cmd.substring(5, 6));
         boolean b = this.pserial.adc_ck[adc_id].isSelected();
         String str;
         if(b)
           str =new String("adc_enable " + adc_id + " " + 1 + (char)0x0D);
         else
           str =new String("adc_enable " + adc_id + " " + 0 + (char)0x0D);
 
         if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[0]))
         {
           this.pserial.adc_enable[0][adc_id] = b;
           this.pserial.out[0].write(str.getBytes());
         }
         else if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[1]))
         {
           this.pserial.adc_enable[1][adc_id] = b;
           this.pserial.out[1].write(str.getBytes());
         }
       }
       catch(IOException ioe){}
     }
     else if(cmd.equals("timer"))
     {
       try
       {
         if((this.ch = this.hex_fr.read()) != -1)
         {
           this.bar = (int)(((double)this.count / (double)this.size) * 100);
           this.pserial.update_pb.setValue(this.bar);
           this.count++;
           this.pserial.out[0].write(this.ch);
         }
         if(this.ch == -1 || this.ch == 59)
         {
           this.pserial.update_pb.setValue(0);
           if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[0]))
             this.pserial.closeSerialPort(0);
           else if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[1]))
             this.pserial.closeSerialPort(1);
           this.pserial.openclose_b.setText("Open");
           this.hex_fr.close();
           this.timer.stop();
         }
       }
       catch(IOException ioe){}
     }
   }
 
   public void stateChanged(ChangeEvent e)
   {
     if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[0]))
     {
       this.pserial.starting_column[0] = (Integer)this.pserial.startcolumn_s.getValue();
       this.pserial.starting_row[0] = (Integer)this.pserial.startrow_s.getValue();
     }
     else if(((String)this.pserial.device_cb.getSelectedItem()).equals(this.pserial.device[1]))
     {
       this.pserial.starting_column[1] = (Integer)this.pserial.startcolumn_s.getValue();
       this.pserial.starting_row[1] = (Integer)this.pserial.startrow_s.getValue();
     }
   }
 
   public class MidiDetailFrame extends JFrame implements ActionListener
   {
     File save_f, load_f;
     JPanel mididetail_p;
     JButton save, load;
     MidiPadConfPanel[][] mpcp = new MidiPadConfPanel[16][8];
 
     MidiDetailFrame(){
       super("MIDI Detail Setting...");
     }
     public void init()
     {
       SpringLayout sl = new SpringLayout();
       this.mididetail_p = new JPanel();
       this.mididetail_p.setPreferredSize(new Dimension(1800, 570));
       this.mididetail_p.setLayout(sl);
       //sy c.setLayout(sl);
 
       PicnomeSerial.this.pserial.prev_index = 0;
       PicnomeSerial.this.pserial.para_change_flag = false;
       String[] type_name = {"Channel (1 - 16)", "Velocity 1 (0 - 127)", "Velocity 2 (0 - 127)",
                             "Duration 1 (0 - 60000) [msec]", "Duration 2 (0 - 60000) [msec]"};
       PicnomeSerial.this.pserial.midiparameter_cb = new JComboBox(type_name);
       PicnomeSerial.this.pserial.midiparameter_cb.setActionCommand("TypeChanged");
       PicnomeSerial.this.pserial.midiparameter_cb.addActionListener(this);
       sl.putConstraint(SpringLayout.WEST, PicnomeSerial.this.pserial.midiparameter_cb, 10, SpringLayout.WEST, this.mididetail_p);
       sl.putConstraint(SpringLayout.NORTH, PicnomeSerial.this.pserial.midiparameter_cb, 20, SpringLayout.NORTH, this.mididetail_p);
       this.mididetail_p.add(PicnomeSerial.this.pserial.midiparameter_cb);
 
       this.save = new JButton("Save As...");
       this.save.addActionListener(this);
       sl.putConstraint(SpringLayout.WEST, this.save, 10, SpringLayout.EAST, PicnomeSerial.this.pserial.midiparameter_cb);
       sl.putConstraint(SpringLayout.NORTH, this.save, 0, SpringLayout.NORTH, PicnomeSerial.this.pserial.midiparameter_cb);
       this.mididetail_p.add(this.save);
 
       this.load = new JButton("Load...");
       this.load.addActionListener(this);
       sl.putConstraint(SpringLayout.WEST, this.load, 10, SpringLayout.EAST, this.save);
       sl.putConstraint(SpringLayout.NORTH, this.load, 0, SpringLayout.NORTH, this.save);
       this.mididetail_p.add(this.load);
 
       for(int j = 0; j < this.mpcp[0].length; j++)
       {
         for(int i = 0; i < this.mpcp.length; i++)
         {
           this.mpcp[i][j] = new MidiPadConfPanel(i, j);
           sl.putConstraint(SpringLayout.WEST, this.mpcp[i][j], (110 * i) + 10, SpringLayout.WEST, this.mididetail_p);
           sl.putConstraint(SpringLayout.NORTH, this.mpcp[i][j], (60 * j) + 60, SpringLayout.NORTH, this.mididetail_p);
           this.mididetail_p.add(this.mpcp[i][j]);
 
           for(int k = 0; k < 5; k++)
             switch(k)
             {
             case 0:
               PicnomeSerial.this.pserial.midi_parameter[i][j][k] = 1;
               break;
             case 1:
               PicnomeSerial.this.pserial.midi_parameter[i][j][k] = 127;
               break;
             case 2:
               PicnomeSerial.this.pserial.midi_parameter[i][j][k] = 0;
               break;
             case 3:
               PicnomeSerial.this.pserial.midi_parameter[i][j][k] = 30000;
               break;
             case 4:
               PicnomeSerial.this.pserial.midi_parameter[i][j][k] = 1;
               break;
             }
         }
       }
       Container c = getContentPane();
       JScrollPane sp = new JScrollPane(this.mididetail_p);
       c.add(sp);
     }
 
     public void setHalfVisible()
     {
       for(int j = 0; j < this.mpcp[0].length; j++)
         for(int i = 0; i < this.mpcp.length; i++)
           if(i > 7)
             this.mpcp[i][j].setVisible(false);
     }
 
     public void actionPerformed(ActionEvent e)
     {
       String cmd = e.getActionCommand();
 
       if(cmd.equals("TypeChanged"))
       {
         PicnomeSerial.this.pserial.para_change_flag = true;
         String index_name = (String)PicnomeSerial.this.pserial.midiparameter_cb.getSelectedItem();
         int index = PicnomeSerial.this.pserial.midiparameter_cb.getSelectedIndex();
 
         if(index != PicnomeSerial.this.pserial.prev_index)
         {
           for(int j = 0; j < this.mpcp[0].length; j++)
           {
             for(int i = 0; i < this.mpcp.length; i++)
             {
               PicnomeSerial.this.pserial.midi_parameter[i][j][PicnomeSerial.this.pserial.prev_index]
                 = (Integer)this.mpcp[i][j].value.getValue();
             }
           }
         }
 
         for(int j = 0; j < this.mpcp[0].length; j++)
         {
           for(int i = 0; i < this.mpcp.length; i++)
           {
             switch(index)
             {
             case 0:
               this.mpcp[i][j].setSliderRange(1, 16);
               break;
             case 1:
               this.mpcp[i][j].setSliderRange(0, 127);
               break;
             case 2:
               this.mpcp[i][j].setSliderRange(0, 127);
               break;
             case 3:
               this.mpcp[i][j].setSliderRange(0, 60000);
               break;
             case 4:
               this.mpcp[i][j].setSliderRange(0, 60000);
               break;
             }
             this.mpcp[i][j].value.setValue(PicnomeSerial.this.pserial.midi_parameter[i][j][index]);
           }
         }
         PicnomeSerial.this.pserial.prev_index = index;
         PicnomeSerial.this.pserial.para_change_flag = false;
       }
       else if(cmd.equals("Save As..."))
       {
         JFileChooser fc = new JFileChooser();
         int selected = fc.showSaveDialog(this);
         if (selected == JFileChooser.APPROVE_OPTION)
         {
           this.save_f = fc.getSelectedFile();
         }
         try
         {
           FileWriter fw = new FileWriter(this.save_f);
           BufferedWriter bw = new BufferedWriter(fw);
           for(int k = 0; k < 5; k++)
           {
             switch(k)
             {
             case 0:
               bw.write("//MIDI channel" + System.getProperty("line.separator"));
               break;
             case 1:
               bw.write("//MIDI velocity 1" + System.getProperty("line.separator"));
               break;
             case 2:
               bw.write("//MIDI velocity 2" + System.getProperty("line.separator"));
               break;
             case 3:
               bw.write("//MIDI duration 1" + System.getProperty("line.separator"));
               break;
             case 4:
               bw.write("//MIDI duration 2" + System.getProperty("line.separator"));
               break;
             }
             for(int j = 0; j < this.mpcp[0].length; j++)
             {
               String line = "";
               for(int i = 0; i < this.mpcp.length - 1; i++)
               {
                 line += (Integer.toString(PicnomeSerial.this.pserial.midi_parameter[i][j][k]) + " ");
               }
               line += (Integer.toString(PicnomeSerial.this.pserial.midi_parameter[this.mpcp.length - 1][j][k]));
               line += System.getProperty("line.separator");
               bw.write(line);
             }
             if(k != 3)
               bw.write(System.getProperty("line.separator"));
           }
           bw.flush();
           bw.close();
         }
         catch(IOException ioe){}
       }
       else if(cmd.equals("Load..."))
       {
         JFileChooser fc = new JFileChooser();
         int selected = fc.showOpenDialog(this);
         if (selected == JFileChooser.APPROVE_OPTION)
         {
           this.load_f = fc.getSelectedFile();
         }
         try
         {
           FileReader fr = new FileReader(this.load_f);
           BufferedReader br = new BufferedReader(fr);
           String line = "";
           int x = 0, y = 0, index = -1;
           while((line = br.readLine()) != null)
           {
             if(line.indexOf("//") != -1 || line.indexOf(System.getProperty("line.separator")) == 0)
             {
               y = 0;
               index++;
             }
             else
             {
               java.util.StringTokenizer st = new java.util.StringTokenizer(line);
               x = 0;
               while(st.hasMoreTokens())
               {
                 PicnomeSerial.this.pserial.midi_parameter[x][y][index] = Integer.valueOf(st.nextToken());
                 x++;
               }
               y++;
             }
           }
           int k = PicnomeSerial.this.pserial.midiparameter_cb.getSelectedIndex();
           for(int j = 0; j < this.mpcp[0].length; j++)
           {
             for(int i = 0; i < this.mpcp.length; i++)
             {
               this.mpcp[i][j].value.setValue(PicnomeSerial.this.pserial.midi_parameter[i][j][k]);
             }
           }
         }
         catch(IOException ioe){}
       }
     }
   }
 
   public class MidiPadConfPanel extends JPanel implements ChangeListener
   {
     SpinnerNumberModel snm;
     JSpinner value;
     JSlider slider;
     int lattice_x, lattice_y;
 
     MidiPadConfPanel(int x, int y){
       this.lattice_x = x;
       this.lattice_y = y;
       SpringLayout mpcp_sl = new SpringLayout();
       this.setLayout(mpcp_sl);
       this.setPreferredSize(new Dimension(110, 50));
 
       this.snm = new SpinnerNumberModel(1, 1, 16, 1);
       this.value = new JSpinner(this.snm);
       JSpinner.NumberEditor ne = new JSpinner.NumberEditor(this.value);
       this.value.setEditor(ne);
       JFormattedTextField ftf = ne.getTextField();
       ftf.setEditable(false);
       this.value.addChangeListener(this);
       this.value.setPreferredSize(new Dimension(100, 22));
       mpcp_sl.putConstraint(SpringLayout.WEST, this.value, 0, SpringLayout.WEST, this); 
       mpcp_sl.putConstraint(SpringLayout.NORTH, this.value, 0, SpringLayout.NORTH, this);
       this.add(this.value);
 
       this.slider = new JSlider();
       this.slider.setValue(1);
       this.slider.setMinimum(1);
       this.slider.setMaximum(16); 
       this.slider.addChangeListener(this);
       this.slider.setPreferredSize(new Dimension(116, 25));
       mpcp_sl.putConstraint(SpringLayout.WEST, this.slider, -6, SpringLayout.WEST, this);
       mpcp_sl.putConstraint(SpringLayout.NORTH, this.slider, 30, SpringLayout.NORTH, this);
       this.add(this.slider);
     }
 
     void setSliderRange(int min, int max)
     {
       this.snm.setMinimum(min);
       this.snm.setMaximum(max);
       this.slider.setMinimum(min);
       this.slider.setMaximum(max);
     }
 
     public void stateChanged(ChangeEvent e)
     {
       if(e.getSource() == this.value)
         this.slider.setValue((Integer)this.value.getValue());
       else if(e.getSource() == this.slider)
         this.value.setValue(this.slider.getValue());
       if(PicnomeSerial.this.pserial.para_change_flag == false)
       {
         int index = PicnomeSerial.this.pserial.midiparameter_cb.getSelectedIndex(); 
         PicnomeSerial.this.pserial.midi_parameter[lattice_x][lattice_y][index] = (Integer)this.value.getValue();
         if(index == 0)
           PicnomeSerial.this.pserial.midiout[lattice_x + (lattice_y * 8)] = PicnomeSerial.this.pserial.midiio.getMidiOut(
             PicnomeSerial.this.pserial.midi_parameter[lattice_x][lattice_y][index] - 1, PicnomeSerial.this.pserial.midi_out_port);
       }
     }
   }
 }
