 package ru.spbau.textminer.text;
 
 import org.w3c.dom.Element;
 import ru.spbau.textminer.text.feature.Features;
 import ru.spbau.textminer.text.feature.Link;
 
 import java.util.*;
 
 public class Word {
     public Word(Element wElement, Sentence sentence) throws InvalidWordException {
         this.sentence = sentence;
         children = new ArrayList<LinkWordPair>();
 
         wordId = readWordId(wElement);
         xmlParentId = readParentId(wElement);
         lemma = readLemma(wElement);
         text = wElement.getTextContent().trim().replace(' ', '_');
 
         features = readFeatures(wElement);
         if (features.getPOS() == null) {
             throw new InvalidWordException("No POS specified (word ID: " + this.wordId + ")");
         }
 
         link = WordUtil.getLink(wElement.getAttribute(LINK_ATTR));
         if ((link == null) && !isRoot()) {
             throw new InvalidWordException("Invalid link: '" + wElement.getAttribute(LINK_ATTR) +
                     "' (word ID: " + this.wordId + ")");
         }
     }
 
     private int readWordId(Element wElement) throws InvalidWordException {
         try {
             return Integer.parseInt(wElement.getAttribute(ID_ATTR));
         } catch (NumberFormatException e) {
             throw new InvalidWordException("Invalid word ID: '" + wElement.getAttribute(ID_ATTR) +
                     "' (word: '" + wElement.getTextContent() + "')");
         }
     }
 
     private int readParentId(Element wElement) throws InvalidWordException {
         isRoot = false;
         try {
             String domAttr = wElement.getAttribute(DOM_ATTR).trim();
             if (domAttr.equals(DOM_ROOT)) {
                 isRoot = true;
                 return ROOT_PARENT_ID;
             } else {
                 return Integer.parseInt(domAttr);
             }
         } catch (NumberFormatException e) {
             throw new InvalidWordException("Invalid DOM attribute: '" + wElement.getAttribute(DOM_ATTR) +
                     "' (word ID: " + this.wordId + ")", e);
         }
     }
 
     private String readLemma(Element wElement) throws InvalidWordException {
        String lemma = wElement.getAttribute(LEMMA_ATTR).trim();
         if (lemma.length() == 0) {
             throw new InvalidWordException("No word's lemma specified (word ID: " + this.wordId + ")");
         }
         return lemma;
     }
 
     private Features readFeatures(Element wElement) throws InvalidWordException {
         try {
             return new Features(wElement.getAttribute(FEAT_ATTR));
         } catch (InvalidFeatureException e) {
             throw new InvalidWordException("Invalid word's features: '" +
                     wElement.getAttribute(FEAT_ATTR) + "' (word ID: " + this.wordId + ")", e);
         }
     }
 
     public List<Word> findByLink(Link... linkArr) {
         List<Link> linkList = Arrays.asList(linkArr);
         List<Word> result = new ArrayList<Word>();
         for (LinkWordPair pair : children) {
             if (linkList.contains(pair.getLink())) {
                 result.add(pair.getWord());
             }
         }
         return result;
     }
 
     public void buildTree(List<Word> words) throws InvalidSentenceException {
         buildTree(null, words, new HashSet<Word>());
     }
 
     private void buildTree(Word parent, List<Word> words, Set<Word> visited) throws InvalidSentenceException {
         if (visited.contains(this)) {
             throw new InvalidSentenceException("Invalid tree structure (sentence ID: " +
                     sentence.getId() + ", " + sentence.getFilePath());
         }
 
         visited.add(this);
         setParent(parent);
         for (Word word : words) {
             if (word.xmlParentId == this.getId() && !word.isRoot()) {
                 addChild(word.getLink(), word);
                 word.buildTree(this, words, visited);
             }
         }
     }
 
     public void addChild(Link link, Word child) {
         children.add(new LinkWordPair(link, child));
     }
 
     public Sentence getSentence() {
         return sentence;
     }
     public List<LinkWordPair> getChildren() {
         return Collections.unmodifiableList(children);
     }
 
     public void setParent(Word parent) {
         this.parent = parent;
     }
 
     public Word getParent() {
         return parent;
     }
 
     public int getId() {
         return wordId;
     }
 
     public String getLemma() {
         return lemma;
     }
 
     public String getText() {
         if (text.length() != 0) {
             return text;
         } else {
             return "[" + lemma + "]";
         }
     }
 
     public Link getLink() {
         return link;
     }
 
     public Features getFeatures() {
         return features;
     }
 
     public boolean isRoot() {
         return isRoot;
     }
 
     private Sentence sentence;
     private int xmlParentId;
     private int wordId;
     private String lemma;
     private String text;
     private Link link;
     private Features features;
     private boolean isRoot;
 
     private Word parent;
     private List<LinkWordPair> children;
 
     private static final int ROOT_PARENT_ID = 0;
     private static final String ID_ATTR = "ID";
     private static final String DOM_ATTR = "DOM";
     private static final String DOM_ROOT = "_root";
     private static final String FEAT_ATTR = "FEAT";
     private static final String LEMMA_ATTR = "LEMMA";
     private static final String LINK_ATTR = "LINK";
 }
