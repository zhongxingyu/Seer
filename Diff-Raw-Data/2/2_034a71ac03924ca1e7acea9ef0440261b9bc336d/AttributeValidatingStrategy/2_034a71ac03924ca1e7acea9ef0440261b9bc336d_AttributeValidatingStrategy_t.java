 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.validation.internal.strategy;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.jst.jsf.common.dom.AttrDOMAdapter;
 import org.eclipse.jst.jsf.common.dom.AttributeIdentifier;
 import org.eclipse.jst.jsf.common.dom.DOMAdapter;
 import org.eclipse.jst.jsf.common.internal.types.CompositeType;
 import org.eclipse.jst.jsf.common.internal.types.TypeComparator;
 import org.eclipse.jst.jsf.common.internal.types.TypeComparatorDiagnosticFactory;
 import org.eclipse.jst.jsf.common.internal.types.TypeConstants;
 import org.eclipse.jst.jsf.common.internal.types.TypeTransformer;
 import org.eclipse.jst.jsf.common.runtime.internal.model.ViewObject;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentFactory;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ConverterDecorator;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ConverterTypeInfo;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContextFactory;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.region.Region2AttrAdapter;
 import org.eclipse.jst.jsf.core.internal.region.Region2ElementAdapter;
 import org.eclipse.jst.jsf.core.jsfappconfig.internal.IJSFAppConfigManager;
 import org.eclipse.jst.jsf.core.jsfappconfig.internal.JSFAppConfigManagerFactory;
 import org.eclipse.jst.jsf.designtime.DTAppManagerUtil;
 import org.eclipse.jst.jsf.designtime.internal.view.DTUIViewRoot;
 import org.eclipse.jst.jsf.designtime.internal.view.IDTViewHandler.ViewHandlerException;
 import org.eclipse.jst.jsf.designtime.internal.view.XMLViewDefnAdapter;
 import org.eclipse.jst.jsf.designtime.internal.view.XMLViewDefnAdapter.DTELExpression;
 import org.eclipse.jst.jsf.designtime.internal.view.XMLViewObjectMappingService;
 import org.eclipse.jst.jsf.designtime.internal.view.XMLViewObjectMappingService.ElementData;
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterForClassType;
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterType;
 import org.eclipse.jst.jsf.metadataprocessors.MetaDataEnabledProcessingFactory;
 import org.eclipse.jst.jsf.metadataprocessors.features.ELIsNotValidException;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidELValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidationMessage;
 import org.eclipse.jst.jsf.validation.internal.AbstractXMLViewValidationStrategy;
 import org.eclipse.jst.jsf.validation.internal.JSFValidationContext;
 import org.eclipse.jst.jsf.validation.internal.el.ELExpressionValidator;
 import org.eclipse.jst.jsp.core.internal.regions.DOMJSPRegionContexts;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 
 /**
  * An XML view validation strategy that validates
  * 
  * @author cbateman
  * 
  */
 public class AttributeValidatingStrategy extends
 AbstractXMLViewValidationStrategy
 {
     private static final  String       DISABLE_ALTERATIVE_TYPES_KEY = "jsfCoreDisableConverterValidation"; //$NON-NLS-1$
     private static final  String       ENABLE_ALTERATIVE_TYPES_KEY  = "jsfCoreEnableConverterValidation"; //$NON-NLS-1$
     static final boolean               DEBUG;
     static
     {
         final String value = Platform
         .getDebugOption("org.eclipse.jst.jsf.validation.internal.el/debug/jspsemanticsvalidator"); //$NON-NLS-1$
         DEBUG = value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
     }
 
     /**
      * identifier
      */
     public final static String         ID           = "org.eclipse.jst.jsf.validation.strategy.AttributeValidatingStrategy"; //$NON-NLS-1$
     private final static String        DISPLAY_NAME = "Attribute Validator"; //$NON-NLS-1$
 
     private final JSFValidationContext _validationContext;
     private final TypeComparator       _typeComparator;
     private Set<String>                _conversionTypes;
 
     /**
      * Default constructor
      * 
      * @param validationContext
      */
     public AttributeValidatingStrategy(final JSFValidationContext validationContext)
     {
         super(ID, DISPLAY_NAME);
 
         _validationContext = validationContext;
         _typeComparator = new TypeComparator(
                 new TypeComparatorDiagnosticFactory(validationContext
                         .getPrefs().getTypeComparatorPrefs()));
     }
 
     @Override
     public boolean isInteresting(final DOMAdapter domAdapter)
     {
         return (domAdapter instanceof AttrDOMAdapter);
     }
 
     @Override
     public void validate(final DOMAdapter domAdapter)
     {
         if (domAdapter instanceof AttrDOMAdapter)
         {
             final long curTime = System.nanoTime();
             final Region2AttrAdapter attrAdapter = (Region2AttrAdapter) domAdapter;
             // check that this is attribute value region - 221722
             if (attrAdapter.getAttributeValueRegion() != null)
             {
                 final IStructuredDocumentContext context = IStructuredDocumentContextFactory.INSTANCE
                 .getContext(attrAdapter.getDocumentContext()
                         .getStructuredDocument(), attrAdapter
                         .getOwningElement().getDocumentContext()
                         .getDocumentPosition()
                         + attrAdapter.getAttributeValueRegion()
                         .getStart());
 
                 validateAttributeValue(context, attrAdapter);
             }
             if (DEBUG)
             {
                 System.out.println(String.format("Validation for attribute: %s took %d" //$NON-NLS-1$
                     , domAdapter.toString()
                     , Long.valueOf(System.nanoTime()-curTime)));
             }
         }
     }
 
     /**
      * Validates the attribute value. Reports any problems to the reporter in
      * the JSFValidationContext.
      * 
      * @param context
      * @param attrAdapter
      */
     private void validateAttributeValue(
             final IStructuredDocumentContext context,
             final Region2AttrAdapter attrAdapter)
     {
         // so of the code in run calls out into extension code or code
         // dependent on external data (meta-data)
         SafeRunner.run(new ISafeRunnable()
         {
             public void handleException(final Throwable exception)
             {
                 JSFCorePlugin.log(String.format(
                         "Error validating attribute: %s on element %s", //$NON-NLS-1$
                         attrAdapter.getNodeName(), attrAdapter
                         .getOwningElement().getNodeName()), exception);
             }
 
             public void run() throws Exception
             {
                 final Region2ElementAdapter elementAdapter =
                     attrAdapter.getOwningElement();
                 // if there's elText then validate it
                 // TODO: this approach will fail with mixed expressions
                 if (!checkIfELAndValidate(elementAdapter, attrAdapter, context))
                 {
                     validateNonELAttributeValue(context, attrAdapter);
                 }
             }
         });
     }
 
     private boolean checkIfELAndValidate(final Region2ElementAdapter elementAdapter,
             final Region2AttrAdapter attrAdapter,
             final IStructuredDocumentContext context)
     {
         int offsetOfFirstEL = -1;
         final String attrValue = attrAdapter.getValue();
 
         // TODO: should find and validate all
         offsetOfFirstEL = attrValue.indexOf('#');
 
         if (offsetOfFirstEL != -1 && offsetOfFirstEL < attrValue.length() - 1
                 && attrValue.charAt(offsetOfFirstEL + 1) == '{')
         {
             offsetOfFirstEL += 2;
         }
         else
         {
             offsetOfFirstEL = -1;
         }
 
         final XMLViewDefnAdapter adapter = DTAppManagerUtil
         .getXMLViewDefnAdapter(context);
 
         boolean isEL = false;
         if (adapter != null && offsetOfFirstEL != -1)
         {
             try
             {
                 // use the attribute's context plus the offset into the
                 // whole attribute value to find where we think the el
                 // expression starts. Add one since the attribute value
                 // string returned by attrAdapter will have the value quotes
                 // removed, but the region offsets include the quotes.
                 IStructuredDocumentContext elContext = IStructuredDocumentContextFactory.INSTANCE
                 .getContext(context.getStructuredDocument(), context
                         .getDocumentPosition()
                         + offsetOfFirstEL + 1);
                 final DTELExpression elExpression = adapter
                 .getELExpression(elContext);
                 if (elExpression != null)
                 {
                     final String elText = elExpression.getText();
 
                     if (DEBUG)
                     {
                         System.out.println(addDebugSpacer(3) + "EL attrVal= " //$NON-NLS-1$
                                 + elText);
                     }
 
                     elContext = elExpression.getDocumentContext();
                     // EL validation is user configurable because
                     // it can be computationally costly.
                     if (_validationContext.shouldValidateEL())
                     {
                         // also, skip the validation if the expression is empty
                         // or only whitespace, since the parser doesn't handle
                         // it
                         // anyway.
                         if ("".equals(elText.trim())) //$NON-NLS-1$
                         {
                             final int offset = elContext.getDocumentPosition() - 1;
                             final int length = elText.length() + 2;
                             final Diagnostic diagnostic = _validationContext
                             .getDiagnosticFactory()
                             .create_EMPTY_EL_EXPRESSION();
                             // detected empty EL expression
                             if (_validationContext.shouldValidateEL())
                             {
                                 _validationContext.getReporter().report(
                                         diagnostic, offset, length);
                             }
                         }
                         else
                         {
                             final List elVals = MetaDataEnabledProcessingFactory
                             .getInstance()
                             .getAttributeValueRuntimeTypeFeatureProcessors(
                                     IValidELValues.class,
                                     elContext,
                                     attrAdapter
                                     .getAttributeIdentifier());
                             final String safeELText = elText.replaceAll(
                                     "[\n\r\t]", " "); //$NON-NLS-1$ //$NON-NLS-2$
                             validateELExpression(context, elContext, elVals,
                                     elementAdapter, attrAdapter, safeELText);
                             isEL = true;
                         }
                    } else {
                        isEL = true;
                     }
                 }
             }
             catch (final ViewHandlerException e)
             {
                 // fall through to return false
             }
         }
 
         // is el if we've already detected it or if the step 2 method
         // finds it. Run the method first to avoid short-circuiting
         final boolean isEL2 = checkIfELAndValidate2(attrAdapter, context);
 
         return isEL || isEL2;
     }
 
     /**
      * Checks the region to see if it contains an EL attribute value. If it
      * does, validates it
      * 
      * @return true if validated EL, false otherwise
      */
     private boolean checkIfELAndValidate2(final Region2AttrAdapter attrAdapter,
             final IStructuredDocumentContext sDocContext)
     {
         final ITextRegion attrValueRegion = attrAdapter
                 .getAttributeValueRegion();
         if (attrValueRegion instanceof ITextRegionCollection)
         {
             final ITextRegionCollection parentRegion = ((ITextRegionCollection) attrValueRegion);
             if (parentRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE)
             {
                 final ITextRegionList regionList = parentRegion.getRegions();
 
                 if (regionList.size() >= 3)
                 {
                     final ITextRegion openQuote = regionList.get(0);
                     final ITextRegion vblOpen = regionList.get(1);
 
                     if ((openQuote.getType() == DOMJSPRegionContexts.XML_TAG_ATTRIBUTE_VALUE_DQUOTE || openQuote
                             .getType() == DOMJSPRegionContexts.JSP_VBL_DQUOTE || 
                             openQuote.getType() == DOMJSPRegionContexts.JSP_TAG_ATTRIBUTE_VALUE_DQUOTE)
                             && vblOpen.getType() == DOMJSPRegionContexts.JSP_VBL_OPEN)
                     {
                         boolean foundClosingQuote = false;
                         for (int i = 2; !foundClosingQuote
                                 && i < regionList.size(); i++)
                         {
                             final ITextRegion searchRegion = regionList.get(i);
                             if (searchRegion.getType() == DOMJSPRegionContexts.JSP_VBL_CLOSE)
                             {
                                 foundClosingQuote = true;
                             }
                         }
 
                         if (!foundClosingQuote
                                 && _validationContext.shouldValidateEL())
                         {
                             final int offset = sDocContext
                                     .getDocumentPosition() + 1;
                             final int length = parentRegion.getText().length();
                             final Diagnostic diagnostic = _validationContext
                                     .getDiagnosticFactory()
                                     .create_MISSING_CLOSING_EXPR_BRACKET();
                             _validationContext.getReporter().report(diagnostic,
                                     offset, length);
                         }
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private void validateELExpression(final IStructuredDocumentContext context,
             final IStructuredDocumentContext elContext, final List elVals,
             final Region2ElementAdapter elementAdapter,
             final Region2AttrAdapter attrAdapter, final String elText)
     {
         // Call EL validator which will perform at least the syntactical
         // validation
         final ELExpressionValidator elValidator = new ELExpressionValidator(
                 elContext, elText, _validationContext
                         .getSymbolResolverFactory(), _validationContext
                         .getReporter());
         elValidator.validateXMLNode();
 
         final CompositeType exprType = elValidator.getExpressionType();
         if (exprType != null)
         {
         	// Ignore the expression whose last two segments are of types Object.
         	final CompositeType boxedType = TypeTransformer
             	.transformBoxPrimitives(exprType);
         	final String[] testSignatures = boxedType.getSignatures();
         	if (testSignatures.length > 0 && TypeConstants.TYPE_JAVAOBJECT.equals(testSignatures[0])) 
         	{
         		if (elText.indexOf('.') != -1) 
         		{
         			String elText2 = elText.substring(0, elText.lastIndexOf('.'));
                     final ELExpressionValidator elValidator2 = new ELExpressionValidator(
                             elContext, elText2, _validationContext
                                     .getSymbolResolverFactory(), _validationContext
                                     .getReporter());
                     elValidator2.validateXMLNode();
 
                     final CompositeType exprType2 = elValidator.getExpressionType();
                 	final CompositeType boxedType2 = TypeTransformer.transformBoxPrimitives(exprType2);
                 	final String[] testSignatures2 = boxedType2.getSignatures();
                 	if (testSignatures2.length > 0 && TypeConstants.TYPE_JAVAOBJECT.equals(testSignatures2[0])) 
                 	{
                 		return;
                 	}
         		}
         	}
         	
             for (final Iterator it = elVals.iterator(); it.hasNext();)
             {
                 final IValidELValues elval = (IValidELValues) it.next();
                 final String attributeVal = attrAdapter.getValue();
                 CompositeType expectedType;
                 Diagnostic status = null;
                 try
                 {
                     expectedType = elval.getExpectedRuntimeType();
 
                     if (expectedType != null)
                     {
                         expectedType = maybeAddAlternativeTypes(
                                 expectedType, exprType, elementAdapter,
                                 attrAdapter);
                         status = _typeComparator.calculateTypeCompatibility(
                                 expectedType, exprType);
                         if (status.getSeverity() != Diagnostic.OK)
                         {
                             reportValidationMessage(status, context,
                                     attributeVal);
                         }
                     }
                 }
                 catch (final ELIsNotValidException e)
                 {
                     reportValidationMessage(createValidationMessage(context,
                             attributeVal, IStatus.WARNING, e.getMessage(),
                             _validationContext.getFile()), context,
                             attributeVal);
                 }
             }
         }
     }
 
     private boolean disableAlternativeTypes()
     {
         if (hasProperty(DISABLE_ALTERATIVE_TYPES_KEY))
         {
             return true;
         }
 
         if (hasProperty(ENABLE_ALTERATIVE_TYPES_KEY))
         {
             return false;
         }
         
 //      As of Helios, alternative type is disabled by default
         return true;
         
 //        final IPreferenceStore prefStore = JSFCorePlugin.getDefault().getPreferenceStore();
 //        return prefStore.getBoolean("org.eclipse.jst.jsf.core."+DISABLE_ALTERATIVE_TYPES_KEY); //$NON-NLS-1$
     }
     
     private boolean hasProperty(final String key) {
     	 String res = System.getProperty(key);
          if (res == null) {
              //check env var also
              res = System.getenv(key);
          }
          return res != null;
     }
     /**
      * @return true if alternative type comparison (i.e. post-conversion) passes
      */
     private CompositeType maybeAddAlternativeTypes(
             final CompositeType expectedType, final CompositeType exprTypes,
             final Region2ElementAdapter elementAdapter,
             final Region2AttrAdapter attrAdapter)
     {
         final long curTime = System.nanoTime();
         
         //As of Helios, alternative type is disabled by default
         //and enabled by the ENABLE_ALTERNATIVE_TYPES_KEY system/env property
         if (disableAlternativeTypes())
         {
             return expectedType;
         }
 
         final IStructuredDocumentContext context = elementAdapter
                 .getDocumentContext();
         final DTUIViewRoot viewRoot = _validationContext.getViewRootHandle().getCachedViewRoot();
         final IAdaptable serviceAdaptable = viewRoot.getServices();
         final XMLViewObjectMappingService mappingService = (XMLViewObjectMappingService) serviceAdaptable
                 .getAdapter(XMLViewObjectMappingService.class);
         if (mappingService != null)
         {
             final ElementData elementData = XMLViewObjectMappingService
                     .createElementData(elementAdapter.getNamespace(),
                             elementAdapter.getLocalName(), context,
                             Collections.EMPTY_MAP);
             final ViewObject viewObject = mappingService
                     .findViewObject(elementData);
             // if the corresponding view object is a valueholder, then
             // we need to see if you think there a valid conversion
             // available
             if (viewObject instanceof ComponentInfo
                     && ((ComponentInfo) viewObject).getComponentTypeInfo() != null
                     && ((ComponentInfo) viewObject).getComponentTypeInfo()
                             .isInstanceOf(
                                     ComponentFactory.INTERFACE_VALUEHOLDER))
             {
                 final ComponentInfo component = (ComponentInfo) viewObject;
                 // get the original elementData
                 final ElementData mappedElementData = mappingService
                         .findElementData(component);
                 final String propName = mappedElementData
                         .getPropertyName(attrAdapter.getLocalName());
                 if ("value".equals(propName)) //$NON-NLS-1$
                 {
                     // final List converters =
                     // component.getDecorators(ComponentFactory.CONVERTER);
 
                     // (ConverterDecorator) it.next();
                     final CompositeType alternativeTypes = createCompositeType(
                             expectedType, exprTypes, component
                                     .getDecorators(ComponentFactory.CONVERTER));
                     if (DEBUG)
                     {
                         System.out.println(String.format(
                                 "maybeAddAlternative took %d", Long.valueOf(System //$NON-NLS-1$
                                         .nanoTime()
                                             - curTime)));
                     }
                     return alternativeTypes;
                 }
             }
         }
         if (DEBUG)
         {
             System.out.println(String.format("maybeAddAlternative took %d", Long //$NON-NLS-1$
                 .valueOf(System.nanoTime() - curTime)));
         }
         // don't add anything by default
         return expectedType;
     }
 
     private CompositeType createCompositeType(final CompositeType initialTypes,
             final CompositeType testTypes, final List<ConverterDecorator> decorators)
     {
         // indicates unknown converter
         final Set<String> types = new HashSet(Arrays.asList(initialTypes
                 .getSignatures()));
         // look for converters.  If there's one where we don't know the type,
         // simply copy over the testTypes to force validation to ignore, since
         // we have no  idea.
         for (final ConverterDecorator decorator : decorators)
         {
             if (decorator.getTypeInfo() != null)
             {
                 final ConverterTypeInfo converterTypeInfo = decorator.getTypeInfo();
                 if (converterTypeInfo.getForClass().length == 0)
                 {
                     types.addAll(Arrays.asList(testTypes.getSignatures()));
                     break;
                 }
                 types.addAll(createSignatures(converterTypeInfo.getForClass()));
             }
         }
         types.addAll(getRegisteredConversionTypesByClass());
         return new CompositeType(types.toArray(new String[0])
                 , initialTypes.getAssignmentTypeMask());
     }
 
     private Set<String>  getRegisteredConversionTypesByClass()
     {
         if (_conversionTypes == null)
         {
             _conversionTypes = new HashSet<String>();
             final IProject project = _validationContext.getFile().getProject();
             final IJSFAppConfigManager appConfig = JSFAppConfigManagerFactory.getJSFAppConfigManagerInstance(project);
             final List<ConverterType> converters = appConfig.getConverters();
             for (final ConverterType converterType : converters)
             {
                 final ConverterForClassType forClassType = converterType.getConverterForClass();
                 if (forClassType != null)
                 {
                     final String forClass = forClassType.getTextContent();
                     if (forClass != null)
                     {
                         String signature = forClass.trim();
                         try
                         {
                             // arrays are a special case where the 
                             // [Ljava.lang.String; syntax is valid.
                             if (Signature.getArrayCount(signature)>0)
                             {
                                 _conversionTypes.add(signature);
                             }
                         }
                         catch (final IllegalArgumentException e)
                         {
                             // ignore
                         }
 
                         try
                         {
                             signature = Signature.createTypeSignature(signature, true);
                             _conversionTypes.add(signature);
                         }
                         catch (final Exception e)
                         {
                             // ignore: JSFCorePlugin.log(IStatus.INFO, "Could not use registered converter for-class: "+forClass); //$NON-NLS-1$
                         }
                     }
                 }
             }
         }
         return _conversionTypes;
     }
 
     private List<String> createSignatures(final String[] classNames)
     {
         final List<String> signatures = new ArrayList<String>();
         for (final String className : classNames)
         {
             try
             {
                 String signature = Signature.createTypeSignature(className, true);
                 signatures.add(signature);
             }
             catch (final Exception e)
             {
                 JSFCorePlugin.log(e, "Trying to create signature"); //$NON-NLS-1$
             }
         }
         return signatures;
     }
 
     /**
      * Validates an attribute value in context using the JSF metadata processing
      * framework
      * 
      * @param context
      * @param region
      * @param uri
      * @param tagName
      * @param attrName
      * @param attributeVal
      * @param reporter
      * @param file
      */
     private void validateNonELAttributeValue(
             final IStructuredDocumentContext context,
             final Region2AttrAdapter attrAdapter)
     {
         final String attributeValue = attrAdapter.getValue();
         // else validate as static attribute value
         if (DEBUG)
         {
             System.out.println(addDebugSpacer(3) + "attrVal= " //$NON-NLS-1$
                     + (attributeValue != null ? attributeValue : "null")); //$NON-NLS-1$
         }
 
         final AttributeIdentifier attributeId = attrAdapter
         .getAttributeIdentifier();
 
         if (attributeId.getTagIdentifier() == null
                 || attributeId.getTagIdentifier().getTagName() == null
                 || attributeId.getName() == null)
         {
             return;
         }
 
         final List vv = MetaDataEnabledProcessingFactory.getInstance()
         .getAttributeValueRuntimeTypeFeatureProcessors(
                 IValidValues.class, context, attributeId);
         if (!vv.isEmpty())
         {
             for (final Iterator it = vv.iterator(); it.hasNext();)
             {
                 final IValidValues v = (IValidValues) it.next();
                 if (!v.isValidValue(attributeValue.trim()))
                 {
                     if (DEBUG)
                     {
                         System.out.println(addDebugSpacer(4) + "NOT VALID "); //$NON-NLS-1$
                     }
 
                     for (final Iterator msgs = v.getValidationMessages()
                             .iterator(); msgs.hasNext();)
                     {
                         final IValidationMessage msg = (IValidationMessage) msgs
                         .next();
                         reportValidationMessage(createValidationMessage(
                                 context, attributeValue, msg.getSeverity(), msg
                                 .getMessage(), _validationContext
                                 .getFile()), context, attributeValue);
                     }
                 }
                 else if (DEBUG)
                 {
                     System.out.println(addDebugSpacer(5) + "VALID "); //$NON-NLS-1$
                 }
             }
         }
         else if (DEBUG)
         {
             System.out.println(addDebugSpacer(4) + "NO META DATA "); //$NON-NLS-1$
         }
     }
 
     private void reportValidationMessage(final Diagnostic problem,
             final IStructuredDocumentContext context,
             final String attributeValue)
     {
         final int start = context.getDocumentPosition() + 1;
         final int length = attributeValue.length();
         _validationContext.getReporter().report(problem, start, length);
     }
 
     private Diagnostic createValidationMessage(
             final IStructuredDocumentContext context,
             final String attributeValue, final int severity, final String msg,
             final IFile file)
     {
         // TODO: need factory
         final Diagnostic diagnostic = new BasicDiagnostic(severity, "", -1, //$NON-NLS-1$
                 msg, null);
         return diagnostic;
     }
 
     private String addDebugSpacer(final int count)
     {
         final String TAB = "\t"; //$NON-NLS-1$
         final StringBuffer ret = new StringBuffer(""); //$NON-NLS-1$
         for (int i = 0; i <= count; i++)
         {
             ret.append(TAB);
         }
         return ret.toString();
     }
 
 }
