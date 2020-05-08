 package roujo.emily.core.util;
 
 public enum InternalUser {
	Skynet(UserLevel.Owner, new String[] { "Skynet", "Skynet|Work" });
 
 	private final UserLevel status;
 	private final String[] nicks;
 
 	private InternalUser(UserLevel status, String[] nicks) {
 		this.status = status;
 		this.nicks = nicks;
 	}
 
 	private InternalUser(UserLevel status, String nick) {
 		this(status, new String[] { nick });
 	}
 
 	public UserLevel getStatus() {
 		return status;
 	}
 
 	public String[] getNicks() {
 		return nicks;
 	}
 	
 	public boolean isOwner() {
 		return status.getRank() >= UserLevel.Owner.getRank();
 	}
 	
 	public boolean isSuper() {
 		return status.getRank() >= UserLevel.Super.getRank();
 	}
 	
 	public boolean isBlackListed() {
 		return status.getRank() <= UserLevel.BlackListed.getRank();
 	}
 }
