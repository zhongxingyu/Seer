 
 
 package es.igosoftware.euclid.experimental.algorithms;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import es.igosoftware.euclid.shape.GLinesStrip2D;
 import es.igosoftware.euclid.shape.GSegment3D;
 import es.igosoftware.euclid.shape.IPolygonalChain;
 import es.igosoftware.euclid.shape.IPolygonalChain2D;
 import es.igosoftware.euclid.shape.IPolygonalChain3D;
 import es.igosoftware.euclid.vector.GVector2D;
 import es.igosoftware.euclid.vector.GVector3D;
 import es.igosoftware.euclid.vector.IVector;
 import es.igosoftware.euclid.vector.IVector2;
 import es.igosoftware.euclid.vector.IVector3;
 
 
 public class GLinesToEquispacedPoints<
 
 VectorT extends IVector<VectorT, ?>
 
 >
          implements
             IAlgorithm<VectorT> {
 
 
    @SuppressWarnings("null")
    public List<VectorT> process(final IPolygonalChain<VectorT, ?, ?> geom,
                                 final double distance) {
 
 
       final List<VectorT> coords = geom.getPoints();
       if (coords.isEmpty()) {
          throw new RuntimeException("Invalid geometry: " + geom);
       }
 
      final ArrayList<VectorT> output = new ArrayList<VectorT>(coords.size());
 
       output.add(coords.get(0));

      double remainingDistFromLastSegment = 0;
       for (int i = 0; i < coords.size() - 1; i++) {
          final VectorT current = coords.get(i);
          final VectorT next = coords.get(i + 1);
 
         final double distToNextPoint = next.distance(current);
 
         final VectorT direction = next.sub(current).normalized();
 
         final int pointsToAdd = (int) ((remainingDistFromLastSegment + distToNextPoint) / distance);
         if (pointsToAdd > 0) {
             VectorT addedPoint = null;
            for (int j = 0; j < pointsToAdd; j++) {
               final double dist = (distance - remainingDistFromLastSegment) + (j * distance);
 
               addedPoint = current.add(direction.scale(dist));
                output.add(addedPoint);
             }
 
             remainingDistFromLastSegment = addedPoint.distance(next);
          }
          else {
             remainingDistFromLastSegment += distToNextPoint;
          }
 
       }
 
      output.trimToSize(); // release some memory

       return output;
    }
 
 
    public static void main(final String[] args) {
 
       final GLinesToEquispacedPoints<IVector2> alg2 = new GLinesToEquispacedPoints<IVector2>();
 
       final IPolygonalChain2D line2 = new GLinesStrip2D(false, new GVector2D(0, 0), new GVector2D(0, 10), new GVector2D(10, 10));
       final List<IVector2> points2 = alg2.process(line2, 1);
 
       System.out.println("Points: " + points2.size());
       for (final IVector2 point : points2) {
          System.out.println(" " + point);
       }
 
       System.out.println();
 
 
       final GLinesToEquispacedPoints<IVector3> alg3 = new GLinesToEquispacedPoints<IVector3>();
 
       final IPolygonalChain3D line3 = new GSegment3D(new GVector3D(0, 0, 0), new GVector3D(0, 10, 0));
       final List<IVector3> points3 = alg3.process(line3, 1);
 
       System.out.println("Points: " + points3.size());
       for (final IVector3 point : points3) {
          System.out.println(" " + point);
       }
 
 
    }
 
 }
