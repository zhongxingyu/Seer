 package Model;
 
 /**
  * Represents a physical lump of concrete that can be declared as a runway
  * Contains two declared runways and an optional obstacle
  * 
  * @author Oscar & Kelvin
  */
 public class PhysicalRunway {
 
 	public static int DEFAULT = 0;
 	public static int REDECLARED = 1;
 
 	private String id;
 	private Runway[] runway;
 	private Obstacle obstacle;
 
 	// Metres are used for these measurements
 	private double distanceAwayFromThreshold, distanceAwayFromCenterLine;
 	private double[] runwayStripWidth, clearedAndGradedWidth, RESA, stopway,
 			blastAllowance, angleOfSlope;
 
 	private boolean closeToA;
 
 	@Override
 	public boolean equals(Object obj) {
 		return this.id.equals(((PhysicalRunway) obj).id);
 	}
 
 	/**
 	 * Default constructor for Physical Runway
 	 * 
 	 * @param identifier
 	 *            The name of the physical runway
 	 * @param runwayOne
 	 *            The first runway
 	 * @param runwayTwo
 	 *            The second runway
 	 */
 	public PhysicalRunway(String identifier, Runway runwayOne, Runway runwayTwo) {
 		runway = new Runway[2];
 		runway[0] = (runwayOne == null ? new Runway("L", 3900, 3900, 3900,
 				3500, 0) : runwayOne);
 		runway[1] = (runwayTwo == null ? new Runway("R", 3900, 3900, 3900,
 				3500, 0) : runwayTwo);
 		id = (identifier == null ? runway[0].getName() + "-"
 				+ runway[1].getName() : identifier);
 
 		this.clearedAndGradedWidth = new double[2];
 		this.clearedAndGradedWidth[DEFAULT] = 75;
 		this.clearedAndGradedWidth[REDECLARED] = 75;
 
 		this.runwayStripWidth = new double[2];
 		this.runwayStripWidth[DEFAULT] = 150;
 		this.runwayStripWidth[REDECLARED] = 150;
 
 		this.RESA = new double[2];
 		this.RESA[DEFAULT] = 240;
 		this.RESA[REDECLARED] = 240;
 
 		this.stopway = new double[2];
 		this.stopway[DEFAULT] = 60;
 		this.stopway[REDECLARED] = 60;
 
 		this.blastAllowance = new double[2];
 		this.blastAllowance[DEFAULT] = 300;
 		this.blastAllowance[REDECLARED] = 300;
 
 		this.angleOfSlope = new double[2];
 		this.angleOfSlope[DEFAULT] = 50;
 		this.angleOfSlope[REDECLARED] = 50;
 	}
 
 	/**
 	 * @param index
 	 *            Which runway to return
 	 * @return The runway
 	 */
 	public Runway getRunway(int index) {
 		return runway[index];
 	}
 
 	/**
 	 * @return The runway's id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            The new id for the physical runway
 	 */
 	public void setId(String id) {
 		this.id = (id == null ? runway[0].getName() + "-" + runway[1].getName()
 				: id);
 	}
 
 	/**
 	 * @return The width of the runway, cleared and graded area
 	 */
 	public double getClearedAndGradedWidth() {
 		return clearedAndGradedWidth[REDECLARED];
 	}
 
 	/**
 	 * @param clearAndGradedWidth
 	 *            The new width of the Clear and Graded area
 	 */
 	public void setClearedAndGradedWidth(double clearedAndGradedWidth) {
 		if (clearedAndGradedWidth >= 0)
 			this.clearedAndGradedWidth[REDECLARED] = clearedAndGradedWidth;
 	}
 
 	/**
 	 * Resets the values of the width of the runway, cleared and graded area
 	 */
 	public void resetClearedAndGradedWidth() {
 		this.clearedAndGradedWidth[REDECLARED] = this.clearedAndGradedWidth[DEFAULT];
 	}
 
 	/**
 	 * @return The width of the runway, cleared and graded area and runway strip
 	 */
 	public double getRunwayStripWidth() {
 		return runwayStripWidth[REDECLARED];
 	}
 
 	/**
 	 * @param runwayStripwidth
 	 *            The new width of the runway strip
 	 */
 	public void setRunwayStripWidth(double runwayStripWidth) {
 		if (runwayStripWidth >= 0) {
 			this.runwayStripWidth[REDECLARED] = runwayStripWidth;
 			calculateParameters();
 		}
 	}
 
 	/**
 	 * Reset the values of the width of the runway, cleared and graded area and
 	 * runway strip
 	 */
 	public void resetRunwayStripWidth() {
 		this.runwayStripWidth[REDECLARED] = this.runwayStripWidth[DEFAULT];
 		calculateParameters();
 	}
 
 	/**
 	 * @return Value of RESA
 	 */
 	public double getRESA() {
 		return this.RESA[REDECLARED];
 	}
 
 	/**
 	 * @param aSDA
 	 *            New value of RESA
 	 */
 	public void setRESA(double resa) {
 		if (resa >= 0) {
 			this.RESA[REDECLARED] = resa;
 			calculateParameters();
 		}
 	}
 
 	/**
 	 * Reset the values of the RESA
 	 */
 	public void resetRESA() {
 		this.RESA[REDECLARED] = this.RESA[DEFAULT];
 		calculateParameters();
 	}
 
 	/**
 	 * @return Value of stopway
 	 */
 	public double getStopway() {
 		return this.stopway[REDECLARED];
 	}
 
 	/**
 	 * @param aSDA
 	 *            New value of stopway
 	 */
 	public void setStopway(double stopway) {
 		if (stopway >= 0) {
 			this.stopway[REDECLARED] = stopway;
 			calculateParameters();
 		}
 	}
 
 	/**
 	 * Reset the value of the stopway
 	 */
 	public void resetStopway() {
 		this.stopway[REDECLARED] = this.stopway[DEFAULT];
 		calculateParameters();
 	}
 
 	/**
 	 * @return Value of RESA
 	 */
 	public double getBlastAllowance() {
 		return this.blastAllowance[REDECLARED];
 	}
 
 	/**
 	 * @param blastAllowance
 	 *            The new blast allowance
 	 */
 	public void setBlastAllowance(double blastAllowance) {
 		if (blastAllowance >= 0) {
 			this.blastAllowance[REDECLARED] = blastAllowance;
 			calculateParameters();
 		}
 	}
 
 	/**
 	 * Reset the values of the stopway
 	 */
 	public void resetBlastAllowance() {
 		this.blastAllowance[REDECLARED] = this.blastAllowance[DEFAULT];
 		calculateParameters();
 	}
 
 	/**
 	 * @return Value of angle of slope
 	 */
 	public double getAngleOfSlope() {
 		return this.angleOfSlope[REDECLARED];
 	}
 
 	/**
 	 * @param angleOfSlope
 	 *            New value for angle of slope
 	 */
 	public void setAngleOfSlope(double angleOfSlope) {
 		if (angleOfSlope >= 0) {
 			this.angleOfSlope[REDECLARED] = angleOfSlope;
 			calculateParameters();
 		}
 	}
 
 	/**
 	 * Reset the values of the stopway
 	 */
 	public void resetAngleOfSlope() {
 		this.angleOfSlope[REDECLARED] = this.angleOfSlope[DEFAULT];
 		calculateParameters();
 	}
 
 	/**
 	 * Places a new obstacle on the runway and performs the required
 	 * calculations
 	 * 
 	 * @param obstacle
 	 *            The obstacle to place
 	 * @param distanceAwayFromThreshold
 	 *            The distance the obstacle is from the end of the runway
 	 * @param closeToRunwayName
 	 *            Which end the distance is measured from
 	 */
 	public void placeNewObstacle(Obstacle obstacle,
 			double distanceAwayFromThreshold,
 			double distanceAwayFromCenterLine, String closeToRunwayName) {
 		reset();
 		this.obstacle = obstacle;
 		this.distanceAwayFromThreshold = distanceAwayFromThreshold;
 		this.distanceAwayFromCenterLine = distanceAwayFromCenterLine;
 		setCloserToWhichThreshold(closeToRunwayName);
 		calculateParameters();
 	}
 
 	public Obstacle getObstacle() {
 		return obstacle;
 	}
 
 	public void setObstacle(Obstacle obstacle) {
 		this.obstacle = obstacle;
 		calculateParameters();
 	}
 
 	/**
 	 * @return The distance between the obstacle and the center line of the
 	 *         physical runway
 	 */
 	public double getDistanceAwayFromCenterLine() {
 		return distanceAwayFromCenterLine;
 	}
 
 	/**
 	 * @param distanceAwayFromCenterLine
 	 *            The new distance between the obstacle and the center line of
 	 *            the physical runway
 	 */
 	public void setDistanceAwayFromCenterLine(double distanceAwayFromCenterLine) {
 		this.distanceAwayFromCenterLine = distanceAwayFromCenterLine;
 		calculateParameters();
 	}
 
 	/**
 	 * Specifies which end the obstacle's distance is measured from
 	 * 
 	 * @param closeToRunwayName
 	 *            The runway the obstacle is closest to
 	 */
 	public void setCloserToWhichThreshold(String closeToRunwayName) {
 		closeToA = closeToRunwayName.equals(runway[0].getName());
 		calculateParameters();
 	}
 
 	/**
 	 * @return The runway with the threshold the object is measured from
 	 */
 	public Runway closeTo() {
 		return closeToA ? runway[0] : runway[1];
 	}
 
 	/**
 	 * Removes the obstacle from the runway and resets values
 	 */
 	public void removeObstacleAndReset() {
 		obstacle = null;
 		distanceAwayFromThreshold = 0;
 		distanceAwayFromCenterLine = 0;
 		reset();
 	}
 
 	/**
 	 * Resets the runway numbers to their defaults
 	 */
 	private void reset() {
 		resetRunwayStripWidth();
 		resetClearedAndGradedWidth();
 		resetRESA();
 		resetStopway();
 		resetBlastAllowance();
 		resetAngleOfSlope();
 	}
 
 	/**
 	 * Reset the runway parameters to the defaults
 	 */
 	public void defaultValues() {
 		reset();
 		calculateParameters();
 	}
 
 	/**
 	 * Calculated the redclared runway parameter
 	 */
 	public void calculateParameters() {
 		Runway closeTo = closeToA ? runway[0] : runway[1];
 		Runway awayFrom = closeToA ? runway[1] : runway[0];
 
 		calculateTORATowardsObstacle(closeTo, awayFrom);
 		calculateTORAAwayFromObstacle(closeTo);
 		calculateASDATowardsObstacle(closeTo, awayFrom);
 		calculateASDAAwayFromObstacle(closeTo);
 		calculateTODATowardsObstacle(closeTo, awayFrom);
 		calculateTODAAwayFromObstacle(closeTo);
 		calculateLDATowardsObstacle(closeTo, awayFrom);
 		calculateLDAOverObstacle(closeTo);
 	}
 
 	/**
 	 * @return The distance that the obstalce is from the threshold
 	 */
 	public double getDistanceAwayFromThreshold() {
 		return distanceAwayFromThreshold;
 	}
 
 	/**
 	 * @param distanceAwayFromThreshold
 	 *            The distance that the obstacle is from the threshold
 	 */
 	public void setDistanceAwayFromThreshold(double distanceAwayFromThreshold) {
 		if (distanceAwayFromThreshold >= 0) {
 			this.distanceAwayFromThreshold = distanceAwayFromThreshold;
 			calculateParameters();
 		}
 	}
 
 	/**
 	 * Produces the calculations as a human readable text
 	 * 
 	 * @param runwayName
 	 *            name of runway to perform calculation for
 	 * @return Text of calculations
 	 */
 	public String toCalculation(String runwayName) {
 		StringBuilder result = new StringBuilder();
 		Runway closeTo = closeToA ? runway[0] : runway[1];
 		Runway awayFrom = closeToA ? runway[1] : runway[0];
 
 		if (runwayName.equals(closeTo.getName())) {
 			if (obstacle == null
 					|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 				if (obstacle == null) {
 					result.append("There are no obstacle on runway at the moment.\n");
 				} else {
 					result.append("Obstacle name: " + obstacle.getName() + "\n");
 					result.append("The obstacle is closer to "
 							+ closeTo.getName() + "'s threshold\n");
 					result.append("The obstacle is "
 							+ Math.abs(distanceAwayFromCenterLine)
 							+ " away from center line. No new calculation needed.\n");
 				}
 				result.append("TORA: " + closeTo.getTORA(Runway.DEFAULT)
 						+ "m\n");
 				result.append("ASDA: " + closeTo.getASDA(Runway.DEFAULT)
 						+ "m\n");
 				result.append("TODA: " + closeTo.getTODA(Runway.DEFAULT)
 						+ "m\n");
 				result.append("LDA : " + closeTo.getLDA(Runway.DEFAULT) + "m\n");
 			} else {
 				result.append("Calcuations on runway " + closeTo.getName()
 						+ ":\n");
 				result.append("Obstacle name: " + obstacle.getName() + "\n");
 				result.append("The obstacle is closer to " + closeTo.getName()
 						+ "'s threshold\n");
 				result.append("Obstacle height: " + obstacle.getHeight()
 						+ "m\n");
 				result.append("Distance away from threshold: "
 						+ distanceAwayFromThreshold + "m\n");
 				result.append("Blast allowance: " + blastAllowance[REDECLARED]
 						+ "m\n");
 				result.append("Displaced threshold: "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
 						+ "m\n");
 				result.append("Angle of slope: " + angleOfSlope[REDECLARED]
 						+ "\n");
 				result.append("Stopway: " + stopway[REDECLARED] + "m\n");
 
 				String resa = "";
 
 				if (obstacle.getHeight() * angleOfSlope[REDECLARED] < RESA[REDECLARED]) {
 					result.append("RESA: " + RESA[REDECLARED] + "m\n");
 					resa = String.valueOf(RESA[REDECLARED]);
 				} else {
 					resa = "(" + obstacle.getHeight() + " * "
 							+ angleOfSlope[REDECLARED] + ")";
 				}
 
 				result.append("\n--Any negative result will be assigned as zero--\n");
 				result.append("New TORA : " + closeTo.getTORA(Runway.DEFAULT)
 						+ " - " + distanceAwayFromThreshold + " - "
 						+ blastAllowance[REDECLARED] + " - "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
 						+ " = " + closeTo.getTORA(Runway.REDECLARED) + "m\n");
 				result.append("New ASDA : " + closeTo.getASDA(Runway.DEFAULT)
 						+ " - " + distanceAwayFromThreshold + " - "
 						+ blastAllowance[REDECLARED] + " - "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
 						+ " = " + closeTo.getASDA(Runway.REDECLARED) + "m\n");
 				result.append("New TODA : " + closeTo.getTODA(Runway.DEFAULT)
 						+ " - " + distanceAwayFromThreshold + " - "
 						+ blastAllowance[REDECLARED] + " - "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
 						+ " = " + closeTo.getTODA(Runway.REDECLARED) + "m\n");
 				result.append("New LDA : " + closeTo.getLDA(Runway.DEFAULT)
 						+ " - " + distanceAwayFromThreshold + " - " + resa
 						+ " - " + stopway[REDECLARED] + " = "
 						+ closeTo.getLDA(Runway.REDECLARED) + "m\n");
 			}
 		} else {
 			if (obstacle == null
 					|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 				if (obstacle == null) {
 					result.append("There are no obstacle on runway at the moment.\n");
 				} else {
 					result.append("Obstacle name: " + obstacle.getName() + "\n");
 					result.append("The obstacle is closer to "
 							+ closeTo.getName() + "'s threshold\n");
 					result.append("The obstacle is "
 							+ Math.abs(distanceAwayFromCenterLine)
 							+ " away from center line. No new calculation needed.\n");
 				}
 				result.append("TORA: " + awayFrom.getTORA(Runway.DEFAULT)
 						+ "m\n");
 				result.append("ASDA: " + awayFrom.getASDA(Runway.DEFAULT)
 						+ "m\n");
 				result.append("TODA: " + awayFrom.getTODA(Runway.DEFAULT)
 						+ "m\n");
 				result.append("LDA : " + awayFrom.getLDA(Runway.DEFAULT)
 						+ "m\n");
 			} else {
 
 				result.append("Calcuations on runway " + awayFrom.getName()
 						+ ":\n");
 				result.append("Obstacle name: " + obstacle.getName() + "\n");
 				result.append("The obstacle is closer to " + closeTo.getName()
 						+ "'s threshold\n");
 				result.append("Obstacle height: " + obstacle.getHeight()
 						+ "m\n");
 				result.append("Distance away from threshold: "
 						+ distanceAwayFromThreshold + "m\n");
 				result.append("Blast allowance: " + blastAllowance[REDECLARED]
 						+ "m\n");
 				result.append("Displaced threshold: "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
 						+ "m\n");
 				result.append("Angle of slope: " + angleOfSlope[REDECLARED]
 						+ "\n");
 				result.append("RESA: " + RESA[REDECLARED] + "m\n");
 				result.append("Stopway: " + stopway[REDECLARED] + "m\n");
 
 				String resa = "";
 
 				if (obstacle.getHeight() * angleOfSlope[REDECLARED] < RESA[REDECLARED]) {
 					resa = String.valueOf(RESA[REDECLARED]);
 				} else {
 					resa = "(" + obstacle.getHeight() + " * "
 							+ angleOfSlope[REDECLARED] + ")";
 				}
 
 				result.append("\n--Any negative result will be assigned as zero--\n");
 				result.append("New TORA : " + awayFrom.getTORA(Runway.DEFAULT)
 						+ " - " + distanceAwayFromThreshold + " - " + resa
 						+ " - " + stopway[REDECLARED] + " - "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
 						+ " = " + awayFrom.getTORA(Runway.REDECLARED) + "m\n");
			
				result.append("New TODA : " + awayFrom.getTODA(Runway.DEFAULT)
 						+ " - " + distanceAwayFromThreshold + " - " + resa
 						+ " - " + stopway[REDECLARED] + " - "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
						+ " = " + awayFrom.getTODA(Runway.REDECLARED) + "m\n");
				result.append("New ASDA : " + awayFrom.getASDA(Runway.DEFAULT)
 						+ " - " + distanceAwayFromThreshold + " - " + resa
 						+ " - " + stopway[REDECLARED] + " - "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
						+ " = " + awayFrom.getASDA(Runway.REDECLARED) + "m\n");
 				result.append("New LDA  : " + awayFrom.getLDA(Runway.DEFAULT)
 						+ " - "
 						+ closeTo.getDisplacedThreshold(Runway.REDECLARED)
 						+ " - " + distanceAwayFromThreshold + " - "
 						+ RESA[REDECLARED] + " - " + stopway[REDECLARED]
 						+ " = " + awayFrom.getLDA(Runway.REDECLARED) + "m\n");
 			}
 		}
 		return result.toString();
 	}
 
 	/**
 	 * Calculate the LDA for landing towards the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 * @param awayFrom
 	 *            The runway at the other end on the same physical runway
 	 */
 	private void calculateLDATowardsObstacle(Runway closeTo, Runway awayFrom) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			awayFrom.setLDA(Runway.REDECLARED, awayFrom.getLDA(Runway.DEFAULT));
 		} else {
 			double result = awayFrom.getLDA(Runway.DEFAULT)
 					- closeTo.getDisplacedThreshold(Runway.REDECLARED)
 					- distanceAwayFromThreshold - RESA[REDECLARED]
 					- stopway[REDECLARED];
 			awayFrom.setLDA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 
 	/**
 	 * Calculate the LDA for landing over the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 */
 	private void calculateLDAOverObstacle(Runway closeTo) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			closeTo.setLDA(Runway.REDECLARED, closeTo.getLDA(Runway.DEFAULT));
 		} else {
 			double actualResa = obstacle.getHeight() * angleOfSlope[REDECLARED];
 			if (actualResa < RESA[REDECLARED]) {
 				actualResa = RESA[REDECLARED];
 			}
 			double result = closeTo.getLDA(Runway.DEFAULT)
 					- distanceAwayFromThreshold - actualResa
 					- stopway[REDECLARED];
 			closeTo.setLDA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 
 	/**
 	 * Calculate the TORA away from the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 */
 	private void calculateTORAAwayFromObstacle(Runway closeTo) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			closeTo.setTORA(Runway.REDECLARED, closeTo.getTORA(Runway.DEFAULT));
 		} else {
 			double result = closeTo.getTORA(Runway.DEFAULT)
 					- distanceAwayFromThreshold - blastAllowance[REDECLARED]
 					- closeTo.getDisplacedThreshold(Runway.REDECLARED);
 			closeTo.setTORA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 
 	/**
 	 * Calculate the TORA towards the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 * @param awayFrom
 	 *            The runway at the other end on the same physical runway
 	 */
 	private void calculateTORATowardsObstacle(Runway closeTo, Runway awayFrom) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			awayFrom.setTORA(Runway.REDECLARED,
 					awayFrom.getTORA(Runway.DEFAULT));
 		} else {
 			double actualResa = obstacle.getHeight() * angleOfSlope[REDECLARED];
 			if (actualResa < RESA[REDECLARED]) {
 				actualResa = RESA[REDECLARED];
 			}
 			double result = awayFrom.getTORA(Runway.DEFAULT)
 					- distanceAwayFromThreshold - actualResa
 					- stopway[REDECLARED]
 					- closeTo.getDisplacedThreshold(Runway.REDECLARED);
 			awayFrom.setTORA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 
 	/**
 	 * Calculate the ASDA away from the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 */
 	private void calculateASDAAwayFromObstacle(Runway closeTo) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			closeTo.setASDA(Runway.REDECLARED, closeTo.getASDA(Runway.DEFAULT));
 		} else {
 			double result = closeTo.getASDA(Runway.DEFAULT)
 					- distanceAwayFromThreshold - blastAllowance[REDECLARED]
 					- closeTo.getDisplacedThreshold(Runway.REDECLARED);
 			closeTo.setASDA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 
 	/**
 	 * Calculate the ASDA towards the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 * @param awayFrom
 	 *            The runway at the other end on the same physical runway
 	 */
 	private void calculateASDATowardsObstacle(Runway closeTo, Runway awayFrom) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			awayFrom.setASDA(Runway.REDECLARED,
 					awayFrom.getASDA(Runway.DEFAULT));
 		} else {
 			double actualResa = obstacle.getHeight() * angleOfSlope[REDECLARED];
 			if (actualResa < RESA[REDECLARED]) {
 				actualResa = RESA[REDECLARED];
 			}
			double result = awayFrom.getTORA(Runway.DEFAULT)
 					- distanceAwayFromThreshold - actualResa
 					- stopway[REDECLARED]
 					- closeTo.getDisplacedThreshold(Runway.REDECLARED);
 			awayFrom.setASDA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 
 	/**
 	 * Calculate the TODA away from the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 */
 	private void calculateTODAAwayFromObstacle(Runway closeTo) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			closeTo.setTODA(Runway.REDECLARED, closeTo.getTODA(Runway.DEFAULT));
 		} else {
 			double result = closeTo.getTODA(Runway.DEFAULT)
 					- distanceAwayFromThreshold - blastAllowance[REDECLARED]
 					- closeTo.getDisplacedThreshold(Runway.REDECLARED);
 			closeTo.setTODA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 
 	/**
 	 * Calculate the TODA towards the obstacle
 	 * 
 	 * @param closeTo
 	 *            The runway with threshold closer to the obstacle
 	 * @param awayFrom
 	 *            The runway at the other end on the same physical runway
 	 */
 	private void calculateTODATowardsObstacle(Runway closeTo, Runway awayFrom) {
 		if (obstacle == null
 				|| Math.abs(distanceAwayFromCenterLine) > runwayStripWidth[REDECLARED]) {
 			awayFrom.setTODA(Runway.REDECLARED,
 					awayFrom.getTODA(Runway.DEFAULT));
 		} else {
 			double actualResa = obstacle.getHeight() * angleOfSlope[REDECLARED];
 			if (actualResa < RESA[REDECLARED]) {
 				actualResa = RESA[REDECLARED];
 			}
			double result = awayFrom.getTORA(Runway.DEFAULT)
 					- distanceAwayFromThreshold - actualResa
 					- stopway[REDECLARED]
 					- closeTo.getDisplacedThreshold(Runway.REDECLARED);
 			awayFrom.setTODA(Runway.REDECLARED, (result >= 0 ? result : 0));
 		}
 	}
 }
