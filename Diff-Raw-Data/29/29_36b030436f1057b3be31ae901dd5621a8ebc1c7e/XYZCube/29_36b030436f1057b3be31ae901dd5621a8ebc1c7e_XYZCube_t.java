 package edu.berkeley.gcweb.gui.gamescubeman.XYZCube;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.Timer;
 
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Polygon3D;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;
 import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Shape3D;
 
 public class XYZCube extends Shape3D implements ActionListener {
 	//We're representing a cube of size 2 units by 2 units by 2 units
 	//centered at (0, 0, 4). The cube can occupy anything in the sphere of
 	//diameter sqrt(1+1+1) ~= 1.73 < 3.
 	private int[] dimensions;
 	private double stickerGap = 0.1;
 	public XYZCube(int dimX, int dimY, int dimZ) {
 		super(0, 0, 4);
 		setDimensions(dimX, dimY, dimZ);
 	}
 	public enum CubeVariation {
 		NORMAL("Normal"), VOID_CUBE("Void"), BABYFACE("Babyface");
 		private String desc;
 		private CubeVariation(String desc) {
 			this.desc = desc;
 		}
 		public String toString() {
 			return desc;
 		}
 	}
 	private CubeVariation variation = CubeVariation.NORMAL;
 	public void setCubeVariation(CubeVariation variation) {
 		this.variation = variation;
 		for(int f = 0; f < cubeStickers.length; f++) {
 			int width = cubeStickers[f].length;
 			int height = cubeStickers[f][0].length;
 			for(int w = 0; w < width; w++)
 				for(int h = 0; h < height; h++)
 					cubeStickers[f][w][h].setVisible(true);
 			if(variation == CubeVariation.VOID_CUBE) {
 				for(int w = 1; w < width - 1; w++) {
 					for(int h = 1; h < height - 1; h++) {
 						cubeStickers[f][w][h].setVisible(false);
 					}
 				}
 			} else if(variation == CubeVariation.BABYFACE) {
 				for(int w = 0; w < width; w++) {
 					for(int h = 0; h < height; h++) {
 						if(w == 0 || w == width - 1 || h == 0 || h == height - 1)
 							cubeStickers[f][w][h].setVisible(false);
 					}
 				}
 			}
 		}
 		fireStateChanged(null);
 	}
 	public CubeVariation getCubeVariation() {
 		return variation;
 	}
 	public int[] getDimensions() {
 		return dimensions;
 	}
 	public double getStickerGap() {
 		return stickerGap;
 	}
 	//spacing can range from 0->.5
 	public void setStickerGap(double spacing) {
 		this.stickerGap = spacing;
 		createPolys(true);
 	}
 	public int getMaxTurningRate() {
 		return TURNING_RATES.length - 1;
 	}
 	public int getTurningRate() {
 		return TURNING_RATE;
 	}
 	public void setTurningRate(int newRate) {
 		TURNING_RATE = newRate;
 	}
 	
 	public void setDimensions(int dimX, int dimY, int dimZ) {
 		setDimensions(new int[] { dimX, dimY, dimZ });
 	}
 	public void setDimensions(int[] dims) {
 		dimensions = dims;
 		createPolys(false);
 	}
 	
 	private Timer turner = new Timer(10, this);
 	private ArrayList<FaceTurn> turnQueue = new ArrayList<FaceTurn>();
 	public void actionPerformed(ActionEvent e) {
 		if(turnQueue.get(0).doMove()) { //finished animation
			for(FaceLayerTurn turn : turnQueue.remove(0).toFaceLayerTurns())
				fireStateChanged(turn);
 			if(turnQueue.isEmpty())
 				turner.stop();
 		}
 	}
 	private static Integer[] TURNING_RATES;
 	{
 		HashSet<Integer> rates = new HashSet<Integer>();
 		for(int i : new int[] {1, 2})
 			for(int j : new int[] {1, 3})
 				for(int k : new int[] {1, 3})
 					for(int l : new int[] {1, 5})
 						rates.add(i*j*k*l);
 		ArrayList<Integer> temp = new ArrayList<Integer>(rates);
 		Collections.sort(temp);
 		TURNING_RATES = temp.toArray(new Integer[0]);
 	}
 	private int TURNING_RATE = 8;
 	private class FaceTurn {
 		private int degreesLeft;
 		private RotationMatrix rotation;
 		private int[][][] stickers;
 		//cwTurns = the number of turns cw (in 90 degree increments) we want to turn a face
 		private int cwTurns; //1 -> cw; -1 -> ccw; 2 -> half turn
 		private int legalTurns; //this depends on if the face is square or rectangular
 		private Face face;
 		private int layer;
 		public FaceTurn(Face face, int layer, int amtCW) {
 			this.face = face;
 			this.layer = layer;
 			cwTurns = amtCW;
 			if(dimensions[face.getWidthAxis()] != dimensions[face.getHeightAxis()]) {
 				if((cwTurns & 1) == 1)
 					cwTurns = (cwTurns > 0) ? cwTurns + 1 : cwTurns - 1;
 				legalTurns = cwTurns / 2;
 			} else
 				legalTurns = cwTurns;
 			degreesLeft = 90*Math.abs(cwTurns);
 			stickers = getLayerIndices(face, layer);
 		}
 		private boolean cubeRotation;
 		public void setCubeRotation(boolean b) {
 			cubeRotation = b;
 		}
 		//this is useful for creating cube rotations
 		private FaceTurn otherTurn;
 		public boolean mergeTurn(FaceTurn other) {
 			//if not compatible
 			if(cubeRotation && other.cubeRotation)
 				return false;
 			boolean mergingRotation = cubeRotation || other.cubeRotation;
 			if(face.getRotationAxis() != other.face.getRotationAxis() || (!mergingRotation && face.index() == other.face.index() && layer == other.layer))
 				return false;
 			if(otherTurn == null) {
 				otherTurn = other;
 				return true;
 			}
 			return otherTurn.mergeTurn(other);
 		}
 		//this will turn the stickers, and return true when the move animation has finished
 		private boolean done = false;
 		private int speed = -1;
 		public boolean doMove() {
 			if(speed == -1) { //just starting animation
 				speed = TURNING_RATES[TURNING_RATE];
 				//multiply by -1 because the rotation matrix expects degrees ccw
 				rotation = new RotationMatrix(face.getRotationAxis(), -1*(face.isCWWithAxis() ? 1 : -1)*Math.signum(cwTurns)*speed);
 			}
 			if(!done) {
 				degreesLeft -= speed;
 				for(int i=0; i<stickers.length; i++)
 					for(int[] index : stickers[i])
 						cubeStickers[index[0]][index[1]][index[2]].rotate(rotation);
 
 				done = degreesLeft <= 0;
 				if(done) //updating internal representation
 					for(int[][] cycleIndices : stickers)
 						cycle(cubeStickers, cycleIndices, legalTurns);
 			}
 			boolean bothDone;
 			if(otherTurn != null)
 				bothDone = otherTurn.doMove() && done;
 			else
 				bothDone = done;
 			fireCanvasChange();
 			return bothDone;
 		}
		private FaceLayerTurn toFaceLayerTurn() {
 			return new FaceLayerTurn(face, cubeRotation ? -1 : layer, cwTurns);
 		}
		public FaceLayerTurn[] toFaceLayerTurns() {
			if(otherTurn != null && !cubeRotation)
				return new FaceLayerTurn[] { toFaceLayerTurn(), otherTurn.toFaceLayerTurn() };
			else
				return new FaceLayerTurn[] { toFaceLayerTurn() };
		}
 	}
 
 	//needed because java's modulo is weird with negative values
 	//assumes m > 0
 	private static int modulo(int x, int m) {
 		int y = x % m;
 		if(y >= 0) return y;
 		return y + m;
 	}
 	//offset indicates that new_polys[(i + offset) % polys.length] = old_polys[i] for all 0 <= i < polys.length
 	private static void cycle(Polygon3D[][][] polys, int[][] indices, int offset) {
 		Polygon3D[][][] old_polys = new Polygon3D[polys.length][][];
 		for(int i = 0; i < old_polys.length; i++) { //making a deep copy of the original array
 			old_polys[i] = new Polygon3D[polys[i].length][];
 			for(int j = 0; j < old_polys[i].length; j++) {
 				old_polys[i][j] = new Polygon3D[polys[i][j].length];
 				for(int k = 0; k < old_polys[i][j].length; k++)
 					old_polys[i][j][k] = polys[i][j][k];
 			}
 		}
 		
 		for(int c = 0; c < indices.length; c++) {
 			int[] from = indices[c], to = indices[modulo(c+offset, indices.length)];
 			polys[to[0]][to[1]][to[2]] = old_polys[from[0]][from[1]][from[2]];
 		}
 	}
 	
 	//This returns an array of arrays of indices, where each element is an index of a sticker (represented as a 2 element array)
 	//So for the R face, an [2][][2] array faces would be returned where where faces[0] is a 4 element array of all the R face stickers,
 	//and faces[1] is a 6 element array of half the F, U, B, D stickers
 	//The return value is structured like this to facilitate cycling stickers as necessary
 	//TODO - would memoization be a good idea here?
 	private int[][][] getLayerIndices(Face face, int layer) {
 		int width = dimensions[face.getWidthAxis()];
 		int height = dimensions[face.getHeightAxis()];
 		int depth = dimensions[face.getRotationAxis()];
 		if(layer >= depth)
 			layer = depth - 1;
 		else if(layer < 1)
 			layer = 1;
 		
 		//each cycle is of 4 stickers if width == height, 2 stickers otherwise
 		int cycleLength = (width == height) ? 4 : 2;
 		int faceCycles = (int)Math.ceil((double)width*height/cycleLength);
 		int layerCycles = 2*layer*(width + height)/cycleLength;
 		int[][][] stickers = new int[faceCycles + layerCycles][cycleLength][3];
 
 		int nthCycle = 0;
 		boolean square = width == height;
 		if(!square) { //half turn only
 			for(int h = 0; h < height; h++) {
 				int maxW = (int) Math.ceil(width / 2.);
 				if((width & 1) == 1 && h >= Math.ceil(height / 2.))
 					maxW--;
 				for(int w = 0; w < maxW; w++) {
 					stickers[nthCycle][0] = new int[] { face.index(), h, w };
 					stickers[nthCycle][1] = new int[] { face.index(), height - 1 - h, width - 1 - w };
 					nthCycle++;
 				}
 			}
 		} else { //quarter turn legal
 			for(int h = 0; h < Math.ceil(height / 2.); h++) {
 				for(int w = 0; w < width / 2; w++) {
 					stickers[nthCycle][0] = new int[] { face.index(), h, w };
 					stickers[nthCycle][1] = new int[] { face.index(), w, height - 1 - h };
 					stickers[nthCycle][2] = new int[] { face.index(), width - 1 - h, height - 1 - w };
 					stickers[nthCycle][3] = new int[] { face.index(), width - 1 - w, h };
 					if(!face.isFirstAxisClockwise())
 						Collections.reverse(Arrays.asList(stickers[nthCycle]));
 					nthCycle++;
 				}
 			}
 			//we include the center of odd cubes here, so it gets animated
 			if((width & 1) == 1)
 				stickers[nthCycle++] = new int[][] { {face.index(), width / 2, width / 2} };
 		}
 		
 		if(face == Face.RIGHT) {
 			if(square) {
 				for(int w = 0; w < width; w++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.FRONT.index(), w, l},
 								{Face.UP.index(), w, l},
 								{Face.BACK.index(), width - 1 - w, l},
 								{Face.DOWN.index(), width - 1 - w, l} };
 			} else {
 				for(int h = 0; h < height; h++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.FRONT.index(), h, l},
 								{Face.BACK.index(), height - 1 - h, l} };
 				for(int w = 0; w < width; w++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.UP.index(), w, l},
 								{Face.DOWN.index(), width - 1 - w, l} };
 			}
 		} else if(face == Face.LEFT) {
 			if(square) {
 				for(int l = 0; l < layer; l++)
 					for(int w = 0; w < width; w++)
 						stickers[nthCycle++] = new int[][] { {Face.UP.index(), w, depth - 1 - l},
 							{Face.FRONT.index(), w, depth - 1 - l},
 							{Face.DOWN.index(), height - 1 - w, depth - 1 - l},
 							{Face.BACK.index(), height - 1 - w, depth - 1 - l} };
 			} else {
 				for(int h = 0; h < height; h++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.FRONT.index(), h, depth - 1 - l},
 								{Face.BACK.index(), height - 1 - h, depth - 1 - l} };
 				for(int w = 0; w < width; w++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.UP.index(), w, depth - 1 - l},
 								{Face.DOWN.index(), width - 1 - w, depth - 1 - l} };
 			}
 		} else if(face == Face.FRONT) {
 			if(square) {
 				for(int l = 0; l < layer; l++)
 					for(int w = 0; w < width; w++)
 						stickers[nthCycle++] = new int[][] { {Face.UP.index(), l, w},
 							{Face.RIGHT.index(), w, l},
 							{Face.DOWN.index(), l, width - 1 - w},
 							{Face.LEFT.index(), width - 1 - w, l} };
 			} else {
 				for(int h = 0; h < height; h++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.LEFT.index(), h, l},
 								{Face.RIGHT.index(), height - 1 - h, l} };
 				for(int w = 0; w < width; w++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.UP.index(), l, w},
 								{Face.DOWN.index(), l, width - 1 - w} };
 			}
 		} else if(face == Face.BACK) {
 			if(square) {
 				for(int l = 0; l < layer; l++)
 					for(int w = 0; w < width; w++)
 						stickers[nthCycle++] = new int[][] { {Face.UP.index(), depth - 1 - l, w},
 							{Face.LEFT.index(), width - 1 - w, depth - 1 - l},
 							{Face.DOWN.index(), depth - 1 - l, width - 1 - w},
 							{Face.RIGHT.index(), w, depth - 1 - l} };
 			} else {
 				for(int h = 0; h < height; h++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.LEFT.index(), h, depth - 1 - l},
 								{Face.RIGHT.index(), height - 1 - h, depth - 1 - l} };
 				for(int w = 0; w < width; w++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.UP.index(), depth - 1 - l, w},
 								{Face.DOWN.index(), depth - 1 - l, width - 1 - w} };
 			}
 		} else if(face == Face.UP) {
 			if(square) {
 				for(int l = 0; l < layer; l++)
 					for(int w = 0; w < width; w++)
 						stickers[nthCycle++] = new int[][] { {Face.FRONT.index(), depth - 1 - l, w},
 							{Face.LEFT.index(), depth - 1 - l, w},
 							{Face.BACK.index(), depth - 1 - l, width - 1 - w},
 							{Face.RIGHT.index(), depth - 1 - l, width - 1 - w} };
 			} else {
 				for(int h = 0; h < height; h++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.LEFT.index(), depth - 1 - l, h},
 								{Face.RIGHT.index(), depth - 1 - l, height - 1 - h} };
 				for(int w = 0; w < width; w++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.FRONT.index(), depth - 1 - l, w},
 								{Face.BACK.index(), depth - 1 - l, width - 1 - w} };
 			}
 		} else if(face == Face.DOWN) {
 			if(square) {
 				for(int l = 0; l < layer; l++)
 					for(int w = 0; w < width; w++)
 						stickers[nthCycle++] = new int[][] { {Face.FRONT.index(), l, w},
 							{Face.RIGHT.index(), l, width - 1 - w},
 							{Face.BACK.index(), l, width - 1 - w},
 							{Face.LEFT.index(), l, w} };
 			} else {
 				for(int h = 0; h < height; h++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.LEFT.index(), l, h},
 								{Face.RIGHT.index(), l, height - 1 - h} };
 				for(int w = 0; w < width; w++)
 					for(int l = 0; l < layer; l++)
 						stickers[nthCycle++] = new int[][] { {Face.FRONT.index(), l, w},
 								{Face.BACK.index(), l, width - 1 - w} };
 			}
 		}
 		
 		return stickers;
 	}
 	
 	private ArrayList<FaceLayerTurn> turns = new ArrayList<FaceLayerTurn>();
 	private void appendTurn(FaceLayerTurn t) {
 		if(!turns.isEmpty()) {
 			FaceLayerTurn old = turns.get(turns.size() - 1);
 			if(old.isMergeable(t)) {
 				if(old.merge(t))
 					turns.remove(turns.size() - 1);
 				t = null;
 			}
 		}
 		if(t != null) {//if it hasn't been merged
 			turns.add(t);
 		}
 	}
 	
 	private ArrayList<Face> legalFaces;
 	public void setLegalFaces(ArrayList<Face> faces) {
 		legalFaces = faces;
 	}
 	
 	public void doTurn(Face face, int layer, int cw) {
 		if(legalFaces == null || legalFaces.contains(face)) {
 			layer = Math.min(layer, dimensions[face.getRotationAxis()] - 1);
 			appendTurn(new FaceLayerTurn(face, layer, cw));
 			FaceTurn turn = new FaceTurn(face, layer, cw);
 			if(turnQueue.isEmpty() || !turnQueue.get(turnQueue.size() - 1).mergeTurn(turn))
 				turnQueue.add(turn);
 			turner.start();
 		}
 	}
 	
 	private boolean cubeRotation = true;
 	public void setCubeRotations(boolean cubeRotation) {
 		this.cubeRotation = cubeRotation;
 	}
 	
 	public void doCubeRotation(Face face, int cw) {
 		if(!cubeRotation) return;
 		appendTurn(new FaceLayerTurn(face, -1, cw));
 		FaceTurn turn = new FaceTurn(face, dimensions[face.getRotationAxis()] - 1, cw);
 		FaceTurn t = new FaceTurn(face.getOppositeFace(), 1, -cw);
 		t.setCubeRotation(true);
 		turn.mergeTurn(t);
 		turn.setCubeRotation(true);
 
 		if(turnQueue.isEmpty() || !turnQueue.get(turnQueue.size() - 1).mergeTurn(turn))
 			turnQueue.add(turn);
 		turner.start();
 	}
 	
 	public void resetCube() {
 		createPolys(false);
 	}
 	
 	private CubeSticker[][][] cubeStickers;
 	private void createPolys(boolean copyOld) {
 		turns.clear();
 		turnQueue.clear();
 		turner.stop();
 		resetHandPositions();
 		clearPolys();
 		CubeSticker[][][] cubeStickersOld = cubeStickers;
 		cubeStickers = new CubeSticker[6][][];
 		double[] point = new double[3];
 		double scale = 2. / (Math.max(Math.max(dimensions[0], dimensions[1]), dimensions[2]));
 
 		for(Face f1 : Face.faces) {
 			if(f1.isCWWithAxis()) continue;
 			Face f2 = f1.getOppositeFace();
 			int height = dimensions[f1.getHeightAxis()];
 			int width = dimensions[f1.getWidthAxis()];
 			int depth = dimensions[f1.getRotationAxis()];
 			double halfHeight = height / 2.;
 			double halfWidth = width / 2.;
 			double halfDepth = depth / 2.;
 			cubeStickers[f1.index()] = new CubeSticker[height][width];
 			cubeStickers[f2.index()] = new CubeSticker[height][width];
 			for(int h = 0; h < height; h++) {
 				for(int w = 0; w < width; w++) {
 					CubeSticker sticker = new CubeSticker();
 					List<Double> spaces1 = Arrays.asList(stickerGap, 1 - stickerGap);
 					List<Double> spaces2 = new ArrayList<Double>(spaces1);
 					for(double hh : spaces1) {
 						for(double ww : spaces2) {
 							point[f1.getHeightAxis()] = h + hh;
 							point[f1.getWidthAxis()] = w + ww;
 							point[f1.getRotationAxis()] = 0;
 							sticker.addPoint(point);
 						}
 						Collections.reverse(spaces2); //want to form a box, not an x
 					}
 
 					double[] translate = new double[3];
 					translate[f1.getWidthAxis()] = -halfWidth;
 					translate[f1.getHeightAxis()] = -halfHeight;
 					translate[f1.getRotationAxis()] = -halfDepth;
 					sticker.translate(translate).scale(scale, scale, scale);
 					if(copyOld)
 						sticker.setFace(cubeStickersOld[f1.index()][h][w].getFace());
 					else
 						sticker.setFace(f1);
 					cubeStickers[f1.index()][h][w] = sticker;
 					addPoly(sticker);
 
 					translate = new double[3];
 					translate[f1.getRotationAxis()] = scale*depth;
 					sticker = (CubeSticker) sticker.clone().translate(translate);
 					if(copyOld)
 						sticker.setFace(cubeStickersOld[f2.index()][h][w].getFace());
 					else
 						sticker.setFace(f2);
 					cubeStickers[f2.index()][h][w] = sticker;
 					addPoly(sticker);
 				}
 			}
 		}
 		fireStateChanged(null);
 	}
 	private ArrayList<CubeStateChangeListener> stateListeners = new ArrayList<CubeStateChangeListener>();
 	public void addStateChangeListener(CubeStateChangeListener l) {
 		stateListeners.add(l);
 	}
 
 	//    values = { "F" : 0, "U" : 0, "R" : 0, "L" : 1, "B" : 2, "D" : 4}
 	
 	//look @ BLD
 	//look for guy w/ BL stickers and deduce U color
 	//look for LD stickers and deduce F color
 	//look for BD stickers and deduce R color
 	public String getState() {
 		if(true)
 			return turns.toString();
 		ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(new Color[Face.faces.size()]));
 		int[] values = new int[Face.faces.size()];
 		values[Face.FRONT.index()] = 0;
 		values[Face.RIGHT.index()] = 0;
 		values[Face.UP.index()] = 0;
 		values[Face.LEFT.index()] = 1;
 		values[Face.BACK.index()] = 2;
 		values[Face.DOWN.index()] = 4;
 		
 		colors.set(Face.BACK.index(), cubeStickers[Face.BACK.index()][0][0].getFillColor());
 		colors.set(Face.LEFT.index(), cubeStickers[Face.LEFT.index()][0][0].getFillColor());
 		colors.set(Face.DOWN.index(), cubeStickers[Face.DOWN.index()][0][0].getFillColor());
 		for(Polygon3D[][] face : cubeStickers) {
 			for(int i = 0; i < face.length; i++)
 				for(int j = 0; j < face[i].length; j++) {
 					face[i][j].getFillColor();
 				}
 		}
 		return "Cube State Here!";
 	}
 	
 	public boolean isSolved() {
 		for(Polygon3D[][] face : cubeStickers) {
 			Color c = null;
 			for(int i = 0; i < face.length; i++)
 				for(int j = 0; j < face[i].length; j++) {
 					if(c == null && face[i][j].isVisible())
 						c = face[i][j].getFillColor();
 					if(face[i][j].isVisible() && !face[i][j].getFillColor().equals(c))
 						return false;
 				}
 		}
 		return true;
 	}
 	
 	private void fireStateChanged(FaceLayerTurn turn) {
 		for(CubeStateChangeListener l : stateListeners)
 			l.cubeStateChanged(this, turn);
 		fireCanvasChange();
 	}
 
 	private int[] handPositions = new int[Face.faces().length];
 	private void resetHandPositions() {
 		Arrays.fill(handPositions, 1);
 	}
 	//TODO - improve hand indicators
 	private void refreshHandPositions() {
 //		for(Polygon3D[][] face : cubeStickers)
 //			for(Polygon3D[] polys: face)
 //				for(Polygon3D poly: polys)
 //					poly.setOpacity(.8f);
 //		int[][][] stickerCycles = getLayerIndices(Face.LEFT, left);
 //		for(int[][] stickerCycle : stickerCycles) {
 //			for(int[] stickers : stickerCycle) {
 //				cubeStickers[stickers[0]][stickers[1]][stickers[2]].setOpacity(1f);
 //			}
 //		}
 //		stickerCycles = getLayerIndices(Face.RIGHT, right);
 //		for(int[][] stickerCycle : stickerCycles) {
 //			for(int[] stickers : stickerCycle) {
 //				cubeStickers[stickers[0]][stickers[1]][stickers[2]].setOpacity(1f);
 //			}
 //		}
 	}
 	private static final HashMap<String, Integer> TURN_DIRECTION = new HashMap<String, Integer>();
 	public static final HashMap<Integer, String> DIRECTION_TURN = new HashMap<Integer, String>();
 	{
 		TURN_DIRECTION.put("", 1);
 		TURN_DIRECTION.put("'", -1);
 		TURN_DIRECTION.put("2", 2);
 		DIRECTION_TURN.put(1, "");
 		DIRECTION_TURN.put(-1, "'");
 		DIRECTION_TURN.put(2, "2");
 	}
 	public boolean doTurn(String turn) {
 		if(turn.equals("scramble")) {
 			scramble();
 			return true;
 		}
 		char ch = turn.charAt(0);
 		Face face = Face.decodeFace(ch);
 		Integer direction = TURN_DIRECTION.get(turn.substring(1));
 		if(direction == null) { //hand shift
 			int leftRightWidth = dimensions[Face.RIGHT.getRotationAxis()];
 			direction = 0;
 			if("<<".equals(turn.substring(1)))
 				direction = -1;
 			else if(">>".equals(turn.substring(1)))
 				direction = 1;
 			else
 				return false;
 			if(!face.isCWWithAxis()) direction = -direction;
 			handPositions[face.index()] += direction;
 			handPositions[face.index()] = Math.max(1, handPositions[face.index()]);
 			handPositions[face.index()] = Math.min(leftRightWidth - 1, handPositions[face.index()]);
 			return true;
 		} else if(face != null) { //n-layer face turn
 			int layer = handPositions[face.index()] + ((Character.isUpperCase(ch)) ? 0 : 1);
 			doTurn(face, layer, direction);
 			return true;
 		} else { //cube rotation
 			doCubeRotation(Face.decodeCubeRotation(ch), direction);
 			return true;
 		}
 	}
 	public void scramble() {
 		Face[] faces = Face.faces();
 		Random r = new Random();
 		for(int ch = 0; ch < 3*(dimensions[0]+dimensions[1]+dimensions[2]); ch++) {
 			Face f = faces[r.nextInt(faces.length)];
 			doTurn(f, r.nextInt(Math.max(1, dimensions[f.getRotationAxis()]-1))+1, (r.nextInt(2)+1));
 		}
 	}
 }
