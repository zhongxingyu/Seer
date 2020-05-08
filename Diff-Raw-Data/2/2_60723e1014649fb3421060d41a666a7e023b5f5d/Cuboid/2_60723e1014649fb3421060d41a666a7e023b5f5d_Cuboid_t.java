 
 package com.quartercode.quarterbukkit.api;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Location;
 import org.bukkit.util.Vector;
 
 /**
  * <p>
  * This class represents a cuboid between to different locations saved as {@link Vector}s.
  * </p>
  * 
  * <p>
  * The cuboid can't get modified after construction, so it's final. You can only read values in different ways.
  * </p>
  * 
  * <p>
  * All constructors delivers their cooridnates to {@link #Cuboid(double, double, double, double, double, double)} which sorts them to a larger and a smaller {@link Vector}. This will lose the original
  * locations, but you can work better with the data later on.
  * </p>
  * 
  * <p>
  * {@link #getVector1()} returns the larger one, {@link #getVector2()} the smaller one. You can also read those values by using the {@code getXXX1()} and {@code getXXX2()} methods.
  * </p>
  * 
  * <p>
  * Here's an example how the sorting works: We have two locations with the coordinates {@code 5, 10, 7} and {@code 12, 6, 18}, the {@link Vector}s will be the larger one {@code 12, 10, 18} and the
  * smaller one {@code 5, 6, 7}.
  * </p>
  */
 public class Cuboid {
 
     private final Vector vector1;
     private final Vector vector2;
 
     /**
      * <p>
      * Creates a new cuboid out of two locations given by six doubles, three for each location.
      * </p>
      * 
      * <p>
      * This constructor sorts the coordinates to a larger and a smaller {@link Vector}. The larger one contains all the larger values, the smaller one contains all coordinates with the smaller values.
      * This will lose the original locations, but you can work better with the data later on.
      * </p>
      * 
      * <p>
      * Here's an example how the sorting works: We have two locations with the coordinates {@code 5, 10, 7} and {@code 12, 6, 18}, the {@link Vector}s will be the larger one {@code 12, 10, 18} and the
      * smaller one {@code 5, 6, 7}.
      * </p>
      * 
      * @param x1 The x-coordinate for the first location.
      * @param y1 The y-coordinate for the first location.
      * @param z1 The z-coordinate for the first location.
      * @param x2 The x-coordinate for the second location.
      * @param y2 The y-coordinate for the second location.
      * @param z2 The z-coordinate for the second location,
      */
     public Cuboid(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
 
         vector1 = new Vector();
         vector2 = new Vector();
 
         if (x1 < x2) {
             vector1.setX(x2);
             vector2.setX(x1);
         } else {
             vector1.setX(x1);
             vector2.setX(x2);
         }
 
         if (y1 < y2) {
             vector1.setY(y2);
             vector2.setY(y1);
         } else {
             vector1.setY(y1);
             vector2.setY(y2);
         }
 
         if (z1 < z2) {
             vector1.setZ(z2);
             vector2.setZ(z1);
         } else {
             vector1.setZ(z1);
             vector2.setZ(z2);
         }
     }
 
     /**
      * <p>
      * Creates a new cuboid out of two locations given by two {@link Vector}s.
      * </p>
      * 
      * <p>
      * This constructor sorts the coordinates to a larger and a smaller {@link Vector}. The larger one contains all the larger values, the smaller one contains all coordinates with the smaller values.
      * This will lose the original locations, but you can work better with the data later on.
      * </p>
      * 
      * <p>
      * Here's an example how the sorting works: We have two locations with the coordinates {@code 5, 10, 7} and {@code 12, 6, 18}, the {@link Vector}s will be the larger one {@code 12, 10, 18} and the
      * smaller one {@code 5, 6, 7}.
      * </p>
      * 
      * @param vector1 The {@link Vector} for the first location.
      * @param vector2 The {@link Vector} for the second location.
      */
     public Cuboid(final Vector vector1, final Vector vector2) {
 
         this(vector1.getX(), vector1.getY(), vector1.getZ(), vector2.getX(), vector2.getY(), vector2.getZ());
     }
 
     /**
      * <p>
      * Creates a new cuboid out of two locations given by two {@link Location}s.
      * </p>
      * 
      * <p>
      * This constructor sorts the coordinates to a larger and a smaller {@link Vector}. The larger one contains all the larger values, the smaller one contains all coordinates with the smaller values.
      * This will lose the original locations, but you can work better with the data later on.
      * </p>
      * 
      * <p>
      * Here's an example how the sorting works: We have two locations with the coordinates {@code 5, 10, 7} and {@code 12, 6, 18}, the {@link Vector}s will be the larger one {@code 12, 10, 18} and the
      * smaller one {@code 5, 6, 7}.
      * </p>
      * 
      * @param location1 The {@link Location} for the first location.
      * @param location2 The {@link Location} for the second location.
      */
     public Cuboid(final Location location1, final Location location2) {
 
         this(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
     }
 
     /**
      * <p>
      * This method returns the first cuboid {@link Vector} with the larger coordinates.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the one with the
      * larger values.
      * </p>
      * 
      * @return The first cuboid {@link Vector} with the larger coordinates.
      */
     public Vector getVector1() {
 
         return vector1;
     }
 
     /**
      * <p>
      * This method returns the second cuboid {@link Vector} with the smaller coordinates.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the one with the
      * smaller values.
      * </p>
      * 
      * @return The second cuboid {@link Vector} with the smaller coordinates.
      */
     public Vector getVector2() {
 
         return vector2;
     }
 
     /**
      * <p>
      * This method returns the larger x-coordinate of both locations. You could also use the {@link Vector#getX()}-method of the {@link Vector} you're getting by {@link #getVector1()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the x-coordinate of
      * the one with the larger values.
      * </p>
      * 
      * @return The larger x-coordinate of both locations.
      */
     public double getX1() {
 
         return vector1.getX();
     }
 
     /**
      * <p>
      * This method returns the larger x-coordinate of both locations as a block location. You could also use the {@link Vector#getBlockX()}-method of the {@link Vector} you're getting by
      * {@link #getVector1()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the x-coordinate of
      * the one with the larger values as a block location.
      * </p>
      * 
      * @return The larger x-coordinate of both locations as a block location.
      */
     public int getBlockX1() {
 
         return vector1.getBlockX();
     }
 
     /**
      * <p>
      * This method returns the larger y-coordinate of both locations. You could also use the {@link Vector#getY()}-method of the {@link Vector} you're getting by {@link #getVector1()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the y-coordinate of
      * the one with the larger values.
      * </p>
      * 
      * @return The larger y-coordinate of both locations.
      */
     public double getY1() {
 
         return vector1.getY();
     }
 
     /**
      * <p>
      * This method returns the larger y-coordinate of both locations as a block location. You could also use the {@link Vector#getBlockY()}-method of the {@link Vector} you're getting by
      * {@link #getVector1()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the y-coordinate of
      * the one with the larger values as a block location.
      * </p>
      * 
      * @return The larger y-coordinate of both locations as a block location.
      */
     public int getBlockY1() {
 
         return vector1.getBlockY();
     }
 
     /**
      * <p>
      * This method returns the larger z-coordinate of both locations. You could also use the {@link Vector#getZ()}-method of the {@link Vector} you're getting by {@link #getVector1()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the z-coordinate of
      * the one with the larger values.
      * </p>
      * 
      * @return The larger z-coordinate of both locations.
      */
     public double getZ1() {
 
         return vector1.getZ();
     }
 
     /**
      * <p>
      * This method returns the larger z-coordinate of both locations as a block location. You could also use the {@link Vector#getBlockZ()}-method of the {@link Vector} you get by
      * {@link #getVector1()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the z-coordinate of
      * the one with the larger values as a block location.
      * </p>
      * 
      * @return The larger z-coordinate of both locations as a block location.
      */
     public int getBlockZ1() {
 
         return vector1.getBlockZ();
     }
 
     /**
      * <p>
      * This method returns the smaller x-coordinate of both locations. You could also use the {@link Vector#getX()}-method of the {@link Vector} you're getting by {@link #getVector2()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the x-coordinate of
      * the one with the smaller values.
      * </p>
      * 
      * @return The smaller x-coordinate of both locations.
      */
     public double getX2() {
 
         return vector2.getX();
     }
 
     /**
      * <p>
      * This method returns the smaller x-coordinate of both locations as a block location. You could also use the {@link Vector#getBlockX()}-method of the {@link Vector} you get by
      * {@link #getVector2()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the x-coordinate of
      * the one with the smaller values as a block location.
      * </p>
      * 
      * @return The smaller x-coordinate of both locations as a block location.
      */
     public int getBlockX2() {
 
         return vector2.getBlockX();
     }
 
     /**
      * <p>
      * This method returns the smaller y-coordinate of both locations. You could also use the {@link Vector#getY()}-method of the {@link Vector} you're getting by {@link #getVector2()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the y-coordinate of
      * the one with the smaller values.
      * </p>
      * 
      * @return The smaller y-coordinate of both locations.
      */
     public double getY2() {
 
         return vector2.getY();
     }
 
     /**
      * <p>
      * This method returns the smaller y-coordinate of both locations as a block location. You could also use the {@link Vector#getBlockY()}-method of the {@link Vector} you get by
      * {@link #getVector2()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the y-coordinate of
      * the one with the smaller values as a block location.
      * </p>
      * 
      * @return The smaller y-coordinate of both locations as a block location.
      */
     public int getBlockY2() {
 
         return vector2.getBlockY();
     }
 
     /**
      * <p>
      * This method returns the smaller z-coordinate of both locations. You could also use the {@link Vector#getZ()}-method of the {@link Vector} you're getting by {@link #getVector2()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the z-coordinate of
      * the one with the smaller values.
      * </p>
      * 
      * @return The smaller z-coordinate of both locations.
      */
     public double getZ2() {
 
         return vector2.getZ();
     }
 
     /**
      * <p>
      * This method returns the smaller z-coordinate of both locations as a block location. You could also use the {@link Vector#getBlockX()}-method of the {@link Vector} you get by
      * {@link #getVector2()}.
      * </p>
      * 
      * <p>
      * The save {@link Vector}s aren't the input locations, the coordinates of both locations are sorted into one with the larger and one with the smaller ones. This will returns the z-coordinate of
      * the one with the smaller values as a block location.
      * </p>
      * 
      * @return The smaller z-coordinate of both locations as a block location.
      */
     public int getBlockZ2() {
 
         return vector2.getBlockZ();
     }
 
     /**
      * <p>
      * This method returns the distance between the two x-coordinates (you can use this to get the distance between the two x-walls) by subtracting the smaller from the larger one. This will always
      * return a positive distance.
      * </p>
      * 
      * @return The distance between the two x-coordinates (you can use this to get the distance between the two x-walls).
      */
     public double getXDistance() {
 
         return getX1() - getX2();
     }
 
     /**
      * <p>
      * This method returns the distance between the two x-block-coordinates (you can use this to get the distance between the two x-walls as a block-value) by subtracting the smaller from the larger
      * one and round. This will always return a positive distance.
      * </p>
      * 
      * @return The distance between the two x-block-coordinates (you can use this to get the distance between the two x-walls as a block-value).
      */
     public double getBlockXDistance() {
 
         return getBlockX1() - getBlockX2();
     }
 
     /**
      * <p>
      * This method returns the distance between the two y-coordinates (you can use this to get the distance between the two y-walls) by subtracting the smaller from the larger one. This will always
      * return a positive distance.
      * </p>
      * 
      * @return The distance between the two y-coordinates (you can use this to get the distance between the two y-walls).
      */
     public double getYDistance() {
 
         return getY1() - getY2();
     }
 
     /**
      * <p>
      * This method returns the distance between the two y-block-coordinates (you can use this to get the distance between the two y-walls as a block-value) by subtracting the smaller from the larger
      * one and round. This will always return a positive distance.
      * </p>
      * 
      * @return The distance between the two y-block-coordinates (you can use this to get the distance between the two y-walls as a block-value).
      */
     public double getBlockYDistance() {
 
         return getBlockY1() - getBlockY2();
     }
 
     /**
      * <p>
      * This method returns the distance between the two z-coordinates (you can use this to get the distance between the two z-walls) by subtracting the smaller from the larger one. This will always
      * return a positive distance.
      * </p>
      * 
      * @return The distance between the two z-coordinates (you can use this to get the distance between the two z-walls).
      */
     public double getZDistance() {
 
         return getZ1() - getZ2();
     }
 
     /**
      * <p>
      * This method returns the distance between the two z-block-coordinates (you can use this to get the distance between the two z-walls as a block-value) by subtracting the smaller from the larger
      * one and round. This will always return a positive distance.
      * </p>
      * 
      * @return The distance between the two z-block-coordinates (you can use this to get the distance between the two z-walls as a block-value).
      */
     public double getBlockZDistance() {
 
         return getBlockZ1() - getBlockZ2();
     }
 
     /**
      * <p>
      * This method returns the center position of the cuboid by subtracting the half of every distance from every larger coordinate. You can use this to get the center of gravity of the cuboid.
      * </p>
      * 
      * @return The center position of the cuboid.
      */
     public Vector getCenter() {
 
         return new Vector(getX1() - getXDistance() / 2, getY1() - getYDistance() / 2, getZ1() - getZDistance() / 2);
     }
 
     /**
      * <p>
      * This method returns the center position of the cuboid as a block location by subtracting the half of every distance from every larger coordinate and calculating the block location of every
      * result coordinate. You can use this to get the center of gravity of the cuboid as a block location.
      * </p>
      * 
      * @return The center position of the cuboid as a block location.
      */
     public Vector getBlockCenter() {
 
         return new Vector(getCenter().getBlockX(), getCenter().getBlockY(), getCenter().getBlockZ());
     }
 
     /**
      * <p>
      * This method checks if an other location actually intersects with the cuboid. The location is given by three doubles representing the x-, y- and z-coordinate and returning the result as a
      * boolean.
      * </p>
      * 
      * <p>
      * There will only be a positive result if all coordinates are intersecting the cuboid.
      * </p>
      * 
      * @param x The x-coordinate for checking if it's intersecting the cuboid.
      * @param y The y-coordinate for checking if it's intersecting the cuboid.
      * @param z The z-coordinate for checking if it's intersecting the cuboid.
      * @return If the other location actually intersects with the cuboid.
      */
     public boolean intersects(final double x, final double y, final double z) {
 
         return x <= getX1() && x >= getX2() && y <= getY1() && y >= getY2() && z <= getZ1() && z >= getZ2();
     }
 
     /**
      * <p>
      * This method checks if an other location actually intersects with the cuboid. The location is given by a {@link Vector} and returning the result as a boolean.
      * </p>
      * 
      * <p>
      * There will only be a positive result if all coordinates of the {@link Vector} are intersecting the cuboid.
      * </p>
      * 
      * @param vector The {@link Vector} for checking if it's intersecting the cuboid.
      * @return If the other location actually intersects with the cuboid.
      */
     public boolean intersects(final Vector vector) {
 
         return intersects(vector.getX(), vector.getY(), vector.getZ());
     }
 
     /**
      * <p>
      * This method checks if an other location actually intersects with the cuboid. The location is given by a {@link Location} and returning the result as a boolean.
      * </p>
      * 
      * <p>
      * There will only be a positive result if all coordinates of the {@link Location} are intersecting the cuboid.
      * </p>
      * 
      * @param location The {@link Location} for checking if it's intersecting the cuboid.
      * @return If the other location actually intersects with the cuboid.
      */
     public boolean intersects(final Location location) {
 
         return intersects(location.getX(), location.getY(), location.getZ());
     }
 
     /**
      * <p>
      * This method returns a list of all blocks which are located in the cuboid as a list of {@link Vector}s.
      * </p>
      * 
      * <p>
      * You can iterate over the list and doing anything you want with the blocks like this:
      * 
      * <pre>
      * for (Vector vector : getBlocks()) {
      *     vector.toLocation(world).getBlock()...
      * }
      * </pre>
      * 
      * </p>
      * 
     * @return A list of all blocks which are located in the cuboid as a list of {@link Vector}s.
      */
     public List<Vector> getBlocks() {
 
         final List<Vector> blockPositions = new ArrayList<Vector>();
 
         for (int x = getBlockX2(); x <= getBlockX1(); x++) {
             for (int y = getBlockY2(); y <= getBlockY1(); y++) {
                 for (int z = getBlockZ2(); z <= getBlockZ1(); z++) {
                     blockPositions.add(new Vector(x, y, z));
                 }
             }
         }
 
         return blockPositions;
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = 1;
         result = prime * result + (vector1 == null ? 0 : vector1.hashCode());
         result = prime * result + (vector2 == null ? 0 : vector2.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(final Object obj) {
 
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Cuboid other = (Cuboid) obj;
         if (vector1 == null) {
             if (other.vector1 != null) {
                 return false;
             }
         } else if (!vector1.equals(other.vector1)) {
             return false;
         }
         if (vector2 == null) {
             if (other.vector2 != null) {
                 return false;
             }
         } else if (!vector2.equals(other.vector2)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [vector1=" + vector1 + ", vector2=" + vector2 + "]";
     }
 
 }
