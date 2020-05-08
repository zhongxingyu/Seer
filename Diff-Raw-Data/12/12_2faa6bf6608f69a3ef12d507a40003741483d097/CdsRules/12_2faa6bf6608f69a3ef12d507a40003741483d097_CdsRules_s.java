 // Copyright (C),2005-2007 HandCoded Software Ltd.
 // All rights reserved.
 //
 // This software is licensed in accordance with the terms of the 'Open Source
 // License (OSL) Version 3.0'. Please see 'license.txt' for the details.
 //
 // HANDCODED SOFTWARE LTD MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 // SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT
 // LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 // PARTICULAR PURPOSE, OR NON-INFRINGEMENT. HANDCODED SOFTWARE LTD SHALL NOT BE
 // LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 // OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 
 package com.handcoded.fpml.validation;
 
 import java.math.BigDecimal;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.handcoded.finance.Date;
 import com.handcoded.finance.Interval;
 import com.handcoded.finance.Period;
 import com.handcoded.validation.Rule;
 import com.handcoded.validation.RuleSet;
 import com.handcoded.validation.ValidationErrorHandler;
 import com.handcoded.xml.DOM;
 import com.handcoded.xml.Logic;
 import com.handcoded.xml.NodeIndex;
 import com.handcoded.xml.XPath;
 
 /**
  * The <CODE>CdsRules</CODE> class contains a <CODE>RuleSet</CODE>
  * initialised with FpML defined validation rules for credit derivative
  * products.
  *
  * @author	BitWise
  * @version	$Id$
  * @since	TFP 1.0
  */
 public final class CdsRules extends Logic
 {
 	/**
 	 * A <CODE>Rule</CODE> that ensures <CODE>tradeHeader/tradeDate</CODE> is before
 	 * <CODE>creditDefaultSwap/generalTerms/effectiveDate/unadjustedDate</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE01
 		= new Rule (Preconditions.R4_0__LATER, "cd-1")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		creditDefaultSwap
 						= DOM.getElementByLocalName (context, "creditDefaultSwap");
 						
 					if ((creditDefaultSwap == null) || !isSingleName (creditDefaultSwap)) continue;
 					
 					try {
 						Date tradeDate		= Date.parse (DOM.getInnerText (DOM.getElementByLocalName (context, "tradeHeader", "tradeDate")));
 						Date effectiveDate	= Date.parse (DOM.getInnerText (DOM.getElementByLocalName (creditDefaultSwap, "generalTerms", "effectiveDate", "unadjustedDate")));
 					
 						if (tradeDate.compareTo (effectiveDate) < 0) continue;
 
 						errorHandler.error ("305", context,
 								"The tradeHeader/tradeDate must be before creditDefaultSwap/generalTerms/effectiveDate/unadjustedDate",
 								getName (), null);
 						result = false;
 					}
 					catch (IllegalArgumentException error) {
 						// Syntax errors handled elsewhere
 					}
 				}
 				return (result);
 			}
 		};
 		
 		/**
 		 * A <CODE>Rule</CODE> that ensures <CODE>tradeHeader/tradeDate</CODE> is before
 		 * <CODE>creditDefaultSwap/generalTerms/effectiveDate/unadjustedDate</CODE>.
 		 * <P>
 		 * Applies to FpML 4.0 and later.
 		 * @since	TFP 1.0
 		 */
 		public static final Rule	RULE01B
 			= new Rule (Preconditions.R4_0__LATER, "cd-1b")
 			{
 				/**
 				 * {@inheritDoc}
 				 */
 				public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 				{
 					boolean		result 	= true;
 					NodeList	list 	= nodeIndex.getElementsByName ("trade");
 					
 					for (int index = 0; index < list.getLength (); ++index) {
 						Element		context = (Element) list.item (index);
 						
 						Element		creditDefaultSwap
 							= DOM.getElementByLocalName (context, "creditDefaultSwap");
 							
 						if ((creditDefaultSwap == null) || !isCreditIndex (creditDefaultSwap)) continue;
 						
 						try {
 							Date tradeDate		= Date.parse (DOM.getInnerText (DOM.getElementByLocalName (context, "tradeHeader", "tradeDate")));
 							Date effectiveDate	= Date.parse (DOM.getInnerText (DOM.getElementByLocalName (creditDefaultSwap, "generalTerms", "effectiveDate", "unadjustedDate")));
 						
							if (tradeDate.compareTo (effectiveDate) >= 0) {
								errorHandler.error ("305", context,
										"The tradeHeader/tradeDate must not be before creditDefaultSwap/generalTerms/effectiveDate/unadjustedDate",
										getName (), null);
								result = false;
							}
 						}
 						catch (IllegalArgumentException error) {
 							// Syntax errors handled elsewhere
 						}
 					}
 					return (result);
 				}
 			};
 			
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>calculationAgent</CODE> is present,
 	 * it may contain only <CODE>calculationAgentPartyReference</CODE> elements.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE02 
 		= new Rule (Preconditions.R4_0__LATER, "cd-2")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		creditDefaultSwap
 						= DOM.getElementByLocalName (context, "creditDefaultSwap");
 						
 					if (creditDefaultSwap == null) continue;
 					
 					Element		calculationAgent
 						= DOM.getElementByLocalName (context, "calculationAgent");
 						
 					if (calculationAgent != null) {
 						boolean		failed = false;
 						
 						for (Node node = calculationAgent.getFirstChild (); node != null; node = node.getNextSibling ()) {
 							if (node.getNodeType () == Node.ELEMENT_NODE) {
 								if (node.getLocalName ().equals ("calculationAgentPartyReference"))
 									continue;
 								
 								if (node.getLocalName ().equals ("calculationAgentParty") &&
 									DOM.getInnerText ((Element) node).trim ().equals ("AsSpecifiedInMasterAgreement"))
 									continue;
 							
 								failed = true;
 							}
 						}
 						
 						if (failed) {
 							errorHandler.error ("305", context,
 									"The calculationAgent element may only contain calculationAgentPartyReferences " +
 									"or a calculationAgentParty with the value 'AsSpecifiedInMasterAgreement",
 									getName (), null);
 							result = false;
 						}
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures contracts referencing ISDA 1999 definitions
 	 * do not use ISDA 2003 supplements.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE03
 		= new Rule (Preconditions.R4_0__LATER, "cd-3")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		creditDefaultSwap
 						= DOM.getElementByLocalName (context, "creditDefaultSwap");
 						
 					if (creditDefaultSwap == null) continue;
 					
 					if (!isIsda1999 (context)) continue;
 					
 					Element		supplement
 						= DOM.getElementByLocalName (context, "documentation", "contractualSupplement");
 	
 					if (supplement != null)
 						if (DOM.getInnerText (supplement).startsWith ("ISDA2003Credit")) {
 							errorHandler.error ("305", context,
 								"The contractualSupplement name may not begin with ISDA2003Credit",
 								getName (), DOM.getInnerText (supplement));
 							result = false;
 						}			
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures contracts referencing ISDA 2003 definitions
 	 * do not use ISDA 1999 supplements.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE04
 		= new Rule (Preconditions.R4_0__LATER, "cd-4")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		creditDefaultSwap
 						= DOM.getElementByLocalName (context, "creditDefaultSwap");
 						
 					if (creditDefaultSwap == null) continue;
 					
 					if (!isIsda2003 (context)) continue;
 					
 					Element		supplement
 						= DOM.getElementByLocalName (context, "documentation", "contractualSupplement");
 	
 					if (supplement != null)
 						if (DOM.getInnerText (supplement).startsWith ("ISDA1999Credit")) {
 							errorHandler.error ("305", context,
 								"The contractualSupplement name may not begin with ISDA1999Credit",
 								getName (), DOM.getInnerText (supplement));
 							result = false;
 						}			
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>scheduledTerminationDate/adjustableDate</CODE>
 	 * exists, then <CODE>effectiveDate/unadjustedDate</CODE> &lt;
 	 * <CODE>scheduledTerminationDate/adjustableDate/unadjustedDate</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE05 
 		= new Rule (Preconditions.R4_0__LATER, "cd-5")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("generalTerms");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		termination
 						= DOM.getElementByLocalName (context, "scheduledTerminationDate", "adjustableDate");
 						
 					if (termination != null) {
 						try {
 							Date effective	= Date.parse (DOM.getInnerText (DOM.getElementByLocalName (context, "effectiveDate", "unadjustedDate")));
 							Date adjustable	= Date.parse (DOM.getInnerText (DOM.getElementByLocalName (termination, "unadjustedDate")));
 								
 							if (effective.compareTo (adjustable) >= 0) {
 								errorHandler.error ("305", context,
 										"The scheduled termination date must be later than the effective date",
 										getName (), null);
 								result = false;
 							}
 						}
 						catch (IllegalArgumentException error) {
 							// Syntax errors handled elsewhere.
 						}
 					}	
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures <CODE>buyerPartyReference/@href</CODE> and
 	 * <CODE>sellerPartyReference/@href</CODE> are different.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE06
 		= new Rule (Preconditions.R4_0__LATER, "cd-6")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("generalTerms");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		buyer
 						= DOM.getElementByLocalName (context, "buyerPartyReference");
 					
 					Element		seller
 						= DOM.getElementByLocalName (context, "sellerPartyReference");
 						
 					if (buyer.getAttribute ("href").equals (seller.getAttribute ("href"))) {
 						errorHandler.error ("305", context,
 							"The buyer and seller party references must be different",
 							getName (), null);
 						result = false;
 					}
 					
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures long form contracts contain effective
 	 * date adjustments.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE07
 		= new Rule (Preconditions.R4_0__LATER, "cd-7")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("generalTerms");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (!isLongForm (DOM.getGrandParent (context)))
 						continue;
 						
 					Element		effective
 						= DOM.getElementByLocalName (context, "effectiveDate");
 					Element		def
 						= DOM.getElementByLocalName (effective, "dateAdjustments");
 					Element		ref
 						= DOM.getElementByLocalName (effective, "dateAdjustmentsReference");
 						
 					if (((def == null) && (ref == null)) || ((def != null) && (ref != null))) {
 						errorHandler.error ("305", context,
 							"A date adjustment for the effective date must either be specified or referenced",
 							getName (), null);
 						result = false;
 					}				
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures long form contracts contain termination
 	 * date adjustments.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE08 
 		= new Rule (Preconditions.R4_0__LATER, "cd-8")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("generalTerms");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (!isLongForm (DOM.getGrandParent (context)))
 						continue;
 						
 					Element		adjustable
 						= DOM.getElementByLocalName (context, "scheduledTerminationDate", "adjustableDate");
 					Element		def
 						= DOM.getElementByLocalName (adjustable, "dateAdjustments");
 					Element		ref
 						= DOM.getElementByLocalName (adjustable, "dateAdjustmentsReference");
 						
 					if (((def == null) && (ref == null)) || ((def != null) && (ref != null))) {
 						errorHandler.error ("305", context,
 							"A date adjustment for the scheduled termination date must either be specified or referenced",
 							getName (), null);
 						result = false;
 					}					
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>referenceObligation/primaryObligorReference</CODE>
 	 * exists, then it must reference the <CODE>referenceEntity</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE09
 		= new Rule (Preconditions.R4_0__LATER, "cd-9")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("referenceInformation");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					NodeList obligations = context.getElementsByTagName ("referenceObligation");
 					for (int count = 0; count < obligations.getLength (); ++count) {
 						Element		temp = (Element) obligations.item (count);
 						Element		ref  = DOM.getElementByLocalName (temp, "primaryObligorReference");
 						
 						if (ref != null) {
 							Element		entity = DOM.getElementByLocalName (context, "referenceEntity");
 							
 							if (!ref.getAttribute ("href").equals (entity.getAttribute ("id"))) {
 								errorHandler.error ("305", context,
 									"The primaryObligorReference must refer to the referenceEntity",
 									getName (), ref.getAttribute ("href"));
 								result = false;
 							}
 						}
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>referenceObligation/guarantorReference</CODE>
 	 * exists, then it must reference the <CODE>referenceEntity</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE10 
 		= new Rule (Preconditions.R4_0__LATER, "cd-10")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("referenceInformation");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					NodeList obligations = context.getElementsByTagName ("referenceObligation");
 					for (int count = 0; count < obligations.getLength (); ++count) {
 						Element		temp = (Element) obligations.item (count);
 						Element		ref  = DOM.getElementByLocalName (temp, "guarantorReference");
 						
 						if (ref != null) {
 							Element		entity = DOM.getElementByLocalName (context, "referenceEntity");
 							
 							if (!ref.getAttribute ("href").equals (entity.getAttribute ("id"))) {
 								errorHandler.error ("305", context,
 									"The primaryObligorReference must refer to the referenceEntity",
 									getName (), ref.getAttribute ("href"));
 								result = false;
 							}
 						}
 					}					
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures ISDA 2003 long form contracts contain
 	 * <CODE>allGuarantees</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE11 
 		= new Rule (Preconditions.R4_0__LATER, "cd-11")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("referenceInformation");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		trade 	= DOM.getGreatGrandParent (context);
 					
 					if (!isLongForm (trade) || !isIsda2003 (trade))
 						continue;
 						
 					Element 	guarantees = DOM.getElementByLocalName (context, "allGuarantees");
 					
 					if (guarantees == null) {
 						errorHandler.error ("305", context,
 							"The allGuarantees element must be present",
 							getName (), null);
 						result = false;
 					}	
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>referencePrice</CODE> is present
 	 * then its is not negative.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE12 
 		= new Rule (Preconditions.R4_0__LATER, "cd-12")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("referenceInformation");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		price = DOM.getElementByLocalName (context, "referencePrice");
 					
 					if (price != null) {
 						String		priceValue = DOM.getInnerText (price);
 						
 						if (Double.parseDouble (priceValue) < 0.0) {
 							errorHandler.error ("305", context,
 								"The reference price must be greater than or equal to zero",
 								getName (), priceValue);
 							result = false;
 						}
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>protectionTerms/creditEvents/creditEventNotice/notifyingParty/buyerPartyReference</CODE>
 	 * is present, its @href attribute must match that of <CODE>generalTerms/buyerPartyReference</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE13 
 		= new Rule (Preconditions.R4_0__LATER, "cd-13")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					NodeList	buyers	= XPath.paths (context, "protectionTerms", "creditEvents", "creditEventNotice", "notifyingParty", "buyerPartyReference");
 					for (int count = 0; count < buyers.getLength (); ++count) {
 						Element		buyer = (Element) buyers.item (count);
 						String		buyerName;
 						String		referenceName;
 						
 						if (equal (
 								buyerName = buyer.getAttribute ("href"),
 								referenceName = XPath.path (context, "generalTerms", "buyerPartyReference").getAttribute ("href")))
 							continue;
 						
 						errorHandler.error ("305", context,
 							"Credit event notice references buyer party reference " + buyerName +
 							" but general terms references " + referenceName,
 							getName (), null);
 
 						result = false;
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>protectionTerms/creditEvents/creditEventNotice/notifyingParty/sellerPartyReference</CODE>
 	 * is present, its @href attribute must match that of <CODE>generalTerms/sellerPartyReference</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE14 
 		= new Rule (Preconditions.R4_0__LATER, "cd-14")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					NodeList	sellers =  XPath.paths (context, "protectionTerms", "creditEvents", "creditEventNotice", "notifyingParty", "sellerPartyReference");
 					for (int count = 0; count < sellers.getLength (); ++count) {
 						Element		seller = (Element) sellers.item (count);
 						if (equal (seller.getAttribute ("href"), 
 								XPath.path (context, "generalTerms", "sellerPartyReference").getAttribute ("href")))
 							continue;
 
 						errorHandler.error ("305", context,
 							"If protectionTerms/creditEvents/creditEventNotice/notifyingParty/sellerPartyReference " +
 							"is present, its @href attribute must match that of generalTerms/sellerPartyReference",
 							getName (), null);
 
 						result = false;
 					}
 				}			
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the valuation method is valid when
 	 * there is one obligation and one valuation date.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE15 
 		= new Rule (Preconditions.R4_0__LATER, "cd-15")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (implies (
 							and (
 								equal (
 									count (XPath.paths (context, "generalTerms", "referenceInformation", "referenceObligation")), 1),
 								exists (XPath.path (context, "cashSettlementTerms", "valuationDate", "singleValuationDate"))),
 							or (
 								equal (
 									XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 									"Market"),
 								equal (
 									XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 									"Highest"))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"If there is exactly one generalTerms/referenceInformation/referenceObligation " +
 						"and cashSettlementTerms/valuationDate/singleValuationDate occurs " +
 						"then the value of cashSettlementTerms/valuationMethod must be " +
 						"Market or Highest",
 						getName (), DOM.getInnerText (XPath.path (context, "cashSettlementTerms", "valuationMethod")));
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the valuation method is valid when
 	 * there is one obligation and multiple valuation dates.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE16 
 		= new Rule (Preconditions.R4_0__LATER, "cd-16")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (implies (
 							and (
 								equal (
 									count (XPath.paths (context, "generalTerms", "referenceInformation", "referenceObligation")), 1),
 								exists (XPath.path (context, "cashSettlementTerms", "valuationDate", "multipleValuationDates"))),
 							or (
 								equal (
 									XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 									"AverageMarket"),
 								or (
 									equal (
 										XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 										"Highest"),
 									equal (
 										XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 										"AverageHighest")))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"If there is exactly one generalTerms/referenceInformation/referenceObligation " +
 						"and cashSettlementTerms/valuationDate/multipleValuationDates occurs " +
 						"then the value of cashSettlementTerms/valuationMethod must be " +
 						"AverageMarket, Highest or AverageHighest",
 						getName (), DOM.getInnerText (XPath.path (context, "cashSettlementTerms", "valuationMethod")));
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the valuation method is valid when
 	 * there are multiple obligations and one valuation date.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE17 
 		= new Rule (Preconditions.R4_0__LATER, "cd-17")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (implies (
 							and (
 								greater (
 									count (XPath.paths (context, "generalTerms", "referenceInformation", "referenceObligation")), 1),
 								exists (XPath.path (context, "cashSettlementTerms", "valuationDate", "singleValuationDate"))),
 							or (
 								equal (
 									XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 									"BlendedMarket"),
 								equal (
 									XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 									"BlendedHighest"))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"If there are more that one generalTerms/referenceInformation/referenceObligation " +
 						"and cashSettlementTerms/valuationDate/singleValuationDate occurs " +
 						"then the value of cashSettlementTerms/valuationMethod must be " +
 						"BlendedMarket or BlendedHighest",
 						getName (), DOM.getInnerText (XPath.path (context, "cashSettlementTerms", "valuationMethod")));
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the valuation method is valid when
 	 * there are multiple obligations and multiple valuation dates.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE18 
 		= new Rule (Preconditions.R4_0__LATER, "cd-18")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (implies (
 							and (
 								greater (
 									count (XPath.paths (context, "generalTerms", "referenceInformation", "referenceObligation")), 1),
 								exists (XPath.path (context, "cashSettlementTerms", "valuationDate", "multipleValuationDates"))),
 							or (
 								equal (
 									XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 									"AverageBlendedMarket"),
 								equal (
 									XPath.path (context, "cashSettlementTerms", "valuationMethod"),
 									"AverageBlendedHighest"))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"If there are more than one generalTerms/referenceInformation/referenceObligation " +
 						"and cashSettlementTerms/valuationDate/multipleValuationDates occurs " +
 						"then the value of cashSettlementTerms/valuationMethod must be " +
 						"AverageBlendedMarket or AverageBlendedHighest",
 						getName (), DOM.getInnerText (XPath.path (context, "cashSettlementTerms", "valuationMethod")));
 
 					result = false;	
 				}				
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures elements related to ISDA 2003 contracts
 	 * are not present in ISDA 1999 contracts.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE19 
 		= new Rule (Preconditions.R4_0__LATER, "cd-19")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		trade = (Element) list.item (index);
 					
 					if (isIsda1999 (trade)) {
 						Element	context = XPath.path (trade, "creditDefaultSwap");
 						
 						result &=
 							  validate (context, XPath.path (context, "protectionTerms", "creditEvents", "creditEventNotice", "businessCenter"), errorHandler)
 							& validate (context, XPath.path (context, "protectionTerms", "creditEvents", "restructuring", "multipleHolderObligation"), errorHandler)
 							& validate (context, XPath.path (context, "protectionTerms", "creditEvents", "restructuring", "multiplCreditEventNotices"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "referenceInformation", "allGuarantees"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "indexReferenceInformation"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "substitution"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "modifiedEquityDelivery"), errorHandler);
 					}
 				}
 				return (result);
 			}
 			
 			private boolean validate (final Element context, final Element illegal, ValidationErrorHandler errorHandler)
 			{
 				if (illegal != null) {
 					errorHandler.error ("305", context,
 						"Illegal element found in ISDA 1999 credit default swap",
 						getName (), XPath.forNode (illegal));
 
 					return (false);
 				}
 				return (true);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures elements related to ISDA 1999 contracts
 	 * are not present in ISDA 2003 contracts.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE20 
 		= new Rule (Preconditions.R4_0__LATER, "cd-20")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 					boolean		result 	= true;
 					NodeList	list 	= nodeIndex.getElementsByName ("trade");
 					
 					for (int index = 0; index < list.getLength (); ++index) {
 						Element		trade = (Element) list.item (index);
 						
 						if (isIsda2003 (trade)) {
 							Element	context = XPath.path (trade, "creditDefaultSwap");
 							
 							if (!exists (XPath.path (context, "protectionTerms", "obligations", "notContingent")))
 								continue;
 
 							errorHandler.error ("305", context,
 								"Illegal element found in ISDA 2003 credit default swap",
 								getName (), "notContingent");
 
 							result = false;
 						}
 					}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures a short form contract does not
 	 * contain invalid elements.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE21 
 		= new Rule (Preconditions.R4_0__LATER, "cd-21")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		trade = (Element) list.item (index);
 					
 					if (isShortForm (trade)) {
 						Element context = XPath.path (trade, "creditDefaultSwap");
 						
 						if (!isSingleName (context)) continue;
 						
 						result &=
 							  validate (context, XPath.path (context, "cashSettlementTerms"), errorHandler)
 							& validate (context, XPath.path (context, "physicalSettlementTerms"), errorHandler)
 							& validate (context, XPath.path (context, "feeLeg", "periodicPayment", "fixedAmountCalculation", "calculationAmount"), errorHandler)
 							& validate (context, XPath.path (context, "feeLeg", "periodicPayment", "fixedAmountCalculation", "dayCountFraction"), errorHandler)
 							& validate (context, XPath.path (context, "protectionTerms", "obligations"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "referenceInformation", "allGuarantees"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "referenceInformation", "referencePrice"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "effectiveDate", "dateAdjustments"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "effectiveDate", "dateAdjustmentsReference"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "scheduledTerminationDate", "adjustableDate", "dateAdjustments"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "scheduledTerminationDate", "adjustableDate", "dateAdjustmentsReference"), errorHandler)
 							& validate (context, XPath.path (context, "generalTerms", "dateAdjustments"), errorHandler);	
 					}
 				}
 				return (result);
 			}
 			
 			private boolean validate (final Element context, final Element illegal, ValidationErrorHandler errorHandler)
 			{
 				if (illegal != null) {
 					errorHandler.error ("305", context,
 						"Illegal element found in short form credit default swap",
 						getName (), XPath.forNode (illegal));
 
 					return (false);
 				}
 				return (true);
 			}			
 		};
 		
 		/**
 		 * A <CODE>Rule</CODE> that ensures a short form contract does not
 		 * contain invalid elements.
 		 * <P>
 		 * Applies to FpML 4.0 and later.
 		 * @since	TFP 1.0
 		 */
 		public static final Rule	RULE21B 
 			= new Rule (Preconditions.R4_0__LATER, "cd-21b")
 			{
 				/**
 				 * {@inheritDoc}
 				 */
 				public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 				{
 					boolean		result 	= true;
 					NodeList	list 	= nodeIndex.getElementsByName ("trade");
 					
 					for (int index = 0; index < list.getLength (); ++index) {
 						Element		trade = (Element) list.item (index);
 						
 						if (isShortForm (trade)) {
 							Element context = XPath.path (trade, "creditDefaultSwap");
 							
 							if (!isCreditIndex (context)) continue;
 							
 							result &=
 								  validate (context, XPath.path (context, "cashSettlementTerms"), errorHandler)
 								& validate (context, XPath.path (context, "physicalSettlementTerms"), errorHandler)
 								& validate (context, XPath.path (context, "feeLeg", "periodicPayment", "fixedAmountCalculation", "calculationAmount"), errorHandler)
 								& validate (context, XPath.path (context, "feeLeg", "periodicPayment", "fixedAmountCalculation", "dayCountFraction"), errorHandler)
 								& validate (context, XPath.path (context, "protectionTerms", "obligations"), errorHandler)
 								& validate (context, XPath.path (context, "protectionTerms", "creditEvents"), errorHandler)
 								& validate (context, XPath.path (context, "generalTerms", "effectiveDate", "dateAdjustments"), errorHandler)
 								& validate (context, XPath.path (context, "generalTerms", "effectiveDate", "dateAdjustmentsReference"), errorHandler)
 								& validate (context, XPath.path (context, "generalTerms", "scheduledTerminationDate", "adjustableDate", "dateAdjustments"), errorHandler)
 								& validate (context, XPath.path (context, "generalTerms", "scheduledTerminationDate", "adjustableDate", "dateAdjustmentsReference"), errorHandler)
 								& validate (context, XPath.path (context, "generalTerms", "dateAdjustments"), errorHandler);	
 						}
 					}
 					return (result);
 				}
 				
 				private boolean validate (final Element context, final Element illegal, ValidationErrorHandler errorHandler)
 				{
 					if (illegal != null) {
 						errorHandler.error ("305", context,
 							"Illegal element found in short form credit default swap",
 							getName (), XPath.forNode (illegal));
 
 						return (false);
 					}
 					return (true);
 				}			
 			};
 			
 	/**
 	 * A <CODE>Rule</CODE> that ensures short form contracts can only contain
 	 * restructuring events.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE22 
 		= new Rule (Preconditions.R4_0__LATER, "cd-22")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		trade = (Element) list.item (index);
 					
 					if (isShortForm (trade)) {
 						Element	context = XPath.path (trade, "creditDefaultSwap");
 						Element	events	= XPath.path (context, "protectionTerms", "creditEvents");
 						
 						if (events != null) {
 							NodeList	children = events.getChildNodes ();
 							for (int count = 0; count < children.getLength (); ++count) {
 								Node	node = children.item (count);
 								if (node instanceof Element) {
 									String name = node.getLocalName();
 									
 									if (name.equals ("bankrupcy") ||
 										name.equals ("failureToPay") ||
 										name.equals ("repudiationMoratorium") ||
 										name.equals ("obligationDefault") ||
 										name.equals ("obligationAcceleration")) {
 										errorHandler.error ("305", context,
 											"Illegal element found in short form credit default swap",
 											getName (), XPath.forNode (node));
 
 											result = false;
 									}										
 								}
 							}
 						}
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures long form contracts specify the
 	 * settlement terms.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE23 
 		= new Rule (Preconditions.R4_0__LATER, "cd-23")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		trade = (Element) list.item (index);
 					
 					if (isLongForm (trade)) {
 						Element	context = XPath.path (trade, "creditDefaultSwap");
 
 						if (or (
 							exists (XPath.path (context, "cashSettlementTerms")),
 							exists (XPath.path (context, "physicalSettlementTerms"))))
 							continue;
 
 						errorHandler.error ("305", context,
 							"Neither cash nor physical settlement terms are present",
 							getName (), null);
 
 						result = false;
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures long form contracts contain mandatory
 	 * data values.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE24 
 		= new Rule (Preconditions.R4_0__LATER, "cd-24")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		trade = (Element) list.item (index);
 					
 					if (isLongForm (trade)) {
 						Element	context = XPath.path (trade, "creditDefaultSwap");
 
 						if (!exists (XPath.path (context, "protectionTerms", "creditEvents", "creditEventNotice"))) {
 							errorHandler.error ("305", context,
 								"Long Form credit default swap is missing a mandatory element",
 								getName (), "protectionEvents/creditEvents/creditEventNotices");
 
 							result = false;
 						}
 
 						if (!exists (XPath.path (context, "protectionTerms", "obligations"))) {
 							errorHandler.error ("305", context,
 								"Long Form credit default swap is missing a mandatory element",
 								getName (), "protectionTerms/obligations");
 
 							result = false;
 						}
 
 						if (!exists (XPath.path (context, "generalTerms", "referenceInformation", "referencePrice"))) {
 							errorHandler.error ("305", context,
 								"Long Form credit default swap is missing a mandatory element",
 								getName (), "generalTerms/referenceInformation/referencePrice");
 
 							result = false;
 						}
 					}
 				}			
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures long form contracts with physical
 	 * settlement contain the necessary data.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE25 
 		= new Rule (Preconditions.R4_0__LATER, "cd-25")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		trade = (Element) list.item (index);
 					
 					if (isLongForm (trade)) {
 						Element	context = XPath.path (trade, "creditDefaultSwap");
 
 						if (exists (XPath.path (context, "physicalSettlementTerms"))) {
 							if (!exists (XPath.path (context, "physicalSettlementTerms", "settlementCurrency"))) {
 								errorHandler.error ("305", context,
 									"A mandatory element for physical settlement is missing",
 									getName (), "physicalSettlementTerms/settlementCurrency");
 
 								result = false;
 							}
 
 							if (!exists (XPath.path (context, "physicalSettlementTerms", "physicalSettlementPeriod"))) {
 								errorHandler.error ("305", context,
 									"A mandatory element for physical settlement is missing",
 									getName (), "physicalSettlementTerms/physicalSettlementPeriod");
 
 								result = false;
 							}
 
 							if (!exists (XPath.path (context, "physicalSettlementTerms", "escrow"))) {
 								errorHandler.error ("305", context,
 									"A mandatory element for physical settlement is missing",
 									getName (), "physicalSettlementTerms/escrow");
 
 								result = false;
 							}
 
 							if (!exists (XPath.path (context, "physicalSettlementTerms", "deliverableObligations", "accruedInterest"))) {
 								errorHandler.error ("305", context,
 									"A mandatory element for physical settlement is missing",
 									getName (), "physicalSettlementTerms/deliverableObligations/accruedInterest");
 
 								result = false;
 							}
 						}
 					}
 				}	
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>feeLeg/singlePayment/adjustablePaymentDate</CODE>
 	 * is present then <CODE>feeLeg/singlePayment/adjustablePaymentDate</CODE> &gt;
 	 * <CODE>generalTerms/effectiveDate/unadjustedDate</CODE>.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE26 
 		= new Rule (Preconditions.R4_0__LATER, "cd-26")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		paymentDate;
 					Element		effectiveDate;
 
 					if (implies (
 							exists (XPath.path (context, "feeLeg", "singlePayment", "adjustablePaymentDate")),
 							greater (
 								paymentDate   = XPath.path (context, "feeLeg", "singlePayment", "adjustablePaymentDate"),
 								effectiveDate = XPath.path (context, "generalTerms", "effectiveDate", "unadjustedDate"))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"Single payment date " + DOM.getInnerText (paymentDate) + " must be " +
 						"after the effective date " + DOM.getInnerText (effectiveDate),
 						getName (), null);
 
 					result = false;
 				}				
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if a payment date is defined it falls
 	 * before the termination date.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE27 
 		= new Rule (Preconditions.R4_0__LATER, "cd-27")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		feeDate;
 					Element		termDate;
 
 					if (and (
 						exists (feeDate = XPath.path (context, "feeLeg", "singlePayment", "adjustablePaymentDate")),
 						exists (termDate = XPath.path (context, "generalTerms", "scheduledTerminationDate", "adjustableDate")))) {
 						if (less (feeDate, termDate)) continue;
 
 						errorHandler.error ("305", context,
 							"Single payment date '" + DOM.getInnerText (feeDate) + "' must be " +
 							"before scheduled termination date '" + DOM.getInnerText(termDate) + "'",
 							getName (), null);
 
 						result = false;
 					}		
 				}	
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if a first payment date is present it
 	 * falls after the effective date.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE28 
 		= new Rule (Preconditions.R4_0__LATER, "cd-28")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);	
 					Element		paymentDate;
 					Element		effectiveDate;
 
 					if (implies (
 						exists (XPath.path (context, "feeLeg", "periodicPayment", "firstPaymentDate")),
 						greater (
 							paymentDate = XPath.path (context, "feeLeg", "periodicPayment", "firstPaymentDate"),
 							effectiveDate = XPath.path (context, "generalTerms", "effectiveDate", "unadjustedDate"))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"First periodic payment date '" + DOM.getInnerText (paymentDate) + "' " +
 						"must be after the effective date '" + DOM.getInnerText (effectiveDate) + "'",
 						getName (), null);
 
 					result = false;
 				}
 				
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if a first payment date is present it
 	 * falls before the termination date.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE29 
 		= new Rule (Preconditions.R4_0__LATER, "cd-29")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		paymentDate;
 					Element		terminationDate;
 
 					if (and (
 						exists (paymentDate = XPath.path (context, "feeLeg", "periodicPayment", "firstPaymentDate")),
 						exists (terminationDate = XPath.path (context, "generalTerms", "scheduledTerminationDate", "adjustableDate", "unadjustedDate")))) {
 						if (less (paymentDate, terminationDate)) continue;
 
 						errorHandler.error ("305", context,
 							"First periodic payment date '" + DOM.getInnerText (paymentDate) + "' " +
 							"must be before the termination date '" + DOM.getInnerText (terminationDate) + "'",
 							getName (), null);
 	
 						result = false;
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if a last regular payment date is present
 	 * it falls before the termination date.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE30 
 		= new Rule (Preconditions.R4_0__LATER, "cd-30")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		paymentDate;
 					Element		terminationDate;
 
 					if (and (
 						exists (paymentDate = XPath.path (context, "feeLeg", "periodicPayment", "lastRegularPaymentDate")),
 						exists (terminationDate = XPath.path (context, "generalTerms", "scheduledTerminationDate", "adjustableDate", "unadjustedDate")))) {
 						if (less (paymentDate, terminationDate)) continue;
 
 						errorHandler.error ("305", context,
 							"Last regular periodic payment date '" + DOM.getInnerText (paymentDate) + "' " +
 							"must be before the termination date '" + DOM.getInnerText (terminationDate) + "'",
 							getName (), null);
 
 						result = false;
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the first payment date falls before the
 	 * last regular payment date.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE31 
 		= new Rule (Preconditions.R4_0__LATER, "cd-31")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("periodicPayment");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		firstDate;
 					Element		lastDate;
 
 					if (implies (
 							and (
 								exists (firstDate = XPath.path (context, "firstPaymentDate")),
 								exists (lastDate  = XPath.path (context, "lastRegularPaymentDate"))),
 							less (firstDate, lastDate)))
 						continue;
 
 					errorHandler.error ("305", context,
 						"First payment date '" + DOM.getInnerText (firstDate) + "' must be before " +
 						"last payment date '" + DOM.getInnerText (lastDate) + "'",
 						getName (), null);
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if a long form contracts defines a feeLeg
 	 * then it must contain a calculationAmount and dayCountFraction.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE32 
 		= new Rule (Preconditions.R4_0__LATER, "cd-32")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("trade");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		trade = (Element) list.item (index);
 					
 					if (isLongForm (trade)) {
 						Element	context = XPath.path (trade, "creditDefaultSwap", "feeLeg", "periodicPayment");
 
 						if (context == null) continue;
 
 						if (!exists (XPath.path (context, "fixedAmountCalculation", "calculationAmount"))) {
 							errorHandler.error ("305", context,
 								"Calculation amount must be present in the fixed amount " +
 								"calculation of periodic payment",
 								getName (), null);
 
 							result = false;
 						}
 
 						if (!exists (XPath.path (context, "fixedAmountCalculation", "dayCountFraction"))) {
 							errorHandler.error ("305", context,
 								"Day count fraction must be present in the fixed amount " +
 								"calculation of periodic payment",
 								getName (), null);
 
 							result = false;
 						}
 					}
 				}				
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the interval between the first and last payment
 	 * dates is a multiple of the paymentFrequency.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE33 
 		= new Rule (Preconditions.R4_0__LATER, "cd-33")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("periodicPayment");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		firstDate	= XPath.path (context, "firstPaymentDate");
 					Element		lastDate	= XPath.path (context, "lastRegularPaymentDate");
 
 					if ((firstDate == null) || (lastDate == null)) continue;
 
 					Interval	interval	= interval (XPath.path (context, "paymentFrequency"));
 
 					if (interval.dividesDates (Date.parse (DOM.getInnerText (firstDate)), Date.parse (DOM.getInnerText (lastDate))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"Last regular payment date '" + DOM.getInnerText (lastDate) + "' is not " +
 						"an integer multiple of the payment period after the first payment " +
 						" date '" + DOM.getInnerText (firstDate) + "'",
 						getName (), null);
 
 					result = false;
 				}
 				
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the only the allowed reference obligations
 	 * are defined.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE34 
 		= new Rule (Preconditions.R4_0__LATER, "cd-34")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("deliverableObligations");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (equal (XPath.path (context, "category"), "ReferenceObligationsOnly")) {
 						NodeList	children = context.getChildNodes ();
 						for (int count = 0; count < children.getLength(); ++count) {
 							Node node	= children.item (count);
 							if ((node instanceof Element) && !node.getLocalName ().equals ("category")) {
 								errorHandler.error ("305", context,
 									"Deliverable obligations category is set to 'Reference " +
 									" Obligations Only' but further elements have been included",
 									getName (), null);
 
 								result = false;
 								break;
 							}
 						}
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures at least on credit event type is defined.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE35
 		= new Rule (Preconditions.R4_0__LATER, "cd-35")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("creditEvents");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (DOM.getFirstChild (context) == null) {
 						errorHandler.error ("305", context,
 							"No elements where found in creditEvents. The structure must " +
 							"contain at least one element",
 							getName (), null);
 
 						result = false;
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the correct number of information sources
 	 * are defined.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE36 
 		= new Rule (Preconditions.R4_0__LATER, "cd-36")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= XPath.paths (nodeIndex.getElementsByName ("creditEvents"), "creditEventNotice", "publiclyAvailableInformation");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (or (
 							exists (XPath.path (context, "standardPublicSources")),
 							exists (XPath.path (context, "publicSource"))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"Either at least one public source or standard public sources " +
 						"must be referred to in publiclyAvailableInformation",
 						getName (), null);
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the quotation amount is no smaller than
 	 * the minimum quotation amount.
 	 * <P>
 	 * Applies to FpML 4.0 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule	RULE37 
 		= new Rule (Preconditions.R4_0__LATER, "cd-37")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("cashSettlementTerms");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element	ccy1	= XPath.path (context, "quotationAmount", "currency");
 					Element	amount	= XPath.path (context, "quotationAmount", "amount");
 					Element	ccy2 	= XPath.path (context, "minimumQuotationAmount", "currency");
 					Element	minimum = XPath.path (context, "minimumQuotationAmount", "amount");
 
 					if ((ccy1 == null) || (ccy2 == null) || (amount == null) || (minimum == null)
 						|| notEqual (ccy1, ccy2)
 						|| (Double.parseDouble (string (amount)) >= Double.parseDouble (string (minimum))))
 						continue;
 
 					errorHandler.error ("305", context,
 						"In cash settlement terms, quotation amount " + string (amount) +
 						" must be greater or equal to minimum quotation amount",
 						getName (), null);
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the if any
 	 * <CODE>referencePoolItem/constituentWeight/basketPercentage<CODE> values
 	 * are defined then they must sum to 1.
 	 * <P>
 	 * Applies to FpML 4.2 and later.
 	 * @since	TFP 1.1
 	 */
 	public static final Rule	RULE38 
 		= new Rule (Preconditions.R4_2__LATER, "cd-38")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (validate (nodeIndex.getElementsByName ("creditDefaultSwap"), errorHandler));
 			}
 			
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element 	pool	= XPath.path (context, "generalTerm", "basketReferenceInformation",	"referencePool");
 					NodeList	items	= XPath.paths (pool, "referencePoolItem", "constituentWeight", "basketPercentage");
 					
 					if (items.getLength() == 0) continue;
 					
 					BigDecimal total = BigDecimal.ZERO;
 					for (int count = 0; count < items.getLength (); ++count)
 						total = total.add (decimal (items.item (count)));
 					
 					if (total.equals(BigDecimal.ONE)) continue;
 					
 					errorHandler.error ("305", pool,
 							"The sum of referencePoolItem/constituentWeight/basketPercentage should be equal to 1",
 							getName (), total.toString ());
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 	
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>nthToDefault</CODE> is present
 	 * and <CODE>mthToDefault</CODE> is present then <CODE>nthToDefault</CODE>
 	 * must be less than <CODE>mthToDefaultM</CODE>. 
 	 * <P>
 	 * Applies to FpML 4.2 and later.
 	 * @since	TFP 1.1
 	 */
 	public static final Rule	RULE39 
 		= new Rule (Preconditions.R4_2__LATER, "cd-39")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (validate (nodeIndex.getElementsByName ("creditDefaultSwap"), errorHandler));
 			}
 			
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		info	= XPath.path (context, "generalTerm", "basketReferenceInformation");
 					Element		nth		= XPath.path (context, "nthToDefault");
 					Element		mth		= XPath.path (context, "mthToDefault");
 					
 					if ((nth == null) || (mth == null) || (integer (nth) < integer (mth))) continue;
 					
 					errorHandler.error ("305", info,
 							"If nthToDefault is present and mthToDefault is present then nthToDefault must be less than mthToDefault.",
 							getName (), null);
 
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures <CODE>attachmentPoint</CODE> must be
 	 * less than or equal to <CODE>exhaustionPoint</CODE>. 
 	 * <P>
 	 * Applies to FpML 4.2 and later.
 	 * @since	TFP 1.1
 	 */
 	public static final Rule	RULE40 
 		= new Rule (Preconditions.R4_2__LATER, "cd-40")
 		{
 		/**
 		 * {@inheritDoc}
 		 */
 		public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 		{
 			return (validate (nodeIndex.getElementsByName ("creditDefaultSwap"), errorHandler));
 		}
 		
 		private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 		{
 			boolean		result = true;
 			
 			for (int index = 0; index < list.getLength (); ++index) {
 				Element		context = (Element) list.item (index);
 				Element		tranche	= XPath.path (context, "generalTerm", "basketReferenceInformation", "tranche");
 				Element		attach	= XPath.path (context, "attachmentPoint");
 				Element		exhaust	= XPath.path (context, "exhaustionPoint");
 				
 				if ((attach == null) || (exhaust == null) ||
 						(decimal (attach).compareTo (decimal (exhaust)) <= 0)) continue;
 				
 				errorHandler.error ("305", tranche,
 						"attachmentPoint must be less than or equal to exhaustionPoint.",
 						getName (), null);
 
 				result = false;
 			}
 			return (result);
 		}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures if <CODE>indexReferenceInformation/tranche<CODE>
 	 * is not present then <CODE>modifiedEquityDelivery<CODE> must not be present.
 	 * <P> 
 	 * Applies to FpML 4.3 and later.
 	 * @since	TFP 1.1
 	 */
 	public static final Rule	RULE41 
 		= new Rule (Preconditions.R4_3__LATER, "cd-41")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("generalTerms");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		tranche
 						= XPath.path (context, "indexReferenceInformation", "tranche");
 					Element		delivery
 						= XPath.path (context, "modifiedEquityDelivery");
 
 					if ((tranche == null) && (delivery != null)) {			
 						errorHandler.error ("305", context,
 							"If indexReferenceInformation/tranche is not present then modifiedEquityDelivery must not be present.",
 							getName (), null);
 						
 						result = false;
 					}
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * If <CODE>basketReferenceInformation<CODE> is not present then
 	 * <CODE>substitution<CODE> must not be present.
 	 * <P>
 	 * Applies to FpML 4.3 and later.
 	 * @since	TFP 1.1
 	 */
 	public static final Rule	RULE42
 		= new Rule (Preconditions.R4_3__LATER, "cd-42")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				NodeList	list 	= nodeIndex.getElementsByName ("generalTerms");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					Element		basket
 						= XPath.path (context, "basketReferenceInformation");
 					Element		substitution
 						= XPath.path (context, "substitution");
 
 					if ((basket == null) && (substitution != null)) {
 						errorHandler.error ("305", context,
 							"If basketReferenceInformation is not present then substitution must not be present.",
 							getName (), null);
 						
 						result = false;
 					}
 				}
 				return (result);
 			}
 		};
 		
 		/**
 		 * A <CODE>Rule</CODE> that ensures if the trade has an initial payment
 		 * then it is paid by the protection buyer to the protection seller.
 		 * <P>
 		 * Applies to FpML 4.3 and later.
 		 * @since	TFP 1.1
 		 */
 		public static final Rule	RULE43 
 			= new Rule (Preconditions.R4_3__LATER, "cd-43")
 			{
 				/**
 				 * {@inheritDoc}
 				 */
 				public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 				{
 					boolean		result 	= true;
 					NodeList	list 	= nodeIndex.getElementsByName ("creditDefaultSwap");
 					
 					for (int index = 0; index < list.getLength (); ++index) {
 						Element		context = (Element) list.item (index);
 						
 						if (!isSingleName (context)) continue;
 						
 						if (!exists (XPath.path(context, "feeLeg", "initialPayment"))) continue;
 						
 						Element		payer		= XPath.path (context, "feeLeg", "initialPayment", "payerPartyReference");
 						Element		seller		= XPath.path (context, "generalTerms", "sellerPartyReference");
 						Element		receiver 	= XPath.path (context, "feeLeg", "initialPayment", "payerPartyReference");
 						Element		buyer		= XPath.path (context, "generalTerms", "buyerPartyReference");
 
 						if ((payer != null) && (seller != null) && (receiver != null) && (buyer != null)) {
 							if (DOM.getAttribute (payer, "href").equals(DOM.getAttribute (seller, "href")) &&
 								DOM.getAttribute (receiver, "href").equals(DOM.getAttribute (buyer, "href")))
 								continue;
 						}
 						
 						errorHandler.error ("305", context,
 							"The initial payment should be paid by the protection buyer to the protection seller",
 							getName (), null);
 
 						result = false;
 					}
 					return (result);
 				}
 			};
 			
 		/**
 		 * A <CODE>Rule</CODE> that ensures either every <CODE>referencePoolItem</CODE>
 		 * has a <CODE>basketPercentage</CODE> or that none of them have.
 		 * <P>
 		 * Applies to FpML 4.2 and later.
 		 * @since	TFP 1.1
 		 */
 		public static final Rule	RULE44 
 			= new Rule (Preconditions.R4_2__LATER, "cd-44")
 			{
 				/**
 				 * {@inheritDoc}
 				 */
 				public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 				{
 					return (validate (nodeIndex.getElementsByName ("creditDefaultSwap"), errorHandler));
 				}
 				
 				private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 				{
 					boolean		result = true;
 					
 					for (int index = 0; index < list.getLength (); ++index) {
 						Element		context = (Element) list.item (index);
 						Element		pool	= XPath.path (context, "generalTerm", "basketReferenceInformation", "referencePool");
 						NodeList	items  	= XPath.paths (pool, "referencePoolItem");
 						NodeList	weights	= XPath.paths (pool, "referencePoolItem", "constituentWeight", "basketPercentage");
 						
 						if ((weights.getLength () == 0) || (weights.getLength () == items.getLength ())) continue;
 						
 						errorHandler.error ("305", pool,
 								"Either every referencePoolItem should have a basketPercentage or none should have one",
 								getName (), null);
 
 						result = false;
 					}
 					return (result);
 				}
 				};
 				
 	/**
 	 * Provides access to the CDS validation rule set.
 	 * 
 	 * @return	The data type validation rule set.
 	 * @since	TFP 1.0
 	 */
 	public static RuleSet getRules ()
 	{
 		return (rules);
 	}
 	
 	/**
 	 * Determines the trade under test references ISDA 1999 definitions.
 	 * 
 	 * @param	context			The trade element.
 	 * @return	<CODE>true</CODE> if the trade uses 1999 definitions.
 	 * @since	TFP 1.0
 	 */
 	protected static boolean isIsda1999 (Element context)
 	{
 		Element		documentation = DOM.getElementByLocalName (context, "documentation");
 
 		if (documentation != null) {
 			Element 	definitions = DOM.getElementByLocalName (documentation, "contractualDefinitions");
 			Element 	confirmType = DOM.getElementByLocalName (documentation, "masterConfirmation", "masterConfirmationType");
 			
 			return (DOM.getInnerText (definitions).equals ("ISDA1999Credit")
 				 || DOM.getInnerText (confirmType).equals ("ISDA1999Credit"));
 		}
 		return (false);
 	}
 		
 	/**
 	 * Determines the trade under test references ISDA 2003 definitions.
 	 * 
 	 * @param	trade		The trade element.
 	 * @return	<CODE>true</CODE> if the trade uses 2003 definitions.
 	 * @since	TFP 1.0
 	 */
 	protected static boolean isIsda2003 (Element trade)
 	{
 		if (exists (XPath.path (trade, "creditDefaultSwap"))) {
 			Element		target;
 
 			if ((target = XPath.path (trade, "documentation", "contractualDefinitions")) != null)
 				if (DOM.getInnerText (target).trim ().startsWith ("ISDA2003Credit"))
 					return (true);
 			
 			if ((target = XPath.path (trade, "documentation", "masterConfirmation", "masterConfirmationType")) != null) {
 				String value = DOM.getInnerText (trade).trim ();
 	
 				if (value.startsWith ("ISDA2003Credit") ||
 					value.startsWith ("ISDA2004Credit"))
 					return (true);
 			}
 		}		
 		return (false);
 	}
 	
 	/**
 	 * Determines if the trade under tests contains a short form contract.
 	 * 
 	 * @param	trade		The trade element.
 	 * @return	<CODE>true</CODE> if the trade is short form.
 	 * @since	TFP 1.0
 	 */
 	protected static boolean isShortForm (Element trade)
 	{
 		Element	target;
 		
 		if (exists (XPath.path (trade, "creditDefaultSwap"))) {
 			if (exists (XPath.path (trade, "documentation", "masterConfirmation")))
 				return (true);
 			if (exists (XPath.path (trade, "documentation", "contractualMatrix")))
 				return (true);
 
 			if ((target = XPath.path (trade, "documentation", "contractualTermsSupplement")) != null) {
 				String	value = DOM.getInnerText (target).trim ();
 				if (value.startsWith ("iTraxx") ||
 					value.startsWith ("CDX"))
 					return (true);
 			}
 		}
 		return (false);
 	}
 	
 	/**
 	 * Determines if the trade under tests contains a long form contract.
 	 * 
 	 * @param	trade		The trade element.
 	 * @return	<CODE>true</CODE> if the trade is long form.
 	 * @since	TFP 1.0
 	 */
 	protected static boolean isLongForm (Element trade)
 	{
 		Element	cds;
 
 		if (exists (cds = XPath.path (trade, "creditDefaultSwap"))) {
 			if (exists (XPath.path (trade, "documentation", "masterConfirmation")))
 				return (false);
 			if (exists (XPath.path (trade, "documentation", "contractualMatrix")))
 				return (false);
 
 			return (isSingleName (cds));
 		}
 		return (false);
 	}
 	
 	/**
 	 * Determines if a credit default swap is on a single name.
 	 * 
 	 * @param 	cds			The credit default swap product.
 	 * @return	<CODE>true</CODE> if the product is single name.
 	 * @since	TFP 1.0
 	 */
 	private static boolean isSingleName (Element cds)
 	{
 		if (exists (XPath.path (cds, "generalTerms", "referenceInformation", "referenceObligation", "bond")))
 			return (true);
 		if (exists (XPath.path (cds, "generalTerms", "referenceInformation", "referenceObligation", "convertableBond")))
 			return (true);
 
 		return (false);
 	}
 
 	/**
 	 * Determines if a credit default swap is on an index.
 	 * 
 	 * @param 	cds			The credit default swap product.
 	 * @return	<CODE>true</CODE> if the product is an index.
 	 * @since	TFP 1.0
 	 */
 	private static boolean isCreditIndex (Element cds)
 	{
 		if (exists (XPath.path (cds, "generalTerms", "indexReferenceInformation")) &&
 		   !exists (XPath.path (cds, "generalTerms", "indexReferenceInformation", "tranche")))
 			return (true);
 
 		return (false);
 	}
 
 	/**
 	 * A <CODE>RuleSet</CODE> containing all the standard FpML defined
 	 * validation rules for interest rate products.
 	 * @since	TFP 1.0
 	 */
 	private static final RuleSet	rules = new RuleSet ();
 	
 	/**
 	 * Ensures no instances can be created.
 	 * @since	TFP 1.0
 	 */
 	private CdsRules ()
 	{ }
 	
 	/**
 	 * Extracts an <CODE>Interval</CODE> from the data stored below the
 	 * given context node.
 	 *
 	 * @param 	context			The context <CODE>XmlElement</CODE>
 	 * @return	An <CODE>Interval</CODE> constructed from the stored data.
 	 * @since	TFP 1.0
 	 */
 	private static Interval interval (final Element context)
 	{
 		try {
 			return (new Interval (
 				Integer.parseInt (string (XPath.path (context, "periodMultiplier"))),
 				Period.forCode (string (XPath.path (context, "period")))));
 		}
 		catch (Exception error) {
 			return (null);
 		}
 	}
 
 	/**
 	 * Initialises the <CODE>RuleSet</CODe> by adding the individually defined
 	 * validation rules. 
 	 * @since	TFP 1.0
 	 */
 	static {
 		rules.add (RULE01);
 		rules.add (RULE01B);
 		rules.add (RULE02);
 		rules.add (RULE03);
 		rules.add (RULE04);
 		rules.add (RULE05);
 		rules.add (RULE06);
 		rules.add (RULE07);
 		rules.add (RULE08);
 		rules.add (RULE09);
 		rules.add (RULE10);
 		rules.add (RULE11);
 		rules.add (RULE12);
 		rules.add (RULE13);
 		rules.add (RULE14);
 		rules.add (RULE15);
 		rules.add (RULE16);
 		rules.add (RULE17);
 		rules.add (RULE18);
 		rules.add (RULE19);
 		rules.add (RULE20);
 		rules.add (RULE21);
 		rules.add (RULE21B);
 		rules.add (RULE22);
 		rules.add (RULE23);
 		rules.add (RULE24);
 		rules.add (RULE25);
 		rules.add (RULE26);
 		rules.add (RULE27);
 		rules.add (RULE28);
 		rules.add (RULE29);
 		rules.add (RULE30);
 		rules.add (RULE31);
 		rules.add (RULE32);
 		rules.add (RULE33);
 		rules.add (RULE34);
 		rules.add (RULE35);
 		rules.add (RULE36);
 		rules.add (RULE37);
 		rules.add (RULE38);
 		rules.add (RULE39);
 		rules.add (RULE40);
 		rules.add (RULE41);
 		rules.add (RULE42);
 		rules.add (RULE43);
 		rules.add (RULE44);
 	}
 }
