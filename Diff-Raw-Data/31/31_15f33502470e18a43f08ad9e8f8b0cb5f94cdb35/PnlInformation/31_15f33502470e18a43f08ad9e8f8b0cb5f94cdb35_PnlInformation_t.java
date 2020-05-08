 /*
  * Created by JFormDesigner on Fri Apr 12 15:56:27 CEST 2013
  */
 
 package op.care.info;
 
 import com.jidesoft.pane.CollapsiblePane;
 import com.jidesoft.pane.CollapsiblePanes;
 import com.jidesoft.pane.event.CollapsiblePaneAdapter;
 import com.jidesoft.pane.event.CollapsiblePaneEvent;
 import com.jidesoft.popup.JidePopup;
 import com.jidesoft.swing.DefaultOverlayable;
 import com.jidesoft.swing.JideBoxLayout;
 import com.jidesoft.swing.JideButton;
 import com.jidesoft.wizard.WizardDialog;
 import entity.files.SYSFilesTools;
 import entity.info.*;
 import entity.process.*;
 import entity.system.SYSPropsTools;
 import op.OPDE;
 import op.care.PnlCare;
 import op.care.sysfiles.DlgFiles;
 import op.process.DlgProcessAssign;
 import op.residents.DlgEditResidentBaseData;
 import op.residents.bwassistant.AddBWWizard;
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
 import javax.persistence.RollbackException;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyVetoException;
 import java.text.DateFormat;
 import java.util.*;
 import java.util.List;
 
 /**
  * @author Torsten Löhr
  */
 public class PnlInformation extends NursingRecordsPanel {
 
 
     public static final String internalClassID = "nursingrecords.info";
 
     private Resident resident;
     private JScrollPane jspSearch;
     private final PnlCare pnlCare;
     private CollapsiblePanes searchPanes;
     private CollapsiblePanes cpsAll;
     private JideButton btnResDied, btnResMovedOut, btnResIsAway, btnResIsBack;
     private JXSearchField txtSearch;
     private JList listSearchResults;
     private List<ResInfoCategory> listCategories = new ArrayList<ResInfoCategory>();
     private List<ResInfo> listAllInfos;
     private List<ResInfoType> listAllTypes;
     private Map<ResInfoCategory, ArrayList<ResInfoType>> mapCat2Type;
     private Map<ResInfoType, ArrayList<ResInfo>> mapType2ResInfos;
     private Map<ResInfo, PnlEditResInfo> mapInfo2Editor;
     private Map<String, CollapsiblePane> mapKey2CP;
     private Map<Integer, ResInfoType> mapEquiv2Type;
 
 
     public PnlInformation(Resident resident, JScrollPane jspSearch, PnlCare pnlCare) {
         this.resident = resident;
         this.jspSearch = jspSearch;
         this.pnlCare = pnlCare;
         initComponents();
         initPanel();
     }
 
     public void initPanel() {
 //        typeAbsence = ResInfoTypeTools.getByType(ResInfoTypeTools.TYPE_ABSENCE);
 //        typeStartOfStay = ResInfoTypeTools.getByType(ResInfoTypeTools.TYPE_STAY);
         cpsAll = new CollapsiblePanes();
         jspMain.setViewportView(cpsAll);
         mapCat2Type = Collections.synchronizedMap(new HashMap<ResInfoCategory, ArrayList<ResInfoType>>());
         mapType2ResInfos = Collections.synchronizedMap(new HashMap<ResInfoType, ArrayList<ResInfo>>());
         listAllInfos = Collections.synchronizedList(new ArrayList<ResInfo>());
         listAllTypes = Collections.synchronizedList(new ArrayList<ResInfoType>());
         mapInfo2Editor = Collections.synchronizedMap(new HashMap<ResInfo, PnlEditResInfo>());
         mapKey2CP = Collections.synchronizedMap(new HashMap<String, CollapsiblePane>());
         mapEquiv2Type = Collections.synchronizedMap(new HashMap<Integer, ResInfoType>());
         prepareSearchArea();
         reload();
     }
 
     public void sortData() {
         synchronized (mapType2ResInfos) {
             mapType2ResInfos.clear();
         }
         synchronized (mapCat2Type) {
             mapCat2Type.clear();
         }
 
         synchronized (listCategories) {
             Collections.sort(listCategories);
         }
 
         synchronized (mapCat2Type) {
             for (ResInfoCategory cat : listCategories) {
                 mapCat2Type.put(cat, new ArrayList<ResInfoType>());
             }
         }
         for (ResInfoType type : listAllTypes) {
             synchronized (mapType2ResInfos) {
                 mapType2ResInfos.put(type, new ArrayList<ResInfo>());
             }
             synchronized (mapCat2Type) {
                 mapCat2Type.get(type.getResInfoCat()).add(type);
             }
             if (type.getEquiv() > 0) {
                 synchronized (mapEquiv2Type) {
                     mapEquiv2Type.put(type.getEquiv(), type);
                 }
             }
         }
 
         synchronized (mapType2ResInfos) {
             for (ResInfo info : listAllInfos) {
                 if (mapType2ResInfos.containsKey(info.getResInfoType())) {
 
                     mapType2ResInfos.get(info.getResInfoType()).add(info);
 
                 } else if (mapEquiv2Type.containsKey(info.getResInfoType().getEquiv())) { // this maybe an obsolete infotype. we will file it under its replacement
                     mapType2ResInfos.get(mapEquiv2Type.get(info.getResInfoType().getEquiv())).add(info);
                 }
             }
 
             for (ResInfoType type : listAllTypes) {
                 if (mapType2ResInfos.containsKey(type)) {
                     Collections.sort(mapType2ResInfos.get(type));
                 }
             }
         }
     }
 
     private void refreshData() {
         cleanup();
         synchronized (listCategories) {
             listCategories.addAll(ResInfoCategoryTools.getAll());
         }
         synchronized (listAllTypes) {
             listAllTypes.addAll(ResInfoTypeTools.getAllActive());
         }
         synchronized (listAllInfos) {
             listAllInfos.addAll(ResInfoTools.getAll(resident));
         }
         sortData();
     }
 
     private void reloadDisplay() {
 
         OPDE.getMainframe().setBlocked(true);
         OPDE.getDisplayManager().setProgressBarMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.wait"), -1, 100));
 
 
         cpsAll.removeAll();
         cpsAll.setLayout(new JideBoxLayout(cpsAll, JideBoxLayout.Y_AXIS));
 
 
         SwingWorker worker = new SwingWorker() {
 
             @Override
             protected Object doInBackground() throws Exception {
                 int progress = 0;
 
                 try {
                     for (final ResInfoCategory cat : listCategories) {
                         progress++;
                         OPDE.getDisplayManager().setProgressBarMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.wait"), progress, listCategories.size()));
 
                         OPDE.debug(cat.getText());
                         OPDE.debug(mapCat2Type.get(cat).isEmpty());
 
                         if (!mapCat2Type.get(cat).isEmpty()) {
 
                             final String keyResInfoCat = cat.getID() + ".resinfocat";
                             if (!mapKey2CP.containsKey(keyResInfoCat)) {
 
                                 synchronized (mapKey2CP) {
                                     mapKey2CP.put(keyResInfoCat, new CollapsiblePane());
                                 }
                                 mapKey2CP.get(keyResInfoCat).setStyle(CollapsiblePane.TREE_STYLE);
                                 mapKey2CP.get(keyResInfoCat).addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
                                     @Override
                                     public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                                         SYSPropsTools.storeProp(keyResInfoCat + ".expanded", "true", OPDE.getLogin().getUser());
                                     }
 
                                     @Override
                                     public void paneCollapsed(CollapsiblePaneEvent collapsiblePaneEvent) {
                                         SYSPropsTools.storeProp(keyResInfoCat + ".expanded", "false", OPDE.getLogin().getUser());
                                     }
                                 });
                                 try {
                                     mapKey2CP.get(keyResInfoCat).setCollapsed(!SYSPropsTools.isBooleanTrue(keyResInfoCat + ".expanded"));
                                 } catch (PropertyVetoException e) {
                                     // Bah!
                                 }
 
                             }
 
                             final CollapsiblePane cpCat = mapKey2CP.get(keyResInfoCat);
                             final DefaultCPTitle cpTitleCat = new DefaultCPTitle(cat.getText(), new ActionListener() {
                                 @Override
                                 public void actionPerformed(ActionEvent e) {
                                     try {
                                         cpCat.setCollapsed(!cpCat.isCollapsed());
                                     } catch (PropertyVetoException pve) {
                                         // BAH!
                                     }
                                 }
                             });
 
 
                             GUITools.addExpandCollapseButtons(cpCat, cpTitleCat.getRight());
 
                             /***
                              *                _
                              *       ___ ___ | | ___  _ __
                              *      / __/ _ \| |/ _ \| '__|
                              *     | (_| (_) | | (_) | |
                              *      \___\___/|_|\___/|_|
                              *
                              */
                             if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.MANAGER, internalClassID)) {
                                 final JButton btnColor = new JButton(SYSConst.icon22colorset);
                                 btnColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                 btnColor.setContentAreaFilled(false);
                                 btnColor.setBorder(null);
                                 btnColor.setToolTipText(OPDE.lang.getString("misc.msg.colorset"));
                                 btnColor.setSelectedIcon(SYSConst.icon22colorsetPressed);
                                 btnColor.addActionListener(new ActionListener() {
                                     @Override
                                     public void actionPerformed(ActionEvent e) {
                                         final JColorChooser clr = new JColorChooser(cat.getColor());
                                         final JidePopup popup = new JidePopup();
                                         clr.getSelectionModel().addChangeListener(new ChangeListener() {
                                             @Override
                                             public void stateChanged(ChangeEvent e) {
                                                 popup.hidePopup();
                                                 EntityManager em = OPDE.createEM();
                                                 try {
                                                     em.getTransaction().begin();
                                                     ResInfoCategory myCat = em.merge(cat);
                                                     em.lock(myCat, LockModeType.OPTIMISTIC);
                                                     myCat.setColor(clr.getColor());
                                                     em.getTransaction().commit();
                                                     synchronized (listCategories) {
                                                         listCategories.remove(cat);
                                                         listCategories.add(myCat);
                                                     }
                                                     sortData();
                                                     reload();
                                                 } catch (OptimisticLockException ole) {
                                                     if (em.getTransaction().isActive()) {
                                                         em.getTransaction().rollback();
                                                     }
                                                     OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                                                 } catch (RollbackException ole) {
                                                     if (em.getTransaction().isActive()) {
                                                         em.getTransaction().rollback();
                                                     }
                                                     if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                                         OPDE.getMainframe().emptyFrame();
                                                         OPDE.getMainframe().afterLogin();
                                                     }
                                                     OPDE.getDisplayManager().addSubMessage(new DisplayMessage(ole.getMessage(), DisplayMessage.IMMEDIATELY));
                                                 } catch (Exception ex) {
                                                     if (em.getTransaction().isActive()) {
                                                         em.getTransaction().rollback();
                                                     }
                                                     OPDE.fatal(ex);
                                                 } finally {
                                                     em.close();
                                                 }
                                             }
                                         });
 
                                         popup.setMovable(false);
                                         popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
                                         popup.setPopupType(JidePopup.HEAVY_WEIGHT_POPUP);
 
                                         popup.setOwner(btnColor);
                                         popup.removeExcludedComponent(btnColor);
                                         popup.setTransient(false);
                                         popup.getContentPane().add(clr);
                                         popup.setDefaultFocusComponent(clr);
                                         GUITools.showPopup(popup, SwingConstants.SOUTH_WEST);
                                     }
                                 });
                                 cpTitleCat.getRight().add(btnColor);
                             }
 
                             cpTitleCat.getButton().setFont(SYSConst.ARIAL24);
                             cpTitleCat.getButton().setForeground(GUITools.getForeground(cat.getColor()));
 
                             cpTitleCat.getButton().setForeground(cat.getColor());
                             cpCat.setBackground(Color.white);
                             cpCat.setTitleLabelComponent(cpTitleCat.getMain());
 
                             //                cpCat.setBackground(cat.getColor());
 
 
                             JPanel pnlTypes = new JPanel();
                             pnlTypes.setLayout(new BoxLayout(pnlTypes, BoxLayout.Y_AXIS));
 
 
                             /***
                              *                          _ _     _                        _             _
                              *      _ __   ___  ___ ___(_) |__ | | ___    ___ ___  _ __ | |_ ___ _ __ | |_
                              *     | '_ \ / _ \/ __/ __| | '_ \| |/ _ \  / __/ _ \| '_ \| __/ _ \ '_ \| __|
                              *     | |_) | (_) \__ \__ \ | |_) | |  __/ | (_| (_) | | | | ||  __/ | | | |_
                              *     | .__/ \___/|___/___/_|_.__/|_|\___|  \___\___/|_| |_|\__\___|_| |_|\__|
                              *     |_|
                              */
                             for (final ResInfoType resInfoType : mapCat2Type.get(cat)) {
                                 final String keyResInfoType = resInfoType.getID() + ".resinfotype";
                                 if (!mapKey2CP.containsKey(keyResInfoType)) {
                                     synchronized (mapKey2CP) {
                                         mapKey2CP.put(keyResInfoType, new CollapsiblePane());
                                     }
                                     mapKey2CP.get(keyResInfoType).setStyle(CollapsiblePane.TREE_STYLE);
                                     mapKey2CP.get(keyResInfoType).addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
                                         @Override
                                         public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                                             SYSPropsTools.storeProp(keyResInfoType + ".expanded", "true", OPDE.getLogin().getUser());
                                         }
 
                                         @Override
                                         public void paneCollapsed(CollapsiblePaneEvent collapsiblePaneEvent) {
                                             SYSPropsTools.storeProp(keyResInfoType + ".expanded", "false", OPDE.getLogin().getUser());
                                         }
                                     });
                                     try {
                                         mapKey2CP.get(keyResInfoType).setCollapsed(!SYSPropsTools.isBooleanTrue(keyResInfoType + ".expanded"));
                                     } catch (PropertyVetoException e) {
                                         // Bah!
                                     }
 
                                 }
 
                                 CollapsiblePane cpResInfoType = mapKey2CP.get(keyResInfoType);
                                 cpResInfoType.setTitle(resInfoType.getShortDescription());
 
                                 cpResInfoType.setFont(SYSConst.ARIAL18);
 
 
                                 //                    cpResInfoType.setBackground((GUITools.blend(cat.getColor(), Color.WHITE, 0.7f)));
                                 cpResInfoType.setForeground((GUITools.blend(cat.getColor(), Color.BLACK, 0.7f)));
                                 cpResInfoType.setBackground(Color.WHITE);
 
                                 JPanel pnlInfos = new JPanel();
                                 pnlInfos.setLayout(new BoxLayout(pnlInfos, BoxLayout.Y_AXIS));
 
                                 CollapsiblePanes cpsType = new CollapsiblePanes();
                                 cpsType.setLayout(new JideBoxLayout(cpsType, JideBoxLayout.Y_AXIS));
 
                                 pnlInfos.add(cpsType);
 
                                 cpResInfoType.setContentPane(pnlInfos);
 
                                 /***
                                  *                _     _
                                  *       __ _  __| | __| |
                                  *      / _` |/ _` |/ _` |
                                  *     | (_| | (_| | (_| |
                                  *      \__,_|\__,_|\__,_|
                                  *
                                  */
                                 if (resInfoType.getIntervalMode() == ResInfoTypeTools.MODE_INTERVAL_SINGLE_INCIDENTS ||
                                         resInfoType.getIntervalMode() == ResInfoTypeTools.MODE_INTERVAL_NOCONSTRAINTS ||
                                         mapType2ResInfos.get(resInfoType).isEmpty() ||
                                         ResInfoTypeTools.containsOnlyClosedInfos(mapType2ResInfos.get(resInfoType)) ||
                                         ResInfoTypeTools.containsOneActiveObsoleteInfo(mapType2ResInfos.get(resInfoType))
                                         ) {
                                     if (resInfoType.getType() == ResInfoTypeTools.TYPE_DIAGNOSIS) {
                                         final JideButton btnAdd = GUITools.createHyperlinkButton("nursingrecords.info.noconstraints", SYSConst.icon22add, null);
                                         btnAdd.addActionListener(new ActionListener() {
                                             @Override
                                             public void actionPerformed(ActionEvent e) {
                                                 new DlgDiag(new ResInfo(resInfoType, resident), new Closure() {
                                                     @Override
                                                     public void execute(Object o) {
                                                         if (o != null) {
                                                             EntityManager em = OPDE.createEM();
                                                             try {
                                                                 em.getTransaction().begin();
                                                                 em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                                                 // so that no conflicts can occur if another user enters a new info at the same time
                                                                 ResInfoType myType = em.merge(resInfoType);
                                                                 em.lock(myType, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                                                                 final ResInfo newinfo = em.merge((ResInfo) o);
 
                                                                 em.getTransaction().commit();
                                                                 synchronized (mapType2ResInfos) {
                                                                     mapType2ResInfos.get(newinfo.getResInfoType()).add(newinfo);
                                                                     Collections.sort(mapType2ResInfos.get(newinfo.getResInfoType()));
                                                                 }
 
                                                                 synchronized (listAllTypes) {
                                                                     listAllTypes.remove(resInfoType);
                                                                     listAllTypes.add(myType);
                                                                 }
 
                                                                 synchronized (listAllInfos) {
                                                                     listAllInfos.add(newinfo);
                                                                 }
 
 //                                                                if (newinfo.getResInfoType().isAlertType()) {
 //                                                                    GUITools.setResidentDisplay(resident);
 //                                                                }
 
                                                                 reloadDisplay();
 
                                                             } catch (OptimisticLockException ole) {
                                                                 if (em.getTransaction().isActive()) {
                                                                     em.getTransaction().rollback();
                                                                 }
                                                                 if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                                                     OPDE.getMainframe().emptyFrame();
                                                                     OPDE.getMainframe().afterLogin();
                                                                 }
                                                                 OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
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
                                                     }
                                                 });
                                             }
                                         });
                                         cpsType.add(btnAdd);
                                     } else if (resInfoType.getType() == ResInfoTypeTools.TYPE_ABSENCE) {
                                         cpsType.add(new JLabel(OPDE.lang.getString("nursingrecords.info.cant.add.absence.here")));
                                     } else if (resInfoType.getType() == ResInfoTypeTools.TYPE_STAY) {
                                         cpsType.add(new JLabel(OPDE.lang.getString("nursingrecords.info.cant.add.stays.here")));
                                     } else {// if (resInfoType.getType() != ResInfoTypeTools.TYPE_ABSENCE && resInfoType.getType() != ResInfoTypeTools.TYPE_STAY)
                                         String buttonText = mapType2ResInfos.get(resInfoType).isEmpty() ? "nursingrecords.info.no.entry.yet" : "nursingrecords.info.only.closed.entries";
                                         if (resInfoType.getIntervalMode() == ResInfoTypeTools.MODE_INTERVAL_SINGLE_INCIDENTS) {
                                             buttonText = "nursingrecords.info.singleincident";
                                         }
                                         final JideButton btnAdd = GUITools.createHyperlinkButton(buttonText, SYSConst.icon22add, null);
                                         btnAdd.addActionListener(new ActionListener() {
                                             @Override
                                             public void actionPerformed(ActionEvent e) {
                                                 final JidePopup popup = new JidePopup();
                                                 PnlEditResInfo pnlEditResInfo = new PnlEditResInfo(new ResInfo(resInfoType, resident), new Closure() {
                                                     @Override
                                                     public void execute(Object o) {
                                                         popup.hidePopup();
                                                         if (o != null) {
                                                             EntityManager em = OPDE.createEM();
                                                             try {
                                                                 em.getTransaction().begin();
                                                                 em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                                                 // so that no conflicts can occur if another user enters a new info at the same time
                                                                 ResInfoType myType = em.merge(resInfoType);
                                                                 em.lock(myType, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                                                                 final ResInfo newinfo = em.merge((ResInfo) o);
                                                                 newinfo.setHtml(ResInfoTools.getContentAsHTML(newinfo));
 
                                                                 // only for the screen refresh
                                                                 // if there are active resinfos with obsolete forms that should be replaced
                                                                 // by this one, they must all be closed now.
                                                                 if (!newinfo.isSingleIncident()) {
                                                                     for (ResInfo myResInfo : mapType2ResInfos.get(resInfoType)) {
                                                                         if (!myResInfo.isClosed() && myResInfo.getResInfoType().isObsolete()) {
                                                                             ResInfo closedResInfo = em.merge(myResInfo);
                                                                             em.lock(closedResInfo, LockModeType.OPTIMISTIC);
                                                                             closedResInfo.setTo(new DateTime(newinfo.getFrom()).minusSeconds(1).toDate());
                                                                             closedResInfo.setUserOFF(em.merge(OPDE.getLogin().getUser()));
                                                                         }
                                                                     }
                                                                 }
 
                                                                 em.getTransaction().commit();
 
                                                                 reload();
 
                                                                 if (newinfo.getResInfoType().isAlertType()) {
                                                                     GUITools.setResidentDisplay(resident);
                                                                 }
                                                                 OPDE.getMainframe().addSpeciality(newinfo.getResInfoType(), resident);
 
                                                             } catch (OptimisticLockException ole) {
                                                                 if (em.getTransaction().isActive()) {
                                                                     em.getTransaction().rollback();
                                                                 }
                                                                 if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                                                     OPDE.getMainframe().emptyFrame();
                                                                     OPDE.getMainframe().afterLogin();
                                                                 }
                                                                 OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
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
                                                     }
                                                 }, resInfoType.getResInfoCat().getColor());
                                                 pnlEditResInfo.setEnabled(true, PnlEditResInfo.NEW);
 
                                                 popup.setMovable(false);
                                                 popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
                                                 JScrollPane scrl = new JScrollPane(pnlEditResInfo.getPanel());
                                                 //scrl.setPreferredSize(new Dimension(pnlEditResInfo.getPanel().getPreferredSize().width + 100, Math.min(pnlEditResInfo.getPanel().getPreferredSize().height, OPDE.getMainframe().getHeight()) - 100));
                                                 scrl.setPreferredSize(new Dimension(pnlEditResInfo.getPanel().getPreferredSize().width + 100, Math.min(pnlEditResInfo.getPanel().getPreferredSize().height, OPDE.getMainframe().getHeight())));
 
                                                 popup.setOwner(btnAdd);
                                                 popup.removeExcludedComponent(btnAdd);
                                                 popup.getContentPane().add(scrl);
                                                 popup.setDefaultFocusComponent(scrl);
                                                 GUITools.showPopup(popup, SwingConstants.CENTER);
                                             }
                                         });
                                         cpsType.add(btnAdd);
 
                                         try {
                                             mapKey2CP.get(keyResInfoType).setCollapsed(true);
                                         } catch (PropertyVetoException e) {
                                             // bah!
                                         }
                                     }
                                 }
 
                                 for (final ResInfo info : mapType2ResInfos.get(resInfoType)) {
                                     cpsType.add(createCP4(info));
                                 }
 
                                 cpsType.addExpansion();
 
                                 pnlTypes.add(cpResInfoType);
 
                             }
 
                             cpCat.setContentPane(pnlTypes);
 
                             cpsAll.add(cpCat);
                         }
 
                     }
                     cpsAll.addExpansion();
                 } catch (Exception e) {
                     OPDE.fatal(e);
                 }
                 return null;
             }
 
             @Override
             protected void done() {
                 OPDE.getDisplayManager().setProgressBarMessage(null);
                 OPDE.getMainframe().setBlocked(false);
             }
         };
         worker.execute();
 
 
     }
 
     private CollapsiblePane createCP4(final ResInfo resInfo) {
         final String keyResInfo = resInfo.getID() + ".resinfo";
         if (!mapKey2CP.containsKey(keyResInfo)) {
             mapKey2CP.put(keyResInfo, new CollapsiblePane());
             mapKey2CP.get(keyResInfo).setStyle(CollapsiblePane.TREE_STYLE);
         }
 
         final CollapsiblePane cpInfo = mapKey2CP.get(keyResInfo);
 
         final boolean btnMenuEnabled = resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_ABSENCE && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_STAY;
 
         String title = "";
 
         if (resInfo.isSingleIncident()) {
             title += DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT).format(resInfo.getFrom()) + " (" + resInfo.getUserON().getFullname() + ")";
         } else {
            title += DateFormat.getDateInstance().format(resInfo.getFrom()) + " (" + (resInfo.getUserON() != null ? resInfo.getUserON().getFullname() : "--") + ") " + " >> ";
             title += resInfo.isClosed() ? DateFormat.getDateInstance().format(resInfo.getTo()) + " (" + resInfo.getUserOFF().getFullname() + ")" : "";
         }
 
         if (resInfo.getResInfoType().isObsolete() && mapEquiv2Type.containsKey(resInfo.getResInfoType().getEquiv())) {
             title += " -" + resInfo.getResInfoType().getShortDescription() + "-";
         }
 
         DefaultCPTitle cptitle = new DefaultCPTitle(title, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     cpInfo.setCollapsed(!cpInfo.isCollapsed());
                 } catch (PropertyVetoException pve) {
                     // BAH!
                 }
             }
         });
 
         if (resInfo.getResInfoType().isObsolete()) {
             cptitle.getButton().setIcon(SYSConst.icon22infogray);
             cptitle.getButton().setHorizontalTextPosition(SwingConstants.LEADING);
             cptitle.getButton().setToolTipText(SYSTools.toHTMLForScreen(OPDE.lang.getString("nursingrecords.info.outdated.form.explanation")));
         }
 
         if (!resInfo.isClosed() || !resInfo.isSingleIncident()) {
             cptitle.getButton().setForeground(GUITools.blend(resInfo.getResInfoType().getResInfoCat().getColor(), Color.BLACK, 0.25f));
             cpInfo.setBackground(Color.WHITE);
         }
 
 
         if (!resInfo.getAttachedFilesConnections().isEmpty()) {
             /***
              *      _     _         _____ _ _
              *     | |__ | |_ _ __ |  ___(_) | ___  ___
              *     | '_ \| __| '_ \| |_  | | |/ _ \/ __|
              *     | |_) | |_| | | |  _| | | |  __/\__ \
              *     |_.__/ \__|_| |_|_|   |_|_|\___||___/
              *
              */
             final JButton btnFiles = new JButton(Integer.toString(resInfo.getAttachedFilesConnections().size()), SYSConst.icon22greenStar);
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
                     Closure fileHandleClosure = new Closure() {
                         @Override
                         public void execute(Object o) {
                             EntityManager em = OPDE.createEM();
                             final ResInfo editinfo = em.merge(resInfo);
                             em.refresh(editinfo);
                             em.close();
 
                             // refresh data
                             synchronized (mapType2ResInfos) {
                                 int oldIndex = mapType2ResInfos.get(resInfo.getResInfoType()).indexOf(resInfo);
                                 mapType2ResInfos.get(resInfo.getResInfoType()).remove(resInfo);
                                 mapType2ResInfos.get(editinfo.getResInfoType()).add(oldIndex, editinfo);
                             }
 
                             synchronized (listAllInfos) {
                                 listAllInfos.remove(resInfo);
                                 listAllInfos.add(editinfo);
                             }
                             synchronized (mapKey2CP) {
                                 mapKey2CP.remove(keyResInfo);
                             }
                             sortData();
                             reloadDisplay();
                         }
                     };
                     new DlgFiles(resInfo, fileHandleClosure);
                 }
             });
             btnFiles.setEnabled(OPDE.isFTPworking());
             cptitle.getRight().add(btnFiles);
         }
 
         if (!resInfo.getAttachedQProcessConnections().isEmpty()) {
             /***
              *      _     _         ____
              *     | |__ | |_ _ __ |  _ \ _ __ ___   ___ ___  ___ ___
              *     | '_ \| __| '_ \| |_) | '__/ _ \ / __/ _ \/ __/ __|
              *     | |_) | |_| | | |  __/| | | (_) | (_|  __/\__ \__ \
              *     |_.__/ \__|_| |_|_|   |_|  \___/ \___\___||___/___/
              *
              */
             final JButton btnProcess = new JButton(Integer.toString(resInfo.getAttachedQProcessConnections().size()), SYSConst.icon22redStar);
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
                     new DlgProcessAssign(resInfo, new Closure() {
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
                                 ResInfo myInfo = em.merge(resInfo);
                                 em.lock(myInfo, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
 
                                 ArrayList<SYSINF2PROCESS> attached = new ArrayList<SYSINF2PROCESS>(myInfo.getAttachedQProcessConnections());
                                 for (SYSINF2PROCESS linkObject : attached) {
                                     if (unassigned.contains(linkObject.getQProcess())) {
                                         linkObject.getQProcess().getAttachedNReportConnections().remove(linkObject);
                                         linkObject.getResInfo().getAttachedQProcessConnections().remove(linkObject);
                                         em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_REMOVE_ELEMENT) + ": " + myInfo.getTitle() + " ID: " + myInfo.getID(), PReportTools.PREPORT_TYPE_REMOVE_ELEMENT, linkObject.getQProcess()));
                                         em.remove(linkObject);
                                     }
                                 }
                                 attached.clear();
 
                                 for (QProcess qProcess : assigned) {
                                     java.util.List<QProcessElement> listElements = qProcess.getElements();
                                     if (!listElements.contains(myInfo)) {
                                         QProcess myQProcess = em.merge(qProcess);
                                         SYSINF2PROCESS myLinkObject = em.merge(new SYSINF2PROCESS(myQProcess, myInfo));
                                         em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_ASSIGN_ELEMENT) + ": " + myInfo.getTitle() + " ID: " + myInfo.getID(), PReportTools.PREPORT_TYPE_ASSIGN_ELEMENT, myQProcess));
                                         qProcess.getAttachedResInfoConnections().add(myLinkObject);
                                         myInfo.getAttachedQProcessConnections().add(myLinkObject);
                                     }
                                 }
 
                                 em.getTransaction().commit();
 
                                 synchronized (listAllInfos) {
                                     listAllInfos.remove(resInfo);
                                     listAllInfos.add(myInfo);
                                 }
                                 synchronized (mapKey2CP) {
                                     mapKey2CP.remove(keyResInfo);
                                 }
                                 sortData();
                                 reloadDisplay();
 
                             } catch (OptimisticLockException ole) {
                                 if (em.getTransaction().isActive()) {
                                     em.getTransaction().rollback();
                                 }
                                 if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                     OPDE.getMainframe().emptyFrame();
                                     OPDE.getMainframe().afterLogin();
                                 } else {
                                     reloadDisplay();
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
             cptitle.getRight().add(btnProcess);
         }
 
 
         final JButton btnPrint = new JButton(SYSConst.icon22print2);
         btnPrint.setPressedIcon(SYSConst.icon22print2Pressed);
         btnPrint.setAlignmentX(Component.RIGHT_ALIGNMENT);
         btnPrint.setAlignmentY(Component.TOP_ALIGNMENT);
         btnPrint.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         btnPrint.setContentAreaFilled(false);
         btnPrint.setBorder(null);
         btnPrint.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (!mapInfo2Editor.containsKey(resInfo)) {
                     try {
                         cpInfo.setCollapsed(false);
                     } catch (PropertyVetoException e1) {
                         //bah!!
                     }
                 }
 
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         mapInfo2Editor.get(resInfo).print();
                     }
                 });
             }
         });
         cptitle.getRight().add(btnPrint);
         btnPrint.setEnabled(btnMenuEnabled);
 
         // forward declaration
         final JButton btnEdit = new JButton(SYSConst.icon22edit3);
         final JButton btnMenu = new JButton(SYSConst.icon22menu);
         /***
          *           _
          *       ___| |__   __ _ _ __   __ _  ___
          *      / __| '_ \ / _` | '_ \ / _` |/ _ \
          *     | (__| | | | (_| | | | | (_| |  __/
          *      \___|_| |_|\__,_|_| |_|\__, |\___|
          *                             |___/
          */
         final JButton btnChange = new JButton(SYSConst.icon22playerPlay);
         btnChange.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         btnChange.setContentAreaFilled(false);
         btnChange.setBorder(null);
         btnChange.setAlignmentY(Component.TOP_ALIGNMENT);
         btnChange.setPressedIcon(SYSConst.icon22Pressed);
         cptitle.getRight().add(btnChange);
         btnChange.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (!mapInfo2Editor.containsKey(resInfo) || cpInfo.isCollapsed()) {
                     try {
                         cpInfo.setCollapsed(false);
                     } catch (PropertyVetoException e1) {
                         //bah!!
                     }
                 }
                 btnPrint.setEnabled(false);
                 btnMenu.setEnabled(false);
                 mapInfo2Editor.get(resInfo).setEnabled(resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_DIAGNOSIS && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_STAY && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_OLD && !resInfo.isClosed() && !resInfo.isSingleIncident() && !resInfo.isNoConstraints(), PnlEditResInfo.CHANGE);
                 final boolean wasChangeable = btnChange.isEnabled();
                 btnChange.setEnabled(false);
                 final boolean wasEditable = btnEdit.isEnabled();
                 btnEdit.setEnabled(false);
 
                 mapInfo2Editor.get(resInfo).setClosure(new Closure() {
                     @Override
                     public void execute(final Object o) {
                         btnPrint.setEnabled(true);
                         btnMenu.setEnabled(btnMenuEnabled);
                         mapInfo2Editor.get(resInfo).setEnabled(false, PnlEditResInfo.CHANGE);
                         btnChange.setEnabled(wasChangeable);
                         btnEdit.setEnabled(wasEditable);
 
                         if (o != null) { //  && mapInfo2Editor.get(resInfo).isChanged()
 
                             new DlgYesNo(OPDE.lang.getString("misc.questions.change1") + "<br/>&raquo;" + resInfo.getResInfoType().getShortDescription() + "&laquo;<br/>" + DateFormat.getDateInstance().format(resInfo.getFrom()) + "<br/>" + OPDE.lang.getString("misc.questions.change2"), SYSConst.icon48play, new Closure() {
                                 @Override
                                 public void execute(Object answer) {
                                     if (!answer.equals(JOptionPane.YES_OPTION)) {
                                         return;
                                     }
                                     EntityManager em = OPDE.createEM();
                                     try {
                                         em.getTransaction().begin();
                                         ResInfo oldinfo = em.merge(resInfo);
 
 
                                         ResInfo newinfo = em.merge((ResInfo) o);
 //                                    mapInfo2Editor.get(resInfo).getResInfo()
                                         em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                         // so that no conflicts can occur if another user enters a new info at the same time
                                         em.lock(em.merge(resInfo.getResInfoType()), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                                         em.lock(oldinfo, LockModeType.OPTIMISTIC);
 
                                         newinfo.setHtml(ResInfoTools.getContentAsHTML(newinfo));
                                         newinfo.setUserON(em.merge(OPDE.getLogin().getUser()));
                                         newinfo.setFrom(new Date());
 
                                         oldinfo.setTo(new DateTime(newinfo.getFrom()).minusSeconds(1).toDate());
                                         oldinfo.setUserOFF(newinfo.getUserON());
 
                                         em.getTransaction().commit();
 
                                         synchronized (mapType2ResInfos) {
                                             mapType2ResInfos.get(oldinfo.getResInfoType()).remove(resInfo);
                                             mapType2ResInfos.get(newinfo.getResInfoType()).add(newinfo);
                                             mapType2ResInfos.get(newinfo.getResInfoType()).add(oldinfo);
                                             Collections.sort(mapType2ResInfos.get(newinfo.getResInfoType()));
                                         }
 
                                         synchronized (listAllInfos) {
                                             listAllInfos.remove(resInfo);
                                             listAllInfos.add(oldinfo);
                                             listAllInfos.add(newinfo);
                                         }
                                     } catch (OptimisticLockException ole) {
                                         if (em.getTransaction().isActive()) {
                                             em.getTransaction().rollback();
                                         }
                                         if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                             OPDE.getMainframe().emptyFrame();
                                             OPDE.getMainframe().afterLogin();
                                         }
                                         OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                                     } catch (Exception ex) {
                                         if (em.getTransaction().isActive()) {
                                             em.getTransaction().rollback();
                                         }
                                         OPDE.fatal(ex);
                                     } finally {
                                         em.close();
                                     }
 
                                     synchronized (mapKey2CP) {
                                         mapKey2CP.remove(keyResInfo);
                                     }
                                 }
                             });
                         }
                         reloadDisplay();
                     }
                 });
             }
         });
 
 
         btnChange.setEnabled(
                 resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_DIAGNOSIS
                         && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_STAY
                         && !resInfo.getResInfoType().isObsolete()
                         && !resInfo.isClosed()
                         && !resInfo.isSingleIncident()
                         && !resInfo.isNoConstraints());
 
         /***
          *               _ _ _
          *       ___  __| (_) |_
          *      / _ \/ _` | | __|
          *     |  __/ (_| | | |_
          *      \___|\__,_|_|\__|
          *
          */
         btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         btnEdit.setPressedIcon(SYSConst.icon22Pressed);
         btnEdit.setContentAreaFilled(false);
         btnEdit.setBorder(null);
         btnEdit.setSelectedIcon(SYSConst.icon22edit3Pressed);
         btnEdit.setAlignmentY(Component.TOP_ALIGNMENT);
         cptitle.getRight().add(btnEdit);
         btnEdit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 //                if (!mapInfo2Editor.containsKey(resInfo)) {
 //
 //                }
                 if (!mapInfo2Editor.containsKey(resInfo) || cpInfo.isCollapsed()) {
                     try {
                         cpInfo.setCollapsed(false);
 
                     } catch (PropertyVetoException e1) {
                         //bah!!
                     }
                 }
                 btnPrint.setEnabled(false);
                 btnMenu.setEnabled(false);
                 mapInfo2Editor.get(resInfo).setEnabled(true, PnlEditResInfo.EDIT);//resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_DIAGNOSIS && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_STAY && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_OLD && !resInfo.isClosed() && !resInfo.isSingleIncident() && !resInfo.isNoConstraints());
                 final boolean wasChangeable = btnChange.isEnabled();
                 btnChange.setEnabled(false);
                 final boolean wasEditable = btnEdit.isEnabled();
                 btnEdit.setEnabled(false);
 
                 mapInfo2Editor.get(resInfo).setClosure(new Closure() {
                     @Override
                     public void execute(final Object o) {
                         btnPrint.setEnabled(true);
                         btnMenu.setEnabled(btnMenuEnabled);
                         mapInfo2Editor.get(resInfo).setEnabled(false, PnlEditResInfo.EDIT);
                         btnChange.setEnabled(wasChangeable);
                         btnEdit.setEnabled(wasEditable);
 
 //                        btnEdit.setEnabled(ResInfoTools.isEditable(resInfo) && (OPDE.isAdmin() ||
 //                                (resInfo.getUserON().equals(OPDE.getLogin().getUser()) && new DateMidnight(resInfo.getFrom()).equals(new DateMidnight()))  // The same user only on the same day.
 //                        ));
 
                         if (o == null || !mapInfo2Editor.get(resInfo).isChanged()) {
                             return;
                         }
 
                         new DlgYesNo(OPDE.lang.getString("misc.questions.edit1") + "<br/>&raquo;" + resInfo.getResInfoType().getShortDescription() + "&laquo;<br/>" + DateFormat.getDateInstance().format(resInfo.getFrom()) + "<br/>" + OPDE.lang.getString("misc.questions.edit2"), SYSConst.icon48play, new Closure() {
                             @Override
                             public void execute(Object answer) {
 
                                 if (!answer.equals(JOptionPane.YES_OPTION)) {
                                     return;
                                 }
 
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     ResInfo editinfo = em.merge(resInfo);
 
                                     ResInfo tmpInfo = (ResInfo) o; //mapInfo2Editor.get(resInfo).getResInfo();
                                     editinfo.setHtml(ResInfoTools.getContentAsHTML(tmpInfo));
                                     editinfo.setProperties(tmpInfo.getProperties());
                                     editinfo.setText(tmpInfo.getText());
                                     editinfo.setUserON(em.merge(OPDE.getLogin().getUser()));
 
                                     em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                     // so that no conflicts can occur if another user enters a new info at the same time
                                     em.lock(em.merge(editinfo.getResInfoType()), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                                     em.lock(editinfo, LockModeType.OPTIMISTIC);
 
                                     em.getTransaction().commit();
 
 
                                     synchronized (mapType2ResInfos) {
                                         int oldIndex = mapType2ResInfos.get(resInfo.getResInfoType()).indexOf(resInfo);
                                         mapType2ResInfos.get(resInfo.getResInfoType()).remove(resInfo);
                                         mapType2ResInfos.get(editinfo.getResInfoType()).add(oldIndex, editinfo);
                                     }
                                     synchronized (listAllInfos) {
                                         listAllInfos.remove(resInfo);
                                         listAllInfos.add(editinfo);
                                     }
                                 } catch (OptimisticLockException ole) {
                                     if (em.getTransaction().isActive()) {
                                         em.getTransaction().rollback();
                                     }
                                     if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                         OPDE.getMainframe().emptyFrame();
                                         OPDE.getMainframe().afterLogin();
                                     }
                                     OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                                 } catch (Exception ex) {
                                     if (em.getTransaction().isActive()) {
                                         em.getTransaction().rollback();
                                     }
                                     OPDE.fatal(ex);
                                 } finally {
                                     em.close();
                                 }
                                 synchronized (mapKey2CP) {
                                     mapKey2CP.remove(keyResInfo);
                                 }
                                 reloadDisplay();
                             }
                         });
 
 
                     }
                 });
             }
         });
 
         // Only active ones can be edited, and only by the same user that started it or the admin.
         btnEdit.setEnabled(ResInfoTools.isEditable(resInfo) && (OPDE.isAdmin() ||
                 (resInfo.getUserON().equals(OPDE.getLogin().getUser()) && new DateMidnight(resInfo.getFrom()).equals(new DateMidnight()))  // The same user only on the same day.
         ));
 
 
         /***
          *      __  __
          *     |  \/  | ___ _ __  _   _
          *     | |\/| |/ _ \ '_ \| | | |
          *     | |  | |  __/ | | | |_| |
          *     |_|  |_|\___|_| |_|\__,_|
          *
          */
 
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
                 JPanel pnl = getMenu(resInfo);
                 popup.getContentPane().add(pnl);
                 popup.setDefaultFocusComponent(pnl);
 
                 GUITools.showPopup(popup, SwingConstants.WEST);
             }
         });
         cptitle.getRight().add(btnMenu);
         btnMenu.setEnabled(btnMenuEnabled);
 
 
         cpInfo.addCollapsiblePaneListener(new CollapsiblePaneAdapter() {
             @Override
             public void paneExpanded(CollapsiblePaneEvent collapsiblePaneEvent) {
                 if (resInfo.getResInfoType().getType() == ResInfoTypeTools.TYPE_ABSENCE || resInfo.getResInfoType().getType() == ResInfoTypeTools.TYPE_STAY || resInfo.getResInfoType().getType() == ResInfoTypeTools.TYPE_DIAGNOSIS) {
                     JTextPane txt = new JTextPane();
                     txt.setContentType("text/html");
                     txt.setEditable(false);
                     txt.setText(SYSTools.toHTMLForScreen(SYSConst.html_fontface + resInfo.getContentAsHTML() + "</font>"));//   SYSConst.html_div(resInfo.getContentAsHTML()));
                     cpInfo.setContentPane(new JScrollPane(txt));
                 } else {
                     if (!mapInfo2Editor.containsKey(resInfo)) {
                         mapInfo2Editor.put(resInfo, new PnlEditResInfo(resInfo.clone(), resInfo.getResInfoType().getResInfoCat().getColor()));
                     }
                     cpInfo.setContentPane(new JScrollPane(mapInfo2Editor.get(resInfo).getPanel()));
                 }
 
             }
         });
 
         cpInfo.setTitleLabelComponent(cptitle.getMain());
         try {
             cpInfo.setCollapsed(resInfo.isClosed() || resInfo.isSingleIncident());
         } catch (PropertyVetoException e) {
             // bah!
         }
         return cpInfo;
     }
 
 
     @Override
     public void switchResident(Resident resident) {
         this.resident = resident;
         GUITools.setResidentDisplay(resident);
         reload();
     }
 
     @Override
     public void cleanup() {
         cpsAll.removeAll();
         synchronized (mapType2ResInfos) {
             mapType2ResInfos.clear();
         }
         synchronized (mapCat2Type) {
             mapCat2Type.clear();
         }
         synchronized (listCategories) {
             listCategories.clear();
         }
         synchronized (listAllTypes) {
             listAllTypes.clear();
         }
         synchronized (listAllInfos) {
             listAllInfos.clear();
         }
         synchronized (mapInfo2Editor) {
             mapInfo2Editor.clear();
         }
         synchronized (mapKey2CP) {
             mapKey2CP.clear();
         }
         synchronized (mapEquiv2Type) {
             mapEquiv2Type.clear();
         }
     }
 
     @Override
     public void reload() {
         refreshData();
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.MANAGER, internalClassID)) {
             btnResDied.setEnabled(resident.isActive());
             btnResMovedOut.setEnabled(resident.isActive());
         }
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
             btnResIsAway.setEnabled(resident.isActive() && !ResInfoTools.isAway(resident));
             btnResIsBack.setEnabled(resident.isActive() && ResInfoTools.isAway(resident));
         }
         reloadDisplay();
     }
 
     private void prepareSearchArea() {
         searchPanes = new CollapsiblePanes();
         searchPanes.setLayout(new JideBoxLayout(searchPanes, JideBoxLayout.Y_AXIS));
         jspSearch.setViewportView(searchPanes);
 
         JPanel mypanel = new JPanel();
         mypanel.setLayout(new VerticalLayout());
         mypanel.setBackground(Color.WHITE);
 
         CollapsiblePane searchPane = new CollapsiblePane(OPDE.lang.getString(internalClassID));
         searchPane.setStyle(CollapsiblePane.PLAIN_STYLE);
         searchPane.setCollapsible(false);
 
 
         try {
             searchPane.setCollapsed(false);
         } catch (PropertyVetoException e) {
             OPDE.error(e);
         }
 
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.MANAGER, internalClassID) || OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
             JPanel cmdPanel = new JPanel();
             CollapsiblePane commandPane = new CollapsiblePane(OPDE.lang.getString("nursingrecords.info.functions"));
             cmdPanel.setLayout(new VerticalLayout());
             GUITools.addAllComponents(cmdPanel, addCommands());
             commandPane.setContentPane(cmdPanel);
             searchPanes.add(commandPane);
             try {
                 commandPane.setCollapsed(true);
             } catch (PropertyVetoException e) {
                 //BAH!
             }
         }
 
         /***
          *      ____       _       _
          *     |  _ \ _ __(_)_ __ | |_
          *     | |_) | '__| | '_ \| __|
          *     |  __/| |  | | | | | |_
          *     |_|   |_|  |_|_| |_|\__|
          *
          */
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.PRINT, internalClassID)) {
             final JButton btnPrint = GUITools.createHyperlinkButton(OPDE.lang.getString("misc.commands.print"), SYSConst.icon22print2, new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
 
                     String html = "";
                     html += "<h1 id=\"fonth1\" >" + OPDE.lang.getString("nursingrecords.info");
                     html += " " + OPDE.lang.getString("misc.msg.for") + " " + ResidentTools.getLabelText(resident) + "</h1>\n";
 
                     for (ResInfoCategory cat : listCategories) {
                         html += "<h2 id=\"fonth2\" >" + cat.getText() + "</h2>\n";
                         for (ResInfoType type : ResInfoTypeTools.getByCat(cat)) {
                             if (mapType2ResInfos.containsKey(type) && !mapType2ResInfos.get(type).isEmpty()) {
                                 html += "<h3 id=\"fonth3\" >" + type.getShortDescription() + "</h3>\n";
 
                                 html += type.getType() == ResInfoTypeTools.TYPE_INFECTION ? SYSConst.html_48x48_biohazard : "";
                                 html += type.getType() == ResInfoTypeTools.TYPE_DIABETES ? SYSConst.html_48x48_diabetes : "";
                                 html += type.getType() == ResInfoTypeTools.TYPE_ALLERGY ? SYSConst.html_48x48_allergy : "";
                                 html += type.getType() == ResInfoTypeTools.TYPE_WARNING ? SYSConst.html_48x48_warning : "";
 
                                 html += ResInfoTools.getResInfosAsHTML(mapType2ResInfos.get(type), true, null);
                             }
                         }
                     }
                     SYSFilesTools.print(html, true);
                 }
             });
             btnPrint.setAlignmentX(Component.RIGHT_ALIGNMENT);
 
             mypanel.add(btnPrint);
         }
 
 
         try {
             searchPane.setCollapsed(false);
         } catch (PropertyVetoException e) {
             OPDE.error(e);
         }
 
 
         GUITools.addAllComponents(mypanel, addDisplayCommands());
         GUITools.addAllComponents(mypanel, addFilters());
 
 
         searchPane.setContentPane(mypanel);
 
         searchPanes.add(searchPane);
         searchPanes.addExpansion();
     }
 
     @Override
     public String getInternalClassID() {
         return internalClassID;
     }
 
     private void initComponents() {
         // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
         jspMain = new JScrollPane();
 
         //======== this ========
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         add(jspMain);
         // JFormDesigner - End of component initialization  //GEN-END:initComponents
     }
 
     // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
     private JScrollPane jspMain;
     // JFormDesigner - End of variables declaration  //GEN-END:variables
 
 
     private JPanel getMenu(final ResInfo resInfo) {
 
         final JPanel pnlMenu = new JPanel(new VerticalLayout());
         final String keyResInfo = resInfo.getID() + ".resinfo";
 
         if (resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_ABSENCE && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_STAY) {
 
             if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
 
 
                 /***
                  *      ____  _
                  *     / ___|| |_ ___  _ __
                  *     \___ \| __/ _ \| '_ \
                  *      ___) | || (_) | |_) |
                  *     |____/ \__\___/| .__/
                  *                    |_|
                  */
                 final JButton btnStop = GUITools.createHyperlinkButton("nursingrecords.info.btnStop.tooltip", SYSConst.icon22playerStop, null);
                 btnStop.setAlignmentX(Component.RIGHT_ALIGNMENT);
                 btnStop.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent actionEvent) {
                         new DlgYesNo(OPDE.lang.getString("misc.questions.cancel") + "<br/>" + resInfo.getResInfoType().getShortDescription() + "<br/>" + resInfo.getPITAsHTML(), SYSConst.icon48playerStop, new Closure() {
                             @Override
                             public void execute(Object answer) {
                                 if (answer.equals(JOptionPane.YES_OPTION)) {
                                     EntityManager em = OPDE.createEM();
                                     try {
                                         em.getTransaction().begin();
                                         ResInfo editinfo = em.merge(resInfo);
                                         em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                         em.lock(editinfo, LockModeType.OPTIMISTIC);
                                         editinfo.setTo(new Date());
                                         editinfo.setUserOFF(em.merge(OPDE.getLogin().getUser()));
                                         em.getTransaction().commit();
 
                                         synchronized (mapType2ResInfos) {
                                             ResInfoType referenceType = mapType2ResInfos.containsKey(resInfo.getResInfoType()) ? resInfo.getResInfoType() : mapEquiv2Type.get(resInfo.getResInfoType().getEquiv());
                                             // refresh data
                                             int oldIndex = mapType2ResInfos.get(referenceType).indexOf(resInfo);
                                             mapType2ResInfos.get(referenceType).remove(resInfo);
                                             mapType2ResInfos.get(referenceType).add(oldIndex, editinfo);
                                         }
                                         synchronized (listAllInfos) {
                                             listAllInfos.remove(resInfo);
                                             listAllInfos.add(editinfo);
                                         }
                                         synchronized (mapKey2CP) {
                                             mapKey2CP.remove(keyResInfo);
                                         }
 
                                         sortData();
                                         reloadDisplay();
                                         if (editinfo.getResInfoType().isAlertType()) {
                                             GUITools.setResidentDisplay(resident);
                                         }
                                         OPDE.getMainframe().removeSpeciality(editinfo.getResInfoType(), resident);
 
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
                 btnStop.setEnabled(!resInfo.isClosed() && !resInfo.isSingleIncident());
                 pnlMenu.add(btnStop);
 
 
                 if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.DELETE, internalClassID)) {
                     /***
                      *      ____       _      _
                      *     |  _ \  ___| | ___| |_ ___
                      *     | | | |/ _ \ |/ _ \ __/ _ \
                      *     | |_| |  __/ |  __/ ||  __/
                      *     |____/ \___|_|\___|\__\___|
                      *
                      */
                     final JButton btnDelete = GUITools.createHyperlinkButton("nursingrecords.info.btnDelete.tooltip", SYSConst.icon22delete, null);
                     btnDelete.setAlignmentX(Component.RIGHT_ALIGNMENT);
                     btnDelete.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent actionEvent) {
                             new DlgYesNo(OPDE.lang.getString("misc.questions.delete1") + "<br/><i>" + resInfo.getPITAsHTML() + "</i><br/>" + OPDE.lang.getString("misc.questions.delete2"), SYSConst.icon48delete, new Closure() {
                                 @Override
                                 public void execute(Object answer) {
                                     if (answer.equals(JOptionPane.YES_OPTION)) {
                                         EntityManager em = OPDE.createEM();
                                         try {
                                             em.getTransaction().begin();
                                             ResInfo editinfo = em.merge(resInfo);
                                             em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                             em.lock(editinfo, LockModeType.OPTIMISTIC);
                                             em.remove(editinfo);
                                             em.getTransaction().commit();
 
 
                                             synchronized (mapType2ResInfos) {
                                                 // refresh data
                                                 ResInfoType referenceType = mapType2ResInfos.containsKey(resInfo.getResInfoType()) ? resInfo.getResInfoType() : mapEquiv2Type.get(resInfo.getResInfoType().getEquiv());
                                                 mapType2ResInfos.get(referenceType).remove(resInfo);
 
                                             }
                                             synchronized (listAllInfos) {
                                                 listAllInfos.remove(resInfo);
                                             }
                                             sortData();
                                             synchronized (mapKey2CP) {
                                                 mapKey2CP.remove(keyResInfo);
                                             }
 
                                             reloadDisplay();
                                             if (editinfo.getResInfoType().isAlertType()) {
                                                 GUITools.setResidentDisplay(resident);
                                             }
                                             OPDE.getMainframe().removeSpeciality(editinfo.getResInfoType(), resident);
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
                     pnlMenu.add(btnDelete);
                 }
                 if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
                     /***
                      *      ____           _           _
                      *     |  _ \ ___ _ __(_) ___   __| |
                      *     | |_) / _ \ '__| |/ _ \ / _` |
                      *     |  __/  __/ |  | | (_) | (_| |
                      *     |_|   \___|_|  |_|\___/ \__,_|
                      *
                      */
                     final JButton btnChangePeriod = GUITools.createHyperlinkButton("nursingrecords.info.btnChangePeriod.tooltip", SYSConst.icon22changePeriod, null);
                     btnChangePeriod.setAlignmentX(Component.RIGHT_ALIGNMENT);
                     btnChangePeriod.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent actionEvent) {
                             if (resInfo.isSingleIncident()) {
                                 final JidePopup popup = new JidePopup();
                                 PnlPIT pnlPIT = new PnlPIT(resInfo.getFrom(), new Closure() {
                                     @Override
                                     public void execute(Object o) {
                                         popup.hidePopup();
                                         if (o != null) {
                                             EntityManager em = OPDE.createEM();
                                             try {
                                                 em.getTransaction().begin();
                                                 ResInfo editinfo = em.merge(resInfo);
                                                 em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                                 em.lock(editinfo, LockModeType.OPTIMISTIC);
                                                 Date date = (Date) o;
                                                 editinfo.setFrom(date);
                                                 editinfo.setTo(date);
                                                 em.getTransaction().commit();
 
                                                 synchronized (mapType2ResInfos) {
                                                     int oldIndex = mapType2ResInfos.get(resInfo.getResInfoType()).indexOf(resInfo);
                                                     mapType2ResInfos.get(resInfo.getResInfoType()).remove(resInfo);
                                                     mapType2ResInfos.get(editinfo.getResInfoType()).add(oldIndex, editinfo);
                                                 }
                                                 synchronized (listAllInfos) {
                                                     listAllInfos.remove(resInfo);
                                                     listAllInfos.add(editinfo);
                                                 }
                                                 synchronized (mapKey2CP) {
                                                     mapKey2CP.remove(keyResInfo);
                                                 }
                                                 sortData();
                                                 reloadDisplay();
                                                 if (editinfo.getResInfoType().isAlertType() || editinfo.getResInfoType().getType() == ResInfoTypeTools.TYPE_ABSENCE) {
                                                     GUITools.setResidentDisplay(resident);
                                                     if (editinfo.isClosed()) {
                                                         OPDE.getMainframe().removeSpeciality(editinfo.getResInfoType(), resident);
                                                     } else {
                                                         OPDE.getMainframe().addSpeciality(editinfo.getResInfoType(), resident);
                                                     }
                                                 }
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
                                 popup.setMovable(false);
                                 popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
 
                                 popup.setOwner(pnlMenu);
                                 popup.removeExcludedComponent(pnlMenu);
                                 popup.getContentPane().add(pnlPIT);
                                 popup.setDefaultFocusComponent(pnlPIT);
                                 GUITools.showPopup(popup, SwingConstants.WEST);
                             } else {
                                 final JidePopup popup = new JidePopup();
                                 Pair<Date, Date> expansion = ResInfoTools.getMinMaxExpansion(resInfo, mapType2ResInfos.get(resInfo.getResInfoType()));
                                 PnlPeriod pnlPeriod = new PnlPeriod(expansion.getFirst(), expansion.getSecond(), resInfo.getFrom(), resInfo.getTo(), new Closure() {
                                     @Override
                                     public void execute(Object o) {
                                         popup.hidePopup();
                                         if (o != null) {
                                             EntityManager em = OPDE.createEM();
                                             try {
                                                 em.getTransaction().begin();
                                                 ResInfo editinfo = em.merge(resInfo);
                                                 em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                                 em.lock(editinfo, LockModeType.OPTIMISTIC);
                                                 Pair<Date, Date> period = (Pair<Date, Date>) o;
                                                 editinfo.setFrom(period.getFirst());
                                                 editinfo.setTo(period.getSecond());
                                                 editinfo.setUserOFF(editinfo.getTo().equals(SYSConst.DATE_UNTIL_FURTHER_NOTICE) ? null : em.merge(OPDE.getLogin().getUser()));
                                                 em.getTransaction().commit();
 
                                                 synchronized (mapType2ResInfos) {
                                                     int oldIndex = mapType2ResInfos.get(resInfo.getResInfoType()).indexOf(resInfo);
                                                     mapType2ResInfos.get(resInfo.getResInfoType()).remove(resInfo);
                                                     mapType2ResInfos.get(editinfo.getResInfoType()).add(oldIndex, editinfo);
                                                 }
                                                 synchronized (listAllInfos) {
                                                     listAllInfos.remove(resInfo);
                                                     listAllInfos.add(editinfo);
                                                 }
                                                 synchronized (mapKey2CP) {
                                                     mapKey2CP.remove(keyResInfo);
                                                 }
                                                 sortData();
                                                 reloadDisplay();
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
                                 popup.setMovable(false);
                                 popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
 
                                 popup.setOwner(pnlMenu);
                                 popup.removeExcludedComponent(pnlMenu);
                                 popup.getContentPane().add(pnlPeriod);
                                 popup.setDefaultFocusComponent(pnlPeriod);
                                 GUITools.showPopup(popup, SwingConstants.WEST);
 
                             }
 
                         }
                     });
                     btnChangePeriod.setEnabled((ResInfoTools.isEditable(resInfo) || resInfo.getResInfoType().getType() == ResInfoTypeTools.TYPE_STAY) && !resInfo.isSingleIncident()
                             && (OPDE.isAdmin() ||
                             (resInfo.getUserON().equals(OPDE.getLogin().getUser()) && new DateMidnight(resInfo.getFrom()).equals(new DateMidnight()))  // The same user only on the same day.
                     ));
                     //            btnChangePeriod.setEnabled(!resInfo.isClosed() && !resInfo.isSingleIncident());
                     pnlMenu.add(btnChangePeriod);
 
                     pnlMenu.add(new JSeparator());
                 }
             }
             if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
                 /***
                  *      _     _         _____ _ _
                  *     | |__ | |_ _ __ |  ___(_) | ___  ___
                  *     | '_ \| __| '_ \| |_  | | |/ _ \/ __|
                  *     | |_) | |_| | | |  _| | | |  __/\__ \
                  *     |_.__/ \__|_| |_|_|   |_|_|\___||___/
                  *
                  */
                 final JButton btnFiles = GUITools.createHyperlinkButton(resInfo.isClosed() ? "misc.btnfiles.tooltip.closed" : "misc.btnfiles.tooltip", SYSConst.icon22attach, null);
                 btnFiles.setAlignmentX(Component.RIGHT_ALIGNMENT);
                 btnFiles.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent actionEvent) {
                         // If the closure is null, only attached files can be viewed but no new ones can be attached.
                         Closure closure = null;
                         if (!resInfo.isClosed() && resInfo.getResInfoType().getType() != ResInfoTypeTools.TYPE_OLD) {
                             closure = new Closure() {
                                 @Override
                                 public void execute(Object o) {
                                     EntityManager em = OPDE.createEM();
                                     final ResInfo editinfo = em.merge(resInfo);
                                     em.refresh(editinfo);
                                     em.close();
 
                                     synchronized (mapType2ResInfos) {
                                         int oldIndex = mapType2ResInfos.get(resInfo.getResInfoType()).indexOf(resInfo);
                                         mapType2ResInfos.get(resInfo.getResInfoType()).remove(resInfo);
                                         mapType2ResInfos.get(editinfo.getResInfoType()).add(oldIndex, editinfo);
                                     }
                                     synchronized (listAllInfos) {
                                         listAllInfos.remove(resInfo);
                                         listAllInfos.add(editinfo);
                                     }
                                     synchronized (mapKey2CP) {
                                         mapKey2CP.remove(keyResInfo);
                                     }
                                     sortData();
                                     reloadDisplay();
                                 }
                             };
                         }
                         new DlgFiles(resInfo, closure);
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
                         new DlgProcessAssign(resInfo, new Closure() {
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
                                     final ResInfo myInfo = em.merge(resInfo);
                                     em.lock(myInfo, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
 
                                     ArrayList<SYSINF2PROCESS> attached = new ArrayList<SYSINF2PROCESS>(myInfo.getAttachedQProcessConnections());
                                     for (SYSINF2PROCESS linkObject : attached) {
                                         if (unassigned.contains(linkObject.getQProcess())) {
                                             linkObject.getQProcess().getAttachedNReportConnections().remove(linkObject);
                                             linkObject.getResInfo().getAttachedQProcessConnections().remove(linkObject);
                                             em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_REMOVE_ELEMENT) + ": " + myInfo.getTitle() + " ID: " + myInfo.getID(), PReportTools.PREPORT_TYPE_REMOVE_ELEMENT, linkObject.getQProcess()));
                                             em.remove(linkObject);
                                         }
                                     }
                                     attached.clear();
 
                                     for (QProcess qProcess : assigned) {
                                         java.util.List<QProcessElement> listElements = qProcess.getElements();
                                         if (!listElements.contains(myInfo)) {
                                             QProcess myQProcess = em.merge(qProcess);
                                             SYSINF2PROCESS myLinkObject = em.merge(new SYSINF2PROCESS(myQProcess, myInfo));
                                             em.merge(new PReport(OPDE.lang.getString(PReportTools.PREPORT_TEXT_ASSIGN_ELEMENT) + ": " + myInfo.getTitle() + " ID: " + myInfo.getID(), PReportTools.PREPORT_TYPE_ASSIGN_ELEMENT, myQProcess));
                                             qProcess.getAttachedResInfoConnections().add(myLinkObject);
                                             myInfo.getAttachedQProcessConnections().add(myLinkObject);
                                         }
                                     }
 
                                     em.getTransaction().commit();
 
                                     synchronized (listAllInfos) {
                                         listAllInfos.remove(resInfo);
                                         listAllInfos.add(myInfo);
                                     }
                                     synchronized (mapKey2CP) {
                                         mapKey2CP.remove(keyResInfo);
                                     }
                                     sortData();
                                     reloadDisplay();
 
                                 } catch (OptimisticLockException ole) {
                                     if (em.getTransaction().isActive()) {
                                         em.getTransaction().rollback();
                                     }
                                     if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                         OPDE.getMainframe().emptyFrame();
                                         OPDE.getMainframe().afterLogin();
                                     }
                                     OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
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
                 btnProcess.setEnabled(ResInfoTools.isEditable(resInfo));
 
                 if (!resInfo.getAttachedQProcessConnections().isEmpty()) {
                     JLabel lblNum = new JLabel(Integer.toString(resInfo.getAttachedQProcessConnections().size()), SYSConst.icon16redStar, SwingConstants.CENTER);
                     lblNum.setFont(SYSConst.ARIAL14BOLD);
                     lblNum.setForeground(Color.YELLOW);
                     lblNum.setHorizontalTextPosition(SwingConstants.CENTER);
                     DefaultOverlayable overlayableBtn = new DefaultOverlayable(btnProcess, lblNum, DefaultOverlayable.SOUTH_EAST);
                     overlayableBtn.setOpaque(false);
                     pnlMenu.add(overlayableBtn);
                 } else {
                     pnlMenu.add(btnProcess);
                 }
             }
         }
         return pnlMenu;
     }
 
     private java.util.List<Component> addCommands() {
 
         java.util.List<Component> list = new ArrayList<Component>();
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.MANAGER, internalClassID)) {
             /***
              *                          ____           _     _            _
              *      _ __   _____      _|  _ \ ___  ___(_) __| | ___ _ __ | |_
              *     | '_ \ / _ \ \ /\ / / |_) / _ \/ __| |/ _` |/ _ \ '_ \| __|
              *     | | | |  __/\ V  V /|  _ <  __/\__ \ | (_| |  __/ | | | |_
              *     |_| |_|\___| \_/\_/ |_| \_\___||___/_|\__,_|\___|_| |_|\__|
              *
              */
             JideButton addRes = GUITools.createHyperlinkButton(OPDE.lang.getString("nursingrecords.info.addbw"), SYSConst.icon22addbw, null);
             addRes.addMouseListener(GUITools.getHyperlinkStyleMouseAdapter());
             addRes.setAlignmentX(Component.LEFT_ALIGNMENT);
             addRes.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     final MyJDialog dlg = new MyJDialog(false);
                     WizardDialog wizard = new AddBWWizard(new Closure() {
                         @Override
                         public void execute(Object o) {
                             dlg.dispose();
                             // to refresh the resident list
                             OPDE.getMainframe().emptySearchArea();
                             jspSearch = OPDE.getMainframe().prepareSearchArea();
                             prepareSearchArea();
                         }
                     }).getWizard();
                     dlg.setContentPane(wizard.getContentPane());
                     dlg.pack();
                     dlg.setSize(new Dimension(800, 550));
                     dlg.setVisible(true);
                 }
             });
             list.add(addRes);
 
             JideButton editRes = GUITools.createHyperlinkButton(OPDE.lang.getString("nursingrecords.info.editbw"), SYSConst.icon22edit3, null);
             editRes.addMouseListener(GUITools.getHyperlinkStyleMouseAdapter());
             editRes.setAlignmentX(Component.LEFT_ALIGNMENT);
             editRes.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     if (!resident.isActive()) {
                         OPDE.getDisplayManager().addSubMessage(new DisplayMessage("misc.msg.cantChangeInactiveResident"));
                         return;
                     }
                     new DlgEditResidentBaseData(resident, new Closure() {
                         @Override
                         public void execute(Object o) {
                             if (o != null) {
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     Resident myResident = em.merge((Resident) o);
                                     em.lock(em.merge(myResident), LockModeType.OPTIMISTIC);
                                     em.getTransaction().commit();
                                     resident = myResident;
                                     GUITools.setResidentDisplay(resident);
                                     // to refresh the resident list
                                     OPDE.getMainframe().emptySearchArea();
                                     jspSearch = OPDE.getMainframe().prepareSearchArea();
                                     prepareSearchArea();
                                     pnlCare.setJspSearch(jspSearch);
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
             list.add(editRes);
 
             /***
              *      _       ____                 _
              *     (_)___  |  _ \  ___  __ _  __| |
              *     | / __| | | | |/ _ \/ _` |/ _` |
              *     | \__ \ | |_| |  __/ (_| | (_| |
              *     |_|___/ |____/ \___|\__,_|\__,_|
              *
              */
             btnResMovedOut = GUITools.createHyperlinkButton(OPDE.lang.getString("nursingrecords.info.resident.movedout"), SYSConst.icon22residentGone, null);
             btnResDied = GUITools.createHyperlinkButton(OPDE.lang.getString("nursingrecords.info.resident.died"), SYSConst.icon22residentDied, null);
             btnResDied.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     new DlgPIT(OPDE.lang.getString("nursingrecords.info.dlg.dateofdeath"), new Closure() {
                         @Override
                         public void execute(Object o) {
                             if (o != null) {
                                 Date dod = (Date) o;
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                     resident.setStation(null);
                                     ResidentTools.endOfStay(em, em.merge(resident), dod, ResInfoTypeTools.STAY_VALUE_DEAD);
                                     em.getTransaction().commit();
                                     btnResDied.setEnabled(false);
                                     btnResMovedOut.setEnabled(false);
                                     btnResIsAway.setEnabled(false);
                                     btnResIsBack.setEnabled(false);
 
                                     OPDE.getMainframe().emptySearchArea();
                                     jspSearch = OPDE.getMainframe().prepareSearchArea();
                                     prepareSearchArea();
                                     pnlCare.setJspSearch(jspSearch);
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
             btnResDied.setEnabled(resident.isActive());
             list.add(btnResDied);
 
 
             //TODO: We need a MOVED IN AGAIN
             /***
              *                                   _               _
              *      _ __ ___   _____   _____  __| |   ___  _   _| |_
              *     | '_ ` _ \ / _ \ \ / / _ \/ _` |  / _ \| | | | __|
              *     | | | | | | (_) \ V /  __/ (_| | | (_) | |_| | |_
              *     |_| |_| |_|\___/ \_/ \___|\__,_|  \___/ \__,_|\__|
              *
              */
             btnResMovedOut.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     new DlgPIT(OPDE.lang.getString("nursingrecords.info.dlg.dateofmoveout"), new Closure() {
                         @Override
                         public void execute(Object o) {
                             if (o != null) {
                                 Date dod = (Date) o;
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
                                     resident.setStation(null);
 
                                     ResidentTools.endOfStay(em, em.merge(resident), dod, ResInfoTypeTools.STAY_VALUE_LEFT);
                                     em.getTransaction().commit();
 
                                     btnResDied.setEnabled(false);
                                     btnResMovedOut.setEnabled(false);
                                     btnResIsAway.setEnabled(false);
                                     btnResIsBack.setEnabled(false);
 
                                     OPDE.getMainframe().emptySearchArea();
                                     jspSearch = OPDE.getMainframe().prepareSearchArea();
                                     prepareSearchArea();
                                     pnlCare.setJspSearch(jspSearch);
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
             btnResMovedOut.setEnabled(resident.isActive());
             list.add(btnResMovedOut);
         } // MANAGER
 
 
         if (OPDE.getAppInfo().isAllowedTo(InternalClassACL.UPDATE, internalClassID)) {
             btnResIsAway = GUITools.createHyperlinkButton(OPDE.lang.getString("nursingrecords.info.resident.isaway"), SYSConst.icon22residentAbsent, null);
             /***
              *      _          _
              *     (_)___     / \__      ____ _ _   _
              *     | / __|   / _ \ \ /\ / / _` | | | |
              *     | \__ \  / ___ \ V  V / (_| | |_| |
              *     |_|___/ /_/   \_\_/\_/ \__,_|\__, |
              *                                  |___/
              */
             btnResIsAway.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     final JidePopup popup = new JidePopup();
                     PnlAway pnlAway = new PnlAway(new ResInfo(ResInfoTypeTools.getByType(ResInfoTypeTools.TYPE_ABSENCE), resident), new Closure() {
                         @Override
                         public void execute(Object o) {
                             popup.hidePopup();
                             if (o != null) {
                                 EntityManager em = OPDE.createEM();
                                 try {
                                     em.getTransaction().begin();
                                     Resident myResident = em.merge(resident);
                                     em.lock(myResident, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
                                     ResInfo newAbsence = em.merge((ResInfo) o);
                                     em.getTransaction().commit();
 
                                     switchResident(myResident);
                                     OPDE.getDisplayManager().addSubMessage(new DisplayMessage(OPDE.lang.getString("nursingrecords.info.msg.isawaynow")));
                                     OPDE.getMainframe().addSpeciality(newAbsence.getResInfoType(), resident);
                                 } catch (OptimisticLockException ole) {
                                     if (em.getTransaction().isActive()) {
                                         em.getTransaction().rollback();
                                     }
                                     if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                                         OPDE.getMainframe().emptyFrame();
                                         OPDE.getMainframe().afterLogin();
                                     }
                                     OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
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
                         }
                     });
                     popup.setMovable(false);
                     popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
 
                     popup.setOwner(btnResIsAway);
                     popup.removeExcludedComponent(btnResIsAway);
                     popup.getContentPane().add(pnlAway);
                     popup.setDefaultFocusComponent(pnlAway);
                     GUITools.showPopup(popup, SwingConstants.NORTH_EAST);
                 }
             });
             btnResIsAway.setEnabled(resident.isActive() && !ResInfoTools.isAway(resident));
             list.add(btnResIsAway);
 
             btnResIsBack = GUITools.createHyperlinkButton(OPDE.lang.getString("nursingrecords.info.resident.isback"), SYSConst.icon22residentBack, null);
             /***
              *      _       ____             _
              *     (_)___  | __ )  __ _  ___| | __
              *     | / __| |  _ \ / _` |/ __| |/ /
              *     | \__ \ | |_) | (_| | (__|   <
              *     |_|___/ |____/ \__,_|\___|_|\_\
              *
              */
             btnResIsBack.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     EntityManager em = OPDE.createEM();
                     try {
                         em.getTransaction().begin();
                         Resident myResident = em.merge(resident);
                         em.lock(myResident, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
 
                         ResInfo lastabsence = em.merge(ResInfoTools.getLastResinfo(resident, ResInfoTypeTools.getByType(ResInfoTypeTools.TYPE_ABSENCE)));
                         em.lock(lastabsence, LockModeType.OPTIMISTIC);
                         lastabsence.setTo(new Date());
                         lastabsence.setUserOFF(em.merge(OPDE.getLogin().getUser()));
                         em.getTransaction().commit();
 
                         switchResident(myResident);
                         OPDE.getDisplayManager().addSubMessage(new DisplayMessage(OPDE.lang.getString("nursingrecords.info.msg.isbacknow")));
                         OPDE.getMainframe().removeSpeciality(lastabsence.getResInfoType(), resident);
                     } catch (OptimisticLockException ole) {
                         OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
                         if (em.getTransaction().isActive()) {
                             em.getTransaction().rollback();
                         }
                         reload();
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
             btnResIsBack.setEnabled(resident.isActive() && ResInfoTools.isAway(resident));
             list.add(btnResIsBack);
 
 
         } // UPDATE
 
 
         return list;
     }
 
     private java.util.List<Component> addDisplayCommands() {
         java.util.List<Component> list = new ArrayList<Component>();
 //        list.add(new JSeparator());
 //        list.add(new JLabel(OPDE.lang.getString("misc.msg.key")));
 //        list.add(new JLabel(OPDE.lang.getString("nursingrecords.info.keydescription1"), SYSConst.icon22stopSign, SwingConstants.LEADING));
 //        list.add(new JLabel(OPDE.lang.getString("nursingrecords.info.keydescription7"), SYSConst.icon22stopSignGray, SwingConstants.LEADING));
 //        list.add(new JLabel(OPDE.lang.getString("nursingrecords.info.keydescription2"), SYSConst.icon22infogreen2, SwingConstants.LEADING));
 //        list.add(new JLabel(OPDE.lang.getString("nursingrecords.info.keydescription3"), SYSConst.icon22ledGreenOn, SwingConstants.LEADING));
 //        list.add(new JLabel(OPDE.lang.getString("nursingrecords.info.keydescription4"), SYSConst.icon22ledGreenOff, SwingConstants.LEADING));
 
         final JButton btnExpandAll = GUITools.createHyperlinkButton("misc.msg.expand.active", SYSConst.icon22expand, null);
         btnExpandAll.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     for (ResInfo info : listAllInfos) {
                         final String keyResInfo = info.getID() + ".resinfo";
                         final String keyResInfoType = info.getResInfoType().getID() + ".resinfotype";
                         final String keyResInfoCat = info.getResInfoType().getResInfoCat().getID() + ".resinfocat";
                         if (!info.isClosed()) {
                             if (mapKey2CP.containsKey(keyResInfo) && mapKey2CP.get(keyResInfo).isCollapsed()) {
                                 mapKey2CP.get(keyResInfo).setCollapsed(false);
                             }
                             if (mapKey2CP.containsKey(keyResInfoType) && mapKey2CP.get(keyResInfoType).isCollapsed()) {
                                 mapKey2CP.get(keyResInfoType).setCollapsed(false);
                             }
                             if (mapKey2CP.containsKey(keyResInfoCat) && mapKey2CP.get(keyResInfoCat).isCollapsed()) {
                                 mapKey2CP.get(keyResInfoCat).setCollapsed(false);
                             }
                         }
                     }
                 } catch (PropertyVetoException e) {
                     // bah!
                 }
             }
 
 
         });
         list.add(btnExpandAll);
 
         final JButton btnCollapseAll = GUITools.createHyperlinkButton("misc.msg.collapseall", SYSConst.icon22collapse, null);
 
         btnCollapseAll.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     GUITools.setCollapsed(cpsAll, true);
                 } catch (PropertyVetoException e) {
                     // bah!
                 }
             }
 
 
         });
         list.add(btnCollapseAll);
 
         return list;
     }
 
     private java.util.List<Component> addFilters() {
         java.util.List<Component> list = new ArrayList<Component>();
 
         listSearchResults = new JList();
         listSearchResults.setModel(new DefaultListModel());
 //        listSearchResults.setVisibleRowCount(7);
         listSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listSearchResults.addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (e.getValueIsAdjusting()) return;
 
 
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         // collapse everything first
                         try {
                             GUITools.setCollapsed(cpsAll, true);
                         } catch (PropertyVetoException ev) {
                             // bah!
                         }
 
 
                         String key = "";
                         if (listSearchResults.getSelectedValue() instanceof ResInfo) {
                             key = ((ResInfo) listSearchResults.getSelectedValue()).getID() + ".resinfo";
                         }
 
                         if (listSearchResults.getSelectedValue() instanceof ResInfoType) {
                             key = ((ResInfoType) listSearchResults.getSelectedValue()).getID() + ".resinfotype";
                         }
 
 
                         if (mapKey2CP.containsKey(key)) {
                             try {
                                 GUITools.expand(mapKey2CP.get(key));
                             } catch (PropertyVetoException e1) {
                                 //bah!!
                             }
                         }
                         GUITools.scroll2show(jspMain, mapKey2CP.get(key), cpsAll, null);
                     }
                 });
 
 
             }
         });
 
         listSearchResults.setCellRenderer(new ListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                 String text = "";
                 if (value instanceof ResInfo) {
                     text = ((ResInfo) value).getResInfoType().getShortDescription() + ", " + DateFormat.getDateInstance(DateFormat.SHORT).format(((ResInfo) value).getFrom());
                 } else if (value instanceof ResInfoType) {
                     text = ((ResInfoType) value).getShortDescription();
                 } else {
                     text = value.toString();
                 }
 
                 return new DefaultListCellRenderer().getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
 
             }
         });
 
         txtSearch = new JXSearchField(OPDE.lang.getString("misc.msg.searchphrase"));
         txtSearch.setFont(SYSConst.ARIAL14);
         txtSearch.setInstantSearchDelay(750);
         txtSearch.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (SYSTools.catchNull(txtSearch.getText()).trim().length() > 3) {
 
                     ArrayList<ResInfo> searchResults1 = searchInInfos(txtSearch.getText().trim());
                     ArrayList<ResInfoType> searchResults2 = searchInTypes(txtSearch.getText().trim());
                     if (searchResults1.isEmpty() && searchResults2.isEmpty()) {
                         OPDE.getDisplayManager().addSubMessage(new DisplayMessage("misc.msg.searchempty"));
                         listSearchResults.setModel(new DefaultListModel());
                     } else {
 
                         DefaultListModel dlm = new DefaultListModel();
 
                         for (ResInfoType o : searchResults2) {
                             dlm.addElement(o);
                         }
                         for (ResInfo o : searchResults1) {
                             if (searchResults2.contains(o)) {
                                 dlm.removeElement(o.getResInfoType());
                             }
                             dlm.addElement(o);
                         }
 
                         listSearchResults.setModel(dlm);
                     }
                 }
             }
         });
 
         list.add(txtSearch);
         JPanel pnlList = new JPanel();
         pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.PAGE_AXIS));
         pnlList.add(new JScrollPane(listSearchResults));
         list.add(pnlList);
 
         return list;
     }
 
     private boolean containsOnlyClosedInfos(final ResInfoType type) {
         boolean containsOnlyClosedInfos = true;
         if (mapType2ResInfos.containsKey(type)) {
             for (ResInfo info : mapType2ResInfos.get(type)) {
                 containsOnlyClosedInfos = info.isClosed();
                 if (!containsOnlyClosedInfos) {
                     break;
                 }
             }
         }
         return containsOnlyClosedInfos;
     }
 
     private ArrayList<ResInfo> searchInInfos(String pattern) {
         pattern = pattern.toLowerCase();
         ArrayList<ResInfo> hits = new ArrayList<ResInfo>();
         for (ResInfo info : listAllInfos) {
             boolean hit = false;
             if (!hit) hit |= SYSTools.catchNull(info.getText()).toLowerCase().indexOf(pattern) >= 0;
             if (!hit) hit |= SYSTools.catchNull(info.getHtml()).toLowerCase().indexOf(pattern) >= 0;
             if (!hit) hit |= SYSTools.catchNull(info.getProperties()).toLowerCase().indexOf(pattern) >= 0;
             if (hit) hits.add(info);
         }
         return hits;
     }
 
     private ArrayList<ResInfoType> searchInTypes(String pattern) {
         pattern = pattern.toLowerCase();
 
         ArrayList<ResInfoType> hits = new ArrayList<ResInfoType>();
         for (ResInfoType type : listAllTypes) {
             boolean hit = false;
             if (!hit)
                 hit |= SYSTools.catchNull(type.getShortDescription()).toLowerCase().indexOf(pattern) >= 0;
             if (!hit)
                 hit |= SYSTools.catchNull(type.getResInfoCat().getText()).toLowerCase().indexOf(pattern) >= 0;
             if (!hit)
                 hit |= SYSTools.catchNull(type.getLongDescription()).toLowerCase().indexOf(pattern) >= 0;
             if (!hit)
                 hit |= SYSTools.catchNull(type.getXml()).toLowerCase().indexOf(pattern) >= 0;
             if (hit) hits.add(type);
         }
 
         return hits;
     }
 }
