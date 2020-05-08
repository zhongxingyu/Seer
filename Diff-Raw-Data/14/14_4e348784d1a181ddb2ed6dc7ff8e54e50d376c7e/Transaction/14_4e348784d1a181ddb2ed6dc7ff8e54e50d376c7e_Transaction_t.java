  /*
   * Copyright  2012 Mufasa developer unit
   *
   * This file is part of Mufasa Budget.
   *
   *	Mufasa Budget is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * Mufasa Budget is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public License
   * along with Mufasa Budget.  If not, see <http://www.gnu.org/licenses/>.
   */
 package it.chalmers.mufasa.android_budget_app.model;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 /**
  * A class for modeling a transaction from or to your bank account.
  * 
  * @author daubigne
  * 
  * 
  */
 public class Transaction {
 	private double amount;
 	private Date date;
 	private String name;
 	private Category category;
 	private int id;
 
 	public Transaction(int id, double amount, Date date, String name,
 			Category category) {
 		setAmount(amount);
 		setDate(date);
 		setName(name);
 		setCategory(category);
 		setId(id);
 
 	}
 
 	/**
 	 * Sets the amount of the transaction.
 	 */
 	private void setAmount(double amount) {
 		this.amount = amount;
 	}
 
 	/**
 	 * Sets the date of the transaction
 	 */
 	private void setDate(Date date) {
 		this.date = date;
 	}
 
 	/**
 	 * Sets the name of the transaction.
 	 */
 	private void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Sets the category of the transaction.
 	 */
 	private void setCategory(Category category) {
 		this.category = category;
 	}
 
 	/**
 	 * Sets the ID of the transaction.
 	 */
 	private void setId(int id) {
 		this.id = id;
 	}
 
 	/**
 	 * Returns the amount of the transaction.
 	 */
 	public double getAmount() {
 		return this.amount;
 	}
 
 	/**
 	 * Returns the name of the transaction.
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Returns the date of the transaction.
 	 */
 	public Date getDate() {
		Date temp = (Date)this.date.clone();
 		return temp;
 	}
 
 	/**
 	 * Returns the category of the transaction.
 	 */
 	public Category getCategory() {
		Category temp = this.category.clone();
 		return temp;
 	}
 	
 	/**
 	 * Returns the ID of the transaction.
 	 */
 	public int getId() {
 		return id;
 	}
 	
 	@Override
 	public String toString() {
 		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		
 		return "Transaction: id="+id+" date="+dateFormat.format(this.date)+" amount="+amount+" category="+category.toString(); 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(amount);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result
 				+ ((category == null) ? 0 : category.hashCode());
 		result = prime * result + ((date == null) ? 0 : date.hashCode());
 		result = prime * result + id;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Transaction other = (Transaction) obj;
 		if (Double.doubleToLongBits(amount) != Double
 				.doubleToLongBits(other.amount)) {
 			return false;
 		}
 		if (category.equals(other.category)) {
 			return false;
 		} else if (!(category.equals(other.category))) {
 			return false;
 		}
 		if (date == null) {
 			if (other.date != null) {
 				return false;
 			}
 		} else if (!date.equals(other.date)) {
 			return false;
 		}
 		if (id != other.id) {
 			return false;
 		}
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		return true;
 	}
 
 }
