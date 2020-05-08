 /**
  * Copyright (C) 2006 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.guiceyfruit.jndi;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Injector;
 import com.google.inject.Provider;
 import com.google.inject.Scopes;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.naming.spi.InitialContextFactory;
 import org.guiceyfruit.Injectors;
 import org.guiceyfruit.jndi.internal.JndiContext;
 
 /**
  * A factory of the Guice JNDI provider which creates an injector from all the available modules
  * specified in the space separated {@link Injectors#MODULE_CLASS_NAMES} property.
  *
  * For more details of how this JNDI provider works see
  * <a href="http://code.google.com/p/camel-extra/wiki/GuiceJndi">the wiki documentation</a>
  *
  * @version $Revision: 656978 $
  */
 public class GuiceInitialContextFactory implements InitialContextFactory {
   public static final String NAME_PREFIX = "org.guiceyfruit.jndi/";
 
   /**
    * Creates a new context with the given environment.
    *
    * @param environment the environment, must not be <tt>null</tt>
    * @return the created context.
    * @throws NamingException is thrown if creation failed.
    */
   public Context getInitialContext(final Hashtable environment) throws NamingException {
     try {
       // lets avoid infinite recursion with a provider creating an InitialContext by binding the
       // singleton initial context into the injector
       Injector injector = Injectors.createInjector(environment, new AbstractModule() {
         protected void configure() {
           bind(Context.class).toProvider(new Provider<Context>() {
             public Context get() {
              return new JndiContext(environment);
             }
           }).in(Scopes.SINGLETON);
         }
       });
      Context context = injector.getInstance(Context.class);

      Properties jndiNames = createJndiNamesProperties(environment);
      JndiBindings.bindInjectorAndBindings(context, injector, jndiNames);
      return context;
    }
    catch (NamingException e) {
      throw e;
     }
     catch (Exception e) {
       NamingException exception = new NamingException(e.getMessage());
       exception.initCause(e);
       throw exception;
     }
   }
 
   /**
    * Creates a properties object containing all of the values whose keys start with
    * {@link #NAME_PREFIX} with the prefix being removed on the key
    * @return a properties object
    */
   private Properties createJndiNamesProperties(Hashtable environment) {
     Set<Map.Entry> set = environment.entrySet();
     Properties answer = new Properties();
     for (Entry entry : set) {
       String key = entry.getKey().toString();
       if (key.startsWith(NAME_PREFIX)) {
         String name = key.substring(NAME_PREFIX.length());
         Object value = entry.getValue();
         answer.put(name, value);
       }
     }
     return answer;
   }
 
 }
