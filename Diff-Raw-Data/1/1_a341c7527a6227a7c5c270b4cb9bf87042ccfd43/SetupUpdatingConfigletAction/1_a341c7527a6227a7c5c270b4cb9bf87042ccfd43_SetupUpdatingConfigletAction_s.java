 package de.zib.gndms.infra.action;
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import de.zib.gndms.kit.configlet.Configlet;
 import de.zib.gndms.logic.model.config.ConfigActionResult;
 import de.zib.gndms.model.common.ConfigletState;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 import java.io.PrintWriter;
 import java.text.ParseException;
 
 /**
  * @author try ma ik jo rr a zib
  * @date 13.04.11  14:24
  * @brief Delegates the update to the configlet class.
  */
 public class SetupUpdatingConfigletAction extends SetupDefaultConfigletAction {
 
     protected Log logger = LogFactory.getLog( this.getClass() );
 
 
     @Override
     protected ConfigActionResult update( ConfigletState state, EntityManager emParam, PrintWriter writerParam ) {
         ConfigActionResult res = super.update( state, emParam, writerParam );
         try {
             Class clazz = Class.forName( state.getClassName() );
             Configlet conf = Configlet.class.cast( clazz.newInstance() ); // todo find class from system dict
             conf.update( state.getState() );
         } catch ( ClassNotFoundException e ) {
             logger.warn( e );
         } catch ( InstantiationException e ) {
             logger.warn( e );
         } catch ( IllegalAccessException e ) {
             logger.warn( e );
         }
         return res;
     }
 }
