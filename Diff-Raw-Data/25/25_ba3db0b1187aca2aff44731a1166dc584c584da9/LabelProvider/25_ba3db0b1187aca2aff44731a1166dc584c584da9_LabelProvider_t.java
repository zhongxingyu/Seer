 package de.ptb.epics.eve.viewer.views.messagesview;
 
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.PlatformUI;
 
import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.messages.ViewerMessage;
 
 /**
  * <code>MessagesTableLabelProvider</code> is the label provider for the table 
  * viewer of the Messages View.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class LabelProvider implements ITableLabelProvider {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Image getColumnImage(final Object element, final int columnIndex) {
 		if(columnIndex == 2) {
 			ViewerMessage message = (ViewerMessage) element;
 			switch(message.getMessageType()) {
 			case DEBUG:
				return Activator.getDefault().getImageRegistry().get("DEBUG");
 			case ERROR:
				return Activator.getDefault().getImageRegistry().get("ERROR");
 			case FATAL:
				return Activator.getDefault().getImageRegistry().get("FATAL");
 			case INFO:
				return Activator.getDefault().getImageRegistry().get("INFO");
 			case MINOR:
				return Activator.getDefault().getImageRegistry().get("WARNING");
 			case SYSTEM:
				return Activator.getDefault().getImageRegistry().get("SYSTEM");
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getColumnText(final Object element, final int columnIndex) {
 		ViewerMessage message = (ViewerMessage) element;
 		
 		if(columnIndex == 0) {
 			// first column is the time (show only msecs not nsecs)
 			return message.getTimeStamp().toMONDDYYYY().length()==30 
 					? message.getTimeStamp().toMONDDYYYY().substring(0, 24) 
 					: message.getTimeStamp().toMONDDYYYY();
 		} else if(columnIndex == 1) {
 			// second column is the source
 			return message.getMessageSource().toString();
 		} else if(columnIndex == 3) {
 			// fourth column is the message
 			return message.getMessage();
 		}
 		return null;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isLabelProperty(final Object element, final String property) {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addListener(final ILabelProviderListener listener) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void removeListener(final ILabelProviderListener listener) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 	}
 }
