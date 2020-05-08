 package com.ecnmelog.model;
 
 import java.sql.*;
 
 /**
  * Classe de gestion des containers dans la zone d'attente
  * @author Clément Delafargue
  * */
public class Attente implements Entrepot{
 	
 	/**
 	 * Initialisation de l'espace d'attente
 	 * */
 	public Attente(){
 		
 	}
 	
 	/**
 	 * Ajoute un container à la zone d'attente
 	 * @param container Le container à ajouter à la zone d'attente
 	 * @throws ContainerException si le container est déjà dans la base de données
 	 */
 	public void addContainer(Container container) throws ContainerException{
 		Connection conn = DbConn.getInstance();
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("INSERT INTO container (container_id, type_id) VALUES (?, ?);");
 			stat.setInt(1, container.getId());
 			stat.setInt(2, container.getType());
 			
 			stat.execute();
 		}
 		catch(SQLException e){
 			throw new ContainerException("Le conteneur est déjà répertorié !");
 		}
 	}
 	
 	
 	/*
 	 * -----------------------------------------------------------------
 	 * 
 	 * Implémentation de l'interface Entrepot
 	 * 
 	 * -----------------------------------------------------------------
 	 */
 	
 	
 	/**
 	 * Renvoie le nombre de containers en attente
 	 * @return Le nombre de containers (types confondus) en attente
 	 */
 	public int countContainers(){
 		Connection conn = DbConn.getInstance();
 		int enStock = 0;
 		
 		try{
 			Statement stat = conn.createStatement();
 			ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS stock FROM container WHERE emplacement_id ISNULL;");
 			if(rs.next()) {
 				enStock = rs.getInt("stock");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			// TODO THROW EXCEPTION
 			System.out.println(e.getMessage());
 		}
 		
 		return enStock;
 	}
 	
 	
 	/**
 	 * Renvoie le nombre de containers d'un type donné en attente
 	 * @param type Le type de containers à compter
 	 * @return Le nombre de containers du type donné en attente
 	 */
 	public int countContainers(int type){
 		Connection conn = DbConn.getInstance();
 		int enStock = 0;
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("SELECT COUNT(*) AS stock FROM container WHERE emplacement_id ISNULL AND type_id= ?;");
 			stat.setInt(1, type);
 			ResultSet rs = stat.executeQuery();
 			if(rs.next()) {
 				enStock = rs.getInt("stock");
 			}
 			rs.close();
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			// TODO THROW EXCEPTION
 			System.out.println(e.getMessage());
 		}
 		
 		return enStock;
 	}
 	
 	
 	/**
 	 * Enlève le container demandé de la zone d'attente
 	 * @param id L'id du container à enlever
 	 * */
 	public void removeContainerById(int id){
 		Connection conn = DbConn.getInstance();
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("DELETE FROM container WHERE container_id=? AND emplacement_id ISNULL;");
 			stat.setInt(1, id);
 			stat.execute();
 		}
 		catch(SQLException e){
 			System.out.println("Erreur SQL");
 		}
 	}
 	
 	/**
 	 * Enlève un container du type demandé de la zone d'attente
 	 * @param type Le type de container à enlever
 	 * */
 	public void removeContainerByType(int type){
 		Connection conn = DbConn.getInstance();
 		Integer id = null;
 		
 		try{
 			// On repère le container à enlever (du type qu'on veut)
 			PreparedStatement toDelete = conn.prepareStatement("SELECT container_id FROM container WHERE type_id=? AND emplacement_id ISNULL LIMIT 1;");
 			toDelete.setInt(1, type);
 			ResultSet rs = toDelete.executeQuery();
 			if(rs.next()) {
 				id = rs.getInt("container_id");
 			}
 			rs.close();
 			
 			if(id != null){
 				PreparedStatement delete = conn.prepareStatement("DELETE FROM container WHERE container_id=?;");
 				delete.setInt(1, id);
 				delete.executeUpdate();
 			}
 		}
 		catch(SQLException e){
 			//e.printStackTrace();
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Enlève tous les containers du type demandé de la zone d'attente
 	 * @param type Le type de containers à enlever
 	 * */
 	public void removeContainersByType(int type){
 		Connection conn = DbConn.getInstance();
 		
 		try{
 			PreparedStatement stat = conn.prepareStatement("DELETE FROM container WHERE type_id=? AND emplacement_id ISNULL;");
 			stat.setInt(1, type);
 			stat.execute();
 		}
 		catch(SQLException e){
 			System.out.println("Erreur SQL");
 		}
 	}
 	
 	/** Vide la zone d'attente */
 	public void empty(){
 		Connection conn = DbConn.getInstance();
 		try{
 			Statement stat = conn.createStatement();
 			stat.executeUpdate("DELETE FROM container WHERE emplacement_id ISNULL;");
 		}
 		catch(SQLException e){
 			System.out.println("Erreur SQL");
 		}
 	}
 	
 }
