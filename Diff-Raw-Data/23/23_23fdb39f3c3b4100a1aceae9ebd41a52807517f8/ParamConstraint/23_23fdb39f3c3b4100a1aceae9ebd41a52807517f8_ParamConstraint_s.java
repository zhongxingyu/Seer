 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Constraint on a single parameter.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
abstract class ParamConstraint
 extends Constraint {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = ParamConstraint.class.getName();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>ParamConstraint</code> for a parameter with the
     * specified name.
     *
     * @param name
     *    the parameter name, cannot be <code>null</code> and cannot be an
     *    empty string.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || name.length() &lt; 1</code>.
     */
    ParamConstraint(String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
       if (name.length() < 1) {
          throw new IllegalArgumentException("name.length() == 0");
       }
 
       // Store information
       _name = name;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of the parameter. This value of this field can neither be
     * <code>null</code> nor an empty string.
     */
    private final String _name;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Validates this constraint in the specified context (implementation
     * method). If this constraint is violated, then <code>false</code> is
     * returned, otherwise <code>true</code> is returned.
     *
     * <p>This method should only ever be called from
     * {@link #check(ConstraintContext)}.
     *
     * <p>The implementation of this method in class {@link ParamConstraint}
    * delegates to {@link #checkParameterValue(String)}.
     *
     * @param context
     *    the context for the validation, guaranteed not to be
     *    <code>null</code>.
     *
     * @return
     *    flag that indicates if this constraint was violated,
     *    <code>true</code> if it was not, and <code>false</code> if it was.
     */
    final boolean checkImpl(ConstraintContext context) {
       return checkParameterValue(context.getParameter(_name));
    }
 
    /**
     * Checks if the specified value is allowable. If this constraint is
     * violated, then <code>false</code> is returned, otherwise
     * <code>true</code> is returned.
     *
     * <p>This method should only ever be called from
     * {@link #checkImpl(ConstraintContext)}.
     *
     * @param value
     *    the value for the parameter, possibly <code>null</code>.
     *
     * @return
     *    flag that indicates if this constraint was violated,
     *    <code>true</code> if it was not, and <code>false</code> if it was.
     */
    abstract boolean checkParameterValue(Object value);
 }
