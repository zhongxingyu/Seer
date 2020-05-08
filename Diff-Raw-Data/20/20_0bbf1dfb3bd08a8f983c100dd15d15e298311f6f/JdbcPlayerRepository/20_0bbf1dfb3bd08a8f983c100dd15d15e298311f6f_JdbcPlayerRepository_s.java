 package ie.cit.clouddev.domain.dao;
 
 
 import ie.cit.clouddev.domain.Player;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.security.core.context.SecurityContextHolder;
 

 public class JdbcPlayerRepository implements PlayersRepository {
 
 	private JdbcTemplate jdbcTemplate;
 	private PlayerMapper playerMapper = new PlayerMapper();
 
 	public JdbcPlayerRepository(DataSource dataSource) {
 		jdbcTemplate = new JdbcTemplate(dataSource);
 	}
 
 	
 	
 	@Override
 	public Player findPlayerId(String playerId) {
 		return jdbcTemplate.queryForObject(
 				"SELECT PLAYERID, NAME, FITTOPLAY FROM PLAYERS WHERE PLAYERID=?", playerMapper, playerId);
 	}
 
 	@Override
 	public List<Player> getAllP() {
 		return jdbcTemplate.query(
 				"SELECT PLAYERID, NAME, FITTOPLAY FROM PLAYERS WHERE MANAGER=?", playerMapper,
 				getCurrentUser());
 	}
 
 	@Override
 	public void add(Player player) {
		jdbcTemplate.update("INSERT INTO TODO VALUES(?,?,?,?)", player.getPlayerId(),
			player.getName(), getCurrentUser(), player.isfittoplay());
 	}
 
 	private String getCurrentUser() {
 		return SecurityContextHolder.getContext().getAuthentication().getName();
 	}
 
 	@Override
 	public void delete(String playerId) {
 		jdbcTemplate.update("DELETE FROM PLAYERS WHERE PLAYERID=?", playerId);
 	}
 
 	@Override
 	public void update(Player player) {
		jdbcTemplate.update("UPDATE PLAYER SET NAME=?, FITTOPLAY=? WHERE PLAYERID=?",
 				player.getName(), player.isfittoplay(), player.getPlayerId());
 	}
 
 	
 	
 }
 
 class PlayerMapper implements RowMapper<Player> {
 	//@Override
 	public Player mapRow(ResultSet rs, int rowNum) throws SQLException {
 		Player player = new Player();
 		player.setPlayerId(rs.getString("PLAYERID"));
 		player.setName(rs.getString("NAME"));
 		player.setFittoplay(rs.getBoolean("FITTOPLAY"));
 		return player;
 	}
 }
