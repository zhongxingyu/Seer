 /*
  * Copyright (C) 2009  Lars Pötter <Lars_Poetter@gmx.de>
  * All Rights Reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 package org.FriendsUnited.UserInterface;
 
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 
 /**
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class XmlEditPanel implements ActionListener
 {
     private final Logger log = Logger.getLogger(this.getClass().getName());
     private final JComponent MyPanel;
     private Element xml;
     private JLabel Label;
     private JTextField ValueText;
     private final NodeConnection NodeCon;
 
     /**
      *
      */
     public XmlEditPanel(final NodeConnection NodeCon)
     {
         this.NodeCon = NodeCon;
         Label = new JLabel("Value:", JLabel.TRAILING);
         MyPanel = new JPanel();
         MyPanel.setLayout(new BoxLayout(MyPanel, BoxLayout.Y_AXIS));
         ValueText = new JTextField();
         ValueText.setColumns(30);
         Label.setLabelFor(ValueText);
 
 
         //Layout the labels and text fields in a panel.
         final JPanel fieldPane = new JPanel();
         final FlowLayout flayout = new FlowLayout();
         fieldPane.setLayout(flayout);
         fieldPane.add(Label);
         fieldPane.add(ValueText);
         fieldPane.setAlignmentX(Component.CENTER_ALIGNMENT);
         /*
         GroupLayout layout = new GroupLayout(fieldPane);
         fieldPane.setLayout(layout);
         layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
         layout.setHorizontalGroup(
                 layout.createSequentialGroup()
                    .addComponent(Label)
                    .addComponent(ValueText)
              );
              layout.setVerticalGroup(
                 layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                         .addComponent(Label)
                         .addComponent(ValueText)
              );
              */
 
         // Layout the buttons in a panel
         final JButton save = new JButton("Save");
         save.setActionCommand("save");
         save.addActionListener(this);
         save.setAlignmentX(Component.CENTER_ALIGNMENT);
         final JButton add = new JButton("Add");
         add.setActionCommand("add");
         add.addActionListener(this);
         add.setAlignmentX(Component.CENTER_ALIGNMENT);
         final JButton delete = new JButton("Delete");
         delete.setActionCommand("delete");
         delete.addActionListener(this);
         delete.setAlignmentX(Component.CENTER_ALIGNMENT);
 
         final FlowLayout blayout = new FlowLayout();
         final JPanel buttonPane = new JPanel(blayout);
         buttonPane.add(add);
         buttonPane.add(delete);
         buttonPane.add(save);
         buttonPane.setAlignmentX(Component.CENTER_ALIGNMENT);
 
         //Put the panels in this panel, labels on left,
         //text fields on right, buttons on the bottom.
         MyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         MyPanel.add(Box.createVerticalGlue());
         MyPanel.add(fieldPane);
         MyPanel.add(Box.createVerticalGlue());
         MyPanel.add(buttonPane);
     }
 
     public final JComponent getPanel()
     {
         log.debug("xep =" + MyPanel.toString());
         return MyPanel;
     }
 
     public final void setElement(final Element newxml)
     {
         // Get Node
        if(null != newxml)
        {
            log.debug("set Element " + newxml.getName());
            this.xml = newxml;
            // Fill in Information
            Label.setText(xml.getName());
            ValueText.setText(xml.getText());
            // Display changes
            MyPanel.validate();
        }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public final void actionPerformed(final ActionEvent e)
     {
         if(true == "save".equals(e.getActionCommand()))
         {
             // Save Button has been pressed
             String TagName = xml.getName();
             log.info("creating " + TagName);
             final Element res = new Element(TagName);
             res.addContent(ValueText.getText());
             // Wrap result up for transmission
             Element packet = res;
             Element curPos = xml.getParentElement();
             while(null != curPos)
             {
                 TagName = curPos.getName();
                 log.info("Adding " + TagName);
                 final Element wrap = new Element(TagName);
                 wrap.addContent(packet);
                 curPos = curPos.getParentElement();
                 packet = wrap;
             }
             if(true == "Reply".equals(packet.getName()))
             {
                 // Remove Request
                 log.info("Removing Reply");
                 final List<Element> le = packet.getChildren();
                 packet = le.get(0);
                 packet = (Element) packet.detach();
             }
             // Send Change to Node
             final Element change = new Element("Change");
             change.addContent(packet);
             NodeCon.executeRequest(change);
         }
     }
 }
