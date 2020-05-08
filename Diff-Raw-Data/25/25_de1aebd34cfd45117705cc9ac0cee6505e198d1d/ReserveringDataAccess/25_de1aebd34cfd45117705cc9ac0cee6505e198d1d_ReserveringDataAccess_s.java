 package leen.meij.dataAccess;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import leen.meij.Factuur;
 import leen.meij.Reservering;
 import leen.meij.utilities.DataAccess;
 
 public class ReserveringDataAccess extends DataAccess
 {
 
 	private KlantDataAccess klantDataAccess = new KlantDataAccess();
 	private VoertuigDataAccess voertuigDataAccess = new VoertuigDataAccess();
 	
 	private PreparedStatement preparedStatement = null;
 	private ResultSet resultSet = null;
 	
 	private Reservering buildReserveringModel() throws SQLException
 	{
 		Reservering reservering = new Reservering();
 		
 		if (heeftKolom(resultSet, "id"))
 		{
 			reservering.setReserveringID(resultSet.getInt("id"));
 		} else
 		{
 			reservering.setReserveringID(resultSet.getInt("reservering_id"));
 		}
 
 		if (heeftKolom(resultSet, "klant_id"))
 		{
 			reservering.setKlant(klantDataAccess.buildModel(resultSet));
 		} else
 		{
 			reservering.setKlant(null);
 		}
 
 		if (heeftKolom(resultSet, "voertuig_id"))
 		{
 			reservering.setVoertuig(voertuigDataAccess.buildVoertuigModel(resultSet));
 		} else
 		{
 			reservering.setVoertuig(null);
 		}
 
 		reservering.setKlantID(resultSet.getInt("klantenid"));
 		reservering.setVoertuigID(resultSet.getInt("voertuigenid"));
 		reservering.setReserveerDatum(resultSet.getDate("reserveerdatum"));
 		reservering.setBeginDatum(resultSet.getDate("begindatum"));
 		reservering.setEindDatum(resultSet.getDate("einddatum"));
 
 		return reservering;
 	}
 	
 	private int fillStatement(Reservering reservering) throws SQLException
 	{
 		int i = 1;
 
 		preparedStatement.setInt(i++, reservering.getKlant().getKlantID());
 		preparedStatement.setInt(i++, reservering.getVoertuig().getVoertuigID());
 		preparedStatement.setDate(i++, new java.sql.Date(reservering.getReserveerDatum().getTime()));
 		preparedStatement.setDate(i++,  new java.sql.Date(reservering.getBeginDatum().getTime()));
 		preparedStatement.setDate(i++, new java.sql.Date(reservering.getEindDatum().getTime()));
 		preparedStatement.setInt(i++, reservering.getKilometer());
 		preparedStatement.setDouble(i++, reservering.getBedrag());
 		preparedStatement.setString(i++, reservering.getStatus());
 
 		return i;
 	}
 	
 	
 	private Factuur buildFactuurModel() throws SQLException
 	{
 		Factuur factuur = new Factuur();
 		
 		factuur.setReserveringID(resultSet.getInt("id"));
 		factuur.setFactuurID(resultSet.getInt("id"));
 		factuur.setFactuurnummer(resultSet.getInt("factuurnummer"));
 		factuur.setDatum(resultSet.getString("datum"));
 		factuur.setReden(resultSet.getString("reden"));
 		
 		return factuur;
 		
 	}
 
 	
 	
 	/**
 	 * 
 	 * @param reserveringID
 	 */
 	public Reservering select(int reserveringID)
 	{
 		openConnection();
 		
 		Reservering reservering;
 
 		StringBuilder builder = new StringBuilder("SELECT ");
 		builder.append("R.id AS reservering_id,");
 		builder.append("klantenid,");
 		builder.append("voertuigenid,");
 		builder.append("reserveerdatum,");
 		builder.append("begindatum,");
 		builder.append("einddatum,");
 		builder.append("achternaam,");
 		builder.append("bedrijfsnaam,");
 		builder.append("emailadres,");
 		builder.append("geboortedatum,");
 		builder.append("huisnummer,");
 		builder.append("K.id AS klant_id,");
 		builder.append("klantnummer,");
 		builder.append("kopiePaspoort,");
 		builder.append("kopierijbewijs,");
 		builder.append("kvknummer,");
 		builder.append("land,");
 		builder.append("mobielnummer,");
 		builder.append("postcode,");
 		builder.append("provincie,");
 		builder.append("straat,");
 		builder.append("telefoonnummer,");
 		builder.append("tussenvoegsel,");
 		builder.append("voornaam,");
 		builder.append("woonplaats, ");
 		builder.append("V.id AS voertuig_id, ");
 		builder.append("categorie,");
 		builder.append("merk,");
 		builder.append("type,");
 		builder.append("kleur,");
 		builder.append("beschrijving,");
		builder.append("verhuurbaar ");
 		builder.append("FROM reservering AS R,");
 		builder.append("klant AS K, ");
 		builder.append("voertuig AS V ");
 		builder.append("WHERE klantenid = K.id ");
 		builder.append("AND voertuigenid = V.id ");
 		builder.append("AND R.id = ? ");
 		builder.append("ORDER BY R.id");
 
 
 		try
 		{
 
 			preparedStatement = connection.prepareStatement(builder.toString());
 
 			preparedStatement.setInt(1, reserveringID);
 			resultSet = preparedStatement.executeQuery();
 
 			if (resultSet.next())
 			{
 				reservering = buildReserveringModel();
 				
 				return reservering;
 			}
 		}
 		catch (SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null) try
 			{
 				resultSet.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			if (preparedStatement != null) try
 			{
 				preparedStatement.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			closeConnection();
 		}
 
 		return null;
 	}
 	
 	
 	//temporary Factuur select
 	public Factuur selectFactuur(int reserveringID)
 	{
 		openConnection();
 		
 		Factuur factuur;
 
 		try
 		{
 
 			preparedStatement = connection.prepareStatement("SELECT * FROM factuur WHERE id = ?");
 
 			preparedStatement.setInt(1, reserveringID);
 			resultSet = preparedStatement.executeQuery();
 
 			if (resultSet.next())
 			{
 				factuur = buildFactuurModel();
 				
 				return factuur;
 			}
 		}
 		catch (SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null) try
 			{
 				resultSet.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			if (preparedStatement != null) try
 			{
 				preparedStatement.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			closeConnection();
 		}
 
 		return null;
 	}
 	
 
 	public ArrayList<Reservering> selectAll()
 	{
 		ArrayList<Reservering> reservering = new ArrayList<Reservering>();
 		openConnection();
 
 		StringBuilder builder = new StringBuilder("SELECT ");
 		builder.append("R.id AS reservering_id,");
 		builder.append("klantenid,");
 		builder.append("voertuigenid,");
 		builder.append("reserveerdatum,");
 		builder.append("begindatum,");
 		builder.append("einddatum,");
 		builder.append("achternaam,");
 		builder.append("bedrijfsnaam,");
 		builder.append("emailadres,");
 		builder.append("geboortedatum,");
 		builder.append("huisnummer,");
 		builder.append("K.id AS klant_id,");
 		builder.append("klantnummer,");
 		builder.append("kopiePaspoort,");
 		builder.append("kopierijbewijs,");
 		builder.append("kvknummer,");
 		builder.append("land,");
 		builder.append("mobielnummer,");
 		builder.append("postcode,");
 		builder.append("provincie,");
 		builder.append("straat,");
 		builder.append("telefoonnummer,");
 		builder.append("tussenvoegsel,");
 		builder.append("voornaam,");
 		builder.append("woonplaats, ");
 		builder.append("V.id AS voertuig_id, ");
 		builder.append("categorie,");
 		builder.append("merk,");
 		builder.append("type,");
 		builder.append("kleur,");
 		builder.append("beschrijving,");
		builder.append("verhuurbaar ");
 		builder.append("FROM reservering AS R,");
 		builder.append("klant AS K, ");
 		builder.append("voertuig AS V ");
 		builder.append("WHERE klantenid = K.id ");
 		builder.append("AND voertuigenid = V.id ");
 		builder.append("ORDER BY R.id");
 
 		try
 		{
 			preparedStatement = connection.prepareStatement(builder.toString());
 
 			resultSet = preparedStatement.executeQuery();
 
 			while (resultSet.next())
 			{
 				reservering.add(buildReserveringModel());
 			}
 			return reservering;
 			
 		}
 		catch (SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null) try
 			{
 				resultSet.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			if (preparedStatement != null) try
 			{
 				preparedStatement.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			closeConnection();
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param reservering
 	 */
 	public Reservering add(Reservering reservering)
 	{
 
 		openConnection();
 		
 		Reservering tempReservering;
 
 		StringBuilder builder = new StringBuilder("INSERT INTO reservering (");
 
 		builder.append("klantenid,");
 		builder.append("voertuigenid,");
 		builder.append("reserveerdatum,");
 		builder.append("begindatum,");
 		builder.append("einddatum,");
 		builder.append("kilometer,");
 		builder.append("bedrag,");
 		builder.append("status) ");
 		builder.append("VALUES (?,?,?,?,?,?,?,?)");
 		builder.append("RETURNING *");
 
 		try
 		{
 			preparedStatement = connection.prepareStatement(builder.toString());
 																																				
 			fillStatement(reservering);
 			resultSet = preparedStatement.executeQuery();
 
 			if (resultSet.next())
 			{
 				tempReservering = buildReserveringModel();
 				if (tempReservering.getKlant() == null && tempReservering.getVoertuig() == null)
 				{
 					tempReservering.setKlant(reservering.getKlant());
 					tempReservering.setVoertuig(reservering.getVoertuig());
 				} else if (tempReservering.getKlant() == null ^ tempReservering.getVoertuig() == null)
 				{
 					System.out.println("Het gaat anders dan je dacht Daan");
 				}
 				reservering = tempReservering;
 			}
 		}
 		catch (SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null) try
 			{
 				resultSet.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			if (preparedStatement != null) try
 			{
 				preparedStatement.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			closeConnection();
 		}
 		return reservering;
 
 	
 	}
 
 	/**
 	 * 
 	 * @param klantID
 	 */
 	public void delete(int reserveringID)
 	{
 		openConnection();
 		
 		try
 		{
 
 			preparedStatement = connection.prepareStatement("DELETE FROM reservering WHERE id = ?");
 
 			preparedStatement.setInt(1, reserveringID);
 			preparedStatement.execute();
 		}
 		catch (SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null) try
 			{
 				resultSet.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			if (preparedStatement != null) try
 			{
 				preparedStatement.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			closeConnection();
 		}
 	}
 
 
 	public Reservering edit(Reservering reservering)
 	{
 		openConnection();
 
 		Reservering tempReservering;
 		
 		StringBuilder builder = new StringBuilder("UPDATE reservering SET ");
 
 		builder.append("klantenid=?,");
 		builder.append("voertuigenid=?,");
 		builder.append("reserveerdatum=?,");
 		builder.append("begindatum=?,");
 		builder.append("einddatum=?,");
 		builder.append("kilometer=?,");
 		builder.append("bedrag=?,");
 		builder.append("status=? ");
 		builder.append("WHERE id = ? ");
 		builder.append("RETURNING *");
 
 		try
 		{
 			preparedStatement = connection.prepareStatement(builder.toString());
 
 			int index = this.fillStatement(reservering);
 			preparedStatement.setInt(index++, reservering.getReserveringID());
 			resultSet = preparedStatement.executeQuery();
 			if(resultSet.next())
 			{
 				tempReservering = buildReserveringModel();
 				if (tempReservering.getKlant() == null && tempReservering.getVoertuig() == null)
 				{
 					tempReservering.setKlant(reservering.getKlant());
 					tempReservering.setVoertuig(reservering.getVoertuig());
 				} else if (tempReservering.getKlant() == null ^ tempReservering.getVoertuig() == null)
 				{
 					System.out.println("Het gaat anders dan je dacht Daan");
 				}
 				reservering = tempReservering;
 			}
 		}
 
 		
 		catch(SQLException sqle)
 		{
 			sqle.printStackTrace();
 		}
 		finally
 		{
 			if (resultSet != null) try
 			{
 				resultSet.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			if (preparedStatement != null) try
 			{
 				preparedStatement.close();
 			}
 			catch (SQLException negeer)
 			{
 			}
 			closeConnection();	
 		}
 		return reservering;
 	}
 
 }
