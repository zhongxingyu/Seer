import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
 import org.apache.commons.lang.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
 
 /** Matcher for model objects
  * @author Tommy Skodje
  */
 public abstract class PropertyListMatcher<T> extends TypeSafeMatcher<T> {
 	final List<Object> expected	= new ArrayList<Object>();
 
 	abstract protected List<Object> properties( T actual );
 
 	public PropertyListMatcher( Object... expectedValues ) {
 		super();
 		CollectionUtils.addAll( expected, expectedValues );
 	}
 	@Override public boolean matchesSafely( T actual ) {
		Collection<?> actuals	= properties( actual );
 		return ListUtils.isEqualList( expected, actuals );
 	}
 	public void describeTo(Description description) {
 		String msg	= StringUtils.join( expected, "," );
 		description.appendText( msg );
 	}
 
 	/** Nice to have here */
 	public List<Object> asList( Object... values ) {
 		List<Object> list	= new ArrayList<Object>();
 		CollectionUtils.addAll( list, values );
 		return list;
 	}
 }
