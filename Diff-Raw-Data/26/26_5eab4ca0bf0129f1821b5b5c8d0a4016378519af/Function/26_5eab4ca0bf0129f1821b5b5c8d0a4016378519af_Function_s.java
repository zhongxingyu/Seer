 package org.jheme.core;
 
 import java.util.Iterator;
 import java.util.List;
 
 public abstract class Function {
 	final private String _name;
 
 	public Function() {
 		_name = null;
 	}
 
 	public Function(String name) {
 		_name = name;
 	}
 
 	@Override
 	public String toString() {
 		if (_name != null)
 			return _name;
 		return "<unnamed function>";
 	}
 
 	public Object doCall(List<?> args) {
 		if (args.isEmpty())
 			return apply();
 		
 		final Iterator<?> iter = args.iterator();
 		
 		final Object a1 = iter.next();
 		if (!iter.hasNext())
 			return apply(a1);
 		final Object a2 = iter.next();
 		if (!iter.hasNext())
 			return apply(a1, a2);
 		final Object a3 = iter.next();
 		if (!iter.hasNext())
 			return apply(a1, a2, a3);
 		final Object a4 = iter.next();
 		if (!iter.hasNext())
 			return apply(a1, a2, a3, a4);
 		final Object a5 = iter.next();
 		if (!iter.hasNext())
 			return apply(a1, a2, a3, a4, a5);
 		
 		throw new UnsupportedOperationException("no functions with more than 5 arguments");
 	}
 	
 	protected Object apply() { 
		throw new UnsupportedOperationException("incorrect number of arguments: 0");
 	}
 
 	protected Object apply(Object a1) { 
		throw new UnsupportedOperationException("incorrect number of arguments: 1");
 	}
 	
 	protected Object apply(Object a1, Object a2) { 
		throw new UnsupportedOperationException("incorrect number of arguments: 2");
 	}
 	
 	protected Object apply(Object a1, Object a2, Object a3) { 
		throw new UnsupportedOperationException("incorrect number of arguments: 3");
 	}
 	
 	protected Object apply(Object a1, Object a2, Object a3, Object a4) { 
		throw new UnsupportedOperationException("incorrect number of arguments: 4");
 	}
	
 	protected Object apply(Object a1, Object a2, Object a3, Object a4, Object a5) { 
		throw new UnsupportedOperationException("incorrect number of arguments: 5");
 	}
 }
