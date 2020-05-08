 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.vjo.tool.typespace;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstParseController;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.ts.JstQueryExecutor;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.jst.ts.TypeSpaceConfig;
 import org.eclipse.vjet.dsf.jst.ts.TypeSpaceLocker;
 import org.eclipse.vjet.dsf.jstojava.controller.JstParseController;
 import org.eclipse.vjet.dsf.ts.ITypeSpace;
 import org.eclipse.vjet.dsf.ts.TypeSpace;
 import org.eclipse.vjet.dsf.ts.event.EventListenerStatus;
 import org.eclipse.vjet.dsf.ts.event.ISourceEvent;
 import org.eclipse.vjet.dsf.ts.event.ISourceEventCallback;
 import org.eclipse.vjet.dsf.ts.event.dispatch.IEventListenerHandle;
 import org.eclipse.vjet.dsf.ts.event.group.AddGroupEvent;
 import org.eclipse.vjet.dsf.ts.event.group.BatchGroupLoadingEvent;
 import org.eclipse.vjet.dsf.ts.event.group.RemoveGroupEvent;
 import org.eclipse.vjet.dsf.ts.event.type.AddTypeEvent;
 import org.eclipse.vjet.dsf.ts.event.type.ModifyTypeEvent;
 import org.eclipse.vjet.dsf.ts.event.type.RemoveTypeEvent;
 import org.eclipse.vjet.dsf.ts.group.IGroup;
 import org.eclipse.vjet.dsf.ts.method.MethodName;
 import org.eclipse.vjet.dsf.ts.property.PropertyName;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 import org.eclipse.vjet.vjo.lib.LibManager;
 import org.eclipse.vjet.vjo.lib.TsLibLoader;
 
 
 /**
  * Facade class for all type space operations. Send type space events for manage
  * resources in {@link TypeSpace} object. Also contains utilities methods for
  * find types, satisfiers and sub types.
  * 
  * 
  * 
  */
 public class TypeSpaceMgr {
 
 	public static final String WINDOW = "Window";
 
 	public static final String GLOBAL = "Global";
 
 	public static final String OBJECT = "Object";
 
 	private static TypeSpaceMgr s_instance = new TypeSpaceMgr();
 
 	public static final String NATIVE_GROUP = JstTypeSpaceMgr.JS_NATIVE_GRP;
 
 	private JstParseController m_controller;
 
 	private ITypeSpaceLoader m_typeLoader;
 
 	private Map<String, List<String>> m_groupDepends = Collections.emptyMap();
 
 	public static final String WINDOW_VAR = "window";
 
 	private final TypeSpaceLocker m_locker = new TypeSpaceLocker();
 
 	private Collection<TypeSpaceListener> m_listeners = new ArrayList<TypeSpaceListener>();
 
 	private Map<String, URI> m_typeToFileMap = new HashMap<String, URI>();
 
 	private ModifyTypeCallback modifyTypeCallback = new ModifyTypeCallback();
 
 	private Map<String, URI> m_typeToFileMapSync;
 
 	public static final List<String> NATIVE_GLOBAL_OBJECTS;
 	static {
 		List<String> nativeGlobalObjects = new ArrayList<String>(16);
 		nativeGlobalObjects.add("Array");
 		nativeGlobalObjects.add("Boolean");
 		nativeGlobalObjects.add("Date");
 		nativeGlobalObjects.add("Error");
 		nativeGlobalObjects.add("EvalError");
 		nativeGlobalObjects.add("Function");
 		nativeGlobalObjects.add("Math");
 		nativeGlobalObjects.add("Number");
 		nativeGlobalObjects.add("Object");
 		nativeGlobalObjects.add("RangeError");
 		nativeGlobalObjects.add("ReferenceError");
 		nativeGlobalObjects.add("RegExp");
 		nativeGlobalObjects.add("String");
 		nativeGlobalObjects.add("SyntaxError");
 		nativeGlobalObjects.add("TypeError");
 		nativeGlobalObjects.add("URIError");
 		nativeGlobalObjects.add("Window");
 		nativeGlobalObjects.add("Global");
 		nativeGlobalObjects.add("Object");
 		// nativeGlobalObjects.add("Storage"); //missing???
 
 		NATIVE_GLOBAL_OBJECTS = Collections
 				.unmodifiableList(nativeGlobalObjects);
 	}
 
 	/**
 	 * Creates instance of this class.
 	 */
 	private TypeSpaceMgr() {
 		super();
 		// IJstParseController controller = new JstParseController(new
 		// VjoParser());'
 
 		// JstNativeTypeGenJob job = new JstNativeTypeGenJob();
 		// job.schedule();
 	}
 
 	public void init(IJstParseController controller) {
 		m_controller = (JstParseController) controller;
 		// m_controller = new JstParseController(new VjoParserToJstAndIType());
 		// m_controller.setRefResolver(resolver.newInstance(m_controller));
 		// new JstTypeSpaceMgr(m_controller, new VjoJstTypeLoader());
 		JstTypeSpaceMgr jstTypeSpaceMgr = m_controller.getJstTypeSpaceMgr();
 		jstTypeSpaceMgr.initialize();
 		TsLibLoader.loadDefaultLibs(jstTypeSpaceMgr);
 		promoteGlobals(jstTypeSpaceMgr);
 	}
 
 	// TODO enhance 3rd party library loading and remove this code
 	private void promoteGlobals(JstTypeSpaceMgr jstTypeSpaceMgr) {
 		jstTypeSpaceMgr.getTypeSpace().addAllGlobalTypeMembers(
 				new TypeName(JstTypeSpaceMgr.JS_BROWSER_GRP, "Global"));
 //		jstTypeSpaceMgr.getTypeSpace().addToGlobalMemberSymbolMap("JQueryx",
 //				"$", "vjo.dsf.jqueryx.Jq.$");
 //		jstTypeSpaceMgr.getTypeSpace().addToGlobalTypeSymbolMap("JQueryx",
 //				"jQuery", "vjo.dsf.jqueryx.Jq");
 		jstTypeSpaceMgr.getTypeSpace().addToGlobalTypeSymbolMap(
 				LibManager.VJO_SELF_DESCRIBED, "vjo", "vjo");
 //		jstTypeSpaceMgr.getTypeSpace().addToGlobalMemberSymbolMap(
 //				JstTypeSpaceMgr.JS_BROWSER_GRP, "alert", "Window.alert");
 	}
 
 	/**
 	 * Returns the {@link TypeSpace} object.
 	 * 
 	 * @return the {@link TypeSpace} object.
 	 */
 	public ITypeSpace<IJstType, IJstNode> getTypeSpace() {
 		return getController().getJstTypeSpaceMgr().getTypeSpace();
 	}
 
 	/**
 	 * Process {@link IEventListenerHandle} event by {@link JstTypeSpaceMgr}.
 	 * 
 	 * @param event
 	 *            {@link IEventListenerHandle} event
 	 */
 	public void processEvent(ISourceEvent<IEventListenerHandle> event) {
 		try {
 			getController().getJstTypeSpaceMgr().processEvent(event);
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Process {@link IEventListenerHandle} event by {@link JstTypeSpaceMgr}
 	 * with {@link ISourceEventCallback} call back object.
 	 * 
 	 * @param event
 	 *            {@link IEventListenerHandle} event
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	public static void processEvent(ISourceEvent<IEventListenerHandle> event,
 			ISourceEventCallback<IJstType> callback) {
 		try {
 			// using sync call since builder will call multiple
 			TypeSpaceMgr.getInstance().getController().getJstTypeSpaceMgr().processEvent(event);
 			// TODO pass monitor/callback into processEvent Not correct but temporary - always successful
 			if(callback!=null){
 				callback.onComplete( new EventListenerStatus<IJstType>(
 						EventListenerStatus.Code.Successful));
 			}
 
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets the new value for the {@link TypeSpaceConfig} object of the
 	 * {@link JstTypeSpaceMgr}.
 	 * 
 	 * @param config
 	 *            {@link TypeSpaceConfig} object.
 	 * @return {@link JstTypeSpaceMgr} object.
 	 */
 	public JstTypeSpaceMgr setConfig(TypeSpaceConfig config) {
 		return getController().getJstTypeSpaceMgr().setConfig(config);
 	}
 
 	/**
 	 * Returns singleton instance of this class.
 	 * 
 	 * @return singleton instance of this class.
 	 */
 	public static TypeSpaceMgr getInstance() {
 		return s_instance;
 	}
 
 	/**
 	 * Creates {@link AddGroupEvent} event from the {@link GroupInfo} object and
 	 * check that group info not contains in list of the processed groups.
 	 * 
 	 * @param group
 	 *            {@link GroupInfo} object.
 	 * @param groups
 	 *            list of the processed group names.
 	 * @return {@link AddGroupEvent} event
 	 */
 	public AddGroupEvent createGroupEvent(GroupInfo group, List<String> groups) {
 
 		if (!groups.contains(group.getGroupName())) {
 			groups.add(group.getGroupName());
 			return createGroupEvent(group);
 		}
 		return null;
 	}
 
 	/**
 	 * Create {@link AddGroupEvent} event from the specified {@link GroupInfo}
 	 * object.
 	 * 
 	 * @param group
 	 *            {@link GroupInfo} object.
 	 * @return {@link AddGroupEvent} event.
 	 */
 	private AddGroupEvent createGroupEvent(GroupInfo group) {
 		
 		
 		
 		List<String> inclusionRules = group.getSrcPath().getInclusionRules();
 		List<String> exclusionRules = group.getSrcPath().getExclusionRules();
 		return new AddGroupEvent(group.getGroupName(), group.getGroupPath(),
 				group.isLibrary()? group.getSrcPath().getSourcePaths(): null, 
 				group.getClassPath(), 
 				group.getDirectDependency(), 
 				group.getBootstrapPath(),
 				inclusionRules, exclusionRules);
 	}
 
 	/**
 	 * Find type in type space by specified {@link TypeName} object.
 	 * 
 	 * @param typeName
 	 *            {@link TypeName} object.
 	 * @return {@link IJstType} object.
 	 */
 	public IJstType findType(TypeName typeName) {
 			return getController().getJstTypeSpaceMgr().getQueryExecutor()
 					.findType(typeName);
 	}
 
 	/**
 	 * Find list of the satisfiers in type space for specified {@link TypeName}
 	 * object.
 	 * 
 	 * @param name
 	 *            {@link TypeName} object.
 	 * @return list of the {@link IJstType} object.
 	 */
 	public List<IJstType> findSatisfiers(TypeName name) {
 		return getController().getJstTypeSpaceMgr().getQueryExecutor()
 				.findSatisfiers(name);
 	}
 
 	/**
 	 * Find list of the sub types in type space for specified {@link TypeName}
 	 * object.
 	 * 
 	 * @param name
 	 *            {@link TypeName} object.
 	 * @return list of the {@link IJstType} object.
 	 */
 	public List<IJstType> findSubTypes(TypeName name) {
 		return getController().getJstTypeSpaceMgr().getQueryExecutor()
 				.findSubTypes(name);
 	}
 
 	/**
 	 * Load all workspace types on startup.
 	 * 
 	 * @see TypeSpaceLoadJob
 	 * 
 	 * @param monitor
 	 *            {@link TypeLoadMonitor} object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	public synchronized void load(TypeLoadMonitor monitor,
 			ISourceEventCallback<IJstType> callback) {
 
 		doLoad(monitor, callback);
 
 	}
 
 	private void doLoad(TypeLoadMonitor monitor,
 			ISourceEventCallback<IJstType> callback) {
 		if (m_typeLoader != null) {
 			loadTypes(monitor, callback);
 		}
 	}
 
 	/**
 	 * Clean type space. Remove all user groups from the type space.
 	 * 
 	 */
 	public void clean() {
 		// copy group names from type space
 		Set<String> set = new LinkedHashSet<String>(getController()
 				.getJstTypeSpaceMgr().getTypeSpace().getGroups().keySet());
 
 		Iterator<String> iter = set.iterator();
 
 		while (iter.hasNext()) {
 			String group = iter.next();
 			cleanGroup(group);
 		}
 
 		this.m_groupDepends = null;
 
 	}
 
 	public void cleanGroup(String group) {
 		if (!TsLibLoader.isDefaultLibName(group)) {
 			processEvent(new RemoveGroupEvent(group, group));
 		}
 	}
 
 	private void loadGroupDepends() {
 		this.m_groupDepends = m_typeLoader.getGroupDepends();
 	}
 
 	/**
 	 * Loading types using {@link EclipseTypeSpaceLoader#getGroupInfo()} method.
 	 * 
 	 * @param monitor
 	 *            {@link TypeLoadMonitor} monitor object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	private void loadTypes(TypeLoadMonitor monitor,
 			ISourceEventCallback<IJstType> callback) {
 
 		monitor.preparationTypeListStarted();
 		List<GroupInfo> list = m_typeLoader.getGroupInfo();
 		monitor.preparationTypeListFinished();
 		loadTypes(monitor, list);
 	}
 
 	private void loadTypes(TypeLoadMonitor monitor, List<GroupInfo> list) {
 
 		try {
 			// m_locker.lockExclusive();
 
 			// stop loading if list of the group info is empty
 			if (list.size() == 0) {
 				return;
 			}
 
 			monitor.loadTypeListStarted(list.size());
 
 			int initGroupSize = m_controller.getJstTypeSpaceMgr()
 					.getTypeSpace().getGroups().size();
 
 
 			// callback that will call all VjoSourceModules that will be
 			// reconciled DLTK to JST models
 			int totalGroups = initGroupSize + list.size();
 
 			loadToTypeSpace(monitor, list, new TypeSpaceLoadEvent(totalGroups, m_locker, monitor));
 
 		} finally {
 			// m_locker.releaseExclusive();
 			// TODO use call back instead this locks up workspace
 			// waitUntilLoaded();
 			// while (m_loaded == false)
 			// ;
 
 		}
 	}
 
 	/**
 	 * Creates {@link BatchGroupLoadingEvent} from the list of the
 	 * {@link GroupInfo} objects and send event to the type space.
 	 * 
 	 * @param monitor
 	 *            {@link TypeLoadMonitor} monitor.
 	 * @param list
 	 *            list of the {@link GroupInfo} object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	private void loadToTypeSpace(TypeLoadMonitor monitor, List<GroupInfo> list,
 			final ISourceEventCallback<IJstType> callback) {
 		BatchGroupLoadingEvent batch = new BatchGroupLoadingEvent();
 		List<String> groups = new ArrayList<String>();
 
 		for (GroupInfo stn : list) {
 //			if(stn.getDirectDependency().size()==0){
 //				throw new DsfRuntimeException("no direct dependencies for group " + stn.getGroupName() +" try again");
 //			}
 			AddGroupEvent event = createGroupEvent(stn, groups);
 			if (event != null) {
 				batch.addGroupEvent(event);
 			}
 		}
 
 //		m_fullyLoaded = false;
 
 		processEvent(batch, null);
 		
 	}
 
 	public ITypeSpaceLoader getTypeLoader() {
 		return m_typeLoader;
 	}
 
 	public void setTypeLoader(ITypeSpaceLoader typeLoader) {
 		this.m_typeLoader = typeLoader;
 	}
 
 	/**
 	 * Find super types for specified {@link TypeName} object.
 	 * 
 	 * @param typeName
 	 *            {@link TypeName} object.
 	 * @return list of the {@link IJstType} objects.
 	 */
 	public List<IJstType> findSuperTypes(TypeName typeName) {
 
 		IJstType type = getTypeSpace().getType(typeName);
 		if (type == null) {
 			return Collections.emptyList();
 		}
 
 		List<IJstType> superTypes = new ArrayList<IJstType>();
 
 		while (type != null) {
 			// get sub type
 			type = type.getExtend();
 
 			if (superTypes.contains(type)) {
 				break;
 			}
 
 			if (type != null) {
 				superTypes.add(0, type);
 			}
 		}
 
 		return superTypes;
 	}
 
 	/**
 	 * Find types by string pattern.
 	 * 
 	 * @param pattern
 	 *            string pattern.
 	 * 
 	 * @return list of the {@link IJstType} objects.
 	 */
 	public List<IJstType> findType(String pattern) {
 		return getTypeSpace().getType(pattern);
 	}
 
 	/**
 	 * Returns all types in type space.
 	 * 
 	 * @return list of the {@link IJstType} objects.
 	 */
 	public Collection<IJstType> getAllTypes() {
 		return getTypeSpace().getTypes().values();
 	}
 
 	/**
 	 * Returns list of the depends groups for specified group. Calls
 	 * {@link EclipseTypeSpaceLoader#getGroupDepends()} method.
 	 * 
 	 * @param group
 	 *            name of the group.
 	 * @return list of the depends group names.
 	 */
 	public List<String> getGroupDepends(String group) {
 
 		if (m_groupDepends == null) {
 			loadGroupDepends();
 		}
 
 		List<String> list = m_groupDepends.get(group);
 
 		if (list == null) {
 			list = new ArrayList<String>(1);
 		}
 
 		if (!list.contains(JstTypeSpaceMgr.JS_NATIVE_GRP)) {
 			list.add(JstTypeSpaceMgr.JS_NATIVE_GRP);
 		}
 
 		return list;
 	}
 
 	/**
 	 * Calls {@link #clean()} an then
 	 * {@link #load(TypeLoadMonitor, ISourceEventCallback)} methods.
 	 * 
 	 * @param monitor
 	 *            {@link TypeLoadMonitor} monitor object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} object.
 	 */
 	public synchronized void reload(TypeLoadMonitor monitor,
 			ISourceEventCallback<IJstType> callback) {
 		clean();
 		load(monitor, callback);
 	}
 
 	public synchronized void reloadGroup(TypeLoadMonitor monitor, String group,
 			ISourceEventCallback<IJstType> callback) {
 		cleanGroup(group);
 
 		loadGroup(monitor, group, callback);
 	}
 
 	private void loadGroup(TypeLoadMonitor monitor, String group,
 			ISourceEventCallback<IJstType> callback) {
 
 		if (m_typeLoader != null) {
 			monitor.preparationTypeListStarted();
 			List<GroupInfo> list = m_typeLoader.getGroupInfo(group);
 			monitor.preparationTypeListFinished();
 
 			loadTypes(monitor, list);
 		}
 	}
 
 	/**
 	 * Reload type space.
 	 * 
 	 * @param callback
 	 *            {@link ISourceEventCallback}
 	 */
 	public synchronized void reload(ISourceEventCallback<IJstType> callback) {
 		reload(new EmptyTypeLoadMonitor(), callback);
 	}
 
 	/**
 	 * Returns list of the method depends nodes for the specified method name.
 	 * 
 	 * @param mtdName
 	 *            {@link MethodName} object.
 	 * @return list of the {@link IJstNode} objects.
 	 */
 	public List<IJstNode> getMethodDependents(MethodName mtdName) {
 		List<IJstNode> dependents = getController().getJstTypeSpaceMgr().getTypeSpace()
 				.getMethodDependents(mtdName);
 
		return dependents;
 	}
 
 	/**
 	 * Returns list of the property depends nodes for the specified property
 	 * name.
 	 * 
 	 * @param ptyName
 	 *            {@link PropertyName} object.
 	 * @return list of the {@link PropertyName} objects.
 	 */
 	public List<IJstNode> getPropertyDependents(PropertyName ptyName) {
 		List<IJstNode> dependents = getController().getJstTypeSpaceMgr().getTypeSpace()
 				.getPropertyDependents(ptyName);
 
 		return getVisibleDependents(dependents, ptyName.getGroupName());
 	}
 
 	/**
 	 * Returns list of the direct depends to the specified type name.
 	 * 
 	 * @param typeName
 	 *            {@link TypeName} object.
 	 * @return list of the {@link IJstNode} objects.
 	 */
 	public List<IJstType> getDirectDependents(TypeName typeName) {
 		List<IJstType> dependents =  getController().getJstTypeSpaceMgr().getTypeSpace()
 				.getDirectDependents(typeName);
 
 		return getVisibleDependents(dependents, typeName.groupName());
 	}
 
 	/**
 	 * Returns collection of the types by group name.
 	 * 
 	 * @param jsNativeGrp
 	 *            group name.
 	 * @return collection of the {@link IJstType} objects.
 	 */
 	public Collection<IJstType> getTypes(String jsNativeGrp) {
 		Collection<IJstType> collection = Collections.emptyList();
 		IGroup<IJstType> group = getController().getJstTypeSpaceMgr()
 				.getTypeSpace().getGroup(jsNativeGrp);
 		if (group != null) {
 			collection = group.getEntities().values();
 		}
 		return collection;
 	}
 
 	/**
 	 * Returns true if exists type in type space with specified group and type
 	 * name.
 	 * 
 	 * @param group
 	 *            group name
 	 * @param name
 	 *            type name
 	 * @return true if exists type with specified group and type name.
 	 */
 	public boolean existType(String group, String name) {
 		TypeName typeName = new TypeName(group, name);
 		return existType(typeName);
 	}
 
 	/**
 	 * Returns true if exist type in type space with specified type name object.
 	 * 
 	 * @param typeName
 	 *            {@link TypeName} object.
 	 * @return true if exist type in type space with specified type name object.
 	 */
 	public static boolean existType(TypeName typeName) {
 		return TypeSpaceMgr.getInstance().findType(typeName) != null;
 	}
 
 	/**
 	 * Find native type in type space by specified name.
 	 * 
 	 * @param name
 	 *            native type name.
 	 * @return {@link IJstType} object.
 	 */
 	public IJstType getNativeType(String name) {
 		TypeName typeName = new TypeName(JstTypeSpaceMgr.JS_NATIVE_GRP, name);
 		return findType(typeName);
 	}
 
 	/**
 	 * Add type space listener to this class.
 	 * 
 	 * @param listener
 	 *            {@link TypeSpaceListener} object.
 	 */
 	public void addTypeSpaceListener(TypeSpaceListener listener) {
 		if (!m_listeners.contains(listener)) {
 			m_listeners.add(listener);
 		}
 	}
 
 	/**
 	 * Fire event to all {@link TypeSpaceListener} objects when loading types
 	 * finished.
 	 */
 	protected void fireLoadTypesFinished() {
 		Collection<TypeSpaceListener> listeners;
 		listeners = new ArrayList<TypeSpaceListener>(m_listeners);
 
 		for (TypeSpaceListener listener : listeners) {
 			listener.loadTypesFinished();
 		}
 	}
 
 	/**
 	 * Remove type space listener from this class.
 	 * 
 	 * @param listener
 	 *            {@link TypeSpaceListener} object.
 	 */
 	public void removeTypeSpaceListener(TypeSpaceListener listener) {
 		m_listeners.remove(listener);
 	}
 
 	/**
 	 * Refresh type space types. Calls method
 	 * {@link EclipseTypeSpaceLoader#getChangedTypes()}.
 	 * 
 	 * @param monitor
 	 *            {@link TypeLoadMonitor} monitor object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	public synchronized void refresh(TypeLoadMonitor monitor,
 			ISourceEventCallback<IJstType> callback) {
 
 		try {
 			// m_locker.lockExclusive();
 			doRefresh(monitor, callback);
 		} finally {
 			// m_locker.releaseExclusive();
 			fireRefreshTypesFinished();
 		}
 
 	}
 
 	/**
 	 * Fire event to all {@link TypeSpaceListener} objects when refreshing types
 	 * finished.
 	 */
 	private void fireRefreshTypesFinished() {
 		Collection<TypeSpaceListener> listeners;
 		listeners = new ArrayList<TypeSpaceListener>(m_listeners);
 		List<SourceTypeName> list = m_typeLoader.getChangedTypes();
 		for (TypeSpaceListener listener : listeners) {
 			listener.refreshFinished(list);
 		}
 	}
 
 	/**
 	 * Refresh resource in type space.
 	 * 
 	 * @param monitor
 	 *            {@link TypeLoadMonitor} monitor object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	private void doRefresh(TypeLoadMonitor monitor,
 			ISourceEventCallback<IJstType> callback) {
 		if (m_typeLoader != null) {
 			loadChangedTypes(monitor, modifyTypeCallback);
 		}
 	}
 
 	/**
 	 * Load changed types to the type space. Calls
 	 * {@link EclipseTypeSpaceLoader#getChangedTypes()} method.
 	 * 
 	 * @param monitor
 	 *            {@link TypeLoadMonitor} monitor object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	private void loadChangedTypes(TypeLoadMonitor monitor,
 			ISourceEventCallback<IJstType> callback) {
 		monitor.preparationTypeListStarted();
 		List<SourceTypeName> changedTypes = m_typeLoader.getChangedTypes();
 		for (SourceTypeName source : changedTypes) {
 			doProcessType(source, callback);
 		}
 	}
 
 	/**
 	 * Resolve expressions for all type in type space.
 	 */
 	private void resolveAll() {
 		Collection<IJstType> types = getAllTypes();
 		for (IJstType jstType : types) {
 			getController().resolve(jstType);
 		}
 	}
 
 	public void setAllowChanges(boolean isAllowChanges) {
 	//	this.isAllowChanges = isAllowChanges;
 	}
 
 	/**
 	 * Run protected type space operation.
 	 * 
 	 * @param runnable
 	 *            {@link ITypeSpaceRunnable}
 	 */
 	public void run(ITypeSpaceRunnable runnable) {
 		try {
 			// m_locker.lockShared();
 			runnable.run();
 		} finally {
 			// m_locker.releaseShared();
 		}
 	}
 
 	/**
 	 * Returns true if specified group exist in type space.
 	 * 
 	 * @param groupName
 	 *            group name
 	 * @return true if specified group exist in type space.
 	 */
 	public boolean existGroup(String groupName) {
 		return getController().getJstTypeSpaceMgr().getTypeSpace().getGroup(
 				groupName) != null;
 	}
 
 	/**
 	 * Find type in type space by specified group name and type name.
 	 * 
 	 * @param group
 	 *            group name.
 	 * @param name
 	 *            type name.
 	 * @return {@link IJstType} object
 	 */
 	public static IJstType findType(String group, String name) {
 		TypeName typeName = new TypeName(group, name);
 		IJstType jstType = getInstance().findType(typeName);
 //		if (jstType == null) {
 //			jstType = JstFactory.getInstance().createJstType(
 //					typeName.typeName(), true);
 //		}
 		return jstType;
 	}
 
 	public static JstTypeSpaceMgr TS() {
 		return getInstance().getController().getJstTypeSpaceMgr();
 	}
 
 	public static IJstParseController parser() {
 		return getInstance().getController();
 	}
 
 	public JstParseController getController() {
 		return m_controller;
 	}
 
 	public static JstQueryExecutor QE() {
 		return TS().getQueryExecutor();
 	}
 
 	public static boolean isNativeGlobalObject(String token) {
 		// TODO this should use findType from native groups
 		return NATIVE_GLOBAL_OBJECTS.contains(token);
 	}
 
 
 	public void waitUntilLoaded() {
 		// while (!m_loaded){
 		// try {
 		// Thread.sleep(1000);
 		// } catch (InterruptedException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 		// }
 
 	}
 
 	/**
 	 * Process type changes with specified source type name. if type is added
 	 * the send {@link AddTypeEvent}, changed {@link ModifyTypeEvent}, removed
 	 * {@link RemoveTypeEvent}.
 	 * 
 	 * 
 	 * @param name
 	 *            {@link SourceTypeName} object.
 	 * @param callback
 	 *            {@link ISourceEventCallback} object.
 	 */
 	public static void doProcessType(SourceTypeName name,
 			ISourceEventCallback<IJstType> callback) {
 
 		TypeName typeName = new TypeName(name.groupName(), name.typeName());
 
 		int action = name.getAction();
 
 		if (isChangedTypeNotExist(name)) {
 			action = SourceTypeName.ADDED;
 		}
 
 		switch (action) {
 		case SourceTypeName.ADDED:
 //			IJstType type = findType(name);
 			IJstType type =  TypeSpaceMgr.getInstance().getController().getJstTypeSpaceMgr().getQueryExecutor()
 					.findType(typeName);
 			AddTypeEvent addEvent = null;
 			if (type == null) {
 				 addEvent = new AddTypeEvent(name.groupName(), name
 						.typeName(), name.source());
 			} else {
 				addEvent = new AddTypeEvent<IJstType>(name, type);
 			}
 			processEvent(addEvent, callback);
 			break;
 		case SourceTypeName.CHANGED:
 			ModifyTypeEvent event = new ModifyTypeEvent(name.groupName(), name
 					.typeName(), name.source());
 			processEvent(event, callback);
 			break;
 		case SourceTypeName.REMOVED:
 			RemoveTypeEvent removeEvent = new RemoveTypeEvent(typeName);
 			processEvent(removeEvent, callback);
 			break;
 		default:
 			// DLTKCore.error("Unprocesses action " + name.getAction());
 			break;
 		}
 
 		// System.out.println("Update type space - " + name);
 	}
 
 	/**
 	 * Returns true if changed type not exist in type space.
 	 * 
 	 * @param name
 	 *            {@link SourceTypeName} object.
 	 * @return true if changed type not exist in type space.
 	 */
 	private static boolean isChangedTypeNotExist(SourceTypeName name) {
 		return name.getAction() == SourceTypeName.CHANGED && !existType(name);
 	}
 
 	/**
 	 * Load types to type space for specified {@link GroupInfo} list.
 	 * 
 	 * @param monitor
 	 *            {@link EclipseTypeSpaceLoader} monitor.
 	 * @param list
 	 *            list of the {@link GroupInfo} objects.
 	 * @param callback
 	 *            {@link ISourceEventCallback} call back object.
 	 */
 	public void load(TypeLoadMonitor monitor, List<GroupInfo> list) {
 
 			loadTypes(monitor, list);
 		
 	}
 
 
 	public Map<String, URI> getTypeToFileMap() {
 		if(m_typeToFileMapSync == null){
 			m_typeToFileMapSync = Collections.synchronizedMap(m_typeToFileMap);
 		}
 		return m_typeToFileMapSync;
 	}
 
 	public void setTypeToFileMap(Map<String, URI> typeToFileMap) {
 		m_typeToFileMap = typeToFileMap;
 	}
 
 	// Private
 	private <T extends IJstNode> List<T> getVisibleDependents(final List<T> dependents, final String groupName){
 		if (dependents == null || dependents.size() == 0){
 			return Collections.emptyList();
 		}
 
 		if (groupName == null){
 			return Collections.unmodifiableList(dependents);
 		}
 
 		ITypeSpace<IJstType,IJstNode> ts = getTypeSpace();
 		IGroup<IJstType> group = ts.getGroup(groupName);
 		if (group == null){
 			throw new RuntimeException("group is not found in type space: " + groupName);
 		}
 		List<T> visible = new ArrayList<T>();
 		for (T d: dependents){
 			if (ts.getGroup(d.getRootType()).isDependOn(group)){
 				visible.add(d);
 			}
 		}
 		return Collections.unmodifiableList(visible);
 	}
 }
