 package de.tuberlin.dima.presslufthammer.data;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class SchemaNode {
     public enum Type {
         RECORD, PRIMITIVE
     }
 
     public enum Modifier {
         REQUIRED, OPTIONAL, REPEATED
     }
 
     // only valid for records (groups)
     private List<SchemaNode> fieldList = null;
     private Map<String, SchemaNode> fieldMap = null;
 
     // valid if type is primitive
     private PrimitiveType primitiveType = null;
 
     private String name;
     private Type type;
 
     private Modifier modifier = Modifier.REQUIRED;
 
     private SchemaNode parent = null;
 
     private SchemaNode(Type nodeType) {
         type = nodeType;
     }
 
     public static SchemaNode createPrimitive(String name, PrimitiveType type) {
         SchemaNode newSchema = new SchemaNode(Type.PRIMITIVE);
         newSchema.name = name;
         newSchema.primitiveType = type;
         return newSchema;
     }
 
     public static SchemaNode createRecord(String name) {
         SchemaNode newSchema = new SchemaNode(Type.RECORD);
         newSchema.name = name;
         newSchema.fieldList = Lists.newLinkedList();
         newSchema.fieldMap = Maps.newHashMap();
         return newSchema;
     }
 
     public int getRepetition() {
         int parentRepetition = 0;
         if (this.parent != null) {
             parentRepetition = parent.getRepetition();
         }
         if (isRepeated()) {
             return parentRepetition + 1;
         } else {
             return parentRepetition;
         }
 
     }
 
     public int getMaxDefinition() {
         int parentMaxDefinition = 0;
         if (this.parent != null) {
             parentMaxDefinition = parent.getMaxDefinition();
         }
         if (isOptional() || isRepeated()) {
             return parentMaxDefinition + 1;
         } else {
             return parentMaxDefinition;
         }
     }
 
     public String getName() {
         return name;
     }
 
     public Type getType() {
         return type;
     }
 
     public boolean isPrimitive() {
         return type == Type.PRIMITIVE;
     }
 
     public PrimitiveType getPrimitiveType() {
         if (type != Type.PRIMITIVE) {
             throw new RuntimeException("SchemaTree " + name
                     + " is not a primitive type.");
         }
         return primitiveType;
     }
 
     public boolean isRequired() {
         return modifier == Modifier.REQUIRED;
     }
 
     public void setRequired() {
         modifier = Modifier.REQUIRED;
     }
 
     public boolean isOptional() {
         return modifier == Modifier.OPTIONAL;
     }
 
     public void setOptional() {
         modifier = Modifier.OPTIONAL;
     }
 
     public boolean isRepeated() {
         return modifier == Modifier.REPEATED;
     }
 
     public void setRepeated() {
         modifier = Modifier.REPEATED;
     }
 
     public Modifier getModifier() {
         return modifier;
     }
 
     public boolean isRecord() {
         return type == Type.RECORD;
     }
 
     public List<SchemaNode> getFieldList() {
         if (type != Type.RECORD) {
             throw new RuntimeException("SchemaTree " + name
                     + " is not a record type.");
         }
         return fieldList;
     }
 
     public void addField(SchemaNode newField) {
         addField(newField, newField.getName());
     }
 
     public void addField(SchemaNode newField, String name) {
         if (type != Type.RECORD) {
             throw new RuntimeException("Adding a field to SchemaTree " + name
                     + " is not possible because it is not a record.");
         }
         if (fieldMap.containsKey(name)) {
             throw new RuntimeException("Duplicate field name in SchemaTree "
                     + name + ".");
         }
         fieldList.add(newField);
         fieldMap.put(name, newField);
         newField.parent = this;
     }
 
     public boolean hasField(String fieldName) {
         return fieldMap.containsKey(fieldName);
     }
 
     public SchemaNode getField(String fieldName) {
         return fieldMap.get(fieldName);
     }
 
     public boolean hasParent() {
         return parent != null;
     }
 
     public SchemaNode getParent() {
         return parent;
     }
 
     public boolean equals(Object other) {
         if (!(other instanceof SchemaNode)) {
             return false;
         }
 
         SchemaNode otherSchema = (SchemaNode) other;
 
         if (getType() != otherSchema.getType()) {
             return false;
         }
 
         if (getModifier() != otherSchema.getModifier()) {
             return false;
         }
 
         if (isPrimitive()
                 && !(this.primitiveType.equals(otherSchema.primitiveType))) {
             return false;
         }
 
         if (isRecord() && fieldList.size() != otherSchema.fieldList.size()) {
             return false;
         }
 
         if (isRecord()) {
             for (int i = 0; i < fieldList.size(); ++i) {
                 if (!fieldList.get(i).equals(otherSchema.fieldList.get(i))) {
                     return false;
                 }
             }
         }

        if (!getName().equals(otherSchema.getName())) {
            return false;
        }
         return true;
     }
 
     public int hashCode() {
         return Objects.hashCode(fieldMap, primitiveType, name, type, modifier);
     }
 
     public String getQualifiedName() {
         if (hasParent()) {
             return parent.getQualifiedName() + "." + name;
         } else {
             return name;
         }
     }
 
     public String toString() {
         return "package " + getName() + ";\n" + toStringRecursive(0);
     }
 
     private String toStringRecursive(int indentation) {
         StringBuffer identBuffer = new StringBuffer();
         for (int i = 0; i < indentation; i++) {
             identBuffer.append("      ");
         }
         indentation++;
         String indent = identBuffer.toString();
 
         String result = "";
         switch (type) {
         case PRIMITIVE:
             switch (modifier) {
             case REQUIRED:
                 result += "required ";
                 break;
             case OPTIONAL:
                 result += "optional ";
                 break;
             case REPEATED:
                 result += "repeated ";
                 break;
             }
             result += primitiveType.toString() + " " + name;
             break;
         case RECORD:
             result += "message " + name + " {\n";
             List<String> childStrings = Lists.newLinkedList();
             int count = 1;
             for (SchemaNode schema : fieldList) {
                 childStrings.add(fieldMap.get(schema.getName())
                         .toStringRecursive(indentation) + " = " + count + ";");
                 ++count;
             }
             Joiner join = Joiner.on('\n');
             result += join.join(childStrings);
             result += " }";
             if (parent != null) {
                 result += "\n" + indent;
                 switch (modifier) {
                 case REQUIRED:
                     result += "required ";
                     break;
                 case OPTIONAL:
                     result += "optional ";
                     break;
                 case REPEATED:
                     result += "repeated ";
                     break;
                 }
                 result += getName() + " " + getName();
             }
             break;
         default:
             throw new RuntimeException("Unexpected node type " + type);
         }
 
         return indent + result;
     }
 }
