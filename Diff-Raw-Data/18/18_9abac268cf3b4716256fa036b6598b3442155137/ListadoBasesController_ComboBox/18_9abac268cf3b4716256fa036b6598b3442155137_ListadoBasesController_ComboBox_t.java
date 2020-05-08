 /*
  * Copyright (c) 2012, 2013 Oracle and/or its affiliates.
  * All rights reserved. Use is subject to license terms.
  *
  * This file is available and licensed under the following license:
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *  - Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *  - Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the distribution.
  *  - Neither the name of Oracle nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package presentacion;
 
 import excepciones.DAOExcepcion;
 import excepciones.DominioExcepcion;
 import java.awt.Dialog;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Scene;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableColumn.CellEditEvent;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.Tooltip;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.control.cell.TextFieldTableCell;
 import javafx.scene.layout.Pane;
 import javafx.stage.Stage;
 import jfx.messagebox.MessageBox;
 import logica.Aditivo;
 import logica.Base;
 import logica.Pigmento;
 import logica.Controlador;
 import logica.Pigmento;
 
 
 public class ListadoBasesController_ComboBox implements Initializable, ControlledScreen{
    ScreensController myController;
     @FXML private static TableView<Aditivo> tableView;
     private String cantidad;
     Base base;
     Aditivo aditivo;
     @FXML
     private TextField txtCantidad;
     //@FXML
     //private Label lblErrorCantidad;
     @FXML private ChoiceBox elegirBase,aditivosChoiceBox;
     
    @FXML
     public void eliminarAditivoBase(ActionEvent event) throws DAOExcepcion, DominioExcepcion{
         
     
         }
 
     public void anyadirAditivoBase(ActionEvent event) throws IOException, DAOExcepcion, DominioExcepcion {
         Stage stage=new Stage();
         boolean error=false;
          Controlador controlador= Controlador.dameControlador();
        cantidad=txtCantidad.getText();
        base=controlador.getBase(elegirBase.getSelectionModel().getSelectedItem().toString());
        aditivo=(Aditivo)aditivosChoiceBox.getSelectionModel().getSelectedItem();
     if(cantidad.equals(""))
              error=true;
             if(!error){
            aditivo.setCantidad(Double.parseDouble(cantidad));
            controlador.asociarAditivo(base, aditivo);
            cargarAditivos(base.getId());
             }
             
     }
     
     @Override
     public void initialize (URL location,ResourceBundle resources){
        elegirBase.getSelectionModel().selectedIndexProperty().addListener(new
             ChangeListener<Number>() {
                 public void changed(ObservableValue ov,
                     Number value, Number new_value) {
                     try {
                         //  label.setText(greetings[new_value.intValue()]);
                       cargarAditivos(((Base)elegirBase.getItems().get(new_value.intValue())).getId());
                     } catch (DAOExcepcion ex) {
                         Logger.getLogger(ListadoBasesController_ComboBox.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
         });
 
        elegirBase.setItems(null);
         tableView.setEditable(true);
         tableView.setMaxWidth(400);
         TableColumn Id=new TableColumn("Id");
         TableColumn Cantidad=new TableColumn("Cantidad");
         Id.setMinWidth(100);
         Cantidad.setMinWidth(200);
       Id.setCellValueFactory(new PropertyValueFactory<Pigmento,String>("Id"));
       Cantidad.setCellValueFactory(new PropertyValueFactory<Pigmento,String>("Cantidad"));
       tableView.getColumns().addAll(Id,Cantidad);
       tableView.setTableMenuButtonVisible(true);
       cargarBases();
    // Cantidad.setCellFactory(TextFieldTableCell.forTableColumn());
    /* Cantidad.setOnEditCommit(
     new EventHandler<CellEditEvent<Pigmento, String>>() {
         @Override
         public void handle(CellEditEvent<Pigmento, String> t) {
                Controlador controlador = null;
                Pigmento pigmento=null;
                Stage stage=new Stage();
         try {
             controlador=controlador.dameControlador();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoPigmentosController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DominioExcepcion ex) {
             Logger.getLogger(ListadoPigmentosController.class.getName()).log(Level.SEVERE, null, ex);
         }
             try {
                 pigmento=controlador.getPigmento(t.getNewValue());
             } catch (DAOExcepcion ex) {
                 Logger.getLogger(ListadoPigmentosController.class.getName()).log(Level.SEVERE, null, ex);
             }
             try {
                 this.finalize();
             } catch (Throwable ex) {
                 Logger.getLogger(ListadoPigmentosController.class.getName()).log(Level.SEVERE, null, ex);
             }
              if(t.getNewValue().trim().equals("")){
                 int answer = MessageBox.show(stage,
 						"El cantidad del pigmento no  puede estar vacio",
 						"Information dialog", 
 						MessageBox.ICON_INFORMATION | MessageBox.OK);
                 cargar();
             }
             if(pigmento==null && !t.getNewValue().trim().equals("")){
                ((Pigmento) t.getTableView().getItems().get(
                 t.getTablePosition().getRow())
                 ).setCantidad(t.getNewValue());
                Pigmento pigmentoA=  ((Pigmento) t.getTableView().getItems().get(
                 t.getTablePosition().getRow()));
                pigmentoA.setCantidad(t.getNewValue().trim());
                    try {
                        controlador.modificarPigmento(pigmentoA);
                    } catch (DAOExcepcion ex) {
                        Logger.getLogger(ListadoPigmentosController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                cargar();
             }
            
             if(pigmento!= null && !t.getNewValue().trim().equals("")){
                 int answer = MessageBox.show(stage,
 						"Ese pigmento ya existe, introduzca un cantidad distinto",
 						"Information dialog", 
 						MessageBox.ICON_INFORMATION | MessageBox.OK);
                 cargar();
     
             }
       
         }
     }
 );*/
     
         }
     
     
     public void cargarAditivos(int idb) throws DAOExcepcion{
         
                     Controlador controlador = null;
 
   ArrayList<Aditivo> aditivos=new ArrayList<Aditivo>();
 
         try {
             controlador = Controlador.dameControlador();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DominioExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             aditivos=controlador.getAditivos(idb);
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
                   
        ObservableList<Aditivo> aditivosConvertidosParaTabla = FXCollections.observableList(aditivos);  
     
       ArrayList<Aditivo> aditivosChoiceBoxx = controlador.getAditivosNoAsociados(idb);
        /*
        
        ObservableList<Aditivo> aditivosOVChoiceBox = null;
        if(aditivos.size()>0){
        for(Aditivo aditivoOV : aditivosTodos)
            for(Aditivo aditivoOV2 : aditivos)
            if(!aditivoOV.getCantidad().equals(aditivoOV2.getCantidad()))aditivosChoiceBoxx.add(aditivoOV);
        
        aditivosOVChoiceBox = FXCollections.observableList(aditivosChoiceBoxx); 
        }else{
           
        aditivosOVChoiceBox = FXCollections.observableList(aditivosTodos); 
        }
        */
       ObservableList<Aditivo>  aditivosOVChoiceBox = FXCollections.observableList(aditivosChoiceBoxx); 
        aditivosChoiceBox.setItems(aditivosOVChoiceBox);
        tableView.setItems(aditivosConvertidosParaTabla);
     }
     
     public  void cargarBases(){
         
                     Controlador controlador = null;
 
   ArrayList<Base> bases=new ArrayList<Base>();
 
         try {
             controlador = Controlador.dameControlador();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DominioExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             bases=controlador.getBases();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
                   
        ObservableList<Base> bases2 = FXCollections.observableList(bases);  
        elegirBase.setItems(bases2);
  
     }
     
 
     @Override
     public void setScreenParent(ScreensController screenPage) {
         myController=screenPage;
     }
   
 }
