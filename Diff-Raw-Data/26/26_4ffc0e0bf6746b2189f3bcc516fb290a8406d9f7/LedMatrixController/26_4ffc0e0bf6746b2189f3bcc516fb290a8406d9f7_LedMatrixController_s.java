 package be.kuleuven.med.brainfuck.controller;
 
 import java.awt.event.ActionEvent;
 import java.util.EventObject;
 import java.util.List;
 
 import javax.swing.AbstractButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Task;
 import org.jdesktop.application.Task.BlockingScope;
 import org.jdesktop.beansbinding.AbstractBindingListener;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Binding;
 import org.jdesktop.beansbinding.Binding.ValueResult;
 import org.jdesktop.beansbinding.BindingGroup;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.ELProperty;
 import org.jdesktop.beansbinding.PropertyStateEvent;
 import org.jdesktop.swingbinding.SwingBindings;
 
 import be.kuleuven.med.brainfuck.component.AppComponent;
 import be.kuleuven.med.brainfuck.connector.LedMatrixConnector;
 import be.kuleuven.med.brainfuck.connector.SerialPortConnector;
 import be.kuleuven.med.brainfuck.connector.ThorlabsDC2100Connector;
 import be.kuleuven.med.brainfuck.connector.ThorlabsDC2100Connector.OperationMode;
 import be.kuleuven.med.brainfuck.domain.settings.ExperimentSettings;
 import be.kuleuven.med.brainfuck.domain.settings.LedPosition;
 import be.kuleuven.med.brainfuck.domain.settings.LedSettings;
 import be.kuleuven.med.brainfuck.model.LedMatrixGfxModel;
 import be.kuleuven.med.brainfuck.model.LedMatrixGfxSelectionModel;
 import be.kuleuven.med.brainfuck.model.LedMatrixPanelModel;
 import be.kuleuven.med.brainfuck.modelbuilder.LedMatrixGfxModelBuilder;
 import be.kuleuven.med.brainfuck.modelbuilder.LedMatrixGfxSelectionModelBuilder;
 import be.kuleuven.med.brainfuck.task.AbstractTask;
 import be.kuleuven.med.brainfuck.view.LedMatrixGfxView;
 import be.kuleuven.med.brainfuck.view.LedMatrixPanelView;
 
 @AppComponent
 public class LedMatrixController {
 
 	public static final String UPDATE_SERIAL_PORTS_ACTION = "updateSerialPortNames";
 
 	public static final String INIT_LED_MATRIX_CONNECTOR_ACTION = "initLedMatrixConnector";
 	
 	public static final String INIT_THORLABS_CONNECTOR_ACTION = "initThorlabsConnector";
 	
 	public static final String CLOSE_SERIAL_PORT_CONNECTOR_ACTION = "closeSerialPortConnector";
 
 	public static final String UPDATE_LED_MATRIX_ACTION = "updateLedMatrix";
 
 	public static final String START_EXPERIMENT_ACTION = "startExperiment";
 
 	public static final String TOGGLE_LED_ACTION = "toggleLed";
 
 	public final static BeanProperty<JComponent, String> TEXT = BeanProperty.create("text");
 
 	public final static BeanProperty<JComponent, Boolean> ENABLED = BeanProperty.create("enabled");
 
 	private LedMatrixPanelView ledMatrixPanelView;
 	
 	private LedMatrixGfxView ledMatrixGfxView;
 
 	private LedMatrixPanelModel ledMatrixPanelModel;
 
 	private LedMatrixGfxModel ledMatrixGfxModel;
 
 	private LedMatrixConnector ledMatrixConnector;
 
 	private ThorlabsDC2100Connector thorlabsConnector;
 
 	public LedMatrixController(LedMatrixPanelModel ledMatrixPanelModel, LedMatrixGfxModel ledMatrixGfxModel, 
 			LedMatrixConnector ledMatrixConnector, ThorlabsDC2100Connector thorlabsConnector) {
 		this.ledMatrixPanelModel = ledMatrixPanelModel;
 		this.ledMatrixGfxModel = ledMatrixGfxModel;
 		this.ledMatrixConnector = ledMatrixConnector;
 		this.thorlabsConnector = thorlabsConnector;
 	}
 
 	public void initViews(LedMatrixPanelView ledMatrixPanelView, LedMatrixGfxView ledMatrixGfxView) {
 		this.ledMatrixPanelView = ledMatrixPanelView;
 		this.ledMatrixGfxView = ledMatrixGfxView;
 
 		BindingGroup bindingGroup = new BindingGroup();
 		Binding<?, Boolean, ? extends JComponent, Boolean> enabledBinding = null;
 		Binding<?, Integer, ? extends JComponent, ?> valueBinding = null;
 		// bind led matrix connector info
 		BeanProperty<LedMatrixPanelModel, List<String>> serialPortNamesProperty = BeanProperty.create("serialPortNames");
 		Binding<?, ?, ?, ?> comboBoxBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, serialPortNamesProperty, ledMatrixPanelView.getLedMatrixConnectorBox());
 		bindingGroup.addBinding(comboBoxBinding);
 		BeanProperty<JComboBox<?>, String> selectedItemProperty = BeanProperty.create("selectedItem");
 		BeanProperty<LedMatrixPanelModel, String> selectedLedMatrixPortNameProperty = BeanProperty.create("selectedLedMatrixPortName");
 		Binding<JComboBox<?>, String, LedMatrixPanelModel, String> selectedElementBinding = 
 				Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelView.getLedMatrixConnectorBox(), selectedItemProperty, ledMatrixPanelModel, selectedLedMatrixPortNameProperty);
 		bindingGroup.addBinding(selectedElementBinding);
 		// bind led matrix connector info enabled state
 		ELProperty<LedMatrixPanelModel, Boolean> ledMatrixConnectorInitializedProperty = ELProperty.create("${!ledMatrixConnectorInitialized}");
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, ledMatrixConnectorInitializedProperty, ledMatrixPanelView.getLedMatrixConnectorBox(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		// bind width and height matrix properties
 		BeanProperty<LedMatrixPanelModel, Integer> widthProperty = BeanProperty.create("width");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, widthProperty, ledMatrixPanelView.getRowTextField(), TEXT);
 		bindingGroup.addBinding(valueBinding);
 		BeanProperty<LedMatrixPanelModel, Integer> heightProperty = BeanProperty.create("height");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, heightProperty, ledMatrixPanelView.getColumnTextField(), TEXT);
 		bindingGroup.addBinding(valueBinding);
 		// bind row and column pin numbers
 		BeanProperty<LedMatrixGfxModel, Integer> pinRowProperty = BeanProperty.create("ledMatrixGfxSelectionModel.rowPin");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixGfxModel, pinRowProperty, ledMatrixPanelView.getRowPinTextField(), TEXT);
 		valueBinding.addBindingListener(new AbstractBindingListener() {
 
 			@Override
 			public void targetChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
 				@SuppressWarnings("rawtypes")
 				ValueResult targetValueForSource = binding.getTargetValueForSource();
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				for(LedSettings ledSettings : ledMatrixGfxSelectionModel.getSelectedLedSettings()) {
 					ledSettings.setRowPin((Integer) targetValueForSource.getValue());
 				}
 			}
 			
 		});
 		bindingGroup.addBinding(valueBinding);
 		ELProperty<LedMatrixGfxModel, Boolean> rowSelectedProperty = ELProperty.create("${ledMatrixGfxSelectionModel.rowSelected && !illuminated}");
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixGfxModel, rowSelectedProperty, ledMatrixPanelView.getRowPinTextField(), ENABLED);
 		enabledBinding.setTargetNullValue(false);
 		bindingGroup.addBinding(enabledBinding);
 		BeanProperty<LedMatrixGfxModel, Integer> pinColumnProperty = BeanProperty.create("ledMatrixGfxSelectionModel.columnPin");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixGfxModel, pinColumnProperty, ledMatrixPanelView.getColumnPinTextField(), TEXT);
 		valueBinding.addBindingListener(new AbstractBindingListener() {
 
 			@Override
 			public void targetChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
 				@SuppressWarnings("rawtypes")
 				ValueResult targetValueForSource = binding.getTargetValueForSource();
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				for(LedSettings ledSettings : ledMatrixGfxSelectionModel.getSelectedLedSettings()) {
 					ledSettings.setColumnPin((Integer) targetValueForSource.getValue());
 				}
 			}
 			
 		});
 		bindingGroup.addBinding(valueBinding);
 		ELProperty<LedMatrixGfxModel, Boolean> columnSelectedProperty = ELProperty.create("${ledMatrixGfxSelectionModel.columnSelected && !illuminated}");
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixGfxModel, columnSelectedProperty, ledMatrixPanelView.getColumnPinTextField(), ENABLED);
 		enabledBinding.setTargetNullValue(false);
 		bindingGroup.addBinding(enabledBinding);
 		
 		ELProperty<LedMatrixController, Boolean> ledSettingsSelectedProperty = ELProperty.create("${!ledMatrixGfxModel.ledMatrixGfxSelectionModel.cleared && !ledMatrixPanelModel.experimentRunning}");
 		BeanProperty<LedMatrixGfxModel, Integer> secondsToRunProperty = BeanProperty.create("ledMatrixGfxSelectionModel.secondsToRun");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixGfxModel, secondsToRunProperty, ledMatrixPanelView.getSecondsToRunTextField(), TEXT);
 		valueBinding.addBindingListener(new AbstractBindingListener() {
 
 			@Override
 			public void targetChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
 				@SuppressWarnings("rawtypes")
 				ValueResult targetValueForSource = binding.getTargetValueForSource();
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				for(LedSettings ledSettings : ledMatrixGfxSelectionModel.getSelectedLedSettings()) {
 					ledSettings.setSecondsToRun((Integer) targetValueForSource.getValue());
 				}
 			}
 			
 		});
 		bindingGroup.addBinding(valueBinding);
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ledSettingsSelectedProperty, ledMatrixPanelView.getSecondsToRunTextField(), ENABLED);
 		enabledBinding.setTargetNullValue(false);
 		bindingGroup.addBinding(enabledBinding);
 		
 		BeanProperty<LedMatrixGfxModel, Integer> flickerFrequencyProperty = BeanProperty.create("ledMatrixGfxSelectionModel.flickerFrequency");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixGfxModel, flickerFrequencyProperty, ledMatrixPanelView.getFlickerFrequencyTextField(), TEXT);
 		valueBinding.addBindingListener(new AbstractBindingListener() {
 
 			@Override
 			public void targetChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
 				@SuppressWarnings("rawtypes")
 				ValueResult targetValueForSource = binding.getTargetValueForSource();
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				for(LedSettings ledSettings : ledMatrixGfxSelectionModel.getSelectedLedSettings()) {
 					ledSettings.setFlickerFrequency((Integer) targetValueForSource.getValue());
 				}
 			}
 			
 		});
 		bindingGroup.addBinding(valueBinding);
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ledSettingsSelectedProperty, ledMatrixPanelView.getFlickerFrequencyTextField(), ENABLED);
 		enabledBinding.setTargetNullValue(false);
 		bindingGroup.addBinding(enabledBinding);
 		
 		// bind led controls (just the enabled state)
 		ELProperty<LedMatrixController, Boolean> ledControlsEnabledProperty = ELProperty.create("${!ledMatrixGfxModel.ledMatrixGfxSelectionModel.cleared && ledMatrixPanelModel.ledMatrixConnectorInitialized && !ledMatrixPanelModel.experimentRunning}");
 		BeanProperty<LedMatrixGfxModel, Integer> intensityProperty = BeanProperty.create("ledMatrixGfxSelectionModel.intensity");
 		BeanProperty<JSlider, Integer> valueProperty = BeanProperty.create("value");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixGfxModel, intensityProperty, ledMatrixPanelView.getIntensitySlider(), valueProperty);
 		bindingGroup.addBinding(valueBinding);
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ledControlsEnabledProperty, ledMatrixPanelView.getIntensitySlider(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ledControlsEnabledProperty, ledMatrixPanelView.getToggleLedButton(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		// bind thorlabs connector info 
 		comboBoxBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, serialPortNamesProperty, ledMatrixPanelView.getThorlabsConnectorBox());
 		bindingGroup.addBinding(comboBoxBinding);
 		BeanProperty<LedMatrixPanelModel, String> selectedThorlabsPortNameProperty = BeanProperty.create("selectedThorlabsPortName");
 		selectedElementBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelView.getThorlabsConnectorBox(), selectedItemProperty, ledMatrixPanelModel, selectedThorlabsPortNameProperty);
 		bindingGroup.addBinding(selectedElementBinding);
 		// bind thorlabs select box enabled state
 		ELProperty<LedMatrixPanelModel, Boolean> thorlabsConnectorInitializedProperty = ELProperty.create("${!thorlabsConnectorInitialized}");
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, thorlabsConnectorInitializedProperty, ledMatrixPanelView.getThorlabsConnectorBox(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		// bind experiment settings controls
 		BeanProperty<LedMatrixPanelModel, Integer> cyclesToRunProperty = BeanProperty.create("experimentSettings.cyclesToRun");
 		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, ledMatrixPanelModel, cyclesToRunProperty, ledMatrixPanelView.getCyclesToRunTextField(), TEXT);
 		bindingGroup.addBinding(valueBinding);
 		ELProperty<LedMatrixPanelModel, Boolean> experimentInitializedNotStarted = ELProperty.create("${ledMatrixConnectorInitialized && experimentInitialized && !experimentRunning}"); 
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, experimentInitializedNotStarted, ledMatrixPanelView.getCyclesToRunTextField(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		ELProperty<LedMatrixPanelModel, Boolean> experimentInitialized = ELProperty.create("${ledMatrixConnectorInitialized && experimentInitialized}"); 
 		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, ledMatrixPanelModel, experimentInitialized, ledMatrixPanelView.getStartExperimentButton(), ENABLED);
 		bindingGroup.addBinding(enabledBinding);
 		bindingGroup.bind();
 	}
 	
 	@Action
 	public void updateLedMatrix() {
 		// should regenerate led matrix??
 		int width = ledMatrixPanelModel.getWidth();
 		int height = ledMatrixPanelModel.getHeight();
 		if (width > 0 && height > 0) {
 			ledMatrixPanelModel.setExperimentInitialized(true);
 			ledMatrixGfxModel = new LedMatrixGfxModelBuilder(ledMatrixGfxModel).
 					resizeMatrix(width, height).build();
 			ledMatrixGfxView.resizeMatrix(ledMatrixGfxModel.getLedSettingsMap().keySet(), width, height);
 			// should send data to arduino as well??
 			ledMatrixGfxView.repaint();
 		}
 	}
 	
 	/**
 	 * Just set ledSettings as the only selected item
 	 * @param ledSettings The setttings
 	 */
 	public void updateSingleSelection(LedSettings ledSettings) {
 		// single element selectièon in this case
 		LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = 
 				LedMatrixGfxSelectionModelBuilder.of(ledSettings);
 		ledMatrixGfxModel.setLedMatrixGfxSelectionModel(ledMatrixGfxSelectionModel);
 	}
 
 	public void updateSelection(LedPosition ledPosition, boolean isShiftDown, boolean isControlDown) {
 		if (!ledMatrixGfxModel.isIlluminated()) {
 			if (ledPosition != null) {
 				LedSettings ledSettings = ledMatrixGfxModel.getLedSettings(ledPosition);
 				if (isShiftDown) {
 					// TODO implement shift down
 				} else if (isControlDown) {
 					LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 					ledMatrixGfxSelectionModel = new LedMatrixGfxSelectionModelBuilder(ledMatrixGfxSelectionModel)
 					.addRemoveLedSettings(ledSettings).build();
 					ledMatrixGfxModel.setLedMatrixGfxSelectionModel(ledMatrixGfxSelectionModel);
 				} else {
 					updateSingleSelection(ledSettings);
 				}
 			} else {
 				clearSelection();
 			}
 			ledMatrixGfxView.repaint();
 		}
 	}
 	
 	private void clearSelection() {
 		LedMatrixGfxSelectionModel emptySelection = LedMatrixGfxSelectionModelBuilder.EMPTY_SELECTION;
 		ledMatrixGfxModel.setLedMatrixGfxSelectionModel(emptySelection);
 	}
 	
 	private void toggleName(AbstractButton button, String actionName, boolean selected) {
 		StringBuilder stringBuilder = new StringBuilder(actionName + ".Action.");
 		stringBuilder.append(selected ? "selectedText" : "text");
 		button.setText(getResourceMap().getString(stringBuilder.toString()));
 	}
 
 	private void toggleName(AbstractButton button, String actionName) {
 		toggleName(button, actionName, button.isSelected());
 	}
 
 	@Action(block=BlockingScope.APPLICATION)
 	public Task<?, ?> initLedMatrixConnector(final ActionEvent event) {
 		
 		final String selectedLedMatrixPortName  = ledMatrixPanelModel.getSelectedLedMatrixPortName();
 		if (selectedLedMatrixPortName != null && !"".equals(selectedLedMatrixPortName) && 
 				!ledMatrixPanelModel.isLedMatrixConnectorInitialized()) {
 			return new AbstractTask<Void, Void>(INIT_LED_MATRIX_CONNECTOR_ACTION) {
 
 				protected Void doInBackground() throws Exception {
 					message("startMessage", selectedLedMatrixPortName);
 					ledMatrixConnector.initialize(selectedLedMatrixPortName);
 					// disabling all leds could also be done by using pullup/pulldown resistors?
 					ledMatrixConnector.disableAllLeds(ledMatrixGfxModel.getLedMatrixSettings());
 					// will disable enabled state of in the gui..
 					ledMatrixPanelModel.setLedMatrixConnectorInitialized(true);
 					// should be updating the view on EDT
 					message("endMessage", selectedLedMatrixPortName);
 					return null;
 				}
 
 				@Override
 				protected void succeeded(Void result) {
 					toggleName((AbstractButton) event.getSource(), INIT_LED_MATRIX_CONNECTOR_ACTION, 
 							ledMatrixPanelModel.isLedMatrixConnectorInitialized());
 					super.succeeded(result);
 				}
 
 			};
 		} else {
 			return new AbstractTask<Void, Void>(CLOSE_SERIAL_PORT_CONNECTOR_ACTION) {
 
 				protected Void doInBackground() throws Exception {
 					message("startMessage", selectedLedMatrixPortName);
 					ledMatrixConnector.close();
 					ledMatrixPanelModel.setLedMatrixConnectorInitialized(false);
 					message("endMessage", selectedLedMatrixPortName);
 					return null;
 				}
 				
 				@Override
 				protected void succeeded(Void result) {
 					toggleName((AbstractButton) event.getSource(), INIT_LED_MATRIX_CONNECTOR_ACTION, 
 							ledMatrixPanelModel.isLedMatrixConnectorInitialized());
 					super.succeeded(result);
 				}
 
 			};
 		}
 	}
 	
 
 	@Action(block=BlockingScope.APPLICATION)
 	public Task<?, ?> initThorlabsConnector(final ActionEvent event) {
 		final String selectedThorlabsPortName  = ledMatrixPanelModel.getSelectedThorlabsPortName();
 		if (selectedThorlabsPortName != null && !"".equals(selectedThorlabsPortName) && 
 				!ledMatrixPanelModel.isThorlabsConnectorInitialized()) {
 			return new AbstractTask<Void, Void>(INIT_THORLABS_CONNECTOR_ACTION) {
 
 				protected Void doInBackground() throws Exception {
 					message("startMessage", selectedThorlabsPortName);
 					thorlabsConnector.initialize(selectedThorlabsPortName);
 					thorlabsConnector.setOperationMode(OperationMode.PWM);
 					thorlabsConnector.setPwmCounts(0);
 					// will disable enabled state of in the gui..
 					ledMatrixPanelModel.setThorlabsConnectorInitialized(true);
 					// should be updating the view on EDT
 					message("endMessage", selectedThorlabsPortName);
 					return null;
 				}
 
 				@Override
 				protected void succeeded(Void result) {
 					toggleName((AbstractButton) event.getSource(), INIT_THORLABS_CONNECTOR_ACTION, 
 							ledMatrixPanelModel.isThorlabsConnectorInitialized());
 					super.succeeded(result);
 				}
 
 			};
 		} else {
 			return new AbstractTask<Void, Void>(CLOSE_SERIAL_PORT_CONNECTOR_ACTION) {
 
 				protected Void doInBackground() throws Exception {
 					message("startMessage", selectedThorlabsPortName);
 					thorlabsConnector.close();
 					ledMatrixPanelModel.setThorlabsConnectorInitialized(false);
 					message("endMessage", selectedThorlabsPortName);
 					return null;
 				}
 				
 				@Override
 				protected void succeeded(Void result) {
 					toggleName((AbstractButton) event.getSource(), INIT_THORLABS_CONNECTOR_ACTION, 
 							ledMatrixPanelModel.isThorlabsConnectorInitialized());
 					super.succeeded(result);
 				}
 
 			};
 		}
 	}
 
 	@Action(block=BlockingScope.APPLICATION)
 	public Task<?, ?> updateSerialPortNames() {
 		return new AbstractTask<Void, Void>(UPDATE_SERIAL_PORTS_ACTION) {
 
 			protected Void doInBackground() throws Exception {
 				message("startMessage");
 				List<String> serialPortNames = SerialPortConnector.getSerialPortNames();
 				String selectedLedMatrixPortName = ledMatrixConnector.getSelectedSerialPortName();
 				String selectedThorlabsPortName = thorlabsConnector.getSelectedSerialPortName();
 				// should be updating the view on EDT
 				ledMatrixPanelModel.setSerialPortNames(serialPortNames);
 				ledMatrixPanelModel.setSelectedLedMatrixPortName(selectedLedMatrixPortName);
 				ledMatrixPanelModel.setSelectedThorlabsPortName(selectedThorlabsPortName);
 				message("endMessage");
 				return null;
 			}
 
 		};
 	}
 
 	public void adjustIntensity(ChangeEvent event) {
 		getContext().getTaskService().execute(buildToggleLedTask(event));
 	}
 
 	@Action
 	public Task<?, ?> toggleLed(final ActionEvent event) {
 		return buildToggleLedTask(event);
 	}
 
 	private Task<?, ?> buildToggleLedTask(final EventObject event) {
 		return new AbstractTask<Void, Void>(TOGGLE_LED_ACTION) {
 
 			protected Void doInBackground() throws Exception {
 				Object source = event.getSource();
 				boolean illuminated = ledMatrixPanelView.getToggleLedButton().isSelected();
 				if (source instanceof JToggleButton) {
 					JToggleButton button = (JToggleButton) event.getSource();
 					toggleName(button, TOGGLE_LED_ACTION);
 					ledMatrixGfxModel.setIlluminated(illuminated);
 				} 
 				LedMatrixGfxSelectionModel ledMatrixGfxSelectionModel = ledMatrixGfxModel.getLedMatrixGfxSelectionModel();
 				int index = 0; 
 				boolean atLeastOneSelected = false;
 				for (LedSettings ledSettings : ledMatrixGfxSelectionModel.getSelectedLedSettings()) {
 					if (source instanceof JSlider) {
 						JSlider slider = (JSlider) event.getSource();
 						int sliderValue = slider.getValue();
 						// don't change anything if slider didn't really move
 						if (index == 0 && ledSettings.getIntensity() == sliderValue) {
 							return null;
 						}
 						ledSettings.setIntensity(sliderValue);
 					}
 					boolean selected = ledMatrixGfxModel.isSelected(ledSettings);
 					atLeastOneSelected |= selected;
 					ledMatrixConnector.toggleLed(ledSettings, selected && illuminated);
 					// set flicker frequency + intensity for this led
 					index++;
 				}
 				// first set led state, then poweron the PSU
 				thorlabsConnector.setPwmFrequency(ledMatrixGfxSelectionModel.getFlickerFrequency());
 				thorlabsConnector.setPwmCurrent(ledMatrixGfxSelectionModel.getIntensity());
 				thorlabsConnector.setLedOn(atLeastOneSelected && illuminated);
 				// update gfx illumination state
 				ledMatrixGfxView.repaint();
 				message("endMessage");
 				return null;
 			}
 
 		};
 	}
 	
 	public void stopExperiment() {
 		clearSelection();
 		thorlabsConnector.setLedOn(false);
 		ledMatrixConnector.disableAllLeds(ledMatrixGfxModel.getLedMatrixSettings());
 		ledMatrixGfxModel.setIlluminated(false);
 		ledMatrixGfxView.repaint();
 	}
 
 	@Action
 	public Task<?, ?> startExperiment(final ActionEvent event) {
 		final JToggleButton source = (JToggleButton) event.getSource();
 		toggleName(source, START_EXPERIMENT_ACTION);
 		// set selected state to enable/disable ui controls
 		ledMatrixPanelModel.setExperimentRunning(source.isSelected());
 		return new AbstractTask<Void, Void>(START_EXPERIMENT_ACTION) {
 
 			private void doPeriodicToggle(LedSettings ledSettings, 
 					int flickerFrequency, long timePerLed) {
 				try {
 					if (flickerFrequency == 0 || thorlabsConnector.isInitialized()) {
 						ledMatrixGfxModel.setIlluminated(true);
 						updateSingleSelection(ledSettings);
 						ledMatrixGfxView.repaint();
 						message("progressMessage", ledSettings.getLedPosition());
 						ledMatrixConnector.toggleLed(ledSettings, true);
 						thorlabsConnector.setLedOn(true);
 						Thread.sleep(timePerLed * 1000);
 						thorlabsConnector.setLedOn(false);
 						ledMatrixConnector.toggleLed(ledSettings, false);
 						ledMatrixGfxModel.setIlluminated(false);
 					} else {
 						long endTime = System.currentTimeMillis() + timePerLed * 1000;
 						while(System.currentTimeMillis() < endTime) {
 							ledMatrixGfxModel.setIlluminated(true);
 							updateSingleSelection(ledSettings);
 							ledMatrixGfxView.repaint();
 							message("progressMessage", ledSettings.getLedPosition());
 							ledMatrixConnector.toggleLed(ledSettings, true);
 							Thread.sleep(flickerFrequency);
 							ledMatrixConnector.toggleLed(ledSettings, false);
 							ledMatrixGfxModel.setIlluminated(false);
 						}
 					}
 					clearSelection();
 					ledMatrixPanelView.repaint();
 				} catch(InterruptedException e) {
 					getLogger().error("Led toggling for " + ledSettings.getLedPosition() + " ended unexpectedly");
 				}
 			}
 
 			protected Void doInBackground() throws Exception {
 				stopExperiment();
 				if (ledMatrixPanelModel.isExperimentRunning()) {
 					ExperimentSettings experimentSettings = ledMatrixPanelModel.getExperimentSettings();
 					message("startMessage", experimentSettings.getCyclesToRun());
 					for (int i = 0; i < experimentSettings.getCyclesToRun(); i++) {
 						for (int j = 0; j < ledMatrixGfxModel.getWidth(); j++) {
 							for (int k = 0; k < ledMatrixGfxModel.getHeight() && ledMatrixPanelModel.isExperimentRunning(); k++) {
 								LedPosition ledPosition = LedPosition.ledPositionFor(j, k);
 								LedSettings ledSettings = ledMatrixGfxModel.getLedSettings(ledPosition);
 								message("progressMessage", ledPosition);
 								// will set intensity for thorlabs driver in case it's connected??
 								int flickerFrequency = ledSettings.getFlickerFrequency();
 								thorlabsConnector.setPwmFrequency(flickerFrequency);
 								thorlabsConnector.setPwmCurrent(ledSettings.getIntensity());
 								doPeriodicToggle(ledSettings, flickerFrequency, ledSettings.getSecondsToRun());
 							}
 						}
 					}
 					message("endMessage");
 				}
 				return null;
 			}
 
 			@Override
 			protected void finished() {
 				stopExperiment();
 				ledMatrixPanelModel.setExperimentRunning(false);
 				source.setSelected(false);
 				toggleName(source, START_EXPERIMENT_ACTION, false);
 			}
 
 		};
 	}
 	
 	public LedMatrixPanelModel getLedMatrixPanelModel() {
 		return ledMatrixPanelModel;
 	}
 
 	public LedMatrixGfxModel getLedMatrixGfxModel() {
 		return ledMatrixGfxModel;
 	}
 
 }
