 package nature;
 
 import java.util.Arrays;
 import java.util.Random;
 
 public class BitString {
 	private static final Random random = new Random();
 
 	private boolean[] string;
 
 	public BitString(int length, double probability) {
 		string = new boolean[length];
 		for (int i = 0; i < string.length; i++) {
 			string[i] = (Math.random() < probability);
 		}
 	}
 
 	public BitString(boolean[] string) {
 		this.string = string;
 	}
 
 	public BitString(String textString) {
 		string = new boolean[textString.length()];
 		for (int i = 0; i < textString.length(); i++) {
 			char c = textString.charAt(i);
 			string[i] = (c == '0' ? false : true);
 		}
 	}
 
 	public int length() {
 		return string.length;
 	}
 
 	public int numberOfOnes() {
 		int ones = 0;
 		for (boolean bit : string) {
 			if (bit) ones++;
 		}
 		return ones;
 	}
 
 	public int numberOfLeadingOnes() {
 		int ones = 0;
 		for (boolean bit : string) {
			ones++;
 			if (!bit) return ones;
 		}
 		return ones;
 	}
 
 	public double positionOfOnes() {
 		int n = string.length;
 		int sum = 0;
 		int ones = 0;
 
 		for (int i = 0; i < n; i++) {
 			if (string[i]) {
 				sum += i;
 				ones++;
 			}
 		}
 
 		double avg = (double) sum / n;
 		double min = (ones - 1) * ones / 2.0 / n;
 		double max = ((n - 1) * n - (n - ones - 1) * (n - ones)) / 2.0 / n;
 		double diff = max - min;
 
 		if (diff == 0) {
 			return 0.0;
 		}
 
 		return (avg - min) / diff;
 	}
 
 	private int nextPos = -1;
 
 	private int getNextPos(int length, double l) {
 		if (nextPos >= 0) {
 			return savePos(nextPos, length);
 		}
 		nextPos = (int) Math.floor(Math.log(random.nextDouble()) / l);
 		return savePos(nextPos, length);
 	}
 
 	private int savePos(int pos, int length) {
 		if (pos > length) {
 			nextPos = pos - length - 1;
 			return -1;
 		}
 
 		nextPos = -1;
 		return pos;
 	}
 
 	public BitString globalMutation(double probability) {
 		int start = 0;
 		double l = Math.log(1.0 - probability);
 		int next = getNextPos(string.length - 1, l);
 		if (next != -1) {
 			boolean[] mutation = Arrays.copyOf(string, string.length);
 			while (next != -1) {
 				mutation[start + next] = !string[start + next];
 				start += next + 1;
 				next = getNextPos(string.length - start - 1, l);
 			}
 			return new BitString(mutation);
 		}
 		return new BitString(string);
 	}
 
 	public BitString localMutation() {
 		boolean[] mutation = Arrays.copyOf(string, string.length);
 		int i = random.nextInt(mutation.length);
 		mutation[i] = !string[i];
 		return new BitString(mutation);
 	}
 
 	public BitString constructMutation(double[] pheromone) {
 		boolean[] mutation = new boolean[string.length];
 		for (int i = 0; i < mutation.length; i++) {
 			mutation[i] = random.nextDouble() < pheromone[i];
 		}
 		return new BitString(mutation);
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		for (boolean bit : string) {
 			sb.append((bit ? '1' : '0'));
 		}
 		return sb.toString();
 	}
 
 	public boolean[] getString() {
 		return string;
 	}
 }
