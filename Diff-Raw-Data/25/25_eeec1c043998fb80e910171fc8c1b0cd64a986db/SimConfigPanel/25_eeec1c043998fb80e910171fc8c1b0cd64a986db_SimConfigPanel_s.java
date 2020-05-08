 package evaluation.simulator.gui.customElements;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import evaluation.simulator.gui.actionListeners.LoadButtonAction;
 import evaluation.simulator.gui.actionListeners.ResetButtonAction;
 import evaluation.simulator.gui.actionListeners.SaveButtonAction;
 
 @SuppressWarnings("serial")
 public class SimConfigPanel extends JPanel {
 
 	private static SimConfigPanel _instance = null;
 
 	public static SimConfigPanel getInstance() {
 		if (_instance == null) {
 			_instance = new SimConfigPanel();
 		}
 		return _instance;
 	}
 
 	public static void setStatusofSaveButton(Boolean enabled) {
 		getInstance().saveButton.setEnabled(enabled);
 		// getInstance()._accordian.repaint();
 	}
 
 	// private Accordion _accordian;
 	private JPanel buttonBar;
 	private JButton loadButton;
 
 	private PluginPanel pluginPanel;
 
 	private JButton resetButton;
 	private JButton saveButton;
 
 	private SimConfigPanel() {
 		this.init();
 	}
 
 	private void init() {
 
 		// this._accordian = new Accordion();
 		// this.plugInSelection = new PlugInSelection();
 
 		this.pluginPanel = new PluginPanel();
 
 		this.buttonBar = new JPanel();
 
 		this.loadButton = new JButton("Load");
 		this.loadButton.addActionListener(new LoadButtonAction());
 		this.buttonBar.add(this.loadButton, BorderLayout.SOUTH);
 		this.saveButton = new JButton("Save");
 		this.saveButton.addActionListener(new SaveButtonAction());
 		this.buttonBar.add(this.saveButton, BorderLayout.SOUTH);
 
 		this.resetButton = new JButton("Reset");
 		this.resetButton.addActionListener(new ResetButtonAction());
 		this.buttonBar.add(this.resetButton, BorderLayout.SOUTH);
 
 		this.setLayout(new BorderLayout());
 		// this.add(this.plugInSelection, BorderLayout.NORTH);
 		// this.add(this._accordian, BorderLayout.CENTER);
 
		this.add(this.pluginPanel, BorderLayout.NORTH);
 		this.add(this.buttonBar, BorderLayout.SOUTH);
 	};
 }
