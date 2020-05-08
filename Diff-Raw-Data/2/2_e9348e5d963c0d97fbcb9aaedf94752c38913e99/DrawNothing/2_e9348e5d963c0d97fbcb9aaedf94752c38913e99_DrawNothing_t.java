 package net.wengs.drawnothing;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * @author Sutra Zhou
  */
 public class DrawNothing {
 
 	private int answerMinLength = 3;
 	private int answerMaxLength = 8;
 
 	private Set<String>[] dicts;
 
 	public DrawNothing() throws IOException {
 		initDicts();
 	}
 
 	public Collection<String> riddle(String givenLetters, int answerWordLength) {
 		Set<String> dict = dicts[answerWordLength - answerMinLength];
 		Collection<String> answers = new ArrayList<String>();
 
 		Collection<Character> candidates = toCollection(givenLetters.toLowerCase());
 		for (String word : dict) {
 			boolean sub = CollectionUtils.isSubCollection(toCollection(word),
 					candidates);
 			if (sub) {
 				answers.add(word);
 			}
 		}
 
 		return answers;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initDicts() throws IOException {
 		dicts = new LinkedHashSet[answerMaxLength - answerMinLength + 1];
 		for (int i = answerMinLength; i <= answerMaxLength; i++) {
 			dicts[i - answerMinLength] = new LinkedHashSet<String>();
 		}
 
 		addDict("freqwords.txt");
 		addDict("words.txt");
 	}
 
 	private void addDict(String dictName) throws IOException {
 		InputStream input = getClass().getResourceAsStream(dictName);
 		try {
 			List<String> words = IOUtils.readLines(input);
 			addDict(words);
 		} finally {
 			IOUtils.closeQuietly(input);
 		}
 	}
 
 	private void addDict(List<String> words) {
 		int dictCount = dicts.length;
 		int dictIndex;
 
 		for (String word : words) {
 			dictIndex = word.length() - answerMinLength;
			if (dictIndex >= 0 && dictIndex < dictCount) {
 				dicts[dictIndex].add(word.toLowerCase());
 			}
 		}
 	}
 
 	private Collection<Character> toCollection(String str) {
 		char[] chars = str.toCharArray();
 		Collection<Character> candidates = new ArrayList<Character>(
 				chars.length);
 		for (char c : chars) {
 			candidates.add(c);
 		}
 		return candidates;
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		DrawNothing dn = new DrawNothing();
 
 		long start = System.currentTimeMillis();
 
 		Collection<String> answers = dn.riddle(args[0],
 				Integer.parseInt(args[1]));
 
 		long end = System.currentTimeMillis();
 
 		System.out.println(end - start);
 		System.out.println(StringUtils.join(answers, "\n"));
 	}
 
 }
