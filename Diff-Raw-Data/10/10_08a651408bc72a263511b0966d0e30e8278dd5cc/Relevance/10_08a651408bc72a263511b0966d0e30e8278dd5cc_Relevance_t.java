 package CLRLM;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class Relevance {
 	BufferedReader sourceR;
 	BufferedReader targetR;
 	BufferedReader probSourceR;
 	BufferedReader probTargetR;
 	ArrayList<String[]> sourceText, targetText, documents;
 	HashMap<String, Double> probSource, probTarget;
 	TreeMap<Double, String[]> KLDocument; 
 	HashMap<String, HashMap<Integer, Double>> probWordGivenSource;
 	HashMap<String, HashMap<Integer, Double>> probWordGivenTarget;
 	Entry entry;
 	double alpha = 0.8, beta = 0.2;
 	
 	public Relevance(Entry entry) {
 		try{
 			this.entry = entry;
 			sourceR = new BufferedReader(new InputStreamReader(new FileInputStream("source.txt")));
 			targetR = new BufferedReader(new InputStreamReader(new FileInputStream("target.txt")));
 			sourceText = new ArrayList<String[]>();
 			targetText = new ArrayList<String[]>();
 			documents = new ArrayList<String[]>();
 			String line;
 			while((line = sourceR.readLine()) != null) {
 				sourceText.add(line.split(" "));
 			}
 			sourceR.close();
 			while((line = targetR.readLine()) != null) {
 				targetText.add(line.split(" "));
 			}
 			targetR.close();
 			documents = targetText;	
 			if(entry.t) {
 				probSource = entry.ps;
 				probTarget = entry.pt;
 				probWordGivenSource = entry.probWordGivenSource;
 				probWordGivenTarget = entry.probWordGivenTarget;				
 			}
 			else {
 				probSourceR = new BufferedReader(new InputStreamReader(new FileInputStream("probSource.txt")));
 				probTargetR = new BufferedReader(new InputStreamReader(new FileInputStream("probTarget.txt")));
 				probSource = new HashMap<String, Double>();
 				probTarget = new HashMap<String, Double>();
 				probWordGivenSource = new HashMap<String, HashMap<Integer, Double>>();
 				probWordGivenTarget = new HashMap<String, HashMap<Integer, Double>>();
 				
 				while((line = probSourceR.readLine()) != null) {
 					probSource.put(line.split(" ")[0], Double.valueOf(line.split(" ")[1]));
 				}
 				probSourceR.close();
 				while((line = probTargetR.readLine()) != null) {
 					probTarget.put(line.split(" ")[0], Double.valueOf(line.split(" ")[1]));
 				}
 				probTargetR.close();
 				
 				BufferedReader pwgs = new BufferedReader(new InputStreamReader(new FileInputStream("probWordGivenSource.txt")));
 				while((line = pwgs.readLine()) != null) {
 					String[] lines = line.split(" ");
 					if(probWordGivenSource.containsKey(lines[0])) {
 						for(int i = 1; i < lines.length; i += 2) {
 							probWordGivenSource.get(lines[0]).put(Integer.valueOf(lines[i]), Double.valueOf(lines[i+1]));
 						}
 					}
 					else {
 						HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
 						for(int i = 1; i < lines.length; i += 2) {
 							tmp.put(Integer.valueOf(lines[i]), Double.valueOf(lines[i+1]));
 						}
 						probWordGivenSource.put(lines[0], tmp);
 					}
 				}
 				pwgs.close();
 				
 				BufferedReader pwgt = new BufferedReader(new InputStreamReader(new FileInputStream("probWordGivenTarget.txt")));
 				while((line = pwgt.readLine()) != null) {
 					String[] lines = line.split(" ");
 					if(probWordGivenTarget.containsKey(lines[0])) {
 						for(int i = 1; i < lines.length; i += 2) {
 							probWordGivenTarget.get(lines[0]).put(Integer.valueOf(lines[i]), Double.valueOf(lines[i+1]));
 						}
 					}
 					else {
 						HashMap<Integer, Double> tmp = new HashMap<Integer, Double>();
 						for(int i = 1; i < lines.length; i += 2) {
 							tmp.put(Integer.valueOf(lines[i]), Double.valueOf(lines[i+1]));
 						}
 						probWordGivenTarget.put(lines[0], tmp);
 					}
 				}
 				pwgt.close();
 			}
 			System.out.println("Ready");
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void configure(String query) {
 		System.out.println(query);
 		String[] queryTerms = query.split(" ");
 		HashMap<String, Double> pwR = new HashMap<String, Double>();
 		ArrayList<Double> pqM = new ArrayList<Double>();
 		double totalProb = 0;
 		/*
 		 * 先遍历Ms，获取query在Ms上的概率，即P(Q|Ms)
 		 */
 		for(int i = 0; i < sourceText.size(); i++) {
 			double p = 1;
 			for(int j = 0; j < queryTerms.length; j++) {
 				if(probWordGivenSource.get(queryTerms[j]).containsKey(i+1)) {
 					p *= alpha*probWordGivenSource.get(queryTerms[j]).get(i+1) + beta*probSource.get(queryTerms[j]);//probS(queryTerms[j], sourceText.get(i));
 				}
 				else {
 					p *= beta*probSource.get(queryTerms[j]);
 				}
 			}
 			pqM.add(p);
 			totalProb += p;
 		}
 		System.out.println("w");
 		/*
 		 * 遍历w，计算P(w|Q)=P(w|Mt)*P(Ms|Q),其中计算P(Ms|Q)时使用贝叶斯公式并忽略P(Ms)项
 		 */
 		double allp = 0;
 		for(Map.Entry<String, Double> targetTerm:probTarget.entrySet()) {
 			String w = targetTerm.getKey();
 			double pwRm = 0;
 			for(int i = 0; i < targetText.size(); i++) {
 				double p = 1;
 				if(probWordGivenTarget.get(w).containsKey(i+1)) {
 					p *= alpha*probWordGivenTarget.get(w).get(i+1) + beta*probTarget.get(w);//probS(queryTerms[j], sourceText.get(i));
 				}
 				else {
 					p *= beta*probTarget.get(w);
 				}
 				p = p * pqM.get(i);
 				p = p /totalProb;
 				pwRm += p;
 			}
 			pwR.put(w, pwRm);
 			allp += pwRm*Math.log(pwRm/(beta*probTarget.get(w)));
 		}
 		System.out.println("KL");
 		/*
 		 * 遍历文档，根据KL散度计算每篇文档与query的相关性
 		 */
 		KLDocument = new TreeMap<Double, String[]>();
 		for(int i = 0; i < documents.size(); i++) {
 			String[] document = documents.get(i);
 			double p = allp;
 			/*
 			for(Map.Entry<String, Double> targetTerm:probTarget.entrySet()) {
 				String w = targetTerm.getKey();
 				double pwD;
 				if(probWordGivenTarget.get(w).containsKey(i+1)) {
 					pwD = 0.8*probWordGivenTarget.get(w).get(i+1) + 0.2*probTarget.get(w);//probS(queryTerms[j], sourceText.get(i));
 				}
 				else {
 					pwD = 0.2*probTarget.get(w);
 				}
 				//probT(w, document);
 				p += pwR.get(w)*Math.log(pwR.get(w)/pwD);
 			}
 			*/
 			HashSet<String> filter = new HashSet<String>();
 			for(int j = 0; j < document.length; j++) {
 				String w = document[j].toLowerCase();
 				if(entry.stopWords.contains(w)||filter.contains(w)) continue;
 				filter.add(w);
 				double pwD = alpha*probWordGivenTarget.get(w).get(i+1) + beta*probTarget.get(w);//probS(queryTerms[j], sourceText.get(i));
 				p = p + pwR.get(w)*Math.log(pwR.get(w)/pwD) - pwR.get(w)*Math.log(pwR.get(w)/(beta*probTarget.get(w)));
 			}
 			KLDocument.put(p, document);
 		}
 		
 		/*
 		 * 输出前10个相关文档
 		 */
 		int num = 0;
 		System.out.println(KLDocument.size());
 		for(Map.Entry<Double, String[]> document:KLDocument.entrySet()) {
 			String s = "";
 			String[] terms = document.getValue();
 			for(int i = 0; i < terms.length; i++) {
 				s += terms[i] + " ";
 			}
 			System.out.println(s);
 			System.out.println(document.getKey());
 			num++;
 			if(num > 10) break;
 		}
 		
 	}
 	
 	public void close() {

 	}
 }
