 package com.polytech.propps.models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import com.polytech.propps.bdd.Base;
 
 public class Recruteur extends Utilisateur {
 	
 	private final static String colID = "ID_Utilisateur";
 
 	
 	protected Societe societe;
 
 	public Recruteur(int ID) {
 		super(ID);
 		societe = null;
 	}
 	
 	public Recruteur(String sNom, String sPrenom, String sEmail, String sPassword, Societe societe) {
 		super(sNom, sPrenom, sEmail, sPassword);
 		this.societe = societe;
 	}
 
 	@Override
 	public void insertOrUpdate() {
 		Base b = new Base();
 		try {
 			super.insertOrUpdate();
 			b.connect();
 			b.procedureInit("Recruteur_insertOrUpdate", 2);
 			b.setParamInt("_" + colID, super.ID_Utilisateur);
 			b.setParamInt("_" + Societe.colID, societe.getID());
			b.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			b.close();
 		}
 	}
 
 	@Override
 	public void delete() {
 		Base b = new Base();
 		try {
 			b.connect();
 			b.procedureInit("Recruteur_delete", 1);
 			b.setParamInt("_" + colID, super.ID_Utilisateur);
 			super.delete();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			b.close();
 		}
 		
 	} 
 
 	public Societe getSociete() {
 		if(!super.bFill) {
 			fill();
 		}
 		return societe;
 	}
 
 	public void setSociete(Societe newSociete) {
 		this.societe = newSociete;
 		super.bFill = true;
 	}
 
 
 	@Override
 	public void fill() {
 		if(!super.bFill) {
 			Base b = new Base();
 			try {
 				b.connect();
 				b.procedureInit("Recruteur_getByID", 1);
 				b.setParamInt("_" + colID, super.ID_Utilisateur);
 				ResultSet result = b.executeQuery();
 				if(result.next()) {
 					super.sNom = result.getString(colNom);
 					super.sPrenom = result.getString(colPrenom);
 					super.sEmail = result.getString(colEmail);
 					super.sPassword = result.getString(colPassword);
 					super.adresse.setAdresse(result.getString(Adresse.colAdresse));
 					super.adresse.setVille(result.getString(Adresse.colVille));
 					super.adresse.setCodePostal(result.getString(Adresse.colCP));
 					super.adresse.setPays(result.getString(Adresse.colPays));
 					
 					societe = new Societe(result.getInt(Societe.colID));
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}finally {
 				b.close();
 			}
 			bFill = true;
 		}
 		
 	}
 
 }
