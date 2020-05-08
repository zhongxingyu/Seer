 /*
  * Copyright (C) 2010 Alex Cojocaru
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.alexalecu.imageCrop;
 
 import com.alexalecu.dataBinding.JBus;
 
 public class ImageCropWizard {
 
 	private boolean wizardMode;
 	
 	private ImageCropGUI gui;
 	
 	/**
 	 * @param gui the app GUI
 	 */
	public ImageCropWizard(ImageCropGUI gui) {
 		this.gui = gui;
 	}
 	
 	/**
 	 * @return true if we're in wizard mode
 	 */
 	public boolean isWizardMode() {
 		return wizardMode;
 	}
 
 	/**
 	 * set the wizard mode
 	 * @param wizardMode
 	 */
 	public void setWizardMode(boolean wizardMode) {
 		this.wizardMode = wizardMode;
 	}
 	
 	/**
 	 * start, resume or stop the wizard
 	 */
 	public void toggleWizard() {
 		if (!isWizardMode()) { // start the wizard mode if it is currently inactive
 			setWizardMode(true);
 			triggerWizard(true);
 		}
 		else { // resume it otherwise
 			triggerWizard(false);
 		}
 	}
 	
 	/**
 	 * trigger the next step of the wizard
 	 * @param restart force the wizard to be started from the beginning
 	 */
 	public void triggerWizard(boolean restart) {
		ImageParams imageParams = engine.getCurrentImageParams();
 		
 		if (!isWizardMode()) // do nothing if the wizard is off
 			return;
 		
 		// reset the application state to Init
 		if (restart) {
 			if (imageParams.getImageFile() != null) {
 				if (!gui.showConfirmDialog("Are you sure you want to discard current picture ?"))
 					return;
 				
 				while (imageParams.getImageFile() != null) // discard all images in stack
					engine.discard();
 			}
 			
 			// switch to the init state
 			if (imageParams.getState() != ImageCropState.StateInit) {
 				imageParams.setState(ImageCropState.StateInit);
 				gui.setState(imageParams.getState());
 			}
 		}
 		
 		switch (imageParams.getState()) {
 			case StateInit:
 				executeWizardAction(imageParams.getState());
 				break;
 			case StateImageLoaded:
 				// switch to the next state
 				imageParams.setState(ImageCropState.StateBackgroundColor);
 				gui.setState(imageParams.getState());
 
 				executeWizardAction(imageParams.getState());
 				
 				// and pause the wizard, the user is responsible for triggering the next step
 				gui.setWizardButtonText("Resume wizard");
 				break;
 			case StateSelectingBackgroundColor:
 				// do nothing in this state
 				break;
 			case StateBackgroundColor:
 				// switch to the next state
 				imageParams.setState(ImageCropState.StateSelection);
 				gui.setState(imageParams.getState());
 
 				executeWizardAction(imageParams.getState());
 				
 				// and pause the wizard, the user is responsible for triggering the next step
 				gui.setWizardButtonText("Resume wizard");
 				break;
 			case StateSelection:
 				// do nothing
 				break;
 			case StateSelectionAutoSelected:
 				executeWizardAction(imageParams.getState());
 				
 				// switch to the next state
 				imageParams.setState(ImageCropState.StateSelectionDone);
 				gui.setState(imageParams.getState());
 
 				// and pause the wizard, the user is responsible for triggering the next step
 				gui.setWizardButtonText("Resume wizard");
 				break;
 			case StateSelectionDone:
 				executeWizardAction(imageParams.getState());
 				
 				// switch to the next state
 				imageParams.setState(ImageCropState.StateCrop);
 				gui.setState(imageParams.getState());
 
 				setWizardMode(false); // stop the wizard, as we reached the end of it
 				gui.setWizardButtonText("Start wizard");
 				break;
 		}
 	}
 
 	/**
 	 * execute the wizard action for the corresponding image state passed as parameter,
 	 * only if the wizard mode is true
 	 * @param state the current image state
 	 * @return true if the next step has been triggered, false if not
 	 */
 	private boolean executeWizardAction(ImageCropState state) {
 		if (!wizardMode) // do nothing if we're not in wizard mode
 			return false;
 
 		switch (state) {
 			case StateInit:
 				JBus.getInstance().post(NotificationType.LOAD_IMAGE_ACTION);
 				break;
 			case StateBackgroundColor:
 				gui.showInfoDialog(
 						"<html>Select the background color and tolerance.<br>" +
 						"Use the background red, green and blue controls on the right<br>" +
 						"or click the <b>Select background color</b> button " +
 						"and pick the background color from the image.<br><br>" +
 						"When done, click the <b>Resume wizard</b> button on the right.</html>");
 				break;
 			case StateSelection:
 				gui.showInfoDialog(
 						"<html>Mark the image you want to select by drawing a " +
 						"selection rectangle inside it.<br>" +
 						"Choose the <b>Select method</b> using the control on the right<br>" +
 						"and click the <b>Auto select picture</b> button.</html>");
 				break;
 			case StateSelectionAutoSelected:
 				gui.showInfoDialog(
 						"<html>Fine tune the selection if needed.<br>" +
 						"Use the controls on the right to change its position and size or<br>" +
 						"drag / resize the selection using the mouse in the image panel.<br><br>" +
 						"When done, click the <b>Resume wizard</b> button on the right.</html>");
 				break;
 			case StateSelectionDone:
 				gui.showInfoDialog(
 						"<html>Crop the selection and start working with it.<br>" +
 						"You can save it later.</html>");
 				break;
 		}
 		return true;
 	}
 
 }
