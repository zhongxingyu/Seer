 
 public class Tauler {
 
 	int alc, ampl;
 	
 	Casella casella [][];
 	
 	
 	public Tauler(int amplada, int alcada)
 	{
 		alc=alcada;
 		ampl=amplada;
 		casella = new Casella[alc][ampl];
 		FerTotAigua();
 	}
 
 	public void FerTotAigua()
 	{
 		
 		for(int i=0;i<alc;++i)
 			for(int j=0;j<ampl;++j)
 				casella[i][j]=Casella.AIGUA;
 		
 	}
 	
 	public boolean ColocarVaixell(int y, int x, Vaixell v)
 	{
 		int incrX=0 ;
 		int incrY=0;
 		
		switch (v.getOrientacio() )
 		{
 			case V:
 				incrX=0;
 				incrY=1;
 				break;
 			case H:
 				incrX=1;
 				incrY=0;
 				break;
 		}
 
 		// Copiem el vaixell
 		int xPointer=x;
 		int yPointer=y;
 		
 		for(int i=0;i<v.getMida();++i)
 		{
 			casella[yPointer][xPointer]=Casella.VAIXELL;
 			yPointer+=incrY;
 			xPointer+=incrX;
 		}
 		
 		
 		
 		
 		return true;
 	}
 	
 	public Casella Tret(int x, int y)
 	{
 	}
 	
 }
