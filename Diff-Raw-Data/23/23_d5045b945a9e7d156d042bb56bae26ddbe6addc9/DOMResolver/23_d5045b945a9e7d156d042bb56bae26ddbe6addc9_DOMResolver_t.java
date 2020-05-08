 package org.eclipse.dlkt.javascript.dom.support.internal;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.dlkt.javascript.dom.support.IDesignTimeDOMProvider;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.IReferenceResolver;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.IResolvableReference;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.eclipse.dltk.internal.javascript.typeinference.AbstractCallResultReference;
 import org.eclipse.dltk.internal.javascript.typeinference.IClassReference;
 import org.eclipse.dltk.internal.javascript.typeinference.IReference;
 import org.eclipse.dltk.internal.javascript.typeinference.NewReference;
 import org.eclipse.dltk.internal.javascript.typeinference.UncknownReference;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 public class DOMResolver implements IReferenceResolver, IExecutableExtension {
 
 	final static class ClassReference extends UncknownReference implements
 			IClassReference {
 		private ClassReference(String paramOrVarName, boolean childIsh) {
 			super(paramOrVarName, childIsh);
 		}
 	}
 
 	public Set getChilds(IResolvableReference ref) {
 		HashMap classRef = getClassMap();
 		HashSet result = new HashSet();
 		if (ref instanceof AbstractCallResultReference) {
 			final AbstractCallResultReference cm = (AbstractCallResultReference) ref;
 			if (cm instanceof NewReference) {
 				Class clzz = (Class) classRef.get(cm.getId());
 				if (clzz != null) {
 					Method[] methods = clzz.getMethods();
 					for (int a = 0; a < methods.length; a++) {
 						String string = "jsFunction_";
 						String stringget = "jsGet_";
 						String stringset = "jsSet_";
 						Method method = methods[a];
 						if (method.getName().startsWith(string)) {
 							UncknownReference r = new UncknownReference(method
 									.getName().substring(string.length()), true);
 
 							result.add(r);
 							r.setFunctionRef();
 						} else if (method.getName().startsWith(stringget)) {
 							IReference r = new UncknownReference(method
 									.getName().substring(stringget.length()),
 									true);
 							result.add(r);
 						} else if (method.getName().startsWith(stringset)) {
 							IReference r = new UncknownReference(method
 									.getName().substring(stringset.length()),
 									true);
 							result.add(r);
 						}
 					}
 				}
 				Object obj = getGlobalMap().get(cm.getId());
 				if (obj instanceof ScriptableObject) {
 					ScriptableObject sc = (ScriptableObject) obj;
 					if (sc instanceof Function) {
 						Function f = (Function) sc;
 						Scriptable construct = f.construct(Context
 								.getCurrentContext(), sc, new Object[0]);
 						if (construct instanceof ScriptableObject) {
 							ScriptableObject sm = (ScriptableObject) construct;
 							HashMap map = new HashMap();
 							fillMap(map, sm);
 							createReferences("", map, result);
 						}
 					}
 
 				}
 			}
 		}
 		return result;
 	}
 
 	IDesignTimeDOMProvider[] providers;
 	ISourceModule module;
 
 	public HashMap getClassMap() {
 		if (classRef != null) {
 			HashMap object = (HashMap) classRef.get();
 			if (object != null)
 				return object;
 		}
 		HashMap mp = new HashMap();
 
 		for (int a = 0; a < providers.length; a++) {
 			if (providers[a].canResolve(module)) {
 				Class[] resolveHostObjectClasses = providers[a]
 						.resolveHostObjectClasses(module);
 				if (resolveHostObjectClasses == null)
 					continue;
 				for (int b = 0; b < resolveHostObjectClasses.length; b++) {
 					Class class1 = resolveHostObjectClasses[b];
 
 					try {
 						ScriptableObject dc = (ScriptableObject) class1
 								.newInstance();
 						mp.put(dc.getClassName(), class1);
 					} catch (InstantiationException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 					// mp.put(class1.getSimpleName(), class1);
 				}
 			}
 		}
 		classRef = new WeakReference(mp);
 		return mp;
 	}
 
 	WeakReference classRef;
 	WeakReference scopeRef;
 
 	public HashMap getGlobalMap() {
 		if (scopeRef != null) {
 			HashMap object = (HashMap) scopeRef.get();
 			if (object != null)
 				return object;
 		}
 		HashMap mp = new HashMap();
 		for (int a = 0; a < providers.length; a++) {
 			if (providers[a].canResolve(module)) {
 				ScriptableObject resolveTopLevelScope = providers[a]
 						.resolveTopLevelScope(module);
 				if (resolveTopLevelScope == null)
 					continue;
 				fillMap(mp, resolveTopLevelScope);
 			}
 		}
 		scopeRef = new WeakReference(mp);
 		return mp;
 	}
 
	private Object[] fillMap(HashMap mp, Scriptable resolveTopLevelScope) {
 		Scriptable prototype = resolveTopLevelScope.getPrototype();
 		if (prototype != null) {
			if (prototype instanceof Scriptable) {
				fillMap(mp, prototype);
 			}
 		}
		Object[] allIds = null;
		if (resolveTopLevelScope instanceof ScriptableObject && false)
		{
			allIds = ((ScriptableObject)resolveTopLevelScope).getAllIds();
		}
		else
		{
			allIds = resolveTopLevelScope.getIds();
		}
 		for (int b = 0; b < allIds.length; b++) {
 			String key = allIds[b].toString();
 			try {
 				Object object = resolveTopLevelScope.get(key,
 						resolveTopLevelScope);
 				mp.put(key, object);
 			} catch (Throwable e) {
 
 			}
 
 		}
 
 		return allIds;
 	}
 
 	public void init(ReferenceResolverContext owner) {
 		this.module = owner.getModule();
 		providers = DomResolverSupport.getProviders();
 
 	}
 
 	public void processCall(String call, String objId) {
 
 	}
 
 	public Set resolveGlobals(String id) {
 		int pos = id.indexOf('.');
 		String key = pos == -1 ? id : id.substring(0, pos);
 
 		HashMap globals = getGlobalMap();
 		HashMap clss = getClassMap();
 		HashSet rs = new HashSet();
 
 		if (pos == -1) {
 			Iterator iterator;
 			createReferences(key, globals, rs);
 			iterator = clss.keySet().iterator();
 			while (iterator.hasNext()) {
 				String s = (String) iterator.next();
 				if (s.startsWith(key)) {
 					UncknownReference uref = new ClassReference(s, false);
 					rs.add(uref);
 				}
 			}
 		} else {
 			while (pos != -1) {
 				Object object = globals.get(key);
				if (object instanceof Scriptable) {
					Scriptable sc = (Scriptable) object;
 					globals.clear();
 					fillMap(globals, sc);
 				}
 				id = id.substring(pos + 1);
 				pos = id.indexOf('.');
 				key = pos == -1 ? id : id.substring(0, pos);
 			}
 			createReferences(key, globals, rs);
 		}
 		return rs;
 	}
 
 	private void createReferences(String key, HashMap globals, HashSet rs) {
 		Iterator iterator = globals.keySet().iterator();
 		while (iterator.hasNext()) {
 			String s = (String) iterator.next();
 
 			if (s.startsWith(key)) {
 				UncknownReference uref = new UncknownReference(s, false);
 				Object object = globals.get(s);
 				if (object instanceof Function) {
 					uref.setFunctionRef();
 				}
 				rs.add(uref);
 			}
 		}
 	}
 
 	public boolean canResolve(ISourceModule module) {
 		return true;
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 	}
 }
