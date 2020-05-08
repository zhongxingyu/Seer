 package com.pellcorp.android.isohunt;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.select.Elements;
 
 public class SearchResultParser {
 	private static final Pattern PAGE_DETAILS_PATTERN = 
 			Pattern.compile(".*Page ([0-9]+) of ([0-9]+).*", Pattern.MULTILINE | Pattern.DOTALL);
 	
 	public static final int CATEGORY_IDX = 0;
 	public static final int AGE_IDX = 1;
 	public static final int TITLE_IDX = 2;
 	public static final int SIZE_IDX = 3;
 	public static final int SEEDERS_IDX = 4;
 	public static final int LEECHERS_IDX = 5;
 	
 	public PageResults parseResults(Document doc) {
 		PageResults results = new PageResults();
 		updatePageDetails(results, doc);
 		
 		Elements table = doc.select("table[id=serps]").select("tr[class=hlRow]");
 		List<Element> rowList = table.subList(1, table.size());
 		for (Element row : rowList) {
 			Elements rowCells = row.select("td");
 			
 			Result result = new Result();
 			result.setCategory(parseCategory(rowCells.get(CATEGORY_IDX).text()));
 			result.setAge(new Age(rowCells.get(AGE_IDX).text()));
 			result.setSize(new Size(rowCells.get(SIZE_IDX).text()));
 			result.setSeeders(parseInt(rowCells.get(SEEDERS_IDX).text()));
 			result.setLeeches(parseInt(rowCells.get(LEECHERS_IDX).text()));
 			
 			Element titleCell = rowCells.get(TITLE_IDX);
 			result.setRating(parseRating(titleCell));
			result.setId(parseId(titleCell.select("a").last().attr("href")));
 			result.setTitle(titleCell.select("a").last().text());
 			
 			results.addResult(result);
 		}
 		return results;
 	}
 	
 	
 	
 	private Integer parseInt(String value) {
 		try {
 			return Integer.parseInt(value);
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 	}
 	
 	private Category parseCategory(String category) {
 		for (Category categoryEnum : Category.values()) {
 			if (category.length() >= categoryEnum.name().length()) {
 				return valueOf(category.substring(0, categoryEnum.name().length()));
 			}
 		}
 		return Category.UNKNOWN;
 	}
 	
 	private Category valueOf(String category) {
 		try {
 			return Category.valueOf(category);
 		} catch (Exception e) { 
 			return null;
 		}
 	}
 	
 	// /torrent_details/199226013/doctor+who+season+5?tab=summary
 	private String parseId(String href) {
 		int index = href.indexOf("/torrent_details/");
 		if (index != -1) {
 			href = href.substring(index + "/torrent_details/".length());
 			int lastIndex = href.indexOf("/");
 			return href.substring(0, lastIndex);
 		}
 		return null;
 	}
 	
 	private Rating parseRating(Element titleCell) {
 		List<TextNode> textNodes = titleCell.select("a").first().textNodes();
 		String rating = textNodes.get(0).text(); // rating text
 		String comments = textNodes.get(1).text();
 		return new Rating(rating, parseInt(comments.trim()));
 	}
 	
 	private void updatePageDetails(PageResults results, Document doc) {
 		Matcher m = PAGE_DETAILS_PATTERN.matcher(doc.html());
 		if (m.matches()) {
 			results.setCurrentPage(parseInt(m.group(1)));
 			results.setNumberOfPages(parseInt(m.group(2)));
 		}
 	}
 }
