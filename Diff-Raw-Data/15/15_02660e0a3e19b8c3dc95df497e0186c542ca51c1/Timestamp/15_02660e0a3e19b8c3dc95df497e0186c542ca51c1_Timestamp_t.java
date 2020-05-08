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
		SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat simpleDateFormatHours = new SimpleDateFormat("HHmm");
		return simpleDateFormatDate.format(new Date())+"T"+simpleDateFormatHours.format(new Date());
 	}
 
 	public void setProperty(String property) {
 		this.property = property;
 	}
 }
