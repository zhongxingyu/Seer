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
 package fr.free.movierenamer.namematcher;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import fr.free.movierenamer.utils.NumberUtils;
 
 
 /**
  * Class TvShowEpisodeNumMatcher
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public class TvShowEpisodeNumMatcher {
   public static final Pattern seasonPattern = Pattern.compile("(?:(?:season)|(?:saison)|(?:s))\\W?([0-9]{1,2})");
   public static final Pattern episodePattern = Pattern.compile("(?:(?:(?:[eé]p)|(?:[eé]pisode)) ([0-9]{1,2}))|(?:(?:^| )([0-9]{1,2})[ -_])");
 
   public enum TvShowNumPattern {
 
     SxEPattern("([0-9]{1,2})x([0-9]{1,2})(?:\\D|$)"),
     SxEPattern2("s([0-9]{1,2}).?[eé]([0-9]{1,2})"),
     SxEPattern3("(?:^|[\\W} ])([0-9]{1,2})([0-9][0-9])[\\._ \\-]"),
     SxEPattern4("(?:(?:season)|(?:saison)).?([0-9]{1,2}).*[eé]p.?([0-9]{1,2})"),
     SxEPattern5("(?:(?:season)|(?:saison)).?([0-9]{1,2}).*(?:[eé]pisode).?([0-9]{1,2})"),
     SxEPattern6("s([0-9]{1,2}).*[ée]pisode.?\\D?([0-9]{1,2})"),
    SxEPattern7("([0-9]{2}) ([0-9]{2})(?:\\D|$)");
     private Pattern pattern;
 
     private TvShowNumPattern(String pattern) {
       this.pattern = Pattern.compile(pattern);
     }
 
     public Pattern getPattern() {
       return pattern;
     }
   }
   private String episodeName;
   private String parentFolder;
 
   public TvShowEpisodeNumMatcher(File episodeFile) {
     this.episodeName = normalize(episodeFile.getName());
     this.parentFolder = (episodeFile.getParent()!=null)?episodeFile.getParent().toLowerCase():null;
     String episodeName = episodeFile.getParent() + File.separator + episodeFile.getName();
     if (episodeName.contains(File.separator)) {
       parentFolder = episodeName.substring(0, episodeName.lastIndexOf(File.separator)).toLowerCase();
       this.episodeName = normalize(episodeName.substring(episodeName.lastIndexOf(File.separator) + 1));
     } else {
       this.episodeName = normalize(episodeName);
     }
   }
 
   /**
    * Retreive season and episode
    *
    * @return SxE
    */
   public SxE matchEpisode() {
     return matchAll();
   }
 
   /**
    * Try to get the most probable match between all matches result
    *
    * @return SxE
    */
   private SxE matchAll() {
     System.out.println("File : " + episodeName);
     SxE sxe;
     List<SxE> SxEs = new ArrayList<SxE>();
     for (TvShowEpisodeNumMatcher.TvShowNumPattern patternToTest : TvShowEpisodeNumMatcher.TvShowNumPattern.values()) {
       if ((sxe = match(patternToTest)) != null) {
         SxEs.add(sxe);
         System.out.println("  Matcher " + patternToTest.name() + " Match : " + sxe);
       }
     }
 
     if (SxEs.isEmpty()) {
       System.out.println("  No Match Found, Try To match Separately");
       sxe = new SxE();
       Matcher matcher = seasonPattern.matcher(parentFolder == null ? episodeName : parentFolder);
       if (matcher.find()) {
         String season = matcher.group(1);
         sxe.setSeason(NumberUtils.isDigit(season) ? Integer.parseInt(season) : 1);
       }
 
       matcher = episodePattern.matcher(episodeName);
       if (matcher.find()) {
         String episode = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
         sxe.setEpisode(NumberUtils.isDigit(episode) ? Integer.parseInt(episode) : 1);
       }
       
       if(!sxe.isValid()){
         return new SxE(1,1);
       }
       
       if (sxe.isPartial()) {
         if (sxe.getSeason() < 0) {
           sxe.setSeason(1);
         }
         if (sxe.getEpisode() < 0) {
           sxe.setEpisode(1);
         }
       }
       return sxe;
     }
 
     List<SxE> completeMatch = new ArrayList<SxE>();
     List<SxE> partialMatch = new ArrayList<SxE>();
 
     // Separe complete match and partial match (partial match will be empty in almost all cases)
     for (SxE match : SxEs) {
       if (match.isValid()) {
         completeMatch.add(match);
       } else if (match.isPartial()) {
         partialMatch.add(match);
       }
     }
 
     // If no complete match, try to make a complete match with partial match
     if (completeMatch.isEmpty() && partialMatch.size() > 1) {
       SxE match = new SxE();
       for (SxE mSxE : partialMatch) {
         if (match.getEpisode() == -1 && mSxE.getEpisode() != -1) {
           match.setEpisode(mSxE.getEpisode());
         }
         if (match.getSeason() == -1 && mSxE.getSeason() != -1) {
           match.setSeason(mSxE.getSeason());
         }
         if (match.isValid()) {
           break;
         }
       }
       return match;
     }
 
     if (completeMatch.size() == 1) {
       return completeMatch.get(0);
     }
 
     // Try to get the most probable match
     if (completeMatch.size() > 1) {
       SxE fMatch = completeMatch.get(0);
       boolean different = false;
       for (SxE match : completeMatch) {
         if (!fMatch.equals(match)) {
           different = true;
         }
       }
       if (!different) {
         return fMatch;
       }
       return getSxE(completeMatch);
     }
 
     //No match found
     if (SxEs.isEmpty() && partialMatch.isEmpty()) {
       return new SxE(1, 1);
     }
 
     return partialMatch.isEmpty() ? SxEs.get(0) : partialMatch.get(0);
   }
 
   /**
    * Try to match season and episode in fileName
    *
    * @param EPpattern Season/Episode pattern
    * @return SxE
    */
   private SxE match(TvShowNumPattern EPpattern) {
     Matcher matcher = EPpattern.getPattern().matcher(episodeName);
     if (matcher.find()) {
       String season = matcher.group(1);
       String episode = matcher.group(2);
       String match = matcher.group(1) + matcher.group(2);
 
       int S, E;
       S = NumberUtils.isDigit(season) ? Integer.parseInt(season) : -1;
       E = NumberUtils.isDigit(episode) ? Integer.parseInt(episode) : -1;
 
       if (E == 0 && NumberUtils.isDigit(season)) {// Absolute number ?
         S = Integer.parseInt(season + episode);
         E = 0;
       }
 
       if (S != -1 || E != -1) {
         return new SxE(S, E, match);
       }
     }
     return null;
   }
 
   /**
    * Get the most probable season and episode by occurrence number
    *
    * @param SxEs List of SxE
    * @return SxE
    */
   private SxE getSxE(List<SxE> SxEs) {
     SxE sxe = new SxE();
     Map<Integer, Integer> seasonMatch = new LinkedHashMap<Integer, Integer>();
     Map<Integer, Integer> episodeMatch = new LinkedHashMap<Integer, Integer>();
     for (SxE tmp : SxEs) {
       if (tmp.getSeason() != -1) {
         if (seasonMatch.containsKey(tmp.getSeason())) {
           int count = seasonMatch.get(tmp.getSeason()).intValue();
           seasonMatch.remove(tmp.getSeason());
           seasonMatch.put(tmp.getSeason(), count++);
         } else {
           seasonMatch.put(tmp.getSeason(), 1);
         }
       }
 
       if (tmp.getEpisode() != -1) {
         if (episodeMatch.containsKey(tmp.getEpisode())) {
           int count = episodeMatch.get(tmp.getEpisode()).intValue();
           episodeMatch.remove(tmp.getEpisode());
           episodeMatch.put(tmp.getEpisode(), count++);
         } else {
           episodeMatch.put(tmp.getEpisode(), 1);
         }
       }
 
       sxe.setSeason(getMostProbableNumber(seasonMatch));
       sxe.setEpisode(getMostProbableNumber(episodeMatch));
     }
     return sxe;
   }
 
   /**
    * Get the most encountered value in Map
    *
    * @param map Map
    * @return Key or -1
    */
   private int getMostProbableNumber(Map<Integer, Integer> map) {
     if (map.isEmpty()) {
       return -1;
     }
     return getKeyByValue(map, Collections.max(map.values()));
   }
 
   /**
    * Get key by value
    *
    * @param map Map
    * @param value Value to reteive key
    * @return Key or null
    */
   public static Integer getKeyByValue(Map<Integer, Integer> map, Integer value) {
     for (Entry<Integer, Integer> entry : map.entrySet()) {
       if (value.equals(entry.getValue())) {
         return entry.getKey();
       }
     }
     return null;
   }
 
   /**
    * Normalize tvShow fileName
    *
    * @param str
    * @return String normalized
    */
   private String normalize(String str) {
     str = str.substring(0, str.lastIndexOf("."));// Remove extension
     str = str.replace(".", " ").replace("_", " ").replace("-", " ").trim();
     str = str.replaceAll("[,;:!]", "");// Remove ponctuation
     str = str.replaceAll("\\s+", " ");// Remove duplicate space character
     return str.toLowerCase();
   }
 
 }
