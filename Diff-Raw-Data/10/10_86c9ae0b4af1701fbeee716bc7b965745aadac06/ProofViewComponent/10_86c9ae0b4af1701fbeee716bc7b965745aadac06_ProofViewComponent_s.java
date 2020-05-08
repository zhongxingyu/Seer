 package de.unisiegen.tpml.ui.proofview;
 
 import java.awt.BorderLayout;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.MessageFormat;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 
 import de.unisiegen.tpml.core.CannotRedoException;
 import de.unisiegen.tpml.core.CannotUndoException;
 import de.unisiegen.tpml.core.ProofModel;
 import de.unisiegen.tpml.graphics.Messages;
 import de.unisiegen.tpml.graphics.ProofView;
 import de.unisiegen.tpml.ui.EditorComponent;
 
 /**
  * Editor Component that displays a Proof. 
  * It unites undo / redo functions of the model (core) and visualization.
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
 
 	private static final Logger logger = Logger
 			.getLogger(ProofViewComponent.class);
 
 	private ProofView view;
 
 	private ProofModel model;
 
 	private boolean nextStatus;
 
 	private boolean redoStatus;
 
 	private boolean undoStatus;
 
 	public ProofViewComponent(ProofView view, ProofModel model) {
 		if (model == null || view == null) {
 			throw new NullPointerException("model or view are null");
 		}
 		setLayout(new BorderLayout());
 		this.view = view;
 		this.model = model;
 
 		this.model.addPropertyChangeListener(new ModelChangeListener());
 		add((JComponent) view, BorderLayout.CENTER);
 	}
 
 	/**
 	 * Returns the Next Status of the Component.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isNextStatus()
 	 */
 	public boolean isNextStatus() {
 		return nextStatus;
 	}
 
 	/**
 	 * Sets the Next Status of the Component.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setNextStatus(boolean)
 	 */
 	public void setNextStatus(boolean nextStatus) {
 
 		firePropertyChange("nextStatus", this.nextStatus, nextStatus);
 		this.nextStatus = nextStatus;
 	}
 
 	/**
 	 * Returns the Redo Status of the Component.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isRedoStatus()
 	 */
 	public boolean isRedoStatus() {
 		return redoStatus;
 	}
 
 	/**
 	 * Sets the Redo Status of the Component.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setRedoStatus(boolean)
 	 */
 	public void setRedoStatus(boolean redoStatus) {
 		firePropertyChange("redoStatus", this.redoStatus, redoStatus);
 		this.redoStatus = redoStatus;
 
 	}
 
 	/**
 	 * Returns the Undo Status of the Component.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#isUndoStatus()
 	 */
 	public boolean isUndoStatus() {
 		return undoStatus;
 	}
 
 	/**
 	 * Sets the Undo Status of the Component.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setUndoStatus(boolean)
 	 */
 	public void setUndoStatus(boolean undoStatus) {
 		firePropertyChange("undoStatus", this.undoStatus, undoStatus);
 		this.undoStatus = undoStatus;
 
 	}
 
 	/**
 	 * Sets the Default States of the Component's functions.
 	 * Attention: For now the NextStatus is alsways enabled.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#setDefaultStates()
 	 */
 	public void setDefaultStates() {
		setNextStatus(true);
 		setRedoStatus(model.isRedoable());
 		setUndoStatus(model.isUndoable());
 	}
 
 	/**
 	 * Executes the next function on the view.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#handleNext()
 	 */
 	public void handleNext() {
 		try {
 			view.guess();
 		} catch (Exception e) {
 			//logger.error("Guess could not be executed", e);
 			JOptionPane.showMessageDialog(getTopLevelAncestor(), MessageFormat.format(java.util.ResourceBundle.getBundle(
 			"de/unisiegen/tpml/ui/ui").getString("NodeComponent.5"), e.getMessage()), java.util.ResourceBundle.getBundle(
 			"de/unisiegen/tpml/ui/ui").getString("NodeComponent.6"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 	}
 
 	/**
 	 * Executes the redo function on the view.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#handleRedo()
 	 */
 	public void handleRedo() {
 		try {
 			model.redo();
 		} catch (CannotRedoException e) {
 			logger.error("Can not redo on this model", e);
 		}
 
 	}
 
 	/**
 	 * Executes the undo function on the view.
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unisiegen.tpml.ui.EditorComponent#handleUndo()
 	 */
 	public void handleUndo() {
 		try {
 			model.undo();
 		} catch (CannotUndoException e) {
 			logger.error("Can not undo on this model", e);
 		}
 
 	}
 
 	/**
 	 * Handles Property Changes fired by the model.
 	 * it supports:
 	 * 	undoable, redoable, and finished.
 	 *
 	 * @author Christoph Fehling
 	 * @version $Rev$ 
 	 *
 	 */
 	private class ModelChangeListener implements PropertyChangeListener {
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			if (evt.getPropertyName().equals("undoable")) {
 				setUndoStatus((Boolean) evt.getNewValue());
 			} else if (evt.getPropertyName().equals("redoable")) {
 				setRedoStatus((Boolean) evt.getNewValue());
 			} else if (evt.getPropertyName().equals("finished")) {
 				setNextStatus(!(Boolean) evt.getNewValue());
 			}
 		}
 
 	}
 
 	public void setAdvanced(boolean status) {
 		view.setAdvanced(status);
 	}
 }
