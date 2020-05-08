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
 package org.jboss.aop;
 
 import gnu.trove.TLongObjectHashMap;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.jboss.aop.advice.AdviceBinding;
 import org.jboss.aop.advice.AspectDefinition;
 import org.jboss.aop.advice.GeneratedAdvisorInterceptor;
 import org.jboss.aop.advice.InterceptorFactory;
 import org.jboss.aop.advice.PrecedenceSorter;
 import org.jboss.aop.instrument.ConByConJoinPointGenerator;
 import org.jboss.aop.instrument.ConByMethodJoinPointGenerator;
 import org.jboss.aop.instrument.ConstructionJoinPointGenerator;
 import org.jboss.aop.instrument.ConstructorJoinPointGenerator;
 import org.jboss.aop.instrument.FieldJoinPointGenerator;
 import org.jboss.aop.instrument.JoinPointGenerator;
 import org.jboss.aop.instrument.MethodByConJoinPointGenerator;
 import org.jboss.aop.instrument.MethodByMethodJoinPointGenerator;
 import org.jboss.aop.instrument.MethodJoinPointGenerator;
 import org.jboss.aop.joinpoint.FieldJoinpoint;
 import org.jboss.aop.joinpoint.Joinpoint;
 import org.jboss.aop.joinpoint.MethodJoinpoint;
 import org.jboss.aop.pointcut.PointcutMethodMatch;
 
 /**
  * Comment
  *
  * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
  * @version $Revision$
  */
 public class GeneratedClassAdvisor extends ClassAdvisor
 {
    public static final String ADD_METHOD_INFO = "addMethodInfo";
    public static final String ADD_CONSTRUCTOR_INFO = "addConstructorInfo";
    public static final String ADD_CONSTRUCTION_INFO = "addConstructionInfo";
    public static final String ADD_FIELD_READ_INFO = "addFieldReadInfo";
    public static final String ADD_FIELD_WRITE_INFO = "addFieldWriteInfo";
    public static final String GET_PARENT_ADVISOR = "getParentAdvisor";
 
    MethodInterceptors methodInfos = new MethodInterceptors(this);
    ArrayList constructorInfos = new ArrayList();
    ArrayList constructionInfos = new ArrayList();
    ArrayList fieldReadInfos = new ArrayList();
    ArrayList fieldWriteInfos = new ArrayList();
    /** Super class methods that have been overrridden - these need special handling in this weaving mode */
    ArrayList overriddenMethods = new ArrayList(); 
 
    //TODO These are only needed for the class advisor really
    //All joinpoint generators apart from field reads and constructions go in here
    private ConcurrentHashMap joinPoinGenerators = new ConcurrentHashMap();
    //Needs its own map to avoid crashing with the field write generators
    private ConcurrentHashMap fieldReadJoinPoinGenerators = new ConcurrentHashMap();
    //Needs its own map to avoid crashing with the constructor generators
    private ConcurrentHashMap constructionJoinPointGenerators = new ConcurrentHashMap();
    
    ConcurrentHashMap oldInfos = new ConcurrentHashMap();
    ConcurrentHashMap oldFieldReadInfos = new ConcurrentHashMap();
    ConcurrentHashMap oldConstructionInfos = new ConcurrentHashMap();
 
    boolean initialisedSuperClasses; 
 
    private int version;
    
    /**
     * Different behaviour depending on if we are a class advisor or instance advisor
     */
    AdvisorStrategy advisorStrategy;
 
    protected GeneratedClassAdvisor(String classname)
    {
       //Generated advisors will not pass in an aspectmanager
       //This will be passed in via the initialise() method
       super(classname, null);
       advisorStrategy = new ClassAdvisorStrategy(); 
    }
    
    protected GeneratedClassAdvisor(String classname, GeneratedClassAdvisor parent)
    {
       super(classname, null);
       advisorStrategy = new InstanceAdvisorStrategy(parent);
    }
 
    protected void initialise(Class clazz, AspectManager manager)
    {
       advisorStrategy.initialise(clazz, manager);
    }
 
    /**
     * Generated class advisor sub class will override
     */
    protected void initialiseCallers()
    {
    }
 
    /**
     * Generated instance advisor sub class will override
     */
    protected void initialiseInfosForInstance()
    {
       
 
    }
    
    /**
     * To be called by initialiseInfosForInstance() in the generated instance advisors
     */
    protected MethodInfo copyInfoFromClassAdvisor(MethodInfo info)
    {
       MethodInfo copy = (MethodInfo)info.copy();
       copy.setAdvisor(this);
       addMethodInfo(copy);
       return copy;
    }
    
    /**
     * To be called by initialiseInfosForInstance() in the generated instance advisors
     */
    protected FieldInfo copyInfoFromClassAdvisor(FieldInfo info)
    {
       FieldInfo copy = (FieldInfo)info.copy();
       copy.setAdvisor(this);
       if (copy.isRead())
       {
          addFieldReadInfo(copy);
       }
       else
       {
          addFieldWriteInfo(copy);
       }
       return copy;
    }
 
    /**
     * To be called by initialiseInfosForInstance() in the generated instance advisors
     */
    protected ConByConInfo copyInfoFromClassAdvisor(ConByConInfo info)
    {
       ConByConInfo copy = (ConByConInfo)info.copy();
       copy.setAdvisor(this);
 //      addMethodInfo(copy);
       return copy;
    }
 
    /**
     * To be called by initialiseInfosForInstance() in the generated instance advisors
     */
    protected MethodByConInfo copyInfoFromClassAdvisor(MethodByConInfo info)
    {
       MethodByConInfo copy = (MethodByConInfo)info.copy();
       copy.setAdvisor(this);
 //      addMethodInfo(copy);
       return copy;
    }
 
    /**
     * To be called by initialiseInfosForInstance() in the generated instance advisors
     */
    protected ConByMethodInfo copyInfoFromClassAdvisor(ConByMethodInfo info)
    {
       ConByMethodInfo copy = (ConByMethodInfo)info.copy();
       copy.setAdvisor(this);
 //      addMethodInfo(copy);
       return copy;
    }
    
    /**
     * To be called by initialiseInfosForInstance() in the generated instance advisors
     */
    protected MethodByMethodInfo copyInfoFromClassAdvisor(MethodByMethodInfo info)
    {
       MethodByMethodInfo copy = (MethodByMethodInfo)info.copy();
       copy.setAdvisor(this);
 //      addMethodInfo(copy);
       return copy;
    }
    
    
    @Override
    protected void rebuildInterceptors()
    {
       version++;
       advisorStrategy.rebuildInterceptors();
    }
    
    /**
     * Callback for instance advisors to rebuild their interceptors when their
     * version number is out of sync
     */
    protected synchronized void internalRebuildInterceptors()
    {
       super.rebuildInterceptors();
    }
 
    /**
     * Callback for generated instance advisors to check if the version has been updated 
     */
    protected void checkVersion()
    {
       advisorStrategy.checkVersion();
    }
 
    /**
     * Will be overridden by generated instanceadvisor classes and perform a rebuild
     */
    protected void doRebuildForInstance()
    {
       
    }
 
    protected void handleOverriddenMethods(AdviceBinding binding)
    {
       if (overriddenMethods != null && overriddenMethods.size() > 0)
       {
          for (Iterator it = overriddenMethods.iterator() ; it.hasNext() ; )
          {
             MethodInfo info = (MethodInfo)it.next();
             Method method = info.getAdvisedMethod();
             PointcutMethodMatch match = binding.getPointcut().matchesExecution(this, method);
             
             if (match != null && match.isMatch())
             {
                adviceBindings.add(binding);
                if (AspectManager.verbose)
                {
                   System.err.println("method matched binding " + binding.getPointcut().getExpr() + " " + method.toString());     
                }
                binding.addAdvisor(this);
                pointcutResolved(info, binding, new MethodJoinpoint(method));
             }
          }
       }      
    }
 
    @Override
    protected void resolveMethodPointcut(MethodInterceptors newMethodInterceptors, AdviceBinding binding)
    {
       GeneratedClassAdvisor classAdvisor = getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
       if (classAdvisor == null)
       {
          //We are either the class advisor or an instanceadvisor with own data so we need to do all the work
          super.resolveMethodPointcut(newMethodInterceptors, binding);
          handleOverriddenMethods(binding);
       }
 
    }
 
    @Override
    protected void resolveFieldPointcut(ArrayList newFieldInfos, AdviceBinding binding, boolean write)
    {
       GeneratedClassAdvisor classAdvisor = getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
       if (classAdvisor == null)
       {
          //We are either the class advisor or an instanceadvisor with own data so we need to do all the work
          super.resolveFieldPointcut(newFieldInfos, binding, write);
       }
    }
 
    @Override
    protected void resolveConstructorPointcut(ArrayList newConstructorInfos, AdviceBinding binding)
    {
       advisorStrategy.resolveConstructorPointcut(newConstructorInfos, binding);
    }
 
    @Override
    protected void resolveConstructionPointcut(ArrayList newConstructionInfos, AdviceBinding binding)
    {
       advisorStrategy.resolveConstructionPointcut(newConstructionInfos, binding);
    }
    
    /**
     * Generated class advisor sub class will override
     */
    protected void initialiseMethods()
    {
    }
    
    /**
     * Called by initialiseMethods() in generated advisor sub classes
     */
    protected void addMethodInfo(MethodInfo mi)
    {
       MethodInfo old = methodInfos.getMethodInfo(mi.getHash());
       if (old != null)
       {
          overriddenMethods.add(old);
       }
       methodInfos.put(mi.getHash(), mi);
       
       advisorStrategy.makeAccessibleMethod(mi);
    }
 
    @Override
    protected MethodInterceptors initializeMethodChain()
    {
       //We have all the advised methods here, need to get all the others here too
 
       long[] keys = advisedMethods.keys();
       for (int i = 0; i < keys.length; i++)
       {
          MethodMatchInfo matchInfo = methodInfos.getMatchInfo(keys[i]);
 
          if (super.initialized && matchInfo != null)
          {
             matchInfo.clear();
          }
 
          if (matchInfo == null)
          {
             MethodInfo info = new MethodInfo();
             Method amethod = (Method) advisedMethods.get(keys[i]);
             info.setAdvisedMethod(amethod);
             info.setUnadvisedMethod(amethod);
             info.setHash(keys[i]);
             info.setAdvisor(this);
             methodInfos.put(keys[i], info);
          }
       }
 
       return methodInfos;
    }
 
 
    /**
     * Generated class advisor sub class will override
     */
    protected void initialiseConstructors()
    {
    }
 
    /**
     * Called by initialiseConstructors() in generated advisor sub classes
     */
    protected void addConstructorInfo(ConstructorInfo ci)
    {
       constructorInfos.add(ci);
       //If we do dynamic invokes the constructor will need to be accessible via reflection
       SecurityActions.setAccessible(ci.getConstructor());
    }
 
    @Override
    protected void createInterceptorChains() throws Exception
    {
       advisorStrategy.createInterceptorChains();
    }
    
    @Override
    protected ArrayList initializeConstructorChain()
    {
       if (super.initialized)
       {
          for (Iterator it = constructorInfos.iterator() ; it.hasNext() ; )
          {
             ((ConstructorInfo)it.next()).clear();
          }
       }
       return constructorInfos;
    }
 
    /**
     * Generated class advisor sub class will override
     */
    protected void initialiseConstructions()
    {
    }
 
    /**
     * Called by initialiseConstructions() in generated advisor sub classes
     */
    protected void addConstructionInfo(ConstructionInfo ci)
    {
       constructionInfos.add(ci);
    }
 
    @Override
    protected ArrayList initializeConstructionChain()
    {
       if (super.initialized)
       {
          for (Iterator it = constructionInfos.iterator() ; it.hasNext() ; )
          {
             ((ConstructionInfo)it.next()).clear();
          }
       }
       return constructionInfos;
    }
 
    /**
     * Generated class advisor sub class will override
     */
    protected void initialiseFieldReads()
    {
    }
 
    /**
     * Called by initialiseFieldReads() in generated advisor sub classes
     */
    protected void addFieldReadInfo(FieldInfo fi)
    {
       fieldReadInfos.add(fi);
       //If we do dynamic invokes the field will need to be accessible via reflection
       advisorStrategy.makeAccessibleField(fi);
    }
 
    @Override
    protected ArrayList initializeFieldReadChain()
    {
       return mergeFieldInfos(fieldReadInfos, true);
    }
 
    /**
     * Generated class advisor sub class will override
     */
    protected void initialiseFieldWrites()
    {
    }
 
    /**
     * Called by initialiseFieldWrites() in generated advisor sub classes
     */
    protected void addFieldWriteInfo(FieldInfo fi)
    {
       fieldWriteInfos.add(fi);
       //If we do dynamic invokes the field will need to be accessible via reflection
       advisorStrategy.makeAccessibleField(fi);
    }
 
    @Override
    protected ArrayList initializeFieldWriteChain()
    {
       return mergeFieldInfos(fieldWriteInfos, false);
    }
 
    /* Creates a full list of field infos for all fields, using the ones added by
     * generated advisor for advised fields.
     */
    private ArrayList mergeFieldInfos(ArrayList advisedInfos, boolean read)
    {
       ArrayList newInfos = new ArrayList(advisedFields.length);
 
       FieldInfo nextFieldInfo = null;
       Iterator it = advisedInfos.iterator();
       if (it.hasNext())
       {
          nextFieldInfo = (FieldInfo)it.next();
       }
 
       for (int i = 0 ; i < advisedFields.length ; i++)
       {
          if (nextFieldInfo != null && nextFieldInfo.getIndex() == i)
          {
             if (super.initialized)
             {
                nextFieldInfo.clear();
             }
 
             newInfos.add(nextFieldInfo);
             if (it.hasNext())
             {
                nextFieldInfo = (FieldInfo)it.next();
             }
             else
             {
                nextFieldInfo = null;
             }
          }
          else
          {
             FieldInfo info = new FieldInfo(this, read);
             info.setAdvisedField(advisedFields[i]);
             info.setIndex(i);
             newInfos.add(info);
          }
       }
 
       return newInfos;
    }
 
    @Override
    protected void finalizeChains(MethodInterceptors newMethodInfos, ArrayList newFieldReadInfos, ArrayList newFieldWriteInfos, ArrayList newConstructorInfos, ArrayList newConstructionInfos)
    {
       ClassAdvisor classAdvisor = getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
       if (classAdvisor != null)
       {
          //We are an instance advisor who has not resolved their own pointcuts, make sure that we set the bindings that are referenced
          //so that they can be removed properly
          //Make sure that all the adviceBindings for the class advisor are referenced from us
          synchronized(this.adviceBindings)
          {
             this.adviceBindings.addAll(classAdvisor.adviceBindings);
             for (Iterator it = this.adviceBindings.iterator() ; it.hasNext() ; )
             {
                AdviceBinding binding = (AdviceBinding)it.next();
                binding.addAdvisor(this);
             }
          }
       }
       super.finalizeChains(newMethodInfos, newFieldReadInfos, newFieldWriteInfos, newConstructorInfos, newConstructionInfos);
    }
 
    @Override
    protected void finalizeMethodChain(MethodInterceptors newMethodInterceptors)
    {
       ClassAdvisor classAdvisor = getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
       if (classAdvisor != null)
       {
          //We are an instance advisor with no own data influencing the chains, copy these from the parent advisor
          easyFinalizeMethodChainForInstance(classAdvisor, newMethodInterceptors);
       }
       else
       {
          //We are either the class advisor or an instanceadvisor with own data so we need to do all the work
          fullWorkFinalizeMethodChain(newMethodInterceptors);
       }
    }
 
    private void easyFinalizeMethodChainForInstance(ClassAdvisor classAdvisor, MethodInterceptors newMethodInterceptors)
    {
       long[] keys = newMethodInterceptors.keys();
       for (int i = 0; i < keys.length; i++)
       {
          MethodInfo classMethodInfo = classAdvisor.getMethodInfo(keys[i]);
          MethodMatchInfo matchInfo = newMethodInterceptors.getMatchInfo(keys[i]);
          MethodInfo myMethodInfo = matchInfo.getInfo();
          myMethodInfo.cloneChains(classMethodInfo);
          
          if (updateOldInfo(oldInfos, myMethodInfo))
          {
             MethodJoinPointGenerator generator = getJoinPointGenerator(myMethodInfo);
             generator.rebindJoinpoint(myMethodInfo);
          }
       }
    }
    
    private void fullWorkFinalizeMethodChain(MethodInterceptors newMethodInterceptors)
    {
       //We are either the class advisor or an instanceadvisor with own data so we need to do all the work
       TLongObjectHashMap newMethodInfos = new TLongObjectHashMap();
 
       long[] keys = newMethodInterceptors.keys();
       for (int i = 0; i < keys.length; i++)
       {
          MethodMatchInfo matchInfo = newMethodInterceptors.getMatchInfo(keys[i]);
          matchInfo.populateBindings();
 
          MethodInfo info = matchInfo.getInfo();
          newMethodInfos.put(keys[i], info);
 
          MethodJoinPointGenerator generator = getJoinPointGenerator(info);
          finalizeChainAndRebindJoinPoint(oldInfos, info, generator);
       }
       methodInterceptors = newMethodInfos;
       
       //Handle the overridden methods
       if (overriddenMethods != null && overriddenMethods.size() > 0)
       {
          for (Iterator it = overriddenMethods.iterator() ; it.hasNext() ; )
          {
             MethodInfo info = (MethodInfo)it.next();
 
             MethodJoinPointGenerator generator = getJoinPointGenerator(info);
             finalizeChainAndRebindJoinPoint(oldInfos, info, generator);
          }
       }      
    }
 
    @Override
    protected void finalizeFieldReadChain(ArrayList newFieldInfos)
    {
       ClassAdvisor classAdvisor = getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
       if (classAdvisor != null)
       {
          //We are an instance advisor with no own data influencing the chains, copy these from the parent advisor
          easyFinalizeFieldChainForInstance(oldFieldReadInfos, classAdvisor.getFieldReadInfos(), newFieldInfos);
       }
       else
       {
          //We are either the class advisor or an instanceadvisor with own data so we need to do all the work
          fullWorkFinalizeFieldChain(oldFieldReadInfos, newFieldInfos);
       }
    }
 
    @Override
    protected void finalizeFieldWriteChain(ArrayList newFieldInfos)
    {
       ClassAdvisor classAdvisor = getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
       if (classAdvisor != null)
       {
          //We are an instance advisor with no own data influencing the chains, copy these from the parent advisor
          easyFinalizeFieldChainForInstance(oldInfos, classAdvisor.getFieldWriteInfos(), newFieldInfos);
       }
       else
       {
          //We are either the class advisor or an instanceadvisor with own data so we need to do all the work
          fullWorkFinalizeFieldChain(oldInfos, newFieldInfos);
       }
    }
 
    private void easyFinalizeFieldChainForInstance(Map oldFieldInfos, FieldInfo[] classFieldInfos, ArrayList newFieldInfos)
    {
       //We are an instance advisor with no own data influencing the chains, copy these from the parent advisor
       if (newFieldInfos.size() > 0)
       {
          for (int i = 0; i < newFieldInfos.size(); i++)
          {
             FieldInfo myInfo = (FieldInfo)newFieldInfos.get(i);
             myInfo.cloneChains(classFieldInfos[i]);
             
             if (updateOldInfo(oldFieldInfos, myInfo))
             {
                FieldJoinPointGenerator generator = getJoinPointGenerator(myInfo);
                generator.rebindJoinpoint(myInfo);
             }
          }
       }
    }
    
    private void fullWorkFinalizeFieldChain(Map oldFieldInfos, ArrayList newFieldInfos)
    {
       //We are either the class advisor or an instanceadvisor with own data so we need to do all the work
       for (int i = 0; i < newFieldInfos.size(); i++)
       {
          FieldInfo info = (FieldInfo)newFieldInfos.get(i);
          FieldJoinPointGenerator generator = getJoinPointGenerator(info);
          finalizeChainAndRebindJoinPoint(oldFieldInfos, info, generator);
       }
    }
    
    @Override
    protected void finalizeConstructorChain(ArrayList newConstructorInfos)
    {
       advisorStrategy.finalizeConstructorChain(newConstructorInfos);
    }
 
    @Override
    protected void finalizeConstructionChain(ArrayList newConstructionInfos)
    {
       advisorStrategy.finalizeConstructionChain(newConstructionInfos);
    }
 
    @Override
    protected void finalizeMethodCalledByMethodInterceptorChain(MethodByMethodInfo info)
    {
       MethodByMethodJoinPointGenerator generator = getJoinPointGenerator(info);
       finalizeChainAndRebindJoinPoint(oldInfos, info, generator);
    }
 
    @Override
    protected void finalizeConCalledByMethodInterceptorChain(ConByMethodInfo info)
    {
       ConByMethodJoinPointGenerator generator = getJoinPointGenerator(info);
       finalizeChainAndRebindJoinPoint(oldInfos, info, generator);
    }
 
    @Override
    protected void finalizeConCalledByConInterceptorChain(ConByConInfo info)
    {
       ConByConJoinPointGenerator generator = getJoinPointGenerator(info);
       finalizeChainAndRebindJoinPoint(oldInfos, info, generator);
    }
 
    @Override
    protected void finalizeMethodCalledByConInterceptorChain(MethodByConInfo info)
    {
       //An extra level of indirection since we distinguish between callers of method depending on
       //where the called method is defined (sub/super interfaces)
       ConcurrentHashMap map = (ConcurrentHashMap)joinPoinGenerators.get(info.getJoinpoint());
       if (map == null)
       {
          map = new ConcurrentHashMap();
          joinPoinGenerators.put(info.getJoinpoint(), map);
          map = (ConcurrentHashMap)joinPoinGenerators.get(info.getJoinpoint());
       }
 
       MethodByConJoinPointGenerator generator = getJoinPointGenerator(info);
       finalizeChainAndRebindJoinPoint(oldInfos, info, generator);
    }
 
    private JoinPointGenerator getJoinPointGenerator(JoinPointInfo info)
    {
       if (info instanceof MethodInfo)
       {
          return getJoinPointGenerator((MethodInfo)info);
       }
       else if (info instanceof FieldInfo)
       {
          return getJoinPointGenerator((FieldInfo)info);
       }
       else if (info instanceof ConstructionInfo)
       {
          return getJoinPointGenerator((ConstructionInfo)info);
       }
       else if (info instanceof ConstructorInfo)
       {
          return getJoinPointGenerator((ConstructorInfo)info);
       }
       else if (info instanceof ConByConInfo)
       {
          return getJoinPointGenerator((ConByConInfo)info);
       }
       else if (info instanceof ConByMethodInfo)
       {
          return getJoinPointGenerator((ConByMethodInfo)info);
       }
       else if (info instanceof MethodByMethodInfo)
       {
          return getJoinPointGenerator((MethodByMethodInfo)info);
       }
       else if (info instanceof MethodByConInfo)
       {
          return getJoinPointGenerator((MethodByConInfo)info);
       }
       else
       {
          throw new RuntimeException("Invalid JoinPointInfo passed in: " + info.getClass().getName());
       }
    }
 
    protected MethodJoinPointGenerator getJoinPointGenerator(MethodInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
 
    protected FieldJoinPointGenerator getJoinPointGenerator(FieldInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
    
    protected ConstructorJoinPointGenerator getJoinPointGenerator(ConstructorInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
 
    protected ConstructionJoinPointGenerator getJoinPointGenerator(ConstructionInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
 
    protected MethodByMethodJoinPointGenerator getJoinPointGenerator(MethodByMethodInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
 
    protected ConByMethodJoinPointGenerator getJoinPointGenerator(ConByMethodInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
 
    protected ConByConJoinPointGenerator getJoinPointGenerator(ConByConInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
 
    protected MethodByConJoinPointGenerator getJoinPointGenerator(MethodByConInfo info)
    {
       return advisorStrategy.getJoinPointGenerator(info);
    }
 
    /**
     * Override default behaviour of when a pointcut is matched, populate the factories since this
     * is what is needed for generating the optimized invocation method
     */
    @Override
    protected void pointcutResolved(JoinPointInfo info, AdviceBinding binding, Joinpoint joinpoint)
    {
       ArrayList curr = info.getInterceptorChain();
       if (binding.getCFlow() != null)
       {
          //TODO Handle CFlow
          InterceptorFactory[] factories = binding.getInterceptorFactories();
          for (int i = 0 ; i < factories.length ; i++)
          {
             curr.add(new GeneratedAdvisorInterceptor(factories[i], this, joinpoint, binding.getCFlowString(), binding.getCFlow()));
          }
       }
       else
       {
          InterceptorFactory[] factories = binding.getInterceptorFactories();
          for (int i = 0 ; i < factories.length ; i++)
          {
             curr.add(new GeneratedAdvisorInterceptor(factories[i], this, joinpoint));
          }
       }
    }
 
    private void finalizeChainAndRebindJoinPoint(Map oldInfos, JoinPointInfo info, JoinPointGenerator generator)
    {
       ArrayList list = info.getInterceptorChain();
       GeneratedAdvisorInterceptor[] factories = null;
       if (list.size() > 0)
       {
          factories = applyPrecedence((GeneratedAdvisorInterceptor[]) list.toArray(new GeneratedAdvisorInterceptor[list.size()]));
       }
       info.setInterceptors(factories);
 
       if (updateOldInfo(oldInfos, info))
       {
          generator.rebindJoinpoint(info);
       }
    }
 
    @Override
    public String toString()
    {
       Class clazz = this.getClass();
       StringBuffer sb = new StringBuffer("CLASS: " + clazz.getName());
 
       Field[] fields = clazz.getFields();
       for (int i = 0 ; i < fields.length ; i++)
       {
          sb.append("\n\t" + fields[i]);
       }
       return sb.toString();
    }
 
    GeneratedAdvisorInterceptor[] applyPrecedence(GeneratedAdvisorInterceptor[] interceptors)
    {
       return PrecedenceSorter.applyPrecedence(interceptors, manager);
    }
 
    /**
     * If this is an instance advisor, will check with parent class advisor if the aspect
     * is already registered. If so, we should use the one from the parent advisor
     */
    @Override
    public Object getPerClassAspect(AspectDefinition def)
    {
       return advisorStrategy.getPerClassAspect(def);
    }
 
    /**
     * Generated ClassAdvisors and InstanceAdvisors will be different instances,
     * so keep track of what per_class_joinpoint aspects have been added where
     */
    ConcurrentHashMap perClassJoinpointAspectDefinitions = new ConcurrentHashMap();
 
 
    public Object getPerClassJoinpointAspect(AspectDefinition def, Joinpoint joinpoint)
    {
       return advisorStrategy.getPerClassJoinpointAspect(def, joinpoint);
    }
 
    public synchronized void addPerClassJoinpointAspect(AspectDefinition def, Joinpoint joinpoint)
    {
       Map joinpoints = (Map)perClassJoinpointAspectDefinitions.get(def);
       if (joinpoints == null)
       {
          joinpoints = new ConcurrentHashMap();
          perClassJoinpointAspectDefinitions.put(def, joinpoints);
       }
 
       if (joinpoints.get(joinpoint) == null)
       {
          joinpoints.put(joinpoint, def.getFactory().createPerJoinpoint(this, joinpoint));
       }
    }
 
    /**
     * @see Advisor#chainOverridingForInheritedMethods()
     */
    @Override
    public boolean chainOverridingForInheritedMethods()
    {
       return true;
    }
 
    @Override
    public Object getFieldAspect(FieldJoinpoint joinpoint, AspectDefinition def)
    {
       Object instance = getPerClassJoinpointAspect(def, joinpoint);
       if (instance == null)
       {
          addPerClassJoinpointAspect(def, joinpoint);
          instance = getPerClassJoinpointAspect(def, joinpoint);
       }
       return instance;
    }
 
    private GeneratedClassAdvisor getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices()
    {
       return advisorStrategy.getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
    }
    
    /**
     * Optimization so that when we create instance advisors we don't have to create the method tables again,
     * they were already created for the class advisor 
     */
    @Override
    protected void createMethodTables() throws Exception
    {
       advisorStrategy.createMethodTables();
    }
    
    /**
     * Optimization so that when we create instance advisors we don't have to create the field tables again,
     * they were already created for the class advisor 
     */
    @Override
    protected void createFieldTable() throws Exception
    {
       advisorStrategy.createFieldTable();
    }
    
    /**
     * Optimization so that when we create instance advisors we don't have to create the constructor tables again,
     * they were already created for the class advisor 
     */
    @Override
    protected void createConstructorTables() throws Exception
    {
       advisorStrategy.createConstructorTables();
    }
 
    @Override
    public Set getPerInstanceAspectDefinitions()
    {
       return advisorStrategy.getPerInstanceAspectDefinitions();
    }
 
    @Override
    public Map getPerInstanceJoinpointAspectDefinitions()
    {
       return advisorStrategy.getPerInstanceJoinpointAspectDefinitions();
    }
 
    /**
     * Caches the old info and checks if the chains have been updated
     */
    private boolean updateOldInfo(Map oldInfos, JoinPointInfo newInfo)
    {
       JoinPointInfo oldInfo = (JoinPointInfo)oldInfos.get(newInfo.getJoinpoint());
       if (oldInfo != null)
       {
          //We are not changing any of the bindings
          if (oldInfo.equalChains(newInfo))
          {
             return false;
          }
       }
       oldInfo = newInfo.copy();
       oldInfos.put(newInfo.getJoinpoint(), oldInfo);
       return true;
    }
 
    protected void generateJoinPointClass(MethodInfo info)
    {
       MethodJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
 
    protected void generateJoinPointClass(FieldInfo info)
    {
       FieldJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
 
    protected void generateJoinPointClass(ConstructorInfo info)
    {
       ConstructorJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
 
    protected void generateJoinPointClass(ConstructionInfo info)
    {
       ConstructionJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
 
    protected void generateJoinPointClass(MethodByMethodInfo info)
    {
       MethodByMethodJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
 
    protected void generateJoinPointClass(ConByMethodInfo info)
    {
       ConByMethodJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
 
    protected void generateJoinPointClass(ConByConInfo info)
    {
       ConByConJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
 
    protected void generateJoinPointClass(MethodByConInfo info)
    {
       MethodByConJoinPointGenerator generator = getJoinPointGenerator(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
    
    protected void rebindJoinPointWithInstanceInformation(JoinPointInfo info)
    {
       JoinPointGenerator generator = getJoinPointGenerator(info);
       generator.rebindJoinpoint(info);
       generator.generateJoinPointClass(this.getClass().getClassLoader(), info);
    }
    
    /**
     * Encapsulates different behaviours depending on if this is an instance or class advisor
     */
    private interface AdvisorStrategy
    {
       void initialise(Class clazz, AspectManager manager);
       void checkVersion();
       void createInterceptorChains() throws Exception;
       MethodJoinPointGenerator getJoinPointGenerator(MethodInfo info);
       FieldJoinPointGenerator getJoinPointGenerator(FieldInfo info);
       ConstructorJoinPointGenerator getJoinPointGenerator(ConstructorInfo info);
       ConstructionJoinPointGenerator getJoinPointGenerator(ConstructionInfo info);
       MethodByMethodJoinPointGenerator getJoinPointGenerator(MethodByMethodInfo info);
       ConByMethodJoinPointGenerator getJoinPointGenerator(ConByMethodInfo info);
       ConByConJoinPointGenerator getJoinPointGenerator(ConByConInfo info);
       MethodByConJoinPointGenerator getJoinPointGenerator(MethodByConInfo info);
       Object getPerClassAspect(AspectDefinition def);
       Object getPerClassJoinpointAspect(AspectDefinition def, Joinpoint joinpoint);
       GeneratedClassAdvisor getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices();
       void createMethodTables() throws Exception;
       void createFieldTable() throws Exception;
       void createConstructorTables() throws Exception;
       Set getPerInstanceAspectDefinitions();
       Map getPerInstanceJoinpointAspectDefinitions();
       void rebuildInterceptors();
       void resolveConstructorPointcut(ArrayList newConstructorInfos, AdviceBinding binding);
       void resolveConstructionPointcut(ArrayList newConstructionInfos, AdviceBinding binding);
       void finalizeConstructorChain(ArrayList newConstructorInfos);
       void finalizeConstructionChain(ArrayList newConstructionInfos);
       void makeAccessibleField(FieldInfo fi);
       void makeAccessibleMethod(MethodInfo mi);
    }
    
    private class ClassAdvisorStrategy implements AdvisorStrategy
    {
       GeneratedClassAdvisor parent;
 
       public void initialise(Class clazz, AspectManager manager)
       {
          initialiseMethods();
          initialiseConstructors();
          initialiseConstructions();
          initialiseFieldReads();
          initialiseFieldWrites();
          
          GeneratedClassAdvisor.super.setManager(manager);
 
          //Make sure that we copy across per class and per joinpoint aspects from the old advisor if it exists
          //Generated advisors get created when the class is first accessed (not loaded), meaning that there could be an exisiting advisor
          //used for mathcing already when setting up the microcontainer.
          Advisor existing = AspectManager.instance().getAnyAdvisorIfAdvised(clazz);
          if (existing != null)
          {
             GeneratedClassAdvisor.this.aspects = existing.aspects;
             if (existing instanceof GeneratedClassAdvisor)
             {
                GeneratedClassAdvisor.this.perClassJoinpointAspectDefinitions = ((GeneratedClassAdvisor)existing).perClassJoinpointAspectDefinitions;
             }
          }
          
          manager.initialiseClassAdvisor(clazz, GeneratedClassAdvisor.this);
          
          initialiseCallers();
       }
 
       public void checkVersion()
       {
          //The version is only has any significance for instance advisors
       }
 
       public void createInterceptorChains() throws Exception
       {
          GeneratedClassAdvisor.super.createInterceptorChains();
       }
       
       public MethodJoinPointGenerator getJoinPointGenerator(MethodInfo info)
       {
          MethodJoinPointGenerator generator = (MethodJoinPointGenerator)joinPoinGenerators.get(info.getJoinpoint());
          if (generator == null)
          {
             generator = new MethodJoinPointGenerator(GeneratedClassAdvisor.this, info);
             joinPoinGenerators.put(info.getJoinpoint(), generator);
          }
          return generator;
       }
 
       public FieldJoinPointGenerator getJoinPointGenerator(FieldInfo info)
       {
          if (info.isRead())
          {
             FieldJoinPointGenerator generator = (FieldJoinPointGenerator)fieldReadJoinPoinGenerators.get(info.getJoinpoint());
             if (generator == null)
             {
                generator = new FieldJoinPointGenerator(GeneratedClassAdvisor.this, info);
                fieldReadJoinPoinGenerators.put(info.getJoinpoint(), generator);
             }
             return generator;
          }
          else
          {
             FieldJoinPointGenerator generator = (FieldJoinPointGenerator)joinPoinGenerators.get(info.getJoinpoint());
             if (generator == null)
             {
                generator = new FieldJoinPointGenerator(GeneratedClassAdvisor.this, info);
                joinPoinGenerators.put(info.getJoinpoint(), generator);
             }
             return generator;
          }
       }
       
       public ConstructorJoinPointGenerator getJoinPointGenerator(ConstructorInfo info)
       {
          //We are the class advisor
          ConstructorJoinPointGenerator generator = (ConstructorJoinPointGenerator)constructionJoinPointGenerators.get(info.getJoinpoint());
          if (generator == null)
          {
             generator = new ConstructorJoinPointGenerator(GeneratedClassAdvisor.this, info);
             constructionJoinPointGenerators.put(info.getJoinpoint(), generator);
          }
          return generator;
       }
 
       public ConstructionJoinPointGenerator getJoinPointGenerator(ConstructionInfo info)
       {
          ConstructionJoinPointGenerator generator = (ConstructionJoinPointGenerator)joinPoinGenerators.get(info.getJoinpoint());
          if (generator == null)
          {
             generator = new ConstructionJoinPointGenerator(GeneratedClassAdvisor.this, info);
             joinPoinGenerators.put(info.getJoinpoint(), generator);
          }
          return generator;
       }
 
       public MethodByMethodJoinPointGenerator getJoinPointGenerator(MethodByMethodInfo info)
       {
          //An extra level of indirection since we distinguish between callers of method depending on
          //where the called method is defined (sub/super interfaces)
          ConcurrentHashMap map = (ConcurrentHashMap)joinPoinGenerators.get(info.getJoinpoint());
          if (map == null)
          {
             map = new ConcurrentHashMap();
             joinPoinGenerators.put(info.getJoinpoint(), map);
             map = (ConcurrentHashMap)joinPoinGenerators.get(info.getJoinpoint());
          }
 
          MethodByMethodJoinPointGenerator generator = (MethodByMethodJoinPointGenerator)map.get(info.getCalledClass());
          if (generator == null)
          {
             generator = new MethodByMethodJoinPointGenerator(GeneratedClassAdvisor.this, info);
             map.put(info.getCalledClass(), generator);
             generator = (MethodByMethodJoinPointGenerator)map.get(info.getCalledClass());
          }
          return generator;
       }
 
       public ConByMethodJoinPointGenerator getJoinPointGenerator(ConByMethodInfo info)
       {
          ConByMethodJoinPointGenerator generator = (ConByMethodJoinPointGenerator)joinPoinGenerators.get(info.getJoinpoint());
          if (generator == null)
          {
             generator = new ConByMethodJoinPointGenerator(GeneratedClassAdvisor.this, info);
             joinPoinGenerators.put(info.getJoinpoint(), generator);
          }
          return generator;
       }
 
       public ConByConJoinPointGenerator getJoinPointGenerator(ConByConInfo info)
       {
          //We are the class advisor
          ConByConJoinPointGenerator generator = (ConByConJoinPointGenerator)joinPoinGenerators.get(info.getJoinpoint());
          if (generator == null)
          {
             generator = new ConByConJoinPointGenerator(GeneratedClassAdvisor.this, info);
             joinPoinGenerators.put(info.getJoinpoint(), generator);
          }
          return generator;
       }
 
       public MethodByConJoinPointGenerator getJoinPointGenerator(MethodByConInfo info)
       {
          //An extra level of indirection since we distinguish between callers of method depending on
          //where the called method is defined (sub/super interfaces)
          ConcurrentHashMap map = (ConcurrentHashMap)joinPoinGenerators.get(info.getJoinpoint());
          if (map == null)
          {
             map = new ConcurrentHashMap();
             joinPoinGenerators.put(info.getJoinpoint(), map);
             map = (ConcurrentHashMap)joinPoinGenerators.get(info.getJoinpoint());
          }
 
          MethodByConJoinPointGenerator generator = (MethodByConJoinPointGenerator)map.get(info.getCalledClass());
          if (generator == null)
          {
             generator = new MethodByConJoinPointGenerator(GeneratedClassAdvisor.this, info);
             map.put(info.getCalledClass(), generator);
             generator = (MethodByConJoinPointGenerator)map.get(info.getCalledClass());
          }
          return generator;
       }
 
       public Object getPerClassAspect(AspectDefinition def)
       {
          return GeneratedClassAdvisor.super.getPerClassAspect(def);
       }
 
       public Object getPerClassJoinpointAspect(AspectDefinition def, Joinpoint joinpoint)
       {
          Map joinpoints = (Map) perClassJoinpointAspectDefinitions.get(def);
          if (joinpoints != null)
          {
             return joinpoints.get(joinpoint);
          }
          return null;
       }
       
       public GeneratedClassAdvisor getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices()
       {
          return null;
       }
       
       public void createMethodTables() throws Exception
       {
          GeneratedClassAdvisor.super.createMethodTables();
       }
       
       public void createFieldTable() throws Exception
       {
          GeneratedClassAdvisor.super.createFieldTable();
       }
 
       public void createConstructorTables() throws Exception
       {
          GeneratedClassAdvisor.super.createConstructorTables();
       }
       
       public Set getPerInstanceAspectDefinitions()
       {
          return GeneratedClassAdvisor.super.getPerInstanceAspectDefinitions();
       }
 
       public Map getPerInstanceJoinpointAspectDefinitions()
       {
          return GeneratedClassAdvisor.super.getPerInstanceJoinpointAspectDefinitions();
       }
       
       public void rebuildInterceptors()
       {
          version++;
          GeneratedClassAdvisor.super.rebuildInterceptors();
       }
 
       public void resolveConstructorPointcut(ArrayList newConstructorInfos, AdviceBinding binding)
       {
          GeneratedClassAdvisor.super.resolveConstructorPointcut(newConstructorInfos, binding);
       }
 
       public void resolveConstructionPointcut(ArrayList newConstructionInfos, AdviceBinding binding)
       {
          GeneratedClassAdvisor.super.resolveConstructionPointcut(newConstructionInfos, binding);
       }
 
       public void finalizeConstructorChain(ArrayList newConstructorInfos)
       {
          for (int i = 0; i < newConstructorInfos.size(); i++)
          {
             ConstructorInfo info = (ConstructorInfo) newConstructorInfos.get(i);
             ConstructorJoinPointGenerator generator = getJoinPointGenerator(info);
             finalizeChainAndRebindJoinPoint(oldInfos, info, generator);
          }
       }
 
       public void finalizeConstructionChain(ArrayList newConstructionInfos)
       {
          for (int i = 0; i < newConstructionInfos.size(); i++)
          {
             ConstructionInfo info = (ConstructionInfo) newConstructionInfos.get(i);
             ConstructionJoinPointGenerator generator = getJoinPointGenerator(info);
             finalizeChainAndRebindJoinPoint(oldConstructionInfos, info, generator);
          }
       }
 
       public void makeAccessibleField(FieldInfo fi)
       {
          //If we do dynamic invokes the field will need to be accessible via reflection
          SecurityActions.setAccessible(fi.getAdvisedField());
       }
       
       public void makeAccessibleMethod(MethodInfo mi)
       {
          //If we do dynamic invokes the method will need to be accessible via reflection if private/protected
          SecurityActions.setAccessible(mi.getAdvisedMethod());
       }
    }
    
    private class InstanceAdvisorStrategy implements AdvisorStrategy 
    {
       GeneratedClassAdvisor parent;
       boolean needsRebuild = true;
       
       public InstanceAdvisorStrategy(GeneratedClassAdvisor parent)
       {
          this.parent = parent;
          GeneratedClassAdvisor.this.version = parent.version;         
       }
       
       public void initialise(Class clazz, AspectManager manager)
       {
          initialiseInfosForInstance();
          
          GeneratedClassAdvisor.super.setManager(manager);
 
          manager.initialiseClassAdvisor(clazz, GeneratedClassAdvisor.this);
       }
 
       public void checkVersion()
       {
          if (needsRebuild || parent.version != GeneratedClassAdvisor.this.version)
          {
             doRebuildForInstance();
             needsRebuild = false;
          }
       }
       
       public void createInterceptorChains() throws Exception
       {
          if (GeneratedClassAdvisor.super.initialized)
          {
             GeneratedClassAdvisor.super.createInterceptorChains();
          }
          else
          {
             //Instance advisor copies the chains from the class advisor during its initialise stage 
             GeneratedClassAdvisor.super.initialized = true;
          }
       }
 
       public MethodJoinPointGenerator getJoinPointGenerator(MethodInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
 
       public FieldJoinPointGenerator getJoinPointGenerator(FieldInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
       
       public ConstructorJoinPointGenerator getJoinPointGenerator(ConstructorInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
 
       public ConstructionJoinPointGenerator getJoinPointGenerator(ConstructionInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
 
       public MethodByMethodJoinPointGenerator getJoinPointGenerator(MethodByMethodInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
 
       public ConByMethodJoinPointGenerator getJoinPointGenerator(ConByMethodInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
 
       public ConByConJoinPointGenerator getJoinPointGenerator(ConByConInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
 
       public MethodByConJoinPointGenerator getJoinPointGenerator(MethodByConInfo info)
       {
          //We are an instance advisor, get the generator from the class advisor
          return parent.getJoinPointGenerator(info);
       }
 
       /**
        * This is an instance advisor, so we will check with parent class advisor if the aspect
        * is already registered. If so, we should use the one from the parent advisor
        */
       public Object getPerClassAspect(AspectDefinition def)
       {
          Object aspect = parent.getPerClassAspect(def);
          if (aspect != null) return aspect;
          return GeneratedClassAdvisor.super.getPerClassAspect(def);
       }
 
       /**
        * This is an instance advisor, so we will check with parent class advisor if the aspect
        * is already registered. If so, we should use the one from the parent advisor
        */
       public Object getPerClassJoinpointAspect(AspectDefinition def, Joinpoint joinpoint)
       {
          Object aspect = parent.getPerClassJoinpointAspect(def, joinpoint);
          if (aspect != null)return aspect;
          return parent.getPerClassJoinpointAspect(def, joinpoint);
       }
 
       public GeneratedClassAdvisor getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices()
       {
          if (((Domain)getManager()).hasOwnDataWithEffectOnAdvices())
          {
             return null;
          }
          return parent;
       }
       
       public void createMethodTables() throws Exception
       {
          GeneratedClassAdvisor.this.unadvisedMethods = parent.unadvisedMethods;
          GeneratedClassAdvisor.this.advisedMethods = parent.advisedMethods;
       }
 
       public void createFieldTable() throws Exception
       {
          GeneratedClassAdvisor.this.advisedFields = parent.advisedFields;
       }
 
       public void createConstructorTables() throws Exception
       {
          GeneratedClassAdvisor.this.constructors = parent.constructors;
          
          methodCalledByConBindings = new HashMap[constructors.length];
          methodCalledByConInterceptors = new HashMap[constructors.length];
 
          conCalledByConBindings = new HashMap[constructors.length];
          conCalledByConInterceptors = new HashMap[constructors.length];
       }
 
       public Set getPerInstanceAspectDefinitions()
       {
          return parent.getPerInstanceAspectDefinitions();
       }
 
       public Map getPerInstanceJoinpointAspectDefinitions()
       {
          return parent.getPerInstanceJoinpointAspectDefinitions();
       }
 
       
       public synchronized void rebuildInterceptors()
       {
          if (getClassAdvisorIfInstanceAdvisorWithNoOwnDataWithEffectOnAdvices() != null && GeneratedClassAdvisor.this.version != parent.version)
          {
             adviceBindings.clear();
             needsRebuild = true;
          }
          else
          {
             GeneratedClassAdvisor.super.rebuildInterceptors();
          }
       }
 
       public void resolveConstructorPointcut(ArrayList newConstructorInfos, AdviceBinding binding)
       {
          //Since the instance already exists it makes no sense to have bindings for constructors
       }
 
       public void resolveConstructionPointcut(ArrayList newConstructionInfos, AdviceBinding binding)
       {
          //Since the instance already exists it makes no sense to have bindings for constructors         
       }
 
       public void finalizeConstructorChain(ArrayList newConstructorInfos)
       {
          //Since the instance already exists it makes no sense to have bindings for constructors
       }
 
       public void finalizeConstructionChain(ArrayList newConstructionInfos)
       {
          //Since the instance already exists it makes no sense to have bindings for constructors
       }
 
       public void makeAccessibleField(FieldInfo fi)
       {
          //Do nothing, field was already made accessible in class advisor
       }
 
       public void makeAccessibleMethod(MethodInfo mi)
       {
          //Do nothing, field was already made accessible in class advisor
       }
    }
 }
