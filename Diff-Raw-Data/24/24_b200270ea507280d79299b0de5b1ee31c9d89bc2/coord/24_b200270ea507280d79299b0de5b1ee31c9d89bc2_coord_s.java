 package random;
 import javax.swing.*;
 public class coord {
 	//Xf=(a*Xi+b)%m;
 	public static double seed = 1;
 	public static double a = 179424361;
 	public static double b = 11;
 	public static double m = 1000000000;
 	public static double dNom = 100000;
 	public static double[][] matriz;
 	
 	public static void main(String[] args){
 		int id = 1;//ID das estrelas
 		double xAnterior = seed;
 		
 		msg("Programa de criao de coordenadas utilizando\nalgoritimos pseudo-randomicos");
		double n = entrDouble("Entre com o nmero de estrelas a serem geradas");
		matriz(matriz,(int)n);
 		
 		for(int linha=0;linha<matriz.length;linha++){
 			for(int coluna=0;coluna<4;coluna++){
				
 				if(coluna==0){// ID da estrela
 					matriz[linha][coluna]=id;
					id++;
 				}else if(coluna==1){// Coordenada X
 					matriz[linha][coluna]=seeder(xAnterior);
 					xAnterior = matriz[linha][coluna];
 				}else if(coluna==2){// Coordenada Y
 					matriz[linha][coluna]=seeder(xAnterior);
 					xAnterior = matriz[linha][coluna];
 				}else if(coluna==3){// Coordenada Z
 					matriz[linha][coluna]=seeder(xAnterior);
 					xAnterior = matriz[linha][coluna];
 				}
 				
 			}
		}
 		
 	}//main
 	
 	//==============================================================
	public static void matriz(double v[][],int n){
		v = new double[n][4];
	}//matriz
 	
 	public static double seeder(double Xi){
 		double Xf;
 		Xf = (a*Xi+b)%m;
 		Xf = (int)(Xf/dNom);
 		return Xf;
 	}//seeder
 	
 	public static void msg(String msg){
 		JOptionPane.showMessageDialog(null, msg);
 	}//msg
 	
 	public static double entrDouble(String msg){
 		return Double.parseDouble(JOptionPane.showInputDialog(msg));
 	}
 	
 }
