 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Random;
 
 public class Support {
 
 	public static void main(String[] args) {
 		Scanner scanner = new Scanner(System.in);
 		String line = scanner.nextLine();
 		TreeMap<String, Integer> keyWords = new TreeMap<String, Integer>();
		String[] words = line.split(" ");
 		for (int i = 0; i < words.length; i++) {
 			String word = words[i].toLowerCase();
 			if (keyWords.get(word) == null) {
 				keyWords.put(word, 1);
 			}
 		}
 		System.out.println("Thanks for contacting, we recommend:");
 		Set<Map.Entry<String, Integer>> entrySet = keyWords.entrySet();
 		for (Map.Entry<String, Integer> entry: entrySet) {
			System.out.println(Answer.getAnswer(entry.getKey()));
 		}
 
 	}
 
 	public enum Answer {
 		ram("ram", "exaggerate the ram capcity", "DDR to GDDR", "ERAM!"),
 		cpu("cpu", "higher frequency CPU", "AMD to INTEL", "overclocking CPU!"),
		disk("disk", "lager capcity disk", "SSD!", "10000 rpm!");
 
 		private String word;
 		private String answer1;
 		private String answer2;
 		private String answer3;
 
 		private Answer(String word, String answer1, String answer2, String answer3){
 			this.word = word;
 			this.answer1 = answer1;
 			this.answer2 = answer2;
 			this.answer3 = answer3;
 		}
 
 		private static Map<String, Answer> wordToAnswerMapping;
 
 		public static Answer getAnswer(String i) {
 			if (wordToAnswerMapping == null) {
 				initMapping();
 			}
 			return wordToAnswerMapping.get(i);
 		}
 
 		private static void initMapping() {
 			wordToAnswerMapping = new HashMap<String, Answer>();
 			for (Answer s : values()) {
 				wordToAnswerMapping.put(s.word, s);
 			}
 		}
 
 		@Override
 		public String toString() {
 			final StringBuilder sb = new StringBuilder();
 			Random rand = new Random();
 			int randomPick = rand.nextInt(3);
 			switch(randomPick) {
 				case 0: sb.append(answer1); break;
 				case 1: sb.append(answer2); break;
 				case 2: sb.append(answer3); break;
 			}
 			return sb.toString();
 		}
 	}
 }
