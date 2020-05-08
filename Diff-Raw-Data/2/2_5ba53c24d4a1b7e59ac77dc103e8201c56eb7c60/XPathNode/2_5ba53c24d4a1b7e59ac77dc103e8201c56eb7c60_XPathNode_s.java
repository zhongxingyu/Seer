 // Copyright (C),2005-2011 HandCoded Software Ltd.
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
 
 package com.handcoded.classification.xml;
 
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Element;
 
 /**
  * 
  * @author 	BitWise
  * @version	$Id$
  * @since	TFP 1.6
  */
 final class XPathNode extends ExprNode
 {
 	/**
 	 * Constructs an <CODE>XPathNode</CODE> that will execute the indicated
 	 * XPath expression on test <CODE>Element</CODE> instances. The path should
 	 * use the prefix 'dyn' for elements that will be dynamically associated
 	 * with a namespace derived from the context.
 	 * 
 	 * @param 	test			The XPath expression
 	 * @since	TFP 1.6
 	 */
 	public XPathNode (final String test)
 	{
 		this.test = test;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @since	TFP 1.6
 	 */
 	@Override
 	public boolean evaluate (final Object context)
 	{
 		synchronized (xpath) {
 			try {
 				xpath.setNamespaceContext (new DynamicNamespaceContext ((Element) context));
 				Object result = xpath.evaluate (test, context, XPathConstants.BOOLEAN);
 				
 				return (((Boolean) result).booleanValue ());
 			}
 			catch (XPathExpressionException error) {
				logger.log (Level.SEVERE, "Failed to evaluate XPath", error);
 			}
 			return (false);
 		}
 	}
 		
 	/**
 	 * A <CODE>Logger</CODE> instance used to report serious errors.
 	 * @since	TFP 1.6
 	 */
 	private static Logger	logger
 		= Logger.getLogger ("com.handcoded.classification.xml.XPathNode");
 
 	/**
 	 * The <CODE>XPathFactory</CODE> used to create <CODE>XPath</CODE> instances.
 	 * @since	TFP 1.6
 	 */
 	private static XPathFactory	factory = XPathFactory.newInstance ();
 	
 	/**
 	 * The XPath expression that will be evaluated against the context element.
 	 * @since	TFP 1.6
 	 */
 	private final String		test;
 	
 	/**
 	 * The <CODE>XPath</CODE> instance used to evaluate the expression.
 	 * One instance is shared by all executing threads and is locked before
 	 * use.
 	 * @since	TFP 1.6
 	 */
 	private XPath				xpath	= factory.newXPath ();
 	
 	/**
 	 * 
 	 * @author 	BitWise
 	 * @since	TFP 1.6
 	 */
 	private static class DynamicNamespaceContext implements NamespaceContext
 	{
 		/**
 		 * Constructs a <CODE>DynamicNamespaceContext</CODE> based on the
 		 * indicated context <CODE>Element</CODE>.
 		 * 
 		 * @param 	context			The context <CODE>Element</CODE>.
 		 * @since	TFP 1.6
 		 */
 		public DynamicNamespaceContext (Element context)
 		{
 			this.context = context;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 * @since	TFP 1.6
 		 */
 		public String getNamespaceURI (String prefix)
 		{
 			if (prefix.equals ("dyn"))
 				return (context.getNamespaceURI ());
 			
 			return (null);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * @since	TFP 1.6
 		 */
 		public String getPrefix (String namespaceURI)
 		{
 			if (namespaceURI.equals (context.getNamespaceURI ()))
 				return ("dyn");
 			
 			return null;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * Dummy implementation. Always returns <CODE>null</CODE>.
 		 * 
 		 * @since	TFP 1.6
 		 */
 		public Iterator getPrefixes (String namespaceURI)
 		{
 			return null;
 		}
 		
 		/**
 		 * The context element to derive the namespaces from.
 		 * @since	TFP 1.6
 		 */
 		private final Element	context;
 	}
 }
