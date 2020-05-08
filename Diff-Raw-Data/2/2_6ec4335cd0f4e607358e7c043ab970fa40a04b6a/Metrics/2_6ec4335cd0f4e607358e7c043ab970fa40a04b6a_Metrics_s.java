 package domain;
 
 /**
  * 
  * @author Jiayuan Song
  *
  */
 public class Metrics {
 	private int metricId;
 	private int metricTypeId;
 	private int metricsWeight;
 	private int policyId;
 	private MetricsType metricsType;
 	/**
 	 * @return the metricId
 	 */
 	public int getMetricId() {
 		return metricId;
 	}
 	/**
 	 * @param metricId the metricId to set
 	 */
 	public void setMetricId(int metricId) {
 		this.metricId = metricId;
 	}
 	/**
 	 * @return the metricTypeId
 	 */
 	public int getMetricTypeId() {
 		return metricTypeId;
 	}
 	/**
 	 * @param metricTypeId the metricTypeId to set
 	 */
 	public void setMetricTypeId(int metricTypeId) {
 		this.metricTypeId = metricTypeId;
 	}
 	/**
 	 * @return the metricsWeight
 	 */
 	public int getMetricsWeight() {
 		return metricsWeight;
 	}
 	/**
 	 * @param metricsWeight the metricsWeight to set
 	 */
 	public void setMetricsWeight(int metricsWeight) {
 		this.metricsWeight = metricsWeight;
 	}
 	/**
 	 * @return the policyId
 	 */
 	public int getPolicyId() {
 		return policyId;
 	}
 	/**
 	 * @param policyId the policyId to set
 	 */
 	public void setPolicyId(int policyId) {
 		this.policyId = policyId;
 	}
 	
 	public MetricsType getMetricsType(){
		return metricsType;
 	}
 	
 	public void setMetricsType(MetricsType metricsType){
 		this.metricsType = metricsType;
 	}
 	
 }
