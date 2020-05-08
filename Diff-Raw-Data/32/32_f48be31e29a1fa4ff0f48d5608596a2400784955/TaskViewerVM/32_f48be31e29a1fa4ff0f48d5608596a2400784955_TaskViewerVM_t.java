 package org.zkoss.todoZK.viewmodel;
 
 import java.util.List;
 
 import org.zkoss.bind.BindContext;
 import org.zkoss.bind.Converter;
 import org.zkoss.todoZK.dao.AbstractDB;
 import org.zkoss.todoZK.dao.DBProvider;
 import org.zkoss.todoZK.vo.Milestone;
 import org.zkoss.todoZK.vo.Workspace;
 import org.zkoss.zk.ui.Execution;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zul.Div;
 
 public class TaskViewerVM {
 	private AbstractDB db = DBProvider.getInstance();
 	String ws;
 	String ms;
 	private List<Milestone> milestones;
 	private Milestone milestone;
 	private Converter<String, Integer, Div> priorityConverter = new PriorityConverter();
 	private Workspace workspace;
 
 	public TaskViewerVM() {
 		Execution exec = Executions.getCurrent();
 		ws = exec.getParameter("ws");
 		ms = exec.getParameter("ms");
 		if (ws != null) {
 			try {
 				workspace = db.getWorkspaceById(Long.parseLong(ws));
 				milestones = workspace.getMilestones();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (ms != null) {
 			try {
 				milestone = db.getMilestoneById(Long.parseLong(ms));
 				workspace = db.getWorkspaceById(milestone.getWorkspaceId());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public List<Milestone> getMilestones() {
 		return milestones;
 	}
 	
 	public Milestone getMilestone() {
 		return milestone;
 	}
 	
 	public Workspace getWorkspace() {
 		return workspace;
 	}
 	
 	public Converter<String, Integer, Div> getPriorityConverter() {
 		return priorityConverter;
 	}
 	
 	public class PriorityConverter implements Converter<String, Integer, Div> {
 		@Override
 		public Integer coerceToBean(String sclass, Div div, BindContext ctx) {
 			return null;
 		}
 		
 		@Override
 		public String coerceToUi(Integer priority, Div div, BindContext ctx) {
 			switch (priority) {
 			case 0:
 				return "priority routine";
 			case 1:
 				return "priority essential";
 			case 2:
 				return "priority vital";
 			case 3:
 				return "priority important";
 			case 4:
 				return "priority urgent";
 			default:
 				return null;
 			}
 		}
 	}
 }
