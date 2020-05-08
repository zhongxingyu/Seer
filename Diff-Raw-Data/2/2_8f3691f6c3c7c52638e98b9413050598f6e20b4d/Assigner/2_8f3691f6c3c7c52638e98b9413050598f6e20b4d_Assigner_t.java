 package uk.ac.cam.db538.dexter.aux.struct;
 
 import uk.ac.cam.db538.dexter.aux.RuntimeUtils;
 import uk.ac.cam.db538.dexter.aux.TaintConstants;
 
 public final class Assigner {
 
 	private Assigner() { }
 	
 	public static final TaintExternal newExternal(Object obj, int initialTaint) {
 		TaintExternal tobj = newExternal_Undefined(obj.getClass());
 		defineExternal(obj, tobj, initialTaint);
 		return tobj;
 	}
 	
 	public static final TaintExternal newExternal_NULL(int initialTaint) {
 		return new TaintImmutable(initialTaint);
 	}
 
 	public static final TaintExternal newExternal_Undefined(Class<?> objClass) {
 		if (TaintConstants.isImmutableType(objClass))
 			return new TaintImmutable();
 		else 
 			return new TaintExternal();
 	}
 
 	public static final TaintInternal newInternal_NULL(int taint) {
 		TaintExternal t_super = newExternal_NULL(taint);
 		return new TaintInternal(null, t_super);
 	}
 	
 	public static final TaintInternal newInternal_Undefined() {
 		return new TaintInternal(null, null);
 	}
 	
 	public static final void defineExternal(Object obj, TaintExternal tobj, int taint) {
 		taint = TaintConstants.sinkTaint(obj, taint);
 		tobj.define(obj, taint);
 		Cache.insert(obj, tobj);
 	}
 	
 	public static final TaintArrayPrimitive newArrayPrimitive(Object obj, int length, int lengthTaint) {
 		TaintArrayPrimitive tobj = new TaintArrayPrimitive(obj, length, lengthTaint);
 		if (obj != null)
 			Cache.insert(obj, tobj);
 		return tobj;
 	}
 	
 	public static final TaintArrayReference newArrayReference(Object obj, int length, int lengthTaint) {
 		TaintArrayReference tobj = new TaintArrayReference((Object[]) obj, lengthTaint);
 		if (obj != null)
 			Cache.insert(obj, tobj);
 		return tobj;
 	}
 
 	public static final TaintExternal lookupExternal(Object obj, int taint) {
 		taint = TaintConstants.sinkTaint(obj, taint);
 		
 		if (TaintConstants.isImmutable(obj))
 			return new TaintImmutable(taint);
 		
 		TaintExternal tobj = (TaintExternal) Cache.get(obj);
 		if (tobj == null) {
 			tobj = new TaintExternal(taint);
 			Cache.insert(obj, tobj);
 		} else if (taint != TaintConstants.EMPTY.value)
 			tobj.set(taint);
 		
 		return tobj;
 	}
 
 	public static final TaintInternal lookupInternal(Object obj, int taint) {
 		if (obj == null)
 			RuntimeUtils.die("Cannot lookup internal taint of NULL");
 		else if (!(obj instanceof InternalDataStructure))
 			RuntimeUtils.die("Given object is not internal");
 
 		taint = TaintConstants.sinkTaint(obj, taint);
 		
 		TaintInternal tobj = (TaintInternal) Cache.get(obj);
 		if (tobj == null) {
 			
 			UndefinedObject undef = getConstructedSuperTaint();
 			if (undef == null)
 				RuntimeUtils.die("Internal object is not initialized");
 			
 			// define the object
 			tobj = undef.t_obj;
 			tobj.define((InternalDataStructure) obj, new TaintExternal(undef.t_init));
 			Cache.insert(obj, tobj);
 		}
 		
 		if (taint != TaintConstants.EMPTY.value) {
 			TaintInternal.clearVisited();
 			tobj.set(taint);
 		}
 		return tobj;
 	}
 
 	public static final Taint lookupUndecidable(Object obj, int taint) {
 		if (obj == null)
 			return new TaintImmutable(taint);
 		else if (obj instanceof InternalDataStructure)
 			return lookupInternal((InternalDataStructure) obj, taint);
 		else if (obj instanceof Object[])
 			return lookupArrayReference(obj, taint);
 		else if (obj.getClass().isArray())
 			return lookupArrayPrimitive(obj, taint);
 		else
 			return lookupExternal(obj, taint);
 	}
 	
 	public static final TaintArrayPrimitive lookupArrayPrimitive(Object obj, int taint) {
 		if (obj == null)
 			return new TaintArrayPrimitive(obj, 0, taint);
 		
 		TaintArrayPrimitive tobj = (TaintArrayPrimitive) Cache.get(obj);
 		if (tobj == null) {
 			int length;
 			if (obj instanceof int[])
 				length = ((int[]) obj).length;
 			else if (obj instanceof boolean[])
 				length = ((boolean[]) obj).length;
 			else if (obj instanceof byte[])
 				length = ((byte[]) obj).length;
 			else if (obj instanceof char[])
 				length = ((char[]) obj).length;
 			else if (obj instanceof double[])
 				length = ((double[]) obj).length;
 			else if (obj instanceof float[])
 				length = ((float[]) obj).length;
 			else if (obj instanceof long[])
 				length = ((long[]) obj).length;
 			else if (obj instanceof short[])
 				length = ((short[]) obj).length;
 			else  {
 				RuntimeUtils.die("Object is not of a primitive array type");
 				/* will never get executed */ length = 0;
 			}
 
			tobj = new TaintArrayPrimitive(obj, length, taint, taint);
 			Cache.insert(obj, tobj);
 		} else
 			tobj.set(taint);
 		
 		return tobj;
 	}
 	
 	public static final TaintArrayReference lookupArrayReference(Object obj, int taint) {
 		if (obj == null)
 			return new TaintArrayReference(null, taint);
 		
 		TaintArrayReference tobj = (TaintArrayReference) Cache.get(obj);
 		if (tobj == null) {
 			tobj = new TaintArrayReference((Object[]) obj, taint);
 			Cache.insert(obj, tobj);
 		} else
 			tobj.set(taint);
 		
 		return tobj;
 	}
 
 	private static class UndefinedObject {
 		public TaintInternal t_obj;
 		public int t_init;
 		
 		UndefinedObject(TaintInternal t_obj, int t_init) {
 			this.t_obj = t_obj;
 			this.t_init = t_init;
 		}
 	}
 	
 	private static ThreadLocal<UndefinedObject> T_UNDEF;
 	
 	static {
 		T_UNDEF = new ThreadLocal<UndefinedObject>();
 	}
 	
 	public static final void eraseConstructedSuperTaint() {
 		T_UNDEF.set(null);
 	}
 	
 	public static final void setConstructedSuperTaint(TaintInternal t_obj, int t_init) {
 		if (T_UNDEF.get() != null)
 			RuntimeUtils.die("Simultaneous construction of two objects");
 		else
 			T_UNDEF.set(new UndefinedObject(t_obj, t_init));
 	}
 	
 	private static final UndefinedObject getConstructedSuperTaint() {
 		UndefinedObject result = T_UNDEF.get();
 		if (result != null)
 			T_UNDEF.set(null);
 		return result;
 	}
 }
