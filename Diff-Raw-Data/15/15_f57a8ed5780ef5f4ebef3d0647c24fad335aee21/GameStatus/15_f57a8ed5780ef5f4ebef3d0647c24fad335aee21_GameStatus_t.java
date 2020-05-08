 package com.hack.regionunlocked;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.net.URL;
 
 public class GameStatus {
 
 	private String upcCode;
 	private String name;
 	private String checkName;
 	private List<RegionSupportStatusSet> support;
 	private String scandItKey = "key=-rdsomoapvSlt5JjXpPNr0WfBpw-H7f5R9JJMnIbw5J";
 	private boolean found = false;
	private boolean failed = false;
 
 	public GameStatus(String upcCode) {
 		this.upcCode = upcCode;
 		this.name = getUPCDatabaseName(upcCode);
 		this.checkName = getScandItName(upcCode);
 		found = checkNames();
 
		if (found == true)
 			checkStatusWikia();
	}

	public boolean hasFailed() {
		return this.failed;
 	}
 
 	private String getScandItName(String upcCode) {
		String url = "https://api.scandit.com/v2/products/" + upcCode + "?"
 				+ scandItKey;
 
 		String content = getWebsiteContent(url);
 
 		if (content.contains("name")) {
 
 			String strip = content.substring(18);
 			int check = strip.indexOf("\"");
 			strip = strip.substring(0, check);
 			check = strip.indexOf("0");
 			strip = strip.substring(0, check - 9);
 			System.out.println(strip);
 			checkName = strip;
 			return strip;
 		} else {
 			return "";
 		}
 	}
 
 	private String getUPCDatabaseName(String upcCode) {
 		// 885370201215 = Gears of War 3
 		String content = getWebsiteContent("http://www.upcdatabase.com/item/"
 				+ upcCode);
 
 		String regex = "<td>Description</td><td></td><td>(.*?)</td>";
 
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(content);
 
 		try {
 
 			matcher.find();
 			return matcher.group(1);
 
 		} catch (Exception ex) {
 			return "";
 		}
 
 	}
 
 	private boolean checkNames() {
 		if (name.contains(checkName))
 			return true;
 		else
 			return false;
 	}
 
 	private void checkStatusWikia() {
 
 		if (!this.name.equals("")) {
 			String content = getWebsiteContent("http://gaming.wikia.com/wiki/Region_Free_Xbox_360_Games");
 
 			String regex = "(?i)<td>[\\s]*<a href=\"[^\"]*\"[^>]*>"
 					+ this.name
 					+ "</a>[\\s]*</td>[\\s]*"
 					+ "<td>[\\s]*([\\w]+)[\\s]*</td>[\\s]*"
 					+ // version
 					"<td bgcolor=\"#[A-F0-9]*\">[\\s]*([\\w?]+)[\\s]*</td>[\\s]*"
 					+ // NTSC/J compatibility
 					"<td bgcolor=\"#[A-F0-9]*\">[\\s]*([\\w?]+)[\\s]*</td>[\\s]*"
 					+ // NTSC/U compatibility
 					"<td bgcolor=\"#[A-F0-9]*\">[\\s]*([\\w?]+)[\\s]*</td>"; // PAL
 																				// compatibility
 
 			Pattern pattern = Pattern.compile(regex);
 			Matcher matcher = pattern.matcher(content);
 
 			while (matcher.find()) {
 
 				GameRegion region = GameRegion.UNKNOWN;
 				if (matcher.group(1).equals("NTSC/J"))
 					region = GameRegion.NTSC_J;
 				if (matcher.group(1).equals("NTSC/U")
 						|| matcher.group(1).equals("US"))
 					region = GameRegion.NTSC_U;
 				if (matcher.group(1).equals("PAL"))
 					region = GameRegion.PAL;
 				if (region != GameRegion.UNKNOWN) {
 					RegionSupportStatusSet set = new RegionSupportStatusSet(
 							region);
 
 					if (matcher.group(2).equals("Yes"))
 						set.supportStatuses.put(GameRegion.NTSC_J,
 								RegionSupportStatus.Yes);
 					if (matcher.group(2).equals("No"))
 						set.supportStatuses.put(GameRegion.NTSC_J,
 								RegionSupportStatus.No);
 					if (matcher.group(2).equals("?"))
 						set.supportStatuses.put(GameRegion.NTSC_J,
 								RegionSupportStatus.Unknown);
 
 					if (matcher.group(3).equals("Yes"))
 						set.supportStatuses.put(GameRegion.NTSC_U,
 								RegionSupportStatus.Yes);
 					if (matcher.group(3).equals("No"))
 						set.supportStatuses.put(GameRegion.NTSC_U,
 								RegionSupportStatus.No);
 					if (matcher.group(3).equals("?"))
 						set.supportStatuses.put(GameRegion.NTSC_U,
 								RegionSupportStatus.Unknown);
 
 					if (matcher.group(4).equals("Yes"))
 						set.supportStatuses.put(GameRegion.PAL,
 								RegionSupportStatus.Yes);
 					if (matcher.group(4).equals("No"))
 						set.supportStatuses.put(GameRegion.PAL,
 								RegionSupportStatus.No);
 					if (matcher.group(4).equals("?"))
 						set.supportStatuses.put(GameRegion.PAL,
 								RegionSupportStatus.Unknown);
 
 					support.add(set);
 				}
 			}
 		}
 	}
 
 	private String getWebsiteContent(String urlString) {
 		try {
 			URL url = new URL(urlString);
 			InputStream inStream = url.openStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					inStream));
 			String content = "";
 			String line;
 			while ((line = br.readLine()) != null) {
 				content += line;
 			}
 			return content;
 		} catch (Exception ex) {
 			return "";
 		}
 	}
 	
 	public String getSupportAsText() {
 		if ((support == null) || (support.size() == 0)) {
 			return "No Results";
 		} else {
 			String result = "";
 			for (int i = 0; i < support.size(); i++) {
 				result += "Version: " + GameRegionToString(support.gameRegion) + "\n";
 				result += "\tNTSC/J: " + RegionSupportStatusToString(support.get(GameRegion.NTSC_J)) + "\n";
 				result += "\tNTSC/U: " + RegionSupportStatusToString(support.get(GameRegion.NTSC_U)) + "\n";
 				result += "\tPAL:    " + RegionSupportStatusToString(support.get(GameRegion.PAL)) + "\n";
 				result += "\n";
 			}
 			result += "\n";
 			return result;
 		}
 	}
 	
 	private String GameRegionToString(GameRegion region) {
 		switch (region) {
 			case GameRegion.NTSC_J:
 				return "NTSC/J";
 			case GameRegion.NTSC_U:
 				return "NTSC/U";
 			case GameRegion.PAL:
 				return "PAL";
 			default:
 				return "Unknown";
 		}
 	}
 	private String RegionSupportStatusToString(RegionSupportStatus status) {
 		switch (status) {
 			case RegionSupportStatus.Yes:
 				return "Yes";
 			case RegionSupportStatus.No:
 				return "No";
 			default:
 				return "Unknown";
 		}
 	}
 	
 }
