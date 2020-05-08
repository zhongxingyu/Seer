 package jscl.text;
 
 import jscl.math.Generic;
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * User: serso
  * Date: 10/27/11
  * Time: 3:18 PM
  */
 public class MultiTryParser<T> implements Parser<T> {
 
 	@NotNull
 	private final List<Parser<T>> parsers;
 
 	public MultiTryParser(@NotNull List<Parser<T>> parsers) {
 		this.parsers = parsers;
 	}
 
 	@Override
 	public T parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		T result = null;
 
 		for (final Iterator<Parser<T>> it = parsers.iterator(); it.hasNext(); ) {
 			try {
 				result = it.next().parse(string, position);
 			} catch (ParseException e) {
 				if (!it.hasNext()) {
 					throw e;
 				}
 			}

			if (result != null) {
				break;
			}
 		}
 
 		return result;
 	}
 }
