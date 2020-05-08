 package edu.ai;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PylosReturnMove extends PylosRaiseMove {
 
 	List<PylosPosition> removals;
 	
 	public PylosReturnMove(PylosPosition move, PylosPosition raiseFrom, PylosPosition... removals) {
 		super(move,raiseFrom);
 		this.removals = new ArrayList<PylosPosition>();;
 		if(removals.length > 2) {
 			throw new IllegalArgumentException("Attempted to remove more than two spheres");
 		}
 		for(int i = 0; i < removals.length; i++) {
			if(removals[i] == null) continue;
 			this.removals.add(removals[i]);
 		}
 	}
 	
 	public boolean equals(Object o) {
 		if(o instanceof PylosReturnMove) {
 			PylosReturnMove prm = (PylosReturnMove) o;
 			if(this.raiseFrom == null && prm.raiseFrom != null) return false;
 			if(this.raiseFrom != null && prm.raiseFrom == null) return false;
 			if((this.raiseFrom != null && prm.raiseFrom != null)
 					&& !raiseFrom.equals(prm.raiseFrom)) return false;
 			int nSame = 0;
 			for(PylosPosition p : removals) {
 				for(PylosPosition p2 : prm.removals) {
 					if(p.equals(p2)) nSame++;
 				}
 			}
 			return nSame == removals.size() && prm.move.equals(move);
 		}
 		return false;
 	}
 }
