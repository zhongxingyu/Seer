 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations (FAO)
  * and the Lesotho Land Administration Authority (LAA). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the names of FAO, the LAA nor the names of its contributors may be used to
  *       endorse or promote products derived from this software without specific prior
  * 	  written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.clients.swing.ui.cadastre;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.JFormattedTextField;
 import org.sola.clients.beans.address.AddressBean;
 import org.sola.clients.beans.cadastre.CadastreObjectBean;
 import org.sola.clients.beans.party.PartySummaryListBean;
 import org.sola.clients.beans.referencedata.LandGradeTypeListBean;
 import org.sola.clients.beans.referencedata.RoadClassTypeListBean;
 import org.sola.clients.swing.ui.address.AddressDialog;
 import org.sola.clients.swing.ui.renderers.FormattersFactory;
 import org.sola.clients.swing.ui.renderers.SimpleComboBoxRenderer;
 import org.sola.common.WindowUtility;
 import org.sola.clients.swing.common.controls.CalendarForm;
 
 /**
  * Parcel panel to create and manage parcel objects
  */
 public class ParcelPanel extends javax.swing.JPanel {
 
     private CadastreObjectBean cadastreObjectBean;
     private boolean readOnly = false;
     private boolean lockCadastreFields = false;
     
     private RoadClassTypeListBean createRoadClassList(){
         return new RoadClassTypeListBean();
     }
     
     private LandGradeTypeListBean createLadGradeList(){
         return new LandGradeTypeListBean();
     }
     
     public ParcelPanel() {
         this(null, true);
     }
     
     public ParcelPanel(CadastreObjectBean cadastreObject, boolean readOnly) {
         this.readOnly = readOnly;
         if(cadastreObject==null){
             this.cadastreObjectBean = new CadastreObjectBean();
         } else {
             this.cadastreObjectBean = cadastreObject;
         }
         initComponents();
         postInit();
     }
 
      public CadastreObjectBean getCadastreObjectBean() {
         return cadastreObjectBean;
     }
 
     public void setCadastreObjectBean(CadastreObjectBean cadastreObjectBean) {
         CadastreObjectBean oldValue = this.cadastreObjectBean;
         if (cadastreObjectBean == null) {
             this.cadastreObjectBean = new CadastreObjectBean();
         } else {
             this.cadastreObjectBean = cadastreObjectBean;
         }
         firePropertyChange("cadastreObjectBean", oldValue, this.cadastreObjectBean);
         postInit();
     }
 
     public boolean isReadOnly() {
         return readOnly;
     }
 
     public void setReadOnly(boolean readOnly) {
         this.readOnly = readOnly;
         customizeForm();
     }
     
     private void postInit(){
         cadastreObjectBean.addPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if(evt.getPropertyName().equals(CadastreObjectBean.SELECTED_ADDRESS_PROPERTY)){
                     customizeAddressButtons();
                 }
             }
         });
         customizeAddressButtons();
         customizeForm();
     }
  
     private void customizeForm(){
         boolean enabled = !readOnly;
         boolean enabledAll = enabled && !lockCadastreFields;
         
         txtArea.setEnabled(enabledAll);
         cbxEstateType.setEnabled(enabledAll);
         txtSurveyDate.setEnabled(enabledAll);
         txtSurveyFee.setEnabled(enabledAll);
         cbxSurveyor.setEnabled(enabledAll);
         txtParcelSurveyRef.setEnabled(enabledAll);
         btnSurveyDate.setEnabled(enabledAll);
         txtRemarks.setEnabled(enabledAll);
         cbxLandGrade.setEnabled(enabled);
         cbxRoadClass.setEnabled(enabled);
         txtValuationAmount.setEnabled(enabled);
        txtLastPart.setEnabled(enabled && !cadastreObjectBean.isNew());
         
         customizeAddressButtons();
     }
     
     private void customizeAddressButtons(){
         boolean enabled = cadastreObjectBean.getSelectedAddress()!=null && 
                 !readOnly && !lockCadastreFields;
         btnAdd1.setEnabled(!readOnly && !lockCadastreFields);
         btnEdit1.setEnabled(enabled);
         btnRemove1.setEnabled(enabled);
         menuAdd1.setEnabled(btnAdd1.isEnabled());
         menuRemove1.setEnabled(enabled);
     }
     
     private void addAddress(){
         AddressDialog form = new AddressDialog(null, null, true);
         WindowUtility.centerForm(form);
         form.addPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if(evt.getPropertyName().equals(AddressDialog.ADDRESS_SAVED)){
                     cadastreObjectBean.addAddress((AddressBean)evt.getNewValue());
                 }
             }
         });
         form.setVisible(true);
     }
     
     private void editAddress(){
         if(cadastreObjectBean.getSelectedAddress()==null){
             return;
         }
         
         AddressDialog form = new AddressDialog(
                 (AddressBean)cadastreObjectBean.getSelectedAddress().copy(), 
                 null, true);
         WindowUtility.centerForm(form);
         form.addPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if(evt.getPropertyName().equals(AddressDialog.ADDRESS_SAVED)){
                     cadastreObjectBean.getSelectedAddress().copyFromObject((AddressBean)evt.getNewValue());
                 }
             }
         });
         form.setVisible(true);
     }
     
     private void removeAddress(){
         cadastreObjectBean.removeSelectedAddress();
     }
     
     /** 
      * Forcibly commits edits on FormattedText fields, such as valuation amount. 
      * It allows to save values to the underlying bean if field in the edit mode at the moment. 
      */
     public void commitEdits(){
         try {
             txtValuationAmount.commitEdit();
             txtArea.commitEdit();
         } catch (Exception e) {
         }
     }
     private void showCalendar(JFormattedTextField dateField) {
         CalendarForm calendar = new CalendarForm(null, true, dateField);
         calendar.setVisible(true);
     }
 
     /** Returns true if cadastre department relevant fields are enabled.*/
     public boolean isLockCadastreFields() {
         return lockCadastreFields;
     }
 
     /** Locks cadastre department relevant fields.*/
     public void setLockCadastreFields(boolean lockCadastreFields) {
         this.lockCadastreFields = lockCadastreFields;
         customizeForm();
     }
     
     /**
      * This method is used by the form designer to create the list of Surveyors.
      */
     private PartySummaryListBean createPartySummaryList() {
         PartySummaryListBean surveyorList = new PartySummaryListBean();
         surveyorList.FillSurveyors(true);
         return surveyorList;
     }
     
     
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         cadastreObjectTypeListBean1 = new org.sola.clients.beans.referencedata.CadastreObjectTypeListBean();
         popUpAddresses = new javax.swing.JPopupMenu();
         menuAdd1 = new org.sola.clients.swing.common.menuitems.MenuAdd();
         menuEdit1 = new org.sola.clients.swing.common.menuitems.MenuEdit();
         menuRemove1 = new org.sola.clients.swing.common.menuitems.MenuRemove();
         landGradeTypeListBean1 = createLadGradeList();
         roadClassTypeListBean1 = createRoadClassList();
         partySummaryList = createPartySummaryList();
         jPanel1 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         jLabel17 = new javax.swing.JLabel();
         txtFirstPart = new javax.swing.JTextField();
         jPanel3 = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         txtLastPart = new javax.swing.JTextField();
         jPanel4 = new javax.swing.JPanel();
         jLabel10 = new javax.swing.JLabel();
         cbxEstateType = new javax.swing.JComboBox();
         jPanel7 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         txtArea = new javax.swing.JFormattedTextField();
         jPanel5 = new javax.swing.JPanel();
         jLabel9 = new javax.swing.JLabel();
         txtParcelSurveyRef = new javax.swing.JTextField();
         jPanel9 = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         cbxSurveyor = new javax.swing.JComboBox();
         jPanel10 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         txtSurveyDate = new javax.swing.JFormattedTextField();
         btnSurveyDate = new javax.swing.JButton();
         jPanel11 = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         txtSurveyFee = new javax.swing.JFormattedTextField();
         jPanel6 = new javax.swing.JPanel();
         labLandUse = new javax.swing.JLabel();
         cbxRoadClass = new javax.swing.JComboBox();
         jPanel14 = new javax.swing.JPanel();
         labLandUse1 = new javax.swing.JLabel();
         cbxLandGrade = new javax.swing.JComboBox();
         jPanel8 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         txtValuationAmount = new javax.swing.JFormattedTextField();
         jPanel12 = new javax.swing.JPanel();
         jLabel6 = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         txtRemarks = new javax.swing.JTextArea();
         jPanel13 = new javax.swing.JPanel();
         groupPanel1 = new org.sola.clients.swing.ui.GroupPanel();
         jToolBar1 = new javax.swing.JToolBar();
         btnAdd1 = new org.sola.clients.swing.common.buttons.BtnAdd();
         btnEdit1 = new org.sola.clients.swing.common.buttons.BtnEdit();
         btnRemove1 = new org.sola.clients.swing.common.buttons.BtnRemove();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTableWithDefaultStyles1 = new org.sola.clients.swing.common.controls.JTableWithDefaultStyles();
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sola/clients/swing/ui/cadastre/Bundle"); // NOI18N
         popUpAddresses.setName(bundle.getString("ParcelPanel.popUpAddresses.name")); // NOI18N
 
         menuAdd1.setName(bundle.getString("ParcelPanel.menuAdd1.name")); // NOI18N
         menuAdd1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuAdd1ActionPerformed(evt);
             }
         });
         popUpAddresses.add(menuAdd1);
 
         menuEdit1.setName(bundle.getString("ParcelPanel.menuEdit1.name")); // NOI18N
         menuEdit1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuEdit1ActionPerformed(evt);
             }
         });
         popUpAddresses.add(menuEdit1);
 
         menuRemove1.setName(bundle.getString("ParcelPanel.menuRemove1.name")); // NOI18N
         menuRemove1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuRemove1ActionPerformed(evt);
             }
         });
         popUpAddresses.add(menuRemove1);
 
         setName("Form"); // NOI18N
 
         jPanel1.setName(bundle.getString("ParcelPanel.jPanel1.name")); // NOI18N
         jPanel1.setLayout(new java.awt.GridLayout(3, 4, 15, 15));
 
         jPanel2.setName(bundle.getString("ParcelPanel.jPanel2.name")); // NOI18N
 
         jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel17.setText(bundle.getString("ParcelPanel.jLabel17.text")); // NOI18N
         jLabel17.setName("jLabel17"); // NOI18N
 
         txtFirstPart.setEnabled(false);
         txtFirstPart.setName("txtFirstPart"); // NOI18N
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.nameFirstpart}"), txtFirstPart, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(jLabel17)
                 .add(0, 85, Short.MAX_VALUE))
             .add(txtFirstPart)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(jLabel17)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtFirstPart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 2, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel2);
 
         jPanel3.setName(bundle.getString("ParcelPanel.jPanel3.name")); // NOI18N
 
         jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel7.setText(bundle.getString("ParcelPanel.jLabel7.text")); // NOI18N
         jLabel7.setName("jLabel7"); // NOI18N
 
         txtLastPart.setEnabled(false);
         txtLastPart.setName("txtLastPart"); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.nameLastpart}"), txtLastPart, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(jLabel7)
                 .add(0, 86, Short.MAX_VALUE))
             .add(txtLastPart)
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(jLabel7)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtLastPart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 2, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel3);
 
         jPanel4.setName(bundle.getString("ParcelPanel.jPanel4.name")); // NOI18N
 
         jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel10.setText(bundle.getString("ParcelPanel.jLabel10.text")); // NOI18N
         jLabel10.setName("jLabel10"); // NOI18N
 
         cbxEstateType.setName("cbxEstateType"); // NOI18N
         cbxEstateType.setRenderer(new SimpleComboBoxRenderer("getDisplayValue"));
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectTypeList}");
         org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, cadastreObjectTypeListBean1, eLProperty, cbxEstateType);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.cadastreObjectType}"), cbxEstateType, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .add(jLabel10)
                 .add(0, 105, Short.MAX_VALUE))
             .add(cbxEstateType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .add(jLabel10)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cbxEstateType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 2, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel4);
 
         jPanel7.setName(bundle.getString("ParcelPanel.jPanel7.name")); // NOI18N
 
         jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel1.setText(bundle.getString("ParcelPanel.jLabel1.text")); // NOI18N
         jLabel1.setName(bundle.getString("ParcelPanel.jLabel1.name")); // NOI18N
 
         txtArea.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
         txtArea.setText(bundle.getString("ParcelPanel.txtArea.text")); // NOI18N
         txtArea.setName(bundle.getString("ParcelPanel.txtArea.name")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.officialAreaSize}"), txtArea, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel7Layout.createSequentialGroup()
                 .add(jLabel1)
                 .addContainerGap(81, Short.MAX_VALUE))
             .add(txtArea)
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel7Layout.createSequentialGroup()
                 .add(jLabel1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel1.add(jPanel7);
 
         jPanel5.setName(bundle.getString("ParcelPanel.jPanel5.name")); // NOI18N
 
         jLabel9.setText(bundle.getString("ParcelPanel.jLabel9.text")); // NOI18N
         jLabel9.setName("jLabel9"); // NOI18N
 
         txtParcelSurveyRef.setName("txtParcelSurveyRef"); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.sourceReference}"), txtParcelSurveyRef, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel5Layout.createSequentialGroup()
                 .add(jLabel9)
                 .add(0, 90, Short.MAX_VALUE))
             .add(txtParcelSurveyRef)
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel5Layout.createSequentialGroup()
                 .add(jLabel9)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtParcelSurveyRef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel1.add(jPanel5);
 
         jPanel9.setName(bundle.getString("ParcelPanel.jPanel9.name")); // NOI18N
 
         jLabel3.setText(bundle.getString("ParcelPanel.jLabel3.text")); // NOI18N
         jLabel3.setName(bundle.getString("ParcelPanel.jLabel3.name")); // NOI18N
 
         cbxSurveyor.setName(bundle.getString("ParcelPanel.cbxSurveyor.name")); // NOI18N
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${partySummaryList}");
         jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, partySummaryList, eLProperty, cbxSurveyor);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.surveyor}"), cbxSurveyor, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel9Layout.createSequentialGroup()
                 .add(jLabel3)
                 .add(0, 99, Short.MAX_VALUE))
             .add(cbxSurveyor, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel9Layout.createSequentialGroup()
                 .add(jLabel3)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                 .add(cbxSurveyor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel1.add(jPanel9);
 
         jPanel10.setName(bundle.getString("ParcelPanel.jPanel10.name")); // NOI18N
 
         jLabel4.setText(bundle.getString("ParcelPanel.jLabel4.text")); // NOI18N
         jLabel4.setName(bundle.getString("ParcelPanel.jLabel4.name")); // NOI18N
 
         txtSurveyDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT))));
         txtSurveyDate.setName(bundle.getString("ParcelPanel.txtSurveyDate.name")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.surveyDate}"), txtSurveyDate, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         txtSurveyDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtSurveyDateActionPerformed(evt);
             }
         });
 
         btnSurveyDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnSurveyDate.setText(bundle.getString("ParcelPanel.btnSurveyDate.text")); // NOI18N
         btnSurveyDate.setBorder(null);
         btnSurveyDate.setName(bundle.getString("ParcelPanel.btnSurveyDate.name")); // NOI18N
         btnSurveyDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSurveyDateActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel10Layout.createSequentialGroup()
                 .add(jLabel4)
                 .add(0, 83, Short.MAX_VALUE))
             .add(jPanel10Layout.createSequentialGroup()
                 .add(txtSurveyDate)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(btnSurveyDate))
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel10Layout.createSequentialGroup()
                 .add(jLabel4)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(btnSurveyDate)
                     .add(txtSurveyDate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
         );
 
         jPanel1.add(jPanel10);
 
         jPanel11.setName(bundle.getString("ParcelPanel.jPanel11.name")); // NOI18N
 
         jLabel5.setText(bundle.getString("ParcelPanel.jLabel5.text")); // NOI18N
         jLabel5.setName(bundle.getString("ParcelPanel.jLabel5.name")); // NOI18N
 
         txtSurveyFee.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
         txtSurveyFee.setText(bundle.getString("ParcelPanel.txtSurveyFee.text")); // NOI18N
         txtSurveyFee.setName(bundle.getString("ParcelPanel.txtSurveyFee.name")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.surveyFee}"), txtSurveyFee, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel11Layout.createSequentialGroup()
                 .add(jLabel5)
                 .add(0, 88, Short.MAX_VALUE))
             .add(txtSurveyFee)
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel11Layout.createSequentialGroup()
                 .add(jLabel5)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtSurveyFee, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel1.add(jPanel11);
 
         jPanel6.setName(bundle.getString("ParcelPanel.jPanel6.name")); // NOI18N
 
         labLandUse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         labLandUse.setText(bundle.getString("ParcelPanel.labLandUse.text")); // NOI18N
         labLandUse.setName(bundle.getString("ParcelPanel.labLandUse.name")); // NOI18N
 
         cbxRoadClass.setName(bundle.getString("ParcelPanel.cbxRoadClass.name")); // NOI18N
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${roadClassTypeList}");
         jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, roadClassTypeListBean1, eLProperty, cbxRoadClass);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.roadClassType}"), cbxRoadClass, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel6Layout.createSequentialGroup()
                 .add(labLandUse)
                 .add(0, 76, Short.MAX_VALUE))
             .add(cbxRoadClass, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel6Layout.createSequentialGroup()
                 .add(labLandUse)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cbxRoadClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 2, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel6);
 
         jPanel14.setName(bundle.getString("ParcelPanel.jPanel14.name")); // NOI18N
 
         labLandUse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         labLandUse1.setText(bundle.getString("ParcelPanel.labLandUse1.text")); // NOI18N
         labLandUse1.setName(bundle.getString("ParcelPanel.labLandUse1.name")); // NOI18N
 
         cbxLandGrade.setName(bundle.getString("ParcelPanel.cbxLandGrade.name")); // NOI18N
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${landGradeTypeList}");
         jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, landGradeTypeListBean1, eLProperty, cbxLandGrade);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.landGradeType}"), cbxLandGrade, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel14Layout = new org.jdesktop.layout.GroupLayout(jPanel14);
         jPanel14.setLayout(jPanel14Layout);
         jPanel14Layout.setHorizontalGroup(
             jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel14Layout.createSequentialGroup()
                 .add(labLandUse1)
                 .add(0, 105, Short.MAX_VALUE))
             .add(cbxLandGrade, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel14Layout.setVerticalGroup(
             jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel14Layout.createSequentialGroup()
                 .add(labLandUse1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cbxLandGrade, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 2, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel14);
 
         jPanel8.setName(bundle.getString("ParcelPanel.jPanel8.name")); // NOI18N
 
         jLabel2.setText(bundle.getString("ParcelPanel.jLabel2.text")); // NOI18N
         jLabel2.setName(bundle.getString("ParcelPanel.jLabel2.name")); // NOI18N
 
         txtValuationAmount.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
         txtValuationAmount.setText(bundle.getString("ParcelPanel.txtValuationAmount.text")); // NOI18N
         txtValuationAmount.setName(bundle.getString("ParcelPanel.txtValuationAmount.name")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.valuationAmount}"), txtValuationAmount, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel8Layout.createSequentialGroup()
                 .add(jLabel2)
                 .add(0, 59, Short.MAX_VALUE))
             .add(txtValuationAmount)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel8Layout.createSequentialGroup()
                 .add(jLabel2)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtValuationAmount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel1.add(jPanel8);
 
         jPanel12.setName(bundle.getString("ParcelPanel.jPanel12.name")); // NOI18N
 
         jLabel6.setText(bundle.getString("ParcelPanel.jLabel6.text")); // NOI18N
         jLabel6.setName(bundle.getString("ParcelPanel.jLabel6.name")); // NOI18N
 
         jScrollPane2.setName(bundle.getString("ParcelPanel.jScrollPane2.name")); // NOI18N
 
         txtRemarks.setColumns(20);
         txtRemarks.setRows(5);
         txtRemarks.setName(bundle.getString("ParcelPanel.txtRemarks.name")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.remarks}"), txtRemarks, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jScrollPane2.setViewportView(txtRemarks);
 
         org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel12Layout.createSequentialGroup()
                 .add(jLabel6)
                 .add(0, 0, Short.MAX_VALUE))
             .add(jScrollPane2)
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel12Layout.createSequentialGroup()
                 .add(jLabel6)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane2))
         );
 
         jPanel13.setName(bundle.getString("ParcelPanel.jPanel13.name")); // NOI18N
 
         groupPanel1.setName(bundle.getString("ParcelPanel.groupPanel1.name")); // NOI18N
         groupPanel1.setTitleText(bundle.getString("ParcelPanel.groupPanel1.titleText")); // NOI18N
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
         jToolBar1.setName(bundle.getString("ParcelPanel.jToolBar1.name")); // NOI18N
 
         btnAdd1.setName(bundle.getString("ParcelPanel.btnAdd1.name")); // NOI18N
         btnAdd1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAdd1ActionPerformed(evt);
             }
         });
         jToolBar1.add(btnAdd1);
 
         btnEdit1.setName(bundle.getString("ParcelPanel.btnEdit1.name")); // NOI18N
         btnEdit1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnEdit1ActionPerformed(evt);
             }
         });
         jToolBar1.add(btnEdit1);
 
         btnRemove1.setName(bundle.getString("ParcelPanel.btnRemove1.name")); // NOI18N
         btnRemove1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemove1ActionPerformed(evt);
             }
         });
         jToolBar1.add(btnRemove1);
 
         jScrollPane1.setName(bundle.getString("ParcelPanel.jScrollPane1.name")); // NOI18N
 
         jTableWithDefaultStyles1.setName(bundle.getString("ParcelPanel.jTableWithDefaultStyles1.name")); // NOI18N
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.addressFilteredList}");
         org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, jTableWithDefaultStyles1);
         org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${description}"));
         columnBinding.setColumnName("Description");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${cadastreObjectBean.selectedAddress}"), jTableWithDefaultStyles1, org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
         bindingGroup.addBinding(binding);
 
         jScrollPane1.setViewportView(jTableWithDefaultStyles1);
         jTableWithDefaultStyles1.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("ParcelPanel.jTableWithDefaultStyles1.columnModel.title0_1")); // NOI18N
         jTableWithDefaultStyles1.getColumnModel().getColumn(0).setCellRenderer(new org.sola.clients.swing.ui.renderers.TableCellTextAreaRenderer());
 
         org.jdesktop.layout.GroupLayout jPanel13Layout = new org.jdesktop.layout.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(groupPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
             .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel13Layout.createSequentialGroup()
                 .add(groupPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE))
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(jPanel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(18, 18, 18)
                 .add(jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnAdd1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdd1ActionPerformed
         addAddress();
     }//GEN-LAST:event_btnAdd1ActionPerformed
 
     private void btnEdit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEdit1ActionPerformed
         editAddress();
     }//GEN-LAST:event_btnEdit1ActionPerformed
 
     private void btnRemove1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemove1ActionPerformed
         removeAddress();
     }//GEN-LAST:event_btnRemove1ActionPerformed
 
     private void menuAdd1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAdd1ActionPerformed
         addAddress();
     }//GEN-LAST:event_menuAdd1ActionPerformed
 
     private void menuEdit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEdit1ActionPerformed
         editAddress();
     }//GEN-LAST:event_menuEdit1ActionPerformed
 
     private void menuRemove1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRemove1ActionPerformed
         removeAddress();
     }//GEN-LAST:event_menuRemove1ActionPerformed
 
     private void btnSurveyDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSurveyDateActionPerformed
         showCalendar(txtSurveyDate);
     }//GEN-LAST:event_btnSurveyDateActionPerformed
 
     private void txtSurveyDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSurveyDateActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_txtSurveyDateActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.sola.clients.swing.common.buttons.BtnAdd btnAdd1;
     private org.sola.clients.swing.common.buttons.BtnEdit btnEdit1;
     private org.sola.clients.swing.common.buttons.BtnRemove btnRemove1;
     private javax.swing.JButton btnSurveyDate;
     private org.sola.clients.beans.referencedata.CadastreObjectTypeListBean cadastreObjectTypeListBean1;
     private javax.swing.JComboBox cbxEstateType;
     private javax.swing.JComboBox cbxLandGrade;
     private javax.swing.JComboBox cbxRoadClass;
     private javax.swing.JComboBox cbxSurveyor;
     private org.sola.clients.swing.ui.GroupPanel groupPanel1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private org.sola.clients.swing.common.controls.JTableWithDefaultStyles jTableWithDefaultStyles1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JLabel labLandUse;
     private javax.swing.JLabel labLandUse1;
     private org.sola.clients.beans.referencedata.LandGradeTypeListBean landGradeTypeListBean1;
     private org.sola.clients.swing.common.menuitems.MenuAdd menuAdd1;
     private org.sola.clients.swing.common.menuitems.MenuEdit menuEdit1;
     private org.sola.clients.swing.common.menuitems.MenuRemove menuRemove1;
     private org.sola.clients.beans.party.PartySummaryListBean partySummaryList;
     private javax.swing.JPopupMenu popUpAddresses;
     private org.sola.clients.beans.referencedata.RoadClassTypeListBean roadClassTypeListBean1;
     private javax.swing.JFormattedTextField txtArea;
     private javax.swing.JTextField txtFirstPart;
     private javax.swing.JTextField txtLastPart;
     private javax.swing.JTextField txtParcelSurveyRef;
     private javax.swing.JTextArea txtRemarks;
     private javax.swing.JFormattedTextField txtSurveyDate;
     private javax.swing.JFormattedTextField txtSurveyFee;
     private javax.swing.JFormattedTextField txtValuationAmount;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 }
