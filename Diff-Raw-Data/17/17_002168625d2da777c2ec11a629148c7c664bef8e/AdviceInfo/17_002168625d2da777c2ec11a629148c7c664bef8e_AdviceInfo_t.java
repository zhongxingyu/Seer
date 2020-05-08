 package org.jboss.aop.advice.annotation;
 
 import java.lang.reflect.Method;
 
 import org.jboss.aop.advice.AdviceMethodProperties;
 import org.jboss.aop.advice.annotation.AdviceMethodFactory.ReturnType;
 
 /**
  * Contains information about an advice method and its matching process.
  * 
  * @author Flavia Rainone
  */
 abstract class AdviceInfo implements Comparable<AdviceInfo>
 {
    // the righest the rank the better this advice is
    protected int rank;
    // advice method
    protected Method method;
    // since method.getParameterTypes creates a vector, better store this information
    // instead of calling repeatedly getParameterTypes
    protected Class<?>[] parameterTypes;
    
    /**
     * Creates an advice info.
     * 
     * @param method the advice method
     * @param rank   the initial rank value of this advice
     */
    protected AdviceInfo(Method method, int rank)
    {
       this.method = method;
       this.rank = rank;
       this.parameterTypes = method.getParameterTypes();
    }
    
    /**
     * Returns the assignability degree from <code>fromType</code> to </code>toType</code>. 
     * <p>
     * The assignability degree is the distance in class and interface hierarchy between
     * <code>fromType</code> and </code>toType</code>. If <code>toType</code> is an
     * interface implemented by <code>fromType</code>, then the degree is 1. Otherwise,
     * the degree is exactly the number of hierarchical levels between <code>fromType
     * </code> and <code>toType</code>.
     * 
     * @param fromType the type from which <code>toType</code> is supposedly assignable.
     * @param toType   the type to which <code>fromType</code> can be converted without
     *                 type casting.
     * @return {@link AdviceMethodFactory#NOT_ASSIGNABLE_DEGREE if <code>toType</code> is
     *         not assignable from <code>fromType</code>; the hierarchical distance between
     *         <code>fromType</code> and <code>toType</code> otherwise.
     *         
     * @see java.lang.Class#isAssignableFrom(Class)
     */
    protected short getAssignabilityDegree(Class<?> fromType, Class<?> toType)
    {
       // they're the same
       if (fromType == toType)
       {
          return 0;
       }
       if (toType.isInterface())
       {
          if (fromType.isInterface())
          {
             // assignability degree on interface inheritance
             return getInterfaceInheritanceAD(fromType, toType, (short) 0);
          }
          else
          {
             // assignability degree on interface implementation
             return getImplementationAD(fromType, toType);
          }
       }
       if (fromType.isInterface())
       {
          // you can't get to a class from an interface
          return AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE;
       }
       // assignability degree on class inheritance
       return getClassInheritanceAD(fromType.getSuperclass(), toType, (short) 1);
    }
    
    /**
     * Returns the assignability degree between <code>fromClassType</code> and <code>
     * toInterfaceType</code>.
     * 
     * @param fromClassType   a class type that supposedly implements <code>
     *                        toInterfaceType</code>
     * @param toInterfaceType an interface type that is supposedly implemented by <code>
     *                        fromClassType</code>
     * @return {@link AdviceMethodFactory#NOT_ASSIGNABLE_DEGREE} if <code>fromClassType
     *         </code> does not implement <code>toInterfaceType</code>; otherwise, if
     *         <code>fromType</code> implements a subinterface of <code>toInterfaceType
     *         </code>, returns 1 + the assignability degree between this subinterface and
     *         <code>toType</code>; otherwhise, returns 1.
     *         
     */
    private short getImplementationAD(Class<?> fromClassType, Class<?> toInterfaceType)
    {
       if (fromClassType == null)
       {
          return AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE;
       }
       
       Class[] interfaces = fromClassType.getInterfaces();
       for (int i = 0 ; i < interfaces.length ; i++)
       {
          if(interfaces[i] == toInterfaceType)
          {
             return 1;
          }
       }
       short currentDegree = AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE;
       for (int i = 0 ; i < interfaces.length ; i++)
       {
          currentDegree = (short) Math.min(getInterfaceInheritanceAD(interfaces[i],
                toInterfaceType, (short) 1), currentDegree);
       }
       if (currentDegree == AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE)
       {
          return getImplementationAD(fromClassType.getSuperclass(), toInterfaceType);
       }
       return currentDegree;
    }
    
    /**
     * Recursive method that returns the assignability degree on an interface inheritance.
     * 
     * @param fromInterfaceType  the interface that supposedly inherits (directly or
     *                           indirectly <code>toInterfaceType</code>.
     * @param toInterfaceType    the interface which is supposedly assignable from <code>
     *                           fromInterfaceType</code>. The type <code>
     *                           toInterfaceType</code> is not the same as <code>
     *                           fromInterfaceType</code>.
     * @param currentDegree      the current assignability degree
     * @return                   {@link AdviceMethodFactory#NOT_ASSIGNABLE_DEGREE} if
     *                           <code>toInterfaceType</code> is not assignable from <code>
     *                           fromInterfaceType</code>; <code>currentDegree + </code>
     *                           the assignability degree from <code>fromInterfaceType
     *                           </code> to <code>toInterfaceType</code>.
     */
    public short getInterfaceInheritanceAD(Class<?> fromInterfaceType,
          Class<?> toInterfaceType, short currentDegree)
    {
       Class[] interfaces = fromInterfaceType.getInterfaces();
       currentDegree ++;
       for (int i = 0; i < interfaces.length; i++)
       {
          if(interfaces[i] == toInterfaceType)
          {
             return currentDegree;
          }
       }
       short bestDegree = AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE;
       for (int i = 0; i < interfaces.length; i++)
       {
          bestDegree = (short) Math.min(getInterfaceInheritanceAD(interfaces[i],
                toInterfaceType, currentDegree), bestDegree);
       }
       return bestDegree;
    }
    
    /**
     * Recursive method that returns the assignability degree on an class inheritance.
     * 
     * @param fromClassType  the class that supposedly inherits (directly or
     *                       indirectly <code>toClassType</code>.
     * @param toClassType    the class which is supposedly assignable from <code>
     *                       fromInterfaceType</code>. The type <code>toClassType</code> is
     *                       not the same as <code>fromClassType</code>.
     * @param currentDegree  the current assignability degree
     * @return               {@link AdviceMethodFactory#NOT_ASSIGNABLE_DEGREE} if <code>
     *                       toClassType</code> is not assignable from <code>fromClassType
     *                       </code>; <code>currentDegree + </code> the assignability
     *                       degree from <code>fromClassType</code> to <code>toClassType
     *                       </code>.
     */
    private short getClassInheritanceAD(Class<?> fromClassType, Class<?> toClassType,
          short currentDegree)
    {
       if (fromClassType == null)
       {
          return AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE;
       }
       if (fromClassType == toClassType)
       {
          return currentDegree;
       }
       return getClassInheritanceAD(fromClassType.getSuperclass(), toClassType,
             ++currentDegree);
    }
 
    /**
     * Returns the distance in hierarchy between the annotated parameter identified by
     * <code>annotationIndex</code>, and the expected type of this parameter.
     * 
     * @param annotationIndex  identifies a parameter annotation rule
     * @param properties       contains information about the queried advice method
     * @return                 the assignability degree if there is a parameter with the
     *                         annotation identified by <code>typeIndex</code>;
     *                         {@link AdviceMethodFactory#NOT_ASSIGNABLE_DEGREE} otherwise.
     */
   public final short getReturnAssignabilityDegree(AdviceMethodProperties properties)
    {
       Class returnType = this.method.getReturnType();
       if (returnType == void.class)
       {
          return AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE;
       }
       short degree = this.getAssignabilityDegree(returnType,
             properties.getJoinpointReturnType());
       if (degree == AdviceMethodFactory.NOT_ASSIGNABLE_DEGREE)
       {
          // return type is Object.class and join point return type is not
         // Object is worse than join point return type, but better than NOT_ASSIGNABLE
          return AdviceMethodFactory.MAX_DEGREE;
       }
       return degree;
    }
 
    /**
     * Returns the rank of this advice.
     * @return the rank value
     */
    public final int getRank()
    {
       return rank;
    }
    
    /**
     * Compares this advice info against <code>o</code> in decreasing order of the rank
     * value.
     */
    public int compareTo(AdviceInfo o)
    {
       return ((AdviceInfo)o).rank - rank;
    }
    
    public String toString()
    {
       return method.toString();
    }
    
    /**
     * Validate this advice, indicating whether it can be the answer to the method query
     * contained in <code>properties</code>.
     * 
     * @param properties        contains information about the queried method
     * @param mutuallyExclusive a list of mutually exclusive rules
     * @param returnType        the expected return type
     * @return                  <code>true</code> only if this advice is valid
     */
    public abstract boolean validate(AdviceMethodProperties properties,
          int[][] mutuallyExclusive, ReturnType returnType);
 
    /**
     * Returns the distance in hierarchy between the annotated parameter identified by
     * <code>annotationIndex</code>, and the expected type of this parameter.
     * 
     * @param annotationIndex  identifies a parameter annotation rule
     * @param properties       contains information about the queried advice method
     * @return                 the assignability degree if there is a parameter with the
     *                         annotation identified by <code>typeIndex</code>;
     *                         {@link AdviceMethodFactory#NOT_ASSIGNABLE_DEGREE} otherwise.
     */
    public abstract short getAssignabilityDegree(int annotationIndex,
          AdviceMethodProperties properties);
    
    /**
     * Assign information of this advice to <code>properties</code>.
     * 
     * @param properties contains information about the queried advice method.
     */
    public abstract void assignAdviceInfo(AdviceMethodProperties properties);
 }
