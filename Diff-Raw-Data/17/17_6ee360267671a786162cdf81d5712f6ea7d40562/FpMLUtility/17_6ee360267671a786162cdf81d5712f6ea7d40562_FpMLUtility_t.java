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
 
 package com.handcoded.fpml;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 
 import org.w3c.dom.Document;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 
 import com.handcoded.fpml.validation.AllRules;
 import com.handcoded.meta.SchemaRelease;
 import com.handcoded.validation.RuleSet;
 import com.handcoded.validation.ValidationErrorHandler;
 import com.handcoded.xml.SchemaSet;
 import com.handcoded.xml.XmlUtility;
 import com.handcoded.xml.resolver.Catalog;
 import com.handcoded.xml.resolver.CatalogManager;
 
 /**
  * The <CODE>FpMLUtility</CODE> class contains a set of methods for performing 
  * common operations on FpML document instances.
  * 
  * @author	BitWise
  * @version	$Id$
  * @since	TFP 1.0
  */
 public final class FpMLUtility
 {
 	/**
 	 * Provides access to the schema set.
 	 * 
 	 * @return	The <CODE>SchemaSet</CODE> instance.
 	 */
 	public static SchemaSet getSchemaSet ()
 	{
 		return (schemaSet);
 	}
 	
 	/**
 	 * Provides access to the compiled schema collection used by the parse
 	 * functions.
 	 * <P>
 	 * Calling this function will force the runtime to load all the known
 	 * FpML schemas into memory if it has not already been done.
 	 * 
 	 * @return	The compiled <CODE>Schema</CODE> collection.
 	 * @since	TFP 1.0
 	 */
 	public static Schema getSchema ()
 	{
 		return (schemaSet.getSchema ());
 	}
 	
 	/**
 	 * Parses the XML string provided passing any validation problems to
 	 * the indicated <CODE>ErrorHandler</CODE>.
 	 *
 	 * @param	xml				The XML string to be parsed.
 	 * @param	errorHandler	The <CODE>ErrorHandler</CODE> instance or <CODE>null</CODE>
 	 * @return	A <CODE>Document</CODE> instance constructed from the XML document.
 	 * @since	TFP 1.0
 	 */
 	public static Document parse (final String xml, ErrorHandler errorHandler)
 	{
 		return (parse (XmlUtility.DTD_OR_SCHEMA, xml, errorHandler));
 	}
 	
 	public static Document parse (int grammar, final String xml, ErrorHandler errorHandler)
 	{
 		return (XmlUtility.validatingParse (grammar, xml, getSchema (), getCatalog (), errorHandler));
 	}
 
 	/**
 	 * Parses an XML document from the given <CODE>File</CODE> passing
 	 * any reported errors to the <CODE>EerorHandler</CODE> instance.
 	 *
 	 * @param	file			The <CODE>File</CODE> to process XML from.
 	 * @param	errorHandler	The <CODE>ErrorHandler</CODE> instance or <CODE>null</CODE>
 	 * @return	A <CODE>Document</CODE> instance constructed from the XML document.
 	 * @since	TFP 1.0
 	 */
 	public static Document parse (File file, ErrorHandler errorHandler)
 	{
 		return (parse (XmlUtility.DTD_OR_SCHEMA, file, errorHandler));
 	}
 	
 	public static Document parse (int grammar, File file, ErrorHandler errorHandler)
 	{
 		return (XmlUtility.validatingParse (grammar, file, getSchema (), getCatalog (), errorHandler));
 	}
 	
 	/**
 	 * Uses the given <CODE>RuleSet</CODE> to perform a semantic validation of
 	 * the DOM <CODE>Document</CODE> and reports errors (if any).
 	 *
 	 * @param 	document		The <CODE>Document</CODE> to be validated.
 	 * @param 	rules			The <CODE>RuleSet</CODE> to use.
 	 * @param 	validationErrorHandler	The <CODE>ValidationErrorHandler</CODE> used to report issues.
 	 * @return	<CODE>true</CODE> if the <CODE>Document</CODE> successfully passed
 	 * 			all applicable rules, <CODE>false</CODE> if one or more rules
 	 * 			failed.
 	 * @since	TFP 1.0
 	 */
 	public static boolean validate (final Document document, final RuleSet rules, final ValidationErrorHandler validationErrorHandler)
 	{
 		return (rules.validate (document, validationErrorHandler));
 	}
 
 	/**
 	 * Uses the default FpML  <CODE>RuleSet</CODE> to perform a semantic validation of
 	 * the DOM <CODE>Document</CODE> and reports errors (if any).
 	 *
 	 * @param 	document		The <CODE>Document</CODE> to be validated.
 	 * @param 	validationErrorHandler	The <CODE>ValidationErrorHandler</CODE> used to semantic report issues.
 	 * @return	<CODE>true</CODE> if the <CODE>Document</CODE> successfully passed
 	 * 			all applicable rules, <CODE>false</CODE> if one or more rules
 	 * 			failed.
 	 * @since	TFP 1.0
 	 */
 	public static boolean validate (final Document document, final ValidationErrorHandler validationErrorHandler)
 	{
 		return (validate (document, AllRules.getRules (), validationErrorHandler));
 	}
 
 	/**
 	 * Attempts to parse an XML document from a string and then pass it through
 	 * the specified validation rule set.
 	 * <P>
 	 * This function does not provide any access to the DOM <CODE>Document</CODE>
 	 * created during the parsing process.
 	 * 
 	 * @param	xml				The XML string to be parsed.
 	 * @param 	rules			The <CODE>RuleSet</CODE> used for validation.
 	 * @param 	errorHandler	The <CODE>ErrorHandler</CODE> used to report parser errors.
 	 * @param 	validationErrorHandler	The <CODE>ValidationErrorHandler</CODE> used to semantic report issues.
 	 * @since	TFP 1.0
 	 */
 	public static boolean parseAndValidate (final String xml, RuleSet rules, ErrorHandler errorHandler, ValidationErrorHandler validationErrorHandler)
 	{
 		Document		document = parse (xml, errorHandler);
 
 		return ((document != null) ? rules.validate (document, validationErrorHandler) : false);
 	}
 	
 	/**
 	 * Attempts to parse an XML document from the indicated <CODE>File</CODE>
 	 * and then pass it through the specified validation rule set.
 	 * <P>
 	 * This function does not provide any access to the DOM <CODE>Document</CODE>
 	 * created during the parsing process.
 	 * 
 	 * @param	file			The <CODE>File</CODE> to be parsed.
 	 * @param 	rules			The <CODE>RuleSet</CODE> used for validation.
 	 * @param 	errorHandler	The <CODE>ErrorHandler</CODE> used to report parser errors.
 	 * @param 	validationErrorHandler	The <CODE>ValidationErrorHandler</CODE> used to semantic report issues.
 	 * @since	TFP 1.0
 	 */
 	public static boolean parseAndValidate (File file, RuleSet rules, ErrorHandler errorHandler, ValidationErrorHandler validationErrorHandler)
 	{
 		Document		document = parse (file, errorHandler);
 
 		return ((document != null) ? rules.validate (document, validationErrorHandler) : false);
 	}
 	
 	/**
 	 * Attempts to parse an XML document from a string and then pass it through
 	 * the default FpML validation rule set.
 	 * <P>
 	 * This function does not provide any access to the DOM <CODE>Document</CODE>
 	 * created during the parsing process.
 	 * 
 	 * @param	xml				The XML string to be parsed.
 	 * @param 	errorHandler	The <CODE>ErrorHandler</CODE> used to report parser errors.
 	 * @param 	validationErrorHandler	The <CODE>ValidationErrorHandler</CODE> used to semantic report issues.
 	 * @since	TFP 1.0
 	 */
 	public static boolean parseAndValidate (final String xml, ErrorHandler errorHandler, ValidationErrorHandler validationErrorHandler)
 	{
 		return (parseAndValidate (xml, AllRules.getRules (), errorHandler, validationErrorHandler));
 	}
 
 	/**
 	 * Attempts to parse an XML document from the indicated <CODE>File</CODE>
 	 * and then pass it through the default FpML validation rule set.
 	 * <P>
 	 * This function does not provide any access to the DOM <CODE>Document</CODE>
 	 * created during the parsing process.
 	 * 
 	 * @param	file			The <CODE>File</CODE> to be parsed.
 	 * @param 	errorHandler	The <CODE>ErrorHandler</CODE> used to report parser errors.
 	 * @param 	validationErrorHandler	The <CODE>ValidationErrorHandler</CODE> used to semantic report issues.
 	 * @since	TFP 1.0
 	 */
 	public static boolean parseAndValidate (File file, ErrorHandler errorHandler, ValidationErrorHandler validationErrorHandler)
 	{
 		return (parseAndValidate (file, AllRules.getRules (), errorHandler, validationErrorHandler));
 	}
 
 	/**
 	 * Provides access to the <CODE>Catalog</CODE> instance to be used for
 	 * entity resolution. If the <CODE>-catalog</CODE> option was not specified
 	 * then the result will be <CODE>null</CODE>
 	 * 
 	 * @return	The <CODE>Catalog</CODE> instance or <CODE>null</CODE>.
 	 * @since	TFP 1.0
 	 */
 	protected static Catalog getCatalog ()
 	{
 		return (catalog);
 	}
 	
 	/**
 	 * A <CODE>Logger</CODE> instance used to report serious errors.
 	 * @since	TFP 1.0
 	 */
 	private static Logger	logger
 		= Logger.getLogger ("com.handcoded.fpml.FpMLUtility");
 
 	/**
 	 * The catalog used to resolve DTD and schema references.
 	 * @since	TFP 1.0
 	 */
 	private static Catalog		catalog;
 
 	/**
 	 * The schema collection used to validate schema based documents.
 	 * @since	TFp 1.0
 	 */
 	private static SchemaSet	schemaSet = new SchemaSet ();
 		
 	/**
 	 * Ensures no instances can be created
 	 * @since	TFP 1.0
 	 */
 	private FpMLUtility()
 	{ }
 	
 	/**
 	 * This routine attempts to resolve a schemas target namespace through the
 	 * catalog to its external location. A <CODE>StreamSource</CODE> is created for
 	 * each schema add to the <CODE>sources</CODE> vector.
 	 * 
 	 * @param 	release			The <CODE>SchemaRelease</CODE> to resolve.
 	 * @since	TFP 1.0
 	 */
 	private static StreamSource resolveSchema (SchemaRelease release)
 	{
 		try {
 			StreamSource	source = catalog.resolveUri (release.getNamespaceUri());
 			
 			if (source == null) {
 				logger.severe ("Failed to resolve schema URI '" + release.getNamespaceUri() + "'");
 				source = new StreamSource (release.getSchemaLocation ());
 			}
 			return (source);
 		}
 		catch (SAXException error) {
 			logger.log (Level.SEVERE, "Unexpected SAX exception creating schema source", error);
 			System.exit (2);
 		}
 		return (null);
 	}
 	
 	/**
 	 * Construct a catalog and schema collection when this class is first
 	 * accessed.
 	 */
 	static {
 		try {
 			catalog = CatalogManager.find ("files/catalog.xml");
 		}
 		catch (SAXException error) {
 			logger.log (Level.SEVERE, "Unexpected SAX exception loading schema catalog", error);
 			System.exit (2);
 		}
 	
 		schemaSet.add (resolveSchema (com.handcoded.dsig.Releases.R1_0));
 		schemaSet.add (resolveSchema (com.handcoded.fpml.Releases.R4_0));
 		schemaSet.add (resolveSchema (com.handcoded.fpml.Releases.R4_1));
 		schemaSet.add (resolveSchema (com.handcoded.fpml.Releases.TR4_2));
 		
 		schemaSet.add (resolveSchema (com.handcoded.acme.Releases.R1_0));
 	}
 }
