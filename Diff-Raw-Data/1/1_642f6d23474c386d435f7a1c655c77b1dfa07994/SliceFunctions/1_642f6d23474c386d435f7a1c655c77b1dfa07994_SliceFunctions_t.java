 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package com.ibm.wala.ipa.slicer;
 
 import com.ibm.wala.dataflow.IFDS.IFlowFunction;
 import com.ibm.wala.dataflow.IFDS.IFlowFunctionMap;
 import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
 import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
 import com.ibm.wala.util.debug.Assertions;
 
 /**
  * flow functions for FSCS slicer
  * 
  * @author sjfink
  * 
  */
 public class SliceFunctions implements IFlowFunctionMap<Statement> {
 
   public IUnaryFlowFunction getCallFlowFunction(Statement src, Statement dest) {
     return ReachabilityFunctions.singleton().getCallFlowFunction(src, dest);
   }
 
   public IUnaryFlowFunction getCallNoneToReturnFlowFunction(Statement src, Statement dest) {
     if (src == null) {
       throw new IllegalArgumentException("src is null");
     }
     Statement s = src;
     switch (s.getKind()) {
     case NORMAL_RET_CALLER:
     case PARAM_CALLER:
    case EXC_RET_CALLER:
       // uh oh.  anything that flows into the missing function will be killed.
       return ReachabilityFunctions.killReachability;
     case HEAP_PARAM_CALLEE:
     case HEAP_PARAM_CALLER:
     case HEAP_RET_CALLEE:
     case HEAP_RET_CALLER:
       if (dest instanceof HeapStatement) {
         HeapStatement hd = (HeapStatement)dest;
         HeapStatement hs = (HeapStatement)src;
         if (hs.getLocation().equals(hd.getLocation())) {
           return IdentityFlowFunction.identity();
         } else {
           return ReachabilityFunctions.killReachability;
         }
       } else {
         return ReachabilityFunctions.killReachability;
       }
     case NORMAL:
       // only control dependence flows into the missing function.
       // this control dependence does not flow back to the caller.
       return ReachabilityFunctions.killReachability;
     default: 
       Assertions.UNREACHABLE(s.getKind().toString());
       return null;
     }
   }
 
   public IUnaryFlowFunction getCallToReturnFlowFunction(Statement src, Statement dest) {
     return ReachabilityFunctions.singleton().getCallToReturnFlowFunction(src, dest);
   }
 
   public IUnaryFlowFunction getNormalFlowFunction(Statement src, Statement dest) {
     return ReachabilityFunctions.singleton().getNormalFlowFunction(src, dest);
   }
 
   public IFlowFunction getReturnFlowFunction(Statement call, Statement src, Statement dest) {
     return ReachabilityFunctions.singleton().getReturnFlowFunction(call, src, dest);
   }
 
 }
