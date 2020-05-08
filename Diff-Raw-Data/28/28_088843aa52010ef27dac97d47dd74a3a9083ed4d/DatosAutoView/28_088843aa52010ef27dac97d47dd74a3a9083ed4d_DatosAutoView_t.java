 /**
  *   Copyright 2012-2013 Nekorp
  *
  *Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License
  */
 package org.nekorp.workflow.desktop.view;
 
 
 import java.awt.Dimension;
 import java.awt.event.KeyEvent;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.JFrame;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.nekorp.workflow.desktop.control.ControlAuto;
 import org.nekorp.workflow.desktop.modelo.auto.Auto;
 import org.nekorp.workflow.desktop.rest.util.Callback;
 import org.nekorp.workflow.desktop.view.binding.Bindable;
 import org.nekorp.workflow.desktop.view.binding.BindableListModel;
 import org.nekorp.workflow.desktop.view.binding.BindingManager;
 import org.nekorp.workflow.desktop.view.model.auto.TipoElevadorVB;
 import org.nekorp.workflow.desktop.view.model.auto.TipoTransmisionVB;
 import org.nekorp.workflow.desktop.view.model.servicio.ServicioVB;
 import org.nekorp.workflow.desktop.view.model.validacion.ValidacionDatosAuto;
 import org.nekorp.workflow.desktop.view.model.validacion.ValidacionGeneralDatosAuto;
 import org.nekorp.workflow.desktop.view.resource.IconProvider;
 import org.nekorp.workflow.desktop.view.resource.imp.AutoSearchJListModel;
 import org.nekorp.workflow.desktop.view.resource.imp.DocumentSizeValidatorMayusculas;
 
 /**
  *
  * 
  */
 public class DatosAutoView extends ApplicationView {
     /**
      * el supuesto size de cada renglon en la lista de busqueda de clientes.
      */
     private int renglonSearchSize;
     /**
      * el numero de renglones visibles en la lista de busqueda.
      */
     private int renglonesVisiblesSearch;
     /**
      * por que no...
      * para que no se activen los scrolls
      */
     private int constanteUniversalDeAjuste;
     private AutoSearchJListModel searchModel;
     private ControlAuto aplication;
     private boolean activo;
     //se utiliza para ignorar aciones de update
     private int updateIgnoreCount;
     
     private javax.swing.JFrame mainFrame;
     private BindingManager<Bindable> bindingManager;
     private ServicioVB viewServicioModel;
     private ValidacionDatosAuto validacionDatosAuto;
     private ValidacionGeneralDatosAuto validacionGeneralDatosAuto;
     private IconProvider iconProvider;
     private String searchIconRaw;
     private String cancelSearchIconRaw;
     private String validacionOkIconRaw;
     private String validacionErrorIconRaw;
     
     private BindableListModel<String> modelEquipoAdicional;
     
     @Override
     public void setEditableStatus(boolean value) {
         activo = value;
        this.wrapperSearch.setEditable(value);
        this.searchIcon.setVisible(value);
        this.cancelIcon.setVisible(value);
        
         this.marca.setEditable(value);
         this.validacionMarca.setVisible(value);
         this.version.setEditable(value);
         this.validacionVersion.setVisible(value);
         this.modelo.setEditable(value);
         this.validacionModelo.setVisible(value);
         this.placas.setEditable(value);
         this.validacionPlacas.setVisible(value);
         this.tipo.setEditable(value);
         this.validacionTipo.setVisible(value);
         this.numeroSerie.setEditable(value);
         this.validacionSerie.setVisible(value);
         this.color.setEditable(value);
         this.validacionColor.setVisible(value);
         this.kilometraje.setEditable(value);
         this.validacionKilometraje.setVisible(value);
         this.descripcionServicio.setEnabled(value);
         this.validacionDescripcionServicio.setVisible(value);
         this.combustible.setEditable(value);
         this.combustibleSlide.setEnabled(value);
         this.estandar.setEnabled(value);
         this.automatico.setEnabled(value);
         this.manuales.setEnabled(value);
         this.electrico.setEnabled(value);
         this.bolsasDeAire.setEditable(value);
         this.aireAcondicionado.setEnabled(value);
         this.agregarEquipoAdicional.setEnabled(value);
         this.borrarEquipoAdicional.setEnabled(value);
         this.equipoAdicional.setEnabled(value);
     }
     /**
      * Creates new form DatosAuto
      */
     public DatosAutoView() {
         modelEquipoAdicional = new BindableListModel<>();
     }
     
     @Override
     public void iniciaVista() {
         initComponents();
         bindComponents();
         searchModel = new AutoSearchJListModel();        
         search.setModel(searchModel);
         //esto resuelve el problema de regresar el foco a la caja de texto.
         search.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if(search.hasFocus()) {
                     numeroSerie.requestFocus();
                 }
             }
         });
         this.numeroSerie.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 actualizarNumeroSerie();
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 actualizarNumeroSerie();
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 actualizarNumeroSerie();
             }
         });
         this.cancelIcon.add(iconProvider.getIcon(cancelSearchIconRaw));
         this.searchIcon.add(iconProvider.getIcon(searchIconRaw));
     }
     
     private void actualizarNumeroSerie() {
         if (!activo) {
             return;
         }
         if (updateIgnoreCount > 0) {
             updateIgnoreCount = updateIgnoreCount - 1;
             return;
         }
         aplication.buscarAuto(numeroSerie.getText(), new Callback<List<Auto>>() {
             @Override
             public void execute(List<Auto> param) {
                 actualizaListaSearch(param);
             }
         });
     }
     
     private void actualizaListaSearch(List<Auto> data) {
         searchModel.updateData(data);
         search.removeSelectionInterval(search.getSelectedIndex(), search.getSelectedIndex());
         if (!searchScroll.isVisible() && this.numeroSerie.hasFocus()) {
             searchScroll.setVisible(true);
         }
         int nuevaAltura = renglonSearchSize * search.getModel().getSize();
         if (nuevaAltura > 0) {
             nuevaAltura = nuevaAltura + constanteUniversalDeAjuste;
         }
         if (nuevaAltura > renglonSearchSize * renglonesVisiblesSearch + constanteUniversalDeAjuste) {
             nuevaAltura = renglonSearchSize * renglonesVisiblesSearch + constanteUniversalDeAjuste;
         }
         searchScroll.setSize(new Dimension(170,nuevaAltura));
     }
     
     private void calculaNuevaPosicionScroll(int indexSeleccion, int verticalScrollValue) {
         int diferencia = (indexSeleccion * renglonSearchSize) - searchScroll.getVerticalScrollBar().getValue();
         if (diferencia < renglonSearchSize * renglonesVisiblesSearch && diferencia > 0) {
             return; //si aun esta en una zona visible
         }
         this.searchScroll.getVerticalScrollBar().setValue(verticalScrollValue);
     }
     
     private void calculaNuevaPosicionScrollDown(int indexSeleccion) {
         int verticalScrollValue = (indexSeleccion - renglonesVisiblesSearch + 1) * renglonSearchSize;
         if (verticalScrollValue < this.searchScroll.getVerticalScrollBar().getMinimum()) {
             verticalScrollValue = this.searchScroll.getVerticalScrollBar().getMinimum();
         }
         if (verticalScrollValue > this.searchScroll.getVerticalScrollBar().getMaximum()) {
             verticalScrollValue = this.searchScroll.getVerticalScrollBar().getMaximum();
         }
         calculaNuevaPosicionScroll(indexSeleccion, verticalScrollValue);
     }
     
     private void calculaNuevaPosicionScrollUp(int indexSeleccion) {
         int verticalScrollValue = indexSeleccion * renglonSearchSize;
         if (verticalScrollValue < this.searchScroll.getVerticalScrollBar().getMinimum()) {
             verticalScrollValue = this.searchScroll.getVerticalScrollBar().getMinimum();
         }
         if (verticalScrollValue > this.searchScroll.getVerticalScrollBar().getMaximum()) {
             verticalScrollValue = this.searchScroll.getVerticalScrollBar().getMaximum();
         }
         calculaNuevaPosicionScroll(indexSeleccion, verticalScrollValue);
     }
     
     public void bindComponents() {
         bindingManager.registerBind(viewServicioModel, "descripcion", (Bindable)this.descripcionServicio);
         
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"marca", (Bindable)marca);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"tipo", (Bindable)tipo);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"version", (Bindable)version);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"numeroSerie", (Bindable)numeroSerie);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"modelo", (Bindable)modelo);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"color", (Bindable)color);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"placas", (Bindable)placas);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"kilometraje", (Bindable)kilometraje);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"combustible", (Bindable)combustible);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto(),"combustible", (Bindable)combustibleSlide);
         
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto().getEquipamiento(),"transmision", (Bindable)estandar);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto().getEquipamiento(),"transmision", (Bindable)automatico);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto().getEquipamiento(),"elevadores", (Bindable)manuales);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto().getEquipamiento(),"elevadores", (Bindable)electrico);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto().getEquipamiento(),"bolsasDeAire", (Bindable)bolsasDeAire);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto().getEquipamiento(),"aireAcondicionado", (Bindable)aireAcondicionado);
         this.bindingManager.registerBind(viewServicioModel.getDatosAuto().getEquipamiento(),"equipoAdicional", (Bindable)modelEquipoAdicional);
         
         //binding validaciones
         this.bindingManager.registerBind(validacionDatosAuto, "marca", (Bindable)validacionMarca);
         this.bindingManager.registerBind(validacionDatosAuto, "tipo", (Bindable)validacionTipo);
         this.bindingManager.registerBind(validacionDatosAuto, "version", (Bindable)validacionVersion);
         this.bindingManager.registerBind(validacionDatosAuto, "numeroSerie", (Bindable)validacionSerie);
         this.bindingManager.registerBind(validacionDatosAuto, "modelo", (Bindable)validacionModelo);
         this.bindingManager.registerBind(validacionDatosAuto, "color", (Bindable)validacionColor);
         this.bindingManager.registerBind(validacionDatosAuto, "placas", (Bindable)validacionPlacas);
         this.bindingManager.registerBind(validacionDatosAuto, "kilometraje", (Bindable)validacionKilometraje);
         this.bindingManager.registerBind(validacionDatosAuto, "descripcionServicio", (Bindable)validacionDescripcionServicio);
     }
     
     @Override
     public ViewValidIndicator getValidInidicator() {
         return validacionGeneralDatosAuto;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         grupoTransmision = new javax.swing.ButtonGroup();
         grupoElevadores = new javax.swing.ButtonGroup();
         jLayeredPane1 = new javax.swing.JLayeredPane();
         jLabel1 = new javax.swing.JLabel();
         marca = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         jLabel2 = new javax.swing.JLabel();
         tipo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         jLabel4 = new javax.swing.JLabel();
         numeroSerie = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         ((javax.swing.text.AbstractDocument)numeroSerie.getDocument()).setDocumentFilter(new DocumentSizeValidatorMayusculas(17));
         searchIcon = new javax.swing.JPanel();
         cancelIcon = new javax.swing.JPanel();
        wrapperSearch = new javax.swing.JTextField();
         searchScroll = new javax.swing.JScrollPane();
         search = new javax.swing.JList();
         jLabel3 = new javax.swing.JLabel();
         version = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         jLabel5 = new javax.swing.JLabel();
         modelo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         jLabel6 = new javax.swing.JLabel();
         color = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         jLabel7 = new javax.swing.JLabel();
         placas = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         ((javax.swing.text.AbstractDocument)placas.getDocument()).setDocumentFilter(new DocumentSizeValidatorMayusculas(10));
         jLabel9 = new javax.swing.JLabel();
         kilometraje = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         jLabel10 = new javax.swing.JLabel();
         combustible = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         combustibleSlide = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJSlider();
         validacionMarca = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         validacionVersion = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         validacionModelo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         validacionPlacas = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         validacionTipo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         validacionSerie = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         validacionColor = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         validacionKilometraje = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jScrollPane1 = new javax.swing.JScrollPane();
         descripcionServicio = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextArea();
         validacionDescripcionServicio = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         datosEquipamiento = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         estandar = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJRadioButton(TipoTransmisionVB.estandar);
         automatico = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJRadioButton(TipoTransmisionVB.automatico);
         jPanel3 = new javax.swing.JPanel();
         manuales = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJRadioButton(TipoElevadorVB.manuales);
         electrico = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJRadioButton(TipoElevadorVB.electricos);
         aireAcondicionado = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJCheckBox();
         jLabel8 = new javax.swing.JLabel();
         jPanel4 = new javax.swing.JPanel();
         jToolBar2 = new javax.swing.JToolBar();
         agregarEquipoAdicional = new javax.swing.JButton();
         borrarEquipoAdicional = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         equipoAdicional = new javax.swing.JList();
         bolsasDeAire = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         jLabel11 = new javax.swing.JLabel();
 
         jLabel1.setText("Marca:");
         jLabel1.setBounds(320, 10, 33, 14);
         jLayeredPane1.add(jLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
         marca.setBounds(390, 10, 170, 20);
         jLayeredPane1.add(marca, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel2.setText("Tipo:");
         jLabel2.setBounds(10, 40, 24, 14);
         jLayeredPane1.add(jLabel2, javax.swing.JLayeredPane.DEFAULT_LAYER);
         tipo.setBounds(100, 40, 170, 20);
         jLayeredPane1.add(tipo, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel4.setText("Número de serie:");
         jLabel4.setBounds(10, 10, 82, 14);
         jLayeredPane1.add(jLabel4, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         numeroSerie.setBorder(null);
         numeroSerie.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 numeroSerieActionPerformed(evt);
             }
         });
         numeroSerie.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 numeroSerieFocusLost(evt);
             }
         });
         numeroSerie.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 numeroSerieKeyPressed(evt);
             }
         });
         numeroSerie.setBounds(101, 12, 137, 16);
         jLayeredPane1.add(numeroSerie, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         searchIcon.setOpaque(false);
         searchIcon.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 searchIconMouseClicked(evt);
             }
         });
         searchIcon.setLayout(new java.awt.BorderLayout());
         searchIcon.setBounds(236, 12, 16, 16);
         jLayeredPane1.add(searchIcon, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         cancelIcon.setOpaque(false);
         cancelIcon.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 cancelIconMouseClicked(evt);
             }
         });
         cancelIcon.setLayout(new java.awt.BorderLayout());
         cancelIcon.setBounds(252, 12, 16, 16);
         jLayeredPane1.add(cancelIcon, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
        wrapperSearch.setText("jTextField1");
        wrapperSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wrapperSearchFocusGained(evt);
            }
        });
        wrapperSearch.setBounds(100, 10, 170, 20);
        jLayeredPane1.add(wrapperSearch, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         searchScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 204)));
         searchScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         searchScroll.setPreferredSize(new java.awt.Dimension(170, 130));
 
         search.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 searchMouseClicked(evt);
             }
         });
         searchScroll.setViewportView(search);
 
         searchScroll.setBounds(100, 30, 170, 0);
         jLayeredPane1.add(searchScroll, javax.swing.JLayeredPane.POPUP_LAYER);
 
         jLabel3.setText("Versión:");
         jLabel3.setBounds(320, 40, 39, 14);
         jLayeredPane1.add(jLabel3, javax.swing.JLayeredPane.DEFAULT_LAYER);
         version.setBounds(390, 40, 170, 20);
         jLayeredPane1.add(version, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel5.setText("Modelo:");
         jLabel5.setBounds(10, 70, 38, 14);
         jLayeredPane1.add(jLabel5, javax.swing.JLayeredPane.DEFAULT_LAYER);
         modelo.setBounds(100, 70, 170, 20);
         jLayeredPane1.add(modelo, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel6.setText("Color:");
         jLabel6.setBounds(320, 70, 29, 14);
         jLayeredPane1.add(jLabel6, javax.swing.JLayeredPane.DEFAULT_LAYER);
         color.setBounds(390, 70, 170, 20);
         jLayeredPane1.add(color, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel7.setText("Placas:");
         jLabel7.setBounds(10, 100, 34, 14);
         jLayeredPane1.add(jLabel7, javax.swing.JLayeredPane.DEFAULT_LAYER);
         placas.setBounds(100, 100, 170, 20);
         jLayeredPane1.add(placas, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel9.setText("Kilometraje:");
         jLabel9.setBounds(320, 100, 57, 14);
         jLayeredPane1.add(jLabel9, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         kilometraje.setNextFocusableComponent(descripcionServicio);
         kilometraje.setBounds(390, 100, 170, 20);
         jLayeredPane1.add(kilometraje, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel10.setText("Combustible:");
         jLabel10.setBounds(10, 140, 62, 14);
         jLayeredPane1.add(jLabel10, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         combustible.setText("100");
         combustible.setBounds(90, 140, 32, 20);
         jLayeredPane1.add(combustible, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         combustibleSlide.setMinorTickSpacing(25);
         combustibleSlide.setPaintLabels(true);
         combustibleSlide.setPaintTicks(true);
         combustibleSlide.setBounds(140, 140, 132, 31);
         jLayeredPane1.add(combustibleSlide, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionMarca.setLayout(new java.awt.BorderLayout());
         validacionMarca.setBounds(570, 10, 16, 16);
         jLayeredPane1.add(validacionMarca, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionVersion.setLayout(new java.awt.BorderLayout());
         validacionVersion.setBounds(570, 40, 16, 16);
         jLayeredPane1.add(validacionVersion, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionModelo.setLayout(new java.awt.BorderLayout());
         validacionModelo.setBounds(280, 70, 16, 16);
         jLayeredPane1.add(validacionModelo, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionPlacas.setLayout(new java.awt.BorderLayout());
         validacionPlacas.setBounds(280, 100, 16, 16);
         jLayeredPane1.add(validacionPlacas, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionTipo.setLayout(new java.awt.BorderLayout());
         validacionTipo.setBounds(280, 40, 16, 16);
         jLayeredPane1.add(validacionTipo, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionSerie.setLayout(new java.awt.BorderLayout());
         validacionSerie.setBounds(280, 10, 16, 16);
         jLayeredPane1.add(validacionSerie, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionColor.setLayout(new java.awt.BorderLayout());
         validacionColor.setBounds(570, 70, 16, 16);
         jLayeredPane1.add(validacionColor, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         validacionKilometraje.setLayout(new java.awt.BorderLayout());
         validacionKilometraje.setBounds(570, 100, 16, 16);
         jLayeredPane1.add(validacionKilometraje, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         descripcionServicio.setColumns(20);
         descripcionServicio.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
         descripcionServicio.setRows(8);
         jScrollPane1.setViewportView(descripcionServicio);
 
         validacionDescripcionServicio.setLayout(new java.awt.BorderLayout());
 
         datosEquipamiento.setBorder(javax.swing.BorderFactory.createTitledBorder("Equipamiento"));
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Transmision"));
 
         grupoTransmision.add(estandar);
         estandar.setSelected(true);
         estandar.setText("Estandar");
 
         grupoTransmision.add(automatico);
         automatico.setText("Automatico");
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(estandar)
                     .addComponent(automatico))
                 .addGap(0, 20, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(estandar)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(automatico)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Elevadores"));
 
         grupoElevadores.add(manuales);
         manuales.setSelected(true);
         manuales.setText("Manuales");
 
         grupoElevadores.add(electrico);
         electrico.setText("Eléctricos");
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(manuales)
                     .addComponent(electrico))
                 .addContainerGap(36, Short.MAX_VALUE))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addComponent(manuales)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(electrico)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         aireAcondicionado.setText("Aire Acondicionado");
 
         jLabel8.setText("Bolsas de aire:");
 
         jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Características del auto"));
 
         jToolBar2.setFloatable(false);
         jToolBar2.setRollover(true);
 
         agregarEquipoAdicional.setText("Agregar");
         agregarEquipoAdicional.setFocusable(false);
         agregarEquipoAdicional.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         agregarEquipoAdicional.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         agregarEquipoAdicional.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 agregarEquipoAdicionalActionPerformed(evt);
             }
         });
         jToolBar2.add(agregarEquipoAdicional);
 
         borrarEquipoAdicional.setText("Borrar");
         borrarEquipoAdicional.setFocusable(false);
         borrarEquipoAdicional.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         borrarEquipoAdicional.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         borrarEquipoAdicional.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 borrarEquipoAdicionalActionPerformed(evt);
             }
         });
         jToolBar2.add(borrarEquipoAdicional);
 
         jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jScrollPane2.setMinimumSize(new java.awt.Dimension(23, 79));
 
         equipoAdicional.setModel(modelEquipoAdicional);
         jScrollPane2.setViewportView(equipoAdicional);
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout datosEquipamientoLayout = new javax.swing.GroupLayout(datosEquipamiento);
         datosEquipamiento.setLayout(datosEquipamientoLayout);
         datosEquipamientoLayout.setHorizontalGroup(
             datosEquipamientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(datosEquipamientoLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(datosEquipamientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(datosEquipamientoLayout.createSequentialGroup()
                         .addComponent(jLabel8)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(bolsasDeAire, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(aireAcondicionado))
                 .addContainerGap(527, Short.MAX_VALUE))
             .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         datosEquipamientoLayout.setVerticalGroup(
             datosEquipamientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, datosEquipamientoLayout.createSequentialGroup()
                 .addGroup(datosEquipamientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(datosEquipamientoLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(datosEquipamientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel8)
                             .addComponent(bolsasDeAire, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(aireAcondicionado)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jLabel11.setText("Descripción del Servicio");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(datosEquipamiento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(612, 612, 612)
                         .addComponent(jLabel11)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(validacionDescripcionServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 594, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel11)
                             .addComponent(validacionDescripcionServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(8, 8, 8)
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(datosEquipamiento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void agregarEquipoAdicionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agregarEquipoAdicionalActionPerformed
         String s = (String)javax.swing.JOptionPane.showInputDialog(
                             this,
                             "Especifique el equipamiento adicional",
                             "Equipamiento Adicional",
                             javax.swing.JOptionPane.PLAIN_MESSAGE);
         modelEquipoAdicional.addElement(s);
     }//GEN-LAST:event_agregarEquipoAdicionalActionPerformed
 
     private void borrarEquipoAdicionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarEquipoAdicionalActionPerformed
         modelEquipoAdicional.removeElement((String)equipoAdicional.getSelectedValue());
     }//GEN-LAST:event_borrarEquipoAdicionalActionPerformed
 
     private void searchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchMouseClicked
         if (!search.isSelectionEmpty()) {
             updateIgnoreCount = 2;
             this.aplication.loadAuto(this.searchModel.getAutoAt(this.search.getSelectedIndex()));
             this.actualizaListaSearch(new LinkedList<Auto>());
         }
     }//GEN-LAST:event_searchMouseClicked
 
     private void numeroSerieKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numeroSerieKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
             if (this.search.getModel().getSize() > 0) {
                 if (this.search.isSelectionEmpty()) { //si no hay nada seleccionado
                     int nuevoIndex = this.search.getModel().getSize() - 1;
                     this.search.setSelectedIndex(nuevoIndex);
                     calculaNuevaPosicionScrollUp(nuevoIndex);
                 } else {
                     int nuevoIndex = this.search.getSelectedIndex() - 1;
                     if (nuevoIndex < 0) {
                         nuevoIndex = this.search.getModel().getSize() - 1;
                     }
                     this.search.setSelectedIndex(nuevoIndex);
                     calculaNuevaPosicionScrollUp(nuevoIndex);
                 }
             }
         }
         if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
             if (this.search.getModel().getSize() > 0) {
                 if (this.search.isSelectionEmpty()) { //si no hay nada seleccionado
                     this.search.setSelectedIndex(0);
                     calculaNuevaPosicionScrollDown(0);
                 } else {
                     int nuevoIndex = this.search.getSelectedIndex() + 1;
                     if (nuevoIndex > this.search.getModel().getSize() - 1) {
                         nuevoIndex = 0;
                     }
                     this.search.setSelectedIndex(nuevoIndex);
                     calculaNuevaPosicionScrollDown(nuevoIndex);
                 }
             }
         }
         if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
             this.searchScroll.setVisible(false);
         }
     }//GEN-LAST:event_numeroSerieKeyPressed
 
     private void numeroSerieFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numeroSerieFocusLost
         if(evt.getOppositeComponent() != this.search) {
             this.searchScroll.setVisible(false);
         }
     }//GEN-LAST:event_numeroSerieFocusLost
 
     private void numeroSerieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numeroSerieActionPerformed
         if (!search.isSelectionEmpty()) {
             updateIgnoreCount = 2;
             this.aplication.loadAuto(this.searchModel.getAutoAt(this.search.getSelectedIndex()));
             this.actualizaListaSearch(new LinkedList<Auto>());
         }
     }//GEN-LAST:event_numeroSerieActionPerformed
 
     private void searchIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchIconMouseClicked
         BusquedaAutoView dialog = new BusquedaAutoView(mainFrame, true, aplication);
         dialog.setVisible(true);
     }//GEN-LAST:event_searchIconMouseClicked
 
     private void cancelIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelIconMouseClicked
         this.aplication.loadAuto(new Auto());
     }//GEN-LAST:event_cancelIconMouseClicked
 
    private void wrapperSearchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wrapperSearchFocusGained
        this.numeroSerie.requestFocus();
    }//GEN-LAST:event_wrapperSearchFocusGained

     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton agregarEquipoAdicional;
     private javax.swing.JCheckBox aireAcondicionado;
     private javax.swing.JRadioButton automatico;
     private javax.swing.JTextField bolsasDeAire;
     private javax.swing.JButton borrarEquipoAdicional;
     private javax.swing.JPanel cancelIcon;
     private javax.swing.JTextField color;
     private javax.swing.JTextField combustible;
     private javax.swing.JSlider combustibleSlide;
     private javax.swing.JPanel datosEquipamiento;
     private javax.swing.JTextArea descripcionServicio;
     private javax.swing.JRadioButton electrico;
     private javax.swing.JList equipoAdicional;
     private javax.swing.JRadioButton estandar;
     private javax.swing.ButtonGroup grupoElevadores;
     private javax.swing.ButtonGroup grupoTransmision;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JLayeredPane jLayeredPane1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JToolBar jToolBar2;
     private javax.swing.JTextField kilometraje;
     private javax.swing.JRadioButton manuales;
     private javax.swing.JTextField marca;
     private javax.swing.JTextField modelo;
     private javax.swing.JTextField numeroSerie;
     private javax.swing.JTextField placas;
     private javax.swing.JList search;
     private javax.swing.JPanel searchIcon;
     private javax.swing.JScrollPane searchScroll;
     private javax.swing.JTextField tipo;
     private javax.swing.JPanel validacionColor;
     private javax.swing.JPanel validacionDescripcionServicio;
     private javax.swing.JPanel validacionKilometraje;
     private javax.swing.JPanel validacionMarca;
     private javax.swing.JPanel validacionModelo;
     private javax.swing.JPanel validacionPlacas;
     private javax.swing.JPanel validacionSerie;
     private javax.swing.JPanel validacionTipo;
     private javax.swing.JPanel validacionVersion;
     private javax.swing.JTextField version;
    private javax.swing.JTextField wrapperSearch;
     // End of variables declaration//GEN-END:variables
 
     public void setBindingManager(BindingManager<Bindable> bindingManager) {
         this.bindingManager = bindingManager;
     }
 
     public void setViewServicioModel(ServicioVB viewServicioModel) {
         this.viewServicioModel = viewServicioModel;
     }
 
     public void setValidacionDatosAuto(ValidacionDatosAuto validacionDatosAuto) {
         this.validacionDatosAuto = validacionDatosAuto;
     }
 
     public void setValidacionGeneralDatosAuto(ValidacionGeneralDatosAuto validacionGeneralDatosAuto) {
         this.validacionGeneralDatosAuto = validacionGeneralDatosAuto;
     }
 
     public void setIconProvider(IconProvider iconProvider) {
         this.iconProvider = iconProvider;
     }
     
     public void setSearchIconRaw(String searchIconRaw) {
         this.searchIconRaw = searchIconRaw;
     }
 
     public void setCancelSearchIconRaw(String cancelSearchIconRaw) {
         this.cancelSearchIconRaw = cancelSearchIconRaw;
     }
 
     public void setValidacionOkIconRaw(String validacionOkIconRaw) {
         this.validacionOkIconRaw = validacionOkIconRaw;
     }
 
     public void setValidacionErrorIconRaw(String validacionErrorIconRaw) {
         this.validacionErrorIconRaw = validacionErrorIconRaw;
     }
     
     public void setRenglonSearchSize(int renglonSearchSize) {
         this.renglonSearchSize = renglonSearchSize;
     }
 
     public void setRenglonesVisiblesSearch(int renglonesVisiblesSearch) {
         this.renglonesVisiblesSearch = renglonesVisiblesSearch;
     }
 
     public void setConstanteUniversalDeAjuste(int constanteUniversalDeAjuste) {
         this.constanteUniversalDeAjuste = constanteUniversalDeAjuste;
     }
 
     public void setAplication(ControlAuto aplication) {
         this.aplication = aplication;
     }
     
     public void setMainFrame(JFrame mainFrame) {
         this.mainFrame = mainFrame;
     }
     
 }
