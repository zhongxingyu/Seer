 /* EventDataManager.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		2012/3/22 Created by dennis
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.zkoss.zats.mimic.Agent;
 import org.zkoss.zats.mimic.operation.OperationAgent;
 import org.zkoss.zk.ui.event.Event;
 
 /**
  * This class maintain a list of {@link ValueResolver}. When it resolves a Agent, it calls each resolver in the list to resolve it.
  * @author dennis
  *
  */
 public class ValueResolverManager {
 	private static ValueResolverManager instance;
 	
 	public static synchronized ValueResolverManager getInstance(){
 		if(instance==null){
 			instance = new ValueResolverManager(); 
 		}
 		return instance;
 	}
	private List<ValueResolver> resolvers = new ArrayList<ValueResolver>();
 
 	public ValueResolverManager() {
 	
 		//ComponentAgent resolver
 		registerResolver("5.0.0","*",new ValueResolver(){
 			@SuppressWarnings("unchecked")
 			public <T> T resolve(Agent agent, Class<T> clazz) {
 				if (OperationAgent.class.isAssignableFrom(clazz)) {
 					Class<OperationAgent> opc = (Class<OperationAgent>) clazz;
 					OperationAgentBuilder<Agent, OperationAgent> builder = OperationAgentManager.getInstance().getBuilder(
 							agent.getDelegatee(), opc);
 					if (builder != null)
 						return (T) builder.getOperation(agent);
 				}
 				return null;
 			}
 		});
 		//ZK native component resolver
 		registerResolver("5.0.0","*",new ValueResolver(){
 			@SuppressWarnings("unchecked")
 			public <T> T resolve(Agent agent, Class<T> clazz) {
 				if (clazz.isInstance(agent.getDelegatee())) {
 					return (T) agent.getDelegatee();
 				}
 				return null;
 			}
 		});
 	}
 	
 	@SuppressWarnings({ "rawtypes"})
 	public void registerResolver(String startVersion, String endVersion, String resolverClazz) {
 		if (startVersion == null || endVersion == null || resolverClazz == null)
 			throw new IllegalArgumentException();
 		
 		if(!Util.checkVersion(startVersion,endVersion)) return;
 		ValueResolver resolver = null;
 		try{
 			Class buildClz = Class.forName(resolverClazz);
 			resolver = (ValueResolver)buildClz.newInstance();
 		}catch(Exception x){
 			throw new IllegalArgumentException(x.getMessage(),x);
 		}
 		
 		registerResolver(startVersion,endVersion,resolver);
 	}
 
 	
 	public <T extends Event> 
 		void registerResolver(String startVersion, String endVersion, ValueResolver resolver) {
 		
 		if (startVersion == null || endVersion == null || resolver==null)
 			throw new IllegalArgumentException();
 
 		if(!Util.checkVersion(startVersion,endVersion)) return;
 		resolvers.add(resolver);
 	}
 	
 	/**
 	 * resolve the component agent to a object with registered value resolver
 	 */
 	public <T> T resolve(Agent agent, Class<T> clazz){
 		for(ValueResolver r:resolvers){
 			T obj = r.resolve(agent, clazz);
 			if(obj!=null) return obj;
 		}
 		return null;
 	}
 }
