 /**
  * Projekt ANNtool 
  *
  * Copyright (c) 2011 github.com/timaschew/jANN
  * 
  * anton
  */
 package de.unikassel.ann.controller;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 
 /**
  * @author anton
  * 
  */
 public class ActionControllerTest extends JFrame {
 
 	public static void main(final String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					instance = new ActionControllerTest();
 					instance.setVisible(true);
 					instance.setBounds(100, 100, 400, 400);
 					instance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public final static Integer MIN = 0;
 	public final static Integer MAX = 10;
 
 	public class MyModel {
 
 		public final static String LAYER_COUNT_PROPERTY = "layerCount";
 		private Integer layerCount = 0;
 		private PropertyChangeSupport pcs;
 
 		public MyModel() {
 			pcs = new PropertyChangeSupport(this);
 		}
 
 		public PropertyChangeSupport getPCS() {
 			return pcs;
 		}
 
 		/**
 		 * @return the layerCount
 		 */
 		public Integer getLayerCount() {
 			return layerCount;
 		}
 
 		/**
 		 * @param layerCount
 		 *          the layerCount to set
 		 */
 		public void setLayerCount(final Integer layerCount) {
 			if (layerCount < MIN || layerCount > MAX) {
 				return;
 			}
 			Integer oldValue = this.layerCount;
 			this.layerCount = layerCount;
 			pcs.firePropertyChange(LAYER_COUNT_PROPERTY, oldValue, layerCount);
 		}
 
 		/**
 		 * @param propertyChangeListener
 		 */
		public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(propertyName, listener);
 		}
 
 	}
 
 	public static ActionControllerTest instance;
 	public MyModel dataModel = new MyModel();
 	public SpinnerModel spinnerModel = new SpinnerNumberModel(new Integer(0), MIN, MAX, new Integer(1));
 	public ComboBoxModel comboBoxModel = new DefaultComboBoxModel(new Integer[] { -1, -2 });
 	public JButton button = new JButton("+1");
 
 	public JComboBox comboBox;
 	public JSpinner spinner;
 
	private ActionController ac = ActionController.get();
 	public JLabel label;
 
 	public ActionControllerTest() {
 
 		spinner = new JSpinner(spinnerModel);
 		getContentPane().add(spinner, BorderLayout.CENTER);
 
 		comboBox = new JComboBox(comboBoxModel);
 		getContentPane().add(comboBox, BorderLayout.EAST);
 
 		label = new JLabel("<empty>");
 		getContentPane().add(label, BorderLayout.SOUTH);
 
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				dataModel.setLayerCount(dataModel.getLayerCount() + 1);
 			}
 		});
 
 		getContentPane().add(button, BorderLayout.NORTH);
 		initChangeListeners();
 	}
 
 	private void initChangeListeners() {
		dataModel.addPropertyChangeListener(MyModel.LAYER_COUNT_PROPERTY, new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.TEST_UPDATEVIEW, evt);
 			}
 		});
 
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
		editor.getTextField().addPropertyChangeListener("value", new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.TEST_UPDATEMODEL, evt);
 			}
		});
 
 		comboBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				ac.doAction(Actions.TEST_UPDATEMODEL, new PropertyChangeEvent(comboBox, "item", "", comboBox.getSelectedItem()));
 			}
 		});
 	}
 
 }
