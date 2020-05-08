 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
 package org.jboss.aop.advice.annotation;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.WeakHashMap;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.advice.AdviceMethodProperties;
 import org.jboss.aop.util.ReflectUtils;
 
 /**
  * Utility class to select an advice method for a given joinpoint.
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @author Flavia Rainone
  * @version $Revision$
  */
 public class AdviceMethodFactory
 {
    /**
     * Rule to be applied on advice return types
     */
    enum ReturnType {VOID, ANY, NOT_VOID};
    
    /**
     * Factory that selects advice methods for <i>before</i> interception.
     */
    public static final AdviceMethodFactory BEFORE = new AdviceMethodFactory (null,
          new ParameterAnnotationRule[]{ParameterAnnotationRule.JOIN_POINT},
          ReturnType.VOID);
    /**
     * Factory that selects advice methods for <i>after</i> interception.
     */
    public static final AdviceMethodFactory AFTER = new AdviceMethodFactory (null,
          new ParameterAnnotationRule[]{ParameterAnnotationRule.JOIN_POINT,
          ParameterAnnotationRule.RETURN}, ReturnType.ANY);
    /**
     * Factory that selects advice methods for <i>throwing</i> interception.
     */
    public static final AdviceMethodFactory THROWING = new AdviceMethodFactory (null,
          new ParameterAnnotationRule[]{ParameterAnnotationRule.JOIN_POINT,
          ParameterAnnotationRule.THROWABLE}, ReturnType.VOID);
    /**
     * Factory that selects advice methods for <i>aroung</i> interception.
     */
    public static final AdviceMethodFactory AROUND = new AdviceMethodFactory (
          // around can also follow a specific signature instead of following
          // parameter annotation rules
          new AdviceSignatureRule()
          {
             public boolean applies(Method method)
             {  
                // only one parameter
                Annotation[][] annotations = method.getParameterAnnotations();
                if (annotations.length != 1)
                {
                  return false;
                }
                // not annotated
                for (Annotation annotation: annotations[0])
                {
                   if (annotation.annotationType().getPackage() == 
                      AdviceMethodFactory.class.getPackage())
                   {
                      return false;
                   }
                }
                // returning Object
                if (method.getReturnType() != Object.class)
                {
                   if (AspectManager.verbose)
                   {
                      adviceMatchingMessage.append("\n[warn] - method ");
                      adviceMatchingMessage.append(method);
                      adviceMatchingMessage.append(" does not match default around signature because it returns ");
                      adviceMatchingMessage.append(method.getReturnType());
                      adviceMatchingMessage.append(" intead of java.lang.Object");
                   }
                   return false;
                }
                // throws Throwable
                for (Class exceptionType: method.getExceptionTypes())
                {
                   if (exceptionType == Throwable.class)
                   {
                      return true;
                   }
                }
                if (AspectManager.verbose)
                {
                   adviceMatchingMessage.append("\n[warn] - method ");
                   adviceMatchingMessage.append(method);
                   adviceMatchingMessage.append(" does not match default around signature because it does not throw Throwable");
                }
                return false;
             }
             
             public AdviceInfo getAdviceInfo(Method method)
             {
                // creates an advice info with the greatest rank of all advices
               return new AdviceInfo(method, 1000)
                {
                   public boolean validate(AdviceMethodProperties properties,
                         ReturnType adviceReturn)
                   {
                      if(parameterTypes[0].isAssignableFrom(properties.getInvocationType()))
                      {
                         return true;
                      }
                      if (AspectManager.verbose)
                      {
                         adviceMatchingMessage.append("\n[warn] - argument 0 of method ");
                         adviceMatchingMessage.append(method);
                         adviceMatchingMessage.append(" is not assignable from ");
                         adviceMatchingMessage.append(properties.getInvocationType());
                      }
                      return false;
                   }
                   
                   public void resetValidation() {}
 
                   public short getAssignabilityDegree(int typeIndex,
                         boolean isContextRule, AdviceMethodProperties properties)
                   {
                      return getAssignabilityDegree(parameterTypes[0], properties.getInvocationType());
                   }
                   
                   public void assignAdviceInfo(AdviceMethodProperties properties)
                   {
                      properties.setFoundProperties(this.method, new int[]{
                            AdviceMethodProperties.INVOCATION_ARG});
                   }
                };
             }
          },         
          new ParameterAnnotationRule[]{ParameterAnnotationRule.INVOCATION},
          ReturnType.NOT_VOID);
          
 
    static final short NOT_ASSIGNABLE_DEGREE = Short.MAX_VALUE;
    static final short MAX_DEGREE = NOT_ASSIGNABLE_DEGREE - 1;
    static final ParameterAnnotationRule[] FULLY_STATIC =
       new ParameterAnnotationRule[] {ParameterAnnotationRule.ARGS,
       ParameterAnnotationRule.ARG};
    static final int[][] FS_INCOMPATIBILITY = new int[][]{{0, 1}};
    
    static final ParameterAnnotationRule[] CALLER_AVAILABLE =
       new ParameterAnnotationRule[] {ParameterAnnotationRule.CALLER,
       ParameterAnnotationRule.ARGS, ParameterAnnotationRule.ARG};
    static final int[][] CA_INCOMPATIBILITY = new int[][]{{1, 2}};
    
    static final ParameterAnnotationRule[] TARGET_AVAILABLE =
       new ParameterAnnotationRule[] {ParameterAnnotationRule.TARGET,
       ParameterAnnotationRule.ARGS, ParameterAnnotationRule.ARG};
    static final int[][] TA_INCOMPATIBILITY = CA_INCOMPATIBILITY;
    
    static final ParameterAnnotationRule[] TARGET_CALLER_AVAILABLE =
       new ParameterAnnotationRule[] {ParameterAnnotationRule.TARGET,
       ParameterAnnotationRule.CALLER,
       ParameterAnnotationRule.ARGS, ParameterAnnotationRule.ARG};
    static final int[][] TCA_INCOMPATIBILITY = new int[][]{{2, 3}};
    
    /** Stores advice matching failure messages on verbose mode. */
    static StringBuffer adviceMatchingMessage;
    
    /**
     * Method that returns log information about the last matching process executed.
     * Should be called only if <code>Aspect.verbose</code> is <code>true</code>.
     * 
     * @return advice matching log information
     */
    public final static String getAdviceMatchingMessage()
    {
       String message = adviceMatchingMessage.toString();
       return message;
    }
    
    private HashMap<String, WeakHashMap<ParameterAnnotationRule[],
       Collection<AdviceInfo>>> adviceInfoCache;
    
    private ReturnType returnType;
    private AdviceSignatureRule adviceSignatureRule;
    private ParameterAnnotationRule[] rules;
    
    
    /**
     * Creates an advice method factory.
     * 
     * @param adviceSignatureRule the factory can have a highest priority signature rule,
     *                            that avoids the parameter rules verification.
     * @param rules               the parameter annotation rules that can be used by
     *                            this factory on the advice method matching.
     * @param returnType          indicates whether the queried advice methods can return
     *                            a value to overwrite the join point execution result.
     */
    private AdviceMethodFactory(AdviceSignatureRule adviceSignatureRule,
          ParameterAnnotationRule[] rules, ReturnType returnType)
    {
       this.adviceSignatureRule = adviceSignatureRule;
       this.rules = rules;
       this.returnType = returnType;
       this.adviceInfoCache = new HashMap
          <String, WeakHashMap<ParameterAnnotationRule[], Collection<AdviceInfo>>>();
    }
    
    /**
     * Finds the more appropriate advice method.
     * 
     * @param properties contains information regarding the queried advice method
     * @return           a properties fullfilled with the found method information. Can be
     *                   <code>null</code> if no suitable method was found.
     */
    public final AdviceMethodProperties findAdviceMethod(AdviceMethodProperties properties)
    {
       if (AspectManager.verbose)
       {
          adviceMatchingMessage = new StringBuffer();
       }
       
       ParameterAnnotationRule[] contextRules = null;
       int[][] mutuallyExclusive = null;
       switch(properties.getOptionalParameters())
       {
          case NONE:
             contextRules = FULLY_STATIC;
             mutuallyExclusive = FS_INCOMPATIBILITY;
             break;
          case TARGET:
             contextRules = TARGET_AVAILABLE;
             mutuallyExclusive = TA_INCOMPATIBILITY;
             break;
          case CALLER:
             contextRules = CALLER_AVAILABLE;
             mutuallyExclusive = CA_INCOMPATIBILITY;
             break;
          case TARGET_CALLER:
             contextRules = TARGET_CALLER_AVAILABLE;
             mutuallyExclusive = TCA_INCOMPATIBILITY;
             break;
          default:
             throw new RuntimeException("Unexpected Optional Parameters Option" +
                   properties.getOptionalParameters());
       }
       
       Collection<AdviceInfo> cacheCollection = getRankedAdvices(
                properties, contextRules, mutuallyExclusive);
 
       // no advice method following the rules was found
       if (cacheCollection == null || cacheCollection.isEmpty())
       {
          return null;
       }
       synchronized(cacheCollection)
       {
          LinkedList<AdviceInfo> rankedAdvices =
                new LinkedList<AdviceInfo>(cacheCollection);
          // validate and retrive best match
          AdviceInfo bestAdvice = bestValidAdvice(rankedAdvices, properties,
                contextRules);
          if (bestAdvice == null)
          {
             return null;
          }
          // assign best Advice info to properties 
          bestAdvice.assignAdviceInfo(properties);
       }
       return properties;
    }
 
    /**
     * Returns a list of one or more advice infos ranked according to their priority.
     * The valid advice with the highest rank is the one that should be chosen.
     * 
     * @param properties        contains information regarding the queried advice
     *                          method
     * @param contextRules      the rules that must be followed according to the
     *                          joinpoint context
     * @param mutuallyExclusive a list of which context rules are mutually exclusive
     * 
     * @return a sorted collection containing all advice infos that might be
     *         used on the joinpoint interception. This collection should not be
     *         altered during validation, nor should the advice infos be concurrently
     *         used during validation (these objects cache data of <code>properties
     *         </code> during the process).
     */
    private Collection<AdviceInfo> getRankedAdvices(AdviceMethodProperties properties,
          ParameterAnnotationRule[] contextRules, int[][] mutuallyExclusive)
    {
       WeakHashMap<ParameterAnnotationRule[], Collection<AdviceInfo>> map;
       synchronized(adviceInfoCache)
       {
          // verify if list is on cache
          String key =
                  properties.getAspectClass().getName() + properties.getAdviceName();
          map = adviceInfoCache.get(key);
          if (map == null)
          {
             map = new WeakHashMap<ParameterAnnotationRule[], Collection<AdviceInfo>>();
             adviceInfoCache.put(key, map);
          }
       }
       synchronized(map)
       {
          if (map.containsKey(contextRules))
          {
             Collection<AdviceInfo> advices = map.get(contextRules);
             for(AdviceInfo adviceInfo: advices)
             {
                adviceInfo.resetValidation();
             }
             return advices;
          }
 
          // create the list
          Method[] methods = ReflectUtils.getMethodsWithName(
                properties.getAspectClass(), properties.getAdviceName());
          ArrayList<AdviceInfo> rankedAdvices =
                new ArrayList<AdviceInfo>(methods.length);
          // add list to cache
          map.put(contextRules, rankedAdvices);
 
 
          if (methods.length == 0)
          {
             if (AspectManager.verbose)
             {
                adviceMatchingMessage.append("\n[warn] - advice method ");
                adviceMatchingMessage.append(properties.getAspectClass());
                adviceMatchingMessage.append(".");
                adviceMatchingMessage.append(properties.getAdviceName());
                adviceMatchingMessage.append(" not found");
             }
             return null;
          }
 
          for (int i = 0; i < methods.length; i++)
          {
             // advice applies to signature rule
             if (adviceSignatureRule != null &&
                   adviceSignatureRule.applies(methods[i]))
             {
                rankedAdvices.add(adviceSignatureRule.getAdviceInfo(methods[i]));
             }
             else
             {
                try
                {
                   // advice applies to annotated parameter rules
                   rankedAdvices.add(new AnnotatedParameterAdviceInfo(properties,
                         methods[i], rules, contextRules, mutuallyExclusive));
                }catch (ParameterAnnotationRuleException pare)
                {
                   // no need to print messages -> 
                   // exception prints automatically on verbose mode
                }
             }
          }
          // sort according to rank
          Collections.sort(rankedAdvices);
          return rankedAdvices;
       }
    }
    
    /**
     * Validates the highest rank advice methods and return the best match.
     * 
     * @param rankedAdvices a sorted collection of advice infos
     * @param properties    contains information about the queried advice method
     * @param contextRules  the parameter annotation rules that are dependent on the
     *                      join point context
     * @return              information about the best advice method match
     */
    private AdviceInfo bestValidAdvice(LinkedList<AdviceInfo> rankedAdvices,
          AdviceMethodProperties properties, ParameterAnnotationRule[] contextRules)
    {
       AdviceInfo bestAdvice = null;
       ListIterator<AdviceInfo> iterator = rankedAdvices.listIterator();
       while (iterator.hasNext())
       {
          AdviceInfo advice = iterator.next();
          if (advice.validate(properties, returnType))
          {
             bestAdvice = advice;
             break;
          }
          else
          {
             iterator.remove();
          }
       }
       
       switch(rankedAdvices.size())
       {
       case 0:
          // no valid advice method was found
          return null;
       case 1:
          // only one valid advice method
          return bestAdvice;
       }
       // is there only one advice method valid with the highest rank?
       while (iterator.hasNext())
       {
          AdviceInfo advice = iterator.next();
          if (advice.getRank() == bestAdvice.getRank())
          {
             if (!advice.validate(properties, returnType))
             {
                iterator.remove();
             }
          }
          else
          {
             iterator.previous();
             break;
          }
       }
       // if yes, return it
       if (iterator.previous() == bestAdvice)
       {
          return bestAdvice;
       }
       iterator.next();
       // if not, retrive the list of all valid advices with the highest rank
       List<AdviceInfo> bestAdvices =
          rankedAdvices.subList(0, iterator.nextIndex());
       // deep process these advices to find the best match
       return bestMatch(bestAdvices, properties, contextRules);
    }
    
    /**
     * Returns the best advice method among the advices contained in <code>greatestRank
     * </code>. The criteria used is the specificness of annotated parameters type,
     * i.e., the more specific type <code>MethodInvocation</code> is better than
     * <code>Invocation</code>.
     * 
     * @param greatestRank contains information about all valid advice methods with the
     *                     highest rank
     * @param properties   information about the queried advice method
     * @param contextRules the context annotation rules (depend on the join point
     *                     context)
     * @return             information about the best advice method match
     */
    AdviceInfo bestMatch(Collection<AdviceInfo> greatestRank,
          AdviceMethodProperties properties, ParameterAnnotationRule[] contextRules)
    {
       
       // select the closest match according to assignability degree of parameters
       // like Invocation, JoinPoint, Return, etc
       AdviceInfo bestAdvice = selectBestRuleMatch(greatestRank, properties,
             rules.length, false);
       if (bestAdvice != null)
       {
          return bestAdvice;
       }
       // context rules are second priority
       bestAdvice = selectBestRuleMatch(greatestRank, properties, contextRules.length,
             true);
       if (bestAdvice != null)
       {
          return bestAdvice;
       }
       short bestDegree = NOT_ASSIGNABLE_DEGREE;
       if (returnType == ReturnType.ANY || returnType == ReturnType.NOT_VOID)
       {
          for (AdviceInfo currentAdvice: greatestRank)
          {
             short currentDegree =  currentAdvice.getReturnAssignabilityDegree(properties);
             if (currentDegree < bestDegree)
             {
                bestAdvice = currentAdvice;
                bestDegree = currentDegree;
             }
          }
          //in case of two or more advices with the same match degree, pick any one of them
          return bestAdvice;
       }
       // we have more than one best advice; return any one of them
       return greatestRank.iterator().next();
    }
    
    /**
     * Returns the best advice method among the advices contained in <code>greatestRank
     * </code>. The criteria used in the assignability degree of parameters
     * that follow a set of rules (defined by <code>useContextRules</code>).
     * 
     * @param greatestRank    contains information about all valid advice methods with
     *                        the highest rank
     * @param properties      information about the queried advice method
     * @param totalRules      the total number of rules that will be used on matching
     * @param useContextRules if <code>true</code>, context rules are used instead
     *                        of factory rules
     * @return                the best match. If there is more than one match, returns
     *                        <code>null</code>
     */
    private AdviceInfo selectBestRuleMatch(Collection<AdviceInfo> greatestRank,
          AdviceMethodProperties properties, int totalRules,
          boolean isContextRule)
    {
       short bestDegree = NOT_ASSIGNABLE_DEGREE;
       AdviceInfo bestAdvice = null;
       Collection<AdviceInfo> removeList = new ArrayList<AdviceInfo>();
       
       // rule i is more important than rule i + 1
       for (int i = 0; i < totalRules; i++)
       {
          for (Iterator<AdviceInfo> iterator = greatestRank.iterator();
                iterator.hasNext();)
          {
             AdviceInfo currentAdvice = iterator.next();
             short currentDegree = currentAdvice.getAssignabilityDegree(i,
                   isContextRule, properties);
             if (currentDegree < bestDegree)
             {
                if (bestAdvice != null)
                {
                   removeList.add(bestAdvice);
                }
                bestAdvice = currentAdvice;
                bestDegree = currentDegree;
             }
             else if (currentDegree > bestDegree)
             {
                iterator.remove();
             }
          }
          // found the best
          if (greatestRank.size() - removeList.size() == 1)
          {
             return bestAdvice;
          }
          greatestRank.removeAll(removeList);
          // reset values
          removeList.clear();
          bestAdvice = null;
          bestDegree = NOT_ASSIGNABLE_DEGREE;
       }
       return null;
    }
    
    /**
     * Represents a highest priority signature rule alternative to the parameter rules.
     */
    interface AdviceSignatureRule
    {
       boolean applies(Method method);
       AdviceInfo getAdviceInfo(Method method);
    }
 }
