 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.infinispan.arquillian.core;
 
 import org.infinispan.test.arquillian.DatagridManager;
 import org.jboss.arquillian.core.api.Instance;
 import org.jboss.arquillian.core.api.annotation.Inject;
 import org.jboss.arquillian.core.api.annotation.Observes;
 import org.jboss.arquillian.test.spi.event.suite.After;
 import java.util.logging.Logger;
 
 /**
  * 
  * DatagridDestructor.
  * 
  * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
  * 
  */
 public class DatagridDestructor
 {
    private static final Logger log = Logger.getLogger(DatagridDestructor.class.getName());
    
    @Inject
    private Instance<InfinispanContext> infinispanContext;
    
    @SuppressWarnings("unchecked")
    public void destroyInfinispanDatagrid(@Observes After event)
    {
       DatagridManager manager = (DatagridManager) infinispanContext.get().get(DatagridManager.class);
       if (manager != null)
       {
         log.fine("Destroying datagrid...");
          manager.destroy();
       }
    }
 }
