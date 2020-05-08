 package org.node.idleness;
 
import org.ourgrid.node.idleness.IdlenessDetector;
import org.ourgrid.node.idleness.IdlenessListener;
 
public class TestIdlenessChecker implements IdlenessDetector {
 
 	private boolean isIdle = true;
 	
 	public void setIdle(boolean isIdle) {
 		this.isIdle = isIdle;  
 	}
 	
 	@Override
 	public boolean isIdle() {
 		return isIdle;
 	}
 
	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public void addListener(IdlenessListener listener) {
		// TODO Auto-generated method stub
	}

 }
