 package nyoronsheppard.android.SaberyGanar;
 
 
 /**
  * Clase ListButtons
  * @author nyoronsheppard
  * @date   29/03/2012 
  * @version 0.0.1
  */
 public class ListButtons {
 	
 	//Constantes
 	private final int KMAX = 2;
 	
 	//Variables
 	private ButtonSound[] buttonlist;
 	
 	
 	
 	//Constructor
 	public ListButtons()
 	{
 		this.buttonlist = new ButtonSound[KMAX];
 	}
 	
 	//Métodos
 	
 	public void getButtons()
 	{
 	
 		for(int i = 0; i < KMAX; i++)
 		{
 			buttonlist[i] = new ButtonSound("Titulo "+String.valueOf(i+1));
 		}
 
 	}
 	
 	public ButtonSound getPosition(int position)
 	{		
 		return (buttonlist[position]);
 	}
	
 	public int getSize()
 	{
 		return (KMAX);
 	}
 }
