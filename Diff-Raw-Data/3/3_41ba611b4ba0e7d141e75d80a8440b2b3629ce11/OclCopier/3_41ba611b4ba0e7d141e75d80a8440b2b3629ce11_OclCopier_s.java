 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.modelinglab.ocl.core.ast.utils;
 
 import org.modelinglab.ocl.core.ast.types.TemplateRestrictions;
 import org.modelinglab.ocl.core.ast.types.VoidType;
 import org.modelinglab.ocl.core.ast.types.InvalidType;
 import org.modelinglab.ocl.core.ast.types.Classifier;
 import org.modelinglab.ocl.core.ast.types.SetType;
 import org.modelinglab.ocl.core.ast.types.OrderedSetType;
 import org.modelinglab.ocl.core.ast.types.BagType;
 import org.modelinglab.ocl.core.ast.types.TemplateParameterType;
 import org.modelinglab.ocl.core.ast.types.MessageType;
 import org.modelinglab.ocl.core.ast.types.CollectionType;
 import org.modelinglab.ocl.core.ast.types.AnyType;
 import org.modelinglab.ocl.core.ast.types.TupleType;
 import org.modelinglab.ocl.core.ast.types.ClassifierType;
 import org.modelinglab.ocl.core.ast.types.PrimitiveType;
 import org.modelinglab.ocl.core.ast.types.SequenceType;
 import org.modelinglab.ocl.core.ast.expressions.OperationCallExp;
 import org.modelinglab.ocl.core.ast.expressions.InvalidLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.PrimitiveLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.LiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.TupleLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.OclExpression;
 import org.modelinglab.ocl.core.ast.expressions.Variable;
 import org.modelinglab.ocl.core.ast.expressions.CollectionLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.CallExp;
 import org.modelinglab.ocl.core.ast.expressions.TupleAttributeCallExp;
 import org.modelinglab.ocl.core.ast.expressions.LetExp;
 import org.modelinglab.ocl.core.ast.expressions.RealLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.VariableExp;
 import org.modelinglab.ocl.core.ast.expressions.StringLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.FeatureCallExp;
 import org.modelinglab.ocl.core.ast.expressions.UnlimitedNaturalExp;
 import org.modelinglab.ocl.core.ast.expressions.TypeExp;
 import org.modelinglab.ocl.core.ast.expressions.EnumLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.AssociationEndCallExp;
 import org.modelinglab.ocl.core.ast.expressions.StateExp;
 import org.modelinglab.ocl.core.ast.expressions.IterateExp;
 import org.modelinglab.ocl.core.ast.expressions.NavigationCallExp;
 import org.modelinglab.ocl.core.ast.expressions.CollectionLiteralPart;
 import org.modelinglab.ocl.core.ast.expressions.MessageExp;
 import org.modelinglab.ocl.core.ast.expressions.IfExp;
 import org.modelinglab.ocl.core.ast.expressions.LoopExp;
 import org.modelinglab.ocl.core.ast.expressions.NullLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.BooleanLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.CollectionRange;
 import org.modelinglab.ocl.core.ast.expressions.IteratorExp;
 import org.modelinglab.ocl.core.ast.expressions.AttributeCallExp;
 import org.modelinglab.ocl.core.ast.expressions.IntegerLiteralExp;
 import org.modelinglab.ocl.core.ast.expressions.CollectionItem;
 import org.modelinglab.ocl.core.ast.expressions.TupleLiteralPart;
 import org.modelinglab.ocl.core.ast.UmlClass;
 import org.modelinglab.ocl.core.ast.Operation;
 import org.modelinglab.ocl.core.ast.AssociationEnd;
 import org.modelinglab.ocl.core.ast.Attribute;
 import org.modelinglab.ocl.core.ast.Element;
 import org.modelinglab.ocl.core.ast.Namespace;
 import org.modelinglab.ocl.core.ast.UmlEnumLiteral;
 import org.modelinglab.ocl.core.ast.Comment;
 import org.modelinglab.ocl.core.ast.Parameter;
 import org.modelinglab.ocl.core.ast.UmlEnum;
 import java.util.*;
 
 /**
  *
  * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
  */
 public class OclCopier implements OclVisitor<Element, OclCopier.Argument> {
 
     private void visitElement(Element input, Element output, OclCopier.Argument argument) {
         if (input.getNamespace() != null) {
             input.getNamespace().accept(this, argument); //this will call output.setNamespaceUnsecure()
         }
 //        if (argument.mapAnnotations) {
 //            if (!argument.cloneAnnotations) {
 //                for (Class class1 : input.getAllAnnotations()) {
 //                    Object previousAnnotation = output.setAnnotation(class1, input.getAnnotation(class1));
 //                    assert previousAnnotation == null;
 //                }
 //            } else {
 //                for (Class class1 : input.getAllAnnotations()) {
 //                    Immutable value = input.getAnnotation(class1);
 //                    boolean implementsCloneable = false;
 //                    for (Class<?> class2 : value.getClass().getInterfaces()) {
 //                        if (class2.equals(Cloneable.class)) {
 //                            implementsCloneable = true;
 //                            break;
 //                        }
 //                    }
 //                    if (!implementsCloneable) {
 //                        throw new IllegalArgumentException(input + " has an annotation of class "
 //                                + value.getClass() + " that does not extends ImmutableCloneable");
 //                    }
 //                    Method cloneMethod = null;
 //                    try {
 //                        cloneMethod = value.getClass().getDeclaredMethod("clone", new Class<?>[0]);
 //                        Immutable clone = (Immutable) cloneMethod.invoke(value);
 //                        output.setAnnotation(class1, clone);
 //                    } catch (ReflectiveOperationException ex) {
 //                        throw new IllegalArgumentException(input + " has an annotation of class "
 //                                + value.getClass() + " that does not extends ImmutableCloneable", ex);
 //                    } catch (IllegalArgumentException ex) {
 //                        throw new IllegalArgumentException(input + " has an annotation of class "
 //                                + value.getClass() + " that does not extends ImmutableCloneable", ex);
 //                    } catch (SecurityException ex) {
 //                        throw new IllegalArgumentException(input + " has an annotation of class "
 //                                + value.getClass() + " that does not extends ImmutableCloneable", ex);
 //                    }
 //                }
 //            }
 //        }
     }
 
     @Override
     public Element visit(AssociationEnd o, OclCopier.Argument argument) {
         if (argument.map.containsKey(o)) {
             return argument.map.get(o);
         }
         AssociationEnd result = new AssociationEnd(o);
         argument.map.put(o, result);
         visitElement(o, result, argument);
 
         if (o.getUmlClass() != null) {
             result.setUmlClass((UmlClass) o.getUmlClass().accept((UmlVisitor<Element, OclCopier.Argument>) this, argument));
         }
         if (o.getOpposited() != null) {
             result.setOppositedUnsecure((AssociationEnd) o.getOpposited().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(Attribute o, OclCopier.Argument argument) {
         if (argument.map.containsKey(o)) {
             return argument.map.get(o);
         }
         Attribute result = new Attribute(o);
         argument.map.put(o, result);
         visitElement(o, result, argument);
 
         if (o.getType() != null) {
             result.setReferredType((Classifier) o.getType().accept(this, argument));
         }
         return result;
     }
 
     @Override
     public Element visit(Comment o, OclCopier.Argument argument) {
         throw new UnsupportedOperationException("Not supported.");
     }
 
     @Override
     public Element visit(Operation o, OclCopier.Argument argument) {
         if (argument.map.containsKey(o)) {
             return argument.map.get(o);
         }
         Classifier source = (Classifier) o.getSource().accept(this, argument);
 
         List<Classifier> argTypes = new ArrayList<Classifier>(o.getOwnedParameters().size());
         for (Parameter parameter : o.getOwnedParameters()) {
             argTypes.add((Classifier) parameter.getType().accept(this, argument));
         }
 
         TemplateRestrictions restrictions = new TemplateRestrictions();
         source.fixTemplates(restrictions);
         Operation result = o.specificOperation(source, argTypes, restrictions);
 
         argument.map.put(o, result);
         visitElement(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(Parameter o, OclCopier.Argument argument) {
         throw new UnsupportedOperationException("Paremeters are not copiable. You should copy its Operation.");
     }
 
     @Override
     public Classifier visit(UmlClass o, OclCopier.Argument argument) {
         if (argument.map.containsKey(o)) {
             return (Classifier) argument.map.get(o);
         }
         UmlClass result = new UmlClass();
         argument.map.put(o, result);
         result.setName(o.getName());
         visitElement(o, result, argument);
 
         for (UmlClass subClass : o.getSubClasses()) {
             result.addSubClass((UmlClass) subClass.accept((UmlVisitor<Element, OclCopier.Argument>) this, argument));
         }
         for (UmlClass superClass : o.getSuperClasses()) {
             result.addSuperClass((UmlClass) superClass.accept((UmlVisitor<Element, OclCopier.Argument>) this, argument));
         }
         for (Operation operation : o.getOperations()) {
             result.addOperation((Operation) operation.accept(this, argument));
         }
         for (AssociationEnd associationEnd : o.getOwnedAssociationEnds()) {
             result.addOwnedAssociationEnd((AssociationEnd) associationEnd.accept(this, argument));
         }
         for (Attribute attribute : o.getOwnedAttributes()) {
             result.addOwnedAttribute((Attribute) attribute.accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(UmlEnum o, OclCopier.Argument argument) {
         if (argument.map.containsKey(o)) {
             return argument.map.get(o);
         }
         UmlEnum result = new UmlEnum();
         argument.map.put(o, result);
         visitElement(o, result, argument);
 
        result.setName(o.getName());
         for (UmlClass subClass : o.getSubClasses()) {
             result.addSubClass((UmlClass) subClass.accept((UmlVisitor<Element, OclCopier.Argument>) this, argument));
         }
         for (UmlClass superClass : o.getSuperClasses()) {
             result.addSuperClass((UmlClass) superClass.accept((UmlVisitor<Element, OclCopier.Argument>) this, argument));
         }
         for (Operation operation : o.getOperations()) {
             result.addOperation((Operation) operation.accept(this, argument));
         }
         for (AssociationEnd associationEnd : o.getOwnedAssociationEnds()) {
             result.addOwnedAssociationEnd((AssociationEnd) associationEnd.accept(this, argument));
         }
         for (Attribute attribute : o.getOwnedAttributes()) {
             result.addOwnedAttribute((Attribute) attribute.accept(this, argument));
         }
         for (UmlEnumLiteral umlEnumLiteral : o.getLiterals()) {
             result.addLiteral((UmlEnumLiteral) umlEnumLiteral.accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(UmlEnumLiteral o, OclCopier.Argument argument) {
         if (argument.map.containsKey(o)) {
             return argument.map.get(o);
         }
         UmlEnumLiteral result = new UmlEnumLiteral(o.getName());
         argument.map.put(o, result);
         visitElement(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(Namespace o, OclCopier.Argument argument) {
         if (argument.map.containsKey(o)) {
             return argument.map.get(o);
         }
         Namespace result = new Namespace();
         argument.map.put(o, result);
         visitElement(o, result, argument);
 
         result.setName(o.getName());
 
         for (Element element : o.getOwnedMembers()) {
             boolean implicit = o.isImplicit(element);
             result.addMember(element.accept(this, argument), implicit);
         }
 
         return result;
     }
 
     /*
      * *********************************************************************************************
      * OclExpressions
      * ********************************************************************************************
      */
     /*
      * Ocl expressions clones are not cached in argument map to avoid that two equals input objects
      * was mapped to one unique output object
      */
     private void visitOclExpression(OclExpression input, OclExpression output, OclCopier.Argument argument) {
         visitElement(input, output, argument);
     }
 
     private void visitCallExp(CallExp input, CallExp output, OclCopier.Argument argument) {
         visitOclExpression(input, output, argument);
         if (input.getSource() != null) {
             output.setSource((OclExpression) input.getSource().accept(this, argument));
         }
     }
 
     private void visitFeatureCallExp(FeatureCallExp input, FeatureCallExp output, OclCopier.Argument argument) {
         visitCallExp(input, output, argument);
         output.setPre(input.isPre());
     }
 
     private void visitNavigationCallExp(NavigationCallExp input, NavigationCallExp output, OclCopier.Argument argument) {
         visitFeatureCallExp(input, output, argument);
         if (input.getNavigationSource() != null) {
             output.setNavigationSource((AssociationEnd) input.getNavigationSource().accept(this, argument));
         }
 
         for (OclExpression oclExpression : input.getQualifiers()) {
             output.addQualifier((OclExpression) oclExpression.accept(this, argument));
         }
     }
 
     private void visitLiteralExp(LiteralExp input, LiteralExp output, OclCopier.Argument argument) {
         visitOclExpression(input, output, argument);
     }
 
     private void visitPrimitiveLiteralExp(PrimitiveLiteralExp input, PrimitiveLiteralExp output, OclCopier.Argument argument) {
         visitLiteralExp(input, output, argument);
     }
 
     private void visitLoopExp(LoopExp input, LoopExp output, OclCopier.Argument argument) {
         visitCallExp(input, output, argument);
         for (Variable variable : input.getIterators()) {
             Variable var = (Variable) variable.accept(this, argument);
             output.addIterator(var);
             assert var.getTreeParent() == output;
         }
         if (input.getBody() != null) {
             output.setBody((OclExpression) input.getBody().accept(this, argument));
         }
     }
 
     @Override
     public Element visit(AssociationEndCallExp o, OclCopier.Argument argument) {
         AssociationEndCallExp result = new AssociationEndCallExp();
         visitNavigationCallExp(o, result, argument);
 
         if (o.getReferredAssociationEnd() != null) {
             result.setReferredAssociationEnd((AssociationEnd) o.getReferredAssociationEnd().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(AttributeCallExp o, OclCopier.Argument argument) {
         AttributeCallExp result = new AttributeCallExp();
         visitFeatureCallExp(o, result, argument);
 
         if (o.getReferredAttribute() != null) {
             result.setReferredAttribute((Attribute) o.getReferredAttribute().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(BooleanLiteralExp o, OclCopier.Argument argument) {
         BooleanLiteralExp result = new BooleanLiteralExp(o.getValue());
         visitPrimitiveLiteralExp(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(CollectionLiteralExp o, OclCopier.Argument argument) {
         CollectionLiteralExp result = new CollectionLiteralExp();
         visitLiteralExp(o, result, argument);
 
         if (o.getType() != null) {
             result.setType((CollectionType) o.getType().accept(this, argument));
         }
         for (CollectionLiteralPart part : o.getParts()) {
             result.addPart((CollectionLiteralPart) part.accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(EnumLiteralExp o, OclCopier.Argument argument) {
         EnumLiteralExp result = new EnumLiteralExp();
         visitLiteralExp(o, result, argument);
 
         if (o.getEnumerationLiteral() != null) {
             result.setEnumerationLiteral((UmlEnumLiteral) o.getEnumerationLiteral().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(IfExp o, OclCopier.Argument argument) {
         IfExp result = new IfExp();
         visitOclExpression(o, result, argument);
 
         if (result.getCondition() != null) {
             result.setCondition((OclExpression) o.getCondition().accept(this, argument));
         }
         if (result.getThenExpression() != null) {
             result.setThenExpression((OclExpression) o.getThenExpression().accept(this, argument));
         }
         if (result.getElseExpression() != null) {
             result.setElseExpression((OclExpression) o.getElseExpression().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(IntegerLiteralExp o, OclCopier.Argument argument) {
         IntegerLiteralExp result = new IntegerLiteralExp(o.getValue());
         visitPrimitiveLiteralExp(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(InvalidLiteralExp o, OclCopier.Argument argument) {
         InvalidLiteralExp result = new InvalidLiteralExp();
         visitOclExpression(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(IterateExp o, OclCopier.Argument argument) {
         IterateExp result = new IterateExp();
         visitLoopExp(o, result, argument);
 
         if (o.getResult() != null) {
             result.setResult((Variable) o.getResult().accept(this, argument));
             assert result.getResult().getTreeParent() == result;
         }
 
         return result;
     }
 
     @Override
     public Element visit(IteratorExp o, OclCopier.Argument argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Element visit(LetExp o, OclCopier.Argument argument) {
         LetExp result = new LetExp();
         visitOclExpression(o, result, argument);
 
         if (o.getVariable() != null) {
             result.setVariable((Variable) o.getVariable().accept(this, argument));
             assert result.getVariable().getTreeParent() == result;
         }
         if (o.getIn() != null) {
             result.setIn((OclExpression) o.getIn().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(MessageExp o, OclCopier.Argument argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Element visit(NullLiteralExp o, OclCopier.Argument argument) {
         NullLiteralExp result = new NullLiteralExp();
         visitOclExpression(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(OperationCallExp o, OclCopier.Argument argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Element visit(RealLiteralExp o, OclCopier.Argument argument) {
         RealLiteralExp result = new RealLiteralExp(o.getValue());
         visitPrimitiveLiteralExp(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(StateExp o, OclCopier.Argument argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Element visit(StringLiteralExp o, OclCopier.Argument argument) {
         StringLiteralExp result = new StringLiteralExp(o.getValue());
         visitPrimitiveLiteralExp(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(TupleAttributeCallExp o, Argument argument) {
         TupleAttributeCallExp result = new TupleAttributeCallExp(o);
         visitCallExp(o, result, argument);
         return result;
     }
 
     @Override
     public Element visit(TupleLiteralExp o, OclCopier.Argument argument) {
         TupleLiteralExp result = new TupleLiteralExp();
         visitLiteralExp(o, result, argument);
 
         for (TupleLiteralPart part : o.getParts()) {
             result.addPart((TupleLiteralPart) part.accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(TypeExp o, OclCopier.Argument argument) {
         TypeExp result = new TypeExp((Classifier) o.getType().accept(this, argument));
         visitOclExpression(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(UnlimitedNaturalExp o, OclCopier.Argument argument) {
         UnlimitedNaturalExp result = new UnlimitedNaturalExp(o.getValue());
         visitPrimitiveLiteralExp(o, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(VariableExp o, OclCopier.Argument argument) {
         VariableExp result = new VariableExp();
         visitOclExpression(o, result, argument);
 
         if (o.getReferredVariable() != null) {
             result.setReferredVariable((Variable) o.getReferredVariable().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(Variable o, OclCopier.Argument argument) {
         /*
          * Unlike the expressions, variables are cached (all expressions VariableExp in the same
          * expression share the variable
          */
         if (argument.map.containsKey(o)) {
             return argument.map.get(o);
         }
         Variable result = new Variable(o.getName());
         argument.map.put(o, result);
         visitElement(o, result, argument);
 
         if (o.getType() != null) {
             result.setType((Classifier) o.getType().accept(this, argument));
         }
         if (o.getInitExpression() != null) {
             result.setInitExpression((OclExpression) o.getInitExpression().accept(this, argument));
         }
         //result.scope will be set when o.scope was translated
 
         return result;
     }
 
     /*
      * *********************************************************************************************
      * TYPES
      * ********************************************************************************************
      */
     private void visitCollectionType(CollectionType input, CollectionType output, Argument argument) {
         if (input.getElementType() != null) {
             output.setElementType((Classifier) input.getElementType().accept(this, argument));
         }
     }
 
     @Override
     public Element visit(AnyType anyType, OclCopier.Argument argument) {
         return AnyType.getInstance();
     }
 
     @Override
     public Element visit(BagType bagType, OclCopier.Argument argument) {
         if (argument.map.containsKey(bagType)) {
             return argument.map.get(bagType);
         }
         BagType result = new BagType();
         argument.map.put(bagType, result);
 
         visitCollectionType(bagType, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(CollectionType collectionType, OclCopier.Argument argument) {
         if (argument.map.containsKey(collectionType)) {
             return argument.map.get(collectionType);
         }
         CollectionType result = new CollectionType();
         argument.map.put(collectionType, result);
 
         visitCollectionType(collectionType, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(InvalidType invalidType, OclCopier.Argument argument) {
         return InvalidType.getInstance();
     }
 
     @Override
     public Element visit(MessageType messageType, OclCopier.Argument argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Element visit(OrderedSetType orderedSetType, OclCopier.Argument argument) {
         if (argument.map.containsKey(orderedSetType)) {
             return argument.map.get(orderedSetType);
         }
         OrderedSetType result = new OrderedSetType();
         argument.map.put(orderedSetType, result);
 
         visitCollectionType(orderedSetType, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(PrimitiveType primitiveType, OclCopier.Argument argument) {
         return PrimitiveType.getInstance(primitiveType.getPrimitiveKind());
     }
 
     @Override
     public Element visit(SequenceType sequenceType, OclCopier.Argument argument) {
         if (argument.map.containsKey(sequenceType)) {
             return argument.map.get(sequenceType);
         }
         SequenceType result = new SequenceType();
         argument.map.put(sequenceType, result);
 
         visitCollectionType(sequenceType, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(TemplateParameterType templateParameterType, OclCopier.Argument argument) {
         return new TemplateParameterType(templateParameterType);
     }
 
     @Override
     public Element visit(TupleType tupleType, OclCopier.Argument argument) {
         if (argument.map.containsKey(tupleType)) {
             return argument.map.get(tupleType);
         }
         TupleType result = new TupleType();
         argument.map.put(tupleType, result);
 
         for (String attId : tupleType.getAllAttributes().keySet()) {
             result.addAttribute(attId, (Classifier) tupleType.accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(SetType setType, OclCopier.Argument argument) {
         if (argument.map.containsKey(setType)) {
             return argument.map.get(setType);
         }
         SetType result = new SetType();
         argument.map.put(setType, result);
 
         visitCollectionType(setType, result, argument);
 
         return result;
     }
 
     @Override
     public Element visit(VoidType voidType, OclCopier.Argument argument) {
         return VoidType.getInstance();
     }
 
     @Override
     public Element visit(ClassifierType classifierType, OclCopier.Argument argument) {
         Classifier other = (Classifier) classifierType.getReferredClassifier().accept(this, argument);
         return other.getClassifierType();
     }
 
     @Override
     public Element visit(CollectionRange obj, OclCopier.Argument argument) {
         CollectionRange result = new CollectionRange();
         if (obj.getFirst() != null) {
             result.setFirst((OclExpression) obj.getFirst().accept(this, argument));
         }
         if (obj.getLast() != null) {
             result.setLast((OclExpression) obj.getLast().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(CollectionItem obj, OclCopier.Argument argument) {
         CollectionItem result = new CollectionItem();
         if (obj.getItem() != null) {
             result.setItem((OclExpression) obj.getItem().accept(this, argument));
         }
 
         return result;
     }
 
     @Override
     public Element visit(TupleLiteralPart tupleLiteralPart, OclCopier.Argument argument) {
         return new TupleLiteralPart((Variable) tupleLiteralPart.getAttribute().accept(this, argument));
     }
 
     public static class Argument {
 
         final Map<Element, Element> map;
         final boolean mapAnnotations;
         final boolean cloneAnnotations;
 
         public Argument(boolean mapAnnotations, boolean cloneAnnotations) {
             this.mapAnnotations = mapAnnotations;
             this.cloneAnnotations = cloneAnnotations;
             map = new HashMap<Element, Element>();
         }
 
         public Argument(Map<Element, Element> map, boolean mapAnnotations, boolean cloneAnnotations) {
             this.map = map;
             this.mapAnnotations = mapAnnotations;
             this.cloneAnnotations = cloneAnnotations;
         }
 
         public boolean isCloneAnnotations() {
             return cloneAnnotations;
         }
 
         public Map<Element, Element> getMap() {
             return map;
         }
 
         public boolean isMapAnnotations() {
             return mapAnnotations;
         }
 
         /**
          * After call this method, the map has no key of expression package. it is specially
          * importat wih variables.
          */
         public void cleanExpressionObjects() {
             List<Element> elementsToRemove = new LinkedList<Element>();
             for (Element element : map.keySet()) {
                 if (element instanceof OclExpression
                         || element instanceof CollectionLiteralPart
                         || element instanceof TupleLiteralPart
                         || element instanceof Variable) {
 
                     elementsToRemove.add(element);
                 }
             }
             for (Element element : elementsToRemove) {
                 map.remove(element);
             }
         }
     }
 }
