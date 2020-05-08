 package sgcmf.view.comiteGestor.ocorrenciaJogo;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import sgcmf.control.CtrComiteGestor;
 import sgcmf.control.CtrGol;
 import sgcmf.control.CtrOcorrenciaJogo;
 import sgcmf.model.other.SGCMFIcons;
 import sgcmf.model.other.ResultadoOperacao;
 import sgcmf.model.other.TipoResultadoOperacao;
 import sgcmf.view.comiteGestor.LimBuscarJogador;
 import sgcmf.view.UtilView;
 import sgcmf.view.tecnico.ISelecionarJogador;
 
 public class LimRegistrarGol extends JDialog implements ISelecionarJogador
 {
     private LimBuscarJogador limBuscarJogador;
     private LimGerenciarOcorrenciasJogo limGerenciarOcorrencias;
     private CtrComiteGestor ctrComiteGestor;
     private JTextField jtfInstanteTempoMin;
     private JTextField jtfInstateTempoSeg;
     private JTextField jtfJogador;
     private JTextField jtfJogadorAssist;
     private ButtonGroup bgTipo;
     private ButtonGroup bgModo;
     private boolean selecaoJogadorAutor = false;
     private JRadioButton jrbTipoAFavor;
     private JRadioButton jrbTipoContra;
     private JRadioButton jrbModoComum;
     private JRadioButton jrbModoFalta;
     private JRadioButton jrbModoPenalti;
     private JButton jbPesqJogadorAssist;
 
     public LimRegistrarGol(CtrComiteGestor ctrComiteGestor, LimBuscarJogador limBuscarJogador, LimGerenciarOcorrenciasJogo limGerenciarOcorrencias)
     {
         this.ctrComiteGestor = ctrComiteGestor;
         this.limBuscarJogador = limBuscarJogador;
         this.limGerenciarOcorrencias = limGerenciarOcorrencias;
 
         setTitle("Registrar Gol");
         setDefaultCloseOperation(HIDE_ON_CLOSE);
 
         add(montaMainPanel());
         setModal(true);
         setSize(370, 275);
         setLocationRelativeTo(null);
 
         addWindowListener(new WindowAdapter()
         {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 resetCamposInterface();
             }
         });
     }
 
     private JPanel montaMainPanel()
     {
         JPanel mainPanel = new JPanel(new BorderLayout());
         JPanel formPanel = new JPanel(new GridLayout(5, 2));
         JPanel panelJRBTipo = new JPanel(new FlowLayout(FlowLayout.LEFT));
         JPanel panelJRBModo = new JPanel(new GridLayout(3, 1));
         JPanel jpAuxJogadorAutor = new JPanel(new FlowLayout(FlowLayout.LEFT));
         JPanel jpAuxJogadorAssist = new JPanel(new FlowLayout(FlowLayout.LEFT));
         JPanel jpAuxTempo = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         JLabel jlInstanteTempo = new JLabel("Instante de Tempo:");
         UtilView.alinhaLabel(jlInstanteTempo);
         JLabel jlJogador = new JLabel("Jogador Autor:");
         UtilView.alinhaLabel(jlJogador);
         JLabel jlJogadorAssist = new JLabel("Jogador Assistente: ");
         UtilView.alinhaLabel(jlJogadorAssist);
         JLabel jlTipo = new JLabel("Tipo:");
         UtilView.alinhaLabel(jlTipo);
         JLabel jlModo = new JLabel("Modo:");
         UtilView.alinhaLabel(jlModo);
 
         jtfInstanteTempoMin = new JTextField(4);
         JLabel jlMin = new JLabel("min");
         jtfInstateTempoSeg = new JTextField(4);
         JLabel jlSeg = new JLabel("seg");
 
         jtfJogador = new JTextField(10);
         jtfJogador.setEditable(false);
 
         jtfJogadorAssist = new JTextField(10);
         jtfJogadorAssist.setEditable(false);
 
         jrbTipoAFavor = new JRadioButton("A Favor");
         jrbTipoAFavor.setActionCommand("A Favor");
         jrbTipoAFavor.setSelected(true);
 
         jrbTipoAFavor.addItemListener(new ItemListener()
         {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (jrbTipoAFavor.isSelected())
                 {
                     if (!jtfJogador.getText().equals(""))
                     {
                         jbPesqJogadorAssist.setEnabled(true);
                     }                    
                     jrbModoComum.setSelected(true);
                     jrbModoFalta.setEnabled(true);
                     jrbModoPenalti.setEnabled(true);
                 }
             }
         });
 
         jrbTipoContra = new JRadioButton("Contra");
         jrbTipoContra.setActionCommand("Contra");
         jrbTipoContra.addItemListener(new ItemListener()
         {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (jrbTipoContra.isSelected())
                 {
                     jbPesqJogadorAssist.setEnabled(false);
                     jrbModoComum.setSelected(true);
                     jrbModoFalta.setEnabled(false);
                     jrbModoPenalti.setEnabled(false);
                     jtfJogadorAssist.setText("");
                 }
             }
         });
 
         jrbModoComum = new JRadioButton("Comum");
         jrbModoComum.setActionCommand("Comum");
         jrbModoComum.setSelected(true);
         jrbModoComum.addItemListener(new ItemListener()
         {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (jrbModoComum.isSelected() && jrbTipoAFavor.isSelected() && !jtfJogador.getText().equals(""))
                 {
                     jbPesqJogadorAssist.setEnabled(true);
                 }
             }
         });
 
         jrbModoFalta = new JRadioButton("Falta");
         jrbModoFalta.setActionCommand("Falta");
         jrbModoFalta.addItemListener(new ItemListener()
         {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (jrbModoFalta.isSelected())
                 {
                     jbPesqJogadorAssist.setEnabled(false);
                     jtfJogadorAssist.setText("");
                 }
             }
         });
 
         jrbModoPenalti = new JRadioButton("Pênalti");
         jrbModoPenalti.setActionCommand("Penalti");
         jrbModoPenalti.addItemListener(new ItemListener()
         {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (jrbModoPenalti.isSelected())
                 {
                     jbPesqJogadorAssist.setEnabled(false);
                     jtfJogadorAssist.setText("");
                 }
             }
         });
 
         JButton jbRegistrarGol = new JButton("Registrar Gol");
         jbRegistrarGol.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 registrarGol();
             }
         });
 
         JButton jbPesqJogadorAutor = new JButton(SGCMFIcons.PESQUISAR);
         UtilView.ajustarTamanhoBotaoPesquisar(jbPesqJogadorAutor);
         
         jbPesqJogadorAssist = new JButton(SGCMFIcons.PESQUISAR);
         jbPesqJogadorAssist.setEnabled(false);
         UtilView.ajustarTamanhoBotaoPesquisar(jbPesqJogadorAssist);
         jbPesqJogadorAutor.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 selecaoJogadorAutor = true;
                 ativaTelaBuscarJogador();
             }
         });
         jbPesqJogadorAssist.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 ativaTelaBuscarJogadorMesmaSelecao();
             }
         });
 
         bgTipo = new ButtonGroup();
         bgTipo.add(jrbTipoAFavor);
         bgTipo.add(jrbTipoContra);
 
         bgModo = new ButtonGroup();
         bgModo.add(jrbModoComum);
         bgModo.add(jrbModoFalta);
         bgModo.add(jrbModoPenalti);
 
         formPanel.add(UtilView.putComponentInFlowLayoutPanel(jlInstanteTempo));
         jpAuxTempo.add(jtfInstanteTempoMin);
         jpAuxTempo.add(jlMin);
         jpAuxTempo.add(jtfInstateTempoSeg);
         jpAuxTempo.add(jlSeg);
         formPanel.add(jpAuxTempo);
 
         formPanel.add(UtilView.putComponentInFlowLayoutPanel(jlJogador));
         jpAuxJogadorAutor.add(jtfJogador);
         jpAuxJogadorAutor.add(jbPesqJogadorAutor);
         formPanel.add(jpAuxJogadorAutor);
 
         formPanel.add(UtilView.putComponentInFlowLayoutPanel(jlTipo));
         panelJRBTipo.add(jrbTipoAFavor);
         panelJRBTipo.add(jrbTipoContra);
         formPanel.add(panelJRBTipo);
 
         formPanel.add(UtilView.putComponentInFlowLayoutPanel(jlModo));
         panelJRBModo.add(jrbModoComum);
         panelJRBModo.add(jrbModoFalta);
         panelJRBModo.add(jrbModoPenalti);
         formPanel.add(panelJRBModo);
 
         formPanel.add(UtilView.putComponentInFlowLayoutPanel(jlJogadorAssist));
         jpAuxJogadorAssist.add(jtfJogadorAssist);
         jpAuxJogadorAssist.add(jbPesqJogadorAssist);
         formPanel.add(jpAuxJogadorAssist);
 
         mainPanel.add(formPanel, BorderLayout.CENTER);
         mainPanel.add(UtilView.putComponentInFlowLayoutPanel(jbRegistrarGol), BorderLayout.SOUTH);
 
         return mainPanel;
     }
 
     private void registrarGol()
     {
         String tipo;
         String modo;
         ResultadoOperacao result;
 
         tipo = bgTipo.getSelection().getActionCommand();
         modo = bgModo.getSelection().getActionCommand();
 
         result = ctrComiteGestor.getCtrGol().registraGol(jtfInstanteTempoMin.getText(),
                                     jtfInstateTempoSeg.getText(),
                                     limGerenciarOcorrencias.getIdJogo(),
                                     jtfJogador.getText(),
                                     jtfJogadorAssist.getText(),
                                     tipo,
                                     modo);
 
         if (result.getTipo() == TipoResultadoOperacao.EXITO)
         {
             JOptionPane.showMessageDialog(this, result.getMsg(), "Êxito!", JOptionPane.INFORMATION_MESSAGE);
 
             setVisible(false);
             resetCamposInterface();
             limGerenciarOcorrencias.preencheTabelaGol();
         }
         else
         {
             JOptionPane.showMessageDialog(this, result.getMsg(), "Erro!", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     private void ativaTelaBuscarJogador()
     {
         limBuscarJogador.ativaTelaSelecionaJogador(this, limGerenciarOcorrencias.getIdJogo());
     }
     
     private void ativaTelaBuscarJogadorMesmaSelecao()
     {
         Short idSelecao;
         Short idJogadorAutor;
         
         idJogadorAutor = Short.parseShort(jtfJogador.getText());
         idSelecao = ctrComiteGestor.getCtrJogador().queryIdSelecaoJogador(idJogadorAutor);
         limBuscarJogador.ativaTelaSelecionaOutroJogadorSelecao(this, limGerenciarOcorrencias.getIdJogo(), idSelecao, idJogadorAutor);
     }
 
     private void resetCamposInterface()
     {
         jtfInstanteTempoMin.setText("");
         jtfInstateTempoSeg.setText("");
         jtfJogador.setText("");
         jtfJogadorAssist.setText("");
         jrbTipoAFavor.setSelected(true);
         jrbModoComum.setSelected(true);
         jbPesqJogadorAssist.setEnabled(false);
     }
 
     @Override
     public void jogadorSelecionado(Short idJogador)
     {
         if (selecaoJogadorAutor)
         {
             jtfJogador.setText(idJogador + "");
             selecaoJogadorAutor = false;
             if (jrbTipoAFavor.isSelected() && jrbModoComum.isSelected())
             {
                 jbPesqJogadorAssist.setEnabled(true);
            } 
            jtfJogadorAssist.setText("");
         }
         else
         {
             jtfJogadorAssist.setText(idJogador + "");
         }
     }
 }
