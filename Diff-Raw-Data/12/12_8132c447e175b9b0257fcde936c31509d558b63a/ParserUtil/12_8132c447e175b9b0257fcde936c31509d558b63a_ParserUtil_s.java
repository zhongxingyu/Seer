 package edu.umich.sbolt.language;
 
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ParserUtil {
 	
 	public String extractObject(String tagString, Map<String, Object> tagsToWords, int Counter) {
 		Pattern regex = Pattern.compile("(DT\\d* )?(JJ\\d* )*(NN\\d*)");
 		String tag = "OBJ";
 		Matcher m = regex.matcher(tagString);
 		if(m.find()){
 			StringBuffer sb = new StringBuffer();
 			do{	
 				LingObject obj = new LingObject();
 				obj.extractLinguisticComponents(m.group(),tagsToWords);
 				String newTag = tag+Integer.toString(Counter);
 				tagsToWords.put(newTag, obj);
 				m.appendReplacement(sb, newTag);
 				Counter++;
 			}while(m.find());
 			return sb.toString();
 		}
 		return tagString;
 	}
 	
	public   String extractObjectRelation(String tagString, Map<String, Object> tagsToWords, int Counter){
 		Pattern regex = Pattern.compile("(OBJ\\d* )(is\\d* )(PP\\d* )(OBJ\\d*)");
 		String tag = "REL";
 		Matcher m = regex.matcher(tagString);
 		if(m.find()){
 			StringBuffer sb = new StringBuffer();
 			do {
 				//	System.out.println(m.group());
 				ObjectRelation rel = new ObjectRelation();
 				rel.extractLinguisticComponents(m.group(),tagsToWords);
 				String newTag = tag+Integer.toString(Counter);
 				tagsToWords.put(newTag, rel);
 				m.appendReplacement(sb, newTag);
 				Counter++;
 			}while(m.find());
 			return sb.toString();
 		}
 		return tagString;
 	}
 	
 	public   String extractSentence(String tagString, Map<String, Object> tagsToWords, int Counter){
 		Pattern regex = Pattern.compile("(VBC\\d*)|(GS\\d*)|(PS\\d*)|(REL\\d*)");
 		String tag = "SEN";
 		Matcher m = regex.matcher(tagString);
 		if(m.find()){
 			StringBuffer sb = new StringBuffer();
 			do {
 				//	System.out.println(m.group());
 				Sentence sen = new Sentence();
 				sen.extractLinguisticComponents(m.group(), tagsToWords);
 				String newTag = tag+Integer.toString(Counter);
 				tagsToWords.put(newTag, sen);
 				m.appendReplacement(sb, newTag);
 				Counter++;
 			}while(m.find());
 			return sb.toString();
 		}
 		return tagString;
 	}
 	
 	public   String extractVerbCommand(String tagString, Map<String, Object> tagsToWords, int Counter) {
 		Pattern regex = Pattern.compile("((VB\\d* )(OBJ\\d* )(PP\\d* )(OBJ\\d*))|((VB\\d* )(OBJ\\d*))|(VB\\d*)");
 		String tag = "VBC";
 		Matcher m = regex.matcher(tagString);
 		if(m.find()){
 			StringBuffer sb = new StringBuffer();
 			do{	
 				VerbCommand vc = new VerbCommand();
 				vc.extractLinguisticComponents(m.group(),tagsToWords);
 				String newTag = tag+Integer.toString(Counter);
 				tagsToWords.put(newTag, vc);
 				m.appendReplacement(sb, newTag);
 				Counter++;
 			}while(m.find());
 			m.appendTail(sb);
 			return sb.toString();
 		}
 		return tagString;
 	}
 	
 	public String extractGoalInfo(String tagString, Map<String, Object> tagsToWords, int Counter){
 		Pattern regex = Pattern.compile("(goal\\d* )(of\\d* )(VBC\\d* )(is\\d* )(REL\\d*\\s*)+");
 		String tag = "GS";
 		Matcher m = regex.matcher(tagString);
 		if(m.find()){
 			StringBuffer sb = new StringBuffer();
 			do {
 				//	System.out.println(m.group());
 			    GoalInfo goal = new GoalInfo();
 				goal.extractLinguisticComponents(m.group(),tagsToWords);
 				String newTag = tag+Integer.toString(Counter);
 				tagsToWords.put(newTag, goal);
 				m.appendReplacement(sb, newTag);
 				Counter++;
 			}while(m.find());
 			return sb.toString();
 		}
 		return tagString;
 	}
 	
 	public String extractProposalInfo(String tagString, Map<String, Object> tagsToWords, int Counter){
 		Pattern regex = Pattern.compile("(VBC\\d* )(if\\d* )(REL\\d*\\s*)+");
 		String tag = "PS";
 		Matcher m = regex.matcher(tagString);
 		if(m.find()){
 			StringBuffer sb = new StringBuffer();
 			do {
 				//	System.out.println(m.group());
 			    ProposalInfo proposal = new ProposalInfo();
 				proposal.extractLinguisticComponents(m.group(),tagsToWords);
 				String newTag = tag+Integer.toString(Counter);
 				tagsToWords.put(newTag, proposal);
 				m.appendReplacement(sb, newTag);
 				Counter++;
 			}while(m.find());
 			return sb.toString();
 		}
 		return tagString;
 	}
 
 }
