 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.db;
 
 import java.io.IOException;
 import java.sql.*;
 
 import util.misc.*;
 
 /** 
  * Used to obtain a version of the SQL queries used in Om for a given database.
  * This base class provides (fairly) standard SQL versions. Needs to be 
  * overridden for each supported database.
  */
 public abstract class OmQueries
 {
 	private final String prefix;
 
 	/**
 	 * @param prefix String prepended to the name of each database table.
 	 */
 	protected OmQueries(String prefix) {
 		this.prefix = prefix;
 	}
 
 	/**
 	 * Obtains JDBC URL for this database. Must also call Class.forName for the
 	 * JDBC driver.
 	 * @param server Database server
 	 * @param database Database name
 	 * @param username Username
 	 * @param password Password
 	 * @return JDB string
 	 * @throws ClassNotFoundException 
 	 */
 	public abstract String getURL(String server,String database,String username,String password)
 	  throws ClassNotFoundException;
 
 	public ResultSet querySummary(DatabaseAccess.Transaction dat,int ti) throws SQLException
 	{
 		return dat.query(
 			"SELECT tq.questionnumber,q.finished,r.questionline,r.answerline,tq.question,r.attempts,tq.sectionname "+ 
 			"FROM " + getPrefix() + "testquestions tq " +
 			"LEFT JOIN " + getPrefix() + "questions q ON tq.question=q.question AND tq.ti=q.ti " +
 			"LEFT JOIN " + getPrefix() + "results r ON q.qi=r.qi " +
 			"WHERE tq.ti="+ti+" " +
 			"ORDER BY tq.questionnumber, q.attempt DESC");
 	}
 	
 	public ResultSet queryUnfinishedSessions(DatabaseAccess.Transaction dat,String oucu,String deploy) throws SQLException
 	{
 		return dat.query( 
 			"SELECT ti,rseed,finished,variant,testposition " +
 			"FROM " + getPrefix() + "tests " +
 			"WHERE oucu="+Strings.sqlQuote(oucu)+" AND deploy="+Strings.sqlQuote(deploy)+" " +
 			"ORDER BY attempt DESC LIMIT 1");
 	}
 	
 	public ResultSet queryScores(DatabaseAccess.Transaction dat,int ti) throws SQLException
 	{
 		return dat.query(
 			"SELECT tq.question,q.majorversion,q.minorversion,q.attempt,s.axis,s.score,tq.requiredversion "+ 
 			"FROM " + getPrefix() + "testquestions tq " +
 			"LEFT JOIN " + getPrefix() + "questions q ON tq.question=q.question AND tq.ti=q.ti " +
 			"LEFT JOIN " + getPrefix() + "scores s ON s.qi=q.qi " +
 			"WHERE tq.ti="+ti+" " +
 			"ORDER BY tq.question,SIGN(q.finished) DESC,q.attempt DESC;");	
 	}
 	
 	public ResultSet queryQuestionAttemptCount(DatabaseAccess.Transaction dat,int ti)
 	  throws SQLException
 	{
 		return dat.query(
 			"SELECT COUNT(qi) " +
 			"FROM " + getPrefix() + "questions " +
 			"WHERE ti="+ti+";");	
 	}
 	
 	public ResultSet queryDoneQuestions(DatabaseAccess.Transaction dat,int ti)
 	  throws SQLException
 	{
 		return dat.query(
 			"SELECT DISTINCT q.question " +
 			"FROM " + getPrefix() + "questions q " +
 			"WHERE q.ti="+ti+" AND q.finished>=1");	
 	}
 	
 	public ResultSet queryDoneInfoPages(DatabaseAccess.Transaction dat,int ti)
 	  throws SQLException
 	{
 		return dat.query(
 			"SELECT testposition " +
 			"FROM " + getPrefix() + "infopages " +
 			"WHERE ti="+ti+";");	
 	}
 	
 	public ResultSet queryMaxQuestionAttempt(DatabaseAccess.Transaction dat,int ti,String questionID)
 	  throws SQLException
 	{
 		return dat.query(
 			"SELECT MAX(attempt) " +
 			"FROM " + getPrefix() + "questions " +
 			"WHERE ti="+ti+" AND question="+Strings.sqlQuote(questionID)+";");	
 	}
 	
 	public ResultSet queryMaxTestAttempt(DatabaseAccess.Transaction dat,String oucu,String testID)
 		throws SQLException
   {
 		return dat.query(
 			"SELECT MAX(attempt) " +
 			"FROM " + getPrefix() + "tests " +
 			"WHERE oucu="+Strings.sqlQuote(oucu)+" AND deploy="+Strings.sqlQuote(testID)+";");
 	}
 	
 	public ResultSet queryQuestionActions(DatabaseAccess.Transaction dat,int ti,String questionID)
 		throws SQLException
 	{
 		return dat.query(
 			"SELECT q.qi, MAX(a.seq), q.finished,q.attempt,q.majorversion,q.minorversion "+ 
 			"FROM " + getPrefix() + "questions q "+
 			"LEFT JOIN " + getPrefix() + "actions a ON q.qi=a.qi "+
 			"WHERE q.ti="+ti+" AND q.question="+Strings.sqlQuote(questionID)+" "+
 			"AND attempt=("+
 				"SELECT MAX(attempt) FROM " + getPrefix() + "questions q2 "+
 				"WHERE q2.ti="+ti+" AND q2.question="+Strings.sqlQuote(questionID)+") "+
 			"GROUP BY q.qi,q.finished,q.attempt,q.majorversion,q.minorversion");
 	}
 
 	public ResultSet queryQuestionActionParams(DatabaseAccess.Transaction dat,int qi)
 		throws SQLException
 	{
 		return dat.query(
 			"SELECT seq, paramname, paramvalue " +
 			"FROM " + getPrefix() + "params " +
 			"WHERE qi="+qi+" "+
 			"ORDER BY seq");
 	}
 	
 	public ResultSet queryQuestionResults(DatabaseAccess.Transaction dat,int qi)
 		throws SQLException
 	{
 		return dat.query(
 			"SELECT questionline,answerline,attempts " +
 			"FROM " + getPrefix() + "results " +
 			"WHERE qi="+qi	);
 	}
 	
 	public abstract void checkDatabaseConnection(DatabaseAccess.Transaction dat)
 		throws SQLException;
 	
 	public ResultSet queryTestAttempters(DatabaseAccess.Transaction dat,String testID)
 	  throws SQLException
 	{
 		return dat.query(
 			"SELECT oucu,pi,clock,finished,admin,finishedclock " +
 			"FROM " + getPrefix() + "tests t " +
 			"WHERE deploy="+Strings.sqlQuote(testID)+" " +
 			"AND (SELECT COUNT(*) FROM " + getPrefix() + "questions q WHERE q.ti=t.ti AND finished>0)>0 "+
 			"ORDER BY finished DESC,pi,clock DESC");
 	}
 	
 	public ResultSet queryQuestionList(DatabaseAccess.Transaction dat,String testID)
 		throws SQLException
 	{
 		return dat.query(
 			"SELECT q.question, sum(s.score),count(s.score),max(q.majorversion),tq.questionnumber " +
 			"FROM " + getPrefix() + "tests t " +				
 			"LEFT JOIN " + getPrefix() + "testquestions tq ON t.ti=tq.ti "+
 			"LEFT JOIN " + getPrefix() + "questions q ON t.ti=q.ti AND q.question=tq.question " +
 			"LEFT JOIN " + getPrefix() + "scores s ON q.qi=s.qi " +
 			"WHERE s.axis='' AND s.score IS NOT NULL " +
 			"AND t.deploy="+Strings.sqlQuote(testID)+" "+
 			"GROUP BY q.question,tq.questionnumber " +
 			"ORDER BY tq.questionnumber");
 	}
 	
 	public ResultSet queryQuestionReport(DatabaseAccess.Transaction dat,String testID,String questionID)
 	  throws SQLException
   {
 		return dat.query(
 	  		"SELECT t.pi,t.oucu,q.attempt,q.clock," +
 			  "r.questionline,r.answerline,r.actions,r.attempts,s.axis,s.score "+
 			"FROM " + getPrefix() + "tests t " +
 			"INNER JOIN " + getPrefix() + "questions q ON t.ti=q.ti " +
 			"INNER JOIN " + getPrefix() + "results r ON q.qi=r.qi " +
 			"LEFT JOIN " + getPrefix() + "scores s ON q.qi=s.qi " +
 			"WHERE t.deploy="+Strings.sqlQuote(testID)+" AND q.question="+Strings.sqlQuote(questionID)+" AND q.finished>0 " +
 			"ORDER BY t.pi,q.attempt");
   }
 	
 	public ResultSet queryUserReportSessions(DatabaseAccess.Transaction dat,String testID,String pi)
 	  throws SQLException
 	{
 		return dat.query(
 			"SELECT t.attempt,si.clock,si.ip,si.useragent " +
 			"FROM " + getPrefix() + "tests t " +
 			"INNER JOIN " + getPrefix() + "sessioninfo si ON t.ti=si.ti " +
 			"WHERE t.deploy="+Strings.sqlQuote(testID)+" AND t.pi="+Strings.sqlQuote(pi)+" " +
 			"ORDER BY t.attempt,si.clock");
 	}
 	
 	public ResultSet queryUserReportTest(DatabaseAccess.Transaction dat,String testID,String pi)
 	  throws SQLException
 	{
 		return dat.query(
 			"SELECT t.attempt,t.finished,q.question,q.attempt,q.clock,r.questionline,r.answerline,r.actions,r.attempts,s.axis,s.score,tq.questionnumber,t.finishedclock," +
 				"(SELECT MIN(a2.clock) FROM " + getPrefix() + "actions a2 WHERE a2.qi=q.qi) AS minaction,"+
 				"(SELECT MAX(a2.clock) FROM " + getPrefix() + "actions a2 WHERE a2.qi=q.qi) AS maxaction "+
 			"FROM " + getPrefix() + "tests t " +
 				"INNER JOIN " + getPrefix() + "questions q ON t.ti=q.ti " +
 				"INNER JOIN " + getPrefix() + "testquestions tq ON t.ti=tq.ti AND q.question=tq.question "+
 				"INNER JOIN " + getPrefix() + "results r ON q.qi=r.qi " +
 				"LEFT OUTER JOIN " + getPrefix() + "scores s ON q.qi=s.qi " +
 			"WHERE t.deploy="+Strings.sqlQuote(testID)+" AND t.pi="+Strings.sqlQuote(pi)+" AND q.finished>0 " +
 			"ORDER BY t.attempt,tq.questionnumber,q.clock");
 	}
 	
 	public void insertAction(DatabaseAccess.Transaction dat,int qi,int seq) 
 	  throws SQLException
 	{
 		dat.update("INSERT INTO " + getPrefix() + "actions VALUES ("+qi+","+seq+",DEFAULT);");
 	}
 	
 	public void insertParam(DatabaseAccess.Transaction dat,int qi,int seq,String name,String value)
   		throws SQLException
 	{
 		dat.update("INSERT INTO " + getPrefix() + "params VALUES ("+qi+","+seq+
 			","+Strings.sqlQuote(name)+","+unicode(Strings.sqlQuote(value))+");");
 	}
 	
 	public void insertResult(DatabaseAccess.Transaction dat,int qi,String questionLine,String answerLine,String actionSummary,
 		int attempts)
 		throws SQLException
 	{
 		dat.update("INSERT INTO " + getPrefix() + "results (qi,questionline,answerline,actions,attempts) VALUES("+
 			qi+
 			","+unicode(Strings.sqlQuote(questionLine))+
 			","+unicode(Strings.sqlQuote(answerLine))+
 			","+unicode(Strings.sqlQuote(actionSummary))+
 			","+attempts+");");
 	}
 	
 	public void insertScore(DatabaseAccess.Transaction dat,int qi,String axis,int marks)
 		throws SQLException
 	{
 		dat.update("INSERT INTO " + getPrefix() + "scores VALUES("+qi+","+
 			Strings.sqlQuote(axis)+","+marks+");");
 	}
 
 	public void insertCustomResult(DatabaseAccess.Transaction dat,int qi,String name,String value)
 		throws SQLException
 	{
 		dat.update("INSERT INTO " + getPrefix() + "customresults VALUES("+qi+
 			","+Strings.sqlQuote(name)+
 			","+unicode(Strings.sqlQuote(value))+");");
 	}
 	
 	public void insertQuestion(DatabaseAccess.Transaction dat,int ti,String questionID,int attempt)
 		throws SQLException
 	{
 		dat.update(
 			"INSERT INTO " + getPrefix() + "questions (ti,question,attempt,finished,clock,majorversion,minorversion) " +
 			"VALUES ("+ti+","+Strings.sqlQuote(questionID)+","+attempt+",0,DEFAULT,0,0);");
 	}
 	
 	public void insertTestQuestion(DatabaseAccess.Transaction dat,int ti,int number,String questionID,
 		int requiredVersion,String sectionName)
 		throws SQLException
 	{
 		dat.update(
 			"INSERT INTO " + getPrefix() + "testquestions (ti,questionnumber,question,requiredversion,sectionname) " +
 			"VALUES("+ti+","+number+","+Strings.sqlQuote(questionID)+","+
 			( requiredVersion==-1 ? "NULL" : ""+requiredVersion)+","+
 			(sectionName==null? "NULL" : Strings.sqlQuote(sectionName))+
 			");");
 	}
 	
 	public void insertTest(DatabaseAccess.Transaction dat,String oucu,String testID,
 		long rseed,int attempt,boolean admin,String pi,int fixedVariant)
 		throws SQLException
 	{
 		dat.update(
 			"INSERT INTO " + getPrefix() + "tests (oucu,deploy,rseed,attempt,finished,clock,admin,pi,variant,testposition) VALUES ("+
 			Strings.sqlQuote(oucu)+","+Strings.sqlQuote(testID)+","+
 			rseed+","+attempt+",0,DEFAULT,"+
 			(admin?"1":"0")+","+Strings.sqlQuote(pi)+
 			","+	(fixedVariant==-1 ? "NULL" : fixedVariant+"")+	",0);");
 	}
 	
 	public void insertInfoPage(DatabaseAccess.Transaction dat,int ti,int index)
 	  throws SQLException
 	{
 		dat.update("INSERT INTO " + getPrefix() + "infopages (ti,testposition) VALUES ("+ti+","+index+");");
 	}
 	
 	public void insertSessionInfo(DatabaseAccess.Transaction dat,int ti,String ip,String agent)
 	  throws SQLException
 	{
 		dat.update(
 			"INSERT INTO " + getPrefix() + "sessioninfo(ti,ip,useragent) VALUES("+ti+","+Strings.sqlQuote(ip)+","+
 			Strings.sqlQuote(agent)+");");
 	}
 	
 	public void updateQuestionFinished(DatabaseAccess.Transaction dat,int qi,int finished)
 		throws SQLException
 	{
 		dat.update("UPDATE " + getPrefix() + "questions SET finished="+finished+" WHERE qi="+qi+";");
 	}
 
 	public void updateTestFinished(DatabaseAccess.Transaction dat,int ti)
 		throws SQLException
 	{
 		dat.update("UPDATE " + getPrefix() + "tests SET finished=1,finishedclock="+currentDateFunction()+" WHERE ti="+ti+";");
 	}
 	
 	public void updateTestVariant(DatabaseAccess.Transaction dat,int ti,int variant)
 	  throws SQLException
 	{
 		dat.update("UPDATE " + getPrefix() + "tests SET variant="+variant+" WHERE ti="+ti+";");
 	}
 	
 	public void updateSetTestPosition(DatabaseAccess.Transaction dat,int ti, int position)
 	  throws SQLException
 	{
 		dat.update("UPDATE " + getPrefix() + "tests SET testposition="+position+" WHERE ti="+ti);
 	}
 	
 	public void updateSetQuestionVersion(DatabaseAccess.Transaction dat,int qi, int major,int minor)
 	  throws SQLException
 	{
 		dat.update(
 			"UPDATE " + getPrefix() + "questions " +
 			"SET majorversion="+major+", minorversion="+minor+" " +
 			"WHERE qi="+qi);
 	}
 	
 	protected boolean tableExists(DatabaseAccess.Transaction dat,String table)
 	  throws SQLException
 	{
 		ResultSet rs=dat.query("SELECT COUNT(*) FROM information_schema.tables WHERE table_name="+Strings.sqlQuote(table));
 		rs.next();
 		return rs.getInt(1)!=0;
 	}
 	
 	/** 
 	 * Checks that database tables are present. If not, initialises tables
 	 * using createdb.sql from the same package as the database class.
 	 * @param dat Transaction
 	 * @throws SQLException If there is an error in setting up tables
 	 */
 	public void checkTables(DatabaseAccess.Transaction dat) throws SQLException,IOException
 	{
 		// Check if we've already got them
 		if(tableExists(dat,getPrefix() + "tests")) return;
 
 		// Get statements
 		String sStatements=
 			IO.loadString(getClass().getResourceAsStream("createdb.sql"));
 		
 		// Remove comments
 		sStatements="!!!LINE!!!"+sStatements.replaceAll("\n","!!!LINE!!!")+"!!!LINE!!!";
 		sStatements=sStatements.replaceAll("!!!LINE!!!--.*?(?=!!!LINE!!!)","");
 		sStatements=sStatements.replaceAll("!!!LINE!!!"," ");
 		sStatements=sStatements.replaceAll("\\s"," ");
 
 		// Replace the database prefix
 		sStatements=sStatements.replaceAll("prefix_",getPrefix());
 
 		// Split on ; and process each line
 		String[] asStatements=sStatements.split(";");			
 		for(int i=0;i<asStatements.length;i++)
 		{
 			String sStatement=asStatements[i].replaceAll("\\s+"," ").trim()+";";
 			if(sStatement.equals(";")) continue;
 			try
 			{
 				dat.update(sStatement);
 			}
 			catch(SQLException se)
 			{
 				throw new SQLException("SQL statement:\n\n"+sStatement+"\n\n"+se.getMessage());
 			}
 		}		
 	}
 
 	protected String currentDateFunction()
 	{
 		return "current_timestamp";
 	}
 	
 	/**
 	 * @param quotedString String surrounded in quotes and quoted as appropriate
 	 *   for normal SQL.
 	 * @return Same string also quoted with any necessary changes bearing in mind
 	 *   that this string may contain non-ASCII characters (for sane databases,
 	 *   this is the same as the input, but not all databases are sane)
 	 */
 	protected String unicode(String quotedString)
 	{
 		return quotedString;
 	}
 	
 
 	/**
 	 * Immediately after an insert into a table that contains an auto-generated
 	 * sequence column, this method returns the value of the ID that was just
 	 * added.
 	 * @param dat Transaction (insert must be previous command)
	 * @param table Table name
 	 * @param column Column name
 	 * @return ID value
 	 * @throws SQLException If database throws a wobbly
 	 */
 	public abstract int getInsertedSequenceID(DatabaseAccess.Transaction dat,
 		String table,String column) throws SQLException;
 
 	/**
 	 * @return the prefix
 	 */
 	public String getPrefix() {
 		return prefix;
 	}
 }
