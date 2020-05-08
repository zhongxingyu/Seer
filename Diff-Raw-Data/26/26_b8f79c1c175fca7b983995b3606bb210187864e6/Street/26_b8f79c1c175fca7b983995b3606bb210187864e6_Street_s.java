 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.logic.state.fields;
 
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.DataConversionException;
 import org.jdom.Element;
 import org.ojim.logic.ServerLogic;
 import org.ojim.logic.accounting.Bank;
 
 public class Street extends BuyableField {
 
 	// Erlaube maximal ein Hotel.
 	public static final int MAXMIMUM_BUILT_LEVEL = 5;
 
 	private int builtLevel;
 	private int[] rentByLevel;
 
 	public Street(String name, int position, int[] rentByLevel, int builtLevel,
 			int price) {
 		super(name, position, price);
 		this.setRentAndBuiltLevel(rentByLevel, builtLevel);
 	}
 
 	public Street(String name, int position, int[] rentByLevel, int price) {
 		this(name, position, rentByLevel, 0, price);
 	}
 
 	public Street(String name, int position, int[] rentByLevel, int builtLevel,
 			int price, ServerLogic logic) {
 		super(name, position, price, logic);
 		this.setRentAndBuiltLevel(rentByLevel, builtLevel);
 	}
 
 	public Street(String name, int position, int[] rentByLevel, int price,
 			ServerLogic logic) {
 		this(name, position, rentByLevel, 0, price, logic);
 	}
 
 	public Street(Element element, ServerLogic logic,
 			Map<Integer, FieldGroup> groups) throws DataConversionException {
 		super(element, logic, groups);
 		this.rentByLevel = new int[Street.MAXMIMUM_BUILT_LEVEL];
 		/*
 		 * These guys think that generics are out -.- *facepalm*
 		 * http://www.jdom.org/pipermail/jdom-interest/2002-July/010244.html
 		 */
 		List rents = element.getChildren("rent");
 		for (Object object : rents) {
 			this.rentByLevel[((Element) object).getAttribute("level")
 					.getIntValue()] = Integer.parseInt(((Element) object)
 					.getText());
 		}
 	}
 
 	private void setRentAndBuiltLevel(int[] rentByLevel, int builtLevel) {
 		this.builtLevel = builtLevel;
 		this.rentByLevel = rentByLevel;
 	}
 
 	// xZise: Please activate the UTF-8 encoding in Eclipse!
 	// TODO: Noch benötigt?
 	public int getBuiltLevel() {
 		return this.builtLevel;
 	}
 
 	public int getRent() {
 		return getRent(this.builtLevel);
 	}
 
 	public int getRent(int level) {
 		// If no buildings here and all streets of the Group belong to the same
 		// owner, the rent is doubled
 		if (level == 0) {
 			for (Field field : this.getFieldGroup().getFields()) {
 				if (!(field instanceof Street)
 						|| ((Street) field).getOwner() == null
 						|| !this.getOwner().equals(((Street) field).getOwner())) {
 					// Not all the same owner: normal rent
 					return rentByLevel[0];
 				}
 			}
 			// else: double rent
 			return rentByLevel[0] * 2;
 		} else {
 			return rentByLevel[level];
 		}
 	}
 
 	public int getNumberOfHotels() {
 		return this.builtLevel / 5;
 	}
 
 	public int getNumberOfHouse() {
 		return this.builtLevel % 5;
 	}
 
 	/**
 	 * Changes the built level of this street by the given change.
 	 * 
 	 * @param level
 	 *            Changes of the level.
 	 */
	public boolean upgrade(int level, Bank bank) {
 		int newLevel = this.builtLevel + level;
 		if (newLevel < 0 || newLevel > 5) {
 			return false;
 		}
 		
		for(Field street : this.getFieldGroup().getFields()) {
			if(((Street)street).getBuiltLevel() < newLevel - 1) {
				return false;
			}
		}

		// Check if enough houses and hotels are in the Bank
		bank.takeHouses(-(builtLevel % 5));
		bank.takeHotels(-(builtLevel / 5));

		if (!bank.takeHouses(newLevel == 5 ? 4 : newLevel)) {
			return false;
		}
		if (!bank.takeHotels(newLevel / 5)) {
			return false;
		}
 		this.builtLevel = newLevel;
 		return true;
 	}
 
 	public void setFieldGroup(StreetFieldGroup fieldGroup) {
 		super.setFieldGroup(fieldGroup);
 	}
 
 	public StreetFieldGroup getFieldGroup() {
 		return (StreetFieldGroup) super.getFieldGroup();
 	}
 
 }
