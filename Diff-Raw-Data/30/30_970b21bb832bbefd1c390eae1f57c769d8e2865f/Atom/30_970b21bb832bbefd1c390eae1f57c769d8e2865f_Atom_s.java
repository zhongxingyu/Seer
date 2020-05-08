 package cs252.lab6.atoms;
 
 import android.graphics.Color;
 import android.graphics.Point;
 
 public class Atom
 {
 	private Player player;
 	private int num;
 	private double[] elecs;
 	private int[] speeds;
 	private double nuc;
 	
	public Atom(int rings)
 	{
		player = new Player("null", Color.GRAY, true);
 		num = 0;
 		elecs = new double[rings];
 		speeds = new int[rings];
 		for(int i=0;i<rings;i++)
 		{
 			elecs[i] = Math.random() * (2 * Math.PI);
 			int reverse = (int)(Math.random()*2) == 0 ? -1 : 1;
 			speeds[i] = reverse * ((int)(Math.random() * 8) + 3);
 		}
 		nuc = Math.random() * (2 * Math.PI);
 	}
 	
 	public void moveElectrons()
 	{
 		int movingElecs = num;
 		if(num > elecs.length)
 			movingElecs = elecs.length;
 		for(int i=0;i<movingElecs;i++)
 			elecs[i] = (elecs[i] + (Math.PI / 180) * speeds[i]) % (2 * Math.PI);
 	}
 	
 	public void addElectron() { num++; }
 	public void sendOutElectrons() { num -= elecs.length + 1; }
 	public void setPlayer(Player player) { this.player = player; }
 	
 	public Player getPlayer() { return player; }
 	public int getColor() { return player.getColor(); }
 	public int getBackColor() { return player.getBackColor(); }
 	public int getNum() { return num; }
 	public int getRings() { return elecs.length; }
 	public double[] getElecs() { return elecs; }
 	public double getNuc() { return nuc; }
 	public int getOutermostElec() { return num < elecs.length-1 ? num : elecs.length-1; }
 }
