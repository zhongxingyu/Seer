 package src;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 
 public class Client18 {
 	
 	public static double[] getUnary(int zahl){
 		double[][] zahlenUnary = new double[10][10];
 		double[] zahl0 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
 		double[] zahl1 = {0, 0, 0, 0, 0, 0, 0, 0, 1, 0};
 		double[] zahl2 = {0, 0, 0, 0, 0, 0, 0, 1, 0, 0};
 		double[] zahl3 = {0, 0, 0, 0, 0, 0, 1, 0, 0, 0};
 		double[] zahl4 = {0, 0, 0, 0, 0, 1, 0, 0, 0, 0};
 		double[] zahl5 = {0, 0, 0, 0, 1, 0, 0, 0, 0, 0};
 		double[] zahl6 = {0, 0, 0, 1, 0, 0, 0, 0, 0, 0};
 		double[] zahl7 = {0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
 		double[] zahl8 = {0, 1, 0, 0, 0, 0, 0, 0, 0, 0};
 		double[] zahl9 = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 //		double[] zahl0 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
 //		double[] zahl1 = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1};
 //		double[] zahl2 = {0, 0, 0, 0, 0, 0, 0, 1, 1, 1};
 //		double[] zahl3 = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1};
 //		double[] zahl4 = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1};
 //		double[] zahl5 = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1};
 //		double[] zahl6 = {0, 0, 0, 1, 1, 1, 1, 1, 1, 1};
 //		double[] zahl7 = {0, 0, 1, 1, 1, 1, 1, 1, 1, 1};
 //		double[] zahl8 = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1};
 //		double[] zahl9 = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};		
 		zahlenUnary[0] = zahl0;
 		zahlenUnary[1] = zahl1;
 		zahlenUnary[2] = zahl2;
 		zahlenUnary[3] = zahl3;
 		zahlenUnary[4] = zahl4;
 		zahlenUnary[5] = zahl5;
 		zahlenUnary[6] = zahl6;
 		zahlenUnary[7] = zahl7;
 		zahlenUnary[8] = zahl8;
 		zahlenUnary[9] = zahl9;
 		return zahlenUnary[zahl];
 	}
 
 	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<Dataset> training = new Reader("testing.txt").getDatasets();
 		PerzeptronNetz netz = new PerzeptronNetz(16,10);
 		LinkedList<Muster> muster = new LinkedList<Muster>();
 		
 		for(Dataset d: training){
 			muster.add(new Muster( d.features, getUnary(d.correctKlass)));
 		}
 		// lernen
 		int it = netz.lernen_all(muster);
 		System.out.println("gelernt in " + it + " Lernschritten.");
 		
 		// testen
		ArrayList<Dataset> testen = new Reader("training.txt").getDatasets();
 		int erfolg = 0;
 		for(Dataset d:testen){
 			double[] soll = getUnary(d.correctKlass);
 			Muster m = new Muster(d.features, soll);
 			double[] ergebnis = netz.ergebnisZuMuster(m);
 			System.out.println("ergebnis: " + Arrays.toString( ergebnis));
 			System.out.println("soll:     " + Arrays.toString(soll));
 			
 			boolean korrekt = true;
 			for(int i=0; i<soll.length; i++){
 				if (soll[i] != ergebnis[i]){
 					korrekt = false;
 				}
 			}
 			
 			if(korrekt){
 				erfolg++;
 			}
 		}
 		System.out.println(erfolg/(double)testen.size());
 		
 //		
 //		
 //		
 //		PerzeptronNetz netz = new PerzeptronNetz(4,8);
 //
 //		LinkedList<Muster> muster = new LinkedList<Muster>();
 //		double[] i0 = {0,0,0}; double[] o0= {0,0,0,0,0,0,0,0}; Muster m_0 = new Muster(i0,o0); muster.add(m_0);
 //		double[] i1 = {0,0,1}; double[] o1= {1,0,0,0,0,0,0,0}; Muster m_1 = new Muster(i1,o1); muster.add(m_1);
 //		double[] i2 = {0,1,0}; double[] o2= {1,1,0,0,0,0,0,0}; Muster m_2 = new Muster(i2,o2); muster.add(m_2);
 //		double[] i3 = {0,1,1}; double[] o3= {1,1,1,0,0,0,0,0}; Muster m_3 = new Muster(i3,o3); muster.add(m_3);
 //		double[] i4 = {1,0,0}; double[] o4= {1,1,1,1,0,0,0,0}; Muster m_4 = new Muster(i4,o4); muster.add(m_4);
 //		double[] i5 = {1,0,1}; double[] o5= {1,1,1,1,1,0,0,0}; Muster m_5 = new Muster(i5,o5); muster.add(m_5);
 //		double[] i6 = {1,1,0}; double[] o6= {1,1,1,1,1,1,0,0}; Muster m_6 = new Muster(i6,o6); muster.add(m_6);
 //		double[] i7 = {1,1,1}; double[] o7= {1,1,1,1,1,1,1,0}; Muster m_7 = new Muster(i7,o7); muster.add(m_7);
 //		
 //		
 //		System.out.println("nicht gelernt:");
 //		netz.ergebnisZuMuster_all(muster);
 //		
 //		System.out.println("lernen...");
 //		int it = netz.lernen_all(muster);
 //		
 //		
 //		System.out.println("nach "+it+" Lernschritten gelernt!");
 //		netz.ergebnisZuMuster_all(muster);
 //		
 	}
 
 }
