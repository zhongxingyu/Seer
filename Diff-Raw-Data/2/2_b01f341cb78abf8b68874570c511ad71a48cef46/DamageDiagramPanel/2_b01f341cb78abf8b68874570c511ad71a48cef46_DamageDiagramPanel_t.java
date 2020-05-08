 package general;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.MathContext;
 import java.math.RoundingMode;
 
 import javax.swing.JPanel;
 
 import tk.speedprog.dota2.webapi.MatchDetails;
 import tk.speedprog.dota2.webapi.PlayerStats;
 
 
 public class DamageDiagramPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	BigDecimal[] dmgPercent = new BigDecimal[10];
 	private static final long serialVersionUID = 1L;
 	public DamageDiagramPanel(MatchDetails md) {
 		super();
 		BigInteger totalDmg = new BigInteger("0");
 		for (PlayerStats ps : md.players) {
 			totalDmg = totalDmg.add(ps.heroDamage);
 		}
 		for (PlayerStats ps : md.players) {
			dmgPercent[ps.player.playerSlot] = (new BigDecimal(ps.heroDamage)).divide(new BigDecimal(totalDmg), 20, RoundingMode.HALF_EVEN).multiply(new BigDecimal("100"));
 		}
 	}
  public void paintComponent(Graphics g) {
 	 super.paintComponent(g);
 	 Graphics2D g2 = (Graphics2D) g;
 	 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 	 Dimension d = this.getSize();
 	 int size = Math.min(d.width, d.height);
 	 int x = (d.width-size) / 2;
 	 int y = (d.height-size) / 2;
 	 int lastangle = 180;
 	 for (int p = 0; p<10; p++) {
 		 g.setColor(MatchWindow.slotColor[p]);
 		 int addangle = dmgPercent[p].multiply(new BigDecimal(3.6).round(new MathContext(0, RoundingMode.HALF_UP))).intValue();
 		 g.fillArc(x, y, size, size, lastangle-addangle, addangle);
 		 lastangle -= addangle;
 	 }
 	 
 	 // arg1 : x, arg2: y, arg3: width of circle, arg4: height of circle, arg5: what angle to start 0..360, arg6: how much to draw 0..360
 	 //g.fillArc(args[0], args[1], args[2], args[3], args[4], args[5]);
  }
  
 }
