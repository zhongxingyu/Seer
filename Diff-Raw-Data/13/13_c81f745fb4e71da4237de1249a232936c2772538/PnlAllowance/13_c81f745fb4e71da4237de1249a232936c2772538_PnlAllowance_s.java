 /*
  * Offene-Pflege.de (OPDE)
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
 package op.allowance;
 
 import com.jidesoft.pane.CollapsiblePane;
 import com.jidesoft.pane.CollapsiblePanes;
 import com.jidesoft.pane.event.CollapsiblePaneAdapter;
 import com.jidesoft.pane.event.CollapsiblePaneEvent;
 import com.jidesoft.popup.JidePopup;
 import com.jidesoft.swing.JideBoxLayout;
 import com.jidesoft.swing.JideButton;
 import entity.Allowance;
 import entity.AllowanceTools;
 import entity.files.SYSFilesTools;
 import entity.info.Resident;
 import entity.info.ResidentTools;
 import op.OPDE;
 import op.system.InternalClassACL;
 import op.threads.DisplayManager;
 import op.threads.DisplayMessage;
 import op.tools.*;
 import org.apache.commons.collections.Closure;
 import org.jdesktop.swingx.JXSearchField;
 import org.jdesktop.swingx.VerticalLayout;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 
 import javax.persistence.EntityManager;
 import javax.persistence.LockModeType;
 import javax.persistence.OptimisticLockException;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.beans.PropertyVetoException;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.Format;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  * @author tloehr
  */
 public class PnlAllowance extends CleanablePanel {
     public static final String internalClassID = "admin.residents.cash";
     NumberFormat cf = NumberFormat.getCurrencyInstance();
     Format monthFormatter = new SimpleDateFormat("MMMM yyyy");
     private Resident currentResident;
     private JScrollPane jspSearch;
     private CollapsiblePanes searchPanes;
     private JXSearchField txtSearch;
     private JComboBox cmbResident;
     private ArrayList<Resident> lstResidents;
     // this map contains the monthly lists of allowances, once they have been loaded
     // it's a cache to speed up things. the key is a combined string like this:
     // RID-MONTHNUMBER-YEARNUMBER
     private HashMap<String, ArrayList<Allowance>> cashmap;
     private HashMap<String, CollapsiblePane> cpMap;
     private HashMap<String, JPanel> contentmap;
     private HashMap<Allowance, JPanel> linemap;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JPanel pnlCash;
     private JScrollPane jspCash;
     private CollapsiblePanes cpsCash;
 
     public PnlAllowance(JScrollPane jspSearch) {
         super();
         this.jspSearch = jspSearch;
         initComponents();
         prepareSearchArea();
         initPanel();
         reloadDisplay();
     }
 
     @Override
     public String getInternalClassID() {
         return internalClassID;
     }
 
     private void initPanel() {
         OPDE.getMainframe().collapseNursingRecords();
         lstResidents = new ArrayList<Resident>();
         cpMap = new HashMap<String, CollapsiblePane>();
         cashmap = new HashMap<String, ArrayList<Allowance>>();
         contentmap = new HashMap<String, JPanel>();
         linemap = new HashMap<Allowance, JPanel>();
 
         lstResidents = ResidentTools.getAllActive();
         currentResident = null;
         OPDE.getMainframe().setCurrentResident(currentResident);
     }
 
     @Override
     public void reload() {
         reloadDisplay();
     }
 
     @Override
     public void cleanup() {
         lstResidents.clear();
         cpsCash.removeAll();
         searchPanes.removeAll();
         cashmap.clear();
         cpMap.clear();
         contentmap.clear();
     }
 
     /**
      * This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the PrinterForm Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         pnlCash = new JPanel();
         jspCash = new JScrollPane();
         cpsCash = new CollapsiblePanes();
 
         //======== this ========
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
         //======== pnlCash ========
         {
             pnlCash.setLayout(new BoxLayout(pnlCash, BoxLayout.X_AXIS));
 
             //======== jspCash ========
             {
 
                 //======== cpsCash ========
                 {
                     cpsCash.setLayout(new BoxLayout(cpsCash, BoxLayout.X_AXIS));
                 }
                 jspCash.setViewportView(cpsCash);
             }
             pnlCash.add(jspCash);
         }
         add(pnlCash);
     }// </editor-fold>//GEN-END:initComponents
 
     private void reloadDisplay() {
         /***
          *               _                 _ ____  _           _
          *      _ __ ___| | ___   __ _  __| |  _ \(_)___ _ __ | | __ _ _   _
          *     | '__/ _ \ |/ _ \ / _` |/ _` | | | | / __| '_ \| |/ _` | | | |
          *     | | |  __/ | (_) | (_| | (_| | |_| | \__ \ |_) | | (_| | |_| |
          *     |_|  \___|_|\___/ \__,_|\__,_|____/|_|___/ .__/|_|\__,_|\__, |
          *                                              |_|            |___/
          */
 
 
         final boolean withworker = true;
         cpsCash.removeAll();
         cashmap.clear();
         cpMap.clear();
         contentmap.clear();
 
         if (withworker) {
 
             OPDE.getMainframe().setBlocked(true);
             OPDE.getDisplayManager().setProgressBarMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.wait"), -1, 100));
 
             SwingWorker worker = new SwingWorker() {
 
                 @Override
                 protected Object doInBackground() throws Exception {
                     int progress = 0;
                     OPDE.getDisplayManager().setProgressBarMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.wait"), progress, lstResidents.size()));
 
                     for (Resident resident : lstResidents) {
                         progress++;
                         createCP4(resident);
                         OPDE.getDisplayManager().setProgressBarMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.wait"), progress, lstResidents.size()));
                     }
                     return null;
                 }
 
                 @Override
                 protected void done() {
                     if (currentResident != null) {
                         OPDE.getDisplayManager().setMainMessage(ResidentTools.getLabelText(currentResident));
                     } else {
                         OPDE.getDisplayManager().setMainMessage(OPDE.lang.getString(internalClassID));
                     }
                     buildPanel();
                     OPDE.getDisplayManager().setProgressBarMessage(null);
                     OPDE.getMainframe().setBlocked(false);
                 }
             };
             worker.execute();
 
         } else {
 
             for (Resident resident : lstResidents) {
                 createCP4(resident);
             }
             if (currentResident != null) {
                 OPDE.getDisplayManager().setMainMessage(ResidentTools.getLabelText(currentResident));
             } else {
                 OPDE.getDisplayManager().setMainMessage(OPDE.lang.getString(internalClassID));
             }
             buildPanel();
         }
 
     }
 
     private CollapsiblePane createCP4(final Resident resident) {
         /***
          *                          _        ____ ____  _  _    ______           _     _            _ __
          *       ___ _ __ ___  __ _| |_ ___ / ___|  _ \| || |  / /  _ \ ___  ___(_) __| | ___ _ __ | |\ \
          *      / __| '__/ _ \/ _` | __/ _ \ |   | |_) | || |_| || |_) / _ \/ __| |/ _` |/ _ \ '_ \| __| |
          *     | (__| | |  __/ (_| | ||  __/ |___|  __/|__   _| ||  _ <  __/\__ \ | (_| |  __/ | | | |_| |
          *      \___|_|  \___|\__,_|\__\___|\____|_|      |_| | ||_| \_\___||___/_|\__,_|\___|_| |_|\__| |
          *                                                     \_\                                    /_/
          */
         final String key = resident.getRID();
         if (!cpMap.containsKey(key)) {
             cpMap.put(key, new CollapsiblePane());
             try {
                 cpMap.get(key).setCollapsed(true);
             } catch (PropertyVetoException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
         final CollapsiblePane cpResident = cpMap.get(key);
 
         BigDecimal sumOverall = AllowanceTools.getSUM(resident);
         String title = "<html><table border=\"0\">" +
                 "<tr>" +
 
                 "<td width=\"520\" align=\"left\"><font size=+1>" + resident.toString() + "</font></td>" +
                 "<td width=\"200\" align=\"right\"><font size=+1" +
                 (sumOverall.compareTo(BigDecimal.ZERO) < 0 ? " color=\"red\" " : "") +
                 ">" + cf.format(sumOverall) + "</font></td>" +
 
                 "</tr>" +
                 "</table>" +
 
 
                 "</html>";
         //, (resident.isActive() ? SYSConst.icon22residentActive : SYSConst.icon22residentInactive), null);
 
         DefaultCPTitle cptitle = new DefaultCPTitle(title, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     cpResident.setCollapsed(!cpResident.isCollapsed());
                 } catch (PropertyVetoException pve) {
                     // BAH!
                 }
             }
         });
 
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.ARCHIVE, internalClassID) && OPDE.getAppInfo().isAllowedTo(InternalClassACL.PRINT, internalClassID)) {
             /***
              *      ____       _       _   ____           _     _            _
              *     |  _ \ _ __(_)_ __ | |_|  _ \ ___  ___(_) __| | ___ _ __ | |_
              *     | |_) | '__| | '_ \| __| |_) / _ \/ __| |/ _` |/ _ \ '_ \| __|
              *     |  __/| |  | | | | | |_|  _ <  __/\__ \ | (_| |  __/ | | | |_
              *     |_|   |_|  |_|_| |_|\__|_| \_\___||___/_|\__,_|\___|_| |_|\__|
              *
              */
             final JButton btnPrintResident = new JButton(SYSConst.icon22print2);
             btnPrintResident.setPressedIcon(SYSConst.icon22print2Pressed);
             btnPrintResident.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnPrintResident.setContentAreaFilled(false);
             btnPrintResident.setBorder(null);
             btnPrintResident.setToolTipText(OPDE.lang.getString(internalClassID + ".btnprintresident.tooltip"));
             btnPrintResident.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     SYSFilesTools.print(AllowanceTools.getAsHTML(AllowanceTools.getAll(resident), BigDecimal.ZERO, currentResident), true);
                 }
 
 
             });
             cptitle.getRight().add(btnPrintResident);
         }
 
         cpResident.setTitleLabelComponent(cptitle.getMain());
         cpResident.setSlidingDirection(SwingConstants.SOUTH);
 
 
         /***
          *           _ _      _            _                               _     _            _
          *       ___| (_) ___| | _____  __| |   ___  _ __    _ __ ___  ___(_) __| | ___ _ __ | |_
          *      / __| | |/ __| |/ / _ \/ _` |  / _ \| '_ \  | '__/ _ \/ __| |/ _` |/ _ \ '_ \| __|
          *     | (__| | | (__|   <  __/ (_| | | (_) | | | | | | |  __/\__ \ | (_| |  __/ | | | |_
          *      \___|_|_|\___|_|\_\___|\__,_|  \___/|_| |_| |_|  \___||___/_|\__,_|\___|_| |_|\__|
          *
          */
         cpResident.addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
             @Override
             public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                 // somebody clicks on the name of the resident. the cash informations
                 // are loaded from the database, if necessary.
                 cpResident.setContentPane(createContentPanel4(resident));
             }
         });
         cpResident.setBackground(getBG(resident, 7));
 
         if (!cpResident.isCollapsed()) {
             cpResident.setContentPane(createContentPanel4(resident));
         }
 
         cpResident.setHorizontalAlignment(SwingConstants.LEADING);
         cpResident.setOpaque(false);
 
         return cpResident;
     }
 
     private JPanel createContentPanel4(final Resident resident) {
         JPanel pnlContent = new JPanel(new VerticalLayout());
         Pair<Date, Date> minmax = AllowanceTools.getMinMax(resident);
 
 
         /***
          *      _____       _              _______  __
          *     | ____|_ __ | |_ ___ _ __  |_   _\ \/ /___
          *     |  _| | '_ \| __/ _ \ '__|   | |  \  // __|
          *     | |___| | | | ||  __/ |      | |  /  \\__ \
          *     |_____|_| |_|\__\___|_|      |_| /_/\_\___/
          *
          */
         final JidePopup popupTX = new JidePopup();
         popupTX.setMovable(false);
         PnlTX pnlTX = new PnlTX(new Allowance(resident), new Closure() {
             @Override
             public void execute(Object o) {
                 OPDE.debug(o);
                 if (o != null) {
 
                     EntityManager em = OPDE.createEM();
                     try {
                         em.getTransaction().begin();
                         final Allowance myAllowance = em.merge((Allowance) o);
                         em.lock(em.merge(myAllowance.getResident()), LockModeType.OPTIMISTIC);
                         em.getTransaction().commit();
 
                         DateTime txDate = new DateTime(myAllowance.getPit());
 
                         final String keyResident = myAllowance.getResident().getRID();
                         final String keyYear = myAllowance.getResident().getRID() + "-" + txDate.getYear();
                         final String keyMonth = myAllowance.getResident().getRID() + "-" + txDate.getYear() + "-" + txDate.getMonthOfYear();
 
                         if (!lstResidents.contains(myAllowance.getResident())) {
                             lstResidents.add(myAllowance.getResident());
                             Collections.sort(lstResidents);
                         }
 
                         if (!cashmap.containsKey(keyMonth)) {
                             cashmap.put(keyMonth, AllowanceTools.getMonth(myAllowance.getResident(), myAllowance.getPit()));
                         } else {
                             cashmap.get(keyMonth).add(myAllowance);
                             Collections.sort(cashmap.get(keyMonth));
                         }
 
                         contentmap.remove(keyMonth);
 
                         createCP4(myAllowance.getResident());
 
                         try {
                             cpMap.get(keyResident).setCollapsed(false);
                             cpMap.get(keyYear).setCollapsed(false);
                             cpMap.get(keyMonth).setCollapsed(false);
                         } catch (PropertyVetoException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
 
                         buildPanel();
 
                         GUITools.scroll2show(jspCash, cpMap.get(keyMonth), cpsCash, new Closure() {
                             @Override
                             public void execute(Object o) {
                                 GUITools.flashBackground(linemap.get(myAllowance), Color.YELLOW, 2);
                             }
                         });
 
                     } catch (OptimisticLockException ole) {
                         if (em.getTransaction().isActive()) {
                             em.getTransaction().rollback();
                         }
                         if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                             OPDE.getMainframe().emptyFrame();
                             OPDE.getMainframe().afterLogin();
                         }
                         OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                     } catch (Exception e) {
                         if (em.getTransaction().isActive()) {
                             em.getTransaction().rollback();
                         }
                         OPDE.fatal(e);
                     } finally {
                         em.close();
                     }
 
                 }
             }
         });
         popupTX.setContentPane(pnlTX);
         popupTX.removeExcludedComponent(pnlTX);
         popupTX.setDefaultFocusComponent(pnlTX);
 
         final JideButton btnNewTX = GUITools.createHyperlinkButton(OPDE.lang.getString(internalClassID + ".enterTXs"), SYSConst.icon22add, null);
         btnNewTX.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 popupTX.setOwner(btnNewTX);
                 GUITools.showPopup(popupTX, SwingConstants.NORTH);
             }
         });
         btnNewTX.setBackground(getBG(resident, 9));
         btnNewTX.setOpaque(true);
         pnlContent.add(btnNewTX);
 
         if (minmax != null) {
             DateMidnight start = new DateMidnight(minmax.getFirst()).dayOfMonth().withMinimumValue();
             DateMidnight end = resident.isActive() ? new DateMidnight() : new DateMidnight(minmax.getSecond()).dayOfMonth().withMinimumValue();
             if (!resident.equals(currentResident)) {
                 OPDE.getDisplayManager().setMainMessage(ResidentTools.getLabelText(resident));
                 currentResident = resident;
                 OPDE.getMainframe().setCurrentResident(currentResident);
             }
 
             CollapsiblePane cpArchive = new CollapsiblePane(OPDE.lang.getString(internalClassID + ".archive"));
             try {
                 cpArchive.setCollapsed(true);
             } catch (PropertyVetoException e) {
                 //bah!
             }
             cpArchive.setBackground(getBG(resident, 7));
             JPanel pnlArchive = new JPanel(new VerticalLayout());
             cpArchive.setContentPane(pnlArchive);
             for (int year = end.getYear(); year >= start.getYear(); year--) {
                 if (year >= new DateMidnight().getYear() - 1) {
                     pnlContent.add(createCP4(resident, year, start, end));
                 } else if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.ARCHIVE, internalClassID)) {
                     pnlArchive.add(createCP4(resident, year, start, end));
                 }
             }
             if (pnlArchive.getComponentCount() > 0) {
                 pnlContent.add(cpArchive);
             }
         }
         return pnlContent;
     }
 
     private CollapsiblePane createCP4(final Resident resident, final int year, DateMidnight min, DateMidnight max) {
         /***
          *                          _        ____ ____  _  _    ______           _     _            _       _       _ __
          *       ___ _ __ ___  __ _| |_ ___ / ___|  _ \| || |  / /  _ \ ___  ___(_) __| | ___ _ __ | |_    (_)_ __ | |\ \
          *      / __| '__/ _ \/ _` | __/ _ \ |   | |_) | || |_| || |_) / _ \/ __| |/ _` |/ _ \ '_ \| __|   | | '_ \| __| |
          *     | (__| | |  __/ (_| | ||  __/ |___|  __/|__   _| ||  _ <  __/\__ \ | (_| |  __/ | | | |_ _  | | | | | |_| |
          *      \___|_|  \___|\__,_|\__\___|\____|_|      |_| | ||_| \_\___||___/_|\__,_|\___|_| |_|\__( ) |_|_| |_|\__| |
          *                                                     \_\                                     |/             /_/
          */
 
         final DateMidnight start = new DateMidnight(year, 1, 1).isBefore(min.dayOfMonth().withMinimumValue()) ? min.dayOfMonth().withMinimumValue() : new DateMidnight(year, 1, 1);
         final DateMidnight end = new DateMidnight(year, 12, 31).isAfter(max.dayOfMonth().withMaximumValue()) ? max.dayOfMonth().withMaximumValue() : new DateMidnight(year, 12, 31);
 
         final String key = resident.getRID() + "-" + Integer.toString(year);
         if (!cpMap.containsKey(key)) {
             cpMap.put(key, new CollapsiblePane());
             try {
                 cpMap.get(key).setCollapsed(true);
             } catch (PropertyVetoException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
 
         final CollapsiblePane cpYear = cpMap.get(key);
 
         DateTime to = new DateTime(year, 1, 1, 0, 0).dayOfYear().withMaximumValue();
         BigDecimal carry4screen = AllowanceTools.getSUM(resident, to);
         final BigDecimal carry4print = AllowanceTools.getSUM(resident, to.minusYears(1));
 
         String title = "<html><table border=\"0\">" +
                 "<tr>" +
 
                 "<td width=\"520\" align=\"left\"><font size=+1>" + Integer.toString(year) + "</font></td>" +
                 "<td width=\"200\" align=\"right\">" +
                 (carry4screen.compareTo(BigDecimal.ZERO) < 0 ? "<font color=\"red\">" : "") +
                 cf.format(carry4screen) +
                 (carry4screen.compareTo(BigDecimal.ZERO) < 0 ? "</font>" : "") +
                 "</td>" +
 
                 "</tr>" +
                 "</table>" +
 
 
                 "</font></html>";
 
         DefaultCPTitle cptitle = new DefaultCPTitle(title, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     cpYear.setCollapsed(!cpYear.isCollapsed());
                 } catch (PropertyVetoException pve) {
                     // BAH!
                 }
             }
         });
 
         GUITools.addExpandCollapseButtons(cpYear, cptitle.getRight());
 
         /***
          *      ____       _       _ __   __
          *     |  _ \ _ __(_)_ __ | |\ \ / /__  __ _ _ __
          *     | |_) | '__| | '_ \| __\ V / _ \/ _` | '__|
          *     |  __/| |  | | | | | |_ | |  __/ (_| | |
          *     |_|   |_|  |_|_| |_|\__||_|\___|\__,_|_|
          *
          */
         final JButton btnPrintYear = new JButton(SYSConst.icon22print2);
         btnPrintYear.setPressedIcon(SYSConst.icon22print2Pressed);
         btnPrintYear.setAlignmentX(Component.RIGHT_ALIGNMENT);
         btnPrintYear.setContentAreaFilled(false);
         btnPrintYear.setBorder(null);
         btnPrintYear.setToolTipText(OPDE.lang.getString("misc.tooltips.btnprintyear"));
         btnPrintYear.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 SYSFilesTools.print(AllowanceTools.getAsHTML(AllowanceTools.getYear(resident, start.toDate()), carry4print, currentResident), true);
             }
 
 
         });
         cptitle.getRight().add(btnPrintYear);
 
         cpYear.setTitleLabelComponent(cptitle.getMain());
         cpYear.setSlidingDirection(SwingConstants.SOUTH);
         cpYear.setBackground(SYSConst.orange1[SYSConst.medium3]);
         cpYear.setOpaque(true);
 
         /***
          *           _ _      _            _
          *       ___| (_) ___| | _____  __| |   ___  _ __    _   _  ___  __ _ _ __
          *      / __| | |/ __| |/ / _ \/ _` |  / _ \| '_ \  | | | |/ _ \/ _` | '__|
          *     | (__| | | (__|   <  __/ (_| | | (_) | | | | | |_| |  __/ (_| | |
          *      \___|_|_|\___|_|\_\___|\__,_|  \___/|_| |_|  \__, |\___|\__,_|_|
          *                                                   |___/
          */
         cpYear.addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
             @Override
             public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                 JPanel pnlContent = new JPanel(new VerticalLayout());
 
                 if (!resident.equals(currentResident)) {
                     OPDE.getDisplayManager().setMainMessage(ResidentTools.getLabelText(resident));
                     currentResident = resident;
                     OPDE.getMainframe().setCurrentResident(currentResident);
                 }
 
                 // somebody clicked on the year
                 // monthly informations will be generated. even if there
                 // are no allowances for that month
                 for (DateMidnight month = end; month.compareTo(start) >= 0; month = month.minusMonths(1)) {
                     pnlContent.add(createCP4(resident, month));
                 }
 
                 cpYear.setContentPane(pnlContent);
                 cpYear.setOpaque(false);
             }
 
         });
         cpYear.setBackground(getBG(resident, 9));
 
         if (!cpYear.isCollapsed()) {
             JPanel pnlContent = new JPanel(new VerticalLayout());
             for (DateMidnight month = end; month.compareTo(start) > 0; month = month.minusMonths(1)) {
                 pnlContent.add(createCP4(resident, month));
             }
             cpYear.setContentPane(pnlContent);
         }
 
         cpYear.setHorizontalAlignment(SwingConstants.LEADING);
         cpYear.setOpaque(false);
 
         return cpYear;
     }
 
     private CollapsiblePane createCP4(final Resident resident, final DateMidnight month) {
         /***
          *                          _        ____ ____  _  _    ______           _     _            _       ____        _      _____ _              __
          *       ___ _ __ ___  __ _| |_ ___ / ___|  _ \| || |  / /  _ \ ___  ___(_) __| | ___ _ __ | |_    |  _ \  __ _| |_ __|_   _(_)_ __ ___   __\ \
          *      / __| '__/ _ \/ _` | __/ _ \ |   | |_) | || |_| || |_) / _ \/ __| |/ _` |/ _ \ '_ \| __|   | | | |/ _` | __/ _ \| | | | '_ ` _ \ / _ \ |
          *     | (__| | |  __/ (_| | ||  __/ |___|  __/|__   _| ||  _ <  __/\__ \ | (_| |  __/ | | | |_ _  | |_| | (_| | ||  __/| | | | | | | | |  __/ |
          *      \___|_|  \___|\__,_|\__\___|\____|_|      |_| | ||_| \_\___||___/_|\__,_|\___|_| |_|\__( ) |____/ \__,_|\__\___||_| |_|_| |_| |_|\___| |
          *                                                     \_\                                     |/                                           /_/
          */
         final String key = resident.getRID() + "-" + month.getYear() + "-" + month.getMonthOfYear();
         if (!cpMap.containsKey(key)) {
             cpMap.put(key, new CollapsiblePane());
             try {
                 cpMap.get(key).setCollapsed(true);
             } catch (PropertyVetoException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
         }
         final CollapsiblePane cpMonth = cpMap.get(key);
 
         DateTime to = new DateTime(month).dayOfMonth().withMaximumValue();
         final BigDecimal carry4screen = AllowanceTools.getSUM(resident, to);
        final BigDecimal carry4print = AllowanceTools.getSUM(resident, to.minusMonths(1));
         String title = "<html><table border=\"0\">" +
                 "<tr>" +
 
                 "<td width=\"520\" align=\"left\">" + monthFormatter.format(month.toDate()) + "</td>" +
                 "<td width=\"200\" align=\"right\">" +
                 (carry4screen.compareTo(BigDecimal.ZERO) < 0 ? "<font color=\"red\">" : "") +
                 cf.format(carry4screen) +
                 (carry4screen.compareTo(BigDecimal.ZERO) < 0 ? "</font>" : "") +
                 "</td>" +
                 "</tr>" +
                 "</table>" +
 
 
                 "</font></html>";
 
         DefaultCPTitle cptitle = new DefaultCPTitle(title, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     cpMonth.setCollapsed(!cpMonth.isCollapsed());
                 } catch (PropertyVetoException pve) {
                     // BAH!
                 }
             }
         });
 
         /***
          *      ____       _       _   __  __             _   _
          *     |  _ \ _ __(_)_ __ | |_|  \/  | ___  _ __ | |_| |__
          *     | |_) | '__| | '_ \| __| |\/| |/ _ \| '_ \| __| '_ \
          *     |  __/| |  | | | | | |_| |  | | (_) | | | | |_| | | |
          *     |_|   |_|  |_|_| |_|\__|_|  |_|\___/|_| |_|\__|_| |_|
          *
          */
         final JButton btnPrintMonth = new JButton(SYSConst.icon22print2);
         btnPrintMonth.setPressedIcon(SYSConst.icon22print2Pressed);
         btnPrintMonth.setAlignmentX(Component.RIGHT_ALIGNMENT);
         btnPrintMonth.setContentAreaFilled(false);
         btnPrintMonth.setBorder(null);
         btnPrintMonth.setToolTipText(OPDE.lang.getString("misc.tooltips.btnprintmonth"));
         btnPrintMonth.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (!cashmap.containsKey(key)) {
                     cashmap.put(key, AllowanceTools.getMonth(resident, month.toDate()));
                 }
                 SYSFilesTools.print(AllowanceTools.getAsHTML(cashmap.get(key), carry4print, currentResident), true);
             }
         });
 
         cptitle.getRight().add(btnPrintMonth);
 
         cpMonth.setTitleLabelComponent(cptitle.getMain());
         cpMonth.setSlidingDirection(SwingConstants.SOUTH);
 
         cpMonth.setBackground(getBG(resident, 10));
 
         /***
          *           _ _      _            _                                       _   _
          *       ___| (_) ___| | _____  __| |   ___  _ __    _ __ ___   ___  _ __ | |_| |__
          *      / __| | |/ __| |/ / _ \/ _` |  / _ \| '_ \  | '_ ` _ \ / _ \| '_ \| __| '_ \
          *     | (__| | | (__|   <  __/ (_| | | (_) | | | | | | | | | | (_) | | | | |_| | | |
          *      \___|_|_|\___|_|\_\___|\__,_|  \___/|_| |_| |_| |_| |_|\___/|_| |_|\__|_| |_|
          *
          */
         cpMonth.addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
             @Override
             public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
 
                 if (!resident.equals(currentResident)) {
                     OPDE.getDisplayManager().setMainMessage(ResidentTools.getLabelText(resident));
                     currentResident = resident;
                     OPDE.getMainframe().setCurrentResident(currentResident);
                 }
 
                 cpMonth.setContentPane(createContentPanel4(resident, carry4screen, month));
                 cpMonth.setOpaque(false);
             }
         });
 
         if (!cpMonth.isCollapsed()) {
             cpMonth.setContentPane(createContentPanel4(resident, carry4screen, month));
         }
 
         cpMonth.setHorizontalAlignment(SwingConstants.LEADING);
         cpMonth.setOpaque(false);
 
         return cpMonth;
     }
     // End of variables declaration//GEN-END:variables
 
     private void prepareSearchArea() {
         searchPanes = new CollapsiblePanes();
         searchPanes.setLayout(new JideBoxLayout(searchPanes, JideBoxLayout.Y_AXIS));
         jspSearch.setViewportView(searchPanes);
 
         JPanel mypanel = new JPanel();
         mypanel.setLayout(new VerticalLayout(3));
         mypanel.setBackground(Color.WHITE);
 
         CollapsiblePane searchPane = new CollapsiblePane(OPDE.lang.getString(internalClassID));
         searchPane.setStyle(CollapsiblePane.PLAIN_STYLE);
         searchPane.setCollapsible(false);
 
         try {
             searchPane.setCollapsed(false);
         } catch (PropertyVetoException e) {
             OPDE.error(e);
         }
 
         GUITools.addAllComponents(mypanel, addCommands());
         GUITools.addAllComponents(mypanel, addFilters());
 
         searchPane.setContentPane(mypanel);
 
         searchPanes.add(searchPane);
         searchPanes.addExpansion();
     }
 
     private java.util.List<Component> addFilters() {
         java.util.List<Component> list = new ArrayList<Component>();
 
         txtSearch = new JXSearchField(OPDE.lang.getString("misc.msg.residentsearch"));
         txtSearch.setFont(SYSConst.ARIAL14);
         txtSearch.setInstantSearchDelay(100000);
         txtSearch.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ArrayList<Resident> listSearchResidents = ResidentTools.getBy(txtSearch.getText().trim(), OPDE.getAppInfo().isAllowedTo(InternalClassACL.ARCHIVE, internalClassID));
                 if (listSearchResidents != null && !listSearchResidents.isEmpty()) {
                     cmbResident.setModel(SYSTools.list2cmb(listSearchResidents));
                     lstResidents = new ArrayList<Resident>();
                     lstResidents.add((Resident) cmbResident.getSelectedItem());
                     currentResident = (Resident) cmbResident.getSelectedItem();
                     OPDE.getMainframe().setCurrentResident(currentResident);
                     reloadDisplay();
                 } else {
                     OPDE.getDisplayManager().addSubMessage(new DisplayMessage("misc.msg.nodata"));
                 }
             }
         });
 
         list.add(txtSearch);
 
         cmbResident = new JComboBox();
         cmbResident.setFont(SYSConst.ARIAL14);
         cmbResident.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     lstResidents = new ArrayList<Resident>();
                     lstResidents.add((Resident) e.getItem());
                     currentResident = (Resident) e.getItem();
                     OPDE.getMainframe().setCurrentResident(currentResident);
                     reloadDisplay();
                 }
             }
         });
         list.add(cmbResident);
 
         final JideButton btnAllActiveResidents = GUITools.createHyperlinkButton(OPDE.lang.getString(internalClassID + ".showallactiveresidents"), SYSConst.icon22residentActive, null);
         btnAllActiveResidents.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 lstResidents = ResidentTools.getAllActive();
                 currentResident = null;
                 OPDE.getMainframe().setCurrentResident(currentResident);
                 reloadDisplay();
             }
         });
         list.add(btnAllActiveResidents);
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.ARCHIVE, internalClassID)) {
             final JideButton btnAllInactiveResidents = GUITools.createHyperlinkButton(OPDE.lang.getString(internalClassID + ".showallinactiveresidents"), SYSConst.icon22residentInactive, null);
             btnAllInactiveResidents.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     lstResidents = ResidentTools.getAllInactive();
                     currentResident = null;
                     OPDE.getMainframe().setCurrentResident(currentResident);
                     reloadDisplay();
                 }
             });
             list.add(btnAllInactiveResidents);
 
             final JideButton btnAllResidents = GUITools.createHyperlinkButton(OPDE.lang.getString(internalClassID + ".showallresidents"), SYSConst.icon22residentBoth, null);
             btnAllResidents.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     lstResidents = ResidentTools.getAllInactive();
                     lstResidents.addAll(ResidentTools.getAllActive());
                     Collections.sort(lstResidents);
                     currentResident = null;
                     OPDE.getMainframe().setCurrentResident(currentResident);
                     reloadDisplay();
                 }
             });
             list.add(btnAllResidents);
         }
         return list;
     }
 
     private java.util.List<Component> addCommands() {
 
         java.util.List<Component> list = new ArrayList<Component>();
 
         /***
          *      _____       _              _______  __
          *     | ____|_ __ | |_ ___ _ __  |_   _\ \/ /___
          *     |  _| | '_ \| __/ _ \ '__|   | |  \  // __|
          *     | |___| | | | ||  __/ |      | |  /  \\__ \
          *     |_____|_| |_|\__\___|_|      |_| /_/\_\___/
          *
          */
         final JidePopup popupTX = new JidePopup();
         popupTX.setMovable(false);
         PnlTX pnlTX = new PnlTX(new Allowance(currentResident), new Closure() {
             @Override
             public void execute(Object o) {
                 OPDE.debug(o);
                 if (o != null) {
 
                     EntityManager em = OPDE.createEM();
                     try {
                         em.getTransaction().begin();
                         final Allowance myAllowance = em.merge((Allowance) o);
                         em.lock(em.merge(myAllowance.getResident()), LockModeType.OPTIMISTIC);
                         em.getTransaction().commit();
 
                         DateTime txDate = new DateTime(myAllowance.getPit());
 
                         final String keyResident = myAllowance.getResident().getRID();
                         final String keyYear = myAllowance.getResident().getRID() + "-" + txDate.getYear();
                         final String keyMonth = myAllowance.getResident().getRID() + "-" + txDate.getYear() + "-" + txDate.getMonthOfYear();
 
                         if (!lstResidents.contains(myAllowance.getResident())) {
                             lstResidents.add(myAllowance.getResident());
                             Collections.sort(lstResidents);
                         }
 
                         if (!cashmap.containsKey(keyMonth)) {
                             cashmap.put(keyMonth, AllowanceTools.getMonth(myAllowance.getResident(), myAllowance.getPit()));
                         } else {
                             cashmap.get(keyMonth).add(myAllowance);
                             Collections.sort(cashmap.get(keyMonth));
                         }
 
                         contentmap.remove(keyMonth);
 
                         createCP4(myAllowance.getResident());
 
                         try {
                             cpMap.get(keyResident).setCollapsed(false);
                             cpMap.get(keyYear).setCollapsed(false);
                             cpMap.get(keyMonth).setCollapsed(false);
                         } catch (PropertyVetoException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
 
                         buildPanel();
 
                         GUITools.scroll2show(jspCash, cpMap.get(keyMonth), cpsCash, new Closure() {
                             @Override
                             public void execute(Object o) {
                                 GUITools.flashBackground(linemap.get(myAllowance), Color.YELLOW, 2);
                             }
                         });
 
                     } catch (OptimisticLockException ole) {
                         if (em.getTransaction().isActive()) {
                             em.getTransaction().rollback();
                         }
                         if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                             OPDE.getMainframe().emptyFrame();
                             OPDE.getMainframe().afterLogin();
                         }
                         OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                     } catch (Exception e) {
                         if (em.getTransaction().isActive()) {
                             em.getTransaction().rollback();
                         }
                         OPDE.fatal(e);
                     } finally {
                         em.close();
                     }
 
                 }
             }
         });
         popupTX.setContentPane(pnlTX);
         popupTX.removeExcludedComponent(pnlTX);
         popupTX.setDefaultFocusComponent(pnlTX);
 
         final JideButton btnNewTX = GUITools.createHyperlinkButton(OPDE.lang.getString(internalClassID + ".enterTXs"), SYSConst.icon22add, null);
         btnNewTX.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 popupTX.setOwner(btnNewTX);
                 GUITools.showPopup(popupTX, SwingConstants.NORTH_EAST);
             }
         });
         list.add(btnNewTX);
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.MANAGER, internalClassID)) {
             final JideButton btnPrintStat = GUITools.createHyperlinkButton(OPDE.lang.getString(internalClassID + ".printstat"), SYSConst.icon22calc, null);
             btnPrintStat.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     SYSFilesTools.print(AllowanceTools.getOverallSumAsHTML(12), false);
                 }
             });
             list.add(btnPrintStat);
         }
         return list;
     }
 
     private Color getBG(Resident resident, int level) {
         if (lstResidents.indexOf(resident) % 2 == 0) {
             return SYSConst.purple1[level];
         } else {
             return SYSConst.greyscale[level];
         }
     }
 
     private void buildPanel() {
         cpsCash.removeAll();
         cpsCash.setLayout(new JideBoxLayout(cpsCash, JideBoxLayout.Y_AXIS));
 
         for (Resident resident : lstResidents) {
             cpsCash.add(cpMap.get(resident.getRID()));
         }
 
         cpsCash.addExpansion();
     }
 
     private JPanel createContentPanel4(final Resident resident, BigDecimal carry, DateMidnight month) {
         final String key = resident.getRID() + "-" + month.getYear() + "-" + month.getMonthOfYear();
 
         if (!contentmap.containsKey(key)) {
 
             JPanel pnlMonth = new JPanel(new VerticalLayout());
 
             pnlMonth.setBackground(getBG(resident, 11));
             pnlMonth.setOpaque(false);
 
             BigDecimal rowsum = carry;
 
             if (!cashmap.containsKey(key)) {
                 cashmap.put(key, AllowanceTools.getMonth(resident, month.toDate()));
             }
 
             JLabel lblEOM = new JLabel("<html><table border=\"0\">" +
                     "<tr>" +
                     "<td width=\"130\" align=\"left\">" + DateFormat.getDateInstance().format(month.dayOfMonth().withMaximumValue().toDate()) + "</td>" +
                     "<td width=\"400\" align=\"left\">" + OPDE.lang.getString(internalClassID + ".endofmonth") + "</td>" +
                     "<td width=\"100\" align=\"right\"></td>" +
                     "<td width=\"100\" align=\"right\">" +
                     (rowsum.compareTo(BigDecimal.ZERO) < 0 ? "<font color=\"red\">" : "") +
                     cf.format(rowsum) +
                     (rowsum.compareTo(BigDecimal.ZERO) < 0 ? "</font>" : "") +
                     "</td>" +
                     "</tr>" +
                     "</table>" +
 
                     "</font></html>");
             pnlMonth.add(lblEOM);
 
             for (final Allowance allowance : cashmap.get(key)) {
 
                 String title = "<html><table border=\"0\">" +
                         "<tr>" +
                         "<td width=\"130\" align=\"left\">" + DateFormat.getDateInstance().format(allowance.getPit()) + "</td>" +
                         "<td width=\"400\" align=\"left\">" + allowance.getText() + "</td>" +
                         "<td width=\"100\" align=\"right\">" +
                         (allowance.getAmount().compareTo(BigDecimal.ZERO) < 0 ? "<font color=\"red\">" : "") +
                         cf.format(allowance.getAmount()) +
                         (allowance.getAmount().compareTo(BigDecimal.ZERO) < 0 ? "</font>" : "") +
                         "</td>" +
                         "<td width=\"100\" align=\"right\">" +
                         (rowsum.compareTo(BigDecimal.ZERO) < 0 ? "<font color=\"red\">" : "") +
                         cf.format(rowsum) +
                         (rowsum.compareTo(BigDecimal.ZERO) < 0 ? "</font>" : "") +
                         "</td>" +
                         "</tr>" +
                         "</table>" +
 
                         "</font></html>";
 
                 DefaultCPTitle cptitle = new DefaultCPTitle(title, new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
 
                     }
                 });
                 cptitle.getButton().setIcon(allowance.isReplaced() || allowance.isReplacement() ? SYSConst.icon22eraser : null);
 
                 if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
                     /***
                      *      _____    _ _ _
                      *     | ____|__| (_) |_
                      *     |  _| / _` | | __|
                      *     | |__| (_| | | |_
                      *     |_____\__,_|_|\__|
                      *
                      */
                     final JButton btnEdit = new JButton(SYSConst.icon22edit3);
                     btnEdit.setPressedIcon(SYSConst.icon22edit3Pressed);
                     btnEdit.setAlignmentX(Component.RIGHT_ALIGNMENT);
                     btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                     btnEdit.setContentAreaFilled(false);
                     btnEdit.setBorder(null);
                     btnEdit.setToolTipText(OPDE.lang.getString(internalClassID + ".btnedit.tooltip"));
                     btnEdit.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent actionEvent) {
 
                             final JidePopup popupTX = new JidePopup();
                             popupTX.setMovable(false);
                             PnlTX pnlTX = new PnlTX(allowance, new Closure() {
                                 @Override
                                 public void execute(Object o) {
 
                                     if (o != null) {
 
                                         EntityManager em = OPDE.createEM();
                                         try {
                                             em.getTransaction().begin();
                                             Allowance myAllowance = em.merge((Allowance) o);
                                             em.lock(em.merge(myAllowance.getResident()), LockModeType.OPTIMISTIC);
                                             em.lock(myAllowance, LockModeType.OPTIMISTIC);
                                             em.getTransaction().commit();
 
                                             DateTime txDate = new DateTime(myAllowance.getPit());
 
                                             final String keyMonth = myAllowance.getResident().getRID() + "-" + txDate.getYear() + "-" + txDate.getMonthOfYear();
                                             contentmap.remove(keyMonth);
                                             cpMap.remove(keyMonth);
                                             cashmap.get(keyMonth).remove(allowance);
                                             cashmap.get(keyMonth).add(myAllowance);
                                             Collections.sort(cashmap.get(keyMonth));
                                             createCP4(myAllowance.getResident());
 
                                             try {
                                                 cpMap.get(keyMonth).setCollapsed(false);
                                             } catch (PropertyVetoException e) {
                                                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                             }
 
                                             buildPanel();
                                         } catch (OptimisticLockException ole) {
                                             if (em.getTransaction().isActive()) {
                                                 em.getTransaction().rollback();
                                             }
                                             if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                                 OPDE.getMainframe().emptyFrame();
                                                 OPDE.getMainframe().afterLogin();
                                             }
                                             OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                                         } catch (Exception e) {
                                             if (em.getTransaction().isActive()) {
                                                 em.getTransaction().rollback();
                                             }
                                             OPDE.fatal(e);
                                         } finally {
                                             em.close();
                                         }
 
                                     }
                                 }
                             });
                             popupTX.setContentPane(pnlTX);
                             popupTX.removeExcludedComponent(pnlTX);
                             popupTX.setDefaultFocusComponent(pnlTX);
 
                             popupTX.setOwner(btnEdit);
                             GUITools.showPopup(popupTX, SwingConstants.WEST);
 
                         }
                     });
                     cptitle.getRight().add(btnEdit);
                     // you can edit your own entries or you are a manager. once they are replaced or a replacement record, its over.
                     btnEdit.setEnabled((OPDE.getAppInfo().isAllowedTo(InternalClassACL.MANAGER, internalClassID) || allowance.getUser().equals(OPDE.getLogin().getUser())) && !allowance.isReplaced() && !allowance.isReplacement());
 
                     /***
                      *      _   _           _         _______  __
                      *     | | | |_ __   __| | ___   |_   _\ \/ /
                      *     | | | | '_ \ / _` |/ _ \    | |  \  /
                      *     | |_| | | | | (_| | (_) |   | |  /  \
                      *      \___/|_| |_|\__,_|\___/    |_| /_/\_\
                      *
                      */
                     final JButton btnUndoTX = new JButton(SYSConst.icon22undo);
                     btnUndoTX.setPressedIcon(SYSConst.icon22Pressed);
                     btnUndoTX.setAlignmentX(Component.RIGHT_ALIGNMENT);
                     btnUndoTX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                     btnUndoTX.setContentAreaFilled(false);
                     btnUndoTX.setBorder(null);
                     btnUndoTX.setToolTipText(OPDE.lang.getString(internalClassID + ".btnundotx.tooltip"));
                     btnUndoTX.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent actionEvent) {
 
 
                             new DlgYesNo(OPDE.lang.getString("misc.questions.undo1") + "<br/><i>" + "<br/><i>" + allowance.getText() + "&nbsp;" + cf.format(allowance.getAmount()) + "</i><br/>" + OPDE.lang.getString("misc.questions.undo2"), SYSConst.icon48undo, new Closure() {
                                 @Override
                                 public void execute(Object answer) {
                                     if (answer.equals(JOptionPane.YES_OPTION)) {
                                         EntityManager em = OPDE.createEM();
                                         try {
                                             em.getTransaction().begin();
 
                                             Allowance myOldAllowance = em.merge(allowance);
                                             Allowance myCancelAllowance = em.merge(new Allowance(myOldAllowance));
                                             em.lock(em.merge(myOldAllowance.getResident()), LockModeType.OPTIMISTIC);
                                             em.lock(myOldAllowance, LockModeType.OPTIMISTIC);
                                             myOldAllowance.setReplacedBy(myCancelAllowance, em.merge(OPDE.getLogin().getUser()));
 
                                             em.getTransaction().commit();
 
                                             DateTime txDate = new DateTime(myCancelAllowance.getPit());
 
                                             final String keyMonth = myCancelAllowance.getResident().getRID() + "-" + txDate.getYear() + "-" + txDate.getMonthOfYear();
                                             contentmap.remove(keyMonth);
                                             cpMap.remove(keyMonth);
                                             cashmap.get(keyMonth).remove(allowance);
                                             cashmap.get(keyMonth).add(myOldAllowance);
                                             cashmap.get(keyMonth).add(myCancelAllowance);
                                             Collections.sort(cashmap.get(keyMonth));
                                             createCP4(myCancelAllowance.getResident());
 
                                             try {
                                                 cpMap.get(keyMonth).setCollapsed(false);
                                             } catch (PropertyVetoException e) {
                                                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                             }
 
                                             buildPanel();
 
                                         } catch (OptimisticLockException ole) {
                                             if (em.getTransaction().isActive()) {
                                                 em.getTransaction().rollback();
                                             }
                                             if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                                 OPDE.getMainframe().emptyFrame();
                                                 OPDE.getMainframe().afterLogin();
                                             }
                                             OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                                         } catch (Exception e) {
                                             if (em.getTransaction().isActive()) {
                                                 em.getTransaction().rollback();
                                             }
                                             OPDE.fatal(e);
                                         } finally {
                                             em.close();
                                         }
                                     }
                                 }
                             });
                         }
                     });
                     cptitle.getRight().add(btnUndoTX);
                     btnUndoTX.setEnabled(!allowance.isReplaced() && !allowance.isReplacement());
                 }
 
                 if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.DELETE, internalClassID)) {
                     /***
                      *      ____       _      _
                      *     |  _ \  ___| | ___| |_ ___
                      *     | | | |/ _ \ |/ _ \ __/ _ \
                      *     | |_| |  __/ |  __/ ||  __/
                      *     |____/ \___|_|\___|\__\___|
                      *
                      */
                     final JButton btnDelete = new JButton(SYSConst.icon22delete);
                     btnDelete.setPressedIcon(SYSConst.icon22deletePressed);
                     btnDelete.setAlignmentX(Component.RIGHT_ALIGNMENT);
                     btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                     btnDelete.setContentAreaFilled(false);
                     btnDelete.setBorder(null);
                     btnDelete.setToolTipText(OPDE.lang.getString(internalClassID + ".btndelete.tooltip"));
                     btnDelete.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent actionEvent) {
                             new DlgYesNo(OPDE.lang.getString("misc.questions.delete1") + "<br/><i>" + allowance.getText() + "&nbsp;" + cf.format(allowance.getAmount()) + "</i><br/>" + OPDE.lang.getString("misc.questions.delete2"), SYSConst.icon48delete, new Closure() {
                                 @Override
                                 public void execute(Object answer) {
                                     if (answer.equals(JOptionPane.YES_OPTION)) {
                                         EntityManager em = OPDE.createEM();
                                         try {
                                             em.getTransaction().begin();
                                             Allowance myAllowance = em.merge(allowance);
                                             em.lock(em.merge(myAllowance.getResident()), LockModeType.OPTIMISTIC);
 
                                             Allowance theOtherOne = null;
                                             // Check for special cases
                                             if (myAllowance.isReplacement()) {
                                                 theOtherOne = em.merge(myAllowance.getReplacementFor());
                                                 theOtherOne.setReplacedBy(null);
                                                 theOtherOne.setEditedBy(null);
                                                 myAllowance.setEditPit(null);
                                             }
                                             if (myAllowance.isReplaced()) {
                                                 theOtherOne = em.merge(myAllowance.getReplacedBy());
                                                 theOtherOne.setReplacementFor(null);
                                             }
 
                                             em.remove(myAllowance);
                                             em.getTransaction().commit();
 
                                             DateTime txDate = new DateTime(myAllowance.getPit());
                                             final String keyMonth = myAllowance.getResident().getRID() + "-" + txDate.getYear() + "-" + txDate.getMonthOfYear();
 
                                             contentmap.remove(keyMonth);
                                             cpMap.remove(keyMonth);
                                             cashmap.get(keyMonth).remove(myAllowance);
                                             if (theOtherOne != null) {
                                                 cashmap.get(keyMonth).remove(theOtherOne);
                                                 cashmap.get(keyMonth).add(theOtherOne);
                                                 Collections.sort(cashmap.get(keyMonth));
                                             }
                                             createCP4(myAllowance.getResident());
 
                                             try {
                                                 cpMap.get(keyMonth).setCollapsed(false);
                                             } catch (PropertyVetoException e) {
                                                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                             }
 
                                             buildPanel();
                                         } catch (OptimisticLockException ole) {
                                             if (em.getTransaction().isActive()) {
                                                 em.getTransaction().rollback();
                                             }
                                             if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                                 OPDE.getMainframe().emptyFrame();
                                                 OPDE.getMainframe().afterLogin();
                                             }
                                             OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                                         } catch (Exception e) {
                                             if (em.getTransaction().isActive()) {
                                                 em.getTransaction().rollback();
                                             }
                                             OPDE.fatal(e);
                                         } finally {
                                             em.close();
                                         }
                                     }
                                 }
                             });
                         }
                     });
                     cptitle.getRight().add(btnDelete);
                 }
                 pnlMonth.add(cptitle.getMain());
                 linemap.put(allowance, cptitle.getMain());
 
                 rowsum = rowsum.subtract(allowance.getAmount());
             }
 
             JLabel lblBOM = new JLabel("<html><table border=\"0\">" +
                     "<tr>" +
                     "<td width=\"130\" align=\"left\">" + DateFormat.getDateInstance().format(month.dayOfMonth().withMinimumValue().toDate()) + "</td>" +
                     "<td width=\"400\" align=\"left\">" + OPDE.lang.getString(internalClassID + ".startofmonth") + "</td>" +
                     "<td width=\"100\" align=\"right\"></td>" +
                     "<td width=\"100\" align=\"right\">" +
                     (rowsum.compareTo(BigDecimal.ZERO) < 0 ? "<font color=\"red\">" : "") +
                     cf.format(rowsum) +
                     (rowsum.compareTo(BigDecimal.ZERO) < 0 ? "</font>" : "") +
                     "</td>" +
                     "</tr>" +
                     "</table>" +
 
                     "</font></html>");
             lblBOM.setBackground(getBG(resident, 11));
             pnlMonth.add(lblBOM);
             contentmap.put(key, pnlMonth);
         }
 
         return contentmap.get(key);
     }
 
 }
