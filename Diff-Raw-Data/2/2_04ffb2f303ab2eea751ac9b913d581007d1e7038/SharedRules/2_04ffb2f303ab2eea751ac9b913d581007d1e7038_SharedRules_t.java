 // Copyright (C),2005-2006 HandCoded Software Ltd.
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
 
 import java.util.Vector;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.handcoded.finance.Date;
 import com.handcoded.finance.Time;
 import com.handcoded.validation.ValidationErrorHandler;
 import com.handcoded.validation.Rule;
 import com.handcoded.validation.RuleSet;
 import com.handcoded.xml.DOM;
 import com.handcoded.xml.Logic;
 import com.handcoded.xml.NodeIndex;
 import com.handcoded.xml.XPath;
 
 /**
  * The <CODE>SharedRules</CODE> class contains a <CODE>RuleSet</CODE>
  * initialised with FpML defined validation rules for shared components.
  *
  * @author	BitWise
  * @version	$Id$
  * @since	TFP 1.0
  */
 public final class SharedRules extends Logic
 {
 	/**
 	 * A <CODE>Rule</CODE> instance that ensures that business centers are
 	 * only present if the date adjustment convention allows them.
 	 * <P>
 	 * Applies to all FpML versions.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE01 = new Rule ("shared-1")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (
 					  validate (nodeIndex.getElementsByName ("dateAdjustments"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("calculationPeriodDatesAdjustments"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("paymentDatesAdjustments"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("resetDatesAdjustments"), errorHandler));
 			}
 
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)			
 			{
 				boolean		result = true;
 
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					String text = DOM.getInnerText (DOM.getElementByLocalName (context, "businessDayConvention"));
 												
 					NodeList defs = context.getElementsByTagName ("businessCenters");
 					NodeList refs = context.getElementsByTagName ("businessCentersReference");
 					
 					if (text.equals ("NONE") || text.equals ("NotApplicable")) {
 						if ((defs.getLength () + refs.getLength ()) != 0) {
 							errorHandler.error ("305", context,
 								"business center definitions or references should not be present", getName (), null);
 							result = false;
 						}
 					}
 					else {
 						if ((defs.getLength () + refs.getLength ()) == 0) {
 							errorHandler.error ("305", context,
 								"business center definitions or references should be present", getName (), null);
 							result = false;
 						}
 					}
 				}
 				return (result);
 			}			
 		};
 		
 	/**
 	 *  A <CODE>Rule</CODE> that ensures that period multiplier is 'D' if the
 	 * &lt;dayType&gt; element is present.
 	 * <P>
 	 * Applies to FpML 1.0, 2.0 and 3.0.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE02 = new Rule (Preconditions.R1_0__TR3_0, "shared-2")
 		{
 			/*
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (
 					  validate (nodeIndex.getElementsByName ("cashSettlementValuationDate"), errorHandler)
 				    & validate (nodeIndex.getElementsByName ("feePaymentDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDateOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("initialFixingDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("paymentDaysOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("rateCutOffDaysOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("relativeDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalInterimExchangePaymentDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalFixingDates"), errorHandler));
 			}
 
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					if (implies (
 							exists (XPath.path (context, "dayType")),
 							equal (XPath.path (context, "period"), "D")))
 						continue;
 					
 					errorHandler.error ("305", context,
 						"Offset contains a day type by the the period is '" +
 						DOM.getInnerText (XPath.path (context, "period")) + "', not 'D'",
 						getName (), null);
 					
 					result = false;
 				}
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures that period multiplier is not zero when
 	 * the day type is 'Business
 	 * <P>
 	 * Applies to FpML 1.0, 2.0 and 3.0.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE03 = new Rule (Preconditions.R1_0__TR3_0, "shared-3")
 		{
 			/*
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return ( 
 					  validate (nodeIndex.getElementsByName ("cashSettlementValuationDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("feePaymentDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDateOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("initialFixingDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("paymentDaysOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("rateCutOffDaysOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("relativeDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalInterimExchangePaymentDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalFixingDates"), errorHandler));
 			}
 
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 			{
 				boolean		result		= true;
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 
 					if (implies (
 							equal (XPath.path (context, "dayType"), "Business"),
 							notEqual (XPath.path (context, "periodMultiplier"), 0)))
 						continue;
 
 					errorHandler.error ("305", context,
 						"Offset has day type set to 'Business' but the period " +
 						"multiplier is set to zero.",
 						getName (), string (XPath.path (context, "periodMultiplier")));
 
 					result = false;
 				}				
 				return (result);
 			}
 		};
 
 	/**
 	 * A <CODE>Rule</CODE> that ensures that the businessDayConvention is
 	 * NONE when the day type is Business.
 	 * <P>
 	 * Applies to all FpML versions.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE04 = new Rule ("shared-4")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (
 					  validate (nodeIndex.getElementsByName ("relativeDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDateOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("initialFixingDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("cashSettlementValuationDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalFixingDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalInterimExchangePaymentDates"), errorHandler));
 			}
 			 
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 								
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		dayType = DOM.getElementByLocalName (context, "dayType");
 					
 					if ((dayType != null) && DOM.getInnerText (dayType).equals ("Business")) {
 						String value = DOM.getInnerText (DOM.getElementByLocalName (context, "businessDayConvention"));
 						if (!value.equals ("NONE")) {
 							errorHandler.error ("305", context,
 								"businessDayConvention should be NONE", getName (), value);
 							result = false;
 						}
 					}
 				}
 				return (result);
 			}			
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the payer and receivers are different.
 	 * <P>
 	 * Applies to all FpML versions.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE05 = new Rule ("shared-5")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("payerPartyReference");
 
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element payer  = (Element) list.item (index);
 					Element parent = (Element) payer.getParentNode ();
  					Element receiver = DOM.getElementByLocalName (parent, "receiverPartyReference");
 					
 					if (payer.getAttribute ("href").equals (receiver.getAttribute ("href"))) {
 						errorHandler.error ("305", parent,
 							"payer and receiver party references must be different",
 							getName (), payer.getAttribute ("href"));
 						result = false;
 					}
 				}
 
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures latestExerciseTime is after the
 	 * earliestExerciseTime for American exercises.
 	 * <P>
 	 * Applies to FpML 3-0 and later.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule 	RULE06
 		= new Rule (Preconditions.TR3_0__LATER, "shared-06")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("americanExercise");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		latest  = DOM.getElementByLocalName (context, "latestExerciseTime");
 					
 					if (latest != null) {
 						Element		earliest = DOM.getElementByLocalName (context, "earliestExerciseTime");
 						
 						try {
 							Time latestTime   = Time.parse (DOM.getInnerText (DOM.getElementByLocalName (latest, "hourMinuteTime")));
 							Time earliestTime = Time.parse (DOM.getInnerText (DOM.getElementByLocalName (earliest, "hourMinuteTime")));
 
 							if (earliestTime.compareTo (latestTime) >= 0) {
 								errorHandler.error ("305", context,
 									"The latest exercise time must be after the earliest",
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
 	 * A <CODE>Rule</CODE> that ensures latestExerciseTime is after the
 	 * earliestExerciseTime for American exercises.
 	 * <P>
 	 * Applies to FpML 3-0 and later.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule 	RULE07 
 		= new Rule (Preconditions.TR3_0__LATER, "shared-07")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("bermudaExercise");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					Element		latest  = DOM.getElementByLocalName (context, "latestExerciseTime");
 
 					if (latest != null) {
 						Element		earliest = DOM.getElementByLocalName (context, "earliestExerciseTime");
 						
 						try {
 							Time latestTime   = Time.parse (DOM.getInnerText (latest));
 							Time earliestTime = Time.parse (DOM.getInnerText (earliest));
 
 							if (earliestTime.compareTo (latestTime) >= 0) {
 								errorHandler.error ("305", context,
 									"The latest exercise time must be after the earliest",
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
 	 * A <CODE>Rule</CODE> that ensures unadjustedFirstDate is before
 	 * unadjustedLastDate.
 	 * <P>
 	 * Applies to FpML 3-0 and later.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE08 
 		= new Rule (Preconditions.TR3_0__LATER, "shared-8")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (
 					  validate (nodeIndex.getElementsByName ("scheduleBounds"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("businessDateRange"), errorHandler));
 			}
 			
 			/**
 			 * Validate all of the elements identified by the given
 			 * <CODE>NodeList</CODE>.
 			 *
 			 * @param	list		The <CODE>NodeList</CODE> of candidate elements.
 			 * @param	errorHandler The <CODE>ErrorHandler</CODE> to report
 			 *						semantic errors through.
 			 * @return	<CODE>false</CODE> of the RULE failed, <CODE>true
 			 *			</CODE> otherwise.
 			 */
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 			{
 				boolean		result 	= true;
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					
 					try {
 						Date firstDate = Date.parse (DOM.getInnerText (DOM.getElementByLocalName (context, "unadjustedFirstDate")));
 						Date lastDate  = Date.parse (DOM.getInnerText (DOM.getElementByLocalName (context, "unadjustedLastDate")));
 						
 						if (firstDate.compareTo (lastDate) >= 0) {
 							errorHandler.error ("305", context,
 									"The unadjusted last date must be after the unadjusted first date",
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
 	 * A <CODE>Rule</CODE> that ensures business centers are not defined
 	 * or referenced if the businessDayConvention is NONE or NotApplicable.
 	 * <P>
 	 * Applies to FpML 3-0 and later.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE09 
 		= new Rule (Preconditions.TR3_0__LATER, "shared-9")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (validate (nodeIndex.getElementsByName ("businessDateRange"), errorHandler));
 			} 
 
 			/**
 			 * Validate all of the elements identified by the given
 			 * <CODE>NodeList</CODE>.
 			 *
 			 * @param	list		The <CODE>NodeList</CODE> of candidate elements.
 			 * @param	errorHandler The <CODE>ErrorHandler</CODE> to report
 			 *						semantic errors through.
 			 * @return	<CODE>false</CODE> of the RULE failed, <CODE>true
 			 *			</CODE> otherwise.
 			 */
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)			
 			{
 				boolean		result = true;
 
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element		context = (Element) list.item (index);
 					
 					String text = DOM.getInnerText (DOM.getElementByLocalName (context, "businessDayConvention"));
 												
 					NodeList defs = context.getElementsByTagName ("businessCenters");
 					NodeList refs = context.getElementsByTagName ("businessCentersReference");
 					
 					if (text.equals ("NONE") || text.equals ("NotApplicable")) {
 						if ((defs.getLength () + refs.getLength ()) != 0) {
 							errorHandler.error ("305", context,
 								"business center definitions or references should not be present", getName (), null);
 							result = false;
 						}
 					}
 					else {
 						if ((defs.getLength () + refs.getLength ()) == 0) {
 							errorHandler.error ("305", context,
 								"business center definitions or references should be present", getName (), null);
 							result = false;
 						}
 					}
 				}
 				return (result);
 			}			
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures calculationAgentPartyReference/@href
 	 * attributes are unique.
 	 * <P>
 	 * Applies to all FpML versions.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE10 = new Rule ("shared-10")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list   = nodeIndex.getElementsByName ("calculationAgent");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					NodeList refs 	= context.getElementsByTagName ("calculationAgentPartyReference");
 					Vector	values 	= new Vector ();
 					
 					for (int count = 0; count < refs.getLength(); ++count) {
 						String href = ((Element) refs.item (count)).getAttribute ("href");
 						
 						if (values.contains (href)) {
 							errorHandler.error ("305", context,
 								"Duplicate calculationAgentPartyReference", getName (), href);
 							result = false;
 						}
 						else
 							values.add (href);
 					}	
 					values.clear ();
 				}
 
 				return (result);
 			}
 		};
 
 	/**
 	 * A <CODE>Rule</CODE> that ensures businessDateRange references to
 	 * business centers are within the same trade.
 	 * <P>
 	 * Applies to FpML 3-0 and later.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE11 
 		= new Rule (Preconditions.TR3_0__LATER, "shared-11")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("businessDateRange");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element	context = (Element) list.item (index);
 					Element ref = DOM.getElementByLocalName (context, "businessCentersReference");
 		
 					if (ref != null) {
 						Element def = nodeIndex.getDocument ().getElementById (ref.getAttribute ("href"));
 						
 						if (!def.getLocalName ().equals ("businessCenters")) {
 							errorHandler.error ("305", context,
 								"Reference does not match with a businessCenters element",
 								getName (), null);
 							result = false;
 						}
 						else {
 							// Walk up to <trade> node	
 							do {
 								Node node = ref.getParentNode ();
 								
 								if (node.getNodeType () == Node.ELEMENT_NODE)
 									ref = (Element) node;
 								else
 									ref = null;
 							} while ((ref != null) && !ref.getLocalName ().equals ("trade"));
 							
 							// Walk up to <trade> node
 							do {
 								Node node = def.getParentNode ();
 								
 								if (node.getNodeType () == Node.ELEMENT_NODE)
 									def = (Element) node;
 								else
 									def = null;
 							} while ((def != null) && !def.getLocalName ().equals ("trade"));
 							
 							if (def != ref) {
 								errorHandler.error ("305", context,
 									"The referenced business centers are not in the same trade",
 									getName (), null);
 								result = false;
 							}
 						}
 					}
 				}
 
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the referential integrity of
 	 * buyerPartyReference/@href instances.
 	 * <P>
 	 * Applies to FpML 1-0 and 2-0.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE12A 
 		= new Rule (Preconditions.R1_0__R2_0, "shared-12a")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("buyerPartyReference");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					String  href	= context.getAttribute ("href");
 					
 					if ((href == null) || (href.length() < 2) || (href.charAt(0) != '#')) {
 						errorHandler.error ("305", context,
 							"The @href attribute is not a valid XPointer",
 							getName (), href);
 						result = false;
 						continue;
 					}
 					
 					Element target	= nodeIndex.getElementById (href.substring (1));
 					
 					if ((target == null) || !(target.getLocalName ().equals ("party") || target.getLocalName ().equals ("tradeSide"))) {
 						errorHandler.error ("305", context,
 							"The @href attribute does not reference a party element",
 							getName (), href);
 						result = false;
 					}
 				}
 				
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the referential integrity of
 	 * buyerPartyReference/@href instances.
 	 * <P>
 	 * Applies to FpML 3-0 and later.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE12B
 		= new Rule (Preconditions.TR3_0__LATER, "shared-12b")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("buyerPartyReference");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					String  href	= context.getAttribute ("href");
 					Element target	= nodeIndex.getElementById (href);
 					
 					if ((target == null) || !(target.getLocalName ().equals ("party")  || target.getLocalName ().equals ("tradeSide"))) {
 						errorHandler.error ("305", context,
 							"The @href attribute does not reference a party element",
 							getName (), href);
 						result = false;
 					}
 				}
 				
 				return (result);
 			}
 		};
 			
 	/**
 	 * A <CODE>Rule</CODE> that ensures the referential integrity of
 	 * sellerPartyReference/@href instances.
 	 * <P>
 	 * Applies to FpML 1-0 and 2-0.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE13A
 		= new Rule (Preconditions.R1_0__R2_0, "shared-13a")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("sellerPartyReference");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					String  href	= context.getAttribute ("href");
 					
 					if ((href == null) || (href.length() < 2) || (href.charAt(0) != '#')) {
 						errorHandler.error ("305", context,
 							"The @href attribute is not a valid XPointer",
 							getName (), href);
 						result = false;
 						continue;
 					}
 					
 					Element target	= nodeIndex.getElementById (href.substring (1));
 					
 					if ((target == null) || !(target.getLocalName ().equals ("party"))) {
 						errorHandler.error ("305", context,
 							"The @href attribute does not reference a party element",
 							getName (), href);
 						result = false;
 					}
 				}
 				
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the referential integrity of
 	 * sellerPartyReference/@href instances.
 	 * <P>
 	 * Applies to FpML 3-0 and later. 
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE13B 
 		= new Rule (Preconditions.TR3_0__LATER, "shared-13b")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("sellerPartyReference");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					String  href	= context.getAttribute ("href");
 					Element target	= nodeIndex.getElementById (href);
 					
					if ((target == null) || !(target.getLocalName ().equals ("party") || target.getLocalName ().equals ("tradeSide"))) {
 						errorHandler.error ("305", context,
 							"The @href attribute does not reference a party element",
 							getName (), href);
 						result = false;
 					}
 				}
 				
 				return (result);
 			}
 		};
 			
 	/**
 	 * A <CODE>Rule</CODE> that ensures the referential integrity of
 	 * calculationAgentPartyReference/@href instances.
 	 * <P>
 	 * Applies to FpML 1-0 and 2-0. 
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE14A
 		= new Rule (Preconditions.R1_0__R2_0, "shared-14a")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("calculationAgentPartyReference");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					String  href	= context.getAttribute ("href");
 					
 					if ((href == null) || (href.length() < 2) || (href.charAt(0) != '#')) {
 						errorHandler.error ("305", context,
 							"The @href attribute is not a valid XPointer",
 							getName (), href);
 						result = false;
 						continue;
 					}
 					
 					Element target	= nodeIndex.getElementById (href.substring (1));
 					
 					if ((target == null) || !(target.getLocalName ().equals ("party"))) {
 						errorHandler.error ("305", context,
 							"The @href attribute does not reference a party element",
 							getName (), href);
 						result = false;
 					}
 				}
 				
 				return (result);
 			}
 		};
 
 	/**
 	 * A <CODE>Rule</CODE> that ensures the referential integrity of
 	 * calculationAgentPartyReference/@href instances.
 	 * <P>
 	 * Applies to FpML 3-0 and later. 
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE14B
 		= new Rule (Preconditions.TR3_0__LATER, "shared-14b")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = nodeIndex.getElementsByName ("calculationAgentPartyReference");
 				
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					String  href	= context.getAttribute ("href");
 					Element target	= nodeIndex.getElementById (href);
 					
 					if ((target == null) || !(target.getLocalName ().equals ("party"))) {
 						errorHandler.error ("305", context,
 							"The @href attribute does not reference a party element",
 							getName (), href);
 						result = false;
 					}
 				}
 				
 				return (result);
 			}
 		};
 
 	/**
 	 * A <CODE>Rule</CODE> that ensures that period multiplier is 'D' if the
 	 * &lt;dayType&gt; element is present.
 	 * <P>
 	 * Applies to all FpML versions.
 	 * @since	TFP 1.0	
 	 */
 	public static final Rule	RULE15 = new Rule ("shared-15")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				return (
 					  validate (nodeIndex.getElementsByName ("gracePeriod"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("paymentDaysOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("rateCutOffDaysOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("relativeDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDateOffset"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("initialFixingDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("fixingDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("cashSettlementValuationDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalFixingDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("varyingNotionalInterimExchangePaymentDates"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("feePaymentDate"), errorHandler)
 					& validate (nodeIndex.getElementsByName ("relativeDates"), errorHandler));
 			}
 			
 			/**
 			 * Validate all of the elements identified by the given
 			 * <CODE>NodeList</CODE>.
 			 *
 			 * @param	list		The <CODE>NodeList</CODE> of candidate elements.
 			 * @param	errorHandler The <CODE>ErrorHandler</CODE> to report
 			 *						semantic errors through.
 			 * @return	<CODE>false</CODE> if the RULE failed, <CODE>true
 			 *			</CODE> otherwise.
 			 */
 			private boolean validate (NodeList list, ValidationErrorHandler errorHandler)
 			{			
 				boolean		result = true;
 
 				for (int index = 0; index < list.getLength (); ++index) {
 					Element context = (Element) list.item (index);
 					Element period  = DOM.getElementByLocalName (context, "period");
 					Element dayType = DOM.getElementByLocalName (context, "dayType");
 					Element factor  = DOM.getElementByLocalName (context, "periodMultiplier");
 					
 					if (dayType != null) {
 						if (!DOM.getInnerText (period).equals ("D")
 							|| Integer.parseInt (DOM.getInnerText (factor)) == 0) {
 							errorHandler.error ("305", context,
 								"The dayType element should not be present",
 								getName (), null);
 							result = false;
 						}
 					}
 					else {
 						if (DOM.getInnerText (period).equals ("D")
 							&& Integer.parseInt (DOM.getInnerText (factor)) != 0) {
 							errorHandler.error ("305", context,
 								"The dayType element should be present",
 								getName (), null);
 							result = false;
 						}
 					}			
 				}
 
 				return (result);
 			}
 		};
 		
 	/**
 	 * A <CODE>Rule</CODE> that ensures the reference integrity of trade side
 	 * party references.
 	 * <P>
 	 * Applies to FpML 4.2 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule RULE16	= new Rule (Preconditions.TR4_2__LATER, "shared-16")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = XPath.paths (nodeIndex.getElementsByName ("tradeSide"), "*", "party");
 				
 				for (int index = 0; index < list.getLength(); ++index) {
 					Element		context = (Element) list.item (index);
 					String		href	= context.getAttribute ("href");
 					Element		target	= nodeIndex.getElementById (href);
 					
 					if (target.getLocalName ().equals ("party")) continue;
 					
 					errorHandler.error ("305", context,
 						"The value of the href attribute does not refere to a party structure",
 						getName (), href);
 					
 					result = false;
 				}
 				return (result);
 			}
 		};
 
 	/**
 	 * A <CODE>Rule</CODE> that ensures the reference integrity of trade side
 	 * account references.
 	 * <P>
 	 * Applies to FpML 4.2 and later.
 	 * @since	TFP 1.0
 	 */
 	public static final Rule RULE17	= new Rule (Preconditions.TR4_2__LATER, "shared-17")
 		{
 			/**
 			 * {@inheritDoc}
 			 */
 			public boolean validate (NodeIndex nodeIndex, ValidationErrorHandler errorHandler)
 			{
 				boolean		result = true;
 				NodeList	list = XPath.paths (nodeIndex.getElementsByName ("tradeSide"), "*", "account");
 				
 				for (int index = 0; index < list.getLength(); ++index) {
 					Element		context = (Element) list.item (index);
 					String		href	= context.getAttribute ("href");
 					Element		target	= nodeIndex.getElementById (href);
 					
 					if (target.getLocalName ().equals ("account")) continue;
 					
 					errorHandler.error ("305", context,
 						"The value of the href attribute does not refere to an account structure",
 						getName (), href);
 					
 					result = false;
 				}
 				return (result);
 			}
 		};
 
 	/**
 	 * Provides access to the shared components validation rule set.
 	 * 
 	 * @return	The data type validation rule set.
 	 * @since	TFP 1.0
 	 */
 	public static RuleSet getRules ()
 	{
 		return (rules);
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
 	private SharedRules ()
 	{ }
 	
 	/**
 	 * Initialises the <CODE>RuleSet</CODe> by adding the individually defined
 	 * validation rules.
 	 * @since	TFP 1.0	
 	 */
 	static {
 		rules.add (RULE01);
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
 		rules.add (RULE12A);
 		rules.add (RULE12B);
 		rules.add (RULE13A);
 		rules.add (RULE13B);
 		rules.add (RULE14A);
 		rules.add (RULE14B);
 		rules.add (RULE15);
 		rules.add (RULE16);
 		rules.add (RULE17);
 	}
 }
