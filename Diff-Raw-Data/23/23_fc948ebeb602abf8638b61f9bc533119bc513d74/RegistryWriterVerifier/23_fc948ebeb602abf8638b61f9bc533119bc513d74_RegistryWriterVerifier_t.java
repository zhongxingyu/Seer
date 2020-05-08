 package org.codehaus.modello.plugin.registry;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.apache.archiva.redback.components.registry.commons.CommonsConfigurationRegistry;
 import org.codehaus.modello.test.model.Model;
 import org.codehaus.modello.test.model.Reference;
 import org.codehaus.modello.test.model.EmptyReference;
 import org.codehaus.modello.test.model.io.registry.ModelRegistryWriter;
 import org.codehaus.modello.verifier.Verifier;
 import org.apache.archiva.redback.components.registry.Registry;
 
 import junit.framework.Assert;
 
import java.lang.AssertionError;
 import java.util.*;
 
 /**
  * @author <a href="mailto:brett@apache.org">Brett Porter</a>
  *
  */
 public class RegistryWriterVerifier
     extends Verifier
 {
     private static Reference createReference( String name )
     {
         Reference reference = new Reference();
         reference.setName( name );
         return reference;
     }
 
     public void verify()
         throws Exception
     {
         Registry registry = new CommonsConfigurationRegistry();
         registry.initialize();
 
         Model model = new Model();
         model.setName( "name" );
         model.setNumeric( 9 );
         model.setReference( createReference( "ref-name" ) );
         model.setEmptyReference( new EmptyReference() );
         model.setListReferences( new ArrayList( Arrays.asList( new Reference[] {
             createReference( "list-name1" ),
             createReference( "list-name2" ),
             createReference( "list-name3" )
         })));
         model.setSetReferences( new HashSet( Arrays.asList( new Reference[] {
             createReference( "set-name1" ),
             createReference( "set-name2" ),
         })));
         model.setStringReferences( Arrays.asList( new String[] { "S1", "S2", "S3", "S4", "S5" } ) );
 
         Map map = new HashMap();
         map.put( "property", "value1" );
         map.put( "property2", "value2" );
         map.put( "something.else", "value3" );
         model.setMap( map );
 
         Properties properties = new Properties();
         properties.setProperty( "property", "value1" );
         properties.setProperty( "property2", "value2" );
         properties.setProperty( "something.else", "value3" );
         model.setProperties( properties );
 
         ModelRegistryWriter modelWriter = new ModelRegistryWriter();
 
         modelWriter.write( model, registry );
 
         Assert.assertEquals( "name", registry.getString( "name" ) );
         Assert.assertEquals( 9, registry.getInt( "numeric" ) );
         Assert.assertEquals( "ref-name", registry.getString( "reference.name" ) );
         Assert.assertNull( registry.getString( "missingReference" ) );
         Assert.assertNull( registry.getString( "missingReference.name" ) );
         Assert.assertNull( registry.getString( "emptyReference" ) );
         Assert.assertNull( registry.getString( "emptyReference.name" ) );
         Assert.assertEquals( "list-name1", registry.getString( "listReferences.listReference(0).name" ) );
         Assert.assertEquals( "list-name2", registry.getString( "listReferences.listReference(1).name" ) );
         Assert.assertEquals( "list-name3", registry.getString( "listReferences.listReference(2).name" ) );
         List names = new ArrayList( 2 );
         names.add( registry.getString( "setReferences.setReference(0).name" ) );
         names.add( registry.getString( "setReferences.setReference(1).name" ) );
         Collections.sort( names );
         Assert.assertEquals( Arrays.asList( new String[] { "set-name1", "set-name2" } ), names );
         Assert.assertEquals( Arrays.asList( new String[] { "S1", "S2", "S3", "S4", "S5" } ),
                              registry.getList( "stringReferences.stringReference" ) );
 
         map = registry.getProperties( "map" );
         Assert.assertEquals( 3, map.size() );
         Assert.assertEquals( "value1", map.get( "property" ) );
         Assert.assertEquals( "value2", map.get( "property2" ) );
         Assert.assertEquals( "value3", map.get( "something.else" ) );
 
         properties = registry.getProperties( "properties" );
         Assert.assertEquals( 3, properties.size() );
         Assert.assertEquals( "value1", properties.getProperty( "property" ) );
         Assert.assertEquals( "value2", properties.getProperty( "property2" ) );
         Assert.assertEquals( "value3", properties.getProperty( "something.else" ) );
 
         // test defaults
         Assert.assertNull( registry.getString( "defString" ) );
 
         try
         {
             registry.getInt( "defNumeric" );
             Assert.fail();
         }
         catch ( NoSuchElementException e )
         {
             // expected
         }
 

        Assert.assertTrue( registry.getBoolean( "defBoolean" ) );

 
         // test removing an element from a list [MODELLO-84]
         model.getListReferences().remove( 0 );
         modelWriter.write( model, registry );
         Assert.assertEquals( "list-name2", registry.getString( "listReferences.listReference(0).name" ) );
         Assert.assertEquals( "list-name3", registry.getString( "listReferences.listReference(1).name" ) );
         Assert.assertNull( registry.getString( "listReferences.listReference(2).name" ) );
 
         // test removing an element from a map
         model.getMap().remove( "property2" );
         modelWriter.write( model, registry );
         map = registry.getProperties( "map" );
         Assert.assertEquals( 2, map.size() );
         Assert.assertEquals( "value1", map.get( "property" ) );
         Assert.assertNull( "value2", map.get( "property2" ) );
         Assert.assertEquals( "value3", map.get( "something.else" ) );
     }
 }
