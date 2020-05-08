 /*
  * $Id$
  */
 package org.xins.specs;
 
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Base class for specification for a parameter for a function. This is an
  * abstract base class.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class ParamSpec
 extends Spec {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Checks the arguments to the constructor and returns the first one.
     *
     * @param type
     *    the type of this specification object, not <code>null</code>.
     *
     * @param parent
     *    the function this parameter is part of, not <code>null</code>.
     *
     * @param name
     *    the name for the parameter, not <code>null</code>.
     *
     * @param paramType
     *    the type for the parameter, not <code>null</code>.
     *
     * @return
     *    the first argument, the type of this parameter specification object,
     *    so never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null || parent == null || name == null || paramType == null</code>.
     *
     * @throws InvalidNameException
     *    if <code>type.</code>{@link SpecType#isValidName(String) isValidName}<code>(name) == false</code>.
     */
    private static final Type checkArguments(Type         type,
                                             FunctionSpec parent,
                                             String       name,
                                             TypeSpec     paramType)
    throws IllegalArgumentException,
           InvalidNameException {
 
       // Check required arguments
       MandatoryArgumentChecker.check("type",      type,
                                      "parent",    parent,
                                      "name",      name,
                                      "paramType", paramType);
 
       // Check name string
       if (type.isValidName(name) == false) {
          throw new InvalidNameException(type, name);
       }
 
       // Return type
       return type;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ParamSpec</code> for a parameter of the specified
     * type and with the specified name.
     *
     * @param type
     *    the type of this specification object, not <code>null</code>.
     *
     * @param parent
     *    the function this parameter is part of, not <code>null</code>.
     *
     * @param name
     *    the name for the component, not <code>null</code>.
     *
     * @param paramType
     *    the type for the parameter, not <code>null</code>.
     *
     * @param required
     *    flag that indicates if this parameter is required or not.
     *
     * @throws IllegalArgumentException
     *    if <code>type == null || parent == null || name == null || paramType == null</code>.
     *
     * @throws InvalidNameException
     *    if <code>type.</code>{@link SpecType#isValidName(String) isValidName}<code>(name) == false</code>.
     */
    ParamSpec(Type         type,
              FunctionSpec parent,
              String       name,
              TypeSpec     paramType,
              boolean      required)
    throws IllegalArgumentException, InvalidNameException {
 
       // Check arguments and then call superconstructor
       super(checkArguments(type, parent, name, paramType),
             parent,
             name);
 
       // Store additional data
       _paramType = paramType;
       _required  = required;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The parameter type.
     */
    private final TypeSpec _paramType;
 
    /**
     * Flag that indicates if the parameter is required.
     */
    private final boolean _required;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Retrieves the type for the parameter.
     *
     * @return
     *    the parameter {@link TypeSpec}, never <code>null</code>.
     */
    public final TypeSpec getParamType() {
       return _paramType;
    }
 
    /**
     * Checks if the parameter is required.
     *
     * @return
     *    <code>true</code> if the parameter is required, <code>false</code>
     *    otherwise.
     */
    public final boolean isRequired() {
       return _required;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Base type for parameter component specification types.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     */
    static class Type extends SpecType {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Type</code> object.
        *
        * @param name
        *    the name for the type, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>name == null</code>.
        */
      Type(String name) {
          super(FunctionSpec.TYPE, name, "^[a-z]+[A-Za-z0-9]*$");
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
    }
 }
