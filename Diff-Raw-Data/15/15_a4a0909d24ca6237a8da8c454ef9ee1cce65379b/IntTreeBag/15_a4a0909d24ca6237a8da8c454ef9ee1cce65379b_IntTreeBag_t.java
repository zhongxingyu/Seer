 import java.util.ArrayList;
 public class IntTreeBag implements Cloneable
 {
 	private  IntBTNode root;
 	ArrayList<Integer> al = new ArrayList<Integer>(); //in order to store data as an array
 	private int iteration;
 	public IntTreeBag(int rootValue)
 	{//initialize empty bag
 		root = new IntBTNode(rootValue,null,null);
 		iteration = 0;
 	}
 	public void add(int element)
 	{
 		IntBTNode cursor;
 		IntBTNode nodex = new IntBTNode(element, null, null);
 		cursor = root;
 		boolean done = false;
 		while(!done)
 		{
 			if(element < cursor.getData())
 			{
 				if(cursor.getLeft() == null)
 				{
 					cursor.setLeft(nodex);
 					done = true;
 				}
 				else
 				{
 					cursor = cursor.getLeft();
 				}
 			}//outer if
 			else
 			{
 				if(cursor.getRight()==null)
 				{
 					cursor.setRight(nodex);
 					done = true;
 				}
 				else
 				{
 					cursor = cursor.getRight();
 				}
 			}//outer else
 		}//while
 	}//add
 	public void addAll(IntTreeBag addend)
 	{//add
 		int addedData[] = new int [addend.size()];
 		addedData = addend.getAllDatawihtArray();
 		for(int index = 0; index < addedData.length; index++)
 		{
 			this.add(addedData[index]);
 		}
 	}
 	
 	public IntTreeBag clone()
 	{
 		/*IntTreeBag copiedBag = new IntTreeBag(this.root.getData());
 		copied = IntBTNode.treeCopy(root);
 		return copied;
 		*/
 		IntTreeBag copiedBag = new IntTreeBag(this.root.getData());
 		int allData[] = this.getAllDatawihtArray();
 		for(int index = 1; index < allData.length; index++)
 		{
 			copiedBag.add(allData[index]);
 		}
 		return copiedBag;
 	}
 	
 	public int countOccurrences(int target)
 	{
 		int [] allData = getAllDatawihtArray();
 		int occurrence = 0;
 		for(int walker = 0; walker < this.size();walker++)
 		{
 			if(target == allData[walker])
 			{
 				occurrence++;
 			}
 		}
 		
 		return occurrence;
 	}
 	
 	private boolean remove(int target)
 	{
 		IntBTNode cursor;
 		IntBTNode previousCursor;
 		cursor = root;
 		previousCursor = root;
 		boolean done = false;
 		while(!done)
 		{
 			if(target < cursor.getData())
 			{//target is in left side
 				if(cursor.getLeft() == null)
 				{//check if cursor's left side is leaf or not
 					return false;
 				}
 				else
 				{
 					previousCursor = cursor;
 					cursor = cursor.getLeft();
 				}
 				
 			}
 			else
 			{//target is in right side or equal
 				if(target == cursor.getData())
 				{//find target
 					if(cursor.getLeft() == null && cursor.getRight() == null)
 					{//true then it is leaf node
 						if(previousCursor.getLeft() == cursor)
 						{//if target is on the left side of previous cursor
 							previousCursor.setLeft(null);
 						}
 						else
 						{//if target is on the right side of previous cursor
 							previousCursor.setRight(null);
 						}
 					}
 					else
 					{//this is an intermediate node
 						if(previousCursor.getLeft() == cursor)
 						{
 							previousCursor.setLeft(cursor.getRight());
 							cursor.getRight().getLeftmost().setLeft(cursor.getLeft());
 						}
 						else
 						{
 							previousCursor.setRight(cursor.getLeft());
 							cursor.getLeft().getRightmost().setLeft(cursor.getLeft());
 						}
 						
 					}
 					return true;
 				}
 				else
 				{//target > cursor.getData()
 					if(cursor.getRight()== null)
 					{
 						return false;
 					}
 					else
 					{
 						previousCursor = cursor;
 						cursor = cursor.getRight();
 					}
 				}
 			}//outter else
 		}//while
 		return false;
 	}//remove
 	
 	public int size()
 	{
 		return IntBTNode.treeSize(root);
 	}
 	/*
 	public static IntTreeBag union(IntTreeBag b1, IntTreeBag b2)
 	{//combine
 		
 	}
 	*/
 	public void print(int depth)
 	{
 		root.print(depth);
 	}
 	/*
 	private ArrayList<Integer> traverse(IntBTNode root)
 	{
 		IntBTNode cursor;
 		cursor = root;
 		if(cursor.getLeft() != null)
 		{
 			al.add(cursor.getData());
 			cursor = cursor.getLeft();
 			traverse(cursor);
 		}
 		if(cursor.getRight() != null)
 		{
 			al.add(cursor.getData());
 			cursor = cursor.getLeft();
 			traverse(cursor);
 		}
 		return al;
 	}
 	*/
 	private int traverse(IntBTNode root)
 	{
 		int res_data;
 		res_data = root.getData();
 		al.add(res_data);
 		if(root.getLeft() != null)
 		{
 			traverse(root.getLeft());
 		}
 		if(root.getRight() != null)
 		{
 			traverse(root.getRight());
 		}
 		return res_data;
 	}
 	public ArrayList<Integer> getAlldata()
 	{
 		al = new ArrayList();
 		traverse(root);
 		return al;
 	}
 	public int[] getAllDatawihtArray()
 	{
 		/*Integer itemArray[] = new Integer [al.size()];
 		Integer temporalyArray[] = al.toArray(itemArray);
 		int alldata[] = new int [al.size()];
 		for(int i = 0; i < al.size(); i++)
 		{
 			alldata[i] = al.get(i).intValue();
 		}
 		return alldata;
 		*/
		al = new ArrayList();
		traverse(root);
 		int alldata[] = new int [al.size()];
 		for(int i = 0; i < al.size(); i++)
 		{
 			alldata[i] = al.get(i);
			System.out.println(alldata[i]);
 		}
 		return alldata;
 	}//getAllDatawihtArray
 	/*
 	public boolean contains(int target)
 	{
 		int index; 
 		boolean duplicate = false;
 		for(index = 0; (index < al.size())&&(target != ))
 		if()
 	}
 	*/
 	private int getNext()
 	{
 		iteration 
 		
 		return 
 	}
 	
 
 }
