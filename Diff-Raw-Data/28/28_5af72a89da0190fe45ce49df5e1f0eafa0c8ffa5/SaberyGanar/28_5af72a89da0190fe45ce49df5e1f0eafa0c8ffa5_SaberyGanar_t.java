 package nyoronsheppard.android.SaberyGanar;
 
 
 //Paquetes Android
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * Clase Principal del programa
  * @author nyoronsheppard
  *
  */
 public class SaberyGanar extends Activity 
 {
 	
	private ListButtons frases = new ListButtons();  
	
	public final int KMAX = 2;
	
	private ButtonSound[] buttons = new ButtonSound[KMAX];
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         //this es el array de titulares
         AdaptadorTitulares adaptador = new AdaptadorTitulares(this);
         
         ListView lstOpciones = (ListView)findViewById(R.id.LstOpciones);
         
         lstOpciones.setAdapter(adaptador); 
         
        frases.getButtons();
        
        for(int i = 0; i < KMAX; i++)
        {
        	buttons[i] = frases.getPosition(i);
        	//buttons[i] = new ButtonSound("Hola");
        }
         
     }
     
 
     //Clase que nos guarda IDs de layouts para ahorrar memoria
     static class ViewHolder 
     {
         TextView titlesound;
     }
     
     //Adaptador para Incorporar los botones de Clase Titular
     class AdaptadorTitulares extends ArrayAdapter
     {
     	 
         Activity context;
      
         	//Constructor de la clase
             @SuppressWarnings("unchecked")
 			AdaptadorTitulares(Activity context) 
             {
            	super(context, R.layout.buttonsound, buttons);
                 this.context = context;
             }
      
             public View getView(int position, View convertView, ViewGroup parent)
             {
                 View item = convertView;
                 ViewHolder holder;
              
                 //Con esto ahorramos CPU (no hay que generar los items que no aparecen en la pantalla
                 if(item == null)
                 {
                     LayoutInflater inflater = context.getLayoutInflater();
                     item = inflater.inflate(R.layout.buttonsound, null);
              
                     //Creamos el ViewHolder y guardamos los ID del Objeto
                     holder = new ViewHolder();
                     holder.titlesound = (TextView)item.findViewById(R.id.LblTitulo);
              
                     item.setTag(holder);
                 }
                 else
                 {
                     holder = (ViewHolder)item.getTag();
                 }
              
                 //Metodos get de la clase Titulo
                holder.titlesound.setText(buttons[position].getTitleSound());
                            
                 return(item);
             }
     }
     
 }
 
