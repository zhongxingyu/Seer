 package moleculeMaker;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 public class ConnectionList 
 {
 	private HashMap<String, Element> elements;
 	private HashMap<String, Bond> bonds;
 	private HashMap<String, Arrow> arrows;
 
 	private MoleculeComponent selected;
 
 	public ConnectionList()
 	{
 		elements = new HashMap<String, Element>();
 		bonds = new HashMap<String, Bond>();
 		selected = null;
 
 		arrows = new HashMap<String, Arrow>();
 
 	}
 	
	private void clearSelected()
 	{
 		selected = null;
	}
	
	public void clearBonds()
	{
		clearSelected();
 		bonds = new HashMap<String, Bond>();
		clearArrows();
 	}
 	
 	public void clearElements()
 	{
		clearSelected();
 		bonds = new HashMap<String, Bond>();
 		arrows = new HashMap<String, Arrow>();
 		elements = new HashMap<String, Element>();
 	}
 	public void clearArrows()
 	{
		clearSelected();
 		arrows = new HashMap<String, Arrow>();
 	}
 	
 	/**
 	 * Accepts a location, and returns a MoleculeComponent if one exists
 	 * at the specified location
 	 * @param x The absolute X coordinate clicked
 	 * @param y The absolute Y coordinate clicked
 	 * @return The MoleculeComponent at the specified location, or null
 	 */
 	public Bond clickedBond(int x, int y)
 	{
 		for(Bond b : getBonds())
 		{
 			if (b.contains(x, y))
 			{
 				return b;
 			}
 		}
 		
 		return null;
 	}
 	
 	public Arrow clickedArrow(int x, int y)
 	{
 		for(Arrow a : getArrows())
 		{
 			if (a.contains(x, y))
 			{
 				return a;
 			}
 		}
 		
 		return null;
 	}
 	
 //	public MoleculeConnectorComponent containsConnector() {
 //		MoleculeConnectorComponent c = new 
 //	}
 
 	
 	private void removeBonds(MoleculeComponent selected2)
 	{
 		// Kudos to: http://joecode.blogspot.com/2004/10/hash-map-iteration.html
 		// for the nifty iteration technique, and thanks to:
     	// http://stackoverflow.com/questions/1110404/remove-Elements-from-a-hashset-while-iterating
     	// for pointing out, I needed to use i.remove() instead of Bond.remove(key);
 		
 		String tempKey = selected2.getKey().replace(" ", "");
 		
 		for (Iterator<String> i = bonds.keySet().iterator(); i.hasNext();)
 		{
 			String key = i.next();
 			
 		    if (key.contains(tempKey))
 		    {
 		    	i.remove(); 
 		    }
 		}
 	}
 	
 	/**
 	 * Add a MoleculeComponent (element, bond, or arrow) to the list
 	 * @param e The MoleculeComponent to add to the list
 	 */
 	public void add(MoleculeComponent e)
 	{
 		if (e.getClass() == Element.class) {
 			elements.put(e.getKey(), (Element) e);
 		}
 		else if (e.getClass() == Bond.class) {
 			bonds.put(e.getKey(), (Bond) e);
 		}
 		else if (e.getClass() == Arrow.class) {
 			arrows.put(e.getKey(), (Arrow) e);
 		}
 		
 	}
 
 	public ArrayList<MoleculeConnectorComponent> getBondsAndArrows()
 	{
 		ArrayList<MoleculeConnectorComponent> temp = new ArrayList<MoleculeConnectorComponent>();
 		
 		temp.addAll(getBondComponents());
 		temp.addAll(getArrowComponents());
 		
 		return temp;
 	}
 	
 	public void remove(MoleculeComponent e)
 	{
 		if (e == null) { return; }
 		
 		if (e.getClass() == Element.class) {
 			elements.remove(e.getKey());
 		}
 		else if (e.getClass() == Bond.class) {
 			bonds.remove(e.getKey());
 		}
 		else if (e.getClass() == Arrow.class) {
 			arrows.remove(e.getKey());
 		}
 		
 //		element.remove(e.getKey());
 	}
 
 	public Element[] getCoordinates()
 	{
 		// Return null if there are no Elements in map
 		if (elements.keySet().size() == 0) { return null; }
 		
 		Element[] temp = new Element[elements.keySet().size()];
 		
 		int i = 0;
 		for (Element e : elements.values())
 		{
 			temp[i] = e;
 			i++;
 		}
 		
 		return temp;
 	}
 
 	public boolean hasElements()
 	{
 		return elements.keySet().size() > 0;
 	}
 	
 	/**
 	 * Returns the element, bond, or arrow that was clicked on
 	 * @return
 	 */
 	public MoleculeComponent getClickedComponent(double x, double y)
 	{
 		// Check for element
 		double roundX = MoleculeGrid.getGraphCoordinateX(x);
 		double roundY = MoleculeGrid.getGraphCoordinateY(y);
 		
 		MoleculeComponent component = getElementAt(roundX, roundY);
 		if (component != null)
 		{
 			return component;
 		}
 		
 		for(Bond b : getBonds())
 		{
 			if (b.contains(x, y))
 			{
 				return b;
 			}
 		}
 		
 		for(Arrow a : getArrows())
 		{
 			if (a.contains(x, y))
 			{
 				return a;
 			}
 		}
 		
 		return null;
 	}
 	
 //	/**
 //	 * Add an ElementImproved to the list by creating a new ElementImproved at the specified coordinates
 //	 * @param x The X coordinate of the ElementImproved
 //	 * @param y The Y coordinate of the ElementImproved
 //	 */
 //	public void addElement(int x, int y)
 //	{
 //		Element e = new Element(x, y);
 //		addElement(e);
 //	}
 //	
 
 	public void setSelected(MoleculeComponent e)
 	{
 		
 		System.out.println(">>>>>>>>> Set selected called for " + e);
 		if (e == null) // If null, clear selection
 		{
 			if (selected != null) {
 				if (selected.getClass() == Element.class) {
 				elements.get(selected.getKey()).setSelected(false); // set the internal selection flag to true
 				}
 				else if (selected.getClass() == Bond.class) {
 					bonds.get(selected.getKey()).setSelected(false); // set the internal selection flag to true
 				}
 				else if (selected.getClass() == Arrow.class) {
 					arrows.get(selected.getKey()).setSelected(false);
 				}
 			}
 			
 			selected = null;
 			return;
 		}
 		
 		if (e.getClass() == Element.class) {
 			selected = elements.get(e.getKey()); // set selected to the newest Element selected
 			elements.get(selected.getKey()).setSelected(true); // set the internal selection flag to true
 		}
 		else if (e.getClass() == Bond.class) {
 			selected = bonds.get(e.getKey());
 			bonds.get(selected.getKey()).setSelected(true);
 		}
 		else if (e.getClass() == Arrow.class) {
 			selected = arrows.get(e.getKey());
 			arrows.get(selected.getKey()).setSelected(true);
 		}
 		
 	}
 	
 
 	/**
 	 * Clears the selected element, bond, or arrow and also removes it from its
 	 * appropriate HashMap.
 	 */
 	public void removeSelected()
 	{
 		if (selected == null) { return; }
 		
 		if (selected.getClass() == Element.class) {
 			removeBonds(selected); // if an element is deleted, attached bonds need to go too!
 			elements.remove(selected.getKey());	
 		}
 		else if (selected.getClass() == Bond.class) {
 			bonds.remove(selected.getKey());
 		}
 		else if (selected.getClass() == Arrow.class) {
 			arrows.remove(selected.getKey());
 		}
 		 
 		selected = null;
 	}
 	
 	public MoleculeComponent getSelected()
 	{
 		return (MoleculeComponent) selected;
 	}
 //	
 //	public void setDragging(MoleculeComponent e)
 //	{
 //		dragging = e;
 //	}
 //	
 //	public Element getDragging()
 //	{
 //		return dragging;
 //	}
 	
 	public boolean isSelected(MoleculeComponent e)
 	{
 		if (e == null)
 			return false;
 		
 		if (selected.getKey().equals(e.getKey()))
 			return true;
 
 		return false;
 	}
 	
 	public String toString()
 	{
 		return "Element List: " + elements.toString();
 	}
 	
 	public Element getElementAt(double i, double j) {
 		String key = "" + i + "," + j;
 		return getElementAt(key);
 	}
 	
 	public Element getElementAt(String key)
 	{
 		return elements.get(key);
 	}
 	
 //	public Element getMoving() {
 //		return moving;
 //	}
 //
 //	public void setMoving(MoleculeComponent moving) {
 //		this.moving = moving;
 //	}
 	
 	public ArrayList<Bond> getBonds()
 	{
 		return new ArrayList<Bond>(bonds.values());
 	}
 	
 	public ArrayList<MoleculeConnectorComponent> getBondComponents()
 	{
 		return new ArrayList<MoleculeConnectorComponent>(bonds.values());
 	}
 	
 	public ArrayList<Arrow> getArrows()
 	{
 		return new ArrayList<Arrow>(arrows.values());
 	}
 	
 	public ArrayList<MoleculeConnectorComponent> getArrowComponents()
 	{
 		return new ArrayList<MoleculeConnectorComponent>(arrows.values());
 	}
 
 	public void setBonds(HashMap<String, Bond> Bonds) {
 		this.bonds = Bonds;
 	}
 	
 	public void setArrows(HashMap<String, Arrow> arrows) {
 		this.arrows = arrows;
 	}
 
 	public HashMap<String, Element> getMap() {
 		return elements;
 	}
 	
 	public HashMap<String, Arrow> getArrowHash() {
 		return arrows;
 	}
 
 	public HashMap<String, Bond> getBondHash() {
 		return bonds;
 	}
 	
 }
