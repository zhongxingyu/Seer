 /****************************************************************************
 
 	ASCMII is a web application developped for the Ecole Centrale de Nantes
 	aiming to organize quizzes during courses or lectures.
     Copyright (C) 2013  Malik Olivier Boussejra
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
 
 ******************************************************************************/
 
 package controllers;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import functions.AGAPUtil;
 import functions.agap.AGAPStringUtil;
 import functions.agap.Matiere;
 import models.Lien;
 import models.Serie;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.matiereListeDynamique;
 
 /**
  * Contient les fonctions qui sont appelées par Ajax pour le rafraîchissement
  * automatique des pages.
  * @author Admin
  *
  */
 public class Ajax extends Controller{
 
 	/**
 	 * Permet la fermeture de la page où l'élève répond à la question via Ajax
 	 * lorsqu'une série est close et que l'élève n'a pas encore répondu. Renvoie
 	 * "0" si la fin de la série n'est pas encore fixée.
 	 * @param serie_id
 	 * @return "0" si la fin de la série n'est pas fixée, autre chose sinon
 	 */
 	public static Result infoHeure(Long serie_id){
 		Serie serie = Serie.find.ref(serie_id);
 		if(serie.date_fermeture!=null){
 			return ok(Long.toString(serie.date_fermeture.getTime()));
 		}else{
 			return ok("0");
 		}
 	}
 	
 	/**
 	 * Permet d'avoir la proportion du nombre de répondant. (Pour rafraîchir la barre
 	 * de chargement du template resultatEnCours avec ajax)
 	 * @param serie_id
 	 * @return un int compris entre 0 et 100 transformé en string valant la proportion
 	 * du nombre de répondants en pourcent.
 	 */
 	
 	public static Result infoNReponses(Long serie_id){
 		Serie serie = Serie.find.ref(serie_id);
 		return ok(String.valueOf(
 					Lien.find.where().eq("repondu", true).eq("serie",serie).findList().size()*100/
 					Lien.find.where().eq("serie",serie).findList().size()
 				));
 	}
 	/**
 	 * Permet de tester si oui où non la série a commencé dans serieNonCommencee.scala.html
 	 * @param chemin
 	 * @return "0" si la série a commencé, "1" sinon
 	 */
 	public static Result hasSerieBegun(String chemin){
 		Lien lien = Lien.find.ref(chemin);
 		if(lien.serie.date_ouverte!=null){
 			return ok("1");
 		}else{
 			return ok("0");
 		}
 	}
 	
 	/**
 	 * Renomme une série.
 	 * @param serie_id
 	 */
 	public static Result renameSerie(Long serie_id,String newName){
 		Serie serie = Serie.find.ref(serie_id);
 		serie.nom=newName;
 		serie.save();
 		return ok("The serie "+serie_id+" has been renamed successfully to "+newName);
 	}
 	
 	/**
 	 * Renvoie la liste des matières qui contiennent le chaîne boutDeMatiere
 	 * @param boutDeMatiere
 	 * @return
 	 */
 	public static Result listeMatieres(String boutDeMatiere){
 		List<String> listeMatieres = new ArrayList<String>();
 		for(Matiere mat : AGAPUtil.listMatieres){
 			if((mat.libellecourt+mat.semestre).toUpperCase().contains(boutDeMatiere.toUpperCase())){
 				listeMatieres.add(mat.libellecourt+mat.semestre);
 			}
 		}
 		Collections.sort(listeMatieres);
 		return ok(matiereListeDynamique.render(listeMatieres));
 	}
 	
 	/**
 	 * Affiche le nombre d'étudiant assistant à un cours.
 	 * @param inputMatiere
 	 * @param inputGroupe
 	 * @return
 	 */
 	public static Result listeEtudiants(String inputMatiere, String inputGroupe){
 		System.out.println("Group:"+inputGroupe+"Matière:"+inputMatiere);
		String res;
		if(inputGroupe.equals("")){
			res = AGAPStringUtil.getStudentNumber(null, Matiere.getID(inputMatiere));
		}else{
			res = AGAPStringUtil.getStudentNumber(inputGroupe, Matiere.getID(inputMatiere));
		}
 		return ok(res);
 	}
 }
