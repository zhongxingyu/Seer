 package net.milanaleksic.guitransformer.providers.impl;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableMap;
 import net.milanaleksic.guitransformer.converters.Converter;
 import net.milanaleksic.guitransformer.converters.typed.TypedConverter;
 import net.milanaleksic.guitransformer.integration.loader.Loader;
 import net.milanaleksic.guitransformer.providers.ConverterProvider;
 import net.milanaleksic.guitransformer.util.*;
 
 import javax.inject.Inject;
 import java.util.concurrent.atomic.AtomicReference;
 
 public class ConfigurableConverterProvider implements ConverterProvider {
 
     private final Loader loader;
 
     private final AtomicReference<ImmutableMap<Class<?>, Converter>> mapping = new AtomicReference<>(null);
 
     @Inject
     public ConfigurableConverterProvider(Loader loader) {
         this.loader = loader;
     }
 
     private void bootUpLazilyMapping() {
         mapping.compareAndSet(null,
                 Configuration.<Converter>loadClassToInstanceMapping(
                        "converters",
                         Optional.of(loader)
                 )
         );
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 for (Converter converter : mapping.get().values()) {
                     converter.cleanUp();
                 }
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     private Converter doProvideConverterForClass(Class<?> clazz) {
         if (mapping.get() == null)
             bootUpLazilyMapping();
         return mapping.get().get(clazz);
     }
 
     @Override
     public Converter provideConverterForClass(final Class<?> argType) {
         Converter converter = doProvideConverterForClass(argType);
         if (converter == null)
             return doProvideConverterForClass(Object.class);
         return converter;
     }
 
     @Override
     @SuppressWarnings({"unchecked"})
     public <T> Optional<TypedConverter<T>> provideTypedConverterForClass(final Class<T> type) {
         TypedConverter<T> converter = (TypedConverter<T>) doProvideConverterForClass(type);
         if (converter == null)
             return Optional.absent();
         return Optional.of(converter);
     }
 
 }
