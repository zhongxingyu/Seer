 package org.eclipse.xtext.services.validation;
 
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.toArray;
 import static com.google.common.collect.Lists.transform;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.xtext.common.types.JvmAnnotationTarget;
 import org.eclipse.xtext.common.types.JvmFormalParameter;
 import org.eclipse.xtext.common.types.JvmGenericType;
 import org.eclipse.xtext.common.types.JvmOperation;
 import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
 import org.eclipse.xtext.common.types.JvmType;
 import org.eclipse.xtext.common.types.JvmTypeParameterDeclarator;
 import org.eclipse.xtext.common.types.JvmTypeReference;
 import org.eclipse.xtext.common.types.util.FeatureOverridesService;
 import org.eclipse.xtext.common.types.util.ITypeArgumentContext;
 import org.eclipse.xtext.common.types.util.JavaReflectAccess;
 import org.eclipse.xtext.common.types.util.TypeArgumentContextProvider;
 import org.eclipse.xtext.common.types.util.TypeReferences;
 import org.eclipse.xtext.services.services.Component;
 import org.eclipse.xtext.services.services.Service;
 import org.eclipse.xtext.services.services.ServicesPackage;
 import org.eclipse.xtext.services.types.TypeErasedSignature;
 import org.eclipse.xtext.validation.Check;
 import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;
 
 import com.google.common.base.Function;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
  
 
 /**
  * Parts of this implementation have been taken from the XtendJavaValidator.
  * 
  * @author Robert von Massow
  */
 @SuppressWarnings("restriction")
 public class ServicesJavaValidator extends AbstractServicesJavaValidator {
 
 	@Inject
 	private FeatureOverridesService featureOverridesService;
 
 	@Inject
 	private TypeArgumentContextProvider typeArgumentContextProvider;
 
 	@Inject
 	private TypeReferences types;
 
 	@Inject
 	private JavaReflectAccess jra;
 
 	@Inject
 	private JvmModelAssociator jvmModelAssociator;
 
 	@Inject
 	private TypeErasedSignature.Provider typeErasedSignatureProvider;
 
 	@Inject
 	private ServiceOverridesService overridesService;
 
 	@Check
 	public void checkComponentStartsWithCapital(Component component) {
 		if (!Character.isUpperCase(component.getName().charAt(0))) {
 			warning("Name should start with a capital", ServicesPackage.Literals.COMPONENT__NAME);
 		}
 	}
 
 	@Check
 	public void checkServiceArguments(Service service) {
 		if(service.isService()) {
 			JvmType type = service.getType().getType();
 			if(type != null)
 				if((List.class).isAssignableFrom(jra.getRawType(type))) {
 					type = ((JvmGenericType)type).getTypeParameters().get(0);
 				}
 			if(type instanceof JvmAnnotationTarget && !isSupportedPrimitiveType(type)) {
 				JvmAnnotationTarget targetType = (JvmAnnotationTarget)type;
 				if(!targetType.getAnnotations().contains(types.findDeclaredType("javax.xml.bind.annotation.XmlRootElement", service))) {
					error("Return type is not an XmlRootElement, supported primitive type (long, int, boolean, String) or a List of those", ServicesPackage.Literals.SERVICE__TYPE);
 				}
 			}
 			for(int i = 0; i< service.getParams().size(); i++) {
 				JvmFormalParameter parameter = service.getParams().get(i);
 				if((List.class).isAssignableFrom(jra.getRawType(parameter.getParameterType().getType()))) {
 					type = ((JvmGenericType)type).getTypeParameters().get(0);
 				}
 				if(type instanceof JvmAnnotationTarget && !isSupportedPrimitiveType(type)) {
 					JvmAnnotationTarget targetType = (JvmAnnotationTarget)type;
 					if(!targetType.getAnnotations().contains(types.findDeclaredType("javax.xml.bind.annotation.XmlRootElement", service))) {
 						error("Parameter "+ parameter.getName()+" is not an XmlRootElement or supported primitive", ServicesPackage.Literals.SERVICE__PARAMS, i);
 					}
 				}
 			}
 		}
 	}
 
 	private boolean isSupportedPrimitiveType(JvmType type) {
 		Class<?> rawType = jra.getRawType(type);
 		return boolean.class.isAssignableFrom(rawType) ||
 				int.class.isAssignableFrom(rawType) ||
 				long.class.isAssignableFrom(rawType) ||
 				String.class.isAssignableFrom(rawType);
 	}
 
 	@Check
 	public void checkInterfaceConformance(final Component component) {
 		for (int i = 0; i < component.getImplements().size(); ++i) {
 			JvmTypeReference implementedType = component.getImplements().get(i);
 			if (!(implementedType.getType() instanceof JvmGenericType)
 					|| !((JvmGenericType) implementedType.getType()).isInterface()) {
 				error("Components can only conform to interfaces", ServicesPackage.Literals.COMPONENT__IMPLEMENTS, i, ServicesIssueCodes.INTERFACE_EXPECTED);
 			}
 		}
 		final JvmGenericType inferredType = getFirstOrNull(jvmModelAssociator.getJvmElements(component), JvmGenericType.class);
 		final JvmParameterizedTypeReference typeRef = types.createTypeRef(inferredType);
 		final ITypeArgumentContext typeArgumentContext = typeArgumentContextProvider
 				.getTypeArgumentContext(new TypeArgumentContextProvider.AbstractRequest() {
 					@Override
 					public JvmTypeReference getReceiverType() {
 						return typeRef;
 					}
 
 					@Override
 					public JvmTypeParameterDeclarator getNearestDeclarator() {
 						return inferredType;
 					}
 				});
 		if(inferredType != null) {
 			Multimap<Object, JvmOperation> operationsPerErasure = HashMultimap.create();
 			for (JvmOperation operation : inferredType.getDeclaredOperations()) {
 				TypeErasedSignature signature = typeErasedSignatureProvider.get(operation);
 				operationsPerErasure.put(signature, operation);
 			}
 			List<JvmOperation> operationsMissingImplementation = null;
 			// get all declared operations
 			for(JvmOperation operation : filter(
 					featureOverridesService.getAllJvmFeatures(inferredType, typeArgumentContext), JvmOperation.class)){
 				// if the operation has been declared by the component or an implemented interface
 				if(operation.getDeclaringType() != inferredType) {
 					TypeErasedSignature signature = typeErasedSignatureProvider.get(operation);
 					if (operationsPerErasure.containsKey(signature)) {
 						Collection<JvmOperation> myOperations = operationsPerErasure.get(signature);
 						if (myOperations.size() == 1) {
 							JvmOperation myOperation = Iterables.getOnlyElement(myOperations);
 							if (!featureOverridesService.isOverridden(myOperation, operation, typeArgumentContext,
 									false)) {
 								Service source = (Service) jvmModelAssociator.getPrimarySourceElement(myOperation);
 								error("Name clash: The service "
 										+ myOperation.getQualifiedName() + " of type "
 										+ inferredType.getSimpleName()
 										+ " has the same erasure as "
 										+
 										// use source with other operations parameters to avoid confusion
 										// due to name transformations in JVM model inference
 										operation.getQualifiedName() + " of type "
 										+ operation.getDeclaringType().getSimpleName() + " but does not override it.",
 										source, ServicesPackage.Literals.SERVICE__NAME, 0);
 							}
 						}
 					}
 					if(operation.isAbstract()) {
 						boolean overridden = false;
 						if (operationsPerErasure.containsKey(signature)) {
 							for (JvmOperation myOperation : operationsPerErasure.get(signature)) {
 								if (featureOverridesService.isOverridden(myOperation, operation, typeArgumentContext,
 										false)) {
 									overridden = true;
 									break;
 								}
 							}
 						}
 						if (!overridden) {
 							if(operationsMissingImplementation == null)
 								operationsMissingImplementation = Lists.newArrayList();
 							operationsMissingImplementation.add(operation);
 						}
 					}
 				}
 			}
 			if(operationsMissingImplementation != null) {
 				
 				reportMissingImplementations(component, typeArgumentContext, operationsMissingImplementation);
 			}
 		}
 	}
 
 	protected void reportMissingImplementations(Component xtendClass, final ITypeArgumentContext typeArgumentContext,
 			List<JvmOperation> operationsMissingImplementation) {
 		StringBuilder errorMsg = new StringBuilder();
 		errorMsg.append("The component ").append(xtendClass.getName())
 			.append(" must implement ");
 		boolean needsNewLine = operationsMissingImplementation.size() > 1;
 		JvmOperation operation;
 		for(int i=0; i<operationsMissingImplementation.size() && i<3; ++i) {
 			operation = operationsMissingImplementation.get(i);
 			if(needsNewLine)
 				errorMsg.append("\n- ");
 			errorMsg.append(operation.getSimpleName());
 //			, Lists.transform(
 //						operation.getParameters(),
 //						new Function<JvmFormalParameter, JvmTypeReference>() {
 //							public JvmTypeReference apply(JvmFormalParameter from) {
 //								JvmTypeReference parameterType = from.getParameterType();
 //								JvmTypeReference result = typeArgumentContext
 //										.resolve(parameterType);
 //								return result;
 //							}
 //						})));
 		}
 		int numUnshownOperations = operationsMissingImplementation.size() - 3;
 		if(numUnshownOperations >0)
 			errorMsg.append("\nand " +  numUnshownOperations + " more.");
 		List<String> uris = transform(operationsMissingImplementation, new Function<JvmOperation, String>() {
 			public String apply(JvmOperation from) {
 				return EcoreUtil.getURI(from).toString();
 			}
 		});
 		error(errorMsg.toString(), xtendClass, ServicesPackage.Literals.COMPONENT__NAME, ServicesIssueCodes.MISSING_IMPLEMENTATION, 
 						toArray(uris, String.class));
 	}
 	
 	@Check
 	protected void checkFunctionOverride(Service service) {
 		JvmOperation operation = getFirstOrNull(jvmModelAssociator.getJvmElements(service), JvmOperation.class);
 		JvmOperation overriddenOperation = overridesService.findOverriddenOperation(service);
 		if(overriddenOperation != null) {
 			if (!service.isService())
 				error("The internal service " + operation.getSimpleName() +" of type "+operation.getDeclaringType().getSimpleName()+" must be a service.", service,
 						ServicesPackage.Literals.SERVICE__NAME, ServicesIssueCodes.SERVICE_EXPECTED);
 			JvmOperation inferredOperation = overridesService.getDirectlyInferredOperation(service);
 			for(JvmTypeReference unhandledException: findUnhandledExceptions(service, Collections.<JvmTypeReference>emptyList(),overriddenOperation.getExceptions()))
 				error("Services can not throw exceptions: " + unhandledException.getSimpleName() + " thrown by " + overriddenOperation.getQualifiedName()
 						, ServicesPackage.Literals.SERVICE__NAME);
 			if (service.getType() == types.createTypeRef(types.findDeclaredType(void.class, service))) {
 				return;
 			}
 			ITypeArgumentContext typeArgumentContext = typeArgumentContextProvider
 					.getTypeArgumentContext(new TypeArgumentContextProvider.ReceiverRequest(getTypeRefs().createTypeRef(
 							inferredOperation.getDeclaringType())));
 			JvmTypeReference returnTypeUpperBound = typeArgumentContext.getUpperBound(overriddenOperation.getReturnType(),
 					service);
 			if (!isConformant(returnTypeUpperBound, service.getType())) {
 				error("The return type is incompatible with " + overriddenOperation.getIdentifier(), service,
 						ServicesPackage.Literals.SERVICE__TYPE, ServicesIssueCodes.INCOMPATIBLE_RETURN_TYPE);
 			}
 		}
 	}
 
 	protected <T> T getFirstOrNull(Iterable<EObject> elements, Class<T> type) {
 		Iterator<T> iterator = filter(elements, type).iterator();
 		return iterator.hasNext() ? iterator.next() : null;
 	}
 
 	@Check
 	public void checkValidPackage(Component c) {
 		
 	}
 }
