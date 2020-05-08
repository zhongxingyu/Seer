 /*
  * Copyright (c) 2006-2013 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.util.annotations.factory;
 
 import com.dmdirc.util.annotations.util.SourceFileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.Modifier;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.tools.Diagnostic;
 
 /**
  * Processor for the {@link Factory} annotation.
  */
 @SupportedAnnotationTypes({
     "com.dmdirc.util.annotations.factory.Factory",
     "com.dmdirc.util.annotations.factory.Unbound",
 })
 @SupportedSourceVersion(SourceVersion.RELEASE_7)
 public class FactoryProcessor extends AbstractProcessor {
 
     /** {@inheritDoc} */
     @Override
     public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnv) {
         for (Element type : roundEnv.getElementsAnnotatedWith(Factory.class)) {
             Factory annotation = type.getAnnotation(Factory.class);
 
             if (annotation == null) {
                 continue;
             }
 
             final TypeElement typeElement = (TypeElement) type;
             final PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
 
             final List<Parameter> boundParameters = new ArrayList<>();
             final List<List<Parameter>> allParameters = new ArrayList<>();
 
             for (Element child : type.getEnclosedElements()) {
                 if (child.getKind() == ElementKind.CONSTRUCTOR) {
                     final List<Parameter> params = new ArrayList<>();
 
                     ExecutableElement ctor = (ExecutableElement) child;
                     for (VariableElement element : ctor.getParameters()) {
                         final Parameter param = new Parameter(
                                 element.asType().toString(),
                                 element.getSimpleName().toString(),
                                 getAnnotations(element));
                         if (element.getAnnotation(Unbound.class) == null) {
                             if (!boundParameters.contains(param)) {
                                 boundParameters.add(param);
                             }
                         }
 
                         params.add(param);
                     }
 
                     allParameters.add(params);
                 }
             }
 
             writeFactory(
                     packageElement.getQualifiedName().toString(),
                     annotation.name().isEmpty() ?
                             typeElement.getSimpleName() + "Factory" :
                             annotation.name(),
                     typeElement.getSimpleName().toString(),
                     annotation,
                     boundParameters,
                     allParameters,
                     type);
         }
 
         return false;
     }
 
     /**
      * Writes all of the given parameters as method parameters.
      *
      * @param annotation The annotation configuring the factory. {@code null} to use defaults.
      * @param writer The writer to write to.
      * @param parameters The parameters to be written.
      * @throws IOException If the operation failed.
      */
     private void writeMethodParameters(
             final Factory annotation,
             final SourceFileWriter writer,
             final List<Parameter> parameters) throws IOException {
         for (Parameter param : parameters) {
             writer.writeMethodParameter(
                     param.getAnnotations(),
                    annotation != null && annotation.providers() ?
                            maybeWrapProvider(annotation, param.getType()) :
                            param.getType(),
                     param.getName(),
                     Modifier.FINAL);
         }
     }
 
     /**
      * Gets all the annotations (except the ones declared by this library) of the given element
      * as a string.
      *
      * @param element The element to retrieve annotations for.
      * @return A space-separated string of all annotations and their values.
      */
     private String getAnnotations(final Element element) {
         final StringBuilder builder = new StringBuilder();
 
         for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
             if (mirror.getAnnotationType().toString().startsWith("com.dmdirc.util.annotations")) {
                 continue;
             }
 
             if (builder.length() > 0) {
                 builder.append(' ');
             }
 
             builder.append(mirror);
         }
 
         return builder.toString();
     }
 
     /**
      * Writes out a factory class as a source file.
      *
      * @param packageName The package to put the factory in.
      * @param factoryName The simple name of the factory.
      * @param typeName The simple name of the class being built.
      * @param annotation The annotation configuring the factory.
      * @param boundParameters The parameters bound by the factory (required by the constructor).
      * @param allParameters A list of parameters taken by each constructor.
      * @param elements The element(s) responsible for the file being written.
      */
     private void writeFactory(final String packageName, final String factoryName,
             final String typeName, final Factory annotation, final List<Parameter> boundParameters,
             final List<List<Parameter>> allParameters, final Element... elements) {
         try (SourceFileWriter writer = new SourceFileWriter(processingEnv.getFiler(),
                 packageName + (packageName.isEmpty() ? "" : ".") + factoryName, elements)) {
             final String methodName = "get" + typeName;
 
             writer.writePackageDeclaration(packageName)
                     .writeAnnotationIf("@javax.inject.Singleton", annotation.singleton())
                     .writeClassDeclaration(factoryName, getClass(), annotation.modifiers());
 
             // All the fields we need
             for (Parameter boundParam : boundParameters) {
                 writer.writeField(
                        maybeWrapProvider(annotation, boundParam.getType()),
                         boundParam.getName(),
                         Modifier.PRIVATE, Modifier.FINAL);
             }
 
             // Constructor declaration
             writer.writeAnnotationIf("@javax.inject.Inject", annotation.inject())
                     .writeConstructorDeclarationStart(factoryName, Modifier.PUBLIC);
             writeMethodParameters(annotation, writer, boundParameters);
             writer.writeMethodDeclarationEnd();
 
             // Assign the values to fields
             for (Parameter boundParam : boundParameters) {
                 writer.writeFieldAssignment(boundParam.getName(), boundParam.getName());
             }
 
             // End of constructor
             writer.writeBlockEnd();
 
             // Write each factory method out in turn
             for (List<Parameter> params : allParameters) {
                 final List<Parameter> unbound = new ArrayList<>(params);
                 unbound.removeAll(boundParameters);
 
                 final String[] parameters = new String[params.size()];
                 for (int i = 0; i < parameters.length; i++) {
                     if (annotation.providers()
                             && !params.get(i).getType().startsWith("javax.inject.Provider")
                            && boundParameters.contains(params.get(i))) {
                         parameters[i] = params.get(i).getName() + ".get()";
                     } else {
                         parameters[i] = params.get(i).getName();
                     }
                 }
 
                 writer.writeMethodDeclarationStart(typeName, methodName, annotation.methodModifiers());
                 writeMethodParameters(null, writer, unbound);
                 writer.writeMethodDeclarationEnd()
                         .writeReturnStart()
                         .writeNewInstance(typeName, parameters)
                         .writeStatementEnd()
                         .writeBlockEnd();
             }
 
             // Done!
             writer.writeBlockEnd();
         } catch (IOException ex) {
             processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                     "Unable to write factory file: " + ex.getMessage());
         }
     }
 
     /**
      * Writes the given type in a Provider&lt;&gt;, if the factory is configured to use them,
     * and the type is not already a provider.
      *
      * @param annotation The annotation configuring the factory.
     * @param type The type to possibly wrap.
      * @return The possibly-wrapped type.
      */
    private String maybeWrapProvider(final Factory annotation, final String type) {
        if (annotation.providers() && !type.startsWith("javax.inject.Provider")) {
            return "javax.inject.Provider<" + type + ">";
         } else {
            return type;
         }
     }
 
 }
