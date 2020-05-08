 package com.alexrnl.subtitlecorrector.gui.view;
 
 import static com.alexrnl.subtitlecorrector.common.TranslationKeys.KEYS;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import com.alexrnl.commons.gui.swing.AbstractFrame;
 import com.alexrnl.subtitlecorrector.correctionstrategy.Strategy;
 import com.alexrnl.subtitlecorrector.gui.controller.MainWindowController;
 
 /**
  * TODO
  * @author Alex
  */
 public class MainWindowView extends AbstractFrame {
 	/** Logger */
 	private static Logger			lg					= Logger.getLogger(MainWindowView.class.getName());
 	
 	/** Serial version UID */
 	private static final long		serialVersionUID	= -6742939741527280064L;
 	
 	/** The default insets to use in the window */
 	private static final Insets		DEFAULT_INSETS		= new Insets(5, 5, 5, 5);
 	
 	// GUI elements
 	/** The field where the path of the subtitle will be displayed */
 	private JTextField			subtitleField;
 	/** The button to open the file explorer window to select the subtitle */
 	private JButton				subtitleButton;
 	/** The combo box for the strategy */
 	private JComboBox<Strategy>	strategyComboBox;
 	
 	/** The controller in charge of the view */
 	private MainWindowController controller;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param iconFile
 	 *        the icon file to use for the main window.
 	 * @param controller
 	 *        the controller which handle this view.
 	 */
 	public MainWindowView (final Path iconFile, final MainWindowController controller) {
 		super(KEYS.mainWindow().title(), iconFile, controller);
 	}
 
 	@Override
 	protected void preInit (final Object... parameters) {
 		controller = (MainWindowController) parameters[0];
 		
 	}
 	
 	@Override
 	protected void build () {
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
 		buildMainContainer();
 		installListeners();
 	}
 
 	/**
 	 * Build the main container of the main window.
 	 */
 	private void buildMainContainer () {
 		final Container panel = getContentPane();
 		panel.setLayout(new GridBagLayout());
 		
 		int xIndex = 0;
 		int yIndex = 0;
 		final GridBagConstraints c = new GridBagConstraints(xIndex, yIndex, 1, 1, 0, 0,
 				GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.HORIZONTAL,
 				DEFAULT_INSETS, 0, 0);
 		add(new JLabel(KEYS.mainWindow().subtitleLabel()), c);
 		c.gridy = ++yIndex;
 		add(new JLabel(KEYS.mainWindow().strategyLabel()), c);
 		
 		yIndex = 0;
 		c.gridy = yIndex;
 		c.gridx = ++xIndex;
 		subtitleField = new JTextField();
 		add(subtitleField, c);
 		
 		c.gridx = ++xIndex;
 		subtitleButton = new JButton(KEYS.mainWindow().subtitleButton());
		subtitleButton.
 		add(subtitleButton, c);
 		
 		c.gridx = --xIndex;
 		c.gridy = ++yIndex;
 		c.gridwidth = 2;
 		strategyComboBox = new JComboBox<>();//strategyManager.getStrategies()
 		add(strategyComboBox, c);
 	}
 	
 	/**
 	 * Install the listeners on the components.
 	 */
 	private void installListeners () {
 		if (subtitleField == null || subtitleButton == null || strategyComboBox == null) {
 			throw new IllegalStateException("Cannot install listener on null components, call " +
 					"buildMainContainer before this method");
 		}
 		
 		subtitleField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyTyped (final KeyEvent e) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run () {
 						controller.changeSubtitle(Paths.get(subtitleField.getText()));
 					}
 				});
 			}
 		});
 		subtitleButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed (final ActionEvent e) {
 				final JFileChooser fileChooser = new JFileChooser();
 				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				fileChooser.setMultiSelectionEnabled(false);
 				final int answer = fileChooser.showOpenDialog(getFrame());
 				if (answer == JFileChooser.APPROVE_OPTION) {
 					controller.changeSubtitle(fileChooser.getSelectedFile().toPath());
 				} else {
 					if (lg.isLoggable(Level.INFO)) {
 						lg.info("User canceled file selection");
 					}
 				}
 			}
 		});
 		strategyComboBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged (final ItemEvent e) {
 				controller.changeStrategy((Strategy) e.getItem());
 			}
 		});
 	}
 	
 	@Override
 	public void dispose () {
 		super.dispose();
 		controller.dispose();
 	}
 	
 	@Override
 	public void modelPropertyChange (final PropertyChangeEvent evt) {
 		if (!isReady()) {
 			lg.warning("Main window is not ready, cannot update view (" + evt + ")");
 			return;
 		}
 		
 		if (evt == null || evt.getPropertyName() == null) {
 			lg.warning("Change event is null, or property name is null, cannot process event: "
 					+ evt);
 			return;
 		}
 		
 		switch (evt.getPropertyName()) {
 			case MainWindowController.SUBTITLE_PROPERTY:
 				subtitleField.setText(((Path)evt.getNewValue()).toString());
 				break;
 			case MainWindowController.STRATEGY_PROPERTY:
 				final Strategy strategy = (Strategy) evt.getNewValue();
 				strategyComboBox.setSelectedItem(strategy);
 				break;
 			default:
 				lg.info("Model property not handle by main window: " + evt);
 				break;
 		}
 	}
 }
