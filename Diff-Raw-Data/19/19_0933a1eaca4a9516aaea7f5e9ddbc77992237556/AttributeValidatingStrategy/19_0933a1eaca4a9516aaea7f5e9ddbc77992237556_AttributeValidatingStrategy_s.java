 package org.eclipse.jst.jsf.validation.internal.strategy;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jst.jsf.common.dom.AttrDOMAdapter;
 import org.eclipse.jst.jsf.common.dom.AttributeIdentifier;
 import org.eclipse.jst.jsf.common.dom.DOMAdapter;
 import org.eclipse.jst.jsf.common.internal.types.CompositeType;
 import org.eclipse.jst.jsf.common.internal.types.TypeComparator;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContextFactory;
 import org.eclipse.jst.jsf.core.internal.region.Region2AttrAdapter;
 import org.eclipse.jst.jsf.designtime.DTAppManagerUtil;
 import org.eclipse.jst.jsf.designtime.internal.view.XMLViewDefnAdapter;
 import org.eclipse.jst.jsf.designtime.internal.view.IDTViewHandler.ViewHandlerException;
 import org.eclipse.jst.jsf.designtime.internal.view.XMLViewDefnAdapter.DTELExpression;
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
     public final static String         ID           = "org.eclipse.jst.jsf.validation.strategy.AttributeValidatingStrategy";
     private final static String        DISPLAY_NAME = "Attribute Validator";
 
     private final JSFValidationContext _validationContext;
 
     /**
      * Default constructor
      * 
      * @param validationContext
      */
     public AttributeValidatingStrategy(
             final JSFValidationContext validationContext)
     {
         super(ID, DISPLAY_NAME);
 
         _validationContext = validationContext;
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
             final Region2AttrAdapter attrAdapter = (Region2AttrAdapter) domAdapter;
            final IStructuredDocumentContext context = IStructuredDocumentContextFactory.INSTANCE
                    .getContext(attrAdapter.getDocumentContext()
                            .getStructuredDocument(), attrAdapter
                            .getOwningElement().getDocumentContext()
                            .getDocumentPosition()
                            + attrAdapter.getAttributeValueRegion().getStart());

            validateAttributeValue(context, attrAdapter);
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
         // if there's elText then validate it
         // TODO: this approach will fail with mixed expressions
         if (!checkIfELAndValidate(attrAdapter, context))
         {
             validateNonELAttributeValue(context, attrAdapter);
         }
     }
 
     private boolean checkIfELAndValidate(final Region2AttrAdapter attrAdapter,
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
                         System.out.println(addDebugSpacer(3) + "EL attrVal= "
                                 + elText);
                     }
 
                     elContext = elExpression.getDocumentContext();
                     // EL validation is user configurable because
                     // it can be computationally costly.
                     if (_validationContext.shouldValidateEL())
                     {
                         // also, skip the validation if the expression is empty
                         // or only whitespace, since the parser doesn't handle it
                         // anyway.
                         if ("".equals(elText.trim()))
                         {
                             final int offset = elContext.getDocumentPosition()-1;
                             final int length = elText.length()+2;
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
                                             IValidELValues.class, elContext,
                                             attrAdapter.getAttributeIdentifier());
                             validateELExpression(context, elContext, elVals,
                                     attrAdapter.getValue(), elText);
                             isEL = true;
                         }
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
                 ITextRegionList regionList = parentRegion.getRegions();
 
                 if (regionList.size() >= 3)
                 {
                     final ITextRegion openQuote = regionList.get(0);
                     final ITextRegion vblOpen = regionList.get(1);
 
                     if ((openQuote.getType() == DOMJSPRegionContexts.XML_TAG_ATTRIBUTE_VALUE_DQUOTE || openQuote
                             .getType() == DOMJSPRegionContexts.JSP_VBL_DQUOTE)
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
             final String attributeVal, final String elText)
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
             for (final Iterator it = elVals.iterator(); it.hasNext();)
             {
                 final IValidELValues elval = (IValidELValues) it.next();
                 CompositeType expectedType;
                 Diagnostic status = null;
                 try
                 {
                     expectedType = elval.getExpectedRuntimeType();
 
                     if (expectedType != null)
                     {
                         status = TypeComparator.calculateTypeCompatibility(
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
             System.out.println(addDebugSpacer(3) + "attrVal= "
                     + (attributeValue != null ? attributeValue : "null"));
         }
 
         final AttributeIdentifier attributeId = attrAdapter
                 .getAttributeIdentifier();
 
         if (attributeId.getTagIdentifier() == null
                 || attributeId.getTagIdentifier().getTagName() == null
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
                         System.out.println(addDebugSpacer(4) + "NOT VALID ");
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
                     System.out.println(addDebugSpacer(5) + "VALID ");
                 }
             }
         }
         else if (DEBUG)
         {
             System.out.println(addDebugSpacer(4) + "NO META DATA ");
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
         final Diagnostic diagnostic = new BasicDiagnostic(severity, "", -1,
                 msg, null);
         return diagnostic;
     }
 
     private String addDebugSpacer(final int count)
     {
         final String TAB = "\t";
         final StringBuffer ret = new StringBuffer("");
         for (int i = 0; i <= count; i++)
         {
             ret.append(TAB);
         }
         return ret.toString();
     }
 
 }
