 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package fr.imag.adele.apam.apammavenplugin;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.felix.ipojo.xml.parser.SchemaResolver;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.declarations.AtomicImplementationDeclaration;
 import fr.imag.adele.apam.declarations.ComponentDeclaration;
 import fr.imag.adele.apam.declarations.CompositeDeclaration;
 import fr.imag.adele.apam.declarations.RelationDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationDeclaration;
 import fr.imag.adele.apam.declarations.InstanceDeclaration;
 import fr.imag.adele.apam.declarations.InterfaceReference;
 import fr.imag.adele.apam.declarations.MessageReference;
 import fr.imag.adele.apam.declarations.PropertyDefinition;
 import fr.imag.adele.apam.declarations.ResolvableReference;
 import fr.imag.adele.apam.declarations.SpecificationDeclaration;
 import fr.imag.adele.apam.declarations.SpecificationReference;
 import fr.imag.adele.apam.declarations.UndefinedReference;
 
 public class ApamRepoBuilder {
 
 	/**
 	 * Metadata (in internal format).
 	 */
 
 	private static Logger logger = LoggerFactory
 			.getLogger(ApamRepoBuilder.class);
 
 	private final Set<SpecificationReference> bundleRequiresSpecifications = new HashSet<SpecificationReference>();
 
 	private static List<ComponentDeclaration> components;
 
 	private static final String BEGIN_P = "      <p n='";
 	private static final String END_P = "' />\n";
 	private static final String END_M = "' >\n";
 	private static final String ATT_V = "' v='";
 
 	/**
 	 * Flag describing if we need or not use local XSD files (i.e. use the
 	 * {@link SchemaResolver} or not). If <code>true</code> the local XSD are
 	 * not used.
 	 */
 
 	public ApamRepoBuilder(List<ComponentDeclaration> components,
 			List<ComponentDeclaration> dependencies) {
 		ApamRepoBuilder.components = components;
 		ApamCapability.init(components, dependencies);
 	}
 
 	public StringBuffer writeOBRFile() {
 
 		// if a component is defined twice, or error is name space, remove the
 		// second definition
 		checkDoubleDefinition();
 
 		StringBuffer obrContent = new StringBuffer("<obr> \n");
 
 		// print maven groupId, artifactId and version as resource capability
 		printOBRMavenElement(obrContent);
 
 		for (ComponentDeclaration comp : components) {
 			if (comp instanceof SpecificationDeclaration) {
 				printOBRElement(obrContent, comp);
 			}
 		}
 		for (ComponentDeclaration comp : components) {
 			if (comp instanceof AtomicImplementationDeclaration) {
 				printOBRElement(obrContent, comp);
 			}
 		}
 		for (ComponentDeclaration comp : components) {
 			if (comp instanceof CompositeDeclaration) {
 				printOBRElement(obrContent, comp);
 			}
 		}
 		for (ComponentDeclaration comp : components) {
 			if (comp instanceof InstanceDeclaration) {
 				printOBRElement(obrContent, comp);
 			}
 		}
 
 		generateFilters(obrContent);
 
 		obrContent.append("</obr> \n");
 		return obrContent;
 	}
 
 	private void printOBRMavenElement(StringBuffer obrContent) {
 		obrContent.append("   <capability name='" + CST.MAVEN + END_M);
 		obrContent.append(BEGIN_P + CST.GROUP_ID + ATT_V
 				+ OBRGeneratorMojo.currentProjectGroupId + END_P);
 		obrContent.append(BEGIN_P + CST.ARTIFACT_ID + ATT_V
 				+ OBRGeneratorMojo.currentProjectArtifactId + END_P);
 		obrContent.append(BEGIN_P + CST.VERSION + ATT_V
 				+ OBRGeneratorMojo.currentProjectVersion + END_P);
 		obrContent.append("   </capability>\n");
 	}
 
 	private void printOBRElement(StringBuffer obrContent,
 			ComponentDeclaration component) {
 		String type = "undefine";
 		if (component instanceof ImplementationDeclaration) {
 			type = "implementation ";
 		}
 		if (component instanceof CompositeDeclaration) {
 			type = "composite ";
 		}
 		if (component instanceof InstanceDeclaration) {
 			type = "instance ";
 		}
 		if (component instanceof SpecificationDeclaration) {
 			type = "specification ";
 		}
 		logger.info("Checking " + type + component.getName() + " ...");
 
 		// headers
 		obrContent.append("   <capability name='" + CST.CAPABILITY_COMPONENT
 				+ END_M);
 		generateProperty(obrContent, component, CST.NAME, component.getName());
 
 		if (component instanceof ImplementationDeclaration) {
 			generateProperty(obrContent, component, CST.COMPONENT_TYPE,
 					CST.IMPLEMENTATION);
 			generateProperty(obrContent, component, CST.IMPLNAME,
 					component.getName());
 		}
 
 		if (component instanceof InstanceDeclaration) {
 			generateProperty(obrContent, component, CST.COMPONENT_TYPE,
 					CST.INSTANCE);
 			generateProperty(obrContent, component, CST.INSTNAME,
 					component.getName());
 		}
 
 		if (component instanceof CompositeDeclaration) {
 			CompositeDeclaration composite = (CompositeDeclaration) component;
 			generateProperty(obrContent, component, CST.APAM_COMPOSITE,
 					CST.V_TRUE);
 			if (composite.getMainComponent() != null) {
 				generateProperty(obrContent, component,
 						CST.APAM_MAIN_COMPONENT, composite.getMainComponent()
 								.getName());
 			}
 			CheckObr.checkCompoMain((CompositeDeclaration) component);
 			// check the information for composite : grant, start, own,
 			// visibility etc.
 			CheckObr.checkCompositeContent((CompositeDeclaration) component);
 		}
 
 		if (component instanceof SpecificationDeclaration) {
 			generateProperty(obrContent, component, CST.COMPONENT_TYPE,
 					CST.SPECIFICATION);
 			generateProperty(obrContent, component, CST.SPECNAME,
 					component.getName());
 		}
 
 		// checks shared, singleton, instantiable, exclusive,
 		CheckObr.checkComponentHeader(component);
 
 		// provide clause
 		printProvided(obrContent, component);
 
 		// properties
 		printProperties(obrContent, component);
 
 		// Require, fields and constraints
 		printRequire(obrContent, component);
 
 		generateTypedProperty(obrContent, component, "version", "version",
 				OBRGeneratorMojo.thisBundleVersion);
 		ApamCapability.get(component.getReference()).freeze();
 		obrContent.append("   </capability>\n");
 	}
 
 	private void printProvided(StringBuffer obrContent,
 			ComponentDeclaration component) {
		if (component instanceof InstanceDeclaration)
 			return;
 		Set<UndefinedReference> undefinedMessages = new HashSet<UndefinedReference>();
 		Set<UndefinedReference> undefinedInterfaces = new HashSet<UndefinedReference>();
 		Set<UndefinedReference> undefinedReferences = component
 				.getProvidedResources(UndefinedReference.class);
 
 		for (UndefinedReference undefinedReference : undefinedReferences) {
 			if (undefinedReference.isKind(MessageReference.class)) {
 				undefinedMessages.add(undefinedReference);
 			} else if (undefinedReference.isKind(InterfaceReference.class)) {
 				undefinedInterfaces.add(undefinedReference);
 			}
 		}
 
 		Set<InterfaceReference> interfaces = component
 				.getProvidedResources(InterfaceReference.class);
 		for (InterfaceReference ref : interfaces) {
 			CheckObr.checkInterfaceExist(ref.getName());
 		}
 		String val = setReference2String(interfaces);
 		if (val != null) {
 			generateProperty(obrContent, component, CST.PROVIDE_INTERFACES,
 					setReference2String(interfaces));
 		}
 
 		Set<MessageReference> messages = component
 				.getProvidedResources(MessageReference.class);
 		for (MessageReference ref : messages) {
 			CheckObr.checkInterfaceExist(ref.getName());
 		}
 		val = setReference2String(messages);
 		if (val != null) {
 			generateProperty(obrContent, component, CST.PROVIDE_MESSAGES, val);
 		}

 		if (component instanceof ImplementationDeclaration) {
 			SpecificationReference spec = ((ImplementationDeclaration) component)
 					.getSpecification();
 			if ((spec != null) && !spec.getName().isEmpty()) {
 				generateProperty(obrContent, component,
 						CST.PROVIDE_SPECIFICATION, spec.getName());
 				bundleRequiresSpecifications.add(spec);
 				CheckObr.checkImplProvide(component, spec.getName(),
 						interfaces, messages, undefinedInterfaces,
 						undefinedMessages);
			}
 		}
 	}
 
 	private void printProperties(StringBuffer obrContent,
 			ComponentDeclaration component) {
 		Map<String, Object> properties = CheckObr.getValidProperties(component);
 		for (String attr : properties.keySet()) {
 			generateProperty(obrContent, component, attr, properties.get(attr)
 					.toString());
 		}
 
 		// definition attributes
 		List<PropertyDefinition> definitions = component
 				.getPropertyDefinitions();
 		for (PropertyDefinition definition : definitions) {
 			String type = definition.getType();
 			String attrDef = definition.getName();
 			String defaultValue = definition.getDefaultValue();
 
 			if (CheckObr.checkProperty(component, attrDef, type, defaultValue)) {
 				generateTypedProperty(obrContent, component,
 						CST.DEFINITION_PREFIX + attrDef, type, defaultValue);
 			}
 		}
 	}
 
 	/**
 	 * provided a set of resources references (interface or messages)
 	 * fr.mag....A , B, C references produces a string "[;fr.imag....A;B;C;]"
 	 * 
 	 * @param refs
 	 * @return
 	 */
 
 	private String setReference2String(Set<? extends ResolvableReference> refs) {
 		if (refs.isEmpty()) {
 			return null;
 		}
 		String val = "";
 		for (ResolvableReference mess : refs) {
 			val += mess.getName() + ",";
 		}
 		// remove last ","
 		return val.substring(0, val.length() - 1);
 	}
 
 	private void printRequire(StringBuffer obrContent,
 			ComponentDeclaration component) {
 		// We do not generate dependencies for specification to remain lazy
 		// the spec version is mentionned in the implementations that implement
 		// that spec.
 		if (component instanceof ImplementationDeclaration) {
 			for (RelationDeclaration dep : component.getDependencies()) {
 				if (dep.getTarget().as(SpecificationReference.class) != null) {
 					bundleRequiresSpecifications.add(dep.getTarget().as(
 							SpecificationReference.class));
 				}
 			}
 		}
 
 		// all components : checks dependencies and constraints
 		CheckObr.checkRelations(component);
 	}
 
 	private void generateFilters(StringBuffer obrContent) {
 		/**
 		 * Generate filters for all provided specifications
 		 */
 		// VersionRange version ;
 		for (SpecificationReference res : bundleRequiresSpecifications) {
 			generateRequire(obrContent, res.getName(),
 					getVersionExpression(res.getName()));
 		}
 	}
 
 	private void generateProperty(StringBuffer obrContent,
 			ComponentDeclaration component, String attr, String value) {
 		if (ApamCapability.get(component.getReference()).putAttr(attr, value)) {
 			obrContent.append(BEGIN_P + attr + ATT_V + value + END_P);
 			return;
 		}
 		// CheckObr.error ("Property " + attr + " already defined for  " +
 		// component.getName()) ;
 	}
 
 	private void generateTypedProperty(StringBuffer obrContent,
 			ComponentDeclaration component, String attr, String type,
 			String value) {
 		if (value == null) {
 			value = "";
 		}
 
 		if (ApamCapability.get(component.getReference()).putAttr(attr, value)) {
 			obrContent.append(BEGIN_P + attr + "' t='" + type + ATT_V + value
 					+ END_P);
 			return;
 		}
 		// CheckObr.error ("Property " + attr + " already defined for  " +
 		// component.getName()) ;
 	}
 
 	private void generateRequire(StringBuffer obrContent, String target,
 			String version) {
 		if (version == null) {
 			obrContent
 					.append("   <require name='apam-component' filter='(name="
 							+ target
 							+ ")' extend='false' multiple='false' optional='false'>"
 							+ " specification relation toward " + target
 							+ "</require>\n");
 		} else {
 			obrContent
 					.append("   <require name='apam-component' filter='(&amp;(name="
 							+ target
 							+ ")"
 							+ version
 							+ ")' extend='false' multiple='false' optional='false'>"
 							+ " specification relation toward "
 							+ target
 							+ "</require>\n");
 		}
 	}
 
 	private String getVersionExpression(String name) {
 		VersionRange range = getVersionRange(name);
 		if (range == null) {
 			return null;
 		}
 		String version;
 		if (range.toString().indexOf('-') != -1) {
 			version = range.toString().substring(0,
 					range.toString().indexOf('-'));
 		} else {
 			version = range.toString().replace('-', '.');
 		}
 		return "(version&gt;=" + version + ")";
 	}
 
 	private VersionRange getVersionRange(String name) {
 		if (OBRGeneratorMojo.versionRange.containsKey(name)) {
 			return OBRGeneratorMojo.versionRange.get(name);
 		}
 		return null;
 	}
 
 	private void checkDoubleDefinition() {
 		Set<String> defined = new HashSet<String>();
 		List<ComponentDeclaration> dcl = new ArrayList<ComponentDeclaration>(
 				components);
 
 		/*
 		 * Verify in the current project
 		 */
 		for (ComponentDeclaration comp : dcl) {
 			if (defined.contains(comp.getName())) {
 				CheckObr.error("In " + comp + ": component " + comp.getName()
 						+ " already defined");
 				components.remove(comp);
 			} else {
 				defined.add(comp.getName());
 			}
 		}
 
 		/*
 		 * Verify if the components we are building were not in already
 		 * processed in another project in the same built
 		 * 
 		 * NOTE this causes problems because a lot of information is kept in
 		 * static variables that are shared in the same build execution.
 		 */
 		for (ComponentDeclaration comp : new ArrayList<ComponentDeclaration>(
 				components)) {
 
 			ApamCapability existingDefinition = ApamCapability.get(comp
 					.getReference());
 
 			/*
 			 * never built is OK to process
 			 */
 			if (existingDefinition == null) {
 				continue;
 			}
 
 			/*
 			 * already built in the repository is OK to process
 			 */
 			if (!existingDefinition.isFinalized()) {
 				continue;
 			}
 			/*
 			 * Already processed in this build execution
 			 */
 			CheckObr.error("Component " + comp.getName()
 					+ " is already defined in another project in this build");
 			components.remove(comp);
 		}
 
 	}
 }
