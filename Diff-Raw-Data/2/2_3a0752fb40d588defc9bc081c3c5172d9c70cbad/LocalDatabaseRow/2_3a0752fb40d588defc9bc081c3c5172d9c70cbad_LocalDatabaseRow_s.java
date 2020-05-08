 package edu.helsinki.sulka.models;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 @Entity
@Table(name="Recoveries")
 @JsonIgnoreProperties(ignoreUnknown=false)
 public class LocalDatabaseRow implements Serializable {
 	public LocalDatabaseRow() {}
 	
 	public LocalDatabaseRow(RowType rowType) {
 		this.rowType = rowType;
 	}
 	
 	public enum RowType {
 		RINGING,
 		RECOVERY
 	};
 	
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "id")
 	@JsonProperty("id")
 	private Long id;
 	
 	@JsonProperty("userId")
 	@Column(name = "userId")
 	private String userId;
 	
 	@JsonProperty("rowType")
 	@Column(name = "rowType")
 	private RowType rowType;
 	
 	@JsonProperty("row")
 	@Column(name = "rowJSON")
 	private String row;
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public String getUserId() {
 		return userId;
 	}
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 	public RowType getRowType() {
 		return rowType;
 	}
 	public void setRowType(RowType rowType) {
 		this.rowType = rowType;
 	}
 	public String getRow() {
 		return row;
 	}
 	public void setRow(String row) {
 		this.row = row;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof LocalDatabaseRow && obj.getClass() == this.getClass()) {
 			return id == ((LocalDatabaseRow)obj).id;
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		if (id == null) return 0;
 		return (int) (long) id;
 	}
 }
 
