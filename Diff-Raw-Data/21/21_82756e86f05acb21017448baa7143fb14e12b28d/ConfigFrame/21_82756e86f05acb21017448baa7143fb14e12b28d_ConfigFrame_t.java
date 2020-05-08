 package ambibright.ihm;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import ambibright.ressources.Factory;
 import ambibright.ressources.Config.Parameters;
 import ambibright.ressources.Config;
 import ambibright.ressources.CheckUpdate;
 import ambibright.engine.squareAnalyser.SquareAnalyser;
 import ambibright.engine.color.ColorAlgorithm;
 import ambibright.engine.ArduinoSender;
 
 public class ConfigFrame extends JFrame {
 
 	private JTextField arduinoSerial, arduinoDataRate, appList, squareSize, fps, ledNbTop, lebNbLeft, analysePitch, delayCheckRatio, delayCheckProcess, updateUrl;
 	private JComboBox screenDevice, squareAnalyser, smoothing;
 	private JCheckBox checkApp, blackOtherScreens, showFPSFrame;
 	private CheckUpdate checkUpdate;
 	private JButton checkUpdateBtn, findPortBtn;
 	// TODO fix this thing
 	private EnumMap<Parameters, JSlider> colorAlgoSliders = new EnumMap<Parameters, JSlider>(Parameters.class);
 	private EnumMap<Parameters, ColorAlgorithm> colorAlgoMap = new EnumMap<Parameters, ColorAlgorithm>(Parameters.class);
 
 	public ConfigFrame(AmbiFont ambiFont, final Config config, final List<ColorAlgorithm> colorAlgorithmList) {
 		super(Factory.appName + " - Configuration");
 		setIconImage(Factory.get().getImageIcon());
 		setLayout(new GridLayout(20 + colorAlgorithmList.size(), 2));
 
 		arduinoSerial = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_ARDUINO_PORT));
 		appList = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_PROCESS_LIST));
 		arduinoDataRate = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_ARDUINO_DATA_RATE));
 		squareSize = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_SQUARE_SIZE));
 		fps = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_FPS));
 		ledNbTop = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_LED_NB_TOP));
 		lebNbLeft = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_LED_NB_LEFT));
 		analysePitch = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_ANALYSE_PITCH));
 		delayCheckRatio = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_DELAY_CHECK_RATIO));
 		delayCheckProcess = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_DELAY_CHECK_PROCESS));
 		updateUrl = new JTextField(Factory.get().getConfig().get(Parameters.CONFIG_UPDATE_URL));
 
 		checkUpdate = new CheckUpdate(Factory.get().getConfig().get(Parameters.CONFIG_UPDATE_URL));
 
 		screenDevice = new JComboBox();
 		screenDevice.setBorder(null);
 		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
 			screenDevice.addItem(device.getIDstring() + " - " + device.getDefaultConfiguration().getBounds().width + "x" + device.getDefaultConfiguration().getBounds().height);
 		}
 		screenDevice.setSelectedIndex(Integer.valueOf(Factory.get().getConfig().get(Parameters.CONFIG_SCREEN_DEVICE)));
 
 		squareAnalyser = new JComboBox();
 		squareAnalyser.setBorder(null);
 		for (SquareAnalyser c : SquareAnalyser.values()) {
 			squareAnalyser.addItem(c);
 		}
 		squareAnalyser.setSelectedItem(Factory.get().getSquareAnalyser());
 
 		smoothing = new JComboBox();
 		smoothing.setBorder(null);
 		for (int i = 1; i < 6; i++) {
 			smoothing.addItem(i);
 		}
 		smoothing.setSelectedItem(Factory.get().getSmoothing() - 1);
 
 		checkApp = new JCheckBox();
 		checkApp.setSelected(Factory.get().isCheckProcess());
 
 		blackOtherScreens = new JCheckBox();
 		blackOtherScreens.setSelected(Factory.get().isBlackOtherScreens());
 
 		showFPSFrame = new JCheckBox();
 		showFPSFrame.setSelected(Factory.get().isShowFPSFrame());
 
 		findPortBtn = new JButton("Try to find the port");
 		findPortBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				findPortBtn.setEnabled(false);
 				arduinoSerial.setText(ArduinoSender.getArduinoPort(Integer.valueOf(arduinoDataRate.getText()), ArduinoSender.defaultTestString));
 				findPortBtn.setEnabled(true);
 			}
 		});
 
 		checkUpdateBtn = new JButton("Check for update");
 		checkUpdateBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				checkUpdateBtn.setEnabled(false);
 				checkUpdate.setUrl(updateUrl.getText());
 				checkUpdate.manage();
 				checkUpdateBtn.setEnabled(true);
 			}
 		});
 
 		for (final ColorAlgorithm algo : colorAlgorithmList) {
 			float value = Float.parseFloat(Factory.get().getConfig().get(algo.getParameter()));
 			float inter = algo.getMaxValue() - algo.getMinValue();
			int percent = (int) (((value - algo.getMinValue()) / inter) * 100);
			final JSlider slider = new JSlider(0, 100, percent);
 			slider.addChangeListener(new ChangeListener() {
 				@Override
 				public void stateChanged(ChangeEvent e) {
					float value = getValueFromSlider( algo.getMinValue(), algo.getMaxValue(), slider.getValue() );
 					algo.updateParameter(value);
 				}
 			});
 			colorAlgoMap.put(algo.getParameter(), algo);
 			colorAlgoSliders.put(algo.getParameter(), slider);
 		}
 
 		JButton save = new JButton("Save");
 		save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				config.put(Parameters.CONFIG_LED_NB_LEFT, lebNbLeft.getText());
 				config.put(Parameters.CONFIG_LED_NB_TOP, ledNbTop.getText());
 				config.put(Parameters.CONFIG_ARDUINO_PORT, arduinoSerial.getText());
 				config.put(Parameters.CONFIG_PROCESS_LIST, appList.getText());
 				config.put(Parameters.CONFIG_SCREEN_DEVICE, screenDevice.getSelectedIndex() + "");
 				config.put(Parameters.CONFIG_CHECK_PROCESS, checkApp.isSelected() + "");
 				config.put(Parameters.CONFIG_ARDUINO_DATA_RATE, arduinoDataRate.getText());
 				config.put(Parameters.CONFIG_SQUARE_SIZE, squareSize.getText());
 				config.put(Parameters.CONFIG_FPS, fps.getText());
 				config.put(Parameters.CONFIG_ANALYSE_PITCH, analysePitch.getText());
 				config.put(Parameters.CONFIG_DELAY_CHECK_RATIO, delayCheckRatio.getText());
 				config.put(Parameters.CONFIG_DELAY_CHECK_PROCESS, delayCheckProcess.getText());
 				config.put(Parameters.CONFIG_SQUARE_ANALYSER, squareAnalyser.getSelectedItem().toString());
 				config.put(Parameters.CONFIG_UPDATE_URL, updateUrl.getText());
 				config.put(Parameters.CONFIG_SMOOTHING, smoothing.getSelectedIndex() + 1 + "");
 				config.put(Parameters.CONFIG_BLACK_OTHER_SCREENS, blackOtherScreens.isSelected() + "");
 				config.put(Parameters.CONFIG_SHOW_FPS_FRAME, showFPSFrame.isSelected() + "");
 
 				for (Map.Entry<Parameters, JSlider> entry : colorAlgoSliders.entrySet()) {
 					ColorAlgorithm algo = colorAlgoMap.get(entry.getKey());
					float value = getValueFromSlider( algo.getMinValue(), algo.getMaxValue(), entry.getValue().getValue() );
 					algo.updateParameter(value);
 					config.put(entry.getKey(), Float.toString(value));
 				}
 
 				config.save();
 				Factory.get().getTray().updateCheckBox();
 				Factory.get().getManager().restart();
 				dispose();
 			}
 		});
 
 		add(ambiFont.setFontBold(new JLabel(" Screen")));
 		add(ambiFont.setFont(screenDevice));
 
 		add(ambiFont.setFontBold(new JLabel(" Top LED number")));
 		add(ambiFont.setFont(ledNbTop));
 
 		add(ambiFont.setFontBold(new JLabel(" Left/Right LED number")));
 		add(ambiFont.setFont(lebNbLeft));
 
 		add(ambiFont.setFontBold(new JLabel(" Arduino Data Rate")));
 		add(ambiFont.setFont(arduinoDataRate));
 
 		add(ambiFont.setFontBold(new JLabel(" Arduino Serial Port")));
 		add(ambiFont.setFont(arduinoSerial));
 
 		add(ambiFont.setFontBold(new JLabel("")));
 		add(ambiFont.setFont(findPortBtn));
 
 		add(ambiFont.setFontBold(new JLabel(" Square Size")));
 		add(ambiFont.setFont(squareSize));
 
 		add(ambiFont.setFontBold(new JLabel(" Smoothing")));
 		add(ambiFont.setFont(smoothing));
 
 		add(ambiFont.setFontBold(new JLabel(" Check for Process")));
 		add(ambiFont.setFont(checkApp));
 
 		add(ambiFont.setFontBold(new JLabel(" Check for Process list")));
 		add(ambiFont.setFont(appList));
 
 		add(ambiFont.setFontBold(new JLabel(" Delay check process")));
 		add(ambiFont.setFont(delayCheckProcess));
 
 		add(ambiFont.setFontBold(new JLabel(" FPS")));
 		add(ambiFont.setFont(fps));
 
 		add(ambiFont.setFontBold(new JLabel(" Capture analyse pitch")));
 		add(ambiFont.setFont(analysePitch));
 
 		add(ambiFont.setFontBold(new JLabel(" Square analyser")));
 		add(ambiFont.setFont(squareAnalyser));
 
 		add(ambiFont.setFontBold(new JLabel(" Delay check ratio")));
 		add(ambiFont.setFont(delayCheckRatio));
 
 		add(ambiFont.setFontBold(new JLabel(" Black other screens")));
 		add(ambiFont.setFont(blackOtherScreens));
 
 		add(ambiFont.setFontBold(new JLabel(" Show FPS Frame")));
 		add(ambiFont.setFont(showFPSFrame));
 
 		add(ambiFont.setFontBold(new JLabel(" Update Url")));
 		add(ambiFont.setFont(updateUrl));
 
 		for (Map.Entry<Parameters, JSlider> entry : colorAlgoSliders.entrySet()) {
 			ColorAlgorithm algo = colorAlgoMap.get(entry.getKey());
 			add(ambiFont.setFontBold(new JLabel(" " + algo.getName())));
 			add(ambiFont.setFont(entry.getValue()));
 		}
 
 		add(ambiFont.setFontBold(new JLabel("Current version: " + checkUpdate.getLocalVersion())));
 		add(ambiFont.setFont(checkUpdateBtn));
 
 		add(ambiFont.setFontBold(new JLabel("")));
 		add(ambiFont.setFont(save));
 
 		pack();
 		setResizable(false);
 		setLocationRelativeTo(getParent());
 		setVisible(true);
 	}
 
    private float getValueFromSlider(float min, float max, float sliderValue){
        float inter = max - min;
        float percent = sliderValue / 100f;
        return min + (inter * percent);
    }

 }
