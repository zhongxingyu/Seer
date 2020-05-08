 package com.sk.api.impl;
 
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
 import com.sk.util.PersonalData;
 import com.sk.util.parse.scrape.BasicGrabber;
 import com.sk.util.parse.scrape.Grabber;
 
 public class LinkedinApiSearcher extends AbstractApiSearcher {
 
 	public LinkedinApiSearcher() {
 		super(new ApiUtility(LinkedInApi.class));
 		util.init("gtgrab");
 
 	}
 
 	private static final String BASE = "http://api.linkedin.com/v1/people-search:(people:(api-standard-profile-request))?count=25";
 	private static final String REQUEST_FIELDS = ":(first-name,last-name,headline,location:(name,country:(code)),industry,summary,specialties,positions)";
 
 	@Override
 	public OAuthRequest getNameRequest(String first, String last) {
 		try {
 			return new OAuthRequest(Verb.GET, BASE + "&first-name=" + URLEncoder.encode(first, "UTF-8")
 					+ "&last-name=" + URLEncoder.encode(last, "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException();
 		}
 	}
 
 	private static final Grabber[] grabbers = { new BasicGrabber("first-name", "first-name"),
 			new BasicGrabber("last-name", "last-name"), new BasicGrabber("location name", "location"),
 			new BasicGrabber("location country code", "country"), new BasicGrabber("industry", "industry"),
 			new BasicGrabber("headline", "job-title") };
 
 	@Override
 	public boolean parseResponse(Response resp) {
 		if (resp == null)
 			return false;
 		String body = resp.getBody();
 		if (body == null || body.length() == 0)
 			return false;
 		Document xdoc = Jsoup.parse(body, "", Parser.xmlParser());
 		List<PersonalData> data = new ArrayList<PersonalData>();
 		for (Element person : xdoc.select("person")) {
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
 			PersonalData dat = new PersonalData("linkedin");
 			Document pdoc = Jsoup.parse(pbody, "", Parser.xmlParser());
 			for (Grabber g : grabbers) {
 				g.grab(pdoc, dat);
 			}
 			dat.put("name", dat.get("first-name").get() + " " + dat.get("last-name").get());
 			data.add(dat);
 		}
 		this.data.set(data.toArray(new PersonalData[data.size()]));
 		return true;
 	}
 }
