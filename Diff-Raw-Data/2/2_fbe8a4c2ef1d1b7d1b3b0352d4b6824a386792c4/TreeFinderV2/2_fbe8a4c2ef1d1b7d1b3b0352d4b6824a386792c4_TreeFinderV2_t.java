 package edu.gac.arboretumweb.server;
 
 import edu.gac.arboretumweb.shared.domain.Tree;
 
 import com.google.gdata.client.spreadsheet.*;
 import com.google.gdata.data.Source;
 import com.google.gdata.data.spreadsheet.*;
 import com.google.gdata.util.*;
 
 import java.io.IOException;
 import java.net.*;
 import java.util.*;
 
 public class TreeFinderV2 {
 	
 	private URL treeURL;
 	
 	public TreeFinderV2(URL treeURL) {
 		this.treeURL = treeURL;
 	}
 	
 	public void getTrees(ArrayList<Tree> treeList) throws AuthenticationException, MalformedURLException, IOException, ServiceException {
		SpreadsheetService treeSpreadsheetService = new SpreadsheetService("Tree Spreadsheet");
 		
 		WorksheetFeed feed = treeSpreadsheetService.getFeed(treeURL,
 		        WorksheetFeed.class);
 		List<WorksheetEntry> worksheets = feed.getEntries();
 		
 		//TODO: no worksheets found
 		if (worksheets.size() == 0) {
 		      
 		}
 		    
 		WorksheetEntry treeWorksheet = new WorksheetEntry();
 		for (WorksheetEntry worksheet : worksheets) {
 			if ("Trees".equals(worksheet.getTitle().getPlainText())) {
 				treeWorksheet = worksheet;
 		    }
 		}
 		    
 		    // Fetch the list feed of the worksheet.
 		URL listFeedUrl = treeWorksheet.getListFeedUrl();
 		ListFeed listFeed = treeSpreadsheetService.getFeed(listFeedUrl, ListFeed.class);
 		    
 		for (ListEntry entry : listFeed.getEntries()) {
 			Tree tree = new Tree();
 			String commonName = entry.getTitle().getPlainText();
 			tree.setCommonName(commonName);
 			
 			for (String tag : entry.getCustomElements().getTags()) {
 				if ("scientificname".equals(tag)) {
 					String scientificName = entry.getCustomElements().getValue(tag);
 					tree.setScientificName(scientificName);
 				} else if ("yearplanted".equals(tag)) {
 					String yearPlanted = entry.getCustomElements().getValue(tag);
 					tree.setYearPlanted(yearPlanted);
 				} else if ("health".equals(tag)) {
 					String health = entry.getCustomElements().getValue(tag);
 					tree.setHealth(health);
 				} else if ("longitude".equals(tag)) {
 					String longitude = entry.getCustomElements().getValue(tag);
 					tree.setLongitude(longitude);
 				} else if ("latitude".equals(tag)) {
 					String latitude = entry.getCustomElements().getValue(tag);
 					tree.setLatitude(latitude);
 				} else if ("diameter".equals(tag)) {
 					String diameter = entry.getCustomElements().getValue(tag);
 					tree.setDiameter(diameter);
 				} else if ("yeardonated".equals(tag)) {
 					String yearDonated = entry.getCustomElements().getValue(tag);
 					tree.setYearDonated(yearDonated);
 				} else if ("donatedby".equals(tag)) {
 					String donatedBy = entry.getCustomElements().getValue(tag);
 					tree.setDonatedBy(donatedBy);
 				} else if ("donatedfor".equals(tag)) {
 					String donatedFor = entry.getCustomElements().getValue(tag);
 					tree.setDonatedFor(donatedFor);
 				}
 			}
 			treeList.add(tree);
 		}
 		    		
 	}
 
 		    
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
