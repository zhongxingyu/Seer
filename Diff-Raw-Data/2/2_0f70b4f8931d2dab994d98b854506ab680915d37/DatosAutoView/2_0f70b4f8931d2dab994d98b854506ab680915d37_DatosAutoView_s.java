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
 
 
 import org.nekorp.workflow.desktop.view.binding.Bindable;
 import org.nekorp.workflow.desktop.view.binding.BindableListModel;
 import org.nekorp.workflow.desktop.view.binding.BindingManager;
 import org.nekorp.workflow.desktop.view.model.ServicioVB;
 import org.nekorp.workflow.desktop.view.model.auto.TipoElevadorVB;
 import org.nekorp.workflow.desktop.view.model.auto.TipoTransmisionVB;
 import org.nekorp.workflow.desktop.view.model.validacion.ValidacionDatosAuto;
 import org.nekorp.workflow.desktop.view.model.validacion.ValidacionGeneralDatosAuto;
 import org.nekorp.workflow.desktop.view.resource.IconProvider;
 import org.nekorp.workflow.desktop.view.resource.imp.DocumentSizeValidatorMayusculas;
 
 /**
  *
  * 
  */
 public class DatosAutoView extends ApplicationView {
     private BindingManager<Bindable> bindingManager;
     private ServicioVB viewServicioModel;
     private ValidacionDatosAuto validacionDatosAuto;
     private ValidacionGeneralDatosAuto validacionGeneralDatosAuto;
     private IconProvider iconProvider;
     private String validacionOkIconRaw;
     private String validacionErrorIconRaw;
     
     private BindableListModel<String> modelEquipoAdicional;
     
     @Override
     public void setEditableStatus(boolean value) {
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
         datosGenerales = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         marca = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         validacionMarca = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel3 = new javax.swing.JLabel();
         version = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         validacionVersion = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel5 = new javax.swing.JLabel();
         modelo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         validacionModelo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel7 = new javax.swing.JLabel();
         placas = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         ((javax.swing.text.AbstractDocument)placas.getDocument()).setDocumentFilter(new DocumentSizeValidatorMayusculas(10));
         validacionPlacas = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel2 = new javax.swing.JLabel();
         tipo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         validacionTipo = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel4 = new javax.swing.JLabel();
         numeroSerie = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         ((javax.swing.text.AbstractDocument)numeroSerie.getDocument()).setDocumentFilter(new DocumentSizeValidatorMayusculas(17));
         validacionSerie = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel6 = new javax.swing.JLabel();
         color = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         validacionColor = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel9 = new javax.swing.JLabel();
         kilometraje = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         validacionKilometraje = new org.nekorp.workflow.desktop.view.binding.SimpleBindableValidationIcon(this.iconProvider.getIcon(validacionOkIconRaw), this.iconProvider.getIcon(validacionErrorIconRaw));
         jLabel10 = new javax.swing.JLabel();
         combustible = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJTextField();
         combustibleSlide = new org.nekorp.workflow.desktop.view.binding.SimpleBindableJSlider();
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
 
         validacionMarca.setLayout(new java.awt.BorderLayout());
 
         jLabel3.setText("Versión:");
 
         validacionVersion.setLayout(new java.awt.BorderLayout());
 
         jLabel5.setText("Modelo:");
 
         validacionModelo.setLayout(new java.awt.BorderLayout());
 
         jLabel7.setText("Placas:");
 
         validacionPlacas.setLayout(new java.awt.BorderLayout());
 
         jLabel2.setText("Tipo:");
 
         validacionTipo.setLayout(new java.awt.BorderLayout());
 
         jLabel4.setText("Número de serie:");
 
         validacionSerie.setLayout(new java.awt.BorderLayout());
 
         jLabel6.setText("Color:");
 
         validacionColor.setLayout(new java.awt.BorderLayout());
 
         jLabel9.setText("Kilometraje:");
 
         kilometraje.setNextFocusableComponent(descripcionServicio);
 
         validacionKilometraje.setLayout(new java.awt.BorderLayout());
 
         jLabel10.setText("Combustible:");
 
         combustible.setText("100");
 
         combustibleSlide.setMinorTickSpacing(25);
         combustibleSlide.setPaintLabels(true);
         combustibleSlide.setPaintTicks(true);
 
         javax.swing.GroupLayout datosGeneralesLayout = new javax.swing.GroupLayout(datosGenerales);
         datosGenerales.setLayout(datosGeneralesLayout);
         datosGeneralesLayout.setHorizontalGroup(
             datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(datosGeneralesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel1)
                             .addComponent(jLabel3)
                             .addComponent(jLabel5)
                             .addComponent(jLabel7))
                         .addGap(31, 31, 31))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, datosGeneralesLayout.createSequentialGroup()
                         .addComponent(jLabel10)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                 .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(placas)
                     .addComponent(modelo)
                     .addComponent(version)
                     .addComponent(marca)
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(combustible, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(combustibleSlide, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(validacionMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel2))
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(validacionVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel4))
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(validacionModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel6))
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(validacionPlacas, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel9)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(tipo, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(validacionTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(numeroSerie, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(validacionSerie, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(color, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(validacionColor, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addComponent(kilometraje, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(validacionKilometraje, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         datosGeneralesLayout.setVerticalGroup(
             datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(datosGeneralesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel1)
                     .addGroup(datosGeneralesLayout.createSequentialGroup()
                         .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(marca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(tipo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel2))
                             .addComponent(validacionMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(validacionTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(numeroSerie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(version, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel3)
                                 .addComponent(jLabel4))
                             .addComponent(validacionVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(validacionSerie, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(modelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel5)
                                 .addComponent(jLabel6))
                             .addComponent(validacionModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(color, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(validacionColor, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(validacionPlacas, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(placas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(kilometraje, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel7)
                                 .addComponent(jLabel9))
                             .addComponent(validacionKilometraje, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(datosGeneralesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(combustible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel10))
                     .addComponent(combustibleSlide, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
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
 
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Equipamiento Adicional"));
 
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
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
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
                 .addContainerGap())
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
                 .addComponent(datosGenerales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabel11)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(validacionDescripcionServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(datosGenerales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel11)
                             .addComponent(validacionDescripcionServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton agregarEquipoAdicional;
     private javax.swing.JCheckBox aireAcondicionado;
     private javax.swing.JRadioButton automatico;
     private javax.swing.JTextField bolsasDeAire;
     private javax.swing.JButton borrarEquipoAdicional;
     private javax.swing.JTextField color;
     private javax.swing.JTextField combustible;
     private javax.swing.JSlider combustibleSlide;
     private javax.swing.JPanel datosEquipamiento;
     private javax.swing.JPanel datosGenerales;
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
 
     public void setValidacionOkIconRaw(String validacionOkIconRaw) {
         this.validacionOkIconRaw = validacionOkIconRaw;
     }
 
     public void setValidacionErrorIconRaw(String validacionErrorIconRaw) {
         this.validacionErrorIconRaw = validacionErrorIconRaw;
     }
     
 }
