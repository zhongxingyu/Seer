 package edu.northwestern.bioinformatics.studycalendar.web.reporting;
 
 import java.util.List;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
 
 /*
  * @author Yufang Wang
  * @author Jaron Sampson 
  */
 
 public class ReportBuilderCommand {
 	private String startDate;
 	private String endDate;
	private Boolean excelFormat;
 	private List<Study> studiesFilter;
 	private List<Site> sitesFilter;
 	private List<Participant> participantsFilter;
 
     public String getStartDate() {
         return startDate;
     }
 
     public void setStartDate(String startDate) {
         this.startDate = startDate;
     }
 	
     public String getEndDate() {
         return endDate;
     }
 
     public void setEndDate(String endDate) {
         this.endDate = endDate;
     }
     
 	public List<Site> getSitesFilter() {
 		return sitesFilter;
 	}
 
 	public void setSitesFilter(List<Site> siteFilter) {
 		this.sitesFilter = siteFilter;
 	}
 
 	public List<Participant> getParticipantsFilter() {
 		return participantsFilter;
 	}
 
 	public void setParticipantsFilter(List<Participant> participantsFilter) {
 		this.participantsFilter = participantsFilter;
 	}
 
 	public List<Study> getStudiesFilter() {
 		return studiesFilter;
 	}
 
 	public void setStudiesFilter(List<Study> studiesFilter) {
 		this.studiesFilter = studiesFilter;
 	}

	public Boolean getExcelFormat() {
		return excelFormat;
	}

	public void setExcelFormat(Boolean excelFormat) {
		this.excelFormat = excelFormat;
	}
 	
 
 }
