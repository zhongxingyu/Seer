 package org.atlasapi.query.content;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.remotesite.bbc.BbcIplayerHightlightsAdapter;
 import org.atlasapi.remotesite.bbc.BbcUriCanonicaliser;
 import org.atlasapi.remotesite.channel4.C4HighlightsAdapter;
 import org.atlasapi.remotesite.itv.ItvMercuryBrandAdapter;
 import org.atlasapi.remotesite.youtube.YoutubeUriCanonicaliser;
 
 import com.metabroadcast.common.base.Maybe;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class PerPublisherCurieExpander implements CurieExpander {
 
 	public enum CurieAlgorithm {
 		BBC {
 
 			@Override
 			public String expand(String curie) {
 				Maybe<String> uri = BbcIplayerHightlightsAdapter.expand(curie);
 				if (uri.hasValue()) {
 					return uri.requireValue();
 				}
 				return "http://www.bbc.co.uk/programmes/" + curie.substring(4);
 			}
 
 			@Override
 			public String compact(String url) {
 				Maybe<String> curie = BbcIplayerHightlightsAdapter.compact(url);
 				if (curie.hasValue()) {
 					return curie.requireValue();
 				}
 				return "bbc:" + BbcUriCanonicaliser.bbcProgrammeIdFrom(url);
 			}
 		},
 		C4 {
 			
 			final String separator = "_";
 			@Override
 			public String expand(String curie) {
 				Maybe<String> uri = expandC4Curie(curie);
 				if (uri.hasValue()) {
 					return uri.requireValue();
 				}
 				uri = C4HighlightsAdapter.expand(curie);
 				if (uri.hasValue()) {
 					return uri.requireValue();
 				}
 				String withoutPrefix = curie.substring(curie.indexOf(":") + 1);
 				if (withoutPrefix.contains(separator)) {
 					String[] components = withoutPrefix.split(separator);
 					return String.format("http://www.channel4.com/programmes/%s/4od#%s", components[0], components[1]);
 				} else {
					return String.format("http://www.channel4.com/programmes/%s", withoutPrefix.replace("-series", "/episode-guide/series").replace("-episode", "/episode"));
 				}
 			}
 
 			final Pattern c4BrandIdPattern = Pattern.compile("http://www.channel4.com/programmes/([^\\./&=]+)(?:/episode-guide/(series-\\d+)(?:/(episode-\\d+))?)?");
 			final Pattern c4odPattern = Pattern.compile("http://www.channel4.com/programmes/([^\\./&=]+)/4od#([^\\./&=]+).*");
 
 			@Override
 			public String compact(String url) {
 				
 				Maybe<String> curie = compactC4Uri(url);
 				if (curie.hasValue()) {
 					return curie.requireValue();
 				}
 				curie = C4HighlightsAdapter.compact(url);
 				if (curie.hasValue()) {
 					return curie.requireValue();
 				}
 				
 				Matcher itemMatcher = c4odPattern.matcher(url);
 				if (itemMatcher.matches()) {
 					return String.format("%s:%s_%s", this.name().toLowerCase(), itemMatcher.group(1), itemMatcher.group(2));
 				}
 
 				Matcher brandIdMatcher = c4BrandIdPattern.matcher(url);
 				if (brandIdMatcher.matches()) {
 					String brandIdPossiblyWithSeries = brandIdMatcher.group(1);
 					if (brandIdMatcher.group(2) != null) {
 						brandIdPossiblyWithSeries += "-" + brandIdMatcher.group(2);
 					}
 					if (brandIdMatcher.group(3) != null) {
 						brandIdPossiblyWithSeries += "-" + brandIdMatcher.group(3);
 					}
 					return String.format("%s:%s", this.name().toLowerCase(), brandIdPossiblyWithSeries);
 				}
 				
 				return null;
 			}
 		},
 		ITV {
 			private static final String ITV_CURIE = "itv:";
 			
 			@Override
 			public String expand(String curie) {
 				if (curie.startsWith(ITV_CURIE)) {
 				    return ItvMercuryBrandAdapter.BASE_URL+curie.replace(ITV_CURIE, "");
 				}
 				return null;
 			}
 
 			@Override
 			public String compact(String url) {
 				Matcher matcher = ItvMercuryBrandAdapter.BRAND_URL.matcher(url);
 				if (matcher.matches()) {
 				    return ITV_CURIE+matcher.group(1);
 				}
 
 				return null;
 			}
 		},
 		YT {
 			@Override
 			public String expand(String curie) {
 				return "http://www.youtube.com/watch?v=" + curie.substring(3);
 			}
 
 			@Override
 			public String compact(String url) {
 				return "yt:" + YoutubeUriCanonicaliser.videoIdFrom(url);
 			}	
 		},
 		FB {
 			@Override
 			public String expand(String curie) {
 				return "http://graph.facebook.com/" + curie.substring(3);
 			}
 
 			@Override
 			public String compact(String url) {
 				return "fb:" + url.replace("http://graph.facebook.com/", "");
 			}	
 		},
 		BLIP {
 			@Override
 			public String expand(String curie) {
 				return "http://blip.tv/file/" + curie.substring(5);
 			}
 
 			@Override
 			public String compact(String url) {
 				return "blip:" + removeTrailingSlash(url.replace("http://blip.tv/file/", ""));
 			}
 		},
 		VIM {
 			@Override
 			public String expand(String curie) {
 				return "http://vimeo.com/" + curie.substring(4);
 			}
 
 			final Pattern vimeoPattern = Pattern.compile("https?://.*vimeo.com/([^\\./&=]+).*");
 
 			@Override
 			public String compact(String url) {
 				return "vim:" + matchAgainst(url, vimeoPattern);
 			}	
 		},
 		DM {
 			@Override
 			public String expand(String curie) {
 				return "http://www.dailymotion.com/video/" + curie.substring(3);
 			}
 
 			final Pattern dailyMotionPattern = Pattern.compile("https?://.*dailymotion.com/video/([^\\./&=]+).*");
 
 			@Override
 			public String compact(String url) {
 				return "dm:" + matchAgainst(url, dailyMotionPattern);
 			}	
 		},
 		HULU {
 		    final Pattern episodePattern = Pattern.compile("^\\d+$");
 		    
 			@Override
 			public String expand(String curie) {
 			    String identifier = curie.substring(5);
 			    Matcher matcher = episodePattern.matcher(identifier);
 			    if (matcher.matches()) {
 			        return "http://www.hulu.com/watch/" + identifier;
 			    } else {
 			        return "http://www.hulu.com/" + identifier;
 			    }
 			}
 
 			final Pattern huluEpisodePattern = Pattern.compile("https?://.*hulu.com/watch/([^\\./&=]+).*");
 			final Pattern huluBrandPattern = Pattern.compile("https?://.*hulu.com/([a-z\\-]+).*");
 
 			@Override
 			public String compact(String url) {
 			    Matcher matcher = huluEpisodePattern.matcher(url);
 			    if (matcher.matches()) {
 			        return "hulu:" + matcher.group(1);
 			    } else {
 			        return "hulu:" + matchAgainst(url, huluBrandPattern);
 			    }
 			}	
 		},
 		TED {
 			@Override
 			public String expand(String curie) {
 				return "http://www.ted.com/talks/" + curie.substring(4) + ".html";
 			}
 
 			final Pattern tedPattern = Pattern.compile("https?://.*ted.com/talks/([^\\./&=]+).html");
 
 			@Override
 			public String compact(String url) {
 				return "ted:" + matchAgainst(url, tedPattern);
 			}	
 		},
 		
 		WIKI {
 			
 			private final Pattern WIKI_CURIE_PATTERN = Pattern.compile(name().toLowerCase() + "(:[a-z]{2})?:(.+)");
 
 			@Override
 			public String expand(String curie) {
 				Matcher matcher = WIKI_CURIE_PATTERN.matcher(curie);
 				if (matcher.matches()) {
 					String subdomain = matcher.group(1) == null ? "en" : matcher.group(1).substring(1);
 					return String.format("http://%s.wikipedia.org/%s", subdomain, matcher.group(2));
 				}
 				return null;
 			}
 
 			private final Pattern WIKI_FULL_URL_PATTERN = Pattern.compile("https?://([a-z]{2})\\.wikipedia.org/([^/]+)");
 
 			@Override
 			public String compact(String url) {
 				Matcher matcher = WIKI_FULL_URL_PATTERN.matcher(url);
 				if (matcher.matches()) {
 					return String.format("%s:%s:%s", name().toLowerCase(), matcher.group(1), matcher.group(2));
 				}
 				return null;
 			}	
 		};
 		
 		public abstract String expand(String curie);
 		public abstract String compact(String url);
 		
 		private static String removeTrailingSlash(String url) {
 			if (url.endsWith("/")) {
 				return url.substring(0, url.length() - 1);
 			}
 			return url;
 		}
 		
 		private static String matchAgainst(String uri, Pattern pattern) {
 			Matcher matcher = pattern.matcher(uri);
 			if (matcher.matches()) {
 				return matcher.group(1);
 			}
 			return null;
 		}
 
 	}
 	
 	@Override
 	public Maybe<String> expand(String curie) {
 		String prefix = prefixOf(curie);
 		if (prefix == null) {
 			return Maybe.nothing();
 		}
 		CurieAlgorithm algorithm;
 		try {
 			algorithm = CurieAlgorithm.valueOf(prefix.toUpperCase());
 		} catch (IllegalArgumentException e) {
 			// no matching algorithm
 			return Maybe.nothing();
 		}
 		return Maybe.fromPossibleNullValue(algorithm.expand(curie));
 	}
 
 	private String prefixOf(String curie) {
 		int index = curie.indexOf(":");
 		if (index < 1) {
 			return null;
 		}
 		return curie.substring(0, index);
 	}
 	
 	private static final String C4_ATOZ_CURIE_PREFIX = "c4:atoz_";
     private static final String C4_ATOZ_URI_PREFIX = "http://www.channel4.com/programmes/atoz/";
     private static final Pattern ATOZ = Pattern.compile(C4_ATOZ_URI_PREFIX + "([a-z]|0-9)");
 
 	private static Maybe<String> compactC4Uri(String uri) {
         Matcher matcher = ATOZ.matcher(uri);
         if (matcher.matches()) {
             return Maybe.just(C4_ATOZ_CURIE_PREFIX +  matcher.group(1));
         }
         return Maybe.nothing();
     }
 
     private static Maybe<String> expandC4Curie(String curie) {
         if (curie.startsWith(C4_ATOZ_CURIE_PREFIX)) {
             return Maybe.just(C4_ATOZ_URI_PREFIX + curie.substring(C4_ATOZ_CURIE_PREFIX.length()));
         }
         return Maybe.nothing();
     }
 }
