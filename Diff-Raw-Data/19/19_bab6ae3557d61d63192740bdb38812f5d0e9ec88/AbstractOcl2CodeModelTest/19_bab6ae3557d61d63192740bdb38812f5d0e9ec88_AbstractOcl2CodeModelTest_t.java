 package org.vdfp.uml2code;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.Model;
 import org.eclipse.uml2.uml.Operation;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.ParameterDirectionKind;
 import org.eclipse.uml2.uml.UMLFactory;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.resource.UMLResource;
 import org.junit.Before;
 import org.opaeum.ocltocode.OclCodeBuilder;
 import org.vdfp.uml.common.util.UmlResourceSetFactory;
 import org.vdfp.uml.common.util.emulated.DefaultParentOclEnvironment;
 import org.vdfp.uml.common.util.emulated.OclLibrary;
 import org.vdfp.uml2code.codemodel.CodeModelBuilder;
 import org.vdfp.uml2code.codemodel.UmlCodeModelVisitorAdaptor;
 
 public abstract class AbstractOcl2CodeModelTest {
 
 	protected CodeModelBuilder builder;
 	protected Model model;
 	protected UmlCodeModelVisitorAdaptor adaptor;
 	protected UmlResourceSetFactory resourceSetFactory=new UmlResourceSetFactory(new AdaptableFileLocator());
 	protected OclLibrary library;
 	protected OclCodeBuilder oclCodeBuilder;
 	
 	public AbstractOcl2CodeModelTest() {
 		super();
 	}
 
 	@Before
 	public void setup() {
 		this.builder= new CodeModelBuilder();
 		adaptor=new UmlCodeModelVisitorAdaptor();
 		ResourceSet rst = resourceSetFactory.prepareResourceSet();
 		this.library=new DefaultParentOclEnvironment(rst).getLibrary();
 		Resource r = rst.createResource(URI.createFileURI("tmp.uml"));
 		this.model=UMLFactory.eINSTANCE.createModel();
 		model.setName("model");
 		r.getContents().add(model);
 		Package pkg1 = model.createNestedPackage("pkg1");
 		Package pkg2 = model.createNestedPackage("pkg2");
 		Class emptyClass = (Class) pkg1.createOwnedType("EmptyClass", UMLPackage.eINSTANCE.getClass_());
 		Model primitiveTypes = (Model) rst.getResource(URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI),true).getContents().get(0);
 		emptyClass.createOwnedAttribute("name", primitiveTypes.getOwnedType("String"));
 		Class theClass = (Class) pkg2.createOwnedType("TheClass", UMLPackage.eINSTANCE.getClass_());
 		emptyClass.createOwnedAttribute("theClass", theClass);
 		theClass.createOwnedAttribute("emptyClass", emptyClass);
 		Operation operation = UMLFactory.eINSTANCE.createOperation();
 		operation.getOwnedParameter("param0", emptyClass,false,true).setDirection(ParameterDirectionKind.IN_LITERAL);
 		operation.getOwnedParameter("result", emptyClass,false,true).setDirection(ParameterDirectionKind.RETURN_LITERAL);
 		operation.setName("myOper");
 		emptyClass.getOwnedOperations().add(operation);
 		populateTheClass(theClass);
 		oclCodeBuilder=new OclCodeBuilder();
 	}
 	protected abstract void populateTheClass(Class theClass);
 
 }
