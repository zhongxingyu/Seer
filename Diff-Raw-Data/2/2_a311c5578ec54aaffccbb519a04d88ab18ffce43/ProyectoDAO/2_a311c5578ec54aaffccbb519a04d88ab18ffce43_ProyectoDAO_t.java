 package com.odea.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import com.odea.domain.Actividad;
 import com.odea.domain.Proyecto;
 
 @Repository
 public class ProyectoDAO extends AbstractDAO {
 	
 	public List<Proyecto> getProyectos()
 	{
 		List<Proyecto> proyectos = jdbcTemplate.query("SELECT * FROM projects", new RowMapper<Proyecto>() {
 			@Override
 			public Proyecto mapRow(ResultSet rs, int rowNum) throws SQLException {
 				boolean habilitado = rs.getInt(5) == 1;
 				return new Proyecto(rs.getInt(1), rs.getString(3),habilitado);
 			}
 		});
 		
 		Collections.sort(proyectos);
 		
 		return proyectos;
 	}
 	
 	public List<Proyecto> getProyectosHabilitados()
 	{
 		List<Proyecto> proyectos = jdbcTemplate.query("SELECT * FROM projects WHERE p_status=1", new RowMapper<Proyecto>() {
 			@Override
 			public Proyecto mapRow(ResultSet rs, int rowNum) throws SQLException {
 				boolean habilitado = rs.getInt(5) == 1;
 				return new Proyecto(rs.getInt(1), rs.getString(3),habilitado);
 			}
 		});
 		
 		Collections.sort(proyectos);
 		
 		return proyectos;
 	}
 	
 	public List<Proyecto> getProyectos(Actividad actividad)
 	{
		List<Proyecto> proyectos = jdbcTemplate.query("SELECT ab.ab_id_p, p.p_name, p.p_status FROM projects p, activity_bind ab WHERE p.p_id = ab.ab_id_p AND ab.ab_id_a = ?", new RowMapper<Proyecto>() {
 			@Override
 			public Proyecto mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return new Proyecto(rs.getInt(1), rs.getString(2),rs.getInt(3)==1);
 			}
 		}, actividad.getIdActividad());
 		
 		Collections.sort(proyectos);
 		
 		return proyectos;
 	}
 	
 	public List<Proyecto> getProyectosHabilitados(Actividad actividad)
 	{
 		List<Proyecto> proyectos = jdbcTemplate.query("SELECT ab.ab_id_p, p.p_name p.p_status FROM projects p, activity_bind ab WHERE p.p_id = ab.ab_id_p AND ab.ab_id_a = ? AND p.p_status=1", new RowMapper<Proyecto>() {
 			@Override
 			public Proyecto mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return new Proyecto(rs.getInt(1), rs.getString(2),rs.getInt(3)==1);
 			}
 		}, actividad.getIdActividad());
 		
 		Collections.sort(proyectos);
 		
 		return proyectos;
 	}
 	
 	public Proyecto buscarPorNombre(String nombre){
 		
 		if (nombre.charAt(0)== " ".charAt(0)){
 			nombre = nombre.substring(1);
 		}
 		
 		Integer id;
 		try {
 			id = jdbcTemplate.queryForInt("SELECT p_id FROM projects where p_name=?", nombre);
 		} catch (DataAccessException e) {
 			nombre = nombre.replaceAll("ó","Ã³").replaceAll("é","Ã©").replaceAll("ñ","Ã±").replaceAll("á","Ã¡").replaceAll("í","Ã­") ;		
 			id = jdbcTemplate.queryForInt("SELECT p_id FROM projects where p_name=?", nombre);
 		}
 		return new Proyecto(id, nombre,true);
 	}
 	
 	public void cambiarNombreProyecto(Proyecto proyecto){
 		jdbcTemplate.update("UPDATE projects SET p_name=? WHERE p_id=?",proyecto.getNombre(),proyecto.getIdProyecto());
 		
 	}
 	
 	public void borrarProyecto(Proyecto proyecto){
 		jdbcTemplate.update("DELETE FROM projects WHERE p_id=?",proyecto.getIdProyecto());
 		jdbcTemplate.update("DELETE FROM activity_bind WHERE ab_id_p=?",proyecto.getIdProyecto());
 	}
 	
 	
 	
 	public void actualizarRelaciones(int idProyecto, List<Actividad> borrar, List<Actividad> añadir){
 		
 		for (Actividad actividad : añadir) {
 			
 			int max=jdbcTemplate.queryForInt("SELECT max(ab_id) FROM activity_bind")+1;
 			
 			String sql= "INSERT INTO activity_bind (ab_id,ab_id_a,ab_id_p)";
 				sql+="SELECT * FROM (SELECT ?,?,?) AS tmp";
 				sql+="WHERE NOT EXISTS (";
 				sql+="SELECT * FROM activity_bind WHERE (ab_id_a=? and ab_id_p=? )";
 				sql+=") LIMIT 1;";
 		jdbcTemplate.update(sql,max,actividad.getIdActividad(),idProyecto,actividad.getIdActividad(),idProyecto);
 		}
 		
 		for (Actividad actividad2 : borrar) {
 			jdbcTemplate.update("DELETE FROM activity_bind WHERE ab_id_p=? and ab_id_a=?",idProyecto, actividad2.getIdActividad());
 		}
 		
 		
 		
 	}
 	
 	public void insertarProyecto(Proyecto proyecto, Collection<Actividad> actividadesRelacionadas) {
 		
 		if (proyecto.getIdProyecto() == 0) {
 			this.agregarProyecto(proyecto, actividadesRelacionadas);
 		}else{
 			this.modificarProyecto(proyecto, actividadesRelacionadas);
 		}
 		
 	}
 	
 	private void modificarProyecto(Proyecto proyecto, Collection<Actividad> actividadesRelacionadas) {
 		int idActivityBind = jdbcTemplate.queryForInt("SELECT max(ab_id) FROM activity_bind");
 		
 		jdbcTemplate.update("UPDATE projects SET p_name=? WHERE p_id=?", proyecto.getNombre(), proyecto.getIdProyecto());
 		
 		jdbcTemplate.update("DELETE FROM activity_bind WHERE ab_id_p=?", proyecto.getIdProyecto());
 		
 		for (Actividad actividad : actividadesRelacionadas) {
 			idActivityBind += 1;
 			jdbcTemplate.update("INSERT INTO activity_bind (ab_id, ab_id_a, ab_id_p) VALUES (?,?,?)", idActivityBind, actividad.getIdActividad(), proyecto.getIdProyecto());
 		}
 	}
 
 	public void agregarProyecto(Proyecto proyecto, Collection<Actividad> actividadesRelacionadas) {
 		
 		int idProyecto = jdbcTemplate.queryForInt("SELECT max(p_id) FROM projects")+1;
 		
 		jdbcTemplate.update("INSERT INTO projects (p_id, p_name) VALUES (?,?)", idProyecto, proyecto.getNombre());
 
 		
 		int idActivityBind = jdbcTemplate.queryForInt("SELECT max(ab_id) FROM activity_bind");
 		
 		for (Actividad actividad : actividadesRelacionadas) {
 			idActivityBind += 1;
 			jdbcTemplate.update("INSERT INTO activity_bind (ab_id, ab_id_a, ab_id_p) VALUES (?,?,?)", idActivityBind, actividad.getIdActividad(), idProyecto);			
 		}
 
 	}
 	
 	public List<Proyecto> obtenerOrigen(Actividad actividad)
 	{
 		String sql = "SELECT DISTINCT pa.ab_id_p, p.p_name p.p_status FROM projects p, activity_bind pa ";
 		sql += "WHERE pa.ab_id_p = p.p_id AND pa.ab_id_a <> ? ";
 		sql += "AND p.p_id NOT IN (SELECT DISTINCT p.p_id FROM projects p, activity_bind pa WHERE pa.ab_id_p = p.p_id AND pa.ab_id_a = ?)";
 		
 		List<Proyecto> proyectos = jdbcTemplate.query(sql, new RowMapper<Proyecto>() {
 			@Override
 			public Proyecto mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return new Proyecto(rs.getInt(1), rs.getString(2),rs.getInt(3)==1);
 			}
 		}, actividad.getIdActividad(), actividad.getIdActividad());
 		
 		Collections.sort(proyectos);
 		
 		return proyectos;
 		
 	}
 	
 	public void cambiarStatus(Proyecto proyecto) {
 			int status;
 			proyecto.setHabilitado(!proyecto.isHabilitado());
 			if (proyecto.isHabilitado()) {
 				status = 1;
 			} else {
 				status = 0;
 			}
 			
 			jdbcTemplate.update("UPDATE projects SET p_status=? WHERE p_id=?", status, proyecto.getIdProyecto());
 		}
 	
 	public void relacionarProyecto(Proyecto proyecto, List<Integer> actividad){
 		for (int i = 0; i < actividad.size(); i++) {
 			jdbcTemplate.update("INSERT INTO activity_bind(ab_id_a,ab_id_p) VALUES (?,?)",i,proyecto.getIdProyecto());
 		}
 	}
 
 	
 	
 	
 	
 }
