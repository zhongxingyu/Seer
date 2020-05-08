 /**
  * @author David S Anderson
  *
  *
  * Copyright (C) 2012 David S Anderson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
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
 package org.dsanderson.morctrailreport.parser;
 
import java.util.Date;

 import org.dsanderson.util.IUserSettingsSource;
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.IReportRetriever;
 import org.dsanderson.xctrailreport.core.ISourceSpecificFactory;
 import org.dsanderson.xctrailreport.core.ISourceSpecificInfoParser;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 
 /**
  * 
  */
 public abstract class MorcFactory implements ISourceSpecificFactory {
 	public static final String SOURCE_NAME = "MORC";
 	public static final String XML_TAG = "morc";
 	private static MorcFactory instance = null;
 
 	private boolean enabled = true;
 
 	private MorcReportRetriever retriever = null;
 	private MorcParser parser = null;
 	private MorcInfoPool pool = null;
 	private RegionManager regions = null;
 	private TrailInfo singleReportInfo;
	private Date singleReportDate;
 
 	/**
 	 * 
 	 */
 	public MorcFactory(IAbstractFactory factory) {
 		assert (instance == null);
 		instance = this;
 		pool = new MorcInfoPool();
 		retriever = new MorcReportRetriever(factory, this);
 		parser = new MorcParser();
 		regions = new RegionManager();
 	}
 
 	public static MorcFactory getInstance() {
 		assert (instance != null);
 		return instance;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.ISourceSpecificFactory#setEnabled(boolean
 	 * )
 	 */
 	@Override
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.ISourceSpecificFactory#getEnabled()
 	 */
 	@Override
 	public boolean getEnabled() {
 		return enabled;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.ISourceSpecificFactory#getSourceName()
 	 */
 	@Override
 	public String getSourceName() {
 		return SOURCE_NAME;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.ISourceSpecificFactory#getDefaultComposeUrl
 	 * ()
 	 */
 	@Override
 	public String getDefaultComposeUrl() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.ISourceSpecificFactory#getDefaultRequestUrl
 	 * ()
 	 */
 	@Override
 	public String getDefaultRequestUrl() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.ISourceSpecificFactory#getReportRetriever
 	 * ()
 	 */
 	@Override
 	public IReportRetriever getReportRetriever() {
 		return retriever;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.xctrailreport.core.ISourceSpecificFactory#
 	 * getSourceSpecificXmlKey()
 	 */
 	@Override
 	public String getSourceSpecificXmlKey() {
 		return XML_TAG;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.xctrailreport.core.ISourceSpecificFactory#
 	 * getSourceSpecificParser()
 	 */
 	@Override
 	public ISourceSpecificInfoParser getSourceSpecificParser() {
 		return parser;
 	}
 
 	public MorcInfoPool getTrailInfoPool() {
 		return pool;
 	}
 
 	public RegionManager getRegionManager() {
 		return regions;
 	}
 
 	/**
 	 * @return
 	 */
 	public IUserSettingsSource getUserSettingsSource() {
 		return null;
 	}
 
 	public void setAllReportsInfo(TrailInfo singleReportInfo) {
 		this.singleReportInfo = singleReportInfo;
 	}
 
 	public TrailInfo getAllReportsInfo() {
 		return singleReportInfo;
 	}
	
	public void setAllReportsDate(Date date) {
		this.singleReportDate = date;
	}
	
	public Date getAllReportsDate() {
		return singleReportDate;
	}
 
 }
