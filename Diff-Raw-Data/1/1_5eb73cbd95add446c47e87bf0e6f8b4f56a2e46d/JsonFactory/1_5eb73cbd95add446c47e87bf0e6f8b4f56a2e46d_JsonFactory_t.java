 package org.atlasapi.serialization.json;
 
 import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.annotation.PropertyAccessor;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import com.fasterxml.jackson.datatype.guava.GuavaModule;
 import com.metabroadcast.common.intl.Country;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.RelatedLink;
 
 /**
  */
 public class JsonFactory {
 
     public static ObjectMapper makeJsonMapper() {
         ObjectMapper mapper = new ObjectMapper();
         mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, SerializationFeature.WRITE_NULL_MAP_VALUES);
         mapper.registerModule(new AtlasModule());
         mapper.registerModule(new GuavaModule());
         mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.ANY);
         mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
         mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
         mapper.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
         mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
         return mapper;
     }
 
     private static class AtlasModule extends SimpleModule {
 
         public AtlasModule() {
             super("Atlas Module", new com.fasterxml.jackson.core.Version(0, 0, 1, null, null, null));
         }
 
         @Override
         public void setupModule(SetupContext context) {
             super.setupModule(context);
             //
             context.setMixInAnnotations(Object.class, ObjectConfiguration.class);
             //
             context.setMixInAnnotations(Container.class, ContainerConfiguration.class);
             context.setMixInAnnotations(Item.class, ItemConfiguration.class);
             context.setMixInAnnotations(Broadcast.class, BroadcastConfiguration.class);
             context.setMixInAnnotations(Country.class, CountryConfiguration.class);
             context.setMixInAnnotations(RelatedLink.class, RelatedLinkConfiguration.class);
             context.setMixInAnnotations(ParentRef.class, ParentRefConfiguration.class);
             context.setMixInAnnotations(ChildRef.class, ChildRefConfiguration.class);
         }
     }
 }
