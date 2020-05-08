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
 import org.mule.galaxy.script.ScriptJob;
 import org.mule.galaxy.script.ScriptManager;
 import org.quartz.CronTrigger;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.springmodules.jcr.JcrCallback;
 
 public class ScriptJobDaoImpl extends AbstractReflectionDao<ScriptJob>  {
     private Scheduler scheduler;
     
     public ScriptJobDaoImpl() throws Exception {
         super(ScriptJob.class, "scriptJobs", true);
     }
 
     @Override
     protected String getNodeType() {
         return "galaxy:scriptJob";
     }
 
     @Override
     public void initialize() throws Exception {
         super.initialize();
     }
 
     @Override
    public void save(ScriptJob t) throws DuplicateItemException, NotFoundException {
        super.save(t);
 
         try {
            JobDetail job = new JobDetail(t.getId(), null, ExecuteScriptJob.class);
             job.setDurability(true);
            job.getJobDataMap().put(ExecuteScriptJob.SCRIPT_ID, t.getScript().getId());
             
            CronTrigger trigger = new CronTrigger(t.getId(), null, t.getExpression());
            trigger.setJobName(t.getId());
             
             scheduler.scheduleJob(job, trigger);
         } catch (SchedulerException e) {
             throw new RuntimeException(e);
         } catch (ParseException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     protected void doDelete(String id, Session session) throws RepositoryException {
         try {
             scheduler.deleteJob(id, null);
         } catch (SchedulerException e) {
             throw new RuntimeException(e);
         }
         
         super.doDelete(id, session);
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
                         scheduler.deleteJob(node.getUUID(), null);
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
