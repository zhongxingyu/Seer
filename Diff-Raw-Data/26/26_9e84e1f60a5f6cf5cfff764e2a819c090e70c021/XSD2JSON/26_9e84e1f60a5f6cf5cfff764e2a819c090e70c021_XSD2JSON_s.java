 package org.nuxeo.xsd.parser;
 
 import java.io.File;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
 import org.nuxeo.ecm.core.schema.XSDLoader;
 import org.nuxeo.ecm.core.schema.types.ComplexType;
 import org.nuxeo.ecm.core.schema.types.Field;
 import org.nuxeo.ecm.core.schema.types.ListType;
 import org.nuxeo.ecm.core.schema.types.Schema;
 import org.nuxeo.runtime.RuntimeService;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.util.SimpleRuntime;
 
 public class XSD2JSON {
 
     public static String asJSON(String name, String prefix, File xsdFile)
             throws Exception {
         Schema schema = loadSchema(name, prefix, xsdFile);
         if (schema != null) {
             return asJSON(schema);
         }
         return null;
     }
 
     public static String asJSON(String name, String prefix,
             String xsdFileLocation) throws Exception {
         return asJSON(name, prefix, new File(xsdFileLocation));
     }
 
     protected static Schema loadSchema(String name, String prefix, File xsdFile)
             throws Exception {
         if (!Framework.isInitialized()) {
          RuntimeService runtime = new SimpleRuntime();
          System.setProperty("nuxeo.home", System.getProperty("java.io.tmpdir"));
          Framework.initialize(runtime);
         }
         SchemaManagerImpl schemaManager = new SchemaManagerImpl();
         XSDLoader loader = new XSDLoader(schemaManager);
         Schema schema = loader.loadSchema(name, prefix, xsdFile, true);
         return schema;
     }
 
     public static String asJSON(Schema schema) throws JSONException {
 
        JSONObject root = new JSONObject();
        root.put("@prefix", schema.getNamespace().prefix);
         for (Field field : schema.getFields()) {
            addField(root, field);
         }
        return schema.getName() + root.toString(1);
     }
 
     protected static void addField(JSONObject object, Field field)
             throws JSONException {
         if (!field.getType().isComplexType()) {
             if (field.getType().isListType()) {
                 ListType lt = (ListType) field.getType();
                 if (lt.getFieldType().isComplexType()) {
                     JSONObject cplx = new JSONObject();
                     cplx.put("type", "complex[]");
                     JSONObject fields = buildComplexFields(lt.getField());
                     cplx.put("fields", fields);
                     object.put(field.getName().getLocalName(), cplx);
                 } else {
                     object.put(field.getName().getLocalName(),
                             lt.getFieldType().getName() + "[]");
                 }
             } else {
                 object.put(field.getName().getLocalName(),
                         field.getType().getName());
             }
         } else {
             JSONObject cplx = new JSONObject();
             cplx.put("type", "complex");
             JSONObject fields = buildComplexFields(field);
             cplx.put("fields", fields);
 
             object.put(field.getName().getLocalName(), cplx);
         }
     }
 
     protected static JSONObject buildComplexFields(Field field)
             throws JSONException {
         JSONObject fields = new JSONObject();
         ComplexType cplXType = (ComplexType) field.getType();
         for (Field subField : cplXType.getFields()) {
             addField(fields, subField);
         }
         return fields;
     }
 }
