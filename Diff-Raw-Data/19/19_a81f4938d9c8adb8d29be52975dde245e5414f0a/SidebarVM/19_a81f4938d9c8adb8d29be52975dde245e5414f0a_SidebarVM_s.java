 package org.zkoss.todoZK.viewmodel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.todoZK.dao.AbstractDB;
 import org.zkoss.todoZK.dao.DBProvider;
 import org.zkoss.todoZK.exception.MilestoneNotExist;
 import org.zkoss.todoZK.vo.Milestone;
 import org.zkoss.todoZK.vo.Task;
 import org.zkoss.todoZK.vo.Workspace;
 import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zul.DefaultTreeModel;
 import org.zkoss.zul.DefaultTreeNode;
 import org.zkoss.zul.TreeNode;
 
 public class SidebarVM {
 	private static AbstractDB db = DBProvider.getInstance();
 	private List<Workspace> workspaces;	//Refactory Maybe not a good idea in product
 	private DefaultTreeModel<BoardItem> boardModel;
 	private Workspace nowWorkspace;
 	private int finishedTask;
 	private int totalTaskAmount;
 	
 	public SidebarVM() {
 		fetchDB();
 	}
 	
 	public DefaultTreeModel<BoardItem> getBoardModel() {
 		return boardModel;
 	}
 	
 	public String getCurrentPath() {
 		return nowWorkspace == null ? "All Workspace" : nowWorkspace.getTitle();
 	}
 	
 	public int getFinishedAmount() {
 		return finishedTask;
 	}
 	
 	public int getUnfinishedAmount() {
 		return totalTaskAmount - finishedTask;
 	}
 	
 	@NotifyChange({"finishedAmount", "unfinishedAmount", "currentPath"})
 	public void setSelectedItem(TreeNode<BoardItem> selectedItem) {
 		Workspace ws;
 		BoardItem boardItem = selectedItem.getData();
 		switch (boardItem.getType()) {
 		case BoardItem.WORKSPACE_TYPE:
 			ws = getWorkspaceById(boardItem.getId());
 			if (!ws.equals(nowWorkspace)) {
 				nowWorkspace = ws;
 			}
 			resetTaskStat();
 			gatherTaskStat(nowWorkspace);
			//TODO change TaskViewer
			Clients.evalJavaScript("changeContentUrl('innerpage/zul/cardview.zul?ws=" + ws.getId() + "')");
 			break;
 		case BoardItem.MILESTONE_TYPE:
 			Milestone ms;
 			try {
 				ms = db.getMilestoneById(boardItem.getId()); //Refactory should not query DB
 				ws = getWorkspaceById(ms.getWorkspaceId());
 				if (!ws.equals(nowWorkspace)) {
 					nowWorkspace = ws;
 				}
 				resetTaskStat();
 				gatherTaskStat(nowWorkspace);
				//TODO change TaskViewer
 			} catch (MilestoneNotExist e) {
 			}
 			break;
 		case BoardItem.ABOUT_PAGE_TYPE:
 		case BoardItem.LOG_PAGE_TYPE:
			//TODO change content's url
 			nowWorkspace = null;
 			gatherTaskStat();
			Clients.evalJavaScript("changeContentUrl('innerpage/jsp/" + (boardItem.isAboutPage() ? "about.jsp" : "release.jsp") + "')");
 			break;
 		case BoardItem.ROOT_PAGE_TYPE:
 		default:
 			nowWorkspace = null;
 			gatherTaskStat();
			Clients.evalJavaScript("changeContentUrl('innerpage/jsp/document.jsp')");
 			break;
 		}
 	}
 
 	private void fetchDB() {
 		DefaultTreeNode<BoardItem> root = new DefaultTreeNode<BoardItem>(null, new ArrayList<DefaultTreeNode<BoardItem>>());
 		boardModel = new DefaultTreeModel<BoardItem>(root);
 		
 		// static page node
 		DefaultTreeNode<BoardItem> pageRoot = new DefaultTreeNode<BoardItem>(
 			new BoardItem(Long.MIN_VALUE, "Document", BoardItem.ROOT_PAGE_TYPE),
 			new ArrayList<DefaultTreeNode<BoardItem>>()
 		);
 		pageRoot.add(
 			new DefaultTreeNode<BoardItem>(
 				new BoardItem(Long.MIN_VALUE, "About", BoardItem.ABOUT_PAGE_TYPE)
 			)
 		);
 		pageRoot.add(
 			new DefaultTreeNode<BoardItem>(
 				new BoardItem(Long.MIN_VALUE, "Release Log", BoardItem.LOG_PAGE_TYPE)
 			)
 		);		
 		root.add(pageRoot);
 		boardModel.addOpenObject(pageRoot);
 		////
 		
 		// build items of Workspace / Milestone
 		workspaces = db.getWorkspaces();
 		for (Workspace ws : workspaces) {
 			DefaultTreeNode<BoardItem> wsNode = new DefaultTreeNode<BoardItem>(
 				new BoardItem(ws.getId(), ws.getTitle(), BoardItem.WORKSPACE_TYPE),
 				new ArrayList<DefaultTreeNode<BoardItem>>()
 			);
 			for (Milestone ms : ws.getMilestones()) {
 				wsNode.add(
 					new DefaultTreeNode<BoardItem>(
 						new BoardItem(ms.getId(), ms.getTitle(), BoardItem.MILESTONE_TYPE)
 					)
 				);
 			}
 			root.add(wsNode);
 			boardModel.addOpenObject(wsNode);
 		}
 		////
 
 		gatherTaskStat();
 	}
 	
 	private void resetTaskStat() {
 		finishedTask = 0;
 		totalTaskAmount = 0;		
 	}
 
 	/**
 	 * Gather statistics of tasks in every workspace.
 	 */
 	private void gatherTaskStat() {
 		resetTaskStat();
 		for (Workspace ws : workspaces) {
 			gatherTaskStat(ws);	
 		}		
 	}
 	
 	/**
 	 * Gather statistics of tasks in specified workspace.
 	 * @param ws
 	 */
 	private void gatherTaskStat(Workspace ws) {
 		for (Milestone ms : ws.getMilestones()) {
 			for (Task task : ms.getTasks()) {
 				totalTaskAmount++;
 				if (task.isFinish()) {
 					finishedTask++;
 				}
 			}
 		}
 	}
 	
 	private Workspace getWorkspaceById(Long id) {
 		for (Workspace ws : workspaces) {
 			if (ws.getId().equals(id)) {
 				return ws;
 			}
 		}
 		return null;
 	}
 }
