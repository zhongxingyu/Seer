 package dk.statsbiblioteket.doms.ingest.reklamepbcoremapper;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import dk.statsbiblioteket.util.xml.DOM;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 
 import javax.xml.transform.TransformerException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.regex.Pattern;
 
 /**
  * Map all biografreklamefilm data to PBCore.
  */
 public class BiografPBCoreMapper {
     private static final SimpleDateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddZ");
     private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("HH:mm:ss");
     static {
         DURATION_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
     private static final XPathSelector XPATH_SELECTOR = DOM
             .createXPathSelector("p", "http://www.pbcore.org/PBCore/PBCoreNamespace.html");
 
     private static final String SQL_QUERY_AD = "SELECT "
             + "ad.AdID,"                                                                                             // 1
             + "ad.title,"                                                                                            // 2
             + "ad.titleAlternative,"                                                                                 // 3
             + "ad.subject,"                                                                                          // 4
             + "substring(ad.description,0,charindex('$',ad.description)) AS description1,"                           // 5
             + "substring(ad.description,charindex('$',description) + 1,char_length(ad.description)) AS description2,"// 6
             + "ad.descriptionExt,"                                                                                   // 7
             + "ad.dateCensorship,"                                                                                   // 8
             + "ad.datePremiere,"                                                                                     // 9
             + "ad.formatExtentDigital,"                                                                              //10
             + "ad.formatExtentAnalogue,"                                                                             //11
             + "ad.identifierCensorshipCard,"                                                                         //12
             + "ad.identifierCensorshipCardNo,"                                                                       //13
             + "ad.registrant,"                                                                                       //14
             + "ad.registerDate,"                                                                                     //15
             + "ad.lastModifiedBy,"                                                                                   //16
             + "ad.lastModified,"                                                                                     //17
             + "ad.recordIDcensor,"                                                                                   //18
             + "ad.fileName,"                                                                                         //19
             + "ad.covSpatial "                                                                                       //20
             + "FROM Advertisement ad "
             + "WHERE ad.fileName != NULL";
 
     private static final String SQL_QUERY_SUBJECT_KEYWORD = "SELECT "
             + "subject.word "
             + "FROM SubjectKeyword subject, AdvSubjectKeyword "
             + "WHERE AdvSubjectKeyword.AdID=?"
             + "  AND AdvSubjectKeyword.subjectKeywordID = subject.subjectKeywordID";
 
     private static final String SQL_QUERY_SUBJECT_REKLAMEFILM = "SELECT "
             + "child.word as subjectChild,"
             + "parent.word as subjectParent "
             + "FROM SubjectReklamefilm child, SubjectReklamefilm parent, AdvSubjectReklamefilm "
             + "WHERE AdvSubjectReklamefilm.AdID=?"
             + "  AND AdvSubjectReklamefilm.subjectReklameID = child.subjectReklameID"
             + "  AND child.parentID = parent.subjectReklameID";
 
     private static final String SQL_QUERY_DECADE = "SELECT "
             + "decade.decade "
             + "FROM Decade decade, AdvDecade "
             + "WHERE AdvDecade.AdID=?"
             + "  AND AdvDecade.decadeID = decade.decadeID";
 
     private static final String SQL_QUERY_LANGUAGE = "SELECT "
             + "lan.abbreviation "
             + "FROM Language lan, AdvLanguage "
             + "WHERE AdvLanguage.AdID=? "
             + "AND lan.languageID=AdvLanguage.languageID";
 
     private static final String SQL_QUERY_CONTRIBUTOR = "SELECT conrole.description, con.name FROM Contributor con, ContributorRole conrole, AdvContributor "
             + "WHERE con.contributorID=AdvContributor.contributorID "
             + "AND conrole.contributorRoleID = con.contributorRoleID "
             + "AND AdvContributor.AdID=?";
 
     private static final String SQL_QUERY_CREATOR = "SELECT crerole.description, cre.name FROM Creator cre, CreatorRole crerole, AdvCreator "
             + "WHERE cre.creatorID=AdvCreator.creatorID "
             + "AND crerole.creatorRoleID = cre.creatorRoleID "
             + "AND AdvCreator.AdID=?";
 
     //TODO: What to do with audit data (registrant etc.) 13,14,15,16
 
     private static class MappingTuple {
         enum Type {STRING, INT, DURATION, DATE, FILE, EXTENSIONCENSORCARDDATA1, EXTENSIONCENSORCARDDATA2, EXTENSIONCENSORCARDDATA3, EXTENSIONCENSORDATE, EXTENSIONCENSORESTIMATEDREELLENGTH, EXTENSIONCENSORCARD}
 
         final int resultindex;
         final String xpath;
         final Type type;
         boolean parent;
 
         private MappingTuple(int resultindex, String xpath, Type type, boolean parent) {
             this.resultindex = resultindex;
             this.xpath = xpath;
             this.type = type;
             this.parent = parent;
         }
     }
 
     private final List<MappingTuple> pbcoreBiografTemplateMappingTuples = new ArrayList<MappingTuple>(Arrays.asList(
             new MappingTuple(1, "/p:PBCoreDescriptionDocument/p:pbcoreIdentifier[1]/p:identifier",
                              MappingTuple.Type.INT, true),
             new MappingTuple(1, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:pbcoreFormatID/p:formatIdentifier",
                              MappingTuple.Type.INT, true),
             new MappingTuple(2, "/p:PBCoreDescriptionDocument/p:pbcoreTitle[1]/p:title",
                              MappingTuple.Type.STRING, true),
             new MappingTuple(3, "/p:PBCoreDescriptionDocument/p:pbcoreTitle[2]/p:title",
                              MappingTuple.Type.STRING, true),
             new MappingTuple(4, "/p:PBCoreDescriptionDocument/p:pbcoreSubject[4]/p:subject",
                              MappingTuple.Type.STRING, true),
             new MappingTuple(5, "/p:PBCoreDescriptionDocument/p:pbcoreExtension[1]/p:extension",
                              MappingTuple.Type.EXTENSIONCENSORCARDDATA1, true),
             new MappingTuple(6, "/p:PBCoreDescriptionDocument/p:pbcoreExtension[2]/p:extension",
                              MappingTuple.Type.EXTENSIONCENSORCARDDATA2, true),
             new MappingTuple(7, "/p:PBCoreDescriptionDocument/p:pbcoreExtension[3]/p:extension",
                              MappingTuple.Type.EXTENSIONCENSORCARDDATA3, true),
             new MappingTuple(8, "/p:PBCoreDescriptionDocument/p:pbcoreExtension[4]/p:extension",
                              MappingTuple.Type.EXTENSIONCENSORDATE, true),
             new MappingTuple(9, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:dateIssued",
                              MappingTuple.Type.DATE, false),
             new MappingTuple(10, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:formatDuration",
                              MappingTuple.Type.DURATION, false),
             new MappingTuple(11, "/p:PBCoreDescriptionDocument/p:pbcoreExtension[5]/p:extension",
                              MappingTuple.Type.EXTENSIONCENSORESTIMATEDREELLENGTH, true),
             new MappingTuple(12, "/p:PBCoreDescriptionDocument/p:pbcoreIdentifier[3]/p:identifier",
                              MappingTuple.Type.STRING, true),
             new MappingTuple(13, "/p:PBCoreDescriptionDocument/p:pbcoreIdentifier[2]/p:identifier",
                              MappingTuple.Type.INT, true),
             new MappingTuple(18, "/p:PBCoreDescriptionDocument/p:pbcoreExtension[6]/p:extension",
                              MappingTuple.Type.EXTENSIONCENSORCARD, true),
             new MappingTuple(19, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:formatLocation",
                              MappingTuple.Type.FILE, false),
             new MappingTuple(20, "/p:PBCoreDescriptionDocument/p:pbcoreCoverage[1]/p:coverage",
                              MappingTuple.Type.STRING, true)));
 
 
     public void mapSQLDataToPBCoreFiles(File outputdir, Connection c) throws SQLException, ClassNotFoundException {
         Statement statement = c.createStatement();
         statement.execute(SQL_QUERY_AD);
         ResultSet resultSet = statement.getResultSet();
         mapSQLDataToPBCoreFiles(resultSet, outputdir, c);
     }
 
     private void mapSQLDataToPBCoreFiles(ResultSet resultSet, File outputdir, Connection c) throws SQLException {
         while (resultSet.next()) {
             try {
                 mapResultSetToPBCoreFile(resultSet, outputdir, c);
             } catch (Exception e) {
                 //TODO logging
                 e.printStackTrace(System.err);
             }
         }
     }
 
     private void mapResultSetToPBCoreFile(ResultSet resultSet, File outputdir, Connection c)
             throws ParseException, IOException, TransformerException, SQLException {
         // Initiate pbcore template
         Document pbcoreDocument = DOM
                 .streamToDOM(getClass().getClassLoader().getResourceAsStream("pbcorebiograftemplate.xml"), true);
         // Inject data
         List<Node> nodesToDelete = new ArrayList<Node>();
         for (MappingTuple pbcoreBiografTemplateMappingTuple : pbcoreBiografTemplateMappingTuples) {
             String value;
             switch (pbcoreBiografTemplateMappingTuple.type) {
                 case STRING:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     break;
                 case DATE:
                     Date date = resultSet.getDate(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = date == null ? null : OUTPUT_DATE_FORMAT.format(date);
                     break;
                 case DURATION:
                     int duration = resultSet.getInt(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = DURATION_FORMAT.format(new Date(duration * 1000L));
                     break;
                 case INT:
                     int number = resultSet.getInt(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = Integer.toString(number);
                     break;
                 case FILE:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex).replaceAll(Pattern.quote("+"), " ");
                     break;
                 case EXTENSIONCENSORCARDDATA1:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = value == null ? null : "censorcarddata1: "  + value;
                     break;
                 case EXTENSIONCENSORCARDDATA2:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = value == null ? null : "censorcarddata2: "  + value;
                     break;
                 case EXTENSIONCENSORCARDDATA3:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = value == null ? null : "censorcarddata3: "  + value;
                     break;
                 case EXTENSIONCENSORDATE:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = value == null ? null : "censordate: "  + value;
                     break;
                 case EXTENSIONCENSORESTIMATEDREELLENGTH:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = value == null ? null : "censorestimatedreellength: "  + value;
                     break;
                 case EXTENSIONCENSORCARD:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     value = value == null ? null : "censorcard: "  + value;
                     break;
                 default:
                     value = resultSet.getString(pbcoreBiografTemplateMappingTuple.resultindex);
                     break;
             }
             Node node = XPATH_SELECTOR.selectNode(pbcoreDocument, pbcoreBiografTemplateMappingTuple.xpath);
             if (value != null) {
                 node.setTextContent(value);
             } else {
                 if (pbcoreBiografTemplateMappingTuple.parent) {
                     nodesToDelete.add(node.getParentNode());
                 } else {
                     nodesToDelete.add(node);
                 }
             }
         }
 
         int adID = resultSet.getInt(1);
 
         //subject reklamefilm
         PreparedStatement subjectStatement = c.prepareStatement(SQL_QUERY_SUBJECT_REKLAMEFILM);
         subjectStatement.setInt(1, adID);
         subjectStatement.execute();
         ResultSet subjects = subjectStatement.getResultSet();
         Node subjectNode = XPATH_SELECTOR
                 .selectNode(pbcoreDocument, "/p:PBCoreDescriptionDocument/p:pbcoreSubject[2]/p:subject");
         Node subjectNode2 = XPATH_SELECTOR
                 .selectNode(pbcoreDocument, "/p:PBCoreDescriptionDocument/p:pbcoreSubject[3]/p:subject");
         if (subjects.next()) {
             String subjectString = subjects.getString(1);
             if (subjectString.isEmpty()) {
                nodesToDelete.add(subjectNode);
             } else {
                 subjectNode.setTextContent(subjectString);
             }
             String subjectString2 = subjects.getString(2);
             if (subjectString2.isEmpty()) {
                nodesToDelete.add(subjectNode2);
             } else {
                 subjectNode2.setTextContent(subjectString2);
             }
         } else {
            nodesToDelete.add(subjectNode);
            nodesToDelete.add(subjectNode2);
         }
         subjectStatement.close();
 
         //subject keyword
         PreparedStatement subjectKeywordStatement = c.prepareStatement(SQL_QUERY_SUBJECT_KEYWORD);
         subjectKeywordStatement.setInt(1, adID);
         subjectKeywordStatement.execute();
         ResultSet subjectKeywords = subjectKeywordStatement.getResultSet();
         Node subjectKeywordNode = XPATH_SELECTOR
                 .selectNode(pbcoreDocument, "/p:PBCoreDescriptionDocument/p:pbcoreSubject[1]/p:subject");
         if (subjectKeywords.next()) {
             String subjectKeywordString = subjectKeywords.getString(1);
             if (subjectKeywordString.isEmpty()) {
                 nodesToDelete.add(subjectKeywordNode);
             } else {
                 subjectKeywordNode.setTextContent(subjectKeywordString);
             }
         } else {
             nodesToDelete.add(subjectKeywordNode);
         }
         subjectKeywordStatement.close();
 
         //decade
         PreparedStatement decadeStatement = c.prepareStatement(SQL_QUERY_DECADE);
         decadeStatement.setInt(1, adID);
         decadeStatement.execute();
         ResultSet decades = decadeStatement.getResultSet();
         Node decadeNode = XPATH_SELECTOR
                 .selectNode(pbcoreDocument, "/p:PBCoreDescriptionDocument/p:pbcoreCoverage[2]/p:coverage");
         if (decades.next()) {
             String decadeString = decades.getString(1);
             if (decadeString.isEmpty()) {
                 nodesToDelete.add(decadeNode);
             } else {
                 decadeNode.setTextContent(decadeString);
             }
         } else {
             nodesToDelete.add(decadeNode);
         }
         decadeStatement.close();
 
         //languages
         PreparedStatement langStatement = c.prepareStatement(SQL_QUERY_LANGUAGE);
         langStatement.setInt(1, adID);
         langStatement.execute();
         ResultSet languages = langStatement.getResultSet();
         Node langNode = XPATH_SELECTOR
                 .selectNode(pbcoreDocument, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:language");
         String languageString = "";
         while (languages.next()) {
             if (!languageString.isEmpty()) {
                 languageString = languageString + ";";
             }
             languageString = languageString + languages.getString(1);
         }
         if (languageString.isEmpty()) {
             nodesToDelete.add(langNode);
         } else {
             langNode.setTextContent(languageString);
         }
         langStatement.close();
 
         //creators & contributors
         Node createTemplateNode = XPATH_SELECTOR.selectNode(pbcoreDocument, "/p:PBCoreDescriptionDocument/p:pbcoreCreator");
         Node contributorTemplateNode = XPATH_SELECTOR.selectNode(pbcoreDocument, "/p:PBCoreDescriptionDocument/p:pbcoreContributor");
         nodesToDelete.add(createTemplateNode);
         nodesToDelete.add(contributorTemplateNode);
 
         PreparedStatement creatorStatement = c.prepareStatement(SQL_QUERY_CREATOR);
         creatorStatement.setInt(1, adID);
         creatorStatement.execute();
         ResultSet creators = creatorStatement.getResultSet();
         while (creators.next()) {
             addCreatorOrContributor(createTemplateNode, contributorTemplateNode, creators.getString(2),
                                     creators.getString(1));
         }
         creatorStatement.close();
 
         PreparedStatement contributorStatement = c.prepareStatement(SQL_QUERY_CONTRIBUTOR);
         contributorStatement.setInt(1, adID);
         contributorStatement.execute();
         ResultSet contributors = contributorStatement.getResultSet();
         while (contributors.next()) {
             addCreatorOrContributor(createTemplateNode, contributorTemplateNode, contributors.getString(2),
                                     contributors.getString(1));
         }
         contributorStatement.close();
 
         //Nodes are deleted last, to avoid affecting the XPath paths.
         for (Node node : nodesToDelete) {
             node.getParentNode().removeChild(node);
         }
 
         //Write pbcore to template with file name
         String filename = resultSet.getString(19).replace(".mpg", ".xml").replaceAll(Pattern.quote("+"), " ");
         new FileOutputStream(new File(outputdir, filename)).write(
                 DOM.domToString(pbcoreDocument, true).getBytes());
     }
 
     private void addCreatorOrContributor(Node creatorTemplate, Node contributorTemplate, String name, String role) {
         if (role.equals("Instruktør")) {
             cloneNode(creatorTemplate, name, "Director", "p:creator", "p:creatorRole");
         } else if (role.equals("Tegner")) {
             cloneNode(creatorTemplate, name, "Illustrator", "p:creator", "p:creatorRole");
         } else if (role.equals("Oversætter")) {
             cloneNode(contributorTemplate, name, "Translator", "p:contributor", "p:contributorRole");
         } else if (role.equals("Medvirkende")) {
             cloneNode(contributorTemplate, name, "Actor", "p:contributor", "p:contributorRole");
         } else if (role.equals("Tekniske arbejder")) {
             cloneNode(contributorTemplate, name, "Technical Production", "p:contributor", "p:contributorRole");
         } else if (role.equals("Bureau")) {
             cloneNode(creatorTemplate, name, "Production Unit", "p:creator", "p:creatorRole");
         } else if (role.equals("Producent")) {
             cloneNode(creatorTemplate, name, "Producer", "p:creator", "p:creatorRole");
         } else {
             throw new UnsupportedOperationException("Unsupported role: " + role);
         }
     }
 
     private void cloneNode(Node template, String role, String name, String rolePath, String creatorPath) {
         Node creatorNode = template.cloneNode(true);
         Node roleNode = XPATH_SELECTOR.selectNode(creatorNode, rolePath);
         Node nameNode = XPATH_SELECTOR.selectNode(creatorNode, creatorPath);
         roleNode.setTextContent(role);
         nameNode.setTextContent(name);
         template.getParentNode().insertBefore(creatorNode, template);
     }
 }
