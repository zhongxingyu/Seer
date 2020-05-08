 /* Copyright 2009 Meta Broadcast Ltd
 
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
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.query.uri.canonical.Canonicaliser;
 
 import com.google.common.collect.Lists;
 
 public class BbcUriCanonicaliser implements Canonicaliser {
 
 	private static final List<Pattern> alternateUris = Lists.newArrayList(
			Pattern.compile("https?://(?:www\\.)?bbc\\.co\\.uk/programmes/([bp]00[^\\./]+)(\\.rdf)?"),
			Pattern.compile("https?://(?:www\\.)?bbc\\.co\\.uk/iplayer/.*?/([bp]00[^\\./]+)"),
			Pattern.compile("https?://(?:www\\.)?bbc\\.co\\.uk/iplayer/.*?/([bp]00[^\\./]+)/.*?(?<!\\.rdf)"),
			Pattern.compile("https?://(?:www\\.)?fanhu.bz.*?([bp]00[^\\./]+).*"),
 			Pattern.compile("https?://bbc\\.co\\.uk/i/([^\\.]+?)/?")
 	);
 
 	public static String bbcProgrammeIdFrom(String uri) {
 		for (Pattern p : alternateUris) {
 			Matcher matcher = p.matcher(uri);
 			if (matcher.matches()) {
 				String pid = matcher.group(1);
				if (pid.startsWith("b00") || pid.startsWith("p00")) {
 					return pid;
 				} else {
 					return "b00" + pid;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String canonicalise(String alternate) {
 		String programmeId = bbcProgrammeIdFrom(alternate);
 		if (programmeId == null) {
 			return null;
 		}
 		return BbcFeeds.slashProgrammesUriForPid(programmeId);
 	}
 
 	// Curie [ bbc:b00abcd ]
 	static String curieFor(String episodeUri) {
 		String pid = bbcProgrammeIdFrom(episodeUri);
 		return "bbc:" + pid;
 	}
 }
