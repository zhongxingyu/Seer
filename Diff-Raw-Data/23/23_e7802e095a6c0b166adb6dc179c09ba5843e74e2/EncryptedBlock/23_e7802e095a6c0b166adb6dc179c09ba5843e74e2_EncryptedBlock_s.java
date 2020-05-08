 package me.footlights.core.data;
 
 import java.nio.ByteBuffer;
 
 
 /**
  * Encrypted block data.
  * @author Jonathan Anderson (jon@footlights.me)
  */
 public class EncryptedBlock
 {
 	public static class Builder
 	{
 		public EncryptedBlock build()
 		{
			return new EncryptedBlock(ciphertext, name, link);
 		}
 
 		public Builder setCiphertext(ByteBuffer ciphertext)
 		{
 			this.ciphertext = ciphertext;
 			return this;
 		}
		public Builder setName(String name)
		{
			this.name = name;
			return this;
		}
 		public Builder setLink(Link link)
 		{
 			this.link = link;
 			return this;
 		}
 
 		private Builder() {}
 
 		private ByteBuffer ciphertext;
		private String name;
 		private Link link;
 	}
 
 	public static Builder newBuilder() { return new Builder(); }
 
 	/** The name of the block. */
	public String name() { return name; }
 
 	/** The ciphertext itself. */
 	public ByteBuffer ciphertext() { return ciphertext.asReadOnlyBuffer(); }
 
 	/** A link to the ciphertext. */
 	public Link link() { return link; }
 
 
	private EncryptedBlock(ByteBuffer ciphertext, String name, Link link)
 	{
 		this.ciphertext = ciphertext.asReadOnlyBuffer();
		this.name = name;
 		this.link = link;
 	}
 
 	private final ByteBuffer ciphertext;
	private final String name;
 	private final Link link;
 }
