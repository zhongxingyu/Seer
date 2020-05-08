 package jipdbs;
 
 import java.util.Date;
 
 public class AliasResult {
 
 	private String nickname;
 	private String ip;
 	private Date updated;
 	private int count;
 
 	public String getNickname() {
 		return nickname;
 	}
 
 	public void setNickname(String nickname) {
 		this.nickname = nickname;
 	}
 
 	public String getIp() {
 		return ip;
 	}
 
 	public void setIp(String ip) {
 		this.ip = ip;
 	}
 
 	public int getCount() {
 		return count;
 	}
 
 	public void setCount(int count) {
 		this.count = count;
 	}
 
 	public Date getUpdated() {
 		return updated;
 	}
 
 	public void setUpdated(Date updated) {
 		this.updated = updated;
 	}

	public String getIpSearch() {
		if (ip == null)
			return null;
		int l = ip.lastIndexOf('.');
		if (l < 0)
			return null;

		return ip.substring(0, l + 1) + "*";
	}
 }
