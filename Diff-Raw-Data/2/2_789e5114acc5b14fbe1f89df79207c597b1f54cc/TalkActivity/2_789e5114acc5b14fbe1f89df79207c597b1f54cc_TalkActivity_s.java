 package com.level42.mixit.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.google.inject.Inject;
 import com.level42.mixit.R;
 import com.level42.mixit.listeners.OnTaskPostExecuteListener;
 import com.level42.mixit.models.Interest;
 import com.level42.mixit.models.Speaker;
 import com.level42.mixit.models.Talk;
 import com.level42.mixit.services.ITalkService;
 import com.level42.mixit.tasks.GetTalkAsyncTask;
 import com.level42.mixit.utils.MessageBox;
 import com.level42.mixit.utils.Utils;
 
 /**
  * Ecran de détail d'un talk.
  */
 @ContentView(R.layout.activity_talk)
 public class TalkActivity extends RoboActivity {
 
     /**
      * Identifiant du talk passé en paramètre de l'activité.
      */
     public static final String TALK_ID = "TALK_ID";
 
     /**
      * Interface vers le service de gestion des talks.
      */
     @Inject
     private ITalkService talkService;
 
     /**
      * Contrôle : Titre du talk.
      */
     @InjectView(R.id.talk_textTitre)
     private TextView titreTalk;
     
     /**
      * Contrôle : Picto favoris du talk.
      */
     @InjectView(R.id.talk_imgFavoris)
     private ImageView imgFavoris;
 
     /**
      * Contrôle : Contenu du talk.
      */
     @InjectView(R.id.talk_textContenu)
     private TextView contenuTalk;
 
     /**
      * Contrôle : Centre d'intérêts du talk.
      */
     @InjectView(R.id.talk_textInteret)
     private TextView interestsTalk;
 
     /**
      * Contrôle : Date du talk.
      */
     @InjectView(R.id.talk_textSession)
     private TextView sessionTalk;
 
     /**
      * Contrôle : Salle du talk.
      */
     @InjectView(R.id.talk_textSalle)
     private TextView salleTalk;
 
     /**
      * Contrôle : Niveau du talk.
      */
     @InjectView(R.id.talk_textNiveau)
     private TextView niveauTalk;
 
     /**
      * Contrôle : Liste des speakers du talk.
      */
     @InjectView(R.id.talk_speakers)
     private LinearLayout speakersLayoutTalk;
 
     /**
      * Liste des talkls de l'activité.
      */
     private Talk talk = new Talk();
 
     /**
      * Boite d'attente de chargement.
      */
     private ProgressDialog progressDialog;
 
     /**
      * Listener sur la tâche asynchrone
      */
     private OnTaskPostExecuteListener<Talk> listenerAsync = new OnTaskPostExecuteListener<Talk>() {
         public void onTaskPostExecuteListener(Talk result) {
             if (result != null) {
                 talk = result;
                 try {
                     if (progressDialog.isShowing()) {
                         progressDialog.dismiss();
                     }
                 } catch (IllegalArgumentException ex) {
                     // nop
                 }
                 displayTalk(talk);
             }
         }
 
         public void onTaskInterruptListener(Exception cancelReason) {
             OnClickListener listener = new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog,
                         int which) {
                     finish();
                 }
             };
             MessageBox.showError(
                     getResources().getString(
                             R.string.label_dialog_error),
                     cancelReason.getMessage(), listener,
                     TalkActivity.this);
         }
 
         public void onTaskCancelledListener() {
             MessageBox.showInformation(
                     getResources().getString(
                             R.string.label_dialog_aborted),
                     TalkActivity.this);
         }
     };
     
     /*
      * (non-Javadoc)
      * @see roboguice.activity.RoboActivity#onCreate(android.os.Bundle)
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Talk savedTalk = (Talk) getLastNonConfigurationInstance();
         if (savedTalk == null) {
             this.setupProgressDialog();
             this.loadTalk();
         } else {
             talk = savedTalk;
             this.displayTalk(talk);
         }
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onRetainNonConfigurationInstance()
      */
     @Override
     public Object onRetainNonConfigurationInstance() {
         return this.talk;
     }
 
     /**
      * Affiche la boite de chargement.
      */
     protected void setupProgressDialog() {
         if (progressDialog == null) {
             progressDialog = MessageBox.getProgressDialog(TalkActivity.this);
         }
     }
 
     /**
      * Rafraichit la liste des talks.
      */
     protected void loadTalk() {
 
         // Préparation du service
         GetTalkAsyncTask getTalkAsyncService = new GetTalkAsyncTask();
 
         // Ajout d'un listener pour récupérer le retour
         getTalkAsyncService.setPostExecuteListener(listenerAsync);
 
         // Execution du service
         Integer id = (int) this.getIntent().getExtras()
                 .getLong(TalkActivity.TALK_ID);
         getTalkAsyncService.execute(talkService, id);
     }
 
     /**
      * Gère le rendu du talk dans le template.
      * @param talk Talk a afficher
      */
     protected void displayTalk(Talk talk) {
         
         Resources res = getResources();
 
         titreTalk.setText(String.format(res.getString(R.string.label_talk_titre), talk.getFormat(), talk.getTitle()));        
         contenuTalk.setText(talk.getDescription());
 
         if (!talk.isFavoris()) {
             imgFavoris.setVisibility(View.INVISIBLE);
         }
         
         // Ajout des speakers
         for (Speaker speaker : talk.getSpeakers()) {
             
             LinearLayout layout = new LinearLayout(TalkActivity.this);
             layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
             layout.setOrientation(LinearLayout.HORIZONTAL);
             layout.setPadding(10, 10, 10, 10);
             layout.setGravity(Gravity.CENTER_VERTICAL);
             
             ImageView speakerAvatarView = new ImageView(TalkActivity.this);
             speakerAvatarView.setImageBitmap(speaker.getImage());
             
             layout.addView(speakerAvatarView);
 
             TextView speakerNameView = new TextView(TalkActivity.this);
             speakerNameView.setPadding(10, 0, 0, 0);
             speakerNameView.setText(String.format(res.getString(R.string.label_talk_speaker), speaker.getFirstname(), speaker.getLastname()));
             
             layout.addView(speakerNameView);
             
             speakersLayoutTalk.addView(layout);
         }
 
         if (talk.getInterests() != null) {
             List<String> list = new ArrayList<String>();
             for (Interest interest : talk.getInterests()) {
                 list.add(interest.getName());
             }
             interestsTalk.setText(StringUtils.join(list, ", "));
         }
 
         if (talk.getLevel() != null) {
             String niveau = (String) res.getText(res.getIdentifier("label_talk_" + talk.getLevel(), "string", "com.level42.mixit"));
             niveauTalk.setText(String.format(res.getString(R.string.label_talk_niveau), niveau));
         }
 
         if (talk.getSalleSession() != null) {
             int salleColor = res.getIdentifier("room" + talk.getRoom(), "color", "com.level42.mixit");
             if(salleColor == 0) {
                 salleTalk.setVisibility(View.INVISIBLE);
             } else {
                 salleTalk.setBackgroundColor(res.getColor(salleColor));
                 salleTalk.setVisibility(View.VISIBLE);
                 salleTalk.setText(talk.getSalleSession());
             }
         }
 
         if (talk.getDateSession() != null) {
             sessionTalk.setText(Utils.getPeriodeSession(talk, TalkActivity.this));
         }
     }
 }
