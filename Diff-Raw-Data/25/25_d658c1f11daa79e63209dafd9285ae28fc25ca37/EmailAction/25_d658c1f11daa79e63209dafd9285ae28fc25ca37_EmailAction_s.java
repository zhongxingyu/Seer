 /**
  * Copyright (C) 2009 Mads Mohr Christensen, <hr.mohr@gmail.com>
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
 package dk.cubing.liveresults.action.admin;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import dk.cubing.liveresults.utilities.CsvConverter;
 import org.apache.commons.mail.SimpleEmail;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.annotation.Secured;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.opensymphony.xwork2.Action;
 
 import dk.cubing.liveresults.action.FrontendAction;
 import dk.cubing.liveresults.model.Competition;
 import dk.cubing.liveresults.service.CompetitionService;
 
 @Secured( { "ROLE_USER" })
 public class EmailAction extends FrontendAction {
 
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger log = LoggerFactory.getLogger(EmailAction.class);
 	
 	private CompetitionService competitionService;
     private final CsvConverter csvConverter;
 	
 	private File csv;
 	private String csvContentType;
 	private String csvFileName;
     private boolean csvConvert = false;
     private List<String> csvFileEncodings = new ArrayList<String>();
     private String csvFileEncoding = "ISO-8859-1";
 	
 	private List<Competition> competitions;
 	private String competitionId;
 	private Competition competition;
 	
 	private List<String> acceptedCompetitors;
 	private List<String> pendingCompetitors;
 	private boolean sendToAccepted = true;
 	private boolean sendToPending = false;
 	private String subject;
 	private String body;
 	
 	public EmailAction() {
 		this.acceptedCompetitors = new ArrayList<String>();
 		this.pendingCompetitors = new ArrayList<String>();
         this.csvConverter = new CsvConverter();
 	}
 	
 	/**
 	 * @param competitionService the competitionService to set
 	 */
 	public void setCompetitionService(CompetitionService competitionService) {
 		this.competitionService = competitionService;
 	}
 
 	/**
 	 * @return the competitionService
 	 */
 	public CompetitionService getCompetitionService() {
 		return competitionService;
 	}
 	
 	/**
 	 * @return the csv
 	 */
 	public File getCsv() {
 		return csv;
 	}
 
 	/**
 	 * @param csv the csv to set
 	 */
 	public void setCsv(File csv) {
 		this.csv = csv;
 	}
 
 	/**
 	 * @return the csvContentType
 	 */
 	public String getCsvContentType() {
 		return csvContentType;
 	}
 
 	/**
 	 * @param csvContentType the csvContentType to set
 	 */
 	public void setCsvContentType(String csvContentType) {
 		this.csvContentType = csvContentType;
 	}
 
 	/**
 	 * @return the csvFileName
 	 */
 	public String getCsvFileName() {
 		return csvFileName;
 	}
 
 	/**
 	 * @param csvFileName the csvFileName to set
 	 */
 	public void setCsvFileName(String csvFileName) {
 		this.csvFileName = csvFileName;
 	}
 
     /**
      * @return
      */
     public String getCsvFileEncoding() {
         return csvFileEncoding;
     }
 
     /**
      * @param csvFileEncoding
      */
     public void setCsvFileEncoding(String csvFileEncoding) {
         this.csvFileEncoding = csvFileEncoding;
     }
 
     /**
      * @return
      */
     public List<String> getCsvFileEncodings() {
         return csvFileEncodings;
     }
 
     /**
      * @return
      */
     public boolean isCsvConvert() {
         return csvConvert;
     }
 
     /**
      * @param csvConvert
      */
     public void setCsvConvert(boolean csvConvert) {
         this.csvConvert = csvConvert;
     }
 
 	/**
 	 * @param competitions the competitions to set
 	 */
 	public void setCompetitions(List<Competition> competitions) {
 		this.competitions = competitions;
 	}
 
 	/**
 	 * @return the competitions
 	 */
 	@Secured( { "ROLE_ADMIN" })
 	public List<Competition> getCompetitions() {
 		return competitions;
 	}
 
 	/**
 	 * @param competitionId the competitionId to set
 	 */
 	public void setCompetitionId(String competitionId) {
 		this.competitionId = competitionId;
 	}
 
 	/**
 	 * @return the competitionId
 	 */
 	public String getCompetitionId() {
 		return competitionId;
 	}
 
 	/**
 	 * @param competition the competition to set
 	 */
 	public void setCompetition(Competition competition) {
 		this.competition = competition;
 	}
 
 	/**
 	 * @return the competition
 	 */
 	public Competition getCompetition() {
 		return competition;
 	}
 	
 	/**
 	 * @return the acceptedCompetitors
 	 */
 	public List<String> getAcceptedCompetitors() {
 		return acceptedCompetitors;
 	}
 
 	/**
 	 * @param acceptedCompetitors the acceptedCompetitors to set
 	 */
 	public void setAcceptedCompetitors(List<String> acceptedCompetitors) {
 		this.acceptedCompetitors = acceptedCompetitors;
 	}
 
 	/**
 	 * @return the pendingCompetitors
 	 */
 	public List<String> getPendingCompetitors() {
 		return pendingCompetitors;
 	}
 
 	/**
 	 * @param pendingCompetitors the pendingCompetitors to set
 	 */
 	public void setPendingCompetitors(List<String> pendingCompetitors) {
 		this.pendingCompetitors = pendingCompetitors;
 	}
 	
 	/**
 	 * @return the sendToAccepted
 	 */
 	public boolean isSendToAccepted() {
 		return sendToAccepted;
 	}
 
 	/**
 	 * @param sendToAccepted the sendToAccepted to set
 	 */
 	public void setSendToAccepted(boolean sendToAccepted) {
 		this.sendToAccepted = sendToAccepted;
 	}
 
 	/**
 	 * @return the sendToPending
 	 */
 	public boolean isSendToPending() {
 		return sendToPending;
 	}
 
 	/**
 	 * @param sendToPending the sendToPending to set
 	 */
 	public void setSendToPending(boolean sendToPending) {
 		this.sendToPending = sendToPending;
 	}
 
 	/**
 	 * @return the subject
 	 */
 	public String getSubject() {
 		return subject;
 	}
 
 	/**
 	 * @param subject the subject to set
 	 */
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	/**
 	 * @return the body
 	 */
 	public String getBody() {
 		return body;
 	}
 
 	/**
 	 * @param body the body to set
 	 */
 	public void setBody(String body) {
 		this.body = body;
 	}
 
 	/**
 	 * @return
 	 */
 	public String parseCsv() {
 		if (csv != null && competitionId != null) {
 			Competition competition = getCompetitionService().find(competitionId);
 			if (competition == null) {
 				log.error("Could not load competition: {}", competitionId);
 				return Action.ERROR;
 			} else {
 				setCompetition(competition);
 				log.debug("Loaded competition: {}", competition.getName());
 			}
 			
 			List<String> acceptedCompetitors = new ArrayList<String>();
 			List<String> pendingCompetitors = new ArrayList<String>();
 			
 			// parse csv file
 			try {
 				// convert the CSV file
                 CSVReader reader;
                 if (isCsvConvert()) {
                     StringWriter sw = new StringWriter();
                     csvConverter.compTool2Wca(new InputStreamReader(new FileInputStream(csv), getCsvFileEncoding()), sw);
                     reader = new CSVReader(new StringReader(sw.toString()), ',');
                     setCsvConvert(false);
                 } else {
                     reader = new CSVReader(new InputStreamReader(new FileInputStream(csv), getCsvFileEncoding()), ',');
                 }
 				List<String[]> csvLines = reader.readAll();
 				csvLines.remove(0); // first line contains column definition
 				for (String[] line : csvLines) {
 					String email = line[line.length-3];
 					log.info("Email: {}", email);
 					if ("a".equals(line[0])) { // accepted competitors
 						acceptedCompetitors.add(email);
 					} else { // pending competitors
 						pendingCompetitors.add(email);
 					}
 				}
 				setAcceptedCompetitors(acceptedCompetitors);
 				setPendingCompetitors(pendingCompetitors);
 				setSendToAccepted(true);
 				setSendToPending(false);
                 setBody("");
                 setSubject("");
 				return Action.SUCCESS;
 			} catch (Exception e) {
 				log.error(e.getLocalizedMessage(), e);
 			}
 		}
 		return Action.INPUT;
 	}
 	
 	/**
 	 * @return
 	 */
 	public String sendEmail() {
 		if (getCompetition() != null) {
 			try {
 				SimpleEmail email = new SimpleEmail();
 				email.setCharset(SimpleEmail.ISO_8859_1);
 				email.setHostName(getText("email.smtp.server"));
 				if (!getText("email.username").isEmpty() && !getText("email.password").isEmpty()) {
 					email.setAuthentication(getText("email.username"), getText("email.password"));
 				}
 				email.setSSL("true".equals(getText("email.ssl")));
 				email.setSubject(getSubject());
 				email.setMsg(getBody());
 				email.setFrom(getCompetition().getOrganiserEmail(), getCompetition().getOrganiser());
 				email.addBcc(getCompetition().getWcaDelegateEmail(), getCompetition().getWcaDelegate());
 				if (isSendToAccepted()) {
 					for (String toAddress : getAcceptedCompetitors()) {
 						email.addBcc(toAddress);
 					}
 				}
 				if (isSendToPending()) {
 					for (String toAddress : getPendingCompetitors()) {
 						email.addBcc(toAddress);
 					}
 				}
 				email.send();
 				return Action.SUCCESS;
 			} catch (Exception e) {
 				log.error("Could not send email upon competition creation!", e);
 				return Action.ERROR;
 			}
 		} else {
 			log.error("Could not load competition!");
 			return Action.ERROR;
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.opensymphony.xwork2.ActionSupport#execute()
 	 */
 	public String execute() throws Exception {
 		return Action.SUCCESS;
 	}
 	
 	/* (non-Javadoc)
 	 * @see dk.cubing.liveresults.action.FrontendAction#list()
 	 */
 	@Override
 	public String list() {
 		setCompetitions(getCompetitionService().list(page, size));
 		return Action.SUCCESS;
 	}
 }
