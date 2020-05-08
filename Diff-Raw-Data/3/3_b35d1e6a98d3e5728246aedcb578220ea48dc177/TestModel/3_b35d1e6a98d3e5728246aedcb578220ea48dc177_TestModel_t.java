 package texmex.dinner.models;
 
import burrito.annotations.Displayable;
 import burrito.links.Linkable;
 import siena.Generator;
 import siena.Id;
 import siena.Model;
 
 public class TestModel extends Model implements Linkable {
 
 	@Id(Generator.AUTO_INCREMENT)
 	private Long id;
 
	@Displayable
 	private String title;
 
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	@Override
 	public String getUrl() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public String toString() {
 		return new StringBuilder().append(title).toString();
 	}
 }
