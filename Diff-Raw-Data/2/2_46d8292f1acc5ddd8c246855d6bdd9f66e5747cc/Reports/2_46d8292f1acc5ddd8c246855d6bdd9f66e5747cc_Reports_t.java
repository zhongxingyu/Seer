 package org.alt60m.ministry.servlet;
 
 import java.util.*;
 
 import org.alt60m.ministry.model.dbio.*;
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import org.alt60m.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class Reports {
 	private static Log log = LogFactory.getLog(Reports.class);
 	
 	//the following strings are for getSuccessCriteriaReport() method. 
 	private static String  conditionsPortion(String type, String region, String periodEnd, String periodBegin, String strategyList, String localLevelId, String targetAreaID){
 		String queryPortion="";
 		queryPortion="WHERE ("+
 			" ministry_statistic.periodEnd < "+periodEnd+
 			" AND ministry_statistic.periodEnd >="+periodBegin+
 			" and ministry_activity.Strategy in ("+strategyList+") ";
 			if (type.equals("locallevel")){
 				queryPortion+=" and ministry_locallevel.teamID = '" + localLevelId + "' ";
 			}
 			else if (type.equals("regional")){
 				queryPortion+=" and ministry_targetarea.region = '" + region+ "' ";
 			}
 			else if (type.equals("national"))
 			{
 				queryPortion+=" and (ministry_targetarea.region is not null) AND (ministry_targetarea.region <> '') ";
 			}
 			else if (type.equals("targetarea"))
 			{
 				queryPortion+=" and (ministry_targetarea.targetAreaID = '" + targetAreaID+ "') ";
 			}
 			queryPortion+=") "; 
 			return queryPortion;
 	}
 	private static String groupPortion(String type){ 
 		if(type.equals("national"))
 		{
 			return " GROUP BY ministry_targetarea.region ORDER BY ministry_targetarea.region";
 		}
 		else if(type.equals("targetarea"))
 		{
 			return " GROUP BY ministry_activity.ActivityID, ministry_statistic.periodEnd, ministry_statistic.peopleGroup ORDER BY ministry_targetarea.name,ministry_targetarea.TargetAreaID, ministry_activity.strategy, ministry_statistic.periodEnd, ministry_statistic.peopleGroup";
 		}
 		else
 		{
 		return " GROUP BY ministry_activity.ActivityID, ministry_statistic.peopleGroup"+
				" ORDER BY ministry_targetarea.name,ministry_targetarea.TargetAreaID, ministry_activity.strategy,  ministry_statistic.peopleGroup, ministry_activity.ActivityID ";
 		}
 	}
 	private static String summingFieldsPortion(String type){
 		String queryPortion="";
 		queryPortion="SELECT "+
 		
 		" ministry_activity.ActivityID as activityID,"+
 		" CAST(ministry_activity.periodBegin AS DATE) as activityPeriodBegin,"+
 		" CAST(ministry_statistic.periodEnd AS DATE) as statPeriodEnd,"+
 		" CAST(ministry_statistic.periodBegin AS DATE) as statPeriodBegin,"+
 		" ministry_locallevel.teamID as localLevelId,"+
 		" ministry_targetarea.region, "+
 		" SUM(ministry_statistic.exposuresViaMedia) as exposuresViaMedia,"+
 		" SUM(ministry_statistic.evangelisticOneOnOne) as evangelisticOneOnOne, "+
 		" SUM(ministry_statistic.evangelisticGroup) as evangelisticGroup,"+
 		" SUM(ministry_statistic.decisions) as decisionSum,"+
 		" SUM(ministry_statistic.decisionsHelpedByOneOnOne) as decisionsPersonalEvangelismExposures, "+
 		" SUM(ministry_statistic.decisionsHelpedByGroup) as decisionsGroupEvangelismExposures, "+
 		" SUM(ministry_statistic.decisionsHelpedByMedia) as decisionsMediaExposures, "+
 		" SUM(ministry_statistic.holySpiritConversations) as holySpirit, "+
 		" SUM(ministry_statistic.laborersSent) as laborersSent, " +
 		" ministry_statistic.peopleGroup, ";
 		
 		if (type.equals("targetarea")){					
 			queryPortion+=" ministry_activity.status as status, "+
 				
 				
 				" MAX(ministry_targetarea.TargetAreaID) targetAreaID,"+
 				" MAX(ministry_targetarea.name) campusName,"+
 				" ministry_activity.strategy, "+
 				" concat(ministry_activity.ActivityID,'_',ministry_statistic.periodEnd) as rowid, "+
 				" MAX(ministry_targetarea.enrollment) enrollment ";
 		}
 		
 		else if (!type.equals("national")){					
 			queryPortion+=" ministry_activity.status as status, "+
 				
 				
 				" MAX(ministry_targetarea.TargetAreaID) targetAreaID,"+
 				" MAX(ministry_targetarea.name) campusName,"+
 				" ministry_activity.strategy, "+
 				" ministry_activity.ActivityID as rowid,"+
 				" MAX(ministry_targetarea.enrollment) enrollment ";
 		}
 		else
 		{
 			queryPortion+=" '' as status, "+
 				
 				
 				" '' as targetAreaID, "+
 				" '' as campusName,"+
 				" 'FS' as strategy, "+
 				" ministry_targetarea.region as rowid,"+
 				" ''  as enrollment ";
 		}
 		
 		return queryPortion;		
 	}
 	private static String summingTablesPortion=" from ministry_statistic left join "+
 							" ministry_activity on (ministry_statistic.fk_Activity = ministry_activity.ActivityID) left join "+
 							" ministry_locallevel on (ministry_locallevel.TeamID = ministry_activity.fk_TeamID) LEFT JOIN "+
 							" ministry_targetarea on ministry_targetarea.targetAreaID = ministry_activity.fk_targetAreaID ";
 
 	private static String demographicFieldsPortion(String type) { 
 		String queryPortion="";
 		if(type.equals("targetarea")){
 			queryPortion="SELECT ministry_statistic.peopleGroup,"+
 							" concat(ministry_activity.ActivityID,'_',ministry_statistic.periodEnd) as rowid, "+
 							
 							" ministry_targetarea.region,"+
 							" ministry_targetarea.targetAreaID, "+
 							" ministry_targetarea.name, "+
 							" ministry_statistic.multipliers, "+
 							" ministry_statistic.invldStudents, "+
 							" ministry_statistic.ongoingEvangReln as Seekers,"+
 							" ministry_statistic.studentLeaders, "+
 							" ministry_statistic.periodEnd, "+
 							" ministry_locallevel.region ";
 		}
 		else if(!type.equals("national")){
 			queryPortion="SELECT ministry_statistic.peopleGroup,"+
 							" ministry_activity.ActivityID as rowid, "+
 							
 							" ministry_targetarea.region,"+
 							" ministry_targetarea.targetAreaID, "+
 							" ministry_activity.strategy, "+
 							" ministry_targetarea.name, "+
 							" ministry_statistic.multipliers, "+
 							" ministry_statistic.invldStudents, "+
 							" ministry_statistic.ongoingEvangReln as Seekers,"+
 							" ministry_statistic.studentLeaders, "+
 							
 							" ministry_locallevel.region ";
 		}
 		else
 		{
 			queryPortion="SELECT ministry_statistic.peopleGroup, "+
 							" ministry_targetarea.region as rowid, "+
 							" ministry_targetarea.region,"+
 							" ministry_targetarea.targetAreaID, "+
 							" ministry_targetarea.name, "+
 							" SUM(ministry_statistic.multipliers) multipliers, "+
 							" SUM(ministry_statistic.invldStudents) invldStudents, "+
 							" SUM(ministry_statistic.ongoingEvangReln) as Seekers, "+
 							" SUM(ministry_statistic.studentLeaders) studentLeaders,"+
 							" ministry_locallevel.region ";
 							
 		}
 		
 				return queryPortion;
 		}
 	private static String demographicTablesPortion(String type, String periodEnd, String periodBegin) {
 		return "FROM ministry_statistic INNER JOIN "+
 					(!type.equals("targetarea")? //we skip this step for targetarea, since all weeks are shown
 							" (SELECT ministry_statistic.peopleGroup as peopleGroup, "+
 						" ministry_statistic.fk_Activity AS fk_Activity, "+
 						" MAX(ministry_statistic.periodEnd) AS lastDate "+
 						" FROM ministry_statistic WHERE ministry_statistic.periodEnd < " + periodEnd + 
 						" AND ministry_statistic.periodEnd >= " + periodBegin + 
 						" GROUP BY fk_Activity, ministry_statistic.peopleGroup) LastActivities"+
 					" ON ministry_statistic.fk_Activity = LastActivities.fk_Activity "+
 						" AND ministry_statistic.periodEnd = LastActivities.lastDate "+
 						" and (((ministry_statistic.peopleGroup is null) and (LastActivities.peopleGroup is null)) "+
 							" or (ministry_statistic.peopleGroup=LastActivities.peopleGroup)) LEFT JOIN "
 						:
 							"")+
 			" ministry_activity on ministry_statistic.fk_Activity = ministry_activity.ActivityID LEFT JOIN "+
 			" ministry_locallevel on ministry_locallevel.teamID = ministry_activity.fk_teamID LEFT JOIN "+
 			" ministry_targetarea on ministry_targetarea.targetAreaID = ministry_activity.fk_targetAreaID ";
 	}
 
 	private static ReportRow resultSet2ReportRow(ResultSet sums, ResultSet demos)throws Exception 
 	{
 			try{
 		ReportRow row=new ReportRow();
 		row.setRowid(sums.getString("rowid"));
 		row.setActivityID(sums.getString("activityID"));
 		row.setCampusName(sums.getString("campusName"));
 		row.setStrategy(sums.getString("strategy"));
 		row.setRegion(sums.getString("region"));
 		row.setPeopleGroup(sums.getString("peopleGroup"));
 		row.setStatus(sums.getString("status"));
 		row.setTargetAreaID(sums.getString("targetAreaID"));
 		row.setLocalLevelId(sums.getString("localLevelId"));
 		row.setActivityPeriodBegin(sums.getString("activityPeriodBegin"));
 		row.setStatPeriodEnd(sums.getString("statPeriodEnd"));
 		row.setStatPeriodBegin(sums.getString("statPeriodBegin"));
 		row.setPeopleGroup(sums.getString("peopleGroup"));
 		row.setEnrollment(sums.getString("enrollment"));
 		
 		
 		
 		row.setEvangelisticOneOnOne(sums.getInt("evangelisticOneOnOne"));
 		row.setEvangelisticGroup(sums.getInt("evangelisticGroup"));
 		
 		row.setExposuresViaMedia(sums.getInt("exposuresViaMedia"));
 		row.setHolySpirit(sums.getInt("holySpirit"));
 		
 		row.setDecisions(sums.getInt("decisionSum"));
 		row.setDecisionsPersonalEvangelismExposures(sums.getInt("decisionsPersonalEvangelismExposures"));
 		row.setDecisionsGroupEvangelismExposures(sums.getInt("decisionsGroupEvangelismExposures"));
 		
 		row.setDecisionsMediaExposures(sums.getInt("decisionsMediaExposures"));
 		row.setLaborersSent(sums.getInt("laborersSent"));
 		
 		row.setMultipliers(demos.getInt("multipliers"));
 		row.setInvldStudents(demos.getInt("invldStudents"));
 		row.setSeekers(demos.getInt("seekers"));
 		row.setStudentLeaders(demos.getInt("studentLeaders"));
 		return row;
 	}catch (Exception e) {
 		log.error("Failed to perform Reports.resultSet2ReportRow().", e);
         throw new Exception(e);
     }
 	}
 	// Returns stats for various Success Criteria Reports
 	public static Vector<ReportRow> getSuccessCriteriaReport(String type, String region, String strategyList, String periodEnd, String periodBegin, String localLevelId, String targetAreaId) throws Exception{
 		try {		
 			String sumsQuery=summingFieldsPortion(type)+summingTablesPortion+
 							conditionsPortion(type, region,  periodEnd, periodBegin,  strategyList, localLevelId, targetAreaId)+
 							groupPortion(type);
 			String demographicQuery=demographicFieldsPortion(type)+demographicTablesPortion(type, periodEnd, periodBegin)+
 							conditionsPortion(type, region, periodEnd, periodBegin, strategyList, localLevelId, targetAreaId)+groupPortion(type);
 					
 			Vector<ReportRow> report=new Vector<ReportRow>();
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			Statement stmt2 = conn.createStatement();
 			log.debug(sumsQuery);
 			
 			ResultSet sums = stmt1.executeQuery(sumsQuery);
 			
 			log.debug(demographicQuery);
 			ResultSet demos = stmt2.executeQuery(demographicQuery);
 			ReportRow lastRow=new ReportRow();
 			ReportRow row=new ReportRow();
 			ReportRow summingRow=new ReportRow(); //for within Bridges movements
 			ReportRow runningTotal=new ReportRow(); //for totals at report bottoms
 			if (sums.isBeforeFirst()){
 				while (sums.next()){
 				
 					demos.next();
 					row=new ReportRow();
 					if (sums.getString("rowid").equals(demos.getString("rowid"))){
 						row=resultSet2ReportRow(sums,demos);
 					
 						if (type.equals("national")){
 							row.setLabel(org.alt60m.ministry.Regions.expandRegion(row.getRegion()));
 						} else if (type.equals("targetarea")){
 							row.setLabel((row.getStatPeriodBegin().replace("-","/")+" - "+row.getStatPeriodEnd().replace("-","/")));
 						} else {
 							row.setLabel(row.getCampusName()+" - "+org.alt60m.ministry.Strategy.expandStrategy(row.getStrategy())+" ("+row.getEnrollment()+" enrolled)");
 						}
 				
 						if((!lastRow.getRowid().equals(row.getRowid()))&&(lastRow.getStrategy().equals("BR"))){ //new activity or week after a run of Bridges; the order is important for these functional rows
 						
 							//put end row on if after Bridges rows, since we are now in new activity
 							ReportRow endRow=new ReportRow(summingRow); //we have been totaling the previous Bridges rows, now we dump them into final row
 							endRow.setFunction("end"); 
 							endRow.setLabel(summingRow.getLabel());
 							report.add(endRow);
 							
 						}
 					
 						if ((!lastRow.getStrategy().equals(row.getStrategy()))&&(type.equals("targetarea"))&&(!(lastRow.getStrategy().equals("")||(lastRow.getStrategy()==null)))){ // before the top row of each strategy  we also insert a totals row for the bottom of each strategy
 							report.add(getBottom(type,lastRow,summingRow,runningTotal));
 							runningTotal=new ReportRow();						
 						}					
 						if (((!lastRow.getStrategy().equals(row.getStrategy()))&&(type.equals("targetarea")))||((lastRow.getStrategy().equals("")||(lastRow.getStrategy()==null)))){	//always at top and between strategies for targetarea
 							ReportRow top=new ReportRow(row);
 							top.setFunction("top");
 							report.add(top);					
 						}		
 						if ((!lastRow.getRowid().equals(row.getRowid()))&&(row.getStrategy().equals("BR"))){// is new activity and also Bridges; start toggling rows
 							row.setFunction("detail");
 							ReportRow startRow=new ReportRow();
 							startRow.setFunction("start"); 
 							startRow.setRowGroup(row.getRowid());
 							startRow.setStrategy(row.getStrategy());
 							startRow.setCampusName(row.getCampusName());
 							startRow.setEnrollment(row.getEnrollment());
 							startRow.setLabel(row.getLabel());
 							report.add(startRow);
 						} else if (row.getStrategy().equals("BR")) {//row within split activity 
 							row.setFunction("detail"); 
 						}
 				
 						if((!lastRow.getRowid().equals(row.getRowid()))&&(row.getStrategy().equals("BR"))){//new activity and it's Bridges
 							summingRow=new ReportRow();	//we have been totaling the previous Bridges rows, now we clear them before starting a new summing session
 						}
 				
 						if (row.getStrategy().equals("BR")){ // we sum for Bridges rows only
 							summingRow.addToTotal(row);
 						}
 				
 						runningTotal.addToTotal(row);
 				
 						report.add(row);
 					} else {
 						throw new Exception("Rows do not match in Reports.getSuccessCriteriaReport()") ;
 					}
 					lastRow=new ReportRow(row);
 				}
 				if (lastRow.getStrategy().equals("BR")){//put end row on if last activity was Bridges
 					ReportRow endRow=new ReportRow(summingRow); //we have been totaling the previous Bridges rows, now we dump them
 					endRow.setFunction("end"); 
 					report.add(endRow);
 				}
 			
 				report.add(getBottom(type,lastRow,summingRow,runningTotal));
 				runningTotal=new ReportRow();
 			}//resultset had data, otherwise return no rows in Vector<ReportRow> 'report'
 			
 			return report;
 		} catch (Exception e) {
 				log.error("Failed to perform getSuccessCriteriaReport().", e);
 	            throw new Exception(e);
         }
 	}
 
 	private static ReportRow getBottom(String type, ReportRow lastRow, ReportRow summingRow, ReportRow runningTotal){
 		ReportRow bottom=new ReportRow(runningTotal);
 		bottom.setFunction("bottom");
 		if (type.equals("targetarea")){ //we actually don't want a running total of these for targetarea, just the last record.
 			if(lastRow.getStrategy().equals("BR")){
 				ReportRow endRow=new ReportRow(summingRow);
 				bottom.setInvldStudents(endRow.getInvldStudents());
 		        bottom.setMultipliers(endRow.getMultipliers());
 		        bottom.setStudentLeaders(endRow.getStudentLeaders());
 		        bottom.setSeekers(endRow.getSeekers());
 		      }
 		      else
 		      {
 		    	bottom.setInvldStudents(lastRow.getInvldStudents());
 		        bottom.setMultipliers(lastRow.getMultipliers());
 		        bottom.setStudentLeaders(lastRow.getStudentLeaders());
 		      }
 		}
 		return bottom;
 	}
 	
 	private static String lastStatus(String date){
 		return " (SELECT ministry_activity_history.activity_id as activity_id, ministry_activity_history.to_status as status, ministry_activity_history.period_begin as periodBegin"+
 			" FROM	("+
 			"	SELECT ministry_activity_history.activity_id as activity_id, MAX(ministry_activity_history.period_begin) as periodBegin "+
 			" FROM ministry_activity_history WHERE "+
 			" (ministry_activity_history.period_begin<='"+date+"') AND (ministry_activity_history.toStrategy Is Null) "+
 			" GROUP BY ministry_activity_history.activity_id ) lastDates INNER JOIN ministry_activity_history "+
 			" ON (lastDates.activity_id=ministry_activity_history.activity_id AND lastDates.periodBegin=ministry_activity_history.period_begin)) lastStatus ";
 	}
 	private static String processedOrder( Vector<String> order){
 		String processedOrder="";	
 		Hashtable<String,String> orderCodes=new Hashtable<String,String>();
 		
 		orderCodes.put("strategy","strategy");
 		orderCodes.put("status"," field(lastStatus.status,'AC','TR','LA','KE','PI','FR') ");
 		orderCodes.put("team","teamName");
 		orderCodes.put("campus","campusName");
 		orderCodes.put("region","region");
 		orderCodes.put("city","country, state, city ");
 		for (String o : order){
 				if (orderCodes.keySet().contains(o)){
 					processedOrder+=orderCodes.get(o)+", ";
 				}
 			}
 		processedOrder=processedOrder.substring(0,processedOrder.length()-2);//trim final comma 
 		return processedOrder;
 	}
 	
 	private static String countReportQuery(String type, String region, String strategyList, String date, Vector<String> order){
 		String group="";
 		String address="";
 		if (type.equals("movement")){
 			group="ministry_targetarea.targetAreaID,  ministry_activity.ActivityID ";
 			 address=" MAX(ministry_targetarea.region) as region, MAX(ministry_targetarea.city) as city,  MAX(ministry_targetarea.state) as state, MAX(ministry_targetarea.country) as country ";
 		}
 		else if (type.equals("location")){
 			group="ministry_targetarea.targetAreaID ";
 			 address=" MAX(ministry_targetarea.region) as region, MAX(ministry_targetarea.city) as city,  MAX(ministry_targetarea.state) as state, MAX(ministry_targetarea.country) as country ";
 		}
 		else if (type.equals("teamorg")||type.equals("teamgeo")){
 			group="ministry_locallevel.teamID ";
 			 address=" MAX(ministry_locallevel.region) as region, MAX(ministry_locallevel.city) as city,  MAX(ministry_locallevel.state) as state, MAX(ministry_locallevel.country) as country ";
 		}
 		
 		
 		
 		return "SELECT MAX(ministry_targetarea.name) as campusName, MAX(ministry_targetarea.isSecure) as isSecure,"+
 			"  lastStatus.status as status,  MAX(ministry_targetarea.enrollment) as enrollment, "+
 			" MAX(ministry_activity.strategy) as strategy, "+
 			" MAX(ministry_locallevel.name) as teamName, MAX(ministry_locallevel.teamID) as teamID, MAX(ministry_targetarea.targetAreaID) as campusID, "+
 			address+ //this portion of query generated based on type of report requested
 			" FROM (ministry_activity INNER JOIN ministry_targetarea ON ministry_activity.fk_targetAreaID = ministry_targetarea.targetAreaID) "+
 			" INNER JOIN ministry_locallevel ON ministry_activity.fk_teamID = ministry_locallevel.teamID "+
 			" LEFT JOIN "+lastStatus(date)+" ON ministry_activity.ActivityID=lastStatus.activity_id "+
 			" WHERE "+ 
 			" ministry_activity.strategy in ("+strategyList+") and (ministry_targetarea.isClosed is null or ministry_targetarea.isClosed='') and "+
 			" lastStatus.status in ('AC','TR','LA') "+
 			((!(region.equals(""))&& (!(region.toLowerCase().equals("national"))))? " and "+(type.equals("teamorg")?"ministry_locallevel.":"ministry_targetarea.")+"region = '"+region+"' ":"")+
 			" GROUP BY "+group+
 			" ORDER BY "+ processedOrder( order)+
 			";";
 	}
 	private static String convertStatus(String code){
 		Hashtable<String,String> codes=new Hashtable<String,String>();
 		codes.put("IN","Inactive");
 		codes.put("FR","Forerunner");
 		codes.put("PI","Pioneering");
 		codes.put("KE","Key Contact");
 		codes.put("LA","Launched");
 		codes.put("AC","Active");
 		codes.put("TR","Transformational");
 		if(codes.keySet().contains(code.toUpperCase())){
 			return codes.get(code);
 		}
 		else
 		{
 			return "Other";
 		}
 	}
 	private static StringBuffer getMusterTop(String type){
 		StringBuffer renderedReport=new StringBuffer();
 		if (type.equals("movement")){
 			renderedReport.append("<tr ><td colspan=\"2\" class=\"label_darker_blue\">Movements</td>");
 		}
 		else if (type.equals("location")){
 			renderedReport.append("<tr ><td class=\"label_darker_blue\">Ministry Locations</td>");
 		}
 		else if (type.equals("teamorg")){
 			renderedReport.append("<tr ><td class=\"label_darker_blue\">Missional Teams</td>");
 		}
 		else if (type.equals("teamgeo")){
 			renderedReport.append("<tr ><td class=\"label_darker_blue\">Missional Teams</td>");
 		}	
 		renderedReport.append("<td class=\"report_light_blue\">Region</td>");
 		renderedReport.append("<td class=\"report_darker_blue\">City</td><td class=\"report_light_blue\">State</td><td class=\"report_darker_blue\">Country</td>");
 		if (type.equals("location")){
 			renderedReport.append("<td class=\"report_light_blue\">Enrollment</td>");
 			} 
 		if (type.equals("movement")){
 			renderedReport.append("<td colspan=\"2\" class=\"report_light_blue\">Missional Team</td>");
 		}
 		renderedReport.append("</tr>");
 		return renderedReport;
 	}
 	private static StringBuffer getMusterBottom(String type, int rows, int secureRows, int enrollment){
 		StringBuffer renderedReport=new StringBuffer();
 		if (type.equals("movement")){
 			renderedReport.append("<tr ><td colspan=\"2\" class=\"label_darker_blue\">"+org.alt60m.util.Toolbox.commatize(rows+"")+" Movements On Record<br>"+(secureRows>0?"<i>"+secureRows+" Sensitive Movements Not Displayed</i>":"")+"</td>");
 		}
 		else if (type.equals("location")){
 			renderedReport.append("<tr ><td class=\"label_darker_blue\">"+org.alt60m.util.Toolbox.commatize(rows+"")+" Ministry Locations On Record<br>"+(secureRows>0?"<i>"+secureRows+" Sensitive Locations Not Displayed</i>":"")+"</td>");
 		}
 		else if (type.equals("teamorg")){
 			renderedReport.append("<tr ><td class=\"label_darker_blue\">"+org.alt60m.util.Toolbox.commatize(rows+"")+" Missional Teams On Record</td>");
 		}
 		else if (type.equals("teamgeo")){
 			renderedReport.append("<tr ><td class=\"label_darker_blue\">"+org.alt60m.util.Toolbox.commatize(rows+"")+" Missional Teams On Record</td>");
 		}	
 		renderedReport.append("<td class=\"report_light_blue\">Region</td>");
 		renderedReport.append("<td class=\"report_darker_blue\">City</td><td class=\"report_light_blue\">State</td><td class=\"report_darker_blue\">Country</td>");
 		if (type.equals("location")){
 			renderedReport.append("<td class=\"report_light_blue\">Enrollment <br>("+org.alt60m.util.Toolbox.commatize(enrollment+"")+" total)</td>");
 			} 
 		if (type.equals("movement")){
 			renderedReport.append("<td colspan=\"2\" class=\"report_light_blue\">Missional Team</td>");
 		}
 		renderedReport.append("</tr>");
 		return renderedReport;
 	}
 	public static String getMuster(String type, String region, String strategyList, String periodEnd, Vector<String> order,java.util.Vector<String> keys) throws Exception{
 		try{
 			
 			String query="";
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			query=countReportQuery( type, region,  periodEnd, strategyList, order);
 			log.debug(query);
 			ResultSet resultSet = stmt1.executeQuery(query);
 			
 			StringBuffer renderedReport=new StringBuffer();
 			renderedReport.append(getMusterTop(type));
 			int rows=0;
 			int enrollment=0;
 			int secureRows=0;
 			boolean alternate=true;
 			boolean lighter=true;
 			
 			while (resultSet.next()){
 				renderedReport=renderMuster( renderedReport, lighter,  resultSet,  type,  keys);
 				if (alternate){lighter=!lighter;}
 			rows++;
 			enrollment+=org.alt60m.util.Toolbox.stringToIntegerForceZero(resultSet.getString("enrollment"));
 				
 			}
 			secureRows=getSecureRows();
 			renderedReport.append(getMusterBottom(type,rows,secureRows,enrollment));
 			resultSet=null;
 			conn.close();
 			return renderedReport.toString();
 			}
 		catch (Exception e) {
 		log.error("Failed to perform getMuster().", e);
         throw new Exception(e);
     	}
 	}
 	private static int secureRows=0;
 	public static int getSecureRows(){
 		return secureRows;
 	}
 	public  static StringBuffer renderMuster(StringBuffer renderedReport, boolean lighter, java.sql.ResultSet resultSet, String type, java.util.Vector<String> keys)throws Exception{
 
 		String cellAlt="";
 		String cell="";
 		boolean secure=false;
 			if(lighter){
 						cell="light";
 						cellAlt="darker";
 					}
 					else
 					{
 						cell="darker";
 						cellAlt="veryDark";
 					}
 					secure=(!(resultSet.getString("isSecure").equals("F")))&&(!type.equals("team"));
 					if(
 						(!secure)||
 						(keys.contains(resultSet.getString("campusID"))||
 						(keys.contains(resultSet.getString("region"))||
 						(keys.contains("ALL"))))
 						){ 
 							renderedReport.append("<tr >");
 							renderedReport.append("<td class=\"label_"+(!type.equals("movement")?cell:cellAlt) +"\">"+(secure?"<i>":""));
 							if(type.equals("teamorg")||type.equals("teamgeo")){ 
 								renderedReport.append("<a href=\"/servlet/InfoBaseController?action=showTeam&locallevelid="+resultSet.getString("teamID")+"\">");
 								renderedReport.append(resultSet.getString("teamName"));
 							}
 							 else 
 							{ 
 								renderedReport.append("<a href=\"/servlet/InfoBaseController?action=showTargetArea&targetareaid="+resultSet.getString("campusID")+"\">");
 								if (type.equals("movement")){
 									renderedReport.append((resultSet.getString("campusName")+((secure?" (sensitive)":"")+"</a>")+" </td><td class=\""+
 											(type.equals("movement")?"report_"+cell:"label_"+cellAlt) +"\">"+(secure?"<i>":"")+
 											org.alt60m.ministry.Strategy.expandStrategy(resultSet.getString("strategy"))));
 								}
 								else if (type.equals("location")){
 									renderedReport.append(resultSet.getString("campusName")+((secure?" (sensitive)":"")+"</a>"));
 								}
 								
 								
 							}
 						 
 						
 						renderedReport.append("</td><td class=\"report_"+cellAlt +"\">"+(secure?"<i>":"")+""+org.alt60m.ministry.Regions.expandRegion(resultSet.getString("region")!=null?resultSet.getString("region"):"")+"</td>");
 						renderedReport.append("<td class=\"report_"+cell +"\">"+(secure?"<i>":"")+""+resultSet.getString("city")+"</td>");
 						renderedReport.append("<td class=\"report_"+cellAlt +"\">"+(secure?"<i>":"")+""+resultSet.getString("state")+"</td>");
 						renderedReport.append("<td class=\"report_"+cell +"\">"+(secure?"<i>":"")+""+resultSet.getString("country")+"</td>");
 						if (type.equals("movement")){ 
 							 
 							 renderedReport.append("<td class=\"report_"+cellAlt +"\">"+(secure?"<i>":""));
 							 renderedReport.append("<a href=\"/servlet/InfoBaseController?action=showTeam&locallevelid="+resultSet.getString("teamID")+"\">"+resultSet.getString("teamName")+"</a>");	
 									 renderedReport.append("</td>");
 						} 
 						if(type.equals("location")){
 							renderedReport.append("<td class=\"report_"+cellAlt +"\">"+(secure?"<i>":"")+""+org.alt60m.util.Toolbox.commatize(resultSet.getString("enrollment"))+"</td>");
 						}
 			renderedReport.append("</tr>");
 					}else{
 			secureRows++;
 					}
 			
 			
 		
 		return renderedReport;
 	}
 
 }
