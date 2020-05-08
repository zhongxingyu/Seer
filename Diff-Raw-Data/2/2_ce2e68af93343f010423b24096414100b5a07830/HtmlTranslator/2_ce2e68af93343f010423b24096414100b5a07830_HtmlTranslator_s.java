 package org.uriplay.beans.html;
 
 import static org.springframework.web.util.HtmlUtils.htmlEscape;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.jherd.beans.BeanGraphWriter;
 import org.jherd.core.Defect;
 import org.springframework.core.io.ClassPathResource;
 import org.uriplay.media.entity.Description;
 import org.uriplay.media.entity.Encoding;
 import org.uriplay.media.entity.Episode;
 import org.uriplay.media.entity.Item;
 import org.uriplay.media.entity.Location;
 import org.uriplay.media.entity.Playlist;
 import org.uriplay.media.entity.Version;
 import org.uriplay.media.util.ChildFinder;
 
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 /**
  * {@link BeanGraphWriter} that translates the full URIplay object model
  * into a simplified form and renders that as XML.
  *  
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class HtmlTranslator implements BeanGraphWriter {
 
 	private static final String NEW_LINE = "\n";
 	private final String header;
 	private final String footer;
 
 	public HtmlTranslator() throws IOException {
 		header = readHtmlFrom("/html/header.htmlf");
 		footer = readHtmlFrom("/html/footer.htmlf");
 	}
 	
 	private Iterable<Object> rootsOf(Collection<Object> beans) {
 		return Iterables.filter(beans, Predicates.not(new ChildFinder(beans)));
 	}
 	
 	public void writeTo(Collection<Object> fullGraph, OutputStream stream) {
 		Iterable<Object> beansToProcess = beansToProcess(fullGraph);
 		Writer writer;
 		try {
 			writer = new OutputStreamWriter(stream, "UTF-8");
 		} catch (UnsupportedEncodingException e1) {
 			throw new Defect("incorrect encoding specified");
 		}
 		try {
 			
 			beginPage(writer);
 			
 			if (beansToProcess != null && beansToProcess.iterator().hasNext()) {
 				writeSummaryOf(beansToProcess, writer);
 				writeFullDescriptionOf(beansToProcess, writer);
 			} else {
 				writeNoBeansFound(writer);
 			}
 			endPage(writer);
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				writer.flush();
 				writer.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	
 	}
 
 	private void writeNoBeansFound(Writer writer) throws IOException {
 		writer.write("<h1>Nothing matched your query :(</h1>");
 	}
 
 	private Iterable<Object> beansToProcess(Collection<Object> fullGraph) {
 		if (fullGraph == null) {
 			return null;
 		} else {
 			return rootsOf(fullGraph);
 		}
 	}
 
 	private void writeSummaryOf(Iterable<Object> beansToProcess, Writer writer) throws IOException {
 
 		if (totalNumberOfPlaylistsIn(beansToProcess) > 1) {
 			return; // don't output summary
 		}
 		
 		for (Object bean : beansToProcess) {
 
 			Description description = (Description) bean;
 			
 			bold(htmlEscape(description.getCanonicalUri()), writer);
 			writer.write("<br />");
 			
 			if (bean instanceof Playlist) {
 
 				Playlist playlist = (Playlist) bean;
 				writeSummaryBox("Playlist", playlist.getTitle(), playlist.getDescription(), firstEmbedCodeFrom(playlist), writer);
 				return;
 				
 			} 
 			
 			if (bean instanceof Item) {
 
 				Item item = (Item) bean;
 				writeSummaryBox("Item", item.getTitle(), item.getDescription(), firstEmbedCodeFrom(item), writer);
 				return;
 				
 			} 
 		}
 		
 	}
 
 	private int totalNumberOfPlaylistsIn(Iterable<Object> beansToProcess) {
 		
 		int count = 0;
 		for (Object bean : beansToProcess) {
 			if (bean instanceof Playlist) {
 				count++;
 			}
 		}
 		return count;
 	}
 
 	private String firstEmbedCodeFrom(Playlist playlist) {
 		
 		for (Item item : playlist.getItems()) {
 			String embedCode = firstEmbedCodeFrom(item);
 			if (!StringUtils.isEmpty(embedCode)) {
 				return embedCode;
 			}
 		}
 		
 		for (Playlist innerPlaylist : playlist.getPlaylists()) {
 			String embedCode = firstEmbedCodeFrom(innerPlaylist);
 			if (!StringUtils.isEmpty(embedCode)) {
 				return embedCode;
 			}
 		}
 		
 		return null;
 	}
 
 	private String firstEmbedCodeFrom(Item item) {
 		
 		for (Version version : item.getVersions()) {
 			for (Encoding encoding : version.getManifestedAs()) {
 				for (Location location : encoding.getAvailableAt()) {
 					if (!StringUtils.isEmpty(location.getEmbedCode())) {
 						return location.getEmbedCode();
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	private void writeSummaryBox(String type, String title, String description, String embedCode, Writer writer) throws IOException {
 
 		bold(type, title, writer);
 		
 		if (description != null) {
 			writer.write(description);
 		}
 
 		writer.write("<br />");
 		if (embedCode != null) {
 			writer.write(embedCode);
 		}
 		
 		writer.write("<hr />");
 	}
 
 	private static void bold(String title, Writer writer) throws IOException {
 		writer.write("<strong>");
 		writer.write(title == null ? "" : title);
 		writer.write("</strong>");
 		writer.write(" ");
 	}
 
 	private void writeFullDescriptionOf(Iterable<Object> beansToProcess, Writer writer) throws IOException {
 		
 		Set<Object> processed = Sets.newHashSet();
 		
 		for (Object bean : beansToProcess) {
 
 			if (bean instanceof Playlist && !processed.contains(bean)) {
 
 				Playlist playList = (Playlist) bean;
 				writeHtmlForPlaylist(playList, writer, processed);
 				processed.add(playList);
 				processed.addAll(playList.getItems());
 			} 
 		}
 		
 		for (Object bean : beansToProcess) {
 
 			if (bean instanceof Item && !processed.contains(bean)) {
 
 				Item item = (Item) bean;
 				writeHtmlFor(item, writer, true);
 				processed.add(item);
 			} 
 		}
 	}
 
 	private void beginPage(Writer writer) throws IOException {
 		writer.write(header);
 	}
 	
 	private void endPage(Writer writer) throws IOException {
 		writer.write(footer);
 	}
 
 	private String readHtmlFrom(String resourceName) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(resourceName).getInputStream()));
 		
 		StringBuffer html = new StringBuffer();
 		String line;
 		while((line = reader.readLine()) !=  null) {
 			html.append(line);
 		}
 	
 		String header = html.toString();
 		return header;
 	}
 	
 	static void writeHtmlForPlaylist(Playlist playlist, Writer writer, Set<Object> processed) throws IOException {
 
 		beginDefinitionList("playlist", writer);
 
 		defineTerm("<strong>Playlist :</strong> canonical URI", uriplayLink(playlist.getCanonicalUri()), writer);
 		defineTerm("aliases", listOf(playlist.getAliases()), writer);
 		defineTerm("curie", playlist.getCurie(), writer);
 		defineTerm("title", playlist.getTitle(), writer);
 		defineTerm("description", playlist.getDescription(), writer);
 		defineTerm("contained in", listOf(containedInLinksFor(playlist)), writer);
 
 		if (!playlist.getItems().isEmpty()) {
 			beginNestedDefinitionList("Items", writer);
 
 			for (Item item : playlist.getItems()) {
 				writeHtmlFor(item, writer);
 			}
 			endNestedDefinitionList(writer);
 		}
 		
 		if (!playlist.getPlaylists().isEmpty()) {
 			beginNestedDefinitionList("Playlists", writer);
 
 			for (Playlist subPlaylist : playlist.getPlaylists()) {
 				writeHtmlForPlaylist(subPlaylist, writer, processed);
 			}
 			endNestedDefinitionList(writer);
 		}
 		
 		endDefinitionList(writer);
 	}
 
 	private static void bold(String title, String subtitle, Writer writer) throws IOException {
 		if (subtitle == null) {
 			bold(title, writer);
 		} else {
 			bold(title + " - " + subtitle, writer);
 		}
 	}
 
 	private static String externalLink(String url) {
 		if (url == null) { return null; }
 		return String.format("<a href=\"%s\">%s</a> (ext)", url, url);
 	}
 	
 	private static String uriplayLink(String url) {
 		if (url == null) { return null; }
 			return constructLink(url, url, "html") + " (or view as " + constructLink(url, "RDF", "rdf.xml") + " or " + constructLink(url, "JSON", "json") + ")";
 		
 	}
 
 	private static String constructLink(String url, String title, String format) {
 		try {
 			return String.format("<a href=\"/2.0/doc.%s?uri=%s\">%s</a>", format, URLEncoder.encode(url, "UTF-8"), htmlEscape(title));
 		} catch (UnsupportedEncodingException e) {
 			throw new Defect("incorrect encoding specified");
 		}
 	}
 
 	private static void beginDefinitionList(String cssClass, Writer writer) throws IOException {
 		writer.write("<dl class=\"" + cssClass + "\">");
 	}
 
 	private static void endDefinitionList(Writer writer) throws IOException {
 		writer.write("</dl>");
 		writer.write(NEW_LINE);
 	}
 
 	private static void defineTerm(String term, Object description, Writer writer) throws IOException {
 		
 		if (term == null || description == null) { return; }
 		
 		writer.write("<dt>");
 		writer.write(term);
 		writer.write("</dt>");
 		writer.write("<dd>");
 		writer.write(description.toString());
 		writer.write("</dd>");
 		
 		writer.write(NEW_LINE);
 	}
 
 	static void writeHtmlFor(Item item, Writer writer) throws IOException {
 		writeHtmlFor(item, writer, false);
 	}
 
 	static void writeHtmlFor(Item item, Writer writer, boolean topLevel) throws IOException {
 
 		if (topLevel) {
 			beginDefinitionList("item", writer);
 		}
 
 		defineTerm("<strong>Item :</strong> canonical URI", uriplayLink(item.getCanonicalUri()), writer);
 		defineTerm("aliases", listOf(item.getAliases()), writer);
 		defineTerm("curie", item.getCurie(), writer);
 		defineTerm("title", item.getTitle(), writer);
 		defineTerm("description", item.getDescription(), writer);
 		defineTerm("isLongForm", item.getIsLongForm(), writer);
 
 		defineTerm("publisher", queryApiPublisherLink(item.getPublisher()), writer);
 		defineTerm("image", externalLink(item.getImage()), writer);
 		defineTerm("thumbnail", image(item.getThumbnail()), externalLink(item.getThumbnail()), writer);
 
 		defineTerm("genres", listOf(queryApiGenreLinks(item.getGenres())), writer);
 		defineTerm("tags", listOf(externalLinks(item.getTags())), writer);
 
 		if (item instanceof Episode) {
 			Episode episode = (Episode) item;
 			defineTerm("series number", episode.getSeriesNumber(), writer);
 			defineTerm("episode number", episode.getEpisodeNumber(), writer);
 		}
 
 		defineTerm("contained in", listOf(containedInLinksFor(item)), writer);
 
 		beginNestedDefinitionList("versions", writer);
 
 		for (Version version : item.getVersions()) {
 			writeHtmlFor(version, writer);
 		}
 
 		endNestedDefinitionList(writer);
 
 		if (topLevel) {
 			endDefinitionList(writer);
 		}
 	}
 
 	private static Set<String> queryApiGenreLinks(Set<String> genres) {
 		Set<String> links = Sets.newHashSet();
 		for (String genre : genres) {
 			links.add(queryApiGenreLink(genre));
 		}
 		return links;
 	}
 
 	private static String queryApiGenreLink(String genreUrl) {
 		String format = "html";
 		return String.format("<a href=\"/2.0/query/items.%s?genre-contains=%s\">%s</a>", format, lastPartOf(genreUrl), htmlEscape(genreUrl));
 	}
 	
 	private static String queryApiPublisherLink(String publisher) {
 		String format = "html";
 		return String.format("<a href=\"/2.0/query/items.%s?publisher=%s\">%s</a>", format, publisher, publisher);
 	}
 
 
 	private static String lastPartOf(String genreUrl) {
 		return genreUrl.substring(genreUrl.lastIndexOf("/") + 1);
 	}
 
 
 	private static Set<String> externalLinks(Set<String> urls) {
 		Set<String> links = Sets.newHashSet();
 		for (String url : urls) {
 			links.add(externalLink(url));
 		}
 		return links;
 	}
 
 	private static Set<String> containedInLinksFor(Item item) {
 		return containedInLinksFor(item.getContainedInUris());
 	}
 	
 	private static Set<String> containedInLinksFor(Playlist playlist) {
 		return containedInLinksFor(playlist.getContainedInUris());
 	}
 
 	private static Set<String> containedInLinksFor(Set<String> containedInUris) {
 		Set<String> uriplayLinks = Sets.newHashSet();
 		for (String containingPlaylist : containedInUris) {
 			uriplayLinks.add(uriplayLink(containingPlaylist));
 		}
 		return uriplayLinks;
 	}
 
 	private static void defineTerm(String term, Object description1, Object description2, Writer writer) throws IOException {
 		if (description1 == null && description2 == null) { return; }
 		if (description1 == null) {
 			defineTerm(term, description2, writer);
 		} else if (description2 == null) {
 			defineTerm(term, description1, writer);
 		} else {
 			defineTerm(term, description1.toString() + "<br/>" + description2.toString(), writer);
 		}
 	}
 
 	private static String image(String url) {
 		if (url == null) { return null; }
 		return "<img src=\"" + url +"\" alt=\"thumbnail image\" />";
 	}
 
 	private static void writeHtmlFor(Version version, Writer writer) throws IOException {
 
 		beginNestedDefinitionList("Version", writer);
 		
 			defineTerm("published duration", version.getPublishedDuration(), writer);
 			defineTerm("duration", version.getDuration(), writer);
 			defineTerm("rating", version.getRating(), writer);
 			defineTerm("rating text", version.getRatingText(), writer);
 			
 			beginNestedDefinitionList("manifested as", writer);
 			
 				for (Encoding encoding : version.getManifestedAs()) {
 					writeHtmlFor(encoding, writer);
 				}
 	
 			endNestedDefinitionList(writer);
 
 		endNestedDefinitionList(writer);
 	}
 	
 	private static void writeHtmlFor(Encoding encoding, Writer writer) throws IOException {
 
 		beginNestedDefinitionList("Encoding", writer);
 		
 			defineTerm("advertising duration", encoding.getAdvertisingDuration(), writer);
 			defineTerm("audio bit rate", encoding.getAudioBitRate(), writer);
 			defineTerm("audio channels", encoding.getAudioChannels(), writer);
 			defineTerm("audio coding", encoding.getAudioCoding(), writer);
 			defineTerm("bit rate", encoding.getBitRate(), writer);
 			defineTerm("contains advertising", encoding.getContainsAdvertising(), writer);
 			defineTerm("data container format", encoding.getDataContainerFormat(), writer);
			defineTerm("data size", encoding.getDataContainerFormat(), writer);
 			defineTerm("data distributor", encoding.getDistributor(), writer);
 			defineTerm("has digital onscreen graphic (DOG)", encoding.getHasDOG(), writer);
 			defineTerm("source", encoding.getSource(), writer);
 			defineTerm("video aspect ration", encoding.getVideoAspectRatio(), writer);
 			defineTerm("video bit rate", encoding.getVideoBitRate(), writer);
 			defineTerm("video coding", encoding.getVideoCoding(), writer);
 			defineTerm("video framerate", encoding.getVideoFrameRate(), writer);
 			defineTerm("video horizontal size", encoding.getVideoHorizontalSize(), writer);
 			defineTerm("video progressive scan", encoding.getVideoProgressiveScan(), writer);
 			defineTerm("video vertical size", encoding.getVideoVerticalSize(), writer);
 	
 			beginNestedDefinitionList("available at", writer);
 			
 				for (Location location : encoding.getAvailableAt()) {
 					writeHtmlFor(location, writer);
 				}
 	
 			endNestedDefinitionList(writer);
 
 		endNestedDefinitionList(writer);
 	}
 
 	private static void beginNestedDefinitionList(String title, Writer writer) throws IOException {
 
 		writer.write("<dt>");
 		writer.write(title);
 		writer.write("</dt>");
 		writer.write("<dd><dl class=\"" + title.toLowerCase() + "\">");
 	}
 	
 	private static void endNestedDefinitionList(Writer writer) throws IOException {
 
 		writer.write("</dl>");
 		writer.write("</dd>");
 		writer.write(NEW_LINE);
 	}
 
 	private static void writeHtmlFor(Location location, Writer writer) throws IOException {
 		
 		beginNestedDefinitionList("Location", writer);
 		
 			defineTerm("available", location.getAvailable(), writer);
 			defineTerm("availability start", location.getAvailabilityStart(), writer);
 			defineTerm("DRM playable from", location.getDrmPlayableFrom(), writer);
 			defineTerm("restricted by", location.getRestrictedBy(), writer);
 			defineTerm("transport is live", location.getTransportIsLive(), writer);
 			defineTerm("transport type", location.getTransportType(), writer);
 			defineTerm("transport sub type", location.getTransportSubType(), writer);
 			defineTerm("URI", externalLink(location.getUri()), writer);
 			defineTerm("embed code", htmlEscape(location.getEmbedCode()), pre(location.getEmbedCode()), writer);
 
 		endNestedDefinitionList(writer);
 	}
 
 	private static String pre(String embedCode) {
 		if (embedCode == null) { return null; }
 		return "<pre>" + embedCode + "</pre>";
 	}
 
 	private static String listOf(Set<String> elements) {
 		
 		if (elements == null || elements.isEmpty()) { return null; }
 		
 		StringBuffer buf = new StringBuffer();
 		buf.append("<ul>");
 		for (String element : elements) {
 			buf.append("<li>");
 			buf.append(element);
 			buf.append("</li>");
 		}
 		buf.append("</ul>");
 		return buf.toString();
 	}
 
 }
