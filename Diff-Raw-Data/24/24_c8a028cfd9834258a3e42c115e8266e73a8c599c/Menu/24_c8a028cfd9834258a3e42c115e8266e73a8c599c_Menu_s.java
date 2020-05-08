 package org.korotovsky.bst.cli;
 
 import org.korotovsky.bst.tree.Tree;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
abstract public class Menu<T> implements MenuInterface {
     protected ArrayList<String> items = new ArrayList<String>();
     protected BufferedWriter writer;
     protected BufferedReader reader;
    protected Tree<T> tree;
 
    public Menu(Tree<T> tree, BufferedWriter writer, BufferedReader reader) {
         this.writer = writer;
         this.reader = reader;
         this.tree = tree;
     }
 
     abstract public void dispatch(MenuDispatcher dispatcher, Integer index) throws IOException;
 
     public ArrayList<String> getItems() {
         return items;
     }
 
     public void append(String label) {
         items.add(label + "\n");
     }
 
     public void print() throws IOException {
         ArrayList<String> items = getItems();
 
         writer.write("\n");
         for (Integer i = 0; i < items.size(); i++) {
             writer.write(" " + i + ") " + items.get(i));
         }
 
         writer.flush();
     }
 }
