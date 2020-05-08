 
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
 	}
 	
 	public Casella Tret(int x, int y)
 	{
 	}
 	
 }
