 package com.eyeq.pivot4j.ui.primefaces;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.faces.FacesException;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import org.olap4j.OlapException;
 import org.olap4j.metadata.Hierarchy;
 import org.olap4j.metadata.Member;
 import org.primefaces.component.commandbutton.CommandButton;
 import org.primefaces.event.NodeSelectEvent;
 import org.primefaces.model.DefaultTreeNode;
 import org.primefaces.model.TreeNode;
 
 import com.eyeq.pivot4j.PivotModel;
 import com.eyeq.pivot4j.transform.PlaceMembersOnAxes;
 import com.eyeq.pivot4j.ui.primefaces.tree.MemberNode;
 import com.eyeq.pivot4j.ui.primefaces.tree.SelectionNode;
 import com.eyeq.pivot4j.util.MemberSelection;
 
 @ManagedBean(name = "memberSelectionHandler")
 @SessionScoped
 public class MemberSelectionHandler {
 
 	@ManagedProperty(value = "#{pivotModelManager.model}")
 	private PivotModel model;
 
 	private TreeNode sourceNode;
 
 	private TreeNode targetNode;
 
 	private TreeNode[] sourceSelection;
 
 	private TreeNode[] targetSelection;
 
 	private Hierarchy hierarchy;
 
 	private String hierarchyName;
 
 	private CommandButton buttonAdd;
 
 	private CommandButton buttonRemove;
 
 	private CommandButton buttonUp;
 
 	private CommandButton buttonDown;
 
 	private CommandButton buttonApply;
 
 	private MemberSelection selection;
 
 	@PostConstruct
 	protected void initialize() {
 	}
 
 	@PreDestroy
 	protected void destroy() {
 	}
 
 	/**
 	 * @return the model
 	 */
 	public PivotModel getModel() {
 		return model;
 	}
 
 	/**
 	 * @param model
 	 *            the model to set
 	 */
 	public void setModel(PivotModel model) {
 		this.model = model;
 	}
 
 	/**
 	 * @return the sourceNode
 	 */
 	public TreeNode getSourceNode() {
 		if (sourceNode == null) {
 			this.sourceNode = new DefaultTreeNode();
 
 			Hierarchy hierarchy = getHierarchy();
 			if (hierarchy != null) {
 				try {
 					List<? extends Member> members = hierarchy.getRootMembers();
 
 					for (Member member : members) {
 						MemberNode node = new MemberNode(sourceNode, member,
 								getSelection());
 						node.setSelectable(true);
 
 						sourceNode.getChildren().add(node);
 					}
 				} catch (OlapException e) {
 					throw new FacesException(e);
 				}
 			}
 		}
 
 		return sourceNode;
 	}
 
 	/**
 	 * @param sourceNode
 	 *            the sourceNode to set
 	 */
 	public void setSourceNode(TreeNode sourceNode) {
 		this.sourceNode = sourceNode;
 	}
 
 	/**
 	 * @return the targetNode
 	 */
 	public TreeNode getTargetNode() {
 		if (targetNode == null) {
 			MemberSelection selection = getSelection();
 
 			if (selection != null) {
 				this.targetNode = new SelectionNode(selection);
 
 				targetNode.setExpanded(true);
 			}
 		}
 
 		return targetNode;
 	}
 
 	/**
 	 * @param targetNode
 	 *            the targetNode to set
 	 */
 	public void setTargetNode(TreeNode targetNode) {
 		this.targetNode = targetNode;
 	}
 
 	public void show() {
 		reset();
 
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		Map<String, String> parameters = context.getExternalContext()
 				.getRequestParameterMap();
 
 		this.hierarchyName = parameters.get("hierarchy");
 	}
 
 	public void reset() {
 		buttonAdd.setDisabled(true);
 		buttonRemove.setDisabled(true);
 		buttonUp.setDisabled(true);
 		buttonDown.setDisabled(true);
 		buttonApply.setDisabled(true);
 
 		this.hierarchyName = null;
 		this.hierarchy = null;
 		this.sourceNode = null;
 		this.targetNode = null;
 		this.selection = null;
 	}
 
 	public void apply() {
 		PlaceMembersOnAxes transform = model
 				.getTransform(PlaceMembersOnAxes.class);
 		transform.placeMembers(getHierarchy(), getSelection().getMembers());
 
 		buttonApply.setDisabled(true);
 	}
 
 	public void add() {
		remove(SelectionMode.Single.name());
 	}
 
 	/**
 	 * @param modeName
 	 */
 	public void add(String modeName) {
 		SelectionMode mode = null;
 
 		if (modeName != null) {
 			mode = SelectionMode.valueOf(modeName);
 		}
 
 		MemberSelection selection = getSelection();
 
 		if (mode == null) {
 			selection.clear();
 		} else {
 			boolean empty = true;
 
 			List<Member> members = selection.getMembers();
 
 			for (TreeNode node : sourceSelection) {
 				MemberNode memberNode = (MemberNode) node;
 
 				Member member = memberNode.getElement();
 
 				List<Member> targetMembers = mode.getTargetMembers(member);
 
 				for (Member target : targetMembers) {
 					if (!members.contains(target)) {
 						members.add(target);
 						empty = false;
 					}
 				}
 			}
 
 			if (empty) {
 				FacesContext.getCurrentInstance().addMessage(
 						null,
 						new FacesMessage(FacesMessage.SEVERITY_WARN, "Note:",
 								"There are no members to select."));
 				return;
 			}
 
 			this.selection = new MemberSelection(members);
 		}
 
 		this.sourceNode = null;
 		this.targetNode = null;
 
 		this.sourceSelection = null;
 		this.targetSelection = null;
 
 		buttonAdd.setDisabled(true);
 		buttonRemove.setDisabled(true);
 		buttonApply.setDisabled(false);
 	}
 
 	public void remove() {
 		remove(SelectionMode.Single.name());
 	}
 
 	/**
 	 * @param modeName
 	 */
 	public void remove(String modeName) {
 		SelectionMode mode = null;
 
 		if (modeName != null) {
 			mode = SelectionMode.valueOf(modeName);
 		}
 
 		MemberSelection selection = getSelection();
 
 		if (mode == null) {
 			selection.clear();
 		} else {
 			boolean empty = true;
 
 			List<Member> members = selection.getMembers();
 
 			for (TreeNode node : targetSelection) {
 				SelectionNode memberNode = (SelectionNode) node;
 
 				Member member = memberNode.getElement();
 
 				List<Member> targetMembers = mode.getTargetMembers(member);
 
 				for (Member target : targetMembers) {
 					if (members.contains(target)) {
 						members.remove(target);
 						empty = false;
 					}
 				}
 			}
 
 			if (empty) {
 				FacesContext.getCurrentInstance().addMessage(
 						null,
 						new FacesMessage(FacesMessage.SEVERITY_WARN, "Note:",
 								"There are no members to remove."));
 				return;
 			}
 
 			this.selection = new MemberSelection(members);
 		}
 
 		this.sourceNode = null;
 		this.targetNode = null;
 
 		this.sourceSelection = null;
 		this.targetSelection = null;
 
 		buttonAdd.setDisabled(true);
 		buttonRemove.setDisabled(true);
 		buttonApply.setDisabled(false);
 	}
 
 	public void moveUp() {
 		SelectionNode node = (SelectionNode) targetSelection[0];
 		Member member = node.getElement();
 
 		MemberSelection selection = getSelection();
 		selection.moveUp(member);
 
 		SelectionNode parent = (SelectionNode) node.getParent();
 		parent.moveUp(node);
 
 		buttonUp.setDisabled(!selection.canMoveUp(member));
 		buttonApply.setDisabled(false);
 	}
 
 	public void moveDown() {
 		SelectionNode node = (SelectionNode) targetSelection[0];
 		Member member = node.getElement();
 
 		MemberSelection selection = getSelection();
 		selection.moveDown(member);
 
 		SelectionNode parent = (SelectionNode) node.getParent();
 		parent.moveDown(node);
 
 		buttonUp.setDisabled(!selection.canMoveDown(member));
 		buttonApply.setDisabled(false);
 	}
 
 	public Hierarchy getHierarchy() {
 		if (hierarchy == null) {
 			if (hierarchyName != null && model.isInitialized()) {
 				this.hierarchy = model.getCube().getHierarchies()
 						.get(hierarchyName);
 			}
 		}
 
 		return hierarchy;
 	}
 
 	protected MemberSelection getSelection() {
 		if (selection == null) {
 			Hierarchy hierarchy = getHierarchy();
 
 			if (hierarchy != null) {
 				PlaceMembersOnAxes transform = model
 						.getTransform(PlaceMembersOnAxes.class);
 
 				List<Member> members = transform.findVisibleMembers(hierarchy);
 				this.selection = new MemberSelection(members);
 			}
 		}
 
 		return selection;
 	}
 
 	/**
 	 * @param e
 	 */
 	public void onSourceNodeSelected(NodeSelectEvent e) {
 		boolean canAdd;
 
 		if (sourceSelection == null) {
 			canAdd = false;
 		} else {
 			canAdd = true;
 
 			for (TreeNode node : sourceSelection) {
 				if (((MemberNode) node).getData().isSelected()) {
 					canAdd = false;
 					break;
 				}
 			}
 		}
 
 		buttonAdd.setDisabled(!canAdd);
 	}
 
 	/**
 	 * @param e
 	 */
 	public void onTargetNodeSelected(NodeSelectEvent e) {
 		boolean canRemove;
 		boolean canMoveUp;
 		boolean canMoveDown;
 
 		if (targetSelection == null) {
 			canRemove = false;
 			canMoveUp = false;
 			canMoveDown = false;
 		} else {
 			canRemove = true;
 
 			for (TreeNode node : targetSelection) {
 				if (!((SelectionNode) node).getData().isSelected()) {
 					canRemove = false;
 					break;
 				}
 			}
 
 			if (targetSelection.length == 1) {
 				SelectionNode node = (SelectionNode) e.getTreeNode();
 
 				Member member = node.getElement();
 
 				MemberSelection selection = getSelection();
 
 				canMoveUp = selection.canMoveUp(member);
 				canMoveDown = selection.canMoveDown(member);
 			} else {
 				canMoveUp = false;
 				canMoveDown = false;
 			}
 		}
 
 		buttonRemove.setDisabled(!canRemove);
 		buttonUp.setDisabled(!canMoveUp);
 		buttonDown.setDisabled(!canMoveDown);
 	}
 
 	/**
 	 * @return the hierarchyName
 	 */
 	public String getHierarchyName() {
 		return hierarchyName;
 	}
 
 	/**
 	 * @param hierarchyName
 	 *            the hierarchyName to set
 	 */
 	public void setHierarchyName(String hierarchyName) {
 		this.hierarchyName = hierarchyName;
 	}
 
 	/**
 	 * @return the sourceSelection
 	 */
 	public TreeNode[] getSourceSelection() {
 		return sourceSelection;
 	}
 
 	/**
 	 * @param sourceSelection
 	 *            the sourceSelection to set
 	 */
 	public void setSourceSelection(TreeNode[] sourceSelection) {
 		this.sourceSelection = sourceSelection;
 	}
 
 	/**
 	 * @return the targetSelection
 	 */
 	public TreeNode[] getTargetSelection() {
 		return targetSelection;
 	}
 
 	/**
 	 * @param targetSelection
 	 *            the targetSelection to set
 	 */
 	public void setTargetSelection(TreeNode[] targetSelection) {
 		this.targetSelection = targetSelection;
 	}
 
 	/**
 	 * @return the buttonAdd
 	 */
 	public CommandButton getButtonAdd() {
 		return buttonAdd;
 	}
 
 	/**
 	 * @param buttonAdd
 	 *            the buttonAdd to set
 	 */
 	public void setButtonAdd(CommandButton buttonAdd) {
 		this.buttonAdd = buttonAdd;
 	}
 
 	/**
 	 * @return the buttonRemove
 	 */
 	public CommandButton getButtonRemove() {
 		return buttonRemove;
 	}
 
 	/**
 	 * @param buttonRemove
 	 *            the buttonRemove to set
 	 */
 	public void setButtonRemove(CommandButton buttonRemove) {
 		this.buttonRemove = buttonRemove;
 	}
 
 	/**
 	 * @return the buttonUp
 	 */
 	public CommandButton getButtonUp() {
 		return buttonUp;
 	}
 
 	/**
 	 * @param buttonUp
 	 *            the buttonUp to set
 	 */
 	public void setButtonUp(CommandButton buttonUp) {
 		this.buttonUp = buttonUp;
 	}
 
 	/**
 	 * @return the buttonDown
 	 */
 	public CommandButton getButtonDown() {
 		return buttonDown;
 	}
 
 	/**
 	 * @param buttonDown
 	 *            the buttonDown to set
 	 */
 	public void setButtonDown(CommandButton buttonDown) {
 		this.buttonDown = buttonDown;
 	}
 
 	/**
 	 * @return the buttonApply
 	 */
 	public CommandButton getButtonApply() {
 		return buttonApply;
 	}
 
 	/**
 	 * @param buttonApply
 	 *            the buttonApply to set
 	 */
 	public void setButtonApply(CommandButton buttonApply) {
 		this.buttonApply = buttonApply;
 	}
 }
