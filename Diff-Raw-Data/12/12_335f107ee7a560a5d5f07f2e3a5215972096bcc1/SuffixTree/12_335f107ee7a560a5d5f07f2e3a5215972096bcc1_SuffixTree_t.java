 package com.od.filtertable.suffixtree;
 
 import com.od.filtertable.index.MutableCharSequence;
 import com.od.filtertable.index.MutableSequence;
 
 import java.io.PrintWriter;
 import java.util.Collection;
 
 /**
  * User: nick
  * Date: 07/05/13
  * Time: 19:00
  */
 public abstract class SuffixTree<V> {
 
     SuffixTree<V> firstChild;
     SuffixTree<V> nextPeer;
     Collection<V> values;
 
     char[] label = CharUtils.EMPTY_CHAR_ARRAY;
 
     /**
      * Create a root node
      */
     public SuffixTree() {
     }
 
     public SuffixTree(CharSequence s) {
         this.label = CharUtils.createCharArray(s);
     }
 
     public void add(CharSequence s, V value) {
         MutableCharSequence c = CharUtils.addTerminalCharAndCheck(s);
         add(c, value);
     }
 
     public void add(MutableCharSequence s, V value) {
         if (s.length() == 0) {
             addValue(value);
         } else {
             ChildNodeIterator<V> i = new ChildNodeIterator<V>(this);
             boolean added = false;
             while (i.isValid()) {
                 int matchingChars = CharUtils.getSharedPrefixCount(s, i.getCurrentNode().label);
                if (matchingChars == s.length() /* since s must end with terminal char, this  be a terminal node */ ) {
                     addToChild(s, value, i, matchingChars);
                     added = true;
                     break;
                } else if ( matchingChars == i.getCurrentNode().label.length /*whole prefix matched */) {
                     addToChild(s, value, i, matchingChars);
                     added = true;
                     break;
                 } else if ( matchingChars > 0) {  //only part of the current node label matched
                     split(i, s, value, matchingChars);
                     added = true;
                     break;              
                } else if ( CharUtils.isLowerValue(CharUtils.createCharArray(s), i.getCurrentNode().label)) {
                    insert(i, s, value);
                    added = true;
                    break;
                 }
                 i.next();
             }
 
             if (!added) {
                 insert(i, s, value);
             }        
         }      
     }
 
     private void addToChild(MutableCharSequence s, V value, ChildNodeIterator<V> i, int matchingChars) {
         s.incrementStart(matchingChars);        
         i.getCurrentNode().add(s, value);
         s.decrementStart(matchingChars);
     }
 
     private void split(ChildNodeIterator<V> i, MutableCharSequence s, V value, int matchingChars) {
         char[] labelForReplacement = CharUtils.getPrefix(s, matchingChars);
 
         SuffixTree<V> nodeToReplace = i.getCurrentNode();
         int labelLengthForReplacementChild = nodeToReplace.label.length - matchingChars;
         char[] labelForReplacementChild = CharUtils.getSuffix(nodeToReplace.label, labelLengthForReplacementChild);
         
         int labelLengthForNewChild = s.length() - matchingChars;
         char[] labelForNewChild = CharUtils.getSuffix(s, labelLengthForNewChild);
         
         SuffixTree<V> replacementNode = createNewSuffixTreeNode();
         replacementNode.label = labelForReplacement;
         i.replaceCurrent(replacementNode);
         
         SuffixTree<V> replacementChild = createNewSuffixTreeNode();
         replacementChild.label = labelForReplacementChild;
         replacementChild.firstChild = nodeToReplace.firstChild;
         replacementChild.values = nodeToReplace.values;
 
         SuffixTree<V> newChild = createNewSuffixTreeNode();
         newChild.label = labelForNewChild;
         newChild.addValue(value);
 
         boolean newChildFirst = CharUtils.isLowerValue(labelForNewChild, labelForReplacementChild);
         SuffixTree<V> firstChild = newChildFirst ? newChild : replacementChild;
         SuffixTree<V> secondChild = newChildFirst ? replacementChild : newChild;
         
         replacementNode.firstChild = firstChild;
         firstChild.nextPeer = secondChild;
     }
 
     private void insert(ChildNodeIterator<V> i, MutableCharSequence s, V value) {
         SuffixTree<V> newNode = createNewSuffixTreeNode();
         newNode.label = CharUtils.createCharArray(s);
         newNode.addValue(value);
         i.insert(newNode);
     }
 
     private void addValue(V value) {
         if ( values == null) {
             values = getCollectionFactory().createTerminalNodeCollection();
         }
         values.add(value);
     }
 
     public Collection<V> get(CharSequence c, Collection<V> targetCollection) {
         return get(new MutableSequence(c), targetCollection);
     }
 
     public Collection<V> get(MutableCharSequence s, Collection<V> targetCollection) {
         if ( isTerminalNode() ) {
             targetCollection.addAll(values);
         } else {
             ChildNodeIterator<V> i = new ChildNodeIterator<V>(this);
             boolean foundMatch = false;                      
             //get results from all nodes which share a prefix
             while(i.isValid()) {
                 int sharedCharCount = i.getSharedChars(s);
                 if ( sharedCharCount > 0 || s.length() == 0 /* all chars already matched, include */ ) {
                     getValues(s, targetCollection, i, sharedCharCount);
                     foundMatch = true;
                 } else if (foundMatch) {
                     break;
                     //since alphabetical, if we already found at least one match, and the next match fails
                     //we can assume all subsequent will fail
                 }
                 i.next();
             }
         }
         return targetCollection;
     }
 
     private void getValues(MutableCharSequence s, Collection<V> targetCollection, ChildNodeIterator<V> i, int sharedCharCount) {
         s.incrementStart(sharedCharCount);
         i.getCurrentNode().get(s, targetCollection);
         s.decrementStart(sharedCharCount);
     }
 
     public void printStructure(int level, PrintWriter w) {
         StringBuilder sb = new StringBuilder();
         addIndent(level, sb);
         sb.append(label);
         if ( values != null) {
             sb.append("\n");
             addIndent(level, sb);
             sb.append("val: ");
             for (Object o : values) {
                 sb.append(o.toString()).append(" ");    
             }
         }
         sb.append(" -->\n");
         w.print(sb.toString());
         w.flush();
         ChildNodeIterator<V> i = new ChildNodeIterator<V>(this);
         while (i.isValid()) {
             i.getCurrentNode().printStructure(level + 1, w);
             i.next();
         }
     }
 
     private void addIndent(int level, StringBuilder sb) {
         for ( int loop=0; loop < level; loop++) {
             sb.append("  ");
         }
     }
 
     protected abstract SuffixTree<V> createNewSuffixTreeNode();
 
     protected abstract CollectionFactory<V> getCollectionFactory();
 
     public boolean isTerminalNode() {
         return label.length > 0 /* root node */ && label[label.length - 1] == CharUtils.TERMINAL_CHAR;
     }
 }
