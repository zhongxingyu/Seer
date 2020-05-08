 package nl.kennisnet.arena.client.domain;
 
 import java.io.Serializable;
 
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class RoundDTO implements Serializable{
 
 	private static final long serialVersionUID = 1L;
 
 	private Integer id;
 		
 	private String name;
 	
 	public RoundDTO(String name){
 		this.name = name;
 	}
 
 	public RoundDTO(Integer id, String name){
 		this.id = id;
 		this.name = name;
 	}
 	
 	public RoundDTO() {
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(obj instanceof RoundDTO){
 			RoundDTO rdDto = (RoundDTO)obj;
 			if(this.name.equals(rdDto.getName())){
 				return true;
 			}
 		}
 		return super.equals(obj);
 	}
 	
 	@Override
 	public int hashCode() {
		return new HashCodeBuilder().append(this.getName()).toHashCode();
 	}
 	
 }
