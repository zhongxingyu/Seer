 package at.fhv.audioracer.camera.pivot;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.apache.pivot.beans.BXML;
 import org.apache.pivot.beans.Bindable;
 import org.apache.pivot.collections.Map;
 import org.apache.pivot.util.Resources;
 import org.apache.pivot.wtk.Alert;
 import org.apache.pivot.wtk.Button;
 import org.apache.pivot.wtk.ButtonPressListener;
 import org.apache.pivot.wtk.Component;
 import org.apache.pivot.wtk.ComponentMouseButtonListener;
 import org.apache.pivot.wtk.Label;
 import org.apache.pivot.wtk.PushButton;
 import org.apache.pivot.wtk.Slider;
 import org.apache.pivot.wtk.SliderValueListener;
 import org.apache.pivot.wtk.Spinner;
 import org.apache.pivot.wtk.SpinnerSelectionListener;
 import org.apache.pivot.wtk.SplitPane;
 
 public class CameraSplitPane extends SplitPane implements Bindable {
 	
 	private static final int HUE_VALUE_RANGE = 20;
 	
 	@BXML
 	private Spinner _cameraIdSpinner;
 	@BXML
 	private PushButton _cameraSelectedButton;
 	@BXML
 	private PushButton _calibrationStepButton;
 	@BXML
 	private PushButton _calibrationFinishedButton;
 	@BXML
 	private PushButton _loadCalibrationButton;
 	@BXML
 	private PushButton _storeCalibrationButton;
 	@BXML
 	private PushButton _startPositioningButton;
 	@BXML
 	private PushButton _rotateButton;
 	@BXML
 	private Slider _rotationSlider;
 	@BXML
 	private Label _rotationLabel;
 	@BXML
 	private PushButton _startSelectGameAreaButton;
 	@BXML
 	private PushButton _gameAreaSelectedButton;
 	@BXML
 	private PushButton _loadGameAreaButton;
 	@BXML
 	private PushButton _storeGameAreaButton;
 	@BXML
 	private PushButton _directionHueButton;
 	@BXML
 	private PushButton _carConfiguredButton;
 	@BXML
 	private PushButton _allCarsDetectedButton;
 	
 	// HUE sliders
 	@BXML
 	private Slider _colorLowerSlider;
 	@BXML
 	private Slider _colorUpperSlider;
 	@BXML
 	private Slider _saturationLowerSlider;
 	@BXML
 	private Slider _saturationUpperSlider;
 	@BXML
 	private Slider _valueLowerSlider;
 	@BXML
 	private Slider _valueUpperSlider;
 	@BXML
 	private Label _colorLowerLabel;
 	@BXML
 	private Label _colorUpperLabel;
 	@BXML
 	private Label _saturationLowerLabel;
 	@BXML
 	private Label _saturationUpperLabel;
 	@BXML
 	private Label _valueLowerLabel;
 	@BXML
 	private Label _valueUpperLabel;
 	
 	@BXML
 	private CameraMapComponent _cameraMapComponent;
 	
 	private ComponentMouseButtonListener _mouseButtonListener;
 	
 	public CameraSplitPane() {
 		_mouseButtonListener = new ComponentMouseButtonListener.Adapter() {
 			@Override
 			public boolean mouseClick(Component component,
 					org.apache.pivot.wtk.Mouse.Button button, int x, int y, int count) {
 				
 				int[] values = _cameraMapComponent.getHueValues(x, y);
 				
 				int lowerHue = values[0] - HUE_VALUE_RANGE;
 				int upperHue = values[0] + HUE_VALUE_RANGE;
 				int lowerSaturation = values[1] - (2 * HUE_VALUE_RANGE);
 				int upperSaturation = values[1] + (2 * HUE_VALUE_RANGE);
 				int lowerValue = values[2] - (2 * HUE_VALUE_RANGE);
 				int upperValue = values[2] + (2 * HUE_VALUE_RANGE);
 				
 				if (lowerHue < _colorLowerSlider.getStart()) {
 					lowerHue = _colorLowerSlider.getStart();
 				}
 				if (upperHue > _colorUpperSlider.getEnd()) {
 					upperHue = _colorUpperSlider.getEnd();
 				}
 				if (lowerSaturation < _saturationLowerSlider.getStart()) {
 					lowerSaturation = _saturationLowerSlider.getStart();
 				}
 				if (upperSaturation > _saturationUpperSlider.getEnd()) {
 					upperSaturation = _saturationUpperSlider.getEnd();
 				}
 				if (lowerValue < _valueLowerSlider.getStart()) {
 					lowerValue = _valueLowerSlider.getStart();
 				}
 				if (upperValue > _valueUpperSlider.getEnd()) {
 					upperValue = _valueUpperSlider.getEnd();
 				}
 				
 				_colorLowerSlider.setValue(lowerHue);
 				_colorUpperSlider.setValue(upperHue);
 				_saturationLowerSlider.setValue(lowerSaturation);
 				_saturationUpperSlider.setValue(upperSaturation);
 				_valueLowerSlider.setValue(lowerValue);
 				_valueUpperSlider.setValue(upperValue);
 				
 				return true;
 			}
 		};
 	}
 	
 	@Override
 	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
 		_calibrationStepButton.setEnabled(false);
 		_calibrationFinishedButton.setEnabled(false);
 		_loadCalibrationButton.setEnabled(false);
 		_storeCalibrationButton.setEnabled(false);
 		_startPositioningButton.setEnabled(false);
 		_rotateButton.setEnabled(false);
 		_rotationSlider.setEnabled(false);
 		_startSelectGameAreaButton.setEnabled(false);
 		_gameAreaSelectedButton.setEnabled(false);
 		_loadGameAreaButton.setEnabled(false);
 		_storeGameAreaButton.setEnabled(false);
 		_directionHueButton.setEnabled(false);
 		_carConfiguredButton.setEnabled(false);
 		_allCarsDetectedButton.setEnabled(false);
 		
 		_cameraIdSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener.Adapter() {
 			@Override
 			public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
 				int id = (int) _cameraIdSpinner.getSelectedItem();
 				_cameraMapComponent.selectCamera(id);
 			}
 		});
 		_cameraMapComponent.selectCamera((int) _cameraIdSpinner.getSelectedItem());
 		_cameraSelectedButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				_cameraIdSpinner.setEnabled(false);
 				_cameraSelectedButton.setEnabled(false);
 				_calibrationStepButton.setEnabled(true);
 				_calibrationFinishedButton.setEnabled(true);
 				_loadCalibrationButton.setEnabled(true);
 				
 				_cameraMapComponent.startCalibration();
 				_cameraMapComponent.setDrawCheesboard(true);
 			}
 		});
 		
 		_calibrationStepButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				if (!_cameraMapComponent.calibrationStep()) {
 					Alert.alert("Detection failed!", getWindow());
 				}
 			}
 		});
 		_calibrationFinishedButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				if (!_cameraMapComponent.endCalibration()) {
 					Alert.alert("Calibration failed!", getWindow());
 					return;
 				}
 				
 				calibrationFinished();
 			}
 		});
 		_loadCalibrationButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				try {
 					if (!_cameraMapComponent.loadCalibration()) {
 						Alert.alert("Calibration loading failed!", getWindow());
 					} else {
 						calibrationFinished();
 					}
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		_storeCalibrationButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				try {
 					_cameraMapComponent.storeCalibration();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		_startPositioningButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				if (!_cameraMapComponent.startPositioning()) {
 					Alert.alert("No cheesboard detected!", getWindow());
 					return;
 				}
 				
 				_cameraMapComponent.setDrawCheesboard(false);
 				
 				_startPositioningButton.setEnabled(false);
 				_rotateButton.setEnabled(true);
 				_rotationSlider.setEnabled(true);
 				_startSelectGameAreaButton.setEnabled(true);
 			}
 		});
 		_rotateButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				_cameraMapComponent.rotate();
 			}
 		});
 		_rotationSlider.getSliderValueListeners().add(new SliderValueListener() {
 			
 			@Override
 			public void valueChanged(Slider slider, int previousValue) {
 				// TODO Auto-generated method stub
 				_cameraMapComponent.setRotation(_rotationSlider.getValue());
 				updateRotationLabel();
 			}
 		});
 		updateRotationLabel();
 		_startSelectGameAreaButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				_rotateButton.setEnabled(false);
 				_rotationSlider.setEnabled(false);
 				_startSelectGameAreaButton.setEnabled(false);
 				_gameAreaSelectedButton.setEnabled(true);
 				
 				_cameraMapComponent.startSelectGameArea();
 			}
 		});
 		
 		_gameAreaSelectedButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				if (!_cameraMapComponent.gameAreaSelected()) {
 					Alert.alert("No game area selected!", getWindow());
 					return;
 				}
 				
 				gameAreaSelected();
 			}
 		});
 		_loadGameAreaButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				if (_cameraMapComponent.loadGameArea()) {
 					gameAreaSelected();
 				}
 			}
 		});
 		_storeGameAreaButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				_cameraMapComponent.storeGameArea();
 			}
 		});
 		
 		_directionHueButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				_directionHueButton.setEnabled(false);
 				_carConfiguredButton.setEnabled(true);
 				_allCarsDetectedButton.setEnabled(true);
 				
 				_cameraMapComponent.directionHueConfigured();
 			}
 		});
 		_carConfiguredButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				if (!_cameraMapComponent.carConfigured()) {
 					Alert.alert("Configuration issue! Car could not be detected!", getWindow());
 				}
 			}
 		});
 		_allCarsDetectedButton.getButtonPressListeners().add(new ButtonPressListener() {
 			
 			@Override
 			public void buttonPressed(Button button) {
 				if (_cameraMapComponent.getMap().getCars().size() == 0) {
 					Alert.alert("At least one car must be detected!", getWindow());
 					return;
 				}
 				
 				_cameraMapComponent.allCarsDetected();
 				
 				_carConfiguredButton.setEnabled(false);
 				_allCarsDetectedButton.setEnabled(false);
 				
 				_cameraMapComponent.getComponentMouseButtonListeners().remove(_mouseButtonListener);
 				
 				CameraApplication.getInstance().allCarsDected();
 			}
 		});
 		
 		// HUE sliders
 		_colorLowerSlider.getSliderValueListeners().add(new SliderValueListener() {
 			
 			@Override
 			public void valueChanged(Slider slider, int previousValue) {
 				int value = slider.getValue();
 				
 				if (value > _colorUpperSlider.getValue()) {
 					_colorUpperSlider.setValue(value);
 				}
 				
 				updateHue();
 			}
 		});
 		_colorUpperSlider.getSliderValueListeners().add(new SliderValueListener() {
 			
 			@Override
 			public void valueChanged(Slider slider, int previousValue) {
 				int value = slider.getValue();
 				
 				if (value < _colorLowerSlider.getValue()) {
 					_colorLowerSlider.setValue(value);
 				}
 				
 				updateHue();
 			}
 		});
 		_saturationLowerSlider.getSliderValueListeners().add(new SliderValueListener() {
 			
 			@Override
 			public void valueChanged(Slider slider, int previousValue) {
 				int value = slider.getValue();
 				
 				if (value > _saturationUpperSlider.getValue()) {
 					_saturationUpperSlider.setValue(value);
 				}
 				
 				updateHue();
 			}
 		});
 		_saturationUpperSlider.getSliderValueListeners().add(new SliderValueListener() {
 			
 			@Override
 			public void valueChanged(Slider slider, int previousValue) {
 				int value = slider.getValue();
 				
 				if (value < _saturationLowerSlider.getValue()) {
 					_saturationLowerSlider.setValue(value);
 				}
 				
 				updateHue();
 			}
 		});
 		_valueLowerSlider.getSliderValueListeners().add(new SliderValueListener() {
 			
 			@Override
 			public void valueChanged(Slider slider, int previousValue) {
 				int value = slider.getValue();
 				
 				if (value > _valueUpperSlider.getValue()) {
 					_valueUpperSlider.setValue(value);
 				}
 				
 				updateHue();
 			}
 		});
 		_valueUpperSlider.getSliderValueListeners().add(new SliderValueListener() {
 			
 			@Override
 			public void valueChanged(Slider slider, int previousValue) {
 				int value = slider.getValue();
 				
 				if (value < _valueLowerSlider.getValue()) {
 					_valueLowerSlider.setValue(value);
 				}
 				
 				updateHue();
 			}
 		});
 		updateHue();
 	}
 	
 	private void calibrationFinished() {
 		_calibrationStepButton.setEnabled(false);
 		_calibrationFinishedButton.setEnabled(false);
 		_loadCalibrationButton.setEnabled(false);
 		_startPositioningButton.setEnabled(true);
 		_loadGameAreaButton.setEnabled(true);
 	}
 	
 	private void updateRotationLabel() {
 		_rotationLabel.setText(_rotationSlider.getValue() + "°");
 	}
 	
 	private void gameAreaSelected() {
 		_startPositioningButton.setEnabled(false);
 		_rotateButton.setEnabled(false);
 		_rotationSlider.setEnabled(false);
 		_startSelectGameAreaButton.setEnabled(false);
 		_gameAreaSelectedButton.setEnabled(false);
 		_loadGameAreaButton.setEnabled(false);
 		_storeGameAreaButton.setEnabled(true);
 		_directionHueButton.setEnabled(true);
 		
 		_cameraMapComponent.getComponentMouseButtonListeners().add(_mouseButtonListener);
 	}
 	
 	private void updateHue() {
 		_cameraMapComponent.updateHueRange(_colorLowerSlider.getValue(),
 				_colorUpperSlider.getValue(), _saturationLowerSlider.getValue(),
 				_saturationUpperSlider.getValue(), _valueLowerSlider.getValue(),
 				_valueUpperSlider.getValue());
 		
 		_colorLowerLabel.setText(Integer.toString(_colorLowerSlider.getValue()));
 		_colorUpperLabel.setText(Integer.toString(_colorUpperSlider.getValue()));
 		_saturationLowerLabel.setText(Integer.toString(_saturationLowerSlider.getValue()));
 		_saturationUpperLabel.setText(Integer.toString(_saturationUpperSlider.getValue()));
 		_valueLowerLabel.setText(Integer.toString(_valueLowerSlider.getValue()));
 		_valueUpperLabel.setText(Integer.toString(_valueUpperSlider.getValue()));
 	}
 }
