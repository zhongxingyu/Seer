 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executor.
  * 
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.gui.frames;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import net.lmxm.ute.resources.ResourcesUtils;
 import net.lmxm.ute.resources.types.ApplicationResourceType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class AbstractFrame.
  */
 public class AbstractFrame extends JFrame {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 2742976937617347846L;
 
     /** The Constant LOGGER. */
     private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFrame.class);
 
 	/**
 	 * Generic error message dialog.
 	 * 
 	 * @param throwable the throwable/exception that occurred
 	 */
 	protected void displayError(final Throwable throwable) {
         LOGGER.error("Error occurred.", throwable);

 		// TODO handle AbstractRuntimeException and other errors differently.
 		final String message = throwable.getMessage();
 		final String title = ResourcesUtils.getResourceTitle(ApplicationResourceType.ERROR_OCCURRED);
 
 		JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
 	}
 
 	// /**
 	// * Generic warning message dialog.
 	// *
 	// * @param message Warning message to display.
 	// */
 	// protected void displayWarning(final String message) {
 	// JOptionPane.showMessageDialog(this, message, ResourceUtil.getString("application.warning"),
 	// JOptionPane.WARNING_MESSAGE);
 	// }
 	//
 	// /**
 	// * Show confirm dialog.
 	// *
 	// * @param messageId the message id
 	// * @param titleId the title id
 	// * @return the int
 	// */
 	// protected int showConfirmDialog(final String messageId, final String titleId) {
 	// return JOptionPane.showConfirmDialog(this, ResourceUtil.getString(messageId), ResourceUtil.getString(titleId),
 	// JOptionPane.YES_NO_OPTION);
 	// }
 	//
 	// /**
 	// * Show warn confirm dialog.
 	// *
 	// * @param message the message
 	// * @return the int
 	// */
 	// protected int showWarnConfirmDialog(final String message) {
 	// return JOptionPane.showConfirmDialog(this, message, ResourceUtil.getString("application.warning"),
 	// JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 	// }
 }
