 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sgcmf.view.tecnico;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.JDialog;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import sgcmf.control.CtrTecnico;
 import sgcmf.model.other.SGCMFIcons;
 
 /**
  *
  * @author Helio
  */
 public class LimGerenciarJogador extends JDialog
 {
     private PanelCadastrarJogador pCadastrarJogador;
     private PanelAlterarJogador pAlterarJogador;
     private PanelRemoverJogador pRemoverJogador;
     private PanelConsultarJogador pConsultarJogador;
     private JTabbedPane jtp;
 
     public LimGerenciarJogador(CtrTecnico ctrTecnico)
     {
         setIconImage(SGCMFIcons.JOGADOR.getImage());
         pCadastrarJogador = new PanelCadastrarJogador(ctrTecnico);
         pAlterarJogador = new PanelAlterarJogador(ctrTecnico);
         pRemoverJogador = new PanelRemoverJogador(ctrTecnico);
         pConsultarJogador = new PanelConsultarJogador(ctrTecnico);
         setTitle("Gerenciar Jogadores");
         setResizable(false);
         setModal(true);
         setDefaultCloseOperation(HIDE_ON_CLOSE);
 
         add(montaPainel());
 
         addWindowListener(new WindowAdapter()
         {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 pCadastrarJogador.limparCampos();
                 pConsultarJogador.limparCampos();
                 pAlterarJogador.limparTodosCampos();
                 jtp.setSelectedIndex(0);
                 setLocationRelativeTo(null);
             }
         });
     }
 
     private JTabbedPane montaPainel()
     {
         jtp = new JTabbedPane();
         jtp.addChangeListener(new ChangeListener()
         {
             @Override
             public void stateChanged(ChangeEvent e)
             {
                 String tituloAba = jtp.getTitleAt(jtp.getSelectedIndex());
                 if (tituloAba.equals("Cadastrar"))
                 {
                     setSize(380, 335);
                 }
                 else if (tituloAba.equals("Alterar"))
                 {
                     setSize(700, 400);
                     pAlterarJogador.ativaTela();

                 }
                 else if (tituloAba.equals("Remover"))
                 {
                     setSize(700, 400);
                     pRemoverJogador.ativaTela();
                 }
                 else if (tituloAba.equals("Consultar"))
                 {
                     setSize(700, 400);
                     pConsultarJogador.ativaTela();
                 }
                pAlterarJogador.limparTodosCampos();
                pConsultarJogador.limparCampos();
                pRemoverJogador.limparTodosCampos();
                 setLocationRelativeTo(null);
             }
         });
 
         jtp.add(pCadastrarJogador, "Cadastrar");
         jtp.add(pAlterarJogador, "Alterar");
         jtp.add(pRemoverJogador, "Remover");
         jtp.add(pConsultarJogador, "Consultar");
 
         return jtp;
     }
 }
