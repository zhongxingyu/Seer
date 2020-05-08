 //------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //------------------------------------------------------------------------------
 package cytoscape.visual.calculators;
 //------------------------------------------------------------------------------
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Vector;
 import java.util.Properties;
 import cytoscape.GraphObjAttributes;
 import cytoscape.dialogs.GridBagGroup;
 import cytoscape.dialogs.MiscGB;
 import cytoscape.visual.mappings.ObjectMapping;
 import cytoscape.visual.mappings.MappingFactory;
 import cytoscape.visual.Network;
 //------------------------------------------------------------------------------
 /**
  * AbstractCalculator is the top of the tree for the Calculator classes. <b>DO NOT</b>
  * extend this class directly! All calculators should extend one of {@link NodeCalculator}
  * or {@link EdgeCalculator} <b>AND</b> implement one of the 11 attribute calculator
  * interfaces.
  */
 public abstract class AbstractCalculator implements Calculator {
     /**
      * Vector of all mappings contained by this calculator. Usually small. Contains
      * ObjectMapping objects.
      */
     protected Vector mappings = new Vector(4, 2);
     // private ObjectMapping m;
 
     /**
      * The domain classes accepted by the mappings underlying this calculator.
      * Contains an array of classes - each representing the appropriate domain
      * classes of the mapping in the {@link #mappings} vector at the same index.
      */
     protected Vector acceptedDataClasses = new Vector(4, 2);
     //protected Class[] acceptedDataClasses;
     protected String name;
     
     /** keep track of how many times I've been duplicated */
     private int dupeCount = 0; 
 
     /**
      * Create a calculator with the specified object mapping and name. The
      * object mapping is used to determine what classes of attribute data
      * this calculator can map from. If the object mapping is null, no
      * filtration on the attribute data is performed.
      *
      * @param	m	Object mapping for this calculator, or null
      * @param	name	Name of this calculator
      */
     public AbstractCalculator(String name, ObjectMapping m) {
 	this.name = name;
 	this.addMapping(m);
     }
 
     /**
      * Add a mapping to the mappings contained by the calculator.
      * @param m Mapping to add.
      */
     public void addMapping(ObjectMapping m) {
 	this.mappings.add(m);
 	this.acceptedDataClasses.add(m.getAcceptedDataClasses());
     }
 
     /**
      * Get the first mapping in the vector of mappings. Provided for compatibility
      * with previous releases. 
      *
      * @deprecated Use {@link #getMappings()} or {@link #getMapping(int)} instead.
      * @return First mapping in vector of mappings.
      */
     public ObjectMapping getMapping() {
 	return (ObjectMapping) mappings.get(0);
     }
     
     /**
      * Get all mappings contained by this calculator.
      * @return Vector of all mappings contained in this calculator
      */
     public Vector getMappings() {
 	return mappings;
     }
 
     /**
      * Get the mapping at a specific index in this calculator.
      * @param i index of mapping to retrieve
      * @return ObjectMapping at index i
      */
     public ObjectMapping getMapping(int i) {
 	return (ObjectMapping) this.mappings.get(i);
     }
 
     /**
      * Get how many times this calculator has been duplicated.
      * @return Calculator duplication count
      */
     public int getDupeCount() {
 	return dupeCount;
     }
 
     /**
      * Clone the calculator. AbstractCalculator makes an independent clone of itself but DOES NOT
      * ensure that a unique name is created. Whoever is cloning the calculator should enter the
      * new calculator in the catalog and create an appropriate name for it if needed.
      *
      *
      * @return Clone of this calculator
      * @throws CloneNotSupportedException if something is seriously borked.
      */
     public Object clone() throws CloneNotSupportedException {
 	AbstractCalculator clonedCalc = null;
 	try {
 	    clonedCalc = (AbstractCalculator) super.clone();
 	}
 	catch (CloneNotSupportedException e) {
 	    System.err.println("Error cloning AbstractCalculator and associated mapping");
 	    throw e;
 	}
 	// remove the duplication count appended to the name. This makes
 	// maintaining the duplicate naming scheme much easier by starting the
 	// valid name search at the first object every time
 	String dupeFreeName;
 	if (dupeCount != 0) {
 	    int dupeCountIndex = name.lastIndexOf(new Integer(dupeCount).toString());
 	    dupeFreeName = name.substring(0, dupeCountIndex);
 	}
 	else {
 	    dupeFreeName = new String(name);
 	}
 	clonedCalc.name = dupeFreeName;
 	clonedCalc.mappings = new Vector(this.mappings.size(),2);
 	// Not needed since data classes don't change and are not configurable
 	// clonedCalc.acceptedDataClasses = this.acceptedDataClasses.clone();
 	// clone mapping data
 	// clonedCalc.m = (ObjectMapping) m.clone();
 	for (int i = 0; i < this.mappings.size(); i++) {
 	    ObjectMapping m = this.getMapping(i);
 	    // Not needed since data classes don't change and are not configurable
 	    //Class[] mClass = (Class[]) this.acceptedDataClasses.get(i);
 	    //clonedCalc.acceptedDataClasses.add(mClass.clone());
 	    clonedCalc.mappings.add(m.clone());
 	}
 
 	clonedCalc.dupeCount++;
 	return clonedCalc;
     }
 
     /**
      * Get the name of this calculator.
      *
      * @return the calculator's name
      */
     public final String toString() {
 	return this.name;
     }
 
     /**
      * Set the name of this calculator. Should only be done by the CalculatorCatalog after checking
      * that name will not be duplicated.
      *
      * @param	newName	the new name for this calculator. Must be unique.
      */
     public void setName(String newName) {
 	this.name = newName;
     }
 
     /**
      * Returns a properties description of this calculator. Adds the
      * keyword ".mapping" to the supplied base key and calls the
      * getProperties method of MappingFactory with the ObjectMapping
      * and the augmented base key.
      */ 
     public Properties getProperties(String baseKey) {
         String mapBaseKey = baseKey + ".mapping";
         ObjectMapping m = getMapping(0);
         return MappingFactory.getProperties(m, mapBaseKey);
     }
 
     /**
      * updateAttribute is called when the currently selected attribute changes.
      *
      * @param	attrName	the name of the newly selected attribute
      * @param   network         the Network on which this attribute is defined
      * @deprecated Only supports one mapping, use
     *		{@link #updateAttribute(String, Network, ObjectMapping)} or
     *		{@link #updateAttribute(String, Newtork, int)) instead.
      */
     void updateAttribute(String attrName, Network network) {
 	this.getMapping().setControllingAttributeName(attrName, network, false);
     }
 
     /**
      * updateAttribute is called when the currently selected attribute changes.
      * Any changes needed in the mapping UI should be performed at this point.
     * Use {@link #updateAttribute(String, Network, int)} for best performance.
      *
      * @param	attrName	the name of the newly selected attribute
      * @param   network         the Network on which this attribute is defined
      * @param	m		the object mapping to update
      * @throws	IllegalArgumentException if the given object mapping isn't in this
      *		    calculator.
      */
     void updateAttribute(String attrName, Network network, ObjectMapping m)
     throws IllegalArgumentException {
 	int mapIndex = this.mappings.indexOf(m);
 	if (mapIndex == -1) {
 	    throw new IllegalArgumentException(m.getClass().getName() + " " + m.toString() +
 					       " is not contained in calculator " +
 					       this.toString());
 	}
 	this.updateAttribute(attrName, network, mapIndex);
     }	    
 
     /**
      * updateAttribute is called when the currently selected attribute changes.
      * Any changes needed in the mapping UI should be performed at this point.
      * <p>
      * Calls the specified mapper's setControllingAttribute method.
      * @param	attrName	the name of the newly selected attribute
      * @param   network         the Network on which this attribute is defined
      * @param	mIndex		the index of the object mapping to update
      * @throws	ArrayIndexOutOfBoundsException if the given object mapping index
      *		    is out of bounds.
      */
     void updateAttribute(String attrName, Network network, int mIndex)
     throws ArrayIndexOutOfBoundsException {
 	ObjectMapping m = (ObjectMapping) this.mappings.get(mIndex);
 	m.setControllingAttributeName(attrName, network, false);
 	// refresh the mapping UI
     }
 
     /**
      * Get the UI for the calculator.
      * 
      * @param	parent	Parent JDialog for the UI
      * @param	network	Network object containing underlying graph data
      */
     public abstract JPanel getUI(JDialog parent, Network network);
 
     /**
      * Get the UI for calculators. Display a JComboBox with attributes in the given
      * GraphObjAttributes whose data are instances of the classes accepted by each
      * ObjectMapping. The resulting JComboBox calls
     * {@link #updateAttribute(String, Network, int)} when frobbed.
      *
      * @param	attr	GraphObjAttributes to look up attributes from
      * @return	UI with controlling attribute selection facilities
      */
     protected JPanel getUI(GraphObjAttributes attr, JDialog parent, Network network) {
 	GridBagGroup g = new GridBagGroup();
 	String[] attrNames = attr.getAttributeNames();
 	int i, yPos;
 	for (i = yPos = 0; i < this.mappings.size(); i++, yPos++) {
 	    MiscGB.insert(g, new JLabel("Map Attribute:", SwingConstants.RIGHT),
 			  0, yPos);
 	    MiscGB.insert(g, Box.createHorizontalStrut(5), 1, yPos);
 	    ObjectMapping m = (ObjectMapping) this.mappings.get(i);
 
 	    // filter list of interactions
 	    String[] validAttr;
 	    Class [] okClass = (Class[]) this.acceptedDataClasses.get(i);
 	    if (okClass != null) {
 		Vector validAttrV = new Vector(attrNames.length);
 		for (int j = 0; j < attrNames.length; j++) {
 		    Class attrClass = attr.getClass(attrNames[j]);
 		    for (int k = 0; k < okClass.length; k++) {
 			if (okClass[k].isAssignableFrom(attrClass)) {
 			    validAttrV.add(attrNames[j]);
 			    break;
 			}
 		    }
 		}
 		validAttr = (String[]) validAttrV.toArray(new String[0]);
 	    }
 	    else {
 		validAttr = attrNames;
 	    }
 
 	    // create the JComboBox
 	    JComboBox attrBox = new JComboBox(validAttr);
 	    // set the attrBox to the currently selected attribute
 	    String selectedAttr = m.getControllingAttributeName();
 	    // make no selection first, in case the selectedAttr doesn't exist
 	    attrBox.setSelectedIndex(-1);
 	    attrBox.setSelectedItem(selectedAttr);
 	    
 	    attrBox.addItemListener(new AttributeSelectorListener(network, i));
 	    
 	    MiscGB.insert(g, attrBox, 2, yPos, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 
 	    // underlying mapping's UI
 	    JPanel mapperUI = m.getUI(parent, network);
 	    MiscGB.insert(g, mapperUI, 0, ++yPos, 3, 1, 2, 2, GridBagConstraints.BOTH);
 	}
 	return g.panel;
     }
 
     /**
      * Refresh the UI. Call when something in the underlying structures have
      * changed and the UI needs to be refreshed.
      */
     public void refreshUI(JDialog parent, Network network) {
 	getUI(parent, network);
     }
 
     /**
      * AttributeSelectorListener listens for events on the JComboBoxes that
      * select the controlling attribute for mappers contained in each calculator.
      */
     protected class AttributeSelectorListener implements ItemListener {
         private Network network;
 	private int mapIndex;
 	/**
 	 * Constructs an AttributeSelectorListener for the ObjectMapping at
 	 * index mapIndex.
          * @param network  passed to the mapping to get data values for the
          *                 new attribute
 	 * @param mapIndex Index of the mapping in the {@link #mappings} Vector
 	 *                 to report changes in the selected
 	 *                 attribute to.
 	 */
 	protected AttributeSelectorListener(Network network, int mapIndex) {
             this.network = network;
 	    this.mapIndex = mapIndex;
 	}
 	    
 	public void itemStateChanged(ItemEvent e) {
 	    if (e.getStateChange() == ItemEvent.SELECTED) {
 		JComboBox c = (JComboBox) e.getItemSelectable();
 		String attrName = (String) c.getSelectedItem();
 		updateAttribute(attrName, network, this.mapIndex);
 	    }
 	}
     }
 }
