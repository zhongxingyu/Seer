 package math.algebra.permgroups.permutation;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
 
import java.util.Map;
 import java.util.Set;
 
 final class ComposedPermutation<E> extends Permutation<E> {
   private final Permutation<E> p;
   private final Permutation<E> q;
 
   ComposedPermutation(Permutation<E> p, Permutation<E> q) {
     this.p = checkNotNull(p);
     this.q = checkNotNull(q);
     checkDomains(p, q);
   }
 
   @Override public Set<E> domain() {
     return p.domain();
   }
 
   @Override public E image(E e) {
     return q.image(p.image(e));
   }
 
   @Override public E preimage(E e) {
     return p.preimage(q.preimage(e));
   }
 
   @Override Permutation<E> createInverse() {
     return new ComposedPermutation<E>(q.inverse(), p.inverse());
   }
 
   @Override Function<E, E> createAsFunction() {
     return Functions.compose(q.asFunction(), p.asFunction());
   }
 }
