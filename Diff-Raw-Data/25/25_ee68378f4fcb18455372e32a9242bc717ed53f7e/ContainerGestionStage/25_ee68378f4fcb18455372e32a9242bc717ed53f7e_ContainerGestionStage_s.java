 package istic.sapfor.client.gui;
 
 import istic.sapfor.client.command.ICommand;
 import istic.sapfor.client.command.ICommandContextKey;
 import istic.sapfor.client.command.impl.DefaultCommandContext;
 import istic.sapfor.client.gui.sapforcomponent.SapforButton;
 import istic.sapfor.client.gui.sapforcomponent.SapforJDialogConnection;
 import istic.sapfor.client.gui.sapforcomponent.SapforJFrameAgent;
 import istic.sapfor.client.gui.sapforcomponent.SapforLabel;
 import istic.sapfor.client.gui.sapforcomponent.SapforListeStage;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class ContainerGestionStage implements IHMGStage {
 
 	private ClassPathXmlApplicationContext context = null;
 	private SapforJFrameAgent frame;
 	private SapforJDialogConnection Jlogin;
 
 	public void showUI(ClassPathXmlApplicationContext ctx) {
 
 		context = ctx;
 		// Partie  modifier car trop lourde, mettre a dans une mthode ou
 		// autre...
 
 		// Cration de la boite de dialogue de connexion.
 		Jlogin = new SapforJDialogConnection();
 
 		// Listener du bouton "connexion"
 		Jlogin.getLog().addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 
 				ICommand cmd = (ICommand) context.getBean("cmdLogin");
 				DefaultCommandContext ctx = new DefaultCommandContext();
 				List<String> login = new LinkedList<String>();
 				System.out.println(Jlogin.getFname().getText());
 				System.out.println(Jlogin.getFpwd().getText());
 				String log = Jlogin.getFname().getText();
 				String pwd = Jlogin.getFpwd().getText();
 				login.add(log);
 				login.add(pwd);
 
 				ctx.put(ICommandContextKey.Key_login, login);
 				cmd.execute(ctx);
 			}
 		});
 
 		// Fentre principale
 
 		frame = new SapforJFrameAgent("page stage dispo");
 		frame.getBts().setPreferredSize(Jlogin.getStd());
 
 		frame.getBts().addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 
 				ICommand cmd = (ICommand) context
 						.getBean("cmdDisplayStageDispo");
 				DefaultCommandContext ctx = new DefaultCommandContext();
 				cmd.execute(ctx);
 
 				System.out.println("OK");
 
 			}
 		});
 
 		//Listener pour l'affichage des stages
 		frame.getButtonAdmin().addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 
 				ICommand cmd = (ICommand) context.getBean("cmdDisplayStageDir");
 				DefaultCommandContext ctx = new DefaultCommandContext();
 				cmd.execute(ctx);

 				System.out.println("OK");
 
 			}
 		});
 
 		frame.getButtonAddAgent().addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 
 				ICommand cmd = (ICommand) context.getBean("cmdDisplayAgent");
 				DefaultCommandContext ctx = new DefaultCommandContext();
 				cmd.execute(ctx);

 				System.out.println("OK");
 
 			}
 		});
 	}
 
 	public void displayStageDir(HashMap<Long, String> stDir) {
 		frame.getPaneStage().removeAll();
 
 		if (stDir == null) {
 			SapforListeStage sdir = new SapforListeStage(
 					"Pas de stage disponible");
 			sdir.getBtu().setVisible(false);
 
 			frame.getPaneStage().add(sdir);
 		}
 
 		else {
 
 			for (Entry<Long, String> entry : stDir.entrySet()) {
 
 				final Long cle = entry.getKey();
 				String valeur = entry.getValue();
 				SapforListeStage sdir = new SapforListeStage(valeur);
 				frame.getPaneStage().add(sdir);
 
 				sdir.getBtu().addMouseListener(new MouseAdapter() {
 					@Override
 					public void mouseClicked(MouseEvent e) {
 
 						String idS = (cle.toString());
 						DefaultCommandContext ctx = new DefaultCommandContext();
 						ctx.put(ICommandContextKey.Key_Stage, idS);
 						ContainerGestionAdmin rootUI = (ContainerGestionAdmin) context
 								.getBean("uiAdmin");
 						rootUI.showUI(context, ctx, frame);
 						frame.setVisible(false);
 
 					}
 				});
 
 			}
 		}
 		frame.getPaneStage().revalidate();
 		frame.getBts().setVisible(true);
 		System.out.println("Essai sans serveur ni BD");
 
 	}// fin displayStageDir
 
 	public void displayStageDispo(HashMap<Long, String> st) {
 		// TODO Auto-generated method stub
 
 		frame.getPaneStage().removeAll();
 
 		if (st == null) {
 			SapforListeStage s = new SapforListeStage("Pas de stage disponible");
 			s.getBtu().setVisible(false);
 
 			frame.getPaneStage().add(s);
 		}
 
 		else {
 
 			for (Entry<Long, String> entry : st.entrySet()) {
 
 				final Long cle = entry.getKey();
 				String valeur = entry.getValue();
 				SapforListeStage s = new SapforListeStage(valeur);
 				frame.getPaneStage().add(s);
 
 				//Listener pour afficher les UV disponibles
 				s.getBtu().addMouseListener(new MouseAdapter() {
 					@Override
 					public void mouseClicked(MouseEvent e) {
 						// String idA = accueilLabel.getText().split(" ")[3];

 						ICommand cmd = (ICommand) context
 								.getBean("cmdDisplayUvDispo");
 						DefaultCommandContext ctx = new DefaultCommandContext();
 
 						List<String> stageDisp = new LinkedList<String>();
 
 						stageDisp.add(cle.toString());
 						ctx.put(ICommandContextKey.Key_Stage, stageDisp);
 
 						cmd.execute(ctx);

 						System.out.println("OK2");
 					}
 				});
 
 			}
 		}
 		frame.getPaneStage().revalidate();
 		frame.getBts().setVisible(false);
 		System.out.println("Essai sans serveur ni BD");
 
 	}// fin displayStageDispo
 
 	@Override
 	public void displayUvDispo(final HashMap<Long, String> uv,
 			final HashMap<Long, String> uvInscrit) {
 
 		// System.out.println("----------------------------------------");
 		// System.out.println(uv+"\n"+uvInscrit);
 		if (uv == null && uvInscrit == null) {
 			SapforLabel nonUv = new SapforLabel("Aucune Uv disponible");
 			frame.getInfoStageUv().add(nonUv);
 		}
 
 		else {
 			// System.out.println("uv && uvInscrit non null");
 			final Map<Integer, JCheckBox> lstbox = new HashMap<Integer, JCheckBox>();
 			Integer i = 0;
 
 			frame.getInfoStageUv().removeAll();
 
 			// frame.getInfoStageUv().setVisible(false);
 			if (uvInscrit == null) {// System.out.println("uvInscrit == null");
 				for (Entry<Long, String> entry : uv.entrySet()) {
 					Long cle = entry.getKey();
 					String valeur = entry.getValue();
 					System.out.println(valeur + " titre UV");
 					SapforLabel nuv = new SapforLabel(valeur);
 					String nom = cle.toString();
 					JCheckBox rad = new JCheckBox();
 					rad.setName(nom);
 
 					// keyset
 					lstbox.put(i, rad);
 					i++;
 
 					frame.getInfoStageUv().add(nuv);
 					frame.getInfoStageUv().add(rad);
 				}
 
 			}
 
 			else {
 				System.out.println("uvInscrit non null");
 				for (Entry<Long, String> entry2 : uv.entrySet()) {
 					if (uvInscrit.containsKey(entry2.getKey()) == true) {
 						// System.out.println("uvInscrit - test boucle");
 						Long cle = entry2.getKey();
 						String valeur = entry2.getValue();
 						System.out.println(valeur + " titre UV if");
 						SapforLabel nuv = new SapforLabel(valeur);
 						String nom = cle.toString();
 						JCheckBox rad = new JCheckBox();
 						rad.setName(nom);
 
 						lstbox.put(i, rad);
 						i++;
 
 						frame.getInfoStageUv().add(nuv);
 						// infoStageUv.add(rad);
 
 					} else {
 						Long cle = entry2.getKey();
 						String valeur = entry2.getValue();
 						// System.out.println(valeur + " titre UV else");
 						SapforLabel nuv = new SapforLabel(valeur);
 						String nom = cle.toString();
 						JCheckBox rad = new JCheckBox();
 						rad.setName(nom);
 						rad.setEnabled(true);
 						// keyset
 						lstbox.put(i, rad);
 						i++;
 
 						frame.getInfoStageUv().add(nuv);
 						frame.getInfoStageUv().add(rad);
 					}
 				}
 
 			}// fin for
 			SapforButton inscr = new SapforButton("s'inscrire");
 			frame.getInfoStageUv().add(inscr);
 			frame.getInfoStageUv().revalidate();
 			frame.getInfoStageUv().setVisible(true);
 
 			// quand on submit on regarde dans toutes les box si etat=true; on
 			// fait une list des IDuv
 
 			// listener pour la validation de l'inscription
 
 			inscr.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseClicked(MouseEvent e) {
 
 					List<String> idUv = new LinkedList<String>();
 					for (int i = 0; i < lstbox.size(); i++) {
 						if (lstbox.get(i).isSelected() == true) {
 							idUv.add(lstbox.get(i).getName());
 
 						}
 					}
 
 					if (idUv.size() == 0) {
 						JOptionPane.showMessageDialog(null,
 								"Vous n'avez selectionn aucune Uv", "Error",
 								JOptionPane.ERROR_MESSAGE);
 					} else {
 
 						ICommand cmd = (ICommand) context
 								.getBean("cmdAddInscrition");
 						DefaultCommandContext ctx = new DefaultCommandContext();
 						ctx.put(ICommandContextKey.Key_Insct, idUv);
 						boolean etat = cmd.execute(ctx);
 						System.out.println("OK2");
 						if (etat == true) {
 							JOptionPane.showMessageDialog(null,
 									"Inscription russie", "Confirmation",
 									JOptionPane.PLAIN_MESSAGE);
 						} else {
 							JOptionPane.showMessageDialog(null,
 									"erreur inscription (dj inscrit)",
 									"Error", JOptionPane.ERROR_MESSAGE);
 						}
 					}
 
 				}
 			});
 
 		}
 		// System.out.println("----------------------------------------");
 	}// fin displayUvDispo
 
 	@Override
 	public void displayAccueilAgentSuccessfull(String nameA, String fNameA,
 			long typeAg) {
 		// TODO Auto-generated method stub
 		String infoAgent = fNameA + " " + nameA;
 		frame.getAccueilLabel().setText("Bonjour " + infoAgent);// Edition du
 																// message
 																// personnalis
 																// (Nom+prnom
 																// de l'agent
 																// logg)
 
 		Jlogin.setVisible(false); // Disparition de la boite de dialogue de
 									// connexion
 		frame.setVisible(true); // Apparition de la page d'accueil personnalise
 								// de l'agent logg
 		if (typeAg == 0) {
 			frame.getButtonAdmin().setVisible(true);
 			frame.getButtonAddAgent().setVisible(true);
 		}
 	}
 
 	@Override
 	public void errorLogin() {
 		// TODO Auto-generated method stub
 		JOptionPane.showMessageDialog(null, "Mauvais Login/Mot de passe",
 				"Error", JOptionPane.ERROR_MESSAGE);
 	}
 
 	public void displayAgent(Map<Long, String> nomAgent) {
 
 		DefaultCommandContext ctx = new DefaultCommandContext();
 		ctx.put(ICommandContextKey.Key_DisplayAgent, nomAgent);
 		ContainerGestionAgent rootUI = (ContainerGestionAgent) context
 				.getBean("uiAgent");
 		rootUI.showUI(context, ctx, frame);
 		frame.setVisible(false);
 	}
 
 }
