 package preprocessors;
 
 import general.Email;
 
 /**
 * removes punctuation and any non alphanumeric characters from an email
 * converting it into words separated with single white space
  * 
  * @author ahmedkotb
  * 
  */
 public class WordsCleaner implements Preprocessor {
 
 	private String splitRegex = "\\W+";
 
 	@Override
 	public void apply(Email email) {
 		try {
 			// splits the email on any non alpha numeric characters
 
			//two special cases has to be handled
			//1- split method can return an empty space at the
			//   Beginning of the array
			//2- \W doesn't split on underscores

 			// process the subject
 			String[] words = email.getSubject().split(splitRegex);
 			String subject = "";
			for (String word : words){
				if (word.equals("") || word.equals("_"))
					continue;
 				subject += word + " ";
			}
 			email.setSubject(subject);
 
 			// process the content
 			words = ((String) email.getContent()).split(splitRegex);
 			StringBuilder sb = new StringBuilder(
 					((String) email.getContent()).length());
 
			for (String word : words){
				if (word.equals("") || word.equals("_"))
					continue;
 				sb.append(word + " ");
			}
 
 			email.setContent(sb.toString(), "text/plain");
 		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
 		}
 	}
 
 }
