 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual.mappings;
 //----------------------------------------------------------------------------
 import java.util.*;
 import javax.swing.*;
import java.awt.Color;
 import java.awt.event.*;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import cytoscape.dialogs.MiscGB;
 import cytoscape.dialogs.GridBagGroup;
 import cytoscape.visual.Network;
 import cytoscape.visual.ui.ValueDisplayer;
 import cytoscape.visual.parsers.ValueParser;
 import cytoscape.visual.parsers.ObjectToString;
 //----------------------------------------------------------------------------
 /**
  * Implements an interpolation table mapping data to values of a particular class.
  * The data value is extracted from a bundle of attributes by using a
  * specified data attribute name.
  */
 public class ContinuousMapping extends TreeMap implements ObjectMapping {
     
     Object defaultObj; //the default value held by this mapping
     Class rangeClass; //the class of values held by this mapping
     String attrName;  //the name of the controlling data attribute
     Interpolator fInt; //object used to interpolate between boundaries
     private ContinuousUI myUI; //contains the UI
     private byte mapType;
     private boolean UICreated = false;
 
     //when the UI is changed, needsUpdate becomes true.
     //updateMapper() is the ui call which can make needsUpdate false again.
     //calculateRangeValue() calls updateMapper if needsUpdate is true.
     private boolean needsUpdate = false;
 
     /** Standard constructor for compatibility with new calculator creation in UI.
      *	@param	defaultObj	default object to map to
      *	@param	mapType		Type of mapping, one of {@link ObjectMapping#EDGE_MAPPING}
      *				or {@link ObjectMapping#NODE_MAPPING}
      */
     public ContinuousMapping(Object defaultObj, byte mapType) throws IllegalArgumentException {
 	this(defaultObj,null,null,mapType);
     }
     public ContinuousMapping(Object defaultObj, String attrName, Interpolator i, byte mapType) {
         this.rangeClass = defaultObj.getClass();
 	this.defaultObj = defaultObj;
 	if (mapType != ObjectMapping.EDGE_MAPPING &&
 	    mapType != ObjectMapping.NODE_MAPPING) {
 	    throw new IllegalArgumentException("Unknown mapping type " + mapType);
 	}
	if (i == null) {
	    if (Color.class.isAssignableFrom(this.rangeClass)) {
		i = new LinearNumberToColorInterpolator();
	    }
	    else if (Number.class.isAssignableFrom(this.rangeClass)) {
		i = new LinearNumberToNumberInterpolator();
	    }
	    else {
		i = new FlatInterpolator();
	    }
	}
 	this.mapType = mapType;
         setControllingAttributeName(attrName, null, false);
         setInterpolator(i);
     }
     
     public Object clone() {
 	ContinuousMapping miniMe = (ContinuousMapping) super.clone();
 	// defaultObj doesn't need to be cloned since it is only
 	// assigned on construction by the creator of the mapping
 	// rangeClass doesn't need to be cloned since the cloned
 	// calculator has the same type as the original
 	miniMe.attrName = new String(attrName);
 	// recreate the UI on next call
 	miniMe.UICreated = false;
 	return miniMe;
     }
     
     public Class getRangeClass() {return rangeClass;}
     
     public Class[] getAcceptedDataClasses() {
 	// only numbers supported
 	Class[] ret = {Number.class};
 	return ret;
     }
 
     public String getControllingAttributeName() {return attrName;}
     public void setControllingAttributeName(String attrName, Network network,
                                             boolean preserveMapping) {
         this.attrName = attrName;
     }
     
     public Interpolator getInterpolator() {return fInt;}
     public void setInterpolator(Interpolator i) {fInt = i;}
     
     public JPanel getUI(JDialog parent, Network network) {
         //construct a UI to view/edit this mapping
 	if(!UICreated) {
 	    myUI = new ContinuousUI(parent, network);
 	}
 	return myUI;
     }
     
     public Object calculateRangeValue(Map attrBundle) {
 	// update if necessary.
 	if(needsUpdate) myUI.updateMapper();
 
         if (attrBundle == null || attrName == null) {return null;}
         if (this.size() == 0) {return null;}
         Object attrValue = attrBundle.get(attrName);
         if ( !(attrValue instanceof Number) ) {return null;}
         return getRangeValue((Number)attrValue);
     }
     
     private Object getRangeValue(Number domainValue) {
         Number minDomain = (Number)this.firstKey();
         /* if given domain value is smaller than any in our Vector,
 	   return the range value for the smallest domain value we have */
         int firstCmp = compareValues(domainValue, minDomain);
         if (firstCmp <= 0) {
             BoundaryRangeValues bv =
 		(BoundaryRangeValues)this.get(minDomain);
 	    if (firstCmp < 0) {return bv.lesserValue;} else {return bv.equalValue;}
 	}
         /* if given domain value is larger than any in our Vector,
         return the range value for the largest domain value we have */
         Number maxDomain = (Number)this.lastKey();
         if (compareValues(domainValue, maxDomain) > 0) {
             BoundaryRangeValues bv =(BoundaryRangeValues)this.get(maxDomain);
             return bv.greaterValue;
         }
         
         /* OK, it's somewhere in the middle, so find the boundaries and
 	 * pass to our interpolator function. First check for a null
 	 * interpolator function */
 	if (this.fInt == null) {return null;}
 
 	/* Note that the following set is sorted since it comes from a SortedMap.
 	 * Also, the case of the inValue equalling the smallest key was
 	 * checked above. */
 	Set domainValues = this.keySet();
 	Iterator i = domainValues.iterator();
 	Number lowerDomain = (Number)i.next();
 	Number upperDomain = null;
 	for ( ; i.hasNext(); ) {
 	    upperDomain = (Number)i.next();
             int cmpValue = compareValues(domainValue, upperDomain);
 	    if (cmpValue == 0) {
 		BoundaryRangeValues bv = (BoundaryRangeValues)this.get(upperDomain);
 		return bv.equalValue;
 	    } else if (cmpValue < 0) {
 		break;
 	    } else {
 		lowerDomain = upperDomain;
 	    }
 	}
 
 	/* this is tricky. The desired domain value is greater than
 	 * lowerDomain and less than upperDomain. Therefore, we want
 	 * the "greater" field of the lower boundary value (because the
 	 * desired domain value is greater) and the "lesser" field of
 	 * the upper boundary value (semantic difficulties).
 	 */
 	BoundaryRangeValues lv =
 	    (BoundaryRangeValues)this.get(lowerDomain);
 	Object lowerRange = lv.greaterValue;
 	BoundaryRangeValues gv =
 	    (BoundaryRangeValues)this.get(upperDomain);
 	Object upperRange = gv.lesserValue;
 
 	return this.fInt.getRangeValue(lowerDomain, lowerRange,
 				       upperDomain, upperRange,
 				       domainValue);
     }
     
     /**
      * Helper function to compare Number objects. This is needed because Java
      * doesn't allow comparing, for example, Integer objects to Double objects.
      */
     private int compareValues(Number probe, Number target) {
         double d1 = probe.doubleValue();
         double d2 = target.doubleValue();
         if (d1 < d2) {return -1;} else if (d1 > d2) {return 1;} else {return 0;}
     }
         
     /**
      * Redefine the put method to only accept non-null keys that are Numbers and
      * values that are non-null BoundaryRangeValues objects, each of which must
      * contains objects of the correct type.
      */
     public Object put(Object key, Object value) {
         if (key == null || value == null) {
             StringBuffer sb = new StringBuffer();
             String lineSep = System.getProperty("line.separator");
             sb.append("Invalid map entry: cannot accept null values" + lineSep);
             sb.append("key = " + key + ", value = " + value);
             throw new ClassCastException(sb.toString());
         }
         if ( !(key instanceof Number) ) {
             String s = "Invalid map key: expected a Number, got class "
                     + key.getClass().toString();
             throw new ClassCastException(s);
         }
         //because this is a SortedMap, we want to convert the Number key to
         //a Double to avoid ClassCastExceptions when comparing keys
         Double d = new Double( ((Number)key).doubleValue() );
         
         if ( !(value instanceof BoundaryRangeValues) ) {
             String s = "Invalid map entry: expected a BoundaryRangeValues object,"
                     + " got class " + value.getClass().toString();
             throw new ClassCastException(s);
         }
         BoundaryRangeValues brv = (BoundaryRangeValues)value;
         testClass(brv.lesserValue, "lesserValue");
         testClass(brv.equalValue, "equalValue");
         testClass(brv.greaterValue, "greaterValue");
         //if we get to here, the value is fine and we can add it
         return super.put(d, value);
     }
     
     /**
      * This helper method tests objects against the expected class.
      */
     private void testClass(Object o, String label) throws ClassCastException {
         if ( !(rangeClass.isInstance(o)) ) {
             StringBuffer sb = new StringBuffer();
             String lineSep = System.getProperty("line.separator");
             sb.append("Invalid map entry: BoundaryRangeValues field " + label);
             sb.append(":" + lineSep);
             sb.append("Expected class " + rangeClass.toString() );
             sb.append(", got class " + o.getClass().toString() + lineSep);
             throw new ClassCastException(sb.toString());
         }
     }
     
     /**
      * Redefine the putAll method to only accept keys that are Numbers and
      * values that are BoundaryRangeValues objects, each of which must
      * contains objects of the correct type.
      */
     public void putAll(Map m) {
         if (m == null) {return;}
         for (Iterator si = m.keySet().iterator(); si.hasNext(); ) {
             Object key = si.next();
             put( key, m.get(key) );
         }
     }
 
     /**
      * Customize this object by applying mapping defintions described by the
      * supplied Properties argument.
      */
     public void applyProperties(Properties props, String baseKey, ValueParser parser) {
         String contKey = baseKey + ".controller";
         String contValue = props.getProperty(contKey);
         if (contValue != null) {setControllingAttributeName(contValue, null, false);}
         
         String intKey = baseKey + ".interpolator";
         String intValue = props.getProperty(intKey);
         if (intValue == null) {
             System.err.println("Warning: while parsing attributeMap properties:");
             System.err.println("    no interpolator specified for");
             System.err.println("    continuous key: " + baseKey);
         } else {
             setInterpolator( InterpolatorFactory.newInterpolator(intValue) );
         }
         
         String bvNumKey = baseKey + ".boundaryvalues";
 	String bvNumString = props.getProperty(bvNumKey);
 	if (bvNumString == null) {
 	    System.err.println("Warning: while parsing attributeMap properties:");
 	    System.err.println("    no boundary values specified for");
 	    System.err.println("    continuous key: " + baseKey);
 	    return;
 	}
 	int numBV;
 	try {
 	    numBV = Integer.parseInt(bvNumString);
 	} catch (NumberFormatException e) {
 	    System.err.println("Error parsing attributeMap properties:");
 	    System.err.println("    Expected number value for key: "
 			       + bvNumString);
 	    return;
 	}
 	for (int i=0; i<numBV; i++) {
 	    String bvBase = baseKey + ".bv" + Integer.toString(i);
 	    String dvKey = bvBase + ".domainvalue";
 	    String dvString = props.getProperty(dvKey);
 	    if (dvString == null) {
 		System.err.println("Error parsing attributeMap properties:");
 		System.err.println("    expected value for key: " + dvKey);
 		continue;
 	    }
             Double dVal = null;
             try {
 		dVal = Double.valueOf(dvString);
 	    } catch (NumberFormatException e) {
 		System.err.println("Error parsing attributeMap properties:");
 		System.err.println("    expected number value for key: "+ dvKey);
 		continue;
 	    }
 	    BoundaryRangeValues bv = new BoundaryRangeValues();
 	    String lKey = bvBase + ".lesser";
 	    String lString = props.getProperty(lKey);
             Object lValue = parser.parseStringValue(lString);
 	    bv.lesserValue = lValue;
 	    String eKey = bvBase + ".equal";
 	    String eString = props.getProperty(eKey);
             Object eValue = parser.parseStringValue(eString);
 	    bv.equalValue = eValue;
 	    String gKey = bvBase + ".greater";
 	    String gString = props.getProperty(gKey);
             Object gValue = parser.parseStringValue(gString);
 	    bv.greaterValue = gValue;
 
 	    put(dVal,bv);
 	}
     }
     
     /**
      * Return a Properties object with entries suitable for customizing this
      * object via the applyProperties method.
      */
     public Properties getProperties(String baseKey) {
         Properties newProps = new Properties();
         //save the controlling attribute name
         String contKey = baseKey + ".controller";
         String contValue = getControllingAttributeName();
         newProps.setProperty(contKey, contValue);
         
         //save the interpolator
         String intKey = baseKey + ".interpolator";
         String intName = InterpolatorFactory.getName( this.getInterpolator() );
         newProps.setProperty(intKey, intName);
         
         //save the number of boundary values
         String bvNumKey = baseKey + ".boundaryvalues";
         int numBV = this.keySet().size();
         String numString = Integer.toString(numBV);
         newProps.setProperty(bvNumKey, numString);
         
         //save each of the boundary values
         int count=0;
         for (Iterator si = this.keySet().iterator(); si.hasNext(); count++) {
             String bvBase = baseKey + ".bv" + count;
             //save the domain value
             String bvKey = bvBase + ".domainvalue";
             Double dVal = (Double)si.next();
             String dValString = dVal.toString();
             newProps.setProperty(bvKey, dValString);
             
             //save the fields of the brv object
             BoundaryRangeValues brv = (BoundaryRangeValues)this.get(dVal);
             String lKey = bvBase + ".lesser";
             String lString = ObjectToString.getStringValue(brv.lesserValue);
             newProps.setProperty(lKey, lString);
             String eKey = bvBase + ".equal";
             String eString = ObjectToString.getStringValue(brv.equalValue);
             newProps.setProperty(eKey, eString);
             String gKey = bvBase + ".greater";
             String gString = ObjectToString.getStringValue(brv.greaterValue);
             newProps.setProperty(gKey, gString);
         }
         return newProps;
     }
         
     /////////////////// begin interface code.
 
     private class ContinuousUI extends JPanel {
 	
 	private static final int LESSER = 0;
 	private static final int EQUAL = 1;
 	private static final int GREATER = 2;
 
 	private JDialog parentDialog;
 	private ContinuousMapping cm;
 
 	private JLabel spaces = new JLabel(" ");
 	private JLabel upperThresholdLabel= new JLabel("Threshold");
 	private JLabel upperColorLabel=new JLabel("Appearance");
 
 	// numberOfPoints should only be accessed by three methods:
 	// getPointCount(), addToPoints(), and removeFromPoints().
 	private int numberOfPoints=0;
 
 	private Vector points;
 	private Vector lesserButtons, equalButtons, greaterButtons;
 	private Vector lesserColors, equalColors, greaterColors;
 	private Vector lesserVDs, equalVDs, greaterVDs;
 	private Vector pointText, delButtons;
 
 	GridBagGroup g;
 	PointTextListener ptl;
 
 	/**
 	 *  ContinuousUI is based on an older class, dialogs/PointsAndColors.
 	 *
 	 *  @param jd parent dialog
 	 *  @param network the network
 	 */
 	ContinuousUI (JDialog jd, Network network)
 	{
 	    this.parentDialog = jd;
 	    this.cm = ContinuousMapping.this;
 	    initializeVectors();
 	    establishPointsAndAppearances();
 	    createColorPanel();
 	}
 
 	/** initializeVectors() simply calls new for each Vector. */
 	private void initializeVectors() {
 	    initializePointVectors();
 	    initializeUIVectors();
 	}
 	/** initializePointVectors() calls new for each data Vector. */
 	private void initializePointVectors() {
 	    lesserColors = new Vector();
 	    equalColors = new Vector();
 	    greaterColors = new Vector();
 	    points = new Vector();
 	}
 	/** initializeUIVectors() calls new for each interface Vector. */		private void initializeUIVectors() {
 	    lesserButtons = new Vector();
 	    equalButtons = new Vector();
 	    greaterButtons = new Vector();
 	    
 	    lesserVDs = new Vector();
 	    equalVDs = new Vector();
 	    greaterVDs = new Vector();
 	    
 	    pointText = new Vector();
 	    delButtons = new Vector();
 	}
 
 	/** establishPointsAndAppearances loads an existing map. */
 	private void establishPointsAndAppearances() {
 	    // get boundary values
 	    Iterator it = cm.keySet().iterator();
 	    // iterate over the Double keys in the map.
 	    while(it.hasNext()) {
 		Double doubleBVal = (Double)it.next();
 		if(doubleBVal == null) return;
 		BoundaryRangeValues bvObj =
 		    (BoundaryRangeValues)cm.get(doubleBVal);
 		addToPoints(bvObj,doubleBVal);
 	    }
 	}
 
 	/** encapsulating method for adding a range point */
 	private void addToPoints(BoundaryRangeValues brv, Double point) {
 	    points.add(point);
 	    lesserColors.add(brv.lesserValue);
 	    equalColors.add(brv.equalValue);
 	    greaterColors.add(brv.greaterValue);
 	    numberOfPoints++;
 	}
 	/** encapsulating method for removing a range point */
 	private void removeFromPoints(int index) {
 	    lesserColors.removeElementAt(index);
 	    equalColors.removeElementAt(index);
 	    greaterColors.removeElementAt(index);
 	    points.removeElementAt(index);
 	    numberOfPoints--;
 	}
 	/** encapsulating method for finding out the number of points. */
 	private int getPointCount() {
 	    return numberOfPoints;
 	}
 
 	/** setEnabled() enables or disables all components of the panel.
 	 *  @param b whether to enable the panel (true) or disable it (false)
 	 */
 	public void setEnabled(boolean b) {
 	    for(int i=0;i<getPointCount();i++) {
 		((ValueDisplayer)lesserVDs.get(i)).setEnabled(b);
 		((ValueDisplayer)equalVDs.get(i)).setEnabled(b);
 		((ValueDisplayer)greaterVDs.get(i)).setEnabled(b);
 		((JButton)lesserButtons.get(i)).setEnabled(b);
 		((JButton)equalButtons.get(i)).setEnabled(b);
 		((JButton)greaterButtons.get(i)).setEnabled(b);
 		((JTextField)pointText.get(i)).setEnabled(b);
 		((JButton)delButtons.get(i)).setEnabled(b);
 	    }
 	}
 
 	/** updating color mapper: uses values that are in the
 	 *  interface (labels, buttons, textfields) to update the
 	 *  internally maintained ContinuousMapper.  updateMapper()
 	 *  is called within getContinuousMapper() before it returns.
 	 */
 	public void updateMapper() {
 	    Iterator it = cm.keySet().iterator();
 	    Vector vTemp = new Vector();
 	    while(it.hasNext()) {
 		vTemp.add((Double)it.next());
 	    }
 	    Iterator it2 = vTemp.iterator();
 	    while(it2.hasNext()) {
 		cm.remove((Double)it2.next());
 	    }
 	    
 	    // use the equal colors for all colors except first and last.
 	    for(int i=0; i<getPointCount(); i++) {
 		if(i!=0) {
 		    lesserColors.set(i,equalColors.get(i));
 		}
 		if(i!=(getPointCount()-1)) {
 		    greaterColors.set(i,equalColors.get(i));
 		}
 	    }
 
 	    // make them into boundary range values, and add to mapping.
 	    for(int i=0; i<getPointCount(); i++) {
 		BoundaryRangeValues bvObj = new BoundaryRangeValues ();
 		bvObj.lesserValue = lesserColors.get(i);
 		bvObj.equalValue = equalColors.get(i);
 		bvObj.greaterValue = greaterColors.get(i);
 		cm.put((Double)points.get(i),bvObj);
 	    }
 
 	    // update is no longer necessary; mapper is up-to-date.
 	    needsUpdate=false;
 	}
 
 	/** createColorPanel generates the panel that is the purpose
 	 *  of this class.
 	 */
 	public void createColorPanel() {
 	    g = new GridBagGroup();
 	    ptl = new PointTextListener();
 
 	    JButton newPointButton = new JButton("Add Point");
 	    newPointButton.addActionListener(new NewPointListener());
 	    MiscGB.insert(g,newPointButton,4,0);
 
 	    for(int i=0;i<getPointCount();i++) {
 		JTextField text = new JTextField(points.get(i).toString(),6);
 		pointText.add(text);
 		text.addFocusListener(ptl);
 		MiscGB.insert(g,text,1,i+1);
 
 		JButton bDel = new JButton("Del");
 		delButtons.add(bDel);
 		bDel.addActionListener(new DelListener(i));
 		MiscGB.insert(g,bDel,0,i+1);
 
 		createButtonAndVD(LESSER,i);
 		createButtonAndVD(EQUAL,i);
 		createButtonAndVD(GREATER,i);
 	    }
 	    add(g.panel);
 	}
 
 	private void createButtonAndVD(int lesserEqualGreater, int i) {
 	    Vector vColors=null, vVDs=null, vButtons=null;
 	    String buttonString;
 	    int offset=0;
 	    switch(lesserEqualGreater) {
 	    case LESSER:
 		vColors=lesserColors;
 		vVDs=lesserVDs;
 		vButtons=lesserButtons;
 		buttonString="Below";
 		offset=0;
 		break;
 	    case EQUAL:
 		vColors=equalColors;
 		vVDs=equalVDs;
 		vButtons=equalButtons;
 		buttonString="Equal";
 		offset=1;
 		break;
 	    case GREATER:
 	    default:
 		vColors=greaterColors;
 		vVDs=greaterVDs;
 		vButtons=greaterButtons;
 		buttonString="Above";
 		offset=2;
 		break;
 	    }
 	    Object o = vColors.get(i);
 	    ValueDisplayer vd =
 		ValueDisplayer.getDisplayFor(parentDialog,"Select Appearance",o);
 	    ValueDisplayerItemListener vdil =
 		new ValueDisplayerItemListener(vColors,i);
 	    vd.addItemListener(vdil);
 	    JButton b = new JButton(buttonString);
 	    b.addActionListener(vd.getInputListener());
 	    vVDs.add(vd);
 	    vButtons.add(b);
 	    if(((i==0)&&(lesserEqualGreater==LESSER)) ||
 	       (lesserEqualGreater==EQUAL) ||
 	       ((i==getPointCount()-1)&&(lesserEqualGreater==GREATER))) {
 		MiscGB.insert(g,b,2,i+offset,1,1,GridBagConstraints.HORIZONTAL);
 		MiscGB.insert(g,vd,3,i+offset);
 	    }
 	}
 	// this is the listener for the value displayer.
 	private class ValueDisplayerItemListener implements ItemListener {
 	    Vector v;
 	    int index;
 	    ValueDisplayerItemListener(Vector v, int index) {
 		this.v=v;
 		this.index=index;
 	    }
 	    public void itemStateChanged(ItemEvent e) {
 		Object o = ((ValueDisplayer)e.getItemSelectable()).getValue();
 		// ui for mapper could have been modified.
 		needsUpdate=true;
 		v.set(index,o);
 	    }
 	}
 
 	/** redoInterface() throws out the interface and regenerates it. */
 	private void redoInterface() {
 	    // throw out old color panel
 	    remove(g.panel);
 	    initializeUIVectors();
 	    // create a new one based on the current data
 	    createColorPanel();
 	    // re-pack everything.
 	    g.panel.validate();
 	    parentDialog.validate();
 	    parentDialog.pack();
 	    // ui for mapper is modified.
 	    needsUpdate=true;
 	    // go ahead and update the mapping.
 	    updateMapper();
 	}
 
 	/** DelListener deletes a point from the data structure
 	 *  and form the interface.  */
 	private class DelListener implements ActionListener {
 	    int index=-1;
 	    DelListener(int i) {index = i;}
 	    public void actionPerformed(ActionEvent e) {
 		if((index<0)||(index>=getPointCount())) return;
 		// remove element from the colors and points
 		removeFromPoints(index);
 		redoInterface();
 	    }
 	}
 
 	// this is the listener for the button that creates new points.
 	private class NewPointListener implements ActionListener {
 	    public void actionPerformed(ActionEvent e) {
 		Double point;
 		int i=getPointCount();
 		BoundaryRangeValues brv = new BoundaryRangeValues ();
 		if(i>0) {
 		    brv.lesserValue = lesserColors.get(i-1);
 		    brv.equalValue = equalColors.get(i-1);
 		    brv.greaterValue = greaterColors.get(i-1);
 		    point = new Double(((Double)points.get(i-1)).doubleValue());
 		}
 		else {
 		    brv.lesserValue = defaultObj;
 		    brv.equalValue = defaultObj;
 		    brv.greaterValue = defaultObj;
 		    point = new Double(0.0);
 		}
 		addToPoints(brv,point);
 
 		redoInterface();
 	    }
 	}
 
 	// this is the listener that listens to the JTextFields.
 	private class PointTextListener implements FocusListener { 
 	    public void focusGained (FocusEvent e) {
 		validate();
 	    }
 	    public void focusLost (FocusEvent e) {
 		validate();
 	    }
 	    private String fixZeroLength(String s, double d, JTextField tf) {
 		if(s.length()==0) {
 		    String r=Double.toString(d);
 		    tf.setText(r);
 		    return r;
 		}
 		else return s;
 	    }
 	    private String dropNonNumeric(String s)
 	    { return s.replaceAll("[^0-9+.-]",""); }
 	    private Double makeDouble(String s,
 				      double minValue,
 				      double defaultValue,
 				      double maxValue,
 				      boolean isMin,
 				      boolean isMax) {
 		double d;
 		try {
 		    d = Double.parseDouble(s);
 		    if(!isMax && d>maxValue) d = defaultValue;
 		    if(!isMin && d<minValue) d = defaultValue;
 		}
 		catch (NumberFormatException nfe) {
 		    System.out.println("Not actually a double: " + s);
 		    d = defaultValue;
 		}
 		return new Double(d);
 	    }
 	    private void validate() {
 		for (int i=0;i<getPointCount();i++) {
 		    String pt2 =
 			dropNonNumeric(((JTextField)pointText.get(i)).getText());
 		    pt2 = fixZeroLength(pt2, ((Double)points.get(i)).doubleValue(),
 					((JTextField)pointText.get(i)));
 		    
 		    double minD = ((Double)points.get(i)).doubleValue();
 		    double defD = ((Double)points.get(i)).doubleValue();
 		    double maxD = ((Double)points.get(i)).doubleValue();
 		    boolean bMin = true;
 		    boolean bMax = true;
 		    if(i>0) {
 			minD = ((Double)points.get(i-1)).doubleValue();
 			bMin = false;
 		    }
 		    if(i<getPointCount()-1) {
 			maxD = ((Double)points.get(i+1)).doubleValue();
 			bMax = false;
 		    }
 		    points.set(i,makeDouble(pt2,minD,defD,maxD,bMin,bMax));
 		    ((JTextField)
 		     pointText.get(i)).setText(((Double)points.get(i)).toString());
 		}
 		// ui for mapper could have been modified.
 		needsUpdate=true;
 	    }
 	} // PointTextListener
     } // ContinuousUI
 
 }
