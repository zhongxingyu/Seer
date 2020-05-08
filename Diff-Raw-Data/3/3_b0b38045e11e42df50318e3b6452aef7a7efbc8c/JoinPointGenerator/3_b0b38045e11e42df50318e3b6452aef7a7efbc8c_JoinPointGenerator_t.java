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
 package org.jboss.aop.instrument;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.CtNewConstructor;
 import javassist.CtNewMethod;
 import javassist.Modifier;
 import javassist.NotFoundException;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.CallerConstructorInfo;
 import org.jboss.aop.ConByConInfo;
 import org.jboss.aop.GeneratedClassAdvisor;
 import org.jboss.aop.InstanceAdvisor;
 import org.jboss.aop.JoinPointInfo;
 import org.jboss.aop.advice.AdviceMethodProperties;
 import org.jboss.aop.advice.GeneratedAdvisorInterceptor;
 import org.jboss.aop.advice.Scope;
 import org.jboss.aop.advice.annotation.AdviceMethodFactory;
 import org.jboss.aop.joinpoint.Invocation;
 import org.jboss.aop.pointcut.ast.ASTCFlowExpression;
 import org.jboss.aop.pointcut.ast.ClassExpression;
 import org.jboss.aop.util.JavassistUtils;
 import org.jboss.aop.util.ReflectToJavassist;
 import org.jboss.util.collection.temp.WeakValueHashMap;
 
 /**
  * Creates the Joinpoint invocation replacement classes used with Generated advisors
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision$
  */
 public abstract class JoinPointGenerator
 {
    public static final String INFO_FIELD = "info";
    public static final String INVOKE_JOINPOINT = "invokeJoinpoint";
    public static final String INVOKE_TARGET = "invokeTarget";
    public static final String DISPATCH = "dispatch";
    protected static final String TARGET_FIELD = "typedTargetObject";
    protected static final String CALLER_FIELD = "callingObject";
    protected static final String GENERATED_CLASS_ADVISOR = GeneratedClassAdvisor.class.getName();
    public static final String GENERATE_JOINPOINT_CLASS = "generateJoinPointClass";
    private static final String CURRENT_ADVICE = "super.currentInterceptor";
    public static final String JOINPOINT_FIELD_PREFIX = "joinpoint_";
    public static final String JOINPOINT_CLASS_PREFIX = "JoinPoint_";
    private static final String RETURN_VALUE = "ret";
    private static final String THROWABLE = "t";
    protected static final String ARGUMENTS= "arguments";
    private static final String GET_ARGUMENTS= OptimizedBehaviourInvocations.GET_ARGUMENTS + "()";
    protected static final CtClass[] EMPTY_CTCLASS_ARRAY = new CtClass[0];
    private final ArrayList<Integer> joinPointArguments;
    
    
    private JoinPointParameters parameters;
    private static int increment;
    private Class advisorClass;
    private String baseJoinPointClassName;
    protected String joinpointClassName;
    protected String joinpointFieldName;
    private String joinpointFqn;
    private Field joinpointField;
    private boolean initialised;
    private ThreadLocal<Set<Integer>> inconsistentTypeArgs;
    
    /**
    * A cache of the generated joinpoint classes indexed by the interceptor chains for the info to 
    * avoid having to generate a new class on every single rebind
     */
    private HashMap generatedJoinPointClassCache = new HashMap();
    
    protected JoinPointGenerator(GeneratedClassAdvisor advisor, JoinPointInfo info,
          JoinPointParameters parameters, int argumentsSize)
    {
 //      this.info = info;
       this.parameters = parameters;
 //      this.advisor = advisor;
       this.advisorClass = advisor.getClass();
       Class[] interfaces = advisorClass.getInterfaces();
       
       for (int i = 0 ; i < interfaces.length ; i++)
       {
          if (interfaces[i].equals(InstanceAdvisor.class))
          {
             //The InstanceAdvisor extends the Advisor, which is what contains the JoinPoint field
             advisorClass = advisorClass.getSuperclass();
             break;
          }
       }
       // helper collection
       joinPointArguments = new ArrayList<Integer>(argumentsSize);
       // iterate once so we don't need to iterate always (use this array instead)
       for (int i = 0; i < argumentsSize; i++)
       {
          joinPointArguments.add(i);
       }
       // joinpoint generation helper field, will contain list of typed args that are
       // inconsistent with arguments array
       // is ThreadLocal so we can support more than one Thread
       // generating the a joinpoint class
       inconsistentTypeArgs = new ThreadLocal<Set<Integer>>()
       {
          protected synchronized Set<Integer> initialValue() {
             return new HashSet<Integer>();
          }
       };
     
       initialiseJoinPointNames(info);
       findAdvisedField(advisorClass, info);
       initBaseJoinPointClassName(advisor);
    }
    
    private void initBaseJoinPointClassName(GeneratedClassAdvisor advisor)
    {
       Package pkg = advisor.getClass().getPackage();
       
       StringBuffer className = new StringBuffer();
       if (pkg != null)
       {
          className.append(pkg.getName());
          className.append(".");
       }
       className.append(joinpointClassName);
       className.append("_");
       baseJoinPointClassName = className.toString();
    }
    
    public void rebindJoinpoint(JoinPointInfo newInfo)
    {
       try
       {
          if (joinpointField == null) return;
          joinpointField.set(newInfo.getAdvisor(), null);
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }   
    }
       
    /**
     * Called by the joinpoint if a interceptors were regenereated
     * Here for backwards compatiblity with AOP 1.5.0
     */
    public void generateJoinPointClass()
    {
       generateJoinPointClass(null, null);
    }
    
    /**
     * Called by the joinpoint if a interceptors were regenereated
     */
    public synchronized void generateJoinPointClass(ClassLoader classloader, JoinPointInfo info)
    {
       if (info == null)
       {
          throw new RuntimeException("GeneratedAdvisor weaving in AOP 2.0.0.aplha5 and later is not compatible with that of previous versions");
       }
       
       if (System.getSecurityManager() == null)
       {
          GenerateJoinPointClassAction.NON_PRIVILEGED.generateJoinPointClass(classloader, this, info);
       }
       else
       {
          GenerateJoinPointClassAction.PRIVILEGED.generateJoinPointClass(classloader, this, info);
       }
    }
     
    /**
     * Does the work for generateJoinPointClass()
     * @see JoinPointGenerator#generateJoinPointClass()
     */
    private void doGenerateJoinPointClass(ClassLoader classloader, JoinPointInfo info)
    {
       try
       {
          if (classloader  == null)
          {
             classloader = Thread.currentThread().getContextClassLoader();
          }
 
          //Attempt to get the cached information so we don't have to recreate the class every time we rebind the joinpoint
          String infoAdviceString = info.getAdviceString();
          GeneratedClassInfo generatedClass = (GeneratedClassInfo)generatedJoinPointClassCache.get(infoAdviceString);
          Class clazz = null;
          if (generatedClass != null)
          {
             clazz = classloader.loadClass(generatedClass.getGenerated().getName());
          }
          
          if (clazz == null)
          {
             //We need to do all the work again
             AspectManager manager = AspectManager.instance();
             ClassPool pool = manager.findClassPool(classloader);
             generatedClass = generateJoinpointClass(pool, info);
             
             ProtectionDomain pd = advisorClass.getProtectionDomain();
             clazz = toClass(pool, generatedClass.getGenerated(), pd);
             generatedJoinPointClassCache.put(infoAdviceString, generatedClass);
          }
          Object obj = instantiateClass(clazz, generatedClass.getAroundSetups(), info);
          
          joinpointField.set(info.getAdvisor(), obj);
       }
       catch (Throwable e)
       {
          e.printStackTrace();
          // AutoGenerated
          throw new RuntimeException("Error generating joinpoint class for joinpoint " + info, e);
          
       }
       initialised = true;
    }
 
    private Class toClass(ClassPool pool, CtClass ctclass, ProtectionDomain pd) throws NotFoundException, CannotCompileException, ClassNotFoundException
    {
       return TransformerCommon.toClass(ctclass, pd);
    }
    
    private Object instantiateClass(Class clazz, AdviceSetup[] aroundSetups, JoinPointInfo info) throws Exception
    {
       Constructor ctor = clazz.getConstructor(new Class[] {info.getClass()});
       Object obj;
       try
       {
          obj = ctor.newInstance(new Object[] {info});
       }
       catch (Exception e)
       {
          StringBuffer sb = new StringBuffer();
          throw new RuntimeException(debugClass(sb, clazz).toString(), e);
       }
       
       for (int i = 0 ; i < aroundSetups.length ; i++)
       {
          if (aroundSetups[i].isNewCFlow())
          {
             Field field = clazz.getDeclaredField("cflow" + aroundSetups[i].useCFlowFrom());
             field.setAccessible(true);
             field.set(obj, aroundSetups[i].getCFlow());
          }
       }
       return obj;
    }
     
    private StringBuffer debugClass(StringBuffer sb, Class clazz)
    {
       sb.append("\n\t\t" + Modifier.toString(clazz.getModifiers()) + " " + clazz.getName() + " " + clazz.getClassLoader()); 
       Field[] fields = clazz.getDeclaredFields();
       for (int i = 0 ; i < fields.length ; i++)
       {
          sb.append("\n\t\t\t" + Modifier.toString(fields[i].getModifiers()) + " " + fields[i].getType().getName() + " " + fields[i].getName() + " " + fields[i].getType().getClassLoader());
       }
      
      Class superClass = clazz.getSuperclass();
      if (superClass != null && superClass != Object.class)
      {
         sb.append("\n\t\t\textends\n");
         debugClass(sb, superClass);
      }
      return sb;
    }
    
    
    private static synchronized int getIncrement()
    {
       return ++increment;
    }
    
    protected abstract void initialiseJoinPointNames(JoinPointInfo info);
 
    private GeneratedClassInfo generateJoinpointClass(ClassPool pool, JoinPointInfo newInfo) throws NotFoundException,
    CannotCompileException, ClassNotFoundException
    {
       CtClass superClass = pool.get(joinpointFqn);
       String className = getJoinpointClassName();
       try
       {
          CtClass clazz = TransformerCommon.makeClass(pool, className);
          clazz.setSuperclass(superClass);
          addUntransformableInterface(pool, clazz);
   
          AdviceSetupsByType setups = initialiseAdviceInfosAndAddFields(pool, clazz, newInfo);
          
          createConstructors(pool, superClass, clazz, setups);
          createJoinPointInvokeMethod(
                superClass, 
                clazz, 
                isVoid(),
                setups,
                newInfo);
   
          createInvokeNextMethod(clazz, isVoid(), setups.getAroundSetups(), newInfo);
   
          overrideDispatchMethods(superClass, clazz, newInfo);
          return new GeneratedClassInfo(clazz, setups.getAroundSetups());
       }
       catch (NotFoundException e)
       {
          System.err.println("Exception generating " + className + ": " + e.getMessage());
          throw e;
       }
       catch (CannotCompileException e)
       {
          System.err.println("Exception generating " + className + ": " + e.getMessage());
          throw e;
       }
       catch (ClassNotFoundException e)
       {
          System.err.println("Exception generating " + className + ": " + e.getMessage());
          throw e;
       }
    }
 
    private String getJoinpointClassName()
    {
       return baseJoinPointClassName + getIncrement();
    }
 
    protected abstract boolean isVoid();
    protected abstract Class getReturnType(); 
    protected abstract AdviceMethodProperties getAdviceMethodProperties(JoinPointInfo info, AdviceSetup setup);
    
    protected boolean isCaller()
    {
       return false;
    }
    
    protected boolean hasCallingObject()
    {
       return false;
    }
    
    protected abstract boolean hasTargetObject();
    
    private boolean isStaticCall()
    {
       if (isCaller())
       {
          return !hasCallingObject();
       }
       else
       {
          return !hasTargetObject();
       }
    }
    
    private void findAdvisedField(Class advisorSuperClazz, JoinPointInfo info)
    {
       if (info.getClazz() == null)
       {
          return;
       }
       
       while (advisorSuperClazz != null && advisorSuperClazz.getDeclaringClass() != info.getClazz())
       {
          advisorSuperClazz = advisorSuperClazz.getSuperclass();
       }
       try
       {
          try
          {
             joinpointField = advisorSuperClazz.getDeclaredField(joinpointFieldName);
             SecurityActions.setAccessible(joinpointField);
             joinpointFqn = advisorSuperClazz.getDeclaringClass().getName() + "$" + joinpointClassName;
          }
          catch (NoSuchFieldException e)
          {
             //GeneratedClassAdvisor is the base class for all generated advisors
             if (!advisorSuperClazz.getName().equals(GENERATED_CLASS_ADVISOR))
             {
                findAdvisedField(advisorSuperClazz.getSuperclass(), info);
             }
          }
       }
       catch (NoClassDefFoundError e)
       {
          throw e;
       }
    }
 
    private AdviceSetupsByType initialiseAdviceInfosAndAddFields(ClassPool pool, CtClass clazz, JoinPointInfo info) throws ClassNotFoundException, NotFoundException, CannotCompileException
    {
       HashMap<String, Integer> cflows = new HashMap<String, Integer>();
       AdviceSetup[] setups = new AdviceSetup[info.getInterceptors().length];
 
       for (int i = 0 ; i < info.getInterceptors().length ; i++)
       {
          setups[i] = new AdviceSetup(i, (GeneratedAdvisorInterceptor)info.getInterceptors()[i], info);
          addAspectFieldAndGetter(pool, clazz, setups[i]);
          addCFlowFieldsAndGetters(pool, setups[i], clazz, cflows);
       }
    
       return new AdviceSetupsByType(info, setups);
    }
    
    private void addAspectFieldAndGetter(ClassPool pool, CtClass clazz, AdviceSetup setup) throws NotFoundException, CannotCompileException
    {
       CtClass aspectClass = setup.getAspectCtClass();
       
       if (!setup.shouldInvokeAspect())
       {
          return;
       }
 
       CtField field = new CtField(aspectClass, setup.getAspectFieldName(), clazz);
       field.setModifiers(Modifier.PRIVATE | Modifier.TRANSIENT);
       clazz.addField(field);
       
       String body = getAspectFieldGetterBody(setup); 
       CtMethod method = CtNewMethod.make(
             aspectClass, 
             setup.getAspectInitialiserName(), 
             new CtClass[0], 
             new CtClass[0], 
             body, 
             clazz);
       method.setModifiers(Modifier.PRIVATE);
       clazz.addMethod(method);
    }
 
    private String getAspectFieldGetterBody(AdviceSetup setup)
    {
       if (setup.requiresInstanceAdvisor())
       {
          String instanceAdvisor = (isCaller()) ?
                "org.jboss.aop.InstanceAdvisor ia = ((org.jboss.aop.Advised)callingObject)._getInstanceAdvisor();" :
                   "org.jboss.aop.InstanceAdvisor ia = ((org.jboss.aop.Advised)targetObject)._getInstanceAdvisor();";
                         
          return
             "{" +
             "   " + instanceAdvisor +
             "   org.jboss.aop.advice.GeneratedAdvisorInterceptor fw = (org.jboss.aop.advice.GeneratedAdvisorInterceptor)info.getInterceptors()[" + setup.getIndex() + "];" +
             "   Object o = fw.getPerInstanceAspect(info.getAdvisor(), info.getJoinpoint(), ia);" +
             "   return (" + setup.getAspectClass().getName() + ")o;" +
             "}";
       }
       else
       {
          return
             "{" +
             "   if (" + setup.getAspectFieldName() + " != null)" +
             "   {" +
             "      return " + setup.getAspectFieldName() + ";" +
             "   }" +
             "   org.jboss.aop.advice.GeneratedAdvisorInterceptor fw = (org.jboss.aop.advice.GeneratedAdvisorInterceptor)info.getInterceptors()[" + setup.getIndex() + "];" +
             "   Object o = fw.getAspect(info.getAdvisor(), info.getJoinpoint());" +
             "   return (" + setup.getAspectClass().getName() + ")o;" +
             "}";
       }
    }
    private void addCFlowFieldsAndGetters(ClassPool pool, AdviceSetup setup,
          CtClass clazz, HashMap<String, Integer> cflows)
          throws NotFoundException, CannotCompileException
    {
       if (setup.getCFlowString() != null)
       {
          Integer useCFlowIndex = cflows.get(setup.getCFlowString());
          if (useCFlowIndex == null)
          {
             useCFlowIndex = new Integer(setup.getIndex());
             cflows.put(setup.getCFlowString(), useCFlowIndex);
             
             CtField cflowX = new CtField(
                   pool.get(ASTCFlowExpression.class.getName()), 
                   "cflow" + useCFlowIndex, 
                   clazz);
             clazz.addField(cflowX);
             
             CtField matchesCFlowX = new CtField(
                   CtClass.booleanType, 
                   "matchesCflow" + useCFlowIndex, 
                   clazz);
             clazz.addField(matchesCFlowX);
             
             String initCFlowXBody = 
                "{" +
                "   org.jboss.aop.pointcut.CFlowMatcher matcher = new org.jboss.aop.pointcut.CFlowMatcher();" +
                "   return matcher.matches(" + cflowX.getName() + ", this);" +
                "}";
             CtMethod initCFlowX = CtNewMethod.make(
                   CtClass.booleanType,
                   "getCFlow" + useCFlowIndex,
                   new CtClass[0],
                   new CtClass[0],
                   initCFlowXBody,
                   clazz);
             clazz.addMethod(initCFlowX);
          }
          setup.setUseCFlowFrom(useCFlowIndex.intValue());
       }
    }
    
    private void createJoinPointInvokeMethod(CtClass superClass, CtClass clazz, boolean isVoid, AdviceSetupsByType setups, JoinPointInfo info) throws CannotCompileException, NotFoundException
    {
       CtMethod superInvoke = superClass.getDeclaredMethod(INVOKE_JOINPOINT);
       String code = null;
       try
       {
          code = createJoinpointInvokeBody(
                clazz, 
                setups,
                superInvoke.getExceptionTypes(), superInvoke.getParameterTypes(), info);
          CtMethod invoke = CtNewMethod.make(
                superInvoke.getReturnType(), 
                superInvoke.getName(), 
                superInvoke.getParameterTypes(), 
                superInvoke.getExceptionTypes(), 
                code, 
                clazz);
          clazz.addMethod(invoke);
       }
       catch (CannotCompileException e)
       {
          throw new RuntimeException("Error compiling code for Joinpoint (" + info.getJoinpoint() +"): " + code + "\n - " + e + "\n - " + getMethodString(clazz, superInvoke.getName(), superInvoke.getParameterTypes()) + "\n - " + clazz.getName(), e);
       }
    }
    
    private String createJoinpointInvokeBody(CtClass joinpointClass,
          AdviceSetupsByType setups, CtClass[] declaredExceptions, CtClass[] parameterTypes, JoinPointInfo info)throws NotFoundException
    {
       StringBuffer code = new StringBuffer();
       code.append("{");
       if (!isVoid())
       {
          String ret = null;
          Class retType = getReturnType();
          if (retType.isPrimitive())
          {
             if (retType.equals(Boolean.TYPE)) ret = "false";
             else if (retType.equals(Character.TYPE)) ret = "'\\0'";
             else if (retType.equals(Byte.TYPE)) ret = "(byte)0";
             else if (retType.equals(Short.TYPE)) ret = "(short)0";
             else if (retType.equals(Integer.TYPE)) ret = "(int)0";
             else if (retType.equals(Long.TYPE)) ret = "0L";
             else if (retType.equals(Float.TYPE)) ret = "0.0f";
             else if (retType.equals(Double.TYPE)) ret =  "0.0d";
          }
          code.append("   " + ClassExpression.simpleType(getReturnType()) + "  " + RETURN_VALUE + " = " + ret + ";");
       }
       code.append("   try");
       code.append("   {");
       boolean argsFoundBefore = DefaultAdviceCallStrategy.getInstance().
          addInvokeCode(this, setups.getBeforeSetups(), code, info);
       
       // add around according to whether @Args were found before
       boolean joinPointCreated = addAroundInvokeCode(code, setups, joinpointClass,
             argsFoundBefore, parameterTypes);
       
       // generate after code
       StringBuffer afterCode = new StringBuffer();
       boolean argsFoundAfter = AfterAdviceCallStrategy.getInstance().addInvokeCode(
             this, setups.getAfterSetups(), afterCode, info);
       afterCode.append("   }");
       afterCode.append("   catch(java.lang.Throwable " + THROWABLE + ")");
       afterCode.append("   {");
       argsFoundAfter = DefaultAdviceCallStrategy.getInstance().addInvokeCode(this,
             setups.getThrowingSetups(), afterCode, info) || argsFoundAfter;
       
       // if joinpoint has been created for around,
       // need to update arguments variable when this variable is used,
       // which happens in one of both cases
       // 1.an @Args parameter is found on after code
       // 2.an @Arg parameter is found on after code (in this case, we need
       //   to update the variable value according to what is contained in joinpoint)
       if (joinPointCreated &&  (argsFoundAfter ||
             inconsistentTypeArgs.get().size() < joinPointArguments.size()))
          // TODO ((argsFoundAfter || argsFoundBefore) && joinPointCreated) ||
       {
          code.append(ARGUMENTS);
          code.append(" = jp.").append(GET_ARGUMENTS).append(";");
          argsFoundAfter = true; // force creation of arguments variable
       }
       
       // add after code
       code.append(afterCode.toString());
       // finish code body
       addHandleExceptionCode(code, declaredExceptions);
       code.append("   }");
       if (!isVoid())
       {
          code.append("   return " + RETURN_VALUE + ";");
       }
       code.append("}");;
       
       // declare arguments array if necessary
       if (argsFoundBefore || argsFoundAfter)
       {
          code.insert(1, parameters.declareArgsArray(parameterTypes.length));
       }
       return code.toString();
    }
 
    private boolean addAroundInvokeCode(StringBuffer code, AdviceSetupsByType setups,
          CtClass joinpointClass, boolean argsFoundBefore, CtClass[] parameterTypes)
    throws NotFoundException
    {
       if (setups.getAroundSetups() != null)
       {
          StringBuffer aspects = new StringBuffer();
          StringBuffer cflows = new StringBuffer();
          
          AdviceSetup[] asetups = setups.getAllSetups(); 
          for (int i = 0 ; i < asetups.length ; i++)
          {
             if (asetups[i].requiresInstanceAdvisor())
             {
             }
             else
             {
                aspects.append(", ");
                aspects.append(asetups[i].getAspectFieldName());
             }
          
             if (asetups[i].isNewCFlow())
             {
                cflows.append(", cflow" + asetups[i].getIndex());
             }
          }
          code.append(joinpointFqn).append(" jp = null;");
          code.append("      if(" + INFO_FIELD + ".getInterceptors() != null)");
          code.append("      {");
          code.append("         jp = new " + joinpointClass.getName() + "(this");
          if (argsFoundBefore)
          {
             parameters.appendParameterListWithoutArgs(code);
             
          }
          else
          {
             code.append(", $$");
          }
          
          code.append(aspects.toString() + cflows.toString() + ");");
          
          if (argsFoundBefore)
          {
             code.append("   jp.setArguments(");
             code.append(ARGUMENTS);
             code.append(");");
          }
          
          if (!isVoid())
          {
             code.append("          " + RETURN_VALUE + " = ($r)");
          }
          code.append("jp.invokeNext();");
          
          code.append("      }");
          code.append("      else");
          code.append("      {");
          
          addDispatchCode(code, parameterTypes, argsFoundBefore);
          
          code.append("      }");
          
          // 'after' code will find all args inconsistent, since we have to update
          // arguments array according to invocation values
          inconsistentTypeArgs.get().addAll(joinPointArguments);
          return true;
       }
       else
       {
          addDispatchCode(code, parameterTypes, argsFoundBefore);
          return false;
       }
    }
 
    private final void addDispatchCode(StringBuffer code, CtClass[] parameterTypes,
          boolean argsFound)
    {
       if (! isVoid())
       {
          code.append("          " + RETURN_VALUE + " = ($r)");
       }
       code.append("super.dispatch(");
       if (argsFound)
       {
          parameters.appendParameterList(code, parameterTypes);
       }
       else
       {
          code.append("$$");
       }
       code.append(");");
    }
    
    private void addHandleExceptionCode(StringBuffer code, CtClass[] declaredExceptions)
    {
       for (int i = 0 ; i < declaredExceptions.length ; i++)
       {
          code.append("if (t instanceof " + declaredExceptions[i].getName() + ")");
          code.append("   throw (" + declaredExceptions[i].getName() + ")t;");
       }
       
       code.append("if (t instanceof java.lang.RuntimeException)");
       code.append(   "throw t;");
       
       code.append("throw new java.lang.RuntimeException(t);");
    }
 
    private void createInvokeNextMethod(CtClass jp, boolean isVoid, AdviceSetup[] aroundSetups, JoinPointInfo info) throws NotFoundException, CannotCompileException
    {
       if (aroundSetups == null) return;
       
       CtMethod method = jp.getSuperclass().getSuperclass().getDeclaredMethod("invokeNext");
       CtMethod invokeNext = CtNewMethod.copy(method, jp, null);
       
       String code = createInvokeNextMethodBody(jp, isVoid, aroundSetups, info);
       
       try
       {
          invokeNext.setBody(code);
       }
       catch (CannotCompileException e)
       {
          throw new RuntimeException("Error creating invokeNext method: " + code, e);
       }
       
       jp.addMethod(invokeNext);
    }
 
    private String createInvokeNextMethodBody(CtClass jp, boolean isVoid, AdviceSetup[] aroundSetups, JoinPointInfo info) throws NotFoundException
    {
       final String returnStr = (isVoid) ? "" : "return ($w)";
 
       StringBuffer body = new StringBuffer();
       body.append("{");
       body.append("   try{");
       body.append("      switch(++" + CURRENT_ADVICE + "){");
       AroundAdviceCallStrategy.getInstance().addInvokeCode(this, aroundSetups, body, info);
       body.append("      default:");
       body.append("         " + returnStr + "this.dispatch();");
       body.append("      }");
       body.append("   }finally{");
       body.append("      --" + CURRENT_ADVICE + ";");
       body.append("   }");
       body.append("   return null;");
       body.append("}");
       
       return body.toString();
    }
    
    private void createConstructors(ClassPool pool, CtClass superClass, CtClass clazz, AdviceSetupsByType setups) throws NotFoundException, CannotCompileException
    {
       CtConstructor[] superCtors = superClass.getDeclaredConstructors();
       if (superCtors.length != 3 && superCtors.length != 2 && !this.getClass().equals(MethodJoinPointGenerator.class)
             && !FieldJoinPointGenerator.class.isAssignableFrom(this.getClass()))
       {
          throw new RuntimeException("JoinPoints should have 2 or 3 constructors, not " + superCtors.length);
       }
       else if (superCtors.length != 4 && superCtors.length != 3 && this.getClass().equals(MethodJoinPointGenerator.class))
       {
          throw new RuntimeException("Method JoinPoints should have 3 or 4 constructors, not " + superCtors.length);
       }
       
       int publicIndex = -1;
       int protectedIndex1 = -1;
       int protectedIndex2 = -1;
       int defaultIndex = -1;
       
       for (int i = 0 ; i < superCtors.length ; i++)
       {
          int modifier = superCtors[i].getModifiers();
          if (Modifier.isPublic(modifier))
          {
             if (superCtors[i].getParameterTypes().length == 0) defaultIndex = i;
             else publicIndex = i;
          }
          else if (Modifier.isProtected(modifier))
          {
             if (protectedIndex1 == -1)
             {
                protectedIndex1 = i;
             }
             else
             {
                protectedIndex2 = i;
             }
          }
       }
       
       if (publicIndex < 0 || protectedIndex1 < 0)
       {
          throw new RuntimeException("One of the JoinPoint constructors should be public, and at least one of them should be protected");
       }
       
       if (defaultIndex >= 0)
       {
          createDefaultConstructor(superCtors[defaultIndex], clazz);
       }
       
       createPublicConstructor(superCtors[publicIndex], clazz, setups);
       if (protectedIndex2 == -1)
       {
          createProtectedConstructors(pool, superCtors[protectedIndex1], null, clazz, setups);
       }
       else
       {
          createProtectedConstructors(pool, superCtors[protectedIndex1], superCtors[protectedIndex2], clazz, setups);
       }
       createCopyConstructorAndMethod(pool, clazz);
    }
 
    /**
     * Currently only method joinpoints need serialization and thus a default ctor
     */
    private void createDefaultConstructor(CtConstructor superCtor, CtClass clazz) throws CannotCompileException
    {
       CtConstructor ctor = CtNewConstructor.defaultConstructor(clazz);
       clazz.addConstructor(ctor);
    }
    
    /**
     * This is the constructor that will be called by the GeneratedClassAdvisor, make sure it
     * initialises all the non-per-instance aspects
     */
    private void createPublicConstructor(CtConstructor superCtor, CtClass clazz, AdviceSetupsByType setups)throws CannotCompileException, NotFoundException
    {
       StringBuffer body = new StringBuffer();
       try
       {
          body.append("{super($$);");
   
          //Initialise all the aspects not scoped per_instance or per_joinpoint
          AdviceSetup[] allSetups = setups.getAllSetups();
          for (int i = 0 ; i < allSetups.length ; i++)
          {
             if (!allSetups[i].requiresInstanceAdvisor())
             {
                body.append(allSetups[i].getAspectFieldName() + " = " + allSetups[i].getAspectInitialiserName() + "();");
             }
          }
          
          body.append("}");
 
          CtConstructor ctor = CtNewConstructor.make(superCtor.getParameterTypes(), superCtor.getExceptionTypes(), body.toString(), clazz);
          ctor.setModifiers(superCtor.getModifiers());
          clazz.addConstructor(ctor);
       }
       catch (CannotCompileException e)
       {
          // AutoGenerated
          throw new CannotCompileException("Error compiling. Code \n" + body.toString(), e);
       }
    }
    
    /**
     * These are the constructors that will be called by the invokeJoinPoint() method,
     * make sure it copies across all the non-per-instance aspects
     */
    private void createProtectedConstructors(ClassPool pool, CtConstructor superCtor1,
          CtConstructor superCtor2, CtClass clazz, AdviceSetupsByType setups)
          throws CannotCompileException, NotFoundException
    {
       
       ArrayList<AdviceSetup> aspects = new ArrayList<AdviceSetup>(); 
       ArrayList<Integer> cflows = new ArrayList<Integer>();
       StringBuffer adviceInit  = new StringBuffer(); 
       
       AdviceSetup[] allSetups = setups.getAllSetups();
       for (int i = 0 ; i < allSetups.length ; i++)
       {
          if (!allSetups[i].shouldInvokeAspect())
          {
             continue;
          }
          
          if (allSetups[i].requiresInstanceAdvisor())
          {
             adviceInit.append(allSetups[i].getAspectFieldName());
             adviceInit.append(" = ");
             adviceInit.append(allSetups[i].getAspectInitialiserName());
             adviceInit.append("();");
          }
          else
          {
             aspects.add(allSetups[i]);
          }
          
          if (allSetups[i].isNewCFlow())
          {
             cflows.add(allSetups[i].useCFlowFrom());
          }
       }
       createProtectedConstructor(pool, clazz, superCtor1, allSetups, aspects, cflows,
             adviceInit.toString());
       if (superCtor2 != null)
       {
          createProtectedConstructor(pool, clazz, superCtor2, allSetups, aspects, cflows,
             adviceInit.toString());
       }
    }
    
    private void createProtectedConstructor(ClassPool pool, CtClass clazz,
          CtConstructor superCtor, AdviceSetup[] allSetups,
          ArrayList<AdviceSetup> aspects, ArrayList<Integer> cflows,
          String aspectInitialization)
       throws NotFoundException, CannotCompileException
    {
       // Set up the parameters
       CtClass[] superParams = superCtor.getParameterTypes();
       CtClass[] params = new CtClass[superParams.length + aspects.size() + cflows.size()];
       System.arraycopy(superParams, 0, params, 0, superParams.length);
 
       StringBuffer init = new StringBuffer();
       for (int i = 0 ; i < aspects.size() ; i++)
       {
          AdviceSetup setup = (AdviceSetup)aspects.get(i);
          params[i + superParams.length] = setup.getAspectCtClass();
          init.append("this." + setup.getAspectFieldName() + " = $" + (i + superParams.length + 1) + ";");
       }
       final int aspectsLength = superParams.length + aspects.size();
       if (cflows.size() > 0 )
       {
          CtClass astCFlowExpr = pool.get(ASTCFlowExpression.class.getName());
          for (int i = 0 ; i < cflows.size() ; i++)
          {
             params[i + aspectsLength] = astCFlowExpr;
             init.append("cflow" + cflows.get(i) + "= $" + (i + aspectsLength + 1) + ";");
             init.append("matchesCflow" + cflows.get(i) + " = getCFlow" + allSetups[cflows.get(i)].useCFlowFrom() + "();");
          }
       }
       
       StringBuffer body = new StringBuffer("{super(");
       for (int i = 0 ; i < superParams.length ; i++)
       {
          if (i > 0)
          {
             body.append(", ");
          }
          body.append("$" + (i + 1));
       
       }
       body.append(");");
       body.append(aspectInitialization);
       body.append(init.toString());
       
       body.append("}");   
       CtConstructor ctor = CtNewConstructor.make(
           params, 
           superCtor.getExceptionTypes(),
           body.toString(),
           clazz);
          ctor.setModifiers(superCtor.getModifiers());
          clazz.addConstructor(ctor);
    }
 
 
    private void createCopyConstructorAndMethod(ClassPool pool, CtClass clazz) throws NotFoundException, CannotCompileException
    {
       //Add all fields from this class and all superclasses
       StringBuffer body = new StringBuffer();
       body.append("{");
       body.append("   super($1.info);");
 
       CtClass superClass = clazz;
       while (superClass != null && !superClass.getName().equals("java.lang.Object"))
       {
          CtField[] fields = superClass.getDeclaredFields();
          for (int i = 0 ; i < fields.length ; i++)
          {
             if (Modifier.isPrivate(fields[i].getModifiers()) && fields[i].getDeclaringClass() != clazz)
             {
                continue;
             }
             
             if (Modifier.isFinal(fields[i].getModifiers()) || Modifier.isStatic(fields[i].getModifiers()) )
             {
                continue;
             }
             
             body.append("   this." + fields[i].getName() + " = $1." + fields[i].getName() + ";");
          }
          superClass = superClass.getSuperclass();
       }
       body.append("}");
       
       CtConstructor copyCtor = CtNewConstructor.make(new CtClass[] {clazz}, new CtClass[0], body.toString(), clazz);
       copyCtor.setModifiers(Modifier.PRIVATE);
       clazz.addConstructor(copyCtor);
       
       CtMethod superCopy = pool.get(Invocation.class.getName()).getDeclaredMethod("copy");
       String copyBody = 
          "{" +
          "   return new " + clazz.getName() + "(this);" +
          "}";
       CtMethod copy = CtNewMethod.make(
             superCopy.getReturnType(), 
             superCopy.getName(), 
             new CtClass[0], 
             new CtClass[0], 
             copyBody, 
             clazz);
       clazz.setModifiers(Modifier.PUBLIC);
       clazz.addMethod(copy);
    }
 
    /**
     * Normal people don't want to override the dispatch method
     */
    protected void overrideDispatchMethods(CtClass superClass, CtClass clazz, JoinPointInfo newInfo) throws CannotCompileException, NotFoundException
    {     
    }
    
    /**
     * For ConByXXXX,  If target constructor is execution advised, replace it with a call to the constructor wrapper
     */
    protected void overrideDispatchMethods(CtClass superClass, CtClass clazz, CallerConstructorInfo cinfo) throws NotFoundException, CannotCompileException
    {
       if (cinfo.getWrappingMethod() == null)
       {
          return;
       }
       
       CtMethod[] superDispatches = JavassistUtils.getDeclaredMethodsWithName(superClass, DISPATCH);
       
       if (superDispatches.length > 2)
       {
          if (AspectManager.verbose) System.out.println("[warn] - Too many dispatch() methods found in " + superClass.getName());
       }
       
       for (int i = 0 ; i < superDispatches.length ; i++)
       {
          CtMethod wrapperMethod = ReflectToJavassist.methodToJavassist(cinfo.getWrappingMethod());
          CtClass[] params = wrapperMethod.getParameterTypes(); 
          
          StringBuffer parameters = new StringBuffer("(");
          if (superDispatches[i].getParameterTypes().length == 0)
          {
             //This is the no params version called by invokeNext() for around advices
             for (int j = 0 ; j < params.length ; j++)
             {
                if (j > 0)parameters.append(", ");
                parameters.append("arg" + j);
             }
          }
          else
          {
             //This is the no parameterized version called by invokeJoinPoint() when there are no around advices
             int offset = (hasCallingObject()) ? 1 : 0; 
             for (int j = 0 ; j < params.length ; j++)
             {
                if (j > 0)parameters.append(", ");
                parameters.append("$" + (j + offset + 1));
             }
          }
          parameters.append(")");
 
          String body = 
             "{ return " + cinfo.getConstructor().getDeclaringClass().getName() + "." + cinfo.getWrappingMethod().getName() +  parameters + ";}";
    
          try
          {
             CtMethod dispatch = CtNewMethod.make(
                   superDispatches[i].getReturnType(), 
                   superDispatches[i].getName(), 
                   superDispatches[i].getParameterTypes(), 
                   superDispatches[i].getExceptionTypes(), 
                   body, 
                   clazz);
             dispatch.setModifiers(superDispatches[i].getModifiers());
             clazz.addMethod(dispatch);
          }
          catch (CannotCompileException e)
          {
             throw new RuntimeException("Could not compile code " + body + " for method " + getMethodString(clazz, superDispatches[i].getName(), superDispatches[i].getParameterTypes()), e);
          }
          
       }
    }
    
    protected static void addUntransformableInterface(Instrumentor instrumentor, CtClass clazz) throws NotFoundException
    {
       addUntransformableInterface(instrumentor.getClassPool(), clazz);
    }
 
    protected static void addUntransformableInterface(ClassPool pool, CtClass clazz) throws NotFoundException
    {
       CtClass untransformable = pool.get(Untransformable.class.getName());
       clazz.addInterface(untransformable);
    }
 
    protected static String getMethodString(CtClass joinpoint, String method, CtClass[] params)
    {
       StringBuffer sb = new StringBuffer();
       sb.append(joinpoint);
       sb.append(".");
       sb.append("name");
       sb.append("(");
       for (int i = 0 ; i < params.length ; i++)
       {
          if (i > 0) sb.append(", ");
          sb.append(params[i].getName());
       }
       sb.append(")");
       
       return sb.toString();
    }
    
    protected class AdviceSetup
    {
       int index;
       Class aspectClass;
       CtClass aspectCtClass;
       String adviceName;
       Scope scope;
       String registeredName;
       String cflowString;
       ASTCFlowExpression cflowExpr;
       int cflowIndex;
       boolean isBefore;
       boolean isAfter;
       boolean isThrowing;
       AdviceMethodProperties adviceMethodProperties;
       
       AdviceSetup(int index, GeneratedAdvisorInterceptor ifw, JoinPointInfo info) throws ClassNotFoundException, NotFoundException
       {
          this.index = index;
          scope = ifw.getScope();
          adviceName = ifw.getAdviceName();
          registeredName = ifw.getRegisteredName();
          cflowString = ifw.getCFlowString();
          cflowExpr = ifw.getCflowExpression();
          if (ifw.isAspectFactory())
          {
             Object aspectInstance = ((GeneratedAdvisorInterceptor)info.getInterceptors()[index]).getAspect(info.getAdvisor(), info.getJoinpoint(), true);
             aspectClass = aspectInstance.getClass();
          }
          else
          {
             aspectClass = Thread.currentThread().getContextClassLoader().loadClass(ifw.getAspectClassName());
          }
          aspectCtClass = ReflectToJavassist.classToJavassist(aspectClass);
 
          isBefore = ifw.isBefore();
          isAfter = ifw.isAfter();
          isThrowing = ifw.isThrowing();
       }
       
       String getAdviceName()
       {
          return adviceName;
       }
       
       
       Class getAspectClass()
       {
          return aspectClass;
       }
       
       CtClass getAspectCtClass()
       {
          return aspectCtClass;
       }
       
       Scope getScope()
       {
          return scope;
       }
       
       int getIndex()
       {
          return index;
       }
       
       String getRegisteredName()
       {
          return registeredName;
       }
       
       String getAspectFieldName()
       {
          StringBuffer name = new StringBuffer();
          if (isAround())
          {
             name.append("around");
          }
          else if (isBefore())
          {
             name.append("before");
          }
          else if (isAfter())
          {
             name.append("after");
          }
          else if (isThrowing())
          {
             name.append("throwing");
          }
          else
          {
             if (AspectManager.verbose) System.out.println("[warn] Unsupported type of advice");
          }
          name.append(index + 1);
          return name.toString();
       }
       
       String getAspectInitialiserName()
       {
          StringBuffer name = new StringBuffer();
          if (isAround())
          {
             name.append("getAround");
          }
          else if (isBefore())
          {
             name.append("getBefore");
          }
          else if (isAfter())
          {
             name.append("getAfter");
          }
          else if (isThrowing())
          {
             name.append("getThrowing");
          }
          else
          {
             if (AspectManager.verbose) System.out.println("[warn] Unsupported type of advice");
          }
          name.append(index + 1);
          return name.toString();
       }
       
       boolean isPerInstance()
       {
          return scope == Scope.PER_INSTANCE;
       }
       
       boolean isPerJoinpoint()
       {
          return scope == Scope.PER_JOINPOINT;
       }
       
       boolean shouldInvokeAspect()
       {
          return !(isPerInstance() && isStaticCall());
       }
       
       boolean requiresInstanceAdvisor()
       {
          return (isPerInstance() || (isPerJoinpoint() && !isStaticCall()));
       }
       
       String getCFlowString()
       {
          return cflowString;
       }
       
       ASTCFlowExpression getCFlow()
       {
          return cflowExpr;
       }
       
       void setUseCFlowFrom(int index)
       {
          cflowIndex = index;
       }
       
       int useCFlowFrom()
       {
          return cflowIndex;
       }
       
       boolean isNewCFlow()
       {
          return (getCFlowString() != null && index == cflowIndex);
       }
 
       boolean isAfter()
       {
          return isAfter;
       }
 
       boolean isBefore()
       {
          return isBefore;
       }
 
       boolean isThrowing()
       {
          return isThrowing;
       }
       
       boolean isAround()
       {
          return !isAfter && !isBefore && !isThrowing;
       }
 
       public AdviceMethodProperties getAdviceMethodProperties()
       {
          return adviceMethodProperties;
       }
 
       public void setAdviceMethodProperties(AdviceMethodProperties adviceMethodProperties)
       {
          this.adviceMethodProperties = adviceMethodProperties;
       }
    }
    
    private class GeneratedClassInfo
    {
       CtClass generated;
       AdviceSetup[] aroundSetups;
       
       GeneratedClassInfo(CtClass generated, AdviceSetup[] aroundSetups)
       {
          this.generated = generated;
          this.aroundSetups = aroundSetups;
       }
       
       CtClass getGenerated()
       {
          return generated;
       }
       
       AdviceSetup[] getAroundSetups()
       {
          return (aroundSetups == null) ? new AdviceSetup[0] : aroundSetups;
       }
    }
    
    private class AdviceSetupsByType
    {
       AdviceSetup[] allSetups; 
       AdviceSetup[] beforeSetups;
       AdviceSetup[] afterSetups;
       AdviceSetup[] throwingSetups;
       AdviceSetup[] aroundSetups;
 
       AdviceSetupsByType(JoinPointInfo info, AdviceSetup[] setups)
       {
          allSetups = setups;
          ArrayList<AdviceSetup> beforeAspects = null;
          ArrayList<AdviceSetup> afterAspects = null;
          ArrayList<AdviceSetup> throwingAspects = null;
          ArrayList<AdviceSetup> aroundAspects = null;
 
          for (int i = 0 ; i < setups.length ; i++)
          {
             if (setups[i].isBefore())
             {
                if (beforeAspects == null) beforeAspects = new ArrayList<AdviceSetup>();
                
                AdviceMethodProperties properties = AdviceMethodFactory.BEFORE.findAdviceMethod(getAdviceMethodProperties(info, setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   beforeAspects.add(setups[i]);
                   continue;
                }
             }
             else if (setups[i].isAfter())
             {
                if (afterAspects == null) afterAspects = new ArrayList<AdviceSetup>();
                AdviceMethodProperties properties = AdviceMethodFactory.AFTER.findAdviceMethod(getAdviceMethodProperties(info, setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   afterAspects.add(setups[i]);
                   continue;
                }
             }
             else if (setups[i].isThrowing())
             {
                if (throwingAspects == null) throwingAspects = new ArrayList<AdviceSetup>();
                AdviceMethodProperties properties = AdviceMethodFactory.THROWING.findAdviceMethod(getAdviceMethodProperties(info, setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   throwingAspects.add(setups[i]);
                   continue;
                }
             }
             else
             {
                if (aroundAspects == null) aroundAspects = new ArrayList<AdviceSetup>();
                AdviceMethodProperties properties = AdviceMethodFactory.AROUND.findAdviceMethod(getAdviceMethodProperties(info, setups[i]));
                if (properties != null)
                {
                   setups[i].setAdviceMethodProperties(properties);
                   aroundAspects.add(setups[i]);
                   continue;
                }
             }
             
             if (AspectManager.verbose)
             {
                System.out.print("[warn] No matching advice called '" + setups[i].getAdviceName() + 
                      "' could be found in " + setups[i].getAspectClass().getName() +
                      " for joinpoint " + info + ":");
                System.out.println(AdviceMethodFactory.getAdviceMatchingMessage());
             }
          }
          beforeSetups = (beforeAspects == null) ? null : (AdviceSetup[])beforeAspects.toArray(new AdviceSetup[beforeAspects.size()]);
          afterSetups = (afterAspects == null) ? null : (AdviceSetup[])afterAspects.toArray(new AdviceSetup[afterAspects.size()]);
          throwingSetups = (throwingAspects == null) ? null : (AdviceSetup[])throwingAspects.toArray(new AdviceSetup[throwingAspects.size()]);
          aroundSetups = (aroundAspects == null) ? null : (AdviceSetup[])aroundAspects.toArray(new AdviceSetup[aroundAspects.size()]);
       }
 
       public AdviceSetup[] getAllSetups()
       {
          return allSetups;
       }
       
       public AdviceSetup[] getAfterSetups()
       {
          return afterSetups;
       }
 
       public AdviceSetup[] getAroundSetups()
       {
          return aroundSetups;
       }
 
       public AdviceSetup[] getBeforeSetups()
       {
          return beforeSetups;
       }
 
       public AdviceSetup[] getThrowingSetups()
       {
          return throwingSetups;
       }
    }
 
    private interface GenerateJoinPointClassAction
    {
       void generateJoinPointClass(ClassLoader classloader, JoinPointGenerator joinPointGenerator, JoinPointInfo info);
       
       GenerateJoinPointClassAction PRIVILEGED = new GenerateJoinPointClassAction()
       {
          public void generateJoinPointClass(final ClassLoader classloader, final JoinPointGenerator joinPointGenerator, final JoinPointInfo info) 
          {
             try
             {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
                {
                   public Object run() throws Exception
                   {
                      joinPointGenerator.doGenerateJoinPointClass(classloader, info);
                      return null;
                   }
                });
             }
             catch (PrivilegedActionException e)
             {
                Exception actual = e.getException();
                if (actual instanceof RuntimeException)
                {
                   throw (RuntimeException)actual;
                }
                throw new RuntimeException(actual);
             }
          }
       };
 
       GenerateJoinPointClassAction NON_PRIVILEGED = new GenerateJoinPointClassAction()
       {
          public void generateJoinPointClass(ClassLoader classloader, JoinPointGenerator joinPointGenerator, JoinPointInfo info)
          {
             joinPointGenerator.doGenerateJoinPointClass(classloader, info);
          }
       };
    }
 
 
    private static abstract class AdviceCallStrategy
    {
       public boolean addInvokeCode(JoinPointGenerator generator,
             AdviceSetup[] setups, StringBuffer code, JoinPointInfo info) throws NotFoundException
       {
          StringBuffer call = new StringBuffer();
          if (setups == null || setups.length == 0)
          {
             return false;
          }
          boolean argsFound = false;
          String key = generateKey(generator);
          for (int i = 0 ; i < setups.length ; i++)
          {
             call.setLength(0);
             argsFound = appendAdviceCall(setups[i], key, code, call, generator, info)
                || argsFound;
             code.append(call);
             call.setLength(0);
          }
          return argsFound;
       }
       
       protected abstract String generateKey(JoinPointGenerator generator);
       protected abstract boolean appendAdviceCall(AdviceSetup setup, String key,
            StringBuffer beforeCall, StringBuffer call, JoinPointGenerator generator, JoinPointInfo info) throws NotFoundException;
       
       private final boolean appendAdviceCall(AdviceSetup setup,
             StringBuffer beforeCall, StringBuffer call,
             boolean isAround, JoinPointGenerator generator) 
       {
          AdviceMethodProperties properties = setup.getAdviceMethodProperties();
          if (properties == null)
          {
             return false;
          }
          call.append(setup.getAspectFieldName());
          call.append(".");
          call.append(setup.getAdviceName());
          call.append("(");
          
          
          final int[] args = properties.getArgs();
          boolean argsFound = false;
          if (args.length > 0)
          {
             final Class[] adviceParams = properties.getAdviceMethod().getParameterTypes();
             call.append("(");
             call.append(ClassExpression.simpleType(adviceParams[0]));
             call.append(")");
             argsFound = appendParameter(beforeCall, call, args[0], adviceParams[0], properties,
                   generator);
             for (int i = 1 ; i < args.length ; i++)
             {
                call.append(", (");
                call.append(ClassExpression.simpleType(adviceParams[i]));
                call.append(")");
                argsFound = appendParameter(beforeCall, call, args[i], adviceParams[i],
                      properties, generator) || argsFound;
             }
          }
          
          call.append(");");
          return argsFound;
       }
       
       protected boolean appendParameter(StringBuffer beforeCall, StringBuffer call,
             final int arg, final Class adviceParam, AdviceMethodProperties properties,
             JoinPointGenerator generator)
       {
          switch(arg)
          {
          case AdviceMethodProperties.INVOCATION_ARG:
             call.append("this");
             break;
          case AdviceMethodProperties.JOINPOINT_ARG:
             call.append(INFO_FIELD);
             break;
          case AdviceMethodProperties.RETURN_ARG:
             call.append(RETURN_VALUE);
             break;
          case AdviceMethodProperties.THROWABLE_ARG:
             call.append(THROWABLE);
             break;
          case AdviceMethodProperties.TARGET_ARG:
             if (!generator.parameters.hasTarget())
             {
                call.append("null");
             }
             else
             {
                call.append('$');
                call.append(generator.parameters.getTargetIndex());
             }
             break;
          case AdviceMethodProperties.CALLER_ARG:
             if (!generator.parameters.hasCaller())
             {
                call.append("null");
             }
             else
             {
                call.append('$');
                call.append(generator.parameters.getCallerIndex());
             }
             break;
          case AdviceMethodProperties.ARGS_ARG:
             call.append(ARGUMENTS);
             // all typed args may become inconsistent with arguments array after
             // advice call
             generator.inconsistentTypeArgs.get().addAll(generator.joinPointArguments);
             // return true when args has been found; false otherwise
             return true;
          default:
             // make typed argument consistent, if that is the case
             Set<Integer> inconsistentTypeArgs = generator.inconsistentTypeArgs.get();
             int argIndex = arg + generator.parameters.getFirstArgIndex();
             if (inconsistentTypeArgs.contains(arg))
             {
                beforeCall.append("$").append(argIndex).append(" = ");
                beforeCall.append(ReflectToJavassist.castInvocationValueToTypeString(properties.getJoinpointParameters()[arg], ARGUMENTS + '[' + arg + ']'));
                beforeCall.append(';');
                inconsistentTypeArgs.remove(arg);
             }
             //The parameter array is 1-based, and the invokeJoinPoint method may also take the target and caller objects
             call.append("$");
             call.append(arg + generator.parameters.getFirstArgIndex());
          }
          return false;
       }
       
 //      private final boolean appendParameters(StringBuffer code, final int arg,
 //            final Class adviceParam, boolean isAround,
 //            AdviceMethodProperties properties, JoinPointGenerator generator)
 //      {
 //         code.append("(");
 //         // In case of overloaded methods javassist sometimes seems to pick up the wrong method - use explicit casts to get hold of the parameters
 //         code.append(ClassExpression.simpleType(adviceParam));
 //         code.append(")");
 //         switch(arg)
 //         {
 //         case AdviceMethodProperties.INVOCATION_ARG:
 //            code.append("this");
 //            break;
 //         case AdviceMethodProperties.JOINPOINT_ARG:
 //            code.append(INFO_FIELD);
 //            break;
 //         case AdviceMethodProperties.RETURN_ARG:
 //            code.append(RETURN_VALUE);
 //            break;
 //         case AdviceMethodProperties.THROWABLE_ARG:
 //            code.append(THROWABLE);
 //            break;
 //         case AdviceMethodProperties.TARGET_ARG:
 //            if (!generator.parameters.hasTarget())
 //            {
 //               code.append("null");
 //            }
 //            else if (isAround)
 //            {
 //               code.append(TARGET_FIELD);
 //            }
 //            else
 //            {
 //               code.append('$');
 //               code.append(generator.parameters.getTargetIndex());
 //            }
 //            break;
 //         case AdviceMethodProperties.CALLER_ARG:
 //            if (!generator.parameters.hasCaller())
 //            {
 //               code.append("null");
 //            }
 //            else if (isAround)
 //            {
 //               code.append(CALLER_FIELD);
 //            }
 //            else
 //            {
 //               code.append('$');
 //               code.append(generator.parameters.getCallerIndex());
 //            }
 //            break;
 //         case AdviceMethodProperties.ARGS_ARG:
 //            if (isAround)
 //            {
 //               code.append(GET_ARGUMENTS);
 //            }
 //            else
 //            {
 //               code.append(ARGUMENTS);
 //            }
 //            // return true when args has been found; false otherwise
 //            return true;
 //         default:
 //            if (isAround)
 //            {
 //               code.append("this.arg");
 //               code.append(arg);
 //            }
 //            else 
 //            {
 //               //The parameter array is 1-based, and the invokeJoinPoint method may also take the target and caller objects
 //               code.append("$");
 //               code.append(arg + generator.parameters.getFirstArgIndex());
 //            }
 //         }
 //         return false;
 //      }
    }
    
    private static class AroundAdviceCallStrategy extends AdviceCallStrategy
    {
       private static ThreadLocal<AroundAdviceCallStrategy> INSTANCE =
          new ThreadLocal<AroundAdviceCallStrategy>()
          {
             protected synchronized AroundAdviceCallStrategy initialValue() {
                return new AroundAdviceCallStrategy();
             }
          };
    
       public static final AroundAdviceCallStrategy getInstance()
       {
          return INSTANCE.get();
       }
       
       private AroundAdviceCallStrategy() {}
       
       private int addedAdvice = 0;
       private boolean consistencyEnforced = false;
       
       public String generateKey(JoinPointGenerator generator)
       {
          addedAdvice = 0;
          if (generator.isVoid())
          {
             return "";
          }
          return "return ($w)";
       }
       
       public boolean appendAdviceCall(AdviceSetup setup, String key,
             StringBuffer beforeCall, StringBuffer call, JoinPointGenerator generator, JoinPointInfo info)
       {
          if (!setup.shouldInvokeAspect())
          {
             //We are invoking a static method/ctor, do not include advice in chain
             return false;
          }
       
          boolean result = false;
          AdviceMethodProperties properties = AdviceMethodFactory.AROUND.
             findAdviceMethod(generator.getAdviceMethodProperties(info, setup));
          if (properties == null || properties.getAdviceMethod() == null)
          {
             // throw new RuntimeException("DEBUG ONLY Properties was null " + 
             // aroundSetups[i].getAspectClass().getName() + "." + aroundSetups[i].getAdviceName());
             return false;
          }
          
          beforeCall.append("      case ");
          beforeCall.append(++addedAdvice);
          beforeCall.append(":");
          
          if (setup.getCFlowString() != null)
          {
             beforeCall.append("         if (matchesCflow" + setup.useCFlowFrom() + ")");
             beforeCall.append("         {");
             result = appendAroundCallString(beforeCall, call, key, setup, properties, generator);
             call.append("         }");
             call.append("         else");
             call.append("         {");
             call.append("            ");
             call.append(key);
             call.append(" invokeNext();");
             call.append("         }");
          }
          else
          {
             result = appendAroundCallString(beforeCall, call, key, setup, properties, generator);
          }
          
          call.append("      break;");
          return result;
       }
       
       
       public boolean appendAroundCallString(StringBuffer beforeCall,
             StringBuffer call, String returnStr, AdviceSetup setup,
             AdviceMethodProperties properties,
             JoinPointGenerator generator)
       {
          // method that avoids more than one repeated call to ASSURE_ARGS_CONSISTENCY
          this.consistencyEnforced = false;
          int[] args = properties.getArgs();
          
          final boolean firstParamIsInvocation =
             (args.length >= 1 && args[0] == AdviceMethodProperties.INVOCATION_ARG);
 
          if (!firstParamIsInvocation)
          {
             call.append("try{");
             call.append("   org.jboss.aop.joinpoint.CurrentInvocation.push(this); ");
          }
          call.append("   ");
          call.append(returnStr);
          call.append(" ");
          boolean result = super.appendAdviceCall(setup, beforeCall, call, true,
                generator);
          
          if (!firstParamIsInvocation)
          {
             call.append("}finally{");
             call.append("   org.jboss.aop.joinpoint.CurrentInvocation.pop(); ");
             call.append("}");
          }
          return result;
       }
       
       protected boolean appendParameter(StringBuffer beforeCall, StringBuffer call,
             final int arg, final Class adviceParam,
             AdviceMethodProperties properties, JoinPointGenerator generator)
       {
          switch(arg)
          {
          case AdviceMethodProperties.TARGET_ARG:
             if (generator.parameters.hasTarget())
             {
                call.append(TARGET_FIELD);
                return false;
             }
             break;
          case AdviceMethodProperties.CALLER_ARG:
             if (generator.parameters.hasCaller())
             {
                call.append(CALLER_FIELD);
                return false;
             }
             break;
          case AdviceMethodProperties.ARGS_ARG:
             call.append(GET_ARGUMENTS);
             // return true when args has been found; false otherwise
             return true;
          }
          if (arg >= 0)
          {
             if (!consistencyEnforced)
             {
                beforeCall.append(OptimizedBehaviourInvocations.ENFORCE_ARGS_CONSISTENCY);
                beforeCall.append("();");
                consistencyEnforced = true;
             }
             call.append("this.arg");
             call.append(arg);
             return false;
          }
          return super.appendParameter(beforeCall, call, arg, adviceParam, properties, generator);
       } 
    }
    
    private static class DefaultAdviceCallStrategy extends AdviceCallStrategy
    {
       private static DefaultAdviceCallStrategy INSTANCE = new DefaultAdviceCallStrategy();
       
       public static final DefaultAdviceCallStrategy getInstance()
       {
          return INSTANCE;
       }
       
       private DefaultAdviceCallStrategy() {}
 
       public String generateKey(JoinPointGenerator generator)
       {
          return null;
       }
       
       public boolean appendAdviceCall(AdviceSetup setup, String key,
             StringBuffer beforeCall, StringBuffer call, JoinPointGenerator generator, JoinPointInfo info)
       {
          return super.appendAdviceCall(setup, beforeCall, call, false, generator);
       }
    }
    
    private static class AfterAdviceCallStrategy extends AdviceCallStrategy
    {
       private static AfterAdviceCallStrategy INSTANCE = new AfterAdviceCallStrategy();
       
       public static final AfterAdviceCallStrategy getInstance()
       {
          return INSTANCE;
       }
       
       private AfterAdviceCallStrategy() {}
 
       public String generateKey(JoinPointGenerator generator)
       {
          if (generator.isVoid())
          {
             return "";
          }
          return "          " + RETURN_VALUE + " = (" +
          generator.getReturnType().getName() + ")";
       }
 
       public boolean appendAdviceCall(AdviceSetup setup, String key,
             StringBuffer beforeCall, StringBuffer call, JoinPointGenerator generator, JoinPointInfo info) throws NotFoundException
       {
          AdviceMethodProperties properties = setup.getAdviceMethodProperties();
          if (properties != null && !properties.isAdviceVoid())
          {
             call.append(key);
          }
          return super.appendAdviceCall(setup, beforeCall, call, false, generator);
       }
    }
 
    // TODO replace by enum
    protected static class JoinPointParameters {
       public static final JoinPointParameters ONLY_ARGS = new JoinPointParameters(false, -1, false, -1, 0, null);
       public static final JoinPointParameters TARGET_ARGS = new JoinPointParameters(true, 1, false, -1, 1, "$1");
       public static final JoinPointParameters CALLER_ARGS = new JoinPointParameters(false, -1, true, 1, 1, "$1");
       public static final JoinPointParameters TARGET_CALLER_ARGS = new JoinPointParameters(true, 1, true, 2, 2, "$1, $2");
       
       private boolean target;
       private boolean caller;
       
       private int targetIndex;
       private int callerIndex;
       private int firstArgIndex;
       private String targetCallerList;
       
       private JoinPointParameters(boolean target, int targetIndex, boolean caller, int callerIndex, int firstArgIndex, String targetCallerList)
       {
          this.target = target;
          this.targetIndex = targetIndex;
          this.caller = caller;
          this.callerIndex = callerIndex;
          this.firstArgIndex = firstArgIndex + 1;
          this.targetCallerList = targetCallerList;
       }
       
       public final boolean hasTarget()
       {
          return target;
       }
       
       public final int getTargetIndex()
       {
          return targetIndex;
       }
       
       public final boolean hasCaller()
       {
          return caller;
       }
       
       public final int getCallerIndex()
       {
          return callerIndex;
       }
       
       public final int getFirstArgIndex()
       {
          return firstArgIndex;
       }
       
       public final String declareArgsArray(int totalParameters)
       {
          StringBuffer buffer = new StringBuffer("Object[] ");
          buffer.append(ARGUMENTS);
          if (++totalParameters == firstArgIndex)
          {
             buffer.append(" = new Object[0];");
          }
          else
          {
             buffer.append(" = new Object[]{($w)$");
             buffer.append(firstArgIndex);
             for (int i = firstArgIndex + 1; i < totalParameters; i++)
             {
                buffer.append(", ($w)$");
                buffer.append(i);
             }
             buffer.append("};");
          }
          return buffer.toString();
       }
       
       public final void appendParameterList(StringBuffer code, CtClass[] parameterTypes)
       {
 
          int j = firstArgIndex - 1;
          int totalParameters = parameterTypes.length - j;
          if (targetCallerList != null)
          {
             code.append(targetCallerList);
          }
          if (totalParameters == 0)
          {
             return;
          }
          if (targetCallerList != null)
          {
             code.append(", ");
          }
 
          castArgument(code, parameterTypes[j++], 0);
 
          for (int i = 1; i < totalParameters; i++, j++)
          {
             code.append(", ");
             castArgument(code, parameterTypes[j], i);
          }
       }
       
       public final void appendParameterListWithoutArgs(StringBuffer code)
       {
          if (targetCallerList != null)
          {
             code.append(',');
             code.append(targetCallerList);
          }
       }
       
       
       private static final String[][] primitiveExtractions;
 
       static{
          primitiveExtractions = new String[][]{
                {"((Boolean) " + ARGUMENTS + " [", "]).booleanValue()"},
                {"((Integer) " + ARGUMENTS + "[", "]).intValue()"},
                {"((Double) " + ARGUMENTS + "[", "]).doubleValue()"},
                {"((Float) " + ARGUMENTS + "[", "]).floatValue()"},
                {"((Character) " + ARGUMENTS + "[", "]).charValue()"},
                {"((Byte) " + ARGUMENTS + " [", "]).byteValue()"},
                {"((Long) " + ARGUMENTS + "[", "]).longValue()"},
                {"((Short) " + ARGUMENTS + "[", "]).shortValue()"}
          };
       }
       
       public final void castArgument(StringBuffer code, CtClass expectedType, int i)
       {
 
          if (expectedType.isPrimitive())
          {
             String[] extraction = null;
             if (expectedType == CtClass.booleanType)
             {
                extraction = primitiveExtractions[0];
             }
             else if (expectedType == CtClass.intType)
             {
                extraction = primitiveExtractions[1];
             }
             else if (expectedType == CtClass.doubleType)
             {
                extraction = primitiveExtractions[2];
             }
             else if (expectedType == CtClass.floatType)
             {
                extraction = primitiveExtractions[3];
             }
             else if (expectedType == CtClass.charType)
             {
                extraction = primitiveExtractions[4];
             }
             else if (expectedType == CtClass.byteType)
             {
                extraction = primitiveExtractions[5];
             }
             else if (expectedType == CtClass.longType)
             {
                extraction = primitiveExtractions[6];
             }
             else if (expectedType == CtClass.shortType)
             {
                extraction = primitiveExtractions[7];
             }
             code.append(extraction[0]);
             code.append(i);
             code.append(extraction[1]);
          }
          else
          {
             code.append("(");
             code.append(expectedType.getName());
             code.append(") ");
             code.append(ARGUMENTS);
             code.append("[");
             code.append(i);
             code.append("]");
          }
       }
    }
    
 }
