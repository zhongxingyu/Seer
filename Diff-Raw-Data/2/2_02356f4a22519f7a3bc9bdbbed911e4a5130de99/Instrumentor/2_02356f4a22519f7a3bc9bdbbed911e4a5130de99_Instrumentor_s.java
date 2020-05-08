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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.jboss.aop.Advised;
 import org.jboss.aop.Advisor;
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.ClassAdvisor;
 import org.jboss.aop.annotation.compiler.AnnotationInfoCreator;
 import org.jboss.aop.classpool.AOPClassPool;
 import org.jboss.aop.classpool.AOPClassPoolRepository;
 import org.jboss.aop.introduction.AnnotationIntroduction;
 import org.jboss.aop.introduction.InterfaceIntroduction;
 import org.jboss.aop.util.Advisable;
 import org.jboss.aop.util.CtConstructorComparator;
 import org.jboss.aop.util.CtFieldComparator;
 import org.jboss.aop.util.JavassistMethodHashing;
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CodeConverter;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.Modifier;
 import javassist.NotFoundException;
 import javassist.SerialVersionUID;
 import javassist.bytecode.AnnotationsAttribute;
 import javassist.bytecode.FieldInfo;
 import javassist.bytecode.MethodInfo;
 
 /**
  * Transforms byte code, making a class advisable. Implements
  * command line class instrumentor as well. Reads classes from class path and creates
  * advised versions in specified directory. Usage:
  * <pre>
  * Instrumentor [dest. directory] [class[ class...]]
  * </pre>
  *
  * You can control which instrumentor to use by passing in the jboss.aop.instrumentor
  * system property. 
  *
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:gte863h@prism.gatech.edu">Austin Chau</a>
  * @author <a href="mailto:crazybob@crazybob.org">Bob Lee</a>
  * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
  * @version $Revision$
  */
 public abstract class Instrumentor
 {
    /**
     * Package of AOP classes.
     */
    public static final String AOP_PACKAGE =
            Advised.class.getPackage().getName();
 
    /**
     * Name of helper class.
     */
    public static final String ASPECT_MANAGER_CLASS_NAME =
            AOP_PACKAGE + ".AspectManager";
 
    /**
     * Helper class's field name.
     */
    public static final String HELPER_FIELD_NAME = "aop$classAdvisor" + ClassAdvisor.NOT_TRANSFORMABLE_SUFFIX;
 
    protected AOPClassPool classPool;
    protected boolean basicsSet = false;
 
 
    protected CodeConverter converter;
    protected AspectManager manager;
    protected JoinpointClassifier joinpointClassifier;
    protected static Collection processedClasses = new ArrayList();
 
    // Transformers, more than meets the eye!
    MethodExecutionTransformer methodExecutionTransformer;
    ConstructorExecutionTransformer constructorExecutionTransformer;
    ConstructionTransformer constructionTransformer;
    FieldAccessTransformer fieldAccessTransformer;
    CallerTransformer callerTransformer;  
    DynamicTransformationObserver dynamicTransformationObserver;
    
    /**
     * Constructs new instrumentor.
     * @joinpointClassifier algorithm of joinpoint classification to be used.
     * @param observer need be notified of every joinpoint wrapping caused only
     * by pointcuts dynamicaly added.
     */
    protected Instrumentor(AOPClassPool pool, 
          AspectManager manager, 
          JoinpointClassifier joinpointClassifier, 
          DynamicTransformationObserver observer)
    {
       this.classPool = pool;
       this.converter = new CodeConverter();
       this.manager = manager;
       this.joinpointClassifier = joinpointClassifier;
       this.dynamicTransformationObserver = observer;
       intitialiseTransformers();
    }
 
    protected Instrumentor(AspectManager manager, 
          JoinpointClassifier joinpointClassifier)
    {
       this(null, 
             manager, 
             joinpointClassifier, 
             null);
    }
    
    protected abstract void intitialiseTransformers();
    
    public ClassPool getClassPool()
    {
       return classPool;
    }
 
    CodeConverter getCodeConverter()
    {
       return converter;
    }
 
    public boolean isAdvised(CtClass clazz) throws NotFoundException
    {
       CtClass[] interfaces = clazz.getInterfaces();
       CtClass advised = forName(AOP_PACKAGE + ".Advised");
       for (int i = 0; i < interfaces.length; i++)
       {
          if (interfaces[i].equals(advised)) return true;
          if (interfaces[i].getName().equals(AOP_PACKAGE + ".Advised")) return true;
       }
       return false;
    }
    
    public static boolean implementsAdvised(CtClass clazz) throws NotFoundException
    {
       CtClass[] interfaces = clazz.getInterfaces();
       for (int i = 0; i < interfaces.length; i++)
       {
          if (interfaces[i].getName().equals(AOP_PACKAGE + ".Advised")) return true;
       }
       return false;
    }
 
 
    public static boolean isTransformable(CtClass clazz) throws NotFoundException
    {
       CtClass[] interfaces = clazz.getInterfaces();
       //CtClass advised = forName(AOP_PACKAGE + ".instrument.Untransformable");
       for (int i = 0; i < interfaces.length; i++)
       {
          //if (interfaces[i].equals(advised)) return false;
          if (interfaces[i].getName().equals(AOP_PACKAGE + ".instrument.Untransformable")) return false;
       }
       return true;
    }
 
    protected boolean isBaseClass(CtClass clazz)
            throws NotFoundException
    {
       if (clazz.getSuperclass() != null)
       {
          return !isAdvised(clazz.getSuperclass());
       }
       return true;
    }
 
 
    protected static String mixinFieldName(CtClass mixinClass)
    {
       StringBuffer buf = new StringBuffer("_");
       buf.append(mixinClass.getName().replace('.', '$'));
       buf.append("$aop$mixin");
       return buf.toString();
    }
 
    private void addMixinMethod(Advisor advisor, CtMethod method, CtClass clazz, CtMethod delegate, long hash) throws Exception
    {
       CtClass[] exceptions = method.getExceptionTypes();
 
       // create base, delegating method.
       CtMethod newMethod = CtNewMethod.wrapped(method.getReturnType(),
                                                method.getName(),
                                                method.getParameterTypes(),
                                                exceptions,
                                                delegate,
                                                CtMethod.ConstParameter.integer(hash),
                                                clazz);
       newMethod.setModifiers(Modifier.PUBLIC);
       clazz.addMethod(newMethod);
    }
 
    private void addMixin(CtClass clazz, InterfaceIntroduction pointcut, InterfaceIntroduction.Mixin mixin, HashMap baseMethods) throws Exception
    {
       // REVISIT:
       // Later on we should follow the same pattern as
       // C++ public virtual Mixins
       // But, for now, just throw an exception if the
       // mixin is adding any interfaces already
       // defined in base class or another mixin.
       CtClass mixinClass = classPool.get(mixin.getClassName());
       String initializer = (mixin.getConstruction() == null) ? ("new " + mixinClass.getName() + "()") : mixin.getConstruction();
       CtClass type = forName(mixinClass.getName());
       CtField field = new CtField(type, mixinFieldName(mixinClass), clazz);
       int modifiers = Modifier.PRIVATE;
       if (mixin.isTransient()) modifiers = modifiers | Modifier.TRANSIENT;
       field.setModifiers(modifiers);
       clazz.addField(field, CtField.Initializer.byExpr(initializer));
       HashSet addedMethods = new HashSet();
 
       String[] interfaces = mixin.getInterfaces();
       for (int i = 0; i < interfaces.length; i++)
       {
          CtClass intf = classPool.get(interfaces[i]);
          if (clazz.subtypeOf(intf)) continue;
          clazz.addInterface(intf);
          HashMap intfMap = JavassistMethodHashing.getMethodMap(intf);
          Iterator entries = intfMap.entrySet().iterator();
          while (entries.hasNext())
          {
             Map.Entry entry = (Map.Entry) entries.next();
             Long hash = (Long) entry.getKey();
             CtMethod method = (CtMethod) entry.getValue();
             CtMethod baseMethod = (CtMethod)baseMethods.get(hash); 
             if (baseMethod != null && !addedMethods.contains(hash))
             {
                String msg = "Mixin " + mixinClass.getName() +
                         " of pointcut " + pointcut.getName() +
                         " is trying to apply an already existing method" + method.getName() + " for class " + clazz.getName();
                
                if (baseMethod.getDeclaringClass().equals(clazz))
                {
                   throw new RuntimeException(msg);
                }
                else
                {
                   if (AspectManager.verbose)System.out.println("[warn] " + msg);
                }
             }
             // If another interface of this mixin has a duplicate method, then its ok, but don't re-add
             if (addedMethods.contains(hash)) continue;
             createMixinInvokeMethod(clazz, mixinClass, initializer, method, hash.longValue());
             baseMethods.put(hash, method);
             addedMethods.add(hash);
          }
       }
    }
 
    private void addIntroductionPointcutInterface(CtClass clazz, Advisor advisor, String intf, HashMap baseMethods) throws Exception
    {
       CtClass iface = classPool.get(intf);
       if (clazz.subtypeOf(iface)) return;
       if (clazz.subclassOf(iface)) return;
 
       clazz.addInterface(iface);
 
       CtMethod mixinInvokeMethod = createInvokeMethod(clazz);
       HashMap intfMap = JavassistMethodHashing.getMethodMap(iface);
       Iterator entries = intfMap.entrySet().iterator();
       while (entries.hasNext())
       {
          Map.Entry entry = (Map.Entry) entries.next();
          Long hash = (Long) entry.getKey();
          if (baseMethods.containsKey(hash)) continue;
          CtMethod method = (CtMethod) entry.getValue();
          addMixinMethod(advisor, method, clazz, mixinInvokeMethod, hash.longValue());
          baseMethods.put(hash, method);
       }
    }
 
    private void instrumentIntroductions(CtClass clazz, Advisor advisor)
            throws Exception
    {
       ArrayList pointcuts = advisor.getInterfaceIntroductions();
       if (pointcuts.size() == 0) return;
       HashMap baseMethods = JavassistMethodHashing.getMethodMap(clazz);
       Iterator it = pointcuts.iterator();
       if (it.hasNext()) setupBasics(clazz);
       while (it.hasNext())
       {
 
          InterfaceIntroduction pointcut = (InterfaceIntroduction) it.next();
          ArrayList mixins = pointcut.getMixins();
          for (int i = 0; i < mixins.size(); i++)
          {
             InterfaceIntroduction.Mixin mixin = (InterfaceIntroduction.Mixin) mixins.get(i);
             addMixin(clazz, pointcut, mixin, baseMethods);
          }
       }
 
       // pointcut interfaces.  If a method is already implemented for it then use that method
       // otherwise delegate to an interceptor
       it = pointcuts.iterator();
       while (it.hasNext())
       {
          InterfaceIntroduction pointcut = (InterfaceIntroduction) it.next();
          String[] interfaces = pointcut.getInterfaces();
          if (interfaces == null) continue;
          for (int i = 0; i < interfaces.length; i++)
          {
             addIntroductionPointcutInterface(clazz, advisor, interfaces[i], baseMethods);
          }
       }
    }
 
    private boolean instrumentAnnotationIntroductions(CtClass clazz, ClassAdvisor advisor)
            throws Exception
    {
       boolean changed = false;
       Iterator it = advisor.getManager().getAnnotationIntroductions().iterator();
       while (it.hasNext())
       {
          AnnotationIntroduction introduction = (AnnotationIntroduction) it.next();
          if (AspectManager.verbose) System.out.println("**** " + introduction.getOriginalAnnotationExpr() + " invisible: " + introduction.isInvisible() + " expr: " + introduction.getOriginalExpression());
          if (introduction.matches(advisor, clazz))
          {
             if (AspectManager.verbose) System.out.println(introduction.getAnnotation() + " binds to " + clazz.getName());
             javassist.bytecode.annotation.Annotation info = AnnotationInfoCreator.createAnnotationInfo(classPool, clazz.getClassFile2().getConstPool(), introduction.getAnnotation());
             if (introduction.isInvisible())
             {
                AnnotationsAttribute invisible = (AnnotationsAttribute) clazz.getClassFile2().getAttribute(AnnotationsAttribute.invisibleTag);
                if (invisible == null)
                {
                   invisible = new AnnotationsAttribute(clazz.getClassFile2().getConstPool(), AnnotationsAttribute.invisibleTag);
                   clazz.getClassFile2().addAttribute(invisible);
                }
                changed = true;
                invisible.addAnnotation(info);
             }
             else
             {
                AnnotationsAttribute visible = (AnnotationsAttribute) clazz.getClassFile2().getAttribute(AnnotationsAttribute.visibleTag);
                if (visible == null)
                {
                   visible = new AnnotationsAttribute(clazz.getClassFile2().getConstPool(), AnnotationsAttribute.visibleTag);
                   clazz.getClassFile2().addAttribute(visible);
                }
                changed = true;
                visible.addAnnotation(info);
             }
          }
 
          CtMethod[] methods = clazz.getDeclaredMethods();
          for (int i = 0; i < methods.length; i++)
          {
             if (introduction.matches(advisor, methods[i]))
             {
                javassist.bytecode.annotation.Annotation info = AnnotationInfoCreator.createAnnotationInfo(classPool, methods[i].getMethodInfo2().getConstPool(), introduction.getAnnotation());
                MethodInfo mi = methods[i].getMethodInfo2();
                if (introduction.isInvisible())
                {
                   AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
                   if (invisible == null)
                   {
                      invisible = new AnnotationsAttribute(mi.getConstPool(), AnnotationsAttribute.invisibleTag);
                      mi.addAttribute(invisible);
                   }
                   changed = true;
                   invisible.addAnnotation(info);
                }
                else
                {
                   AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
                   if (visible == null)
                   {
                      visible = new AnnotationsAttribute(mi.getConstPool(), AnnotationsAttribute.visibleTag);
                      mi.addAttribute(visible);
                   }
                   changed = true;
                   visible.addAnnotation(info);
                }
             }
 
          }
 
          CtConstructor[] cons = clazz.getDeclaredConstructors();
          for (int i = 0; i < cons.length; i++)
          {
             if (introduction.matches(advisor, cons[i]))
             {
                javassist.bytecode.annotation.Annotation info = AnnotationInfoCreator.createAnnotationInfo(classPool, cons[i].getMethodInfo2().getConstPool(), introduction.getAnnotation());
                MethodInfo mi = cons[i].getMethodInfo2();
                if (introduction.isInvisible())
                {
                   AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
                   if (invisible == null)
                   {
                      invisible = new AnnotationsAttribute(mi.getConstPool(), AnnotationsAttribute.invisibleTag);
                      mi.addAttribute(invisible);
                   }
                   changed = true;
                   invisible.addAnnotation(info);
                }
                else
                {
                   AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
                   if (visible == null)
                   {
                      visible = new AnnotationsAttribute(mi.getConstPool(), AnnotationsAttribute.visibleTag);
                      mi.addAttribute(visible);
                   }
                   changed = true;
                   visible.addAnnotation(info);
                }
             }
          }
 
          CtField[] fields = clazz.getDeclaredFields();
          for (int i = 0; i < fields.length; i++)
          {
             if (introduction.matches(advisor, fields[i]))
             {
                javassist.bytecode.annotation.Annotation info = AnnotationInfoCreator.createAnnotationInfo(classPool, fields[i].getFieldInfo2().getConstPool(), introduction.getAnnotation());
                FieldInfo mi = fields[i].getFieldInfo2();
                if (introduction.isInvisible())
                {
                   AnnotationsAttribute invisible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag);
                   if (invisible == null)
                   {
                      invisible = new AnnotationsAttribute(mi.getConstPool(), AnnotationsAttribute.invisibleTag);
                      mi.addAttribute(invisible);
                   }
                   changed = true;
                   invisible.addAnnotation(info);
                }
                else
                {
                   AnnotationsAttribute visible = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
                   if (visible == null)
                   {
                      visible = new AnnotationsAttribute(mi.getConstPool(), AnnotationsAttribute.visibleTag);
                      mi.addAttribute(visible);
                   }
                   changed = true;
                   visible.addAnnotation(info);
                }
             }
          }
       }
       return changed;
    }
 
    private boolean instrumentAnnotationOverrides(CtClass clazz, ClassAdvisor advisor)
            throws Exception
    {
       boolean changed = false;
       Iterator it = advisor.getManager().getAnnotationOverrides().iterator();
       while (it.hasNext())
       {
          AnnotationIntroduction introduction = (AnnotationIntroduction) it.next();
          if (introduction.matches(advisor, clazz))
          {
             advisor.getAnnotations().addClassAnnotation(introduction.getAnnotation().getIdentifier(), introduction.getOriginalAnnotationExpr());
          }
 
          CtMethod[] methods = clazz.getDeclaredMethods();
          for (int i = 0; i < methods.length; i++)
          {
             if (introduction.matches(advisor, methods[i]))
             {
                advisor.getAnnotations().addAnnotation(methods[i], introduction.getAnnotation().getIdentifier());
             }
          }
 
          CtConstructor[] cons = clazz.getDeclaredConstructors();
          for (int i = 0; i < cons.length; i++)
          {
             if (introduction.matches(advisor, cons[i]))
             {
                advisor.getAnnotations().addAnnotation(cons[i], introduction.getAnnotation().getIdentifier());
             }
          }
 
          CtField[] fields = clazz.getDeclaredFields();
          for (int i = 0; i < fields.length; i++)
          {
             if (introduction.matches(advisor, fields[i]))
             {
                advisor.getAnnotations().addAnnotation(fields[i], introduction.getAnnotation().getIdentifier());
             }
          }
       }
       return changed;
    }
 
    public boolean applyCallerPointcuts(CtClass clazz, ClassAdvisor advisor) throws CannotCompileException
    {
       return callerTransformer.applyCallerPointcuts(clazz, advisor);
    }
    
    /**
     * Find all classes that this class references.  If any of those classes are advised and have field and/or constructor
     * interception, do instrumentation on this class so that those fields and constructors are instrumented
     */
    protected boolean convertReferences(CtClass clazz) throws Exception
    {
       boolean converted = false;
       String ref = null;
       try
       {
          AOPClassPool pool = AOPClassPool.createAOPClassPool(clazz.getClassPool(), AOPClassPoolRepository.getInstance());
          
          //Class.getRefClasses() only gets classes explicitly referenced in the class. We need to check the super classes and do some extra handling
          for (ReferenceClassIterator it = new ReferenceClassIterator(clazz.getRefClasses()) ; it.hasNext() ; )
          {
             ref = it.next();
             if (!manager.getInterceptionMarkers().convertReference(ref)
                 || manager.isNonAdvisableClassName(ref)
                 || ref.startsWith("java.")
                 || ref.startsWith("javax.")
                 || ref.startsWith("["))
             {
                continue;
             }
             // Only need a temporary advisor for resolving metadata
             CtClass ctRef = null;
             try
             {
                ctRef = pool.get(ref);
             }
             catch (NotFoundException e)
             {
                if (AspectManager.suppressReferenceErrors)
                {
                   System.err.println("[warn] Could not find class " + ref + " that " + clazz.getName() + " references.  It may not be in your classpath and you may not be getting field and constructor weaving for this class.");
                   if (AspectManager.verbose) e.printStackTrace();
                   continue;
                }
                else
                {
                   throw e;
                }
             }
             if (!isTransformable(ctRef)) continue;
             
             it.addSuperClass(ctRef);
             
             ClassAdvisor advisor = manager.getTempClassAdvisor(ctRef);
             
             
             if (!manager.getInterceptionMarkers().shouldSkipFieldAccess(ref) && !ref.equals(clazz.getName()))
             {
                List fields = getAdvisableFields(ctRef);
                if (fieldAccessTransformer.replaceFieldAccess(fields, ctRef, advisor))
                {
                   manager.getInterceptionMarkers().addFieldInterceptionMarker(ref);
                   converted = true;
                }
                else
                {
                   manager.getInterceptionMarkers().skipFieldAccess(ref);
                }
             }
             if (!manager.getInterceptionMarkers().shouldSkipConstruction(ref))
             {
                if (constructorExecutionTransformer.replaceConstructorAccess(advisor, ctRef))
                {
                   manager.getInterceptionMarkers().addConstructionInterceptionMarker(ref);
                   converted = true;
                }
                else
                {
                   manager.getInterceptionMarkers().skipConstruction(ref);
                }
             }
 
             if (!converted)
             {
                manager.getInterceptionMarkers().skipReference(ref);
             }
             ref = null;
          }
       }
       catch (Exception ex)
       {
          if (ref != null)
          {
             throw new TransformationException("Failed to aspectize class " + clazz.getName() + ".  Could not find class it references " + ref + "  It may not be in your classpath and you may not be getting field and constructor weaving for this class.");
          }
          throw ex;
       }
       return converted;
    }
 
    protected boolean shouldNotTransform(CtClass clazz)throws NotFoundException
    {
       return (clazz.isInterface() ||
             clazz.isFrozen() ||
             clazz.isArray() ||
             clazz.getName().startsWith("org.jboss.aop") ||
             isAdvised(clazz) ||
             !isTransformable(clazz));
    }
    
    /**
     * Makes class advisable.
     */
    public boolean transform(CtClass clazz,
                             ClassAdvisor advisor)
    {
       synchronized(this.processedClasses)
       {
          processedClasses.add(clazz);
       }
       try
       {
          if (shouldNotTransform(clazz)) return false;
          if (AspectManager.verbose) System.out.println("[trying to transform] " + clazz.getName());
 
          DeclareChecker.checkDeclares(manager, clazz, advisor);
 
          boolean converted = instrumentAnnotationIntroductions(clazz, advisor);
          converted = instrumentAnnotationOverrides(clazz, advisor) || converted;
          boolean constructorAccessConverted = false;
          converted = applyCallerPointcuts(clazz, advisor) || converted;
          methodExecutionTransformer.instrument(clazz, advisor);
          boolean constructionTransformation = constructionTransformer.insertConstructionInterception(clazz, advisor);
          constructorAccessConverted = constructorExecutionTransformer.transform(clazz, advisor);
          String classname = clazz.getName();
          if (constructorAccessConverted)
          {
             manager.getInterceptionMarkers().addConstructionInterceptionMarker(classname);
          }
          else
          {
             manager.getInterceptionMarkers().skipConstruction(classname);
          }
          converted = converted || constructorAccessConverted;
 
          instrumentIntroductions(clazz, advisor);
 
          converted = convertReferences(clazz) || converted;
          // need to instrument no matter what so that
          // previously declared field and constructor interceptions
          // get instrumented within this class.
          if (converted || basicsSet)
          {
             clazz.instrument(converter);
          }
 
          // create static wrapper methods after
          // clazz.instrument because the wrappers may call cons or fields
          fieldAccessTransformer.buildFieldWrappers(clazz, advisor);
          if (constructorAccessConverted)
          {
             constructorExecutionTransformer.codeConverted();
          }
          else
          {
             if (manager.getInterceptionMarkers().shouldSkipFieldAccess(classname))
             {
                manager.getInterceptionMarkers().skipReference(classname);
             }
          }
 
 
          // notifies dynamic transformation observer
          dynamicTransformationObserver.transformationFinished(clazz, converter);
 
          if (AspectManager.verbose) System.out.println("[debug] was " + clazz.getName() + " converted: " + (basicsSet || converted));
 
          if (basicsSet || converted)
          {
             return true;
          }
          else
          {
             //classPool.flushClass(clazz.getName());
             return false;
          }
 
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
 
 
    public List getConstructors(CtClass clazz)
    {
       List list = new ArrayList();
 
       CtConstructor[] constructors = clazz.getDeclaredConstructors();
 
       for (int i = 0; i < constructors.length; i++)
       {
          list.add(constructors[i]);
       }
       Collections.sort(list, CtConstructorComparator.INSTANCE);
 
       return list;
    }
 
    /**
     * Gets sorted collection of advisable methods.
     */
    public static List getAdvisableFields(CtClass clazz) throws NotFoundException
    {
       List list = new ArrayList();
       CtField[] fields = clazz.getDeclaredFields();
       for (int i = 0; i < fields.length; i++)
       {
          if (Advisable.isAdvisable(fields[i]))
          {
             list.add(fields[i]);
          }
       }
       Collections.sort(list, CtFieldComparator.INSTANCE);
 
       return list;
    }
 
    /**
     * Creates generic invoke method to be wrapped by real signatures.
     */
    private CtMethod createInvokeMethod(CtClass clazz)
            throws CannotCompileException
    {
       return CtNewMethod.make("public java.lang.Object invoke(java.lang.Object[] args, long i)" +
                               "       throws java.lang.Throwable {" +
                               "   return ((org.jboss.aop.ClassAdvisor)this._getAdvisor()).invokeMethod(this, i, args);" +
                               "}",
                               clazz);
    }
 
    /**
     * Gets a class by its name.
     */
    public CtClass forName(String name) throws NotFoundException
    {
       return this.classPool.get(name);
    }
 
    /**
     * Gets a class by its name.
     */
    public CtClass forName(ClassPool pool, String name) throws NotFoundException
    {
       return pool.get(name);
    }
 
 
    /**
     * Adds a static field to a class.
     */
    protected CtField addStaticField(CtClass clazz, String name, String typeName,
                                   CtField.Initializer initializer)
            throws CannotCompileException, NotFoundException
    {
       CtClass type = forName(typeName);
       CtField field = new CtField(type, name, clazz);
       field.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
       clazz.addField(field, initializer);
 
       return field;
    }
 
    /**
     * Adds a protected field to a class.
     */
    protected CtField addProtectedField(CtClass clazz, String name, String typeName,
                                      CtField.Initializer initializer)
            throws CannotCompileException, NotFoundException
    {
       CtClass type = forName(typeName);
       CtField field = new CtField(type, name, clazz);
       field.setModifiers(Modifier.PROTECTED | Modifier.TRANSIENT);
       if (initializer != null)
       {
          clazz.addField(field, initializer);
       }
       else
       {
          clazz.addField(field);
       }
       return field;
    }
 
    public void setupBasics(CtClass clazz) throws CannotCompileException, NotFoundException
    {
       if (basicsSet) return;
       basicsSet = true;
       // add serialVersionUID.
       SerialVersionUID.setSerialVersionUID(clazz);
 
       // add marker interface.
       clazz.addInterface(forName(AOP_PACKAGE + ".Advised"));
       
       doSetupBasics(clazz);
    }
    
    /**
     * Notifies the <code>Instrumentor</code> that some joinpoint status were updated.
     * This method hot swaps the code of afected classes.
     * @param joinpointUpdates a collection of <code>org.jboss.aop.instrument.JoinpointStatusUpdate</code>.
     * @param hotSwapper object capable of hot swapping classes.
     */
    public synchronized void interceptorChainsUpdated(Collection joinpointUpdates, HotSwapper hotSwapper) {
       //creates a converter
       this.converter = new CodeConverter();
       // list of instrumented classes
       Collection classes = new HashSet();
       try {
          // transform classes whose joinpont status have changed
          for (Iterator iterator = joinpointUpdates.iterator(); iterator.hasNext(); )
          {
             JoinpointStatusUpdate update = (JoinpointStatusUpdate) iterator.next();
             CtClass clazz = update.clazz;
             JoinpointStatusUpdate.ClassJoinpoints wrapTargets = update.newlyAdvisedJoinpoints;
             JoinpointStatusUpdate.ClassJoinpoints unwrapTargets = update.newlyUnadvisedJoinpoints;
             
             clazz.defrost();
             fieldAccessTransformer.wrap(clazz, wrapTargets.fieldReads, wrapTargets.fieldWrites);
             fieldAccessTransformer.unwrap(clazz, unwrapTargets.fieldReads, unwrapTargets.fieldWrites);
             constructorExecutionTransformer.wrap(clazz, wrapTargets.constructorExecutions);
             constructorExecutionTransformer.unwrap(clazz, unwrapTargets.constructorExecutions);
             methodExecutionTransformer.wrap(clazz, wrapTargets.methodExecutions);
             methodExecutionTransformer.unwrap(clazz, unwrapTargets.methodExecutions);
             if (!update.isEmpty())
             {
                clazz.instrument(converter);
                classes.add(clazz);
             }
          }
          // instrument classes that access the joinpoints whose status have changed, in
          // order to make this classes access the joinpoint wrapper instead
          Collection classPools = manager.getRegisteredCLs().values();
          Collection conversionsRegistered = new HashSet();
          synchronized(this.processedClasses)
          {
             for (Iterator iterator2 = processedClasses.iterator(); iterator2.hasNext(); ) {       
 
                CtClass clazz = (CtClass) iterator2.next();
                if (manager.isNonAdvisableClassName(clazz.getName()) || ! isTransformable(clazz))
                {
                   continue;
                }
                // class already instrumented
                if (classes.contains(clazz))
                {
                   continue;
                }
                // check if clazz should be added to classes
                clazz.defrost();
                byte[] previousByteCode = clazz.toBytecode();
                clazz.defrost();
                clazz.instrument(converter);
                if (!java.util.Arrays.equals(clazz.toBytecode(), previousByteCode))
                {
                   classes.add(clazz);
                }
                clazz.defrost();
             }
          }
          // notifies code conversion observers
          fieldAccessTransformer.codeConverted();
          constructorExecutionTransformer.codeConverted();
          
          // registers the classes bytecodes to be hot swapped
          for (Iterator iterator = classes.iterator(); iterator.hasNext(); )
          {
             CtClass clazz = (CtClass) iterator.next();
             AOPClassPool classPool = (AOPClassPool) clazz.getClassPool();
             clazz.defrost();
             hotSwapper.registerChange(classPool.getClassLoader().loadClass(clazz.getName()),
                   clazz.toBytecode());
          }
          // performs the hot swap of registered classes
          hotSwapper.hotSwap();
       }
       catch (Exception e) {
          e.printStackTrace();
          if (AspectManager.suppressTransformationErrors)
          {
             System.err.println("[warn] AOP Instrumentor failed to updated wrapping status.");
             e.printStackTrace();
          }
          else
          {
             if (e instanceof TransformationException)
             {
                throw ((TransformationException) e);
             }
             else
             {
                throw new RuntimeException("failed to update wrapping status", e);
             }
          }
       }
    }
 
    
    /**
     * Converts all processed classes to make wrapping of the appropriate joinpoints.
     * This method must be called if some dynamic transformation ocurred (i. e. a 
     * class has just been loaded and one or more of its joinpoints were wrapped due
     * only to bindings added dynamicaly; in this case, the previously loaded classes
     * may not call the wrappers of this joinpoints, and need to be instrumented).
     * @param hostSwapper
     * @param clazz the clazz whose transformation involved dynamic wrapping.
     * @param fieldReads collection of fields whose read joinpoit was dynamicaly wrapped.
     * @param fieldWrites collection of fields whose read joinpoit was dynamicaly wrapped.
     * @param constructor <code>true</code> if the <code>clazz</code> constructors were
     * dynamicaly wrapped. 
     */
    public void convertProcessedClasses(HotSwapper hotSwapper, CtClass clazz,
          Collection fieldReads, Collection fieldWrites, boolean constructor)
    {
       AOPClassPool classPool = (AOPClassPool) clazz.getClassPool();
       CodeConverter codeConverter = new CodeConverter();
       for (Iterator iterator = fieldReads.iterator(); iterator.hasNext(); )
       {
          CtField field = (CtField) iterator.next();
          codeConverter.replaceFieldRead(field, clazz, fieldAccessTransformer.fieldRead(field.getName()));
       }
      for (Iterator iterator = fieldReads.iterator(); iterator.hasNext(); )
       {
          CtField field = (CtField) iterator.next();
          codeConverter.replaceFieldWrite(field, clazz, fieldAccessTransformer.fieldWrite(field.getName()));
       }
       if (constructor)
       {
          codeConverter.replaceNew(clazz, clazz, ConstructorExecutionTransformer.constructorFactory(clazz.getSimpleName()));
       }
          
       synchronized(processedClasses)
       {
       for (Iterator iterator = processedClasses.iterator(); iterator.hasNext();)
       {
          CtClass processedClass = (CtClass) iterator.next();
          if (processedClass == clazz)
             continue;
          if (processedClass.getRefClasses() == null ||
                 ! clazz.getRefClasses().contains(clazz.getName()))
           {
              continue;
           }
           try
           {
              processedClass.defrost();
              byte[] previousByteCode = processedClass.toBytecode();
              processedClass.defrost();
              processedClass.instrument(codeConverter);
              byte[] updatedByteCode = processedClass.toBytecode();
              if (!java.util.Arrays.equals(updatedByteCode, previousByteCode))
              {
                hotSwapper.registerChange(classPool.getClassLoader().loadClass(processedClass.getName()), updatedByteCode);
              }
              processedClass.defrost();
           }
           catch (Exception e)
           {
              e.printStackTrace();
              if (AspectManager.suppressTransformationErrors)
              {
                 System.err.println("[warn] AOP Instrumentor failed to updated wrapping status.");
                 e.printStackTrace();
              }
              else if (e instanceof TransformationException)
              {
                 throw ((TransformationException) e);
              }
              else
              {
                 throw new RuntimeException("failed to update wrapping status", e);
              }
           }
 
          }
       }
       hotSwapper.hotSwap();
    }
    
    protected abstract void doSetupBasics(CtClass clazz) throws CannotCompileException, NotFoundException;
    
    /**
     * Creates generic invoke method to be wrapped by real signatures.
     */
    protected abstract CtMethod createMixinInvokeMethod(CtClass clazz, CtClass mixinClass, String initializer, CtMethod method, long hash)
            throws CannotCompileException, NotFoundException, Exception;
 
    private static class ReferenceClassIterator
    {
       int size;
       int current;
       ArrayList classes;
       HashSet handledClasses;
       String currentEntry;
       
       public ReferenceClassIterator(Collection refClasses)
       {
          size = refClasses.size();
          classes = new ArrayList(refClasses.size());
          classes.addAll(refClasses);
          handledClasses = new HashSet(refClasses.size());
       }
 
       boolean hasNext()
       {
          while (current < size)
          {
             String s = (String) classes.get(current++);
             if (!handledClasses.contains(s))
             {
                handledClasses.add(s);
                currentEntry = s;
                return true;
             }
          }
          return false;
       }
 
       String next()
       {
          return currentEntry;
       }
       
       void addSuperClass(CtClass clazz)throws NotFoundException
       {
          if (clazz != null)
          {
             CtClass superClass = clazz.getSuperclass();
             if (superClass != null)
             {
                String name = superClass.getName();
                if (!handledClasses.contains(name))
                {
                   classes.add(name);
                   size++;
                }
             }
          }
       }
    }
    
 }
