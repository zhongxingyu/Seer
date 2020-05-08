 /*
  * PageFlowController.java
  *
  * Created on November 5, 2012, 8:37 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.osiris2.common;
 
 
 import com.rameses.common.ExpressionResolver;
 import com.rameses.osiris2.client.*;
 import com.rameses.osiris2.flow.AbstractNode;
 import com.rameses.osiris2.flow.SubProcessNode;
 import com.rameses.osiris2.flow.Transition;
 import com.rameses.rcp.annotations.Binding;
 import com.rameses.rcp.annotations.Caller;
 import com.rameses.rcp.annotations.Controller;
 import com.rameses.rcp.annotations.Invoker;
 import com.rameses.rcp.common.Action;
 import com.rameses.rcp.common.MsgBox;
 import com.rameses.rcp.common.StyleRule;
 import com.rameses.rcp.util.ControlSupport;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Elmo
  */
 public class PageFlowController 
 {
     @Controller
     protected WorkUnitUIController workunit;
     
     @Binding
     private com.rameses.rcp.framework.Binding binding;
     
     @Caller
     private Object caller;
     
     @Invoker
     protected com.rameses.osiris2.Invoker invoker;
     
     public PageFlowController() {
     }
     
     public Object start() {
         workunit.getWorkunit().start();
         return workunit.getWorkunit().getCurrentPage().getName();
     }
     
     public Object start(String name) {
         workunit.getWorkunit().start(name);
         return workunit.getWorkunit().getCurrentPage().getName();
     }
     
      public Object signal() {
         return signal(null);
     }
      
     public Object signal(String msg) {
         return signal(msg,null);
     } 
     
     private final PageFlowController getRootCaller() {
        if(caller==null)
             return this;
        else {
            return ((PageFlowController)caller).getRootCaller();
        }
     }
     
     private final void buildStackClosed(StringBuilder sb) {
         if(caller!=null) {
             sb.insert(0, "_close:");
             ((PageFlowController)caller).buildStackClosed( sb );
         }
     }
     
     public Object signal(String msg, String tag) {
         if(msg==null)
             workunit.getWorkunit().signal();
         else
             workunit.getWorkunit().signal(msg);
         
         if(workunit.getWorkunit().isPageFlowCompleted()) {
             if(caller!=null && (caller instanceof PageFlowController)) {
                 Object _out = null;
                 if(tag!=null) {
                     _out = ((PageFlowController)caller).getRootCaller().signal(tag);    
                 }
                 else {
                     _out =  ((PageFlowController)caller).getRootCaller().signal();    
                 }
                 if(_out!=null) {
                     if(_out instanceof String) {
                         StringBuilder sb = new StringBuilder(_out.toString());
                         buildStackClosed(sb);
                         return sb.toString();
                     }
                     else {
                         return _out;
                     }
                 }
             }
             return "_close";
         }
         AbstractNode node = workunit.getWorkunit().getCurrentNode();
         if( node instanceof SubProcessNode) {
             SubProcessNode pm = (SubProcessNode)node;
             String openerName = pm.getHandler();
             if(openerName ==null)
                 throw new IllegalStateException("Please specify a handler in subprocess node"); 
             
             Object bean = workunit.getWorkunit().getController();
             Object o = ExpressionResolver.getInstance().eval( openerName,bean );            
             if (o instanceof String) { 
                 try { 
                     return InvokerUtil.lookupOpener((String)o); 
                 } catch(Throwable t) {
                     System.out.println("[WARN] error lookup actions caused by " + t.getMessage());
                 }
             }
             return o;
         }
         
         String pageName = node.getName();
         if(pageName==null) {
             throw new RuntimeException("Page name must not be null");
         }
         return pageName;
     }
     
    
     // <editor-fold defaultstate="collapsed" desc=" Form and Navigation Actions ">  
     
     public List<Action> getFormActions() {
         return getActions();
     }
     
     public List<Action> getNavActions() 
     {
         try {
             return lookupActions("navActions");
         } catch(Exception ex) {
             return null; 
         } 
     } 
     
     protected final List<Action> lookupActions(String type)
     {        
         List<Action> actions = new ArrayList();
         try { 
             actions = InvokerUtil.lookupActions(type, new InvokerFilter() {
                 public boolean accept(com.rameses.osiris2.Invoker o) { 
                     return o.getWorkunitid().equals(invoker.getWorkunitid()); 
                 }
             }); 
         } catch(Throwable t) {
             System.out.println("[WARN] error lookup actions caused by " + t.getMessage());
         }
         
         for (int i=0; i<actions.size(); i++) 
         {
             Action newAction = actions.get(i).clone();
             actions.set(i, newAction);
         }
         return actions; 
     }    
     
     // </editor-fold>
     
     public StyleRule[] getStyleRules() {
         return null;
     }
     
     public List<Action> getActions() {
         List<Action> actions = new ArrayList();
         List<Transition> transitions = workunit.getWorkunit().getTransitions();
         for (Transition t: transitions) {
             String domain = t.getDomain();
             if (domain == null) domain = workunit.getWorkunit().getModule().getDomain();
             
             boolean permitted = ControlSupport.isPermitted(domain, t.getRole(), t.getPermission()); 
             if (!permitted) continue; 
             
             String tname = t.getName();
             if (tname == null) tname = t.getTo();
             
             TransitionAction a = new TransitionAction(tname);
             String sval = (String) t.getProperties().get("title");
             if (sval == null) sval = (String)t.getProperties().get("caption");
             if (sval == null) sval = tname; 
 
             a.setCaption(sval);
             a.setTooltip(sval);
             a.setConfirm(t.getConfirm());             
             
             sval = (String) t.getProperties().get("tag");            
             a.setTag(sval); 
             
             sval = (String) t.getProperties().get("icon");            
             if (sval != null) a.setIcon(sval);
             
             sval = (String)t.getProperties().get("visibleWhen");            
             if (sval != null) a.setVisibleWhen(sval);
             
             boolean immediate = true;
             try {
                 sval = (String)t.getProperties().get("immediate");
                 if (sval != null) immediate = Boolean.parseBoolean(sval);
             } catch(Throwable ex) { 
                 //do nothing 
             } finally { 
                 a.setImmediate(immediate);
             }
             
             sval = (String) t.getProperties().get("mnemonic"); 
             if (sval != null && sval.trim().length() > 0) {
                 a.setMnemonic(sval.trim().charAt(0)); 
             }
             
             a.setDomain(domain);
             a.setRole(t.getRole());
             a.setPermission(t.getPermission());            
             a.setShowCaption(true);
             actions.add(a);
         }
         return actions;
     }
     
     public class TransitionAction extends Action {
         private String confirm;
         private String tag;
         public TransitionAction(String name) {
             super(name);
         }
         public String getConfirm() {
             return confirm;
         }
         public void setConfirm(String confirm) {
             this.confirm = confirm;
         }
         public Object execute() {
             if( confirm !=null ) {
                 if( !MsgBox.confirm(confirm)) {
                     return null;
                 }
             }
             if(tag!=null)
                 return PageFlowController.this.signal( getName(), tag );
             else    
                 return PageFlowController.this.signal( getName() );
         }
 
         public String getTag() {
             return tag;
         }
 
         public void setTag(String tag) {
             this.tag = tag;
         }
     }
     
    
     
     public String getTitle() {
         String expr = workunit.getWorkunit().getStateTitle();
         Object bean = workunit.getWorkunit().getController();
         return ExpressionResolver.getInstance().evalString( expr, bean );
     }
 
     public String getState() {
         return workunit.getWorkunit().getCurrentNode().getName();
     }
     
     public Object getCaller() {
         return caller;
     }
 
     public com.rameses.rcp.framework.Binding getBinding() {
         return binding;
     }
     
 }
