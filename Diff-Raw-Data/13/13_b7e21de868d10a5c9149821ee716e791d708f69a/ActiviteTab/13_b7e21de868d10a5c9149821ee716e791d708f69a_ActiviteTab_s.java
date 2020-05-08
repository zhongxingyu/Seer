 package com.ensiie.iaato;
 import com.ensiie.iaato.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class ActiviteTab extends Activity {
   // La cha�ne de caract�res par d�faut
 
 	Button info;
 	Button gene;
 	Button trajet;
 	Button modif;
 	Button annuaire;
 	Button liste;
 
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.home);
     
     info = (Button) this.findViewById(R.id.btn_info);
     info.setOnClickListener(blistener);
     
     gene = (Button) this.findViewById(R.id.btn_gene);
     gene.setOnClickListener(blistener);
     
     trajet = (Button) this.findViewById(R.id.btn_trajet);
     trajet.setOnClickListener(blistener);
     
     modif = (Button) this.findViewById(R.id.btn_modif);
     modif.setOnClickListener(blistener);
     
     annuaire = (Button) this.findViewById(R.id.btn_annuaire);
     annuaire.setOnClickListener(blistener);
     
     liste = (Button) this.findViewById(R.id.btn_liste);
     liste.setOnClickListener(blistener);
 
   
   }
   
 
   private OnClickListener blistener = new OnClickListener()
   {
 	   @Override
 	   public void onClick(View v) 
 	   {
 		  if(v.getId()==R.id.btn_gene)
 		   {
 			
 			   //Intent intent = new Intent(ActiviteTab.this, ShipActivity.class);
 				//startActivity(intent);
 		   }
 		  else  if(v.getId()==R.id.btn_liste)
 		   {
 			
 			  Intent intent = new Intent(ActiviteTab.this, SiteActivity.class);
 				startActivity(intent);
 		   }
<<<<<<< HEAD
		  else  if(v.getId()==R.id.btn_annuaire)
=======
<<<<<<< HEAD
 		  else if(v.getId()==R.id.btn_info)
 		   {
 			
 			  Intent intent = new Intent(ActiviteTab.this, InfoActivity.class);
 				startActivity(intent);
 		   }
		   else
=======
		  else  if(v.getId()==R.id.btn_annuaire)
>>>>>>> annuaire et liste quasi OK
>>>>>>> 9d1fed5169622414db791c1dfd3e98042f322f9a
 		   {
 			  Intent intent = new Intent(ActiviteTab.this, ShipActivity.class);
 			  startActivity(intent);
 		   } 
 		   
 		   
 		   
 			/*Intent intent = new Intent(ActiviteTab.this, SwipeActivity.class);
 			SwipeActivity.setView(v);			
 			startActivity(intent);*/
 			   //Intent intent = new Intent(ActiviteTab.this, SiteActivity.class);
 				//startActivity(intent);
 	   }
   };
   
 
 }
