 /*
  * (C) Copyright Blackboard Inc. 2013 - All Rights Reserved
  * 
  * See the NOTICE file distributed with this work for additional information regarding copyright ownership. Blackboard
  * Inc. licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at:
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * BLACKBOARD MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-
  * INFRINGEMENT. BLACKBOARD SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
  * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
  */
 
 package blackboard.plugin.hayabusa.config;
 
 import blackboard.plugin.hayabusa.provider.ModuleItemProvider;
 import blackboard.plugin.hayabusa.provider.Provider;
 
 import org.springframework.context.annotation.*;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 
 /**
  * Spring configuration for the Hayabua project.
  * 
  * @author Danny Thomas
  * @since 1.0
  */
 @Configuration
 @EnableWebMvc
 @ComponentScan( basePackages = "blackboard.plugin.hayabusa" )
 public class HayabusaConfiguration extends WebMvcConfigurerAdapter
 {
 
   @Bean
   public Provider moduleItemProvider()
   {
     return new ModuleItemProvider();
   }
 
}
