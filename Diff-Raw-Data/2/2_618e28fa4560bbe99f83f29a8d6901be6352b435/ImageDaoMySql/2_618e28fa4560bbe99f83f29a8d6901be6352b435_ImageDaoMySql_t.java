 package edu.uib.info323.dao;
 
 import java.awt.Color;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
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
 
 import edu.uib.info323.image.CompressedColor;
 import edu.uib.info323.image.CompressedColorFactory;
 import edu.uib.info323.model.Image;
 import edu.uib.info323.model.ImageFactory;
 
 @Component
 public class ImageDaoMySql implements ImageDao{
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ImageDaoMySql.class);
 	@Autowired
 	private CompressedColorFactory colorFactory;
 	private int defaultImageReturnThreshold = 40;
 
 	private int defaultIndexEnd = 100;
 	private int defaultIndexStart = 0;
 	@Autowired
 	private ImageFactory imageFactory;
 
 	private NamedParameterJdbcTemplate jdbcTemplate;
 
 	public void delete(Image image) {
 		List<Image> images = new LinkedList<Image>();
 		this.delete(images);
 	}
 
 	public void delete(List<Image> images) {
 		String sql = "DELETE FROM image WHERE id = :id";
 		SqlParameterSource[] parameterSource = this.getSqlParameterSource(images);
 		int[] deleted = jdbcTemplate.batchUpdate(sql, parameterSource);
 		int imagesDeleted = 0;
 		for(Integer i : deleted) {
 			imagesDeleted += i;
 		}
 		sql = "DELETE FROM image_page WHERE image = :image";
 		deleted = jdbcTemplate.batchUpdate(sql, parameterSource);
 		int pagesDeleted = 0;
 		for(Integer i : deleted) {
 			pagesDeleted += i;
 		}
 		LOGGER.debug(imagesDeleted + " images and " + pagesDeleted + " pages deleted");
 		
 	}
 
 	public List<Image> getAllImages() {
 
 		String sql = "SELECT i.image_uri, ip.image_page FROM image_page AS ip INNER JOIN image AS i ON (ip.image = i.id)";
 		List<Image> images = this.jdbcTemplate.query(sql, new MapSqlParameterSource() ,new RowMapper<Image>() {
 
 			public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return imageFactory.createImage(rs.getString("image_uri"), rs.getString("image_page"));
 			}
 		});
 
 		return this.removeDuplicates(images);
 	}
 
 	public Image getImageByImageUri(String imageUri) {
 		String sql = "SELECT page_uri " +
 					 "FROM image_page " +
 					 "WHERE image = :image";
 		List<String> pageUris = jdbcTemplate.queryForList(sql, new MapSqlParameterSource("image_uri", imageUri), String.class);
 
 		return imageFactory.createImage(imageUri, pageUris);
 	}
 
 
 
 	public List<Image> getImagesWithColor(List<String> colorList, List<Integer> freqList, int startIndex, int endIndex) {
 		StringBuilder sql = new StringBuilder("SELECT i.image_uri, ip.page_uri, i.height, i.width " +
 				"FROM image_page AS ip INNER JOIN image AS i ON ip.image = i.id " +
 				"WHERE ip.image IN ( SELECT a.image FROM ( ");
 		MapSqlParameterSource parameterSource = new MapSqlParameterSource();
 		parameterSource.addValue("start_index", startIndex);
 		parameterSource.addValue("end_index", endIndex);
 		for(int i = 0; i < colorList.size(); i++) {
 			if(i>0) {
 				sql.append(" INNER JOIN ");
 			}
 			sql.append("(SELECT image FROM color WHERE color = :color" + i + " AND relative_freq >= :relative_freq_low" +  i +" AND relative_freq <= :relative_freq_high" +  i +") AS "+ Character.toChars((i+97))[0]);
 			if( i > 0 ) {
 				sql.append(" USING (image) ");
 			}
 			CompressedColor color = colorFactory.createCompressedColor(Color.decode(colorList.get(i)));
 			parameterSource.addValue("color"+i, color.getColor());
 			if(freqList.size() == 1){
 				parameterSource.addValue("relative_freq_low"  + i, 20);
 				parameterSource.addValue("relative_freq_high" + i, 100);
 			}else{
 			parameterSource.addValue("relative_freq_low"  + i, freqList.get(i) - 15);
 			parameterSource.addValue("relative_freq_high" + i, freqList.get(i) + 15);
 			}
 		}
 		sql.append(" )) LIMIT :start_index, :end_index ");
 		LOGGER.debug("SQL query to run: " + sql.toString());
 		long startTime = System.currentTimeMillis();
 		List<Image> imagesWithDuplicates = jdbcTemplate.query(sql.toString(),parameterSource, new RowMapper<Image>() {
 
 			public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
 				return imageFactory.createImage(rs.getString("image_uri"), rs.getString("page_uri"),rs.getInt("height"),rs.getInt("width"));
 			}
 
 		});
 		long endTime = System.currentTimeMillis();
 		LOGGER.debug("Query time: " + (( endTime - startTime) / 1000.0));
 		List<Image> images = this.removeDuplicates(imagesWithDuplicates);
 		return images;
 	}
 
 	public List<Image> getImagesWithColor(String color) {
 		return this.getImagesWithColor(color, this.defaultImageReturnThreshold);
 	}
 
 	public List<Image> getImagesWithColor(String color, int relativeFreq) {
 		return this.getImagesWithColor(color, relativeFreq, this.defaultIndexStart, this.defaultIndexEnd);
 	}
 
 	public List<Image> getImagesWithColor(String color, int startIndex, int endIndex) {
 		return this.getImagesWithColor(color, defaultImageReturnThreshold, startIndex, endIndex);
 	}
 
 	public List<Image> getImagesWithColor(String color, int relativeFreq, int startIndex, int endIndex) {
 		List<String> colorList = new LinkedList<String>();
 		colorList.add(color);
 		List<Integer> freqList = new LinkedList<Integer>();
 		freqList.add(relativeFreq);
 		return this.getImagesWithColor(colorList,freqList, startIndex, endIndex); 
 	}
 
 	private MapSqlParameterSource getMapSqlParameterSource(Image image) {
 		MapSqlParameterSource parameterSource = new MapSqlParameterSource();
 		parameterSource.addValue("image_uri", image.getImageUri());
 		parameterSource.addValue("id", image.getImageUri().hashCode());
 		parameterSource.addValue("image", image.getImageUri().hashCode());
 		if(image.getPageUris().size() > 0) {
 			parameterSource.addValue("page_uri", image.getPageUris().get(0));
 			parameterSource.addValue("page", image.getPageUris().get(0).hashCode());
 		}
 		parameterSource.addValue("date_analyzed", new Date(System.currentTimeMillis()));
 
 		parameterSource.addValue("height", image.getHeight());
		parameterSource.addValue("width", image.getWidth());
 
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
 				return imageFactory.createImage(rs.getString("image_uri"), rs.getInt("id"));
 			}
 		});
 	}
 
 	public void insert(Image image) {
 		List<Image> imageList =  new ArrayList<Image>(1);
 		imageList.add(image);
 		this.insert(imageList);
 	}
 
 	public void insert(final List<Image> images) {
 		long insertStart = System.currentTimeMillis();
 		String sql = "INSERT INTO image (id, image_uri) VALUES (:id, :image_uri) ON DUPLICATE KEY UPDATE id = id";
 		SqlParameterSource[] parameterSource = this.getSqlParameterSource(images);
 		jdbcTemplate.batchUpdate(sql, parameterSource);
 		long insertEnd = System.currentTimeMillis();
 		double imageInsertTime = (insertEnd - insertStart) / 1000.0;
 
 		insertStart = System.currentTimeMillis();
 		sql = "INSERT INTO image_page (image, page, page_uri) VALUES (:image, :page, :page_uri) ON DUPLICATE KEY UPDATE image = image";
 		jdbcTemplate.batchUpdate(sql, parameterSource);
 		insertEnd = System.currentTimeMillis();
 		double imagePageInsertTime = (insertEnd - insertStart) / 1000.0;
 		LOGGER.debug("Time to insert images: " + imageInsertTime + ", time to insert image_pages: " + imagePageInsertTime);
 
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
 
 	@Autowired
 	public void setDataSource(DataSource dataSource) {
 		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
 	}
 
 	public void update(List<Image> images) {
 		String sql = "UPDATE image SET height = :height, width = :width , date_analyzed = :date_analyzed WHERE id = :id";
 		jdbcTemplate.batchUpdate(sql, this.getSqlParameterSource(images));
 	}
 
 	public void updateAnalysedDate(final List<Image> images) {
 		String sql = "UPDATE image SET date_analyzed = :date_analyzed WHERE id = :id";
 		jdbcTemplate.batchUpdate(sql, this.getSqlParameterSource(images));
 	}
 }
