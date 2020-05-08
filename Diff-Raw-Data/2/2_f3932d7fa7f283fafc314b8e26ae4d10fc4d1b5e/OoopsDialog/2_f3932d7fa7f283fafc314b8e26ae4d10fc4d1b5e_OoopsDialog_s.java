 /*
  * Copyright (C) 2012 AXIA Studio (http://www.axiastudio.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.suite.plugins.ooops;
 
 import com.axiastudio.pypapi.ui.Util;
 import com.axiastudio.pypapi.ui.Window;
 import com.trolltech.qt.core.QFile;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.designer.QUiLoader;
 import com.trolltech.qt.designer.QUiLoaderException;
 import com.trolltech.qt.gui.*;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tiziano
  */
 public class OoopsDialog extends QDialog {
     
     private final Object currentEntity;
     private final List<Template> templates;
     private final OoopsHelper helper;
 
     public OoopsDialog(QWidget parent, OoopsHelper helper, List<Template> templates) {
         super(parent);
         this.currentEntity = ((Window) parent).getContext().getCurrentEntity();
         this.templates = templates;
         this.helper = helper;
         QFile file = Util.ui2jui(new QFile("classpath:com/axiastudio/suite/plugins/ooops/ooopsdialog.ui"));
         this.loadUi(file);
 
         this.refreshList();
     }
 
     private void loadUi(QFile uiFile){
         QDialog dialog = null;
         try {
             dialog = (QDialog) QUiLoader.load(uiFile);
         } catch (QUiLoaderException ex) {
             Logger.getLogger(OoopsDialog.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.setLayout(dialog.layout());
         this.setModal(true);
 
         QToolButton toolButtonCompose = (QToolButton) this.findChild(QToolButton.class, "toolButtonCompose");
         toolButtonCompose.setIcon(new QIcon("classpath:com/axiastudio/suite/resources/page_white_add.png"));
         toolButtonCompose.clicked.connect(this, "compose()");
 
         QToolButton toolButtonPreparaProtocollo = (QToolButton) this.findChild(QToolButton.class, "toolButtonPreparaProtocollo");
         toolButtonPreparaProtocollo.setIcon(new QIcon("classpath:com/axiastudio/suite/resources/email.png"));
         toolButtonPreparaProtocollo.clicked.connect(this, "preparaProtocollo()");
 
         QToolButton uploadModel = (QToolButton) this.findChild(QToolButton.class, "toolButtonUploadModel");
         uploadModel.setIcon(new QIcon("classpath:com/axiastudio/suite/resources/upload.png"));
         //uploadModel.clicked.connect(this, "upload()");
 
         QToolButton downloadModel = (QToolButton) this.findChild(QToolButton.class, "toolButtonDownloadModel");
         downloadModel.setIcon(new QIcon("classpath:com/axiastudio/suite/resources/download.png"));
         //downloadModel.clicked.connect(this, "upload()");
 
         QToolButton deleteModel = (QToolButton) this.findChild(QToolButton.class, "toolButtonDeleteModel");
         deleteModel.setIcon(new QIcon("classpath:com/axiastudio/suite/resources/delete.png"));
         //deleteModel.clicked.connect(this, "upload()");
 
         QToolButton editRules = (QToolButton) this.findChild(QToolButton.class, "toolButtonEditRules");
         editRules.setIcon(new QIcon("classpath:com/axiastudio/suite/resources/page_white_database.png"));
         //editRules.clicked.connect(this, "upload()");
 
 
         QTableWidget qtw = (QTableWidget) this.findChild(QTableWidget.class, "tableWidget");
         qtw.verticalHeader().hide();
         qtw.setShowGrid(false);
         qtw.setAlternatingRowColors(true);
         qtw.setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
         qtw.setSortingEnabled(true);
         qtw.horizontalHeader().setResizeMode(QHeaderView.ResizeMode.Interactive);
 
         // file type
         QComboBox fileType = (QComboBox) this.findChild(QComboBox.class, "comboBoxFileType");
         fileType.addItem(new QIcon("classpath:com/axiastudio/suite/resources/page_odt.png"),
                 "Open Document (odt)", "writer8");
         fileType.addItem(new QIcon("classpath:com/axiastudio/suite/resources/page_doc.png"),
                 "Microsoft Word (doc)", "writer_MS_Word_97");
         fileType.addItem(new QIcon("classpath:com/axiastudio/suite/resources/page_pdf.png"),
                 "Acrobat PDF (pdf)", "writer_pdf_Export");
 
     }
 
 
     private void refreshList(){
         QTableWidget qtw = (QTableWidget) this.findChild(QTableWidget.class, "tableWidget");
         while( qtw.rowCount() > 0 ){
             qtw.removeRow(0);
         }
         qtw.setColumnCount(3);
         List<String> labels = new ArrayList();
         labels.add("title");
         labels.add("description");
         labels.add("parent");
         qtw.setHorizontalHeaderLabels(labels);
         Integer i = 0;
         for( Template template: this.templates ){
             qtw.insertRow(i);
             QTableWidgetItem itemName = new QTableWidgetItem(template.getName());
             itemName.setData(Qt.ItemDataRole.UserRole, i);
             itemName.setFlags(Qt.ItemFlag.ItemIsSelectable, Qt.ItemFlag.ItemIsEnabled);
             qtw.setItem(i, 0, itemName);
             QTableWidgetItem itemDescription = new QTableWidgetItem(template.getDescription());
             itemDescription.setFlags(Qt.ItemFlag.ItemIsSelectable, Qt.ItemFlag.ItemIsEnabled);
             qtw.setItem(i, 1, itemDescription);
             String parentTemplateName = "";
             if( template.getParentTemplateName() != null ){
                 parentTemplateName = template.getParentTemplateName();
             }
             QTableWidgetItem itemParent = new QTableWidgetItem(parentTemplateName);
             itemParent.setFlags(Qt.ItemFlag.ItemIsSelectable, Qt.ItemFlag.ItemIsEnabled);
             qtw.setItem(i, 2, itemParent);
             i++;
         }
     }
 
     protected OoopsHelper getHelper() {
         return helper;
     }
 
 
     private void compose(){
 
         // template
         QTableWidget qtw = (QTableWidget) this.findChild(QTableWidget.class, "tableWidget");
         QTableWidgetItem item = qtw.item(qtw.currentRow(), 0);
         //QTableWidgetItem item = qtw.currentItem();
         Integer i = (Integer) item.data(Qt.ItemDataRole.UserRole);
         Template template = this.templates.get(i);
         String parentTemplateName = template.getName();
         if( template.getParentTemplateName() != null ){
             for( Template t: this.templates ){
                 if( t.getName().equals(template.getParentTemplateName()) ){
                     template.setRuleSet(t.getRuleSet());
                     parentTemplateName = t.getName();
                     break;
                 }
             }
         }
 
         // filter
         QComboBox fileType = (QComboBox) this.findChild(QComboBox.class, "comboBoxFileType");
         String filter = (String) fileType.itemData(fileType.currentIndex());
 
         // mime type
         String mimeType = null;
         if( filter.equals("writer_pdf_Export") ){
             mimeType = "application/pdf";
         } else if( filter.equals("writer8") ){
             mimeType = "application/vnd.oasis.opendocument.text";
         } else if( filter.equals("writer_MS_Word_97") ){
             mimeType = "application/msword";
         }
 
 
         composeFromTemplate(template);
         // TODO: post compose handler
         if( IDocumentFolder.class.isInstance(this.parent()) ){
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             helper.storeDocumentComponent(outputStream, filter);
             // XXX: maybe is better to use streams?
             ((IDocumentFolder) this.parent()).createDocument("", template.getName(), "Autocomposizione", parentTemplateName, outputStream.toByteArray(), mimeType);
         }
         this.close();
     }
 
 
     /*
      *  Elabora i documenti per l'inserimento nel protocollo
      */
     private void preparaProtocollo(){
 
         // i documenti da allegare al protocollo sono convertiti in pdf
         String mimeType = "application/pdf";
         String filter = "writer_pdf_Export";
 
         QTableWidget qtw = (QTableWidget) this.findChild(QTableWidget.class, "tableWidget");
         for( int y=0; y<qtw.rowCount(); y++ ){
             QTableWidgetItem item = qtw.item(y, 0);
             Integer i = (Integer) item.data(Qt.ItemDataRole.UserRole);
             Template template = this.templates.get(i);
             Boolean protocollabile=false;
             String parentTemplateName = template.getName();
             if( template.getName().endsWith(".odt") ){
                 if( template.getParentTemplateName() != null ){
                     for( Template t: this.templates ){
                         if( t.getName().equals(template.getParentTemplateName()) ){
                             //template.setRuleSet(t.getRuleSet());
                             parentTemplateName = t.getName();
                             protocollabile=true;
                             break;
                         }
                     }
                 }
                 // se protocollabile aggiungo le regole
                 if( protocollabile ){
                     HashMap<String, String> rules = new HashMap<String, String>();
                     rules.put("numeroprotocollo", "{ obj -> obj.protocollo ? obj.protocollo.iddocumento : \"YYYYNNNNNNNN\" }");
                    rules.put("dataprotocollo", "{ obj -> obj.protocollo ? obj.protocollo.dataprotocollo : \"GG/MM/YYY\"}");
                     RuleSet ruleSetProtocollo = new RuleSet(rules);
                     template.setRuleSet(ruleSetProtocollo);
                 }
                 composeFromTemplate(template);
                 if( IDocumentFolder.class.isInstance(this.parent()) ){
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                     helper.storeDocumentComponent(outputStream, filter);
                     // XXX: maybe is better to use streams?
                     ((IDocumentFolder) this.parent()).createDocument("protocollo", template.getName(), "Protocollo", parentTemplateName, outputStream.toByteArray(), mimeType);
                 }
 
             }
         }
         this.close();
 
     }
 
 
     protected void composeFromTemplate(Template template) {
         Object entity = ((Window) this.parent()).getContext().getCurrentEntity();
         Map<String, Object> values = template.getRuleSet().eval(entity);
         this.helper.loadDocumentComponent(template.getStreamProvider().getInputStream());
         this.helper.composeDocument(values);
         //this.helper.close();
     }
 }
