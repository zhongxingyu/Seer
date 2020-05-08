 package org.romppu.translit.document.impl;
 
 import org.romppu.translit.TranslitDocumentException;
 import org.romppu.translit.dictionary.TranslitDictionary;
 import org.romppu.translit.document.TranslitDocument;
 
 import java.text.MessageFormat;
 import java.util.*;
 
 /**
  * Default implementation of {@link TranslitDocument}
  * The DefaultTranslitDocument represents a content. Main goal of this class is a parsing of
  * a specified text with a specified dictionary and its conversion to an internal format of the DefaultTranslitDocument.
  * After parsing, the content may be retrieved from the DefaultTranslitDocument by the {@link #getString(org.romppu.translit.dictionary.TranslitDictionary.Side)} method.
  */
 public class DefaultTranslitDocument extends TranslitDocument {
 
     private static final String ERR_INVALID_DATA_RANGE = "Invalid data range ({0},{1}). Collection rowCount: {2}";
     private static final String ERR_INVALID_DATA_POS = "Invalid position ({0}). Position must be in range [{1}-{2}]";
 
     private TranslitDictionary dictionary;
 
     private Vector<Element> elements = new Vector();
     private MatchSelectionStrategy matchSelectionStrategy;
 
     /**
      * Creates a new instance of DefaultTranslitDocument with the specified {@see dictionary}
      *
      * @param dictionary translit dictionary
      */
     public DefaultTranslitDocument(TranslitDictionary dictionary) {
         this.dictionary = dictionary;
     }
 
     /**
      * Parses the specified text with specified parameters and creates a new instance of the DefaultTranslitDocument.
      * The EagerMatchSelectionStrategy will be used as default selection strategy.
      *
      * @param dict translit dictionary
      * @param text to transliteration
      * @param side text will be transliterated from the specified side into an opposite side
      * @return new instance of DefaultTranslitDocument
      * @throws org.romppu.translit.TranslitDocumentException
      */
     public static DefaultTranslitDocument create(
             TranslitDictionary dict,
             String text,
             TranslitDictionary.Side side)
             throws TranslitDocumentException {
         return create(dict, text, side, null);
     }
 
     /**
      * Parses the specified {@see text} with the specified {@see dictionary} and {@see strategy}
      * and creates a new instance of the DefaultTranslitDocument
      *
      * @param dict     TranslitDictionary
      * @param text     to transliteration
      * @param side     text will be transliterated from the specified side into an opposite side
      * @param strategy match selection strategy
      * @return new instance of DefaultTranslitDocument
      * @throws TranslitDocumentException
      */
     public static DefaultTranslitDocument create(
             TranslitDictionary dict,
             String text,
             TranslitDictionary.Side side,
             MatchSelectionStrategy strategy)
             throws TranslitDocumentException {
         DefaultTranslitDocument doc = new DefaultTranslitDocument(dict);
         doc.setMatchSelectionStrategy(strategy);
         ParsingContext parsingContext = doc.parse(text, side);
         doc.elements.addAll(parsingContext.elements());
         return doc;
     }
 
     /**
      * Sets the {@see matchSelectionStrategy} property value
      *
      * @param matchSelectionStrategy
      */
     private void setMatchSelectionStrategy(MatchSelectionStrategy matchSelectionStrategy) {
         this.matchSelectionStrategy = matchSelectionStrategy;
     }
 
     /**
      * Returns {@see matchSelectionStrategy} property, if value of the property is null then
      * creates a new instance of the EagerMatchSelectionStrategy
      *
      * @return matchSelectionStrategy property.
      */
     public MatchSelectionStrategy getMatchSelectionStrategy() {
         if (matchSelectionStrategy == null)
             matchSelectionStrategy = new EagerMatchSelectionStrategy();
         return matchSelectionStrategy;
     }
 
     /**
      * Returns the element at the specified position
      *
      * @param pos element position
      * @return element
      * @throws TranslitDocumentException
      */
     public Element getElement(int pos) throws TranslitDocumentException {
         validatePosition(pos);
         return elements.get(pos);
     }
 
     /**
      * Returns the content at the specified position in this document
      *
      * @param pos  element position
      * @param side in dictionary (left or right)
      * @return String content of element
      * @throws TranslitDocumentException
      */
     public String getElementData(int pos, TranslitDictionary.Side side) throws TranslitDocumentException {
         validatePosition(pos);
         return getElement(pos).getStringValue(new StringBuildingContext(side));
     }
 
     /**
      * Removes all elements
      */
     @Override
     public void clear() {
         elements.removeAllElements();
     }
 
     /**
      * Returns document content as string
      *
      * @param side text will be transliterated from the specified side into an opposite side
      * @return transliterated text
      * @throws TranslitDocumentException
      */
     @Override
     public String getString(TranslitDictionary.Side side) throws TranslitDocumentException {
         return buildString(0, elements.size(), side);
     }
 
     @Override
     public void setString(String string, TranslitDictionary.Side side) throws TranslitDocumentException {
         clear();
         for (Character ch: string.toCharArray()) {
             String str = ch.toString();
             if (dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.START).equals(str)) {
                 elements.add(new ExclusionMarkerElement(TranslitDictionary.ExclusionMarker.START));
             } if (dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.END).equals(str)) {
                 elements.add(new ExclusionMarkerElement(TranslitDictionary.ExclusionMarker.END));
             } else {
                 int idx = dictionary.indexOf(ch.toString(), side);
                 if (idx != -1) {
                     elements.add(new IndexElement(idx));
                 } else {
                     elements.add(new CharacterElement(str));
                 }
             }
         }
     }
 
     @Override
     public void insertStringAt(int index, String text, TranslitDictionary.Side side) throws TranslitDocumentException {
         for (int i = index; i < index + text.length(); i++) {
            String str = String.valueOf(text.charAt(i - index));
             if (dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.START).equals(str)) {
                 elements.add(i, new ExclusionMarkerElement(TranslitDictionary.ExclusionMarker.START));
             } if (dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.END).equals(str)) {
                 elements.add(i, new ExclusionMarkerElement(TranslitDictionary.ExclusionMarker.END));
             } else {
                 int idx = dictionary.indexOf(str, side);
                 if (idx != -1) {
                     elements.add(i, new IndexElement(idx));
                 } else {
                     elements.add(i, new CharacterElement(str));
                 }
             }
         }
     }
 
     /**
      * Returns document content as string with exclusion markers.
      * @param side text will be transliterated from the specified side into an opposite side
      * @return
      * @throws TranslitDocumentException
      */
     @Override
     public String getMarkedString(TranslitDictionary.Side side) throws TranslitDocumentException {
         return buildString(elements, side, true);
     }
 
     /**
      * Returns elements as string
      * @param elementList to process
      * @param side text will be transliterated from the specified side into an opposite side
      * @return transliterated text
      * @throws TranslitDocumentException
      */
     public String getString(List<Element> elementList, TranslitDictionary.Side side) throws TranslitDocumentException {
         return buildString(elementList, side, false);
     }
 
     /**
      * Inserts the specified {@see text} into the elements list at the specified {@see index}.
      * The {@see text} will be transliterated from the specified {@see side} into an opposite side.
      * The {@see index} means an element index, it is not index in a text content. To retrieve the element index by position in the content use {@link #convertToElementIndex(int, org.romppu.translit.dictionary.TranslitDictionary.Side)} method
      *
      * @param index element index
      * @param text  to insert
      * @param side  text will be transliterated from the specified side into an opposite side
      * @return result of insert operation
      * @throws TranslitDocumentException
      */
     @Override
     public Mutation insertAt(int index, String text, TranslitDictionary.Side side) throws TranslitDocumentException {
         if (index > elements.size() || index < 0) {
             throw new TranslitDocumentException("Invalid index " + index);
         }
         int longestWord = getDictionary().getLongestWordLen(side);
         Mutation mutation = new Mutation();
         mutation.setLeftShift(index);
         if (elements.size() > 0) {
             StringBuildingContext buildingContext = new StringBuildingContext(side);
             while (mutation.getStringBuffer().length() < longestWord
                     && mutation.getLeftShift() > 0
                     && elements.get(mutation.getLeftShift() - 1) instanceof IndexElement) {
                 mutation.setLeftShift(mutation.getLeftShift() - 1);
                 Element element = elements.get(mutation.getLeftShift());
                 mutation.oldElements().add(0, element);
                 mutation.getStringBuffer().insert(0, element.getStringValue(buildingContext));
             }
         }
         removeElements(mutation.getLeftShift(), mutation.oldElements().size());
         mutation.getStringBuffer().append(text);
         ParsingContext parsingContext = parse(mutation.getStringBuffer().toString(), side);
         mutation.newElements().addAll(parsingContext.elements());
         mutation.setOffset(index);
         if (mutation.newElements().size() - 1 < mutation.oldElements().size()) {
             mutation.setOffset(mutation.getOffset() - (mutation.oldElements().size() - (mutation.newElements().size() - 1)));
         }
         elements.addAll(mutation.getLeftShift(), mutation.newElements());
         return mutation;
     }
 
     /**
      * Returns document size
      *
      * @return size of elements in the document
      */
     @Override
     public int getSize() {
         return elements.size();
     }
 
     /**
      * Converts the specified position to the element index with the specified side
      *
      * @param position position in string
      * @param side     LEFT or RIGHT
      * @return element index
      */
     @Override
     public int convertToElementIndex(int position, TranslitDictionary.Side side) {
         int currentPosition = 0;
         if (position == 0) return currentPosition;
         StringBuildingContext stringBuildingContext = new StringBuildingContext(side);
         for (int i = 0; i < elements.size(); i++) {
             Element element = elements.get(i);
             String elementValue = element.getStringValue(stringBuildingContext);
             currentPosition += elementValue.length();
             if (currentPosition >= position) return i;
         }
         return elements.size();
     }
 
     /**
      * Converts the specified element index to the text position with the specified side
      * @param startIndex starts counting from the specified element index
      * @param indexToConvert index of element which will be converted to text position
      * @param side     LEFT or RIGHT
      * @return element index
      */
     @Override
     public int convertToTextPosition(int startIndex, int indexToConvert, TranslitDictionary.Side side) {
         int currentPosition = 0;
         StringBuildingContext stringBuildingContext = new StringBuildingContext(side);
         for (int i = startIndex; i < indexToConvert; i++) {
             Element element = elements.get(i);
             String elementValue = element.getStringValue(stringBuildingContext);
             currentPosition += elementValue.length();
         }
         return currentPosition;
     }
 
 
     /**
      * Removes the specified {@see amount} of elements from the specified {@see position} of the document.
      *
      * @param position removes from
      * @param amount   elements amount
      */
     @Override
     public void removeElements(int position, int amount) {
         Vector<Element> toRemove = new Vector<Element>();
         for (int i = position; i < position + amount; i++) {
             toRemove.add(elements.get(i));
         }
         elements.removeAll(toRemove);
     }
 
     @Override
     public boolean isTranslitAt(int idx) throws TranslitDocumentException {
         return getElement(idx) instanceof IndexElement;
     }
 
     /**
      * Returns the dictionary property
      *
      * @return dictionary
      */
     @Override
     public TranslitDictionary getDictionary() {
         return dictionary;
     }
 
     private String buildString(List<Element> list, TranslitDictionary.Side side, boolean addMarkers) throws TranslitDocumentException {
         StringBuildingContext stringBuildingContext = new StringBuildingContext(side);
         StringBuffer stringBuffer = new StringBuffer();
         boolean startMarker = false;
         for (Iterator<Element> i = list.iterator(); i.hasNext(); ) {
             Element e = i.next();
             String newChar = e.getStringValue(stringBuildingContext);
             if (addMarkers && !newChar.isEmpty() && Character.isAlphabetic(newChar.charAt(0))) {
                 if (e instanceof CharacterElement && !startMarker) {
                     startMarker = true;
                     stringBuffer.append(dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.START));
                 } else if (!(e instanceof CharacterElement) && startMarker) {
                     startMarker = false;
                     stringBuffer.append(dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.END));
                 }
             }
             stringBuffer.append(newChar);
         }
         if (startMarker) {
             stringBuffer.append(dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.END));
         }
         return stringBuffer.toString();
     }
 
     protected String buildString(int start, int end, TranslitDictionary.Side side) throws TranslitDocumentException {
         //validateElementsRange(start, end);
         return buildString(elements.subList(start, end), side, false);
     }
 
     private ParsingContext parse(String text, TranslitDictionary.Side side) {
         ParsingContext context = new ParsingContext(text, side);
         while (context.getPosition() < text.length()) {
             SortedSet matchSet = newSynchronizedSortedSet();
             String part = text.substring(context.getPosition(), text.length());
             for (int i = 0; i < dictionary.getSize(); i++) {
                 String dictionaryValue = dictionary.getValueAt(i, side);
                 if (part.startsWith(dictionaryValue)) {
                     matchSet.add(new Match(i, dictionaryValue));
                 }
             }
             if (!matchSet.isEmpty()) {
                 context.matches().add(matchSet);
                 Match selectedMatch = getMatchSelectionStrategy().selectMatch(context);
                 context.setPosition(context.getPosition() + selectedMatch.length());
                 context.elements().add(new IndexElement(selectedMatch.index()));
             } else {
                 String data = String.valueOf(part.charAt(0));
                 if (data.equals(dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.START))) {
                     context.elements().add(new ExclusionMarkerElement(TranslitDictionary.ExclusionMarker.START));
                 } else if (data.equals(dictionary.getExclusionMarker(TranslitDictionary.ExclusionMarker.END))) {
                     context.elements().add(new ExclusionMarkerElement(TranslitDictionary.ExclusionMarker.END));
                 } else {
                     int idx = dictionary.indexOf(data, side.invert());
                     if (idx > -1) {
                         context.elements().add(new IndexElement(idx));
                     } else {
                         context.elements().add(new CharacterElement(data));
                     }
                 }
                 context.setPosition(context.getPosition() + 1);
             }
         }
         return context;
     }
 
 
     private void validatePosition(int pos) throws TranslitDocumentException {
         if (pos + 1 > elements.size() || pos < 0)
             throw new TranslitDocumentException(MessageFormat.format(ERR_INVALID_DATA_POS,
                     new Object[]{new Integer(pos), new Integer(0), new Integer(elements.size() > 0 ? elements.size() - 1 : 0)}));
     }
 
     private void validateElementsRange(int start, int end) throws TranslitDocumentException {
         if (start > end || end > elements.size() || start < 0 || end < 0)
             throw new TranslitDocumentException(MessageFormat.format(ERR_INVALID_DATA_RANGE,
                     new Object[]{new Integer(start), new Integer(end), new Integer(elements.size())}));
     }
 
     private SortedSet<Match> newSynchronizedSortedSet() {
         return new TreeSet(new Comparator() {
             public int compare(Object o1, Object o2) {
                 return ((Match) o1).length().compareTo(((Match) o2).length());
             }
         });
     }
 
     public class IndexElement extends Element {
 
         private int index;
 
         public IndexElement(int index) {
             this.index = index;
         }
 
         @Override
         public String getStringValue(StringBuildingContext buildingContext) {
             return getDictionary().getValueAt(index, buildingContext.getSide());
         }
 
         @Override
         public boolean isTransliteration() {
             return true;
         }
     }
 
     public class CharacterElement extends Element {
 
         private String data;
 
         public CharacterElement(String data) {
             this.data = data;
         }
 
         @Override
         public String getStringValue(StringBuildingContext stringBuildingContext) {
             return data;
         }
 
         @Override
         public boolean isTransliteration() {
             return false;
         }
     }
 
     public class ExclusionMarkerElement extends Element {
 
         private final TranslitDictionary.ExclusionMarker exclusionMarker;
 
         public ExclusionMarkerElement(TranslitDictionary.ExclusionMarker exclusionMarker) {
             this.exclusionMarker = exclusionMarker;
         }
 
         @Override
         public String getStringValue(StringBuildingContext stringBuildingContext) {
             return !stringBuildingContext.isMarkersShowed() ? "" : dictionary.getExclusionMarker(exclusionMarker);
 
         }
 
         @Override
         public boolean isTransliteration() {
             return false;
         }
     }
 
     /**
      *
      */
     public class Match {
         private int index;
         private String stringPart;
 
         public Match(int index, String stringPart) {
             this.index = index;
             this.stringPart = stringPart;
         }
 
         public int index() {
             return index;
         }
 
         public String getStringPart() {
             return stringPart;
         }
 
         public Integer length() {
             return stringPart.length();
         }
 
         public String toString() {
             return stringPart + ";idx=" + index;
         }
     }
 
     public class ParsingContext {
         private TranslitDictionary.Side side;
         private String text;
         private Vector elements = new Vector();
         private int position;
         private Vector<SortedSet<Match>> matchesVector = new Vector();
 
         public ParsingContext(String text, TranslitDictionary.Side side) {
             this.side = side;
             this.text = text;
         }
 
         public TranslitDocument getDocument() {
             return DefaultTranslitDocument.this;
         }
 
         public TranslitDictionary getDictionary() {
             return DefaultTranslitDocument.this.getDictionary();
         }
 
         public int getPosition() {
             return position;
         }
 
         public TranslitDictionary.Side getSide() {
             return side;
         }
 
         public List<Element> elements() {
             return elements;
         }
 
         public String getText() {
             return text;
         }
 
         public Vector<SortedSet<Match>> matches() {
             return matchesVector;
         }
 
         public SortedSet<Match> currentMatchSet() {
             return matchesVector.lastElement();
         }
 
         public void setPosition(int position) {
             this.position = position;
         }
     }
 
 
 
 }
