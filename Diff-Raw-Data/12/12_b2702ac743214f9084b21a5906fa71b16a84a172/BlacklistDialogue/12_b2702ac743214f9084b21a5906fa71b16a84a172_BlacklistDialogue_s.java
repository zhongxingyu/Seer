 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.filter.blacklist.impl.desktop;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.swing.AbstractListModel;
 import javax.swing.ComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import org.osgi.service.component.ComponentContext;
 import org.paxle.desktop.DIComponent;
 import org.paxle.desktop.Utilities;
 import org.paxle.filter.blacklist.IBlacklist;
 import org.paxle.filter.blacklist.IBlacklistManager;
 import org.paxle.filter.blacklist.InvalidFilenameException;
 
 /**
  * @scr.component immediate="true"
  * @scr.service interface="org.paxle.desktop.DIComponent"
  */
 public class BlacklistDialogue extends JPanel implements DIComponent, ActionListener, DocumentListener {
 	
 	private static final long serialVersionUID = 1L;
 
 	/** 
 	 * @scr.reference
 	 */
 	protected IBlacklistManager blacklistFilter = null;
 	
 	private static final Dimension DIM = new Dimension(400, 400);
 	
	private static final String AC_LIST_CREATE = "";
	private static final String AC_LIST_DELETE = "";
	private static final String AC_LIST_SELECT = "";
	private static final String AC_ITEM_ADD = "";
	private static final String AC_ITEM_DEL = "";
	private static final String AC_ITEM_EDIT = "";
 	
 	private FilterListsComboBoxModel flm;
 	private JComboBox listSelCBox;
 	private final JList itemList = new JList();
 	private final JButton listAddB = Utilities.setButtonProps(new JButton(), "Create", this, AC_LIST_CREATE, -1, null);
 	private final JButton listDelB = Utilities.setButtonProps(new JButton(), "Delete", this, AC_LIST_DELETE, -1, null);
 	private final JButton itemAddB = Utilities.setButtonProps(new JButton(), "Add Entry", this, AC_ITEM_ADD, -1, null);
 	private final JButton itemDelB = Utilities.setButtonProps(new JButton(), "Remove Entry", this, AC_ITEM_DEL, -1, null);
 	private final JButton itemEditB = Utilities.setButtonProps(new JButton(), "Edit Entry", this, AC_ITEM_EDIT, -1, null);
 	private final JTextField itemF = new JTextField();
 	private final ListModel nullModel = new AbstractListModel() {
 		private static final long serialVersionUID = 1L;
 		public Object getElementAt(int index) {
 			return null;
 		}
 		public int getSize() {
 			return 0;
 		}
 	};
 	
 	private ItemListModel ilm = null;
 	
 	protected void activate(ComponentContext context) {
 		this.flm = new FilterListsComboBoxModel(blacklistFilter);
 		this.listSelCBox = new JComboBox(flm);
 		init();
 	}
 	
 	public void setFrame(Frame frame) {
 	}
 	
 	private void init() {
 		final JPanel listP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
 		listP.add(new JLabel("Blacklist:"));
 		listP.add(listSelCBox);
 		listP.add(listAddB);
 		listP.add(listDelB);
 		listSelCBox.setEditable(true);
 		listSelCBox.setActionCommand(AC_LIST_SELECT);
 		listSelCBox.addActionListener(this);
 		
 		final JPanel itemP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		itemP.add(itemAddB);
 		itemP.add(itemEditB);
 		itemP.add(itemDelB);
 		final JPanel itemAP = new JPanel(new GridLayout(2, 1));
 		itemAP.add(itemF);
 		itemF.getDocument().addDocumentListener(this);
 		itemAP.add(itemP);
 		
 		final JScrollPane scroll = new JScrollPane();
 		scroll.setViewportView(itemList);
 		
 		super.setLayout(new BorderLayout());
 		super.add(listP, BorderLayout.NORTH);
 		super.add(scroll, BorderLayout.CENTER);
 		super.add(itemAP, BorderLayout.SOUTH);
 	}
 	
 	public void changedUpdate(DocumentEvent e) {
 		// ignore
 	}
 	
 	public void insertUpdate(DocumentEvent e) {
 		validateItemF();
 	}
 	
 	public void removeUpdate(DocumentEvent e) {
 		validateItemF();
 	}
 	
 	private void validateItemF() {
 		boolean correct = false;
 		final String text = itemF.getText();
 		if (text.length() > 0) try {
 			Pattern.compile(itemF.getText());
 			correct = true;
 		} catch (PatternSyntaxException e) { /* ignore */ }
 		itemAddB.setEnabled(correct);
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		final String ac = e.getActionCommand();
 		try {
 			if (ac == AC_LIST_CREATE) {
 				final Object name = listSelCBox.getSelectedItem();
 				if (name != null) {
 					itemList.setModel(ilm = new ItemListModel(blacklistFilter.createList(name.toString())));
 					flm.update();
 					listSelCBox.setSelectedItem(name.toString());
 				}
 			} else if (ac == AC_LIST_DELETE) {
 				final int selected = listSelCBox.getSelectedIndex();
 				final Object name = listSelCBox.getSelectedItem();
 				if (name != null) {
 					final IBlacklist bl = blacklistFilter.getList(name.toString());
 					if (bl != null)
 						bl.delete();
 					flm.update();
 				}
 				listSelCBox.setSelectedIndex(Math.max(-1, Math.min(selected, flm.getSize() - 1)));
 			} else if (ac == AC_LIST_SELECT) {
 				final Object name = listSelCBox.getSelectedItem();
 				final IBlacklist bl;
 				if (name != null && (bl = blacklistFilter.getList(name.toString())) != null) {
 					itemList.setModel(ilm = new ItemListModel(bl));
 				} else {
 					ilm = null;
 					itemList.setModel(nullModel);
 				}
 			} else if (ilm != null) {
 				if (ac == AC_ITEM_ADD) {
 					ilm.add(itemF.getText());
 				} else if (ac == AC_ITEM_DEL) {
 					final Object[] items = itemList.getSelectedValues();
 					final String[] patterns = new String[items.length];
 					for (int i=0; i<items.length; i++)
 						patterns[i] = items[i].toString();
 					ilm.remove(patterns);
 				} else if (ac == AC_ITEM_EDIT) {
 					final Object item = itemList.getSelectedValue();
 					if (item != null) {
 						ilm.edit(item.toString(), itemF.getText());
 						final ListSelectionModel lsm = itemList.getSelectionModel();
 						lsm.removeSelectionInterval(lsm.getLeadSelectionIndex(), lsm.getLeadSelectionIndex());
 					}
 				}
 			} else if (e.getSource() == listSelCBox) {
 				// pressed "enter"
 			}
 		} catch (InvalidFilenameException ee) {  }
 	}
 	
 	public Dimension getWindowSize() {
 		return DIM;
 	}
 	
 	public String getTitle() {
 		return "Blacklist Configurator";
 	}
 	
 	public Container getContainer() {
 		return this;
 	}
 	
 	public void close() {
 	}
 	
 	private class ItemListModel extends AbstractListModel implements ListModel {
 		
 		private static final long serialVersionUID = 1L;
 		
 		private final IBlacklist bl;
 		
 		public ItemListModel(final IBlacklist bl) {
 			this.bl = bl;
 		}
 		
 		public void remove(final String pattern) {
 			final int idx = getIndexOf(pattern);
 			if (idx >= 0 && bl.removePattern(pattern))
 				fireIntervalRemoved(this, idx, idx);
 		}
 		
 		public void remove(final String[] patterns) {
 			if (patterns.length > 0) {
 				Arrays.sort(patterns);
 				final int firstIdx = getIndexOf(patterns[0]);
 				final int lastIdx = (patterns.length > 1) ? getIndexOf(patterns[patterns.length - 1]) : firstIdx;
 				for (final String pattern : patterns)
 					bl.removePattern(pattern);
 				fireContentsChanged(this, firstIdx, lastIdx);
 			}
 		}
 		
 		public void add(final String pattern) {
 			if (bl.addPattern(pattern)) {
 				final int idx = getIndexOf(pattern);
 				fireIntervalAdded(this, idx, idx);
 			}
 		}
 		
 		public void edit(final String pattern, final String toPattern) {
 			final int prevIdx = getIndexOf(pattern);
 			if (prevIdx >= 0 && bl.editPattern(pattern, toPattern)) {
 				final int nextIdx = getIndexOf(toPattern);
 				fireContentsChanged(this, prevIdx, nextIdx);
 			}
 		}
 		
 		public int getSize() {
 			return bl.getPatternList().size();
 		}
 		
 		private int getIndexOf(final String o) {
 			final List<String> list = bl.getPatternList();
 			Collections.sort(list);
 			return Collections.binarySearch(list, o);
 		}
 		
 		public Object getElementAt(int index) {
 			final List<String> list = bl.getPatternList();
 			Collections.sort(list);
 			return list.get(index);
 		}
 	}
 	
 	private class FilterListsComboBoxModel extends AbstractListModel implements ComboBoxModel {
 		
 		private static final long serialVersionUID = 1L;
 
 		private final IBlacklistManager blacklistFilter;
 
 		public FilterListsComboBoxModel(final IBlacklistManager blacklistFilter) {
 			this.blacklistFilter = blacklistFilter;
 		}
 		
 		private Object selected = null;
 		
 		public Object getSelectedItem() {
 			return selected;
 		}
 		
 		public void setSelectedItem(Object anItem) {
 			selected = anItem;
 			final int idx = indexOf(anItem);
 			super.fireContentsChanged(this, idx, idx);
 		}
 		
 		public void update() {
 			fireContentsChanged(this, 0, getSize());
 		}
 		
 		private int indexOf(final Object item) {
 			final List<String> lists = blacklistFilter.getLists();
 			Collections.sort(lists);
 			return Collections.binarySearch(lists, (String)item);
 		}
 		
 		public int getSize() {
 			return blacklistFilter.getLists().size();
 		}
 		
 		public Object getElementAt(int index) {
 			final List<String> lists = blacklistFilter.getLists();
 			Collections.sort(lists);
 			return lists.get(index);
 		}
 	}
 }
