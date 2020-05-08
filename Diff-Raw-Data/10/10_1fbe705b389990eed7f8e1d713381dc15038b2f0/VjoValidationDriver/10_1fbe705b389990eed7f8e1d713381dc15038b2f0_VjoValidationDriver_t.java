 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsgen.shared.validation.vjo;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.validator.VjoSemanticValidatorRepo;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.util.JstGlobalScopeUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.VjoValidationVisitor;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.IScriptProblem;
 import org.eclipse.vjet.dsf.jst.IScriptUnit;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.ts.ITypeSpace;
 import org.eclipse.vjet.dsf.ts.group.IGroup;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 
 import org.eclipse.vjet.dsf.logger.Logger;
 
 /**
  * 
  * 
  * <p>
  * this is the primary entry class for vjet validation
  * @see VjoValidationDriver#validateComplete(List, String)
  * </p>
  */
 public class VjoValidationDriver {
 	
 	/**
 	 * mode helps to decide if the validation should be done in ede environment or test case environment
 	 */
 	public static enum VjoValidationMode {
 		validateTypeSpace,
 		validateType
 	}
 	
 	private static Logger s_logger = null;
 	
 	private JstTypeSpaceMgr m_jstTypeSpaceMgr;
 	
 	public void setTypeSpaceMgr(JstTypeSpaceMgr typeSpaceMgr){
 		m_jstTypeSpaceMgr = typeSpaceMgr;
 	}
 	
 	public JstTypeSpaceMgr getJstTypeSpaceMgr(){
 		return m_jstTypeSpaceMgr;
 	}
 	
 	private boolean m_hasInternalErrors;
 	
 	public boolean hasInternalErrors(){
 		return m_hasInternalErrors;
 	}
 	
 	private Logger getLogger(){
 		if(s_logger==null){
 			s_logger = Logger.getInstance(VjoValidationDriver.class);
 		}
 		return s_logger;
 	}
 	
 	
 	public VjoValidationResult validate(List<IJstType> entryJstTypes, String groupId) {
 		
 		//copy the dependencies to the ctx
 		final VjoValidationResult result = new VjoValidationResult();
 		final VjoValidationCtx ctx = new VjoValidationCtx();
 		//moved validation ctx & visitor into the for loop
 		initValidationCtx(ctx, groupId, VjoValidationMode.validateTypeSpace);
 		
 		//depth 1st traverse the dependency tree
 		for(IJstType jstType : entryJstTypes){
 			try{
 				walkThrough(jstType, null, null, ctx, groupId, null, VjoValidationMode.validateTypeSpace);
 			}
 			catch(VjoValidationRuntimeException ex){
 				ex.printStackTrace();	//KEEPME
 				getLogger().log(ex);
 				//stop on error
 				break;
 			}
 			catch(Throwable th){
 				th.printStackTrace();	//KEEPME
 				getLogger().log(th);
 				m_hasInternalErrors = true;
 			}
 			finally{
 				result.addResult(jstType, ctx.getAllProblems());
 			}
 		}
 		
 		return result;
 	}
 	
 	public VjoValidationResult validateComplete(Map<String, IScriptUnit> entryJstTypes, String groupId){
 		return validateComplete(entryJstTypes, groupId, VjoValidationMode.validateTypeSpace);
 	}
 	
 	public VjoValidationResult validateComplete(Map<String, IScriptUnit> entryJstTypes, String groupId, VjoValidationMode mode){
 		final Map<String, IJstType> toValidate = new LinkedHashMap<String, IJstType>();
 
 		for(Map.Entry<String, IScriptUnit> entry : entryJstTypes.entrySet()){
 			toValidate.put(entry.getKey(), entry.getValue().getType());
 		}
 		
 		//copy the dependencies to the ctx
 		final VjoValidationResult result = new VjoValidationResult();
 
 		final VjoValidationCtx ctx = new VjoValidationCtx();
 		initValidationCtx(ctx, groupId, mode);
 		
 		//depth 1st traverse the dependency tree
 		for(Map.Entry<String, IJstType> typeEntry : toValidate.entrySet()){
 			final String uniquePath = typeEntry.getKey();
 			final IScriptUnit scriptUnit = entryJstTypes.get(uniquePath);
 			IJstType jstType2Validate = typeEntry.getValue();
 			
 			try{
 				//skip validations as there're syntax errors
 				walkThrough(jstType2Validate, scriptUnit.getJstBlockList(), scriptUnit.getProblems(), ctx, groupId, uniquePath, mode);
 			}
 			catch(VjoValidationRuntimeException ex){
 				ex.printStackTrace(); //KEEPME
 				getLogger().log(ex);
 				//stop on error
 				break;
 			}
 			catch(Throwable th){
 				th.printStackTrace(); //KEEPME
 				getLogger().log(th);
 				m_hasInternalErrors = true;
 			}
 			finally{
 				result.addResult(jstType2Validate, ctx.getAllProblems());
 				ctx.resetProblems();
 			}
 		}
 		return result;
 	}
 	
 	public VjoValidationResult validateComplete(List<IScriptUnit> entryJstTypes, String groupId) {
 		return validateComplete(entryJstTypes, groupId, VjoValidationMode.validateTypeSpace);
 	}
 	
 	/**
 	 * <p>
 	 * </p>
 	 * @param entryJstTypes
 	 * @param groupId
 	 * @param mode
 	 * @return
 	 */
 	public VjoValidationResult validateComplete(List<IScriptUnit> entryJstTypes, String groupId, VjoValidationMode mode) {
 		final Map<IJstType, IScriptUnit> type2UnitMap = new HashMap<IJstType, IScriptUnit>();
 		final List<IJstType> toValidate = new ArrayList<IJstType>();
 
 		for(IScriptUnit unit : entryJstTypes){
 			type2UnitMap.put(unit.getType(), unit);
 			toValidate.add(unit.getType());
 		}
 		
 		//copy the dependencies to the ctx
 		final VjoValidationResult result = new VjoValidationResult();
 
 		final VjoValidationCtx ctx = new VjoValidationCtx();
 		initValidationCtx(ctx, groupId, mode);
 		
 		//depth 1st traverse the dependency tree
 		for(IJstType jstType : toValidate){
 			final IScriptUnit scriptUnit = type2UnitMap.get(jstType);
 			try{
 				//skip validations as there're syntax errors
 				walkThrough(jstType, scriptUnit.getJstBlockList(), scriptUnit.getProblems(), ctx, groupId, null, mode);
 			}
 			catch(VjoValidationRuntimeException ex){
 //				ex.printStackTrace(); //KEEPME
 				//stop on error
 				getLogger().log(ex);
 				break;
 			}
 			catch(Throwable th){
 				th.printStackTrace();	//KEEPME
 				getLogger().log(th);
 				m_hasInternalErrors = true;
 			}
 			finally{
 				result.addResult(jstType, ctx.getAllProblems());
 				ctx.resetProblems();
 			}
 		}
 		return result;
 	}
 	
 	public VjoValidationResult validate(List<IJstType> entryJstTypes) {
 		//bugfix, group name inferred from entry types
 		if(entryJstTypes.size() > 0){
 			final IJstType candidateType = entryJstTypes.iterator().next();
 			String groupName = "";
 			if(candidateType != null
 					&& candidateType.getPackage() != null
 					&& candidateType.getPackage().getGroupName() != null){
 				groupName = candidateType.getPackage().getGroupName();
 			}
 			return validate(entryJstTypes, groupName);
 		}
 		else{
 			return new VjoValidationResult();
 		}
 	}
 	
 	private void walkThrough(IJstType jstType,
 			final List<? extends IJstNode> syntaxRoots,
 			final List<IScriptProblem> problems,
 			final VjoValidationCtx ctx,
 			final String groupId,
 			final String uniquePath,
 			final VjoValidationMode mode){
 		final Set<IJstType> dependencies = calculateDependencies(ctx, jstType, groupId, mode);
 		
 		//moved validation ctx & visitor into the for loop
 		final VjoValidationVisitor visitor = new VjoValidationVisitor();
 		visitor.setCtx(ctx);
 		
 		//skip validations as there're syntax errors
 		if(problems != null && problems.size() > 0){
 			//skip the validation and surface the syntax problems
 			updateValidationCtx(ctx, uniquePath, jstType, new HashSet<IJstType>(), problems);
 			jstType.accept(visitor);
 			return;
 		}
 
 		//bugfix, to unify the type for validation
 		jstType = ctx.getTypeSpaceType(jstType);
 		updateValidationCtx(ctx, uniquePath, jstType, dependencies, problems);
 		
 		
 		//bugfix to make sure no dep type error surfaced for the target type
 		
 		//validating syntax
 		VjoSemanticValidatorRepo.getInstance().deactivateListener(JstIdentifier.class);
 		if(syntaxRoots != null){
 			ctx.getJstTypeNames().clear();
 			ctx.getScope().addTypeNode(jstType);
 			for(IJstNode syntaxRoot : syntaxRoots){
 				syntaxRoot.accept(visitor);
 			}
 			ctx.getScope().removeTypeNode(jstType);
 		}
 		VjoSemanticValidatorRepo.getInstance().activateListener(JstIdentifier.class);
 		
 		//validating symantics
 		jstType.accept(visitor);
 	}
 	
 	private void initValidationCtx(final VjoValidationCtx ctx, 
 			final String groupId,
 			final VjoValidationMode mode){
 		ctx.setValidationMode(mode);
 		ctx.setGroupId(groupId);
 		final IJstType global = JstGlobalScopeUtil.getGlobal(ctx, getJstTypeSpaceMgr());
 		ctx.getScope().addScopeNode(global);
 	}
 	
 	private void updateValidationCtx(final VjoValidationCtx ctx, 
 			final String uniquePath, 
 			IJstType entryType, 
 			Set<IJstType> dependencies,
 			List<IScriptProblem> syntaxProblems){
 		
 		ctx.addSyntaxProblems(entryType, syntaxProblems);
 		ctx.setPath(uniquePath);
 	}
 	
 	private Set<IJstType> getDirectDependencies(IJstType type) {
 
 		Set<IJstType> dependencies = new LinkedHashSet<IJstType>();
 		dependencies.addAll(type.getImports());
 		dependencies.addAll(type.getInactiveImports());
 		dependencies.addAll(type.getSatisfies());
 		dependencies.addAll(type.getExpects());
 		dependencies.addAll(type.getExtends());
 
 		for (IJstTypeReference typeRef : type.getMixinsRef()) {
 			dependencies.add(typeRef.getReferencedType());
 
 		}
 		
 		/* should we introduce inner types' dependencies
 		for(IJstType innerType : type.getEmbededTypes()){
 			
 		}
 		*/
 
 		return dependencies;
 	}
 
 	
 	private IJstType lookUpTypeSpace(final VjoValidationCtx ctx, 
 			final IJstType contract,
 			final String defaultGroupName){
 		
 		final String typeName = contract.getName();
 		final JstTypeSpaceMgr mgr = getJstTypeSpaceMgr();
 		
 		if(typeName != null && mgr != null){
 			//bugfix, using the type's own group name, for cross group references
 			String typeGroupName = contract.getPackage() != null ? contract.getPackage().getGroupName() : null;
 			if(typeGroupName == null || typeGroupName.length() == 0){
 				typeGroupName = defaultGroupName;
 			}
 			
 			IJstType typeInSpace = mgr.getQueryExecutor().findType(new TypeName(typeGroupName, typeName));
 			if(typeInSpace != null){
 				return typeInSpace;
 			}
 		}
 		
 		return null;
 	}
 	
 	private void getAllDependencies(final VjoValidationCtx ctx,
 			final IJstType type, 
 			final Set<IJstType> dependencies, 
 			final Set<String> circularProof, 
 			final String defaultGroupName,
 			final VjoValidationMode mode){
 		if(circularProof.contains(type.getName())){
 			return;
 		}
 		else{
 			circularProof.add(type.getName());
 		}
 		
 		boolean depFound = false;
 		IJstType typeInSpace = null;
 		if(VjoValidationMode.validateTypeSpace.equals(mode)){
 			typeInSpace = lookUpTypeSpace(ctx, type, defaultGroupName);
 			if(typeInSpace != null){
 				depFound = true;
 				ctx.addTypeSpaceType(type, typeInSpace);
 			}
 		}
 		if(typeInSpace == null){
 			typeInSpace = type;
 		}
 		
 		Set<IJstType> directDependencies = getDirectDependencies(typeInSpace);
 		for(Iterator<IJstType> it = directDependencies.iterator(); it.hasNext();){
 			getAllDependencies(ctx, it.next(), dependencies, circularProof, defaultGroupName, VjoValidationMode.validateTypeSpace);
 		}
 		
 		//check if type needs to be looked up under type space
 
 		//TODO need a more effective way looking for types, jstType.jstPackage could often give an empty package name
 		if((VjoValidationMode.validateTypeSpace.equals(mode) && depFound && typeInSpace != null)
 			|| (!VjoValidationMode.validateTypeSpace.equals(mode) && typeInSpace != null)){
 			dependencies.add(typeInSpace);
 			for(IJstType innerType : typeInSpace.getEmbededTypes()){
 				dependencies.add(innerType);
 			}
 			ctx.setDependencyVerifier(typeInSpace, new VjoDependencyVerifier(Collections.unmodifiableSet(dependencies), m_jstTypeSpaceMgr));
 		}
 	}
 	
 	private Set<IJstType> calculateDependencies(final VjoValidationCtx ctx,
 			final IJstType entryType, 
 			final String defaultGroupName,
 			final VjoValidationMode mode) {
 		if(entryType != null){
 			//bugfix, switch to LinkedHashSet to ensure the dependencies order matches their depths
 			Set<IJstType> calculated = new LinkedHashSet<IJstType>();
 			getAllDependencies(ctx, entryType, calculated, new HashSet<String>(), defaultGroupName, mode);
 			
 			//without type space
 			if(calculated.size() <= 0){
 				calculated.add(entryType);
 			}
 			return calculated;
 		}
 		
 		return Collections.emptySet();
 	}
 	
 	public static class VjoGroupDependencyHelper{
 		
 		public static boolean isGroupDependencySatisfied(final IJstType curr,
 				final IJstType dep,
 				final JstTypeSpaceMgr tsMgr){
 			
 			if(curr == null || dep == null || tsMgr == null){
 				throw new IllegalArgumentException("{current type, dependency type, or type space} none should be null!");
 			}
 			
			JstPackage package1 = curr.getPackage();
			if(package1==null){
				return false;
			}
			String groupName = package1.getGroupName();
			if(groupName==null){
				return false;
			}
			final IGroup<IJstType> currGroup = tsMgr.getTypeSpace().getGroup(groupName);
 			final IGroup<IJstType> depGroup = tsMgr.getTypeSpace().getGroup(dep);
 			if(currGroup == null || depGroup == null){
 				return false;
 			}
 			else{
 				return currGroup.isDependOn(depGroup);
 			}
 		}
 	}
 	
 	public static class VjoDependencyVerifier implements IVjoDependencyVerifiable{
 
 		private final Set<IJstType> _directDependencies;
 		
 		private final JstTypeSpaceMgr _tsMgr;
 		
 		public VjoDependencyVerifier(final Set<IJstType> directDependencies,
 				final JstTypeSpaceMgr tsMgr){
 			if(directDependencies == null){
 				throw new IllegalArgumentException("direct dependencies set should never be null");
 			}
 			_directDependencies = new HashSet<IJstType>(directDependencies);
 			_tsMgr = tsMgr;
 		}
 		
 		@Override
 		public boolean verify(final IJstType currentType, final String name) {
 			if(name == null){
 				return false;
 			}
 			
 			for(IJstType dd: _directDependencies){
 				if(name.equals(dd.getName()) || name.equals(dd.getSimpleName())){
 					return VjoGroupDependencyHelper.isGroupDependencySatisfied(currentType, dd, _tsMgr);
 				}
 			}
 			//bugfix by huzhou@ebay.com to verify dependencies via group info
 			final JstPackage pkg = currentType.getRootType().getPackage();
 			if(pkg != null){
 				final String groupName = pkg.getGroupName();
 				final ITypeSpace<IJstType, IJstNode> ts = _tsMgr.getTypeSpace();
 				final IGroup<IJstType> selfGroup = ts.getGroup(groupName);
 				final List<IJstType> visibleTypesByName = ts
 					.getVisibleType(name, selfGroup);
 				if(visibleTypesByName != null 
 						&& visibleTypesByName.size() == 1){
 					return true;
 				}
 			}
 			
 			return false;
 		}
 
 		@Override
 		public void addDependency(IJstType dep) {
 			_directDependencies.add(dep);
 		}
 
 		@Override
 		public Set<IJstType> getDirectDependenciesFilteredByGroup(final IJstType thisType) {
 			final Set<IJstType> filtered = new HashSet<IJstType>();
 			for(IJstType it : _directDependencies){
 				if(VjoGroupDependencyHelper.isGroupDependencySatisfied(thisType, it, _tsMgr)){
 					filtered.add(it);
 				}
 			}
 			return filtered;
 		}
 		
 		@Override
 		public Set<IJstType> getDirectDependencies(){
 			return Collections.unmodifiableSet(_directDependencies);
 		}
 
 		@Override
 		public JstTypeSpaceMgr getTypeSpaceMgr() {
 			return _tsMgr;
 		}
 	}
 	
 
 	public static class VjoDependencyVerifierDecorator implements IVjoDependencyVerifiable{
 		private final IVjoDependencyVerifiable _verifiable;
 		private final Set<IJstType> _directDependencies;
 		
 		public VjoDependencyVerifierDecorator(final IVjoDependencyVerifiable verifiable){
 			if(verifiable == null){
 				throw new IllegalArgumentException("null verifiable not allowed in this constructor");
 			}
 			
 			_verifiable = verifiable;
 			_directDependencies = new HashSet<IJstType>(verifiable.getDirectDependencies());
 		}
 
 		@Override
 		public void addDependency(IJstType dep) {
 			_directDependencies.add(dep);
 		}
 
 		@Override
 		public Set<IJstType> getDirectDependenciesFilteredByGroup(final IJstType thisType) {
 			final Set<IJstType> dds = new HashSet<IJstType>();
 			for(IJstType it : _directDependencies){
 				if(VjoGroupDependencyHelper.isGroupDependencySatisfied(thisType, it, _verifiable.getTypeSpaceMgr())){
 					dds.add(it);
 				}
 			}
 			dds.addAll(_verifiable.getDirectDependenciesFilteredByGroup(thisType));
 			return dds;
 		}
 		
 		@Override
 		public Set<IJstType> getDirectDependencies(){
 			return Collections.unmodifiableSet(_directDependencies);
 		}
 
 		@Override
 		public boolean verify(final IJstType thisType, final String name) {
 			if(name == null){
 				return false;
 			}
 			
 			for(IJstType dd: _directDependencies){
 				if(name.equals(dd.getName()) || name.equals(dd.getSimpleName())){
 					return VjoGroupDependencyHelper.isGroupDependencySatisfied(thisType, dd, getTypeSpaceMgr());
 				}
 			}
 			
 			return _verifiable.verify(thisType, name);
 		}
 
 		@Override
 		public JstTypeSpaceMgr getTypeSpaceMgr() {
 			return _verifiable.getTypeSpaceMgr();
 		}
 		
 	}
 }
