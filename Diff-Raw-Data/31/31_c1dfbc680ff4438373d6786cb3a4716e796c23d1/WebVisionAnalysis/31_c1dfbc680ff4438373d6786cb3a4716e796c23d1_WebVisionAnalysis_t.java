 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 
 import ICTCLAS.I3S.AC.ICTCLAS50;
 
 public class WebVisionAnalysis {
 
 	String inputPath = null;
 
 	String outputPath = null;
 
 	HashMap<String, Integer> keywordInfo = null;
 
 	ArrayList<String> sortedKeywords = null;
 
 	HashSet<String> ChineseStopWords = null;
 
 	HashSet<String> EnglishStopWords = null;
 	
 	int fileCount = -1;
 
 	public WebVisionAnalysis(String inputPath, String outputPath) {
 		this.inputPath = inputPath;
 		this.outputPath = outputPath;
 
 		this.keywordInfo = new HashMap<String, Integer>();
 
 		this.EnglishStopWords = new HashSet<String>();
 		this.ChineseStopWords = new HashSet<String>();
 		initStopWords();
 		
 		this.fileCount = 0;
 	}
 
 	private void initStopWords() {
 		String EnglishStopWordsPath = "D:/workspace2/WebVisionAnalysis/blacklist_en.txt";
 		try {
 			FileReader fr = new FileReader(EnglishStopWordsPath);
 			BufferedReader br = new BufferedReader(fr);
 			while (br.ready()) {
 				String line = br.readLine();
 				EnglishStopWords.add(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		String ChineseStopWordsPath = "D:/workspace2/WebVisionAnalysis/blacklist_cn.txt";
 		try {
 			FileReader fr = new FileReader(ChineseStopWordsPath);
 			BufferedReader br = new BufferedReader(fr);
 			while (br.ready()) {
 				String line = br.readLine();
 				EnglishStopWords.add(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void main(String[] args) {
		String inputPath = "D:/data2/UKviews";
		String outputPath = "D:/output/UKviews";
 
 		WebVisionAnalysis wva = new WebVisionAnalysis(inputPath, outputPath);
 		wva.handle();
 
 	}
 
 	private void handle() {
 		System.out.println(" handling " + inputPath);
 		// Process
 		File f = new File(inputPath);
 		readAllFiles(f);
 		// sort and output the Keywords group3 needed
 		//Keywords2File();
 	}
 	private void readAllFiles(File f)
 	{	
 		if(f.isDirectory())
 		{
 			File[] fs = f.listFiles();
 			
 			for(int i=0; i < fs.length; i++){
 				System.out.println( i + " " + fs.length + fs[i].getAbsolutePath());
 				readAllFiles(fs[i]);
 			}
 		}
 		else
 		{
 			String rel = f.getAbsolutePath().substring(inputPath.length(), f.getAbsolutePath().length());
 			handleFile(f, rel);
 			
			//System.out.println("removing"  + f.getAbsolutePath());
			//f.delete();
 			fileCount++;
 		}
 	}
 
 	private void handleFile(File file, String relativePath)
 	{
 		System.out.println("\nhandling\t" + relativePath + keywordInfo.size());
 		 
 		try {
 			FileReader fr = new FileReader(file);
 			BufferedReader br = new BufferedReader(fr);
 			while (br.ready()) {
 				// read a line from data source
 				String line = br.readLine();
 				//System.out.println("\t" + line);
 
 				// analysis the line
 				if (isEnglish(line)) {
 					analysisEnglish(line);
 				} else {
 					analysisChinese(line);
 				}
 			}
 			br.close();
 			fr.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Keywords2File(relativePath);
 		//System.out.println("readData finished");
 	}
 
 
 	private void analysisChinese(String line) {
 		//System.out.println("analysisChinese");
 		//		 select keyword by some rules and update keywordInfo
 		
 		try {
 			ICTCLAS50 testICTCLAS50 = new ICTCLAS50();
 			// ִ·
 			String argu = ".";
 			// ʼ
 			String code = "GB2312";
 			// String code = "UTF8";
 			if (testICTCLAS50.ICTCLAS_Init(argu.getBytes(code)) == false) {
 				System.out.println("ICTCLAS Init Fail!");
 				return;
 			} else {
 				//System.out.println("ICTCLAS Init Succeed!");
 			}
 			byte nativeBytes[] = testICTCLAS50.ICTCLAS_ParagraphProcess(line
 					.getBytes(code), 0, 1);
 			//System.out.println(nativeBytes.length);
 			String nativeStr = new String(nativeBytes, 0, nativeBytes.length,
 					code);
 			String[] words = nativeStr.split(" ");
 			for (String s : words) {
 				//System.out.println("\tword: " + s);
 				String[] parts = s.split("/");
 				if (parts.length > 1)
 					if (!ChineseStopWords.contains(parts[0])) {
 						if(parts[1].contains("n") || parts[1].contains("a"))
 						{
 							//System.out.println("word:" + parts[0]);
 							//System.out.println("pos:" + parts[1]);
 							addWord(parts[0]);
 				
 						}
 				}
 			}
 			//System.out.println("The result is " + nativeStr);
 			testICTCLAS50.ICTCLAS_Exit();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		//System.out.println("analysisChinese finished");
 
 		// ictclas suggestted
 
 	}
 
 	private void addWord(String keyword) {
 		//System.out.println("addWord " + keyword);
 
 		if (keywordInfo.containsKey(keyword)) {
 			int i = keywordInfo.get(keyword);
 			keywordInfo.put(keyword, i + 1);
 		} else {
 			keywordInfo.put(keyword, 1);
 			//System.out.println("newWord " + keyword);
 		}
 
 	}
 
 	private void analysisEnglish(String line) {
 		// wordnet suggestted
		System.out.println("analysisEnglish");
		String[] words = line.split(" |,|\\.|\\?|!|-|\\=");
 		for (String w : words) {
 			if (!EnglishStopWords.contains(w)) {
				/*
 				if (w.endsWith("s")) {
 					w = w.substring(0, w.length() - 2);
 				}
				*/
				System.out.println("\t" + w);
 				addWord(w);
 			}
 		}
 
 	}
 
 	private void Keywords2File(String relativePath) {
		System.out.println(" Keywords2File " + outputPath + relativePath);
 		try {
			File f = new File(outputPath + relativePath);
			f.mkdirs();
			if(f.isDirectory())
				f.delete();
 			PrintWriter fos = new PrintWriter(new FileWriter(outputPath + relativePath));
 			Set<Entry<String, Integer>> entryset = keywordInfo.entrySet();
 			sortedKeywords = new ArrayList<String>(entryset.size());
 			for (Entry e : entryset) {
 				sortedKeywords.add((String) e.getKey());			
 			}
 			
 			Collections.sort(sortedKeywords, new KeywordComparator());
 			for(String s:sortedKeywords)
 			{
 				//System.out.println(s + " " + keywordInfo.get(s));
 				if(s.length() > 2)
 					fos.println(s + " " + keywordInfo.get(s));				// data format requested
 			}
 
 			
 			fos.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	class KeywordComparator implements Comparator {
 		public int compare(Object o1, Object o2) {
 			String s1 = (String) o1;
 			String s2 = (String) o2;
 			return keywordInfo.get(s2) - keywordInfo.get(s1);
 		}
 
 	}
 	public static boolean isEnglish(String line) {
 		// check if this line is pure English
 		int n;
 		n = line.length();
 		// System.out.print(n);
 		for (int i = 0; i < n; i++) {
 			// System.out.print(line.charAt(i));
			int a=line.charAt(i);
			//System.out.print(a);
 			if (line.charAt(i) >= 128 || line.charAt(i) <= 0)
 				return false;
 			else if (i == n) {
 				return true;
 			}
 		}
 		return true;
 	}
 
 }
