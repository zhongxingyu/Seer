 package frontEnd;
 
 import java.io.FileNotFoundException;
 
 import frontEnd.PrintListener;
 
 import backend.*;
 
 public class Main {
 
 	public static void main(String[] args) {
 
 	Board board = null, boardSolution = null;
 
 	Parser parser=new Parser();
 	if(args.length<2){
 		System.out.println("Error: cantidad de parametros menor a dos");
 		return;
 	}
 
 	try{
 		board = parser.parseLevel(args[0]);
 	}catch (FileNotFoundException e) {
 		// TODO Auto-generated catch block
 		System.out.println("Error:Archivo \""+args[0]+"\"inexistente.");return;
 	}catch(Exception e){
 		System.out.println("Error:archivo invalido.");return;
 	}
 	
 	
 	System.out.println("Se leyo el archivo \""+args[0]+"\".");
 	FlowJframe frame = new FlowJframe(board);
 	
 	Listener l = null;
 	
 	
 	Chronometer chronometer = new Chronometer();//cronometro que se usa para medir el tiempo total tardado por el algoritmo
 
 	if(args[1].equals("exact")){
 		chronometer.start();
 
 		System.out.println("Se busca la solucion en forma exacta");
 		if(args.length>=3 && args[2].equals("progress"))
 			l=new PrintListener(frame);
 		else if(args.length>=3){
 			System.out.println("Error: tercer parametro ingresado invalido ");
 			return;
 		}
 		
 		boardSolution=board.solve(l);
 		chronometer.stop();
 		if(boardSolution==null){
 			System.out.println("El mapa ingresado no tiene soluci�n.");
 			System.out.println("Tiempo demorado:"+chronometer.getElapsedTimeInMilisecs()+"milisegundos");
 			return;
 		}
 			
 	}
 	else if(args[1].equals("approx")){
 		if(args.length<2 ){
 			System.out.println("Error: hay que pasar como parametro el tiempo que se le desea dedicar al algoritmo");
 			return;
 		}
 		System.out.println("Se busca la solucion en forma aproximada con "+Integer.valueOf(args[2])+"segundos");
 		Chronometer timer=new Chronometer(Integer.valueOf(args[2]));
 
 		if(args.length>3 && args[3].equals("progress"))
 			l=new PrintListener(frame);
 		chronometer.start();
 		
 		boardSolution=board.solveAprox(l, timer);
 		if(boardSolution==null){
			System.out.println("No se encontro soluci�n al mapa en el tiempo dado");
 			return;
 		}
 		chronometer.stop();
 	}else{
 		System.out.println("Error:los parametros ingresados son invalidos.");
 		return;
 	}
 	
 		frame.changeBoard(boardSolution);
 		System.out.println("Tiempo transcurrido en obtener la solucion :"+chronometer.getElapsedTimeInMilisecs()+" milisegundos.");
 		
 		frame.showBoard();
 		System.out.println("Cantidad de lugares libres"+boardSolution.unPaintedCells());
		System.out.println("Tablero solución:");
		System.out.println(boardSolution);
 	}
 
 
 
 	
 	
 
 
 
 }
