 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IMessageBoxRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.IMessageBoxRidget.MessageBoxOption;
 import org.eclipse.riena.ui.ridgets.IMessageBoxRidget.Type;
 
 public class MessageBoxSubModuleController extends SubModuleController {
 
 	private static final MessageBoxOption MAYBE = new IMessageBoxRidget.MessageBoxOption("Maybe"); //$NON-NLS-1$
 	private static final MessageBoxOption BLUE = new IMessageBoxRidget.MessageBoxOption("Blue"); //$NON-NLS-1$
 	private static final MessageBoxOption YELLOW = new IMessageBoxRidget.MessageBoxOption("Yellow"); //$NON-NLS-1$
 
 	private IMessageBoxRidget messageBoxRidget;
 
 	public MessageBoxSubModuleController() {
 		super(null);
 	}
 
 	@Override
 	public void configureRidgets() {
 
 		super.configureRidgets();
 
 		messageBoxRidget = (IMessageBoxRidget) getRidget("messageBox"); //$NON-NLS-1$
 
 		ITextRidget messageTitle = (ITextRidget) getRidget("messageTitle"); //$NON-NLS-1$
 		messageTitle.addPropertyChangeListener(ITextRidget.PROPERTY_TEXT, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				messageBoxRidget.setTitle((String) evt.getNewValue());
 			}
 		});
 		messageTitle.setText("Bridgekeeper"); //$NON-NLS-1$
 
 		ITextRidget messageText = (ITextRidget) getRidget("messageText"); //$NON-NLS-1$
 		messageText.addPropertyChangeListener(ITextRidget.PROPERTY_TEXT, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				messageBoxRidget.setText((String) evt.getNewValue());
 			}
 		});
 		messageText.setText("What is your favourite colour?"); //$NON-NLS-1$
 
 		MessageBoxSettingsBean bean = new MessageBoxSettingsBean();
 
 		IComboRidget messageTypes = (IComboRidget) getRidget("messageType"); //$NON-NLS-1$
 		messageTypes.bindToModel(bean, "messageTypes", LabeledMessageType.class, null, bean, "selectedMessageType"); //$NON-NLS-1$ //$NON-NLS-2$
 		messageTypes.updateFromModel();
 
 		IComboRidget messageOptions = (IComboRidget) getRidget("messageOptions"); //$NON-NLS-1$
 		messageOptions.bindToModel(bean, "messageOptions", LabeledOptions.class, null, bean, "selectedMessageOptions"); //$NON-NLS-1$ //$NON-NLS-2$
 		messageOptions.updateFromModel();
 
 		IActionRidget showMessage = (IActionRidget) getRidget("showMessage"); //$NON-NLS-1$
 		showMessage.setText("Show Message Box"); //$NON-NLS-1$
 		showMessage.addListener(new ShowMessageActionListener());
 	}
 
 	private final class ShowMessageActionListener implements IActionListener {
 		public void callback() {
 			IMessageBoxRidget.MessageBoxOption selectedOption = messageBoxRidget.show();
 
 			// Filling a textfield based on the selected option. Just an
 			// example...
 			ITextRidget selectedOptionTextField = (ITextRidget) getRidget("selectedOption"); //$NON-NLS-1$
 			if (selectedOption.equals(IMessageBoxRidget.OK)) {
 				selectedOptionTextField.setText("You clicked 'OK'."); //$NON-NLS-1$
 			} else if (selectedOption.equals(IMessageBoxRidget.CANCEL)) {
 				selectedOptionTextField.setText("You clicked 'Cancel'."); //$NON-NLS-1$
 			} else if (selectedOption.equals(IMessageBoxRidget.YES)) {
 				selectedOptionTextField.setText("You said 'Yes'."); //$NON-NLS-1$
 			} else if (selectedOption.equals(IMessageBoxRidget.NO)) {
 				selectedOptionTextField.setText("You said 'No'."); //$NON-NLS-1$
 			} else if (selectedOption.equals(IMessageBoxRidget.CLOSED)) {
 				selectedOptionTextField.setText("You closed the window without clicking a button."); //$NON-NLS-1$
 			} else if (selectedOption.equals(MAYBE)) {
 				selectedOptionTextField.setText("You are undecided."); //$NON-NLS-1$
 			} else if (selectedOption.equals(BLUE)) {
 				selectedOptionTextField.setText("Your favourite colour is blue."); //$NON-NLS-1$
 			} else if (selectedOption.equals(YELLOW)) {
 				selectedOptionTextField.setText("Your favourite colour is yellow."); //$NON-NLS-1$
 			}
 		}
 	}
 
 	public final class MessageBoxSettingsBean {
 
 		private List<LabeledMessageType> messageTypes;
 		private LabeledMessageType selectedMessageType;
 		private List<LabeledOptions> messageOptions;
 		private LabeledOptions selectedMessageOptions;
 
 		private MessageBoxSettingsBean() {
 			messageTypes = new ArrayList<LabeledMessageType>();
 			messageTypes.add(new LabeledMessageType("Plain (no Icon)", IMessageBoxRidget.Type.PLAIN)); //$NON-NLS-1$
 			messageTypes.add(new LabeledMessageType("Information", IMessageBoxRidget.Type.INFORMATION)); //$NON-NLS-1$
 			messageTypes.add(new LabeledMessageType("Warning", IMessageBoxRidget.Type.WARNING)); //$NON-NLS-1$
 			messageTypes.add(new LabeledMessageType("Error", IMessageBoxRidget.Type.ERROR)); //$NON-NLS-1$
 			messageTypes.add(new LabeledMessageType("Help", IMessageBoxRidget.Type.HELP)); //$NON-NLS-1$
 			LabeledMessageType question = new LabeledMessageType("Question", IMessageBoxRidget.Type.QUESTION); //$NON-NLS-1$
 			messageTypes.add(question);
 			setSelectedMessageType(question);
 
 			messageOptions = new ArrayList<LabeledOptions>();
 			messageOptions.add(new LabeledOptions("just OK", IMessageBoxRidget.OPTIONS_OK)); //$NON-NLS-1$
 			messageOptions.add(new LabeledOptions("OK and Cancel", IMessageBoxRidget.OPTIONS_OK_CANCEL)); //$NON-NLS-1$
 			messageOptions.add(new LabeledOptions("Yes or No", IMessageBoxRidget.OPTIONS_YES_NO)); //$NON-NLS-1$
 			messageOptions.add(new LabeledOptions("Yes, No or Cancel", IMessageBoxRidget.OPTIONS_YES_NO_CANCEL)); //$NON-NLS-1$
 			messageOptions.add(new LabeledOptions("Yes, No or Maybe (partially custom)", //$NON-NLS-1$
 					new IMessageBoxRidget.MessageBoxOption[] { IMessageBoxRidget.YES, IMessageBoxRidget.NO, MAYBE }));
 			LabeledOptions blueOrYellow = new LabeledOptions("Blue or Yellow (custom)", //$NON-NLS-1$
 					new IMessageBoxRidget.MessageBoxOption[] { BLUE, YELLOW });
 			messageOptions.add(blueOrYellow);
 			setSelectedMessageOptions(blueOrYellow);
 		}
 
 		public List<LabeledMessageType> getMessageTypes() {
 			return messageTypes;
 		}
 
 		public LabeledMessageType getSelectedMessageType() {
 			return selectedMessageType;
 		}
 
 		public void setSelectedMessageType(LabeledMessageType selectedMessageType) {
 			this.selectedMessageType = selectedMessageType;
 			messageBoxRidget.setType(this.selectedMessageType.getType());
 		}
 
 		public List<LabeledOptions> getMessageOptions() {
 			return messageOptions;
 		}
 
 		public LabeledOptions getSelectedMessageOptions() {
 			return selectedMessageOptions;
 		}
 
 		public void setSelectedMessageOptions(LabeledOptions selectedMessageOptions) {
 			this.selectedMessageOptions = selectedMessageOptions;
 			messageBoxRidget.setOptions(this.selectedMessageOptions.getOptions());
 		}
 	}
 
	private static class LabeledMessageType {
 
 		private IMessageBoxRidget.Type type;
 		private String label;
 
 		public LabeledMessageType(String label, Type type) {
 			this.label = label;
 			this.type = type;
 		}
 
 		public IMessageBoxRidget.Type getType() {
 			return type;
 		}
 
 		public String toString() {
 			return label;
 		}
 	}
 
	private static class LabeledOptions {
 
 		private IMessageBoxRidget.MessageBoxOption[] options;
 		private String label;
 
 		public LabeledOptions(String label, IMessageBoxRidget.MessageBoxOption[] options) {
 			this.label = label;
 			this.options = options;
 		}
 
 		public IMessageBoxRidget.MessageBoxOption[] getOptions() {
 			return options;
 		}
 
 		public String toString() {
 			return label;
 		}
 	}
 }
