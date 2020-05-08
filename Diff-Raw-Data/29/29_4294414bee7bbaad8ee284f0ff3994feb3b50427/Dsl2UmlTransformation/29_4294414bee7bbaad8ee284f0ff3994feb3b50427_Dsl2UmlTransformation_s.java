 /*
  *    Project: The EDNA Kernel
  *             http://www.edna-site.org
  *
  *    File: "$Id:$"
  *
  *    Copyright (C) 2008-2009 European Synchrotron Radiation Facility
  *                            Grenoble, France
  *
  *    Principal authors: Marie-Francoise Incardona (incardon@esrf.fr)
  *                       Olof Svensson (svensson@esrf.fr)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published
  *    by the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    and the GNU Lesser General Public License  along with this program.
  *    If not, see <http://www.gnu.org/licenses/>.
  */
 package org.edna.datamodel.transformations.m2m;
 
 import static org.edna.datamodel.transformations.m2m.NamingUtil.normalize;
 
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.uml2.uml.Association;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.Comment;
 import org.eclipse.uml2.uml.Generalization;
 import org.eclipse.uml2.uml.PrimitiveType;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.UMLFactory;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.resource.UMLResource;
 import org.eclipse.xtext.EcoreUtil2;
 import org.eclipse.xtext.util.Strings;
 import org.edna.datamodel.datamodel.ComplexType;
 import org.edna.datamodel.datamodel.ElementDeclaration;
 import org.edna.datamodel.datamodel.util.DatamodelSwitch;
 import org.edna.datamodel.transformations.util.JavaExtensions;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * Transforms an EDNA Datamodel to UML.
  * @author Karsten Thoms (karsten.thoms@itemis.de)
  */
 public class Dsl2UmlTransformation extends AbstractDatamodelTransformation<org.edna.datamodel.datamodel.Model, org.eclipse.uml2.uml.Model>{
 
 	@Override
 	public org.eclipse.uml2.uml.Model transform (org.edna.datamodel.datamodel.Model sourceModel) {
 
 		this.sourceModel = sourceModel;
 		this.targetModel = UMLFactory.eINSTANCE.createModel();
 
 
 		// resolve all proxies
 		EcoreUtil.resolveAll(sourceModel.eResource());
 
 		// Iterate recursively through the model and create the structure
 		modelTraverser.doSwitch(sourceModel);
 
 		transformDocumentation(sourceModel);
 		monitor.worked(1);
 
 		if (monitor.isCanceled()) return null;
 		return targetModel;
 	}
 
 	private ModelTraverser modelTraverser = new ModelTraverser();
 	/**
 	 * This one iterates through the model and handles the Model object, Packages and Complex Types.
 	 */
 	private class ModelTraverser extends DatamodelSwitch<EObject> {
 		/**
 		 * The packageStack is to remember the current Package used in the transformation in order to assign
 		 * the Complex Types to the right packages.
 		 */
 		private Stack<org.eclipse.uml2.uml.Package> packageStack = new Stack<org.eclipse.uml2.uml.Package>();
 
 		public EObject caseModel(org.edna.datamodel.datamodel.Model object) {
 			packageStack.push(targetModel);
 			for (EObject obj : object.eContents()) {
 				doSwitch(obj);
 			}
 			packageStack.pop();
 			return targetModel;
 		};
 		public EObject caseComplexType(ComplexType object) {
 			if (monitor.isCanceled()) return null;
 			monitor.subTask(object.getName());
 			Class type = createClass.apply(object);
 			currentPackage().getPackagedElements().add(type);
 			monitor.worked(1);
 			return type;
 		};
 		public EObject casePackage(org.edna.datamodel.datamodel.Package object) {
 			org.eclipse.uml2.uml.Package target = createPackage.apply(object);
 			currentPackage().getPackagedElements().add(target);
 			packageStack.push(target);
 			for (EObject obj : object.eContents()) {
 				doSwitch(obj);
 			}
 			packageStack.pop();
 			return target;
 		};
 
 		public org.eclipse.uml2.uml.Package currentPackage () {
 			return packageStack.peek();
 		}
 	};
 
 
 	private EmfCreateFunction<org.edna.datamodel.datamodel.Package, org.eclipse.uml2.uml.Package> createPackage =
 		new EmfCreateFunction<org.edna.datamodel.datamodel.Package, org.eclipse.uml2.uml.Package>(UMLPackage.eINSTANCE.getPackage()) {
 
 			@Override
 			protected org.eclipse.uml2.uml.Package configure(org.edna.datamodel.datamodel.Package source,
 					org.eclipse.uml2.uml.Package newInstance) {
 				newInstance.setName(normalize(source.getName()));
 				return null;
 			}
 
 	};
 
 	protected void transformDocumentation(org.edna.datamodel.datamodel.Model sourceModel) {
 		for (ComplexType cls: EcoreUtil2.getAllContentsOfType(sourceModel, ComplexType.class)) {
 			if (cls.getDoc() != null) {
 				Comment c = UMLFactory.eINSTANCE.createComment();
 				c.setBody(cls.getDoc());
 				Class target = createClass.apply(cls);
 				target.getOwnedComments().add(c);
 			}
 
 			for (ElementDeclaration element : cls.getElements()) {
 				if (element.getDoc() != null) {
 					Comment c = UMLFactory.eINSTANCE.createComment();
 					c.setBody(element.getDoc());
 					Property target = createProperty.apply(element);
 					target.getOwnedComments().add(c);
 				}
 			}
 		}
 	}
 
 	private EmfCreateFunction<ElementDeclaration, Property> createProperty = new EmfCreateFunction<ElementDeclaration, Property>(UMLPackage.eINSTANCE.getProperty()) {
 		public Property configure (ElementDeclaration source, Property target) {
 			target.setName(source.getName());
 			target.setLower(source.isOptional() ? 0 : 1);
 			target.setUpper(source.isMultiple() ? -1 : 1);
 
 			if (source.getRef()!=null) {
 				Class type = createClass.apply(source.getRef());
 				target.setType(type);
 			} else {
 				PrimitiveType type = null;
 				for (PrimitiveType t : JavaExtensions.getXsdPrimitiveTypes()) {
 					if (t.getName().equals(source.getType().getName())) {
 						type = t;
 						break;
 					}
 				}
 				if (type == null) {
 					throw new IllegalStateException ("Unknown primitive type "+source.getType().getName());
 				}
 
 				target.setType(type);
 			}
 
 			return target;
 		};
 	};
 
 	private EmfCreateFunction<ElementDeclaration, Association> createAssociation = new EmfCreateFunction<ElementDeclaration, Association>(UMLPackage.eINSTANCE.getAssociation()) {
 		@Override
 		protected Association configure(ElementDeclaration source, Association newInstance) {
 			ComplexType type = (ComplexType) source.eContainer();
 			newInstance.setName("A_<"+Strings.toFirstLower(type.getName())+">_<"+Strings.toFirstLower(source.getRef().getName())+">");
 			return newInstance;
 		}
 	};
 
 	/**
 	 * Creates an UML Class for a Datamodel Complex Type.
 	 */
 	private EmfCreateFunction<ComplexType, Class> createClass = new EmfCreateFunction<ComplexType, Class>(UMLPackage.eINSTANCE.getClass_()) {
 		@Override
 		public Class find(ComplexType source) {
 			return allClasses().get(source.getName());
 		};
 
 		public Class configure (ComplexType source, Class target) {
 			target.setName(source.getName());
 			if (source.getBaseType()!=null) {
 				Class baseType = createClass.apply(source.getBaseType());
 				Generalization general = UMLFactory.eINSTANCE.createGeneralization();
 				general.setGeneral(baseType);
 				target.getGeneralizations().add(general);
 			}
 
 			for (ElementDeclaration p : source.getElements()) {
 				Property property = createProperty.apply(p);
 
 				if (p.getRef()==null || isDataTypeWrapper(p.getRef())) {
 					target.getOwnedAttributes().add(property);
 				} else {
 					Association assoc = createAssociation.apply(p);
 					property.setAssociation(assoc);
 					assoc.getOwnedEnds().add(property);
 					property.setIsNavigable(true);
 
 					Property backRefProperty = UMLFactory.eINSTANCE.createProperty();
 					backRefProperty.setName(Strings.toFirstLower(target.getName()));
 					backRefProperty.setLower(1);
 					backRefProperty.setUpper(1);
 					backRefProperty.setAssociation(assoc);
 					backRefProperty.setIsNavigable(false);
 					backRefProperty.setType(target);
 					assoc.getOwnedEnds().add(backRefProperty);
 
 					modelTraverser.currentPackage().getPackagedElements().add(assoc);
 				}
 			}
 
 			return target;
 		};
 	};
 
 	/**
 	 * Types which only contain elements, whose type is a primitive type,
 	 * are considered as Datatype Wrapper. References to those types should
 	 * be just a Property without association in the target UML model.
 	 */
 	private boolean isDataTypeWrapper (ComplexType type) {
 		return Iterables.all(type.getElements(), new Predicate<ElementDeclaration>() {
 			public boolean apply(ElementDeclaration input) {
 				return input.getRef()==null;
 			}
 		});
 	}
 
 	private Map<String,org.eclipse.uml2.uml.Class> allClasses;
 	private Map<String,org.eclipse.uml2.uml.Class> allClasses () {
 		if (allClasses==null) {
 			allClasses = Maps.newTreeMap(Comparators.stringComparator);
 			for (Resource r : getResourceSet().getResources()) {
 				if (!r.getURI().lastSegment().equals(getTargetUri().lastSegment()) && r.getURI().lastSegment().endsWith(UMLResource.FILE_EXTENSION) && !r.getContents().isEmpty()) {
 					for (org.eclipse.uml2.uml.Class cls : EcoreUtil2.eAllOfType(r.getContents().get(0), Class.class)) {
 						allClasses.put(cls.getName(), cls);
 					}
 				}
 			}
 		}
 		return allClasses;
 	}
 	@Override
 	protected int getAmountOfWork(org.edna.datamodel.datamodel.Model sourceModel) {
 		final int numberOfClasses = EcoreUtil2.eAllOfType(sourceModel, ComplexType.class).size();
 		// +1: transformDocumentation
 		return numberOfClasses+1;
 	}
 
 	@Override
 	public Resource loadSourceModel() {
 		final Resource sourceResource = super.loadSourceModel();
 		final Iterable<Resource> resources = Iterables.filter(sourceResource.getResourceSet().getResources(), new Predicate<Resource>() {
 			public boolean apply(Resource input) {
 				return !input.getURI().lastSegment().equals(sourceResource.getURI().lastSegment()) && input.getURI().lastSegment().endsWith("edna_datamodel");
 			}
 		});
 
 		// For each .edna_datamodel resource in the resource set put the respective .uml model resource into the
 		// ResourceSet.
 		// The given URIs will be file URIs (from PathTraverser of Reader component). It is necessary to map
 		// them to platform resources, otherwise the referenced files will have absolute paths. The can only
 		// be relative if we use platform resource URIs.
 		for (Resource r : Lists.newArrayList(resources)) {
 			URI uriToLoad = URI.createURI(r.getURI().toString().replace(".edna_datamodel", ".uml"));
 			for (String projectName : EcorePlugin.getPlatformResourceMap().keySet()) {
 				URI projectUri = EcorePlugin.getPlatformResourceMap().get(projectName);
 				// Is the .uml resource relative to the given project?
				if (uriToLoad.toString().startsWith(projectUri.toString())) {
					// convert to platform resource
					uriToLoad = URI.createPlatformResourceURI("/"+projectName+uriToLoad.toString().substring(uriToLoad.toString().indexOf(projectName)+projectName.length()),true);
 				}
 			}
 			sourceResource.getResourceSet().getResource(uriToLoad, true);
 		}
 		return sourceResource;
 	}
 }
