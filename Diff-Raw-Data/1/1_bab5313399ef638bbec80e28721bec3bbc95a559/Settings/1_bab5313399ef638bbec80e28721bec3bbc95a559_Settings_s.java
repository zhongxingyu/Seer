 package view.menu.subMenuPanels;
 
 
import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Locale;
 
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 
 import model.save.SettingsModel;
 
 import resources.MenuLookAndFeel;
 import resources.Translator;
 import view.menu.MenuButton;
 import view.menu.MenuLabel;
 
 /**
  * 
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class Settings extends SubMenuPanel {
 	private static JTextField nameField = new JTextField();
 	private static JComboBox<Locale> language = new JComboBox<Locale>();
 	private static JToggleButton fullscreen = new JToggleButton();
 	
 	public Settings(MenuButton button) {
 		super(Translator.getMenuString("settings"), getPanel(), button);
 		button.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveSettings();
 			}
 		});
 	}
 
 	private static JPanel getPanel() {
 		JPanel p = new JPanel();		
 		
 		p.setLayout(new GridLayout(0, 2, MenuLookAndFeel.getGap(), MenuLookAndFeel.getGap()));
 		
 		p.add(new MenuLabel(Translator.getMenuString("playerName")+ ":"));
 		p.add(nameField);
 		nameField.setFont(MenuLookAndFeel.getLargeFont());
 		nameField.setText(SettingsModel.getUserName());
 		nameField.setBackground(MenuLookAndFeel.getInputFieldColor());
 		nameField.setBorder(MenuLookAndFeel.getSettingsTextFieldFont());
 		
 		startComboBox();
 		p.add(new MenuLabel(Translator.getMenuString("language")+":"));
 		p.add(language);
 		
 		p.add(new MenuLabel(Translator.getMenuString("fullscreen")+":"));
 		p.add(fullscreen);
 		if(fullscreen.isSelected()) {
 			fullscreen.setText(Translator.getMenuString("on"));
 		}else{
 			fullscreen.setText(Translator.getMenuString("off"));
 		}
 		fullscreen.setFont(MenuLookAndFeel.getLargeFont());
 		fullscreen.setSelected(SettingsModel.getFullscreen());
 		fullscreen.setBorder(MenuLookAndFeel.getButtonBorder());
 		fullscreen.setBackground(MenuLookAndFeel.getButtonColor());
 		fullscreen.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 			@Override
 			public void mousePressed(MouseEvent e) {
 			}
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				if(fullscreen.isSelected()) {
 					fullscreen.setText(Translator.getMenuString("on"));
 				}else{
 					fullscreen.setText(Translator.getMenuString("off"));
 				}
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				fullscreen.setBorder(MenuLookAndFeel.getButtonHighlightedBorder());
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 				fullscreen.setBorder(MenuLookAndFeel.getButtonBorder());
 			}
 		});
 
 		return p;
 	}
 	
 	private static void startComboBox() {
 		language.removeAllItems();
 		language.addItem(model.save.SettingsModel.getLocale());
 		Locale[] l = model.save.SettingsModel.getAllLocales();
 		for (int a=0; a< l.length; a++){
 			if (!(l[a]).equals(model.save.SettingsModel.getLocale())){
 				language.addItem(l[a]);
 			}
 		}
 		
 		language.setFont(MenuLookAndFeel.getLargeFont());
 		language.setBackground(MenuLookAndFeel.getInputFieldColor());
 		language.setBorder(resources.MenuLookAndFeel.getSettingsTextFieldFont());
 	}
 
 	private static void saveSettings(){
 		SettingsModel.setUserName(nameField.getText());
 		SettingsModel.setFullscreen(fullscreen.isSelected());
 		SettingsModel.setLocale((Locale) language.getSelectedItem());
 		SettingsModel.save();
 	}
 
 }
