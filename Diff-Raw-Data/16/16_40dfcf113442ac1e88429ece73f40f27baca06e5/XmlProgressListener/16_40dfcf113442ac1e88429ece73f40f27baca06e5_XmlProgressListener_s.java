 package org.drooms.impl;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.drooms.api.Collectible;
 import org.drooms.api.GameProgressListener;
 import org.drooms.api.Action;
 import org.drooms.api.Node;
 import org.drooms.api.Player;
 import org.drooms.api.Playground;
 import org.drooms.impl.util.GameProperties;
 import org.drooms.impl.util.XmlUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class XmlProgressListener implements GameProgressListener {
 
     private static Logger logger = LoggerFactory.getLogger(XmlProgressListener.class);
 
     private static String collectibleXml(final Collectible c) {
         return "<collectible points='" + c.getPoints() + "' expiresInTurn='" + c.expiresInTurn() + "' />";
     }
 
     private static String nodeXml(final Node c) {
         return "<node x='" + c.getX() + "' y='" + c.getY() + "' />";
     }
 
     private static String playerXml(final Player p) {
         return "<player name='" + p.getName() + "' />";
     }
 
     private final StringBuilder report = new StringBuilder();
 
     private int turnNumber = 0;
 
     private final Map<Player, Integer> playerPoints = new HashMap<>();
 
     public XmlProgressListener(final Playground p, final Collection<Player> players, final GameProperties gameConfig) {
         this.report.append("<game>");
         // report game config
         this.report.append("<config>");
         for (final Map.Entry<Object, Object> pair : gameConfig.getTextEntries()) {
             final String key = (String) pair.getKey();
             final String value = (String) pair.getValue();
             this.report.append("<property name='" + key + "' value='" + value + "' />");
         }
         this.report.append("</config>");
         // report players
         this.report.append("<players>");
         for (final Player player : players) {
             this.report.append(XmlProgressListener.playerXml(player));
         }
         this.report.append("</players>");
         // report playground
         this.report.append("<playground>");
         for (int x = -1; x <= p.getWidth(); x++) {
             for (int y = -1; y <= p.getHeight(); y++) {
                 if (p.isAvailable(x, y)) {
                     this.report.append(XmlProgressListener.nodeXml(p.getNodeAt(x, y)));
                 }
             }
         }
         this.report.append("</playground>");
         this.report.append("<turns>");
     }
 
     private void addPoints(final Player p, final int points) {
         if (this.playerPoints.containsKey(p)) {
             this.playerPoints.put(p, this.playerPoints.get(p) + points);
         } else {
             this.playerPoints.put(p, points);
         }
     }
 
     @Override
     public void collectibleAdded(final Collectible c) {
         this.report.append("<newCollectible>");
         this.report.append(XmlProgressListener.collectibleXml(c));
         this.report.append(XmlProgressListener.nodeXml(c.getAt()));
         this.report.append("</newCollectible>");
     }
 
     @Override
     public void collectibleCollected(final Collectible c, final Player p, final int points) {
         this.addPoints(p, points);
         this.report.append("<collectedCollectible points='" + points + "'>");
         this.report.append(XmlProgressListener.collectibleXml(c));
         this.report.append(XmlProgressListener.playerXml(p));
         this.report.append(XmlProgressListener.nodeXml(c.getAt()));
         this.report.append("</collectedCollectible>");
     }
 
     @Override
     public void collectibleRemoved(final Collectible c) {
         this.report.append("<removedCollectible>");
         this.report.append(XmlProgressListener.collectibleXml(c));
         this.report.append(XmlProgressListener.nodeXml(c.getAt()));
         this.report.append("</removedCollectible>");
     }
 
     @Override
     public void nextTurn() {
         if (this.turnNumber > 0) {
             this.report.append("</turn>");
         }
         this.report.append("<turn number='" + this.turnNumber + "'>");
         this.turnNumber += 1;
     }
 
     @Override
     public void playerCrashed(final Player p) {
         this.report.append("<crashedPlayer>");
         this.report.append(XmlProgressListener.playerXml(p));
         this.report.append("</crashedPlayer>");
     }
 
     @Override
     public void playerDeactivated(final Player p) {
         this.report.append("<deactivatedPlayer>");
         this.report.append(XmlProgressListener.playerXml(p));
         this.report.append("</deactivatedPlayer>");
     }
 
     @Override
    public void playerMoved(final Player p, final Action m, final Node... nodes) {
         this.report.append("<playerPosition>");
         this.report.append(XmlProgressListener.playerXml(p));
         for (final Node n : nodes) {
             this.report.append(XmlProgressListener.nodeXml(n));
         }
         this.report.append("</playerPosition>");
     }
 
     @Override
     public void playerSurvived(final Player p, final int points) {
         this.addPoints(p, points);
         this.report.append("<survivedPlayer points='" + points + "'>");
         this.report.append(XmlProgressListener.playerXml(p));
         this.report.append("</survivedPlayer>");
     }
 
     @Override
     public void write(final Writer w) throws IOException {
         final StringBuilder result = new StringBuilder(this.report);
         if (this.turnNumber > 0) {
             result.append("</turn>");
         }
         result.append("</turns>");
         result.append("<results>");
         for (final Map.Entry<Player, Integer> entry : this.playerPoints.entrySet()) {
             result.append("<score points='" + entry.getValue() + "'>");
             result.append(XmlProgressListener.playerXml(entry.getKey()));
             result.append("</score>");
         }
         result.append("</results>");
         result.append("</game>");
         String resultingXml = result.toString();
         /*
          * make the XML pretty; if it fails, error is logged and original string
          * will be returned
          */
         try {
             resultingXml = XmlUtil.prettyPrint(result.toString());
         } catch (final RuntimeException re) {
             XmlProgressListener.logger.error("Error while pretty printing XML report, !\n", re);
         }
         w.write(resultingXml);
     }
 }
