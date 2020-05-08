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
 
 package de.cosmocode.palava.salesforce.sync;
 
 import java.util.concurrent.ExecutorService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.sforce.soap.enterprise.sobject.SObject;
 
import de.cosmocode.commons.concurrent.Runnables;
 import de.cosmocode.palava.model.base.EntityBase;
 import de.cosmocode.palava.salesforce.SalesforceExecutor;
 import de.cosmocode.palava.salesforce.SalesforceService;
 
 /**
  * Abstract base implementation of the {@link SyncService} interface.
  *
  * @author Willi Schoenborn
  */
 public abstract class DefaultSyncService implements SyncService {
 
     private static final Logger LOG = LoggerFactory.getLogger(DefaultSyncService.class);
 
     private final ExecutorService service;
     
     private final SalesforceService salesforce;
     
     @Inject
     public DefaultSyncService(@SalesforceExecutor ExecutorService service, SalesforceService salesforce) {
         this.service = Preconditions.checkNotNull(service, "Service");
         this.salesforce = Preconditions.checkNotNull(salesforce, "Salesforce");
     }
     
     @Override
     public void execute(SyncTask task) {
         Preconditions.checkNotNull(task, "Task");
         LOG.trace("Executing {}", task);
         service.execute(task);
     }
 
     @Override
     public void execute(SyncTask first, SyncTask second, SyncTask... rest) {
         final Runnable task = Runnables.chain(first, second, rest);
         LOG.trace("Executing {}", task);
         service.execute(task);
     }
 
     @Override
     public <S extends EntityBase, T extends SObject> void sync(final S from, final Function<S, T> function) {
         Preconditions.checkNotNull(from, "From");
         Preconditions.checkNotNull(function, "Function");
         
         execute(new SyncTask() {
             
             @Override
             public void run() {
                 salesforce.upsert(function.apply(from));
             }
             
         });
     }
     
     @Override
     public void complete() {
         complete(false);
     }
     
 }
