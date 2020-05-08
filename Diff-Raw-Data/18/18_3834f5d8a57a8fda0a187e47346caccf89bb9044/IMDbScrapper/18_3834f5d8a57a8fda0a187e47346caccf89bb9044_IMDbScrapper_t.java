 /*
  * movie-renamer-core
  * Copyright (C) 2012 Nicolas Magré
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.scrapper.impl.movie;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import fr.free.movierenamer.info.CastingInfo;
 import fr.free.movierenamer.info.CastingInfo.PersonProperty;
 import fr.free.movierenamer.info.IdInfo;
 import fr.free.movierenamer.info.ImageInfo;
 import fr.free.movierenamer.info.ImageInfo.ImageCategoryProperty;
 import fr.free.movierenamer.info.ImageInfo.ImageProperty;
 import fr.free.movierenamer.info.MovieInfo;
 import fr.free.movierenamer.info.MovieInfo.MovieProperty;
 import fr.free.movierenamer.scrapper.MovieScrapper;
 import fr.free.movierenamer.searchinfo.Movie;
 import fr.free.movierenamer.utils.LocaleUtils;
 import fr.free.movierenamer.utils.LocaleUtils.AvailableLanguages;
 import fr.free.movierenamer.utils.ScrapperUtils;
 import fr.free.movierenamer.utils.StringUtils;
 import fr.free.movierenamer.utils.URIRequest;
 import fr.free.movierenamer.utils.XPathUtils;
 import org.w3c.dom.NodeList;
 
 /**
  * Class IMDbScrapper : search movie on IMDB
  *
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public class IMDbScrapper extends MovieScrapper {
 
   private static final String host = "www.imdb.com";
   private static final String name = "IMDb";
   private static final String CHARSET = URIRequest.ISO;
 
   public IMDbScrapper() {
     super(AvailableLanguages.values());
   }
 
   @Override
   public String getName() {
     return name;
   }
 
   @Override
   protected String getHost() {
     return host;
   }
 
   private String createImgPath(String imgPath) {
     return imgPath.replaceAll("S[XY]\\d+(.)+\\.jpg", "SY70_SX100.jpg");
   }
 
   private URIRequest.RequestProperty getRequestProperties(Locale language) {
     return new URIRequest.RequestProperty("Accept-Language", String.format("%s-%s", language.getLanguage(), language.getCountry()));
   }
 
   @Override
   protected List<Movie> searchMedia(String query, Locale language) throws Exception {
     // http://www.imdb.com/find?s=tt&ref_=fn_tt&q=
     // Only title -> ref_=fn_tt
     // Only movie -> ref_=fn_ft
     URL searchUrl = new URL("http", host, "/find?s=tt&ref_=fn_tt&q=" + URIRequest.encode(query));
     return searchMedia(searchUrl, language);
   }
 
   @Override
   protected List<Movie> searchMedia(URL searchUrl, Locale language) throws Exception {
     Document dom = URIRequest.getHtmlDocument(searchUrl.toURI(), getRequestProperties(language));
 
     // select movie results
     List<Node> nodes = XPathUtils.selectNodes("//TABLE[@class='findList']//TR", dom);
     List<Movie> results = new ArrayList<Movie>(nodes.size());
 
     for (Node node : nodes) {
       try {
         Node retNode = XPathUtils.selectNode("TD[@class='result_text']", node);
         String title = XPathUtils.selectNode("A", retNode).getTextContent().trim();
         Matcher m = Pattern.compile("\\((\\d{4}).*\\)").matcher(retNode.getTextContent());
         String year;
         if (m.find()) {
           year = m.group(1);
         } else {
           year = "-1";
         }
         // String year = retNode.getTextContent().replaceAll(title, "").replaceAll("\\D", "").replaceAll("[\\p{Punct}\\p{Space}]+", ""); // remove non-number characters //FIXME à refaire !
         String href = XPathUtils.getAttribute("href", XPathUtils.selectNode("A", retNode));
         int imdbid = findImdbId(href);
         URL thumb;
         try {
           String imgPath = XPathUtils.getAttribute("src", XPathUtils.selectNode("TD[@class='primary_photo']/A/IMG", node));
          if (!imgPath.contains("nopicture")) {
             thumb = new URL(createImgPath(imgPath));
           } else {
             thumb = null;
           }
         } catch (Exception ex) {
           thumb = null;
         }
 
         results.add(new Movie(new IdInfo(imdbid, ScrapperUtils.AvailableApiIds.IMDB), title, null, thumb, Integer.parseInt(year)));
       } catch (Exception e) {
         // ignore
       }
     }
 
     // we might have been redirected to the movie page
     if (results.isEmpty()) {
       try {
         int imdbid = findImdbId(XPathUtils.selectString("//LINK[@rel='canonical']/@href", dom));
         MovieInfo info = fetchMediaInfo(new Movie(new IdInfo(imdbid, ScrapperUtils.AvailableApiIds.IMDB), null, null, null, -1), language);
         URL thumb;
         try {
           String imgPath = info.getPosterPath().toURL().toExternalForm();
           thumb = new URL(createImgPath(imgPath));
         } catch (Exception ex) {
           thumb = null;
         }
         Movie movie = new Movie(new IdInfo(imdbid, ScrapperUtils.AvailableApiIds.IMDB), info.getTitle(), null, thumb, info.getYear());
         if (movie != null) {
           results.add(movie);
         }
       } catch (Exception e) {
         // ignore, can't find movie
       }
     }
 
     return results;
   }
 
   @Override
   protected MovieInfo fetchMediaInfo(Movie movie, Locale language) throws Exception {
     URL searchUrl = new URL("http", host, String.format("/title/%s/combined", movie.getMediaId()));
     Document dom = URIRequest.getHtmlDocument(searchUrl.toURI(), getRequestProperties(language));
 
     Map<MovieProperty, String> fields = new EnumMap<MovieProperty, String>(MovieProperty.class);
     List<String> genres = new ArrayList<String>();
     List<Locale> countries = new ArrayList<Locale>();
     List<String> studios = new ArrayList<String>();
 
     Node node = XPathUtils.selectNode("//H1", dom);
     fields.put(MovieProperty.title, StringUtils.unEscapeXML(XPathUtils.selectString("text()", node), CHARSET));
 
     String year = XPathUtils.selectString("//SPAN/A[contains(@href,'year')]", node);
     if (year != null) {
       Pattern pattern = Pattern.compile("\\d{4}");
       Matcher matcher = pattern.matcher(year);
       if (matcher.find()) {
         fields.put(MovieProperty.releasedDate, year);
       }
     }
 
     String originalTitle = XPathUtils.selectString("//SPAN[@class='title-extra']/I[contains(., '(original title)')]/preceding-sibling::text()", node);
     if (originalTitle != null) {
       fields.put(MovieProperty.originalTitle, StringUtils.unEscapeXML(originalTitle, CHARSET));
     }
 
     String rating = XPathUtils.selectString("//DIV[@class='starbar-meta']/B", dom);
     if (rating.contains("/")) {
       String[] rateVal = rating.split("\\/");
       Float rate = Float.parseFloat(rateVal[0]);
       fields.put(MovieProperty.rating, String.valueOf(rate / 2));
     }
 
     String votes = XPathUtils.selectString("//DIV[@class='starbar-meta']/A[@href='ratings']", dom);
     if (votes.contains(" votes")) {
       fields.put(MovieProperty.votes, votes.replaceAll(" .*", ""));
     }
 
     String runtime = XPathUtils.selectString("//DIV[@class='info']/H5[contains(., 'Runtime')]/following-sibling", dom);
     if (!runtime.equals("")) {
       Pattern pattern = Pattern.compile(".*(\\d{3}) min.*");
       Matcher matcher = pattern.matcher(runtime);
       if (matcher.find()) {
         fields.put(MovieProperty.runtime, matcher.group(1));
       }
     }
 
    String overview = XPathUtils.selectString("//DIV[@class='info']/H5[contains(., 'Plot')]/parent::node()//DIV/text()", dom);
    if (!overview.equals("")) {
      if(overview.endsWith("|")) {
        overview = overview.substring(0, overview.length() - 2).trim();
      }
      fields.put(MovieProperty.overview, StringUtils.unEscapeXML(overview, CHARSET));
    }

     List<Node> ngenres = XPathUtils.selectNodes("//DIV[@class='info']//A[contains(@href, 'Genres')]", dom);
     for (Node genre : ngenres) {
       genres.add(StringUtils.unEscapeXML(XPathUtils.selectString("text()", genre), CHARSET));
     }
 
     List<Node> ncountries = XPathUtils.selectNodes("//DIV[@class='info']//A[contains(@href, 'country')]", dom);
     for (Node country : ncountries) {
       countries.add(LocaleUtils.findCountry(StringUtils.unEscapeXML(XPathUtils.selectString("text()", country), CHARSET), language));
     }
 
     Node nstudios = XPathUtils.selectNode("//DIV[@id='tn15content']//B[@class='blackcatheader' and contains(., 'Production Companies')]", dom);
     if (nstudios != null) {
       NodeList nl = nstudios.getNextSibling().getChildNodes();
       for (int i = 0; i < nl.getLength(); i++) {
         String studio = nl.item(i).getTextContent();
         studios.add(StringUtils.removeBrackets(StringUtils.unEscapeXML(studio, CHARSET)).trim());
       }
     }
 
     try {
       String posterPath = XPathUtils.getAttribute("src", XPathUtils.selectNode("//DIV[@class='photo']/A[@name='poster']/IMG", dom));
       if (!posterPath.equals("")) {
         fields.put(MovieProperty.posterPath, createImgPath(posterPath));
       }
     } catch (Exception ex) {
       // No thumb
     }
 
     Node plot = XPathUtils.selectNode(String.format("//A[@href='/title/%s/plotsummary']", movie.getMediaId()), dom);
     if (plot != null) {
       searchUrl = new URL("http", host, String.format("/title/%s/plotsummary", movie.getMediaId()));
       dom = URIRequest.getHtmlDocument(searchUrl.toURI(), getRequestProperties(language));
      List<Node> nodes = XPathUtils.selectNodes("//P[@class='plotpar'][1]/descendant::text()[not(ancestor::I) and . != '\n']", dom);
      overview = "";
      for (Node pnode : nodes) {
        overview += pnode.getTextContent().trim();
      }
       fields.put(MovieProperty.overview, StringUtils.unEscapeXML(overview, CHARSET));
     }
 
     for (String str : fields.values()) {
       System.out.println(str);
     }
 
     /*
      // Thumb
      searchMatcher = ImdbInfoPattern.THUMB.getPattern().matcher(moviePage);
      if (searchMatcher.find()) {
      String imdbThumb = searchMatcher.group(1);
      fields.put(MovieProperty.posterPath, imdbThumb);
      }
      */
 
     List<IdInfo> ids = new ArrayList<IdInfo>();
     ids.add(movie.getId());
 
     MovieInfo movieInfo = new MovieInfo(fields, ids, genres, countries, studios);
     return movieInfo;
   }
 
   protected int findImdbId(String source) {
     Matcher matcher = Pattern.compile("tt(\\d+{7})").matcher(source);
 
     if (matcher.find()) {
       return Integer.parseInt(matcher.group(1));
     }
 
     // not found
     // throw new IllegalArgumentException(String.format("Cannot find imdb id: %s", source));
     return 0;
   }
 
   protected int findCastImdbId(String source) {
     Matcher matcher = Pattern.compile("nm(\\d+{7})").matcher(source);
 
     if (matcher.find()) {
       return Integer.parseInt(matcher.group(1));
     }
 
     // not found
     // throw new IllegalArgumentException(String.format("Cannot find cast imdb id: %s", source));
     return 0;
   }
 
   @Override
   protected List<ImageInfo> getScrapperImages(Movie movie, Locale language) throws Exception {
     URL searchUrl = new URL("http", host, String.format("/title/%s/mediaindex", movie.getMediaId()));
     String imagesPage = URIRequest.getDocumentContent(searchUrl.toURI(), getRequestProperties(language));
 
     List<ImageInfo> images = new ArrayList<ImageInfo>();
 
     Matcher searchMatcher = Pattern.compile("src=\"(http://ia.media-imdb.com/images/.*)\"").matcher(imagesPage);
     while (searchMatcher.find()) {
       String url = searchMatcher.group(1);
       Map<ImageProperty, String> imageFields = new EnumMap<ImageProperty, String>(ImageProperty.class);
       imageFields.put(ImageProperty.url, url.replaceAll("CR[\\d,]+_SS\\d+", "SY214_SX314"));
       images.add(new ImageInfo(imageFields, ImageCategoryProperty.unknown));
     }
     return images;
   }
 
   @Override
   protected List<CastingInfo> fetchCastingInfo(Movie movie, Locale language) throws Exception {
     URL searchUrl = new URL("http", host, String.format("/title/%s/fullcredits", movie.getMediaId()));
     Document dom = URIRequest.getHtmlDocument(searchUrl.toURI(), getRequestProperties(language));
 
     List<CastingInfo> casting = new ArrayList<CastingInfo>();
 
     List<Node> castNodes = XPathUtils.selectNodes("//TABLE[@class='cast']//TR", dom);
     for (Node node : castNodes) {
       Node actorNode = XPathUtils.selectNode("TD[@class='nm']", node);
       if (actorNode != null) {
         Node pictureNode = XPathUtils.selectNode("TD[@class='hs']//IMG", node);
         Node characterNode = XPathUtils.selectNode("TD[@class='char']", node);
 
         Map<PersonProperty, String> personFields = fetchPersonIdAndName(actorNode);
         if (personFields != null) {
           String picture = (pictureNode != null) ? createImgPath(XPathUtils.getAttribute("src", pictureNode)) : "";
           if (picture.contains("no_photo")) {
             picture = "";
           }
           personFields.put(PersonProperty.job, CastingInfo.ACTOR);
           personFields.put(PersonProperty.character, (characterNode != null) ? StringUtils.unEscapeXML(characterNode.getTextContent().trim(), CHARSET) : "");
           personFields.put(PersonProperty.picturePath, picture);
           casting.add(new CastingInfo(personFields));
         }
       }
     }
 
     List<Node> directorNodes = XPathUtils.selectNodes("//TABLE[.//A[@name='directors']]//TR", dom);
     for (Node node : directorNodes) {
       Map<PersonProperty, String> personFields = fetchPersonIdAndName(node);
       if (personFields != null) {
         personFields.put(PersonProperty.job, CastingInfo.DIRECTOR);
         casting.add(new CastingInfo(personFields));
       }
     }
 
     List<Node> writerNodes = XPathUtils.selectNodes("//TABLE[.//A[@name='writers']]//TR", dom);
     for (Node node : writerNodes) {
       Map<PersonProperty, String> personFields = fetchPersonIdAndName(node);
       if (personFields != null) {
         personFields.put(PersonProperty.job, CastingInfo.WRITER);
         casting.add(new CastingInfo(personFields));
       }
     }
 
     return casting;
   }
 
   private Map<PersonProperty, String> fetchPersonIdAndName(Node node) {
     if (node != null) {
       Node link = XPathUtils.selectNode(".//A", node);
       if (link != null) {
         String pname = StringUtils.unEscapeXML(link.getTextContent().trim(), CHARSET);
         if (pname.length() > 1) {
           int imdbId = findCastImdbId(XPathUtils.getAttribute("href", link));
           if (imdbId != 0) {
             Map<PersonProperty, String> personFields = new EnumMap<PersonProperty, String>(PersonProperty.class);
             personFields.put(PersonProperty.id, Integer.toString(imdbId));
             personFields.put(PersonProperty.name, pname);
 
             return personFields;
           }
         }
       }
     }
     return null;
   }
 }
