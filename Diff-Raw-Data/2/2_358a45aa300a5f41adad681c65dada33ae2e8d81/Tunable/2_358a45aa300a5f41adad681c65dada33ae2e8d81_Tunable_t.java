 // vim: set ts=2: */
 package cytoscape.layout;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.data.CyAttributes;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import javax.swing.BoxLayout;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 
 
 /**
  * Tunables are typed objects that maintain properties and an
  * easy way to generate a settings panel to allow user manipulation
  * of them.
  * <p>
  * A Tunable is a utility that serves two important functions:
  * <ol><li>It provides a convenient way to read, set, and save
  * properties that can be used to influence algorithms or presentation</li>
  * <li>It provides a quick way to present an interface to the user
  * to allow them to update and change those properties.</li></ol>
  * <p>Tunables were originally written to support the needs of users
  * of Cytoscape's various layout algorithms (see {@link CyLayoutAlgorithm})
  * to access the various tunable parameters in the algorithms.  In general,
  * Tunables are collected together by the {@link cytoscape.util.ModuleProperties} system.
  * See {@link LayoutProperties} as a good example.
  * <h3>Basic Concepts</h3>
  * A Tunable can be thought of as a typed variable that knowns how
  * to initialize and save itself as a Cytoscape property, and present
  * itself to the user for input.  All tunables have the following
  * information:
  * <ul><li><b>name</b>: the name of the tunable, which is used to access
  * the tunable and to save it as a property.</li>
  * <li><b>description</b>: the description of the tunable, which is displayed
  * to the user for input.</li>
  * <li><b>type</b>: the type of the tunable, which must be one of 
  * {@link #INTEGER}, {@link #DOUBLE}, {@link #BOOLEAN}, {@link #STRING},
  * {@link #NODEATTRIBUTE}, {@link #EDGEATTRIBUTE}, {@link #LIST}, {@link #GROUP},
  * or {@link #BUTTON}.</li>
  * <li><b>value</b>: this is set in construction as the initial value, but is
  * also maintained as the value loaded from the Cytoscape properties or entered
  * by the user.  The type of this value depends on the <b>type</b> of the
  * Tunable:
  * <ul>
  * <li>{@link #INTEGER}: {@link Integer}</li>
  * <li>{@link #DOUBLE}: {@link Double}</li>
  * <li>{@link #BOOLEAN}: {@link Boolean}</li>
  * <li>{@link #STRING}: {@link String}</li>
  * <li>{@link #NODEATTRIBUTE}: {@link String}, or if {@link #MULTISELECT}, comma-separated list of Strings representing the selected attributes</li>
  * <li>{@link #EDGEATTRIBUTE}: {@link String}, or if {@link #MULTISELECT}, comma-separated list of Strings representing the selected attributes</li>
  * <li>{@link #LIST}: {@link Integer}, or if {@link #MULTISELECT}, comma-separated list of integers representing the selected attributes</li>
  * </ul></li>
  * <li><b>lowerBound</b>: for {@link #INTEGER} or {@link #DOUBLE} tunables,
  * the lowerBound is an optional {@link Integer} or {@link Double} value representing
  * the smallest integer or double that constitutes a legal
  * input.  For {@link #LIST} tunables, the lowerBound is an array of Objects representing the list to choose from, and for
  * {@link #NODEATTRIBUTE}, and {@link #EDGEATTRIBUTE} tunables, lowerBound is an optional array of {@link String}s that will
  * prefix the list of attributes.  This allows users to provide their own overrides into the list. For {@link #GROUP} tunables,
  * that have the {@link #COLLAPSABLE} flag set, this is a Boolean value that indicates whether the group is collapsed or expanded.</li>
  * <li><b>upperBound</b>: for {@link #INTEGER} or {@link #DOUBLE} tunables,
  * the upperBound is an optional {@link Integer} or {@link Double} value representing
  * the larget integer or double that constitutes a legal
  * input. </li>
  * <li><b>flag</b>: Flags provide hints as to the desired presentation of the Tunable or restrictions on the values. Currently, the flags
  * include: {@link #IMMUTABLE}, which prevents user entry; {@link #MULTISELECT}, which presents lists as a {@link javax.swing.JList} rather
  * than {@link javax.swing.JComboBox}; {@link #NOINPUT}, which indicates that this tunable should not be 
  * presented to the user; {@link #NUMERICATTRIBUTE}, which indicates for attribute lists that only numeric attributes 
  * should be used; and {@link #USESLIDER}, which suggests to the UI to use a slider to present this value.</li>
  *
  * </ul>
  * <h3>Common Usage</h3>
  * <p>
  * The most common usage of Tunables is to provide a list of values that influences the presentation or calculation of
  * a layout, clustering algorithm, etc.  When combined with {@link LayoutProperties}, tunables provide a very quick way
  * to put together a user interface and get the results from it.  A common approach would be for the algorithm to
  * include a section of code that creates all of the Tunables associated with the algorithm's parameters.  For example
  * this section from {@link csplugins.layout.algorithms.force#ForceDirectedLayout}:
  *
  * <pre><code>
  *     layoutProperties = new LayoutProperties("my_module");
  *     layoutProperties.add(new Tunable("standard", "Standard settings",
  *                          Tunable.GROUP, new Integer(2)));
  *
  *     layoutProperties.add(new Tunable("partition", "Partition graph before layout",
  *                          Tunable.BOOLEAN, new Boolean(true)));
  *
  *     layoutProperties.add(new Tunable("selected_only", "Only layout selected nodes",
  *                          Tunable.BOOLEAN, new Boolean(false)));
  *
  *     layoutProperties.add(new Tunable("force_alg_settings", "Algorithm settings",
  *                          Tunable.GROUP, new Integer(5), new Boolean(true), null, Tunable.COLLAPSABLE));
  *
  *     layoutProperties.add(new Tunable("defaultSpringCoefficient", "Default Spring Coefficient",
  *                          Tunable.DOUBLE, new Double(defaultSpringCoefficient)));
  *
  *     layoutProperties.add(new Tunable("defaultSpringLength", "Default Spring Length",
  *                          Tunable.DOUBLE, new Double(defaultSpringLength)));
  *
  *     layoutProperties.add(new Tunable("defaultNodeMass", "Default Node Mass",
  *                          Tunable.DOUBLE, new Double(defaultNodeMass)));
  *
  *     layoutProperties.add(new Tunable("numIterations", "Number of Iterations",
  *                          Tunable.INTEGER, new Integer(numIterations)));
  *
  *     layoutProperties.add(new Tunable("integrator", "Integration algorithm to use",
  *                          Tunable.LIST, new Integer(0),
  *                          (Object) integratorArray, (Object) null, 0));
  *
  * </code></pre>
  *
  * The values are read in an update method:
  * <pre><code>
  *  Tunable t = layoutProperties.get("selected_only");
  *  if ((t != null) && (t.valueChanged() || force))
  *    selectedOnly = ((Boolean) t.getValue()).booleanValue();
  *
  *  t = layoutProperties.get("partition");
  *  if ((t != null) && (t.valueChanged() || force))
  *       setPartition(t.getValue().toString());
  *
  *  t = layoutProperties.get("defaultSpringCoefficient");
  *  if ((t != null) && (t.valueChanged() || force))
  *    defaultSpringCoefficient = ((Double) t.getValue()).doubleValue();
  *
  *  t = layoutProperties.get("defaultSpringLength");
  *  if ((t != null) && (t.valueChanged() || force))
  *    defaultSpringLength = ((Double) t.getValue()).doubleValue();
  *
  *  t = layoutProperties.get("defaultNodeMass");
  *  if ((t != null) && (t.valueChanged() || force))
  *    defaultNodeMass = ((Double) t.getValue()).doubleValue();
  *
  *  t = layoutProperties.get("numIterations");
  *  if ((t != null) && (t.valueChanged() || force))
  *    numIterations = ((Integer) t.getValue()).intValue();
  *
  *  t = layoutProperties.get("integrator");
  *  if ((t != null) && (t.valueChanged() || force)) {
  *    if (((Integer) t.getValue()).intValue() == 0)
  *      integrator = new RungeKuttaIntegrator();
  *    else if (((Integer) t.getValue()).intValue() == 1)
  *      integrator = new EulerIntegrator();
  *    else
  *      return;
  *  }
  * </code></pre>
  *
  * {@link LayoutProperties} has a method {@link LayoutProperties#getTunablePanel()} that can be called to 
  * return a {@link javax.swing.JPanel} that contains all of the Tunable panels.  These are combined into
  * a dialog to allow users to set and update the values.  By using {@link #addTunableValueListener(cytoscape.layout.TunableListener)} a
  * caller can be notified when a user changes a value.
  *
  */
 public class Tunable implements FocusListener,ChangeListener,ActionListener,ItemListener,ListSelectionListener {
 	private String name;
 	private String desc;
 	private int type = STRING;
 	private int flag = 0;
 	private Object value;
 	private Object lowerBound;
 	private Object upperBound;
 	private JComponent inputField = null;
 	private JSlider slider = null;
 	private boolean valueChanged = true;
 	private String savedValue = null;
 	private boolean usingSlider = false;
 	private boolean collapsed = false;
 	private List<String>attributeList = null;
 	private List<TunableListener>listenerList = null;
 	
 	/****************************************************
 	 * Types
 	 ***************************************************/
 
 	/**
 	 * Tunables of type INTEGER allow data entry of integer
 	 * values, possibly bounded between lowerBound and
 	 * upperBound: if the flag {@link #USESLIDER} is set,
 	 * and the caller provides both lower and upper bounds,
 	 * the user is presented with a slider interface.
 	 */
 	final public static int INTEGER = 0;
 
 	/**
 	 * Tunables of type DOUBLE allow data entry of double
 	 * values, possibly bounded between lowerBound and
 	 * upperBound: if the flag {@link #USESLIDER} is set,
 	 * and the caller provides both lower and upper bounds,
 	 * the user is presented with a slider interface.
 	 */
 	final public static int DOUBLE = 1;
 
 	/**
 	 * Tunables of type BOOLEAN allow data entry of a
 	 * boolean value: this is presented to the user
 	 * as a simple check box.
 	 */
 	final public static int BOOLEAN = 2;
 
 	/**
 	 * Tunables of type STRING allow data entry of
 	 * text.
 	 */
 	final public static int STRING = 3;
 
 	/**
 	 * Tunables of type NODEATTRIBUTE present a
 	 * user with a list of the currently defined
 	 * node attributes for selection: if the
 	 * flag {@link #NUMERICATTRIBUTE} is set, only
 	 * integer or double attributes are shown, and if
 	 * the {@link #MULTISELECT} flag is set, the user
 	 * is presented with a list from which multiple
 	 * values can be selected, otherwise, the user
 	 * is presented with a combo box.
 	 */
 	final public static int NODEATTRIBUTE = 4;
 
 	/**
 	 * Tunables of type EDGEATTRIBUTE present a
 	 * user with a list of the currently defined
 	 * edge attributes for selection:  if the
 	 * flag {@link #NUMERICATTRIBUTE} is set, only
 	 * integer or double attributes are shown, and if
 	 * the {@link #MULTISELECT} flag is set, the user
 	 * is presented with a list from which multiple
 	 * values can be selected, otherwise, the user
 	 * is presented with a combo box.
 	 */
 	final public static int EDGEATTRIBUTE = 5;
 
 	/**
 	 * Tunables of type LIST present a
 	 * user with a list of values to select
 	 * from: if the {@link #MULTISELECT} flag is 
 	 * set, the user is presented with a list 
 	 * from which multiple values can be selected, 
 	 * otherwise, the user is presented with a 
 	 * combo box.
 	 */
 	final public static int LIST = 6;
 
 	/**
 	 * The GROUP Tunable provides a mechanism to
 	 * improve the asthetics of the interface:
 	 * the value of the GROUP Tunable indicates the
 	 * number of the subsequent tunables that should
 	 * be grouped together.
 	 */
 	final public static int GROUP = 7;
 
 	/**
 	 * The BUTTON Tunable provides a simple button
 	 * to be presented to the user: the caller can
 	 * provide an ActionListener to be called when
 	 * the button is selected.
 	 */
 	final public static int BUTTON = 8;
 
 	/****************************************************
 	 * Flags
 	 ***************************************************/
 
 	/**
 	 * When the NOINPUT flag is set, this Tunable is not
 	 * presented to the user, it is only used for property
 	 * settings.
 	 */
 	final public static int NOINPUT = 0x1;
 
 	/**
 	 * For attributes, indicate that the list should be restricted to integer
 	 * or float attributes.
 	 */
 	final public static int NUMERICATTRIBUTE = 0x2;
 
 	/**
 	 * For LIST, NODEATTRIBUTE, or EDGEATTRIBUTE types, use a list widget that
 	 * supports multiselect rather than a combo box.
 	 */
 	final public static int MULTISELECT = 0x4;
 
 	/**
  	 * For INTEGER or DOUBLE tunables, preferentially use a slider widget --  this
  	 * will *only* take effect if the upper and lower bounds are provided.
  	 */
 	final public static int USESLIDER = 0x8;
 
 	/**
 	 * If the IMMUTABLE flag is set, then the Tunable is presented as usual,
 	 * with both label and user widget, but the user widget is disabled.
 	 */
 	final public static int IMMUTABLE =0x10;
 
 	/**
 	 * If the COLLAPSABLE flag is set for a GROUP Tunable, then this Group of Tunables
 	 * should be collapsable, and will be rendered with a button to allow 
 	 * collapse/expand.
 	 */
 	final public static int COLLAPSABLE =0x20;
 
 	/**
 	 * Constructor to create a Tunable with no bounds
 	 * information, and no flag.
 	 *
 	 * @param name The name of the Tunable
 	 * @param desc The description of the Tunable
 	 * @param type Integer value that represents the type of
 	 *             the Tunable.  The type not only impact the
 	 *             way that the value is interpreted, but also
 	 *             the component used for the LayoutSettingsDialog
 	 * @param value The initial (default) value of the Tunable
 	 */
 	public Tunable(String name, String desc, int type, Object value) {
 		this(name, desc, type, value, null, null, 0);
 	}
 
 	/**
 	 * Constructor to create a Tunable with no bounds
 	 * information, but with a flag.
 	 *
 	 * @param name The name of the Tunable
 	 * @param desc The description of the Tunable
 	 * @param type Integer value that represents the type of
 	 *             the Tunable.  The type not only impact the
 	 *             way that the value is interpreted, but also
 	 *             the component used for the LayoutSettingsDialog
 	 * @param value The initial (default) value of the Tunable
 	 * @param flag The initial value of the flag.  This can be
 	 *             used to indicate that this tunable is not user
 	 *             changeable (e.g. debug), or to indicate if there
 	 *             is a specific type for the attributes.
 	 */
 	public Tunable(String name, String desc, int type, Object value, int flag) {
 		this(name, desc, type, value, null, null, flag);
 	}
 
 	/**
 	 * Constructor to create a Tunable with bounds
 	 * information as well as a flag.
 	 *
 	 * @param name The name of the Tunable
 	 * @param desc The description of the Tunable
 	 * @param type Integer value that represents the type of
 	 *             the Tunable.  The type not only impact the
 	 *             way that the value is interpreted, but also
 	 *             the component used for the LayoutSettingsDialog
 	 * @param value The initial (default) value of the Tunable.  This
 	 *             is a String in the case of an EDGEATTRIBUTE or
 	 *             NODEATTRIBUTE tunable, it is an Integer index
 	 *             a LIST tunable.
 	 * @param lowerBound An Object that either represents the lower
 	 *             bounds of a numeric Tunable or an array of values
 	 *             for an attribute (or other type of) list.
 	 * @param upperBound An Object that represents the upper bounds
 	 *             of a numeric Tunable.
 	 * @param flag The initial value of the flag.  This can be
 	 *             used to indicate that this tunable is not user
 	 *             changeable (e.g. debug), or to indicate if there
 	 *             is a specific type for the attributes.
 	 */
 	public Tunable(String name, String desc, int type, Object value, Object lowerBound,
 	               Object upperBound, int flag) {
 		this.name = name;
 		this.desc = desc;
 		this.type = type;
 		this.value = value;
 		this.upperBound = upperBound;
 		this.lowerBound = lowerBound;
 		this.flag = flag;
 	}
 	
 	/**
 	 * Constructor to create a Tunable with bounds
 	 * information as well as a flag.
 	 *
 	 * @param name The name of the Tunable
 	 * @param desc The description of the Tunable
 	 * @param type Integer value that represents the type of
 	 *             the Tunable.  The type not only impact the
 	 *             way that the value is interpreted, but also
 	 *             the component used for the LayoutSettingsDialog
 	 * @param value The initial (default) value of the Tunable.  This
 	 *             is a String in the case of an EDGEATTRIBUTE or
 	 *             NODEATTRIBUTE tunable, it is an Integer index
 	 *             a LIST tunable.
 	 * @param lowerBound An Object that either represents the lower
 	 *             bounds of a numeric Tunable or an array of values
 	 *             for an attribute (or other type of) list.
 	 * @param upperBound An Object that represents the upper bounds
 	 *             of a numeric Tunable.
 	 * @param flag The initial value of the flag.  This can be
 	 *             used to indicate that this tunable is not user
 	 *             changeable (e.g. debug), or to indicate if there
 	 *             is a specific type for the attributes.
 	 * @param immutable If 'true', this Tunable is immutable
 	 *
 	 * @deprecated Use the <b>IMMUTABLE</b> flag directly rather than this special constructor. 
 	 */
 	public Tunable(String name, String desc, int type, Object value, Object lowerBound,
             Object upperBound, int flag, boolean immutable) {
 		this(name, desc, type, value, lowerBound, upperBound, flag);
 		if (immutable)
 			setFlag(IMMUTABLE);
 		// CyLogger.getLogger().info("Tunable "+desc+" has value "+value);
 	}
 
 	/**
 	 * This method can be used to set a flag for this Tunable
 	 *
 	 * @param flag integer value the contains the flag to set.
 	 */
 	public void setFlag(int flag) {
 		this.flag |= flag;
 	}
 
 	/**
 	 * This method can be used to clear a flag for this Tunable
 	 *
 	 * @param flag integer value the contains the flag to be cleared.
 	 */
 	public void clearFlag(int flag) {
 		this.flag &= ~flag;
 	}
 
 	/**
 	 * This method is used to check the value of a flag.  It
 	 * returns <b>true</b> if the flag is set, <b>false</b>
 	 * otherwise.
 	 *
 	 * @param flag integer value the contains the flag to be checked.
 	 * @return true if the flag is set.
 	 */
 	public boolean checkFlag(int flag) {
 		return ( (this.flag & flag) != 0 );
 	}
 
 	/**
  	 * This method can be used to set the "immutable" boolean,
  	 * which essentially get's mapped to the appropriate mechanism
  	 * for allowing a value to be editted.
  	 *
  	 * @param immutable 'true' if this is an immutable value
  	 */
 	public void setImmutable(boolean immutable) {
 		if (immutable)
 			setFlag(IMMUTABLE);
 		else
 			clearFlag(IMMUTABLE);
 		if (inputField != null)
			inputField.setEnabled(!checkFlag(IMMUTABLE));
 	}
 
 	/**
 	 * This method is used to set the value for this Tunable.  If
 	 * this is an INTEGER, DOUBLE, or BOOLEAN Tunable, then value
 	 * is assumed to be a String.  This also sets the "changed" state
 	 * of the value to "true".
 	 *
 	 * @param value Object (usually String) containing the value to be set
 	 */
 	public void setValue(Object value) {
 
 		switch (type) {
 			case INTEGER:
 				// CyLogger.getLogger().info("Setting Integer tunable "+desc+" value to "+value);
 				if (value.getClass() == String.class)
 					this.value = new Integer((String) value);
 				else
 					this.value = value;
 
 				if ((slider != null) && checkFlag(USESLIDER))
 					slider.setValue(sliderScale(this.value));
 				else if (inputField != null) {
 					((JTextField)inputField).setText(this.value.toString());
 				}
 				break;
 
 			case DOUBLE:
 					// CyLogger.getLogger().info("Setting Double tunable "+desc+" value to "+value);
 				if (value.getClass() == String.class)
 					this.value = new Double((String) value);
 				else
 					this.value = value;
 				if ((slider != null) && checkFlag(USESLIDER))
 					slider.setValue(sliderScale(value));
 				else if (inputField != null) {
 					((JTextField)inputField).setText(this.value.toString());
 				}
 				break;
 
 			case BOOLEAN:
 				// CyLogger.getLogger().info("Setting Boolean tunable "+desc+" value to "+value);
 				if (value.getClass() == String.class)
 					this.value = new Boolean((String) value);
 				else
 					this.value = value;
 				if (inputField != null)
 					((JCheckBox)inputField).setSelected(((Boolean)this.value).booleanValue());
 				break;
 
 			case LIST:
 				// CyLogger.getLogger().info("Setting List tunable "+desc+" value to "+value);
 				if (checkFlag(MULTISELECT)) {
 					// Multiselect LIST -- value is a List of Integers, or String values
 					this.value = value;
 					if (inputField != null && value != null) {
 						int[] intArray = decodeIntegerArray((String)value);
 						if (intArray != null)
 							((JList)inputField).setSelectedIndices(intArray);
 					}
 				} else {
 					if (value.getClass() == String.class)
 						this.value = new Integer((String) value);
 					else
 						this.value = value;
 
 					if (inputField != null)
 						((JComboBox)inputField).setSelectedIndex(((Integer)this.value).intValue());
 				}
 				break;
 
 			case NODEATTRIBUTE:
 			case EDGEATTRIBUTE:
 				// CyLogger.getLogger().info("Setting List tunable "+desc+" value to "+value);
 				if (checkFlag(MULTISELECT)) {
 					// Multiselect LIST -- value is a List of Integers, or String values
 					this.value = value;
 					if (inputField != null) {
 						((JList)inputField).setSelectedIndices(getSelectedValues(attributeList, decodeArray((String)value)));
 					}
 				} else {
 					this.value = value;
 
 					if (inputField != null)
 						((JComboBox)inputField).setSelectedItem(this.value);
 				}
 				break;
 
 			case GROUP:
 				// CyLogger.getLogger().info("Setting Group tunable "+desc+" value to "+value);
 				if (value.getClass() == String.class)
 					this.value = new Integer((String) value);
 				else
 					this.value = value;
 				return;
 
 			case STRING:
 				// CyLogger.getLogger().info("Setting String tunable "+desc+" value to "+value);
 				this.value = value;
 				if (inputField != null)
 					((JTextField)inputField).setText((String)value);
 				break;
 
 			case BUTTON:
 				// CyLogger.getLogger().info("Setting String tunable "+desc+" value to "+value);
 				this.value = value;
 				if (inputField != null)
 					((JButton)inputField).setText((String)value);
 				break;
 		}
 
 		if (inputField != null)
 			inputField.validate();
 
 		valueChanged = true;
 	}
 
 	/**
 	 * This method returns the current value.  This method
 	 * also resets the state of the value to indicate that
 	 * it has not been changed since the last "get".
 	 *
 	 * @return Object that contains the value for this Tunable
 	 */
 	public Object getValue() {
 		valueChanged = false;
 
 		return value;
 	}
 
 	/**
 	 * Returns the changed state of the value.  If true,
 	 * the value has been changed since it was last retrieved.
 	 *
 	 * @return boolean value of the changed state.
 	 */
 	public boolean valueChanged() {
 		return valueChanged;
 	}
 
 	/**
 	 * Method to set the lowerBound for this Tunable.  This might be used to change a Tunable
 	 * based on changes in the plugin environment.
 	 *
 	 * @param lowerBound the new lowerBound for the tunable
 	 */
 	public void setLowerBound(Object lowerBound) {
 		this.lowerBound = lowerBound;
 		if (inputField == null)
 			return;
 
 		// If we're a slider or a list, this might require us to reset things...
 		if (type == LIST) {
 			Object[] listData = (Object[])lowerBound;
 			if (checkFlag(MULTISELECT)) {
 				JList list = (JList)inputField;
 				list.setListData(listData);
 			} else {
 				JComboBox cbox = (JComboBox)inputField;
 				cbox.removeAllItems();
 				for (int i = 0; i < listData.length; i++)
 					cbox.addItem(listData[i]);
 			}
 		} else if (type == NODEATTRIBUTE) {
 		} else if (type == EDGEATTRIBUTE) {
 		} else if (checkFlag(USESLIDER)) {
 			slider.setMinimum(sliderScale(lowerBound));
 			slider.setLabelTable(createLabels(slider));
 		} else if (type == GROUP) {
 			// This indicates whether this GROUP is collapsed
 			// or not
 			if (!checkFlag(COLLAPSABLE))
 				return;
 			// Update presentation
 			updateValueListeners();
 		}
 	}
 
 	/**
 	 * Method to get the lowerBound for this Tunable.
 	 *
 	 * @return the lowerBound the tunable
 	 */
 	public Object getLowerBound() {
 		return this.lowerBound;
 	}
 
 	/**
 	 * Method to set the upperBound for this Tunable.  This might be used to change a Tunable
 	 * based on changes in the plugin environment.
 	 *
 	 * @param upperBound the new upperBound for the tunable
 	 */
 	public void setUpperBound(Object upperBound) {
 		this.upperBound = upperBound;
 		if (inputField == null)
 			return;
 
 		// If we're a slider, this might require us to reset things
 		if (checkFlag(USESLIDER)) {
 			slider.setMaximum(sliderScale(upperBound));
 			slider.setLabelTable(createLabels(slider));
 		}
 	}
 
 	/**
 	 * Method to get the upperBound for this Tunable.
 	 *
 	 * @return the upperBound the tunable
 	 */
 	public Object getUpperBound() {
 		return this.upperBound;
 	}
 
 	/**
 	 * Method to return a string representation of this Tunable,
 	 * which is essentially its name.
 	 *
 	 * @return String value of the name of the Tunable.
 	 */
 	public String toString() {
 		return name;
 	}
 
 	/**
 	 * Method to return a string representation of this Tunable,
 	 * which is essentially its name.
 	 *
 	 * @return String value of the name of the Tunable.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Method to return the type of this Tunable.
 	 *
 	 * @return Tunable type
 	 */
 	public int getType() {
 		return type;
 	}
 
 	/**
 	 * Method to return the description for this Tunable.
 	 *
 	 * @return Tunable description
 	 */
 	public String getDescription() {
 		return desc;
 	}
 
 
 	/**
  	 * Method to add a value listener to this Tunable.  A value
  	 * listener is called whenever a tunable value is updated
  	 * by the user (as opposed to programmatically).  This can
  	 * be used to change the UI based on certain inputs.
  	 *
  	 * @param listener the TunableListener to add
  	 */
 	public void addTunableValueListener(TunableListener listener) {
 		if (listenerList == null)
 			listenerList = new ArrayList();
 
 		if (listener == null || listenerList.contains(listener))
 			return;
 
 		listenerList.add(listener);
 	}
 
 	/**
  	 * Method to remove a value listener from this Tunable.  A value
  	 * listener is called whenever a tunable value is updated
  	 * by the user (as opposed to programmatically).  This can
  	 * be used to change the UI based on certain inputs.
  	 *
  	 * @param listener the TunableListener to remove
  	 */
 	public void removeTunableValueListener(TunableListener listener) {
 		if (listener == null || listenerList == null)
 			return;
 
 		listenerList.remove(listener);
 	}
 
 	/**
  	 * Method to call all of the value listeners.
  	 */
 	public void updateValueListeners() {
 		if (listenerList == null)
 			return;
 
 		updateValue();
 
 		for (TunableListener listener: listenerList)
 			listener.tunableChanged(this);
 	}
 
 	/**
 	 * This method returns a JPanel suitable for inclusion in the
 	 * LayoutSettingsDialog to represent this Tunable.  Note that
 	 * while the type of the widgets used to represent the Tunable
 	 * are customized to represent the type, no ActionListeners are
 	 * included.  The dialog must call updateSettings to set the
 	 * value of the Tunable from the user input data.
 	 *
 	 * @return JPanel that can be used to enter values for this Tunable
 	 */
 	public JPanel getPanel() {
 		if (checkFlag(NOINPUT))
 			return null;
 
 		if (type == GROUP) {
 			JPanel tunablesPanel = new JPanel();
 			BoxLayout box = new BoxLayout(tunablesPanel, BoxLayout.Y_AXIS);
 			tunablesPanel.setLayout(box);
 
 			// Special case for groups
 			Border refBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
 			TitledBorder titleBorder = BorderFactory.createTitledBorder(refBorder, this.desc);
 			titleBorder.setTitlePosition(TitledBorder.LEFT);
 			titleBorder.setTitlePosition(TitledBorder.TOP);
 			tunablesPanel.setBorder(titleBorder);
 			if (checkFlag(COLLAPSABLE)) {
 				String label = "<html><b><i>&nbsp;&nbsp;Hide "+desc+"</i></b></html>";
 				// Update the internal collapsed flag
 				this.collapsed = ((Boolean)lowerBound).booleanValue();
 				if (collapsed)
 					label = "<html><b><i>&nbsp;&nbsp;Show "+desc+"</i></b></html>";
 				// Add collapse/expand button
 				JPanel collapsePanel = new JPanel(new BorderLayout(0, 2));
 				JButton collapseButton = null;
 				if (collapsed)
 					collapseButton = new JButton(UIManager.getIcon("Tree.collapsedIcon"));
 				else
 					collapseButton = new JButton(UIManager.getIcon("Tree.expandedIcon"));
 				collapseButton.setActionCommand("collapse");
 				collapseButton.addActionListener(this);
 				collapsePanel.add(collapseButton, BorderLayout.LINE_START);
 				collapsePanel.add(new JLabel(label), BorderLayout.CENTER);
 				tunablesPanel.add(collapsePanel);
 			}
 
 			return tunablesPanel;
 		}
 
 		JPanel tunablePanel = new JPanel(new BorderLayout(0, 2));
 		JLabel tunableLabel = new JLabel(desc);
 		String labelLocation = BorderLayout.LINE_START;
 		String fieldLocation = BorderLayout.LINE_END;
 
 		if ((type == DOUBLE) || (type == INTEGER)) {
 			if ( (checkFlag(USESLIDER)) && (lowerBound != null) && (upperBound != null)) {
 				// We're going to use a slider,  We need to be somewhat intelligent about the bounds and
 				// labels.  It would also be nice to provide feedback, which we do by providing a text field
 				// in addition to the slider.  The text field can also be used to enter the desired value
 				// directly.
 
 				slider = new JSlider(JSlider.HORIZONTAL, 
 				                            sliderScale(lowerBound), 
 				                            sliderScale(upperBound), 
 				                            sliderScale(value));
 
 				slider.setLabelTable(createLabels(slider));
 				slider.setPaintLabels(true);
 				slider.addChangeListener(this);
 				tunablePanel.add(tunableLabel, BorderLayout.NORTH);
 				tunablePanel.add(slider, BorderLayout.CENTER);
 
 				JTextField textField = new JTextField(value.toString(), 4);
 				textField.addFocusListener(this);
 				inputField = textField;
 				tunablePanel.add(textField, BorderLayout.EAST);
 				textField.setBackground(Color.white);
 				return tunablePanel;
 
 			} else {
 				// We can't use a slider, so turn off the flag
 				clearFlag(USESLIDER);
 				JTextField field = new JTextField(value.toString(), 8);
 				field.setHorizontalAlignment(JTextField.RIGHT);
 				// If we have an upper and/or lower bounds, we want to "listen" for changes
 				field.addFocusListener(this);
 				inputField = field;
 			}
 		} else if (type == BOOLEAN) {
 			JCheckBox box = new JCheckBox();
 			box.setSelected(((Boolean) value).booleanValue());
 			box.addItemListener(this);
 			inputField = box;
 		} else if (type == NODEATTRIBUTE) {
 			CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
 			inputField = getAttributePanel(nodeAttributes);
 		} else if (type == EDGEATTRIBUTE) {
 			CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
 			inputField = getAttributePanel(edgeAttributes);
 		} else if (type == LIST) {
 			inputField = getListPanel((Object[]) lowerBound);
 		} else if (type == STRING) {
 			JTextField field = new JTextField(value.toString(), 20);
 			field.addFocusListener(this);
 			field.setHorizontalAlignment(JTextField.RIGHT);
 			inputField = field;
 		} else if (type == BUTTON) {
 			JButton button = new JButton((String)value);
 			button.addActionListener((ActionListener)lowerBound);
 			button.setActionCommand(name);
 			inputField = button;
 		}
 
 		// Added by kono
 		// This allow immutable value.
 		if(checkFlag(IMMUTABLE)) {
 			inputField.setEnabled(false);
 		}
 		inputField.setBackground(Color.white);
 
 		tunablePanel.add(tunableLabel, labelLocation);
 
 		// Special case for MULTISELECT lists
 		if ((type == LIST || type == NODEATTRIBUTE || type == EDGEATTRIBUTE) 
 			  && checkFlag(MULTISELECT)) {
 			JScrollPane listScroller = new JScrollPane(inputField);
 			listScroller.setPreferredSize(new Dimension(200,100));
 			tunablePanel.add(listScroller, fieldLocation);
 		} else {
 			tunablePanel.add(inputField, fieldLocation);
 		}
 		return tunablePanel;
 	}
 
 	/**
 	 * This method is used by getPanel to construct a JComboBox that
 	 * contains a list of node or edge attributes that the user can
 	 * choose from for doing attribute-dependent layouts.
 	 *
 	 * @param attributes CyAttributes of the appropriate (edge or node) type
 	 * @return a JComponent with an entry for each attribute
 	 */
 	private JComponent getAttributePanel(CyAttributes attributes) {
 		final String[] attList = attributes.getAttributeNames();
 		final List<String> list = new ArrayList<String>();
 
 		// See if we have any initial attributes (mapped into lowerBound)
 		if (lowerBound != null) {
 			list.addAll((List) lowerBound);
 		}
 
 		for (int i = 0; i < attList.length; i++) {
 			// Is this attribute user visible?
 			if (!attributes.getUserVisible(attList[i]))
 				continue;
 
 			byte type = attributes.getType(attList[i]);
 
 			if (((flag & NUMERICATTRIBUTE) == 0)
 			    || ((type == CyAttributes.TYPE_FLOATING) 
 			    || (type == CyAttributes.TYPE_INTEGER))) {
 				list.add(attList[i]);
 			}
 		}
 
 		attributeList = list;
 
 		if (checkFlag(MULTISELECT)) {
 			// Set our current value as selected
 			JList jList = new JList(list.toArray());
 			int [] indices = getSelectedValues(attributeList, decodeArray((String)value));
 			if (indices != null && indices.length > 0)
 				jList.setSelectedIndices(indices);
 			jList.addListSelectionListener(this);
 			return jList;
 		} else {
 			// Set our current value as selected
 			JComboBox box = new JComboBox(attributeList.toArray());
 			box.setSelectedItem((String) value);
 			box.addActionListener(this);
 			return box;
 		}
 	}
 
 	/**
 	 * This method is used by getPanel to construct a JComboBox that
 	 * contains a list of values the user can choose from.
 	 *
 	 * @param list Array of Objects containing the list
 	 * @return a JComboBox with an entry for each item on the list
 	 */
 	private JComponent getListPanel(Object[] list) {
 		if (checkFlag(MULTISELECT)) {
 			JList jList =  new JList(list);
 			if (value != null && ((String)value).length() > 0) {
 				int[] intArray = decodeIntegerArray((String)value);
 				if (intArray != null)
 					jList.setSelectedIndices(intArray);
 			}
 			jList.addListSelectionListener(this);
 			return jList;
 		} else {
 			// Set our current value as selected
 			JComboBox box = new JComboBox(list);
 			box.setSelectedIndex(((Integer) value).intValue());
 			box.addActionListener(this);
 			return box;
 		}
 	}
 
 	/**
  	 * Return an array of indices suitable for selection. The passed
  	 * String value is an encoded list of entries of the form [attr1,attr2,...]
  	 *
  	 * @param attrs the list of attributes to choose from
  	 * @param values the list of values
  	 * @return array of integers to use to select values
  	 */
 	private int[] getSelectedValues(List<String>attrs, String[] values) {
 		if (values == null) return null;
 		int[] selVals = new int[values.length];
 		for (int i = 0; i < values.length;  i++) {
 			selVals[i] = attrs.indexOf(values[i]);
 		}
 		return selVals;
 	}
 
 	private int[] decodeIntegerArray(String value) {
 		if(value == null || value.length() == 0) {
 			return null;
 		}
 		String[] valArray = value.split(",");
 		int[] intArray = new int[valArray.length];
 		for (int i = 0; i < valArray.length; i++) {
 			intArray[i] = Integer.valueOf(valArray[i]).intValue();
 		}
 		return intArray;
 	}
 
 	private String[] decodeArray(String value) {
 		if(value == null || value.length() == 0) {
 			return null;
 		}
 		return value.split(",");
 	}
 
 	/**
 	 * This method is called to extract the user-entered data from the
 	 * JPanel and store it as our value.
 	 */
 	public void updateValue() {
 		Object newValue;
 
 		if (inputField == null || type == GROUP || type == BUTTON)
 			return;
 
 		if (type == DOUBLE) {
 			if (usingSlider) {
 				newValue = new Double(((JSlider) inputField).getValue());
 			} else {
 				newValue = new Double(((JTextField) inputField).getText());
 			}
 		} else if (type == INTEGER) {
 			if (usingSlider) {
 				newValue = new Integer(((JSlider) inputField).getValue());
 			} else {
 				newValue = new Integer(((JTextField) inputField).getText());
 			}
 		} else if (type == BOOLEAN) {
 			newValue = new Boolean(((JCheckBox) inputField).isSelected());
 		} else if (type == LIST) {
 			if (checkFlag(MULTISELECT)) {
 				int [] selVals = ((JList) inputField).getSelectedIndices();
 				String newString = "";
 				for (int i = 0; i < selVals.length; i++) {
 					newString += Integer.toString(selVals[i]);
 					if (i < selVals.length-1) newString+= ",";
 				}
 				newValue = (Object) newString;
 			} else {
 				newValue = new Integer(((JComboBox) inputField).getSelectedIndex());
 			}
 		} else if ((type == NODEATTRIBUTE) || (type == EDGEATTRIBUTE)) {
 			if (checkFlag(MULTISELECT)) {
 				Object [] selVals = ((JList) inputField).getSelectedValues();
 				String newString = "";
 				for (int i = 0; i < selVals.length; i++) {
 					newString += selVals[i];
 					if (i < selVals.length) newString+= ",";
 				}
 				newValue = (Object) newString;
 			} else {
 				newValue = (String) ((JComboBox) inputField).getSelectedItem();
 			}
 		} else {
 			newValue = ((JTextField) inputField).getText();
 		}
 
 		if (value == null || !value.equals(newValue)) {
 			valueChanged = true;
 		}
 
 		value = newValue;
 	}
 
 	/**
  	 * Document listener routines to handle bounds checking
  	 */
 	public void focusLost(FocusEvent ev) {
 		Object value = null;
 		// Check the bounds
 		if (type == DOUBLE) {
 			Double newValue = null;
 			try {
 				newValue = new Double(((JTextField) inputField).getText());
 			} catch (NumberFormatException e) {
 				displayBoundsError("a floating point");
 				return;
 			}
 			if ((upperBound != null && newValue > (Double)upperBound) ||
 			   (lowerBound != null && newValue < (Double)lowerBound)) {
 				displayBoundsError("a floating point");
 				return;
 			}
 			value = (Object)newValue;
 		} else if (type == INTEGER) {
 			Integer newValue = null;
 			try {
 				newValue = new Integer(((JTextField) inputField).getText());
 			} catch (NumberFormatException e) {
 				displayBoundsError("an integer");
 				return;
 			}
 			if ((upperBound != null && newValue > (Integer)upperBound) ||
 			   (lowerBound != null && newValue < (Integer)lowerBound)) {
 				displayBoundsError("an integer");
 				return;
 			}
 			value = (Object)newValue;
 		}
 
 		if (checkFlag(USESLIDER)) {
 			// Update the slider with this new value
 			slider.setValue(sliderScale(value));
 		} else {
 			updateValueListeners();
 		}
 	}
 
 	/**
 	 * This method is public as a byproduct of the implementation.
 	 */
 	public void focusGained(FocusEvent ev) {
 		// Save the current value
 		savedValue = ((JTextField) inputField).getText();
 	}
 
 	/**
 	 * This method is public as a byproduct of the implementation.
 	 */
 	public void stateChanged(ChangeEvent e) {
 		if (((type == DOUBLE) || (type == INTEGER)) && (checkFlag(USESLIDER))) {
 			// Get the widget
 			JSlider slider = (JSlider) e.getSource();
 
 			// Get the value
 			int value = slider.getValue();
 
 			// Update the text box
 			((JTextField) inputField).setText(sliderScale(value).toString());
 		} else {
 			updateValueListeners();
 		}
 	}
 
 	/**
 	 * This method is public as a byproduct of the implementation.
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if (type == GROUP && checkFlag(COLLAPSABLE)) {
 			if (collapsed) 
 				collapsed = false;
 			else
 				collapsed = true;
 			lowerBound = Boolean.valueOf(collapsed);
 		}
 		updateValueListeners();
 	}
 
 	/**
 	 * This method is public as a byproduct of the implementation.
 	 */
 	public void itemStateChanged(ItemEvent e) {
 		updateValueListeners();
 	}
 
 	/**
 	 * This method is public as a byproduct of the implementation.
 	 */
 	public void valueChanged(ListSelectionEvent e) {
 		updateValueListeners();
 	}
 
 	private void displayBoundsError(String typeString) {
 		if (lowerBound != null && upperBound != null) {
 			JOptionPane.showMessageDialog(null,  "Value must be "+typeString+" between "+lowerBound+" and "+upperBound,
 				"Bounds Error", JOptionPane.ERROR_MESSAGE);
 		} else if (lowerBound != null) {
 			JOptionPane.showMessageDialog(null, "Value must be "+typeString+" greater than "+lowerBound,
 				"Bounds Error", JOptionPane.ERROR_MESSAGE);
 		} else if (upperBound != null) {
 			JOptionPane.showMessageDialog(null, "Value must be "+typeString+" less than "+upperBound,
 				"Bounds Error", JOptionPane.ERROR_MESSAGE);
 		} else {
 			JOptionPane.showMessageDialog(null, "Value must be "+typeString+" number",
 				"Type Error", JOptionPane.ERROR_MESSAGE);
 		}
 		((JTextField) inputField).setText(savedValue);
 	}
 
 	private int getIntValue(Object v) {
 		if (type == DOUBLE) {
 			Double d = (Double)v;
 			return d.intValue();
 		} else if (type == INTEGER) {
 			Integer d = (Integer)v;
 			return d.intValue();
 		}
 		return 0;
 	}
 
 	private int sliderScale(Object value) {
 		if (type == INTEGER) {
 			// Don't mess with Integer values
 			return ((Integer)value).intValue();
 		}
 		double minimum = ((Double)lowerBound).doubleValue();
 		double maximum = ((Double)upperBound).doubleValue();
 		double input = ((Double)value).doubleValue();
 		double extent = maximum-minimum;
 
 		// Use a scale from 0-100 with 0 = minimum and 100 = maximum
 		return (int)(((input-minimum)/extent)*100.0);
 	}
 
 	private Object sliderScale(int value) {
 		if (type == INTEGER) {
 			// Don't mess with Integer values
 			return Integer.valueOf(value);
 		}
 		double minimum = ((Double)lowerBound).doubleValue();
 		double maximum = ((Double)upperBound).doubleValue();
 		double extent = maximum-minimum;
 		double dvalue = (double)value/100.0;
 		double scaledValue = (dvalue*extent)+minimum;
 		int places = 2;
 		if (extent < 1.0)
 			places = (int)Math.round(-Math.log10(extent)) + 1;
 		return new Double(round(scaledValue, places));
 
 	}
 
 	private Hashtable createLabels(JSlider slider) {
 		if (type == INTEGER) {
 			int increment = (getIntValue(upperBound)-getIntValue(lowerBound))/5;
 			if (increment > 1)
 				return slider.createStandardLabels(increment);
 			else
 				return slider.createStandardLabels(1);
 		}
 		Hashtable<Integer,JComponent>table = new Hashtable();
 		// Create our table in 5 steps from lowerBound to upperBound
 		// This could obviously be much fancier, but it's probably sufficient for now.
 		for (int label = 0; label < 6; label++) {
 			Double v = (Double)sliderScale(label*20);
 			table.put(label*20, new JLabel(v.toString()));
 		}
 		return table;
 	}
 
 	private double round(double val, int places) {
 		long factor = (long)Math.pow(10, places);
 		val = val * factor;
 
 		long tmp = Math.round(val);
 
 		return (double)tmp / factor;
 	}
 }
