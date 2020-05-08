 package org.fao.fi.vme.msaccess.tables;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.fao.fi.vme.VmeException;
 import org.fao.fi.vme.domain.InformationSource;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.fao.fi.vme.msaccess.formatter.MeetingDateParser;
 import org.fao.fi.vme.msaccess.mapping.TableDomainMapper;
 
 public class Meetings implements TableDomainMapper {
 
 	private int ID;
 	private String RFB_ID;
 	private int Year_ID;
 	private String Meeting_Date;
 	private String Report_Summary;
 	private String Committee;
 	private String Citation;
 	private String Link_Tagged_File;
 	private String Link_Source;
 
 	public int getID() {
 		return ID;
 	}
 
 	public void setID(int iD) {
 		ID = iD;
 	}
 
 	public String getRFB_ID() {
 		return RFB_ID;
 	}
 
 	public void setRFB_ID(String rFB_ID) {
 		RFB_ID = rFB_ID;
 	}
 
 	public int getYear_ID() {
 		return Year_ID;
 	}
 
 	public void setYear_ID(int year_ID) {
 		Year_ID = year_ID;
 	}
 
 	public String getMeeting_Date() {
 		return Meeting_Date;
 	}
 
 	public void setMeeting_Date(String meeting_Date) {
 		Meeting_Date = meeting_Date;
 	}
 
 	public String getReport_Summary() {
 		return Report_Summary;
 	}
 
 	public void setReport_Summary(String report_Summary) {
 		Report_Summary = report_Summary;
 	}
 
 	public String getCommittee() {
 		return Committee;
 	}
 
 	public void setCommittee(String committee) {
 		Committee = committee;
 	}
 
 	public String getCitation() {
 		return Citation;
 	}
 
 	public void setCitation(String citation) {
 		Citation = citation;
 	}
 
 	public String getLink_Tagged_File() {
 		return Link_Tagged_File;
 	}
 
 	public void setLink_Tagged_File(String link_Tagged_File) {
 		Link_Tagged_File = link_Tagged_File;
 	}
 
 	public String getLink_Source() {
 		return Link_Source;
 	}
 
 	public void setLink_Source(String link_Source) {
 		Link_Source = link_Source;
 	}
 
 	@Override
 	public Object map() {
 		InformationSource is = new InformationSource();
 		// TODO what are the types?
 		is.setSourceType(0);
 		MultiLingualStringUtil u = new MultiLingualStringUtil();
 		is.setCommittee(u.english(this.Committee));
 
 		MeetingDateParser p = new MeetingDateParser(this.Meeting_Date);
 		is.setMeetingStartDate(p.getStart());
 		is.setMeetingEndDate(p.getEnd());
 		is.setId(this.ID);
 
 		is.setReportSummary(u.english(this.getReport_Summary()));
 
 		try {
 			URL url = new URL(this.getLink_Source());
 			is.setUrl(url);
 		} catch (MalformedURLException e) {
 			throw new VmeException(e);
 		}
 
		is.setCitation(u.english(this.getCitation()));
 		return is;
 	}
 }
