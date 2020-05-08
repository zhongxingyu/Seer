 /*
  * Copyright (c) 2011, DataLite. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301  USA
  */
 package cz.datalite.zk.annotation.invoke;
 
 import java.util.List;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zkplus.databind.DataBinder;
 
 /**
  * <p>Handles binding request before and after method invocation. For
  * all registered component executes load or safe based on annotation's
  * properties.</p>
  *
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class ZkBindingHandler extends Handler {
 
     /** Components to be saved before */
     private List<Component> saveBefore;
 
     /** Components to be load after */
     private List<Component> loadAfter;
 
     public ZkBindingHandler( Invoke inner, List<Component> saveBefore, List<Component> loadAfter ) {
         super( inner );
         this.saveBefore = saveBefore;
         this.loadAfter = loadAfter;
     }
 
     @Override
     protected void doAfterInvoke( Event event ) {
         for ( Component component : loadAfter ) {
            getBinder( component ).saveComponent( component );
         }
     }
 
     @Override
     protected boolean doBeforeInvoke( Event event ) {
         for ( Component component : saveBefore ) {
             getBinder( component ).saveComponent( component );
         }
         return true;
     }
 
     /**
      * Vraci odkaz na binder
      * @param comp komponenta podle ktere ho urci
      * @return odkaz na binder
      */
     private static DataBinder getBinder( final Component comp ) {
         return ( DataBinder ) comp.getAttributeOrFellow( "binder", true );
     }
 }
