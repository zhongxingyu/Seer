 package de.furryhome.e621api;
 
 import java.lang.reflect.Field;
 
 public final class ListItem {
 	@SuppressWarnings("unused")
 	private int
 		id, parent_id, creator_id, height, width, sample_height, sample_width, preview_height, preview_width, change, score;
 	@SuppressWarnings("unused")
 	private String
 		md5, file_url, sample_url, preview_url, status, source, rating, tags, created_at, author;
 	@SuppressWarnings("unused")
 	private boolean
 		has_children, has_notes, has_comments;
 	@SuppressWarnings("unused")
 	private boolean
 		md5_set = false, id_set = false, parent_id_set = false, creator_id_set = false, height_set = false, width_set = false,
 		file_url_set = false, sample_height_set = false, sample_width_set = false, sample_url_set = false, preview_height_set = false,
 		preview_width_set = false, preview_url_set = false, has_children_set = false, has_notes_set = false, has_comments_set = false,
 		status_set = false, source_set = false, rating_set = false, tags_set = false, created_at_set = false, author_set = false, change_set = false,
 		score_set = false;
 
 	private void setFieldValue(String fieldName, Object value) throws IllegalAccessException {
 		try {
 			Field field = getClass().getDeclaredField(fieldName);
 			Field field_check = getClass().getDeclaredField(fieldName + "_set");
 			
 			if (!field_check.getBoolean(this)) {
 				try {
 					field.set(this, value);
 					field_check.setBoolean(this, true);
 				} catch (IllegalArgumentException | IllegalAccessException e) {}
 			} else {
 				throw new IllegalAccessException(fieldName + " may only be initialized once");
 			}
 		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException e) {}
 	}
 	private Object getFieldValue(String fieldName) throws IllegalAccessException {
 		try {
 			Field field_check = getClass().getDeclaredField(fieldName + "_set");
 			if (field_check.getBoolean(this)) {
 				try {
 					Field field = getClass().getDeclaredField(fieldName);
 					return field.get(this);
 				} catch (IllegalArgumentException | IllegalAccessException e) {}
 			} else {
 				throw new IllegalAccessException(fieldName + " is not initialized yet");
 			}
 		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException e) {}
		return false;
 	}
 		
 	protected final void setMd5(String md5) throws IllegalAccessException {	this.setFieldValue("md5", md5); }
 	protected final void setId(int id) throws IllegalAccessException { this.setFieldValue("id", id); }
 	protected final void setParent_id(int parent_id) throws IllegalAccessException { this.setFieldValue("parent_id", parent_id); }
 	protected final void setCreator_id(int creator_id) throws IllegalAccessException { this.setFieldValue("creator_id", creator_id); }
 	protected final void setFile_url(String file_url) throws IllegalAccessException { this.setFieldValue("file_url", file_url); }
 	protected final void setSample_url(String sample_url) throws IllegalAccessException { this.setFieldValue("sample_url", sample_url); }
 	protected final void setPreview_url(String preview_url) throws IllegalAccessException { this.setFieldValue("preview_url", preview_url); }
 	protected final void setStatus(String status) throws IllegalAccessException { this.setFieldValue("status", status); }
 	protected final void setSource(String source) throws IllegalAccessException { this.setFieldValue("source", source); }
 	protected final void setRating(String rating) throws IllegalAccessException { this.setFieldValue("rating", rating); }
 	protected final void setTags(String tags) throws IllegalAccessException { this.setFieldValue("tags", tags); }
 	protected final void setCreated_at(String created_at) throws IllegalAccessException { this.setFieldValue("created_at", created_at); }
 	protected final void setAuthor(String author) throws IllegalAccessException { this.setFieldValue("author", author); }
 	protected final void setChange(int change) throws IllegalAccessException { this.setFieldValue("change", change); }
 	protected final void setScore(int score) throws IllegalAccessException { this.setFieldValue("score", score); }
 	protected final void setHeight(int height) throws IllegalAccessException { this.setFieldValue("height", height); }
 	protected final void setWidth(int width) throws IllegalAccessException { this.setFieldValue("width", width); }
 	protected final void setSample_height(int sample_height) throws IllegalAccessException { this.setFieldValue("sample_height", sample_height); }
 	protected final void setSample_width(int sample_width) throws IllegalAccessException { this.setFieldValue("sample_width", sample_width); }
 	protected final void setPreview_height(int preview_height) throws IllegalAccessException { this.setFieldValue("preview_height", preview_height); }
 	protected final void setPreview_width(int preview_width) throws IllegalAccessException { this.setFieldValue("preview_width", preview_width); }
 	protected final void setHas_children(boolean has_children) throws IllegalAccessException { this.setFieldValue("has_children", has_children); }
 	protected final void setHas_notes(boolean has_notes) throws IllegalAccessException { this.setFieldValue("has_notes", has_notes); }
 	protected final void setHas_comments(boolean has_comments) throws IllegalAccessException { this.setFieldValue("has_comments", has_comments); }
 	
 	public final String getMd5() throws IllegalAccessException { return (String) this.getFieldValue("md5"); }
 	public final int getId() throws IllegalAccessException { return (int) this.getFieldValue("id"); }
 	public final int getParent_id() throws IllegalAccessException { return (int) this.getFieldValue("parent_id"); }
 	public final int getCreator_id() throws IllegalAccessException { return (int) this.getFieldValue("creator_id"); }
 	public final String getFile_url() throws IllegalAccessException { return (String) this.getFieldValue("file_url"); }
 	public final String getSample_url() throws IllegalAccessException { return (String) this.getFieldValue("sample_url"); }
 	public final String getPreview_url() throws IllegalAccessException { return (String) this.getFieldValue("preview_url"); }
 	public final String getStatus() throws IllegalAccessException { return (String) this.getFieldValue("status"); }
 	public final String getSource() throws IllegalAccessException { return (String) this.getFieldValue("source"); }
 	public final String getRating() throws IllegalAccessException { return (String) this.getFieldValue("rating"); }
 	public final String getTags() throws IllegalAccessException { return (String) this.getFieldValue("tags"); }
 	public final String getCreated_at() throws IllegalAccessException { return (String) this.getFieldValue("created_at"); }
 	public final String getAuthor() throws IllegalAccessException { return (String) this.getFieldValue("author"); }
 	public final int getChange() throws IllegalAccessException { return (int)this.getFieldValue("change"); }
 	public final int getScore() throws IllegalAccessException { return (int)this.getFieldValue("score"); }
 	public final int getHeight() throws IllegalAccessException { return (int)this.getFieldValue("height"); }
 	public final int getWidth() throws IllegalAccessException { return (int)this.getFieldValue("width"); }
 	public final int getSample_height() throws IllegalAccessException { return (int)this.getFieldValue("sample_height"); }
 	public final int getSample_width() throws IllegalAccessException { return (int)this.getFieldValue("sample_width"); }
 	public final int getPreview_height() throws IllegalAccessException { return (int)this.getFieldValue("preview_height"); }
 	public final int getPreview_width() throws IllegalAccessException { return (int)this.getFieldValue("preview_width"); }
 	public final boolean isHas_children() throws IllegalAccessException { return (boolean) this.getFieldValue("has_childen"); }
 	public final boolean isHas_notes() throws IllegalAccessException { return (boolean) this.getFieldValue("has_notes"); }
 	public final boolean isHas_comments() throws IllegalAccessException { return (boolean) this.getFieldValue("has_comments"); }
 }
