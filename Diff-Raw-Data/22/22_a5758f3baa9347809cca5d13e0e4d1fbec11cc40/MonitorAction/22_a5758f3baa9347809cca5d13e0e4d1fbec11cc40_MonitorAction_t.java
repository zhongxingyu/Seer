 package org.jpos.ee.pm.struts.actions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.struts.action.ActionMessages;
 import org.jpos.ee.pm.core.PMContext;
 import org.jpos.ee.pm.core.PMException;
 import org.jpos.ee.pm.core.PMLogger;
 import org.jpos.ee.pm.core.PMMessage;
 import org.jpos.ee.pm.core.monitor.Monitor;
 import org.jpos.ee.pm.core.monitor.MonitorWatcher;
 import org.jpos.ee.pm.struts.PMEntitySupport;
 import org.jpos.ee.pm.struts.PMForwardException;
 
 public class MonitorAction extends ActionSupport {
 
 	private static final String PM_MONITOR_WATCHER = "pm.monitor.watcher";
 
 	protected boolean prepare(PMContext ctx) throws PMException {
 		super.prepare(ctx);
 		String c = ctx.getParameter("continue");
 		boolean kontinue = (c!=null) && (c.compareTo("true")==0);
 		ctx.put(PM_MONITOR_CONTINUE, kontinue);
 		
 		String pmid = ctx.getRequest().getParameter(PM_ID);
 		if(pmid==null) {
 			pmid=(String) ctx.getSession().getAttribute(LAST_PM_ID);
 		}else{
 			ctx.getSession().setAttribute(LAST_PM_ID,pmid);
 		}
 		Monitor monitor = PMEntitySupport.getInstance().getPmservice().getMonitor(pmid);
 		if(monitor == null) {
 			ctx.getErrors().add(new PMMessage(ActionMessages.GLOBAL_MESSAGE, "pm.struts.error.monitor.not.found", pmid));
 			throw new PMException();
 		}
 		ctx.put(PM_MONITOR, monitor);		
 		return true;
 	}
 	
 	protected void doExecute(PMContext ctx) throws PMException {
 		boolean kontinue = (Boolean) ctx.get(PM_MONITOR_CONTINUE);
 		Monitor monitor = (Monitor) ctx.get(PM_MONITOR);
 		MonitorWatcher watcher = (MonitorWatcher)ctx.getSession().getAttribute(PM_MONITOR_WATCHER);
 		List<String> lines = new ArrayList<String>();
 		if(!kontinue || watcher==null){
 			watcher = monitor.newWatcher();
 			try {
 				lines.add(watcher.startWatching());
 			} catch (Exception e) {
 				PMLogger.error(e);
 				lines.add("ERROR");
 			}
 			ctx.getSession().setAttribute(PM_MONITOR_WATCHER, watcher);
 		}else{
 			try {
 				lines.addAll( watcher.getNewLines() );
 			} catch (Exception e) {
 				PMLogger.error(e);
 				lines.add("ERROR C");
 			}
 		}
 		ctx.getSession().setAttribute("pm_monitor_lines", lines);
 		ctx.getSession().setAttribute("monitor", monitor);
 		if(kontinue)
 			throw new PMForwardException("monitor_cont");
 	}
 }
