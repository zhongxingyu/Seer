 package it.bisi.report.jasper;
 
 import it.bisi.Utils;
 
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.view.JasperViewer;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.export.*;
 
 
 public class ExecuteReport {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		runReport(args[0],args[1],args[2],args[3],args[4]);
 	}
 	
 	public static Connection connectDB(String databaseName, String userName, String password) {
 		Connection jdbcConnection = null;
 		try{
 			Class.forName("com.mysql.jdbc.Driver");
 			String host="jdbc:mysql://"+databaseName;
 			//System.out.println("host="+host);
 			jdbcConnection = DriverManager.getConnection("jdbc:mysql://"+databaseName,userName,password);
 		}catch(Exception ex) {
 			String connectMsg = "Could not connect to the database: " + ex.getMessage() + " " + ex.getLocalizedMessage();
 	         
 			ex.printStackTrace();
 		}
 		return jdbcConnection;
 	}
 	
 	public static void runReport(String databaseName, String userName, String password, String report_identifier, String output_dir)
 	{
 		try {
 			Connection conn = connectDB(databaseName, userName, password);
 			InputStream inputStream = Utils.class.getResourceAsStream("/it/bisi/resources/report1.jasper");
			Map prms = new HashMap();
 			prms.put("OUTPUT_DIR", output_dir);
 			//minus group...
 			//find out the group on rows and other report options
 			Statement stmt_rows=conn.createStatement();
 			stmt_rows.execute("select * from report_definitions where id='"+report_identifier+"'");
 			
 			ResultSet rs_repdef = stmt_rows.getResultSet();
 			rs_repdef.next();
 			String identifier=rs_repdef.getString("data_set_id");
 			String group_rows=rs_repdef.getString("group_question_id");
 			String split_question_id=rs_repdef.getString("split_question_id");
 			String output_file_name=output_dir + '/' + rs_repdef.getString("output_filename");
 			String output_format=rs_repdef.getString("output_format");
 			String report_type=rs_repdef.getString("report_type");
 			String ignore_question_ids = rs_repdef.getString("ignore_question_ids");
 			String language = rs_repdef.getString("language");
 			String customer = rs_repdef.getString("customer");
 			String page_orientation = rs_repdef.getString("page"); 
 			
 			prms.put("DATA_SET_ID", identifier);
 			prms.put("GROUP_ROWS", group_rows);
 			prms.put("REPORT_IDENTIFIER", report_identifier);
 			prms.put("REPORT_TYPE", report_type);
 			prms.put("CUSTOMER", customer);
 			prms.put("SPLIT_QUESTION_ID", split_question_id );
 			stmt_rows.close();
 
 			//hva-fmb: >3.9=groen, 3.0 en 3.1: geel, <3 rood.
 			//coloring of the values in table report.
 			//test in ireport is >=
			Map color_range=new HashMap();
 			if (customer.equals("fraijlemaborg") && report_type.equals("barcharts")){
 				color_range.put("lowRed",new Double(0.0));
 				color_range.put("highRed",new Double(0.0));
 				color_range.put("lowYellow",new Double(0.0));
 				color_range.put("highYellow",new Double(0.0));
 				color_range.put("lowWhite",new Double(1.0));
 				color_range.put("highWhite", new Double(5.0));
 				color_range.put("lowGreen",new Double(0.0));
 				color_range.put("highGreen", new Double(0.0));
 			}else if (customer.equals("fraijlemaborg") && report_type.equals("tables")){
 				color_range.put("lowRed",new Double(1.0));
 				color_range.put("highRed",new Double(3.04999));
 				color_range.put("lowYellow",new Double(0.0));
 				color_range.put("highYellow",new Double(0.0));
 				color_range.put("lowWhite",new Double(1.0));
 				color_range.put("highWhite", new Double(5.0));
 				color_range.put("lowGreen",new Double(3.95));
 				color_range.put("highGreen", new Double(5.0));
 			}else {
 				//default
 				color_range.put("lowRed",new Double(1.0));
 				color_range.put("highRed",new Double(2.9999));
 				color_range.put("lowYellow",new Double(3.0));
 				color_range.put("highYellow",new Double(3.0999));
 				color_range.put("lowWhite",new Double(3.1));
 				color_range.put("highWhite", new Double(3.8999));
 				color_range.put("lowGreen",new Double(3.9));
 				color_range.put("highGreen", new Double(5.0));
 			}
 			prms.put("COLOR_RANGE", color_range);
			System.out.println(color_range);
 			//Alternate color range eg for special table (extremes negative, center green) LWB
 			// determination of use of color_range of color_range alternate is on:
 			// report1l.jrxml -> details -> right jrxml (report3l.jrxml)-> parameters:
 			 //$F{group_id}.equals(5)? $P{COLOR_RANGE_ALTERNATE}: $P{COLOR_RANGE}
 			//we plot red first, above it yellow, above it white, above it green
 			
			Map color_range_alternate=new HashMap();
 			if (customer.equals("leeuwenburg")){
 				color_range_alternate.put("lowRed",new Double(1.0));
 				color_range_alternate.put("highRed",new Double(5.0));
 				color_range_alternate.put("lowYellow",new Double(1.5));
 				color_range_alternate.put("highYellow",new Double(4.5));
 				color_range_alternate.put("lowWhite",new Double(0.0));
 				color_range_alternate.put("highWhite", new Double(0.0));
 				color_range_alternate.put("lowGreen",new Double(2.5));
 				color_range_alternate.put("highGreen", new Double(3.5));
 			} else {
 				//default
 				color_range_alternate.put("lowRed",new Double(1.0));
 				color_range_alternate.put("highRed",new Double(5.0));
 				color_range_alternate.put("lowYellow",new Double(2.0));
 				color_range_alternate.put("highYellow",new Double(4.0));
 				color_range_alternate.put("lowWhite",new Double(0.0));
 				color_range_alternate.put("highWhite", new Double(0.0));
 				color_range_alternate.put("lowGreen",new Double(2.75));
 				color_range_alternate.put("highGreen", new Double(3.25));
 			}
 			prms.put("COLOR_RANGE_ALTERNATE", color_range_alternate);
 			System.out.println(color_range_alternate);
 			/* get key/value pairs for current language/customer-combination */
 			String key = "";
 			String val = "";
			Map texts = new HashMap();
 			Statement stmt_texts = conn.createStatement();
 			stmt_texts.execute("SELECT `key`, `value` FROM `text` WHERE `language` = '" + language + "' AND `customer` = '" + customer + "';");
 			ResultSet rs_keyValPairs = stmt_texts.getResultSet();
 			while (rs_keyValPairs.next()) {
 				key = rs_keyValPairs.getString("key");
 				val = rs_keyValPairs.getString("value");
 				texts.put(key, val);
 			}
 			stmt_texts.close();
 			prms.put("TEXTS", texts);
 			
 			
 			
 			//
 			//returns result:
 			//title 	srtdt 	enddt 	response 	percentage 	type 	group_id
 			//MER jaar 2 - Vt blok 3 0910 	40276 	40306 	15 	0.1239669421487603 	AVG 	1
 			//MER jaar 2 - Vt blok 3 0910 	40276 	40306 	15 	0.1239669421487603 	AVG 	3
 			//MER jaar 2 - Vt blok 3 0910 	40276 	40306 	15 	0.1239669421487603 	AVG 	5
 			// where group_id=theme_id
 			// and type is type of crosstab needed.
 			String query="SELECT a.value title,b.value srtdt, " +
          		"c.value enddt,d.value response,e.value percentage,type,group_id  " +
          		"FROM info_"+identifier+" a,info_"+identifier+" b," +
          				"info_"+identifier+" c, info_"+identifier+" d," +
          				"info_"+identifier+" e," +
          				"(SELECT distinct q.group_id," +
          				"	case type " +
          				"		when 'HVA_Model_Data_Question_Closed_Scale_Two' then 'AVG'" +	         				
          				"		when 'HVA_Model_Data_Question_Closed_Scale_Three' then 'AVG'" +	         				
          				"		when 'HVA_Model_Data_Question_Closed_Scale_Four' then 'AVG'" +	         				
          				"		when 'HVA_Model_Data_Question_Closed_Scale_Five' then 'AVG'" +
          				"		when 'HVA_Model_Data_Question_Closed_Scale_Six' then 'AVG'" +
          				"		when 'HVA_Model_Data_Question_Closed_Scale_Seven' then 'AVG'" +
          				"		when 'HVA_Model_Data_Question_Closed_Percentage' then 'PERC'" +
          				"		when 'HVA_Model_Data_Question_Closed_Number' then 'NUMBER'" +
          				"       when 'HVA_Model_Data_Question_Open_Text' then 'OPEN'" +
          				"		when 'HVA_Model_Data_Question_Open_Email' then 'SKIP'" +
          				"		when 'HVA_Model_Data_Question_Open' then 'SKIP' " +
          				"		ELSE 'NOTYETIMPLEMENTED'" +
          				"	end type " +
          				"FROM questions_"+identifier+" q,meta_"+identifier+" m "+ 
          				"where m.question_id=q.id and parent_id=0 and q.id!='"+group_rows+"' and q.id!='"+split_question_id+"' and " +
          				"(type like '%Closed_Percentage%' or type like '%Closed_Scale%' or type like '%open%' or type like '%number%')) t "+
          		"where a.id='Titel vragenlijst' " +
          		"and b.id='Startdatum' " +
          		"and c.id='Einddatum' " +
          		"and d.id='unieke respondenten' " +
          		"and e.id='Respons percentage' ";
 			prms.put("QUERY", query);
 			
 			//looping through possible split by values (multiple reports for subset of respondents)
 			if (split_question_id !=null && split_question_id.length()>0 ) {
 				//get split_values
 				Statement stmt_rows_values=conn.createStatement();
 				stmt_rows_values.execute("select distinct "+split_question_id+" as split_values FROM values_"+identifier);
 				ResultSet rs_rows_values = stmt_rows_values.getResultSet();
 				while (rs_rows_values.next()) {
 					String split_value=rs_rows_values.getString("split_values");
 					//needed for displaying content
 					prms.put("SPLIT_VALUE", split_value);
 					
 					//response (not percentage, but number of respondents in this report)
 					Statement stmt_response=conn.createStatement();
 					String response_query="select count(*) as response from values_"+identifier+" where "+split_question_id+" like '"+split_value+"'";
 					stmt_response.execute(response_query);
 					ResultSet rs_response = stmt_response.getResultSet();
 					rs_response.next();
 					String response=rs_response.getString("response");
 					prms.put("RESPONSE",response);
 					stmt_response.close();
 					
 
 					//ugly hack voor fraijlemaborg om groepinformatie in rapport te krijgen.
 					//alleen voor onderwijsevaluatie datasetid/identifier=50
 					if (identifier.equals("50") && customer !=null && customer.length()>0 && customer.equals("fraijlemaborg")){
 						Statement stmt_fmb_group_info=conn.createStatement();
 						stmt_fmb_group_info.execute("select 25_groep as group_name, 26_boecode as boecode," +
 								"35_docent as docent, 34_ownaam as module_name " +
 								" from values_"+identifier+" " +
 								" where "+split_question_id+" like '"+split_value+"';" );
 						ResultSet rs_fmb_group_info=stmt_fmb_group_info.getResultSet();
 						rs_fmb_group_info.next();
 						String group_name=rs_fmb_group_info.getString("group_name");
 						String boecode=rs_fmb_group_info.getString("boecode");
 						String docent=rs_fmb_group_info.getString("docent");
 						String module_name=rs_fmb_group_info.getString("module_name");
 						prms.put("GROUP_NAME",group_name);
 						prms.put("BOECODE",boecode);
 						prms.put("DOCENT",docent);
 						prms.put("MODULE_NAME",module_name);
 						stmt_fmb_group_info.close();
 					}
 
 					
 					if (page_orientation != null && page_orientation.equals("landscape")) {
 						inputStream = Utils.class.getResourceAsStream("/it/bisi/resources/report1l.jasper");
 					}else{
 						inputStream = Utils.class.getResourceAsStream("/it/bisi/resources/report1.jasper");
 					}
 					JasperPrint print = JasperFillManager.fillReport(inputStream, prms, conn);
 
 					// Create output in directory public/reports  
 					if(output_format.equals("pdf")) {
 						JasperExportManager.exportReportToPdfFile(print, output_file_name + "_" + split_value + ".pdf");
 					} else if(output_format.equals("odt")) {
 						net.sf.jasperreports.engine.export.oasis.JROdtExporter exporter = new net.sf.jasperreports.engine.export.oasis.JROdtExporter();
 						exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.OUTPUT_FILE_NAME, output_file_name + "_" + split_value+ ".odt");
 						exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.JASPER_PRINT, print);
 						exporter.exportReport();
 					} else if(output_format.equals("html")) {
 						JasperExportManager.exportReportToHtmlFile(print, output_file_name +"_"+split_value+ ".html");
 					} else if(output_format.equals("xml")) {
 						JasperExportManager.exportReportToXmlFile(print, output_file_name +"_"+split_value+ ".xml", false);
 					} else { 
 						JasperViewer.viewReport(print);
 					}
 				}
 			}else{
 				//no split value
 				//response (not percentage, but number of respondents in this report)
 				Statement stmt_response=conn.createStatement();
 				String response_query="select count(*) as response from values_"+identifier;
 				stmt_response.execute(response_query);
 				ResultSet rs_response = stmt_response.getResultSet();
 				rs_response.next();
 				String response=rs_response.getString("response");
 				prms.put("RESPONSE",response);
 				stmt_response.close();
 				
 
 				if (page_orientation != null && page_orientation.equals("landscape")) {
 					inputStream = Utils.class.getResourceAsStream("/it/bisi/resources/report1l.jasper");
 				}else{
 					inputStream = Utils.class.getResourceAsStream("/it/bisi/resources/report1.jasper");
 				}	
 				JasperPrint print = JasperFillManager.fillReport(inputStream, prms, conn);
 				/* Create output in directory public/reports */ 
 				if(output_format.equals("pdf")) {
 					JasperExportManager.exportReportToPdfFile(print, output_file_name + ".pdf");
 				} else if(output_format.equals("odt")) {
 					net.sf.jasperreports.engine.export.oasis.JROdtExporter exporter = new net.sf.jasperreports.engine.export.oasis.JROdtExporter();
 					exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.OUTPUT_FILE_NAME, output_file_name + ".odt");
 					exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.JASPER_PRINT, print);
 					exporter.exportReport();
 				} else if(output_format.equals("html")) {
 					JasperExportManager.exportReportToHtmlFile(print, output_file_name + ".html");
 				} else if(output_format.equals("xml")) {
 					JasperExportManager.exportReportToXmlFile(print, output_file_name + ".xml", false);
 				} else { 
 					JasperViewer.viewReport(print);
 				}
 			}
 		}
 		
 		catch(Exception ex) {
 			String connectMsg = "Could not create the report " + ex.getMessage() + " " + ex.getLocalizedMessage();
 			ex.printStackTrace();
 		}
 	}
 }
