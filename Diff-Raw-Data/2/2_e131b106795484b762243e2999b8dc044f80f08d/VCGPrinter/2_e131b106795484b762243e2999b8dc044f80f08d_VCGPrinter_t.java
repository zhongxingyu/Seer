 package compiler;
 
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.Test;
 
 import compiler.back.regAloc.VirtualRegister;
 import compiler.back.regAloc.VirtualRegisterFactory;
 import compiler.front.Parser.ParserException;
 import compiler.front.Scanner.ScannerException;
 import compiler.ir.cfg.*;
 import compiler.ir.instructions.*;
 
 
 import tests.TestUtils;
 
 public class VCGPrinter {
     private DLXCompiler compiler;
     private List<CFG> CFGs;
     private HashMap<BasicBlock, Integer> nodeMap;
     private Integer nodeNumber;
     private boolean doCompile;
 
 
     public void terminate() {
         compiler.terminate();
     }
 
     @Test
     public void IRCFGs(){
         doCompile=false;
         generateCFGs();
     }
     
     @Test
     public void OptCFGs(){
         doCompile=true;
         generateCFGs();
     }
     
     
     public void generateCFGs(){
         String testFilesFolder = "src/testCases";
         String[] testFiles = TestUtils.listFiles(testFilesFolder, "al.tst");// Edit here to run one test
 
         for (String testFile : testFiles) {
             // init output file and scanner
 
             String graphType = "";
             if (doCompile){
                 graphType = "CO";
             }else{
                 graphType = "IR";
             }
             
             PrintStream vcgOut = null;
             try {
                 vcgOut = new PrintStream(new FileOutputStream(testFilesFolder + "/" + testFile +"-"+graphType+ ".vcg"));
             } catch (FileNotFoundException e) {
                 System.err.println("init:Source file " + testFile + "not found");
             }
             
 
             // open graph and initial settings; closed in "finally" block
             openGraph(vcgOut);
 
             // parse
             compiler = new DLXCompiler(testFilesFolder + "/" + testFile);
             nodeNumber = 0;
             nodeMap = new HashMap<BasicBlock, Integer>();
             try {
 //                parser.parse();
                 if (doCompile){
                     compiler.compile();
                 }else{
                     compiler.parser.parse();
                 }
                 CFGs = compiler.parser.CFGs;
                 
                 for (CFG cfg : CFGs) {
                     
                     // Nodes
                     buildNodes(vcgOut, cfg);
                     
                     vcgOut.println();
 
                     
                 }
                 
                 // Edges
                 cfgEdges(vcgOut);
 
                 vcgOut.println();
 
                 // Print dominator edges
                 dominatorEdges(vcgOut);
 
                 if (doCompile){
                     //Live Ranges
                     buildVirtualRegisters(vcgOut);
                 }
                 
             } catch (ParserException
                     | ScannerException e) {
                 e.printStackTrace();
             } 
             finally {
                 // close output file and scanner
                 closeGraph(vcgOut);
                 compiler.terminate();
             }
         }
     }
 
 
 
 
     private void openGraph(PrintStream out) {
         out.println("graph: { title: \"Control Flow Graph\"\n"
                 + "    layoutalgorithm: dfs\n"
                 + "    display_edge_labels: yes\n"
                 + "    manhatten_edges: yes\n"
                 + "\n"
                 + "    classname 1 : \"CFG Edges (blue)\"\n"
                 + "    classname 2 : \"Virtual Registers (green)\"\n"
                 + "    classname 3 : \"Dominator Tree (gray)\"\n"
                 + "       yspace: 34\n"
                 + "       xspace: 30\n"
                 + "       xlspace: 10\n"
                 // scaling: 0.75
                 + "       portsharing: no\n"
                 + "       finetuning: yes\n"
                 + "       equalydist: yes\n"
                 + "       orientation: toptobottom\n"
                 + "       lateedgelabels: no\n"
                 + "       dirtyedgelabels: yes\n"
                 + "       linearsegments: yes\n"
                 + "       nearedges: yes\n"
                 + "       fstraightphase: yes\n"
                 + "       straightphase: yes\n"
                 + "       priorityphase: yes\n"
                 + "       crossingphase2: yes\n"
                 + "       crossingoptimization: yes\n"
                 + "       crossingweight: medianbary\n"
                 + "       arrowmode: fixed\n"
                 + "       node.borderwidth: 3\n"
                 + "       node.bordercolor: darkyellow\n"
                 + "       node.color: lightyellow\n"
                 + "       node.textcolor: black\n"
                 + "       edge.arrowsize: 15\n"
                 + "       edge.thickness: 4\n");
     }
 
     private void closeGraph(PrintStream out) {
         out.println("}");
         out.close();
     }
 
     private void buildNodes(PrintStream out, CFG cfg) {
         Iterator<BasicBlock> blockIterator = cfg.topDownIterator();
         while (blockIterator.hasNext()) {
             BasicBlock currentBlock = blockIterator.next();
 
             // insert into node map
             nodeMap.put(currentBlock, nodeNumber);
             
 
             // basic name; label open
             out.print("    node: { title:\"" + nodeNumber 
                     + "\" info1: \""+ currentBlock.label + "\nNode: "+ nodeNumber + "\nDepth: "+ currentBlock.depth + "\nFunction: " + cfg.label);
 
             out.print( "\" info2: \"Parameters: "+cfg.printParams()+"\nVariables: "+ cfg.printVars() +"\nArrays: "+ cfg.printArrays());
             if (doCompile){
                 //Linear scan
                 out.print( "\" info3: \"RegAlloc:\nLiveIn: "+currentBlock.liveIn);
             }
             out.print( "\" vertical_order: "+currentBlock.depth + " label: \"" + currentBlock.label);
             
             // function names for start and exit blocks
             if (currentBlock.label.equals("exit") || currentBlock.label.equals("start")){
                 out.print(" : "+ cfg.label +"\n" + (cfg.isFunc()?"FUNCTION":"PROCEDURE"));
             }
             if (currentBlock.label.equals("start")) {
                 if (cfg.label.equals("main")) {
                     for (Global global : compiler.parser.globals) {
                         out.print("\n" + global.toString());
                     }
                 }
                 for (Instruction frameItem : cfg.frame) {
                     out.print("\n" + frameItem.toString());
                 }
             }
             
             // print instructions if they exist
             if (!currentBlock.isInstructionsEmpty()){
                 for (Instruction instruction : currentBlock.getInstructions()) {
                     out.print("\n" + instruction.toString());
                 }   
             }
             
             // label closed
             out.print("\" ");
             
             // special formats
             if (currentBlock.label.equals("exit")){
                 out.print("shape: ellipse color: pink bordercolor: darkred ");
             } else if(currentBlock.label.equals("start")) {
                 out.print("shape: ellipse color: lightgreen bordercolor: darkgreen ");
             } else if (currentBlock.label.equals("while-cond") || currentBlock.label.equals("if-cond")) {
                 out.print("shape: rhomb color: lightcyan bordercolor: darkblue ");
             }
             
             // close
             out.print("}\n");
 
             // next node
             ++nodeNumber;
         }
     }
 
     
     private void cfgEdges(PrintStream vcgOut) {
 //        vcgOut.println("\nedge.class: 1\n");
         String edgeClass = "class: 1";
         for (BasicBlock node : nodeMap.keySet()) {
             // out edges
             for (BasicBlock dest : node.succ) {
                 if (node.label.equals("if-cond") && dest.label.equals("then")) {
                     vcgOut.println("    bentnearedge: { sourcename:\"" + nodeMap.get(node) + "\" targetname:\"" + nodeMap.get(dest) + "\"  label: \"true\" color: darkgreen "+edgeClass+"}");
                 } else if (node.label.equals("if-cond") && dest.label.equals("else")) {
                     vcgOut.println("    bentnearedge: { sourcename:\"" + nodeMap.get(node) + "\" targetname:\"" + nodeMap.get(dest) + "\"  label: \"false\" color: red "+edgeClass+"}");
                 } else if (node.label.equals("while-cond") && dest.label.equals("while-body")) {
                     vcgOut.println("    bentnearedge: { sourcename:\"" + nodeMap.get(node) + "\" targetname:\"" + nodeMap.get(dest) + "\"  label: \"true\" color: darkgreen "+edgeClass+"}");
                 } else if (node.label.equals("while-cond") && dest.label.equals("while-next")) {
                     vcgOut.println("    bentnearedge: { sourcename:\"" + nodeMap.get(node) + "\" targetname:\"" + nodeMap.get(dest) + "\"  label: \"false\" color: red "+edgeClass+"}");
                 } else if (node.depth > dest.depth) {
                     vcgOut.println("    backedge: { sourcename:\"" + nodeMap.get(node) + "\" targetname:\"" + nodeMap.get(dest) + "\"  label: \"back\" color: orange "+edgeClass+"}");
                 } else {
                     vcgOut.println("    edge: { sourcename:\"" + nodeMap.get(node) + "\" targetname:\"" + nodeMap.get(dest) + "\" "+edgeClass+"}");
                 }
             }
         }
     }
     
 
 
     private void dominatorEdges(PrintStream vcgOut) {
 //        vcgOut.println("\nedge.class: 3\n");
         String edgeClass = "class: 3";
         for (BasicBlock node : nodeMap.keySet()) {
             // Dominator Edges
 //            for (BasicBlock dominator : node.semiDom) {
 //        		vcgOut.println("    edge: { sourcename:\"" + nodeMap.get(dominator) + "\" targetname:\"" + nodeMap.get(node) + "\" label: \"DOM\" color: darkgray "+edgeClass+"}");
 //            }
             // Immediate Dominator Edges
             if (node.iDom != null){
                 vcgOut.println("    edge: { sourcename:\"" + nodeMap.get(node.iDom) + "\" targetname:\"" + nodeMap.get(node) + "\" label: \"DOM\" color: lightgray "+edgeClass+"}");
             }
         }
     }
 
     private void buildVirtualRegisters(PrintStream vcgOut) {
         vcgOut.println("\nedge.class: 3\n"+
                 "edge.color: lightgreen\n"+
                 "node.color: darkgreen\n"+
                 "node.textcolor: lightgreen\n"+
                 "node.bordercolor: green\n");
         
 
         String edgeClass = "class: 2";
         Integer regNumber=0;
 
         for (VirtualRegister vReg : VirtualRegisterFactory.virtualRegisters) {
 //            System.err.println(vReg.regNumber + ":\t" + vReg.getRanges());
 
             //TODO make more efficient
             
             //Print reg node
             regNumber = vReg.regNumber;
             int startDepth = -1;
             int endDepth = -1;
             int startLine = Integer.MAX_VALUE;
             int endLine = -1;
             String sourceCode = "";
             String destCode = "";
             String edges = "";
 
             //Range start and ends
             List<Range> ranges = vReg.getRanges();
             //Ignore empty ranges
             if(ranges == null || ranges.isEmpty() ){
                 continue;
             }
             
             for (CFG cfg : CFGs) {
                 Iterator<BasicBlock> blockIterator = cfg.topDownIterator();
                 while (blockIterator.hasNext()) {
                     BasicBlock currentBlock = blockIterator.next();
 
                     String edgeType = "edge";
                     if (currentBlock.label.equals("if-cond") || currentBlock.label.equals("while-cond")) {
                        edgeType = "bentnearedge";
                     }
 
                     
                     for (Range range : ranges) {
                         startLine = Math.min(startLine, range.begin);
                         endLine = Math.max(endLine, range.end);
                     }
 //                        System.err.println(regNumber+": "+currentBlock.begin()+"["+startLine +" - "+ endLine+"]"+currentBlock.end());
                         
                     if (startLine >= currentBlock.begin() && startLine <= currentBlock.end()) {
                         edges = edgeType + ": { sourcename: \"" + nodeMap.get(currentBlock) + "\" targetname: \"vr" + regNumber + "\" "+edgeClass+"}\n"+ edges;
                         startDepth = currentBlock.depth;
                         for (Instruction ins: currentBlock.getInstructions()){
                             
 //                                System.err.println(ins.getInstrNumber());
                                 
                             if(ins.getInstrNumber() == startLine){
                                 sourceCode = ins.toString();
 //                                    System.err.println("^^SOURCE^^");
                             }
                         }
                     }
                     if (endLine >= currentBlock.begin() && endLine <= currentBlock.end()) {
                         edges += edgeType + ": { targetname: \"" + nodeMap.get(currentBlock) + "\" sourcename: \"vr" + regNumber + "\" "+edgeClass+"}";
                         endDepth = currentBlock.depth;
                         for (Instruction ins: currentBlock.getInstructions()){
                             
 //                              System.err.println(ins.getInstrNumber());
                             
                             if(ins.getInstrNumber() == endLine){
                                 destCode = ins.toString();
 //                                  System.err.println("^^DEST^^");
                             }
                         }
                     }
                     
                 }
             }
             
             int depth = (startDepth + (endDepth-startDepth)/2);
             if (startDepth == -1){
                 depth=endDepth;
             }else if (endDepth == -1){
                 depth=startDepth;
             }
 
             vcgOut.println("node: { title: \"vr" + regNumber + "\" label: \"vr" + regNumber + "\"  " + //vertical_order: "+depth+"
             		"info1: \"Source: " + sourceCode + 
                     "\nStart: " + startLine + "\" " +
                     "info2: \"Dest: " + destCode +
             		"\nEnd: " + endLine + "\" " +
             		"info3: \"Depth: "+depth+"\"}\n" + edges);
         }
     }
 }
