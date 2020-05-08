 package com.jcheed06.myhealthapp.measurement;
 
 public class ECGMeasurement extends Measurement {
 	
 	private Integer printerval;
 	private Integer prsegment;
 	private Integer qrscomplex;
 	private Integer stsegment;
 	private Integer qtinterval;
 	private Integer qtrough;
 	private Integer rpeak;
	private Integer strough	;
 	private Integer tpeak;
 	private Integer ppeak;
 	
 	public ECGMeasurement(){
 		super(null);
 	}
 
 	public ECGMeasurement(String id, Integer printerval, Integer prsegment, Integer qrscomplex, Integer stsegment ,
 								Integer qtinterval, Integer qtrough, Integer rpeak, Integer strough, Integer tpeak,
 								Integer ppeak) {
 		super(id);
 		this.setPrinterval(printerval);
 		this.setPrsegment(prsegment);
 		this.setQrscomplex(qrscomplex);
 		this.setStsegment(stsegment);
 		this.setQtinterval(qtinterval);
 		this.setQtrough(qtrough);
 		this.setRpeak(rpeak);
 		this.setStrough(strough);
 		this.setTpeak(tpeak);
 		this.setPpeak(ppeak);
 		
 	}
 	
 	@Override
 	public String getMeasurementValues() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("PR Interval : " + printerval);
 		builder.append("PR Segment : " + prsegment);
 		builder.append("QRS Complex : " + qrscomplex);
 		builder.append("ST Segment : " + stsegment);
 		builder.append("QT Interval : " + qtinterval);
 		builder.append("Q Trough : " + qtrough);
 		builder.append("R Peak : " + rpeak);
 		builder.append("S Trough : " + strough);
 		builder.append("T Peak : " + tpeak);
 		builder.append("P Peak : " + ppeak);
 		return builder.toString();
 	}
 	
 	public Integer getPpeak() {
 		return ppeak;
 	}
 	public void setPpeak(Integer ppeak) {
 		this.ppeak = ppeak;
 	}
 	public Integer getQtrough() {
 		return qtrough;
 	}
 	public void setQtrough(Integer qtrough) {
 		this.qtrough = qtrough;
 	}
 	public Integer getRpeak() {
 		return rpeak;
 	}
 	public void setRpeak(Integer rpeak) {
 		this.rpeak = rpeak;
 	}
 	public Integer getStrough() {
 		return strough;
 	}
 	public void setStrough(Integer strough) {
 		this.strough = strough;
 	}
 	public Integer getTpeak() {
 		return tpeak;
 	}
 	public void setTpeak(Integer tpeak) {
 		this.tpeak = tpeak;
 	}
 	public Integer getPrinterval() {
 		return printerval;
 	}
 	public void setPrinterval(Integer printerval) {
 		this.printerval = printerval;
 	}
 	public Integer getPrsegment() {
 		return prsegment;
 	}
 	public void setPrsegment(Integer prsegment) {
 		this.prsegment = prsegment;
 	}
 	public Integer getQrscomplex() {
 		return qrscomplex;
 	}
 	public void setQrscomplex(Integer qrscomplex) {
 		this.qrscomplex = qrscomplex;
 	}
 	public Integer getStsegment() {
 		return stsegment;
 	}
 	public void setStsegment(Integer stsegment) {
 		this.stsegment = stsegment;
 	}
 	public Integer getQtinterval() {
 		return qtinterval;
 	}
 	public void setQtinterval(Integer qtinterval) {
 		this.qtinterval = qtinterval;
 	}
 }
