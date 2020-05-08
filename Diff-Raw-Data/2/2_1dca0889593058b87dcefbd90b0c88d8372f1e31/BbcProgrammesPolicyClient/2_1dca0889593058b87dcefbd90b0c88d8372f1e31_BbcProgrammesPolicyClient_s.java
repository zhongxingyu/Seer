 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.bbc;
 
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Country;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.remotesite.HttpClients;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 public class BbcProgrammesPolicyClient {
 
 	private static final String EPISODE_FEED_BASE_URI = "http://feeds.bbc.co.uk/iplayer/episode/";
 	private static final Pattern PID_PATTERN = Pattern.compile("(b00[^\\s]+)");
 	
 	private final SimpleHttpClient client;
 
 	public BbcProgrammesPolicyClient(SimpleHttpClient client) {
 		this.client = client;
 	}
 	
 	public BbcProgrammesPolicyClient() {
 		this(HttpClients.webserviceClient());
 	}
 	
 	public Maybe<Policy> policyForUri(String episodeUri) {
 		return policyForEpisodeWithPid(pidFrom(episodeUri));
 	}
 	
 	
 	private static String pidFrom(String episodeUri) {
 		Matcher matcher = PID_PATTERN.matcher(episodeUri);
 		if (matcher.find()) {
 			return matcher.group(1);
 		}
 		throw new IllegalArgumentException("Uri does not contain a PID");
 	}
 
 	private Maybe<Policy> policyForEpisodeWithPid(String pid) {
 		try {
 			return policyFromAtom(client.getContentsOf(EPISODE_FEED_BASE_URI + pid));
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
	private static final Pattern START_END_FORMAT = Pattern.compile("<dcterms:valid>[^<]*start=(.+);[^<]*end=(.+);", Pattern.DOTALL);
 	private static final Pattern RESTRICTION_PATTERN = Pattern.compile("<media:restriction relationship=\"allow\"[^>]*>([a-zA-Z]+)<");
 	
 	private Maybe<Policy> policyFromAtom(String data) {
 		
 		Set<Country> availableCountries = null;
 		
 		Matcher restrictionMatcher = RESTRICTION_PATTERN.matcher(data);
 		
 		if (restrictionMatcher.find()) {
 			availableCountries = Countries.fromDelimtedList(restrictionMatcher.group(1));
 			
 		}
 		
 		Interval validRange = null;
 			Matcher matcher = START_END_FORMAT.matcher(data);
 			if (matcher.find()) {
 				validRange = new Interval(new DateTime(matcher.group(1)), new DateTime(matcher.group(2)));
 			}
 		
 		if (availableCountries != null && validRange != null) {
 			Policy policy = new Policy();
 			policy.setAvailableCountries(availableCountries);
 			policy.setAvailabilityStart(validRange.getStart());
 			policy.setAvailabilityEnd(validRange.getEnd());
 			return Maybe.just(policy);
 		}
 		
 		return Maybe.nothing();
 	}
 }
