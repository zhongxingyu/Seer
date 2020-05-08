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
 
 @JsonIgnoreProperties(ignoreUnknown=false)
 @Entity
@Table(name="Ringings_TMP")
 public class RingingDatabaseRow implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	@JsonProperty("id")
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "id")
 	private Long id;
 	@JsonProperty("userId")
 	@Column(name = "userId")
 	private String userId;
 	@JsonProperty("row")
 	@Column(name = "rowJSON")
 	private String row;
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}	
 	public String getUserId() {
 		return userId;
 	}
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 	public String getRow() {
 		return row;
 	}
 	public void setRow(String row) {
 		this.row = row;
 	}
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 	
 	@Override
 	public boolean equals(Object obj){
 		if(obj instanceof RingingDatabaseRow)
 			return id == ((RingingDatabaseRow)obj).id;
 		return false;
 	}
 }
