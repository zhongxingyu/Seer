 package plarktmaatsDAO;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import plarktmaatsDomein.Bod;
 import plarktmaatsDomein.Categorie;
 import plarktmaatsDomein.Gebruiker;
 import plarktmaatsDomein.Veiling;
 
 public class VeilingDAOImpl implements PlarktmaatsDAOInterface<Veiling> {
 
 	@Override
 	public void create(Veiling v) {
 		String naam = v.getVeilingNaam();
 		String omschrijving = v.getVeilingOmschrijving();
 		int minbedrag = v.getMinBedrag();
 		Calendar eindtijd = v.getEindTijd();
 		Date einddatum = null;
 		if (eindtijd != null) {
 			einddatum = new java.sql.Date(eindtijd.getTimeInMillis());
 		} else {
 			Calendar morgen = Calendar.getInstance();
 			morgen.add(Calendar.DAY_OF_MONTH, 1);
 			einddatum = new java.sql.Date(morgen.getTimeInMillis());
 		}
 
 		String gebruikersNaam = v.getAanbieder().getGebruikersnaam();
 		String categorieNaam = v.getDeCategorie().getNaam();
 		String foto = v.getFoto();
 		int verwerkt = 0;
 		if(v.getVerwerkt())
 			verwerkt = 1;
 
		String query = "INSERT INTO " + ConnectionData.DATABASE	+ ".\"VEILINGEN\" VALUES (seq_veiling.nextval, '" + naam + "', '" + omschrijving + "', '" + minbedrag + "', To_Date('" + einddatum + "','yyyy-mm-dd'), '" + gebruikersNaam + "', '" + categorieNaam + "', '" + foto + "', "+verwerkt+")";
 		Connection con = connect();
 		try {
 			con.createStatement().execute(query);
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Veiling read(String pk) {
 		Connection con = connect();
 		try {
 			PreparedStatement read = con.prepareStatement("SELECT * FROM "
 					+ ConnectionData.DATABASE + ".\"VEILINGEN\" WHERE ID = ?");
 			read.setString(1, pk);
 			ResultSet rs = read.executeQuery();
 			while (rs.next()) {
 				int id = rs.getInt("ID");
 				String naam = rs.getString("NAAM");
 				String omschrijving = rs.getString("OMSCHRIJVING");
 				int minbedrag = rs.getInt("MINBEDRAG");
 				Date eindtijdTemp = rs.getDate("EINDTIJD");
 				Calendar eindtijd = Calendar.getInstance();
 				eindtijd.setTime(eindtijdTemp);
 				String gebruikersnaam = rs
 						.getString("GEBRUIKERS_GEBRUIKERSNAAM");
 				String categorienaam = rs.getString("CATEGORIEEN_NAAM");
 				boolean verwerkt = false;
 				if(rs.getInt("Verwerkt") == 1)
 					verwerkt = true;
 				String foto = rs.getString("FOTO");
 
 				PersoonDAOImpl dao = new PersoonDAOImpl();
 				Gebruiker aanbieder = (Gebruiker) dao.read(gebruikersnaam);
 				Categorie cat = new Categorie(categorienaam);
 				Veiling veil = new Veiling(id, naam, omschrijving, foto,
 						minbedrag, eindtijd, aanbieder, cat, verwerkt);
 				BodDAOImpl boddao = new BodDAOImpl();
 				ArrayList<Bod> biedingen = boddao.getAllFromVeiling(id);
 				if (!biedingen.isEmpty()) {
 					for (Bod b : biedingen) {
 						veil.voegBodToe(b);
 					}
 				}
 				return veil;
 			}
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public List<Veiling> mijnVeilingen(String gebruikersNaam) {
 		Connection con = connect();
 		List<Veiling> mijnVeilingen = null;
 		try {
 			PreparedStatement read = con.prepareStatement("SELECT * FROM " + ConnectionData.DATABASE + ".\"VEILINGEN\" WHERE GEBRUIKERSNAAM = ?");
 			read.setString(1, gebruikersNaam);
 			ResultSet rs = read.executeQuery();
 			while (rs.next()) {
 				int id = rs.getInt("ID");
 				String naam = rs.getString("NAAM");
 				String omschrijving = rs.getString("OMSCHRIJVING");
 				int minbedrag = rs.getInt("MINBEDRAG");
 				Date eindtijdTemp = rs.getDate("EINDTIJD"); // rs.getDate("GEBDATUM");
 				Calendar eindtijd = Calendar.getInstance();
 				eindtijd.setTime(eindtijdTemp);
 				String gebruikersnaam = rs.getString("GEBRUIKERS_GEBRUIKERSNAAM");
 				String foto = rs.getString("FOTO");
 				String categorienaam = rs.getString("CATEGORIEEN_NAAM");
 				PersoonDAOImpl dao = new PersoonDAOImpl();
 				Gebruiker aanbieder = (Gebruiker) dao.read(gebruikersnaam);
 				Categorie cat = new Categorie(categorienaam);
 				boolean verwerkt = false;
 				if(rs.getInt("Verwerkt") == 1)
 					verwerkt = true;
 				Veiling v = new Veiling(id, naam, omschrijving, foto, minbedrag, eindtijd, aanbieder, cat, verwerkt);
 				mijnVeilingen.add(v);
 			}
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return mijnVeilingen;
 	}
 
 	public List<Bod> mijnBiedingen(String gebruikersNaam) {
 		Connection con = connect();
 		List<Bod> mijnBiedingen = null;
 		try {
 			PreparedStatement read = con.prepareStatement("SELECT * FROM " + ConnectionData.DATABASE + ".\"BIEDINGEN\" WHERE GEBRUIKERSNAAM = ?");
 			read.setString(1, gebruikersNaam);
 			ResultSet rs = read.executeQuery();
 			while (rs.next()) {
 				int id = rs.getInt("ID");
 				int bedrag = rs.getInt("BEDRAG");
 				Date tijdstipTemp = rs.getDate("TIJDSTIP");
 				Calendar tijdstip = Calendar.getInstance();
 				tijdstip.setTime(tijdstipTemp);
 				String veilingId = rs.getString("VEILINGEN_ID");
 				PersoonDAOImpl dao = new PersoonDAOImpl();
 				Gebruiker bieder = (Gebruiker) dao.read(gebruikersNaam);
 				Bod b = new Bod(id, bedrag, tijdstip, bieder, veilingId);
 				mijnBiedingen.add(b);
 			}
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return mijnBiedingen;
 	}
 	
 	public ArrayList<String> readBedragTijd(int pk) {
 		ArrayList<String> returnVal = new ArrayList<String>();
 		
 		Connection con = connect();
 		try {
 			String read = "SELECT minbedrag, eindtijd FROM " + ConnectionData.DATABASE + ".\"VEILINGEN\" WHERE ID = "+pk;
 			ResultSet rs = con.createStatement().executeQuery(read);
 			while (rs.next()) {
 				int bedrag = rs.getInt("MINBEDRAG");				
 
 				BodDAOImpl boddao = new BodDAOImpl();
 				ArrayList<Bod> biedingen = boddao.getAllFromVeiling(pk);
 				for(Bod b : biedingen) {
 					if(b.getBedrag() > bedrag)
 						bedrag = b.getBedrag();
 				}
 				returnVal.add(0, bedrag+"");
 				
 				Date eindtijd = rs.getDate("EINDTIJD");
 				Date nu = (Date)Calendar.getInstance().getTime();
 				
 				long diff = (eindtijd.getTime() - nu.getTime())/1000;
 				
 				returnVal.add(1, diff+"");
 				}
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return returnVal;
 	}
 
 	public ArrayList<Veiling> getAll() {
 		ArrayList<Veiling> array = new ArrayList<Veiling>();
 		Connection con = connect();
 		try {
 			PreparedStatement read = con.prepareStatement("SELECT * FROM "
 					+ ConnectionData.DATABASE + ".\"VEILINGEN\" ORDER BY eindtijd ASC");
 			ResultSet rs = read.executeQuery();
 			while (rs.next()) {
 				int id = rs.getInt("ID");
 				String naam = rs.getString("NAAM");
 				String omschrijving = rs.getString("OMSCHRIJVING");
 				int minbedrag = rs.getInt("MINBEDRAG");
 				Date eindtijdTemp = rs.getDate("EINDTIJD"); // rs.getDate("GEBDATUM");
 				Calendar eindtijd = Calendar.getInstance();
 				eindtijd.setTime(eindtijdTemp);
 				String gebruikersnaam = rs
 						.getString("GEBRUIKERS_GEBRUIKERSNAAM");
 				String categorienaam = rs.getString("CATEGORIEEN_NAAM");
 				String foto = rs.getString("FOTO");
 
 				PersoonDAOImpl persoonDAO = new PersoonDAOImpl();
 				Gebruiker aanbieder = (Gebruiker) persoonDAO.read(gebruikersnaam);
 				Categorie cat = new Categorie(categorienaam);
 				boolean verwerkt = false;
 				if(rs.getInt("Verwerkt") == 1)
 					verwerkt = true;
 				Veiling v = new Veiling(id, naam, omschrijving, foto, minbedrag,
 						eindtijd, aanbieder, cat, verwerkt);
 				array.add(v);
 				BodDAOImpl bodDAO = new BodDAOImpl();
 				ArrayList<Bod> alleBiedingen = bodDAO.getAll();
 				for(Bod b : alleBiedingen) {
 					if(b.getVeilingId().equals(id+""))
 						v.voegBodToe(b);
 				}
 			}
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return array;
 	}
 
 	@Override
 	public void update(String pk, Veiling v) {
 		int id = v.getVeilingId();
 		String naam = v.getVeilingNaam();
 		String omschrijving = v.getVeilingOmschrijving();
 		int minbedrag = v.getMinBedrag();
 		Calendar eindtijd = v.getEindTijd();
 		Date einddatum = null;
 		if (eindtijd != null) {
 			einddatum = new java.sql.Date(eindtijd.getTimeInMillis());
 		} else {
 			Calendar morgen = Calendar.getInstance();
 			morgen.add(Calendar.DAY_OF_MONTH, 1);
 			einddatum = new java.sql.Date(morgen.getTimeInMillis());
 		}
 
 		String gebruikersNaam = v.getAanbieder().getGebruikersnaam();
 		String categorieNaam = v.getDeCategorie().getNaam();
 		String foto = v.getFoto();
 		int verwerkt = 0;
 		if(v.getVerwerkt())
 			verwerkt = 1;
 
 		String query = "UPDATE \"STUD1630460\".\"VEILINGEN\" ";
 		query += "SET id='" + id + "',naam='" + naam + "',omschrijving='"
 				+ omschrijving + "',minbedrag='" + minbedrag
 				+ "',eindtijd= To_Date('" + einddatum
 				+ "','yyyy-mm-dd'),gebruikers_gebruikersnaam='"
 				+ gebruikersNaam + "',categorieen_naam='" + categorieNaam
 				+ "',foto='" + foto + "', verwerkt='"+verwerkt+"' ";
 		query += "WHERE id = '" + pk + "'";
 		Connection con = connect();
 		try {
 			con.createStatement().execute(query);
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void delete(String pk) {
 		Connection con = connect();
 		try {
 			PreparedStatement delete = con.prepareStatement("DELETE FROM "
 					+ ConnectionData.DATABASE + ".\"VEILINGEN\" WHERE id = ?");
 			delete.setString(1, pk);
 			delete.execute();
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Connection connect() { // DONE
 		try {
 			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
 			return DriverManager.getConnection(ConnectionData.HOST,
 					ConnectionData.USERNAME, ConnectionData.PASSWORD);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static void main(String[] args) {
 		// VeilingDAOImpl impl = new VeilingDAOImpl();
 		// Calendar gebdat = Calendar.getInstance();
 		// Gebruiker freak = new Gebruiker("Freak","Freek", "Nederland",
 		// "superloser@superfreak.com", gebdat, "8482929", "super");
 		// Categorie cat = new Categorie("Personen");
 		// Veiling veil = new Veiling(0, "freak", "superfreak original", null,
 		// 5, gebdat, freak, cat);
 		// // impl.create(veil);
 		// // veil = impl.read("freak");
 		// // System.out.println(veil);
 		// // veil = new Veiling("freak", "superfreak non-original", null, 5,
 		// gebdat, freak, cat);
 		// // impl.update("0", veil);
 		// // impl.delete("0");
 		// ArrayList<Veiling> array = impl.getAll();
 		// System.out.println(array.toString());
 	}
 }
