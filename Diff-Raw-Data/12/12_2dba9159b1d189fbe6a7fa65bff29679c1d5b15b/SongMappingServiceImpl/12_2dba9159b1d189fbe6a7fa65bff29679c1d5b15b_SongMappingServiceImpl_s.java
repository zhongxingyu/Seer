 package de.lmu.ios.geomelody.service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.springframework.jdbc.core.ResultSetExtractor;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.lmu.ios.geomelody.dom.Filters;
 import de.lmu.ios.geomelody.dom.Location;
 import de.lmu.ios.geomelody.dom.Song;
 import de.lmu.ios.geomelody.mapper.SongMapper;
 
 public class SongMappingServiceImpl implements SongMappingService {
 	private NamedParameterJdbcTemplate jdbcTemplate;
 
 	private static Map<String, Object> emptyMap = Collections
 			.unmodifiableMap(new LinkedHashMap<String, Object>());
 
 	public void setDataSource(final DataSource dataSource) {
 		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
 	}
 
 	@Override
 	@Transactional
 	public void saveSong(final Song song) {
 		final Map<String, Integer> tagIdMap = jdbcTemplate.query(
 				"SELECT id, name FROM tags", emptyMap,
 				new ResultSetExtractor<Map<String, Integer>>() {
 					public Map<String, Integer> extractData(ResultSet rs)
 							throws SQLException {
 						Map<String, Integer> map = new LinkedHashMap<String, Integer>(30);
 						while (rs.next()) {
 							int col1 = rs.getInt("id");
 							String col2 = rs.getString("name");
 							map.put(col2, col1);
 						}
 						return map;
 					};
 				});
 
 		Map<String, Object> params = new HashMap<String, Object>(5);
 		params.put("soundcloud_song_id", song.getSoundCloudSongId());
 		params.put("soundcloud_user_id", song.getSoundCloudUserId());
 		params.put("comment", song.getComment());
 		params.put("longitude", song.getLocation().getLongitude());
 		params.put("latitude", song.getLocation().getLatitude());
 
 		jdbcTemplate
 				.update("INSERT INTO songs (soundcloud_song_id, soundcloud_user_id, comment, geom) "
 						+ "VALUES (:soundcloud_song_id, :soundcloud_user_id, :comment, "
 						+ "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))",
 						params);
 
 		final long songId = jdbcTemplate.queryForLong(
 				"SELECT CURRVAL('songs_id_seq')", emptyMap);
 		
 		final List<String> tags = song.getTags();
 
 		for(String tag : tags) {
 			Integer tagId;
 			if((tagId = tagIdMap.get(tag)) != null) {
 				Map<String, Object> insertMap = new HashMap<String, Object>(2);
 				insertMap.put("song_id", songId);
 				insertMap.put("tag_id", tagId);
 				jdbcTemplate.update(
 						"INSERT INTO songs_tags (song_id, tag_id) VALUES (:song_id, :tag_id)", insertMap);
 			}
 		}
 	}
 
 	@Override
 	public List<Song> getkNearestSongs(final Location location, final Filters filters, final int k) {
		Map<String, Object> params = new HashMap<String, Object>(4);
		params.put("longitude", location.getLongitude());
		params.put("latitude", location.getLatitude());
		params.put("k", k);
		params.put("filters", filters != null ? filters.getFilters() : null);
 
 		String query = "SELECT s.id, s.soundcloud_song_id, s.soundcloud_user_id, s.comment, " + 
 				"	array_to_string(ARRAY(SELECT t.name " + 
 				"	    FROM tags AS T " + 
 				"	    INNER JOIN songs_tags AS st ON t.id = st.tag_id " + 
 				"	    WHERE " + 
 				"		st.song_id = s.id), ',') as tags, " + 
 				"		ST_X(geom) as longitude, " + 
 				"		ST_Y(geom) as latitude " + 
 				"	FROM songs AS s " + 
 				"		INNER JOIN songs_tags AS st ON s.id = st.song_id " + 
 				"		INNER JOIN tags AS t ON t.id = st.tag_id " + 
 				"	WHERE " + 
 				"		(:filters) IS NULL OR t.name IN (:filters) " + 
 				"	GROUP BY " + 
 				"		s.id " + 
 				"	ORDER BY " + 
 				"		s.geom <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) " + 
 				"	LIMIT :k";
 		
 		return jdbcTemplate
 				.query(query, params, new SongMapper());
 	}
 }
