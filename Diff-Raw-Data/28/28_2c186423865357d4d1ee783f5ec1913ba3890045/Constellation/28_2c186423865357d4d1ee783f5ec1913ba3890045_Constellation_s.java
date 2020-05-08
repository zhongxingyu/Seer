 package com.constello.client;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.vaadin.contrib.gwtgraphics.client.DrawingArea;
 import com.vaadin.contrib.gwtgraphics.client.animation.Animate;
 
 public class Constellation extends DrawingArea {
 
 	/* Default Constructor */
 	public Constellation(int width, int height) {
 		
 		// Call parent's constructor
 		super(width, height);
 		
 	}
 	
 	/* Add a Star to the Constellation */
 	public void addStar(Star s) {
 		
 		add(s);
 		_stars.add(s);
 		s.parentIs(this);
 		++_numStars;
 	}
 	
 	/* Create a link between two stars */
 	public void linkStars(Star s1, Star s2) {
 		
 		Link lk = new Link(s1, s2);
 		s1.addNeighbor(s2, lk);
 		s2.addNeighbor(s1, lk);
 		add(lk);
 		_links.add(lk);
 		lk.parentIs(this);
 		++_numLinks;
 	}
 	
 	/* Slowly dim the links at beginning of round*/
 	public void dimLinks() {
 		
 		Iterator<Link> linkListIt = _links.iterator();
 		while(linkListIt.hasNext()) {
 			
 			Link l = linkListIt.next();
 			l.dim();
 		}
 	}
 	
 	/* Execute _nextMove on the Constellation */
 	public Boolean nextMove() {
 		
 		// TODO
 		return true;
 	}
 	
 	/* Audit interface */
 	public int auditErrors(int scope) {
 		
 		// Instantiate the error counter
 		AuditReport ar = new AuditReport("Constellation");
 		
 		// Check invariants
 		// ar.verify(_SomeExpression_, "_SomeExpression_ is true");
 		
 		// Invariant 1.1
 		// - check that _stars.size() matches _numStars
 		ar.verify(_stars.size() == _numStars, "Star list size matches star count");
 		
 		// Invariant 1.2
 		// - check that _links.size() matches _numLinks
 		ar.verify(_links.size() == _numLinks, "Link list size matches link count");
 		
 		// Invariant 1.3
 		// - check that getVectorObjectCount matches _numStars + _numLinks
 		ar.verify(getVectorObjectCount() == (_numStars + _numLinks), "Vector object count matches (star count + link count)");
 		
		
 		// Invariant 2.1
 		// - every Star in _stars should have this Constellation as the parent
 		Iterator<Star> starListIt = _stars.iterator();
 		while(starListIt.hasNext()) {
 			
 			Star s = starListIt.next();
 			ar.verify(s.parent() == this, "Star child points back to correct Constellation parent");
 		}
 		
 		// Invariant 2.2
 		// - every Link in _links should have this Constellation as the parent
 		Iterator<Link> linkListIt = _links.iterator();
 		while(linkListIt.hasNext()) {
 			
 			Link l = linkListIt.next();
 			ar.verify(l.parent() == this, "Link child points back to correct Constellation parent");
 		}
 		
 		return ar.falseInvariants();
 	}
 
 	/* Private members */
 	private List<Star> _stars = new ArrayList<Star>();
 	private List<Link> _links = new ArrayList<Link>();
 	private Move _nextMove = new Move();
 	private int _numStars = 0; // Redundant: for auditing _stars
 	private int _numLinks = 0; // Redundant: for auditing _links
 }
