 package edu.teco.dnd.blocks;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * An output of a {@link FunctionBlock}.
  * 
  * @param <T>
  *            the type of data this Output outputs
  */
 public class Output<T extends Serializable> implements Serializable {
 	/**
 	 * Used for serialization.
 	 */
 	private static final long serialVersionUID = 8652366978996728530L;
 
 	/**
 	 * The name of this Output.
 	 */
 	private final String name;
 
 	/**
 	 * The {@link ConnectionTarget}s connected to this output.
 	 */
 	private final Set<ConnectionTarget> connectedTargets = new HashSet<ConnectionTarget>();
 
 	/**
 	 * The type of this output.
 	 */
 	private Class<T> type;
 
 	/**
 	 * Constructs a new Output.
 	 * 
 	 * @param name
 	 *            the name of the Output
 	 */
 	public Output(final String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Sets a new value for this Output. Connected targets will be informed about this change.
 	 * 
 	 * @param value
 	 *            the new value
 	 */
 	public final synchronized void setValue(final T value) {
 		for (ConnectionTarget ct : connectedTargets) {
 			ct.setValue(value);
 		}
 	}
 
 	/**
	 * Adds a {@link ConnectionTarget} to the list of connected targets. The connected Output of the target is
	 * set automatically.
 	 * 
 	 * @param target
 	 *            the new target
 	 */
 	public final synchronized void addConnection(final ConnectionTarget target) {
 		if (!connectedTargets.contains(target)) {
 			connectedTargets.add(target);
 			target.setConnectedOutput(this);
 		}
 	}
 
 	/**
	 * Removes a {@link ConnectionTarget} from the list of connected targets. The connected Output of the
	 * target is set to null if it has been connected to this Output.
 	 * 
 	 * @param target
 	 *            the target to remove
 	 */
 	public final synchronized void removeConnection(final ConnectionTarget target) {
 		if (connectedTargets.contains(target)) {
 			connectedTargets.remove(target);
 			target.setConnectedOutput(null);
 		}
 	}
 
 	/**
 	 * Returns all {@link ConnectionTarget}s connected to this Output.
 	 * 
 	 * @return all ConnectionTargets connected to this output
 	 */
 	public final synchronized Set<ConnectionTarget> getConnectedTargets() {
 		return connectedTargets;
 	}
 
 	/**
 	 * Returns the name of the Output.
 	 * 
 	 * @return the name of the Output
 	 */
 	public final String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the type of this output.
 	 * 
 	 * @return the type of this output
 	 */
 	public final Class<T> getType() {
 		return type;
 	}
 
 	/**
 	 * Sets the type of this output.
 	 * 
 	 * @param type
 	 *            the type of this output.
 	 */
 	public final void setType(final Class<T> type) {
 		this.type = type;
 	}
 
 	/**
 	 * Returns whether or not a ConnectionTarget is compatible with this output.
 	 * 
 	 * @param ct
 	 *            the ConnectionTarget to test. Must not be null.
 	 * @return true if the ConnectionTarget can be connected to this output
 	 */
 	public final boolean isCompatible(final ConnectionTarget ct) {
 		if (ct == null) {
 			throw new IllegalArgumentException("ct must not be null");
 		}
 		return this.type == null || ct.getType().isAssignableFrom(this.type);
 	}
 
	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((connectedTargets == null) ? 0 : connectedTargets.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		return result;
 	}
 
	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Output<?> other = (Output<?>) obj;
 		if (connectedTargets == null) {
 			if (other.connectedTargets != null) {
 				return false;
 			}
 		} else if (!connectedTargets.equals(other.connectedTargets)) {
 			return false;
 		}
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		if (type == null) {
 			if (other.type != null) {
 				return false;
 			}
 		} else if (!type.equals(other.type)) {
 			return false;
 		}
 		return true;
 	}
 }
