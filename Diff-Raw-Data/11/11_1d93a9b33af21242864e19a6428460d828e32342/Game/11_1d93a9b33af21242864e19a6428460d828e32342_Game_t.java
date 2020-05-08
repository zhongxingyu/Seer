 package rose.BoucherMercer.drunkr;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Game implements Serializable {
 
 	private static final long serialVersionUID = -6262540717172076660L;
 	private long id;
 	private String name;
 	private String instructions;
 	private String[] props;
 	
	public Game(){
		props = new String[0];
	}
	
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public void setName(String name) {
 		if(name != null)
 			this.name = name;
 		else
 			this.name = "";
 	}
 	public void setInstructions(String instructions) {
 		if(instructions != null)
 			this.instructions = instructions;
 		else
 			this.instructions = "";
 	}
 	public void setProps(List<String> props) {
 		if(props != null)
 			this.props = props.toArray(new String[] {});
 		else
 			this.props = new String[] {};
 	}
 	public String getName() {
 		return name;
 	}
 	public String getInstructions() {
 		return instructions;
 	}
 	public List<String> getProps() {
 		List<String> ls =  new ArrayList<String>();
 		for(int i = 0; i < props.length; i++)
 			ls.add(props[i]);
 		return ls;
 	}
 	
 }
