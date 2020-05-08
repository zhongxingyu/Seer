 package controller;
 
 import java.awt.image.BufferedImage;
 
 
 import gui.IMyView;
 import mathUtil.Coordinate2D;
 import model.ISFTModel;
 import model.Orientation;
 import model.SrcImage;
 import model.IFilterBankModel;
 
 public class MainController implements IMyController {
 
 	
 	private IMyView mainView;
 	
 	private IFilterBankModel filterBankModel;
 	
 	private ISFTModel sftModel;
 	
 	
 	private SrcImage srcImg; 
 	
 	
 
 	private void initModel(){
 		
		this.srcImg = new SrcImage("test/5_s30t250.bmp");
 		
 		
 		filterBankModel.init();
 		
 		sftModel.init();
 	}
 
 	
 	
 	public void triggerFiltering(Coordinate2D centerCoord){
 		
 		filterBankModel.doFiltering(srcImg.getSpectrum(), centerCoord);
 		
 		filterBankModel.printCurrentResult();
 		
 	}
 	
 	
 
 	
 	public Orientation triggerShapeEstimation(Coordinate2D coordA, Coordinate2D coordB) {
 		
 		return sftModel.doShapeEstimation(filterBankModel, srcImg.getSpectrum(), coordA, coordB);
 		
 	}
 	
 	
 	
 	public Orientation triggerShapeEstimationEnergy(Coordinate2D coordA, Coordinate2D coordB) {
 		
 		
 		return sftModel.doShapeEstimationEnergy(filterBankModel, srcImg.getSpectrum(), coordA, coordB);
 		
 		
 	}
 	
 	
 	
 	public void updateSrcImage(SrcImage srcImg){
 		this.srcImg = srcImg;
 	}
 	
 	public void setView(IMyView view) {
 		
 		this.mainView = view;
 		
 	}
 
 	
 	public void setModel(IFilterBankModel model) {
 		
 		this.filterBankModel = model;
 		
 	}
 
 	public void setModel(ISFTModel model){
 		this.sftModel = model;
 	}
 
 
 	
 	public void init() {
 	
 		initModel();
 		
 		initView();
 		
 		
 		
 	}
 	
 	
 	
 	private void initView(){
 		
 		
 		mainView.init();
 	}
 	
 	
 	public void start() {
 		mainView.start();
 		
 	}
 	
 	
 	
 	
 	public void updateView() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void updateModel() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public SrcImage getSrcImage() {
 		// TODO Auto-generated method stub
 		
 		return srcImg;
 	}
 
 	
 	public int getSrcImgWidth(){
 		return srcImg.getWidth();
 	}
 	
 	public int getSrcImgHeight(){
 		return srcImg.getHeight();
 	}
 	
 	public int getPatchWidth() {
 		// TODO Auto-generated method stub
 		return sftModel.getPatchWidth();
 	}
 
 	
 	public int getPatchHeight() {
 		// TODO Auto-generated method stub
 		return sftModel.getPatchHeight();
 	}
 
 
 
 
 
 	
 }
