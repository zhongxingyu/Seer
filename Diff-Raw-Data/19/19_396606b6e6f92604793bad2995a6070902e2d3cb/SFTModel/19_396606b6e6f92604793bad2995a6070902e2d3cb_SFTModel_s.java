 package model;
 
 import imgUtil.Spectrum;
 
 import java.awt.image.BufferedImage;
 
 import mathUtil.Coordinate2D;
 import sft.ShapeEstimation;
 import controller.IMyController;
 
 public class SFTModel implements ISFTModel{
 
 	
 	private double viewAngleD;
 	
 	private int dimension;
 	
 	private static int patchWidth = 32;
 	
 	private static int patchHeight = 32;
 	
 	
 	private IMyController controller;
 	
 	private ShapeEstimation shapeEstimation;
 	
 	
 	
 	public SFTModel(double viewAngleD, int dimension){
 	
 		this.viewAngleD = viewAngleD;
 		
 		this.dimension = dimension;
 	}
 	
 	
 	
 	public void setController(IMyController controller) {
 
 		this.controller = controller;
 		
 	}
 	
 	
 	public int getPatchWidth(){
 		return patchWidth;
 	}
 	
 	public int getPatchHeight(){
 		return patchHeight;
 	}
 
 	
 	public void init() {
 		
 		this.shapeEstimation = new ShapeEstimation(viewAngleD, dimension);
 		
 	}
 	
 	
 	
 	public Orientation doShapeEstimation(IFilterBankModel filterBankModel, Spectrum srcImg, Coordinate2D pointA, Coordinate2D pointB){
 		
 		filterBankModel.doFilteringConcurrent(srcImg, pointA);
 		
 		double[] responseA = filterBankModel.getFilterResultConcurrent();
 		
 		filterBankModel.doFilteringConcurrent(srcImg, pointB);
 		
 		double[] responseB = filterBankModel.getFilterResultConcurrent();
 		
 		
 		Orientation estimatedOrientation = shapeEstimation.estimateShape(responseA, responseB, pointA.shiftedCoord(), pointB.shiftedCoord());  
 		
 		System.out.println(estimatedOrientation.toString());
 		
 		
 		return estimatedOrientation;
 	}
 	
 	public Orientation doShapeEstimationEnergy(IFilterBankModel filterBankModel, Spectrum srcImg, Coordinate2D pointA, Coordinate2D pointB){
 		
 		filterBankModel.doFilteringConcurrentEnergy(srcImg, pointA);
 		
 		double[] responseA = filterBankModel.getFilterResultConcurrentEnergy();
 		
 		filterBankModel.doFilteringConcurrentEnergy(srcImg, pointB);
 		
 		double[] responseB = filterBankModel.getFilterResultConcurrentEnergy();
 		
 		
		Orientation estimatedOrientation = shapeEstimation.estimateShapeEnergyGraphic(responseA, responseB, pointA.shiftedCoord(), pointB.shiftedCoord());  
 		
 		
 		System.out.println(estimatedOrientation.toString());
 		
 		
 		return estimatedOrientation;
 	}
 	
 	
 	public void doSlantTiltMapGeneration(IFilterBankModel filterBankModel, Coordinate2D startCoord, int rangeX, int rangeY){
 		
 	}
 
 }
