 /* LICENSE
  * This work is licensed under the 
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
  * To view a copy of this license, visit 
  * http://creativecommons.org/licenses/by-nc-sa/3.0/.
  * 
  * Copyright (c) 2013 by Laurent Constantin <constantin.laurent@gmail.com>
  */
 package ch.laurent.scoreManager;
 
 
 import java.util.Observable;
 import java.util.Observer;
 
 
 import android.content.Context;
 import android.view.View;
 import ch.laurent.chibre.R;
 import ch.laurent.helpers.TeamNameHelper;
 import ch.laurent.scoreManager.ScoreStack.Score;
 
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.GraphViewSeries.*;
 
 import com.jjoe64.graphview.LineGraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 
 /**
  * Affiche le score des parties dans un graphique Le score est represente par
  * ScoreStack
  * 
  * @author Laurent Constantin
  * @see https://github.com/jjoe64/GraphView/
  */
 public abstract class ScoreGraph implements Observer {
 	// Le graphique
 	final GraphView mGraphView;
 	// La pile de score
 	ScoreStack mScoreStack;
 	// Le nombre de series presents dans le graphe (2 ou 0)
 	int mNbSeries = 0;
 
 	// Couleur des equipes
 	final int mTeam1Color;
 	final int mTeam2Color;
 
 	final String[] mTeamName;
 
 	/**
 	 * Instancie le graphe
 	 * 
 	 * @param main L'activite Main
 	 * @param scoreStack Le score
 	 */
	public ScoreGraph(Context mContext, ScoreStack scoreStack) {
		mGraphView = new LineGraphView(mContext, "");
		mTeamName = new String[] { TeamNameHelper.getTeamName(mContext, 1),
				TeamNameHelper.getTeamName(mContext, 2) };
 
		mTeam1Color = mContext.getResources().getColor(R.color.red);
		mTeam2Color = mContext.getResources().getColor(R.color.blue);
 
 		setScoreStack(scoreStack);
 
 	}
 
 	/**
 	 * Change le ScoreStack du graph
 	 * 
 	 * @param scoreStack
 	 */
 	public void setScoreStack(ScoreStack scoreStack) {
 		// Supprime l'observateur
 		if (this.mScoreStack != null) {
 			this.mScoreStack.deleteObserver(this);
 		}
 		// Ajout du nouvel observateur
 		this.mScoreStack = scoreStack;
 		this.mScoreStack.addObserver(this);
 	}
 
 	/**
 	 * Genere le graph
 	 */
 	private void generate() {
 		if (mScoreStack == null)
 			return;
 		
 		// Efface les series precedentes
 		while (mNbSeries > 0) {
 			mGraphView.removeSeries(0);
 			mNbSeries--;
 		}
 		// Boucle pour afficher les traits de chaque equipe
 		int topScore = 0;
 		
 		// Constuit la serie de score pour chaque equipe
 		for (int team = 1; team < 3; team++) {
 			final int mColor = (team == 1 ? mTeam1Color : mTeam2Color);
 			
 			// Tableau des scores
 			GraphViewData gfd[] = new GraphViewData[mScoreStack.getStack()
 					.size()];
 			// Tableau des legendes (vide)
 			String[] gfdString = new String[gfd.length];
 			
 			// On boucle sur la pile des scores
 			int i = 0;
 			for (Score s : mScoreStack.getStack()) {
 				final int score = (team == 1 ? s.score1 : s.score2);
 				
 				// Sauve le score max pour la legende verticale
 				topScore = Math.max(topScore,score);
 				
 				// Insertion des points dans le graph
 				gfd[i] = new GraphViewData(i + 1, score);
 				
 				// Legende horizontales vides
 				gfdString[i] = "";
 				i++;
 			}
 			// Applique les legendes
 			mGraphView.setVerticalLabels(new String[] { topScore + "", "0" });
 			mGraphView.setHorizontalLabels(gfdString);
 
 			// Ajoute les points dans le graph pour l'equipe courante
 			mGraphView.addSeries(new GraphViewSeries(mTeamName[team - 1],
 					new GraphViewStyle(mColor, 3), gfd));
 			// Il y a une serie de plus dans le graphe
 			mNbSeries++;
 		}
 	}
 
 	/**
 	 * Calcule et renvoie la vue du graph
 	 * 
 	 * @return Le graphe
 	 */
 	public View getView() {
 		generate();
 		return mGraphView;
 	}
 
 	/**
 	 * Appele lorsque les scores changent
 	 */
 	public abstract void update(Observable observable, Object data);
 
 }
