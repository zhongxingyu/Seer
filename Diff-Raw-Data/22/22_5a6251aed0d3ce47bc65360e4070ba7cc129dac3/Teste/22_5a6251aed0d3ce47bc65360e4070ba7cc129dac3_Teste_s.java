 package gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import dados.*;
 import jogosolimpicos.*;
 import listaligada.ListaLigada;
 
 @SuppressWarnings("serial")
 public class Teste extends JFrame {
 
 	public Teste(String titulo) {
 
 		super(titulo);
 		setLayout(new BorderLayout());
 
 		Imagens img = new Imagens();
 
 		JMenuBar menuBar;
 		JMenu menu;
 		JMenuItem menuItem;
 		final Csv csv = new Csv();
 
 		menuBar = new JMenuBar();
 
 		menu = new JMenu("File");
 		menu.setMnemonic('F');
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("Import Country", 'I');
 		menuItem.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 
 				csv.importPais(Teste.this, Main.getPaises());
 			}
 		});
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Export Country", 'E');
 		menuItem.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 
 				csv.exportPais(Teste.this, Main.getPaises());
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("List Country", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				for (int i = 0; i < Main.getPaises().size(); i++) {
 					System.out.println(Main.getPaises().get(i));
 				}
 
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Import Competitions", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				csv.importDisc(Teste.this, Main.getDisciplinas(), Main.getModalidades());
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Export Competitions", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				csv.exportDisciplina(Teste.this, Main.getModalidades());
 
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("List Competitions", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				for (int i = 0; i < Main.getDisciplinas().size(); i++) {
 					System.out.println(Main.getDisciplinas().get(i).toStringTest());
 				}
 
 				for (int i = 0; i < Main.getModalidades().size(); i++) {
 					System.out.println(Main.getModalidades().get(i).toStringTest());
 				}
 
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Import Results", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				csv.importResultados(Teste.this, Main.getAtleta(), Main.getModalidades(), Main.getPaises(), Main.getProvas(), Main.getEquipas(), Main.getJogos());
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("List Results", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int itModal = 0;
 
 				for (; itModal < Main.getModalidades().size(); itModal++) {
 					if (Main.getModalidades().get(itModal).getNome().equals("Gymnastics"))
 						break;
 				}
 
 				ListaLigada<Prova> provas = Main.getProvas();
 
 				for (int i = 0; i < Main.getModalidades().get(itModal).getDisc().size(); i++) {
 					if (!Main.getModalidades().get(itModal).getDisc().get(i).getTipoMod())
 						for (int j = 0; j < provas.size(); j++) {
 
 							if (Main.getModalidades().get(itModal).getDisc().get(i).getNome().equals(provas.get(j).getDisciplina().getNome()) && Main.getModalidades().get(itModal).getNome().equals(provas.get(j).getDisciplina().getModalidade().getNome())) {
 
 								if (provas.get(j) instanceof ProvaInd) {
 
									System.out.println(Main.getModalidades().get(itModal).getDisc().get(i).getNome());
 									for (int k = 0; k < ((ProvaInd) Main.getProvas().get(j)).getResultados().size(); k++) {
 										System.out.println(((ProvaInd) Main.getProvas().get(j)).getResultados().get(k).getAtleta().getNome() + " : " + ((ProvaInd) Main.getProvas().get(j)).getResultados().get(k).getResulTemp());
 									}
 									provas.remove(j);
 									j--;
 								}
 
 							}
 						}
 				}
 
 				for (int i = 0; i < Main.getModalidades().get(itModal).getDisc().size(); i++) {
 					if (Main.getModalidades().get(itModal).getDisc().get(i).getTipoMod())
 						for (int j = 0; j < Main.getProvas().size(); j++) {
 
 							if (Main.getModalidades().get(itModal).getDisc().get(i).getNome().equals(Main.getProvas().get(j).getDisciplina().getNome()) && Main.getModalidades().get(itModal).getNome().equals(Main.getProvas().get(j).getDisciplina().getModalidade().getNome())) {
 
 								if (Main.getProvas().get(j) instanceof ProvaCol) {
									System.out.println(Main.getModalidades().get(itModal).getDisc().get(i).getNome());
 
 									for (int k = 0; k < ((ProvaCol) Main.getProvas().get(j)).getResultados().size(); k++) {
 										System.out.println(((ProvaCol) Main.getProvas().get(j)).getResultados().get(k).getEquipa().getPais().getCodigoPais(2008) + " : " + ((ProvaCol) Main.getProvas().get(j)).getResultados().get(k).getResulTemp());
 									}
 									provas.remove(j);
 									j--;
 								}
 
 							}
 						}
 				}
 
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Export Results", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				csv.exportResultados(Teste.this, Main.getModalidades(), Main.getProvas(), "Gymnastics", 1, 2008);
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Import Provas", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				csv.importProvas(Teste.this, Main.getJogos(), Main.getProvas(), Main.getDisciplinas(), Main.getModalidades());
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Export Provas", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				csv.exportProvas(Teste.this, Main.getProvas(), 2008);
 			}
 		});
 
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("List Provas", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				for (int i = 0; i < Main.getProvas().size(); i++) {
 					System.out.println(Main.getProvas().get(i).toStringTest());
 				}
 
 			}
 		});
 
 		menu.add(menuItem);
 
 		JMenu listar = new JMenu("Listar");
 
 		menuItem = new JMenuItem("Listar", 'L');
 		menuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				TesteListagens.listarMedalhas(Main.getPaises(), Main.getProvas(), 1996, 2008);
 			}
 		});
 
 		listar.add(menuItem);
 		menuBar.add(listar);
 
 		setJMenuBar(menuBar);
 
 		JLabel label = new JLabel(new ImageIcon(img.constrution));
 		label.setEnabled(false);
 		label.setDisabledIcon(new ImageIcon(img.constrution));
 		add(label);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setSize(400, 300);
 		pack();
 		setResizable(true);
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 }
