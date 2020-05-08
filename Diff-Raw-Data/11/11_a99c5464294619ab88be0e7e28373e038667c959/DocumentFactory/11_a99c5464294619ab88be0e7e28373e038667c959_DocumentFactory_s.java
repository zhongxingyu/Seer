 package cpsc310.client;
 
 import com.google.gwt.user.client.ui.HTML;
 
 public class DocumentFactory {
 
 	/**
 	 * Constructor for creating the help and terms of use documents
 	 */
 	public DocumentFactory() {
 		//empty constructor
 	}
 	
 	/**
 	 * Method to create the Help document for the application. 
 	 * @pre true;
 	 * @post true;
 	 * @return an HTML object containing the contents for the Help document.
 	 */
 	public HTML createHelpDocument() {
 		HTML helpDoc = new HTML(
 			"<span>" +
 			"1) How to register as a Realtor?<br><br>" +
 			"If you already have a Google Account, then all you have to do is sign in with your Google Account and you will have Realtor privileges.<br><br>" +
 			"2) How do I search for houses?<br><br>" +
 			"Type in the desired type of house (e.g. house price range, location, etc.) in our search panel and click the search button.  All houses matching your input will be displayed in the table.  If you are looking for a more detailed information search, click on the advanced search option for more search options.<br><br>" +
 			"3) How can I see more results per page of the table?<br><br>" +
 			"Click on Expand Table at the top right of the table and you will be able to view at most 50 houses per table page.<br><br>" +
 			"4) How do I view houses?<br><br>" +
 			"From the houses displayed in the table, click on the house that you're interested in and the Google Map and Street View will update, displaying the house you clicked.<br><br>" +
 			"5) How do I sell a house?<br><br>" +
 			"After successfully logging into our application, select the house you want to sell, and press the \"Edit\" button.  In the popup window, change the radio button from \"No\" to \"Yes\", edit the price, and then press \"OK\".  The house you selected will automatically update and display that it is for sale.<br><br>" +
 			"6) How do I remove sale information from a house?<br><br>" +
 			"After successfully logging into our application, select the house you want to remove the sale information from.  Click the \"Remove\" button and all information regarding the selling of that house will be removed.<br><br>" +
 			"7) What's the best way to share this application with people I know?<br><br>" +
 			"If you and your friends have Facebook accounts, you're in luck, each house displayed on our Google Map contains a Facebook Share button which can post the link to a given house on various locations including your wall and your friend's wall.<br><br>" +
 			"8) Is it possible to sort my search results?<br><br>" +
 			"It certainly is, in fact, we have implemented our sort so that you can sort the entire database by the value of any of the columns of our table.  All you have to do is click the value you want to sort by on the table, and you can sort in ascending or descending order.<br><br>" +
 			"9) Can I search for houses only within a particular region of the Google Map?<br><br>" +
 			"Yes.  Click on the Draw button (button with pencil icon), select the region on the map that you're interested in by clicking on the Google Map and you will see a red box appear.  You can adjust the shape and size of the box via the editable vertices.  When you're done editing the search region, click on the Search button and all houses that are for sale within the region you drew will be displayed in the table.  If you don't like the region you drew or don't require it anymore, just click on the Erase button (button with eraser icon).<br><br>" +
 			"10) If I'm a realtor, how can I edit my contact information?<br><br>" +
 			"After logging in with your Google Account, click on the \"My Account\" tab.  Click on the \"Edit Account Information\" button, enter the new information, and press the \"OK\" button.  And now you're done!  Note: Name and E-mail are not editable.<br><br>" +
 			"11) If I'm a realtor, how can I view the houses I am selling?<br><br>" +
 			"After logging in with your Google Account, click on the \"My Account\" tab.  Click on the \"See My Houses\" button and all houses associated with you are displayed in the table.<br><br>" +
 			"12)  I don't like having so many panels open at a time, is there any way to collapse some of them?<br><br>" +
 			"Both the search panel and the table in our application can be collapsed.  A minimize button can be found at the top right of the search panel and another one can be found at the top left of the table.  In addition, the amount of space that the Google Map and Street View take can be adjusted by sliding the grey vertical bar that separates them.<br><br>" +
 			"13) Is it possible to reset my search input and/or search results?<br><br>" +
 			"Yes.  Press the \"Reset\" button located to the right of the \"Street Number\" input box and all values input for the search criteria will be reset (except for Sale Status).  For your search results in the table, simply click the \"Reset Table\" button and the table will reinitialize itself.<br><br>" +
 			"14) Are there any policies or rules that I should be aware of?<br><br>" +
 			"This type of important information can be found in the link to our Terms of Use document.  Just click the link next to the Help link and our Terms of Use information will be displayed." +
 			"</span>");
 		helpDoc.setWordWrap(true);
 		return helpDoc;
 	}
 	
 	/**
 	 * Method to create the Terms document for the application.
 	 * @pre true;
 	 * @post true;
 	 * @return an HTML object containing the contents for the Terms document.
 	 */
 	public HTML createTermsDoc() {
 		HTML termsDoc = new HTML(
 			"<span>" +
 				"Disclaimer: This application was solely produced for educational purposes.  We are not responsible for how the data is used.  In addition, there is no moderator for this application in any aspect, including the purchase of houses; in other words, we are not responsible for how users interact with the application as well as between each other.<br><br>" +
 				"By using the information in this application, you also agree to the terms in the following document online:<br><br>" +
 				"<center><a href=http://data.vancouver.ca/termsOfUse.htm>Data Vancouver Terms of Use</a></center><br><br>" +
 				"This document can be viewed at any time by clicking the \"Terms of Use\" link found on the application." +
 			"</span>");
 		termsDoc.setWordWrap(true);
 		return termsDoc;
 	}
}
