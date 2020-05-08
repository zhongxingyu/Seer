 package com.github.lbroudoux.roo.addon.taggable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.springframework.roo.classpath.TypeLocationService;
 import org.springframework.roo.classpath.TypeManagementService;
 import org.springframework.roo.classpath.details.MemberFindingUtils;
 import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
 import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
 import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
 import org.springframework.roo.model.JavaType;
 import org.springframework.roo.project.ProjectOperations;
 import org.springframework.roo.project.Dependency;
 import org.springframework.roo.project.DependencyScope;
 import org.springframework.roo.project.DependencyType;
 import org.springframework.roo.project.Repository;
 import org.springframework.roo.support.util.Assert;
 import org.springframework.roo.support.util.XmlUtils;
 import org.w3c.dom.Element;
 
 /**
  * Implementation of operations this add-on offers.
  *
  * @since 1.1
  */
 @Component // Use these Apache Felix annotations to register your commands class in the Roo container
 @Service
 public class TaggableOperationsImpl implements TaggableOperations {
 	
 	/**
 	 * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
 	 */
 	@Reference private ProjectOperations projectOperations;
 
 	/**
 	 * Use TypeLocationService to find types which are annotated with a given annotation in the project
 	 */
 	@Reference private TypeLocationService typeLocationService;
 	
 	/**
 	 * Use TypeManagementService to change types
 	 */
 	@Reference private TypeManagementService typeManagementService;
 
 	/** {@inheritDoc} */
 	public boolean isCommandAvailable() {
 		// Check if a project has been created
 		return projectOperations.isFocusedProjectAvailable();
 	}
 
 	/** {@inheritDoc} */
 	public void annotateType(JavaType javaType) {
 		// Use Roo's Assert type for null checks
 		Assert.notNull(javaType, "Java type required");
 
 		// Obtain ClassOrInterfaceTypeDetails for this java type
 		ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);
 
 		// Test if the annotation already exists on the target type
 		if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), new JavaType(RooTaggable.class.getName())) == null) {
 			ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
 			
 			// Create JavaType instance for the add-ons trigger annotation
 			JavaType rooRooTaggable = new JavaType(RooTaggable.class.getName());
 
 			// Create Annotation metadata
 			AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(rooRooTaggable);
 			
 			// Add annotation to target type
 			classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
 			
 			// Save changes to disk
 			typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
 		}
 	}
 
 	/** {@inheritDoc} */
 	public void annotateAll() {
 		// Use the TypeLocationService to scan project for all types with a specific annotation
 		for (JavaType type: typeLocationService.findTypesWithAnnotation(new JavaType("org.springframework.roo.addon.javabean.RooJavaBean"))) {
 			annotateType(type);
 		}
 	}
 	
 	/** {@inheritDoc} */
 	public void setup() {
 	   List<Dependency> dependencies = new ArrayList<Dependency>();
 	   
 		// Install the add-on Google code repository needed to get the annotation 
 		projectOperations.addRepository("", new Repository("Taggable Roo add-on repository", "Taggable Roo add-on repository", "http://raw.github.com/lbroudoux/spring-roo-addon-taggable/master/repo"));
 		
 		// Install the dependency on the add-on jar (
		dependencies.add(new Dependency("com.github.lbroudoux.roo.addon", "com.github.lbroudoux.roo.addon.taggable", "0.1.1.BUILD", DependencyType.JAR, DependencyScope.PROVIDED));
 		
 		// Install dependencies defined in external XML file
 		for (Element dependencyElement : XmlUtils.findElements("/configuration/batch/dependencies/dependency", XmlUtils.getConfiguration(getClass()))) {
 			dependencies.add(new Dependency(dependencyElement));
 		}
 
 		// Add all new dependencies to pom.xml
 		projectOperations.addDependencies("", dependencies);
 	}
 }
