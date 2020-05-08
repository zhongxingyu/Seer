 package se.fnord.jamon.internal;
 
import java.util.HashMap;
 import java.util.Map;
 import java.util.Objects;
 
 import se.fnord.jamon.Node;
 import se.fnord.jamon.NodeContext;
 import se.fnord.jamon.NodeMatcher;
 import se.fnord.jamon.Path;
 
 public class NodeContextImpl implements NodeContext {
 	public final class CacheKey {
 		private final int hashCode;
 		private final Path path;
 		private final NodeMatcher matcher;
 
 		public CacheKey(Path path, NodeMatcher matcher) {
 			this.path = path;
 			this.matcher = matcher;
 			this.hashCode = path.hashCode() * 31 + matcher.hashCode();
 		}
 
 		public final Path path() {
 			return path;
 		}
 
 		public final NodeMatcher matcher() {
 			return matcher;
 		}
 
 		@Override
 		public final String toString() {
 			return String.format("{ %d, %s }", path, Objects.toString(matcher));
 		}
 
 		@Override
 		public int hashCode() {
 			return hashCode;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (!(obj instanceof CacheKey))
 				return false;
 			final CacheKey that = (CacheKey) obj;
 			return this.hashCode == that.hashCode && Objects.equals(this.path, that.path) && Objects.equals(this.matcher, that.matcher);
 		}
 	}
 
 	private final Map<CacheKey, Boolean> matchCache;
 	private final Path path;
 
 	public NodeContextImpl(Path path, Map<CacheKey, Boolean> matchCache) {
 		this.matchCache = matchCache;
 		this.path = path;
     }
 	
 	@Override
     public NodeContext forChild(Node n) {
 	    return new NodeContextImpl(path.forChild(n), matchCache);
     }
 
 	@Override
     public NodeContext forParent() {
 	    return new NodeContextImpl(path.forParent(), matchCache);
     }
 
 	@Override
 	public boolean matches(NodeMatcher matcher) {
 		final CacheKey key = new CacheKey(path, matcher);
 		Boolean result = matchCache.get(key);
 		if (result != null)
 			return result;
 
 		result = matcher.match(this, path.leaf());
 
 		final Boolean oldResult = matchCache.put(key, result);
 		if (oldResult != null && oldResult != result)
 			throw new IllegalStateException("The (path, matcher) yielded different results on different rounds");
 
 		return result;
 	}
 
 	@Override
 	public Path path() {
 	    return path;
 	}
 }
