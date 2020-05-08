 package benchmark;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import vEB.VEBTree;
 import cuckooHash.Cuckoo;
 
 public class BenchmarkDictionary
 {
 	public static int universeSize = 4194304;
 	public static String insertFile = "insertIntegers.txt";
 	public static String deleteFile = "deleteIntegers.txt";
 	
 	public static void main(String[] args) 
 	{
 		System.out.println(System.getProperty("sun.arch.data.model") );
 		System.out.println(Runtime.getRuntime().maxMemory());
 		benchmarchDataStructures();
 	}
 
 	
 	public static void benchmarchDataStructures()
 	{
 		writeTestFiles();
 		
 		/* Run benchmarks */
 		benchmarkVEBTree();
 		benchmarkCuckooHash();
 		benchmarkBinaryTree();
 		benchmarkLinkedList();
 	}
 	
 	public static void writeTestFiles()
 	{
 		try
 		{
 			BufferedWriter bw = new BufferedWriter(new FileWriter(insertFile));
 			BufferedWriter deleteBR = new BufferedWriter(new FileWriter(deleteFile));
 			
 			ArrayList<Integer> list = new ArrayList<Integer>();
 			
 			for(int i = 0; i < universeSize; i=i+2)
 			{
 				list.add(i);
 			}
 			
 			/* Insert Order */
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			
 			for(int i = 1; i <= list.size(); i++)
 			{
 				if(i % 100 == 0)
 				{
 					bw.newLine();
 				}
 				bw.write(list.get(i-1)+" ");
 			}
 			
 			bw.close();
 			
 			/* Delete Order */
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			Collections.shuffle(list);
 			
 			for(int i = 1; i <= list.size(); i++)
 			{
 				if(i % 100 == 0)
 				{
 					deleteBR.newLine();
 				}
 				deleteBR.write(list.get(i-1)+" ");
 			}
 			
 			deleteBR.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void benchmarkVEBTree()
 	{		
 		try
 		{
 			System.out.println("*** vEB Benchmark for universe of size "+universeSize+" ***");
 			
 			BufferedReader insertReader = new BufferedReader(new FileReader(insertFile));
 			BufferedReader deleteReader = new BufferedReader(new FileReader(deleteFile));
 			String lineIn = "";
 			
 			/* initialize data structure */
 			long startTime = System.currentTimeMillis();
 			VEBTree vEBTree =  VEBTree.createVEBTree(universeSize);
 			System.out.println("Init time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************* */
 			
 			/* insert into data structure */
 			startTime = System.currentTimeMillis();
 			while ((lineIn = insertReader.readLine()) != null)
 			{
 				StringTokenizer st = new StringTokenizer(lineIn);
 				while(st.hasMoreTokens())
 				{
 					vEBTree.insert(Integer.parseInt(st.nextToken()));
 				}
 			}
 			System.out.println("Insert time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************** */
 			
 			/* search the entire universe */
 			startTime = System.currentTimeMillis();
 			for(int i = 0; i < universeSize; i++)
 			{
 				vEBTree.search(i);
 			}
 			System.out.println("Search time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* **************************** */
 			
 			/* predecessor benchmark */
 			startTime = System.currentTimeMillis();
 			for(int i = 0; i < universeSize; i++)
 			{
 				vEBTree.predecessor(i);
 			}
 			System.out.println("Predecessor time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ********************* */
 			
 			/* delete all elements */
 			startTime = System.currentTimeMillis();
 			while ((lineIn = deleteReader.readLine()) != null)
 			{
 				StringTokenizer st = new StringTokenizer(lineIn);
 				while(st.hasMoreTokens())
 				{
 					vEBTree.delete(Integer.parseInt(st.nextToken()));
 				}
 			}
 			System.out.println("Delete time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ********************** */
			
			insertReader.close();
			deleteReader.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void benchmarkCuckooHash()
 	{
 		try
 		{
 			System.out.println("\n*** CuckooHash benchmark ***");
 			
 			BufferedReader insertReader = new BufferedReader(new FileReader(insertFile));
 			String lineIn = "";
 			
 			/* initialize data structure */
 			long startTime = System.currentTimeMillis();
 			Cuckoo<Integer> cuckooHash =  new Cuckoo<Integer>();
 			System.out.println("Init time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************* */
 			
 			/* insert into data structure */
 			startTime = System.currentTimeMillis();
 			while ((lineIn = insertReader.readLine()) != null)
 			{
 				StringTokenizer st = new StringTokenizer(lineIn);
 				while(st.hasMoreTokens())
 				{
 					cuckooHash.add(Integer.parseInt(st.nextToken()));
 				}
 			}
 			System.out.println("Insert time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************** */
 			
 			/* search in the data structure */
 			startTime = System.currentTimeMillis();
 			for(int i = 0; i < universeSize; i++)
 			{
 				cuckooHash.contains(i);
 			}
 			System.out.println("Search time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* **************************** */
			
			insertReader.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void benchmarkBinaryTree()
 	{
 		try
 		{
 			System.out.println("\n*** Balanced Binary Tree Benchmark ***");
 			
 			BufferedReader insertReader = new BufferedReader(new FileReader(insertFile));
 			BufferedReader deleteReader = new BufferedReader(new FileReader(deleteFile));
 			String lineIn = "";
 			
 			/* initialize data structure */
 			long startTime = System.currentTimeMillis();
 			TreeSet<Integer> binaryTree =  new TreeSet<Integer>();
 			System.out.println("Init time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************* */
 			
 			/* insert into data structure */
 			startTime = System.currentTimeMillis();
 			while ((lineIn = insertReader.readLine()) != null)
 			{
 				StringTokenizer st = new StringTokenizer(lineIn);
 				while(st.hasMoreTokens())
 				{
 					binaryTree.add(Integer.parseInt(st.nextToken()));
 				}
 			}
 			System.out.println("Insert time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************** */
 			
 			/* search in the data structure */
 			startTime = System.currentTimeMillis();
 			for(int i = 0; i < universeSize; i++)
 			{
 				binaryTree.contains(i);
 			}
 			System.out.println("Search time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* **************************** */
 			
 			/* predecessor benchmark */
 			startTime = System.currentTimeMillis();
 			for(int i = 0; i < universeSize; i++)
 			{
 				binaryTree.lower(i);
 			}
 			System.out.println("Predecessor time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ********************* */
 			
 			/* delete all elements */
 			startTime = System.currentTimeMillis();
 			while ((lineIn = deleteReader.readLine()) != null)
 			{
 				StringTokenizer st = new StringTokenizer(lineIn);
 				while(st.hasMoreTokens())
 				{
 					binaryTree.remove(Integer.parseInt(st.nextToken()));
 				}
 			}
 			System.out.println("Delete time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ********************** */
			
			insertReader.close();
			deleteReader.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void benchmarkLinkedList()
 	{
 		try
 		{
 			System.out.println("\n*** Linked List Benchmark ***");
 			
 			BufferedReader insertReader = new BufferedReader(new FileReader(insertFile));
 			BufferedReader deleteReader = new BufferedReader(new FileReader(deleteFile));
 			String lineIn = "";
 			
 			/* initialize data structure */
 			long startTime = System.currentTimeMillis();
 			LinkedList<Integer> linkedList =  new LinkedList<Integer>();
 			System.out.println("Init time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************* */
 			
 			/* insert into data structure */
 			startTime = System.currentTimeMillis();
 			while ((lineIn = insertReader.readLine()) != null)
 			{
 				StringTokenizer st = new StringTokenizer(lineIn);
 				while(st.hasMoreTokens())
 				{
 					linkedList.add(Integer.parseInt(st.nextToken()));
 				}
 			}
 			System.out.println("Insert time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* ************************** */
 			
 			/* search in the data structure */
 			startTime = System.currentTimeMillis();
 			for(int i = 0; i < universeSize; i++)
 			{
 				linkedList.contains(i);
 			}
 			System.out.println("Search time = "+(System.currentTimeMillis()-startTime)+"ms.");
 			/* **************************** */
 			
 			/* delete all elements */
 			/* this took much too long, removed test */
 			/* ********************** */
			
			insertReader.close();
			deleteReader.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}	
 }
 
 
 
 
