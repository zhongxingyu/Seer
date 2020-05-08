 package com.rameses.rcp.util;
 
 import com.rameses.common.MethodResolver;
 import com.rameses.rcp.control.XButton;
 import com.rameses.rcp.framework.*;
 import com.rameses.rcp.ui.UICommand;
 
 import com.rameses.rcp.common.Action;
 import com.rameses.util.BusinessException;
 import com.rameses.util.ExceptionManager;
 import com.rameses.util.ValueUtil;
 import java.beans.Beans;
 import java.lang.reflect.Method;
 import javax.swing.JComponent;
 
 /**
  *
  * @author jaycverg
  */
 public class UICommandUtil {
     
     public static void processAction(UICommand command) 
     {
         if ( Beans.isDesignTime() ) return;
 
         try 
         {
             ClientContext ctx = ClientContext.getCurrentContext();
             MethodResolver resolver = MethodResolver.getInstance();
             Binding binding = command.getBinding();
             
             binding.formCommit();
             validate(command, binding);
             
             String target = ValueUtil.isEmpty(command.getTarget())? "parent": command.getTarget();
             NavigatablePanel navPanel = UIControlUtil.getParentPanel((JComponent)command, target);
             if ( !"parent".equals(target) ) {
                 UIControllerContext rootCon = (UIControllerContext) navPanel.getControllers().peek();
                 Binding rootBinding = rootCon.getCurrentView().getBinding();
                 validate(command, rootBinding);
             }
             
             //set parameters
             XButton btn = (XButton) command;
             ControlSupport.setProperties( binding.getBean(), btn.getParams());
             
             //notify handlers who hooked before execution
             binding.getActionHandlerSupport().fireBeforeExecute();
             
             Object outcome = null;
             String action = command.getActionName();
             if ( btn.getClientProperty(Action.class.getName()) != null ) 
             {
                 Action a = (Action) btn.getClientProperty(Action.class.getName());
                 outcome = a.execute();                
             } 
             else if ( action != null ) 
             {
                 if ( !action.startsWith("_")) 
                 {
                     Object[] actionParams = new Object[]{};
                     Object actionInvoker = btn.getClientProperty("Action.Invoker");
                     if (actionInvoker != null) actionParams = new Object[]{ actionInvoker };
                     
                     if (hasMethod(binding.getBean(), action, actionParams))
                         outcome = resolver.invoke(binding.getBean(), action, actionParams);
                     else 
                         outcome = resolver.invoke(binding.getBean(), action, null, null); 
                 } 
                 else { 
                     outcome = action;
                 }
                 
                 if ( command.isUpdate() ) binding.update();
             }
             
             //notify handlers who hooked after execution
             binding.getActionHandlerSupport().fireAfterExecute(); 
             
             NavigationHandler handler = ctx.getNavigationHandler();
             if ( handler != null ) handler.navigate(navPanel, command, outcome);
         } 
         catch(Exception ex) 
         {
             ex.printStackTrace();
             
             Exception e = ExceptionManager.getOriginal(ex);            
             if (!ExceptionManager.getInstance().handleError(e))
                 ClientContext.getCurrentContext().getPlatform().showError((JComponent) command, ex);
         }
     }
     
     private static void validate(UICommand command, Binding binding) throws BusinessException 
     {
         if ( binding == null ) return;
         if ( !command.isUpdate() && command.isImmediate() ) return;
         
         binding.validate();
     }
     
     private static boolean hasMethod(Object bean, String name, Object[] args) 
     {
         if (bean == null || name == null) return false;
         
         Class beanClass = bean.getClass();
         while (beanClass != null) 
         {
             Method[] methods = beanClass.getMethods(); 
             for (int i=0; i<methods.length; i++) 
             {
                 Method m = methods[i];
                 if (!m.getName().equals(name)) continue;
 
                 int paramSize = (m.getParameterTypes() == null? 0: m.getParameterTypes().length); 
                 int argSize = (args == null? 0: args.length); 
                if (paramSize == argSize && paramSize == 0) return true;
                if (paramSize == argSize && m.getParameterTypes()[0] == Object.class) return true; 
             }
             beanClass = beanClass.getSuperclass(); 
         }
         return false;
     }
 }
