 package com.tdam2013.grupo05;
 
import com.tdam2013.grupo05.utiles.UtilesIntents;

 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 public class EnviarMensajeWebActivity extends Activity {
 
     public static final int DIALOG_ERROR = 1;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.enviar_mensaje_web_activity);
 
         /*
          * Listeners
          */
         getButton(R.id.enviar_mensaje_web_button).setOnClickListener(
                 new EnviarMensajeWebOnClickListener());
 
     }
 
     /**
      * Dialog
      */
     protected Dialog onCreateDialog(int id) {
         if (id == DIALOG_ERROR) {
             Dialog dialog = new AlertDialog.Builder(this).setTitle("ERROR")
                     .setMessage("No se ha podido enviar el mensaje web.").create();
             return dialog;
         } else {
             return super.onCreateDialog(id);
         }
     }
 
     /**
      * OnClickListener
      */
     public class EnviarMensajeWebOnClickListener implements View.OnClickListener {
 
        boolean mostrarDialogo = true;

         @Override
         public void onClick(View v) {
            if (mostrarDialogo) {
                EnviarMensajeWebActivity.this.showDialog(DIALOG_ERROR);
            } else {
                EnviarMensajeWebActivity.this.startActivity(UtilesIntents
                        .getRegistrarUsuarioActivityIntent(EnviarMensajeWebActivity.this));
            }
            mostrarDialogo = !mostrarDialogo;
         }
     }
 
     /*
      * Utiles
      */
     public Button getButton(int id) {
         return (Button) this.findViewById(R.id.enviar_mensaje_web_button);
     }
 
 }
