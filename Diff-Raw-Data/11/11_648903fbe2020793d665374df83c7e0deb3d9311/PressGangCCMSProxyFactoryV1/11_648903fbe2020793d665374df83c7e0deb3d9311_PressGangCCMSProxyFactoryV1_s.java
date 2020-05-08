 package org.jboss.pressgang.ccms.rest.v1.client;
 
 import java.net.URI;
 
 import org.codehaus.jackson.map.DeserializationConfig.Feature;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTBlobConstantV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTCategoryV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTFilterV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTImageV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTLogDetailsV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTProjectV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTPropertyTagV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTRoleV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTStringConstantV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTTagV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTTopicV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTTranslatedTopicV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.RESTUserV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTBlobConstantCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTCategoryCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTFilterCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTImageCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTProjectCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTPropertyTagCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTRoleCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTStringConstantCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTTagCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTTopicCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTTranslatedTopicCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.converter.collection.RESTUserCollectionV1JSONConverter;
 import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTInterfaceAdvancedV1;
 import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTInterfaceV1;
 import org.jboss.resteasy.client.ClientRequestFactory;
 import org.jboss.resteasy.client.ProxyFactory;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 
 public class PressGangCCMSProxyFactoryV1 {
     private static final ResteasyProviderFactory providerFactory = ResteasyProviderFactory.getInstance();
     static {
         // Configure the Jackson Provider to ignore Unknown Fields
         final ResteasyJacksonProvider jacksonProvider = new ResteasyJacksonProvider();
         jacksonProvider.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         providerFactory.registerProviderInstance(jacksonProvider);
 
         providerFactory.addStringConverter(RESTLogDetailsV1JSONConverter.class);
         providerFactory.addStringConverter(RESTBlobConstantV1JSONConverter.class);
         providerFactory.addStringConverter(RESTCategoryV1JSONConverter.class);
         providerFactory.addStringConverter(RESTFilterV1JSONConverter.class);
         providerFactory.addStringConverter(RESTImageV1JSONConverter.class);
         providerFactory.addStringConverter(RESTProjectV1JSONConverter.class);
         providerFactory.addStringConverter(RESTPropertyTagV1JSONConverter.class);
         providerFactory.addStringConverter(RESTRoleV1JSONConverter.class);
         providerFactory.addStringConverter(RESTStringConstantV1JSONConverter.class);
         providerFactory.addStringConverter(RESTTagV1JSONConverter.class);
         providerFactory.addStringConverter(RESTTopicV1JSONConverter.class);
         providerFactory.addStringConverter(RESTTranslatedTopicV1JSONConverter.class);
         providerFactory.addStringConverter(RESTUserV1JSONConverter.class);
         providerFactory.addStringConverter(RESTBlobConstantCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTCategoryCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTFilterCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTImageCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTProjectCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTPropertyTagCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTRoleCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTStringConstantCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTTagCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTTopicCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTTranslatedTopicCollectionV1JSONConverter.class);
         providerFactory.addStringConverter(RESTUserCollectionV1JSONConverter.class);
 
        final APIVersionDecorator versionDecorator = new APIVersionDecorator("1.0-SNAPSHOT");
         providerFactory.registerProviderInstance(versionDecorator);
 
         RegisterBuiltin.register(providerFactory);
     }
 
     private final ClientRequestFactory requestFactory;
 
     private PressGangCCMSProxyFactoryV1(final String url) {
         final URI uri = ProxyFactory.createUri(fixUrl(url));
         requestFactory = new ClientRequestFactory(uri);
     }
 
     public static PressGangCCMSProxyFactoryV1 create(final String url) {
         return new PressGangCCMSProxyFactoryV1(url);
     }
 
     private String fixUrl(final String url) {
         String fixedUrl = new String(url);
         if (!fixedUrl.endsWith("/")) {
             fixedUrl += "/";
         }
 
         return fixedUrl;
     }
 
     public RESTInterfaceV1 getRESTClient() {
         return requestFactory.createProxy(RESTInterfaceV1.class);
     }
 
     public RESTInterfaceAdvancedV1 getRESTAdvancedClient() {
         return requestFactory.createProxy(RESTInterfaceAdvancedV1.class);
     }
 
     public void registerProvider(final Class<?> clazz) {
         providerFactory.registerProvider(clazz);
     }
 }
 
