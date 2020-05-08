 package org.korotovsky.bst.cli.menu;
 
 import org.korotovsky.bst.cli.Menu;
 import org.korotovsky.bst.cli.MenuDispatcher;
 import org.korotovsky.bst.tree.Tree;
 import org.korotovsky.bst.tree.TreeNode;
 import org.korotovsky.bst.tree.exceptions.DuplicateItemTreeException;
 import org.korotovsky.bst.tree.exceptions.NotFoundTreeException;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 
 public class MainMenu<T> extends Menu<T> {
     public static final String SUCCESS_TREE_WAS_CLEANED = "SUCCESS: Tree was cleaned";
     public static final String SUCCESS_TREE_SIZE_IS = "SUCCESS: Tree size is: ";
     public static final String SUCCESS_TREE_IS_EMPTY = "SUCCESS: Tree is empty";
     public static final String SUCCESS_TREE_IS_NOT_EMPTY = "SUCCESS: Tree is not empty";
     public static final String SUCCESS_NODE_CONTAINS = "SUCCESS: Node contains ";
 
     public static final String ERROR = "ERROR: ";
     public static final String ERROR_INVALID_KEY_PROVIDED = "ERROR: Invalid key provided";
     public static final String ERROR_INVALID_KEY_OR_DATA_PROVIDED = "ERROR: Invalid key or data provided";
 
     public static final String NOTICE_INPUT_NODE_KEY = "NOTICE: Input node key: ";
     public static final String NOTICE_INPUT_NODE_DATA = "NOTICE: Input node data: ";
 
     public static final String ITEM_PRINT_TREE = "Print tree";
     public static final String ITEM_CLEAR_TREE = "Clear tree";
     public static final String ITEM_GET_TREE_SIZE = "Get tree size";
     public static final String ITEM_CHECK_IF_TREE_IS_EMPTY = "Check if tree is empty";
     public static final String ITEM_CREATE_NODE = "Create node";
     public static final String ITEM_FIND_NODE_BY_KEY = "Find node by key";
     public static final String ITEM_DELETE_NODE_BY_KEY = "Delete node by key";
     public static final String ITEM_ITERATOR_MENU = "Iterator menu";
 
     public MainMenu(Tree<T> tree, BufferedWriter writer, BufferedReader reader) {
         super(tree, writer, reader);
 
         append(ITEM_PRINT_TREE);
         append(ITEM_CLEAR_TREE);
         append(ITEM_GET_TREE_SIZE);
         append(ITEM_CHECK_IF_TREE_IS_EMPTY);
         append(ITEM_CREATE_NODE);
         append(ITEM_FIND_NODE_BY_KEY);
         append(ITEM_DELETE_NODE_BY_KEY);
         append(ITEM_ITERATOR_MENU);
     }
 
     public void dispatch(MenuDispatcher dispatcher, Integer index) throws IOException {
         switch (index) {
             case 0:
                 printTree();
                 break;
             case 1:
                 clear();
                 break;
             case 2:
                 size();
                 break;
             case 3:
                 isEmpty();
                 break;
             case 4:
                 createNode();
                 break;
             case 5:
                 findNode();
                 break;
             case 6:
                 removeNode();
                 break;
             default:
                 dispatcher.setMenu(1);
         }
     }
 
     private void printTree() throws IOException {
         tree.print(writer);
     }
 
     private void createNode() throws IOException {
         writer.write(NOTICE_INPUT_NODE_DATA);
         writer.flush();
         String key = reader.readLine();
 
         writer.write(NOTICE_INPUT_NODE_DATA);
         writer.flush();
         String value = reader.readLine();
 
         if (key.equals("") || value.equals("")) {
             writer.write(ERROR_INVALID_KEY_OR_DATA_PROVIDED);
             writer.flush();
         } else {
             try {
                 tree.create(key, (T) value); // Shit
                 writer.newLine();
                 writer.write(SUCCESS_NODE_CONTAINS + key + ":" + value);
             } catch (DuplicateItemTreeException e) {
                 writer.write(ERROR + e.getMessage());
             }
         }
 
         writer.flush();
     }
 
     private void removeNode() throws IOException {
         writer.write(NOTICE_INPUT_NODE_KEY);
         writer.flush();
         String key = reader.readLine();
 
         if (key.equals("")) {
             writer.write(ERROR_INVALID_KEY_PROVIDED);
             writer.flush();
         } else {
             try {
                 tree.remove(key);
             } catch (NotFoundTreeException e) {
                 writer.write(ERROR + e.getMessage());
             }
         }
 
         writer.flush();
     }
 
     private void findNode() throws IOException {
         writer.write(NOTICE_INPUT_NODE_KEY);
         writer.flush();
         String key = reader.readLine();
 
         if (key.equals("")) {
             writer.write(ERROR_INVALID_KEY_PROVIDED);
             writer.flush();
         } else {
             try {
                 TreeNode<T> treeNode = tree.find(key);
                 writer.newLine();
                 writer.write(SUCCESS_NODE_CONTAINS + treeNode.getLine());
             } catch (NotFoundTreeException e) {
                 writer.write(ERROR + e.getMessage());
             }
         }
 
         writer.flush();
     }
 
     private void isEmpty() throws IOException {
         Boolean isEmpty = tree.isEmpty();
         writer.newLine();
         writer.write(isEmpty ? SUCCESS_TREE_IS_EMPTY : SUCCESS_TREE_IS_NOT_EMPTY);
         writer.newLine();
         writer.flush();
     }
 
     private void size() throws IOException {
         Integer size = tree.size();
         writer.newLine();
         writer.write(SUCCESS_TREE_SIZE_IS + size);
         writer.newLine();
         writer.flush();
     }
 
     private void clear() throws IOException {
         tree.clear();
         writer.newLine();
         writer.write(SUCCESS_TREE_WAS_CLEANED);
         writer.newLine();
         writer.flush();
     }
 }
