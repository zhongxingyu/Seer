 package com.imminentmeals.prestige.codegen;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Context;
 
 import com.google.common.base.CaseFormat;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Maps.EntryTransformer;
 import com.google.common.io.Closeables;
 import com.imminentmeals.prestige.ControllerContract;
 import com.imminentmeals.prestige.SegueController;
 import com.imminentmeals.prestige.annotations.Controller;
 import com.imminentmeals.prestige.annotations.Controller.Default;
 import com.imminentmeals.prestige.annotations.ControllerImplementation;
 import com.imminentmeals.prestige.annotations.InjectDataSource;
 import com.imminentmeals.prestige.annotations.InjectModel;
 import com.imminentmeals.prestige.annotations.InjectPresentation;
 import com.imminentmeals.prestige.annotations.InjectPresentationFragment;
 import com.imminentmeals.prestige.annotations.Model;
 import com.imminentmeals.prestige.annotations.ModelImplementation;
 import com.imminentmeals.prestige.annotations.Presentation;
 import com.imminentmeals.prestige.annotations.Presentation.NoProtocol;
 import com.imminentmeals.prestige.annotations.PresentationFragment;
 import com.imminentmeals.prestige.annotations.PresentationFragmentImplementation;
 import com.imminentmeals.prestige.annotations.PresentationImplementation;
 import com.imminentmeals.prestige.annotations.meta.Implementations;
 import com.squareup.javawriter.JavaWriter;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.CheckForNull;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.Syntax;
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.MirroredTypeException;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.util.Elements;
 import javax.lang.model.util.Types;
 import javax.tools.JavaFileObject;
 
 import dagger.Lazy;
 import dagger.Module;
 import dagger.Provides;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 import static com.google.common.collect.Maps.transformEntries;
 import static com.google.common.collect.Sets.newHashSet;
 import static com.imminentmeals.prestige.annotations.meta.Implementations.PRODUCTION;
 import static java.lang.Math.min;
 import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
 import static javax.lang.model.element.ElementKind.FIELD;
 import static javax.lang.model.element.ElementKind.INTERFACE;
 import static javax.lang.model.element.Modifier.FINAL;
 import static javax.lang.model.element.Modifier.PRIVATE;
 import static javax.lang.model.element.Modifier.PUBLIC;
 import static javax.lang.model.element.Modifier.STATIC;
 import static javax.tools.Diagnostic.Kind.ERROR;
 
 // TODO: can you mandate that a default implementation is provided for everything that has any implementation?
 // TODO: public static Strings for generated methods and classes to use in Prestige helper methods
 @SupportedAnnotationTypes({ "com.imminentmeals.prestige.annotations.Presentation",
 	                        "com.imminentmeals.prestige.annotations.PresentationImplementation",
 	                        "com.imminentmeals.prestige.annotations.InjectDataSource",
                             "com.imminentmeals.prestige.annotations.Controller",
                             "com.imminentmeals.prestige.annotations.ControllerImplementation",
                             "com.imminentmeals.prestige.annotations.Model",
                             "com.imminentmeals.prestige.annotations.ModelImplementation",
                             "com.imminentmeals.prestige.annotations.PresentationFragment",
                             "com.imminentmeals.prestige.annotations.PresentationFragmentImplementation",
                             "com.imminentmeals.prestige.annotations.InjectPresentationFragment",
                             "com.imminentmeals.prestige.annotations.InjectPresentation" })
 public class AnnotationProcessor extends AbstractProcessor {	
 	public static final String DATA_SOURCE_INJECTOR_SUFFIX = "$$DataSourceInjector";
 	public static final String CONTROLLER_MODULE_SUFFIX = "ControllerModule";
 	public static final String MODEL_INJECTOR_SUFFIX = "$$ModelInjector";
 	public static final String MODEL_MODULE_SUFFIX = "ModelModule";
 	public static final String PRESENTATION_FRAGMENT_INJECTOR_SUFFIX = "$$PresentationFragmentInjector";
     public static final String PRESENTATION_INJECTOR_SUFFIX = "$$PresentationInjector";
 
     @Override
 	public SourceVersion getSupportedSourceVersion() {
 		return SourceVersion.latestSupported();
 	}
 
 	/**
 	 * <p>Processes the source code.</p>
 	 * @param _ Unused
 	 * @param environment The round environment
 	 * @return {@code true} indicating that the processed annotations have been completely handled
 	 */
 	@Override
 	public boolean process(Set<? extends TypeElement> _, RoundEnvironment environment) {
 		// Makes sure to only process once
 		if (_passes++ > 0)
 			return true;
 		
 		// Grabs the annotation processing utilities
 		_element_utilities = processingEnv.getElementUtils();
 		_type_utilities = processingEnv.getTypeUtils();
 		
 		// Initializes the data model subcomponents that will be used when generating the code
 		final List<PresentationControllerBinding> presentation_controller_bindings = newArrayList();
 		final Map<String, List<ControllerData>> controllers = newHashMap();
 		final List<DataSourceInjectionData> data_source_injections = newArrayList();
 		final Map<String, List<ModelData>> models = newHashMap();
 		final List<ModelData> model_interfaces = newArrayList();
 		final Map<Element, List<ModelInjectionData>> model_injections = newHashMap();
 		final Map<Element, List<PresentationFragmentInjectionData>> presentation_fragment_injections = newHashMap();
 		final Map<Element, List<PresentationFragmentInjectionData>> controller_presentation_fragment_injections = newHashMap();
         final Map<Element, PresentationInjectionData> controller_presentation_injections = newHashMap();
 		
 		// Processes the @PresentationFragment and @PresentationFragmentImplementation annotations
 		final ImmutableMap<Element, PresentationFragmentData> presentation_fragments = processPresentationFragments(environment);
 		
 		// Processes the @Presentation and @PresentationImplementation annotations
 		final ImmutableMap<Element, PresentationData> presentations = processPresentations(environment, presentation_fragments);
 		
 		// Processes the @InjectDataSource annotations
 		processDataSourceInjections(environment, data_source_injections, presentations, presentation_fragments);
 		
 		// Processes the @InjectPresentationFragment annotations
 		processPresentationFragmentInjections(environment, presentation_fragment_injections, 
 				                              controller_presentation_fragment_injections, presentation_fragments);
 
         // Processes the @InjectPresentation annotations
         processPresentationInjections(environment, controller_presentation_injections);
 		
 		// Processes the @Controller annotations and @ControllerImplementation annotations per @Controller annotation
 		processControllers(environment, presentation_controller_bindings, controllers, presentations);
 		
 		// Processes the @Model annotations
 		processModels(environment, models, model_interfaces);
 		
 		// Processes the @InjectModel annotations
 		processModelInjections(environment, model_injections, model_interfaces);
 		
 		// Reformats the gathered information to be used in data models
 		final ImmutableList.Builder<ModuleData> controller_modules = ImmutableList.<ModuleData>builder();
 		for (Map.Entry<String, List<ControllerData>> controller_implementations : controllers.entrySet()) {
 			final Element implementation = controller_implementations.getValue().get(0)._implementation;
 			final String package_name = _element_utilities.getPackageOf(implementation).getQualifiedName() + "";
 			final String class_name = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, 
 					controller_implementations.getKey()) + CONTROLLER_MODULE_SUFFIX;
 			controller_modules.add(new ModuleData(String.format("%s.%s", package_name, class_name), 
 					                              controller_implementations.getKey(),class_name, package_name,
 					                              controller_implementations.getValue()));
 		}
 		final ImmutableList.Builder<ModuleData> model_modules = ImmutableList.<ModuleData>builder();
 		for (Map.Entry<String, List<ModelData>> model_implementations : models.entrySet()) {
 			final Element implementation = model_implementations.getValue().get(0)._implementation;
 			final String package_name = _element_utilities.getPackageOf(implementation).getQualifiedName() + "";
 			final String class_name = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, 
 					model_implementations.getKey()) + MODEL_MODULE_SUFFIX;
 			model_modules.add(new ModuleData(String.format("%s.%s", package_name, class_name), 
 					                              model_implementations.getKey(),class_name, package_name,
 					                              model_implementations.getValue()));
 		}
 		final Map<Element, Map<Integer, List<PresentationFragmentInjectionData>>> presentation_fragment_display_injections =
 			Maps.transformValues(presentation_fragment_injections, 
 				new Function<List<PresentationFragmentInjectionData>, Map<Integer, List<PresentationFragmentInjectionData>>>() {
 
 					@Nullable public Map<Integer, List<PresentationFragmentInjectionData>> apply(
 							@Nullable List<PresentationFragmentInjectionData> presentation_fragments) {
 						final Map<Integer, List<PresentationFragmentInjectionData>> injections = newHashMap();
 						for (PresentationFragmentInjectionData presentation_fragment : presentation_fragments) {
 							if (presentation_fragment._is_manually_created)
 								// Skips the current injection, since it will be handled manually
 								continue;
 							for (Entry<Integer, Integer> injection : presentation_fragment._displays.entrySet())
 								if (injections.containsKey(injection.getKey()))
 									injections.get(injection.getKey()).add(presentation_fragment);
 								else
 									injections.put(injection.getKey(), newArrayList(presentation_fragment));
 						}
 						return ImmutableMap.copyOf(injections);
 					}
 				});
 		
 		// Generates the code
 		generateSourceCode(presentation_controller_bindings, controller_modules.build(), data_source_injections,
 				           model_modules.build(), model_interfaces, model_injections, 
 				           ImmutableMap.copyOf(presentation_fragment_display_injections),
 				           ImmutableMap.copyOf(controller_presentation_fragment_injections), controller_presentation_injections);
 	     
 		// Releases the annotation processing utilities
 		_element_utilities = null;
 		_type_utilities = null;
 		
 		return true;
 	}
 	
 	/**
 	 * <p>Processes the source code for the @PresentationFragment and @PresentationFragmentImplementation annotations.</p>
 	 * @param environment The round environment
 	 * @return
 	 */
 	private ImmutableMap<Element, PresentationFragmentData> processPresentationFragments(RoundEnvironment environment) {
 		final TypeMirror fragment_type = _element_utilities.getTypeElement(Fragment.class.getCanonicalName()).asType();
 		final Map<Element, Element> presentation_fragment_protocols = newHashMap();
 		final Map<Element, Element> presentation_fragment_implementations = newHashMap();
 		final Map<Element, Set<Element>> unverified_presentation_fragments = newHashMap();
 		final TypeMirror no_protocol = _element_utilities.getTypeElement(PresentationFragment.NoProtocol.class.getCanonicalName()).asType();
 		
 		for (Element element : environment.getElementsAnnotatedWith(PresentationFragment.class)) {
 			System.out.println("@PresentationFragment is " + element);
 			
 			// Verifies that the target type is an interface
 			if (element.getKind() != INTERFACE) {
 				error(element, "@PresentationFragment annotation may only be specified on interfaces (%s).", element);
 				// Skips the current element
 				continue;
 			}
 			
 			// Verifies that the interface's visibility is public
 			if (!element.getModifiers().contains(PUBLIC)) {
 				error(element, "@PresentationFragment interfaces must be public (%s).", element);
 				// Skips the current element
 				continue;
 			}
 			
 			// Gathers @PresentationFragment information
 			final PresentationFragment presentation_fragment_annotation = element.getAnnotation(PresentationFragment.class);
 			Element protocol = null;
 			try {
 				presentation_fragment_annotation.protocol();
 			} catch (MirroredTypeException exception) {
 				protocol = _type_utilities.asElement(exception.getTypeMirror());
 			}
 			
 			System.out.println("\twith Protocol: " + protocol);
 			
 			// Verifies that the Protocol is an Interface
 			if (protocol.getKind() != INTERFACE) {
 				error(element, "@PresentationFragment Protocol must be an interface (%s).",
 					  protocol);
 				// Skips the current element
 				continue;
 			}
 			
 			// Verifies that the Protocol visibility is public
 			if (!protocol.getModifiers().contains(PUBLIC)) {
 				error(element, "@PresentationFragment Protocol must be public (%s).",
 						protocol);
 				// Skips the current element
 				continue;
 			}
 			
 			protocol = _type_utilities.isSameType(protocol.asType(), no_protocol)? null : protocol;
 			
 			// Verifies previously unverified Presentation Fragments that use this Presentation Fragment
 			// Notice that these were deferred until this Presentation Fragment was processed
 			if (unverified_presentation_fragments.containsKey(element))
 				for (Element presentation_fragment : unverified_presentation_fragments.get(element)) {
 					final TypeMirror super_protocol = presentation_fragment_protocols.get(presentation_fragment).asType();
 					if (!_type_utilities.isSubtype(super_protocol, protocol.asType())) {
 						error(presentation_fragment, 
 							  "@PresentationFragment Protocol must extend %s from Presentation Fragment %s (%s).",
 							  protocol, element, presentation_fragment);
 						// Skips the current element
 						continue;
 					} else {
 						unverified_presentation_fragments.get(element).remove(presentation_fragment);
 						if (unverified_presentation_fragments.get(element).isEmpty())
 							unverified_presentation_fragments.remove(element);
 					}
 				}
 			
 			// Adds the mapping of the Presentation to its Protocol (null if no Protocol is defined)
 			presentation_fragment_protocols.put(element, protocol);
 			
 			// Now that the @PresentationFragment annotation has been verified and its data extracted find its implementations				
 			// TODO: very inefficient
 			for (Element implementation_element : environment.getElementsAnnotatedWith(PresentationFragmentImplementation.class)) {
 				// Makes sure to only deal with Presentation Fragment implementations for the current @PresentationFragment
 				if (!_type_utilities.isSubtype(implementation_element.asType(), element.asType()))
 					continue;
 				
 				System.out.println("\twith an implementation of " + implementation_element);
 				
 				// Verifies that the Presentation Fragment implementation extends from Fragment
 		        if (!_type_utilities.isSubtype(implementation_element.asType(), fragment_type)) {
 		          error(implementation_element, "@PresentationFragmentImplementation classes must extend from Fragment (%s).",
 		                implementation_element); 
 		          // Skips the current element
 		          continue;
 		        }
 		        
 		        // Finds Presentation Fragment injections and verifies that their Protocols are met
 		        for (Element enclosed_element : implementation_element.getEnclosedElements()) {
 		        	if (enclosed_element.getAnnotation(InjectPresentationFragment.class) != null && 
 		        		enclosed_element.getKind() == FIELD) {			
 		    			// Verifies that the Protocol extends all of the Presentation Fragment's Protocols
 		    			// Notice that it only checks against the Presentation Fragments that have already been processed
 		        		final Element sub_presentation_fragment = _type_utilities.asElement(enclosed_element.asType());
 		        		System.out.println("\tcontains Presentation Fragment: " + sub_presentation_fragment);
 	    				if (presentation_fragment_protocols.get(sub_presentation_fragment) != null) {
 	    					final TypeMirror sub_protocol = 
 	    							presentation_fragment_protocols.get(sub_presentation_fragment).asType();
 	    					if (!_type_utilities.isSubtype(protocol.asType(), sub_protocol)) {
 	    						error(implementation_element, 
 	    							  "@PresentationFragment Protocol must extend %s from Presentation Fragment %s (%s).",
 	    							  sub_protocol, sub_presentation_fragment, implementation_element);
 	    						// Skips the current element
 	    						continue;
 	    					} 
 	    				} else if (unverified_presentation_fragments.containsKey(enclosed_element))
 	    					unverified_presentation_fragments.get(enclosed_element).add(element);
 	    				else
 	    					unverified_presentation_fragments.put(enclosed_element, newHashSet(element));
 		        	}
 		        }
 		        
 		        // Adds the implementation to the list of imports
 				presentation_fragment_implementations.put(element, implementation_element);
 			}
 		}
 		
 		// Verifies that all PresentationFragments have been verified
 		final String format = "@PresentationFragment Protocol must extend %s from Presentation Fragment %s (%s).";
 		for (Entry<Element, Set<Element>> entry : unverified_presentation_fragments.entrySet()) {
 			final Element protocol = presentation_fragment_protocols.get(entry.getKey());
 			for (Element element : entry.getValue())
 				error(element, format, protocol, entry.getKey(), element);
 		}
 		
 		System.out.println("Finished processing Presentations Fragment.");
 		return ImmutableMap.copyOf(transformEntries(presentation_fragment_protocols, 
 				new EntryTransformer<Element, Element, PresentationFragmentData>() {
 
 					public PresentationFragmentData transformEntry(@Nonnull Element key, @Nullable Element protocol) {
 						return new PresentationFragmentData(protocol, presentation_fragment_implementations.get(key));
 					}
 			
 		}));
 	}
 
 	/**
 	 * <p>Processes the source code for the @Presentation and @PresentationImplementation annotations.</p>
 	 * @param environment The round environment
 	 */
 	private ImmutableMap<Element, PresentationData> processPresentations(RoundEnvironment environment, 
 			Map<Element, PresentationFragmentData> presentation_fragments) {
 		final TypeMirror activity_type = _element_utilities.getTypeElement(Activity.class.getCanonicalName()).asType();
 		final Map<Element, Element> presentation_protocols = newHashMap();
 		final Map<Element, Element> presentation_implementations = newHashMap();
 					
 		for (Element element : environment.getElementsAnnotatedWith(Presentation.class)) {
 			System.out.println("@Presentation is " + element);
 			
 			// Verifies that the target type is an interface
 			if (element.getKind() != INTERFACE) {
 				error(element, "@Presentation annotation may only be specified on interfaces (%s).",
 					  element);
 				// Skips the current element
 				continue;
 			}
 			
 			// Verifies that the interface's visibility is public
 			if (!element.getModifiers().contains(PUBLIC)) {
 				error(element, "@Presentation interface must be public (%s).",
 					  element);
 				// Skips the current element
 				continue;
 			}
 			
 			// Gathers @Presentation information
 			final Presentation presentation_annotation = element.getAnnotation(Presentation.class);
 			Element protocol = null;
 			try {
 				presentation_annotation.protocol();
 			} catch (MirroredTypeException exception) {
 				protocol = _type_utilities.asElement(exception.getTypeMirror());
 			}
 			
 			System.out.println("\twith Protocol: " + protocol);
 			
 			// Verifies that the Protocol is an Interface
 			if (protocol.getKind() != INTERFACE) {
 				error(element, "@Presentation Protocol must be an interface (%s).",
 					  protocol);
 				// Skips the current element
 				continue;
 			}
 			
 			// Verifies that the Protocol visibility is public
 			if (!protocol.getModifiers().contains(PUBLIC)) {
 				error(element, "@Presentation Protocol must be public (%s).",
 						protocol);
 				// Skips the current element
 				continue;
 			}
 			
 			// Adds the mapping of the Presentation to its Protocol (null if no Protocol is defined)
 			presentation_protocols.put(element, protocol);
 
 			// Now that the @Presentation annotation has been verified and its data extracted find its implementations				
 			// TODO: very inefficient
 			for (Element implementation_element : environment.getElementsAnnotatedWith(PresentationImplementation.class)) {
 				// Makes sure to only deal with Presentation implementations for the current @Presentation
 				if (!_type_utilities.isSubtype(implementation_element.asType(), element.asType()))
 					continue;
 				
 				System.out.println("\twith an implementation of " + implementation_element);
 				
 				// Verifies that the Presentation implementation extends from Activity
 		        if (!_type_utilities.isSubtype(implementation_element.asType(), activity_type)) {
 		          error(element, "@PresentationImplementation classes must extend from Activity (%s).",
 		                element); 
 		          // Skips the current element
 		          continue;
 		        }
 		        
 		        // Finds Presentation Fragment injections and verifies that their Protocols are met
 		        for (Element enclosed_element : implementation_element.getEnclosedElements()) {
 		        	if (enclosed_element.getAnnotation(InjectPresentationFragment.class) != null && 
 		        		enclosed_element.getKind() == FIELD) {		
 		        		final Element presentation_fragment = _type_utilities.asElement(enclosed_element.asType()); 
 		    			// Verifies that the Presentation Fragment is an @PresentationFragment
 		        		if (!presentation_fragments.containsKey(presentation_fragment)) {
 		        			error(implementation_element, 
 		        				  "@InjectPresentationFragment must be an @PresentationFragment %s (%s).",
 		        				  presentation_fragment, implementation_element);
 		        			continue;
 		        		}
 		        		
 		        		System.out.println("\tcontains Presentation Fragment: " + presentation_fragment);
 		        		// Retrieves the Presentation Fragment's Protocol
 		        		final Element presentation_fragment_protocol = 
 		        				presentation_fragments.get(presentation_fragment)._protocol;
 		        		
 		        		// Verifies that the Presentation Protocol extends the Presentation Fragment Protocol if one is required
 		        		if (presentation_fragment_protocol != null && 
 		        			!_type_utilities.isSubtype(protocol.asType(), presentation_fragment_protocol.asType())) {
     						error(implementation_element, 
     							  "@Presentation Protocol must extend %s from Presentation Fragment %s (%s).",
     							  presentation_fragment_protocol, presentation_fragment, implementation_element);
     						// Skips the current element
     						continue;
 	    				}
 		        	}
 		        }
 		        
 		        // Adds the implementation to the list of imports
 				presentation_implementations.put(element, implementation_element);
 			}
 		}
 		
 		System.out.println("Finished processing Presentations.");
 		// TODO: Should require there to exist a @PresentationImplementation for @Controller to work?? Good?
 		return ImmutableMap.copyOf(transformEntries(presentation_protocols, 
 				new EntryTransformer<Element, Element, PresentationData>() {
 
 					public PresentationData transformEntry(@Nonnull Element key, @Nullable Element protocol) {
 						return new PresentationData(protocol, presentation_implementations.get(key));
 					}
 			
 		}));
 	}
 	
 	private void processDataSourceInjections(RoundEnvironment environment, 
 			                                 List<DataSourceInjectionData> data_source_injections,
 			                                 ImmutableMap<Element, PresentationData> presentations,
 			                                 ImmutableMap<Element, PresentationFragmentData> presentation_fragments) {
 		final TypeMirror no_protocol = _element_utilities.getTypeElement(NoProtocol.class.getCanonicalName()).asType();
 
 		for (Element element : environment.getElementsAnnotatedWith(InjectDataSource.class)) {
 			final TypeElement enclosing_element = (TypeElement) element.getEnclosingElement();
 			System.out.println("@InjectDataSource is " + element);
 			System.out.println("\tin " + enclosing_element);
 			
 			// Verifies containing type is a Presentation Implementation
 	        if (enclosing_element.getAnnotation(PresentationImplementation.class) == null &&
 	        	enclosing_element.getAnnotation(PresentationFragmentImplementation.class) == null) {
 	          error(element, "@InjectDataSource annotations must be specified in @PresentationImplementation or " +
 	          		" @PresentationFragmentImplementation classes (%s).",
 	              enclosing_element);
 	          // Skips the current element
 	          continue;
 	        }
 			
 	        TypeMirror protocol = no_protocol;
 	        final boolean is_presentation = enclosing_element.getAnnotation(PresentationFragmentImplementation.class) == null;
 	        // Finds the corresponding Presentation Protocol if enclosing element is a Presentation
 	        if (is_presentation) {
 		        for (PresentationData data : presentations.values())
 		        	if (data._implementation != null && 
 		        	    _type_utilities.isSameType(data._implementation.asType(), enclosing_element.asType())) {
 		        		protocol = data._protocol.asType();
 		        		break;
 		        	}
 	        // Finds the corresponding Presentation Fragment Protocol if enclosing element is a Presentation Fragment
 	        } else {
         		for (PresentationFragmentData data : presentation_fragments.values())
 		        	if (data._implementation != null && 
 		        	    _type_utilities.isSameType(data._implementation.asType(), enclosing_element.asType())) {
 		        		protocol = data._protocol.asType();
 		        		break;
 		        	}
 	        }
 	        System.out.println("\tdefined Protocol is " + protocol);
 	        // Verifies that Presentation has a Protocol
 	        if (_type_utilities.isSameType(protocol, no_protocol)) {
 	        	error(element, "@InjectDataSource may only be used with " + 
 	                  (is_presentation? "Presentations" : "Presentation Fragments") + 
 	        		  "that have a Protocol (%s).", enclosing_element);
 	        	// Skips the current element
 	        	continue;
 	        }
 	        // Verifies that the target type is the Presentation's Protocol
 	        if (!_type_utilities.isSameType(element.asType(), protocol)) {
 	          error(element, "@InjectDataSource fields must be the same as the " +
 	          		(is_presentation? "Presentation's" : "Presentation Fragment's") +
 	          		"Protocol (%s.%s).", enclosing_element.getQualifiedName(), element);
 	          // Skips the current element
 	          continue;
 	        }
 	        	        
 	        // Verify field properties.
 	        Set<Modifier> modifiers = element.getModifiers();
 	        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
 	          error(element, "@InjectDataSource fields must not be private or static (%s.%s).",
 	              enclosing_element.getQualifiedName(), element);
 	          continue;
 	        }
 	        
 	        // Gathers the @InjectDataSource information
 	        final String package_name = _element_utilities.getPackageOf(enclosing_element) + "";
 			final String element_class = _element_utilities.getBinaryName((TypeElement) enclosing_element) + "";
 	        data_source_injections.add(new DataSourceInjectionData(package_name, enclosing_element, element, element_class));
 		}
 	}
 	
 	/**
 	 * <p>Processes the source code for the @Controller and @ControllerImplementation annotations.</p>
 	 * @param environment The round environment
 	 * @param presentation_controller_bindings Will hold the bindings between Presentation implementations and their 
 	 *        Controllers 
 	 * @param controllers Will hold the list of Controllers grouped under an implementation scope
 	 * @param presentations The map of Presentations -> {@link PresentationData}
 	 */
 	private void processControllers(RoundEnvironment environment,
 			                        List<PresentationControllerBinding> presentation_controller_bindings,
 			                        Map<String, List<ControllerData>> controllers, 
 			                        ImmutableMap<Element, PresentationData> presentations) {
 		final TypeMirror default_presentation = _element_utilities.getTypeElement(Default.class.getCanonicalName()).asType();
 		final TypeMirror no_protocol = _element_utilities.getTypeElement(NoProtocol.class.getCanonicalName()).asType();
 		
 		for (Element element : environment.getElementsAnnotatedWith(Controller.class)) {
 			System.out.println("@Controller is " + element);
 			
 			// Verifies that the target type is an interface
 			if (element.getKind() != INTERFACE) {
 				error(element, "@Controller annotation may only be specified on interfaces (%s).",
 					  element);
 				// Skips the current element
 				continue;
 			} 
 			
 			// Verifies that the interface's visibility is public
 			if (!element.getModifiers().contains(PUBLIC)) {
 				error(element, "@Controller interface must be public (%s).",
 					  element);
 				// Skips the current element
 				continue;
 			}
 			
 			// Gathers @Controller information
 			final Controller controller_annotation = element.getAnnotation(Controller.class);
 			Element presentation = null;
 			try {
 				controller_annotation.presentation();
 			} catch (MirroredTypeException exception) {
 				presentation = _type_utilities.asElement(exception.getTypeMirror());
 			}
 			
 			// Searches for a matching @Presentation if the Presentation is defined by naming convention
 			if (_type_utilities.isSameType(presentation.asType(), default_presentation)) {
 				final String presentation_from_controller_name = 
 						_CONTROLLER_TO_ROOT.reset(element.getSimpleName()+ "").replaceAll("$1Presentation");
 				for (Element presentation_interface : presentations.keySet()) 
 					if (presentation_interface.getSimpleName().contentEquals(presentation_from_controller_name)) {
 						presentation = presentation_interface;
 						break;
 					}
 			}
 			
 			System.out.println("\tfor Presentation: " + presentation);
 			
 			// Verifies that the Controller's Presentation is a Presentation
 			if (!presentations.containsKey(presentation)) {
 				error(element, "@Controller Presentation must be an @Presentation (%s).", element);
 				// Skips the current element
 				continue;
 			}
 			
 			// Verifies that the Controller implements the Presentation's Protocol, if one is required
 			final Element protocol = presentations.get(presentation)._protocol;
 			if (!(_type_utilities.isSubtype(protocol.asType(), no_protocol) || 
 				  _type_utilities.isSubtype(element.asType(), protocol.asType()))) {
 				error(element, "@Controller is required to implement Protocol %s by its Presentation (%s).",
 					  protocol, element);
 				// Skips the current element
 				continue;
 			}
 			
 			// Adds Presentation Controller binding if Controller has a Presentation
 			if (!_type_utilities.isSameType(presentation.asType(), default_presentation)) {
 				// TODO: Should require there to exist a @PresentationImplementation for @Controller to work?? Good?
 				if (presentations.get(presentation)._implementation != null)
 					presentation_controller_bindings.add(new PresentationControllerBinding(element, 
 							presentations.get(presentation)._implementation));
 			}
 			
 			// Now that the @Controller annotation has been verified and its data extracted find its implementations				
 			// TODO: very inefficient
 			for (Element implementation_element : environment.getElementsAnnotatedWith(ControllerImplementation.class)) {
 				// Makes sure to only deal with Controller implementations for the current @Controller
 				if (!_type_utilities.isSubtype(implementation_element.asType(), element.asType()))
 					continue;
 				
 				System.out.println("\twith an implementation of " + implementation_element);
 				
 				// Gathers @ControllerImplementation information
 				final String scope = implementation_element.getAnnotation(ControllerImplementation.class).value();
 				final PackageElement package_name = _element_utilities.getPackageOf(implementation_element);
 				final List<ControllerData> implementations = controllers.get(scope);
 				if (implementations == null)
 					controllers.put(scope, newArrayList(new ControllerData(element, implementation_element)));
 				// Verifies that the scope-grouped @ControllerImplementations are in the same package
 				else if (!_element_utilities.getPackageOf(implementations.get(0)._implementation).equals(package_name)) {
 					error(element, "All @ControllerImplementation(\"%s\") must be defined in the same package (%s).",
 						  scope, implementation_element);
 					// Skips the current element
 					continue;
 				} else
 					implementations.add(new ControllerData(element, implementation_element));
 			}
 		}
 		
 		System.out.println("Finished processing Controllers.");
 	}
 	
 	private void processModels(RoundEnvironment environment, Map<String, List<ModelData>> models, 
 			                   List<ModelData> model_interfaces) {
 		for (Element element : environment.getElementsAnnotatedWith(Model.class)) {
 			System.out.println("@Model is " + element);
 			
 			// Verifies that the target type is an interface
 			if (element.getKind() != INTERFACE) {
 				error(element, "@Model annotation may only be specified on interfaces (%s).",
 					  element);
 				// Skips the current element
 				continue;
 			} 
 			
 			// Verifies that the interface's visibility is public
 			if (!element.getModifiers().contains(PUBLIC)) {
 				error(element, "@Model interface must be public (%s).",
 					  element);
 				// Skips the current element
 				continue;
 			}
 			
 			model_interfaces.add(new ModelData(element, null, null));
 			// Now that the @Controller annotation has been verified and its data extracted find its implementations				
 			// TODO: very inefficient
 			for (Element implementation_element : environment.getElementsAnnotatedWith(ModelImplementation.class)) {
 				// Makes sure to only deal with Model implementations for the current @Model
 				if (!_type_utilities.isSubtype(implementation_element.asType(), element.asType()))
 					continue;
 				
 				System.out.println("\twith an implementation of " + implementation_element);
 				
 				// Gathers @ModelImplementation information
 				final String scope = implementation_element.getAnnotation(ModelImplementation.class).value();
 				final PackageElement package_name = _element_utilities.getPackageOf(implementation_element);
 				final List<ModelData> implementations = models.get(scope);
 				final List<? extends VariableElement> parameters = injectModelConstructor((TypeElement) implementation_element);
 				if (implementations == null)
 					models.put(scope, newArrayList(new ModelData(element, implementation_element, parameters)));
 				// Verifies that the scope-grouped @ControllerImplementations are in the same package
 				else if (!_element_utilities.getPackageOf(implementations.get(0)._implementation).equals(package_name)) {
 					error(element, "All @ModelImplementation(\"%s\") must be defined in the same package (%s).",
 						  scope, implementation_element);
 					// Skips the current element
 					continue;
 				} else
 					implementations.add(new ModelData(element, implementation_element, parameters));
 			}
 		}
 		System.out.println("Finished processing Models.");
 	}
 	
 	private void processModelInjections(RoundEnvironment environment, Map<Element, List<ModelInjectionData>> model_injections,
 			                            List<ModelData> models) {
 		final Set<Element> model_interfaces = ImmutableSet.copyOf(Lists.transform(models, 
 				new Function<ModelData, Element>() {
 
 					@Override
 					@Nullable public Element apply(@Nullable ModelData model) {
 						return model._interface;
 					}
 		}));
 		for (Element element : environment.getElementsAnnotatedWith(InjectModel.class)) {
 			// @InjectModel constructors are processed during @Model processing
 			if (element.getKind() != FIELD) continue;
 			
 			final TypeElement enclosing_element = (TypeElement) element
 					.getEnclosingElement();
 			System.out.println("@InjectModel is " + element);
 			System.out.println("\tin " + enclosing_element);
 
 			// Verifies containing type is a Controller Implementation
 			if (enclosing_element.getAnnotation(ControllerImplementation.class) == null) {
 				error(element,
 					  "@InjectModel-annotated fields must be specified in @ControllerImplementation classes (%s).",
 					  enclosing_element);
 				// Skips the current element
 				continue;
 			}
 
 			// Verifies that the target type is a Model
 			System.out.println("\tinjecting Model " + element.asType());
 			if (!model_interfaces.contains(_type_utilities.asElement(element.asType()))) {
 				error(element, "@InjectModel must be a Model (%s).", enclosing_element);
 				continue;
 			}
 
 			// Verifies field properties
 			Set<Modifier> modifiers = element.getModifiers();
 			if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
 				error(element, "@InjectModel fields must not be private or static (%s.%s).",
 					  enclosing_element.getQualifiedName(), element);
 				continue;
 			}
 
 			// Gathers the @InjectDataSource information
 			final String package_name = _element_utilities.getPackageOf(enclosing_element) + "";
 			final String element_class = _element_utilities.getBinaryName((TypeElement) enclosing_element) + "";
 			final ModelInjectionData injection = new ModelInjectionData(package_name, element, element_class);
 			if (model_injections.containsKey(enclosing_element))
 				model_injections.get(enclosing_element).add(injection);
 			else
 				model_injections.put(enclosing_element, newArrayList(injection));
 		}
 	}
 	
 	private void processPresentationFragmentInjections(RoundEnvironment environment, 
             Map<Element, List<PresentationFragmentInjectionData>> presentation_fragment_injections,
             Map<Element, List<PresentationFragmentInjectionData>> controller_presentation_fragment_injections,
             Map<Element, PresentationFragmentData> presentation_fragments) {
 		for (Element element : environment.getElementsAnnotatedWith(InjectPresentationFragment.class)) {
 			// Verifies @InjectPresentationFragment is on a field
 			if (element.getKind() != FIELD)
 				continue;
 
 			final TypeElement enclosing_element = (TypeElement) element
 					.getEnclosingElement();
 			System.out.println("@InjectPresentationFragment is " + element);
 			System.out.println("\tin " + enclosing_element);
 
 			// Verifies containing type is a Presentation, Presentation Fragment, or Controller implementation
 			if (enclosing_element.getAnnotation(PresentationImplementation.class) == null && 
 				enclosing_element.getAnnotation(PresentationFragmentImplementation.class) == null &&
 				enclosing_element.getAnnotation(ControllerImplementation.class) == null) {
 				error(element,
 						"@InjectPresentationFragment-annotated fields must be specified in @PresentationImplementation " +
 						"or @PresentationFragmentImplementation classes (%s).",
 						enclosing_element);
 				// Skips the current element
 				continue;
 			}
 
 			// Verifies field properties
 			Set<Modifier> modifiers = element.getModifiers();
 			if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
 				error(element,
 						"@InjectPresentationFragment fields must not be private or static (%s.%s).",
 						enclosing_element.getQualifiedName(), element);
 				continue;
 			}
 
 			// Gathers the @InjectPresentaionFragment information
 			final String package_name = _element_utilities.getPackageOf(enclosing_element) + "";
 			final String element_class = _element_utilities.getBinaryName((TypeElement) enclosing_element) + "";
 			final InjectPresentationFragment inject_annotation = 
 					element.getAnnotation(InjectPresentationFragment.class);
 			final int[] displays = inject_annotation.value();
 			final String tag = inject_annotation.tag();
 			final boolean is_manually_created = inject_annotation.manual();
 			final PresentationFragmentInjectionData injection = new PresentationFragmentInjectionData(
 					package_name, element, element_class, displays, tag, 
 					presentation_fragments.get(_type_utilities.asElement(element.asType()))._implementation,
 					is_manually_created);
 			if (enclosing_element.getAnnotation(ControllerImplementation.class) != null) {
 				if (controller_presentation_fragment_injections.containsKey(enclosing_element))
 					controller_presentation_fragment_injections.get(enclosing_element).add(injection);
 				else
 					controller_presentation_fragment_injections.put(enclosing_element, newArrayList(injection));
 			} else if (presentation_fragment_injections.containsKey(enclosing_element))
 				presentation_fragment_injections.get(enclosing_element).add(injection);
 			else
 				presentation_fragment_injections.put(enclosing_element, newArrayList(injection));
 		}
 	}
 
 	// TODO: add tests for @InjectPresentation
     private void processPresentationInjections(RoundEnvironment environment,
                                                Map<Element, PresentationInjectionData> controller_presentation_injections) {
         for (Element element : environment.getElementsAnnotatedWith(InjectPresentation.class)) {
             // Verifies @InjectPresentation is on a field
             if (element.getKind() != FIELD)
                 continue;
 
             final TypeElement enclosing_element = (TypeElement) element.getEnclosingElement();
             System.out.println("@InjectPresentation is " + element);
             System.out.println("\tin " + enclosing_element);
 
             // Verifies containing type is a Controller implementation
             if (enclosing_element.getAnnotation(ControllerImplementation.class) == null) {
                 error(element,
                         "@InjectPresentation-annotated fields must be specified in @ControllerImplementation " +
                                 " classes (%s).",
                         enclosing_element);
                 // Skips the current element
                 continue;
             }
 
             // Verifies field properties
             Set<Modifier> modifiers = element.getModifiers();
             if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
                 error(element,
                         "@InjectPresentation fields must not be private or static (%s.%s).",
                         enclosing_element.getQualifiedName(), element);
                 continue;
             }
 
             // Gathers the @InjectPresentation information
             final String package_name = _element_utilities.getPackageOf(enclosing_element) + "";
             final String element_class = _element_utilities.getBinaryName((TypeElement) enclosing_element) + "";
             final PresentationInjectionData injection = new PresentationInjectionData(package_name, element, element_class);
 
             // Verifies only one @InjectPresentation per Controller
             if (controller_presentation_injections.containsKey(enclosing_element)) {
                 error(element,
                         "A Controller must only have one @InjectPresentation-annotated field " +
                                 " (%s).",
                         enclosing_element);
                 // Skips the current element
                 //noinspection UnnecessaryContinue
                 continue;
             } else
                 controller_presentation_injections.put(enclosing_element, injection);
         }
     }
 
 	@SuppressWarnings("unchecked")
 	private void generateSourceCode(List<PresentationControllerBinding> controllers, List<ModuleData> controller_modules,
 			                        List<DataSourceInjectionData> data_source_injections,
 			                        List<ModuleData> model_modules, List<ModelData> model_interfaces, 
 			                        Map<Element, List<ModelInjectionData>> model_injections,
 			                        Map<Element, Map<Integer, List<PresentationFragmentInjectionData>>> presentation_fragment_injections,
 			                        Map<Element, List<PresentationFragmentInjectionData>> controller_presentation_fragment_injections,
                                     Map<Element, PresentationInjectionData> controller_presentation_injections) {
 		final Filer filer = processingEnv.getFiler();
 		Writer writer = null;
 		try {				
 			// Generates the _SegueController
 			JavaFileObject source_code = filer.createSourceFile(_SEGUE_CONTROLLER_SOURCE, (Element) null);
 	        writer = source_code.openWriter();
 	        writer.flush();
 	        generateSegueControllerSourceCode(writer, controllers, controller_modules, model_modules, model_interfaces);
 	        
 	        // Generates the *ControllerModules
 	        for (ModuleData controller_module : controller_modules) {
 	        	source_code = filer.createSourceFile(controller_module._qualified_name, (Element) null);
 	        	writer = source_code.openWriter();
 	        	writer.flush();
 	        	generateControllerModule(writer, controller_module._package_name, 
 	        			                 (List<ControllerData>) controller_module._components, controller_module._class_name);
 	        }
 	        
 	        // Generates the $$DataSourceInjectors
 	        final Elements element_utilities = processingEnv.getElementUtils();
 	        for (DataSourceInjectionData data_source_injection : data_source_injections) {
 	        	final TypeElement element = (TypeElement) data_source_injection._target;
 	        	source_code = filer.createSourceFile(element_utilities.getBinaryName(element) + DATA_SOURCE_INJECTOR_SUFFIX, 
 	        			                             element);
 	        	writer = source_code.openWriter();
 	        	writer.flush();
 	        	generateDataSourceInjector(writer, data_source_injection._package_name, data_source_injection._target,
 	        			                   data_source_injection._variable_name, data_source_injection._class_name);
 	        }
 	        
 	        // Generates the *ModelModules
 	        for (ModuleData model_module : model_modules) {
 	        	source_code = filer.createSourceFile(model_module._qualified_name, (Element) null);
 	        	writer = source_code.openWriter();
 	        	writer.flush();
 	        	generateModelModule(writer, model_module._package_name, (List<ModelData>) model_module._components,
 	        			            model_module._class_name);
 	        }
 	        
 	        // Generates the $$ModelInjectors
 	        for (Entry<Element, List<ModelInjectionData>> injection : model_injections.entrySet()) {
 	        	final TypeElement element = (TypeElement) injection.getKey();
 	        	final String full_name = element_utilities.getBinaryName(element) + MODEL_INJECTOR_SUFFIX;
 	        	source_code = filer.createSourceFile(full_name, element);
 	        	writer = source_code.openWriter();
 	        	writer.flush();
 	        	final String package_name = _element_utilities.getPackageOf(element) + "";
 	        	final String class_name = full_name.substring(package_name.length() + 1);
 	        	generateModelInjector(writer, package_name, injection.getValue(), class_name, element);
 	        }
 	        
 	        // Generates the $$PresentationFragmentInjectors
 	        for (Entry<Element, Map<Integer, List<PresentationFragmentInjectionData>>> injection : presentation_fragment_injections.entrySet()) {
 	        	final TypeElement element = (TypeElement) injection.getKey();
 	        	final String full_name = _element_utilities.getBinaryName(element) + PRESENTATION_FRAGMENT_INJECTOR_SUFFIX;
 	        	source_code = filer.createSourceFile(full_name, element);
 	        	writer = source_code.openWriter();
 	        	writer.flush();
 	        	final String package_name = _element_utilities.getPackageOf(element) + "";
 	        	final String class_name = full_name.substring(package_name.length() + 1);
 	        	generatePresentationFragmentInjector(writer, package_name, injection.getValue(), class_name, element);
 	        }
 	        for (Entry<Element, List<PresentationFragmentInjectionData>> injection : controller_presentation_fragment_injections.entrySet()) {
 	        	final TypeElement element = (TypeElement) injection.getKey();
 	        	final String full_name = _element_utilities.getBinaryName(element) + PRESENTATION_FRAGMENT_INJECTOR_SUFFIX;
 	        	source_code = filer.createSourceFile(full_name, element);
 	        	writer = source_code.openWriter();
 	        	writer.flush();
 	        	final String package_name = _element_utilities.getPackageOf(element) + "";
 	        	final String class_name = full_name.substring(package_name.length() + 1);
 	        	generateControllerPresentationFragmentInjector(writer, package_name, injection.getValue(), class_name, element);
 	        }
 
             // Generates the $$PresentationInjectors
             for (Entry<Element, PresentationInjectionData> injection : controller_presentation_injections.entrySet()) {
                 final TypeElement element = (TypeElement) injection.getKey();
                 final String full_name = _element_utilities.getBinaryName(element) + PRESENTATION_INJECTOR_SUFFIX;
                 source_code = filer.createSourceFile(full_name, element);
                 writer = source_code.openWriter();
                 writer.flush();
                 final String package_name = _element_utilities.getPackageOf(element) + "";
                 final String class_name = full_name.substring(package_name.length() + 1);
                 generateControllerPresentationInjector(writer, package_name, injection.getValue(), class_name, element);
             }
 		} catch (IOException exception) {
 			processingEnv.getMessager().printMessage(ERROR, exception.getMessage());
 		} finally {
 			try {
 				Closeables.close(writer, writer != null);
 			} catch (IOException exception) { }
 		}
 	}
 
 	/**
 	 * @param segue_controller_data_model
 	 * @param writer
 	 * @throws IOException
 	 */
 	private void generateSegueControllerSourceCode(Writer writer, List<PresentationControllerBinding> controllers,
                                                    List<ModuleData> controller_modules, List<ModuleData> model_modules,
                                                    List<ModelData> models)
 			                                        		throws IOException {
 		final EnumSet<Modifier> public_modifier = EnumSet.of(PUBLIC);
 		final EnumSet<Modifier> private_final = EnumSet.of(PRIVATE, FINAL);
 		JavaWriter java_writer = new JavaWriter(writer);
 		java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
 				   .emitPackage("com.imminentmeals.prestige")
 		           .emitImports("java.util.HashMap",
 		        		        "java.util.ArrayList",
 						        "java.util.List",
 						        "java.util.Map",
 						        "javax.inject.Inject",
 						        "javax.inject.Named",
 						        "javax.inject.Provider",
 						        JavaWriter.type(Lazy.class),
 						        "android.app.Activity",
 						        "com.google.common.collect.ImmutableMap",
 						        "com.squareup.otto.Bus",
 						        "dagger.Module",
 						        "dagger.ObjectGraph")
 					.emitEmptyLine()
 					.emitJavadoc("<p>A Segue Controller that handles getting the appropriate Controller\n" +
 							     "for the current Presentation, and communicating with the Controller Bus.</p>")
 				    .beginType("com.imminentmeals.prestige._SegueController", "class", public_modifier, null, 
 				    		   // implements
 				    		   "com.imminentmeals.prestige.SegueController")
 				    .emitJavadoc("Bus over which Presentations communicate to their Controllers")
 				    .emitAnnotation(Inject.class)
 				    .emitAnnotation(Named.class, ControllerContract.BUS)
 				    .emitField("com.squareup.otto.Bus", "controller_bus");
 		final StringBuilder controller_puts = new StringBuilder();
 		for (PresentationControllerBinding binding : controllers) {
 			java_writer.emitJavadoc("Provider for instances of the {@link %s} Controller", binding._controller)
 			           .emitAnnotation(Inject.class)
 			           .emitField("javax.inject.Provider<" + binding._controller + ">", binding._variable_name);
 			controller_puts.append(String.format(".put(%s.class, %s)\n", 
 					    binding._presentation_implementation, binding._variable_name));
 		}
 		
 		final StringBuilder model_puts = new StringBuilder();
 		for (ModelData model : models) {
 			java_writer.emitJavadoc("Provider for instances of the {@link %s} Model", model._interface)
 			           .emitAnnotation(Inject.class)
 			           .emitField("dagger.Lazy<" + model._interface + ">", model._variable_name);
 			model_puts.append(String.format(".put(%s.class, %s)\n",
 					model._interface, model._variable_name));
 		}
 		// Constructor
 		java_writer.emitEmptyLine()
 		           .emitJavadoc("<p>Constructs a {@link SegueController}.</p>")
 		           .beginMethod(null, "com.imminentmeals.prestige._SegueController", public_modifier, 
 		        		        "java.lang.String", "scope")
 		           .emitStatement("List<Object> modules = new ArrayList<Object>()")
 		           .emitSingleLineComment("Controller modules");
 		if (!controller_modules.isEmpty()) {
 			for (ModuleData controller_module : controller_modules)
 				if (controller_module._scope.equals(Implementations.PRODUCTION)) {
 					java_writer.emitStatement("modules.add(new %s())", controller_module._qualified_name);
 					break;
 				}
 			java_writer.beginControlFlow("if (scope.equals(\"" + controller_modules.get(0)._scope + "\"))")
 		               .emitStatement("modules.add(new %s())", controller_modules.get(0)._qualified_name)
 		               .endControlFlow();
 			for (ModuleData module : controller_modules.subList(min(1, controller_modules.size()), controller_modules.size()))
 				java_writer.beginControlFlow("else if (scope.equals(\"" + module._scope + "\"))")
 		                   .emitStatement("modules.add(new %s())", module._qualified_name)
 		                   .endControlFlow();
 		}
 		java_writer.emitSingleLineComment("Model modules");
 		if (!model_modules.isEmpty()) {
 			for (ModuleData model_module : model_modules)
 				if (model_module._scope.equals(Implementations.PRODUCTION)) {
 					java_writer.emitStatement("modules.add(new %s())", model_module._qualified_name);
 					break;
 				}
 			java_writer.beginControlFlow("if (scope.equals(\"" + model_modules.get(0)._scope + "\"))")
 		               .emitStatement("modules.add(new %s())", model_modules.get(0)._qualified_name)
 		               .endControlFlow();
 			for (ModuleData module : model_modules.subList(1, model_modules.size()))
 				java_writer.beginControlFlow("else if (scope.equals(\"" + module._scope + "\"))")
 		        .emitStatement("modules.add(new %s())", module._qualified_name)
 		        .endControlFlow();
 		}
 		java_writer.emitStatement("_object_graph = ObjectGraph.create(modules.toArray())")
 		           .emitStatement("_object_graph.inject(this)")
 		           .emitStatement(
 				"_presentation_controllers = ImmutableMap.<Class<?>, Provider>builder()\n" +
 				"%s.build()",
 				controller_puts)
 				   .emitStatement(
                 "_model_implementations = ImmutableMap.<Class<?>, Lazy>builder()\n%s.build()", model_puts)
 		           .emitStatement("_controllers = new HashMap<Class<?>, Object>()")
 				   .endMethod()
 				   .emitEmptyLine()
 				   // SegueController Contract 
 				   .emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("unchecked"))
 				   .emitAnnotation(Override.class)
 				   .beginMethod("<T> T", "dataSource", public_modifier, "Class<?>", "target")
 				   .emitStatement("return (T) _controllers.get(target)")
 				   .endMethod()
 				   .emitEmptyLine()
 				   .emitAnnotation(Override.class)
 				   .beginMethod("void", "sendMessage", public_modifier, "Object", "message")
 				   .emitStatement("controller_bus.post(message)")
 				   .endMethod()
 				   .emitEmptyLine()
 				   .emitAnnotation(Override.class)
 				   .beginMethod("void", "createController", public_modifier, 
 						        JavaWriter.type(Activity.class), "activity")
 				   .emitStatement("final Class<?> activity_class = activity.getClass()")
 				   .emitStatement("if (!_presentation_controllers.containsKey(activity_class)) return")
 				   .emitEmptyLine()
 				   .emitStatement("final Object controller = _presentation_controllers.get(activity_class).get()")
 				   .emitStatement("Prestige.injectModels(this, controller)")
                    .emitStatement("Prestige.injectPresentation(controller, activity)")
 				   .emitStatement("_controllers.put(activity_class, controller)")
 				   .endMethod()
 				   .emitEmptyLine()
 				   .emitAnnotation(Override.class)
 				   .beginMethod("void", "didDestroyActivity", public_modifier, 
 						   JavaWriter.type(Activity.class), "activity")
 				   .emitStatement("final Class<?> activity_class = activity.getClass()")
 				   .emitStatement("if (!_presentation_controllers.containsKey(activity_class)) return")
 				   .emitEmptyLine()
 				   .emitStatement("_controllers.remove(activity_class)")
 				   .endMethod()
 				   .emitEmptyLine()
 				   .emitAnnotation(SuppressWarnings.class, JavaWriter.stringLiteral("unchecked"))
 				   .emitAnnotation(Override.class)
 				   .beginMethod("<T> T", "createModel", public_modifier, "Class<T>", "model_interface")
 				   .emitStatement("return (T) _model_implementations.get(model_interface).get()")
 				   .endMethod()
 				   .emitEmptyLine()
 				   .emitAnnotation(Override.class)
 				   .beginMethod("void", "attachPresentationFragment", public_modifier,
 						   JavaWriter.type(Activity.class), "activity",
 						   JavaWriter.type(Object.class), "presentation_fragment",
 						   JavaWriter.type(String.class), "tag")
 				   .emitStatement("final Object controller = _controllers.get(activity.getClass())")
 				   .beginControlFlow("if (controller != null)")
 				   .emitStatement("Prestige.attachPresentationFragment(controller, presentation_fragment, tag)")
 				   .endControlFlow()
 				   .endMethod()
 				   .emitEmptyLine()
 				   .emitAnnotation(Override.class)
 				   .beginMethod("void", "registerForControllerBus", public_modifier,
 						   JavaWriter.type(Activity.class), "activity")
 				   .emitStatement("final Class<?> activity_class = activity.getClass()")
 				   .emitStatement("if (!_controllers.containsKey(activity_class)) return")
 				   .emitEmptyLine()
 				   .emitStatement("controller_bus.register(_controllers.get(activity_class))")
 				   .endMethod()
 				   .emitEmptyLine()
 				   .emitAnnotation(Override.class)
 				   .beginMethod("void", "unregisterForControllerBus", public_modifier,
 						   JavaWriter.type(Activity.class), "activity")
 				   .emitStatement("final Class<?> activity_class = activity.getClass()")
 				   .emitStatement("if (!_controllers.containsKey(activity_class)) return")
 				   .emitEmptyLine()
 				   .emitStatement("controller_bus.unregister(_controllers.get(activity_class))")
 				   .endMethod()
 				   .emitEmptyLine()
 				   // Private fields
 				   .emitJavadoc("Dependency injection object graph")
 				   .emitField("ObjectGraph", "_object_graph", private_final)
 				   .emitJavadoc("Provides the Controller implementation for the given Presentation Implementation")
 				   .emitField("ImmutableMap<Class<?>, Provider>",
 						      "_presentation_controllers", private_final)
 				   .emitJavadoc("Maintains the Controller references as they are being used")
 				   .emitField("Map<Class<?>, Object>",
 						      "_controllers", private_final)
 				   .emitJavadoc("Provides the Model implementation for the given Model interface")
 				   .emitField("Map<Class<?>, Lazy>", "_model_implementations", private_final)
 				   .endType()
 				   .emitEmptyLine();
 		java_writer.close();
 	}
 	
 	/**
 	 * @param writer
 	 * @param _package_name
 	 * @param _components
 	 * @param _class_name
 	 */
 	private void generateControllerModule(Writer writer, String package_name, List<ControllerData> controllers, 
 			                             String class_name) throws IOException {
 		final EnumSet<Modifier> public_modifier = EnumSet.of(PUBLIC);
 		JavaWriter java_writer = new JavaWriter(writer);
 		java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
 				   .emitPackage(package_name)
 				   .emitImports("javax.inject.Named",
 						        "com.imminentmeals.prestige._SegueController",
 						        "com.squareup.otto.Bus",
 						        "dagger.Module",
 						        "dagger.Provides",
 						        "javax.inject.Singleton")
 					.emitEmptyLine();
 		final StringBuilder controller_list = new StringBuilder();
 		for (ControllerData controller : controllers) {
 			controller_list.append("<li>{@link " + controller._interface + "}</li>\n");
 		}
 		java_writer.emitJavadoc("<p>Module for injecting:\n" +
 				                "<ul>\n" +
 				                "%s" +
 				                "</ul></p>", controller_list)
 				   .emitAnnotation(Module.class, ImmutableMap.of(
 						   "injects",
 						   "{\n" +
 								   "_SegueController.class" +
 						   "\n}",
 						   "overrides", !class_name.equals(_DEFAULT_CONTROLLER_MODULE),
 						   "library", true,
 						   "complete", false))
 					.beginType(class_name, "class", public_modifier)
 					.emitEmptyLine()
 					.emitAnnotation(Provides.class)
 					.emitAnnotation(Singleton.class)
 					.emitAnnotation(Named.class, ControllerContract.BUS)
 					.beginMethod("com.squareup.otto.Bus", "providesControllerBus", EnumSet.noneOf(Modifier.class))
 					.emitStatement("return new Bus(\"Controller Bus\")")
 					.endMethod();
 		// Controller providers
 		for (ControllerData controller : controllers)
 			java_writer.emitEmptyLine()
 			           .emitAnnotation(Provides.class)
 			           .beginMethod(controller._interface + "", "provides" + controller._interface.getSimpleName(),
 			        		        EnumSet.noneOf(Modifier.class))
 			           .emitStatement("return new %s()", controller._implementation)
 			           .endMethod();
 		java_writer.endType();
 		java_writer.close();
 	}
 	
 	/**
 	 * @param writer
 	 * @param _package_name
 	 * @param _variable
 	 * @param _variable_name
 	 * @param _class_name
 	 */
 	private void generateDataSourceInjector(Writer writer, String package_name, Element target, String variable_name,
 			                                String class_name) throws IOException {
 		final JavaWriter java_writer = new JavaWriter(writer);
 		java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
 		           .emitPackage(package_name)
 		           .emitImports(JavaWriter.type(Finder.class))
 			       .emitEmptyLine()
 			       .emitJavadoc("<p>Injects the Data Source into {@link %s}'s %s.</p>", target, variable_name)
 			       .beginType(class_name, "class", EnumSet.of(PUBLIC, FINAL))
 			       .emitEmptyLine()
 			       .emitJavadoc("<p>Injects the Data Source into {@link %s}'s %s.</p>\n" +
 			       		        "@param finder The finder that specifies how to retrieve the Segue Controller from the target\n" +
 			       		        "@param target The target of the injection", target, variable_name)
 			       .beginMethod("void", "injectDataSource", EnumSet.of(PUBLIC, STATIC), 
 			    		        JavaWriter.type(Finder.class), "finder", 
			    		        processingEnv.getElementUtils().getBinaryName((TypeElement) target) + "", "target")
 			       .emitStatement("target.%s = " +
 			       		"finder.findSegueControllerApplication(target).segueController().dataSource(" +
 			       		"finder.findContext(target).getClass())", 
 			       		variable_name)
 			       .endMethod()
 			       .endType()
 			       .emitEmptyLine();
 		java_writer.close();
 	}
 	
 	/**
 	 * @param writer
 	 * @param _package_name
 	 * @param _components
 	 * @param _class_name
 	 */
 	private void generateModelModule(Writer writer, String package_name, List<ModelData> models, String class_name) 
 			throws IOException {
 		JavaWriter java_writer = new JavaWriter(writer);
 		java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
 				   .emitPackage(package_name)
 				   .emitImports("javax.inject.Named",
 						        "com.imminentmeals.prestige._SegueController",
 						        "dagger.Module",
 						        "dagger.Provides",
 						        "javax.inject.Singleton")
 					.emitEmptyLine();
 		final StringBuilder model_list = new StringBuilder();
 		for (ModelData model : models) {
 			model_list.append("<li>{@link " + model._interface + "}</li>\n");
 		}
 		java_writer.emitJavadoc("<p>Module for injecting:\n" +
 				                "<ul>\n" +
 				                "%s" +
 				                "</ul></p>", model_list)
 				   .emitAnnotation(Module.class, ImmutableMap.of(
 						   "injects",
 						   "{\n" +
 								   "_SegueController.class" +
 						   "\n}",
 						   /*"entryPoints", 
 						   "{\n" +
 							   Joiner.on(",\n").join(Lists.asList("_SegueController.class", Lists.transform(models, 
 									   new Function<ModelData, String>() {
 
 										@Override
 										@Nullable public String apply(@Nullable ModelData model) {
 											return model._implementation + ".class";
 										}										
 								}).toArray())) +
 						   "\n}",*/
 						   "overrides", !class_name.equals(_DEFAULT_MODEL_MODULE),
 						   "library", true,
 						   "complete", false))
 					.beginType(class_name, "class", EnumSet.of(PUBLIC))
 					.emitEmptyLine();
 		// Model providers
 		for (ModelData model : models)
 			if (model._parameters == null || model._parameters.isEmpty())
 				java_writer.emitEmptyLine()
 				           .emitAnnotation(Provides.class)
 				           .emitAnnotation(Singleton.class)
 				           .beginMethod(model._interface + "", "provides" + model._interface.getSimpleName(),
 				        		        EnumSet.noneOf(Modifier.class))
 				           .emitStatement("return new %s()", model._implementation)
 				           .endMethod();
 			else {
 				final List<String> provider_method_parameters = newArrayList();
 				final List<String> constructor_parameters = newArrayList();
 				System.out.println("Model " + model._interface);
 				for (VariableElement parameter : model._parameters) {
 					System.out.print("\t has parameter " + parameter);
 					System.out.println(" of type " + parameter.asType());
 					provider_method_parameters.add(_type_utilities.asElement(parameter.asType()) + "");
 					provider_method_parameters.add(parameter + "");
 					constructor_parameters.add(parameter + "");
 				}
 				final String[] parameters = new String[provider_method_parameters.size()];
 				java_writer.emitEmptyLine()
 				           .emitAnnotation(Provides.class)
 				           .emitAnnotation(Singleton.class)
 				           .beginMethod(model._interface + "", "provides" + model._interface.getSimpleName(),
 				        		        EnumSet.noneOf(Modifier.class), 
 				        		        provider_method_parameters.toArray(parameters))
 				           .emitStatement("return new %s(%s)", model._implementation,
 				        		   Joiner.on(", ").join(constructor_parameters))
 				           .endMethod();
 			}
 		java_writer.endType();
 		java_writer.close();
 	}
 	
 	/**
 	 * @param writer
 	 * @param _package_name
 	 * @param _variable
 	 * @param _variable_name
 	 * @param _class_name
 	 */
 	private void generateModelInjector(Writer writer, String package_name, List<ModelInjectionData> injections,
 			                           String class_name, Element target) throws IOException {
 		final JavaWriter java_writer = new JavaWriter(writer);
 		java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
 		           .emitPackage(package_name)
 			       .emitEmptyLine()
 			       .emitJavadoc("<p>Injects the Models into {@link %s}.</p>", class_name)
 			       .beginType(class_name, "class", EnumSet.of(PUBLIC, FINAL))
 			       .emitEmptyLine()
 			       .emitJavadoc("<p>Injects the Models into {@link %s}.</p>\n" +
 			       		        "@param segue_controller The Segue Controller from which to retrieve Models\n" +
 			       		        "@param target The target of the injection", target)
 			       .beginMethod("void", "injectModels", EnumSet.of(PUBLIC, STATIC), 
 			    		        JavaWriter.type(SegueController.class), "segue_controller", 
 			    		        processingEnv.getElementUtils().getBinaryName((TypeElement) target) + "", "target");
 		for (ModelInjectionData injection : injections)
 			java_writer.emitStatement("target.%s = segue_controller.createModel(%s.class)", injection._variable_name,
 					                  injection._variable.asType());
 		java_writer.endMethod()
 			       .endType()
 			       .emitEmptyLine();
 		java_writer.close();
 	}
 
 	private void generateControllerPresentationFragmentInjector(Writer writer, String package_name, 
 			List<PresentationFragmentInjectionData> injections, String class_name, Element target) throws IOException {
 		final JavaWriter java_writer = new JavaWriter(writer);
 		java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
 		           .emitPackage(package_name)
 		           .emitEmptyLine()
 		           .emitEmptyLine()
 		           .emitJavadoc("<p>Injects the Presentation Fragments into {@link %s}.</p>", class_name)
 		           .beginType(class_name, "class", EnumSet.of(PUBLIC, FINAL))
 		           .emitEmptyLine()
 		           .emitJavadoc("<p>Injects the Presentation Fragments into {@link %s}.</p>\n" +
 		        		        "@param int display The current display state\n" +
 		        		        "@param target The target of the injection", target)
 		           .beginMethod("void", "attachPresentationFragment", 
 		        		        EnumSet.of(PUBLIC, STATIC),
 		        		        processingEnv.getElementUtils().getBinaryName((TypeElement) target) + "", "target",
 		        		        JavaWriter.type(Object.class), "presentation_fragment",
 		        		        JavaWriter.type(String.class), "tag");
 		final String control_format = "%s (presentation_fragment instanceof %s &&\n" +
 		        		              "\ttag.equals(\"%s\"))";
 		if (!injections.isEmpty()) {
 			final PresentationFragmentInjectionData injection = injections.get(0);
 			java_writer.beginControlFlow(String.format(control_format, "if", injection._implementation, injection._tag))
 			           .emitStatement("target.%s = (%s) presentation_fragment", injection._variable_name, 
 			        		          injection._variable.asType())
 			           .endControlFlow();
 		}
 		for (PresentationFragmentInjectionData injection : injections.subList(min(1, injections.size()), injections.size()))
 			java_writer.beginControlFlow(String.format(control_format, "else if", injection._implementation, injection._tag))
 		               .emitStatement("target.%s = (%s) presentation_fragment", injection._variable_name,
 		            		          injection._variable.asType())
 		               .endControlFlow();
 		
 		java_writer.endMethod()
 		           .endType()
 		           .emitEmptyLine();
 		java_writer.close();
 	}
 	
 	private void generatePresentationFragmentInjector(Writer writer, String package_name, 
 			Map<Integer, List<PresentationFragmentInjectionData>> injections, String class_name, Element target) throws IOException {
 		final JavaWriter java_writer = new JavaWriter(writer);
 		java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
 		           .emitPackage(package_name)
 		           .emitEmptyLine()
 		           .emitImports(JavaWriter.type(FragmentManager.class),
 		        		        JavaWriter.type(Fragment.class),
 		        		        JavaWriter.type(Finder.class),
 		        		        JavaWriter.type(Context.class))
 		           .emitEmptyLine()
 		           .emitJavadoc("<p>Injects the Presentation Fragments into {@link %s}.</p>", target)
 		           .beginType(class_name, "class", EnumSet.of(PUBLIC, FINAL))
 		           .emitEmptyLine()
 		           .emitJavadoc("<p>Injects the Presentation Fragments into {@link %s}.</p>\n" +
 		                        "@param finder The finder that specifies how to retrieve the context\n" +
 		        		        "@param int display The current display state\n" +
 		        		        "@param target The target of the injection", target)
 		           .beginMethod("void", "injectPresentationFragments", 
 		        		        EnumSet.of(PUBLIC, STATIC),
 		        		        JavaWriter.type(Finder.class), "finder",
 		        		        JavaWriter.type(int.class), "display",
 		        		        processingEnv.getElementUtils().getBinaryName((TypeElement) target) + "", "target");
 		if (!injections.isEmpty()) {
 			java_writer.emitStatement("final Context context = finder.findContext(target)")
 			           .emitStatement("final FragmentManager fragment_manager = finder.findFragmentManager(target)")
 			           .beginControlFlow("switch (display)");
 			for (Entry<Integer, List<PresentationFragmentInjectionData>> entry : injections.entrySet()) {
 				java_writer.beginControlFlow("case " + entry.getKey() + ":");
 				final StringBuilder transactions = new StringBuilder();
 				for (PresentationFragmentInjectionData injection : entry.getValue()) {
 					java_writer.emitStatement("target.%s = (%s) Fragment.instantiate(context, \"%s\")", injection._variable_name,
 			                                  injection._variable.asType(), injection._implementation);
 	                transactions.append("\t.add(" +  injection._displays.get(entry.getKey()) + ",\n" +
 			            "\t(Fragment) \ttarget." + injection._variable_name);
 	                if (!injection._tag.isEmpty())
 	                	transactions.append(",\n\"" + injection._tag + "\"");
 	                transactions.append(")\n");
 				}
 				java_writer.emitStatement("fragment_manager.beginTransaction()\n" + transactions + "\t.commit()")
 						   .emitStatement("break")
 		                   .endControlFlow();
 			}
 			java_writer.endControlFlow();
 		}
 		java_writer.endMethod()
                    .endType()
                    .emitEmptyLine();
 		java_writer.close();
 	}
 
     private void generateControllerPresentationInjector(Writer writer, String package_name, PresentationInjectionData injection,
                                                         String class_name, Element target) throws IOException {
         final JavaWriter java_writer = new JavaWriter(writer);
         java_writer.emitSingleLineComment("Generated code from Prestige. Do not modify!")
                 .emitPackage(package_name)
                 .emitEmptyLine()
                 .emitEmptyLine()
                 .emitJavadoc("<p>Injects the Presentation into {@link %s}.</p>", class_name)
                 .beginType(class_name, "class", EnumSet.of(PUBLIC, FINAL))
                 .emitEmptyLine()
                 .emitJavadoc("<p>Injects the Presentation into {@link %s}.</p>\n" +
                         "@param target The target of the injection\n" +
                         "@param presentation The presentation to inject", target)
                 .beginMethod("void", "injectPresentation",
                         EnumSet.of(PUBLIC, STATIC),
                         processingEnv.getElementUtils().getBinaryName((TypeElement) target) + "", "target",
                         JavaWriter.type(Object.class), "presentation");
         final String control_format = "if (presentation instanceof %s)";
         java_writer.beginControlFlow(String.format(Locale.US, control_format, injection._variable.asType()))
                 .emitStatement("target.%s = (%s) presentation", injection._variable_name, injection._variable.asType())
                 .endControlFlow();
 
         java_writer.endMethod()
                 .endType()
                 .emitEmptyLine();
         java_writer.close();
     }
 	
 	@CheckForNull private List<? extends VariableElement> injectModelConstructor(TypeElement element) {
 		List<? extends VariableElement> parameters = null;
 		boolean found_accessible_constructor = false;
 		boolean found_annotated_constructor = false;
 		for (Element member : element.getEnclosedElements())
 			if (member.getAnnotation(InjectModel.class) != null && member.getKind() == CONSTRUCTOR) {
 				// Verifies there is at most one @InjectModel constructors
 				if (found_annotated_constructor)
 					error(element, "There must only be one @InjectModel constructor (%s).", member);
 				parameters = ((ExecutableElement) member).getParameters();
 				found_annotated_constructor = true;
 				found_accessible_constructor = !member.getModifiers().contains(PRIVATE);
 				break;
 			} else if (member.getKind() == CONSTRUCTOR)
 				found_accessible_constructor = !member.getModifiers().contains(PRIVATE);
 		
 		// Verifies there is an accessible constructor 
 		if (!found_accessible_constructor)
 			error(element, "There must be a non-private @InjectModel or default constructor (%s).", element);
 		return parameters;
 	}
 	
 	/**
 	 * <p>Produces an error message with the given information.</p>
 	 * @param element The element to relate the error message to
 	 * @param message The message to send
 	 * @param arguments Arguments to format into the message
 	 */
 	private void error(Element element, String message, Object... arguments) {
 		processingEnv.getMessager().printMessage(ERROR,
 				String.format(message, arguments), element);
 	}
 	
 	/**
 	 * <p>Container for Presentation Controller binding data. Relates an @Controller to an @Presentation's 
 	 * implementation and provides a name to use to refer to an instance of the @Controller's implementation.</p>
 	 * @author Dandre Allison
 	 */
 	private static class PresentationControllerBinding {
 		
 		private final Element _controller;
 		private final String _variable_name;
 		private final Element _presentation_implementation;
 		
 		/**
 		 * <p>Constructs a {@link PresentationControllerBinding}.</p>
 		 * @param controller The @Controller
 		 * @param presentation_implementation The implementation of the @Controller's @Presentation 
 		 */
 		public PresentationControllerBinding(Element controller, Element presentation_implementation) {
 			final String class_name = controller.getSimpleName() + "";
 			_controller = controller;
 			_variable_name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, class_name);
 			_presentation_implementation = presentation_implementation;
 		}
 	}
 	
 	/**
 	 * <p>Container for @Controller data, groups the Controller interface with its implementation</p>.
 	 * @author Dandre Allison
 	 */
 	private static class ControllerData {
 		/** The @ControllerImplementation stored for use in setting up code generation */
 		private final Element _implementation;
 		private final Element _interface;
 		
 		/**
 		 * <p>Constructs a {@link ControllerData}.<p>
 		 * @param controller The @Controller 
 		 * @param controller_implementation The implementation of the @Controller
 		 */
 		public ControllerData(Element controller, Element controller_implementation) {
 			_implementation = controller_implementation;
 			_interface = controller;
 		}
 	}
 	
 	/**
 	 * <p>Container for @Model data, groups the Model interface with its implementation</p>.
 	 * @author Dandre Allison
 	 */
 	private static class ModelData {
 		/** The @ModelImplementation stored for use in setting up code generation */
 		private final Element _implementation;
 		private final Element _interface;
 		private final List<? extends VariableElement> _parameters;
 		private final String _variable_name;
 		
 		/**
 		 * <p>
 		 * Constructs a {@link ModelData}.
 		 * <p>
 		 * 
 		 * @param model The @Model
 		 * @param model_implementation The implementation of the @Model
 		 */
 		public ModelData(@Nonnull Element model, Element model_implementation, List<? extends VariableElement> parameters) {
 			_implementation = model_implementation;
 			_interface = model;
 			_parameters = parameters;
 			_variable_name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, model.getSimpleName() + "");
 		}
 	}
 	
 	/**
 	 * <p>Container for Presentation Fragment data.</p>
 	 * @author Dandre Allison
 	 */
 	private static class PresentationFragmentData extends PresentationData {
 		
 		/**
 		 * <p>Constructs a {@link PresentationData}.</p>
 		 * @param protocol The Protocol
 		 * @param implementation The presentation implementation
 		 */
 		public PresentationFragmentData(Element protocol, Element implementation) {
 			super(protocol, implementation);
 		}
 
 		@Override
 		public String toString() {
 			return String.format(format, _protocol, _implementation);
 		}
 		
 		@Syntax("RegEx")
 		private static final String format = "{protocol: %s, implementation: %s}";
 	}
 	
 	/**
 	 * <p>Container for Presentation data.</p>
 	 * @author Dandre Allison
 	 */
 	private static class PresentationData {
 		/** The Protocol */
 		protected final Element _protocol;
 		/** The Presentation implementation */
 		protected final Element _implementation;
 		
 		/**
 		 * <p>Constructs a {@link PresentationData}.</p>
 		 * @param protocol The Protocol
 		 * @param implementation The presentation implementation
 		 */
 		public PresentationData(Element protocol, Element implementation) {
 			_protocol = protocol;
 			_implementation = implementation;
 		}
 
 		@Override
 		public String toString() {
 			return String.format(format, _protocol, _implementation);
 		}
 		
 		@Syntax("RegEx")
 		private static final String format = "{protocol: %s, implementation: %s}";
 	}
 	
 	private static class ModuleData {
 		private final String _qualified_name;
 		private final String _scope;
 		private final String _class_name;
 		private final String _package_name;
 		private final List<?> _components;
 		
 		/**
 		 * <p>Constructs a {@link ModuleData}.</p>
 		 * @param element The module
 		 * @param scope The implementation scope the module provides
 		 */
 		public ModuleData(String name, String scope, String class_name, String package_name, List<?> components) {
 			_qualified_name = name;
 			_scope = scope;
 			_class_name = class_name;
 			_package_name = package_name;
 			_components = components;
 		}
 	}
 	
 	private static class DataSourceInjectionData {
 		private final String _package_name;
 		private final Element _target;
 		private final String _variable_name;
 		private final String _class_name;
 		
 		/**
 		 * <p>Constructs a {@link DataSourceInjectionData}.</p>
 		 * @param target The target of the injection
 		 * @param variable The variable from the target in which to inject the Data Source
 		 */
 		public DataSourceInjectionData(String package_name, Element target, Element variable, String element_class) {
 			_package_name = package_name;
 			_target = target;
 			_variable_name = variable.getSimpleName() + "";
 			_class_name = element_class.substring(package_name.length() + 1) + DATA_SOURCE_INJECTOR_SUFFIX;
 		}
 	}
 	
 	private static class ModelInjectionData {
 		private final String _package_name;
 		private final Element _variable;
 		private final String _variable_name;
 		private final String _class_name;
 		
 		/**
 		 * <p>Constructs a {@link ModelInjectionData}.</p>
 		 * @param target The target of the injection
 		 * @param variable The variable in which to inject the Model
 		 */
 		public ModelInjectionData(String package_name, Element variable, String element_class) {
 			_package_name = package_name;
 			_variable = variable;
 			_variable_name = variable.getSimpleName() + "";
 			_class_name = element_class.substring(package_name.length() + 1) + MODEL_INJECTOR_SUFFIX;
 		}
 	}
 	
 	private static class PresentationFragmentInjectionData {
 		private final String _package_name;
 		private final Element _variable;
 		private final String _variable_name;
 		private final String _class_name;
 		private final Map<Integer, Integer> _displays;
 		private final String _tag;
 		private final Element _implementation;
 		private final boolean _is_manually_created;
 		
 		public PresentationFragmentInjectionData(String package_name, Element variable, String element_class,
 				int[] displays, String tag, Element implementation, boolean is_manually_created) {
 			assert displays.length % 2 == 0;
 			
 			_package_name = package_name;
 			_variable = variable;
 			_variable_name = variable.getSimpleName() + "";
 			_class_name = element_class.substring(package_name.length() + 1) + PRESENTATION_FRAGMENT_INJECTOR_SUFFIX;
 			_implementation = implementation;
 			_is_manually_created = is_manually_created;
 			
 			if (_is_manually_created) {
 				_displays = null;
 				_tag = null;
 				return;
 			}
 			
 			final ImmutableMap.Builder<Integer, Integer> builder = ImmutableMap.builder();
 			for (int i = 0; i < displays.length; i += 2)
 				builder.put(displays[i], displays[i + 1]);
 			_displays = builder.build();
 			_tag = tag;
 		}
 	}
 
     private static class PresentationInjectionData {
         private final String _package_name;
         private final Element _variable;
         private final String _variable_name;
         private final String _class_name;
 
         public PresentationInjectionData(String package_name, Element variable, String element_class) {
             _package_name = package_name;
             _variable = variable;
             _variable_name = variable.getSimpleName() + "";
             _class_name = element_class.substring(package_name.length() + 1) + PRESENTATION_INJECTOR_SUFFIX;
         }
     }
 	
 	/** Extracts the root from a Controller following the naming convention "*Controller" */
 	private static final Matcher _CONTROLLER_TO_ROOT = Pattern.compile("(.+)Controller").matcher("");
 	/** Qualified name for the SegueController source code */
 	private static final String _SEGUE_CONTROLLER_SOURCE = "com.imminentmeals.prestige._SegueController";
 	private static final String _DEFAULT_CONTROLLER_MODULE = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, PRODUCTION) + 
 			CONTROLLER_MODULE_SUFFIX;
 	private static final String _DEFAULT_MODEL_MODULE = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, PRODUCTION) +
 			MODEL_MODULE_SUFFIX;
 	/** Counts the number of passes The Annotation Processor has performed */
 	private int _passes = 0;
 	private Elements _element_utilities;
 	private Types _type_utilities;
 }
