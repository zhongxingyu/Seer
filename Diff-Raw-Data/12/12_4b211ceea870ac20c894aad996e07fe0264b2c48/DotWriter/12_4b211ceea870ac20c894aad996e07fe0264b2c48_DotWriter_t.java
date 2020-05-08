 package org.ow2.mind.adl.annotations;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.TreeSet;
 
 import org.objectweb.fractal.adl.interfaces.Interface;
 import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
 import org.ow2.mind.adl.ast.ASTHelper;
 import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.DefinitionReference;
 import org.ow2.mind.adl.ast.MindInterface;
 import org.ow2.mind.adl.ast.Source;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Definition;
 import org.ow2.mind.adl.ast.Binding;
 import org.objectweb.fractal.adl.types.TypeInterface;
 
 public class DotWriter {
 
 	private PrintWriter currentPrinter;
 	private String compName;
 	private String localName;
 	private String buildDir;
 	private String fileName;
 	private String srcs="Sources [shape=record,label=\" {{ ";
 	private int srcNb=0;
 	private String srvItfs="Servers [rank=sink,shape=Mrecord,style=filled,fillcolor=blue,label=\" Servers | {{ ";
 	private int srvItfsNb=0;
 	private String cltItfs="Clients [rank=source,shape=Mrecord,style=filled,fillcolor=blue,label=\" Clients | {{ ";;
 	private int cltItfsNb=0;
 	private int maxItf=0; // Used to adapt the size of composite interface boxes
 	private int color=1;
 
 	public DotWriter(String dir, String name) {
 		try {
 			compName = name;
 			final int i = name.lastIndexOf('.');
 			if (i == -1 ) { 
 				localName = name;
 			} else {
 				localName = name.substring(i + 1);
 			}
 			buildDir = dir;
 			fileName = buildDir + File.separator + compName + ".dot";
 			currentPrinter = new PrintWriter( new FileWriter( fileName ) );
 			writeHeader();
 		} catch ( final IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void writeHeader() {
 		currentPrinter.println("digraph " + localName + " {");
 		currentPrinter.println("rankdir=LR;");
 		currentPrinter.println("ranksep=3;");
 		currentPrinter.println("subgraph cluster_membrane {");
 		currentPrinter.println("penwidth=15;");
 		currentPrinter.println("color=blue;");
 		currentPrinter.println("style=rounded;");
 		//currentPrinter.println("height=20;"); // max number of itf /50*18 
 
 
 	}
 
 	public void addSubComponent(Component component) {
 		try {
 			int clientItf = 0;
 			int serverItf = 0;
			DefinitionReference defRef = component.getDefinitionReference();
			final Definition definition = ASTHelper.getResolvedDefinition(defRef, null, null);
 			currentPrinter.print(component.getName() + "[URL=\"" + compName + "." + component.getName() + ".dot\"shape=Mrecord,style=filled,fillcolor=lightgrey,label=\"" + component.getName() + " | {{ " );
 			if (definition instanceof InterfaceContainer) {
 
 				TreeSet<MindInterface> interfaces = new TreeSet<MindInterface>(new MindInterfaceComparator());
 				for (Interface itf : ((InterfaceContainer) definition).getInterfaces())
 					interfaces.add((MindInterface) itf); 
 				//final Interface[] interfaces = ((InterfaceContainer) definition).getInterfaces();
 				//			for (int i = 0; i < interfaces.length; i++) {
 				//				final MindInterface itf = (MindInterface) interfaces[i];
 				for (MindInterface itf : interfaces) {
 					if (itf.getRole()==TypeInterface.SERVER_ROLE) {
 						if ( serverItf !=0 ) currentPrinter.print(" | ");
 						currentPrinter.print("<" + itf.getName() + "> " + itf.getName());
 						serverItf++;
 						//itf.getSignature()); //TODO might put this info somwhere latter
 					}
 				}
 				currentPrinter.print(" } | | { ");
 				//			for (int i = 0; i < interfaces.length; i++) {
 				//				final MindInterface itf = (MindInterface) interfaces[i];	
 				for (MindInterface itf : interfaces) {	
 					if (itf.getRole()==TypeInterface.CLIENT_ROLE) {
 						if ( clientItf !=0 ) currentPrinter.print(" | ");
 						currentPrinter.print("<" + itf.getName() + "> " + itf.getName());
 						clientItf++;
 						//itf.getSignature());
 					}
 				}
 				currentPrinter.print(" }} | \" ];");
 				currentPrinter.println("");
 				if (clientItf > maxItf) maxItf = clientItf;
 				if (serverItf > maxItf) maxItf = serverItf;
 			}
 		} catch (final ADLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void addBinding(Binding binding) {
 		color++;
 		if (color >= 12) color=1;
 		String fc = binding.getFromComponent();
 		String fi = binding.getFromInterface();
 		String tc = binding.getToComponent();
 		String ti = binding.getToInterface();
 		if (fc == "this") fc="Servers";
 		if (tc == "this") tc="Clients";
 		currentPrinter.println( fc + ":" + fi + "->" + tc + ":" + ti + "[tailport=e headport=w colorscheme=\"paired12\" color=" + color + "];");	
 	}
 
 	public void addSource(Source source) {
 		if (srcNb != 0) srcs = srcs + " | ";
 		srcs=srcs + source.getPath();
 		srcNb++;
 	}
 
 	public void addServer(String itf) {
 		if (srvItfsNb != 0) srvItfs=srvItfs + " | ";
 		srvItfs=srvItfs + "<" + itf + "> " + itf;
 		srvItfsNb++;
 	}
 
 	public void addClient(String itf) {
 		if (cltItfsNb != 0) cltItfs=cltItfs + " | ";
 		cltItfs=cltItfs + "<" + itf + "> " + itf;
 		cltItfsNb++;	
 	}
 
 	public void close() {
 		writeFooter();
 
 	}
 
 	private void writeFooter() {
 		if (cltItfsNb > maxItf) maxItf=cltItfsNb;
 		if (srvItfsNb > maxItf) maxItf=srvItfsNb;
 		if (srcNb > maxItf) maxItf=srcNb;
 		srvItfs=srvItfs + "}} | \",height=" + ((int)((maxItf+3)/2)) + " ];";
 		cltItfs=cltItfs + "}} | \",height=" + ((int)((maxItf+3)/2)) + " ];";
 		srcs=srcs + " }} \" ];";
 		if (srvItfsNb > 0) currentPrinter.println(srvItfs);
 		if (cltItfsNb > 0) currentPrinter.println(cltItfs);
 		if (srcNb > 0) currentPrinter.println(srcs);
 		if ((srvItfsNb*srcNb) > 0) currentPrinter.println("Servers->Sources[color=none]");
 		if ((srcNb*cltItfsNb) > 0)currentPrinter.println("Sources->Clients[color=none]");
 		currentPrinter.println("}");
 		currentPrinter.println("}");
 		currentPrinter.close();
 	}
 }
