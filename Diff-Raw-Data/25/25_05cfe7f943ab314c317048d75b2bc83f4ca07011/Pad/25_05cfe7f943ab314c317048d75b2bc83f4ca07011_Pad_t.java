 package org.eclipse.iee.editor.core.pad;
 
 import java.util.UUID;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.iee.editor.core.container.Container;
 import org.eclipse.swt.widgets.Composite;
 
 public abstract class Pad {
 	private String fContainerID;
	private String fType;
 	private Container fContainer;
 
 	public Pad() {
 		fContainerID = UUID.randomUUID().toString();
		fType = "Unknown";
 	}
 
 	public Pad(String id) {
 		fContainerID = id;
		fType = "Unknown";
 	}
 
 	public void setContainerID(String containerID) {
 		fContainerID = containerID;
 	}
 
 	public String getContainerID() {
 		return fContainerID;
 	}
 
 	public String getContainerManagerID() {
 		return fContainer.getContainerManagerID();
 	}
 
 	public boolean isContainerAttached() {
 		return fContainer != null;
 	}
 
 	public Container getContainer() {
 		return fContainer;
 	}
 
 	public void attachContainer(Container container) {
 		Assert.isNotNull(container);
 		Assert.isLegal(!isContainerAttached(),
 				"Another container is already attached");
 
 		if (!fContainerID.equals(container.getContainerID())) {
 			container.setContainerID(fContainerID);
 		}
 
 		fContainer = container;
 		createPartControl(fContainer.getComposite());
 		fContainer.getComposite().pack();
 	}
 
 	public void detachContainer() {
 		Assert.isLegal(isContainerAttached(), "No container attached");
 		Assert.isLegal(fContainerID.equals(fContainer.getContainerID()));
 
 		fContainer = null;
 	}
 
 	/* Abstract methods */
 
 	/**
 	 * Method is called when Container is attached to Pad
 	 */
 	public abstract void createPartControl(Composite parent);
 
 	/**
 	 * Copy pad with @param containerID
 	 * 
 	 * @return
 	 */
 	public abstract Pad copy();
 
 	/**
 	 * Save pad
 	 */
 	public abstract void save();
 
 	/**
 	 * Remove pad's storage
 	 */
 	public abstract void unsave();
 
 	/**
	 * Getter for fType
	 */
	public String getType()
	{
		return fType;
	}
	
	/**
	 * Setter for fType
 	 */
	public void setType(String type)
	{
		fType = type;
	}
 
 }
