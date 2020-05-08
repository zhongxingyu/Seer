 /*******************************************************************************
  * Copyright (c) 2012 Andreas Engelbredt Dalsgaard <andreas.dalsgaard@gmail.com>.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Andreas Engelbredt Dalsgaard <andreas.dalsgaard@gmail.com>
  ******************************************************************************/
 package analyser;
 
 import com.ibm.wala.classLoader.CallSiteReference;
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.Context;
 import com.ibm.wala.ipa.callgraph.ContextSelector;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
 import com.ibm.wala.ipa.cha.ClassHierarchy;
 import com.ibm.wala.util.intset.EmptyIntSet;
 import com.ibm.wala.util.intset.IntSet;
 import java.util.UUID;
 
 
 public class ScjContextSelector implements ContextSelector {
 		
 	private IClass missionIClass = null;
	private IClass AEHIClass = null;	
 	int counter = 0;	
 	private ClassHierarchy cha;
 	private IClass ManagedMemoryIClass;
 	private IClass MemoryAreaIClass;	
 	
 	public ScjContextSelector(ClassHierarchy cha) 
 	{
 		this.cha = cha;
 		this.missionIClass = util.getIClass("Ljavax/safetycritical/Mission", cha);
 				
 		if (this.missionIClass == null)
 			throw new IllegalArgumentException("No mission class in ClassHierarchy");
 		
		this.AEHIClass = util.getIClass("Ljavax/realtime/AbstractAsyncEventHandler", cha);
 		this.ManagedMemoryIClass  = util.getIClass("Ljavax/safetycritical/ManagedMemory", cha);		
 		this.MemoryAreaIClass  = util.getIClass("Ljavax/realtime/MemoryArea", cha);		
 	}
 	
 	public Context getCalleeTarget(CGNode caller, CallSiteReference site,
 			IMethod callee, InstanceKey[] actualParameters) 
 	{	
 		ScjContext calleeContext;		
 	
 		// Handles the first nodes that is called from the synthetic fakeRoot
 		if (!(caller.getContext() instanceof ScjContext))		
 		{			
 			return new ScjContext(null, "Ljavax/realtime/ImmortalMemory", ScjScopeType.IMMORTAL);
 		}		
 		
 		calleeContext = (ScjContext) caller.getContext();
 				
		if (isSubclassOf(callee, this.AEHIClass) && 
 				isFuncName(callee, "handleAsyncEvent")) 
 		{
 			calleeContext = new ScjContext(calleeContext, callee.getDeclaringClass().getName().toString(), ScjScopeType.PM);
 		} else if (isSubclassOf(callee,this.ManagedMemoryIClass)) 
 		{			
 			if (isFuncName(callee, "enterPrivateMemory")) {
 				calleeContext = new ScjContext(calleeContext, getUniquePMName(), ScjScopeType.PM);
 			} else if (isFuncName(callee, "getCurrentManagedMemory")) {
 				((ScjContext)caller.getContext()).setLastGetCurrentScope(calleeContext.getStackTop());
 			}
 		} else if(isSubclassOf(callee,this.MemoryAreaIClass)) 
 		{					
 			if (isFuncName(callee, "executeInArea"))
 			{				
 				calleeContext = new ScjContext(calleeContext, calleeContext.getLastGetCurrentScope().getName().toString(), calleeContext.getLastGetCurrentScope().getScopeType()); 
 			} else if (isFuncName(callee, "getMemoryArea"))
 			{
 					((ScjContext)caller.getContext()).setLastGetCurrentScope(calleeContext.getStackTop());
 			}
 		} else if (isFuncName(callee, "startMission") && callee.getDeclaringClass().getName().toString().equals("Ljavax/safetycritical/JopSystem")) 
 		{						
 				calleeContext = new ScjContext(calleeContext, 
 						caller.getMethod().getDeclaringClass().getName().toString(), ScjScopeType.MISSION);
 		} 
 					
 		return calleeContext;
 	}
 	
 	public boolean isSubclassOf(IMethod callee, IClass parent)
 	{
 		if (parent != null)			
 			return this.cha.isSubclassOf(callee.getDeclaringClass(), parent);
 		return false;		
 	}
 	
 	public boolean isFuncName(IMethod callee, String str)
 	{
 		return callee.getName().toString().equals(str);
 	}
 	
 	public String getUniquePMName()
 	{
 		return UUID.randomUUID().toString();
 	}
 	
 	public IntSet getRelevantParameters(CGNode caller, CallSiteReference site) {
 		return EmptyIntSet.instance;
 	}	
 }
