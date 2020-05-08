 /*
  *  Copyright the original author or authors.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package therian;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.el.CompositeELResolver;
 import javax.el.ELContext;
 import javax.el.ELContextEvent;
 import javax.el.ELContextListener;
 import javax.el.ELResolver;
 import javax.el.ExpressionFactory;
 
 import therian.uelbox.ELContextWrapper;
 import therian.uelbox.SimpleELContext;
 
 /**
  * Therian entry point.
  */
 public class Therian {
     private static final TherianModule DEFAULT_MODULE = TherianModule.create().withOperators(Operators.standard());
 
     private static final Therian STANDARD = Therian.usingModules(DEFAULT_MODULE);
 
     private final TherianModule[] modules;
     // TODO sort by type/params
     private final Set<Operator<?>> operators = new LinkedHashSet<Operator<?>>();
     private final List<ELResolver> elResolvers = new ArrayList<ELResolver>();
 
     private Therian(TherianModule... modules) {
         this.modules = modules == null ? new TherianModule[0] : modules;
        for (TherianModule module : modules) {
             Collections.addAll(operators, module.getOperators());
             Collections.addAll(elResolvers, module.getElResolvers());
         }
     }
 
     Set<Operator<?>> getOperators() {
         return operators;
     }
 
     public TherianContext context() {
         return contextFor(new SimpleELContext());
     }
 
     public TherianContext contextFor(ELContext wrapped) {
         TherianContext result = new TherianContext(new ELContextWrapper(wrapped) {
 
             @Override
             protected ELResolver wrap(ELResolver elResolver) {
                 final CompositeELResolver compositeResolver = new CompositeELResolver();
                 for (TherianModule module : modules) {
                     for (ELResolver configuredELResolver : module.getElResolvers()) {
                         compositeResolver.add(configuredELResolver);
                     }
                 }
                 compositeResolver.add(elResolver);
                 return compositeResolver;
             }
         });
         result.putContext(Therian.class, this);
         ExpressionFactory expressionFactory = result.getTypedContext(ExpressionFactory.class);
         if (expressionFactory == null) {
             result.putContext(ExpressionFactory.class, ExpressionFactory.newInstance());
         }
         ELContextEvent event = new ELContextEvent(result);
         for (TherianModule module : modules) {
             for (ELContextListener listener : module.getElContextListeners()) {
                 listener.contextCreated(event);
             }
         }
         return result;
     }
 
     public static Therian usingModules(TherianModule... modules) {
         return new Therian(modules);
     }
 
     public static Therian standard() {
         return STANDARD;
     }
 
 }
