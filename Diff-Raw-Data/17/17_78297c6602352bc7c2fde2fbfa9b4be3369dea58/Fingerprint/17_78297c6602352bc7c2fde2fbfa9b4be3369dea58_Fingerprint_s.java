 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.core.crypto;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import scala.Option;
 
 import org.apache.commons.codec.binary.Base32;
 import org.apache.commons.codec.binary.Hex;
 
 import me.footlights.core.HasBytes;
 import me.footlights.core.Preconditions;
 import me.footlights.core.Preferences;
 import me.footlights.core.ConfigurationError;
 import me.footlights.core.ProgrammerError;
 
 
 /** A fingerprint for a number of bytes. */
 public class Fingerprint
 {
 	public static Option<Fingerprint> unapply(String s)
 	{
 		try { return Option.apply(decode(new URI(s))); }
		catch (Exception e) { return Option.apply(null); }
 	}
 
 	public static Fingerprint decode(URI uri)
 		throws NoSuchAlgorithmException
 	{
 		return decode(uri.getScheme(), uri.getSchemeSpecificPart());
 	}
 
 	public static Fingerprint decode(String name)
 		throws NoSuchAlgorithmException
 	{
 		String parts[] = name.split(":");
 		if (parts.length != 3)
 			throw new IllegalArgumentException("Invalid fingerprint '" + name + "'");
 
 		if (!parts[0].equals("urn"))
 			throw new IllegalArgumentException("URNs must start with 'urn:'");
 
 		return decode(parts[1].toLowerCase(), parts[2].toUpperCase());
 	}
 
 	public static Fingerprint decode(String algorithmName, String hash)
 		throws NoSuchAlgorithmException
 	{
 		MessageDigest algorithm = MessageDigest.getInstance(algorithmName);
 		final URI uri;
 		try { uri = new URI("urn", algorithmName + ":" + hash, null); }
 		catch (URISyntaxException e) { throw new IllegalArgumentException(e); }
 
 		return new Fingerprint(algorithm,
 				ByteBuffer.wrap(new Base32().decode(hash.getBytes())),
 				uri);
 	}
 
 	public static Builder newBuilder() { return new Builder(Preferences.getDefaultPreferences()); }
 
 	public URI toURI() { return uri; }
 	public String encode() { return uri.toString(); }
 
 	public MessageDigest getAlgorithm() { return algorithm; }
 
 	public boolean matches(ByteBuffer b) { return (0 == bytes.compareTo(b)); }
 	public boolean matches(byte[] b) { return matches(ByteBuffer.wrap(b)); }
 
 	public ByteBuffer getBytes() { return bytes.asReadOnlyBuffer(); }
 	public byte[] copyBytes()
 	{
 		byte[] copy = new byte[bytes.remaining()];
 		bytes.mark();
 		bytes.get(copy);
 		bytes.reset();
 		return copy;
 	}
 
 	public static class Builder
 	{
 		public Fingerprint build()
 		{
 			Preconditions.notNull(bytes);
 
 			ByteBuffer hash = ByteBuffer.wrap(algorithm.digest(bytes));
 
 			final URI uri;
 			try
 			{
 				uri = new URI("urn",
 						algorithm.getAlgorithm().toLowerCase() + ":" +
 						new String(new Base32().encode(hash.array())), null);
 			}
 			catch (URISyntaxException e)
 			{
 				throw new ProgrammerError("Failed to make URI for Fingerprint", e);
 			}
 
 			return new Fingerprint(algorithm, hash, uri);
 		}
 
 		public Builder setAlgorithm(String a) throws NoSuchAlgorithmException
 		{
 			algorithm = MessageDigest.getInstance(a);
 			return this;
 		}
 
 		public Builder setContent(byte[] b) { bytes = b; return this; }
 		public Builder setContent(HasBytes h) { return setContent(h.getBytes()); }
 		public Builder setContent(ByteBuffer b)
 		{
 			if (b.isReadOnly())
 			{
 				bytes = new byte[b.remaining()];
 				b.get(bytes);
 			}
 			else bytes = b.array();
 
 			return this;
 		}
 		
 		private Builder(Preferences preferences)
 		{
 			try
 			{
 				algorithm = MessageDigest.getInstance(
 					preferences.getString("crypto.hash.algorithm").get());
 			}
 			catch (NoSuchAlgorithmException e)
 			{
 				throw new ConfigurationError("Invalid hash algorithm: " + e);
 			}
 		}
 
 		private MessageDigest algorithm;
 		private byte[] bytes;
 	}
 
 
 	@Override public String toString() { return uri.toString(); }
 	@Override public int hashCode() { return bytes.duplicate().getInt(); }
 	@Override public boolean equals(Object o)
 	{
 		if (!(o instanceof Fingerprint)) return false;
 		Fingerprint f = (Fingerprint) o;
 
 		if (!algorithm.getAlgorithm().toLowerCase()
 				.equals(f.algorithm.getAlgorithm().toLowerCase()))
 			return false;
 		if (bytes.compareTo(f.bytes) != 0) return false;
 
 		return true;
 	}
 
 	String hex() { return Hex.encodeHexString(bytes.array()); }
 
 	private Fingerprint(MessageDigest hashAlgorithm, ByteBuffer fingerprintBytes, URI uri)
 	{
 		Preconditions.check(uri.getScheme().equals("urn"));
 
 		this.algorithm = hashAlgorithm;
 		this.bytes = fingerprintBytes;
 		this.uri = uri;
 	}
 
 	private MessageDigest algorithm;
 	private ByteBuffer bytes;
 	private URI uri;
 }
