 /**
  * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
  *
  * This file is part of Snooze, a scalable, autonomic, and
  * energy-aware virtual machine (VM) management framework.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation, either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses>.
  */
 package org.inria.myriads.snoozeclient.systemtree.popup;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.HeadlessException;
 import java.awt.Insets;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.UUID;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.inria.myriads.snoozeclient.systemtree.SystemTreeVisualizer;
 import org.inria.myriads.snoozecommon.communication.groupmanager.repository.GroupLeaderRepositoryInformation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * 
  * PopupComponent.
  * 
  * @author msimonin
  *
  */
 /**
  * @author msimonin
  *
  */
 public abstract class PopupComponent extends JFrame 
 {
     
     /** Define the logger. */
     private static final Logger log_ = LoggerFactory.getLogger(GroupManagerPopupComponent.class);
     
     /** Summary panel. */
     protected JPanel usedSummaryPanel_;
     
     /** Main panel. */
     private JPanel hostDescriptionPanel_;
         
     /** The Component id. */
     private String popupComponentId_;
     
     /** The system tree visualizer. */
     private SystemTreeVisualizer systemTreeVisualizer_;
     
     
     /**
      * 
      * Constructor.
      * 
      * @param systemTreeVisualizer      The system Tree Visualizer.
      * @throws HeadlessException        exception
      */
     public PopupComponent(SystemTreeVisualizer systemTreeVisualizer) throws HeadlessException
     {
         super("Host Description");        
         systemTreeVisualizer_ = systemTreeVisualizer;
         initializeGui();
         popupComponentId_ = UUID.randomUUID().toString();
     }
 
 
 
     /**
      * 
      * Constructor.
      * 
      * @param title                 The title.
      * @throws HeadlessException    Exception
      */
     public PopupComponent(String title) throws HeadlessException 
     {
         super(title);
     }
 
 
 
     /**
      * 
      * Initializes GUI.
      * 
      */
     public void initializeGui()
     {
         
         setResizable(false);
         setDefaultLookAndFeelDecorated(false);
         this.setPreferredSize(new Dimension(800, 600));
         GridBagLayout hostLayout = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         setLayout(hostLayout);
         
         hostDescriptionPanel_ = new JPanel();
         hostDescriptionPanel_.setBorder(BorderFactory.createTitledBorder("Host description"));
         c.gridx = 0; 
         c.gridy = 0;
         c.gridwidth = 1;
         c.gridheight = 1;
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.NORTHWEST;
         c.weightx = 10;
         c.weighty = 10;
         add(hostDescriptionPanel_, c);
         
         usedSummaryPanel_ = new JPanel();
         usedSummaryPanel_.setBorder(BorderFactory.createTitledBorder("Summary Information"));
         c.gridy = 1;
         c.weightx = 90;
         c.weighty = 90;
         c.insets = new Insets(2, 2, 2, 2);
         add(usedSummaryPanel_, c);
         pack();
         
         addWindowListener(new WindowListener() {
             
             public void windowActivated(WindowEvent arg0) 
             {
             }
             public void windowClosing(WindowEvent arg0) 
             {
                 log_.debug("===============");
                 log_.debug("Closing Windows");
                 systemTreeVisualizer_.remove(popupComponentId_);
                 dispose();
             }
             public void windowDeactivated(WindowEvent arg0) 
             {
             }
             public void windowDeiconified(WindowEvent arg0) 
             {
             }
             public void windowIconified(WindowEvent arg0) 
             {
             }
             public void windowOpened(WindowEvent arg0) 
             {
             }
             public void windowClosed(WindowEvent e) 
             {
             }
         });       
     }
 
     /**
      * 
      * Display the popup.
      * 
      */
     public void display() 
     {
          pack();
          setVisible(true);
     }
 
     /**
      * @return the popupComponentId_
      */
     public String getPopupComponentId() 
     {
         return popupComponentId_;
     }
 
 
 
     /**
      * 
      * Sets the popup component id.
      * 
      * @param popupComponentId the popupComponentId to set
      */
     public void setPopupComponentId(String popupComponentId) 
     {
         popupComponentId_ = popupComponentId;
     }
     
     /**
      * 
      * Update the popup component.
      * 
      * @param hierarchy         The hierarchy.
      * @return                  true if everything ok.
      */
     public abstract boolean update(GroupLeaderRepositoryInformation hierarchy);
     
     /**
      * 
      * initializes the host panel.
      * 
      * @return          true if everything ok.
      */
     abstract boolean initializeHostPanel();
 
 
 
     /**
      * 
      * Gets the host panel.
      * 
      * @return the hostDescriptionPanel_
      */
     public JPanel getHostDescriptionPanel() 
     {
         return hostDescriptionPanel_;
     }
 
 
 
     /**
      * 
      * Sets the host panel.
      * 
      * @param hostDescriptionPanel the hostDescriptionPanel to set
      */
     public void setHostDescriptionPanel(JPanel hostDescriptionPanel)
     {
         hostDescriptionPanel_ = hostDescriptionPanel;
     }
 }
