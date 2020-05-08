 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.core;
 
 import static org.jboss.tools.ws.jaxrs.core.WorkbenchUtils.changeAnnotationValue;
 import static org.jboss.tools.ws.jaxrs.core.WorkbenchUtils.resolveAnnotation;
 import static org.jboss.tools.ws.jaxrs.core.WorkbenchUtils.resolveAnnotations;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.APPLICATION_PATH;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.CONSUMES;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.ENCODED;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.HTTP_METHOD;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.PATH;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.PRODUCES;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.RETENTION;
 import static org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname.TARGET;
 import static org.mockito.Mockito.spy;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.internal.core.JavaProject;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsElementFactory;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsHttpMethod;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsJavaApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsMetamodel;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsProvider;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResource;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResourceField;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsResourceMethod;
 import org.jboss.tools.ws.jaxrs.core.internal.metamodel.domain.JaxrsWebxmlApplication;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.WtpUtils;
 import org.jboss.tools.ws.jaxrs.core.jdt.Annotation;
 import org.jboss.tools.ws.jaxrs.core.jdt.CompilationUnitsRepository;
 import org.jboss.tools.ws.jaxrs.core.jdt.EnumJaxrsClassname;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodParameter;
 import org.jboss.tools.ws.jaxrs.core.jdt.JavaMethodSignature;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
 import org.jboss.tools.ws.jaxrs.core.metamodel.EnumElementKind;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.TestRule;
 import org.junit.rules.TestWatcher;
 import org.junit.runner.Description;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Made abstract, so won't be automatically picked up as test (since intended to
  * be subclassed).
  * 
  * Based on
  * http://dev.eclipse.org/viewcvs/index.cgi/incubator/sourceediting/tests
  * /org.eclipse
  * .wst.xsl.ui.tests/src/org/eclipse/wst/xsl/ui/tests/AbstractXSLUITest
  * .java?revision=1.2&root=WebTools_Project&view=markup
  */
 @RunWithProject("org.jboss.tools.ws.jaxrs.tests.sampleproject")
 @SuppressWarnings("restriction")
 public abstract class AbstractCommonTestCase {
 
 	@SuppressWarnings("unused")
 	private static final String M2_REPO = "M2_REPO";
 
 	public static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommonTestCase.class);
 
 	protected String projectName = null;
 
 	protected IJavaProject javaProject;
 
 	protected IProject project;
 
 	public final static String DEFAULT_SAMPLE_PROJECT_NAME = WorkbenchUtils
 			.retrieveSampleProjectName(AbstractCommonTestCase.class);
 
 	private ProjectSynchronizator synchronizor;
 
 	@Rule
 	public TestRule watchman = new TestWatcher() {
 		@Override
 		public void starting(Description description) {
 			LOGGER.info("**********************************************************************************");
 			LOGGER.info("Starting test '{}'...", description.getMethodName());
 			LOGGER.info("**********************************************************************************");
 		}
 
 		@Override
 		public void finished(Description description) {
 			LOGGER.info("**********************************************************************************");
 			LOGGER.info("Test '{}' finished.", description.getMethodName());
 			LOGGER.info("**********************************************************************************");
 		}
 	};
 
 	@BeforeClass
 	public static void setupWorkspace() throws Exception {
 		// org.eclipse.jdt.core.JavaCore.getPlugin().start(bundle.getBundleContext());
 		long startTime = new Date().getTime();
 		try {
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			if (workspace.isAutoBuilding()) {
 				IWorkspaceDescription description = workspace.getDescription();
 				description.setAutoBuilding(false);
 				workspace.setDescription(description);
 			}
 
 			workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
 			LOGGER.info("Initial Synchronization (@BeforeClass)");
 			WorkbenchTasks.syncSampleProject(DEFAULT_SAMPLE_PROJECT_NAME);
 		} finally {
 			long endTime = new Date().getTime();
 			LOGGER.info("Initial Workspace setup in " + (endTime - startTime) + "ms.");
 		}
 	}
 
 	@BeforeClass
 	public static void unregistrerListeners() {
 		JBossJaxrsCorePlugin.getDefault().pauseListeners();
 	}
 
 	@Before
 	public void bindSampleProject() throws Exception {
 		long startTime = new Date().getTime();
 		try {
 			JBossJaxrsCorePlugin.getDefault().pauseListeners();
 			projectName = WorkbenchUtils.retrieveSampleProjectName(this.getClass());
 			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 			project.open(new NullProgressMonitor());
 			javaProject = JavaCore.create(project);
 			javaProject.open(new NullProgressMonitor());
 			Assert.assertNotNull("JavaProject not found", javaProject.exists());
 			Assert.assertNotNull("Project not found", javaProject.getProject().exists());
 			Assert.assertTrue("Project is not a JavaProject", JavaProject.hasJavaNature(javaProject.getProject()));
 			synchronizor = new ProjectSynchronizator();
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			workspace.addResourceChangeListener(synchronizor);
 		} finally {
 			long endTime = new Date().getTime();
 			LOGGER.info("Test Workspace setup in " + (endTime - startTime) + "ms.");
 		}
	}

	@Before
	public void clearCompilationUnitsRepository() {
 		CompilationUnitsRepository.getInstance().clear();
	}

	protected JaxrsMetamodel metamodel;
 
	@Before
	public void setup() throws CoreException {
 		JBossJaxrsCorePlugin.getDefault().pauseListeners();
 		// metamodel = Mockito.mock(JaxrsMetamodel);
 		// in case an element was attempted to be removed, some impact would be
 		// retrieved
 		// when(metamodel.remove(any(JaxrsElement))).thenReturn(true);
 		metamodel = spy(JaxrsMetamodel.create(javaProject));
 		// replace the normal metamodel instance with the one spied by Mockito
 		javaProject.getProject().setSessionProperty(JaxrsMetamodel.METAMODEL_QUALIFIED_NAME, metamodel);
 	}
 
 	@After
 	public void removeResourceChangeListener() throws CoreException, InvocationTargetException, InterruptedException {
 		long startTime = new Date().getTime();
 		try {
 			LOGGER.info("Synchronizing the workspace back to its initial state...");
 			// remove listener before sync' to avoid desync...
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			workspace.removeResourceChangeListener(synchronizor);
 			synchronizor.resync();
 		} finally {
 			long endTime = new Date().getTime();
 			LOGGER.info("Test Workspace sync'd in " + (endTime - startTime) + "ms.");
 		}
 	}
 
 	protected IType resolveType(String typeName) throws CoreException {
 		return JdtUtils.resolveType(typeName, javaProject, new NullProgressMonitor());
 	}
 
 	/**
 	 * Creates a java annotated type based JAX-RS Application element
 	 * @param isApplicationSubtype TODO
 	 * @param type
 	 * @param applicationPath
 	 * 
 	 * @return
 	 * @throws CoreException
 	 */
 	protected JaxrsJavaApplication createJavaApplication(String typeName) throws CoreException {
 		return createJavaApplication(typeName, true);
 		
 	}
 	/**
 	 * Creates a java annotated type based JAX-RS Application element
 	 * @param isApplicationSubtype TODO
 	 * @param type
 	 * @param applicationPath
 	 * 
 	 * @return
 	 * @throws CoreException
 	 */
 	protected JaxrsJavaApplication createJavaApplication(String typeName, boolean isApplicationSubtype) throws CoreException {
 		final IType type = resolveType(typeName);
 		final Map<String, Annotation> annotations = resolveAnnotations(type, APPLICATION_PATH.qualifiedName, SuppressWarnings.class.getName());
 		final JaxrsJavaApplication application = new JaxrsJavaApplication(type, annotations, isApplicationSubtype,
 				metamodel);
 		metamodel.add(application);
 		return application;
 
 	}
 
 	/**
 	 * Creates a java annotated type based JAX-RS Application element
 	 * 
 	 * @param type
 	 * @param applicationPath
 	 * @return
 	 * @throws CoreException
 	 */
 	protected JaxrsJavaApplication createJavaApplication(String typeName, String applicationPath) throws CoreException {
 		final JaxrsJavaApplication application = createJavaApplication(typeName, true);
 		final Annotation applicationPathAnnotation = application.getAnnotation(APPLICATION_PATH.qualifiedName);
 		application.addOrUpdateAnnotation(changeAnnotationValue(applicationPathAnnotation, applicationPath));
 		return application;
 	}
 
 	/**
 	 * Creates a web.xml based JAX-RS Application element
 	 * 
 	 * @param applicationPath
 	 * @return
 	 * @throws JavaModelException
 	 */
 	protected JaxrsWebxmlApplication createWebxmlApplication(final String applicationClassName,
 			final String applicationPath) throws JavaModelException {
 		final IResource webDeploymentDescriptor = WtpUtils.getWebDeploymentDescriptor(project);
 		final JaxrsWebxmlApplication webxmlApplication = new JaxrsWebxmlApplication(applicationClassName,
 				applicationPath, webDeploymentDescriptor, metamodel);
 		metamodel.add(webxmlApplication);
 		return webxmlApplication;
 	}
 
 	protected JaxrsProviderBuilder providerBuilder(final String typeName) throws CoreException {
 		return new JaxrsProviderBuilder(typeName);
 	}
 
 	/**
 	 * 'Provider' element builder.
 	 */
 	public class JaxrsProviderBuilder {
 		final IType javaType;
 		private final Map<String, Annotation> annotations = new HashMap<String, Annotation>();
 		private final Map<EnumElementKind, IType> providedTypes = new HashMap<EnumElementKind, IType>();
 
 		public JaxrsProviderBuilder(final String typeName) throws CoreException {
 			this.javaType = resolveType(typeName);
 		}
 
 		public JaxrsProviderBuilder providedType(final EnumElementKind kind, IType providedType) {
 			this.providedTypes.put(kind, providedType);
 			return this;
 		}
 
 		public JaxrsProviderBuilder providedType(final EnumElementKind kind, String providedTypeName) throws CoreException {
 			this.providedTypes.put(kind, resolveType(providedTypeName));
 			return this;
 		}
 
 		public JaxrsProviderBuilder providedTypes(final Map<EnumElementKind, IType> providedTypes) {
 			this.providedTypes.putAll(providedTypes);
 			return this;
 		}
 		
 		public JaxrsProviderBuilder annotation(final Annotation annotation) throws JavaModelException {
 			final Annotation resolvedAnnotation = resolveAnnotation(javaType, annotation.getFullyQualifiedName());
 			// support for value override
 			if(annotation.getValue() != null && resolvedAnnotation.getValue() != null && !annotation.getValue().equals(resolvedAnnotation.getValue())) {
 				resolvedAnnotation.update(annotation);
 			}
 			this.annotations.put(annotation.getFullyQualifiedName(), resolvedAnnotation);
 			return this;
 		}
 
 		public JaxrsProviderBuilder annotations(final Map<String, Annotation> annotations) {
 			this.annotations.putAll(annotations);
 			return this;
 		}
 
 		public JaxrsProvider build() {
 			final JaxrsProvider jaxrsProvider = new JaxrsProvider(javaType, annotations, providedTypes, metamodel);
 			metamodel.add(jaxrsProvider);
 			return jaxrsProvider;
 		}
 
 	}
 
 	/**
 	 * @return
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	protected JaxrsHttpMethod createHttpMethod(EnumJaxrsClassname httpMethodElement) throws CoreException,
 			JavaModelException {
 		return createHttpMethod(httpMethodElement.qualifiedName);
 	}
 	
 	protected JaxrsHttpMethod createHttpMethod(String typeName) throws CoreException, JavaModelException {
 		final IType type = resolveType(typeName);
 		return createHttpMethod(type);
 	}
 
 	protected JaxrsHttpMethod createHttpMethod(String typeName, String httpVerb) throws CoreException, JavaModelException {
 		final IType type = resolveType(typeName);
 		return createHttpMethod(type, httpVerb);
 	}
 
 	protected JaxrsHttpMethod createHttpMethod(IType type) throws JavaModelException {
 		final CompilationUnit ast = JdtUtils.parse(type.getCompilationUnit(), null);
 		final Map<String, Annotation> annotations = JdtUtils.resolveAnnotations(type, ast, HTTP_METHOD.qualifiedName,
 				TARGET.qualifiedName, RETENTION.qualifiedName, SuppressWarnings.class.getName());
 		final JaxrsHttpMethod httpMethod = new JaxrsHttpMethod(type, annotations, metamodel);
 		metamodel.add(httpMethod);
 		return httpMethod;
 	}
 
 	protected JaxrsHttpMethod createHttpMethod(IType type, String httpVerb) throws JavaModelException {
 		final Map<String, Annotation> annotations = resolveAnnotations(type, HTTP_METHOD.qualifiedName, TARGET.qualifiedName, RETENTION.qualifiedName);
 		final Annotation httpMethodAnnotation = annotations.get(HTTP_METHOD.qualifiedName);
 		annotations.put(HTTP_METHOD.qualifiedName, changeAnnotationValue(httpMethodAnnotation, httpVerb));
 		final JaxrsHttpMethod httpMethod = new JaxrsHttpMethod(type, annotations, metamodel);
 		metamodel.add(httpMethod);
 		return httpMethod;
 	}
 	
 	protected JaxrsProvider createProvider(final String typeName) throws JavaModelException, CoreException {
 		final IType type = resolveType(typeName);
 		return createProvider(type);
 	}
 
 	protected JaxrsProvider createProvider(final IType type) throws JavaModelException, CoreException {
 		final JaxrsProvider provider = JaxrsElementFactory.createProvider(type, JdtUtils.parse(type, new NullProgressMonitor()), metamodel, new NullProgressMonitor());
 		metamodel.add(provider);
 		return provider;
 	}
 
 	/**
 	 * Creates the JAX-RS Resource for the given type, but
 	 * <strong>WITHOUT</strong> its Method nor its Field children.
 	 * 
 	 * @param typeName
 	 * @return
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	protected JaxrsResource createSimpleResource(String typeName) throws CoreException, JavaModelException {
 		return createSimpleResource(typeName, PATH.qualifiedName, CONSUMES.qualifiedName, PRODUCES.qualifiedName, ENCODED.qualifiedName);
 	}
 	
 	protected JaxrsResource createSimpleResource(String typeName, String... annotationNames) throws CoreException, JavaModelException {
 		final IType type = resolveType(typeName);
 		final CompilationUnit ast = JdtUtils.parse(type.getCompilationUnit(), new NullProgressMonitor());
 		final Map<String, Annotation> annotations = JdtUtils.resolveAnnotations(type, ast, annotationNames);
 		final JaxrsResource resource = new JaxrsResource(type, annotations, metamodel);
 		metamodel.add(resource);
 		return resource;
 	}
 
 	protected JaxrsResource createFullResource(String typeName) throws CoreException, JavaModelException {
 		final IType type = JdtUtils.resolveType(typeName, javaProject, new NullProgressMonitor());
 		final JaxrsResource resource = JaxrsElementFactory.createResource(type,
 				JdtUtils.parse(type.getCompilationUnit(), null), metamodel);
 		metamodel.add(resource);
 		return resource;
 	}
 	
 	protected Annotation createAnnotation(String className) {
 		return createAnnotation(null, className, null);
 	}
 
 	protected Annotation createAnnotation(String className, String value) {
 		return createAnnotation(null, className, value);
 	}
 
 	protected Annotation createAnnotation(IAnnotation annotation, String name, String value) {
 		Map<String, List<String>> values = new HashMap<String, List<String>>();
 		values.put("value", Arrays.asList(value));
 		return new Annotation(annotation, name, values);
 	}
 
 	
 	protected JaxrsResourceMethod createResourceMethod(String methodName, JaxrsResource parentResource,
 			String... annotationNames) throws CoreException, JavaModelException {
 		final IType javaType = parentResource.getJavaElement();
 		final ICompilationUnit compilationUnit = javaType.getCompilationUnit();
 		final IMethod javaMethod = getMethod(javaType, methodName);
 		final JavaMethodSignature methodSignature = JdtUtils.resolveMethodSignature(javaMethod,
 				JdtUtils.parse(compilationUnit, new NullProgressMonitor()));
 		final Map<String, Annotation> annotations = resolveAnnotations(javaMethod, (annotationNames.length > 0 ? annotationNames : new String[]{PATH.qualifiedName, CONSUMES.qualifiedName, PRODUCES.qualifiedName}));
 		final IType returnedType = methodSignature.getReturnedType();
 		final List<JavaMethodParameter> methodParameters = methodSignature.getMethodParameters();
 		final JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(javaMethod, methodParameters, returnedType, annotations, parentResource, metamodel);
 		metamodel.add(resourceMethod);
 		return resourceMethod;
 	}
 	
 	protected JaxrsResourceMethodBuilder resourceMethodBuilder(JaxrsResource parentResource, String methodName) throws JavaModelException {
 		JaxrsResourceMethodBuilder builder = new JaxrsResourceMethodBuilder(methodName, parentResource);
 		return builder;
 	}
 	
 	public class JaxrsResourceMethodBuilder {
 
 		private final IMethod javaMethod;
 		private final JaxrsResource parentResource;
 		private final Map<String, Annotation> annotations = new HashMap<String, Annotation>();
 		private final List<JavaMethodParameter> javaMethodParameters = new ArrayList<JavaMethodParameter>();
 		private IType returnedType;
 		
 		public JaxrsResourceMethodBuilder(String methodName, JaxrsResource parentResource) throws JavaModelException {
 			this.parentResource = parentResource;
 			this.javaMethod = getMethod(parentResource.getJavaElement(), methodName);
 		}
 		
 		public JaxrsResourceMethodBuilder annotation(String annotationName) throws JavaModelException {
 			annotations.putAll(resolveAnnotations(javaMethod, annotationName));
 			return this;
 		}
 
 		public JaxrsResourceMethodBuilder annotation(String annotationName, String annotationValueOverride) throws JavaModelException {
 			final Map<String, Annotation> resolvedAnnotations = resolveAnnotations(javaMethod, annotationName);
 			final Annotation annotation = resolvedAnnotations.get(annotationName);
 			resolvedAnnotations.put(annotationName, changeAnnotationValue(annotation, annotationValueOverride));
 			this.annotations.putAll(resolvedAnnotations);
 			return this;
 		}
 		
 		public JaxrsResourceMethodBuilder methodParameter(String name, String type, Annotation... annotations) {
 			final JavaMethodParameter parameter = new JavaMethodParameter(name, type,
 					Arrays.asList(annotations));
 			this.javaMethodParameters.add(parameter);
 			return this;
 		}
 
 		public JaxrsResourceMethodBuilder returnedType(String typeName) throws CoreException {
 			this.returnedType = resolveType(typeName);
 			return this;
 		}
 
 		public JaxrsResourceMethod build() {
 			final JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(javaMethod, this.javaMethodParameters, this.returnedType, annotations, parentResource,  metamodel);
 			metamodel.add(resourceMethod);
 			return resourceMethod;
 		}
 	}
 
 	protected JaxrsResourceFieldBuilder resourceFieldBuilder(JaxrsResource parentResource, String fieldName) throws JavaModelException {
 		JaxrsResourceFieldBuilder builder = new JaxrsResourceFieldBuilder(fieldName, parentResource);
 		return builder;
 	}
 	
 	public class JaxrsResourceFieldBuilder {
 
 		private final IField javaField;
 		private final JaxrsResource parentResource;
 		private final Map<String, Annotation> annotations = new HashMap<String, Annotation>();
 		
 		public JaxrsResourceFieldBuilder(String fieldName, JaxrsResource parentResource) throws JavaModelException {
 			this.parentResource = parentResource;
 			this.javaField = parentResource.getJavaElement().getField(fieldName);
 		}
 		
 		public JaxrsResourceFieldBuilder annotation(String annotationName) throws JavaModelException {
 			annotations.putAll(resolveAnnotations(javaField, annotationName));
 			return this;
 		}
 
 		public JaxrsResourceFieldBuilder annotation(String annotationName, String annotationValueOverride) throws JavaModelException {
 			final Map<String, Annotation> resolvedAnnotations = resolveAnnotations(javaField, annotationName);
 			final Annotation annotation = resolvedAnnotations.get(annotationName);
 			resolvedAnnotations.put(annotationName, changeAnnotationValue(annotation, annotationValueOverride));
 			this.annotations.putAll(resolvedAnnotations);
 			return this;
 		}
 		
 		public JaxrsResourceField build() {
 			final JaxrsResourceField resourceField = new JaxrsResourceField(javaField, annotations, parentResource, metamodel);
 			metamodel.add(resourceField);
 			return resourceField;
 		}
 
 	}
 	protected IType getType(String typeName) throws CoreException {
 		return JdtUtils.resolveType(typeName, javaProject, null);
 	}
 
 	/**
 	 * @param type
 	 * @return
 	 * @throws JavaModelException
 	 */
 	protected IMethod getMethod(IType type, String name) throws JavaModelException {
 		for (IMethod method : type.getMethods()) {
 			if (method.getElementName().equals(name)) {
 				return method;
 			}
 		}
 		Assert.fail("Failed to locate method named '" + name + "'");
 		return null;
 	}
 
 	protected IPackageFragmentRoot getPackageFragmentRoot(String path) throws JavaModelException {
 		return WorkbenchUtils.getPackageFragmentRoot(javaProject, path,
 				new NullProgressMonitor());
 	}
 
 }
