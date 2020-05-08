 package org.dawnsci.rcp.histogram;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Scale;
 
 import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;
 
 /**
  * Simple Class which combines a spinner and a scale so that they have the same bounds etc.
  * With many of the same so that they look ok in a composite together
  * @author ssg37927
  *
  */
 public class SpinnerScaleSet {
 
 	// Class Items
 	private Map<String, Double> maxs = new HashMap<String,Double>();
 	private Map<String, Double> mins = new HashMap<String,Double>();
 	private int steps;
 
 	// GUI Components
 	private Composite comp;
 
 	// GUI Components for the items
 	private Map<String,Label> labels = new HashMap<String,Label>();
 	private Map<String,Scale> scales = new HashMap<String,Scale>();
 	private Map<String,FloatSpinner> spinners = new HashMap<String,FloatSpinner>();
 	private Map<String,SelectionListener> listeners = new HashMap<String,SelectionListener>();
 
 	private List<SelectionListener> externalListeners = new ArrayList<SelectionListener>();
 
 	public SpinnerScaleSet(Composite parent, int sliderSteps, String... names) {
 
 		// Set up the composite
 		comp = new Composite(parent, SWT.NONE);
 		comp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		comp.setLayout(new GridLayout(3, false));
 
 		steps = sliderSteps;
 
 
 		// now itterate through all the names and create appropriate gui elements
 
 		for (String name : names) {
 
 			GridData gridDataLabel = new GridData(SWT.NONE, SWT.NONE, false, false);
 			gridDataLabel.minimumWidth = 100;
 			
 			Label label = new Label(comp, SWT.NONE);
 			label.setText(name);
 			
 			labels.put(name, label);
 
 			SelectionListener listener = new SelectionListener() {			
 				@Override
 				public void widgetSelected(SelectionEvent event) {
 					if (event.getSource() instanceof Scale) {
 						for (String name : scales.keySet()) {
 							if(scales.get(name) == event.getSource()) {
 								updateSpinner(name);
 								continue;
 							}
 						}
 					}
 					if (event.getSource() instanceof FloatSpinner) {
 						for (String name : spinners.keySet()) {
 							if(spinners.get(name) == event.getSource()) {
 								updateScale(name);
 								continue;
 							}
 						}
 					}
 					updateListeners(event);
 				}
 				@Override
 				public void widgetDefaultSelected(SelectionEvent event) {
 					widgetSelected(event);
 				}
 			};
 			
 			listeners.put(name, listener);
 
 			GridData gridDataSpinner = new GridData(SWT.NONE, SWT.NONE, false, false);
 			gridDataSpinner.minimumWidth = 90;
 
 			FloatSpinner spinner = new FloatSpinner(comp, SWT.BORDER);
 			spinner.setLayoutData(gridDataSpinner);
 			spinner.addSelectionListener(listener);
 			spinners.put(name, spinner);
 
 			GridData gridDataScale = new GridData(SWT.FILL, SWT.CENTER, true, false);
 			Scale scale = new Scale(comp, SWT.NONE);
 			scale.setLayoutData(gridDataScale);
 			scale.addSelectionListener(listener);
 			scale.setMinimum(0);
 			scale.setMaximum(steps);
 			scale.setIncrement(1);
 			//scale.setPageIncrement(1);
 			scales.put(name, scale);
 			
 			maxs.put(name, 100.0);
 			mins.put(name, -100.0);
 		}
 		
 	}
 
 	public double getMax(String name) {
 		return maxs.get(name);
 	}
 
 	public void setMax(String name, double max) {
 		this.maxs.put(name, max);
 		spinners.get(name).setMaximum(max);
 	}
 
 	public double getMin(String name) {
 		return mins.get(name);
 	}
 
 	public void setMin(String name, double min) {
 		this.mins.put(name, min);
 		spinners.get(name).setMinimum(min);
 	}
 
 	public double getValue(String name) {
 		return spinners.get(name).getDouble();
 	}
 
 	public void setValue(String name, double value) {
 		spinners.get(name).setDouble(value);
 		updateScale(name);
 	}
 	
 
 	public void addSelectionListener(SelectionListener listener) {
 		externalListeners.add(listener);
 	}
 
 	public void removeSelectionListener(SelectionListener listener) {
 		externalListeners.remove(listener);
 	}
 
 	public void updateListeners(SelectionEvent event) {
 		for (SelectionListener listener : externalListeners) {
 			listener.widgetSelected(event);	
 		}
 	}
 
 	private void updateSpinner(String name) {
 		int selection = scales.get(name).getSelection();
 		double a = ((double)selection/(double)steps);
 		double val = (a*(maxs.get(name)-mins.get(name)))+mins.get(name);
 		spinners.get(name).setDouble(val);
 	}
 
 	private void updateScale(String name) {
 		double minval = spinners.get(name).getDouble();
 		scales.get(name).setSelection((int) (((minval-mins.get(name))/(maxs.get(name)-mins.get(name)))*steps));
 	}
 
 
 
 }
