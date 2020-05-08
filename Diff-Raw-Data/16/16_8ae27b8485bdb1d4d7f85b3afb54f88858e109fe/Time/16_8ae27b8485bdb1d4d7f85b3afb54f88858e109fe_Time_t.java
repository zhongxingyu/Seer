 package gov.usgs.cida.gdp.dataaccess.bean;
 
 import com.thoughtworks.xstream.XStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 import com.thoughtworks.xstream.io.xml.QNameMap;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.LoggerFactory;
 
 @XStreamAlias("availabletimes")
 public class Time extends Response {
 
 	private static final long serialVersionUID = 82376486L;
 	private static final transient org.slf4j.Logger log = LoggerFactory.getLogger(Time.class);
 	@XStreamAlias("times")
 	@XStreamImplicit(itemFieldName = "time")
 	private String[] time;
 	private TimeBreakdown starttime;
 	private TimeBreakdown endtime;
 
 	public Time() {
 		this.time = new String[2];
 		this.starttime = new TimeBreakdown();
 		this.endtime = new TimeBreakdown();
 	}
 
 	public Time(String[] dateRange) throws ParseException {
 		this.time = dateRange;
 
 		if (this.time.length == 0) {
 			this.starttime = new TimeBreakdown();
 			this.endtime = new TimeBreakdown();
 		} else {
 			this.starttime = new TimeBreakdown(dateRange[0]);
 			this.endtime = new TimeBreakdown(dateRange[1]);
 		}
 	}
 
 	public void setTime(String[] time) {
 		this.time = time;
 	}
 
 	public String[] getTime() {
 		return time;
 	}
 
 	public void setStarttime(TimeBreakdown starttime) {
 		this.starttime = starttime;
 	}
 
 	public TimeBreakdown getStarttime() {
 		return starttime;
 	}
 
 	public void setEndtime(TimeBreakdown endtime) {
 		this.endtime = endtime;
 	}
 
 	public TimeBreakdown getEndtime() {
 		return endtime;
 	}
 
 	@Override
 	public String toXML() {
 		String result;
 		QNameMap qmap = new QNameMap();
 		qmap.setDefaultNamespace("xsd/gdptime-1.0.xsd");
 		qmap.setDefaultPrefix("gdp");
 		StaxDriver sd = new StaxDriver(qmap);
 		XStream xstream = new XStream(sd);
 		xstream.autodetectAnnotations(true);
 		result = xstream.toXML(this);
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return this.time[0] + "|" + this.time[1];
 	}
 
 	public static Time buildFromCache(ResponseCache.CacheIdentifier ci) {
 		Time result;
 		FileInputStream fileIn = null;
 		ObjectInputStream in = null;
 		File cacheFile;
 		try {
 			cacheFile = ci.getFile();
 		} catch (IOException ex) {
 			log.warn("Could not create new cache file", ex);
 			return null;
 		}
 
 		try {
 			try {
 				fileIn = new FileInputStream(cacheFile);
 			} catch (FileNotFoundException ex) {
 				log.warn("Could not find cache file {}", cacheFile.getPath(), ex);
 				return null;
 			}
 
 			try {
 				in = new ObjectInputStream(fileIn);
 			} catch (IOException ex) {
 				log.warn("Could not read cache file {}", cacheFile.getPath(), ex);
 				return null;
 			}
 
 			try {
 				result = (Time) in.readObject();
 			} catch (IOException ex) {
 				log.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
 				return null;
 			} catch (ClassNotFoundException ex) {
 				log.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
 				return null;
 			}
 		} finally {
 			IOUtils.closeQuietly(in);
 			IOUtils.closeQuietly(fileIn);
 		}
 		return result;
 	}
 
 	/**
 	 *
 	 * @param ci
 	 * @return
 	 */
 	@Override
 	public boolean writeToCache(ResponseCache.CacheIdentifier ci) {
 		FileOutputStream filOutputStream = null;
 		ObjectOutputStream objOutputStream = null;
 		File cacheFile;
 		try {
 			cacheFile = ci.getFile();
 		} catch (IOException ex) {
 			log.warn("Could not create new cache file", ex);
 			return false;
 		}
 		try {
 			try {
 				cacheFile.createNewFile();
 				filOutputStream = new FileOutputStream(cacheFile);
 			} catch (FileNotFoundException ex) {
 				log.warn("Could not open output stream to file {}", cacheFile.getPath(), ex);
 				return false;
 			} catch (IOException ex) {
 				log.warn("Could notcreate new cache file {}", cacheFile.getPath(), ex);
 				return false;
 			}
 
 			try {
 				objOutputStream = new ObjectOutputStream(filOutputStream);
 			} catch (IOException ex) {
 				log.warn("Could not open object output stream to file {}", cacheFile.getPath(), ex);
 				return false;
 			}
 			try {
 				objOutputStream.writeObject(this);
 			} catch (IOException ex) {
 				log.warn("Could not write object output to output stream", ex);
 				return false;
 			}
 			return true;
 		} finally {
 			IOUtils.closeQuietly(filOutputStream);
 			IOUtils.closeQuietly(objOutputStream);
 		}
 	}
 
 	static class TimeBreakdown extends Response {
 
 		private static final long serialVersionUID = 2423423L;
 		private static final transient org.slf4j.Logger tbLog = LoggerFactory.getLogger(TimeBreakdown.class);
 		private int month;
 		private int day;
 		private int year;
 		private int timezone;
 		private int hour;
 		private int minute;
 		private int second;
 
 		public TimeBreakdown() {
 		}
 
 		public TimeBreakdown(String dateInstance) throws ParseException {
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // HACK can we use JODA?
 			Date dateInstanceDate = null;
 			try {
 				dateInstanceDate = sdf.parse(dateInstance);
 			} catch (ParseException pe) {
 				throw pe;
 			}
 
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(dateInstanceDate);
 			this.month = cal.get(Calendar.MONTH) + 1;
 			this.day = cal.get(Calendar.DAY_OF_MONTH);
 			this.year = cal.get(Calendar.YEAR);
 			this.hour = cal.get(Calendar.HOUR_OF_DAY);
 			this.minute = cal.get(Calendar.MINUTE);
 			this.second = cal.get(Calendar.SECOND);
 
 		}
 
 		public TimeBreakdown(Calendar cal) {
 			this.month = cal.get(Calendar.MONTH) + 1;
 			this.day = cal.get(Calendar.DAY_OF_MONTH);
 			this.year = cal.get(Calendar.YEAR);
 			this.hour = cal.get(Calendar.HOUR_OF_DAY);
 			this.minute = cal.get(Calendar.MINUTE);
 			this.second = cal.get(Calendar.SECOND);
 		}
 
 		public int getMonth() {
 			return month;
 		}
 
 		public void setMonth(int month) {
 			this.month = month;
 		}
 
 		public int getDay() {
 			return day;
 		}
 
 		public void setDay(int day) {
 			this.day = day;
 		}
 
 		public int getYear() {
 			return year;
 		}
 
 		public void setYear(int year) {
 			this.year = year;
 		}
 
 		public int getTimezone() {
 			return timezone;
 		}
 
 		public void setTimezone(int timezone) {
 			this.timezone = timezone;
 		}
 
 		public int getHour() {
 			return hour;
 		}
 
 		public void setHour(int hour) {
 			this.hour = hour;
 		}
 
 		public int getMinute() {
 			return minute;
 		}
 
 		public void setMinute(int minute) {
 			this.minute = minute;
 		}
 
 		public int getSecond() {
 			return second;
 		}
 
 		public void setSecond(int second) {
 			this.second = second;
 		}
 
 		public static TimeBreakdown buildFromCache(ResponseCache.CacheIdentifier ci) {
 			TimeBreakdown result;
 			FileInputStream fileIn = null;
 			ObjectInputStream in = null;
 			File cacheFile;
 			try {
 				cacheFile = ci.getFile();
 			} catch (IOException ex) {
 				log.warn("Could not create new cache file", ex);
 				return null;
 			}
 			
 			try {
 				try {
 					fileIn = new FileInputStream(cacheFile);
 				} catch (FileNotFoundException ex) {
 					tbLog.warn("Could not find cache file {}", cacheFile.getPath(), ex);
 					return null;
 				}
 
 				try {
 					in = new ObjectInputStream(fileIn);
 				} catch (IOException ex) {
 					tbLog.warn("Could not read cache file {}", cacheFile.getPath(), ex);
 					return null;
 				}
 
 				try {
 					result = (TimeBreakdown) in.readObject();
 				} catch (IOException ex) {
 					tbLog.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
 					return null;
 				} catch (ClassNotFoundException ex) {
 					tbLog.warn("Could not inflate cache file {}", cacheFile.getPath(), ex);
 					return null;
 				}
 			} finally {
 				IOUtils.closeQuietly(in);
 				IOUtils.closeQuietly(fileIn);
 			}
 			return result;
 		}
 
 		/**
 		 *
 		 * @param ci
 		 * @return
 		 */
 		@Override
 		public boolean writeToCache(ResponseCache.CacheIdentifier ci) {
 			FileOutputStream filOutputStream = null;
 			ObjectOutputStream objOutputStream = null;
 			File cacheFile;
 			try {
 				cacheFile = ci.getFile();
 			} catch (IOException ex) {
 				log.warn("Could not create new cache file", ex);
 				return false;
 			}
 			try {
 				try {
 					cacheFile.createNewFile();
 					filOutputStream = new FileOutputStream(cacheFile);
 				} catch (FileNotFoundException ex) {
 					log.warn("Could not open output stream to file {}", cacheFile.getPath(), ex);
 					return false;
 				} catch (IOException ex) {
 					log.warn("Could notcreate new cache file {}", cacheFile.getPath(), ex);
 					return false;
 				}
 
 				try {
 					objOutputStream = new ObjectOutputStream(filOutputStream);
 				} catch (IOException ex) {
 					log.warn("Could not open object output stream to file {}", cacheFile.getPath(), ex);
 					return false;
 				}
 				try {
 					objOutputStream.writeObject(this);
 				} catch (IOException ex) {
 					log.warn("Could not write object output to output stream", ex);
 					return false;
 				}
 				return true;
 			} finally {
 				IOUtils.closeQuietly(filOutputStream);
 				IOUtils.closeQuietly(objOutputStream);
 			}
 		}
 	}
 }
