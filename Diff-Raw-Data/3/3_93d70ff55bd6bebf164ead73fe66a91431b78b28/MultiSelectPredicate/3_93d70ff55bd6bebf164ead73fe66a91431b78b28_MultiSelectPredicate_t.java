 package pl.art.mnp.rogalin.db.predicate;
 
 import java.util.Collection;
 
 import com.mongodb.DBObject;
 
 @SuppressWarnings("serial")
 public class MultiSelectPredicate implements Predicate {
 
 	private final Collection<String> subset;
 
 	private final String fieldName;
 
 	public MultiSelectPredicate(Collection<String> subset, String fieldName) {
 		this.subset = subset;
 		this.fieldName = fieldName;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public boolean matches(DBObject dbObject) {
 		DBObject field = (DBObject) dbObject.get(fieldName);
		if (field == null) {
			return true;
		}
 		Collection<String> values = (Collection<String>) field.get("values");
 		for (String s : subset) {
 			if (!values.contains(s)) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
