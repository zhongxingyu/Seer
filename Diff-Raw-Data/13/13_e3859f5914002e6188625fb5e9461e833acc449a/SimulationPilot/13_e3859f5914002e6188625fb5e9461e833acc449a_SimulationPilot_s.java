 package simulator.pilot;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.Random;
 
 import mapping.Barcode;
 import mapping.MapGraph;
 import mapping.Obstruction;
 import mapping.Orientation;
 import mapping.Tile;
 import simulator.viewport.SimulatorPanel;
 
 public class SimulationPilot extends AbstractPilot {
 
     private static class SimulationSensorData {
         // Whether there the light is bright or quite dark. influences the light
         // sensor
         private static boolean isBrightLight = true;
         private static boolean isDriving = true;
 
         // Standard Deviation of the light sensor, when standing on a panel
         // containing a barcode under given circumstances.
         public static final double getMBarcodeTileLS(final int color) {
             // Black
             if (color == 0) {
                 if (isBrightLight) {
                     if (isDriving) {
                         return 33.05;
                     } else {
                         return 34;
                     }
                 } else {
                     if (isDriving) {
                         return 33.42;
                     } else {
                         return 33;
                     }
                 }
             }
             // White
             else if (color == 1) {
                 return SimulationSensorData.getMWhiteLineLS();
                 // Not on the code itself, but on the brown panel next to it
             } else {
                 return SimulationSensorData.getMEmptyPanelLS();
             }
         }
 
         // Mean value of the light sensor, when standing on an empty panel under
         // given circumstances.
         public static final double getMEmptyPanelLS() {
             if (isBrightLight) {
                 if (isDriving) {
                     return 49.3863;
                 } else {
                     return 49.99688474;
                 }
             } else {
                 if (isDriving) {
                     return 49.46829;
                 } else {
                     return 49;
                 }
             }
         }
 
         // Mean value of the infrared sensor, when the robot has no infrared in
         // view
         public static final double getMNoInfraRedIS() {
             return 2;
         }
 
         // Mean value of the infrared sensor, when having an open seesaw in view
         public static final double getMSeesawIS() {
             return 120;
         }
 
         // Mean value of the light sensor, when standing a white line under
         // given circumstances.
         public static final double getMWhiteLineLS() {
             if (isBrightLight) {
                 if (isDriving) {
                     return 55.02606;
                 } else {
                     return 54.99377;
                 }
             } else {
                 if (isDriving) {
                     return 54.98442;
                 } else {
                     return 55;
                 }
             }
         }
 
         // Standard Deviation of the light sensor, when standing on a panel
         // containing a barcode under given circumstances.
         // The color should be 0 when standing on a black part, 1 when standing
         // on a white part or something else when standing next to the panel.
         public static final double getSDBarcodeTileLS(final int color) {
             // Black
             if (color == 0) {
                 if (isBrightLight) {
                     if (isDriving) {
                         return 0.272790608;
                     } else {
                         return 0;
                     }
                 } else {
                     if (isDriving) {
                         return 0.575648536;
                     } else {
                         return 0;
                     }
                 }
             }
             // White
             else if (color == 1) {
                 return SimulationSensorData.getSDWhiteLineLS();
                 // Not on the code itself, but on the brown panel next to it
             } else {
                 return SimulationSensorData.getSDEmptyPanelLS();
             }
         }
 
         // Standard Deviation of the light sensor, when standing on an empty
         // panel under given circumstances.
         public static final double getSDEmptyPanelLS() {
             if (isBrightLight) {
                 if (isDriving) {
                     return 0.6425;
                 } else {
                     return 0.055814557;
                 }
             } else {
                 if (isDriving) {
                     return 1.218249;
                 } else {
                     return 0;
                 }
             }
         }
 
         // Standard Deviation of the ultrasonic sensor under given
         // circumstances.
         public static final double getSDUS() {
             return 0.523148364;
         }
 
         // Standard Deviation of the light sensor, when standing a white line
         // under given circumstances.
         public static final double getSDWhiteLineLS() {
             if (isBrightLight) {
                 if (isDriving) {
                     return 0.966416;
                 } else {
                     return 0.111629;
                 }
             } else {
                 if (isDriving) {
                     return 1.1218249;
                 } else {
                     return 0;
                 }
             }
         }
     }
 
     private MapGraph mapGraphLoaded;
     private final double lightSensorDistanceFromAxis = 7.5;
     private final double ultrasonicSensorDistanceFromAxis = 2.5;
 
     public SimulationPilot(int teamNumber, MapGraph mapGraphLoaded) {
         super(teamNumber);
         this.mapGraphLoaded = mapGraphLoaded;
     }
 
     private double calculateDistanceToWall() {
         if (mapGraphLoaded == null) {
             return 250;
         }
         double distanceToFirstEdge;
         int amountOfTilesVisible;
         Orientation orientation = getOrientation();
         Tile tile = mapGraphLoaded.getTile(getMatrixPosition());
         // Berekent de afstand van de UltraSensor tot de eerste edge
         if (orientation == Orientation.NORTH) {
             distanceToFirstEdge = getUltrasonicSensorCoordinates()[0]
                     % sizeTile();
         } else if (orientation == Orientation.SOUTH) {
             distanceToFirstEdge = sizeTile()
                     - (getUltrasonicSensorCoordinates()[0] % sizeTile());
         } else if (orientation == Orientation.WEST) {
             distanceToFirstEdge = getUltrasonicSensorCoordinates()[1]
                     % sizeTile();
         } else {
             distanceToFirstEdge = sizeTile()
                     - (getUltrasonicSensorCoordinates()[1] % sizeTile());
         }
 
         // Berekent het aantal tegels die zichtbaar zijn na deze edge (tot een
         // maximum van 3)
         if (tile.getEdgeAt(orientation).getObstruction() != Obstruction.WALL) {
             tile = tile.getEdgeAt(orientation).getNeighbour(tile);
             if (tile.getEdgeAt(orientation).getObstruction() != Obstruction.WALL) {
                 tile = tile.getEdgeAt(orientation).getNeighbour(tile);
                 if (tile.getEdgeAt(orientation).getObstruction() != Obstruction.WALL) {
                     tile = tile.getEdgeAt(orientation).getNeighbour(tile);
                     if (tile.getEdgeAt(orientation).getObstruction() != Obstruction.WALL) {
                         return 250;
                     } else {
                         amountOfTilesVisible = 3;
                     }
                 } else {
                     amountOfTilesVisible = 2;
                 }
             } else {
                 amountOfTilesVisible = 1;
             }
         } else {
             amountOfTilesVisible = 0;
         }
 
         return distanceToFirstEdge + amountOfTilesVisible * sizeTile();
     }
 
     @Override
     protected boolean crashImminent() {
         Point2D.Double point = new Point2D.Double();
         if (getOrientation() == Orientation.NORTH) {
             point.setLocation(getPosition().getX(), getPosition().getY() - 1);
         } else if (getOrientation() == Orientation.SOUTH) {
             point.setLocation(getPosition().getX(), getPosition().getY() + 1);
         } else if (getOrientation() == Orientation.EAST) {
             point.setLocation(getPosition().getX() + 1, getPosition().getY());
         } else if (getOrientation() == Orientation.WEST) {
             point.setLocation(getPosition().getX() - 1, getPosition().getY());
         }
 
         return SimulatorPanel.robotOn(point);
     }
 
     @Override
     public String getConsoleTag() {
         return "[SIMULATOR]";
     }
 
     @Override
     public int getInfraRedSensorValue() {
         if (mapGraphLoaded == null) {
             return (int) SimulationSensorData.getMNoInfraRedIS();
         } else {
             return recursiveInfraRed(getMatrixPosition(), 3);
         }
     }
 
     private double[] getLightSensorCoordinates() {
         final double[] coordinates = new double[2];
         coordinates[0] = (getPosition().getX() + lightSensorDistanceFromAxis
                 * Math.cos(Math.toRadians(getAngle())));
         coordinates[1] = (getPosition().getX() + lightSensorDistanceFromAxis
                 * Math.sin(Math.toRadians(getAngle())));
         return coordinates;
     }
 
     @Override
     public int getLightSensorValue() {
         double mean = 0;
         double standardDeviation = 1;
         final double[] coordinates = getLightSensorCoordinates();
 
         // check on which sort of underground your are standing
         // and adjust the mean and standardDeviation accordingly
         if (onEmptyTile(coordinates[0], coordinates[1])) {
             mean = SimulationSensorData.getMEmptyPanelLS();
             standardDeviation = SimulationSensorData.getSDEmptyPanelLS();
         } else if (onWhiteLine(coordinates[0], coordinates[1])) {
             mean = SimulationSensorData.getMWhiteLineLS();
             standardDeviation = SimulationSensorData.getSDWhiteLineLS();
         } else if (onBarcodeTile(coordinates[0], coordinates[1])) {
             final int color = ((Barcode) (mapGraphLoaded
                     .getTile(getMatrixPosition()).getContent())).getColorValue(
                     coordinates[0] % sizeTile(), coordinates[1] % sizeTile());
             mean = SimulationSensorData.getMBarcodeTileLS(color);
             standardDeviation = SimulationSensorData.getSDBarcodeTileLS(color);
         }
         return (int) Math.round(mean
                 + (new Random().nextGaussian() * standardDeviation));
     }
 
     @Override
     public int getUltraSensorValue() {
         try {
             return (int) Math.round(calculateDistanceToWall()
                     + (new Random().nextGaussian() * SimulationSensorData
                             .getSDUS()));
         } catch (Exception e) {
             System.out
                     .println("Error in SimulationPilot.getUltraSensorValue()!");
             return 0;
         }
     }
 
     private double[] getUltrasonicSensorCoordinates() {
         final double[] coordinates = new double[2];
         coordinates[0] = (getPosition().getX() - ultrasonicSensorDistanceFromAxis
                 * Math.cos(Math.toRadians(getAngle())));
         coordinates[1] = (getPosition().getX() - ultrasonicSensorDistanceFromAxis
                 * Math.sin(Math.toRadians(getAngle())));
         return coordinates;
     }
 
     // True if the robot is not on an edge, but on a tile containing a barcode.
     private boolean onBarcodeTile(final double x, final double y) {
         return !pointOnEdge(x, y)
                 && mapGraphLoaded != null
                 && (mapGraphLoaded.getTile(getMatrixPosition()).getContent() instanceof Barcode);
     }
 
     // True if the robot is not on an edge, but on a tile without a content.
     private boolean onEmptyTile(final double x, final double y) {
         return !pointOnEdge(x, y)
                 && (mapGraphLoaded == null || mapGraphLoaded.getTile(
                         getMatrixPosition()).getContent() == null);
     }
 
     // True if the robot is on an edge and this edge is not a wall
     private boolean onWhiteLine(final double x, final double y) {
         return pointOnEdge(x, y)
                 && (mapGraphLoaded == null || mapGraphLoaded
                         .getTile(getMatrixPosition())
                         .getEdgeAt(Orientation.calculateOrientation(getAngle()))
                         .getObstruction() != Obstruction.WALL);
     }
 
     @Override
     protected int readBarcode() {
         if (mapGraphLoaded.getTile(getMatrixPosition()).getContent() == null
                 || !(mapGraphLoaded.getTile(getMatrixPosition()).getContent() instanceof Barcode)) {
             return -1;
         }
         int value = mapGraphLoaded.getTile(getMatrixPosition()).getContent()
                 .getValue();
         return value;
     }
 
     private int recursiveInfraRed(Point currentPoint, int nbOfTilesToLook) {
         if (nbOfTilesToLook < 0
                 || mapGraphLoaded.getTile(currentPoint)
                         .getEdgeAt(getOrientation()).getObstruction() == Obstruction.WALL
                 || mapGraphLoaded.getTile(currentPoint)
                         .getEdgeAt(getOrientation()).getObstruction() == Obstruction.SEESAW_DOWN) {
             return (int) SimulationSensorData.getMNoInfraRedIS();
         } else if (mapGraphLoaded.getTile(currentPoint)
                 .getEdgeAt(getOrientation()).getObstruction() == Obstruction.SEESAW_UP) {
             return (int) SimulationSensorData.getMSeesawIS();
         } else {
             return recursiveInfraRed(getOrientation().getNext(currentPoint),
                     nbOfTilesToLook--);
         }
     }
 
     @Override
     public void travel(final double distance) {
         super.travel(distance);
         if (readBarcodes
                 && !permaBarcodeStop
                 && mapGraphLoaded.getTile(getMatrixPosition()).getContent() instanceof Barcode) {
             setBusyExecutingBarcode(true);
             pilotActions.barcodeFound();
             setBusyExecutingBarcode(false);
         }
     }
 }
