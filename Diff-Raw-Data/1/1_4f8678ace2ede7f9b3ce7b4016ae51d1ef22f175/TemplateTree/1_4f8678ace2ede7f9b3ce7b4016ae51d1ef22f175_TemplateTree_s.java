 package ru.romanchuk.sqlmaster.parser;
 
 import ru.romanchuk.sqlmaster.parser.tree.EmbeddedNode;
 import ru.romanchuk.sqlmaster.parser.tree.ParameterNode;
 import ru.romanchuk.sqlmaster.parser.tree.RootNode;
 
 import java.util.List;
 
 /**
  * @author Alexey Romanchuk
  */
 public interface TemplateTree {
     RootNode getRootNode();

     List<ParameterNode> getParameterNode(String name);
     List<EmbeddedNode> getEmbeddedNode(String name);
 }
