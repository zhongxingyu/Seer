 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jslang.ts.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstParseController;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.jstojava.controller.JstParseController;
 import org.eclipse.vjet.dsf.jstojava.loader.DefaultJstTypeLoader;
 import org.eclipse.vjet.dsf.jstojava.parser.VjoParser;
 import org.eclipse.vjet.dsf.ts.ITypeSpace;
 import org.eclipse.vjet.dsf.ts.event.EventListenerStatus;
 import org.eclipse.vjet.dsf.ts.event.ISourceEventCallback;
 import org.eclipse.vjet.dsf.ts.event.dispatch.IEventListenerHandle;
 import org.eclipse.vjet.dsf.ts.event.group.AddGroupDependencyEvent;
 import org.eclipse.vjet.dsf.ts.event.group.AddGroupEvent;
 import org.eclipse.vjet.dsf.ts.event.group.BatchGroupLoadingEvent;
 import org.eclipse.vjet.dsf.ts.event.group.IGroupEventListener;
 import org.eclipse.vjet.dsf.ts.event.group.RemoveGroupDependencyEvent;
 import org.eclipse.vjet.dsf.ts.event.group.RemoveGroupEvent;
 import org.eclipse.vjet.dsf.ts.event.type.AddTypeEvent;
 import org.eclipse.vjet.dsf.ts.event.type.ITypeEventListener;
 import org.eclipse.vjet.dsf.ts.event.type.ModifyTypeEvent;
 import org.eclipse.vjet.dsf.ts.event.type.RemoveTypeEvent;
 import org.eclipse.vjet.dsf.ts.event.type.RenameTypeEvent;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 import org.eclipse.vjet.vjo.lib.IResourceResolver;
 import org.eclipse.vjet.vjo.lib.LibManager;
 import org.eclipse.vjet.vjo.lib.TsLibLoader;
 import org.junit.Test;
 
 public class JsLangTypeSpaceTests {
 
 	
 	interface Locator {
 		URL resolve(URL url);
 	}
 
 	static class EclipseLocator implements Locator {
 		public URL resolve(URL url) {
 			try {
 				return org.eclipse.core.runtime.FileLocator.resolve(url);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 	
 	
 	@Test
 	public void testJsLangLoad() throws Exception {
 		
 		try{
 			
 		IJstParseController controller = new JstParseController(new VjoParser());
 	
 		JstTypeSpaceMgr ts = new JstTypeSpaceMgr(controller, new DefaultJstTypeLoader());
 		//addTraceEvents(ts);
 		ts.initialize();
 		
 		IResourceResolver jstLibResolver = org.eclipse.vjet.dsf.jstojava.test.utils.JstLibResolver
 				.getInstance()
 				.setSdkEnvironment(
 						new org.eclipse.vjet.dsf.jstojava.test.utils.VJetSdkEnvironment(
 								new String[0], "DefaultSdk"));
 
 		LibManager.getInstance().setResourceResolver(jstLibResolver);
 
 		
 		TsLibLoader.loadDefaultLibs(ts);
 //		printTypes(ts);
 		
 		URL url = this.getClass().getClassLoader().getResource("dsf/jslang/feature/tests/EcmaArrayTests.js");
 		if(url.getProtocol().startsWith("bundleresource")){
 			url = new EclipseLocator().resolve(url);
 		}
 		String path = url.getFile();
 		System.out.println("path = " + path);
 
 		int end = path.indexOf("dsf/jslang/feature/tests/"); 
 		String groupFullPath =  path.substring(0, end-1);
 		System.out.println("groupFullPath = " + groupFullPath);
 		int lastSlashIdx = groupFullPath.lastIndexOf("/");
 		String groupPath = groupFullPath.substring(0, lastSlashIdx+1);
 		String srcPath = groupFullPath.substring(lastSlashIdx+1);
 		System.out.println("srcPath = " + srcPath);
 		ts.processEvent(new AddGroupEvent("org.eclipse.vjet.test.core.jstojava", groupPath, srcPath, null));
 		
 		
 //		TypeName typeName = new TypeName(JstTypeSpaceMgr.JS_NATIVE_GRP, "Array");
 //		IJstType type = ts.getQueryExecutor().findType(typeName);
 //		assertNotNull(type);
 		
 		TypeName typeName = new TypeName("org.eclipse.vjet.test.core.jstojava", "dsf.jslang.feature.tests.EcmaArrayTests");
 		IJstType type = ts.getQueryExecutor().findType(typeName);
 		Map<String, IJstType> entities = ts.getTypeSpace().getGroup("org.eclipse.vjet.test.core.jstojava").getEntities();
 		Set<String> types = entities.keySet();
 		int groupSize = types.size();
 		System.out.println("number of types in typespace: " + groupSize);
		assertEquals(1831, groupSize);
 		
 		Set<IJstType> notResolved = new HashSet<IJstType>();
 		for(String typeN: types){
 			IJstType ijsttype = entities.get(typeN);
 			
 			if(ijsttype instanceof JstType){
 				JstType jsttype = (JstType)ijsttype;
 				if(!jsttype.getStatus().hasResolution()){
 					notResolved.add(jsttype);
 					System.out.println("not resolved: " + jsttype.getName());
 				}
 			}
 			
 			
 		}
 			assertEquals(1, notResolved.size()); // expect org.eclipse.vjet.dsf.jst.validation.vjo.rt.etype.BadEType1."a" to not be resolved
 		
 		printTypes(ts);
 		assertNotNull(type);
 		}
 		catch(Exception e){
 			e.printStackTrace();
 			fail();
 		}
 		
 		
 	}
 
 	private void addTraceEvents(JstTypeSpaceMgr ts) {
 	
 		ts.registerSourceEventListener(new	ITypeEventListener<IJstType>(){
 
 			@Override
 			public EventListenerStatus<IJstType> onTypeAdded(
 					AddTypeEvent<IJstType> event, IEventListenerHandle handle,
 					ISourceEventCallback<IJstType> callBack) {
 
 				System.out.println("onTypeAdded add dependendency" + event.getTypeName().typeName());
 				return null;
 			}
 
 			@Override
 			public EventListenerStatus<IJstType> onTypeModified(
 					ModifyTypeEvent event, IEventListenerHandle handle,
 					ISourceEventCallback<IJstType> callBack) {
 				System.out.println("onTypeModified add dependendency" + event.getTypeName().typeName());
 				return null;
 			}
 
 			@Override
 			public EventListenerStatus<IJstType> onTypeRemoved(
 					RemoveTypeEvent event, IEventListenerHandle handle,
 					ISourceEventCallback<IJstType> callBack) {
 				System.out.println("onTypeRemoved add dependendency" + event.getTypeName().typeName());
 					return null;
 			}
 
 			@Override
 			public EventListenerStatus<IJstType> onTypeRenamed(
 					RenameTypeEvent event, IEventListenerHandle handle,
 					ISourceEventCallback<IJstType> callBack) {
 				System.out.println("onTypeRenamed add dependendency" + event.getTypeName().typeName());
 					return null;
 			}
 			
 		});
 		
 			ts.registerSourceEventListener(new IGroupEventListener<IJstType>() {
 
 				@Override
 				public EventListenerStatus<IJstType> onBatchGroupLoaded(
 						BatchGroupLoadingEvent event,
 						IEventListenerHandle handle,
 						ISourceEventCallback callBack) {
 					System.out.println("on batch group add dependendency");
 					return null;
 				}
 
 				@Override
 				public EventListenerStatus onGroupAddDependency(
 						AddGroupDependencyEvent event,
 						IEventListenerHandle handle,
 						ISourceEventCallback callBack) {
 					// TODO Auto-generated method stub
 					System.out.println("onGroupAddDependency for group:" + event.getGroupName());
 					System.out.println("\tgroup path: " + event.getGroupPath());
 					System.out.println("\tdependency list: " + event.getDependencyList());
 					return null;
 				}
 
 				@Override
 				public EventListenerStatus onGroupAdded(AddGroupEvent event,
 						IEventListenerHandle handle,
 						ISourceEventCallback callBack) {
 					// TODO Auto-generated method stub
 					System.out.println("on group added");
 					System.out.println("bootstrap paths:" + event.getBootStrapList());
 					System.out.println(event);
 					return null;
 				}
 
 				@Override
 				public EventListenerStatus onGroupRemoveDependency(
 						RemoveGroupDependencyEvent event,
 						IEventListenerHandle handle,
 						ISourceEventCallback callBack) {
 					System.out.println("on group remove dependency");
 					System.out.println(event);
 					return null;
 				}
 
 				@Override
 				public void onGroupRemoved(RemoveGroupEvent event) {
 					// TODO Auto-generated method stub
 					System.out.println("on group removed");
 					System.out.println(event);
 				}
 				
 			});
 		}
 		
 	
 
 	private void printTypes(JstTypeSpaceMgr ts) {
 		ITypeSpace<IJstType, IJstNode> tsds = ts.getTypeSpace();
 		Map<TypeName,IJstType> types = tsds.getTypes();
 		for(TypeName type:types.keySet()){
 			System.out.println(type + ":");
 		//	System.out.print("pkg= " + types.get(type).getPackage());
 			System.out.println("\ttype=" + types.get(type).getName() );
 			System.out.println("\talias=" + types.get(type).getAlias() );
 		}
 	}
 	
 
 	
 	
 }
