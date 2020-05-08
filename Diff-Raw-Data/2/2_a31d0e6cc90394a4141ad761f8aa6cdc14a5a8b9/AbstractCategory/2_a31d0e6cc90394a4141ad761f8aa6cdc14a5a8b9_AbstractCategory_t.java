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
 
 package com.handcoded.classification;
 
 import java.util.Enumeration;
 import java.util.HashSet;
 
 /**
  * An <CODE>AbstractCategory</CODE> is used to relate a set of sub-category
  * instances.
  *
  * @author	BitWise
  * @version	$Id$
  * @since	TFP 1.0
  */
 public class AbstractCategory extends Category
 {
 	/**
 	 * Construct an <CODE>AbstractCategory</CODE> with a given name.
 	 * 
 	 * @param 	name			The name of the <CODE>Category</CODE>.
 	 * @since	TFP 1.0
 	 */
 	public AbstractCategory (final String name)
 	{
 		super (name);
 	}
 	
 	/**
 	 * Construct an <CODE>AbstractCategory</CODE> that is a sub-classification
 	 * of another <CODE>Category</CODE>.
 	 * 
 	 * @param 	name			The name of the <CODE>Category</CODE>.
 	 * @param 	parent			The parent <CODE>Category</CODE>.
 	 * @since	TFP 1.0
 	 */
 	public AbstractCategory (final String name, Category parent)
 	{
 		super (name, parent);
 	}
 	
 	/**
 	 * Construct an <CODE>AbstractCategory</CODE> that is a sub-classification
 	 * of other <CODE>Category</CODE> instances.
 	 * 
 	 * @param	name			The name of the <CODE>Category</CODE>.
 	 * @param 	parents			The parent <CODE>Category</CODE> instances.
 	 * @since	TFP 1.0
 	 */
 	public AbstractCategory (final String name, Category [] parents)
 	{
 		super (name, parents);
 	}
 	
 	/**
	 * {@inheritDoc}
 	 */
 	protected Category classify (final Object value, HashSet visited)
 	{
 		Category			result	= null;
 		Enumeration			cursor	= subCategories.elements ();
 		
 		visited.add (this);
 		while (cursor.hasMoreElements ()) {
 			Category 			category = (Category)(cursor.nextElement ());
 			Category			match;
 
 			if (!visited.contains (category) && (match = category.classify (value)) != null) {
 				if ((result != null) && (result != match))
 					throw new RuntimeException ("Object cannot be unambiguously classified ("
 													+ result + " & " + match + ")");
 
 				result = match;
 			}
 		}
 		visited.remove (this);
 		return (result);
 	}	
 }
