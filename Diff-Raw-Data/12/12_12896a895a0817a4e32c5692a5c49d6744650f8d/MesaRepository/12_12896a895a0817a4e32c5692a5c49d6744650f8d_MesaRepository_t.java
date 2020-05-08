 package mpa.manager.repository;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Date;
 import java.util.List;
 
 import mpa.manager.bean.Mesa;
 import mpa.manager.bean.MpaConfiguracao;
 import mpa.manager.bean.Objectiviano;
 
 
 public class MesaRepository extends Repository<Mesa> {
 
 	private MesaRepository(Connection connection) throws SQLException {
 		super(connection);
 	}
 
 	private static MesaRepository instance;
 	private MpaConfiguracao mpa = null;
 
 	@Override
 	public Mesa materializa(ResultSet rs) throws SQLException {
 
 		int idMesa = rs.getInt("id_mesa");
 		int numeroMesa = rs.getInt("numero");
 		String time = rs.getString("equipe");
 
 		int id_mpa = rs.getInt("id_mpa");
 		if (mpa == null || mpa.getId() != id_mpa) {
 			Date dataInicio = new Date(rs.getDate("data_inicio").getTime());
 			Date dataFim = rs.getDate("data_fim") != null ? new Date(rs
 					.getDate("data_fim").getTime()) : null;
 			mpa = new MpaConfiguracao(id_mpa, dataInicio, dataFim);
 		}
 
 		int idPrimeiroObjectiviano = rs.getInt("id_des1");
 		int idSegundoObjectiviano = rs.getInt("id_des2");
 		String primeiroNome = rs.getString("primeiroNome");
 		String segundoNome = rs.getString("segundoNome");
 		String primeiroLogin = rs.getString("primeiroLogin");
 		String segundoLogin = rs.getString("segundoLogin");
 
 		return new Mesa(idMesa, numeroMesa, mpa,
 				new Objectiviano(idPrimeiroObjectiviano, primeiroNome, primeiroLogin),
 				segundoNome != null ? new Objectiviano(idSegundoObjectiviano, segundoNome, segundoLogin) : null,
 				time);
 	}
 
 	@Override
 	public String getSql() {
 		return queryMesa("mpa.data_inicio <= current_date and ((mpa.data_fim is null) or (mpa.data_fim >= current_date))");
 	}
 
 	public static void createInstance(Connection connection) throws SQLException {
 		instance = new MesaRepository(connection);
 	}
 
 	public static MesaRepository getInstance() {
 		if (instance == null)
 			throw new IllegalStateException(
 					"MpaRepository não foi instanciado ainda. Utilize createInstance(connection)");
 
 		return instance;
 	}
 
 	public void update(Mesa mesa) throws SQLException {
 		if (mesa.getMpa().isPassado()) throw new IllegalStateException("Não é possível alterar mpas passados.");
 		
 		String update = "UPDATE MESA SET ID_PRIMEIRO_OBJECTIVIANO = ?, ID_SEGUNDO_OBJECTIVIANO = ?, EQUIPE = ? WHERE ID = ?";
 		
 		PreparedStatement statement = null;
 		try{
 			statement = connection.prepareStatement(update);
 			statement.setInt(1, mesa.getPrimeiroObjectiviano().getId());
 			if (mesa.getSegundoObjectiviano() != null)
 				statement.setInt(2,  mesa.getSegundoObjectiviano().getId());
 			else 
 				statement.setNull(2, Types.INTEGER);
 			
 			statement.setString(3, mesa.getEquipe());
 			statement.setInt(4, mesa.getId());
 			statement.execute();
 		} finally {
 			statement.close();
 		}
 	}
 
 	public void insert(Mesa mesa) throws SQLException{
 		if (mesa.getMpa().isPassado()) throw new IllegalStateException("Não é possível alterar mpas passados.");
 		
 		PreparedStatement statement = null;
 		try {
 			String insert = "INSERT INTO MESA VALUES (NULL, ?, ?, ?, ?, ?)";
 			statement = connection.prepareStatement(insert);
 			statement.setInt(1, mesa.getMpa().getId());
 			statement.setInt(2, mesa.getNumero());
 			statement.setInt(3, mesa.getPrimeiroObjectiviano().getId());
 			if (mesa.getSegundoObjectiviano() != null)
 				statement.setInt(4,  mesa.getSegundoObjectiviano().getId());
 			else 
 				statement.setNull(4, Types.INTEGER);
 			
 			statement.setString(5, mesa.getEquipe());
 			statement.execute();
 		} catch (SQLException e) {
 			throw new IllegalStateException("Número de mesa já existente.");
 		} finally {
 			statement.close();
 		}
 	}
 	
 	public List<Mesa> dadoMpa(MpaConfiguracao mpa) throws SQLException {
 		String select = queryMesa("mpa.id = ?");
 		
 		PreparedStatement statement = null; 
 		ResultSet rs = null;
 		List<Mesa> result = null;
 		try{
 			statement = connection.prepareStatement(select);
 			statement.setInt(1, mpa.getId());
 			rs = statement.executeQuery();
 			result = materializaEntidades(rs);
 		} finally {
 			rs.close();
 			statement.close();
 		}
 		
 		return result;
 	}
 
 	public void delete(Mesa mesa) throws SQLException {
 		PreparedStatement statement = null;
 		int numeroMesa = 0;
 		String sql;
 		numeroMesa = getNumero(mesa);
 		
 		sql = "DELETE FROM MESA WHERE ID = ?";
 		
 		try{
 			statement = connection.prepareStatement(sql);
 			statement.setInt(1, mesa.getId());
 			statement.execute();
 		} finally {
 			statement.close();
 		}
 		
 		sql = "UPDATE MESA M " +
 				"SET M.NUMERO = M.NUMERO -1 " +
 				"WHERE M.ID_MPA_CONFIGURACAO = ? " +
 				"AND M.NUMERO > ?";
 
 		try{
 			statement = connection.prepareStatement(sql);
 			statement.setInt(1, mesa.getMpa().getId());
 			statement.setInt(2, numeroMesa);
 			statement.executeUpdate();
 		} finally {
 			statement.close();
 		}
 	}
 	
 	public void move(Mesa mesa, int direcao) throws SQLException {
 		PreparedStatement statement = null;
 		int numeroMesa = getNumero(mesa);
 		String sql;
 		
 		sql = "UPDATE MESA M " +
 				"SET M.NUMERO = ? " +
 				"WHERE M.ID_MPA_CONFIGURACAO = ? " +
 				"AND M.NUMERO = ?";
 		
 		try{
 			statement = connection.prepareStatement(sql);
 			statement.setInt(1, 9999);
 			statement.setInt(2, mesa.getMpa().getId());
 			statement.setInt(3, numeroMesa + direcao);
 			statement.executeUpdate();
			statement.close();
 			
 			statement = connection.prepareStatement(sql);
 			statement.setInt(1, numeroMesa + direcao);
 			statement.setInt(2, mesa.getMpa().getId());
 			statement.setInt(3, numeroMesa);
 			statement.executeUpdate();
			statement.close();
 			
 			statement = connection.prepareStatement(sql);
 			statement.setInt(1, numeroMesa);
 			statement.setInt(2, mesa.getMpa().getId());
 			statement.setInt(3, 9999);
 			statement.executeUpdate();
 		} finally {
 			statement.close();
 		}
 	}
 
 	private int getNumero(Mesa mesa) throws SQLException {
 		PreparedStatement statement = null;
 		ResultSet rs = null;
 		int numeroMesa;
 		String sql = "SELECT NUMERO FROM MESA WHERE ID = ?";
 		try {
 			statement = connection.prepareStatement(sql);
 			statement.setInt(1, mesa.getId());
 			rs = statement.executeQuery();
 			rs.next();
 			numeroMesa = rs.getInt("NUMERO");			
 		} finally {
 			statement.close();
 			rs.close();
 		}
 		return numeroMesa;
 	}
 
 	private String queryMesa(String where) {
 		return "select mpa.id as id_mpa, mpa.data_inicio, mpa.data_fim,"
 				+ "    mesa.id as id_mesa, mesa.numero, mesa.equipe,"
 				+ "    des1.id as id_des1, des1.nome as primeiroNome, des1.login as primeiroLogin,"
 				+ "    des2.id as id_des2, des2.nome as segundoNome, des2.login as segundoLogin"
 				+ " from mpa_configuracao mpa"
 				+ " inner join mesa mesa on mesa.id_mpa_configuracao = mpa.id"
 				+ " inner join objectiviano des1 on des1.id = mesa.id_primeiro_objectiviano"
 				+ "  left outer join objectiviano des2 on des2.id = mesa.id_segundo_objectiviano"
 				+ " where " + where;
 	}	
 }
