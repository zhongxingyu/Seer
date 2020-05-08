 import java.util.ArrayList;
 
 import javax.naming.spi.DirStateFactory.Result;
 public class Transaction {
 	public ArrayList<ItemSet> itemSets;
 	public int count;
 	
 	Transaction(){
 		itemSets = new ArrayList<ItemSet>();
 		count = 0;
 	}
 	
 	//return all items
 	public ArrayList<ItemSet> getItems(){
 		ArrayList<ItemSet> res = new ArrayList<ItemSet>();
 		for(int i = 0; i < itemSets.size(); i++){
 			res.add(itemSets.get(i));
 		}
 		return res;
 	}
 	
 	public Integer getFirstItem(){
 		//return the id of the first item in this transaction
 		return itemSets.get(0).items.get(0);
 	}
 	
 	public Integer getLastItem(){
 		//return the id of the last item in this transaction
 		ArrayList<Integer> set = itemSets.get(itemSets.size()-1).items;
 		return set.get(set.size()-1);
 	}
 	
 	public ItemSet getLastItemSet(){
 		return itemSets.get(itemSets.size()-1);
 	}
 	
 	public ItemSet getFirstItemSet(){
 		return itemSets.get(0);
 	}
 	public Transaction copy(){
 		//return a copy of this Transaction
 		Transaction cp = new Transaction();
 		for(int i=0; i<this.itemSets.size(); i++){
 			ItemSet set = new ItemSet();
 			set.items.addAll(this.itemSets.get(i).items);
 			cp.itemSets.add(set);
 		}
 		return cp;
 	}
 	public void print(){
 		//print out everything contained in this transaction
 		System.out.printf("<");
 		for(int i=0; i < itemSets.size(); i++){
 			System.out.printf("{");
 			ItemSet set = itemSets.get(i);
 			for(int j=0; j < set.items.size() - 1;j++){
 				System.out.printf(set.items.get(j)+",");
 			}
 			System.out.printf(set.items.get(set.items.size()-1)+"}");
 		}
 		System.out.println(">");
 	}
 	
 	public boolean isContained(Transaction trans){
 		boolean result = true;
 		int m = this.itemSets.size();
 		int n = trans.itemSets.size();
 		int j = 0;
 		for(int i=0; i<m; i++){
 			ItemSet set = this.itemSets.get(i);
 			do{
 				if(m-i>n-j){
 					result = false;
 					break;
 				}
 				if (set.isSubset(trans.itemSets.get(j))){
 					j++;
 					break;
 				}
 				j++;
 			}while(j<n);
 			if(result==false)
 				break;
 			if(i==m-1&&j==n)
 				return set.isSubset(trans.itemSets.get(j-1));
 		}
 		return result;
 	}
 	
 	public boolean equals(Object transaction){
 		Transaction trans = (Transaction) transaction;
 		boolean result;
 		if(this.isContained(trans)&&this.itemSets.size()==trans.itemSets.size()&&this.getItems().size()==trans.getItems().size())
 			result = true;
 		else
 			result = false;
 		return result;
 	}
 	
 	public Transaction getItemSetAfterIndexOf(int index){
 		Transaction res = this.copy();
		if(index >= 0)
			for(int i=0; i<index; i++)
				res.itemSets.remove(0);
		else{
			for(int i=0; i< this.itemSets.size()+index; i++)
				res.itemSets.remove(0);
		}
 		return res;
 	}
 	
 	public void deleteFirstNItemSets(int index){
 		for(int i=0; i<index; i++){
 			this.itemSets.get(0).items.clear();
 			this.itemSets.remove(0);
 		}
 	}
 	
 	public Integer getItem(int pos, int ind){
 		return this.itemSets.get(pos).items.get(ind);
 	}
 	
 	public ItemSet getItemSet(int pos){
 		return this.itemSets.get(pos);
 	}
 	public void delItemSetIndexOf(int pos){
 		this.itemSets.get(pos).items.clear();
 		this.itemSets.remove(pos);
 	}
 	public boolean isEmpty(){
 		if(this.itemSets.size()>0)
 			return false;
 		else
 			return true;
 	}
 	
 }
