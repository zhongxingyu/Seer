 package com.larvalabs.svgandroid.util;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.anddev.andengine.util.Debug;
 
 import com.larvalabs.svgandroid.adt.SVGProperties;
 
 import android.graphics.Path;
 import android.graphics.Path.FillType;
 
 
 /**
  * Parses a single SVG path and returns it as a <code>android.graphics.Path</code> object.
  * An example path is <code>M250,150L150,350L350,350Z</code>, which draws a triangle.
  *
  * @see <a href="http://www.w3.org/TR/SVG/paths.html">Specification</a>.
  * 
  * @author Nicolas Gramlich
  * @since 17:16:39 - 21.05.2011
  */
 public class SVGPathParser {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private String mString;
 	private int mLength;
 	private int mPosition;
 	private char mCurrentChar;
 
 	private final PathParserHelper mPathParserHelper = new PathParserHelper();
 
 	private Path mPath;
 	private Character mCommand = null;
 	private int mCommandStart = 0;
 	private final Queue<Float> mCommandParameters = new LinkedList<Float>();
 
 	private float mSubPathStartX;
 	private float mSubPathStartY;
 	private float mLastX;
 	private float mLastY;
 	private float mLastCubicBezierX2;
 	private float mLastCubicBezierY2;
 	private float mLastQuadraticBezierX2;
 	private float mLastQuadraticBezierY2;
 
 	//	private final RectF mRectF = new RectF();
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	/**
 	 * Uppercase rules are absolute positions, lowercase are relative.
 	 * Types of path rules:
 	 * <p/>
 	 * <ol>
 	 * <li>M/m - (x y)+ - Move to (without drawing)
 	 * <li>Z/z - (no params) - Close path (back to starting point)
 	 * <li>L/l - (x y)+ - Line to
 	 * <li>H/h - x+ - Horizontal ine to
 	 * <li>V/v - y+ - Vertical line to
 	 * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier to
 	 * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes the x2, y2 from previous C/S is the x1, y1 of this bezier)
 	 * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier to
 	 * <li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control point is "reflection" of last one w.r.t. to current point)
 	 * <li>A/a - ... - Arc to</li>
 	 * </ol>
 	 * <p/>
 	 * Numbers are separate by whitespace, comma or nothing at all (!) if they are self-delimiting, (ie. begin with a - sign)
 	 */
 	public Path parse(final SVGProperties pSVGProperties) {
 		final String pathString = pSVGProperties.getStringProperty("d");
 		if(pathString == null) {
 			return null;
 		}
 		
 		final String fillrule = pSVGProperties.getStringProperty("fill-rule");
 		if(fillrule != null) {
 			if("evenodd".equals(fillrule)) {
 				this.mPath.setFillType(FillType.EVEN_ODD);
 			} else {
 				this.mPath.setFillType(FillType.WINDING);
 			}
 			
 			/*
 			 *  TODO Check against: 
 			 *  http://www.w3.org/TR/SVG/images/painting/fillrule-nonzero.svg / http://www.w3.org/TR/SVG/images/painting/fillrule-nonzero.png
 			 *  http://www.w3.org/TR/SVG/images/painting/fillrule-evenodd.svg / http://www.w3.org/TR/SVG/images/painting/fillrule-evenodd.png
 			 */
 		}
 		
		this.mString = pathString.trim();
		this.mLastX = 0;
		this.mLastY = 0;
		this.mLastCubicBezierX2 = 0;
		this.mLastCubicBezierY2 = 0;
		this.mCommand = null;
		this.mCommandParameters.clear();
		this.mPath = new Path();
		if(this.mString.length() == 0) {
			return this.mPath;
		}
 		this.mCurrentChar = this.mString.charAt(0);
 
 		this.mPosition = 0;
 		this.mLength = this.mString.length();
 		while (this.mPosition < this.mLength) {
 			try {
 				this.mPathParserHelper.skipWhitespace();
 				if (Character.isLetter(this.mCurrentChar) && (this.mCurrentChar != 'e') && (this.mCurrentChar != 'E')) {
 					this.processCommand();
 
 					this.mCommand = this.mCurrentChar;
 					this.mCommandStart = this.mPosition;
 					this.mPathParserHelper.advance();
 				} else {
 					final float parameter = this.mPathParserHelper.nextFloat();
 					this.mCommandParameters.add(parameter);
 				}
 			} catch(final Throwable t) {
 				throw new IllegalArgumentException("Error parsing: '" + this.mString.substring(this.mCommandStart, this.mPosition) + "'. Command: '" + this.mCommand + "'. Parameters: '" + this.mCommandParameters.size() + "'.", t);
 			}
 		}
 		this.processCommand();
 		return this.mPath;
 	}
 
 	private void processCommand() {
 		if (this.mCommand != null) {
 			// Process command
 			this.generatePathElement();
 			this.mCommandParameters.clear();
 		}
 	}
 
 	private void generatePathElement() {
 		boolean wasCubicBezierCurve = false;
 		boolean wasQuadraticBezierCurve = false;
 		switch (this.mCommand) {
 			case 'm':
 				this.generateMove(false);
 				break;
 			case 'M':
 				this.generateMove(true);
 				break;
 			case 'l':
 				this.generateLine(false);
 				break;
 			case 'L':
 				this.generateLine(true);
 				break;
 			case 'h':
 				this.generateHorizontalLine(false);
 				break;
 			case 'H':
 				this.generateHorizontalLine(true);
 				break;
 			case 'v':
 				this.generateVerticalLine(false);
 				break;
 			case 'V':
 				this.generateVerticalLine(true);
 				break;
 			case 'c':
 				this.generateCubicBezierCurve(false);
 				wasCubicBezierCurve = true;
 				break;
 			case 'C':
 				this.generateCubicBezierCurve(true);
 				wasCubicBezierCurve = true;
 				break;
 			case 's':
 				this.generateSmoothCubicBezierCurve(false);
 				wasCubicBezierCurve = true;
 				break;
 			case 'S':
 				this.generateSmoothCubicBezierCurve(true);
 				wasCubicBezierCurve = true;
 				break;
 			case 'q':
 				this.generateQuadraticBezierCurve(false);
 				wasQuadraticBezierCurve = true;
 				break;
 			case 'Q':
 				this.generateQuadraticBezierCurve(true);
 				wasQuadraticBezierCurve = true;
 				break;
 			case 't':
 				this.generateSmoothQuadraticBezierCurve(false);
 				wasQuadraticBezierCurve = true;
 				break;
 			case 'T':
 				this.generateSmoothQuadraticBezierCurve(true);
 				wasQuadraticBezierCurve = true;
 				break;
 			case 'a':
 				this.generateArc(false);
 				break;
 			case 'A':
 				this.generateArc(true);
 				break;
 			case 'z':
 			case 'Z':
 				this.generateClose();
 				break;
 			default:
 				throw new RuntimeException("Unexpected SVG command: " + this.mCommand);
 		}
 		if (!wasCubicBezierCurve) {
 			this.mLastCubicBezierX2 = this.mLastX;
 			this.mLastCubicBezierY2 = this.mLastY;
 		}
 		if (!wasQuadraticBezierCurve) {
 			this.mLastQuadraticBezierX2 = this.mLastX;
 			this.mLastQuadraticBezierY2 = this.mLastY;
 		}
 	}
 
 	private void assertParameterCountMinimum(final int pParameterCount) {
 		if (this.mCommandParameters.size() < pParameterCount) {
 			throw new RuntimeException("Incorrect parameter count: '" + this.mCommandParameters.size() + "'. Expected at least: '" + pParameterCount + "'.");
 		}
 	}
 
 	private void assertParameterCount(final int pParameterCount) {
 		if (this.mCommandParameters.size() != pParameterCount) {
 			throw new RuntimeException("Incorrect parameter count: '" + this.mCommandParameters.size() + "'. Expected: '" + pParameterCount + "'.");
 		}
 	}
 
 	private void generateMove(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(2);
 		final float x = this.mCommandParameters.poll();
 		final float y = this.mCommandParameters.poll();
 		/** Moves the line from mLastX,mLastY to x,y. */
 		if (pAbsolute) {
 			this.mPath.moveTo(x, y);
 			this.mLastX = x;
 			this.mLastY = y;
 		} else {
 			this.mPath.rMoveTo(x, y);
 			this.mLastX += x;
 			this.mLastY += y;
 		}
 		this.mSubPathStartX = this.mLastX;
 		this.mSubPathStartY = this.mLastY;
 		if(this.mCommandParameters.size() >= 2) {
 			this.generateLine(pAbsolute);
 		}
 	}
 
 	private void generateLine(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(2);
 		/** Draws a line from mLastX,mLastY to x,y. */
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 2) {
 				final float x = this.mCommandParameters.poll();
 				final float y = this.mCommandParameters.poll();
 				this.mPath.lineTo(x, y);
 				this.mLastX = x;
 				this.mLastY = y;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 2) {
 				final float x = this.mCommandParameters.poll();
 				final float y = this.mCommandParameters.poll();
 				this.mPath.rLineTo(x, y);
 				this.mLastX += x;
 				this.mLastY += y;
 			}
 		}
 	}
 
 	private void generateHorizontalLine(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(1);
 		/** Draws a horizontal line to the point defined by mLastY and x. */
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 1) {
 				final float x = this.mCommandParameters.poll();
 				this.mPath.lineTo(x, this.mLastY);
 				this.mLastX = x;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 1) {
 				final float x = this.mCommandParameters.poll();
 				this.mPath.rLineTo(x, 0);
 				this.mLastX += x;
 			}
 		}
 	}
 
 	private void generateVerticalLine(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(1);
 		/** Draws a vertical line to the point defined by mLastX and y. */
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 1) {
 				final float y = this.mCommandParameters.poll();
 				this.mPath.lineTo(this.mLastX, y);
 				this.mLastY = y;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 1) {
 				final float y = this.mCommandParameters.poll();
 				this.mPath.rLineTo(0, y);
 				this.mLastY += y;
 			}
 		}
 	}
 
 	private void generateCubicBezierCurve(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(6);
 		/** Draws a cubic bezier curve from current pen point to x,y.
 		 * x1,y1 and x2,y2 are start and end control points of the curve. */
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 6) {
 				final float x1 = this.mCommandParameters.poll();
 				final float y1 = this.mCommandParameters.poll();
 				final float x2 = this.mCommandParameters.poll();
 				final float y2 = this.mCommandParameters.poll();
 				final float x = this.mCommandParameters.poll();
 				final float y = this.mCommandParameters.poll();
 				this.mPath.cubicTo(x1, y1, x2, y2, x, y);
 				this.mLastCubicBezierX2 = x2;
 				this.mLastCubicBezierY2 = y2;
 				this.mLastX = x;
 				this.mLastY = y;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 6) {
 				final float x1 = this.mCommandParameters.poll() + this.mLastX;
 				final float y1 = this.mCommandParameters.poll() + this.mLastY;
 				final float x2 = this.mCommandParameters.poll() + this.mLastX;
 				final float y2 = this.mCommandParameters.poll() + this.mLastY;
 				final float x = this.mCommandParameters.poll() + this.mLastX;
 				final float y = this.mCommandParameters.poll() + this.mLastY;
 				this.mPath.cubicTo(x1, y1, x2, y2, x, y);
 				this.mLastCubicBezierX2 = x2;
 				this.mLastCubicBezierY2 = y2;
 				this.mLastX = x;
 				this.mLastY = y;
 			}
 		}
 	}
 
 	private void generateSmoothCubicBezierCurve(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(4);
 		/** Draws a cubic bezier curve from the last point to x,y.
 		 * x2,y2 is the end control point.
 		 * The start control point is is assumed to be the same as
 		 * the end control point of the previous curve. */
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 4) {
 				final float x1 = 2 * this.mLastX - this.mLastCubicBezierX2;
 				final float y1 = 2 * this.mLastY - this.mLastCubicBezierY2;
 				final float x2 = this.mCommandParameters.poll();
 				final float y2 = this.mCommandParameters.poll();
 				final float x = this.mCommandParameters.poll();
 				final float y = this.mCommandParameters.poll();
 				this.mPath.cubicTo(x1, y1, x2, y2, x, y);
 				this.mLastCubicBezierX2 = x2;
 				this.mLastCubicBezierY2 = y2;
 				this.mLastX = x;
 				this.mLastY = y;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 4) {
 				final float x1 = 2 * this.mLastX - this.mLastCubicBezierX2;
 				final float y1 = 2 * this.mLastY - this.mLastCubicBezierY2;
 				final float x2 = this.mCommandParameters.poll() + this.mLastX;
 				final float y2 = this.mCommandParameters.poll() + this.mLastY;
 				final float x = this.mCommandParameters.poll() + this.mLastX;
 				final float y = this.mCommandParameters.poll() + this.mLastY;
 				this.mPath.cubicTo(x1, y1, x2, y2, x, y);
 				this.mLastCubicBezierX2 = x2;
 				this.mLastCubicBezierY2 = y2;
 				this.mLastX = x;
 				this.mLastY = y;
 			}
 		}
 	}
 
 	private void generateQuadraticBezierCurve(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(4);
 		/** Draws a quadratic bezier curve from mLastX,mLastY x,y. x1,y1 is the control point.. */
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 4) {
 				final float x1 = this.mCommandParameters.poll();
 				final float y1 = this.mCommandParameters.poll();
 				final float x2 = this.mCommandParameters.poll();
 				final float y2 = this.mCommandParameters.poll();
 				this.mPath.quadTo(x1, y1, x2, y2);
 				this.mLastQuadraticBezierX2 = x2;
 				this.mLastQuadraticBezierY2 = y2;
 				this.mLastX = x2;
 				this.mLastY = y2;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 4) {
 				final float x1 = this.mCommandParameters.poll() + this.mLastX;
 				final float y1 = this.mCommandParameters.poll() + this.mLastY;
 				final float x2 = this.mCommandParameters.poll() + this.mLastX;
 				final float y2 = this.mCommandParameters.poll() + this.mLastY;
 				this.mPath.quadTo(x1, y1, x2, y2);
 				this.mLastQuadraticBezierX2 = x2;
 				this.mLastQuadraticBezierY2 = y2;
 				this.mLastX = x2;
 				this.mLastY = y2;
 			}
 		}
 	}
 
 	private void generateSmoothQuadraticBezierCurve(final boolean pAbsolute) {
 		this.assertParameterCountMinimum(2);
 		/** Draws a quadratic bezier curve from mLastX,mLastY to x,y.
 		 * The control point is assumed to be the same as the last control point used. */
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 2) {
 				final float x1 = 2 * this.mLastX - this.mLastQuadraticBezierX2;
 				final float y1 = 2 * this.mLastY - this.mLastQuadraticBezierY2;
 				final float x2 = this.mCommandParameters.poll();
 				final float y2 = this.mCommandParameters.poll();
 				this.mPath.quadTo(x1, y1, x2, y2);
 				this.mLastQuadraticBezierX2 = x2;
 				this.mLastQuadraticBezierY2 = y2;
 				this.mLastX = x2;
 				this.mLastY = y2;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 2) {
 				final float x1 = 2 * this.mLastX - this.mLastQuadraticBezierX2;
 				final float y1 = 2 * this.mLastY - this.mLastQuadraticBezierY2;
 				final float x2 = this.mCommandParameters.poll() + this.mLastX;
 				final float y2 = this.mCommandParameters.poll() + this.mLastY;
 				this.mPath.quadTo(x1, y1, x2, y2);
 				this.mLastQuadraticBezierX2 = x2;
 				this.mLastQuadraticBezierY2 = y2;
 				this.mLastX = x2;
 				this.mLastY = y2;
 			}
 		}
 	}
 
 	private void generateArc(final boolean pAbsolute) {
 		// TODO This implementation might be useful: https://code.google.com/p/svg-edit/source/browse/trunk/editor/canvg/canvg.js?r=2031#1244
 		this.assertParameterCountMinimum(7);
 		Debug.w("The arc command ('A'/'a') is not supported yet!");
 		if(pAbsolute) {
 			while(this.mCommandParameters.size() >= 7) {
 				final float rx = this.mCommandParameters.poll();
 				final float ry = this.mCommandParameters.poll();
 				final float theta = this.mCommandParameters.poll();
 				final int largeArc = this.mCommandParameters.poll().intValue();
 				final int sweepArc = this.mCommandParameters.poll().intValue();
 				final float x = this.mCommandParameters.poll();
 				final float y = this.mCommandParameters.poll();
 
 				// TODO Implement
 				//				this.mRectF.set(left, top, right, bottom);
 				//				this.mPath.addArc(this.mRectF, startAngle, sweepAngle); // or arcTo?
 				this.mPath.lineTo(x, y);
 
 				this.mLastX = x;
 				this.mLastY = y;
 			}
 		} else {
 			while(this.mCommandParameters.size() >= 7) {
 				final float rx = this.mCommandParameters.poll();
 				final float ry = this.mCommandParameters.poll();
 				final float theta = this.mCommandParameters.poll();
 				final int largeArc = this.mCommandParameters.poll().intValue();
 				final int sweepArc = this.mCommandParameters.poll().intValue();
 				final float x = this.mCommandParameters.poll() + this.mLastX;
 				final float y = this.mCommandParameters.poll() + this.mLastY;
 
 				//				this.mRectF.set(left, top, right, bottom);
 				//				this.mPath.addArc(this.mRectF, startAngle, sweepAngle); // or arcTo?
 				this.mPath.lineTo(x, y);
 
 				// TODO Implement
 				this.mLastX = x;
 				this.mLastY = y;
 			}
 		}
 	}
 
 	private void generateClose() {
 		this.assertParameterCount(0);
 		this.mPath.close();
 		this.mLastX = this.mSubPathStartX;
 		this.mLastY = this.mSubPathStartY;
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 	/**
 	 * Parses numbers from SVG text. Based on the Batik Number Parser (Apache 2 License).
 	 *
 	 * @author Apache Software Foundation
 	 * @author Larva Labs LLC
 	 * @author Nicolas Gramlich
 	 */
 	public class PathParserHelper {
 		// ===========================================================
 		// Constants
 		// ===========================================================
 
 		// ===========================================================
 		// Fields
 		// ===========================================================
 
 		// ===========================================================
 		// Constructors
 		// ===========================================================
 
 		// ===========================================================
 		// Getter & Setter
 		// ===========================================================
 
 		// ===========================================================
 		// Methods for/from SuperClass/Interfaces
 		// ===========================================================
 
 		// ===========================================================
 		// Methods
 		// ===========================================================
 
 		private char read() {
 			if (SVGPathParser.this.mPosition < SVGPathParser.this.mLength) {
 				SVGPathParser.this.mPosition++;
 			}
 			if (SVGPathParser.this.mPosition == SVGPathParser.this.mLength) {
 				return '\0';
 			} else {
 				return SVGPathParser.this.mString.charAt(SVGPathParser.this.mPosition);
 			}
 		}
 
 		public void skipWhitespace() {
 			while (SVGPathParser.this.mPosition < SVGPathParser.this.mLength) {
 				if (Character.isWhitespace(SVGPathParser.this.mString.charAt(SVGPathParser.this.mPosition))) {
 					this.advance();
 				} else {
 					break;
 				}
 			}
 		}
 
 		public void skipNumberSeparator() {
 			while (SVGPathParser.this.mPosition < SVGPathParser.this.mLength) {
 				final char c = SVGPathParser.this.mString.charAt(SVGPathParser.this.mPosition);
 				switch (c) {
 					case ' ':
 					case ',':
 					case '\n':
 					case '\t':
 						this.advance();
 						break;
 					default:
 						return;
 				}
 			}
 		}
 
 		public void advance() {
 			SVGPathParser.this.mCurrentChar = this.read();
 		}
 
 		/**
 		 * Parses the content of the buffer and converts it to a float.
 		 */
 		private float parseFloat() {
 			int     mantissa     = 0;
 			int     mantissaDigit  = 0;
 			boolean mantPosition  = true;
 			boolean mantissaRead = false;
 
 			int     exp      = 0;
 			int     expDig   = 0;
 			int     expAdj   = 0;
 			boolean expPos   = true;
 
 			switch (SVGPathParser.this.mCurrentChar) {
 				case '-':
 					mantPosition = false;
 				case '+':
 					SVGPathParser.this.mCurrentChar = this.read();
 			}
 
 			m1: switch (SVGPathParser.this.mCurrentChar) {
 				default:
 					return Float.NaN;
 
 				case '.':
 					break;
 
 				case '0':
 					mantissaRead = true;
 					l: for (;;) {
 						SVGPathParser.this.mCurrentChar = this.read();
 						switch (SVGPathParser.this.mCurrentChar) {
 							case '1': case '2': case '3': case '4':
 							case '5': case '6': case '7': case '8': case '9':
 								break l;
 							case '.': case 'e': case 'E':
 								break m1;
 							default:
 								return 0.0f;
 							case '0':
 						}
 					}
 
 				case '1': case '2': case '3': case '4':
 				case '5': case '6': case '7': case '8': case '9':
 					mantissaRead = true;
 					l: for (;;) {
 						if (mantissaDigit < 9) {
 							mantissaDigit++;
 							mantissa = mantissa * 10 + (SVGPathParser.this.mCurrentChar - '0');
 						} else {
 							expAdj++;
 						}
 						SVGPathParser.this.mCurrentChar = this.read();
 						switch (SVGPathParser.this.mCurrentChar) {
 							default:
 								break l;
 							case '0': case '1': case '2': case '3': case '4':
 							case '5': case '6': case '7': case '8': case '9':
 						}
 					}
 			}
 
 			if (SVGPathParser.this.mCurrentChar == '.') {
 				SVGPathParser.this.mCurrentChar = this.read();
 				m2: switch (SVGPathParser.this.mCurrentChar) {
 					default:
 					case 'e': case 'E':
 						if (!mantissaRead) {
 							throw new IllegalArgumentException("Unexpected char '" + SVGPathParser.this.mCurrentChar + "'.");
 						}
 						break;
 
 					case '0':
 						if (mantissaDigit == 0) {
 							l: for (;;) {
 								SVGPathParser.this.mCurrentChar = this.read();
 								expAdj--;
 								switch (SVGPathParser.this.mCurrentChar) {
 									case '1': case '2': case '3': case '4':
 									case '5': case '6': case '7': case '8': case '9':
 										break l;
 									default:
 										if (!mantissaRead) {
 											return 0.0f;
 										}
 										break m2;
 									case '0':
 								}
 							}
 						}
 					case '1': case '2': case '3': case '4':
 					case '5': case '6': case '7': case '8': case '9':
 						l: for (;;) {
 							if (mantissaDigit < 9) {
 								mantissaDigit++;
 								mantissa = mantissa * 10 + (SVGPathParser.this.mCurrentChar - '0');
 								expAdj--;
 							}
 							SVGPathParser.this.mCurrentChar = this.read();
 							switch (SVGPathParser.this.mCurrentChar) {
 								default:
 									break l;
 								case '0': case '1': case '2': case '3': case '4':
 								case '5': case '6': case '7': case '8': case '9':
 							}
 						}
 				}
 			}
 
 			switch (SVGPathParser.this.mCurrentChar) {
 				case 'e': case 'E':
 					SVGPathParser.this.mCurrentChar = this.read();
 					switch (SVGPathParser.this.mCurrentChar) {
 						default:
 							throw new IllegalArgumentException("Unexpected char '" + SVGPathParser.this.mCurrentChar + "'.");
 						case '-':
 							expPos = false;
 						case '+':
 							SVGPathParser.this.mCurrentChar = this.read();
 							switch (SVGPathParser.this.mCurrentChar) {
 								default:
 									throw new IllegalArgumentException("Unexpected char '" + SVGPathParser.this.mCurrentChar + "'.");
 								case '0': case '1': case '2': case '3': case '4':
 								case '5': case '6': case '7': case '8': case '9':
 							}
 						case '0': case '1': case '2': case '3': case '4':
 						case '5': case '6': case '7': case '8': case '9':
 					}
 
 					en: switch (SVGPathParser.this.mCurrentChar) {
 						case '0':
 							l: for (;;) {
 								SVGPathParser.this.mCurrentChar = this.read();
 								switch (SVGPathParser.this.mCurrentChar) {
 									case '1': case '2': case '3': case '4':
 									case '5': case '6': case '7': case '8': case '9':
 										break l;
 									default:
 										break en;
 									case '0':
 								}
 							}
 
 						case '1': case '2': case '3': case '4':
 						case '5': case '6': case '7': case '8': case '9':
 							l: for (;;) {
 								if (expDig < 3) {
 									expDig++;
 									exp = exp * 10 + (SVGPathParser.this.mCurrentChar - '0');
 								}
 								SVGPathParser.this.mCurrentChar = this.read();
 								switch (SVGPathParser.this.mCurrentChar) {
 									default:
 										break l;
 									case '0': case '1': case '2': case '3': case '4':
 									case '5': case '6': case '7': case '8': case '9':
 								}
 							}
 					}
 				default:
 			}
 
 			if (!expPos) {
 				exp = -exp;
 			}
 			exp += expAdj;
 			if (!mantPosition) {
 				mantissa = -mantissa;
 			}
 
 			return this.buildFloat(mantissa, exp);
 		}
 
 		public float nextFloat() {
 			this.skipWhitespace();
 			final float f = this.parseFloat();
 			this.skipNumberSeparator();
 			return f;
 		}
 
 		public float buildFloat(int pMantissa, final int pExponent) {
 			if (pExponent < -125 || pMantissa == 0) {
 				return 0.0f;
 			}
 
 			if (pExponent >=  128) {
 				return (pMantissa > 0)
 				? Float.POSITIVE_INFINITY
 						: Float.NEGATIVE_INFINITY;
 			}
 
 			if (pExponent == 0) {
 				return pMantissa;
 			}
 
 			if (pMantissa >= (1 << 26)) {
 				pMantissa++;  // round up trailing bits if they will be dropped.
 			}
 
 			return (float) ((pExponent > 0) ? pMantissa * com.larvalabs.svgandroid.util.constants.MathUtils.POWERS_OF_10[pExponent] : pMantissa / com.larvalabs.svgandroid.util.constants.MathUtils.POWERS_OF_10[-pExponent]);
 		}
 
 		// ===========================================================
 		// Inner and Anonymous Classes
 		// ===========================================================
 	}
 }
