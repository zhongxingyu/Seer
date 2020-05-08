 /**
  *     Copyright (C) 2011 Julien SMADJA <julien dot smadja at gmail dot com>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.anzymus.neogeo.hiscores.service;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import javax.ejb.embeddable.EJBContainer;
 import javax.naming.Context;
 import javax.naming.NamingException;
 
public abstract class AbstractTest {
 
     protected static EJBContainer container;
 
     protected static Context namingContext;
 
     protected static PlayerService playerService;
     protected static ScoreService scoreService;
     protected static GameService gameService;
 
     static {
         try {
             Map<String, Object> properties = new HashMap<String, Object>();
             properties.put(EJBContainer.MODULES, new File("target/classes"));
             container = EJBContainer.createEJBContainer(properties);
 
             namingContext = container.getContext();
 
             playerService = (PlayerService) lookup("PlayerService");
             scoreService = (ScoreService) lookup("ScoreService");
             gameService = (GameService) lookup("GameService");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private static Object lookup(String key) throws NamingException {
         return namingContext.lookup("java:global/classes/" + key);
     }
 
 }
