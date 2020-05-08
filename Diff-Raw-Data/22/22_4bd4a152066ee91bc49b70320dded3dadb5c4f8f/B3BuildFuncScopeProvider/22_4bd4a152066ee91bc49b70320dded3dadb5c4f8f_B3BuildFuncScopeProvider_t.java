 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.build.evaluator;
 
 import java.util.ArrayList;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import org.eclipse.b3.backend.evaluator.b3backend.IFunction;
 import org.eclipse.b3.backend.scoping.B3BackendFuncScopeProvider;
 import org.eclipse.b3.build.BeeModel;
 import org.eclipse.b3.build.core.B3BuildConstants;
 import org.eclipse.b3.build.engine.B3BuildEngineResource;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.xtext.resource.EObjectDescription;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 
 /**
  * Function Scope Provider for B3 Build
  * 
  */
 public class B3BuildFuncScopeProvider extends B3BackendFuncScopeProvider {
 	public static class Unfiltered extends B3BuildFuncScopeProvider {
 		public Unfiltered() {
 			setIncludeOperators(true);
 		}
 	}
 
 	private boolean includeOperators = false;
 
 	private Pattern pID = null;
 
	public boolean isIncludeOperators() {
		return includeOperators;
	}

	public void setIncludeOperators(boolean flag) {
		includeOperators = flag;
	}

 	IScope funcScope(B3BuildEngineResource r) {
 		ArrayList<IEObjectDescription> result = new ArrayList<IEObjectDescription>();
 		// filter out functions that can not be called directly (i.e. '+' '-' etc).
 		//
 		if(pID == null)
 			pID = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");
 		for(Entry<String, IFunction> entry : r.getFunctions().entries()) {
 			if(isIncludeOperators() || pID.matcher(entry.getKey()).matches())
 				result.add(new EObjectDescription(entry.getKey(), entry.getValue(), null));
 		}
 		if(result.size() < 1)
 			return null; // did not find any functions with ID names, and there are no more outer scopes
 		return createScope(null, result);
 	}
 
 	IScope funcScope(BeeModel container) {
 		ArrayList<IEObjectDescription> result = new ArrayList<IEObjectDescription>();
 
 		for(IFunction func : container.getFunctions())
 			result.add(new EObjectDescription(func.getName(), func, null));
 
		// if(result.size() < 1)
		// return doGetFuncScope(container.eContainer());
 
 		URI uri = URI.createURI(B3BuildConstants.B3ENGINE_MODEL_URI);
 		B3BuildEngineResource r = (B3BuildEngineResource) container.eResource().getResourceSet().getResource(uri, false);
 
 		return createScope(doGetFuncScope(r), result);
 	}
 
 }
