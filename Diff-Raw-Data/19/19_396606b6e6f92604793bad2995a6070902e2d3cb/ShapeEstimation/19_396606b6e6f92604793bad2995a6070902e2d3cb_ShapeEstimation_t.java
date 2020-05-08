 package sft;
 
 import imgUtil.ImgCommonUtil;
 import mathUtil.Coordinate2D;
 import model.Orientation;
 import static java.lang.Math.*;
 
 public class ShapeEstimation {
 	
 	
 	private double viewAngleD;
 	
 	private int dimension;
 	
 	
 	public ShapeEstimation(double viewAngleD, int dimension){
 		this.viewAngleD = viewAngleD;
 		this.dimension = dimension;
 	}
 	
 	
 	public Orientation estimateShape(double[] responseA, double[] responseB, Coordinate2D pointA, Coordinate2D pointB){
 		
 		
 		double slantEstimatedD = 0.0;
 		double tiltEstimatedD = 0.0;
 		
 		
 		double slantGapD = 1.0;
 		double tiltGapD = 1.0;
 		
 		
 		double minDiff = 0.0;
 		
 		
 		double slantD = 0.0;
 		double tiltD = 0.0;
 		
 		DistortionMatrix phi = new DistortionMatrix(viewAngleD, dimension, toRadians(slantD), toRadians(tiltD), pointA, pointB);
 		
 		
 		double responseDiff = ResponseDiff.computeResponseDiff(phi, responseA, responseB);
 		
 			
 //		slantEstimatedD = slantD;
 //		tiltEstimatedD = tiltD;
 			
 		minDiff = responseDiff;
 			
 			
 		
 		
 		
 		
 		for(slantD = slantGapD; slantD < 90.0; slantD += slantGapD)
 			
 			for(tiltD = tiltGapD; tiltD < 360.0; tiltD += tiltGapD){
 				
 				
 				phi = new DistortionMatrix(viewAngleD, dimension, toRadians(slantD), toRadians(tiltD), pointA, pointB);
 				
 				
 				responseDiff = ResponseDiff.computeResponseDiff(phi, responseA, responseB);
 				
 				if(responseDiff < minDiff){
 					
 					slantEstimatedD = slantD;
 					tiltEstimatedD = tiltD;
 					
 					minDiff = responseDiff;
 					
 					
 				}
 				
 			}
 			
 			
 		
 		
 		return Orientation.orientationInDegree(slantEstimatedD, tiltEstimatedD);
 		
 		
 		
 		
 	}
 
 	public Orientation estimateShapeEnergy(double[] responseA, double[] responseB, Coordinate2D pointA, Coordinate2D pointB){
 		
 		
 		double slantEstimatedD = 0.0;
 		double tiltEstimatedD = 0.0;
 		
 		
 		double slantGapD = 1.0;
 		double tiltGapD = 1.0;
 		
 		
 		double minDiff = 0.0;
 		
 		
 		double slantD = 0.0;
 		double tiltD = 0.0;
 		
 		DistortionMatrix phi = new DistortionMatrix(viewAngleD, dimension, toRadians(slantD), toRadians(tiltD), pointA, pointB);
 		
 		
 		double responseDiff = ResponseDiff.computeResponseDiffEnergy(phi, responseA, responseB);
 		
 			
 //		slantEstimatedD = slantD;
 //		tiltEstimatedD = tiltD;
 			
 		minDiff = responseDiff;
 		
 		double maxDiff = Double.MIN_VALUE;
 			
 			
 		
 		
 		
 		
 		for(slantD = slantGapD; slantD < 90.0; slantD += slantGapD){
 			
 			for(tiltD = tiltGapD; tiltD < 360.0; tiltD += tiltGapD){
 				
 				
 				phi = new DistortionMatrix(viewAngleD, dimension, toRadians(slantD), toRadians(tiltD), pointA, pointB);
 				
 				
 				responseDiff = ResponseDiff.computeResponseDiffEnergy(phi, responseA, responseB);
 				
 				if(responseDiff < minDiff){
 					
 					slantEstimatedD = slantD;
 					tiltEstimatedD = tiltD;
 					
 					minDiff = responseDiff;
 					
 					
 				}
 				
 				if(responseDiff > maxDiff)
 					maxDiff = responseDiff;
 			}
 			
 		}
 		
 		
 		return Orientation.orientationInDegree(slantEstimatedD, tiltEstimatedD);
 		
 		
 	}
 	
 	
 	
 	public Orientation estimateShapeEnergyGraphic(double[] responseA, double[] responseB, Coordinate2D pointA, Coordinate2D pointB){
 		
 		
 		double slantEstimatedD = 0.0;
 		double tiltEstimatedD = 0.0;
 		
 		
 		double slantGapD = 1.0;
 		double tiltGapD = 1.0;
 		
 		
 		double minDiff = Double.MAX_VALUE;
 //		double maxDiff = Double.MIN_VALUE;
 		
 		
 		
 		double slantD = 0.0;
 		double tiltD = 0.0;
 		
 		DistortionMatrix phi;
 		
 		double responseDiff = 0.0;
 			
 //		slantEstimatedD = slantD;
 //		tiltEstimatedD = tiltD;
 			
 			
 		int slantCount = 90;
 		int tiltCount = 360;
 		
 		
 		double[][] diffMatrix = new double[tiltCount][slantCount]; 
 		
 		
 
 		int slantIdx = 0;
 		int tiltIdx;
 		
 		for(slantD = 0.0; slantD < 90.0; slantD += slantGapD){
 			
 			
 			tiltIdx = 0;
 			
 			for(tiltD = 0.0; tiltD < 360.0; tiltD += tiltGapD){
 				
 				
 				
 				phi = new DistortionMatrix(viewAngleD, dimension, toRadians(slantD), toRadians(tiltD), pointA, pointB);
 				
 				
 				responseDiff = ResponseDiff.computeResponseDiffEnergy(phi, responseA, responseB);
 				
//				diffMatrix[tiltIdx++][slantIdx] = Math.pow(responseDiff, 2.5);
 				
 				if(responseDiff < minDiff){
 					
 					slantEstimatedD = slantD;
 					tiltEstimatedD = tiltD;
 					
 					minDiff = responseDiff;
 					
 					
 				}
 				
 			}
 			
 			slantIdx++;
 		}
 		
//		ImgCommonUtil.writeToImgFile(tiltCount, slantCount, diffMatrix, Math.pow(minDiff, 2.5), "diffMatrix");
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		return Orientation.orientationInDegree(slantEstimatedD, tiltEstimatedD);
 		
 		
 	}
 
 }
