 package edu.teco.dnd.module;
 
 import java.io.Serializable;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.blocks.AssignmentException;
 import edu.teco.dnd.blocks.ConnectionTarget;
 
 public class RemoteConnectionTarget extends ConnectionTarget {
 	private static final long serialVersionUID = -4588323454242345616L;
 	private static final Logger LOGGER = LogManager.getLogger(RemoteConnectionTarget.class);
 	
 	private Application app;
 	private final UUID targetFunctionBlock;
 	private final String targetInput;
 
 	public RemoteConnectionTarget(String name, final UUID targetFunctionBlock, final String targetInput) {
 		super(name);
 		if (targetFunctionBlock == null) {
 			throw new IllegalArgumentException("targetFunctionBlock must not be null");
 		}
 		if (targetInput == null) {
 			throw new IllegalArgumentException("targetInput must not be null");
 		}
 		this.targetFunctionBlock = targetFunctionBlock;
 		this.targetInput = targetInput;
 	}
 
 	@Override
 	public Class<? extends Serializable> getType() {
 		return null;
 	}
 
 	@Override
 	public void setValue(Serializable value) {
 		if (app != null) {
 			app.sendValue(targetFunctionBlock, targetInput, value);
 		} else {
			throw LOGGER.throwing(new IllegalStateException("associated application not set before trying to send a value."));
 		}
 	}
 	
 	public void setApplication(Application app) {
 		this.app = app;
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 
 	@Override
 	public void update() throws AssignmentException {
 	}
 }
