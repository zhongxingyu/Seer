 package org.geworkbench.events.listeners;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import org.geworkbench.analysis.AbstractSaveableParameterPanel;
 
 /**
  * 
  * @author keshav, yc2480
  * @version $Id: ParameterActionListener.java,v 1.1.2.1 2009/01/22 16:50:10
  *          keshav Exp $
  */
 public class ParameterActionListener implements ActionListener,
		ListDataListener, ChangeListener, PropertyChangeListener {
 
 	private AbstractSaveableParameterPanel aspp = null;
 
 	/**
 	 * 
 	 * @param aspp
 	 */
 	public ParameterActionListener(AbstractSaveableParameterPanel aspp) {
 		this.aspp = aspp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent evt) {
 		notifyAnalysisPanelIfNeeded();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
 	 */
 	public void contentsChanged(ListDataEvent arg0) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
 	 */
 	public void intervalAdded(ListDataEvent arg0) {
 		notifyAnalysisPanelIfNeeded();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
 	 */
 	public void intervalRemoved(ListDataEvent arg0) {
 		notifyAnalysisPanelIfNeeded();
 	}
 
 	public void stateChanged(ChangeEvent arg0) {
 		notifyAnalysisPanelIfNeeded();
 	}
 
 	boolean calledFromProgramFlag = false;
 
 	/**
 	 * 
 	 * @param flag
 	 *            Set the flag to "true" before you change any monitored GUI
 	 *            components, to avoid cycle events.
 	 */
 	public void setCalledFromProgramFlag(boolean flag) {
 		this.calledFromProgramFlag = flag;
 	}
 
 	public boolean getCalledFromProgramFlag() {
 		return this.calledFromProgramFlag;
 	}
 
 	private void notifyAnalysisPanelIfNeeded() {
 		if (!calledFromProgramFlag) {
 			calledFromProgramFlag = true;
 			aspp.notifyAnalysisPanel();
 			calledFromProgramFlag = false;
 		}
 	}
 
 	public void propertyChange(PropertyChangeEvent arg0) {
 		notifyAnalysisPanelIfNeeded();
 	}
 }
