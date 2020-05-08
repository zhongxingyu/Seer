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
 
 package org.eclipse.jst.jsf.metadataprocessors.internal;
 
 /**
  * Registry of <code>AbstractMetaDataEnabledType</code>s that are loaded from 
  * the <code>AttributeValueRuntimeValueType</code> extension point
  * 
  * @author Gerry Kessler - Oracle
  *
  */
 public class AttributeValueRuntimeTypeRegistry extends AbstractMetaDataEnabledTypeRegistry {
 
 	private static final String EXTPTID = "AttributeValueRuntimeTypes";
	private static final String DEFAULT_CLASS = "org.eclipse.jst.jsf.metadataprocessors.internal.provisional.DefaultTypeDescriptor";
 	private static AttributeValueRuntimeTypeRegistry INSTANCE;
 	
 	/**
 	 * @return singleton instance
 	 */
 	public static AttributeValueRuntimeTypeRegistry getInstance(){
 		if (INSTANCE == null){
 			INSTANCE = new AttributeValueRuntimeTypeRegistry();	
 		}
 		return INSTANCE;
 	}
 	
 	private AttributeValueRuntimeTypeRegistry(){
 		super(EXTPTID);
 	}
 
 	protected String getDefaultClassName() {
 		return DEFAULT_CLASS;
 	}
 
 }
