 package com.dafrito.rfe.gui.debug;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.LinkedList;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.tree.TreePath;
 
 public class Debug_Listener extends JPanel implements ActionListener, ComponentListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6965306788286409632L;
 	// Core stuff
 	private DebugEnvironment debugger;
 	private int width;
 	// GUI stuff
 	protected JButton createListener, sendToFilter, refresh, jump;
 	protected JCheckBox isSynchronized, isCapturing, defaultFilter;
 	private JSplitPane treeAndHotspotSplitPane;
 	// Debug components and filter-parents and children
 	private Debug_Tree treePanel;
 	private Debug_Hotspots hotspotPanel;
 	private Debug_Listener source;
 	private java.util.List<Debug_Listener> childOutputs = new LinkedList<Debug_Listener>();
 	protected boolean isListening = false;
 	// CONSTRUCTOR!!
 	private String threadName;
 
 	public Debug_Listener(String threadName, DebugEnvironment debugger, Debug_Listener source, String name) {
 		this.threadName = threadName;
 		this.treePanel = new Debug_Tree(new Debug_Filter(this));
 		this.debugger = debugger;
 		this.width = this.debugger.getWidth();
 		this.source = source;
 		this.setLayout(new BorderLayout());
 		// Set up top-of-the-window buttons
 		JPanel buttons = new JPanel();
 		this.add(buttons, BorderLayout.NORTH);
 		buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
 		buttons.add(this.createListener = new JButton("Create Listener"));
 		buttons.add(this.sendToFilter = new JButton("Send to Filter"));
 		buttons.add(this.refresh = new JButton("Refresh"));
 		buttons.add(this.isSynchronized = new JCheckBox("Synchronized"));
 		buttons.add(this.isCapturing = new JCheckBox("Capturing"));
 		buttons.add(this.defaultFilter = new JCheckBox("Default Filter"));
 		buttons.add(this.jump = new JButton("Jump to Parent"));
 		// Set up split-pane
 		this.treeAndHotspotSplitPane = new JSplitPane();
 		this.add(this.treeAndHotspotSplitPane);
 		this.treeAndHotspotSplitPane.setLeftComponent(new JScrollPane(this.treePanel));
 		JTabbedPane hotspotAndFiltersPanel = new JTabbedPane();
 		this.treeAndHotspotSplitPane.setRightComponent(hotspotAndFiltersPanel);
 		// Set up hotspot-panel
 		hotspotAndFiltersPanel.add("Hotspots", this.hotspotPanel = new Debug_Hotspots(this.treePanel));
 		hotspotAndFiltersPanel.add("Filters", this.treePanel.getFilter());
 		if (this.isUnfiltered()) {
 			this.jump.setEnabled(false);
 			this.isSynchronized.setEnabled(false);
 			this.refresh.setEnabled(false);
 			this.isCapturing.setEnabled(false);
 			this.defaultFilter.setEnabled(false);
 			hotspotAndFiltersPanel.setEnabledAt(1, false);
 		}
 		// Listeners!
 		this.addComponentListener(this);
 		this.sendToFilter.addActionListener(this);
 		this.defaultFilter.addActionListener(this);
 		this.createListener.addActionListener(this);
 		this.refresh.addActionListener(this);
 		this.jump.addActionListener(this);
 		this.isSynchronized.addActionListener(this);
 		this.isCapturing.addActionListener(this);
 		this.setVisible(true);
 		this.treeAndHotspotSplitPane.setDividerLocation((int) (this.width * .68));
 	}
 
 	// ActionListener implementation
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource().equals(this.defaultFilter)) {
 			this.setFiltering(this.defaultFilter.isSelected());
 		} else if (event.getSource().equals(this.isSynchronized)) {
 			this.setSynchronized(this.isSynchronized.isSelected());
 		} else if (event.getSource().equals(this.isCapturing)) {
 			this.setCapturing(this.isCapturing.isSelected());
 		} else if (event.getSource().equals(this.createListener)) {
 			this.createListener();
 		} else if (event.getSource().equals(this.sendToFilter)) {
 			this.sendToFilter();
 		} else if (event.getSource().equals(this.refresh)) {
 			this.getTreePanel().refresh();
 		} else if (event.getSource().equals(this.jump)) {
 			this.jump();
 		}
 	}
 
 	public void addChildOutput(Debug_Listener output) {
 		this.childOutputs.add(output);
 	}
 
 	// Command functions
 	public void clearTab() {
 		this.hotspotPanel.reset();
 		this.treePanel.reset();
 		System.gc();
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent x) {
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent x) {
 	}
 
 	// ComponentListener implementation
 	@Override
 	public void componentResized(ComponentEvent x) {
 		double location = ((double) this.treeAndHotspotSplitPane.getDividerLocation()) / (double) this.width;
 		if (location > 1) {
 			location = 1;
 		}
 		this.treeAndHotspotSplitPane.setDividerLocation((int) (this.getWidth() * location));
 		this.width = this.getWidth();
 	}
 
 	@Override
 	public void componentShown(ComponentEvent x) {
 	}
 
 	public Debug_Listener createListener() {
 		return this.debugger.addOutputListener(this, this.treePanel.getFirstAvailableFilter());
 	}
 
 	// Quick retrievals
 	public DebugEnvironment getDebugger() {
 		return this.debugger;
 	}
 
 	public Debug_Hotspots getHotspotPanel() {
 		return this.hotspotPanel;
 	}
 
 	public Debug_Listener getSource() {
 		return this.source;
 	}
 
 	public String getThreadName() {
 		return this.threadName;
 	}
 
 	public Debug_Tree getTreePanel() {
 		return this.treePanel;
 	}
 
 	public boolean isCapturing() {
 		return this.isCapturing.isSelected();
 	}
 
 	public boolean isFiltering() {
 		return this.defaultFilter.isSelected();
 	}
 
 	public boolean isSynchronizing() {
 		return this.isSynchronized.isSelected();
 	}
 
 	public boolean isUnfiltered() {
 		return this.source == null;
 	}
 
 	public void jump() {
 		TreePath path = this.getTreePanel().getSelectedRelativeTreePath();
 		this.debugger.focusOnOutput(this.source);
 		this.source.getTreePanel().showTreePath(path);
 	}
 
 	public Debug_Listener promptCreateListener() {
 		Object filter = this.treePanel.getFirstAvailableFilter();
 		if (this.treePanel.getSelectedNode() != null) {
 			filter = JOptionPane.showInputDialog(this, "Insert Listener Name", "Listener Creation", JOptionPane.PLAIN_MESSAGE, null, null, filter);
 		}
 		return this.debugger.addOutputListener(this, filter);
 	}
 
 	public void removeTab() {
 		for (Debug_Listener output : this.childOutputs) {
 			output.setSource(this.source);
 			output.clearTab();
 		}
 		if (this.debugger.getFilteringOutput() != null && this.debugger.getFilteringOutput().equals(this)) {
 			this.debugger.setFilteringOutput(null);
 		}
 	}
 
 	public void sendToFilter() {
 		if (this.debugger.getFilteringOutput() == null) {
 			Debug_Listener listener;
 			listener = this.createListener();
 			if (listener == null) {
 				JOptionPane.showMessageDialog(null, "No filtering output defined.", "Undefined Filter", JOptionPane.WARNING_MESSAGE);
			} else {
				listener.setFiltering(true);
 			}
 			return;
 		}
 		Object[] array = null;
 		Object value = null;
 		if (this.getTreePanel().getSelectedNode() != null) {
 			value = this.getTreePanel().getSelectedNode().getData().toString();
 			if (this.getTreePanel().getSelectedNode().getGroup() != null) {
 				value = this.getTreePanel().getSelectedNode().getGroup();
 				array = new Object[2];
 				array[0] = this.getTreePanel().getSelectedNode().getGroup();
 				array[1] = this.getTreePanel().getSelectedNode().getData();
 			}
 		}
 		Object obj = JOptionPane.showInputDialog(this, "Insert New Filter", "Adding Filter", JOptionPane.QUESTION_MESSAGE, null, array, value);
 		if (obj != null) {
 			this.debugger.getFilteringOutput().getTreePanel().getFilter().addFilter(obj);
 		}
 	}
 
 	public void setCapturing(boolean isCapturing) {
 		this.isCapturing.setSelected(isCapturing);
 		if (this.isCapturing.isSelected()) {
 			this.refresh.setEnabled(false);
 			this.isSynchronized.setEnabled(false);
 			this.jump.setEnabled(false);
 		} else {
 			if (!this.defaultFilter.isSelected()) {
 				this.refresh.setEnabled(true);
 				this.isSynchronized.setEnabled(true);
 			}
 			this.jump.setEnabled(true);
 		}
 	}
 
 	public void setFiltering(boolean isFiltering) {
 		this.defaultFilter.setSelected(isFiltering);
 		if (this.defaultFilter.isSelected()) {
 			if (this.debugger.getFilteringOutput() != null) {
 				this.debugger.getFilteringOutput().setFiltering(false);
 			}
 			this.isSynchronized.setSelected(true);
 			this.isSynchronized.setEnabled(false);
 			this.debugger.setFilteringOutput(this);
 		} else {
 			this.debugger.setFilteringOutput(null);
 			if (!this.isCapturing.isSelected()) {
 				this.isSynchronized.setEnabled(true);
 			}
 		}
 	}
 
 	public void setSource(Debug_Listener source) {
 		this.source = source;
 	}
 
 	// Setters
 	public void setSynchronized(boolean isSynchronized) {
 		this.isSynchronized.setSelected(isSynchronized);
 	}
 }
 
 class TreePathStruct {
 	private boolean isExpanding;
 	private TreePath path;
 
 	public TreePathStruct(TreePath path, boolean isExpanding) {
 		this.path = path;
 		this.isExpanding = isExpanding;
 	}
 
 	public TreePath getPath() {
 		return this.path;
 	}
 
 	public boolean isExpanding() {
 		return this.isExpanding;
 	}
 }
