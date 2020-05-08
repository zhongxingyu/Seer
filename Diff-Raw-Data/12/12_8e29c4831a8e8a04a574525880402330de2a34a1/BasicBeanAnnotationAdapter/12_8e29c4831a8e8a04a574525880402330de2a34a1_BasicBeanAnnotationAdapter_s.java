 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.kernel.plugins.annotations;
 
 import java.lang.annotation.Annotation;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.metadata.KernelMetaDataRepository;
 import org.jboss.logging.Logger;
 import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
 import org.jboss.metadata.spi.signature.ConstructorSignature;
 import org.jboss.metadata.spi.signature.FieldSignature;
 import org.jboss.metadata.spi.signature.MethodSignature;
 import org.jboss.metadata.spi.signature.Signature;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.FieldInfo;
 import org.jboss.reflect.spi.MethodInfo;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 @SuppressWarnings("unchecked")
 public class BasicBeanAnnotationAdapter implements BeanAnnotationAdapter
 {
    protected Logger log = Logger.getLogger(BasicBeanAnnotationAdapter.class);
 
    protected Set<AnnotationPlugin> classAnnotationPlugins = new HashSet<AnnotationPlugin>();
    protected Set<AnnotationPlugin> constructorAnnotationPlugins = new HashSet<AnnotationPlugin>();
    protected Set<AnnotationPlugin> propertyAnnotationPlugins = new HashSet<AnnotationPlugin>();
    protected Set<AnnotationPlugin> methodAnnotationPlugins = new HashSet<AnnotationPlugin>();
    protected Set<AnnotationPlugin> fieldAnnotationPlugins = new HashSet<AnnotationPlugin>();
 
    public BasicBeanAnnotationAdapter()
    {
       // -- adapters
       Annotation2ValueMetaDataAdapter[] adapters = new Annotation2ValueMetaDataAdapter[]{
          InjectAnnotationPlugin.INSTANCE,
          StringValueAnnotationPlugin.INSTANCE,
          ValueFactoryAnnotationPlugin.INSTANCE,
          ThisValueAnnotationPlugin.INSTANCE,
          NullValueAnnotationPlugin.INSTANCE,
       };
       // -- plugins
       // class
       addAnnotationPlugin(new AliasesAnnotationPlugin());
       addAnnotationPlugin(new DemandsAnnotationPlugin());
       addAnnotationPlugin(new DependsAnnotationPlugin());
       addAnnotationPlugin(new SupplysAnnotationPlugin());
       // constructor
       addAnnotationPlugin(new ConstructorParameterAnnotationPlugin(adapters));
       // property
       addAnnotationPlugin(InjectAnnotationPlugin.INSTANCE);
       addAnnotationPlugin(StringValueAnnotationPlugin.INSTANCE);
       addAnnotationPlugin(ValueFactoryAnnotationPlugin.INSTANCE);
       addAnnotationPlugin(ThisValueAnnotationPlugin.INSTANCE);
       addAnnotationPlugin(NullValueAnnotationPlugin.INSTANCE);
       addAnnotationPlugin(new PropertyInstallCallbackAnnotationPlugin());
       addAnnotationPlugin(new PropertyUninstallCallbackAnnotationPlugin());
       // method
       addAnnotationPlugin(new FactoryMethodAnnotationPlugin(adapters));
       addAnnotationPlugin(new CreateLifecycleAnnotationPlugin(adapters));
       addAnnotationPlugin(new StartLifecycleAnnotationPlugin(adapters));
       addAnnotationPlugin(new StopLifecycleAnnotationPlugin(adapters));
       addAnnotationPlugin(new DestroyLifecycleAnnotationPlugin(adapters));
       addAnnotationPlugin(new MethodInstallCallbackAnnotationPlugin());
       addAnnotationPlugin(new MethodUninstallCallbackAnnotationPlugin());
       addAnnotationPlugin(new InstallMethodParameterAnnotationPlugin(adapters));
       addAnnotationPlugin(new UninstallMethodParameterAnnotationPlugin(adapters));
       // field
    }
 
    protected void addAnnotationPlugin(AnnotationPlugin plugin)
    {
       Class<? extends Annotation> annotation = plugin.getAnnotation();
       if (annotation.getAnnotation(Target.class) == null)
          log.warn("Annotation " + annotation + " missing @Target annotation!");
       if (annotation.getAnnotation(Retention.class) == null)
          log.warn("Annotation " + annotation + " missing @Retention annotation!");
 
      if (plugin.getSupportedTypes().contains(ElementType.TYPE))
       {
          classAnnotationPlugins.add(plugin);
       }
      if (plugin.getSupportedTypes().contains(ElementType.CONSTRUCTOR))
       {
          constructorAnnotationPlugins.add(plugin);
       }
      if (plugin.getSupportedTypes().contains(ElementType.METHOD))
       {
          if (plugin instanceof PropertyAware)
             propertyAnnotationPlugins.add(plugin);
          else
             methodAnnotationPlugins.add(plugin);
       }
      if (plugin.getSupportedTypes().contains(ElementType.FIELD))
       {
          fieldAnnotationPlugins.add(plugin);
       }
    }
 
    public void applyAnnotations(KernelControllerContext context) throws Throwable
    {
       Kernel kernel = context.getKernel();
       KernelMetaDataRepository repository = kernel.getMetaDataRepository();
       MetaDataRetrieval retrieval = repository.getMetaDataRetrieval(context);
 
       boolean trace = log.isTraceEnabled();
       BeanInfo info = context.getBeanInfo();
 
       // class
       ClassInfo classInfo = info.getClassInfo();
       for(AnnotationPlugin plugin : classAnnotationPlugins)
          plugin.applyAnnotation(classInfo, retrieval, context);
 
       // constructors
       Set<ConstructorInfo> constructors = info.getConstructors();
       if (constructors != null && constructors.isEmpty() == false)
       {
          for(ConstructorInfo ci : constructors)
          {
             Signature cis = new ConstructorSignature(Configurator.getParameterTypes(trace, ci.getParameterTypes()));
             MetaDataRetrieval cmdr = retrieval.getComponentMetaDataRetrieval(cis);
             if (cmdr != null)
             {
                for(AnnotationPlugin plugin : constructorAnnotationPlugins)
                   plugin.applyAnnotation(ci, cmdr, context);
             }
          }
       }
 
       // properties
       Set<PropertyInfo> properties = info.getProperties();
       if (properties != null && properties.isEmpty() == false)
       {
          for(PropertyInfo pi : properties)
          {
             MethodInfo setter = pi.getSetter();
             if (setter != null)
             {
                Signature sis = new MethodSignature(setter.getName(), Configurator.getParameterTypes(trace, setter.getParameterTypes()));
                MetaDataRetrieval cmdr = retrieval.getComponentMetaDataRetrieval(sis);
                if (cmdr != null)
                {
                   for(AnnotationPlugin plugin : propertyAnnotationPlugins)
                      plugin.applyAnnotation(pi, cmdr, context);
                }
             }
          }
       }
 
       // methods
       Set<MethodInfo> methods = info.getMethods();
       if (methods != null && methods.isEmpty() == false)
       {
          for(MethodInfo mi : methods)
          {
             Signature mis = new MethodSignature(mi.getName(), Configurator.getParameterTypes(trace, mi.getParameterTypes()));
             MetaDataRetrieval cmdr = retrieval.getComponentMetaDataRetrieval(mis);
             if (cmdr != null)
             {
                for(AnnotationPlugin plugin : methodAnnotationPlugins)
                   plugin.applyAnnotation(mi, cmdr, context);
             }
          }
       }
 
       // static methods
       MethodInfo[] staticMethods = classInfo.getDeclaredMethods();
       if (staticMethods != null && staticMethods.length != 0)
       {
          for(MethodInfo smi : staticMethods)
          {
             if (smi.isStatic() && smi.isPublic())
             {
                Signature mis = new MethodSignature(smi.getName(), Configurator.getParameterTypes(trace, smi.getParameterTypes()));
                MetaDataRetrieval cmdr = retrieval.getComponentMetaDataRetrieval(mis);
                if (cmdr != null)
                {
                   for(AnnotationPlugin plugin : methodAnnotationPlugins)
                      plugin.applyAnnotation(smi, cmdr, context);
                }
             }
          }
       }
 
       // fields
       FieldInfo[] fields = classInfo.getDeclaredFields();
       if (fields != null && fields.length > 0)
       {
          for(FieldInfo fi : fields)
          {
             Signature fis = new FieldSignature(fi.getName());
             MetaDataRetrieval cmdr = retrieval.getComponentMetaDataRetrieval(fis);
             if (cmdr != null)
             {
                for(AnnotationPlugin plugin : fieldAnnotationPlugins)
                   plugin.applyAnnotation(fi, cmdr, context);
             }
          }
       }
    }
 
 }
