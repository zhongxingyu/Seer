 package org.alt60m.ministry.servlet.modules;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.alt60m.ministry.States;
 import org.alt60m.ministry.model.dbio.*;
 import org.alt60m.ministry.servlet.modules.model.Section;
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import org.alt60m.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class InfoBaseModuleQueries {
 	private static Log log = LogFactory.getLog(InfoBaseModuleController.class);
 	public static Vector getLocalLevelTeamsByRegion(String region) {
 		LocalLevel ll = new LocalLevel();
 		Vector v = ll.selectList("((hasMultiRegionalAccess='T' AND isActive = 'T') OR (region = '" + region + "' AND isActive = 'T')) ORDER BY name");
 		return v;
 	}
 	public static ResultSet getBreadcrumbSearchResults(String type,String name,String city,String state,String region,String country,  String granularity)throws Exception{
 		Connection conn = DBConnectionFactory.getDatabaseConn();
 		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		String select="";
 		String tableAbbr="";
 		city=city.replace("'","''");
 		country=country.replace("'","''");
 		name=name.replace("'","''");
 		if (type.equals("team")){
 			tableAbbr=" ml.";
 			select="  ml.teamID as id from  ministry_locallevel ml  ";
 		}else { 
 			tableAbbr=" mt.";
 			select="  mt.targetAreaID as id from  ministry_targetarea mt  ";
 		}
 		String qry=" Select "+selectFields( tableAbbr,  granularity,  select) + 
 				" where  true "+nonNameConditions( city, state, region,  country, "", type,  false)+
 				getBreadcrumbTypeConditions( type, granularity, tableAbbr )+" group by "+tableAbbr+granularity+" order by name asc;";
 		log.debug(qry);
 		return stmt.executeQuery(qry);
 	}
 	public static String selectFields(String tableAbbr, String granularity, String select){
 		String result=tableAbbr+granularity+" as name,  "+tableAbbr+"city as city, "+tableAbbr+"state as state, "+tableAbbr+"region as region,   "+tableAbbr+"country as country, "+select;
 		return result;
 	}
 	public static String getBreadcrumbTypeConditions(String type,String granularity,String tableAbbr ){
 		String typeConditions=(type.equals("team")?" and ml.isActive='T' ":"  and (mt.isClosed is null or mt.isClosed != 'T') and (mt.eventType is null or mt.eventType<=>'') ");
 		typeConditions+=" and "+tableAbbr+"region is not null and not("+tableAbbr+"region<=>'') and "+
 		tableAbbr+"city is not null and not("+tableAbbr+"city<=>'') and "+
 		tableAbbr+"country is not null and not("+tableAbbr+"country<=>'')  ";
 		if (granularity.equals("state")){
 			typeConditions+=" and "+tableAbbr+"state is not null and not("+tableAbbr+"state<=>'') ";
 		}
 		String result=typeConditions+(granularity.equals("country")?" and "+tableAbbr+"country <> 'USA' ":"")+(granularity.equals("state")?" and "+tableAbbr+"country = 'USA' ":"");
 		return result;
 	}
 	public static String getNameExp(String type){
 		if (type.equals("person")){
 			return " concat_ws('',' ',mp.firstName,' ( ',mp.preferredName,' ) ',mp.lastName,' ') ";
 		}else if (type.equals("team")){
 		return " concat_ws('',' ',ml.name,' ') ";
 		}else { 
 		return " concat_ws('',' ',mt.name,' ( ',mt.altName,' / ',mt.abbrv,' )') ";
 			
 		}
 	}
 	public static String getTypeConditions(String type){
 		if (type.equals("person")){
 			return " and mp.isSecure != 'T' ";
 		}else if (type.equals("team")){
 			return " and ml.isActive='T' ";
 		}else { 
 			return " and (mt.isClosed is null or mt.isClosed != 'T') and (mt.eventType is null or mt.eventType<=>'') ";
 			}
 	}
 	public static String getSelect(String type, boolean reqMovement){
 		if (type.equals("person")){
 			return " Select concat_ws('',mp.firstName,if((mp.preferredName<>'' and mp.preferredName <> mp.firstName),concat_ws('',' (',mp.preferredName,') '),' '),mp.lastName)  as name, "+
 					" ma.city as city, ma.state as state, ma.country as country, mp.region as region, "+(reqMovement?" mact.strategy ":" '' ") +"  as strategy,  "
 					+(reqMovement?" mact.status ":" '' ") +"  as status, mp.personID as id, ms.accountNo as accountNo, "
 					+" concat_ws('', ma.address1 ,if(ma.address2 is null,'',' '),ma.address2) as address, ma.email as email " ;
 		}else if (type.equals("team")){
 			return " Select ml.name as name, "+
 					" ml.city as city, ml.state as state, ml.country as country, ml.region as region, "+(reqMovement?" mact.strategy ":" '' ") +"  as strategy,  "+(reqMovement?" mact.status ":" '' ") +"  as status, ml.teamID as id, '' as accountNo, '' as address, '' as email  ";
 		}else { 
 			return " Select mt.name as name, "+
 					" mt.city as city, mt.state as state,  mt.country as country, mt.region as region,  "+(reqMovement?" mact.strategy ":" '' ") +"  as strategy,   "+(reqMovement?" mact.status ":" '' ") +"  as status, mt.targetAreaID as id, '' as accountNo,  '' as address, '' as email ";
 		}
 	}
 	public static String getGroup(String type){
 		if (type.equals("person")){
 			return " mp.personID ";
 		}else if (type.equals("team")){
 			return " ml.teamID ";
 		}else { 
 			return " mt.targetAreaID ";
 		}
 	}
 	public static String getAddressTable(String type){
 		if (type.equals("person")){
 			return " ma.";
 		}else if (type.equals("team")){
 			return " ml.";
 		}else { 
 			return " mt.";
 		}
 	}
 	public static String getObjectTable(String type){
 		if (type.equals("person")){
 			return " mp.";
 		}else if (type.equals("team")){
 			return " ml.";
 		}else { 
 			return " mt.";
 		}
 	}
 	public static String getTables(String type, boolean reqMovement){
 		if (type.equals("person")){
 			return " ministry_person mp left join ministry_newaddress ma "
 					+" on (ma.fk_PersonID=mp.personID and ma.addressType='current') "+
 					"  left join ministry_staff ms on ms.person_id=mp.personID "+
 					(reqMovement?" inner join ministry_movement_contact mmc on mmc.personID= mp.personID  "
 					+" inner join ministry_activity mact  on "
 					+" ( mact.ActivityID=mmc.ActivityID and mact.status <> 'IN' and mact.status is not null ) ":"  ");
 		}else if (type.equals("team")){
 			return " ministry_locallevel ml   "+
 					(reqMovement?" inner  join ministry_activity mact on (ml.teamID=mact.fk_teamID and mact.status <> 'IN' and mact.status is not null ) ":"");
 		}else { 
 			return " ministry_targetarea mt   "+
 				(reqMovement?" inner  join ministry_activity mact on (mt.targetAreaID=mact.fk_targetAreaID and mact.status <> 'IN' and mact.status is not null  ) ":"");
 		}
 	}
 	public static String addressConditions(String element, String elementType, String addressTable){
 		String result="";
 		element=element.replace("'","''");
 		if (!element.equals("")&&element!=null){
 			result +=" and upper("+addressTable+elementType+") like '%"+element.toUpperCase().replaceAll("[ \t\n\f\r]+", "% %")+"%' ";
 		}
 		return result;
 	}
 	public static String nonNameConditions(String city,String state,String region, String country,String strategy,String type, boolean reqMovement){
 		String objectTable=getObjectTable(type);
 		String addressTable=getAddressTable(type);
 		String result="";
 		result+=addressConditions(city,"city",addressTable);
 		result+=addressConditions(state,"state",addressTable);
 		result+=addressConditions(country,"country",addressTable);
 		if (!region.equals("")&&!region.toUpperCase().equals("('NONNULL')")&&region!=null){
 			result +=" and upper("+objectTable+"region) in "+region.toUpperCase()+" ";
 		}
 		if (reqMovement){
			result +=" and upper(mact.strategy) in "+strategy.toUpperCase()+" ";
 		}
 		return result;
 	}
 	public static Hashtable<String, String>nonBlankNameConditions(String name,String type,String tables,String addressTable,String conditions,String nameOnlyConditions,boolean singleField,String nameExp){
 		Hashtable<String, String>result=new Hashtable<String, String>();
 		boolean hasCountry=false;
 		boolean hasState=false;
 		name=name.replace("%","");
 		name=name.replace("'","''");
 		String[]namePart=name.toUpperCase().split("[ \t\n\f\r]+");
 		for (int i=0;i<namePart.length;i++){
 			String s=namePart[i].trim();
 			
 			
 				Vector<String>countries=CountryCodes.partialToCodes(s);				
 				Vector<String>states=States.partialToCodes(s);
 				if(s.length()>2){
 					s="%"+s+"%";}else{
 						s="% "+s+" %";
 					}
 					
 				if(countries.size()+states.size()>0&&singleField){
 					conditions+=" and (false ";
 				for(String country:countries){
 					conditions +=" or country ='"+country+"' ";
 					
 					}
 				for(String state:states){
 					conditions +=" or state='"+state+"' ";
 					
 					}
 				conditions +=" or concat_ws('',upper("+nameExp+")"+(singleField?" ,' ',upper(city),' ' ":"")+") like '"+s+"') ";
 				
 				}else{
 					conditions +=" and (concat_ws('',upper("+nameExp+")"+(singleField?" ,' ',upper(city),' ' ":"")+") like '"+s+"') ";
 			
 				}
 			
 			nameOnlyConditions +=" + (upper("+nameExp+") like '"+s+"' ) "+((type.equals("person")||type.equals("team"))?"":" and (upper(abbrv) like '"+s+"' ) ");
 		}
 		result.put("tables",tables);
 		result.put("nameOnlyConditions", nameOnlyConditions+" ) as priority ");
 		result.put("conditions", conditions);
 		return result;
 	}
 
 	public static Hashtable<String, String>getNameConditions(String name,String type,String tables,String addressTable,String conditions,boolean singleField){
 		Hashtable<String, String>result=new Hashtable<String, String>();
 		String nameExp=getNameExp(type);
 		if (!name.trim().equals("")&&name!=null){
 			return nonBlankNameConditions( name, type, tables, addressTable, conditions, " , (true ",singleField, nameExp);
 			}
 		result.put("nameOnlyConditions", " , 1 as priority ");
 		result.put("tables", tables);
 		result.put("conditions", conditions+" and upper("+(type.equals("person")?" concat_ws('',mp.firstName,mp.lastName) ":nameExp)+") is not null ");
 		return result;
 	}
 	public static ResultSet getSearchResults(String type,String name,String city,String state,String region, String country,String strategy, boolean singleField)throws Exception{
 		Connection conn = DBConnectionFactory.getDatabaseConn();
 		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		Boolean reqMovement=(!strategy.equals("")&&!strategy.toUpperCase().equals("('NONNULL')")&&strategy!=null);
 		String addressTable=getAddressTable(type);
 		String select=getSelect(type,reqMovement);
 		String group=getGroup(type);
 		String typeConditions=getTypeConditions(type);
 		String tables=getTables(type,reqMovement);
 		Hashtable<String,String>nameConditions=getNameConditions( name, type, tables, addressTable, "", singleField);
 		tables=nameConditions.get("tables");
 		String conditions=nameConditions.get("conditions");
 		String nameOnlyConditions=nameConditions.get("nameOnlyConditions");
 		conditions+=nonNameConditions( city, state, region,  country, strategy, type,  reqMovement);
 		if(conditions.equals("")){conditions=" and false ";}
 		String qry="select core.name as name, core.city as city, core.state as state, core.country as country, core.region as region, "+
 			" core.id as id, core.accountNo as accountNo, core.status as status, core.strategy as strategy, core.priority as priority, "+
 			" core.address as address, core.email as email from ("+
 			select +nameOnlyConditions+ " from "+tables+" where (true "+conditions+typeConditions+" ) group by "+group+" ) core"+
 			" order by core.priority desc, core.name asc;";
 		log.debug(qry);
 		return stmt.executeQuery(qry);
 	}
 	public static Vector listContactsByLastName(String search) {
 		try {
 			Vector<Contact>c=new Vector<Contact>();
 			if (search.length()>0){
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			String query="SELECT ministry_person.personID as personID, ministry_person.firstName as firstName,"+
 			" ministry_person.preferredName as preferredName, ministry_person.lastName as lastName, max(ministry_newaddress.email) as email,"+
 			" ministry_person.accountNo as accountNo "+
 			" FROM (ministry_person INNER JOIN ministry_newaddress "+
 			" ON ministry_person.personID = ministry_newaddress.fk_PersonID) "+ 
 			" WHERE    not(isSecure<=>'T') and UPPER(ministry_person.lastName) like '" + search.toUpperCase() + 
 			"%' and ministry_newaddress.email is not null and ministry_newaddress.addressType='current' group by ministry_person.personID ORDER BY ministry_person.lastName, ministry_person.firstName;";
 			log.debug(query);
 			ResultSet rs = stmt.executeQuery(query);
 			while (rs.next()){
 				Contact contact= new Contact(rs.getInt("personID"));
 				contact.setAccountNo(rs.getString("accountNo"));
 				contact.setFirstName(rs.getString("firstName"));
 				contact.setLastName(rs.getString("lastName"));
 				contact.setPreferredName(rs.getString("preferredName"));
 				contact.setEmail(rs.getString("email"));
 				c.add(contact);
 			}
 			}
 			return c;
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static Vector<Hashtable<String,Object>> getMovementContacts(String activityId){
 		try{
 			Vector<Hashtable<String,Object>>c=new Vector<Hashtable<String,Object>>();
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			String query="SELECT ministry_movement_contact.personID as personID, ministry_person.firstName as firstName,"+
 			" ministry_person.preferredName as preferredName, ministry_person.lastName as lastName, max(mna1.email) as emailCurrent,  max(mna2.email) as emailPermanent,"+
 			" ministry_person.accountNo as accountNo "+
 			" FROM (ministry_movement_contact inner join ministry_person "+
 			" on ministry_person.personID=ministry_movement_contact.personID Left JOIN ministry_newaddress mna1 "+
 			" ON (ministry_person.personID = mna1.fk_PersonID and mna1.addressType='current' )Left JOIN ministry_newaddress mna2"+
 			" ON (ministry_person.personID = mna2.fk_PersonID and mna2.addressType='permanent' )) " +
 			" WHERE ministry_movement_contact.ActivityID ='"+activityId+"' and not(isSecure<=>'T')  group by ministry_person.personID order by lastName, firstName;";
 			log.debug(query);
 			ResultSet rs = stmt.executeQuery(query);
 			while (rs.next()){
 				Hashtable<String,Object> h=new Hashtable<String,Object>();
 				h.put("id",rs.getString("personID")+"");
 				h.put("accountNo",rs.getString("accountNo")+"");
 				String operantName="";
 				if ((rs.getString("firstName")+"").equals(rs.getString("preferredName")+"")||(rs.getString("preferredName")+"").equals("")){
 					operantName=(rs.getString("firstName")+" "+rs.getString("lastName"));
 				}else {operantName=rs.getString("firstName")+" ("+rs.getString("preferredName")+") "+rs.getString("lastName")+"";}
 				h.put("name", operantName);
 				h.put("firstName",rs.getString("firstName")+"");
 				h.put("lastName",rs.getString("lastName")+"");
 				h.put("preferredName",rs.getString("preferredName")+"");
 				String email=(((rs.getString("emailCurrent")==null)||(rs.getString("emailCurrent").equals("")))?
 						(((rs.getString("emailPermanent")==null)||(rs.getString("emailPermanent").equals("")))?
 								""
 								:
 								rs.getString("emailPermanent"))
 						:
 						rs.getString("emailCurrent"));
 
 				h.put("email",email);
 				c.add(h);
 			}
 			return c;
 		}
 		catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static ResultSet getContactMovements(String personID){
 		try{
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			String query=" Select mt.name as location, ma.strategy as strategy, ma.status as status, "+
 			" mt.city as city, mt.state as state, mt.region as region, mt.targetAreaID as location_id, "+
 			" ml.name as team, ml.teamID as team_id, ma.ActivityID as id, ma.url as url, ma.facebook as facebook, "+
 			" (select sum(invldStudents) as invldStudents from ministry_statistic" +
 			" where fk_Activity = ma.ActivityID and periodEnd = (select max(periodEnd) as maxPeriodEnd" +
 			" from ministry_statistic where fk_Activity = ma.ActivityID)) as size "+
 			" from ministry_targetarea mt inner join ministry_activity ma on ma.fk_targetareaid=mt.targetareaid "+
 			" inner join ministry_movement_contact mmc on mmc.ActivityID=ma.ActivityID "+
 			" inner join ministry_locallevel ml on ml.teamID=ma.fk_teamID "+
 			" where mmc.personID="+personID+"  and ma.status<>'IN'  order by  location asc, strategy asc, size desc ;";
 			log.debug(query);
 			ResultSet rs=stmt.executeQuery(query);
 			return rs;
 		}
 		catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static ResultSet getTeamMovements(String teamID){
 		try{
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			String query=" Select mt.name as location, ma.strategy as strategy, ma.status as status, "+
 			" mt.city as city, mt.state as state, mt.region as region, mt.targetAreaID as location_id, "+
 			" ml.name as team, ml.teamID as team_id, ma.ActivityID as id, ma.url as url, ma.facebook as facebook, "+
 			" (select sum(invldStudents) as invldStudents from ministry_statistic" +
 			" where fk_Activity = ma.ActivityID and periodEnd = (select max(periodEnd) as maxPeriodEnd" +
 			" from ministry_statistic where fk_Activity = ma.ActivityID)) as size "+
 			" from ministry_targetarea mt inner join ministry_activity ma on ma.fk_targetareaid=mt.targetareaid "+
 			" inner join ministry_locallevel ml on ml.teamID=ma.fk_teamID "+
 			" where ma.fk_teamID="+teamID+" and ma.status<>'IN'  order by location asc, strategy asc, size desc  ;";
 			log.debug(query);
 			ResultSet rs=stmt.executeQuery(query);
 			return rs;
 		}
 		catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static ResultSet getLocationMovements(String taID){
 		try{
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			String query=" Select mt.name as location, ma.strategy as strategy, ma.status as status, "+
 			" mt.city as city, mt.state as state, mt.region as region, mt.targetAreaID as location_id, "+
 			" ml.name as team, ml.teamID as team_id, ma.ActivityID as id, ma.url as url, ma.facebook as facebook, "+
 			" (select sum(invldStudents) as invldStudents from ministry_statistic" +
 			" where fk_Activity = ma.ActivityID and periodEnd = (select max(periodEnd) as maxPeriodEnd" +
 			" from ministry_statistic where fk_Activity = ma.ActivityID)) as size, "+
 			" leaders.leader_id as leader_id "+
 			" from ministry_targetarea mt inner join ministry_activity ma on ma.fk_targetareaid=mt.targetareaid "+
 			" inner join ministry_locallevel ml on ml.teamID=ma.fk_teamID "+
 			" left join (select group_concat(tm.personID separator ',') as leader_id, tm.teamID as teamID "+
 			" from ministry_missional_team_member tm where tm.is_leader=1 group by tm.teamID) leaders on leaders.teamID=ml.teamID  "+
 			" where ma.fk_targetAreaId="+taID+" and ma.status<>'IN' order by team asc, strategy asc, size desc ;";
 			log.debug(query);
 			ResultSet rs=stmt.executeQuery(query);
 			
 			return rs;
 		}
 		catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static Section getTeamMembers(String teamID){
 		try{
 			Section c=new Section();
 			c.setType("Person");
 			c.setName("Missional Team Members");
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			String query="SELECT ministry_missional_team_member.personID as personID, ministry_person.firstName as firstName,"+
 			" ministry_missional_team_member.is_leader as is_leader, "+
 			" ministry_missional_team_member.is_people_soft as is_people_soft, "+
 			" ministry_person.preferredName as preferredName, ministry_person.lastName as lastName, max(mna1.email) as emailCurrent,  max(mna2.email) as emailPermanent,"+
 			" ministry_person.accountNo as accountNo, max(mna1.city) as city, max(mna1.state) as state "+
 			" FROM (ministry_missional_team_member inner join ministry_person "+
 			" on ministry_person.personID=ministry_missional_team_member.personID Left JOIN ministry_newaddress mna1 "+
 			" ON (ministry_person.personID = mna1.fk_PersonID and mna1.addressType='current' )Left JOIN ministry_newaddress mna2"+
 			" ON (ministry_person.personID = mna2.fk_PersonID and mna2.addressType='permanent' )) " +
 			" WHERE ministry_missional_team_member.teamID ='"+teamID+"'  and not(isSecure<=>'T') group by ministry_person.personID order by lastName, firstName;";
 			log.debug(query);
 			ResultSet rs = stmt.executeQuery(query);
 			while (rs.next()){
 				Hashtable<String,Object> h=new Hashtable<String,Object>();
 				h.put("id",rs.getString("personID")+"");
 				h.put("is_leader",rs.getString("is_leader")==null?false:rs.getString("is_leader").equals("1"));
 				h.put("is_people_soft",rs.getString("is_people_soft")==null?false:rs.getString("is_people_soft").equals("1"));
 				h.put("accountNo",rs.getString("accountNo")+"");
 				h.put("city",rs.getString("city")+"");
 				h.put("state",rs.getString("state")+"");
 				String makeName="";
 				if(((String)rs.getString("firstName")+"").equals(rs.getString("preferredName")+"")){
 					makeName=rs.getString("firstName")+" "+rs.getString("lastName")+"";
 				} else {
 					makeName=rs.getString("firstName")+" ("+rs.getString("preferredName")+") "+rs.getString("lastName")+"";
 				}
 				h.put("name", makeName);
 				h.put("firstName",rs.getString("firstName")+"");
 				h.put("lastName",rs.getString("lastName")+"");
 				h.put("preferredName",rs.getString("preferredName")+"");
 				String email=(((rs.getString("emailCurrent")==null)||(rs.getString("emailCurrent").equals("")))?
 						(((rs.getString("emailPermanent")==null)||(rs.getString("emailPermanent").equals("")))?
 								""
 								:
 								rs.getString("emailPermanent"))
 						:
 						rs.getString("emailCurrent"));
 
 				h.put("email",email);
 				c.addRow(h);
 			}
 			return c;
 		}
 		catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 	public static Boolean isTeamLeader(Person person,LocalLevel ll)throws Exception
     {
     	
         String personID = person.getPersonID()+"";
        String llid=ll.getLocalLevelId();
        Connection conn = DBConnectionFactory.getDatabaseConn();
 		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		String query="SELECT max(ministry_missional_team_member.is_leader) as is_leader "+
 		" FROM ministry_missional_team_member  WHERE ministry_missional_team_member.teamID ="+llid+
 		" and ministry_missional_team_member.personID="+personID+" group by ministry_missional_team_member.teamID, ministry_missional_team_member.personID  ; ";
 		log.debug(query);
 		ResultSet rs = stmt.executeQuery(query);
 		if (rs.next()){
 		if(rs.getString("is_leader")==null){
 			 return false;
 		} else {
 			return rs.getString("is_leader").equals("1");
 		}
 		}
 		return false;
     }
 
 	public static Section listTeamsForPerson(String personID){
 		try{
 			Section t=new Section();
 			t.setType("Team");
 			t.setName("Missional Teams");
 			Connection conn = DBConnectionFactory.getDatabaseConn();
 			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			String query="SELECT ministry_missional_team_member.teamID, ministry_locallevel.name as name, "+
 			" ministry_locallevel.city as city, ministry_locallevel.state as state, "+
 			" ministry_locallevel.region as region, ministry_locallevel.lane as strategy "+
 			" FROM (ministry_missional_team_member inner join ministry_locallevel "+
 			" on ministry_missional_team_member.teamID=ministry_locallevel.teamID) " +
 			" WHERE ministry_missional_team_member.personID ='"+personID+"' order by ministry_locallevel.name;";
 			log.debug(query);
 			ResultSet rs = stmt.executeQuery(query);
 			Hashtable<String,Object> h=new Hashtable<String,Object>();
 			while (rs.next()){
 				h=new Hashtable<String,Object>();
 				h.put("id", rs.getString("teamID")+"");
 				h.put("name", rs.getString("name")+"");
 				h.put("city", rs.getString("city")+"");
 				h.put("state", rs.getString("state")+"");
 				h.put("region", rs.getString("region")+"");
 				h.put("strategy", rs.getString("strategy")+"");
 				t.addRow(h);
 			}
 			return t;
 		}
 		catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 		
 	}
 }
