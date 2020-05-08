 /*
  * OffenePflege
  * Copyright (C) 2006-2012 Torsten Löhr
  * This program is free software; you can redistribute it and/or modify it under the terms of the 
  * GNU General Public License V2 as published by the Free Software Foundation
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even 
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program; if not, write to 
  * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
  * www.offene-pflege.de
  * ------------------------ 
  * Auf deutsch (freie Übersetzung. Rechtlich gilt die englische Version)
  * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU General Public License, 
  * wie von der Free Software Foundation veröffentlicht, weitergeben und/oder modifizieren, gemäß Version 2 der Lizenz.
  *
  * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein wird, aber 
  * OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN 
  * BESTIMMTEN ZWECK. Details finden Sie in der GNU General Public License.
  *
  * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm erhalten haben. Falls nicht, 
  * schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
  * 
  */
 package op;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jidesoft.pane.CollapsiblePane;
 import com.jidesoft.pane.CollapsiblePanes;
 import com.jidesoft.plaf.basic.ThemePainter;
 import com.jidesoft.status.LabelStatusBarItem;
 import com.jidesoft.status.MemoryStatusBarItem;
 import com.jidesoft.status.StatusBar;
 import com.jidesoft.status.TimeStatusBarItem;
 import com.jidesoft.swing.JideBoxLayout;
 import com.jidesoft.swing.JideButton;
 import com.jidesoft.swing.JideSplitPane;
 import entity.Station;
 import entity.StationTools;
 import entity.info.*;
 import entity.prescription.PrescriptionTools;
 import entity.system.SYSLoginTools;
 import entity.system.SYSPropsTools;
 import op.allowance.PnlAllowance;
 import op.care.PnlCare;
 import op.care.info.PnlInformation;
 import op.care.med.structure.PnlMed;
 import op.care.supervisor.PnlHandover;
 import op.controlling.PnlControlling;
 import op.dev.PnlDev;
 import op.process.PnlProcess;
 import op.settings.PnlSystemSettings;
 import op.settings.PnlUserSettings;
 import op.system.DlgLogin;
 import op.system.InternalClassACL;
 import op.threads.DisplayManager;
 import op.threads.DisplayMessage;
 import op.threads.PrintProcessor;
 import op.tools.*;
 import op.users.PnlUser;
 import op.welcome.PnlWelcome;
 import org.apache.commons.collections.Closure;
 import org.apache.commons.io.FileUtils;
 import org.jdesktop.swingx.VerticalLayout;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.swing.*;
 import javax.swing.border.SoftBevelBorder;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyVetoException;
 import java.io.File;
 import java.net.URI;
 import java.text.DateFormat;
 import java.util.*;
 
 /**
  * @author __USER__
  */
 public class FrmMain extends JFrame {
     public static final String internalClassID = "opde.mainframe";
     private boolean initPhase;
     private ArrayList<CollapsiblePane> listOfNursingrecords;
 
     private Map<Integer, Set<Resident>> specialities;
     private Map<Resident, JPanel> iconPanels;
 
     private DlgLogin dlgLogin;
 
     public DisplayManager getDisplayManager() {
         return displayManager;
     }
 
     private DisplayManager displayManager;
     private PrintProcessor printProcessor;
 
     public PrintProcessor getPrintProcessor() {
         return printProcessor;
     }
 
     private CleanablePanel currentVisiblePanel;
     private Resident currentResident;
     private String currentClassname;
     private JideButton previousProgButton;
     private LabelStatusBarItem labelUSER;
     private JScrollPane jspSearch, jspApps;
     private CollapsiblePanes panesSearch, panesApps;
     //    private HashMap<Resident, JideButton> bwButtonMap;
     private JideButton homeButton;
 
     public FrmMain() {
         initPhase = true;
         initComponents();
 
         currentVisiblePanel = null;
         currentResident = null;
         lblWait.setText(OPDE.lang.getString("misc.msg.wait"));
         lblWait.setVisible(false);
         listOfNursingrecords = new ArrayList<CollapsiblePane>();
         btnHelp.setToolTipText(OPDE.lang.getString("opde.mainframe.btnHelp.tooltip"));
 
         iconPanels = Collections.synchronizedMap(new HashMap<Resident, JPanel>());
 
         if (OPDE.isDebug()) {
             setSize(1366, 768);
         } else {
             this.setExtendedState(JFrame.MAXIMIZED_BOTH);
         }
 
         setTitle(SYSTools.getWindowTitle(""));
 
         displayManager = new DisplayManager(pbMsg, lblMainMsg, lblSubMsg, pnlIcons);
         displayManager.start();
 
         printProcessor = new PrintProcessor();
         printProcessor.start();
 
 
         // StatusBar Setup
         final LabelStatusBarItem label = new LabelStatusBarItem("Line");
         label.setText(OPDE.getUrl() + " [Build " + OPDE.getAppInfo().getBuildnum() + "]");
         if (OPDE.isCustomUrl()) {
             label.setForeground(Color.RED);
         }
         label.setFont(new Font("Arial", Font.PLAIN, 14));
         statusBar.add(label, JideBoxLayout.FLEXIBLE);
         labelUSER = new LabelStatusBarItem("Line");
         labelUSER.setText("--");
         labelUSER.setAlignment(JLabel.CENTER);
         labelUSER.setFont(new Font("Arial", Font.PLAIN, 14));
         statusBar.add(labelUSER, JideBoxLayout.FLEXIBLE);
         final TimeStatusBarItem time = new TimeStatusBarItem();
         time.setFont(new Font("Arial", Font.PLAIN, 14));
         time.setUpdateInterval(10000);
         time.setTextFormat(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT));
         time.setAlignment(JLabel.CENTER);
         statusBar.add(time, JideBoxLayout.FLEXIBLE);
         final MemoryStatusBarItem gc = new MemoryStatusBarItem();
         gc.setFont(new Font("Arial", Font.PLAIN, 14));
         statusBar.add(gc, JideBoxLayout.FLEXIBLE);
         initPhase = false;
     }
 
     public void completeRefresh() {
         emptyFrame();
         afterLogin();
     }
 
 
     public void emptyFrame() {
         if (currentVisiblePanel != null) {
             pnlCard.remove(currentVisiblePanel);
             lblWait.setVisible(false);
         }
         currentVisiblePanel = null;
         displayManager.clearAllMessages();
 
         emptySearchArea();
 
     }
 
     public void emptySearchArea() {
         jspSearch.removeAll();
         jspSearch = null;
         jspApps.removeAll();
         jspApps = null;
 
         panesSearch.removeAll();
         panesSearch = null;
         panesApps.removeAll();
         panesApps = null;
         splitPaneLeft.removeAll();
     }
 
     private void btnExitActionPerformed(ActionEvent e) {
         logout();
         showLogin();
     }
 
     private void thisWindowClosing(WindowEvent e) {
         if (OPDE.getLogin() != null) {
             logout();
         }
         // Delete the pidfile if present
         FileUtils.deleteQuietly(new File(OPDE.getOPWD() + File.separatorChar + "opde.pid"));
         System.exit(0);
     }
 
     @Override
     public void setVisible(boolean b) {
         super.setVisible(b);
 
         if (b) {
             showLogin();
         }
     }
 
     private void btnReloadActionPerformed(ActionEvent e) {
         if (currentVisiblePanel != null) {
             currentVisiblePanel.reload();
         }
 
     }
 
     private void splitPaneLeftPropertyChange(PropertyChangeEvent e) {
         if (!initPhase) {
             SYSPropsTools.storeProp("opde.mainframe:splitPaneLeftDividerLocation", SYSTools.getDividerInRelativePosition(splitPaneLeft).toString(), OPDE.getLogin().getUser());
         }
     }
 
     private void btnHelpActionPerformed(ActionEvent e) {
         if (currentVisiblePanel != null && Desktop.isDesktopSupported() && currentVisiblePanel.getInternalClassID() != null && OPDE.lang.containsKey(currentVisiblePanel.getInternalClassID() + ".helpurl")) {
             try {
                 URI uri = new URI(OPDE.lang.getString(currentVisiblePanel.getInternalClassID() + ".helpurl"));
                 Desktop.getDesktop().browse(uri);
             } catch (Exception ex) {
                 OPDE.getDisplayManager().addSubMessage(new DisplayMessage("opde.mainframe.noHelpAvailable"));
             }
         } else {
             OPDE.getDisplayManager().addSubMessage(new DisplayMessage("opde.mainframe.noHelpAvailable"));
         }
     }
 
     private void btnTXActionPerformed(ActionEvent e) {
         if (currentResident != null) {
 
             new TXEssenDoc(currentResident);
 
 //            if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
 //                SYSFilesTools.print(ResInfoTools.getTXReport(currentResident, true, true, true, true, true, true, true, true, true), true);
 //            } else {
 //
 //
 //            }
 //
 
 
         } else {
             displayManager.addSubMessage(new DisplayMessage("misc.msg.choose.a.resident.first"));
         }
     }
 
     public void removeSpeciality(ResInfoType type, final Resident resident) {
         if (!type.isAlertType() && type.getType() != ResInfoTypeTools.TYPE_ABSENCE) {
             return;
         }
         synchronized (specialities) {
             specialities.get(type.getType()).remove(resident);
         }
         synchronized (iconPanels) {
             setIconPanel(iconPanels.get(resident), resident);
         }
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 iconPanels.get(resident).repaint();
             }
         });
     }
 
     public void addSpeciality(ResInfoType type, final Resident resident) {
         if (!type.isAlertType() && type.getType() != ResInfoTypeTools.TYPE_ABSENCE) {
             return;
         }
         synchronized (specialities) {
             specialities.get(type.getType()).add(resident);
         }
         synchronized (iconPanels) {
             setIconPanel(iconPanels.get(resident), resident);
         }
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 iconPanels.get(resident).repaint();
             }
         });
 
     }
 
     public void afterLogin() {
         dlgLogin = null;
 
         if (specialities != null) {
             synchronized (specialities) {
                 SYSTools.clear(specialities);
             }
         }
 
         specialities = Collections.synchronizedMap(new HashMap<Integer, Set<Resident>>());
         synchronized (specialities) {
             specialities.put(ResInfoTypeTools.TYPE_ABSENCE, new HashSet<Resident>());
             specialities.put(ResInfoTypeTools.TYPE_INFECTION, new HashSet<Resident>());
             specialities.put(ResInfoTypeTools.TYPE_WARNING, new HashSet<Resident>());
             specialities.put(ResInfoTypeTools.TYPE_ALLERGY, new HashSet<Resident>());
             specialities.put(ResInfoTypeTools.TYPE_DIABETES, new HashSet<Resident>());
 
             for (ResInfo info : ResInfoTools.getSpecialInfos()) {
                 specialities.get(info.getResInfoType().getType()).add(info.getResident());
             }
         }
 
         prepareSearchArea();
         labelUSER.setText(OPDE.getLogin().getUser().getFullname());
 
         Runnable runnable = new Runnable() {
             @Override
             public void run() {
                 initPhase = true;
                 double pos;
                 try {
                     pos = Double.parseDouble(OPDE.getProps().getProperty("opde.mainframe:splitPaneLeftDividerLocation"));
                 } catch (Exception e) {
                     pos = 0.5d;
                 }
                 splitPaneLeft.setDividerLocation(0, SYSTools.getDividerInAbsolutePosition(splitPaneLeft, pos));
                 initPhase = false;
                 homeButton.doClick();
             }
         };
 
         SwingUtilities.invokeLater(runnable);
     }
 
     /**
      * This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the PrinterForm Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         pnlMain = new JPanel();
         pnlMainMessage = new JPanel();
         btnTX = new JButton();
         panel1 = new JPanel();
         pnlIcons = new JPanel();
         lblMainMsg = new JLabel();
         btnExit = new JButton();
         lblSubMsg = new FadingLabel();
         btnHelp = new JButton();
         pbMsg = new JProgressBar();
         btnReload = new JButton();
         splitPaneLeft = new JideSplitPane();
         pnlCard = new JPanel();
         pnlWait = new JPanel();
         lblWait = new JLabel();
         statusBar = new StatusBar();
 
         //======== this ========
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setTitle("Offene-Pflege.de");
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 thisWindowClosing(e);
             }
         });
         Container contentPane = getContentPane();
         contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
 
         //======== pnlMain ========
         {
             pnlMain.setLayout(new FormLayout(
                     "0dlu, $lcgap, pref, $lcgap, left:default:grow, 2*($rgap)",
                     "$rgap, default, $rgap, default:grow, $lgap, pref, $lgap, 0dlu"));
 
             //======== pnlMainMessage ========
             {
                 pnlMainMessage.setBackground(new Color(220, 223, 208));
                 pnlMainMessage.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
                 pnlMainMessage.setLayout(new FormLayout(
                         "0dlu, $lcgap, 23dlu, $lcgap, default:grow, $lcgap, min, $lcgap, 0dlu",
                         "0dlu, $lgap, pref, $lgap, fill:11dlu, $lgap, pref, $lgap, 0dlu"));
 
                 //---- btnTX ----
                 btnTX.setIcon(new ImageIcon(getClass().getResource("/artwork/32x32/ambulance2.png")));
                 btnTX.setBorder(null);
                 btnTX.setBorderPainted(false);
                 btnTX.setSelectedIcon(null);
                 btnTX.setToolTipText("Verlegungsbericht drucken");
                 btnTX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 btnTX.setPressedIcon(new ImageIcon(getClass().getResource("/artwork/32x32/ambulance2_pressed.png")));
                 btnTX.setContentAreaFilled(false);
                 btnTX.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnTXActionPerformed(e);
                     }
                 });
                 pnlMainMessage.add(btnTX, CC.xywh(3, 3, 1, 3));
 
                 //======== panel1 ========
                 {
                     panel1.setOpaque(false);
                     panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
 
                     //======== pnlIcons ========
                     {
                         pnlIcons.setOpaque(false);
                         pnlIcons.setLayout(new BoxLayout(pnlIcons, BoxLayout.LINE_AXIS));
                     }
                     panel1.add(pnlIcons);
 
                     //---- lblMainMsg ----
                     lblMainMsg.setText("OPDE");
                     lblMainMsg.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 22));
                     lblMainMsg.setForeground(new Color(105, 80, 69));
                     lblMainMsg.setHorizontalAlignment(SwingConstants.CENTER);
                     lblMainMsg.setIcon(null);
                     lblMainMsg.setHorizontalTextPosition(SwingConstants.LEADING);
                     panel1.add(lblMainMsg);
                 }
                 pnlMainMessage.add(panel1, CC.xy(5, 3, CC.CENTER, CC.DEFAULT));
 
                 //---- btnExit ----
                 btnExit.setIcon(new ImageIcon(getClass().getResource("/artwork/32x32/lock.png")));
                 btnExit.setBorder(null);
                 btnExit.setBorderPainted(false);
                 btnExit.setOpaque(false);
                 btnExit.setContentAreaFilled(false);
                 btnExit.setToolTipText("Abmelden");
                 btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 btnExit.setPressedIcon(new ImageIcon(getClass().getResource("/artwork/32x32/lock_pressed.png")));
                 btnExit.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnExitActionPerformed(e);
                     }
                 });
                 pnlMainMessage.add(btnExit, CC.xywh(7, 3, 1, 3));
 
                 //---- lblSubMsg ----
                 lblSubMsg.setText("OPDE");
                 lblSubMsg.setFont(new Font("Arial", Font.PLAIN, 14));
                 lblSubMsg.setForeground(new Color(105, 80, 69));
                 lblSubMsg.setHorizontalAlignment(SwingConstants.CENTER);
                 pnlMainMessage.add(lblSubMsg, CC.xy(5, 5));
 
                 //---- btnHelp ----
                 btnHelp.setText(null);
                 btnHelp.setIcon(new ImageIcon(getClass().getResource("/artwork/32x32/help.png")));
                 btnHelp.setBorderPainted(false);
                 btnHelp.setContentAreaFilled(false);
                 btnHelp.setPressedIcon(new ImageIcon(getClass().getResource("/artwork/32x32/help_pressed.png")));
                 btnHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 btnHelp.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnHelpActionPerformed(e);
                     }
                 });
                 pnlMainMessage.add(btnHelp, CC.xy(3, 7));
 
                 //---- pbMsg ----
                 pbMsg.setValue(50);
                 pbMsg.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
                 pbMsg.setForeground(new Color(105, 80, 69));
                 pnlMainMessage.add(pbMsg, CC.xy(5, 7, CC.FILL, CC.FILL));
 
                 //---- btnReload ----
                 btnReload.setIcon(new ImageIcon(getClass().getResource("/artwork/32x32/reload.png")));
                 btnReload.setBorder(null);
                 btnReload.setBorderPainted(false);
                 btnReload.setOpaque(false);
                 btnReload.setContentAreaFilled(false);
                 btnReload.setToolTipText("Ansicht aktualisieren");
                 btnReload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 btnReload.setPressedIcon(new ImageIcon(getClass().getResource("/artwork/32x32/reload_pressed.png")));
                 btnReload.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnReloadActionPerformed(e);
                     }
                 });
                 pnlMainMessage.add(btnReload, CC.xy(7, 7));
             }
             pnlMain.add(pnlMainMessage, CC.xywh(3, 2, 4, 1));
 
             //---- splitPaneLeft ----
             splitPaneLeft.setOneTouchExpandable(true);
             splitPaneLeft.setProportionalLayout(true);
             splitPaneLeft.setShowGripper(true);
             splitPaneLeft.addPropertyChangeListener("dividerLocation", new PropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent e) {
                     splitPaneLeftPropertyChange(e);
                 }
             });
             pnlMain.add(splitPaneLeft, CC.xy(3, 4, CC.FILL, CC.FILL));
 
             //======== pnlCard ========
             {
                 pnlCard.setLayout(new CardLayout());
 
                 //======== pnlWait ========
                 {
                     pnlWait.setLayout(new BorderLayout());
 
                     //---- lblWait ----
                     lblWait.setText("text");
                     lblWait.setFont(new Font("Arial", Font.BOLD, 22));
                     lblWait.setHorizontalAlignment(SwingConstants.CENTER);
                     pnlWait.add(lblWait, BorderLayout.CENTER);
                 }
                 pnlCard.add(pnlWait, "cardWait");
             }
             pnlMain.add(pnlCard, CC.xy(5, 4, CC.FILL, CC.FILL));
 
             //---- statusBar ----
             statusBar.setBackground(new Color(238, 238, 238));
             pnlMain.add(statusBar, CC.xywh(3, 6, 4, 1, CC.FILL, CC.FILL));
         }
         contentPane.add(pnlMain);
         setSize(945, 695);
         setLocationRelativeTo(getOwner());
     }// </editor-fold>//GEN-END:initComponents
 
     public void clearPreviousProgbutton() {
         if (previousProgButton != null) {
             previousProgButton.setBackground(Color.WHITE);
             previousProgButton.setOpaque(false);
         }
     }
 
     public JScrollPane prepareSearchArea() {
         // fixes #1
         if (panesApps != null) {
             panesApps.removeAll();
         }
         for (CollapsiblePane cp : listOfNursingrecords) {
             cp.removeAll();
         }
         listOfNursingrecords.clear();
 
         panesSearch = new CollapsiblePanes();
         panesSearch.setLayout(new JideBoxLayout(panesSearch, JideBoxLayout.Y_AXIS));
         jspSearch = new JScrollPane(panesSearch);
 
         panesApps = new CollapsiblePanes();
         panesApps.setLayout(new JideBoxLayout(panesApps, JideBoxLayout.Y_AXIS));
 
         homeButton = GUITools.createHyperlinkButton(OPDE.lang.getString(PnlWelcome.internalClassID), SYSConst.icon22home, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (previousProgButton != null) {
                     previousProgButton.setBackground(Color.WHITE);
                     previousProgButton.setOpaque(false);
                 }
                 currentResident = null;
                 previousProgButton = (JideButton) actionEvent.getSource();
                 previousProgButton.setBackground(Color.YELLOW);
                 previousProgButton.setOpaque(true);
                 displayManager.setMainMessage(OPDE.lang.getString(PnlWelcome.internalClassID));
                 displayManager.addSubMessage(new DisplayMessage(OPDE.lang.getString(PnlWelcome.internalClassID + ".longDescription")));
                 displayManager.clearAllIcons();
                 setPanelTo(new PnlWelcome(jspSearch));
             }
         });
         panesApps.add(homeButton);
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("SELECT s FROM Station s ORDER BY s.name");
         ArrayList<Station> stationen = new ArrayList<Station>(query.getResultList());
         em.close();
         for (Station station : stationen) {
             panesApps.add(addNursingRecords(station));
         }
 
         // May see the archive
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.ARCHIVE, PnlInformation.internalClassID)) {
             panesApps.add(addNursingRecords(null));
         }
 
         panesApps.addExpansion();
         jspApps = new JScrollPane(panesApps);
 
         splitPaneLeft.setOrientation(JideSplitPane.VERTICAL_SPLIT);
         splitPaneLeft.add(jspApps);
         splitPaneLeft.add(jspSearch);
         return jspSearch;
     }
 
     public CleanablePanel loadPanel(String classname) {
         CleanablePanel panel = null;
         currentResident = null;
         currentClassname = classname;
         if (classname.equals("op.allowance.PnlAllowance")) {
             panel = new PnlAllowance(jspSearch);
         } else if (classname.equals("op.process.PnlProcess")) {
             panel = new PnlProcess(jspSearch);
         } else if (classname.equals("op.care.med.structure.PnlMed")) {
             panel = new PnlMed(jspSearch);
         } else if (classname.equals("op.users.PnlUser")) {
             panel = new PnlUser(jspSearch);
         } else if (classname.equals("op.care.supervisor.PnlHandover")) {
             panel = new PnlHandover(jspSearch);
         } else if (classname.equals("op.welcome.PnlWelcome")) {
             panel = new PnlWelcome(jspSearch);
         } else if (classname.equals("op.controlling.PnlControlling")) {
             panel = new PnlControlling(jspSearch);
         } else if (classname.equals("op.settings.PnlSystemSettings")) {
             panel = new PnlSystemSettings(jspSearch);
         } else if (classname.equals("op.settings.PnlUserSettings")) {
             panel = new PnlUserSettings(jspSearch);
         } else if (classname.equals("op.dev.PnlDev")) {
             panel = new PnlDev();
         }
         return panel;
     }
 
     public String getCurrentClassname() {
         return currentClassname;
     }
 
     public void setCurrentClassname(String currentClassname) {
         this.currentClassname = currentClassname;
     }
 
     private CollapsiblePane addNursingRecords(final Station station) {
 //        bwButtonMap = new HashMap<Resident, JideButton>();
 
         EntityManager em = OPDE.createEM();
         Query query;
         if (station == null) {
             query = em.createQuery("SELECT b FROM Resident b WHERE b.station IS NULL ORDER BY b.name, b.firstname");
         } else {
             query = em.createQuery("SELECT b FROM Resident b WHERE b.station = :station ORDER BY b.name, b.firstname");
             query.setParameter("station", station);
         }
         ArrayList<Resident> residentList = new ArrayList<Resident>(query.getResultList());
         em.close();
 
         CollapsiblePane mypane = new CollapsiblePane(station == null ? OPDE.lang.getString("misc.msg.Archive") : station.getName());
         mypane.setFont(SYSConst.ARIAL14);
         mypane.setEmphasized(station != null && station.equals(StationTools.getStationForThisHost()));
         mypane.setStyle(CollapsiblePane.PLAIN_STYLE);
 
         JPanel labelPanel = new JPanel();
         labelPanel.setLayout(new VerticalLayout());
         labelPanel.setBackground(Color.WHITE);
 
         try {
             mypane.setCollapsed(!mypane.isEmphasized());
         } catch (PropertyVetoException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         if (!residentList.isEmpty() && station != null) {
             JideButton button = GUITools.createHyperlinkButton("opde.mainframe.printdailyplan", SYSConst.icon22print2, new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                         PrescriptionTools.printDailyPlan(station, "html");
                     } else {
                         PrescriptionTools.printDailyPlan(station, "pdf");
                     }
                 }
             });
             button.setBackground(Color.WHITE);
             labelPanel.add(button);
         }
 
         for (final Resident resident : residentList) {
             ActionListener actionListener = new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     if (currentResident != resident) {
 
                         if (previousProgButton != null) {
                             previousProgButton.setBackground(Color.WHITE);
                             previousProgButton.setOpaque(false);
                         }
 
                         previousProgButton = (JideButton) actionEvent.getSource();
                         previousProgButton.setBackground(Color.YELLOW);
                         previousProgButton.setOpaque(true);
 
                         currentResident = resident;
 
                         if (currentVisiblePanel instanceof PnlCare) {
                             ((NursingRecordsPanel) currentVisiblePanel).switchResident(resident);
                         } else {
                             setPanelTo(new PnlCare(resident, jspSearch));
                         }
 
                     }
                 }
             };
 
             JPanel singleButtonPanel = new JPanel();
             singleButtonPanel.setLayout(new BoxLayout(singleButtonPanel, BoxLayout.X_AXIS));
             singleButtonPanel.setBorder(null);
 
             singleButtonPanel.setOpaque(false);
 
             String titel = resident.getName() + ", " + resident.getFirstname() + " [" + resident.getRIDAnonymous() + "]";
             JideButton button = GUITools.createHyperlinkButton(titel, null, actionListener);
             button.setForegroundOfState(ThemePainter.STATE_DEFAULT, resident.getGender() == ResidentTools.FEMALE ? Color.red : Color.blue);
             button.setBackground(Color.WHITE);
 
             singleButtonPanel.add(button);
 
             OPDE.getDisplayManager().clearSubMessages();
 
             if (station != null) { // not for the archive
                 JPanel pnl = new JPanel();
                 pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
                 pnl.setOpaque(false);
                 pnl.setBorder(null);
 
                 setIconPanel(pnl, resident);
 
                 synchronized (iconPanels) {
                     iconPanels.put(resident, pnl);
                 }
 
                 singleButtonPanel.add(pnl);
             }
 
             labelPanel.add(singleButtonPanel);
         }
 
         mypane.setContentPane(labelPanel);
         listOfNursingrecords.add(mypane);
         return mypane;
     }
 
 
     private void setIconPanel(JPanel pnl, Resident resident) {
         pnl.removeAll();
         synchronized (specialities) {
             if (specialities.get(ResInfoTypeTools.TYPE_ABSENCE).contains(resident)) {
                 pnl.add(new JLabel(SYSConst.icon16residentAbsent));
             }
             if (specialities.get(ResInfoTypeTools.TYPE_INFECTION).contains(resident)) {
                 pnl.add(new JLabel(SYSConst.icon16biohazard));
             }
             if (specialities.get(ResInfoTypeTools.TYPE_DIABETES).contains(resident)) {
                 pnl.add(new JLabel(SYSConst.icon16diabetes));
             }
             if (specialities.get(ResInfoTypeTools.TYPE_ALLERGY).contains(resident)) {
                 pnl.add(new JLabel(SYSConst.icon16allergy));
             }
             if (specialities.get(ResInfoTypeTools.TYPE_WARNING).contains(resident)) {
                 pnl.add(new JLabel(SYSConst.icon16warning));
             }
         }
     }
 
 
     /**
      * fixes #1
      */
     public void collapseNursingRecords() {
         try {
             for (CollapsiblePane cp : listOfNursingrecords) {
                 cp.setCollapsed(true);
             }
         } catch (PropertyVetoException e) {
             // bah!
         }
     }
 
     public Resident getCurrentResident() {
         return currentResident;
     }
 
     public void setCurrentResident(Resident currentResident) {
         this.currentResident = currentResident;
     }
 
     public boolean isBlocked() {
         return lblWait.isVisible();
     }
 
     public void setBlocked(boolean blocked) {
         if (blocked) {
             lblWait.setVisible(true);
             JPanel glass = new JPanel();
             glass.addMouseListener(new MouseAdapter() {
             });
             glass.addMouseMotionListener(new MouseMotionAdapter() {
             });
             glass.addKeyListener(new KeyAdapter() {
             });
             glass.setOpaque(false);
             setGlassPane(glass);
             getGlassPane().setVisible(true);
             ((CardLayout) pnlCard.getLayout()).show(pnlCard, "cardWait");
         } else {
             getGlassPane().setVisible(false);
             setGlassPane(new JPanel());
             ((CardLayout) pnlCard.getLayout()).show(pnlCard, "cardContent");
         }
     }
 
     public Point getLocationForDialog(Dimension dimOfDialog) {
         Point point = new Point((getSize().width - dimOfDialog.width) / 2, pnlMainMessage.getHeight() + 10);
         SwingUtilities.convertPointToScreen(point, this);
         return point;
     }
 
     public void setPanelTo(CleanablePanel pnl) {
         if (currentVisiblePanel != null) {
             pnlCard.remove(currentVisiblePanel);
             currentVisiblePanel.cleanup();
         }
 
         currentVisiblePanel = pnl;
         pnlCard.add("cardContent", currentVisiblePanel);
         ((CardLayout) pnlCard.getLayout()).show(pnlCard, "cardContent");
     }
 
     public void dispose() {
         displayManager.interrupt();
         cleanup();
         super.dispose();
     }
 
     private void showLogin() {
         dlgLogin = new DlgLogin(new Closure() {
             @Override
             public void execute(Object o) {
                 if (o != null) {
                     afterLogin();
                 } else {
                     FileUtils.deleteQuietly(new File(OPDE.getOPWD() + File.separatorChar + "opde.pid"));
                     System.exit(0);
                 }
             }
         });
     }
 
     private void cleanup() {
         if (currentVisiblePanel != null) {
             currentVisiblePanel.cleanup();
         }
         synchronized (specialities) {
             specialities.clear();
         }
         synchronized (iconPanels) {
             iconPanels.clear();
         }
     }
 
     private void logout() {
         emptyFrame();
         OPDE.saveLocalProps();
 
         labelUSER.setText("--");
 
         SYSLoginTools.logout();
 
         // After the logout a forced garbage collection seems to be adequate
         System.gc();
         cleanup();
         SYSTools.checkForSoftwareupdates();
 
     }
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JPanel pnlMain;
     private JPanel pnlMainMessage;
     private JButton btnTX;
     private JPanel panel1;
     private JPanel pnlIcons;
     private JLabel lblMainMsg;
     private JButton btnExit;
     private FadingLabel lblSubMsg;
     private JButton btnHelp;
     private JProgressBar pbMsg;
     private JButton btnReload;
     private JideSplitPane splitPaneLeft;
     private JPanel pnlCard;
     private JPanel pnlWait;
     private JLabel lblWait;
     private StatusBar statusBar;
     // End of variables declaration//GEN-END:variables
 
 
 }
