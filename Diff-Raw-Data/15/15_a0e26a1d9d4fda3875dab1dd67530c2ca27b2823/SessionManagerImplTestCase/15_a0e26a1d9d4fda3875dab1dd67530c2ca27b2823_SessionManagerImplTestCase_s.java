 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.core;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 
 public class SessionManagerImplTestCase extends MockObjectTestCase {
 
 	@SuppressWarnings("unchecked")
 	public void test() throws Exception {
 		
 		String name = "Metamodel";
 		Metadata<String, String> metadata = null;
 		final ServiceFactory<Model<Object, Object>> serviceFactory = mock(ServiceFactory.class);
 		
 		MetaModelImpl metaModel = new MetaModelImpl(name, metadata);
		SessionManager sessionManager = new SessionManagerImpl(metaModel, serviceFactory);
 		
 		final Model<Object, Object> model = mock(Model.class);
 		final String modelName = "MyModel";
 
 		checking(new Expectations(){{
 			one(model).getName();
             will(returnValue(modelName));
         }});
         
         assertTrue(metaModel.getData().getExtant().size() == 0);
         
         // update metamodel - insertion
         
         Collection<Update<Model<Object, Object>>> nil = Collections.emptyList();
 		metaModel.update(Collections.singleton(new Update<Model<Object, Object>>(null, model)), nil, nil);
 
 		{
 			Collection<String> data = metaModel.getData().getExtant();
 	        assertTrue(data.size() == 1);
 	        assertTrue(data.iterator().next() == modelName);
 		}
         
         final Data<Object> data = new Data<Object>(null, null);
         final View<Object> view = mock(View.class);
 
         // // register a view - unsuccessfully 
 
         // checking(new Expectations(){{
         // 	one(serviceFactory).server(model, modelName);
         // 	will(throwException(new UnsupportedOperationException()));
         // }});
         
         // assertTrue(sessionManager.registerView(modelName, view) == null);
         
         // // register a view - successfully
 
         // checking(new Expectations(){{
         // 	one(serviceFactory).server(model, modelName);
         // 	will(returnValue(null));
         //     one(model).registerView(view);
         //     will(returnValue(data));
         // }});
         
         // assertTrue(sessionManager.registerView(modelName, view) == data);
         
         // // deregister view
         
         // checking(new Expectations(){{
         //     one(model).deregisterView(view);
         //     will(returnValue(null));
         // }});
 
         // assertTrue(sessionManager.deregisterView(modelName, view) == null);
 
         // // register/deregister 2nd view on MODEL - exercises slightly different code path...
 
         // checking(new Expectations(){{
         //     one(model).registerView(view);
         //     will(returnValue(data));
         // }});
         
         // assertTrue(sessionManager.registerView(modelName, view) == data);
 
         // checking(new Expectations(){{
         //     one(model).deregisterView(view);
         //     will(returnValue(null));
         // }});
 
         // assertTrue(sessionManager.deregisterView(modelName, view) == null);
 
         // // update metamodel - update
 
         // checking(new Expectations(){{
 	// 		one(model).getName();
         //     will(returnValue(modelName));
 	// 		one(model).getName();
         //     will(returnValue(modelName));            
         // }});
         
 	// 	metaModel.update(nil, Collections.singleton(new Update<Model<Object, Object>>(model, model)), nil);
 		
         // {
         // 	Collection<String> data = metaModel.getData();
         //     assertTrue(data.size() == 1);
         //     assertTrue(data.iterator().next() == modelName);
         // }
 
         // // update metamodel - deletion
 		
 	// 	checking(new Expectations(){{
 	// 		one(model).getName();
         //     will(returnValue(modelName));
         // }});
         
 	// 	metaModel.update(nil, nil, Collections.singleton(new Update<Model<Object, Object>>(model, null)));
         // assertTrue(metaModel.getData().size() == 0);
 	}
 }
