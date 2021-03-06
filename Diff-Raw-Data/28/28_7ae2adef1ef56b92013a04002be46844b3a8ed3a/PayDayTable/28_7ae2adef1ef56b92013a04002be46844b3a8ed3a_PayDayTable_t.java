 /*
  * This file is part of Craftconomy3.
  *
  * Copyright (c) 2011-2012, Greatman <http://github.com/greatman/>
  *
  * Craftconomy3 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Craftconomy3 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Craftconomy3.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.greatmancode.craftconomy3.database.tables;
 
 import com.alta189.simplesave.Field;
 import com.alta189.simplesave.Id;
 import com.alta189.simplesave.Table;
 
 @Table("cc3_payday")
 public class PayDayTable {
 
 	@Id
 	public int id;
 	
 	@Field
 	public String name;
 	
 	@Field
 	public boolean disabled;
 	
 	/**
 	 * In seconds
 	 */
 	@Field
	public int time;
 	
 	@Field
 	public String account;
 	
 	/**
 	 * 0 = wage
 	 * 1 = tax
 	 */
 	@Field
 	public int status;
 	
 	@Field
 	public int currency_id;
 	
 	@Field
 	public double value;
 	
 	@Field
 	public String worldName;
 }
