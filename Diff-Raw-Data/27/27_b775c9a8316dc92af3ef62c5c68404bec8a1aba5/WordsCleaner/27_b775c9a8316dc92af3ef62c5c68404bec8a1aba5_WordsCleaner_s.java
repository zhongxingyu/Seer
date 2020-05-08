 package preprocessors;
 
 import general.Email;
 
 /**
  * removes punctuations and any non alphanumeric characters from an email
  * and converts the email into words separated with single white space
  * @author ahmedkotb
  *
  */
 public class WordsCleaner implements Preprocessor{
 
 	@Override
 	public void apply(Email email) {
 		//splits the email on any non alpha numeric characters
 		
 		//process the subject
		String[] words = email.getSubject().split("\\W+");
 		String subject = "";
 		for (String word : words)
 			subject += word + " ";
 		email.setSubject(subject);
 		
 		//process the content
		words = email.getContent().split("\\W+");
 		StringBuilder sb = new StringBuilder(email.getContent().length());
 		
 		for (String word : words)
 			sb.append(word + " ");
 		
 		email.setContent(sb.toString());
 	}
 
 }
