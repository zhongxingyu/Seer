 /*
 * Copyright (c) 2013 Danil W. Crompton <info+straw@specialbrands.net>
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 /**
  * 
  */
 package net.specialbrands.strawberry;
 
 public class OpeningHours {
 	/**
 	 * 
 	 */
 	private String[] open;
 	private String[] close;
 	private long secondsOpen;
 
 	/**
 	 * @param _open
 	 * @param _close
 	 * @param businessHourCalculator
 	 *            TODO
 	 */
 	public OpeningHours(String _open, String _close) {
 		setOpen(_open.split(":"));
 		setClose(_close.split(":"));
 		secondsOpen = new Long(
 				((new Integer(close[0]) * 60 * 60) + (new Integer(close[1]) * 60))
 						- ((new Integer(open[0]) * 60 * 60) + (new Integer(
 								open[1]) * 60)));
 	}
 
 	/**
 	 * @return the close
 	 */
 	public String[] getClose() {
 		return close;
 	}
 
 	/**
 	 * @param close
 	 *            the close to set
 	 */
 	public void setClose(String[] close) {
 		this.close = close;
 	}
 
 	/**
 	 * @return the open
 	 */
 	public String[] getOpen() {
 		return open;
 	}
 
 	/**
 	 * @param open
 	 *            the open to set
 	 */
 	public void setOpen(String[] open) {
 		this.open = open;
 	}
 }
