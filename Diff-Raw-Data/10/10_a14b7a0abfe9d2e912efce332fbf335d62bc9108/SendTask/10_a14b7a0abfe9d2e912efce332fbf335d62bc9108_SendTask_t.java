 package org.levi.engine.impl.bpmn;
 
 import org.levi.engine.bpmn.RunnableFlowNode;
 import org.levi.engine.mail.MailClient;
 import org.levi.engine.persistence.hibernate.HibernateDao;
 import org.levi.engine.persistence.hibernate.process.hobj.ProcessInstanceBean;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.runtime.ProcessInstance;
 import org.omg.spec.bpmn.x20100524.model.TSendTask;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.mail.MessagingException;
 import java.util.Date;
 
 public class SendTask extends RunnableFlowNode {
     private final TSendTask task;
     private final ProcessInstance processInstance;
     // private String from;
 
 
     public static class Builder {
         private TSendTask task;
         private ProcessInstance process;
 
         public Builder(TSendTask task) {
             this.task = task;
         }
 
         public Builder processInstance(ProcessInstance process) {
             this.process = process;
             return this;
         }
 
         public SendTask build() {
             return new SendTask(this);
         }
     }
 
     private SendTask(Builder builder) {
         this.task = builder.task;
         this.processInstance = builder.process;
         //TResourceRole[] resourceRoles = task.getResourceRoleArray();
         //if (resourceRoles[0] instanceof TPotentialOwner) {
         //    TPotentialOwner potentialOwner = (TPotentialOwner)resourceRoles[0];
         //    potentialOwner.getResourceAssignmentExpression().getExpression();
         //}
         //THumanPerformer humanPerformer = (THumanPerformer)resourceRoles[1];
         //humanPerformer.getResourceAssignmentExpression().getExpression();
 
         //hasInputForm = task.getInputForm() != null;
 
         // todo check and write the input form data.
         //if (hasInputForm()) {
         persistSendTask(this);
         //}
     }
 
     private void persistSendTask(SendTask sendTask) {
         HibernateDao dao = new HibernateDao();
         // todo remove later
 //        processInstance.setVariable("recipient", "eranda");
 //        processInstance.setVariable("orderId", 1234);
 //        processInstance.setVariable("male", true);
 //        processInstance.setVariable("recipientName", "Eranda");
         processInstance.setVariable("now", new Date());
         TaskBean starteventbean = (TaskBean) dao.getObject(TaskBean.class, sendTask.getId());
         if (starteventbean == null) {
             starteventbean = new TaskBean();
             Node exElems = task.getExtensionElements().getDomNode();
             NodeList children = exElems.getChildNodes();
             for (int i = 0; i < children.getLength(); ++i) {
                 Node field = children.item(i);
                 if (field.getNodeType() != Node.ELEMENT_NODE) {
                     continue;
                 }
                 if (!"field".equals(field.getNodeName())) {
                     throw new RuntimeException("Unknown element '" + field.getNodeName()
                             +"' inside sendTask.");
                 }
                 Node nameAttribute = field.getAttributes().getNamedItem("name");
                 String key = nameAttribute.getNodeValue();
                 // key must be one of {from, to, subject, content}. we must handle this
                 // from the xsd.
                 if (!("from".equals(key) || "to".equals(key)
                         || "subject".equals(key) || "content".equals(key))) {
                     throw new RuntimeException("Field name must be one of {from, to, subject, content}. " + key);
                 }
                 Node stringValueAttribute = field.getAttributes().getNamedItem("stringValue");
                 Node expressionAttribute = field.getAttributes().getNamedItem("expression");
                 String value = null;
                 if (stringValueAttribute != null) {
                     if (expressionAttribute != null) {
                         throw new RuntimeException("Cant have values for both StringValue or Expression attributes");
                     }
                     value = stringValueAttribute.getNodeValue();
                 } else if (expressionAttribute != null) {
                     FormalExpression fe = new FormalExpression(expressionAttribute.getNodeValue());
                     value = fe.evaluateString(processInstance);
                     System.out.println("value: " + value);
                 } else { // both stringValue and expression attrbts are null. Then this field elem must have an expression elem
                     NodeList elems = field.getChildNodes();
                     for (int e = 0; e < elems.getLength(); ++e) {
                         Node expression = elems.item(e);
                         if (expression.getNodeType() != Node.ELEMENT_NODE) {
                             continue;
                         }
                         if (!("expression".equals(expression.getNodeName())
                                 || "string".equals(expression.getNodeName()))) {
                             throw new RuntimeException("Unknown element '" + expression.getNodeName()
                                     + "' inside field.");
                         }
                         FormalExpression fe = new FormalExpression(expression.getFirstChild().getNodeValue());
                         value = fe.evaluateString(processInstance);
                         System.out.println("value: " + value);
                     }
                 }
                 if (key == null || value == null) {
                     throw new RuntimeException("Malformed sendTask.");
                 }
                 processInstance.setVariable(getId()+"_"+key, value);
             }
             starteventbean.setTaskId(sendTask.getId());
             starteventbean.setTaskId(sendTask.getId());
             ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processInstance.getProcessId());
             starteventbean.setProcesseInstance(processInstanceBean);
             //UserBean user = (UserBean) dao.getObject(UserBean.class, task.getAssignee());
             //starteventbean.setAssignee(user);
             starteventbean.setFormName(task.getName());
             starteventbean.setTaskName(task.getName());
             // starteventbean.setHasUserForm(hasInputForm());
             //starteventbean.setFromPath(task.getInputForm());
             dao.save(starteventbean);
         }
         dao.close();
     }
 
     public void run() {
         processInstance.addRunning(getId());
         // get the details
         System.out.println("SendTask run(): Getting the task details.");
         String to = (String) processInstance.getVariable(getId()+"_to");
         System.out.println("SendTask persistSendTask(): Recipent:" + to);
         String subject = (String) processInstance.getVariable(getId()+"_subject");
         System.out.println("SendTask persistSendTask(): Subject:" + subject);
         String content = (String) processInstance.getVariable(getId()+"_content");
         System.out.println("SendTask persistSendTask(): Content:" + content);
 
         // write them to the db
         //System.out.println("SendTask run(): Wrote details to the db.");
         // see if a form is there
         //System.out.println("SendTask run(): Checking for an input form.");
         // if yes then create a waitedtask and wait for it
         //System.out.println("SendTask run(): Creating the waited task for the form.");
         //System.out.println("SendTask run(): Waiting for the form reply...");
         //WaitedTaskChannel channel = newChannel(WaitedTaskChannel.class, "channel");
         //WaitedTask task = new WaitedTask(channel);
         //processInstance.addWaitedTask(getId(), task);
         //instance(task);
         // todo:
         if (hasInputForm()) {
             System.out.println("SendTask hasInputForm. Pause.");
             processInstance.pause(getId());
         } else {
             try {
                 resumeTask();
             } catch (MessagingException e) {
                 e.printStackTrace();
             }
         }
         /*
         object(new WaitedTaskChannelListener(channel) {
             @Override
             public void resume(Map<String, Object> vars) {
                 System.out.println("UserTask run(). done." + vars.toString());
                 System.out.println("UserTask run(). saving the variables.");
                 processInstance.getVariables().putAll(vars);
                 resumeTask();
             }
         }); */
 
 
     }
 
     public String getId() {
         return task.getId();
     }
 
     public void resumeTask() throws MessagingException {
         // todo this is what must happen when
         // processInstance.getVariables().putAll(vars);
         System.out.println("Resuming send task id " + getId());
           MailClient marketingClient = new MailClient("marketing", "localhost");
        String to = (String) processInstance.getVariable(getId()+"_to");
         System.out.println("SendTask persistSendTask(): Recipent:" + to);
        String subject = (String) processInstance.getVariable(getId()+"_subject");
         System.out.println("SendTask persistSendTask(): Subject:" + subject);
        String content = (String) processInstance.getVariable(getId()+"_content");
         System.out.println("SendTask persistSendTask(): Content:" + content);
         marketingClient.sendMessage(to+"@localhost", subject,  content);
         instance(processInstance.executeNext(this));
         processInstance.addCompleted(getId());
 
     }
 
     // todo
     public boolean hasInputForm() {
         boolean hasInputForm = false;
         return hasInputForm;
     }
 }
 
