 package com.odea.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Service;
 
 import com.odea.domain.Proyecto;
 import com.odea.domain.Usuario;
 
 
 
 @Service
 public class UsuarioDAO extends AbstractDAO {
 	
 	public Usuario getUsuario(String nombre){
 		Usuario usuario = jdbcTemplate.queryForObject("SELECT * FROM users WHERE u_name='" + nombre + "'", new RowMapper<Usuario>(){
 				@Override
 				public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new Usuario(rs.getInt(1),rs.getString(3),rs.getString(4));
 				}
 			});
 		return usuario;
 	}
 	
 	// este no se va a usar, el usuario esta solo en el name
 //	public Usuario getUsuario(Usuario user){
 //		Usuario usuario = jdbcTemplate.queryForObject("SELECT * FROM usuarios WHERE nombre='" + user.getNombre() + "' AND apellido='" + user.getApellido() + "'", new RowMapper<Usuario>(){
 //				@Override
 //				public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
 //					return new Usuario(rs.getInt(1),rs.getString(2),rs.getString(3));
 //				}
 //			});
 //		return usuario;
 //	}
 	
 	
 	
 	public void agregarUsuario(Usuario usuario){
 		
 		jdbcTemplate.update("INSERT INTO users(u_id,u_login,u_password) VALUES (?,?,?)", usuario.getIdUsuario(), usuario.getNombre(), usuario.getPassword());
 	}
 	
 	public Collection<Usuario> getUsuarios(Proyecto proyecto){
 		Collection<Usuario> usuarios = jdbcTemplate.query("SELECT u.u_id, u.u_name, u.u_password FROM users u, user_bind up WHERE u.u_id=up.ub_id_u AND up.ub_id_p='" + proyecto.getIdProyecto() + "'", new RowMapper<Usuario>() {
 			
 			@Override
 			public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return new Usuario(rs.getInt(1), rs.getString(2), rs.getString(3));
 			}
 		});
 		
 		return usuarios;
 		
 	}
 	
 
 }
