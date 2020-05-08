 /**
  * Jin - a chess client for internet chess servers.
  * More information is available at http://www.jinchess.com/.
  * Copyright (C) 2003 Alexander Maryanovsky.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package free.jin.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import free.util.swing.UrlDisplayingAction;
 
 
 /**
  * The help menu on Jin's menubar.
  */
 
 public class HelpMenu extends JMenu{
 
 
 
   /**
    * The action listener that forwards all event handling to the private
    * <code>menuActivated</code> method.
    */
 
   private final ActionListener menuActionListener = new ActionListener(){
     public void actionPerformed(ActionEvent evt){
       menuActivated(evt.getActionCommand());
     }
   };
 
 
 
   /**
    * Creates a new <code>HelpMenu</code>.
    */
 
   public HelpMenu(){
     super("Help");
     setMnemonic('H');
 
     add(createWebsiteMenuItem());
     add(createCreditsMenuItem());
    add(createBugReportMenuItem());
    add(createFeatureRequestMenuItem());
     add(createAboutMenuItem());
   }
 
 
 
   /**
    * Responds to action events on the various menus.
    */
 
   private void menuActivated(String actionCommand){
     if ("credits".equals(actionCommand))
       new LicensePanel().display();
     else if ("about".equals(actionCommand))
       new AboutPanel().display();
     else
       throw new IllegalArgumentException("Unknown action command: " + actionCommand);
   }
 
 
 
   /**
    * Creates the "Tonic Website" menu item.
    */
 
   private JMenuItem createWebsiteMenuItem(){
     JMenuItem item = new JMenuItem("Tonic Website", 'J');
     item.addActionListener(new UrlDisplayingAction("http://tonic-chess.blogspot.com"));
     return item;
   }
 
 

   /**
    * Creates the "Credits and copyrights" menu item.
    */
 
   private JMenuItem createCreditsMenuItem(){
     JMenuItem item = new JMenuItem("Credits and Copyrights...", 'C');
     item.setActionCommand("credits");
     item.addActionListener(menuActionListener);
     return item;
   }
 
 

  /**
   * Creates the "Report a Bug" menu item.
   */

  private JMenuItem createBugReportMenuItem(){
    JMenuItem item = new JMenuItem("Report a Bug", 'R');
    item.addActionListener(new UrlDisplayingAction("http://sourceforge.net/tracker/?group_id=169064&atid=848921"));
    return item;
  }



  /**
   * Creates the "Suggest a Feature" menu item.
   */

  private JMenuItem createFeatureRequestMenuItem(){
    JMenuItem item = new JMenuItem("Suggest a Feature", 'S');
    item.addActionListener(new UrlDisplayingAction("http://sourceforge.net/tracker/?group_id=169064&atid=848924"));
    return item;
  }



   /**
    * Creates the "About..." menu item.
    */
 
   private JMenuItem createAboutMenuItem(){
     JMenuItem item = new JMenuItem("About Tonic...", 'A');
     item.setActionCommand("about");
     item.addActionListener(menuActionListener);
     return item;
   }
 
 
 
 }
