 package is.idega.idegaweb.pheidippides.business;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.data.Event;
 import is.idega.idegaweb.pheidippides.data.Participant;
 import is.idega.idegaweb.pheidippides.data.Race;
 import is.idega.idegaweb.pheidippides.data.Registration;
 import is.idega.idegaweb.pheidippides.data.ShirtSize;
 import is.idega.idegaweb.pheidippides.util.PheidippidesUtil;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.block.pdf.business.PrintingContext;
 import com.idega.block.pdf.business.PrintingContextImpl;
 import com.idega.idegaweb.IWApplicationContext;
 import com.idega.idegaweb.IWBundle;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.util.FileUtil;
 import com.idega.util.LocaleUtil;
 import com.idega.util.PersonalIDFormatter;
 import com.idega.util.expression.ELUtil;
 
 
 public class ReceiptPrintingContext extends PrintingContextImpl {
 
 	@Autowired
 	private PheidippidesService service;
 	
 	private IWBundle iwb;
 	private IWResourceBundle iwrb;
 
 	public ReceiptPrintingContext(IWApplicationContext iwac, Registration registration, Locale locale) {
 		init(iwac, registration, locale);
 	}
 
 	private void init(IWApplicationContext iwac, Registration registration, Locale locale) {
 		Map<String, Object> props = new HashMap<String, Object>();
 		
 		props.put(PrintingContext.IW_BUNDLE_ROPERTY_NAME, getBundle(iwac));
 		props.put("iwrb", getResourceBundle(iwac, locale));
 		
 		Race race = registration.getRace();
 		Event event = race.getEvent();
 		Participant participant = getService().getParticipant(registration);
 		ShirtSize size = registration.getShirtSize();
 		int price = registration.getAmountPaid() - registration.getAmountDiscount();
 		
 		NumberFormat nf = NumberFormat.getCurrencyInstance(LocaleUtil.getIcelandicLocale()); 
 		nf.setMaximumFractionDigits(0);
 		nf.setMinimumFractionDigits(0);
 		nf.setParseIntegerOnly(true);
 		if (price < 250) {
 			nf.setCurrency(java.util.Currency.getInstance("EUR"));
 		}
 		
 		props.put("raceName", PheidippidesUtil.escapeXML(getResourceBundle(iwac, locale).getLocalizedString(event.getLocalizedKey() + ".name", event.getName())));
 		props.put("name", participant.getFullName());
 		if (participant.getPersonalId() != null) {
 			props.put("personalID", PersonalIDFormatter.format(participant.getPersonalId(), locale));
 		}
 		props.put("shirtSize", PheidippidesUtil.escapeXML(getResourceBundle(iwac, locale).getLocalizedString(event.getLocalizedKey() + "." + size.getLocalizedKey(), size.getGender() + " - " + size.getSize())));
 		props.put("distance", PheidippidesUtil.escapeXML(getResourceBundle(iwac, locale).getLocalizedString(event.getLocalizedKey() + "." + race.getDistance().getLocalizedKey() + (race.getNumberOfRelayLegs() > 1 ? ".relay" : ""), race.getDistance().getName())));
 		props.put("price", nf.format(price));
 
 		setFileName(getResourceBundle(iwac, locale).getLocalizedString("review_filename", "receipt") + ".pdf");
 		addDocumentProperties(props);
 		setResourceDirectory(new File(getResourceRealPath(getBundle(iwac), locale)));
 		try {
			File file = FileUtil.getFileFromWorkspace(getResourceRealPath(getBundle(iwac), locale) + event.getReportSign().toLowerCase() + "/" + race.getYear() + "/" + "receipt_template.xml");
 			setTemplateStream(new FileInputStream(file));
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected String getResourceRealPath(IWBundle iwb, Locale locale) {
 		String printFolder = "/print/";
 
 		if (locale != null) {
 			return iwb.getResourcesRealPath(locale) + printFolder;
 		}
 		else {
 			return iwb.getResourcesRealPath() + printFolder;
 		}
 	}
 
 	private PheidippidesService getService() {
 		if (service == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return service;
 	}
 
 	private String getBundleIdentifier() {
 		return PheidippidesConstants.IW_BUNDLE_IDENTIFIER;
 	}
 
 	private IWBundle getBundle(IWApplicationContext iwac) {
 		if (this.iwb == null) {
 			this.iwb = iwac.getIWMainApplication().getBundle(getBundleIdentifier());
 		}
 		return this.iwb;
 	}
 
 	private IWResourceBundle getResourceBundle(IWApplicationContext iwac, Locale locale) {
 		if (this.iwrb == null) {
 			this.iwrb = getBundle(iwac).getResourceBundle(locale);
 		}
 		return this.iwrb;
 	}
 }
