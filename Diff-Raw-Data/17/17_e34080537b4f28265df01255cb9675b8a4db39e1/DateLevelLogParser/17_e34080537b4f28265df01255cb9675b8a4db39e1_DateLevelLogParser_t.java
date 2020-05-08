 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package biz.bidi.archivee.components.listeners.parser;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import biz.bidi.archivee.commons.exceptions.ArchiveeException;
 import biz.bidi.archivee.commons.interfaces.ILogParser;
 import biz.bidi.archivee.commons.model.xml.ParserMessage;
 import biz.bidi.archivee.commons.properties.ArchiveeProperties;
 import biz.bidi.archivee.commons.properties.IArchiveePropertiesLoader;
 import biz.bidi.archivee.commons.utils.ArchiveeDateUtils;
 import biz.bidi.archivee.commons.utils.ArchiveePatternUtils;
 
 /**
  * @author Andrey Bidinotto
  * @email andreymoser@bidi.biz
  * @since Sep 22, 2012
  */
 public class DateLevelLogParser implements ILogParser, IArchiveePropertiesLoader {
 
 	/**
 	 * The default date locale 
 	 */
 	private String dateLocale;
 	/**
 	 * The default simple date format
 	 */
 	private String simpleDateFormat;
 	/**
 	 * The level delimiter values
 	 */
 	private String levelDelimiter;
 	/**
 	 * The level values separed by levelDelimiter 
 	 */
 	private String levelValues; 
 	/**
 	 * The level regex 
 	 */
 	private String levelRegex; 
 
 	public DateLevelLogParser() {
 		super();
 		try {
 			loadProperties(null);
 			
 			loadLevelRegex();
 		} catch (ArchiveeException e) {
 			ArchiveeException.error(e, "Unable to load properties for Date and Level parser.", this);
 		} catch (Exception e) {
 			ArchiveeException.error(e, "Unable to define level regex.", this);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see biz.bidi.archivee.commons.interfaces.ILogParser#parseLogLine(java.lang.String)
 	 */
 	@Override
 	public void parseLog(ParserMessage message) throws ArchiveeException {
 		message.setDate("");
 		message.setLevel("");
 		
 		message.setDate(ArchiveeDateUtils.findDate(message.getMessage(), simpleDateFormat, dateLocale));
 		
 		for(String value : ArchiveePatternUtils.getRegexValues(message.getMessage(), levelRegex)) {
 			message.setLevel(value);
 			break;
 		}
 		
 		if(message.getDate().length() > 0) {
 			message.setMessage(message.getMessage().replace(message.getDate(), ""));
 			
 			Date date = ArchiveeDateUtils.stringToDate(message.getDate(), simpleDateFormat, dateLocale);
 			message.setDate(ArchiveeDateUtils.convertDateToString(date));
 		}
 		message.setMessage(message.getMessage().replaceFirst(levelRegex, ""));
 		message.setMessage(message.getMessage().trim());
 	}
 	
 	/**
 	 * Loads the level regex
 	 * @return
 	 */
 	private void loadLevelRegex() {
 		levelRegex = "";
 		for(String value : levelValues.split(levelDelimiter)) {
 			levelRegex += levelRegex==""?value:("|"+value);
 		}
 	}
 
 	/**
 	 * @return the dateLocale
 	 */
 	public String getDateLocale() {
 		return dateLocale;
 	}
 
 	/**
 	 * @param dateLocale the dateLocale to set
 	 */
 	public void setDateLocale(String dateLocale) {
 		this.dateLocale = dateLocale;
 	}
 
 	/**
 	 * @return the simpleDateFormat
 	 */
 	public String getSimpleDateFormat() {
 		return simpleDateFormat;
 	}
 
 	/**
 	 * @param simpleDateFormat the simpleDateFormat to set
 	 */
 	public void setSimpleDateFormat(String simpleDateFormat) {
 		this.simpleDateFormat = simpleDateFormat;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see biz.bidi.archivee.commons.properties.IArchiveePropertiesLoader#loadProperties(java.lang.String)
 	 */
 	@Override
 	public void loadProperties(String prefixKey) throws ArchiveeException {
 		ArchiveeProperties.loadProperties(this);
 	}
 
 	/**
 	 * @return the levelDelimiter
 	 */
 	public String getLevelDelimiter() {
 		return levelDelimiter;
 	}
 
 	/**
 	 * @param levelDelimiter the levelDelimiter to set
 	 */
 	public void setLevelDelimiter(String levelDelimiter) {
 		this.levelDelimiter = levelDelimiter;
 	}
 
 	/**
 	 * @return the levelValues
 	 */
 	public String getLevelValues() {
 		return levelValues;
 	}
 
 	/**
 	 * @param levelValues the levelValues to set
 	 */
 	public void setLevelValues(String levelValues) {
 		this.levelValues = levelValues;
 	}
 	
 	/**
 	 * Returns the levels list "INFO,WARN,etc.."
 	 * @return
 	 * @throws ArchiveeException
	 * @deprecated
 	 */
 	public static ArrayList<String> getLevelsList() throws ArchiveeException {
		//TODO remove this method from this class, listener component not in EJB
 		ArrayList<String> levels = new ArrayList<String>();
 		
 		DateLevelLogParser dateLevelLogParser = new DateLevelLogParser();
 		dateLevelLogParser.loadProperties(null);
 		
 		if(dateLevelLogParser.getLevelValues() != null) {
 			for(String level : dateLevelLogParser.getLevelValues().split(dateLevelLogParser.getLevelDelimiter())) {
 				levels.add(level);
 			}
 		}
 		
 		return levels;
 	}
 
 }
