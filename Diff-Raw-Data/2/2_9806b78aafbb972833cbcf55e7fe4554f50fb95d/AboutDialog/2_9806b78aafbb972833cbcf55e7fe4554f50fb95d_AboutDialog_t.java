 //----------------------------------------------------------------------------
 // $Id$
 // $Source$
 //----------------------------------------------------------------------------
 
 package gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.net.URL;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.*;
 import version.*;
 import utils.GuiUtils;
 import utils.Platform;
 
 //----------------------------------------------------------------------------
 
 public class AboutDialog
     extends JOptionPane
 {
     public static void show(Component parent, String name, String version,
                             String protocolVersion, String command)
     {
         AboutDialog aboutDialog =
             new AboutDialog(name, version, protocolVersion, command);
         JDialog dialog = aboutDialog.createDialog(parent, "About");
         dialog.setVisible(true);
         dialog.dispose();
     }
 
     private AboutDialog(String name, String version, String protocolVersion,
                         String command)
     {
         JTabbedPane tabbedPane = new JTabbedPane();
         ClassLoader classLoader = getClass().getClassLoader();
         URL imageUrl = classLoader.getResource("images/project-support.png");
         String projectUrl = "http://gogui.sourceforge.net";
         String supportUrl =
             "http://sourceforge.net/donate/index.php?group_id=59117";
         JPanel goguiPanel =
             createPanel("<p align=\"center\"><b>GoGui " + Version.get()
                         + "</b></p>" +
                         "<p align=\"center\">" +
                         "Graphical interface to Go programs<br>" +
                         "&copy; 2003-2004, Markus Enzenberger" +
                         "</p>" +
                         "<p align=\"center\">" +
                         "<tt><a href=\"" + projectUrl + "\">"
                         + projectUrl + "</a></tt>" +
                         "</p>" +
                         "<p align=\"center\">" +
                         "<a href=\"" + supportUrl + "\">"
                         + "<img src=\"" + imageUrl + "\" border=\"0\"></a>" +
                         "</p>");
         tabbedPane.add("GoGui", goguiPanel);
         tabbedPane.setMnemonicAt(0, KeyEvent.VK_G);
         boolean isProgramAvailable = (name != null && ! name.equals(""));
         JPanel programPanel;
         if (isProgramAvailable)
         {
             String fullName = name;
            if (version != null && ! version.equals(""))
                 fullName = fullName + " " + version;
             int width = GuiUtils.getDefaultMonoFontSize() * 25;
             programPanel =
                 createPanel("<p align=\"center\"><b>" + fullName
                             + "</b></p>" +
                             "<p align=\"center\" width=\"" + width + "\">"
                             + "Command: <tt>" + command + "</tt></p>" +
                             "<p align=\"center\">" +
                             "GTP protocol version " + protocolVersion +
                             "</p>");
         }
         else
             programPanel = new JPanel();
         tabbedPane.add("Go Program", programPanel);
         tabbedPane.setMnemonicAt(1, KeyEvent.VK_P);
         if (! isProgramAvailable)
             tabbedPane.setEnabledAt(1, false);
         setMessage(tabbedPane);
         setOptionType(DEFAULT_OPTION);
     }
 
     private static JPanel createPanel(String text)
     {
         JPanel panel = new JPanel(new GridLayout(1, 1));
         JEditorPane editorPane = new JEditorPane();
         editorPane.setBorder(GuiUtils.createEmptyBorder());        
         editorPane.setEditable(false);
         panel.add(editorPane);
         JLabel dummyLabel = new JLabel();
         editorPane.setBackground(dummyLabel.getBackground());
         EditorKit editorKit =
             JEditorPane.createEditorKitForContentType("text/html");
         editorPane.setEditorKit(editorKit);
         editorPane.setText(text);
         editorPane.addHyperlinkListener(new HyperlinkListener()
             {
                 public void hyperlinkUpdate(HyperlinkEvent event)
                 {
                     HyperlinkEvent.EventType type = event.getEventType();
                     if (type == HyperlinkEvent.EventType.ACTIVATED)
                     {
                         URL url = event.getURL();
                         if (! Platform.openInExternalBrowser(url))
                             SimpleDialogs.showError(null,
                                                     "Could not open URL"
                                                     + " in external browser");
                     }
                 }
             });
         return panel;
     }
 }
 
 //----------------------------------------------------------------------------
