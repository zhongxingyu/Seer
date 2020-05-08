 package algos;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import model.Algorithm;
 import model.CandidateItemset;
 import model.Dataset;
 import model.ItemSet;
 import model.LargeItemset;
 import model.MinSup;
 import model.Transaction;
 import model.aprioritid.CandidateItemsetBar;
 import model.aprioritid.ItemSetBar;
 import util.AprioriUtils;
 import util.DBReader;
 import util.FileReader;
 import util.InputReader;
 
 
 /**
  * Implements the AprioriTid Algorithm for frequent itemset mining.
  * 
  * @author saurabh
  *
  */
 
 public class AprioriTid {
 	
 	private static int MAX_K;
 	
 	public static void main(String[] args)
 	{
 		runExperiment(Dataset.T5_I2_D100K, MinSup.POINT_TWO_FIVE_PERCENT);
 	}
 	
 	/* 
 	 * Given C_bar[k-1] and all itemsets, find C_bar[k].
 	 */
 	private static CandidateItemsetBar generate_C_bar(CandidateItemsetBar C_k_1_bar, ItemSet[] allItemsets, CandidateItemset C_k)
 	{
 		//System.out.println("In generate_C_bar().");
 		CandidateItemsetBar toReturn = new CandidateItemsetBar();
 		
 		for(ItemSetBar itemsetbar : C_k_1_bar.getItemsetbars())	//1 transaction
 		{
 			//System.out.println("In transaction: " + itemsetbar.getTid());
 			ItemSetBar k_itemset_bar = new ItemSetBar();
 			k_itemset_bar.setTid(itemsetbar.getTid());
 			
 			SortedMap<Integer, Integer> kitemset_support = new TreeMap<Integer, Integer>();
 			for(Integer Ck_1_id : itemsetbar.getCandidateItemsetId())
 			{
 				//System.out.println("Itemset in transaction: " + allItemsets[Ck_1_id].getItems());
 				for(Integer Ck_id : allItemsets[Ck_1_id].getExtensions())
 				{
 					//System.out.print("Extension = " + C_k.getItemsets()[Ck_id] + "; ");
 					if(kitemset_support.containsKey(Ck_id))
 					{
 						int support = kitemset_support.get(Ck_id);
 						kitemset_support.put(Ck_id, support + 1);
 						//System.out.println("Support = " + (support + 1));
 					}
 					else
 					{
 						kitemset_support.put(Ck_id, 1);
 						//System.out.println("Support = " + 1);
 					}
 				}
 				
 			}
 			
 			SortedSet<Integer> keys = new TreeSet<Integer>(kitemset_support.keySet());
 			for(Integer key : keys)
 			{
 				if(kitemset_support.get(key) >= 2)
 				{
 					k_itemset_bar.getCandidateItemsetId().add(key);
 					C_k.getItemsets()[key].incrementSupportCount();
 				}
 			}
 			
 			if(k_itemset_bar.getCandidateItemsetId().size() > 0)
 				toReturn.getItemsetbars().add(k_itemset_bar);
 		}
 		
 		return toReturn;
 	}
 
 	/*
 	 * Run AprioriTid algorithm for the specified experiment parameters
 	 */
 	public static int runExperiment(Dataset dataset, MinSup minSup)
 	{
 		System.out.println("AprioriTid: " + dataset + ", " + minSup);
 		
 		long expStartTime = System.currentTimeMillis();
 		
 		InputReader reader = getDatasetReader(dataset);
 		
 		List<Transaction> transactions = new ArrayList<Transaction>();
 		
 		while(reader.hasNextTransaction()) {
 			transactions.add(reader.getNextTransaction());
 		}
 		
 		int minSupportCount = (int)(minSup.getMinSupPercentage() * transactions.size())/100;
 		MAX_K = 400 * dataset.getAvgTxnSize();
 		//System.out.println(MAX_K);
 		
 		LargeItemset[] largeItemsets = new LargeItemset[MAX_K];
 		CandidateItemset[] candidateItemsets = new CandidateItemset[MAX_K];
 		CandidateItemsetBar[] candidateItemsetBars = new CandidateItemsetBar[MAX_K];
 		
 		candidateItemsets[1] = new CandidateItemset(MAX_K);
 		candidateItemsetBars[1] = new CandidateItemsetBar();
 		largeItemsets[1] = new LargeItemset();
 		getInitialCandidateItemsets(transactions, candidateItemsets[1], candidateItemsetBars[1]);
 		getInitialLargeItemsets(candidateItemsets[1], minSupportCount, largeItemsets[1]);
 		//printAll(candidateItemsets[1].getItemsets());
 		//System.out.println("\nk = " + 1);
 		//AprioriUtils.print(largeItemsets[1], candidateItemsets[1].getItemsets());
 		long bottleNeckStartTime = 0;
 		long bottleNeckEndTime = 0;
 		for(int k = 2; largeItemsets[k-1].getItemsetIds().size() != 0; k++)
 		{
 			//System.out.println("\nk = " + k);
 			//System.out.println("1");
 			candidateItemsets[k] = new CandidateItemset(MAX_K);
 			candidateItemsets[k].setItemsets(AprioriUtils.apriori_gen(candidateItemsets[k-1].getItemsets(), largeItemsets[k-1].getItemsetIds(), k - 1));
 			//System.out.println("2");
 			
 			if(k == 2)
 				bottleNeckStartTime = System.currentTimeMillis();
 			candidateItemsetBars[k] = generate_C_bar(candidateItemsetBars[k-1], candidateItemsets[k-1].getItemsets(), candidateItemsets[k]);
 			if(k == 2)
 				bottleNeckEndTime = System.currentTimeMillis();
 			//System.out.println("3");
 			largeItemsets[k] = generateLargeItemsets(candidateItemsets[k], minSupportCount);
 			//AprioriUtils.print(largeItemsets[k], candidateItemsets[k].getItemsets());
 		}
 		
 		long expEndTime = System.currentTimeMillis();
 		int timeTaken = (int)((expEndTime - expStartTime) / 1000); 
		System.out.println("BottleNeck Time taken = " + (int)((bottleNeckEndTime - bottleNeckStartTime) / 1000) + " seconds.");
 		System.out.println("Time taken = " + timeTaken + " seconds.\n");
 		
 		return timeTaken;
 	}
 	
 	private static LargeItemset generateLargeItemsets(CandidateItemset candidateItemset, int minSupportCount) {
 		
 		//System.out.println("In generateLargeItemsets().");
 		
 		LargeItemset largeItemset = new LargeItemset();
 		int index = 0;
 		for(ItemSet itemset : candidateItemset.getItemsets())
 		{
 			if(itemset == null)
 				break;
 			if(itemset.getSupportCount() >= minSupportCount)
 				largeItemset.getItemsetIds().add(index);
 			index++;
 		}
 		
 		return largeItemset;
 	}
 
 	private static void getInitialCandidateItemsets(List<Transaction> transactions, CandidateItemset C, CandidateItemsetBar C_) {
 		//System.out.println("In getInitialCandidateItemsets().");
 		SortedMap<Integer, Integer> itemset_support = new TreeMap<Integer, Integer>();
 		
 		//This loop counts support.
 		for(Transaction t : transactions)
 		{
 			for(Integer i : t.getItems())
 			{
 				if(itemset_support.containsKey(i))
 				{
 					int support = itemset_support.get(i);
 					itemset_support.put(i, support + 1);
 				}
 				else
 				{
 					itemset_support.put(i, 1);
 				}
 			}
 		}
 		
 		//This part sorts and creates candidate itemsets.
 		SortedSet<Integer> keys = new TreeSet<Integer>(itemset_support.keySet());
 		Map<Integer, Integer> itemset_pos = new HashMap<Integer, Integer>(); 
 		int index = 0;
 		for(Integer item : keys)
 		{
 			Integer support = itemset_support.get(item);
 			C.getItemsets()[index] = new ItemSet();
 			C.getItemsets()[index].getItems().add(item);
 			C.getItemsets()[index].setSupportCount(support);
 			
 			itemset_pos.put(item, index);
 			index++;
 		}
 		
 		//This part creates C_.
 		for(Transaction t : transactions)
 		{
 			ItemSetBar itemsetbar = new ItemSetBar();
 			itemsetbar.setTid(t.getTid());
 			for(Integer i : t.getItems())
 			{
 				int pos = itemset_pos.get(i);
 				itemsetbar.getCandidateItemsetId().add(pos);
 			}
 			C_.getItemsetbars().add(itemsetbar);
 		}
 	}
 
 	private static void getInitialLargeItemsets(CandidateItemset C, int minSupportCount, LargeItemset L)
 	{
 		int index = 0;
 		for(ItemSet itemset : C.getItemsets()) {
 			if(itemset == null)
 				break;
 			if(itemset.getSupportCount() >= minSupportCount)
 				L.getItemsetIds().add(index);
 			index++;
 		}
 	}
 	
 	/*
 	 * Gets a iterative reader to the dataset and algorithm corresponding to the current experiment.
 	 */
 	private static InputReader getDatasetReader(Dataset dataset)
 	{
 		return new FileReader(dataset, Algorithm.APRIORI_TID);
 	}
 }
