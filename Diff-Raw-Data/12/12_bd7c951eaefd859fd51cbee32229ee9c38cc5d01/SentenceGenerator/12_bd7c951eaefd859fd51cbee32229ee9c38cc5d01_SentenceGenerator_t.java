 import java.util.ArrayList;
 
 public class SentenceGenerator {
     WordRepository repository;
     public int numOfWords = 10;
     final static Word endOfSentence = new Word("^&eos&^");
     
     public SentenceGenerator(WordRepository repository) {
         this.repository = repository;
     }
     
     public void generate() {
         StringBuilder sb = new StringBuilder();
         ArrayList<Word> wordsClone = (ArrayList<Word>) this.repository.getWordList().clone();
         Word word = wordsClone.get((int) (Math.random() * wordsClone.size()));
         String sentence = buildString(word, numOfWords);
         sb.append(sentence);
         System.out.println(sb.toString());
     }
 
     public Word getNextLink(Word first) {
         Word check = repository.getByName(first.toString());
         return check.getBestLink();
     }
 
     public String buildString(Word start, int counter) {
         StringBuilder sb = new StringBuilder();
         Word next = repository.getByName(start.toString());
         String firstWord = next.toString();
         String firstLetter = firstWord.substring(0,1);
        sb.append(firstWord.replaceFirst(firstLetter, firstLetter.toUpperCase()));
         sb.append(" ");
         ArrayList<Word> usedWords = new ArrayList<Word>();
         usedWords.add(next);
         for (int i = 0; i < counter; i++) {
             if (i == counter - 1 ) {
                 if (repository.getLikelyEOSWord(next) == null) {
                     sb.append("EOS NOT FOUND");
                     break;
                 } else {
                     usedWords.add(repository.getLikelyEOSWord(next));
                     sb.append(repository.getLikelyEOSWord(next).toString()).append(".");
                     break;
                 }
             } else if (next.getBestLink() != null) {
                 usedWords.add(next.getBestLink());
                 sb.append(next.getBestLink().toString());
                 sb.append(" ");
                 next = repository.getByName(next.getBestLink().toString());
             } else {
                 // sb.append("NO FOLLOWING LINK");  <- commenting out for now
                 sb.append("is the end.");
                 break;
             }
         }
         for (Word word : usedWords) {
             for (Link link : word.links) {
                 link.restoreRate();
             }
         }
         return cullDuplicates(sb.toString());
     }
 
     public String cullDuplicates(String sentence) {
         String[] words = sentence.split(" ");
         ArrayList<String> returnList = new ArrayList<String>();
         for (int i = 0; i < words.length; i++) {
             if (i >= 1 && (words[i].equals(words[i - 1]))) {
                 // ignore - do not add consecutive identical words
             } else {
                 returnList.add(words[i]);
             }
         }
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < returnList.size(); i++) {
             if (i == (returnList.size() - 1)) {
                 sb.append(returnList.get(i));
             } else {
                 sb.append(returnList.get(i));
                 sb.append(" ");
             }
         }
        return sb.toString().replaceAll(" i ", " I ");
     }
 }
