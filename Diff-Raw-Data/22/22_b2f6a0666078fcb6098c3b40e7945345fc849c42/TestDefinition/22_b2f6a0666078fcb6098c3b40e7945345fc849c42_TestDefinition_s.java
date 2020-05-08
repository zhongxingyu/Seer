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
 package om.tnavigator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import om.OmException;
 import om.OmFormatException;
 
 import org.w3c.dom.*;
 
 import util.xml.XML;
 import util.xml.XMLException;
 
 /** Represents the test definition XML file */
 public class TestDefinition
 {
 	private Document dTest;
 	private String sName;
 	private Element eContent,eFinal,eOptions, eConfirm;
 	private boolean bNavigation,bRedoQuestion,bRedoQuestionAuto,
 	  bRedoTest,bFreeSummary,bFreeStop,bSummaryScores,bSummaryAttempts,bSummaryQuestions,
	  bQuestionNames,bEndSummary;
 	private int iNavLocation;
	private String sLabelSet="";
 
 	private Element eConfirmParagraphs;
 	private String sConfirmButton,sConfirmTitle;
 
 	static final int NAVLOCATION_BOTTOM=0,NAVLOCATION_LEFT=1,NAVLOCATION_WIDE=2;
 
 
 	/**
 	 * Constructs test definition and checks format.
 	 * @param f File to use
 	 * @throws OmException Failure loading file or parsing XML
 	 * @throws OmFormatException Anything wrong with the specific format
 	 */
 	TestDefinition(File f) throws OmException
 	{
 		try
 		{
 			// Parse XML
 			dTest=XML.parse(f);
 		}
 		catch(IOException ioe)
 		{
 			throw new OmException("Error loading/parsing test definition: "+f,ioe);
 		}
 
 		try
 		{
 			// Get basic stuff from the XML so we don't need to throw exceptions
 			// later if it's absent
 			sName=XML.getText(dTest.getDocumentElement(),"title");
 			eContent=XML.getChild(dTest.getDocumentElement(),"content");
 			eFinal=XML.getChild(dTest.getDocumentElement(),"final");
 
 			if(XML.hasChild(dTest.getDocumentElement(),"confirm"))
 			{
 				eConfirm=XML.getChild(dTest.getDocumentElement(),"confirm");
 				eConfirmParagraphs=dTest.createElement("div");
 				for(Node n=eConfirm.getFirstChild();n!=null;n=n.getNextSibling())
 				{
 					if(n.getNodeName().equals("title"))
 						sConfirmTitle=XML.getText(n);
 					else
 						eConfirmParagraphs.appendChild(dTest.importNode(n,true));
 				}
 				if(eConfirm.hasAttribute("button"))
 					sConfirmButton=eConfirm.getAttribute("button");
 			}
 			if(eConfirmParagraphs==null)
 				eConfirmParagraphs=XML.parse(
 					"<div><p>After ending the test, you can no longer change any answers or " +
 					"complete	unfinished questions.</p>" +
 					"<p>If this test is assessed, ending the test will submit your " +
 					"answers to be officially marked.<br/><em class='warning'>Your " +
 					"results will not be counted unless you click the button " +
 					"below.</em></p></div>").getDocumentElement();
 			if(sConfirmButton==null)
 				sConfirmButton="Submit test";
 			if(sConfirmTitle==null)
 				sConfirmTitle="Are you ready to submit the test?";
 
 			eOptions=XML.getChild(dTest.getDocumentElement(),"options");
 			bNavigation="yes".equals(eOptions.getAttribute("navigation"));
 			bRedoQuestion="yes".equals(eOptions.getAttribute("redoquestion"));
 			bRedoQuestionAuto="yes".equals(eOptions.getAttribute("redoquestionauto"));
 			if(bRedoQuestionAuto) bRedoQuestion=true;
 			bRedoTest="yes".equals(eOptions.getAttribute("redotest"));
 			bFreeSummary="yes".equals(eOptions.getAttribute("freesummary"));
 			bSummaryScores="yes".equals(eOptions.getAttribute("summaryscores"));
 			bSummaryAttempts="yes".equals(eOptions.getAttribute("summaryattempts"));
 			bFreeStop="yes".equals(eOptions.getAttribute("freestop"));
 			bQuestionNames="yes".equals(eOptions.getAttribute("questionnames"));
 			bEndSummary=!"no".equals(eOptions.getAttribute("endsummary"));
 			bSummaryQuestions=!"no".equals(eOptions.getAttribute("summaryquestions"));
 			String navLocation = eOptions.getAttribute("navlocation");
 			if(navLocation.equals("left"))
 				iNavLocation=NAVLOCATION_LEFT;
 			else if(navLocation.equals("wide"))
 				iNavLocation=NAVLOCATION_WIDE;
 			else
 				iNavLocation=NAVLOCATION_BOTTOM;
 
 			if(eOptions.hasAttribute("labelset")) sLabelSet=eOptions.getAttribute("labelset");
 
 		}
 		catch(XMLException xe)
 		{
 			throw new OmFormatException("Error processing test definition: "+f,xe);
 		}
 	}
 	
 	/**
 	 * Get the value of options attribute.
 	 * @param attrbName Name of options attribute
 	 * @return Value of the attribute
 	 */
 	public String getOptionsAttribute(String attrbName) {
 		if (eOptions == null) {
 			return null;
 		}
 		return eOptions.getAttribute(attrbName);
 	}
 	
 	/**
 	 * @return true if confirm tag exists
 	 */
 	public boolean hasConfirmTag() {
 		return eConfirm != null;
 	}
 
 	/**
 	 * @return true if confirm button exists
 	 */
 	public boolean hasConfirmButton() {
 		return eConfirm != null && eConfirm.hasAttribute("button");
 	}
 
 	/**
 	 * Get the value of final summary tag attribute.
 	 * @param attribute name
 	 * @return attribute value
 	 */
 	public String getFinalSummaryAttribute(String name) {
 		if (eFinal == null || !XML.hasChild(eFinal, "summary")) {
 			return null;
 		}
 		String value = null;
 		try {
 			Element eSummary = XML.getChild(eFinal, "summary");
 			value = eSummary.getAttribute(name);
 		} catch (XMLException e) {
 			return null;
 		}
 		return value;
 	}
 	
 	/**
 	 * @return true if <rescore marks="100"/> exists under test content.
 	 */
 	public boolean hasOverallPercentage() {
 		return XML.hasChildWithAttribute(eContent, "rescore", "marks", "100");
 	}
 
 	/** @return Name of test */
 	public String getName()
 	{
 		return sName;
 	}
 
 	/**
 	 * Obtain resolved list of questions/pages in test for this user.
 	 * @param lRandomSeed Random seed for user
 	 * @return Array of items
 	 * @throws OmFormatException If there's anything wrong with the test format
 	 *   that we didn't notice already
 	 */
 	TestGroup getResolvedContent(long lRandomSeed) throws OmFormatException
 	{
 		Random r=new Random(lRandomSeed);
 		TestGroup tg=(TestGroup)getTestItem(r,eContent,null);
 		tg.numberQuestions();
 		return tg;
 	}
 
 	/**
 	 * Recursively add this element and any children to tree of items
 	 * @param r Random number generator
 	 * @param eThis This parent element
 	 * @throws OmFormatException If there's any problem with XML format
 	 */
 	private TestItem getTestItem(Random r,Element eThis,TestItem iParent) throws OmFormatException
 	{
 		String sParentTag=eThis.getTagName();
 		Element[] aeChildren=XML.getChildren(eThis);
 		if(sParentTag.equals("question"))
 		{
 			return new TestQuestion(iParent,eThis);
 		}
 		else if(sParentTag.equals("info"))
 		{
 			return new TestInfo(iParent,eThis);
 		}
 
 		// OK, it's some type of group
 		TestGroup g=new TestGroup(iParent,eThis);
 		if(sParentTag.equals("content") || sParentTag.equals("group") || sParentTag.equals("section"))
 		{
 			// Add each child in turn
 			for(int i=0;i<aeChildren.length;i++)
 			{
 				// Skip titles
 				String sTagName=aeChildren[i].getTagName();
 				if(!sTagName.equals("title") && !sTagName.equals("rescore"))
 					g.add(getTestItem(r,aeChildren[i],g));
 			}
 		}
 		else if(sParentTag.equals("random"))
 		{
 			int iChoose;
 			String sChoose=null;
 			try
 			{
 				// Get the number of items to choose; if they leave it out, we choose
 				// everything, but just shuffled
 				sChoose=eThis.getAttribute("choose");
 				if(sChoose==null || sChoose.equals(""))
 				{
 					iChoose=aeChildren.length;
 				}
 				else
 				{
 					iChoose=Integer.parseInt(sChoose);
 					if(iChoose > aeChildren.length)
 						throw new OmFormatException("<random> Can't choose "+iChoose+" from "+aeChildren.length);
 				}
 			}
 			catch(NumberFormatException e)
 			{
 				throw new OmFormatException("<random> Invalid value for choose= (expected number): "+sChoose);
 			}
 
 			List<Element> lPicks=new LinkedList<Element>(Arrays.asList(aeChildren));
 			for(int i=0;i<iChoose;i++)
 			{
 				g.add(getTestItem(r,
 					lPicks.remove(r.nextInt(lPicks.size())),g));
 			}
 		}
 		else
 			throw new OmFormatException("Unexpected tag in <questions>: '"+sParentTag+"'");
 		return g;
 	}
 	
 	/**
 	 * Get all question ids from test file.
 	 * @return List of question ids
 	 * @throws OmFormatException
 	 */
 	public List<String> getAllQuestionIds() throws OmFormatException {		
 		Element[] eQuestions = XML.getElementArray(dTest.getElementsByTagName("question"));
 		List<String> ids = new ArrayList<String>(eQuestions.length);
 		for (Element eQuestion : eQuestions) {
 			try {
 				ids.add(XML.getRequiredAttribute(eQuestion,"id"));
 			} catch (XMLException e) {
 				throw new OmFormatException("Id attribute missing in <question> tag");
 			}
 		}
 		return ids;
 	}
 
 	boolean isNavigationAllowed()
 	{
 		return bNavigation;
 	}
 
 	boolean isRedoQuestionAllowed()
 	{
 		return bRedoQuestion;
 	}
 
 	boolean isAutomaticRedoQuestionAllowed()
 	{
 		return bRedoQuestionAuto;
 	}
 
 	boolean isRedoTestAllowed()
 	{
 		return bRedoTest;
 	}
 
 	boolean isSummaryAllowed()
 	{
 		return bFreeSummary;
 	}
 
 	boolean areQuestionsNamed()
 	{
 		return bQuestionNames;
 	}
 
 	boolean doesSummaryIncludeScores()
 	{
 		return bSummaryScores;
 	}
 
 	boolean doesSummaryIncludeAttempts()
 	{
 		return bSummaryAttempts;
 	}
 
 	boolean doesSummaryIncludeQuestions()
 	{
 		return bSummaryQuestions;
 	}
 
 	boolean isStopAllowed()
 	{
 		return bFreeStop;
 	}
 
 	boolean isSummaryIncludedAtEndCheck()
 	{
 		return bEndSummary;
 	}

 	int getNavLocation()
 	{
 		return iNavLocation;
 	}
 
 	Element getFinalPage()
 	{
 		return eFinal;
 	}
 
 	Element getConfirmParagraphs()
 	{
 		return eConfirmParagraphs;
 	}
 	String getConfirmButtonLabel()
 	{
 		return sConfirmButton;
 	}
 	String getConfirmTitle()
 	{
 		return sConfirmTitle;
 	}
 
 	String getLabelSet()
 	{
 		return sLabelSet;
 	}
 }
