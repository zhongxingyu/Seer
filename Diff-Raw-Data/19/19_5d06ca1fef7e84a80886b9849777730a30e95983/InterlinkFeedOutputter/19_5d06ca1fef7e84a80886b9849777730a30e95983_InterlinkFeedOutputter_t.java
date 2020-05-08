 package org.atlasapi.feeds.interlinking.outputting;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 import nu.xom.Attribute;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Namespace;
 import nu.xom.Serializer;
 
 import org.atlasapi.feeds.interlinking.InterlinkBase;
 import org.atlasapi.feeds.interlinking.InterlinkBrand;
 import org.atlasapi.feeds.interlinking.InterlinkBroadcast;
 import org.atlasapi.feeds.interlinking.InterlinkContent;
 import org.atlasapi.feeds.interlinking.InterlinkEpisode;
 import org.atlasapi.feeds.interlinking.InterlinkFeed;
 import org.atlasapi.feeds.interlinking.InterlinkBase.Operation;
 import org.atlasapi.feeds.interlinking.InterlinkFeed.InterlinkFeedAuthor;
 import org.atlasapi.feeds.interlinking.InterlinkOnDemand;
 import org.atlasapi.feeds.interlinking.InterlinkSeries;
 import org.atlasapi.feeds.xml.XMLNamespace;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.joda.time.format.ISOPeriodFormat;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Strings;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class InterlinkFeedOutputter {
 
 	private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
 	
 	private static final XMLNamespace NS_ATOM = new XMLNamespace("atom", "http://www.w3.org/2005/Atom");
 	private static final XMLNamespace NS_DC = new XMLNamespace("dc", "http://purl.org/dc/terms");
 	private static final XMLNamespace NS_ILINK = new XMLNamespace("ilink", "http://www.bbc.co.uk/developer/interlinking");
 	private static final XMLNamespace NS_MRSS = new XMLNamespace("media", "http://search.yahoo.com/mrss/");
 	
 	public void output(InterlinkFeed feed, OutputStream out, boolean isBootstrap) throws IOException {
 		Element feedElem = createFeed(feed);
 		for (InterlinkBase entry : feed.entries()) {
 		    if (! isBootstrap || entry.operation() != Operation.DELETE) {
     			if (entry instanceof InterlinkBrand) {
     			    feedElem.appendChild(brandToEntry((InterlinkBrand) entry));
     			} else if (entry instanceof InterlinkSeries) {
     			    feedElem.appendChild(seriesToEntry((InterlinkSeries) entry));
     			} else if (entry instanceof InterlinkEpisode) {
     			    feedElem.appendChild(episodeToEntry((InterlinkEpisode) entry));
     			} else if (entry instanceof InterlinkBroadcast) {
     			    feedElem.appendChild(broadcastToEntry((InterlinkBroadcast) entry));
     			} else if (entry instanceof InterlinkOnDemand) {
     			    feedElem.appendChild(onDemandToEntry((InterlinkOnDemand) entry));
     			}
 		    }
 		}
 	    write(out, feedElem);  
 	}
 	
 	private Element onDemandToEntry(InterlinkOnDemand onDemand) {
 		Element entry = createElement("entry", NS_ATOM);
 		addCommonFieldsTo(onDemand, entry);
 		entry.appendChild(stringElement("type", NS_ILINK, "ondemand"));
 		if (onDemand.lastUpdated() != null) {
             entry.appendChild(stringElement("updated", NS_ATOM, onDemand.lastUpdated().toString(DATE_TIME_FORMAT)));
         }
 		if (onDemand.operation() != Operation.DELETE) {
     		Element mrssContent = createElement("content", NS_MRSS);
     		mrssContent.appendChild(stringElement("parent_id", NS_ILINK, onDemand.parentId()));
     		mrssContent.appendChild(stringElement("availability_start", NS_ILINK, onDemand.availabilityStart().toString(DATE_TIME_FORMAT)));
     		mrssContent.appendChild(stringElement("availability_end", NS_ILINK, onDemand.availabilityEnd().toString(DATE_TIME_FORMAT)));
     		if (onDemand.duration() != null) {
     			mrssContent.appendChild(stringElement("duration", NS_ILINK, duration(onDemand.duration())));
     		}
     		addService(onDemand.service(), mrssContent);
     		
     		// TODO: Static attributes for now
     		mrssContent.appendChild(stringElement("platform_code", NS_ILINK, "pc"));
     		mrssContent.appendChild(stringElement("payment_type", NS_ILINK, "free"));
     		entry.appendChild(atomContentElementContaining(mrssContent));
 		}
 		return entry;
 	}
 	
 	private void addService(String service, Element mrssContent) {
 	    if (service != null) {
             mrssContent.appendChild(stringElement("service", NS_ILINK, service(service)));
         }
 	}
 	
 	private String service(String service) {
	    //service = service.toLowerCase();
	    //if ("c4".equals(service)) return "channel4";
	    //if ("m4".equals(service)) return "more4";
 	    return service;
 	}
 	
 	private String duration(Duration duration) {
 	    Period period = duration.toPeriod();
 	    return ISOPeriodFormat.standard().print(period);
 	}
 
 	private Element broadcastToEntry(InterlinkBroadcast broadcast) {
 		Element entry = createElement("entry", NS_ATOM);
 		
 		addCommonFieldsTo(broadcast, entry);
 		
 		entry.appendChild(stringElement("type", NS_ILINK, "broadcast"));
 		if (broadcast.lastUpdated() != null) {
             entry.appendChild(stringElement("updated", NS_ATOM, broadcast.lastUpdated().toString(DATE_TIME_FORMAT)));
         }
 		
 		if (broadcast.operation() != Operation.DELETE) {
     		Element mrssContent = createElement("content", NS_MRSS);
     		mrssContent.appendChild(stringElement("parent_id", NS_ILINK, broadcast.episode().id()));
     		
     		DateTime broadcastStart = broadcast.broadcastStart();
     		if (broadcastStart != null) {
     			mrssContent.appendChild(stringElement("broadcast_start", NS_ILINK, broadcastStart.toString(DATE_TIME_FORMAT)));
     		}
     		
     		if (broadcast.duration() != null) {
     			mrssContent.appendChild(stringElement("duration", NS_ILINK, ISOPeriodFormat.standard().print(broadcast.duration().toPeriod())));
     		}
     		addService(broadcast.service(), mrssContent);
     		entry.appendChild(atomContentElementContaining(mrssContent));
 		}
 		return entry;
 	}
 
 	private Element episodeToEntry(InterlinkEpisode episode) {
 		Element entry = createElement("entry", NS_ATOM);
 		addCommonFieldsTo(episode, entry);
 		entry.appendChild(stringElement("type", NS_ILINK, "episode"));
 		
 		Element linkElement = createElement("link", NS_ATOM);
 		linkElement.addAttribute(new Attribute("href", episode.link()));
 		linkElement.addAttribute(new Attribute("rel", "alternate"));
 		entry.appendChild(linkElement);
 		
 		addCommonContentFieldsTo(episode, entry, episode.parent());
 		return entry;
 	}
 
 	private Element seriesToEntry(InterlinkSeries series) {
 		Element entry = createElement("entry", NS_ATOM);
 		addCommonFieldsTo(series, entry);
 		entry.appendChild(stringElement("type", NS_ILINK, "series"));
 		addCommonContentFieldsTo(series, entry, series.brand());
 		return entry;
 	}
 	
 	private Element brandToEntry(InterlinkBrand brand) {
 		Element entry = createElement("entry", NS_ATOM);
 		addCommonFieldsTo(brand, entry);
 		entry.appendChild(stringElement("type", NS_ILINK, "brand"));
 		addCommonContentFieldsTo(brand, entry, null);
 		return entry;
 	}
 
 	private Element contentElement(InterlinkContent content, InterlinkContent parent) {
 		Element mrssContent = createElement("content", NS_MRSS);
 		
 		if (content.description() != null) {
 			mrssContent.appendChild(stringElement("description", NS_MRSS, content.description()));
 		}
 		if (parent != null) {
             mrssContent.appendChild(stringElement("parent_id", NS_ILINK, parent.id()));
 			if(content.indexWithinParent() != null) {
 			    mrssContent.appendChild(stringElement("index", NS_ILINK, String.valueOf(content.indexWithinParent())));
 			}
 		}
 		if (content.title().matches("Episode \\d+")) {
 			mrssContent.appendChild(stringElement("presentation_title", NS_ILINK, content.title()));
 		}
 		if (content.thumbnail() != null) {
             Element thumbnail = createElement("thumbnail", NS_MRSS);
             thumbnail.addAttribute(new Attribute("url", content.thumbnail()));
             mrssContent.appendChild(thumbnail);
         }
 		return atomContentElementContaining(mrssContent);
 	}
 
 	private Element atomContentElementContaining(Element element) {
 		Element atomContent = createElement("content", NS_ATOM);
 		atomContent.addAttribute(new Attribute("type", "application/xml"));
 		atomContent.appendChild(element);
 		return atomContent;
 	}
 
 
 
 	private void addCommonContentFieldsTo(InterlinkContent content, Element entry, InterlinkContent parent) {
 	    entry.appendChild(stringElement("summary", NS_ATOM, Strings.nullToEmpty(content.summary())));
 
 	    DateTime updated = content.lastUpdated() != null ? content.lastUpdated() : new DateTime(DateTimeZones.UTC);
 	    entry.appendChild(stringElement("updated", NS_ATOM, updated.toString(DATE_TIME_FORMAT)));
 		
 		if (content.operation() != Operation.DELETE) {
 		    entry.appendChild(contentElement(content, parent));
 		}
 	}
 
 	private Element createFeed(InterlinkFeed feed) {
 		Element feedElem = new Element("feed", NS_ATOM.getUri());
 
 		NS_DC.addDeclarationTo(feedElem);
 		NS_MRSS.addDeclarationTo(feedElem);
 		NS_ILINK.addDeclarationTo(feedElem);
 		
 		feedElem.addAttribute(new Attribute("xml:lang", Namespace.XML_NAMESPACE, "en-GB"));
 		
 		feedElem.appendChild(stringElement("title", NS_ATOM, feed.title()));
 		feedElem.appendChild(stringElement("subtitle", NS_ATOM, feed.subtitle()));
 		feedElem.appendChild(stringElement("updated", NS_ATOM, feed.updated().toString(DATE_TIME_FORMAT)));
 		feedElem.appendChild(stringElement("id", NS_ATOM, feed.id()));
 
 		Element authorElem = createElement("author", NS_ATOM);
 		InterlinkFeedAuthor author = feed.author();
 		authorElem.appendChild(stringElement("partner", NS_ILINK, author.partner()));
 		authorElem.appendChild(stringElement("supplier", NS_ILINK, author.supplier()));
 		feedElem.appendChild(authorElem);
 		
 		return feedElem;
 	}
 	
 	private void addCommonFieldsTo(InterlinkBase base, Element entry) {
 		if (base.title() != null) {
 			entry.appendChild(stringElement("title", NS_ATOM, base.title()));
 		}
 		entry.appendChild(stringElement("id", NS_ATOM, base.id()));
 		if (base.operation() != null) {
 			entry.appendChild(stringElement("operation", NS_ILINK, base.operation().toString()));
 		}
 	}
 
 	private void write(OutputStream out, Element feed) throws UnsupportedEncodingException, IOException {
 		Serializer serializer = new Serializer(out, Charsets.UTF_8.toString());
 	    serializer.setIndent(4);
 	    serializer.setLineSeparator("\n");
 		serializer.write(new Document(feed));
 	}
 
 	private Element stringElement(String name, XMLNamespace ns, String value) {
 		Element elem = createElement(name, ns);
 		elem.appendChild(value);
 		return elem;
 	}
 
 	private Element createElement(String name, XMLNamespace ns) {
 		Element elem = new Element(name, ns.getUri());
 		if (!NS_ATOM.equals(ns)) {
 			elem.setNamespacePrefix(ns.getPrefix());
 		}
 		return elem;
 	}
 }
