 package de.dhbw.muehle.gui.menus;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 import de.dhbw.muehle.gui.MillButton;
 import de.dhbw.muehle.gui.View;
 import de.dhbw.muehle.gui.ViewController;
 import de.dhbw.muehle.gui.viewactions.SettingsPanelVA;
 import de.dhbw.muehle.model.theme.Theme;
 import de.dhbw.muehle.model.theme.ThemeLoader;
 
 public class SettingsPanel extends Menu {
 
 	private SettingsPanelVA vActions;
 	private View view;
 	
 	private MillButton btnBack;
 	private MillLabel lblTheme;
 	
 	
 	public SettingsPanel(ViewController vController, View view) {
 		super(view);
 		this.view = view;
 		
 		// Listener initialisieren
 		vActions = new SettingsPanelVA(vController);
 				
 		// Panelgröße festlegen
 		setSize(Size);
 		
 		
 		setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("25px"),
 				ColumnSpec.decode("max(13px;min):grow(3)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(51px;min)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(149px;min)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(51px;min)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(192px;min):grow(10)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(149px;min)"),
 				ColumnSpec.decode("25px"),},
 			new RowSpec[] {
 				RowSpec.decode("25px"),
 				RowSpec.decode("max(95px;min):grow"),
 				RowSpec.decode("15px"),
 				RowSpec.decode("max(52px;min)"),
 				RowSpec.decode("15px"),
 				RowSpec.decode("max(88px;min)"),
 				RowSpec.decode("15px"),
 				RowSpec.decode("max(88px;min)"),
 				RowSpec.decode("15px"),
 				RowSpec.decode("max(88px;min)"),
 				RowSpec.decode("15px:grow(3)"),
 				RowSpec.decode("max(52px;min)"),
 				RowSpec.decode("25px"),}));
 		
 		btnBack = new MillButton(view, "ZumMenue");
 		btnBack.addActionListener(vActions.new btnBack());
 		add(btnBack, "12, 12, fill, fill");
 		
 		lblTheme = new MillLabel(view);
 		add(lblTheme, "6, 4, fill, fill");
 		
 		
 		MillButton btn8bitMill = new MillButton(view, "Logo");
 		btn8bitMill.setTheme(vController.getTheme("8bit Mill"));
 		btn8bitMill.addActionListener(vActions.new btn8bitMill());
 		add(btn8bitMill, "4, 8, 5, 1, fill, fill");
 		
 		MillButton btnWoodenMill = new MillButton(view, "Logo");
 		btnWoodenMill.setTheme(vController.getTheme("Wooden Mill"));
 		btnWoodenMill.addActionListener(vActions.new btnWoodenMill());
 		add(btnWoodenMill, "4, 6, 5, 1, fill, fill");
 		
 		MillButton btnDirtyMill = new MillButton(view, "Logo");
 		btnDirtyMill.setTheme(vController.getTheme("Dirty Mill"));
 		btnDirtyMill.addActionListener(vActions.new btnDirtyMill());
 		add(btnDirtyMill, "4, 10, 5, 1, fill, fill");
 	}
 	
 	
 	
 	@Override
 	public void paintComponent(Graphics g){
 		g.drawImage(view.getTheme().getEinstellungsHintergrund(), 0, 0, getWidth(), getHeight(), this);
 	}
 	
 	
 	
 	private class MillLabel extends JLabel{
 		
 		private View view;
 		
 		
 		public MillLabel(View view){
 			this.view = view;
 		}
 		
 		
 		
 		@Override
 		public void paintComponent(Graphics g){
 			g.drawImage(view.getTheme().getBtnTheme(), 0, 0, getWidth(), getHeight(), this);
 		}
 	}
 }
