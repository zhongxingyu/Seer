 package com.xebialabs.rest.doclet;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.google.common.base.Strings;
 import com.sun.javadoc.*;
 import com.sun.javadoc.AnnotationDesc.ElementValuePair;
 import com.xebialabs.commons.html.Element;
 
 public class RestServiceWriter extends RestdocWriter {
 
     private final ClassDoc service;
     private final String path;
     private final String defaultConsumes;
     private final String defaultProduces;
 
     public RestServiceWriter(PrintWriter writer, ClassDoc service) {
         super(writer);
         this.service = service;
         this.path = getPath(service);
         this.defaultConsumes = getConsumes(service);
         this.defaultProduces = getProduces(service);
     }
 
     //
     // Page structure
     //
 
     public void writeRestService() {
         writeHeader();
         writeIndex();
         writeDetails();
     }
 
     //
     // Html rendering
     //
 
     private void writeHeader() {
         h1(service.name()).write();
         p(asText(service.inlineTags())).write();
 
     }
 
     private void writeIndex() {
         table().cssClass("parameter-table").writeOpen();
         for (MethodDoc method : getRestMethods(service)) {
             String httpMethod = getHttpMethod(method);
             String resource = path + "/" + getPath(method);
             row(httpMethod, link("#" + method.qualifiedName(), resource), asText(method.firstSentenceTags())).write();
         }
         table().writeClose();
     }
 
     private void writeDetails() {
         hr().write();
         for (MethodDoc method : getRestMethods(service)) {
             writeMethodDetail(method, path);
         }
     }
 
     private void writeMethodDetail(MethodDoc method, String path) {
         String httpMethod = getHttpMethod(method);
         String resource = path + "/" + getPath(method);
 
         // Method signature and comments
         anchor(method.qualifiedName()).write();
         h2(httpMethod, " ", resource).cssClass("resource-header").write();
         div(asText(method.inlineTags())).write();
 
         // Permissions
         writePermissions(method);
 
         // Parameters
         writeParameters(method);
 
         // Return type
         writeReturnType(method);
 
         // See tags
         for (Tag seeTag : method.tags("see")) {
             definitionList("See", asText(seeTag.inlineTags())).write();
         }
     }
 
     private void writePermissions(MethodDoc method) {
         Tag[] permissions = method.tags("permission");
         if (permissions.length > 0) {
             Element dt = definitionList("Permissions");
             for (Tag permission : permissions) {
                 String rest = restOfSentence(permission);
                 if (!Strings.isNullOrEmpty(rest)) {
                     rest = " - " + rest;
                 }
                 dt.add(element("dd", code(firstWord(permission)), rest));
             }
             dt.write();
         }
     }
 
     private void writeReturnType(MethodDoc method) {
 
         if ("void".equals(method.returnType().simpleTypeName())) {
             definitionList(
                     "Response body",
                     italic("Empty")
             ).write();
         } else {
             definitionList(
                     "Response body",
                    renderType(method.returnType()) + " - " + asText(method.tags("return")[0].inlineTags()),
                     "Content type: " + getMethodProduces(method)
             ).write();
         }
     }
 
     private void writeParameters(MethodDoc method) {
         if (method.paramTags().length <= 0) {
             return;
         }
 
         Element table = table().cssClass("parameter-table");
         for (ParamTag param : method.paramTags()) {
             ParameterInfo info = getParameterInfo(method, param);
             if (info == null) {
                 System.out.println("Warning: No actual parameter for @param " + param.parameterName() + " on " + method);
                 continue;
             }
             table.add(row(italic(info.kind), info.name, renderType(info.type), asText(param.inlineTags())));
         }
         definitionList("Parameters", table).write();
     }
 
     //
     // Inspection
     //
 
     private static List<MethodDoc> getRestMethods(ClassDoc service) {
         List<MethodDoc> methods = new ArrayList<MethodDoc>();
 
         for (MethodDoc method : service.methods()) {
             if (isRestMethod(method)) {
                 methods.add(method);
             }
         }
 
         Collections.sort(methods, new MethodComparator());
 
         return methods;
     }
 
     private static boolean isRestMethod(MethodDoc method) {
         for (AnnotationDesc annotation : method.annotations()) {
             if (annotation.annotationType().qualifiedName().startsWith("javax.ws.rs")) {
                 return true;
             }
         }
         return false;
     }
 
     private static String getPath(ProgramElementDoc element) {
         return getAnnotationValue(element, "javax.ws.rs.Path");
     }
 
     private static String getConsumes(ProgramElementDoc element) {
         return getAnnotationValue(element, "javax.ws.rs.Consumes").replace("\"", "").replaceAll("[\"\\]\\[]", "");
     }
 
     private String getMethodConsumes(MethodDoc method) {
         String consumes = getConsumes(method);
         if (Strings.isNullOrEmpty(consumes)) {
             return defaultConsumes;
         }
         return consumes;
     }
 
     private static String getProduces(ProgramElementDoc element) {
         return getAnnotationValue(element, "javax.ws.rs.Produces").replaceAll("[\"\\]\\[]", "");
     }
 
     private String getMethodProduces(MethodDoc method) {
         String produces = getProduces(method);
         if (Strings.isNullOrEmpty(produces)) {
             return defaultProduces;
         }
         return produces;
     }
 
     private static String getHttpMethod(ProgramElementDoc element) {
         for (AnnotationDesc annotation : element.annotations()) {
             if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.GET")) {
                 return "GET";
             }
             if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.POST")) {
                 return "POST";
             }
             if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.PUT")) {
                 return "PUT";
             }
             if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.DELETE")) {
                 return "DELETE";
             }
             if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.HEAD")) {
                 return "HEAD";
             }
         }
         return "?";
     }
 
     public static String getAnnotationValue(ProgramElementDoc element, String annotationType) {
         return getAnnotationValue(getAnnotation(element, annotationType));
     }
 
     private static String getAnnotationValue(AnnotationDesc annotation) {
         if (annotation == null) {
             return "";
         }
 
         for (ElementValuePair item : annotation.elementValues()) {
             Object value = item.value().value();
             if (value instanceof Object[]) {
                 return Arrays.asList((Object[]) value).toString();
             }
             return value.toString();
         }
         return "";
     }
 
     private static AnnotationDesc getAnnotation(ProgramElementDoc element, String type) {
         for (AnnotationDesc annotation : element.annotations()) {
             if (annotation.annotationType().qualifiedName().equals(type)) {
                 return annotation;
             }
         }
         return null;
     }
 
     private ParameterInfo getParameterInfo(MethodDoc method, ParamTag tag) {
 
         String name = tag.parameterName();
         for (Parameter param : method.parameters()) {
             if (!param.name().equals(name)) {
                 continue;
             }
 
             Type type = param.type();
             for (AnnotationDesc annotation : param.annotations()) {
                 if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.PathParam")) {
                     return new ParameterInfo(getAnnotationValue(annotation), "Path", type);
                 }
                 if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.QueryParam")) {
                     return new ParameterInfo(getAnnotationValue(annotation), "Query", type);
                 }
                 if (annotation.annotationType().qualifiedName().equals("javax.ws.rs.HeaderParam")) {
                     return new ParameterInfo(getAnnotationValue(annotation), "Header", type);
                 }
                 if (annotation.annotationType().qualifiedName().equals("org.jboss.resteasy.annotations.providers.multipart.MultipartForm")) {
                     return new ParameterInfo(getAnnotationValue(annotation), "Multipart", type);
                 }
             }
             return new ParameterInfo(getMethodConsumes(method), "Request&nbsp;body", type);
         }
 
         return null;
     }
 
     private static class ParameterInfo {
         final String name;
         final String kind;
         final Type type;
 
         ParameterInfo(String name, String kind, Type type) {
             this.name = name;
             this.kind = kind;
             this.type = type;
         }
     }
 
     private static class MethodComparator implements Comparator<MethodDoc> {
 
         @Override
         public int compare(MethodDoc method, MethodDoc anotherMethod) {
             return getPath(method).compareTo(getPath(anotherMethod));
         }
     }
 }
