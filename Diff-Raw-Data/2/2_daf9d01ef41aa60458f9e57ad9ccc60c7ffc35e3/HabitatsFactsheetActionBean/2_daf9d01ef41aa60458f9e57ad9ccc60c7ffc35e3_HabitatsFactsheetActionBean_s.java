 package eionet.eunis.stripes.actions;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 
 import ro.finsiel.eunis.factsheet.habitats.HabitatsFactsheet;
 import eionet.eunis.dto.HabitatFactsheetOtherDTO;
 import eionet.eunis.util.Pair;
 
 /**
  * Action bean to handle habitats-factsheet functionality.
  * 
  * @author Risto Alt
  * <a href="mailto:risto.alt@tietoenator.com">contact</a>
  */
 @UrlBinding("/habitats/{idHabitat}/{tab}")
 public class HabitatsFactsheetActionBean extends AbstractStripesAction {
 	
 	private String idHabitat = "";
 		
 	private static final String [] tabs = {
 			"General information", 
 			"Geographical distribution", 
 			"Legal instruments", 
 			"Habitat types", 
 			"Sites", 
 			"Species", 
 			"Other info"
 	};
 
     private static final String[][] dbtabs = {
     	{"GENERAL_INFORMATION","general"},
     	{"GEOGRAPHICAL_DISTRIBUTION","distribution"},
     	{"LEGAL_INSTRUMENTS","instruments"},
     	{"HABITATS","habitats"},
     	{"SITES","sites"},
     	{"SPECIES","species"},
     	{"OTHER","other"}
     };
     
     private static final Integer[] dictionary = {
             HabitatsFactsheet.OTHER_INFO_ALTITUDE,
             HabitatsFactsheet.OTHER_INFO_DEPTH,
             HabitatsFactsheet.OTHER_INFO_CLIMATE,
             HabitatsFactsheet.OTHER_INFO_GEOMORPH,
             HabitatsFactsheet.OTHER_INFO_SUBSTRATE,
             HabitatsFactsheet.OTHER_INFO_LIFEFORM,
             HabitatsFactsheet.OTHER_INFO_COVER,
             HabitatsFactsheet.OTHER_INFO_HUMIDITY,
             HabitatsFactsheet.OTHER_INFO_WATER,
             HabitatsFactsheet.OTHER_INFO_SALINITY,
             HabitatsFactsheet.OTHER_INFO_EXPOSURE,
             HabitatsFactsheet.OTHER_INFO_CHEMISTRY,
             HabitatsFactsheet.OTHER_INFO_TEMPERATURE,
             HabitatsFactsheet.OTHER_INFO_LIGHT,
             HabitatsFactsheet.OTHER_INFO_SPATIAL,
             HabitatsFactsheet.OTHER_INFO_TEMPORAL,
             HabitatsFactsheet.OTHER_INFO_IMPACT,
             HabitatsFactsheet.OTHER_INFO_USAGE
     };
     private int dictionaryLength;
     
     private String btrail;
     private String pageTitle = "";
     private String metaDescription = "";
 	
 	private HabitatsFactsheet factsheet;
 	private List<HabitatFactsheetOtherDTO> otherInfo = new ArrayList<HabitatFactsheetOtherDTO>();
 	
 	//selected tab
 	private String tab;
 	private boolean isMini;
 	//tabs to display
 	private List<Pair<String,String>> tabsWithData = new LinkedList<Pair<String,String>>();
 	
 	/**
 	 * This action bean only serves RDF through {@link RdfAware}.
 	 */
 	@DefaultHandler
 	public Resolution defaultAction() {
 		
 		if(tab == null || tab.length() == 0){
 			tab = "general";
 		}
 		
 		String eeaHome = getContext().getInitParameter("EEA_HOME");
 		btrail = "eea#" + eeaHome + ",home#index.jsp,habitat_types#habitats.jsp,factsheet";
 		factsheet = new HabitatsFactsheet(idHabitat);
 		//set metadescription and page title
 		if (factsheet != null) {
 			metaDescription = factsheet.getMetaHabitatDescription();
 			pageTitle = getContext().getInitParameter("PAGE_TITLE") 
					+ getContentManagement().cms("factsheet_for")
 					+ " " + factsheet.getHabitat().getScientificName();
 		} else {
 			pageTitle = getContext().getInitParameter("PAGE_TITLE")
 					+ getContentManagement().cmsPhrase("Sorry, no habitat type has been found in the database with Habitat type ID = ")
 					+ "'" + idHabitat + "'";
 			try{
 				getContext().getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
 			} catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		if (factsheet != null) {
 	
 			for (int i=0; i < tabs.length; i++) {
 				if(!getContext().getSqlUtilities().TabPageIsEmpy(factsheet.idNatureObject.toString(),"HABITATS",dbtabs[i][0])) {
 					tabsWithData.add(new Pair<String, String>(dbtabs[i][1], tabs[i]));
 				}
 			}
 			
 			if(tab != null && tab.equals("other")){
 				dictionaryLength = dictionary.length;
 				if(factsheet.isEunis()){
 					for(int i = 0; i < dictionary.length; i++){
 						try{
 							Integer dictionaryType = dictionary[i];
 							String title = factsheet.getOtherInfoDescription(dictionaryType);
 							String SQL = factsheet.getSQLForOtherInfo(dictionaryType);
 							String noElements = getContext().getSqlUtilities().ExecuteSQL(SQL);
 							if(title != null){
 								HabitatFactsheetOtherDTO dto = new HabitatFactsheetOtherDTO();
 								dto.setTitle(title);
 								dto.setDictionaryType(dictionaryType);
 								dto.setNoElements(noElements);
 								otherInfo.add(dto);
 							}
 							
 						}catch (Exception e){
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		
 		}
 		return new ForwardResolution("/stripes/habitats-factsheet.layout.jsp");
 	}
 	
 	public String getIdHabitat() {
 		return idHabitat;
 	}
 
 
 	public void setIdHabitat(String idHabitat) {
 		this.idHabitat = idHabitat;
 	}
 
 	public HabitatsFactsheet getFactsheet() {
 		return factsheet;
 	}
 
 	/**
 	 * @return the tabs
 	 */
 	public String[] getTabs() {
 		return tabs;
 	}
 
 	/**
 	 * @return the dbtabs
 	 */
 	public String[][] getDbtabs() {
 		return dbtabs;
 	}
 
 	/**
 	 * @return the btrail
 	 */
 	public String getBtrail() {
 		return btrail;
 	}
 
 	/**
 	 * @return the pageTitle
 	 */
 	public String getPageTitle() {
 		return pageTitle;
 	}
 
 	/**
 	 * @return the metaDescription
 	 */
 	public String getMetaDescription() {
 		return metaDescription;
 	}
 
 	/**
 	 * @return the tab
 	 */
 	public String getTab() {
 		return tab;
 	}
 
 	/**
 	 * @param tab the tab to set
 	 */
 	public void setTab(String tab) {
 		this.tab = tab;
 	}
 
 	public boolean isMini() {
 		return isMini;
 	}
 
 	public void setMini(boolean isMini) {
 		this.isMini = isMini;
 	}
 
 	/**
 	 * @return the tabsWithData
 	 */
 	public List<Pair<String, String>> getTabsWithData() {
 		return tabsWithData;
 	}
 
 	public List<HabitatFactsheetOtherDTO> getOtherInfo() {
 		return otherInfo;
 	}
 
 	public Integer[] getDictionary() {
 		return dictionary;
 	}
 
 	public int getDictionaryLength() {
 		return dictionaryLength;
 	}
 
 	public void setDictionaryLength(int dictionaryLength) {
 		this.dictionaryLength = dictionaryLength;
 	}
 
 
 }
