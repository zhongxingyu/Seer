 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * TopLevelMenu.java
  *
  * Created on Jan 31, 2011, 2:29:18 PM
  */
 
 package chocansimulator;
 
 import chocansimulator.datamangement.Bill;
 import chocansimulator.datamangement.BillManager;
 import chocansimulator.datamangement.Id;
 import chocansimulator.datamangement.Member;
 import chocansimulator.datamangement.MemberManager;
 import chocansimulator.datamangement.Provider;
 import chocansimulator.datamangement.ProviderManager;
 import chocansimulator.datamangement.ServiceCode;
 import chocansimulator.datamangement.ServiceCodeManager;
 import chocansimulator.reports.EFTReport;
 import chocansimulator.reports.MemberReport;
 import chocansimulator.reports.ProviderReport;
 import chocansimulator.reports.SummaryReport;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 /**
  *
  * @author tman
  */
 public class ChocAnUserInterface extends javax.swing.JFrame {
     public static final String chocAnDataDir = "chocAnData";
     public static final String chocAnReportsDir = "chocAnData/Reports";
     public static final String chocAnMemberData = chocAnDataDir + "/member.dat";
     public static final String chocAnId = chocAnDataDir + "/id.dat";
     public static final String chocAnServiceData = chocAnDataDir + "/svc.dat";
     public static final String chocAnProviderData = chocAnDataDir + "/provider.dat";
     public static final String chocAnBillData = chocAnDataDir + "/bill.dat";
     public static final char chocAnFileDelimiter = '^';
     public static final ServiceCodeManager svcMan = ServiceCodeManager.singletonServiceCodeManager(chocAnServiceData);
     public static final MemberManager memberMan = MemberManager.singletonMemberManager(chocAnMemberData);
     public static final BillManager billMan = BillManager.singletonBillManager(chocAnBillData);
     public static final ProviderManager providerMan = ProviderManager.singletonProviderManager(chocAnProviderData);
     
     NumberFormat feeFormat = new DecimalFormat("#0.00");
 
     /** Creates new form TopLevelMenu */
     public ChocAnUserInterface() {
         
         initComponents();
 
         File file = new File(chocAnReportsDir);
         if ( !file.exists())
             file.mkdirs();
 
         if (billMan.numOfRecords() > 0) {
             if (memberMan.numOfRecords() == 0 || providerMan.numOfRecords() == 0 ||
                 svcMan.numOfRecords() == 0) {
                     System.out.println("Files have been corrupted can not execute");
                     System.exit(1);
             }
         }
 
         if  (memberMan.numOfRecords() > 0 ) {
             if (Id.singletonId(chocAnId).getCurrentMemberId() == 0) {
                 System.out.println("member.dat does not match id.dat");
                 System.exit(1);
             }
         }
 
         if  (providerMan.numOfRecords() > 0 ) {
             if (Id.singletonId(chocAnId).getCurrentProviderId() == 0) {
                 System.out.println("provider.dat does not match id.dat");
                 System.exit(1);
             }
         }
 
         if  (svcMan.numOfRecords() > 0 ) {
             if (Id.singletonId(chocAnId).getCurrentServiceCodeId() == 0) {
                 System.out.println("svc.dat does not match id.dat");
                 System.exit(1);
             }
         }
 
     }
 
     private void validateString(String fieldName, String str, int len) throws FieldException {
 
         if ( str.trim().length() > len ) {
             globalMessageLabel.setText(fieldName + " is too long");
             throw new FieldException();
         }
 
         if ( str.trim().isEmpty() ) {
             globalMessageLabel.setText(fieldName + " is required");
             throw new FieldException();
         }
         int i = str.indexOf('^');
         if (i != -1) {
             globalMessageLabel.setText(fieldName + " contains invalid character '^'.");
             throw new FieldException();
         }
 
     }
 
     private void validateInt(String fieldName, String str, int len) throws FieldException {
 
         try {
             int i = Integer.parseInt(str.trim());
         } catch (NumberFormatException e) {
               globalMessageLabel.setText(fieldName + " is not a legal number");
               throw new FieldException();
         }
 
         if ( str.trim().length() > len ) {
             globalMessageLabel.setText(fieldName + " contains too many digits");
             throw new FieldException();
         }
     }
 
     private void validateFloat(String fieldName, String str, int len) throws FieldException {
 
         try {
             float f = Float.valueOf(str.trim()).floatValue();
         } catch (NumberFormatException e) {
               globalMessageLabel.setText(fieldName + " is not a legal number");
               throw new FieldException();
         }
 
         if ( str.trim().length() > len + 1 ){
             globalMessageLabel.setText(fieldName + " contains too many digits");
             throw new FieldException();
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         ChocAnPanel = new javax.swing.JTabbedPane();
         MemberTabbedPanel = new javax.swing.JPanel();
         memberDataManagerPanels = new javax.swing.JTabbedPane();
         memberAddPanel = new javax.swing.JPanel();
         memberAddNameLabel = new javax.swing.JLabel();
         memberAddAddressLabel = new javax.swing.JLabel();
         memberAddCityLabel = new javax.swing.JLabel();
         memberAddStateLabel = new javax.swing.JLabel();
         memberAddZipLabel = new javax.swing.JLabel();
         memberAddNameText = new javax.swing.JTextField();
         memberAddAddressText = new javax.swing.JTextField();
         memberAddCityText = new javax.swing.JTextField();
         memberAddStateText = new javax.swing.JTextField();
         memberAddZipText = new javax.swing.JTextField();
         memberAddButton = new javax.swing.JButton();
         memberAddClearButton = new javax.swing.JButton();
         memberUpdatePanel = new javax.swing.JPanel();
         memberUpNameLabel = new javax.swing.JLabel();
         memberUpAddressLabel = new javax.swing.JLabel();
         memberUpCityLabel = new javax.swing.JLabel();
         memberUpStateLabel = new javax.swing.JLabel();
         memberUpZipLabel = new javax.swing.JLabel();
         memberUpNameText = new javax.swing.JTextField();
         memberUpAddressText = new javax.swing.JTextField();
         memberUpCityText = new javax.swing.JTextField();
         memberUpStateText = new javax.swing.JTextField();
         memberUpZipText = new javax.swing.JTextField();
         memberUpdateButton = new javax.swing.JButton();
         memberUpClearButton = new javax.swing.JButton();
         memberUpNumLabel = new javax.swing.JLabel();
         memberUpNumText = new javax.swing.JTextField();
         memberUpSearchButton = new javax.swing.JButton();
         memberDeletePanel = new javax.swing.JPanel();
         memberDelNameLabel = new javax.swing.JLabel();
         memberDelAddressLabel = new javax.swing.JLabel();
         memberDelCityLabel = new javax.swing.JLabel();
         memberDelStateLabel = new javax.swing.JLabel();
         memberDelZipLabel = new javax.swing.JLabel();
         memberDelNameText = new javax.swing.JTextField();
         memberDelAddressText = new javax.swing.JTextField();
         memberDelCityText = new javax.swing.JTextField();
         memberDelStateText = new javax.swing.JTextField();
         memberDelZipText = new javax.swing.JTextField();
         memberDeleteButton = new javax.swing.JButton();
         memberDelClearButton = new javax.swing.JButton();
         memberDelNumLabel = new javax.swing.JLabel();
         memberDelNumText = new javax.swing.JTextField();
         memberDelSearchButton = new javax.swing.JButton();
         ProviderTabbedPanel = new javax.swing.JPanel();
         ProviderDataManagerPanels = new javax.swing.JTabbedPane();
         providerAddPanel = new javax.swing.JPanel();
         providerAddNameLabel = new javax.swing.JLabel();
         providerAddAddressLabel = new javax.swing.JLabel();
         providerAddCityLabel = new javax.swing.JLabel();
         providerAddStateLabel = new javax.swing.JLabel();
         providerAddZipLabel = new javax.swing.JLabel();
         providerAddNameText = new javax.swing.JTextField();
         providerAddAddressText = new javax.swing.JTextField();
         providerAddCityText = new javax.swing.JTextField();
         providerAddStateText = new javax.swing.JTextField();
         providerAddZipText = new javax.swing.JTextField();
         providerAddButton = new javax.swing.JButton();
         providerAddClearButton = new javax.swing.JButton();
         providerUpdatePanel = new javax.swing.JPanel();
         providerUpNameLabel = new javax.swing.JLabel();
         providerUpAddressLabel = new javax.swing.JLabel();
         providerUpCityLabel = new javax.swing.JLabel();
         providerUpStateLabel = new javax.swing.JLabel();
         providerUpZipLabel = new javax.swing.JLabel();
         providerUpNameText = new javax.swing.JTextField();
         providerUpAddressText = new javax.swing.JTextField();
         providerUpCityText = new javax.swing.JTextField();
         providerUpStateText = new javax.swing.JTextField();
         providerUpZipText = new javax.swing.JTextField();
         providerUpdateButton = new javax.swing.JButton();
         providerUpClearButton = new javax.swing.JButton();
         providerUpNumLabel = new javax.swing.JLabel();
         providerUpNumText = new javax.swing.JTextField();
         providerUpSearchButton = new javax.swing.JButton();
         providerDeletePanel = new javax.swing.JPanel();
         providerDelNameLabel = new javax.swing.JLabel();
         providerDelAddressLabel = new javax.swing.JLabel();
         providerDelCityLabel = new javax.swing.JLabel();
         providerDelStateLabel = new javax.swing.JLabel();
         providerDelZipLabel = new javax.swing.JLabel();
         providerDelNameText = new javax.swing.JTextField();
         providerDelAddressText = new javax.swing.JTextField();
         providerDelCityText = new javax.swing.JTextField();
         providerDelStateText = new javax.swing.JTextField();
         providerDelZipText = new javax.swing.JTextField();
         providerDeleteButton = new javax.swing.JButton();
         providerDelClearButton = new javax.swing.JButton();
         providerDelNumLabel = new javax.swing.JLabel();
         providerDelNumText = new javax.swing.JTextField();
         providerDelSearchButton = new javax.swing.JButton();
         ServiceCodeTabbedPanel = new javax.swing.JPanel();
         svcDataManagerPanels = new javax.swing.JTabbedPane();
         svcAddPanel = new javax.swing.JPanel();
         descLabel = new javax.swing.JLabel();
         feeLabel = new javax.swing.JLabel();
         svcAddDescText = new javax.swing.JTextField();
         svcAddFeeText = new javax.swing.JTextField();
         svcAddButton = new javax.swing.JButton();
         svcAddClearButton = new javax.swing.JButton();
         svcUpdatePanel = new javax.swing.JPanel();
         jPanel9 = new javax.swing.JPanel();
         svcUpNumLabel = new javax.swing.JLabel();
         svcUpDescLabel = new javax.swing.JLabel();
         svcUpFeeLabel = new javax.swing.JLabel();
         svcUpNumText = new javax.swing.JTextField();
         svcUpDescText = new javax.swing.JTextField();
         svcUpFeeText = new javax.swing.JTextField();
         svcUpSearchButton = new javax.swing.JButton();
         svcUpdateButton = new javax.swing.JButton();
         svcUpdateClearButton = new javax.swing.JButton();
         jPanel10 = new javax.swing.JPanel();
         svcDelNumLabel = new javax.swing.JLabel();
         svcDelDescLabel = new javax.swing.JLabel();
         svcDelFeeLabel = new javax.swing.JLabel();
         svcDelNumText = new javax.swing.JTextField();
         svcDelDescText = new javax.swing.JTextField();
         svcDelFeeText = new javax.swing.JTextField();
         svcDelSearchButton = new javax.swing.JButton();
         svcDeleteButton = new javax.swing.JButton();
         svcDelClearButton = new javax.swing.JButton();
         ReportsTabbedPanel = new javax.swing.JPanel();
         memberRptButton = new javax.swing.JButton();
         eftRptButton = new javax.swing.JButton();
         summaryRptButton = new javax.swing.JButton();
         providerRptButton = new javax.swing.JButton();
         TerminalTabbedPanel = new javax.swing.JPanel();
         terminalDataManagerPanels = new javax.swing.JTabbedPane();
         valMemPanel = new javax.swing.JPanel();
         valMemLabel = new javax.swing.JLabel();
         valMemText = new javax.swing.JTextField();
         valMemButton = new javax.swing.JButton();
         valMemClearButton = new javax.swing.JButton();
         valProPanel = new javax.swing.JPanel();
         valProLabel = new javax.swing.JLabel();
         valProText = new javax.swing.JTextField();
         valProButton = new javax.swing.JButton();
         valProClearButton = new javax.swing.JButton();
         valSvcPanel = new javax.swing.JPanel();
         valSvcLabel = new javax.swing.JLabel();
         valSvcText = new javax.swing.JTextField();
         valSvcButton = new javax.swing.JButton();
         valSvcClearButton = new javax.swing.JButton();
         proDirPanel = new javax.swing.JPanel();
         runProDirButton = new javax.swing.JButton();
         addBillPanel = new javax.swing.JPanel();
         dateLabel = new javax.swing.JLabel();
         memNumLabel = new javax.swing.JLabel();
         proNumLabel = new javax.swing.JLabel();
         svcLabel = new javax.swing.JLabel();
         commentLabel = new javax.swing.JLabel();
         billDateText = new javax.swing.JTextField();
         billMemNumText = new javax.swing.JTextField();
         billProNumText = new javax.swing.JTextField();
         billSvcText = new javax.swing.JTextField();
         billCommentText = new javax.swing.JTextField();
         addBillButton = new javax.swing.JButton();
         billClearButton = new javax.swing.JButton();
         globalMessageLabel = new javax.swing.JLabel();
         exitButton = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("CHOCHOLICS");
 
         ChocAnPanel.setMinimumSize(new java.awt.Dimension(108, 140));
         ChocAnPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 chocAnPanelMouseClick(evt);
             }
         });
 
         memberDataManagerPanels.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 dataManagerPanelsMouseClick(evt);
             }
         });
 
         memberAddNameLabel.setText("Name:");
 
         memberAddAddressLabel.setText("Address:");
 
         memberAddCityLabel.setText("City:");
 
         memberAddStateLabel.setText("State:");
 
         memberAddZipLabel.setText("Zip:");
 
         memberAddButton.setText("Add");
         memberAddButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberAddButtonActionPerformed(evt);
             }
         });
 
         memberAddClearButton.setText("Clear");
         memberAddClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberAddClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout memberAddPanelLayout = new javax.swing.GroupLayout(memberAddPanel);
         memberAddPanel.setLayout(memberAddPanelLayout);
         memberAddPanelLayout.setHorizontalGroup(
             memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(memberAddPanelLayout.createSequentialGroup()
                 .addGroup(memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(memberAddNameLabel)
                     .addComponent(memberAddAddressLabel))
                 .addGap(18, 18, 18)
                 .addGroup(memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(memberAddNameText)
                     .addComponent(memberAddAddressText, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                 .addContainerGap(98, Short.MAX_VALUE))
             .addGroup(memberAddPanelLayout.createSequentialGroup()
                 .addComponent(memberAddCityLabel)
                 .addGap(6, 6, 6)
                 .addComponent(memberAddCityText, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(memberAddStateLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(memberAddStateText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(memberAddZipLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(memberAddZipText, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(142, 142, 142))
             .addGroup(memberAddPanelLayout.createSequentialGroup()
                 .addGap(112, 112, 112)
                 .addComponent(memberAddButton)
                 .addGap(68, 68, 68)
                 .addComponent(memberAddClearButton)
                 .addContainerGap(134, Short.MAX_VALUE))
         );
         memberAddPanelLayout.setVerticalGroup(
             memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(memberAddPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberAddNameLabel)
                     .addComponent(memberAddNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(26, 26, 26)
                 .addGroup(memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberAddAddressLabel)
                     .addComponent(memberAddAddressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(27, 27, 27)
                 .addGroup(memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberAddCityLabel)
                     .addComponent(memberAddCityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberAddZipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberAddZipLabel)
                     .addComponent(memberAddStateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberAddStateLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                 .addGroup(memberAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberAddButton)
                     .addComponent(memberAddClearButton))
                 .addContainerGap())
         );
 
         memberDataManagerPanels.addTab("Add", memberAddPanel);
 
         memberUpNameLabel.setText("Name:");
 
         memberUpAddressLabel.setText("Address:");
 
         memberUpCityLabel.setText("City:");
 
         memberUpStateLabel.setText("State:");
 
         memberUpZipLabel.setText("Zip:");
 
         memberUpdateButton.setText("Update");
         memberUpdateButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberUpdateButtonActionPerformed(evt);
             }
         });
 
         memberUpClearButton.setText("Clear");
         memberUpClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberUpClearButtonActionPerformed(evt);
             }
         });
 
         memberUpNumLabel.setText("Number:");
 
         memberUpSearchButton.setText("Search");
         memberUpSearchButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberUpSearchButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout memberUpdatePanelLayout = new javax.swing.GroupLayout(memberUpdatePanel);
         memberUpdatePanel.setLayout(memberUpdatePanelLayout);
         memberUpdatePanelLayout.setHorizontalGroup(
             memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(memberUpdatePanelLayout.createSequentialGroup()
                 .addGap(112, 112, 112)
                 .addComponent(memberUpdateButton)
                 .addGap(68, 68, 68)
                 .addComponent(memberUpClearButton)
                 .addContainerGap(110, Short.MAX_VALUE))
             .addGroup(memberUpdatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(memberUpNameLabel)
                 .addContainerGap(345, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, memberUpdatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, memberUpdatePanelLayout.createSequentialGroup()
                         .addComponent(memberUpNumLabel)
                         .addGap(18, 18, 18)
                         .addComponent(memberUpNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                         .addComponent(memberUpSearchButton))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, memberUpdatePanelLayout.createSequentialGroup()
                         .addComponent(memberUpCityLabel)
                         .addGap(6, 6, 6)
                         .addComponent(memberUpCityText, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(6, 6, 6)
                         .addComponent(memberUpStateLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(memberUpStateText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(memberUpZipLabel))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, memberUpdatePanelLayout.createSequentialGroup()
                         .addComponent(memberUpAddressLabel)
                         .addGap(18, 18, 18)
                         .addGroup(memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(memberUpNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                             .addComponent(memberUpAddressText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))))
                 .addGap(18, 18, 18)
                 .addComponent(memberUpZipText, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(16, 16, 16))
         );
         memberUpdatePanelLayout.setVerticalGroup(
             memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(memberUpdatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberUpNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberUpNumLabel)
                     .addComponent(memberUpSearchButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberUpNameLabel)
                     .addComponent(memberUpNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberUpAddressLabel)
                     .addComponent(memberUpAddressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberUpZipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberUpZipLabel)
                     .addComponent(memberUpStateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberUpStateLabel)
                     .addComponent(memberUpCityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberUpCityLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                 .addGroup(memberUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberUpdateButton)
                     .addComponent(memberUpClearButton))
                 .addContainerGap())
         );
 
         memberDataManagerPanels.addTab("Update", memberUpdatePanel);
 
         memberDelNameLabel.setText("Name:");
 
         memberDelAddressLabel.setText("Address:");
 
         memberDelCityLabel.setText("City:");
 
         memberDelStateLabel.setText("State:");
 
         memberDelZipLabel.setText("Zip:");
 
         memberDeleteButton.setText("Delete");
         memberDeleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberDeleteButtonActionPerformed(evt);
             }
         });
 
         memberDelClearButton.setText("Clear");
         memberDelClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberDelClearButtonActionPerformed(evt);
             }
         });
 
         memberDelNumLabel.setText("Number:");
 
         memberDelSearchButton.setText("Search");
         memberDelSearchButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberDelSearchButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout memberDeletePanelLayout = new javax.swing.GroupLayout(memberDeletePanel);
         memberDeletePanel.setLayout(memberDeletePanelLayout);
         memberDeletePanelLayout.setHorizontalGroup(
             memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(memberDeletePanelLayout.createSequentialGroup()
                 .addGap(112, 112, 112)
                 .addComponent(memberDeleteButton)
                 .addGap(68, 68, 68)
                 .addComponent(memberDelClearButton)
                 .addContainerGap(113, Short.MAX_VALUE))
             .addGroup(memberDeletePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(memberDelNameLabel)
                 .addContainerGap(345, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, memberDeletePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, memberDeletePanelLayout.createSequentialGroup()
                         .addComponent(memberDelNumLabel)
                         .addGap(18, 18, 18)
                         .addComponent(memberDelNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                         .addComponent(memberDelSearchButton))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, memberDeletePanelLayout.createSequentialGroup()
                         .addComponent(memberDelCityLabel)
                         .addGap(6, 6, 6)
                         .addComponent(memberDelCityText, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(6, 6, 6)
                         .addComponent(memberDelStateLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(memberDelStateText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(memberDelZipLabel))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, memberDeletePanelLayout.createSequentialGroup()
                         .addComponent(memberDelAddressLabel)
                         .addGap(18, 18, 18)
                         .addGroup(memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(memberDelNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                             .addComponent(memberDelAddressText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))))
                 .addGap(18, 18, 18)
                 .addComponent(memberDelZipText, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(16, 16, 16))
         );
         memberDeletePanelLayout.setVerticalGroup(
             memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(memberDeletePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberDelNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberDelNumLabel)
                     .addComponent(memberDelSearchButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberDelNameLabel)
                     .addComponent(memberDelNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberDelAddressLabel)
                     .addComponent(memberDelAddressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberDelZipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberDelZipLabel)
                     .addComponent(memberDelStateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberDelStateLabel)
                     .addComponent(memberDelCityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(memberDelCityLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                 .addGroup(memberDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberDeleteButton)
                     .addComponent(memberDelClearButton))
                 .addContainerGap())
         );
 
         memberDataManagerPanels.addTab("Delete", memberDeletePanel);
 
         javax.swing.GroupLayout MemberTabbedPanelLayout = new javax.swing.GroupLayout(MemberTabbedPanel);
         MemberTabbedPanel.setLayout(MemberTabbedPanelLayout);
         MemberTabbedPanelLayout.setHorizontalGroup(
             MemberTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(MemberTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(memberDataManagerPanels, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(104, Short.MAX_VALUE))
         );
         MemberTabbedPanelLayout.setVerticalGroup(
             MemberTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(MemberTabbedPanelLayout.createSequentialGroup()
                 .addComponent(memberDataManagerPanels)
                 .addGap(24, 24, 24))
         );
 
         ChocAnPanel.addTab("Member", MemberTabbedPanel);
 
         ProviderDataManagerPanels.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 providerManagerPanelsMouseClick(evt);
             }
         });
 
         providerAddNameLabel.setText("Name:");
 
         providerAddAddressLabel.setText("Address:");
 
         providerAddCityLabel.setText("City:");
 
         providerAddStateLabel.setText("State:");
 
         providerAddZipLabel.setText("Zip:");
 
         providerAddButton.setText("Add");
         providerAddButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerAddButtonActionPerformed(evt);
             }
         });
 
         providerAddClearButton.setText("Clear");
         providerAddClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerAddClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout providerAddPanelLayout = new javax.swing.GroupLayout(providerAddPanel);
         providerAddPanel.setLayout(providerAddPanelLayout);
         providerAddPanelLayout.setHorizontalGroup(
             providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(providerAddPanelLayout.createSequentialGroup()
                 .addGroup(providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(providerAddNameLabel)
                     .addComponent(providerAddAddressLabel))
                 .addGap(18, 18, 18)
                 .addGroup(providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(providerAddNameText)
                     .addComponent(providerAddAddressText, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                 .addContainerGap(98, Short.MAX_VALUE))
             .addGroup(providerAddPanelLayout.createSequentialGroup()
                 .addComponent(providerAddCityLabel)
                 .addGap(6, 6, 6)
                 .addComponent(providerAddCityText, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(providerAddStateLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(providerAddStateText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(providerAddZipLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(providerAddZipText, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(142, 142, 142))
             .addGroup(providerAddPanelLayout.createSequentialGroup()
                 .addGap(112, 112, 112)
                 .addComponent(providerAddButton)
                 .addGap(68, 68, 68)
                 .addComponent(providerAddClearButton)
                 .addContainerGap(134, Short.MAX_VALUE))
         );
         providerAddPanelLayout.setVerticalGroup(
             providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(providerAddPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerAddNameLabel)
                     .addComponent(providerAddNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(26, 26, 26)
                 .addGroup(providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerAddAddressLabel)
                     .addComponent(providerAddAddressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(27, 27, 27)
                 .addGroup(providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerAddCityLabel)
                     .addComponent(providerAddCityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerAddZipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerAddZipLabel)
                     .addComponent(providerAddStateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerAddStateLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                 .addGroup(providerAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerAddButton)
                     .addComponent(providerAddClearButton))
                 .addContainerGap())
         );
 
         ProviderDataManagerPanels.addTab("Add", providerAddPanel);
 
         providerUpNameLabel.setText("Name:");
 
         providerUpAddressLabel.setText("Address:");
 
         providerUpCityLabel.setText("City:");
 
         providerUpStateLabel.setText("State:");
 
         providerUpZipLabel.setText("Zip:");
 
         providerUpdateButton.setText("Update");
         providerUpdateButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerUpdateButtonActionPerformed(evt);
             }
         });
 
         providerUpClearButton.setText("Clear");
         providerUpClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerUpClearButtonActionPerformed(evt);
             }
         });
 
         providerUpNumLabel.setText("Number:");
 
         providerUpSearchButton.setText("Search");
         providerUpSearchButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerUpSearchButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout providerUpdatePanelLayout = new javax.swing.GroupLayout(providerUpdatePanel);
         providerUpdatePanel.setLayout(providerUpdatePanelLayout);
         providerUpdatePanelLayout.setHorizontalGroup(
             providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(providerUpdatePanelLayout.createSequentialGroup()
                 .addGap(112, 112, 112)
                 .addComponent(providerUpdateButton)
                 .addGap(68, 68, 68)
                 .addComponent(providerUpClearButton)
                 .addContainerGap(110, Short.MAX_VALUE))
             .addGroup(providerUpdatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(providerUpNameLabel)
                 .addContainerGap(345, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, providerUpdatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, providerUpdatePanelLayout.createSequentialGroup()
                         .addComponent(providerUpNumLabel)
                         .addGap(18, 18, 18)
                         .addComponent(providerUpNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                         .addComponent(providerUpSearchButton))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, providerUpdatePanelLayout.createSequentialGroup()
                         .addComponent(providerUpCityLabel)
                         .addGap(6, 6, 6)
                         .addComponent(providerUpCityText, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(6, 6, 6)
                         .addComponent(providerUpStateLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(providerUpStateText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(providerUpZipLabel))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, providerUpdatePanelLayout.createSequentialGroup()
                         .addComponent(providerUpAddressLabel)
                         .addGap(18, 18, 18)
                         .addGroup(providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(providerUpNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                             .addComponent(providerUpAddressText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))))
                 .addGap(18, 18, 18)
                 .addComponent(providerUpZipText, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(16, 16, 16))
         );
         providerUpdatePanelLayout.setVerticalGroup(
             providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(providerUpdatePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerUpNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerUpNumLabel)
                     .addComponent(providerUpSearchButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerUpNameLabel)
                     .addComponent(providerUpNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerUpAddressLabel)
                     .addComponent(providerUpAddressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerUpZipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerUpZipLabel)
                     .addComponent(providerUpStateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerUpStateLabel)
                     .addComponent(providerUpCityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerUpCityLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                 .addGroup(providerUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerUpdateButton)
                     .addComponent(providerUpClearButton))
                 .addContainerGap())
         );
 
         ProviderDataManagerPanels.addTab("Update", providerUpdatePanel);
 
         providerDelNameLabel.setText("Name:");
 
         providerDelAddressLabel.setText("Address:");
 
         providerDelCityLabel.setText("City:");
 
         providerDelStateLabel.setText("State:");
 
         providerDelZipLabel.setText("Zip:");
 
         providerDeleteButton.setText("Delete");
         providerDeleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerDeleteButtonActionPerformed(evt);
             }
         });
 
         providerDelClearButton.setText("Clear");
         providerDelClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerDelClearButtonActionPerformed(evt);
             }
         });
 
         providerDelNumLabel.setText("Number:");
 
         providerDelSearchButton.setText("Search");
         providerDelSearchButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerDelSearchButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout providerDeletePanelLayout = new javax.swing.GroupLayout(providerDeletePanel);
         providerDeletePanel.setLayout(providerDeletePanelLayout);
         providerDeletePanelLayout.setHorizontalGroup(
             providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(providerDeletePanelLayout.createSequentialGroup()
                 .addGap(112, 112, 112)
                 .addComponent(providerDeleteButton)
                 .addGap(68, 68, 68)
                 .addComponent(providerDelClearButton)
                 .addContainerGap(113, Short.MAX_VALUE))
             .addGroup(providerDeletePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(providerDelNameLabel)
                 .addContainerGap(345, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, providerDeletePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, providerDeletePanelLayout.createSequentialGroup()
                         .addComponent(providerDelNumLabel)
                         .addGap(18, 18, 18)
                         .addComponent(providerDelNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                         .addComponent(providerDelSearchButton))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, providerDeletePanelLayout.createSequentialGroup()
                         .addComponent(providerDelCityLabel)
                         .addGap(6, 6, 6)
                         .addComponent(providerDelCityText, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(6, 6, 6)
                         .addComponent(providerDelStateLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(providerDelStateText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(providerDelZipLabel))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, providerDeletePanelLayout.createSequentialGroup()
                         .addComponent(providerDelAddressLabel)
                         .addGap(18, 18, 18)
                         .addGroup(providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(providerDelNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                             .addComponent(providerDelAddressText, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))))
                 .addGap(18, 18, 18)
                 .addComponent(providerDelZipText, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(16, 16, 16))
         );
         providerDeletePanelLayout.setVerticalGroup(
             providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(providerDeletePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerDelNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerDelNumLabel)
                     .addComponent(providerDelSearchButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerDelNameLabel)
                     .addComponent(providerDelNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerDelAddressLabel)
                     .addComponent(providerDelAddressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerDelZipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerDelZipLabel)
                     .addComponent(providerDelStateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerDelStateLabel)
                     .addComponent(providerDelCityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(providerDelCityLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                 .addGroup(providerDeletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerDeleteButton)
                     .addComponent(providerDelClearButton))
                 .addContainerGap())
         );
 
         ProviderDataManagerPanels.addTab("Delete", providerDeletePanel);
 
         javax.swing.GroupLayout ProviderTabbedPanelLayout = new javax.swing.GroupLayout(ProviderTabbedPanel);
         ProviderTabbedPanel.setLayout(ProviderTabbedPanelLayout);
         ProviderTabbedPanelLayout.setHorizontalGroup(
             ProviderTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ProviderTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(ProviderDataManagerPanels, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(104, Short.MAX_VALUE))
         );
         ProviderTabbedPanelLayout.setVerticalGroup(
             ProviderTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ProviderTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(ProviderDataManagerPanels)
                 .addContainerGap())
         );
 
         ChocAnPanel.addTab("Provider", ProviderTabbedPanel);
 
         svcDataManagerPanels.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 svcCodeManagerPanelsMouseClick(evt);
             }
         });
 
         descLabel.setText("Description:");
 
         feeLabel.setText("Fee:");
 
         svcAddDescText.setText(" ");
 
         svcAddFeeText.setText(" ");
 
         svcAddButton.setText("Add");
         svcAddButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcAddButtonActionPerformed(evt);
             }
         });
 
         svcAddClearButton.setText("Clear");
         svcAddClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcAddClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout svcAddPanelLayout = new javax.swing.GroupLayout(svcAddPanel);
         svcAddPanel.setLayout(svcAddPanelLayout);
         svcAddPanelLayout.setHorizontalGroup(
             svcAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(svcAddPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(svcAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(descLabel)
                     .addComponent(feeLabel))
                 .addGap(26, 26, 26)
                 .addGroup(svcAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(svcAddPanelLayout.createSequentialGroup()
                         .addComponent(svcAddButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(svcAddClearButton))
                     .addComponent(svcAddFeeText)
                     .addComponent(svcAddDescText, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                 .addContainerGap(210, Short.MAX_VALUE))
         );
         svcAddPanelLayout.setVerticalGroup(
             svcAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(svcAddPanelLayout.createSequentialGroup()
                 .addGap(32, 32, 32)
                 .addGroup(svcAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(svcAddDescText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(descLabel))
                 .addGap(21, 21, 21)
                 .addGroup(svcAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(svcAddPanelLayout.createSequentialGroup()
                         .addComponent(svcAddFeeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addGroup(svcAddPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(svcAddButton)
                             .addComponent(svcAddClearButton)))
                     .addComponent(feeLabel))
                 .addContainerGap(52, Short.MAX_VALUE))
         );
 
         svcDataManagerPanels.addTab("Add", svcAddPanel);
 
         svcUpNumLabel.setText("Service Code:");
 
         svcUpDescLabel.setText("Description:");
 
         svcUpFeeLabel.setText("Fee:");
 
         svcUpNumText.setText(" ");
 
         svcUpDescText.setMaximumSize(new java.awt.Dimension(20, 20));
 
         svcUpFeeText.setText(" ");
 
         svcUpSearchButton.setText("Search");
         svcUpSearchButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcUpSearchButtonActionPerformed(evt);
             }
         });
 
         svcUpdateButton.setText("Update");
         svcUpdateButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcUpdateButtonActionPerformed(evt);
             }
         });
 
         svcUpdateClearButton.setText("Clear");
         svcUpdateClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcUpdateClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(svcUpDescLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(svcUpNumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(38, 38, 38))
                     .addComponent(svcUpFeeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addGap(1, 1, 1)
                         .addComponent(svcUpNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(41, 41, 41)
                         .addComponent(svcUpSearchButton))
                     .addComponent(svcUpFeeText, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(svcUpDescText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(37, 37, 37))
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addGap(88, 88, 88)
                 .addComponent(svcUpdateButton)
                 .addGap(54, 54, 54)
                 .addComponent(svcUpdateClearButton)
                 .addContainerGap(130, Short.MAX_VALUE))
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcUpNumLabel)
                     .addComponent(svcUpNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(svcUpSearchButton))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcUpDescLabel)
                     .addComponent(svcUpDescText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcUpFeeLabel)
                     .addComponent(svcUpFeeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcUpdateButton)
                     .addComponent(svcUpdateClearButton))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout svcUpdatePanelLayout = new javax.swing.GroupLayout(svcUpdatePanel);
         svcUpdatePanel.setLayout(svcUpdatePanelLayout);
         svcUpdatePanelLayout.setHorizontalGroup(
             svcUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(svcUpdatePanelLayout.createSequentialGroup()
                 .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(110, Short.MAX_VALUE))
         );
         svcUpdatePanelLayout.setVerticalGroup(
             svcUpdatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(svcUpdatePanelLayout.createSequentialGroup()
                 .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         svcDataManagerPanels.addTab("Update", svcUpdatePanel);
 
         svcDelNumLabel.setText("Service Code:");
 
         svcDelDescLabel.setText("Description:");
 
         svcDelFeeLabel.setText("Fee:");
 
         svcDelNumText.setText(" ");
 
         svcDelDescText.setMaximumSize(new java.awt.Dimension(20, 20));
 
         svcDelFeeText.setText(" ");
 
         svcDelSearchButton.setText("Search");
         svcDelSearchButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcDelSearchButtonActionPerformed(evt);
             }
         });
 
         svcDeleteButton.setText("Delete");
         svcDeleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcDeleteButtonActionPerformed(evt);
             }
         });
 
         svcDelClearButton.setText("Clear");
         svcDelClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 svcDelClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addComponent(svcDelDescLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addComponent(svcDelNumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                         .addGap(38, 38, 38))
                     .addComponent(svcDelFeeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addGap(1, 1, 1)
                         .addComponent(svcDelNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(41, 41, 41)
                         .addComponent(svcDelSearchButton))
                     .addComponent(svcDelFeeText, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(svcDelDescText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(131, 131, 131))
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addGap(88, 88, 88)
                 .addComponent(svcDeleteButton)
                 .addGap(54, 54, 54)
                 .addComponent(svcDelClearButton)
                 .addContainerGap(243, Short.MAX_VALUE))
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcDelNumLabel)
                     .addComponent(svcDelNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(svcDelSearchButton))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcDelDescLabel)
                     .addComponent(svcDelDescText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcDelFeeLabel)
                     .addComponent(svcDelFeeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcDeleteButton)
                     .addComponent(svcDelClearButton))
                 .addContainerGap())
         );
 
         svcDataManagerPanels.addTab("Delete", jPanel10);
 
         javax.swing.GroupLayout ServiceCodeTabbedPanelLayout = new javax.swing.GroupLayout(ServiceCodeTabbedPanel);
         ServiceCodeTabbedPanel.setLayout(ServiceCodeTabbedPanelLayout);
         ServiceCodeTabbedPanelLayout.setHorizontalGroup(
             ServiceCodeTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ServiceCodeTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(svcDataManagerPanels, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                 .addContainerGap())
         );
         ServiceCodeTabbedPanelLayout.setVerticalGroup(
             ServiceCodeTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ServiceCodeTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(svcDataManagerPanels, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(47, Short.MAX_VALUE))
         );
 
         ChocAnPanel.addTab("Service Code", ServiceCodeTabbedPanel);
 
         memberRptButton.setText("Run Member Report");
         memberRptButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 memberRptButtonActionPerformed(evt);
             }
         });
 
         eftRptButton.setText("Run EFT Report");
         eftRptButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eftRptButtonActionPerformed(evt);
             }
         });
 
         summaryRptButton.setText("Run Summary Report");
         summaryRptButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 summaryRptButtonActionPerformed(evt);
             }
         });
 
         providerRptButton.setText("Run Provider Report");
         providerRptButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 providerRptButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout ReportsTabbedPanelLayout = new javax.swing.GroupLayout(ReportsTabbedPanel);
         ReportsTabbedPanel.setLayout(ReportsTabbedPanelLayout);
         ReportsTabbedPanelLayout.setHorizontalGroup(
             ReportsTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ReportsTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(ReportsTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(memberRptButton)
                     .addComponent(providerRptButton))
                 .addGap(51, 51, 51)
                 .addGroup(ReportsTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(eftRptButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(summaryRptButton, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                 .addContainerGap(148, Short.MAX_VALUE))
         );
         ReportsTabbedPanelLayout.setVerticalGroup(
             ReportsTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ReportsTabbedPanelLayout.createSequentialGroup()
                 .addGap(39, 39, 39)
                 .addGroup(ReportsTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memberRptButton)
                     .addComponent(eftRptButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(ReportsTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(providerRptButton)
                     .addComponent(summaryRptButton))
                 .addContainerGap(195, Short.MAX_VALUE))
         );
 
         ChocAnPanel.addTab("Reports", ReportsTabbedPanel);
 
         terminalDataManagerPanels.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 terminalManagerPanelsMouseClick(evt);
             }
         });
 
         valMemLabel.setText("Member Number:");
 
         valMemButton.setText("Validate");
         valMemButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 valMemButtonActionPerformed(evt);
             }
         });
 
         valMemClearButton.setText("Clear");
         valMemClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 valMemClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout valMemPanelLayout = new javax.swing.GroupLayout(valMemPanel);
         valMemPanel.setLayout(valMemPanelLayout);
         valMemPanelLayout.setHorizontalGroup(
             valMemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(valMemPanelLayout.createSequentialGroup()
                 .addGroup(valMemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(valMemPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(valMemLabel))
                     .addGroup(valMemPanelLayout.createSequentialGroup()
                         .addGap(90, 90, 90)
                         .addComponent(valMemButton)))
                 .addGroup(valMemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(valMemPanelLayout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(valMemText, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(valMemPanelLayout.createSequentialGroup()
                         .addGap(118, 118, 118)
                         .addComponent(valMemClearButton)))
                 .addContainerGap(169, Short.MAX_VALUE))
         );
         valMemPanelLayout.setVerticalGroup(
             valMemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(valMemPanelLayout.createSequentialGroup()
                 .addGap(45, 45, 45)
                 .addGroup(valMemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(valMemLabel)
                     .addComponent(valMemText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                 .addGroup(valMemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(valMemButton)
                     .addComponent(valMemClearButton))
                 .addGap(40, 40, 40))
         );
 
         terminalDataManagerPanels.addTab("Val Member", valMemPanel);
 
         valProLabel.setText("Provider Number:");
 
         valProButton.setText("Validate");
         valProButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 valProButtonActionPerformed(evt);
             }
         });
 
         valProClearButton.setText("Clear");
         valProClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 valProClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout valProPanelLayout = new javax.swing.GroupLayout(valProPanel);
         valProPanel.setLayout(valProPanelLayout);
         valProPanelLayout.setHorizontalGroup(
             valProPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(valProPanelLayout.createSequentialGroup()
                 .addGroup(valProPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(valProPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(valProLabel))
                     .addGroup(valProPanelLayout.createSequentialGroup()
                         .addGap(89, 89, 89)
                         .addComponent(valProButton)))
                 .addGroup(valProPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(valProPanelLayout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(valProText, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap(232, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, valProPanelLayout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(valProClearButton)
                         .addGap(110, 110, 110))))
         );
         valProPanelLayout.setVerticalGroup(
             valProPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(valProPanelLayout.createSequentialGroup()
                 .addGap(45, 45, 45)
                 .addGroup(valProPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(valProLabel)
                     .addComponent(valProText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                 .addGroup(valProPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(valProButton)
                     .addComponent(valProClearButton))
                 .addGap(40, 40, 40))
         );
 
         terminalDataManagerPanels.addTab("Val Provider", valProPanel);
 
         valSvcLabel.setText("Service Code:");
 
         valSvcButton.setText("Validate");
         valSvcButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 valSvcButtonActionPerformed(evt);
             }
         });
 
         valSvcClearButton.setText("Clear");
         valSvcClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 valSvcClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout valSvcPanelLayout = new javax.swing.GroupLayout(valSvcPanel);
         valSvcPanel.setLayout(valSvcPanelLayout);
         valSvcPanelLayout.setHorizontalGroup(
             valSvcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(valSvcPanelLayout.createSequentialGroup()
                 .addGroup(valSvcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(valSvcPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(valSvcLabel)
                         .addGap(18, 18, 18)
                         .addComponent(valSvcText, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(valSvcPanelLayout.createSequentialGroup()
                         .addGap(74, 74, 74)
                         .addComponent(valSvcButton)
                         .addGap(117, 117, 117)
                         .addComponent(valSvcClearButton)))
                 .addContainerGap(186, Short.MAX_VALUE))
         );
         valSvcPanelLayout.setVerticalGroup(
             valSvcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(valSvcPanelLayout.createSequentialGroup()
                 .addGap(45, 45, 45)
                 .addGroup(valSvcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(valSvcLabel)
                     .addComponent(valSvcText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                 .addGroup(valSvcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(valSvcButton)
                     .addComponent(valSvcClearButton))
                 .addGap(41, 41, 41))
         );
 
         terminalDataManagerPanels.addTab("Val Svc Code", valSvcPanel);
 
         runProDirButton.setText("Run Provider Directory");
         runProDirButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 runProDirButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout proDirPanelLayout = new javax.swing.GroupLayout(proDirPanel);
         proDirPanel.setLayout(proDirPanelLayout);
         proDirPanelLayout.setHorizontalGroup(
             proDirPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(proDirPanelLayout.createSequentialGroup()
                 .addGap(126, 126, 126)
                 .addComponent(runProDirButton)
                 .addContainerGap(198, Short.MAX_VALUE))
         );
         proDirPanelLayout.setVerticalGroup(
             proDirPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(proDirPanelLayout.createSequentialGroup()
                 .addGap(69, 69, 69)
                 .addComponent(runProDirButton)
                 .addContainerGap(145, Short.MAX_VALUE))
         );
 
         terminalDataManagerPanels.addTab("Provider Dir", proDirPanel);
 
         dateLabel.setText("Service date:");
 
         memNumLabel.setText("Member #:");
 
         proNumLabel.setText("Provider #:");
 
         svcLabel.setText("Service Code:");
 
         commentLabel.setText("Comments:");
 
         addBillButton.setText("Add Bill");
         addBillButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addBillButtonActionPerformed(evt);
             }
         });
 
         billClearButton.setText("Clear");
         billClearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 billClearButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout addBillPanelLayout = new javax.swing.GroupLayout(addBillPanel);
         addBillPanel.setLayout(addBillPanelLayout);
         addBillPanelLayout.setHorizontalGroup(
             addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(addBillPanelLayout.createSequentialGroup()
                 .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(addBillPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(addBillPanelLayout.createSequentialGroup()
                                 .addComponent(dateLabel)
                                 .addGap(18, 18, 18)
                                 .addComponent(billDateText, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(memNumLabel)))
                     .addGroup(addBillPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addBillPanelLayout.createSequentialGroup()
                                 .addComponent(commentLabel)
                                 .addGap(27, 27, 27)
                                 .addComponent(billCommentText, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addBillPanelLayout.createSequentialGroup()
                                 .addComponent(svcLabel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(addBillPanelLayout.createSequentialGroup()
                                         .addComponent(billMemNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(proNumLabel)
                                         .addGap(18, 18, 18)
                                         .addComponent(billProNumText, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addComponent(billSvcText, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                     .addGroup(addBillPanelLayout.createSequentialGroup()
                         .addGap(78, 78, 78)
                         .addComponent(addBillButton)
                         .addGap(137, 137, 137)
                         .addComponent(billClearButton)))
                 .addContainerGap())
         );
         addBillPanelLayout.setVerticalGroup(
             addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(addBillPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(dateLabel)
                     .addComponent(billDateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(memNumLabel)
                     .addComponent(billMemNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(proNumLabel)
                     .addComponent(billProNumText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(svcLabel)
                     .addComponent(billSvcText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(commentLabel)
                     .addComponent(billCommentText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                 .addGroup(addBillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(addBillButton)
                     .addComponent(billClearButton))
                 .addContainerGap())
         );
 
         terminalDataManagerPanels.addTab("Bill", addBillPanel);
 
         javax.swing.GroupLayout TerminalTabbedPanelLayout = new javax.swing.GroupLayout(TerminalTabbedPanel);
         TerminalTabbedPanel.setLayout(TerminalTabbedPanelLayout);
         TerminalTabbedPanelLayout.setHorizontalGroup(
             TerminalTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(TerminalTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(terminalDataManagerPanels, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                 .addContainerGap())
         );
         TerminalTabbedPanelLayout.setVerticalGroup(
             TerminalTabbedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(TerminalTabbedPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(terminalDataManagerPanels)
                 .addContainerGap())
         );
 
         ChocAnPanel.addTab("Terminal", TerminalTabbedPanel);
 
         exitButton.setText("Write and Exit");
         exitButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exitButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(exitButton)
                         .addGap(212, 212, 212))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(globalMessageLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                             .addComponent(ChocAnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE))
                         .addContainerGap())))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(ChocAnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(globalMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                 .addGap(40, 40, 40)
                 .addComponent(exitButton)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void svcUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcUpdateButtonActionPerformed
         ServiceCode s = null;
         try {
             validateString("Description", svcUpDescText.getText(), 20);
             validateFloat("Fee", svcUpFeeText.getText(), 5);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(svcUpNumText.getText().trim());
         s = (ServiceCode) svcMan.search(i);
         if (s == null) {
             globalMessageLabel.setText("Service Code not found update not completed.");
         } else {
             s.setDescription(svcUpDescText.getText());
 
             String wrk = svcUpFeeText.getText();
             float f = Float.valueOf(wrk.trim()).floatValue();
             s.setFee(f);
             globalMessageLabel.setText("Service Code updated.");
         }
         svcUpNumText.setEnabled(true);
 }//GEN-LAST:event_svcUpdateButtonActionPerformed
 
     private void svcAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcAddButtonActionPerformed
         try {
             validateString("Description", svcAddDescText.getText(), 20);
             validateFloat("Fee", svcAddFeeText.getText(), 5);
         } catch (FieldException ex) {
             return;
         }
         
         ServiceCode tempSvc = new ServiceCode();
 
         tempSvc.setDescription(svcAddDescText.getText());
         String wrk = svcAddFeeText.getText();
         float f = Float.valueOf(wrk.trim()).floatValue();
         tempSvc.setFee(f);
         svcMan.addData(tempSvc);
         globalMessageLabel.setText("Service Code " + tempSvc.getNumber() +
                                    " has been added");
     }//GEN-LAST:event_svcAddButtonActionPerformed
 
     private void svcUpSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcUpSearchButtonActionPerformed
         ServiceCode s = null;
         try {
             validateInt("Service Code", svcUpNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(svcUpNumText.getText().trim());
         s = (ServiceCode) svcMan.search(i);
         if (s == null) {
             globalMessageLabel.setText("Service Code not found.");
         } else {
             if (s.getStatus() == 0) {
                 globalMessageLabel.setText("Service Code invalid.");
             } else {
                 svcUpDescText.setText(s.getDescription());
                 svcUpFeeText.setText(feeFormat.format(s.getFee()));
                 globalMessageLabel.setText("Service Code ready for update.");
                 svcUpNumText.setEnabled(false);
                 svcUpSearchButton.setEnabled(false);
             }
         }
         
     }//GEN-LAST:event_svcUpSearchButtonActionPerformed
 
     private void svcUpdateClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcUpdateClearButtonActionPerformed
         svcUpDescText.setText("");
         svcUpFeeText.setText("");
         svcUpNumText.setText("");
         svcUpNumText.setEnabled(true);
         svcUpSearchButton.setEnabled(true);
         globalMessageLabel.setText("");
     }//GEN-LAST:event_svcUpdateClearButtonActionPerformed
 
     private void svcAddClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcAddClearButtonActionPerformed
         svcAddDescText.setText("");
         svcAddFeeText.setText("");
         globalMessageLabel.setText("");
     }//GEN-LAST:event_svcAddClearButtonActionPerformed
 
     private void svcDelSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcDelSearchButtonActionPerformed
         ServiceCode s = null;
         try {
             validateInt("Service Code", svcDelNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(svcDelNumText.getText().trim());
         s = (ServiceCode) svcMan.search(i);
         if (s == null) {
             globalMessageLabel.setText("Service Code not found.");
         } else {
             if (s.getStatus() == 0) {
                 globalMessageLabel.setText("Service Code invalid.");
             } else {
                 svcUpDescText.setText(s.getDescription());
                 svcUpFeeText.setText(feeFormat.format(s.getFee()));
                 globalMessageLabel.setText("Service Code ready for update.");
                 svcUpNumText.setEnabled(false);
                 svcUpSearchButton.setEnabled(false);
             }
         }
     }//GEN-LAST:event_svcDelSearchButtonActionPerformed
 
     private void svcDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcDeleteButtonActionPerformed
         try {
             validateInt("Service Code", svcDelNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         ServiceCode s = null;
         int i = Integer.parseInt(svcDelNumText.getText().trim());
         s = (ServiceCode) svcMan.search(i);
         if (s == null) {
             globalMessageLabel.setText("Service Code not found.");
         } else {
             s.setStatus(0);
             globalMessageLabel.setText("Service Code disabled.");
             svcDelNumText.setEnabled(false);
             svcDelSearchButton.setEnabled(false);
         }
     }//GEN-LAST:event_svcDeleteButtonActionPerformed
 
     private void svcDelClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svcDelClearButtonActionPerformed
         svcDelDescText.setText("");
         svcDelFeeText.setText("");
         svcDelNumText.setText("");
         svcDelNumText.setEnabled(true);
         svcDelSearchButton.setEnabled(true);
         globalMessageLabel.setText("");
     }//GEN-LAST:event_svcDelClearButtonActionPerformed
 
     private void memberUpSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberUpSearchButtonActionPerformed
         Member m = null;
 
         try {
             validateInt("Member number", memberUpNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(memberUpNumText.getText().trim());
         m = (Member) memberMan.search(i);
         if (m == null) {
             globalMessageLabel.setText("Member not found.");
         } else {
             if (m.getStatus() == 0 || m.getStatus() == 2 ) {
                 globalMessageLabel.setText("Member invalid.");
             } else {
                 memberUpNameText.setText(m.getName());
                 memberUpAddressText.setText(m.getAddress());
                 memberUpCityText.setText(m.getCity());
                 memberUpStateText.setText(m.getState());
                 memberUpZipText.setText(String.valueOf(m.getZip()));
                 memberUpNumText.setEnabled(false);
                 memberUpSearchButton.setEnabled(false);
                 globalMessageLabel.setText("Member ready to update.");
             }
 
         }
 }//GEN-LAST:event_memberUpSearchButtonActionPerformed
 
     private void memberUpClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberUpClearButtonActionPerformed
         memberUpNameText.setText("");
         memberUpAddressText.setText("");
         memberUpCityText.setText("");
         memberUpStateText.setText("");
         memberUpZipText.setText("");
         memberUpNumText.setText("");
         memberUpNumText.setEnabled(true);
         memberUpSearchButton.setEnabled(true);
         globalMessageLabel.setText("");
 }//GEN-LAST:event_memberUpClearButtonActionPerformed
 
     private void memberUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberUpdateButtonActionPerformed
         Member m = null;
         try {
             validateString("Name", memberUpNameText.getText(), 25);
             validateString("Address", memberUpAddressText.getText(), 25);
             validateString("City", memberUpCityText.getText(), 14);
             validateString("State", memberUpStateText.getText(), 2);
             validateInt("Zip", memberUpZipText.getText(), 5);
         } catch (FieldException ex) {
             return;
         }
 
 
         int i = Integer.parseInt(memberUpNumText.getText().trim());
         m = (Member) memberMan.search(i);
         if (m == null) {
             globalMessageLabel.setText("Member not found.");
         } else {
             m.setName(memberUpNameText.getText());
             m.setAddress(memberUpAddressText.getText());
             m.setCity(memberUpCityText.getText());
             m.setState(memberUpStateText.getText());
             int k = Integer.parseInt(memberUpZipText.getText().trim());
             m.setZip(k);
             memberUpNumText.setEnabled(false);
             memberUpSearchButton.setEnabled(false);
             globalMessageLabel.setText("Member has been updated.");
 
         }
 }//GEN-LAST:event_memberUpdateButtonActionPerformed
 
     private void memberAddClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberAddClearButtonActionPerformed
         memberAddNameText.setText("");
         memberAddAddressText.setText("");
         memberAddCityText.setText("");
         memberAddStateText.setText("");
         memberAddZipText.setText("");
         globalMessageLabel.setText("");
 }//GEN-LAST:event_memberAddClearButtonActionPerformed
 
     private void memberAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberAddButtonActionPerformed
 
 
         Member m = new Member();
         try {
             validateString("Name", memberAddNameText.getText(), 25);
             validateString("Address", memberAddAddressText.getText(), 25);
             validateString("City", memberAddCityText.getText(), 14);
             validateString("State", memberAddStateText.getText(), 2);
             validateInt("Zip", memberAddZipText.getText(), 5);
         } catch (FieldException ex) {
             return;
         }
 
         m.setName(memberAddNameText.getText());
         m.setAddress(memberAddAddressText.getText());
         m.setCity(memberAddCityText.getText());
         m.setState(memberAddStateText.getText());
         int i = Integer.parseInt(memberAddZipText.getText().trim());
         m.setZip(i);
         memberMan.addData(m);
         globalMessageLabel.setText("Member " + m.getNumber() +
                 " has been added");
 }//GEN-LAST:event_memberAddButtonActionPerformed
 
     private void memberDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberDeleteButtonActionPerformed
         Member m = null;
 
         try {
             validateInt("Member number", memberDelNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(memberDelNumText.getText().trim());
         m = (Member) memberMan.search(i);
         if (m == null) {
             globalMessageLabel.setText("Member not found.");
         } else {
             m.setStatus(0);
             memberUpNumText.setEnabled(false);
             memberUpSearchButton.setEnabled(false);
             globalMessageLabel.setText("Member has been disabled.");
 
         }
     }//GEN-LAST:event_memberDeleteButtonActionPerformed
 
     private void memberDelClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberDelClearButtonActionPerformed
         memberDelNameText.setText("");
         memberDelAddressText.setText("");
         memberDelCityText.setText("");
         memberDelStateText.setText("");
         memberDelZipText.setText("");
         memberDelNumText.setText("");
         memberDelNumText.setEnabled(true);
         memberDelSearchButton.setEnabled(true);
         globalMessageLabel.setText("");
     }//GEN-LAST:event_memberDelClearButtonActionPerformed
 
     private void memberDelSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberDelSearchButtonActionPerformed
         Member m = null;
 
         try {
             validateInt("Member number", memberDelNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(memberDelNumText.getText().trim());
         m = (Member) memberMan.search(i);
         if (m == null) {
             globalMessageLabel.setText("Member not found.");
         } else {
             if (m.getStatus() == 0 || m.getStatus() == 2) {
                 globalMessageLabel.setText("Member invalid.");
             } else {
                 memberDelNameText.setText(m.getName());
                 memberDelAddressText.setText(m.getAddress());
                 memberDelCityText.setText(m.getCity());
                 memberDelStateText.setText(m.getState());
                 memberDelZipText.setText(String.valueOf(m.getZip()));
                 memberDelNumText.setEnabled(false);
                 memberDelSearchButton.setEnabled(false);
                 globalMessageLabel.setText("Member has been found.");
            }
         }
     }//GEN-LAST:event_memberDelSearchButtonActionPerformed
 
     private void memberRptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberRptButtonActionPerformed
         MemberReport r = new MemberReport();
         if (r.createReport()) {
             globalMessageLabel.setText("Member reports are available.");
         } else {
             globalMessageLabel.setText("Member reports could not be run.");
         }
     }//GEN-LAST:event_memberRptButtonActionPerformed
 
     private void providerRptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerRptButtonActionPerformed
         ProviderReport r = new ProviderReport();
         if (r.createReport()) {
             globalMessageLabel.setText("Provider reports are available.");
         } else {
             globalMessageLabel.setText("Provider reports could not be run.");
         }
     }//GEN-LAST:event_providerRptButtonActionPerformed
 
     private void eftRptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eftRptButtonActionPerformed
         EFTReport r = new EFTReport();
         if (r.createReport()) {
             globalMessageLabel.setText("EFT reports are available.");
         } else {
             globalMessageLabel.setText("EFT reports could not be run.");
         }
     }//GEN-LAST:event_eftRptButtonActionPerformed
 
     private void summaryRptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryRptButtonActionPerformed
         SummaryReport r = new SummaryReport();
         if (r.createReport()) {
             globalMessageLabel.setText("Summary reports are available.");
         } else {
             globalMessageLabel.setText("Summary reports could not be run.");
         }
     }//GEN-LAST:event_summaryRptButtonActionPerformed
 
     private void providerAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerAddButtonActionPerformed
         Provider p = new Provider();
         
         try {
             validateString("Name", providerAddNameText.getText(), 25);
             validateString("Address", providerAddAddressText.getText(), 25);
             validateString("City", providerAddCityText.getText(), 14);
             validateString("State", providerAddStateText.getText(), 2);
             validateInt("Zip", providerAddZipText.getText(), 5);
         } catch (FieldException ex) {
             return;
         }
         
         p.setName(providerAddNameText.getText());
         p.setAddress(providerAddAddressText.getText());
         p.setCity(providerAddCityText.getText());
         p.setState(providerAddStateText.getText());
         int i = Integer.parseInt(providerAddZipText.getText().trim());
         p.setZip(i);
         providerMan.addData(p);
         globalMessageLabel.setText("Provider " + p.getNumber() +
                 " has been added");
     }//GEN-LAST:event_providerAddButtonActionPerformed
 
     private void providerAddClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerAddClearButtonActionPerformed
         providerAddNameText.setText("");
         providerAddAddressText.setText("");
         providerAddCityText.setText("");
         providerAddStateText.setText("");
         providerAddZipText.setText("");
         globalMessageLabel.setText("");
     }//GEN-LAST:event_providerAddClearButtonActionPerformed
 
     private void providerUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerUpdateButtonActionPerformed
         Provider p = null;
 
         try {
             validateString("Name", providerUpNameText.getText(), 25);
             validateString("Address", providerUpAddressText.getText(), 25);
             validateString("City", providerUpCityText.getText(), 14);
             validateString("State", providerUpStateText.getText(), 2);
             validateInt("Zip", providerUpZipText.getText(), 5);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(providerUpNumText.getText().trim());
         p = (Provider) providerMan.search(i);
         if (p == null) {
             globalMessageLabel.setText("Provider not found.");
         } else {
             p.setName(providerUpNameText.getText());
             p.setAddress(providerUpAddressText.getText());
             p.setCity(providerUpCityText.getText());
             p.setState(providerUpStateText.getText());
             int k = Integer.parseInt(providerUpZipText.getText().trim());
             p.setZip(k);
             providerUpNumText.setEnabled(false);
             providerUpSearchButton.setEnabled(false);
             globalMessageLabel.setText("Provider has been updated.");
 
         }
     }//GEN-LAST:event_providerUpdateButtonActionPerformed
 
     private void providerUpClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerUpClearButtonActionPerformed
         providerUpNameText.setText("");
         providerUpAddressText.setText("");
         providerUpCityText.setText("");
         providerUpStateText.setText("");
         providerUpZipText.setText("");
         providerUpNumText.setText("");
         providerUpNumText.setEnabled(true);
         providerUpSearchButton.setEnabled(true);
         globalMessageLabel.setText("");
     }//GEN-LAST:event_providerUpClearButtonActionPerformed
 
     private void providerUpSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerUpSearchButtonActionPerformed
         Provider p = null;
 
         try {
             validateInt("Provider number", providerUpNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(providerUpNumText.getText().trim());
         p = (Provider) providerMan.search(i);
         if (p == null) {
             globalMessageLabel.setText("Provider not found.");
         } else {
             if (p.getStatus() == 0) {
                 globalMessageLabel.setText("Provider invalid.");
             } else {
                 providerUpNameText.setText(p.getName());
                 providerUpAddressText.setText(p.getAddress());
                 providerUpCityText.setText(p.getCity());
                 providerUpStateText.setText(p.getState());
                 providerUpZipText.setText(String.valueOf(p.getZip()));
                 providerUpNumText.setEnabled(false);
                 providerUpSearchButton.setEnabled(false);
                 globalMessageLabel.setText("Provider ready to update.");
             }
         }
     }//GEN-LAST:event_providerUpSearchButtonActionPerformed
 
     private void providerDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerDeleteButtonActionPerformed
         Provider p = null;
 
         try {
             validateInt("Provider number", providerDelNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(providerDelNumText.getText().trim());
         p = (Provider) providerMan.search(i);
         if (p == null) {
             globalMessageLabel.setText("Provider not found.");
         } else {
             p.setStatus(0);
             providerUpNumText.setEnabled(false);
             providerUpSearchButton.setEnabled(false);
             globalMessageLabel.setText("Provider has been disabled.");
         }
     }//GEN-LAST:event_providerDeleteButtonActionPerformed
 
     private void providerDelClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerDelClearButtonActionPerformed
         providerDelNameText.setText("");
         providerDelAddressText.setText("");
         providerDelCityText.setText("");
         providerDelStateText.setText("");
         providerDelZipText.setText("");
         providerDelNumText.setText("");
         providerDelNumText.setEnabled(true);
         providerDelSearchButton.setEnabled(true);
         globalMessageLabel.setText("");
     }//GEN-LAST:event_providerDelClearButtonActionPerformed
 
     private void providerDelSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerDelSearchButtonActionPerformed
         Provider p = null;
 
         try {
             validateInt("Provider number", providerDelNumText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(providerDelNumText.getText().trim());
         p = (Provider) providerMan.search(i);
         if (p == null) {
             globalMessageLabel.setText("Provider not found.");
         } else {
             if (p.getStatus() == 0) {
                 globalMessageLabel.setText("Provider invalid.");
             } else {
                 providerDelNameText.setText(p.getName());
                 providerDelAddressText.setText(p.getAddress());
                 providerDelCityText.setText(p.getCity());
                 providerDelStateText.setText(p.getState());
                 providerDelZipText.setText(String.valueOf(p.getZip()));
                 providerDelNumText.setEnabled(false);
                 providerDelSearchButton.setEnabled(false);
                 globalMessageLabel.setText("Provider has been found.");
             }
         }
     }//GEN-LAST:event_providerDelSearchButtonActionPerformed
 
     private void valMemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valMemButtonActionPerformed
         Member m = null;
 
         try {
             validateInt("Member number", valMemText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(valMemText.getText().trim());
 
         TerminalSimulator ts = new TerminalSimulator();
         int rc = (int) ts.validateMember(i);
 
         switch(rc) {
             case 0:
                 globalMessageLabel.setText("Invalid Member");
                 break;
             case 1:
                 globalMessageLabel.setText("Validated");
                 break;
             case 2:
                 globalMessageLabel.setText("Member Suspended");
                 break;
             default:
                 globalMessageLabel.setText("Member not found");
                 break;
         }
 
     }//GEN-LAST:event_valMemButtonActionPerformed
 
     private void valMemClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valMemClearButtonActionPerformed
         valMemText.setText("");
         globalMessageLabel.setText("");
     }//GEN-LAST:event_valMemClearButtonActionPerformed
 
     private void valProButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valProButtonActionPerformed
         Provider p = null;
 
         try {
             validateInt("Provider number", valProText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(valProText.getText().trim());
 
         TerminalSimulator ts = new TerminalSimulator();
         int rc = (int) ts.validateProvider(i);
 
         switch(rc) {
             case 0:
                 globalMessageLabel.setText("Invalid Provider");
                 break;
             case 1:
                 globalMessageLabel.setText("Validated");
                 break;
             default:
                 globalMessageLabel.setText("Provider not found");
                 break;
         }
 
     }//GEN-LAST:event_valProButtonActionPerformed
 
     private void valProClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valProClearButtonActionPerformed
         valProText.setText("");
         globalMessageLabel.setText("");
     }//GEN-LAST:event_valProClearButtonActionPerformed
 
     private void valSvcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valSvcButtonActionPerformed
         ServiceCode s = null;
 
         try {
             validateInt("Service code", valSvcText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         int i = Integer.parseInt(String.valueOf(valSvcText.getText()).trim());
 
         TerminalSimulator ts = new TerminalSimulator();
         int rc = (int) ts.validateServiceCode(i);
 
         if (rc == -1) {
             globalMessageLabel.setText("Service Code not found");
             return;
         } else {
             if ( rc == 0 ) {
                 globalMessageLabel.setText("Invalid Service Code");
                 return;
             } else {
                 globalMessageLabel.setText("Fee: " + feeFormat.format(rc));
                 return;
             }
         }
     }//GEN-LAST:event_valSvcButtonActionPerformed
 
     private void valSvcClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valSvcClearButtonActionPerformed
         valSvcText.setText("");
         globalMessageLabel.setText("");
     }//GEN-LAST:event_valSvcClearButtonActionPerformed
 
     private void addBillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBillButtonActionPerformed
 
         Bill b = new Bill();
 
         try {
             validateInt("Member number", billMemNumText.getText(), 9);
             validateInt("Provider number", billProNumText.getText(), 9);
             validateInt("Service Code", billSvcText.getText(), 9);
         } catch (FieldException ex) {
             return;
         }
 
         if (billCommentText.getText().length() > 100) {
             globalMessageLabel.setText("Comment too long.");
             return;
         }
 
         int i = Integer.parseInt(billMemNumText.getText().trim());
         Member m = (Member) memberMan.search(i);
         if (m == null) {
             globalMessageLabel.setText("Member not found.");
             return;
         }
 
         i = Integer.parseInt(billProNumText.getText().trim());
         Provider p = (Provider) providerMan.search(i);
         if (p == null) {
             globalMessageLabel.setText("Provider not found.");
             return;
         }
 
         i = Integer.parseInt(billSvcText.getText().trim());
         ServiceCode s = (ServiceCode) svcMan.search(i);
         if (s == null) {
             globalMessageLabel.setText("Service code not found.");
             return;
         }
 
        DateFormat dfServiceDate = new SimpleDateFormat("mm/dd/yyyy");
         try {
             Date svcDate = dfServiceDate.parse(billDateText.getText());
             b.setServiceDate(svcDate);
         } catch (ParseException ex) {
             globalMessageLabel.setText("Invalid service date(mm/dd/yyyy), bill not added.");
             return;
         }
 
         i = Integer.parseInt(billMemNumText.getText().trim());
         b.setMemberNumber(i);
         i = Integer.parseInt(billProNumText.getText().trim());
         b.setProviderNumber(i);
         i = Integer.parseInt(billSvcText.getText().trim());
         b.setServiceCode(i);
         b.setFee(s.getFee());
         b.setComment(billCommentText.getText());
         billMan.addData(b);
         globalMessageLabel.setText("Bill has been added and the fee is "
                                     + feeFormat.format(s.getFee()));
     }//GEN-LAST:event_addBillButtonActionPerformed
 
     private void billClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billClearButtonActionPerformed
         billDateText.setText("");
         billMemNumText.setText("");
         billProNumText.setText("");
         billSvcText.setText("");
         billCommentText.setText("");
         globalMessageLabel.setText("");
     }//GEN-LAST:event_billClearButtonActionPerformed
 
     private void runProDirButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runProDirButtonActionPerformed
         TerminalSimulator ts = new TerminalSimulator();
         if (ts.requestServiceDirectory()) {
             globalMessageLabel.setText("Service code directory now available.");
         } else {
             globalMessageLabel.setText("Service code directory could not be run.");
         }
     }//GEN-LAST:event_runProDirButtonActionPerformed
 
     private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
         try {
             Id.singletonId(chocAnId).writeFile();
             billMan.writeFile();
             memberMan.writeFile();
             providerMan.writeFile();
             svcMan.writeFile();
 
         } catch (IOException ex) {
             System.out.println("Error encountered while writing files.");
         }
         System.exit(1);
     }//GEN-LAST:event_exitButtonActionPerformed
 
     private void chocAnPanelMouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chocAnPanelMouseClick
         globalMessageLabel.setText(" ");
     }//GEN-LAST:event_chocAnPanelMouseClick
 
     private void dataManagerPanelsMouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataManagerPanelsMouseClick
         globalMessageLabel.setText(" ");
     }//GEN-LAST:event_dataManagerPanelsMouseClick
 
     private void providerManagerPanelsMouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_providerManagerPanelsMouseClick
         globalMessageLabel.setText(" ");
     }//GEN-LAST:event_providerManagerPanelsMouseClick
 
     private void svcCodeManagerPanelsMouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_svcCodeManagerPanelsMouseClick
         globalMessageLabel.setText(" ");
     }//GEN-LAST:event_svcCodeManagerPanelsMouseClick
 
     private void terminalManagerPanelsMouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_terminalManagerPanelsMouseClick
         globalMessageLabel.setText(" ");
     }//GEN-LAST:event_terminalManagerPanelsMouseClick
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
   
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 
                 new ChocAnUserInterface().setVisible(true);
             }
         });
 
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTabbedPane ChocAnPanel;
     private javax.swing.JPanel MemberTabbedPanel;
     private javax.swing.JTabbedPane ProviderDataManagerPanels;
     private javax.swing.JPanel ProviderTabbedPanel;
     private javax.swing.JPanel ReportsTabbedPanel;
     private javax.swing.JPanel ServiceCodeTabbedPanel;
     private javax.swing.JPanel TerminalTabbedPanel;
     private javax.swing.JButton addBillButton;
     private javax.swing.JPanel addBillPanel;
     private javax.swing.JButton billClearButton;
     private javax.swing.JTextField billCommentText;
     private javax.swing.JTextField billDateText;
     private javax.swing.JTextField billMemNumText;
     private javax.swing.JTextField billProNumText;
     private javax.swing.JTextField billSvcText;
     private javax.swing.JLabel commentLabel;
     private javax.swing.JLabel dateLabel;
     private javax.swing.JLabel descLabel;
     private javax.swing.JButton eftRptButton;
     private javax.swing.JButton exitButton;
     private javax.swing.JLabel feeLabel;
     private javax.swing.JLabel globalMessageLabel;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JLabel memNumLabel;
     private javax.swing.JLabel memberAddAddressLabel;
     private javax.swing.JTextField memberAddAddressText;
     private javax.swing.JButton memberAddButton;
     private javax.swing.JLabel memberAddCityLabel;
     private javax.swing.JTextField memberAddCityText;
     private javax.swing.JButton memberAddClearButton;
     private javax.swing.JLabel memberAddNameLabel;
     private javax.swing.JTextField memberAddNameText;
     private javax.swing.JPanel memberAddPanel;
     private javax.swing.JLabel memberAddStateLabel;
     private javax.swing.JTextField memberAddStateText;
     private javax.swing.JLabel memberAddZipLabel;
     private javax.swing.JTextField memberAddZipText;
     private javax.swing.JTabbedPane memberDataManagerPanels;
     private javax.swing.JLabel memberDelAddressLabel;
     private javax.swing.JTextField memberDelAddressText;
     private javax.swing.JLabel memberDelCityLabel;
     private javax.swing.JTextField memberDelCityText;
     private javax.swing.JButton memberDelClearButton;
     private javax.swing.JLabel memberDelNameLabel;
     private javax.swing.JTextField memberDelNameText;
     private javax.swing.JLabel memberDelNumLabel;
     private javax.swing.JTextField memberDelNumText;
     private javax.swing.JButton memberDelSearchButton;
     private javax.swing.JLabel memberDelStateLabel;
     private javax.swing.JTextField memberDelStateText;
     private javax.swing.JLabel memberDelZipLabel;
     private javax.swing.JTextField memberDelZipText;
     private javax.swing.JButton memberDeleteButton;
     private javax.swing.JPanel memberDeletePanel;
     private javax.swing.JButton memberRptButton;
     private javax.swing.JLabel memberUpAddressLabel;
     private javax.swing.JTextField memberUpAddressText;
     private javax.swing.JLabel memberUpCityLabel;
     private javax.swing.JTextField memberUpCityText;
     private javax.swing.JButton memberUpClearButton;
     private javax.swing.JLabel memberUpNameLabel;
     private javax.swing.JTextField memberUpNameText;
     private javax.swing.JLabel memberUpNumLabel;
     private javax.swing.JTextField memberUpNumText;
     private javax.swing.JButton memberUpSearchButton;
     private javax.swing.JLabel memberUpStateLabel;
     private javax.swing.JTextField memberUpStateText;
     private javax.swing.JLabel memberUpZipLabel;
     private javax.swing.JTextField memberUpZipText;
     private javax.swing.JButton memberUpdateButton;
     private javax.swing.JPanel memberUpdatePanel;
     private javax.swing.JPanel proDirPanel;
     private javax.swing.JLabel proNumLabel;
     private javax.swing.JLabel providerAddAddressLabel;
     private javax.swing.JTextField providerAddAddressText;
     private javax.swing.JButton providerAddButton;
     private javax.swing.JLabel providerAddCityLabel;
     private javax.swing.JTextField providerAddCityText;
     private javax.swing.JButton providerAddClearButton;
     private javax.swing.JLabel providerAddNameLabel;
     private javax.swing.JTextField providerAddNameText;
     private javax.swing.JPanel providerAddPanel;
     private javax.swing.JLabel providerAddStateLabel;
     private javax.swing.JTextField providerAddStateText;
     private javax.swing.JLabel providerAddZipLabel;
     private javax.swing.JTextField providerAddZipText;
     private javax.swing.JLabel providerDelAddressLabel;
     private javax.swing.JTextField providerDelAddressText;
     private javax.swing.JLabel providerDelCityLabel;
     private javax.swing.JTextField providerDelCityText;
     private javax.swing.JButton providerDelClearButton;
     private javax.swing.JLabel providerDelNameLabel;
     private javax.swing.JTextField providerDelNameText;
     private javax.swing.JLabel providerDelNumLabel;
     private javax.swing.JTextField providerDelNumText;
     private javax.swing.JButton providerDelSearchButton;
     private javax.swing.JLabel providerDelStateLabel;
     private javax.swing.JTextField providerDelStateText;
     private javax.swing.JLabel providerDelZipLabel;
     private javax.swing.JTextField providerDelZipText;
     private javax.swing.JButton providerDeleteButton;
     private javax.swing.JPanel providerDeletePanel;
     private javax.swing.JButton providerRptButton;
     private javax.swing.JLabel providerUpAddressLabel;
     private javax.swing.JTextField providerUpAddressText;
     private javax.swing.JLabel providerUpCityLabel;
     private javax.swing.JTextField providerUpCityText;
     private javax.swing.JButton providerUpClearButton;
     private javax.swing.JLabel providerUpNameLabel;
     private javax.swing.JTextField providerUpNameText;
     private javax.swing.JLabel providerUpNumLabel;
     private javax.swing.JTextField providerUpNumText;
     private javax.swing.JButton providerUpSearchButton;
     private javax.swing.JLabel providerUpStateLabel;
     private javax.swing.JTextField providerUpStateText;
     private javax.swing.JLabel providerUpZipLabel;
     private javax.swing.JTextField providerUpZipText;
     private javax.swing.JButton providerUpdateButton;
     private javax.swing.JPanel providerUpdatePanel;
     private javax.swing.JButton runProDirButton;
     private javax.swing.JButton summaryRptButton;
     private javax.swing.JButton svcAddButton;
     private javax.swing.JButton svcAddClearButton;
     private javax.swing.JTextField svcAddDescText;
     private javax.swing.JTextField svcAddFeeText;
     private javax.swing.JPanel svcAddPanel;
     private javax.swing.JTabbedPane svcDataManagerPanels;
     private javax.swing.JButton svcDelClearButton;
     private javax.swing.JLabel svcDelDescLabel;
     private javax.swing.JTextField svcDelDescText;
     private javax.swing.JLabel svcDelFeeLabel;
     private javax.swing.JTextField svcDelFeeText;
     private javax.swing.JLabel svcDelNumLabel;
     private javax.swing.JTextField svcDelNumText;
     private javax.swing.JButton svcDelSearchButton;
     private javax.swing.JButton svcDeleteButton;
     private javax.swing.JLabel svcLabel;
     private javax.swing.JLabel svcUpDescLabel;
     private javax.swing.JTextField svcUpDescText;
     private javax.swing.JLabel svcUpFeeLabel;
     private javax.swing.JTextField svcUpFeeText;
     private javax.swing.JLabel svcUpNumLabel;
     private javax.swing.JTextField svcUpNumText;
     private javax.swing.JButton svcUpSearchButton;
     private javax.swing.JButton svcUpdateButton;
     private javax.swing.JButton svcUpdateClearButton;
     private javax.swing.JPanel svcUpdatePanel;
     private javax.swing.JTabbedPane terminalDataManagerPanels;
     private javax.swing.JButton valMemButton;
     private javax.swing.JButton valMemClearButton;
     private javax.swing.JLabel valMemLabel;
     private javax.swing.JPanel valMemPanel;
     private javax.swing.JTextField valMemText;
     private javax.swing.JButton valProButton;
     private javax.swing.JButton valProClearButton;
     private javax.swing.JLabel valProLabel;
     private javax.swing.JPanel valProPanel;
     private javax.swing.JTextField valProText;
     private javax.swing.JButton valSvcButton;
     private javax.swing.JButton valSvcClearButton;
     private javax.swing.JLabel valSvcLabel;
     private javax.swing.JPanel valSvcPanel;
     private javax.swing.JTextField valSvcText;
     // End of variables declaration//GEN-END:variables
 
 }
