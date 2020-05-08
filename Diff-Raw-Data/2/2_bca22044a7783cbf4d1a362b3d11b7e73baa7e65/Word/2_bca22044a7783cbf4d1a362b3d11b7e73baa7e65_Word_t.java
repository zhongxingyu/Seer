 import java.util.ArrayList;
 import java.util.Collections;
 
 public class Word {
     String word;
     ArrayList<Link> links = new ArrayList<Link>();
     final String eos = "^&eos&^";
 
     public Word(String word) {
         this.word = this.trimWord(word);
     }
 
     public void addLink(Word otherWord) {
         Integer linkPosition = getIndexOfLinkOrNull(otherWord);
         if (linkPosition != null) {
             this.links.get(linkPosition).plusRate();
         } else {
             this.links.add(new Link(otherWord));
         }
         Collections.sort(this.links, new LinksComparator());
     }
 
     private String trimWord(String word) {
         return word.toLowerCase().replaceFirst("(\\.|!|;|\\-|:|\\?|\\s|,)", "");
     }
 
     public ArrayList<Link> getLinks() {
         return links;
     }
 
     public Word getSingleLink(int index) {
         if (links.isEmpty()) {
             return null;
         } else {
             return links.get(index).otherWord;
         }
     }
 
     public Link getNearestEOSLink() {
         if (links.isEmpty()) {
             return null;
         } else {
             for (Link next : links) {
                 if (next.otherWord.toString().equals(eos)) {
                     return next;
                 }
             }
         }
         return null;
     }
 
     public Integer getEOSIndexOrNull() {
         if (links.isEmpty()) {
             return null;
         } else {
             int count = 0;
             for (Link link : links) {
                 if (link.otherWord.toString().equals(eos)) {
                     return count;
                 }
                 count++;
             }
         }
         return null;
     }
 
     public Integer getIndexOfLinkOrNull(Word link) {
         int count = 0;
         Link otherLink = new Link(link);
         for (Link next : links) {
             if (next.equals(otherLink)) {
                 return count;
             }
             count++;
         }
         return null;
     }
 
     public Word getBestLink() {
         Word returnWord;
         if (getSingleLink(0) == null) {
             return null;
        } else if ((getSingleLink(0).toString().equals("^&eos&^")) && links.size() > 1) {
             returnWord = getSingleLink(1);
             links.get(1).downRate();
             Collections.sort(this.links, new LinksComparator());
         } else if (getSingleLink(0).toString().equals("^&eos&^")) {
             return null;
         } else {
             returnWord = getSingleLink(0);
             links.get(0).downRate();
             Collections.sort(this.links, new LinksComparator());
         }
         return returnWord;
     }
 
     public boolean hasEOSLink() {
         for (Link link : links) {
             Word x = link.otherWord;
             if (x.equals(new Word(eos))) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Word word1 = (Word) o;
         return !(word != null ? !word.equals(word1.word) : word1.word != null);
     }
 
     @Override
     public int hashCode() {
         int result = word != null ? word.hashCode() : 0;
         result = 31 * result + (links != null ? links.hashCode() : 0);
         result = 31 * result + (eos.hashCode());
         return result;
     }
 
     //override so we can compare vs "eos" via .equals()
     @Override
     public String toString() {
         return this.word;
     }
 }
