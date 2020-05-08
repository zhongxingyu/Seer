 package org.mule.galaxy.impl.script;
 
 import java.io.IOException;
 import java.text.ParseException;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.impl.jcr.JcrUtil;
 import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
 import org.mule.galaxy.script.CronParseException;
 import org.mule.galaxy.script.ScriptJob;
 import org.quartz.CronExpression;
 import org.quartz.CronTrigger;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.springmodules.jcr.JcrCallback;
 
 public class ScriptJobDaoImpl extends AbstractReflectionDao<ScriptJob> {
     private Scheduler scheduler;
     
     public ScriptJobDaoImpl() throws Exception {
         super(ScriptJob.class, "scriptJobs", true);
     }
 
     @Override
     protected String getNodeType() {
         return "galaxy:scriptJob";
     }
 
     @Override
     public void save(ScriptJob t) throws DuplicateItemException, NotFoundException {
         try {
             new CronExpression(t.getExpression());
         } catch (ParseException e) {
             throw new CronParseException(e.getMessage());
         }
         super.save(t);
     }
 
     @Override
     protected void doSave(ScriptJob t, Node node, boolean isNew, boolean isMoved, Session session) throws RepositoryException {
         String origName = getJobName(t.getId(), JcrUtil.getStringOrNull(node, "name"));
         String name = getJobName(t.getId(), t.getName());
         
         super.doSave(t, node, isNew, isMoved, session);
         
         try {
             Class cls; 
             if (!t.isConcurrentExecutionAllowed()) {
                 cls = StatefulExecuteScriptJob.class;
             } else {
                 cls = ExecuteScriptJob.class;
             }
             JobDetail job = new JobDetail(name, null, cls);
             
             job.setDurability(true);
             job.getJobDataMap().put(ExecuteScriptJob.SCRIPT_ID, t.getScript().getId());
             
             CronTrigger trigger = new CronTrigger(name, null, t.getExpression());
             trigger.setJobName(name);
             
             if (origName != null) {
                 scheduler.deleteJob(origName, null);
             }
             
             scheduler.scheduleJob(job, trigger);
         } catch (SchedulerException e) {
             throw new RuntimeException(e);
         } catch (ParseException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     protected void doDeleteNode(Session session, Node node) throws RepositoryException {
         try {
            String id = getJobName(node.getName(), JcrUtil.getStringOrNull(node, "name"));
             
             if (!scheduler.deleteJob(id, null)) {
                 throw new RepositoryException("Job to delete doesn't exist. JobID: " + id);
             }
             
             super.doDeleteNode(session, node);
         } catch (SchedulerException e) {
             throw new RuntimeException(e);
         }
     }
 
     private String getJobName(String uuid, String name) {
         return name + "-" + uuid;
     }
 
     public void deleteJobsWithScript(final String scriptId) {
         execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 // cascade delete all the related jobs
                 QueryManager qm = getQueryManager(session);
                 
                 QueryResult result = qm.createQuery("//element(*, galaxy:scriptJob)[@script='" + scriptId + "']", 
                                                     Query.XPATH).execute();
                 
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
                     
                     try {
                         scheduler.deleteJob(getJobName(node.getName(), JcrUtil.getStringOrNull(node, "name")), null);
                     } catch (SchedulerException e) {
                         throw new RuntimeException(e);
                     }
                     
                     node.remove();
                 }
                 
                 return null;
             }
             
         });
     }
 
     public void setScheduler(Scheduler scheduler) {
         this.scheduler = scheduler;
     }
     
 }
