 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.client.ui;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.daxplore.presenter.client.event.QueryUpdateEvent;
 import org.daxplore.presenter.client.event.QueryUpdateHandler;
 import org.daxplore.presenter.client.event.SelectionUpdateEvent;
 import org.daxplore.presenter.client.json.Groups;
 import org.daxplore.presenter.client.resources.UIResources;
 import org.daxplore.presenter.client.resources.UITexts;
 import org.daxplore.presenter.shared.QueryDefinition;
 import org.daxplore.presenter.shared.QuestionMetadata;
 
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * A widget that contains a tree of all the questions that can be shown
  * in a chart.
  * 
  * <p>The tree is made up from groups of questions, that the user can
  * select.</p>
  */
 public class QuestionPanel extends Composite implements QueryUpdateHandler{
 	
 	protected final QuestionMetadata questions;
 	protected final EventBus eventBus;
 	
 	private Tree treeRoot;
 	private QuestionTreeItem selected;
 	
 	private VerticalPanel vp = new VerticalPanel();
 
 	@Inject
 	QuestionPanel(QuestionMetadata questions, Groups groups, EventBus eventBus, UITexts uiTexts, UIResources uiResources) {
 		this.questions = questions;
 		this.eventBus = eventBus;
 		
 		treeRoot = new Tree(uiResources, false);
 		
 		Label header = new Label(uiTexts.pickAQuestionHeader());
 		header.addStyleName("daxplore-QuestionPanel-header");
 		vp.add(header);
 		vp.setCellHeight(header, "30px");
 
 		// Set up questiontree
 
 		treeRoot.addSelectionHandler(new QuestionSelectionHandler());
 		treeRoot.addOpenHandler(new GroupOpenHandler());
 
 		for (int i = 0; i < groups.getGroupCount(); i++) {
 			String txt = groups.getGroupName(i);
 			txt = "<span class=\"daxplore-QuestionPanel-branch\">&nbsp;" + txt.substring(0, 1).toUpperCase() + txt.substring(1).toLowerCase() + "&nbsp;</span>";
 			GroupItem gr = new GroupItem(txt);
 			List<String> qlist = groups.getQuestionIDs(i);
 			for (String q : qlist) {
 				boolean ho = questions.hasSecondary(q);
 				QuestionTreeItem qi = new QuestionTreeItem("&nbsp;" + questions.getShortText(q) + (ho ? " <span class=\"super\">'92</span>" : "") + "&nbsp;", q);
 				qi.setTitle(questions.getFullText(q));
 				gr.addItem(qi);
 			}
 			treeRoot.addItem(gr);
 		}
 		treeRoot.ensureSelectedItemVisible();
 
 		vp.add(treeRoot);
 		vp.setCellVerticalAlignment(treeRoot, HasVerticalAlignment.ALIGN_TOP);
 		vp.setWidth("100%");
 		initWidget(vp);
 		
 		QueryUpdateEvent.register(eventBus, this);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onQueryUpdate(QueryUpdateEvent event) {
 		setQueryDefinition(event.getQueryDefinition());
 	}
 
 	/**
 	 * Set a new query definition, updating which group is open and
 	 * what question is selected.
 	 * 
 	 * @param queryDefinition
 	 *            the new query definition
 	 */
 	public void setQueryDefinition(QueryDefinition queryDefinition) {
 		Iterator<TreeItem> iter = treeRoot.treeItemIterator();
 		while (iter.hasNext()) {
 			TreeItem curr = iter.next();
 			if (curr instanceof QuestionTreeItem) {
 				if (((QuestionTreeItem) curr).getQuestionID().equalsIgnoreCase(queryDefinition.getQuestionID())) {
 					treeRoot.setSelectedItem(null, false);
 					curr.setSelected(true);
 					selected = (QuestionTreeItem) curr;
 					curr.getParentItem().setState(true, true);
 				} else {
 					curr.setSelected(false);
 				}
 			}
 		}
 		treeRoot.ensureSelectedItemVisible();
 	}
 
 	/**
 	 * Get the questionID of the currently selected question.
 	 * 
 	 * @return the questionID
 	 */
 	public String getQuestionID() {
 		if (selected != null) {
 			return selected.getQuestionID();
 		} else {
 			return "";
 		}
 
 	}
 
 	/**
 	 * A handler that manages the automatic closing of groups.
 	 */
 	protected class GroupOpenHandler implements OpenHandler<TreeItem> {
 
 		@Override
 		public void onOpen(OpenEvent<TreeItem> event) {
 			TreeItem t = event.getTarget();
 			for (int i = 0; i < treeRoot.getItemCount(); i++) {
 				TreeItem gr = treeRoot.getItem(i);
 				if (gr instanceof GroupItem) {
 					GroupItem g = (GroupItem) gr;
 					if (g.getState() == true && (g != t) && !(g.hasSelectedChild())) {
 						g.setState(false, false);
 					}
 				}
 			}
 			treeRoot.ensureSelectedItemVisible();
 		}
 
 	}
 
 	/**
 	 * A handler that manages what happens when questions are selected,
 	 * including firing events on the system's eventbus. 
 	 */
 	protected class QuestionSelectionHandler implements SelectionHandler<TreeItem> {
 
 		GroupItem prev;
 
 		@Override
 		public void onSelection(SelectionEvent<TreeItem> event) {
 			if (event.getSelectedItem() instanceof QuestionTreeItem) {
 				QuestionTreeItem qi = (QuestionTreeItem) event.getSelectedItem();
 				selected = qi;
 				Iterator<TreeItem> iter = treeRoot.treeItemIterator();
 				while (iter.hasNext()) {
 					TreeItem n = iter.next();
 					if (n instanceof QuestionTreeItem) {
 						QuestionTreeItem curr = (QuestionTreeItem) n;
 						if (curr != selected) {
 							curr.setSelected(false);
 							GroupItem p = (GroupItem) curr.getParentItem();
 							if (!p.hasSelectedChild()) {
 								p.setState(false);
 							}
 						}
 					} else if (n instanceof GroupItem) {
 						if (n.getState() && !(((GroupItem) n).hasSelectedChild())) {
 							n.setState(false);
 						}
 					}
 				}
 				eventBus.fireEvent(new SelectionUpdateEvent());
 			} else if (event.getSelectedItem() instanceof GroupItem) {
 				GroupItem gi = (GroupItem) event.getSelectedItem();
 				gi.setSelected(false);
 				if (selected != null) {
 					selected.setSelected(true);
 				}
 
 				if (gi.getState() == true && (!gi.hasSelectedChild())) {
 					prev = gi;
 					gi.setState(false);
 				} else if (gi.getState() == false) {
 					if (prev != null && prev == gi) {
 						prev = null;
 					} else {
 						gi.setState(true);
 						prev = null;
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * A class that defines a group as displayed in the question tree.
 	 */
 	private class GroupItem extends TreeItem {
 
 		GroupItem(String text) {
			super(text);
 		}
 
 		public boolean hasSelectedChild() {
 			boolean out = false;
 			for (int i = 0; i < getChildCount(); i++) {
 				out = out || getChild(i).isSelected();
 			}
 			return out;
 		}
 	}
 }
