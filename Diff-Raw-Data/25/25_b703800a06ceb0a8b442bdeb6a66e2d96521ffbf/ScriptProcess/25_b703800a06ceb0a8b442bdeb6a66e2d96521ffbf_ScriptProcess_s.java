 package net.ex337.scriptus.model;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import java.util.concurrent.Callable;
 
 import javax.annotation.Resource;
 
 import net.ex337.scriptus.ProcessScheduler;
 import net.ex337.scriptus.config.ScriptusConfig;
 import net.ex337.scriptus.dao.ScriptusDAO;
 import net.ex337.scriptus.exceptions.ProcessNotFoundException;
 import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
 import net.ex337.scriptus.interaction.InteractionMedium;
 import net.ex337.scriptus.model.api.ScriptusAPI;
 import net.ex337.scriptus.model.api.Termination;
 import net.ex337.scriptus.model.api.output.ErrorTermination;
 import net.ex337.scriptus.model.api.output.NormalTermination;
 import net.ex337.scriptus.model.support.ScriptusClassShutter;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContinuationPending;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;
 
 /**
  * Represents one script process. The source of the process
  * is loaded at initialisation time and is kept with the 
  * process, i.e. changing a script will have no effect on
  * currently executing scripts.
  * 
  * @author ian
  *
  */
 public class ScriptProcess implements Callable<ScriptAction>, Runnable, Serializable, Cloneable {
 
 	private static final Log LOG = LogFactory.getLog(ScriptProcess.class);
 
 	/**
 	 * The length of the ID in bytes.
 	 */
 	public static final int ID_SIZE_BYTES = 16;
 
 	private static final long serialVersionUID = -7512596370437192858L;
 
 	private UUID pid;
 	private UUID waiterPid;
 	private String userId;
 	private String source;
 	private String sourceName;
 	private String args;
 	private String owner;
 	private Object state;
 	private int version;
 	private List<UUID> children = new ArrayList<UUID>();
 	private Function compiled;
 	private boolean isRoot;
 	
 	private transient Object continuation;
 	private transient Scriptable globalScope;
 	
 	private boolean isKilled;
 	
 	@Resource(name="dao")
 	private transient ScriptusDAO dao;
 
 	@Resource
 	private transient ProcessScheduler scheduler;
 
 	@Resource(name="interaction")
 	private transient InteractionMedium interaction;
 
 	@Resource
 	private transient ScriptusConfig config;
 	
 	public ScriptProcess() {
 	}
 
 	/**
 	 * Sets the state from DAO using the supplied process ID (pid).
 	 * 
 	 * TODO storage must be versioned, since scripts can have a long life-span.
 	 * 
 	 * @param pid
 	 */
 	public void load(final UUID pid) {
 
 //		this.dao = dao;// not set when deserialising below
 
 		if (pid == null) {
 			throw new ScriptusRuntimeException("Cannot load null pid");
 		}
 
 		LOG.debug("loading " + pid.toString().substring(30));
 
 		Context cx = Context.enter();
 		cx.setClassShutter(new ScriptusClassShutter());
 		cx.setOptimizationLevel(-1); // must use interpreter mode
 		
 		try {
 
 			byte[] process = dao.loadProcess(pid);
 			
 			if(process == null) {
 				throw new ProcessNotFoundException(pid.toString());
 			}
 			
 			InputStream bais = new ByteArrayInputStream(process);
 
			ScriptusAPI tmpScriptusApi = new ScriptusAPI(config);
			Scriptable tmpGlobalScope = tmpScriptusApi.createScope(cx);
 
			ScriptableInputStream in = new ScriptableInputStream(bais, tmpGlobalScope);
 
 			ScriptProcess p = (ScriptProcess) in.readObject();
 			
 			this.pid = p.pid;
 			this.waiterPid = p.waiterPid;
 			this.source = p.source;
 			this.sourceName = p.sourceName;
 			this.userId = p.userId;
 			this.args = p.args;
 			this.state = p.state;
 			this.children = p.children;
 			this.compiled = p.compiled;
 			this.owner = p.owner;
 			this.isRoot = p.isRoot;
 			this.version = p.version;
 			
 			p = null;
 
 			this.setGlobalScope((ScriptableObject) in.readObject());
 			this.setContinuation(in.readObject());
 
 			in.close();
 		} catch (ScriptusRuntimeException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new ScriptusRuntimeException(e);
 		} finally {
 			Context.exit();
 		}
 		
 
 	}
 
 	/**
 	 * 
 	 * Initialises a process using the supplied source from the given user ID.
 	 * 
 	 * @param userId
 	 * @param sourceName
 	 * @param args
 	 * @param owner
 	 */
 	public void init(String userId, final String sourceName, String args, String owner) {
 
 		LOG.debug("ctor, source=" + sourceName);
 
 //		this.dao = dao;
 		this.userId = userId;
 		this.sourceName = sourceName;
 		this.args = args;
 		this.owner = owner;
 		this.source = dao.loadScriptSource(userId, sourceName);
 		this.isRoot = true;
 		this.version = 0;
 
 		Context cx = Context.enter();
 		cx.setClassShutter(new ScriptusClassShutter());
 		cx.setOptimizationLevel(-1); // must use interpreter mode
 		
 		try {
 
 			ScriptusAPI scriptusApi = new ScriptusAPI(config);
 
 			Scriptable globalScope = scriptusApi.createScope(cx);
 			
 			setGlobalScope(globalScope);
 			
 			compiled = cx.compileFunction(globalScope, "function ___scriptus___ () {"+source+"}", sourceName, 0, null);
 
 		} catch (ScriptusRuntimeException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new ScriptusRuntimeException(e);
 		} finally {
 			Context.exit();
 		}
 
 	}
 
 	/**
 	 * Writes the script state to DAO. If the pid is null we assign a new one.
 	 */
 	public void save() {
 		if (getPid() == null) {
 			setPid(UUID.randomUUID());
 		} else {
 			version++;
 		}
 
 		LOG.debug("saving " + getPid().toString().substring(30));
 
 		Context cx = Context.enter();
 		cx.setClassShutter(new ScriptusClassShutter());
 		cx.setOptimizationLevel(-1); // must use interpreter mode
 		
 		try {
 			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ScriptableOutputStream out = new ScriptableOutputStream(bout, getGlobalScope());
//				out.addExcludedName("scriptus");
 			out.writeObject(this);
 			out.writeObject(getGlobalScope());
 			out.writeObject(getContinuation());
 			out.close();
 
 			dao.writeProcess(getPid(), bout.toByteArray());
 				
 		} catch (ScriptusRuntimeException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new ScriptusRuntimeException(e);
 		} finally {
 			Context.exit();
 		}
 
 	}
 
 	/**
 	 * 
 	 * Executes the script and returns the ScriptAction of the next API call, or a {@link Termination}
 	 * if the script finished.
 	 * 
 	 */
 	public ScriptAction call() {
 
 		Object result;
 
 		try {
 
 			if (continuation == null) {
 
 				LOG.debug("starting new script");
 
 				Context cx = Context.enter();
 				cx.putThreadLocal("process", this);
 				cx.setClassShutter(new ScriptusClassShutter());
 
 				globalScope.put("args", globalScope, Context.javaToJS(args, globalScope));
 				globalScope.put("owner", globalScope, Context.javaToJS(owner, globalScope));
 
 				try {
 					// running for first time
 					result = cx.callFunctionWithContinuations(compiled, globalScope, new Object[0]);
 				} finally {
 					Context.exit();
 
 				}
 
 			} else {
 
 				LOG.debug("continuing existing script " + getPid().toString().substring(30));
 
 				if (state instanceof ConvertsToScriptable) {
 					state = ((ConvertsToScriptable) state).toScriptable();
 				}
 
 				Context cx = Context.enter();
 				cx.setClassShutter(new ScriptusClassShutter());
 				cx.putThreadLocal("process", this);
 				try {
 					result = cx.resumeContinuation(continuation, globalScope, state);
 				} finally {
 					Context.exit();
 				}
 
 			}
 
 		} catch (RhinoException e) {
 
 			LOG.error("script error", e);
 
 			return new ErrorTermination(e);
 
 		} catch (ContinuationPending cp) {
 
 			continuation = cp.getContinuation();
 
 			state = cp.getApplicationState();
 
 			LOG.error("script continuation, state obj=" + state.getClass());
 
 			if (state instanceof ScriptAction) {
 				return (ScriptAction) state;
 			} else {
 				throw new ScriptusRuntimeException("Continuation state not ScriptAction:" + state);
 			}
 
 		}
 
 		state = result;
 
 		return new NormalTermination(result);
 
 	}
 
 
 	/**
 	 * Does the same as call() above, but also saves the process and calls visit()
 	 * on the resulting {@link ScriptAction}.
 	 */
 	@Override
 	public void run() {
 		
 		ScriptAction result = this.call();
 		
 		if(isKilled) {
 			return;
 			
 		}
 
 		this.save();
 		
 		result.visit(scheduler, interaction, dao, this);
 		
 		
 	}
 	
 	/**
 	 * If the process is running when kill() is called,
 	 * this method stops the next continuation from
 	 * executing.
 	 */
 	public void kill() {
 		isKilled = true;
 	}
 
 	/**
 	 * Copies the process, used in fork()ing.
 	 * Not a complete clone, the differences are:
 	 *  - isRoot is false
 	 *  - version is 0
 	 *  - pid & waiterPid is null
 	 *  - children is empty
 	 * 
 	 */
 	public ScriptProcess clone() {
 		ScriptProcess r = new ScriptProcess();
 		r.args = this.args;
 		r.compiled = this.compiled;
 		r.continuation = this.continuation;
 		r.globalScope = this.globalScope;
 		r.source = this.source;
 		r.sourceName = this.sourceName;
 		r.state = this.state;
 		r.userId = this.userId;
 		r.dao = this.dao;
 		r.scheduler = this.scheduler;
 		r.interaction = this.interaction;
 		r.config = this.config;
 		r.owner = this.owner;
 		// ?
 		r.isRoot = false;
 		r.version = 0;
 		r.pid = null;
 		r.waiterPid = null;
 		r.children = new ArrayList<UUID>();
 
 		return r;
 
 	}
 
 	/**
 	 * deletes the script from DAO.
 	 */
 	public void delete() {
 		
 		/*
 		 * TODO should this recursively delete child processes?
 		 * 
 		 */
 		
 		dao.deleteProcess(getPid());
 	}
 
 	
 	public String getSource() {
 		return source;
 	}
 
 	public Object getContinuation() {
 		return continuation;
 	}
 
 	public Object getState() {
 		return state;
 	}
 
 	public void setState(Object state) {
 		this.state = state;
 	}
 
 	public List<UUID> getChildren() {
 		return children;
 	}
 
 	public void setArgs(String args) {
 		this.args = args;
 	}
 
 	public UUID getWaiterPid() {
 		return waiterPid;
 	}
 
 	public void setWaiterPid(UUID pid) {
 		this.waiterPid = pid;
 	}
 
 	public String getArgs() {
 		return args;
 	}
 
 	public Scriptable getGlobalScope() {
 		return globalScope;
 	}
 
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	public void setContinuation(Object continuation) {
 		this.continuation = continuation;
 	}
 
 	public void setChildren(List<UUID> children) {
 		this.children = children;
 	}
 
 	public void setGlobalScope(Scriptable globalScope) {
 		this.globalScope = globalScope;
 	}
 
 	public boolean equals(Object o) {
 		return EqualsBuilder.reflectionEquals(this, o);
 	}
 
 	public UUID getPid() {
 		return pid;
 	}
 
 	public void setPid(UUID pid) {
 		this.pid = pid;
 	}
 
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 
 	
 
 
 	public boolean isRoot() {
 		return isRoot;
 	}
 
 	public void setRoot(boolean isRoot) {
 		this.isRoot = isRoot;
 	}
 
 	public String getUserId() {
 		return userId;
 	}
 
 	public String getSourceName() {
 		return sourceName;
 	}
 
 }
