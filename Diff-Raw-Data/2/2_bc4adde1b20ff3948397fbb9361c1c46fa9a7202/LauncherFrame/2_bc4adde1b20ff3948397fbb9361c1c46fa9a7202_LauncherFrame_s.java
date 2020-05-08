 package it.planetgeeks.mclauncher.frames;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 import it.planetgeeks.mclauncher.Launcher;
 import it.planetgeeks.mclauncher.frames.panels.LoginPanel;
 import it.planetgeeks.mclauncher.frames.panels.MainPanel;
 import it.planetgeeks.mclauncher.frames.utils.CustomMouseListener;
 import it.planetgeeks.mclauncher.utils.LanguageUtils;
 
 import javax.swing.ButtonGroup;
 import javax.swing.GroupLayout;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.KeyStroke;
 import javax.swing.LayoutStyle;
 import javax.swing.WindowConstants;
 
 public class LauncherFrame extends JFrame
 {
 	private static final long serialVersionUID = 1L;
 
 	public LauncherFrame()
 	{
 		dim = new Dimension();
 		dim.width = 840;
 		dim.height = 530;
 		initComponents();
 	}
 
 	private void initComponents()
 	{
 		setTitle(LanguageUtils.getTranslated("launcher.title"));
 
 		this.setMinimumSize(dim);
 
 		loginPanel = new LoginPanel();
 		mainPanel = new MainPanel();
 
 		menuBar = new JMenuBar();
 		menu1 = new JMenu();
 		menu2 = new JMenu();
 		menu3 = new JMenu();
 
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
 		menuBar.add(menu1);
 		menuBar.add(menu2);
 		menuBar.add(menu3);
 
 		setJMenuBar(menuBar);
 
 		setMenu();
 
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(loginPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
 		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(loginPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
 
 		pack();
 	}
 
 	private void setMenu()
 	{
 		menu1.setText(LanguageUtils.getTranslated("lancher.bar.file"));
 		menu2.setText(LanguageUtils.getTranslated("launcher.bar.options"));
 		menu3.setText(LanguageUtils.getTranslated("launcher.bar.info"));
 
 		Object items1[][] = { { "Esci", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK), "normal" } };
 
 		menuItemCreation(menu1, items1);
 
 		menu1.setMnemonic(KeyEvent.VK_F);
 
 		menu1.getItem(0).addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				Launcher.closeLauncher();
 
 			}
 		});
 
 		Object items2[][] = { { LanguageUtils.getTranslated("launcher.bar.options.manageMem"), KeyEvent.VK_M, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK), "normal" }, { LanguageUtils.getTranslated("launcher.bar.options.showConsole"), KeyEvent.VK_K, KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK), "check" }, { "separator" }, { LanguageUtils.getTranslated("launcher.bar.options.advanced"), KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), "check" } };
 
 		menuItemCreation(menu2, items2);
 
 		menu2.setMnemonic(KeyEvent.VK_O);
 
 		menu2.getItem(0).addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				Launcher.openMemoryEditor(0, null);
 
 			}
 		});
 
 		menu2.getItem(1).addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				Launcher.openOrCloseConsole();
 			}
 		});
 
 		menu2.getItem(3).addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{ 
 				Launcher.openOrCloseOptionsFrame();
 			}
 		});
 
 		menu2.addMouseListener(new CustomMouseListener()
 		{
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
 				((JCheckBoxMenuItem) menu2.getItem(1)).setSelected(Launcher.isConsoleOpened());
 				((JCheckBoxMenuItem) menu2.getItem(4)).setSelected(Launcher.isAdvOptionsOpened());
 			}
 		});
 
 		String langs[] = LanguageUtils.getNames();
 
 		Object items[][] = new String[langs.length][4];
 
 		for (int i = 0; i < langs.length; i++)
 		{
 			items[i][0] = langs[i];
 			items[i][1] = null;
 			items[i][2] = null;
 			items[i][3] = "check";
 		}
 
 		JMenu menuLanguage = new JMenu(LanguageUtils.getTranslated("launcher.bar.options.language"));
 
 		menuLanguage.setMnemonic(KeyEvent.VK_L);
 
 		menuItemCreation(menuLanguage, items);
 
 		for (int i = 0; i < langs.length; i++)
 		{
 			temp = i;
 			menuLanguage.getItem(i).addActionListener(new ActionListener()
 			{
 				@Override
 				public void actionPerformed(ActionEvent arg0)
 				{
 					LanguageUtils.setLanguage(temp);
 				}
 			});
 			temp = 0;
 		}
 
 		menuLanguage.getItem(LanguageUtils.getCurrentLangIndex()).setSelected(true);
 
 		menu2.add(menuLanguage, 2);
 
 		Object items3[][] = { { LanguageUtils.getTranslated("launcher.bar.info.website"), KeyEvent.VK_W, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK), "normal" }, { LanguageUtils.getTranslated("launcher.bar.info.info"), KeyEvent.VK_I, KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK), "check" } };
 
 		menuItemCreation(menu3, items3);
 
 		menu3.setMnemonic(KeyEvent.VK_QUOTE);
 
 		menu3.getItem(1).addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				Launcher.openOrCloseInfoFrame();
 			}
 		});
 		
 		menu3.addMouseListener(new CustomMouseListener()
 		{
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
				((JCheckBoxMenuItem) menu2.getItem(1)).setSelected(Launcher.isInfoOpened());
 			}
 		});
 	}
 
 	private void menuItemCreation(JMenu menu, Object data[][])
 	{
 		ButtonGroup bg = new ButtonGroup();
 		JMenuItem item = null;
 
 		for (int i = 0; i < data.length; i++)
 		{
 			if (data[i].length > 1)
 			{
 				if (data[i][3].equals("normal"))
 				{
 					item = new JMenuItem();
 				}
 				else if (data[i][3].equals("radio"))
 				{
 					item = new JRadioButtonMenuItem();
 					bg.add(item);
 				}
 				else if (data[i][3].equals("check"))
 				{
 					item = new JCheckBoxMenuItem();
 				}
 			}
 			else
 			{
 				item = new JMenuItem();
 			}
 
 			String data0 = (String) data[i][0];
 			if (data0.equals("separator"))
 				menu.addSeparator();
 			else
 			{
 				String text = data0;
 				Integer mnemonic = data[i][1] != null ? (Integer) data[i][1] : KeyEvent.VK_UNDEFINED;
 				KeyStroke accelerator = data[i][2] != null ? (KeyStroke) data[i][2] : null;
 
 				item.setText(text);
 				item.setMnemonic(mnemonic);
 				item.setAccelerator(accelerator);
 
 				menu.add(item);
 			}
 		}
 	}
 
 	private Dimension dim;
 	private JMenu menu1;
 	private JMenu menu2;
 	private JMenu menu3;
 	private JMenuBar menuBar;
 	public MainPanel mainPanel;
 	public LoginPanel loginPanel;
 	private int temp;
 }
