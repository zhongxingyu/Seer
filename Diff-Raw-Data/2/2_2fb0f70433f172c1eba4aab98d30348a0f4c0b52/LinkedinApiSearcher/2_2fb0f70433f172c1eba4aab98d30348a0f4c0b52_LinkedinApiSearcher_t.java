 package com.sk.api.impl;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.parser.Parser;
 import org.scribe.builder.api.LinkedInApi;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import com.sk.api.AbstractApiSearcher;
 import com.sk.api.ApiUtility;
 import com.sk.api.NameComparison;
 import com.sk.util.FieldBuilder;
 import com.sk.util.PersonalData;
 import com.sk.util.parse.scrape.BasicGrabber;
 import com.sk.util.parse.scrape.Grabber;
 
 public class LinkedinApiSearcher extends AbstractApiSearcher {
 
 	public LinkedinApiSearcher() {
 		super(new ApiUtility(LinkedInApi.class));
 		util.init("gtgrab");
 
 	}
 
 	private static final String BASE = "http://api.linkedin.com/v1/people-search:(people:"
 			+ "(api-standard-profile-request,first-name,last-name),num-results)?count=25&"
 			+ "first-name=%s&last-name=%s&start=%d";
 	private static final String REQUEST_FIELDS = ":(first-name,last-name,headline,"
 			+ "location:(name,country:(code)),industry,summary,specialties,positions,"
 			+ "picture-url,main-address,phone-numbers,twitter-accounts)";
 
 	public OAuthRequest getNameRequest(String first, String last, int start) {
 		try {
 			return new OAuthRequest(Verb.GET, String.format(BASE, URLEncoder.encode(first, "UTF-8"),
 					URLEncoder.encode(last, "UTF-8"), start));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException();
 		}
 	}
 
 	private static final Grabber[] grabbers = { new BasicGrabber("first-name", "firstName"),
 			new BasicGrabber("last-name", "lastName"), new BasicGrabber("location name", "location"),
 			new BasicGrabber("location country code", "country"),
 			new BasicGrabber("person > industry", "industry"), new BasicGrabber("positions title", "jobTitle"),
 			new BasicGrabber("positions company name", "company"), new BasicGrabber("person > summary", "blob"),
 			new BasicGrabber("picture-url", "profilePictureUrl"), new BasicGrabber("main-address", "address"),
 			new BasicGrabber("phone-number > phone-number", "phone"),
			new BasicGrabber("twitter-account > provider-account-name", "twitter") };
 
 	public int parseResponse(Response resp, int start, List<PersonalData> data, String... names) {
 		if (resp == null)
 			return -1;
 		String body = resp.getBody();
 		if (body == null || body.length() == 0)
 			return -1;
 		Document xdoc = Jsoup.parse(body, "", Parser.xmlParser());
 		int total;
 		try {
 			total = Integer.parseInt(xdoc.select("num-results").text());
 		} catch (NumberFormatException ex) {
 			System.out.println(body);
 			return -1;
 		}
 
 		NameComparison nameUtil = NameComparison.get();
 		for (Element person : xdoc.select("person")) {
 			FieldBuilder builder = new FieldBuilder();
 
 			String first = person.select("first-name").text(), last = person.select("last-name").text();
 			if (!nameUtil.isSameName(names, new String[] { first, last }))
 				return -1;
 			String url = person.select("api-standard-profile-request url").text();
 			if (url.length() < 10)
 				continue;
 			OAuthRequest req = new OAuthRequest(Verb.GET, url + REQUEST_FIELDS);
 			for (Element httpHeader : person.select("api-standard-profile-request http-header")) {
 				req.addHeader(httpHeader.select("name").text(), httpHeader.select("value").text());
 			}
 			Response presp = util.send(req);
 			String pbody = presp.getBody();
 			if (pbody == null || pbody.length() == 0)
 				continue;
 			Document pdoc = Jsoup.parse(pbody, "", Parser.xmlParser());
 			for (Grabber g : grabbers) {
 				g.grab(pdoc, builder);
 			}
 			PersonalData dat = new PersonalData("linkedin");
 			builder.joinNames();
 			builder.addTo(dat);
 			data.add(dat);
 		}
 		if (total <= start + 25)
 			return -1;
 		return start + 25;
 	}
 
 	@Override
 	public boolean lookForName(String first, String last) throws IOException {
 		int start = 0;
 		List<PersonalData> found = new ArrayList<>();
 		do {
 			start = parseResponse(getResponse(getNameRequest(first, last, start)), start, found, first, last);
 		} while (start != -1);
 		this.data.set(found.toArray(new PersonalData[found.size()]));
 		return !found.isEmpty();
 	}
 }
