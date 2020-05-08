 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.spec;
 
 import org.xins.common.types.Type;
 
 /**
  * Specification of the parameter.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class Parameter {
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
    
    /**
     * Creates a new instance of Parameter.
     *
     * @param reference
     *    the reference class.
     * @param name
     *    the name of the parameter.
     * @param type
     *    the type of the parameter.
     * @param required
     *    <code>true</code> if the parameter is required, <code>false</code> otherwise.
     * @param description
     *    the description of the parameter.
     */
    Parameter(Class reference, String name, String type, boolean required, String description) {
       _reference = reference;
       _parameterName = name;
       _parameterType = type;
       _required = required;
       _description = description;
    }
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
    
    /**
     * The reference class.
     */
    private final Class _reference;
    
    /**
     * Name of the parameter.
     */
    private final String _parameterName;
    
    /**
     * Type of the parameter.
     */
    private final String _parameterType;
    
    /**
     * Flags indicating if this parameter is required.
     */
    private final boolean _required;
    
    /**
     * Description of the parameter.
     */
    private String _description;
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
    
    /**
     * Gets the name of the parameter.
     *
     * @return
     *    The name of the parameter, never <code>null</code>.
     */
    public String getName() {
       
       return _parameterName;
    }
    
    /**
     * Gets the description of the parameter.
     *
     * @return
     *    The description of the parameter, never <code>null</code>.
     */
    public String getDescription() {
       
       return _description;
    }
 
    /**
     * Returns whether the parameter is mandatory.
     *
     * @return
     *    <code>true</code> if the parameter is requierd, <code>false</code> otherwise.
     */
    public boolean isRequired() {
       
       return _required;
    }
 
    /**
     * Gets the type of the parameter.
     *
     * @return
     *    The type of the parameter, never <code>null</code>.
     *
     * @throws InvalidSpecificationException
     *    If the type is not recognized.
     */
    public Type getType() throws InvalidSpecificationException {
       
       if (_parameterType == null || _parameterType.equals("") || _parameterType.equals("_text")) {
          return org.xins.common.types.standard.Text.SINGLETON;
       } else if (_parameterType.equals("_int8")) {
          return org.xins.common.types.standard.Int8.SINGLETON;
       } else if (_parameterType.equals("_int16")) {
          return org.xins.common.types.standard.Int16.SINGLETON;
       } else if (_parameterType.equals("_int32")) {
          return org.xins.common.types.standard.Int32.SINGLETON;
       } else if (_parameterType.equals("_int64")) {
          return org.xins.common.types.standard.Int64.SINGLETON;
       } else if (_parameterType.equals("_float32")) {
          return org.xins.common.types.standard.Float32.SINGLETON;
       } else if (_parameterType.equals("_float64")) {
          return org.xins.common.types.standard.Float64.SINGLETON;
       } else if (_parameterType.equals("_boolean")) {
          return org.xins.common.types.standard.Boolean.SINGLETON;
       } else if (_parameterType.equals("_date")) {
          return org.xins.common.types.standard.Date.SINGLETON;
       } else if (_parameterType.equals("_timestamp")) {
          return org.xins.common.types.standard.Timestamp.SINGLETON;
       } else if (_parameterType.equals("_base64")) {
          return org.xins.common.types.standard.Base64.SINGLETON;
       } else if (_parameterType.equals("_descriptor")) {
          return org.xins.common.types.standard.Descriptor.SINGLETON;
       } else if (_parameterType.equals("_properties")) {
          return org.xins.common.types.standard.Properties.SINGLETON;
       } else if (_parameterType.equals("_url")) {
          return org.xins.common.types.standard.URL.SINGLETON;
       } else if (_parameterType.charAt(0) != '_') {
          String className = _reference.getName();
          int truncatePos = className.lastIndexOf(".capi.CAPI");
          if (truncatePos == -1) {
             truncatePos = className.lastIndexOf(".api.APIImpl");
          }
          try {
             String typeClassName = className.substring(0, truncatePos) + ".types." + _parameterType + ".class";
            Class typeClass = Class.forName(typeClassName);
             Type type = (Type) typeClass.getField("SINGLETON").get(null);
             return type;
          } catch (Exception ex) {
             throw new InvalidSpecificationException("Invalid type: " + _parameterType + " ; " + ex.getMessage());
          }
       }
       throw new InvalidSpecificationException("Invalid type: " + _parameterType + ".");
    }
 }
