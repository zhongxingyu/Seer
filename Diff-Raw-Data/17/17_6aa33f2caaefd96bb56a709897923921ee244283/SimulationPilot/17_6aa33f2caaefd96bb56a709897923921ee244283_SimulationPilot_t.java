 package simulator.pilot;
 
 import java.util.Random;
 
 import mapping.Barcode;
 import mapping.Obstruction;
 import mapping.Orientation;
 import mapping.Tile;
 import simulator.SimulationSensorData;
 
 public class SimulationPilot extends AbstractPilot {
     /**
      * Calculates the distance to the first wall the robot will encounter facing
      * its current orientation. Returns 250 if no map is loaded or no wall is
      * found whithin the range of the sensor. The maximum range of the sensor is
      * 120 cm.
      * 
      * By virtually moving forward (the temporary coordinates) en on every
      * border checking whether there is a wall. If so, you calculate the
      * distance to is. If not, keep om moving (the robot doesn't move!)
      */
     /*
      * TODO Waarom is dit met virtueel bewegen? Dit kan toch berekend worden?
      * (lage prioriteit) Uit richting en plaats weet ge de zijden (van het grid)
      * die ge gaat kruisen, die checkt ge dan in de juiste volgorde op de
      * aanwezigheid van een muur.
      */
     private double calculateDistanceToWall() {
         // current temporary position; to check whether there are walls in the
         // direction the robot is facing
         double xTemp = getUltrasonicSensorAbsolute()[0];
         double yTemp = getUltrasonicSensorAbsolute()[1];
         // keep the last temporary position, so you can compare with the current
         // temporary position
         double xTempPrev = xTemp;
         double yTempPrev = yTemp;
 
         // there is no map loaded, so the sensor will detect no walls en returns
         // the maximum value.
         if (getMapGraphLoaded() == null) {
             return 250;
         }
         Tile tileTemp = getMapGraphLoaded().getTile(getRelativePosition());
         int i = 1;
 
         while (i < 148) {
             while (!(Math.abs(xTempPrev % sizeTile() - xTemp % sizeTile()) > 5)
                     && !(Math.abs(yTempPrev % sizeTile() - yTemp % sizeTile()) > 5)) {
                 xTempPrev = xTemp;
                 yTempPrev = yTemp;
 
                 xTemp = (getUltrasonicSensorAbsolute()[0] + i
                         * Math.cos(Math.toRadians(getAngle())));
                 yTemp = (getUltrasonicSensorAbsolute()[1] + i
                         * Math.sin(Math.toRadians(getAngle())));
                 i++;
             }
 
             final Orientation oriTemp = Orientation.defineBorderCrossed(xTemp,
                     yTemp, xTempPrev, yTempPrev, sizeTile());
 
             // the edge you have found, does not contain a wall, you can look
             // right over it.
             // change the current tile to the next tile en move a few steps
             // foreward (with the temporary coordinates).
             if (tileTemp.getEdge(oriTemp).isPassable()) {
                 tileTemp = tileTemp.getEdge(oriTemp).getNeighbour(tileTemp);
                 for (int j = 0; j < 35; j++) {
                     xTempPrev = xTemp;
                     yTempPrev = yTemp;
 
                     xTemp = (getUltrasonicSensorAbsolute()[0] + i
                             * Math.cos(Math.toRadians(getAngle())));
                     yTemp = (getUltrasonicSensorAbsolute()[1] + i
                             * Math.sin(Math.toRadians(getAngle())));
                     i++;
                 }
             } else {
                 return Math
                         .sqrt(Math.pow(
                                 xTemp - getUltrasonicSensorAbsolute()[0], 2)
                                 + Math.pow(yTemp
                                         - getUltrasonicSensorAbsolute()[1], 2));
             }
         }
 
         // no wall is found within the range of the ultrasonic sensor
         return 250;
     }
 
     /**
      * Returns a number from a normal distribution that represents a lightsensor
      * value.
      */
     @Override
     public int getLightSensorValue() {
         // initialisation
         final Random random = new Random();
         double mean = 0;
         double standardDeviation = 1;
         final double[] absLS = getLightSensorAbsolute();
 
         // check on which sort of underground your are standing
         // and adjust the mean and standardDeviation accordingly
         if (onEmptyTile(absLS[0], absLS[1])) {
             mean = SimulationSensorData.getMEmptyPanelLS();
             standardDeviation = SimulationSensorData.getSDEmptyPanelLS();
         } else if (onWhiteLine(absLS[0], absLS[1])) {
             mean = SimulationSensorData.getMWhiteLineLS();
             standardDeviation = SimulationSensorData.getSDWhiteLineLS();
         } else if (onBarcodeTile(absLS[0], absLS[1])) {
             final int color = getMapGraphLoaded()
                     .getTile(getRelativePosition())
                     .getContent()
                     .getColorValue(absLS[0] % sizeTile(), absLS[1] % sizeTile());
             mean = SimulationSensorData.getMBarcodeTileLS(color);
             standardDeviation = SimulationSensorData.getSDBarcodeTileLS(color);
         }
         return (int) Math.round(mean
                 + (random.nextGaussian() * standardDeviation));
     }
 
     @Override
     protected int getRotateSleepTime(double angle) {
         return 5 - getSpeed();
     }
 
     @Override
     protected int getTravelSleepTime(double distance) {
         switch (getSpeed()) {
         case 1:
             return 10;
         case 2:
             return 7;
         case 3:
             return 5;
         case 4:
             return 3;
         }
         return 0;
     }
 
     @Override
     public int getUltraSensorValue() {
         final Random random = new Random();
 
         final double mean = calculateDistanceToWall();
         final double standardDeviation = SimulationSensorData.getSDUS();
 
         return (int) Math.round(mean
                 + (random.nextGaussian() * standardDeviation));
     }
 
     /**
      * True if the robot is not on an edge, but on a tile containing a barcode.
      */
     private boolean onBarcodeTile(final double x, final double y) {
         if (getMapGraphLoaded() == null) {
             // System.out.println("b: /");
             return false;
         } else {
             // System.out.println("b: " + (!this.onEdge(x,y) &&
             // (this.getMapGraph().getContentCurrentTile() instanceof
             // Barcode)));
             return !pointOnEdge(x, y)
                     && (getMapGraphLoaded().getTile(getRelativePosition())
                             .getContent() instanceof Barcode);
         }
     }
 
     /**
      * True if the robot is not on an edge, but on a tile without a content.
      */
     private boolean onEmptyTile(final double x, final double y) {
         return (!pointOnEdge(x, y) && getMapGraphLoaded() == null)
                 || (!pointOnEdge(x, y) && getMapGraphLoaded().getTile(
                         getRelativePosition()).getContent() == null);
 
     }
 
     /**
      * True if the robot is on an edge and this edge is not a wall
      */
     private boolean onWhiteLine(final double x, final double y) {
         // System.out.println("w: " + (this.onEdge(x,y) && (this.getMapGraph()
         // == null ||
         // this.getMapGraph().getObstruction(Orientation.calculateOrientation(x,
         // y, this.getAlpha())) != Obstruction.WALL)));
         return pointOnEdge(x, y)
                 && (getMapGraphLoaded() == null || getMapGraphLoaded()
                         .getObstruction(getRelativePosition(),
                                 Orientation.calculateOrientation(getAngle())) != Obstruction.WALL);
 
     }
 
     @Override
     public String getConsoleTag() {
         return "[SIMULATOR]";
     }

	@Override
	public void recieveMessage(String message) {
		// TODO Auto-generated method stub
		
	}
 }
