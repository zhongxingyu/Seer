 
 import java.io.FileReader;
 import java.io.IOException;
 
 
 public class GetRoom {
 
 	 
 	  public static void room(int i) throws IOException
 	  {
 		  double[] array = new double [11];
 		  int[] richtung= new int [5];
 		  double[] boss= new double [10];
 	    
 		
 		StdDraw.setPenColor(StdDraw.BLACK);
 		StdDraw.setPenRadius(0.01);
 	    //String fileName="C:/Users/Nuck/workspace/TestRealm/src/Test.txt";
 	    FileReader fr = new FileReader(Globals.fileName);
 	    int ch;
 	    int j=0;
 	    int k=0;
 	    int n=0;
 	    int m=0;
 	    while( j<21 ){
 	    	k=0;
 	    	while ((ch=fr.read()) != -1 &&k<22){//lese jede Ziffer einzelt aus und speicher in Variable ch
 	    		
 	    	
 	    	
 	    	if (ch==120){//Erstelle einen Raum durch Textdatei
 
 	    		StdDraw.filledSquare(0.05*k,1- 0.05*j, 0.026);
 	    			    		}
 	    	else if(ch==115){
 	    		Globals.startx=k*0.05;//Speicher Startkoordinaten (Spieler Start)
 	    		Globals.starty=1-j*0.05;
 
 
 	    		StdDraw.setPenColor(StdDraw.RED);//schreibe Start
 	    		StdDraw.text(0.05*k,1-0.05*j, "Start");
 	    		StdDraw.setPenColor(StdDraw.BLACK);
 	    			}
 	    	else if(ch==122){
 	    		Globals.zielx=k*0.05;//Speicher Zielkoordinaten
 	    		Globals.ziely=1-j*0.05;
 	    		
 	    		StdDraw.setPenColor(StdDraw.RED);//schreibe Ziel
 	    		StdDraw.text(0.05*k,1- 0.05*j, "Ziel");
 	    		StdDraw.setPenColor(StdDraw.BLACK);
 	    		}
 	    	else if(ch==101 && i==0){
 	    		richtung[n]=1;	//Richtung in die sich die Falle bewegt (ndert sich in Wall)
 	    		array[0+2*n]=k*0.05;	//Speichert die Koordinaten von den Gegnern
 	    		array[1+2*n]=1-j*0.05;	
 	    		n++;
 	    		Globals.anzahlfallen=n;
 	    	    Globals.arraylokal = array;
 	    	    Globals.richtung=richtung;
 	    		}
 	    	else if(ch==83 && i==0){
 	    		Globals.shopx=k*0.05;	//Koordinaten fr den Shop
	    		Globals.shopy=k*0.05;
 	    		}
 	    	else if(ch==66 && i==0){
 	    		boss[0+2*m]=k*0.05;
 	    		boss[1+2*m]=1-j*0.05;
 	    		m++;
 	    		Globals.anzahlboss=m;
 	    		Globals.boss=boss;
 	    		}
 	    	k++;
 	    	}
 	    j++;}
 	    
 	    
 
 	    fr.close();}
 
 
 }
