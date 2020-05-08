 package com.mpower.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.apache.commons.collections.FactoryUtils;
 import org.apache.commons.collections.list.LazyList;
 import org.hibernate.annotations.IndexColumn;
 
 @Entity
 @Table(name = "REPORTCROSSTABFIELDS")
 public class ReportCrossTabFields implements java.io.Serializable {
 	
 	public ReportCrossTabFields() {
 		//this.reportCrossTabMeasure = -1;
 		this.reportCrossTabOperation = new String();
 		reportCrossTabMeasure = LazyList.decorate(new ArrayList<ReportCrossTabMeasure>(),FactoryUtils.instantiateFactory(ReportCrossTabMeasure.class));
 		reportCrossTabRows = LazyList.decorate(new ArrayList<ReportCrossTabRow>(),FactoryUtils.instantiateFactory(ReportCrossTabRow.class));
 		reportCrossTabColumns = LazyList.decorate(new ArrayList<ReportCrossTabColumn>(),FactoryUtils.instantiateFactory(ReportCrossTabColumn.class));
 	}
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1714845278538209085L;
 
 	//private long reportCrossTabMeasure;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "REPORTCROSSTABFIELDS_ID")
 	private long id;
 	
 	@Column(name = "OPERATION")
 	private String reportCrossTabOperation;
 	
 	@OneToMany(cascade = CascadeType.ALL)
 	@IndexColumn(name="REPORTGROUPBYFIELD_ID")	
 	private List<ReportCrossTabMeasure> reportCrossTabMeasure;
 	
 	@OneToMany(cascade = CascadeType.ALL)
 	@IndexColumn(name="REPORTGROUPBYFIELD_ID")	
 	private List<ReportCrossTabRow> reportCrossTabRows;
 	
 	@OneToMany(cascade = CascadeType.ALL)
 	@IndexColumn(name="REPORTGROUPBYFIELD_ID")	
 	private List<ReportCrossTabColumn> reportCrossTabColumns;
 	
 /*
 	public long getReportCrossTabMeasure() {
 		return reportCrossTabMeasure;
 	}
 
 	public void setReportCrossTabMeasure(long value) {
 		this.reportCrossTabMeasure = value;
 	}
 */
 	public List<ReportCrossTabMeasure> getReportCrossTabMeasure() {
 		return reportCrossTabMeasure; 
 	}
 
 	public void setReportCrossTabMeasure(List<ReportCrossTabMeasure> value) {
 		this.reportCrossTabMeasure = value;
 	}
 	
 	public String getReportCrossTabOperation() {
 		return reportCrossTabOperation;
 	}
 
 	public void setReportCrossTabOperation(String value) {
 		this.reportCrossTabOperation = value;
 	}
 	
 	public List<ReportCrossTabRow> getReportCrossTabRows() {
 		return reportCrossTabRows;
 	}
 
 	public void setReportCrossTabRows(List<ReportCrossTabRow> value) {
 		this.reportCrossTabRows = value;
 	}
 	
 	public List<ReportCrossTabColumn> getReportCrossTabColumns() {
 		return reportCrossTabColumns;
 	}
 
 	public void setReportCrossTabColumns(List<ReportCrossTabColumn> value) {
 		this.reportCrossTabColumns = value;
 	}
 	
 	
 }
