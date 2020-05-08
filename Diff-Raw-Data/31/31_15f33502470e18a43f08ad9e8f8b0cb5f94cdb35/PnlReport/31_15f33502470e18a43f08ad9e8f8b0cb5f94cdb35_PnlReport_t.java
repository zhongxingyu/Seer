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
 package op.care.reports;
 
 import com.jidesoft.pane.CollapsiblePane;
 import com.jidesoft.pane.CollapsiblePanes;
 import com.jidesoft.pane.event.CollapsiblePaneAdapter;
 import com.jidesoft.pane.event.CollapsiblePaneEvent;
 import com.jidesoft.popup.JidePopup;
 import com.jidesoft.swing.JideBoxLayout;
 import com.jidesoft.swing.JideButton;
 import entity.EntityTools;
 import entity.files.SYSFilesTools;
 import entity.files.SYSNR2FILE;
 import entity.info.Resident;
 import entity.process.*;
 import entity.reports.NReport;
 import entity.reports.NReportTAGS;
 import entity.reports.NReportTAGSTools;
 import entity.reports.NReportTools;
 import op.OPDE;
 import op.care.sysfiles.DlgFiles;
 import op.process.DlgProcessAssign;
 import op.system.InternalClassACL;
 import op.threads.DisplayManager;
 import op.threads.DisplayMessage;
 import op.tools.*;
 import org.apache.commons.collections.Closure;
 import org.jdesktop.swingx.JXSearchField;
 import org.jdesktop.swingx.VerticalLayout;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 
 import javax.persistence.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyVetoException;
 import java.text.DateFormat;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * @author root
  */
 public class PnlReport extends NursingRecordsPanel {
 
     public static final String internalClassID = "nursingrecords.reports";
 
     private JXSearchField txtSearch;
     private JToggleButton tbShowReplaced;
     private JComboBox cmbTags;
 
     private Map<String, CollapsiblePane> cpMap;
     private Map<String, JPanel> contentmap;
     private Map<String, ArrayList<NReport>> valuecache;
     private Map<NReport, JPanel> linemap;
    private Map<DateMidnight, String> holidays;
 
     private Resident resident;
     private boolean initPhase;
 
     private JScrollPane jspSearch;
     private CollapsiblePanes searchPanes;
 
     Format monthFormatter = new SimpleDateFormat("MMMM yyyy");
     Format weekFormater = new SimpleDateFormat("w yyyy");
     Format dayFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy");
 
 
     @Override
     public String getInternalClassID() {
         return internalClassID;
     }
 
     /**
      * Creates new form PnlReport
      */
     public PnlReport(Resident resident, JScrollPane jspSearch) {
         this.initPhase = true;
         initComponents();
         this.jspSearch = jspSearch;
 
         prepareSearchArea();
 
         initPanel();
         this.initPhase = false;
 
         switchResident(resident);
 
     }
 
     private void initPanel() {
         contentmap = Collections.synchronizedMap(new HashMap<String, JPanel>());
         cpMap = Collections.synchronizedMap(new HashMap<String, CollapsiblePane>());
         valuecache = Collections.synchronizedMap(new HashMap<String, ArrayList<NReport>>());
         linemap = Collections.synchronizedMap(new HashMap<NReport, JPanel>());
        holidays = new HashMap<DateMidnight, String>();
         prepareSearchArea();
     }
 
     private void prepareSearchArea() {
 
         searchPanes = new CollapsiblePanes();
         searchPanes.setLayout(new JideBoxLayout(searchPanes, JideBoxLayout.Y_AXIS));
         jspSearch.setViewportView(searchPanes);
 
         JPanel mypanel = new JPanel();
         mypanel.setLayout(new VerticalLayout(5));
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
 
     @Override
     public void reload() {
         GUITools.setResidentDisplay(resident);
         reloadDisplay();
     }
 
     /**
      * This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the PrinterForm Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         jspReports = new JScrollPane();
         cpsReports = new CollapsiblePanes();
 
         //======== this ========
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
         //======== jspReports ========
         {
 
             //======== cpsReports ========
             {
                 cpsReports.setLayout(new BoxLayout(cpsReports, BoxLayout.X_AXIS));
             }
             jspReports.setViewportView(cpsReports);
         }
         add(jspReports);
     }// </editor-fold>//GEN-END:initComponents
 
     @Override
     public void cleanup() {
         synchronized (contentmap) {
             SYSTools.clear(contentmap);
         }
         synchronized (cpMap) {
             SYSTools.clear(cpMap);
         }
         cpsReports.removeAll();
         synchronized (linemap) {
             SYSTools.clear(linemap);
         }
         synchronized (valuecache) {
             SYSTools.clear(valuecache);
         }
        synchronized (holidays) {
            SYSTools.clear(holidays);
         }
     }
 
     @Override
     public void switchResident(Resident res) {
         this.resident = EntityTools.find(Resident.class, res.getRID());
         GUITools.setResidentDisplay(resident);
 //        txtSearch.setText(null);
 //        firstReport = NReportTools.getFirstReport(resident);
 //        jdcVon.setMaxSelectableDate(new Date());
 //        jdcVon.setMinSelectableDate(firstReport.getPit());
         reloadDisplay();
     }
 
     private java.util.List<Component> addFilters() {
         java.util.List<Component> list = new ArrayList<Component>();
 
         txtSearch = new JXSearchField(OPDE.lang.getString("misc.msg.searchphrase"));
         txtSearch.setInstantSearchDelay(100000);
         txtSearch.setFont(SYSConst.ARIAL14);
         txtSearch.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (SYSTools.catchNull(txtSearch.getText()).trim().length() > 3) {
                     SYSFilesTools.print(NReportTools.getReportsAsHTML(NReportTools.getNReports4Search(resident, txtSearch.getText().trim()), false, true, OPDE.lang.getString("misc.msg.searchresults") + ": &quot;" + txtSearch.getText().trim() + "&quot;", txtSearch.getText().trim()), false);
                 }
             }
         });
 
         list.add(txtSearch);
 
 //        jdcVon = new JDateChooser(new Date());
 //        jdcVon.setBackground(Color.WHITE);
 //        jdcVon.setFont(SYSConst.ARIAL14);
 //        jdcVon.addPropertyChangeListener(new PropertyChangeListener() {
 //            @Override
 //            public void propertyChange(PropertyChangeEvent evt) {
 //                if (initPhase) {
 //                    return;
 //                }
 //                if (!evt.getPropertyName().equals("date")) {
 //                    return;
 //                }
 //                reloadDisplay(NReportTools.getReports(resident, jdcVon.getDate(), WEEKS_BACK));
 //            }
 //        });
 //        labelPanel.add(jdcVon);
 //
 //        JPanel buttonPanel = new JPanel();
 //        buttonPanel.setBackground(Color.WHITE);
 //        buttonPanel.setLayout(new HorizontalLayout(5));
 //        buttonPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
 
         EntityManager em = OPDE.createEM();
         MouseAdapter ma = GUITools.getHyperlinkStyleMouseAdapter();
         Query query = em.createQuery("SELECT p FROM NReportTAGS p WHERE p.aktiv = TRUE ORDER BY p.besonders DESC, p.sort DESC, p.bezeichnung");
         DefaultComboBoxModel dcbm = new DefaultComboBoxModel(query.getResultList().toArray());
         em.close();
 
         dcbm.insertElementAt(OPDE.lang.getString("misc.commands.noselection"), 0);
         cmbTags = new JComboBox(dcbm);
         cmbTags.setFont(SYSConst.ARIAL14);
         cmbTags.setRenderer(NReportTAGSTools.getPBerichtTAGSRenderer());
         cmbTags.setSelectedIndex(0);
         cmbTags.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent itemEvent) {
                 if (initPhase || itemEvent.getStateChange() != ItemEvent.SELECTED || !(cmbTags.getSelectedItem() instanceof NReportTAGS))
                     return;
                 SYSFilesTools.print(NReportTools.getReportsAsHTML(NReportTools.getNReports4Tags(resident, (NReportTAGS) cmbTags.getSelectedItem()), false, true, null, null), true);
 
             }
         });
         list.add(cmbTags);
 //        SYSPropsTools.restoreState(internalClassID + ":cmbTags", cmbTags);
 
 //        tbFilesOnly = GUITools.getNiceToggleButton(OPDE.lang.getString("misc.filters.filesonly"));
 //        tbFilesOnly.addItemListener(new ItemListener() {
 //            @Override
 //            public void itemStateChanged(ItemEvent itemEvent) {
 //                if (initPhase) return;
 ////                SYSPropsTools.storeState(internalClassID + ":tbFilesOnly", tbFilesOnly);
 //                reload();
 //            }
 //        });
 //        labelPanel.add(tbFilesOnly);
 ////        SYSPropsTools.restoreState(internalClassID + ":tbFilesOnly", tbFilesOnly);
 //        tbFilesOnly.setHorizontalAlignment(SwingConstants.LEFT);
 
         tbShowReplaced = GUITools.getNiceToggleButton(OPDE.lang.getString("misc.filters.showreplaced"));
         tbShowReplaced.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent itemEvent) {
                 if (initPhase) return;
                 reloadDisplay();
             }
         });
         list.add(tbShowReplaced);
         tbShowReplaced.setHorizontalAlignment(SwingConstants.LEFT);
 
 //        JideButton resetButton = GUITools.createHyperlinkButton(OPDE.lang.getString("misc.commands.resetFilter"), SYSConst.icon22undo, new ActionListener() {
 //            @Override
 //            public void actionPerformed(ActionEvent actionEvent) {
 //                initPhase = true;
 //                cmbTags.setSelectedIndex(0);
 //                tbShowReplaced.setSelected(false);
 //                txtSearch.setText(null);
 //                initPhase = false;
 //                reloadDisplay();
 //            }
 //        });
 //        list.add(resetButton);
 
         return list;
     }
 
     private java.util.List<Component> addCommands() {
         java.util.List<Component> list = new ArrayList<Component>();
 
         /***
          *      _   _
          *     | \ | | _____      __
          *     |  \| |/ _ \ \ /\ / /
          *     | |\  |  __/\ V  V /
          *     |_| \_|\___| \_/\_/
          *
          */
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
             JideButton addButton = GUITools.createHyperlinkButton(OPDE.lang.getString("misc.commands.new"), new ImageIcon(getClass().getResource("/artwork/22x22/bw/add.png")), new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     if (!resident.isActive()) {
                         OPDE.getDisplayManager().addSubMessage(new DisplayMessage("misc.msg.cantChangeInactiveResident"));
                         return;
                     }
                     new DlgReport(new NReport(resident), new Closure() {
                         @Override
                         public void execute(Object report) {
                             if (report != null) {
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                     final NReport myReport = (NReport) em.merge(report);
                                     em.getTransaction().commit();
 
                                     final String keyYear = Integer.toString(new DateTime(myReport.getPit()).getYear()) + ".year";
                                     if (!cpMap.containsKey(keyYear)) {
                                         reloadDisplay();
                                     } else {
                                         final String keyDay = DateFormat.getDateInstance().format(myReport.getPit());
                                         synchronized (contentmap) {
                                             contentmap.remove(keyDay);
                                         }
                                         synchronized (valuecache) {
                                             if (valuecache.containsKey(keyDay)) {
                                                 valuecache.get(keyDay).add(myReport);
                                                 Collections.sort(valuecache.get(keyDay));
                                             }
                                         }
                                         createCP4Day(new DateMidnight(myReport.getPit()));
                                         expandDay(new DateMidnight(myReport.getPit()));
 
                                         buildPanel();
                                         GUITools.scroll2show(jspReports, cpMap.get(keyDay), cpsReports, new Closure() {
                                             @Override
                                             public void execute(Object o) {
                                                 GUITools.flashBackground(linemap.get(myReport), Color.YELLOW, 2);
                                             }
                                         });
                                     }
                                 } catch (OptimisticLockException ole) {
                                     if (em.getTransaction().isActive()) {
                                         em.getTransaction().rollback();
                                     }
                                     if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                         OPDE.getMainframe().emptyFrame();
                                         OPDE.getMainframe().afterLogin();
                                     } else {
                                         reloadDisplay(true);
                                     }
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
             list.add(addButton);
         }
         return list;
     }
 
     private void reloadDisplay() {
         reloadDisplay(false);
     }
 
     private void reloadDisplay(final boolean lockmessageAfterwards) {
         /***
          *               _                 _ ____  _           _
          *      _ __ ___| | ___   __ _  __| |  _ \(_)___ _ __ | | __ _ _   _
          *     | '__/ _ \ |/ _ \ / _` |/ _` | | | | / __| '_ \| |/ _` | | | |
          *     | | |  __/ | (_) | (_| | (_| | |_| | \__ \ |_) | | (_| | |_| |
          *     |_|  \___|_|\___/ \__,_|\__,_|____/|_|___/ .__/|_|\__,_|\__, |
          *                                              |_|            |___/
          */
 
         synchronized (contentmap) {
             SYSTools.clear(contentmap);
         }
         synchronized (cpMap) {
             SYSTools.clear(cpMap);
         }
 
         synchronized (linemap) {
             SYSTools.clear(linemap);
         }
         synchronized (valuecache) {
             SYSTools.clear(valuecache);
         }
 
         initPhase = true;
 
 
         OPDE.getMainframe().setBlocked(true);
         OPDE.getDisplayManager().setProgressBarMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.wait"), -1, 100));
 
         SwingWorker worker = new SwingWorker() {
             Date max = null;
 
             @Override
             protected Object doInBackground() throws Exception {
 
                 GUITools.setResidentDisplay(resident);
 
                 Pair<DateTime, DateTime> minmax = NReportTools.getMinMax(resident);
 
                holidays = Collections.synchronizedMap(SYSCalendar.getHolidays(minmax.getFirst().getYear(), minmax.getSecond().getYear()));
 
                 if (minmax != null) {
                     max = minmax.getSecond().toDate();
                     DateMidnight start = minmax.getFirst().toDateMidnight().dayOfMonth().withMinimumValue();
                     DateMidnight end = resident.isActive() ? new DateMidnight() : minmax.getSecond().toDateMidnight().dayOfMonth().withMinimumValue();
                     for (int year = end.getYear(); year >= start.getYear(); year--) {
                         createCP4Year(year, start, end);
                     }
                 }
 
                 return null;
             }
 
             @Override
             protected void done() {
                 expandTheLast2Weeks();
 
                 buildPanel();
                 initPhase = false;
                 OPDE.getDisplayManager().setProgressBarMessage(null);
                 OPDE.getMainframe().setBlocked(false);
                 if (lockmessageAfterwards) OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                 if (max != null) {
                     OPDE.getDisplayManager().addSubMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.lastEntry") + ": " + DateFormat.getDateInstance().format(max), 5));
                 } else {
                     OPDE.getDisplayManager().addSubMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.noentryyet"), 5));
                 }
             }
         };
         worker.execute();
 
 
     }
 
     private CollapsiblePane createCP4Year(final int year, DateMidnight min, DateMidnight max) {
         /***
          *                          _        ____ ____     __             __   _______    _    ____
          *       ___ _ __ ___  __ _| |_ ___ / ___|  _ \   / _| ___  _ __  \ \ / / ____|  / \  |  _ \
          *      / __| '__/ _ \/ _` | __/ _ \ |   | |_) | | |_ / _ \| '__|  \ V /|  _|   / _ \ | |_) |
          *     | (__| | |  __/ (_| | ||  __/ |___|  __/  |  _| (_) | |      | | | |___ / ___ \|  _ <
          *      \___|_|  \___|\__,_|\__\___|\____|_|     |_|  \___/|_|      |_| |_____/_/   \_\_| \_\
          *
          */
 
         final DateMidnight start = new DateMidnight(year, 1, 1).isBefore(min.dayOfMonth().withMinimumValue()) ? min.dayOfMonth().withMinimumValue() : new DateMidnight(year, 1, 1);
         final DateMidnight end = new DateMidnight(year, 12, 31).isAfter(max.dayOfMonth().withMaximumValue()) ? max.dayOfMonth().withMaximumValue() : new DateMidnight(year, 12, 31);
 
         final String keyYear = Integer.toString(year) + ".year";
         synchronized (cpMap) {
             if (!cpMap.containsKey(keyYear)) {
                 cpMap.put(keyYear, new CollapsiblePane());
                 try {
                     cpMap.get(keyYear).setCollapsed(true);
                 } catch (PropertyVetoException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
             }
         }
 
         final CollapsiblePane cpYear = cpMap.get(keyYear);
 
         String title = "<html><font size=+1>" +
                 "<b>" + Integer.toString(year) + "</b>" +
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
 
                 // somebody clicked on the year
                 for (DateMidnight month = end; month.compareTo(start) >= 0; month = month.minusMonths(1)) {
                     pnlContent.add(createCP4Month(month));
                 }
 
                 cpYear.setContentPane(pnlContent);
 
             }
         });
 //        cpYear.setBackground(getColor(vtype, SYSConst.light4));
 
         if (!cpYear.isCollapsed()) {
             JPanel pnlContent = new JPanel(new VerticalLayout());
 
             for (DateMidnight month = end; month.compareTo(start) >= 0; month = month.minusMonths(1)) {
                 pnlContent.add(createCP4Month(month));
             }
 
             cpYear.setContentPane(pnlContent);
             cpYear.setOpaque(false);
         }
 
         cpYear.setHorizontalAlignment(SwingConstants.LEADING);
         cpYear.setOpaque(false);
 
         return cpYear;
     }
 
 
     private CollapsiblePane createCP4Month(final DateMidnight month) {
         /***
          *                          _        ____ ____     __                      __  __  ___  _   _ _____ _   _
          *       ___ _ __ ___  __ _| |_ ___ / ___|  _ \   / _| ___  _ __    __ _  |  \/  |/ _ \| \ | |_   _| | | |
          *      / __| '__/ _ \/ _` | __/ _ \ |   | |_) | | |_ / _ \| '__|  / _` | | |\/| | | | |  \| | | | | |_| |
          *     | (__| | |  __/ (_| | ||  __/ |___|  __/  |  _| (_) | |    | (_| | | |  | | |_| | |\  | | | |  _  |
          *      \___|_|  \___|\__,_|\__\___|\____|_|     |_|  \___/|_|     \__,_| |_|  |_|\___/|_| \_| |_| |_| |_|
          *
          */
         final String key = monthFormatter.format(month.toDate()) + ".month";
         synchronized (cpMap) {
             if (!cpMap.containsKey(key)) {
                 cpMap.put(key, new CollapsiblePane());
                 try {
                     cpMap.get(key).setCollapsed(true);
                 } catch (PropertyVetoException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
             }
         }
         final CollapsiblePane cpMonth = cpMap.get(key);
 
         String title = "<html><font size=+1><b>" +
                 monthFormatter.format(month.toDate()) +
                 "</b>" +
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
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.PRINT, internalClassID)) {
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
                     SYSFilesTools.print(NReportTools.getReportsAsHTML(NReportTools.getNReports4Month(resident, month), false, true, null, null), true);
                 }
             });
             cptitle.getRight().add(btnPrintMonth);
         }
         cpMonth.setTitleLabelComponent(cptitle.getMain());
         cpMonth.setSlidingDirection(SwingConstants.SOUTH);
         cpMonth.setBackground(SYSConst.orange1[SYSConst.medium2]);
         cpMonth.setOpaque(true);
         cpMonth.setHorizontalAlignment(SwingConstants.LEADING);
 
         cpMonth.addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
             @Override
             public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                 cpMonth.setContentPane(createContentPanel4Month(month));
             }
         });
 
         if (!cpMonth.isCollapsed()) {
             cpMonth.setContentPane(createContentPanel4Month(month));
         }
 
 
         return cpMonth;
     }
 
 
     private JPanel createContentPanel4Month(DateMidnight month) {
         /***
          *                      _             _      __              __  __  ___  _   _ _____ _   _
          *       ___ ___  _ __ | |_ ___ _ __ | |_   / _| ___  _ __  |  \/  |/ _ \| \ | |_   _| | | |
          *      / __/ _ \| '_ \| __/ _ \ '_ \| __| | |_ / _ \| '__| | |\/| | | | |  \| | | | | |_| |
          *     | (_| (_) | | | | ||  __/ | | | |_  |  _| (_) | |    | |  | | |_| | |\  | | | |  _  |
          *      \___\___/|_| |_|\__\___|_| |_|\__| |_|  \___/|_|    |_|  |_|\___/|_| \_| |_| |_| |_|
          *
          */
 
 
 //        if (!contentmap.containsKey(key)) {
 
         JPanel pnlMonth = new JPanel(new VerticalLayout());
 
         pnlMonth.setOpaque(false);
 
         DateMidnight now = new DateMidnight();
 
         boolean sameMonth = now.dayOfMonth().withMaximumValue().equals(month.dayOfMonth().withMaximumValue());
 
         final DateMidnight start = sameMonth ? now : month.dayOfMonth().withMaximumValue();
         final DateMidnight end = month.dayOfMonth().withMinimumValue();
 
         for (DateMidnight week = start; end.compareTo(week) <= 0; week = week.minusWeeks(1)) {
             pnlMonth.add(createCP4Week(week));
         }
 
         return pnlMonth;
     }
 
     private CollapsiblePane createCP4Week(final DateMidnight week) {
         final String key = weekFormater.format(week.toDate()) + ".week";
         synchronized (cpMap) {
             if (!cpMap.containsKey(key)) {
                 cpMap.put(key, new CollapsiblePane());
                 try {
                     cpMap.get(key).setCollapsed(true);
                 } catch (PropertyVetoException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
             }
         }
         final CollapsiblePane cpWeek = cpMap.get(key);
 
         String title = "<html><font size=+1><b>" +
                 DateFormat.getDateInstance(DateFormat.SHORT).format(week.dayOfWeek().withMaximumValue().toDate()) + " - " +
                 DateFormat.getDateInstance(DateFormat.SHORT).format(week.dayOfWeek().withMinimumValue().toDate()) +
                 " (" +
                 OPDE.lang.getString("misc.msg.weekinyear") +
                 week.getWeekOfWeekyear() +
                 ")" +
                 "</b>" +
                 "</font></html>";
 
         DefaultCPTitle cptitle = new DefaultCPTitle(title, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     cpWeek.setCollapsed(!cpWeek.isCollapsed());
                 } catch (PropertyVetoException pve) {
                     // BAH!
                 }
             }
         });
 
         GUITools.addExpandCollapseButtons(cpWeek, cptitle.getRight());
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.PRINT, internalClassID)) {
             final JButton btnPrintWeek = new JButton(SYSConst.icon22print2);
             btnPrintWeek.setPressedIcon(SYSConst.icon22print2Pressed);
             btnPrintWeek.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnPrintWeek.setContentAreaFilled(false);
             btnPrintWeek.setBorder(null);
             btnPrintWeek.setToolTipText(OPDE.lang.getString("misc.tooltips.btnprintweek"));
             btnPrintWeek.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     SYSFilesTools.print(NReportTools.getReportsAsHTML(NReportTools.getNReports4Week(resident, week), false, true, null, null), true);
                 }
             });
             cptitle.getRight().add(btnPrintWeek);
         }
 
         cpWeek.setTitleLabelComponent(cptitle.getMain());
         cpWeek.setSlidingDirection(SwingConstants.SOUTH);
 
         cpWeek.setBackground(SYSConst.orange1[SYSConst.medium1]);
         cpWeek.setOpaque(false);
         cpWeek.setHorizontalAlignment(SwingConstants.LEADING);
 //        cpMonth.setBackground(getColor(vtype, SYSConst.light3));
 
         /***
          *           _ _      _            _                                       _   _
          *       ___| (_) ___| | _____  __| |   ___  _ __    _ __ ___   ___  _ __ | |_| |__
          *      / __| | |/ __| |/ / _ \/ _` |  / _ \| '_ \  | '_ ` _ \ / _ \| '_ \| __| '_ \
          *     | (__| | | (__|   <  __/ (_| | | (_) | | | | | | | | | | (_) | | | | |_| | | |
          *      \___|_|_|\___|_|\_\___|\__,_|  \___/|_| |_| |_| |_| |_|\___/|_| |_|\__|_| |_|
          *
          */
         cpWeek.addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
             @Override
             public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                 cpWeek.setContentPane(createWeekContentPanel4(week));
             }
         });
 
         if (!cpWeek.isCollapsed()) {
             cpWeek.setContentPane(createWeekContentPanel4(week));
         }
 
 
         return cpWeek;
     }
 
     private JPanel createWeekContentPanel4(DateMidnight week) {
         JPanel pnlWeek = new JPanel(new VerticalLayout());
 
         pnlWeek.setOpaque(false);
 
         DateMidnight now = new DateMidnight();
 
         boolean sameWeek = now.dayOfWeek().withMaximumValue().equals(week.dayOfWeek().withMaximumValue());
 
         final DateMidnight start = sameWeek ? now : week.dayOfWeek().withMaximumValue();
         final DateMidnight end = week.dayOfWeek().withMinimumValue();
 
         for (DateMidnight day = start; end.compareTo(day) <= 0; day = day.minusDays(1)) {
             pnlWeek.add(createCP4Day(day));
         }
 
         return pnlWeek;
     }
 
 
     private CollapsiblePane createCP4Day(final DateMidnight day) {
         final String key = DateFormat.getDateInstance().format(day.toDate());
         synchronized (cpMap) {
             if (!cpMap.containsKey(key)) {
                 cpMap.put(key, new CollapsiblePane());
                 try {
                     cpMap.get(key).setCollapsed(true);
                 } catch (PropertyVetoException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         }
         final CollapsiblePane cpDay = cpMap.get(key);
         String titleDay = "<html><font size=+1>" +
                 dayFormat.format(day.toDate()) +
                SYSTools.catchNull(holidays.get(day), " (", ")") +
                 "</font></html>";
         final DefaultCPTitle titleCPDay = new DefaultCPTitle(titleDay, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     cpDay.setCollapsed(!cpDay.isCollapsed());
                 } catch (PropertyVetoException pve) {
                     // BAH!
                 }
             }
         });
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.PRINT, internalClassID)) {
             final JButton btnPrintDay = new JButton(SYSConst.icon22print2);
             btnPrintDay.setPressedIcon(SYSConst.icon22print2Pressed);
             btnPrintDay.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnPrintDay.setContentAreaFilled(false);
             btnPrintDay.setBorder(null);
             btnPrintDay.setToolTipText(OPDE.lang.getString("misc.tooltips.btnprintday"));
             btnPrintDay.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     SYSFilesTools.print(NReportTools.getReportsAsHTML(NReportTools.getNReports4Day(resident, day), false, true, null, null), true);
                 }
             });
             titleCPDay.getRight().add(btnPrintDay);
         }
 
         cpDay.setTitleLabelComponent(titleCPDay.getMain());
         cpDay.setSlidingDirection(SwingConstants.SOUTH);
 
        if (holidays.containsKey(day)) {
             cpDay.setBackground(SYSConst.red1[SYSConst.medium1]);
         } else if (day.getDayOfWeek() == DateTimeConstants.SATURDAY || day.getDayOfWeek() == DateTimeConstants.SUNDAY) {
             cpDay.setBackground(SYSConst.red1[SYSConst.light3]);
         } else {
             cpDay.setBackground(SYSConst.orange1[SYSConst.light3]);
         }
         cpDay.setOpaque(true);
 
         cpDay.setHorizontalAlignment(SwingConstants.LEADING);
         cpDay.setStyle(CollapsiblePane.PLAIN_STYLE);
         cpDay.addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
             @Override
             public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                 cpDay.setContentPane(createContentPanel4Day(day));
             }
         });
 
         if (!cpDay.isCollapsed()) {
             cpDay.setContentPane(createContentPanel4Day(day));
         }
 
         return cpDay;
     }
 
     private JPanel createContentPanel4Day(DateMidnight day) {
 //        OPDE.getDisplayManager().setProgressBarMessage(new DisplayMessage("misc.msg.wait", progress, progressMax));
 //        progress++;
 
         final String key = DateFormat.getDateInstance().format(day.toDate());
         synchronized (contentmap) {
             if (contentmap.containsKey(key)) {
                 return contentmap.get(key);
             }
         }
         final JPanel dayPanel = new JPanel(new VerticalLayout());
         dayPanel.setOpaque(false);
         synchronized (valuecache) {
             if (!valuecache.containsKey(key)) {
                 valuecache.put(key, NReportTools.getNReports4Day(resident, day));
             }
 
 
             int i = 0; // for zebra pattern
             for (final NReport nreport : valuecache.get(key)) {
 
                 if (tbShowReplaced.isSelected() || !nreport.isObsolete()) {
 
                     String title = "<html><table border=\"0\">" +
                             "<tr valign=\"top\">" +
                             "<td width=\"100\" align=\"left\">" + DateFormat.getTimeInstance(DateFormat.SHORT).format(nreport.getPit()) +
                             " " + OPDE.lang.getString("misc.msg.Time.short") +
                             "<br/>" + nreport.getMinutes() + " " + OPDE.lang.getString("misc.msg.Minute(s)") +
                             "</td>" +
                             "<td width=\"100\" align=\"left\">" + SYSTools.catchNull(NReportTools.getTagsAsHTML(nreport), " [", "]") + "</td>" +
                             "<td width=\"400\" align=\"left\">" +
                             SYSTools.replace(nreport.getText(), "\n", "<br/>", false) +
                             "</td>" +
                             "<td width=\"200\" align=\"left\">" + nreport.getUser().getFullname() + "</td>" +
                             "</tr>" +
                             "</table>" +
                             "</html>";
 
                     final DefaultCPTitle pnlSingle = new DefaultCPTitle(SYSTools.toHTMLForScreen(title), null);
                     if (nreport.isObsolete()) {
                         pnlSingle.getButton().setIcon(SYSConst.icon22eraser);
                     } else if (nreport.isReplacement()) {
                         pnlSingle.getButton().setIcon(SYSConst.icon22edited);
                     }
                     pnlSingle.getButton().addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             GUITools.showPopup(GUITools.getHTMLPopup(pnlSingle.getButton(), NReportTools.getInfoAsHTML(nreport)), SwingConstants.NORTH);
                         }
                     });
 
                     if (!nreport.getAttachedFilesConnections().isEmpty()) {
                         /***
                          *      _     _         _____ _ _
                          *     | |__ | |_ _ __ |  ___(_) | ___  ___
                          *     | '_ \| __| '_ \| |_  | | |/ _ \/ __|
                          *     | |_) | |_| | | |  _| | | |  __/\__ \
                          *     |_.__/ \__|_| |_|_|   |_|_|\___||___/
                          *
                          */
                         final JButton btnFiles = new JButton(Integer.toString(nreport.getAttachedFilesConnections().size()), SYSConst.icon22greenStar);
                         btnFiles.setToolTipText(OPDE.lang.getString("misc.btnfiles.tooltip"));
                         btnFiles.setForeground(Color.BLUE);
                         btnFiles.setHorizontalTextPosition(SwingUtilities.CENTER);
                         btnFiles.setFont(SYSConst.ARIAL18BOLD);
                         btnFiles.setPressedIcon(SYSConst.icon22Pressed);
                         btnFiles.setAlignmentX(Component.RIGHT_ALIGNMENT);
                         btnFiles.setAlignmentY(Component.TOP_ALIGNMENT);
                         btnFiles.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                         btnFiles.setContentAreaFilled(false);
                         btnFiles.setBorder(null);
 
                         btnFiles.addActionListener(new ActionListener() {
                             @Override
                             public void actionPerformed(ActionEvent actionEvent) {
                                 Closure fileHandleClosure = OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID) ? null : new Closure() {
                                     @Override
                                     public void execute(Object o) {
                                         EntityManager em = OPDE.createEM();
                                         final NReport myReport = em.merge(nreport);
                                         em.refresh(myReport);
                                         em.close();
 
                                         final String keyNewDay = DateFormat.getDateInstance().format(myReport.getPit());
 
                                         synchronized (contentmap) {
                                             contentmap.remove(keyNewDay);
                                         }
                                         synchronized (linemap) {
                                             linemap.remove(nreport);
                                         }
 
                                         synchronized (valuecache) {
                                             valuecache.get(keyNewDay).remove(nreport);
                                             valuecache.get(keyNewDay).add(myReport);
                                             Collections.sort(valuecache.get(keyNewDay));
                                         }
 
                                         createCP4Day(new DateMidnight(myReport.getPit()));
 
                                         buildPanel();
                                         GUITools.flashBackground(linemap.get(myReport), Color.YELLOW, 2);
                                     }
                                 };
                                 new DlgFiles(nreport, fileHandleClosure);
                             }
                         });
                         btnFiles.setEnabled(OPDE.isFTPworking());
                         pnlSingle.getRight().add(btnFiles);
                     }
 
 
                     if (!nreport.getAttachedQProcessConnections().isEmpty()) {
                         /***
                          *      _     _         ____
                          *     | |__ | |_ _ __ |  _ \ _ __ ___   ___ ___  ___ ___
                          *     | '_ \| __| '_ \| |_) | '__/ _ \ / __/ _ \/ __/ __|
                          *     | |_) | |_| | | |  __/| | | (_) | (_|  __/\__ \__ \
                          *     |_.__/ \__|_| |_|_|   |_|  \___/ \___\___||___/___/
                          *
                          */
                         final JButton btnProcess = new JButton(Integer.toString(nreport.getAttachedQProcessConnections().size()), SYSConst.icon22redStar);
                         btnProcess.setToolTipText(OPDE.lang.getString("misc.btnprocess.tooltip"));
                         btnProcess.setForeground(Color.YELLOW);
                         btnProcess.setHorizontalTextPosition(SwingUtilities.CENTER);
                         btnProcess.setFont(SYSConst.ARIAL18BOLD);
                         btnProcess.setPressedIcon(SYSConst.icon22Pressed);
                         btnProcess.setAlignmentX(Component.RIGHT_ALIGNMENT);
                         btnProcess.setAlignmentY(Component.TOP_ALIGNMENT);
                         btnProcess.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                         btnProcess.setContentAreaFilled(false);
                         btnProcess.setBorder(null);
                         btnProcess.addActionListener(new ActionListener() {
                             @Override
                             public void actionPerformed(ActionEvent actionEvent) {
                                 new DlgProcessAssign(nreport, new Closure() {
                                     @Override
                                     public void execute(Object o) {
                                         if (o == null) {
                                             return;
                                         }
                                         Pair<ArrayList<QProcess>, ArrayList<QProcess>> result = (Pair<ArrayList<QProcess>, ArrayList<QProcess>>) o;
 
                                         ArrayList<QProcess> assigned = result.getFirst();
                                         ArrayList<QProcess> unassigned = result.getSecond();
 
                                         EntityManager em = OPDE.createEM();
 
                                         try {
                                             em.getTransaction().begin();
 
                                             em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                             NReport myReport = em.merge(nreport);
                                             em.lock(myReport, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
 
                                             ArrayList<SYSNR2PROCESS> attached = new ArrayList<SYSNR2PROCESS>(myReport.getAttachedQProcessConnections());
                                             for (SYSNR2PROCESS linkObject : attached) {
                                                 if (unassigned.contains(linkObject.getQProcess())) {
                                                     linkObject.getQProcess().getAttachedNReportConnections().remove(linkObject);
                                                     linkObject.getNReport().getAttachedQProcessConnections().remove(linkObject);
                                                     em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_REMOVE_ELEMENT) + ": " + nreport.getTitle() + " ID: " + nreport.getID(), PReportTools.PREPORT_TYPE_REMOVE_ELEMENT, linkObject.getQProcess()));
 
                                                     em.remove(linkObject);
                                                 }
                                             }
                                             attached.clear();
 
                                             for (QProcess qProcess : assigned) {
                                                 java.util.List<QProcessElement> listElements = qProcess.getElements();
                                                 if (!listElements.contains(myReport)) {
                                                     QProcess myQProcess = em.merge(qProcess);
                                                     SYSNR2PROCESS myLinkObject = em.merge(new SYSNR2PROCESS(myQProcess, myReport));
                                                     em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_ASSIGN_ELEMENT) + ": " + nreport.getTitle() + " ID: " + nreport.getID(), PReportTools.PREPORT_TYPE_ASSIGN_ELEMENT, myQProcess));
                                                     qProcess.getAttachedNReportConnections().add(myLinkObject);
                                                     myReport.getAttachedQProcessConnections().add(myLinkObject);
                                                 }
                                             }
 
                                             em.getTransaction().commit();
 
                                             final String keyNewDay = DateFormat.getDateInstance().format(myReport.getPit());
 
 
                                             synchronized (contentmap) {
                                                 contentmap.remove(keyNewDay);
                                             }
                                             synchronized (linemap) {
                                                 linemap.remove(nreport);
                                             }
 
                                             synchronized (valuecache) {
                                                 valuecache.get(keyNewDay).remove(nreport);
                                                 valuecache.get(keyNewDay).add(myReport);
                                                 Collections.sort(valuecache.get(keyNewDay));
                                             }
 
                                             createCP4Day(new DateMidnight(myReport.getPit()));
 
                                             buildPanel();
                                             GUITools.flashBackground(linemap.get(myReport), Color.YELLOW, 2);
                                         } catch (OptimisticLockException ole) {
                                             if (em.getTransaction().isActive()) {
                                                 em.getTransaction().rollback();
                                             }
                                             if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                                 OPDE.getMainframe().emptyFrame();
                                                 OPDE.getMainframe().afterLogin();
                                             } else {
                                                 reloadDisplay(true);
                                             }
                                         } catch (RollbackException ole) {
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
                                 });
                             }
                         });
                         btnProcess.setEnabled(OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID));
                         pnlSingle.getRight().add(btnProcess);
                     }
 
                     /***
                      *      __  __
                      *     |  \/  | ___ _ __  _   _
                      *     | |\/| |/ _ \ '_ \| | | |
                      *     | |  | |  __/ | | | |_| |
                      *     |_|  |_|\___|_| |_|\__,_|
                      *
                      */
                     final JButton btnMenu = new JButton(SYSConst.icon22menu);
                     btnMenu.setPressedIcon(SYSConst.icon22Pressed);
                     btnMenu.setAlignmentX(Component.RIGHT_ALIGNMENT);
                     btnMenu.setAlignmentY(Component.TOP_ALIGNMENT);
                     btnMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                     btnMenu.setContentAreaFilled(false);
                     btnMenu.setBorder(null);
                     btnMenu.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             JidePopup popup = new JidePopup();
                             popup.setMovable(false);
                             popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
                             popup.setOwner(btnMenu);
                             popup.removeExcludedComponent(btnMenu);
                             JPanel pnl = getMenu(nreport);
                             popup.getContentPane().add(pnl);
                             popup.setDefaultFocusComponent(pnl);
 
                             GUITools.showPopup(popup, SwingConstants.WEST);
                         }
                     });
                     btnMenu.setEnabled(!nreport.isObsolete());
                     pnlSingle.getRight().add(btnMenu);
 
                     JPanel zebra = new JPanel();
                     zebra.setLayout(new BoxLayout(zebra, BoxLayout.LINE_AXIS));
                     zebra.setOpaque(true);
                     if (i % 2 == 0) {
                         zebra.setBackground(SYSConst.orange1[SYSConst.light2]);
                     } else {
                         zebra.setBackground(Color.WHITE);
                     }
                     zebra.add(pnlSingle.getMain());
                     i++;
 
                     dayPanel.add(zebra);
                     linemap.put(nreport, pnlSingle.getMain());
                 }
             }
         }
         synchronized (contentmap) {
             contentmap.put(key, dayPanel);
         }
         return dayPanel;
     }
 
     private void buildPanel() {
         cpsReports.removeAll();
         cpsReports.setLayout(new JideBoxLayout(cpsReports, JideBoxLayout.Y_AXIS));
 
         synchronized (cpMap) {
             Pair<DateTime, DateTime> minmax = NReportTools.getMinMax(resident);
             if (minmax != null) {
                 DateMidnight start = minmax.getFirst().toDateMidnight().dayOfMonth().withMinimumValue();
                 DateMidnight end = resident.isActive() ? new DateMidnight() : minmax.getSecond().toDateMidnight().dayOfMonth().withMinimumValue();
                 for (int year = end.getYear(); year >= start.getYear(); year--) {
                     final String keyYear = Integer.toString(year) + ".year";
 
                     cpsReports.add(cpMap.get(keyYear));
                 }
             }
         }
         cpsReports.addExpansion();
     }
 
     private void expandDay(DateMidnight day) {
         final String keyYear = Integer.toString(day.getYear()) + ".year";
         if (cpMap.containsKey(keyYear) && cpMap.get(keyYear).isCollapsed()) {
             try {
                 cpMap.get(keyYear).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
         final String keyMonth = monthFormatter.format(day.toDate()) + ".month";
         if (cpMap.containsKey(keyMonth) && cpMap.get(keyMonth).isCollapsed()) {
             try {
                 cpMap.get(keyMonth).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
         final String keyThisWeek = weekFormater.format(day.toDate()) + ".week";
         if (cpMap.containsKey(keyThisWeek) && cpMap.get(keyThisWeek).isCollapsed()) {
             try {
                 cpMap.get(keyThisWeek).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
         final String keyDay = DateFormat.getDateInstance().format(day.toDate());
         if (cpMap.containsKey(keyDay) && cpMap.get(keyDay).isCollapsed()) {
             try {
                 cpMap.get(keyDay).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
     }
 
 
     private JPanel getMenu(final NReport nreport) {
 
         JPanel pnlMenu = new JPanel(new VerticalLayout());
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
             /***
              *      _____    _ _ _
              *     | ____|__| (_) |_
              *     |  _| / _` | | __|
              *     | |__| (_| | | |_
              *     |_____\__,_|_|\__|
              *
              */
             final JButton btnEdit = GUITools.createHyperlinkButton(internalClassID + ".btnEdit.tooltip", SYSConst.icon22edit3, null);
             btnEdit.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnEdit.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     new DlgReport(nreport.clone(), new Closure() {
                         @Override
                         public void execute(Object o) {
                             if (o != null) {
 
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                     final NReport newReport = em.merge((NReport) o);
                                     NReport oldReport = em.merge(nreport);
                                     em.lock(oldReport, LockModeType.OPTIMISTIC);
                                     newReport.setReplacementFor(oldReport);
 
                                     for (SYSNR2FILE oldAssignment : oldReport.getAttachedFilesConnections()) {
                                         em.remove(oldAssignment);
                                     }
                                     oldReport.getAttachedFilesConnections().clear();
                                     for (SYSNR2PROCESS oldAssignment : oldReport.getAttachedQProcessConnections()) {
                                         em.remove(oldAssignment);
                                     }
                                     oldReport.getAttachedQProcessConnections().clear();
 
                                     oldReport.setEditedBy(em.merge(OPDE.getLogin().getUser()));
                                     oldReport.setEditDate(new Date());
                                     oldReport.setReplacedBy(newReport);
 
                                     em.getTransaction().commit();
 
                                     final String keyNewDay = DateFormat.getDateInstance().format(newReport.getPit());
                                     final String keyOldDay = DateFormat.getDateInstance().format(oldReport.getPit());
 
                                     synchronized (contentmap) {
                                         contentmap.remove(keyNewDay);
                                         contentmap.remove(keyOldDay);
                                     }
                                     synchronized (linemap) {
                                         linemap.remove(oldReport);
                                     }
 
                                     synchronized (valuecache) {
                                         valuecache.get(keyOldDay).remove(nreport);
                                         valuecache.get(keyOldDay).add(oldReport);
                                         Collections.sort(valuecache.get(keyOldDay));
 
                                         if (valuecache.containsKey(keyNewDay)) {
                                             valuecache.get(keyNewDay).add(newReport);
                                             Collections.sort(valuecache.get(keyNewDay));
                                         }
                                     }
 
                                     createCP4Day(new DateMidnight(oldReport.getPit()));
                                     createCP4Day(new DateMidnight(newReport.getPit()));
 
                                     buildPanel();
                                     GUITools.scroll2show(jspReports, cpMap.get(keyNewDay), cpsReports, new Closure() {
                                         @Override
                                         public void execute(Object o) {
                                             GUITools.flashBackground(linemap.get(newReport), Color.YELLOW, 2);
                                         }
                                     });
                                 } catch (OptimisticLockException ole) {
                                     if (em.getTransaction().isActive()) {
                                         em.getTransaction().rollback();
                                     }
                                     if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                         OPDE.getMainframe().emptyFrame();
                                         OPDE.getMainframe().afterLogin();
                                     } else {
                                         reloadDisplay(true);
                                     }
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
             btnEdit.setEnabled(NReportTools.isChangeable(nreport));
             pnlMenu.add(btnEdit);
             /***
              *      ____       _      _
              *     |  _ \  ___| | ___| |_ ___
              *     | | | |/ _ \ |/ _ \ __/ _ \
              *     | |_| |  __/ |  __/ ||  __/
              *     |____/ \___|_|\___|\__\___|
              *
              */
             final JButton btnDelete = GUITools.createHyperlinkButton(internalClassID + ".btnDelete.tooltip", SYSConst.icon22delete, null);
             btnDelete.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnDelete.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     new DlgYesNo(OPDE.lang.getString("misc.questions.delete1") + "<br/><i>" + DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT).format(nreport.getPit()) + "</i><br/>" + OPDE.lang.getString("misc.questions.delete2"), SYSConst.icon48delete, new Closure() {
                         @Override
                         public void execute(Object answer) {
                             if (answer.equals(JOptionPane.YES_OPTION)) {
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                     final NReport delReport = em.merge(nreport);
                                     em.lock(delReport, LockModeType.OPTIMISTIC);
                                     delReport.setDeletedBy(em.merge(OPDE.getLogin().getUser()));
                                     for (SYSNR2FILE oldAssignment : delReport.getAttachedFilesConnections()) {
                                         em.remove(oldAssignment);
                                     }
                                     delReport.getAttachedFilesConnections().clear();
                                     for (SYSNR2PROCESS oldAssignment : delReport.getAttachedQProcessConnections()) {
                                         em.remove(oldAssignment);
                                     }
                                     delReport.getAttachedQProcessConnections().clear();
                                     em.getTransaction().commit();
 
                                     final String keyDay = DateFormat.getDateInstance().format(delReport.getPit());
 
 
                                     synchronized (contentmap) {
                                         contentmap.remove(keyDay);
                                     }
                                     synchronized (linemap) {
                                         linemap.remove(delReport);
                                     }
 
                                     synchronized (valuecache) {
                                         valuecache.get(keyDay).remove(nreport);
                                         valuecache.get(keyDay).add(delReport);
                                         Collections.sort(valuecache.get(keyDay));
                                     }
 
                                     createCP4Day(new DateMidnight(delReport.getPit()));
 
                                     buildPanel();
                                     if (tbShowReplaced.isSelected()) {
                                         GUITools.flashBackground(linemap.get(delReport), Color.YELLOW, 2);
                                     }
                                 } catch (OptimisticLockException ole) {
                                     if (em.getTransaction().isActive()) {
                                         em.getTransaction().rollback();
                                     }
                                     if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                         OPDE.getMainframe().emptyFrame();
                                         OPDE.getMainframe().afterLogin();
                                     } else {
                                         reloadDisplay(true);
                                     }
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
             btnDelete.setEnabled(NReportTools.isChangeable(nreport));
             pnlMenu.add(btnDelete);
 
 
             /***
              *      _     _       _____  _    ____
              *     | |__ | |_ _ _|_   _|/ \  / ___|___
              *     | '_ \| __| '_ \| | / _ \| |  _/ __|
              *     | |_) | |_| | | | |/ ___ \ |_| \__ \
              *     |_.__/ \__|_| |_|_/_/   \_\____|___/
              *
              */
             final JButton btnTAGs = GUITools.createHyperlinkButton(internalClassID + ".btntags.tooltip", SYSConst.icon22checkbox, null);
             btnTAGs.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnTAGs.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     JidePopup popup = new JidePopup();
                     ItemListener il = new ItemListener() {
                         @Override
                         public void itemStateChanged(ItemEvent itemEvent) {
                             JCheckBox cb = (JCheckBox) itemEvent.getSource();
                             NReportTAGS tag = (NReportTAGS) cb.getClientProperty("UserObject");
 
                             EntityManager em = OPDE.createEM();
                             try {
                                 em.getTransaction().begin();
                                 em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                 final NReport myReport = em.merge(nreport);
                                 em.lock(myReport, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
 
                                 if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                                     myReport.getTags().remove(tag);
                                 } else {
                                     myReport.getTags().add(tag);
                                 }
                                 em.getTransaction().commit();
 
                                 final String keyNewDay = DateFormat.getDateInstance().format(myReport.getPit());
 
 
                                 synchronized (contentmap) {
                                     contentmap.remove(keyNewDay);
                                 }
                                 synchronized (linemap) {
                                     linemap.remove(nreport);
                                 }
 
                                 synchronized (valuecache) {
                                     valuecache.get(keyNewDay).remove(nreport);
                                     valuecache.get(keyNewDay).add(myReport);
                                     Collections.sort(valuecache.get(keyNewDay));
                                 }
 
                                 createCP4Day(new DateMidnight(myReport.getPit()));
 
                                 buildPanel();
                                 GUITools.flashBackground(linemap.get(myReport), Color.YELLOW, 2);
                             } catch (OptimisticLockException ole) {
                                 if (em.getTransaction().isActive()) {
                                     em.getTransaction().rollback();
                                 }
                                 if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                     OPDE.getMainframe().emptyFrame();
                                     OPDE.getMainframe().afterLogin();
                                 } else {
                                     reloadDisplay(true);
                                 }
                             } catch (RollbackException ole) {
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
                     };
 
                     popup = new JidePopup();
                     JPanel pnl = NReportTAGSTools.createCheckBoxPanelForTags(il, nreport.getTags(), new GridLayout(8, 4));
                     popup.setMovable(false);
                     popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
                     popup.setOwner(btnTAGs);
                     popup.removeExcludedComponent(btnTAGs);
                     popup.getContentPane().add(pnl);
                     popup.setDefaultFocusComponent(pnl);
 
                     GUITools.showPopup(popup, SwingConstants.WEST);
 
                 }
             });
             btnTAGs.setEnabled(NReportTools.isChangeable(nreport) && NReportTools.isMine(nreport));
             pnlMenu.add(btnTAGs);
 
             /***
              *      _     _         __  __ _             _
              *     | |__ | |_ _ __ |  \/  (_)_ __  _   _| |_ ___  ___
              *     | '_ \| __| '_ \| |\/| | | '_ \| | | | __/ _ \/ __|
              *     | |_) | |_| | | | |  | | | | | | |_| | ||  __/\__ \
              *     |_.__/ \__|_| |_|_|  |_|_|_| |_|\__,_|\__\___||___/
              *
              */
             final JButton btnMinutes = GUITools.createHyperlinkButton(internalClassID + ".btnminutes.tooltip", SYSConst.icon22clock, null);
             btnMinutes.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnMinutes.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
 
                     final JPopupMenu menu = SYSCalendar.getMinutesMenu(new int[]{1, 2, 3, 4, 5, 10, 15, 20, 30, 45, 60, 120, 240, 360}, new Closure() {
                         @Override
                         public void execute(Object o) {
                             EntityManager em = OPDE.createEM();
                             try {
                                 em.getTransaction().begin();
 
                                 em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                 NReport myReport = em.merge(nreport);
                                 em.lock(myReport, LockModeType.OPTIMISTIC);
 
                                 myReport.setMinutes((Integer) o);
                                 myReport.setEditDate(new Date());
 
                                 em.getTransaction().commit();
 
                                 final String keyNewDay = DateFormat.getDateInstance().format(myReport.getPit());
 
 
                                 synchronized (contentmap) {
                                     contentmap.remove(keyNewDay);
                                 }
                                 synchronized (linemap) {
                                     linemap.remove(nreport);
                                 }
 
                                 synchronized (valuecache) {
                                     valuecache.get(keyNewDay).remove(nreport);
                                     valuecache.get(keyNewDay).add(myReport);
                                     Collections.sort(valuecache.get(keyNewDay));
                                 }
 
                                 createCP4Day(new DateMidnight(myReport.getPit()));
 
                                 buildPanel();
                                 GUITools.flashBackground(linemap.get(myReport), Color.YELLOW, 2);
                             } catch (OptimisticLockException ole) {
                                 if (em.getTransaction().isActive()) {
                                     em.getTransaction().rollback();
                                 }
                                 if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                     OPDE.getMainframe().emptyFrame();
                                     OPDE.getMainframe().afterLogin();
                                 } else {
                                     reloadDisplay(true);
                                 }
                             } catch (Exception e) {
                                 if (em.getTransaction().isActive()) {
                                     em.getTransaction().rollback();
                                 }
                                 OPDE.fatal(e);
                             } finally {
                                 em.close();
                             }
                         }
                     });
 
                     menu.show(btnMinutes, 0, btnMinutes.getHeight());
                 }
             });
             btnMinutes.setEnabled(!nreport.isObsolete() && NReportTools.isMine(nreport));
             pnlMenu.add(btnMinutes);
 
             pnlMenu.add(new JSeparator());
 
             /***
              *      _     _         _____ _ _
              *     | |__ | |_ _ __ |  ___(_) | ___  ___
              *     | '_ \| __| '_ \| |_  | | |/ _ \/ __|
              *     | |_) | |_| | | |  _| | | |  __/\__ \
              *     |_.__/ \__|_| |_|_|   |_|_|\___||___/
              *
              */
             final JButton btnFiles = GUITools.createHyperlinkButton("misc.btnfiles.tooltip", SYSConst.icon22attach, null);
             btnFiles.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnFiles.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     Closure fileHandleClosure = nreport.isObsolete() ? null : new Closure() {
                         @Override
                         public void execute(Object o) {
                             EntityManager em = OPDE.createEM();
                             final NReport myReport = em.merge(nreport);
                             em.refresh(myReport);
                             em.close();
 
                             final String keyNewDay = DateFormat.getDateInstance().format(myReport.getPit());
 
 
                             synchronized (contentmap) {
                                 contentmap.remove(keyNewDay);
                             }
                             synchronized (linemap) {
                                 linemap.remove(nreport);
                             }
 
                             synchronized (valuecache) {
                                 valuecache.get(keyNewDay).remove(nreport);
                                 valuecache.get(keyNewDay).add(myReport);
                                 Collections.sort(valuecache.get(keyNewDay));
                             }
 
                             createCP4Day(new DateMidnight(myReport.getPit()));
 
                             buildPanel();
                             GUITools.flashBackground(linemap.get(myReport), Color.YELLOW, 2);
                         }
                     };
                     new DlgFiles(nreport, fileHandleClosure);
                 }
             });
             btnFiles.setEnabled(OPDE.isFTPworking());
             pnlMenu.add(btnFiles);
 
 
             /***
              *      _     _         ____
              *     | |__ | |_ _ __ |  _ \ _ __ ___   ___ ___  ___ ___
              *     | '_ \| __| '_ \| |_) | '__/ _ \ / __/ _ \/ __/ __|
              *     | |_) | |_| | | |  __/| | | (_) | (_|  __/\__ \__ \
              *     |_.__/ \__|_| |_|_|   |_|  \___/ \___\___||___/___/
              *
              */
             final JButton btnProcess = GUITools.createHyperlinkButton("misc.btnprocess.tooltip", SYSConst.icon22link, null);
             btnProcess.setAlignmentX(Component.RIGHT_ALIGNMENT);
             btnProcess.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     new DlgProcessAssign(nreport, new Closure() {
                         @Override
                         public void execute(Object o) {
                             if (o == null) {
                                 return;
                             }
                             Pair<ArrayList<QProcess>, ArrayList<QProcess>> result = (Pair<ArrayList<QProcess>, ArrayList<QProcess>>) o;
 
                             ArrayList<QProcess> assigned = result.getFirst();
                             ArrayList<QProcess> unassigned = result.getSecond();
 
                             EntityManager em = OPDE.createEM();
 
                             try {
                                 em.getTransaction().begin();
 
                                 em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                 NReport myReport = em.merge(nreport);
                                 em.lock(myReport, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
 
                                 ArrayList<SYSNR2PROCESS> attached = new ArrayList<SYSNR2PROCESS>(myReport.getAttachedQProcessConnections());
                                 for (SYSNR2PROCESS linkObject : attached) {
                                     if (unassigned.contains(linkObject.getQProcess())) {
                                         linkObject.getQProcess().getAttachedNReportConnections().remove(linkObject);
                                         linkObject.getNReport().getAttachedQProcessConnections().remove(linkObject);
                                         em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_REMOVE_ELEMENT) + ": " + nreport.getTitle() + " ID: " + nreport.getID(), PReportTools.PREPORT_TYPE_REMOVE_ELEMENT, linkObject.getQProcess()));
                                         em.remove(linkObject);
                                     }
                                 }
                                 attached.clear();
 
                                 for (QProcess qProcess : assigned) {
                                     java.util.List<QProcessElement> listElements = qProcess.getElements();
                                     if (!listElements.contains(myReport)) {
                                         QProcess myQProcess = em.merge(qProcess);
                                         SYSNR2PROCESS myLinkObject = em.merge(new SYSNR2PROCESS(myQProcess, myReport));
                                         em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_ASSIGN_ELEMENT) + ": " + nreport.getTitle() + " ID: " + nreport.getID(), PReportTools.PREPORT_TYPE_ASSIGN_ELEMENT, myQProcess));
                                         qProcess.getAttachedNReportConnections().add(myLinkObject);
                                         myReport.getAttachedQProcessConnections().add(myLinkObject);
                                     }
                                 }
 
                                 em.getTransaction().commit();
 
                                 final String keyNewDay = DateFormat.getDateInstance().format(myReport.getPit());
 
 
                                 synchronized (contentmap) {
                                     contentmap.remove(keyNewDay);
                                 }
                                 synchronized (linemap) {
                                     linemap.remove(nreport);
                                 }
 
                                 synchronized (valuecache) {
                                     valuecache.get(keyNewDay).remove(nreport);
                                     valuecache.get(keyNewDay).add(myReport);
                                     Collections.sort(valuecache.get(keyNewDay));
                                 }
 
                                 createCP4Day(new DateMidnight(myReport.getPit()));
 
                                 buildPanel();
                                 GUITools.flashBackground(linemap.get(myReport), Color.YELLOW, 2);
                             } catch (OptimisticLockException ole) {
                                 if (em.getTransaction().isActive()) {
                                     em.getTransaction().rollback();
                                 }
                                 if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                     OPDE.getMainframe().emptyFrame();
                                     OPDE.getMainframe().afterLogin();
                                 } else {
                                     reloadDisplay(true);
                                 }
                             } catch (RollbackException ole) {
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
                     });
                 }
             });
             pnlMenu.add(btnProcess);
 
         }
         return pnlMenu;
     }
 
     private void expandTheLast2Weeks() {
         final String keyYear = Integer.toString(new DateTime().getYear()) + ".year";
         if (cpMap.containsKey(keyYear)) {
             try {
                 cpMap.get(keyYear).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
         final String keyMonth = monthFormatter.format(new Date()) + ".month";
         if (cpMap.containsKey(keyMonth)) {
             try {
                 cpMap.get(keyMonth).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
         final String keyThisWeek = weekFormater.format(new Date()) + ".week";
         if (cpMap.containsKey(keyThisWeek)) {
             try {
                 GUITools.setCollapsed(cpMap.get(keyThisWeek), false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
 
         final String keyLastYear = Integer.toString(new DateMidnight().minusWeeks(1).getYear()) + ".year";
         if (cpMap.containsKey(keyLastYear)) {
             try {
                 cpMap.get(keyLastYear).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
         final String keyLastMonth = monthFormatter.format(new DateMidnight().minusWeeks(1).toDate()) + ".month";
         if (cpMap.containsKey(keyLastMonth)) {
             try {
                 cpMap.get(keyLastMonth).setCollapsed(false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
         final String keyLastWeek = weekFormater.format(new DateMidnight().minusWeeks(1).toDate()) + ".week";
         if (cpMap.containsKey(keyLastWeek)) {
             try {
                 GUITools.setCollapsed(cpMap.get(keyLastWeek), false);
             } catch (PropertyVetoException e) {
                 // bah!
             }
         }
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JScrollPane jspReports;
     private CollapsiblePanes cpsReports;
     // End of variables declaration//GEN-END:variables
 }
