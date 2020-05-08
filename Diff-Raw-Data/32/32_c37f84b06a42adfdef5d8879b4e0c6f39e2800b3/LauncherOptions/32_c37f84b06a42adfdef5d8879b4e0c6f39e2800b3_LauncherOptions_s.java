 package spaceshooters.main;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import spaceshooters.util.Logger;
 import spaceshooters.util.config.Configuration;
 import spaceshooters.util.config.EnumConfig;
 
 class LauncherOptions extends JFrame implements ItemListener, ActionListener {
 	private static final long serialVersionUID = -7537326377780210924L;
 	
 	private Configuration config = Configuration.getConfiguration();
 	private String[] fpsString = { "75 FPS", "VSync", "Unlimited" };
 	private JPanel window = new JPanel();
 	private JButton done = new JButton("Done");
 	private JLabel fpsText = new JLabel("FPS Limiter:");
 	private JCheckBox fullscreen = new JCheckBox("Fullscreen");
 	private JCheckBox oldGraphics = new JCheckBox("Old textures");
 	private JComboBox<String> fpsSelector = new JComboBox<>(fpsString);
 	
 	public LauncherOptions() {
 		super("Spaceshooters 2 Options");
 		this.setSize(240, 320);
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			Logger.error("I cannot get system's look and feel! :(");
 		}
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		this.getContentPane().add(window);
 		this.setResizable(false);
 		this.setVisible(true);
 		window.setLayout(null);
 		
 		initButtons();
 	}
 	
 	public void initButtons() {
 		done.setBounds(240 / 2 - 100 / 2, 220, 100, 50);
 		oldGraphics.setSelected(config.getBoolean(EnumConfig.OLD_GRAPHICS) ? true : false);
		oldGraphics.setBounds(110, 153, 95, 20);
 		fullscreen.setSelected(config.getBoolean(EnumConfig.FULLSCREEN) ? true : false);
		fullscreen.setBounds(15, 153, 85, 20);
		fpsText.setBounds(15, 126, 85, 20);
 		fpsSelector.setSelectedIndex(determineFPSLimiter());
		fpsSelector.setBounds(100, 126, 100, 20);
 		
 		done.addActionListener(this);
 		oldGraphics.addItemListener(this);
 		fullscreen.addItemListener(this);
 		fpsSelector.addActionListener(this);
 		
 		window.add(done);
 		window.add(fpsText);
 		window.add(fpsSelector);
 		window.add(oldGraphics);
 		window.add(fullscreen);
 	}
 	
	private int determineFPSLimiter() {
		if ((config.getBoolean(EnumConfig.LIMIT_FPS)) && (!config.getBoolean(EnumConfig.USE_VSYNC))) {
			return 0;
		} else if ((!config.getBoolean(EnumConfig.LIMIT_FPS)) && (config.getBoolean(EnumConfig.USE_VSYNC))) {
			return 1;
		} else {
			return 2;
		}
	}
	
 	@Override
 	public void itemStateChanged(ItemEvent event) {
 		if (event.getSource() == fullscreen) {
 			if (fullscreen.isSelected()) {
 				config.setBoolean(EnumConfig.FULLSCREEN, true);
 			} else {
 				config.setBoolean(EnumConfig.FULLSCREEN, false);
 			}
 		}
 		if (event.getSource() == oldGraphics) {
 			if (oldGraphics.isSelected()) {
 				config.setBoolean(EnumConfig.OLD_GRAPHICS, true);
 			} else {
 				config.setBoolean(EnumConfig.OLD_GRAPHICS, false);
 			}
 		}
 		this.repaint();
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource() == done) {
 			this.dispose();
 		}
 		
 		if (event.getSource() == fpsSelector) {
 			String option = (String) fpsSelector.getSelectedItem();
 			switch (option) {
 			case "75 FPS":
 				config.setBoolean(EnumConfig.LIMIT_FPS, true);
 				config.setBoolean(EnumConfig.USE_VSYNC, false);
 				break;
 			case "VSync":
 				config.setBoolean(EnumConfig.LIMIT_FPS, false);
 				config.setBoolean(EnumConfig.USE_VSYNC, true);
 				break;
 			case "Unlimited":
 				config.setBoolean(EnumConfig.LIMIT_FPS, false);
 				config.setBoolean(EnumConfig.USE_VSYNC, false);
 				break;
 			}
 		}
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		Image logo = null;
 		Image logoOld = null;
 		try {
 			logo = ImageIO.read(this.getClass().getResourceAsStream("/gfx/menu/launchoptions2.png"));
 			logoOld = ImageIO.read(this.getClass().getResourceAsStream("/gfx/menu/launchoptions.png"));
 		} catch (IOException e) {
 			Logger.error("I cannot load my logo file!");
 			Logger.error("By the way, I am options.");
 		}
 		super.paint(g);
 		if (config.getBoolean(EnumConfig.OLD_GRAPHICS)) {
 			g.drawImage(logoOld, 0, 0, null);
 		} else {
 			g.drawImage(logo, 0, 0, null);
 		}
 	}
 }
