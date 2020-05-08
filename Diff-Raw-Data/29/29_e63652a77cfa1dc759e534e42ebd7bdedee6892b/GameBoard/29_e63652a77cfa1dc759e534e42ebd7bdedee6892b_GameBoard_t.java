 package com.difane.games.ticktacktoe;
 
 import java.util.Vector;
 
 import com.difane.games.ticktacktoe.exceptions.GameBoardImpossibleException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLineLengthException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLinePointsCountException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLinePositionException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLineRequirementsException;
 import com.livescribe.afp.Scale;
 import com.livescribe.geom.Point;
 import com.livescribe.geom.PolyLine;
 import com.livescribe.geom.Rectangle;
 import com.livescribe.penlet.Logger;
 
 public class GameBoard {
 
 	private PolyLine firstVerticalLine;
 	private int firstVerticalLineLength;
 	private PolyLine secondVerticalLine;
 	private int secondVerticalLineLength;
 	private PolyLine firstHorizontalLine;
 	private int firstHorizontalLineLength;
 	private PolyLine secondHorizontalLine;
 	private int secondHorizontalLineLength;
 	private Vector board;
 	private Rectangle boardBox;
 
 	private Logger logger;
 	/**
 	 * Constructor
 	 */
 	public GameBoard(Logger logger) {
 		this.logger = logger;
 		this.firstVerticalLine = null;
 		this.secondVerticalLine = null;
 		this.firstHorizontalLine = null;
 		this.secondHorizontalLine = null;
 
 		this.firstVerticalLineLength = 0;
 		this.secondVerticalLineLength = 0;
 		this.firstHorizontalLineLength = 0;
 		this.secondHorizontalLineLength = 0;
 		
 		this.board = new Vector(10);
 	}
 
 	/**
 	 * Sets first vertical line (lines are from the left to the right)
 	 * 
 	 * @param firstVerticalLine
 	 *            Polyline, that contains 2 points. First point is the top
 	 *            point, second point is the bottom point
 	 * @throws GameBoardLinePointsCountException
 	 * @throws GameBoardLineLengthException
 	 */
 	public void setFirstVerticalLine(PolyLine firstVerticalLine)
 			throws GameBoardLinePointsCountException,
 			GameBoardLineLengthException {
 		/*
 		 * If polyline has other count of points, than 2 - it is an error
 		 */
 		this.validateLinePointsCount(firstVerticalLine);
 
 		/*
 		 * At first - we will normalize vertical line and assume, that line is
 		 * true vertical and was moved from the top point to the bottom, so we
 		 * will use following vertical line coords: Top point - (X of the top
 		 * point, Y of the top point) Bottom point - (X of the top point, Y of
 		 * the bottom point)
 		 */
 		firstVerticalLine.setX(1, firstVerticalLine.getX(0));
 
 		/*
 		 * Now we must check, that line has required length
 		 */
 		int length = firstVerticalLine.getY(1) - firstVerticalLine.getY(0);
 		float lengthInMM = Scale.auToMM(length);
 
 		if (lengthInMM < 10) { // Less than one Centimeter
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_SHORT);
 		}
 
 		this.firstVerticalLine = firstVerticalLine;
 		this.firstVerticalLineLength = length;
 		
 		logger.debug("[BOARD] FirstVerticalLine: "+this.firstVerticalLine);
 	}
 
 	/**
 	 * Sets second vertical line (lines are from the left to the right)
 	 * 
 	 * @param secondVerticalLine
 	 *            Polyline, that contains 2 points. First point is the top
 	 *            point, second point is the bottom point
 	 * @throws GameBoardLinePointsCountException
 	 * @throws GameBoardLineRequirementsException
 	 * @throws GameBoardLineLengthException
 	 * @throws GameBoardLinePositionException
 	 */
 	public void setSecondVerticalLine(PolyLine secondVerticalLine)
 			throws GameBoardLinePointsCountException,
 			GameBoardLineRequirementsException, GameBoardLineLengthException,
 			GameBoardLinePositionException {
 		/*
 		 * If first vertical line was not set - it's error
 		 */
 		if (null == this.firstVerticalLine) {
 			throw new GameBoardLineRequirementsException();
 		}
 		/*
 		 * If polyline has other count of points, than 2 - it is an error
 		 */
 		this.validateLinePointsCount(secondVerticalLine);
 
 		/*
 		 * At first - we will normalize vertical line and assume, that line is
 		 * true vertical and was moved from the top point to the bottom, so we
 		 * will use following vertical line coords: Top point - (X of the top
 		 * point, Y of the top point) Bottom point - (X of the top point, Y of
 		 * the bottom point)
 		 */
 		secondVerticalLine.setX(1, secondVerticalLine.getX(0));
 
 		/*
 		 * Now we must check, that line position is correct.
 		 */
 		/*
 		 * 1. Line must be at the right of the first line
 		 */
 
 		if (secondVerticalLine.getX(0) <= firstVerticalLine.getX(0)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_AT_THE_RIGHT);
 		}
 
 		/*
 		 * 2. Distance between lines must be greater than 1/4 of the first line
 		 * length and less than 1/2 of the first line length
 		 */
 		int lineDistance = secondVerticalLine.getX(0)
 				- this.firstVerticalLine.getX(0);
 
 		if ((lineDistance < this.firstVerticalLineLength / 4)
 				|| (lineDistance > this.firstVerticalLineLength / 2)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_NEAR_THE_OTHER_LINES);
 		}
 
 		/*
 		 * 3. If YF is Y of the top point of the first line and YS is Y of the
 		 * top point of the second line and YFL is length of the first line,
 		 * than YS must be greater, than YF-0.25YFL and YS must be less than
 		 * YF+0.25YFL
 		 */
 		int minY = (int) (this.firstVerticalLine.getY(0) - this.firstVerticalLineLength / 4);
 		int maxY = (int) (this.firstVerticalLine.getY(0) + this.firstVerticalLineLength / 4);
 
 		if (secondVerticalLine.getY(0) < minY) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_NEAR_THE_OTHER_LINES);
 		}
 		if (secondVerticalLine.getY(0) > maxY) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_NEAR_THE_OTHER_LINES);
 		}
 
 		/*
 		 * Now we must check, that line has required length
 		 */
 		int length = secondVerticalLine.getY(1) - secondVerticalLine.getY(0);
 		float lengthInMM = Scale.auToMM(length);
 
 		if (lengthInMM < 10) { // Less than one Centimeter
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_SHORT);
 		}
 
 		int minLength = (this.firstVerticalLineLength * 2) / 3;
 		int maxLength = (this.firstVerticalLineLength * 5) / 4;
 
 		if (length < minLength) {
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_SHORT);
 		}
 		if (length > maxLength) {
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_LONG);
 		}
 
 		this.secondVerticalLine = secondVerticalLine;
 		this.secondVerticalLineLength = length;
 		
 		logger.debug("[BOARD] secondVerticalLine: "+this.secondVerticalLine);
 	}
 
 	/**
 	 * Sets first horizontal line (lines are from the top to the bottom)
 	 * 
 	 * @param firstHorizontalLine
 	 *            Polyline, that contains 2 points. First point is the left
 	 *            point, second point is the right point
 	 * @throws GameBoardLineRequirementsException
 	 * @throws GameBoardLinePointsCountException
 	 * @throws GameBoardLinePositionException
 	 * @throws GameBoardLineLengthException
 	 */
 	public void setFirstHorizontalLine(PolyLine firstHorizontalLine)
 			throws GameBoardLineRequirementsException,
 			GameBoardLinePointsCountException, GameBoardLinePositionException,
 			GameBoardLineLengthException {
 		/*
 		 * If first and second vertical line was not set - it's error
 		 */
 		if (null == this.firstVerticalLine || null == this.secondVerticalLine) {
 			throw new GameBoardLineRequirementsException();
 		}
 		/*
 		 * If polyline has other count of points, than 2 - it is an error
 		 */
 		this.validateLinePointsCount(firstHorizontalLine);
 
 		/*
 		 * At first - we will normalize horizontal line and assume, that line is
 		 * true horizontal and was moved from the left point to the right, so we
 		 * will use following horizontal line coords: Left point - (X of the
 		 * left point, Y of the left point) Right point - (X of the right point,
 		 * Y of the left point)
 		 */
 		firstHorizontalLine.setY(1, firstHorizontalLine.getY(0));
 
 		/*
 		 * Now we must check, that line position is correct.
 		 */
 		/*
 		 * 1. Line must start at the left from first vertical line and end at
 		 * the right from second vertical line
 		 */
 		if (firstHorizontalLine.getX(0) > this.firstVerticalLine.getX(0)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_AT_THE_LEFT);
 		}
 
 		if (firstHorizontalLine.getX(1) < this.secondVerticalLine.getX(0)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_AT_THE_RIGHT);
 		}
 
 		/*
 		 * 2. Distance by X between start of the first horizontal line and first
 		 * vertical line must be greater than 1/4 of the first line length and
 		 * less than 1/2 of the first line length
 		 */
 
 		int distance = this.firstVerticalLine.getX(0)
 				- firstHorizontalLine.getX(0);
 
 		int minDistance = (int) (this.firstVerticalLineLength / 4);
 		int maxDistance = (int) (this.firstVerticalLineLength / 2);
 
 		if (distance < minDistance || distance > maxDistance) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_NEAR_THE_OTHER_LINES);
 		}
 
 		/*
 		 * Now we must check, that line has required length
 		 */
 		int length = firstHorizontalLine.getX(1) - firstHorizontalLine.getX(0);
 		float lengthInMM = Scale.auToMM(length);
 
 		if (lengthInMM < 10) { // Less than one Centimeter
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_SHORT);
 		}
 
 		int minLength = (this.firstVerticalLineLength * 2) / 3;
 		int maxLength = (this.firstVerticalLineLength * 5) / 4;
 
 		if (length < minLength) {
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_SHORT);
 		}
 		if (length > maxLength) {
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_LONG);
 		}
 
 		this.firstHorizontalLine = firstHorizontalLine;
 		this.firstHorizontalLineLength = length;
 		
 		logger.debug("[BOARD] firstHorizontalLine: "+this.firstHorizontalLine);
 	}
 
 	/**
 	 * Sets second horizontal line (lines are from the top to the bottom)
 	 * 
 	 * @param secondHorizontalLine
 	 *            Polyline, that contains 2 points. First point is the left
 	 *            point, second point is the right point
 	 * @throws GameBoardLineRequirementsException
 	 * @throws GameBoardLinePointsCountException
 	 * @throws GameBoardLinePositionException
 	 * @throws GameBoardLineLengthException
 	 */
 	public void setSecondHorizontalLine(PolyLine secondHorizontalLine)
 			throws GameBoardLineRequirementsException,
 			GameBoardLinePointsCountException, GameBoardLinePositionException,
 			GameBoardLineLengthException {
 
 		/*
 		 * If first and second vertical and first horizontal lines was not set -
 		 * it's error
 		 */
 		if (null == this.firstVerticalLine || null == this.secondVerticalLine
 				|| null == this.firstHorizontalLine) {
 			throw new GameBoardLineRequirementsException();
 		}
 		/*
 		 * If polyline has other count of points, than 2 - it is an error
 		 */
 		this.validateLinePointsCount(secondHorizontalLine);
 
 		/*
 		 * At first - we will normalize horizontal line and assume, that line is
 		 * true horizontal and was moved from the left point to the right, so we
 		 * will use following horizontal line coords: Left point - (X of the
 		 * left point, Y of the left point) Right point - (X of the right point,
 		 * Y of the left point)
 		 */
 		secondHorizontalLine.setY(1, secondHorizontalLine.getY(0));
 
 		/*
 		 * Now we must check, that line position is correct.
 		 */
 		/*
 		 * 1. Line must start at the left from first vertical line and end at
 		 * the right from second vertical line
 		 */
 		if (secondHorizontalLine.getX(0) > this.firstVerticalLine.getX(0)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_AT_THE_LEFT);
 		}
 
 		if (secondHorizontalLine.getX(1) < this.secondVerticalLine.getX(0)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_AT_THE_RIGHT);
 		}
 
 		/*
 		 * 2. Line must be at the bottom of the first horizontal line
 		 */
 
 		if (secondHorizontalLine.getY(0) <= firstHorizontalLine.getY(0)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_AT_THE_BOTTOM);
 		}
 
 		/*
 		 * 3. Distance between lines must be greater than 1/4 of the first line
 		 * length and less than 1/2 of the first line length
 		 */
 		int lineDistance = secondHorizontalLine.getY(0)
 				- this.firstHorizontalLine.getY(0);
 
 		if ((lineDistance < this.firstHorizontalLineLength / 4)
 				|| (lineDistance > this.firstHorizontalLineLength / 2)) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_NEAR_THE_OTHER_LINES);
 		}
 
 		/*
 		 * 4. If XF is X of the left point of the first line and XS is X of the
 		 * left point of the second line and XFL is length of the first line,
 		 * than XS must be greater, than XF-0.25XFL and XS must be less than
 		 * XF+0.25XFL
 		 */
 		int minX = (int) (this.firstHorizontalLine.getX(0) - this.firstHorizontalLineLength / 4);
 		int maxX = (int) (this.firstHorizontalLine.getX(0) + this.firstHorizontalLineLength / 4);
 
 		if (secondHorizontalLine.getX(0) < minX) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_NEAR_THE_OTHER_LINES);
 		}
 		if (secondHorizontalLine.getX(0) > maxX) {
 			throw new GameBoardLinePositionException(
 					GameBoardLinePositionException.REASON_MUST_BE_NEAR_THE_OTHER_LINES);
 		}
 
 		/*
 		 * Now we must check, that line has required length
 		 */
 		int length = secondHorizontalLine.getX(1)
 				- secondHorizontalLine.getX(0);
 		float lengthInMM = Scale.auToMM(length);
 
 		if (lengthInMM < 10) { // Less than one Centimeter
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_SHORT);
 		}
 
 		int minLength = (this.firstHorizontalLineLength * 2) / 3;
 		int maxLength = (this.firstHorizontalLineLength * 5) / 4;
 
 		if (length < minLength) {
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_SHORT);
 		}
 		if (length > maxLength) {
 			throw new GameBoardLineLengthException(
 					GameBoardLineLengthException.REASON_LINE_TO_LONG);
 		}
 
 		this.secondHorizontalLine = secondHorizontalLine;
 		this.secondHorizontalLineLength = length;
 		
 		logger.debug("[BOARD] secondHorizontalLine: "+this.secondHorizontalLine);
 	}
 
 	/**
 	 * Calculates board cells coordinates, that then will be used to determine,
 	 * where turn was made
 	 * @throws GameBoardImpossibleException 
 	 */
 	public void calculateBoard() throws GameBoardImpossibleException {
 		// At first calculating 4 points, that are points of intersection
 		// between lines
 		
 		// 1. First point - first vertical line and first horizontal line
 		Point tlPoint = intersection(firstVerticalLine.getX(0),
 				firstVerticalLine.getY(0), firstVerticalLine.getX(1),
 				firstVerticalLine.getY(1), firstHorizontalLine.getX(0),
 				firstHorizontalLine.getY(0), firstHorizontalLine.getX(1),
 				firstHorizontalLine.getY(1));
 		
 		logger.debug("[BOARD] tlPoint: "+tlPoint);
 		
 		// 2. Second point - second vertical line and first horizontal line
 		Point trPoint = intersection(secondVerticalLine.getX(0),
 				secondVerticalLine.getY(0), secondVerticalLine.getX(1),
 				secondVerticalLine.getY(1), firstHorizontalLine.getX(0),
 				firstHorizontalLine.getY(0), firstHorizontalLine.getX(1),
 				firstHorizontalLine.getY(1));
 		
 		logger.debug("[BOARD] trPoint: "+trPoint);
 		
 		// 3. Third point - first vertical line and second horizontal line
 		Point blPoint = intersection(firstVerticalLine.getX(0),
 				firstVerticalLine.getY(0), firstVerticalLine.getX(1),
 				firstVerticalLine.getY(1), secondHorizontalLine.getX(0),
 				secondHorizontalLine.getY(0), secondHorizontalLine.getX(1),
 				secondHorizontalLine.getY(1));
 		
 		logger.debug("[BOARD] blPoint: "+blPoint);
 		
 		// 4. Fourth point - second vertical line and second horizontal line
 		Point brPoint = intersection(secondVerticalLine.getX(0),
 				secondVerticalLine.getY(0), secondVerticalLine.getX(1),
 				secondVerticalLine.getY(1), secondHorizontalLine.getX(0),
 				secondHorizontalLine.getY(0), secondHorizontalLine.getX(1),
 				secondHorizontalLine.getY(1));
 		
 		logger.debug("[BOARD] brPoint: "+brPoint);
 		
 		// If there are no all 4 intersections - board is impossible :)
 		if (null == tlPoint || null == trPoint || null == blPoint
 				|| null == brPoint) {
 			throw new GameBoardImpossibleException();
 		}
 		
 		// Now constructing 9 rectangles for 9 board fields
 		// At first getting four helper values for board corners
 		int topMax = Math.min(firstVerticalLine.getY(0), secondVerticalLine.getY(0));
 		int bottomMax = Math.max(firstVerticalLine.getY(1), secondVerticalLine.getY(1));
 		int leftMax = Math.min(firstHorizontalLine.getX(0), secondHorizontalLine.getX(0));
 		int rightMax = Math.max(firstHorizontalLine.getX(1), secondHorizontalLine.getX(1));
 		
 		logger.debug("[BOARD] topMax: "+topMax);
 		logger.debug("[BOARD] bottomMax: "+bottomMax);
 		logger.debug("[BOARD] leftMax: "+leftMax);
 		logger.debug("[BOARD] rightMax: "+rightMax);
 		
 		// Element with zero index (to be arranged with existing game algo
 		board.addElement(new Rectangle());
 		
 		// Rectangle #1
 		board.addElement(new Rectangle(leftMax, topMax, tlPoint.getX()-leftMax, tlPoint.getY()-topMax));
 		
 		// Rectangle #2
 		board.addElement(new Rectangle(tlPoint.getX(), topMax, trPoint.getX()-tlPoint.getX(), trPoint.getY() - topMax));
 		
 		// Rectangle #3
 		board.addElement(new Rectangle(trPoint.getX(), topMax, rightMax - trPoint.getX(), trPoint.getY() - topMax));
 		
 		// Rectangle #4
 		board.addElement(new Rectangle(leftMax, tlPoint.getY(), tlPoint.getX()-leftMax, blPoint.getY()-tlPoint.getY()));
 		
 		// Rectangle #5
 		board.addElement(new Rectangle(tlPoint.getX(), tlPoint.getY(), trPoint.getX()-tlPoint.getX(), brPoint.getY()-trPoint.getY()));
 		
 		// Rectangle #6
 		board.addElement(new Rectangle(trPoint.getX(), trPoint.getY(), rightMax-trPoint.getX(), brPoint.getY()-trPoint.getY()));
 		
 		// Rectangle #7
 		board.addElement(new Rectangle(leftMax, blPoint.getY(), blPoint.getX()-leftMax, bottomMax-blPoint.getY()));
 		
 		// Rectangle #8
 		board.addElement(new Rectangle(blPoint.getX(), blPoint.getY(), brPoint.getX()-blPoint.getX(), bottomMax-brPoint.getY()));
 		
 		// Rectangle #9
 		board.addElement(new Rectangle(brPoint.getX(), brPoint.getY(), rightMax-brPoint.getX(), bottomMax-brPoint.getY()));
 	}
 
 	/**
 	 * Return field, where turn was made, by coordinates of the user turn Fields
 	 * are counted from the left to the right and from the top to the bottom
 	 * 
 	 * @param p
 	 *            Point, where user has dome his turn
 	 * @return number from 1 to 9, if turn was made to the one of the cells, -1
 	 *         otherwise
 	 */
 	public int getTurnField(Point p) {
 		for (int i = 1; i < board.size(); i++) {
 			Rectangle r = (Rectangle) board.elementAt(i);
 			
 			logger.debug("[LOGIC] Field #"+i+". Checking rectangle: "+r+". Point: "+p);
 			if(r.contains(p.getX(), p.getY()))
 			{
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Validates count of the points in the line. If it differs from 2 throws an
 	 * exception
 	 * 
 	 * @param l
 	 *            Polyline, that must be checked
 	 * @throws GameBoardLinePointsCountException
 	 */
 	private void validateLinePointsCount(PolyLine l)
 			throws GameBoardLinePointsCountException {
 		if (2 != l.getNumberofVertices()) {
 			throw new GameBoardLinePointsCountException();
 		}
 	}
 
 	/**
 	 * Computes the intersection between two lines. The calculated point is
 	 * approximate, since integers are used. If you need a more precise result,
 	 * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
 	 * license). http://www.ahristov.com
 	 * 
 	 * @param x1
 	 *            Point 1 of Line 1
 	 * @param y1
 	 *            Point 1 of Line 1
 	 * @param x2
 	 *            Point 2 of Line 1
 	 * @param y2
 	 *            Point 2 of Line 1
 	 * @param x3
 	 *            Point 1 of Line 2
 	 * @param y3
 	 *            Point 1 of Line 2
 	 * @param x4
 	 *            Point 2 of Line 2
 	 * @param y4
 	 *            Point 2 of Line 2
 	 * @return Point where the segments intersect, or null if they don't
 	 */
 	public Point intersection(int x1, int y1, int x2, int y2, int x3, int y3,
 			int x4, int y4) {
		
		double dx1 = x1;
		double dy1 = y1;
		double dx2 = x2;
		double dy2 = y2;
		double dx3 = x3;
		double dy3 = y3;
		double dx4 = x4;
		double dy4 = y4;
		
		double d = (dx1 - dx2) * (dy3 - dy4) - (dy1 - dy2) * (dx3 - dx4);
 		if (d == 0)
 			return null;
		
		double xi = ((dx3 - dx4) * (dx1 * dy2 - dy1 * dx2) - (dx1 - dx2)
				* (dx3 * dy4 - dy3 * dx4))
 				/ d;
		double yi = ((dy3 - dy4) * (dx1 * dy2 - dy1 * dx2) - (dy1 - dy2)
				* (dx3 * dy4 - dy3 * dx4))
 				/ d;
		
		return new Point((int)xi, (int)yi);
 	}
 
 }
