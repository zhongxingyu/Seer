 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat, Inc., and individual contributors
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
 package org.jboss.forge.validation;
 
 import java.io.FileNotFoundException;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 import javax.validation.constraints.AssertFalse;
 import javax.validation.constraints.AssertTrue;
 import javax.validation.constraints.DecimalMax;
 import javax.validation.constraints.DecimalMin;
 import javax.validation.constraints.Digits;
 import javax.validation.constraints.Future;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Null;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 import org.jboss.forge.parser.java.Annotation;
 import org.jboss.forge.parser.java.JavaClass;
 import org.jboss.forge.project.Project;
 import org.jboss.forge.project.facets.JavaSourceFacet;
 import org.jboss.forge.resources.Resource;
 import org.jboss.forge.resources.java.JavaFieldResource;
 import org.jboss.forge.resources.java.JavaMethodResource;
 import org.jboss.forge.resources.java.JavaResource;
 import org.jboss.forge.shell.Shell;
 import org.jboss.forge.shell.plugins.Alias;
 import org.jboss.forge.shell.plugins.Command;
 import org.jboss.forge.shell.plugins.Option;
 import org.jboss.forge.shell.plugins.Plugin;
 import org.jboss.forge.shell.plugins.RequiresFacet;
 import org.jboss.forge.shell.plugins.RequiresResource;
 import org.jboss.forge.validation.api.ValidationFacet;
 import org.jboss.forge.validation.completer.PropertyCompleter;
 
 import static org.jboss.forge.validation.util.ResourceHelper.addAnnotationTo;
 import static org.jboss.forge.validation.util.ResourceHelper.getJavaClassFromResource;
 
 /**
  * @author Kevin Pollet
  */
 @Alias("new-constraint")
 @RequiresResource({JavaResource.class, JavaFieldResource.class, JavaMethodResource.class})
 @RequiresFacet({ValidationFacet.class, JavaSourceFacet.class})
 public class ConstraintPlugin implements Plugin
 {
     private final JavaSourceFacet javaSourceFacet;
     private final Shell shell;
 
     @Inject
     public ConstraintPlugin(Project project, Shell shell)
     {
         this.javaSourceFacet = project.getFacet(JavaSourceFacet.class);
         this.shell = shell;
     }
 
     @Command(value = "Valid")
    public void addNullConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Valid.class);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
         outputConstraintAdded(property, Valid.class);
     }
 
     @Command(value = "Null")
     public void addNullConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                   @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Null.class);
         addConstraintMessageTo(constraintAnnotation, message);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Null.class);
     }
 
     @Command(value = "NotNull")
     public void addNotNullConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                      @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, NotNull.class);
         addConstraintMessageTo(constraintAnnotation, message);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, NotNull.class);
     }
 
     @Command(value = "AssertTrue")
     public void addAssertTrueConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                         @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, AssertTrue.class);
         addConstraintMessageTo(constraintAnnotation, message);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, AssertTrue.class);
     }
 
     @Command(value = "AssertFalse")
     public void addAssertFalseConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                          @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, AssertFalse.class);
         addConstraintMessageTo(constraintAnnotation, message);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, AssertFalse.class);
     }
 
     @Command(value = "Min")
     public void addMinConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                  @Option(name = "min", required = true) long min,
                                  @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Min.class);
         addConstraintMessageTo(constraintAnnotation, message);
         constraintAnnotation.setLiteralValue(String.valueOf(min));
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Min.class);
     }
 
     @Command(value = "Max")
     public void addMaxConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                  @Option(name = "max", required = true) long max,
                                  @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Max.class);
         addConstraintMessageTo(constraintAnnotation, message);
         constraintAnnotation.setLiteralValue(String.valueOf(max));
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Max.class);
     }
 
 
     @Command(value = "DecimalMin")
     public void addDecimalMinConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                         @Option(name = "min", required = true) String min,
                                         @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, DecimalMin.class);
         addConstraintMessageTo(constraintAnnotation, message);
         constraintAnnotation.setStringValue(min);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, DecimalMin.class);
     }
 
     @Command(value = "DecimalMax")
     public void addDecimalMaxConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                         @Option(name = "max", required = true) String max,
                                         @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, DecimalMax.class);
         addConstraintMessageTo(constraintAnnotation, message);
         constraintAnnotation.setStringValue(max);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, DecimalMax.class);
     }
 
     @Command(value = "Size")
     public void addSizeConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                   @Option(name = "min") Integer min,
                                   @Option(name = "max") Integer max,
                                   @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Size.class);
         addConstraintMessageTo(constraintAnnotation, message);
 
         if (min != null)
         {
             constraintAnnotation.setLiteralValue("min", String.valueOf(min));
         }
 
         if (max != null)
         {
             constraintAnnotation.setLiteralValue("max", String.valueOf(max));
         }
 
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Size.class);
     }
 
     @Command(value = "Digits")
     public void addDigitsConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                     @Option(name = "integer") int integer,
                                     @Option(name = "fraction") int fraction,
                                     @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Digits.class);
         addConstraintMessageTo(constraintAnnotation, message);
         constraintAnnotation.setLiteralValue("integer", String.valueOf(integer));
         constraintAnnotation.setLiteralValue("fraction", String.valueOf(fraction));
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Digits.class);
     }
 
     @Command(value = "Past")
     public void addPastConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                   @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Past.class);
         addConstraintMessageTo(constraintAnnotation, message);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Past.class);
     }
 
     @Command(value = "Future")
     public void addFutureConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                     @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Future.class);
         addConstraintMessageTo(constraintAnnotation, message);
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Future.class);
     }
 
     //TODO need a closer look to this command
     @Command(value = "Pattern")
     public void addPatternConstraint(@Option(name = "on", completer = PropertyCompleter.class) String property,
                                      @Option(name = "regexp", required = true) String regexp,
                                      @Option(name = "flags") Pattern.Flag[] flags,
                                      @Option(name = "message") String message) throws FileNotFoundException
     {
         final Annotation<JavaClass> constraintAnnotation = addConstraintAnnotation(property, Pattern.class);
         addConstraintMessageTo(constraintAnnotation, message);
         constraintAnnotation.setStringValue("regexp", regexp);
 
         constraintAnnotation.getOrigin().addImport(Pattern.Flag.class);
         final StringBuilder flagsLiteral = new StringBuilder();
         flagsLiteral.append('{');
         if (flags != null)
         {
             int i = 0;
             for (Pattern.Flag oneFlag : flags)
             {
                 flagsLiteral.append(oneFlag);
                 if (i < (flags.length - 1))
                 {
                     flagsLiteral.append(",");
                 }
                 i++;
             }
         }
         flagsLiteral.append('}');
 
         constraintAnnotation.setStringValue("flags", flagsLiteral.toString());
         javaSourceFacet.saveJavaSource(constraintAnnotation.getOrigin());
 
         outputConstraintAdded(property, Pattern.class);
     }
 
     private Annotation<JavaClass> addConstraintAnnotation(String property, Class<? extends java.lang.annotation.Annotation> annotationClass) throws FileNotFoundException
     {
         final Resource<?> current = shell.getCurrentResource();
         if (property != null)
         {
             final JavaClass clazz = getJavaClassFromResource(current);
             if (clazz.hasField(property))
             {
                 return clazz.getField(property).addAnnotation(annotationClass);
             }
             throw new IllegalStateException("The current class has no property named '" + property + "'");
         }
         return addAnnotationTo(current, annotationClass);
     }
 
     private void addConstraintMessageTo(Annotation<JavaClass> annotation, String message)
     {
         if (message != null)
         {
             annotation.setStringValue("message", message);
         }
     }
 
     //TODO a better output will be great
     private void outputConstraintAdded(String property, Class<? extends java.lang.annotation.Annotation> constraintClass)
     {
         final StringBuilder builder = new StringBuilder();
         builder.append(constraintClass.getSimpleName() + " has been added on ");
         builder.append(property != null ? property : shell.getCurrentResource().getName());
         shell.println(builder.toString());
     }
 }
