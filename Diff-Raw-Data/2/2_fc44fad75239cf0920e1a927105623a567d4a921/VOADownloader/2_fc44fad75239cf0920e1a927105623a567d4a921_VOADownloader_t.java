 package uk.co.epii.conservatives.henryaddington.voa;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: James Robinson
  * Date: 07/10/2013
  * Time: 16:59
  */
 public class VOADownloader {
 
     private static final Logger LOG = LoggerFactory.getLogger(VOADownloader.class);
 
     private final Pattern showingPattern = Pattern.compile("Showing [0-9]* - [0-9]* of ([0-9]*)");
 
     private WebClient webClient;
     private HtmlForm dwellingSearchForm;
     private HtmlSelect authoritiesSelect;
     private HtmlButton searchButton;
     private HtmlSelect councilTaxBandSelect;
     private TreeMap<String, String> localAuthorityCodes;
     private ArrayList<String> bands;
     private HtmlPage resultsHtmlPage;
     private int seen;
     private int total;
     private String council;
     private String band;
     private FileWriter fileWriter;
     private PrintWriter printWriter;
 
     private String advancedSearchFormId;
     private String localAuthoritySelectId;
     private String councilTaxBandsSelectId;
     private String paginationSelectId;
     private String saveLocationRoot;
     private String voaUri;
     private int paginate;
     private long sleepBetweenPageRequests;
     private String resultsTableTitle;
 
     public HtmlPage getPage(String uri) {
         try {
             return webClient.getPage(uri);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public HtmlPage setSelectedAttribute(HtmlSelect s, String attribute)
     {
         return s.setSelectedAttribute(attribute, true);
     }
 
     public HtmlPage click(HtmlButton button)
     {
         try {
             return button.click();
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void init()
     {
         initiateWebClient();
         findDwellingSearchForm();
         loadAuthorityCodes();
         loadCouncilTaxBands();
         findSearchButton();
     }
 
     private void initiateWebClient() {
         this.webClient = new WebClient(BrowserVersion.FIREFOX_17);
         this.webClient.setThrowExceptionOnScriptError(false);
         this.webClient.setThrowExceptionOnFailingStatusCode(false);
     }
 
     private void loadCouncilTaxBands() {
         councilTaxBandSelect = dwellingSearchForm.getElementById(councilTaxBandsSelectId);
         this.bands = new ArrayList();
         for (HtmlOption o : councilTaxBandSelect.getOptions())
             this.bands.add(o.getText());
         this.bands.remove(0);
     }
 
     private void loadAuthorityCodes() {
         authoritiesSelect = dwellingSearchForm.getElementById(localAuthoritySelectId);
         localAuthorityCodes = new TreeMap();
         for (HtmlOption o : this.authoritiesSelect.getOptions()) {
             this.localAuthorityCodes.put(o.getText().toUpperCase(), o.getAttribute("value"));
         }
         this.localAuthorityCodes.remove("");
     }
 
     private void findDwellingSearchForm() {
         HtmlPage page = getPage(voaUri);
         List<HtmlForm> forms = page.getForms();
         dwellingSearchForm = null;
         for (HtmlForm f : forms)
             if (forms.get(0).getId().equals(advancedSearchFormId)) {
                 dwellingSearchForm = f;
                 break;
             }
         if (dwellingSearchForm == null) {
             throw new RuntimeException(String.format("Form with id '{}' cannot be found", advancedSearchFormId));
         }
     }
 
     private void findSearchButton() {
         for (HtmlElement e : dwellingSearchForm.getElementsByTagName("button")) {
             if ((e.getAttribute("value").equals("Search")) && (e.getAttribute("type").equals("submit"))) {
                 searchButton = ((HtmlButton)e);
             }
         }
     }
 
     public void downloadAll() {
         for (String council : localAuthorityCodes.keySet()) {
             download(council);
         }
     }
 
     public void download(String council) {
         initiateWriters(council, null);
         try {
             for (String band : bands) {
                 process(council, band);
             }
         }
         finally {
             closeWriters();
         }
     }
 
     public void download(String council, String band) {
         initiateWriters(council, band);
         try {
             process(council, band);
         }
         finally {
             closeWriters();
         }
     }
 
     private void process(String council, String band) {
         this.council = council;
         this.band = band;
         selectTargetCouncilAndBand(council, band);
         loadPageOfDesiredSize();
         findTotal();
         readAndWrite();
     }
 
     private void readAndWrite() {
         seen = 0;
         while (seen < total) {
             LOG.info("{} - {} loaded: {} of {}", new Object[]{council, band, seen, total});
             save(getResultsTable());
             if (isComplete()) {
                 break;
             }
             loadNextPage();
             pauseToAvoidDoSAttack();
         }
     }
 
     private boolean isComplete() {
         if (seen == total) {
             return true;
         }
         if (seen > total) {
             throw new IllegalStateException("More than the expected total number of results has been seen");
         }
         return false;
     }
 
     private void loadNextPage() {
         HtmlAnchor nextPage = findNextPage();
         try {
             resultsHtmlPage = nextPage.click();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void initiateWriters(String council, String band) {
         String fileName = band == null ?
                 String.format("%s%s.txt", saveLocationRoot, council) :
                 String.format("%s%s-%s.txt", saveLocationRoot, council, band);
         try {
             fileWriter = new FileWriter(fileName.toString(), true);
             printWriter = new PrintWriter(fileWriter, true);
         }
         catch (IOException ioe) {
             throw new RuntimeException(ioe);
         }
     }
 
     private void closeWriters() {
         if (printWriter != null) {
             printWriter.flush();
             printWriter.close();
             printWriter = null;
         }
         if (fileWriter != null) {
             try {
                 fileWriter.close();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
             finally {
                 fileWriter = null;
             }
         }
     }
 
     private void pauseToAvoidDoSAttack() {
         try {
             Thread.sleep(sleepBetweenPageRequests);
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     private HtmlAnchor findNextPage() {
         for (DomElement e : resultsHtmlPage.getElementsByTagName("a")) {
             if ((e.getTextContent().trim().equals("Next page")) && (e.getAttribute("href").equals("Javascript:Next()")))
             {
                 return (HtmlAnchor)e;
             }
         }
         LOG.error(resultsHtmlPage.asXml());
         throw new IllegalStateException("The current page is not the end yet in contains no next page anchor");
     }
 
     private HtmlTable getResultsTable() {
         ArrayList<HtmlTable> resultsTables = new ArrayList<HtmlTable>();
         for (DomElement e : resultsHtmlPage.getElementsByTagName("table")) {
             if (e.getAttribute("title").equals(resultsTableTitle)) {
                 resultsTables.add((HtmlTable)e);
             }
         }
         if (resultsTables.isEmpty()) {
             LOG.error("No search resultsHtmlPage found");
             LOG.error(resultsHtmlPage.asXml());
             throw new IllegalStateException("No search resultsHtmlPage found");
         }
         if (resultsTables.size() > 1) {
             LOG.error("Multiple resultsHtmlPage found");
             LOG.error(resultsHtmlPage.asXml());
             throw new IllegalStateException("Multiple resultsHtmlPage found");
         }
         return resultsTables.get(0);
     }
 
     private void loadPageOfDesiredSize() {
         resultsHtmlPage = click(searchButton);
         HtmlSelect s = (HtmlSelect)resultsHtmlPage.getElementById(paginationSelectId);
         resultsHtmlPage = setSelectedAttribute(s, paginate + "");
     }
 
     private void findTotal() {
         Matcher showingMatcher = showingPattern.matcher(resultsHtmlPage.asText());
         if (showingMatcher.find()) {
             total = Integer.parseInt(showingMatcher.group(1));
         }
         else {
             throw new IllegalArgumentException("Provided page does not contain a match for the Showing regex");
         }
     }
 
     private void selectTargetCouncilAndBand(String council, String band) {
         String value = this.localAuthorityCodes.get(council.toUpperCase());
         if (!bands.contains(band)) {
             throw new IllegalArgumentException(String.format("Unknown band: %s", band));
         }
         if (value == null) {
             throw new IllegalArgumentException(String.format("Unknown council: %s", council));
         }
         authoritiesSelect.setSelectedAttribute(value, true);
         councilTaxBandSelect.setSelectedAttribute(band, true);
     }
 
     public void save(HtmlTable table) {
         LOG.info("Table: {}-{}-{}", new Object[]{council, band, seen});
         int saved = 0;
         for (HtmlTableBody tableBody : table.getBodies()) {
             for (HtmlTableRow row : tableBody.getRows()) {
                 List<HtmlTableCell> cells = row.getCells();
                 if (cells.size() != 4) {
                     continue;
                 }
                 printWriter.println(String.format("%s~%s~%s~%s",
                         cells.get(0).getTextContent().trim(),
                         cells.get(1).getTextContent().trim(),
                         cells.get(2).getTextContent().trim().length() == 0 ? "YES" : "NO",
                         cells.get(3).getTextContent().trim()));
                 saved++;
             }
         }
         seen += saved;
         if (saved != paginate && seen != total) {
             throw new IllegalArgumentException(
                    String.format("The table provided contains the wrong number of cells: %s", saved));
         }
     }
 
     public void setVoaUri(String voaUri) {
         this.voaUri = voaUri;
     }
 
     public void setAdvancedSearchFormId(String advancedSearchFormId) {
         this.advancedSearchFormId = advancedSearchFormId;
     }
 
     public void setLocalAuthoritySelectId(String localAuthoritySelectId) {
         this.localAuthoritySelectId = localAuthoritySelectId;
     }
 
     public void setCouncilTaxBandsSelectId(String councilTaxBandsSelectId) {
         this.councilTaxBandsSelectId = councilTaxBandsSelectId;
     }
 
     public void setSaveLocationRoot(String saveLocationRoot) {
         this.saveLocationRoot = saveLocationRoot.replaceAll("^\\~", System.getProperty("user.home"));
     }
 
     public void setPaginate(int paginate) {
         this.paginate = paginate;
     }
 
     public void setPaginationSelectId(String paginationSelectId) {
         this.paginationSelectId = paginationSelectId;
     }
 
     public void setSleepBetweenPageRequests(long sleepBetweenPageRequests) {
         this.sleepBetweenPageRequests = sleepBetweenPageRequests;
     }
 
     public void setResultsTableTitle(String resultsTableTitle) {
         this.resultsTableTitle = resultsTableTitle;
     }
 }
