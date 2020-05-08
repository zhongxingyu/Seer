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
 import java.text.DecimalFormat;
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
 import javafx.scene.layout.Pane;
 import javafx.stage.Stage;
 import jfx.messagebox.MessageBox;
 import logica.Acabado;
 import logica.Aditivo;
 import logica.Base;
 import logica.Pigmento;
 import logica.Controlador;
 import logica.Pigmento;
 import logica.Producto;
 import static presentacion.ListadoProductosController.cargar;
 
 
 public class CalcularController implements Initializable, ControlledScreen{
    ScreensController myController;
     @FXML private static TableView<Base> tableView;
     private String comprobarCantidad;
     private Double cantidad;
     Producto producto;
     Acabado acabado;
     Pigmento pigmento;
     @FXML private TextField txtCantidad;
     @FXML private ComboBox comboProducto,comboAcabado,comboPigmento;
     @FXML private Label lblErrorAcabado,lblErrorProducto,lblErrorCantidad,lblErrorPigmento;
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
     public void calcular(ActionEvent event) throws DAOExcepcion, DominioExcepcion{
     ArrayList<Base> basesResultado=new ArrayList<Base>();
     boolean correcto=true;
     pigmento=(Pigmento)comboPigmento.getSelectionModel().getSelectedItem();
     producto=(Producto)comboProducto.getSelectionModel().getSelectedItem();
     acabado=(Acabado)comboAcabado.getSelectionModel().getSelectedItem();
     comprobarCantidad=txtCantidad.getText().trim().replace(',', '.');
         for(char x: comprobarCantidad.toCharArray()){
             if(Character.isLetter(x)){
                 correcto=false;
                 lblErrorCantidad.setText("No se pueden introducir letras");
                 txtCantidad.setText("");
                 break;
             }else{
                lblErrorCantidad.setText(""); 
             }
             } 
         
    if(comprobarCantidad.equals("")){
    lblErrorCantidad.setText("Introduce cantidad");
    correcto=false;
     }else{
     lblErrorCantidad.setText(""); 
    }
     if(pigmento==null){
         lblErrorPigmento.setText("Selecciona pigmento");
         correcto=false;
     }else{
         lblErrorPigmento.setText("");
     }
     if(acabado==null){
         lblErrorAcabado.setText("Selecciona acabado");
         correcto=false;
     }else{
         lblErrorAcabado.setText("");
     }
     if(producto==null){
         lblErrorProducto.setText("Selecciona producto");
         correcto=false;
     }else{
         lblErrorProducto.setText("");
     }
         
     if(correcto){
     cantidad=Double.parseDouble(comprobarCantidad);
     ArrayList<Base> bases=getBases(pigmento);
     for(int i=0;i<bases.size();i++){
         basesResultado.add(calculo(bases.get(i), pigmento, acabado, producto, cantidad));
     }
    ObservableList<Base> basesTabla = FXCollections.observableList(basesResultado);  
        tableView.setItems(basesTabla);
     }
     }
    
     public Base calculo(Base _base,Pigmento _pigmento,Acabado _acabado,Producto _producto,Double _cantidad){
        Double factor=Double.parseDouble(_producto.getFactor())*Double.parseDouble(_acabado.getFactor())*_base.getPorcentaje();
        Double factorAditivos=_base.getFactorBase();
        Double resultado100g=factor+factor*factorAditivos;
        Double resultadParaCantidadSeleccionada=(resultado100g*_cantidad)/0.1;
        Base baseResultado=_base;
        baseResultado.setPorcentaje(Math.round(resultadParaCantidadSeleccionada*100)/100d);
         return baseResultado;
         
     }
  
    public ArrayList<Base> getBases(Pigmento _pigmento) throws DAOExcepcion, DominioExcepcion{
        Controlador controlador=Controlador.dameControlador();
        ArrayList<Base> bases=controlador.getBases(_pigmento.getId());
        return bases;
    }
    
     
     @Override
     public void initialize (URL location,ResourceBundle resources){
         
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
        
        calcular.setDisable(true);
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
        aditivoABase.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent t) {
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoBases_ComboBox.fxml"));
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
         inicio.hide();
         stage.setScene(new Scene(root));
         stage.show();
  
            }
        });
  
       baseAPigmento.setOnAction(new EventHandler<ActionEvent>() {
 
           @Override
            public void handle(ActionEvent t) {
 
                Parent root=null;
                try {
                    root = FXMLLoader.load(getClass().getResource("ListadoBases_ComboBox.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
                }
 
         stage.setTitle("Base a Pigmento");
         stage.setScene(new Scene(root));
         stage.show();
            }
        });
            
          tableView.setEditable(true);
         tableView.setMaxWidth(400);
         TableColumn Nombre=new TableColumn("Base");
         TableColumn Cantidad=new TableColumn("Cantidad(g)");
        Cantidad.setMinWidth(150);
         Nombre.setMinWidth(200);
       Nombre.setCellValueFactory(new PropertyValueFactory<Base,String>("Nombre"));
       Cantidad.setCellValueFactory(new PropertyValueFactory("Porcentaje"));
       tableView.getColumns().addAll(Nombre,Cantidad);
        try {
            cargarPigmentos();
        } catch (DAOExcepcion ex) {
            Logger.getLogger(CalcularController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            cargarAcabados();
        } catch (DAOExcepcion ex) {
            Logger.getLogger(CalcularController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            cargarProductos();
        } catch (DAOExcepcion ex) {
            Logger.getLogger(CalcularController.class.getName()).log(Level.SEVERE, null, ex);
        }
         }
     
     
     public void cargarPigmentos() throws DAOExcepcion{
         
                     Controlador controlador = null;
 
   ArrayList<Pigmento> pigmentos=new ArrayList<Pigmento>();
 
         try {
             controlador = Controlador.dameControlador();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DominioExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             pigmentos=controlador.getPigmentos();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
           ObservableList<Pigmento> pigmento = FXCollections.observableList(pigmentos);
           comboPigmento.setItems(pigmento);
     }
      public void cargarAcabados() throws DAOExcepcion{
         
                     Controlador controlador = null;
 
   ArrayList<Acabado> acabados=new ArrayList<Acabado>();
 
         try {
             controlador = Controlador.dameControlador();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DominioExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             acabados=controlador.getAcabados();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
           ObservableList<Acabado> acabado = FXCollections.observableList(acabados);
           comboAcabado.setItems(acabado);
     }
       public void cargarProductos() throws DAOExcepcion{
         
                     Controlador controlador = null;
 
   ArrayList<Producto> productos=new ArrayList<Producto>();
 
         try {
             controlador = Controlador.dameControlador();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DominioExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             productos=controlador.getProductos();
         } catch (DAOExcepcion ex) {
             Logger.getLogger(ListadoBasesController.class.getName()).log(Level.SEVERE, null, ex);
         }
           ObservableList<Producto> producto = FXCollections.observableList(productos);
           comboProducto.setItems(producto);
     }
     
 
     @Override
     public void setScreenParent(ScreensController screenPage) {
         myController=screenPage;
     }
   
 }
