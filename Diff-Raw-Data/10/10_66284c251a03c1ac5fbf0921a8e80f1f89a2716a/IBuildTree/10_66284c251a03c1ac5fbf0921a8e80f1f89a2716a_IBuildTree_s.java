 /*
  * Copyright (C) 2009 Matthias Ableitner (http://abma.de/)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package agai.info;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import agai.AGAI;
 import agai.AGInfos;
 import agai.unit.AGUnit;
 
 import com.springrts.ai.oo.Resource;
 import com.springrts.ai.oo.Unit;
 import com.springrts.ai.oo.UnitDef;
 
 /**
  * The Class BuildTree to search for the "path" to build a unit
  */
 public class IBuildTree {
 
 	/** The ai. */
 	private AGAI ai;
 
 	/** The mark. */
 	private int mark; // value to mark nodes already visited in search
 
 	/** The ress. */
 	private List<Resource> ress;
 
 	/** The unit list. */
 	private HashMap<Integer, IBuildTreeUnit> unitList;
 
 	/**
 	 * Instantiates a new aG build tree.
 	 * 
 	 * @param ai
 	 *            the ai
 	 * @param infos 
 	 */
 	public IBuildTree(AGAI ai, AGInfos infos) {
 		this.mark = 0;
 		this.ai = ai;
 		this.ress = ai.getClb().getResources();
 		updateGraph();
 	}
 	
 	public void updateGraph(){
 		generateList(ai.getClb().getUnitDefs());
 		generateGraph();
 	}
 
 	/**
 	 * Adds the node to the graph (links to other units / create sublinks if not
 	 * exist).
 	 * 
 	 * @param parent
 	 *            the parent node
 	 * @param unit
 	 *            the unit
 	 */
 	public void linkNode(UnitDef unit, UnitDef parent) {
 		IBuildTreeUnit pnode = searchNode(parent); // search parent entry
 		IBuildTreeUnit node = searchNode(unit); 
 		for (int i=0; i<pnode.getNodes().size(); i++){//check if link already exists
 			if (pnode.getNodes().get(i).getId()==node.getId())
 				return;
 		}
 		pnode.addNode(node); //link nodes
 		node.setParent(pnode);
 	}
 
 	/**
 	 * Dump graph to stdout copy the text into a file and then convert it with
 	 * graphiz: dot -Tpdf techtree.dot -o techtree.pdf
 	 */
 	public void dumpGraph() {
 		System.out.println("digraph G {");
 		for (int i = 0; i < unitList.size(); i++) {
 			IBuildTreeUnit u = unitList.get(i);
 			for (int j = 0; j < u.getNodes().size(); j++) {
 				IBuildTreeUnit child = u.getNodes().get(j);
 				System.out.println(u.getUnit().getName() + " -> "
 						+ child.getUnit().getName() + ";");
 			}
 		}
 		System.out.println("}");
 	}
 	
 	public String toString(){
 		String res="";
 		for (int i = 0; i < unitList.size(); i++) {
 			IBuildTreeUnit u = unitList.get(i);
 			res=res + u.toString();
 			if (i+1<unitList.size());
 				res=res+"\n";
 		}
 		return res;
 	}
 	/**
 	 * Dump units.
 	 */
 	public void dumpUnits() {
 		for (int i = 0; i < unitList.size(); i++) {
 			UnitDef u = unitList.get(i).getUnit();
 			ai.logNormal(u.getName() + " " + u.hashCode());
 		}
 	}
 
 	/**
 	 * Links the Nodes together
 	 */
 	private void generateGraph() {
 		ai.logInfo("Initializing Build Graph....");
 		Iterator<IBuildTreeUnit> it = unitList.values().iterator();
 		while (it.hasNext()){
 			IBuildTreeUnit cur = it.next();
 			List<UnitDef> buildopts = cur.getUnit().getBuildOptions();
 			for (int j=0; j<buildopts.size(); j++){
 				linkNode(buildopts.get(j), cur.getUnit());
 			}
 		}
 	}
 
 	/**
	 * Fill unitList with all avaiable Units from Graph.
 	 * @param list 
 	 */
 	private void generateList(List<UnitDef> list) {
 		unitList = new HashMap<Integer, IBuildTreeUnit>();
 		for (int i=0; i<list.size(); i++){
			unitList.put(list.get(i).getUnitDefId(), new IBuildTreeUnit(ai, list.get(i)));
 		}
 		List<Unit> units = ai.getClb().getFriendlyUnits();
 		for (int i=0; i<units.size(); i++){
 			this.searchNode(units.get(i).getDef()).incUnitcount();
 		}
 	}
 
 	/**
 	 * Gets an avaiable builder to build the unit.
 	 * 
 	 * @param unit
 	 *            the unit
 	 * 
 	 * @return the builders that can build the requested unit
 	 */
 	public List<AGUnit> getBuilder(UnitDef unit) {
 		IBuildTreeUnit node = searchNode(unit);
 		if (node == null)
 			return null;
 		LinkedList<IBuildTreeUnit> list = node.getBacklink();
 		LinkedList<AGUnit> res = new LinkedList<AGUnit>();
 		for (int i = 0; i < list.size(); i++) {
 			if (list.get(i).getUnitcount() > 0) { // search only for units available
 				List<AGUnit> units = ai.getUnits().getUnits(list.get(i).getUnit());
 				for (int j = 0; j < units.size(); j++) {
 					res.add(units.get(j));
 				}
 			}
 		}
 		if (res.size() == 0){
 			return null;
 		}
 		return res;
 	}
 	
 	/**
 	 * Checks if is unit available or planed.
 	 * 
 	 * @param unit the unit
 	 * 
 	 * @return true, if is unit available or planed
 	 */
 	public boolean isUnitAvailableOrPlaned(UnitDef unit){
 		IBuildTreeUnit u = searchNode(unit);
 		return ((u.getPlannedunits()>0) || (u.getUnitcount()>0));
 	}
 	/**
 	 * Gets the builds the time.
 	 * 
 	 * @param unit
 	 *            the unit
 	 * 
 	 * @return the builds the time
 	 */
 	public float getBuildTime(IBuildTreeUnit unit) {
 		return unit.getUnit().getBuildTime();
 	}
 
 	/**
 	 * Gets the price.
 	 * 
 	 * @param unit
 	 *            the unit
 	 * @param resource
 	 *            the resource
 	 * 
 	 * @return the price
 	 */
 	public float getPrice(IBuildTreeUnit unit, int resource) {
 		return unit.getUnit().getCost(ress.get(resource));
 	}
 
 	/**
 	 * Gets the unit list.
 	 * 
 	 * @return the unit list
 	 */
 	public Collection<IBuildTreeUnit> getUnitList() {
 		return unitList.values();
 	}
 
 	/**
 	 * Search node.
 	 * 
 	 * @param root
 	 *            the root
 	 * @param search
 	 *            the search
 	 * 
 	 * @return the aG build tree unit, tmp.parent.parent.parent ... until root
 	 *         contains the build path
 	 */
 	private IBuildTreeUnit searchNode(IBuildTreeUnit root, UnitDef search) {
 		LinkedList<IBuildTreeUnit> queue = new LinkedList<IBuildTreeUnit>();
 		IBuildTreeUnit tmp;
 		queue.add(root);
 		mark++;
 		while (queue.size() > 0) {
 			tmp = queue.removeFirst();
 			if (tmp.getUnit().getUnitDefId() == search.getUnitDefId()) { // node found, return path
 				return tmp;
 			}
 			tmp.setMark(mark);
 			for (int i = 0; i < tmp.getNodes().size(); i++) {
 				if (tmp.getNodes().get(i).getMark() != mark) {
 					queue.add(tmp.getNodes().get(i));
 					tmp.getNodes().get(i).setParent(tmp); // set backlink to recognize later the path we went so
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Search node.
 	 * 
 	 * @param node
 	 *            the node
 	 * 
 	 * @return the aG build tree unit
 	 */
 	public IBuildTreeUnit searchNode(UnitDef node) {
 		if (node==null)
 			return null;
 		return unitList.get(node.getUnitDefId());
 	}
 
 	/**
 	 * Returns all possible (also non-existing) Units that can build the Unit u 
 	 * 
 	 * @param u the u
 	 * 
 	 * @return the all builders
 	 */
 	public LinkedList <UnitDef>getAllBuilders(UnitDef u){
 		LinkedList<UnitDef> res=new LinkedList<UnitDef>();
 		Iterator<IBuildTreeUnit> it = unitList.values().iterator();
 		while (it.hasNext()){
 			IBuildTreeUnit tmp=searchNode(it.next(), u);
 			if (tmp!=null)
 				for (int j=0; j<tmp.getBacklink().size(); j++){
 					res.add(tmp.getBacklink().get(j).getUnit());
 				}
 		}
 		if (res.size()==0)
 			return null;
 		return res;
 	}
 
 	public List<UnitDef> getBuildPath(UnitDef unit) {
 		LinkedList<IBuildTreeUnit> queue = new LinkedList<IBuildTreeUnit>();
 		IBuildTreeUnit tmp;
 		IBuildTreeUnit root = searchNode(unit);
 		if (root!=null)
 			queue.add(root);
 		mark++;
 		root.setMark(mark);
 		while (queue.size() > 0) {
 			tmp = queue.removeFirst();
 			if ((tmp.getPlannedunits()>0) || (tmp.getUnitcount()>0)) { // node found, where unit is available return path
 				List <UnitDef> res=new LinkedList<UnitDef>();
 				tmp=tmp.getParent(); //don't add available builder
 				mark++;
 				while(tmp!=null){
 					if ((tmp.getMark()==mark) || 
 							(tmp.getPlannedunits()>0) || (tmp.getUnitcount()>0)) //go back until available builder
 						return res;
 					tmp.setMark(mark); //mark to avoid loops
 					res.add(tmp.getUnit());
 					ai.logDebug(tmp.getUnit().getName());
 					tmp=tmp.getParent();
 				}
 				if (res.size()==0)
 					return null;
 				return res;
 			}
 			tmp.setMark(mark);
 			for (int i = 0; i < tmp.getBacklink().size(); i++) {
 				if (tmp.getBacklink().get(i).getMark() != mark) {
 					queue.add(tmp.getBacklink().get(i));
 					tmp.getBacklink().get(i).setParent(tmp); // set backlink to recognize later the path we went so
 				}
 			}
 		}
 		return null;
 	}
 
 	public IResource getPrice(UnitDef unit){
 		IResource price=new IResource(ai);
 		for (int i=0; i<price.size(); i++){
 			Resource r=ai.getClb().getResources().get(i);
 			price.setCurrent(i, unit.getCost(r));
 		}
 		return price;
 	}
 
 }
