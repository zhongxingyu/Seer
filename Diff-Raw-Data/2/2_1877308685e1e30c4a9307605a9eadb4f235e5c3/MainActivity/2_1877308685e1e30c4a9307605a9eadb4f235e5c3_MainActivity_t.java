 package com.laurent.chibre;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RatingBar;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnRatingBarChangeListener {
 
 	// Valeurs par defaut
 	private final int INIT_RATING = 1;
 	private final int TOUT_ATOUT_RATING = 6;
 
 	// Noms des champs a sauvegarder
 	private final String SAVE_SCORE_1 = "score1";
 	private final String SAVE_SCORE_2 = "score2";
 	private final String SAVE_RATING = "rating";
 
 	// Nombre de points en cas de match
 	private final int BONUS = 100;
 	private final int MATCH_VALUE = 157;
 	private final int MATCH_TOUTATOUT_VALUE = 262;
 	
 	// Elements de controles
 	private RatingBar ratingBar;
 	EditText input_score1;
 	EditText input_score2;
 
 	// Score des equipes
 	private int[] score = { 0, 0 };
 	// Coefficient de la partie (P.ex: 2x pour pique double)
 	private int coefficient = 1;
 
 	/**
 	 * Ajoute une annonce a une equipe
 	 * @param team L'equipe (1/2)
 	 * @param announce Le nombre de points de l'annonce
 	 */
 	private void addAnnounce(final int team, final int announce) {
 		final int i = (team == 1 ? 0 : 1);
 		score[i] += announce;
 		// Annonce peut etre negative
 		if (score[i] < 0)
 			score[i] = 0;
 
 		displayScore(team, score[i]);
 	}
 
 	/**
 	 * Ajoute des points aux equipes
 	 * @param team L'ID de l'equipe
 	 * @param point Points de l'equipe
 	 * @param max Nombre de points maximum de la partie (sans le bonus de match)
 	 * @param coeff Coefficient de la partie (P.ex 2 pour pique double)
 	 */
 	private void addPoints(final int team, int point, int max, final int coeff) {
 		// Multiplie les points suivant le coeficient
 		max *= coeff;
 		point *= coeff;
 
 		// Le reste est pour l'autre equipe
 		int reste = max - point;
 		// Si le reste est negatif (on a un match)
 		if (reste < 0)
 			reste = 0;
 		// Si le score est de 0, l'autre equipe a un match
 		if (point == 0)
 			reste = max + BONUS * coeff;
 		
 		// Calcule du score
 		if (team == 1) {
 			score[0] += point;
 			score[1] += reste;
 		} else {
 			score[0] += reste;
 			score[1] += point;
 		}
 		// Affiche le score
 		displayScore(1, score[0]);
 		displayScore(2, score[1]);
 	}
 
 	/**
 	 * Efface le score
 	 */
 	private void reset() {
 		score[0] = score[1] = 0;
 		displayScore(1, score[0]);
 		displayScore(2, score[1]);
 		ratingBar.setRating(INIT_RATING);
 	}
 
 	/**
 	 * Affiche le score
 	 * 
 	 * @param team
 	 *            L'equipe (1 ou 2)
 	 * @param score
 	 *            Le score
 	 */
 	private void displayScore(int team, int score) {
 		String displayScore = getString(R.string.score);
 		TextView txt1 = (TextView) findViewById(R.id.txt_score1);
 		TextView txt2 = (TextView) findViewById(R.id.txt_score2);
 		displayScore = displayScore.replace("0", score + "");
 		(team != 1 ? txt2 : txt1).setText(displayScore);
 	}
 
 	/**
 	 * Gere les boutons lors de la saisie. (Couleur, Inhibation)
 	 */
 	public void checkInput() {
 		// Si les inputs 1 et 2 sont vides
 		boolean isEmpty1 = input_score1.getText().toString().equals("");
 		boolean isEmpty2 = input_score2.getText().toString().equals("");
 		// Recupere les boutons
 		Button btn_score = (Button) findViewById(R.id.btn_score);
 		Button btn_announcement = (Button) findViewById(R.id.btn_announcement);
 		// Active les boutons
 		btn_score.setEnabled(true);
 		btn_announcement.setEnabled(true);
 
 		// Style des boutons par default
 		int style_an = (isEmpty2 ? R.style.Team1 : R.style.Team2);
 		int style_score = R.style.TeamAll;
 
 		// Si les 2 champs de saisie sont vide ou pleins,
 		// on desactive les boutons
 		if ((isEmpty1 && isEmpty2) || (!isEmpty1 && !isEmpty2)) {
 			btn_score.setEnabled(false);
 			btn_announcement.setEnabled(false);
 			style_an = style_score = R.style.TeamNone;
 		}
 		// Active la saisie du score pour une seule equipe a la fois
 		input_score1.setEnabled(isEmpty2);
 		input_score2.setEnabled(isEmpty1);
 
 		// Applique le style sur les boutons
 		btn_score.setTextAppearance(getApplicationContext(), style_score);
 		btn_announcement.setTextAppearance(getApplicationContext(), style_an);
 
 	}
 
 	/**
 	 * Lorsque l'activite est cree.
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Gere la bare de "rating"
 		ratingBar = (RatingBar) findViewById(R.id.multiply);
 		ratingBar.setOnRatingBarChangeListener(this);
 		ratingBar.setRating(INIT_RATING);
 
 		// Recupere les champs d'entree et ecoute les saisies
 		input_score1 = (EditText) findViewById(R.id.input_score1);
 		input_score2 = (EditText) findViewById(R.id.input_score2);
 
 		// Verif des saisies a la volee
 		input_score1.addTextChangedListener(new TextWatcherAdapter());
 		input_score2.addTextChangedListener(new TextWatcherAdapter());
 		checkInput();
 
 	}
 
 	/**
 	 * Lorsque l'application est interompue, on sauve le score
 	 */
 	public void onPause() {
 		super.onPause();
 
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		SharedPreferences.Editor ed = sharedPrefs.edit();
 		// Valeurs a sauver
 		ed.putInt(SAVE_SCORE_1, score[0]);
 		ed.putInt(SAVE_SCORE_2, score[1]);
 		ed.putFloat(SAVE_RATING, ratingBar.getRating());
 		// Sauve
 		ed.commit();
 	}
 
 	/**
 	 * Lorsque l'application est reprise, on restaure le score
 	 */
 	public void onResume() {
 		super.onResume();
 
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		// Valeur a restaurer
 		score[0] = sharedPrefs.getInt(SAVE_SCORE_1, 0);
 		score[1] = sharedPrefs.getInt(SAVE_SCORE_2, 0);
 		ratingBar.setRating(sharedPrefs.getFloat(SAVE_RATING, 1.0f));
 
 		// Raffraichit la vue
 		displayScore(1, score[0]);
 		displayScore(2, score[1]);
 		checkInput();
 	}
 
 	/**
 	 * Lors de la selection d'un element du menu
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_reset:
 			// Confirmation pour reset()
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.score_reset_confirm))
 					.setCancelable(true)
 					.setPositiveButton(getString(android.R.string.yes),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// Reset
 									reset();
 									dialog.cancel();
 								}
 							})
 					.setNegativeButton(getString(android.R.string.no),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// Annule
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 		case R.id.menu_about:
 			// Affiche la boite de dialgue "A propos"
 			About.showAbout(this);
 			return true;
 			
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * Action lorsqu'on clique sur un des boutons
 	 * 
 	 * @param v
 	 *            Le bouton clique
 	 */
 	public void onClick(View v) {
 		// Verifie les champs
 		String error = "";
 		// Les 2 sont vides
 		if (input_score1.getText().toString().equals("")
 				&& input_score2.getText().toString().equals("")) {
 			error = getString(R.string.error_empty);
 		}
 		// Les 2 sont pleins
 		if (!input_score1.getText().toString().equals("")
 				&& !input_score2.getText().toString().equals("")) {
 			error = getString(R.string.error_full);
 		}
 		// Affiche l'erreur
 		if (!error.equals("")) {
 			Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG)
 					.show();
 			return;
 		}
 
 		// Regarde si le score est dans le champ 1 ou 2.
 		boolean team1 = !input_score1.getText().toString().equals("");
 
 		// Recuperer les points de l'equipe
 		int point = 0;
 		if (team1) {
 			point = Integer.parseInt(input_score1.getText().toString());
 		} else {
 			point = Integer.parseInt(input_score2.getText().toString());
 		}
 
 		/* calculs les points de l'equipe */
 		// Regarde si on a tout atout
 		boolean toutAtout = ratingBar.getRating() == TOUT_ATOUT_RATING;
 		// Points maximum
 		int max = MATCH_VALUE;
 		// En tout atout, la partie vaut plus de points
 		if (toutAtout)
 			max = MATCH_TOUTATOUT_VALUE;
 
 		// Les points sont trop eleves (accepte le bonus de match)
		if (v.getId() == R.id.btn_score &&  point > max && point != max + BONUS) {
 			Toast.makeText(getApplicationContext(),
 					getString(R.string.error_high_value), Toast.LENGTH_LONG)
 					.show();
 			return;
 		}
 
 		// Plus d'erreur,... on se charge des saisies
 		input_score1.setText("");
 		input_score2.setText("");
 
 		// Si les points sont negatifs, suppression de points a l'equipe
 		// sans prendre en compte les coefficients
 		if (point < 0) {
 			addAnnounce((team1 ? 1 : 2), point);
 			return;
 		}
 
 		/* Ajoute les points comme une annonce */
 		if (v.getId() == R.id.btn_announcement) {
 			addAnnounce((team1 ? 1 : 2), point * coefficient);
 			return;
 		}
 		/* Ajoute les points comme score */
 		addPoints((team1 ? 1 : 2), point, max, coefficient);
 	}
 
 	/**
 	 * Cree le menu contextuel
 	 * 
 	 * @param le menu
 	 * @return true;
 	 * @Override
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	/**
 	 * Appelee lorsqu'on change le type de jeu. Modifie le coefficient des
 	 * points et mets a jour la legende
 	 * 
 	 * @Override
 	 * @param ratinBar
 	 *            La barre de rating
 	 * @param rating
 	 *            La valeur de la barre de rating
 	 * @param fromUser
 	 *            Indique si la mise a jour est faite par l'utilisateur
 	 */
 	public void onRatingChanged(RatingBar ratingBar, float rating,
 			boolean fromUser) {
 		TextView txt_legend = (TextView) findViewById(R.id.multiplyLegend);
 		String[] legends = getResources().getStringArray(R.array.type_array);
 		// Index du coefficient suivant le rating
 		int i = (int) rating - 1;
 		// Force un rating a une etoile minimum
 		if (i < 0) {
 			i = 0;
 			ratingBar.setRating(1);
 		}
 		// Le coefficient se definit suivant le nombre d'etoiles.
 		// Voir dans res/values/integers.xml,
 		coefficient = getResources().getIntArray(R.array.coeff)[i];
 		txt_legend.setText(legends[i] + " x" + coefficient);
 	}
 
 	/**
 	 * Lors de la saisie des score, on appel checkInput().
 	 */
 	class TextWatcherAdapter implements TextWatcher {
 
 		@Override
 		public void afterTextChanged(Editable arg0) {
 			checkInput();
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 		}
 	}
 }
