 package handler;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import main.Araclar;
 
 
 public class AraclarHandler {
 	static final String URL = "jdbc:postgresql://ec2-23-21-129-125.compute-1.amazonaws.com/d70stbr7v9732u?useUnicode=true&characterEncoding=UTF-8";
 	static final String USER = "jfcawhlrrysslo";
 	static final String PASS = "pce7oasKqyHunqIDF_ajTL1_UR";
 	private Connection  conn;
 	private Statement stmt;
 	private ResultSet rs;
 	
 	public void AracEkle(Araclar arac) {
 		
		String sorgu = "INSERT INTO ferahti.arac (`marka`, `model`, `durum`) " +
 				"VALUES ('"+ arac.getMarka() +"', '" + arac.getPlaka() + "', '"+ arac.getDurum() +"')";
 		try {
 			Class.forName("org.postgresql.Driver");
 			conn = DriverManager.getConnection(URL, USER, PASS);
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sorgu);
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 		
 	public void AracGuncelle(String id, String user, String status) {
 		
 		String sorgu = "UPDATE ferahti.arac SET kullanici='" + user + "', durum='" + status + "' WHERE idarac='"+ id + "';";
 		
 		try {
 			Class.forName("org.postgresql.Driver");
 			conn = DriverManager.getConnection(URL, USER, PASS);
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sorgu);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public ArrayList<Araclar> AraclariGetir() {
 		ArrayList<Araclar> araclistesi = new ArrayList<Araclar>();
 		String sorgu = "select * from ferahti.arac";
 		try {
 			Class.forName("org.postgresql.Driver");
 			conn = DriverManager.getConnection(URL, USER, PASS);
 			stmt = conn.createStatement();
 			stmt.executeQuery(sorgu);
 			rs = stmt.executeQuery(sorgu);
 			while (rs.next()) {
 				Araclar a = new Araclar(rs.getInt("idarac"), rs.getString("marka"),rs.getString("kullanici"), rs.getString("plaka"), rs.getString("durum"));
 				araclistesi.add(a);
 			}
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return araclistesi;
 	}
 	public void AracSil(String id) {
 		String sorgu = "DELETE FROM ferahti.arac WHERE idarac='"+ id +"';";
 		try {
 			Class.forName("org.postgresql.Driver");
 			conn = DriverManager.getConnection(URL, USER, PASS);
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sorgu);
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	public ArrayList<Araclar> BosAraclariGetir() {
 		ArrayList<Araclar> araclistesi = new ArrayList<Araclar>();
 		String sorgu = "select * from ferahti.arac";
 		try {
 			Class.forName("org.postgresql.Driver");
 			conn = DriverManager.getConnection(URL, USER, PASS);
 			stmt = conn.createStatement();
 			stmt.executeQuery(sorgu);
 			rs = stmt.executeQuery(sorgu);
 			while (rs.next()) {
 				Araclar a = new Araclar(rs.getInt("idarac"), rs.getString("marka"),rs.getString("kullanici"), rs.getString("plaka"), rs.getString("durum"));
				if (rs.getString("durum").equals("boşta") || rs.getString("durum") == null) {
 					araclistesi.add(a);
 				}
 				
 			}
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return araclistesi;
 	}
 	
 	public ArrayList<Araclar> KullanimdakiAraclariGetir(String user) {
 		ArrayList<Araclar> araclistesi = new ArrayList<Araclar>();
 		String sorgu = "select * from ferahti.arac";
 		try {
 			Class.forName("org.postgresql.Driver");
 			conn = DriverManager.getConnection(URL, USER, PASS);
 			stmt = conn.createStatement();
 			stmt.executeQuery(sorgu);
 			rs = stmt.executeQuery(sorgu);
 			while (rs.next()) {
 				Araclar a = new Araclar(rs.getInt("idarac"), rs.getString("marka"),rs.getString("kullanici"), rs.getString("plaka"), rs.getString("durum"));
 				if (rs.getString("kullanici").equals(user)) {
 					araclistesi.add(a);
 				}
 				
 			}
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return araclistesi;
 	}
 	
 	
 }
