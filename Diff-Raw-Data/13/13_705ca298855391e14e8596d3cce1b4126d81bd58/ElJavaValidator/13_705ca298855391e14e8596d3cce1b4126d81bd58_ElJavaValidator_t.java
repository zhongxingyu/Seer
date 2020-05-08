 package org.regeinc.lang.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.xtext.validation.Check;
 import org.regeinc.lang.el.Association;
 import org.regeinc.lang.el.Contract;
 import org.regeinc.lang.el.ElPackage;
 import org.regeinc.lang.el.Element;
 import org.regeinc.lang.el.Entity;
 import org.regeinc.lang.el.Import;
 import org.regeinc.lang.el.LocalVariableDeclaration;
 import org.regeinc.lang.el.MethodDeclaration;
 import org.regeinc.lang.el.MethodDefinition;
 import org.regeinc.lang.el.Model;
 import org.regeinc.lang.el.Parameter;
 import org.regeinc.lang.el.Reference;
 import org.regeinc.lang.el.Type;
 
 /**
  * this class checks for
  * uniqueness, completeness and making sure that 
  * right thing is located at the right place (extension to scoping)
  * 
  * @author nilesh
  *
  */
 public class ElJavaValidator extends AbstractElJavaValidator {
 	
 	@Check
 	public void checkTypeParameterWithoutType(org.regeinc.lang.el.TypeParameter typeParameter){
 		if(typeParameter.eContainer() instanceof Element){			
 			error("keyword class or interface missing", ElPackage.eINSTANCE.eContainingFeature());
 		}
 	}
 	
 	@Check
 	public void checkDuplicateImport(Import importStatement){
 		for(Element element: ((Model)importStatement.eContainer()).getAllElement()){
 			for(Import imp:element.getAllImport()){
 				if(imp.equals(importStatement)){
 					continue;
 				}
 				if(imp.getImportedNamespace().equals(importStatement.getImportedNamespace())){
 					warning("duplicate import", ElPackage.eINSTANCE.eContainingFeature());	
 				}	
 			}			
 		}
 	}
 	
 	@Check
 	public void checkStateCyclicReferenceToSelf(org.regeinc.lang.el.State state){
 
 	}
 	
 	@Check
 	public void checkDuplicateAssociation(Association association){
 		List<Association> allAssociation = ((Entity)association.eContainer()).getAllAssociation();
 		for(Association nextAssociation: allAssociation){
 			if(association.equals(nextAssociation)){
 				continue;
 			}else{
 				if(association.getQualifiedReference().getReference().getName().equals(nextAssociation.getQualifiedReference().getReference().getName())){
 					error("duplicate association", ElPackage.eINSTANCE.eContainingFeature());
 					break;
 				}
 			}
 		}
 	}
 
 	@Check
 	public void checkDuplicateState(org.regeinc.lang.el.State state){
 		List<org.regeinc.lang.el.State> allState = ((Entity)state.eContainer()).getAllState();
 		for(org.regeinc.lang.el.State existingState: allState){
 			if(existingState.equals(state)){
 				continue;
 			}else{
 				if(existingState.getName().equals(state.getName())){
 					error("duplicate state", ElPackage.eINSTANCE.eContainingFeature());
 					break;
 				}
 			}
 		}
 	}
 
 	@Check
 	public void checkDuplicateMethodDeclaration(MethodDeclaration newMethodDeclaration){
 		if(newMethodDeclaration.eContainer() instanceof Contract){
 			for(MethodDeclaration existingMethodDeclaration: ((Contract)newMethodDeclaration.eContainer()).getAllMethodDeclaration()){
 				if(existingMethodDeclaration.equals(newMethodDeclaration)){
 					continue;
 				}
 				if(isSameMethodDeclaration(existingMethodDeclaration,newMethodDeclaration)){		
 					error("duplicate method", ElPackage.eINSTANCE.eContainingFeature());
 					break;
 				}
 			}
 		}else if(newMethodDeclaration.eContainer() instanceof Entity){
 			for(MethodDefinition methodDefinition: ((Entity)newMethodDeclaration.eContainer().eContainer()).getAllMethodDefinition()){
 				MethodDeclaration existingMethodDeclaration = methodDefinition.getMethodDeclaration();
 				if(existingMethodDeclaration.equals(newMethodDeclaration)){
 					continue;
 				}else{
 					if(isSameMethodDeclaration(existingMethodDeclaration,newMethodDeclaration)){		
 						error("duplicate method", ElPackage.eINSTANCE.eContainingFeature());
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	boolean isSameMethodDeclaration(MethodDeclaration first, MethodDeclaration second){
 		if(isSameMethodName(first, second)){ // same name
 			if(first.getParameter()!=null && second.getParameter()!=null){
 				if(isSameParameter(first.getParameter(), second.getParameter())){
 					return true;
 				}
 			}	
 		}
 		return false;
 	}
 	
 	boolean isSameMethodName(MethodDeclaration first, MethodDeclaration second){
 		return first.getName().equals(second.getName());
 	}
 
 	boolean isSameParameter(Parameter first, Parameter second){
 		Type firstType = first.getReference().getParameterizedType().getType();
 		Type secondType = second.getReference().getParameterizedType().getType(); 
 		if(isSameType(firstType, secondType)){
 			if(first.isList() && second.isList()){
 				return isSameParameter(first.getNext(), second.getNext());
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	boolean isSameType(Type first, Type second){
 		String firstFullName = ((Element)first.eContainer()).getPkg().getName()+"."+first.getName();
 		String secondFullName = ((Element)second.eContainer()).getPkg().getName()+"."+first.getName();
 		
 		return firstFullName.equals(secondFullName);
 	}
 	
 	@Check
 	public void checkClassWithAbstractMethodBeDeclaredAbstract(Entity entity){
 		for(MethodDefinition methodDefinition : entity.getAllMethodDefinition()){
 			if(methodDefinition.isABSTRACT() && !entity.isABSTRACT()){
 				error("class with abstract method must be declared abstract", ElPackage.eINSTANCE.eContainingFeature());
 				break;
 			}	
 		}		
 	}
 	
	@Check
 	public void checksForInstanceMethod(MethodDefinition methodDefinition){
 		if(methodDefinition.getMethodDeclaration().getName().equals("instance")){
 			if(!(methodDefinition.getMethodDeclaration().getParameterizedType().getType()!=null) 
					|| !(isOfType(methodDefinition.getMethodDeclaration().getParameterizedType().getType(),(Type)methodDefinition.eContainer()))){
 				error("instance method must return instance of the enclosing class or subclass", ElPackage.eINSTANCE.eContainingFeature());
 			}
 			List<Reference> identities = identities(((Entity)methodDefinition.eContainer()).getAllAssociation());
 			if(identities.size()>0 && methodDefinition.getMethodDeclaration().getParameter() == null){
 				error("instance method must declare all identity fields", ElPackage.eINSTANCE.eContainingFeature());
 			}else{
 				List<Reference> result = new ArrayList<Reference>();
 				result.addAll(identities);
 				for(Reference identity : identities){
 					Parameter param = methodDefinition.getMethodDeclaration().getParameter();
 					if(param.getReference().getParameterizedType().getType().getName().equals(identity.getParameterizedType().getType().getName())){
 						result.remove(identity);
 					}
 				}
 				if(result.size()>0){
 					error("instance method must declare all identity fields", ElPackage.eINSTANCE.eContainingFeature());
 				}
 			}
 			if(!methodDefinition.getVisibility().toString().equals("public")){
 				error("instance method must be declared public", ElPackage.eINSTANCE.eContainingFeature());
 			}
 		}
 	}
 	
 	boolean isOfType(Type source, Type destination){
 		boolean flag = false;
 		if(destination.getName().equals(source.getName())){
 			flag = true;
 		}else{
			if(source.getParameterizedType().getType()!=null){
				flag = isOfType(source.getParameterizedType().getType(), destination);
 			}
 		}
 		return flag;
	}
 	
 	List<Reference> identities(List<Association> associationList){
 		List<Reference> identityList = new ArrayList<Reference>();
 		for(Association association: associationList){
 			if(association.isIDENTITY()){
 				identityList.add(association.getQualifiedReference().getReference());
 			}
 		}
 		return identityList;
 	}
 	
 	@Check
 	public void checkAbstractMethodsNotDeclaredPrivate(MethodDefinition methodDefinition){
 		if(!(methodDefinition.eContainer() instanceof Contract)){
 			if(methodDefinition.isABSTRACT()){
 				if(methodDefinition.getVisibility().toString().equals("private")){
 					error("abstract methods can not be declared private", ElPackage.eINSTANCE.eContainingFeature());	
 				}
 			}
 		}
 	}
 	
 	@Check
 	public void checkTypeMatches(LocalVariableDeclaration localVariableDeclaration){
 		
 	}
 
 }
