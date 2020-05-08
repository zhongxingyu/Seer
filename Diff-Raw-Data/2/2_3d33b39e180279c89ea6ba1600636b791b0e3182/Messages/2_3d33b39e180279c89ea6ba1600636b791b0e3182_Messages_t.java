 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.taglibprocessing.attributevalues;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Message bundle for attributevalue types/features
  */
 public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jst.jsf.taglibprocessing.attributevalues.messages"; //$NON-NLS-1$
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 	/**
 	 * see messages.properties
 	 */
 	public static String ActionType_navcase_display;
     /**
      * see messages.properties
      */
 	public static String ActionType_invalid_value;
     /**
      * see messages.properties
      */
 	public static String BooleanType_invalid_values;
     /**
      * see messages.properties
      */
 	public static String ComponentBindingType_invalid_value;
     /**
      * see messages.properties
      */
 	public static String ComponentIDType_invalid_as_el;
     /**
      * see messages.properties
      */
 	public static String ComponentIDType_invalid_value;
     /**
      * see messages.properties
      */
 	public static String DoubleType_invalid_double;
     /**
      * see messages.properties
      */
 	public static String DoubleType_invalid_member;
     /**
      * see messages.properties
      */
 	public static String FacesConfigConverterIDFeatures_converterid_empty;
     /**
      * see messages.properties
      */
 	public static String FacesConfigIdentifierType_invalid_converter_id;
     /**
      * see messages.properties
      */
 	public static String FacesConfigIdentifierType_invalid_validator_id;
     /**
      * see messages.properties
      */
 	public static String FacesConfigValidatorIDFeatures_validatorid_empty;
     /**
      * see messages.properties
      */
 	public static String IntegerType_invalid_integer;
     /**
      * see messages.properties
      */
 	public static String IntegerType_invalid_member;
     /**
      * see messages.properties
      */
 	public static String JavaClassType_invalid_type;
     /**
      * see messages.properties
      */
 	public static String JavaClassType_not_found;
     /**
      * see messages.properties
      */
 	public static String LongType_invalid_long;
     /**
      * see messages.properties
      */
 	public static String LongType_invalid_member;
     /**
      * see messages.properties
      */
 	public static String MethodBindingType_invalid_value;
     /**
      * see messages.properties
      */
 	public static String NumberType_max_val;
     /**
      * see messages.properties
      */
 	public static String NumberType_min_val;
     /**
      * see messages.properties
      */
 	public static String StringType_invalid_value;
     /**
      * see messages.properties
      */
 	public static String ValueType_invalid_value;
     /**
      * see messages.properties
      */
 	public static String ValueType_invalid_value_without_setter;
 }
