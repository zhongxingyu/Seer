 /*
  * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.service.mocks;
 
 import org.qi4j.composite.CompositeBuilder;
 import org.qi4j.test.AbstractQi4jTest;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.bootstrap.AssemblyException;
 
 public class MockEntityServiceTest extends AbstractQi4jTest
 {
     private SampleService sampleService;
 
    protected void setUp()
        throws Exception
     {
        super.setUp();
         CompositeBuilder<SampleServiceComposite> compositeBuilder = compositeBuilderFactory.newCompositeBuilder( SampleServiceComposite.class );
         sampleService = compositeBuilder.newInstance();
     }
 
     public void testSave()
     {
         SampleEntityComposite account = sampleService.newInstance( SampleEntityComposite.class );
         sampleService.save( account );
         assertEquals( sampleService.countAll(), 1 );
     }
 
     public void testGet()
     {
         SampleEntityComposite sample = sampleService.newInstance( SampleEntityComposite.class );
 
         sampleService.save( sample );
 
         assertNotNull( sampleService.get( sample.identity().get() ) );
     }
 
     public void testDelete()
     {
         SampleEntityComposite sample = sampleService.newInstance( SampleEntityComposite.class );
 
         sampleService.save( sample );
 
         assertEquals( sampleService.countAll(), 1 );
     }
 
     public void configure( ModuleAssembly module ) throws AssemblyException
     {
        module.addComposites( SampleEntityComposite.class );
     }
 }
