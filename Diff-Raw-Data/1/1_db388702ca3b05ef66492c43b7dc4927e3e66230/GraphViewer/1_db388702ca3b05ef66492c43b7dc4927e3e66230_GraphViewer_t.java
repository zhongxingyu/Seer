 package jp.ac.osaka_u.ist.sel.metricstool.graphviewer;
 
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.cfg.IntraProceduralCFG;
 import jp.ac.osaka_u.ist.sel.metricstool.main.MetricsTool;
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageListener;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePool;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.pdg.ControlDependenceEdge;
 import jp.ac.osaka_u.ist.sel.metricstool.pdg.DataDependenceEdge;
 import jp.ac.osaka_u.ist.sel.metricstool.pdg.IntraProceduralPDG;
 import jp.ac.osaka_u.ist.sel.metricstool.pdg.PDGEdge;
 import jp.ac.osaka_u.ist.sel.metricstool.pdg.PDGNode;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 
 /**
  * ͂ꂽvOCFG܂PDGgraphvizɏo͂郂W[
  * 
  * @author higo
  *
  */
 public class GraphViewer extends MetricsTool {
 
     public static void main(String[] args) {
 
         try {
 
             //@R}hC
             final Options options = new Options();
 
             {
                 final Option d = new Option("d", "directory", true, "target directory");
                 d.setArgName("directory");
                 d.setArgs(1);
                 d.setRequired(true);
                 options.addOption(d);
             }
 
             {
                 final Option l = new Option("l", "language", true,
                         "programming language of analyzed source code");
                 l.setArgName("language");
                 l.setArgs(1);
                 l.setRequired(true);
                 options.addOption(l);
             }
 
             {
                 final Option c = new Option("c", "ControlFlowGraph", true, "control flow graph");
                 c.setArgName("file");
                 c.setArgs(1);
                 c.setRequired(false);
                 options.addOption(c);
             }
 
             {
                 final Option p = new Option("p", "ProgramDepencenceGraph", true,
                         "program dependence graph");
                 p.setArgName("file");
                 p.setArgs(1);
                 p.setRequired(false);
                 options.addOption(p);
             }
 
             final CommandLineParser parser = new PosixParser();
             final CommandLine cmd = parser.parse(options, args);
 
             // ͗pݒ
             Settings.getInstance().setLanguage(cmd.getOptionValue("l"));
             Settings.getInstance().setTargetDirectory(cmd.getOptionValue("d"));
             Settings.getInstance().setVerbose(true);
 
             // \pݒ
             {
                 final Class<?> metricstool = MetricsTool.class;
                 final Field out = metricstool.getDeclaredField("out");
                 out.setAccessible(true);
                 out.set(null, new DefaultMessagePrinter(new MessageSource() {
                     public String getMessageSourceName() {
                         return "scdetector";
                     }
                 }, MESSAGE_TYPE.OUT));
                 final Field err = metricstool.getDeclaredField("err");
                 err.setAccessible(true);
                 err.set(null, new DefaultMessagePrinter(new MessageSource() {
                     public String getMessageSourceName() {
                         return "main";
                     }
                 }, MESSAGE_TYPE.ERROR));
                 MessagePool.getInstance(MESSAGE_TYPE.OUT).addMessageListener(new MessageListener() {
                     public void messageReceived(MessageEvent event) {
                         System.out.print(event.getSource().getMessageSourceName() + " > "
                                 + event.getMessage());
                     }
                 });
                 MessagePool.getInstance(MESSAGE_TYPE.ERROR).addMessageListener(
                         new MessageListener() {
                             public void messageReceived(MessageEvent event) {
                                 System.err.print(event.getSource().getMessageSourceName() + " > "
                                         + event.getMessage());
                             }
                         });
             }
 
             // ΏۃfBNgȉJavat@Co^C
             {
                 final GraphViewer viewer = new GraphViewer();
                 viewer.readTargetFiles();
                 viewer.analyzeTargetFiles();
             }
 
             if (cmd.hasOption("c")) {
                 out.println("building and outputing CFGs ...");
                 final BufferedWriter writer = new BufferedWriter(new FileWriter(cmd
                         .getOptionValue("c")));
                 for (final TargetMethodInfo method : DataManager.getInstance()
                         .getMethodInfoManager().getTargetMethodInfos()) {
                     final IntraProceduralCFG cfg = new IntraProceduralCFG(method);
                 }
 
                 for (final TargetConstructorInfo constructor : DataManager.getInstance()
                         .getMethodInfoManager().getTargetConstructorInfos()) {
                     final IntraProceduralCFG cfg = new IntraProceduralCFG(constructor);
                 }
                 writer.close();
             }
 
             if (cmd.hasOption("p")) {
                 out.println("building and outputing PDGs ...");
                 final BufferedWriter writer = new BufferedWriter(new FileWriter(cmd
                         .getOptionValue("p")));
                 writer.write("digraph XXX {");
                 writer.newLine();
 
                 int createdGraphNumber = 0;
                 for (final TargetMethodInfo method : DataManager.getInstance()
                         .getMethodInfoManager().getTargetMethodInfos()) {
 
                     final IntraProceduralPDG pdg = new IntraProceduralPDG(method);
 
                     writer.write("subgraph cluster");
                     writer.write(Integer.toString(createdGraphNumber++));
                     writer.write(" {");
                     writer.newLine();
 
                     writer.write("label = \"");
                     writer.write(method.getMethodName());
                     writer.write("\";");
                    writer.newLine();
 
                     final Map<PDGNode<?>, Integer> nodeLabels = new HashMap<PDGNode<?>, Integer>();
                     for (final PDGNode<?> node : pdg.getAllNodes()) {
                         nodeLabels.put(node, nodeLabels.size());
                     }
 
                     for (final Map.Entry<PDGNode<?>, Integer> entry : nodeLabels.entrySet()) {
                         writer.write(Integer.toString(entry.getValue()));
                         writer.write(" [label = \"");
                         writer.write(entry.getKey().getText());
                         writer.write("\"];");
                         writer.newLine();
                     }
 
                     final Set<PDGEdge> edges = new HashSet<PDGEdge>();
                     for (final PDGNode<?> node : pdg.getAllNodes()) {
                         edges.addAll(node.getBackwardEdges());
                         edges.addAll(node.getForwardEdges());
                     }
 
                     for (final PDGEdge edge : edges) {
                         writer.write(Integer.toString(nodeLabels.get(edge.getFromNode())));
                         writer.write(" -> ");
                         writer.write(Integer.toString(nodeLabels.get(edge.getToNode())));
                         if (edge instanceof DataDependenceEdge) {
                             writer.write(" [style = solid]");
                         } else if (edge instanceof ControlDependenceEdge) {
                             writer.write(" [style = dotted]");
                         }
                         writer.write(";");
                         writer.newLine();
                     }
 
                     writer.write("}");
                     writer.newLine();
                 }
 
                 for (final TargetConstructorInfo constructor : DataManager.getInstance()
                         .getMethodInfoManager().getTargetConstructorInfos()) {
                     final IntraProceduralPDG pdg = new IntraProceduralPDG(constructor);
                 }
 
                 writer.write("}");
 
                 writer.close();
             }
 
         } catch (IOException e) {
             err.println(e.getMessage());
             System.exit(0);
         } catch (ParseException e) {
             err.println(e.getMessage());
             System.exit(0);
         } catch (NoSuchFieldException e) {
             err.println(e.getMessage());
             System.exit(0);
         } catch (IllegalAccessException e) {
             err.println(e.getMessage());
             System.exit(0);
         }
     }
 }
