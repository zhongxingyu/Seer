 package de.shop.util.rest;
 
 
 /**
  * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
  */
 public class NotFoundException extends RuntimeException {
 	private static final long serialVersionUID = -866705588853138386L;
 	
 	private final Object[] args;
 
 	public NotFoundException(String msg, Object... args) {
 		super(msg);
 		this.args = args;
 	}
 
 	public Object[] getArgs() {
		return args;
 	}
 }
