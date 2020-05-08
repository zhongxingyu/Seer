 package py.edu.uca.fcyt.toluca.db;
 
 /** Java class "DbOperations.java" generated from Poseidon for UML.
  *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
  *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
  */
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.List;
 import java.util.Properties;
 
 import py.edu.uca.fcyt.game.Game;
 import py.edu.uca.fcyt.toluca.LoginFailedException;
 import py.edu.uca.fcyt.toluca.RoomServer;
 import py.edu.uca.fcyt.toluca.game.TrucoPlayer;
 import py.edu.uca.fcyt.toluca.game.TrucoTeam;
 
 /**
  * <p>
  * La clase DbOperations provee al TOLUCA de la interfaz a los datos alojados en
  * una base de datos relacional en el lado del servidor. Los m&#233;todos
  * p&#250;blicos permiten a los otros objetos acceder a los datos subyacentes.
  * Es instanciado por el RoomServer que es el programa principal del lado del
  * servidor. Los m&#233;todos son todos s&#237;ncronos. Queda para las futuras
  * generaciones hacer que esta clase sea un thread as&#237;ncrono para procesar
  * de manera m&#225;s eficiente los requests a la base de datos.
  * </p>
  *  
  */
 public class DbOperations {
 
     ///////////////////////////////////////
     // attributes
 
     /**
      * <p>
      * Representa la conexi&#243;n al servidor de datos (SQL Server)
      * </p>
      *  
      */
     private Connection conn;
 
     ///////////////////////////////////////
     // associations
 
     /**
      * <p>
      * 
      * </p>
      */
     private RoomServer roomServer;
 
 	public static final String DBURL = "dburl";
 	public static final String USER_NAME = "uname";
 	public static final String PASSWORD = "password";	
 
     ///////////////////////////////////////
     // operations
 
     /**
      * <p>
      * Crea un nuevo usuario en el sistema, haciendo la inserci&#243;n
      * correspondiente en la tabla &quot;USUARIOS&quot; del sistema
      * </p>
      * <p>
      * 
      * @param uname
      *            ... Nombre de usuario
      *            </p>
      *            Inserta un Player en la base de datos
      *            <p>
      * @param upass
      *            ... Password
      *            </p>
      */
     public void createPlayer(String email, String upass, String uname)
             throws SQLException {
 
     } // end createPlayer
 
     /**
      * <p>
      * Borra de la tabla de usuarios a un jugador
      * </p>
      * <p>
      * 
      * </p>
      * <p>
      * 
      * @param uname
      *            Nombre de usuario del jugador a borrar
      *            </p>
      */
     public void deletePlayer(TrucoPlayer player) {
         // your code here
     } // end deletePlayer
 
     /**
      * <p>
      * Acutaliza los datos del jugador en la Tabla
      * </p>
      * <p>
      * 
      * @param player
      *            Datos actualizados del jugador que deben ser reflejados en la
      *            base de datos
      *            </p>
      */
     public void updatePlayerData(TrucoPlayer player) {
         // your code here
     } // end updatePlayerData
 
     /**
      * <p>
      * Does ...
      * </p>
      * <p>
      * 
      * </p>
      * <p>
      * 
      * @param game
      *            ...
      *            </p>
      */
     public void createGame(Game game) {
         // your code here
     } // end createGame
 
     /**
      * <p>
      * Does ...
      * </p>
      * <p>
      * 
      * </p>
      * <p>
      * 
      * @param game
      *            ...
      *            </p>
      */
     public void updateGameData(Game game) {
         // your code here
     } // end updateGameData
 
     /**
      * <p>
      * Crea una entrada en la tabla &quot;LOG&quot; con el mensaje. Utilizado
      * exclusivamente para mantener una bit&#225;cora de los eventos
      * interesantes del sistema.
      * </p>
      * <p>
      * 
      * </p>
      * <p>
      * 
      * @param logMessage
      *            El mensaje que se quiere alojar en la tabla
      *            </p>
      */
     public void log(String logMessage) {
         // your code here
     } // end log
 
     /**
      * <p>
      * Autentica al usuario con su password correspondiente
      * </p>
      * <p>
      * 
      * </p>
      * <p>
      * 
      * @param username
      *            Nombre de usuario
      *            </p>
      *            Hace la autenticaci�n del usuario.
      *            <p>
      * @param password
      *            Password o contrasena
      * @return Player el objeto Player o una excepcion de Login
      *         </p>
      * @throws LoginFailedException
      * @throws SQLException
      */
     public TrucoPlayer authenticatePlayer(String username, String password)
             throws SQLException, LoginFailedException {
 
     	
     	
         TrucoPlayer p = getPlayer(username, password);
         return p;
         //throw new LoginFailedException("No anda tu password");
         // else
         //throw new LoginFailedException();
 
     }
 
     /**
      * Getter for property roomServer.
      * 
      * @return Value of property roomServer.
      */
     public RoomServer getRoomServer() {
         return this.roomServer;
     }
 
     /**
      * Setter for property roomServer.
      * 
      * @param roomServer
      *            New value of property roomServer.
      */
     public void setRoomServer(RoomServer roomServer) {
         this.roomServer = roomServer;
     }
 
     public TrucoPlayer getPlayer(String uname, String logPassword) throws SQLException, LoginFailedException {
     	String password = null;
     	int puntaje;
     	
     	final String sqlLogin = "SELECT \"PASSWORD\", PUNTAJE from JUGADORES where JUGADOR = ?";
     	
     	PreparedStatement ps = conn.prepareStatement(sqlLogin);
     	ps.setString(1, uname);
     	ResultSet rs = ps.executeQuery();
     	
     	if (rs.next()) {
     		password = rs.getString(1);
     		puntaje = rs.getInt(2);
     	} else
     		throw new LoginFailedException("El usuario: " + uname + " es incorrecto:" );
 
     	if (!password.equals(logPassword))
     		throw new LoginFailedException("El password es incorrecto para el usuario: " + uname);
     	
         return new TrucoPlayer(uname, puntaje);
         //return new TrucoPlayer(uname, rand.nextInt());
     }
 
     // end authenticatePlayer
 
 
     /**
      * @param properties
      * @param server
      * @throws ClassNotFoundException
      * @throws SQLException
      */
     public DbOperations(Properties properties, RoomServer server) throws SQLException, ClassNotFoundException {
 
     	this.roomServer = server;
     	initConnection(properties);
     	
     }
 
 	/**
 	 * @param properties
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	private void initConnection(Properties properties) throws SQLException, ClassNotFoundException {
 
 		String dburl = properties.getProperty(DBURL);
 		String username = properties.getProperty(USER_NAME);
 		String password = properties.getProperty(PASSWORD);
 		
 		if (dburl == null || username == null || password == null) 
 			throw new SQLException();
 		
 		Class.forName("org.firebirdsql.jdbc.FBDriver");
 		conn = DriverManager.getConnection(	dburl,	username,	password);
 		
 	}
 
 	/**
 	 * @param team1
 	 * @param team2
 	 * @param teamGanador
 	 * @throws SQLException
 	 */
 	public void updateGameData(TrucoTeam team1, TrucoTeam team2, int teamGanador) throws SQLException {
 		int numeroPartida = nuevaPartida(teamGanador);
 		System.out.println("Partida: "+numeroPartida);
 		nuevaPartidaDetalle(team1, team2, numeroPartida);
 		calcularNuevoRating(numeroPartida);
 	}
 
 	/**
 	 * @param numeroPartida
 	 * @throws SQLException
 	 */
 	private void calcularNuevoRating(int numeroPartida) throws SQLException {
 
 		String procedure = "{call calcularating ?  }";		
 		CallableStatement cs = conn.prepareCall(procedure);
 		cs.setInt(1, numeroPartida);
 		cs.execute();
 		cs.close();
 		
 		
 	}
 
 	/**
 	 * @param team1
 	 * @param team2
 	 * @param teamGanador
 	 * @throws SQLException
 	 */
 	private void nuevaPartidaDetalle(TrucoTeam team1, TrucoTeam team2, int partida) throws SQLException {
 		List lp1 = team1.getPlayers();
 		List lp2 = team2.getPlayers();
 		
 		final String sqlInsert =
 		" INSERT INTO PARTIDAS_DETALLE" +
 		" (JUGADOR,PARTIDA,EQUIPO,MARCANTE)" +
 		" VALUES (?, ?, ?, ?)";
 		
 		PreparedStatement insertStmt = conn.prepareStatement(sqlInsert);
 
 		for (int i = 0; i < lp1.size() ; i++) {
 			TrucoPlayer tp1 = (TrucoPlayer)lp1.get(i);
 			TrucoPlayer tp2 = (TrucoPlayer)lp2.get(i);
 			
			//TEAM 0
 			insertStmt.setString(1, tp1.getName());
 			insertStmt.setInt(2, partida);
			insertStmt.setInt(3, 0);
 			insertStmt.setString(4, tp1.getName());
 			
 			insertStmt.executeUpdate();
 
			/*Hacemos lo mismo para el jugador i del equipo 1*/
 			insertStmt.setString(1, tp2.getName());
 			insertStmt.setInt(2, partida);
			insertStmt.setInt(3, 1);
 			insertStmt.setString(4, tp2.getName());
 			
 			insertStmt.executeUpdate();
 			
 		}
 		insertStmt.close();
 	}
 
 	/**
 	 * @param teamGanador
 	 * @return
 	 * @throws SQLException
 	 */
 	private int nuevaPartida(int teamGanador) throws SQLException {
 	
 		String procedure = "{call GET_PARTIDA_ID returning_values ? }";		
 		CallableStatement cs = conn.prepareCall(procedure);
 		cs.registerOutParameter(1, Types.INTEGER);
 		cs.execute();
 		int partidaNro = cs.getInt(1);
 		cs.close();
 			
 		String insertSql = "INSERT INTO PARTIDAS (PARTIDA, GANADOR) VALUES (?, ?);";
 		PreparedStatement ps = conn.prepareStatement(insertSql);
 		ps.setInt(1, partidaNro);
 		ps.setInt(2, teamGanador);
 		ps.executeUpdate();
 		
 		return partidaNro;
 
 	}
 
 
 } // end DbOperations
 
