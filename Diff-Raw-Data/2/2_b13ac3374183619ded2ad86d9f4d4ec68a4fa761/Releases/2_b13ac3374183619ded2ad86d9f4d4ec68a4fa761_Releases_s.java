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
 
 package com.handcoded.acme;
 
 import com.handcoded.meta.SchemaRelease;
 import com.handcoded.meta.Specification;
 
 /**
  * The <CODE>Releases</CODE> class contains a set of static objects describing
  * the HandCoded Acme extension schemas.
  * 
  * @author	BitWise
  * @version	$Id$
  * @since	TFP 1.0
  */
 public final class Releases
 {
 	/**
 	 * A <CODE>Specification</CODE> instance representing Acme extension schemas
 	 * as a whole.
 	 * @since	TFP 1.0
 	 */
 	public static Specification	ACME
 		= new Specification ("Acme");
 	
 	/**
 	 * A <CODE>SchemaRelease</CODE> instance containing the details for the
 	 * Acme 1-0 schema.
 	 * @since	TFP 1.0
 	 */
 	public static SchemaRelease	R1_0
 		= new SchemaRelease (ACME, "1-0",
 				"http://www.handcoded.com/spec/2005/Acme-1-0", "acme-1-0.xsd",
 				"acme", null);
 	
 	/**
 	 * A <CODE>SchemaRelease</CODE> instance containing the details for the
 	 * Acme 2-0 schema.
 	 * @since	TFP 1.0
 	 */
 	public static SchemaRelease	R2_0
 		= new SchemaRelease (ACME, "2-0",
				"http://www.handcoded.com/spec/2005/Acme-2-0", "acme-2-0.xsd",
 				"acme", null);
 	
 	/**
 	 * Ensures no instances can be constructed.
 	 * @since	TFP 1.0
 	 */
 	private Releases ()
 	{ }
 	
 	/**
 	 * Add a schema import link between the Acme extension schemas and the
 	 * version of FpML they relate to.
 	 */
 	static {
 		R1_0.addImport (com.handcoded.fpml.Releases.R4_0);
 		R2_0.addImport (com.handcoded.fpml.Releases.TR4_2);
 	}
 }
