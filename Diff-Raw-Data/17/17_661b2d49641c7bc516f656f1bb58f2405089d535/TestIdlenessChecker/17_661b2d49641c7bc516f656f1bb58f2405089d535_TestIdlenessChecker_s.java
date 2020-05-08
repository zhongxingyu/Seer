 package org.node.idleness;
 
import org.ourgrid.node.idleness.IdlenessChecker;
 
public class TestIdlenessChecker implements IdlenessChecker {
 
 	private boolean isIdle = true;
 	
 	public void setIdle(boolean isIdle) {
 		this.isIdle = isIdle;  
 	}
 	
 	@Override
 	public boolean isIdle() {
 		return isIdle;
 	}
 
 }
