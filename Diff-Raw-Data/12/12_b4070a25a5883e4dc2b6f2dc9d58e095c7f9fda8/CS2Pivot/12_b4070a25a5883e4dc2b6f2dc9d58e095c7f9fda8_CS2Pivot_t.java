 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: CS2Pivot.java,v 1.13 2011/05/20 15:27:24 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.base.cs2pivot;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EContentsEList;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagedAdapter;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.messages.OCLMessages;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeFilter;
 import org.eclipse.ocl.examples.pivot.utilities.AbstractConversion;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.BaseCSTPackage;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementRefCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ModelElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PathElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PathNameCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.RootCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TypedTypeRefCS;
 import org.eclipse.ocl.examples.xtext.base.util.BaseCSVisitor;
 import org.eclipse.ocl.examples.xtext.base.utilities.CSI2PivotMapping;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.xtext.TerminalRule;
 import org.eclipse.xtext.diagnostics.Diagnostic;
 import org.eclipse.xtext.diagnostics.DiagnosticMessage;
 import org.eclipse.xtext.diagnostics.IDiagnosticConsumer;
 import org.eclipse.xtext.diagnostics.Severity;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.ILeafNode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 
 /**
  * CS2Pivot manages the equivalence between a Concrete Syntax Resources
  * and their corresponding Pivot Resources creating a CS2PivotConversion
  * to update.
  */
 public abstract class CS2Pivot extends AbstractConversion implements MetaModelManagedAdapter
 {	
 	public static interface UnresolvedProxyMessageProvider
 	{
 		@NonNull EReference getEReference();	
 		@Nullable String getMessage(@NonNull EObject context, @NonNull String linkText);
 	}
 
 	public static abstract class AbstractUnresolvedProxyMessageProvider implements UnresolvedProxyMessageProvider
 	{
 		protected final @NonNull EReference eReference;
 		
 		public AbstractUnresolvedProxyMessageProvider(/*@NonNull*/ EReference eReference) {
 			assert eReference != null;
 			this.eReference = eReference;
 		}
 		public @NonNull EReference getEReference() {
 			return eReference;
 		}
 		
 		public abstract @Nullable String getMessage(@NonNull EObject context, @NonNull String linkText);
 	}
 
 	/**
 	 * Return the containment features ordered so that library and import features are processed bedfore anything else.
 	 */
 	public static EList<EObject> computeRootContainmentFeatures(RootCS csRoot) {
 		BasicEList<EReference> containmentsList = new BasicEList<EReference>();
 		for (EStructuralFeature eStructuralFeature : csRoot.eClass().getEAllStructuralFeatures()) {
 			if (eStructuralFeature instanceof EReference) {
 				EReference eReference = (EReference) eStructuralFeature;
 				if (eReference.isContainment()) {
 					containmentsList.add(eReference);
 				}
 			}
 		}
 		int index = containmentsList.indexOf(BaseCSTPackage.Literals.ROOT_CS__OWNED_IMPORT);
 		if (index > 0) {
 			containmentsList.move(0, index);		// Process imports second
 		}
 		index = containmentsList.indexOf(BaseCSTPackage.Literals.ROOT_CS__OWNED_LIBRARY);
 		if (index > 0) {
 			containmentsList.move(0, index);		// Process libraries first
 		}
 		return new EContentsEList<EObject>(csRoot, containmentsList);
 	}
 	
 	private static Map<EReference, UnresolvedProxyMessageProvider> unresolvedProxyMessageProviderMap = new HashMap<EReference, UnresolvedProxyMessageProvider>();
 
 	/**
 	 * Whether to show file and line number context at the start of messages.
 	 */
 	public static boolean showContext = false;
 
 	/**
 	 * Interface for an optional message binder that may be used to affix additional context
 	 * for standalone and command line applications.
 	 */
 	public static interface MessageBinder
 	{
 		@NonNull String bind(@NonNull EObject csContext, @NonNull String messageTemplate, Object... bindings);	
 	}
 
 	/**
 	 * Default message binder that just invokes {@link NLS.bind}.
 	 */
 	public static class DefaultMessageBinder implements CS2Pivot.MessageBinder
 	{
 		public static final @NonNull MessageBinder INSTANCE = new DefaultMessageBinder();
 
 		public @NonNull String bind(@NonNull EObject csContext, @NonNull String messageTemplate, Object... bindings) {
 			return DomainUtil.bind(messageTemplate, bindings);
 		}
 	}
 
 	/**
 	 * Message binder that prefixes the uri and line number to the return from {@link NLS.bind}.
 	 */
 	public static class MessageBinderWithLineContext implements CS2Pivot.MessageBinder
 	{
 		public static final MessageBinder INSTANCE = new MessageBinderWithLineContext();
 
 		public @NonNull String bind(@NonNull EObject csContext, @NonNull String messageTemplate, Object... bindings) {
 			String message = DomainUtil.bind(messageTemplate, bindings);
 			ICompositeNode node = NodeModelUtils.getNode(csContext);
 			if (node != null) {
 				int startLine = node.getStartLine();
 				String uri = csContext.eResource().getURI().toString();
 				return uri + ":" + startLine + " " + message;
 			}
 			return message;
 		}
 	}
 	
 	private static MessageBinder messageBinder = DefaultMessageBinder.INSTANCE;
 
 	public static void addUnresolvedProxyMessageProvider(UnresolvedProxyMessageProvider unresolvedProxyMessageProvider) {
 		unresolvedProxyMessageProviderMap.put(unresolvedProxyMessageProvider.getEReference(), unresolvedProxyMessageProvider);
 	}
 
 	public static Element basicGetType(TypedTypeRefCS csTypedRef) {
 		List<PathElementCS> path = csTypedRef.getPathName().getPath();
 		int iLast = path.size()-1;
 		for (int i = 0; i < iLast; i++) {
 			Element element = path.get(i).basicGetElement();
 			if (element == null) {
 				return null;
 			}
 		}
 		Element element = path.get(iLast).basicGetElement();
 		if (element == null) {
 			return null;
 		}
 		return element;
 	}
 
 	public static @Nullable DiagnosticMessage getUnresolvedProxyMessage(@NonNull EReference eReference, @NonNull EObject csContext, @NonNull String linkText) {
 		String message = getUnresolvedProxyText(eReference, csContext, linkText);
 		return message != null ? new DiagnosticMessage(message, Severity.ERROR, Diagnostic.LINKING_DIAGNOSTIC) : null;
 	}	
 
 	public static @Nullable String getUnresolvedProxyText(@NonNull EReference eReference, @NonNull EObject csContext, @NonNull String linkText) {
 		ExceptionAdapter exceptionAdapter = PivotUtil.getAdapter(ExceptionAdapter.class, csContext);
 		if (exceptionAdapter != null) {
 			return exceptionAdapter.getErrorMessage();
 		}
 		UnresolvedProxyMessageProvider unresolvedProxyMessageProvider = unresolvedProxyMessageProviderMap.get(eReference);
 		if (unresolvedProxyMessageProvider != null) {
 			return unresolvedProxyMessageProvider.getMessage(csContext, linkText);
 		}
 		@SuppressWarnings("null") @NonNull String messageTemplate = OCLMessages.Unresolved_ERROR_;
 		String errorContext = "Unknown";
 		EClass referenceType = eReference.getEReferenceType();
 		if (referenceType != null) {
 			errorContext = referenceType.getName();
 		}
 		return messageBinder.bind(csContext, messageTemplate, errorContext, linkText);
 	}	
 	
 	public static @Nullable CS2Pivot findAdapter(@NonNull ResourceSet resourceSet) {
 		return PivotUtil.getAdapter(CS2Pivot.class, resourceSet);
 	}
 
 	public static List<ILeafNode> getDocumentationNodes(@NonNull ICompositeNode node) {
 		List<ILeafNode> documentationNodes = null;
		for (ILeafNode leafNode : node.getLeafNodes()) {
			if (!leafNode.isHidden()) {
 				break;
 			}
 			EObject grammarElement = leafNode.getGrammarElement();
 			TerminalRule terminalRule = (TerminalRule) grammarElement;
 			String name = terminalRule.getName();
 			if ("WS".equals(name)) {
 			}
 			else if ("SL_COMMENT".equals(name)) {
 			}
 			else if ("ML_COMMENT".equals(name)) {
 				if (documentationNodes == null) {
 					documentationNodes = new ArrayList<ILeafNode>();
 				}
 				documentationNodes.add(leafNode);
 			}
 			else {
 				break;
 			}
 		}
 		return documentationNodes;
 	}
 	
 	public static MessageBinder getMessageBinder() {
 		return messageBinder;
 	}
 	
 	private static long startTime = System.currentTimeMillis();
 	private static @NonNull Map<Thread,Long> threadRunTimes = new HashMap<Thread,Long>();
 	private static long[] indentRunTimes = new long[100];
 	private static @NonNull Integer indentation = 0;
 	private static @NonNull String indents= ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
 
 	public static void printDiagnostic(@NonNull String message, boolean dispose, int indent) {
 		synchronized (indentation) {
 			if (indent < 0) {
 				indentation--;
 			}
 			long currentTimeMillis = System.currentTimeMillis();
 			Thread currentThread = Thread.currentThread();
 			Long threadStartTime = threadRunTimes.get(currentThread);
 			if (threadStartTime == null) {
 				threadStartTime = currentTimeMillis;
 				threadRunTimes.put(currentThread, threadStartTime);
 			}
 			if (indent > 0) {
 				System.out.printf("%s %8.3f %s -- %6.3f %s\n", indents.substring(0, Math.min(indentation, indents.length()-1)), (currentTimeMillis - startTime) * 0.001, currentThread.getName(),
 					(currentTimeMillis - threadStartTime) * 0.001, message);
 			}
 			else {
 				System.out.printf("%s %8.3f %s -- %6.3f %6.3f %s\n", indents.substring(0, Math.min(indentation, indents.length()-1)), (currentTimeMillis - startTime) * 0.001, currentThread.getName(),
 					(currentTimeMillis - threadStartTime) * 0.001, (currentTimeMillis - indentRunTimes[indentation]) * 0.001, message);
 			}
 			if (dispose) {
 				threadRunTimes.remove(currentThread);
 			}
 			if (indent > 0) {
 				indentRunTimes[indentation] = currentTimeMillis;
 				indentation++;
 			}
 		}
 	}
 	
 	public static void setElementType(@NonNull PathNameCS pathNameCS, /*@NonNull*/ EClass elementType, @NonNull ElementCS csContext, @Nullable ScopeFilter scopeFilter) {
 		assert elementType != null;
 		pathNameCS.setContext(csContext);
 		pathNameCS.setScopeFilter(scopeFilter);
 		List<PathElementCS> path = pathNameCS.getPath();
 		int iMax = path.size()-1;
 		path.get(iMax).setElementType(elementType);
 		if (PivotPackage.Literals.FEATURE.isSuperTypeOf(elementType) && (iMax > 0)) {
 			path.get(--iMax).setElementType(PivotPackage.Literals.TYPE);
 		}
 		for (int i = 0; i < iMax; i++) {
 			path.get(i).setElementType(PivotPackage.Literals.NAMESPACE);
 		}
 	}
 	
 	/**
 	 * Define an alternative message binder. THe default null messageBinder uses
 	 * {@link NLS.bind} 
 	 */
 	public static MessageBinder setMessageBinder(MessageBinder messageBinder) {
 		MessageBinder savedMessageBinder = CS2Pivot.messageBinder;
 		CS2Pivot.messageBinder = messageBinder;
 		return savedMessageBinder;
 	}
 	
 	/**
 	 * Mapping of each CS resource to its corresponding pivot Resource.
 	 */
 	protected final @NonNull Map<? extends Resource, ? extends Resource> cs2pivotResourceMap;
 
 	/**
 	 * CS to Pivot mapping controller for aliases and CSIs.
 	 */
 	protected final @NonNull CSI2PivotMapping cs2PivotMapping;
 
 	/**
 	 * A lazily created inverse map that may be required for navigation from an outline.
 	 */
 	private Map<Element, ModelElementCS> pivot2cs = null;
 
 	public CS2Pivot(@NonNull Map<? extends Resource, ? extends Resource> cs2pivotResourceMap, @NonNull MetaModelManager metaModelManager) {
 		super(metaModelManager);
 		this.cs2PivotMapping = CSI2PivotMapping.getAdapter(metaModelManager);
 		this.cs2pivotResourceMap = cs2pivotResourceMap;
 		metaModelManager.addListener(this);
 		metaModelManager.getPivotResourceSet().eAdapters().add(this);
 	}
 	
 	protected CS2Pivot(@NonNull CS2Pivot aConverter) {
 		super(aConverter.metaModelManager);
 		this.cs2pivotResourceMap = aConverter.cs2pivotResourceMap;
 		this.cs2PivotMapping = CSI2PivotMapping.getAdapter(metaModelManager);
 	}
 
 	public @NonNull String bind(@NonNull EObject csContext, /*@NonNull*/ String messageTemplate, Object... bindings) {
 		assert messageTemplate != null;
 		return messageBinder.bind(csContext, messageTemplate, bindings);
 	}
 
 	public @NonNull Map<Element, ModelElementCS> computePivot2CSMap() {
 		Map<Element, ModelElementCS> map = new HashMap<Element, ModelElementCS>();
 		for (Resource csResource : cs2pivotResourceMap.keySet()) {
 			for (Iterator<EObject> it = csResource.getAllContents(); it.hasNext(); ) {
 				EObject eObject = it.next();
 				if (eObject instanceof ModelElementCS) {
 					ModelElementCS csElement = (ModelElementCS)eObject;
 					Element pivotElement = csElement.getPivot();
 					map.put(pivotElement, csElement);
 				}
 			}
 		}
 		return map;
 	}
 	
 	protected abstract @NonNull BaseCSVisitor<Continuation<?>> createContainmentVisitor(@NonNull CS2PivotConversion cs2PivotConversion);
 
 	protected@NonNull  CS2PivotConversion createConversion(@NonNull IDiagnosticConsumer diagnosticsConsumer, @NonNull Collection<? extends Resource> csResources) {
 		return new CS2PivotConversion(this, diagnosticsConsumer, csResources);
 	}
 
 	protected abstract @NonNull BaseCSVisitor<Element> createLeft2RightVisitor(@NonNull CS2PivotConversion cs2PivotConversion);
 	protected abstract @NonNull BaseCSVisitor<Continuation<?>> createPostOrderVisitor(@NonNull CS2PivotConversion converter) ;
 	protected abstract @NonNull BaseCSVisitor<Continuation<?>> createPreOrderVisitor(@NonNull CS2PivotConversion converter);
 
 	public void dispose() {
 		cs2pivotResourceMap.clear();
 		cs2PivotMapping.clear();
 		pivot2cs = null;
 		metaModelManager.getPivotResourceSet().eAdapters().remove(this);
 		metaModelManager.removeListener(this);
 	}
 
 	public ModelElementCS getCSElement(@NonNull Element pivotElement) {
 		if (pivot2cs == null) {
 			pivot2cs = computePivot2CSMap();
 		}
 		return pivot2cs.get(pivotElement);
 	}
 
 	public @NonNull Collection<? extends Resource> getCSResources() {
 		@SuppressWarnings("null") @NonNull Set<? extends Resource> keySet = cs2pivotResourceMap.keySet();
 		return keySet;
 	}
 
 	public Element getPivotElement(@NonNull ModelElementCS csElement) {
 		return cs2PivotMapping.get(csElement);
 	}
 
 	public @Nullable <T extends Element> T getPivotElement(@NonNull Class<T> pivotClass, @NonNull ModelElementCS csElement) {
 		Element pivotElement = cs2PivotMapping.get(csElement);
 		if (pivotElement == null) {
 			return null;
 		}
 		if (!pivotClass.isAssignableFrom(pivotElement.getClass())) {
 			throw new ClassCastException(pivotElement.getClass().getName() + " is not assignable to " + pivotClass.getName());
 		}
 		@SuppressWarnings("unchecked")
 		T castElement = (T) pivotElement;
 		return castElement;
 	}
 
 	public Resource getPivotResource(@NonNull Resource csResource) {
 		return cs2pivotResourceMap.get(csResource);
 	}
 
 	public Collection<? extends Resource> getPivotResources() {
 		return metaModelManager.getPivotResourceSet().getResources();//cs2pivotResourceMap.values();
 	}
 
 	public Notifier getTarget() {
 		return metaModelManager.getPivotResourceSet();
 	}
 	
 	/**
 	 * Install the mapping from a CS element that defines a pivot element to the defined pivot element. The definition
 	 * is 'owned' by the CS element, so if the CS element vanishes, so does the pivot element.
 	 */
 	public void installPivotDefinition(@NonNull ModelElementCS csElement, @NonNull Element newPivotElement) {
 	//	logger.trace("Installing " + csElement.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$	
 		EObject oldPivotElement = csElement.getPivot();	
 		if (oldPivotElement != newPivotElement) {
 			assert !newPivotElement.eIsProxy();
 			csElement.setPivot(newPivotElement);
 			if (oldPivotElement != null) {
 				// WIP Queue dead element
 			}
 		}
 		cs2PivotMapping.put(csElement, newPivotElement);
 	}
 	
 	/**
 	 * Install the mapping from a CS element to a completely independent pivot element. If the pivot element vanishes, the
 	 * reference is stale, if the CS element the pivot element is less referenced.
 	 */
 	public void installPivotReference(@NonNull ElementRefCS csElement, @NonNull Element newPivotElement, @NonNull EReference eReference) {
 		assert eReference.getEContainingClass().isSuperTypeOf(csElement.eClass());
 	//	logger.trace("Installing " + csElement.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$	
 		EObject oldPivotElement = csElement.getPivot();	
 		if (oldPivotElement != newPivotElement) {
 			assert !newPivotElement.eIsProxy();
 			csElement.setPivot(newPivotElement);
 		}
 	}
 	
 	/**
 	 * Install the mapping from a CS element to a related pivot element. This normally arises when more than one CS element
 	 * are associated with a single pivot element. In this case one of the CS elements is the defining CS element and the
 	 * others are users.
 	 */
 	public void installPivotUsage(@NonNull ModelElementCS csElement, @NonNull Element newPivotElement) {
 	//	logger.trace("Installing " + csElement.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$	
 		EObject oldPivotElement = csElement.getPivot();	
 		if (oldPivotElement != newPivotElement) {
 			assert !newPivotElement.eIsProxy();
 			csElement.setPivot(newPivotElement);
 		}
 	}
 
 	public boolean isAdapterForType(Object type) {
 		if (type instanceof Class<?>) {
 			return ((Class<?>)type).isAssignableFrom(getClass());
 		}
 		else {
 			return false;
 		}
 	}
 
 	public boolean isAdapterFor(@NonNull MetaModelManager metaModelManager) {
 		return this.metaModelManager == metaModelManager;
 	}
 
 	public void metaModelManagerDisposed(@NonNull MetaModelManager metaModelManager) {
 		dispose();
 	}
 
 	public void notifyChanged(Notification notification) {
 		// Do nothing.
 	}
 	
 	public @Nullable <T extends Element> T refreshModelElement(@NonNull Class<T> pivotClass, @NonNull EClass pivotEClass, @Nullable ModelElementCS csElement) {
 		Element pivotElement = csElement != null ? getPivotElement(csElement) : null;
 		@NonNull Element pivotElement2;
 		if ((pivotElement == null) || (pivotEClass != pivotElement.eClass())) {
 //			logger.trace("Recreating " + pivotEClass.getName() + " : " + moniker); //$NON-NLS-1$ //$NON-NLS-2$
 			@SuppressWarnings("null") @NonNull Element pivotElement3 = (Element) pivotEClass.getEPackage().getEFactoryInstance().create(pivotEClass);
 			pivotElement2 = pivotElement3;
 		}
 		else {
 			pivotElement2 = pivotElement;
 		}
 		if (csElement != null) {
 			installPivotDefinition(csElement, pivotElement2);
 		}
 		@SuppressWarnings("unchecked")
 		T castElement = (T) pivotElement2;
 		return castElement;
 	}
 
 	public void setTarget(Notifier newTarget) {
 		assert newTarget == metaModelManager.getPivotResourceSet();
 	}
 
 	public void unsetTarget(Notifier oldTarget) {
 		assert oldTarget == metaModelManager.getPivotResourceSet();
 	}
 	
 	public synchronized void update(@NonNull IDiagnosticConsumer diagnosticsConsumer) {
 //		printDiagnostic("CS2Pivot.update start", false, 0);
 		@SuppressWarnings("unused") Map<String, Element> oldCSI2Pivot = cs2PivotMapping.getMapping();
 		@SuppressWarnings("unused") Set<String> newCSIs = cs2PivotMapping.computeCSIs(cs2pivotResourceMap.keySet());
 //		System.out.println("==========================================================================");
 		Collection<? extends Resource> csResources = getCSResources();
 //		for (Resource csResource : csResources) {
 //			System.out.println("CS " + csResource.getClass().getName() + "@" + csResource.hashCode() + " " + csResource.getURI());
 //		}
 		CS2PivotConversion conversion = createConversion(diagnosticsConsumer, csResources);
 		conversion.update();
 //		System.out.println("---------------------------------------------------------------------------");
 //		Collection<? extends Resource> pivotResources = cs2pivotResourceMap.values();
 //		for (Entry<? extends Resource, ? extends Resource> entry : cs2pivotResourceMap.entrySet()) {
 //			Resource csResource = entry.getKey();
 //			Resource pivotResource = entry.getValue();
 //			System.out.println("CS " + csResource.getClass().getName() + "@" + csResource.hashCode() + " => " + pivotResource.getClass().getName() + "@" + pivotResource.hashCode());
 //		}
 /*		Set<String> deadCSIs = new HashSet<String>(oldCSI2Pivot.keySet());
 		deadCSIs.removeAll(newCSIs);
 		for (String deadCSI : deadCSIs) {
 			Element deadPivot = oldCSI2Pivot.get(deadCSI);	// WIP
 //			metaModelManager.kill(deadPivot);
 		} */
 		conversion.garbageCollect(cs2pivotResourceMap);
 		cs2PivotMapping.update(cs2pivotResourceMap.keySet());
 		pivot2cs = null;
 //		printDiagnostic("CS2Pivot.update end", false, 0);
 	}
 }
