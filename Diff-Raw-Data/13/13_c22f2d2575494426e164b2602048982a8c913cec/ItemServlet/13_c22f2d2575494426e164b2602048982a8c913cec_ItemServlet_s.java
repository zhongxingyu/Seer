 package edu.ucla.cs.cs144;
 
 import java.io.IOException;
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 import org.w3c.dom.Attr;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 
 
 public class ItemServlet extends HttpServlet implements Servlet {
        
     public ItemServlet() {}
 
     /* Non-recursive (NR) version of Node.getElementsByTagName(...)
      */
     static Element[] getElementsByTagNameNR(Element e, String tagName) {
         Vector< Element > elements = new Vector< Element >();
         Node child = e.getFirstChild();
         while (child != null) {
             if (child instanceof Element && child.getNodeName().equals(tagName))
             {
                 elements.add( (Element)child );
             }
             child = child.getNextSibling();
         }
         Element[] result = new Element[elements.size()];
         elements.copyInto(result);
         return result;
     }
     
     /* Returns the first subelement of e matching the given tagName, or
      * null if one does not exist. NR means Non-Recursive.
      */
     static Element getElementByTagNameNR(Element e, String tagName) {
         Node child = e.getFirstChild();
         while (child != null) {
             if (child instanceof Element && child.getNodeName().equals(tagName))
                 return (Element) child;
             child = child.getNextSibling();
         }
         return null;
     }
     
     /* Returns the text associated with the given element (which must have
      * type #PCDATA) as child, or "" if it contains no text.
      */
     static String getElementText(Element e) {
         if (e.getChildNodes().getLength() == 1) {
             Text elementText = (Text) e.getFirstChild();
             return elementText.getNodeValue();
         }
         else
             return "";
     }
     
     /* Returns the text (#PCDATA) associated with the first subelement X
      * of e with the given tagName. If no such X exists or X contains no
      * text, "" is returned. NR means Non-Recursive.
      */
     static String getElementTextByTagNameNR(Element e, String tagName) {
         Element elem = getElementByTagNameNR(e, tagName);
         if (elem != null)
             return getElementText(elem);
         else
             return "";
     }
     
     static Attr getAttributeByTagNameNR(Element e, String tagName) {
         Attr att = e.getAttributeNode(tagName);
         if (att != null) {
              return att;
         }
         else {
             return null;
         }
     }
 
     static String getAttributeTextByTagNameNR(Element e, String tagName) {
         Attr att = getAttributeByTagNameNR(e, tagName);
         if (att != null) {
             return att.getValue();
         }
         else {
             return "";
         }
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
    	String pageTitle = "My Item Servlet";
 	    request.setAttribute("title", pageTitle);
 
     	String query = request.getQueryString();
   		if (query != null) 
   		{
         String itemID = request.getParameter("itemID");
         if(itemID == null) itemID = "";
 
   			request.setAttribute("itemID", itemID);
 
   	    AuctionSearchClient sc = new AuctionSearchClient();
   	    String result = sc.getXMLDataForItemId(itemID);
   	    // request.setAttribute("xml", result);
 
   			Document doc = null;
   			Element root = null;
   			DocumentBuilder builder = null;
   		   
 		    if (result.isEmpty())
 		    {
 		    	request.setAttribute("total", 0);
 		    }
 	    	else 
 	    	{
 	    		request.setAttribute("total", 1);
 	    		
 	    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	    		try {
         			builder = factory.newDocumentBuilder();
         	}
         	catch (ParserConfigurationException e)
         	{
         		e.printStackTrace();
     				System.exit(3);
         	}
 
        		InputSource is = new InputSource(new StringReader(result));
 
 				  try {
             doc = builder.parse(is);
   				}
   				catch (IOException e) {
   					e.printStackTrace();
   					System.exit(3);
   				}
   				catch (SAXException e) {
   					e.printStackTrace();
   					System.exit(3);
   				}
 
 					root = doc.getDocumentElement();
           request.setAttribute("name", getElementTextByTagNameNR(root,"Name"));
           request.setAttribute("Currently", getElementTextByTagNameNR(root,"Currently"));
           request.setAttribute("First_Bid", getElementTextByTagNameNR(root,"First_Bid"));
           request.setAttribute("Number_of_Bids", getElementTextByTagNameNR(root,"Number_of_Bids"));
           request.setAttribute("Location", getElementTextByTagNameNR(root,"Location"));
           request.setAttribute("Country", getElementTextByTagNameNR(root,"Country"));
           request.setAttribute("Ends", getElementTextByTagNameNR(root,"Ends"));
           request.setAttribute("Started", getElementTextByTagNameNR(root,"Started"));
           request.setAttribute("Description", getElementTextByTagNameNR(root,"Description"));
 
           Element sellerElement = getElementByTagNameNR(root,"Seller");
           request.setAttribute("SellerID", getAttributeTextByTagNameNR(sellerElement,"UserID"));
           request.setAttribute("SellerRating", getAttributeTextByTagNameNR(sellerElement,"Rating"));
 
           Element bidE = getElementByTagNameNR(root, "Bids");
           Element[] bidArr = getElementsByTagNameNR(bidE, "Bid");
           String bids = "";
           for (int i=0;i<bidArr.length;i++) {
             Element bidder = getElementByTagNameNR(bidArr[i], "Bidder");
             String bidderID = getAttributeTextByTagNameNR(bidder, "UserID");
             String bidderRating = getAttributeTextByTagNameNR(bidder, "Rating");
             String location = getElementTextByTagNameNR(bidder, "Location");
             String country = getElementTextByTagNameNR(bidder, "Country");
             String time = getElementTextByTagNameNR(bidArr[i],"Time");
             String amount = getElementTextByTagNameNR(bidArr[i],"Amount");
             bids += "<br>BidderID: " + bidderID + "<br>BidderRating: " + bidderRating + "<br>BidderLocation: " +  location + "<br>BidderCountry: " + country + "<br>Bid Time: " + time + "<br>Bid Amount: " + amount + "<br><br>";
           }
           request.setAttribute("Bids", bids);
 
           Element[] catArr = getElementsByTagNameNR(root, "Category");
           String categories = "";
           for (int i=0;i<catArr.length;i++) {
             categories += getElementText(catArr[i]);
             if (i!=0 && i!=catArr.length-1) {
               categories += ", ";
             }
           }
           request.setAttribute("Categories", categories);
         }
 		    request.getRequestDispatcher("/itemResult.jsp").forward(request, response);
 
       }
       else {
   	    request.getRequestDispatcher("/getItem.html").forward(request, response);
     	}
     }
 }
 
