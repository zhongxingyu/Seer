 /*
  * Copyright (C) 2011 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.common.regex.util;
 
 import org.exoplatform.common.regex.annotation.ListenerConfig;
 import org.exoplatform.common.regex.annotation.MachineConfiguration;
 import org.exoplatform.common.regex.event.MachineEventListener;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
  * @date 7/7/11
  */
 public class ConfigurationUtil
 {
   public static <C, A> Map<String, MachineEventListener<C, A>> getListeners(MachineConfiguration config)
    {
      Map<String, MachineEventListener<C, A>> listenerMap = new HashMap<String, MachineEventListener<C, A>>();
 
       for(ListenerConfig listenerConfig : config.listeners())
       {
         try
         {
             String id = listenerConfig.id();
             Class<MachineEventListener> clazz = listenerConfig.type();
            MachineEventListener<C, A> listener = (MachineEventListener<C, A>)clazz.newInstance();
             listenerMap.put(id, listener);
          }
          catch(Exception ex)
          {
             //TODO: Add the logging system
          }
       }
       return listenerMap;
    }
 }
