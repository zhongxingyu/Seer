 //ChainedTable.java
 //ChainedTable is a chained hash table
 
 public class ChainedTable<K, E>
 {
 	//For index i, the object in table[i] is head reference for a linked 
 	//of all the elements for which hash(key) is i
 	private Object [] table;
 	private int manyItems = 0;
 	private int manyComp = 0;
 	private int manySearch = 0;
 	
 	public ChainedTable(int tableSize)
 	{
 		if(tableSize <= 0 )
 		{
 			throw new IllegalArgumentException("Table size must be positive");
 		}
 		table = new Object[tableSize];
 	}

 	public boolean containsKey(K key)
 	{
 		int index = hash(key);
 		boolean containing = false;
 		ChainedHashNode <K,E> cursor;
 		ChainedHashNode<K,E> head;
 		head = (ChainedHashNode<K, E>) table[index];
 		System.out.println("about to look for whether the given key contains");
 		for(cursor = head; cursor != null; cursor = cursor.link)
 		{
 			System.out.println("---searching---");
 			if(cursor.key.equals(key))
 			{//can I compare the way? 
 				containing = true;
 			}
 		}
 		
 		
 		return containing;
 	}
 	public E get(K key)
 	{
 		manySearch++;
 		ChainedHashNode<K,E> cursor =null;
 		int index = hash(key);
 
 		if (table[index]!=null)
 		{
 			manyComp++;
 			cursor = (ChainedHashNode<K,E>) table[index];
 			while(cursor!=null)
 			{
 				manyComp++;
 				if ( cursor.key.equals(key) )
 				{
 					return cursor.elements;
 				}else
 				{
 					cursor=cursor.link;
 				}
 			}
 			
 			return null;   // same index, but no same key
 		}else
 		{
 			// no index
 			return null;
 		}
 	}	
 	
 	private int hash(K key)
 	{//the return value is a valid index of the ChainedTable's table
 	//the index is calculated as the remainder 
 	//when the absolute value of the key's hash code is divided by the size of the ChainedTable's  table
 		return Math.abs(key.hashCode()) % table.length;
 	}
 	@SuppressWarnings ("unchecked")
 	public E put(K key, E elements)
 	{
 		ChainedHashNode<K,E> cursor =null;
 		E answer = null;
 		int index;
 		if(key == null || elements == null)
 		{
 			throw new NullPointerException("Key or element is null");
 		}
 		//must write code here to set cursor to refer to the node 
 		//that already contains the specifies node.
 		//if there is no such node, then cursor is set to null
 		index = hash(key);
 		//cursor = (ChainedHashNode<K, E>) table[index];
 		if(table[index] != null)
 		{
 			cursor = (ChainedHashNode<K,E>) table[index];
 			while(cursor != null)
 			{
 				cursor = cursor.link;
 				//cursor = (ChainedHashNode<K,E> table[index]).cursor.link;
 			}
 		}
 		else
 		{
 			cursor = null;
 		}
 		
 		if(cursor == null)
 		{//add a new node at the front of the list of table[hash(key)]
 			int i = hash(key);
 			cursor = new ChainedHashNode<K,E> ();
 			cursor.elements = elements;
 			cursor.key = key; 
 			cursor.link = (ChainedHashNode<K,E>) table[i];
 			table[i] = cursor;
  
 		}
 		else
 		{
 			answer = cursor.elements;
 			cursor.elements = elements;
 		}
 		manyItems++;
 		return answer;
 	}
 	public E remove(K key)
 	{
 		ChainedHashNode<K,E> cursor =null;
 		ChainedHashNode<K,E> prev_cursor=null;
 		
 		int index = hash(key);
 		if (table[index]!=null)
 		{
 			prev_cursor = null;
 			cursor = (ChainedHashNode<K,E>) table[index];
 			while(cursor!=null)
 			{
 				if ( cursor.key.equals(key) )
 				{
 					if (prev_cursor != null)
 					{
 						prev_cursor.link = cursor.link;
 						
 					}else
 					{
 						table[index]=null;   //this node is first one
 					}
 					manyItems--;
 					return cursor.elements;
 				}else
 				{
 					prev_cursor = cursor;
 					cursor=cursor.link;
 				}
 			}
 			manyItems--;
 			return null;   // same index, but no same key
 		}else
 		{
 			// no index
 			manyItems--;
 			return null;
 		}
 	}//remove
 	public double estimateEfficiency()
 	{
 		double estimate;
 		estimate = 1 + (manyItems*0.5/table.length);
 		return estimate;
 	}
 	
 	public double actualEfficiency()
 	{
 		double actual;
 		actual = (manyComp * 1.0) / manySearch;
 		return actual;
 	}
 	
 	@SuppressWarnings ("unchecked")
 	public void printTable()
 	{
 		ChainedHashNode <K,E> cursor = null;
 		for(int i = 0; i < table.length; i++)
 		{
 			System.out.printf("\n %d:",i);
 			if(table[i] != null)
 			{
 				cursor = (ChainedHashNode<K,E>) table[i];
 				while(cursor != null)
 				{
 					System.out.printf("{key: %s \nElement: %s}\n", cursor.key.toString(), cursor.elements.toString());
 					cursor = cursor.link;
 				}
 			}
 			else
 			{
 				System.out.print("No entry");
 			}
 			//System.out.println("\n\nend");
 		}
 		
 	}
 	
 }//class
