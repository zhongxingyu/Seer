 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder;
 
 import static org.eclipse.jdt.core.IJavaElement.COMPILATION_UNIT;
 import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
 import static org.eclipse.jdt.core.IJavaElementDelta.ADDED;
 import static org.eclipse.jdt.core.IJavaElementDelta.CHANGED;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_ADDED_TO_CLASSPATH;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_AST_AFFECTED;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_CONTENT;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_FINE_GRAINED;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_REMOVED_FROM_CLASSPATH;
 import static org.eclipse.jdt.core.IJavaElementDelta.REMOVED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_PROBLEM_SOLVED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_SIGNATURE;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.compiler.IProblem;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.ConstantUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.jdt.CompilationUnitsRepository;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodSignature;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
 
 /** Scans and filters the IJavaElementDelta and IResourceDelta (including their
  * children and annotations) and returns a list of JavaElementChangedEvents that
  * match with the JavaElementChangedEventFilter rules.
  * 
  * @author xcoulon
  * @see @{IJavaElementDelta}
  * @see @{IResourceDelta}
  * @see @{JavaElementChangedEvent}
  * @see @{JavaElementChangedEventFilter} */
 public class ElementChangedEventScanner {
 
 	private static final String FLAGS_PREFIX = "F_";
 
 	private final JavaElementChangedEventFilter filter = new JavaElementChangedEventFilter();
 
 	private final CompilationUnitsRepository compilationUnitsRepository = CompilationUnitsRepository.getInstance();
 
 	public List<JavaElementChangedEvent> scanAndFilterEvent(Object event, IProgressMonitor progressMonitor)
 			throws CoreException {
 		try {
 			progressMonitor.beginTask("Analysing changes", 1);
 			if (event instanceof ElementChangedEvent) {
 				final String type = ConstantUtils.getStaticFieldName(ElementChangedEvent.class,
 						((ElementChangedEvent) event).getType());
 				Logger.debug("Some java elements changed... (kind= {})", type);
 				return scanDelta(((ElementChangedEvent) event).getDelta());
 			} else if (event instanceof IResourceChangeEvent) {
 				final String type = ConstantUtils.getStaticFieldName(ElementChangedEvent.class,
 						((IResourceChangeEvent) event).getType());
 				Logger.debug("Some resources changed... (kind= {})", type);
 				return scanDelta(((IResourceChangeEvent) event).getDelta());
 			}
 			return Collections.emptyList();
 		} finally {
 			progressMonitor.done();
 		}
 	}
 
 	private List<JavaElementChangedEvent> scanDelta(IResourceDelta delta) throws CoreException {
 		final List<JavaElementChangedEvent> events = new ArrayList<JavaElementChangedEvent>();
 		final IResource resource = delta.getResource();
 		if(resource.getType() == IResource.PROJECT && !((IProject)resource).isOpen()) {
 			// skip as the project is closed
 			return Collections.emptyList();
 		}
 		final boolean isJavaFile = resource.getType() == IResource.FILE && ("java").equals(resource.getFileExtension());
 		final boolean javaFileAdded = isJavaFile && delta.getKind() == ADDED;
 		final boolean javaFileWithMarkers = isJavaFile && delta.getKind() == CHANGED
 				&& (delta.getFlags() & IResourceDelta.MARKERS) != 0;
 		final boolean javaFileRemoved = isJavaFile && delta.getKind() == REMOVED;
 		if ((javaFileAdded || javaFileRemoved)) {
			Logger.debug("File {}  {}", resource,
 					ConstantUtils.getStaticFieldName(IResourceDelta.class, delta.getKind()));
 			ICompilationUnit compilationUnit = JdtUtils.getCompilationUnit(delta.getResource());
 			CompilationUnit compilationUnitAST = compilationUnitsRepository.getAST(compilationUnit);
 			JavaElementChangedEvent event = new JavaElementChangedEvent(compilationUnit, delta.getKind(),
 					compilationUnitAST, new int[0]);
			
 			if (filter.apply(event)) {
 				events.add(event);
			} else {
				Logger.debug("Event {} **rejected** ", event);
 			}
 		} else if (javaFileWithMarkers) {
 			ICompilationUnit compilationUnit = JdtUtils.getCompilationUnit(delta.getResource());
 			IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
 			for (IMarkerDelta markerDelta : markerDeltas) {
 				int severity = markerDelta.getAttribute(IMarker.SEVERITY, 0);
 				String type = markerDelta.getType();
 				String message = markerDelta.getAttribute(IMarker.MESSAGE, "");
 				if (severity == IMarker.SEVERITY_ERROR && type.equals("org.eclipse.jdt.core.problem")) {
 					Logger.debug("Marker delta: {} [{}] {}: \"{}\" at line {} (id={})", markerDelta.getResource()
 							.getName(), ConstantUtils.getStaticFieldName(IResourceDelta.class, markerDelta.getKind()),
 							ConstantUtils.getStaticFieldName(IMarker.class, severity, "SEVERITY_"), message,
 							markerDelta.getAttribute(IMarker.LINE_NUMBER), markerDelta.getId());
 					IJavaElement element = compilationUnit
 							.getElementAt(markerDelta.getAttribute(IMarker.CHAR_START, 0));
 					// skip if no element could be found at the given location
 					// (eg : removed element)
 					if (element == null) {
 						continue;
 					}
 					int flag = markerDelta.getKind() == IResourceDelta.ADDED ? IJavaElementDeltaFlag.F_MARKER_ADDED
 							: IJavaElementDeltaFlag.F_MARKER_REMOVED;
 					CompilationUnit compilationUnitAST = compilationUnitsRepository.getAST(compilationUnit);
 					if (compilationUnitAST != null) {
 						JavaElementChangedEvent event = new JavaElementChangedEvent(element, CHANGED,
 								compilationUnitAST, new int[] { flag });
 						if (filter.apply(event)) {
 							events.add(event);
 						}
 					}
 				}
 			}
 		}
 		for (IResourceDelta childDelta : delta.getAffectedChildren()) {
 			events.addAll(scanDelta(childDelta));
 		}
 		return events;
 	}
 
 	/** Recursively analyse delta and notifies the next listeners.
 	 * 
 	 * @param delta
 	 * @throws CoreException
 	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=100267 */
 	private List<JavaElementChangedEvent> scanDelta(IJavaElementDelta delta) throws CoreException {
 		final List<JavaElementChangedEvent> events = new ArrayList<JavaElementChangedEvent>();
 		IJavaElement element = delta.getElement();
 		if(element == null || (element.getJavaProject() != null && !element.getJavaProject().getProject().isOpen())) {
 			// skip as the project is closed
 			return Collections.emptyList();
 		}
 
 		int elementKind = element.getElementType(); // retrieveJavaElementKind(delta);
 		int deltaKind = retrieveDeltaKind(delta);
 		int[] flags = retrieveFlags(delta);
 		CompilationUnit compilationUnitAST = null;
 		final CompilationUnitsRepository astRepository = CompilationUnitsRepository.getInstance();
 		if (elementKind == COMPILATION_UNIT) {
 			ICompilationUnit compilationUnit = (ICompilationUnit) element;
 			switch (deltaKind) {
 			case CHANGED:
 				if (compilationUnit.isWorkingCopy()) {
 					compilationUnitAST = delta.getCompilationUnitAST();
 					if (compilationUnitAST == null) {
 						compilationUnitAST = JdtUtils.parse(compilationUnit, new NullProgressMonitor());
 					}
 				} else {
 					compilationUnitAST = astRepository.getAST(compilationUnit);
 				}
 
 				if (compilationUnit.isWorkingCopy()) {
 					// TODO : implement method, test all AstRepository methods
 					// TODO: test when problems are solved
 
 					final IProblem[] problems = compilationUnitAST.getProblems();
 					Map<IProblem, IJavaElement> solvedProblems = astRepository.mergeProblems(compilationUnit, problems);
 					for (Entry<IProblem, IJavaElement> solvedProblem : solvedProblems.entrySet()) {
 						IJavaElement solvedElement = solvedProblem.getValue();
 						final JavaElementChangedEvent event = new JavaElementChangedEvent(solvedElement, CHANGED,
 								compilationUnitAST, new int[] { F_PROBLEM_SOLVED });
 						if (filter.apply(event)) {
 							events.add(event);
 						}
 					}
 					boolean computeDiffs = requiresDiffsComputation(flags);
 					List<JavaMethodSignature> diffs = astRepository.mergeAST(compilationUnit, compilationUnitAST,
 							computeDiffs);
 					for (JavaMethodSignature diff : diffs) {
 						final JavaElementChangedEvent event = new JavaElementChangedEvent(diff.getJavaMethod(),
 								CHANGED, compilationUnitAST, new int[] { F_SIGNATURE });
 						if (filter.apply(event)) {
 							events.add(event);
 						}
 					}
 
 				}
 				break;
 			case REMOVED:
 				astRepository.removeAST(compilationUnit);
 				break;
 			}
 
 			// compilationUnitAST = astRepository.getAST(compilationUnit);
 		} else {
 			compilationUnitAST = astRepository.getAST(element.getResource());
 		}
 		final JavaElementChangedEvent event = new JavaElementChangedEvent(element, deltaKind, compilationUnitAST, flags);
 		if (filter.apply(event)) {
 			events.add(event);
 		}
 		// carry on with children elements.
 		for (IJavaElementDelta affectedChild : delta.getAffectedChildren()) {
 			events.addAll(scanDelta(affectedChild));
 		}
 		for (IJavaElementDelta annotation : delta.getAnnotationDeltas()) {
 			events.addAll(scanDelta(annotation));
 		}
 		/*
 		 * final IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
 		 * if (resourceDeltas != null) {
 		 * for (IResourceDelta resourceDelta : resourceDeltas) {
 		 * events.addAll(scanDelta(resourceDelta));
 		 * }
 		 * }
 		 */
 		return events;
 	}
 
 	/** @param flags
 	 * @return */
 	private boolean requiresDiffsComputation(int[] flags) {
 		return Arrays.equals(flags, new int[] { F_CONTENT, F_FINE_GRAINED, F_AST_AFFECTED });
 	}
 
 	/** @param delta
 	 * @return */
 	private static int[] retrieveFlags(IJavaElementDelta delta) {
 		return ConstantUtils.splitConstants(IJavaElementDelta.class, delta.getFlags(), FLAGS_PREFIX);
 	}
 
 	/** Retrieves the appropriate kind of the given delta, with some specific
 	 * adaptations for some element types.
 	 * 
 	 * @param delta
 	 *            the delta.
 	 * @return the delta kind.
 	 * @see {@link IJavaElementDelta} */
 	private static int retrieveDeltaKind(IJavaElementDelta delta) {
 		IJavaElement element = delta.getElement();
 		int elementType = element.getElementType();
 		int flags = delta.getFlags();
 		switch (elementType) {
 		case PACKAGE_FRAGMENT_ROOT:
 			switch (flags) {
 			case F_ADDED_TO_CLASSPATH:
 				return ADDED;
 			case F_REMOVED_FROM_CLASSPATH:
 				return REMOVED;
 			}
 		default:
 			return delta.getKind();
 		}
 	}
 }
