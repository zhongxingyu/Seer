 /**
  * TODO add documentation here
  * 
  */
 package de.unisiegen.tpml.ui.proofview;
 
 import java.awt.BorderLayout;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.JComponent;
 
 import org.apache.log4j.Logger;
 
 import de.unisiegen.tpml.core.CannotRedoException;
 import de.unisiegen.tpml.core.CannotUndoException;
 import de.unisiegen.tpml.core.ProofModel;
 import de.unisiegen.tpml.graphics.ProofView;
 import de.unisiegen.tpml.ui.EditorComponent;
 
 /**
  * //TODO add documentation here.
  *
  * @author Christoph Fehling
  * @version $Rev$ 
  *
  */
 public class ProofViewComponent extends JComponent implements EditorComponent {
 	
	/**
	 * The unique serialization identifier.
	 */
	private static final long serialVersionUID = 8218146393722855647L;
	
 	private static final Logger logger = Logger.getLogger(ProofViewComponent.class);
 	private ProofView view;
 	private ProofModel model;
 	private boolean nextStatus;
     private boolean redoStatus;
     private boolean undoStatus;
 	
 	public ProofViewComponent(ProofView view, ProofModel model){
 		if (model == null || view == null){
 			throw new NullPointerException("model or view are null");
 		}
 		setLayout(new BorderLayout());
 		this.view = view;
 		this.model = model;
 		
 		this.model.addPropertyChangeListener(new ModelChangeListener());
 		add((JComponent)view, BorderLayout.CENTER);
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isNextStatus()
 	 */
 	public boolean isNextStatus() {
 		return nextStatus;
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setNextStatus(boolean)
 	 */
 	public void setNextStatus(boolean nextStatus) {
 
 		firePropertyChange ("nextStatus", this.nextStatus, nextStatus);
 		this.nextStatus=nextStatus;
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isRedoStatus()
 	 */
 	public boolean isRedoStatus() {
 		return redoStatus;
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setRedoStatus(boolean)
 	 */
 	public void setRedoStatus(boolean redoStatus) {
 		firePropertyChange ("redoStatus", this.redoStatus, redoStatus);
 		this.redoStatus=redoStatus;
 
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isSaveStatus()
 	 */
 	public boolean isSaveStatus() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setSaveStatus(boolean)
 	 */
 	public void setSaveStatus(boolean saveStatus) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isUndoStatus()
 	 */
 	public boolean isUndoStatus() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setUndoStatus(boolean)
 	 */
 	public void setUndoStatus(boolean undoStatus) {
 		firePropertyChange ("undoStatus", this.undoStatus, undoStatus);
 		this.undoStatus=undoStatus;
 
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isChangeStatus()
 	 */
 	public boolean isChangeStatus() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setChangeStatus(boolean)
 	 */
 	public void setChangeStatus(boolean changeStatus) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setDefaultStates()
 	 */
 	public void setDefaultStates() {
 		setNextStatus(true);
 		setRedoStatus(model.isRedoable());
 		setUndoStatus(model.isUndoable());
 		setChangeStatus(false);
 		setSaveStatus(false);
 
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#handleNext()
 	 */
 	public void handleNext() {
 		// TODO Auto-generated method stub
 		try{
 		view.guess();
 		}
 		catch (Exception e){
 			//TODO log or whatever...
 		}
 
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#handleRedo()
 	 */
 	public void handleRedo() {
 		try{
 			model.redo();
 			} catch (CannotRedoException e) {
 				logger.error("Can not redo on this model", e);
 			}
 
 	}
 
 	/**
 	 * TODO add documentation here 
 	 *
 	 * {@inheritDoc}
 	 * @see de.unisiegen.tpml.ui.EditorComponent#handleUndo()
 	 */
 	public void handleUndo() {
 		try{
 		model.undo();
 		} catch (CannotUndoException e) {
 			logger.error("Can not undo on this model", e);
 		}
 
 	}
 	
 	private class ModelChangeListener implements PropertyChangeListener{
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			if (evt.getPropertyName().equals("undoable")){
 				setUndoStatus((Boolean)evt.getNewValue());
 			}
			else if (evt.getPropertyName().equals("redoable")){
 				setRedoStatus((Boolean)evt.getNewValue());
 			}
			else if (evt.getPropertyName().equals("finished")){
				setNextStatus(!(Boolean)evt.getNewValue());
 			}
 		}
 		
 	}
 }
