 /*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.codeartisans.qipki.core.dci;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Context map for DCI contexts.
  * Here is where all object to role mappings are done.
  */
 public class InteractionContext
 {
 
     private Map<Class, Object> roles = new HashMap<Class, Object>();
     private List<Object> objects = new ArrayList<Object>();
 
     public InteractionContext()
     {
         objects.add( this );
     }
 
     public void playRoles( Object object, Class... roleClasses )
     {
         if ( object == null ) {
             return;
         }
         for ( Class roleClass : roleClasses ) {
             roles.put( roleClass, object );
         }
         objects.add( 0, object );
     }
 
     public <T> T role( Class<T> roleClass )
             throws IllegalArgumentException
     {
         Object object = roles.get( roleClass );
 
         if ( object == null ) {
             // If no explicit mapping has been made, see if
             // any other mapped objects could work
             for ( Object possibleObject : objects ) {
                 if ( roleClass.isInstance( possibleObject ) ) {
                     return roleClass.cast( possibleObject );
                 }
             }
         }
         if ( object == null ) {
             throw new IllegalArgumentException( "No object in context for role:" + roleClass.getSimpleName() );
         }
         return roleClass.cast( object );
     }
 
     public <T> List<T> roleList( Class<T> roleClass )
     {
         List<T> list = new ArrayList<T>();
         Object object = roles.get( roleClass );
 
         if ( object != null ) {
             list.add( roleClass.cast( object ) );
         }
         for ( Object possibleObject : objects ) {
             if ( roleClass.isInstance( possibleObject ) ) {
                 list.add( roleClass.cast( possibleObject ) );
             }
         }
         return list;
     }
 
     public void map( Class fromRoleClass, Class... toRoleClasses )
     {
         Object object = roles.get( fromRoleClass );
         if ( object != null ) {
             for ( Class toRoleClass : toRoleClasses ) {
                 if ( toRoleClass.isInstance( object ) ) {
                     roles.put( toRoleClass, object );
                 } else {
                     throw new IllegalArgumentException( object + " does not implement role type " + toRoleClass.getName() );
                 }
             }
         } else {
             throw new IllegalArgumentException( fromRoleClass.getName() + " has not been mapped" );
         }
     }
 
 }
