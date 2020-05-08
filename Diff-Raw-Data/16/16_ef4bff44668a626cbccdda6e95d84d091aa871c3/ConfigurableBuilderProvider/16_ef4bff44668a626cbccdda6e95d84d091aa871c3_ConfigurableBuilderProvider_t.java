 package net.milanaleksic.guitransformer.providers.impl;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableMap;
 import net.milanaleksic.guitransformer.builders.Builder;
 import net.milanaleksic.guitransformer.integration.loader.Loader;
 import net.milanaleksic.guitransformer.providers.BuilderProvider;
 import net.milanaleksic.guitransformer.util.Configuration;
 
 import javax.inject.Inject;
 import java.util.concurrent.atomic.AtomicReference;
 
 public class ConfigurableBuilderProvider implements BuilderProvider {
 
     private final Loader loader;
 
     private final AtomicReference<ImmutableMap<String, Builder<?>>> mapping = new AtomicReference<>(null);
 
     @Inject
     public ConfigurableBuilderProvider(Loader loader) {
         this.loader = loader;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> Builder<T> provideBuilderForName(String name) {
         if (mapping.get() == null)
             bootUpLazilyMapping();
         return (Builder<T>) mapping.get().get(name);
     }
 
     private void bootUpLazilyMapping() {
         mapping.compareAndSet(null,
                 Configuration.<Builder<?>>loadStringToInstanceMapping(
                        "net.milanaleksic.guitransformer.builders",
                         Optional.of(loader)
                 )
         );
     }
 
 }
