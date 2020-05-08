 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class MSPrefixSpan {
 	public ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>> gen_l1patterns(ArrayList<Transaction> S, ArrayList<Integer> F, HashMap<Integer, Float> MIS, Integer n, 
 			Integer min_sup, HashMap<Integer, Integer> Lcount){
 		//return a list of length-1  pattern
 		ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>>LiPattern = new ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>>();
 		for(Integer item : F){
 			int count = 0;
 			Transaction tmpTrans = new Transaction();
 			ItemSet tmpIs = new ItemSet();
 			tmpIs.items.add(item);
 			tmpTrans.itemSets.add(tmpIs);
 			ArrayList<Transaction> projBD = new ArrayList<Transaction>();
 			for(Transaction tran: S){
 				//print transaction
 				util utils = new util();
 				Pair<Integer, Integer> pair = utils.isItemInElement(item, tran);
 				if(pair.getFirst()!=-1 && pair.getSecond()!=-1)
 				{
 					count++;
 					Transaction clone = (Transaction) tran.copy();
 					Transaction t = clone;
 					for(int num=0;num<pair.getFirst();num++)
 						t.itemSets.remove(0);
 					ArrayList<Integer> clone2 = (ArrayList<Integer>) t.itemSets.get(0).items.clone();
 					ArrayList<Integer> items = clone2;
 					for(int num=0; num<pair.getSecond();num++)
 						t.itemSets.get(0).items.remove(0);
 					if(t.itemSets.get(0).items.size()==1){
 						if(t.itemSets.size()==1)
 							t.itemSets.clear();
 						else
 							t.itemSets.remove(0);
 					}else{
 						t.itemSets.get(0).items.set(0, -1);
 					}
 					if(!t.itemSets.isEmpty()){
 						int i=0;
 						int j=0;
 						while(i<t.itemSets.size()){
 							while(j<t.itemSets.get(i).items.size()){
 								Integer tmp_item = t.itemSets.get(i).items.get(j);
 								if(tmp_item != -1 && ((float)Lcount.get(tmp_item)) < min_sup){
 									if(t.itemSets.get(i).items.size()==1){
 										t.itemSets.remove(i);
 										i-=1;
 										break;
 									}
 									else{
 										t.itemSets.get(i).items.remove(j);
 										j-=1;
 									}
 								}
 								j+=1;
 							}
 							i+=1;
 						}
 						if(!t.itemSets.isEmpty())
 							if(t.itemSets.size() > 1 || t.itemSets.get(0).items.size() >1 || t.itemSets.get(0).items.get(0) != -1)
 								projBD.add(t);						
 					}
 				}	
 			}
 			Patterns<Transaction, Integer, ArrayList<Transaction>> patterns = new Patterns<Transaction, Integer, ArrayList<Transaction>>(tmpTrans, count, projBD);
 			LiPattern.add(patterns);
 		}
 		return LiPattern;
 	}
 	
 	
 	public ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>> prefixSpan(Transaction pattern, ArrayList<Transaction> projDB, ArrayList<Integer> F, Integer min_sup,HashMap<Integer, Integer> Lcount){
 		//nextSeqPattern
 		ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>>output_Patterns = new ArrayList<Patterns<Transaction, Integer, ArrayList<Transaction>>>();
 		util utils = new util();
 		for(Integer item : F){
 			int count=0;
 			int count1=0;
 			int count2=0;
 			ArrayList<Transaction> newProjDB1 = new ArrayList<Transaction>();
 			ArrayList<Transaction> newProjDB2 = new ArrayList<Transaction>();
 
 			for(Transaction tran: projDB){
 				if (tran == null || tran.itemSets.size()==0)
 					continue;
 				
 				
 				ItemSet tmp = pattern.getLastItemSet().copy();
 				if(tran.getFirstItem()==-1)
 					continue;
 				Pair<Integer, Integer> pair = utils.isItemInElement(item, tran);
 				if(pair.getFirst()!=-1&&pair.getSecond()!=-1){
 					count++;
 					Transaction t = tran.copy();
 					for(int num=0;num<pair.getFirst();num++)
 						t.itemSets.remove(0);
 					ArrayList<Integer> clone2 = (ArrayList<Integer>) t.itemSets.get(0).items.clone();
 					ArrayList<Integer> items = clone2;
 					for(int num=0; num<pair.getSecond();num++)
 						t.itemSets.get(0).items.remove(0);
 					if(t.itemSets.get(0).items.size()==1){
 						if(t.itemSets.size()==1)
 							t.itemSets.clear();
 						else
 							t.itemSets.remove(0);
 					}else{
 						t.itemSets.get(0).items.set(0, -1);
 					}
 					if(!t.itemSets.isEmpty()){
 						int i=0;
 						int j=0;
 						while(i<t.itemSets.size()){
 							while(j<t.itemSets.get(i).items.size()){
 								Integer tmp_item = t.itemSets.get(i).items.get(j);
 								if(tmp_item != -1 && ((float)Lcount.get(tmp_item)) < min_sup){
 									if(t.itemSets.get(i).items.size()==1){
 										t.itemSets.remove(i);
 										i-=1;
 										break;
 									}
 									else{
 										t.itemSets.get(i).items.remove(j);
 										j-=1;
 									}
 								}
 								j += 1;
 							}
 							i+=1;
 						}
 						if(!t.itemSets.isEmpty())
 							if(t.itemSets.size() > 1 || t.itemSets.get(0).items.size() > 1 || t.itemSets.get(0).items.get(0) != -1)
 								newProjDB2.add(t);
 					}
 					
 					ItemSet patternTmp = pattern.getLastItemSet().copy();
 					Transaction tmpQuery = new Transaction();
 					ItemSet tmpIs = new ItemSet();
 					tmpIs.items.add(item);
 					tmpQuery.itemSets.add(tmpIs);
 					Transaction tmpTran = new Transaction();
 					tmpTran.itemSets.add(patternTmp);
 					Pair<Integer,Integer> pairTmp = utils.isContained(tmpQuery, tmpTran);
					if(pairTmp.getFirst()==-1 && pairTmp.getSecond()==-1)
 					{
 						patternTmp.items.add(item);
 						tmpTran.itemSets.clear();
 						tmpTran.itemSets.add(patternTmp);
 						pairTmp = utils.isContained(tmpTran, tran);
 						if(pairTmp.getFirst()!=-1 && pairTmp.getSecond()!=-1){
 							count1++;
 						}
 						Transaction seqTmp = tran.copy().getItemSetAfterIndexOf(pairTmp.getFirst());
 						seqTmp.itemSets.get(0).deleteFirstNItems(pairTmp.getSecond());
 						if(seqTmp.itemSets.get(0).items.size()==1){
 							if(seqTmp.itemSets.size()==1)
 								seqTmp.itemSets.clear();
 							else
 								seqTmp.deleteFirstNItemSets(1);
 						}
 						else if(seqTmp.itemSets.get(0).items.size()>1){
 						seqTmp.itemSets.get(0).items.set(0, -1);
 						}
 						if(!seqTmp.itemSets.isEmpty()){
 							int i=0;
 							int j=0;
 							while(i < seqTmp.itemSets.size()){
 								while(j<seqTmp.itemSets.get(i).items.size()){
 									if(seqTmp.getItem(i, j)!=-1 && Lcount.get(seqTmp.getItem(i, j))<min_sup){
 										if(seqTmp.getItemSet(i).items.size()==1){
 											seqTmp.delItemSetIndexOf(i);
 											i--;	
 											break;
 										}
 										else{
 											seqTmp.getItemSet(i).delItemOfindex(j);
 											j--;
 										}	
 									}
 									j+=1;
 								}
 								i+=1;
 							}
 							if(!seqTmp.itemSets.isEmpty())
 								if(seqTmp.itemSets.size() > 1 || seqTmp.itemSets.get(0).items.size() > 1 || seqTmp.itemSets.get(0).items.get(0) != -1)
 									newProjDB1.add(seqTmp);
 						}
 					}
 				}
 			}
 			//Find all patterns of the form <{30,x}> and <{_,x}>
 			for(Transaction transaction: projDB){
 				if(transaction.isEmpty())
 					continue;
 				Pair<Integer, Integer> pair = utils.isItemInElement(item, transaction);
 				if(pair.getFirst()!=-1&&pair.getSecond()!=-1){
 					if(transaction.getFirstItem()==-1){
 						if(pair.getFirst()==0){
 							count1 ++;
 							Transaction tmpseq = transaction.getItemSetAfterIndexOf(1);
 							Pair<Integer, Integer> pairTmp = utils.isItemInElement(item, tmpseq);
 							if(pairTmp.getFirst()!=-1&&pairTmp.getSecond()!=-1){
 								count2++;
 								Transaction seqtmp = tmpseq.getItemSetAfterIndexOf(pairTmp.getFirst());
 								seqtmp.itemSets.get(0).deleteFirstNItems(pairTmp.getSecond());
 								if(seqtmp.itemSets.get(0).items.size()==1){
 									if(seqtmp.itemSets.size()==1){
 										seqtmp.itemSets.clear();
 									}
 									else
 										seqtmp.deleteFirstNItemSets(1);
 								}
 								else if(seqtmp.itemSets.get(0).items.size()>1){
 									seqtmp.getFirstItemSet().items.set(0, -1);
 								}
 								
 								if(!seqtmp.isEmpty()){
 									int i=0;
 									int j=0;
 									while(i<seqtmp.itemSets.size()){
 										while(j<seqtmp.getItemSet(i).items.size()){
 											if(seqtmp.getItem(i, j)!=-1 && Lcount.get(seqtmp.getItem(i, j))<min_sup){
 												if(seqtmp.getItemSet(i).items.size()==1){
 													seqtmp.delItemSetIndexOf(i);
 													i--;
 												}
 												else{
 													seqtmp.getItemSet(i).delItemOfindex(j);
 													j--;
 												}
 											}
 											j++;
 										}
 										i++;
 									}
 									if(!seqtmp.isEmpty())
 										if(seqtmp.itemSets.size()>1 || seqtmp.getItemSet(0).items.size()>1 || seqtmp.getFirstItem()!= -1)
 											newProjDB2.add(seqtmp);
 								}
 							}
 						}
 						else{
 							count2++;
 							Transaction patterntmp = new Transaction();
 							patterntmp.itemSets.add(pattern.getLastItemSet().copy());
 							ItemSet isTmp = new ItemSet();
 							isTmp.items.add(item);
 							Transaction tranTmp = new Transaction();
 							tranTmp.itemSets.add(isTmp);
 							Pair<Integer, Integer> pairtmp = utils.isContained(tranTmp, patterntmp);
 							if(pairtmp.getFirst()==-1 && pairtmp.getSecond()==-1){
 								isTmp = patterntmp.itemSets.get(0);
 								isTmp.items.add(item);
 								patterntmp.itemSets.clear();
 								patterntmp.itemSets.add(isTmp);
 								pairtmp.set(utils.isContained(patterntmp, transaction));
 								if(pairtmp.getFirst()!=-1 && pairtmp.getSecond()!=-1)
 									count1++;
 								Transaction tmpseq = transaction.copy();
 								Transaction seqtmp = tmpseq.getItemSetAfterIndexOf(pairtmp.getFirst());
 								ItemSet isTmp_ = seqtmp.getItemSet(0);
 								isTmp_.deleteFirstNItems(pairtmp.getSecond());
 								seqtmp.itemSets.set(0, isTmp_);
 								if(seqtmp.getItemSet(0).items.size()==1){
 									if(seqtmp.itemSets.size() == 1)
 										seqtmp.itemSets.clear();
 									else
 										seqtmp.deleteFirstNItemSets(1);
 								}
 								else if(seqtmp.getItemSet(0).items.size()>1){
 									seqtmp.getFirstItemSet().items.set(0, -1);
 								}
 								
 								if(!seqtmp.isEmpty()){
 									int i=0;
 									int j=0;
 									while(i<seqtmp.itemSets.size()){
 										while(j<seqtmp.getItemSet(i).items.size()){
 											if(seqtmp.getItem(i, j)!=-1 && Lcount.get(seqtmp.getItem(i, j))<min_sup){
 												if(seqtmp.getItemSet(i).items.size()==1){
 													seqtmp.delItemSetIndexOf(i);
 													i--;
 												}
 												else{
 													seqtmp.getItemSet(i).delItemOfindex(j);
 													j--;
 												}
 											}
 											j++;
 										}
 										i++;
 									}
 									if(!seqtmp.isEmpty())
 										if(seqtmp.itemSets.size()>1 || seqtmp.getItemSet(0).items.size()>1 || seqtmp.getFirstItem()!= -1)
 											newProjDB1.add(seqtmp);
 								}
 							}
 						}
 						
 						Transaction seq = transaction.getItemSetAfterIndexOf(pair.getFirst());
 						seq.getItemSet(0).deleteFirstNItems(pair.getSecond());
 						if(seq.getFirstItemSet().items.size() == 1)
 						{
 							if(seq.itemSets.size() == 1){
 								seq.itemSets.clear();
 							}
 							else
 								seq.deleteFirstNItemSets(1);
 						}
 						else
 							seq.getFirstItemSet().items.set(0, -1);
 						if(!seq.isEmpty()){
 							int i=0;
 							int j=0;
 							while(i<seq.itemSets.size()){
 								while(j<seq.getItemSet(i).items.size()){
 									if(seq.getItem(i, j)!=-1 && Lcount.get(seq.getItem(i, j))<min_sup){
 										if(seq.getItemSet(i).items.size()==1){
 											seq.delItemSetIndexOf(i);
 											i--;
 											break;
 										}
 										else{
 											seq.getItemSet(i).delItemOfindex(j);
 											j--;
 										}
 									}
 									j++;
 								}
 								i++;
 							}
 							if(!seq.isEmpty())
 								if(seq.itemSets.size()>1 || seq.getItemSet(0).items.size()>1 || seq.getFirstItem()!= -1){
 									if(pair.getFirst()==0)
 										newProjDB1.add(seq);
 									else
 										newProjDB2.add(seq);
 								}
 						}
 					}
 				}
 			}
 			if (count1 >= min_sup){
 				Transaction pattern2 = pattern.copy();
 				pattern2.getLastItemSet().items.add(item);
 				output_Patterns.add(new Patterns<Transaction, Integer, ArrayList<Transaction>>(pattern2.copy(),count1,(ArrayList<Transaction>)newProjDB1.clone()));
 			}
 			if((count+count2)>=min_sup){
 				Transaction pattern2 = pattern.copy();
 				ItemSet temp = new ItemSet();
 				temp.items.add(item);
 				pattern2.itemSets.add(temp);
 				output_Patterns.add(new Patterns<Transaction, Integer, ArrayList<Transaction>>(pattern2.copy(),count+count2,(ArrayList<Transaction>)newProjDB2.clone()));
 			}
 		}
 		return output_Patterns;
 	}
 }
 
 
