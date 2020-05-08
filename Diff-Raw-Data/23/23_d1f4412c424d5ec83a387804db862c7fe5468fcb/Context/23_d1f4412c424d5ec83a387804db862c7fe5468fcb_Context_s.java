 package de.congrace.model;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import de.congrace.jaxb.ContextAdapter;
 
 /**
  * Immutable Data transfer object
  * @author fasseg
  *
  */
 @XmlRootElement(name="context")
 public class Context {
 	@XmlElement
 	private final Identifier id;
 	@XmlElement
 	private final String name;
 	
 	//JAXB needs this private constructor on root elements
 	private Context(){
 		this.id=null;
 		this.name=null;
 	}
 	
 	private Context(Builder b) {
 		this.id=new Identifier(new String(b.id.getValue()));
 		this.name=new String(b.name);
 	}
 	
 	public Identifier getId() {
 		return id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Context other = (Context) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	public static class Builder {
 		private Identifier id;
 		private final String name;
 		
 		public Builder(String name) {
 			super();
 			this.name = name;
 		}
 		
		public static Builder fromContext(Context ctx){
			Builder b=new Builder(new String(ctx.getName()));
			b.id=ctx.getId();
			return b;
 		}
 		
 		public Builder id(Identifier id){
 			this.id=id;
 			return this;
 		}
 		
 		public Context build(){
 			return new Context(this);
 		}
 	}
 }
