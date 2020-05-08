 package com.computas.sublima.app.index;
 
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /*
   Copied FRGs code from the Inference-module
  */
 public class EndpointSaver {
 
   private String graph;
   private int bufferSize;
   private DefaultSparulDispatcher sparul = new DefaultSparulDispatcher();
 
   private List<String> triples;
 
   public EndpointSaver(String graph, int bufferSize) {
     if (!graph.startsWith("<") && !graph.endsWith(">")) {
       this.graph = "<" + graph + ">";
     } else {
       this.graph = graph;
     }
 
     this.bufferSize = bufferSize;
     triples = new ArrayList<String>();
   }
 
 
   public String getGraph() {
     return graph;
   }
 
   public boolean DropPropertyForType(String object, String property, String type) {
     if (!property.startsWith("<") && !property.endsWith(">")) {
       property = "<" + property + ">";
     }
 
     if (!type.startsWith("<") && !type.endsWith(">")) {
       type = "<" + type + ">";
     }
 
    return ExecQuery("DELETE FROM " + getGraph() + "\n{ " + object + " " + property + " ?o }\nWHERE { " + object + " " + property + " ?o .\n?s a " + type + " . }\n");
   }
 
   public boolean Flush() {
     if (triples.size() == 0)
       return true;
 
     StringBuffer queryBuffer = new StringBuffer();
     queryBuffer.append("INSERT INTO ");
     queryBuffer.append(graph);
     queryBuffer.append(" {\n");
     for (String triple : triples) {
       System.out.println(triples.size());
       System.out.println(triple);
       if (triple != null) {
         queryBuffer.append(triple);
         if (!triple.endsWith("\n")) {
           queryBuffer.append("\n");
         }
       }
     }
     triples.clear();
     queryBuffer.append("}\n");
     return ExecQuery(queryBuffer.toString());
   }
 
   public boolean Add(String triple) {
     triples.add(triple);
     return triples.size() < bufferSize || Flush();
   }
 
   public boolean ExecQuery(String query) {
     return sparul.query(query);
   }
 }
