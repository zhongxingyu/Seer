/* $Id: TimerList.java,v 1.2 2005-11-26 12:49:39 hampelratte Exp $
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
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 
 import javax.swing.*;
 
 import lazybones.*;
 import lazybones.Timer;
 import util.ui.ProgramList;
 import devplugin.Channel;
 import devplugin.Date;
 import devplugin.Program;
 
 public class TimerList extends JDialog implements ActionListener {
 
     private JScrollPane scrollPane = null;
     private DefaultListModel model = new DefaultListModel();
     private ProgramList timerList = new ProgramList(model);
     private JButton buttonNew = null;
     private JButton buttonEdit = null;
     private JButton buttonRemove = null;
     
     private LazyBones control;
 
     public TimerList(LazyBones control) {
         super(control.getParent(), true);
         this.control = control;
         initGUI();
     }
 
     /**
      * This method initializes the GUI
      * 
      */
     private void initGUI() {
         this.getContentPane().setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         
         gbc.fill = java.awt.GridBagConstraints.BOTH;
         gbc.gridy = 0;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.gridwidth = 3;
         gbc.insets = new java.awt.Insets(10,10,10,10);
         gbc.gridx = 0;
         timerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         scrollPane = new JScrollPane(timerList);
         this.getContentPane().add(scrollPane, gbc);
         
         gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gbc.gridy = 1;
         gbc.weightx = 1.0;
         gbc.weighty = 0.1;
         gbc.gridwidth = 1;
         gbc.insets = new java.awt.Insets(0,10,10,10);
         gbc.gridx = 0;
         buttonNew = new JButton();
         buttonNew.setText("New Timer");
         buttonNew.addActionListener(this);
         this.getContentPane().add(buttonNew, gbc);
         
         gbc.insets = new java.awt.Insets(0,0,10,0);
         gbc.gridx = 1;
         buttonEdit = new JButton();
         buttonEdit.setText("Edit Timer");
         buttonEdit.addActionListener(this);
         this.getContentPane().add(buttonEdit, gbc);
         
         gbc.insets = new java.awt.Insets(0,10,10,10);
         gbc.gridx = 2;
         buttonRemove = new JButton();
         buttonRemove.setText("Delete Timer");
         buttonRemove.addActionListener(this);
         this.getContentPane().add(buttonRemove, gbc);
         
         this.setName("timerManager");
         this.setSize(new java.awt.Dimension(400,600));
         this.setTitle("Timer Manager");
         this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
         
         getTimers();
     }
     
     private void getTimers() {
         model.removeAllElements();
         ArrayList timers = TimerManager.getInstance().getTimers();
         
         ArrayList programs = new ArrayList();
         for (Iterator iter = timers.iterator(); iter.hasNext();) {
             Timer timer = (Timer) iter.next();
             Calendar time;
             time = timer.getStartTime();
             addProgramm(programs, timer, time);
         }
         
         Collections.sort(programs, new ProgramComparator());
         
         for (Iterator it = programs.iterator(); it.hasNext();) {
             Object element = it.next();
             model.addElement(element);
         }
     }
     
     private void addProgramm(ArrayList programs, Timer timer, Calendar time) {
         Channel chan = ProgramManager.getInstance().getChannel(timer);
        TimerProgram p = new TimerProgram(chan, new Date(time), time
                .get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
        p.setTitle(timer.getTitle());
        p.setDescription("");
        p.setTimer(timer);
        programs.add(p);
     }
 
     public void actionPerformed(ActionEvent e) {
         if(e.getSource() == buttonNew) {
             control.createTimer();
         } else if(e.getSource() == buttonEdit) {
             if(timerList.getSelectedIndex() >= 0) {
                 TimerProgram tp = (TimerProgram)timerList.getSelectedValue();
                 Timer timer = tp.getTimer();
                 control.editTimer(timer);
             }
         } else if(e.getSource() == buttonRemove) {
             if(timerList.getSelectedIndex() >= 0) {
                 TimerProgram tp = (TimerProgram)timerList.getSelectedValue();
                 Timer timer = tp.getTimer();
                 control.deleteTimer(timer);
             }
         }
         getTimers();
     }
     
     public void setVisible(boolean visible) {
         getTimers();
         super.setVisible(visible);
     }
     
     
     /**
      * 
      * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net</a>
      *
      * Compares two Programs according to their start time
      */
     private class ProgramComparator implements Comparator {
         public int compare(Object o1, Object o2) {
             if(o1 instanceof Program & o2 instanceof Program) {
                 Program p1 = (Program)o1;
                 Program p2 = (Program)o2;
                 Calendar c1 = getStartTime(p1);
                 Calendar c2 = getStartTime(p2);
                 if(c1.getTimeInMillis() < c2.getTimeInMillis()) {
                     return -1;
                 } else if (c1.getTimeInMillis() > c2.getTimeInMillis()) {
                     return 1;
                 }
             }
             return 0;
         }
         
         /**
          * 
          * @param p a Program object
          * @return The start time of the Program
          */
         private Calendar getStartTime(Program p) {
             Calendar calendar = GregorianCalendar.getInstance();
             Date d = p.getDate();
             calendar.set(Calendar.DAY_OF_MONTH, d.getDayOfMonth());
             calendar.set(Calendar.MONTH, d.getMonth());
             calendar.set(Calendar.YEAR, d.getYear());
             calendar.set(Calendar.HOUR_OF_DAY, p.getHours());
             calendar.set(Calendar.MINUTE, p.getMinutes());
             return calendar;
         }
         
     }
 }
