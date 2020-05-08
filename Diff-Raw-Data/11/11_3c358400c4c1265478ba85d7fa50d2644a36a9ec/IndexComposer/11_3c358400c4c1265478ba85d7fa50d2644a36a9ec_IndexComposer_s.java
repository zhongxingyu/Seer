 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.proto.route;
 
 import tags.proto.LocalIndex;
 
 import tags.util.Maps.MapX2;
 import tags.util.Union.U2;
 import tags.util.Arc;
 import java.util.Map;
 
 /**
 ** DOCUMENT.
 **
** @param <U> Type of node-attribute
 ** @param <W> Type of arc-attribute
 ** @param <S> Type of score
 */
 public interface IndexComposer<W, S> {
 
 	public <T, A> W composeArc(MapX2<A, LocalIndex<T, A, W>, S> source, U2<Arc<T, A>, Arc<T, A>> arc);
 
 }
