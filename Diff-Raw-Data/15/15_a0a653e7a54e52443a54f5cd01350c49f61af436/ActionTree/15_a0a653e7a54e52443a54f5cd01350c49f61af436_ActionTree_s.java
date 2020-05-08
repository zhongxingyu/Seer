 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.lookup;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import com.comphenix.xp.Action;
 
 public abstract class ActionTree<TKey> extends SearchTree<TKey, Action>{
 
 	protected double multiplier;
 	
 	public ActionTree(double multiplier) {
 		this.multiplier = multiplier;
 	}
 	
 	public ActionTree(ActionTree<TKey> other, double multiplier) {
 		this.multiplier = multiplier;
 		this.flatten = other.flatten;
 		this.paramCount = other.paramCount;
 		this.currentID = other.currentID;
 	}
 	
 	@Override
	public Action get(TKey element) {
		Action result = super.get(element);
 		
 		// Automatically include the multiplier
		if (result != null)
			return result.multiply(multiplier);
 		else
			return result;
 	}
 	
 	/**
 	 * Returns a list of every stored range (scaled by experience) in this search tree.
 	 * @return Every stored range.
 	 */
 	@Override
 	public Collection<Action> getValues() {
 
 		// Add multiplier
 		List<Action> scaledValues = new ArrayList<Action>();
 		
 		for (Action action : super.getValues()) {
 			scaledValues.add(action.multiply(multiplier));
 		}
 		
 		return scaledValues;
 	}
 	
 	public double getMultiplier() {
 		return multiplier;
 	}
 }
