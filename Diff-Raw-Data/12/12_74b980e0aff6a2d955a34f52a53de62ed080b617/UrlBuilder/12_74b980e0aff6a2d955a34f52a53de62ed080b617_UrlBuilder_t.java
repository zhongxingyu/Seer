 package org.offlike.server.service;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 public class UrlBuilder {
 	private static final String OFFLIKE_DOMAIN = "http://offlike.herokuapp.com/like/";
 	private static final String URL_ENCODING = "UTF-8";
 	
 
 	public static String createLikeURLWithContentHints(String qrCodeId, String campaignTitle)  {
 		final String URLdomain = createLikeURL(qrCodeId);
 		return URLdomain + createContentHintsPart(qrCodeId, campaignTitle);
 	}
 	
 	private static String createContentHintsPart(String qrCodeId, String campaignTitle)  {
 		String URLcamapignTitle;
 		try {
 			URLcamapignTitle = URLEncoder.encode(campaignTitle, URL_ENCODING);
 		} catch (UnsupportedEncodingException e) {
 			throw new IllegalStateException(e);
 		}
 		
 		return "?campaign_name=" + URLcamapignTitle;
 	}
 	
 	public static String createEncodedLikeURL(String qrCodeId, String campaignTitle) {
 		try {
			return URLEncoder.encode(createLikeURL(qrCodeId) + createContentHintsPart(qrCodeId, campaignTitle), URL_ENCODING);
 		} catch (UnsupportedEncodingException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	public static String createLikeURL(String qrCodeId)  {
 		try {
 			return OFFLIKE_DOMAIN +URLEncoder.encode(qrCodeId,URL_ENCODING);
 		} catch (UnsupportedEncodingException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 }
