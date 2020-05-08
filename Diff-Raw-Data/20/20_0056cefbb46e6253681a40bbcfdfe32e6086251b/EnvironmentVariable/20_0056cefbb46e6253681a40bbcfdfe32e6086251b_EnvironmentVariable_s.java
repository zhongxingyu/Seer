 package org.eclipse.dltk.launching;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 
 public class EnvironmentVariable {
 	private String name;
 	private String value;
 
 	public EnvironmentVariable(EnvironmentVariable var) {
 		this(var.getName(), var.getValue());
 	}
 
 	public EnvironmentVariable(String name, String value) {
 		this.name = name;
 		this.value = value;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		final EnvironmentVariable other = (EnvironmentVariable) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	public IStatus validate() {
 		String name = getName();
 		String value = getValue();
 		if (name == null && value == null) {
 			return new Status(
 					IStatus.ERROR,
 					DLTKCore.PLUGIN_ID,
 					0,
 					Messages.EnvironmentVariable_variableNameAndValueMustNotBeEmpty,
 					null);
 		}
 		return Status.OK_STATUS;
 	}
 
 	public String toString() {
 		return this.name + "=" + this.value; //$NON-NLS-1$
 	}
 }
