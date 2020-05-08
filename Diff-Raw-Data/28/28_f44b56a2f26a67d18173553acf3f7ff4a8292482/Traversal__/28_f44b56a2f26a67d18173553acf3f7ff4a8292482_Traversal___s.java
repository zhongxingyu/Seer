 package de.unifr.acp.runtime;
 
 public interface Traversal__ {
 
     /**
      * Visits the specified reference field of the specified object. The field's
      * static type is a subtype of Object such that the field's dynamic
      * value is a known not to be a reference array.
     * It might be a one or multidimensional primitive array but not primitive. 
      * 
      * @param obj
      *            the object containing the specified field (null in case of a
      *            static field)
      * @param fieldName
      *            the fully qualified field name
      * @param fieldvalue
      *            the non-primitive value of the field
      */
     public void visitField__(Object obj, String fieldName, Object fieldvalue);
 
     /**
      * Visits the specified reference field of the specified object. Only call
      * this method if the dynamic type of the field value can but is not know to
      * be a reference array (static type is object). Call
      * {@link #visitField__(Object, String, Object)},
      * {@link #visitPrimitiveField__(Object, String)}, or
      * {@link #visitArrayField__(Object, String, Object[])} otherwise.
      * 
      * @param obj
      *            the object containing the specified field (null in case of a
      *            static field)
      * @param fieldName
      *            the fully qualified field name
      * @param fieldvalue
      *            the non-primitive value of the field
      */
     public void visitPotentialRefArrayField__(Object obj, String fieldName,
             Object fieldValue);
 
     /**
      * Visits the specified array field of the specified object.
      * 
      * @param obj
      *            the object containing the specified field (null in case of a
      *            static field)
      * @param fieldName
      *            the fully qualified field name
      * @param fieldvalue
      *            the value of the field
      */
     public void visitArrayField__(Object obj, String fieldName,
             Object[] fieldValue);
 
     /**
      * Overloaded version as fallback (needed due to Javassist method dispatch
      * limitations - Object[][] is not regarded as subtype of Object[] but of
      * Object).
      * 
      * @param fieldValue
      *            the value of the field of type Object due to method dispatch
      *            limitations in Javassist. The caller needs to make sure this
      *            argument's dynamic type is an Object[]
      * @see Traversal__#visitArrayField__(Object, String, Object[])
      */
     public void visitArrayField__(Object obj, String fieldName,
             Object fieldValue);
 
     /**
      * Overloaded version to speed up array traversal.
      * 
      * @param the
      *            value of the field
      * @see Traversal__#visitArrayField__(Object, String, Object[])
      */
     public void visitArrayField__(Object obj, String fieldName,
             Object[][] fieldValue);
 
     /**
      * Overloaded version to speed up array traversal.
      * 
      * @param the
      *            value of the field
      * @see Traversal__#visitArrayField__(Object, String, Object[])
      */
     public void visitArrayField__(Object obj, String fieldName,
             Object[][][] fieldValue);
 
     /**
      * Visits a the specified primitive field of the
      * specified object. The field's static type is a primitive type.
      * 
      * @param obj
      *            the object containing the specified field (null in case of a
      *            static field)
      * @param fieldName
      *            the fully qualified field name
      */
     public void visitPrimitiveField__(Object obj, String fieldName);
 }
