 package ch.cern.atlas.apvs.domain;
 
import java.io.IOException;

public class APVSException extends IOException {
 
 	private static final long serialVersionUID = 3520019113105842483L;
 
 	public APVSException() {
 	}
 
 	public APVSException(String msg) {
 		super(msg);
 	}
 
 	public APVSException(Throwable t) {
 		super(t);
 	}
 	
 	public APVSException(String msg, Throwable t) {
 		super(msg, t);
 	}
 }
