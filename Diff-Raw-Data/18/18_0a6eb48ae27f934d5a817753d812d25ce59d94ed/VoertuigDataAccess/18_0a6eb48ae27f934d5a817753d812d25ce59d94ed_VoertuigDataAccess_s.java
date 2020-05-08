 package leen.meij.dataAccess;
 
 import java.sql.*;
 import java.util.*;
 
 import leen.meij.utilities.*;
 import leen.meij.*;
 
 public class VoertuigDataAccess extends DataAccess
 {
 
         private Voertuig buildVoertuigModel(ResultSet resultSet) throws SQLException
         {
                 Voertuig voertuig = new Voertuig();
                voertuig.setBeschrijving(resultSet.getString("beschrijving"));
                voertuig.setCategorie(resultSet.getString("categorie"));
                voertuig.setKleur(resultSet.getString("kleur"));
                voertuig.setMerk(resultSet.getString("merk"));
                voertuig.setType(resultSet.getString("type"));
                 
                 voertuig.setVoertuigID(resultSet.getInt("id"));
                 
                 return voertuig;
         }
 
         private int fillVoertuigStatement(PreparedStatement ps, Voertuig voertuig)
                         throws SQLException
         {
                 int i = 1;
                 ps.setString(i++,voertuig.getBeschrijving());
                 ps.setString(i++,voertuig.getCategorie());
                 ps.setString(i++,voertuig.getKleur());
                 ps.setString(i++,voertuig.getMerk());
                 ps.setString(i++,voertuig.getType());
                 ps.setBoolean(i++,voertuig.getVerhuurbaar());
                 
                 return i;
         }        
         private Onderhoud buildOnderhoud(ResultSet resultSet) throws SQLException
         {
                 Onderhoud onderhoud = new Onderhoud();
                 onderhoud.setHandeling(resultSet.getString("handeling"));
                 onderhoud.setLocatie(resultSet.getString("locatie"));
                 onderhoud.setOnderhoudID(resultSet.getInt("id"));
                 onderhoud.setVoldaan(resultSet.getBoolean("voldaan"));
                 
                 return onderhoud;
         }
 
         private int fillOnderhoudStatement(PreparedStatement ps, Onderhoud onderhoud)
                         throws SQLException
         {
                 int i = 1;
                 ps.setString(i++, onderhoud.getBeschrijving());
                 ps.setString(i++,onderhoud.getHandeling());
                 ps.setString(i++,onderhoud.getLocatie());
                 ps.setBoolean(i++,onderhoud.isVoldaan());
                 
                 
                 return i;
         }
 
         /**
          * 
          * @param voertuigID
          */
         public Voertuig select(int voertuigID)
         {
                 openConnection();
                 
                 PreparedStatement ps = null;
                 ResultSet resultSet = null;
                 try
                 {
         
                         ps = connection.prepareStatement("SELECT * FROM voertuig WHERE id = ?");
                         
                         ps.setInt(1,voertuigID);
                         resultSet = ps.executeQuery();
                         
                         if(resultSet.next())
                         {
                                 Voertuig voertuig = buildVoertuigModel(resultSet);
                                 voertuig.setOnderhoud(getOnderhoud(voertuigID));
                                 
                                 return voertuig;
                         }
                 } catch (SQLException sqle)
                 {
                         sqle.printStackTrace();
                 } finally {
                         if (resultSet != null) try { resultSet.close(); } catch (SQLException negeer) {}
                         if (ps != null) try { ps.close(); } catch (SQLException negeer) {}
                         closeConnection();
                 }
                 
                 return null;
         }
 
         public ArrayList<Voertuig> selectAll()
         {
                 openConnection();
                 
                 PreparedStatement ps = null;
                 ResultSet resultSet = null;
                 try
                 {
                         ArrayList<Voertuig> voertuigen = new ArrayList<Voertuig>();
                         ps = connection.prepareStatement("SELECT * FROM voertuig");
                         
                         
                         resultSet = ps.executeQuery();
                         
                         while(resultSet.next())
                         {
                                 Voertuig voertuig =  buildVoertuigModel(resultSet);
                                 voertuig.setOnderhoud(getOnderhoud(voertuig.getVoertuigID()));
                                 voertuigen.add(voertuig);
                         }
                         return voertuigen;
                 } catch (SQLException sqle)
                 {
                         sqle.printStackTrace();
                 } finally {
                         if (resultSet != null) try { resultSet.close(); } catch (SQLException negeer) {}
                         if (ps != null) try { ps.close(); } catch (SQLException negeer) {}
                         closeConnection();
                 }
                 
                 return null;
         }
 
         /**
          * 
          * @param voertuig
          */
         public Voertuig add(Voertuig voertuig)
         {
                 openConnection();
                 
                 PreparedStatement ps = null;
                 ResultSet resultSet = null;
                 Voertuig addedVoertuig = null;
                 try
                 {
         
                         ps = connection.prepareStatement(
                                         "INSERT INTO voertuig (categorie,merk,type,kleur,beschrijving,verhuurbaar) "+
                                         "VALUES (?,?,?,?,?,?) RETURNING *");
                         this.fillVoertuigStatement(ps, voertuig);
                         
                         resultSet = ps.executeQuery();
                         
                         if(resultSet.next())
                         {
                                 addedVoertuig = buildVoertuigModel(resultSet);
                                 
                                 for(Onderhoud onderhoud : voertuig.getOnderhoud()){
                                         onderhoud.setVoertuig(addedVoertuig);
                                         addedVoertuig.getOnderhoud().add(addOnderhoud(onderhoud));
                                         
                                 }
                                 return addedVoertuig;
                         }
                 } catch (SQLException sqle)
                 {
                         sqle.printStackTrace();
                 } finally {
                         if (resultSet != null) try { resultSet.close(); } catch (SQLException negeer) {}
                         if (ps != null) try { ps.close(); } catch (SQLException negeer) {}
                         closeConnection();
                 }
                 
                 return addedVoertuig;
         }
 
         /**
          * 
          * @param klantID
          */
         public void delete(int voertuigID)
         {
                 openConnection();
                 
                 PreparedStatement ps = null;
                 ResultSet resultSet = null;
                 Voertuig addedVoertuig = null;
                 try
                 {
         
                         ps = connection.prepareStatement(
                                         "BEGIN TRANSACTION;" +
                                         "DELETE FROM onderhoud WHERE voertuigenid = ?;" +
                                         "DELETE FROM voertuig WHERE id = ?;" +
                                         "COMMIT;");
                         ps.setInt(1,voertuigID);
                         ps.setInt(2,voertuigID);
                         
                         ps.execute();
                 
                 } catch (SQLException sqle)
                 {
                         sqle.printStackTrace();
                 } finally {
                         if (resultSet != null) try { resultSet.close(); } catch (SQLException negeer) {}
                         if (ps != null) try { ps.close(); } catch (SQLException negeer) {}
                         closeConnection();
                 }
                 
         }
 
         /**
          * 
          * @param voertuig
          */
         public Voertuig edit(Voertuig voertuig)
         {
                 openConnection();
                 
                 PreparedStatement ps = null;
                 ResultSet resultSet = null;
                 Voertuig edited = null;
                 try
                 {
         
                         ps = connection.prepareStatement(
                                         "UPDATE voertuig SET categorie=?,merk=?,type=?,kleur=?,beschrijving=?,verhuurbaar=? "+
                                         "WHERE id=? RETURNING *");
                         int index = this.fillVoertuigStatement(ps, voertuig);
                         ps.setInt(index++,voertuig.getVoertuigID());
                         resultSet = ps.executeQuery();
                         
                         if(resultSet.next())
                         {
                                 voertuig = buildVoertuigModel(resultSet);
                                 
                                 
                         }
                 } catch (SQLException sqle)
                 {
                         sqle.printStackTrace();
                 } finally {
                         if (resultSet != null) try { resultSet.close(); } catch (SQLException negeer) {}
                         if (ps != null) try { ps.close(); } catch (SQLException negeer) {}
                         closeConnection();
                 }
                 
                 return voertuig;
         }
 
         /**
          * 
          * @param onderhoud
          */
         public Onderhoud addOnderhoud(Onderhoud onderhoud)
         {
                 openConnection();
                 
                 PreparedStatement ps = null;
                 ResultSet resultSet = null;
                 
                 try
                 {
         
                         ps = connection.prepareStatement(
                                         "INSERT INTO onderhoud (beschrijving,handeling,locatie,voldaan,klantenid,voertuigenid) "+
                                         "VALUES (?,?,?,?,?,?) RETURNING *");
                         int index = this.fillOnderhoudStatement(ps, onderhoud);
                         ps.setInt(index++, onderhoud.getKlantID());
                         ps.setInt(index++,onderhoud.getVoertuig().getVoertuigID());
                         resultSet = ps.executeQuery();
                         
                         if(resultSet.next())
                         {
                                 onderhoud = buildOnderhoud(resultSet);
                                 
                                 
                         }
                 } catch (SQLException sqle)
                 {
                         sqle.printStackTrace();
                 } finally {
                         if (resultSet != null) try { resultSet.close(); } catch (SQLException negeer) {}
                         if (ps != null) try { ps.close(); } catch (SQLException negeer) {}
                         closeConnection();
                 }
                 
                 return onderhoud;
         }
         
         private ArrayList<Onderhoud> getOnderhoud(int voertuigID)
         {
                 openConnection();
                 ArrayList<Onderhoud> onderhouden = new ArrayList<Onderhoud>();
                 PreparedStatement ps = null;
                 ResultSet resultSet = null;
                 
                 try
                 {
         
                         ps = connection.prepareStatement(
                                         "SELECT * FROM onderhoud WHERE voertuigenid=?");
                         
                         ps.setInt(1, voertuigID);
                         
                         resultSet = ps.executeQuery();
                         
                         while(resultSet.next())
                         {
                                 Onderhoud onderhoud = buildOnderhoud(resultSet);
                                 onderhouden.add(onderhoud);
                         }
                 } catch (SQLException sqle)
                 {
                         sqle.printStackTrace();
                 } finally {
                         if (resultSet != null) try { resultSet.close(); } catch (SQLException negeer) {}
                         if (ps != null) try { ps.close(); } catch (SQLException negeer) {}
                         closeConnection();
                 }
                 
                 return onderhouden;
         }
         
         
 
 }
