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
 package samples.numeric.numbersandunits;
 
 import java.util.*;
 import om.*;
 import om.helper.*;
 
 /** Mathematical skills Q18b: tests student can read off a solubility curve */
 public class Solubility extends SimpleQuestion1
 {
 
 	private int mass;
 	private double temperature;
 	private int nCorrect;
 
 	protected void init() throws OmException
 	{
	  mass = 5 + (int)(Math.random()*6);
 	  switch (mass)
 	  {
 	    case 5:  temperature = 52.0; break;
 	    case 6:  temperature = 60.0; break;
 	    case 7:  temperature = 68.0; break;
 	    case 8:  temperature = 72.0; break;
 	    case 9:  temperature = 78.0; break;
 	    case 10: temperature = 82.0; break;
 	  }
 
 		setPlaceholder("MASS",Integer.toString(mass));
 		setPlaceholder("4MASS",Integer.toString(4*mass));
 		setPlaceholder("ANS", Integer.toString(4*mass-5));
 
    	getResults().setQuestionLine("What is the solubility of potassium chlorate at " +
     								 						 Double.toString(temperature) + " �C?");
 	}
 
 	protected boolean isRight(int iAttempt) throws OmDeveloperException
 	{
 		//ensure all messages are turned off
 		getComponent("unitsWrong").setDisplay(false);
 
 		double dbl;
 		String answer, answer2;
 
 		nCorrect = 0;
 
 		answer = (getEditField("input").getValue().trim());
 
 		getResults().appendActionSummary("Correct ans:" + (4*mass-5) + " Attempt " + iAttempt + ": " + answer);
 
 		answer = replace(answer,"g"," g");
 		StringTokenizer st = new StringTokenizer(answer, " ");
 		try {
 			dbl = Double.parseDouble(st.nextToken());  // evaluate first number
 		}
 		catch (NumberFormatException nfe) {
 			dbl = 0;
 		}
 		catch (NoSuchElementException nsee) {
 			dbl = 0;
 		}
 
 		answer2 = replace(answer, " ", "");	// knock out spaces
 		answer2 = replace(answer2, "per", "/");
 		answer2 = replace(answer2, "of", "");	// to cope with 100gofwater
 
 		//add a terminating space - required by match...
 		answer2 = answer2 + " ";
 
 		if (range(dbl, ((4 * mass) - 5), 1.1))
 			++nCorrect;
 
 		Match m1 = new Match("g");
 
 		if (m1.match(answer2))
 			nCorrect += 2;
 
 		if ((nCorrect == 3) || (nCorrect == 6)) {
 			return(true);
 		}
 		else if (nCorrect == 1 && iAttempt <= 2) {
 			getComponent("unitsWrong").setDisplay(true);
 		}
 		else if(iAttempt == 2) {
 			setFeedbackID("default");
 		}
 		return false;
 	}
 
 	public static String replace(String sSource, String sSearch, String sReplace) // courtesy Sam
 	{
 	  StringBuffer sbOutput=new StringBuffer();
 	  //search for search string repeatedly
 	  while(sSource.indexOf(sSearch)!=-1)
 	  {
 	    //add text before search
 	    int iPos=sSource.indexOf(sSearch);
 	    sbOutput.append(sSource.substring(0,iPos));
 	    //add replace string
 	    sbOutput.append(sReplace);
 	    //chop off source up to end of search string
 	    sSource=sSource.substring(iPos+sSearch.length());
 	  }
 	  //add whatever's left over
 	  sbOutput.append(sSource);
 	  return sbOutput.toString();
 	}
 
 	private boolean range(double ans, double target, double tolerance)
 	{
 	  if((ans >= (target - tolerance)) && (ans <= (target + tolerance)))
 	  	return true;
 	  else
 	   	return false ;
 	}
 
 }
 
