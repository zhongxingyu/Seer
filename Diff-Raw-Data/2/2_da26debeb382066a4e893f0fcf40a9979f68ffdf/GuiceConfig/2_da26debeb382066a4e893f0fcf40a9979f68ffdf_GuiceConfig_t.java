 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.rest.sample.rest;
 
 import com.cedarsoft.rest.sample.User;
 import com.cedarsoft.rest.sample.jaxb.UserMapping;
 import com.google.common.collect.ImmutableList;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.Stage;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.google.inject.servlet.ServletModule;
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.core.util.FeaturesAndProperties;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class GuiceConfig extends GuiceServletContextListener {
   @Override
   protected Injector getInjector() {
     final Map<String, String> params = new HashMap<String, String>();
     params.put( PackagesResourceConfig.PROPERTY_PACKAGES, UsersResource.class.getPackage().getName() );
     params.put( FeaturesAndProperties.FEATURE_XMLROOTELEMENT_PROCESSING, "true" );
 
     return Guice.createInjector( Stage.DEVELOPMENT, new ExampleModule(), new ServletModule() {
       @Override
       protected void configureServlets() {
         serve( "/*" ).with( GuiceContainer.class, params );
       }
     } );
   }
 
   public static class ExampleModule extends AbstractModule {
     public ExampleModule() {
       System.out.println( "---------------" );
       System.out.println( "---------------" );
       System.out.println( "GuiceConfig$ExampleModule.ExampleModule" );
       System.out.println( "---------------" );
       System.out.println( "---------------" );
     }
 
     @Override
     protected void configure() {
       bind( UserMapping.class ).in( Singleton.class );
     }
 
     @Provides
     List<? extends User> provideUsers() {
       User js = new User( "info@cedarsoft.de", "Johannes Schneider" );
       User max = new User( "markus@mustermann.de", "Markus Mustermann" );
       User eva = new User( "eva@mustermann.de", "Eva Mustermann" );
 
       js.addFriend( max );
       js.addFriend( eva );
       eva.addFriend( max );
      max.addFriend( eva );
       max.addFriend( js );
 
       return ImmutableList.of(
         js,
         max,
         eva
       );
     }
 
 //    @Nullable
 //    private transient WebApplication webApplicationReference;
 //
 //    @Provides
 //    public WebApplication webApp( @NotNull GuiceContainer guiceContainer ) {
 //      WebApplication copy = webApplicationReference;
 //      if ( copy == null ) {
 //        WebComponent component = Reflection.field( "webComponent" ).ofType( WebComponent.class ).in( guiceContainer ).get();
 //        copy = Reflection.field( "application" ).ofType( WebApplication.class ).in( component ).get();
 //        webApplicationReference = copy;
 //      }
 //      return copy;
 //    }
 //
 //    @RequestScoped
 //    @Provides
 //    public HttpContext httpContext( @NotNull WebApplication webApplication ) {
 //      return webApplication.getThreadLocalHttpContext();
 //    }
 //
 //    @Provides
 //    public ExceptionMapperContext exceptionMapperContext( @NotNull WebApplication webApplication ) {
 //      return webApplication.getExceptionMapperContext();
 //    }
 //
 //    @Provides
 //    public FeaturesAndProperties featuresAndProperties( @NotNull WebApplication webApplication ) {
 //      return webApplication.getFeaturesAndProperties();
 //    }
 //
 //    @Provides
 //    public ResourceConfig resourceConfig( @NotNull WebApplication webApplication ) {
 //      return ( ResourceConfig ) webApplication.getFeaturesAndProperties();
 //    }
 //
 //    @Provides
 //    public MessageBodyWorkers messageBodyFactory( @NotNull WebApplication webApplication ) {
 //      return webApplication.getMessageBodyWorkers();
 //    }
 //
 //    @RequestScoped
 //    @Provides
 //    public UriInfo uriInfo( @NotNull HttpContext httpContext ) {
 //      return httpContext.getUriInfo();
 //    }
 //
 //    @RequestScoped
 //    @Provides
 //    public HttpRequestContext requestContext( @NotNull HttpContext httpContext ) {
 //      return httpContext.getRequest();
 //    }
 //
 //    @RequestScoped
 //    @Provides
 //    public HttpHeaders httpHeaders( @NotNull HttpContext httpContext ) {
 //      return httpContext.getRequest();
 //    }
 //
 //    @RequestScoped
 //    @Provides
 //    public Request request( @NotNull HttpContext httpContext ) {
 //      return httpContext.getRequest();
 //    }
 //
 //    @RequestScoped
 //    @Provides
 //    public SecurityContext securityContext( @NotNull HttpContext httpContext ) {
 //      return httpContext.getRequest();
 //    }
 //
 //    @RequestScoped
 //    @Provides
 //    public UriBuilder uriBuilder( @NotNull UriInfo uriInfo ) {
 //      return uriInfo.getRequestUriBuilder();
 //    }
   }
 }
 
