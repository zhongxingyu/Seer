 package org.zkoss.todoZK.viewmodel;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.zkoss.bind.BindUtils;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.GlobalCommand;
 import org.zkoss.todoZK.Utils;
 import org.zkoss.todoZK.dao.AbstractDB;
 import org.zkoss.todoZK.dao.DBProvider;
 import org.zkoss.todoZK.exception.MilestoneNotExist;
 import org.zkoss.todoZK.vo.Milestone;
 import org.zkoss.todoZK.vo.Workspace;
 import org.zkoss.zul.DefaultTreeModel;
 import org.zkoss.zul.DefaultTreeNode;
 import org.zkoss.zul.TreeNode;
 
 public class SidebarVM {
 	private static final String[] VIEW_URL = {"cardview.zul", "treeview.zul"};
 	private static AbstractDB db = DBProvider.getInstance();
 	private List<Workspace> workspaces;	//Refactory Maybe not a good idea in product
 	private DefaultTreeModel<BoardItem> boardModel;
 	private Workspace nowWorkspace;
 	private Milestone nowMilestone;
 	private int finishedTask;
 	private int totalTaskAmount;
 	private int viewType;
 	
 	public SidebarVM() {
 		fetchDB();
 	}
 	
 	public DefaultTreeModel<BoardItem> getBoardModel() {
 		return boardModel;
 	}
 	
 	public String getCurrentPath() {
 		StringBuffer result = new StringBuffer();
 		result.append(nowWorkspace == null ? "All Workspace" : nowWorkspace.getTitle());
 		result.append(nowMilestone == null ? "" : " > " + nowMilestone.getTitle());
 		return result.toString();
 	}
 	
 	public int getFinishedAmount() {
 		return finishedTask;
 	}
 	
 	public int getUnfinishedAmount() {
 		return totalTaskAmount - finishedTask;
 	}
 	
 	public void setSelectedItem(TreeNode<BoardItem> selectedItem) {
 		Workspace ws;
 		BoardItem boardItem = selectedItem.getData();
 		switch (boardItem.getType()) {
 		case BoardItem.WORKSPACE_TYPE:
 			ws = getWorkspaceById(boardItem.getId());
 			if (!ws.equals(nowWorkspace)) {
 				nowWorkspace = ws;
 			}
 			finishedTask = nowWorkspace.getFinishedTask();
 			totalTaskAmount = nowWorkspace.getTotalTaskAmount();
 			changeContent("innerpage/zul/" + VIEW_URL[viewType] +
 					"?ws=" + ws.getId(), ws.getTitle(), finishedTask, totalTaskAmount);
 			break;
 		case BoardItem.MILESTONE_TYPE:
 			Milestone ms;
 			try {
 				ms = db.getMilestoneById(boardItem.getId()); //Refactory should not query DB
 				ws = getWorkspaceById(ms.getWorkspaceId());
 				if (!ws.equals(nowWorkspace)) {
 					nowWorkspace = ws;
 				}
 				if (!ms.equals(nowMilestone)) {
 					nowMilestone = ms;
 				}
 				finishedTask = nowMilestone.getFinishedTask();
 				totalTaskAmount = nowMilestone.getTotalTaskAmount();
 				changeContent("innerpage/zul/" + VIEW_URL[viewType] +
 						"?ms=" + ms.getId(), ws.getTitle(), finishedTask, totalTaskAmount);
 			} catch (MilestoneNotExist e) { }
 			break;
 		case BoardItem.ABOUT_PAGE_TYPE:
 			processStaticPage("about.jsp");
 			break;
 		case BoardItem.LOG_PAGE_TYPE:
 			processStaticPage("release.jsp");
 			break;
 		case BoardItem.ROOT_PAGE_TYPE:
 		default:
 			processStaticPage("document.jsp");
 			break;
 		}
 	}
 	
 	@GlobalCommand
 	public void viewChange(@BindingParam("type")int value){
 		viewType = value;
 	}
 	
 	private void processStaticPage(String url) {
 		nowWorkspace = null;
 		nowMilestone = null;
 		totalTaskAmount = 0;
 		finishedTask = 0;
 		for (Workspace ws : workspaces) {
 			totalTaskAmount += ws.getTotalTaskAmount();
 			finishedTask += ws.getFinishedTask();
 		}
 		changeContent("innerpage/jsp/" + url, "All Workspace", finishedTask, totalTaskAmount);
 	}
 	
 	private void changeContent(String url, String path, int finished, int total) {
 		Utils.changeContent("content", url);
 		Map<String, Object> param = new HashMap<String, Object>();
 		param.put("path", path);
 		param.put("finished", finished);
 		param.put("total", total);
 		BindUtils.postGlobalCommand(null, null, "updateUserStatus", param);
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
