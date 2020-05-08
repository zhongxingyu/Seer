 package core;
 
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import modele.PerspectiveModel;
 
 public abstract class AbstractCoreAction extends AbstractAction{
 	
 	private static final long serialVersionUID = 1L;
 	private Memento memento;
 	
	public AbstractCoreAction(String resource) {
		super(resource);
 	}

 	public void undo(){
 		Memento memento_redo = PerspectiveModel.getInstance().getMemento();
 		PerspectiveModel.getInstance().setMemento(memento);
 		memento = memento_redo;
 	}
 	
 	public void redo(){
 		Memento memento_undo = PerspectiveModel.getInstance().getMemento();
 		PerspectiveModel.getInstance().setMemento(memento);
 		memento = memento_undo;
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		memento = PerspectiveModel.getInstance().getMemento();
 		executeAction();		
 	}
 	
 	public abstract void executeAction();
 
 }
