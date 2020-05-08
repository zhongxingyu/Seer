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
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.Menu;
 import javafx.scene.control.MenuItem;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableColumn.CellEditEvent;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.Tooltip;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.control.cell.TextFieldTableCell;
 import javafx.scene.input.MouseEvent;
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
     @FXML private Button btnEliminar;
     @FXML private Label lblErrorCantidad,lblErrorBase,lblErrorAditivo;
     @FXML private TextField txtCantidad,txtEliminarAditivo,txtEliminarCantidad;
     @FXML private ComboBox elegirBase;
      @FXML private ComboBox aditivosChoiceBox;
          @FXML MenuItem listadoAcabado;
             @FXML MenuItem listadoPigmento;
                     @FXML MenuItem listadoAditivo;
                             @FXML MenuItem listadoBase;
                             @FXML MenuItem listadoProducto;
     @FXML MenuItem aditivoABase;
     @FXML MenuItem baseAPigmento;
     @FXML Menu calcular;
     @FXML Menu inicio;
     Stage stage;
     
    @FXML
     public void eliminarAditivoBase(ActionEvent event) throws DAOExcepcion, DominioExcepcion{
        Controlador controlador= Controlador.dameControlador();
        base=base=controlador.getBase(elegirBase.getSelectionModel().getSelectedItem().toString());
        aditivo=tableView.getSelectionModel().getSelectedItem();
        int answer= MessageBox.show(stage,
         "Estas seguro de que quieres eliminar este aditivo?",
         "Information dialog",
         MessageBox.ICON_INFORMATION | MessageBox.OK | MessageBox.CANCEL);
         System.out.println(answer);
         if(answer==65536){
             controlador.eliminaAsociacionAditivo(base, aditivo);
            cargarAditivos(base.getId());
             txtEliminarAditivo.setText("");
             txtEliminarCantidad.setText("");
             btnEliminar.setDisable(true);
         }  
         }
 
     public void anyadirAditivoBase(ActionEvent event) throws IOException, DAOExcepcion, DominioExcepcion {
         Stage stage=new Stage();
         boolean error=false;
          Controlador controlador= Controlador.dameControlador();
        cantidad=txtCantidad.getText().trim().replace(',', '.');
        base=controlador.getBase(elegirBase.getSelectionModel().getSelectedItem().toString());
        aditivo=(Aditivo)aditivosChoiceBox.getSelectionModel().getSelectedItem();
        
         if(base==null){
            error=true;
            lblErrorBase.setText("Selecciona una Base");
        }else{
           lblErrorBase.setText("");
        }
         if(aditivo==null){
            error=true;
            lblErrorAditivo.setText("Selecciona un Aditivo");
        }else{
           lblErrorAditivo.setText("");
        }
      if(cantidad.equals("")){
    lblErrorCantidad.setText("Introduce cantidad");
    error=true;
     }else{
     lblErrorCantidad.setText(""); 
    }
        for(char x: cantidad.toCharArray()){
             if(Character.isLetter(x)){
                 error=true;
                 lblErrorCantidad.setText("No introducir letras");
                 txtCantidad.setText("");
                 break;
             }else{
                lblErrorCantidad.setText(""); 
             }
             } 
             if(!error){
            aditivo.setCantidad(Double.parseDouble(cantidad));
            controlador.asociarAditivo(base, aditivo);
            cargarAditivos(base.getId());
            txtCantidad.setText("");
             }
             
     }
     
     @Override
     public void initialize (URL location,ResourceBundle resources){
         btnEliminar.setDisable(false);
       tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {

             @Override
             public void handle(MouseEvent t) {
                 Aditivo aditivo= tableView.getSelectionModel().getSelectedItem();
                if(base!=null){
                     txtEliminarAditivo.setText(aditivo.getNombre());
                     txtEliminarCantidad.setText(aditivo.getCantidad().toString());
                     btnEliminar.setDisable(false);
                 }
             }
         });
         
         stage = ObjetoCompartido.dameLo().getStage();
           
        listadoAcabado.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoAcabados.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
   
         stage.setTitle("Acabados");
         stage.setScene(new Scene(root));
         stage.show();
            }
        });
        
        aditivoABase.setDisable(true);
        listadoAditivo.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoAditivos.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
  
         stage.setTitle("Aditivos");
         stage.setScene(new Scene(root));
         stage.show();
            }
        });
        listadoProducto.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoProductos.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
 
         stage.setTitle("Productos");
         stage.setScene(new Scene(root));
         stage.show();
            }
        });
        listadoBase.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoBases.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
  
         stage.setTitle("Bases");
         stage.setScene(new Scene(root));
         stage.show();
            }
        });
        baseAPigmento.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoPigmento_ComboBox.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
  
         stage.setTitle("Pigmento a Base");
         stage.setScene(new Scene(root));
         stage.show();
            }
        });
       listadoPigmento.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoPigmentos.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
 
         stage.setTitle("Pigmentos");
         stage.setScene(new Scene(root));
         stage.show();
            }
        });
       
       inicio.setOnShown(new EventHandler<Event>() {
 
           @Override
            public void handle(Event t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("Principal.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
     
         stage.setTitle("Inicio");
         stage.setScene(new Scene(root));
         stage.show();
         inicio.hide();
            }
        });
  
       calcular.setOnShown(new EventHandler<Event>() {
 
           @Override
            public void handle(Event t) {
 
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("Calcular.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
 
         stage.setTitle("Calcular");
         calcular.hide();
         stage.setScene(new Scene(root));
         stage.show();
         inicio.hide();
            }
        });
         
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
         tableView.setMaxWidth(520);
         TableColumn Nombre=new TableColumn("Nombre");
         TableColumn Cantidad=new TableColumn("Cantidad");
         Nombre.setMinWidth(320);
         Cantidad.setMinWidth(200);
       Nombre.setCellValueFactory(new PropertyValueFactory<Aditivo,String>("Nombre"));
       Cantidad.setCellValueFactory(new PropertyValueFactory<Aditivo,String>("Cantidad"));
       tableView.getColumns().addAll(Nombre,Cantidad);
       cargarBases();    
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
