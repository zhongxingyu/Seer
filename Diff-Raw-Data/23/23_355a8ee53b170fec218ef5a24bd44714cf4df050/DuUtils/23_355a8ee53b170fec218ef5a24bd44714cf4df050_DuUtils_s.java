 package witchdoctor.detect.diff.du;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 import difflib.Delta;
 
 /**
  * Utility class containing several methods to facilitate the manipulation 
  * of diffutils' <code>Delta</code> and <code>Chunk</code> objects.
  * @author Hugo Wood
  *
  */
 public final class DuUtils {
 	
	private static final Predicate<Object> stringIsBlank = new Predicate<Object>() {
 
 		@Override
 		public boolean apply(Object input) {
 			return input.toString().trim().isEmpty();
 		}
 		
 	};
 	
 	private DuUtils() {} // Hiding constructor of utility class.
 	
 	/**
 	 * Returns <code>true</code> if the delta represents a deletion.
 	 * A deletion means that the content was replaced by either nothing or 
 	 * white spaces. This is different from <code>delta.getType() == 
 	 * Delta.TYPE.DELETE</code>, which would return false in the second case.
 	 * @param delta
 	 * @return
 	 * @throws NullPointerException if <code>delta</code> is <code>null</code>
 	 */
 	public static boolean isDelete(Delta delta) {
		if (delta.getType() == Delta.TYPE.DELETE)
 			return true;
		return Iterables.all(delta.getRevised().getLines(), stringIsBlank);
 	}
 	
 	/**
 	 * Returns <code>true</code> if the delta represents a insertion.
 	 * A insertion means that the original content was either nothing or 
 	 * white spaces. This is different from <code>delta.getType() == 
 	 * Delta.TYPE.INSERT</code>, which would return false in the second case.
 	 * @param delta
 	 * @return
 	 * @throws NullPointerException if <code>delta</code> is <code>null</code>
 	 */
 	public static boolean isInsert(Delta delta) {
		if (delta.getType() == Delta.TYPE.INSERT)
 			return true;
		return Iterables.all(delta.getOriginal().getLines(), stringIsBlank);
 	}
 
 }
