 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.example;
 
import com.github.sebhoss.contract.module.DefaultSpringModule;
 import com.google.inject.AbstractModule;
 
 /**
 * Simple guice module which uses the default Guice configuration module to enable annotation-based contracts and
 * creates an instance of the domain model class.
  */
 public class CompanyModule extends AbstractModule {
 
     @Override
     protected void configure() {
        install(new DefaultSpringModule());
 
         bind(InsuranceCompany.class).to(OgnlSpELBasedInsuranceCompany.class).asEagerSingleton();
     }
 
 }
