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
 package om.question;
 
 import java.util.HashMap;
 
 import om.OmDeveloperException;
 
 /** Question results (scores)
  * <p>
  * API CLASS: This class is used in SOAP returns and should probably not be
  * altered (after initial release).
  */
 public class Results
 {
 	/** Question summary */
 	private String sQuestionLine=null;
 
 	/** Answer summary */
 	private String sAnswerLine=null;
 
 	/** Summary of actions */
 	private String sActionSummary=null;
 
 	/** Stores information about the number of attempts taken to get question right (1=first time) */
 	private int iAttempts=ATTEMPTS_UNSET;
 
 	/** User passed on question */
 	public final static int ATTEMPTS_PASS=0;
 
 	/** User got question wrong after all attempts */
 	public final static int ATTEMPTS_WRONG=-1;
 
 	/** User got question partially correct after all attempts */
 	public final static int ATTEMPTS_PARTIALLYCORRECT=-2;
 
 	/** If developer hasn't set the value */
 	public final static int ATTEMPTS_UNSET=-99;
 
 	/** Hashmap of all scores based on axis (String -> Score) */
 	private HashMap<String, Score> hmScores=new HashMap<String, Score>();
 
 	/** Hashmap of custom results (String [id] -> CustomResult) */
 	private HashMap<String, CustomResult> hmCustomResults=new HashMap<String, CustomResult>();
 
 	/** @return One-line summary of question (may be null) */
 	public String getQuestionLine() { return sQuestionLine; }
 	/** @return One-line summary of answer (may be null) */
 	public String getAnswerLine() { return sAnswerLine; }
 	/** @return Summary of user actions (may be null) */
 	public String getActionSummary() { return sActionSummary; }
 	/**
 	 * @return Number of attempts taken to get correct answer; 1=first attempt.
 	 *   ATTEMPTS_PASS(0)=user passed, ATTEMPTS_WRONG(-1)=user failed all attempts,
 	 *   ATTEMPTS_UNSET(-99)=value has not been set
 	 */
 	public int getAttempts() { return iAttempts; }
 
 	/**
 	 * Sets the one-line question summary.
 	 * <p>
 	 * Example: 'Select 5 equations equivalent to 4n.'
 	 * <p>
 	 * Where possible, the summary should define the specific question asked.
 	 * However, some questions may be impossible to accurately summarise in
 	 * a single line.
 	 * @param sText One-line question summary
 	 * @throws OmDeveloperException If sText is longer than 255 characters
 	 */
 	public void setQuestionLine(String sText) throws OmDeveloperException
 	{
 		if(sText.length() > 255) throw new OmDeveloperException(
 			"Question line must be <= 255 characters long");
 		sQuestionLine=sText;
 	}
 
 	/**
 	 * Sets the one-line answer summary.
 	 * <p>
 	 * Example: 'Mean 0.45, Median 0, Mode 1'
 	 * <p>
 	 * Where possible, the summary should define the specific answer the user
 	 * gave. In some cases this may be impossible, for reasons of length or
 	 * the text-only format.
 	 * @param sText One-line answer summary
 	 * @throws OmDeveloperException If sText is longer than 255 characters
 	 */
 	public void setAnswerLine(String sText) throws OmDeveloperException
 	{
 		if(sText.length() > 255) throw new OmDeveloperException(
 			"Answer line must be <= 255 characters long");
 		sAnswerLine=sText;
 	}
 
 	/**
 	 * Appends text to the action summary. This is a longer summary of the
 	 * user's actions, intended for admin view only. Supposing they get a
 	 * question right on the third try, this would typically include the
 	 * first and second answers too. It is not restricted in length (though
 	 * shouldn't be ridiculous) and may contain any other information about the
 	 * question that should be recorded for easy access by admin staff.
 	 * @param sText Additional information that will be appended to the action
 	 *   summary. (You don't need to end with whitespace; a newline is
 	 *   automatically included at the end of each entry.)
 	 */
 	public void appendActionSummary(String sText)
 	{
 		if(sActionSummary==null) sActionSummary="";
 		sActionSummary+=sText+"\n";
 	}
 
 	/**
 	 * Sets the question numerical result (on default score axis).
 	 * @param iMarks Marks obtained
 	 * @param iAttempts Attempts taken to get result, or an ATTEMPTS_xx constant
 	 * @throws OmDeveloperException
 	 */
 	public void setScore(int iMarks,int iAttempts) throws OmDeveloperException
 	{
 	  this.iAttempts=iAttempts;
 		setScore(null,iMarks);
 	}
 
 	/**
 	 * Sets the question numerical result (on specified score axis). If a result
 	 * has already been set in a particular axis, that result is replaced. When
 	 * calling this method, be sure to set the attempts value as well.
 	 * @param sAxis Score axis ID (null = default)
 	 * @param iMarks Marks obtained
 	 * @throws OmDeveloperException If axis name is not a valid ID
 	 */
 	public void setScore(String sAxis,int iMarks) throws OmDeveloperException
 	{
 		checkID(sAxis);
 		hmScores.put(sAxis,new Score(sAxis,iMarks));
 	}
 
 	/**
 	 * Sets the number of attempts taken to get question right. Must be called
 	 * if setting score on individual axes; can be set as part of setScore
 	 * for single-axis results.
 	 * @param iAttempts 1 = user got question
 	 *   right on first attempt, etc.; or use an ATTEMPTS_xx constant
 	 */
 	public void setAttempts(int iAttempts)
 	{
 		this.iAttempts=iAttempts;
 	}
 
 	private void checkID(String sID) throws OmDeveloperException
 	{
 		if(sID==null) return;
		if(!sID.matches("[A-Za-z0-9_-.!]*"))
 			throw new OmDeveloperException("Not a valid ID string (disallowed characters): "+sID);
 		if(sID.length()>64)
 			throw new OmDeveloperException("Not a valid ID string (>64 characters): "+sID);
 	}
 
 	/**
 	 * Sets a custom question result. Custom results are not interpreted by the
 	 * test navigator but may be analysed in later processing.
 	 * @param sName Name (ID) for result
 	 * @param sValue Value of result
 	 * @throws OmDeveloperException If name isn't a valid ID string
 	 */
 	public void setCustomResult(String sName,String sValue) throws OmDeveloperException
 	{
 		checkID(sName);
 		hmCustomResults.put(sName,new CustomResult(sName,sValue));
 	}
 
 	/**
 	 * @return All question scores that have been set.
 	 */
 	public Score[] getScores()
 	{
 		return hmScores.values().toArray(
 			new Score[hmScores.values().size()]);
 	}
 
 	/**
 	 * @return All custom results.
 	 */
 	public CustomResult[] getCustomResults()
 	{
 		return hmCustomResults.values().toArray(
 			new CustomResult[hmCustomResults.values().size()]);
 	}
 }
