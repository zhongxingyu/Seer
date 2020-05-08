 package syl.consauto.app;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Formulaire d'ajout / modification d'un plein.
  *
  * @author Sylvain{17/09/2013}
  */
 public class FaireLePleinActivity extends Activity {
 
     // #############################################################################################
     // ###                                       VARIABLES                                       ###
     // #############################################################################################
 
     /**
      * Zone de texte ({@link EditText}) permettant de saisir la date du plein.
      *
      * @return EditText
      */
     private EditText editDate;
 
     /**
      * Liste déroulante ({@link Spinner}) affichant les différents carburants disponibles.
      *
      * @return Spinner
      */
     private Spinner spinChoixCarburant;
 
     /**
      * Zone de texte ({@link EditText}) permettant de saisir le prix du plein.
      *
      * @return EditText
      */
     private EditText editPrix;
 
     /**
      * Zone de texte ({@link EditText}) permettant de saisir la quantité de carburant du plein.
      *
      * @return EditText
      */
     private EditText editQuantite;
 
     /**
      * Zone de texte ({@link EditText}) permettant d'afficher le prix au litre.
      * La zone de texte est pas accessible à la modification. Le contenu est mis à jour via la méthode
      * <code>listeners()</code>.
      *
      * @return EditText
      */
     private EditText editPrixLitre;
 
     /**
      * Zone de texte ({@link EditText}) permettant de saisir la distance parcourue avec le plein.
      *
      * @return EditText
      */
     private EditText editDistance;
 
     /**
      * Zone de texte ({@link EditText}) permettant de saisir la consommation moyenne au cours du plein.
      *
      * @return EditText
      */
     private EditText editConsommation;
 
     /**
      * Case à cocher ({@link CheckBox}) permettant d'indiquer si le plein a été fait completement.
      *
      * @return CheckBox
      */
     private CheckBox chkComplet;
 
     /**
      * Bouton ({@link} Button) permettant d'ajouter ou mettre à jour un plein.
      * Le comportement de ce {@link Button} est défini dans {@link "listeners()"}.
      *
      * @return Button
      */
     private Button bt_enregistrer;
 
     /**
      * Connexion à la base de donnée.
      * Cette variable est initialisé dans {@link "onCreate()"}.
      *
      * @return ConnexionBDD
      */
     private ConnexionBDD connexion;
 
     /**
      * Instance de {@link syl.consauto.app.RecordPleinHandler} contenant les différentes fonctions
      * d'interaction entre un {@link syl.consauto.app.RecordPlein} et la base de données.
      *
      * @return RecordPlein
      */
     private RecordPleinHandler pleinHandler;
 
     /**
      * Stocke l'identifiant du plein que l'on souhaite modifier.
      * Cet identifiant est initialisé dans {@link "initEditID()"}.
      * Lorsqu'il s'agit d'un ajout, cet identifiant est laissé null, sinon, sa valeur est récupérée
      * du paramètre {@code editID} donné à l'{@link Intent} qui déclenche l'activité.
      *
      * @return int
      */
     private int editID;
 
     // #############################################################################################
     // ###                                   FONCTIONS PRIVEES                                   ###
     // #############################################################################################
 
     /**
      * Lie certaines variables avec leur champ du formulaire.
      * Ce mapping n'est donc plus à faire individuellement dans les autres méthodes de cette classe.
      *
      * @return void
      * @author Sylvain{20/09/2013}
      */
     private void initFormFields() {
         // champs text
         editDate         = (EditText) findViewById(R.id.date_form_plein_date);
         editPrix         = (EditText) findViewById(R.id.nb_form_plein_prix);
         editQuantite     = (EditText) findViewById(R.id.nb_form_plein_quantite);
         editPrixLitre    = (EditText) findViewById(R.id.nb_form_plein_prixLitre);
         editDistance     = (EditText) findViewById(R.id.nb_form_plein_distance);
         editConsommation = (EditText) findViewById(R.id.nb_form_plein_conso);
 
         // listes déroulantes
         spinChoixCarburant = (Spinner) findViewById(R.id.spin_form_plein_choix_carburant);
 
         // cases à cocher
         chkComplet = (CheckBox) findViewById(R.id.chk_form_plein_complet);
 
         // boutons
         bt_enregistrer = (Button) findViewById(R.id.bt_form_plein_save);
     }
 
     /**
      * Intialise la variable <tt>editID</tt>.
      *
      * Lorsqu'on souhaite modifier les données d'un plein, on passe un paramètre <code>editID</code>
      * à l'{@link Intent} qui déclenche l'activité. Lorsque ce paramètres est détecté, on récupère
      * les infos en base de données pour initialisé le formulaire avec.
      *
      * {@note <b>[FACTORISATION]</b>}
      * @param savedInstanceState
      * @return void
      * @author Sylvain{26/09/2013}
      */
     private void initEditId(Bundle savedInstanceState) {
         editID = -1;
         if (savedInstanceState == null) {
             Bundle extras = getIntent().getExtras();
             if (extras != null) {
                 editID = (int) extras.getLong("editID");
             }
         } else {
             editID = Integer.parseInt((String) savedInstanceState.getSerializable("editID"));
         }
     }
 
     /**
      * Met à jour le champ <code>Prix au litre</code> du formulaire à partir des données des champs
      * <code>prix</code> et <code>quantité</code>.
      *
      * Cette méthode est applée lorsque les deux données source sont disponible. Pour coller avec le
      * prix affiché à la pompe, le résultat est affiché avec une précision de 3 chiffres après la
      * virgule.
      *
      * {@note <b>[FACTORISATION]</b>}
      * @param editPrix      {@link android.widget.EditText} : Prix du plein
      * @param editQuantite  {@link android.widget.EditText} : Quantité de carburant servie
      * @param editPrixLitre {@link android.widget.EditText} : Champ à mettre à jour avec le résultat de la division
      * @return void
      * @author Sylvain{18/09/2013}
      */
     private void updatePrixLitre(EditText editPrix, EditText editQuantite, EditText editPrixLitre) {
         if (editPrix.getText().length() > 0 && editQuantite.getText().length() > 0) {
             float prix = Float.parseFloat(editPrix.getText().toString());
             float quantite = Float.parseFloat(editQuantite.getText().toString());
 
             DecimalFormat formatter = new DecimalFormat("0.000");
             editPrixLitre.setText(String.valueOf(formatter.format(prix/quantite)));
         }
     }
 
     /**
      * Récupère la valeur contenu dans l'{@link android.widget.EditText} et la renvoie typée en {@link java.lang.Float}.
      *
      * Si le champs est vide, on renvoie un -1.
      *
      * {@note <b>[FACTORISATION]</b>}
      * @param editText {@link android.widget.EditText} : Champs dont il faut extraire la valeur
      * @return Valeur prête à être utilisée pour initialiser le {@link syl.consauto.app.RecordPlein}
      * @author Sylvain{20/09/2013}
      */
     private float getFloatValueFrom(EditText editText) {
         return Float.parseFloat
             (
                 editText.getText().toString().equals("")
                     ? "-1"
                     : editText.getText().toString()
             );
     }
 
     /**
      * Récupère la valeur contenu dans l'{@link android.widget.EditText} et la renvoie typée en {@link java.util.Date}.
      *
      * La date saisie dans le champ du formulaire est au format <code>dd / MM / yyyy</code>.
      *
      * {@note <b>[FACTORISATION]</b>}
      * @param editText {@link android.widget.EditText} : Champs dont il faut extraire la valeur
      * @return Valeur prête à être utilisée pour initialiser le {@link syl.consauto.app.RecordPlein}
      * @author Sylvain{20/09/2013}
      */
     private Date getDateValueFrom(EditText editText) {
         try {
             return new SimpleDateFormat(getString(R.string.format_date_standard)).parse(editText.getText().toString());
         } catch (ParseException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     /**
      * Récupère l'index du <code>carburant</code> donné en paramètre dans la liste des carburants disponible.
      *
      * Cette liste est codée en dur dans <code>R.array.liste_carburants</code>.
      * Cette méthode permet d'initialiser la liste déroulante.
      *
      * {@note <b>[FACTORISATION]</b>}
      * @param carburant Nom du carburant utilisé (ex: Sans plomb 95)
      * @return Pour le <em>Sans plomb 95</em>, on renvoie 1
      * @author Sylvain{26/09/2013}
      */
     private int getPositionFromCarburant(String carburant) {
         String[] carburants = getResources().getStringArray(R.array.liste_carburants);
         int position = -1;
 
         for (int i = 0 ; i < carburants.length ; i++) {
             if (carburants[i].equals(carburant)) {
                 return i;
             }
         }
 
         return position;
     }
 
     /**
      * Indique si il s'agit d'un ajout d'un nouveau plein ou de la modification d'un plein existant.
      *
      * {@note <b>[FACTORISATION]</b>}
      * @return vrai(true) si un identifiant à été donnée en paramètre de l'{@link Intent} qui déclenche l'activité.
      * @author Sylvain{26/09/2013}
      */
     private boolean isEditContext() {
         return editID > 0;
     }
 
     // #############################################################################################
     // ###                                     CONSTRUCTEURS                                     ###
     // #############################################################################################
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.form_plein);
 
 
         // connexion à la base de données
         connexion = new ConnexionBDD(this);
         pleinHandler = new RecordPleinHandler(connexion);
 
         initEditId(savedInstanceState);
         initFormFields();
 
         if (isEditContext()) {
             defaults(pleinHandler.get(editID));
             setEditLabels();
         } else {
             defaults();
         }
         listeners();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         initEditId(null);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.faire_le_plein, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()){
             case R.id.action_liste:
                 startActivity(new Intent(this, ListeActivity.class));
                 return true;
             case R.id.action_main:
                 startActivity(new Intent(this, MainActivity.class));
                 return true;
         }
         return false;
     }
 
     // #############################################################################################
     // ###                              VALEURS PAR DEFAUT COMPLEXES                             ###
     // #############################################################################################
 
     /**
      * Défini les valeurs par défaut des champs du formulaire d'ajout d'un nouveau plein.
      *
      * @return void
      * @author Sylvain{18/09/2013}
      */
     public void defaults() {
         // date d'aujourd'hui
         editDate.setText(new SimpleDateFormat(getString(R.string.format_date_standard)).format(new Date()));
 
         // liste des carburants
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.liste_carburants, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinChoixCarburant.setAdapter(adapter);
		
		// masque les champs Distance / Consommation / Plein
		findViewById(R.id.txt_form_plein_more).setVisibility(View.INVISIBLE);
		findViewById(R.id.layout_plein_more).setVisibility(View.INVISIBLE);
		findViewById(R.id.chk_form_plein_complet).setVisibility(View.INVISIBLE);
     }
 
     /**
      * Initialise le formulaire avec les valeurs du {@link syl.consauto.app.RecordPlein} donné en paramètre.
      *
      * Cette initialisation à lieu lorsqu'il s'agit de modifier un plein existant.
      * L'appel à <code>defaults()</code> permet de récupérer la liste des carburants.
      *
      * @param recordPlein {@link syl.consauto.app.RecordPlein} : Contient les valeur à placer dans les champs du formulaire
      * @return void
      * @author Sylvain{26/09/2013}
      */
     private void defaults(RecordPlein recordPlein) {
         defaults();
 
         editDate.setText(recordPlein.getFormattedDate());
         editQuantite.setText(String.valueOf(recordPlein.getQuantite()));
         spinChoixCarburant.setSelection(getPositionFromCarburant(recordPlein.getCarburant()));
         editPrix.setText(String.valueOf(recordPlein.getPrix()));
         editDistance.setText(String.valueOf(recordPlein.getDistance()));
         editConsommation.setText(String.valueOf(recordPlein.getConsommation()));
         chkComplet.setChecked(recordPlein.isPlein());
 
         updatePrixLitre(editPrix, editQuantite, editPrixLitre);

		// affiche les champs Distance / Consommation / Plein
		findViewById(R.id.txt_form_plein_more).setVisibility(View.VISIBLE);
		findViewById(R.id.layout_plein_more).setVisibility(View.VISIBLE);
		findViewById(R.id.chk_form_plein_complet).setVisibility(View.VISIBLE);
     }
 
     /**
      * Modifie les labels du formulaire lorsqu'il s'agit d'une modification.
      *
      * "<em>Ajout un nouveau plein</em>" devient "<em>Modifier un plein</em>".
      * "<em>Enregistrer</em>" devient "<em>Mettre à jour</em>".
      *
      * @return void
      * @author Sylvain{26/09/2013}
      */
     private void setEditLabels()
     {
         TextView titre = (TextView) findViewById(R.id.txt_form_plein);
         titre.setText(R.string.form_plein_titre_modifier);
 
         TextView bouton = (TextView) findViewById(R.id.bt_form_plein_save);
         bouton.setText(R.string.form_plein_modifier);
     }
 
     // #############################################################################################
     // ###                                 LISTENERS / HANDLERS                                  ###
     // #############################################################################################
 
     /**
      * Attache les listeners adéquats aux champs du formulaire.
      *
      * Les champs concernés sont :
      * <ul>
      *     <li>
      *         {@link android.widget.EditText} editQuantite : {@link android.text.TextWatcher} pour
      *         détecter la saisie d'une valeur et mettre à jour le prix au litre.
      *     </li>
      *     <li>
      *         {@link android.widget.EditText} editPrix : {@link android.text.TextWatcher} pour
      *         détecter la saisie d'une valeur et mettre à jour le prix au litre.
      *     </li>
      *     <li>
      *         {@link android.widget.Button} bt_enregistrer : {@link android.content.DialogInterface.OnClickListener}
      *         pour enregistrer le formulaire et ajouter ou modifier un plein. Si l'enregistrement
      *         s'est bien passé, on retoure à la liste des pleins
      *     </li>
      * </ul>
      *
      * @return void
      * @author Sylvain{18/09/2013}
      */
     public void listeners() {
         // calcul du prix au litre par modification de la quantité
         editQuantite.addTextChangedListener(new TextWatcher() {
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
             public void afterTextChanged(Editable editable) { updatePrixLitre(editPrix, editQuantite, editPrixLitre);}
         });
 
         // calcul du prix au litre par modification du prix
         editPrix.addTextChangedListener(new TextWatcher() {
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
             public void afterTextChanged(Editable editable) { updatePrixLitre(editPrix, editQuantite, editPrixLitre);}
         });
 
         // enregistrement du formulaire
         bt_enregistrer.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 RecordPlein plein = new RecordPlein();
 
                 plein.setDate(getDateValueFrom(editDate))
                      .setCarburant(spinChoixCarburant.getSelectedItem().toString())
                      .setQuantite(getFloatValueFrom(editQuantite))
                      .setPrix(getFloatValueFrom(editPrix))
                      .setDistance(getFloatValueFrom(editDistance))
                      .setConsommation(getFloatValueFrom(editConsommation))
                      .setPlein((chkComplet.isChecked()) ? true : false);
 
 
                 boolean isSuccess = (isEditContext())
                     ? pleinHandler.update(editID, plein)
                     : pleinHandler.save(plein);
 
                 if (isSuccess)
                     startActivity(new Intent(getApplicationContext(), ListeActivity.class));
             }
         });
     }
 
     /**
      * Affiche le calendrier pour la saisie de la date;
      *
      * @note If your app supports versions of Android lower than 3.0, be sure that you call
      * getSupportFragmentManager() to acquire an instance of FragmentManager. Also make sure that3
      * your activity that displays the time picker extends FragmentActivity instead of the standard
      * Activity class.
      *
      * @see "http://developer.android.com/guide/topics/ui/controls/pickers.html"
      * @param v
      * @author Sylvain{18/09/2013}
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     public void showDatePickerDialog(View v) {
         DialogFragment newFragment = new DatePickerFragment();
         newFragment.show(getFragmentManager(), "datePicker");
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private class DatePickerFragment extends DialogFragment
                                      implements DatePickerDialog.OnDateSetListener
     {
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Use the current date as the default date in the picker
             final Calendar c = Calendar.getInstance();
             SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.format_date_standard));
 
             Date date;
             try {
                 date = sdf.parse(editDate.getText().toString());
             } catch (ParseException e) {
                 date = new Date();
             }
 
             c.setTime(date);
 
             return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
         }
 
         public void onDateSet(DatePicker view, int year, int month, int day) {
             Calendar c = Calendar.getInstance();
             SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.format_date_standard));
             c.set(year, month, day);
 
             editDate.setText(sdf.format(c.getTime()));
         }
     }
}
