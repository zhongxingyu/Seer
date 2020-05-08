 package domain.UmlClass;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class UmlClassModel {
 	private AccessModifier _accessModifier;
 	private String _name;
 	private List<UmlAssociationModel> _associations;
 	private List<UmlAttributeModel> _attributes;
 	private List<UmlMethodModel> _methods;
 	private boolean _isAbstract; 	
 	private boolean _isInterface; 	
 
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
 
 	public UmlAssociationModel addAssociation(String assocTarget,
 			AssociationType associationType) {
 		if (assocTarget == null || associationType == null) return null;
 		// if associations set includes a UmlAssociationModel with SAME target
 		// and a
 		// DIFFERENT associationType, throw exception
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().equals(assocTarget) && !association.getType().equals(associationType)) {
 				return null;
 			}
 		}
 
 		// do nothing if association already exists
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().equals(assocTarget) && association.getType().equals(associationType)) {
 				return null;
 			}
 		}
 
 		// add new UmlAssociationModel
 		UmlAssociationModel newAssociationModel = new UmlAssociationModel(assocTarget, associationType);
 		_associations.add(newAssociationModel);
 		return newAssociationModel;
 	}
 
 	public UmlAssociationModel removeAssociation(UmlClassModel target) {
 		// if multiple tuples in association set have this target, throw
 		// exception
 		for (UmlAssociationModel associationA : _associations) {
 			for (UmlAssociationModel associationB : _associations) {
 				if (associationA.getTarget().equals(associationB.getTarget()) && !associationA.equals(associationB)) {
 					return null;
 				}
 			}
 		}
 		// if association does not exist in the set, throw exception
 		boolean associationFound = false;
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().equals(target.getName())) {
 				associationFound = true;
 				break;
 			}
 		}
 		if (!associationFound) {
 			return null;
 		}
 
 		// remove the UmlAssociationModel
 		for (UmlAssociationModel association : _associations) {
 			if (association.getTarget().equals(target.getName())) {
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
 			if (association.getTarget() == this.getName()
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
 			}
 		}
 
 		// add new UmlMethodModel
 		_methods.add(method);
 		return true;
 	}
 
 	public boolean removeMethod(UmlMethodModel method) {
 		boolean methodFound = false;
 		for (UmlMethodModel methodModel : _methods) {
 			if (methodModel.getName().equals(method.getName())) {
 				methodFound = true;
 				break;
 			}
 		}
 
 		if (!methodFound) {
 			return false;
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
 			
 		sb.append(getAccessModifier().toString().toLowerCase()).append(" ");
 		
 		if (getInterfaceFlag()) {
 			sb.append("interface ");
 		}
 		else if (getAbstractFlag()) {
 			sb.append("abstract class ");
 		}
 		else {
 			sb.append("class ");
 		}
 		
		sb.append(getName()).append(" ");
 
 		for (UmlAssociationModel association : _associations) {
 			if (association.getType().toString().equals("Inheritance")) {
 				sb.append("extends ");
				sb.append(association.getTarget()).append(" ");
 			}
 			if(association.getType().toString().equals("Aggregation")){
				sb.append("aggregate of ");
				sb.append(association.getTarget()).append(" ");
 			}
 			if(association.getType().toString().equals("Dependency")){
 				sb.append("dependent of ");
				sb.append(association.getTarget()).append(" ");
 			}
 		}
 			
		sb.append(" {\n"); 
 		
 		for (UmlAttributeModel value : _attributes) {
 			sb.append("\t");
 			sb.append(value.getAccessModifier().toString().toLowerCase()).append(" ");
 			if (value.getStaticFlag()) {
 				sb.append("static ");
 			}
 			sb.append(value.getType()).append(" ");
 			sb.append(value.getName()).append(";\n");
 		}
 		
 		sb.append("\n");
 
 		for (UmlMethodModel value : _methods) {
 			sb.append("\t");
 			sb.append(value.getAccessModifier().toString().toLowerCase()).append(" ");
 			
 			if (value.getStaticFlag()) {
 				sb.append("static ");
 			}
 			else if (value.getAbstractFlag()) {
 				sb.append("abstract ");
 			}
 				
 			sb.append(value.getReturnType()).append(" ");
 			sb.append(value.getName()).append(" (");
 			for(int i = 0; i < value.getParameters().size(); ++ i) { 
 				if(i == value.getParameters().size() -1) {
 					sb.append(value.getParameters().get(i).getType() + " " + 
 				value.getParameters().get(i).getName());
 				}
 				else {
 					sb.append(value.getParameters().get(i).getType() + " " +
 				value.getParameters().get(i).getName() + ", ");
 				}
 			}
 			sb.append(") {"); 
 			sb.append("\n\n\t}\n");
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
 
 	public boolean getAbstractFlag() {
 		return _isAbstract;
 	}
 
 	public void setAbstractFlag(boolean isAbstract) {
 		this._isAbstract = isAbstract;
 	}
 
 	public boolean getInterfaceFlag() {
 		return _isInterface;
 	}
 
 	public void setInterfaceFlag(boolean isInterface) {
 		this._isInterface = isInterface;
 	}
 }
