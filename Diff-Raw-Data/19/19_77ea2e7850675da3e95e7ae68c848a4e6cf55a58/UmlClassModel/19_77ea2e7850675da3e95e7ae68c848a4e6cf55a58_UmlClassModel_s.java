 package domain.UmlClass;
 
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 public class UmlClassModel {
 	private AccessModifier _accessModifier;
 	private String _name;
 	private List<UmlAssociationModel> _associations;
 	private List<UmlAttributeModel> _attributes;
 	private List<UmlMethodModel> _methods;
 
 	public UmlClassModel() {
 		this(AccessModifier.Public, "newClass");
 	}
 
 	public UmlClassModel(AccessModifier accessMod, String name) {
 		_accessModifier = accessMod;
 		_name = name;
 		_associations = new ArrayList<UmlAssociationModel>();
 		_attributes = new ArrayList<UmlAttributeModel>();
 		_methods = new ArrayList<UmlMethodModel>();
 	}
 
 	public UmlAssociationModel addAssociation(UmlClassModel target,
 			AssociationType associationType) {
 		if (target == null || associationType == null) return null;
 		// if associations set includes a UmlAssociationModel with SAME target
 		// and a
 		// DIFFERENT associationType, throw exception
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().getName().equals(target.getName())
 					&& !association.getType().equals(associationType)) {
 				return null;
 			}
 		}
 
 		// do nothing if association already exists
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().getName().equals(target.getName())
 					&& association.getType().equals(associationType)) {
 				return null;
 			}
 		}
 
 		// add new UmlAssociationModel
 		UmlAssociationModel newAssociationModel = new UmlAssociationModel(
 				target, associationType);
 		_associations.add(newAssociationModel);
 		return newAssociationModel;
 	}
 
 	public UmlAssociationModel removeAssociation(UmlClassModel target) {
 		// if multiple tuples in association set have this target, throw
 		// exception
 		for (UmlAssociationModel associationA : _associations) {
 			for (UmlAssociationModel associationB : _associations) {
 				if (associationA.getTarget().getName()
 						.equals(associationB.getTarget().getName())
 						&& !associationA.equals(associationB)) {
 					return null;
 				}
 			}
 		}
 		// if association does not exist in the set, throw exception
 		boolean associationFound = false;
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().getName().equals(target.getName())) {
 				associationFound = true;
 				break;
 			}
 		}
 		if (!associationFound) {
 			return null;
 		}
 
 		// remove the UmlAssociationModel
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().getName().equals(target.getName())) {
 				_associations.remove(association);
 				return association;
 			}
 		}
 		return null;
 	}
 
 	// maybe this would make more sense as "willCreateInheritanceCycle"
 	public boolean hasInheritanceCycle(UmlClassModel target) {
 		// "a super class cannot inherit from one of its subclasses"
 		// before creating an inheritance association, we inspect the
 		// associations of target;
 		// if target inherits from this, return true
 		List<UmlAssociationModel> targetAssociations = target.getAssociations();
 		for (UmlAssociationModel association : targetAssociations) {
 			if (association.getTarget() == this
 					&& association.getType() == AssociationType.Inheritance)
 				return true;
 		}
 		return false;
 	}
 
 	public boolean addAttribute(UmlAttributeModel attr) {
 		// check for duplicate, throw exception if found
 		for (UmlAttributeModel attribute : _attributes) {
 			if (attribute.getName().equals(attr.getName())) {
 				return false;
 			}
 		}
 
 		// add new UmlAttributeModel
 		_attributes.add(attr);
 		return true;
 	}
 
 	public boolean removeAttribute(UmlAttributeModel attr) {
 		// throw exception if cannot find attr to remove
 		boolean attributeFound = false;
 		for (UmlAttributeModel attribute : _attributes) {
 			if (attribute.getName().equals(attr.getName())) {
 				attributeFound = true;
 				break;
 			}
 		}
 		if (!attributeFound) {
 			return false;
 		}
 
 		// remove UmlAttributeModel
 		for (UmlAttributeModel attribute : _attributes) {
 			if (attribute.getName().equals(attr.getName())) {
 				_attributes.remove(attribute);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean addMethod(UmlMethodModel method) {
 		// check for duplicate, throw exception if found
 		for (UmlMethodModel methodModel : _methods) {
 			if (methodModel.getName().equals(method.getName())) {
 				return false;
 				// throw new Exception ("Duplicate method found.");
 			}
 		}
 
 		// ad new UmlMethodModel
 		_methods.add(method);
 		return true;
 	}
 
 	public boolean removeMethod(UmlMethodModel method) {
 		// throw exception if cannot find method to remove
 		boolean methodFound = false;
 		for (UmlMethodModel methodModel : _methods) {
 			if (methodModel.getName().equals(method.getName())) {
 				methodFound = true;
 				break;
 			}
 		}
 
 		if (!methodFound) {
 			return false;
 			// throw new Exception ("Method not found.");
 		}
 
 		// remove UmlMethodModel
 		for (UmlMethodModel methodModel : _methods) {
 			if (methodModel.getName().equals(method.getName())) {
 				_methods.remove(methodModel);
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	// code skeleton
 	public String toString(){
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append(getAccessModifier().toString().toLowerCase()).append(" class ")
 				.append(getName()).append(" ");
 
 		for (UmlAssociationModel association : _associations) {
 			if (association.getType().equals("Inheritance")) {
 				sb.append("Extends ");
 				sb.append(association.getTarget().getName());
 			}
 			
 		}
 			
		sb.append(getAssociations()).append(" {\n"); 
 		
 		for (UmlAttributeModel value : _attributes) {
 			sb.append("\t");
 			sb.append(value.getAccessModifier().toString().toLowerCase()).append(" ")
 					.append(value.getType()).append(" ")
 					.append(value.getName()).append(";\n");
 		}
 		sb.append("\n");
 
 		for (UmlMethodModel value : _methods) {
 			sb.append("\t");
 			sb.append(value.getAccessModifier().toString().toLowerCase()).append(" ");
 			sb.append(value.getReturnType()).append(" ");
			sb.append(value.getName()).append(" (")
					.append(value.getParameters()).append(") {\n");
			sb.append("\n\t}\n");
 		}
 		sb.append("}\n");
 
 		String output = sb.toString();
 		return output;
 	}
 	
 
 	public AccessModifier getAccessModifier() {
 		return _accessModifier;
 	}
 
 	public void setAccessModifier(AccessModifier accessModifier) {
 		this._accessModifier = accessModifier;
 	}
 
 	public List<UmlAssociationModel> getAssociations() {
 		return _associations;
 	}
 
 	public void setAssociations(List<UmlAssociationModel> associations) {
 		this._associations = associations;
 	}
 
 	public String getName() {
 		return _name;
 	}
 
 	public void setName(String name) {
 		this._name = name;
 	}
 
 	public List<UmlAttributeModel> getAttributes() {
 		return _attributes;
 	}
 
 	public void setAttributes(List<UmlAttributeModel> attributes) {
 		this._attributes = attributes;
 	}
 
 	public List<UmlMethodModel> getMethods() {
 		return _methods;
 	}
 
 	public void setMethods(List<UmlMethodModel> methods) {
 		this._methods = methods;
 	}
 }
