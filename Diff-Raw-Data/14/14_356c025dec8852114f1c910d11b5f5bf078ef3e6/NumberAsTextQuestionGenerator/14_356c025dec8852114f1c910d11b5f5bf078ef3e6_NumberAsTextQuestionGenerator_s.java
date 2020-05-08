 package no.anderska.wta.questions;
 
 import no.anderska.wta.game.Question;
 
 public class NumberAsTextQuestionGenerator extends AbstractQuestionGenerator {
 
     public static class TrivialNumbers extends NumberAsTextQuestionGenerator {
         public TrivialNumbers() {
             super(10, "Convert a number under 20 to text. E.g. 5 => 'five'");
         }
 
         @Override
         protected int randomNumber() {
             if (random.nextInt(100) == 0) return 0;
             if (random.nextInt(100) == 0) return 20;
             return random.nextInt(18) + 1;
         }
     }
 
     public static class SmallNumbers extends NumberAsTextQuestionGenerator {
         public SmallNumbers() {
             super(20, "Convert a number under 100 to text. E.g. 25 => 'twenty five'");
         }
 
         @Override
         protected int randomNumber() {
             if (random.nextInt(100) == 0) return 0;
             if (random.nextInt(100) == 0) return 20;
             return random.nextInt(99) + 1;
         }
     }
 
     public static class NormalNumbers extends NumberAsTextQuestionGenerator {
         public NormalNumbers() {
             super(30, "Convert a number under 1000 to text. E.g. 125 => 'one hundred and twenty five'");
         }
 
         @Override
         protected int randomNumber() {
             if (random.nextInt(100) == 0) return (1+random.nextInt(10))*100;
             return random.nextInt(10) * 100 + random.nextInt(99) + 1;
         }
     }
 
     public static class LargeNumbers extends NumberAsTextQuestionGenerator {
         public LargeNumbers() {
             super(40, "Convert any number to text. E.g. 12225 => 'twelve thosand two hundred and twenty five'");
         }
 
         @Override
         protected int randomNumber() {
             if (random.nextInt(100) == 0) {
                 // Billions
                 return random.nextInt(1000000000) + 1000000000;
             }
             if (random.nextInt(100) == 0) {
                 // Millions
                 return random.nextInt(10000000) + 1000000;
             }
             if (random.nextInt(20) == 0) {
                 // Number without 100 => "three thousand and four"
                 int result = random.nextInt(1000000);
                 return (result / 1000)*1000 + result%100;
             }
             // Number with 100
             int result = random.nextInt(1000000);
             return (result / 1000)*1000 +
                     (random.nextInt(8)+1) * 100 +
                     result%100;
         }
     }
 
     NumberAsTextQuestionGenerator(int points, String description) {
         super(30, description);
     }
 
     @Override
     protected Question createQuestion() {
         int number = randomNumber();
         return new Question(String.valueOf(number), asText(number));
     }
 
     protected int randomNumber() {
         return random.nextInt(20);
     }
 
     public String asText(int number) {
         switch(number){
             case 0: return "zero";
             case 1: return "one";
             case 2: return "two";
             case 3: return "three";
             case 4: return "four";
             case 5: return "five";
             case 6: return "six";
             case 7: return "seven";
             case 8: return "eight";
             case 9: return "nine";
             case 10: return "ten";
             case 11: return "eleven";
             case 12: return "twelve";
             case 13: return "thirteen";
             case 14: return "fourteen";
             case 15: return "fifteen";
             case 16: return "sixteen";
             case 17: return "seventeen";
             case 18: return "eighteen";
             case 19: return "nineteen";
             case 20: return "twenty";
             case 30: return "thirty";
             case 40: return "forty";
             case 50: return "fifty";
             case 60: return "sixty";
             case 70: return "seventy";
             case 80: return "eighty";
             case 90: return "ninety";
         }
 
         if (number >= 1000000000) {
             if (number % 1000000000 == 0) {
                 return asText(number/1000000000) + " billion";
            } else if ((number%1000000000) / 100 > 1) {
                 return asText(number - number%1000000000) + " " + asText(number%1000000000);
             } else {
                 return asText(number - number%1000000000) + " and " + asText(number%1000000000);
             }
         }
 
         if (number >= 1000000) {
             if (number % 1000000 == 0) {
                 return asText(number/1000000) + " million";
            } else if ((number%1000000) / 100 > 1) {
                 return asText(number - number%1000000) + " " + asText(number%1000000);
             } else {
                 return asText(number - number%1000000) + " and " + asText(number%1000000);
             }
         }
 
         if (number >= 1000) {
             if (number % 1000 == 0) {
                 return asText(number/1000) + " thousand";
            } else if ((number%1000) / 100 > 1) {
                 return asText(number - number%1000) + " " + asText(number%1000);
             } else {
                 return asText(number - number%1000) + " and " + asText(number%1000);
             }
         }
 
         if (number >= 100) {
             if (number % 100 == 0) {
                 return asText(number/100) + " hundred";
             } else {
                 return asText(number - number%100) + " and " + asText(number%100);
             }
         }
 
         if (number >= 20 && number % 10 != 0) {
             return asText(number - number%10) + " " + asText(number%10);
         }
 
         return null;
     }
 
 }
