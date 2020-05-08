 package edu.uib.info323.dao;
 
 import java.awt.Color;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.jdbc.core.namedparam.SqlParameterSource;
 import org.springframework.stereotype.Component;
 
 import edu.uib.info323.image.CompressedColorFactory;
 import edu.uib.info323.model.Image;
 import edu.uib.info323.model.ImageFactory;
 
 @Component
 public class ImageDaoMySql implements ImageDao{
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ImageDaoMySql.class);
 	@Autowired
 	private CompressedColorFactory colorFactory;
 	@Autowired
 	private ImageFactory imageFactory;
 
 	private NamedParameterJdbcTemplate jdbcTemplate;
 
 	public void insert(final List<Image> images) {
 		String sql = "INSERT INTO image (image_uri) VALUES (:image_uri) ON DUPLICATE KEY UPDATE image_uri = image_uri";
 		SqlParameterSource[] parameterSource = this.getSqlParameterSource(images);
 		jdbcTemplate.batchUpdate(sql, parameterSource);
 		
 		sql = "INSERT INTO image_page (image_uri,page_uri) VALUES (:image_uri,:page_uri) ON DUPLICATE KEY UPDATE image_uri = image_uri";
 		jdbcTemplate.batchUpdate(sql, parameterSource);
 
 	}
 
 	public void delete(Image image) {
 		String sql = "DELETE FROM image WHERE image_uri = :image_uri";
 		jdbcTemplate.update(sql, this.getMapSqlParameterSource(image));
 
 	}
 
 	public List<Image> getAllImages() {
 
 		String sql = "SELECT image_uri FROM image_page";
 		List<Image> images = this.jdbcTemplate.query(sql, new MapSqlParameterSource() ,new RowMapper<Image>() {
 
 			public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return imageFactory.createImage(rs.getString("image_uri"), rs.getString("image_uri"));
 			}
 		});
 
 		return this.removeDuplicates(images);
 	}
 
 	public Image getImageByImageUri(String imageUri) {
 		String sql = "SELECT page_uri " +
 				"FROM image_page " +
 				"WHERE image_uri = :image_uri";
 		List<String> pageUris = jdbcTemplate.queryForList(sql, new MapSqlParameterSource("image_uri", imageUri), String.class);
 
 		return imageFactory.createImage(imageUri, pageUris);
 	}
 
 
 
 	public List<Image> getImagesWithColor(String color, int startIndex, int endIndex) {
 		Color decodedColor = Color.decode(color);
 		LOGGER.debug("Retrieving images with color " + color);
 		int colorValue = colorFactory.createCompressedColor(decodedColor.getRed(), decodedColor.getGreen(), decodedColor.getBlue()).getColor();
 		String sql = "SELECT image_page.image_uri, image_page.page_uri " +
 				"FROM image_page, color, image " +
 				"WHERE image_page.image_uri = color.image_uri " +
 				"AND color = :color AND image.image_uri = color.image_uri  AND image.width > 50 AND image.height > 50 " +
 				"ORDER BY color.relative_freq DESC " + 
 				"LIMIT  " + startIndex + ", " + endIndex;
 		
 		MapSqlParameterSource parameterSource = new MapSqlParameterSource("color", colorValue);
 		long startTime = System.currentTimeMillis();
 		List<Image> imagesWithDuplicates = jdbcTemplate.query(sql,parameterSource, new RowMapper<Image>() {
 
 			public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return imageFactory.createImage(rs.getString("image_uri"), rs.getString("page_uri"));
 			}
 
 		});
 		long endTime = System.currentTimeMillis();
 		LOGGER.debug("Query time: " + (( endTime - startTime) / 1000.0));
 		List<Image> images = this.removeDuplicates(imagesWithDuplicates);
 
 
 		return images; 
 	}
 
 	private List<Image> removeDuplicates(List<Image> imagesWithDuplicates) {
 		Map<String,Image> imageMap = new HashMap<String, Image>();
 		for(Image image : imagesWithDuplicates) {
 			if(imageMap.containsKey(image.getImageUri())) {
 				imageMap.get(image.getImageUri()).addPageUri(image.getPageUris());
 			}
 			else {
 				imageMap.put(image.getImageUri(), image);
 			}
 
 		}
 		return new ArrayList<Image>(imageMap.values());
 	}
 
 	private MapSqlParameterSource getMapSqlParameterSource(Image image) {
 		MapSqlParameterSource parameterSource = new MapSqlParameterSource();
 		parameterSource.addValue("image_uri", image.getImageUri());
		parameterSource.addValue("page_uri", image.getPageUris());
 		parameterSource.addValue("date_analyzed", new Date(System.currentTimeMillis()));
 		parameterSource.addValue("height", image.getHeight());
 		parameterSource.addValue("width", image.getHeight());
 		return parameterSource; 
 	}
 
 	private SqlParameterSource[] getSqlParameterSource(List<Image> images) {
 		List<SqlParameterSource> parameters = new ArrayList<SqlParameterSource>();
 		for(Image image : images) {
 			parameters.add(this.getMapSqlParameterSource(image));
 		}
 		return parameters.toArray(new SqlParameterSource[0]);
 	}
 
 	public List<Image> getUnprocessedImages() {
 		String sql = "SELECT * FROM image WHERE date_analyzed IS NULL LIMIT 0, 100";
 
 		return 	jdbcTemplate.query(sql,new MapSqlParameterSource(), new RowMapper<Image>() {
 
 			public Image mapRow(ResultSet rs, int rowNum) throws SQLException { 
 				return imageFactory.createImage(rs.getString("image_uri"));
 			}
 		});
 	}
 
 	public void insert(Image image) {
 		String sql = "INSERT INTO image (image_uri) VALUES (:image_uri) ON DUPLICATE KEY UPDATE image_uri = image_uri";
 		MapSqlParameterSource mapSqlParameterSource = this.getMapSqlParameterSource(image);
 		jdbcTemplate.update(sql, mapSqlParameterSource);
 
 		sql = "INSERT INTO image_page (image_uri,page_uri) VALUES (:image_uri,:page_uri) ON DUPLICATE KEY UPDATE image_uri = image_uri";
 		jdbcTemplate.update(sql, mapSqlParameterSource);
 
 		LOGGER.debug("Inserted " + image + " into database");
 
 	}
 
 	@Autowired
 	public void setDataSource(DataSource dataSource) {
 		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
 	}
 
 
 	public void updateAnalysedDate(final List<Image> images) {
 		String sql = "UPDATE image SET date_analyzed = :date_analyzed WHERE image_uri = :image_uri";
 		jdbcTemplate.batchUpdate(sql, this.getSqlParameterSource(images));
 	}
 
 	public void update(List<Image> images) {
 		String sql = "UPDATE image SET height = :height, width = :width , date_analyzed = :date_analyzed WHERE image_uri = :image_uri";
 		jdbcTemplate.batchUpdate(sql, this.getSqlParameterSource(images));
 	}
 }
