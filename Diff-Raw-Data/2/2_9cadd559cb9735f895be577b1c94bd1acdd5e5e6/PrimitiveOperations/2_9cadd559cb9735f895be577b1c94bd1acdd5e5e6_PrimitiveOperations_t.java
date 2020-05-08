 /* MIPL: Mining Integrated Programming Language
  *
  * File: PrimitiveOperations.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Primitive Operations
 */
 package edu.columbia.mipl.datastr;
 
 import edu.columbia.mipl.conf.Configuration;
 import edu.columbia.mipl.matops.*;
 
 public class PrimitiveOperations {
 //	static MapReduceMatrixOperations ops;
 	private static MatrixOperations ops;	
 	static {
 		int mode = Configuration.getInstance().getMode();
 		if (mode == Configuration.MODE_LOCAL)
 			ops = new DefaultMatrixOperations();
 		else if (mode == Configuration.MODE_REMOTE)
 			ops = new MapReduceMatrixOperations();
 	}
 
 	public static PrimitiveType assign(PrimitiveType arg1, PrimitiveType arg2) {
 		if (arg1 == null) {
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix m = (PrimitiveMatrix) arg2;
 				arg1 = new PrimitiveMatrix(m.getRow(), m.getCol());
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
				return new PrimitiveDouble(((PrimitiveDouble) arg2).getData());
 			}
 			else
 				assert (false);
 		}
 		
 		assert (arg2 != null);
 		
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix t = (PrimitiveMatrix) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix s = (PrimitiveMatrix) arg2;
 				ops.assign(t, s);
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) arg2;
 //				ops.assign(t.getData(), s.getData());
 				arg1 = new PrimitiveDouble(s.getData());
 			}
 			else
 				assert (false);
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix m = (PrimitiveMatrix) arg2;
 				arg1 = new PrimitiveMatrix(m.getRow(), m.getCol());
 				ops.assign((PrimitiveMatrix) arg1, m);
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble d1 = (PrimitiveDouble) arg1;
 				PrimitiveDouble d2 = (PrimitiveDouble) arg2;
 //				arg1 = new PrimitiveDouble(s.getData());
 				d1.setData(d2.getData());
 			}
 			else
 				assert (false);
 		}
 		else {
 			assert (false);
 //			return null;
 		}
 
 		return arg1;
 	}
 /*	
 	public static PrimitiveType addAssign(PrimitiveType target, PrimitiveType source) {
 		if (target instanceof PrimitiveMatrix) {
 			PrimitiveMatrix t = (PrimitiveMatrix) target;
 			if (source instanceof PrimitiveMatrix) {
 				PrimitiveMatrix s = (PrimitiveMatrix) source;
 				ops.addassign(t.getData(), s.getData());
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				ops.addassign(t.getData(), s.getData());
 			}
 		}
 		else if (target instanceof PrimitiveDouble) {
 			PrimitiveDouble t = (PrimitiveDouble) target;
 			if (source instanceof PrimitiveMatrix) {
 				assert (false);
 				return null;
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				target = new PrimitiveDouble(t.getData() + s.getData());
 			}
 		}
 		else
 			return null;
 
 		return target;
 	}
 	
 	public static PrimitiveType subAssign(PrimitiveType target, PrimitiveType source) {
 		if (target instanceof PrimitiveMatrix) {
 			PrimitiveMatrix t = (PrimitiveMatrix) target;
 			if (source instanceof PrimitiveMatrix) {
 				PrimitiveMatrix s = (PrimitiveMatrix) source;
 				ops.subassign(t.getData(), s.getData());
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				ops.subassign(t.getData(), s.getData());
 			}
 		}
 		else if (target instanceof PrimitiveDouble) {
 			PrimitiveDouble t = (PrimitiveDouble) target;
 			if (source instanceof PrimitiveMatrix) {
 				assert (false);
 				return null;
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				target = new PrimitiveDouble(t.getData() - s.getData());
 			}
 		}
 		else
 			return null;
 
 		return target;
 	}
 
 	public static PrimitiveType multiAssign(PrimitiveType target, PrimitiveType source) {
 		if (target instanceof PrimitiveMatrix) {
 			PrimitiveMatrix t = (PrimitiveMatrix) target;
 			if (source instanceof PrimitiveMatrix) {
 				PrimitiveMatrix s = (PrimitiveMatrix) source;
 				ops.multassign(t.getData(), s.getData());
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				ops.multassign(t.getData(), s.getData());
 			}
 		}
 		else if (target instanceof PrimitiveDouble) {
 			PrimitiveDouble t = (PrimitiveDouble) target;
 			if (source instanceof PrimitiveMatrix) {
 				assert (false);
 				return null;
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				target = new PrimitiveDouble(t.getData() * s.getData());
 			}
 		}
 		else
 			return null;
 
 		return target;
 	}
 	
 	public static PrimitiveType divAssign(PrimitiveType target, PrimitiveType source) {
 		if (target instanceof PrimitiveMatrix) {
 			PrimitiveMatrix t = (PrimitiveMatrix) target;
 			if (source instanceof PrimitiveMatrix) {
 				PrimitiveMatrix s = (PrimitiveMatrix) source;
 				ops.divassign(t.getData(), s.getData());
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				ops.divassign(t.getData(), s.getData());
 			}
 		}
 		else if (target instanceof PrimitiveDouble) {
 			PrimitiveDouble t = (PrimitiveDouble) target;
 			if (source instanceof PrimitiveMatrix) {
 				assert (false);
 				return null;
 			}
 			else if (source instanceof PrimitiveDouble) {
 				PrimitiveDouble s = (PrimitiveDouble) source;
 				target = new PrimitiveDouble(t.getData() / s.getData());
 			}
 		}
 		else
 			return null;
 
 		return target;
 	}
 */
 	public static PrimitiveBool or(PrimitiveBool expr1, PrimitiveBool expr2) {
 		assert (expr1 != null && expr2 != null);
 		return new PrimitiveBool(expr1.getData() || expr2.getData());
 	}
 
 	public static PrimitiveBool and(PrimitiveBool expr1, PrimitiveBool expr2) {
 		assert (expr1 != null && expr2 != null);
 		return new PrimitiveBool(expr1.getData() && expr2.getData());
 	}
 
 	public static PrimitiveBool eq(PrimitiveType expr1, PrimitiveType expr2) {
 		assert (expr1 != null && expr2 != null);
 		if (expr1 instanceof PrimitiveMatrix && expr2 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix e1 = (PrimitiveMatrix) expr1;
 			PrimitiveMatrix e2 = (PrimitiveMatrix) expr2;
 			return new PrimitiveBool(e1.getData().equalsSemantically(e2.getData()));
 		}
 		else if (expr1 instanceof PrimitiveDouble && expr2 instanceof PrimitiveDouble) {
 			PrimitiveDouble e1 = (PrimitiveDouble) expr1;
 			PrimitiveDouble e2 = (PrimitiveDouble) expr2;
 			return new PrimitiveBool(e1.getData().compareTo(e2.getData()) == 0);
 		}
 		else if (expr1 instanceof PrimitiveBool && expr2 instanceof PrimitiveBool) {
 			PrimitiveBool e1 = (PrimitiveBool) expr1;
 			PrimitiveBool e2 = (PrimitiveBool) expr2;			
 			return new PrimitiveBool(e1.getData() == e2.getData());
 		}
 		return new PrimitiveBool(false);
 	}
 
 	public static PrimitiveBool ne(PrimitiveType expr1, PrimitiveType expr2) {
 		assert (expr1 != null && expr2 != null);
 		return new PrimitiveBool(!eq(expr1, expr2).getData());
 	}
 
 	public static PrimitiveBool lt(PrimitiveType expr1, PrimitiveType expr2) {
 		assert (expr1 != null && expr2 != null);
 		if (expr1 instanceof PrimitiveDouble && expr2 instanceof PrimitiveDouble) {
 			PrimitiveDouble e1 = (PrimitiveDouble) expr1;
 			PrimitiveDouble e2 = (PrimitiveDouble) expr2;
 			return new PrimitiveBool(e1.getData() < e2.getData());
 		}
 		return null; // throws Excpetion;
 	}
 
 	public static PrimitiveBool gt(PrimitiveType expr1, PrimitiveType expr2) {
 		assert (expr1 != null && expr2 != null);
 		if (expr1 instanceof PrimitiveDouble && expr2 instanceof PrimitiveDouble) {
 			PrimitiveDouble e1 = (PrimitiveDouble) expr1;
 			PrimitiveDouble e2 = (PrimitiveDouble) expr2;
 			return new PrimitiveBool(e1.getData() > e2.getData());
 		}
 		return null; // throws Excpetion;
 	}
 
 	public static PrimitiveBool le(PrimitiveType expr1, PrimitiveType expr2) {
 		assert (expr1 != null && expr2 != null);
 		if (expr1 instanceof PrimitiveDouble && expr2 instanceof PrimitiveDouble) {
 			PrimitiveDouble e1 = (PrimitiveDouble) expr1;
 			PrimitiveDouble e2 = (PrimitiveDouble) expr2;
 			return new PrimitiveBool(e1.getData() <= e2.getData());
 		}
 		return null; // throws Excpetion;
 	}
 
 	public static PrimitiveBool ge(PrimitiveType expr1, PrimitiveType expr2) {
 		assert (expr1 != null && expr2 != null);
 		if (expr1 instanceof PrimitiveDouble && expr2 instanceof PrimitiveDouble) {
 			PrimitiveDouble e1 = (PrimitiveDouble) expr1;
 			PrimitiveDouble e2 = (PrimitiveDouble) expr2;
 			return new PrimitiveBool(e1.getData() >= e2.getData());
 		}
 		return null; // throws Excpetion;
 	}
 
 	public static PrimitiveType add(PrimitiveType arg1, PrimitiveType arg2) {		
 		PrimitiveType target = null;
 		
 		assert (arg1 != null && arg2 != null);
 		
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a1 = (PrimitiveMatrix) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix a2 = (PrimitiveMatrix) arg2;
 //				return ops.addMatrix(t, s);
 
 				target = ops.add(a1, a2);
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = ops.add(a1, a2.getData());
 			}
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			PrimitiveDouble a1 = (PrimitiveDouble) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				assert (false);				
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = new PrimitiveDouble(a1.getData() + a2.getData());
 			}
 		}		
 
 		return target;
 	}
 
 	public static PrimitiveType sub(PrimitiveType arg1, PrimitiveType arg2) {
 		PrimitiveType target = null;
 		
 		assert (arg1 != null && arg2 != null);		
 		
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a1 = (PrimitiveMatrix) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix a2 = (PrimitiveMatrix) arg2;
 				target = ops.sub(a1, a2);
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = ops.sub(a1, a2.getData());
 			}
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			PrimitiveDouble a1 = (PrimitiveDouble) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				assert (false);				
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = new PrimitiveDouble(a1.getData() - a2.getData());
 			}
 		}		
 
 		return target;
 	}
 
 
 	public static PrimitiveType mult(PrimitiveType arg1, PrimitiveType arg2) {
 		PrimitiveType target = null;
 		
 		assert (arg1 != null && arg2 != null);		
 		
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a1 = (PrimitiveMatrix) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix a2 = (PrimitiveMatrix) arg2;
 				target = ops.mult(a1, a2);
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = ops.mult(a1, a2.getData());
 			}
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			PrimitiveDouble a1 = (PrimitiveDouble) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix a2 = (PrimitiveMatrix) arg2;
 				target = ops.mult(a2, a1.getData());
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = new PrimitiveDouble(a1.getData() * a2.getData());
 			}
 		}		
 
 		return target;
 	}
 
 
 	public static PrimitiveType div(PrimitiveType arg1, PrimitiveType arg2) {
 		PrimitiveType target = null;
 		
 		assert (arg1 != null && arg2 != null);		
 		
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a1 = (PrimitiveMatrix) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix a2 = (PrimitiveMatrix) arg2;
 				target = ops.div(a1, a2);
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = ops.div(a1, a2.getData());
 			}
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			PrimitiveDouble a1 = (PrimitiveDouble) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				assert (false);				
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = new PrimitiveDouble(a1.getData() / a2.getData());
 			}
 		}		
 
 		return target;
 	}
 
 
 	public static PrimitiveType mod(PrimitiveType arg1, PrimitiveType arg2) {
 		PrimitiveType target = null;
 		
 		assert (arg1 != null && arg2 != null);		
 		
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a1 = (PrimitiveMatrix) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				PrimitiveMatrix a2 = (PrimitiveMatrix) arg2;
 				target = ops.mod(a1, a2);
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 				target = ops.mod(a1, a2.getData());
 			}
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			PrimitiveDouble a1 = (PrimitiveDouble) arg1;
 			if (arg2 instanceof PrimitiveMatrix) {
 				assert (false);				
 			}
 			else if (arg2 instanceof PrimitiveDouble) {
 				PrimitiveDouble a2 = (PrimitiveDouble) arg2;
 //				int v = (int) (a1.getData() / a2.getData());
 //				target = new PrimitiveDouble(a1.getData() - (v * a2.getData()));
 				target = new PrimitiveDouble(a1.getData() % a2.getData());
 			}
 		}		
 
 		return target;
 	}
 
 
 	/*
 	public static PrimitiveBool add(PrimitiveBool expr1, PrimitiveBool expr2) {
 	}
 
 	...
 	*/
 
 	public static PrimitiveType abs(PrimitiveType arg1) {
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a = (PrimitiveMatrix) arg1;
 			return ops.abs(a);
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			PrimitiveDouble a = (PrimitiveDouble) arg1;
 			double d = a.getData();
 			if (d < 0)
 				d = -d;
 			return new PrimitiveDouble(d);
 		}
 		else
 			return null;
 	}
 
 	public static PrimitiveType transpose(PrimitiveType arg1) {
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a = (PrimitiveMatrix) arg1;
 			return ops.transpose(a);
 		}
 		else
 			return null;
 	}
 
 	public static PrimitiveType rowsum(PrimitiveType arg1) {
 		if (arg1 instanceof PrimitiveMatrix) {
 			PrimitiveMatrix a = (PrimitiveMatrix) arg1;
 			return ops.rowsum(a);
 		}
 		else /* if (arg1 instanceof PrimitiveDouble) */
 			return null;
 	}
 
 	public static PrimitiveType cellmult(PrimitiveType arg1, PrimitiveType arg2) {
 		PrimitiveType target = null;
 		
 		assert (arg1 != null && arg2 != null);		
 		
 		if (arg1 instanceof PrimitiveMatrix && arg2 instanceof PrimitiveMatrix) {
 			target = ops.cellmult((PrimitiveMatrix) arg1, (PrimitiveMatrix) arg2);
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			assert (false);				
 		}		
 
 		return target;
 	}
 
 	public static PrimitiveType celldiv(PrimitiveType arg1, PrimitiveType arg2) {
 		PrimitiveType target = null;
 		
 		assert (arg1 != null && arg2 != null);		
 		
 		if (arg1 instanceof PrimitiveMatrix && arg2 instanceof PrimitiveMatrix) {
 			target = ops.celldiv((PrimitiveMatrix) arg1, (PrimitiveMatrix) arg2);
 		}
 		else if (arg1 instanceof PrimitiveDouble) {
 			assert (false);				
 		}		
 
 		return target;
 	}
 
 
 }
