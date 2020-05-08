 package nz.cri.gns.springs.db;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import nz.cri.gns.springs.util.Util;
 import android.util.Log;
 
 import com.j256.ormlite.dao.GenericRawResults;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.field.DataType;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * Measurements of samples collected from geothermal features.
  * @author duncanw
  */
 @DatabaseTable
 public class BiologicalSample extends PersistentObject {
 
 	private static final long serialVersionUID = -7201406858581042942L;
 
 	@DatabaseField(foreign = true)
 	private Survey survey;
 	
 	@DatabaseField private Integer sampleNumber;
 	@DatabaseField private Double temperature;
 	@DatabaseField private Double pH;
 	@DatabaseField private Double orp;
 	@DatabaseField private Double conductivity;
 	@DatabaseField private Double dO;
 	@DatabaseField private Double turbidity;
 	@DatabaseField private Double dnaVolume;
 	@DatabaseField private Double ferrousIronAbs;
 	@DatabaseField private String comments;
 	
 	public Survey getSurvey() {
 		return survey;
 	}
 	public void setSurvey(Survey survey) {
 		this.survey = survey;
 	}
 	public Integer getSampleNumber() {
 		return sampleNumber;
 	}
 	public void setSampleNumber(Integer sampleNumber) {
 		this.sampleNumber = sampleNumber;
 	}
 	public Double getTemperature() {
 		return temperature;
 	}
 	public void setTemperature(Double temperature) {
 		this.temperature = temperature;
 	}
 	public Double getPh() {
 		return pH;
 	}
 	public void setPh(Double pH) {
 		this.pH = pH;
 	}
 	public Double getOrp() {
 		return orp;
 	}
 	public void setOrp(Double orp) {
 		this.orp = orp;
 	}
 	public Double getConductivity() {
 		return conductivity;
 	}
 	public void setConductivity(Double conductivity) {
 		this.conductivity = conductivity;
 	}
 	public Double getDo() {
 		return dO;
 	}
 	public void setDo(Double dO) {
 		this.dO = dO;
 	}
 	public Double getTurbidity() {
 		return turbidity;
 	}
 	public void setTurbidity(Double turbidity) {
 		this.turbidity = turbidity;
 	}
 	public Double getDnaVolume() {
 		return dnaVolume;
 	}
 	public void setDnaVolume(Double dnaVolume) {
 		this.dnaVolume = dnaVolume;
 	}
 	public Double getFerrousIronAbs() {
 		return ferrousIronAbs;
 	}
 	public void setFerrousIronAbs(Double ferrousIronAbs) {
 		this.ferrousIronAbs = ferrousIronAbs;
 	}
 	public String getComments() {
 		return comments;
 	}
 	public void setComments(String comments) {
 		this.comments = comments;
 	}	
 
 	/**
 	 * @return a tab separated string of this BiologicalSample's values. Empty strings are used
 	 *         for null values, numeric values are rounded to 4 decimal places.
 	 */
 	public String toTsvString() {
 		
 		String comms = (comments != null) ? comments.replace("\n", " ") : null;
 		return Util.join("\t", getFormattedSampleNumber(),
 				Util.format(temperature), Util.format(pH), Util.format(orp),
 				Util.format(conductivity), Util.format(dO),
 				Util.format(turbidity), Util.format(dnaVolume),
 				Util.format(ferrousIronAbs), comms);
 	}
 	
 	public static String tsvStringColumns() {
		return Util.join("\t", "SampleNumber", "SampleTemperature", "pH", "OxidationReductionPotential",
				"Conductivity", "DissolvedOxygen", "Turbidity", "DnaVolume", "FerrousIronAbs", "Comments");		
 	}
 	
 	/**
 	 * @return this BiologicalSample's sample number, e.g "P1.0023"
 	 */
 	public String getFormattedSampleNumber() {
 		return formatSampleNumber(getSampleNumber());
 	}
 	
 	/**
 	 * @param sampleNumber
 	 * @return the given number in padded to four digits with leading zeros and prefixed with "P1.".
 	 *         e.g 23 converts to "P1.0023".
 	 */
 	public static String formatSampleNumber(Integer sampleNumber) {
 		return "P1." + String.format("%04d", sampleNumber);
 	}
 	
 	/**
 	 * @param dbHelper
 	 * @return the highest sample number of any BiologicalSample in the database.
 	 */
 	public static Integer getMaxSampleNumber(SpringsDbHelper dbHelper) {
 		RuntimeExceptionDao<BiologicalSample, Long> dao = dbHelper.getBiologicalSampleDao();
 		try {
 			return (int)dao.queryRawValue("select max(sampleNumber) from BiologicalSample");
 		} catch (Exception e) {
 			Log.e(BiologicalSample.class.getSimpleName(), "Error retrieving max sampleNumber", e);
 			return 0;
 		}
 	}
 
 	public static class CurrentSample {
 		
 		public Long sampleId;
 		public Integer sampleNumber;
 		public String featureName;
 		public Long surveyDate;
 		public Long featureId;
 		public Long surveyId;
 	}
 	
 	/**
 	 * @param dbHelper
 	 * @return the details of all BiologicalSamples with status NEW or UPDATED.
 	 */
 	public static List<CurrentSample> getCurrentSamples(SpringsDbHelper dbHelper) {
 		RuntimeExceptionDao<BiologicalSample, Long> dao = dbHelper.getBiologicalSampleDao();
 		String query = 
 				"select samp.id, samp.sampleNumber, feat.featureName, surv.surveyDate, feat.id, surv.id " + 
 				"from BiologicalSample samp " +
 				"left join Survey surv on samp.survey_id = surv.id " + 
 			    "left join Feature feat on surv.feature_id = feat.id " +
 				"where samp.status in (?, ?)";
 		
 		DataType[] columnTypes = {DataType.LONG, DataType.INTEGER, DataType.STRING, DataType.LONG, DataType.LONG, DataType.LONG};
 		
 		GenericRawResults<Object[]> results = dao.queryRaw(query, columnTypes, Status.NEW.name(), Status.UPDATED.name());
 		try {
 			List<Object[]> rows = results.getResults();
 			List<CurrentSample> sampleList = new ArrayList<CurrentSample>(rows.size());
 			for (Object[] row : rows) {
 				CurrentSample sample = new CurrentSample();
 				sample.sampleId = (Long)row[0];
 				sample.sampleNumber = (Integer)row[1];
 				sample.featureName = (String)row[2];
 				sample.surveyDate = (Long)row[3];
 				sample.featureId = (Long)row[4];
 				sample.surveyId = (Long)row[5];
 				sampleList.add(sample);
 			}
 			
 			return sampleList;
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
