 package display;
 
 import java.awt.Cursor;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import utils.Lang;
 
 import components.IMenu;
 import components.IMenuItem;
 import components.ITabbedPane;
 
 /**
  * @author Razican (Iban Eguia)
  */
 public class Menu extends JMenuBar implements ActionListener {
 
 	private static final long	serialVersionUID	= - 2674054941368737779L;
 
 	private IMenu				file, edit, help;
 	private IMenuItem			newFile, open, save, save_as;
 	private IMenuItem			preferences;
 	private ITabbedPane			tabbedPane;
 
 	/**
 	 * Create the menu.
 	 */
 	public Menu()
 	{
 		super();
 
 		file = new IMenu();
 		Lang.setLine(file, "menu_file");
 		file.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		file.setMargin(new Insets(5, 5, 5, 5));
 
 		edit = new IMenu();
 		Lang.setLine(edit, "menu_edit");
 		edit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		edit.setMargin(new Insets(5, 5, 5, 5));
 
 		help = new IMenu();
 		Lang.setLine(help, "menu_help");
 		help.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		help.setMargin(new Insets(5, 5, 5, 5));
 
 		newFile = new IMenuItem();
 		Lang.setLine(newFile, "menu_new");
 		newFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 		InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
 		newFile.setMargin(new Insets(5, 5, 5, 5));
 		newFile.setActionCommand("new");
 		newFile.addActionListener(this);
 		newFile.setIcon(new ImageIcon("img/new-icon.png"));
 
 		open = new IMenuItem();
 		Lang.setLine(open, "menu_open");
 		open.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
 		InputEvent.CTRL_MASK));
 		open.setMargin(new Insets(5, 5, 5, 5));
 		open.setActionCommand("open");
 		open.addActionListener(this);
 		open.setIcon(new ImageIcon("img/open-icon.png"));
 
 		save = new IMenuItem();
 		Lang.setLine(save, "menu_save");
 		save.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 		InputEvent.CTRL_MASK));
 		save.setMargin(new Insets(5, 5, 5, 5));
 		save.setActionCommand("save");
 		save.addActionListener(this);
 		save.setIcon(new ImageIcon("img/save-icon.png"));
 
 		save_as = new IMenuItem();
 		Lang.setLine(save_as, "menu_save_as");
 		save_as.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		save_as.setMargin(new Insets(5, 5, 5, 5));
 		save_as.setActionCommand("save-as");
 		save_as.addActionListener(this);
 		save_as.setIcon(new ImageIcon("img/save-as-icon.png"));
 
 		preferences = new IMenuItem();
 		Lang.setLine(preferences, "preferences");
 		preferences.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		preferences.setIcon(new ImageIcon("img/sett-icon.png"));
 		preferences.addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				Preferences p = new Preferences();
 
 				String[] options = {Lang.getLine("conf_dialog_ok"),
 				Lang.getLine("conf_dialog_cancel")};
 				JOptionPane pane = new JOptionPane(p,
 				JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
 				options, options[1]);
 				JDialog dialog = pane.createDialog(Lang.getLine("preferences"));
 				dialog.setLocationRelativeTo(Window.getInstance());
 				dialog.setVisible(true);
 
 				if (pane.getValue() == options[0])
 				{
 					utils.Preferences.setLocale(Lang.getAvailableLocales().get(
 					p.getLocaleIndex()));
 
 					Lang.setLang(Lang.getAvailableLocales().get(
 					p.getLocaleIndex()));
 
 					utils.Preferences.setLookAndFeelClass(p.getLookAndFeel());
 
 					try
 					{
 						UIManager.setLookAndFeel(p.getLookAndFeel());
 					}
 					catch (ClassNotFoundException | InstantiationException
 					| IllegalAccessException | UnsupportedLookAndFeelException e1)
 					{
 						e1.printStackTrace();
 					}
 
 					SwingUtilities.updateComponentTreeUI(Window.getInstance());
 					Window.getInstance().pack();
 				}
 
 				dialog.dispose();
 			}
 		});
 
 		file.add(newFile);
 		file.add(open);
 		file.add(save);
 		file.add(save_as);
 
 		edit.add(preferences);
 
 		add(file);
 		add(edit);
 		add(help);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		tabbedPane = ((Start) Window.getInstance().getContentPane())
 		.getTabbedPane();
 
 		switch (e.getActionCommand())
 		{
			case "new":// TODO avisar si ya hay un mapa
 				if (tabbedPane.getSelectedComponent() instanceof MapEditor)
 				{
 					NewMap p = new NewMap();
 
 					String[] options = {Lang.getLine("conf_dialog_ok"),
 					Lang.getLine("conf_dialog_cancel")};
 					JOptionPane pane = new JOptionPane(p,
 					JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
 					null, options, options[1]);
 					JDialog dialog = pane.createDialog(Lang.getLine("new_map"));
 					dialog.setSize(500, 200);
 					dialog.setLocationRelativeTo(Window.getInstance());
 					dialog.setVisible(true);
 
 					if (pane.getValue() == options[0])
 					{
 						if (p.getMap() == null)
 						{
 							JOptionPane.showMessageDialog(null,
 							Lang.getLine("sprite_load_error"),
 							Lang.getLine("error"), JOptionPane.ERROR_MESSAGE,
 							new ImageIcon("img/error.png"));
 						}
 						else
 						{
							MapEditor m = new MapEditor();
							((Start) Window.getInstance().getContentPane())
							.getTabbedPane().setComponentAt(0, m);

							m.setMap(p.getMap());
 						}
 					}
 
 					dialog.dispose();
 				}
 			break;
 		}
 	}
 }
