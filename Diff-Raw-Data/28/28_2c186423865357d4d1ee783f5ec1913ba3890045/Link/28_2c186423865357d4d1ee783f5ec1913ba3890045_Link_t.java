 package com.constello.client;
 
 import com.vaadin.contrib.gwtgraphics.client.Line;
 import com.vaadin.contrib.gwtgraphics.client.animation.Animate;
 
 public class Link extends Line {
 
 	/* Default Constructor */
 	public Link(Star s1, Star s2) {
 		
 		// Call Line constructor with s1 and s2's coordinates
 		super(s1.getX(), s1.getY(), s2.getX(), s2.getY());
 		
 		// Store pointers to the two stars
 		_s1 = s1;
 		_s2 = s2;
 		
 		// Set graphical properties
 		setStrokeColor("yellow");
 		setStrokeWidth(10);
 	}
 	
 	/* Getter for _parent */
 	public Constellation parent() {
 		
 		return _parent;
 	}
 	
 	/* Setter for _parent */
 	public void parentIs(Constellation c) {
 		
 		_parent = c;
 	}
 	
 	/* Dim the link */
 	public void dim() {
 		
		new Animate(this, "strokeopacity", 1.0, 0.0, 1000).start();
 	}
 
 	/* Audit interface */
 	public int auditErrors(int scope) {
 		
 		// Instantiate the error counter
 		AuditReport ar = new AuditReport("Link (" + getX1() + ", " + getY1() + "), (" + getX2() + ", " + getY2() + ")");
 		
 		// Check invariants
 		// ar.verify(_SomeExpression_, "_SomeExpression_ is true");
 		
 		return ar.falseInvariants();
 	}
 	
 	/* Private members */
 	private Star _s1, _s2;  // The two stars connected by this link
 	private Constellation _parent = null;
 }
