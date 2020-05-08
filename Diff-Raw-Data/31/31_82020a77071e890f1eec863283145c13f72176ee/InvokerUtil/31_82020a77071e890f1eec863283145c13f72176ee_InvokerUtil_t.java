 /*
  * InvokerUtil.java
  *
  * Created on October 27, 2009, 4:14 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.osiris2.client;
 
 import com.rameses.osiris2.SessionContext;
 import com.rameses.osiris2.Folder;
 import com.rameses.rcp.framework.UIController;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.osiris2.Invoker;
 import com.rameses.platform.interfaces.Platform;
 import com.rameses.rcp.common.Action;
 import com.rameses.rcp.framework.ControllerProvider;
 import com.rameses.rcp.framework.UIControllerContext;
 import com.rameses.rcp.framework.UIControllerPanel;
 import com.rameses.common.ExpressionResolver;
 import com.rameses.util.ExceptionManager;
 import com.rameses.util.ValueUtil;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author elmo
  */
 public final class InvokerUtil {
     
     public static void invoke(Invoker invoker) {
         invoke(invoker,null);
     }
     
    public static void showWindow(Invoker invoker, String target, Map winParams) {
        if ( !ValueUtil.isEmpty(target) ) {
            invoker.getProperties().put("windowmode", target);
        }
        if ( winParams != null ) {
            invoker.getProperties().putAll(winParams);
        }
        invoke(invoker, null);
    }
    
     public static void invoke(Invoker invoker, Map params) {
         try {
             //check if window already exists
             ClientContext ctx = ClientContext.getCurrentContext();
             Platform platform = ctx.getPlatform();
             
             String wuId = invoker.getWorkunitid();
             if ( platform.isWindowExists( wuId )) {
                 platform.activateWindow( wuId );
                 return;
             }
             
             ControllerProvider cp = ctx.getControllerProvider();
             UIController u = cp.getController( wuId );
             String action = invoker.getAction();
             u.setId( wuId );
             u.setName( wuId );
             u.setTitle( invoker.getCaption());
             String outcome = (String) u.init(params, action);
             String target = (String)invoker.getProperties().get("target");
             if( target == null ) target = "_window";
             if((target.endsWith("process")||target.endsWith("action"))) {
                 //do nothing
             } else {
                 UIControllerContext uic = new UIControllerContext( u );
                 if ( !ValueUtil.isEmpty(outcome) ) {
                     uic.setCurrentView(outcome);
                 }
                 UIControllerPanel panel = new UIControllerPanel( uic );
                 Map winParams = new HashMap();
                if ( invoker.getProperties() != null ) {
                    winParams.putAll( invoker.getProperties() );
                 }
                 winParams.put("id", u.getId());
                 winParams.put("title", u.getTitle());
                
                String windowmode = winParams.get("windowmode")+"";
                if ( !"popup".equals(windowmode) ) {
                    platform.showWindow(null, panel, winParams);
                } else {
                    platform.showPopup(null, panel, winParams);
                }
             }
         } catch(Exception ex) {
             Exception e = ExceptionManager.getInstance().getOriginal(ex);
             
             if ( !ExceptionManager.getInstance().handleError(e) ) {
                 ClientContext.getCurrentContext().getPlatform().showError(null, ex);
             }
         }
     }
     
     public static List lookup(String type) {
         return lookup(type,null);
     }
     
     
     /*
      * The object passed will be evaluated by the expression.
      * params = refer to Object parameter passed.
      * context = the invocation context.
      * a sample implementation as follows: #{param.name == context.module.name}
      */
     public static List lookup(String type, Object obj) {
         SessionContext app = OsirisContext.getSession();
         List list = app.getInvokers(type);
         if( obj == null) {
             return list;
         } else {
             List data = new ArrayList();
             ExpressionResolver er = ClientContext.getCurrentContext().getExpressionResolver();
             
             for(Object o: list) {
                 Invoker inv = (Invoker)o;
                 if( inv.getProperties().get("eval")!=null) {
                     Map params = new HashMap();
                     params.put("param", obj);
                     params.put( "context", inv );
                     
                     String cond = (String)inv.getProperties().get("eval");
                     if( cond.trim().length()>0) {
                         boolean b = false;
                         try {
                             b = ((Boolean)er.evaluate(params, cond)).booleanValue();
                         } catch(Exception ign){;}
                         if(b) {
                             data.add(inv);
                         }
                     }
                 } else {
                     data.add(inv);
                 }
             }
             return data;
         }
     }
     
     /**
      * This method returns a list of invokers from a folder path.
      */
     public static List lookupFolder( String name ) {
         if(name == null ) return null;
         if( !name.startsWith("/")) name = "/" + name;
         List invokers = new ArrayList();
         SessionContext app = OsirisContext.getSession();
         List items = (List) app.getFolders(name);
         if(items!=null) {
             for (Object o : items) {
                 Folder f = (Folder) o;
                 if (f.getInvoker() != null) {
                     Invoker v = f.getInvoker();
                     invokers.add(v);
                 }
             }
         }
         return invokers;
     }
     
     /**
      * This method returns a list of actions (used for example by XActionBar).
      * The specified invoke action and the invoke parameter must be specified.
      * if not, invoke action default is "invoke" and invoke target is "invoker"
      *
      * there should be a method named <invokeAction> in the calling controller
      * there should be a setter method named as set<invokeTarget> which accepts an Invoker
      */
     public static List<Action> getInvokerActions(List<Invoker> invokerList, String invokeAction, String invokeTarget  ) {
         if(invokeAction ==null) invokeAction = "invoke";
         if(invokeTarget ==null) invokeAction = "invoker";
         List<Action> list = new ArrayList<Action>();
         for( Invoker i: invokerList ) {
             Map params = new HashMap();
             params.put(invokeTarget, i);
             Action a = new Action(invokeAction, i.getCaption(), (String)i.getProperties().get("icon"));
             a.setParameters(params);
             list.add(a);
         }
         return list;
     }
     
     
     
 }
