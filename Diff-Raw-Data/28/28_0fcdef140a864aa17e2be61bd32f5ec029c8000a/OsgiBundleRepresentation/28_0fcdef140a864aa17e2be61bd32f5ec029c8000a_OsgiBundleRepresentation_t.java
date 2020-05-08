 package edu.northwestern.bioinformatics.studycalendar.restlets.representations;
 
 import edu.northwestern.bioinformatics.studycalendar.restlets.OsgiBundleState;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeInformation;
 import org.osgi.service.metatype.MetaTypeService;
 import org.osgi.service.metatype.ObjectClassDefinition;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Dictionary;
 import java.util.Enumeration;
 
import static edu.northwestern.bioinformatics.studycalendar.restlets.representations.JacksonTools.nullSafeWriteStringField;

 /**
  * Representation for one or many OSGi bundles.
  *
  * @author Rhett Sutphin
  */
 public class OsgiBundleRepresentation extends StreamingJsonRepresentation {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private Bundle[] bundles;
     private boolean isCollection;
     private MetaTypeService metaTypeService;
 
     public static OsgiBundleRepresentation create(Bundle bundle, MetaTypeService metaTypeService) {
         return new OsgiBundleRepresentation(new Bundle[] { bundle }, false, metaTypeService);
     }
 
     public static OsgiBundleRepresentation create(Bundle[] list, MetaTypeService metaTypeService) {
         return new OsgiBundleRepresentation(list, true, metaTypeService);
     }
 
     private OsgiBundleRepresentation(Bundle[] bundles, boolean isCollection, MetaTypeService metaTypeService) {
         super();
         this.bundles = bundles;
         Arrays.sort(this.bundles, new Comparator<Bundle>() {
             public int compare(Bundle b1, Bundle b2) {
                 return (int) (b1.getBundleId() - b2.getBundleId());
             }
         });
         this.isCollection = isCollection;
         this.metaTypeService = metaTypeService;
     }
 
     @Override
     public void generate(JsonGenerator generator) throws IOException, JsonGenerationException {
         if (isCollection) generator.writeStartArray();
         for (Bundle bundle : bundles) {
             writeBundle(generator, bundle);
         }
         if (isCollection) generator.writeEndArray();
     }
 
     @SuppressWarnings({ "unchecked" })
     private void writeBundle(JsonGenerator g, Bundle bundle) throws IOException {
         log.debug("Serializing {}", bundle.getSymbolicName());
 
         g.writeStartObject();
         g.writeNumberField("id", bundle.getBundleId());
         g.writeStringField("symbolic_name", bundle.getSymbolicName());
         g.writeStringField("state", OsgiBundleState.valueOfConstant(bundle.getState()).name());
 
         Dictionary<String, Object> headers = bundle.getHeaders();
         Enumeration<String> headerNames = headers.keys();
         while (headerNames.hasMoreElements()) {
             String name = headerNames.nextElement();
             if (name.startsWith("Bundle-")) {
                 String key = name.substring(7).toLowerCase();
                 Object value = headers.get(name);
                 if (value != null) g.writeStringField(key, value.toString());
             }
         }
 
         ServiceReference[] refs = bundle.getRegisteredServices();
         if (refs != null && refs.length > 0) {
             MetaTypeInformation metaTypes = metaTypeService.getMetaTypeInformation(bundle);
             g.writeArrayFieldStart("services");
             for (ServiceReference ref : refs) {
                 log.debug("- serializing service {} with metatypes {}", ref, metaTypes);
                 writeService(g, ref, metaTypes);
             }
             g.writeEndArray();
         } else {
             log.debug("- no registered services");
         }
 
         g.writeEndObject();
     }
 
     private void writeService(JsonGenerator g, ServiceReference ref, MetaTypeInformation metaTypes) throws IOException {
         g.writeStartObject();
 
         String[] interfaces = (String[]) ref.getProperty(Constants.OBJECTCLASS);
         g.writeArrayFieldStart("interfaces");
         for (String anInterface : interfaces) {
             g.writeString(anInterface);
         }
         g.writeEndArray();
 
         OsgiRepresentationHelper.writeServiceProperties(g, "properties", ref);
 
         if (metaTypes != null) {
             String pid = (String) ref.getProperty(Constants.SERVICE_PID);
             try {
                 ObjectClassDefinition metainfo = metaTypes.getObjectClassDefinition(pid, null);
                 log.debug("- OCD for {} is {}", pid, metainfo);
                 writeMetatype(g, metainfo);
             } catch (IllegalArgumentException iae) {
                 // there isn't metadata for this service
             }
         }
 
         g.writeEndObject();
     }
 
     private void writeMetatype(JsonGenerator g, ObjectClassDefinition type) throws IOException {
         if (type == null) return;
         g.writeObjectFieldStart("metatype");
         nullSafeWriteStringField(g, "id", type.getID());
         nullSafeWriteStringField(g, "name", type.getName());
         nullSafeWriteStringField(g, "description", type.getDescription());
         writeMetatypeAttributes(g, type.getAttributeDefinitions(ObjectClassDefinition.ALL));
         g.writeEndObject();
     }
 
     private void writeMetatypeAttributes(JsonGenerator g, AttributeDefinition[] attributeDefinitions) throws IOException {
         if (attributeDefinitions == null || attributeDefinitions.length == 0) return;
         g.writeArrayFieldStart("attributes");
         for (AttributeDefinition definition : attributeDefinitions) {
             writeMetatypeAttribute(g, definition);
         }
         g.writeEndArray();
     }
 
     private void writeMetatypeAttribute(JsonGenerator g, AttributeDefinition definition) throws IOException {
         g.writeStartObject();
 
         nullSafeWriteStringField(g, "id", definition.getID());
         nullSafeWriteStringField(g, "name", definition.getName());
         nullSafeWriteStringField(g, "description", definition.getDescription());
         g.writeStringField("type", attributeTypeName(definition.getType()));
 
         String[] optionLabels = definition.getOptionLabels();
         String[] optionValues = definition.getOptionValues();
        int optionCount = Math.min(nullSafeLength(optionLabels), nullSafeLength(optionValues));
         if (optionCount > 0) {
             g.writeArrayFieldStart("options");
             for (int i = 0 ; i < optionCount ; i++) {
                 g.writeStartObject();
                 nullSafeWriteStringField(g, "label", optionLabels[i]);
                 nullSafeWriteStringField(g, "value", optionValues[i]);
                 g.writeEndObject();
             }
             g.writeEndArray();
         }
 
         g.writeEndObject();
     }
 
    private int nullSafeLength(String[] array) {
        return array == null ? 0 : array.length;
    }

     private String attributeTypeName(int code) {
         switch(code) {
             case AttributeDefinition.BOOLEAN:   return "boolean";
             case AttributeDefinition.BYTE:      return "byte";
             case AttributeDefinition.CHARACTER: return "character";
             case AttributeDefinition.DOUBLE:    return "double";
             case AttributeDefinition.FLOAT:     return "float";
             case AttributeDefinition.INTEGER:   return "integer";
             case AttributeDefinition.LONG:      return "long";
             case AttributeDefinition.SHORT:     return "short";
             case AttributeDefinition.STRING:    return "string";
         }
         return "unknown";
     }
 
 }
