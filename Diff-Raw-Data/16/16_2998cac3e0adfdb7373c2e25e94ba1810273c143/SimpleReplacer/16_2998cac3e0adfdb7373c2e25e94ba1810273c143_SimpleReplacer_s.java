 package uk.co.brotherlogic.jarpur.replacers;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Map;
 
 import uk.co.brotherlogic.jarpur.LinkTable;
 
 public class SimpleReplacer extends Replacer {
 
 	@Override
 	public String process(Object ref, Map<String, Object> objectMap) {
 
<<<<<<< HEAD
 		System.err.println("PROC: " + LinkTable.getLinkTable());
 
 		if (replacement.startsWith("link")) {
 			System.err.println("LINK: " + replacement.substring(5) + " => "
 					+ resolve(replacement.substring(5), objectMap) + " give "
 					+ LinkTable.getLinkTable());
 			return LinkTable.getLinkTable().resolveLink(
 					resolve(replacement.substring(5), objectMap));
=======
		if (replacement.startsWith("link:resource"))
			return LinkTable.add + replacement.substring(5);

		if (replacement.startsWith("link")) {
			return lTable.resolveLink(resolve(replacement.substring(5),
					objectMap));
>>>>>>> 09eca6a4a3b0c097c978277ed3f41ce5b7d36bd5
 		}
 
 		Object obj = resolve(replacement, objectMap);
 
 		if (obj instanceof Calendar) {
 			DateFormat df = new SimpleDateFormat("dd/MM/yy");
 			return df.format(((Calendar) obj).getTime());
 		}
 
 		return obj.toString();
 	}
 
 	private final String replacement;
 
	public SimpleReplacer(Object pg, String replacerText) {
		setRefObj(pg);
 		replacement = replacerText;
 	}
 
 	@Override
 	public String toString() {
 		return "Simple: " + replacement;
 	}
 }
