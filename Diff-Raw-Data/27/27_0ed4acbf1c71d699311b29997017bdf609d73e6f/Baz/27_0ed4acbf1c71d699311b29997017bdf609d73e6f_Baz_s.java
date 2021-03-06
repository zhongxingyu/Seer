 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc., and individual contributors
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
 package org.jboss.weld.extensions.test.bean.generic.field;
 
 import java.io.Serializable;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.Any;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 
 import org.jboss.weld.extensions.bean.generic.Generic;
 import org.jboss.weld.extensions.bean.generic.GenericBean;
 
 /**
  * A generic bean for the config annotation Message
  * 
  * @author pmuir
  *
  */
 
 @Generic(Message.class)
 public class Baz implements Serializable
 {
 
    private static final long serialVersionUID = 6807449196645110050L;
 
    @Inject @GenericBean
    private Bar bar;
 
    @Inject
    private Corge corge;
    
    @Inject 
    private Message message;
 
    public Bar getBar()
    {
       return bar;
    }
    
    public Corge getCorge()
    {
       return corge;
    }
    
    @Produces
    public Message getMessage()
    {
       return message;
    }
    
    @Produces @Wibble
    public String getCorge(Wobble wobble)
    {
       return wobble.getName() + message.value();
    }
    
    public void observe(@Observes @Any Plugh event)
    {
      // Workaround WELD-573
       if (message != null)
       {
          event.setMessage(message);
       }
    }
 }
