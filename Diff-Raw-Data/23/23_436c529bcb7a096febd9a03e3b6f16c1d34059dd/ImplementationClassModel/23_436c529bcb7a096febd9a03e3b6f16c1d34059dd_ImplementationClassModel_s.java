 package liglab.imag.fr.metadata.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import liglab.imag.fr.metadata.emf.ModelUtil;
 
 import org.apache.felix.CallbackType;
 import org.apache.felix.ComponentType;
 import org.apache.felix.PropertiesType;
 import org.apache.felix.PropertyType;
 import org.apache.felix.ProvidesType;
 import org.apache.felix.RequiresType;
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.ITextFileBuffer;
 import org.eclipse.core.filebuffers.ITextFileBufferManager;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 
 public class ImplementationClassModel {
 
 	/**
 	 * The component type asociated to the class model
 	 */
 	private ComponentType componentType;
 
 	private Map<String, PropertyType> propertiesFields;
 
 	private Map<String, PropertyType> servicePropertiesFields;
 
 	private Map<String, RequiresType> dependencyFields;
 
 	private Map<String, RequiresType> dependencyMethods;
 
 	private Map<String, CallbackType> lifecycleMethods;
 
 	/**
 	 * Store the compilation unit only to add interfaces
 	 */
 	private CompilationUnit compilationUnit = null;
 
 	private static String METHOD_BODY = "{\n// TODO: Add your implementation code here\n}\n";
 
 	public ImplementationClassModel(ComponentType componentType) {
 		this.componentType = componentType;
 		calculateDependenciesFields();
 		calculateDependenciesMethods();
 		calculatePropertiesFields();
 		calculateLifecycleMethods();
 		calculateServicePropertiesFields();
 	}
 
 	private void calculateDependenciesFields() {
 		dependencyFields = new HashMap<String, RequiresType>();
 
 		List<RequiresType> requires = componentType.getRequires();
 		for (RequiresType dependency : requires) {
 			String field = dependency.getField();
 			if (field != null)
 				dependencyFields.put(field, dependency);
 		}
 	}
 
 	private void calculatePropertiesFields() {
 		propertiesFields = new HashMap<String, PropertyType>();
 
 		List<PropertiesType> propertiesLists = componentType.getProperties();
 		if (!propertiesLists.isEmpty()) {
 			List<PropertyType> properties = propertiesLists.get(0).getProperty();
 			for (PropertyType property : properties) {
 				String field = property.getField();
 				propertiesFields.put(field, property);
 			}
 		}
 	}
 
 	private void calculateServicePropertiesFields() {
 		servicePropertiesFields = new HashMap<String, PropertyType>();
 
 		List<ProvidesType> specifications = componentType.getProvides();
 		for (ProvidesType specification : specifications) {
 			List<PropertyType> properties = specification.getProperty();
 			for (PropertyType property : properties) {
 				String field = property.getField();
 				if (field != null)
 					servicePropertiesFields.put(field, property);
 			}
 		}
 	}
 
 	private void calculateDependenciesMethods() {
 		dependencyMethods = new HashMap<String, RequiresType>();
 		List<RequiresType> requires = componentType.getRequires();
 
 		for (RequiresType require : requires) {
 			if (!ModelUtil.isFieldDependency(require)) {
 				String bindMethod = ModelUtil.getBindCallback(require).getMethod();
 				String unbindMethod = ModelUtil.getUnbindCallback(require).getMethod();
 				dependencyMethods.put(bindMethod, require);
 				dependencyMethods.put(unbindMethod, require);
 			}
 		}
 	}
 
 	private void calculateLifecycleMethods() {
 		lifecycleMethods = new HashMap<String, CallbackType>();
 
 		CallbackType validateCallbackType = ModelUtil.getValidateCallback(componentType);
 		CallbackType invalidateCallbackType = ModelUtil.getInvalidateCallback(componentType);
 		if (validateCallbackType != null) {
 			lifecycleMethods.put(validateCallbackType.getMethod(), validateCallbackType);
 		}
 		if (invalidateCallbackType != null) {
 			lifecycleMethods.put(invalidateCallbackType.getMethod(), invalidateCallbackType);
 		}
 	}
 
 	public List<String> getErrorMessages() {
 		List<String> errors = new ArrayList<String>();
 
 		for (String propertiesField : propertiesFields.keySet()) {
 			String propertyName = propertiesFields.get(propertiesField).getName();
 			errors.add("Implementation class has to define the field " + propertiesField + " associated to property "
 			      + propertyName);
 		}
 
 		for (String propertiesField : servicePropertiesFields.keySet()) {
 			String propertyName = propertiesFields.get(propertiesField).getName();
 			errors.add("Implementation class has to define the field " + propertiesField
 			      + " associated to service property " + propertyName);
 		}
 
 		for (String dependenciesField : dependencyFields.keySet()) {
 			String dependencyName = dependencyFields.get(dependenciesField).getField();
 			errors.add("Implementation class has to define the field " + dependenciesField + " associated to dependency "
 			      + dependencyName);
 		}
 
 		for (String dependenciesMethod : dependencyMethods.keySet()) {
 			String dependencyName = dependencyMethods.get(dependenciesMethod).getId();
 			errors.add("Implementation class has to define the method " + dependenciesMethod
 			      + " associated to dependency " + dependencyName);
 		}
 
 		for (String lifecycleMethod : lifecycleMethods.keySet()) {
 			errors.add("Implementation class has to define the method " + lifecycleMethod
 			      + " associated to the component lifecycle ");
 		}
 
 		return errors;
 	}
 
 	public void removeField(String field) {
 		propertiesFields.remove(field);
 		dependencyFields.remove(field);
 		servicePropertiesFields.remove(field);
 	}
 
 	public void removeMethod(String method) {
 		dependencyMethods.remove(method);
 		lifecycleMethods.remove(method);
 	}
 
 	private void generatePropertyCode(IType implClass, PropertyType property, boolean isComponent) {
 		String field = property.getField();
 		String specification = property.getType();
 		StringBuffer code = new StringBuffer();
 		if (isComponent)
 			code.append("/** Injected field for the component property " + property.getName() + " */\n");
 		else
 			code.append("/** Injected field for the service property " + property.getName() + " */\n");
 		if (specification != null && !specification.isEmpty())
 			code.append("private " + JDTUtil.getJavaClassName(specification) + " ");
 		else
 			code.append("private Object ");
 		code.append(field);
 		code.append(";\n\n");
 		try {
 			implClass.createField(code.toString(), null, true, null);
 			if (specification != null && !specification.isEmpty())
 				createImport(implClass, specification);
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void generateFieldScalarDependecyCode(IType implClass, RequiresType dependency) {
 		String field = dependency.getField();
 		String specification = dependency.getSpecification();
 		StringBuffer code = new StringBuffer();
 		code.append("/** Field for " + field + " dependency */\n");
 		if (specification != null && !specification.isEmpty())
 			code.append("private " + JDTUtil.getJavaClassName(specification) + " ");
 		else
 			code.append("private Object ");
 		code.append(field);
 		code.append(";\n\n");
 		try {
 			implClass.createField(code.toString(), null, true, null);
 			if (specification != null && !specification.isEmpty())
 				createImport(implClass, specification);
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void generateFieldMultipleDependecyCode(IType implClass, RequiresType dependency) {
 		String field = dependency.getField();
 		String specification = dependency.getSpecification();
 		StringBuffer code = new StringBuffer();
 		code.append("/** Field for " + field + " dependency */\n");
 		if (specification != null && !specification.isEmpty())
 			code.append("private " + JDTUtil.getJavaClassName(specification) + "[] ");
 		else
 			code.append("private Object ");
 		code.append(field);
 		code.append(";\n\n");
 		try {
 			implClass.createField(code.toString(), null, true, null);
 			if (specification != null && !specification.isEmpty())
 				createImport(implClass, specification);
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void generateDependencyMethod(IType implClass, RequiresType dependency, boolean bind) {
 		StringBuffer code = new StringBuffer();
 		String id = dependency.getId();
 		String specification = dependency.getSpecification();
 		String className = JDTUtil.getJavaClassName(specification);
 
 		if (id == null)
 			id = dependency.getField();
 
 		String dependencyMethod = "";
 
 		if (bind) {
 			dependencyMethod = ModelUtil.getBindCallback(dependency).getMethod();
 			code.append("/** Bind Method for " + id + " dependency */\n");
 		} else {
 			dependencyMethod = ModelUtil.getUnbindCallback(dependency).getMethod();
 			code.append("/** Unbind Method for " + id + " dependency */\n");
 		}
 
 		code.append("public void ");
 		code.append(dependencyMethod);
 		if (specification != null && !specification.isEmpty())
 			code.append("(" + className + " ");
 		else
 			code.append("(Object ");
 		code.append(JDTUtil.firstCharacterToLowerCase(className));
 		code.append(", Map properties)");
 		code.append(METHOD_BODY);
 
 		try {
 			implClass.createMethod(code.toString(), null, true, null);
 			if (specification != null && !specification.isEmpty())
 				createImport(implClass, specification);
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void generateLifecycleMethodCode(IType implClass, CallbackType callback) {
 		StringBuffer code = new StringBuffer();
 		code.append("/** Component Lifecycle Method */\n");
 		code.append("public void ");
 		code.append(callback.getMethod() + "()");
 		code.append(METHOD_BODY);
 
 		try {
 			implClass.createMethod(code.toString(), null, true, null);
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void createImport(IType implClass, String classToImport) throws JavaModelException {
 		String aPackage = JDTUtil.getJavaPackageName(classToImport);
 		if (aPackage.equals("java.lang"))
 			return;
 		implClass.getCompilationUnit().createImport(classToImport, implClass, null);
 	}
 
 	public void completeImplementationClass(IType implClass) {
 
 		// Generation of dependencies based on fields
 		for (RequiresType dependency : dependencyFields.values()) {
 			if (ModelUtil.isFieldDependency(dependency))
 				generateFieldScalarDependecyCode(implClass, dependency);
 			else
 				generateFieldMultipleDependecyCode(implClass, dependency);
 		}
 
 		// Generation of dependencies based on methods
 		Set<RequiresType> dependencies = new HashSet<RequiresType>(dependencyMethods.values());
 		for (RequiresType dependency : dependencies) {
 			generateDependencyMethod(implClass, dependency, true);
 			generateDependencyMethod(implClass, dependency, false);
 		}
 
 		if (!dependencies.isEmpty())
 			try {
 				createImport(implClass, "java.util.Map"); // Creates Map import
 			} catch (JavaModelException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 		// Generation of component configuration properties
 		for (PropertyType property : propertiesFields.values()) {
 			generatePropertyCode(implClass, property, true);
 		}
 
 		// Generation of service properties
 		for (PropertyType property : servicePropertiesFields.values()) {
 			if (!propertiesFields.containsKey(property.getField()))
 				generatePropertyCode(implClass, property, false);
 		}
 
 		// Generation of lifecycle methods
 		for (CallbackType callback : lifecycleMethods.values()) {
 			generateLifecycleMethodCode(implClass, callback);
 		}
 
 		try {
 
 			// Formating the compilation unit
 			ICompilationUnit tempCompilationUnit = implClass.getCompilationUnit();
 			JDTUtil.formatCompilationUnit(tempCompilationUnit);
 			// tempCompilationUnit.save(null, true);
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 
		/*
		 * if (compilationUnit!=null) { addInterface("my.Interface");
		 * saveCompilationUnit(); }
		 */
 
 	}
 
 	public void setCompilationUnit(CompilationUnit unit) {
 		this.compilationUnit = unit;
 	}
 
 	private void addInterface(String name) {
 		AST ast = compilationUnit.getAST();
 		compilationUnit.recordModifications();
 		TypeDeclaration td = (TypeDeclaration) compilationUnit.types().get(0);
 		td.superInterfaceTypes().add(ast.newSimpleType(ast.newName(name)));
 	}
 
 	private void saveCompilationUnit() {
 		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get
 		                                                                               // the
 		                                                                               // buffer
 		                                                                               // manager
 		IPath path = compilationUnit.getJavaElement().getPath(); // unit: instance
 		                                                         // of
 		                                                         // CompilationUnit
 
 		try {
 			bufferManager.connect(path, null); // (1)
 			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
 			// retrieve the buffer
 			IDocument document = textFileBuffer.getDocument(); // (2)
 			// ... edit the document here ...
 
 			// ask the textEditProvider for the change information
 			TextEdit edit = compilationUnit.rewrite(document, null);
 
 			// apply the changes to the document
 			edit.apply(document);
 
 			// commit changes to underlying file
 			textFileBuffer.commit(null /* ProgressMonitor */, false /* Overwrite */); // (3)
 
 		} catch (CoreException e) {
 			e.printStackTrace();
 		} catch (MalformedTreeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				bufferManager.disconnect(path, null);
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
