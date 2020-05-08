 package quakeagent;
 
 import java.util.Vector;
 import java.util.Random;
 
 import soc.qase.bot.ObserverBot;
 import soc.qase.file.bsp.BSPParser;
 import soc.qase.state.Player;
 import soc.qase.state.PlayerMove;
 import soc.qase.state.World;
 import soc.qase.tools.vecmath.Vector3f;
 
 import soc.qase.state.*;
 
 import java.lang.Math;
 
 import jess.*;
 
 public class QuakeAgent {
         
     static MiBotseMueve MiBot,MiBot2;  
     
     public static void main(String[] args) {
         // TODO code application logic here
         Init();	
     }
     
     public static void Init()
 	{		
 		//Establece la ruta del quake2, necesaria para tener informaciÃ³n sobre los mapas.
 		//Observa la doble barra
 		String quake2_path="/usr/share/games/quake2";
 		System.setProperty("QUAKE2", quake2_path); 
 
 
 		//CreaciÃ³n del bot (pueden crearse mÃºltiples bots)
 		MiBot = new MiBotseMueve("SoyBot","male/athena");		
 		
 		//Conecta con el localhost (el servidor debe estar ya lanzado para que se produzca la conexiÃ³n)
		MiBot.connect("192.168.1.61",27910);//Ejemplo de conexiÃ³n a la mÃ¡quina local
              
 	}
 }
