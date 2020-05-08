 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.core.scope;
 
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.Key;
 import com.google.inject.Provider;
 import com.google.inject.Scope;
 
 import de.cosmocode.palava.core.Procedure;
 import de.cosmocode.palava.core.Registry;
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 
 /**
  * Abstract skeleton implementation of the {@link Scope} interface.
  *
  * @author Willi Schoenborn
  * @param <S> the generic scope context type
  */
 public abstract class AbstractScope<S extends ScopeContext> implements Scope, ScopeExitEvent, Provider<S>,
     Initializable, Disposable {
 
     private static final Logger LOG = LoggerFactory.getLogger(AbstractScope.class);
     
     private final Procedure<ScopeEnterEvent> enter = new Procedure<ScopeEnterEvent>() {
         
         @Override
         public void apply(ScopeEnterEvent input) {
             input.eventScopeEnter(get());
         }
         
     };
     
     private final Procedure<ScopeExitEvent> exit = new Procedure<ScopeExitEvent>() {
         
         @Override
         public void apply(ScopeExitEvent input) {
             input.eventScopeExit(get());
         }
         
     };
 
     private Registry registry;
     
     @Inject
     void setRegistry(Registry registry) {
         this.registry = registry;
     }
     
     @Override
     public void initialize() throws LifecycleException {
         registry.register(ScopeExitEvent.class, this);
     }
     
     /**
      * Checks whether this scope is currently in progress.
      * 
      * @return true if this scope is currently in progress, false otherwise
      */
     protected abstract boolean inProgress();
     
     /**
      * Being called by {@link AbstractScope#enter()} to allow
      * doing work.
      */
     protected void doEnter() {
         
     }
     
     /**
      * Enters this scope.
      */
     public final void enter() {
         Preconditions.checkState(!inProgress(), "%s is already in progress", getClass().getSimpleName());
         try {
             LOG.trace("Entering {}", getClass().getSimpleName());
             doEnter();
         } finally {
            registry.notify(ScopeEnterEvent.class, enter);
         }
     }
     
     @Override
     public final <T> Provider<T> scope(final Key<T> key, final Provider<T> provider) {
         LOG.trace("Intercepting scoped request with {} to {}", key, provider);
         Preconditions.checkState(inProgress(), "No %s in progress", getClass().getSimpleName());
         final ScopeContext context = get();
         return new Provider<T>() {
 
             @Override
             public T get() {
                 final T cached = context.<Key<T>, T>get(key);
                 // is there a cached version?
                 if (cached == null && !context.contains(key)) {
                     final T unscoped = provider.get();
                     context.set(key, unscoped);
                     LOG.trace("No cached version for {} found, created {}", key, unscoped);
                     return unscoped;
                 } else {
                     LOG.trace("Found cached version for {}: {}", key, cached);
                     return cached;
                 }
             }
 
         };
     }
     
     /**
      * Being called by {@link AbstractScope#exit()} to allow
      * doing work.
      */
     protected void doExit() {
         
     }
     
     /**
      * Exits this scope.
      */
     public final void exit() {
         if (!inProgress()) {
             LOG.warn("{} already exited", getClass().getSimpleName());
             return;
         }
        try {
            LOG.trace("Exiting {}", getClass().getSimpleName());
            doExit();
        } finally {
            registry.notify(ScopeExitEvent.class, exit);
        }
     }
     
     @Override
     public void dispose() throws LifecycleException {
         registry.remove(this);
     }
     
     @Override
     public void eventScopeExit(ScopeContext context) {
         for (Entry<Object, Object> entry : context) {
             if (entry.getKey() instanceof Destroyable) {
                 try {
                     Destroyable.class.cast(entry.getKey()).destroy();
                 /*CHECKSTYLE:OFF*/
                 } catch (RuntimeException e) {
                 /*CHECKSTYLE:ON*/
                     LOG.error("Failed to destroy scoped key: {}", e);
                 }
             }
             if (entry.getValue() instanceof Destroyable) {
                 try {
                     Destroyable.class.cast(entry.getValue()).destroy();
                 /*CHECKSTYLE:OFF*/
                 } catch (RuntimeException e) {
                 /*CHECKSTYLE:ON*/
                     LOG.error("Failed to destroy scoped value: {}", e);
                 }
             }
         }
     }
 
 }
