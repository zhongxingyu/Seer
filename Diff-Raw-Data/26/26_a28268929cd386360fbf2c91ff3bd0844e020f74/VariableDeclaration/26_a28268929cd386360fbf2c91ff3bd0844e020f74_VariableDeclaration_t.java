 package cps450;
 
 public class VariableDeclaration extends Declaration {
 	
 	Integer instancePosition;
 	Integer argumentPosition;
 	Integer localPosition;
 	
 	public VariableDeclaration(Type _type, String _location) {
 		super(_type, _location);
 	}
 	
 	public void setArgumentPosition(Integer argPos) {
 		argumentPosition = argPos;
 	}
 	
 	public void setLocalPosition(Integer localPos) {
 		localPosition = localPos;
 	}
 	
 	public void setInstancePosition(Integer instPos) {
 		instancePosition = instPos;
 	}
 	
 	public Boolean isInstanceVariable() {
 		return instancePosition != null;
 	}
 	
 	public Boolean isLocalVariable() {
 		return argumentPosition != null || localPosition != null;
 	}
 	
 	public Integer getInstanceOffset() {
 		if (instancePosition != null) {
 			return instancePosition * 4 + 8;
 		} else {
 			return null;
 		}
 	}
 	
 	public Integer getStackOffset() {
 		if (argumentPosition != null) {
			return argumentPosition * 4 + 8; // Offset for saved copy of EBP and return address 
 		} else if (localPosition != null) {
 			// Local variable
 			return localPosition * -4;
 		}
 		return null;
 	}
 	
 }
