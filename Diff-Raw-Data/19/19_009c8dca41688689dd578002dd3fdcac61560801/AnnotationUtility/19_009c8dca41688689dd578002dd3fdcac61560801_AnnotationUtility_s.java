 package edu.jhu.cvrg.waveform.utility;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
import javax.faces.context.FacesContext;

 import org.xmldb.api.base.Resource;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.base.XMLDBException;
 
 import edu.jhu.cvrg.dbapi.XMLUtility;
 import edu.jhu.cvrg.waveform.utility.AnnotationQueryBuilder;
 import edu.jhu.cvrg.waveform.model.AnnotationData;
import edu.jhu.cvrg.waveform.model.StudyEntry;
 
 public class AnnotationUtility extends XMLUtility {
 
 	private AnnotationQueryBuilder annotationBuilder;
 	/** The flag which separates Definintions and Comments 
 	 * within the "value" node of an annotation in the database. */
 	private String DefCommSeparator= "[comment]";
 	/**
 	 * Default Constructor
 	 * 
 	 * tells the query builder where to find the database URI and collection
 	 */
 	public AnnotationUtility(String userName, String userPassword, String uRI,
 			String driver, String mainDatabase) {
 
 		super(userName, userPassword, uRI, driver, mainDatabase);
		annotationBuilder = new AnnotationQueryBuilder(this.dbURI,
				this.dbMainCollection);
 	}
 
 	public AnnotationUtility() {
 		// TODO Auto-generated constructor stub
 		super();
		annotationBuilder = new AnnotationQueryBuilder(this.dbURI,
				this.dbMainCollection);
 	}
 
 	/**
 	 * Stores the annotation in the AnnotationData bean into the metadata
 	 * storage database.<BR>
 	 * Looks up the userID, subjectID leadName and leadIndex and calls the
 	 * method that does the storing.<BR>
 	 * Calls makeXMLAnnotationNode() to generate the XML.<BR>
 	 * <BR>
 	 * Required setters that need to be used for the AnnotationData object:<BR>
 	 * <BR>
 	 * setUserID()<BR>
 	 * setSubjectID()<BR>
 	 * setLeadName()<BR>
 	 * setLeadIndex()<BR>
 	 * setDatasetName()<BR>
 	 * <BR>
 	 * setCreator()<BR>
 	 * setConceptLabel()<BR>
 	 * setConceptRestURL()<BR>
 	 * setUniqueID()<BR>
 	 * setOnsetLabel()<BR>
 	 * setOnsetRestURL()<BR>
 	 * setMicroVoltStart()<BR>
 	 * setMilliSecondStart()<BR>
 	 * setIsSinglePoint()<BR>
 	 * setOffsetLabel()<BR>
 	 * setOffsetRestLabel()<BR>
 	 * setMicroVoltStart()<BR>
 	 * setMilliSecondStart()<BR>
 	 * setAnnotation()<BR>
 	 * setUnit()<BR>
 	 * 
 	 * @param annData
 	 *            - the bean containing all the necessary data about the
 	 *            annotation
 	 * @return - success/fail for the data storage.
 	 */
 	// FIXME: The method does not check the username on this. Once annotations
 	// can be used again, this must be tested. Otherwise, it will save
 	// the same annotation to all versions of that record for all users! -
 	// 4/30/2013 bbenite1
 	public boolean storeLeadAnnotationNode(AnnotationData annData) {
 		boolean success = false;
 		String sAnnotationXML = "", sUserID, sSubjectID, sLeadName, sRecordName, sStudyID, sXml = "";
 		int iLeadIndex;
 
 		sUserID = annData.getUserID();
 		sSubjectID = annData.getSubjectID();
 		sLeadName = annData.getLeadName();
 		sRecordName = annData.getDatasetName();
 		iLeadIndex = annData.getLeadIndex() + 1; // The code uses a zero based
 													// lead index, but the
 													// database uses a one based
 													// index.
 		sAnnotationXML = makeXMLAnnotationNode(annData);
 
 		sStudyID = annData.getStudyID();
 
 		try {
 			String sForCollection = annotationBuilder.defaultFor();
 			String sWhereClause = annotationBuilder.userRecordSearch(sStudyID,
 					sSubjectID, sRecordName, sUserID);
 
 			sXml = sAnnotationXML;
 
 			String sUpdateClause = annotationBuilder.insertLeadStatement(
 					String.valueOf(iLeadIndex), sXml);
 
 			String sQuery = sForCollection + sWhereClause + sUpdateClause;
 
 			// The EnumCollection enumeration will tell the execute method which
 			// collection to use
 			executeQuery(sQuery);
 
 			success = true;
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return success;
 	}
 
 	// TODO: Write a storePhenoytypeAnnotationNode
 
 	/**
 	 * A wrapper function specifically made for retrieving lead annotations.
 	 * 
 	 * Calls getAnnotationNode
 	 */
 	public AnnotationData[] getLeadAnnotationNode(String sUserID,
 			String sStudyID, String sSubjectID, String sLeadName,
 			int iLeadIndex, String sRecordName) {
 		AnnotationData[] annotations = this.getAnnotationNode(sUserID,
 				sStudyID, sSubjectID, sLeadName, iLeadIndex, sRecordName,
 				false, false);
 
 		return annotations;
 	}
 
 	/**
 	 * A wrapper function specifically made for retrieving phenotype
 	 * annotations.
 	 * 
 	 * Calls getAnnotationNode
 	 */
 	public AnnotationData[] getPhenotypeAnnotationNode(String sUserID,
 			String sStudyID, String sSubjectID, String sLeadName,
 			int iLeadIndex, String sRecordName) {
 		AnnotationData[] annotations = this.getAnnotationNode(sUserID,
 				sStudyID, sSubjectID, sLeadName, iLeadIndex, sRecordName, true,
 				false);
 
 		return annotations;
 	}
 
 	/** Gets a single annotation from the metadata storage database and returns it in
 	 * an AnnotationData bean.  Calls existing method getAnnotationNode(), then discards all other annotations.
 	 * 
 	 * @param sUserID - ID of the Waveform user who is currently logged in.
 	 * @param sStudyID - Selected study, e.g. "Mesa".
 	 * @param sSubjectID - Selected Subject within the Study. e.g. "twa94" 
 	 * @param sLeadName - Best guess at lead name, e.g. "III"
 	 * @param iLeadIndex - zero based lead number. e.g. Lead "III" is at LeadIndex "2".
 	 * @param sRecordName - e.g. "twa94"
 	 * @param sUniqueID - Unique Id of the requested annotation.
 	 * @return an AnnotationData bean
 	 * 
 	 * @author mshipwa1
 	 */
 	public AnnotationData getAnnotationByID(String sUserID, String sStudyID,
 			String sSubjectID, String sLeadName, int iLeadIndex,
 			String sRecordName, String sUniqueID) {
 		
 		AnnotationData ret = null;
 		boolean isPhenotype=false, isComment=false;
 		
 		AnnotationData[] annArray = getAnnotationNode(sUserID, sStudyID,
 			 sSubjectID, sLeadName, iLeadIndex,
 			 sRecordName, isPhenotype, isComment);
 		
 		for(int i=0;i< annArray.length;i++){
 			if( annArray[i].getUniqueID().equals(sUniqueID) ){
 				ret=annArray[i];
 				break;
 			}
 		}
 		return ret;
 	}
 	
 
 	/**
 	 * Gets the annotation(s) from the metadata storage database and puts it into
 	 * the an array of AnnotationData beans.
 	 * 
 	 * @param sUserID - ID of the Waveform user who is currently logged in.
 	 * @param sStudyID - Selected study, e.g. "Mesa".
 	 * @param sSubjectID - Selected Subject within the Study. e.g. "twa94" 
 	 * @param sLeadName - Best guess at lead name, e.g. "III"
 	 * @param iLeadIndex - zero based lead number. e.g. Lead "III" is at LeadIndex "2".
 	 * @param sRecordName - e.g. "twa94"
 	 */
 	private AnnotationData[] getAnnotationNode(String sUserID, String sStudyID,
 			String sSubjectID, String sLeadName, int iLeadIndex,
 			String sRecordName, boolean isPhenotype, boolean isComment) {		
 		AnnotationData[] theAnnotations = new AnnotationData[0];
 
 		try {
 			String sLetClause = "";
 			String sForCollection = annotationBuilder.defaultFor();
 			String sWhereClause = annotationBuilder.userRecordSearch(sStudyID,
 					sSubjectID, sRecordName, sUserID);
 			String sReturnClause = "";
 			String sOrderByClause = "";
 			if (isComment) {
 				sLetClause = annotationBuilder.commentLet();
 				sOrderByClause = annotationBuilder.orderByID();
 				sReturnClause = annotationBuilder.returnCommentAnnotation();
 			} else {
 				// sReturnClause =
 				// annotationBuilder.returnLeadAnnotation(sLeadName);
 				sReturnClause = annotationBuilder.returnLeadAnnotation(String
 						.valueOf(iLeadIndex + 1)); // The code uses a zero based
 													// lead index, but the
 													// database uses a one based
 													// index.
 			}
 
 			String sQuery = sLetClause + sForCollection + sWhereClause + sOrderByClause + sReturnClause;
 			System.out.println("AnnotationUtility.getAnnotationNode(), sQuery:\n\""
 							+ sQuery + "\"");
 			// The EnumCollection enumeration will tell the execute method which
 			// collection to use
 			ResourceSet resultSet = executeQuery(sQuery);
 			ResourceIterator iter = resultSet.getIterator();
 			Resource selection = null;
 
 			long resultSizeLong = resultSet.getSize();
 
 			// While a long can be converted to an int, if the size of it is
 			// bigger than what an int can hold it will be++++++++
 			// a serious problem
 			if (resultSizeLong < Integer.MIN_VALUE
 					|| resultSizeLong > Integer.MAX_VALUE) {
 				throw new IllegalArgumentException(resultSizeLong
 						+ " cannot be cast to int without changing its value.");
 			}
 			System.out.println("AnnotationUtility.getAnnotationNode(), resultSizeLong:\"" + resultSizeLong + "\"");
 
 			int resultSizeInt = (int) resultSizeLong;
 
 			theAnnotations = new AnnotationData[resultSizeInt];
 
 			// initialize the array
 			for (int i = 0; i < theAnnotations.length; i++) {
 				theAnnotations[i] = new AnnotationData();
 
 				// by default, annotations are a single point. Intervals will be
 				// explicitly stated otherwise
 				theAnnotations[i].setIsSinglePoint(true);
 				theAnnotations[i].setIsComment(false);
 			}
 
 			int index = 0;
 
 			// The current version of the regex takes into account any
 			// whitespace that falls through the cracks
 			// group(1) - (\s*) - zero or more whitespaces
 			// group(2) - (<\w+>) - open tag brackets "<wwwww>" with one or more of any word characters inside
 			// group(3) - (.*)	-  zero or more of any characters except newline.
 			// group(4) - (</?\w+>) - close tag brackets "</wwww>" with  one or more of any word characters inside
 			Pattern regex = Pattern.compile("(\\s*)(<\\w+\\>)(.*)(<\\/?\\w+\\>)");
 			
 			while (iter.hasMoreResources()) {
 				// Since the fileInfo retrieval query puts all the HTML tags
 				// into one continuous string, we must
 				// use a regex in order to retrieve each value
 				theAnnotations[index].setAnnotation("unknown");
 
 				selection = iter.nextResource();
 				String resultString = (selection.getContent()).toString();
 
 				// The newline character is used because we can guarantee that
 				// each XML line in the result set will have one in it
 				String[] xmlLine = resultString.split("\n");
 
 				for (int i = 0; i < xmlLine.length; i++) {
 
 					// To describe what the regex is supposed to be looking for:
 					// whitespace (if any), starting XML tag, non-whitespace
 					// characters (if any), end tag
 					// each of the above are captured as groups
 					// This way, only lines with a start and end tag are
 					// processed
 
 					// ********************************************************************************************************************
 					// FIXME: Replace this whole thing with a nested set of
 					// XQueries parse the values that way.
 					// It will be cleaner and more flexible once working.
 					// Check the returnCommentBlock() method in the
 					// AnnotationQueryBuilder for an example of how this can be
 					// done
 					// A similar idea can be applied for lead annotations
 					// In addition, it will guarantee that all the values needed
 					// for an annotation are retrieved properly
 					// This regex method will do at the moment but is not a long
 					// term solution
 					// ********************************************************************************************************************
 
 
 //					System.out.println(xmlLine[i]);
 					Matcher theMatches = regex.matcher(xmlLine[i]);
 					boolean matchSuccess = theMatches.matches();
 					
 					if (matchSuccess) {
 						String xmlTag = theMatches.group(2);
 						String xmlContent = theMatches.group(3);
 						// Now check based on what the tag itself is. There are
 						// only certain ones we want to capture
 						//System.out.println("----- theMatches.group(1):" + theMatches.group(1) + "(2):" + sTag + " (3):" + xmlContent );
 						if (xmlTag.equals("<term>")) {
 							// will need a bunch of cases to check which line
 							// this is, based on the index
 							// not a great solution but it will do for now
 
 							// The things being kept track of are the bioportal
 							// references for the ENTIRE annotation first, and
 							// THEN the onset
 							// label, THEN the offset label.
 
 							switch (i) {
 							// concept label/bioPortalReference
 							case 3:
 								theAnnotations[index].setConceptLabel(xmlContent);
 								if ((theAnnotations[index].getConceptLabel()).equals("comment")) {
 									theAnnotations[index].setIsComment(true);
 								}
 								break;
 							// onset label
 							case 9:
 								theAnnotations[index].setOnsetLabel(xmlContent);
 								break;
 							case 19:
 								// offset label
 								theAnnotations[index].setOffsetLabel(xmlContent);
 
 								// indicates an interval
 								theAnnotations[index].setIsSinglePoint(false);
 								break;
 							default:
 								System.out.println("The bioportal term "
 										+ xmlContent
 										+ " is not being used at this time");
 								break;
 							}
 						}
 						// since we will need the createdBy and ID tags anyway,
 						// just get them
 
 						// createdBy could be used as the algorithm identifier
 						// from an analysis, "manual" for manually entered
 						// annotations,
 						// or the userID that submitted a comment
 						else if (xmlTag.equals("<createdBy>")) {
 							theAnnotations[index].setCreator(theMatches
 									.group(3));
 						}
 						// The unique ID will usually be a timestamp. However,
 						// it may be formatted to a human readable for in the
 						// case of a comment
 						else if (xmlTag.equals("<ID>")) {
 //							System.out.println("AnnotationUtility.getAnnotationNode(), for annotation at index "
 //											+ index
 //											+ ", the ID = "
 //											+ xmlContent);
 							theAnnotations[index].setUniqueID(xmlContent);
 						} else if (xmlTag.equals("<yCoordinate>")) {
 
 							switch (i) {
 							// onset
 							case 13:
 								String startVoltString = xmlContent;
 								double startVolt = Double
 										.parseDouble(startVoltString);
 								theAnnotations[index]
 										.setMicroVoltStart(startVolt);
 								break;
 							// offset
 							case 23:
 								String endVoltString = xmlContent;
 								double endVolt = Double
 										.parseDouble(endVoltString);
 								theAnnotations[index].setMicroVoltEnd(endVolt);
 								break;
 							default:
 								String startVoltStringDefault = theMatches
 										.group(3);
 								double startVoltDefault = Double
 										.parseDouble(startVoltStringDefault);
 								theAnnotations[index]
 										.setMicroVoltStart(startVoltDefault);
 								break;
 							}
 						} else if (xmlTag.equals("<tCoordinate>")) {
 
 							switch (i) {
 							// onset
 							case 14:
 								String startMilliString = xmlContent;
 								double startMilli = Double.parseDouble(startMilliString);
 								theAnnotations[index].setMilliSecondStart(startMilli);
 								break;
 							// offset
 							case 24:
 								String endMilliString = xmlContent;
 								double endMilli = Double.parseDouble(endMilliString);
 								theAnnotations[index].setMilliSecondEnd(endMilli);
 								break;
 							default:
 								String startMilliStringDefault = xmlContent;
 								double startMilliDefault = Double.parseDouble(startMilliStringDefault);
 								theAnnotations[index].setMilliSecondStart(startMilliDefault);
 								break;
 							}
 						} else if (xmlTag.equals("<value>")) {
 //							System.out.println("for annotation at index " + index 
 //									+ ", the value = " + xmlContent);
 							int commentIndex = xmlContent.indexOf(DefCommSeparator);
 							if(commentIndex!=(-1)){
 								// manually entered comment is after the "[comment]" flag.
 								theAnnotations[index].setComment(xmlContent.substring(commentIndex+DefCommSeparator.length()));
 								// Ontology definition is before the "[comment]" flag.
 								xmlContent = xmlContent.substring(0,commentIndex); 
 							}else{
 								theAnnotations[index].setComment("");
 							}
 //							System.out.println("Comment found at index: " + commentIndex + ", " + theAnnotations[index].getComment());
 							theAnnotations[index].setAnnotation(xmlContent);
 						} 
 					}
 				}
 
 				index++;
 
 				// Avoid an ArrayOutOfBounds error
 				if (index >= resultSizeInt) {
 					break;
 				}
 
 			}
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return theAnnotations;
 	}
 
 	/**
 	 * Gets an array of the counts of annotations from the metadata storage
 	 * database on a per lead basis.
 	 * 
 	 * @return - a comma separated list with the following columns<BR>
 	 *         Lead number(term), Total annotation count, Manual annotations
 	 *         count, Automated annotation count..
 	 */
 	public int[][] getAnnotationCountPerLead(String sUserID, String sStudyID,
 			String sSubjectID, String sRecordName) {
 		int[][] iaAnnPerLead = null;
 		try {
 			String sForCollection = annotationBuilder.defaultFor();
 			String sWhereClause = annotationBuilder.userRecordSearch(sStudyID,
 					sSubjectID, sRecordName, sUserID);
 			String sOrderByClause = "";
 			String sReturnClause = annotationBuilder
 					.returnLeadAnnotationCount();
 
 			String sQuery = sForCollection + sWhereClause + sOrderByClause
 					+ sReturnClause;
			System.out.println(sQuery);
 			// The EnumCollection enumeration will tell the execute method which
 			// collection to use
 			ResourceSet resultSet = executeQuery(sQuery);
 			ResourceIterator iter = resultSet.getIterator();
 			Resource selection = null;
 
 			long resultSizeLong = resultSet.getSize();
 
 			// While a long can be converted to an int, if the size of it is
 			// bigger than what an int can hold it will be
 			// a serious problem
 			if (resultSizeLong < Integer.MIN_VALUE
 					|| resultSizeLong > Integer.MAX_VALUE) {
 				throw new IllegalArgumentException(resultSizeLong
 						+ " cannot be cast to int without changing its value.");
 			}
 
 			int resultSizeInt = (int) resultSizeLong;
 
 			iaAnnPerLead = new int[resultSizeInt][4];
 
 			int index = 0;
 
 			while (iter.hasMoreResources()) {
 				// Since the fileInfo retrieval query puts all the HTML tags
 				// into one continuous string, we must
 				// use a regex in order to retrieve each value
 
 				selection = iter.nextResource();
 				String csvString = (selection.getContent()).toString();
 				String[] saLine = csvString.split(",");
 				iaAnnPerLead[index][0] = Integer.parseInt(saLine[0]); // lead <term> e.g. ones based lead number from 1 to 12.
 				iaAnnPerLead[index][1] = Integer.parseInt(saLine[1]); // Total annotation count (should be equal to [2] + [3])
 				iaAnnPerLead[index][2] = Integer.parseInt(saLine[2]); // Manual annotation count
 				iaAnnPerLead[index][3] = Integer.parseInt(saLine[3]); // Automated  annotation count
 
 				index++;
 
 				// Avoid an ArrayOutOfBounds error
 				if (index >= resultSizeInt) {
 					break;
 				}
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return iaAnnPerLead;
 	}
 
 	/**
 	 * Converts the data in the AnnotationData bean into XML format for
 	 * insertion into the XML database.
 	 * 
 	 * Required setters that need to be used for the AnnotationData object:
 	 * 
 	 * setUserID(); setSubjectID(); setLeadName(); setLeadIndex();
 	 * setDatasetName();
 	 * 
 	 * setCreator() setConceptLabel() setConceptRestURL() setUniqueID()
 	 * setOnsetLabel() setOnsetRestURL() setMicroVoltStart()
 	 * setMilliSecondStart() setIsSinglePoint() setOffsetLabel()
 	 * setOffsetRestLabel() setMicroVoltStart() setMilliSecondStart()
 	 * setAnnotation() setUnit()
 	 * 
 	 * @param annData
 	 *            - the bean containing all the necessary data about the
 	 *            annotation
 	 * @return - XML for the annotation node.
 	 */
 	public String makeXMLAnnotationNode(AnnotationData annData) {
 		String node = "";
 //		String bioportalUrl = "http://bioportal.bioontology.org/ontologies/45835";
 		// String ms = String.valueOf(java.lang.System.currentTimeMillis());
 
 //		boolean aSinglePoint = annData.getIsSinglePoint();
 
 		// System.out.println("isSinglePoint = " + aSinglePoint);
 
 		node += "<annotation>\n";
 		node += "  <createdBy>" + annData.getCreator() + "</createdBy>";
 		node += "  <bioportalReference>\n";
 		node += "    <term>" + annData.getConceptLabel() + "</term>\n"; // One
 																		// of
 																		// the
 																		// items
 																		// we
 																		// need
 																		// displayed
 																		// for
 																		// now
 		node += "    <bioportalUrl>" + annData.getConceptRestURL()
 				+ "</bioportalUrl>\n";
 		node += "  </bioportalReference>\n";
 		node += "  <ID>" + annData.getUniqueID() + "</ID>\n";
 		if (!(annData.getIsComment())) {
 			// ******* Onset data *****************
 			node += "  <onset>\n";
 			node += "    <bioportalReference>\n";
 			node += "      <term>" + annData.getOnsetLabel() + "</term>\n"; // 2nd
 																			// item
 																			// to
 																			// display
 																			// for
 																			// now
 			node += "      <bioportalUrl>" + annData.getOnsetRestURL()
 					+ "</bioportalUrl>\n";
 			node += "    </bioportalReference>\n";
 			node += "    <position>\n";
 			node += "      <yCoordinate>" + annData.getMicroVoltStart()
 					+ "</yCoordinate>\n"; // third item to display for now
 			node += "      <tCoordinate>" + annData.getMilliSecondStart()
 					+ "</tCoordinate>\n"; // fourth item to display for now
 			node += "    </position>\n";
 			node += "  </onset>\n";
 			// ******* Offset data *****************
 			if (!(annData.getIsSinglePoint())) {
 				node += "  <offset>\n";
 				node += "    <bioportalReference>\n";
 				node += "      <term>" + annData.getOffsetLabel() + "</term>\n";
 				node += "      <bioportalUrl>" + annData.getOffsetRestURL()
 						+ "</bioportalUrl>\n";
 				node += "    </bioportalReference>\n";
 				node += "    <position>\n";
 				node += "      <yCoordinate>" + annData.getMicroVoltEnd()
 						+ "</yCoordinate>\n";
 				node += "      <tCoordinate>" + annData.getMilliSecondEnd()
 						+ "</tCoordinate>\n";
 				node += "    </position>\n";
 				node += "  </offset>\n";
 			}
 			// *******
 		}
 		// Full annotation text, can also be used as a value for phenotypes
 		String valueText = annData.getAnnotation();
 		if(!annData.getComment().isEmpty()){
 			// manually entered comment is stored after the Full annotation text
 			//  and preceded by the "[comment]" flag in the value node.
 			valueText += DefCommSeparator + annData.getComment();
 		}
 		node += "  <value>" + valueText + "</value>\n"; 
 		if (!annData.getIsComment()) {
 			node += "  <measurementUnit>" + annData.getUnit()
 					+ "</measurementUnit>";
 		}
 		node += "</annotation>\n";
 
 		return node;
 	}
 
 	/**
 	 * Creates a new AnnotationData object with many of the values filled in
 	 * with values from userModel and StudyEntry.<BR>
 	 * Required values that need to be filled in are:<BR>
 	 * <BR>
 	 * created by (x) - the source of this annotation (whether it came from an
 	 * algorithm or was entered manually)<BR>
 	 * concept label - the type of annotation as defined in the annotation's
 	 * bioportal reference term<BR>
 	 * annotation ID - a unique ID used for easy retrieval of the annotation in
 	 * the database<BR>
 	 * onset label - the bioportal reference term for the onset position. This
 	 * indicates the start point of an interval<BR>
 	 * or the location of a single point<BR>
 	 * onset y-coordinate - the y coordinate for that point on the ECG wave.<BR>
 	 * onset t-coordinate - the t coordinate for that point on the ECG wave.<BR>
 	 * an "isInterval" boolean - for determining whether this is an interval
 	 * (and thus needs an offset tag)<BR>
 	 * Full text description - This is the "value" so to speak, and contains the
 	 * full definition of the annotation type being used<BR>
 	 * <BR>
 	 * Note: If this is an interval, then an offset label, y-coordinate, and
 	 * t-coordinate are required for that as well.<BR>
 	 * 
 	 * @return
 	 */
 	public AnnotationData createAnnotationData() {
 		// FacesContext context = FacesContext.getCurrentInstance();
 		// UserModel userBean = (UserModel)
 		// context.getApplication().evaluateExpressionGet(context,
 		// "#{userModel}", UserModel.class);
 		// StudyEntry studyEntry = (StudyEntry)
 		// context.getApplication().evaluateExpressionGet(context,
 		// "#{StudyEntry}", StudyEntry.class);
 		AnnotationData annotationToInsert = new AnnotationData();
 		String ms = String.valueOf(java.lang.System.currentTimeMillis()); // used
 																			// for
 																			// GUID
 
 		annotationToInsert.setUniqueID(ms);
 		// annotationToInsert.setUserID(userBean.getUsername());
 		// annotationToInsert.setSubjectID(studyEntry.getSubjectID());
 		// annotationToInsert.setDatasetName(studyEntry.getRecordName());
 		annotationToInsert.setConceptRestURL("");
 		annotationToInsert.setOnsetLabel("Onset");
 		annotationToInsert.setOnsetRestURL("");
 		annotationToInsert.setOffsetLabel("Offset");
 		annotationToInsert.setOffsetRestURL("");
 		annotationToInsert.setUnit("");
 
 		return annotationToInsert;
 	}
 
 	public boolean storeComment(AnnotationData annData) {
 		boolean success = false;
 
 		// assumes that the flag for determining whether it is a comment has
 		// already been set to true
 		try {
 			DateFormat displayFormat = new SimpleDateFormat(
 					"MM.dd.yyyy 'at' HH:mm:ss");
 			Calendar theCalendar = Calendar.getInstance();
 			Date currentTime = theCalendar.getTime();
 			String analysisDate = displayFormat.format(currentTime);
 
 			annData.setUniqueID(analysisDate);
 
 			String sAnnotationXML = makeXMLAnnotationNode(annData);
 
 			String sSubjectID = annData.getSubjectID();
 			String sStudyID = annData.getStudyID();
 			String sRecordName = annData.getDatasetName();
 			String sUserID = annData.getUserID();
 
 			String sForCollection = annotationBuilder.defaultFor();
 			String sWhereClause = annotationBuilder.userRecordSearch(sStudyID,
 					sSubjectID, sRecordName, sUserID);
 
 			String sUpdateClause = annotationBuilder
 					.insertCommentStatement(sAnnotationXML);
 
 			String query = sForCollection + sWhereClause + sUpdateClause;
 
			System.out
					.println("The query for storing comments to be executed is:  "
 							+ query);
 			executeQuery(query);
 
 			success = true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return success;
 	}
 
 	/**
 	 * Will find all comments for a given record.
 	 * 
 	 * WARNING: It will be deprecated once the new queries for annotations are
 	 * complete for all annotation types. See fetchComments() for more details
 	 * 
 	 * @param sUserID
 	 * @param sStudyID
 	 * @param sSubjectID
 	 * @param sRecordName
 	 * @return - An array of comment annotations for use on the 12-lead display
 	 */
 	public ArrayList<AnnotationData> getComments(String sUserID,
 			String sStudyID, String sSubjectID, String sRecordName) {
 		AnnotationData[] annotations = this.getAnnotationNode(sUserID,
 				sStudyID, sSubjectID, "", -1, sRecordName, false, true);
 
 		// a safe way to turn the array into an array list
 		ArrayList<AnnotationData> annotationsList = new ArrayList<AnnotationData>(
 				Arrays.asList(annotations));
 
 		return annotationsList;
 	}
 
 	/**
 	 * This version will also find all comments for a given record. However, the
 	 * query used will provide a formatted String output rather than raw XML.
 	 * This is done as a test prototype. If this function is successful, lead
 	 * annotations will be redone based off of this function as well, removing
 	 * the need for parsing the XML output altogether (which is currently how it
 	 * works).
 	 * 
 	 * @param sUserID
 	 * @param sStudyID
 	 * @param sSubjectID
 	 * @param sRecordName
 	 * @return
 	 */
 	public ArrayList<String> fetchComments(String sUserID, String sStudyID,
 			String sSubjectID, String sRecordName) {
 		ArrayList<String> comments = new ArrayList<String>();
 
 		String sLetClause = "";
 		String sForCollection = annotationBuilder.defaultFor();
 		String sWhereClause = annotationBuilder.userRecordSearch(sStudyID,
 				sSubjectID, sRecordName, sUserID);
 		String sReturnClause = "";
 		String sOrderByClause = "";
 		sLetClause = annotationBuilder.commentLet();
 		sOrderByClause = annotationBuilder.orderByID();
 		sReturnClause = annotationBuilder.returnCommentBlock();
 
 		String sQuery = sLetClause + sForCollection + sWhereClause
 				+ sOrderByClause + sReturnClause;
 		// The EnumCollection enumeration will tell the execute method which
 		// collection to use
 		try {
 			ResourceSet resultSet = executeQuery(sQuery);
 			ResourceIterator iter;
 
 			iter = resultSet.getIterator();
 
 			Resource selection = null;
 
 			while (iter.hasMoreResources()) {
 				// Since the fileInfo retrieval query puts all the HTML tags
 				// into one continuous string, we must
 				// use a regex in order to retrieve each value
 
 				selection = iter.nextResource();
 				String commentString = (selection.getContent()).toString();
 
 				comments.add(commentString);
 			}
 		} catch (XMLDBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return comments;
 
 	}
 
 	/**
 	 * This version will also find all comments for a given record. However, the
 	 * query used will provide a formatted String output rather than raw XML.
 	 * This is done as a test prototype. If this function is successful, lead
 	 * annotations will be redone based off of this function as well, removing
 	 * the need for parsing the XML output altogether (which is currently how it
 	 * works).
 	 * 
 	 * @param sUserID
 	 * @param sStudyID
 	 * @param sSubjectID
 	 * @param sRecordName
 	 * @return
 	 */
 	public ArrayList<String> fetchLeadAnnotationList(String sUserID,
 			String sStudyID, String sSubjectID, String sLeadName,
 			int iLeadIndex, String sRecordName, boolean isPhenotype,
 			boolean isComment) {
 		ArrayList<String> comments = new ArrayList<String>();
 
 		String sLetClause = annotationBuilder.commentLet();
 		String sForCollection = annotationBuilder.defaultFor();
 		String sWhereClause = annotationBuilder.userRecordSearch(sStudyID,
 				sSubjectID, sRecordName, sUserID);
 		String sOrderByClause = annotationBuilder.orderByID();
 		String sReturnClause = annotationBuilder
 				.returnLeadAnnotationBlock(sLeadName);
 
 		String sQuery = sLetClause + sForCollection + sWhereClause
 				+ sOrderByClause + sReturnClause;
 		// The EnumCollection enumeration will tell the execute method which
 		// collection to use
 		try {
 			ResourceSet resultSet = executeQuery(sQuery);
 			ResourceIterator iter;
 
 			iter = resultSet.getIterator();
 
 			Resource selection = null;
 
 			while (iter.hasMoreResources()) {
 				// Since the fileInfo retrieval query puts all the HTML tags
 				// into one continuous string, we must
 				// use a regex in order to retrieve each value
 
 				selection = iter.nextResource();
 				String commentString = (selection.getContent()).toString();
 
 				comments.add(commentString);
 			}
 		} catch (XMLDBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return comments;
 
 	}
 
 	public static String escapeCharacters(String userInput) {
 
 		// TODO: In the future, it may be easier to just use a library, such as
 		// the Apache Commons Lang one, to do this
 
 		userInput = userInput.replaceAll("\"", "&quot");
 		userInput = userInput.replaceAll("'", "&apos");
 		userInput = userInput.replaceAll("<", "&lt");
 		userInput = userInput.replaceAll(">", "&gt");
 		userInput = userInput.replaceAll("&", "&amp");
 
 		return userInput;
 	}
 
 }
