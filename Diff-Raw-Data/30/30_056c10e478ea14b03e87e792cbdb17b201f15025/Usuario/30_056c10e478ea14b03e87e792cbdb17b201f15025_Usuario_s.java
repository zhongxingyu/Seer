 package panchat.addressing;
 
 import java.io.Serializable;
 import java.util.UUID;
 
 public class Usuario implements Serializable, Comparable<Usuario> {
 
 	private static final long serialVersionUID = 1L;
 
 	public UUID uuid;
 	public String ip;
 	public int port;
 	public String nickName;
 
 	public Usuario(String ip, int port, String nickName) {
 		this.ip = ip;
 		this.port = port;
 		this.nickName = nickName;
 		this.uuid = UUID.randomUUID();
 	}
 
 	@Override
 	public int compareTo(Usuario o) {
		return nickName.compareTo(o.nickName);
 	}
 
 	@Override
 	public String toString() {
 		return "[nick:" + nickName + ",port:" + String.valueOf(port) + ",uuid:"
 				+ uuid.toString() + "]";
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Usuario) {
 			Usuario usuario = (Usuario) obj;
 			return uuid.equals(usuario.uuid) && ip.equals(usuario.ip)
 					&& (port == usuario.port)
 					&& nickName.equals(usuario.nickName);
 		} else
 			return false;
 	}
 
 }
