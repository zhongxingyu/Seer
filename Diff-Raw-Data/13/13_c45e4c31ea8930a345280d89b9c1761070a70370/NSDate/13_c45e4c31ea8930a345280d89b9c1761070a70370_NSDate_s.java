 /*
  Property List Date - LGPL licensed
 Copyright (C) 2011  Yørn de Jong
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 
 This file was obtained from http://plist.sf.net/
 */
 package net.sf.plist;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * <p>Represents a date and time.</p>
  * 
  * <p>In this implementation, a {@link Date} is used to represent the {@link NSDate}.</p>
  * @see Date
  */
 public class NSDate extends NSObject {
 
	/** This is the format used for dates in PList files. */
	public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 	
 	private final Date theDate;
 	
 	/**
 	 * Constructor.
 	 * @param theDate value of the new object
 	 */
 	public NSDate(Date theDate) {
 		this.theDate = theDate;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see #date()
 	 */
 	@Override
 	public Date getValue() {
 		return date();
 	}
 	/**
 	 * Get the {@link Date} represented by this object.
 	 * @return the {@link Date}
 	 */
 	public Date date() {
 		return new Date(time());
 	}
 	/**
 	 * @see Date#getTime()
 	 */
 	public long time() {
 		return theDate.getTime();
 	}
 
 }
