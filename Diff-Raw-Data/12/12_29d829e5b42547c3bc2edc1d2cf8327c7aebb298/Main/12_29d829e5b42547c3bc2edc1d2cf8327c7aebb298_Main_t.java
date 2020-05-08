 package ru.aptu;
 
 import ru.aptu.xml.ASTTree;
 import ru.aptu.xml.ast.AstInput;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 
 
 public class Main {
     public static void main(String[] args) throws IOException {
         BufferedReader r = new BufferedReader(new FileReader("input.xml"));
         char[] buf = new char[10000];
         int len = r.read(buf, 0, 10000);
 
         ASTTree<AstInput> tree = ASTTree.parse(new ASTTree.TextSource("input.xml", Arrays.copyOf(buf, len), 1));
         
        for (ASTTree.ASTProblem e : tree.getErrors()) { 
            System.out.println(e.getMessage() +  
                    ". Column " + tree.getSource().columnForOffset(e.getOffset()));
         }
 
     }
 }
