 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
 
 package net.sf.jmoney.reconciliation;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.VerySimpleDateFormat;
 
 /**
  * @author Nigel
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class BankStatement implements Comparable {
 
 	/**
 	 * Date format used for dates in this file format:
 	 * yyyy.MM.dd
 	 */
 	private static SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
 	static {
 		dateFormat.applyPattern("yyyy.MM.dd");
 	}
 	
 	private int statementNumber = 0;
 	private Date statementDate = null;
 	
 	public BankStatement(String s) {
         if (s.length() != 0) {
             String numbers[] = s.split("\\.");
             if (numbers.length == 1) {
                 statementNumber = Integer.parseInt(numbers[0]);
             } else if (numbers.length == 3) {
                 int year = Integer.parseInt(numbers[0]);
                 int month = Integer.parseInt(numbers[1]);
                 int day = Integer.parseInt(numbers[2]);
                 Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);
                 statementDate = cal.getTime();
             }
         }
     }
 	
 	/**
 	 * @param statementDate
 	 */
 	public BankStatement(Date statementDate) {
 		this.statementNumber = 0;
 		this.statementDate = statementDate;
 	}
 
 	public String toString() {
 		if (statementNumber != 0) {
 			return Integer.toString(statementNumber);
 		} else if (statementDate != null) {
 			return dateFormat.format(statementDate);
 		} else {
 			return "";
 		}
 	}
 	
 	public boolean equals(Object other) {
 		if (other == null)
 			return false;
 		BankStatement otherStatement = (BankStatement)other;
 		return this.statementNumber == otherStatement.statementNumber
 			&& JMoneyPlugin.areEqual(this.statementDate, otherStatement.statementDate);
 	}
 	
 	public int hashCode() {
 		return statementDate == null
 		? statementNumber
 				: statementDate.hashCode();
 	}
 
 	/*
 	 * The Comparable interface does not need to be implemented by all simple
 	 * datastore objects.  However, we do want to be able to sort
 	 * statements into order.
 	 */
 	public int compareTo(Object other) {
 		BankStatement otherStatement = (BankStatement)other;
 		if (this.statementDate == null && otherStatement.statementDate == null) {
 			return this.statementNumber - otherStatement.statementNumber;
 		} else if (this.statementDate != null && otherStatement.statementDate != null) {
 			return this.statementDate.compareTo(otherStatement.statementDate);
 		} else {
 			throw new RuntimeException("mixed statement types!!!");
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public String toLocalizedString() {
 		if (statementDate == null) {
 	        return Integer.toString(statementNumber);
 		} else {
 		    VerySimpleDateFormat fDateFormat = new VerySimpleDateFormat(JMoneyPlugin.getDefault().getDateFormat());
 	        return fDateFormat.format(statementDate);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean isNumber() {
 		return statementDate == null;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getNumber() {
 		JMoneyPlugin.myAssert(isNumber());
 		return statementNumber;
 	}
 
 	/**
 	 * @return
 	 */
 	public Date getStatementDate() {
 		JMoneyPlugin.myAssert(!isNumber());
 		return statementDate;
 	}
 	
 	
 }
