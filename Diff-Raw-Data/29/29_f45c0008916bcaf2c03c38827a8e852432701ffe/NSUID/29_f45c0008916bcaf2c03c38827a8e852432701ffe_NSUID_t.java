 /*
 Property List Dictionary - LGPL 3.0 licensed
 Copyright (C) 2012  Yørn de Jong
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 
 File is part of the Property List project.
 Project page on http://plist.sf.net/
 */
 package net.sf.plist;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * A NSUID object is rarely seen in the wild.
  * Therefore it is difficult to come up with a good API for it.
  * PLEASE NOTE THAT MY UNDERSTANDING OF THE NSUID IS LIMITED,
 * AND THEREFORE THIS API COULD TURN OUT TO BE WRONG
 * AND WILL NEED TO BE CHANGED. 
  * 
 * Apple's own Property List Editor (part of XCode)
 * completely ignores the NSUID.
  * By using Apple's QuickLook to convert the Property List to XML code,
  * the NSUID object is converted into a NSDictionary with one entry;
 * the key is always CF$UID and the value is
 * the single byte value contained within the NSUID.
  * 
 * So far I have only seen the NSUID files
 * in the temporary .appdownload files.
  * Mac OS X writes when downloading an app from the Mac App Store.
  * These files have four NSUID values; 0x01, 0x02, 0x03 and 0x04.
  * It is still unclear what these values represent.
 * They could point to objects in the object table
 * but that seems to be unlikely, given the results that would produce.
 * 
 * Because the CF$UID is a Number and
 * QuickLook represents the NSUID as a NSDictionary,
 * it is difficult to choose where NSUID should extend from,
 * {@link NSNumber} or {@link NSDictionary}.
 * Because I can not make a well informed choice
 * we will override from {@link NSObject} for now.
  * 
  * {@link #toMap()} and {@link #toList()} are overridden to return a single entry.
  * The key of the entry in the map is CF$UID.
  */
 public final class NSUID extends NSObject {
 
 	/** Single byte value */
 	private final int cfUid;
 	
 	/**
 	 * Construct a new object with a given CF$UID
 	 * @param cfUid	the CF$UID
 	 */
 	public NSUID(int cfUid) {
 		assert cfUid >= 0;
 		assert cfUid <= 255;
 		this.cfUid = cfUid;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see #toMap()
 	 */
 	@Override
 	public Integer getValue() {
 		return toNumber();
 	}
 	/**
 	 * Return a map which has one entry, which has the key "CF$UID".
 	 * The value of this entry is always {@link NSInteger} and contains the value of this NSUID.
 	 * @return	the map
 	 */
 	@Override
 	public SortedMap<String, NSInteger> toMap() {
 		TreeMap<String, NSInteger> result = new TreeMap<String,NSInteger>();
 		result.put("CF$UID", new NSInteger(cfUid));
 		return Collections.unmodifiableSortedMap(result);
 	}
 	
 	/**
 	 * Return a list which has one item.
 	 * The item is a {@link NSInteger} and contains the value of this NSUID.
 	 * @return	the list
 	 */
 	@Override
 	public List<NSInteger> toList() {
 		return Collections.unmodifiableList(Arrays.asList(new NSInteger[]{new NSInteger(cfUid)}));
 	}
 	
 	/** @see #getCfUid() */
 	@Override
 	public long toLong() {
 		return getCfUid();
 	}
 	
 	/** @see #getCfUid() */
 	@Override
 	public double toDouble() {
 		return getCfUid();
 	}
 
 	/** @see #getCfUid() */
 	@Override
 	public Integer toNumber() {
 		return new Integer(getCfUid());
 	}
 	
 	/**
 	 * Return the CF$UID value
 	 * @return	value of CF$UID
 	 */
 	public int getCfUid() {
 		return cfUid;
 	}
 
 	/**
 	 * This implementation considers the CF$UID to be true if the value of cfUid is bigger than 0.
 	 * The in-the-wild-observed values of the CF$UID have always been bigger than 0.
 	 * @return	truth value of CF$UID
 	 */
 	@Override
 	public boolean isTrue() {
 		return cfUid != 0;
 	}
 
 }
