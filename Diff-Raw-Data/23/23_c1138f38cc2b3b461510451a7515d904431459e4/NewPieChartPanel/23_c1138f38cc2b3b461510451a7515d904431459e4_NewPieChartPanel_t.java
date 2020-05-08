 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.Timer;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PiePlot3D;
 import org.jfree.data.general.DefaultPieDataset;
 import org.jfree.data.general.PieDataset;
 import org.jfree.util.Rotation;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.RequirementModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.overview.OverviewButtonPanel;
 
 /**
  */
 public class NewPieChartPanel extends JScrollPane {
 	private static String title;
 	private ChartPanel pieChart;
 
 	/**
 	 * @param title
 	 *            the title of the pie chart which determines if we are
 	 *            displaying based on status, iteration, or users assigned to.
 	 */
 	public NewPieChartPanel(String title) {
 		NewPieChartPanel.title = title;
 		JPanel panel = new JPanel(new BorderLayout());
 		OverviewButtonPanel buttons = new OverviewButtonPanel();
 		pieChart = createPanel();
 		panel.add(pieChart, BorderLayout.CENTER);
 		panel.add(buttons, BorderLayout.SOUTH);
 		
 		
 		this.setViewportView(panel);
 		
 
 	}
 
 	/**
 	
 	 * @return the specific data set based on the the title of the chart */
 	private static PieDataset setData() {
 		if (title.equals("Iteration")) {
 			return setDataIteration();
 		} else if (title.equals("Status")) {
 			return setDataStatus();
 		} else {
 			return setDataAssignTo();
 		}
 
 	}
 
 	/**
 	
 	 * @return the data with the percentage of requirements with a given status
 	 *         to be displayed by the pie chart */
 	private static PieDataset setDataStatus() {
 		int numStatusNew = 0;
 		int numStatusDeleted = 0;
 		int numStatusInprogress = 0;
 		int numStatusComplete = 0;
 		int numStatusOpen = 0;
 		DefaultPieDataset dataSet = new DefaultPieDataset();
 		List<Requirement> requirements = RequirementModel.getInstance()
 				.getRequirements();// list of requirements
 		for (int i = 0; i < requirements.size(); i++) {
 			if (requirements.get(i).getStatus() == RequirementStatus.NEW) {
 				numStatusNew += 1;
 			} else if (requirements.get(i).getStatus() == RequirementStatus.DELETED) {
 				numStatusDeleted += 1;
 			} else if (requirements.get(i).getStatus() == RequirementStatus.INPROGRESS) {
 				numStatusInprogress += 1;
 			} else if (requirements.get(i).getStatus() == RequirementStatus.COMPLETE) {
 				numStatusComplete += 1;
 			} else {
 				numStatusOpen += 1;
 			}
 		}
 		dataSet.setValue("New", numStatusNew);
 		dataSet.setValue("Deleted", numStatusDeleted);
 		dataSet.setValue("In Progress", numStatusInprogress);
 		dataSet.setValue("Complete", numStatusComplete);
 		dataSet.setValue("Open", numStatusOpen);
 		return dataSet;
 	}
 
 	/**
 	
 	 * @return the data of iterations to be displayed by the pie chart */
 	private static PieDataset setDataIteration() {
 		DefaultPieDataset dataSet = new DefaultPieDataset();
 		List<Iteration> iterations = IterationModel.getInstance()
 				.getIterations();// list of iterations
 		List<Requirement> requirements = RequirementModel.getInstance()
 				.getRequirements();// list of requirements
 		int[] iterationNum = new int[iterations.size()];
 		for (int i = 0; i < iterations.size(); i++) {
 			for (int j = 0; j < requirements.size(); j++) {
 				if (requirements.get(j).getIteration().toString()
 						.equals(iterations.get(i).toString())) {
 					iterationNum[i]++;// increments the number if the requiremet
 										// belongs to the given iteration
 				}
 			}
 		}
 		for (int k = 0; k < iterationNum.length; k++) {
 			dataSet.setValue(iterations.get(k).toString(), iterationNum[k]);// sets
 																			// the
 																			// data
 		}
 		return dataSet;
 	}
 
 	/**
 	
 	 * @return the data of the number of requirements a user has assigned to
 	 *         them */
 	private static PieDataset setDataAssignTo() {
 		DefaultPieDataset dataSet = new DefaultPieDataset();
 		ArrayList<String> userNames = new ArrayList<String>();
 
 		List<Requirement> requirements = RequirementModel.getInstance()
 				.getRequirements();// list of requirements
 		for (int i = 0; i < requirements.size(); i++) {
 			List<String> users = requirements.get(i).getAssignedTo();// list of
 																		// users
 																		// for
 																		// specific
 																		// requirement
 			for (int j = 0; j < users.size(); j++) {
 				if (!userNames.contains(users.get(j))) {
 					userNames.add(users.get(j));// populate a list of all users
 				}
 			}
 
 		}
 		int[] numReqAssigned = new int[userNames.size()];
 		for (int i = 0; i < requirements.size(); i++) {
 			List<String> users = requirements.get(i).getAssignedTo();
 			for (int j = 0; j < userNames.size(); j++) {
 				if (users.contains(userNames.get(j))) {
 					numReqAssigned[j]++;// count the number of requirements a
 										// user has assigned to them
 				}
 			}
 		}
 		for (int k = 0; k < userNames.size(); k++) {
 			dataSet.setValue(userNames.get(k), numReqAssigned[k]);// populate
 																	// pie chart
 																	// data
 		}
 		return dataSet;
 	}
 
 	/**
 	 * @param dataset
 	 *            the data to be displayed by the pie chart
 	 * @param title
 	 *            the title of the chart
 	
 	 * @return the pie chart to be displayed */
 	private static JFreeChart createChart(PieDataset dataset, String title) {
 
 		JFreeChart chart = ChartFactory.createPieChart3D(title, // chart title
 				dataset, // data
 				true, // include legend
 				true, false);
 
 		PiePlot3D plot = (PiePlot3D) chart.getPlot();// 3D pie chart. the cats
 														// are going to love
 														// this.
 		plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
 		plot.setNoDataMessage("No data available");
 		plot.setCircular(true);
 		plot.setLabelGap(0.02);
 		plot.setStartAngle(270);
 		plot.setDirection(Rotation.ANTICLOCKWISE);
 		//Rotator rotator = new Rotator(plot);
 		//rotator.start();
 		return chart;
 
 	}
 
 	/**
 	 * Creates the piechart panel
 	 * @return the piechart panel
 	 */
 	public static ChartPanel createPanel() {
 		JFreeChart chart = createChart(setData(), title);
 		return new ChartPanel(chart);
 	}
 	
 	/**
 	
 	 * @return the title of the chart */
 	public String getTitle(){
 		return title;
 	}
 	@Override
 	public void paintComponent(Graphics g){
 		pieChart.setChart(createChart(setData(), title));
 		super.paintComponent(g);
 	}
 
 }
 
