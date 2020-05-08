 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 
 
 public class MS_PS {
 	public static HashMap<Integer, Float> MIS;
 	public ArrayList<Transaction> T;
 	public static int N; //the number of transaction in T;
 	public static Float SDC;
 	
 	MS_PS(String paraFile, String dataFile) throws IOException{
 		util utils = new util();
 		T = utils.readDataFile(dataFile);
 		MIS = util.readParaFile(paraFile);
 		SDC = utils.getSDC();
 		N = T.size()-1;
 	}
 	
 	public static HashMap<Integer, ArrayList<Pair<Transaction, Integer>>>r_PrefixSpan(Integer i, ArrayList<Transaction> S, int n, int min_sup, ArrayList<Integer> freqM,
 			ArrayList<Integer> M, HashMap<Integer, Float> MIS,HashMap<Integer, ArrayList<Pair<Transaction, Integer>>>F, HashMap<Integer, Integer> Lcount){
 		MSPrefixSpan ps = new MSPrefixSpan();
 		HashMap<Integer, ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>>> L = new HashMap<Integer, ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>>>();
 		for(int index = 1; index < 11; index++)
 			L.put(index, null);
 		util utils = new util();
 		L.put(1, ps.gen_l1patterns(S, M, MIS, n, min_sup, Lcount));
 		for(int index=0; index < L.get(1).size(); index++){
 			Patterns<Transaction, Integer, ArrayList<Transaction>> tmp_pattern = L.get(1).get(index);
 			Transaction a = tmp_pattern.getFirst();
 			Integer b = tmp_pattern.getSecond();
 			ArrayList<Transaction> c = tmp_pattern.getThird();
 			if(utils.isItemInTransaction(i, a)){
 				Pair<Transaction, Integer> tmpair = new Pair<Transaction, Integer>(a,b);
 				if(F.get(1)==null){
 					ArrayList<Pair<Transaction, Integer>> tmplist = new ArrayList<Pair<Transaction, Integer>>();
 					tmplist.add(tmpair);
 					F.put(1, tmplist);
 				}
 				else{
 					ArrayList<Pair<Transaction, Integer>> tmplist = F.get(1);
 					tmplist.add(tmpair);
 					F.put(1, tmplist);
 				}
 			}
 		}
 		int counter = 2;
 		while(L.get(counter-1)!=null){
 			for(Patterns<Transaction, Integer, ArrayList<Transaction>> element: L.get(counter-1)){
 				if(element != null){
 					Transaction pattern = (Transaction) element.getFirst();
 					Integer count = (Integer) element.getSecond();
 					ArrayList<Transaction> projDB = (ArrayList<Transaction>) element.getThird();
 					
 					ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>> result = ps.prefixSpan(pattern, projDB, M, min_sup, Lcount);
 					if(!result.isEmpty()){
 						for(Patterns<Transaction, Integer, ArrayList<Transaction>> item : result){
 							if(L.get(counter)==null){
 								ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>> arrTmp = new ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>>();
 								arrTmp.add(item);
 								L.put(counter, arrTmp);
 							}
 							else{
 								ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>> arrTmp = L.get(counter);
 								arrTmp.add(item);
 								L.put(counter, arrTmp);
 							}
 						}
 					}
 				}
 			}
 			if(L.get(counter)!=null){
 				for(Patterns<Transaction, Integer, ArrayList<Transaction>> item:L.get(counter)){
 					Transaction a = item.getFirst();
 					Integer b = item.getSecond();
 					ArrayList<Transaction> c = item.getThird();
 					if(utils.isItemInTransaction(i, a)){
 						Pair<Transaction, Integer>tmpair = new Pair<Transaction, Integer>(a,b);
 						if(F.get(counter)==null){
 							ArrayList<Pair<Transaction, Integer>> arrTmp = new ArrayList<Pair<Transaction, Integer>>();
 							arrTmp.add(tmpair);
 							F.put(counter, arrTmp);
 						}
 						else{
 							ArrayList<Pair<Transaction, Integer>> arrTmp = F.get(counter);
 							arrTmp.add(tmpair);
 							F.put(counter, arrTmp);
 						}
 					}
 				}
 			}
 			counter++;
 		}
 		return F;
 	}
 	public static void main(String [ ] args) throws IOException{	
 		//Data Initialization
		String dataFile = "/Users/edmond/Downloads/cse514-msprefixspan/src/data1.txt";
		String paraFile = "/Users/edmond/Downloads/cse514-msprefixspan/src/para1.txt";
 		MS_PS msps = new MS_PS(paraFile, dataFile);	
 		for(int i=0; i < msps.T.size(); i++){
 			Transaction tran = msps.T.get(i);
 			tran.print();
 		}
 		for(Integer id : msps.MIS.keySet()){
 			System.out.println("MIS("+ id + "):" + msps.MIS.get(id));
 		}
 		System.out.println("SDC=" + msps.SDC);
 		util utils = new util();
 		
 		ArrayList<Integer> M = utils.sortMIS(msps.MIS);
 		System.out.print("M: {");
 		for(int i=0; i<M.size();i++){
 			System.out.print(M.get(i));
 			if(i == M.size()-1)
 				System.out.println("}");
 			else
 				System.out.print(",");
 		}
 		
 		System.out.println("N: "+msps.N);
 		int n = msps.N;
 		HashMap<Integer, Integer>L = utils.contFreq(msps.MIS, msps.T);
 		System.out.print("L: {");
 		for(Integer key: L.keySet()){
 			System.out.print(key+":"+L.get(key)+" ");
 		}
 		System.out.println("}");
 		HashMap<Integer, Integer>freqL = utils.findFreqL(msps.MIS, L, M, msps.N);
 		System.out.print("FreqL: {");
 		for(Integer key: freqL.keySet()){
 			System.out.print(key+":"+freqL.get(key)+" ");
 		}
 		System.out.println("}");
 		
 		HashMap<Integer, Float> freqMIS = new HashMap<Integer, Float>();
 		for(Integer key: freqL.keySet()){
 			freqMIS.put(key, msps.MIS.get(key));
 		}
 		ArrayList<Integer> freqM = utils.sortMIS(freqMIS);
 		System.out.print("freqM: {");
 		for(int i=0; i<freqM.size(); i++){
 			System.out.print(freqM.get(i));
 			if(i == freqM.size()-1)
 				System.out.println("}");
 			else
 				System.out.print(",");
 		}
 		
 		System.out.println("*******************************");
 		int N = msps.N;
 		HashMap<Integer, ArrayList<Pair<Transaction, Integer>>> F = new HashMap<Integer, ArrayList<Pair<Transaction, Integer>>>();
 		HashMap<Integer, ArrayList<Transaction>> S = new HashMap<Integer, ArrayList<Transaction>>();
 		for(int index=1; index < 11; index++)
 			F.put(index, null);
 		for(int index=0; index<freqM.size(); index++){
 			Integer i = freqM.get(index);
 			ArrayList<Transaction> tmpList = new ArrayList<Transaction>();
 			S.put(i, tmpList);
 			for(Transaction tran: msps.T){
 				if(utils.isItemInTransaction(i, tran)){
 					ArrayList<Transaction> tmp = S.get(i);
 					tmp.add(tran.copy());
 					S.put(i, tmp);
 /*					ArrayList<Transaction> tmp = new ArrayList<Transaction>();
 					for(int i_=0; i_< S.get(i).size(); i_++){
 						tmp.add(S.get(i).get(i_).copy());
 					}
 					(ArrayList<Transaction>) S.get(i).clone();
 					tmp.add(tran);
 					S.put(i,tmp);*/
 				}
 			}
 			
 		/*	for(int _i=0; _i < msps.T.size(); _i++){
 				Transaction tran = msps.T.get(_i);
 				tran.print();
 			}
 			System.out.print("***\n");*/
 			
 			S.put(i, utils.removej(S.get(i), i, L, SDC, N));
 			//r-PrefixSpan
 			if(!S.get(i).isEmpty())
 				F = r_PrefixSpan(i, S.get(i), n, (int) Math.ceil(MIS.get(i)*n), freqM, M, MIS, F, L);
 			msps.T = util.removei(msps.T,i);
 			/*int index_=1;
 			while(F.get(index_)!=null){
 				System.out.print("The number of length "+index_+" sequential patterns is " + F.get(index_).size() + "\n");
 				for(Pair<Transaction, Integer> item : F.get(index_)){
 					Transaction a = item.getFirst();
 					Integer b = item.getSecond();
 					a.print();
 					System.out.print("Count: " + b + "\n");
 				}
 				System.out.println();
 				index_++;
 			}
 			System.out.print("?????????????\n");*/
 		}
 		System.out.println("*******************************");
 		
 		int index=1;
 		while(F.get(index)!=null){
 			System.out.print("The number of length "+index+" sequential patterns is " + F.get(index).size() + "\n");
 			for(Pair<Transaction, Integer> item : F.get(index)){
 				Transaction a = item.getFirst();
 				Integer b = item.getSecond();
 				a.print();
 				System.out.print("Count: " + b + "\n");
 			}
 			System.out.println();
 			index++;
 		}
 	}
 
 }
