 /*   Copyright 2012 Mario Bhmer
  *
  *   Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) 
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 package com.blogspot.marioboehmer.thingibrowse.network;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.blogspot.marioboehmer.thingibrowse.domain.Thing;
 import com.blogspot.marioboehmer.thingibrowse.domain.ThingResultListItem;
 
 /**
  * A HTML parser/processor which parses {@link Thing} and
  * {@link ThingResultListItem} specific data from the thingiverse platform.
  * 
  * @author Mario Bhmer
  */
 public class ThingiverseHTMLParser {
 
 	private static final String SEARCH_LAST_PAGE_INDEX_END_TAG = "?";
 	private static final String THING_TAG = "<div class=\"thing-float\">";
 	private static final String SEARCH_THING_TAG = "<td><a href=\"http://www.thingiverse.com/thing:";
 	private static final String IMAGE_URL_TAG_START = "<img src=\"";
 	private static final String URL_TAG_START = "<a href=\"";
 	private static final String URL_TAG_END = "\"";
 	private static final String THING_URL_TAG_START = "<div class=\"thing-name\"><a href=\"";
 	private static final String SEARCH_THING_URL_TAG_START = URL_TAG_START;
 	private static final String TEXT_TAG_START = "\">";
 	private static final String TEXT_TAG_END = "</a>";
 	private static final String PARAGRAPH_TAG_START = "<p>";
 	private static final String PARAGRAPH_TAG_END = "</p>";
 	private static final String THING_CREATOR_URL_TAG_START = "Created by <a href=\"";
 	private static final String THING_TIME_TAG_START = "<div class=\"thing-time\">";
 	private static final String SEARCH_THING_TIME_TAG_START = "Published: ";
 	private static final String SEARCH_THING_TIME_TAG_END = " on";
 	private static final String DIV_TAG_START = "<div>";
 	private static final String DIV_TAG_END = "</div>";
 	private static final String LAST_PAGE_INDEX_START_TAG_OFFSET = "\"Next\"";
 	private static final String THINGS_LAST_PAGE_INDEX_START_TAG = "/page:";
 	private static final String THING_DETAILS_TITLE_TAG_OFFSET = "<div id=\"thing-meta\">";
 	private static final String THING_DETAILS_TITLE_TAG_START = "<h1>";
 	private static final String THING_DETAILS_TITLE_TAG_END = "</h1>";
 	private static final String THING_DETAILS_CREATED_BY_TAG_OFFSET = "<div class=\"byline\">";
 	private static final String THING_DETAILS_CREATOR_IMAGE_URL_OFFSET = "<div id=\"thing-creator\">";
 	private static final String THING_DETAILS_CREATED_DATE_START = "Created on ";
 	private static final String THING_DETAILS_CREATED_DATE_END = "</";
 	private static final String THING_DETAILS_DESCRIPTION_TAG_START = "<div id=\"thing-description\">";
	private static final String THING_DETAILS_IMAGE_URL_TAG_OFFSET = "<div id=\"thing-gallery-main\"";
 	private static final String THING_DETAILS_ADDITIONAL_IMAGE_URL_TAG_OFFSET = "<div class=\"thing-image-thumb\">";
 	private static final String THING_DETAILS_ADDITIONAL_IMAGE_URLS_TAG_START = "<div id=\"thing-gallery-thumbs\">";
 	private static final String THING_DETAILS_ADDITIONAL_IMAGE_URLS_TAG_END = "<div id=\"thing-info\">";
 	private static final String THING_DETAILS_INSTRUCTIONS_TAG_OFFSET = "<h4>Instructions</h4>";
 	private static final String THING_DETAILS_FILES_TAG_OFFSET = "<div class=\"thing-status\">";
 	private static final String THING_DETAILS_FILES_TAG_SECOND_OFFSET = "<div class=\"thing-file\"";
 	private static final String THING_TITLE_TAG_START = "title=\"";
 	private static final String LARGE_IMAGE_TAG_OFFSET = "<div class=\"main-content\"";
 	private static final String MEDIUM_IMAGE_TAG_OFFSET = "<b>card</b>";
 	private static final String THINGIVERSE_BASE_URL = "http://www.thingiverse.com";
 
 	public static ArrayList<ThingResultListItem> getThingResultListItems(
 			String html) {
 		ArrayList<ThingResultListItem> resultListItems = new ArrayList<ThingResultListItem>();
 		List<Integer> indices = new ArrayList<Integer>();
 		int startIndex = 0;
 		while ((startIndex = html.indexOf(THING_TAG, startIndex)) > 0) {
 			startIndex = startIndex + THING_TAG.length();
 			indices.add(startIndex);
 		}
 		for (int x = 0; x < indices.size(); x++) {
 			String thingHtml;
 			if (x + 1 < indices.size()) {
 				thingHtml = html.substring(indices.get(x).intValue(),
 						indices.get(x + 1));
 			} else {
 				thingHtml = html.substring(indices.get(x));
 			}
 			resultListItems.add(getResultListItemFromHtmlSnippet(thingHtml));
 
 		}
 		return resultListItems;
 	}
 
 	public static ArrayList<ThingResultListItem> getThingResultListItemsForSearch(
 			String html) {
 		ArrayList<ThingResultListItem> resultListItems = new ArrayList<ThingResultListItem>();
 		List<Integer> indices = new ArrayList<Integer>();
 		int startIndex = 0;
 		while ((startIndex = html.indexOf(SEARCH_THING_TAG, startIndex)) > 0) {
 			startIndex = startIndex + SEARCH_THING_TAG.length();
 			indices.add(startIndex);
 		}
 		for (int x = 0; x < indices.size(); x++) {
 			String thingHtml;
 			if (x + 1 < indices.size()) {
 				thingHtml = html.substring(indices.get(x).intValue(),
 						indices.get(x + 1));
 			} else {
 				thingHtml = html.substring(indices.get(x));
 			}
 			resultListItems
 					.add(getResultListItemForSearchFromHtmlSnippet(thingHtml));
 
 		}
 		return resultListItems;
 	}
 
 	public static Thing getThing(String html) {
 		Map<String, String[]> thingFiles = new HashMap<String, String[]>();
 		List<String> thingAllImageUrls = new ArrayList<String>();
 
 		// ThingTitle
 		String thingTitle = getStringForTags(THING_DETAILS_TITLE_TAG_START,
 				THING_DETAILS_TITLE_TAG_END,
 				getStringForTags(THING_DETAILS_TITLE_TAG_OFFSET, null, html));
 
 		// ThingCreatedBy
 		String thingCreatedBy = getStringForTags(
 				TEXT_TAG_START,
 				TEXT_TAG_END,
 				getStringForTags(THING_DETAILS_CREATED_BY_TAG_OFFSET, null,
 						html));
 
 		// ThingCreatorImageUrl
 		String thingCreatorImageUrl = getStringForTags(
 				IMAGE_URL_TAG_START,
 				URL_TAG_END,
 				getStringForTags(THING_DETAILS_CREATOR_IMAGE_URL_OFFSET,
 						THING_DETAILS_CREATED_BY_TAG_OFFSET, html));
 
 		// ThingCreatorUrl
 		String thingCreatorUrl = adjustRelativeUrl(
 				getStringForTags(
 						URL_TAG_START,
 						URL_TAG_END,
 						getStringForTags(THING_DETAILS_CREATED_BY_TAG_OFFSET,
 								null, html)), THINGIVERSE_BASE_URL);
 
 		// ThingDate
 		String thingDate = getStringForTags(THING_DETAILS_CREATED_DATE_START,
 				THING_DETAILS_CREATED_DATE_END, html).replace("\t", "");
 
 		// ThingDescription
 		String thingDescription = getStringForTags(
 				THING_DETAILS_DESCRIPTION_TAG_START, DIV_TAG_END, html)
 				.replace("\t", "");
 
 		// ThingImageUrl
 		String thingImageUrl = getStringForTags(
 				IMAGE_URL_TAG_START,
 				URL_TAG_END,
 				getStringForTags(THING_DETAILS_IMAGE_URL_TAG_OFFSET, null, html));
 
 		// ThingLargeImageUrl
 		String thingLargeImageUrl = adjustRelativeUrl(
 				getStringForTags(
 						URL_TAG_START,
 						URL_TAG_END,
 						getStringForTags(THING_DETAILS_IMAGE_URL_TAG_OFFSET,
 								null, html)), THINGIVERSE_BASE_URL);
 
 		// ThingAllImageUrls
 		thingAllImageUrls.add(thingLargeImageUrl);
 		int thingImageUrlsStartIndex = 0;
 		String additionalImagesHtmlSnippet = getStringForTags(
 				THING_DETAILS_ADDITIONAL_IMAGE_URLS_TAG_START,
 				THING_DETAILS_ADDITIONAL_IMAGE_URLS_TAG_END, html);
 		while ((thingImageUrlsStartIndex = additionalImagesHtmlSnippet.indexOf(
 				THING_DETAILS_ADDITIONAL_IMAGE_URL_TAG_OFFSET,
 				thingImageUrlsStartIndex)) > -1) {
 			thingImageUrlsStartIndex += THING_DETAILS_ADDITIONAL_IMAGE_URL_TAG_OFFSET
 					.length();
 			String thingAdditionalImageSubstring = additionalImagesHtmlSnippet
 					.substring(thingImageUrlsStartIndex);
 			String thingAdditionalImage = adjustRelativeUrl(
 					getStringForTags(URL_TAG_START, URL_TAG_END,
 							thingAdditionalImageSubstring),
 					THINGIVERSE_BASE_URL);
 			thingAllImageUrls.add(thingAdditionalImage);
 		}
 
 		// ThingInstructions
 		String thingInstructions = getStringForTags(
 				PARAGRAPH_TAG_START,
 				PARAGRAPH_TAG_END,
 				getStringForTags(THING_DETAILS_INSTRUCTIONS_TAG_OFFSET, null,
 						html)).replace("\t", "");
 
 		// ThingFiles
 		int thingFileStartIndex = 0;
 		while ((thingFileStartIndex = html.indexOf(
 				THING_DETAILS_FILES_TAG_SECOND_OFFSET, thingFileStartIndex)) > -1) {
 			thingFileStartIndex += THING_DETAILS_FILES_TAG_OFFSET.length();
 			String thingFileSubstring = html.substring(thingFileStartIndex);
 			String fileUrl = adjustRelativeUrl(
 					getStringForTags(URL_TAG_START, URL_TAG_END,
 							thingFileSubstring), THINGIVERSE_BASE_URL);
 			String fileName = getStringForTags(THING_TITLE_TAG_START,
 					TEXT_TAG_START, thingFileSubstring);
 			String fileImageUrl = getStringForTags(IMAGE_URL_TAG_START,
 					URL_TAG_END, thingFileSubstring);
 			String fileSize = getStringForTags(DIV_TAG_START, DIV_TAG_END,
 					getStringForTags(TEXT_TAG_END, null, thingFileSubstring))
 					.replace("\n", "").replace("\t", "");
 			thingFiles.put(fileUrl, new String[] { fileName, fileSize,
 					fileImageUrl });
 		}
 
 		return new Thing(thingTitle, thingCreatedBy, thingCreatorImageUrl,
 				thingDate, thingDescription, thingCreatorUrl, thingImageUrl,
 				thingLargeImageUrl, thingInstructions, thingFiles,
 				thingAllImageUrls);
 	}
 
 	private static String getStringForTags(String startTag, String endTag,
 			String html) {
 		int startIndex = -1;
 		int endIndex = 0;
 		if ((startIndex = html.indexOf(startTag)) > -1) {
 			startIndex += startTag.length();
 			if (endTag != null) {
 				endIndex = html.indexOf(endTag, startIndex);
 				return html.substring(startIndex, endIndex).replace("[", "%5B");
 			} else {
 				return html.substring(startIndex).replace("[", "%5B");
 			}
 		}
 		return "";
 	}
 
 	public static int getThingsLastPageIndex(String html) {
 		return Integer
 				.parseInt(getStringForTags(
 						THINGS_LAST_PAGE_INDEX_START_TAG,
 						URL_TAG_END,
 						getStringForTags(LAST_PAGE_INDEX_START_TAG_OFFSET,
 								null, html)));
 	}
 
 	public static int getThingsLastPageIndexForSearch(String html) {
 		return Integer
 				.parseInt(getStringForTags(
 						THINGS_LAST_PAGE_INDEX_START_TAG,
 						SEARCH_LAST_PAGE_INDEX_END_TAG,
 						getStringForTags(LAST_PAGE_INDEX_START_TAG_OFFSET,
 								null, html)));
 	}
 
 	private static ThingResultListItem getResultListItemFromHtmlSnippet(
 			String htmlSnippet) {
 		// thingImageUrl
 		String thingImageUrl = getStringForTags(IMAGE_URL_TAG_START,
 				URL_TAG_END, htmlSnippet);
 
 		// thingUrl
 		String thingUrl = adjustRelativeUrl(
 				getStringForTags(THING_URL_TAG_START, URL_TAG_END, htmlSnippet),
 				THINGIVERSE_BASE_URL);
 
 		// thingTitle
 		String thingTitle = getStringForTags(THING_TITLE_TAG_START,
 				URL_TAG_END, htmlSnippet);
 
 		// thingCreatorUrl
 		String thingCreatorUrl = adjustRelativeUrl(
 				getStringForTags(THING_CREATOR_URL_TAG_START, URL_TAG_END,
 						htmlSnippet), THINGIVERSE_BASE_URL);
 
 		// thingCreatedBy
 		String thingCreatedBy = "by "
 				+ getStringForTags(
 						TEXT_TAG_START,
 						TEXT_TAG_END,
 						getStringForTags(THING_CREATOR_URL_TAG_START, null,
 								htmlSnippet));
 
 		// thingTime
 		String thingTime = getStringForTags(THING_TIME_TAG_START, DIV_TAG_END,
 				htmlSnippet).replace("\t", "").replace("\n", "");
 
 		return new ThingResultListItem(thingTitle, thingCreatedBy, thingTime,
 				thingUrl, thingCreatorUrl, thingImageUrl);
 	}
 
 	private static ThingResultListItem getResultListItemForSearchFromHtmlSnippet(
 			String htmlSnippet) {
 		// thingImageUrl
 		String thingImageUrl = getStringForTags(IMAGE_URL_TAG_START,
 				URL_TAG_END, htmlSnippet);
 
 		// thingUrl
 		String thingUrl = adjustRelativeUrl(
 				getStringForTags(SEARCH_THING_URL_TAG_START, URL_TAG_END,
 						htmlSnippet), THINGIVERSE_BASE_URL);
 
 		String thingTitleAndCreator = getStringForTags(thingUrl + "\">",
 				"</a>", htmlSnippet);
 
 		String titleCreatorDivider = " by ";
 		String[] titleCreatorArray = thingTitleAndCreator
 				.split(titleCreatorDivider);
 
 		// thingCreatedBy
 		String thingCreatedBy = "";
 
 		if (titleCreatorArray.length == 2) {
 			thingCreatedBy = "by " + titleCreatorArray[1];
 		}
 
 		// thingTitle
 		String thingTitle = titleCreatorArray[0];
 
 		// thingTime
 		String thingTime = getStringForTags(SEARCH_THING_TIME_TAG_START,
 				SEARCH_THING_TIME_TAG_END, htmlSnippet);
 
 		return new ThingResultListItem(thingTitle, thingCreatedBy, thingTime,
 				thingUrl, null, thingImageUrl);
 	}
 
 	public static String getLargeImageUrl(String html) {
 		return getStringForTags(IMAGE_URL_TAG_START, URL_TAG_END,
 				getStringForTags(LARGE_IMAGE_TAG_OFFSET, null, html));
 	}
 
 	public static String getMediumImageUrl(String html) {
 		return getStringForTags(IMAGE_URL_TAG_START, URL_TAG_END,
 				getStringForTags(MEDIUM_IMAGE_TAG_OFFSET, null, html));
 	}
 
 	private static String adjustRelativeUrl(String url, String baseUrl) {
 		if (url.startsWith("/")) {
 			return baseUrl + url;
 		}
 		return url;
 	}
 }
