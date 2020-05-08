 package jpaoletti.jpm.struts.tags;
 
 import java.io.IOException;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import jpaoletti.jpm.core.Entity;
 import jpaoletti.jpm.core.Operation;
 import jpaoletti.jpm.core.Operations;
 import jpaoletti.jpm.core.PMContext;
 import jpaoletti.jpm.core.PMSession;
 import jpaoletti.jpm.core.PresentationManager;
 import jpaoletti.jpm.core.operations.OperationCommandSupport;
 import jpaoletti.jpm.struts.PMStrutsConstants;
 
 /**
  * Display an html div bar with the operations
  *
  * @author jpaoletti
  * @since 15/09/2011
  * @version v1.2
  *
  */
 /**
  * Display an html div bar with the operations
  *
  * @author jpaoletti
  * @since 15/09/2011
  * @version v1.2
  *
  */
 public class OperationsTag extends PMTags {
 
     private boolean labels = true;
     private PMContext ctx;
     private Operations operations;
 
     @Override
     public int doStartTag() throws JspException {
         try {
             final String eid = (getEntity() != null) ? getEntity().getId() : "";
             final StringBuilder script = new StringBuilder("<script type='text/javascript'>\n");
             script.append("PM_register(function(){\n");
             script.append("    $('#jpm_btn_back_").append(eid).append("').click(function(){\n");
            script.append("        loadPage(document.referrer);\n");
             script.append("    }).button({\n");
             script.append("        text: false, icons: {primary: 'ui-icon-arrowthickstop-1-w'}\n");
             script.append("    });\n");
             script.append("    $('#jpm_btn_refresh_").append(eid).append("').click(function(){\n");
             script.append("        location.reload(true);\n");
             script.append("    }).button({\n");
             script.append("        text: false, icons: {primary: 'ui-icon-refresh'}\n");
             script.append("    });\n");
 
             println("<div class='ui-widget-header ui-corner-all'>");
             println("<button id='jpm_btn_back_" + eid + "'>" + PresentationManager.getMessage("pm.title.back") + "</button>");
             println("<button id='jpm_btn_refresh_" + eid + "'>" + PresentationManager.getMessage("pm.title.refresh") + "</button>");
 
             if (getOperations() != null && getOperations().getOperations() != null && !getOperations().getOperations().isEmpty()) {
                 for (Operation operation : getOperations().getOperations()) {
                     if (getPmsession().getUser().hasPermission(operation.getPerm())) {
                         processOperation(operation, script);
                     }
                 }
             }
 
             println("</div>");
             script.append("\n});</script>");
             println(script);
         } catch (Exception ex) {
             throw new JspTagException("OperationsTag: " + ex.getMessage());
         }
         return SKIP_BODY;
     }
 
     @Override
     public int doEndTag() {
         return EVAL_PAGE;
     }
 
     public Operations getOperations() {
         if (operations != null) {
             return operations;
         } else {
             return (Operations) ctx.get(PMStrutsConstants.OPERATIONS);
         }
     }
 
     public PMSession getPmsession() {
         return ctx.getPmsession();
     }
 
     private void processOperation(Operation operation, StringBuilder script) throws IOException {
         final String opid = operation.getId();
         final String jqItem = "    $('#operation" + opid + "')";
         final String item = getCtx().getString(OperationCommandSupport.PM_ITEM);
         final String hreff = (operation.getUrl() != null)
                 ? operation.getUrl()
                 : getContextPath() + "/" + opid + ".do"
                 + "?pmid=" + getEntity().getId()
                 + ((item != null) ? "&item=" + item : "");
 
         script.append(jqItem);
         script.append(".click(function(){");
         if (operation.isAvailable()) {
             if (operation.getConfirm()) {
                 script.append("loadPageConfirm");
             } else {
                 script.append("loadPage");
             }
             script.append("('").append(hreff).append("');");
         } else {
             script.append("alert('").append(PresentationManager.getMessage("operation.not.available", operation.getTitle())).append("');");
         }
         script.append("}).button();\n");
 
         final String style = "\"background-image: url('" + getContextPath() + "/templates/" + getTemplate() + "/img/" + opid + ".gif'); background-position: 5% 50%; background-repeat: no-repeat;\"";
         print("<button class='button' style=" + style + " id='operation" + opid + "'>&nbsp;");
         if (isLabels()) {
             print(operation.getTitle());
         }
         println("</button>");
     }
 
     public boolean isLabels() {
         return labels;
     }
 
     public void setLabels(boolean labels) {
         this.labels = labels;
     }
 
     public Entity getEntity() {
         return ctx.getEntity();
     }
 
     public PMContext getCtx() {
         return ctx;
     }
 
     public void setCtx(PMContext ctx) {
         this.ctx = ctx;
     }
 
     public void setOperations(Operations operations) {
         this.operations = operations;
     }
 }
