 package is.idega.idegaweb.pheidippides.output;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.business.RegistrationStatus;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.Participant;
 import is.idega.idegaweb.pheidippides.data.Race;
 import is.idega.idegaweb.pheidippides.data.Registration;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.rmi.RemoteException;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFFont;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.core.file.util.MimeTypeUtil;
 import com.idega.core.location.data.Country;
 import com.idega.core.location.data.CountryHome;
 import com.idega.data.IDOLookup;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.io.DownloadWriter;
 import com.idega.io.MediaWritable;
 import com.idega.io.MemoryFileBuffer;
 import com.idega.io.MemoryInputStream;
 import com.idega.io.MemoryOutputStream;
 import com.idega.presentation.IWContext;
 import com.idega.util.IWTimestamp;
 import com.idega.util.StringHandler;
 import com.idega.util.expression.ELUtil;
 
 public class ParticipantsWriter extends DownloadWriter implements MediaWritable {
 
 	private static final String PARAMETER_RACE_PK = "prm_race_pk";
 
 	private MemoryFileBuffer buffer = null;
 	private Locale locale;
 	private IWResourceBundle iwrb;
 
 	@Autowired
 	private PheidippidesService service;
 	
 	@Autowired
 	private PheidippidesDao dao;
 	
 	public void init(HttpServletRequest req, IWContext iwc) {
 		this.locale = iwc.getCurrentLocale();
 		this.iwrb = iwc.getIWMainApplication().getBundle(PheidippidesConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(this.locale);
 
 		Race race = getDao().getRace(Long.parseLong(iwc.getParameter(PARAMETER_RACE_PK)));
 		List<Registration> registrations = getDao().getRegistrations(race, RegistrationStatus.OK);
 		Map<Registration, Participant> participantsMap = getService().getParticantMap(registrations);
 
 		try {
 			this.buffer = writeXLS(iwc, race, registrations, participantsMap);
 			setAsDownload(iwc, "participants.xls", this.buffer.length());
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getMimeType() {
 		if (this.buffer != null) {
 			return this.buffer.getMimeType();
 		}
 		return super.getMimeType();
 	}
 
 	public void writeTo(OutputStream out) throws IOException {
 		if (this.buffer != null) {
 			MemoryInputStream mis = new MemoryInputStream(this.buffer);
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			while (mis.available() > 0) {
 				baos.write(mis.read());
 			}
 			baos.writeTo(out);
 		}
 		else {
 			System.err.println("buffer is null");
 		}
 	}
 
 	public MemoryFileBuffer writeXLS(IWContext iwc, Race race, List<Registration> registrations, Map<Registration, Participant> participantsMap) throws Exception {
 		MemoryFileBuffer buffer = new MemoryFileBuffer();
 		MemoryOutputStream mos = new MemoryOutputStream(buffer);
 
 		String sheetname = StringEscapeUtils.unescapeHtml(iwrb.getLocalizedString(race.getEvent().getLocalizedKey() + "." + race.getDistance().getLocalizedKey() + (race.getNumberOfRelayLegs() > 1 ? ".relay" : ""), race.getDistance().getName()).replaceAll("\\<[^>]*>", ""));
 		
 		HSSFWorkbook wb = new HSSFWorkbook();
 		HSSFSheet sheet = wb.createSheet(StringHandler.shortenToLength(sheetname, 30));
 
 		HSSFFont font = wb.createFont();
 		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
 		font.setFontHeightInPoints((short) 12);
 		
 		HSSFCellStyle style = wb.createCellStyle();
 		style.setFont(font);
 
 		int cellRow = 0;
 		HSSFRow row = sheet.createRow(cellRow++);
 
 		int iCell = 0;
 		HSSFCell cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("name", "Name"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("personal_id", "Personal ID"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("date_of_birth", "Date of birth"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("email", "Email"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("address", "Address"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("postal_code", "Postal code"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("country", "Country"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("phone", "Phone"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("mobile", "Mobile"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("nationality", "Nationality"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("shirt_size", "Shirt size"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("amount", "Amount"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("best_marathon_time", "Best marathon time"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("best_ultra_marathon_time", "Best ultra marathon time"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("charity", "Charity"));
 		cell.setCellStyle(style);
 		cell = row.createCell(iCell++);
 		cell.setCellValue(this.iwrb.getLocalizedString("created", "Created"));
 		cell.setCellStyle(style);
 
 		for (Registration registration : registrations) {
 			Participant participant = participantsMap.get(registration);
 			Country nationality = getCountryHome().findByPrimaryKey(registration.getNationality());
 			
 			row = sheet.createRow(cellRow++);
 			iCell = 0;
 			
 			row.createCell(iCell++).setCellValue(participant.getFullName());
 			row.createCell(iCell++).setCellValue(participant.getPersonalId());
 			row.createCell(iCell++).setCellValue(new IWTimestamp(participant.getDateOfBirth()).getDateString("d.M.yyyy"));
 			row.createCell(iCell++).setCellValue(participant.getEmail());
 			row.createCell(iCell++).setCellValue(participant.getAddress());
 			row.createCell(iCell++).setCellValue(participant.getPostalAddress());
 			row.createCell(iCell++).setCellValue(participant.getCountry());
 			row.createCell(iCell++).setCellValue(participant.getPhoneHome());
 			row.createCell(iCell++).setCellValue(participant.getPhoneMobile());
 			row.createCell(iCell++).setCellValue(nationality.getIsoAbbreviation());
 			row.createCell(iCell++).setCellValue(registration.getShirtSize().getSize() + " - " + registration.getShirtSize().getGender());
 			row.createCell(iCell++).setCellValue(registration.getAmountPaid() - registration.getAmountDiscount());
 			row.createCell(iCell++).setCellValue(registration.getBestMarathonTime() != null ? new IWTimestamp(registration.getBestMarathonTime()).getDateString("yyyy - HH:mm") : "");
 			row.createCell(iCell++).setCellValue(registration.getBestUltraMarathonTime() != null ? new IWTimestamp(registration.getBestUltraMarathonTime()).getDateString("yyyy: HH:mm") : "");
 			row.createCell(iCell++).setCellValue(registration.getCharity() != null ? registration.getCharity().getName() : "");
			row.createCell(iCell++).setCellValue(new IWTimestamp(registration.getCreatedDate()).getDateString("d.M.yyyy H:m"));
 		}
 		
 		wb.write(mos);
 
 		buffer.setMimeType(MimeTypeUtil.MIME_TYPE_EXCEL_2);
 		return buffer;
 	}
 
 	private PheidippidesService getService() {
 		if (service == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return service;
 	}
 
 	private PheidippidesDao getDao() {
 		if (dao == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return dao;
 	}
 	
 	private CountryHome getCountryHome() {
 		try {
 			return (CountryHome) IDOLookup.getHome(Country.class);
 		} catch (RemoteException rme) {
 			throw new RuntimeException(rme.getMessage());
 		}
 	}
 }
