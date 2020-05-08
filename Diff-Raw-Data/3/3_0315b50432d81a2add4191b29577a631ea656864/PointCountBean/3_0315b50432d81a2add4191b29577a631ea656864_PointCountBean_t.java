 package yy.nlsde.buaa.region;
 
 public class PointCountBean extends PointBean{
 	
 	public final static int UP=1;
 	public final static int DOWN=2;
 
 	private String time;
 	private int ud;//up or down
 	private int count;
 	
 	public PointCountBean(String line) {
 		String[] sub=line.split(",");
 		this.lon=Double.parseDouble(sub[0]);
 		this.lat=Double.parseDouble(sub[1]);
 		this.time=sub[2];
 		this.ud=Integer.parseInt(sub[3]);
 		this.count=Integer.parseInt(sub[4]);
		
		if (this.time.length()>2)
			this.time=this.time.substring(this.time.length()-2);
 	}
 
 	public String getTime() {
 		return Integer.parseInt(this.time)+"";
 	}
 
 	public void setTime(String time) {
 		this.time = time;
 	}
 
 	public int getUd() {
 		return ud;
 	}
 
 	public void setUd(int ud) {
 		this.ud = ud;
 	}
 
 	public int getCount() {
 		return count;
 	}
 
 	public void setCount(int count) {
 		this.count = count;
 	}
 	
 	
 }
