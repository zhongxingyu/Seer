 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package atencionurgencia.ingreso;
 
 import atencionurgencia.ListadoPacientes.addMedicamentos;
 import atencionurgencia.AtencionUrgencia;
 import atencionurgencia.ListadoPacientes.Ftriaje;
import entidades.Configdecripcionlogin;
 import entidades.InfoAdmision;
 import entidades.InfoAntPersonales;
 import entidades.InfoHcExpfisica;
 import entidades.InfoHistoriac;
 import entidades.InfoPaciente;
 import entidades.InfoPruebasComplement;
 import entidades.StaticCie10;
 import java.awt.Color;
 import java.awt.Event;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.DefaultTableModel;
import jpa.ConfigloginJpaController;
 import jpa.InfoAntPersonalesJpaController;
 import jpa.InfoHcExpfisicaJpaController;
 import jpa.InfoHistoriacJpaController;
 import jpa.InfoPruebasComplementJpaController;
 import jpa.StaticCie10JpaController;
 import jpa.exceptions.IllegalOrphanException;
 import jpa.exceptions.NonexistentEntityException;
 import other.*;
 import tools.Funciones;
 import tools.myStringsFunctions;
 
 
 
 /**
  *
  * @author Alvaro Monsalve
  */
 public class HC extends javax.swing.JPanel {
 
     // <editor-fold desc="Declaracion">
     private cie10 cie=null;
     FormPersonas personas=null;
     public InfoPaciente infopaciente = null;
     public InfoHistoriac infohistoriac=null;
     public InfoAdmision infoadmision =null;
     private InfoHcExpfisica infoexploracion = null;
     private StaticCie10 staticCie10 = null;
     private StaticCie10JpaController staticcie=null;
     private InfoPruebasComplement infoPruebasComplement = null;
     private EntityManagerFactory factory;
     private InfoHistoriacJpaController infohistoriaJPA=null;
     private InfoHcExpfisicaJpaController infohsfisicoJPA=null;
     private InfoPruebasComplementJpaController infoPruebasComplementJPA = null;
     public int idDiag1 = 1,idDiag2= 1,idDiag3= 1,idDiag4= 1,idDiag5= 1;
     public DefaultTableModel modeloAyudDiag,modDestroyAyudDiag;
     private addMedicamentos trat=null;
     public int finalizar = 0;
     private Boolean edite = false;
     private Object dato[] = null;
     private String tipoAyudaDiag[]={"LABORATORIO","IMAGEN","ENDOSCOPIA","ANATOMIA PATOLOGICA","ELECTROGRAMAS","ESTUDIOS ALERGOLÓGICOS"};
     private List<InfoPruebasComplement> listaPruebas = null;
     private String s = System.getProperty("file.separator");
     private InfoAntPersonalesJpaController antPersonalesJPA=null;
     private InfoAntPersonales antPersonales;
     public pTratMedic pMedic=null;
     public pTratPConsultDiag pConsultDiag=null;
     public pTratLaboratorio pLaboratorio=null;
     public pTratImagenologia pImagenologia=null;
     public pTratQuirurgico pQuirurgico=null;
     public pTratMasProcedimientos pProcedimientos=null;
     public pTratInterconsulta pInterconsulta0=null,pInterconsulta1=null,pInterconsulta2=null
             ,pInterconsulta3=null,pInterconsulta4=null;
     public pTratOtrasInterconsultas pOtrasInterconsultas=null;
     public pTratMedidaGeneral pMedidaGeneral=null;
     public pAnexo3 pAnexo31=null;
     public Ftriaje ftriaje=null;
     // </editor-fold>
     
     /**
      * Creates new form HC
      */
     public HC() {
         initComponents();
         TablaAyudDiag();
         inicio();
         InputMap map2 = jTextArea10.getInputMap(JTextArea.WHEN_FOCUSED);
         map2.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK), "null");
     }    
     
     private void inicio(){
             jpCentro.removeAll();
             jpMotivoC.setBounds(0, 0, 584, 472);
             jpCentro.add(jpMotivoC);
             jpMotivoC.setVisible(true);
             jpCentro.validate();
             jpCentro.repaint();
             activeCheck(1);
     }
     
     private void activeCheck(int val){
         switch(val){
             case 1:
                 jCheckBox6.setSelected(true);
                 jCheckBox10.setSelected(false);
                 jCheckBox11.setSelected(false);
                 jCheckBox12.setSelected(false);
                 jCheckBox4.setSelected(false);
                 jCheckBox13.setSelected(false);
                 jCheckBox5.setSelected(false);
                 break;
             case 2:
                 jCheckBox6.setSelected(false);
                 jCheckBox10.setSelected(true);
                 jCheckBox11.setSelected(false);
                 jCheckBox12.setSelected(false);
                 jCheckBox4.setSelected(false);
                 jCheckBox13.setSelected(false);
                 jCheckBox5.setSelected(false);
                 break;
             case 3:
                 jCheckBox6.setSelected(false);
                 jCheckBox10.setSelected(false);
                 jCheckBox11.setSelected(true);
                 jCheckBox12.setSelected(false);
                 jCheckBox4.setSelected(false);
                 jCheckBox13.setSelected(false);
                 jCheckBox5.setSelected(false);
                 break;
             case 4:
                 jCheckBox6.setSelected(false);
                 jCheckBox10.setSelected(false);
                 jCheckBox11.setSelected(false);
                 jCheckBox12.setSelected(true);
                 jCheckBox4.setSelected(false);
                 jCheckBox13.setSelected(false);
                 jCheckBox5.setSelected(false);
                 break;
             case 5:
                 jCheckBox6.setSelected(false);
                 jCheckBox10.setSelected(false);
                 jCheckBox11.setSelected(false);
                 jCheckBox12.setSelected(false);
                 jCheckBox4.setSelected(true);
                 jCheckBox13.setSelected(false);
                 jCheckBox5.setSelected(false);
                 break;
             case 6:
                 jCheckBox6.setSelected(false);
                 jCheckBox10.setSelected(false);
                 jCheckBox11.setSelected(false);
                 jCheckBox12.setSelected(false);
                 jCheckBox4.setSelected(false);
                 jCheckBox13.setSelected(true);
                 jCheckBox5.setSelected(false);
                 break;
             case 7:
                 jCheckBox6.setSelected(false);
                 jCheckBox10.setSelected(false);
                 jCheckBox11.setSelected(false);
                 jCheckBox12.setSelected(false);
                 jCheckBox4.setSelected(false);
                 jCheckBox13.setSelected(false);
                 jCheckBox5.setSelected(true);
                 break;
         }
     }
  
         private Integer getSelectionNivelTriage(){
             if(jRadioButton1.isSelected()){
                 return 0;
             }else if(jRadioButton2.isSelected()){
                 return 1;
             }else if(jRadioButton3.isSelected()){
                 return 2;
             }else{
                 return 3;
             }
          }
         
         public void setSelectionNivelTriage(int var){
             switch (var){
                 case 0:
                     jRadioButton1.setSelected(true);
                     break;
                 case 1:
                     jRadioButton2.setSelected(true);
                     break;
                 case 2:
                     jRadioButton3.setSelected(true);
                     break;
                 case 3:
                     jRadioButton4.setSelected(true);
                     break;
             }
         }
         
         private DefaultTableModel getModAyudaDiag(){
             try {
                 return (new DefaultTableModel(
                 null, new String [] {"imagen","iamgen2","archivo","file","tipo","ruta","bd"}){
                 Class[] types = new Class [] {
                      javax.swing.JLabel.class,
                      javax.swing.JLabel.class,
                      java.lang.String.class,
                      java.io.File.class,
                      java.lang.String.class,
                      java.lang.String.class,
                      java.lang.String.class
                 };
 
                 boolean[] canEdit = new boolean [] {
                 false,false,false,false,false,false,false
                 };
                 @Override
                 public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                 }
                 @Override
                 public boolean isCellEditable(int rowIndex, int colIndex){
                    return canEdit [colIndex];
                 }
             });
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(null, "10071:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                 return null;
             }
         }
         
         
     private void TablaAyudDiag(){
         try {
            modeloAyudDiag = getModAyudaDiag();
            modDestroyAyudDiag = getModAyudaDiag();
            jtbTratamiento4.setModel(modeloAyudDiag);
            jtbTratamiento4.getTableHeader().setReorderingAllowed(false);
            jtbTratamiento4.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            Funciones.setSizeColumnas(jtbTratamiento4, new int[]{0,1,2}, new int[]{20,20,330});
            Funciones.setOcultarColumnas(jtbTratamiento4,new int[]{3,5,6});
            jtbTratamiento4.setDefaultRenderer(Object.class, new tools.IconCellRenderer());
            jtbTratamiento4.setTableHeader(null);
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null, "10052:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
         }
     }
         
     public void CrearHistoriaC(){
         factory = Persistence.createEntityManagerFactory("ClipaEJBPU",AtencionUrgencia.props);
        saveHistoryClinic();
         saveFisicExplorer();
         saveHelpDiag();
         infohistoriac = infohistoriaJPA.findInfoHistoriac(this.infohistoriac.getId());
         if(pImagenologia!=null){
             pImagenologia.saveChanges(factory, infohistoriac);
         }
         if(pLaboratorio!=null){
             pLaboratorio.saveChanges(factory, infohistoriac);
         }
         if(pProcedimientos!=null){
             pProcedimientos.saveChanges(factory, infohistoriac);
         }
         if(pConsultDiag!=null){
             pConsultDiag.saveChanges(factory, infohistoriac);
         }
         if(pQuirurgico!=null){
             pQuirurgico.saveChanges(factory, infohistoriac);
         }
         if(pInterconsulta0!=null){
             pInterconsulta0.saveChanges(infohistoriac);
         }
         if(pInterconsulta1!=null){
             pInterconsulta1.saveChanges(infohistoriac);
         }
         if(pInterconsulta2!=null){
             pInterconsulta2.saveChanges(infohistoriac);
         }
         if(pInterconsulta3!=null){
             pInterconsulta3.saveChanges(infohistoriac);
         }
         if(pInterconsulta4!=null){
             pInterconsulta4.saveChanges(infohistoriac);
         }
         if(pOtrasInterconsultas!=null){
             pOtrasInterconsultas.saveChanges(infohistoriac);
         }
         if(pMedidaGeneral!=null){
             pMedidaGeneral.saveChanges(factory, infohistoriac);
         }
         if(pAnexo31!=null){
             pAnexo31.saveChanges(infohistoriac);
         }
         if(pMedic!=null){
             pMedic.saveChanges(factory, infohistoriac);
         }
       }
     
     public void saveHistoryClinic(){
             if(infohistoriac==null){
                 infohistoriac = new InfoHistoriac();
             }
             infohistoriac.setIdInfoAdmision(infoadmision);
            infohistoriac.setCausaExterna(jComboBox1.getSelectedItem().toString());
             infohistoriac.setMotivoConsulta(jTextArea10.getText().toUpperCase());
             infohistoriac.setNivelTriaje(getSelectionNivelTriage());
             infohistoriac.setAlergias(jTextArea11.getText().toUpperCase());
             infohistoriac.setIngresosPrevios(jTextArea12.getText().toUpperCase());
             infohistoriac.setTraumatismos(jTextArea22.getText().toUpperCase());
             infohistoriac.setTratamientos(jTextArea23.getText().toUpperCase());
             infohistoriac.setSituacionBasal(jTextArea24.getText().toUpperCase());
             infohistoriac.setHta(jCheckBox1.isSelected());
             infohistoriac.setDm(jCheckBox2.isSelected());
             infohistoriac.setDislipidemia(jCheckBox3.isSelected());
             infohistoriac.setTabaco(jCheckBox7.isSelected());
             infohistoriac.setAlcohol(jCheckBox8.isSelected());
             infohistoriac.setDroga(jCheckBox9.isSelected());
             infohistoriac.setOtrosHabitos(jTextArea7.getText().toUpperCase());
             infohistoriac.setDescHdd(jTextArea5.getText().toUpperCase());
             infohistoriac.setAntFamiliar(jTextArea25.getText().toUpperCase());
             infohistoriac.setEnfermedadActual(jTextArea13.getText().toUpperCase());
             infohistoriac.setDiagnostico(idDiag1);
             infohistoriac.setDiagnostico2(idDiag2);
             infohistoriac.setDiagnostico3(idDiag3);
             infohistoriac.setDiagnostico4(idDiag4);
             infohistoriac.setDiagnostico5(idDiag5);
             infohistoriac.setHallazgo(jTextArea19.getText().toUpperCase());
             infohistoriac.setTipoHc(0);//0 = urgencias; 
             infohistoriac.setEstado(finalizar);
             infohistoriac.setIdConfigdecripcionlogin(AtencionUrgencia.configdecripcionlogin);
             //Falta tiempo de consulta que se generara cuando finalize la consulta
             //calculando desde fecha_dato
             Boolean active = false;
             if(infohistoriaJPA==null){
                 infohistoriaJPA = new InfoHistoriacJpaController(factory);
                 if(!edite){
                       infohistoriaJPA.create(this.infohistoriac);
                 }else{
                     active = true;
                 }
             }else{
                 active = true;
             }
             if(active){
                 try {
                     infohistoriaJPA.edit(this.infohistoriac);
                 } catch (IllegalOrphanException ex) {
                     JOptionPane.showMessageDialog(null, "10053:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                 } catch (NonexistentEntityException ex) {
                     JOptionPane.showMessageDialog(null, "10054:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(null, "10055:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         }
         
         public void DatosAntPersonales(){
             if(antPersonalesJPA==null){
                antPersonalesJPA=new InfoAntPersonalesJpaController(factory);
            }
            antPersonales=antPersonalesJPA.findInfoAntPersonalesIDPac(infopaciente);
            if(antPersonales==null){
                 antPersonales=new InfoAntPersonales();
                 antPersonales.setIdPaciente(infopaciente.getId());
                 antPersonales.setAlcohol(false);
                 antPersonales.setAlergias("NINGUNO");
                 antPersonales.setAntFamiliares("NINGUNO");
                 antPersonales.setDescHdd("");
                 antPersonales.setDislipidemia(false);
                 antPersonales.setDm(false);
                 antPersonales.setDroga(false);
                 antPersonales.setHta(false);
                 antPersonales.setIngresosPrevios("NINGUNO");
                 antPersonales.setOtrosHabitos("NINGUNO");
                 antPersonales.setSituacionBasal("NINGUNO");
                 antPersonales.setTabaco(false);
                 antPersonales.setTratamientos("NINGUNO");
                 antPersonales.setTraumatismos("NINGUNO");
                 antPersonalesJPA.create(antPersonales);
             }else{
                 jTextArea11.setText(antPersonales.getAlergias());
                 jTextArea11.setCaretPosition(0);
                 jTextArea12.setText(antPersonales.getIngresosPrevios());
                 jTextArea12.setCaretPosition(0);
                 jTextArea22.setText(antPersonales.getTraumatismos());
                 jTextArea22.setCaretPosition(0);
                 jTextArea23.setText(antPersonales.getTratamientos());
                 jTextArea23.setCaretPosition(0);
                 jTextArea24.setText(antPersonales.getSituacionBasal());
                 jTextArea24.setCaretPosition(0);
                 jCheckBox1.setSelected(antPersonales.getHta());
                 jCheckBox2.setSelected(antPersonales.getDm());
                 jCheckBox3.setSelected(antPersonales.getDislipidemia());
                 jCheckBox7.setSelected(antPersonales.getTabaco());
                 jCheckBox8.setSelected(antPersonales.getAlcohol());
                 jCheckBox9.setSelected(antPersonales.getDroga());
                 jTextArea7.setText(antPersonales.getOtrosHabitos());
                 jTextArea7.setCaretPosition(0);
                 jTextArea5.setText(antPersonales.getDescHdd());
                 jTextArea5.setCaretPosition(0);
                 jTextArea25.setText(antPersonales.getAntFamiliares());
                 jTextArea25.setCaretPosition(0);
                 //muestra de la cajas de texto
            }
         }
         
         private void SaveAntPersonales(){
             antPersonales=antPersonalesJPA.findInfoAntPersonalesIDPac(infopaciente);
             antPersonales.setAlcohol(jCheckBox8.isSelected());
             antPersonales.setAlergias(jTextArea11.getText().toUpperCase());
             antPersonales.setAntFamiliares(jTextArea25.getText().toUpperCase());
             antPersonales.setDescHdd(jTextArea5.getText().toUpperCase());
             antPersonales.setDislipidemia(jCheckBox3.isSelected());
             antPersonales.setDm(jCheckBox2.isSelected());
             antPersonales.setDroga(jCheckBox9.isSelected());
             antPersonales.setHta(jCheckBox1.isSelected());
             antPersonales.setIngresosPrevios(jTextArea12.getText().toUpperCase());
             antPersonales.setOtrosHabitos(jTextArea7.getText().toUpperCase());
             antPersonales.setSituacionBasal(jTextArea24.getText().toUpperCase());
             antPersonales.setTabaco(jCheckBox7.isSelected());
             antPersonales.setTratamientos(jTextArea23.getText().toUpperCase());
             antPersonales.setTraumatismos(jTextArea22.getText().toUpperCase());
             try {            
                 antPersonalesJPA.edit(antPersonales);
             } catch (NonexistentEntityException ex) {
                 JOptionPane.showMessageDialog(null, "10063:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(null, "10064:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
             } 
         }
        
        public void viewClinicHistory(InfoHistoriac infoHistoriac){
            this.infohistoriac = infoHistoriac;
            edite = true;
            setHistoryC();
            setFisicExplorer();
            setHelpDiag();  
        }
        
        private void setHistoryC(){
            infoadmision = infohistoriac.getIdInfoAdmision();
            infopaciente = infoadmision.getIdDatosPersonales();
            jlbNombrePaciente.setText(infopaciente.getNombre1()+" "+infopaciente.getApellido1()+" ["+infopaciente.getNumDoc()+"]");
            jTextArea10.setText(infohistoriac.getMotivoConsulta());
            jTextArea10.setCaretPosition(0);
            jComboBox1.setSelectedItem(infohistoriac.getCausaExterna());
            this.setSelectionNivelTriage(infohistoriac.getNivelTriaje());
            jTextArea11.setText(infohistoriac.getAlergias());
            jTextArea11.setCaretPosition(0);
            jTextArea12.setText(infohistoriac.getIngresosPrevios());
            jTextArea12.setCaretPosition(0);
            jTextArea22.setText(infohistoriac.getTraumatismos());
            jTextArea22.setCaretPosition(0);
            jTextArea23.setText(infohistoriac.getTratamientos());
            jTextArea23.setCaretPosition(0);
            jTextArea24.setText(infohistoriac.getSituacionBasal());
            jTextArea24.setCaretPosition(0);
            jCheckBox1.setSelected(infohistoriac.getHta());
            jCheckBox2.setSelected(infohistoriac.getDm());
            jCheckBox3.setSelected(infohistoriac.getDislipidemia());
            jCheckBox7.setSelected(infohistoriac.getTabaco());
            jCheckBox8.setSelected(infohistoriac.getAlcohol());
            jCheckBox9.setSelected(infohistoriac.getDroga());
            jTextArea7.setText(infohistoriac.getOtrosHabitos());
            jTextArea7.setCaretPosition(0);
            jTextArea5.setText(infohistoriac.getDescHdd());
            jTextArea5.setCaretPosition(0);
            jTextArea13.setText(infohistoriac.getEnfermedadActual());
            jTextArea13.setCaretPosition(0);
            jTextArea25.setText(infohistoriac.getAntFamiliar());
            jTextArea25.setCaretPosition(0);
            if(staticcie == null){
                factory = Persistence.createEntityManagerFactory("ClipaEJBPU",AtencionUrgencia.props);
                staticcie = new StaticCie10JpaController(factory);
            }
            if (infohistoriac.getDiagnostico()!= 0 && infohistoriac.getDiagnostico()!= 1) {
                staticCie10 = staticcie.findStaticCie10(infohistoriac.getDiagnostico());
                jTextField11.setText("["+staticCie10.getCodigo()+"] "+staticCie10.getDescripcion());
                jTextField11.setCaretPosition(0);
                jTextArea12.setToolTipText(myStringsFunctions.stringToDIVstring(jTextArea12.getText()));
                idDiag1 = infohistoriac.getDiagnostico();
            }
            if (infohistoriac.getDiagnostico2()!=0 && infohistoriac.getDiagnostico2()!= 1) {
                staticCie10 = staticcie.findStaticCie10(infohistoriac.getDiagnostico2());
                jTextField12.setText("["+staticCie10.getCodigo()+"] "+staticCie10.getDescripcion());
                jTextField12.setCaretPosition(0);
                jTextArea12.setToolTipText(myStringsFunctions.stringToDIVstring(jTextArea12.getText()));
                idDiag2 = infohistoriac.getDiagnostico2();
            }
            if (infohistoriac.getDiagnostico3()!=0 && infohistoriac.getDiagnostico3()!= 1) {
                staticCie10 = staticcie.findStaticCie10(infohistoriac.getDiagnostico3());
                jTextField13.setText("["+staticCie10.getCodigo()+"] "+staticCie10.getDescripcion());
                jTextField13.setCaretPosition(0);
                jTextArea13.setToolTipText(myStringsFunctions.stringToDIVstring(jTextArea13.getText()));
                idDiag3 = infohistoriac.getDiagnostico3();
            }
            if (infohistoriac.getDiagnostico4()!=0 && infohistoriac.getDiagnostico4()!= 1) {
                staticCie10 = staticcie.findStaticCie10(infohistoriac.getDiagnostico4());
                jTextField14.setText("["+staticCie10.getCodigo()+"] "+staticCie10.getDescripcion());
                jTextField14.setCaretPosition(0);
                jTextArea14.setToolTipText(myStringsFunctions.stringToDIVstring(jTextArea14.getText()));
                idDiag4 = infohistoriac.getDiagnostico4();
            }
            if (infohistoriac.getDiagnostico5()!=0 && infohistoriac.getDiagnostico5()!= 1) {
                staticCie10 = staticcie.findStaticCie10(infohistoriac.getDiagnostico5());
                jTextField15.setText("["+staticCie10.getCodigo()+"] "+staticCie10.getDescripcion());
                jTextField15.setCaretPosition(0);
                jTextArea15.setToolTipText(myStringsFunctions.stringToDIVstring(jTextArea15.getText()));
                idDiag5 = infohistoriac.getDiagnostico5();
            }
 
            jTextArea19.setText(infohistoriac.getHallazgo());
        }
        
         private void setFisicExplorer(){
             infoexploracion = infohistoriac.getInfoHcExpfisica();
             jTextField1.setText(infoexploracion.getTa());
             jTextField1.setCaretPosition(0);
             jTextField8.setText(infoexploracion.getT());
             jTextField8.setCaretPosition(0);
             jTextField2.setText(infoexploracion.getTam());
             jTextField2.setCaretPosition(0);
             jTextField7.setText(infoexploracion.getSao2());
             jTextField7.setCaretPosition(0);
             jTextField3.setText(infoexploracion.getFc());
             jTextField3.setCaretPosition(0);
             jTextField6.setText(infoexploracion.getPvc());
             jTextField6.setCaretPosition(0);
             jTextField4.setText(infoexploracion.getFr());
             jTextField4.setCaretPosition(0);
             jTextField5.setText(infoexploracion.getPic());
             jTextField5.setCaretPosition(0);
             jTextField9.setText(infoexploracion.getPeso());
             jTextField9.setCaretPosition(0);
             jTextField10.setText(infoexploracion.getTalla());
             jTextField10.setCaretPosition(0);
             jTextArea2.setText(infoexploracion.getOtros());
             jTextArea2.setCaretPosition(0);
             jTextArea3.setText(infoexploracion.getAspectogeneral());
             jTextArea3.setCaretPosition(0);
             jTextArea4.setText(infoexploracion.getCara());
             jTextArea4.setCaretPosition(0);
             jTextArea6.setText(infoexploracion.getCardio());
             jTextArea6.setCaretPosition(0);
             jTextArea8.setText(infoexploracion.getRespiratorio());
             jTextArea8.setCaretPosition(0);
             jTextArea9.setText(infoexploracion.getGastro());
             jTextArea9.setCaretPosition(0);
             jTextArea14.setText(infoexploracion.getRenal());
             jTextArea14.setCaretPosition(0);
             jTextArea15.setText(infoexploracion.getHemato());
             jTextArea15.setCaretPosition(0);
             jTextArea16.setText(infoexploracion.getEndo());
             jTextArea16.setCaretPosition(0);
             jTextArea17.setText(infoexploracion.getOsteo());
             jTextArea17.setCaretPosition(0);
         }
         
         private void saveFisicExplorer(){
             if(infoexploracion == null){
                 infoexploracion = new InfoHcExpfisica();
             }
             infoexploracion.setIdInfohistoriac(infohistoriac);
             infoexploracion.setTa(jTextField1.getText().toUpperCase());
             infoexploracion.setT(jTextField8.getText().toUpperCase());
             infoexploracion.setTam(jTextField2.getText().toUpperCase());
             infoexploracion.setSao2(jTextField7.getText().toUpperCase());
             infoexploracion.setFc(jTextField3.getText().toUpperCase());
             infoexploracion.setPvc(jTextField6.getText().toUpperCase());
             infoexploracion.setFr(jTextField4.getText().toUpperCase());
             infoexploracion.setPic(jTextField5.getText().toUpperCase());
             infoexploracion.setPeso(jTextField9.getText().toUpperCase());
             infoexploracion.setTalla(jTextField10.getText().toUpperCase());
             infoexploracion.setOtros(jTextArea2.getText().toUpperCase());
             infoexploracion.setAspectogeneral(jTextArea3.getText().toUpperCase());
             infoexploracion.setCara(jTextArea4.getText().toUpperCase());
             infoexploracion.setCardio(jTextArea6.getText().toUpperCase());
             infoexploracion.setRespiratorio(jTextArea8.getText().toUpperCase());
             infoexploracion.setGastro(jTextArea9.getText().toUpperCase());
             infoexploracion.setRenal(jTextArea14.getText().toUpperCase());
             infoexploracion.setHemato(jTextArea15.getText().toUpperCase());
             infoexploracion.setEndo(jTextArea16.getText().toUpperCase());
             infoexploracion.setOsteo(jTextArea17.getText().toUpperCase());
             Boolean active = false;
             if(infohsfisicoJPA==null){
                 infohsfisicoJPA = new InfoHcExpfisicaJpaController(factory);
                 if(!edite){
                         infohsfisicoJPA.create(infoexploracion);
                 }else{
                     active = true;
                 }
             }else{
                 active= true;
             }
             if(active){
                try {
                     infohsfisicoJPA.edit(infoexploracion);
                 } catch (NonexistentEntityException ex) {
                     JOptionPane.showMessageDialog(null, "10056:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(null, "10057:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         }
         
         private void setHelpDiag() {
             ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("images/Delete16x16.png"));
             ImageIcon icon2 = new ImageIcon(ClassLoader.getSystemResource("images/download.png"));
             File file = null;
             try {
                 file = File.createTempFile("temporal", null);
             } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "10065:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
             } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "10066:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
             }
             listaPruebas = infohistoriac.getInfoPruebasComplements();
             for(int i=0;i<listaPruebas.size();i++){
                 modeloAyudDiag.addRow(dato);
                 modeloAyudDiag.setValueAt(new JLabel(icon), i, 0);
                 modeloAyudDiag.setValueAt(new JLabel(icon2), i, 1);
                 modeloAyudDiag.setValueAt(listaPruebas.get(i).getNombre(), i, 2);
                 modeloAyudDiag.setValueAt(file, i, 3);
                 modeloAyudDiag.setValueAt(listaPruebas.get(i).getTipo(), i, 4);
                 modeloAyudDiag.setValueAt(ReturnPathdiagHelpSaveFile(listaPruebas.get(i).getTipo()), i, 5);
                 modeloAyudDiag.setValueAt("1", i, 6);
             }
         //descargar archivos
         }
         
 
         
         private void saveHelpDiag(){
             if(infoPruebasComplementJPA == null){//verifico si no existe la instancia de la persistencia para crearla
                 infoPruebasComplementJPA = new InfoPruebasComplementJpaController(factory);
             }
             if(modDestroyAyudDiag.getRowCount()>0){
              Boolean existe=false;
              int id = 0;
                 for(int i=0;i<modDestroyAyudDiag.getRowCount();i++){
                     if(((String)modDestroyAyudDiag.getValueAt(i, 6)).equals("1")){//comprobamos si es registro antiguo
                         for(InfoPruebasComplement infopruebas:infohistoriac.getInfoPruebasComplements()){//recorremos los registros en la hc
                             if((((String)modDestroyAyudDiag.getValueAt(i, 5)
                                     + System.getProperty("file.separator")
                                     +(String)modDestroyAyudDiag.getValueAt(i, 2))).equals(infopruebas.getRuta())){//verificamos coincidencias
                                 existe = true;
                                 id = infopruebas.getId();
                                 break;
                             }else{
                                 existe = false;
                             }
                         }
                         if(existe){
                             try {
                                 infoPruebasComplementJPA.destroy(id);
                                 Funciones.fileDelete((String)modDestroyAyudDiag.getValueAt(i, 5)
                                     + System.getProperty("file.separator")+(String)modDestroyAyudDiag.getValueAt(i, 2)
                                     ,(String)modDestroyAyudDiag.getValueAt(i, 5)
                                     , (String)modDestroyAyudDiag.getValueAt(i, 2));
                             } catch (NonexistentEntityException ex) {
                                 JOptionPane.showMessageDialog(null, "10058:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                             } catch (Exception ex) {
                                 JOptionPane.showMessageDialog(null, "10059:\n"+ex.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
                             }
                         }
                     }
                 }
             }else{
                 //no hay archivos por eliminar
             }
             for(int i =0;i<modDestroyAyudDiag.getRowCount();i++){
                 modDestroyAyudDiag.removeRow(i);
             }
             if(modeloAyudDiag.getRowCount()>0){//verificamos si existen archivos en el modelo de muestra
                 for(int i=0;i<modeloAyudDiag.getRowCount();i++){//recorremos el modelo de muestra
                     if(((String)modeloAyudDiag.getValueAt(i, 6)).equals("0")){//comprobamos si esta recien añadido
                         if(infoPruebasComplement == null){
                             infoPruebasComplement = new InfoPruebasComplement();
                         }
                         infoPruebasComplement.setIdInfohistoriac(infohistoriac);
                         infoPruebasComplement.setNombre((String)modeloAyudDiag.getValueAt(i, 2));
                         infoPruebasComplement.setTipo((String)modeloAyudDiag.getValueAt(i, 4));
                         infoPruebasComplement.setRuta((String)modeloAyudDiag.getValueAt(i, 5)
                                 + System.getProperty("file.separator")+(String)modeloAyudDiag.getValueAt(i, 2));
                         infoPruebasComplementJPA.create(infoPruebasComplement);
                         Funciones.fileUpload(((File)modeloAyudDiag.getValueAt(i, 3)).getAbsolutePath()
                                 ,(String)modeloAyudDiag.getValueAt(i, 5)
                                 , (String)modeloAyudDiag.getValueAt(i, 2));
                         modeloAyudDiag.setValueAt("1",i, 6);
                         infoPruebasComplement = null;
                     }else{
                         //como existe el registro no hacemos nada
                     }
                 }
             }
         }
         
         public void cerrarPanel(){
             Funciones.setLabelInfo("GUARDANDO...");
             CrearHistoriaC();
             SaveAntPersonales();
             atencionurgencia.AtencionUrgencia.panelindex.jpContainer.removeAll(); 
             atencionurgencia.AtencionUrgencia.panelindex.jpContainer.validate();
             atencionurgencia.AtencionUrgencia.panelindex.jpContainer.repaint();
             Funciones.setLabelInfo("DATOS GUARDADOS");
         }
        
         /**
          * @param tipo
          * @return "numero documento/año/mes/id admision/tipo de archivo"
          */
         private String ReturnPathdiagHelpSaveFile(String tipo){
             Calendar ahoraCal = Calendar.getInstance();
             ahoraCal.setTime(infoadmision.getFechaIngreso());
             String year =String.valueOf(ahoraCal.get(Calendar.YEAR));
             String month =String.valueOf(ahoraCal.get(Calendar.MONTH));
             return infoadmision.getIdDatosPersonales().getNumDoc()+s+year+s+month+s+infoadmision.getId()+s+tipo;
         }
         
         private Boolean getValidaFirma(){
             boolean f = false;
             try {
                 String sFile = this.infohistoriac.getIdConfigdecripcionlogin().getRuta_firma();
                 File sFile1 = null;
                 if(sFile==null){
                     JOptionPane.showMessageDialog(this, "Errores leyendo archivo de firma del usuario.");
                 }else{
                     sFile1 = new File(sFile);
                     if(sFile1.exists()){
                         f=true;
                     }else{
                         JOptionPane.showMessageDialog(this, "Errores leyendo archivo de firma del usuario.");
                     }  
                 }                
                               
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(null, "10116:\n"+e.getMessage(), HC.class.getName(), JOptionPane.INFORMATION_MESSAGE);
             }
             return f;
         }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jpMotivoC = new javax.swing.JPanel();
         jPanel7 = new javax.swing.JPanel();
         jPanel26 = new javax.swing.JPanel();
         jRadioButton4 = new javax.swing.JRadioButton();
         jRadioButton3 = new javax.swing.JRadioButton();
         jRadioButton2 = new javax.swing.JRadioButton();
         jRadioButton1 = new javax.swing.JRadioButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextArea10 = new javax.swing.JTextArea();
         jLabel37 = new javax.swing.JLabel();
         jPanel5 = new javax.swing.JPanel();
         jComboBox1 = new javax.swing.JComboBox();
         buttonGroup1 = new javax.swing.ButtonGroup();
         jpAntPersonales = new javax.swing.JPanel();
         jPanel3 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         jCheckBox1 = new javax.swing.JCheckBox();
         jCheckBox2 = new javax.swing.JCheckBox();
         jCheckBox3 = new javax.swing.JCheckBox();
         jScrollPane7 = new javax.swing.JScrollPane();
         jTextArea5 = new javax.swing.JTextArea();
         jLabel39 = new javax.swing.JLabel();
         jPanel6 = new javax.swing.JPanel();
         jCheckBox7 = new javax.swing.JCheckBox();
         jCheckBox8 = new javax.swing.JCheckBox();
         jCheckBox9 = new javax.swing.JCheckBox();
         jScrollPane9 = new javax.swing.JScrollPane();
         jTextArea7 = new javax.swing.JTextArea();
         jLabel9 = new javax.swing.JLabel();
         jPanel8 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextArea11 = new javax.swing.JTextArea();
         jPanel9 = new javax.swing.JPanel();
         jScrollPane8 = new javax.swing.JScrollPane();
         jTextArea12 = new javax.swing.JTextArea();
         jPanel19 = new javax.swing.JPanel();
         jScrollPane21 = new javax.swing.JScrollPane();
         jTextArea22 = new javax.swing.JTextArea();
         jPanel20 = new javax.swing.JPanel();
         jScrollPane22 = new javax.swing.JScrollPane();
         jTextArea23 = new javax.swing.JTextArea();
         jPanel21 = new javax.swing.JPanel();
         jScrollPane23 = new javax.swing.JScrollPane();
         jTextArea24 = new javax.swing.JTextArea();
         jPanel31 = new javax.swing.JPanel();
         jScrollPane25 = new javax.swing.JScrollPane();
         jTextArea25 = new javax.swing.JTextArea();
         jLabel38 = new javax.swing.JLabel();
         jpEnfActual = new javax.swing.JPanel();
         jPanel10 = new javax.swing.JPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTextArea13 = new javax.swing.JTextArea();
         jLabel40 = new javax.swing.JLabel();
         jpExpFisica = new javax.swing.JPanel();
         jPanel11 = new javax.swing.JPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel12 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jTextField2 = new javax.swing.JTextField();
         jTextField3 = new javax.swing.JTextField();
         jTextField4 = new javax.swing.JTextField();
         jTextField5 = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jTextField6 = new javax.swing.JTextField();
         jTextField7 = new javax.swing.JTextField();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jTextField8 = new javax.swing.JTextField();
         jPanel25 = new javax.swing.JPanel();
         jScrollPane5 = new javax.swing.JScrollPane();
         jTextArea2 = new javax.swing.JTextArea();
         jLabel10 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jLabel13 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         jLabel16 = new javax.swing.JLabel();
         jLabel17 = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jTextField9 = new javax.swing.JTextField();
         jLabel19 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         jTextField10 = new javax.swing.JTextField();
         jLabel21 = new javax.swing.JLabel();
         jPanel14 = new javax.swing.JPanel();
         jScrollPane6 = new javax.swing.JScrollPane();
         jTextArea3 = new javax.swing.JTextArea();
         jPanel15 = new javax.swing.JPanel();
         jScrollPane10 = new javax.swing.JScrollPane();
         jTextArea4 = new javax.swing.JTextArea();
         jPanel16 = new javax.swing.JPanel();
         jScrollPane11 = new javax.swing.JScrollPane();
         jTextArea6 = new javax.swing.JTextArea();
         jPanel13 = new javax.swing.JPanel();
         jScrollPane12 = new javax.swing.JScrollPane();
         jTextArea8 = new javax.swing.JTextArea();
         jTabbedPane2 = new javax.swing.JTabbedPane();
         jPanel17 = new javax.swing.JPanel();
         jScrollPane13 = new javax.swing.JScrollPane();
         jTextArea9 = new javax.swing.JTextArea();
         jPanel18 = new javax.swing.JPanel();
         jScrollPane14 = new javax.swing.JScrollPane();
         jTextArea14 = new javax.swing.JTextArea();
         jPanel22 = new javax.swing.JPanel();
         jScrollPane15 = new javax.swing.JScrollPane();
         jTextArea15 = new javax.swing.JTextArea();
         jPanel23 = new javax.swing.JPanel();
         jScrollPane16 = new javax.swing.JScrollPane();
         jTextArea16 = new javax.swing.JTextArea();
         jPanel24 = new javax.swing.JPanel();
         jScrollPane17 = new javax.swing.JScrollPane();
         jTextArea17 = new javax.swing.JTextArea();
         jLabel41 = new javax.swing.JLabel();
         jpPruebasDiag = new javax.swing.JPanel();
         jPanel27 = new javax.swing.JPanel();
         jPanel32 = new javax.swing.JPanel();
         jScrollPane26 = new javax.swing.JScrollPane();
         jTextArea19 = new javax.swing.JTextArea();
         jScrollPane27 = new javax.swing.JScrollPane();
         jtbTratamiento4 = new javax.swing.JTable();
         jLabel34 = new javax.swing.JLabel();
         jTextField16 = new javax.swing.JTextField();
         jButton8 = new javax.swing.JButton();
         jLabel42 = new javax.swing.JLabel();
         jpDiagMedico = new javax.swing.JPanel();
         jPanel28 = new javax.swing.JPanel();
         jButton2 = new javax.swing.JButton();
         jLabel28 = new javax.swing.JLabel();
         jTextField11 = new javax.swing.JTextField();
         jTextField12 = new javax.swing.JTextField();
         jLabel29 = new javax.swing.JLabel();
         jTextField13 = new javax.swing.JTextField();
         jLabel30 = new javax.swing.JLabel();
         jButton4 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jLabel31 = new javax.swing.JLabel();
         jTextField14 = new javax.swing.JTextField();
         jButton6 = new javax.swing.JButton();
         jButton7 = new javax.swing.JButton();
         jTextField15 = new javax.swing.JTextField();
         jLabel32 = new javax.swing.JLabel();
         jLabel43 = new javax.swing.JLabel();
         jpTratamiento = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jLabel35 = new javax.swing.JLabel();
         jPanel29 = new javax.swing.JPanel();
         jPanel35 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         jXTaskPane1 = new org.jdesktop.swingx.JXTaskPane();
         jLabel46 = new javax.swing.JLabel();
         jXTaskPane2 = new org.jdesktop.swingx.JXTaskPane();
         jLabel36 = new javax.swing.JLabel();
         jLabel45 = new javax.swing.JLabel();
         jLabel49 = new javax.swing.JLabel();
         jLabel50 = new javax.swing.JLabel();
         jLabel51 = new javax.swing.JLabel();
         jXTaskPane3 = new org.jdesktop.swingx.JXTaskPane();
         jLabel48 = new javax.swing.JLabel();
         jLabel52 = new javax.swing.JLabel();
         jLabel53 = new javax.swing.JLabel();
         jLabel54 = new javax.swing.JLabel();
         jLabel55 = new javax.swing.JLabel();
         jLabel56 = new javax.swing.JLabel();
         jXTaskPane4 = new org.jdesktop.swingx.JXTaskPane();
         jLabel47 = new javax.swing.JLabel();
         jXTaskPane5 = new org.jdesktop.swingx.JXTaskPane();
         jLabel58 = new javax.swing.JLabel();
         jLabel59 = new javax.swing.JLabel();
         jFileChooser1 = new javax.swing.JFileChooser();
         jpCentro = new javax.swing.JPanel();
         jLabel23 = new javax.swing.JLabel();
         jLabel22 = new javax.swing.JLabel();
         jLabel24 = new javax.swing.JLabel();
         jLabel25 = new javax.swing.JLabel();
         jLabel26 = new javax.swing.JLabel();
         jLabel27 = new javax.swing.JLabel();
         jLabel33 = new javax.swing.JLabel();
         jlbNombrePaciente = new javax.swing.JLabel();
         jButton3 = new javax.swing.JButton();
         jCheckBox4 = new javax.swing.JCheckBox();
         jCheckBox5 = new javax.swing.JCheckBox();
         jCheckBox6 = new javax.swing.JCheckBox();
         jCheckBox10 = new javax.swing.JCheckBox();
         jCheckBox11 = new javax.swing.JCheckBox();
         jCheckBox12 = new javax.swing.JCheckBox();
         jCheckBox13 = new javax.swing.JCheckBox();
         jButton1 = new javax.swing.JButton();
         jButton9 = new javax.swing.JButton();
         jButton10 = new javax.swing.JButton();
         jButton11 = new javax.swing.JButton();
 
         jpMotivoC.setMaximumSize(new java.awt.Dimension(584, 472));
         jpMotivoC.setMinimumSize(new java.awt.Dimension(584, 472));
         jpMotivoC.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jPanel7.setBackground(new java.awt.Color(255, 255, 255));
 
         jPanel26.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nivel de Triaje", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 10))); // NOI18N
         jPanel26.setOpaque(false);
 
         buttonGroup1.add(jRadioButton4);
         jRadioButton4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jRadioButton4.setText("CUATRO");
         jRadioButton4.setDoubleBuffered(true);
         jRadioButton4.setHideActionText(true);
         jRadioButton4.setOpaque(false);
 
         buttonGroup1.add(jRadioButton3);
         jRadioButton3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jRadioButton3.setSelected(true);
         jRadioButton3.setText("TRES");
         jRadioButton3.setHideActionText(true);
         jRadioButton3.setOpaque(false);
 
         buttonGroup1.add(jRadioButton2);
         jRadioButton2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jRadioButton2.setText("DOS");
         jRadioButton2.setOpaque(false);
 
         buttonGroup1.add(jRadioButton1);
         jRadioButton1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jRadioButton1.setText("UNO");
         jRadioButton1.setOpaque(false);
 
         javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
         jPanel26.setLayout(jPanel26Layout);
         jPanel26Layout.setHorizontalGroup(
             jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel26Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jRadioButton1)
                 .addGap(28, 28, 28)
                 .addComponent(jRadioButton2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jRadioButton3)
                 .addGap(18, 18, 18)
                 .addComponent(jRadioButton4)
                 .addGap(23, 23, 23))
         );
         jPanel26Layout.setVerticalGroup(
             jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel26Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jRadioButton1)
                     .addComponent(jRadioButton2)
                     .addComponent(jRadioButton3)
                     .addComponent(jRadioButton4))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jScrollPane1.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane1.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane1.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea10.setColumns(20);
         jTextArea10.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea10.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea10.setLineWrap(true);
         jTextArea10.setRows(5);
         jTextArea10.setDragEnabled(true);
         jTextArea10.setMaximumSize(new java.awt.Dimension(164, 40));
         jTextArea10.setMinimumSize(new java.awt.Dimension(164, 40));
         jTextArea10.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea10KeyPressed(evt);
             }
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 jTextArea10KeyTyped(evt);
             }
         });
         jScrollPane1.setViewportView(jTextArea10);
 
         jLabel37.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel37.setText("MOTIVO DE CONSULTA");
 
         jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Origen de la Enfermedad o Accidente", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 10))); // NOI18N
         jPanel5.setOpaque(false);
 
         jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PACIENTE SANO", "ENFERMEDAD GENERAL O COMÚN", "ENFERMEDAD PROFESIONAL U OCUPACIONAL", "ACCIDENTE DE TRABAJO", "ACCIDENTE NO DE TRABAJO O FUERA DEL TRABAJO" }));
         jComboBox1.setSelectedIndex(-1);
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jComboBox1, 0, 294, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                     .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(jPanel7Layout.createSequentialGroup()
                         .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(117, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jpMotivoCLayout = new javax.swing.GroupLayout(jpMotivoC);
         jpMotivoC.setLayout(jpMotivoCLayout);
         jpMotivoCLayout.setHorizontalGroup(
             jpMotivoCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jpMotivoCLayout.setVerticalGroup(
             jpMotivoCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jpAntPersonales.setMaximumSize(new java.awt.Dimension(584, 472));
         jpAntPersonales.setMinimumSize(new java.awt.Dimension(584, 472));
         jpAntPersonales.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jPanel3.setBackground(new java.awt.Color(255, 255, 255));
 
         jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "HTA, DM, DISLIPIDEMIA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 10))); // NOI18N
         jPanel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jPanel4.setOpaque(false);
 
         jCheckBox1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jCheckBox1.setText("HTA");
         jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
         jCheckBox1.setOpaque(false);
 
         jCheckBox2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jCheckBox2.setText("DM");
         jCheckBox2.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
         jCheckBox2.setOpaque(false);
 
         jCheckBox3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jCheckBox3.setText("DISLIPIDEMIA");
         jCheckBox3.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
         jCheckBox3.setOpaque(false);
 
         jTextArea5.setColumns(20);
         jTextArea5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea5.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea5.setLineWrap(true);
         jTextArea5.setRows(2);
         jTextArea5.setDoubleBuffered(true);
         jTextArea5.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea5KeyPressed(evt);
             }
         });
         jScrollPane7.setViewportView(jTextArea5);
 
         jLabel39.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel39.setText("Observacion:");
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(jCheckBox1)
                 .addGap(18, 18, 18)
                 .addComponent(jCheckBox2)
                 .addGap(18, 18, 18)
                 .addComponent(jCheckBox3)
                 .addContainerGap(38, Short.MAX_VALUE))
             .addComponent(jScrollPane7)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(jLabel39)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jCheckBox1)
                     .addComponent(jCheckBox2)
                     .addComponent(jCheckBox3))
                 .addGap(4, 4, 4)
                 .addComponent(jLabel39)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
         );
 
         jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Hábitos Tóxicos"));
         jPanel6.setFocusable(false);
         jPanel6.setOpaque(false);
 
         jCheckBox7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jCheckBox7.setText("Tabaco");
         jCheckBox7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         jCheckBox7.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
         jCheckBox7.setOpaque(false);
 
         jCheckBox8.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jCheckBox8.setText("Alcohol");
         jCheckBox8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         jCheckBox8.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
         jCheckBox8.setOpaque(false);
 
         jCheckBox9.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jCheckBox9.setText("Drogas de Abuso");
         jCheckBox9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         jCheckBox9.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
         jCheckBox9.setOpaque(false);
 
         jTextArea7.setColumns(20);
         jTextArea7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea7.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea7.setLineWrap(true);
         jTextArea7.setRows(1);
         jTextArea7.setDoubleBuffered(true);
         jTextArea7.setMaximumSize(new java.awt.Dimension(104, 30));
         jTextArea7.setMinimumSize(new java.awt.Dimension(104, 30));
         jTextArea7.setPreferredSize(new java.awt.Dimension(164, 30));
         jTextArea7.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea7KeyPressed(evt);
             }
         });
         jScrollPane9.setViewportView(jTextArea7);
 
         jLabel9.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel9.setText("Otros:");
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane9)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel9)
                     .addGroup(jPanel6Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jCheckBox7)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jCheckBox8)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jCheckBox9)))
                 .addContainerGap(46, Short.MAX_VALUE))
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jCheckBox7)
                     .addComponent(jCheckBox8)
                     .addComponent(jCheckBox9))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel9)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Alergias a Medicamentos"));
         jPanel8.setOpaque(false);
 
         jScrollPane2.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane2.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane2.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea11.setColumns(20);
         jTextArea11.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea11.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea11.setLineWrap(true);
         jTextArea11.setRows(2);
         jTextArea11.setText("NINGUNO");
         jTextArea11.setMaximumSize(new java.awt.Dimension(164, 40));
         jTextArea11.setMinimumSize(new java.awt.Dimension(164, 40));
         jTextArea11.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea11FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea11FocusLost(evt);
             }
         });
         jTextArea11.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea11KeyPressed(evt);
             }
         });
         jScrollPane2.setViewportView(jTextArea11);
 
         javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                 .addGap(0, 0, 0)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Ingresos previos y cirugías"));
         jPanel9.setOpaque(false);
 
         jScrollPane8.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane8.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane8.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea12.setColumns(20);
         jTextArea12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea12.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea12.setLineWrap(true);
         jTextArea12.setRows(2);
         jTextArea12.setText("NINGUNO");
         jTextArea12.setMaximumSize(new java.awt.Dimension(164, 40));
         jTextArea12.setMinimumSize(new java.awt.Dimension(164, 40));
         jTextArea12.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea12FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea12FocusLost(evt);
             }
         });
         jTextArea12.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea12KeyPressed(evt);
             }
         });
         jScrollPane8.setViewportView(jTextArea12);
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
         );
 
         jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Traumatismos, accidentes"));
         jPanel19.setOpaque(false);
 
         jScrollPane21.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane21.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane21.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea22.setColumns(20);
         jTextArea22.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea22.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea22.setLineWrap(true);
         jTextArea22.setRows(2);
         jTextArea22.setText("NINGUNO");
         jTextArea22.setMaximumSize(new java.awt.Dimension(164, 40));
         jTextArea22.setMinimumSize(new java.awt.Dimension(164, 40));
         jTextArea22.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea22FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea22FocusLost(evt);
             }
         });
         jTextArea22.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea22KeyPressed(evt);
             }
         });
         jScrollPane21.setViewportView(jTextArea22);
 
         javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
         jPanel19.setLayout(jPanel19Layout);
         jPanel19Layout.setHorizontalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel19Layout.setVerticalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
         );
 
         jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder("Tratamientos habituales"));
         jPanel20.setOpaque(false);
 
         jScrollPane22.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane22.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane22.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea23.setColumns(20);
         jTextArea23.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea23.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea23.setLineWrap(true);
         jTextArea23.setRows(2);
         jTextArea23.setText("NINGUNO");
         jTextArea23.setMaximumSize(new java.awt.Dimension(164, 40));
         jTextArea23.setMinimumSize(new java.awt.Dimension(164, 40));
         jTextArea23.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea23FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea23FocusLost(evt);
             }
         });
         jTextArea23.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea23KeyPressed(evt);
             }
         });
         jScrollPane22.setViewportView(jTextArea23);
 
         javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
         jPanel20.setLayout(jPanel20Layout);
         jPanel20Layout.setHorizontalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel20Layout.setVerticalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane22, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder("Situación basal (crónicos)"));
         jPanel21.setOpaque(false);
 
         jScrollPane23.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane23.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane23.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea24.setColumns(20);
         jTextArea24.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea24.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea24.setLineWrap(true);
         jTextArea24.setRows(2);
         jTextArea24.setText("NINGUNO");
         jTextArea24.setMaximumSize(new java.awt.Dimension(164, 40));
         jTextArea24.setMinimumSize(new java.awt.Dimension(164, 40));
         jTextArea24.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea24FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea24FocusLost(evt);
             }
         });
         jTextArea24.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea24KeyPressed(evt);
             }
         });
         jScrollPane23.setViewportView(jTextArea24);
 
         javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
         jPanel21.setLayout(jPanel21Layout);
         jPanel21Layout.setHorizontalGroup(
             jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel21Layout.setVerticalGroup(
             jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane23, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
         );
 
         jPanel31.setBorder(javax.swing.BorderFactory.createTitledBorder("Antecedentes Familiares de Interes"));
         jPanel31.setOpaque(false);
 
         jScrollPane25.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane25.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane25.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea25.setColumns(20);
         jTextArea25.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea25.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea25.setLineWrap(true);
         jTextArea25.setRows(2);
         jTextArea25.setText("NINGUNO");
         jTextArea25.setMaximumSize(new java.awt.Dimension(164, 40));
         jTextArea25.setMinimumSize(new java.awt.Dimension(164, 40));
         jTextArea25.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea25FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea25FocusLost(evt);
             }
         });
         jTextArea25.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea25KeyPressed(evt);
             }
         });
         jScrollPane25.setViewportView(jTextArea25);
 
         javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
         jPanel31.setLayout(jPanel31Layout);
         jPanel31Layout.setHorizontalGroup(
             jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel31Layout.setVerticalGroup(
             jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane25, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
         );
 
         jLabel38.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel38.setText("ANTECEDENTES PERSONALES");
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addComponent(jLabel38, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jPanel31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout jpAntPersonalesLayout = new javax.swing.GroupLayout(jpAntPersonales);
         jpAntPersonales.setLayout(jpAntPersonalesLayout);
         jpAntPersonalesLayout.setHorizontalGroup(
             jpAntPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jpAntPersonalesLayout.setVerticalGroup(
             jpAntPersonalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jpEnfActual.setMaximumSize(new java.awt.Dimension(584, 472));
         jpEnfActual.setMinimumSize(new java.awt.Dimension(584, 472));
         jpEnfActual.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jPanel10.setBackground(new java.awt.Color(255, 255, 255));
 
         jScrollPane4.setMaximumSize(new java.awt.Dimension(164, 20));
         jScrollPane4.setMinimumSize(new java.awt.Dimension(164, 20));
         jScrollPane4.setPreferredSize(new java.awt.Dimension(164, 20));
 
         jTextArea13.setColumns(20);
         jTextArea13.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea13.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea13.setLineWrap(true);
         jTextArea13.setRows(5);
         jTextArea13.setMaximumSize(new java.awt.Dimension(164, 60));
         jTextArea13.setMinimumSize(new java.awt.Dimension(164, 60));
         jScrollPane4.setViewportView(jTextArea13);
 
         jLabel40.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel40.setText("ENFERMEDAD ACTUAL");
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                     .addComponent(jLabel40, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                 .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(156, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jpEnfActualLayout = new javax.swing.GroupLayout(jpEnfActual);
         jpEnfActual.setLayout(jpEnfActualLayout);
         jpEnfActualLayout.setHorizontalGroup(
             jpEnfActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jpEnfActualLayout.setVerticalGroup(
             jpEnfActualLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jpExpFisica.setMaximumSize(new java.awt.Dimension(584, 472));
         jpExpFisica.setMinimumSize(new java.awt.Dimension(584, 472));
         jpExpFisica.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jPanel11.setBackground(new java.awt.Color(255, 255, 255));
 
         jTabbedPane1.setFocusable(false);
         jTabbedPane1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
 
         jPanel12.setOpaque(false);
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel1.setText("TA");
 
         jTextField1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField1.setForeground(new java.awt.Color(0, 102, 255));
 
         jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel2.setText("TAM");
 
         jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel3.setText("FC");
 
         jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel4.setText("FR");
 
         jTextField2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField2.setForeground(new java.awt.Color(0, 102, 255));
 
         jTextField3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField3.setForeground(new java.awt.Color(0, 102, 255));
 
         jTextField4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField4.setForeground(new java.awt.Color(0, 102, 255));
 
         jTextField5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField5.setForeground(new java.awt.Color(0, 102, 255));
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel5.setText("PIC");
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel6.setText("PVC");
 
         jTextField6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField6.setForeground(new java.awt.Color(0, 102, 255));
 
         jTextField7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField7.setForeground(new java.awt.Color(0, 102, 255));
 
         jLabel7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel7.setText("SaO2");
 
         jLabel8.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel8.setText("T");
 
         jTextField8.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField8.setForeground(new java.awt.Color(0, 102, 255));
 
         jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "OTROS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 10))); // NOI18N
         jPanel25.setOpaque(false);
 
         jTextArea2.setColumns(20);
         jTextArea2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea2.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea2.setLineWrap(true);
         jTextArea2.setRows(3);
         jTextArea2.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea2KeyPressed(evt);
             }
         });
         jScrollPane5.setViewportView(jTextArea2);
 
         javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
         jPanel25.setLayout(jPanel25Layout);
         jPanel25Layout.setHorizontalGroup(
             jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
         );
         jPanel25Layout.setVerticalGroup(
             jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane5)
         );
 
         jLabel10.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel10.setText("mmHg");
 
         jLabel11.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel11.setText("mmHg");
 
         jLabel12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel12.setText("x'");
 
         jLabel13.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel13.setText("x'");
 
         jLabel14.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel14.setText("°C");
 
         jLabel15.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel15.setText("%");
 
         jLabel16.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel16.setText("Cm");
 
         jLabel17.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel17.setText("mmHg");
 
         jLabel18.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel18.setText("PESO");
 
         jTextField9.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField9.setForeground(new java.awt.Color(0, 102, 255));
 
         jLabel19.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel19.setText("Kg");
 
         jLabel20.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel20.setText("TALLA");
 
         jTextField10.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField10.setForeground(new java.awt.Color(0, 102, 255));
 
         jLabel21.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel21.setText("m");
 
         javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                     .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jTextField9)
                     .addComponent(jTextField4)
                     .addComponent(jTextField3)
                     .addComponent(jTextField2)
                     .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(53, 53, 53)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                             .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                         .addGap(2, 2, 2)))
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jTextField10, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                     .addComponent(jTextField6)
                     .addComponent(jTextField7)
                     .addComponent(jTextField8)
                     .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel12Layout.createSequentialGroup()
                         .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jLabel8)
                                     .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel14))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jLabel7)
                                     .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel15))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel6)
                                     .addComponent(jLabel16))
                                 .addGap(19, 19, 19)
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel5)
                                     .addComponent(jLabel17))
                                 .addGap(17, 17, 17)
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jLabel18)
                                     .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel19)
                                     .addComponent(jLabel20)
                                     .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel21)))
                             .addGroup(jPanel12Layout.createSequentialGroup()
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jLabel1)
                                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel10))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jLabel2)
                                     .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel11))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel3)
                                     .addComponent(jLabel12))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel4)
                                     .addComponent(jLabel13))))
                         .addGap(0, 9, Short.MAX_VALUE))
                     .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("CONSTANTES", jPanel12);
 
         jTextArea3.setColumns(20);
         jTextArea3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea3.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea3.setLineWrap(true);
         jTextArea3.setRows(5);
         jTextArea3.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea3KeyPressed(evt);
             }
         });
         jScrollPane6.setViewportView(jTextArea3);
 
         javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
         jPanel14.setLayout(jPanel14Layout);
         jPanel14Layout.setHorizontalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel14Layout.setVerticalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab("ASPECTO GENERAL", jPanel14);
 
         jTextArea4.setColumns(20);
         jTextArea4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea4.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea4.setLineWrap(true);
         jTextArea4.setRows(5);
         jTextArea4.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea4.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea4FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea4FocusLost(evt);
             }
         });
         jTextArea4.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea4KeyPressed(evt);
             }
         });
         jScrollPane10.setViewportView(jTextArea4);
 
         javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
         jPanel15.setLayout(jPanel15Layout);
         jPanel15Layout.setHorizontalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel15Layout.setVerticalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab("NEUROLOGICO ORL Y CARA", jPanel15);
 
         jTextArea6.setColumns(20);
         jTextArea6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea6.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea6.setLineWrap(true);
         jTextArea6.setRows(5);
         jTextArea6.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea6.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea6FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea6FocusLost(evt);
             }
         });
         jTextArea6.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea6KeyPressed(evt);
             }
         });
         jScrollPane11.setViewportView(jTextArea6);
 
         javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
         jPanel16.setLayout(jPanel16Layout);
         jPanel16Layout.setHorizontalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel16Layout.setVerticalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab("CARDIOVASCULAR", jPanel16);
 
         jTextArea8.setColumns(20);
         jTextArea8.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea8.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea8.setLineWrap(true);
         jTextArea8.setRows(5);
         jTextArea8.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea8.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea8FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea8FocusLost(evt);
             }
         });
         jTextArea8.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea8KeyPressed(evt);
             }
         });
         jScrollPane12.setViewportView(jTextArea8);
 
         javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
         );
 
         jTabbedPane1.addTab("RESPIRATORIO", jPanel13);
 
         jTabbedPane2.setFocusable(false);
         jTabbedPane2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
 
         jTextArea9.setColumns(20);
         jTextArea9.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea9.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea9.setLineWrap(true);
         jTextArea9.setRows(5);
         jTextArea9.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea9.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea9FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea9FocusLost(evt);
             }
         });
         jTextArea9.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea9KeyPressed(evt);
             }
         });
         jScrollPane13.setViewportView(jTextArea9);
 
         javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
         jPanel17.setLayout(jPanel17Layout);
         jPanel17Layout.setHorizontalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel17Layout.setVerticalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
         );
 
         jTabbedPane2.addTab("GASTROINTESTINAL", jPanel17);
 
         jTextArea14.setColumns(20);
         jTextArea14.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea14.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea14.setLineWrap(true);
         jTextArea14.setRows(5);
         jTextArea14.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea14.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea14FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea14FocusLost(evt);
             }
         });
         jTextArea14.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea14KeyPressed(evt);
             }
         });
         jScrollPane14.setViewportView(jTextArea14);
 
         javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
         jPanel18.setLayout(jPanel18Layout);
         jPanel18Layout.setHorizontalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel18Layout.setVerticalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
         );
 
         jTabbedPane2.addTab("GENITOURINARIO", jPanel18);
 
         jTextArea15.setColumns(20);
         jTextArea15.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea15.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea15.setLineWrap(true);
         jTextArea15.setRows(5);
         jTextArea15.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea15.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea15FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea15FocusLost(evt);
             }
         });
         jTextArea15.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea15KeyPressed(evt);
             }
         });
         jScrollPane15.setViewportView(jTextArea15);
 
         javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
         jPanel22.setLayout(jPanel22Layout);
         jPanel22Layout.setHorizontalGroup(
             jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel22Layout.setVerticalGroup(
             jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
         );
 
         jTabbedPane2.addTab("HEMATOINFECCIOSO", jPanel22);
 
         jTextArea16.setColumns(20);
         jTextArea16.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea16.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea16.setLineWrap(true);
         jTextArea16.setRows(5);
         jTextArea16.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea16.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea16FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea16FocusLost(evt);
             }
         });
         jTextArea16.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea16KeyPressed(evt);
             }
         });
         jScrollPane16.setViewportView(jTextArea16);
 
         javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
         jPanel23.setLayout(jPanel23Layout);
         jPanel23Layout.setHorizontalGroup(
             jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel23Layout.setVerticalGroup(
             jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
         );
 
         jTabbedPane2.addTab("ENDOCRINO - METABOLICO", jPanel23);
 
         jTextArea17.setColumns(20);
         jTextArea17.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea17.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea17.setLineWrap(true);
         jTextArea17.setRows(5);
         jTextArea17.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         jTextArea17.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 jTextArea17FocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextArea17FocusLost(evt);
             }
         });
         jTextArea17.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jTextArea17KeyPressed(evt);
             }
         });
         jScrollPane17.setViewportView(jTextArea17);
 
         javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
         jPanel24.setLayout(jPanel24Layout);
         jPanel24Layout.setHorizontalGroup(
             jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
         );
         jPanel24Layout.setVerticalGroup(
             jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
         );
 
         jTabbedPane2.addTab("OSTEOMUSCULAR Y PIEL", jPanel24);
 
         jLabel41.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel41.setText("EXPLORACION FISICA");
 
         javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane2)
             .addComponent(jLabel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jpExpFisicaLayout = new javax.swing.GroupLayout(jpExpFisica);
         jpExpFisica.setLayout(jpExpFisicaLayout);
         jpExpFisicaLayout.setHorizontalGroup(
             jpExpFisicaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jpExpFisicaLayout.setVerticalGroup(
             jpExpFisicaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jpPruebasDiag.setMaximumSize(new java.awt.Dimension(584, 472));
         jpPruebasDiag.setMinimumSize(new java.awt.Dimension(584, 472));
         jpPruebasDiag.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jPanel27.setBackground(new java.awt.Color(255, 255, 255));
         jPanel27.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
 
         jPanel32.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "HALLAZGOS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 10))); // NOI18N
         jPanel32.setOpaque(false);
 
         jTextArea19.setColumns(20);
         jTextArea19.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextArea19.setForeground(new java.awt.Color(0, 102, 255));
         jTextArea19.setLineWrap(true);
         jTextArea19.setRows(5);
         jScrollPane26.setViewportView(jTextArea19);
 
         javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
         jPanel32.setLayout(jPanel32Layout);
         jPanel32Layout.setHorizontalGroup(
             jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane26, javax.swing.GroupLayout.Alignment.TRAILING)
         );
         jPanel32Layout.setVerticalGroup(
             jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane26, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
         );
 
         jScrollPane27.setBackground(new java.awt.Color(255, 255, 255));
         jScrollPane27.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         jScrollPane27.setPreferredSize(new java.awt.Dimension(200, 200));
 
         jtbTratamiento4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         jtbTratamiento4.setForeground(new java.awt.Color(0, 51, 255));
         jtbTratamiento4.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         jtbTratamiento4.setShowHorizontalLines(false);
         jtbTratamiento4.setShowVerticalLines(false);
         jtbTratamiento4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jtbTratamiento4MouseClicked(evt);
             }
         });
         jScrollPane27.setViewportView(jtbTratamiento4);
 
         jLabel34.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel34.setText("Adjuntar Documentos");
 
         jTextField16.setEditable(false);
         jTextField16.setBackground(new java.awt.Color(255, 255, 255));
         jTextField16.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField16.setForeground(new java.awt.Color(0, 102, 255));
         jTextField16.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
 
         jButton8.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jButton8.setText("...");
         jButton8.setFocusable(false);
         jButton8.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton8ActionPerformed(evt);
             }
         });
 
         jLabel42.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel42.setText("PRUEBAS COMPLEMENTARIAS");
 
         javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
         jPanel27.setLayout(jPanel27Layout);
         jPanel27Layout.setHorizontalGroup(
             jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel27Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane27, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                         .addGap(0, 17, Short.MAX_VALUE)
                         .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jLabel42, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel27Layout.setVerticalGroup(
             jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel27Layout.createSequentialGroup()
                 .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel34)
                     .addComponent(jButton8))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane27, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout jpPruebasDiagLayout = new javax.swing.GroupLayout(jpPruebasDiag);
         jpPruebasDiag.setLayout(jpPruebasDiagLayout);
         jpPruebasDiagLayout.setHorizontalGroup(
             jpPruebasDiagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jpPruebasDiagLayout.setVerticalGroup(
             jpPruebasDiagLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jpDiagMedico.setMaximumSize(new java.awt.Dimension(584, 472));
         jpDiagMedico.setMinimumSize(new java.awt.Dimension(584, 472));
         jpDiagMedico.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jPanel28.setBackground(new java.awt.Color(255, 255, 255));
         jPanel28.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jButton2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jButton2.setText("...");
         jButton2.setFocusable(false);
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jLabel28.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel28.setText("DIAGNOSTICO PRINCIPAL");
 
         jTextField11.setEditable(false);
         jTextField11.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField11.setForeground(new java.awt.Color(0, 102, 255));
         jTextField11.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField11KeyReleased(evt);
             }
         });
 
         jTextField12.setEditable(false);
         jTextField12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField12.setForeground(new java.awt.Color(0, 102, 255));
         jTextField12.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField12KeyReleased(evt);
             }
         });
 
         jLabel29.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel29.setText("DIAGNOSTICO RELACIONADO 1");
 
         jTextField13.setEditable(false);
         jTextField13.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField13.setForeground(new java.awt.Color(0, 102, 255));
         jTextField13.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField13KeyReleased(evt);
             }
         });
 
         jLabel30.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel30.setText("DIAGNOSTICO RELACIONADO 2");
 
         jButton4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jButton4.setText("...");
         jButton4.setFocusable(false);
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jButton5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jButton5.setText("...");
         jButton5.setFocusable(false);
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
 
         jLabel31.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel31.setText("DIAGNOSTICO RELACIONADO 3");
 
         jTextField14.setEditable(false);
         jTextField14.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField14.setForeground(new java.awt.Color(0, 102, 255));
         jTextField14.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField14KeyReleased(evt);
             }
         });
 
         jButton6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jButton6.setText("...");
         jButton6.setFocusable(false);
         jButton6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton6ActionPerformed(evt);
             }
         });
 
         jButton7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jButton7.setText("...");
         jButton7.setFocusable(false);
         jButton7.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton7ActionPerformed(evt);
             }
         });
 
         jTextField15.setEditable(false);
         jTextField15.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jTextField15.setForeground(new java.awt.Color(0, 102, 255));
         jTextField15.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField15KeyReleased(evt);
             }
         });
 
         jLabel32.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel32.setText("DIAGNOSTICO RELACIONADO 4");
 
         jLabel43.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel43.setText("DIAGNOSTICO MEDICO");
 
         javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
         jPanel28.setLayout(jPanel28Layout);
         jPanel28Layout.setHorizontalGroup(
             jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel28Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jTextField15)
                     .addComponent(jTextField14, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jTextField13, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jTextField11, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel43, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel28Layout.createSequentialGroup()
                         .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel28Layout.createSequentialGroup()
                         .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jTextField12)
                     .addGroup(jPanel28Layout.createSequentialGroup()
                         .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel28Layout.createSequentialGroup()
                         .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel28Layout.createSequentialGroup()
                         .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         jPanel28Layout.setVerticalGroup(
             jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel28Layout.createSequentialGroup()
                 .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel28)
                     .addComponent(jButton2))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(12, 12, 12)
                 .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel29)
                     .addComponent(jButton4))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel30)
                     .addComponent(jButton5))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel31)
                     .addComponent(jButton6))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel32)
                     .addComponent(jButton7))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(138, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jpDiagMedicoLayout = new javax.swing.GroupLayout(jpDiagMedico);
         jpDiagMedico.setLayout(jpDiagMedicoLayout);
         jpDiagMedicoLayout.setHorizontalGroup(
             jpDiagMedicoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jpDiagMedicoLayout.setVerticalGroup(
             jpDiagMedicoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jpTratamiento.setBackground(new java.awt.Color(255, 255, 255));
         jpTratamiento.setMaximumSize(new java.awt.Dimension(584, 472));
         jpTratamiento.setMinimumSize(new java.awt.Dimension(584, 472));
         jpTratamiento.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jPanel1.setMaximumSize(new java.awt.Dimension(584, 472));
         jPanel1.setMinimumSize(new java.awt.Dimension(584, 472));
         jPanel1.setOpaque(false);
         jPanel1.setPreferredSize(new java.awt.Dimension(584, 472));
 
         jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel35.setText("TRTAMIENTO E INDICACIONES");
 
         jPanel29.setBackground(new java.awt.Color(255, 255, 255));
 
         jPanel35.setBackground(new java.awt.Color(255, 255, 255));
         jPanel35.setFocusable(false);
         jPanel35.setMaximumSize(new java.awt.Dimension(380, 420));
         jPanel35.setMinimumSize(new java.awt.Dimension(380, 420));
         jPanel35.setOpaque(false);
         jPanel35.setPreferredSize(new java.awt.Dimension(380, 420));
 
         javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
         jPanel35.setLayout(jPanel35Layout);
         jPanel35Layout.setHorizontalGroup(
             jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 380, Short.MAX_VALUE)
         );
         jPanel35Layout.setVerticalGroup(
             jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 420, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
         jPanel29.setLayout(jPanel29Layout);
         jPanel29Layout.setHorizontalGroup(
             jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel29Layout.setVerticalGroup(
             jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel29Layout.createSequentialGroup()
                 .addComponent(jPanel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(30, Short.MAX_VALUE))
         );
 
         jPanel2.setOpaque(false);
 
         jXTaskPane1.setTitle("MEDICAMENTOS");
         jXTaskPane1.setAnimated(false);
         jXTaskPane1.setFocusable(false);
         jXTaskPane1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
         jXTaskPane1.setOpaque(true);
         jXTaskPane1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jXTaskPane1MouseReleased(evt);
             }
         });
 
         jLabel46.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Pills.png"))); // NOI18N
         jLabel46.setText("Medicamentos");
         jLabel46.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel46.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel46MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel46MouseExited(evt);
             }
         });
         jLabel46.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel46MouseMoved(evt);
             }
         });
         jXTaskPane1.getContentPane().add(jLabel46);
 
         jXTaskPane2.setExpanded(false);
         jXTaskPane2.setTitle("PROCEDIMIENTOS");
         jXTaskPane2.setAnimated(false);
         jXTaskPane2.setFocusable(false);
         jXTaskPane2.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
         jXTaskPane2.setOpaque(true);
         jXTaskPane2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jXTaskPane2MouseReleased(evt);
             }
         });
 
         jLabel36.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bag_icon.png"))); // NOI18N
         jLabel36.setText("<html><p> Monitorización y Tratamientos Diagnósticos</p></html>");
         jLabel36.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel36.setDoubleBuffered(true);
         jLabel36.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel36MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel36MouseExited(evt);
             }
         });
         jLabel36.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel36MouseMoved(evt);
             }
         });
         jXTaskPane2.getContentPane().add(jLabel36);
 
         jLabel45.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel45.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/microscope_icon.png"))); // NOI18N
         jLabel45.setText("<html><p>Laboratorio Clinico</p></html>");
         jLabel45.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel45.setDoubleBuffered(true);
         jLabel45.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel45MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel45MouseExited(evt);
             }
         });
         jLabel45.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel45MouseMoved(evt);
             }
         });
         jXTaskPane2.getContentPane().add(jLabel45);
 
         jLabel49.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/XRay.png"))); // NOI18N
         jLabel49.setText("Imagenologia");
         jLabel49.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel49.setDoubleBuffered(true);
         jLabel49.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel49MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel49MouseExited(evt);
             }
         });
         jLabel49.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel49MouseMoved(evt);
             }
         });
         jXTaskPane2.getContentPane().add(jLabel49);
 
         jLabel50.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Skalpell.png"))); // NOI18N
         jLabel50.setText("Quirúrgicos");
         jLabel50.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel50.setDoubleBuffered(true);
         jLabel50.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel50MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel50MouseExited(evt);
             }
         });
         jLabel50.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel50MouseMoved(evt);
             }
         });
         jXTaskPane2.getContentPane().add(jLabel50);
 
         jLabel51.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel51.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Hospital.png"))); // NOI18N
         jLabel51.setText("Mas Procedimientos");
         jLabel51.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel51.setDoubleBuffered(true);
         jLabel51.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel51MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel51MouseExited(evt);
             }
         });
         jLabel51.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel51MouseMoved(evt);
             }
         });
         jXTaskPane2.getContentPane().add(jLabel51);
 
         jXTaskPane3.setExpanded(false);
         jXTaskPane3.setTitle("VALORACION ESPECIALISTA");
         jXTaskPane3.setAnimated(false);
         jXTaskPane3.setFocusable(false);
         jXTaskPane3.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
         jXTaskPane3.setOpaque(true);
         jXTaskPane3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jXTaskPane3MouseReleased(evt);
             }
         });
 
         jLabel48.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel48.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/surgeon_icon.png"))); // NOI18N
         jLabel48.setText("Cirugia General");
         jLabel48.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel48.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel48MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel48MouseExited(evt);
             }
         });
         jLabel48.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel48MouseMoved(evt);
             }
         });
         jXTaskPane3.getContentPane().add(jLabel48);
 
         jLabel52.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel52.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Female_icon.png"))); // NOI18N
         jLabel52.setText("Ginecología");
         jLabel52.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel52.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel52MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel52MouseExited(evt);
             }
         });
         jLabel52.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel52MouseMoved(evt);
             }
         });
         jXTaskPane3.getContentPane().add(jLabel52);
 
         jLabel53.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel53.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Head_physician_icon.png"))); // NOI18N
         jLabel53.setText("Medicina Interna");
         jLabel53.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel53.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel53MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel53MouseExited(evt);
             }
         });
         jLabel53.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel53MouseMoved(evt);
             }
         });
         jXTaskPane3.getContentPane().add(jLabel53);
 
         jLabel54.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel54.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plaster_icon.png"))); // NOI18N
         jLabel54.setText("Ortopedia");
         jLabel54.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel54.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel54MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel54MouseExited(evt);
             }
         });
         jLabel54.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel54MouseMoved(evt);
             }
         });
         jXTaskPane3.getContentPane().add(jLabel54);
 
         jLabel55.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel55.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stethoscope-icon.png"))); // NOI18N
         jLabel55.setText("Pediatria");
         jLabel55.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel55.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel55MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel55MouseExited(evt);
             }
         });
         jLabel55.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel55MouseMoved(evt);
             }
         });
         jXTaskPane3.getContentPane().add(jLabel55);
 
         jLabel56.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Hospital.png"))); // NOI18N
         jLabel56.setText("Otras Especialidades");
         jLabel56.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel56.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel56MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel56MouseExited(evt);
             }
         });
         jLabel56.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel56MouseMoved(evt);
             }
         });
         jXTaskPane3.getContentPane().add(jLabel56);
 
         jXTaskPane4.setExpanded(false);
         jXTaskPane4.setTitle("MEDIDAS GENERALES");
         jXTaskPane4.setAnimated(false);
         jXTaskPane4.setFocusable(false);
         jXTaskPane4.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
         jXTaskPane4.setOpaque(true);
         jXTaskPane4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jXTaskPane4MouseReleased(evt);
             }
         });
 
         jLabel47.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel47.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Edit.png"))); // NOI18N
         jLabel47.setText("Medidas generales");
         jLabel47.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel47.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel47MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel47MouseExited(evt);
             }
         });
         jLabel47.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel47MouseMoved(evt);
             }
         });
         jXTaskPane4.getContentPane().add(jLabel47);
 
         jXTaskPane5.setExpanded(false);
         jXTaskPane5.setTitle("AUTORIZACIONES");
         jXTaskPane5.setAnimated(false);
         jXTaskPane5.setFocusable(false);
         jXTaskPane5.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
         jXTaskPane5.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jXTaskPane5MouseReleased(evt);
             }
         });
 
         jLabel58.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel58.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/product-sales-report-icon.png"))); // NOI18N
         jLabel58.setText("Anexo 3");
         jLabel58.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel58.setEnabled(false);
         jLabel58.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel58MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel58MouseExited(evt);
             }
         });
         jLabel58.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel58MouseMoved(evt);
             }
         });
         jXTaskPane5.getContentPane().add(jLabel58);
 
         jLabel59.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         jLabel59.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/report-icon.png"))); // NOI18N
         jLabel59.setText("CTC");
         jLabel59.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel59.setEnabled(false);
         jLabel59.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel59MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel59MouseExited(evt);
             }
         });
         jLabel59.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel59MouseMoved(evt);
             }
         });
         jXTaskPane5.getContentPane().add(jLabel59);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jXTaskPane5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jXTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jXTaskPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jXTaskPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jXTaskPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jXTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jXTaskPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jXTaskPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jXTaskPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jXTaskPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
         );
 
         javax.swing.GroupLayout jpTratamientoLayout = new javax.swing.GroupLayout(jpTratamiento);
         jpTratamiento.setLayout(jpTratamientoLayout);
         jpTratamientoLayout.setHorizontalGroup(
             jpTratamientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpTratamientoLayout.createSequentialGroup()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         jpTratamientoLayout.setVerticalGroup(
             jpTratamientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jpTratamientoLayout.createSequentialGroup()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         setBackground(new java.awt.Color(255, 255, 255));
         setMaximumSize(new java.awt.Dimension(764, 514));
         setMinimumSize(new java.awt.Dimension(764, 514));
         setPreferredSize(new java.awt.Dimension(764, 514));
 
         jpCentro.setBackground(new java.awt.Color(204, 204, 255));
         jpCentro.setMaximumSize(new java.awt.Dimension(584, 472));
         jpCentro.setMinimumSize(new java.awt.Dimension(584, 472));
         jpCentro.setPreferredSize(new java.awt.Dimension(584, 472));
 
         javax.swing.GroupLayout jpCentroLayout = new javax.swing.GroupLayout(jpCentro);
         jpCentro.setLayout(jpCentroLayout);
         jpCentroLayout.setHorizontalGroup(
             jpCentroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 584, Short.MAX_VALUE)
         );
         jpCentroLayout.setVerticalGroup(
             jpCentroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 472, Short.MAX_VALUE)
         );
 
         jLabel23.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
         jLabel23.setForeground(new java.awt.Color(51, 102, 255));
         jLabel23.setText("Antecedentes Personales");
         jLabel23.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
         jLabel23.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel23.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel23MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel23MouseExited(evt);
             }
         });
         jLabel23.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel23MouseMoved(evt);
             }
         });
 
         jLabel22.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
         jLabel22.setForeground(new java.awt.Color(51, 102, 255));
         jLabel22.setText("Motivo de Consulta");
         jLabel22.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
         jLabel22.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel22.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel22MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel22MouseExited(evt);
             }
         });
         jLabel22.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel22MouseMoved(evt);
             }
         });
 
         jLabel24.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
         jLabel24.setForeground(new java.awt.Color(51, 102, 255));
         jLabel24.setText("Enfermedad Actual");
         jLabel24.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
         jLabel24.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel24.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel24MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel24MouseExited(evt);
             }
         });
         jLabel24.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel24MouseMoved(evt);
             }
         });
 
         jLabel25.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
         jLabel25.setForeground(new java.awt.Color(51, 102, 255));
         jLabel25.setText("Exploracion Fisica");
         jLabel25.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
         jLabel25.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel25.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel25MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel25MouseExited(evt);
             }
         });
         jLabel25.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel25MouseMoved(evt);
             }
         });
 
         jLabel26.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
         jLabel26.setForeground(new java.awt.Color(51, 102, 255));
         jLabel26.setText("Diagnostico Medico");
         jLabel26.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
         jLabel26.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel26.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel26MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel26MouseExited(evt);
             }
         });
         jLabel26.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel26MouseMoved(evt);
             }
         });
 
         jLabel27.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
         jLabel27.setForeground(new java.awt.Color(51, 102, 255));
         jLabel27.setText("Pruebas Complementarias");
         jLabel27.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
         jLabel27.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel27.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel27MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel27MouseExited(evt);
             }
         });
         jLabel27.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel27MouseMoved(evt);
             }
         });
 
         jLabel33.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
         jLabel33.setForeground(new java.awt.Color(51, 102, 255));
         jLabel33.setText("Ordenes Medicas");
         jLabel33.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
         jLabel33.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         jLabel33.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jLabel33MouseClicked(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jLabel33MouseExited(evt);
             }
         });
         jLabel33.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jLabel33MouseMoved(evt);
             }
         });
 
         jlbNombrePaciente.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         jlbNombrePaciente.setForeground(new java.awt.Color(0, 102, 255));
         jlbNombrePaciente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user.png"))); // NOI18N
         jlbNombrePaciente.setText("NOMBRE");
 
         jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/add_22x22.png"))); // NOI18N
         jButton3.setToolTipText("Mas datos del paciente");
         jButton3.setFocusable(false);
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         jCheckBox4.setBackground(new java.awt.Color(255, 255, 255));
         jCheckBox4.setEnabled(false);
 
         jCheckBox5.setBackground(new java.awt.Color(255, 255, 255));
         jCheckBox5.setEnabled(false);
 
         jCheckBox6.setBackground(new java.awt.Color(255, 255, 255));
         jCheckBox6.setEnabled(false);
 
         jCheckBox10.setBackground(new java.awt.Color(255, 255, 255));
         jCheckBox10.setEnabled(false);
 
         jCheckBox11.setBackground(new java.awt.Color(255, 255, 255));
         jCheckBox11.setEnabled(false);
 
         jCheckBox12.setBackground(new java.awt.Color(255, 255, 255));
         jCheckBox12.setEnabled(false);
 
         jCheckBox13.setBackground(new java.awt.Color(255, 255, 255));
         jCheckBox13.setEnabled(false);
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Save.png"))); // NOI18N
         jButton1.setBorderPainted(false);
         jButton1.setContentAreaFilled(false);
         jButton1.setFocusable(false);
         jButton1.setMaximumSize(new java.awt.Dimension(32, 32));
         jButton1.setMinimumSize(new java.awt.Dimension(32, 32));
         jButton1.setPreferredSize(new java.awt.Dimension(32, 32));
         jButton1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Save1.png"))); // NOI18N
         jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jButton1MouseExited(evt);
             }
         });
         jButton1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jButton1MouseMoved(evt);
             }
         });
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/editdelete.png"))); // NOI18N
         jButton9.setBorderPainted(false);
         jButton9.setContentAreaFilled(false);
         jButton9.setFocusable(false);
         jButton9.setMaximumSize(new java.awt.Dimension(32, 32));
         jButton9.setMinimumSize(new java.awt.Dimension(32, 32));
         jButton9.setPreferredSize(new java.awt.Dimension(32, 32));
         jButton9.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/editdelete1.png"))); // NOI18N
         jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jButton9MouseExited(evt);
             }
         });
         jButton9.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jButton9MouseMoved(evt);
             }
         });
         jButton9.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton9ActionPerformed(evt);
             }
         });
 
         jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/final.png"))); // NOI18N
         jButton10.setBorderPainted(false);
         jButton10.setContentAreaFilled(false);
         jButton10.setFocusable(false);
         jButton10.setMaximumSize(new java.awt.Dimension(32, 32));
         jButton10.setMinimumSize(new java.awt.Dimension(32, 32));
         jButton10.setPreferredSize(new java.awt.Dimension(32, 32));
         jButton10.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/final1.png"))); // NOI18N
         jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jButton10MouseExited(evt);
             }
         });
         jButton10.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jButton10MouseMoved(evt);
             }
         });
         jButton10.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton10ActionPerformed(evt);
             }
         });
 
         jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/print_22x22.png"))); // NOI18N
         jButton11.setBorderPainted(false);
         jButton11.setContentAreaFilled(false);
         jButton11.setFocusable(false);
         jButton11.setMaximumSize(new java.awt.Dimension(32, 32));
         jButton11.setMinimumSize(new java.awt.Dimension(32, 32));
         jButton11.setPreferredSize(new java.awt.Dimension(32, 32));
         jButton11.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/print_22x22_1.png"))); // NOI18N
         jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 jButton11MouseExited(evt);
             }
         });
         jButton11.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 jButton11MouseMoved(evt);
             }
         });
         jButton11.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton11ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jCheckBox5))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jCheckBox13))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jCheckBox4))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jCheckBox6))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jCheckBox12))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jCheckBox11))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jCheckBox10)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addComponent(jpCentro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(jlbNombrePaciente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jlbNombrePaciente)
                     .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jpCentro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap(1, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jCheckBox6, javax.swing.GroupLayout.Alignment.TRAILING))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jCheckBox10)
                             .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jCheckBox11)
                             .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jCheckBox12)
                             .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jCheckBox4, javax.swing.GroupLayout.Alignment.TRAILING))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jCheckBox13))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jCheckBox5))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(43, 43, 43))))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     // <editor-fold desc="eventos de componentes">
     
     private void jLabel22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MouseClicked
         jpCentro.removeAll();
         jpMotivoC.setBounds(0, 0, 584, 472);
         jpCentro.add(jpMotivoC);
         jpMotivoC.setVisible(true);
         jpCentro.validate();
         jpCentro.repaint();
         activeCheck(1);
     }//GEN-LAST:event_jLabel22MouseClicked
 
     private void jLabel23MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MouseClicked
         jpCentro.removeAll();
         jpAntPersonales.setBounds(0, 0, 584, 472);
         jpCentro.add(jpAntPersonales);
         jpAntPersonales.setVisible(true);
         jpCentro.validate();
         jpCentro.repaint(); 
         activeCheck(2);
     }//GEN-LAST:event_jLabel23MouseClicked
 
     private void jLabel24MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseClicked
         jpCentro.removeAll();
         jpEnfActual.setBounds(0, 0, 584, 472);
         jpCentro.add(jpEnfActual);
         jpEnfActual.setVisible(true);
         jpCentro.validate();
         jpCentro.repaint(); 
         activeCheck(3);
     }//GEN-LAST:event_jLabel24MouseClicked
 
     private void jLabel25MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel25MouseClicked
         jpCentro.removeAll();
         jpExpFisica.setBounds(0, 0, 584, 472);
         jpCentro.add(jpExpFisica);
         jpExpFisica.setVisible(true);
         jpCentro.validate();
         jpCentro.repaint(); 
         activeCheck(4);
     }//GEN-LAST:event_jLabel25MouseClicked
 
     private void jLabel26MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel26MouseClicked
         jpCentro.removeAll();
         jpDiagMedico.setBounds(0, 0, 584, 472);
         jpCentro.add(jpDiagMedico);
         jpDiagMedico.setVisible(true);
         jpCentro.validate();
         jpCentro.repaint(); 
         activeCheck(6);
     }//GEN-LAST:event_jLabel26MouseClicked
 
     private void jLabel27MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel27MouseClicked
         jpCentro.removeAll();
         jpPruebasDiag.setBounds(0, 0, 584, 472);
         jpCentro.add(jpPruebasDiag);
         jpPruebasDiag.setVisible(true);
         jpCentro.validate();
         jpCentro.repaint(); 
         activeCheck(5);
     }//GEN-LAST:event_jLabel27MouseClicked
 
     private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         if(personas==null){
             personas = new FormPersonas();
             personas.infopaciente = this.infopaciente;
                 //mostrar datos personas
         }else{
             personas.infopaciente = this.infopaciente;
         }
         personas.setVisible(true);
     }//GEN-LAST:event_jButton3ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if(cie==null){
             cie = new cie10();
             cie.setLocationRelativeTo(this);
             cie.setVisible(true);
         }else{
             cie.setVisible(true);
         }
        cie.diag=1;
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         if(cie==null){
             cie = new cie10();
             cie.setLocationRelativeTo(this);
             cie.setVisible(true);
         }else{
             cie.setVisible(true);
         }
         cie.diag=2;
     }//GEN-LAST:event_jButton4ActionPerformed
 
     private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         if(cie==null){
             cie = new cie10();
             cie.setLocationRelativeTo(this);
             cie.setVisible(true);
         }else{
             cie.setVisible(true);
         }
         cie.diag=3;
     }//GEN-LAST:event_jButton5ActionPerformed
 
     private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
         if(cie==null){
             cie = new cie10();
             cie.setLocationRelativeTo(this);
             cie.setVisible(true);
         }else{
             cie.setVisible(true);
         }
         cie.diag=4;        
     }//GEN-LAST:event_jButton6ActionPerformed
 
     private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
         if(cie==null){
             cie = new cie10();
             cie.setLocationRelativeTo(this);
             cie.setVisible(true);
         }else{
             cie.setVisible(true);
         }
         cie.diag=5;
     }//GEN-LAST:event_jButton7ActionPerformed
 
     private void jLabel33MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel33MouseClicked
         jpCentro.removeAll();
         jpTratamiento.setBounds(0, 0, 584, 472);
         jpCentro.add(jpTratamiento);
         jpTratamiento.setVisible(true);
         jpCentro.validate();
         jpCentro.repaint(); 
         activeCheck(7);
     }//GEN-LAST:event_jLabel33MouseClicked
 
     private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
         jFileChooser1 = new JFileChooser();
          FileNameExtensionFilter filter= new FileNameExtensionFilter("JPG & PNG", "jpg", "png");
          jFileChooser1.setMultiSelectionEnabled(false);
          jFileChooser1.setFileFilter(filter);
         int result = jFileChooser1.showOpenDialog(null);
         File file = null;
         if(result == JFileChooser.APPROVE_OPTION){
             String seleccion = (String) JOptionPane.showInputDialog(this,"Tipo de Archivo Adjunto","Mensaje",
                 JOptionPane.QUESTION_MESSAGE,null,tipoAyudaDiag,"LABORATORIO");
             file = jFileChooser1.getSelectedFile();
             jTextField16.setText(file.getAbsolutePath());
             ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource("/images/Delete16x16.png"));
             ImageIcon icon2 = new javax.swing.ImageIcon(getClass().getResource("/images/download.png"));
             int indexRow = jtbTratamiento4.getRowCount();
             if(modeloAyudDiag.getRowCount()==0){
                 modeloAyudDiag.addRow(dato);
                 modeloAyudDiag.setValueAt(file.getName(), indexRow, 2);
                 modeloAyudDiag.setValueAt(new JLabel(icon), indexRow, 0);
                 modeloAyudDiag.setValueAt(new JLabel(icon2), indexRow, 1);
                 modeloAyudDiag.setValueAt(file, indexRow, 3);
                 modeloAyudDiag.setValueAt(seleccion, indexRow, 4);
                 modeloAyudDiag.setValueAt(ReturnPathdiagHelpSaveFile(seleccion), indexRow, 5);
                 modeloAyudDiag.setValueAt("0", indexRow, 6);//identifica que aun no ha sido guardado en la bd
             }else{
                 Boolean exist=null;
                 for(int i =0;i<modeloAyudDiag.getRowCount();i++){
                     if(((String)modeloAyudDiag.getValueAt(i, 2)).equals(file.getName())){
                         exist = true;
                         break;
                     }else{
                         exist = false;
                     }
                 }
                 if(!exist){
                     modeloAyudDiag.addRow(dato);
                     modeloAyudDiag.setValueAt(file.getName(), indexRow, 2);
                     modeloAyudDiag.setValueAt(new JLabel(icon), indexRow, 0);
                     modeloAyudDiag.setValueAt(new JLabel(icon2), indexRow, 1);
                     modeloAyudDiag.setValueAt(file, indexRow, 3);
                     modeloAyudDiag.setValueAt(seleccion, indexRow, 4);
                     modeloAyudDiag.setValueAt(ReturnPathdiagHelpSaveFile(seleccion), indexRow, 5); 
                     modeloAyudDiag.setValueAt("0", indexRow, 6);//identifica que aun no ha sido guardado en la bd
                 }
             }
         }
     }//GEN-LAST:event_jButton8ActionPerformed
 
     private void jtbTratamiento4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtbTratamiento4MouseClicked
         if(SwingUtilities.isLeftMouseButton(evt)){
             if(jtbTratamiento4.columnAtPoint(evt.getPoint())==0) {
                int confirmar = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea eliminar este archivo de forma permanente?"
                        ,"Eliminar archivo",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(confirmar==JOptionPane.YES_OPTION){
                     int row = jtbTratamiento4.rowAtPoint(evt.getPoint());
                     modDestroyAyudDiag.addRow(new Object[] {modeloAyudDiag.getValueAt(row, 0)
                             ,modeloAyudDiag.getValueAt(row, 1),modeloAyudDiag.getValueAt(row, 2),modeloAyudDiag.getValueAt(row, 3)
                             ,modeloAyudDiag.getValueAt(row, 4),modeloAyudDiag.getValueAt(row, 5),modeloAyudDiag.getValueAt(row, 6)});
                     modeloAyudDiag.removeRow(row);
                } 
             }else if(jtbTratamiento4.columnAtPoint(evt.getPoint())==1){
                 //verificar existencia del archivo en la bd o en la clase entidad
                     if(((String)jtbTratamiento4.getValueAt(jtbTratamiento4.rowAtPoint(evt.getPoint()), 6))
                             .equals("1")){
                             String path2 = (String)modeloAyudDiag.getValueAt(jtbTratamiento4.rowAtPoint(evt.getPoint()), 5)
                                     +s+(String)modeloAyudDiag.getValueAt(jtbTratamiento4.rowAtPoint(evt.getPoint()), 2);
                             Funciones.fileDownload(path2);
                     }
             }
         }
     }//GEN-LAST:event_jtbTratamiento4MouseClicked
 
     private void jTextField15KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField15KeyReleased
        if("".equals(jTextField15.getText())){
             idDiag5 = 1;
         }
     }//GEN-LAST:event_jTextField15KeyReleased
 
     private void jTextField14KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField14KeyReleased
         if("".equals(jTextField14.getText())){
             idDiag4 = 1;
         }
     }//GEN-LAST:event_jTextField14KeyReleased
 
     private void jTextField13KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField13KeyReleased
         if("".equals(jTextField13.getText())){
             idDiag3 = 1;
         }
     }//GEN-LAST:event_jTextField13KeyReleased
 
     private void jTextField12KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField12KeyReleased
         if("".equals(jTextField12.getText())){
             idDiag2 = 1;
         }
     }//GEN-LAST:event_jTextField12KeyReleased
 
     private void jTextField11KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField11KeyReleased
         if("".equals(jTextField11.getText())){
             idDiag1 = 1;
         }
     }//GEN-LAST:event_jTextField11KeyReleased
 
     private void jTextArea10KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea10KeyTyped
         if(evt.getKeyCode()==KeyEvent.VK_CONTROL){
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea10KeyTyped
 
     private void jLabel50MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel50MouseMoved
         Funciones.setLabelInfo("PROCEDIMIENTOS E INTERVENCIONES QUIRÚRGICAS");
         jLabel50.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel50MouseMoved
 
     private void jLabel50MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel50MouseExited
         Funciones.setLabelInfo();
         jLabel50.setForeground(Color.black);
     }//GEN-LAST:event_jLabel50MouseExited
 
     private void jLabel49MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel49MouseMoved
         Funciones.setLabelInfo("IMAGENOLOGIA");
         jLabel49.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel49MouseMoved
 
     private void jLabel49MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel49MouseExited
         Funciones.setLabelInfo();
         jLabel49.setForeground(Color.black);
     }//GEN-LAST:event_jLabel49MouseExited
 
     private void jLabel45MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel45MouseMoved
         Funciones.setLabelInfo("LABORATORIO CLINICO");
         jLabel45.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel45MouseMoved
 
     private void jLabel45MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel45MouseExited
         Funciones.setLabelInfo();
         jLabel45.setForeground(Color.black);
     }//GEN-LAST:event_jLabel45MouseExited
 
     private void jLabel36MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel36MouseMoved
         Funciones.setLabelInfo("CONSULTA, MONITORIZACION Y PROCEDIMIENTOS DIAGNOSTICOS");
         jLabel36.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel36MouseMoved
 
     private void jLabel36MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel36MouseExited
         Funciones.setLabelInfo();
         jLabel36.setForeground(Color.black);
     }//GEN-LAST:event_jLabel36MouseExited
 
     private void jLabel51MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel51MouseExited
         Funciones.setLabelInfo();
         jLabel51.setForeground(Color.black);
     }//GEN-LAST:event_jLabel51MouseExited
 
     private void jLabel51MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel51MouseMoved
         Funciones.setLabelInfo("TODOS LOS PROCEDIMIENTOS Y SERVICIOS");
         jLabel51.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel51MouseMoved
 
     private void jLabel46MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel46MouseExited
         Funciones.setLabelInfo();
         jLabel46.setForeground(Color.black);
     }//GEN-LAST:event_jLabel46MouseExited
 
     private void jLabel46MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel46MouseMoved
         Funciones.setLabelInfo("LISTADO DE MEDICAMENTOS");
         jLabel46.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel46MouseMoved
 
     private void jLabel48MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel48MouseExited
         Funciones.setLabelInfo();
         jLabel48.setForeground(Color.black);
     }//GEN-LAST:event_jLabel48MouseExited
 
     private void jLabel48MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel48MouseMoved
         Funciones.setLabelInfo("CIRUGIA GENERAL");
         jLabel48.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel48MouseMoved
 
     private void jLabel52MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel52MouseExited
         Funciones.setLabelInfo();
         jLabel52.setForeground(Color.black);
     }//GEN-LAST:event_jLabel52MouseExited
 
     private void jLabel52MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel52MouseMoved
         Funciones.setLabelInfo("GINECOLOGIA");
         jLabel52.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel52MouseMoved
 
     private void jLabel53MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel53MouseExited
         Funciones.setLabelInfo();
         jLabel53.setForeground(Color.black);
     }//GEN-LAST:event_jLabel53MouseExited
 
     private void jLabel53MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel53MouseMoved
         Funciones.setLabelInfo("MEDICINA INTERNA");
         jLabel53.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel53MouseMoved
 
     private void jLabel54MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel54MouseExited
         Funciones.setLabelInfo();
         jLabel54.setForeground(Color.black);
     }//GEN-LAST:event_jLabel54MouseExited
 
     private void jLabel54MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel54MouseMoved
         Funciones.setLabelInfo("ORTOPEDIA");
         jLabel54.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel54MouseMoved
 
     private void jLabel55MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel55MouseExited
         Funciones.setLabelInfo();
         jLabel55.setForeground(Color.black);
     }//GEN-LAST:event_jLabel55MouseExited
 
     private void jLabel55MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel55MouseMoved
         Funciones.setLabelInfo("PEDIATRIA");
         jLabel55.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel55MouseMoved
 
     private void jLabel56MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel56MouseExited
         Funciones.setLabelInfo();
         jLabel56.setForeground(Color.black);
     }//GEN-LAST:event_jLabel56MouseExited
 
     private void jLabel56MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel56MouseMoved
         Funciones.setLabelInfo("OTRAS ESPECIALIDADES");
         jLabel56.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel56MouseMoved
 
     private void jLabel47MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel47MouseExited
         Funciones.setLabelInfo();
         jLabel47.setForeground(Color.black);
     }//GEN-LAST:event_jLabel47MouseExited
 
     private void jLabel47MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel47MouseMoved
         Funciones.setLabelInfo("MEDIDAS GENERALES");
         jLabel47.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel47MouseMoved
 
     private void jXTaskPane1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXTaskPane1MouseReleased
         jXTaskPane2.setExpanded(false);
         jXTaskPane3.setExpanded(false);
         jXTaskPane4.setExpanded(false);
         jXTaskPane5.setExpanded(false);
     }//GEN-LAST:event_jXTaskPane1MouseReleased
 
     private void jXTaskPane2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXTaskPane2MouseReleased
         jXTaskPane1.setExpanded(false);
         jXTaskPane3.setExpanded(false);
         jXTaskPane4.setExpanded(false);
         jXTaskPane5.setExpanded(false);
     }//GEN-LAST:event_jXTaskPane2MouseReleased
 
     private void jXTaskPane3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXTaskPane3MouseReleased
         jXTaskPane2.setExpanded(false);
         jXTaskPane1.setExpanded(false);
         jXTaskPane4.setExpanded(false);
         jXTaskPane5.setExpanded(false);
     }//GEN-LAST:event_jXTaskPane3MouseReleased
 
     private void jXTaskPane4MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXTaskPane4MouseReleased
         jXTaskPane2.setExpanded(false);
         jXTaskPane3.setExpanded(false);
         jXTaskPane1.setExpanded(false);
         jXTaskPane5.setExpanded(false);
     }//GEN-LAST:event_jXTaskPane4MouseReleased
 
     private void jLabel46MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel46MouseClicked
         if(pMedic==null){
             pMedic = new pTratMedic();
             pMedic.showListExistentes(factory, infohistoriac);
         }
         jPanel35.removeAll();
         pMedic.setBounds(0,0,380,420);
         jPanel35.add(pMedic);
         pMedic.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel46MouseClicked
 
     private void jLabel36MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel36MouseClicked
         if(pConsultDiag==null){
             pConsultDiag = new pTratPConsultDiag();
             pConsultDiag.showListExistentes(factory, infohistoriac);
         }
         jPanel35.removeAll();
         pConsultDiag.setBounds(0,0,380,420);
         jPanel35.add(pConsultDiag);
         pConsultDiag.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
         pConsultDiag.formularioOpen();
     }//GEN-LAST:event_jLabel36MouseClicked
 
     private void jLabel45MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel45MouseClicked
         if(pLaboratorio==null){
             pLaboratorio = new pTratLaboratorio();
             pLaboratorio.showListExistentes(factory, infohistoriac);
         }
         jPanel35.removeAll();
         pLaboratorio.setBounds(0,0,380,420);
         jPanel35.add(pLaboratorio);
         pLaboratorio.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
         pLaboratorio.formularioOpen();
     }//GEN-LAST:event_jLabel45MouseClicked
 
     private void jLabel49MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel49MouseClicked
         if(pImagenologia==null){
             pImagenologia = new pTratImagenologia();
             pImagenologia.showListExistentes(factory, infohistoriac);
         }
         jPanel35.removeAll();
         pImagenologia.setBounds(0,0,380,420);
         jPanel35.add(pImagenologia);
         pImagenologia.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
         pImagenologia.formularioOpen();
     }//GEN-LAST:event_jLabel49MouseClicked
 
     private void jLabel50MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel50MouseClicked
         if(pQuirurgico==null){
             pQuirurgico = new pTratQuirurgico();
             pQuirurgico.showListExistentes(factory, infohistoriac);
         }
         jPanel35.removeAll();
         pQuirurgico.setBounds(0,0,380,420);
         jPanel35.add(pQuirurgico);
         pQuirurgico.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
         pQuirurgico.formularioOpen();
     }//GEN-LAST:event_jLabel50MouseClicked
 
     private void jLabel51MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel51MouseClicked
         if(pProcedimientos==null){
             pProcedimientos = new pTratMasProcedimientos();
             pProcedimientos.showListExistentes(factory, infohistoriac);
         }
         jPanel35.removeAll();
         pProcedimientos.setBounds(0,0,380,420);
         jPanel35.add(pProcedimientos);
         pProcedimientos.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
         pProcedimientos.formularioOpen();
     }//GEN-LAST:event_jLabel51MouseClicked
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         Funciones.setLabelInfo("GUARDANDO...");
         CrearHistoriaC();//guardo en hc
         SaveAntPersonales();
         //guardo en antecedentes
         Funciones.setLabelInfo("DATOS GUARDADOS");
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
         atencionurgencia.AtencionUrgencia.panelindex.jpContainer.removeAll(); 
         atencionurgencia.AtencionUrgencia.panelindex.jpContainer.validate();
         atencionurgencia.AtencionUrgencia.panelindex.jpContainer.repaint();
     }//GEN-LAST:event_jButton9ActionPerformed
 
     private void jLabel48MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel48MouseClicked
         if(pInterconsulta0==null){
             pInterconsulta0 = new pTratInterconsulta(0);//cirugia
             pInterconsulta0.showExistente(infohistoriac);
         }
         jPanel35.removeAll();
         pInterconsulta0.setBounds(0,0,380,420);
         jPanel35.add(pInterconsulta0);
         pInterconsulta0.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel48MouseClicked
 
     private void jLabel52MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel52MouseClicked
         if(pInterconsulta1==null){
             pInterconsulta1 = new pTratInterconsulta(1);//Ginecologia
             pInterconsulta1.showExistente(infohistoriac);
         }
         jPanel35.removeAll();
         pInterconsulta1.setBounds(0,0,380,420);
         jPanel35.add(pInterconsulta1);
         pInterconsulta1.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel52MouseClicked
 
     private void jLabel53MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel53MouseClicked
         if(pInterconsulta2==null){
             pInterconsulta2 = new pTratInterconsulta(2);//Medicina Interna
             pInterconsulta2.showExistente(infohistoriac);
         }
         jPanel35.removeAll();
         pInterconsulta2.setBounds(0,0,380,420);
         jPanel35.add(pInterconsulta2);
         pInterconsulta2.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel53MouseClicked
 
     private void jLabel54MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel54MouseClicked
         if(pInterconsulta3==null){
             pInterconsulta3 = new pTratInterconsulta(3);//Ortopedia
             pInterconsulta3.showExistente(infohistoriac);
         }
         jPanel35.removeAll();
         pInterconsulta3.setBounds(0,0,380,420);
         jPanel35.add(pInterconsulta3);
         pInterconsulta3.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel54MouseClicked
 
     private void jLabel55MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel55MouseClicked
         if(pInterconsulta4==null){
             pInterconsulta4 = new pTratInterconsulta(4);//Pediatria
             pInterconsulta4.showExistente(infohistoriac);
         }
         jPanel35.removeAll();
         pInterconsulta4.setBounds(0,0,380,420);
         jPanel35.add(pInterconsulta4);
         pInterconsulta4.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel55MouseClicked
 
     private void jLabel56MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel56MouseClicked
         if(pOtrasInterconsultas==null){
             pOtrasInterconsultas = new pTratOtrasInterconsultas();
             pOtrasInterconsultas.showLista(infohistoriac);
         }
         jPanel35.removeAll();
         pOtrasInterconsultas.setBounds(0,0,380,420);
         jPanel35.add(pOtrasInterconsultas);
         pOtrasInterconsultas.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel56MouseClicked
 
     private void jLabel47MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel47MouseClicked
         if(pMedidaGeneral==null){
             pMedidaGeneral = new pTratMedidaGeneral();
             pMedidaGeneral.showListExistentes(factory, infohistoriac);
         }
         jPanel35.removeAll();
         pMedidaGeneral.setBounds(0,0,380,420);
         jPanel35.add(pMedidaGeneral);
         pMedidaGeneral.setVisible(true);
         jPanel35.validate();
         jPanel35.repaint();
     }//GEN-LAST:event_jLabel47MouseClicked
 
     private void jLabel22MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MouseMoved
         Funciones.setLabelInfo("MOTIVO DE CONSULTA");
     }//GEN-LAST:event_jLabel22MouseMoved
 
     private void jLabel23MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MouseMoved
         Funciones.setLabelInfo("ANTECEDENTES PERSONALES");
     }//GEN-LAST:event_jLabel23MouseMoved
 
     private void jLabel24MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseMoved
         Funciones.setLabelInfo("ENFERMEDAD ACTUAL");
     }//GEN-LAST:event_jLabel24MouseMoved
 
     private void jLabel25MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel25MouseMoved
         Funciones.setLabelInfo("EXPLORACION FISICA");
     }//GEN-LAST:event_jLabel25MouseMoved
 
     private void jLabel27MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel27MouseMoved
         Funciones.setLabelInfo("PRUEBAS COMPLEMENTARIAS");
     }//GEN-LAST:event_jLabel27MouseMoved
 
     private void jLabel26MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel26MouseMoved
         Funciones.setLabelInfo("DIAGNOSTICO MEDICO");
     }//GEN-LAST:event_jLabel26MouseMoved
 
     private void jLabel33MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel33MouseMoved
         Funciones.setLabelInfo("TRATAMIENTO E INDICACIONES");
     }//GEN-LAST:event_jLabel33MouseMoved
 
     private void jLabel22MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel22MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jLabel22MouseExited
 
     private void jLabel23MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jLabel23MouseExited
 
     private void jLabel24MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel24MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jLabel24MouseExited
 
     private void jLabel25MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel25MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jLabel25MouseExited
 
     private void jLabel27MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel27MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jLabel27MouseExited
 
     private void jLabel26MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel26MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jLabel26MouseExited
 
     private void jLabel33MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel33MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jLabel33MouseExited
 
     private void jLabel58MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel58MouseClicked
 //        if(pAnexo31==null){
 //            pAnexo31 = new pAnexo3();
 //            pAnexo31.showLista(infohistoriac);
 //        }
 //        jPanel35.removeAll();
 //        pAnexo31.setBounds(0,0,380,420);
 //        jPanel35.add(pAnexo31);
 //        pAnexo31.setVisible(true);
 //        jPanel35.validate();
 //        jPanel35.repaint();
     }//GEN-LAST:event_jLabel58MouseClicked
 
     private void jLabel58MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel58MouseExited
         Funciones.setLabelInfo();
         jLabel58.setForeground(Color.black);
     }//GEN-LAST:event_jLabel58MouseExited
 
     private void jLabel58MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel58MouseMoved
         Funciones.setLabelInfo("ANEXO 3");
         jLabel58.setForeground(Color.blue);
     }//GEN-LAST:event_jLabel58MouseMoved
 
     private void jLabel59MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel59MouseClicked
 
     }//GEN-LAST:event_jLabel59MouseClicked
 
     private void jLabel59MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel59MouseExited
         // TODO add your handling code here:
     }//GEN-LAST:event_jLabel59MouseExited
 
     private void jLabel59MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel59MouseMoved
         // TODO add your handling code here:
     }//GEN-LAST:event_jLabel59MouseMoved
 
     private void jXTaskPane5MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXTaskPane5MouseReleased
         jXTaskPane1.setExpanded(false);
         jXTaskPane2.setExpanded(false);
         jXTaskPane3.setExpanded(false);
         jXTaskPane4.setExpanded(false);
     }//GEN-LAST:event_jXTaskPane5MouseReleased
 
     private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
         if(getValidaFirma()){
             Object[] objeto ={"Si","No"};
                 int n = JOptionPane.showOptionDialog(this, "¿Desea Finalizar la Nota de Ingreso?", "Mensaje", JOptionPane.YES_NO_CANCEL_OPTION, 
                         JOptionPane.QUESTION_MESSAGE, null, objeto, objeto[1]);
             if(n==0){
                 finalizar=1;
                 CrearHistoriaC();//guardo en hc
                 SaveAntPersonales();
                 impresionesHC imp = new impresionesHC();
                 imp.setidHC(this.infohistoriac);
                 imp.setdestinoHc("OBSERVACION DE URGENCIAS");
                 imp.setLocationRelativeTo(null);
                 imp.setNoValido(false);
                 imp.setVisible(true);
             }else{
                 finalizar=0;
                 CrearHistoriaC();//guardo en hc
                 SaveAntPersonales();
             }            
         }
     }//GEN-LAST:event_jButton10ActionPerformed
 
     private void jButton1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jButton1MouseExited
 
     private void jButton10MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton10MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jButton10MouseExited
 
     private void jButton9MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton9MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jButton9MouseExited
 
     private void jButton1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseMoved
         Funciones.setLabelInfo("GUARDAR");
     }//GEN-LAST:event_jButton1MouseMoved
 
     private void jButton10MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton10MouseMoved
         Funciones.setLabelInfo("GUARDAR Y FINALIZAR CONSULTA");
     }//GEN-LAST:event_jButton10MouseMoved
 
     private void jButton9MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton9MouseMoved
         Funciones.setLabelInfo("SALIR DE ATENCION DE URGENCIAS");
     }//GEN-LAST:event_jButton9MouseMoved
 
     private void jTextArea11FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea11FocusLost
         if(jTextArea11.getText().isEmpty()){
             jTextArea11.setText("NINGUNO");
         }
     }//GEN-LAST:event_jTextArea11FocusLost
 
     private void jTextArea11FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea11FocusGained
         if("NINGUNO".equals(jTextArea11.getText())){
             jTextArea11.selectAll();
         }
     }//GEN-LAST:event_jTextArea11FocusGained
 
     private void jTextArea12FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea12FocusGained
         if("NINGUNO".equals(jTextArea12.getText())){
             jTextArea12.selectAll();
         }
     }//GEN-LAST:event_jTextArea12FocusGained
 
     private void jTextArea12FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea12FocusLost
         if(jTextArea12.getText().isEmpty()){
             jTextArea12.setText("NINGUNO");
         }
     }//GEN-LAST:event_jTextArea12FocusLost
 
     private void jTextArea22FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea22FocusGained
         if("NINGUNO".equals(jTextArea22.getText())){
             jTextArea22.selectAll();
         }
     }//GEN-LAST:event_jTextArea22FocusGained
 
     private void jTextArea22FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea22FocusLost
         if(jTextArea22.getText().isEmpty()){
             jTextArea22.setText("NINGUNO");
         }
     }//GEN-LAST:event_jTextArea22FocusLost
 
     private void jTextArea23FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea23FocusGained
         if("NINGUNO".equals(jTextArea23.getText())){
             jTextArea23.selectAll();
         }
     }//GEN-LAST:event_jTextArea23FocusGained
 
     private void jTextArea23FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea23FocusLost
         if(jTextArea23.getText().isEmpty()){
             jTextArea23.setText("NINGUNO");
         }
     }//GEN-LAST:event_jTextArea23FocusLost
 
     private void jTextArea24FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea24FocusGained
         if("NINGUNO".equals(jTextArea24.getText())){
             jTextArea24.selectAll();
         }
     }//GEN-LAST:event_jTextArea24FocusGained
 
     private void jTextArea24FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea24FocusLost
         if(jTextArea24.getText().isEmpty()){
             jTextArea24.setText("NINGUNO");
         }
     }//GEN-LAST:event_jTextArea24FocusLost
 
     private void jTextArea25FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea25FocusGained
         if("NINGUNO".equals(jTextArea25.getText())){
             jTextArea25.selectAll();
         }
     }//GEN-LAST:event_jTextArea25FocusGained
 
     private void jTextArea25FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea25FocusLost
         if(jTextArea25.getText().isEmpty()){
             jTextArea25.setText("NINGUNO");
         }
     }//GEN-LAST:event_jTextArea25FocusLost
 
     private void jTextArea4FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea4FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea4.getText())){
             jTextArea4.selectAll();
         }
     }//GEN-LAST:event_jTextArea4FocusGained
 
     private void jTextArea4FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea4FocusLost
         if(jTextArea4.getText().isEmpty()){
             jTextArea4.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea4FocusLost
 
     private void jTextArea6FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea6FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea6.getText())){
             jTextArea6.selectAll();
         }
     }//GEN-LAST:event_jTextArea6FocusGained
 
     private void jTextArea6FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea6FocusLost
         if(jTextArea6.getText().isEmpty()){
             jTextArea6.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea6FocusLost
 
     private void jTextArea8FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea8FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea8.getText())){
             jTextArea8.selectAll();
         }
     }//GEN-LAST:event_jTextArea8FocusGained
 
     private void jTextArea8FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea8FocusLost
         if(jTextArea8.getText().isEmpty()){
             jTextArea8.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea8FocusLost
 
     private void jTextArea9FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea9FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea9.getText())){
             jTextArea9.selectAll();
         }
     }//GEN-LAST:event_jTextArea9FocusGained
 
     private void jTextArea9FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea9FocusLost
         if(jTextArea9.getText().isEmpty()){
             jTextArea9.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea9FocusLost
 
     private void jTextArea14FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea14FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea14.getText())){
             jTextArea14.selectAll();
         }
     }//GEN-LAST:event_jTextArea14FocusGained
 
     private void jTextArea14FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea14FocusLost
         if(jTextArea14.getText().isEmpty()){
             jTextArea14.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea14FocusLost
 
     private void jTextArea15FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea15FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea15.getText())){
             jTextArea15.selectAll();
         }
     }//GEN-LAST:event_jTextArea15FocusGained
 
     private void jTextArea15FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea15FocusLost
         if(jTextArea15.getText().isEmpty()){
             jTextArea15.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea15FocusLost
 
     private void jTextArea16FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea16FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea16.getText())){
             jTextArea16.selectAll();
         }
     }//GEN-LAST:event_jTextArea16FocusGained
 
     private void jTextArea16FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea16FocusLost
         if(jTextArea16.getText().isEmpty()){
             jTextArea16.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea16FocusLost
 
     private void jTextArea17FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea17FocusGained
         if("NO SE ENCUENTRAN DATOS RELEVANTES".equals(jTextArea17.getText())){
             jTextArea17.selectAll();
         }
     }//GEN-LAST:event_jTextArea17FocusGained
 
     private void jTextArea17FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea17FocusLost
         if(jTextArea17.getText().isEmpty()){
             jTextArea17.setText("NO SE ENCUENTRAN DATOS RELEVANTES");
         }
     }//GEN-LAST:event_jTextArea17FocusLost
 
     private void jTextArea2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea2KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea2.transferFocusBackward();
             }else{
                 jTextArea2.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea2KeyPressed
 
     private void jTextArea10KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea10KeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea10.transferFocusBackward();
             }else{
                 jTextArea10.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea10KeyPressed
 
     private void jTextArea11KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea11KeyPressed
          if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea11.transferFocusBackward();
             }else{
                 jTextArea11.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea11KeyPressed
 
     private void jTextArea12KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea12KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea12.transferFocusBackward();
             }else{
                 jTextArea12.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea12KeyPressed
 
     private void jTextArea22KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea22KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea22.transferFocusBackward();
             }else{
                 jTextArea22.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea22KeyPressed
 
     private void jTextArea23KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea23KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea23.transferFocusBackward();
             }else{
                 jTextArea23.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea23KeyPressed
 
     private void jTextArea5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea5KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 jTextArea5.transferFocusBackward();
             }else{
                 jTextArea5.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea5KeyPressed
 
     private void jTextArea7KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea7KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea7.transferFocusBackward();
             }else{
                 jTextArea7.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea7KeyPressed
 
     private void jTextArea24KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea24KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea24.transferFocusBackward();
             }else{
                 jTextArea24.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea24KeyPressed
 
     private void jTextArea25KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea25KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea24.transferFocusBackward();
             }else{
                 jTextArea24.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea25KeyPressed
 
     private void jTextArea9KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea9KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea9.transferFocusBackward();
             }else{
                 jTextArea9.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea9KeyPressed
 
     private void jTextArea14KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea14KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea14.transferFocusBackward();
             }else{
                 jTextArea14.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea14KeyPressed
 
     private void jTextArea15KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea15KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea15.transferFocusBackward();
             }else{
                 jTextArea15.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea15KeyPressed
 
     private void jTextArea16KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea16KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea16.transferFocusBackward();
             }else{
                 jTextArea16.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea16KeyPressed
 
     private void jTextArea17KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea17KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea17.transferFocusBackward();
             }else{
                 jTextArea17.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea17KeyPressed
 
     private void jTextArea3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea3KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea3.transferFocusBackward();
             }else{
                 jTextArea3.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea3KeyPressed
 
     private void jTextArea4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea4KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea4.transferFocusBackward();
             }else{
                 jTextArea4.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea4KeyPressed
 
     private void jTextArea6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea6KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea6.transferFocusBackward();
             }else{
                 jTextArea6.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea6KeyPressed
 
     private void jTextArea8KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea8KeyPressed
         if(evt.getKeyCode() == KeyEvent.VK_TAB){
             if(evt.getModifiers()>0){
                 
                 jTextArea8.transferFocusBackward();
             }else{
                 jTextArea8.transferFocus();
             }
             evt.consume();
         }
     }//GEN-LAST:event_jTextArea8KeyPressed
 
     private void jButton11MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton11MouseExited
         Funciones.setLabelInfo();
     }//GEN-LAST:event_jButton11MouseExited
 
     private void jButton11MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton11MouseMoved
         Funciones.setLabelInfo("IMPRIMIR");
     }//GEN-LAST:event_jButton11MouseMoved
 
     private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
         if(getValidaFirma()){
                 impresionesHC imp = new impresionesHC();
                 imp.setidHC(this.infohistoriac);
                 imp.setdestinoHc("OBSERVACION DE URGENCIAS");
                 imp.setLocationRelativeTo(null);
                 imp.setNoValido(true);
                 imp.setVisible(true);
         }
     }//GEN-LAST:event_jButton11ActionPerformed
 
     // </editor-fold>
     
     // <editor-fold desc="Variables declaration - do not modify"> 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton10;
     private javax.swing.JButton jButton11;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton7;
     private javax.swing.JButton jButton8;
     private javax.swing.JButton jButton9;
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JCheckBox jCheckBox10;
     private javax.swing.JCheckBox jCheckBox11;
     private javax.swing.JCheckBox jCheckBox12;
     private javax.swing.JCheckBox jCheckBox13;
     private javax.swing.JCheckBox jCheckBox2;
     private javax.swing.JCheckBox jCheckBox3;
     private javax.swing.JCheckBox jCheckBox4;
     private javax.swing.JCheckBox jCheckBox5;
     private javax.swing.JCheckBox jCheckBox6;
     private javax.swing.JCheckBox jCheckBox7;
     private javax.swing.JCheckBox jCheckBox8;
     private javax.swing.JCheckBox jCheckBox9;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JFileChooser jFileChooser1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel22;
     private javax.swing.JLabel jLabel23;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel31;
     private javax.swing.JLabel jLabel32;
     private javax.swing.JLabel jLabel33;
     private javax.swing.JLabel jLabel34;
     private javax.swing.JLabel jLabel35;
     private javax.swing.JLabel jLabel36;
     private javax.swing.JLabel jLabel37;
     private javax.swing.JLabel jLabel38;
     private javax.swing.JLabel jLabel39;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel40;
     private javax.swing.JLabel jLabel41;
     private javax.swing.JLabel jLabel42;
     private javax.swing.JLabel jLabel43;
     private javax.swing.JLabel jLabel45;
     private javax.swing.JLabel jLabel46;
     private javax.swing.JLabel jLabel47;
     private javax.swing.JLabel jLabel48;
     private javax.swing.JLabel jLabel49;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel50;
     private javax.swing.JLabel jLabel51;
     private javax.swing.JLabel jLabel52;
     private javax.swing.JLabel jLabel53;
     private javax.swing.JLabel jLabel54;
     private javax.swing.JLabel jLabel55;
     private javax.swing.JLabel jLabel56;
     private javax.swing.JLabel jLabel58;
     private javax.swing.JLabel jLabel59;
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
     private javax.swing.JPanel jPanel27;
     private javax.swing.JPanel jPanel28;
     private javax.swing.JPanel jPanel29;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel31;
     private javax.swing.JPanel jPanel32;
     private javax.swing.JPanel jPanel35;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JRadioButton jRadioButton1;
     private javax.swing.JRadioButton jRadioButton2;
     private javax.swing.JRadioButton jRadioButton3;
     private javax.swing.JRadioButton jRadioButton4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane10;
     private javax.swing.JScrollPane jScrollPane11;
     private javax.swing.JScrollPane jScrollPane12;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane14;
     private javax.swing.JScrollPane jScrollPane15;
     private javax.swing.JScrollPane jScrollPane16;
     private javax.swing.JScrollPane jScrollPane17;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane21;
     private javax.swing.JScrollPane jScrollPane22;
     private javax.swing.JScrollPane jScrollPane23;
     private javax.swing.JScrollPane jScrollPane25;
     private javax.swing.JScrollPane jScrollPane26;
     private javax.swing.JScrollPane jScrollPane27;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTabbedPane jTabbedPane2;
     public javax.swing.JTextArea jTextArea10;
     private javax.swing.JTextArea jTextArea11;
     private javax.swing.JTextArea jTextArea12;
     private javax.swing.JTextArea jTextArea13;
     private javax.swing.JTextArea jTextArea14;
     private javax.swing.JTextArea jTextArea15;
     private javax.swing.JTextArea jTextArea16;
     private javax.swing.JTextArea jTextArea17;
     private javax.swing.JTextArea jTextArea19;
     private javax.swing.JTextArea jTextArea2;
     private javax.swing.JTextArea jTextArea22;
     private javax.swing.JTextArea jTextArea23;
     private javax.swing.JTextArea jTextArea24;
     private javax.swing.JTextArea jTextArea25;
     private javax.swing.JTextArea jTextArea3;
     private javax.swing.JTextArea jTextArea4;
     private javax.swing.JTextArea jTextArea5;
     private javax.swing.JTextArea jTextArea6;
     private javax.swing.JTextArea jTextArea7;
     private javax.swing.JTextArea jTextArea8;
     private javax.swing.JTextArea jTextArea9;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField10;
     public javax.swing.JTextField jTextField11;
     public javax.swing.JTextField jTextField12;
     public javax.swing.JTextField jTextField13;
     public javax.swing.JTextField jTextField14;
     public javax.swing.JTextField jTextField15;
     public javax.swing.JTextField jTextField16;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JTextField jTextField4;
     private javax.swing.JTextField jTextField5;
     private javax.swing.JTextField jTextField6;
     private javax.swing.JTextField jTextField7;
     private javax.swing.JTextField jTextField8;
     private javax.swing.JTextField jTextField9;
     private org.jdesktop.swingx.JXTaskPane jXTaskPane1;
     private org.jdesktop.swingx.JXTaskPane jXTaskPane2;
     private org.jdesktop.swingx.JXTaskPane jXTaskPane3;
     private org.jdesktop.swingx.JXTaskPane jXTaskPane4;
     private org.jdesktop.swingx.JXTaskPane jXTaskPane5;
     public javax.swing.JLabel jlbNombrePaciente;
     private javax.swing.JPanel jpAntPersonales;
     private javax.swing.JPanel jpCentro;
     private javax.swing.JPanel jpDiagMedico;
     private javax.swing.JPanel jpEnfActual;
     private javax.swing.JPanel jpExpFisica;
     private javax.swing.JPanel jpMotivoC;
     private javax.swing.JPanel jpPruebasDiag;
     private javax.swing.JPanel jpTratamiento;
     private javax.swing.JTable jtbTratamiento4;
     // End of variables declaration//GEN-END:variables
 // </editor-fold>
 
 }
