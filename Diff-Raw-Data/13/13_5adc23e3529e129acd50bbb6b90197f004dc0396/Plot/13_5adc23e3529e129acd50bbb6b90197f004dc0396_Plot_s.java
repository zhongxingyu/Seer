 package com.simplyian.superplots.plot;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 
 import com.simplyian.superplots.SuperPlots;
import com.simplyian.superplots.SuperPlotsPlugin;
 
 /**
  * Represents an ownable piece of land.
  */
 public class Plot {
     /**
      * Name of the plot.
      */
     private String name;
 
     /**
      * Name of the owner of the plot.
      */
     private String owner;
 
     /**
      * Set of coowners of the plot.
      */
     private Set<String> coowners = new HashSet<String>();
 
     /**
      * Set of friends of the plot.
      */
     private Set<String> friends = new HashSet<String>();
 
     /**
      * Funds of the plot.
      */
     private int funds = 0;
 
     /**
      * Size of the plot.
      */
     private int size;
 
     /**
      * Center of the plot.
      */
     private Location center;
 
     /**
      * True if the plot is protected.
      */
     private boolean protect = true;
 
     /**
      * True if the plot is private.
      */
     private boolean privacy = true;
 
     /**
      * @see PlotManager#createPlot
      */
     Plot(String name, String owner, int size, Location center) {
         this.name = name;
         this.owner = owner;
         this.size = size;
         this.center = center;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name
      *            the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @return the owner
      */
     public String getOwner() {
         return owner;
     }
 
     /**
      * @param owner
      *            the owner to set
      */
     public void setOwner(String owner) {
         this.owner = owner;
     }
 
     /**
      * Checks if the given player is the owner of the plot.
      * 
      * @param owner
      * @return
      */
     public boolean isOwner(String owner) {
         return getOwner().equals(owner);
     }
 
     /**
      * Adds a coowner to the plot.
      * 
      * @param coowner
      */
     public void addCoowner(String coowner) {
         coowners.add(coowner);
     }
 
     /**
      * Removes a coowner from the plot.
      * 
      * @param coowner
      */
     public void removeCoowner(String coowner) {
         coowners.remove(coowner);
     }
 
     /**
      * Gets a set of all coowners of this plot.
      * 
      * @return
      */
     public Set<String> getCoowners() {
         return new HashSet<String>(coowners);
     }
 
     /**
      * Checks if the given player is a coowner.
      * 
      * @param coowner
      * @return
      */
     public boolean isCoowner(String coowner) {
         return coowners.contains(coowner);
     }
 
     /**
      * Adds a friend to the plot.
      * 
      * @param friend
      */
     public void addFriend(String friend) {
         friends.add(friend);
     }
 
     /**
      * Removes a friend from the plot.
      * 
      * @param friend
      */
     public void removeFriend(String friend) {
         friends.remove(friend);
     }
 
     /**
      * Checks if the given player is a friend.
      * 
      * @param friend
      * @return
      */
     public boolean isFriend(String friend) {
         return friends.contains(friend);
     }
 
     /**
      * Gets the friends of this plot.
      * 
      * @return
      */
     public Set<String> getFriends() {
         return new HashSet<String>(friends);
     }
 
     /**
      * Returns true if the given player is an adminstrator of the plot.
      * 
      * @param member
      * @return
      */
     public boolean isAdministrator(String member) {
         return isCoowner(member) || isOwner(member);
     }
 
     /**
      * Returns true if the given player is a a member of the plot.
      * 
      * @param member
      * @return
      */
     public boolean isMember(String member) {
         return isFriend(member) || isAdministrator(member);
     }
 
     /**
      * Kicks the given player from the plot. You cannot kick the owner of the
      * plot.
      * 
      * @param member
      */
     public void kick(String member) {
         if (isFriend(member)) {
             removeFriend(member);
         } else if (isCoowner(member)) {
             removeCoowner(member);
         }
     }
 
     /**
      * @return the funds
      */
     public int getFunds() {
         return funds;
     }
 
     /**
      * @param funds
      *            the funds to set
      */
     public void setFunds(int funds) {
         this.funds = funds;
     }
 
     /**
      * Adds funds to the plot.
      * 
      * @param funds
      */
     public void addFunds(int funds) {
         setFunds(getFunds() + funds);
     }
 
     /**
      * Subtracts funds from the plot.
      * 
      * @param funds
      */
     public void subtractFunds(int funds) {
         setFunds(getFunds() - funds);
     }
 
     /**
      * @return the size
      */
     public int getSize() {
         return size;
     }
 
     /**
      * @param size
      *            the size to set
      */
     public void setSize(int size) {
         this.size = size;
     }
 
     /**
      * @return the center
      */
     public Location getCenter() {
         return center;
     }
 
     /**
      * @param center
      *            the center to set
      */
     public void setCenter(Location center) {
         this.center = center;
     }
 
     /**
      * Returns true if the plot is protected.
      * 
      * @return the protect
      */
     public boolean isProtected() {
         return protect;
     }
 
     /**
      * @param protect
      *            the protect to set
      */
     public void setProtected(boolean protect) {
         this.protect = protect;
     }
 
     /**
      * @return the privacy
      */
     public boolean isPrivate() {
         return privacy;
     }
 
     /**
      * Returns true if the plot is public.
      * 
      * @return
      */
     public boolean isPublic() {
         return !privacy;
     }
 
     /**
      * @param privacy
      *            the privacy to set
      */
     public void setPrivate(boolean privacy) {
         this.privacy = privacy;
     }
 
     /**
      * Gets the daily tax rate of the plot.
      * 
      * @return
      */
     public int dailyTax() {
         return getSize() * 5;
     }
 
     /**
      * Gets the amount of days left where tax can still pay off the plot.
      * 
      * @return
      */
     public int taxDaysLeft() {
         return getFunds() / dailyTax();
     }
 
     /**
      * Processes tax.
      * 
      * @return <ul>
      *         <li>0 if the tax was able to be paid.</li>
      *         <li>1 if the tax could not be paid and shrunk as a result.</li>
      *         <li>2 if the plot was deleted.</li>
      *         </ul>
      */
     public int processTax() {
         int tax = dailyTax();
         if (tax <= getFunds()) {
             subtractFunds(tax);
             return 0;
         }
 
         if (getSize() > SuperPlots.getSettings().getMinimumPlotSize()) {
             shrink(1);
             return 1;
         } else {
             disband();
             return 2;
         }
     }
 
     /**
      * Gets the distance between this plot and the given location.
      * 
      * @param other
      * @return
      */
     public double distance(Location other) {
         return center.distance(other);
     }
 
     /**
      * Gets the formatted distance between this plot and the given location.
      * 
      * @param other
      * @return
      */
     public String distancef(Location other) {
         return String.format("%1$,.2f", distance(other));
     }
 
     /**
      * Gets the distance squared between this plot and the given location.
      * 
      * @param other
      * @return
      */
     public double distanceSquared(Location other) {
         return center.distanceSquared(other);
     }
 
     /**
      * Gets the distance between the closest edge of this plot and the given
      * location.
      * 
      * @param other
      * @return
      */
     public double edgeDistance(Location other) {
         return distance(other) - (double) getSize();
     }
 
     /**
      * Gets the distance between the closest edge of the influence of this plot
      * and the given location.
      * 
      * @param location
      * @return
      */
     public double influenceEdgeDistance(Location location) {
         return edgeDistance(location)
                 * SuperPlots.getSettings().getInfluenceMultiplier();
     }
 
     /**
      * Expands the plot by one.
      */
     public void expand() {
         expand(1);
     }
 
     /**
      * Expands the plot by the given amount.
      * 
      * @param amount
      */
     public void expand(int amount) {
         setSize(getSize() + amount);
     }
 
     /**
      * Shrinks the plot by the given amount.
      * 
      * @param amount
      */
     public void shrink(int amount) {
         setSize(getSize() - amount);
     }
 
     /**
      * Returns true if the plot contains the given location.
      * 
      * @param location
      * @return
      */
     public boolean contains(Location location) {
         return distanceSquared(location) < (getSize() * getSize());
     }
 
     /**
      * Gets the value of the plot.
      * 
      * @return
      */
     public int getValue() {
         int val = 0;
         for (int i = 1; i <= getSize(); i++) {
             val += i * 10;
         }
         return val;
     }
 
     /**
      * Disbands the plot.
      */
     public void disband() {
         SuperPlots.getPlotManager().disbandPlot(this);
     }
 
     /**
      * Returns true if the name is valid.
      * 
      * @param name
      * @return
      */
     public static boolean isValidName(String name) {
         return name.matches("[A-Za-z0-9 '!\\.]{1,40}");
     }
 }
