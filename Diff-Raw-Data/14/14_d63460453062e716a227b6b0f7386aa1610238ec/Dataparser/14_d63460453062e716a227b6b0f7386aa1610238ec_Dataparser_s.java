 package dataparser;
 
 //import java.io.File;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import javax.swing.JFileChooser;
 
 //import javax.xml.parsers.DocumentBuilder;
 //import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xquery.XQConnection;
 //import javax.xml.xquery.XQConstants;
 import javax.xml.xquery.XQDataSource;
 import javax.xml.xquery.XQException;
 import javax.xml.xquery.XQExpression;
 import javax.xml.xquery.XQSequence;
 
 //import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import net.sf.saxon.xqj.SaxonXQDataSource;
 
 public class Dataparser {
 
     private static String xml_file_name = "input/dblp_curated_sample.xml";
     private static ArrayList<PublicationBean> publications = new ArrayList<PublicationBean>();
     /**
      * SINGLETONS
      */
     private static Dataparser instance = null;
 
     private Dataparser() {
         initPublications();
     }
 
     //change XMl File
     public static void changeSource() {
         JFileChooser jfc = new JFileChooser();
         System.out.println("Enter here before");
         if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(null));
         System.out.println("Enter here after");
         File file = jfc.getSelectedFile();
 
         xml_file_name = file.getAbsolutePath();
         xml_file_name = xml_file_name.replace('\\', '/');
         System.out.println("xml_file_name :" + xml_file_name);
         publications = new ArrayList<PublicationBean>();
         initPublications();
     }
 
     public static Dataparser getInstance() {
         if (instance == null) {
             instance = new Dataparser();
         }
         return instance;
     }
 
     public static void initPublications() {
         System.out.println("initPublications");
         String query = "for $pub in (doc(\"" + xml_file_name + "\")//dblp/*) return string-join("
                 + "($pub/name(), $pub/title/concat(normalize-unicode(string(.)), '&#xA;'), $pub/year/text(), "
                 + "$pub/author), "
                 + "'###')";
 
         ArrayList<String> pubs = Dataparser.executeQueryAndExtractStringArrayList(query);
         PublicationBean pubbean;
         for (String pub : pubs) {
             try {
                 String[] parts = pub.split("###");
 
                 pubbean = new PublicationBean(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()));
                 for (int idx = 3; idx < parts.length; idx++) {
                     pubbean.addAuthor(parts[idx].trim());
                 }
                 publications.add(pubbean);
             } catch (Exception e) {
                 System.out.println("Problem with publication:");
                 System.out.println(pub);
             }
 
         }
     }
     /*
      *  OPTIMIZATION STUFF
      */
 //	private static boolean useDomOptimization = false;
 //	private static boolean useLocalStorageOptimization = true;
 //	public static void useDomOptimization(boolean b){
 //		useDomOptimization = b;
 //	}
 //	public static void useLocalStorageOptimization(boolean b){
 //		useLocalStorageOptimization = b;
 //	}
     /*
      * FILEPARSING STUFF
      */
     private static XQExpression exp;
 
     public static void initExpression() throws SAXException, IOException, ParserConfigurationException, XQException {
         XQDataSource ds = new SaxonXQDataSource();
         XQConnection conn = ds.getConnection();
         exp = conn.createExpression();
     }
 
     private static XQSequence executeXQuery(String query) throws XQException, SAXException, IOException, ParserConfigurationException {
         if (exp == null) {
             initExpression();
         }
 //		if(useDomOptimization){
 //			query = query.replace("doc(\"input/dblp_curated_sample.xml\")", "");
 //		}
         XQSequence seq = exp.executeQuery(query);
         return seq;
     }
 
 //	private static int executeQueryAndExtractInt(String query){
 //		int returnValue = -1;
 //		try{
 //			XQSequence seq = Dataparser.executeXQuery(query);
 //			seq.next();
 //			returnValue = seq.getInt();
 //			seq.close();
 //		} catch(Exception err) {
 //			System.out.println("Failed while exececuting "+err.getStackTrace()[0].getMethodName()+": " + err.getMessage());
 //			System.out.println("Failed query: "+query);
 //		}
 //		return returnValue;
 //	}
     private static ArrayList<String> executeQueryAndExtractStringArrayList(String query) {
         ArrayList<String> valueList = new ArrayList<String>();
         try {
             XQSequence seq = Dataparser.executeXQuery(query);
             while (seq.next()) {
                 valueList.add(seq.getAtomicValue());
             }
             seq.close();
         } catch (Exception err) {
             System.out.println("Failed while exececuting " + err.getStackTrace()[0].getMethodName() + ": " + err.getMessage());
             System.out.println("Failed query: " + query);
         }
         return valueList;
     }
 
 //	private static String executeQueryAndExtractString(String query){
 //		String valueList = "";
 //		try{
 //			XQSequence seq = Dataparser.executeXQuery(query);
 //			valueList=" "+seq.getSequenceAsString(null);
 //			seq.close();
 //		} catch(Exception err) {
 //			System.out.println("Failed while exececuting "+err.getStackTrace()[0].getMethodName()+": " + err.getMessage());
 //			System.out.println("Failed query: "+query);
 //		}
 //		return valueList;
 //	}
     /**
      * QUERIES
      *
      */
     private static ArrayList<String> getKeysFromHashMap(HashMap<String, ?> hm) {
         ArrayList<String> keys = new ArrayList<String>();
         for (String key : hm.keySet()) {
             keys.add(key);
         }
         return keys;
     }
 
     public List<String> getListOfAuthors() {
         System.out.println("dataparser getListOfAuthors");
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             for (String aut : pub.getAuthors()) {
                 hmAuthors.put(aut, aut);
             }
         }
         return getKeysFromHashMap(hmAuthors);
     }
 
     public int getTotalNumberOfDistinctAuthors() {
         return getListOfAuthors().size();
     }
 
     public List<String> getNumberOfAuthorsForEachPaper() {
         List<String> list = new ArrayList<String>();
         for (PublicationBean pub : publications) {
             list.add(pub.getAuthors().size() + "");
         }
         return list;
     }
 
     public List<String> getNumberOfAuthorsForEachPaperOfType(String type) {
         List<String> list = new ArrayList<String>();
         for (PublicationBean pub : publications) {
             if (pub.isOfType(type)) {
                 list.add(pub.getAuthors().size() + "");
             }
         }
         return list;
     }
 
     public List<String> getAuthorListByPublicationType(String type) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (pub.isOfType(type)) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors);
     }
 
     public int getNoOfTypePublicationsByAuthor(String type, String author) {
         int counter = 0;
         for (PublicationBean pub : publications) {
             if (pub.containsAuthor(author) && pub.isOfType(type)) {
                 counter++;
             }
         }
         return counter;
     }
 
     public List<String> getListOfAuthorsWithPublicationCount() {
         List<String> retval = new ArrayList<String>();
         List<String> authornames = this.getListOfAuthors();
         for (String name : authornames) {
             retval.add(name + "(" + this.getTotalNoOfPublicationsByAuthor(name) + ")");
         }
         return retval;
     }
 
     public int getTotalNumberOfPublications() {
         return publications.size();
     }
 
     public int getNumberOfPublicationsInYear(String year) {
         int counter = 0;
         for (PublicationBean pub : publications) {
             if (pub.isInYear(year)) {
                 counter++;
             }
         }
         return counter;
     }
 
     public int getNumberOfDistinctAuthorsInYear(String year) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (pub.isInYear(year)) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors).size();
     }
 
     public int getNumberOfDistinctAuthorsInYears(String[] years) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (Arrays.asList(years).contains(pub.getYear() + "")) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors).size();
     }
 
     public ArrayList<String> getDistinctPublicationYears() {
         HashMap<String, Integer> hmYears = new HashMap<String, Integer>();
         for (PublicationBean pub : publications) {
             hmYears.put(pub.getYear() + "", pub.getYear());
         }
         return getKeysFromHashMap(hmYears);
     }
 
     public ArrayList<String> getDistinctPublicationTypes() {
         HashMap<String, String> hmTypes = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             hmTypes.put(pub.getType(), pub.getType());
         }
 
         return getKeysFromHashMap(hmTypes);
     }
 
     public int getNumberOfPublicationsByType(String type) {
         int counter = 0;
         for (PublicationBean pub : publications) {
             if (pub.isOfType(type)) {
                 counter++;
             }
         }
         return counter;
     }
 
     public int getNumberOfDistinctAuthorsByType(String type) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (pub.isOfType(type)) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors).size();
     }
 
     public int getNumberOfDistinctAuthorsByTypes(String[] types) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (Arrays.asList(types).contains(pub.getType())) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors).size();
     }
 
     public int getNumberOfDistinctAuthorsByTypeFilteredByYears(String type, String[] years) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (pub.isOfType(type) && Arrays.asList(years).contains(pub.getYear() + "")) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors).size();
     }
 
     public int getNumberOfDistinctAuthorsInYearFilteredByTypes(String year, String[] types) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (pub.isInYear(year) && Arrays.asList(types).contains(pub.getType())) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors).size();
     }
 
     public int getNumberOfPublicationsByTypeFilteredByYear(String type, String selectedYear) {
         int counter = 0;
         for (PublicationBean pub : publications) {
             if (pub.isInYear(selectedYear) && pub.isOfType(type)) {
                 counter++;
             }
         }
         return counter;
     }
 
     public int getNumberOfDistinctAuthorsByTypeFilteredByYear(String type, String selectedYear) {
         HashMap<String, String> hmAuthors = new HashMap<String, String>();
         for (PublicationBean pub : publications) {
             if (pub.isInYear(selectedYear) && pub.isOfType(type)) {
                 for (String aut : pub.getAuthors()) {
                     hmAuthors.put(aut, aut);
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors).size();
     }
 
     public int getNumberOfPublicationsByAuthorByType(String type, String authorname) {
         int counter = 0;
         for (PublicationBean pub : publications) {
             if (pub.isOfType(type) && pub.containsAuthor(authorname)) {
                 counter++;
             }
         }
         return counter;
     }
 
     public ArrayList<String> getNumberOfArticlesOfAuthorByType(String authorName) {
         ArrayList<String> retval = new ArrayList<String>();
         for (String type : this.getDistinctPublicationTypes()) {
             retval.add(type);
             retval.add(this.getNumberOfPublicationsByAuthorByType(type, authorName) + "");
         }
         return retval;
     }
 
     public int getTotalNoOfPublicationsByAuthor(String authorName) {
         int counter = 0;
         for (PublicationBean pub : publications) {
             if (pub.containsAuthor(authorName)) {
                 counter++;
             }
         }
         return counter;
     }
     private HashMap<String, ArrayList<String>> getCoAuthorsForAuthor = new HashMap<String, ArrayList<String>>();
 
     public ArrayList<String> getCoAuthorsForAuthor(String authorName) {
         if (!getCoAuthorsForAuthor.containsKey(authorName)) {
             HashMap<String, String> hmAuthors = new HashMap<String, String>();
 
             for (PublicationBean pub : publications) { // for every publication
                 if (pub.containsAuthor(authorName)) {	// if this is a publication of our desired author
                     for (String aut : pub.getAuthors()) { // go through all the authors in the publication
                         if (!aut.equalsIgnoreCase(authorName)) { // and add the ones that are not our author
                             hmAuthors.put(aut, aut);
                         }
                     }
                 }
             }
             getCoAuthorsForAuthor.put(authorName, getKeysFromHashMap(hmAuthors));
         }
 
         //return getKeysFromHashMap(hmAuthors);
         return getCoAuthorsForAuthor.get(authorName);
     }
 
     public int getNumberOfCoAuthorsForAuthor(String authorName) {
         return getCoAuthorsForAuthor(authorName).size();
     }
     
         public ArrayList<String> getCoAuthorsForAuthorInYearsByType(String authorName, ArrayList<Integer> years, String type){
     	HashMap<String, String> hmAuthors = new HashMap<String, String>();
 
         for (PublicationBean pub : publications) { // for every publication
         	if(years.contains(pub.getYear()) && pub.isOfType(type) && pub.containsAuthor(authorName)){ // if this is a publication by desired author, in years, and of type
                 for (String aut : pub.getAuthors()) { // go through all the authors in the publication
                     if (!aut.equalsIgnoreCase(authorName)) { // and add the ones that are not our author
                         hmAuthors.put(aut, aut);
                     }
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors);
     }
     
     public ArrayList<String> getCoAuthorsForAuthorInYears(String authorName, ArrayList<Integer> years){
     	HashMap<String, String> hmAuthors = new HashMap<String, String>();
 
         for (PublicationBean pub : publications) { // for every publication
         	if(years.contains(pub.getYear()) && pub.containsAuthor(authorName)){ // if this is a publication by desired author, in years, and of type
                 for (String aut : pub.getAuthors()) { // go through all the authors in the publication
                     if (!aut.equalsIgnoreCase(authorName)) { // and add the ones that are not our author
                         hmAuthors.put(aut, aut);
                     }
                 }
             }
         }
         return getKeysFromHashMap(hmAuthors);
     }
     
 
     
 
     public int getNumberOfPublicationsTogether(String authorName, String coAuthorName) {
         int counter = 0;
         for (PublicationBean pub : publications) {
             if (pub.containsAuthor(authorName) && pub.containsAuthor(coAuthorName)) {
                 counter++;
             }
         }
         return counter;
     }
 
     //Sprint 3 Codes
     public String getCoAuthorCollaboration(String authorName) {
         String retval = "";
         for (PublicationBean pub : publications) {
             if (pub.containsAuthor(authorName)) {
                 for (String coauth : pub.getAuthors()) {
                     if (!coauth.equals(authorName)) {
                         retval += coauth + ";";
                         retval += getNumberOfPublicationsTogether(authorName, coauth) + ";";
                     }
                 }
             }
         }
         return retval;
 //        String query = "for $x in distinct-values(doc(\"" + xml_file_name + "\")/*/*[author=\"" + authorName + "\"]/author) "
 //                + "return ($x,';',count(index-of(doc(\"" + xml_file_name + "\")/*/*[author=\"" + authorName + "\"]/author,$x)),';')";
 //
 //        return executeQueryAndExtractString(query);
     }
 
     public String getCollaboratePublications(String authorName, String coAuthorName) {
         String retval = "";
         for (PublicationBean pub : publications) {
             if (pub.containsAuthor(authorName) && pub.containsAuthor(coAuthorName)) {
                 retval += pub.getTitle() + ";;";
                 retval += pub.getType() + ";;";
                 retval += pub.getYear() + ";;";
             }
         }
         return retval;
 
 //        String query = "for $x in doc(\"" + xml_file_name + "\")/*/*[author=\"" + authorName + "\" and author=\"" + coAuthorName + "\"] "
 //                + "let $type :=  $x/name() "
 //                + "let $year :=  $x/year/text() "
 //                + "let $title := $x/title "
 //                + "order by $title,$year "
 //                + "return ($title/text(),';;',$type,';;',$year,';;')";
 //
 //        return executeQueryAndExtractString(query);
     }
 
     private String executeQueryAndExtractString(String query) {
         String valueList = "";
         try {
             XQSequence seq = Dataparser.executeXQuery(query);
             valueList = " " + seq.getSequenceAsString(null);
             seq.close();
         } catch (Exception err) {
             System.out.println("Failed while exececuting " + err.getStackTrace()[0].getMethodName() + ": " + err.getMessage());
             System.out.println("Failed query: " + query);
         }
 
         return valueList;
     }
     
     
     
     
 }
