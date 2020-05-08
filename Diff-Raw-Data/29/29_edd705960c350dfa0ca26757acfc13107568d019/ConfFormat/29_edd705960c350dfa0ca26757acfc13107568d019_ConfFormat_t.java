 package dot2conf;
 
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class ConfFormat {
     static private PrintWriter outStream;
     
     
     static void writeToFile(String filename, ArrayList<Node> node) throws Exception{
         String confFilename;
         String buffer;
         ArrayList<String> inputs;
         Date date;
         Node cur_node;
         int i, j;
         
         // print name
         confFilename = filename.replace(".dot", ".conf");
         outStream = new PrintWriter(new FileOutputStream("conf_files/" + confFilename));
         outStream.println("Name= \"" + confFilename + "\"");
         
         // print date
         date = new Date();
         outStream.printf("%s %tB %<te, %<tY %s", "Date= \"", date, "\"\n\n");
         
         
         // print inputs
         buffer = "inputs = { ";
         for(i=0; i<node.size(); i++){
             inputs = node.get(i).getInputs();
             
             for(j=0; j<inputs.size(); j++)
                 if(inputs.get(j).startsWith("c"))
                     buffer = buffer + inputs.get(j) + ", ";
         }
         buffer = buffer.substring(0, buffer.lastIndexOf(","));
         buffer = buffer + "}";
         outStream.println(buffer);
         
         
         // print outputs
         buffer = "outputs = { ";
         for(i=0; i<node.size(); i++){
             if(node.get(i).getOutput().startsWith("o"))
                 buffer = buffer + node.get(i).getOutput() + ", ";
         }
         
         // FIXME - instead of using this cheat pass the register_coutner, input_coutner and output_counter from dotGraph somehow
         buffer = buffer.substring(0, buffer.lastIndexOf(","));
         buffer = buffer + "}";
         outStream.println(buffer);
         
         
         // print registers
        buffer = "regs = { ";
         for(i=0; i<node.size(); i++){
             if(node.get(i).getOutput().startsWith("r"))
                 buffer = buffer + node.get(i).getOutput() + ", ";
         }
         // FIXME - assumes that there is at least one register (WHICH MAY NOT BE THE CASE)
         buffer = buffer.substring(0, buffer.lastIndexOf(","));
         buffer = buffer + "}\n\n";
         outStream.println(buffer);
         
         // print each node
         for(i=0; i<node.size(); i++){
             cur_node = node.get(i);
             outStream.println("task " + cur_node.getName() + " { ");
             outStream.println("\ttype = " + cur_node.getType());
             
             // each nodes input
             inputs = cur_node.getInputs();
             outStream.print("\tinputs = { ");
             for(j=0; j<inputs.size();j++){
                 outStream.print(inputs.get(j));
                 
                 if(j < inputs.size() - 1)
                     outStream.print(", ");
             }
             outStream.println("}");
             
             
             // each nodes output
             outStream.println("\toutput = " + cur_node.getOutput());
             outStream.println("}");
         }
         
         outStream.close();
     }
 }
