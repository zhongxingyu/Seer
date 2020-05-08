 package br.gov.saudecaruaru.bpai.gui;
 
 import br.gov.saudecaruaru.bpai.business.controller.*;
 import br.gov.saudecaruaru.bpai.business.model.*;
 import br.gov.saudecaruaru.bpai.business.service.SUsuarioDesktop;
 import br.gov.saudecaruaru.bpai.gui.documents.*;
 import br.gov.saudecaruaru.bpai.gui.formatter.CaraterAtendimentoFormatter;
 import br.gov.saudecaruaru.bpai.gui.formatter.DiversasFormatter;
 import br.gov.saudecaruaru.bpai.gui.formatter.EquipeFormatter;
 import br.gov.saudecaruaru.bpai.gui.verifiers.*;
 import br.gov.saudecaruaru.bpai.util.DateUtil;
 import br.gov.saudecaruaru.bpai.util.ModelUtil;
 import br.gov.saudecaruaru.bpai.util.ProcedimentoRealizadoTableModel;
 import br.gov.saudecaruaru.bpai.util.Search;
 import com.towel.swing.combo.ObjectComboBoxModel;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.table.TableColumnModel;
 
 /**
  *
  * @author Junior Pires
  */
 public class CadastroIndividualizado extends javax.swing.JDialog implements TelaCadastroI{
     
      //armazenamento de variáveis para a busca dos dados ficar mais rápida
      public static HashMap<String, Equipe> MAP_EQUIPE= new HashMap<String, Equipe>();
      public static HashMap<Object, Diversas> MAP_DIVERSAS= new HashMap<Object, Diversas>();
      public static HashMap<Object, Paciente> MAP_PACIENTE= new HashMap<Object, Paciente>();
      public static HashMap<Object, Municipio> MAP_MUNICIPIO= new HashMap<Object, Municipio>();
      public static List<String> ARRAY_CBO= new ArrayList<String>();
      
      private Diversas diversas;
      private DiversasPK diversasPk;
      private SUsuarioDesktop sUsuarioDesktop;
      //controladores
      private DiversasController diversasController;
      private MedicoController medicoController;
      private MedicoCboCnesController medicoCboCnesController;
      private PacienteController pacienteController;
      private MunicipioController municipioController;
      private ProcedimentoController procedimentoController;
      private DoencaController doencaController;
      private BIProcedimentoRealizadoController bIProcedimentoRealizadoController;
      private ProcedimentoServicoController procedimentoServicoController;
      private EquipeController equipeController;
      private SProcedimentoRealizadoController sProcedimentoRealizadoController;
      private ProcedimentoRealizadoController procedimentoRealizadoController;
      
      private ProcedimentoRealizado procedimentoRealizado;
      private BIGestorCompetenciaController gestorCompetenciaController;
      
      private ProcedimentoRealizadoTableModel tableModelDados;
      private ObjectComboBoxModel<Diversas> objectComboBoxModelRacaCor;
      private ObjectComboBoxModel<Diversas> objectComboBoxModelServico;
      private ObjectComboBoxModel<Diversas> objectComboBoxModelClassificaoServico;
      private ObjectComboBoxModel<CaraterAtendimento> objectComboBoxModelCaraterAtend;
      private ObjectComboBoxModel<Equipe> objectComboBoxModelEquipe;
      private int sequenciaFolha;
      
      private Set<Paciente> setPaciente;
      private Set<Medico> setMedico;
      private Set<MedicoCboCnes> setMedicoCboCnes;
      
      private  List<Component> listFieldsHeader = new ArrayList<Component>();
      private  List<Component> listFieldsProcedimento = new ArrayList<Component>();
      private  List<Component> listFieldsDates = new ArrayList<Component>();
      
     /**
      * Creates new form CadastroIndividualizado
      */
     public CadastroIndividualizado() {
 
         this.initComponents();
        
         this.myInitComponents();
         
     }
 
     public CadastroIndividualizado(Frame owner) {
         super(owner);
         this.initComponents();
         this.myInitComponents();
         
         
     }
     
     public CadastroIndividualizado(Frame owner, boolean modal) {
         super(owner);
         this.initComponents();
         this.myInitComponents();
         this.setModal(modal);
         
         
     }
     
         /**
      * @return the listFieldsHeader
      */
     public  List<Component> getListFieldsHeader() {
         return listFieldsHeader;
     }
     
     
     private void initInstances(){
         //this.setExtendedState(Frame.MAXIMIZED_BOTH);
         //inicializa as opcoes do JOptionPane
         UIManager.put("OptionPane.yesButtonText", "Sim");     
         UIManager.put("OptionPane.noButtonText", "Não");   
         UIManager.put("OptionPane.cancelButtonText", "Cancelar");
 
         this.objectComboBoxModelRacaCor= new ObjectComboBoxModel<Diversas>();
         this.objectComboBoxModelServico= new ObjectComboBoxModel<Diversas>();
         this.objectComboBoxModelClassificaoServico= new ObjectComboBoxModel<Diversas>();
         this.objectComboBoxModelCaraterAtend= new ObjectComboBoxModel<CaraterAtendimento>();
         this.objectComboBoxModelEquipe= new ObjectComboBoxModel<Equipe>();
         
         DiversasFormatter formatter=new DiversasFormatter();
         this.objectComboBoxModelRacaCor.setFormatter(formatter);
         this.objectComboBoxModelServico.setFormatter(formatter);
         this.objectComboBoxModelClassificaoServico.setFormatter(formatter);
         this.objectComboBoxModelCaraterAtend.setFormatter(new CaraterAtendimentoFormatter());
         this.objectComboBoxModelEquipe.setFormatter(new EquipeFormatter());
         
         this.gestorCompetenciaController = new BIGestorCompetenciaController(); 
         this.diversasController = new DiversasController();
         this.medicoController= new MedicoController();
         this.medicoCboCnesController=new MedicoCboCnesController();
         this.pacienteController=new PacienteController();
         this.municipioController= new MunicipioController();
         this.procedimentoController= new ProcedimentoController();
         this.doencaController=new DoencaController();
         this.bIProcedimentoRealizadoController= new BIProcedimentoRealizadoController();
         this.procedimentoRealizadoController= new ProcedimentoRealizadoController();
         this.sProcedimentoRealizadoController= new SProcedimentoRealizadoController();
         this.procedimentoServicoController= new ProcedimentoServicoController();
         this.equipeController= new EquipeController();
         
         this.setPaciente=new HashSet<Paciente>();
         this.setMedico= new HashSet<Medico>();
         this.setMedicoCboCnes= new HashSet<MedicoCboCnes>();
         
         
         //instancia o modelo DiversasPk
         diversas = new  Diversas();
         diversasPk = new DiversasPK();
         diversas.setDiversasPK(diversasPk);
         this.sUsuarioDesktop= new SUsuarioDesktop("00089076534", "9383748", "cesar", "90834923743287389");
         
         this.procedimentoRealizado= new ProcedimentoRealizado(new ProcedimentoRealizadoPK());
         
         //inicializa a lista com as referencias aos campso do cabeçalho
         this.initListFieldsHeader();
         this.initListFieldsProcedimento();
         this.initListFieldsDates();
     }
     private void initListFieldsHeader(){
        listFieldsHeader= new ArrayList<Component>();
        listFieldsHeader.add(jTextFieldCnes);
        listFieldsHeader.add(jTextFieldCnsProfiss);
        listFieldsHeader.add(jTextFieldNomeProfiss);
        listFieldsHeader.add(jTextFieldCBO);
        listFieldsHeader.add(jTextFieldMes);
        listFieldsHeader.add(jTextFieldAno);
        listFieldsHeader.add(jTextFieldFolha);
 
     }
     
      private void initListFieldsDates(){
        listFieldsDates= new ArrayList<Component>();
        listFieldsDates.add(jTextFieldUsarioDatNasc);
        listFieldsDates.add(jTextFieldProcDataAtend);
      }
     
      private void initListFieldsProcedimento(){
        listFieldsProcedimento= new ArrayList<Component>();
         //seta as referencias dos textsFields na lista
         //LEMBRE-SE QUE A ORDEM É IMPORTANTE PORQUE A VALIDACAO DE UM TEXTFIELD (nos input verifiers)
         //PODEM DEPENDER DO VALOR DE OUTRO
         getListFieldsProcedimento().add(jTextFieldUsuarioNome);
         getListFieldsProcedimento().add(jTextFieldUsarioDatNasc);
         getListFieldsProcedimento().add(jTextFieldUsuarioSexo);
         getListFieldsProcedimento().add(jTextFieldUsuarioCodMunicip);
         getListFieldsProcedimento().add(jTextFieldProcDataAtend);
         getListFieldsProcedimento().add(jTextFieldProcCod);
         getListFieldsProcedimento().add(jTextFieldProcQuant);
        
      }
 
     private void setDocuments() {
         jTextFieldUsuarioCns.setDocument(new OnlyNumbersDocument(15));
         jTextFieldCnes.setDocument(new OnlyNumbersDocument(7));
         jTextFieldMes.setDocument(new OnlyNumbersDocument(2));
         jTextFieldAno.setDocument(new OnlyNumbersDocument(4));
         jTextFieldUsuarioCodEtnia.setDocument(new OnlyNumbersDocument(4));
         jTextFieldUsuarioCodMunicip.setDocument(new OnlyNumbersDocument(6));
         jTextFieldProcNumAut.setDocument(new NumbersUpperLettersDocument(13));
         jTextFieldCBO.setDocument(new NumbersUpperLettersDocument(6));
         jTextFieldFolha.setDocument(new FolhaDocument());
         jTextFieldCnsProfiss.setDocument(new OnlyNumbersDocument(15));
         jTextFieldProcCod.setDocument(new OnlyNumbersDocument(10));
         jTextFieldNomeProfiss.setDocument(new OnlyUpperLettersDocument());
         jTextFieldUsuarioNome.setDocument(new OnlyUpperLettersDocument());
         jTextFieldUsuarioSexo.setDocument(new SexoDocument());
         jTextFieldProcCID.setDocument(new NumbersUpperLettersDocument(4));
     }
     
     private boolean textFieldVerifier(List<Component> listComponents){
      
         for(Component c:listComponents){
             if(c instanceof JTextField){
                 JTextField field = ((JTextField)c);
                 String value = field.getText().replace("/"," ").trim();
                 if(value.isEmpty()){
                      //requisista o foco
                      field.requestFocus();
                      //perde o foco
                      field.transferFocus();
                      return false;
                  
                 }
             }  
 }
       return true;
}
     private void updateJTable(ProcedimentoRealizado pro) {
         try{
          //insere o modelo Procedimento realizado na jTable
             List<BIProcedimentoRealizado> list=this.bIProcedimentoRealizadoController.findAllEqual(new BIProcedimentoRealizado(pro));
             List<ProcedimentoRealizado> lista=this.bIProcedimentoRealizadoController.parserBIProcedimentoRealizadoToProcedimentoRealizado(list);
             this.tableModelDados.replaceAllProcedimentoRealizado(lista);
             this.calcularTamanhoColunasTabela();
      
         }catch(Exception ex){
             ex.printStackTrace();
         }
     }
     
     private void myInitComponents(){
        
         this.initInstances();
         
         //os listerners do Tela
         this.addWindowListener(new java.awt.event.WindowAdapter() {
 
             @Override
             public void windowClosing(java.awt.event.WindowEvent e) {
                 CadastroIndividualizado.this.dispose();
             }
         });
         //quando apertar ESC vai sair
         //pega o elemento root
         JRootPane rootPane = this.getRootPane();
         //pega o map que registra as entradas
         InputMap iMap =	rootPane.getInputMap(	 JComponent.WHEN_IN_FOCUSED_WINDOW);
         
         iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
 
         ActionMap aMap = rootPane.getActionMap();
         //quando clicar em esc vai sair
         aMap.put("escape", new AbstractAction(){
                             public void actionPerformed(ActionEvent e)
                                 {
                                     dispose();
                                 }});
         
         
         this.initJTableDados();
         this.calcularTamanhoColunasTabela();
         
         //caso o objeto pego já possua informacoes, desabilita o cabeçalho
         this.fillFields(this.procedimentoRealizado, true); 
         String seq=ModelUtil.completar(this.sequenciaFolha+"", 2, '0');
         this.jLabelUsuarioSeq.setText(seq);
         this.jLabelProcSeq.setText(seq);
         this.disabledFieldsProcedimento();
         
          this.jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
          this.initComboBoxs();
          //incializa os campos com busca por F2
          this.initKeyPresseds();
          //adicionando listeners aos campos 
          this.addListenersFields();
          //disabilita alguns campos
          this.disableSomeFields();
          //inicializa alguns campos
          this.initFields();
          // Inicializa os validadores dos campos
          this.setVerifiers();
          
          if(this.sequenciaFolha> ProcedimentoRealizado.MAXIMA_QUANTIDADE_SEQUENCIA){
              this.jButtonAtualizar.setEnabled(false);
          }
          
 }
    
     private void disableSomeFields(){
         //desabilita alguns campos do usuario
         jTextFieldUsuarioNomeMunicip.setEnabled(false);
         jTextFieldUsuarioNomeNac.setEnabled(false);
         jTextFieldUsuarioDescEtnia.setEnabled(false);
         
         //desabilita alguns campos do procedimento
         jTextFieldProcDescricao.setEnabled(false);
         jTextFieldProcDescriDoenca.setEnabled(false);
         //desabilitando etnia
         jTextFieldUsuarioCodEtnia.setEnabled(false);
     }
     
     private void initFields(){
         this.setDocuments();
         //inicializando competencia
         String competencia = gestorCompetenciaController.getCompetenciaAtual();
        
         //verifica se existe competencia
         if(!competencia.equals("")){
             jTextFieldMes.setText(competencia.substring(4));
             jTextFieldAno.setText(competencia.substring(0, 4));
         }
         
         jTextFieldUsuarioSexo.setText("");
         //inicializando nacionalidade: BRASIL
         jTextFieldUsuarioCodNac.setText(Diversas.CODIGO_NACIONALIDADE_BRASIL);
         
         
         //inicializando os comboboxs
         this.selectItemJComboBoxRacaCor(Diversas.COD_RACA_COR_SEM_INFORMACAO);
         this.selectItemJComboBoxCaraterAtend(CaraterAtendimento.SEM_INFORMACAO);
         
         
         
        
     }
     
     private void setVerifiers(){
         //atribui validadores
         jTextFieldNomeProfiss.setInputVerifier(new OnlyLettersVerifier(this, "Nome profissional"));
         jTextFieldUsuarioNome.setInputVerifier(new OnlyLettersVerifier(this, "Nome"));
         jTextFieldUsuarioSexo.setInputVerifier(new SexoVerifier(this, "Sexo"));
         jTextFieldUsuarioCns.setInputVerifier(new CnsUsuarioVerifier(this, "CNS Usuário",this));
         jTextFieldCnes.setInputVerifier(new CnesVerifier(this, "CNES"));
         jTextFieldProcQuant.setInputVerifier(new OnlyNumbers(this,"Quantidade"));
         jTextFieldCnsProfiss.setInputVerifier(new CnsVerifier(this,"CNS"));
         jTextFieldCBO.setInputVerifier(new CBOVerifier(this, "CBO",this));
         jTextFieldUsuarioCodNac.setInputVerifier(new NacionalidadeVerifier(this, "Nacionalidade",jTextFieldUsuarioNomeNac));
         jTextFieldUsuarioCodMunicip.setInputVerifier(new MunicipioVerifier(this,"Municipio",jTextFieldUsuarioNomeMunicip));
         jTextFieldUsuarioCodEtnia.setInputVerifier(new EtniaVerifier(this,"Etnia", jTextFieldUsuarioDescEtnia));
         jTextFieldProcCod.setInputVerifier(new ProcedimentoVerifier(this, "Procedimento", jTextFieldProcDescricao,this));
         jTextFieldProcCID.setInputVerifier(new DoencaVerifier(this, "CID", jTextFieldProcDescriDoenca,this));
         jComboBoxProcCaraterAtend.setInputVerifier(new CaraterAtendVerifier(this,"Caráter de Atendimento"));
         jTextFieldUsarioDatNasc.setInputVerifier(new DataVerifier(this, "Data de Nascimento"));
         jTextFieldProcQuant.setInputVerifier(new QuantProcedimentoVerifier(this, "Quantidade",this));
         jTextFieldProcDataAtend.setInputVerifier(new DataAtendimentoVerifier(this, "Data Atendimento",this,jTextFieldUsarioDatNasc));
         jTextFieldAno.setInputVerifier(new CompetenciaVerifier(this,"Ano", jTextFieldMes,this));
         jTextFieldFolha.setInputVerifier(new FolhaVerifier(this, "Folha"));
         jTextFieldMes.setInputVerifier(new MesVerifier(this, "Mês"));
         jComboBoxEquipe.setInputVerifier(new ComboBoxVerifier(this, "Equipe"));
         jComboBoxUsuarioServico.setInputVerifier(new ComboBoxVerifier(this, "Serviço"));
        jComboBoxEquipe.setInputVerifier(new ComboBoxVerifier(this, "Classificação"));
         jTextFieldProcedimentoCnpj.setInputVerifier(new CnpjVerifier(this,"CNPJ"));
     }
     
     
     
     private void initKeyPresseds(){
         //campo do CNES
         this.jTextFieldCnes.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldCnes.transferFocus();
                 }
             }
         });
         
         this.jTextFieldCnsProfiss.addKeyListener(new KeyAdapter() {
              public void keyPressed(java.awt.event.KeyEvent evt) {
                  //o usuário clicou F2
                 if(evt.getKeyCode()==KeyEvent.VK_F1){
                     Search m=CadastroIndividualizado.this.keyPressedJTextFieldCnsProfiss();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         CadastroIndividualizado.this.jTextFieldCnsProfiss.setText(m.getId());
                     }
                 }
                 //teclou "enter"
                 else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldCnsProfiss.transferFocus();
                 }
             }
              
         
         });
         //campo para o nome do profissional
         this.jTextFieldNomeProfiss.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldNomeProfiss.transferFocus();
                 }
             }
         });
          //para CBO do profissional
         this.jTextFieldCBO.addKeyListener(new KeyAdapter() {
              public void keyPressed(java.awt.event.KeyEvent evt) {
                  //o usuário clicou F2
                 if(evt.getKeyCode()==KeyEvent.VK_F1){
                     Search m=CadastroIndividualizado.this.keyPressedJTextFieldCBO();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         CadastroIndividualizado.this.jTextFieldCBO.setText(m.getId());
                     }
                 }
                 if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldCBO.transferFocus();
                 } 
                 
             }
              
         
         });
         
         
         
         //combobox da equipe
         this.jComboBoxEquipe.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jComboBoxEquipe.transferFocus();
                 } 
             }
         });
         //campo mês da competência
         this.jTextFieldMes.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldMes.transferFocus();
                 } 
             }
         });
         //campo ano da competência
         this.jTextFieldAno.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldAno.transferFocus();
                 } 
             }
         });
         
         //folha da competência
         
         this.jTextFieldFolha.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldFolha.transferFocus();
                 } 
             }
         });
         
         //para pacientes/cns do paciente
         this.jTextFieldUsuarioCns.addKeyListener(new KeyAdapter() {
             @Override
              public void keyPressed(java.awt.event.KeyEvent evt) {
                  //o usuário clicou F1
                 if(evt.getKeyCode()==KeyEvent.VK_F1){
                     Search m=CadastroIndividualizado.this.keyPressedJTextFieldUsuarioCns();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         CadastroIndividualizado.this.jTextFieldUsuarioCns.setText(m.getId());
                     }
                 }
                 else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldUsuarioCns.transferFocus();
                 }
                 else if(evt.getKeyCode()==KeyEvent.VK_F5){
                     //CadastroIndividualizado.this.jTextFieldUsuarioCns.transferFocus();
                     CadastroIndividualizado.this.jTextFieldUsuarioCns.setText(Paciente.DADOS_CONSOLIDADOS.getCns());
                     MAP_PACIENTE.put(Paciente.DADOS_CONSOLIDADOS.getCns(), Paciente.DADOS_CONSOLIDADOS);
                 }
             }
              
         
         });
         
         
         //campo nome do paciente/usuário
         this.jTextFieldUsuarioNome.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldUsuarioNome.transferFocus();
                 } 
             }
         });
         
         //campo sexo do paciente/usuário
         this.jTextFieldUsuarioSexo.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldUsuarioSexo.transferFocus();
                 } 
             }
         });
         
         //campo data de nascimento do paciente/usuário
         this.jTextFieldUsarioDatNasc.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldUsarioDatNasc.transferFocus();
                 } 
             }
         });
         
         //para município
         this.jTextFieldUsuarioCodMunicip.addKeyListener(new KeyAdapter() {
              public void keyPressed(java.awt.event.KeyEvent evt) {
                  //o usuário clicou F2
                 if(evt.getKeyCode()==KeyEvent.VK_F1){
                     Search m=CadastroIndividualizado.this.keyPressedJTextFieldUsuarioMunicip();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         //26 é referente ao estado de pernambuco
                         CadastroIndividualizado.this.jTextFieldUsuarioCodMunicip.setText("26"+m.getId());
                         CadastroIndividualizado.this.jTextFieldUsuarioNomeMunicip.setText(m.getDescription());
                     }
                 }
                 
                 if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldUsuarioCodMunicip.transferFocus();
                 } 
             }
              
         
         });
         
         //para município
         this.jTextFieldUsuarioCodNac.addKeyListener(new KeyAdapter() {
              public void keyPressed(java.awt.event.KeyEvent evt) {
                  //o usuário clicou F2
                 if(evt.getKeyCode()==KeyEvent.VK_F1){
                     Search m=CadastroIndividualizado.this.keyPressedJTextFieldUsuarioCodNac();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         //26 é referente ao estado de pernambuco
                         CadastroIndividualizado.this.jTextFieldUsuarioCodNac.setText(m.getId());
                         CadastroIndividualizado.this.jTextFieldUsuarioNomeNac.setText(m.getDescription());
                     }
                 }
                 if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldUsuarioCodNac.transferFocus();
                 } 
             }
              
         
         });
         //combobox raça/cor
         this.jComboBoxUsuarioRacaCor.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                         int index = ((JComboBox)e.getComponent()).getSelectedIndex();
                        Diversas d = (Diversas) objectComboBoxModelRacaCor.getData().get(index);
                        CadastroIndividualizado.this.procedimentoRealizado.setRacaPaciente(d.getDiversasPK().getCodigoItemTabela().toString());
                        if(d.getDiversasPK().getCodigoItemTabela().trim().equals(Diversas.COD_RACA_COR_INDIGENA)){
                             CadastroIndividualizado.this.jTextFieldUsuarioCodEtnia.setEnabled(true);
                        }else{
                             CadastroIndividualizado.this.jTextFieldUsuarioCodEtnia.setEnabled(false);
                        }
                        CadastroIndividualizado.this.jComboBoxUsuarioRacaCor.transferFocus();
                 } 
             }
         });
         
         //campo código da etnia
         this.jTextFieldUsuarioCodEtnia.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                   //o usuário clicou F1
                 if(e.getKeyCode()==KeyEvent.VK_F1){
                     Search m=CadastroIndividualizado.this.keyPressedJTextFieldEtnia();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         CadastroIndividualizado.this.jTextFieldUsuarioCodEtnia.setText(m.getId());
                     }
                 }
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldUsuarioCodEtnia.transferFocus();
                 } 
             }
         });
         
         //campo data de atendimento
         this.jTextFieldProcDataAtend.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldProcDataAtend.transferFocus();
                 } 
             }
         });
         
         //para procedimento realizado
         this.jTextFieldProcCod.addKeyListener(new KeyAdapter() {
              public void keyPressed(java.awt.event.KeyEvent evt) {
                  //o usuário clicou F2
                 if(evt.getKeyCode()==KeyEvent.VK_F1){
                     Procedimento m=CadastroIndividualizado.this.keyPressedJTextFieldProcCod();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         //26 é referente ao estado de pernambuco
                         CadastroIndividualizado.this.jTextFieldProcCod.setText(m.getProcedimentoPk().getId()+m.getDigitoVerificador());
                         CadastroIndividualizado.this.jTextFieldProcDescricao.setText(m.getDescricao());
                     }
                 }
                 if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldProcCod.transferFocus();
                 } 
             }
              
         
         });
         
         //campo quantidade de vezes que o procedimento foi executado
         this.jTextFieldProcQuant.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldProcQuant.transferFocus();
                 } 
             }
         });
         
         //comboxo de serviço
         this.jComboBoxUsuarioServico.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jComboBoxUsuarioServico.transferFocus();
                 } 
             }
         });
         
         //campo quantidade de vezes que o procedimento foi executado
         this.jComboBoxUsuarioClassificacao.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jComboBoxUsuarioClassificacao.transferFocus();
                 } 
             }
         });
          this.jTextFieldProcedimentoCnpj.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldProcedimentoCnpj.transferFocus();
                 }
             }
         });
         //para procedimento realizado
         this.jTextFieldProcCID.addKeyListener(new KeyAdapter() {
              public void keyPressed(java.awt.event.KeyEvent evt) {
                  //o usuário clicou F2
                 if(evt.getKeyCode()==KeyEvent.VK_F1){
                     Search m=CadastroIndividualizado.this.keyPressedJTextFieldProcCID();
                     //o usuário selecionou um registro
                     if(m!=null){
                         //vai setor o valor do código no campo
                         //26 é referente ao estado de pernambuco
                         CadastroIndividualizado.this.jTextFieldProcCID.setText(m.getId());
                         CadastroIndividualizado.this.jTextFieldProcDescriDoenca.setText(m.getDescription());
                     }
                 }
                 
                 if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                     
                     CadastroIndividualizado.this.jTextFieldProcCID.transferFocus();
                     
                 }
             }
              
         
         });
         
         //combobox caráter de atendimento
         this.jComboBoxProcCaraterAtend.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jComboBoxProcCaraterAtend.transferFocus();
                 } 
             }
         });
         
         //campo número de autorização
         this.jTextFieldProcNumAut.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if(e.getKeyCode()==KeyEvent.VK_ENTER){
                     CadastroIndividualizado.this.jTextFieldProcNumAut.transferFocus();
                 } 
             }
         });
     }
     
     private void initJTableDados(){
        //cria a lista para armazenar 20 procedimentos 
        List<ProcedimentoRealizado> list= new ArrayList<ProcedimentoRealizado>();
        //cria um modelo para a tabela
        this.tableModelDados= new ProcedimentoRealizadoTableModel(list);
        this.jTable1.setModel(this.tableModelDados);
        
        this.gerarSequencia();
     }
     
     private void calcularTamanhoColunasTabela(){
         
        //incia o tamanho padrão da tabela
        TableColumnModel columns=this.jTable1.getColumnModel();
        
        //coluna sequência
        columns.getColumn(0).setPreferredWidth(40);
        //coluna cns do paciente
        columns.getColumn(1).setPreferredWidth(120);
        //coluna nome do paciente
        columns.getColumn(2).setPreferredWidth(300);
        //coluna data de nascimento do paciente
        columns.getColumn(3).setPreferredWidth(70);
        //coluna sexo do paciente
        columns.getColumn(4).setPreferredWidth(40);
        //coluna município de residência do paciente
        columns.getColumn(5).setPreferredWidth(120);
        //coluna data de atendimento
        columns.getColumn(6).setPreferredWidth(100);
        //coluna procedimento
        columns.getColumn(7).setPreferredWidth(100);
        //coluna quantidade de realizações do procedimento no paciente
        columns.getColumn(8).setPreferredWidth(40);
        //cid do procedimento
        columns.getColumn(9).setPreferredWidth(40);
        //coluna caraterização do atendimento 
        columns.getColumn(10).setPreferredWidth(80);
        //coluna número de autorização
        columns.getColumn(11).setPreferredWidth(100);
        //coluna raça/cor
        columns.getColumn(12).setPreferredWidth(80);
        //coluna etnia
        columns.getColumn(13).setPreferredWidth(80);
        //coluna nacionalidade do paciente
        columns.getColumn(14).setPreferredWidth(80);
     }
     
     
     public void gerarSequencia(){
        //pega o tamanho da lista na tabela
        int size=this.tableModelDados.getRowCount();
        if(size<ProcedimentoRealizado.MAXIMA_QUANTIDADE_SEQUENCIA){
         this.sequenciaFolha=size+1;
        }
        else{
            this.sequenciaFolha=0;
        }
        
        String seq=ModelUtil.completar(this.sequenciaFolha+"", 2,'0');
        this.jLabelUsuarioSeq.setText(seq);
        this.jLabelProcSeq.setText(seq);
     }
     //todos os keyPressed
     
     /**
      * Método que realiza a busca de um médico
      * @return Search objeto de pesquisa com um identificador e uma descrição
      */
     private Search keyPressedJTextFieldCnsProfiss(){
         return SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.medicoController, "cns", "nome","CNS", "Nome",new HashMap<String, Object>());
     }
     
      /**
      * Método que realiza a busca um CBO
      * @return Search objeto de pesquisa com um identificador e uma descrição
      */
     private Search keyPressedJTextFieldCBO(){
         //restrição para qualquer busca
         HashMap<String, Object> res=new HashMap<String, Object>();
         res.put("diversasPK.codigoTabela", Diversas.TABELA_PROFISSAO);
         return SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.diversasController, 
                                                         "diversasPK.codigoItemTabela", "descricaoItemTabela",
                                                         "Código", "Profissão",res);
     }
     
     /**
      * Método que realiza uma busca de usuários/pacientes
      * @return Search objeto de pesquisa com um identificador e uma descrição
      */
     private Search keyPressedJTextFieldUsuarioCns(){
         
         return SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.pacienteController, "cns", "nome","CNS", "Nome",new HashMap<String, Object>());
     }
     
     private Search keyPressedJTextFieldEtnia(){
         
       //restrição para qualquer busca
         HashMap<String, Object> res=new HashMap<String, Object>();
         res.put("diversasPK.codigoTabela", Diversas.TABELA_ETNIA);
         return SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.diversasController, 
                                                         "diversasPK.codigoItemTabela", "descricaoItemTabela",
                                                         "Código", "Etnia",res);
     }
     
     /**
      * Método que realiza a busca de um município
      * @return Search objeto de pesquisa com um identificador e uma descrição
      */
     private Search keyPressedJTextFieldUsuarioMunicip(){
         HashMap<String, Object> res=new HashMap<String, Object>();
         //pernambuco
         res.put("municipioPK.uf", "26");
         return SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.municipioController, 
                                                             "municipioPK.codigoMunicipio", "nome",
                                                             "Código", "Nome", res);
     }
     
    /**
      * Método que realiza a busca da nacionalidade
      * @return Search objeto de pesquisa com um identificador e uma descrição
      */
     private Search keyPressedJTextFieldUsuarioCodNac(){
         //restrição para qualquer busca
         HashMap<String, Object> res=new HashMap<String, Object>();
         res.put("diversasPK.codigoTabela", Diversas.TABELA_PAIS);
         return SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.diversasController, 
                                                         "diversasPK.codigoItemTabela", "descricaoItemTabela",
                                                         "Código", "Nacionalidade",res);
     }
     
     /**
      * Método que realiza a busca da nacionalidade
      * @return Search objeto de pesquisa com um identificador e uma descrição
      */
     private Procedimento keyPressedJTextFieldProcCod(){
         //restrição para qualquer busca
         HashMap<String, Object> res=new HashMap<String, Object>();
         //vai filtrar os procedimentos pela competencia mais recente
         res.put("procedimentoPk.competencia", ModelUtil.COMPETENCIA_MAIS_RECENTE);
         Search s=SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.procedimentoController, 
                                                         "procedimentoPk.id", "descricao",
                                                         "Código", "Descrição",res);
         //  o usuário escolheu um procedimento
         if(s!=null){
             res.put("procedimentoPk.id",s.getId() );
             Procedimento p=this.procedimentoController.findEqual(res);
             return  p;
         }
         return null;
     }
     
     /**
      * Método que realiza a busca da nacionalidade
      * @return Search objeto de pesquisa com um identificador e uma descrição
      */
     private Search keyPressedJTextFieldProcCID(){
         //restrição para qualquer busca
         HashMap<String, Object> res=new HashMap<String, Object>();
         return SearchGeneric.getInstance().initModeSearch(CadastroIndividualizado.this.doencaController, 
                                                         "codigo", "descricao",
                                                         "Código", "Descrição",res);
     }
     
     
         
    
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jTextFieldNomeProfiss = new javax.swing.JTextField();
         jLabel7 = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         jLabel9 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabelUsuarioSeq = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jTextFieldUsuarioNome = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         jTextFieldUsuarioNomeNac = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         jTextFieldUsuarioNomeMunicip = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         jTextFieldUsuarioDescEtnia = new javax.swing.JTextField();
         jComboBoxUsuarioRacaCor = new javax.swing.JComboBox();
         jLabel17 = new javax.swing.JLabel();
         jTextFieldUsarioDatNasc = new javax.swing.JFormattedTextField();
         jTextFieldUsuarioCns = new javax.swing.JTextField();
         jTextFieldUsuarioSexo = new javax.swing.JTextField();
         jTextFieldUsuarioCodMunicip = new javax.swing.JTextField();
         jTextFieldUsuarioCodNac = new javax.swing.JTextField();
         jTextFieldUsuarioCodEtnia = new javax.swing.JTextField();
         jPanel2 = new javax.swing.JPanel();
         jLabel16 = new javax.swing.JLabel();
         jLabelProcSeq = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         jTextFieldProcQuant = new javax.swing.JTextField();
         jLabel22 = new javax.swing.JLabel();
         jTextFieldProcDescriDoenca = new javax.swing.JTextField();
         jLabel23 = new javax.swing.JLabel();
         jLabel24 = new javax.swing.JLabel();
         jTextFieldProcDescricao = new javax.swing.JTextField();
         jComboBoxProcCaraterAtend = new javax.swing.JComboBox();
         jLabel19 = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jButtonIncluir = new javax.swing.JButton();
         jButtonLimpar = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jTextFieldProcDataAtend = new javax.swing.JFormattedTextField();
         jButtonSair = new javax.swing.JButton();
         jLabel26 = new javax.swing.JLabel();
         jComboBoxUsuarioServico = new javax.swing.JComboBox();
         jLabel27 = new javax.swing.JLabel();
         jComboBoxUsuarioClassificacao = new javax.swing.JComboBox();
         jButtonAtualizar = new javax.swing.JButton();
         jButtonCancelar = new javax.swing.JButton();
         jTextFieldProcCod = new javax.swing.JTextField();
         jTextFieldProcCID = new javax.swing.JTextField();
         jTextFieldProcNumAut = new javax.swing.JTextField();
         jLabel30 = new javax.swing.JLabel();
         jTextFieldProcedimentoCnpj = new javax.swing.JFormattedTextField();
         jSeparator2 = new javax.swing.JSeparator();
         jLabel21 = new javax.swing.JLabel();
         jComboBoxEquipe = new javax.swing.JComboBox();
         jLabel25 = new javax.swing.JLabel();
         jTextFieldCnsProfiss = new javax.swing.JTextField();
         jLabel28 = new javax.swing.JLabel();
         jTextFieldFolha = new javax.swing.JTextField();
         jTextFieldCnes = new javax.swing.JTextField();
         jTextFieldMes = new javax.swing.JTextField();
         jTextFieldAno = new javax.swing.JTextField();
         jTextFieldCBO = new javax.swing.JTextField();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setName("Cadastro indivualizado"); // NOI18N
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel1.setText("CNES");
 
         jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel2.setText("CNS Profissional");
 
         jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel3.setText("Nome Profissional");
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         jLabel5.setText("Mês   /  Ano");
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         jLabel6.setText("Folha");
 
         jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLabel7.setText(" /");
 
         jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
 
         jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel9.setText("CNS");
 
         jLabel8.setText("Usuário Sequência :");
 
         jLabelUsuarioSeq.setText("01");
 
         jLabel10.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel10.setText("Nome ");
 
         jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel4.setText("Sexo");
 
         jLabel11.setBackground(new java.awt.Color(153, 153, 153));
         jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel11.setText(" F/M");
         jLabel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel12.setText("Dt. Nascimento");
 
         jTextFieldUsuarioNomeNac.setBackground(new java.awt.Color(153, 153, 153));
 
         jLabel14.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel14.setText("Nacionalidade");
 
         jTextFieldUsuarioNomeMunicip.setBackground(new java.awt.Color(153, 153, 153));
 
         jLabel13.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel13.setText("Município de Residência");
 
         jLabel15.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel15.setText("Raça/Cor");
 
         jTextFieldUsuarioDescEtnia.setBackground(new java.awt.Color(153, 153, 153));
 
         jComboBoxUsuarioRacaCor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         jLabel17.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel17.setText("Etnia");
 
         try {
             jTextFieldUsarioDatNasc.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
         } catch (java.text.ParseException ex) {
             ex.printStackTrace();
         }
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel8)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabelUsuarioSeq, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jComboBoxUsuarioRacaCor, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jTextFieldUsuarioCodEtnia, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextFieldUsuarioDescEtnia, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                 .addComponent(jTextFieldUsuarioCns, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(140, 140, 140)))
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextFieldUsuarioNome, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                             .addComponent(jTextFieldUsuarioSexo))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jTextFieldUsarioDatNasc, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jTextFieldUsuarioCodMunicip, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTextFieldUsuarioNomeMunicip, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jTextFieldUsuarioCodNac, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jTextFieldUsuarioNomeNac, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel8)
                             .addComponent(jLabelUsuarioSeq))
                         .addGap(5, 5, 5)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addComponent(jLabel9))
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(jLabel4)
                                 .addGap(1, 1, 1)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jTextFieldUsuarioNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jTextFieldUsuarioCns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jTextFieldUsuarioSexo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel12)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel11)
                             .addComponent(jTextFieldUsarioDatNasc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel13)
                     .addComponent(jLabel14))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextFieldUsuarioNomeMunicip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextFieldUsuarioNomeNac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextFieldUsuarioCodMunicip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextFieldUsuarioCodNac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel15)
                     .addComponent(jLabel17))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextFieldUsuarioDescEtnia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBoxUsuarioRacaCor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextFieldUsuarioCodEtnia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
 
         jLabel16.setText("Procedimento Sequência :");
 
         jLabelProcSeq.setText("01");
 
         jLabel20.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel20.setText("Dt. Atendimento");
 
         jLabel22.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel22.setText("Quantidade");
 
         jTextFieldProcDescriDoenca.setBackground(new java.awt.Color(153, 153, 153));
 
         jLabel23.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel23.setText("Código");
 
         jLabel24.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel24.setText("CID");
 
         jTextFieldProcDescricao.setBackground(new java.awt.Color(153, 153, 153));
 
         jComboBoxProcCaraterAtend.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         jLabel19.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel19.setText("Nº Autorização");
 
         jLabel18.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel18.setText("Caráter Atendimento");
 
         jButtonIncluir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jButtonIncluir.setText("Incluir");
         jButtonIncluir.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButtonIncluirMouseClicked(evt);
             }
         });
         jButtonIncluir.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jButtonIncluirKeyPressed(evt);
             }
         });
 
         jButtonLimpar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jButtonLimpar.setText("Limpar");
         jButtonLimpar.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButtonLimparMouseClicked(evt);
             }
         });
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jTable1MouseClicked(evt);
             }
         });
         jScrollPane1.setViewportView(jTable1);
 
         try {
             jTextFieldProcDataAtend.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
         } catch (java.text.ParseException ex) {
             ex.printStackTrace();
         }
 
         jButtonSair.setText("Sair");
         jButtonSair.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButtonSairMouseClicked(evt);
             }
         });
 
         jLabel26.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel26.setText("Serviço");
 
         jComboBoxUsuarioServico.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         jLabel27.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel27.setText("Classificação");
 
         jComboBoxUsuarioClassificacao.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         jButtonAtualizar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jButtonAtualizar.setText("Atualizar");
         jButtonAtualizar.setToolTipText("");
         jButtonAtualizar.setEnabled(false);
         jButtonAtualizar.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButtonAtualizarMouseClicked(evt);
             }
         });
         jButtonAtualizar.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 jButtonAtualizarKeyPressed(evt);
             }
         });
 
         jButtonCancelar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jButtonCancelar.setText("Cancelar");
         jButtonCancelar.setEnabled(false);
         jButtonCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButtonCancelarMouseClicked(evt);
             }
         });
 
         jLabel30.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel30.setText("CNPJ");
 
         try {
             jTextFieldProcedimentoCnpj.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###/####-##")));
         } catch (java.text.ParseException ex) {
             ex.printStackTrace();
         }
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1901, Short.MAX_VALUE)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                 .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(174, 174, 174)
                                 .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                 .addComponent(jLabel16)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jLabelProcSeq, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(jPanel2Layout.createSequentialGroup()
                                 .addComponent(jComboBoxUsuarioServico, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jComboBoxUsuarioClassificacao, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel20)
                                     .addComponent(jTextFieldProcDataAtend, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addGroup(jPanel2Layout.createSequentialGroup()
                                         .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addGap(347, 347, 347))
                                     .addGroup(jPanel2Layout.createSequentialGroup()
                                         .addComponent(jTextFieldProcCod)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addComponent(jTextFieldProcDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                     .addComponent(jTextFieldProcQuant)))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jTextFieldProcedimentoCnpj, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel24)
                                     .addGroup(jPanel2Layout.createSequentialGroup()
                                         .addComponent(jTextFieldProcCID, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jTextFieldProcDescriDoenca, javax.swing.GroupLayout.PREFERRED_SIZE, 428, javax.swing.GroupLayout.PREFERRED_SIZE))))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jComboBoxProcCaraterAtend, javax.swing.GroupLayout.PREFERRED_SIZE, 549, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldProcNumAut))))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButtonIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButtonAtualizar)
                         .addGap(27, 27, 27)
                         .addComponent(jButtonCancelar)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButtonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jButtonSair, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                         .addGroup(jPanel2Layout.createSequentialGroup()
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jLabel16)
                                 .addComponent(jLabelProcSeq))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addGroup(jPanel2Layout.createSequentialGroup()
                                     .addComponent(jLabel22)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jTextFieldProcQuant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGroup(jPanel2Layout.createSequentialGroup()
                                     .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                         .addComponent(jLabel20)
                                         .addComponent(jLabel23))
                                     .addGap(26, 26, 26))
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jTextFieldProcDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldProcDataAtend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldProcCod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addGap(1, 1, 1)
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addGroup(jPanel2Layout.createSequentialGroup()
                                     .addComponent(jLabel27)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                         .addComponent(jComboBoxUsuarioClassificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addComponent(jComboBoxUsuarioServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                 .addGroup(jPanel2Layout.createSequentialGroup()
                                     .addComponent(jLabel26)
                                     .addGap(26, 26, 26)))
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addGroup(jPanel2Layout.createSequentialGroup()
                                     .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                         .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGap(26, 26, 26))
                                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jTextFieldProcCID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldProcedimentoCnpj, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldProcDescriDoenca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jLabel18)
                                 .addComponent(jLabel19))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jComboBoxProcCaraterAtend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jTextFieldProcNumAut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGap(0, 32, Short.MAX_VALUE))
                         .addGroup(jPanel2Layout.createSequentialGroup()
                             .addGap(192, 192, 192)
                             .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jButtonLimpar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jButtonIncluir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jButtonAtualizar)
                                 .addComponent(jButtonCancelar))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButtonSair, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jLabel21.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         jLabel21.setText("CBO");
 
         jComboBoxEquipe.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         jLabel25.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel25.setText("Equipe");
 
         jLabel28.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jLabel28.setText("F5 - DADOS CONSOLIDADOS (SOMENTE PARA O CAMPO CNS DO PACIENTE)");
 
         jTextFieldCBO.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         jTextFieldCBO.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextFieldCBOActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                         .addContainerGap())
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jComboBoxEquipe, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGap(10, 10, 10)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jTextFieldMes, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jLabel7)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jTextFieldAno, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addComponent(jLabel5))
                                 .addGap(10, 10, 10))
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldCnes, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldCnsProfiss, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jTextFieldNomeProfiss, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))))
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jTextFieldCBO, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 607, javax.swing.GroupLayout.PREFERRED_SIZE))))
                             .addComponent(jLabel6)
                             .addComponent(jTextFieldFolha, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(631, 631, 631))))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel1)
                         .addComponent(jLabel2))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel3)
                         .addComponent(jLabel21)))
                 .addGap(5, 5, 5)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextFieldNomeProfiss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextFieldCnsProfiss, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextFieldCnes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextFieldCBO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel28))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jComboBoxEquipe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel7)
                             .addComponent(jTextFieldAno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jTextFieldMes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel6)
                             .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextFieldFolha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButtonIncluirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonIncluirMouseClicked
         if(jButtonIncluir.isEnabled()){
         try{
             if(this.textFieldVerifier(getListFieldsProcedimento())){
 
                 // metodo que pega os valores dos campos e adiciona-os ao modelo
                 this.getValuesToModel();
                 String errors = procedimentoRealizadoController.validateProcedimento(procedimentoRealizado);
                 if(!errors.equals("")){
                     JOptionPane.showMessageDialog(this,errors);
                 }else{
                 
                 int itensFolha=this.tableModelDados.getRowCount();
                 if(itensFolha<ProcedimentoRealizado.MAXIMA_QUANTIDADE_SEQUENCIA){
                     
                      //se não houver elemento vazio siginifica que a operação é de edição
                      //nesse caso salva o objeto;
                     this.procedimentoRealizado.getProcedimentoRealizadoPK().setSequenciaFolha(ModelUtil.completar(""+this.sequenciaFolha, 2, '0'));
                     this.procedimentoRealizado.preencherAtributosVazios();
                     if(this.bIProcedimentoRealizadoController.merge(new BIProcedimentoRealizado(this.procedimentoRealizado))!=null){
                         this.sProcedimentoRealizadoController.enviarSProcedimentoRealizado(this.procedimentoRealizado.getProcedimentoRealizadoParaEnviar(), this.sUsuarioDesktop);
                         //salva o paciente, o médico e o médico com CBO e CNS
                         Paciente p=this.procedimentoRealizado.getPaciente();
                         if( p.getCns() == null ? true : !p.getCns().trim().isEmpty() ){
                             this.pacienteController.merge(p);
                         }
                         this.medicoCboCnesController.merge(this.procedimentoRealizado.getMedicoCboCnes());
                         this.medicoController.merge(this.procedimentoRealizado.getMedico());
                         //pega uma nova referencia para procedimentoRealizado já com cabeçalho
                         this.procedimentoRealizado=this.procedimentoRealizado.getOnlyHeader();
                         this.updateJTable( this.procedimentoRealizado);
                         this.fillFields(this.procedimentoRealizado, true);
                         clearModelsServicoClassificacao();
                         initComboBoxs();
                         //gera a sequência
                         this.gerarSequencia();
                         //a folha foi preenchida
                         itensFolha=this.tableModelDados.getRowCount();
                         if(itensFolha>=ProcedimentoRealizado.MAXIMA_QUANTIDADE_SEQUENCIA){
                             this.initNewFolha();
                         }
                     }
                     else{
                     System.out.println("Deu merda!");
                     }
                 }
                 else{
                     JOptionPane.showMessageDialog(this, "A folha já está completa!\nUma nova folha será gerada.");
                 }
             }
             }
         }catch(Exception ex){
             ex.printStackTrace();
              JOptionPane.showMessageDialog(this, "Ops! Um erro inesperado aconteceu! Relate o problema  aos desenvolvedores!");
         }
       }
     }//GEN-LAST:event_jButtonIncluirMouseClicked
 
     private void initNewFolha(){
         //pega o primeiro objeto da jTable e atribui ao modelo atual
         int opcao=JOptionPane.showOptionDialog(this,"DESEJA INICIAR A INCLUSÃO COM O MESMO CABEÇALHO?","Questão",
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
         if(opcao==JOptionPane.YES_OPTION){
              //pega o valor do campo folha e converte para inteiro
              int folha = Integer.parseInt(jTextFieldFolha.getText());
              //incrementa
              ++folha;
              //converte de volta para String recolocando os zeros a esquerda se necessário com o metodo format
              String f = String.format("%03d",folha);  
              //seta o novo valor de folha no campo folha
              this.procedimentoRealizado.getProcedimentoRealizadoPK().setNumeroFolha(f);
              this.fillHeaderModelProcedimentoRealizado(this.procedimentoRealizado);
         }else{
            //preenche todos os campos  
            this.procedimentoRealizado=new ProcedimentoRealizado(new ProcedimentoRealizadoPK());
            //habilita os campos do cabeçalho
            this.enableFieldsHeader();
         }
         
         //inicia a jTable
         this.initJTableDados();
         this.fillFields(this.procedimentoRealizado, true);
         this.gerarSequencia();
     }
    
     private void insertOrUpdateState(){
         if(this.tableModelDados.getRowCount()>=ProcedimentoRealizado.MAXIMA_QUANTIDADE_SEQUENCIA){
             this.disableFieldsHeader();
             this.disabledFieldsProcedimento();
             this.jButtonIncluir.setEnabled(false);
         }
         else{
             this.enableFieldsProcedimento();
             this.jButtonIncluir.setEnabled(true);
         }
     }
     
     private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
         if(this.jTable1.getSelectedRow()>=0){
             //preenche os campos
             ProcedimentoRealizado p=this.tableModelDados.getProcedimentoRealizado(this.jTable1.getSelectedRow());
             this.procedimentoRealizado=new ProcedimentoRealizado(p);
             this.fillFields(procedimentoRealizado, true);
             //abilitar e desabilitar campos
             this.disableFieldsHeader();
             this.enableFieldsProcedimento();
             
             //desabilita os botoes para editar
             this.jButtonCancelar.setEnabled(true);
             this.jButtonIncluir.setEnabled(false);
             this.jButtonAtualizar.setEnabled(true);
         }
     }//GEN-LAST:event_jTable1MouseClicked
 
     private void jButtonLimparMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonLimparMouseClicked
        this.clearFields();
     }//GEN-LAST:event_jButtonLimparMouseClicked
 
     private void jButtonSairMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonSairMouseClicked
         // TODO add your handling code here:
         this.dispose();
     }//GEN-LAST:event_jButtonSairMouseClicked
 
     private void jButtonIncluirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButtonIncluirKeyPressed
            if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                this.jButtonIncluirMouseClicked(null);
            }
     }//GEN-LAST:event_jButtonIncluirKeyPressed
 
     private void teste(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_teste
         // TODO add your handling code here:
     }//GEN-LAST:event_teste
 
     private void jButtonCancelarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonCancelarMouseClicked
         // TODO add your handling code here:
         this.jTable1.getSelectionModel().clearSelection();
         this.jButtonCancelar.setEnabled(false);
         this.jButtonAtualizar.setEnabled(false);
         this.procedimentoRealizado=new ProcedimentoRealizado(this.procedimentoRealizado.getOnlyHeader());
         this.fillFields(this.procedimentoRealizado, true);
         this.gerarSequencia();
         this.insertOrUpdateState();
     }//GEN-LAST:event_jButtonCancelarMouseClicked
 
     private void jButtonAtualizarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonAtualizarMouseClicked
       if(jButtonAtualizar.isEnabled()){   
       try{
         if(this.textFieldVerifier(this.listFieldsProcedimento)){
             this.getValuesToModel();    
             this.procedimentoRealizado.preencherAtributosVazios();
             //metodo que valida o modelo (referente ao campo Procedimento)
             String errors = procedimentoRealizadoController.validateProcedimento(procedimentoRealizado);
                 if(!errors.equals("")){
                    
                     JOptionPane.showMessageDialog(this,errors);
             }else{
                this.procedimentoRealizado.preencherAtributosVazios();
                if(this.bIProcedimentoRealizadoController.merge(new BIProcedimentoRealizado(this.procedimentoRealizado))!=null){
                    //manda atualizar no serviço
                 this.sProcedimentoRealizadoController.atualizarSProcedimentoRealizado(this.procedimentoRealizado.getProcedimentoRealizadoParaEnviar(), this.sUsuarioDesktop);
                 //salva o paciente, o médico e o médico com CBO e CNS
                 Paciente p=this.procedimentoRealizado.getPaciente();
                 if( p.getCns() == null ? true : !p.getCns().trim().isEmpty() ){
                     this.pacienteController.merge(p);
                 }
                 this.medicoCboCnesController.merge(this.procedimentoRealizado.getMedicoCboCnes());
                 this.medicoController.merge(this.procedimentoRealizado.getMedico());
                 //salvou com sucesso
                 this.procedimentoRealizado=this.procedimentoRealizado.getOnlyHeader();
                 this.jTable1.getSelectionModel().clearSelection();
                 this.updateJTable(this.procedimentoRealizado);
                 //desabilita os botões
                 this.jButtonCancelar.setEnabled(false);
                 this.jButtonAtualizar.setEnabled(false);
                 this.fillFields(this.procedimentoRealizado, true);
                 this.gerarSequencia();
                 
                 this.insertOrUpdateState();
             }
                 }
         }
         }catch(Exception ex){
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "Ops! Um erro inesperado aconteceu! Relate o problema  aos desenvolvedores!");
         }
       }
     }//GEN-LAST:event_jButtonAtualizarMouseClicked
 
     private void jButtonAtualizarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButtonAtualizarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                this.jButtonAtualizarMouseClicked(null);
            }
     }//GEN-LAST:event_jButtonAtualizarKeyPressed
 
     private void jTextFieldCBOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldCBOActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextFieldCBOActionPerformed
                                              
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButtonAtualizar;
     private javax.swing.JButton jButtonCancelar;
     private javax.swing.JButton jButtonIncluir;
     private javax.swing.JButton jButtonLimpar;
     private javax.swing.JButton jButtonSair;
     private javax.swing.JComboBox jComboBoxEquipe;
     private javax.swing.JComboBox jComboBoxProcCaraterAtend;
     private javax.swing.JComboBox jComboBoxUsuarioClassificacao;
     private javax.swing.JComboBox jComboBoxUsuarioRacaCor;
     private javax.swing.JComboBox jComboBoxUsuarioServico;
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
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JLabel jLabelProcSeq;
     private javax.swing.JLabel jLabelUsuarioSeq;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JTable jTable1;
     private javax.swing.JTextField jTextFieldAno;
     private javax.swing.JTextField jTextFieldCBO;
     private javax.swing.JTextField jTextFieldCnes;
     private javax.swing.JTextField jTextFieldCnsProfiss;
     private javax.swing.JTextField jTextFieldFolha;
     private javax.swing.JTextField jTextFieldMes;
     private javax.swing.JTextField jTextFieldNomeProfiss;
     private javax.swing.JTextField jTextFieldProcCID;
     private javax.swing.JTextField jTextFieldProcCod;
     private javax.swing.JFormattedTextField jTextFieldProcDataAtend;
     private javax.swing.JTextField jTextFieldProcDescriDoenca;
     private javax.swing.JTextField jTextFieldProcDescricao;
     private javax.swing.JTextField jTextFieldProcNumAut;
     private javax.swing.JTextField jTextFieldProcQuant;
     private javax.swing.JFormattedTextField jTextFieldProcedimentoCnpj;
     private javax.swing.JFormattedTextField jTextFieldUsarioDatNasc;
     private javax.swing.JTextField jTextFieldUsuarioCns;
     private javax.swing.JTextField jTextFieldUsuarioCodEtnia;
     private javax.swing.JTextField jTextFieldUsuarioCodMunicip;
     private javax.swing.JTextField jTextFieldUsuarioCodNac;
     private javax.swing.JTextField jTextFieldUsuarioDescEtnia;
     private javax.swing.JTextField jTextFieldUsuarioNome;
     private javax.swing.JTextField jTextFieldUsuarioNomeMunicip;
     private javax.swing.JTextField jTextFieldUsuarioNomeNac;
     private javax.swing.JTextField jTextFieldUsuarioSexo;
     // End of variables declaration//GEN-END:variables
 
     /**
      * @return the procedimentoRealizado
      */
     @Override
     public ProcedimentoRealizado getProcedimentoRealizado() {
         return procedimentoRealizado;
     }
 
     /**
      * @return the listFieldsProcedimento
      */
     public List<Component> getListFieldsProcedimento() {
         return listFieldsProcedimento;
     }
 
 
     private void initComboBoxs(){
         
         
         initComboBoxServico();
         initComboBoxClassificacao();
         this.jComboBoxEquipe.setModel(this.objectComboBoxModelEquipe);
         
         //inicializar comboBox Cor
         //seta no modelo Diversas o codigo referente a tabela Cor no banco
         diversas.getDiversasPK().setCodigoTabela(Diversas.TABELA_COR_INDIVIDUO);
         //realiza a busca no banco 
         List<Diversas> list = diversasController.findAllEqual(diversas);
         this.objectComboBoxModelRacaCor.setData(list);
 
         //seta o modelo no combobox Cor
         jComboBoxUsuarioRacaCor.setModel(this.objectComboBoxModelRacaCor);
        
         //seta os objetos no modelo
         this.objectComboBoxModelCaraterAtend.setData(CaraterAtendimento.LIST);
         //seta o modelo no combobox CaraterAtendimento
         jComboBoxProcCaraterAtend.setModel(objectComboBoxModelCaraterAtend);
     }
     private void clearModelsServicoClassificacao(){
         this.objectComboBoxModelServico = new ObjectComboBoxModel<Diversas>();
         this.objectComboBoxModelClassificaoServico = new ObjectComboBoxModel<Diversas>();
         this.objectComboBoxModelClassificaoServico.setFormatter(new DiversasFormatter());
         this.objectComboBoxModelServico.setFormatter(new DiversasFormatter());
         
         
     }
     private void initComboBoxServico(){
         this.jComboBoxUsuarioServico.setModel(this.objectComboBoxModelServico);
    
     }
     private void initComboBoxClassificacao(){
         this.jComboBoxUsuarioClassificacao.setModel(this.objectComboBoxModelClassificaoServico);
     }
    
     
      
       
       
       private void addListenersFields(){
           
           
           jTextFieldCnes.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 //perdeu o foco para um campo e o objeto não tem um CNS
                 if(e.getOppositeComponent() instanceof JTextField ){
                    String cnes=CadastroIndividualizado.this.jTextFieldCnes.getText();
                    if(!cnes.isEmpty()){
                        //caso os cnes's sejam diferentes vai executar
                        if(!cnes.equals(CadastroIndividualizado.this.procedimentoRealizado.getProcedimentoRealizadoPK().getCnesUnidade())){
                            CadastroIndividualizado.this.buscarEquipesDaUnidade(CadastroIndividualizado.this.jTextFieldCnes.getText());
                        }
                    }
                }
               
             }
         });
 
             jTextFieldCnsProfiss.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             
                procedimentoRealizado.getProcedimentoRealizadoPK().setCnsMedico( ((JTextField)e.getComponent()).getText());
                   
             }
         });
          jTextFieldNomeProfiss.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
              
             }
         });   
          jTextFieldCBO.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             
                procedimentoRealizado.getProcedimentoRealizadoPK().setCboMedico(((JTextField)e.getComponent()).getText());
                
              
             }
         });
          
         jTextFieldAno.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                   if(e.getOppositeComponent() instanceof JTextField){
                     procedimentoRealizado.getProcedimentoRealizadoPK().setCompetencia(((JTextField)e.getComponent()).getText()+jTextFieldMes.getText());
                     
                }
             }
         }); 
         
          jTextFieldFolha.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                 
                String folha = bIProcedimentoRealizadoController.getNextFolha(new BIProcedimentoRealizado(procedimentoRealizado));
                if(!folha.isEmpty()&& folha.length()==3){
                    
                    jTextFieldFolha.setText(folha);
                }
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                //se o próximo componente for um Component
                if(e.getOppositeComponent() instanceof Component){
                    String folha = jTextFieldFolha.getText();
                    if(!folha.equals("")){
                         //completa com zeros caso precise
                         folha = String.format("%03d", Integer.parseInt(folha));
                         jTextFieldFolha.setText(folha);
                    }
                    //pega o número da folha
                    procedimentoRealizado.getProcedimentoRealizadoPK().setNumeroFolha(((JTextField)e.getComponent()).getText());
 
                    if(jTextFieldFolha.getInputVerifier().shouldYieldFocus(jTextFieldFolha)){
                         if(textFieldVerifier(getListFieldsHeader())){
                             //desabilita os campos do cabeçalho da tela que são 
                             //referentes as informações da unidade e do usuário
                             disableFieldsHeader();
                             enableFieldsProcedimento();
                             CadastroIndividualizado.this.jTextFieldUsuarioCns.requestFocusInWindow();
                             
                         }  
 
                    }
                     try {
                         //vai pegar a folha caso exista
                        ProcedimentoRealizado pr=(ProcedimentoRealizado) CadastroIndividualizado.this.procedimentoRealizado.clone();
                        //coloca a origem
                        pr.setOrigemProcedimento(ProcedimentoRealizado.ORIGEM_INDIVIDUALIZADO);
                        //retira a nacionalidade
                        pr.setNacionalidadePaciente(null);
                        pr.setQuantidadeRealizada(null);
                        //pr.getProcedimentoRealizadoPK().setCompetencia();
                        //retira a sequência
                        pr.getProcedimentoRealizadoPK().setSequenciaFolha(null);
                         CadastroIndividualizado.this.findAllProcedimentosFolha(pr);
                        
       
                     } catch (CloneNotSupportedException ex) {
                         Logger.getLogger(CadastroIndividualizado.class.getName()).log(Level.SEVERE, null, ex);
                     }
             }
            }
         });
          
          
          
          
          
           jTextFieldUsuarioCns.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 //perdeu o foco para um campo e o objeto não tem um CNS
                 if(e.getOppositeComponent() instanceof JTextField ){
                    String cns=CadastroIndividualizado.this.jTextFieldUsuarioCns.getText().trim();
                    if(!cns.isEmpty()){
                        //caso os códigos sejam diferentes vai executar
                        if(!cns.equals(CadastroIndividualizado.this.procedimentoRealizado.getCnsPaciente())){
                            CadastroIndividualizado.this.buscarPacienteECompletarCampos(CadastroIndividualizado.this.jTextFieldUsuarioCns.getText().trim());
                        }
                    }else{
                        //jTextFieldUsuarioCns.setv
                    }
                }
                
             }
         });
           
            jTextFieldUsuarioNome.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             if(e.getOppositeComponent() instanceof JTextField ){
                String nomeUsuario = ((JTextField)e.getComponent()).getText();
                if(nomeUsuario!=null){
                     if(!nomeUsuario.equals(CadastroIndividualizado.this.procedimentoRealizado.getNomePaciente())){
                         CadastroIndividualizado.this.procedimentoRealizado.setNomePaciente(((JTextField)e.getComponent()).getText());
                     }
                }
             }
             
             }
             
         });
            
             jTextFieldUsuarioSexo.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             
                CadastroIndividualizado.this.procedimentoRealizado.setSexoPaciente(((JTextField)e.getComponent()).getText());
             }
         });
           
             
              jTextFieldUsarioDatNasc.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                //se o próximo componente for um Component e o método ainda não tinha sido executado
                if( e.getOppositeComponent() instanceof Component){ 
                 if(jTextFieldUsarioDatNasc.getInputVerifier().shouldYieldFocus(jTextFieldUsarioDatNasc)){   
                     //converte a data para o formato YYYMMdd 
                     String dataNasc = DateUtil.parseToYearMonthDay(((JTextField)e.getComponent()).getText());
                     CadastroIndividualizado.this.procedimentoRealizado.setDataNascimentoPaciente(dataNasc);
                 }
             
                }
             
             }
         });
             
            jTextFieldUsuarioCodMunicip.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             
                CadastroIndividualizado.this.procedimentoRealizado.setCodigoIBGECidadePaciente(((JTextField)e.getComponent()).getText());
             }
         });
            
             jTextFieldUsuarioCodNac.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             
                CadastroIndividualizado.this.procedimentoRealizado.setNacionalidadePaciente(((JTextField)e.getComponent()).getText());
             }
         });
         jComboBoxUsuarioRacaCor.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                //abre o combobox ao ganhar o foco 
                jComboBoxUsuarioRacaCor.setPopupVisible(true);
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                int index = ((JComboBox)e.getComponent()).getSelectedIndex();
                Diversas d = (Diversas) objectComboBoxModelRacaCor.getData().get(index);
                CadastroIndividualizado.this.procedimentoRealizado.setRacaPaciente(d.getDiversasPK().getCodigoItemTabela().toString());
                if(d.getDiversasPK().getCodigoItemTabela().trim().equals(Diversas.COD_RACA_COR_INDIGENA)){
                    CadastroIndividualizado.this.jTextFieldUsuarioCodEtnia.setEnabled(true);
                    CadastroIndividualizado.this.jTextFieldUsuarioCodEtnia.requestFocusInWindow();
                }
                else{
                    CadastroIndividualizado.this.jTextFieldUsuarioCodEtnia.setEnabled(false);
                }
             }
         });
              
           jTextFieldUsuarioCodEtnia.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             
                CadastroIndividualizado.this.procedimentoRealizado.setEtniaPaciente(((JTextField)e.getComponent()).getText());
             }
         });
           
            jTextFieldProcDataAtend.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
 //               if(jTextFieldUsarioDatNasc.getText().equals("  /  /    ")){
 //                   jTextFieldProcDataAtend.setVerifyInputWhenFocusTarget(true);
 //                   jTextFieldUsarioDatNasc.requestFocus();
 //                   jTextFieldProcDataAtend.setVerifyInputWhenFocusTarget(true);
 //               }
                    
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                //se o próximo componente for um jtextfield e o método ainda não tinha sido executado
                if(e.getOppositeComponent() instanceof JTextField){ 
                    String dataAtend = ((JTextField)e.getComponent()).getText();
                    if(!dataAtend.isEmpty()){
                         //converte a data para o formato YYYYMMdd    
                         dataAtend = DateUtil.parseToYearMonthDay(dataAtend);
                         //caso as data sejam diferentes vai executar e o formato do conteudo do campo for válido
                         if(!dataAtend.equals(CadastroIndividualizado.this.procedimentoRealizado.getDataAtendimento()) 
                                // && jTextFieldProcDataAtend.getInputVerifier().shouldYieldFocus(jTextFieldProcDataAtend)
                                 ){
                             CadastroIndividualizado.this.procedimentoRealizado.setDataAtendimento(dataAtend);   
                             
                            
                 }
                    
                    } 
                }
            }  
         });
            
          jTextFieldProcCod.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                 //se o proximo elemento for um textField
                 if(e.getOppositeComponent() instanceof JTextField ){
                     //se as datas nao forem nulas
                     if(CadastroIndividualizado.this.procedimentoRealizado.getDataNascimentoPaciente()!=null && CadastroIndividualizado.this.procedimentoRealizado.getDataAtendimento()!=null){
                         String dataNasc = CadastroIndividualizado.this.jTextFieldUsarioDatNasc.getText();
                         String dataAtend = CadastroIndividualizado.this.jTextFieldProcDataAtend.getText();
                        
                         
                         //se alguma data for diferente da que está persistida no objeto
                         if(!dataNasc.equals(CadastroIndividualizado.this.procedimentoRealizado.getDataNascimentoPaciente()) || !dataAtend.equals(CadastroIndividualizado.this.procedimentoRealizado.getDataAtendimento())){
                              
                             //obtem a idade do paciente
                             String age = String.valueOf(DateUtil.getAge(CadastroIndividualizado.this.procedimentoRealizado.getDataNascimentoPaciente(),CadastroIndividualizado.this.procedimentoRealizado.getDataAtendimento()));
                             //seta no modelo
                             CadastroIndividualizado.this.procedimentoRealizado.setIdadePaciente(age);
                             //se o campo procedimento estiver vazio zera os campos quantidade e CID
                             //porque estes dependem do codigo do procedimento
                             
                         }
                          
                     }
                 }
                 //textFieldVerifier(listFieldsDates);
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 //perdeu o foco para a campo "quantidade"
                if(e.getOppositeComponent() instanceof JTextField ){
                   
                    String codigo=CadastroIndividualizado.this.jTextFieldProcCod.getText();
                    if(!codigo.isEmpty()){
                        
                        //caso os código sejam diferentes vai executar
                        if(!codigo.equals(CadastroIndividualizado.this.procedimentoRealizado.getCodigoProcedimento())){
                            CadastroIndividualizado.this.buscarServicoEClassificaoServicoEPrencherCampos(CadastroIndividualizado.this.jTextFieldProcCod.getText());
                        }
                        
                    }
                
                }
             }
         });
           
           jTextFieldProcQuant.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 if(e.getOppositeComponent() instanceof JComboBox ){
                    String quantidade=CadastroIndividualizado.this.jTextFieldProcQuant.getText();
                    if(!quantidade.isEmpty()){
                        //caso as quantidades sejam diferentes vai executar
                        try{
                            Double d=Double.parseDouble(quantidade);
 
                            if(!d.equals(CadastroIndividualizado.this.procedimentoRealizado.getQuantidadeRealizada())){
                                CadastroIndividualizado.this.procedimentoRealizado.setQuantidadeRealizada(d); 
                            }
                        }catch(NumberFormatException ex){
                        
                        }
                    }
                }
                
             }
         });
           
          jTextFieldProcCID.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 
                 
                CadastroIndividualizado.this.procedimentoRealizado.setCidDoencaprocedimento(((JTextField)e.getComponent()).getText());
             }
         });
            jTextFieldProcedimentoCnpj.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 
                String cnpj = ((JTextField)e.getComponent()).getText().replaceAll("[.,/,-]","").trim();
                CadastroIndividualizado.this.procedimentoRealizado.setCnpj(cnpj);
             }
         });
           jComboBoxProcCaraterAtend.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                //abre o combobox ao ganhar o foco 
                jComboBoxProcCaraterAtend.setPopupVisible(true);
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                int index = ((JComboBox)e.getComponent()).getSelectedIndex(); 
                CaraterAtendimento c = (CaraterAtendimento) objectComboBoxModelCaraterAtend.getData().get(index);
                CadastroIndividualizado.this.procedimentoRealizado.setCaracterizacaoAtendimento(c.getCodigo().toString());
                
               
               
             }
         });
           
          jTextFieldProcNumAut.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             
                CadastroIndividualizado.this.procedimentoRealizado.setNumeroAutorizacao(((JTextField)e.getComponent()).getText());
             }
         });
           
         jComboBoxEquipe.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                //abre o combobox ao ganhar o foco 
                jComboBoxEquipe.setPopupVisible(true);
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                int index = ((JComboBox)e.getComponent()).getSelectedIndex();
                if(index>=0){
                     Equipe equipe = (Equipe) objectComboBoxModelEquipe.getData().get(index);
                     CadastroIndividualizado.this.procedimentoRealizado.setEquipe(equipe.getEquipePK().getSequencia()+equipe.getArea());
                     CadastroIndividualizado.this.procedimentoRealizado.setEquipeSequencia(equipe.getEquipePK().getSequencia());
                     CadastroIndividualizado.this.procedimentoRealizado.setEquipeArea(equipe.getArea());
                }
                }
         });
         
         jComboBoxUsuarioServico.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                //abre o combobox ao ganhar o foco 
                jComboBoxUsuarioServico.setPopupVisible(true);
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                int index = ((JComboBox)e.getComponent()).getSelectedIndex();
                if(index>=0){
                Diversas d = (Diversas) objectComboBoxModelServico.getData().get(index);
                CadastroIndividualizado.this.procedimentoRealizado.setCodigoServico(d.getDiversasPK().getCodigoItemTabela().toString());
                }
             }
         });
         
         jComboBoxUsuarioClassificacao.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
                 //abre o combobox ao ganhar o foco 
                jComboBoxUsuarioClassificacao.setPopupVisible(true);
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                int index = ((JComboBox)e.getComponent()).getSelectedIndex();
                if(index>=0){
                Diversas d = (Diversas) objectComboBoxModelClassificaoServico.getData().get(index);
                CadastroIndividualizado.this.procedimentoRealizado.setCodigoClassificacaoServico(d.getDiversasPK().getCodigoItemTabela().substring(3));
                }
             }
         });
       }
       
       /**
        * Desabilita os campos do cabeçalho da tela
        */
       public void disableFieldsHeader(){
            this.changeStatusHeader(false);
       }
       
       /**
        * Abilita os campos do cabeçalho da tela
        */
       private void enableFieldsHeader(){
           this.changeStatusHeader(true);
       }
       
       private void changeStatusHeader(boolean status){
             jTextFieldCnes.setEnabled(status);
             jTextFieldCnsProfiss.setEnabled(status);
             jTextFieldNomeProfiss.setEnabled(status);
             jTextFieldCBO.setEnabled(status);
             jComboBoxEquipe.setEnabled(status);
             jTextFieldMes.setEnabled(status);
             jTextFieldAno.setEnabled(status);
             jTextFieldFolha.setEnabled(status);
       }
       /**
        * Desabilita os campos relacionados ao paciente/usuário
        */
       private void disabledFieldsProcedimento(){
         this.changeStatusFieldsProcedimento(false);
           
       }
       private void enableFieldsProcedimento(){
         this.changeStatusFieldsProcedimento(true);
           
       }
       
       
       
       private void changeStatusFieldsProcedimento(boolean status){
           jTextFieldUsuarioCns.setEnabled(status);
           jTextFieldUsuarioNome.setEnabled(status);
           jTextFieldUsuarioSexo.setEnabled(status);
           jTextFieldUsarioDatNasc.setEnabled(status);
           jTextFieldUsuarioCodMunicip.setEnabled(status);
           jTextFieldUsuarioCodNac.setEnabled(status);
           jComboBoxUsuarioRacaCor.setEnabled(status);
           jTextFieldProcDataAtend.setEnabled(status);
           jTextFieldProcCod.setEnabled(status);
           jTextFieldProcQuant.setEnabled(status);
           jComboBoxUsuarioClassificacao.setEnabled(status);
           jComboBoxUsuarioServico.setEnabled(status);
           jTextFieldProcCID.setEnabled(status);
           jTextFieldProcedimentoCnpj.setEnabled(status);
           jComboBoxProcCaraterAtend.setEnabled(status);
           jTextFieldProcNumAut.setEnabled(status);
           jButtonLimpar.setEnabled(status);  
           jButtonIncluir.setEnabled(status);        
           
       }
       
       /**
        *Preenche um modelo ProcedimentoRealizado com os campos do cabeçalho da tela  
        */
       private void fillHeaderModelProcedimentoRealizado(ProcedimentoRealizado p){
           p.getProcedimentoRealizadoPK().setCnesUnidade(jTextFieldCnes.getText());
           p.getProcedimentoRealizadoPK().setCnsMedico(jTextFieldCnsProfiss.getText());
           p.getProcedimentoRealizadoPK().setCboMedico(jTextFieldCBO.getText());
           p.getProcedimentoRealizadoPK().setCompetencia(jTextFieldAno.getText()+jTextFieldMes.getText());
           p.getProcedimentoRealizadoPK().setNumeroFolha(jTextFieldFolha.getText());
           
       }
       
       /**
        * Pega os valores dos campos que ainda não foram colocados no modelo procedimentoRealizado
        */
       private void getValuesToModel(){
         Procedimento procedimento = new Procedimento();
      
         //pega a competencia digitada
         String competencia = jTextFieldAno.getText()+jTextFieldMes.getText();
          
         int indexCarater = jComboBoxProcCaraterAtend.getSelectedIndex();
         CaraterAtendimento c = (CaraterAtendimento) this.objectComboBoxModelCaraterAtend.getData().get(indexCarater);
         int indexRaca = jComboBoxUsuarioRacaCor.getSelectedIndex();
         Diversas d = (Diversas) this.objectComboBoxModelRacaCor.getData().get(indexRaca);
         
         this.procedimentoRealizado.setNacionalidadePaciente(jTextFieldUsuarioCodNac.getText());
         this.procedimentoRealizado.getProcedimentoRealizadoPK().setCompetencia(competencia);
         this.procedimentoRealizado.getProcedimentoRealizadoPK().setNumeroFolha(jTextFieldFolha.getText());
         this.procedimentoRealizado.getProcedimentoRealizadoPK().setSequenciaFolha(String.valueOf(this.sequenciaFolha));
         this.procedimentoRealizado.setCnsPaciente(jTextFieldUsuarioCns.getText());
         this.procedimentoRealizado.setNomePaciente(jTextFieldUsuarioNome.getText());
         this.procedimentoRealizado.setSexoPaciente(jTextFieldUsuarioSexo.getText());
         this.procedimentoRealizado.setCodigoIBGECidadePaciente(jTextFieldUsuarioCodMunicip.getText()); 
         this.procedimentoRealizado.setNacionalidadePaciente(jTextFieldUsuarioCodNac.getText());
         this.procedimentoRealizado.setRacaPaciente(d.getDiversasPK().getCodigoItemTabela()); 
         if(this.procedimentoRealizado.getRacaPaciente().equals(Diversas.COD_RACA_COR_INDIGENA)){
             this.procedimentoRealizado.setEtniaPaciente(jTextFieldUsuarioCodEtnia.getText());
         }
         this.procedimentoRealizado.setDataNascimentoPaciente(DateUtil.parseToYearMonthDay(jTextFieldUsarioDatNasc.getText()));
         this.procedimentoRealizado.setDataAtendimento(DateUtil.parseToYearMonthDay(jTextFieldProcDataAtend.getText()));
         this.procedimentoRealizado.setCodigoProcedimento(jTextFieldProcCod.getText());
         this.procedimentoRealizado.setQuantidadeRealizada(Double.valueOf(jTextFieldProcQuant.getText()));
         this.procedimentoRealizado.setCidDoencaprocedimento(jTextFieldProcCID.getText());
         this.procedimentoRealizado.setCaracterizacaoAtendimento(c.getCodigo());
         this.procedimentoRealizado.setNumeroAutorizacao(jTextFieldProcNumAut.getText());
        
         
         
        
      
         Character digitoVerificador = this.procedimentoRealizado.getCodigoProcedimento().charAt(9);
         procedimento.getProcedimentoPk().setId(this.procedimentoRealizado.getCodigoProcedimento().substring(0,9));
         //procedimento.getProcedimentoPk().setCompetencia(this.procedimentoRealizado.getProcedimentoRealizadoPK().getCompetencia());
         procedimento.setDigitoVerificador(digitoVerificador);
     
         //realiza uma busca pelo procedimento
         List<Procedimento> listEncontrados = procedimentoController.findAllEqual(procedimento);
         
         if(!listEncontrados.isEmpty()){
             String  tipo = listEncontrados.get(0).typeProcedimento();    
             //seta o tipo do procedimento (BPA ou BPAI)
             this.procedimentoRealizado.setOrigemProcedimento(tipo);
         }
         String competenciaMvm  =gestorCompetenciaController.getCompetenciaAtual();
         if(competenciaMvm!=null){
             //seta competencia movimento
             procedimentoRealizado.setCompetenciaMovimento(competencia);
         }
         
          //obtem a idade do paciente
          String age = String.valueOf(DateUtil.getAge(CadastroIndividualizado.this.procedimentoRealizado.getDataNascimentoPaciente(),CadastroIndividualizado.this.procedimentoRealizado.getDataAtendimento()));
          //seta no modelo
          CadastroIndividualizado.this.procedimentoRealizado.setIdadePaciente(age);
       }
       
       /**
        * Insere os procedimentos nas bases de dados e todos as outras
        * entidades, como paciente, Medico e MedicoCboCnes
        */
       private void insertInDatabase(){
           
          
          //insere o modelo no banco de dados do nosso banco de dados
           List<ProcedimentoRealizado> l=this.tableModelDados.getListWithOutEmptyElements();
           List<BIProcedimentoRealizado> biProList=new ArrayList<BIProcedimentoRealizado>();
           
           //vai pegar os médicos e os pacientes
           int size=l.size();
           ProcedimentoRealizado p=null;
           for(int i=0;i<size;i++){
               p=l.get(i);
               //pega o paciente
               Paciente pa=p.getPaciente();
               //se tem um CNS vai gravar no banco de dados
               if(!pa.getCns().isEmpty()){
                     this.setPaciente.add(pa);
               }
               //pega o médico
               Medico m=p.getMedico();
               //caso nao exista um nome preenche com espaços
               m.setNome( m.getNome()==null?"      ":m.getNome());
               this.setMedico.add(m);
               //pega o MedicoCboCnes e adiciona no Set
               this.setMedicoCboCnes.add(p.getMedicoCboCnes());
               //cria um procedimentoRealizado a ser salvo no bando do sistema
               biProList.add(new BIProcedimentoRealizado(p));
           }
           //salva todos os pacientes ou senão atualiza
           this.pacienteController.merge(new ArrayList<Paciente>(this.setPaciente));
           //salva todos os médicos ou senão atualiza
           this.medicoController.merge(new ArrayList<Medico>(this.setMedico));
           //salva todos os médicosCbosCnes ou senão atualiza
           this.medicoCboCnesController.merge(new ArrayList<MedicoCboCnes>(this.setMedicoCboCnes));
           //salva todos os procedimentos no banco próprio do sistema
           this.bIProcedimentoRealizadoController.merge(biProList);
           
           
           
           
           //vai limpar os Sets
           this.setPaciente.clear();
           this.setMedico.clear();
           this.setMedicoCboCnes.clear();
       }
       
       /**
        * Limpa todos os campos da tela
        */
       private void clearFields(){
         jTextFieldProcCID.setText("");
         jTextFieldProcDescriDoenca.setText("");
         jTextFieldProcCod.setText("");
         jTextFieldProcDataAtend.setText("");
         jTextFieldProcDescriDoenca.setText("");
         jTextFieldProcDescricao.setText("");
         jTextFieldProcNumAut.setText("");
         jTextFieldProcQuant.setText("");
         jTextFieldUsarioDatNasc.setText("");
         jTextFieldUsuarioCns.setText("");
         jTextFieldUsuarioCodEtnia.setText("");
         jTextFieldUsuarioCodMunicip.setText("");
         jTextFieldUsuarioDescEtnia.setText("");
         jTextFieldUsuarioNome.setText("");
         jTextFieldUsuarioNomeMunicip.setText("");
         jTextFieldUsuarioNomeNac.setText("");
         jTextFieldUsuarioSexo.setText("");
         jComboBoxProcCaraterAtend.setSelectedIndex(0);
         jComboBoxUsuarioRacaCor.setSelectedIndex(0);
       }
       
       /**
        * Dada um procedimento com a folha, cbo, cnes, cns e competência, vai buscar outros procedimentos, caso exista.
        * A tabela é preenchida e os campos também.
        * @param procedimentoRealizado 
        */
       public void findAllProcedimentosFolha(ProcedimentoRealizado procedimentoRealizado){
           //muda a lista presente na tabela
           this.updateJTable(procedimentoRealizado);
           this.procedimentoRealizado= procedimentoRealizado;
           this.fillFields(procedimentoRealizado, true);
           
           //incremeta a sequencia
           this.gerarSequencia();
           
           this.insertOrUpdateState();
       
       }
       /**
        *Preenche os campos da tela baseado em um objeto procedimento realizado passado 
        */
       private void  fillFields(ProcedimentoRealizado p,boolean flag){
           //se o flag for true o cabecalho também sera preenchido
           if(flag==true){
               jTextFieldCnes.setText(p.getProcedimentoRealizadoPK().getCnesUnidade());
               jTextFieldCnsProfiss.setText(p.getProcedimentoRealizadoPK().getCnsMedico());
               jTextFieldNomeProfiss.setText(p.getNomeProfissional());
               jTextFieldCBO.setText(p.getProcedimentoRealizadoPK().getCboMedico());
               if(p.getProcedimentoRealizadoPK().getCompetencia()!=null){
                 jTextFieldMes.setText(p.getProcedimentoRealizadoPK().getCompetencia().substring(4));
                 jTextFieldAno.setText(p.getProcedimentoRealizadoPK().getCompetencia().substring(0, 4));
               }
               jTextFieldFolha.setText(p.getProcedimentoRealizadoPK().getNumeroFolha());
              
               
           }
           String seqFolha = p.getProcedimentoRealizadoPK().getSequenciaFolha();
           if(seqFolha!=null){
             this.sequenciaFolha=Integer.parseInt(seqFolha);
           }
           jLabelUsuarioSeq.setText(seqFolha);
           jLabelProcSeq.setText(seqFolha);
           jTextFieldUsuarioCns.setText(p.getCnsPaciente());
           jTextFieldUsuarioNome.setText(p.getNomePaciente());
           jTextFieldUsuarioSexo.setText(p.getSexoPaciente());
           jTextFieldUsarioDatNasc.setText(DateUtil.parseToDayMonthYear(p.getDataNascimentoPaciente(),true));
           jTextFieldUsuarioCodMunicip.setText(p.getCodigoIBGECidadePaciente());
           
           jTextFieldProcDataAtend.setText(DateUtil.parseToDayMonthYear(p.getDataAtendimento(),true));
           jTextFieldProcCod.setText(p.getCodigoProcedimento());
           
           if(p.getQuantidadeRealizada()==null){
               jTextFieldProcQuant.setText("");
           }else
             jTextFieldProcQuant.setText(String.valueOf(p.getQuantidadeRealizada()));
           jTextFieldProcedimentoCnpj.setText(p.getCnpj());
           jTextFieldProcCID.setText(p.getCidDoencaprocedimento());
           //seleciona os itens de cada combobox
           this.carregarComboBox(p);
           
           jTextFieldProcNumAut.setText(p.getNumeroAutorizacao());
           
           
           jTextFieldUsuarioNomeNac.setText("");
           jTextFieldProcDescricao.setText("");
           jTextFieldUsuarioNomeMunicip.setText("");
           jTextFieldProcDescriDoenca.setText("");
           
           //caso tenha algum campo com background em vermelho
           this.jTextFieldProcCID.setBackground(Color.WHITE);
           this.jTextFieldProcCod.setBackground(Color.WHITE);
           
       }
     
       /**
        * Carrega as descrições do combobox de acordo com o procedimento escolhido
        * e os respectivos código para cada item
        * @param p 
        */
       private void carregarComboBox(ProcedimentoRealizado p){
           
           if(p.getRacaPaciente()!=null){
             this.selectItemJComboBoxRacaCor(p.getRacaPaciente());
           }
           else{
             this.selectItemJComboBoxRacaCor(Diversas.COD_RACA_COR_SEM_INFORMACAO);  
           }
           if((jComboBoxUsuarioRacaCor.getSelectedItem()!=null) && (jComboBoxUsuarioRacaCor.getSelectedItem().toString().substring(0, 2).equals(Diversas.COD_RACA_COR_INDIGENA))){
               jTextFieldUsuarioCodEtnia.setText(p.getEtniaPaciente());
           }
           
           if(p.getCaracterizacaoAtendimento()!=null){
             this.selectItemJComboBoxCaraterAtend(p.getCaracterizacaoAtendimento());
           }
           else{
             this.selectItemJComboBoxCaraterAtend(CaraterAtendimento.SEM_INFORMACAO);
           }
           if(p.getCodigoServico()!=null){
                this.selectItemJComboBoxServico(p.getCodigoServico());
                
           }else{
              initComboBoxServico();
           }
           
           if(p.getCodigoClassificacaoServico()!=null){
             
               this.selectItemJComboBoxClassificacao(p.getCodigoServico()+p.getCodigoClassificacaoServico());
           
           }else{
             initComboBoxClassificacao();
            }
       }
       private void selectItemJComboBoxRacaCor(String codigoItem){
           Diversas d= new Diversas(new DiversasPK(Diversas.TABELA_COR_INDIVIDUO,codigoItem ));   
           this.objectComboBoxModelRacaCor.setSelectedObject(d);
       }
       
       private void selectItemJComboBoxCaraterAtend(String codigoItem){
           CaraterAtendimento c= new CaraterAtendimento(codigoItem,"");
           
           this.objectComboBoxModelCaraterAtend.setSelectedObject(c);
       }
       
        private void selectItemJComboBoxServico(String codigoItem){
            Diversas d= new Diversas(new DiversasPK(Diversas.TABELA_SERVICO,codigoItem ));
            d=this.diversasController.findEqual(d);
           this.objectComboBoxModelServico.setSelectedObject(d);
       }
        
        private void selectItemJComboBoxClassificacao(String codigoItem){
           Diversas d= new Diversas(new DiversasPK(Diversas.TABELA_CLASSIFICACAO_SERVICO,codigoItem));
           d=this.diversasController.findEqual(d);
           this.objectComboBoxModelClassificaoServico.setSelectedObject(d);
       } 
           
       /**
         * Prenche os combox serviço e classificação de serviço de 
         * acordo com o procedimento escolhido
         * @param codigoProcedimento 
         */
       
       private void buscarServicoEClassificaoServicoEPrencherCampos(String codigoProcedimento){
           
            this.procedimentoRealizado.setCodigoProcedimento(codigoProcedimento);
            HashMap<String, Object> res=new HashMap<String, Object> ();
            res.put("procedimentoServicoPK.codigoProcedimento", this.procedimentoRealizado.getCodigoProcedimento().substring(0, 9));
            res.put("procedimentoServicoPK.competencia", ModelUtil.COMPETENCIA_MAIS_RECENTE);
            //preenche os combobox
            ProcedimentoServico pro=this.procedimentoServicoController.findEqual(res);
            
            if(pro != null){
                String competencia=ModelUtil.COMPETENCIA_MAIS_RECENTE;
                String codigo=this.procedimentoRealizado.getCodigoProcedimento().substring(0,9);
                //busca as classificaçoes dos serviços que o procedimento tem
                List<Diversas> d=this.diversasController.findAllClassificacaoServico(codigo, competencia);
                this.objectComboBoxModelClassificaoServico.setData(d);
                //seleciona a primeira classificação
                if(!d.isEmpty()){
                     this.jComboBoxUsuarioClassificacao.setSelectedIndex(0);
                }
                //busca todos os serviços que o procedimento tem
                d=this.diversasController.findAllServicos(codigo, competencia);
                this.objectComboBoxModelServico.setData(d);
                //seleciona o primeiro serviço
                if(!d.isEmpty()){
                     this.jComboBoxUsuarioServico.setSelectedIndex(0);
                }
 
                }
       }
       /**
        * Pega um paciente com base no CNS e preenche todos os campos da tela
        * referente a ele
        * @param cnsPaciente 
        */
       private void buscarPacienteECompletarCampos(String cnsPaciente){
             this.procedimentoRealizado.setCnsPaciente(cnsPaciente);
             //pega um paciente
             Paciente p= new Paciente(this.procedimentoRealizado.getCnsPaciente());
             if(!p.getCns().isEmpty()){
                 Paciente pa=CadastroIndividualizado.MAP_PACIENTE.get(p.getCns());
                 //não achou nos pacientes já encontrados
                 if(pa==null){
                     //pega no banco de dados
                     pa=this.pacienteController.findEqual(p);
                 }
                 if(pa!=null){
                     //CadastroIndividualizado.MAP_PACIENTE.put(pa.getCns(), pa);
                     this.jTextFieldUsuarioNome.setText(pa.getNome());
                     this.jTextFieldUsuarioCodEtnia.setText(pa.getEtnia());
                     this.jTextFieldUsuarioCodMunicip.setText(pa.getCodigoIbgeCidade());
                     this.jTextFieldUsuarioCodNac.setText(pa.getNacionalidade());
                     this.jTextFieldUsuarioSexo.setText(pa.getSexo().toString());
                     this.jTextFieldUsarioDatNasc.setText(DateUtil.parseToDayMonthYear(pa.getDataNascimento(), false));
                     //muda o foco
                    this.jTextFieldProcDataAtend.requestFocusInWindow();
                     
                     //agora seta no objeto procedimentoRealizado os valores
                     
                     this.procedimentoRealizado.setNomePaciente(pa.getNome());
                     this.procedimentoRealizado.setEtniaPaciente(pa.getEtnia());
                     this.procedimentoRealizado.setRacaPaciente(pa.getRaca());
                     this.procedimentoRealizado.setCodigoIBGECidadePaciente(pa.getCodigoIbgeCidade());
                     this.procedimentoRealizado.setNacionalidadePaciente(pa.getNacionalidade());
                     this.procedimentoRealizado.setSexoPaciente(pa.getSexo().toString());
                     this.procedimentoRealizado.setDataNascimentoPaciente(pa.getDataNascimento());
                     this.carregarComboBox(this.procedimentoRealizado);
                 }
             }
       }
 
       /**
        * Pega todas as equipes de acordo com uma unidade
        * @param CNESUnidade
        */
   private void buscarEquipesDaUnidade(String CNESUnidade){
         this.procedimentoRealizado.getProcedimentoRealizadoPK().setCnesUnidade(CNESUnidade);
         //vai buscar a equipe caso exista
         //criação de restrições
         String competencia=ModelUtil.COMPETENCIA_MAIS_RECENTE;
         
         HashMap<String,Object> res= new HashMap<String, Object>();
         res.put("equipePK.cnes", CNESUnidade);
         res.put("equipePK.competencia", competencia);
         List<Equipe> equipes=this.equipeController.findAllEqual(res);
         //devolveu algo
         if(!equipes.isEmpty()){
             this.objectComboBoxModelEquipe.setSelectedItem(equipes.get(0));
             this.objectComboBoxModelEquipe.setData(equipes);
         }
   }
 }
