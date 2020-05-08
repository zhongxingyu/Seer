 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import util.PropertiesFile;
 import Base.TipoPopUp;
 import Exception.PastaDeMusicaNaoExisteException;
 import Exception.PastaDeMusicaVaziaException;
 
 @SuppressWarnings("serial")
 public class TelaPrincipal extends JFrame {
 
 	public TelaPrincipal() {
 		initframe();
 	}
 
 	/**
 	 * Inicializa componentes visuais
 	 */
 	private void initframe() {
 		// centralizando o Frame
 		this.setSize(ConstantesUI.LARGURA_TELA, ConstantesUI.ALTURA_TELA);
 		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (int) (((dimension.getWidth() / 2) - (this.getWidth() / 2)));
 		int y = (int) (((dimension.getHeight() / 2) - (this.getHeight() / 2)));
 		this.setLocation(x, y);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLayout(new BorderLayout());
 		this.setTitle(ConstantesUI.TITULO);
 
 		// Barra de Menu
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menuFile = new JMenu(ConstantesUI.MENU_FILE);
 		JMenuItem menuItemAbrir = new JMenuItem(ConstantesUI.MENU_ITEM_OPEN);
 		
 		final JMenu menuSettings = new JMenu(ConstantesUI.MENU_SETTINGS);
 		JMenuItem menuItemSetMusicFolder = new JMenuItem(ConstantesUI.MENU_ITEM_SET_MUSIC_FOLDER);
 		
 		String musicFolder = PropertiesFile.getProperties();
 		final JLabel labelMusicFolder = new JLabel("   "+musicFolder);
 		labelMusicFolder.setEnabled(true);
 		
 		try {
 			PropertiesFile.verifyMusicFolder(musicFolder);
 			labelMusicFolder.setForeground(ConstantesUI.COR_DESABILITADO);
 			menuSettings.setForeground(ConstantesUI.COR_PRETO);
 		} catch (PastaDeMusicaNaoExisteException e) {
 			labelMusicFolder.setForeground(ConstantesUI.COR_VERMELHO);
 			menuSettings.setForeground(ConstantesUI.COR_VERMELHO);
 		} catch (PastaDeMusicaVaziaException e) {
 			labelMusicFolder.setForeground(ConstantesUI.COR_VERMELHO);
 			menuSettings.setForeground(ConstantesUI.COR_VERMELHO);
 		}
 		
 		menuFile.add(menuItemAbrir);
 		menuSettings.add(menuItemSetMusicFolder);
 		menuSettings.add(labelMusicFolder);
 		menuBar.add(menuFile);
 		menuBar.add(menuSettings);
 		
 		// Set up do layout
 		JPanel painelCentral = new JPanel();
 		painelCentral.setLayout(new BorderLayout());
 
 		JPanel painelNorte = new JPanel();
 		painelNorte.setLayout(new GridLayout(0, 2));
 		painelCentral.add(painelNorte, BorderLayout.NORTH);
 
 		JPanel painelEsquerda = new JPanel();
 		painelEsquerda.setLayout(new BorderLayout());
 		
 
 		final PainelTagsGerais painelTagsGerais = new PainelTagsGerais();
 		painelNorte.add(painelEsquerda);
 
 		JPanel painelDireita = new JPanel();
 		painelDireita.setLayout(new BorderLayout());
 		painelNorte.add(painelDireita);
 
 		JPanel painelSul = new JPanel();
 		painelSul.setLayout(new BorderLayout());
 		painelCentral.add(painelSul, BorderLayout.CENTER);
 
 		final PainelFaixas painelFaixas = new PainelFaixas();
 		painelSul.add(painelFaixas, BorderLayout.NORTH);
 
 		PainelBotoes painelBotoes = new PainelBotoes();
 		painelDireita.add(painelBotoes, BorderLayout.EAST);
 
 		final PainelImagem painelImagem = new PainelImagem();
 		painelEsquerda.add(painelImagem, BorderLayout.WEST);
 
 		final PainelSelecaoImagem painelSelecaoImagem = new PainelSelecaoImagem(painelImagem);
		
		painelEsquerda.add(painelSelecaoImagem, BorderLayout.CENTER);
		painelDireita.add(painelTagsGerais, BorderLayout.CENTER);
 		
 		menuItemAbrir.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new DialogOpenDisco(painelTagsGerais, painelFaixas, painelImagem, painelSelecaoImagem);
 			}
 		});
 		
 		menuItemSetMusicFolder.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new DialogSetMusicFolder();
 				new PopUp(ConstantesUI.POPUP_DIRETORIO_DE_MUSICA_SALVO, TipoPopUp.INFO);
 				labelMusicFolder.setForeground(ConstantesUI.COR_DESABILITADO);
 				labelMusicFolder.setText("   "+PropertiesFile.getProperties());
 				menuSettings.setForeground(ConstantesUI.COR_PRETO);
 			}
 		});
 		
 		this.setJMenuBar(menuBar);
 		JScrollPane scrollPane = new JScrollPane(painelCentral,
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		this.add(scrollPane, BorderLayout.CENTER);
 
 		this.setVisible(true);
 		
 	}
 
 }
