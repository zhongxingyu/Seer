 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid Federation (ESGF) Data Node Software   *
 *  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid Federation (ESGF) Data Node Software Stack           *
 *                                                                          *
 *   For details, see http://esgf.org/esg-node/                             *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                               *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 package esg.node.core;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 
 
 /**
    <pre>
    Description:
 
    This class describes an event that knows how to transition from one
    event queue to another (from component to component).  This object
    traverses components, essentially having it's own internal route
    through components.
    </pre>
 */
 public class ESGCallableRoutableFutureEvent<T> extends ESGCallableFutureEvent<T> {
 
     private static final Log log = LogFactory.getLog(ESGCallableFutureEvent.class);
 
     private Map<String,ESGCallable> routeTable = null;
     private List<String> routeList = null;
     private Map<String,T> resultDataMap = null;
     private boolean handled = true;
 
     public ESGCallableRoutableFutureEvent(Object source) { super(source,null,null); }
     public ESGCallableRoutableFutureEvent(Object source, String message) { super(source,null,message); }
     public ESGCallableRoutableFutureEvent(Object source, Object data, String message) { super(source, data, message); }
 
     public void setRoute(String... componentNames) {
        if (null == routeList) { routeList = new ArrayList<String>(componentNames.length); }
         for(String componentName : componentNames) {
             routeList.add(componentName);
         }
     }
     public void setRoute(List<String> routeList) {
         this.routeList = routeList;
     }
 
     public String getRouteAsString() {
         if(null == routeList) { return "<empty>"; }
         StringBuffer sb = new StringBuffer();
         for(int i=0, size = routeList.size(); i < size; i++) {
             sb.append(routeList.get(i));
             if(i != (size-1)) sb.append(" -> ");
         }
         return sb.toString();
     }
     public List<String> getRouteAsList() { return routeList; }
    public int getNumHops() { return (routeList != null ? routeList.size() : 0); }
 
     public void associateCallable(String componentName, ESGCallable esgCallable) {
         if (routeTable == null) {routeTable = new HashMap<String,ESGCallable>();}
         routeTable.put(componentName,esgCallable);
     }
 
     public Set<String> getAssociatedComponentNames() { return routeTable.keySet(); }
 
     public boolean call(DataNodeComponent contextComponent) {
         log.warn("I SHOULD NOT BE HERE!!!");
         return false;
     }
 
     @SuppressWarnings("unchecked")
     public boolean doCall(DataNodeComponent contextComponent) {
         try{
             log.trace("routable doCall...");
             String nextComponentName = null;
             String currentComponentName = null;
             if ((currentComponentName = routeList.get(0)).equals(contextComponent.getName())) {
                 log.info("Calling callable on "+currentComponentName);
                 handled &= (routeTable.get(currentComponentName)).call(contextComponent);
 
                 routeList.remove(0); //pop this component off queue
                 //------------------------------------------------
                 //check if I have exhausted the route table... if so set result and return
                 //------------------------------------------------
                 if(routeList.isEmpty()) {
                     log.info("Traversed all components - setting result and returning");
                     //countdown the latch and release thread blocked on get() and return data <T>
                     setResult((T)Boolean.valueOf(handled));
                     clear();
                     return true;
                 }
 
                 //------------------------------------------------
                 //put myself on the queue of the next component...
                 //------------------------------------------------
                 nextComponentName = routeList.get(0);
                 log.info("Jumping to the next component in route: "+nextComponentName);
                 contextComponent.getDataNodeManager().getComponent(nextComponentName).getESGEventQueue().enqueueEvent(this);
             }else{
                 log.error("Component mismatch: [ "+currentComponentName+" != "+contextComponent.getName()+" ]");
                 setResult((T)Boolean.FALSE);
                 clear();
             }
         }catch(Throwable t) {
             if(routeList == null) { log.error("routeList was not set!! ["+routeList+"]"); }
             log.error(t);
             setResult((T)Boolean.FALSE); //No matter what let this thread go... do not keep caller blocked!!!
             clear();
         }
 
         return false;
     }
 
     //-----------------------------------
     //Do some cleanup...  be good to memory
     //-----------------------------------
     private void clear() {
         if (null != routeTable) {
             routeTable.clear();
             routeTable = null; //gc niceness...
         }
         if (null != routeList) {
             routeList.clear();
             routeList = null; //gc niceness...
         }
         //other cleanup... try to let go of all references explicitly (perhaps overkill but still)
         setSource(null);
         setData(null);
         setMessage(null);
     }
 
 }
