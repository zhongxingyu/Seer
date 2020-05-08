 /**
  * SOEN 490
  * Capstone 2011
  * User Domain Object
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package domain.user;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 
 import org.msgpack.MessagePack;
 import org.msgpack.packer.Packer;
 import org.msgpack.unpacker.Unpacker;
 
 public class User implements IUser {
 	
 	private BigInteger uid;
 	private String email;
 	private String password;
 	private UserType type;
 	private int version;
 	
 	public User(BigInteger uid, String email, String password, UserType type, int version)
 	{
 		this.uid = uid;
 		this.email = email;
 		this.password = password;
 		this.type = type;
 		this.version = version;
 	}
 	
 	public BigInteger getUid()
 	{
 		return uid;
 	}
 	
 	public String getPassword()
 	{
 		return password;
 	}
 	
 	public void setPassword(String password) 
 	{
 		this.password = password;
 	}
 	
 	public String getEmail()
 	{
 		return email;
 	}
 	
 	public void setEmail(String email) 
 	{
 		this.email = email;
 	}
 	
 	public UserType getType()
 	{
 		return type;
 	}
 	
 	public void setType(UserType type)
 	{
 		this.type = type;
 	}
 	
 	public int getVersion()
 	{
 		return version;
 	}
 	
 	public void setVersion(int version)
 	{
 		this.version = version;
 	}
 	
 	public void writeClient(DataOutputStream out) throws IOException
 	{
 		Packer packer = (new MessagePack()).createPacker(out);
 		packer.write(getEmail());
 	}
 
 	public void writeServer(DataOutputStream out) throws IOException {
 		Packer packer = (new MessagePack()).createPacker(out);
 		packer.write(uid);
 		packer.write(email);
 		packer.write(password);
 		packer.write(type);
 		packer.write(version);
 	}
 
 	public void readServer(DataInputStream in) throws IOException {
 		Unpacker unpacker = (new MessagePack()).createUnpacker(in);
 		uid = unpacker.readBigInteger();
 		email = unpacker.readString();
 		password = unpacker.readString();
 		type = unpacker.read(UserType.class);
 		version = unpacker.readInt();
 	}
 	
 	@Override
 	public boolean equals(Object o) {
         // If the object is not null and is of the type User
 		if ((o != null) && (o.getClass().equals(this.getClass()))) {
            // Test all of the particulars, except the password
 			return (
 						((User)o).getUid().equals(this.getUid()) &&
 						((User)o).getEmail().equals(this.getEmail()) &&
 	            		((User)o).getType().equals(this.getType()) &&
 	            		((User)o).getVersion() == this.getVersion()
             		);
         }
 		return false; // if we made it here we failed above
 	}
 	
 	@Override
 	public String toString() {
 		return "UserID: "+uid+" Email: "+email+" Password: "+password+" Type: "+type+" Version: "+version;
 	}
 }
