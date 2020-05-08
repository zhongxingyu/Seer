 package nz.cri.gns.springs.db;
 
 
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import nz.cri.gns.springs.util.Util;
 
 import android.text.format.Time;
 
 import com.j256.ormlite.dao.GenericRawResults;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * A Survey captures observations of a geothermal feature at a single date and time.
  * @author duncanw
  */
 @DatabaseTable
 public class Survey extends PersistentObject {
 
 	private static final long serialVersionUID = 8672919678197291499L;
 
 	@DatabaseField(foreign = true)
 	private Feature feature;
 	
 	@DatabaseField private Long surveyDate;
 	@DatabaseField private String size; // e.g "4 x 3 metres"
 	
 	// colour is the red-green-blue value, e.g white is 0xffffff
 	@DatabaseField private Integer colour;
 	@DatabaseField private String ebullition;
 	@DatabaseField private Double temperature;
 	
 	// observer is the full name of the lead scientist on the survey trip
 	@DatabaseField private String observer;
 	
 	public Feature getFeature() {
 		return feature;
 	}
 	public void setFeature(Feature feature) {
 		this.feature = feature;
 	}
 	public Long getSurveyDate() {
 		return surveyDate;
 	}
 	public void setSurveyDate(Long surveyDate) {
 		this.surveyDate = surveyDate;
 	}
 	public String getSize() {
 		return size;
 	}
 	public void setSize(String size) {
 		this.size = size;
 	}
 	public Integer getColour() {
 		return colour;
 	}
 	public void setColour(Integer colour) {
 		this.colour = colour;
 	}
 	public String getEbullition() {
 		return ebullition;
 	}
 	public void setEbullition(String ebullition) {
 		this.ebullition = ebullition;
 	}
 	public Double getTemperature() {
 		return temperature;
 	}
 	public void setTemperature(Double temperature) {
 		this.temperature = temperature;
 	}
 	public String getObserver() {
 		return observer;
 	}
 	public void setObserver(String observer) {
 		this.observer = observer;
 	}
 	
 	/**
 	 * @return a tab-delimited string of this Survey's data. The date/time is in yyyy-mm-dd hh:mm:ss format, aligned to 
 	 *         the tablet's timezone. Numeric values are to two decimal places.
 	 */
 	public String toTsvString() {
 		String date = null;
 		if (surveyDate != null) {
 	        Time now = new Time(Time.getCurrentTimezone());
 	       	now.set(surveyDate);
 	        date = now.format("%Y-%m-%d %H:%M:%S");			
 		}
 		
 		String hexColour = (colour != null) ? Integer.toHexString(colour) : null;
 
 		return Util.join("\t", date, size, hexColour, ebullition, Util.format(temperature), observer);
 	}
 	
 	public static String tsvStringColumns() {
		return Util.join("\t", "SurveyDate", "FeatureSize", "ColourRgbHex", "Ebullition", "Temperature", "LeadObserverName");
 	}
 	
 	public static List<String> getObservers(SpringsDbHelper helper) {
 		
 		RuntimeExceptionDao<Survey, Long> surveyDao = helper.getSurveyDao();
 		List<String> names = new LinkedList<String>();
 		try {
 			GenericRawResults<String[]> rawResults =
 					surveyDao.queryRaw("select distinct observer from Survey where observer is not null");
 			List<String[]> rows = rawResults.getResults();			
 			for (String[] resultColumns : rows) {
 			    names.add(resultColumns[0]);
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		
 		return names;
 	}
 	
 
 }
