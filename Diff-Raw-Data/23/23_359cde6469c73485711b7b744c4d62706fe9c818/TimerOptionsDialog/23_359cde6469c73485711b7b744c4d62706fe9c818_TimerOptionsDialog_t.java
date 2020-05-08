/* $Id: TimerOptionsDialog.java,v 1.13 2007-03-25 13:02:02 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Calendar;
 
 import javax.swing.*;
 
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.hampelratte.svdrp.responses.highlevel.VDRTimer;
 
 import lazybones.LazyBones;
 import lazybones.ProgramManager;
 import lazybones.Time;
 import lazybones.Timer;
 import lazybones.VDRChannelList;
 import tvbrowser.core.ChannelList;
 import devplugin.Date;
 import devplugin.Plugin;
 import devplugin.Program;
 
 /**
  * Shown, if a timer should be edited.
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  */
 public class TimerOptionsDialog implements ActionListener,
         ItemListener {
     private JLabel lChannels = new JLabel(LazyBones.getTranslation("channel", "Channel"));
 
     private JComboBox channels = new JComboBox();
 
     private JLabel lDay = new JLabel(LazyBones.getTranslation("day", "Day"));
 
     private DayChooser dayChooser;
 
     private BrowseTextField day;
 
     private JLabel lStarttime = new JLabel(LazyBones.getTranslation("start", "Start"));
 
     private JSpinner starttime = new JSpinner();
 
     private JLabel lEndtime = new JLabel(LazyBones.getTranslation("stop", "Stop"));
 
     private JSpinner endtime = new JSpinner();
 
     private JLabel lPriority = new JLabel(LazyBones.getTranslation("priority",
             "Prioritt"));
 
     private JSpinner priority = new JSpinner();
 
     private JLabel lLifetime = new JLabel(LazyBones.getTranslation("lifetime", "Lifetime"));
 
     private JSpinner lifetime = new JSpinner();
 
     private JLabel lTitle = new JLabel(LazyBones.getTranslation("title", "Title"));
 
     private JTextField title = new JTextField();
     
     private JLabel lDescription = new JLabel(LazyBones.getTranslation("description", "Description"));
     
     private JComboBox comboDesc = new JComboBox();
 
     private JTextArea description = new JTextArea();
 
     private JButton ok = new JButton();
 
     private JButton cancel = new JButton();
 
     private LazyBones control;
 
     private JDialog dialog;
 
     private JPanel panel = new JPanel();
 
     private Timer timer;
     
     private Program prog;
     
     private JLabel lActive = new JLabel(LazyBones.getTranslation("active", "Active"));
     
     private JCheckBox cbActive = new JCheckBox();
     
     private JLabel lVps = new JLabel(LazyBones.getTranslation("vps", "VPS"));
     
     private JCheckBox cbVps = new JCheckBox();
 
     private boolean update = false;
     
     public TimerOptionsDialog(LazyBones control, Timer timer, Program prog, boolean update) {
         this.update = update;
         this.control = control;
         this.timer = timer;
         this.prog = prog;
         dayChooser = new DayChooser(timer);
         day = new BrowseTextField(dayChooser);
         initGUI();
     }
 
     private void initGUI() {
         dialog = new JDialog(control.getParent(), true);
         dialog.setTitle(LazyBones.getTranslation("windowtitle_timerOptions", "Timer Options"));
         panel.setLayout(new GridBagLayout());
         dialog.getContentPane().add(panel);
         GridBagConstraints gbc = new GridBagConstraints();
 
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.anchor = GridBagConstraints.NORTHWEST;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.weightx = 0.3;
         gbc.weighty = 0.1;
 
         gbc.gridx = 0;
         gbc.gridy = 0;
         panel.add(lActive, gbc);
         
         gbc.gridx = 0;
         gbc.gridy = 1;
         panel.add(lVps, gbc);
         
         gbc.gridx = 0;
         gbc.gridy = 2;
         panel.add(lTitle, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 3;
         panel.add(lChannels, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 4;
         panel.add(lDay, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 5;
         panel.add(lStarttime, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 6;
         panel.add(lEndtime, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 7;
         panel.add(lPriority, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 8;
         panel.add(lLifetime, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 3;
         panel.add(channels, gbc);
         devplugin.Channel[] c = ChannelList.getSubscribedChannels();
         for (int i = 0; i < c.length; i++) {
             channels.addItem(c[i]);
         }
         
         // we have to remove the buffers again, to get the right start date
         // example: start time is 00.00 h with time buffers we have 23.45
         // Calendar then decreases the start date, so that we don't have the right date, but
         // the date of the day before.
         Timer tmp = (Timer)timer.clone();
         control.removeTimerBuffers(tmp);
         Program prog = ProgramManager.getInstance().getProgram(tmp);
         if(prog != null) {
             channels.setSelectedItem(prog.getChannel());
         } else {
             Channel chan = VDRChannelList.getInstance().getChannelByNumber(timer.getChannelNumber());
             channels.addItem(chan);
             channels.setSelectedItem(chan);
         }
 
         gbc.gridx = 0;
         gbc.gridy = 9;
         panel.add(lDescription, gbc);
         
         gbc.gridx = 0;
         gbc.gridy = 10;
         gbc.gridwidth = 2;
         gbc.weighty = 1.0;
         description.setRows(10);
         description.setLineWrap(true);
         description.setWrapStyleWord(true);
         if ("".equals(timer.getDescription())
                 && prog != null && !"".equals(prog.getDescription())) {
             description.setText(prog.getDescription());
             description.append("\n\n" + prog.getChannel().getCopyrightNotice());
         } else {
             description.setText(timer.getDescription());
         }
         panel.add(new JScrollPane(description), gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 11;
         gbc.gridwidth = 1;
         gbc.weighty = 0.1;
         panel.add(cancel, gbc);
 
         gbc.weightx = 1.0;
 
         gbc.gridx = 1;
         gbc.gridy = 0;
         panel.add(cbActive, gbc);
         cbActive.setSelected(timer.isActive());
         
         gbc.gridx = 1;
         gbc.gridy = 1;
         panel.add(cbVps, gbc);
        cbVps.setSelected(timer.hasState(VDRTimer.VPS));
         
         gbc.gridx = 1;
         gbc.gridy = 2;
         panel.add(title, gbc);
         title.setText(timer.getFile());
 
         gbc.gridx = 1;
         gbc.gridy = 4;
         panel.add(day, gbc);
         day.setText(timer.getDayString());
         day.setEditable(false);
 
         gbc.gridx = 1;
         gbc.gridy = 5;
         panel.add(starttime, gbc);
         SpinnerTimeModel model = new SpinnerTimeModel();
         int hour = timer.getStartTime().get(Calendar.HOUR_OF_DAY);
         int minute = timer.getStartTime().get(Calendar.MINUTE);
         model.setValue(new Time(hour, minute));
         starttime.setModel(model);
 
         gbc.gridx = 1;
         gbc.gridy = 6;
         panel.add(endtime, gbc);
         model = new SpinnerTimeModel();
         hour = timer.getEndTime().get(Calendar.HOUR_OF_DAY);
         minute = timer.getEndTime().get(Calendar.MINUTE);
         model.setValue(new Time(hour, minute));
         endtime.setModel(model);
 
         gbc.gridx = 1;
         gbc.gridy = 7;
         panel.add(priority, gbc);
         priority.setModel(new SpinnerNumberModel(timer.getPriority(), 0, 99, 1));
 
         gbc.gridx = 1;
         gbc.gridy = 8;
         panel.add(lifetime, gbc);
         lifetime.setModel(new SpinnerNumberModel(timer.getLifetime(), 0, 99, 1));
         
         gbc.gridx = 1;
         gbc.gridy = 9;
         panel.add(comboDesc, gbc);
         comboDesc.addItem("VDR");
         comboDesc.addItem("TV-Browser");
         comboDesc.addItemListener(this);
         comboDesc.setEnabled(!update);
 
         gbc.gridx = 1;
         gbc.gridy = 11;
         panel.add(ok, gbc);
 
         ok.setText(LazyBones.getTranslation("ok", "OK"));
         cancel.setText(LazyBones.getTranslation("cancel", "Cancel"));
 
         ok.addActionListener(this);
         cancel.addActionListener(this);
 
         if (update) {
             day.setEnabled(false);
             channels.setEnabled(false);
             starttime.setEnabled(false);
             endtime.setEnabled(false);
         }
         
         dialog.setSize(400, 500);
         dialog.setLocation(50, 50);
         dialog.setVisible(true);
         dialog.pack();
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == ok) {
             timer.setFile(title.getText());
             Channel vdrc = null;
             Object selected = channels.getSelectedItem();
             if(selected instanceof devplugin.Channel) {
                 devplugin.Channel c = (devplugin.Channel) selected;
                 vdrc = (Channel) ProgramManager.getChannelMapping().get(c.getId());
             } else if( selected instanceof Channel) {
                 vdrc = (Channel) selected;
             }
             timer.setChannelNumber(vdrc.getChannelNumber());
             Calendar start = timer.getStartTime();
             start.set(Calendar.HOUR_OF_DAY, ((Time) starttime.getValue()).getHour());
             start.set(Calendar.MINUTE, ((Time) starttime.getValue()).getMinute());
             timer.setStartTime(start);
             Calendar end = timer.getEndTime();
             end.set(Calendar.HOUR_OF_DAY, ((Time) endtime.getValue()).getHour());
             end.set(Calendar.MINUTE, ((Time) endtime.getValue()).getMinute());
             timer.setEndTime(end);
             timer.setPriority(((Integer) priority.getValue()).intValue());
             timer.setLifetime(((Integer) lifetime.getValue()).intValue());
             timer.setDescription(description.getText());
            timer.changeStateTo(VDRTimer.ACTIVE, cbActive.isSelected());
            timer.changeStateTo(VDRTimer.VPS, cbVps.isSelected());
             dialog.dispose();
             control.createTimerCallBack(timer, prog, update);
         } else if (e.getSource() == cancel) {
             dialog.dispose();
         } 
     }
 
     public void itemStateChanged(ItemEvent e) {
         if(e.getStateChange() == ItemEvent.SELECTED) {
             String item = (String)e.getItem();
             if("VDR".equals(item)) {
                 /*
                  * HAMPELRATTE funzt noch nicht Date date; if(timer.isRepeating()) {
                  * date = new Date(timer.getProgTime()); } else { date = new
                  * Date(timer.getStartTime()); } Program prog =
                  * Plugin.getPluginManager().getProgram(date,
                  * timer.getTvBrowserProgID()); Calendar tmpCal =
                  * GregorianCalendar.getInstance(); if(timer.isRepeating()) {
                  * tmpCal.setTimeInMillis(timer.getProgTime().getTimeInMillis()); }
                  * else {
                  * tmpCal.setTimeInMillis(timer.getStartTime().getTimeInMillis()); }
                  * tmpCal.add(Calendar.SECOND, 30); // add 30 seconds to be sure,
                  * that the right program is chosen VDRTimer tmp =
                  * control.getVDRProgramAt(tmpCal, prog.getChannel()); String desc =
                  * tmp == null ? "" : tmp.getDescription();
                  * description.setText(desc);
                  */
                 description.setText(timer.getDescription());
             } else {
                 Date date = new Date(timer.getStartTime());
                 Program prog = Plugin.getPluginManager().getProgram(date,
                         timer.getTvBrowserProgIDs().get(0));
                 description.setText(prog.getDescription());
                 description.append("\n\n" + prog.getChannel().getCopyrightNotice());
             }
         }
     }
 }
