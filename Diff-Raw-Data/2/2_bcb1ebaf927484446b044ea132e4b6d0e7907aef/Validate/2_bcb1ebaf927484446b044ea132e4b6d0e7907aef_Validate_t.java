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
 
 package demo.com.handcoded.fpml;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Node;
 import org.xml.sax.SAXParseException;
 
 import com.handcoded.fpml.FpMLUtility;
 import com.handcoded.fpml.validation.AllRules;
 import com.handcoded.fpml.validation.FpMLRules;
 import com.handcoded.framework.Option;
 import com.handcoded.validation.RuleSet;
 import com.handcoded.xml.XPath;
 
 /**
  * This application demonstrates the validation components being used to
  * perform business level validation of an FpML document.
  * 
  * @author	BitWise
  * @version	$Id$
  * @since	TFP 1.0
  */
 public final class Validate extends Application
 {
 	/**
 	 * Creates an application instance and invokes its <CODE>run</CODE>
 	 * method passing the command line arguments.
 	 * 
 	 * @param 	arguments		The command line arguments
 	 * @since	TFP 1.0
 	 */
 	public static void main (String [] arguments)
 	{   
 		new Validate ().run (arguments);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @since	TFP 1.0
 	 */
 	protected void startUp ()
 	{
 		super.startUp ();
 		
 		if (repeatOption.isPresent ()) {
 			repeat = Integer.parseInt (repeatOption.getValue ());
 			if (repeat <= 0) {
 				logger.severe ("The repeat count must be >= 1");
 				System.exit (1);
 			}
 		}
 		random = randomOption.isPresent ();
 
 		if (getArguments ().length == 0) {
 			logger.severe ("No files are present on the command line");
 			System.exit (1);
 		}
 		
		FpMLUtility.getSchemas ();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @since	TFP 1.0
 	 */
 	protected void execute ()
 	{
 		RuleSet					rules = strictOption.isPresent () ? FpMLRules.getRules() : AllRules.getRules ();
 		String []				arguments = getArguments ();
 		ParserErrorHandler		parserErrorHandler = new ParserErrorHandler ();
 		ValidationErrorHandler	validationErrorHandler = new ValidationErrorHandler ();
 		boolean					schemaOnly = schemaOnlyOption.isPresent();
 		int						count = 0;
 		
 		try {
 			long start = System.currentTimeMillis();
 	
 			while (repeat-- > 0) {
 				for (int index = 0; index < arguments.length; ++index) {
 					int		which = random ? (int)(Math.random () * arguments.length ) : index;
 					
 					System.err.println (">> " + arguments [which]);
 					
 					FpMLUtility.parseAndValidate (schemaOnly, new File (arguments [which]), rules, parserErrorHandler, validationErrorHandler);
 					++count;
 				}
 			}
 			
 			long end = System.currentTimeMillis ();
 			
 			System.err.println ("== Processed " + count + " files in "
 				+ (end - start) + " milliseconds");
 			System.err.println ("== " + ((1000.0 * count) / (end - start))
 				+ " files/sec checking " + rules.size () + " rules");		
 		}
 		catch (Exception error) {
 			logger.log (Level.SEVERE, "Unexpected exception during processing", error);
 		}
 		
 		setFinished (true);
 	}
 	
 	/**
 	 * {@inheritDoc} 
 	 * @since	TFP 1.0
 	 */
 	protected String describeArguments ()
 	{
 		return (" files ...");
 	}
 	
 	/**
 	 * The <CODE>ParserErrorHandler</CODE> provides an implementation of
 	 * the SAX <CODE>ErrorHandler</CODE> interface used to report errors
 	 * during XML parsing. 
 	 * 
 	 * @since	TFP 1.0
 	 */
 	private static class ParserErrorHandler implements org.xml.sax.ErrorHandler
 	{
 		public void warning (SAXParseException error)
 		{
 			System.err.println (error.getMessage ());
 		}	
 		
 		public void error (SAXParseException error)
 		{
 			System.err.println (error.getMessage ());
 		}	
 
 		public void fatalError (SAXParseException error)
 		{
 			System.err.println (error.getMessage ());
 		}	
 	}
 	
 	/**
 	 * The <CODE>ValidationErrorHandler</CODE> implements the <CODE>ErrorHandler
 	 * </CODE> interface used by the validation toolkit to report semantic errors.
 	 * 
 	 * @since	TFP 1.0
 	 */
 	private static class ValidationErrorHandler implements com.handcoded.validation.ValidationErrorHandler
 	{
 		public void error (String code, Node context, String description, String ruleName, String additionalData)
 		{
 			if (additionalData != null)
 				System.err.println (ruleName + " " + XPath.forNode(context) + " " + description + " [" + additionalData + "]");
 			else
 				System.err.println (ruleName + " " + XPath.forNode(context) + " " + description);
 		}
 	}
 	
 	/**
 	 * A <CODE>Logger</CODE> instance used to report serious errors.
 	 * @since	TFP 1.0
 	 */
 	private static Logger	logger
 		= Logger.getLogger ("demo.com.handcoded.fpml.Validate");
 
 	/**
 	 * The <CODE>Option</CODE> instance use to detect <CODE>-repeat count</CODE>
 	 * @since	TFP 1.0
 	 */
 	private Option			repeatOption
 		= new Option ("-repeat", "Number of times to processes the files", "count");
 	
 	/**
 	 * The <CODE>Option</CODE> instance use to detect <CODE>-random</CODE>
 	 * @since	TFP 1.0
 	 */
 	private Option			randomOption
 		= new Option ("-random", "Pick files at random for processing");
 	
 	
 	/**
 	 * The <CODE>Option</CODE> instance use to detect <CODE>-strict</CODE>
 	 * @since	TFP 1.0
 	 */
 	private Option			strictOption
 		= new Option ("-strict", "Use only FpML defined rules (no extensions)");
 	
 	/**
 	 * The <CODE>Option</CODE> instance use to detect <CODE>-strict</CODE>
 	 * @since	TFP 1.0
 	 */
 	private Option			schemaOnlyOption
 		= new Option ("-schemaOnly", "Only accept schema based documents");
 	
 	/**
 	 * A counter for the number of time to reprocess the files.
 	 * @since	TFP 1.0
 	 */
 	private int				repeat = 1;
 	
 	/**
 	 * A flag indicating whether to randomise the file list.
 	 * @since	TFP 1.0
 	 */
 	private boolean			random = false;
 
 	/**
 	 * Constructs a <CODE>Validate</CODE> instance.
 	 * @since	TFP 1.0
 	 */
 	private Validate ()
 	{ }
 }
