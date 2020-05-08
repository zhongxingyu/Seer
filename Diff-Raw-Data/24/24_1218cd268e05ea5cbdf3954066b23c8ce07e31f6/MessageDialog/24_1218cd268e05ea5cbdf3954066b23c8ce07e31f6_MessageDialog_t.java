 package com.cahoots.eclipse.indigo.misc;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 public class MessageDialog {
 	public void error(final Shell parent, final String title,
 			final String message) {
 		final int style = SWT.ICON_ERROR | SWT.OK;
 		message(parent, title, message, style);
 	}
 
 	public void warn(final Shell parent, final String title,
 			final String message) {
 		final int style = SWT.ICON_WARNING | SWT.OK;
 		message(parent, title, message, style);
 	}
 
 	public void info(final Shell parent, final String title,
 			final String message) {
 		final int style = SWT.ICON_INFORMATION | SWT.OK;
 		message(parent, title, message, style);
 	}
 
 	private void message(final Shell parent, final String title,
 			
 			final String message, final int style) {
		final Shell p = (parent == null) ? null : parent;
		
 		final MessageBox messageBox = new MessageBox(p, style);
 		messageBox.setText(title);
 		messageBox.setMessage(message);
 		messageBox.open();
 	}
 
 	/**
 	 * Returns {@link MessageDialogStatus}.OK on successful prompt. Cancel
 	 * otherwise
 	 */
 	public MessageDialogStatus prompt(final Shell parent, final String title,
 			final String question) {
 		final MessageBox messageBox = new MessageBox(parent, SWT.ICON_QUESTION
 				| SWT.OK | SWT.CANCEL);
 		messageBox.setText(title);
 		messageBox.setMessage(question);
 		final int status = messageBox.open();
 		switch (status) {
 		case SWT.OK:
 			return MessageDialogStatus.OK;
 		default:
 			return MessageDialogStatus.CANCEL;
 		}
 	}
 }
