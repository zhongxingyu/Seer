 package org.eclipse.uml2.diagram.clazz.tests.tool;
 
 import org.eclipse.gef.palette.PaletteContainer;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
 import org.eclipse.uml2.diagram.clazz.edit.parts.AssociationClass2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.AssociationClassEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.AssociationClassRhombEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Class2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Class3EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Class4EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Class5EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.ClassAttributesEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.ClassClassesEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.ClassOperationsEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.ConstraintEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.DataType2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.DataType3EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.DataTypeEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Enumeration2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Enumeration3EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.EnumerationEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Interface2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.InterfaceClassesEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.OperationEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Package2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.Package3EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PackageAsFrameContentsEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PackageAsFrameEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PackageClassifiersEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PackagePackagesEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PrimitiveType2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PrimitiveType3EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PrimitiveTypeEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PropertyEditPart;
 import org.eclipse.uml2.diagram.clazz.part.UMLPaletteFactory;
 import org.eclipse.uml2.diagram.clazz.part.UMLVisualIDRegistry;
 
 public class PaletteNodesGroupTest extends ClassDiagramCreationToolTest {
 
 	PaletteRoot myRoot = new PaletteRoot();
 
	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		new UMLPaletteFactory().fillPalette(myRoot);
 	}
 
 	public PaletteNodesGroupTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testPropertyInClass() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool1 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool1.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart1 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class2EditPart.VISUAL_ID));
 		assertNotNull(editPart1);
 		assertEquals("Class", getMetaclassName(editPart1));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment1 = editPart1.getChildBySemanticHint(UMLVisualIDRegistry.getType(ClassAttributesEditPart.VISUAL_ID));
 		assertNotNull(compartment1);
 		ToolEntry tool2 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(1)).getChildren().get(0);
 		createNodeByTool(tool2.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart2 = compartment1.getChildBySemanticHint(UMLVisualIDRegistry.getType(PropertyEditPart.VISUAL_ID));
 		assertNotNull(editPart2);
 		assertEquals("Property", getMetaclassName(editPart2));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testPropertyInClass2() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool3 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool3.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart3 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class2EditPart.VISUAL_ID));
 		assertNotNull(editPart3);
 		assertEquals("Class", getMetaclassName(editPart3));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment2 = editPart3.getChildBySemanticHint(UMLVisualIDRegistry.getType(ClassAttributesEditPart.VISUAL_ID));
 		assertNotNull(compartment2);
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Property_3001"), 200, 100);
 
 		IGraphicalEditPart editPart4 = compartment2.getChildBySemanticHint(UMLVisualIDRegistry.getType(PropertyEditPart.VISUAL_ID));
 		assertNotNull(editPart4);
 		assertEquals("Property", getMetaclassName(editPart4));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testOperationInClass() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool4 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool4.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart5 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class2EditPart.VISUAL_ID));
 		assertNotNull(editPart5);
 		assertEquals("Class", getMetaclassName(editPart5));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment3 = editPart5.getChildBySemanticHint(UMLVisualIDRegistry.getType(ClassOperationsEditPart.VISUAL_ID));
 		assertNotNull(compartment3);
 		ToolEntry tool5 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(1)).getChildren().get(1);
 		createNodeByTool(tool5.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart6 = compartment3.getChildBySemanticHint(UMLVisualIDRegistry.getType(OperationEditPart.VISUAL_ID));
 		assertNotNull(editPart6);
 		assertEquals("Operation", getMetaclassName(editPart6));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testClassOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool6 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool6.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart7 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class2EditPart.VISUAL_ID));
 		assertNotNull(editPart7);
 		assertEquals("Class", getMetaclassName(editPart7));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testClassInClass() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool7 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool7.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart8 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class2EditPart.VISUAL_ID));
 		assertNotNull(editPart8);
 		assertEquals("Class", getMetaclassName(editPart8));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment4 = editPart8.getChildBySemanticHint(UMLVisualIDRegistry.getType(ClassClassesEditPart.VISUAL_ID));
 		assertNotNull(compartment4);
 		ToolEntry tool8 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool8.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart9 = compartment4.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class3EditPart.VISUAL_ID));
 		assertNotNull(editPart9);
 		assertEquals("Class", getMetaclassName(editPart9));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testClassInInterface() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Interface_2013"), 200, 100);
 
 		IGraphicalEditPart editPart10 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Interface2EditPart.VISUAL_ID));
 		assertNotNull(editPart10);
 		assertEquals("Interface", getMetaclassName(editPart10));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment5 = editPart10.getChildBySemanticHint(UMLVisualIDRegistry.getType(InterfaceClassesEditPart.VISUAL_ID));
 		assertNotNull(compartment5);
 		ToolEntry tool9 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool9.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart11 = compartment5.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class4EditPart.VISUAL_ID));
 		assertNotNull(editPart11);
 		assertEquals("Class", getMetaclassName(editPart11));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testClassInFramePackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2016"), 200, 100);
 
 		IGraphicalEditPart editPart12 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameEditPart.VISUAL_ID));
 		assertNotNull(editPart12);
 		assertEquals("Package", getMetaclassName(editPart12));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment6 = editPart12.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameContentsEditPart.VISUAL_ID));
 		assertNotNull(compartment6);
 		ToolEntry tool10 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(0);
 		createNodeByTool(tool10.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart13 = compartment6.getChildBySemanticHint(UMLVisualIDRegistry.getType(Class5EditPart.VISUAL_ID));
 		assertNotNull(editPart13);
 		assertEquals("Class", getMetaclassName(editPart13));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testPackageOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool11 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(1);
 		createNodeByTool(tool11.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart14 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Package2EditPart.VISUAL_ID));
 		assertNotNull(editPart14);
 		assertEquals("Package", getMetaclassName(editPart14));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testPackageInPackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2002"), 200, 100);
 
 		IGraphicalEditPart editPart15 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Package2EditPart.VISUAL_ID));
 		assertNotNull(editPart15);
 		assertEquals("Package", getMetaclassName(editPart15));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment7 = editPart15.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackagePackagesEditPart.VISUAL_ID));
 		assertNotNull(compartment7);
 		ToolEntry tool12 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(1);
 		createNodeByTool(tool12.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart16 = compartment7.getChildBySemanticHint(UMLVisualIDRegistry.getType(Package3EditPart.VISUAL_ID));
 		assertNotNull(editPart16);
 		assertEquals("Package", getMetaclassName(editPart16));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testConstraintOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool13 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(5);
 		createNodeByTool(tool13.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart17 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(ConstraintEditPart.VISUAL_ID));
 		assertNotNull(editPart17);
 		assertEquals("Constraint", getMetaclassName(editPart17));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testAssociationClassRectangleOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool14 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(6);
 		createNodeByTool(tool14.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart18 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(AssociationClass2EditPart.VISUAL_ID));
 		assertNotNull(editPart18);
 		assertEquals("AssociationClass", getMetaclassName(editPart18));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testAssociationClassRhombOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool15 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(6);
 		createNodeByTool(tool15.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart19 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(AssociationClassRhombEditPart.VISUAL_ID));
 		assertNotNull(editPart19);
 		assertEquals("AssociationClass", getMetaclassName(editPart19));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testAssociationClassInPackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2002"), 200, 100);
 
 		IGraphicalEditPart editPart20 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Package2EditPart.VISUAL_ID));
 		assertNotNull(editPart20);
 		assertEquals("Package", getMetaclassName(editPart20));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment8 = editPart20.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageClassifiersEditPart.VISUAL_ID));
 		assertNotNull(compartment8);
 		ToolEntry tool16 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(6);
 		createNodeByTool(tool16.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart21 = compartment8.getChildBySemanticHint(UMLVisualIDRegistry.getType(AssociationClassEditPart.VISUAL_ID));
 		assertNotNull(editPart21);
 		assertEquals("AssociationClass", getMetaclassName(editPart21));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testEnumerationOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool17 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(2);
 		createNodeByTool(tool17.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart22 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Enumeration2EditPart.VISUAL_ID));
 		assertNotNull(editPart22);
 		assertEquals("Enumeration", getMetaclassName(editPart22));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testEnumerationInFramePackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2016"), 200, 100);
 
 		IGraphicalEditPart editPart23 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameEditPart.VISUAL_ID));
 		assertNotNull(editPart23);
 		assertEquals("Package", getMetaclassName(editPart23));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment9 = editPart23.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameContentsEditPart.VISUAL_ID));
 		assertNotNull(compartment9);
 		ToolEntry tool18 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(2);
 		createNodeByTool(tool18.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart24 = compartment9.getChildBySemanticHint(UMLVisualIDRegistry.getType(Enumeration3EditPart.VISUAL_ID));
 		assertNotNull(editPart24);
 		assertEquals("Enumeration", getMetaclassName(editPart24));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testEnumerationInPackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2002"), 200, 100);
 
 		IGraphicalEditPart editPart25 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Package2EditPart.VISUAL_ID));
 		assertNotNull(editPart25);
 		assertEquals("Package", getMetaclassName(editPart25));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment10 = editPart25.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageClassifiersEditPart.VISUAL_ID));
 		assertNotNull(compartment10);
 		ToolEntry tool19 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(2);
 		createNodeByTool(tool19.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart26 = compartment10.getChildBySemanticHint(UMLVisualIDRegistry.getType(EnumerationEditPart.VISUAL_ID));
 		assertNotNull(editPart26);
 		assertEquals("Enumeration", getMetaclassName(editPart26));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testDataTypeOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool20 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(3);
 		createNodeByTool(tool20.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart27 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(DataType2EditPart.VISUAL_ID));
 		assertNotNull(editPart27);
 		assertEquals("DataType", getMetaclassName(editPart27));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testDataTypeInFramePackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2016"), 200, 100);
 
 		IGraphicalEditPart editPart28 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameEditPart.VISUAL_ID));
 		assertNotNull(editPart28);
 		assertEquals("Package", getMetaclassName(editPart28));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment11 = editPart28.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameContentsEditPart.VISUAL_ID));
 		assertNotNull(compartment11);
 		ToolEntry tool21 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(3);
 		createNodeByTool(tool21.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart29 = compartment11.getChildBySemanticHint(UMLVisualIDRegistry.getType(DataType3EditPart.VISUAL_ID));
 		assertNotNull(editPart29);
 		assertEquals("DataType", getMetaclassName(editPart29));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testDataTypeInPackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2002"), 200, 100);
 
 		IGraphicalEditPart editPart30 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Package2EditPart.VISUAL_ID));
 		assertNotNull(editPart30);
 		assertEquals("Package", getMetaclassName(editPart30));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment12 = editPart30.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageClassifiersEditPart.VISUAL_ID));
 		assertNotNull(compartment12);
 		ToolEntry tool22 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(3);
 		createNodeByTool(tool22.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart31 = compartment12.getChildBySemanticHint(UMLVisualIDRegistry.getType(DataTypeEditPart.VISUAL_ID));
 		assertNotNull(editPart31);
 		assertEquals("DataType", getMetaclassName(editPart31));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testPrimitiveTypeOnDiagram() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		ToolEntry tool23 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(4);
 		createNodeByTool(tool23.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart32 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(PrimitiveType2EditPart.VISUAL_ID));
 		assertNotNull(editPart32);
 		assertEquals("PrimitiveType", getMetaclassName(editPart32));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testPrimitiveTypeInFramePackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2016"), 200, 100);
 
 		IGraphicalEditPart editPart33 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameEditPart.VISUAL_ID));
 		assertNotNull(editPart33);
 		assertEquals("Package", getMetaclassName(editPart33));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment13 = editPart33.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageAsFrameContentsEditPart.VISUAL_ID));
 		assertNotNull(compartment13);
 		ToolEntry tool24 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(4);
 		createNodeByTool(tool24.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart34 = compartment13.getChildBySemanticHint(UMLVisualIDRegistry.getType(PrimitiveType3EditPart.VISUAL_ID));
 		assertNotNull(editPart34);
 		assertEquals("PrimitiveType", getMetaclassName(editPart34));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public void testPrimitiveTypeInPackage() {
 		IGraphicalEditPart diagramEditPart = getDiagramEditPart();
 		createNodeByTool(ElementTypeRegistry.getInstance().getType("org.eclipse.uml2.diagram.clazz.Package_2002"), 200, 100);
 
 		IGraphicalEditPart editPart35 = diagramEditPart.getChildBySemanticHint(UMLVisualIDRegistry.getType(Package2EditPart.VISUAL_ID));
 		assertNotNull(editPart35);
 		assertEquals("Package", getMetaclassName(editPart35));
 		getDiagramEditPart().getViewer().flush();
 
 		IGraphicalEditPart compartment14 = editPart35.getChildBySemanticHint(UMLVisualIDRegistry.getType(PackageClassifiersEditPart.VISUAL_ID));
 		assertNotNull(compartment14);
 		ToolEntry tool25 = (ToolEntry) ((PaletteContainer) myRoot.getChildren().get(0)).getChildren().get(4);
 		createNodeByTool(tool25.createTool(), 200, 100);
 
 		IGraphicalEditPart editPart36 = compartment14.getChildBySemanticHint(UMLVisualIDRegistry.getType(PrimitiveTypeEditPart.VISUAL_ID));
 		assertNotNull(editPart36);
 		assertEquals("PrimitiveType", getMetaclassName(editPart36));
 		getDiagramEditPart().getViewer().flush();
 
 	}
 
 	private static java.lang.String getMetaclassName(IGraphicalEditPart editPart) {
 		return editPart.getNotationView().getElement().eClass().getName();
 	}
 }
