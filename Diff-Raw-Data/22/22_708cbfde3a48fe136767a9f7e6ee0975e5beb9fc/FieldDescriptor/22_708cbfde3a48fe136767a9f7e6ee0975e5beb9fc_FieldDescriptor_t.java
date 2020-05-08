 package com.aol.wsdl;
 
 import org.apache.axis.encoding.SerializationContext;
 import org.apache.axis.wsdl.symbolTable.Parameter;
 
 import javax.xml.namespace.QName;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class FieldDescriptor extends ArrayList<FieldDescriptor> {
     private Class javaType;
     private String name;
     private int depth = 0;
     private boolean primitive;
     private Parameter parameter;
     private String value;
     private String formName;
     private QName qname;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public boolean isPrimitive() {
         return primitive;
     }
 
     public int getDepth() {
         return depth;
     }
 
     public void setDepth(int depth) {
         this.depth = depth;
     }
 
     public void setPrimitive(boolean primitive) {
         this.primitive = primitive;
     }
 
     public void setParameter(Parameter parameter) {
         this.parameter = parameter;
     }
 
     public Parameter getParameter() {
         return parameter;
     }
 
     public String getValue() {
         return value == null ? "" : value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public String getFormName() {
         return formName;
     }
 
     public void setFormName(String formName) {
         this.formName = formName;
     }
 
     public void setQName(QName qname) {
         this.qname = qname;
     }
 
     public QName getQname() {
         return qname;
     }
 
     public Class getJavaType() {
         return javaType;
     }
 
     public void setJavaType(String javaTypeName) throws ClassNotFoundException {
         javaTypeName = new String(new char[]{javaTypeName.charAt(0)}).toUpperCase() + javaTypeName.substring(1);
        if ("Int".equals(javaTypeName)) {
            javaTypeName = "Integer";
        } else if ("Char".equals(javaTypeName)) {
            javaTypeName = "Character";
        }
         this.javaType = Class.forName("java.lang." + javaTypeName);
     }
 
     public void serialize(SerializationContext context) throws IOException {
         if (isPrimitive()) {
             context.serialize(getQname(), null, "".equals(getValue()) ? null : getValue(), context.getQNameForClass(javaType), javaType);
         } else {
             context.startElement(getQname(), null);
 
             for (FieldDescriptor child : this) {
                 context.setItemQName(null);
                 child.serialize(context);
             }
 
             context.endElement();
         }
     }
 }
