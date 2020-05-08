 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of the wingS demo (http://wings.mercatis.de).
  *
  * The wingS demo is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package wingset;
 
 import java.awt.event.*;
 import java.io.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.swing.*;
 
 import org.wings.*;
 import org.wings.io.Device;
 import org.wings.io.ServletDevice;
 import org.wings.servlet.*;
 import org.wings.session.*;
 import org.wings.util.*;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class WingSetSession
     extends SessionServlet
     implements SConstants
 {
     SLabel timeMeasure = new SLabel();
 
 
     public WingSetSession(Session session) {
         super(session);
         System.out.println("I`m starting now");
     }
 
 
     public void postInit(ServletConfig config) {
         initGUI();
     }
 
     void initGUI() {
         SContainer contentPane = getFrame().getContentPane();
         contentPane.setLayout(new SFlowDownLayout());
 
         STabbedPane tab = new STabbedPane();
 
         tab.add(new LabelExample(), "Label Example");
         tab.add(new BorderExample(), "Border Example");
         tab.add(new TextComponentExample(), "Text Component Example");
         tab.add(new TreeExample(), "Tree Example");
         tab.add(new OptionPaneExample(getFrame()), "OptionPane Example");
         tab.add(new TableExample(), "Table Example");
         tab.add(new ListExample(), "List Example");
         tab.add(new ButtonExample(), "Button Example");
         tab.add(new CheckBoxExample(), "CheckBox Example");
         tab.add(new RadioButtonExample(), "RadioButton Example");
         tab.add(new FileChooserExample(), "FileChooser Example");
         tab.add(new ScrollPaneExample(), "ScrollPane Example");
         tab.add(new TemplateExample(), "Template Example");
         tab.add(new SLabel("temporarily disabled"), "DateChooser Example");
         //tab.add(new DateChooserExample(), "DateChooser Example");
 
         contentPane.add(tab);
         contentPane.add(new SSeparator());
 
         SPanel south = new SPanel();
         south.add(timeMeasure);
 
         final SRadioButton old = new SRadioButton("xhtml/old");
         old.setSelected(false);
         final SRadioButton css1 = new SRadioButton("xhtml/css1");
         css1.setSelected(true);
 
         SButtonGroup group = new SButtonGroup();
         group.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                    try {
                        if (css1.isSelected())
                            getSession().getCGManager()
                                .setLookAndFeel(new org.wings.plaf.xhtml.css1.CSS1LookAndFeel());
                        else
                            getSession().getCGManager()
                                .setLookAndFeel(new org.wings.plaf.xhtml.old.OldLookAndFeel());
                    }
                    catch (UnsupportedLookAndFeelException ulafe) {}
                 }
             });
         group.add(old);
         group.add(css1);
 
         south.add(old);
         south.add(new SSpacer(1));
         south.add(css1);
 
         contentPane.add(south);
     }
 
     /**
      * this function handles the request from a HTML-Browser
      * nearly all servlet functionality is in here
      */
     public void processRequest(HttpServletRequest req,
                                HttpServletResponse res)
         throws ServletException, IOException
         {
             measure.start("time to generate HTML Code ");
             // then write the data of the response
             // Dies ist ein Dummy-Aufruf, um die Zeit zu messen; es wird
             // sozusagen die Seite zweimal aufgebaut, denn die Ausgabe der
             // Zeit ist ja selbst wieder eine Component.
             String erg = getFrame().show();
             measure.stop();
             timeMeasure.setText(measure.print());
             measure.reset();
         }
 
     /**
      * Servletinfo
      */
     public String getServletInfo() {
         return "WingSet ($Revision$)";
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
