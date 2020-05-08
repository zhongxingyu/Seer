 package ch.unibe.scg.cc.activerecord;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import ch.unibe.scg.cc.CannotBeHashedException;
 import ch.unibe.scg.cc.Hasher;
 
 import com.google.common.collect.Lists;
 import com.google.inject.assistedinject.Assisted;
 
 public class Function extends Column {
 	public static final byte[] FUNCTION_SNIPPET = Bytes.toBytes("fs");
 	final private List<Snippet> snippets = Lists.newArrayList();
 	final private int baseLine;
 	/** contains the normalized content */
 	final transient CharSequence normalized;
 	/** contains the real content */
 	final CharSequence contents;
 	private byte[] hash = null;
 	final Hasher hasher;
 
 	public static interface FunctionFactory {
 		Function makeFunction(Hasher hasher, int baseLine, @Assisted("normalized") CharSequence normalized,
 				@Assisted("contents") CharSequence contents);
 	}
 
 	@Inject
 	/**
 	 * Creates a new function by copying only "normalized", "contents", "baseline" and
 	 * "standardHasher" from the provided function.
 	 */
 	public Function(@Assisted Hasher hasher, @Assisted int baseLine, @Assisted("normalized") CharSequence normalized,
 			@Assisted("contents") CharSequence contents) {
 		this.hasher = hasher;
 		this.baseLine = baseLine;
 		this.normalized = normalized;
 		this.contents = contents;
 	}
 
 	/**
	 * @return if the hasher throws a CannotBeHashedException the
	 *         {@link ByteUtils#EMPTY_SHA1_KEY} is returned.
 	 */
 	public byte[] getHash() {
 		if (hash == null) {
 			try {
 				hash = hasher.hash(normalized.toString());
 			} catch (CannotBeHashedException e) {
 				return new byte[20];
 			}
 		}
 		return hash;
 	}
 
 	public int getBaseLine() {
 		return baseLine;
 	}
 
 	public String getContents() {
 		return contents.toString();
 	}
 
 	public void addSnippet(Snippet snippet) {
 		this.snippets.add(snippet);
 	}
 
 	public List<Snippet> getSnippets() {
 		return this.snippets;
 	}
 
 	public void saveContents(Put put) {
 		put.add(FAMILY_NAME, FUNCTION_SNIPPET, 0l, Bytes.toBytes(getContents()));
 	}
 }
