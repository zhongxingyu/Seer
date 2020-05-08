 /*
  * Copyright (C) 2013 AXIA Studio (http://www.axiastudio.com)
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
  * You should have received a copy of the GNU Afffero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.suite.procedimenti.forms;
 
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.db.Controller;
 import com.axiastudio.pypapi.db.IController;
 import com.axiastudio.pypapi.db.Store;
 import com.axiastudio.pypapi.ui.Dialog;
 import com.axiastudio.pypapi.ui.widgets.PyPaPiComboBox;
 import com.axiastudio.suite.AdminConsole;
 import com.axiastudio.suite.procedimenti.entities.CodiceCarica;
 import com.axiastudio.suite.procedimenti.entities.FaseProcedimento;
 import com.axiastudio.suite.procedimenti.entities.Procedimento;
 import com.sun.deploy.util.StringUtils;
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.gui.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author AXIA Studio (http://www.axiastudio.com)
  */
 public class FormFaseProcedimento extends Dialog {
 
     private QListWidget caricheAbilitate;
     private QListWidget caricheDisponibili;
 
     public FormFaseProcedimento(String uiFile, Class entityClass) {
         this(uiFile, entityClass, "");
     }
 
     public FormFaseProcedimento(String uiFile, Class entityClass, String title) {
         super(uiFile, entityClass, title);
 
         QPushButton test = (QPushButton) this.findChild(QPushButton.class, "pushButton_console");
         test.clicked.connect(this, "openConsole()");
         this.storeInitialized.connect(this, "storeConfermataRifiutata()");
 
         QToolButton aggiungiCarica = (QToolButton) this.findChild(QToolButton.class, "aggiungiCarica");
         aggiungiCarica.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui//resources/toolbar/resultset_previous.png"));
         aggiungiCarica.clicked.connect(this, "aggiungiCarica()");
         QToolButton rimuoviCarica = (QToolButton) this.findChild(QToolButton.class, "rimuoviCarica");
         rimuoviCarica.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/resultset_next.png"));
         rimuoviCarica.clicked.connect(this, "rimuoviCarica()");
 
         caricheDisponibili = (QListWidget) this.findChild(QListWidget.class, "caricheDisponibili");
         caricheAbilitate = (QListWidget) this.findChild(QListWidget.class, "caricheAbilitate");
 
         this.storeInitialized.connect(this, "inizializzaCariche()");
 
     }
 
     private void inizializzaCariche() {
         FaseProcedimento faseProcedimento = (FaseProcedimento) this.getContext().getCurrentEntity();
 
         for( CodiceCarica codiceCarica: CodiceCarica.values() ){
             QListWidgetItem item = new QListWidgetItem();
             item.setText(codiceCarica.name());
             caricheDisponibili.addItem(item);
         }
 
         if( faseProcedimento.getCariche() != null ){
             for( String token: faseProcedimento.getCariche().split(",") ){
                 CodiceCarica codiceCarica = CodiceCarica.valueOf(token);
                 QListWidgetItem item = new QListWidgetItem();
                 item.setText(codiceCarica.name());
                 caricheAbilitate.addItem(item);
             }
         }
     }
 
 
     /*
      * Uno store contenente solo le fasi del procedimento
      */
     public void storeConfermataRifiutata(){
         FaseProcedimento fp = (FaseProcedimento) this.getContext().getCurrentEntity();
         Store store = new Store(fp.getProcedimento().getFaseProcedimentoCollection());
         PyPaPiComboBox confermata = (PyPaPiComboBox) this.findChild(PyPaPiComboBox.class, "comboBox_confermata");
         confermata.setLookupStore(store);
         this.getColumn("Confermata").setLookupStore(store);
         confermata.select(fp.getConfermata());
         PyPaPiComboBox rifiutata = (PyPaPiComboBox) this.findChild(PyPaPiComboBox.class, "comboBox_rifiutata");
         rifiutata.setLookupStore(store);
         this.getColumn("Rifiutata").setLookupStore(store);
         rifiutata.select(fp.getRifiutata());
     }
 
 
     private void openConsole(){
         String entityName = ((QLineEdit) this.findChild(QLineEdit.class, "lineEdit_entita")).text();
         String entityId = ((QLineEdit) this.findChild(QLineEdit.class, "lineEdit_id")).text();
         Long id = Long.parseLong(entityId);
         Controller controller = (Controller) Register.queryUtility(IController.class, entityName);
         Object obj = controller.get(id);
         FaseProcedimento faseProcedimento = (FaseProcedimento) this.getContext().getCurrentEntity();
         Procedimento procedimento = faseProcedimento.getProcedimento();
 
         // Apertura della console
         AdminConsole console = new AdminConsole(this, obj);
         console.show();
 
     }
 
     private void aggiungiCarica(){
         FaseProcedimento faseProcedimento = (FaseProcedimento) this.getContext().getCurrentEntity();
         String cariche = faseProcedimento.getCariche();
         QModelIndex index = caricheDisponibili.currentIndex();
         QListWidgetItem item = caricheDisponibili.item(index.row());
         String testoCarica = item.text();
         if( !Arrays.asList(cariche.split(",")).contains(testoCarica) ){
             QListWidgetItem newItem = new QListWidgetItem();
             newItem.setText(testoCarica);
             caricheAbilitate.addItem(newItem);
         }
         aggiornaCariche();
     }
 
     private void rimuoviCarica(){
         QModelIndex index = caricheAbilitate.currentIndex();
         QListWidgetItem item = caricheAbilitate.takeItem(index.row());
         caricheAbilitate.removeItemWidget(item);
         aggiornaCariche();
     }
 
     private void aggiornaCariche(){
         List<String> listCariche = new ArrayList<String>();
         for( Integer i=0; i<caricheAbilitate.count(); i++ ){
             QListWidgetItem item = caricheAbilitate.item(i);
             listCariche.add(item.text());
         }
         String cariche = StringUtils.join(listCariche, ",");
         FaseProcedimento faseProcedimento = (FaseProcedimento) this.getContext().getCurrentEntity();
         faseProcedimento.setCariche(cariche);
         this.getContext().getDirty();
     }
 
 }
