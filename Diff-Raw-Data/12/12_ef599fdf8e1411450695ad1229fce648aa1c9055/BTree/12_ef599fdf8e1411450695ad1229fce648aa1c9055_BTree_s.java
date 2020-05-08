 package b.tree;
 
 public class BTree {
 
 	/** The root of this tree */
 	Page root;
 	/** A temporary 'pointer' to keep where is the key */
 	Page page; //pt
 	/** A boolean to indicate success or fail */
 	boolean found; //f
 	/** The position of the key in the page 'returned' by findB*/
 	int pos; //g
 
 	public BTree() {
 
 	}
 
 	/*
 	 * Find a page
 	 * @param key a key to find
 	 * @param page the page where is the key
 	 * @param found a boolean to indicate success or fail
 	 * @param pos the position of the key in the page
 	 */
 	/**
 	 * Find a page
 	 * @param key
 	 */
	//IT WAS NOT TESTED!!!!!!
 	// public void findB(int key, Page this.page, boolean this.found, int pos)
 	public void findB(int key) {
 		int i, m;
 		
 		//Initialising variables
		Page p = this.root;
 		this.page = null;
 		this.found = false;
 		m = p.getNumRegs();
 		i = this.pos = 0;
 		
 		//It does not make sense without the 'slides' of my AEDII professor
 		// i index to children array
 		// s[] registers array
 		// pont[] children array, pages
 		
 		while(p != null)
 		{
 			page = p;
 			
 			while(i <= m)
 			{
 				if (key > p.getRegistryKey(i))
 				{
 					this.pos = ++i;
 				}
 				else if (key == p.getRegistryKey(i))
 				{
 					p = null;
 					this.found = true;
 				}
 				else
 				{
 					p = p.getChild(i - 1);
 				}
 				
 				i = m + 2;
 				
 				if (i == (m + 1))
 				{
 					p = p.getChild(m);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Insert a registry in the tree
 	 * @param reg a registry to insert
 	 */
 	public void insertB(Registry reg) 
 	{
 		/* IT WAS NOT TESTED!!!!!!
 		 * and it is not complete!
 		 */
 		
 		//Look for the registry reg in the tree
 		//if there is NOT a registry reg, then this.found is false
 		findB(reg.getKey());
 		
 		//if reg was NOT found in this tree
 		if(!found)
 		{
 			page.insertReg(reg, pos);
 		}
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
