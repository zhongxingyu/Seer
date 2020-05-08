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
 package om.helper;
 
 import om.*;
 import om.question.*;
 import om.stdcomponent.BoxComponent;
 import om.stdquestion.*;
 
 import org.w3c.dom.*;
 
 import util.xml.XML;
 
 /** 
  * Base class for simple questions that have an input box and an answer box 
  * (simultaneously visible) and match certain other 'standard' requirements 
  * in their XML. 
  * <p> 
  * Specifically, the XML must contain the following component IDs:
  * <ul>
  * <li>inputbox - box that contains controls into which user enters their
  *   answers</li>
  * <li>answerbox - box that contains information about the answer, e.g. was
  *   it right or not</li>
  * <li>wrong - component within answerbox, shown only if answer is wrong</li>
  * <li>still - component within answerbox, shown only if answer is wrong and
  *   it's not the first time</li>
  * <li>right - component within answerbox, shown only if answer is right</li>
  * <li>pass - component within answerbox, shown only if user passed</li>
  * <li>feedback - component within answerbox, shown only if getFeedbackID()
  *   returns something other than null.
  * </ul>
  * There should be examples matching this pattern in the documentation. 
  */
 public abstract class SimpleQuestion1 extends StandardQuestion
 {
   /** Which question attempt the user is on */
   private int iAttempt=1;
   
   /** Maximum number of attempts allowed (after that it tells you the answer) */
   private int iMaxAttempts=3;
   
   /** If true, question ends when you click OK */
   private boolean bEndNext=false;
   
   /** ID used for feedback */
   private String sFeedbackID;
   
   /** Max marks, or 0 if no scoring */
   private int iMaxMarks=0;
   
  @Override
   public Rendering init(Document d,InitParams ip) throws OmException
 	{
 		Rendering r=super.init(d,ip);
 		if(iMaxAttempts==1)
 			r.setProgressInfo("You have only one attempt.");
 		else
 			r.setProgressInfo("You have "+iMaxAttempts+" attempts.");
 		
 		try
 		{
 			Element eScoring=XML.getChild(d.getDocumentElement(),"scoring");
 			iMaxMarks=Integer.parseInt(XML.getText(eScoring,"marks"));
 		}
 		catch(Exception e)
 		{
 			// Ignore, leave iMaxMarks at 0			
 		}
 		
 		return r;
 	}
   
   /**
    * Set the maximum number of attempts permitted. After that it will tell you
    * the answer. The default is 3.
    * @param iMaxAttempts Number of permitted attempts
    */
   protected void setMaxAttempts(int iMaxAttempts)
   {
   	this.iMaxAttempts=iMaxAttempts;
   }
   
   /**
    * Callback that Om calls when the user clicks the 'Submit' button to enter
    * their answer. Calls checkAnswer for further processing.
    * @throws OmException
    */
   public void actionSubmit() throws OmException
   {
 		checkAnswer(false);
   }
   
   /**
    * Callback that Om calls when the user clicks the 'Give Up' button to enter
    * their answer. Calls checkAnswer for further processing.
    * @throws OmException
    */
   public void actionGiveUp() throws OmException
   {
   	getResults().appendActionSummary("Passed");
   	getResults().setAnswerLine("Passed");
   	checkAnswer(true);
   }
 
   /**
    * Callback that Om calls when the user clicks the 'OK' button after seeing
    * the response to their answer. This either ends the question, or hides
    * the answer box and re-enables the input box for another attempt.
    * @throws OmException
    */
   public void actionOK() throws OmException
   {
   	if(bEndNext) 
   		end();
   	else
   	{
 			int iAttemptsLeft=(iMaxAttempts-iAttempt+1);
 			if(iAttemptsLeft==1)
 				setProgressInfo("This is your last attempt.");
 			else
 				setProgressInfo("You have "+iAttemptsLeft+" attempts left.");
   		
   		getComponent("answerbox").setDisplay(false);
   		getComponent("inputbox").setBoolean(BoxComponent.PROPERTY_PLAINHIDE,false);
   		getComponent("inputbox").setEnabled(true);
   	}
   }
   
   /**
    * Handles the framework around checking the user's answer.
    * @param bPass True if the user passed rather than submitting an answer
    * @throws OmDeveloperException
    */
 	private void checkAnswer(boolean bPass) throws OmDeveloperException
 	{
 		// Disable input, show answer
 		getComponent("inputbox").setEnabled(false);
 		getComponent("inputbox").setBoolean(BoxComponent.PROPERTY_PLAINHIDE,true);
 		getComponent("answerbox").setDisplay(true);
 
 		sFeedbackID=null;
 		
 		// Get the three basic states
 		boolean bRight=false;
 		if(!bPass)
 		{
 			bRight=isRight(iAttempt); 
 		}
 		boolean bWrong=!bRight && !bPass;
 		
 		// OK now show/hide the basic 'you were wrong' bit
 		getComponent("wrong").setDisplay(bWrong);
 		getComponent("still").setDisplay(bWrong && iAttempt>1);
 		getComponent("right").setDisplay(bRight);
 		getComponent("pass").setDisplay(bPass);
 		
 		// Now handle feedback
 		QComponent qcFeedback=getComponent("feedback");
 		if(bWrong)
 		{
 			qcFeedback.setDisplay(sFeedbackID!=null);
 			if(sFeedbackID!=null && !sFeedbackID.equals(""))
 			{
 				QComponent[] aqc=qcFeedback.getComponentChildren();
 				for(int i=0;i<aqc.length;i++)
 				{
 					if(aqc[i].hasUserSetID())
 						aqc[i].setDisplay(aqc[i].getID().equals(sFeedbackID));
 				}
 			}
 		}
 		else qcFeedback.setDisplay(false);		
 		
 		// Should we end next time?
 		bEndNext=bRight||(iAttempt==iMaxAttempts)||bPass;
 				
 		// If so, show answer and update score
 		getComponent("answer").setDisplay(bEndNext);
 		if(bEndNext)
 		{
 			setScore(bRight,bPass,iAttempt);
 			sendResults();
 			try
 			{
 				getComponent("ok").setDisplay(false);
 				getComponent("next").setDisplay(true);
 			}
 			catch(OmDeveloperException ode)
 			{
 				// Ignore, just means question.xml hasn't been update
 				log("Warning: question.xml should be updated to make OK and Next buttons work");
 			}
 		}
 
 		// Give overriders a chance
 		doAdditionalAnswerProcessing(bRight,bWrong,bPass,iAttempt);
 		
 		// Increment feedback level
 		iAttempt++;
 
 		// Clear progress info (looks bad esp. in plain mode)
 		setProgressInfo("");
 	}
 
 	/**
 	 * Override this method to check whether the user's answer is correct or not.
 	 * <p>
 	 * This is a simple right/wrong flag. To provide feedback as to how they were
 	 * wrong, you should call setFeedbackID() [probably only for wrong answers
 	 * and certain attempts]. 
 	 * @param attempt Question attempt (1 for first, 2 for second, etc.)
 	 * @return True if the user is right; false if they are wrong.
 	 * @throws OmDeveloperException
 	 */
   protected abstract boolean isRight(int attempt) throws OmDeveloperException;
 
   /**
    * Override this method to change the scoring. The method should call
    * getResults().setScore.
    * <p>
    * Default scoring is out of 3 points: 3 for correct answer first time, 
    * 2 second time, 1 third time, 0 for fail or pass.  
    * @param bRight True if user was right (rather than passing/failing)
    * @param bPass True if user passed on question
    * @param iAttempt Attempt number (1 = first attempt)
    */
   protected void setScore(boolean bRight,boolean bPass,int iAttempt) throws OmDeveloperException
   {
   	if(iMaxMarks==0) throw new OmDeveloperException(
   			"Cannot set score on question: <scoring> not defined in question.xml");
   	if(!bRight) getResults().setScore(0,bPass ? Results.ATTEMPTS_PASS : Results.ATTEMPTS_WRONG);
   	else getResults().setScore(Math.max(0,iMaxMarks+1-iAttempt),iAttempt);
   }
   
   /**
    * Sets the feedback ID for current question attempt. Should be called from 
    * within isRight(). 
    * <p>
    * Feedback works as follows:
    * <ul>
    * <li>You must have a component with id 'feedback'. This component will
    *   be hidden if this method returns null, and shown otherwise.</li>
    * <li>If you return "" then no further action is taken. Otherwise you must
    *   have multiple components with IDs, directly inside the 'feedback' 
    *   component. These will all be hidden except the one named by the
    *   return value. Components which don't have IDs will be unaffected.</li> 
    * </ul>
    * @param sID ID of component to use for feedback. May be null if there is 
    *   no feedback this time, or an empty string to display just the 'feedback' 
    *   component without messing with its children 
    */
   protected void setFeedbackID(String sID)
   {
   	sFeedbackID=sID;
   }
 
   /**
    * Override this method if you want to do any additional processing with
    * the user's answer. This is called after all default processing.
    * @param bRight True if user was right
    * @param bWrong True if they were wrong
    * @param bPass True if they passed
    * @param attempt Attempt number (1 is first attempt)
    * @throws OmDeveloperException
    */
 	protected void doAdditionalAnswerProcessing(
 		boolean bRight,boolean bWrong,boolean bPass,int attempt) throws OmDeveloperException
 	{
 		// Default does nothing
 	}    
 }
