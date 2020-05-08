 package se.atrosys.birds.model;
 
 import javax.annotation.Generated;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * TODO write comment
  */
 @Entity
 @Table(name = "REGION")
 public class RegionModel {
 	@Id
 	private String name;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RegionModel that = (RegionModel) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
 }
