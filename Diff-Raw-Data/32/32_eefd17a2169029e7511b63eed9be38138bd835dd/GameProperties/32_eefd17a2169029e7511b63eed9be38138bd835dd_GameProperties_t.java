 package org.drooms.impl.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Properties;
 
 import org.drooms.api.Player;
 import org.drooms.util.CommonProperties;
 
 /**
  * Prepares the game properties by reading them from a property file.
  * 
  * <p>
  * Mandatory properties are:
  * </p>
  * 
  * <dl>
  * <dt>collectibles</dt>
  * <dd>Which {@link CollectibleType}s exist, as a comma-separated list. Each name in this list will then subsequently
  * require properties 'collectible.expiration.$NAME', 'collectible.price.$NAME' and 'collectible.probability.$NAME'.</dd>
  * </dl>
  * 
  * <p>
  * Optional properties are:
  * </p>
  * 
  * <dl>
  * <dt>worm.length.start (defaults to 3)</dt>
  * <dd>Length of the worm at the start of the game. Actually, initially each worm will only have a length of 1. But as
  * it first moves its head, the tail of the worm will stay where the head was, until the worm reaches the specified
  * starting length. From then on, the length will be kept constant and only changed upon collecting an item.</dd>
  * <dt>worm.max.turns (defaults to 1000)</dt>
  * <dd>Maximum length of the game, in case more than 1 worm keeps on surviving.</dd>
  * <dt>worm.max.inactive.turns (defaults to 3)</dt>
  * <dd>Maximum number of turns of inactivity after which a player may be terminated, if the game decides so.</dd>
  * <dt>worm.timeout.seconds (defaults to 1)</dt>
 * <dd>The maximum amount of time that the {@link Player}'s Strategy has to make a decision on the next movement
  * of the worm. If it doesn't make it in time, STAY is enforced, potentially leading to the worm being terminated for
  * inactivity.</dd>
  * <dt>worm.survival.bonus (defaults to 5)</dt>
  * <dd>The amount of points that the worm will be awarded upon surviving another worm.</dd>
  * </dl>
  */
 public class GameProperties extends CommonProperties {
 
     public static class CollectibleType {
 
         private final int expiration;
         private final String name;
         private final int points;
         private final BigDecimal probabilityOfAppearance;
 
         private CollectibleType(final String name, final int expiration, final int points,
                 final BigDecimal probabilityOfAppearance) {
             this.name = name;
             this.expiration = expiration;
             this.points = points;
             this.probabilityOfAppearance = probabilityOfAppearance;
         }
 
         public int getExpiration() {
             return this.expiration;
         }
 
         public String getName() {
             return this.name;
         }
 
         public int getPoints() {
             return this.points;
         }
 
         public BigDecimal getProbabilityOfAppearance() {
             return this.probabilityOfAppearance;
         }
 
     }
 
     public static GameProperties read(final InputStream is) throws IOException {
         return new GameProperties(CommonProperties.loadPropertiesFromInputStream(is));
     }
 
     private final Collection<CollectibleType> collectibleTypes;
     private final int deadWormBonus;
     private final int maximumInactiveTurns;
     private final int maximumTurns;
     private final int startingWormLength;
     private final int strategyTimeoutInSeconds;
 
     private GameProperties(final Properties p) {
         super(p);
         this.startingWormLength = Integer.valueOf(this.getOptionalProperty("worm.length.start", "3"));
         this.maximumInactiveTurns = Integer.valueOf(this.getOptionalProperty("worm.max.inactive.turns", "3"));
         this.maximumTurns = Integer.valueOf(this.getOptionalProperty("worm.max.turns", "1000"));
         this.deadWormBonus = Integer.valueOf(this.getOptionalProperty("worm.survival.bonus", "5"));
         this.strategyTimeoutInSeconds = Integer.valueOf(this.getOptionalProperty("worm.timeout.seconds", "1"));
         final Collection<CollectibleType> collectibleTypes = new ArrayList<CollectibleType>();
         for (final String collectibleName : this.getMandatoryProperty("collectibles").split("\\Q,\\E")) {
             final int expiration = Integer.valueOf(this.getMandatoryProperty("collectible.expiration."
                     + collectibleName));
             final int points = Integer.valueOf(this.getMandatoryProperty("collectible.price." + collectibleName));
             final BigDecimal prob = new BigDecimal(this.getMandatoryProperty("collectible.probability."
                     + collectibleName));
             collectibleTypes.add(new CollectibleType(collectibleName, expiration, points, prob));
         }
         this.collectibleTypes = Collections.unmodifiableCollection(collectibleTypes);
     }
 
     public Collection<CollectibleType> getCollectibleTypes() {
         return this.collectibleTypes;
     }
 
     public int getDeadWormBonus() {
         return this.deadWormBonus;
     }
 
     public int getMaximumInactiveTurns() {
         return this.maximumInactiveTurns;
     }
 
     public int getMaximumTurns() {
         return this.maximumTurns;
     }
 
     public int getStartingWormLength() {
         return this.startingWormLength;
     }
 
     public int getStrategyTimeoutInSeconds() {
         return this.strategyTimeoutInSeconds;
     }
 
 }
