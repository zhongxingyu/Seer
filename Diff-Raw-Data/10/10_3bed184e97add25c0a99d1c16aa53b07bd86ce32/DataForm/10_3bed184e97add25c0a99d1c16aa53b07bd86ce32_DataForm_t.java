 // Copyright (C) 2011 Associated Universities, Inc. Washington DC, USA.
 // 
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 // 
 // This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // General Public License for more details.
 // 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 // 
 // Correspondence concerning GBT software should be addressed as follows:
 //       GBT Operations
 //       National Radio Astronomy Observatory
 //       P. O. Box 2
 //       Green Bank, WV 24944-0002 USA
 
 package edu.nrao.dss.client.forms;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.lang.Math;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.Radio;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.google.gwt.core.client.GWT;
 
 import edu.nrao.dss.client.Functions;
 import edu.nrao.dss.client.forms.fields.GeneralCheckbox;
 import edu.nrao.dss.client.forms.fields.GeneralRadioGroup;
 import edu.nrao.dss.client.forms.fields.GeneralText;
 
 
 public class DataForm extends BasicForm {
 	private GeneralText rSigRef, nAverageRef, resolution, bw;
 	//private GeneralCombo nOverlap;
 	private GeneralCheckbox averagePol, differenceSignal;
 	private GeneralRadioGroup smoothing, smoothing_factor;
 	private Radio vel_res_rest_frame, freq_res_topo, freq_res_rest_frame, vel_res_topo_frame, freq_res_topo_frame;
 	private Label smoothing_factor_inst, rSigRefInst, nAverageRefInst;
 	private FieldSet smoothingFieldSet, sigRefSet;
 	private Float restFreq, topoFreq, srcVelocity, redshift, bandwidth;
 	private String doppler, frame, mode;
 
 	public DataForm() {
 		super("Data Reduction");
 	}
 
 	// Initial Layout
 	public void initLayout() {
 		setCollapsible(true);
 		rSigRef = new GeneralText("r_sig_ref","Ratio of time On vs Reference");
 		rSigRef.setLabelStyle("display:none");
 		rSigRef.setValue("1");
 		rSigRefInst = new Label("Ratio of observing time spent on-source/on-frequency to that spent on a reference position/reference frequency.");
 		
 		//nOverlap = new GeneralCombo("nOverlap","Enter number of spectral windows centered",  new ArrayList<String>(Arrays.asList("1","2")));
 		averagePol = new GeneralCheckbox();
 		averagePol.setId("avg_pol");
 		averagePol.setName("avg_pol");
 		averagePol.setValueAttribute("true");
 		averagePol.setBoxLabel("Average Orthognal Polarizations");
 		averagePol.setLabelSeparator("");
 		averagePol.setValue(true);
 		differenceSignal =  new GeneralCheckbox();
 		differenceSignal.setId("diff_signal");
 		differenceSignal.setName("diff_signal");
 		differenceSignal.setValueAttribute("true");
 		differenceSignal.setBoxLabel("Difference Signal and Reference Observations");
 		differenceSignal.setLabelSeparator("");
 		differenceSignal.setValue(true);
 		nAverageRef = new GeneralText("no_avg_ref","Number of Reference Observations");
 		nAverageRef.setValue("1");
 		nAverageRef.setLabelStyle("display:none");
 		nAverageRefInst = new Label("In data reduction you have the option to average multiple reference observations in order to improve the noise. Enter number of reference observations that will be averaged together.");
 		
 		smoothing = new GeneralRadioGroup("smoothing");
 		smoothing.setFieldLabel("Smooth On-source Data to a Desired");
 		smoothing.setName("smoothing");
 		smoothing.setId("smoothing");
 		smoothing.setOrientation(Orientation.VERTICAL);
 		smoothing.addListener(Events.Change, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent be) {
 				updateBW();
 			}
 			
 		});
 		
 		vel_res_rest_frame = new Radio();
 		vel_res_rest_frame.setBoxLabel("Velocity Resolution in the Rest Frame");
 		vel_res_rest_frame.setValueAttribute("velocity_resolution_rest");
 		vel_res_rest_frame.setName("velocity_resolution_rest");
 		vel_res_rest_frame.setValue(true);
 		smoothing.add(vel_res_rest_frame);
 		
 		freq_res_topo = new Radio();
 		freq_res_topo.setBoxLabel("Frequency Resolution in the Topocentric");
 		freq_res_topo.setValueAttribute("frequency_resolution_topo");
 		freq_res_topo.setName("frequency_resolution_topo");
 		smoothing.add(freq_res_topo);
 		
 		freq_res_rest_frame = new Radio();
 		freq_res_rest_frame.setBoxLabel("Frequency Resolution in the Rest Frame");
 		freq_res_rest_frame.setValueAttribute("frequency_resolution_rest");
 		freq_res_rest_frame.setName("frequency_resolution_rest");
 		smoothing.add(freq_res_rest_frame);
 		
 		vel_res_topo_frame = new Radio();
 		vel_res_topo_frame.setBoxLabel("Velocity Resolution in the Topocentric Frame");
 		vel_res_topo_frame.setValueAttribute("velocity_resolution_topo");
 		vel_res_topo_frame.setName("velocity_resolution_topo");
 		vel_res_topo_frame.hide();
 		smoothing.add(vel_res_topo_frame);
 		
 		freq_res_topo_frame = new Radio();
 		freq_res_topo_frame.setBoxLabel("Frequency Resolution in the Topocentric Frame");
 		freq_res_topo_frame.setValueAttribute("frequency_resolution_topo_frame");
 		freq_res_topo_frame.setName("frequency_resolution_topo_frame");
 		freq_res_topo_frame.hide();
 		smoothing.add(freq_res_topo_frame);
 		
 		resolution = new GeneralText("smoothing_resolution", "Desired Resolution (km/s)");
 		resolution.setMaxLength(6);
 		resolution.setValidator(new Validator () {
 
 			@Override
 			public String validate(Field<?> field, String value) {
 				if (value.isEmpty() || value.equals("-")) {
 					return null;
 				}
 				if (Float.valueOf(value) == 0){
 					return "Resolution cannot be zero.";
 				}
 				return null;
 			}
 		});
 		resolution.addListener(Events.Valid, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent be) {
 				updateBW();
 			}
 			
 		});
 		
 		smoothing.addListener(Events.Change, new Listener<FieldEvent> () {
 
 			@Override
 			public void handleEvent(FieldEvent be) {
 				if (smoothing.getValue().getValueAttribute().equals("velocity_resolution_rest")) {
 					resolution.setFieldLabel("Desired Resolution (km/s)");
 				} else {
 					resolution.setFieldLabel("Desired Resolution (MHz)");
 				}
 			}
 			
 		});
 		
 		smoothing_factor_inst = new Label("To improve signal-to-noise you can smooth reference observations to a resolution that is a few times courser than the signal observation.  Select the factor by which you want to smooth the reference observation:");
 				
 		smoothing_factor = new GeneralRadioGroup("smoothing_factor");
 		smoothing_factor.setName("smoothing_factor");
 		smoothing_factor.setId("smoothing_factor");
 		smoothing_factor.setFieldLabel("Smoothing Factor");
 		
 		Radio choice = new Radio();
 		choice.setBoxLabel("1");
 		choice.setValue(true);
 		choice.setValueAttribute("1");
 		choice.setName("1");
 		smoothing_factor.add(choice);
 		
 		choice = new Radio();
 		choice.setBoxLabel("2");
 		choice.setValueAttribute("2");
 		choice.setName("2");
 		smoothing_factor.add(choice);
 		
 		choice = new Radio();
 		choice.setBoxLabel("4");
 		choice.setValueAttribute("4");
 		choice.setName("4");
 		smoothing_factor.add(choice);
 		
 		choice = new Radio();
 		choice.setBoxLabel("8");
 		choice.setValueAttribute("8");
 		choice.setName("8");
 		smoothing_factor.add(choice);
 		
 		smoothingFieldSet = new FieldSet();
 		smoothingFieldSet.setHeading("Smoothing");
 		FormLayout layout = new FormLayout();  
 		layout.setLabelWidth(200);  
 		smoothingFieldSet.setLayout(layout);
 		
 		sigRefSet = new FieldSet();
 		FormLayout layout2 = new FormLayout();  
 		layout2.setLabelWidth(200);  
 		sigRefSet.setLayout(layout2);
 		
 		bw = new GeneralText("bw", "Resolution (MHz)");
 		//bw.setFieldLabel("Resolution (MHz)");
 		bw.setId("bw");
 		bw.setName("bw");
 		bw.setLabelSeparator(":");
 		bw.hide();
 		
 		//initial state
 		rSigRef.hide();
 		rSigRefInst.hide();
 		smoothing.hide();
 		resolution.hide();
 		resolution.setAllowBlank(true);
 		smoothing_factor_inst.hide();
 		smoothing_factor.hide();
 		smoothingFieldSet.hide();
 
 		FormData fd = new FormData(60, 20);
 		
 		//attaching fields
 		sigRefSet.add(rSigRefInst);
 		sigRefSet.add(rSigRef, fd);
 		sigRefSet.add(nAverageRefInst);
 		sigRefSet.add(nAverageRef, fd);
 		add(sigRefSet);
 		//add(nOverlap);
 		add(averagePol);
 		add(differenceSignal);
 		
 		smoothingFieldSet.add(smoothing);
 		smoothingFieldSet.add(resolution);
 		smoothingFieldSet.add(bw);
 		smoothingFieldSet.add(smoothing_factor_inst);
 		smoothingFieldSet.add(smoothing_factor);
 		add(smoothingFieldSet);
 		
 		//attaching fields
 
 	}
 
 
 //	class HandleSmoothing implements Listener<FieldEvent> {
 //		public void handleEvent(FieldEvent fe) {
 //			if (smoothing.getValue()) {
 //				smoothingResolution.show();
 //				bw.show();
 //				bwRef.show();
 //			} else {
 //				smoothingResolution.hide();
 //				bw.hide();
 //				bwRef.hide();
 //			}
 //		}
 //	}
 
 
 	private void updateBW() {
 		if (resolution.getValue() == null || resolution.getValue().equals("")) {
 			return;
 		}
 		if (!mode.equals("Spectral Line") & bandwidth != null){
 			bw.setValue("" + bandwidth);
 			return;
 		}
 		double c = 2.99792458e5;
 		String smoothing_option = smoothing.getValue().getValueAttribute();
 		if (smoothing_option.equals("frequency_resolution_topo")) {
 			bw.setValue(resolution.getValue());
 		} else if (smoothing_option.equals("frequency_resolution_rest")) {
 			Float deltaFreq = Float.valueOf(resolution.getValue());
 			double f01 = Functions.velocity2Frequency(doppler, restFreq + deltaFreq / 2, srcVelocity, redshift);
 			double f02 = Functions.velocity2Frequency(doppler, restFreq - deltaFreq / 2, srcVelocity, redshift);
 			bw.setValue("" + Math.abs(f01 - f02));
 		} else if (smoothing_option.equals("velocity_resolution_rest")) {
 			Float deltaVelocity = Float.valueOf(resolution.getValue());
 			float velocity;
 			if (doppler.equals("Redshift")) {
 				velocity = (float) (redshift * c);
 			} else {
 				velocity = srcVelocity;
 			}
 			
 			float v1   = velocity + deltaVelocity / 2;
 			float v2   = velocity - deltaVelocity / 2;
 			double f01 = Functions.velocity2Frequency(doppler, restFreq, v1, v1 / c);
 			double f02 = Functions.velocity2Frequency(doppler, restFreq, v2, v2 / c);
 			bw.setValue("" + Math.abs(f01 - f02));
 		} else if (smoothing_option.equals("velocity_resolution_topo")) {
 			Float deltaVelocity = Float.valueOf(resolution.getValue());
 			double res = topoFreq * deltaVelocity / c;
 			bw.setValue("" + res);
 		} else if (smoothing_option.equals("frequency_resolution_topo_frame")) {
 			bw.setValue(resolution.getValue());
 		}
 	}
 	
 	public void notify(String name, String value) {
 		// handler for mode
 		if (name.equals("switching")) {
 			if (value.equals("Total Power")) {
 				rSigRef.hide();
 				rSigRefInst.hide();
 			} else {
 				rSigRef.show();
 				rSigRefInst.show();
 			}
 		
 		} else if (name.equals("mode")) {
 			mode = value;
 			if (value.equals("Spectral Line")) {
 				smoothing.show();
 				resolution.show();
 				resolution.setAllowBlank(false);
 				smoothing_factor_inst.show();
 				smoothing_factor.show();
 				smoothingFieldSet.show();
 			} else {
 				smoothing.hide();
 				resolution.hide();
 				resolution.setAllowBlank(true);
 				resolution.setValue("1");
 				smoothing_factor_inst.hide();
 				smoothing_factor.hide();
 				smoothingFieldSet.hide();
 			}
 			
 			if (value.equals("Total Power")) {
 				differenceSignal.hide();
 			} else {
 				differenceSignal.show();
 			}
 
 		} else if (name.equals("polarization")) {
			if (!value.equals("NOTHING")) {
				if (value.equals("Dual")) {
					averagePol.setValue(true);
				} else {
					averagePol.setValue(false);				
				}				
 			}
 		} else if (name.equals("bandwidth") & !value.equals("NOTHING")) {
 			bandwidth = Float.valueOf(value);
 		}
 		
 		if (name.equals("rest_freq")) {
 			restFreq = Float.valueOf(value);
 		} else if (name.equals("topocentric_freq")) {
 			topoFreq = Float.valueOf(value);
 		} else if (name.equals("redshift")) {
 			redshift = Float.valueOf(value);
 		} else if (name.equals("source_velocity")) {
 			srcVelocity = Float.valueOf(value);
 		} else if (name.equals("frame")) {
 			if(!value.equals(frame)) {
 				if (value.equals("Topocentric Frame")) {
 					vel_res_topo_frame.show();
 					vel_res_topo_frame.setValue(true);
 					freq_res_topo_frame.show();
 					vel_res_rest_frame.hide();
 					freq_res_topo.hide();
 					freq_res_rest_frame.hide();
 				} else {
 					vel_res_topo_frame.hide();
 					freq_res_topo_frame.hide();
 					vel_res_rest_frame.show();
 					vel_res_rest_frame.setValue(true);
 					freq_res_topo.show();
 					freq_res_rest_frame.show();
 				}
 				frame = value;
 			}			
 		}else if (name.equals("doppler")) {
 			doppler = value;
 		}
 		updateBW();
 		
 //		if (name.equals("backend")) {
 //			if (value.equals("Spectrometer")) {
 //				nOverlap.show();
 //			} else {
 //				nOverlap.hide();
 //			}
 //
 //		}
 
 	}
 	
 	public void validate() {
 
 	}
 	
 	
 
 }
