 /*
  * Copyright (c) 2007-2008 Concurrent, Inc. All Rights Reserved.
  *
  * Project and contact information: http://www.cascading.org/
  *
  * This file is part of the Cascading project.
  *
  * Cascading is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Cascading is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cascading.groovy.factory;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import cascading.operation.Identity;
 import cascading.operation.Operation;
 import cascading.tuple.Fields;
 
 /**
  *
  */
 public class IdentityFactory extends OperationFactory
   {
   @Override
   protected Operation makeOperation( Object value, Map attributes, Fields declaredFields )
     {
     ArrayList typesList = (ArrayList) attributes.remove( "types" );
 
     return (Operation) makeInstance( Identity.class, declaredFields, createClassArray( typesList ) );
     }
 
  protected Class[] createClassArray( ArrayList typesList )
     {
    if( typesList == null )
       return null;
 
    Class[] results = new Class[typesList.size()];
 
    for( int i = 0; i < typesList.size(); i++ )
      results[ i ] = (Class) typesList.get( i );
 
     return results;
     }
 
 
   }
