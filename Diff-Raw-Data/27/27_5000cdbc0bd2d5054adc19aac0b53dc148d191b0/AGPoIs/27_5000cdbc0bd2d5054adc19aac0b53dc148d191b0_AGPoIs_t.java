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
 
 package agai;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.springrts.ai.AIFloat3;
 
 // TODO: Auto-generated Javadoc
 /**
  * @author matze
  *
  */
 
 class AGPoI{ //Point of Interest
 	private AIFloat3 pos;
 	private boolean visited;
 	private int type = -1;
 	private boolean built;
 	AGPoI(AIFloat3 pos, int type){
 		this.pos=pos;
 		this.type=type;
 		built=false;
 		visited=false;
 	}
 	
 	/**
 	 * Gets if the PoI was already visited (or if a building is built there, visiting isn't necessary)
 	 * 
 	 * @return the visited
 	 */
 	public boolean isVisited(){
 		return visited;
 	}
 	
 	/**
 	 * Sets the visited.
 	 * 
 	 * @param visited the new visited
 	 */
 	public void setVisited(boolean visited){
 		this.visited=visited;
 	}
 	
 	/**
 	 * Gets the pos.
 	 * 
 	 * @return the pos
 	 */
 	public AIFloat3 getPos(){
 		return this.pos;
 	}
 	
 	/**
 	 * Type.
 	 * 
 	 * @return the int
 	 */
 	public int getType(){
 		return this.type;
 	}
 	
 	/**
 	 * Checks if is built.
 	 * 
 	 * @return true, if is built
 	 */
 	public boolean isBuilt() {
 		return built;
 	}
 	
 	/**
 	 * Sets the built.
 	 * 
 	 * @param built the new built
 	 */
 	public void setBuilt(boolean built) {
 		this.built = built;
 	}
 }
 
 
 /**
  * The Class AGPoIs.
  */
 public class AGPoIs {
 	
 	/** The Constant PoIAny. */
 	static final int PoIAny=Integer.MAX_VALUE; //FIXME: descending, because we use the id of the resources (0, 1, 2, ... to avoid collisions)
 	
 	/** The Constant PoIEnemy. */
 	static final int PoIEnemy=Integer.MAX_VALUE-1;
 	
 	
 	/** The poi. */
 	List <AGPoI> poi;
 	
 	/** The ai. */
 	AGAI ai;
 
 	/**
 	 * Instantiates a new aG po is.
 	 * 
 	 * @param ai the ai
 	 */
 	AGPoIs(AGAI ai){
 		this.ai=ai;
 		this.poi=new ArrayList<AGPoI>();
 	}
 	
 	/**
 	 * Gets the nearest point of interest to scout.
 	 * 
 	 * @param curpos position to search the nearest point from
 	 * @param type the type
 	 * @param free list all free PoIs?
 	 * @param visited list already visted PoIs?
 	 * 
 	 * @return the nearest poi
 	 */
	public AGPoI getNearestPoi(AIFloat3 curpos, int type, boolean free, boolean visited){
 		double mindistance=Double.MAX_VALUE;
 		double tmp;
 		AGPoI ret=null;
 		if (poi.size()==0){
 			ai.msg("getNearestPoi: No PoI found!");
 			return ret;
 		}
 		for(int i=0; i<poi.size(); i++){
 			if (((type==PoIAny) || (poi.get(i).getType()==type)) &&
 					!(visited && poi.get(i).isVisited()) &&
 					!(free && poi.get(i).isBuilt()) ){
 				tmp=ai.getDistance(curpos, poi.get(i).getPos());
 				if (tmp<mindistance){
 					mindistance=tmp;
 					ret=poi.get(i);
 				}
 			}
 		}
 		if (poi==null){ //all points visited.. start from beginning
 			for(int i=0; i<poi.size(); i++){
 				poi.get(i).setVisited(false);
 			}
 			return getNearestPoi(curpos, type, free, visited);
 		}
 		return ret;
 	}
 	
 	/**
 	 * Gets the nearest poi where no building is built.
 	 * 
 	 * @param curpos the curpos
 	 * @param type the type
 	 * 
 	 * @return the nearest free poi
 	 */
 	public AGPoI getNearestFreePoi(AIFloat3 curpos, int type){
 		return getNearestPoi(curpos,type,true,false);
 	}
 	
 	/**
 	 * Adds the Point to the list of PoIs.
 	 * 
 	 * @param pos the pos
 	 * @param type the type
 	 */
 	public void add(AIFloat3 pos, int type){
 		poi.add(new AGPoI(pos,type));
 	}
 	
 	/**
 	 * Returns count of PoIs of type.
 	 * 
 	 * @param type the type
 	 * 
 	 * @return the int
 	 */
 	public int size(int type){
 		int count=0;
 		for(int i=0; i<poi.size(); i++){
 			if ( (poi.get(i).getType() & type) != 0)
 				count++;
 		}
 		return count;
 	
 	}
 	
 	/**
 	 * Gets the PoI at pos.
 	 * 
 	 * @param index the index
 	 * @param type the type
 	 * 
 	 * @return the po i
 	 */
 	public AGPoI get(int index, int type){
 		int count=0;
 		for(int i=0; i<poi.size(); i++){
 			if ( (poi.get(i).getType() & type) != -1){
 				if (count==index)
 					return poi.get(i);
 				count++;
 			}
 		}
 		return null;
 	}
 
 }
