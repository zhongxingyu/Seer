 package pig;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 public class AtorJogador extends JFrame {
 
 	private static final long serialVersionUID = -8232011527107568444L;
 	
 	protected static final String[] MENSAGENS = {"Passou a vez!","Boa jogada!","Perdeu os pontos!","Perdeu tudo!","Começou!"};
 	private JPanel contentPane;
 	private Tabuleiro tab;
 	private AtorNetGames atorNetGames;
 
 	protected ImageIcon dado1 = new ImageIcon(AtorJogador.class.getResource("/assets/dado_1.png"));
 	protected ImageIcon dado2 = new ImageIcon(AtorJogador.class.getResource("/assets/dado_2.png"));
 	protected ImageIcon dado3 = new ImageIcon(AtorJogador.class.getResource("/assets/dado_3.png"));
 	protected ImageIcon dado4 = new ImageIcon(AtorJogador.class.getResource("/assets/dado_4.png"));
 	protected ImageIcon dado5 = new ImageIcon(AtorJogador.class.getResource("/assets/dado_5.png"));
 	protected ImageIcon dado6 = new ImageIcon(AtorJogador.class.getResource("/assets/dado_6.png"));
 	protected ImageIcon[] dados = {null,dado1,dado2,dado3,dado4,dado5,dado6};
 	protected JMenuBar menuBar = new JMenuBar();
 	protected JMenu mnPartida = new JMenu("Partida");
 	protected JMenuItem mntmConectar = new JMenuItem("Conectar");
 	protected JMenuItem mntmIniciarPartida = new JMenuItem("Iniciar Partida");
 	protected JMenuItem mntmDesconectar = new JMenuItem("Desconectar");
 	protected JSeparator separator = new JSeparator();
 	protected JSeparator separator_1 = new JSeparator();
 	protected JLabel lblJogador1 = new JLabel();
 	protected JLabel lblJogador2 = new JLabel();
 	protected JLabel lblDado1 = new JLabel();
 	protected JLabel lblDado2 = new JLabel();
 	protected JButton btnJogarDado1 = new JButton("Jogar dado");
 	protected JButton btnJogarDado2 = new JButton("Jogar dado");
 	protected JButton btnPassarAVez1 = new JButton("Passar a vez");
 	protected JButton btnPassarAVez2 = new JButton("Passar a vez");
 	protected JLabel lblPointNumTot1 = new JLabel();
 	protected JLabel lblPointTot1 = new JLabel("Pontuação total");
 	protected JLabel lblPointNumVez1 = new JLabel();
 	protected JLabel lblPointVez1 = new JLabel("Pontuação da vez");
 	protected JLabel lblPig1 = new JLabel(new ImageIcon(AtorJogador.class.getResource("/assets/Porco.png")));
 	protected JLabel lblPointNumTot2 = new JLabel();
 	protected JLabel lblPointTot2 = new JLabel("Pontuação total");
 	protected JLabel lblPointNumVez2 = new JLabel();
 	protected JLabel lblPointVez2 = new JLabel("Pontuação da vez");
 	protected JLabel label = new JLabel(new ImageIcon(AtorJogador.class.getResource("/assets/PorcoAzul.png")));
 	protected JLabel lblJogador = new JLabel("Jogador:");
 	protected JLabel lblNmJogadorVez = new JLabel();
 	protected JLabel lblInformacao = new JLabel();
 
 	protected SobreDialog dialogSobre;
 
 	protected ComoJogarDialog dialogComo;
 	
 	/**
 	 * Create the frame.
 	 */
 	public AtorJogador() {
 				
 		atorNetGames = new AtorNetGames(this);
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 800, 600);
 
 		setJMenuBar(menuBar);
 		menuBar.add(mnPartida);
 		
 		mntmConectar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				atorNetGames.conectar();
 			}
 		});
 		mnPartida.add(mntmConectar);
 		
 		mntmIniciarPartida.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				atorNetGames.iniciarPartidaRede();
 			}
 		});
 		mnPartida.add(mntmIniciarPartida);
 		
 		mntmDesconectar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				atorNetGames.desconectar();
 			}
 		});
 		mnPartida.add(mntmDesconectar);
 		
 		JMenu mnSobre = new JMenu("Ajuda");
 		menuBar.add(mnSobre);
 		
 		JMenuItem mntmSobre = new JMenuItem("Sobre");
 		mntmSobre.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dialogSobre = new SobreDialog();
 				dialogSobre.setModal(true);
 				dialogSobre.pack();
 				dialogSobre.setVisible(true);				
 			}
 		});
 		mnSobre.add(mntmSobre);
 		
 		JMenuItem mntmAjuda = new JMenuItem("Como jogar");
 		mntmAjuda.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dialogComo = new ComoJogarDialog();
 				dialogComo .setModal(true);
 				dialogComo .pack();
 				dialogComo .setVisible(true);				
 				
 			}
 		});
 		mnSobre.add(mntmAjuda);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		separator.setBounds(200, 12, 2, 561);
 		separator.setOrientation(SwingConstants.VERTICAL);
 		contentPane.add(separator);
 		
 		separator_1.setOrientation(SwingConstants.VERTICAL);
 		separator_1.setBounds(600, 12, 2, 561);
 		contentPane.add(separator_1);
 		
 		lblJogador1.setFont(new Font("Dialog", Font.BOLD, 18));
 		lblJogador1.setBounds(58, 17, 82, 32);
 		contentPane.add(lblJogador1);
 		
 		lblJogador2.setFont(new Font("Dialog", Font.BOLD, 18));
 		lblJogador2.setBounds(644, 27, 98, 22);
 		contentPane.add(lblJogador2);
 		
 		lblDado1.setBounds(237, 148, 144, 145);
 		contentPane.add(lblDado1);
 		
 		lblDado2.setBounds(418, 148, 144, 145);
 		contentPane.add(lblDado2);
 		btnJogarDado1.setFont(new Font("Dialog", Font.BOLD, 20));
 		
 		btnJogarDado1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (tab.jogadorLocal.getTipo() == TipoPorco.VERDE) {
 					procederLance(true);
 				} else {
 					MensagemUtil.mostrarErro("Você só pode utilizar os seus botões!", null);
 				}
 			}
 		});
 		btnJogarDado1.setBounds(12, 70, 176, 48);
 		contentPane.add(btnJogarDado1);
 		btnJogarDado2.setFont(new Font("Dialog", Font.BOLD, 20));
 		btnJogarDado2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (tab.jogadorLocal.getTipo() == TipoPorco.AZUL) {
 					procederLance(true);
 				} else {
 					MensagemUtil.mostrarErro("Você só pode utilizar os seus botões!", null);
 				}
 			}
 		});
 		btnJogarDado2.setBounds(610, 70, 176, 48);
 		contentPane.add(btnJogarDado2);
 		btnPassarAVez1.setFont(new Font("Dialog", Font.BOLD, 19));
 		btnPassarAVez1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (tab.jogadorLocal.getTipo() == TipoPorco.VERDE) {
 					procederLance(false);
 				} else {
 					MensagemUtil.mostrarErro("Você só pode utilizar os seus botões!", null);
 				}
 			}
 		});
 		
 		btnPassarAVez1.setBounds(12, 141, 176, 48);
 		contentPane.add(btnPassarAVez1);
 		btnPassarAVez2.setFont(new Font("Dialog", Font.BOLD, 19));
 		btnPassarAVez2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (tab.jogadorLocal.getTipo() == TipoPorco.AZUL) {
 					procederLance(false);
 				} else {
 					MensagemUtil.mostrarErro("Você só pode utilizar os seus botões!", null);
 				}
 			}
 		});
 		
 		btnPassarAVez2.setBounds(610, 141, 176, 48);
 		contentPane.add(btnPassarAVez2);
 		
 		lblPointNumTot1.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPointNumTot1.setFont(new Font("Dialog", Font.BOLD, 30));
 		lblPointNumTot1.setBounds(66, 201, 60, 48);
 		contentPane.add(lblPointNumTot1);
 		
 		lblPointTot1.setBounds(40, 243, 120, 15);
 		contentPane.add(lblPointTot1);
 		
 		lblPointNumVez1.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPointNumVez1.setFont(new Font("Dialog", Font.BOLD, 30));
 		lblPointNumVez1.setBounds(66, 261, 60, 48);
 		contentPane.add(lblPointNumVez1);
 		
 		lblPointVez1.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPointVez1.setBounds(27, 296, 148, 15);
 		contentPane.add(lblPointVez1);
 		
 		lblPig1.setBounds(12,321,170,236);
 		contentPane.add(lblPig1);
 		lblPointNumTot2.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPointNumTot2.setFont(new Font("Dialog", Font.BOLD, 30));
 		lblPointNumTot2.setBounds(659, 201, 60, 48);
 		contentPane.add(lblPointNumTot2);
 		
 		lblPointTot2.setBounds(633, 243, 120, 15);
 		contentPane.add(lblPointTot2);
 		
 		lblPointNumVez2.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPointNumVez2.setFont(new Font("Dialog", Font.BOLD, 30));
 		lblPointNumVez2.setBounds(659, 261, 60, 48);
 		contentPane.add(lblPointNumVez2);
 		
 		lblPointVez2.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPointVez2.setBounds(620, 296, 148, 15);
 		contentPane.add(lblPointVez2);
 		
 		label.setBounds(616, 321, 170, 236);
 		contentPane.add(label);
 		
 		lblJogador.setBounds(237, 27, 70, 15);
 		contentPane.add(lblJogador);
 		
 		lblNmJogadorVez.setBounds(311, 27, 70, 15);
 		contentPane.add(lblNmJogadorVez);
 		
 		lblInformacao.setForeground(new Color(178, 34, 34));
 		lblInformacao.setHorizontalAlignment(SwingConstants.CENTER);
 		lblInformacao.setFont(new Font("Dialog", Font.BOLD, 29));
 		lblInformacao.setBounds(255, 87, 307, 31);
 		contentPane.add(lblInformacao);
 	}
 	
 	protected void procederLance(boolean realizarJogada) {
 		
 		int situacao = tab.procederLance(realizarJogada);
 		
 		int numMensagem = tab.tratarLance(tab.pontuacaoLance, tab.dado1.getValor(), tab.dado2.getValor(), situacao, false);
 		
 		int pontuacaoLance = tab.getPontuacaoLance();
 		int valorDado1 = tab.dado1.getValor();
 		int valorDado2 = tab.dado2.getValor();
 		
 		switch(situacao) {
 		
 		case Situacao.PASSOU:
 			tab.setPontuacaoLance(0);
 			atorNetGames.enviarJogadaRede(0, valorDado1, valorDado2,  false, situacao);
 			break;
 			
 		case Situacao.JOGADA:
 			atorNetGames.enviarJogadaRede(pontuacaoLance, valorDado1, valorDado2,  false, situacao);
 			break;
 			
 		case Situacao.PERDEU:
 			atorNetGames.enviarJogadaRede(pontuacaoLance, valorDado1, valorDado2,  true, situacao);
 			break;
 			
 		case Situacao.PERDEU_TUDO:
 			atorNetGames.enviarJogadaRede(pontuacaoLance, valorDado1, valorDado2,  true, situacao);
 			break;
 			
 		default :
 			break;
 		}
 		
 		boolean haVencedor = tab.verificaVencedor(); 
 		
 		if(haVencedor){
 			
 			boolean localVenceu = tab.jogadorLocal.isVencedor(); 
 			if (localVenceu){
 				MensagemUtil.mostrar("Parabéns! Você venceu a partida!",this);
 			} else {
 				MensagemUtil.mostrar("Infelizmente você perdeu a partida.",this);
 			}
 			this.limparTela();
 		} else {
 			this.atualizar(numMensagem);
 		}
 	}
 
 	public void iniciarPartidaRede(boolean iniciarComoSolicitante) {
 		if (tab == null) {
 			tab = new Tabuleiro();
 		}
 		
 		String nome1;
 		nome1 = atorNetGames.obtemNomeJogador();
 		
 		String nome2;
 		nome2= atorNetGames.obtemNomeAdversario();
 		
 		if(iniciarComoSolicitante) {
 			tab.criarJogador(nome1, TipoPorco.VERDE); // porco Verde
 			tab.criarJogador(nome2, TipoPorco.AZUL); // porco Azul
 			lblJogador1.setText(tab.getJogadorLocal().getNome());
 			lblJogador2.setText(tab.getJogadorRemoto().getNome());
 		} else {
 			tab.criarJogador(nome1, TipoPorco.AZUL); // porco Azul
 			tab.criarJogador(nome2, TipoPorco.VERDE); // porco Verde
 			lblJogador1.setText(tab.getJogadorRemoto().getNome());
 			lblJogador2.setText(tab.getJogadorLocal().getNome());
 		}
 		
 		tab.definirOPrimeiro(iniciarComoSolicitante);
 
 		tab.iniciar();
 		
 		this.atualizar(4); // 4 - mensagem de inicio de partida
 		
 	}
 
 	private void atualizar(int numMensagem) {
 		
 		Jogador daVez = tab.informarJogadorDaVez();
 		
 		lblDado1.setIcon(dados[tab.dado1.getValor()]);
 		lblDado2.setIcon(dados[tab.dado2.getValor()]);
 		
 		lblInformacao.setText(MENSAGENS[numMensagem]);
 		
 		lblNmJogadorVez.setText(daVez.getNome());
 		if(daVez.getTipo() == TipoPorco.VERDE) {              // porco Verde
 			btnJogarDado1.setEnabled(true);
 			btnJogarDado2.setEnabled(false);
 			btnPassarAVez1.setEnabled(true);
 			btnPassarAVez2.setEnabled(false);
 			lblPointNumVez1.setText(String.valueOf(tab.getPontuacaoLance()));
 			lblPointNumVez2.setText("0");
 		} else {                                // porco Azul
 			btnJogarDado1.setEnabled(false);
 			btnJogarDado2.setEnabled(true);
 			btnPassarAVez1.setEnabled(false);
 			btnPassarAVez2.setEnabled(true);
 			lblPointNumVez2.setText(String.valueOf(tab.getPontuacaoLance()));
 			lblPointNumVez1.setText("0");
 		}
 		
 		if (tab.getJogadorLocal().getTipo() == TipoPorco.VERDE) {
 			lblPointNumTot1.setText(String.valueOf(tab.getJogadorLocal().getPontuacao()));
 			lblPointNumTot2.setText(String.valueOf(tab.getJogadorRemoto().getPontuacao()));			
 		} else {
 			lblPointNumTot1.setText(String.valueOf(tab.getJogadorRemoto().getPontuacao()));
 			lblPointNumTot2.setText(String.valueOf(tab.getJogadorLocal().getPontuacao()));			
 		}
 		
 	}
 
 	public void efetuarJogadaRede(int pontuacaoDaVez, int dado1, int dado2, boolean passou, int situacao) {
 		
 		int numMensagem = tab.tratarLance(pontuacaoDaVez, dado1, dado2, situacao, true);
 		boolean vencedor = tab.verificaVencedor(); 
 		if(vencedor){
 			boolean localVenceu = tab.jogadorLocal.isVencedor(); 
 			if (localVenceu){
 				MensagemUtil.mostrar("Parabéns! Você venceu a partida!", this);
 			} else {
 				MensagemUtil.mostrar("Infelizmente você perdeu a partida.", this);
 			}
 			this.limparTela();
 		} else {
 			this.atualizar(numMensagem);
 		}
 	}
 
 	private void limparTela() {
 		
 		lblPointNumTot1.setText("");
 		lblPointNumTot2.setText("");
 		lblPointNumVez2.setText("");
 		lblPointNumVez1.setText("");
 		lblJogador1.setText("");
 		lblJogador2.setText("");
 		lblNmJogadorVez.setText("");
 		lblDado1.setIcon(null);
 		lblDado2.setIcon(null);
 		lblInformacao.setText("");
 	}
 	
 	public void terminarPartidaEmAndamento() {
 		
 		this.limparTela();
 		tab.setPartidaEmAndamento(false);
 	}
 }
