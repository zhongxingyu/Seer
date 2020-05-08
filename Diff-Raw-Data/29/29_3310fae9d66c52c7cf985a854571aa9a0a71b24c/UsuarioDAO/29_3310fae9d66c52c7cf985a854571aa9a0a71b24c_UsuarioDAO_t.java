 package br.com.puc.sispol.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import br.com.puc.sispol.ConnectionFactory;
 import br.com.puc.sispol.modelo.Usuario;
 
 public class UsuarioDAO {
 	private Connection connection;
 
 	public UsuarioDAO() {
 		try {
 			connection = new ConnectionFactory().getConnection();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public boolean existeUsuario(Usuario usuario) {
 		
 		/*if(usuario == null) {
 			throw new IllegalArgumentException("Usuário não deve ser nulo");
 		}*/
 		
 		try {
 			PreparedStatement stmt = this.connection.prepareStatement("select * from Usuario where email = ? and senha = ?");
 			stmt.setString(1, usuario.getEmail());
 			stmt.setString(2, usuario.getSenha());
 			ResultSet rs = stmt.executeQuery();
 			boolean encontrado = false;
 			encontrado = rs.next();
 			rs.close();
 			stmt.close();
 
 			return encontrado;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public Usuario buscaPorLogin(Usuario usuario) {
 
 		/*if (usuario.getLogin() == "") {
 			throw new IllegalStateException("Login do usuario não deve ser nulo.");
 		}*/
 
 		try {
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select * from Usuario where email = ? AND senha = ?" );
 			stmt.setString(1, usuario.getEmail());
 			stmt.setString(2, usuario.getSenha());
 			ResultSet rs = stmt.executeQuery();
 
 			if (rs.next()) {
 				return populaUsuario(rs);
 			}
 
 			rs.close();
 			stmt.close();
 
 			return null;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public Usuario populaUsuario(ResultSet rs) throws SQLException {
 		Usuario usuario = new Usuario();
 
 		// popula o objeto tarefa
 		usuario.setCodUsuario(rs.getLong("CodUsuario"));
 		usuario.setEmail(rs.getString("email"));
 		usuario.setSenha(rs.getString("senha"));
 		usuario.setPerfil(rs.getString("perfil"));
 		usuario.setNome(rs.getString("nome"));
 
 		return usuario;
 	}
 
 	public Usuario buscaPorCodigo(Long codUsuario) {
 		if (codUsuario == null) {
 			throw new IllegalStateException("Codigo do usuario não deve ser nulo.");
 		}
 
 		try {
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select * from sispol.Usuario where CodUsuario = ?");
 			stmt.setLong(1, codUsuario);
 
 			ResultSet rs = stmt.executeQuery();
 
 			if (rs.next()) {
 				return populaUsuario(rs);
 			}
 
 			rs.close();
 			stmt.close();
 
 			return null;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
 
