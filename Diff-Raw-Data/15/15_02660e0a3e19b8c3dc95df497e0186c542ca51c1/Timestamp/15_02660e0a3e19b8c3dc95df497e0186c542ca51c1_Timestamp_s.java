 package dk.exenova.ant;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 
 public class Timestamp extends Task {
 	private String property;
 	@Override
 	public void execute() throws BuildException {
 		getProject().setNewProperty(property, getTimeStamp());
 	}
 
 	private String getTimeStamp(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMTdd-HHmm");
		return simpleDateFormat.format(new Date());
 	}
 
 	public void setProperty(String property) {
 		this.property = property;
 	}
 }
