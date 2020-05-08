 package org.jboss.aop.advice.annotation;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.advice.AdviceMethodProperties;
 import org.jboss.aop.advice.annotation.AdviceMethodFactory.ReturnType;
 
 /**
  * Information about an advice method whose parameters should annotated according to
  * <code>ParameterAnnotationRule</code>s.
  * 
  * @author Flavia Rainone
  */
 class AnnotatedParameterAdviceInfo extends AdviceInfo
 {
    // list that may contain temporary information regarding parameters with more than one
    // valid annotation. Field is static to avoid the cost of constantly creating a list
    private static List<Integer> extraAnnotations = new ArrayList<Integer>();
    
    // the annotated parameter types
    private ParameterAnnotationType paramTypes[];
    
    /**
     * Creates an annotated parameter advice info.
     * 
     * @param method the advice method
     * @param rules  the annnotated parameter rules this method should comply with
     * 
     * @throws ParameterAnnotationRuleException thrown when the advice method does not
     *         comply with a parameter annotation rule.
     * @throws MultipleAdviceInfoException thrown when one parameter contains more than one
     *         valid annotation. Indicates that there must be multiple advice infos to
     *         represent <code>method</code>.
     */
    public AnnotatedParameterAdviceInfo(Method method, ParameterAnnotationRule[] rules)
       throws ParameterAnnotationRuleException, MultipleAdviceInfoException
    {
       this(rules, method);
       this.applyRules();
    }
    
    /**
     * Private constructor. Called only internally, does not apply the parameter annotation
     * rules to the advice method parameters.
     * 
     * @param rules  the parameter annotation rules this method should comply with
     * @param method the advice method 
     */
    private AnnotatedParameterAdviceInfo(ParameterAnnotationRule[] rules, Method method)
    {
       super(method, 0);
       this.paramTypes = new ParameterAnnotationType[rules.length];
       // create appropriate annotated parameter types for each AP rule
       for (int i = 0; i < rules.length; i++)
       {
          if (rules[i].isSingleEnforced())
          {
             this.paramTypes[i] = new SingleParameterType(rules[i]);
          }
          else
          {
             this.paramTypes[i] = new MultipleParameterType(rules[i],
                   method.getParameterTypes().length);
          }
       }
    }
    
    /**
     * Creates all advice info instances necessary for representing the multi-annotated
     * parameters of <code>method</code>.
     * <p>Should be invoked when the constructor of <code>AnnotatedParamereAdviceInfo
     * </code> throws a <code>MultipleAdviceInfoException</code>.
     * 
     * @param method          the method that contains parameters with more than one
     *                        valid annotations
     * @param rules           the parameter annotation rules
     * @param totalInstances  the total number of instances that must be created
     * @param annotations     the list of annotated parameter indexes
     * @return  all advice info instances representing <code>method</code>.
     */
    public static List<AnnotatedParameterAdviceInfo> createAllAdviceInfo(
          Method method, ParameterAnnotationRule[] rules, int totalInstances,
          int[][]annotations)
    {
       List<AnnotatedParameterAdviceInfo> allAdvices =
          new LinkedList<AnnotatedParameterAdviceInfo>();
       Set<AnnotatedParameterAdviceInfo> removeAdvices =
          new HashSet<AnnotatedParameterAdviceInfo>();
       // create instances
       for (int i = 0; i < totalInstances; i++)
       {
          allAdvices.add(new AnnotatedParameterAdviceInfo(rules, method));
       }
       // set all the possible combinations of parameter annotations to the instances
       for (int i = 0; i < annotations.length; i++)
       {
          for (Iterator<AnnotatedParameterAdviceInfo> iterator = allAdvices.iterator();
          iterator.hasNext();)
          {
             for (int j = 0; j < annotations[i].length; j++)
             {
                AnnotatedParameterAdviceInfo adviceInfo = iterator.next();
                try
                {
                   adviceInfo.paramTypes[annotations[i][j]].setIndex(i);
                } catch (ParameterAnnotationRuleException maoe)
                {
                   removeAdvices.add(adviceInfo);
                }
             }
          }
       }
       // remove all combinations that resulted in multiple annotation exception
       allAdvices.removeAll(removeAdvices);
       return allAdvices;
    }
    
    public boolean validate(AdviceMethodProperties properties,
          int[][] mutuallyExclusive, ReturnType returnType)
    {
       for (ParameterAnnotationType paramType: paramTypes)
       {
          if (!paramType.validate(properties))
          {
             return false;
          }
       }
       switch (returnType)
       {
          case ANY:
             if (method.getReturnType() == void.class)
             {
                break;
             }
          case NOT_VOID:
             if (properties.getJoinpointReturnType() != void.class &&
                   method.getReturnType() != Object.class &&
                   !properties.getJoinpointReturnType().
                   isAssignableFrom(method.getReturnType()))
             {
                if (AspectManager.verbose)
                {
                   AdviceMethodFactory.adviceMatchingMessage.append("\n[warn] - return value of ");
                   AdviceMethodFactory.adviceMatchingMessage.append(method);
                   AdviceMethodFactory.adviceMatchingMessage.append(" can not be assigned to type ");
                   AdviceMethodFactory.adviceMatchingMessage.append(properties.getJoinpointReturnType());
                }
                return false;
             }
       }
       
       for (int i = 0; i < mutuallyExclusive.length; i++)
       {
          int[] exclusiveParamTypes = mutuallyExclusive[i];
          int found = -1;
          for (int j = 0; j < exclusiveParamTypes.length; j++)
          {
             if (paramTypes[exclusiveParamTypes[j]].isSet())
             {
                if (found != -1)
                {
                   if (AspectManager.verbose)
                   {
                      AdviceMethodFactory.adviceMatchingMessage.append("\n[warn] - the use of parameter annotations ");
                      AdviceMethodFactory.adviceMatchingMessage.append(paramTypes[exclusiveParamTypes[found]].rule.getAnnotation());
                      AdviceMethodFactory.adviceMatchingMessage.append(" and ");
                      AdviceMethodFactory.adviceMatchingMessage.append(paramTypes[exclusiveParamTypes[j]].rule.getAnnotation());
                      AdviceMethodFactory.adviceMatchingMessage.append(" is mutually exclusive");
                   }
                   return false;
                }
                found = j;
             }
          }
       }
       return true;
    }
    
    public short getAssignabilityDegree(int annotationIndex,
          AdviceMethodProperties properties)
    {
       return paramTypes[annotationIndex].getAssignabilityDegree(properties);
    }
    
    public void assignAdviceInfo(AdviceMethodProperties properties)
    {
       int args[] = new int[parameterTypes.length];
       for (int i = 0; i < paramTypes.length; i++)
       {
          paramTypes[i].assignParameterInfo(args);
       }
       properties.setFoundProperties(this.method, args);
    }
 
    /**
     * Applies all parameter annotation rules to the advice method parameters.
     * 
     * @throws ParameterAnnotationRuleException thrown when the advice method does not
     *         comply with a parameter annotation rule.
     * @throws MultipleAdviceInfoException thrown when one or more parameters is annotated
     *         with more than one valid annotation
     */
    private void applyRules() throws ParameterAnnotationRuleException,
       MultipleAdviceInfoException
    {
       Annotation[][] paramAnnotations = method.getParameterAnnotations();
       int[] annotated = new int[paramAnnotations.length];
       for (int i = 0; i < paramAnnotations.length; i++)
       {
          annotated[i] = -1;
          extraAnnotations.clear();
          for (Annotation annotation: paramAnnotations[i])
          {
             // no valid annotation found for parameter i yet
             if (annotated[i] == -1)
             for (int j = 0; j < paramTypes.length; j++)
             {
                // found
                 if (paramTypes[j].applies(annotation, i))
                 {
                    annotated[i] = j;
                    break;
                 }
             }
             else
             {
                // look for an extra annotation
                extraAnnotations.add(annotated[i]);
                for (int j = 0; j < paramTypes.length; j++)
                {
                   if (paramTypes[j].applies(annotation))
                   {
                      extraAnnotations.add(j);
                   }
                }
             }
          }
          if (annotated[i] == -1)
          {
             throwAnnotationNotFoundException(i);
             
          }
          else if(extraAnnotations.size() > 1)
          {
             throwMAIException(paramAnnotations, annotated, i);
          }
       }
       
    }
 
    /**
     * Throws an exception indicating the ith parameter of this advice is not annotated.
     * 
     * @param i the index of the not annotated parameter.
     * 
     * @throws ParameterAnnotationRuleException
     */
    private final void throwAnnotationNotFoundException(int i)
       throws ParameterAnnotationRuleException
    {
       if (AspectManager.verbose)
       {
          throw new ParameterAnnotationRuleException("\n[warn] -parameter " + i  +
                " of method " + method +  " is not annotated");
       }
       else
       {
          throw new ParameterAnnotationRuleException(null);
       }
    }
 
    /**
     * Throws an exception indicating this advice contains one ore more parameters with
     * more than one valid annotation.
     * The exception thrown contains all information about the advice method parameter
     * annotations.
     * 
     * @param paramAnnotations      the list of parameter annotations of this advice method
     * @param singleAnnotations     the list of previous parameters whose single annotation
     *                              has been processed
     * @param singleAnnotationsSize the number parameteres whose single annotation has been
     *                              processed before a multi-annotated parameter was found
     * @throws MultipleAdviceInfoException
     */
    private final void throwMAIException(Annotation[][] paramAnnotations,
       int[] singleAnnotations, int singleAnnotationsSize)
       throws MultipleAdviceInfoException
    {
       int[][] allAnnotations = new int[paramAnnotations.length][];
       int i = singleAnnotationsSize;
       // record single annotation information
       for (int j = 0; j < i; j++)
       {
          allAnnotations[j] = new int[1];
          allAnnotations[j][0] = singleAnnotations[j];
       }
       // fill multi-annotations information
       int totalCombinations = fillAllAnnotations(allAnnotations, i);
       
       while (++i < paramAnnotations.length)
       {
          extraAnnotations.clear();
          for (Annotation annotation: paramAnnotations[i])
          {
             for (int j = 0; j < paramTypes.length; j++)
             {
                if (paramTypes[j].applies(annotation))
                {
                   extraAnnotations.add(j);
                }
             }
          }
          if (extraAnnotations.isEmpty())
          {
             throw new RuntimeException("Parameter " + singleAnnotationsSize  + " of method " +
                   method +  " is not annotated");
          }
          totalCombinations *= fillAllAnnotations(allAnnotations, i);
       }
       throw new MultipleAdviceInfoException(totalCombinations, allAnnotations);
    }
    
    /**
     * Helper method that fills <code>allAnnotations[i]</code> with all annotations found
     * on the ith advice parameter.
     * @param allAnnotations the list of all annotations found on this advice method
     * @param i              the index of the parameter whose annotations must be filled in
     *                       <code>allAnnotations</code>.
     * @return the total number of annotations found on the ith parameter
     */
    private int fillAllAnnotations(int[][] allAnnotations, int i)
    {
       allAnnotations[i] = new int[extraAnnotations.size()];
       Iterator<Integer> iterator = extraAnnotations.iterator();
       for (int k = 0; k < allAnnotations[i].length; k++)
       {
          allAnnotations[i][k] = iterator.next();
       }
       return allAnnotations[i].length;
    }
    
    /**
     * Contains validation data concerning a parameter annotation rule.
     */
    abstract class ParameterAnnotationType
    {
       ParameterAnnotationRule rule;
       
       public ParameterAnnotationType(ParameterAnnotationRule rule)
       {
          this.rule = rule;
       }
       
       /**
        * Indicates whether <code>parameterAnnotation</code> is of this type.
        * 
        * @param parameterAnnotation the parameter annotation
        * @return <code>true</code> if parameter annotation is of this type
        */
       public final boolean applies(Annotation parameterAnnotation)
       {
          return parameterAnnotation.annotationType() == rule.getAnnotation();
       }
       
       /**
        * Verifies if <code>parameterAnnotation</code> is of this type, and, if it is,
        * sets the parameter index information.
        * 
        * @param parameterAnnotation the parameter annotation
        * @param parameterIndex      the parameter index
        * @return <code>true</code> if <code>parameterAnnotation</code> is of this type
        * @throws ParameterAnnotationRuleException if the parameter annotation has more
        *         than one occurrence and this is forbidden by the annotation rule
        */
       public final boolean applies(Annotation parameterAnnotation, int parameterIndex)
          throws ParameterAnnotationRuleException
       {
          if (parameterAnnotation.annotationType() == rule.getAnnotation())
          {
             setIndex(parameterIndex);
             return true;
          }
          return false;
       }
 
       /**
        * Validates the occurences of this parameter type, according to the annotaion rule
        * and to <code>properties</code>.
        * 
        * @param properties contains information about the queried method
        * @return <code>true</code> if the occurrences of this parameter type are all valid
        */
       public final boolean validate(AdviceMethodProperties properties)
       {
          if (rule.isMandatory() && !isSet())
          {
             if (AspectManager.verbose)
             {
                AdviceMethodFactory.adviceMatchingMessage.append("\n[warn] - mandatory parameter annotation ");
                AdviceMethodFactory.adviceMatchingMessage.append(rule.getAnnotation());
               AdviceMethodFactory.adviceMatchingMessage.append("not found on method ");
                AdviceMethodFactory.adviceMatchingMessage.append(method);
             }
             return false;
          }
          return internalValidate(properties);
       }
 
       
       /**
        * Records that the parameter identified by <code>paramterIndex</code> is of this
        * type.
        * @param parameterIndex the index of the parameter
        * @throws ParameterAnnotationRuleException if the parameter annotation has more
        *         than one occurrence and this is forbidden by the annotation rule
        */
       public abstract void setIndex(int parameterIndex) throws ParameterAnnotationRuleException;
 
       /**
        * Returns <code>true</code> if there is a parameter of this type.
        */
       public abstract boolean isSet();
       
       /**
        * Validates the occurences of this parameter type, according to the annotation rule
        * and to <code>properties</code>.
        * 
        * @param properties contains information about the queried method
        * @return <code>true</code> if the occurrences of this parameter type are all valid
        */
       public abstract boolean internalValidate(AdviceMethodProperties properties);
       
       /**
        * Returns the sum of the assignability degrees of every paramater of this type.
        * 
        * @param properties       contains information about the queried advice method
        * @return                 the assignability degree if this parameter type on the
        *                         advice method
        */
       public abstract short getAssignabilityDegree(AdviceMethodProperties properties);
       
       /**
        * Assigns information regarding all occurences of this parameter type on the
        * advice method to <code>args</code>.
        * 
        * @param args array containing information of parameter type occurrences
        */
       public abstract void assignParameterInfo(int[] args);
    }
 
    /**
     * A parameter type whose annotation can occur only once in an advice method.
     */
    class SingleParameterType extends ParameterAnnotationType
    {
       int index;
       
       public SingleParameterType(ParameterAnnotationRule rule)
       {
          super(rule);
          this.index = -1;
       }
       
       public final void setIndex(int parameterIndex)
          throws ParameterAnnotationRuleException
       {
          if (this.index != -1)
          {
             if (AspectManager.verbose)
             {
                throw new ParameterAnnotationRuleException("\n[warn] - found more than "
                      + "one occurence of " + rule.getAnnotation().getName() +
                      " on parameters of advice" + method);  
             }
             throw new ParameterAnnotationRuleException(null);
          }
          this.index = parameterIndex;
          rank += rule.getRankGrade();
       }
 
       public final boolean isSet()
       {
          return this.index != -1;
       }
       
       public final boolean internalValidate(AdviceMethodProperties properties)
       {
          if (index != -1 && !method.getParameterTypes()[index].isAssignableFrom(
                (Class) rule.getAssignableFrom(properties)))
          {
             if (AspectManager.verbose)
             {
                AdviceMethodFactory.adviceMatchingMessage.append("\n[warn] - parameter annotated with ");
                AdviceMethodFactory.adviceMatchingMessage.append(rule.getAnnotation());
                AdviceMethodFactory.adviceMatchingMessage.append(" is not assignable from expected type ");
                AdviceMethodFactory.adviceMatchingMessage.append(rule.getAssignableFrom(properties));   
                AdviceMethodFactory.adviceMatchingMessage.append(" on  method ");
                AdviceMethodFactory.adviceMatchingMessage.append(method);
             }
             return false;
          }
          return  true;
       }
 
       public final short getAssignabilityDegree(AdviceMethodProperties properties)
       {
          if (this.index == -1)
          {
             return -1;
          }
          return AnnotatedParameterAdviceInfo.this.getAssignabilityDegree(
                (Class) rule.getAssignableFrom(properties),
                method.getParameterTypes()[this.index]);
       }
       
       public final void assignParameterInfo(int[] args)
       {
          if (this.index != -1)
          {
             args[index] = rule.getProperty();
          }
       }
    }
 
    /**
     * A parameter type whose annotation can occur more than once in an advice method.
     */   
    class MultipleParameterType extends ParameterAnnotationType
    {
       private int[][] indexes;
       private int indexesLength;
       
       // maximum size is the total number of parameters
       public MultipleParameterType(ParameterAnnotationRule rule, int totalParams)
       {
          super(rule);
          this.indexes = new int[totalParams][2];
          this.indexesLength = 0;
       }
       
       public final void setIndex(int index) throws ParameterAnnotationRuleException
       {
          if (indexesLength == indexes.length)
          {
             throw new ParameterAnnotationRuleException("Found more arg annotated parameters");
          }
          indexes[indexesLength++][0] = index;
          rank += rule.getRankGrade();
       }
       
       public final boolean isSet()
       {
          return indexesLength > 0;
       }
       
       public final boolean internalValidate(AdviceMethodProperties properties)
       {
          Class<?>[] expectedTypes = (Class<?>[]) rule.getAssignableFrom(properties);
          Class<?>[] adviceTypes = method.getParameterTypes();
          boolean[] taken = new boolean[expectedTypes.length];
          for (int i = 0; i < indexesLength; i++)
          {
             boolean found = false;
             for (int j = 0; j < expectedTypes.length; j++)
             {
                if (adviceTypes[indexes[i][0]] == expectedTypes[j] && !taken[j])
                {
                   indexes[i][1] = j;
                   taken[j] = true;
                   found = true;
                   break;
                }
             }
             if (!found)
             {
                for (int j = 0; j < expectedTypes.length; j++)
                {
                   if (adviceTypes[indexes[i][0]].isAssignableFrom(expectedTypes[j]) &&
                         !taken[j])
                   {
                      indexes[i][1] = j;
                      taken[j] = true;
                      found = true;
                      break;
                   }
                }
                if (!found)
                {
                   if (AspectManager.verbose)
                   {
                      AdviceMethodFactory.adviceMatchingMessage.append("\n[warn] - not found a match for argument ");
                      AdviceMethodFactory.adviceMatchingMessage.append(adviceTypes[indexes[i][0]]);
                      AdviceMethodFactory.adviceMatchingMessage.append(" of ");
                      AdviceMethodFactory.adviceMatchingMessage.append(method);
                      AdviceMethodFactory.adviceMatchingMessage.append("\n[warn]   expected one of types:");
                      for (int j = 0; j < expectedTypes.length; j++)
                      {
                         AdviceMethodFactory.adviceMatchingMessage.append(expectedTypes[j]);
                         AdviceMethodFactory.adviceMatchingMessage.append(" ");
                      }
                   }
                   return false;
                }
             }
          }
          return true;
       }
       
       public short getAssignabilityDegree(AdviceMethodProperties properties)
       {
          if (indexesLength == 0)
          {
             return -1;
          }
          Class[] expectedTypes = (Class[]) rule.getAssignableFrom(properties);
          Class[] paramTypes = method.getParameterTypes();
          short level = 0;
          for (int i = 0; i < indexesLength; i++)
          {
             level += AnnotatedParameterAdviceInfo.this.getAssignabilityDegree(
                   expectedTypes[this.indexes[i][1]],
                   method.getParameterTypes()[this.indexes[i][0]]);
          }
          return level; 
       }
       
       public void assignParameterInfo(int args[])
       {
          for (int i = 0; i < indexesLength; i++)
          {
             args[this.indexes[i][0]] = this.indexes[i][1];
          }
       }  
    }
 }
