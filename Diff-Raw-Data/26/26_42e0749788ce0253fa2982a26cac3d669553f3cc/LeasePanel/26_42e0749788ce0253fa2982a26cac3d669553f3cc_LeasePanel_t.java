 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
  * (FAO). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.clients.swing.desktop.administrative;
 
 import java.awt.BorderLayout;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Date;
 import java.util.Map;
 import javax.swing.JFormattedTextField;
 import javax.validation.groups.Default;
 import net.sf.jasperreports.engine.JasperPrint;
 import org.geotools.map.extended.layer.ExtendedLayer;
 import org.geotools.swing.extended.exception.InitializeMapException;
 import org.geotools.swing.extended.util.GeometryUtility;
 import org.jdesktop.beansbinding.AutoBinding;
 import org.jdesktop.beansbinding.Bindings;
 import org.sola.clients.beans.administrative.BaUnitBean;
 import org.sola.clients.beans.administrative.LeaseReportBean;
 import org.sola.clients.beans.administrative.LeaseSpecialConditionBean;
 import org.sola.clients.beans.administrative.RrrBean;
 import org.sola.clients.beans.administrative.validation.LeaseValidationGroup;
 import org.sola.clients.beans.administrative.validation.RrrValidationGroup;
 import org.sola.clients.beans.application.ApplicationBean;
 import org.sola.clients.beans.application.ApplicationServiceBean;
 import org.sola.clients.beans.cadastre.CadastreObjectBean;
 import org.sola.clients.beans.cadastre.CadastreObjectSummaryBean;
 import org.sola.clients.beans.referencedata.RequestTypeBean;
 import org.sola.clients.beans.referencedata.StatusConstants;
 import org.sola.clients.beans.security.SecurityBean;
 import org.sola.clients.reports.ReportManager;
 import org.sola.clients.swing.common.LafManager;
 import org.sola.clients.swing.common.tasks.SolaTask;
 import org.sola.clients.swing.common.tasks.TaskManager;
 import org.sola.clients.swing.desktop.MainForm;
 import org.sola.clients.swing.desktop.source.DocumentsManagementExtPanel;
 import org.sola.clients.swing.gis.ui.controlsbundle.ControlsBundleForBaUnit;
 import org.sola.clients.swing.ui.ContentPanel;
 import org.sola.clients.swing.ui.renderers.FormattersFactory;
 import org.sola.common.messaging.ClientMessage;
 import org.sola.common.messaging.MessageUtility;
 import org.sola.clients.swing.common.controls.CalendarForm;
 import org.sola.clients.swing.desktop.cadastre.CadastreObjectSearchForm;
 import org.sola.clients.swing.desktop.party.PartyListExtPanel;
 import org.sola.clients.swing.gis.ui.control.MapFeatureImageGenerator;
 import org.sola.clients.swing.ui.MainContentPanel;
 import org.sola.clients.swing.ui.cadastre.CadastreObjectsSearchPanel;
 import org.sola.clients.swing.ui.renderers.TableCellTextAreaRenderer;
 import org.sola.clients.swing.ui.reports.FreeTextDialog;
 import org.sola.clients.swing.ui.reports.ReportViewerForm;
 import org.sola.common.RolesConstants;
 import org.sola.common.StringUtility;
 import org.sola.common.WindowUtility;
 import org.sola.common.logging.LogUtility;
 
 /**
  * Form for managing simple ownership right. {@link RrrBean} is used to bind the
  * data on the form.
  */
 public class LeasePanel extends ContentPanel {
 
     private ApplicationBean applicationBean;
     private ApplicationServiceBean appService;
     private RrrBean.RRR_ACTION rrrAction;
     private BaUnitBean baUnit;
     private ControlsBundleForBaUnit mapControl;
     private boolean zoomedToPlot = false;
     public static final String UPDATED_RRR = "updatedRRR";
 
     private DocumentsManagementExtPanel createDocumentsPanel() {
         if (rrrBean == null) {
             rrrBean = new RrrBean();
         }
         if (applicationBean == null) {
             applicationBean = new ApplicationBean();
         }
 
         boolean allowEdit = true;
         if (rrrAction == RrrBean.RRR_ACTION.VIEW) {
             allowEdit = false;
         }
 
         DocumentsManagementExtPanel panel = new DocumentsManagementExtPanel(
                 rrrBean.getSourceList(), applicationBean, allowEdit);
         return panel;
     }
 
     private PartyListExtPanel createPartyListPanel() {
         return new PartyListExtPanel(rrrBean.getRightHolderList());
     }
 
     private RrrBean CreateRrrBean() {
         if (rrrBean == null) {
             rrrBean = new RrrBean();
         }
         return rrrBean;
     }
 
     /**
      * Creates new form SimpleOwhershipPanel
      */
     public LeasePanel(BaUnitBean baUnit, RrrBean rrrBean, ApplicationBean applicationBean,
             ApplicationServiceBean applicationService, RrrBean.RRR_ACTION rrrAction,
             ControlsBundleForBaUnit map) {
         this.baUnit = baUnit;
         this.applicationBean = applicationBean;
         this.appService = applicationService;
         this.rrrAction = rrrAction;
         this.mapControl = map;
         prepareRrrBean(rrrBean, rrrAction);
         initComponents();
         postInit();
     }
 
     private void postInit() {
         rrrBean.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(RrrBean.SELECTED_SPECIAL_CONDITION_PROPERTY)) {
                     customizeConditionButtons();
                 }
             }
         });
 
         if (isSublease()) {
             // Load the cadastre object if not already set
             if (rrrBean.getCadastreObjectId() != null && rrrBean.getCadastreObject() == null) {
                 rrrBean.setCadastreObject(CadastreObjectBean.getCadastreObject(
                         rrrBean.getCadastreObjectId()));
                 zoomedToPlot = false;
             }
 
             // Create the binding between the Parcel Details panel and the Cadastre Object on the rrrBean
             org.jdesktop.beansbinding.Binding binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
                     rrrBean, org.jdesktop.beansbinding.ELProperty.create("${cadastreObject}"),
                     parcelPanel1, org.jdesktop.beansbinding.BeanProperty.create("cadastreObjectBean"), "parcelDetailsBinding");
             bindingGroup.addBinding(binding);
             bindingGroup.bind();
         }
 
         customizeForm();
         saveRrrState();
     }
 
     private void customizeForm() {
         headerPanel.setTitleText(rrrBean.getRrrType().getDisplayValue());
         if (!StringUtility.isEmpty(rrrBean.getLeaseNumber())) {
             headerPanel.setTitleText(headerPanel.getTitleText() + " #" + rrrBean.getLeaseNumber());
         }
 
         if (rrrAction == RrrBean.RRR_ACTION.NEW) {
             btnSave.setText(MessageUtility.getLocalizedMessage(
                     ClientMessage.GENERAL_LABELS_CREATE_AND_CLOSE).getMessage());
         }
         if (rrrAction == RrrBean.RRR_ACTION.CANCEL) {
             btnSave.setText(MessageUtility.getLocalizedMessage(
                     ClientMessage.GENERAL_LABELS_TERMINATE_AND_CLOSE).getMessage());
         }
 
         boolean enabled = rrrAction != RrrBean.RRR_ACTION.VIEW;
         boolean regEnabled = enabled && SecurityBean.isInRole(RolesConstants.ADMINISTRATIVE_REGISTER_LEASE);
         boolean leaseEnabled = enabled && !isLeaseTransfer()
                 && rrrAction != RrrBean.RRR_ACTION.CANCEL
                 && SecurityBean.isInRole(RolesConstants.ADMINISTRATIVE_MANAGE_LEASE);
         boolean partyListEnabled = leaseEnabled || (enabled && isLeaseTransfer());
 
         // Common fields for registration and lease management
         btnSave.setEnabled(enabled);
         txtStampDuty.setEnabled(enabled);
 
         // Registration part
         txtNotationText.setEnabled(regEnabled);
         txtRegDatetime.setEnabled(regEnabled);
         txtRegistrationNumber.setEnabled(regEnabled);
         txtTransferDuty.setEnabled(regEnabled);
         txtRegistrationFee.setEnabled(regEnabled);
         btnRegistrationDate.setEnabled(regEnabled);
 
         // Lease management part
         txtStartDate.setEnabled(leaseEnabled);
         txtLeaseTerm.setEnabled(leaseEnabled);
         txtPersonalLevy.setEnabled(leaseEnabled);
         txtUsableLand.setEnabled(leaseEnabled);
         txtExpirationDate.setEnabled(leaseEnabled);
         txtExecutionDate.setEnabled(leaseEnabled);
         txtRent.setEnabled(leaseEnabled);
         txtDueDate.setEnabled(leaseEnabled);
         partyList.setReadOnly(!partyListEnabled);
         btnCalculateGroundRent.setEnabled(leaseEnabled);
         btnStartDate.setEnabled(leaseEnabled);
         btnExpirationDate.setEnabled(leaseEnabled);
         btnExecutionDate.setEnabled(leaseEnabled);
         btnNextPaymentDate.setEnabled(leaseEnabled);
         cbxLandUse.setEnabled(leaseEnabled);
 
         menuLease.setEnabled(leaseEnabled);
         menuRejectionLetter.setEnabled(leaseEnabled);
         menuOfferLetter.setEnabled(leaseEnabled);
         menuLeaseSurrender.setEnabled(leaseEnabled);//rrrAction == RrrBean.RRR_ACTION.CANCEL
         menuLeaseVary.setEnabled(leaseEnabled);//
 
         if (!isSublease()) {
             // Remove the tabs for sublease
             jTabbedPane1.removeTabAt(jTabbedPane1.indexOfComponent(subplotTabPanel));
             jTabbedPane1.removeTabAt(jTabbedPane1.indexOfComponent(mapTabPanel));
         } else {
             btnAddSubplot.setEnabled(enabled);
             btnRemoveSubplot.setEnabled(enabled);
         }
 
         if (txtNotationText.isEnabled() && txtNotationText.getText().equals("")
                 && rrrAction != RrrBean.RRR_ACTION.VIEW && appService != null) {
             txtNotationText.setText(appService.getRequestType().getNotationTemplate());
         }
 
         customizeConditionButtons();
     }
 
     private void customizeConditionButtons() {
         boolean editable = rrrAction != RrrBean.RRR_ACTION.VIEW
                 && SecurityBean.isInRole(RolesConstants.ADMINISTRATIVE_MANAGE_LEASE);
 
         boolean enabled = editable && rrrBean.getSelectedSpecialCondition() != null;
 
         btnAddCodition.setEnabled(editable);
         btnEditCondition.setEnabled(enabled);
         btnRemoveCondition.setEnabled(enabled);
 
         menuAddCondition.setEnabled(btnAddCodition.isEnabled());
         menuEditCondition.setEnabled(btnEditCondition.isEnabled());
         menuRemoveCondition.setEnabled(btnRemoveCondition.isEnabled());
     }
 
     private void prepareRrrBean(RrrBean rrrBean, RrrBean.RRR_ACTION rrrAction) {
         if (rrrBean == null) {
             this.rrrBean = new RrrBean();
             this.rrrBean.setStatusCode(StatusConstants.PENDING);
         } else {
             this.rrrBean = rrrBean.makeCopyByAction(rrrAction);
         }
 
         if (isSublease()) {
             if (this.rrrBean.getLeaseNumber() == null
                     || this.rrrBean.getLeaseNumber().trim().isEmpty()) {
                 this.rrrBean.setLeaseNumber(generateSubleaseNumber());
             }
         } else if ((StringUtility.empty(this.rrrBean.getStatusCode()).equals("")
                 || this.rrrBean.getStatusCode().equals(StatusConstants.PENDING))
                 && baUnit != null && baUnit.getCadastreObject() != null) {
             this.rrrBean.setLeaseNumber(baUnit.getCadastreObject().toString());
         }
 
         if (!this.rrrBean.isPrimary()) {
             this.rrrBean.setPrimary(true);
         }
     }
 
     // Returns true is service type affects change of lessee
     private boolean isLeaseTransfer() {
         if (appService != null && appService.getRequestTypeCode() != null) {
             String typeCode = appService.getRequestTypeCode();
             if (typeCode.equals(RequestTypeBean.CODE_ENDORSEMENT)
                     || typeCode.equals(RequestTypeBean.CODE_NAME_CHANGE)
                     || typeCode.equals(RequestTypeBean.CODE_LEASE_TRANSFER)) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean saveRrr() {
         boolean validated;
 
         if (isSublease()) {
             // Configures validations for used by sublease
             if (rrrBean.getLeaseExpiryDate() == null) {
                 // Set data for the sublease expiration date validation
                 rrrBean.setLeaseExpiryDate(getMaxSubLeaseExpiryDate());
             }
 
             if (rrrBean.getCadastreObject() != null && baUnit.getCadastreObject() != null) {
                 // Check the subplot area is within the area of the lease. Must be done
                 // here as the validation bean must not have a dependency to GeometryUtility.
                 rrrBean.setSubplotValid(
                         GeometryUtility.getGeometryFromWkb(baUnit.getCadastreObject().getGeomPolygon()).buffer(1).contains(
                         GeometryUtility.getGeometryFromWkb(rrrBean.getCadastreObject().getGeomPolygon())));
             } else {
                 rrrBean.setSubplotValid(true);
             }
         }
 
         // If user has only lease management role
         if (SecurityBean.isInRole(RolesConstants.ADMINISTRATIVE_MANAGE_LEASE)
                 && !SecurityBean.isInRole(RolesConstants.ADMINISTRATIVE_REGISTER_LEASE)) {
             validated = rrrBean.validate(true, Default.class, LeaseValidationGroup.class).size() < 1;
         } else {
             // Otherwise check all rules
             validated = rrrBean.validate(true, Default.class,
                     LeaseValidationGroup.class, RrrValidationGroup.class).size() < 1;
         }
 
         if (validated) {
             firePropertyChange(UPDATED_RRR, null, rrrBean);
             close();
         }
         return validated;
     }
 
     private void saveRrrState() {
         MainForm.saveBeanState(rrrBean);
     }
 
     @Override
     protected boolean panelClosing() {
         if (btnSave.isEnabled() && MainForm.checkSaveBeforeClose(rrrBean)) {
             return saveRrr();
         }
         return true;
     }
 
     private void showCalendar(JFormattedTextField dateField) {
         CalendarForm calendar = new CalendarForm(null, true, dateField);
         calendar.setVisible(true);
     }
 
     private LeaseReportBean prepareReportBean() {
         return new LeaseReportBean(rrrBean, baUnit.getCadastreObject(), applicationBean, appService);
     }
 
     /**
      * Opens {@link ReportViewerForm} to display report.
      */
     private void showReport(JasperPrint report) {
         ReportViewerForm form = new ReportViewerForm(report);
         form.setLocationRelativeTo(this);
         form.setVisible(true);
     }
 
     private void printLease() {
         if (rrrBean.validate(true, Default.class, LeaseValidationGroup.class).size() < 1) {
             final LeaseReportBean reportBean = prepareReportBean();
             if (reportBean != null) {
                 showReport(ReportManager.getLeaseReport(reportBean, createMapImage()));
             }
         }
     }
 
     private void printLeaseSurrender() {
         final LeaseReportBean reportBean = prepareReportBean();
         if (reportBean != null) {
             showReport(ReportManager.getLeaseSurrenderReport(reportBean));
         }
     }
 
     private void printLeaseVary() {
         final LeaseReportBean reportBean = prepareReportBean();
         if (reportBean != null) {
             showReport(ReportManager.getLeaseVaryReport(reportBean));
         }
     }
 
     private void printOfferLetter() {
         final LeaseReportBean reportBean = prepareReportBean();
         if (reportBean != null) {
             showReport(ReportManager.getLeaseOfferReport(reportBean));
         }
     }
 
     private void printSuccessionReport() {
         final LeaseReportBean reportBean = prepareReportBean();
         if (reportBean != null) {
             showReport(ReportManager.getSuccessionReport(reportBean));
         }
     }
 
     private void printRejectionLetter() {
         final LeaseReportBean reportBean = prepareReportBean();
         if (reportBean != null) {
             // Show free text form
             FreeTextDialog form = new FreeTextDialog(
                     MessageUtility.getLocalizedMessageText(ClientMessage.BAUNIT_LEASE_REJECTION_REASON_TITLE),
                     null, MainForm.getInstance(), true);
             WindowUtility.centerForm(form);
 
             form.addPropertyChangeListener(new PropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent evt) {
                     if (evt.getPropertyName().equals(FreeTextDialog.TEXT_TO_SAVE)) {
                         reportBean.setFreeText((String) evt.getNewValue());
                     }
                 }
             });
             form.setVisible(true);
             showReport(ReportManager.getLeaseRejectionReport(reportBean));
         }
     }
 
     /**
      * Uses the mapControl from the Property form to render the lease parcel on
      * top of the map to create as an image for the lease report. The context
      * information shown in the image (i.e. abutting parcels, roads, rivers,
      * zoom scale, etc) can be changed by the user by modifying the map control
      * on the Property form.
      *
      * @return The file name for the generated image or NULL if the map control
      * is not set.
      */
     private String createMapImage() {
         String result = null;
         if (baUnit.getCadastreObject() != null && mapControl != null && mapControl.getMap() != null) {
             try {
                 // Remove any temporary objects from the map and turn off any layers
                 // that should not be displayed on the Location diagram such as the parcel nodes layer
                 // and the grid layer
                 mapControl.setCadastreObject(null);
 
                 for (Map.Entry<String, ExtendedLayer> en : mapControl.getMap().getSolaLayers().entrySet()) {
                     //String key = en.getKey();
                     //if(!key.equals("parcels") && !key.equals("subplots") && !key.equals("roads") && !key.equals("selection")){
                     ExtendedLayer layer = en.getValue();
                     layer.setVisible(false);
                 }
 
                 MapFeatureImageGenerator generator = new MapFeatureImageGenerator(mapControl.getMap());
 
                 String parcelLabel = baUnit.getCadastreObject().toString();
                 result = generator.getFeatureImage(
                         baUnit.getCadastreObject().getGeomPolygon(),
                         parcelLabel, null,
                         MapFeatureImageGenerator.IMAGE_FORMAT_PNG);
 
             } catch (InitializeMapException mapEx) {
                 LogUtility.log("Unable to initialize MapFeaureImageGenerator", mapEx);
             }
         }
         return result;
     }
 
     private void calculateGroundRent() {
         if (baUnit.getCadastreObject() == null) {
             MessageUtility.displayMessage(ClientMessage.LEASE_SELECT_PLOT);
             return;
         }
        
        if (rrrBean.getLandUseCode() == null) {
            MessageUtility.displayMessage(ClientMessage.LEASE_SELECT_LAND_USE);
            return;
        }
 
         SolaTask<Void, Void> t = new SolaTask<Void, Void>() {
             @Override
             public Void doTask() {
                 setMessage(MessageUtility.getLocalizedMessageText(ClientMessage.PROGRESS_MSG_SAVING));
                 rrrBean.calculateGroundRent(baUnit.getCadastreObject());
                 return null;
             }
         };
         TaskManager.getInstance().runTask(t);
     }
 
     private void addCondition() {
         LeaseSpecialConditionDialog form = new LeaseSpecialConditionDialog(null, null, true);
         form.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(LeaseSpecialConditionDialog.LEASE_CONDITION_SAVED)) {
                     rrrBean.getLeaseSpecialConditionList().addAsNew((LeaseSpecialConditionBean) evt.getNewValue());
                 }
             }
         });
         form.setVisible(true);
     }
 
     private void editCondition() {
         if (rrrBean.getSelectedSpecialCondition() == null) {
             return;
         }
 
         LeaseSpecialConditionDialog form = new LeaseSpecialConditionDialog(
                 (LeaseSpecialConditionBean) rrrBean.getSelectedSpecialCondition().copy(), null, true);
         form.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(LeaseSpecialConditionDialog.LEASE_CONDITION_SAVED)) {
                     rrrBean.getSelectedSpecialCondition().copyFromObject((LeaseSpecialConditionBean) evt.getNewValue());
                 }
             }
         });
         form.setVisible(true);
     }
 
     private void removeCondition() {
         if (MessageUtility.displayMessage(ClientMessage.CONFIRM_REMOVE_RECORD) == MessageUtility.BUTTON_ONE) {
             rrrBean.removeSelectedCondition();
         }
     }
 
     // Returns true if the rrrtype is sublease
     private boolean isSublease() {
         return RrrBean.CODE_SUBLEASE.equals(rrrBean.getTypeCode());
     }
 
     /**
      * Determines the max expiry date for the sublease by obtaining the getting
      * the expiry date of the primary lease. Used for validation of the sublease
      * expiry date.
      *
      * @return
      */
     private Date getMaxSubLeaseExpiryDate() {
         Date result = null;
         // Check for any pending leases first as the expiry date on the
         // pending lease will be the one that gets applied at registration
         for (RrrBean bean : baUnit.getRrrFilteredList()) {
             if (RrrBean.CODE_LEASE.equals(bean.getTypeCode())
                     && StatusConstants.PENDING.equals(bean.getStatusCode())) {
                 result = bean.getExpirationDate();
                 break;
             }
         }
         // Check for the current lease
         if (result == null) {
             for (RrrBean bean : baUnit.getRrrFilteredList()) {
                 if (RrrBean.CODE_LEASE.equals(bean.getTypeCode())
                         && StatusConstants.CURRENT.equals(bean.getStatusCode())) {
                     result = bean.getExpirationDate();
                     break;
                 }
             }
         }
         return result;
     }
 
     /**
      * Determines the sublease number based on the number of subleases that are
      * already on the lease.
      *
      * @return
      */
     private String generateSubleaseNumber() {
         String result = null;
         if (baUnit.getCadastreObject().toString() != null) {
             int count = 0;
 
             for (RrrBean bean : baUnit.getRrrFilteredList()) {
                 if (RrrBean.CODE_SUBLEASE.equals(bean.getTypeCode())) {
                     count++;
                 }
             }
             count++;
             result = String.format("%s-%s", baUnit.getCadastreObject().toString(),
                     String.format("%03d", count));
         }
 
         return result;
     }
 
     /**
      * Opens the CO Search form so the user can search for the subplot parcel
      */
     private void addSubplot() {
         CadastreObjectSearchForm form = new CadastreObjectSearchForm();
         form.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(CadastreObjectsSearchPanel.SELECTED_CADASTRE_OBJECT)) {
                     rrrBean.setCadastreObject(CadastreObjectBean.getCadastreObject(
                             ((CadastreObjectSummaryBean) evt.getNewValue()).getId()));
 
                     if (rrrBean.getCadastreObject() != null) {
                         rrrBean.setCadastreObjectId(rrrBean.getCadastreObject().getId());
                         if (CadastreObjectBean.SUBPLOT_TYPE.equals(rrrBean.getCadastreObject().getTypeCode())) {
                             // This is a subplot, so set the sublease number to match the subplot number. 
                             rrrBean.setLeaseNumber(rrrBean.getCadastreObject().toString());
                         }
                     } else {
                         rrrBean.setCadastreObjectId(null);
                     }
                     zoomedToPlot = false;
                 }
             }
         });
         getMainContentPanel().addPanel(form, MainContentPanel.CARD_PARCEL_SEARCH, true);
     }
 
     /**
      * Clears the selected subplot for the sublease
      */
     private void removeSubplot() {
         rrrBean.setCadastreObject(null);
         rrrBean.setCadastreObjectId(null);
         zoomedToPlot = false;
     }
 
     /**
      * Focuses the map on the subplot if one exists otherwise zooms the map to
      * the area of the lease.
      */
     private void zoomToPlot() {
         if (!zoomedToPlot && rrrBean.getCadastreObject() != null) {
             // Highlight the subplot on the map
             this.mapControl.setCadastreObject(rrrBean.getCadastreObject());
         } else if (!zoomedToPlot && baUnit.getCadastreObject() != null) {
             // Zoom to the area of the lease, but do not highlight anything
             // as there is no subplot defined (i.e. remove the lease area 
             // after the zoom to action. 
             this.mapControl.setCadastreObject(baUnit.getCadastreObject());
             this.mapControl.setCadastreObject(null);
             this.mapControl.refresh(true);
         }
         zoomedToPlot = true;
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         rrrBean = CreateRrrBean();
         popupConditions = new javax.swing.JPopupMenu();
         menuAddCondition = new org.sola.clients.swing.common.menuitems.MenuAdd();
         menuEditCondition = new org.sola.clients.swing.common.menuitems.MenuEdit();
         menuRemoveCondition = new org.sola.clients.swing.common.menuitems.MenuRemove();
         landUseTypes = new org.sola.clients.beans.referencedata.LandUseTypeListBean();
         popupPrints = new javax.swing.JPopupMenu();
         menuOfferLetter = new javax.swing.JMenuItem();
         menuRejectionLetter = new javax.swing.JMenuItem();
         menuLease = new javax.swing.JMenuItem();
         menuLeaseSurrender = new javax.swing.JMenuItem();
         menuLeaseVary = new javax.swing.JMenuItem();
         menuEndorseSuccession = new javax.swing.JMenuItem();
         headerPanel = new org.sola.clients.swing.ui.HeaderPanel();
         jToolBar1 = new javax.swing.JToolBar();
         btnSave = new javax.swing.JButton();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
         jSeparator1 = new javax.swing.JToolBar.Separator();
         dropDownButton2 = new org.sola.clients.swing.common.controls.DropDownButton();
         jSeparator2 = new javax.swing.JToolBar.Separator();
         jLabel1 = new javax.swing.JLabel();
         lblStatus = new javax.swing.JLabel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel10 = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         groupPanel1 = new org.sola.clients.swing.ui.GroupPanel();
         partyList = createPartyListPanel();
         jPanel3 = new javax.swing.JPanel();
         groupPanel2 = new org.sola.clients.swing.ui.GroupPanel();
         documentsManagementPanel = createDocumentsPanel();
         jPanel21 = new javax.swing.JPanel();
         jPanel12 = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         txtLeaseNumber = new javax.swing.JTextField();
         jPanel14 = new javax.swing.JPanel();
         jLabel9 = new javax.swing.JLabel();
         txtStartDate = new javax.swing.JFormattedTextField();
         btnStartDate = new javax.swing.JButton();
         jPanel26 = new javax.swing.JPanel();
         jLabel17 = new javax.swing.JLabel();
         txtLeaseTerm = new javax.swing.JFormattedTextField();
         jPanel7 = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         txtExpirationDate = new javax.swing.JFormattedTextField();
         btnExpirationDate = new javax.swing.JButton();
         jPanel15 = new javax.swing.JPanel();
         jLabel10 = new javax.swing.JLabel();
         txtExecutionDate = new javax.swing.JFormattedTextField();
         btnExecutionDate = new javax.swing.JButton();
         jPanel24 = new javax.swing.JPanel();
         jLabel14 = new javax.swing.JLabel();
         cbxLandUse = new javax.swing.JComboBox();
         jPanel20 = new javax.swing.JPanel();
         jLabel15 = new javax.swing.JLabel();
         txtPersonalLevy = new javax.swing.JFormattedTextField();
         jPanel25 = new javax.swing.JPanel();
         jLabel16 = new javax.swing.JLabel();
         txtUsableLand = new javax.swing.JFormattedTextField();
         jPanel8 = new javax.swing.JPanel();
         jLabel6 = new javax.swing.JLabel();
         txtRent = new javax.swing.JFormattedTextField();
         btnCalculateGroundRent = new javax.swing.JButton();
         jPanel6 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         txtDueDate = new javax.swing.JFormattedTextField();
         btnNextPaymentDate = new javax.swing.JButton();
         jPanel11 = new javax.swing.JPanel();
         jToolBar3 = new javax.swing.JToolBar();
         btnAddCodition = new org.sola.clients.swing.common.buttons.BtnAdd();
         btnEditCondition = new org.sola.clients.swing.common.buttons.BtnEdit();
         btnRemoveCondition = new org.sola.clients.swing.common.buttons.BtnRemove();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTableWithDefaultStyles1 = new org.sola.clients.swing.common.controls.JTableWithDefaultStyles();
         jPanel22 = new javax.swing.JPanel();
         jPanel19 = new javax.swing.JPanel();
         groupPanel3 = new org.sola.clients.swing.ui.GroupPanel();
         jPanel9 = new javax.swing.JPanel();
         jPanel16 = new javax.swing.JPanel();
         jLabel11 = new javax.swing.JLabel();
         txtStampDuty = new javax.swing.JFormattedTextField();
         jPanel18 = new javax.swing.JPanel();
         jLabel13 = new javax.swing.JLabel();
         txtRegistrationFee = new javax.swing.JFormattedTextField();
         jPanel17 = new javax.swing.JPanel();
         jLabel12 = new javax.swing.JLabel();
         txtTransferDuty = new javax.swing.JFormattedTextField();
         jPanel23 = new javax.swing.JPanel();
         jPanel5 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         txtRegDatetime = new javax.swing.JFormattedTextField();
         btnRegistrationDate = new javax.swing.JButton();
         jPanel13 = new javax.swing.JPanel();
         jLabel8 = new javax.swing.JLabel();
         txtRegistrationNumber = new javax.swing.JTextField();
         jPanel4 = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         txtNotationText = new javax.swing.JTextField();
         subplotTabPanel = new javax.swing.JPanel();
         jToolBar2 = new javax.swing.JToolBar();
         btnAddSubplot = new org.sola.clients.swing.common.buttons.BtnAdd();
         btnRemoveSubplot = new org.sola.clients.swing.common.buttons.BtnRemove();
         parcelPanel1 = new org.sola.clients.swing.ui.cadastre.ParcelPanel();
         mapTabPanel = new javax.swing.JPanel();
 
         menuAddCondition.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuAddConditionActionPerformed(evt);
             }
         });
         popupConditions.add(menuAddCondition);
 
         menuEditCondition.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuEditConditionActionPerformed(evt);
             }
         });
         popupConditions.add(menuEditCondition);
 
         menuRemoveCondition.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuRemoveConditionActionPerformed(evt);
             }
         });
         popupConditions.add(menuRemoveCondition);
 
         menuOfferLetter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/print.png"))); // NOI18N
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sola/clients/swing/desktop/administrative/Bundle"); // NOI18N
         menuOfferLetter.setText(bundle.getString("LeasePanel.menuOfferLetter.text")); // NOI18N
         menuOfferLetter.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuOfferLetterActionPerformed(evt);
             }
         });
         popupPrints.add(menuOfferLetter);
 
         menuRejectionLetter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/print.png"))); // NOI18N
         menuRejectionLetter.setText(bundle.getString("LeasePanel.menuRejectionLetter.text")); // NOI18N
         menuRejectionLetter.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuRejectionLetterActionPerformed(evt);
             }
         });
         popupPrints.add(menuRejectionLetter);
 
         menuLease.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/print.png"))); // NOI18N
         menuLease.setText(bundle.getString("LeasePanel.menuLease.text")); // NOI18N
         menuLease.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuLeaseActionPerformed(evt);
             }
         });
         popupPrints.add(menuLease);
 
         menuLeaseSurrender.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/print.png"))); // NOI18N
         menuLeaseSurrender.setText(bundle.getString("LeasePanel.menuLeaseSurrender.text")); // NOI18N
         menuLeaseSurrender.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuLeaseSurrenderActionPerformed(evt);
             }
         });
         popupPrints.add(menuLeaseSurrender);
 
         menuLeaseVary.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/print.png"))); // NOI18N
         menuLeaseVary.setText(bundle.getString("LeasePanel.menuLeaseVary.text")); // NOI18N
         menuLeaseVary.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuLeaseVaryActionPerformed(evt);
             }
         });
         popupPrints.add(menuLeaseVary);
 
         menuEndorseSuccession.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/print.png"))); // NOI18N
         menuEndorseSuccession.setText(bundle.getString("LeasePanel.menuEndorseSuccession.text")); // NOI18N
         menuEndorseSuccession.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuEndorseSuccessionActionPerformed(evt);
             }
         });
         popupPrints.add(menuEndorseSuccession);
 
         setHeaderPanel(headerPanel);
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentShown(java.awt.event.ComponentEvent evt) {
                 formComponentShown(evt);
             }
         });
 
         headerPanel.setTitleText(bundle.getString("SimpleOwhershipPanel.headerPanel.titleText")); // NOI18N
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
 
         btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/save.png"))); // NOI18N
         btnSave.setText(bundle.getString("SimpleOwhershipPanel.btnSave.text")); // NOI18N
         btnSave.setFocusable(false);
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
         jToolBar1.add(btnSave);
         jToolBar1.add(filler1);
         jToolBar1.add(jSeparator1);
 
         dropDownButton2.setText(bundle.getString("LeasePanel.dropDownButton2.text")); // NOI18N
         dropDownButton2.setComponentPopupMenu(popupPrints);
         dropDownButton2.setFocusable(false);
         dropDownButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         dropDownButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(dropDownButton2);
         jToolBar1.add(jSeparator2);
 
         jLabel1.setText(bundle.getString("SimpleOwhershipPanel.jLabel1.text")); // NOI18N
         jToolBar1.add(jLabel1);
 
         lblStatus.setFont(LafManager.getInstance().getLabFontBold());
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${status.displayValue}"), lblStatus, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jToolBar1.add(lblStatus);
 
         jPanel1.setLayout(new java.awt.GridLayout(2, 1, 0, 10));
 
         groupPanel1.setTitleText(bundle.getString("SimpleOwhershipPanel.groupPanel1.titleText")); // NOI18N
 
         partyList.setShowSearchButton(false);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(groupPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
             .addComponent(partyList, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addComponent(groupPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(partyList, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel2);
 
         groupPanel2.setTitleText(bundle.getString("SimpleOwhershipPanel.groupPanel2.titleText")); // NOI18N
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(groupPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
             .addComponent(documentsManagementPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addComponent(groupPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(documentsManagementPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel3);
 
         jPanel21.setLayout(new java.awt.GridLayout(2, 5, 15, 15));
 
         jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel7.setText(bundle.getString("LeasePanel.jLabel7.text")); // NOI18N
 
         txtLeaseNumber.setEnabled(false);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${leaseNumber}"), txtLeaseNumber, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addComponent(jLabel7)
                 .addGap(0, 48, Short.MAX_VALUE))
             .addComponent(txtLeaseNumber)
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addComponent(jLabel7)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtLeaseNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel21.add(jPanel12);
 
         jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel9.setText(bundle.getString("LeasePanel.jLabel9.text")); // NOI18N
 
         txtStartDate.setFormatterFactory(FormattersFactory.getInstance().getDateFormatterFactory());
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${startDate}"), txtStartDate, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnStartDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnStartDate.setText(bundle.getString("LeasePanel.btnStartDate.text")); // NOI18N
         btnStartDate.setBorder(null);
         btnStartDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStartDateActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
         jPanel14.setLayout(jPanel14Layout);
         jPanel14Layout.setHorizontalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addComponent(jLabel9)
                 .addGap(0, 66, Short.MAX_VALUE))
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addComponent(txtStartDate)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnStartDate))
         );
         jPanel14Layout.setVerticalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addComponent(jLabel9)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnStartDate)
                     .addComponent(txtStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         jPanel21.add(jPanel14);
 
         jLabel17.setText(bundle.getString("LeasePanel.jLabel17.text")); // NOI18N
 
         txtLeaseTerm.setFormatterFactory(FormattersFactory.getInstance().getIntegerFormatterFactory());
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${leaseTerm}"), txtLeaseTerm, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
         jPanel26.setLayout(jPanel26Layout);
         jPanel26Layout.setHorizontalGroup(
             jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel26Layout.createSequentialGroup()
                 .addComponent(jLabel17)
                 .addGap(0, 37, Short.MAX_VALUE))
             .addComponent(txtLeaseTerm)
         );
         jPanel26Layout.setVerticalGroup(
             jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel26Layout.createSequentialGroup()
                 .addComponent(jLabel17)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtLeaseTerm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel21.add(jPanel26);
 
         jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel5.setText(bundle.getString("LeasePanel.jLabel5.text")); // NOI18N
 
         txtExpirationDate.setFormatterFactory(FormattersFactory.getInstance().getDateFormatterFactory());
         txtExpirationDate.setText(bundle.getString("LeasePanel.txtExpirationDate.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${expirationDate}"), txtExpirationDate, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnExpirationDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnExpirationDate.setText(bundle.getString("LeasePanel.btnExpirationDate.text")); // NOI18N
         btnExpirationDate.setBorder(null);
         btnExpirationDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExpirationDateActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addComponent(txtExpirationDate)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnExpirationDate))
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addComponent(jLabel5)
                 .addGap(0, 42, Short.MAX_VALUE))
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnExpirationDate)
                     .addComponent(txtExpirationDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         jPanel21.add(jPanel7);
 
         jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel10.setText(bundle.getString("LeasePanel.jLabel10.text")); // NOI18N
 
         txtExecutionDate.setFormatterFactory(FormattersFactory.getInstance().getDateFormatterFactory());
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${executionDate}"), txtExecutionDate, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnExecutionDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnExecutionDate.setText(bundle.getString("LeasePanel.btnExecutionDate.text")); // NOI18N
         btnExecutionDate.setBorder(null);
         btnExecutionDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExecutionDateActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
         jPanel15.setLayout(jPanel15Layout);
         jPanel15Layout.setHorizontalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel15Layout.createSequentialGroup()
                 .addComponent(jLabel10)
                 .addGap(0, 43, Short.MAX_VALUE))
             .addGroup(jPanel15Layout.createSequentialGroup()
                 .addComponent(txtExecutionDate)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnExecutionDate))
         );
         jPanel15Layout.setVerticalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel15Layout.createSequentialGroup()
                 .addComponent(jLabel10)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnExecutionDate)
                     .addComponent(txtExecutionDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         jPanel21.add(jPanel15);
 
         jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel14.setText(bundle.getString("LeasePanel.jLabel14.text")); // NOI18N
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${landUseTypeList}");
         org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, landUseTypes, eLProperty, cbxLandUse);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${landUseType}"), cbxLandUse, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
         jPanel24.setLayout(jPanel24Layout);
         jPanel24Layout.setHorizontalGroup(
             jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel24Layout.createSequentialGroup()
                 .addComponent(jLabel14)
                 .addGap(0, 72, Short.MAX_VALUE))
             .addComponent(cbxLandUse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel24Layout.setVerticalGroup(
             jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel24Layout.createSequentialGroup()
                 .addComponent(jLabel14)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(cbxLandUse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel21.add(jPanel24);
 
         jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel15.setText(bundle.getString("LeasePanel.jLabel15.text")); // NOI18N
 
         txtPersonalLevy.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
         txtPersonalLevy.setToolTipText(bundle.getString("LeasePanel.txtPersonalLevy.toolTipText")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${personalLevy}"), txtPersonalLevy, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
         jPanel20.setLayout(jPanel20Layout);
         jPanel20Layout.setHorizontalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel20Layout.createSequentialGroup()
                 .addComponent(jLabel15)
                 .addGap(0, 49, Short.MAX_VALUE))
             .addComponent(txtPersonalLevy)
         );
         jPanel20Layout.setVerticalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel20Layout.createSequentialGroup()
                 .addComponent(jLabel15)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtPersonalLevy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel21.add(jPanel20);
 
         jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel16.setText(bundle.getString("LeasePanel.jLabel16.text")); // NOI18N
 
         txtUsableLand.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
         txtUsableLand.setToolTipText(bundle.getString("LeasePanel.txtUsableLand.toolTipText")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${landUsable}"), txtUsableLand, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
         jPanel25.setLayout(jPanel25Layout);
         jPanel25Layout.setHorizontalGroup(
             jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel25Layout.createSequentialGroup()
                 .addComponent(jLabel16)
                 .addGap(0, 36, Short.MAX_VALUE))
             .addComponent(txtUsableLand)
         );
         jPanel25Layout.setVerticalGroup(
             jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel25Layout.createSequentialGroup()
                 .addComponent(jLabel16)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtUsableLand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel21.add(jPanel25);
 
         jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel6.setText(bundle.getString("LeasePanel.jLabel6.text")); // NOI18N
 
         txtRent.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
         txtRent.setText(bundle.getString("LeasePanel.txtRent.text_1")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${groundRent}"), txtRent, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnCalculateGroundRent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calculator.png"))); // NOI18N
         btnCalculateGroundRent.setText(bundle.getString("LeasePanel.btnCalculateGroundRent.text")); // NOI18N
         btnCalculateGroundRent.setToolTipText(bundle.getString("LeasePanel.btnCalculateGroundRent.toolTipText")); // NOI18N
         btnCalculateGroundRent.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCalculateGroundRentActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel8Layout.createSequentialGroup()
                 .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                     .addComponent(txtRent))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnCalculateGroundRent, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0))
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(btnCalculateGroundRent, javax.swing.GroupLayout.Alignment.TRAILING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                 .addComponent(jLabel6)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtRent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel21.add(jPanel8);
 
         jLabel4.setText(bundle.getString("LeasePanel.jLabel4.text")); // NOI18N
 
         txtDueDate.setFormatterFactory(FormattersFactory.getInstance().getDateFormatterFactory());
         txtDueDate.setText(bundle.getString("LeasePanel.txtDueDate.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${dueDate}"), txtDueDate, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnNextPaymentDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnNextPaymentDate.setText(bundle.getString("LeasePanel.btnNextPaymentDate.text")); // NOI18N
         btnNextPaymentDate.setBorder(null);
         btnNextPaymentDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnNextPaymentDateActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addComponent(jLabel4)
                 .addGap(0, 36, Short.MAX_VALUE))
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addComponent(txtDueDate)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnNextPaymentDate))
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addComponent(jLabel4)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnNextPaymentDate)
                     .addComponent(txtDueDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         jPanel21.add(jPanel6);
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, 713, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab(bundle.getString("LeasePanel.jPanel10.TabConstraints.tabTitle"), jPanel10); // NOI18N
 
         jToolBar3.setFloatable(false);
         jToolBar3.setRollover(true);
 
         btnAddCodition.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddCoditionActionPerformed(evt);
             }
         });
         jToolBar3.add(btnAddCodition);
 
         btnEditCondition.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnEditConditionActionPerformed(evt);
             }
         });
         jToolBar3.add(btnEditCondition);
 
         btnRemoveCondition.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveConditionActionPerformed(evt);
             }
         });
         jToolBar3.add(btnRemoveCondition);
 
         jTableWithDefaultStyles1.setComponentPopupMenu(popupConditions);
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${filteredLeaseSpecialConditionList}");
         org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, eLProperty, jTableWithDefaultStyles1);
         org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${conditionText}"));
         columnBinding.setColumnName("Condition Text");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${selectedSpecialCondition}"), jTableWithDefaultStyles1, org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
         bindingGroup.addBinding(binding);
 
         jScrollPane3.setViewportView(jTableWithDefaultStyles1);
         jTableWithDefaultStyles1.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("LeasePanel.jTableWithDefaultStyles1.columnModel.title0")); // NOI18N
         jTableWithDefaultStyles1.getColumnModel().getColumn(0).setCellRenderer(new TableCellTextAreaRenderer());
 
         javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab(bundle.getString("LeasePanel.jPanel11.TabConstraints.tabTitle"), jPanel11); // NOI18N
 
         groupPanel3.setTitleText(bundle.getString("LeasePanel.groupPanel3.titleText")); // NOI18N
 
         jPanel9.setLayout(new java.awt.GridLayout(1, 5, 15, 0));
 
         jLabel11.setText(bundle.getString("LeasePanel.jLabel11.text")); // NOI18N
 
         txtStampDuty.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
         txtStampDuty.setText(bundle.getString("LeasePanel.txtStampDuty.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${stampDuty}"), txtStampDuty, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
         jPanel16.setLayout(jPanel16Layout);
         jPanel16Layout.setHorizontalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel16Layout.createSequentialGroup()
                 .addComponent(jLabel11)
                 .addGap(0, 82, Short.MAX_VALUE))
             .addComponent(txtStampDuty)
         );
         jPanel16Layout.setVerticalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel16Layout.createSequentialGroup()
                 .addComponent(jLabel11)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtStampDuty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel9.add(jPanel16);
 
         jLabel13.setText(bundle.getString("LeasePanel.jLabel13.text")); // NOI18N
 
         txtRegistrationFee.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${registrationFee}"), txtRegistrationFee, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
         jPanel18.setLayout(jPanel18Layout);
         jPanel18Layout.setHorizontalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel18Layout.createSequentialGroup()
                 .addComponent(jLabel13)
                 .addGap(0, 59, Short.MAX_VALUE))
             .addComponent(txtRegistrationFee)
         );
         jPanel18Layout.setVerticalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel18Layout.createSequentialGroup()
                 .addComponent(jLabel13)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtRegistrationFee, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel9.add(jPanel18);
 
         jLabel12.setText(bundle.getString("LeasePanel.jLabel12.text")); // NOI18N
 
         txtTransferDuty.setFormatterFactory(FormattersFactory.getInstance().getDecimalFormatterFactory());
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${transferDuty}"), txtTransferDuty, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
         jPanel17.setLayout(jPanel17Layout);
         jPanel17Layout.setHorizontalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel17Layout.createSequentialGroup()
                 .addComponent(jLabel12)
                 .addGap(0, 71, Short.MAX_VALUE))
             .addComponent(txtTransferDuty)
         );
         jPanel17Layout.setVerticalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel17Layout.createSequentialGroup()
                 .addComponent(jLabel12)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtTransferDuty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel9.add(jPanel17);
 
         javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
         jPanel19.setLayout(jPanel19Layout);
         jPanel19Layout.setHorizontalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(groupPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(jPanel19Layout.createSequentialGroup()
                 .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 266, Short.MAX_VALUE))
         );
         jPanel19Layout.setVerticalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel19Layout.createSequentialGroup()
                 .addComponent(groupPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 13, Short.MAX_VALUE))
         );
 
         jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel2.setText(bundle.getString("SimpleOwhershipPanel.jLabel2.text")); // NOI18N
 
         txtRegDatetime.setFormatterFactory(FormattersFactory.getInstance().getDateFormatterFactory());
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${registrationDate}"), txtRegDatetime, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnRegistrationDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnRegistrationDate.setText(bundle.getString("LeasePanel.btnRegistrationDate.text")); // NOI18N
         btnRegistrationDate.setBorder(null);
         btnRegistrationDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRegistrationDateActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addComponent(jLabel2)
                 .addContainerGap(67, Short.MAX_VALUE))
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addComponent(txtRegDatetime)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnRegistrationDate))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtRegDatetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnRegistrationDate)))
         );
 
         jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel8.setText(bundle.getString("LeasePanel.jLabel8.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${registrationNumber}"), txtRegistrationNumber, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addComponent(jLabel8)
                 .addGap(0, 19, Short.MAX_VALUE))
             .addComponent(txtRegistrationNumber)
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addComponent(jLabel8)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtRegistrationNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel3.setText(bundle.getString("SimpleOwhershipPanel.jLabel3.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rrrBean, org.jdesktop.beansbinding.ELProperty.create("${notation.notationText}"), txtNotationText, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(jLabel3)
                 .addGap(0, 0, Short.MAX_VALUE))
             .addComponent(txtNotationText)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtNotationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
         jPanel23.setLayout(jPanel23Layout);
         jPanel23Layout.setHorizontalGroup(
             jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel23Layout.createSequentialGroup()
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel23Layout.setVerticalGroup(
             jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
 
         javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
         jPanel22.setLayout(jPanel22Layout);
         jPanel22Layout.setHorizontalGroup(
             jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel22Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel22Layout.setVerticalGroup(
             jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel22Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(326, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(bundle.getString("LeasePanel.jPanel22.TabConstraints.tabTitle"), jPanel22); // NOI18N
 
         jToolBar2.setFloatable(false);
         jToolBar2.setRollover(true);
 
         btnAddSubplot.setText(bundle.getString("LeasePanel.btnAddSubplot.text")); // NOI18N
         btnAddSubplot.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnAddSubplot.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddSubplotActionPerformed(evt);
             }
         });
         jToolBar2.add(btnAddSubplot);
 
         btnRemoveSubplot.setText(bundle.getString("LeasePanel.btnRemoveSubplot.text")); // NOI18N
         btnRemoveSubplot.setToolTipText(bundle.getString("LeasePanel.btnRemoveSubplot.toolTipText")); // NOI18N
         btnRemoveSubplot.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnRemoveSubplot.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveSubplotActionPerformed(evt);
             }
         });
         jToolBar2.add(btnRemoveSubplot);
 
         javax.swing.GroupLayout subplotTabPanelLayout = new javax.swing.GroupLayout(subplotTabPanel);
         subplotTabPanel.setLayout(subplotTabPanelLayout);
         subplotTabPanelLayout.setHorizontalGroup(
             subplotTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(subplotTabPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(subplotTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(parcelPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE)
                     .addComponent(jToolBar2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         subplotTabPanelLayout.setVerticalGroup(
             subplotTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(subplotTabPanelLayout.createSequentialGroup()
                 .addGap(7, 7, 7)
                 .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(parcelPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(bundle.getString("LeasePanel.subplotTabPanel.TabConstraints.tabTitle"), subplotTabPanel); // NOI18N
 
         mapTabPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentShown(java.awt.event.ComponentEvent evt) {
                 mapTabPanelComponentShown(evt);
             }
         });
 
         javax.swing.GroupLayout mapTabPanelLayout = new javax.swing.GroupLayout(mapTabPanel);
         mapTabPanel.setLayout(mapTabPanelLayout);
         mapTabPanelLayout.setHorizontalGroup(
             mapTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 732, Short.MAX_VALUE)
         );
         mapTabPanelLayout.setVerticalGroup(
             mapTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 473, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab(bundle.getString("LeasePanel.mapTabPanel.TabConstraints.tabTitle"), mapTabPanel); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
             .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTabbedPane1)
                 .addContainerGap())
         );
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
         saveRrr();
     }//GEN-LAST:event_btnSaveActionPerformed
 
     private void btnRegistrationDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrationDateActionPerformed
         showCalendar(txtRegDatetime);
     }//GEN-LAST:event_btnRegistrationDateActionPerformed
 
     private void btnExpirationDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpirationDateActionPerformed
         showCalendar(txtExpirationDate);
     }//GEN-LAST:event_btnExpirationDateActionPerformed
 
     private void btnNextPaymentDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPaymentDateActionPerformed
         showCalendar(txtDueDate);
     }//GEN-LAST:event_btnNextPaymentDateActionPerformed
 
     private void btnStartDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartDateActionPerformed
         showCalendar(txtStartDate);
     }//GEN-LAST:event_btnStartDateActionPerformed
 
     private void btnExecutionDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExecutionDateActionPerformed
         showCalendar(txtExecutionDate);
     }//GEN-LAST:event_btnExecutionDateActionPerformed
 
     private void btnCalculateGroundRentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalculateGroundRentActionPerformed
         calculateGroundRent();
     }//GEN-LAST:event_btnCalculateGroundRentActionPerformed
 
     private void btnAddCoditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCoditionActionPerformed
         addCondition();
     }//GEN-LAST:event_btnAddCoditionActionPerformed
 
     private void btnRemoveConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveConditionActionPerformed
         removeCondition();
     }//GEN-LAST:event_btnRemoveConditionActionPerformed
 
     private void menuAddConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddConditionActionPerformed
         addCondition();
     }//GEN-LAST:event_menuAddConditionActionPerformed
 
     private void menuEditConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditConditionActionPerformed
         editCondition();
     }//GEN-LAST:event_menuEditConditionActionPerformed
 
     private void menuRemoveConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRemoveConditionActionPerformed
         removeCondition();
     }//GEN-LAST:event_menuRemoveConditionActionPerformed
 
     private void btnEditConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditConditionActionPerformed
         editCondition();
     }//GEN-LAST:event_btnEditConditionActionPerformed
 
     private void menuOfferLetterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOfferLetterActionPerformed
         printOfferLetter();
     }//GEN-LAST:event_menuOfferLetterActionPerformed
 
     private void menuLeaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLeaseActionPerformed
         printLease();
     }//GEN-LAST:event_menuLeaseActionPerformed
 
     private void menuRejectionLetterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRejectionLetterActionPerformed
         printRejectionLetter();
     }//GEN-LAST:event_menuRejectionLetterActionPerformed
 
     private void menuLeaseSurrenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLeaseSurrenderActionPerformed
         printLeaseSurrender();
     }//GEN-LAST:event_menuLeaseSurrenderActionPerformed
 
     private void menuLeaseVaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLeaseVaryActionPerformed
         printLeaseVary();
     }//GEN-LAST:event_menuLeaseVaryActionPerformed
 
     private void menuEndorseSuccessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEndorseSuccessionActionPerformed
         printSuccessionReport();
     }//GEN-LAST:event_menuEndorseSuccessionActionPerformed
 
     private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
         if (this.mapControl == null && isSublease()) {
             this.mapControl = new ControlsBundleForBaUnit();
             if (applicationBean != null) {
                 this.mapControl.setApplicationId(this.applicationBean.getId());
             }
             this.mapTabPanel.setLayout(new BorderLayout());
             this.mapTabPanel.add(this.mapControl, BorderLayout.CENTER);
         }
     }//GEN-LAST:event_formComponentShown
 
     private void mapTabPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mapTabPanelComponentShown
         if (isSublease()) {
             zoomToPlot();
         }
     }//GEN-LAST:event_mapTabPanelComponentShown
 
     private void btnAddSubplotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSubplotActionPerformed
         if (isSublease()) {
             addSubplot();
         }
     }//GEN-LAST:event_btnAddSubplotActionPerformed
 
     private void btnRemoveSubplotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveSubplotActionPerformed
         if (isSublease()) {
             removeSubplot();
         }
     }//GEN-LAST:event_btnRemoveSubplotActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.sola.clients.swing.common.buttons.BtnAdd btnAddCodition;
     private org.sola.clients.swing.common.buttons.BtnAdd btnAddSubplot;
     private javax.swing.JButton btnCalculateGroundRent;
     private org.sola.clients.swing.common.buttons.BtnEdit btnEditCondition;
     private javax.swing.JButton btnExecutionDate;
     private javax.swing.JButton btnExpirationDate;
     private javax.swing.JButton btnNextPaymentDate;
     private javax.swing.JButton btnRegistrationDate;
     private org.sola.clients.swing.common.buttons.BtnRemove btnRemoveCondition;
     private org.sola.clients.swing.common.buttons.BtnRemove btnRemoveSubplot;
     private javax.swing.JButton btnSave;
     private javax.swing.JButton btnStartDate;
     private javax.swing.JComboBox cbxLandUse;
     private org.sola.clients.swing.desktop.source.DocumentsManagementExtPanel documentsManagementPanel;
     private org.sola.clients.swing.common.controls.DropDownButton dropDownButton2;
     private javax.swing.Box.Filler filler1;
     private org.sola.clients.swing.ui.GroupPanel groupPanel1;
     private org.sola.clients.swing.ui.GroupPanel groupPanel2;
     private org.sola.clients.swing.ui.GroupPanel groupPanel3;
     private org.sola.clients.swing.ui.HeaderPanel headerPanel;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel15;
     private javax.swing.JPanel jPanel16;
     private javax.swing.JPanel jPanel17;
     private javax.swing.JPanel jPanel18;
     private javax.swing.JPanel jPanel19;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel20;
     private javax.swing.JPanel jPanel21;
     private javax.swing.JPanel jPanel22;
     private javax.swing.JPanel jPanel23;
     private javax.swing.JPanel jPanel24;
     private javax.swing.JPanel jPanel25;
     private javax.swing.JPanel jPanel26;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar.Separator jSeparator2;
     private javax.swing.JTabbedPane jTabbedPane1;
     private org.sola.clients.swing.common.controls.JTableWithDefaultStyles jTableWithDefaultStyles1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JToolBar jToolBar2;
     private javax.swing.JToolBar jToolBar3;
     private org.sola.clients.beans.referencedata.LandUseTypeListBean landUseTypes;
     private javax.swing.JLabel lblStatus;
     private javax.swing.JPanel mapTabPanel;
     private org.sola.clients.swing.common.menuitems.MenuAdd menuAddCondition;
     private org.sola.clients.swing.common.menuitems.MenuEdit menuEditCondition;
     private javax.swing.JMenuItem menuEndorseSuccession;
     private javax.swing.JMenuItem menuLease;
     private javax.swing.JMenuItem menuLeaseSurrender;
     private javax.swing.JMenuItem menuLeaseVary;
     private javax.swing.JMenuItem menuOfferLetter;
     private javax.swing.JMenuItem menuRejectionLetter;
     private org.sola.clients.swing.common.menuitems.MenuRemove menuRemoveCondition;
     private org.sola.clients.swing.ui.cadastre.ParcelPanel parcelPanel1;
     private org.sola.clients.swing.desktop.party.PartyListExtPanel partyList;
     private javax.swing.JPopupMenu popupConditions;
     private javax.swing.JPopupMenu popupPrints;
     private org.sola.clients.beans.administrative.RrrBean rrrBean;
     private javax.swing.JPanel subplotTabPanel;
     private javax.swing.JFormattedTextField txtDueDate;
     private javax.swing.JFormattedTextField txtExecutionDate;
     private javax.swing.JFormattedTextField txtExpirationDate;
     private javax.swing.JTextField txtLeaseNumber;
     private javax.swing.JFormattedTextField txtLeaseTerm;
     private javax.swing.JTextField txtNotationText;
     private javax.swing.JFormattedTextField txtPersonalLevy;
     private javax.swing.JFormattedTextField txtRegDatetime;
     private javax.swing.JFormattedTextField txtRegistrationFee;
     private javax.swing.JTextField txtRegistrationNumber;
     private javax.swing.JFormattedTextField txtRent;
     private javax.swing.JFormattedTextField txtStampDuty;
     private javax.swing.JFormattedTextField txtStartDate;
     private javax.swing.JFormattedTextField txtTransferDuty;
     private javax.swing.JFormattedTextField txtUsableLand;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 }
