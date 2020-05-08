 package is.idega.idegaweb.pheidippides.presentation;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.bean.PheidippidesCompanyBean;
 import is.idega.idegaweb.pheidippides.business.CompanyImportSession;
 import is.idega.idegaweb.pheidippides.business.CompanyImportStatus;
 import is.idega.idegaweb.pheidippides.business.ParticipantHolder;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.Company;
 import is.idega.idegaweb.pheidippides.data.Event;
 import is.idega.idegaweb.pheidippides.data.Participant;
 import is.idega.idegaweb.pheidippides.data.RaceTrinket;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.block.web2.business.JQuery;
 import com.idega.block.web2.business.JQueryPlugin;
 import com.idega.builder.bean.AdvancedProperty;
 import com.idega.facelets.ui.FaceletComponent;
 import com.idega.idegaweb.IWBundle;
 import com.idega.io.UploadFile;
 import com.idega.presentation.IWBaseComponent;
 import com.idega.presentation.IWContext;
 import com.idega.util.CoreConstants;
 import com.idega.util.FileUtil;
 import com.idega.util.IWTimestamp;
 import com.idega.util.PresentationUtil;
 import com.idega.util.expression.ELUtil;
 
 public class CompanyImporter extends IWBaseComponent {
 	private static final String PARAMETER_ACTION = "prm_action";
 	private static final int ACTION_SELECT_FILE = 1;
 	private static final int ACTION_RACE_SELECT = 2;
 	private static final int ACTION_DONE = 3;
 	private static final int ACTION_ERROR = 4;
 
 	@Autowired
 	private PheidippidesService service;
 
 	@Autowired
 	private JQuery jQuery;
 
 	private IWBundle iwb;
 	
 	@Autowired
 	private PheidippidesDao dao;
 
 
 	@Override
 	protected void initializeComponent(FacesContext context) {
 		IWContext iwc = IWContext.getIWContext(context);
 		iwb = getBundle(context, getBundleIdentifier());
 
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery()
 				.getBundleURIToJQueryLib());
 		PresentationUtil.addJavaScriptSourcesLinesToHeader(
 				iwc,
 				getJQuery().getBundleURISToValidation(
 						iwc.getCurrentLocale().getLanguage()));
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery()
 				.getBundleURIToJQueryPlugin(JQueryPlugin.MASKED_INPUT));
 
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc,
 				CoreConstants.DWR_ENGINE_SCRIPT);
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc,
 				CoreConstants.DWR_UTIL_SCRIPT);
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc,
 				"/dwr/interface/PheidippidesService.js");
 
 		PresentationUtil
 				.addJavaScriptSourceLineToHeader(
 						iwc,
 						iwb.getVirtualPathWithFileNameString("javascript/companyImporter.js"));
 		PresentationUtil.addStyleSheetToHeader(iwc,
 				iwb.getVirtualPathWithFileNameString("style/pheidippides.css"));
 
 		PheidippidesCompanyBean bean = getBeanInstance("pheidippidesCompanyBean");
 		bean.setLocale(iwc.getCurrentLocale());
 		bean.setProperty(new AdvancedProperty(String.valueOf(IWTimestamp
 				.RightNow().getYear()), String.valueOf(IWTimestamp.RightNow()
 				.getYear())));
 
 		
 		Company company = getDao().getCompanyByUserUUID(iwc.getCurrentUser().getUniqueId()); 
 		Event event = company.getEvent();
 		bean.setEvent(event);
 		bean.setCompany(company);
 		bean.setRaces(getService().getOpenRaces(bean.getEvent().getId(), IWTimestamp.RightNow().getYear(), false));
 		bean.setRaceShirtSizes(null);
 		bean.setLocale(iwc.getCurrentLocale());
 		List<RaceTrinket> trinkets = new ArrayList<RaceTrinket>();
 		trinkets.add(getDao().getTrinket("MEDAL"));
 		bean.setRaceTrinkets(trinkets);
 
 		boolean showTrinket = bean.getEvent().getReportSign().equals("MH") ? true : false;
 		
 		switch (parseAction(iwc)) {
 		case ACTION_SELECT_FILE:
 			showImportForm(iwc);
 			break;
 
 		case ACTION_ERROR:
 			showError(iwc);
 			break;
 
 		case ACTION_RACE_SELECT:
 			showRaceSelect(iwc, showTrinket);
 			break;
 
 		case ACTION_DONE:
			showDone(iwc, showTrinket);
 			break;
 
 		}
 	}
 
 	private void showImportForm(IWContext iwc) {
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication()
 				.createComponent(FaceletComponent.COMPONENT_TYPE);
 		facelet.setFaceletURI(iwb.getFaceletURI("companyImporter/import.xhtml"));
 		add(facelet);
 	}
 
 	private void showRaceSelect(IWContext iwc, boolean showTrinket) {
 		UploadFile uploadFile = iwc.getUploadedFile();
 		if (uploadFile != null && uploadFile.getName() != null
 				&& uploadFile.getName().length() > 0) {
 			try {
 				PheidippidesCompanyBean bean = getBeanInstance("pheidippidesCompanyBean");
 				CompanyImportSession session = getBeanInstance("companyImportSession");
 
 				FileInputStream input = new FileInputStream(
 						uploadFile.getRealPath());
 				Map<CompanyImportStatus, List<Participant>> toImport = getService()
 						.importCompanyExcelFile(input, bean.getEvent(), IWTimestamp.RightNow().getYear());
 
 				boolean hasError = false;
 				List<Participant> errors = null;
 				List<Participant> participantList = null;
 				if (toImport != null && !toImport.isEmpty()) {
 					errors = toImport.get(CompanyImportStatus.MISSING_REQUIRED_FIELD);
 					if (errors != null && !errors.isEmpty()) {
 						bean.setMissingRequiredFields(errors);
 						hasError = true;
 					}
 
 					errors = toImport
 							.get(CompanyImportStatus.ERROR_IN_PERSONAL_ID);
 					if (errors != null && !errors.isEmpty()) {
 						bean.setInvalidPersonalID(errors);
 						hasError = true;
 					}
 
 					errors = toImport
 							.get(CompanyImportStatus.ERROR_ALREADY_REGISTERED);
 					if (errors != null && !errors.isEmpty()) {
 						bean.setAlreadyRegistered(errors);
 						hasError = true;
 					}
 
 					if (!hasError) {
 						participantList = toImport.get(CompanyImportStatus.OK);
 						if (participantList != null && !participantList.isEmpty()) {
 							long count = dao.getNumberOfParticipantsForCompany(bean.getCompany(), bean.getEvent(), IWTimestamp.RightNow().getYear());
 							count += participantList.size();
 							if (count > bean.getCompany().getMaxNumberOfParticipants()) {
 								hasError = true;
 								bean.setToManyImported(true);
 							} else {
 								bean.setToImport(participantList);
 								session.setParticipantsToImport(participantList);
 							}
 						} else {
 							bean.setUnableToImportFile(true);
 							hasError = true;						
 						}
 					}					
 				} else {
 					bean.setUnableToImportFile(true);
 					hasError = true;
 				}
 
 				try {
 					FileUtil.delete(uploadFile);
 				} catch (Exception ex) {
 					System.err
 							.println("MediaBusiness: deleting the temporary file at "
 									+ uploadFile.getRealPath() + " failed.");
 				}
 
 				if (hasError) {
 					showError(iwc);
 					return;
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				uploadFile.setId(-1);
 			}
 		}
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication()
 				.createComponent(FaceletComponent.COMPONENT_TYPE);
 		if (showTrinket) {
 			facelet.setFaceletURI(iwb
 					.getFaceletURI("companyImporter/raceSelectWithTrinket.xhtml"));
 		} else {
 			facelet.setFaceletURI(iwb
 					.getFaceletURI("companyImporter/raceSelect.xhtml"));			
 		}
 		add(facelet);
 	}
 
	private void showDone(IWContext iwc, boolean raceHasTrinkets) {
 		CompanyImportSession session = getBeanInstance("companyImportSession");
 		PheidippidesCompanyBean bean = getBeanInstance("pheidippidesCompanyBean");
 		String races[] = iwc.getParameterValues("prm_race_pk");
 		//String shirts[] = iwc.getParameterValues("prm_shirt_size");
 		String medal[] = iwc.getParameterValues("prm_trinket");
 		
 		List<Participant> participants = session.getParticipantsToImport();
 		List<ParticipantHolder> holders = new ArrayList<ParticipantHolder>();
 		int counter = 0;
 		for (Participant participant : participants) {
 			ParticipantHolder holder = new ParticipantHolder();
 			holder.setParticipant(participant);
 			holder.setRace(dao.getRace(Long.parseLong(races[counter])));
 			//holder.setShirtSize(dao.getShirtSize(Long.parseLong(shirts[counter++])));
			if (raceHasTrinkets) {
				if (medal[counter] != null && !"".equals(medal[counter])) {
					holder.setTrinket(dao.getTrinket(Long.parseLong(medal[counter++])));
				}
 			}
 			
 			holders.add(holder);
 		}
 
 		if (!holders.isEmpty()) {
 			getService().storeCompanyRegistration(holders, bean.getCompany(), iwc.getCurrentUser().getUniqueId(), iwc.getCurrentLocale());
 		}
 
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication()
 				.createComponent(FaceletComponent.COMPONENT_TYPE);
 		facelet.setFaceletURI(iwb.getFaceletURI("companyImporter/done.xhtml"));
 		add(facelet);
 	}
 
 	private void showError(IWContext iwc) {
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication()
 				.createComponent(FaceletComponent.COMPONENT_TYPE);
 		facelet.setFaceletURI(iwb.getFaceletURI("companyImporter/error.xhtml"));
 		add(facelet);
 	}
 
 	private String getBundleIdentifier() {
 		return PheidippidesConstants.IW_BUNDLE_IDENTIFIER;
 	}
 
 	private PheidippidesService getService() {
 		if (service == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 
 		return service;
 	}
 
 	private JQuery getJQuery() {
 		if (jQuery == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 
 		return jQuery;
 	}
 
 	private PheidippidesDao getDao() {
 		if (dao == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return dao;
 	}
 
 	private int parseAction(IWContext iwc) {
 		int action = iwc.isParameterSet(PARAMETER_ACTION) ? Integer
 				.parseInt(iwc.getParameter(PARAMETER_ACTION))
 				: ACTION_SELECT_FILE;
 
 		return action;
 	}
 }
