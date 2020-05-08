 /**
  * Projekt ANNtool 
  *
  * Copyright (c) 2011 github.com/timaschew/jANN
  * 
  * anton
  */
 package de.unikassel.ann.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.SpinnerModel;
 
 import de.unikassel.ann.controller.ActionControllerTest.MyModel;
 
 /**
  * @author anton
  * 
  */
 public class ActionController {
 
 	private static ActionController instance;
 
	public static ActionController getInstance() {
 		if (instance == null) {
 			instance = new ActionController();
 		}
 		return instance;
 	}
 
 	public void doAction(final Actions a, final PropertyChangeEvent evt) {
 		switch (a) {
 		case TEST_UPDATEMODEL:
 			Integer value = (Integer) evt.getNewValue();
 			MyModel dataModel = ActionControllerTest.instance.dataModel;
 			dataModel.setLayerCount(value);
 			break;
 
 		case TEST_UPDATEVIEW:
 			System.out.println("TEST ACTION: " + evt.getNewValue());
 			// model was changed, upadte everything
 			// property in TEST case is MyModel.layerCount from type Integer
 			Integer layerCount = (Integer) evt.getNewValue();
 
 			// update combox box
 			JComboBox cb = ActionControllerTest.instance.comboBox;
 			// create a list with entries for the comboboxModel
 			Vector<Integer> comboBoxEntries = new Vector<Integer>();
 			for (int i = 0; i <= layerCount; i++) {
 				comboBoxEntries.add(i);
 			}
 			// set overwrite the old combobox model
 			cb.setModel(new DefaultComboBoxModel(comboBoxEntries));
 			cb.setSelectedItem(layerCount); // select the last one
 
 			// update Spinner
 			SpinnerModel sm = ActionControllerTest.instance.spinnerModel;
 			sm.setValue(layerCount);
 
 			// update Label
 			JLabel label = ActionControllerTest.instance.label;
 			label.setText("Aktueller Wert: " + layerCount);
 
 			break;
 		}
 	}
 }
