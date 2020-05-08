 package ch.unibe.scg.cc;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.inject.Inject;
 
 import com.google.common.base.Charsets;
 
 /** Performs no normalization. Simply computes the cryptographic hash of a string */
 public class StandardHasher implements Hasher {
 	final private static long serialVersionUID = 1L;
 
 	/** Package private so that ShingleHasher can learn num of bytes. */
 	transient MessageDigest md = makeDigest();
 
 	@Inject
 	StandardHasher() {}
 
 	@Override
 	public byte[] hash(String document) {
 		checkNotNull(document);
 
 		return md.digest(document.getBytes(Charsets.UTF_8));
 	}
 
 	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
 		stream.defaultReadObject();
 		md = makeDigest();
 	}
 
 	private static MessageDigest makeDigest() {
 		try {
 			return MessageDigest.getInstance("SHA1");
 		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
 		}
 	}
 }
