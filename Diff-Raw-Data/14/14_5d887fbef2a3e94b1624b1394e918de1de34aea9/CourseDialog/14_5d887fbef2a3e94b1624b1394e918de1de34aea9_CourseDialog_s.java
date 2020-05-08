 /*******************************************************************************
  * Copyright (c) 2012 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package com.hawkinbj.gpacalc.view;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import com.hawkinbj.gpacalc.controller.SystemController;
 import com.hawkinbj.gpacalc.model.Course;
 import com.hawkinbj.gpacalc.model.GUIPanel;
 import com.hawkinbj.gpacalc.model.Grade;
 
 public class CourseDialog extends GUIPanel {
 	private static final long serialVersionUID = -6080991655918955653L;
 
 	protected JTextField courseNameField;
 
 	protected JTextField newGradeTypeField;
 
 	private JPanel namePanel;
 
 	private JPanel gradeTypesPanel;
 
 	private JPanel newGradeTypePanel;
 
 	private JPanel navigationPanel;
 
 	private JPanel weightedPanel;
 
 	private JComboBox<String> creditHrsComboBox;
 
 	private JComboBox<String> letterGradeComboBox;
 
 	protected HashMap<String, JCheckBox> gradeCheckBoxes;
 
 	protected JRadioButton weightedRdio;
 
 	protected JRadioButton notWeightedRdio;
 
 	protected static final String[] CREDITHOURS = { "0", "1", "2", "3", "4" };
 
	private String[] letterGrades;
 
 	private String finalGrade;
 
 	private Course course;
 
 	private GridLayout layout;
 
 	private ButtonGroup weightedGroup;
 
 	public CourseDialog(SystemController controller) {
 		super(controller);
 
 		this.gradeCheckBoxes = new HashMap<String, JCheckBox>();
 		this.course = controller.getActiveCourse();
 
 		if (course == null) {
 			System.out.println("Creating new course");
 			course = new Course(null, -1);
 			controller.setActiveCourse(course);
 		}
 
 		this.addComponentsToPane();
 	}
 
 	private void addComponentsToPane() {
 		courseNameField = new JTextField(10);
 		courseNameField.setText(course.getCourseName());
 
 		namePanel = new JPanel(new GridLayout(3, 2));
 		namePanel.add(new JLabel("Course name:"));
 		namePanel.add(courseNameField);
		namePanel.add(new JLabel("Course name:"));
 		namePanel.add(new JLabel("Credit hours:"));
 
 		creditHrsComboBox = new JComboBox<String>(CREDITHOURS);
 
 		if (course.getCreditHours() == -1) {
 			creditHrsComboBox.setSelectedItem("3");
 		} else {
 			creditHrsComboBox.setSelectedItem(Integer.toString(course
 					.getCreditHours()));
 		}
 
 		namePanel.add(creditHrsComboBox);
 		namePanel.add(new JLabel("Set final grade: "));
 
		letterGrades = (String[]) controller.getActiveSchool()
				.getGradingScale().getGradingScaleMap().keySet().toArray();
 
 		Arrays.sort(letterGrades);
 
		letterGradeComboBox = new JComboBox<String>(letterGrades);
 
 		finalGrade = course.getFinalGrade();
 
 		if (finalGrade.equals("N/A"))
 			this.letterGradeComboBox.setSelectedIndex(-1);
 		else
 			this.letterGradeComboBox.setSelectedItem(finalGrade);
 
 		namePanel.add(this.letterGradeComboBox);
 
 		gradeTypesPanel = new JPanel();
 		layout = new GridLayout(gradeCheckBoxes.size() / 2, 2);
 		gradeTypesPanel.setLayout(layout);
 
 		this.createTitledBorder(gradeTypesPanel, "Select Grade Types");
 
 		for (String gradeType : controller.getGradeTypes()) {
 			addGradeCheckBox(gradeType);
 		}
 
 		for (String gradeType : course.getGrades().keySet()) {
 			if (!controller.getGradeTypes().contains(gradeType)) {
 				this.addGradeCheckBox(gradeType);
 			}
 			((JCheckBox) this.gradeCheckBoxes.get(gradeType)).setSelected(true);
 		}
 
 		newGradeTypePanel = new JPanel(new GridLayout(1, 2));
 		this.newGradeTypeField = new JTextField(10);
 		this.createTitledBorder(newGradeTypePanel, "New Grade Type");
 		newGradeTypePanel.add(this.newGradeTypeField);
 		newGradeTypePanel.add(createButton("add", "Add"));
 
 		weightedPanel = new JPanel(new GridLayout(1, 3));
 		this.createTitledBorder(weightedPanel, "Are Grades Weighted?");
 
 		weightedGroup = new ButtonGroup();
 		this.weightedRdio = new JRadioButton("Yes");
 		this.notWeightedRdio = new JRadioButton("No", true);
 
 		if (course.getWeighted()) {
 			this.weightedRdio.setSelected(true);
 		}
 
 		weightedGroup.add(this.weightedRdio);
 		weightedGroup.add(this.notWeightedRdio);
 		weightedPanel.add(this.weightedRdio);
 		weightedPanel.add(this.notWeightedRdio);
 
 		navigationPanel = new JPanel(new GridLayout(2, 1));
 		navigationPanel.add(createButton("next", "Next"));
 		navigationPanel.add(createButton("cancel", "Cancel"));
 		this.createTitledBorder(navigationPanel, "Navigation");
 
 		this.setLayout(new BoxLayout(this, 3));
 		this.add(namePanel);
 		this.add(gradeTypesPanel);
 		this.add(newGradeTypePanel);
 		this.add(weightedPanel);
 		this.add(navigationPanel);
 	}
 
 	private void addGradeCheckBox(String name) {
 		JCheckBox newCheckBox = new JCheckBox(name);
 		newCheckBox.setName(name);
 
 		gradeCheckBoxes.put(name, newCheckBox);
 		layout.setRows(gradeCheckBoxes.size() / 2 + 1);
 		gradeTypesPanel.add(newCheckBox);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String action = e.getActionCommand();
 
 		if (action.equals("cancel")) {
 			if (controller.getRootFrame().getPreviousPanel().getName()
 					.equals("courseInfoPanel")) {
 				controller.getRootFrame().showPanel("courseInfoPanel", this);
 			} else {
 				controller.setActiveCourse(null);
 				controller.getRootFrame().addPanel(new CoursePanel(controller),
 						this);
 			}
 		} else if (action.equals("add")) {
 			String newGradeType = this.newGradeTypeField.getText();
 
 			if ((newGradeType.equals(""))
 					|| (this.gradeCheckBoxes.containsKey(newGradeType))) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"That grade type already exists or name is blank. Try again.",
 								"Error", 0);
 				return;
 			}
 
 			addGradeCheckBox(newGradeType);
 
 			((JCheckBox) this.gradeCheckBoxes.get(newGradeType))
 					.setSelected(true);
 			this.newGradeTypeField.setText("");
 			revalidate();
 			repaint();
 			controller.getRootFrame().pack();
 		}
 
 		if (action.equals("next")) {
 			String courseName = courseNameField.getText();
 
 			if (courseName.equals("")) {
 				JOptionPane.showMessageDialog(this,
 						"Please enter a course name.", "Error", 0);
 				return;
 			}
 
 			if (controller.getActiveSemester().getCourses()
 					.containsKey(courseName)) {
 				int response = JOptionPane.showConfirmDialog(null,
 						"Course already exists. Overwrite?", "Confirm", 0, 3);
 
 				if (response == 0) {
 					controller.getActiveSemester().getCourses()
 							.remove(courseName);
 					controller.saveUserList();
 				}
 			} else {
 				course.setCourseName(courseName);
 				course.setFinalGrade((String) this.letterGradeComboBox
 						.getSelectedItem());
 				course.setCreditHours(Integer
 						.parseInt((String) creditHrsComboBox.getSelectedItem()));
 
 				for (JCheckBox checkBox : this.gradeCheckBoxes.values()) {
 					String gradeType = checkBox.getName();
 
 					if (checkBox.isSelected()) {
 						course.addGrade(gradeType, new Grade(gradeType, 0,
 								0.0D, 0, 0, false));
 					}
 				}
 
 				if (this.weightedRdio.isSelected()) {
 					course.setWeighted(true);
 				} else {
 					course.setWeighted(false);
 				}
 
 				controller.getActiveSemester().addCourse(courseName, course);
 				controller.saveUserList();
 
 				if (course.getFinalGrade().equals("N/A")) {
 					controller.setActiveCourse(course);
 					controller.getRootFrame().addPanel(
 							new CourseInfoPanel(controller), this);
 				} else {
 					controller.setActiveCourse(null);
 					controller.getRootFrame().addPanel(
 							new CoursePanel(controller), this);
 				}
 			}
 		}
 	}
 }
