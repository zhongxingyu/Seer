 package br.net.du.jdbc;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import br.net.du.jdbc.model.Contato;
 
 public class ContatoDAO {
 	private Connection connection;
 
 	public ContatoDAO(Connection connection) {
 		this.connection = connection;
 	}
 
 	public void save(Contato contato) {
 		if (contato.getId() == null || contato.getId() == 0) {
 			this.add(contato);
 		} else {
 			this.update(contato);
 		}
 	}
 
 	public void add(Contato contato) {
 		String sql = "INSERT INTO contatos (nome, email, endereco, dataNascimento) "
 				+ "VALUES (?, ?, ?, ?)";
 
 		PreparedStatement statement;
 
 		try {
			statement = (PreparedStatement) connection.prepareStatement(sql,
					Statement.RETURN_GENERATED_KEYS);
 			statement.setString(1, contato.getNome());
 			statement.setString(2, contato.getEmail());
 			statement.setString(3, contato.getEndereco());
 			statement.setDate(4, new Date(contato.getDataNascimento()
 					.getTimeInMillis()));
 
 			statement.execute();
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.first()) {
				contato.setId(generatedKeys.getLong(1));
			}
 			statement.close();
 		} catch (SQLException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	public void update(Contato contato) {
 		String sql = "UPDATE contatos SET nome = ?, email = ?, endereco = ?, dataNascimento = ? "
 				+ "WHERE id = ?";
 
 		try {
 			PreparedStatement statement = (PreparedStatement) connection
 					.prepareStatement(sql);
 
 			statement.setString(1, contato.getNome());
 			statement.setString(2, contato.getEmail());
 			statement.setString(3, contato.getEndereco());
 			statement.setDate(4, new Date(contato.getDataNascimento()
 					.getTimeInMillis()));
 			statement.setLong(5, contato.getId());
 
 			statement.execute();
 			statement.close();
 
 		} catch (SQLException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	public void delete(Contato contato) {
 		String sql = "DELETE FROM contatos WHERE id = ?";
 
 		try {
 			PreparedStatement statement = (PreparedStatement) connection
 					.prepareStatement(sql);
 
 			statement.setLong(1, contato.getId());
 
 			statement.execute();
 			statement.close();
 
 		} catch (SQLException e) {
 			throw new DAOException(e);
 		}
 	}
 
 	public List<Contato> findAll() {
 		return findByClause("");
 	}
 
 	public List<Contato> findByNome(String nome) {
 		return findByClause(" WHERE `nome` LIKE \"%" + nome + "%\"");
 	}
 
 	public Contato findById(Long id) {
 		List<Contato> contatos = findByClause(" WHERE `id` = " + id);
 
 		if (contatos.isEmpty()) {
 			return null;
 		}
 
 		return contatos.get(0);
 	}
 
 	private List<Contato> findByClause(String clause) {
 		String sql = "SELECT * FROM `contatos`" + clause;
 		PreparedStatement statement;
 
 		List<Contato> contatos = new ArrayList<Contato>();
 
 		try {
 			statement = (PreparedStatement) connection.prepareStatement(sql);
 			ResultSet resultSet = statement.executeQuery();
 
 			while (resultSet.next()) {
 				Long id = resultSet.getLong("id");
 				String nome = resultSet.getString("nome");
 				String email = resultSet.getString("email");
 				String endereco = resultSet.getString("endereco");
 				Calendar dataNascimento = Calendar.getInstance();
 				dataNascimento.setTime(resultSet.getDate("dataNascimento"));
 
 				Contato contato = new Contato();
 				contato.setId(id);
 				contato.setNome(nome);
 				contato.setEmail(email);
 				contato.setEndereco(endereco);
 				contato.setDataNascimento(dataNascimento);
 
 				contatos.add(contato);
 			}
 
 			resultSet.close();
 			statement.close();
 
 			return contatos;
 		} catch (SQLException e) {
 			throw new DAOException(e);
 		}
 	}
 }
