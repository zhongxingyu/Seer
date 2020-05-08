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
 
 import javassist.CannotCompileException;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.CtNewConstructor;
 import javassist.CtNewMethod;
 import javassist.Modifier;
 import javassist.NotFoundException;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.ClassAdvisor;
 import org.jboss.aop.ConByConInfo;
 import org.jboss.aop.ConByMethodInfo;
 import org.jboss.aop.FieldInfo;
 import org.jboss.aop.GeneratedClassAdvisor;
 import org.jboss.aop.JoinPointInfo;
 import org.jboss.aop.MethodByConInfo;
 import org.jboss.aop.MethodByMethodInfo;
 import org.jboss.aop.MethodInfo;
 import org.jboss.aop.classpool.AOPClassPool;
 
 /**
  * Comment
  *
  * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
  * @version $Revision$
  */
 public class GeneratedAdvisorInstrumentor extends Instrumentor
 {
    //field names in advised class
    private static final String CURRENT_ADVISOR = "currentAdvisor$aop";
    private  static final String INSTANCE_ADVISOR = "instanceAdvisor$aop";
    private static final String GET_CURRENT_ADVISOR_NAME = "getCurrentAdvisor$aop";
    public static final String GET_CURRENT_ADVISOR = GET_CURRENT_ADVISOR_NAME + "()";
 
    //field names in advisor
    private static final String DOMAIN = "domain";
    private static final String VERSION = "version";
    private static final String CHECK_VERSION = "checkVersion";
    private static final String ADVICES_UPDATED = "advicesUpdated";
    private static final String INSTANCE_ADVISOR_MIXIN = "instanceAdvisorMixin";
    private static final String CLASS_ADVISOR = "classAdvisor";
 
    //method names in advisor or GeneratedClassAdvisor
    private static final String CREATE_INSTANCE_ADVISOR = "createInstanceAdvisor";
    private static final String INITIALISE_CALLERS = "initialiseCallers";
    private static final String INITIALISE_FIELD_WRITES = "initialiseFieldWrites";
    private static final String INITIALISE_FIELD_READS = "initialiseFieldReads";
    private static final String INITIALISE_CONSTRUCTIONS = "initialiseConstructions";
    private static final String INITIALISE_CONSTRUCTORS = "initialiseConstructors";
    private static final String INITIALISE_METHODS = "initialiseMethods";
    private static final String INITIALISE_INFOS_FOR_INSTANCE = "initialiseInfosForInstance";
    public static final String GET_CLASS_ADVISOR = "_getClassAdvisor";
    private static final String DO_REBUILD_FOR_INSTANCE = "doRebuildForInstance";
 
    private static final String DECLARING_CLASS = "this.getClass().getDeclaringClass()";
 
 
    private static final CtClass[] EMPTY_EXCEPTIONS = new CtClass[0];
    private static final CtClass[] EMPTY_SIG = new CtClass[0];
 
    CtClass clazz;
    CtClass genadvisor;
    CtClass genInstanceAdvisor;
 
 
    public GeneratedAdvisorInstrumentor(AOPClassPool pool, AspectManager manager, JoinpointClassifier joinpointClassifier, DynamicTransformationObserver observer)
    {
       super(pool, manager, joinpointClassifier, observer);
    }
 
    public GeneratedAdvisorInstrumentor(AspectManager manager, JoinpointClassifier joinpointClassifier)
    {
       super(manager, joinpointClassifier);
    }
 
    protected CtClass getGenadvisor()
    {
       return genadvisor;
    }
 
    protected CtClass getGenInstanceadvisor()
    {
       return genInstanceAdvisor;
    }
 
    @Override
    public boolean transform(CtClass clazz, ClassAdvisor advisor)
    {
       try
       {
          this.clazz = clazz;
          super.transform(clazz, advisor);
 
          if (genadvisor != null)
          {
             addInstanceAdvisorWrappers(clazz);
             TransformerCommon.compileOrLoadClass(clazz, genadvisor);
             TransformerCommon.compileOrLoadClass(clazz, genInstanceAdvisor);
          }
          return true;
       }
       catch (Throwable e)
       {
          if (AspectManager.suppressTransformationErrors)
          {
             System.err.println("[warn] AOP Instrumentor failed to transform " + clazz.getName());
             e.printStackTrace();
             return false;
          }
          else
          {
             if (e instanceof TransformationException)
             {
                throw ((TransformationException) e);
             }
             else
             {
                e.printStackTrace();
                throw new RuntimeException("failed to transform: " + clazz.getName(), e);
             }
          }
 
       }
    }
 
    @Override
    protected void intitialiseTransformers()
    {
       callerTransformer = new GeneratedAdvisorCallerTransformer(this, manager);
       fieldAccessTransformer = new GeneratedAdvisorFieldAccessTransformer(this);
       constructorExecutionTransformer = new GeneratedAdvisorConstructorExecutionTransformer(this);
       constructionTransformer = new GeneratedAdvisorConstructionTransformer(this);
       methodExecutionTransformer = new GeneratedAdvisorMethodExecutionTransformer(this);
    }
 
    @Override
    protected CtMethod createMixinInvokeMethod(CtClass clazz, CtClass mixinClass, String initializer, CtMethod method, long hash)
    throws CannotCompileException, NotFoundException, Exception
    {
       return ((GeneratedAdvisorMethodExecutionTransformer)methodExecutionTransformer).addMixinWrappersAndInfo(this, clazz, mixinClass, initializer, genadvisor, method);
    }
 
    protected static String getAdvisorName(CtClass clazz)
    {
       //Strip away the package from classname
       String className = clazz.getName();
       return className.substring(className.lastIndexOf('.') + 1) + "Advisor";
    }
 
    protected static String getInstanceAdvisorName(CtClass clazz)
    {
       //Strip away the package from classname
       String className = clazz.getName();
       return className.substring(className.lastIndexOf('.') + 1) + "InstanceAdvisor";
    }
 
    protected static String getAdvisorFQN(CtClass clazz)
    {
       return clazz.getName() + "$" + getAdvisorName(clazz);
    }
 
    protected static String getInstanceAdvisorFQN(CtClass clazz)
    {
       return clazz.getName() + "$" + getInstanceAdvisorName(clazz);
    }
 
    protected CtClass createAdvisorClass(CtClass clazz) throws NotFoundException, CannotCompileException
    {
       String innerClassName = getAdvisorName(clazz);
 
       //Only static nested classes are supported
       final boolean classStatic = true;
       genadvisor = TransformerCommon.makeNestedClass(clazz, innerClassName, classStatic);
       //The advisor needs to be public in case children are in a child/scoped ucl
       //genadvisor.setModifiers(Modifier.setPublic(genadvisor.getModifiers()));
 
       //Make super class Advisor
       final CtClass superAdvisor = getSuperClassAdvisor(clazz.getSuperclass());
       if (superAdvisor == null)
       {
          genadvisor.setSuperclass(forName(GeneratedClassAdvisor.class.getName()));
       }
       else
       {
          genadvisor.setSuperclass(superAdvisor);
       }
 
       //Make Untransformable
       final CtClass untransformable = getClassPool().get("org.jboss.aop.instrument.Untransformable");
       genadvisor.addInterface(untransformable);
 
       //Add domain
       CtField domain = new CtField(forName("org.jboss.aop.Domain"), DOMAIN, genadvisor);
       domain.setModifiers(Modifier.PROTECTED);
       genadvisor.addField(domain);
       CtMethod getter = CtNewMethod.getter("getDomain", domain);
       genadvisor.addMethod(getter);
 
       CtMethod initialiseMethods = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             INITIALISE_METHODS,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             (isBaseClass(clazz)) ?
                   null : "{super." + INITIALISE_METHODS + "();}",
             genadvisor);
       genadvisor.addMethod(initialiseMethods);
 
       CtMethod initialiseConstructors = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             INITIALISE_CONSTRUCTORS,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             null,
             genadvisor);
       genadvisor.addMethod(initialiseConstructors);
 
       CtMethod initialiseConstructions = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             INITIALISE_CONSTRUCTIONS,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             (isBaseClass(clazz)) ?
                   null : "{super." + INITIALISE_CONSTRUCTIONS + "();}",
             genadvisor);
       genadvisor.addMethod(initialiseConstructions);
 
       CtMethod initialiseFieldReads = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             INITIALISE_FIELD_READS,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             (isBaseClass(clazz)) ?
                   null : "{super." + INITIALISE_FIELD_READS + "();}",
             genadvisor);
       genadvisor.addMethod(initialiseFieldReads);
 
       CtMethod initialiseFieldWrites = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             INITIALISE_FIELD_WRITES,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             (isBaseClass(clazz)) ?
                   null : "{super." + INITIALISE_FIELD_WRITES + "();}",
             genadvisor);
       genadvisor.addMethod(initialiseFieldWrites);
 
       CtMethod initialiseCallers = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             INITIALISE_CALLERS,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             null, //"{" + GeneratedClassAdvisor.CLEAR_CALLERS + "();}",
             genadvisor);
       genadvisor.addMethod(initialiseCallers);
 
       createAdvisorCtors(clazz);
 
       return genadvisor;
    }
 
    protected CtClass createInstanceAdvisorClass(CtClass clazz) throws NotFoundException, CannotCompileException
    {
       String innerClassName = getInstanceAdvisorName(clazz);
 
       //Only static nested classes are supported
       final boolean classStatic = true;
       genInstanceAdvisor = TransformerCommon.makeNestedClass(clazz, innerClassName, classStatic);
       //The advisor needs to be public in case children are in a child/scoped ucl
       genInstanceAdvisor.setModifiers(Modifier.setPublic(genInstanceAdvisor.getModifiers()));
 
       //Make super class Advisor
       genInstanceAdvisor.setSuperclass(getGenadvisor());
 
       //Make Untransformable
       final CtClass untransformable = getClassPool().get("org.jboss.aop.instrument.Untransformable");
       genInstanceAdvisor.addInterface(untransformable);
 
       //Add reference to parent advisor
       CtField classAdvisor = new CtField(genadvisor, CLASS_ADVISOR, genInstanceAdvisor);
       genInstanceAdvisor.addField(classAdvisor);
 
       CtMethod advicesUpdated = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             ADVICES_UPDATED,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             null,
             genInstanceAdvisor);
       genInstanceAdvisor.addMethod(advicesUpdated);
 
       implementInstanceAdvisorMethods();
 
       String drfiBody =
          "{" +
          "   internalRebuildInterceptors(); " +
          "   if (" + INSTANCE_ADVISOR_MIXIN + ".hasInterceptors())" +
          "   {" +
          "       " + ADVICES_UPDATED + "();" +
          "   }" +
          "}";
       CtMethod doRebuildForInstance = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             DO_REBUILD_FOR_INSTANCE,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             drfiBody,
             genInstanceAdvisor);
       genInstanceAdvisor.addMethod(doRebuildForInstance);
 
       String body =
          "{" +
          "    super($2);" +
          "   " + INSTANCE_ADVISOR_MIXIN + " = new org.jboss.aop.GeneratedInstanceAdvisorMixin($1, $2);" +
          "   this.classAdvisor = $2;" +
          "}";
       CtConstructor ctor = CtNewConstructor.make(new CtClass[]{forName("java.lang.Object"), genadvisor}, new CtClass[0], body, genInstanceAdvisor);
       genInstanceAdvisor.addConstructor(ctor);
 
       //Create methods for quicker initialising of the infos when the instance advisor is created
       CtMethod initialiseInfosForInstance = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             INITIALISE_INFOS_FOR_INSTANCE,
             EMPTY_SIG,
             EMPTY_EXCEPTIONS,
             null,
             genInstanceAdvisor);
       genInstanceAdvisor.addMethod(initialiseInfosForInstance);
 
       return genInstanceAdvisor;
    }
 
    protected void createAdvisorCtors(CtClass clazz)throws CannotCompileException, NotFoundException
    {
       String initBody =
          "{" +
          "   String domainName = org.jboss.aop.Domain.getDomainName(" + DECLARING_CLASS + ", $2);" + 
          "   " + DOMAIN + "= new org.jboss.aop.GeneratedAdvisorDomain($1, domainName, " + DECLARING_CLASS + ", false); " +
          "   ((org.jboss.aop.Domain)" + DOMAIN + ").setInheritsBindings(true); " +
          "   super.initialise(" + DECLARING_CLASS + ", " + DOMAIN + ");" +
          "}";
 
          CtMethod initialise = CtNewMethod.make(
             Modifier.PROTECTED,
             CtClass.voidType,
             "initialise",
             new CtClass[]{forName("org.jboss.aop.AspectManager"), CtClass.booleanType},
             EMPTY_EXCEPTIONS,
             initBody,
             genadvisor);
       genadvisor.addMethod(initialise);
 
 
       //This will be called when a class instantiates its advisor
       CtConstructor ctor = CtNewConstructor.defaultConstructor(genadvisor);
       ctor.setBody(
             "{" +
             "   super(\"" + clazz.getName() + "\"); " +
             "   initialise(org.jboss.aop.AspectManager.instance(this.getClass().getClassLoader()), false);" + //Use the CL of the class, since we may be in a scoped loader
             "}");
       genadvisor.addConstructor(ctor);
 
       //This is called by instance advisors
       String instanceBody =
          "{" +
          "   super(\"" + clazz.getName() + "\", $1); " +
          "   initialise($1.getDomain(), true);" +
          "}";
       CtConstructor ctorWithParentAdvisor = CtNewConstructor.make(new CtClass[]{genadvisor}, EMPTY_EXCEPTIONS, instanceBody, genadvisor);
       genadvisor.addConstructor(ctorWithParentAdvisor);
 
 
       //This will be called by sub advisors
       CtConstructor ctorForSubAdvisors = CtNewConstructor.make(new CtClass[]{forName("java.lang.String")}, new CtClass[0], "{super($1);}", genadvisor);
       genadvisor.addConstructor(ctorForSubAdvisors);
       ctorForSubAdvisors.setModifiers(Modifier.PROTECTED);
 
       //This will be called by instance advisors for sub advisors   
       CtConstructor ctorForSubAdvisorInstanceAdvisors = CtNewConstructor.make(new CtClass[]{forName("java.lang.String"), forName("org.jboss.aop.GeneratedClassAdvisor")}, new CtClass[0], "{super($1, $2);}", genadvisor);
       genadvisor.addConstructor(ctorForSubAdvisorInstanceAdvisors);
       ctorForSubAdvisorInstanceAdvisors.setModifiers(Modifier.PROTECTED);
 }
 
    protected CtClass getSuperClassAdvisor(CtClass superclass)throws NotFoundException
    {
       if (superclass != null)
       {
          try
          {
             if (isAdvised(superclass))
             {
                return forName(superclass.getClassPool(), getAdvisorFQN(superclass));
             }
          }
          catch (NotFoundException e)
          {
          }
 
          return getSuperClassAdvisor(superclass.getSuperclass());
       }
       return null;
 
    }
 
    protected void implementInstanceAdvisorMethods() throws NotFoundException, CannotCompileException
    {
       final CtClass instanceAdvisor = getClassPool().get("org.jboss.aop.InstanceAdvisor");
       genInstanceAdvisor.addInterface(instanceAdvisor);
 
       CtField instanceAdvisorMixin =  new CtField(
             getClassPool().get("org.jboss.aop.GeneratedInstanceAdvisorMixin"),
             INSTANCE_ADVISOR_MIXIN,
             genInstanceAdvisor);
       genInstanceAdvisor.addField(instanceAdvisorMixin);
 
       CtMethod[] instanceAdvisorMethods = instanceAdvisor.getDeclaredMethods();
       for (int i = 0 ; i < instanceAdvisorMethods.length ; i++)
       {
          final String name = instanceAdvisorMethods[i].getName();
          if (name.equals("hasAspects"))
          {
             //hasAspects is declared final in Advisor, which we inherit from so we cannot override that
             continue;
          }
          else if (name.equals("getDomain"))
          {
             //We've already implemented this method and don't want to delgate this to the mixin
             continue;
          }
 
          String ret = (instanceAdvisorMethods[i].getReturnType().equals(CtClass.voidType)) ? "" : "return ";
          StringBuffer delegatingBody = new StringBuffer();
          delegatingBody.append("{");
          if (name.startsWith("insertInterceptor") || name.startsWith("removeInterceptor") || name.startsWith("appendInterceptor"))
          {
             delegatingBody.append(ADVICES_UPDATED + "();");
          }
          if (!instanceAdvisorMethods[i].getReturnType().equals(CtClass.voidType))
          {
             delegatingBody.append("return ");
          }
          delegatingBody.append(INSTANCE_ADVISOR_MIXIN + "." + instanceAdvisorMethods[i].getName() + "($$);}");
 
          CtMethod m = CtNewMethod.make(
                Modifier.PUBLIC,
                instanceAdvisorMethods[i].getReturnType(),
                instanceAdvisorMethods[i].getName(),
                instanceAdvisorMethods[i].getParameterTypes(),
                instanceAdvisorMethods[i].getExceptionTypes(),
                delegatingBody.toString(),
                genInstanceAdvisor);
          genInstanceAdvisor.addMethod(m);
       }
    }
 
    private void addCreateInstanceAdvisorToGenAdvisor(CtClass clazz) throws NotFoundException, CannotCompileException
    {
       CtMethod createInstanceAdvisor = CtNewMethod.make(
             Modifier.PUBLIC,
             forName("org.jboss.aop.Advisor"),
             CREATE_INSTANCE_ADVISOR,
             new CtClass[]{forName("java.lang.Object")},
             EMPTY_EXCEPTIONS,
             "{return new " + getInstanceAdvisorFQN(clazz) + "($1, this);}",
             genadvisor);
       genadvisor.addMethod(createInstanceAdvisor);
    }
 
    protected void doSetupBasics(CtClass clazz) throws CannotCompileException, NotFoundException
    {
       createAdvisorClass(clazz);
       createInstanceAdvisorClass(clazz);
       addCreateInstanceAdvisorToGenAdvisor(clazz);
       createAdvisorFieldsAndGetter(clazz);
    }
 
    private void createAdvisorFieldsAndGetter(CtClass clazz)throws NotFoundException, CannotCompileException
    {
       CtField classAdvisor = new CtField(
             forName("org.jboss.aop.Advisor"),
             Instrumentor.HELPER_FIELD_NAME,
             clazz);
       classAdvisor.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.TRANSIENT);
       clazz.addField(classAdvisor, CtField.Initializer.byExpr("new " + getAdvisorFQN(clazz) + "()"));
 //      clazz.addField(classAdvisor, CtField.Initializer.byExpr("this.getClass().getClassLoader().loadClass(" + getAdvisorFQN(clazz) + ").newInstance()"));
 
       CtMethod getAdvisor = CtNewMethod.getter("_getAdvisor", classAdvisor);
       getAdvisor.setModifiers(Modifier.PUBLIC);
       clazz.addMethod(getAdvisor);
 
       CtMethod getClassAdvisor = CtNewMethod.getter(GET_CLASS_ADVISOR, classAdvisor);
       getClassAdvisor.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
       clazz.addMethod(getClassAdvisor);
 
       if (isBaseClass(clazz))
       {
          CtField currentAdvisor = new CtField(
             forName("org.jboss.aop.Advisor"),
             CURRENT_ADVISOR,
             clazz);
          currentAdvisor.setModifiers(Modifier.VOLATILE | Modifier.PROTECTED | Modifier.TRANSIENT);
          clazz.addField(currentAdvisor, CtField.Initializer.byExpr("_getAdvisor()"));
 
          String body =
             "{" +
             "   if (" + CURRENT_ADVISOR + " == null)" +
             "   {" +
             "      " + CURRENT_ADVISOR + " = _getAdvisor();" +
             "   }" +
             "   return " + CURRENT_ADVISOR + ";"+
             "}";
          CtMethod getCurrentAdvisor = CtNewMethod.make(
                Modifier.PROTECTED,
                forName("org.jboss.aop.Advisor"),
                GET_CURRENT_ADVISOR_NAME,
                EMPTY_SIG,
                EMPTY_EXCEPTIONS,
                body,
                clazz);
          clazz.addMethod(getCurrentAdvisor);
 
          CtField instanceAdvisor = new CtField(
                forName("org.jboss.aop.InstanceAdvisor"),
                INSTANCE_ADVISOR,
                clazz);
          instanceAdvisor.setModifiers(Modifier.PROTECTED | Modifier.TRANSIENT);
          clazz.addField(instanceAdvisor);
       }
 
       //Add _getInstanceAdvisor() method
       String body =
          "{ " +
          "   if (" + INSTANCE_ADVISOR + " == null) " +
          "   { " +
          "      synchronized(this) " +
          "      { " +
          "         if (" + INSTANCE_ADVISOR + " == null) " +
          "         { " +
          "            org.jboss.aop.Advisor advisor = ((" + getAdvisorFQN(clazz) + ")" + Instrumentor.HELPER_FIELD_NAME + ").createInstanceAdvisor(this); " +
          "            " + CURRENT_ADVISOR + " = advisor; " +
          "            " + INSTANCE_ADVISOR + " = (org.jboss.aop.InstanceAdvisor)advisor; " +
          "         } " +
          "      } " +
          "   } " +
          "   return " + INSTANCE_ADVISOR +";" +
          "}";
       CtMethod getInstanceAdvisor = CtNewMethod.make(
             forName("org.jboss.aop.InstanceAdvisor"),
             "_getInstanceAdvisor",
             new CtClass[0],
             new CtClass[0],
             body,
             clazz);
       clazz.addMethod(getInstanceAdvisor);
    }
 
    public static String updatedAdvicesName(String infoName)
    {
       return infoName + "_updated";
    }
 
 
    /** Make sure that instance advisors have wrappers for all super advisors
     */
    private void addInstanceAdvisorWrappers(CtClass clazz)throws CannotCompileException, NotFoundException
    {
       CtClass superClass = clazz;
       CtClass superAdvisor = genadvisor;
       boolean isSuper = false;
 
       StringBuffer advicesUpdatedCode = new StringBuffer();
 
       while (true)
       {
          CtField[] fields = superAdvisor.getDeclaredFields();
 
          for (int i = 0 ; i < fields.length ; i++)
          {
             if (Modifier.isStatic(fields[i].getModifiers())) continue;
 
             GeneratedAdvisorNameExtractor names = GeneratedAdvisorNameExtractor.extractNames(superAdvisor, fields[i]);
             if (names == null) continue;
 
             //Add marker to keep track of if advice has interceptors appended
             String infoName = fields[i].getName();
             String updatedJoinpointAdvicesName = addAdvicesUpdatedForJoinpointField(infoName);
             advicesUpdatedCode.append(updatedJoinpointAdvicesName + " = true;");
             addWrapperDelegatorMethodToInstanceAdvisor(names, updatedJoinpointAdvicesName);
             
             //Add code to the initialiseInfosForInstance to copy the infos from the super class
             String infoClassName = fields[i].getType().getName();
             if (
                   infoClassName.equals(FieldInfo.class.getName()) || 
                   infoClassName.equals(MethodInfo.class.getName()) ||
                   infoClassName.equals(ConByConInfo.class.getName()) ||
                   infoClassName.equals(MethodByConInfo.class.getName()) ||
                   infoClassName.equals(ConByMethodInfo.class.getName()) ||
                   infoClassName.equals(MethodByMethodInfo.class.getName()))
             {
               String code = infoName + " = super.copyInfoFromClassAdvisor(((" + genadvisor.getName() + ")" + clazz.getName() + "." + GET_CLASS_ADVISOR + "())." + infoName + ");"
                addCodeToInitialiseMethod(genInstanceAdvisor, code, INITIALISE_INFOS_FOR_INSTANCE);
             }
          }
 
          if (isBaseClass(superClass))
          {
             break;
          }
 
          isSuper = true;
          superClass = superClass.getSuperclass();
          superAdvisor = superAdvisor.getSuperclass();
       }
 
       CtMethod advicesUpdated = genInstanceAdvisor.getDeclaredMethod(ADVICES_UPDATED);
       advicesUpdated.insertAfter(advicesUpdatedCode.toString());
    }
 
    private String addAdvicesUpdatedForJoinpointField(String infoName) throws NotFoundException, CannotCompileException
    {
       String updatedAdvicesName = updatedAdvicesName(infoName);
       try
       {
          genInstanceAdvisor.getDeclaredField(updatedAdvicesName);
       }
       catch(NotFoundException e)
       {
          //Field did not exist - add it
          CtField updatedAdvice = new CtField(CtClass.booleanType, updatedAdvicesName, genInstanceAdvisor);
          updatedAdvice.setModifiers(Modifier.PROTECTED);
          genInstanceAdvisor.addField(updatedAdvice);
       }
 
       return updatedAdvicesName;
    }
 
    private void addWrapperDelegatorMethodToInstanceAdvisor(GeneratedAdvisorNameExtractor names, String updatedAdvicesFieldName) throws  NotFoundException, CannotCompileException
    {
       try
       {
          genInstanceAdvisor.getDeclaredMethod(names.getWrapper().getName());
       }
       catch(NotFoundException e)
       {
          //Method did not exist - add it
          CtMethod instanceAdvisorMethod = CtNewMethod.delegator(names.getWrapper(), genInstanceAdvisor);
          String code =
             CHECK_VERSION + "();" +
             "if (" + updatedAdvicesFieldName + ")" +
             "{ " +
             "   " + JoinPointInfo.class.getName() + " copy = " + names.getInfoFieldName() + ".copy();" +
             "   copy.setInterceptors( " + INSTANCE_ADVISOR_MIXIN + ".getWrappers(copy.getInterceptors()) );" +
             "   " + updatedAdvicesFieldName + " = false;" +
             "   " + names.getJoinPointField().getName() + " = null;" +
 //            "   " + names.getInfoFieldName() + ".setInterceptors(copy.getInterceptors());" +  //We need a way to make this "transient" 
             "   super.rebindJoinPointWithInstanceInformation(copy);" +
             "}";
          instanceAdvisorMethod.insertBefore(code);
          genInstanceAdvisor.addMethod(instanceAdvisorMethod);
       }
    }
 
    protected void initaliseMethodInfo(String infoName, long hash, long unadvisedHash)throws NotFoundException
    {
       //Add code to initialise info to class advisor
       String code =
          infoName + " = new " + MethodExecutionTransformer.METHOD_INFO_CLASS_NAME + "(" +
                "java.lang.Class.forName(\"" + clazz.getName() + "\")," +
                hash + "L, " +
                unadvisedHash + "L, this);" +
          GeneratedClassAdvisor.ADD_METHOD_INFO + "(" + infoName + ");";
 
       addCodeToInitialiseMethod(genadvisor, code, INITIALISE_METHODS);
    }
 
    protected void initialiseFieldReadInfoField(String infoName, int index, String fieldName, long wrapperHash) throws NotFoundException
    {
       //Add code to initialise info to class advisor
       String code =
          infoName + " = new " + FieldAccessTransformer.FIELD_INFO_CLASS_NAME + "(" +
             "java.lang.Class.forName(\"" + clazz.getName() + "\")," +
             index + ", " +
             "\"" + fieldName + "\", " +
             wrapperHash + "L, this, true);" +
          GeneratedClassAdvisor.ADD_FIELD_READ_INFO + "(" + infoName + ");";
 
       addCodeToInitialiseMethod(genadvisor, code, INITIALISE_FIELD_READS);
    }
 
    protected void initialiseFieldWriteInfoField(String infoName, int index, String fieldName, long wrapperHash) throws NotFoundException
    {
       //Add code to initialise info to class advisor
       String code =
          infoName + " = new " + FieldAccessTransformer.FIELD_INFO_CLASS_NAME + "(" +
             "java.lang.Class.forName(\"" + clazz.getName() + "\")," +
             index + ", " +
             "\"" + fieldName + "\", " +
             wrapperHash + "L, this, false);" +
          GeneratedClassAdvisor.ADD_FIELD_WRITE_INFO + "(" + infoName + ");";
 
       addCodeToInitialiseMethod(genadvisor, code, INITIALISE_FIELD_WRITES);
    }
 
    protected void initialiseConstructorInfoField(String infoName, int index, long constructorHash, long wrapperHash) throws NotFoundException
    {
       //Add code to initialise info to class advisor
       String code =
          infoName + " = new " + ConstructorExecutionTransformer.CONSTRUCTOR_INFO_CLASS_NAME + "(" +
             "java.lang.Class.forName(\"" + clazz.getName() + "\")," +
             index + ", " +
             wrapperHash + "L, " +
             constructorHash + "L, this);" +
          GeneratedClassAdvisor.ADD_CONSTRUCTOR_INFO + "(" + infoName + ");";
 
       addCodeToInitialiseMethod(genadvisor, code, INITIALISE_CONSTRUCTORS);
    }
 
    protected void initialiseConstructionInfoField(String infoName, int index, long constructorHash) throws NotFoundException
    {
       //Add code to initialise info to class advisor
       String code =
          infoName + " = new " + ConstructionTransformer.CONSTRUCTION_INFO_CLASS_NAME + "(" +
             "java.lang.Class.forName(\"" + clazz.getName() + "\")," +
             index + ", " +
             constructorHash + "L, this);" +
          GeneratedClassAdvisor.ADD_CONSTRUCTION_INFO + "(" + infoName + ");";
 
       addCodeToInitialiseMethod(genadvisor, code, INITIALISE_CONSTRUCTIONS);
    }
 
    protected void initialiseCallerInfoField(String infoName, String init)throws CannotCompileException, NotFoundException
    {
       //Add code to initialise info to class advisor
       addCodeToInitialiseMethod(genadvisor, infoName + " = " + init + ";", INITIALISE_CALLERS);
    }
 
 
    private void addCodeToInitialiseMethod(CtClass clazz, String code, String methodName) throws NotFoundException
    {
       CtMethod method = clazz.getDeclaredMethod(methodName);
       try
       {
          method.insertAfter(code);
       }
       catch (CannotCompileException e)
       {
          e.printStackTrace();
          throw new RuntimeException("code was: " + code + " for method " + method.getName());
       }
    }
 
    private static class GeneratedAdvisorNameExtractor
    {
       //TODO This kind of sucks. We need a better way to link names of wrapper methods, generators, infos and joinpoints
       String infoName;
       CtMethod wrapper;
       CtField joinPointField;
 
       private GeneratedAdvisorNameExtractor(String infoName, CtMethod wrapper, CtField joinPointField)
       {
          this.infoName = infoName;
          this.wrapper = wrapper;
          this.joinPointField = joinPointField;
       }
 
       private static GeneratedAdvisorNameExtractor extractNames(CtClass genadvisor, CtField infoField) throws NotFoundException
       {
          String infoName = infoField.getName();
 
          if (infoField.getType().getName().equals(FieldInfo.class.getName()))
          {
             boolean isWrite = infoName.startsWith("aop$FieldInfo_w_");
             if (!isWrite && !infoName.startsWith("aop$FieldInfo_r_"))
             {
                throw new RuntimeException("Bad FieldInfo name: '" + infoName + "'");
             }
             String fieldName = infoName.substring("aop$FieldInfo_w_".length());
 
             String wrapperName = (isWrite) ?
                   GeneratedAdvisorFieldAccessTransformer.advisorFieldWrite(genadvisor, fieldName) :
                   GeneratedAdvisorFieldAccessTransformer.advisorFieldRead(genadvisor, fieldName);
 
             CtMethod wrapper = genadvisor.getDeclaredMethod(wrapperName);
 
             String joinPointName = (isWrite) ?
                   FieldJoinPointGenerator.WRITE_JOINPOINT_FIELD_PREFIX + fieldName :
                      FieldJoinPointGenerator.READ_JOINPOINT_FIELD_PREFIX + fieldName;
             CtField joinPointField = genadvisor.getDeclaredField(joinPointName);
 
             return new GeneratedAdvisorNameExtractor(infoName, wrapper, joinPointField);
          }
          else if (infoField.getType().getName().equals(MethodInfo.class.getName()))
          {
             if (!infoName.startsWith("aop$MethodInfo_"))
             {
                throw new RuntimeException("Bad MethodInfo name: '" + infoName + "'");
             }
             String methodNameHash = infoName.substring("aop$MethodInfo_".length());
             CtMethod wrapper = genadvisor.getDeclaredMethod(methodNameHash);
 
             String joinPointName = JoinPointGenerator.JOINPOINT_FIELD_PREFIX + methodNameHash;
             CtField joinPointField = genadvisor.getDeclaredField(joinPointName);
 
             return new GeneratedAdvisorNameExtractor(infoName, wrapper, joinPointField);
          }
          else if (infoField.getType().getName().equals(ConByMethodInfo.class.getName()))
          {
             if (!infoName.startsWith("aop$constructorCall_"))
             {
                throw new RuntimeException("Bad ConByMethodInfo name: '" + infoName + "'");
             }
 
             CtMethod wrapper = genadvisor.getDeclaredMethod(infoName);
 
             String joinPointName = ConByMethodJoinPointGenerator.JOINPOINT_FIELD_PREFIX + infoName.substring("aop$constructorCall_".length());
             CtField joinPointField = genadvisor.getDeclaredField(joinPointName);
 
             return new GeneratedAdvisorNameExtractor(infoName, wrapper, joinPointField);
          }
          else if (infoField.getType().getName().equals(MethodByMethodInfo.class.getName()))
          {
             if (!infoName.startsWith("aop$methodCall_"))
             {
                throw new RuntimeException("Bad MethodByMethodInfo name: '" + infoName + "'");
             }
 
             CtMethod wrapper = genadvisor.getDeclaredMethod(infoName);
 
             String joinPointName = MethodByMethodJoinPointGenerator.JOINPOINT_FIELD_PREFIX + infoName.substring("aop$methodCall_".length());
             CtField joinPointField = genadvisor.getDeclaredField(joinPointName);
 
             return new GeneratedAdvisorNameExtractor(infoName, wrapper, joinPointField);
          }
          else if (infoField.getType().getName().equals(ConByConInfo.class.getName()))
          {
             if (!infoName.startsWith("aop$constructorCall_con_"))
             {
                throw new RuntimeException("Bad ConByConInfo name: '" + infoName + "'");
             }
 
             CtMethod wrapper = genadvisor.getDeclaredMethod(infoName);
 
             String joinPointName = ConByConJoinPointGenerator.JOINPOINT_FIELD_PREFIX + infoName.substring("aop$constructorCall_con_".length());
             CtField joinPointField = genadvisor.getDeclaredField(joinPointName);
 
             return new GeneratedAdvisorNameExtractor(infoName, wrapper, joinPointField);
          }
          else if (infoField.getType().getName().equals(MethodByConInfo.class.getName()))
          {
             if (!infoName.startsWith("aop$methodCall_con"))
             {
                throw new RuntimeException("Bad MethodByConInfo name: '" + infoName + "'");
             }
 
             CtMethod wrapper = genadvisor.getDeclaredMethod(infoName);
 
             String joinPointName = MethodByConJoinPointGenerator.JOINPOINT_FIELD_PREFIX + infoName.substring("aop$methodCall_con_".length());
             CtField joinPointField = genadvisor.getDeclaredField(joinPointName);
 
             return new GeneratedAdvisorNameExtractor(infoName, wrapper, joinPointField);
          }
 
          return null;
       }
 
       public CtField getJoinPointField()
       {
          return joinPointField;
       }
 
       public CtMethod getWrapper()
       {
          return wrapper;
       }
 
       public String getInfoFieldName()
       {
          return infoName;
       }
    }
 }
