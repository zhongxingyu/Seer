 package fr.jules_cesar.Shop;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 @SuppressWarnings("unused")
 public class Shop {
 	
 	public static String shop_message = ChatColor.GOLD + "[SHOP] ";
 	
 	public static void vendre(int quantite, Player joueur, int id, byte data){
 		ItemStack vente_un = new ItemStack(id, 0 , data); vente_un.setAmount(quantite - 1);
 		ItemStack vente_deux = new ItemStack(id, 1, data);
 		HashMap<Integer, ItemStack> restant = joueur.getInventory().removeItem(vente_un, vente_deux);
 		boolean manquant = false;
 		int quantite_manquante = 0;
 		if(!restant.isEmpty()){
 			if(restant.get(0) != null && restant.get(0).getTypeId() == id) {quantite_manquante += restant.get(0).getAmount(); System.out.println("STACK 1 : " + restant.get(0).getAmount());}
 			if(restant.get(1) != null && restant.get(1).getTypeId() == id) {quantite_manquante += restant.get(1).getAmount(); System.out.println("STACK 2 : " + restant.get(1).getAmount());}
 			System.out.println(quantite_manquante);
 			if(quantite_manquante != 0)
 				manquant = true;
 		}
 		if(manquant){
 			joueur.sendMessage(shop_message + "Vous n'avez pas " + quantite + " de " + vente_un.getType().toString()+":"+data);
 			if(quantite - quantite_manquante != 0) {ItemStack retour = vente_un.clone(); retour.setAmount(quantite - quantite_manquante); joueur.getInventory().addItem(retour);}
 		}
 		else{
 			double prix = 0;
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 				 
 				Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 			      
 			      Statement state = conn.createStatement();
			      //L'objet ResultSet contient le rsultat de la requte SQL
 			      ResultSet result = state.executeQuery("SELECT prix FROM shop WHERE id = " + id + " AND data = " + data);
 			      result.next();
 			      prix = Double.parseDouble(result.getObject(1).toString()); result.close();
 			      state.executeUpdate("UPDATE shop SET stock = stock + " + quantite + ", difference = difference - "+quantite+" WHERE id = " + id + " AND data = " + data);
 			      state.close();
 			      conn.close();
 			}
 			catch (ClassNotFoundException e) { e.printStackTrace(); }
 			catch (SQLException e) { e.printStackTrace(); }
 			
 			if(prix != 0){
 				double gain = prix * quantite;
 				Vault.economy.depositPlayer(joueur.getName(), gain);
				joueur.sendMessage(shop_message + "Vous avez vendu " + quantite + " de " + vente_un.getType().toString()+":"+ data + " pour " + ShopCommand.formatDouble(gain, 2) + " EIG");
 			}
 			else{
 				joueur.sendMessage(shop_message + ChatColor.RED + "Erreur du plugin, contactez un administrateur.");
 				joueur.getInventory().addItem(vente_un, vente_deux);
 			}
 		}	
 	}
 	
 	public static void acheter(int quantite, Player joueur, int id, byte data){
 		int stock = 0;
 		double prix = 0;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			 
 			Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 		      
 		      Statement state = conn.createStatement();
		      //L'objet ResultSet contient le rsultat de la requte SQL
 		      ResultSet result = state.executeQuery("SELECT stock FROM shop WHERE id = " + id + " AND data = " + data);
 		      result.next();
 		      stock = Integer.parseInt(result.getObject(1).toString()); result.close();
 		      result = state.executeQuery("SELECT prix FROM shop WHERE id = " + id + " AND data = " + data);
 		      result.next();
 		      prix = Double.parseDouble(result.getObject(1).toString()); result.close();
 		      state.close();
 		      conn.close();
 		}
 		catch (ClassNotFoundException e) { e.printStackTrace(); }
 		catch (SQLException e) { e.printStackTrace(); }
 		
 		if(prix != 0){
 			if(stock >= quantite){
 				double cout = prix * quantite;
 				if(Vault.economy.has(joueur.getName(), cout)){
 				Vault.economy.withdrawPlayer(joueur.getName(), cout);
 				ItemStack achat = new ItemStack(id, quantite, data);
 				joueur.getInventory().addItem(achat);
				joueur.sendMessage(shop_message + "Vous avez achet " + quantite + " de " + achat.getType().toString() + ":"+ data + " pour " + ShopCommand.formatDouble(cout, 2) + " EIG");
 				try {
 					Class.forName("com.mysql.jdbc.Driver");
 					 
 					Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 				      
 				      Statement state = conn.createStatement();
 				      state.executeUpdate("UPDATE shop SET stock = stock - " + quantite + ", difference = difference + " + quantite + " WHERE id = " + id + " AND data = " + data);
 				      state.close(); 
 				      conn.close();
 				}
 				catch (ClassNotFoundException e) { e.printStackTrace(); }
 				catch (SQLException e) { e.printStackTrace(); }
 				}
 				else{
					joueur.sendMessage(shop_message + ChatColor.RED + "Vous n'avez pas l'argent ncessaire pour acheter cela");
 				}
 			}
 			else{
 				int quantite_restante = quantite-stock;
 				double cout = prix * stock + prix * quantite_restante * 1.1;
 				if(Vault.economy.has(joueur.getName(), cout)){
 					stock = 0;
 					Vault.economy.withdrawPlayer(joueur.getName(), cout);
 					ItemStack achat = new ItemStack(id, quantite, data);
 					joueur.getInventory().addItem(achat);
					joueur.sendMessage(shop_message + "Vous avez achet " + quantite + " de " + achat.getType().toString() + ":" + data + " pour " + ShopCommand.formatDouble(cout, 2) + " EIG");
 					try {
 						Class.forName("com.mysql.jdbc.Driver");
 						 
 						Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 					      
 					      Statement state = conn.createStatement();
 					      state.executeUpdate("UPDATE shop SET stock = 0, difference = difference + " + quantite + " WHERE id = " + id + " AND data = " + data);
 					      state.close();
 					      conn.close();
 					}
 					catch (ClassNotFoundException e) { e.printStackTrace(); }
 					catch (SQLException e) { e.printStackTrace(); }
 				}
 				else{
					joueur.sendMessage(shop_message + ChatColor.RED + "Vous n'avez pas l'argent ncessaire pour acheter cela");
 				}
 			}
 		}
 		else{
 			joueur.sendMessage(shop_message + ChatColor.RED + "Erreur du plugin, contactez un administrateur");
 		}
 	}
 }
