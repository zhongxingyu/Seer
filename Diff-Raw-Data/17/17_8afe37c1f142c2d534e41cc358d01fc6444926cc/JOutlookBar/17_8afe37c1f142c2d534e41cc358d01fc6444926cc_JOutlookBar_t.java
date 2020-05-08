 package devhood.im.sim.ui.util;
 
 //Import the GUI classes
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import devhood.im.sim.ui.action.BarAction;
 
 /**
  * A JOutlookBar provides a component that is similar to a JTabbedPane, but
  * instead of maintaining tabs, it uses Outlook-style bars to control the
  * visible component
  *
  * Quelle: http://www.informit.com/guides/content.aspx?g=java&seqNum=236
  */
 public class JOutlookBar extends JPanel implements ActionListener {
 	/**
 	 * The top panel: contains the buttons displayed on the top of the
 	 * JOutlookBar
 	 */
 	private JPanel topPanel = new JPanel(new GridLayout(1, 1));
 
 	/**
 	 * The bottom panel: contains the buttons displayed on the bottom of the
 	 * JOutlookBar
 	 */
 	private JPanel bottomPanel = new JPanel(new GridLayout(1, 1));
 
 	/**
 	 * A LinkedHashMap of bars: we use a linked hash map to preserve the order
 	 * of the bars
 	 */
 	private Map<String, BarInfo> bars = new LinkedHashMap<String, BarInfo>();
 
 	/**
 	 * The currently visible bar (zero-based index)
 	 */
 	private int visibleBar = 0;
 
 	/**
 	 * A place-holder for the currently visible component
 	 */
 	private JComponent visibleComponent = null;
 
 	private Map<JComponent, String> componentNameMap = new HashMap<JComponent, String>();
 
 	private Map<String, JComponent> nameComponent = new HashMap<String, JComponent>();
 
 	private Object firstBarLabel;
 
 	/**
 	 * Creates a new JOutlookBar; after which you should make repeated calls to
 	 * addBar() for each bar
 	 */
 	public JOutlookBar() {
 		this.setLayout(new BorderLayout());
 		this.add(topPanel, BorderLayout.NORTH);
 		this.add(bottomPanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Adds the specified component to the JOutlookBar and sets the bar's name
 	 *
 	 * @param name
 	 *            The name of the outlook bar
 	 * @param componenet
 	 *            The component to add to the bar
 	 */
 	public void addBar(String name, JComponent component, Icon icon,
 			MouseListener mouseListener) {
 		component.addMouseListener(visiblecomponentMouseListener);
 		BarInfo barInfo = null;
 		if (icon != null) {
 			barInfo = new BarInfo(name, component, icon);
 		} else {
 			barInfo = new BarInfo(name, component);
 		}
 
 		barInfo.getButton().addActionListener(this);
 
 		if (mouseListener != null) {
 			barInfo.getButton().addMouseListener(mouseListener);
 		}
 
 		this.bars.put(name, barInfo);
 		this.componentNameMap.put(component, name);
 		this.nameComponent.put(name, component);
 		render();
 	}
 
 	public String getSelectedBarName() {
 		return componentNameMap.get(getVisibleComponent());
 	}
 
 	public JComponent getBar(String name) {
 		return nameComponent.get(name);
 	}
 
 	public Set<String> getBars() {
 		return bars.keySet();
 	}
 
 	/**
 	 * Adds the specified component to the JOutlookBar and sets the bar's name
 	 *
 	 * @param name
 	 *            The name of the outlook bar
 	 * @param icon
 	 *            An icon to display in the outlook bar
 	 * @param componenet
 	 *            The component to add to the bar
 	 */
 	public void addBar(String name, Icon icon, JComponent component) {
 		BarInfo barInfo = new BarInfo(name, icon, component);
 		barInfo.getButton().addActionListener(this);
 		this.bars.put(name, barInfo);
 		render();
 	}
 
 	/**
 	 * Removes the specified bar from the JOutlookBar
 	 *
 	 * @param name
 	 *            The name of the bar to remove
 	 */
 	public void removeBar(String... names) {
 		for (String name : names) {
 			this.bars.remove(name);
 			Object o = nameComponent.get(name);
 			componentNameMap.remove(o);
 
 			nameComponent.remove(name);
 		}
 		render();
 
 	}
 
 	/**
 	 * Returns the index of the currently visible bar (zero-based)
 	 *
 	 * @return The index of the currently visible bar
 	 */
 	public int getVisibleBar() {
 		return this.visibleBar;
 	}
 
 	/**
 	 * Programmatically sets the currently visible bar; the visible bar index
 	 * must be in the range of 0 to size() - 1
 	 *
 	 * @param visibleBar
 	 *            The zero-based index of the component to make visible
 	 */
 	public void setVisibleBar(int visibleBar) {
 		if (visibleBar > 0 && visibleBar < this.bars.size() - 1) {
 			this.visibleBar = visibleBar;
 			render();
 		}
 	}
 
 	/**
 	 * Causes the outlook bar component to rebuild itself; this means that it
 	 * rebuilds the top and bottom panels of bars as well as making the
 	 * currently selected bar's panel visible
 	 */
 	public void render() {
 		// Compute how many bars we are going to have where
 		int totalBars = this.bars.size();
 		int topBars = this.visibleBar + 1;
 		int bottomBars = totalBars - topBars;
 
 		// Get an iterator to walk through out bars with
 		Iterator itr = this.bars.keySet().iterator();
 
 		Set<String> titles = this.bars.keySet();
 		LinkedList<String> orderedBarTitles = new LinkedList<String>();
 		for (String title : titles) {
 			if (firstBarLabel.equals(title)) {
 				orderedBarTitles.addFirst(title);
 			} else {
 				orderedBarTitles.add(title);
 			}
 		}
 
 		// Render the top bars: remove all components, reset the GridLayout to
 		// hold to correct number of bars, add the bars, and "validate" it to
 		// cause it to re-layout its components
 		this.topPanel.removeAll();
 		GridLayout topLayout = (GridLayout) this.topPanel.getLayout();
 		topLayout.setRows(topBars);
 		BarInfo barInfo = null;
 		for (int i = 0; i < topBars; i++) {
 			if (itr.hasNext()) {
 				String barName = (String) itr.next();
 				barInfo = this.bars.get(barName);
 				this.topPanel.add(barInfo.getButton());
 			} else {
 				topPanel.removeAll();
 			}
 		}
 		this.topPanel.validate();
 
 		// Render the center component: remove the current component (if there
 		// is one) and then put the visible component in the center of this
 		// panel
 		if (this.visibleComponent != null) {
 			this.remove(this.visibleComponent);
 		}
 		this.visibleComponent = barInfo.getComponent();
 		this.add(visibleComponent, BorderLayout.CENTER);
 
 		// Render the bottom bars: remove all components, reset the GridLayout
 		// to
 		// hold to correct number of bars, add the bars, and "validate" it to
 		// cause it to re-layout its components
 		this.bottomPanel.removeAll();
 		GridLayout bottomLayout = (GridLayout) this.bottomPanel.getLayout();
 		bottomLayout.setRows(bottomBars);
 		for (int i = 0; i < bottomBars; i++) {
 			String barName = (String) itr.next();
 			barInfo = this.bars.get(barName);
 			this.bottomPanel.add(barInfo.getButton());
 		}
 		this.bottomPanel.validate();
 
 		// Validate all of our components: cause this container to re-layout its
 		// subcomponents
 		this.validate();
 	}
 
 	/**
 	 * Invoked when one of our bars is selected
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() instanceof JButton) {
 			String title = ((JButton) e.getSource()).getText();
 			showTabSelection(title, true);
 		}
 	}
 
 	public void showTabSelection(String buttonName, boolean executeListeners) {
 		int currentBar = 0;
 		for (Iterator i = this.bars.keySet().iterator(); i.hasNext();) {
 			String barName = (String) i.next();
 			BarInfo barInfo = this.bars.get(barName);
 			if (barInfo.getName().equals(buttonName)) {
 				// Found the selected button
 				this.visibleBar = currentBar;
 				render();
 				if (executeListeners) {
 					onBarSelected(barName);
 				}
 				return;
 			}
 			currentBar++;
 		}
 	}
 
 	private BarAction onbarSelectedAction;
 
 	/**
 	 * um auf auswahl zu reagieren.
 	 *
 	 * @param action
 	 */
 	public void setOnBarSelected(BarAction action) {
 		onbarSelectedAction = action;
 	}
 
 	private void onBarSelected(String barName) {
 		if (onbarSelectedAction != null) {
 			onbarSelectedAction.execute(barName);
 		}
 	}
 
 	/**
 	 * Internal class that maintains information about individual Outlook bars;
 	 * specifically it maintains the following information:
 	 *
 	 * name The name of the bar button The associated JButton for the bar
 	 * component The component maintained in the Outlook bar
 	 */
 	class BarInfo {
 		/**
 		 * The name of this bar
 		 */
 		private String name;
 
 		/**
 		 * The JButton that implements the Outlook bar itself
 		 */
 		private JButton button;
 
 		/**
 		 * The component that is the body of the Outlook bar
 		 */
 		private JComponent component;
 
 		/**
 		 * Creates a new BarInfo
 		 *
 		 * @param name
 		 *            The name of the bar
 		 * @param component
 		 *            The component that is the body of the Outlook Bar
 		 */
 		public BarInfo(String name, JComponent component) {
 			this.name = name;
 			this.component = component;
 			this.button = new JButton(name);
 		}
 
 		public BarInfo(String name, JComponent component, Icon icon) {
 			this(name, component);
 			this.button.setIcon(icon);
 		}
 
 		/**
 		 * Creates a new BarInfo
 		 *
 		 * @param name
 		 *            The name of the bar
 		 * @param icon
 		 *            JButton icon
 		 * @param component
 		 *            The component that is the body of the Outlook Bar
 		 */
 		public BarInfo(String name, Icon icon, JComponent component) {
 			this.name = name;
 			this.component = component;
 			this.button = new JButton(name, icon);
 		}
 
 		/**
 		 * Returns the name of the bar
 		 *
 		 * @return The name of the bar
 		 */
 		public String getName() {
 			return this.name;
 		}
 
 		/**
 		 * Sets the name of the bar
 		 *
 		 * @param The
 		 *            name of the bar
 		 */
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		/**
 		 * Returns the outlook bar JButton implementation
 		 *
 		 * @return The Outlook Bar JButton implementation
 		 */
 		public JButton getButton() {
 			return this.button;
 		}
 
 		/**
 		 * Returns the component that implements the body of this Outlook Bar
 		 *
 		 * @return The component that implements the body of this Outlook Bar
 		 */
 		public JComponent getComponent() {
 			return this.component;
 		}
 	}
 
 	private MouseListener visiblecomponentMouseListener;
 
 	public JComponent getVisibleComponent() {
 		return visibleComponent;
 	}
 
 	public void setVisibleComponent(JComponent visibleComponent) {
 		this.visibleComponent = visibleComponent;
 	}
 
 	public boolean hasBar(String name) {
 		return bars.containsKey(name);
 	}
 
 	public void setVisibleComponentMouseListener(MouseAdapter mouseAdapter) {
 		this.visiblecomponentMouseListener = mouseAdapter;
 	}
 
 	public String getTitleOfButton(int i) {
 		return (String) this.bars.keySet().toArray()[i];
 	}
 
 	public Object getFirstBarLabel() {
 		return firstBarLabel;
 	}
 
 	public void setFirstBarLabel(Object firstBarLabel) {
 		this.firstBarLabel = firstBarLabel;
 	}
 }
