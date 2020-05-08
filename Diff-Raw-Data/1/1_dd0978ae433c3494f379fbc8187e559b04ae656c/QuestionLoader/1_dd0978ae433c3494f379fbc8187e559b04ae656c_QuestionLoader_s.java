 package uk.ac.cam.sup.ppdloader;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import uk.ac.cam.sup.models.Data;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.queries.TagQuery;
 import uk.ac.cam.sup.util.DataType;
 
 public class QuestionLoader extends Loader<Question> {
 	private Topic topic;
 	
 	public QuestionLoader(Topic t) {
 		super (
 				"http://www.cl.cam.ac.uk/teaching/exams/pastpapers/" + t.getLink(),
 				"<a href=\"([\\w.]*.pdf)\">[\\w\\s]*</a>\\s*(</li>|(=|&#8211;)\\s*[\\s\\w]*<a href=\"([\\w:/.]*.pdf)\">)"
 		);
 		this.topic = t;
 	}
 
 	@Override
 	protected Question parseGroups(String[] groups) {
 		Question q = new Question(new User("bot1000"));
 		
 		q.setContent(new Data(DataType.PLAIN_TEXT, "http://www.cl.cam.ac.uk/teaching/exams/pastpapers/"+groups[1]));
 		q.addTag(TagQuery.get(topic.getName()));
 		q.setTimeStamp(parseDate(groups[1]));
 		q.setExpectedDuration(30);
 		
 		if (groups[4] != null) {
 			q.setNotes(new Data(DataType.PLAIN_TEXT, groups[4]));
 		}
 		
 		return q;
 	}
 	
 	private Date parseDate(String filename) {
 		GregorianCalendar c = new GregorianCalendar();
 		c.set(Integer.parseInt(filename.substring(1, 5)),Calendar.JUNE,1,13,0,0);
 		return c.getTime();
 	}
 	
 }
