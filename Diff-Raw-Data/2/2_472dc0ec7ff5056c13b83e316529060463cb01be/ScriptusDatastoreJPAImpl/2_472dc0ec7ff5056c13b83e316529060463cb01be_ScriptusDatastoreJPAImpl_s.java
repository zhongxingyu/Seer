 package net.ex337.scriptus.datastore.impl.jpa;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import net.ex337.scriptus.SerializableUtils;
 import net.ex337.scriptus.config.ScriptusConfig;
 import net.ex337.scriptus.config.ScriptusConfig.TransportType;
 import net.ex337.scriptus.datastore.ScriptusDatastore;
 import net.ex337.scriptus.datastore.impl.BaseScriptusDatastore;
 import net.ex337.scriptus.datastore.impl.jpa.dao.ChildProcessPIDDAO;
 import net.ex337.scriptus.datastore.impl.jpa.dao.MessageCorrelationDAO;
 import net.ex337.scriptus.datastore.impl.jpa.dao.ProcessDAO;
 import net.ex337.scriptus.datastore.impl.jpa.dao.ScheduledScriptActionDAO;
 import net.ex337.scriptus.datastore.impl.jpa.dao.ScriptDAO;
 import net.ex337.scriptus.datastore.impl.jpa.dao.ScriptIdDAO;
 import net.ex337.scriptus.datastore.impl.jpa.dao.TransportCursorDAO;
 import net.ex337.scriptus.datastore.impl.jpa.dao.views.ProcessListItemDAO;
 import net.ex337.scriptus.exceptions.ProcessNotFoundException;
 import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
 import net.ex337.scriptus.model.MessageCorrelation;
 import net.ex337.scriptus.model.ProcessListItem;
 import net.ex337.scriptus.model.ScriptProcess;
 import net.ex337.scriptus.model.api.HasStateLabel;
 import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
 import net.ex337.scriptus.model.scheduler.Wake;
 import net.ex337.scriptus.model.support.ScriptusClassShutter;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.ScriptableObject;
 import org.springframework.transaction.annotation.Transactional;
 
 public abstract class ScriptusDatastoreJPAImpl extends BaseScriptusDatastore implements ScriptusDatastore {
     
     private static final Log LOG = LogFactory.getLog(ScriptusDatastoreJPAImpl.class);
     
     @PersistenceContext(unitName="jpa-pu")
     private EntityManager em;
 
     @Override
     @Transactional(readOnly=true)
     public ScriptProcess getProcess(UUID pid) {
         
         if (pid == null) {
             throw new ScriptusRuntimeException("Cannot load null pid");
         }
 
         LOG.debug("loading " + pid.toString().substring(30));
 
         Context cx = Context.enter();
         cx.setClassShutter(new ScriptusClassShutter());
         cx.setOptimizationLevel(-1); // must use interpreter mode
         
         try {
             
             ProcessDAO d = em.find(ProcessDAO.class, pid.toString());
 
             if(d == null) {
                 throw new ProcessNotFoundException(pid.toString());
             }
 
             ScriptProcess result = createScriptProcess();
 
             result.setPid(UUID.fromString(d.pid));
             if(d.waitingPid != null) {
                 result.setWaiterPid(UUID.fromString(d.waitingPid));
             }
             result.setSource(new String(d.source, ScriptusConfig.CHARSET));
             result.setSourceName(d.sourceId);
             result.setUserId(d.userId);
             result.setArgs(d.args);
             
             {
                 ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(d.script_state));
 
                 result.setCompiled((Function) in.readObject());
                 result.setGlobalScope((ScriptableObject) in.readObject());
                 result.setContinuation(in.readObject());
                 
             }
             
             result.setState(SerializableUtils.deserialiseObject(d.state));
             result.setOwner(d.owner);
             result.setRoot(d.isRoot);
             result.setVersion(d.version);
             result.setAlive(d.isAlive);
 
             return result;
 
         } catch (ScriptusRuntimeException e) {
             throw e;
         } catch (IOException e) {
             throw new ScriptusRuntimeException(e);
         } catch (ClassNotFoundException e) {
             throw new ScriptusRuntimeException(e);
         } finally {
             Context.exit();
         }
         
     }
     
     @Override
     @Transactional(readOnly=false)
     public void writeProcess(ScriptProcess p) {
         
         boolean newProcess = false;
         
         /*
          * The semantics are as follows:
          * 
          * fork() sends a new process with a pid
          * normal exec() etc. sends a new process with no pid
          * so we use the version to tell if we are updating
          * or inserting a new record.
          */
         
         if(p.getVersion() == 0) {
             newProcess = true;
         }
         
         if (p.getPid() == null) {
             p.setPid(UUID.randomUUID());
         }
 
         LOG.debug("saving " + p.getPid().toString().substring(30));
 
         Context cx = Context.enter();
         cx.setClassShutter(new ScriptusClassShutter());
         cx.setOptimizationLevel(-1); // must use interpreter mode
         
         try {
             ProcessDAO d = null;
             
             if(newProcess) {
                 d = new ProcessDAO();
                 d.pid = p.getPid().toString();
                 d.created = d.lastmod = System.currentTimeMillis();
             } else {
                 
                 d = em.find(ProcessDAO.class, p.getPid().toString());
                 if(d == null) {
                     throw new ScriptusRuntimeException("Process not found for pid "+p.getPid());
                 }
                 
                 d.lastmod = System.currentTimeMillis();
             }
             
             d.args = p.getArgs();
             
             {
                 ByteArrayOutputStream bout = new ByteArrayOutputStream();
                 ObjectOutputStream out = new ObjectOutputStream(bout);
                 out.writeObject(p.getCompiled());
                 out.writeObject(p.getGlobalScope());
                 out.writeObject(p.getContinuation());
                 out.close();
                 d.script_state = bout.toByteArray();
             }
 
             
             d.state = SerializableUtils.serialiseObject(p.getState());
             d.isRoot = p.isRoot();
             d.owner = p.getOwner();
             d.source = p.getSource().getBytes(ScriptusConfig.CHARSET);
             d.sourceId = p.getSourceName();
             d.userId = p.getUserId();
             d.isAlive = p.isAlive();
             if(p.getWaiterPid() != null) {
                 d.waitingPid = p.getWaiterPid().toString();
             }
             
             
             
             em.persist(d);
 
             //datastore.writeProcess(getPid(), bout.toByteArray());
                 
         } catch (ScriptusRuntimeException e) {
             throw e;
         } catch (IOException e) {
             throw new ScriptusRuntimeException(e);
         } finally {
             Context.exit();
         }
 
     }
 
     @Override
     @Transactional(readOnly=false)
     public void deleteProcess(UUID pid) {
         
         Query q = em.createQuery("delete from ProcessDAO d where d.pid = :pid");
         q.setParameter("pid", pid.toString());
         
         q.executeUpdate();
         
     }
 
     @Override
     @Transactional(readOnly=true)
     public Set<String> listScripts(String userId) {
         
         Query q = em.createQuery("select d.id.name from ScriptDAO d where d.id.userId = :userId");
         q.setParameter("userId", userId);
         
         List<Object> oo = q.getResultList();
         
         Set<String> result = new HashSet<String>();
         
         for(Object o : oo) {
             result.add(o.toString());
         }
 
         return result;
     }
 
     @Override
     @Transactional(readOnly=true)
     public String loadScriptSource(String userId, String name) {
         
         ScriptIdDAO d = new ScriptIdDAO();
         d.userId = userId;
         d.name = name;
         
         ScriptDAO s = em.find(ScriptDAO.class, d);
         
         if(s != null) {
             return new String(s.source, ScriptusConfig.CHARSET);
         }
         
         return null;
         
     }
 
     @Override
     @Transactional(readOnly=false)
     public void saveScriptSource(String userId, String name, String source) {
         
         ScriptIdDAO id = new ScriptIdDAO();
         id.name = name;
         id.userId = userId;
         
         ScriptDAO s = em.find(ScriptDAO.class, id);
         
         if(s == null) {
             s = new ScriptDAO();
             s.id = id;
         }
         s.source = source.getBytes(ScriptusConfig.CHARSET);
         em.persist(s);
 
     }
 
     @Override
     @Transactional(readOnly=false)
     public void deleteScript(String userId, String name) {
         
         Query q = em.createQuery("delete from ScriptDAO d where d.id.userId = :userId and d.id.name = :name");
         q.setParameter("userId", userId);
         q.setParameter("name", name);
         
         q.executeUpdate();
 
     }
 
     @Override
     @Transactional(readOnly=true)
     public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
         
         Query q = em.createQuery("select s from ScheduledScriptActionDAO s where s.when <= :when");
         q.setParameter("when", dueDate.getTimeInMillis());
         
         List<ScheduledScriptActionDAO> daos = q.getResultList();
         
         List<ScheduledScriptAction> result = new ArrayList<ScheduledScriptAction>(daos.size());
         
         for(ScheduledScriptActionDAO d : daos) {
             result.add(toScheduledAction(d));
         }
             
         return result;
         
     }
 
     @Override
     @Transactional(readOnly=false)
     public void deleteScheduledTask(UUID pid, long nonce) {
         
         Query q = em.createQuery("delete from ScheduledScriptActionDAO s where s.pid  = :pid and s.nonce = :nonce");
         q.setParameter("pid", pid.toString());
         q.setParameter("nonce", nonce);
         
         q.executeUpdate();
 
     }
 
     @Override
     @Transactional(readOnly=false)
     public void saveScheduledTask(ScheduledScriptAction task) {
         if(task instanceof Wake) {
             Wake w = (Wake) task;
             
             ScheduledScriptActionDAO d = new ScheduledScriptActionDAO();
             d.action="wake";
             d.nonce = w.getNonce();
             d.pid = w.getPid().toString();
             d.when = task.getWhen();
 
             em.persist(d);
             
         } else {
             throw new ScriptusRuntimeException("unknown scheduled action "+task);
         }
 
     }
 
     @Override
     @Transactional(readOnly=false)
     public void registerMessageCorrelation(MessageCorrelation cid) {
         MessageCorrelationDAO d = new MessageCorrelationDAO();
         d.pid = cid.getPid().toString();
         d.timestamp = cid.getTimestamp();
         d.messageId = cid.getMessageId();
         d.user = cid.getUser();
         em.persist(d);
     }
 
     @Override
     @Transactional(readOnly=true)
     public Set<MessageCorrelation> getMessageCorrelations(String inReplyToMessageId, String fromUser) {
         
         StringBuilder b = new StringBuilder(
                 "select d from MessageCorrelationDAO d" +
                 " where (d.messageId is null and d.user is null)" +
                 " or (d.messageId is null and d.user = :user)");
         
         if(inReplyToMessageId != null) {
             b.append(" or (d.messageId = :messageId and d.user is null)" +
             		 " or (d.messageId = :messageId and d.user = :user)");
         }
         Query q = em.createQuery(b.toString());
         q.setParameter("user", fromUser);
         if(inReplyToMessageId != null) {
             q.setParameter("messageId", inReplyToMessageId);
         }
         
         List<MessageCorrelationDAO> dd = q.getResultList();
         
         Set<MessageCorrelation> result = new HashSet<MessageCorrelation>();
        
         for(MessageCorrelationDAO d : dd) {
             result.add(toMessageCorrelation(d));
         }
         
         return result;
     }
 
     @Override
     @Transactional(readOnly=false)
     public void unregisterMessageCorrelation(MessageCorrelation correlation) {
         
         MessageCorrelationDAO d = em.find(MessageCorrelationDAO.class, correlation.getPid().toString());
         if(d != null) {
             em.remove(d);
         }
 
     }
 
     @Override
     @Transactional(readOnly=true)
     public String getTransportCursor(TransportType transport) {
         TransportCursorDAO d = em.find(TransportCursorDAO.class, transport.toString());
         if(d == null){
             return null;
         }
         return d.cursor;
     }
 
     @Override
     @Transactional(readOnly=false)
    public void updateTransportCursor(TransportType transport, String cursor) {
         TransportCursorDAO d = em.find(TransportCursorDAO.class, transport.toString());
         if(d == null){
             d = new TransportCursorDAO();
             d.transport = transport.toString();
         }
         d.cursor = cursor;
         
         em.persist(d);
     }
 
 
     
     private MessageCorrelation toMessageCorrelation(MessageCorrelationDAO dao) {
         MessageCorrelation r = new MessageCorrelation();
         r.setPid(UUID.fromString(dao.pid));
         r.setUser(dao.user);
         r.setMessageId(dao.messageId);
         r.setTimestamp(dao.timestamp);
         return r;
         
     }
     private ScheduledScriptAction toScheduledAction(ScheduledScriptActionDAO dao) {
         ScheduledScriptAction r = null;
         
         if(dao.action.equalsIgnoreCase("wake")) {
             Wake w = new Wake(UUID.fromString(dao.pid), dao.nonce, dao.when);
             r = w;
         } else {
             throw new ScriptusRuntimeException("unkown type of action "+dao.action);
         }
         
         return r;
     }
     
     @Override
     @Transactional(readOnly=false)
     public void createSamples() {
         super.createSamples();        
     }
 
     @Override
     public List<UUID> getChildren(UUID parent) {
         Query q = em.createQuery("select r from ChildProcessPIDDAO r where r.parent = :parent order by r.seq");
         q.setParameter("parent", parent.toString());
         
         List<ChildProcessPIDDAO> c = q.getResultList();
         List<UUID> result = new ArrayList<UUID>(c.size());
         for(ChildProcessPIDDAO cc : c) {
             result.add(UUID.fromString(cc.child));
         }
         return result;
     }
 
     @Override
     @Transactional(readOnly=false)
     public void removeChild(UUID parent, UUID child) {
         Query q = em.createQuery("delete from ChildProcessPIDDAO p where parent = :parent and child = :child");
         q.setParameter("parent", parent.toString());
         q.setParameter("child", child.toString());
        q.executeUpdate();
     }
 
     @Override
     @Transactional(readOnly=false)
     public void addChild(UUID parent, UUID newChild, int seq) {
         ChildProcessPIDDAO p = new ChildProcessPIDDAO();
         p.child = newChild.toString();
         p.parent = parent.toString();
         p.seq = seq;
         
         em.persist(p);
         
     }
 
     @Override
     public UUID getLastChild(UUID parent) {
         
         Query q = em.createQuery("select r from ChildProcessPIDDAO r where r.parent = :parent and r.seq = (select max(rr.seq) from ChildProcessPIDDAO rr where rr.parent = :parent)");
         q.setParameter("parent", parent.toString());
                 
         try{    
             ChildProcessPIDDAO r = (ChildProcessPIDDAO) q.getSingleResult();
             return UUID.fromString(r.child);
         } catch(NoResultException nre) {
             return null;
         }
     }
 
     @Override
     @Transactional(readOnly=false)
     public void updateProcessState(final UUID pid, final Object o) {
         super.locks.runWithLock(pid, new Runnable() {
             @Override
             public void run() {
                 
                 String label = null;
                 
                 if(o instanceof HasStateLabel) {
                     //TODO locale should be of user
                     label = ((HasStateLabel)o).getStateLabel(Locale.getDefault());
                 }
 
                 Query q = em.createQuery("update ProcessDAO d set d.state = :state, d.state_label = :label where d.pid= :pid");
                 try {
                     q.setParameter("state", SerializableUtils.serialiseObject(o));
                 } catch (IOException e) {
                     throw new ScriptusRuntimeException(e);
                 }
                 q.setParameter("pid", pid.toString());
                 q.setParameter("label", label);
 
                 int rows = q.executeUpdate();
                 
                 if(rows != 1) {
                     throw new ScriptusRuntimeException("no rows updated for pid "+pid);
                 }
             }
             
         });
     }
     
     @Override
     public List<ProcessListItem> getProcessesForUser(String uid) {
         
         List<ProcessListItem> result = new ArrayList<ProcessListItem>();
         
         Query q = em.createQuery("select p from ProcessListItemDAO p where p.uid = :uid");
         q.setParameter("uid", uid);
         
         List<ProcessListItemDAO> dd = q.getResultList();
         
         for(ProcessListItemDAO d : dd) {
             result.add(new ProcessListItem(d.pid, d.uid, d.stateLabel, d.sourceName, d.version, d.sizeOnDisk, d.created, d.lastmod, d.alive));
         }
         
         return result;
     }
 
     @Override
     @Transactional(readOnly=false)
     public void markProcessFinished(UUID pid) {
        Query q = em.createQuery("update ProcessDAO d set d.alive = false where d.pid = :pid");
         q.setParameter("pid", pid.toString());
         
         int rows = q.executeUpdate();
         
         if(rows != 1) {
             throw new ScriptusRuntimeException("no rows updated for pid "+pid);
         }
         
     }
 
 }
