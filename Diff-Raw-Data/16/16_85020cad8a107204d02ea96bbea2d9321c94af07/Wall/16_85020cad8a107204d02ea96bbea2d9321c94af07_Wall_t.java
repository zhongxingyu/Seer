 import java.io.FileReader;
 import java.io.IOException;
 
 
 public class Wall {
 
 	public static void checkp() throws IOException{
 		
 
 	    // Wie bei GetRoom nur diesmal zum berprfen ob wir an einer Wand sind
 	    FileReader fr = new FileReader(Globals.fileName);
 	    int ch;
 	    int j=0;
 	    int k=0;
 	    double x=Globals.xneu;
 	    double y=Globals.yneu;
 	    double s;
 	    double t;
 	   
 	    while( j<21 ){
 	    	k=0;
 	    	while ((ch=fr.read()) != -1 &&k<22){
 	    		
 	    	
 	    	
 	    	if (ch==120){
 	    		s=k*0.05;
 	    		t=1-j*0.05;
 
 	    		if(s-0.05< x && x< s+0.05 && t-0.05<y && t+0.05>y  ){	// Wenn wir eine Wand erreichen drfen wir in dieswe Richtung nicht weiter laufen.
 	    			j=21;												// Weil sonst nur die 2 Schleife beendet wird
 	    			break;	
 	    		}
 	    		
 	    			    }
 		
 	    			k++;}
 	    	
 	    		j++;
 	    		if (j==21){		//sollte keine Wand im Weg stehen speicher die neue Position, die spter gezeichnet wird.
 	    			Globals.x=Globals.xneu;
 	    			Globals.y=Globals.yneu;
 	    			
 	    		}
 	    		}
 	fr.close();}
 
 	public static void checkf(int i) throws IOException{
 		
 
 		// Wie bei GetRoom nur diesmal zum berprfen ob wir an einer Wand sind
 	    FileReader fr = new FileReader(Globals.fileName);
 	    int ch;
 	    int j=0;
 	    int k=0;
 	    double x=Globals.falleneu[0];
 	    double y=Globals.falleneu[1];
 	   
 	    double s;
 	    double t;
 	   
 	    while( j<21 ){
 	    	k=0;
 	    	while ((ch=fr.read()) != -1 &&k<22){
 	    		
 	    	
 	    	
 	    	if (ch==120){
 	    		s=k*0.05;
 	    		t=1-j*0.05;
	    		int r = (int) (Math.random()*4+1);
	    		if(s-0.05< x && x< s+0.05 && t-0.05<y && t+0.05>y || x>0.99 || x<0.01 || y<0.01 || y>0.99 ){	// Wenn wir eine Wand erreichen dann soll die Falle ihre Richtung ndern
 	    			if(Globals.richtung[i]==1){
	    				Globals.richtung[i]=r;
 	    			}
 	    			else if(Globals.richtung[i]==2){
	    				Globals.richtung[i]=r;
 	    			}
 	    			else if(Globals.richtung[i]==3){
	    				Globals.richtung[i]=r;
 	    			}
 	    			else if(Globals.richtung[i]==4){
	    				Globals.richtung[i]=r;
 	    				}
 	    			j=21;
 	    			break;	
 	    		}
 	    		
 	    			    }
 		
 	    			k++;}
 	    	
 	    		j++;
 	    		if (j==21){
 	    			
 	    			Globals.fallen[0+2*i]=Globals.falleneu[0];		// wenn die Falle in keiner Wand landet ndert sich die Richtung nicht und die neue Position wird gespeichert und spter gezeichnet.
 	    			Globals.fallen[1+2*i]=Globals.falleneu[1];
 	    			
 	    			
 	    		}
 	    		}
 	fr.close();}
 
 }
 
