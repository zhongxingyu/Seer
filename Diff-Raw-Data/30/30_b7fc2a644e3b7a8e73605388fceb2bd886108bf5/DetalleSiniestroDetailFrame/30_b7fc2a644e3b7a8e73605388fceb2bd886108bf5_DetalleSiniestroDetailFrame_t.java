 package com.jswitch.siniestros.vista.detalle;
 
 import com.jswitch.base.controlador.documentosAnexos.TipoDocumentoLookupController;
 import com.jswitch.base.controlador.util.DefaultDocumentosAnexosGridController;
 import com.jswitch.base.controlador.util.DefaultGridInternalController;
 import com.jswitch.base.modelo.entidades.Documento;
 import com.jswitch.base.modelo.entidades.NotaTecnica;
 import com.jswitch.base.modelo.entidades.Observacion;
 import com.jswitch.base.modelo.util.bean.BeanVO;
 import com.jswitch.base.vista.util.DefaultDetailFrame;
 import com.jswitch.fas.modelo.Dominios;
 import com.jswitch.siniestros.controlador.DiagnosticoSiniestroGridInternalController;
 import com.jswitch.pagos.controlador.PagoGridInternalController;
 import com.jswitch.pagos.modelo.maestra.Pago;
 import com.jswitch.persona.controlador.PersonaLookupControllerPorNombre;
 import com.jswitch.persona.controlador.PersonasDetailController;
 import com.jswitch.persona.modelo.dominio.TipoPersona;
 import com.jswitch.persona.modelo.maestra.Rif;
 import com.jswitch.reporte.modelo.Reporte;
 import com.jswitch.siniestros.controlador.SiniestroLookupController;
 import com.jswitch.siniestros.controlador.PersonaTipoLookupController;
 import com.jswitch.siniestros.controlador.detalle.reportes.ReporteGridInternalController;
 import com.jswitch.siniestros.modelo.dominio.EtapaSiniestro;
 import com.jswitch.siniestros.modelo.dominio.TipoSiniestro;
 import com.jswitch.siniestros.modelo.maestra.DetalleSiniestro;
 import com.jswitch.siniestros.modelo.maestra.DiagnosticoSiniestro;
 import com.jswitch.siniestros.modelo.maestra.detalle.APS;
 import com.jswitch.siniestros.modelo.maestra.detalle.AyudaSocial;
 import com.jswitch.siniestros.modelo.maestra.detalle.CartaAval;
 import com.jswitch.siniestros.modelo.maestra.detalle.Emergencia;
 import com.jswitch.siniestros.modelo.maestra.detalle.Funerario;
 import com.jswitch.siniestros.modelo.maestra.detalle.Reembolso;
 import com.jswitch.siniestros.modelo.maestra.detalle.Vida;
 import java.awt.event.ActionListener;
 import org.openswing.swing.client.CodLookupControl;
 import org.openswing.swing.client.GridControl;
 import org.openswing.swing.client.InsertButton;
 import org.openswing.swing.form.client.Form;
 import org.openswing.swing.form.client.FormController;
 import org.openswing.swing.mdi.client.MDIFrame;
 import org.openswing.swing.util.java.Consts;
 
 /**
  *
  * @author Orlando Becerra
  * @author Luis Adrian 
  */
 public class DetalleSiniestroDetailFrame extends DefaultDetailFrame {
 
     protected DefaultDocumentosAnexosGridController controllerDocumentosAnexosX;
     protected DefaultGridInternalController controllerObservaciones;
     protected DefaultGridInternalController controllerNotasTecnicas;
     protected DefaultGridInternalController controllerReportes;
     protected DefaultGridInternalController controllerPagos;
     protected DefaultGridInternalController controllerDiagnosticoSiniesto;
     private PersonaTipoLookupController lookupPersonaPago;
     private Class tipoDetalle;
 
     public DetalleSiniestroDetailFrame() {
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         editButton1 = new org.openswing.swing.client.EditButton();
         saveButton1 = new org.openswing.swing.client.SaveButton();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         formAPS = new org.openswing.swing.form.client.Form();
         jPanel3 = new javax.swing.JPanel();
         labelControl6 = new org.openswing.swing.client.LabelControl();
         labelControl7 = new org.openswing.swing.client.LabelControl();
         labelControl13 = new org.openswing.swing.client.LabelControl();
         labelControl14 = new org.openswing.swing.client.LabelControl();
         dateControl4 = new org.openswing.swing.client.DateControl();
         dateControl5 = new org.openswing.swing.client.DateControl();
         codLookupControl5 = new org.openswing.swing.client.CodLookupControl();
         textControl3 = new org.openswing.swing.client.TextControl();
         comboBoxControl2 = new org.openswing.swing.client.ComboBoxControl();
         labelControl15 = new org.openswing.swing.client.LabelControl();
         labelControl16 = new org.openswing.swing.client.LabelControl();
         codLookupControl6 = new org.openswing.swing.client.CodLookupControl();
         labelControl17 = new org.openswing.swing.client.LabelControl();
         codLookupControl7 = new org.openswing.swing.client.CodLookupControl();
         labelControl18 = new org.openswing.swing.client.LabelControl();
         textControl4 = new org.openswing.swing.client.TextControl();
         checkBoxControl2 = new org.openswing.swing.client.CheckBoxControl();
         formAyudaSocial = new org.openswing.swing.form.client.Form();
         jPanel24 = new javax.swing.JPanel();
         labelControl57 = new org.openswing.swing.client.LabelControl();
         labelControl58 = new org.openswing.swing.client.LabelControl();
         labelControl59 = new org.openswing.swing.client.LabelControl();
         labelControl60 = new org.openswing.swing.client.LabelControl();
         dateControl12 = new org.openswing.swing.client.DateControl();
         dateControl13 = new org.openswing.swing.client.DateControl();
         codLookupControl22 = new org.openswing.swing.client.CodLookupControl();
         textControl16 = new org.openswing.swing.client.TextControl();
         codLookupControl23 = new org.openswing.swing.client.CodLookupControl();
         labelControl61 = new org.openswing.swing.client.LabelControl();
         labelControl62 = new org.openswing.swing.client.LabelControl();
         comboBoxControl6 = new org.openswing.swing.client.ComboBoxControl();
         codLookupControl24 = new org.openswing.swing.client.CodLookupControl();
         labelControl63 = new org.openswing.swing.client.LabelControl();
         textControl17 = new org.openswing.swing.client.TextControl();
         labelControl64 = new org.openswing.swing.client.LabelControl();
         labelControl65 = new org.openswing.swing.client.LabelControl();
         labelControl66 = new org.openswing.swing.client.LabelControl();
         codLookupControl25 = new org.openswing.swing.client.CodLookupControl();
         labelControl67 = new org.openswing.swing.client.LabelControl();
         numericControl5 = new org.openswing.swing.client.NumericControl();
         numericControl6 = new org.openswing.swing.client.NumericControl();
         checkBoxControl7 = new org.openswing.swing.client.CheckBoxControl();
         formCartaAval = new org.openswing.swing.form.client.Form();
         jPanel20 = new javax.swing.JPanel();
         labelControl43 = new org.openswing.swing.client.LabelControl();
         labelControl44 = new org.openswing.swing.client.LabelControl();
         labelControl45 = new org.openswing.swing.client.LabelControl();
         labelControl46 = new org.openswing.swing.client.LabelControl();
         dateControl10 = new org.openswing.swing.client.DateControl();
         dateControl11 = new org.openswing.swing.client.DateControl();
         codLookupControl17 = new org.openswing.swing.client.CodLookupControl();
         textControl14 = new org.openswing.swing.client.TextControl();
         codLookupControl18 = new org.openswing.swing.client.CodLookupControl();
         labelControl47 = new org.openswing.swing.client.LabelControl();
         labelControl48 = new org.openswing.swing.client.LabelControl();
         comboBoxControl5 = new org.openswing.swing.client.ComboBoxControl();
         codLookupControl19 = new org.openswing.swing.client.CodLookupControl();
         labelControl49 = new org.openswing.swing.client.LabelControl();
         textControl15 = new org.openswing.swing.client.TextControl();
         labelControl50 = new org.openswing.swing.client.LabelControl();
         labelControl51 = new org.openswing.swing.client.LabelControl();
         labelControl52 = new org.openswing.swing.client.LabelControl();
         codLookupControl20 = new org.openswing.swing.client.CodLookupControl();
         labelControl53 = new org.openswing.swing.client.LabelControl();
         numericControl1 = new org.openswing.swing.client.NumericControl();
         numericControl2 = new org.openswing.swing.client.NumericControl();
         checkBoxControl6 = new org.openswing.swing.client.CheckBoxControl();
         formEmergencia = new org.openswing.swing.form.client.Form();
         jPanel19 = new javax.swing.JPanel();
         labelControl32 = new org.openswing.swing.client.LabelControl();
         labelControl33 = new org.openswing.swing.client.LabelControl();
         labelControl34 = new org.openswing.swing.client.LabelControl();
         labelControl35 = new org.openswing.swing.client.LabelControl();
         dateControl8 = new org.openswing.swing.client.DateControl();
         dateControl9 = new org.openswing.swing.client.DateControl();
         codLookupControl14 = new org.openswing.swing.client.CodLookupControl();
         textControl9 = new org.openswing.swing.client.TextControl();
         labelControl36 = new org.openswing.swing.client.LabelControl();
         labelControl37 = new org.openswing.swing.client.LabelControl();
         labelControl38 = new org.openswing.swing.client.LabelControl();
         textControl10 = new org.openswing.swing.client.TextControl();
         textControl11 = new org.openswing.swing.client.TextControl();
         textControl12 = new org.openswing.swing.client.TextControl();
         comboBoxControl4 = new org.openswing.swing.client.ComboBoxControl();
         labelControl39 = new org.openswing.swing.client.LabelControl();
         codLookupControl15 = new org.openswing.swing.client.CodLookupControl();
         labelControl40 = new org.openswing.swing.client.LabelControl();
         codLookupControl16 = new org.openswing.swing.client.CodLookupControl();
         labelControl41 = new org.openswing.swing.client.LabelControl();
         checkBoxControl5 = new org.openswing.swing.client.CheckBoxControl();
         labelControl42 = new org.openswing.swing.client.LabelControl();
         textControl13 = new org.openswing.swing.client.TextControl();
         labelControl54 = new org.openswing.swing.client.LabelControl();
         labelControl55 = new org.openswing.swing.client.LabelControl();
         labelControl56 = new org.openswing.swing.client.LabelControl();
         codLookupControl21 = new org.openswing.swing.client.CodLookupControl();
         numericControl3 = new org.openswing.swing.client.NumericControl();
         numericControl4 = new org.openswing.swing.client.NumericControl();
         formFunenario = new org.openswing.swing.form.client.Form();
         jPanel17 = new javax.swing.JPanel();
         labelControl19 = new org.openswing.swing.client.LabelControl();
         labelControl20 = new org.openswing.swing.client.LabelControl();
         labelControl21 = new org.openswing.swing.client.LabelControl();
         dateControl6 = new org.openswing.swing.client.DateControl();
         codLookupControl8 = new org.openswing.swing.client.CodLookupControl();
         textControl5 = new org.openswing.swing.client.TextControl();
         codLookupControl9 = new org.openswing.swing.client.CodLookupControl();
         labelControl22 = new org.openswing.swing.client.LabelControl();
         codLookupControl10 = new org.openswing.swing.client.CodLookupControl();
         labelControl23 = new org.openswing.swing.client.LabelControl();
         checkBoxControl3 = new org.openswing.swing.client.CheckBoxControl();
         textControl6 = new org.openswing.swing.client.TextControl();
         labelControl24 = new org.openswing.swing.client.LabelControl();
         formRembolso = new org.openswing.swing.form.client.Form();
         jPanel2 = new javax.swing.JPanel();
         labelControl3 = new org.openswing.swing.client.LabelControl();
         labelControl1 = new org.openswing.swing.client.LabelControl();
         labelControl2 = new org.openswing.swing.client.LabelControl();
         labelControl4 = new org.openswing.swing.client.LabelControl();
         dateControl1 = new org.openswing.swing.client.DateControl();
         dateControl2 = new org.openswing.swing.client.DateControl();
         codLookupControl1 = new org.openswing.swing.client.CodLookupControl();
         textControl1 = new org.openswing.swing.client.TextControl();
         codLookupControl3 = new org.openswing.swing.client.CodLookupControl();
         labelControl8 = new org.openswing.swing.client.LabelControl();
         labelControl9 = new org.openswing.swing.client.LabelControl();
         comboBoxControl1 = new org.openswing.swing.client.ComboBoxControl();
         codLookupControl4 = new org.openswing.swing.client.CodLookupControl();
         labelControl10 = new org.openswing.swing.client.LabelControl();
         dateControl3 = new org.openswing.swing.client.DateControl();
         labelControl12 = new org.openswing.swing.client.LabelControl();
         textControl2 = new org.openswing.swing.client.TextControl();
         codLookupControl2 = new org.openswing.swing.client.CodLookupControl();
         labelControl11 = new org.openswing.swing.client.LabelControl();
         labelControl5 = new org.openswing.swing.client.LabelControl();
         checkBoxControl1 = new org.openswing.swing.client.CheckBoxControl();
         formVida = new org.openswing.swing.form.client.Form();
         jPanel18 = new javax.swing.JPanel();
         labelControl25 = new org.openswing.swing.client.LabelControl();
         labelControl26 = new org.openswing.swing.client.LabelControl();
         labelControl27 = new org.openswing.swing.client.LabelControl();
         dateControl7 = new org.openswing.swing.client.DateControl();
         codLookupControl11 = new org.openswing.swing.client.CodLookupControl();
         textControl7 = new org.openswing.swing.client.TextControl();
         comboBoxControl3 = new org.openswing.swing.client.ComboBoxControl();
         labelControl28 = new org.openswing.swing.client.LabelControl();
         codLookupControl12 = new org.openswing.swing.client.CodLookupControl();
         labelControl29 = new org.openswing.swing.client.LabelControl();
         codLookupControl13 = new org.openswing.swing.client.CodLookupControl();
         labelControl30 = new org.openswing.swing.client.LabelControl();
         labelControl31 = new org.openswing.swing.client.LabelControl();
         textControl8 = new org.openswing.swing.client.TextControl();
         checkBoxControl4 = new org.openswing.swing.client.CheckBoxControl();
         Diagnosticos = new javax.swing.JPanel();
         jPanel22 = new javax.swing.JPanel();
         gridDiagnosticos = new org.openswing.swing.client.GridControl();
         decimalColumn7 = new org.openswing.swing.table.columns.client.DecimalColumn();
         decimalColumn1 = new org.openswing.swing.table.columns.client.DecimalColumn();
         decimalColumn2 = new org.openswing.swing.table.columns.client.DecimalColumn();
         textColumn13 = new org.openswing.swing.table.columns.client.TextColumn();
         textColumn14 = new org.openswing.swing.table.columns.client.TextColumn();
         textColumn15 = new org.openswing.swing.table.columns.client.TextColumn();
         checkBoxColumn2 = new org.openswing.swing.table.columns.client.CheckBoxColumn();
         textColumn16 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn7 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         textColumn17 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn8 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         jPanel23 = new javax.swing.JPanel();
         insertButtonDiagnosticoSiniestro = new org.openswing.swing.client.InsertButton();
         editButton9 = new org.openswing.swing.client.EditButton();
         deleteButton9 = new org.openswing.swing.client.DeleteButton();
         saveButton9 = new org.openswing.swing.client.SaveButton();
         reloadButton9 = new org.openswing.swing.client.ReloadButton();
         pagos = new javax.swing.JPanel();
         jPanel9 = new javax.swing.JPanel();
         jPanel16 = new javax.swing.JPanel();
         insertButtonPagos = new org.openswing.swing.client.InsertButton();
         editButtonPagos = new org.openswing.swing.client.EditButton();
         deleteButtonPagos = new org.openswing.swing.client.DeleteButton();
         saveButtonPagos = new org.openswing.swing.client.SaveButton();
         reloadButtonPagos = new org.openswing.swing.client.ReloadButton();
         filterButtonPagos = new org.openswing.swing.client.FilterButton();
         gridControl6 = new org.openswing.swing.client.GridControl();
         decimalColumn6 = new org.openswing.swing.table.columns.client.DecimalColumn();
         textColumn3 = new org.openswing.swing.table.columns.client.TextColumn();
         textColumn6 = new org.openswing.swing.table.columns.client.TextColumn();
         dateColumn1 = new org.openswing.swing.table.columns.client.DateColumn();
         dateColumn2 = new org.openswing.swing.table.columns.client.DateColumn();
         checkBoxColumn1 = new org.openswing.swing.table.columns.client.CheckBoxColumn();
         textColumn10 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn5 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         textColumn11 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn6 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         observaciones = new javax.swing.JPanel();
         jPanel12 = new javax.swing.JPanel();
         gridControl4 = new org.openswing.swing.client.GridControl();
         decimalColumn4 = new org.openswing.swing.table.columns.client.DecimalColumn();
         textColumn1 = new org.openswing.swing.table.columns.client.TextColumn();
         textColumn8 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn1 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         textColumn4 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn2 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         jPanel13 = new javax.swing.JPanel();
         insertButton6 = new org.openswing.swing.client.InsertButton();
         editButton6 = new org.openswing.swing.client.EditButton();
         deleteButton6 = new org.openswing.swing.client.DeleteButton();
         saveButton6 = new org.openswing.swing.client.SaveButton();
         reloadButton6 = new org.openswing.swing.client.ReloadButton();
         notasTecnicas = new javax.swing.JPanel();
         jPanel14 = new javax.swing.JPanel();
         gridControl5 = new org.openswing.swing.client.GridControl();
         decimalColumn5 = new org.openswing.swing.table.columns.client.DecimalColumn();
         textColumn2 = new org.openswing.swing.table.columns.client.TextColumn();
         textColumn9 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn3 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         textColumn5 = new org.openswing.swing.table.columns.client.TextColumn();
         dateTimeColumn4 = new org.openswing.swing.table.columns.client.DateTimeColumn();
         jPanel15 = new javax.swing.JPanel();
         insertButton7 = new org.openswing.swing.client.InsertButton();
         saveButton7 = new org.openswing.swing.client.SaveButton();
         reloadButton7 = new org.openswing.swing.client.ReloadButton();
         DocAnexos = new javax.swing.JPanel();
         jPanel10 = new javax.swing.JPanel();
         gridControl3 = new org.openswing.swing.client.GridControl();
         buttonColumn1 = new org.openswing.swing.table.columns.client.ButtonColumn();
         integerColumn1 = new org.openswing.swing.table.columns.client.IntegerColumn();
         codLookupColumn2 = new org.openswing.swing.table.columns.client.CodLookupColumn();
         textColumn7 = new org.openswing.swing.table.columns.client.TextColumn();
         dateColumn3 = new org.openswing.swing.table.columns.client.DateColumn();
         jPanel11 = new javax.swing.JPanel();
         insertButton5 = new org.openswing.swing.client.InsertButton();
         editButton5 = new org.openswing.swing.client.EditButton();
         deleteButton5 = new org.openswing.swing.client.DeleteButton();
         saveButton5 = new org.openswing.swing.client.SaveButton();
         reloadButton5 = new org.openswing.swing.client.ReloadButton();
         filterButton5 = new org.openswing.swing.client.FilterButton();
         Reportes = new javax.swing.JPanel();
         gridReportes = new org.openswing.swing.client.GridControl();
         textColumn12 = new org.openswing.swing.table.columns.client.TextColumn();
 
         setTitle("Detalle de Siniestro");
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(editButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(saveButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(597, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(editButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(saveButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         formAPS.setVOClassName(APS.class.getName());
         formAPS.setLayout(new java.awt.GridLayout(1, 2));
 
         labelControl6.setLabel("detalleSiniestro.personaPago");
 
         labelControl7.setLabel("detalleSiniestro.TipoPersona");
 
         labelControl13.setLabel("detalleSiniestro.fechaEmision");
 
         labelControl14.setLabel("detalleSiniestro.fechaVencimiento");
 
         dateControl4.setAttributeName("fechaEmision");
         dateControl4.setEnabledOnInsert(false);
         dateControl4.setRequired(true);
 
         dateControl5.setAttributeName("fechaVencimiento");
         dateControl5.setRequired(true);
 
         codLookupControl5.setAttributeName("personaPago.nombreLargo");
         codLookupControl5.setMaxCharacters(25);
         codLookupControl5.setRequired(true);
 
         textControl3.setAttributeName("tipoPersona.nombre");
         textControl3.setEnabled(false);
         textControl3.setEnabledOnEdit(false);
         textControl3.setEnabledOnInsert(false);
 
         comboBoxControl2.setAttributeName("tratamientoEfectuado");
         comboBoxControl2.setDomainId(Dominios.TratamientoEfectuado().getDomainId());
         comboBoxControl2.setRequired(true);
 
         labelControl15.setLabel("detalleSiniestro.tratamientoEfectuado");
 
         labelControl16.setLabel("detalleSiniestro.tipoSiniestro");
 
         codLookupControl6.setAttributeName("tipoSiniestro.nombre");
         codLookupControl6.setMaxCharacters(25);
         codLookupControl6.setRequired(true);
 
         labelControl17.setLabel("detalleSiniestro.etapaSiniestro");
 
         codLookupControl7.setAttributeName("etapaSiniestro.nombre");
         codLookupControl7.setEnableCodBox(false);
         codLookupControl7.setEnabledOnInsert(false);
         codLookupControl7.setMaxCharacters(25);
 
         labelControl18.setLabel("siniestro.numero");
 
         textControl4.setAttributeName("siniestro.numero");
         textControl4.setEnabled(false);
         textControl4.setEnabledOnEdit(false);
         textControl4.setEnabledOnInsert(false);
 
         checkBoxControl2.setText("auditoria.activo");
         checkBoxControl2.setAttributeName("auditoria.activo");
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl4, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl5, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl3, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl4, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl5, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(comboBoxControl2, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl6, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(labelControl17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl7, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addComponent(checkBoxControl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {checkBoxControl2, labelControl13, labelControl14, labelControl15, labelControl16, labelControl17, labelControl18, labelControl6, labelControl7});
 
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl18, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(labelControl7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl14, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl15, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(comboBoxControl2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl16, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl17, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(checkBoxControl2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(217, Short.MAX_VALUE))
         );
 
         jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {checkBoxControl2, labelControl13, labelControl14, labelControl15, labelControl16, labelControl17, labelControl18, labelControl6, labelControl7, textControl4});
 
         formAPS.add(jPanel3);
 
         jTabbedPane1.addTab("APS", formAPS);
 
         formAyudaSocial.setVOClassName(AyudaSocial.class.getName());
         formAyudaSocial.setLayout(new java.awt.BorderLayout());
 
         labelControl57.setLabel("detalleSiniestro.personaPago");
 
         labelControl58.setLabel("detalleSiniestro.TipoPersona");
 
         labelControl59.setLabel("detalleSiniestro.fechaEmision");
 
         labelControl60.setLabel("detalleSiniestro.fechaVencimiento");
 
         dateControl12.setAttributeName("fechaEmision");
         dateControl12.setEnabledOnInsert(false);
         dateControl12.setRequired(true);
 
         dateControl13.setAttributeName("fechaVencimiento");
         dateControl13.setRequired(true);
 
         codLookupControl22.setAttributeName("personaPago.nombreLargo");
         codLookupControl22.setMaxCharacters(25);
         codLookupControl22.setRequired(true);
 
         textControl16.setAttributeName("tipoPersona.nombre");
         textControl16.setEnabled(false);
         textControl16.setEnabledOnEdit(false);
         textControl16.setEnabledOnInsert(false);
 
         codLookupControl23.setAttributeName("tipoSiniestro.nombre");
         codLookupControl23.setMaxCharacters(25);
         codLookupControl23.setRequired(true);
 
         labelControl61.setLabel("detalleSiniestro.tratamientoEfectuado");
 
         labelControl62.setLabel("detalleSiniestro.tipoSiniestro");
 
         comboBoxControl6.setAttributeName("tratamientoEfectuado");
         comboBoxControl6.setDomainId(Dominios.TratamientoEfectuado().getDomainId());
         comboBoxControl6.setRequired(true);
 
         codLookupControl24.setAttributeName("etapaSiniestro.nombre");
         codLookupControl24.setEnableCodBox(false);
         codLookupControl24.setEnabledOnInsert(false);
         codLookupControl24.setMaxCharacters(25);
 
         labelControl63.setLabel("detalleSiniestro.etapaSiniestro");
 
         textControl17.setAttributeName("siniestro.numero");
         textControl17.setEnabled(false);
         textControl17.setEnabledOnEdit(false);
         textControl17.setEnabledOnInsert(false);
 
         labelControl64.setLabel("siniestro.numero");
 
         labelControl65.setLabel("presupuestadoInicial");
 
         labelControl66.setLabel("presupuestadoAjustado");
 
         codLookupControl25.setAttributeName("analistaNegociador.nombreLargo");
         codLookupControl25.setMaxCharacters(25);
         codLookupControl25.setRequired(true);
 
         labelControl67.setLabel("analistaNegociador");
 
         numericControl5.setAttributeName("presupuestadoInicial");
         numericControl5.setRequired(true);
 
         numericControl6.setAttributeName("presupuestadoAjustado");
         numericControl6.setRequired(true);
 
         checkBoxControl7.setText("auditoria.activo");
         checkBoxControl7.setAttributeName("auditoria.activo");
 
         javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
         jPanel24.setLayout(jPanel24Layout);
         jPanel24Layout.setHorizontalGroup(
             jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel24Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl57, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl22, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl58, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl16, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl59, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl12, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl60, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl13, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl61, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(comboBoxControl6, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl62, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl23, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl63, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl24, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl67, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl25, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl65, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(numericControl5, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl66, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(numericControl6, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel24Layout.createSequentialGroup()
                         .addComponent(labelControl64, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl17, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addComponent(checkBoxControl7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel24Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {checkBoxControl7, labelControl57, labelControl58, labelControl59, labelControl60, labelControl61, labelControl62, labelControl63, labelControl64, labelControl65, labelControl66, labelControl67});
 
         jPanel24Layout.setVerticalGroup(
             jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel24Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl57, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl22, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl58, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl16, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl59, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl60, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl61, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(comboBoxControl6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl62, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl23, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl63, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl24, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl67, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl25, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl65, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(numericControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl66, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(numericControl6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl64, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl17, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(checkBoxControl7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(129, Short.MAX_VALUE))
         );
 
         jPanel24Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {checkBoxControl7, codLookupControl22, codLookupControl23, codLookupControl24, codLookupControl25, comboBoxControl6, dateControl12, dateControl13, labelControl57, labelControl58, labelControl59, labelControl60, labelControl61, labelControl62, labelControl63, labelControl64, labelControl65, labelControl66, labelControl67, numericControl5, numericControl6, textControl16, textControl17});
 
         formAyudaSocial.add(jPanel24, java.awt.BorderLayout.CENTER);
 
         jTabbedPane1.addTab("Ayuda Social", formAyudaSocial);
 
         formCartaAval.setVOClassName(CartaAval.class.getName());
         formCartaAval.setLayout(new java.awt.BorderLayout());
 
         labelControl43.setLabel("detalleSiniestro.personaPago");
 
         labelControl44.setLabel("detalleSiniestro.TipoPersona");
 
         labelControl45.setLabel("detalleSiniestro.fechaEmision");
 
         labelControl46.setLabel("detalleSiniestro.fechaVencimiento");
 
         dateControl10.setAttributeName("fechaEmision");
         dateControl10.setEnabledOnInsert(false);
         dateControl10.setRequired(true);
 
         dateControl11.setAttributeName("fechaVencimiento");
         dateControl11.setRequired(true);
 
         codLookupControl17.setAttributeName("personaPago.nombreLargo");
         codLookupControl17.setMaxCharacters(25);
         codLookupControl17.setRequired(true);
 
         textControl14.setAttributeName("tipoPersona.nombre");
         textControl14.setEnabled(false);
         textControl14.setEnabledOnEdit(false);
         textControl14.setEnabledOnInsert(false);
 
         codLookupControl18.setAttributeName("tipoSiniestro.nombre");
         codLookupControl18.setMaxCharacters(25);
         codLookupControl18.setRequired(true);
 
         labelControl47.setLabel("detalleSiniestro.tratamientoEfectuado");
 
         labelControl48.setLabel("detalleSiniestro.tipoSiniestro");
 
         comboBoxControl5.setAttributeName("tratamientoEfectuado");
         comboBoxControl5.setDomainId(Dominios.TratamientoEfectuado().getDomainId());
         comboBoxControl5.setRequired(true);
 
         codLookupControl19.setAttributeName("etapaSiniestro.nombre");
         codLookupControl19.setEnableCodBox(false);
         codLookupControl19.setEnabledOnInsert(false);
         codLookupControl19.setMaxCharacters(25);
 
         labelControl49.setLabel("detalleSiniestro.etapaSiniestro");
 
         textControl15.setAttributeName("siniestro.numero");
         textControl15.setEnabled(false);
         textControl15.setEnabledOnEdit(false);
         textControl15.setEnabledOnInsert(false);
 
         labelControl50.setLabel("siniestro.numero");
 
         labelControl51.setLabel("presupuestadoInicial");
 
         labelControl52.setLabel("presupuestadoAjustado");
 
         codLookupControl20.setAttributeName("analistaNegociador.nombreLargo");
         codLookupControl20.setMaxCharacters(25);
         codLookupControl20.setRequired(true);
 
         labelControl53.setLabel("analistaNegociador");
 
         numericControl1.setAttributeName("presupuestadoInicial");
         numericControl1.setRequired(true);
 
         numericControl2.setAttributeName("presupuestadoAjustado");
         numericControl2.setRequired(true);
 
         checkBoxControl6.setText("auditoria.activo");
         checkBoxControl6.setAttributeName("auditoria.activo");
 
         javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
         jPanel20.setLayout(jPanel20Layout);
         jPanel20Layout.setHorizontalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel20Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl17, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl14, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl10, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl46, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl11, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl47, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(comboBoxControl5, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl18, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl19, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl20, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(numericControl1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(numericControl2, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel20Layout.createSequentialGroup()
                         .addComponent(labelControl50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl15, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addComponent(checkBoxControl6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel20Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {checkBoxControl6, labelControl43, labelControl44, labelControl45, labelControl46, labelControl47, labelControl48, labelControl49, labelControl50, labelControl51, labelControl52, labelControl53});
 
         jPanel20Layout.setVerticalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel20Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl43, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl17, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl44, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl14, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl45, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl10, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl46, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl11, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl47, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(comboBoxControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl48, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl18, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl49, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl19, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl53, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl20, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl51, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(numericControl1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl52, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(numericControl2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl50, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl15, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(checkBoxControl6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(129, Short.MAX_VALUE))
         );
 
         jPanel20Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {checkBoxControl6, codLookupControl17, codLookupControl18, codLookupControl19, codLookupControl20, comboBoxControl5, dateControl10, dateControl11, labelControl43, labelControl44, labelControl45, labelControl46, labelControl47, labelControl48, labelControl49, labelControl50, labelControl51, labelControl52, labelControl53, numericControl1, numericControl2, textControl14, textControl15});
 
         formCartaAval.add(jPanel20, java.awt.BorderLayout.CENTER);
 
         jTabbedPane1.addTab("Carta Aval", formCartaAval);
 
         formEmergencia.setVOClassName(Emergencia.class.getName());
         formEmergencia.setLayout(new java.awt.GridLayout(1, 2));
 
         labelControl32.setLabel("detalleSiniestro.personaPago");
 
         labelControl33.setLabel("detalleSiniestro.TipoPersona");
 
         labelControl34.setLabel("detalleSiniestro.fechaEntrada");
 
         labelControl35.setLabel("detalleSiniestro.fechaSalida");
 
         dateControl8.setAttributeName("fechaEntrada");
         dateControl8.setEnabledOnInsert(false);
         dateControl8.setRequired(true);
 
         dateControl9.setAttributeName("fechaSalida");
         dateControl9.setEnabledOnInsert(false);
 
         codLookupControl14.setAttributeName("personaPago.nombreLargo");
         codLookupControl14.setMaxCharacters(25);
         codLookupControl14.setRequired(true);
 
         textControl9.setAttributeName("tipoPersona.nombre");
         textControl9.setEnabled(false);
         textControl9.setEnabledOnEdit(false);
         textControl9.setEnabledOnInsert(false);
 
         labelControl36.setLabel("detalleSiniestro.clave");
 
         labelControl37.setLabel("detalleSiniestro.nombreContacto");
 
         labelControl38.setLabel("detalleSiniestro.telefonoContacto");
 
         textControl10.setAttributeName("clave");
 
         textControl11.setAttributeName("nombreContacto");
 
         textControl12.setAttributeName("telefonoContacto");
 
         comboBoxControl4.setAttributeName("tratamientoEfectuado");
         comboBoxControl4.setDomainId(Dominios.TratamientoEfectuado().getDomainId());
         comboBoxControl4.setRequired(true);
 
         labelControl39.setLabel("detalleSiniestro.tratamientoEfectuado");
 
         codLookupControl15.setAttributeName("etapaSiniestro.nombre");
         codLookupControl15.setEnableCodBox(false);
         codLookupControl15.setEnabledOnInsert(false);
         codLookupControl15.setMaxCharacters(25);
 
         labelControl40.setLabel("detalleSiniestro.etapaSiniestro");
 
         codLookupControl16.setAttributeName("tipoSiniestro.nombre");
         codLookupControl16.setMaxCharacters(25);
         codLookupControl16.setRequired(true);
 
         labelControl41.setLabel("detalleSiniestro.tipoSiniestro");
 
         checkBoxControl5.setText("auditoria.activo");
         checkBoxControl5.setAttributeName("auditoria.activo");
 
         labelControl42.setLabel("siniestro.numero");
 
         textControl13.setAttributeName("siniestro.numero");
         textControl13.setEnabled(false);
         textControl13.setEnabledOnEdit(false);
         textControl13.setEnabledOnInsert(false);
 
         labelControl54.setLabel("presupuestadoAjustado");
 
         labelControl55.setLabel("presupuestadoInicial");
 
         labelControl56.setLabel("analistaNegociador");
 
         codLookupControl21.setAttributeName("analistaNegociador.nombreLargo");
         codLookupControl21.setMaxCharacters(25);
         codLookupControl21.setRequired(true);
 
         numericControl3.setAttributeName("presupuestadoInicial");
         numericControl3.setRequired(true);
 
         numericControl4.setAttributeName("presupuestadoAjustado");
         numericControl4.setRequired(true);
 
         javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
         jPanel19.setLayout(jPanel19Layout);
         jPanel19Layout.setHorizontalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel19Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl14, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl9, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl8, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl9, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl10, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl37, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl11, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl12, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(comboBoxControl4, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl16, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl15, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl13, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl56, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl21, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl55, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(numericControl3, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel19Layout.createSequentialGroup()
                         .addComponent(labelControl54, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(numericControl4, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addComponent(checkBoxControl5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel19Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {checkBoxControl5, labelControl32, labelControl33, labelControl34, labelControl35, labelControl36, labelControl37, labelControl38, labelControl39, labelControl40, labelControl41, labelControl42, labelControl54, labelControl55, labelControl56});
 
         jPanel19Layout.setVerticalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel19Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl32, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl14, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl33, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl34, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl35, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl36, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl10, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl37, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl11, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl38, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl39, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(comboBoxControl4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl41, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl16, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl40, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl15, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl42, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl56, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl21, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl55, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(numericControl3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl54, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(numericControl4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(checkBoxControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(36, Short.MAX_VALUE))
         );
 
         jPanel19Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {checkBoxControl5, codLookupControl14, codLookupControl15, codLookupControl16, codLookupControl21, comboBoxControl4, dateControl8, dateControl9, labelControl32, labelControl33, labelControl34, labelControl35, labelControl36, labelControl37, labelControl38, labelControl39, labelControl40, labelControl41, labelControl42, labelControl54, labelControl55, labelControl56, numericControl3, numericControl4, textControl10, textControl11, textControl12, textControl13, textControl9});
 
         formEmergencia.add(jPanel19);
 
         jTabbedPane1.addTab("Emergencia", formEmergencia);
 
         formFunenario.setVOClassName(com.jswitch.siniestros.modelo.maestra.detalle.Funerario.class.getName());
         formFunenario.setPreferredSize(new java.awt.Dimension(662, 474));
         formFunenario.setLayout(new java.awt.GridLayout(1, 2));
 
         labelControl19.setLabel("detalleSiniestro.personaPago");
 
         labelControl20.setLabel("detalleSiniestro.TipoPersona");
 
         labelControl21.setLabel("detalleSiniestro.fechaDefuncion");
 
         dateControl6.setAttributeName("fechaDefuncion");
         dateControl6.setRequired(true);
 
         codLookupControl8.setAttributeName("personaPago.nombreLargo");
         codLookupControl8.setMaxCharacters(25);
         codLookupControl8.setRequired(true);
 
         textControl5.setAttributeName("tipoPersona.nombre");
         textControl5.setEnabled(false);
         textControl5.setEnabledOnEdit(false);
         textControl5.setEnabledOnInsert(false);
 
         codLookupControl9.setAttributeName("etapaSiniestro.nombre");
         codLookupControl9.setEnableCodBox(false);
         codLookupControl9.setEnabledOnInsert(false);
         codLookupControl9.setMaxCharacters(25);
 
         labelControl22.setLabel("detalleSiniestro.etapaSiniestro");
 
         codLookupControl10.setAttributeName("tipoSiniestro.nombre");
         codLookupControl10.setMaxCharacters(25);
         codLookupControl10.setRequired(true);
 
         labelControl23.setLabel("detalleSiniestro.tipoSiniestro");
 
         checkBoxControl3.setText("auditoria.activo");
         checkBoxControl3.setAttributeName("auditoria.activo");
 
         textControl6.setAttributeName("siniestro.numero");
         textControl6.setEnabled(false);
         textControl6.setEnabledOnEdit(false);
         textControl6.setEnabledOnInsert(false);
 
         labelControl24.setLabel("siniestro.numero");
 
         javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
         jPanel17.setLayout(jPanel17Layout);
         jPanel17Layout.setHorizontalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel17Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addComponent(labelControl19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl8, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addComponent(labelControl20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl5, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addComponent(labelControl21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl6, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addComponent(labelControl23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl10, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addComponent(labelControl22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl9, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                     .addGroup(jPanel17Layout.createSequentialGroup()
                         .addComponent(labelControl24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl6, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                     .addComponent(checkBoxControl3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel17Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {checkBoxControl3, labelControl19, labelControl20, labelControl21, labelControl22, labelControl23, labelControl24});
 
         jPanel17Layout.setVerticalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl19, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl20, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                     .addComponent(textControl5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl21, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl23, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl10, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl22, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl24, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(checkBoxControl3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(448, 448, 448))
         );
 
         jPanel17Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {checkBoxControl3, codLookupControl10, codLookupControl8, codLookupControl9, dateControl6, labelControl19, labelControl20, labelControl21, labelControl22, labelControl23, labelControl24, textControl5, textControl6});
 
         formFunenario.add(jPanel17);
 
         jTabbedPane1.addTab("Funenarios", formFunenario);
 
         formRembolso.setVOClassName(Reembolso.class.getName());
         formRembolso.setLayout(new java.awt.GridLayout(1, 0));
 
         labelControl3.setLabel("detalleSiniestro.personaPago");
 
         labelControl1.setLabel("detalleSiniestro.TipoPersona");
 
         labelControl2.setLabel("detalleSiniestro.fechaOcurrencia");
 
         labelControl4.setLabel("detalleSiniestro.fechaNotificacion");
 
         dateControl1.setAttributeName("fechaOcurrencia");
         dateControl1.setRequired(true);
 
         dateControl2.setAttributeName("fechaNotificacion");
         dateControl2.setRequired(true);
 
         codLookupControl1.setAttributeName("personaPago.nombreLargo");
         codLookupControl1.setEnabledOnEdit(false);
         codLookupControl1.setEnabledOnInsert(false);
         codLookupControl1.setMaxCharacters(25);
 
         textControl1.setAttributeName("tipoPersona.nombre");
         textControl1.setEnabled(false);
         textControl1.setEnabledOnEdit(false);
         textControl1.setEnabledOnInsert(false);
 
         codLookupControl3.setAttributeName("tipoSiniestro.nombre");
         codLookupControl3.setMaxCharacters(25);
         codLookupControl3.setRequired(true);
 
         labelControl8.setLabel("detalleSiniestro.tratamientoEfectuado");
 
         labelControl9.setLabel("detalleSiniestro.tipoSiniestro");
 
         comboBoxControl1.setAttributeName("tratamientoEfectuado");
         comboBoxControl1.setDomainId(Dominios.TratamientoEfectuado().getDomainId());
         comboBoxControl1.setRequired(true);
 
         codLookupControl4.setAttributeName("etapaSiniestro.nombre");
         codLookupControl4.setEnableCodBox(false);
         codLookupControl4.setEnabledOnInsert(false);
         codLookupControl4.setMaxCharacters(25);
 
         labelControl10.setLabel("detalleSiniestro.etapaSiniestro");
 
         dateControl3.setAttributeName("fechaConstitucion");
         dateControl3.setRequired(true);
 
         labelControl12.setLabel("detalleSiniestro.fechaConstitucion");
 
         textControl2.setAttributeName("siniestro.numero");
         textControl2.setEnabled(false);
         textControl2.setEnabledOnEdit(false);
         textControl2.setEnabledOnInsert(false);
 
         codLookupControl2.setAttributeName("analistaNegociador.nombreLargo");
         codLookupControl2.setMaxCharacters(25);
         codLookupControl2.setRequired(true);
 
         labelControl11.setLabel("siniestro.numero");
 
         labelControl5.setLabel("analistaNegociador");
 
         checkBoxControl1.setText("auditoria.activo");
         checkBoxControl1.setAttributeName("auditoria.activo");
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl2, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl3, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(comboBoxControl1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl3, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl4, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl2, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(labelControl11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl2, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addComponent(checkBoxControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {checkBoxControl1, labelControl1, labelControl10, labelControl11, labelControl12, labelControl2, labelControl3, labelControl4, labelControl5, labelControl8, labelControl9});
 
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(comboBoxControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(checkBoxControl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(182, Short.MAX_VALUE))
         );
 
         jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {checkBoxControl1, codLookupControl1, codLookupControl2, codLookupControl3, codLookupControl4, comboBoxControl1, dateControl1, dateControl2, dateControl3, labelControl1, labelControl10, labelControl11, labelControl12, labelControl2, labelControl3, labelControl4, labelControl5, labelControl8, labelControl9, textControl1, textControl2});
 
         formRembolso.add(jPanel2);
 
         jTabbedPane1.addTab("Rembolso", formRembolso);
 
         formVida.setVOClassName(Vida.class.getName());
         formVida.setPreferredSize(new java.awt.Dimension(662, 474));
         formVida.setLayout(new java.awt.GridLayout(1, 2));
 
         labelControl25.setLabel("detalleSiniestro.personaPago");
 
         labelControl26.setLabel("detalleSiniestro.TipoPersona");
 
         labelControl27.setLabel("detalleSiniestro.fechaDefuncion");
 
         dateControl7.setAttributeName("fechaDefuncion");
         dateControl7.setEnabledOnInsert(false);
         dateControl7.setRequired(true);
 
         codLookupControl11.setAttributeName("personaPago.nombreLargo");
         codLookupControl11.setEnableCodBox(false);
         codLookupControl11.setEnabledOnEdit(false);
         codLookupControl11.setEnabledOnInsert(false);
         codLookupControl11.setLookupButtonVisible(false);
         codLookupControl11.setMaxCharacters(25);
         codLookupControl11.setRequired(true);
 
         textControl7.setAttributeName("tipoPersona.nombre");
         textControl7.setEnabled(false);
         textControl7.setEnabledOnEdit(false);
         textControl7.setEnabledOnInsert(false);
 
         comboBoxControl3.setAttributeName("tratamientoEfectuado");
         comboBoxControl3.setDomainId(Dominios.TratamientoEfectuado().getDomainId());
         comboBoxControl3.setRequired(true);
 
         labelControl28.setLabel("detalleSiniestro.tratamientoEfectuado");
 
         codLookupControl12.setAttributeName("etapaSiniestro.nombre");
         codLookupControl12.setEnableCodBox(false);
         codLookupControl12.setEnabledOnInsert(false);
         codLookupControl12.setMaxCharacters(25);
 
         labelControl29.setLabel("detalleSiniestro.etapaSiniestro");
 
         codLookupControl13.setAttributeName("tipoSiniestro.nombre");
         codLookupControl13.setMaxCharacters(25);
         codLookupControl13.setRequired(true);
 
         labelControl30.setLabel("detalleSiniestro.tipoSiniestro");
 
         labelControl31.setLabel("siniestro.numero");
 
         textControl8.setAttributeName("siniestro.numero");
         textControl8.setEnabled(false);
         textControl8.setEnabledOnEdit(false);
         textControl8.setEnabledOnInsert(false);
 
         checkBoxControl4.setText("auditoria.activo");
         checkBoxControl4.setAttributeName("auditoria.activo");
 
         javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
         jPanel18.setLayout(jPanel18Layout);
         jPanel18Layout.setHorizontalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel18Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addComponent(labelControl25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl11, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addComponent(labelControl26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl7, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addComponent(labelControl27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateControl7, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addComponent(labelControl28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(comboBoxControl3, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addComponent(labelControl30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl13, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addComponent(labelControl29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(codLookupControl12, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addComponent(labelControl31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(textControl8, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                     .addComponent(checkBoxControl4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel18Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {checkBoxControl4, labelControl25, labelControl26, labelControl27, labelControl28, labelControl29, labelControl30, labelControl31});
 
         jPanel18Layout.setVerticalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl25, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl11, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl26, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                     .addComponent(textControl7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl27, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateControl7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl28, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(comboBoxControl3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl30, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl29, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(codLookupControl12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelControl31, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(textControl8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(checkBoxControl4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(414, 414, 414))
         );
 
         jPanel18Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {checkBoxControl4, codLookupControl11, codLookupControl12, codLookupControl13, comboBoxControl3, dateControl7, labelControl25, labelControl26, labelControl27, labelControl28, labelControl29, labelControl30, labelControl31, textControl7, textControl8});
 
         formVida.add(jPanel18);
 
         jTabbedPane1.addTab("Vida", formVida);
 
         jPanel22.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 
         gridDiagnosticos.setDeleteButton(deleteButton9);
         gridDiagnosticos.setEditButton(editButton9);
         gridDiagnosticos.setMaxNumberOfRowsOnInsert(4);
         gridDiagnosticos.setReloadButton(reloadButton9);
         gridDiagnosticos.setSaveButton(saveButton9);
         gridDiagnosticos.setValueObjectClassName(DiagnosticoSiniestro.class.getName());
 
         decimalColumn7.setColumnName("id");
         decimalColumn7.setColumnRequired(false);
         decimalColumn7.setPreferredWidth(40);
         gridDiagnosticos.getColumnContainer().add(decimalColumn7);
 
         decimalColumn1.setColumnName("montoPendiente");
         gridDiagnosticos.getColumnContainer().add(decimalColumn1);
 
         decimalColumn2.setColumnName("montoPagado");
         gridDiagnosticos.getColumnContainer().add(decimalColumn2);
 
         textColumn13.setColumnName("diagnostico.nombre");
         textColumn13.setMaxCharacters(1024);
         gridDiagnosticos.getColumnContainer().add(textColumn13);
 
         textColumn14.setColumnName("diagnostico.especialidad.nombre");
         textColumn14.setMaxCharacters(1024);
         gridDiagnosticos.getColumnContainer().add(textColumn14);
 
         textColumn15.setColumnName("diagnostico.especialidad.ramo.nombre");
         textColumn15.setMaxCharacters(1024);
         gridDiagnosticos.getColumnContainer().add(textColumn15);
 
         checkBoxColumn2.setColumnName("auditoria.activo");
         gridDiagnosticos.getColumnContainer().add(checkBoxColumn2);
 
         textColumn16.setColumnName("auditoria.usuarioInsert");
         textColumn16.setColumnRequired(false);
         gridDiagnosticos.getColumnContainer().add(textColumn16);
 
         dateTimeColumn7.setColumnName("auditoria.fechaInsert");
         dateTimeColumn7.setColumnRequired(false);
         gridDiagnosticos.getColumnContainer().add(dateTimeColumn7);
 
         textColumn17.setColumnFilterable(true);
         textColumn17.setColumnName("auditoria.usuarioUpdate");
         textColumn17.setColumnRequired(false);
         textColumn17.setColumnSortable(true);
         gridDiagnosticos.getColumnContainer().add(textColumn17);
 
         dateTimeColumn8.setColumnFilterable(true);
         dateTimeColumn8.setColumnName("auditoria.fechaUpdate");
         dateTimeColumn8.setColumnRequired(false);
         dateTimeColumn8.setColumnSortable(true);
         gridDiagnosticos.getColumnContainer().add(dateTimeColumn8);
 
         jPanel23.setLayout(new java.awt.GridLayout(3, 2, 2, 2));
         jPanel23.add(insertButtonDiagnosticoSiniestro);
         jPanel23.add(editButton9);
         jPanel23.add(deleteButton9);
         jPanel23.add(saveButton9);
         jPanel23.add(reloadButton9);
 
         javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
         jPanel22.setLayout(jPanel22Layout);
         jPanel22Layout.setHorizontalGroup(
             jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel22Layout.createSequentialGroup()
                 .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gridDiagnosticos, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE))
         );
         jPanel22Layout.setVerticalGroup(
             jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel22Layout.createSequentialGroup()
                 .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(378, Short.MAX_VALUE))
             .addComponent(gridDiagnosticos, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout DiagnosticosLayout = new javax.swing.GroupLayout(Diagnosticos);
         Diagnosticos.setLayout(DiagnosticosLayout);
         DiagnosticosLayout.setHorizontalGroup(
             DiagnosticosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 662, Short.MAX_VALUE)
             .addGroup(DiagnosticosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(DiagnosticosLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         DiagnosticosLayout.setVerticalGroup(
             DiagnosticosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 502, Short.MAX_VALUE)
             .addGroup(DiagnosticosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(DiagnosticosLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
 
         jTabbedPane1.addTab("Diagnostico Siniestro", Diagnosticos);
 
         jPanel16.setLayout(new java.awt.GridLayout(3, 2, 2, 2));
 
         insertButtonPagos.setButtonId("asegurado");
         jPanel16.add(insertButtonPagos);
         jPanel16.add(editButtonPagos);
         jPanel16.add(deleteButtonPagos);
         jPanel16.add(saveButtonPagos);
         jPanel16.add(reloadButtonPagos);
         jPanel16.add(filterButtonPagos);
 
         gridControl6.setDefaultQuickFilterCriteria(org.openswing.swing.util.java.Consts.CONTAINS);
         gridControl6.setDeleteButton(deleteButtonPagos);
         gridControl6.setEditButton(editButtonPagos);
         gridControl6.setEditOnSingleRow(true);
         gridControl6.setFilterButton(filterButtonPagos);
         gridControl6.setMaxNumberOfRowsOnInsert(4);
         gridControl6.setReloadButton(reloadButtonPagos);
         gridControl6.setSaveButton(saveButtonPagos);
         gridControl6.setValueObjectClassName(Pago.class.getName());
 
         decimalColumn6.setColumnName("id");
         decimalColumn6.setColumnRequired(false);
         decimalColumn6.setColumnVisible(false);
         decimalColumn6.setPreferredWidth(40);
         gridControl6.getColumnContainer().add(decimalColumn6);
 
         textColumn3.setColumnName("numeroFactura");
         textColumn3.setColumnRequired(false);
         gridControl6.getColumnContainer().add(textColumn3);
 
         textColumn6.setColumnName("numeroControl");
         textColumn6.setColumnRequired(false);
         gridControl6.getColumnContainer().add(textColumn6);
 
         dateColumn1.setColumnName("fechaFactura");
         dateColumn1.setColumnRequired(false);
         gridControl6.getColumnContainer().add(dateColumn1);
 
         dateColumn2.setColumnName("fechaRecepcion");
         dateColumn2.setColumnRequired(false);
         gridControl6.getColumnContainer().add(dateColumn2);
 
         checkBoxColumn1.setColumnName("auditoria.activo");
         checkBoxColumn1.setColumnRequired(false);
         gridControl6.getColumnContainer().add(checkBoxColumn1);
 
         textColumn10.setColumnName("auditoria.usuarioInsert");
         textColumn10.setColumnRequired(false);
         gridControl6.getColumnContainer().add(textColumn10);
 
         dateTimeColumn5.setColumnName("auditoria.fechaInsert");
         dateTimeColumn5.setColumnRequired(false);
         gridControl6.getColumnContainer().add(dateTimeColumn5);
 
         textColumn11.setColumnName("auditoria.usuarioInsert");
         textColumn11.setColumnRequired(false);
         gridControl6.getColumnContainer().add(textColumn11);
 
         dateTimeColumn6.setColumnName("auditoria.fechaInsertUpdate");
         dateTimeColumn6.setColumnRequired(false);
         gridControl6.getColumnContainer().add(dateTimeColumn6);
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gridControl6, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE))
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(380, Short.MAX_VALUE))
             .addComponent(gridControl6, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout pagosLayout = new javax.swing.GroupLayout(pagos);
         pagos.setLayout(pagosLayout);
         pagosLayout.setHorizontalGroup(
             pagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pagosLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pagosLayout.setVerticalGroup(
             pagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pagosLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("Liquidacion", pagos);
 
         jPanel12.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 
         gridControl4.setDeleteButton(deleteButton6);
         gridControl4.setEditButton(editButton6);
         gridControl4.setInsertButton(insertButton6);
         gridControl4.setMaxNumberOfRowsOnInsert(4);
         gridControl4.setReloadButton(reloadButton6);
         gridControl4.setSaveButton(saveButton6);
         gridControl4.setValueObjectClassName(Observacion.class.getName());
         gridControl4.setVisibleStatusPanel(false);
 
         decimalColumn4.setColumnName("id");
         decimalColumn4.setColumnRequired(false);
         decimalColumn4.setColumnVisible(false);
         decimalColumn4.setPreferredWidth(40);
         gridControl4.getColumnContainer().add(decimalColumn4);
 
         textColumn1.setColumnName("observacion");
         textColumn1.setEditableOnEdit(true);
         textColumn1.setEditableOnInsert(true);
         textColumn1.setMaxCharacters(1024);
         textColumn1.setPreferredWidth(400);
         gridControl4.getColumnContainer().add(textColumn1);
 
         textColumn8.setColumnName("auditoria.usuarioInsert");
         textColumn8.setColumnRequired(false);
         gridControl4.getColumnContainer().add(textColumn8);
 
         dateTimeColumn1.setColumnName("auditoria.fechaInsert");
         dateTimeColumn1.setColumnRequired(false);
         dateTimeColumn1.setColumnSortable(true);
         dateTimeColumn1.setSortVersus(org.openswing.swing.util.java.Consts.DESC_SORTED);
         gridControl4.getColumnContainer().add(dateTimeColumn1);
 
         textColumn4.setColumnFilterable(true);
         textColumn4.setColumnName("auditoria.usuarioUpdate");
         textColumn4.setColumnRequired(false);
         textColumn4.setColumnSortable(true);
         gridControl4.getColumnContainer().add(textColumn4);
 
         dateTimeColumn2.setColumnFilterable(true);
         dateTimeColumn2.setColumnName("auditoria.fechaUpdate");
         dateTimeColumn2.setColumnRequired(false);
         dateTimeColumn2.setColumnSortable(true);
         gridControl4.getColumnContainer().add(dateTimeColumn2);
 
         jPanel13.setLayout(new java.awt.GridLayout(3, 2, 2, 2));
         jPanel13.add(insertButton6);
         jPanel13.add(editButton6);
         jPanel13.add(deleteButton6);
         jPanel13.add(saveButton6);
         jPanel13.add(reloadButton6);
 
         javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gridControl4, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE))
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(378, Short.MAX_VALUE))
             .addComponent(gridControl4, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout observacionesLayout = new javax.swing.GroupLayout(observaciones);
         observaciones.setLayout(observacionesLayout);
         observacionesLayout.setHorizontalGroup(
             observacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 662, Short.MAX_VALUE)
             .addGroup(observacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(observacionesLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         observacionesLayout.setVerticalGroup(
             observacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 502, Short.MAX_VALUE)
             .addGroup(observacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(observacionesLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
 
         jTabbedPane1.addTab("Observaciones", observaciones);
 
         jPanel14.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 
         gridControl5.setInsertButton(insertButton7);
         gridControl5.setMaxNumberOfRowsOnInsert(4);
         gridControl5.setReloadButton(reloadButton7);
         gridControl5.setSaveButton(saveButton7);
         gridControl5.setValueObjectClassName(NotaTecnica.class.getName());
         gridControl5.setVisibleStatusPanel(false);
 
         decimalColumn5.setColumnName("id");
         decimalColumn5.setColumnRequired(false);
         decimalColumn5.setColumnVisible(false);
         decimalColumn5.setPreferredWidth(40);
         gridControl5.getColumnContainer().add(decimalColumn5);
 
         textColumn2.setColumnName("observacion");
         textColumn2.setEditableOnEdit(true);
         textColumn2.setEditableOnInsert(true);
         textColumn2.setMaxCharacters(1024);
         textColumn2.setPreferredWidth(400);
         gridControl5.getColumnContainer().add(textColumn2);
 
         textColumn9.setColumnName("auditoria.usuarioInsert");
         textColumn9.setColumnRequired(false);
         gridControl5.getColumnContainer().add(textColumn9);
 
         dateTimeColumn3.setColumnName("auditoria.fechaInsert");
         dateTimeColumn3.setColumnRequired(false);
         dateTimeColumn3.setColumnSortable(true);
         dateTimeColumn3.setSortVersus(org.openswing.swing.util.java.Consts.DESC_SORTED);
         gridControl5.getColumnContainer().add(dateTimeColumn3);
 
         textColumn5.setColumnFilterable(true);
         textColumn5.setColumnName("auditoria.usuarioUpdate");
         textColumn5.setColumnRequired(false);
         textColumn5.setColumnSortable(true);
         gridControl5.getColumnContainer().add(textColumn5);
 
         dateTimeColumn4.setColumnFilterable(true);
         dateTimeColumn4.setColumnName("auditoria.fechaUpdate");
         dateTimeColumn4.setColumnRequired(false);
         dateTimeColumn4.setColumnSortable(true);
         gridControl5.getColumnContainer().add(dateTimeColumn4);
 
         jPanel15.setLayout(new java.awt.GridLayout(3, 2, 2, 2));
         jPanel15.add(insertButton7);
         jPanel15.add(saveButton7);
         jPanel15.add(reloadButton7);
 
         javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
         jPanel14.setLayout(jPanel14Layout);
         jPanel14Layout.setHorizontalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gridControl5, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE))
         );
         jPanel14Layout.setVerticalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(378, Short.MAX_VALUE))
             .addComponent(gridControl5, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout notasTecnicasLayout = new javax.swing.GroupLayout(notasTecnicas);
         notasTecnicas.setLayout(notasTecnicasLayout);
         notasTecnicasLayout.setHorizontalGroup(
             notasTecnicasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 662, Short.MAX_VALUE)
             .addGroup(notasTecnicasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(notasTecnicasLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         notasTecnicasLayout.setVerticalGroup(
             notasTecnicasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 502, Short.MAX_VALUE)
             .addGroup(notasTecnicasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(notasTecnicasLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
 
         jTabbedPane1.addTab("Notas Tecnicas", notasTecnicas);
 
         jPanel10.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 
         gridControl3.setDeleteButton(deleteButton5);
         gridControl3.setEditButton(editButton5);
         gridControl3.setFilterButton(filterButton5);
         gridControl3.setInsertButton(insertButton5);
         gridControl3.setMaxNumberOfRowsOnInsert(4);
         gridControl3.setReloadButton(reloadButton5);
         gridControl3.setSaveButton(saveButton5);
         gridControl3.setValueObjectClassName(Documento.class.getName());
         gridControl3.setVisibleStatusPanel(false);
 
         buttonColumn1.setColumnName("button");
         buttonColumn1.setEditableOnEdit(true);
         buttonColumn1.setEnableInReadOnlyMode(true);
         buttonColumn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/view.png"))); // NOI18N
         buttonColumn1.setPreferredWidth(20);
         buttonColumn1.setText("");
         gridControl3.getColumnContainer().add(buttonColumn1);
 
         integerColumn1.setColumnName("id");
         integerColumn1.setColumnRequired(false);
         integerColumn1.setPreferredWidth(40);
         gridControl3.getColumnContainer().add(integerColumn1);
 
         codLookupColumn2.setColumnName("tipoDocumento.nombre");
         codLookupColumn2.setControllerMethodName("getTipoDocAnex");
         codLookupColumn2.setEditableOnEdit(true);
         codLookupColumn2.setEditableOnInsert(true);
         gridControl3.getColumnContainer().add(codLookupColumn2);
 
         textColumn7.setColumnName("observacion");
         textColumn7.setColumnRequired(false);
         textColumn7.setEditableOnEdit(true);
         textColumn7.setEditableOnInsert(true);
         gridControl3.getColumnContainer().add(textColumn7);
 
         dateColumn3.setColumnName("fechaVencimiento");
         dateColumn3.setColumnRequired(false);
         dateColumn3.setEditableOnEdit(true);
         dateColumn3.setEditableOnInsert(true);
         gridControl3.getColumnContainer().add(dateColumn3);
 
         jPanel11.setLayout(new java.awt.GridLayout(3, 2, 2, 2));
         jPanel11.add(insertButton5);
         jPanel11.add(editButton5);
         jPanel11.add(deleteButton5);
         jPanel11.add(saveButton5);
         jPanel11.add(reloadButton5);
         jPanel11.add(filterButton5);
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(gridControl3, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addGap(12, 12, 12)
                 .addComponent(gridControl3, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout DocAnexosLayout = new javax.swing.GroupLayout(DocAnexos);
         DocAnexos.setLayout(DocAnexosLayout);
         DocAnexosLayout.setHorizontalGroup(
             DocAnexosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 662, Short.MAX_VALUE)
             .addGroup(DocAnexosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(DocAnexosLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         DocAnexosLayout.setVerticalGroup(
             DocAnexosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 502, Short.MAX_VALUE)
             .addGroup(DocAnexosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(DocAnexosLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addContainerGap()))
         );
 
         jTabbedPane1.addTab("Documentos Anexos", DocAnexos);
 
         gridReportes.setValueObjectClassName(Reporte.class.getName());
         gridReportes.setVisibleStatusPanel(false);
 
         textColumn12.setColumnName("titulo");
         textColumn12.setEditableOnEdit(true);
         textColumn12.setEditableOnInsert(true);
         textColumn12.setMaxCharacters(1024);
         textColumn12.setPreferredWidth(400);
         gridReportes.getColumnContainer().add(textColumn12);
 
         javax.swing.GroupLayout ReportesLayout = new javax.swing.GroupLayout(Reportes);
         Reportes.setLayout(ReportesLayout);
         ReportesLayout.setHorizontalGroup(
             ReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 662, Short.MAX_VALUE)
             .addGroup(ReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(ReportesLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(gridReportes, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
                     .addContainerGap()))
         );
         ReportesLayout.setVerticalGroup(
             ReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 502, Short.MAX_VALUE)
             .addGroup(ReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(ReportesLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(gridReportes, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                     .addContainerGap()))
         );
 
         jTabbedPane1.addTab("Reportes", Reportes);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(10, 10, 10)
                         .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 667, Short.MAX_VALUE))
                     .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     public BeanVO getBeanVO() {
         return (BeanVO) getMainPanel().getVOModel().getValueObject();
     }
 
     @Override
     public void inicializar(FormController formController, boolean addToMDIFrame) {
         initComponents();
         removeForms();
         org.openswing.swing.table.columns.client.PictureCaptureColumn pcc = new org.openswing.swing.table.columns.client.PictureCaptureColumn();
         pcc.setColumnName("file");
         pcc.setEditableOnInsert(true);
         pcc.setEditableOnEdit(false);
         pcc.setFileNameAttributeName("fileName");
         pcc.setPreferredWidth(220);
         gridControl3.getColumnContainer().add(pcc);
         controllerDocumentosAnexosX = new DefaultDocumentosAnexosGridController(DetalleSiniestro.class, gridControl3);
         buttonColumn1.addActionListener(controllerDocumentosAnexosX);
         gridControl3.setController(controllerDocumentosAnexosX);
         gridControl3.setGridDataLocator(controllerDocumentosAnexosX);
 
         com.jswitch.base.controlador.documentosAnexos.TipoDocumentoLookupController lookupDocumentoAnexo2 =
                 new TipoDocumentoLookupController(com.jswitch.base.modelo.Dominios.Modulos.PERSONAS);
         lookupDocumentoAnexo2.addLookup2ParentLink("tipoDocumento");
         codLookupColumn2.setLookupController(lookupDocumentoAnexo2);
 
         controllerObservaciones =
                 new DefaultGridInternalController(DetalleSiniestro.class.getName(), "getObservaciones", gridControl4);
         gridControl4.setGridDataLocator(controllerObservaciones);
         gridControl4.setController(controllerObservaciones);
 
         controllerNotasTecnicas =
                 new DefaultGridInternalController(DetalleSiniestro.class.getName(), "getNotasTecnicas", gridControl5);
         gridControl5.setGridDataLocator(controllerNotasTecnicas);
         gridControl5.setController(controllerNotasTecnicas);
 
        controllerPagos = new PagoGridInternalController(gridControl6);
         gridControl6.setGridDataLocator(controllerPagos);
         gridControl6.setController(controllerPagos);
 
         controllerReportes =
                 new ReporteGridInternalController(Reembolso.class.getName(), "getReportes", gridReportes);
         gridReportes.setGridDataLocator(controllerReportes);
         gridReportes.setController(controllerReportes);
 
         controllerDiagnosticoSiniesto =
                 new DiagnosticoSiniestroGridInternalController(DetalleSiniestro.class.getName(), "getDiagnosticoSiniestros", gridDiagnosticos, this);
         gridDiagnosticos.setGridDataLocator(controllerDiagnosticoSiniesto);
         gridDiagnosticos.setController(controllerDiagnosticoSiniesto);
 
         String[] tipoPersonaPago = null;
         if (tipoDetalle.equals(Funerario.class)) {
             tipoPersonaPago = new String[]{"CLI", "FUN", "MED"};
         } else if (tipoDetalle.equals(Vida.class)) {
             tipoPersonaPago = new String[]{"BEN"};
 
         } else {
             tipoPersonaPago = new String[]{"CLI", "MED", "LAB"};
         }
         lookupPersonaPago = new PersonaTipoLookupController(tipoPersonaPago);
         PersonaLookupControllerPorNombre p = new PersonaLookupControllerPorNombre("NEG");
         p.addLookup2ParentLink("analistaNegociador");
         SiniestroLookupController tipoController = new SiniestroLookupController(TipoSiniestro.class.getName(), tipoDetalle.getName());
         tipoController.addLookup2ParentLink("tipoSiniestro");
         SiniestroLookupController estadoController = new SiniestroLookupController(EtapaSiniestro.class.getName(), tipoDetalle.getName());
         estadoController.addLookup2ParentLink("etapaSiniestro");
         CodLookupControl persona = null, negociador = null, tipoSiniestro = null, etapaSiniestro = null;
         if (tipoDetalle.equals(APS.class)) {
             persona = codLookupControl5;
             tipoSiniestro = codLookupControl6;
             etapaSiniestro = codLookupControl7;
         } else if (tipoDetalle.equals(AyudaSocial.class)) {
             persona = codLookupControl22;
             negociador = codLookupControl25;
             tipoSiniestro = codLookupControl23;
             etapaSiniestro = codLookupControl24;
         } else if (tipoDetalle.equals(CartaAval.class)) {
             persona = codLookupControl17;
             negociador = codLookupControl20;
             tipoSiniestro = codLookupControl18;
             etapaSiniestro = codLookupControl9;
         } else if (tipoDetalle.equals(Emergencia.class)) {
             persona = codLookupControl14;
             negociador = codLookupControl21;
             tipoSiniestro = codLookupControl16;
             etapaSiniestro = codLookupControl15;
         } else if (tipoDetalle.equals(Funerario.class)) {
             persona = codLookupControl8;
             tipoSiniestro = codLookupControl10;
             etapaSiniestro = codLookupControl9;
         } else if (tipoDetalle.equals(Reembolso.class)) {
             persona = codLookupControl1;
             negociador = codLookupControl2;
             tipoSiniestro = codLookupControl3;
             etapaSiniestro = codLookupControl4;
         } else if (tipoDetalle.equals(Vida.class)) {
             persona = codLookupControl11;
             tipoSiniestro = codLookupControl13;
             etapaSiniestro = codLookupControl12;
         }
         if (persona != null) {
             persona.setLookupController(lookupPersonaPago);
             persona.setOpenDetail("personaPago",
                     PersonasDetailController.class.getName(),
                     new Class[]{GridControl.class, BeanVO.class, Rif.class}, new Object[]{null, null, null}, 1);
         }
         if (negociador != null) {
             negociador.setLookupController(p);
             negociador.setOpenDetail("analistaNegociador",
                     PersonasDetailController.class.getName(),
                     new Class[]{GridControl.class, BeanVO.class, Rif.class}, new Object[]{null, null, null}, 1);
         }
         if (tipoSiniestro != null) {
             tipoSiniestro.setLookupController(tipoController);
             tipoSiniestro.setControllerMethodName("getTipoSiniestro");
         }
         if (etapaSiniestro != null) {
             etapaSiniestro.setLookupController(estadoController);
             etapaSiniestro.setControllerMethodName("getEtapaSiniestro");
         }
 //----------------------------------------------
 
 
         insertButtonPagos.addActionListener((ActionListener) formController);
         insertButtonDiagnosticoSiniestro.addActionListener((ActionListener) formController);
 
         formAPS.setCreateInnerVO(false);
         formAPS.setFormController(formController);
 
         formAyudaSocial.setCreateInnerVO(false);
         formAyudaSocial.setFormController(formController);
 
         formCartaAval.setCreateInnerVO(false);
         formCartaAval.setFormController(formController);
 
         formEmergencia.setCreateInnerVO(false);
         formEmergencia.setFormController(formController);
 
         formFunenario.setCreateInnerVO(false);
         formFunenario.setFormController(formController);
 
         formRembolso.setCreateInnerVO(false);
         formRembolso.setFormController(formController);
 
         formVida.setCreateInnerVO(false);
         formVida.setFormController(formController);
 
         if (addToMDIFrame) {
             pack();
         } else {
             setBounds(0, 0, 0, 0);
         }
         MDIFrame.add(this);
     }
 
     @Override
     public void setOwnerVO(BeanVO beanVO) {
         Long id = null;
         if (beanVO != null) {
             id = ((DetalleSiniestro) beanVO).getId();
         }
         controllerDocumentosAnexosX.setBeanVO(beanVO, id);
         controllerObservaciones.setBeanVO(beanVO);
         controllerNotasTecnicas.setBeanVO(beanVO);
         controllerReportes.setBeanVO(beanVO);
         controllerPagos.setBeanVO(beanVO);
         controllerDiagnosticoSiniesto.setBeanVO(beanVO);
         reloadGridsData();
     }
 
     @Override
     public Form getMainPanel() {
         if (tipoDetalle.equals(APS.class)) {
             return formAPS;
         }
         if (tipoDetalle.equals(AyudaSocial.class)) {
             return formAyudaSocial;
         }
         if (tipoDetalle.equals(CartaAval.class)) {
             return formCartaAval;
         }
         if (tipoDetalle.equals(Emergencia.class)) {
             return formEmergencia;
         }
         if (tipoDetalle.equals(Funerario.class)) {
             return formFunenario;
         }
         if (tipoDetalle.equals(Reembolso.class)) {
             return formRembolso;
         }
         if (tipoDetalle.equals(Vida.class)) {
             return formVida;
         }
         return null;
     }
 
     @Override
     public void reloadGridsData() {
         gridControl3.getReloadButton().doClick();
         gridControl4.getReloadButton().doClick();
         gridControl5.getReloadButton().doClick();
         gridControl6.getReloadButton().doClick();
         gridDiagnosticos.getReloadButton().doClick();
     }
 
     @Override
     public void clearGridsData() {
         gridControl3.clearData();
         gridControl4.clearData();
         gridControl5.clearData();
         gridControl6.clearData();
         gridDiagnosticos.clearData();
     }
 
     @Override
     public void saveGridsData() {
         gridControl3.getSaveButton().doClick();
         gridControl4.getSaveButton().doClick();
         gridControl5.getSaveButton().doClick();
         gridControl6.getSaveButton().doClick();
         gridDiagnosticos.getSaveButton().doClick();
     }
 
     @Override
     public void modeChanged(int currentMode) {
         if (currentMode == Consts.INSERT) {
             clearGridsData();
         }
         if (currentMode == Consts.INSERT) {
             setEnableGridInternalButtons(false);
         } else {
             setEnableGridInternalButtons(true);
         }
     }
 
     private void setEnableGridInternalButtons(boolean enabled) {
         jPanel11.setVisible(enabled);
         jPanel13.setVisible(enabled);
         jPanel15.setVisible(enabled);
         jPanel16.setVisible(enabled);
         jPanel23.setVisible(enabled);
     }
 
     public TipoPersona getTipoPersonaSelected() {
         return lookupPersonaPago.getTipoPersona();
     }
 
     public InsertButton getInsertButtonDiagnostico() {
         return insertButtonDiagnosticoSiniestro;
     }
 
     public InsertButton getInsertButtonPagos() {
         return insertButtonPagos;
     }
 
     public void setTipo(Class tipoDetalle) {
         this.tipoDetalle = tipoDetalle;
     }
 
     public void removeForms() {
         Form form = null;
         if (tipoDetalle.equals(APS.class)) {
             form = formAPS;
             jTabbedPane1.remove(formAyudaSocial);
             jTabbedPane1.remove(formCartaAval);
             jTabbedPane1.remove(formEmergencia);
             jTabbedPane1.remove(formFunenario);
             jTabbedPane1.remove(formRembolso);
             jTabbedPane1.remove(formVida);
         } else if (tipoDetalle.equals(AyudaSocial.class)) {
             form = formAyudaSocial;
             jTabbedPane1.remove(formAPS);
             jTabbedPane1.remove(formCartaAval);
             jTabbedPane1.remove(formEmergencia);
             jTabbedPane1.remove(formFunenario);
             jTabbedPane1.remove(formRembolso);
             jTabbedPane1.remove(formVida);
         } else if (tipoDetalle.equals(CartaAval.class)) {
             form = formCartaAval;
             jTabbedPane1.remove(formAPS);
             jTabbedPane1.remove(formAyudaSocial);
             jTabbedPane1.remove(formEmergencia);
             jTabbedPane1.remove(formFunenario);
             jTabbedPane1.remove(formRembolso);
             jTabbedPane1.remove(formVida);
         } else if (tipoDetalle.equals(Emergencia.class)) {
             form = formEmergencia;
             jTabbedPane1.remove(formAPS);
             jTabbedPane1.remove(formAyudaSocial);
             jTabbedPane1.remove(formCartaAval);
             jTabbedPane1.remove(formFunenario);
             jTabbedPane1.remove(formRembolso);
             jTabbedPane1.remove(formVida);
         } else if (tipoDetalle.equals(Funerario.class)) {
             form = formFunenario;
             jTabbedPane1.remove(formAPS);
             jTabbedPane1.remove(formAyudaSocial);
             jTabbedPane1.remove(formCartaAval);
             jTabbedPane1.remove(formEmergencia);
             jTabbedPane1.remove(formRembolso);
             jTabbedPane1.remove(formVida);
         } else if (tipoDetalle.equals(Reembolso.class)) {
             form = formRembolso;
             jTabbedPane1.remove(formAPS);
             jTabbedPane1.remove(formAyudaSocial);
             jTabbedPane1.remove(formCartaAval);
             jTabbedPane1.remove(formEmergencia);
             jTabbedPane1.remove(formFunenario);
             jTabbedPane1.remove(formVida);
         } else if (tipoDetalle.equals(Vida.class)) {
             form = formVida;
             jTabbedPane1.remove(formAPS);
             jTabbedPane1.remove(formAyudaSocial);
             jTabbedPane1.remove(formCartaAval);
             jTabbedPane1.remove(formEmergencia);
             jTabbedPane1.remove(formFunenario);
             jTabbedPane1.remove(formRembolso);
         }
         form.setEditButton(editButton1);
         form.setSaveButton(saveButton1);
         this.pack();
     }
 
     public GridControl getGridDiagnosticos() {
         return gridDiagnosticos;
     }
 
     public GridControl getGridPagos() {
         return gridControl6;
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel Diagnosticos;
     private javax.swing.JPanel DocAnexos;
     private javax.swing.JPanel Reportes;
     private org.openswing.swing.table.columns.client.ButtonColumn buttonColumn1;
     private org.openswing.swing.table.columns.client.CheckBoxColumn checkBoxColumn1;
     private org.openswing.swing.table.columns.client.CheckBoxColumn checkBoxColumn2;
     private org.openswing.swing.client.CheckBoxControl checkBoxControl1;
     private org.openswing.swing.client.CheckBoxControl checkBoxControl2;
     private org.openswing.swing.client.CheckBoxControl checkBoxControl3;
     private org.openswing.swing.client.CheckBoxControl checkBoxControl4;
     private org.openswing.swing.client.CheckBoxControl checkBoxControl5;
     private org.openswing.swing.client.CheckBoxControl checkBoxControl6;
     private org.openswing.swing.client.CheckBoxControl checkBoxControl7;
     private org.openswing.swing.table.columns.client.CodLookupColumn codLookupColumn2;
     private org.openswing.swing.client.CodLookupControl codLookupControl1;
     private org.openswing.swing.client.CodLookupControl codLookupControl10;
     private org.openswing.swing.client.CodLookupControl codLookupControl11;
     private org.openswing.swing.client.CodLookupControl codLookupControl12;
     private org.openswing.swing.client.CodLookupControl codLookupControl13;
     private org.openswing.swing.client.CodLookupControl codLookupControl14;
     private org.openswing.swing.client.CodLookupControl codLookupControl15;
     private org.openswing.swing.client.CodLookupControl codLookupControl16;
     private org.openswing.swing.client.CodLookupControl codLookupControl17;
     private org.openswing.swing.client.CodLookupControl codLookupControl18;
     private org.openswing.swing.client.CodLookupControl codLookupControl19;
     private org.openswing.swing.client.CodLookupControl codLookupControl2;
     private org.openswing.swing.client.CodLookupControl codLookupControl20;
     private org.openswing.swing.client.CodLookupControl codLookupControl21;
     private org.openswing.swing.client.CodLookupControl codLookupControl22;
     private org.openswing.swing.client.CodLookupControl codLookupControl23;
     private org.openswing.swing.client.CodLookupControl codLookupControl24;
     private org.openswing.swing.client.CodLookupControl codLookupControl25;
     private org.openswing.swing.client.CodLookupControl codLookupControl3;
     private org.openswing.swing.client.CodLookupControl codLookupControl4;
     private org.openswing.swing.client.CodLookupControl codLookupControl5;
     private org.openswing.swing.client.CodLookupControl codLookupControl6;
     private org.openswing.swing.client.CodLookupControl codLookupControl7;
     private org.openswing.swing.client.CodLookupControl codLookupControl8;
     private org.openswing.swing.client.CodLookupControl codLookupControl9;
     private org.openswing.swing.client.ComboBoxControl comboBoxControl1;
     private org.openswing.swing.client.ComboBoxControl comboBoxControl2;
     private org.openswing.swing.client.ComboBoxControl comboBoxControl3;
     private org.openswing.swing.client.ComboBoxControl comboBoxControl4;
     private org.openswing.swing.client.ComboBoxControl comboBoxControl5;
     private org.openswing.swing.client.ComboBoxControl comboBoxControl6;
     private org.openswing.swing.table.columns.client.DateColumn dateColumn1;
     private org.openswing.swing.table.columns.client.DateColumn dateColumn2;
     private org.openswing.swing.table.columns.client.DateColumn dateColumn3;
     private org.openswing.swing.client.DateControl dateControl1;
     private org.openswing.swing.client.DateControl dateControl10;
     private org.openswing.swing.client.DateControl dateControl11;
     private org.openswing.swing.client.DateControl dateControl12;
     private org.openswing.swing.client.DateControl dateControl13;
     private org.openswing.swing.client.DateControl dateControl2;
     private org.openswing.swing.client.DateControl dateControl3;
     private org.openswing.swing.client.DateControl dateControl4;
     private org.openswing.swing.client.DateControl dateControl5;
     private org.openswing.swing.client.DateControl dateControl6;
     private org.openswing.swing.client.DateControl dateControl7;
     private org.openswing.swing.client.DateControl dateControl8;
     private org.openswing.swing.client.DateControl dateControl9;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn1;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn2;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn3;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn4;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn5;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn6;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn7;
     private org.openswing.swing.table.columns.client.DateTimeColumn dateTimeColumn8;
     private org.openswing.swing.table.columns.client.DecimalColumn decimalColumn1;
     private org.openswing.swing.table.columns.client.DecimalColumn decimalColumn2;
     private org.openswing.swing.table.columns.client.DecimalColumn decimalColumn4;
     private org.openswing.swing.table.columns.client.DecimalColumn decimalColumn5;
     private org.openswing.swing.table.columns.client.DecimalColumn decimalColumn6;
     private org.openswing.swing.table.columns.client.DecimalColumn decimalColumn7;
     private org.openswing.swing.client.DeleteButton deleteButton5;
     private org.openswing.swing.client.DeleteButton deleteButton6;
     private org.openswing.swing.client.DeleteButton deleteButton9;
     private org.openswing.swing.client.DeleteButton deleteButtonPagos;
     private org.openswing.swing.client.EditButton editButton1;
     private org.openswing.swing.client.EditButton editButton5;
     private org.openswing.swing.client.EditButton editButton6;
     private org.openswing.swing.client.EditButton editButton9;
     private org.openswing.swing.client.EditButton editButtonPagos;
     private org.openswing.swing.client.FilterButton filterButton5;
     private org.openswing.swing.client.FilterButton filterButtonPagos;
     private org.openswing.swing.form.client.Form formAPS;
     private org.openswing.swing.form.client.Form formAyudaSocial;
     private org.openswing.swing.form.client.Form formCartaAval;
     private org.openswing.swing.form.client.Form formEmergencia;
     private org.openswing.swing.form.client.Form formFunenario;
     private org.openswing.swing.form.client.Form formRembolso;
     private org.openswing.swing.form.client.Form formVida;
     private org.openswing.swing.client.GridControl gridControl3;
     private org.openswing.swing.client.GridControl gridControl4;
     private org.openswing.swing.client.GridControl gridControl5;
     private org.openswing.swing.client.GridControl gridControl6;
     private org.openswing.swing.client.GridControl gridDiagnosticos;
     private org.openswing.swing.client.GridControl gridReportes;
     private org.openswing.swing.client.InsertButton insertButton5;
     private org.openswing.swing.client.InsertButton insertButton6;
     private org.openswing.swing.client.InsertButton insertButton7;
     private org.openswing.swing.client.InsertButton insertButtonDiagnosticoSiniestro;
     private org.openswing.swing.client.InsertButton insertButtonPagos;
     private org.openswing.swing.table.columns.client.IntegerColumn integerColumn1;
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
     private javax.swing.JPanel jPanel22;
     private javax.swing.JPanel jPanel23;
     private javax.swing.JPanel jPanel24;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JTabbedPane jTabbedPane1;
     private org.openswing.swing.client.LabelControl labelControl1;
     private org.openswing.swing.client.LabelControl labelControl10;
     private org.openswing.swing.client.LabelControl labelControl11;
     private org.openswing.swing.client.LabelControl labelControl12;
     private org.openswing.swing.client.LabelControl labelControl13;
     private org.openswing.swing.client.LabelControl labelControl14;
     private org.openswing.swing.client.LabelControl labelControl15;
     private org.openswing.swing.client.LabelControl labelControl16;
     private org.openswing.swing.client.LabelControl labelControl17;
     private org.openswing.swing.client.LabelControl labelControl18;
     private org.openswing.swing.client.LabelControl labelControl19;
     private org.openswing.swing.client.LabelControl labelControl2;
     private org.openswing.swing.client.LabelControl labelControl20;
     private org.openswing.swing.client.LabelControl labelControl21;
     private org.openswing.swing.client.LabelControl labelControl22;
     private org.openswing.swing.client.LabelControl labelControl23;
     private org.openswing.swing.client.LabelControl labelControl24;
     private org.openswing.swing.client.LabelControl labelControl25;
     private org.openswing.swing.client.LabelControl labelControl26;
     private org.openswing.swing.client.LabelControl labelControl27;
     private org.openswing.swing.client.LabelControl labelControl28;
     private org.openswing.swing.client.LabelControl labelControl29;
     private org.openswing.swing.client.LabelControl labelControl3;
     private org.openswing.swing.client.LabelControl labelControl30;
     private org.openswing.swing.client.LabelControl labelControl31;
     private org.openswing.swing.client.LabelControl labelControl32;
     private org.openswing.swing.client.LabelControl labelControl33;
     private org.openswing.swing.client.LabelControl labelControl34;
     private org.openswing.swing.client.LabelControl labelControl35;
     private org.openswing.swing.client.LabelControl labelControl36;
     private org.openswing.swing.client.LabelControl labelControl37;
     private org.openswing.swing.client.LabelControl labelControl38;
     private org.openswing.swing.client.LabelControl labelControl39;
     private org.openswing.swing.client.LabelControl labelControl4;
     private org.openswing.swing.client.LabelControl labelControl40;
     private org.openswing.swing.client.LabelControl labelControl41;
     private org.openswing.swing.client.LabelControl labelControl42;
     private org.openswing.swing.client.LabelControl labelControl43;
     private org.openswing.swing.client.LabelControl labelControl44;
     private org.openswing.swing.client.LabelControl labelControl45;
     private org.openswing.swing.client.LabelControl labelControl46;
     private org.openswing.swing.client.LabelControl labelControl47;
     private org.openswing.swing.client.LabelControl labelControl48;
     private org.openswing.swing.client.LabelControl labelControl49;
     private org.openswing.swing.client.LabelControl labelControl5;
     private org.openswing.swing.client.LabelControl labelControl50;
     private org.openswing.swing.client.LabelControl labelControl51;
     private org.openswing.swing.client.LabelControl labelControl52;
     private org.openswing.swing.client.LabelControl labelControl53;
     private org.openswing.swing.client.LabelControl labelControl54;
     private org.openswing.swing.client.LabelControl labelControl55;
     private org.openswing.swing.client.LabelControl labelControl56;
     private org.openswing.swing.client.LabelControl labelControl57;
     private org.openswing.swing.client.LabelControl labelControl58;
     private org.openswing.swing.client.LabelControl labelControl59;
     private org.openswing.swing.client.LabelControl labelControl6;
     private org.openswing.swing.client.LabelControl labelControl60;
     private org.openswing.swing.client.LabelControl labelControl61;
     private org.openswing.swing.client.LabelControl labelControl62;
     private org.openswing.swing.client.LabelControl labelControl63;
     private org.openswing.swing.client.LabelControl labelControl64;
     private org.openswing.swing.client.LabelControl labelControl65;
     private org.openswing.swing.client.LabelControl labelControl66;
     private org.openswing.swing.client.LabelControl labelControl67;
     private org.openswing.swing.client.LabelControl labelControl7;
     private org.openswing.swing.client.LabelControl labelControl8;
     private org.openswing.swing.client.LabelControl labelControl9;
     private javax.swing.JPanel notasTecnicas;
     private org.openswing.swing.client.NumericControl numericControl1;
     private org.openswing.swing.client.NumericControl numericControl2;
     private org.openswing.swing.client.NumericControl numericControl3;
     private org.openswing.swing.client.NumericControl numericControl4;
     private org.openswing.swing.client.NumericControl numericControl5;
     private org.openswing.swing.client.NumericControl numericControl6;
     private javax.swing.JPanel observaciones;
     private javax.swing.JPanel pagos;
     private org.openswing.swing.client.ReloadButton reloadButton5;
     private org.openswing.swing.client.ReloadButton reloadButton6;
     private org.openswing.swing.client.ReloadButton reloadButton7;
     private org.openswing.swing.client.ReloadButton reloadButton9;
     private org.openswing.swing.client.ReloadButton reloadButtonPagos;
     private org.openswing.swing.client.SaveButton saveButton1;
     private org.openswing.swing.client.SaveButton saveButton5;
     private org.openswing.swing.client.SaveButton saveButton6;
     private org.openswing.swing.client.SaveButton saveButton7;
     private org.openswing.swing.client.SaveButton saveButton9;
     private org.openswing.swing.client.SaveButton saveButtonPagos;
     private org.openswing.swing.table.columns.client.TextColumn textColumn1;
     private org.openswing.swing.table.columns.client.TextColumn textColumn10;
     private org.openswing.swing.table.columns.client.TextColumn textColumn11;
     private org.openswing.swing.table.columns.client.TextColumn textColumn12;
     private org.openswing.swing.table.columns.client.TextColumn textColumn13;
     private org.openswing.swing.table.columns.client.TextColumn textColumn14;
     private org.openswing.swing.table.columns.client.TextColumn textColumn15;
     private org.openswing.swing.table.columns.client.TextColumn textColumn16;
     private org.openswing.swing.table.columns.client.TextColumn textColumn17;
     private org.openswing.swing.table.columns.client.TextColumn textColumn2;
     private org.openswing.swing.table.columns.client.TextColumn textColumn3;
     private org.openswing.swing.table.columns.client.TextColumn textColumn4;
     private org.openswing.swing.table.columns.client.TextColumn textColumn5;
     private org.openswing.swing.table.columns.client.TextColumn textColumn6;
     private org.openswing.swing.table.columns.client.TextColumn textColumn7;
     private org.openswing.swing.table.columns.client.TextColumn textColumn8;
     private org.openswing.swing.table.columns.client.TextColumn textColumn9;
     private org.openswing.swing.client.TextControl textControl1;
     private org.openswing.swing.client.TextControl textControl10;
     private org.openswing.swing.client.TextControl textControl11;
     private org.openswing.swing.client.TextControl textControl12;
     private org.openswing.swing.client.TextControl textControl13;
     private org.openswing.swing.client.TextControl textControl14;
     private org.openswing.swing.client.TextControl textControl15;
     private org.openswing.swing.client.TextControl textControl16;
     private org.openswing.swing.client.TextControl textControl17;
     private org.openswing.swing.client.TextControl textControl2;
     private org.openswing.swing.client.TextControl textControl3;
     private org.openswing.swing.client.TextControl textControl4;
     private org.openswing.swing.client.TextControl textControl5;
     private org.openswing.swing.client.TextControl textControl6;
     private org.openswing.swing.client.TextControl textControl7;
     private org.openswing.swing.client.TextControl textControl8;
     private org.openswing.swing.client.TextControl textControl9;
     // End of variables declaration//GEN-END:variables
 }
