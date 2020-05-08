 package haw.ci.lib.nodes;
 
 import haw.ci.lib.SymbolTable;
 import haw.ci.lib.descriptor.Descriptor;
 
 public class StringNode extends AbstractNode {
 	private static final long serialVersionUID = 6088338929581231417L;
 	private final String value;
 
 	public StringNode(String value) {
 		this.value = value;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		StringNode other = (StringNode) obj;
 		if (value == null) {
 			if (other.value != null)
 				return false;
 		} else if (!value.equals(other.value))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString(int indentation) {
 		String result = toString(indentation, this.getClass().getName() + "\n");
 		if(value != null) {
 		    result += value.toString() + "\n";
 		}
 
 	    return result;
 	}
 
 	@Override
 	public Descriptor compile(SymbolTable symbolTable) {
 		// Nothing to do
 		return null;
 	}
 }
