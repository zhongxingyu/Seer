 package gestionale.magazzino.models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import gestionale.magazzino.Querist;
 
 public class Acquisto {
 	static Querist que;
 	
 	/**
 	 * La query non vede inserita la data in quanto in fase di progettazione 
 	 * del database  stata impostata CURRENT_TIMESTAMP
 	 * @param idDipendente
 	 * @param idProdotto
 	 * @param idFondo
 	 * @param qta quantit del prodotto comprata
 	 */
 	static public void inserisciAcquisto(int idDipendente, int idProdotto, int idFondo, int qta){
 		que = new Querist();
 		String query = "INSERT INTO Acquisto(idDipendente,idProdotto,idFondo,qta) VALUES"+
 				"("+idDipendente+","+idProdotto+","+idFondo+","+qta+")";
 		que.eseguiQueryUpdate(query);
 	}
 	
 	/**
 	 * Questo metodo cancella dal database un acquisto fatto
 	 * @param idAcquisto identificativo dell'acquisto
 	 */
 	static public void cancellaAcquisto (int idAcquisto){
 		que = new Querist();
 		String query = "DELETE FROM Acquisto WHERE idAcquisto = "+idAcquisto;
 		que.eseguiQueryUpdate(query);
 	}
 
 	/**
 	 * Questo metodo visualizza tutti gli acquisti dei dipendenti
 	 * @return un arrayList di acquisti
 	 */
 	static public ArrayList<gestionale.magazzino.Acquisto> visualizzaAcquisti(){
 		que = new Querist();
 		ArrayList<gestionale.magazzino.Acquisto> risultato = new ArrayList<gestionale.magazzino.Acquisto>();
 		String query = "SELECT A.idAcquisto, D.nome AS dipendente, P.nome AS prodotto, F.nome AS fondo, A.qta, A.qta * P.prezzoUnita AS spesa, A.dataAcquisto " +
 					   "FROM Acquisto A, Prodotto P, Fondo F, Dipendente D " +
 					   "WHERE A.idDipendente = D.idDipendente AND " +
 					         "A.idProdotto = P.idProdotto AND " +
 					         "A.idFondo = F.idFondo";
 		System.out.println(query);
 		ResultSet rs = que.eseguiQuery(query);
 		try {
 			while(rs.next()){
 				gestionale.magazzino.Acquisto acq = new gestionale.magazzino.Acquisto(rs.getInt("idAcquisto"),rs.getString("dipendente"), rs.getString("prodotto"), rs.getString("fondo"), rs.getInt("qta"), rs.getFloat("spesa"), rs.getString("dataAcquisto"));
 				risultato.add(acq);
 			}
 		} catch (SQLException e) {
 			gestionale.magazzino.Acquisto acq = null;
 			risultato.add(acq);
 			return risultato;
 		}
 		return risultato;
 	}
 
 	/**
 	 * Questo metodo visualizza gli acquisti di un dato dipendente
 	 * @param idDipendente identificativo del dipendente di cui si vogliono gli acquisti
 	 * @return un arraylist con tutti gli acquisti
 	 */
 	static public ArrayList<gestionale.magazzino.Acquisto> visualizzaAcquistiDipendente(int idDipendente){
 		que = new Querist();
 		ArrayList<gestionale.magazzino.Acquisto> risultato = new ArrayList<gestionale.magazzino.Acquisto>();
 		String query = "SELECT A.idAcquisto, D.nome AS dipendente, P.nome AS prodotto, F.nome AS fondo, A.qta,A.qta * P.prezzoUnita AS spesa, A.dataAcquisto " +
 				" FROM Acquisto A, Dipendente D, Prodotto P, Fondo F " +
				" WHERE A.idDipendente = '"+idDipendente+"' AND " +
 				"A.idProdotto = P.idProdotto AND " +
 				"A.idFondo = F.idFondo ";
 		System.out.println(query);
 		ResultSet rs = que.eseguiQuery(query);
 		try {
 			while(rs.next()){
 				gestionale.magazzino.Acquisto acq = new gestionale.magazzino.Acquisto(rs.getInt("idAcquisto"),rs.getString("dipendente"), rs.getString("prodotto"), rs.getString("fondo"), rs.getInt("qta"), rs.getFloat("spesa"), rs.getString("dataAcquisto"));
 				risultato.add(acq);
 			}
 		} catch (SQLException e) {
 			gestionale.magazzino.Acquisto acq = null;
 			risultato.add(acq);
 			return risultato;
 		}
 		return risultato;
 	}
 	
 	/**
 	 * Questo metodo visualizza un singolo acquisto in base al suo identificativo
 	 * @param idAcquisto identificativo dell'acquisto
 	 * @return un oggetto Acquisto
 	 */
 	static public gestionale.magazzino.Acquisto visualizzaAcquisto(int idAcquisto){
 		que = new Querist();
 		String query = "SELECT A.idAcquisto, A.idDipendente, A.idProdotto, A.idFondo, A.qta, A.dataAcquisto " +
 				       "FROM Acquisto A " +
 				       "WHERE A.idAcquisto = "+idAcquisto;
 		System.out.println(query);
 		ResultSet rs = que.eseguiQuery(query);
 		gestionale.magazzino.Acquisto acq = null;
 		try{
 			acq = new gestionale.magazzino.Acquisto(rs.getInt("idAcquisto"), rs.getInt("idDipendente"), rs.getInt("idProdotto"), rs.getInt("idFondo"), rs.getInt("qta"), rs.getString("dataAcquisto"));
 		}catch(SQLException e){
 		}
 		return acq;
 		
 	}
 }
