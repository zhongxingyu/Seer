 package org.eclipse.dlkt.javascript.dom.support.internal;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Method;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.dlkt.javascript.dom.support.IDesignTimeDOMProvider;
 import org.eclipse.dlkt.javascript.dom.support.IProposalHolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.IReferenceResolver;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.IResolvableReference;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.eclipse.dltk.internal.javascript.typeinference.AbstractCallResultReference;
 import org.eclipse.dltk.internal.javascript.typeinference.HostCollection;
 import org.eclipse.dltk.internal.javascript.typeinference.IClassReference;
 import org.eclipse.dltk.internal.javascript.typeinference.IReference;
 import org.eclipse.dltk.internal.javascript.typeinference.ReferenceFactory;
 import org.eclipse.dltk.internal.javascript.typeinference.UnknownReference;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.MemberBox;
 import org.mozilla.javascript.NativeJavaMethod;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 public class DOMResolver implements IReferenceResolver, IExecutableExtension {
 
 	final static class ClassReference extends UnknownReference implements
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
 			// if (cm instanceof NewReference) {
 			Class clzz = (Class) classRef.get(cm.getId());
 			if (clzz != null) {
 				Method[] methods = clzz.getMethods();
 				for (int a = 0; a < methods.length; a++) {
 					String string = "jsFunction_";
 					String stringget = "jsGet_";
 					String stringset = "jsSet_";
 					Method method = methods[a];
 					if (method.getName().startsWith(string)) {
 						UnknownReference r = new UnknownReference(method
 								.getName().substring(string.length()), true);
 
 						result.add(r);
 						r.setFunctionRef();
 					} else if (method.getName().startsWith(stringget)) {
 						IReference r = new UnknownReference(method.getName()
 								.substring(stringget.length()), true);
 						result.add(r);
 					} else if (method.getName().startsWith(stringset)) {
 						IReference r = new UnknownReference(method.getName()
 								.substring(stringset.length()), true);
 						result.add(r);
 					}
 				}
 			}
 			Set resolveGlobals = resolveGlobals(cm.getId() + ".");
 
 			result.addAll(resolveGlobals);
 			// Object obj = getGlobalMap().get(cm.getId());
 			// if (obj instanceof ScriptableObject) {
 			// ScriptableObject sc = (ScriptableObject) obj;
 			// if (sc instanceof Function) {
 			// Function f = (Function) sc;
 			// Scriptable construct = f.construct(Context
 			// .getCurrentContext(), sc, new Object[0]);
 			// if (construct instanceof ScriptableObject) {
 			// ScriptableObject sm = (ScriptableObject) construct;
 			// HashMap map = new HashMap();
 			// fillMap(map, sm, false);
 			// createReferences("", map, result);
 			// }
 			// }
 			//
 			// }
 			// }
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
 
 	private HashMap getGlobalMap(String key) {
 		HashMap mp = new HashMap();
 		for (int a = 0; a < providers.length; a++) {
 			if (providers[a].canResolve(module)) {
 				Scriptable resolveTopLevelScope = providers[a]
 						.resolveTopLevelScope(module);
 				if (resolveTopLevelScope == null)
 					continue;
 				fillMap(mp, resolveTopLevelScope, true, key);
 			}
 		}
 		return mp;
 	}
 
 	void fillMap(HashMap mp, Scriptable scope, boolean walkParent,
 			String idToFind) {
 		Scriptable prototype = scope.getPrototype();
 		if (prototype != null) {
 			fillMap(mp, prototype, walkParent, idToFind);
 			if (idToFind != null && mp.size() == 1)
 				return;
 		}
 		if (walkParent) {
 			Scriptable parentScope = scope.getParentScope();
 			if (parentScope != null) {
 				fillMap(mp, parentScope, walkParent, idToFind);
 				if (idToFind != null && mp.size() == 1)
 					return;
 			}
 		}
 		Object[] allIds = null;
 		for (int a = 0; a < providers.length; a++) {
 			if (providers[a].canResolve(module)) {
 				allIds = providers[a].resolveIds(scope, idToFind);
 				if (allIds != null)
 					break;
 			}
 		}
 		if (allIds == null) {
 			if (scope instanceof ScriptableObject) {
 				allIds = ((ScriptableObject) scope).getAllIds();
 			} else {
 				allIds = scope.getIds();
 			}
 		}
 		for (int b = 0; b < allIds.length; b++) {
 			String key = allIds[b].toString();
 			if (idToFind != null && !idToFind.equals(key))
 				continue;
 			try {
 				Object object = null;
 				for (int a = 0; a < providers.length; a++) {
 					if (providers[a].canResolve(module)) {
 						object = providers[a].getProposal(scope, key);
 						if (object != null)
 							break;
 					}
 				}
 				if (object == null) {
 					object = scope.get(key, scope);
 				}
 				mp.put(key, object);
 				if (idToFind != null)
 					break;
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		return;
 	}
 
 	public void init(ReferenceResolverContext owner) {
 		this.module = owner.getModule();
 		providers = DomResolverSupport.getProviders();
 
 	}
 
 	public void processCall(String call, String objId) {
 
 	}
 
 	public Set resolveGlobals(String completion) {
 		completion = HostCollection.parseCompletionString(completion, true);
 		int pos = completion.indexOf('.');
 		String key = pos == -1 ? completion : completion.substring(0, pos);
 
 		HashMap globals = getGlobalMap(pos == -1 ? null : key);
 		HashMap clss = getClassMap();
 		HashSet rs = new HashSet();
 
 		if (pos == -1) {
 			Iterator iterator;
 			createReferences(key, globals, rs);
 			iterator = clss.keySet().iterator();
 			while (iterator.hasNext()) {
 				String s = (String) iterator.next();
 				if (s.startsWith(key)) {
 					UnknownReference uref = new ClassReference(s, false);
 					rs.add(uref);
 				}
 			}
 		} else {
 			while (pos != -1) {
 				Object object = globals.get(key);
 				if (object instanceof IProposalHolder) {
 					object = ((IProposalHolder) object).getObject();
 				}
 				object = getObjectReferenceScope(key, object);
 				if (object instanceof Scriptable) {
 					completion = completion.substring(pos + 1);
 					pos = completion.indexOf('.');
 					key = pos == -1 ? completion : completion.substring(0, pos);
 					Scriptable sc = (Scriptable) object;
 					globals = new HashMap();
 					fillMap(globals, sc, false,
 							(pos == -1 && !"[]".equals(key)) ? null : key);
 				} else {
 					// not at match at all clear it
 					globals = new HashMap();
 					break;
 				}
 			}
 			createReferences(key, globals, rs);
 		}
 		return rs;
 	}
 
 	void createReferences(String key, HashMap globals, HashSet rs) {
 		Iterator iterator = globals.keySet().iterator();
 		while (iterator.hasNext()) {
 			String s = (String) iterator.next();
 
 			if (s.startsWith(key)) {
 				Object object = globals.get(s);
 				UnknownReference uref = null;
 				if (object instanceof UnknownReferenceScope) {
 					uref = ((UnknownReferenceScope) object).getReference();
 				} else if (object instanceof Scriptable) {
 					uref = new ScriptableScopeReference(s, (Scriptable) object,
 							this);
 					if (object instanceof IProposalHolder) {
 						IProposalHolder fapn = (IProposalHolder) object;
 						uref.setParameterNames(fapn.getParameterNames());
 						uref.setProposalInfo(fapn.getProposalInfo());
 						uref.setImageUrl(fapn.getImageURL());
 					}
 				} else if (object instanceof IProposalHolder) {
 					IProposalHolder fapn = (IProposalHolder) object;
 					object = getObjectReferenceScope(s,
 							((IProposalHolder) object).getObject());
 					if (object instanceof UnknownReferenceScope) {
 						uref = ((UnknownReferenceScope) object).getReference();
 					} else {
 						uref = new UnknownReference(s, false);
 					}
 					uref.setParameterNames(fapn.getParameterNames());
 					uref.setProposalInfo(fapn.getProposalInfo());
 					uref.setImageUrl(fapn.getImageURL());
 					object = fapn.getObject();
 					if (fapn.isFunctionRef())
 						uref.setFunctionRef();
 				} else {
 					uref = new UnknownReference(s, false);
 				}
 
 				if (object instanceof Function) {
 					uref.setFunctionRef();
 				}
 				if (module instanceof ModelElement) {
 					uref.setLocationInformation((ModelElement) module, uref
 							.getOffset(), uref.getLength());
 				}
 				rs.add(uref);
 			}
 		}
 	}
 
 	/**
 	 * @param s
 	 * @param object
 	 * @return
 	 */
 	private Object getObjectReferenceScope(String s, Object object) {
 		if (object == null)
 			return null;
 		UnknownReference uref = null;
 		Class clz = object.getClass();
 		if (object instanceof NativeJavaObject) {
 			clz = ((NativeJavaObject) object).unwrap().getClass();
 		} else if (object instanceof NativeJavaMethod) {
 			MemberBox[] methods = ((NativeJavaMethod) object).getMethods();
 			for (int i = 0; i < methods.length; i++) {
 				if (methods[i].getReturnType() != null) {
 					clz = methods[i].getReturnType();
 					break;
 				}
 			}
 		}
 		if (clz == String.class) {
 			uref = ReferenceFactory.createStringReference(s);
 		} else if (clz == Boolean.class || clz == boolean.class) {
 			uref = ReferenceFactory.createBooleanReference(s);
 		} else if (Date.class.isAssignableFrom(clz)) {
 			uref = ReferenceFactory.createDateReference(s);
 			// booleans are just above.
 		} else if (Number.class.isAssignableFrom(clz) || clz.isPrimitive()) {
 			uref = ReferenceFactory.createNumberReference(s);
 		} else if ((clz).isArray()) {
 			uref = ReferenceFactory.createArrayReference(s);
 		}
 		if (uref != null)
 			return new UnknownReferenceScope(uref);
 		return object;
 	}
 
 	public boolean canResolve(ISourceModule module) {
 		return true;
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 	}
 }
