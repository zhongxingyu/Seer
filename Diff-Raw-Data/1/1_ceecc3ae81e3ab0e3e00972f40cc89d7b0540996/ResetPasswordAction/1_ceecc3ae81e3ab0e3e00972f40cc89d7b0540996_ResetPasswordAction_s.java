 package jpaoletti.jpm.struts.actions;
 
 import jpaoletti.jpm.core.PMException;
 import jpaoletti.jpm.security.core.operations.ResetPassword;
 import jpaoletti.jpm.struts.PMStrutsContext;
 
 /**
  *
  * @author jpaoletti
  */
 public class ResetPasswordAction extends ActionSupport {
 
     @Override
     protected void doExecute(PMStrutsContext ctx) throws PMException {
         final ResetPassword op = new ResetPassword("resetpsw");
         op.execute(ctx);
     }
 }
