 package forkbomb.intermediate.symtabimpl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
import java.util.HashMap;
 
 import wci.intermediate.*;
 import forkbomb.intermediate.SymTabFactory;
 
 /**
  * <h1>SymTabImpl</h1>
  *
  * <p>An implementation of the symbol table.</p>
  *
  * <p>Copyright (c) 2012 by Steven! Ragnarök</p>
  */
 public class SymTabImpl
   implements SymTab
 {
   private int nestingLevel;
  private ArrayList<SymTabEntry> entries;
  private HashMap<String, Integer> table;
 
   public SymTabImpl(int nestingLevel)
   {
     this.nestingLevel = nestingLevel;
    entries = new ArrayList();
    table = new HashMap();
   }
 
   /**
    * Getter.
    * @return the scope nesting level of this entry.
    */
   public int getNestingLevel()
   {
     return nestingLevel;
   }
 
   /**
    * Create and enter a new entry into the symbol table.
    * @param name the name of the entry.
    * @return the new entry.
    */
   public SymTabEntry enter(String name)
   {
     SymTabEntry entry = SymTabFactory.createSymTabEntry(name, this);
    entries.add(entry);
    int index = entries.size() - 1;
    table.put(name, index);
    entry.setIndex(index);
 
     return entry;
   }
 
   /**
    * Look up an existing symbol table entry.
    * @param name the name of the entry.
    * @return the entry, or null if it does not exist.
    */
   public SymTabEntry lookup(String name)
   {
    int index = table.get(name);
    return entries.get(index);
   }
 
   /**
    * @return a list of symbol table entries sorted by name.
    */
   public ArrayList<SymTabEntry> sortedEntries()
   {
    return entries;
   }
 }
