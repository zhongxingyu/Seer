 package carnero.me.data;
 
 import carnero.me.model.Education;
 import carnero.me.model.EntryIntent;
 import carnero.me.model.Position;
 
 @SuppressWarnings("UnusedDeclaration")
 public class EducationTUL extends Education {
 
 	public EducationTUL() {
 		year = 2006;
 		month = 8;
 		name = "Technical University of Liberec";
		description = "EIŘS,\u00A0Bachelor's\u00A0degree";
 		tapAction = EntryIntent.getWebIntent("http://www.fm.tul.cz/en");
 	}
 }
