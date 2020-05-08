 package com.odea.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.wicket.util.time.Time;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import com.odea.domain.Actividad;
 import com.odea.domain.Entrada;
 import com.odea.domain.Proyecto;
 import com.odea.domain.Usuario;
 
 @Repository
 public class EntradaDAO extends AbstractDAO {
 	
     private static final Logger logger = LoggerFactory.getLogger(EntradaDAO.class);
 
 	
 	
 	private String sqlEntradas = "SELECT e.al_timestamp, e.al_project_id, e.al_activity_id, e.al_duration, e.al_comment, e.ticket_bz, e.ite_id, e.issue_tracker_externo, e.al_user_id, e.al_date, p.p_name , u.u_login, u.u_password, a.a_name FROM activity_log e, projects p, activities a, users u";
 
 	public void agregarEntrada(Entrada entrada){
 
 		String sistemaExterno = this.parsearSistemaExterno(entrada.getSistemaExterno()); 
 		double duracion = this.parsearDuracion(entrada.getDuracion());
 
 		logger.debug("Insert attempt entrada");
 		
 		jdbcTemplate.update("INSERT INTO activity_log (al_project_id, al_activity_id, al_duration, al_comment, ticket_bz, issue_tracker_externo, ite_id, al_user_id, al_date) VALUES (?,?,?,?,?,?,?,?,?)", 
 				new Object[]{
 				entrada.getProyecto().getIdProyecto(), entrada.getActividad().getIdActividad(), new java.sql.Time((long) ((this.parsearDuracion(entrada.getDuracion())*3600000))-(3600000*21)), 
 				entrada.getNota(), entrada.getTicketBZ(), 
 				sistemaExterno, entrada.getTicketExterno(), (entrada.getUsuario().getIdUsuario())
 				, entrada.getFecha()},
 				new int[]{Types.INTEGER, Types.INTEGER, Types.TIME,Types.BLOB,Types.INTEGER,Types.CHAR,Types.VARCHAR,Types.INTEGER ,Types.TIMESTAMP});
 		
 		logger.debug(entrada.getDuracion() +"Entrada agregada - " + new Date(System.currentTimeMillis()));
 		
 	}
 	
 	
 	
 
 
 
 	private double parsearDuracion(String duracion) {
 		double resultado = 0;
 		
 		boolean tieneFormatoHHMM = duracion.indexOf(':') != -1;
 		boolean tieneFormatoDecimal = duracion.indexOf(',') != -1;
 		
 		if (tieneFormatoHHMM) {
 			int pos = duracion.indexOf(':');
 			String horas = duracion.substring(0, pos);
 			String minutos = duracion.substring(pos+1);
 			
 			double hs = Double.parseDouble(horas);
 			double min = Double.parseDouble(minutos) * 100 / 60;
 			
 			resultado = hs + (min / 100); 
 			
 		}
 		
 		
 		if (tieneFormatoDecimal) {
 			duracion = duracion.replace(',', '.');
 			resultado = Double.parseDouble(duracion);
 		}
 		
 		if (!(tieneFormatoDecimal || tieneFormatoHHMM)) {
 			resultado = Double.parseDouble(duracion);
 		}
 		
 		
 		return resultado;
 		
 	}
 
 
 
 
 
 
 	public List<Entrada> getEntradas(Usuario usuario, Timestamp desdeSQL, Timestamp hastaSQL){
 		
 		return jdbcTemplate.query(sqlEntradas +" WHERE e.al_user_id = ? AND e.al_project_id = p.p_id AND e.al_activity_id = a.a_id AND e.al_user_id = u.u_id AND e.al_date BETWEEN ? AND ?", new RowMapper<Entrada>() {
 			@Override
 			public Entrada mapRow(ResultSet rs, int rowNum) throws SQLException {
 				Proyecto proyecto = new Proyecto(rs.getInt(2), rs.getString(11));
 				Actividad actividad = new Actividad(rs.getInt(3), rs.getString(14));
 				Usuario usuario = new Usuario(rs.getInt(9), rs.getString(12), rs.getString(13));
				return new Entrada(rs.getTimestamp(1), proyecto, actividad, String.valueOf(((Double.parseDouble(String.valueOf(rs.getTime(4).getTime()))/3600) - 3000) /1000).substring(0,3)  /* String.valueOf(((Time.valueOf(rs.getTime(4)).getMilliseconds() /3600)-3000))*/, rs.getString(5), rs.getInt(6), rs.getString(7), rs.getString(8), usuario, rs.getDate(10));
 			}}, usuario.getIdUsuario(), desdeSQL, hastaSQL);
 	}
 	
 	public Collection<Entrada> getEntradas(Date desde,Date hasta){
 		Timestamp desdeSQL = new Timestamp(desde.getTime());
 		Timestamp hastaSQL = new Timestamp(hasta.getTime());
 		
 		Collection<Entrada> entradas = jdbcTemplate.query(sqlEntradas + " WHERE e.al_user_id = u.u_id AND e.al_project_id = p.p_id AND e.al_activity_id = a.a_id AND e.al_date BETWEEN ? AND ?", new RowMapperEntradas(), desdeSQL, hastaSQL);
 		 
 		return entradas;
 	}
 	
 	public Collection<Entrada> getEntradas(Proyecto proyecto, Date desde, Date hasta){
 		Timestamp desdeSQL = new Timestamp(desde.getTime());
 		Timestamp hastaSQL = new Timestamp(hasta.getTime());
 		
 		Collection<Entrada> entradas = jdbcTemplate.query(sqlEntradas + " WHERE e.al_user_id = u.u_id AND e.al_project_id = ? AND e.al_activity_id = a.a_id AND e.al_project_id = p.p_id AND e.al_date BETWEEN ? AND ?", new RowMapperEntradas(), proyecto.getIdProyecto(), desdeSQL, hastaSQL);
 		
 		return entradas;
 	}
 	
 	public int getHorasSemanales(Usuario usuario) 
 	{
 
 				LocalDate now = new LocalDate();
 		LocalDate lu = now.withDayOfWeek(DateTimeConstants.MONDAY);
 		LocalDate vie = now.withDayOfWeek(DateTimeConstants.FRIDAY);
 		
 		Date lunes = lu.toDateTimeAtStartOfDay().toDate();
 		Date viernes = vie.toDateTimeAtStartOfDay().toDate();
 	
 		return jdbcTemplate.queryForInt("SELECT HOUR(SEC_TO_TIME(SUM(TIME_TO_SEC(al_duration)))) FROM activity_log WHERE al_user_id=? and al_date BETWEEN ? AND ?",usuario.getIdUsuario(),lunes, viernes);
 
 	}
 	
 	private String parsearSistemaExterno(String sistemaExterno) {
 		String resultado = null;
 		
 		if (sistemaExterno != null) {
 			if (sistemaExterno.equals("Sistema de Incidencias de YPF")) {
 				resultado = "SIY";
 			}
 			if (sistemaExterno.equals("Sistema Geminis de YPF")) {
 				resultado = "SGY";
 			}			
 		}
 		
 		return resultado;
 	}
 	
 	
 	
 	public List<Entrada> getEntradasSemanales(Usuario usuario){
 		LocalDate now = new LocalDate();
 		LocalDate lu = now.withDayOfWeek(DateTimeConstants.MONDAY);
 		LocalDate vie = now.withDayOfWeek(DateTimeConstants.FRIDAY);
 		
 		Date lunes = lu.toDateTimeAtStartOfDay().toDate();
 		Date viernes = vie.toDateTimeAtStartOfDay().toDate();
 		
 		Timestamp desdeSQL = new Timestamp(lunes.getTime());
 		Timestamp hastaSQL = new Timestamp(viernes.getTime());
 				
 		return this.getEntradas(usuario, desdeSQL, hastaSQL); 
 	}
 	
 	public List<Entrada> getEntradasDia(Usuario usuario){
 		LocalDate hoy = new LocalDate();
 		LocalDate maniana = hoy.plusDays(1);
 		
 		Date diaHoy = hoy.toDateTimeAtStartOfDay().toDate();
 		Date diaManiana = maniana.toDateTimeAtStartOfDay().toDate();
 		
 		Timestamp desdeSQL = new Timestamp(diaHoy.getTime());
 		Timestamp hastaSQL = new Timestamp(diaManiana.getTime());
 				
 		return this.getEntradas(usuario, desdeSQL, desdeSQL); 
 	}
 	
 	public List<Entrada> getEntradasMensuales(Usuario usuario){
 		LocalDate now = new LocalDate();
 		LocalDate primeroDelMes = now.withDayOfMonth(1);
 		LocalDate ultimoDelMes = now.plusMonths(1).withDayOfMonth(1).minusDays(1);
 		
 		Date primero = primeroDelMes.toDateTimeAtStartOfDay().toDate();
 		Date ultimo = ultimoDelMes.toDateTimeAtStartOfDay().toDate();
 		
 		Timestamp desdeSQL = new Timestamp(primero.getTime());
 		Timestamp hastaSQL = new Timestamp(ultimo.getTime());
 				
 		return this.getEntradas(usuario, desdeSQL, hastaSQL); 
 	}
 	
 	
 
 		
 		public void borrarEntrada(Entrada entrada)
 		{
 			jdbcTemplate.update("DELETE FROM activity_log WHERE al_timestamp=?", entrada.getIdEntrada());
 		}
 		
 		
 		public void modificarEntrada(Entrada entrada) {
 			String sistemaExterno = this.parsearSistemaExterno(entrada.getSistemaExterno()); 
 			jdbcTemplate.update("UPDATE activity_log SET al_date=?, al_duration=?, al_project_id=?, al_activity_id=?, al_comment=?, ticket_bz=?, issue_tracker_externo=?, ite_id=? WHERE al_timestamp=?", 
 					entrada.getFecha(), new java.sql.Time((long) ((this.parsearDuracion(entrada.getDuracion())*3600000))-(3600000*21)), entrada.getProyecto().getIdProyecto(), entrada.getActividad().getIdActividad(), entrada.getNota(), entrada.getTicketBZ(), sistemaExterno, entrada.getTicketExterno(), entrada.getIdEntrada());
 		}
 
 		public Entrada buscarEntrada(long id) {
 			Timestamp fecha = new Timestamp(id);
 			return jdbcTemplate.queryForObject(sqlEntradas+" WHERE e.al_user_id = u.u_id AND e.al_project_id = p.p_id AND e.al_activity_id = a.a_id AND e.al_timestamp=?", new RowMapperEntradas(), fecha);
 		}
 		
 	}
 	
 	
 
 	
 
 	
 	class RowMapperEntradas implements RowMapper<Entrada>{
 		@Override
 		public Entrada mapRow(ResultSet rs, int rowNum) throws SQLException {
 			
 			Proyecto proyecto = new Proyecto(rs.getInt(2), rs.getString(11));
 			Actividad actividad = new Actividad(rs.getInt(3), rs.getString(14));
 			Usuario usuario = new Usuario(rs.getInt(9), rs.getString(12), rs.getString(13));
 			return new Entrada(rs.getTimestamp(1), proyecto, actividad, String.valueOf(rs.getTime(4).getTime()), rs.getString(5), rs.getInt(6), rs.getString(7), rs.getString(8), usuario, rs.getDate(10));
 		}
 		
 	}
