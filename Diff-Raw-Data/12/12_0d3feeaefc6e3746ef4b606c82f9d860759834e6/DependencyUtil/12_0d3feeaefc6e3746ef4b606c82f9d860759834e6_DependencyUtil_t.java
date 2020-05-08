 package fr.imag.adele.apam.impl;
 
 
 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Dependency;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Resolved;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.declarations.DependencyDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationReference;
 import fr.imag.adele.apam.declarations.SpecificationReference;
 import fr.imag.adele.apam.util.ApamFilter;
 import fr.imag.adele.apam.util.Visible;
 
 public class DependencyUtil {
 
 	private static Logger logger = LoggerFactory.getLogger(DependencyImpl.class);
 
 	/**
 	 * 
 	 * @param impls
 	 * @param insts Set of valid instance. Cannot be empty nor null.
 	 * @param dependency
 	 */
 //	@SuppressWarnings("unchecked") 
 //	public static Instance selectBestInstance (Set<Implementation> impls, Set<Instance> insts, Dependency dep) {
 //		//assert (!insts.isEmpty()) ;
 //		if (insts == null || insts.isEmpty()) 
 //			return null ;
 //		/*
 //		 * No choice, take that instance; and return its implem, even if not visible.
 //		 */
 //		if (insts.size() == 1) {
 //			return insts.iterator().next();
 //		}
 //
 //		if (dep.getImplementationPreferences().isEmpty() 
 //				|| impls==null || impls.isEmpty()) {
 //			return getPrefered(insts, dep) ;
 //		}
 //		/*
 //		 * Try to see if the best implem has existing instances. If not, only consider the instances.
 //		 */
 //		Implementation impl = getPrefered(impls, dep) ;
 //		Set<Instance> selectedInsts = (Set<Instance>)getConstraintsPreferedComponent(impl.getMembers(), dep) ;
 //		//if existing, take an instance of this implem.
 //		if (selectedInsts!= null &&  !selectedInsts.isEmpty()) 
 //			return getPrefered(selectedInsts, dep) ;
 //		else 
 //			return getPrefered(insts, dep) ;
 //	}
 //
 
 	/**
 	 * Return the sub-set of candidates that satisfy all the constraints and preferences.
 	 * Suppose the candidates are of the right kind !
 	 * Visibility is checked is source is provided
 	 * @param <T>
 	 * @param candidates
 	 * @param constraints
 	 * @return
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Resolved<?> getResolved(Component source, Set<? extends Component> candidates, Dependency dep) {
 		if (dep == null) return new Resolved (candidates);
 		if (candidates == null || candidates.isEmpty()) return null ;
 
 		if (candidates.iterator().next().getKind() != dep.getTargetType()) {
 			logger.error ("Invalid type in getResolved") ;
 			return null ;
 		}
 		
 		Set<Component> ret = new HashSet <Component> () ;
 		for (Component c : candidates) {
 			if (Visible.isVisible(source, c) 
 					&& c.matchDependencyConstraints(dep)) {
 				ret.add (c) ;
 			}
 		}
 		
 		if (ret.isEmpty())
 			return null ;
 		
 		if (dep.isMultiple()) 
 			return new Resolved (ret) ;
 
 		//look for preferences
 		return new Resolved (getPrefered (ret, dep)) ; 
 	}
 	
 	public static Resolved<?> getResolved(Component source, Resolved<?> candidates, Dependency dep) {
 		if (candidates.singletonResolved != null) {
 			if (candidates.singletonResolved.matchDependencyConstraints(dep))
 				return candidates ;
 			return null ;
 		}
 		else return getResolved(source, candidates.setResolved, dep) ;
 	}
 
 	
 	/**
 	 * Return the sub-set of candidates that satisfy all the constraints
 	 * @param <T>
 	 * @param candidates
 	 * @param constraints
 	 * @return
 	 */
 	public static <T extends Component> Set<T> getConstraintsComponents(Set<T> candidates, Dependency dep) {
 		if (dep == null) return candidates;
 		if (candidates == null) return null ;
 
 		Set<T> ret = new HashSet <T> () ;
 		for (T c : candidates) {
 			if (c.matchDependencyConstraints(dep)) {
 				ret.add (c) ;
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Return a candidate, selected (by default) among those satisfying the constraints
 	 * @param <T>
 	 * @param candidates
 	 * @param constraints
 	 * @return
 	 */
 	public static  <T extends Component> T getSelectedComponent(Set<T> candidates, Dependency dep) {
 		Set<T> ret = getConstraintsComponents(candidates, dep) ;
 		return getDefaultComponent(ret) ;
 	}
 
 
 	
 //	public static Resolved getSelectedComponents(Set<Component> candidates, Dependency dep) {
 //		if (dep == null) return new Resolved (candidates, null);
 //		if (candidates == null) return null ;
 //
 //		Set<Component> ret = new HashSet <Component> () ;
 //		for (Component c : candidates) {
 //			if (c.matchDependencyConstraints(dep)) {
 //				ret.add (c) ;
 //			}
 //		}
 //		
 //		if (ret.isEmpty())
 //			return null ;
 //		
 //		if (dep.isMultiple()) 
 //			return new Resolved (ret, null) ;
 //
 //		//look for preferences
 //		return new Resolved (null, getPrefered (ret, dep)) ; 
 //	}
 //
 	/**
 	 * Return a candidate, selected (by default) among those satisfying the constraints
 	 * @param <T>
 	 * @param candidates
 	 * @param constraints
 	 * @return
 	 */
 //	public static  <T extends Component> T getSelectedComponent(Set<T> candidates, Dependency dep) {
 //		Set<T> ret = getConstraintsComponents(candidates, dep) ;
 //		return getDefaultComponent(ret) ;
 //	}
 
 	/**
 	 * Return the set (if no preferences), of component matching the constraints, and then selected following the preferences.
 	 * If preferences set, return a single component, otherwise returns all the components matching the constraints.
 	 * Fills in the Insts set, the valid instances.
 	 * @param <T> A component type.
 	 * @param candidates
 	 * @param preferences
 	 * @param constraints
 	 * @return
 	 */
 //	public static <T extends Component> Set<T> getConstraintsPreferedComponent(Set<T> candidates, Dependency dep) {
 //		if (candidates == null || candidates.isEmpty()) return null ;
 //
 //		//Select only those implem with instances matching the constraints		
 //		Set<T> valids = getConstraintsComponents(candidates, dep);
 //
 //		//Valids contains all the valid components
 //		//If no preference return them all.
 //		if ((dep.getImplementationPreferences().isEmpty() && dep.getInstancePreferences().isEmpty())) 
 //			return valids ;
 //
 //		//If preferences return the one and only one prefered
 //		T valid = getPrefered (valids, dep) ;
 //		Set<T> ret = new HashSet<T> () ; 
 //		ret.add(valid) ;
 //		return ret ;
 //	}
 //
 	/**
 	 * Return the candidates that best matches the preferences
 	 * Take the preferences in orden: m candidates
 	 * find  the n candidates that match the constraint.
 	 * 		if n= 0 ignore the constraint
 	 *      if n=1 return it.
 	 * iterate with the n candidates.
 	 * At the end, if n > 1 returns the default one.
 	 *
 	 * @param <T>
 	 * @param candidates
 	 * @param preferences
 	 * @return
 	 */
 	public static  <T extends Component> T getPrefered (Set<T> candidates, Dependency dep) {
 		if (candidates == null || candidates.isEmpty()) return null ;
 		if (candidates.size() == 1) return candidates.iterator().next() ;
 		if (dep == null) {
 			return getDefaultComponent(candidates) ;
 		}
 
 		//TODO check how to do it clean ! if (T ???? Implementation)
 
 		if (candidates.iterator().next() instanceof Implementation) {
 			return getPreferedFilter (candidates, ((DependencyImpl)dep).getImplementationPreferenceFilters()) ;			
 		}
 		return getPreferedFilter (candidates, ((DependencyImpl)dep).getInstancePreferenceFilters()) ;			
 	}	
 
 
 	private static  <T extends Component> T getPreferedFilter (Set<T> candidates, List<ApamFilter> preferences) {
 		if (preferences.isEmpty()) 
 			return  getDefaultComponent(candidates) ;
 
 		Set<T> valids = new HashSet<T> ();
 		for (ApamFilter f : preferences) {
 			for (T compo : candidates) {
 				if (compo.match(f))
 					valids.add (compo) ;
 			}
 
 			//If a single one satisfies, it is the prefered one.
 			if (valids.size()==1) return valids.iterator().next();
 
 			//If nobody satisfies the contraints check next constraint with same set of candidates
 			if (valids.isEmpty()) break ;
 
 			//continue with those that satisfy the constraint
 			candidates = valids ;
 			valids=new HashSet<T> () ;
 		}
 
 		//More than one candidate are still here: return the default one.
 		return getDefaultComponent(candidates) ;
 	}	
 
 	/**
 	 * Return the "best" component among the candidates. 
 	 * Best depends on the component nature. 
 	 * For implems, it is those that have sharable instance or that is instantiable.
 	 * @param <T>
 	 * @param candidates
 	 * @return
 	 */
 	private static <T extends Component> T getDefaultComponent (Set<T> candidates) {
 		if (candidates == null || candidates.isEmpty()) return null ;
 		if (!(candidates.iterator().next() instanceof Implementation)) 
 			return candidates.iterator().next() ;
 
 		for (T impl : candidates) {
 			if (impl.isInstantiable())
 				return impl;
 			for (Component inst : impl.getMembers()) {
 				if (((Instance)inst).isSharable())
 					return impl;
 			}
 		}
 		return candidates.iterator().next();
 	}
 	
 	/**
 	 * Provided a dependency declaration, compute the corresponding dependency, adding group constraint and flags.
 	 * It is supposed to be correct !! No failure expected
 	 * 
 	 * Does not add those dependencies defined "above" nor the composite ones.
 	 * 
 	 * @param depComponent
 	 * @param dependency
 	 * @return
 	 * 
 	 */
 	public static Map<String, Dependency> initializeDependencies (Component client) {
 		Map<String, Dependency> dependencies = new HashMap<String, Dependency> ();
 		for (DependencyDeclaration dependency : client.getDeclaration().getDependencies() ) {
 			Component group = client.getGroup() ;
 			//look for that dependency declaration above
 			DependencyDeclaration groupDep = null ;
 			while (group != null && (groupDep == null)) {
 				groupDep = group.getDeclaration().getDependency(dependency.getIdentifier()) ;
 				group = group.getGroup() ;
 			}
 
 			if (groupDep != null) {
 				//it is declared above. Merge and check.
 				//First merge flags, and then constraints.
 				DependencyUtil.overrideDepFlags (dependency, groupDep, false);
 				dependency.getImplementationConstraints().addAll(groupDep.getImplementationConstraints()) ;
 				dependency.getInstanceConstraints().addAll(groupDep.getInstanceConstraints()) ;
 				dependency.getImplementationPreferences().addAll(groupDep.getImplementationPreferences()) ;
 				dependency.getInstancePreferences().addAll(groupDep.getInstancePreferences()) ;		
 
 				//It is supposed that the compilation checked that the targets are compatible 
 				//dependency.setTarget(groupDep.getTarget()) ;
 			} 
 
 			//Add the override dependency : flags and constraints. Only for source instances
 			else if (client instanceof Instance) {
 				List<DependencyDeclaration> overDeps = ((Instance)client).getComposite().getCompType().getCompoDeclaration().getOverridenDependencies() ;
 				if (overDeps != null && ! overDeps.isEmpty()) {
 					for ( DependencyDeclaration  overDep  : overDeps) {
 						if (matchOverrideDependency((Instance)client, overDep, dependency)) {
 							overrideDepFlags (dependency, overDep, true) ;
 							//It is assumed that the filters have been checked at compile time (checkObr)
 							dependency.getImplementationConstraints().addAll(overDep.getImplementationConstraints()) ;
 							dependency.getInstanceConstraints().addAll(overDep.getInstanceConstraints()) ;
 							dependency.getImplementationPreferences().addAll(overDep.getImplementationPreferences()) ;
 							dependency.getInstancePreferences().addAll(overDep.getInstancePreferences()) ;
 						}
 					}
 				}
 			}
 
 			// Build the corresponding Dependency
 			dependencies.put(dependency.getIdentifier(), new DependencyImpl (dependency, client)) ;
 		}
 		return dependencies ;
 	}
 
 	
 
 	/**
 	 * Provided a client instance, checks if its dependency "clientDep", matches another dependency: "compoDep".
 	 *
 	 * matches only based on same name (same resource or same component).
 	 * If client cardinality is multiple, compo cardinallity must be multiple too.
 	 * No provision for the client constraints or characteristics (missing, eager)
 	 *
 	 * @param compoInst the composite instance containing the client
 	 * @param compoDep the dependency that matches or not
 	 * @param clientDep the client dependency we are trying to resolve
 	 * @return
 	 */
 	public static boolean matchDependency(Instance compoInst, Dependency compoDep, Dependency clientDep) {
 		if (compoDep == null || clientDep == null)
 			return false ;
 
 		//Look for same dependency: the same specification, the same implementation or same resource name
 		//Constraints are not taken into account
 		boolean multiple = clientDep.isMultiple();
 		
 		// if same nature (spec, implem, internface ... make a direct comparison.
 		if (compoDep.getTarget().getClass().equals(clientDep.getTarget().getClass())) { 
 			if (compoDep.getTarget().equals(clientDep.getTarget())) {
 				if (!multiple || compoDep.isMultiple()) {
 					return true;
 				}
 			}
 		}
 
 		//Look for a compatible dependency.
 		//Stop at the first dependency matching only based on same name (same resource or same component)
 		//No provision for : cardinality, constraints or characteristics (missing, eager)
 
 		//Look if the client requires one of the resources provided by the specification
 		if (compoDep.getTarget() instanceof SpecificationReference) {
 			Specification spec = CST.apamResolver.findSpecByName(compoInst,
 					((SpecificationReference) compoDep.getTarget()).getName());
 			if ((spec != null) && spec.getDeclaration().getProvidedResources().contains(clientDep.getTarget())
 					&& (!multiple || compoDep.isMultiple())) {
 				return true;
 			}
 		} 
 
 		//If the composite has a dependency toward an implementation
 		//and the client requires a resource provided by that implementation
 		else {
 			if (compoDep.getTarget() instanceof ImplementationReference) {
 				String implName = ((ImplementationReference<?>) compoDep.getTarget()).getName();
 				Implementation impl = CST.apamResolver.findImplByName(compoInst, implName);
 				if (impl != null) {
 					//The client requires the specification implemented by that implementation
 					if (clientDep.getTarget() instanceof SpecificationReference) {
 						String clientReqSpec = ((SpecificationReference) clientDep.getTarget()).getName();
 						if (impl.getImplDeclaration().getSpecification().getName().equals(clientReqSpec)
 								&& (!multiple || compoDep.isMultiple())) {
 							return true;
 						}
 					} else {
 						//The client requires a resource provided by that implementation
 						if (impl.getImplDeclaration().getProvidedResources().contains(clientDep.getTarget())
 								&& (!multiple || compoDep.isMultiple())) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 
 	/**
 	 * Provided a source component, checks if the provided override dependency 
 	 * matches the compoClient dependency declaration.
 	 * Tags source and sourceType are mandatory. 
 	 * To be applied on a component C, the override must be such that :
 	 * 		id matches the override id
 	 *      source must be the name of C or of an ancestor of C.
 	 * 		target must be the same type (resource of component, and its name must match). 
 	 *
 	 * @param source the source of the relation
 	 * @param overDep the dependencies of the composite: a regExpression
 	 * @param sourceDep the client dependency we are trying to resolve.
 	 * @return
 	 */
 	private static boolean matchOverrideDependency(Component source, DependencyDeclaration overDep, DependencyDeclaration sourceDep) {
 
 		//Check if Ids are compatible
 		if (! sourceDep.getIdentifier().matches(overDep.getIdentifier()))
 			return false ;
 		
 		//Check if overDep source is source or one of its ancestors
 		Component group = source ;
 		boolean found = false ;
 		while (group != null) {
 			if (sourceDep.getSource().getName().matches(overDep.getSource().getName())) {
 				found = true;
 				break ;
 			}
 		}
 		
 		if (!found) 
 			return false ;
 		
 		/*
 		 * Check if targets are compatible
 		 * Same target: the same specification, the same implementation or same resource name with a matching
 		 */
 		String pattern = overDep.getTarget().getName() ;
 		// same nature: direct comparison
 		if (overDep.getTarget().getClass().equals(sourceDep.getTarget().getClass())
 				&& (sourceDep.getTarget().getName().matches(pattern))) {
 			return true;
 		}
 		return false;
 	}
 
 //		//If the client dep is an implementation dependency, check if the specification matches the pattern
 //		if (overDep.getTarget() instanceof SpecificationReference
 //				&& sourceDep.getTarget() instanceof ImplementationReference) {
 //			String implName = ((ImplementationReference<?>) sourceDep.getTarget()).getName();
 //			Implementation impl = CST.apamResolver.findImplByName(source, implName);
 //			if (impl != null && impl.getSpec().getName().matches(pattern)) {
 //				return true ;
 //			}
 //		}
 
 
 	/**
 	 * A dependency may have properties fail= null, wait, exception; exception = null, exception
 	 * A contextual dependency can also have hide: null, true, false and eager: null, true, false
 	 * The most local definition overrides the others.
 	 * Exception null can be overriden by an exception; only generic exception overrides another non null one.
 	 *
 	 * @param dependency : the low level one that will be changed if needed
 	 * @param dep: the group dependency
 	 * @param generic: the dep comes from the composite type. It can override the exception, and has hidden and eager.
 	 * @return
 	 */
	public static void overrideDepFlags (DependencyDeclaration dependency, DependencyDeclaration dep, boolean generic) {
 		//If set, cannot be changed by the group definition.
 		//NOTE: This strategy is because it cannot be compiled, and we do not want to make an error during resolution
 		if (dependency.getMissingPolicy() == null || (generic && dep.getMissingPolicy() != null)) {
 			dependency.setMissingPolicy(dep.getMissingPolicy()) ;
 		}
 
 		if (dependency.getMissingException() == null || (generic && dep.getMissingException() != null)) {
 			dependency.setMissingException(dep.getMissingException()) ;
 		}
 
 		if (generic) {
 			dependency.setHide(dep.isHide()) ;
 			dependency.setEager(dep.isEager()) ;
 		}
 	}
 }
