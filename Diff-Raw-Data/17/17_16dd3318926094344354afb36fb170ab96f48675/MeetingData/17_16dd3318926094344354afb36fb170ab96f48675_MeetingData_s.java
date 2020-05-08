 /**
  * Copyright (c) 2011 Ericsson and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Ericsson Research Canada - Initial API and implementation
  * 
  */
 package org.eclipse.mylyn.reviews.notifications.internal.core;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.mylyn.reviews.notifications.core.IMeetingData;
 
 /**
  * 
  * @author Alvaro Sanchez-Leon
  */
 public class MeetingData implements IMeetingData {
 
 	public static String[] getAttributeTypes() {
 		String[] types = { "Subject", "Location", "Start Time", "End Time" };
 		return types;
 	}
 
 	private String	fCcustomID;
 
 	private String	fSubject;
 
 	private String	fBody;
 
 	private String	fLocation;
 
 	private long	fSartTime;
 
 	private long	fEndTime;
 
 	private int		fSentCounter	= 0;
 
 	public MeetingData(String aCustomId, String aSubject, String aBody, String aLocation, long aStartTimeMilli,
 			long aEndTimeMilli)
 			throws CoreException {
 		fCcustomID = aCustomId;
 		fSubject = aSubject;
 		fBody = aBody;
 		fLocation = aLocation;
 		fSartTime = aStartTimeMilli;
 		fEndTime = aEndTimeMilli;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getCustomID()
 	 */
 	public String getCustomID() {
 		return fCcustomID;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getSubject()
 	 */
 	public String getSubject() {
 		return fSubject;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getBody()
 	 */
 	public String getBody() {
 		return fBody;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getLocation()
 	 */
 	public String getLocation() {
 		return fLocation;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getStartTime()
 	 */
 	public long getStartTime() {
 		return fSartTime;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getEndTime()
 	 */
 	public long getEndTime() {
 		return fEndTime;
 	}
 
	/* (non-Javadoc)
	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getAttributeValues()
	 */
	public String[] getAttributeValues() {
		String[] values = { fSubject, fLocation, Long.toString(fSartTime), Long.toString(fEndTime) };
		return values;
	}

 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#getSentCounter()
 	 */
 	public int getSentCounter() {
 		return fSentCounter;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.notifications.core.IMeetingData#incrementSentCounter()
 	 */
 	public void incrementSentCounter() {
 		fSentCounter++;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return fSubject + ", " + fLocation + ", " + fBody + ", " + fSartTime + ", " + fEndTime;
 	}
 
 }
