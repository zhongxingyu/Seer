 package ai_project;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Arrays;
 import java.util.Set;
 
 
 public class Perceptron {
 		List<String> FilesToLearn;
 		List <wordRecord> bagOfWords2;
 		String [] myBag;
 		int total=0;
 		double [] weights;
 		String classification;
 		
 		public Perceptron(List<String> f,String s){
 			FilesToLearn=new ArrayList<String>();
 			for(String z: f){
 				FilesToLearn.add(z);
 			}
 			classification=s;
 		}
 		
 		public void preprocess(){
 			//first step do normal preprocessing
 			List<String> files2=new ArrayList<String>();
 			for(String f:FilesToLearn){
 				//conver all non alpha,non space chars to spaces
 				f=f.toLowerCase();
 				f=f.replaceAll("[^a-zA-Z]", " ");
 				f=f.replace("\n"," ");
 				String regex = "\\s{2,}"; 
 				f = f.replaceAll(regex, " "); 
 				f=f.trim();
 				files2.add(f);
 			}
 			FilesToLearn.clear();
 			for(String f: files2){
 				FilesToLearn.add(f);
 			}
 			//get word counts
 			boolean toadd=true;
 			List<wordRecord> bagOfWords=new ArrayList<wordRecord>();
 			for(String f:FilesToLearn){
 				String [] g=f.split(" ");
 				for(String hh:g){
 					for(wordRecord z:bagOfWords){
 						if(z.word.equals(hh)){
 							toadd=false;
 							z.count++;
 							break;
 						}
 					}
 					
 					if(toadd){
 						bagOfWords.add(new wordRecord(hh));
 					}
 					toadd=true;
 				}
 			}
 			//calculate total words
 			int totalCount=0;
 			for(wordRecord f:bagOfWords){
 				totalCount+=f.count;
 				//System.out.println(f.word);
 			}
 			//System.exit(1);
 			total=totalCount;
 			//get p(w/c)
 			for(wordRecord f:bagOfWords){
 				f.countP=((float)f.count)/((float)totalCount);
 			}
 			//this is kinda ugly but i am putting the word records into an array and is sorting by p(w/c)
 			Object [] p=bagOfWords.toArray();
 			wordRecord [] pp=new wordRecord[p.length];
 			for(int i=0;i<p.length;i++){
 				pp[i]=(wordRecord)p[i];
 			}
 			Arrays.sort(pp,new Comparator<Object>(){ public int compare(Object o1,Object o2){
 						wordRecord w1=(wordRecord)o1; wordRecord w2=(wordRecord) o2;
 								if(w1.countP<w2.countP)return 1; else if(w1.countP>w2.countP) return -1;
 								else return w1.word.compareTo(w2.word);};});
 			//grab to 20 words, or as many as we can
 			int wordC=0;
 			bagOfWords2=new ArrayList<wordRecord>();
 			for(int i=0;i<pp.length;i++){
 				bagOfWords2.add(pp[i]);
 				wordC++;
 				if(wordC==20)
 					break;
 			}
 		
 		}
 		
 		public void setupbag(List<String> bag){
 			myBag=new String[bag.size()];
 			weights=new double[bag.size()];
 			for(int i=0;i<myBag.length;i++){
 				myBag[i]=bag.get(i);
 				weights[i]=0.6;
 			}
 		}
 		
 		public void train(wordRecord[] wordList, String c, double alpha){
 			boolean results=vote(wordList);
 			int error=0;
 			if(results&&(!c.equalsIgnoreCase(classification))){
 				error=-1;	
 			}
 			if(!results&&(c.equalsIgnoreCase(classification))){
 				error=1;
 			}
 			if(error!=0){
 				for(int i=0;i<weights.length;i++){
 					weights[i]=weights[i]+alpha*error*wordList[i].countP;
 				}
 			}
 		}
 		
 		public boolean vote(wordRecord [] wordList){
 			int sum=0;
 			for(int i=0;i<wordList.length;i++){
 				sum+=weights[i]*wordList[i].countP;
 			}
			sum+=1;
 			if(sum>0){
 				return true;
 			}
 			return false;
 		}
 		
 }
