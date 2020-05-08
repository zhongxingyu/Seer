 package src;
 
 public class MarkovGenerator {
     private final MarkovCollection frequencies;
     private final String text;
     private final GenerateLevel generateLevel;
 
     public MarkovGenerator(String text, GenerateLevel generateLevel) {
         this.text = text;
         this.generateLevel = generateLevel;
         this.frequencies = new MarkovCollection(text, generateLevel.splitSymbol);
     }
 
     public Integer frequencyOf(String letter) {
         return frequencies.getSymbol(letter.charAt(0)).usageCount();
     }
 
     public MarkovSymbol mostCommonlyFollowedOf(String letter) {
         return frequencies.getSymbol(letter.charAt(0)).mostCommonlyFollowedBy();
     }
 
     public String generate(int symbolsToGenerate) {
         return addSymbol("", frequencies.getSymbol(text.charAt(0)), 0, symbolsToGenerate);
     }
 
     public String generate(){
         return generate(text.length());
     }
 
     private String addSymbol(String textSoFar, MarkovSymbol currentSymbol, Integer count, Integer max){
        if (count <= max) {
             textSoFar += currentSymbol.character() + generateLevel.splitSymbol;
             count++;
             Character mostFollowed = currentSymbol.nextFollowedBy().character();
             MarkovSymbol mostFollowedSymbol = frequencies.getSymbol(mostFollowed);
             return addSymbol(textSoFar, mostFollowedSymbol, count, max);
         } else {
             return textSoFar;
         }
     }
 }
