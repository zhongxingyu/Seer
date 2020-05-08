 package preprocessors;
 
 import general.Email;
 
 /**
 * removes punctuations and any non alphanumeric characters from an email and
 * converts the email into words separated with single white space
  * 
  * @author ahmedkotb
  * 
  */
 public class WordsCleaner implements Preprocessor {
 
 	private String splitRegex = "\\W+";
 
 	@Override
 	public void apply(Email email) {
		// TODO Abbas please review
 		try {
 			// splits the email on any non alpha numeric characters
 
 			// process the subject
 			String[] words = email.getSubject().split(splitRegex);
 			String subject = "";
			for (String word : words)
 				subject += word + " ";
 			email.setSubject(subject);
 
 			// process the content
 			words = ((String) email.getContent()).split(splitRegex);
 			StringBuilder sb = new StringBuilder(
 					((String) email.getContent()).length());
 
			for (String word : words)
 				sb.append(word + " ");
 
 			email.setContent(sb.toString(), "text/plain");
 		} catch (Exception ex) {
			//ignore
 		}
 	}
 
 }
