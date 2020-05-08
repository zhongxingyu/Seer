 import java.util.ArrayList;
 
 public class SentenceGenerator {
     public WordRepository repository;
     public int numOfWords;
     final static Word endOfSentence = new Word("^&eos&^");
     String lastSentence;
     
     public SentenceGenerator(WordRepository repository) {
         this.repository = repository;
         this.numOfWords = (int) ((Math.random() * 7) + 4);
     }
 
     public SentenceGenerator(WordRepository repository, int numOfWords) {
         this.repository = repository;
         this.numOfWords = numOfWords;
     }
 
     public void generate() {
         StringBuilder sb = new StringBuilder();
         ArrayList<Word> wordsClone = (ArrayList<Word>) this.repository.getWordList().clone();
         Word word = wordsClone.get((int) (Math.random() * wordsClone.size()));
         System.out.println(word.toString());
         String sentence = buildString(word, numOfWords);
         lastSentence = sentence;
         sb.append(sentence);
         System.out.println(sb.toString());
     }
 
     public void regeneratePartialSentence(Integer userSelection) {
         String[] sentenceArray = lastSentence.split(" ");
 
 //        System.out.println("Sentence array to follow: ");
 //        for (int i = 0; i < sentenceArray.length; i++) {
 //            String s = sentenceArray[i];
 //            System.out.print(s + " ");
 //        }
 
        String partialSentence = getPartialSentence(userSelection - 1, sentenceArray);
         String startingWord = sentenceArray[userSelection - 1];
         System.out.println("Starting word: " + startingWord);

         StringBuilder sb = new StringBuilder();
         Word word = this.repository.getByName(startingWord.toLowerCase());
 
         System.out.println(numOfWords + " : " + (numOfWords - userSelection));
         String sentence = buildString(word, numOfWords - userSelection);
        sentence = partialSentence + sentence;
         sb.append(sentence);
         lastSentence = sentence;
         System.out.println(sb.toString());
     }
 
    private String getPartialSentence(Integer userSelection, String[] sentenceArray) {
        StringBuilder lastSentenceTemp = new StringBuilder();
        for (int i = 0; i < userSelection; i++) {
            String s = sentenceArray[i];
            lastSentenceTemp.append(s + " ");
        }

        return lastSentenceTemp.toString();
    }

     public Word getNextLink(Word first) {
         Word check = repository.getByName(first.toString());
         return check.getBestLink(0);
     }
 
     public String buildString(Word start, int counter) {
         StringBuilder sb = new StringBuilder();
        Word next = new Word("<WORD NOT FOUND>");

        if (repository.getByName(start.toString()) != null) {
            next = repository.getByName(start.toString());
        }

         String firstWord = next.toString();
         firstWord = upperCaseFirstLetter(firstWord);
         sb.append(firstWord).append(" ");
         ArrayList<Word> usedWords = new ArrayList<Word>();
         usedWords.add(next);
         iterateAndSelectWords(counter, sb, next, usedWords);
         restoreLinkRates(usedWords);
         return cullDuplicatesAndCreateString(sb.toString()).replaceAll(" i\\.", " I.");
     }
 
     private String upperCaseFirstLetter(String firstWord) {
         String firstLetter = firstWord.substring(0,1);
         return firstWord.replaceFirst(firstLetter, firstLetter.toUpperCase());
     }
 
     private void iterateAndSelectWords(int counter, StringBuilder sb, Word next, ArrayList<Word> usedWords) {
         for (int i = 0; i < counter; i++) {
             int priorityCounter = priorityCounterGenerate();
             if (i == counter - 1 ) {
                 guardAgainstNull(sb, next, usedWords);
                 break;
             } else if (next.getBestLink(priorityCounter) != null) {
                 usedWords.add(next.getBestLink(priorityCounter));
                 sb.append(next.getBestLink(priorityCounter).toString());
                 sb.append(" ");
                 next = repository.getByName(next.getBestLink(priorityCounter).toString());
             } else {
                 sb.append("<NO FOLLOWING LINK>");
                 break;
             }
         }
     }
     
     private int priorityCounterGenerate() {
         return (int) (Math.random() * 5);
     }
 
     private void guardAgainstNull(StringBuilder sb, Word next, ArrayList<Word> usedWords) {
         if (repository.getLikelyEOSWord(next) == null) {
             sb.append("EOS NOT FOUND");
         } else {
             usedWords.add(repository.getLikelyEOSWord(next));
             sb.append(repository.getLikelyEOSWord(next).toString());
         }
     }
 
     private void restoreLinkRates(ArrayList<Word> usedWords) {
         for (Word word : usedWords) {
             for (Link link : word.links) {
                 link.restoreRate();
             }
         }
     }
 
     public String cullDuplicatesAndCreateString(String sentence) {
         String[] words = sentence.split(" ");
         ArrayList<String> returnList = new ArrayList<String>();
         removeDupes(words, returnList);
         StringBuilder sb = new StringBuilder();
         addNonDuplicateWords(returnList, sb);
         return sb.append(".").toString().replaceAll(" a a", " an a").replaceAll(" a o", " an o")
                 .replaceAll(" a i", " an i").replaceAll(" i ", " I ")
                 .replaceAll(" a e", " an e").replaceAll(" a u", " an u");
     }
 
     private void removeDupes(String[] words, ArrayList<String> returnList) {
         for (int i = 0; i < words.length; i++) {
             if (i >= 1 && (words[i].equals(words[i - 1]))) {
                 // ignore - do not add consecutive identical words
             } else {
                 returnList.add(words[i]);
             }
         }
     }
 
     private void addNonDuplicateWords(ArrayList<String> returnList, StringBuilder sb) {
         for (int i = 0; i < returnList.size(); i++) {
             if (i == (returnList.size() - 1)) {
                 sb.append(returnList.get(i));
             } else {
                 sb.append(returnList.get(i));
                 sb.append(" ");
             }
         }
     }
 }
