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
 
 package functions.agap;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collections;
 import java.util.List;
 
 import models.Eleve;
 import models.Seance;
 import functions.AGAPUtil;
 import functions.Events;
 
 /**
  * Des fonctions utiles pour gérer les données qui sortent d'AGAP
  * @author Admin
  *
  */
 public class AGAPStringUtil {
 
 	/**
 	 * Obtient l'uid à partir de personne_dn (inutile pour l'instant)
 	 * @param dn : coordonnées dans LDAP de la personne
 	 * @return uid
 	 */
 	public static String getUID(String dn){
 		return dn.substring(4, dn.indexOf(','));
 	}
 
 	/**
 	 * Renvoie le semestre d'une certaine matière sous la forme d'une string : S#.
 	 * @param actionformation_id : id de la matière dans AGAP
 	 * @param connection : Connection à la base de donnée
 	 * @return String : S5, S6 ou S7, etc.
 	 */
 	public static String getSemestre(Integer actionformation_id,Connection connection, String actionformation_libellecourt){
 		String semestre = "";
 		String theQuery = "SELECT structures_id,typestructure_id "
 				+ "FROM actionstructure "
 				+ "NATURAL JOIN structures "
 				+ "WHERE actionformation_id="+actionformation_id;
 		try {
 			System.out.println("Executing : " + theQuery);
 			Statement theStmt = connection.createStatement();
 			ResultSet theRS1 = theStmt.executeQuery(theQuery);
 			while (theRS1.next()) {
 				Integer typestructure_id = theRS1.getInt("typestructure_id");
 				Integer structure_id = theRS1.getInt("structures_id");
 				int i=0;
 				while(typestructure_id!=16 && typestructure_id!=1 && typestructure_id!=10){
 					theQuery = "SELECT structures_id,typestructure_id "
 							+ "FROM structuresliens "
 							+ "INNER JOIN structures ON(structures.structures_id=structuresliens.structures_pere_id) "
 							+ "WHERE structures_fils_id="+structure_id;
 					theStmt = connection.createStatement();
 					ResultSet theRS = theStmt.executeQuery(theQuery);
 					while(theRS.next()){
 						System.out.println("NEXT:" + structure_id+" "+typestructure_id+" : "+i);
 						typestructure_id=theRS.getInt("typestructure_id");
 						structure_id = theRS.getInt("structures_id");
 					}
 					i++;
 					System.out.println(structure_id+" "+typestructure_id+" : "+i);
 				}
 				theQuery = "SELECT structures_libelle,structures_libellecourt "
 						+ "FROM structures "
 						+ "WHERE structures_id="+structure_id;
 				theStmt = connection.createStatement();
 				ResultSet theRS = theStmt.executeQuery(theQuery);
 				while(theRS.next()){
 					String sem = theRS.getString("structures_libelle");
 					semestre = toLibelleSemestreCourt(sem,actionformation_libellecourt);
 					System.out.println(semestre);
 				}
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return semestre;
 	}
 	
 	/**
 	 * Renvoie la string du semestre stocké dans AGAP à la forme voulue
 	 * (S5,S6,S7,etc...) si cette chaîne n'est pas déjà dans le libellé court
 	 * de la matière.
 	 * @param sem : semestre stocké dans AGAP sous forme de string
 	 * @param libelle : libelle de la matière
 	 * @return String : S5, S6 ou S7, etc.
 	 */
 	private static String toLibelleSemestreCourt(String sem,String libelle){
 		if(sem.startsWith("Semestre ")){
 			if(libelle.endsWith("_S5")){
 				return "";
 			}else if(libelle.endsWith("_S6")){
 				return "";
 			}else if(libelle.endsWith("_S7")){
 				return "";
 			}else if(libelle.endsWith("_S8")){
 				return "";
 			}else{
 				return "S"+sem.substring(9);
 			}
 		}else{
 			return "";
 		}
 	}
 	
 	/**
 	 * Get students who subscribed to the subject of the seance
 	 * @param seance
 	 * @return
 	 */
 	public static String getStudentList(Seance seance){
 		List<Eleve> eleves = Events.find(seance);
 		Collections.sort(eleves, new Eleve());
 		String r = "<select>";
 		for(Eleve e : eleves){
 			r+="<option>"+e+"</option>";
 		}
 		r+="</select>";
 		return r;
 	}
 	
 	/**
 	 * Get the number of students who are concerned by a seance.
 	 * @param groupe
 	 * @param matiere_id
 	 * @return
 	 */
 	public static String getStudentNumber(String groupe, int matiere_id){
 		List<Eleve> eleves;
 		if(test.Mode.findAllEnabled()){
 			eleves = Eleve.find.all();
 		}else{
			eleves = AGAPUtil.getInscrits(matiere_id);
 		}
 		String r = "";
 		if(eleves.size()<=1){
 			r+="<p>"+eleves.size()+" élève est concerné par ce cours.</p>";
 		}else{
 			r+="<p>"+eleves.size()+" élèves sont concernés par ce cours.</p>";
 		}
 		if(eleves.isEmpty()){
 			r+="<h1>Attention, aucun élève ne suit ce cours !</h1>";
 		}
 		return r;
 	}
 }
