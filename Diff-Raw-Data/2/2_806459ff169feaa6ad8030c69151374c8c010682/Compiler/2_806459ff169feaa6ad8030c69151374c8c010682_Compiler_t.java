 // 
 
 import compiler.scanner.*;
 import compiler.parser.*;
 import compiler.irt.*;
 import compiler.ast.*;
 import compiler.semantic.*;
 import compiler.codegen.*;
 import compiler.opt.*;
 import compiler.lib.*;
 import java.io.*;
 import java.util.ArrayList;
 
 public class Compiler {
 	public String[] args;
 	public int size;
 
 	public Compiler(String[] args){
 		this.args = args;
 		this.size = args.length;
 	}
 
 	// METODOS
 	public void error(int o, int d, PrintWriter a){
 		a.close();
 		error(o, d);
 	}
 	public void error(int o, int d){
 		//System.out.println("-------------------------------------------------------------------------------------");
 		//	DEBUG
 		//System.out.println("*Error on line "+d+".");
 		System.out.println("\n\t\t\t\t::Ayuda::");
 		System.out.println("Para poder ejecuar el compilador correctamente necesita ingresar en la linea de");
 		System.out.println("comandos 'java Compiler <opcion> <filename>' donde <opcion> puede ser una lista");
 		System.out.println("de opciones que se le otorga a usted el usuario para el compilador, las opciones");
 		System.out.println("se le especifican con detalle adelante. Y <filename> sera el archivo el cual");
 		System.out.println("sera compilado y tiene que ser ingresado correctamente como '(filename).txt'.");
 		System.out.println(" ");
 		System.out.println("-------------------------------------------------------------------------------------");
 		System.out.println("\t\t\t\t::Opciones::");
 		System.out.println("Estas son las opciones que seran aceptadas por el compilador:");
 		System.out.println(" ");
 	    System.out.println("-o\t<outname>\tEsta opcion es la cual <outname> es el archivo que sera");
 		System.out.println("\t\t\tel destino donde el compilador dejara lo generado.");
 		System.out.println(" ");
 		System.out.println("-target\t<stage>\t\tEsta opcion lo que realiza es que lleva a un punto en ");
 		System.out.println("\t\t\tespecifico el compilador de su eleccion donde <stage> ");
 		System.out.println("\t\t\tpodria ser (scan, parse, ast, irt, semantic o codegen).");
 		System.out.println(" ");
 		System.out.println("-opt\t<optimization>\tEsta opcion es la cual optimiza el compilador y ");
 		System.out.println("\t\t\t<optimization> puede ser constant o algebraic.");
 		System.out.println(" ");
 		System.out.println("-debug\t<stage>\t\tEsta opcion es la cual debuguea su programa o archivo ");
 		System.out.println("\t\t\tingresado y <stage> hasta donde usted desea que se");
 		System.out.println("\t\t\tdebuguee y puede ser ingresado de esta manera ");
 		System.out.println("\t\t\t'scan:parse:ast:irt:semantic:codegen:'.");
 		System.out.println(" ");
 		System.out.println("-h\t\t\tEsta opcion es la cual usted utiliza para que se le ");
 		System.out.println("\t\t\totorgue la ayuda.");
 		System.out.println("-------------------------------------------------------------------------------------");
 		System.exit(1);
 	}
 	public void noArguments(){
 		if (this.args.length==0)
 			this.error(1, 35);
 	}
 	public int counter(String str){
 		int x=0;
 		for (int i=0; i<this.size; i++) {
 			if(args[i].equals(str)) x++;
 		}
 		return x;
 	}
 	public int position(String str){
 		int x=-1;
 		for (int i=0; i<this.size; i++) {
 			if(args[i].equals(str)) x=i;
 		}
 		return x;
 	}
 	public void verificar(String str){
 		int x = this.position(str);
 		if(x+1>=args.length || (x+2)>=args.length)
 			this.error(1, 53);
 		if(args[x+1].substring(0,1).equals("-"))
 			this.error(1,55);
 
 	}
 	public String[] separate(String str){
 		int n = 1;
 		int x = -1;
 		int i = 0;
 		String aux = str;
 
 		// cuantos stages hay!
 		x = aux.indexOf(":");
 		while (x>=0) {
 			aux = aux.substring(x+1);
 			x = aux.indexOf(":");
 			n++;
 		}
 
 		// creamos el arreglo
 		String stages[] = new String[n];
 
 		// guardarlos a un arreglo
 		x = str.indexOf(":");
 		while (x>=0) {
 			stages[i] = str.substring(0,x);
 			str = str.substring(x+1);
 			x = str.indexOf(":");
 			i++;
 		}
 
 		// el ultimo o primero
 		stages[i] = str;
 		
 		return stages;
 	}
 
 	public String[] insertionSort(String[] arreglo, String[] targets){
 
 		int aux, j;
 		String aux2;
 		int[] positions = new int[arreglo.length];
 		ArrayList<String> fill = new ArrayList<String>(1);
 
 		// array of indexs
 		for (int i=0; i<positions.length; i++)
 			for (int k=0; k<targets.length; k++)
 				if(arreglo[i].equals(targets[k]))
 					positions[i] = k;
 
 		// insertion sort
 		for (int i=1; i<positions.length; i++) {
 			aux = positions[i];
 			aux2 = arreglo[i];
 			j = i-1;
 			while (j>=0 && positions[j]>aux){
 				positions[j+1] = positions[j];
 				arreglo[j+1] = arreglo[j];		//fix
 				j = j-1;
 			}
 			positions[j+1] = aux;
 			arreglo[j+1] = aux2;
 		}
 
 		aux2 = "";
 		// remove repeated stages
 		for (int i=0; i<arreglo.length; i++) {
 			if (aux2.equals(arreglo[i]));
 			else
 				fill.add(arreglo[i]);
 
 			aux2 = arreglo[i];
 
 		}
 
 		// list to array
 		String[] a = new String[fill.size()];
 		for (int i=0; i<fill.size(); i++) {
 			a[i] = fill.get(i);
 		}
 
 		// done
 		return a;
 		
 	}
 
 	public static void main(String[] args) throws Exception {
 		// Define variables
 		int x					= 0;
 		String str 				= "";
 		String filename 		= "";
 		String output			= "";
 		File outFile 			= new File("");
		PrintWriter outputFile	= new PrintWriter("compiler/test/readme.txt");
 		String[] options 		= {"-o", "-target", "-opt", "-debug", "-h"};
 		String[] targets 		= {"scan", "parser", "ast", "semantic", "irt", "codegen"};
 		String[] targetsP 		= {"Scanner", "Parser", "Ast", "Semantic", "Irt", "Codegen"};
 	ArrayList<String> myOptions	= new ArrayList<String>(1);
 	ArrayList<String> debugList	= new ArrayList<String>(1);
 		
 		// Create a compiler
 		Compiler compilador = new Compiler(args);
 
 		// checks if there are no arguments
 		compilador.noArguments();
 
 		int o = compilador.counter("-o");
 		int target = compilador.counter("-target");
 		int opt = compilador.counter("-opt");
 		int debug = compilador.counter("-debug");
 		int h = compilador.counter("-h");
 
 		// if no option recognized
 		if(o==0&&target==0&&opt==0&&debug==0)
 			compilador.error(1, 149);
 
 		// checks for filename
 		if(args.length<3) compilador.error(1, 152);
 		else if(args[args.length-3].substring(0,1).equals("-"))
 			if(args[args.length-2].substring(0,1).equals("-"))
 				compilador.error(1, 155);
 			else
 				if(args[args.length-1].indexOf(".txt")>0)
 					filename = args[args.length-1];
 				else
 					compilador.error(1,160);
 		// exists?
 		File file = new File(filename);
 		if(!(file.exists())) compilador.error(1, 163);
 
 		// checks if the options have valid arguments
 		for (int i=0; i<options.length; i++) {
 			x = compilador.position(options[i]);
 			str = args[x+1];
 			if(x>=0 && i==0){										//VERIFY -O
 				compilador.verificar("-o");
 				if(str.indexOf(".s")<0)
 					compilador.error(1,172);
 			}
 			else if(x>=0 && i==1){									//VERIFY -TARGET
 				x = -1;
 				for (int j=0; j<targets.length; j++)
 					if(str.equals(targets[j])) x=1;
 				if(x<0) compilador.error(1,178);
 			} else if (x>=0 && i==2){								//VERIFY -OPT
 				if(!(str.equals("algebraic")||str.equals("constant")))
 					compilador.error(1, 181);
 			} else if (x>=0 && i==3){								//VERIFY -DEGUB
 				x = 0;
 				String[] stages = compilador.separate(str);
 				for (int k=0; k<stages.length; k++)
 					for (int j=0; j<targets.length; j++)
 						if(stages[k].equals(targets[j]))
 							x++;
 				if (!(x==stages.length)) compilador.error(1, 191);
 			}
 		}
 
 		// all ok!
 		System.out.println("------------------------------");
 		System.out.println("Input  File: "+filename);
 
 
 		// o
 		if(o>0){
 			x = compilador.position("-o");
 			output = args[x+1];
 			System.out.println("Output File: "+output);
 			try {
 				outFile = new File(output);
 				outputFile = new PrintWriter(outFile);
 			} catch (Exception e) {compilador.error(1, 208);}
 		} else {
 			x = filename.indexOf(".");
 			str = filename.substring(0, x);
 			str = str + ".s";
 			output = str;
 			System.out.println("Output File: "+str);
 			
 			try {
 				outFile = new File(output);
 				outputFile = new PrintWriter(outFile);
 			} catch (Exception e) {compilador.error(1, 219);}
 		}
 		System.out.println(" ");
 
 
 		// target
 		if(target>0){
 			x = compilador.position("-target");
 			str = args[x+1];
 			for (int i=0; i<targets.length; i++)
 				if(str.equals(targets[i])) x=i;
 
 			for (int i=0; i<=x; i++){
 				outputFile.println("stage: "+targetsP[i]);
 				myOptions.add(targets[i]);
 			}
 		} else{
 			try {
 				outputFile.println("stage: Scanner");
 				outputFile.println("stage: Parser");
 				outputFile.println("stage: Ast");
 				outputFile.println("stage: Semantic");
 				outputFile.println("stage: Irt");
 				outputFile.println("stage: Codegen");
 				for (int i=0; i<targets.length; i++)
 					myOptions.add(targets[i]);
 			} catch (Exception e) {compilador.error(1, 245);}
 		}
 
 		// opt
 		if(opt>0){
 			x = compilador.position("-opt");
 			str = args[x+1];
 			if(str.equals("algebraic")){
 				outputFile.println("\noptimizing: algebraic simplification");
 				Algebraic al = new Algebraic();
 			} else {
 				outputFile.println("\noptimizing: constant folding");
 				ConstantFolding cf = new ConstantFolding();
 			}
 		}
 
 		// debug
 		if(debug>0){
 			x = compilador.position("-debug");
 			str = args[x+1];
 			String[] stages = compilador.separate(str);
 			
 			String debugin[] = compilador.insertionSort(stages, targets);
 
 			for (int i=0; i<debugin.length; i++) {
 				if(myOptions.indexOf(debugin[i])>=0)
 					System.out.println("Debugging: "+debugin[i]);
 			}
 			
 		}
 
 
 
 		outputFile.close();
 		System.out.println("------------------------------");
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
