 package it.bisi.report.jasper.datasource;
 
 import it.bisi.report.jasper.datasource.object.XformBean;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.StringTokenizer;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 
 
 //import org.apache.commons.lang.StringEscapeUtils;
 
 public class XformJRDataSource {
 	private String data_location;
 	private String xform_location;
 	private String xform_name;
 	private String report_question_ids;
 	private String group_question_id;
 	private String split_question_id;
 	private String split_question_value;
 	private Boolean get_answer_labels;
 
 	public XformJRDataSource(String data_location, String xform_location, String xform_name, String report_question_ids, String group_question_id, String split_question_id, String split_question_value, Boolean get_answer_labels) {
 		this.data_location=data_location;
 		this.xform_location=xform_location;
 		this.xform_name=xform_name;
 		this.report_question_ids=report_question_ids;
 		this.group_question_id=group_question_id;
 		this.split_question_id=split_question_id;
 		this.split_question_value=split_question_value;
 		this.get_answer_labels=get_answer_labels;
 
 		//this.report_question_ids="g6-Tevredenheid/g6-InhoudRelevant";
 	}
 
 	public JRDataSource getRecords() {
 		String report_question_value="";
 		String group_question_value="";
 
 		JRBeanCollectionDataSource dataSource;
 		Collection<XformBean> reportRows=new ArrayList<XformBean>();
 		try{
 			// put data in dom-object
 			File fXmlFile = new File(data_location);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(fXmlFile);
 
 			//loop through report_questions
 			String report_question_id="";
 			StringTokenizer st = new StringTokenizer(report_question_ids,","); 
 			int group_seq_number = 0;
 			while (st.hasMoreTokens()){
 				report_question_id=st.nextToken().trim();
 				String report_question_text=it.bisi.Utils.getXformLabel(xform_location, xform_name, report_question_id, null);
 				group_seq_number++;
 				//String question_field=rsh_questions.getString(1); //question_id
 				//create the xpath expressions
 				//@todo split_value in searchString
 				//here we can select respondents based on split_value, date, whatever.
 				// example string, search respondents with id > 0 and split_question_id and split_value set, need to improve it
 				//String searchRespondents="//respondenten/respondent[@id>0]/*["+split_question_id+"="+split_value+"]";
 				//all respondents
 				String searchRespondents="//respondenten/respondent/*";
 				
 				if (split_question_id != null && !split_question_id.isEmpty() &&split_question_value != null && !split_question_value.isEmpty()) {
 					//searchRespondents="//respondenten/respondent/*['"+split_question_id+"'='"+StringEscapeUtils.escapeXml(split_question_value)+"']";
 					//searchRespondents="//respondenten/respondent/*[\""+split_question_id+"\"=\""+split_question_value+"\"]";
 					//TODO check allow single quote, disallow double quote
 					searchRespondents="//respondenten/respondent/*[.."+split_question_id+"=\""+split_question_value+"\"]";
 				}
 
 				XPathFactory factory = XPathFactory.newInstance();
 				XPath xpath = factory.newXPath();
 				XPathExpression expr = null;
 				expr = xpath.compile(searchRespondents);
 				Object result=null;
 				result = expr.evaluate(doc, XPathConstants.NODESET);
 
 				NodeList nodes = (NodeList) result;
 				String expression;
 				
 				for (int i = 0; i < nodes.getLength(); i++) {
 					String respondent_id=nodes.item(i).getParentNode().getAttributes().getNamedItem("id").getNodeValue();
 					//get report_question_value (answer (Id))
 					//we have /questionnaire in report_question_id, but we are already in that node 
 					expression = report_question_id.replace("/questionnaire/","");
 					Node reportQuestionNode = (Node) xpath.evaluate(expression, nodes.item(i), XPathConstants.NODE);
 					report_question_value=reportQuestionNode.getTextContent();
 					
 					String report_question_label=null;
 					//get answer labels only when needed (get_answer_labels = true and answer=value 
 					if (get_answer_labels && it.bisi.Utils.isNum(report_question_value)){
 						report_question_label=it.bisi.Utils.getXformLabel(xform_location, xform_name, report_question_id, report_question_value);
 					}
 
 					//get group_question_value
 					group_question_value=null;
 					String group_question_text=null;
 					String group_question_label=null;
 					if (group_question_id !=null){
 						group_question_text=it.bisi.Utils.getXformLabel(xform_location, xform_name, group_question_id, null);
						expression=group_question_id;
 						Node groupQuestionNode = (Node) xpath.evaluate(expression, nodes.item(i), XPathConstants.NODE);
 						group_question_value=groupQuestionNode.getTextContent();
 						group_question_label=it.bisi.Utils.getXformLabel(xform_location, xform_name, group_question_id, group_question_value);
 						
 							
 					}
 					//TODO missing values implementation, now if it is empty, we don't put it in the bean
 					if (!report_question_value.isEmpty()){
 						//System.out.println(report_question_id+":;"+ report_question_text+":;"+ report_question_value+":;"+ report_question_label+":;"+ group_question_id+":;"+ group_question_text+":;"+ group_question_value+":;"+ group_question_label+":;"+ new Integer( group_seq_number));
 						XformBean ra=new XformBean(report_question_id, report_question_text, report_question_value, report_question_label, group_question_id, group_question_text, group_question_value, group_question_label,  new Integer( group_seq_number), respondent_id);
 						reportRows.add(ra);
 					}
 				}
 				if ( group_question_id != null && group_question_id.length() > 0 )
 				{
 					// Populate the percentages.
 					//ArrayList hm = new ArrayList();
 					//it.bisi.Utils.hmPercentages.put( report_question_id, hm);
 					//CalculatePercetage( reportRows, report_question_id ,hm );*/
 				}				
 			}
 			
 			
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XPathExpressionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		dataSource = new JRBeanCollectionDataSource(reportRows);
 		return dataSource;
 
 	}	
 
 }
