 package org.eclipse.uml2.diagram.clazz.edit.parts;
 
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartFactory;
 import org.eclipse.gef.tools.CellEditorLocator;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.uml2.diagram.clazz.part.UMLVisualIDRegistry;
 
 /**
  * @generated
  */
 public class UMLEditPartFactory implements EditPartFactory {
 
 	/**
 	 * @generated
 	 */
 	public EditPart createEditPart(EditPart context, Object model) {
 		if (model instanceof View) {
 			View view = (View) model;
 			switch (UMLVisualIDRegistry.getVisualID(view)) {
 
 			case PackageEditPart.VISUAL_ID:
 				return new PackageEditPart(view);
 
 			case Package2EditPart.VISUAL_ID:
 				return new Package2EditPart(view);
 
 			case PackageNameEditPart.VISUAL_ID:
 				return new PackageNameEditPart(view);
 
 			case Class2EditPart.VISUAL_ID:
 				return new Class2EditPart(view);
 
 			case ClassNameEditPart.VISUAL_ID:
 				return new ClassNameEditPart(view);
 
 			case ClassStereotypeEditPart.VISUAL_ID:
 				return new ClassStereotypeEditPart(view);
 
 			case AssociationClass2EditPart.VISUAL_ID:
 				return new AssociationClass2EditPart(view);
 
 			case AssociationClassNameEditPart.VISUAL_ID:
 				return new AssociationClassNameEditPart(view);
 
 			case DataType2EditPart.VISUAL_ID:
 				return new DataType2EditPart(view);
 
 			case DataTypeNameEditPart.VISUAL_ID:
 				return new DataTypeNameEditPart(view);
 
 			case PrimitiveType2EditPart.VISUAL_ID:
 				return new PrimitiveType2EditPart(view);
 
 			case PrimitiveTypeNameEditPart.VISUAL_ID:
 				return new PrimitiveTypeNameEditPart(view);
 
 			case Enumeration2EditPart.VISUAL_ID:
 				return new Enumeration2EditPart(view);
 
 			case EnumerationNameEditPart.VISUAL_ID:
 				return new EnumerationNameEditPart(view);
 
 			case InterfaceEditPart.VISUAL_ID:
 				return new InterfaceEditPart(view);
 
 			case InterfaceNameEditPart.VISUAL_ID:
 				return new InterfaceNameEditPart(view);
 
 			case ConstraintEditPart.VISUAL_ID:
 				return new ConstraintEditPart(view);
 
 			case ConstraintNameEditPart.VISUAL_ID:
 				return new ConstraintNameEditPart(view);
 
 			case InstanceSpecification2EditPart.VISUAL_ID:
 				return new InstanceSpecification2EditPart(view);
 
 			case InstanceSpecificationNameEditPart.VISUAL_ID:
 				return new InstanceSpecificationNameEditPart(view);
 
 			case DependencyEditPart.VISUAL_ID:
 				return new DependencyEditPart(view);
 
 			case DependencyNameEditPart.VISUAL_ID:
 				return new DependencyNameEditPart(view);
 
 			case GeneralizationSetEditPart.VISUAL_ID:
 				return new GeneralizationSetEditPart(view);
 
 			case GeneralizationSetIsCoveringIsDisjointEditPart.VISUAL_ID:
 				return new GeneralizationSetIsCoveringIsDisjointEditPart(view);
 
 			case GeneralizationSetNameEditPart.VISUAL_ID:
 				return new GeneralizationSetNameEditPart(view);
 
 			case Interface2EditPart.VISUAL_ID:
 				return new Interface2EditPart(view);
 
 			case InterfaceName2EditPart.VISUAL_ID:
 				return new InterfaceName2EditPart(view);
 
 			case Package4EditPart.VISUAL_ID:
 				return new Package4EditPart(view);
 
 			case PackageName2EditPart.VISUAL_ID:
 				return new PackageName2EditPart(view);
 
 			case AssociationClassRhombEditPart.VISUAL_ID:
 				return new AssociationClassRhombEditPart(view);
 
 			case PackageAsFrameEditPart.VISUAL_ID:
 				return new PackageAsFrameEditPart(view);
 
 			case PackageName3EditPart.VISUAL_ID:
 				return new PackageName3EditPart(view);
 
 			case Package3EditPart.VISUAL_ID:
 				return new Package3EditPart(view);
 
 			case ClassEditPart.VISUAL_ID:
 				return new ClassEditPart(view);
 
 			case DataTypeEditPart.VISUAL_ID:
 				return new DataTypeEditPart(view);
 
 			case PrimitiveTypeEditPart.VISUAL_ID:
 				return new PrimitiveTypeEditPart(view);
 
 			case EnumerationEditPart.VISUAL_ID:
 				return new EnumerationEditPart(view);
 
 			case AssociationClassEditPart.VISUAL_ID:
 				return new AssociationClassEditPart(view);
 
 			case InstanceSpecificationEditPart.VISUAL_ID:
 				return new InstanceSpecificationEditPart(view);
 
 			case PropertyEditPart.VISUAL_ID:
 				return new PropertyEditPart(view);
 
 			case OperationEditPart.VISUAL_ID:
 				return new OperationEditPart(view);
 
 			case Class3EditPart.VISUAL_ID:
 				return new Class3EditPart(view);
 
 			case PortEditPart.VISUAL_ID:
 				return new PortEditPart(view);
 
 			case PortNameEditPart.VISUAL_ID:
 				return new PortNameEditPart(view);
 
 			case RedefinableTemplateSignatureEditPart.VISUAL_ID:
 				return new RedefinableTemplateSignatureEditPart(view);
 
 			case TemplateSignatureNode_signatureEditPart.VISUAL_ID:
 				return new TemplateSignatureNode_signatureEditPart(view);
 
 			case Property2EditPart.VISUAL_ID:
 				return new Property2EditPart(view);
 
 			case Operation2EditPart.VISUAL_ID:
 				return new Operation2EditPart(view);
 
 			case Property3EditPart.VISUAL_ID:
 				return new Property3EditPart(view);
 
 			case Operation3EditPart.VISUAL_ID:
 				return new Operation3EditPart(view);
 
 			case Property4EditPart.VISUAL_ID:
 				return new Property4EditPart(view);
 
 			case Operation4EditPart.VISUAL_ID:
 				return new Operation4EditPart(view);
 
 			case EnumerationLiteralEditPart.VISUAL_ID:
 				return new EnumerationLiteralEditPart(view);
 
 			case Property5EditPart.VISUAL_ID:
 				return new Property5EditPart(view);
 
 			case Operation5EditPart.VISUAL_ID:
 				return new Operation5EditPart(view);
 
 			case SlotEditPart.VISUAL_ID:
 				return new SlotEditPart(view);
 
 			case Property6EditPart.VISUAL_ID:
 				return new Property6EditPart(view);
 
 			case Operation6EditPart.VISUAL_ID:
 				return new Operation6EditPart(view);
 
 			case Class4EditPart.VISUAL_ID:
 				return new Class4EditPart(view);
 
 			case ElementImportEditPart.VISUAL_ID:
 				return new ElementImportEditPart(view);
 
 			case Package6EditPart.VISUAL_ID:
 				return new Package6EditPart(view);
 
 			case PackageName4EditPart.VISUAL_ID:
 				return new PackageName4EditPart(view);
 
 			case Class5EditPart.VISUAL_ID:
 				return new Class5EditPart(view);
 
 			case ClassName2EditPart.VISUAL_ID:
 				return new ClassName2EditPart(view);
 
 			case ClassQualifiedNameEditPart.VISUAL_ID:
 				return new ClassQualifiedNameEditPart(view);
 
 			case Enumeration3EditPart.VISUAL_ID:
 				return new Enumeration3EditPart(view);
 
 			case EnumerationName2EditPart.VISUAL_ID:
 				return new EnumerationName2EditPart(view);
 
 			case InstanceSpecification3EditPart.VISUAL_ID:
 				return new InstanceSpecification3EditPart(view);
 
 			case InstanceSpecificationName2EditPart.VISUAL_ID:
 				return new InstanceSpecificationName2EditPart(view);
 
 			case DataType3EditPart.VISUAL_ID:
 				return new DataType3EditPart(view);
 
 			case DataTypeName2EditPart.VISUAL_ID:
 				return new DataTypeName2EditPart(view);
 
 			case PrimitiveType3EditPart.VISUAL_ID:
 				return new PrimitiveType3EditPart(view);
 
 			case PrimitiveTypeName2EditPart.VISUAL_ID:
 				return new PrimitiveTypeName2EditPart(view);
 
 			case PackagePackagesEditPart.VISUAL_ID:
 				return new PackagePackagesEditPart(view);
 
 			case PackageClassifiersEditPart.VISUAL_ID:
 				return new PackageClassifiersEditPart(view);
 
 			case PackageOtherEditPart.VISUAL_ID:
 				return new PackageOtherEditPart(view);
 
 			case ClassAttributesEditPart.VISUAL_ID:
 				return new ClassAttributesEditPart(view);
 
 			case ClassOperationsEditPart.VISUAL_ID:
 				return new ClassOperationsEditPart(view);
 
 			case ClassClassesEditPart.VISUAL_ID:
 				return new ClassClassesEditPart(view);
 
 			case AssociationClassAttributesEditPart.VISUAL_ID:
 				return new AssociationClassAttributesEditPart(view);
 
 			case AssociationClassOperationsEditPart.VISUAL_ID:
 				return new AssociationClassOperationsEditPart(view);
 
 			case AssociationClassClassesEditPart.VISUAL_ID:
 				return new AssociationClassClassesEditPart(view);
 
 			case DataTypeAttributesEditPart.VISUAL_ID:
 				return new DataTypeAttributesEditPart(view);
 
 			case DataTypeOperationsEditPart.VISUAL_ID:
 				return new DataTypeOperationsEditPart(view);
 
 			case PrimitiveTypeAttributesEditPart.VISUAL_ID:
 				return new PrimitiveTypeAttributesEditPart(view);
 
 			case PrimitiveTypeOperationsEditPart.VISUAL_ID:
 				return new PrimitiveTypeOperationsEditPart(view);
 
 			case EnumerationLiteralsEditPart.VISUAL_ID:
 				return new EnumerationLiteralsEditPart(view);
 
 			case EnumerationAttributesEditPart.VISUAL_ID:
 				return new EnumerationAttributesEditPart(view);
 
 			case EnumerationOperationsEditPart.VISUAL_ID:
 				return new EnumerationOperationsEditPart(view);
 
 			case InstanceSpecificationSlotsEditPart.VISUAL_ID:
 				return new InstanceSpecificationSlotsEditPart(view);
 
 			case InterfaceAttributesEditPart.VISUAL_ID:
 				return new InterfaceAttributesEditPart(view);
 
 			case InterfaceOperationsEditPart.VISUAL_ID:
 				return new InterfaceOperationsEditPart(view);
 
 			case InterfaceClassesEditPart.VISUAL_ID:
 				return new InterfaceClassesEditPart(view);
 
 			case PackageImportsEditPart.VISUAL_ID:
 				return new PackageImportsEditPart(view);
 
 			case PackageAsFrameContentsEditPart.VISUAL_ID:
 				return new PackageAsFrameContentsEditPart(view);
 
 			case PackageAsFrameContents2EditPart.VISUAL_ID:
 				return new PackageAsFrameContents2EditPart(view);
 
 			case ClassAttributes2EditPart.VISUAL_ID:
 				return new ClassAttributes2EditPart(view);
 
 			case ClassOperations2EditPart.VISUAL_ID:
 				return new ClassOperations2EditPart(view);
 
 			case ClassClasses2EditPart.VISUAL_ID:
 				return new ClassClasses2EditPart(view);
 
 			case EnumerationLiterals2EditPart.VISUAL_ID:
 				return new EnumerationLiterals2EditPart(view);
 
 			case EnumerationAttributes2EditPart.VISUAL_ID:
 				return new EnumerationAttributes2EditPart(view);
 
 			case EnumerationOperations2EditPart.VISUAL_ID:
 				return new EnumerationOperations2EditPart(view);
 
 			case InstanceSpecificationSlots2EditPart.VISUAL_ID:
 				return new InstanceSpecificationSlots2EditPart(view);
 
 			case DataTypeAttributes2EditPart.VISUAL_ID:
 				return new DataTypeAttributes2EditPart(view);
 
 			case DataTypeOperations2EditPart.VISUAL_ID:
 				return new DataTypeOperations2EditPart(view);
 
 			case PrimitiveTypeAttributes2EditPart.VISUAL_ID:
 				return new PrimitiveTypeAttributes2EditPart(view);
 
 			case PrimitiveTypeOperations2EditPart.VISUAL_ID:
 				return new PrimitiveTypeOperations2EditPart(view);
 
 			case GeneralizationEditPart.VISUAL_ID:
 				return new GeneralizationEditPart(view);
 
 			case Dependency2EditPart.VISUAL_ID:
 				return new Dependency2EditPart(view);
 
 			case DependencyName2EditPart.VISUAL_ID:
 				return new DependencyName2EditPart(view);
 
 			case DependencyName3EditPart.VISUAL_ID:
 				return new DependencyName3EditPart(view);
 
 			case Property7EditPart.VISUAL_ID:
 				return new Property7EditPart(view);
 
 			case PropertyNameEditPart.VISUAL_ID:
 				return new PropertyNameEditPart(view);
 
 			case PropertyName2EditPart.VISUAL_ID:
 				return new PropertyName2EditPart(view);
 
 			case ConstraintConstrainedElementEditPart.VISUAL_ID:
 				return new ConstraintConstrainedElementEditPart(view);
 
 			case AssociationEditPart.VISUAL_ID:
 				return new AssociationEditPart(view);
 
 			case AssociationNameEditPart.VISUAL_ID:
 				return new AssociationNameEditPart(view);
 
 			case AssociationName2EditPart.VISUAL_ID:
 				return new AssociationName2EditPart(view);
 
 			case AssociationName3EditPart.VISUAL_ID:
 				return new AssociationName3EditPart(view);
 
 			case AssociationName4EditPart.VISUAL_ID:
 				return new AssociationName4EditPart(view);
 
 			case AssociationName5EditPart.VISUAL_ID:
 				return new AssociationName5EditPart(view);
 
 			case AssociationName6EditPart.VISUAL_ID:
 				return new AssociationName6EditPart(view);
 
 			case AssociationName7EditPart.VISUAL_ID:
 				return new AssociationName7EditPart(view);
 
 			case DependencySupplierEditPart.VISUAL_ID:
 				return new DependencySupplierEditPart(view);
 
 			case DependencyClientEditPart.VISUAL_ID:
 				return new DependencyClientEditPart(view);
 
 			case InterfaceRealizationEditPart.VISUAL_ID:
 				return new InterfaceRealizationEditPart(view);
 
 			case RealizationEditPart.VISUAL_ID:
 				return new RealizationEditPart(view);
 
 			case RealizationNameEditPart.VISUAL_ID:
 				return new RealizationNameEditPart(view);
 
 			case Generalization2EditPart.VISUAL_ID:
 				return new Generalization2EditPart(view);
 
 			case GeneralizationGeneralEditPart.VISUAL_ID:
 				return new GeneralizationGeneralEditPart(view);
 
 			case UsageEditPart.VISUAL_ID:
 				return new UsageEditPart(view);
 
 			case AssociationClassConnectorEditPart.VISUAL_ID:
 				return new AssociationClassConnectorEditPart(view);
 
			case AssociationInstanceEditPart.VISUAL_ID:
				return new AssociationInstanceEditPart(view);
 			}
 		}
 		return createUnrecognizedEditPart(context, model);
 	}
 
 	/**
 	 * @generated
 	 */
 	private EditPart createUnrecognizedEditPart(EditPart context, Object model) {
 		// Handle creation of unrecognized child node EditParts here
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static CellEditorLocator getTextCellEditorLocator(ITextAwareEditPart source) {
 		if (source.getFigure() instanceof WrappingLabel)
 			return new TextCellEditorLocator((WrappingLabel) source.getFigure());
 		else {
 			return new LabelCellEditorLocator((Label) source.getFigure());
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	static private class TextCellEditorLocator implements CellEditorLocator {
 
 		/**
 		 * @generated
 		 */
 		private WrappingLabel wrapLabel;
 
 		/**
 		 * @generated
 		 */
 		public TextCellEditorLocator(WrappingLabel wrapLabel) {
 			this.wrapLabel = wrapLabel;
 		}
 
 		/**
 		 * @generated
 		 */
 		public WrappingLabel getWrapLabel() {
 			return wrapLabel;
 		}
 
 		/**
 		 * @generated
 		 */
 		public void relocate(CellEditor celleditor) {
 			Text text = (Text) celleditor.getControl();
 			Rectangle rect = getWrapLabel().getTextBounds().getCopy();
 			getWrapLabel().translateToAbsolute(rect);
 			if (getWrapLabel().isTextWrapOn() && getWrapLabel().getText().length() > 0) {
 				rect.setSize(new Dimension(text.computeSize(rect.width, SWT.DEFAULT)));
 			} else {
 				int avr = FigureUtilities.getFontMetrics(text.getFont()).getAverageCharWidth();
 				rect.setSize(new Dimension(text.computeSize(SWT.DEFAULT, SWT.DEFAULT)).expand(avr * 2, 0));
 			}
 			if (!rect.equals(new Rectangle(text.getBounds()))) {
 				text.setBounds(rect.x, rect.y, rect.width, rect.height);
 			}
 		}
 
 	}
 
 	/**
 	 * @generated
 	 */
 	private static class LabelCellEditorLocator implements CellEditorLocator {
 
 		/**
 		 * @generated
 		 */
 		private Label label;
 
 		/**
 		 * @generated
 		 */
 		public LabelCellEditorLocator(Label label) {
 			this.label = label;
 		}
 
 		/**
 		 * @generated
 		 */
 		public Label getLabel() {
 			return label;
 		}
 
 		/**
 		 * @generated
 		 */
 		public void relocate(CellEditor celleditor) {
 			Text text = (Text) celleditor.getControl();
 			Rectangle rect = getLabel().getTextBounds().getCopy();
 			getLabel().translateToAbsolute(rect);
 			int avr = FigureUtilities.getFontMetrics(text.getFont()).getAverageCharWidth();
 			rect.setSize(new Dimension(text.computeSize(SWT.DEFAULT, SWT.DEFAULT)).expand(avr * 2, 0));
 			if (!rect.equals(new Rectangle(text.getBounds()))) {
 				text.setBounds(rect.x, rect.y, rect.width, rect.height);
 			}
 		}
 	}
 }
