 package eionet.eunis.stripes.actions;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 
 import org.apache.commons.lang.StringUtils;
 
 import ro.finsiel.eunis.jrfTables.EunisISOLanguagesPersist;
 import eionet.eunis.util.Pair;
 
 /**
  * @author Aleksandr Ivanov
  * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
  */
 @UrlBinding("/refreshtemplate")
 public class UpdateTemplateActionBean extends AbstractStripesAction {
 	
 	public static final String HEADER = "header_template.jsp";
 	public static final String FOOTER = "footer_template.jsp";
 	public static final String REQUIRED_HEAD = "required_head_template.jsp";
 	
 	private List<String> errors = new LinkedList<String>();
 	
 	/**
 	 * updates the templates and redirects to result page
 	 * @return
 	 */
 	@DefaultHandler
 	public Resolution defaultAction() {
 		List<EunisISOLanguagesPersist> languages = getContentManagement().getTranslatedLanguages();
 		//filtering the languages
 		List<String> filteredLanguages = new LinkedList<String>();
 		for(EunisISOLanguagesPersist language : languages) {
 			if (language.getCode() != null && !"en".equalsIgnoreCase(language.getCode().trim())) {
 				filteredLanguages.add(language.getCode().trim().toLowerCase());
 			}
 		}
 		
 		//HEADER update
 		String rawHeaderUrl = getContext().getApplicationProperty("TEMPLATES_HEADER");
 		//updating English template
 		if(!updateTemplate(
 				rawHeaderUrl.replace("#{lang}", ""),
 				HEADER,
 				"header_template.jsp",
 				"EN")){
 			errors.add("Could not update header template for language EN");
 		}
 		// fetching localizations
 		for (String language : filteredLanguages) {	
 			if(!updateTemplate(
 					rawHeaderUrl.replace("#{lang}", language +'/'),
 					HEADER,
 					"header_template.jsp",
 					language)) {
 				errors.add("Could not update header template for language " + language);
 			}
 		}
 		
 		//FOOTER update
 		String rawFooterUrl = getContext().getApplicationProperty("TEMPLATES_FOOTER");
 		//English template
 		if (!updateTemplate(
 				rawFooterUrl.replace("#{lang}", ""),
 				FOOTER,
 				"footer_template.jsp",
 				"EN")) {
 			errors.add("Could not update footer template for language EN");;
 		}
 		// fetching localizations
 		for (String language : filteredLanguages) {	
 			if(!updateTemplate(
 					rawFooterUrl.replace("#{lang}", language +'/'),
 					FOOTER,
 					"footer_template.jsp",
 					language)) {
 				errors.add("Could not update footer template for language " + language);;
 			}
 		}
 		
 		//REQUIRED_HEAD update
         String reqHeaderUrl = getContext().getApplicationProperty("TEMPLATES_REQUIRED_HEAD");
         if(!updateTemplate(
 				reqHeaderUrl,
 				REQUIRED_HEAD,
 				"required_head_template.jsp",
 				"EN")){
         	errors.add("Could not update required head template for language EN");;
         }
         if (!errors.isEmpty()) {
         	logger.warn(errors.toString());
         }
         return new RedirectResolution("/templateUpdated.jsp");
 	}
 
 	private boolean updateTemplate(String url, String idPage, String description, String language) {
         Pair<Integer, String> result = getContentManagement().readContentFromUrl(url);
         if (result != null && result.getId()!= null && result.getId() == 200 && StringUtils.isNotBlank(result.getValue())) {        	
        	return getContentManagement().savePageContentJDBC(
         			idPage,
         			result.getValue(),
         			description,
         			language,
        			(short)result.getValue().length(),
         			null,
         			true,
         			getContext().getJdbcDriver(),
         			getContext().getJdbcUrl(),
         			getContext().getJdbcUser(),
         			getContext().getJdbcPassword());
         }
         return false;
 	}
 	
 
 	/**
 	 * @return the headerUpdated
 	 */
 	public boolean isCorrectlyUpdated() {
 		return errors.isEmpty();
 	}
 	
 	/**
 	 * @return list errors
 	 */
 	public List<String> getErrors() {
 		return errors;
 	}
 
 }
