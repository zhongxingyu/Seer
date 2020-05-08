 package com.shopservice.modules;
 
 import com.google.inject.AbstractModule;
 import com.shopservice.assemblers.CategoryAssembler;
 import com.shopservice.dao.*;
 
 import javax.inject.Singleton;
 
 public class MServiceModule extends AbstractModule {
     @Override
     protected void configure() {
         bind(ProductGroupRepository.class).to(EbeanProductGroupRepository.class).in(Singleton.class);
         bind(Group2ProductRepository.class).to(EbeanGroup2ProductRepository.class).in(Singleton.class);
         bind(ProductEntryRepository.class).to(EbeanProductEntryRepository.class).in(Singleton.class);
         bind(ClientSettingsRepository.class).to(EbeanClientSettingsRepository.class).in(Singleton.class);
     }
 }
