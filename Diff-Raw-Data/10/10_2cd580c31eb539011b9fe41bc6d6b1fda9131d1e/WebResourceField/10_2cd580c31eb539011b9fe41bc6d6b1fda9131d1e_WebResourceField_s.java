 package com.github.t1.webresource;
 
 import javax.lang.model.element.*;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeMirror;
 
 class WebResourceField {
     protected static Element findField(TypeElement classElement, String annotationTypeName) {
         for (Element enclosedElement : classElement.getEnclosedElements()) {
             if (ElementKind.FIELD != enclosedElement.getKind())
                 continue;
             if (isAnnotated(enclosedElement, annotationTypeName)) {
                 return enclosedElement;
             }
         }
         TypeMirror superclass = classElement.getSuperclass();
        if (superclass != null) {
            Element superElement = ((DeclaredType) superclass).asElement();
            return findField((TypeElement) superElement, annotationTypeName);
         }
         return null;
     }
 
     private static boolean isAnnotated(Element element, String annotationName) {
         for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
             if (annotationName.equals(annotation.getAnnotationType().toString())) {
                 return true;
             }
         }
         return false;
     }
 
     protected final String fullyQualifiedTypeName;
     protected final String name;
 
     public WebResourceField(Element field) {
         this.fullyQualifiedTypeName = field.asType().toString();
         this.name = field.getSimpleName().toString();
     }
 
     public boolean nullable() {
         return fullyQualifiedTypeName.contains(".");
     }
 
     public String packageImport() {
         if (!nullable() || fullyQualifiedTypeName.startsWith("java.lang."))
             return null;
         return fullyQualifiedTypeName;
     }
 
     public String type() {
         int index = fullyQualifiedTypeName.lastIndexOf('.');
         return index < 0 ? fullyQualifiedTypeName : fullyQualifiedTypeName.substring(index + 1);
     }
 
     public String name() {
         return name;
     }
 
     private String uppercaps() {
         if (name == null || name.length() == 0)
             return name;
         if (name.length() == 1)
             return name.toUpperCase();
         return Character.toUpperCase(name.charAt(0)) + name.substring(1);
     }
 
     public String getter() {
         return "get" + uppercaps();
     }
 
     public String setter() {
         return "set" + uppercaps();
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((fullyQualifiedTypeName == null) ? 0 : fullyQualifiedTypeName.hashCode());
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         WebResourceField other = (WebResourceField) obj;
         if (fullyQualifiedTypeName == null) {
             if (other.fullyQualifiedTypeName != null)
                 return false;
         } else if (!fullyQualifiedTypeName.equals(other.fullyQualifiedTypeName))
             return false;
         if (name == null) {
             if (other.name != null)
                 return false;
         } else if (!name.equals(other.name))
             return false;
         return true;
     }
 }
