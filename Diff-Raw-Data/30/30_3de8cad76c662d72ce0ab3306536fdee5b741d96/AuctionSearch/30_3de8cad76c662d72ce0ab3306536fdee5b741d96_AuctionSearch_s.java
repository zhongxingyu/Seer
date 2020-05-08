 package edu.ucla.cs.cs144;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.w3c.dom.Element;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.text.SimpleDateFormat;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import edu.ucla.cs.cs144.DbManager;
 import edu.ucla.cs.cs144.SearchConstraint;
 import edu.ucla.cs.cs144.SearchResult;
 
 public class AuctionSearch implements IAuctionSearch {
 
 	/* 
          * You will probably have to use JDBC to access MySQL data
          * Lucene IndexSearcher class to lookup Lucene index.
          * Read the corresponding tutorial to learn about how to use these.
          *
          * Your code will need to reference the directory which contains your
 	 * Lucene index files.  Make sure to read the environment variable 
          * $LUCENE_INDEX with System.getenv() to build the appropriate path.
 	 *
 	 * You may create helper functions or classes to simplify writing these
 	 * methods. Make sure that your helper functions are not public,
          * so that they are not exposed to outside of this class.
          *
          * Any new classes that you create should be part of
          * edu.ucla.cs.cs144 package and their source files should be
          * placed at src/edu/ucla/cs/cs144.
          *
          */
 	
 	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
 			int numResultsToReturn) {
 		SearchResult[] results = new SearchResult[0];
 		
 		try {
 			//String[] fields = {"name", "description", "category"};
 			//MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
 			QueryParser parser = new QueryParser("all", new StandardAnalyzer());
 			Query luceneQuery = parser.parse(query);
 		    results = getSearchResultsForQuery(luceneQuery, numResultsToSkip, numResultsToReturn);
 		} catch (IOException | ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return results;
 	}
 
 	public SearchResult[] advancedSearch(SearchConstraint[] constraints, 
 			int numResultsToSkip, int numResultsToReturn) {
 		SearchResult[] results = new SearchResult[0];
 		
 		// Get itemIDs from SQL search first
 		Connection connection = null;
 		try {
 			connection = DbManager.getConnection(true);
 
 			// Go through constraints to build Lucene and SQL queries
 			String query = "";
 			
 			// TODO: assumes only one bidder constraint if any
 			String bidderID = null;
 			BigDecimal buyNowPrice = null;
 			Date endTime = null;
 			String sellerID = null;
 			for (int constraintIndex = 0, constraintCount = constraints.length; constraintIndex < constraintCount; constraintIndex++)
 			{
 				SearchConstraint constraint = constraints[constraintIndex];
 				String field = constraint.getFieldName();
 				if (field.equals(FieldName.BidderId))
 					bidderID = constraint.getValue();
 				else if (field.equals(FieldName.BuyPrice))
 					buyNowPrice = new BigDecimal(constraint.getValue());
 				else if (field.equals(FieldName.EndTime))
 					endTime = new SimpleDateFormat("MMM-dd-yy HH:mm:ss", Locale.ENGLISH).parse(constraint.getValue());
 				else if (field.equals(FieldName.SellerId))
 					sellerID = constraint.getValue();
 				else
 				{
 					String luceneField = "";
 					if (field.equals(FieldName.ItemName))
 						luceneField = "name";
 					else if (field.equals(FieldName.Description))
 						luceneField = "description";
 					else if (field.equals(FieldName.Category))
 						luceneField = "category";
 					
 					query = query + " +" + luceneField + ":(" + constraint.getValue() + ")";
 				}
 			}
 			
 			String itemSelect = "SELECT item_id FROM Item";
 			boolean whereClause = false;
 			if (buyNowPrice != null)
 			{
 				itemSelect = itemSelect + " WHERE buy_now_price = ?";
 				whereClause = true;
 			}
 			if (endTime != null)
 			{
 				if (whereClause)
 					itemSelect = itemSelect + " AND time_end = ?";
 				else
 				{
 					itemSelect = itemSelect + " WHERE time_end = ?";
 					whereClause = true;
 				}
 			}
 			if (sellerID != null)
 			{
 				if (whereClause)
 					itemSelect = itemSelect + " AND seller_id = ?";
 				else
 				{
 					itemSelect = itemSelect + " WHERE seller_id = ?";
 					whereClause = true;
 				}
 			}
 			
 			String SQLQuery;
 			if (bidderID != null)
 			{
 				if (whereClause)
 					SQLQuery = "SELECT item_id FROM (" + itemSelect + ") AS FilteredItem JOIN Bid ON FilteredItem.item_id = Bid.item_id WHERE bidder_id = ?";
 				else
 					SQLQuery = "SELECT item_id FROM Item AS FilteredItem JOIN Bid ON FilteredItem.item_id = Bid.item_id WHERE bidder_id = ?";
 			}
 			else
 			{
 				if (whereClause)
 					SQLQuery = itemSelect;
 				else
 					SQLQuery = null;
 			}
 			
 			if (SQLQuery != null)
 			{
 				PreparedStatement statement = connection.prepareStatement(SQLQuery);
 				
 				// Set statement parameters
 				int parameterIndex = 1;
 				if (buyNowPrice != null)
 				{
 					statement.setBigDecimal(parameterIndex, buyNowPrice);
 					parameterIndex++;
 				}
 				if (endTime != null)
 				{
					statement.setTimestamp(parameterIndex, (Timestamp) endTime);
 					parameterIndex++;
 				}
 				if (sellerID != null)
 				{
 					statement.setString(parameterIndex, sellerID);
 					parameterIndex++;
 				}
 				if (bidderID != null)
 				{
 					statement.setString(parameterIndex, bidderID);
 				}
 				
 				ResultSet itemsRS = statement.executeQuery();
 				
 				// Make one long string of valid itemIDs
 				String itemIDs = "";
 				while (itemsRS.next())
 				{
 					String itemID = Long.toString(itemsRS.getLong("item_id"));
 					itemIDs = itemIDs + " " + itemID;
 				}
 				
 				if (!itemIDs.equals(""))
 					query = "+id:(" + itemIDs + ")" + query;
 			}
 			
 			QueryParser parser = new QueryParser("description", new StandardAnalyzer());
 			Query luceneQuery = parser.parse(query);
 			
 			results = getSearchResultsForQuery(luceneQuery, numResultsToSkip, numResultsToReturn);
 			
 			connection.close();
 		} catch (SQLException | java.text.ParseException | ParseException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return results;
 	}
 
 	public String getXMLDataForItemId(String itemId) {
 		String XML = "";
 		
 		try {
 			Connection connection = DbManager.getConnection(true);
 			PreparedStatement itemStatement = connection.prepareStatement("SELECT * FROM Item JOIN EbayUser ON Item.seller_id = EbayUser.user_id WHERE item_id = ?");
 			itemStatement.setLong(1, Long.parseLong(itemId));
 			ResultSet itemsRS = itemStatement.executeQuery();
 			itemsRS.next();
 			
         	long itemID = itemsRS.getLong("item_id");
         	String itemName = itemsRS.getString("name");
         	String itemDescription = itemsRS.getString("description");
         	BigDecimal buyNowPrice = itemsRS.getBigDecimal("buy_now_price");
         	BigDecimal minFirstBid = itemsRS.getBigDecimal("minimum_start_bid");
         	Date startTime = itemsRS.getTimestamp("time_start");
         	Date endTime = itemsRS.getTimestamp("time_end");
         	String sellerID = itemsRS.getString("seller_id");
         	String sellerCountry = itemsRS.getString("country");
         	String sellerLocation = itemsRS.getString("location");
         	int sellerRating = itemsRS.getInt("rating");
         	
         	EbayUser seller = new EbayUser(sellerID, sellerRating, sellerCountry, sellerLocation);
         	
         	// Get all bids for item
         	PreparedStatement bidsStatement = connection.prepareStatement("SELECT * FROM Bid JOIN EbayUser ON Bid.bidder_id = EbayUser.user_id WHERE item_id = ? ORDER BY time ASC");
         	bidsStatement.setLong(1, itemID);
         	ResultSet bidsRS = bidsStatement.executeQuery();
         	List<Bid> bids = new ArrayList<Bid>();
         	while (bidsRS.next())
         	{
         		long bidID = bidsRS.getLong("bid_id");
         		BigDecimal amount = bidsRS.getBigDecimal("amount");
         		Date bidTime = bidsRS.getTimestamp("time");
         		String bidderID = bidsRS.getString("bidder_id");
         		String bidderCountry = bidsRS.getString("country");
         		String bidderLocation = bidsRS.getString("location");
         		int bidderRating = bidsRS.getInt("rating");
         		
         		EbayUser bidder = new EbayUser(bidderID, bidderRating, bidderCountry, bidderLocation);
         		
         		Bid bid = new Bid(bidID, itemID, bidder, amount, bidTime);
         		bids.add(bid);
         	}
         	Bid[] bidArray = new Bid[bids.size()];
         	bidArray = bids.toArray(bidArray);        		
         	
         	// Get all categories for this item
         	// TODO:preparedStatement
         	PreparedStatement categoriesStatement = connection.prepareStatement("SELECT name FROM ItemCategory JOIN Category ON ItemCategory.category_id = Category.category_id WHERE item_id = ?");
         	categoriesStatement.setLong(1, itemID);
         	ResultSet categoriesRS = categoriesStatement.executeQuery();
         	Set<String> itemCategories = new HashSet<String>();
         	while (categoriesRS.next())
         	{
         		String category = categoriesRS.getString("name");
         		itemCategories.add(category);
         	}
         	String[] categoryArray = new String[itemCategories.size()];
         	categoryArray = itemCategories.toArray(categoryArray);
         	
         	Item item = new Item(itemID, itemName, seller, itemDescription, minFirstBid, buyNowPrice, startTime, endTime, bidArray, categoryArray);
         	
         	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     		DocumentBuilder builder = factory.newDocumentBuilder();
     		org.w3c.dom.Document document = builder.newDocument();
      
     		// Root (Item)
     		Element itemElement = document.createElement("Item");
     		itemElement.setAttribute("ItemID", Long.toString(item.id));
     		document.appendChild(itemElement);
     		
     		// Name
     		Element nameElement = document.createElement("Name");
     		nameElement.appendChild(document.createTextNode(item.name));
 			itemElement.appendChild(nameElement);
     		
     		// Categories
     		for (int categoryIndex = 0, categoryCount = item.categories.length; categoryIndex < categoryCount; categoryIndex++)
     		{
     			Element categoryElement = document.createElement("Category");
     			categoryElement.appendChild(document.createTextNode(item.categories[categoryIndex]));
     			itemElement.appendChild(categoryElement);
     		}
     		
     		// Current price
     		BigDecimal currentPrice;
     		if (item.bids.length > 0)
     			currentPrice = item.bids[item.bids.length-1].amount;
     		else
     			currentPrice = item.minimumFirstBid;
     		Element currentlyElement = document.createElement("Currently");
     		currentlyElement.appendChild(document.createTextNode(currentPrice.toPlainString()));
     		itemElement.appendChild(currentlyElement);
     		
     		// Buy price
     		if (item.buyNowPrice != null && buyNowPrice.compareTo(BigDecimal.ZERO) != 0)
     		{
     			Element buyPriceElement = document.createElement("Buy_Price");
     			buyPriceElement.appendChild(document.createTextNode(item.buyNowPrice.toPlainString()));
     			itemElement.appendChild(buyPriceElement);
     		}
     		
     		// Min first bid
     		Element minFirstBidElement = document.createElement("First_Bid");
     		minFirstBidElement.appendChild(document.createTextNode(item.minimumFirstBid.toPlainString()));
     		itemElement.appendChild(minFirstBidElement);
     		
     		// Number of bids
     		Element numBidsElement = document.createElement("Number_of_Bids");
     		numBidsElement.appendChild(document.createTextNode(Integer.toString(item.bids.length)));
     		itemElement.appendChild(numBidsElement);
     		
     		// Bids
     		Element bidsElement = document.createElement("Bids");
     		itemElement.appendChild(bidsElement);
     		for (int bidIndex = 0, bidCount = item.bids.length; bidIndex < bidCount; bidIndex++)
     		{
     			Bid bid = item.bids[bidIndex];
     			Element bidElement = document.createElement("Bid");
     			bidsElement.appendChild(bidElement);
     			
     			// Bidder
     			Element bidderElement = document.createElement("Bidder");
     			bidderElement.setAttribute("UserID", bid.bidder.id);
     			bidderElement.setAttribute("Rating", Integer.toString(bid.bidder.rating));
     			bidElement.appendChild(bidderElement);
     			
     			// Location
     			Element bidderLocationElement = document.createElement("Location");
     			bidderLocationElement.appendChild(document.createTextNode(bid.bidder.location));
     			bidderElement.appendChild(bidderLocationElement);
     			
     			// Country
     			Element bidderCountryElement = document.createElement("Country");
     			bidderCountryElement.appendChild(document.createTextNode(bid.bidder.country));
     			bidderElement.appendChild(bidderCountryElement);
     			
     			// Time
     			Element bidTimeElement = document.createElement("Time");
     			String time = new SimpleDateFormat("MMM-dd-yy HH:mm:ss").format(bid.time);
     			bidTimeElement.appendChild(document.createTextNode(time));
     			bidElement.appendChild(bidTimeElement);
     			
     			// Amount
     			Element bidAmountElement = document.createElement("Amount");
     			bidAmountElement.appendChild(document.createTextNode(bid.amount.toPlainString()));
     			bidElement.appendChild(bidAmountElement);
     		}
     		
     		// Location
     		Element itemLocationElement = document.createElement("Location");
     		itemLocationElement.appendChild(document.createTextNode(item.seller.location));
 			itemElement.appendChild(itemLocationElement);
 			
     		// Country
 			Element itemCountryElement = document.createElement("Country");
 			itemCountryElement.appendChild(document.createTextNode(item.seller.country));
 			itemElement.appendChild(itemCountryElement);
     		
     		// Started
 			Element startedElement = document.createElement("Started");
 			String startedTime = new SimpleDateFormat("MMM-dd-yy HH:mm:ss").format(item.startTime);
 			startedElement.appendChild(document.createTextNode(startedTime));
 			itemElement.appendChild(startedElement);
     		
     		// Ends
 			Element endsElement = document.createElement("Ends");
 			String endsTime = new SimpleDateFormat("MMM-dd-yy HH:mm:ss").format(item.endTime);
 			endsElement.appendChild(document.createTextNode(endsTime));
 			itemElement.appendChild(endsElement);
     		
     		// Seller
 			Element sellerElement = document.createElement("Seller");
 			sellerElement.setAttribute("UserID", item.seller.id);
 			sellerElement.setAttribute("Rating", Integer.toString(item.seller.rating));
 			itemElement.appendChild(sellerElement);
     		
     		// Description
     		Element itemDescriptionElement = document.createElement("Description");
     		itemDescriptionElement.appendChild(document.createTextNode(item.description));
 			itemElement.appendChild(itemDescriptionElement);
 			
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(document);
 			StringWriter writer = new StringWriter();
 			StreamResult result = new StreamResult(writer);
 			
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			transformer.transform(source, result);
 			
 			XML = writer.toString();
     		
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return XML;
 	}
 	
 	public String echo(String message) {
 		return message;
 	}
 	
 	private SearchResult[] getSearchResultsForQuery(Query luceneQuery, int numResultsToSkip, int numResultsToReturn) throws CorruptIndexException, IOException
 	{
 		SearchResult[] results = new SearchResult[0];
 		
 		String indexDirectory = System.getenv("LUCENE_INDEX");
 		IndexSearcher searcher = new IndexSearcher(indexDirectory);
 		Hits hits = searcher.search(luceneQuery);
 		
 		// Translate Hits into SearchResults
 		int oneAfterFinalHitIndex;
 		if (numResultsToReturn == 0)
 			oneAfterFinalHitIndex = hits.length();
 		else
 			oneAfterFinalHitIndex = numResultsToSkip + numResultsToReturn;
 		oneAfterFinalHitIndex = oneAfterFinalHitIndex <= hits.length() ? oneAfterFinalHitIndex : hits.length(); 
 		int numHits = oneAfterFinalHitIndex - numResultsToSkip;
 		if (numHits > 0)
 		{
 			results = new SearchResult[numHits];
 			for (int hitIndex = numResultsToSkip, searchResultIndex = 0; hitIndex < oneAfterFinalHitIndex; hitIndex++, searchResultIndex++)
 			{
 				Document document = hits.doc(hitIndex);
 				String itemID = document.get("id");
 				String name = document.get("name");
 			    	
 				SearchResult result = new SearchResult(itemID, name);
 				results[searchResultIndex] = result;
 			}
 		}
 		
 		return results;
 	}
 
 }
