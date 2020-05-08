 /*
  * Copyright 2006 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.ecolib.samples.controls.ChartDemo.java
  * Created on Sep 14, 2006
  * $Id: ChartDemo.java,v 1.5 2006/09/25 13:29:32 lordsam Exp $
  * @author jbornema
  */
 
 package de.jwic.ecolib.samples.controls;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 
 import de.jwic.base.ControlContainer;
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.ListBoxControl;
 import de.jwic.ecolib.controls.chart.ChartControl;
 import de.jwic.events.ElementSelectedEvent;
 import de.jwic.events.ElementSelectedListener;
 import de.jwic.events.SessionAdapter;
 import de.jwic.events.SessionEvent;
 import demo.AreaChartDemo1;
 import demo.BarChart3DDemo1;
 import demo.HistogramDemo1;
 import demo.MeterChartDemo1;
 import demo.MultipleAxisDemo1;
 import demo.ParetoChartDemo1;
 import demo.PieChart3DDemo3;
 import demo.TimeSeriesDemo8;
 import demo.XYAreaChartDemo2;
 
 /* Created on Sep 14, 2006
  * @author jbornema
  */
 
 public class ChartDemo extends ControlContainer {
 
 	private ListBoxControl demoType;
 	private ChartControl chart; 
 	
 	/**
 	 * @param container
 	 */
 	public ChartDemo(IControlContainer container) {
 		this(container, null);
 	}
 
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public ChartDemo(IControlContainer container, String name) {
 		super(container, name);
 		
 		createCharts();
 	}
 
 	/**
 	 * Create demo JFreeCharts
 	 */
 	protected void createCharts() {
 		
 		// please check http://www.jfree.org/jfreechart how to use JFreeChart ! ! !
 		
 		chart = new ChartControl(this, "chart");
 		chart.setWidth(400);
 		chart.setHeight(300);
 		
 		demoType = new ListBoxControl(this, "lbType");
 		demoType.setChangeNotification(true);
 		demoType.addElement("- None -", "NONE");
 		demoType.addElement("Pie Chart 3D", "PIE3");
 		demoType.addElement("Bar Chart 3D", "BAR3");		
 		demoType.addElement("Histogram", "Histogram");
 		demoType.addElement("Area Chart", "Area");
 		demoType.addElement("XY Area Chart", "XYArea");
 		demoType.addElement("Time Series", "TimeSeries");
 		demoType.addElement("Multiple Axis", "MultipleAxis");
 		demoType.addElement("Pareto Chart", "Pareto");
 		demoType.addElement("Meter Chart", "Meter");
 		
 		demoType.addElementSelectedListener(new ElementSelectedListener() {
 			public void elementSelected(de.jwic.events.ElementSelectedEvent event) {
 				changeChartType((String)event.getElement());
 			};
 		});
 		
 		demoType.setSelectedKey("PIE3"); // will trigger the event and call changeChartType(..);
 		
 		// Change Listbox Width
 		ListBoxControl lbWidth = new ListBoxControl(this, "lbWidth");
 		lbWidth.addElement("0 - Unspecified", "0");
 		for (int i = 50; i < 601; i += 50) {
 			lbWidth.addElement(Integer.toString(i) + "px", Integer.toString(i));
 		}
 		lbWidth.setSelectedKey(Integer.toString(chart.getWidth()));
 		lbWidth.setChangeNotification(true);
 		lbWidth.addElementSelectedListener(new ElementSelectedListener() {
 			public void elementSelected(ElementSelectedEvent event) {
 				chart.setWidth(Integer.parseInt((String)event.getElement()));
 			};
 		});
 		
 		// Change Listbox Height
 		ListBoxControl lbHeight = new ListBoxControl(this, "lbHeight");
 		lbHeight.addElement("0 - Unspecified", "0");
 		for (int i = 50; i < 601; i += 50) {
 			lbHeight.addElement(Integer.toString(i) + "px", Integer.toString(i));
 		}
 		lbHeight.setSelectedKey(Integer.toString(chart.getHeight()));
 		lbHeight.setChangeNotification(true);
 		lbHeight.addElementSelectedListener(new ElementSelectedListener() {
 			public void elementSelected(ElementSelectedEvent event) {
 				chart.setHeight(Integer.parseInt((String)event.getElement()));
 			};
 		});
 
 		/*
 		 * Release the chart data on serialization and recreate it when the session is
 		 * reloaded 
 		 */
//		getSessionContext().addSessionListener(new SessionAdapter() {
//			public void beforeSerialization(SessionEvent event) {
//				chart.setChart(null); // release chart
//			}
//			public void afterDeserialization(SessionEvent event) {
//				changeChartType(demoType.getSelectedKey());
//			}
//		});
 		
 	}
 
 	protected void changeChartType(String type) {
 		
 		JFreeChart jfreechart = null;
 		if (type.equals("PIE3")) {
 			jfreechart = ((ChartPanel)PieChart3DDemo3.createDemoPanel()).getChart();
 		} else if (type.equals("BAR3")) {
 			jfreechart = ((ChartPanel)BarChart3DDemo1.createDemoPanel()).getChart();	
 		} else if (type.equals("Histogram")) {
 			jfreechart = ((ChartPanel)HistogramDemo1.createDemoPanel()).getChart();	
 		} else if (type.equals("Area")) {
 			jfreechart = ((ChartPanel)AreaChartDemo1.createDemoPanel()).getChart();	
 		} else if (type.equals("XYArea")) {
 			jfreechart = ((ChartPanel)XYAreaChartDemo2.createDemoPanel()).getChart();	
 		} else if (type.equals("TimeSeries")) {
 			jfreechart = ((ChartPanel)TimeSeriesDemo8.createDemoPanel()).getChart();
 		} else if (type.equals("MultipleAxis")) {
 			jfreechart = ((ChartPanel)MultipleAxisDemo1.createDemoPanel()).getChart();
 		} else if (type.equals("Pareto")) {
 			jfreechart = ((ChartPanel)ParetoChartDemo1.createDemoPanel()).getChart();	
 		} else if (type.equals("Meter")) {
 			jfreechart = ((ChartPanel)MeterChartDemo1.createDemoPanel()).getChart();	
 		}
 		chart.setChart(jfreechart);
 		
 	}
 }
