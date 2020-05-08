 package ru.narod.vn91.pointsop.gui;
 
 import java.awt.event.*;
 import java.io.File;
 import java.net.URI;
 import java.net.URL;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 
 import ru.narod.vn91.pointsop.ai.KeijKvantttAi;
 import ru.narod.vn91.pointsop.ai.RandomAi;
 import ru.narod.vn91.pointsop.data.Memory;
 import ru.narod.vn91.pointsop.server.AiVirtualServer;
 
 public class SelfishGuiStarter {
 
 	public static void main(String[] args) {
 		for (String arg : args) {
 			System.out.println(arg);
 		}
 		if (Memory.getVersion() <= 1
 				&& Memory.getKeijKvantttAiPath().equals("keijkvantttai")) {
 			Memory.setKeijKvantttAiPath("");
 		}
 		if (Memory.getVersion() < 4) {
 			Memory.resetColors();
 		}
 		Memory.setNewestVersion();
 		final JFrame frame = new JFrame("Точки - pointsOp 0.9.8");
 		URL url = SelfishGuiStarter.class.getClassLoader().
 				getResource("ru/narod/vn91/pointsop/gui/vp.jpg");
 		frame.setIconImage(new ImageIcon(url).getImage());
 		frame.setSize(925, 670);
 
 		{
 			if (Memory.getFrameWidth() > 0
 					&& Memory.getFrameHeight() > 0) {
 				//					&& Memory.getFrameX() > 0
 				//					&& Memory.getFrameY() > 0
 				frame.setBounds(
 						Memory.getFrameX(),
 						Memory.getFrameY(),
 						Memory.getFrameWidth(),
 						Memory.getFrameHeight()
 				);
 				//				frame.setSize(
 				//						Memory.getFrameWidth(),
 				//						Memory.getFrameHeight());
 			}
 			//			frame.setLocationRelativeTo(frame.getRootPane());
 
 			frame.addComponentListener(
 					new ComponentListener() {
 
 						public void componentShown(ComponentEvent e) {
 						}
 
 						public void componentResized(ComponentEvent e) {
 							Memory.setFrameWidth(frame.getWidth());
 							Memory.setFrameHeight(frame.getHeight());
 							Memory.setFrameX(frame.getX());
 							Memory.setFrameY(frame.getY());
 						}
 
 						public void componentMoved(ComponentEvent e) {
 							Memory.setFrameX(frame.getX());
 							Memory.setFrameY(frame.getY());
 						}
 
 						public void componentHidden(ComponentEvent e) {
 						}
 					}
 			);
 		}
 
 		{
 			int x = frame.getX(), y = frame.getY();
 			x = Math.max(x, 0);
 			y = Math.max(y, 0);
 			frame.setLocation(x, y);
 		}
 
 		final JTabbedPaneMod tabbedPane = new JTabbedPaneMod();
 		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		frame.add(tabbedPane);
 		tabbedPane.setFocusable(false);
 
 		final GuiController guiController = new GuiController(tabbedPane);
 
 		WelcomePanel roomWelcome = new WelcomePanel(guiController);
 		tabbedPane.addTab("Привет!", roomWelcome, false);
 		roomWelcome.guiController = guiController;
 		guiController.serverOutput = roomWelcome.jTextPane_ServerOutput;
 
 		//		tabbedPane.addTab("game room", new GameRoom(null, "", guiController, "",
 		//				"", 1, 1, "", false, "", true, true));
 		//		tabbedPane.addTab(
 		//				"priv chat", new PrivateChat(null, guiController, ""),
 		//				false
 		//		);
 		//		tabbedPane.addTab("Ai test", new AiRoom().panel1, true);
 
 		{
 			JMenuBar jMenuBar = new JMenuBar();
 
 			{
 				JMenu jMenu = new JMenu("Обучение");
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">Правила игры</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://pointsgame.net/site/rules"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">Разборы партий</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://pointsgame.net/site/analysis"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">Архив игр</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://pointsgame.net/site/games"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 					jMenu.addSeparator();
 
 				{
					JMenuItem jMenuItem = new JMenuItem("Тест (уровень 1) 30 заданий");
 					jMenuItem.addActionListener(
 							new ActionListener() {
 								public void actionPerformed(ActionEvent e) {
 									try {
 										javax.swing.JPanel pointsIQ=new com.google.sites.priymakpoints.pointsiq.PointsIQ(1);
 										tabbedPane.addTab("PointsIQ level 1", pointsIQ, true);
 										tabbedPane.setSelectedComponent(pointsIQ);
 									} catch (Exception ignored) {	}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 
 				{
					JMenuItem jMenuItem = new JMenuItem("Тест (уровень 2) 30 заданий");
 					jMenuItem.addActionListener(
 							new ActionListener() {
 								public void actionPerformed(ActionEvent e) {
 									try {
 										javax.swing.JPanel pointsIQ=new com.google.sites.priymakpoints.pointsiq.PointsIQ(2);
 										tabbedPane.addTab("PointsIQ level 2", pointsIQ, true);
 										tabbedPane.setSelectedComponent(pointsIQ);
 									} catch (Exception ignored) {	}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 
 				{
					JMenuItem jMenuItem = new JMenuItem("Тест (уровень 3) 30 заданий");
 					jMenuItem.addActionListener(
 							new ActionListener() {
 								public void actionPerformed(ActionEvent e) {
 									try {
 										javax.swing.JPanel pointsIQ=new com.google.sites.priymakpoints.pointsiq.PointsIQ(3);
 										tabbedPane.addTab("PointsIQ level 3", pointsIQ, true);
 										tabbedPane.setSelectedComponent(pointsIQ);
 									} catch (Exception ignored) {	}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				{
					JMenuItem jMenuItem = new JMenuItem("Тест (уровень 4) 15 заданий");		
 					jMenuItem.addActionListener(
 							new ActionListener() {
 								public void actionPerformed(ActionEvent e) {
 									try {
 										javax.swing.JPanel pointsIQ=new com.google.sites.priymakpoints.pointsiq.PointsIQ(4);
 										tabbedPane.addTab("PointsIQ level 4", pointsIQ, true);
 										tabbedPane.setSelectedComponent(pointsIQ);
 									} catch (Exception ignored) {	}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">О программе PointsIQ</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://pointsgame.net/site/pointsiq"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				jMenuBar.add(jMenu);
 			}
 
 
 
 			{
 				JMenu jMenu_AI = new JMenu("Играть с ИИ");
 				{
 					final JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=>Keij&Kvanttt AI - описание работы с этим ИИ</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://vkontakte.ru/topic-21455903_24989187"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu_AI.add(jMenuItem);
 				}
 
 				JMenu jMenu_KeijDownloads = new JMenu("Keij&Kvanttt AI - скачать необходимые файлы");
 				{
 					{
 						final JMenuItem jMenuItem = new JMenuItem(
 								"<html><a href=>exe-файл для винды</a></html>"
 						);
 						jMenuItem.addActionListener(
 								new ActionListener() {
 
 									public void actionPerformed(ActionEvent e) {
 										try {
 											java.awt.Desktop.getDesktop().browse(
 													new URI(
 															"http://code.google.com/p/pointsgame/"
 													)
 											);
 										} catch (Exception ignored) {
 										}
 									}
 								}
 						);
 						jMenu_KeijDownloads.add(jMenuItem);
 					}
 					{
 						final JMenuItem jMenuItem = new JMenuItem(
 								"<html><a href=>исполняемый файл для линукса и мака</a></html>"
 						);
 						jMenuItem.addActionListener(
 								new ActionListener() {
 
 									public void actionPerformed(ActionEvent e) {
 										try {
 											java.awt.Desktop.getDesktop().browse(
 													new URI(
 															"http://pointsgame.net/kekvai/keijkvantttai.linux"
 													)
 											);
 										} catch (Exception ignored) {
 										}
 									}
 								}
 						);
 						jMenu_KeijDownloads.add(jMenuItem);
 					}
 					jMenu_AI.add(jMenu_KeijDownloads);
 				}
 
 				final JMenuItem jMenuItem_KeijkvantttaiExecute;
 				{
 					jMenuItem_KeijkvantttaiExecute = new JMenuItem(
 							"Keij&Kvanttt AI - запуск"
 					);
 					jMenuItem_KeijkvantttaiExecute.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									AiVirtualServer aiWrapper =
 											new AiVirtualServer(
 													guiController
 											);
 									aiWrapper.setAi(
 											new KeijKvantttAi(
 													aiWrapper,
 													39, 32,
 													Memory.getKeijKvantttAiPath()
 											)
 									);
 									aiWrapper.init();
 								}
 							}
 					);
 					jMenuItem_KeijkvantttaiExecute.setEnabled(
 							! Memory.getKeijKvantttAiPath().equals("")
 					);
 					//					/home/u2/Downloads/kkai/PointsConsole/a.out
 					//
 					//					jMenu.add(jMenuItem_KeijkvantttaiExecute);
 				}
 
 				{
 					final JMenuItem jMenuItem = new JMenuItem(
 							"Keij&Kvanttt AI - указать путь скачанного файла"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									String command = JOptionPane.showInputDialog(
 											"Для работы с данным ИИ скачайте, пожалуйста, \n" +
 													"файл с искусственным интеллектом (ссылка выше).\n\n" +
 													"" +
 													"После этого введите сюда путь к скачанному файлу. \n" +
 													"Например, если вы скачали keijkvantttai.exe в директорию c:/Downloads/  то введите \n" +
 													"c:/Downloads/keijkvantttai.exe\n\n" +
 													"Чтобы узнать путь к скачанному файлу можно, например, \n" +
 													"заглянуть в свойства файла и скопировать строчку 'размещение'.\n\n" +
 													"Или запустить файлик и скопировать инфу которую он предоставляет (new). \n\n" +
 													"P.S. К сожалению, " +
 													"по политике безопасности java, " +
 													"выбрать файл более привычным способом не получается.",
 											Memory.getKeijKvantttAiPath()
 									);
 									if (command != null) {
 										if (command.endsWith("\\")
 												|| command.endsWith("/")
 												|| command.endsWith(File.pathSeparator)) {
 											command = command + "keijkvantttai.exe";
 										}
 										Memory.setKeijKvantttAiPath(command);
 										JOptionPane.showMessageDialog(
 												null,
 												"Путь изменён на " + command + " \n" +
 														"Попробуйте теперь запустить ИИ. :)"
 										);
 										jMenuItem_KeijkvantttaiExecute.setEnabled(
 												! Memory.getKeijKvantttAiPath().equals("")
 										);
 									}
 								}
 							}
 					);
 					jMenu_AI.add(jMenuItem);
 					jMenu_AI.add(jMenuItem_KeijkvantttaiExecute);
 				}
 
 				jMenu_AI.add(new JSeparator());
 
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">Priymak AI - о программе</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://pointsgame.net/site/pointsai"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu_AI.add(jMenuItem);
 				}
 
 				{
 					final JMenuItem jMenuItem =
 							new JMenuItem("Priymak AI - запуск 1.08");
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									new Thread() {
 										@Override
 										public void run() {
 											AiVirtualServer aiWrapper =
 													new AiVirtualServer(
 															guiController
 													);
 											aiWrapper
 													.setAi(
 													new com.google.sites.priymakpoints.pointsai.pointsAI_1_08.PointsAIEngine(
 															aiWrapper, 39, 32
 													)
 													);
 											aiWrapper.init();
 										}
 									}.start();
 								}
 							}
 					);
 					jMenu_AI.add(jMenuItem);
 				}
 
 				{
 					final JMenuItem jMenuItem =
 							new JMenuItem("Priymak AI - запуск 1.07");
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									new Thread() {
 										@Override
 										public void run() {
 											AiVirtualServer aiWrapper =
 													new AiVirtualServer(
 															guiController
 													);
 											aiWrapper
 													.setAi(
 													new com.google.sites.priymakpoints.pointsai.pointsAI_1_07.PointsAIEngine(
 															aiWrapper, 39, 32
 													)
 													);
 											aiWrapper.init();
 										}
 									}.start();
 								}
 							}
 					);
 					jMenu_AI.add(jMenuItem);
 				}
 
 				jMenu_AI.add(new JSeparator());
 
 				{
 					final JMenuItem jMenuItem = new JMenuItem("Рандомный ИИ - запуск");
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									// ai as server
 									AiVirtualServer aiWrapper =
 											new AiVirtualServer(
 //													(GuiForServerInterface)guiController
 													guiController
 											);
 									aiWrapper.setAi(new RandomAi(aiWrapper, 39, 32));
 									aiWrapper.init();
 								}
 							}
 					);
 					jMenu_AI.add(jMenuItem);
 				}
 
 				jMenuBar.add(jMenu_AI);
 			}
 
 			{
 				JMenu jMenu = new JMenu("Настройки");
 				{
 					JMenuItem jMenuItem = new JMenuItem("Настройки");
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									SettingsPanel settings = new SettingsPanel();
 									tabbedPane.addTab("Настройки", settings, true);
 									tabbedPane.setSelectedComponent(settings);
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				jMenuBar.add(jMenu);
 			}
 			{
 				JMenu jMenu = new JMenu("Помощь");
 
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">О программе PointsOP</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://pointsgame.net/site/pointsop"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">Обсуждение PointsOP</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://vkontakte.ru/pointsgame"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				{
 					JMenuItem jMenuItem = new JMenuItem(
 							"<html><a href=\"\">Полезные ссылки</a></html>"
 					);
 					jMenuItem.addActionListener(
 							new ActionListener() {
 
 								public void actionPerformed(ActionEvent e) {
 									try {
 										java.awt.Desktop.getDesktop().browse(
 												new URI(
 														"http://pointsgame.net/site/links"
 												)
 										);
 									} catch (Exception ignored) {
 									}
 								}
 							}
 					);
 					jMenu.add(jMenuItem);
 				}
 				jMenuBar.add(jMenu);
 			}
 			frame.setJMenuBar(jMenuBar);
 		}
 
 		frame.setVisible(true);
 		roomWelcome.jTextField_Username.requestFocusInWindow();
 		frame.addWindowListener(
 				new WindowAdapter() {
 
 					@Override
 					public void windowClosing(WindowEvent we) {
 						System.exit(0);
 					}
 				}
 		);
 	}
 }
