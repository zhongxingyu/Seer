 package de.ptb.epics.eve.viewer.views.messagesview.ui;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.viewer.XMLDispatcher;
 import de.ptb.epics.eve.viewer.views.messages.IMessagesContainerUpdateListener;
 import de.ptb.epics.eve.viewer.views.messages.MessagesContainer;
 import de.ptb.epics.eve.viewer.views.messages.ViewerMessage;
 
 /**
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class ContentProvider implements IStructuredContentProvider,
 		IMessagesContainerUpdateListener, PropertyChangeListener {
 
 	private static Logger logger = Logger.getLogger(ContentProvider.class
 			.getName());
 	
 	private TableViewer viewer;
 	private MessagesContainer messagesContainer;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object[] getElements(final Object inputElement) {
 		return ((MessagesContainer) inputElement).getList().toArray();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void inputChanged(final Viewer viewer, final Object oldInput, 
 			final Object newInput) {
 		this.viewer = (TableViewer) viewer;
 		if(oldInput != null) {
 			((MessagesContainer)oldInput).
 					removeMessagesContainerUpdateListener(this);
 		}
 		this.messagesContainer = null;
 		if(newInput != null) {
 			this.messagesContainer = (MessagesContainer)newInput;
 			this.messagesContainer.addMessagesContainerUpdateListener(this);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void update() {
 		if (!viewer.getControl().isDisposed()) {
 			// TODO this is a severe performance killer
 			// do not switch to syncExec and do not do viewer.refresh()
 			// use a tableviewer with  viewer.update() or viewer.add() instead!
 			
 			this.viewer.getControl().getDisplay().asyncExec( new Runnable() {
 				@Override public void run() {
 					if (!viewer.getControl().isDisposed()) {
 						viewer.refresh();
 					}
 					setColumnWidth();
 				}
 				
 			});
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addElement(final ViewerMessage viewerMessage) {
 		this.update();
 	}
 	
 	/*
 	 * 
 	 */
 	private void setColumnWidth() {
 		int sourceColMaxWidth = 80;
 		int messageColMaxWidth = 300;
 
 		GC gc = new GC(viewer.getTable());
 		FontMetrics fm = gc.getFontMetrics();
 		int charWidth = fm.getAverageCharWidth();
 
 		for(ViewerMessage msg : this.messagesContainer.getList()) {
 			if (sourceColMaxWidth < msg.getMessageSource().toString().length()
 					* charWidth + 8) {
 				sourceColMaxWidth = msg.getMessageSource().toString().length()
 						* charWidth + 8;
 			}
 			if (messageColMaxWidth < msg.getMessage().length() * charWidth + 8) {
 				messageColMaxWidth = msg.getMessage().length() * charWidth + 8;
 			}
 		}
 		viewer.getTable().getColumn(1).setWidth(sourceColMaxWidth);
 		viewer.getTable().getColumn(3).setWidth(messageColMaxWidth);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 1.13
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName().equals(XMLDispatcher.DEVICE_DEFINITION_PROP)) {
 			if (e.getNewValue() instanceof IMeasuringStation) {
				if (this.messagesContainer == null) {
					return;
				}
 				if (this.messagesContainer.getMessageCount() > 200) {
 					this.messagesContainer.clear();
 					logger.debug("new scan arrived & msg count > 200 -> clear all");
 				} else {
 					logger.debug("new scan arrived but lt 200 msgs -> ignore");
 				}
 			}
 		}
 	}
 }
