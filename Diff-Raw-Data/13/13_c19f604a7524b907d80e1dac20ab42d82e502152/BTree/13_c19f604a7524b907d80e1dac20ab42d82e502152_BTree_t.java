 package b.tree;
 
 import java.util.Arrays;
 
 import exception.NullFather;
 import exception.PageFull;
 
 public class BTree {
 
 	/** The root of this tree */
 	Page root;
 	/** A temporary 'pointer' to keep where is the key */
 	Page page; //pt
 	/** Order of the tree */
 	int order;
 	/** 2*order of the tree */
 	int doubleorder;
 	/** A boolean to indicate success or fail */
 	boolean found; //f
 	/** The position of the key in the page 'returned' by findB*/
 	int pos; //g
 
 	public BTree(int order) {
 		this.root = new Page(order);
 		this.order = order;
 		this.doubleorder = 2 * this.order;
 	}
 
 	/**
 	 * Find a registry
 	 * @param reg a registry to find
 	 * @return a registry or null if it fails
 	 */
 	public Registry find(Registry reg)
 	{
 		return find(reg, this.root);
 	}
 	
 	/**
 	 * Find a registry recursively
 	 * @param reg a registry to look for
 	 * @param p a page where look for reg
 	 * @return the registry or null if it was not found
 	 */
 	private Registry find(Registry reg, Page p)
 	{
 		//Page not found!
 		if(p == null)
 		{
 			return null;
 		}
 
 		int i = 0;
 		//Get all registries from p
 		Registry pageregistries[] = p.getRegistries();
		//Look for reg in pages of p until find a registry bigger then reg
 		while( (i < p.getNumRegs()) && (reg.compareTo(pageregistries[i]) > 0) )
 		{
 			/* It only walk through array pageregistries, 
 			 * until find a registry with key bigger or equal to reg
 			 */
 			i++;
 		}
 		//If reg was found! return the registry
 		if(reg.compareTo(pageregistries[i]) == 0)
 		{
 			return pageregistries[i];
 		}
		//Other wise, go search in p[i]
 		else
 		{
			return find(reg, p.getChild(i));
 		}
 	}
 	
 	/**
 	 * Insert a registry into the B-tree
 	 * @param reg a registry to insert
 	 * @throws Exception
 	 */
 	public void insert(Registry reg) throws Exception
 	{
 		insert(reg, this.root);
 	}
 	
 	/**
 	 * Insert a registry into the B-tree recursively
 	 * @param reg reg a registry to insert
 	 * @param p the page where try to insert reg
 	 * @throws Exception Registry already exist!
 	 */
 	public void insert(Registry reg, Page p) throws Exception
 	{
 		//This index will be used in all method
 		int i = 0;
 		//Keep the father of p
 		Page father = p;
 
 		while(p != null)
 		{
 			//Searching where insert reg
 			for(i = 0; i < p.getNumRegs(); i++)
 			{
 				//If the registry to be inserted already exist, throws exception
 				if(reg.compareTo(p.getRegistry(i)) == 0)
 				{
 					throw new Exception(String.format("Registry with key " + reg.getKey() + " already exist!"));
 				}
 				//If it found a registry bigger then reg, so insert reg in page previous the bigger registry
 				else if(reg.compareTo(p.getRegistry(i)) > 0)
 				{
 					//Skip the loop to insert reg
 					break;
 				}
 			}
 			
 			//If p is a Leaf, then try insert reg into p
 			if(p.isLeaf())
 			{
 				break;
 			}
 			//other wise, look for a leaf to insert reg
 			else
 			{
 				father = p;
 				p = p.getChild(i);
 			}
 		}
 		
 		//Trying to insert, if the page is full, split it!
 		try
 		{
 			father.insertReg(reg);
 		}
 		catch(PageFull e)
 		{
 			System.out.println("\nSplitting...");
 			split(father, reg);
 		}
 	}
 	
 	/**
 	 * Splits a page to insert a registry
 	 * @param p a page to be split
 	 * @param r the registry to insert
 	 */
 	//TODO: implement a split into a split
 	private void split(Page p, Registry r)
 	{
 		//Number of registries in a page + 1 */
 		int nregs = p.getOrder() * 2 + 1;
 		//To store registries from p and the new one
 		Registry[] regs = new Registry[nregs];
 		//The middle of regs
 		int middle = (int) Math.floor((nregs)/2f);
 		//The middle registry
 		Registry mreg = null;
 		//New pages that will be created
 		Page n1 = null, n2 = null;
 		//The p's father
 		Page father = p.getFather();
 		
 		//DEBUG
 		System.out.print("\nPage.split(): nregs " + nregs + " middle " + middle);
 		
 		//Put all registries from p into regs
 		for(int i = 0; i < p.getNumRegs(); i++)
 			regs[i] = p.getRegistry(i);
 		//Put r in the last position of regs
 		regs[nregs - 1] = r;
 		
 		//Sort regs
 		Arrays.sort(regs);
 		
 		//DEBUG
 		System.out.println(": " + regs[middle]);
 		
 		/* Creating new tow pages */
 		//Middle registry, it will go to page above
 		mreg = regs[middle];
 		//removing mreg from regs
 		regs[middle] = null;
 		//Sorting regs
 		Arrays.sort(regs, new CompareRegistries());
 		
 		//Creating new pages, them father is the same as p
 		try {
 			n1 = new Page(father);
 			n2 = new Page(father);
 		}
 		/* If a NullFather was thrown, 
 		 * it means that p is the root of this tree!
 		 * and it was thrown by 'n1 = new Page(p.getFather());',
 		 * so n1 and n2 still being null!
 		 * Then create a new root!
 		 */
 		catch (NullFather e1) {
 			//Creating a new root
 			Page newroot = new Page(p.getOrder());
 			/* Set variable 'father' to the father of new pages,
 			 * it means, set 'father' as new root
 			 */
 			father = newroot;
 			try
 			{
 				//Creating new pages with newroot as father
 				n1 = new Page(newroot);
 				n2 = new Page(newroot);
 				
 				//Putting new pages into new root
 				newroot.insertPage(n1);
 				newroot.insertPage(n2);
 			}
 			catch(PageFull e2)
 			{
 				e2.printStackTrace();
 			}
 			/* if it happens, newroot is null, 
 			 * so we have a big problem! 
 			 * Because it should never happens!
 			 */
 			catch (NullFather e) {
 				System.err.println("BTree.splitsplit(Page p, Registry r): newroot is null, it is a BIG problem!");
 				e.printStackTrace();
 			}
 			//Setting BTree.root as newroot
 			this.root = newroot;
 		}
 		
 		//Putting registries into new pages
 		try
 		{
 			for(int i = 0; i < middle; i++)
 				n1.insertReg(regs[i]);
 			for(int i = middle; i < nregs - 1; i++)
 				n2.insertReg(regs[i]);
 		} 
 		catch (PageFull e)
 		{
 			System.err.println("Must slipt during a split!!!!");
 			System.out.println("TODO: IMPLEMENT IT!!");
 			System.err.flush();
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		/* Insert the middle registry into p.father
 		 * if it was not possible, split father.
 		 */
 		try
 		{
 			father.insertReg(mreg);
 		}
 		catch(PageFull e)
 		{
 			System.err.println("Must slipt father during a split!!!!");
 			System.out.println("TODO: IMPLEMENT IT!!");
 			System.err.flush();
 			e.printStackTrace();
 			System.exit(1);
 		}
 		/* 
 		 * I do not remember why I caught NullPinterException...
 		 * It was because I understood that it happened when
 		 * the page that is been split is a root, but now
 		 * it does not make sense...
 		 */
 		catch (NullPointerException e)
 		{
 			/*
 			//Creating a new root
 			Page newroot = new Page(p.getOrder());
 			this.root.setFather(newroot);
 			try
 			{
 				newroot.insertPage(n1);
 				newroot.insertPage(n2);
 			}
 			catch(PageFull e1)
 			{
 				e1.printStackTrace();
 			}*/
 			e.printStackTrace();
 		}catch (Exception e) {e.printStackTrace();}
 	}
 	
 	/**
 	 * Remove a registry
 	 * @param reg a registry to remove
 	 */
 	public void removeReg(Registry reg) {
 
 	}
 
 	/**
 	 * List all elements in-order
 	 */
 	public void listInOrder() {
 
 	}	
 }
