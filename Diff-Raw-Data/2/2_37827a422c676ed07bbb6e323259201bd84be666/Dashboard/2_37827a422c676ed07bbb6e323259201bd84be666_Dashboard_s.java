 package com.binar.core.dashboard;
 
 import com.binar.core.dashboard.dashboardItem.farmationExpireGoods.FarmationExpiredGoodsModel;
 import com.binar.core.dashboard.dashboardItem.farmationExpireGoods.FarmationExpiredGoodsPresenter;
 import com.binar.core.dashboard.dashboardItem.farmationExpireGoods.FarmationExpiredGoodsViewImpl;
 import com.binar.core.dashboard.dashboardItem.farmationExpiredGoodsStatus.FarmationExpiredGoodsStatusModel;
 import com.binar.core.dashboard.dashboardItem.farmationExpiredGoodsStatus.FarmationExpiredGoodsStatusPresenter;
 import com.binar.core.dashboard.dashboardItem.farmationExpiredGoodsStatus.FarmationExpiredGoodsStatusViewImpl;
 import com.binar.core.dashboard.dashboardItem.farmationGoodsConsumption.FarmationGoodsConsumptionModel;
 import com.binar.core.dashboard.dashboardItem.farmationGoodsConsumption.FarmationGoodsConsumptionPresenter;
 import com.binar.core.dashboard.dashboardItem.farmationGoodsConsumption.FarmationGoodsConsumptionViewImpl;
 import com.binar.core.dashboard.dashboardItem.farmationGoodsWithIncreasingTrend.FarmationGoodsWithIncreasingTrendModel;
 import com.binar.core.dashboard.dashboardItem.farmationGoodsWithIncreasingTrend.FarmationGoodsWithIncreasingTrendPresenter;
 import com.binar.core.dashboard.dashboardItem.farmationGoodsWithIncreasingTrend.FarmationGoodsWithIncreasingTrendViewImpl;
 import com.binar.core.dashboard.dashboardItem.farmationMinimumStock.FarmationMinimumStockModel;
 import com.binar.core.dashboard.dashboardItem.farmationMinimumStock.FarmationMinimumStockPresenter;
 import com.binar.core.dashboard.dashboardItem.farmationMinimumStock.FarmationMinimumStockViewImpl;
 import com.binar.core.dashboard.dashboardItem.farmationMinimumStockFastMoving.FarmationMinimumStockFastMovingModel;
 import com.binar.core.dashboard.dashboardItem.farmationMinimumStockFastMoving.FarmationMinimumStockFastMovingPresenter;
 import com.binar.core.dashboard.dashboardItem.farmationMinimumStockFastMoving.FarmationMinimumStockFastMovingViewImpl;
 import com.binar.core.dashboard.dashboardItem.farmationRequirementStatus.FarmationRequirementStatusModel;
 import com.binar.core.dashboard.dashboardItem.farmationRequirementStatus.FarmationRequirementStatusPresenter;
 import com.binar.core.dashboard.dashboardItem.farmationRequirementStatus.FarmationRequirementStatusViewImpl;
 import com.binar.core.dashboard.dashboardItem.ifrsDeletionApproval.IfrsDeletionApprovalModel;
 import com.binar.core.dashboard.dashboardItem.ifrsDeletionApproval.IfrsDeletionApprovalPresenter;
 import com.binar.core.dashboard.dashboardItem.ifrsDeletionApproval.IfrsDeletionApprovalViewImpl;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodProcurement.IfrsGoodsProcurementModel;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodProcurement.IfrsGoodsProcurementPresenter;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodProcurement.IfrsGoodsProcurementViewImpl;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodReceptionSummary.IfrsGoodsReceptionSummaryModel;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodReceptionSummary.IfrsGoodsReceptionSummaryPresenter;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodReceptionSummary.IfrsGoodsReceptionSummaryViewImpl;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodsConsumption.IfrsGoodsConsumptionPresenter;
 import com.binar.core.dashboard.dashboardItem.ifrsGoodsWithIncreasingTrend.IfrsGoodsWithIncreasingTrendPresenter;
 import com.binar.core.dashboard.dashboardItem.ifrsRequirementPlanning.IfrsRequirementPlanningPresenter;
 import com.binar.core.dashboard.dashboardItem.ppkExpiredGoodsNonAccepted.PpkExpiredGoodsNonAcceptedModel;
 import com.binar.core.dashboard.dashboardItem.ppkExpiredGoodsNonAccepted.PpkExpiredGoodsNonAcceptedPresenter;
 import com.binar.core.dashboard.dashboardItem.ppkExpiredGoodsNonAccepted.PpkExpiredGoodsNonAcceptedViewImpl;
 import com.binar.core.dashboard.dashboardItem.ppkGoodsProcurementSummary.PpkGoodsProcurementPresenter;
 import com.binar.core.dashboard.dashboardItem.ppkRequirementPlanning.PpkRequirementPlanningPresenter;
 import com.binar.core.dashboard.dashboardItem.procurementDueDate.ProcurementDueDateModel;
 import com.binar.core.dashboard.dashboardItem.procurementDueDate.ProcurementDueDatePresenter;
 import com.binar.core.dashboard.dashboardItem.procurementDueDate.ProcurementDueDateViewImpl;
 import com.binar.core.dashboard.dashboardItem.procurementGoodsProcurement.ProcurementGoodsProcurementPresenter;
 import com.binar.core.dashboard.dashboardItem.procurementReceipt.ProcurementReceiptPresenter;
 import com.binar.core.dashboard.dashboardItem.procurementRequirementAcceptance.ProcurementRequirementAcceptanceModel;
 import com.binar.core.dashboard.dashboardItem.procurementRequirementAcceptance.ProcurementRequirementAcceptancePresenter;
 import com.binar.core.dashboard.dashboardItem.procurementRequirementAcceptance.ProcurementRequirementAcceptanceViewImpl;
 import com.binar.core.dashboard.dashboardItem.procurementSupplierRank.ProcurementSupplierRankModel;
 import com.binar.core.dashboard.dashboardItem.procurementSupplierRank.ProcurementSupplierRankPresenter;
 import com.binar.core.dashboard.dashboardItem.procurementSupplierRank.ProcurementSupplierRankViewImpl;
 import com.binar.core.dashboard.dashboardItem.supportDeletedGoodsNonApproved.SupportDeletedGoodsNonApprovedPresenter;
 import com.binar.core.dashboard.dashboardItem.supportGoodsConsumption.SupportGoodsConsumptionPresenter;
 import com.binar.core.dashboard.dashboardItem.supportGoodsProcurementSummary.SupportGoodsProcurementSummaryPresenter;
 import com.binar.core.dashboard.dashboardItem.supportGoodsWithIncreasingTrend.SupportGoodsWithIncreasingTrendPresenter;
 import com.binar.core.dashboard.dashboardItem.supportRequirementNonApproved.SupportRequirementNonApprovedModel;
 import com.binar.core.dashboard.dashboardItem.supportRequirementNonApproved.SupportRequirementNonApprovedPresenter;
 import com.binar.core.dashboard.dashboardItem.supportRequirementNonApproved.SupportRequirementNonApprovedViewImpl;
 import com.binar.generalFunction.GeneralFunction;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 
 public class Dashboard extends VerticalLayout {
 	 
 	GeneralFunction function;
 	GridLayout gridLayout;
 	Panel panel;
 	public Dashboard(GeneralFunction function) {
 		this.function=function;
 		init();
 	}
 	public void init(){
 		this.setSpacing(true);
 		this.setMargin(true);
 		this.setWidth("100%");
 //		this.setHeight("560px");		
 		construct();
 	}
 	public void construct(){
 		panel=new Panel();
 		panel.setWidth("100%");
 		panel.setHeight("550px");
 		this.addComponent(new Label("<h2>Dashboard</h2>", ContentMode.HTML));
 		gridLayout=new GridLayout(2,5);
 		panel.setContent(gridLayout);
 		this.addComponent(panel);
 	}
 	
 //	FarmationMinimumStockFastMovingModel farmationMinimumStockFastMovingModel;
 //	FarmationMinimumStockFastMovingPresenter farmationMinimumStockFastMovingPresenter;
 //	FarmationMinimumStockFastMovingViewImpl farmationMinimumStockFastMovingViewImpl;
 	
 	FarmationGoodsConsumptionModel farmationGoodsConsumptionModel;
 	FarmationGoodsConsumptionViewImpl farmationGoodsConsumptionViewImpl;
 	FarmationGoodsConsumptionPresenter farmationGoodsConsumptionPresenter;
 	
 	FarmationExpiredGoodsModel 	farmationExpiredGoodsModel; 
 	FarmationExpiredGoodsPresenter 	farmationExpiredGoodsPresenter; 
 	FarmationExpiredGoodsViewImpl farmationExpiredGoodsViewImpl; 
 
 	FarmationExpiredGoodsStatusModel farmationExpiredGoodsStatusModel;
 	FarmationExpiredGoodsStatusViewImpl farmationExpiredGoodsStatusViewImpl;
 	FarmationExpiredGoodsStatusPresenter farmationExpiredGoodsStatusPresenter;
 	
 //	FarmationMinimumStockModel 	farmationMinimumStockModel; 
 //	FarmationMinimumStockViewImpl 	farmationMinimumStockViewImpl; 
 //	FarmationMinimumStockPresenter 	farmationMinimumStockPresenter; 
 
 	FarmationGoodsWithIncreasingTrendModel 	farmationGoodsWithIncreasingTrendModel; 
 	FarmationGoodsWithIncreasingTrendPresenter 	farmationGoodsWithIncreasingTrendPresenter; 
 	FarmationGoodsWithIncreasingTrendViewImpl 	farmationGoodsWithIncreasingTrendViewImpl; 
 
 	FarmationRequirementStatusModel farmationRequirementStatusModel;
 	FarmationRequirementStatusPresenter farmationRequirementStatusPresenter;
 	FarmationRequirementStatusViewImpl farmationRequirementStatusViewImpl;
 	
 	
 	public void generateFarmationView(){
 		
 		//Farmation Minimum Stock
 		gridLayout.removeAllComponents();
 //		if(farmationMinimumStockFastMovingModel == null){
 //			farmationMinimumStockFastMovingModel=new FarmationMinimumStockFastMovingModel(function);
 //			farmationMinimumStockFastMovingViewImpl=new FarmationMinimumStockFastMovingViewImpl(function);
 //			farmationMinimumStockFastMovingPresenter= new FarmationMinimumStockFastMovingPresenter(
 //					function ,farmationMinimumStockFastMovingViewImpl, farmationMinimumStockFastMovingModel);
 //		}
 //		gridLayout.addComponent(farmationMinimumStockFastMovingViewImpl,0,0);
 
 //		if(farmationMinimumStockModel==null){
 //			farmationMinimumStockModel = new FarmationMinimumStockModel(function);
 //			farmationMinimumStockViewImpl =new FarmationMinimumStockViewImpl(function);
 //			farmationMinimumStockPresenter = new FarmationMinimumStockPresenter(
 //					function, farmationMinimumStockViewImpl, farmationMinimumStockModel);
 //		}
 //
 //		gridLayout.addComponent(farmationMinimumStockViewImpl, 0, 0);
 		
 		if(farmationGoodsConsumptionModel == null){
 			farmationGoodsConsumptionModel =new FarmationGoodsConsumptionModel(function);
 			farmationGoodsConsumptionViewImpl=new FarmationGoodsConsumptionViewImpl(function);
 			farmationGoodsConsumptionPresenter=new FarmationGoodsConsumptionPresenter(
 					function, farmationGoodsConsumptionViewImpl, farmationGoodsConsumptionModel);
 		}
 		
 		gridLayout.addComponent(farmationGoodsConsumptionViewImpl, 0,0);
 
 		if(farmationExpiredGoodsModel == null){
 			farmationExpiredGoodsModel =new FarmationExpiredGoodsModel(function);
 			farmationExpiredGoodsViewImpl = new FarmationExpiredGoodsViewImpl(function);
 			farmationExpiredGoodsPresenter=new FarmationExpiredGoodsPresenter(
 					function, farmationExpiredGoodsViewImpl, farmationExpiredGoodsModel);
 		}
 		
 		gridLayout.addComponent(farmationExpiredGoodsViewImpl, 0,1);
 		
 		if(farmationExpiredGoodsStatusModel==null){
 			farmationExpiredGoodsStatusModel=new FarmationExpiredGoodsStatusModel(function);
 			farmationExpiredGoodsStatusViewImpl=new FarmationExpiredGoodsStatusViewImpl(function);
 			farmationExpiredGoodsStatusPresenter=new FarmationExpiredGoodsStatusPresenter(
 					function, farmationExpiredGoodsStatusViewImpl, farmationExpiredGoodsStatusModel);
 		}
 		gridLayout.addComponent(farmationExpiredGoodsStatusViewImpl, 1,0);
 		
 		if(farmationRequirementStatusModel == null){
 			farmationRequirementStatusModel =new FarmationRequirementStatusModel(function);
 			farmationRequirementStatusViewImpl =new FarmationRequirementStatusViewImpl(function);
 			farmationRequirementStatusPresenter = new FarmationRequirementStatusPresenter(
 					function, farmationRequirementStatusViewImpl, farmationRequirementStatusModel);
 		}
 		gridLayout.addComponent(farmationRequirementStatusViewImpl, 1,1);	
 		
 		if(farmationGoodsWithIncreasingTrendModel == null){
 			farmationGoodsWithIncreasingTrendViewImpl =new FarmationGoodsWithIncreasingTrendViewImpl(function);
 			farmationGoodsWithIncreasingTrendModel =new FarmationGoodsWithIncreasingTrendModel(function);
 			farmationGoodsWithIncreasingTrendPresenter = new FarmationGoodsWithIncreasingTrendPresenter(
 					function, farmationGoodsWithIncreasingTrendViewImpl, farmationGoodsWithIncreasingTrendModel);
 		}
 		gridLayout.addComponent(farmationGoodsWithIncreasingTrendViewImpl, 0,2, 1,2);			
 	}
 	
 	IfrsDeletionApprovalModel ifrsDeletionApprovalModel;
 	IfrsDeletionApprovalPresenter ifrsDeletionApprovalPresenter;
 	IfrsDeletionApprovalViewImpl ifrsDeletionApprovalViewImpl;
 	
 	IfrsGoodsProcurementViewImpl ifrsGoodsProcurementViewImpl;
 	IfrsGoodsProcurementModel ifrsGoodsProcurementModel;
 	IfrsGoodsProcurementPresenter ifrsGoodsProcurementPresenter;
 	
 	IfrsGoodsReceptionSummaryViewImpl ifrsGoodsReceptionSummaryViewImpl;
 	IfrsGoodsReceptionSummaryPresenter ifrsGoodsReceptionSummaryPresenter;
 	IfrsGoodsReceptionSummaryModel ifrsGoodsReceptionSummaryModel;
 	
 	IfrsRequirementPlanningPresenter ifrsRequirementPlanningPresenter;
 	SupportRequirementNonApprovedModel ifrsRequirementPlanningModel;
 	SupportRequirementNonApprovedViewImpl ifrsRequirementPlanningViewImpl;
 	IfrsGoodsWithIncreasingTrendPresenter ifrsGoodsWithIncreasingTrendPresenter;
 	FarmationGoodsWithIncreasingTrendViewImpl ifrsGoodsWithIncreasingTrendViewImpl;
 	FarmationGoodsWithIncreasingTrendModel ifrsGoodsWithIncreasingTrendModel;
 	
 	IfrsGoodsConsumptionPresenter ifrsGoodsConsumptionPresenter;
 	FarmationGoodsConsumptionModel  ifrsGoodsConsumptionModel;
 	FarmationGoodsConsumptionViewImpl  ifrsGoodsConsumptionViewImpl;
 	
 	FarmationRequirementStatusModel farmationRequirementStatusModelIFRS;
 	FarmationRequirementStatusPresenter farmationRequirementStatusPresenterIFRS;
 	FarmationRequirementStatusViewImpl farmationRequirementStatusViewImplIFRS;
 
 	FarmationExpiredGoodsStatusModel farmationExpiredGoodsStatusModelIFRS;
 	FarmationExpiredGoodsStatusViewImpl farmationExpiredGoodsStatusViewImplIFRS;
 	FarmationExpiredGoodsStatusPresenter farmationExpiredGoodsStatusPresenterIFRS;
 	
 	PpkExpiredGoodsNonAcceptedModel ppkExpiredGoodsNonAcceptedModelIFRS;
 	PpkExpiredGoodsNonAcceptedViewImpl ppkExpiredGoodsNonAcceptedViewImplIFRS;
 	PpkExpiredGoodsNonAcceptedPresenter ppkExpiredGoodsNonAcceptedPresenterIFRS;
 
 
 	public void generateIfrsView(){
 		gridLayout.removeAllComponents();
 		if(farmationExpiredGoodsStatusModelIFRS==null){
 			farmationExpiredGoodsStatusModelIFRS=new FarmationExpiredGoodsStatusModel(function);
 			farmationExpiredGoodsStatusViewImplIFRS=new FarmationExpiredGoodsStatusViewImpl(function);
 			farmationExpiredGoodsStatusPresenterIFRS=new FarmationExpiredGoodsStatusPresenter(
 					function, farmationExpiredGoodsStatusViewImplIFRS, farmationExpiredGoodsStatusModelIFRS);
 		}
 		gridLayout.addComponent(farmationExpiredGoodsStatusViewImplIFRS, 0,0);
 		
 		if(farmationRequirementStatusModelIFRS == null){
 			farmationRequirementStatusModelIFRS =new FarmationRequirementStatusModel(function);
 			farmationRequirementStatusViewImplIFRS =new FarmationRequirementStatusViewImpl(function);
 			farmationRequirementStatusPresenterIFRS = new FarmationRequirementStatusPresenter(
 					function, farmationRequirementStatusViewImplIFRS, farmationRequirementStatusModelIFRS);
 		}
 		gridLayout.addComponent(farmationRequirementStatusViewImplIFRS, 0,1);		
 		
 //		if(ifrsDeletionApprovalModel == null){
 //			ifrsDeletionApprovalModel=new IfrsDeletionApprovalModel(function);
 //			ifrsDeletionApprovalViewImpl=new IfrsDeletionApprovalViewImpl(function);
 //			ifrsDeletionApprovalPresenter= new IfrsDeletionApprovalPresenter(
 //					function ,ifrsDeletionApprovalViewImpl, ifrsDeletionApprovalModel);
 //		}
 //		gridLayout.addComponent(ifrsDeletionApprovalViewImpl,1,0);
 		if(ppkExpiredGoodsNonAcceptedPresenterIFRS == null){
 			ppkExpiredGoodsNonAcceptedModelIFRS =new PpkExpiredGoodsNonAcceptedModel(function);
 			ppkExpiredGoodsNonAcceptedViewImplIFRS =new PpkExpiredGoodsNonAcceptedViewImpl(function);
 			ppkExpiredGoodsNonAcceptedPresenterIFRS = new PpkExpiredGoodsNonAcceptedPresenter(
 					function, ppkExpiredGoodsNonAcceptedViewImplIFRS, ppkExpiredGoodsNonAcceptedModelIFRS);
 		}
 		gridLayout.addComponent(ppkExpiredGoodsNonAcceptedViewImplIFRS,1,0);
 
 		if(ifrsRequirementPlanningPresenter == null){
 			ifrsRequirementPlanningModel =new SupportRequirementNonApprovedModel(function);
 			ifrsRequirementPlanningViewImpl =new SupportRequirementNonApprovedViewImpl(function);
 			ifrsRequirementPlanningPresenter = new IfrsRequirementPlanningPresenter(
 					function, ifrsRequirementPlanningViewImpl, ifrsRequirementPlanningModel);
 		}
		gridLayout.addComponent(supportRequirementNonApprovedViewImpl,1,1);
 
 		if(ifrsGoodsProcurementPresenter == null){
 			ifrsGoodsProcurementModel =new IfrsGoodsProcurementModel(function);
 			ifrsGoodsProcurementViewImpl =new IfrsGoodsProcurementViewImpl(function);
 			ifrsGoodsProcurementPresenter = new IfrsGoodsProcurementPresenter(
 					function, ifrsGoodsProcurementViewImpl, ifrsGoodsProcurementModel);
 		}
 		gridLayout.addComponent(ifrsGoodsProcurementViewImpl,0,2);
 
 
 		if(ifrsGoodsReceptionSummaryPresenter == null){
 			ifrsGoodsReceptionSummaryViewImpl =new IfrsGoodsReceptionSummaryViewImpl(function);
 			ifrsGoodsReceptionSummaryModel =new IfrsGoodsReceptionSummaryModel(function);
 			ifrsGoodsReceptionSummaryPresenter = new IfrsGoodsReceptionSummaryPresenter(
 					function, ifrsGoodsReceptionSummaryViewImpl, ifrsGoodsReceptionSummaryModel);
 		}
 		gridLayout.addComponent(ifrsGoodsReceptionSummaryViewImpl,1,2);
 		if(ifrsGoodsConsumptionPresenter == null){
 			ifrsGoodsConsumptionModel =new FarmationGoodsConsumptionModel(function);
 			ifrsGoodsConsumptionViewImpl=new FarmationGoodsConsumptionViewImpl(function);
 			ifrsGoodsConsumptionPresenter=new IfrsGoodsConsumptionPresenter(
 					function, ifrsGoodsConsumptionViewImpl, ifrsGoodsConsumptionModel);
 		}
 		
 		gridLayout.addComponent(farmationGoodsConsumptionViewImpl, 0,3);
 		
 
 		
 		if(ifrsGoodsWithIncreasingTrendPresenter == null){
 			ifrsGoodsWithIncreasingTrendViewImpl =new FarmationGoodsWithIncreasingTrendViewImpl(function);
 			ifrsGoodsWithIncreasingTrendModel =new FarmationGoodsWithIncreasingTrendModel(function);
 			ifrsGoodsWithIncreasingTrendPresenter = new IfrsGoodsWithIncreasingTrendPresenter(
 					function, ifrsGoodsWithIncreasingTrendViewImpl, ifrsGoodsWithIncreasingTrendModel);
 		}
 		gridLayout.addComponent(farmationGoodsWithIncreasingTrendViewImpl, 0,4, 1,4);			
 
 
 	}
 	
 	PpkExpiredGoodsNonAcceptedModel ppkExpiredGoodsNonAcceptedModel;
 	PpkExpiredGoodsNonAcceptedViewImpl ppkExpiredGoodsNonAcceptedViewImpl;
 	PpkExpiredGoodsNonAcceptedPresenter ppkExpiredGoodsNonAcceptedPresenter;
 	
 	PpkGoodsProcurementPresenter ppkGoodsProcurementPresenter;
 	IfrsGoodsProcurementViewImpl ppkGoodsProcurementViewImpl;
 	IfrsGoodsProcurementModel ppkGoodsProcurementModel;
 	
 	
 	PpkRequirementPlanningPresenter ppkRequirementPlanningPresenter;
 	
 	public void generatePPKView(){
 		gridLayout.removeAllComponents();
 		
 		if(farmationExpiredGoodsStatusModel==null){
 			farmationExpiredGoodsStatusModel=new FarmationExpiredGoodsStatusModel(function);
 			farmationExpiredGoodsStatusViewImpl=new FarmationExpiredGoodsStatusViewImpl(function);
 			farmationExpiredGoodsStatusPresenter=new FarmationExpiredGoodsStatusPresenter(
 					function, farmationExpiredGoodsStatusViewImpl, farmationExpiredGoodsStatusModel);
 		}
 		gridLayout.addComponent(farmationExpiredGoodsStatusViewImpl, 0,0);
 		
 		if(farmationRequirementStatusModel == null){
 			farmationRequirementStatusModel =new FarmationRequirementStatusModel(function);
 			farmationRequirementStatusViewImpl =new FarmationRequirementStatusViewImpl(function);
 			farmationRequirementStatusPresenter = new FarmationRequirementStatusPresenter(
 					function, farmationRequirementStatusViewImpl, farmationRequirementStatusModel);
 		}
 		gridLayout.addComponent(farmationRequirementStatusViewImpl, 0,1);		
 		
 		if(ifrsDeletionApprovalModel == null){
 			ifrsDeletionApprovalModel=new IfrsDeletionApprovalModel(function);
 			ifrsDeletionApprovalViewImpl=new IfrsDeletionApprovalViewImpl(function);
 			ifrsDeletionApprovalPresenter= new IfrsDeletionApprovalPresenter(
 					function ,ifrsDeletionApprovalViewImpl, ifrsDeletionApprovalModel);
 		}
 		gridLayout.addComponent(ifrsDeletionApprovalViewImpl,1,0);
 
 
 		if(ppkGoodsProcurementPresenter == null){
 			ppkGoodsProcurementModel =new IfrsGoodsProcurementModel(function);
 			ppkGoodsProcurementViewImpl =new IfrsGoodsProcurementViewImpl(function);
 			ppkGoodsProcurementPresenter = new PpkGoodsProcurementPresenter(
 					function, ppkGoodsProcurementViewImpl
 					, ppkGoodsProcurementModel);
 		}
 		gridLayout.addComponent(ifrsGoodsProcurementViewImpl,1,1);
 		
 		if(ppkRequirementPlanningPresenter == null){
 			supportRequirementNonApprovedModel =new SupportRequirementNonApprovedModel(function);
 			supportRequirementNonApprovedViewImpl =new SupportRequirementNonApprovedViewImpl(function);
 			ppkRequirementPlanningPresenter = new PpkRequirementPlanningPresenter(
 					function,supportRequirementNonApprovedViewImpl, supportRequirementNonApprovedModel);
 		}
 		
 		gridLayout.addComponent(supportRequirementNonApprovedViewImpl,0,2);
 
 		
 		if(ppkExpiredGoodsNonAcceptedPresenter == null){
 			ppkExpiredGoodsNonAcceptedModel =new PpkExpiredGoodsNonAcceptedModel(function);
 			ppkExpiredGoodsNonAcceptedViewImpl =new PpkExpiredGoodsNonAcceptedViewImpl(function);
 			ppkExpiredGoodsNonAcceptedPresenter = new PpkExpiredGoodsNonAcceptedPresenter(
 					function, ppkExpiredGoodsNonAcceptedViewImpl, ppkExpiredGoodsNonAcceptedModel);
 		}
 		gridLayout.addComponent(ppkExpiredGoodsNonAcceptedViewImpl,1,2);
 
 
 	}
 	
 	ProcurementDueDateModel procurementDueDateModel;
 	ProcurementDueDateViewImpl procurementDueDateViewImpl;
 	ProcurementDueDatePresenter procurementDueDatePresenter;
 	
 	ProcurementGoodsProcurementPresenter procurementGoodsProcurementPresenter;
 	
 	ProcurementReceiptPresenter procurementReceiptPresenter;
 	
 	ProcurementRequirementAcceptanceModel procurementRequirementAcceptanceModel;
 	ProcurementRequirementAcceptanceViewImpl procurementRequirementAcceptanceViewImpl;
 	ProcurementRequirementAcceptancePresenter procurementRequirementAcceptancePresenter;
 	
 	ProcurementSupplierRankModel procurementSupplierRankModel;
 	ProcurementSupplierRankViewImpl procurementSupplierRankViewImpl;
 	ProcurementSupplierRankPresenter procurementSupplierRankPresenter;
 	
 	public void generateProcurementView(){
 		gridLayout.removeAllComponents();
 		if(procurementGoodsProcurementPresenter == null){
 			ifrsGoodsProcurementModel =new IfrsGoodsProcurementModel(function);
 			ifrsGoodsProcurementViewImpl =new IfrsGoodsProcurementViewImpl(function);
 			procurementGoodsProcurementPresenter = new ProcurementGoodsProcurementPresenter(
 					function, ifrsGoodsProcurementViewImpl, ifrsGoodsProcurementModel);
 		}
 		gridLayout.addComponent(ifrsGoodsProcurementViewImpl,0,0);
 
 		if(procurementReceiptPresenter == null){
 			ifrsGoodsReceptionSummaryViewImpl =new IfrsGoodsReceptionSummaryViewImpl(function);
 			ifrsGoodsReceptionSummaryModel =new IfrsGoodsReceptionSummaryModel(function);
 			procurementReceiptPresenter = new ProcurementReceiptPresenter(
 					function, ifrsGoodsReceptionSummaryViewImpl, ifrsGoodsReceptionSummaryModel);
 		}
 		gridLayout.addComponent(ifrsGoodsReceptionSummaryViewImpl,1,0);
 
 		if(procurementSupplierRankPresenter == null){
 			procurementSupplierRankModel =new ProcurementSupplierRankModel(function);
 			procurementSupplierRankViewImpl =new ProcurementSupplierRankViewImpl(function);
 			procurementSupplierRankPresenter = new ProcurementSupplierRankPresenter(
 					function, procurementSupplierRankViewImpl, procurementSupplierRankModel);
 		}
 		gridLayout.addComponent(procurementSupplierRankViewImpl,0,1);
 
 		
 		if(procurementDueDatePresenter == null){
 			procurementDueDateModel =new ProcurementDueDateModel(function);
 			procurementDueDateViewImpl =new ProcurementDueDateViewImpl(function);
 			procurementDueDatePresenter = new ProcurementDueDatePresenter(
 					function, procurementDueDateViewImpl, procurementDueDateModel);
 		}
 		gridLayout.addComponent(procurementDueDateViewImpl,1,1);
 
 
 
 		if(procurementRequirementAcceptancePresenter == null){
 			procurementRequirementAcceptanceViewImpl =new ProcurementRequirementAcceptanceViewImpl(function);
 			procurementRequirementAcceptanceModel =new ProcurementRequirementAcceptanceModel(function);
 			procurementRequirementAcceptancePresenter = new ProcurementRequirementAcceptancePresenter(
 					function, procurementRequirementAcceptanceViewImpl, procurementRequirementAcceptanceModel);
 		}
 		gridLayout.addComponent(procurementRequirementAcceptanceViewImpl,0,2);
 
 
 		
 
 		
 	}
 	
 	SupportDeletedGoodsNonApprovedPresenter supportDeletedGoodsNonApprovedPresenter;
 
 	SupportGoodsProcurementSummaryPresenter supportGoodsProcurementSummaryPresenter;
 	
 	SupportRequirementNonApprovedModel supportRequirementNonApprovedModel;
 	SupportRequirementNonApprovedViewImpl supportRequirementNonApprovedViewImpl;
 	SupportRequirementNonApprovedPresenter supportRequirementNonApprovedPresenter;
 	
 	SupportGoodsWithIncreasingTrendPresenter supportGoodsWithIncreasingTrendPresenter;
 	
 	SupportGoodsConsumptionPresenter supportGoodsConsumptionPresenter;
 	
 	PpkExpiredGoodsNonAcceptedModel ppkExpiredGoodsNonAcceptedModelSupport;
 	PpkExpiredGoodsNonAcceptedViewImpl ppkExpiredGoodsNonAcceptedViewImplSupport;
 	PpkExpiredGoodsNonAcceptedPresenter ppkExpiredGoodsNonAcceptedPresenterSupport;
 
 	public void generateSupportView(){
 		gridLayout.removeAllComponents();
 		if(farmationExpiredGoodsStatusModel==null){
 			farmationExpiredGoodsStatusModel=new FarmationExpiredGoodsStatusModel(function);
 			farmationExpiredGoodsStatusViewImpl=new FarmationExpiredGoodsStatusViewImpl(function);
 			farmationExpiredGoodsStatusPresenter=new FarmationExpiredGoodsStatusPresenter(
 					function, farmationExpiredGoodsStatusViewImpl, farmationExpiredGoodsStatusModel);
 		}
 		gridLayout.addComponent(farmationExpiredGoodsStatusViewImpl, 0,0);
 		
 		if(farmationRequirementStatusModel == null){
 			farmationRequirementStatusModel =new FarmationRequirementStatusModel(function);
 			farmationRequirementStatusViewImpl =new FarmationRequirementStatusViewImpl(function);
 			farmationRequirementStatusPresenter = new FarmationRequirementStatusPresenter(
 					function, farmationRequirementStatusViewImpl, farmationRequirementStatusModel);
 		}
 		gridLayout.addComponent(farmationRequirementStatusViewImpl, 0,1);		
 		
 //		if(supportRequirementNonApprovedPresenter == null){
 //			supportRequirementNonApprovedModel =new SupportRequirementNonApprovedModel(function);
 //			supportRequirementNonApprovedViewImpl =new SupportRequirementNonApprovedViewImpl(function);
 //			supportRequirementNonApprovedPresenter = new SupportRequirementNonApprovedPresenter(
 //					function, supportRequirementNonApprovedViewImpl, supportRequirementNonApprovedModel);
 //		}
 //		gridLayout.addComponent(supportRequirementNonApprovedViewImpl,1,0);
 		if(supportGoodsConsumptionPresenter == null){
 			farmationGoodsConsumptionModel =new FarmationGoodsConsumptionModel(function);
 			farmationGoodsConsumptionViewImpl=new FarmationGoodsConsumptionViewImpl(function);
 			supportGoodsConsumptionPresenter=new SupportGoodsConsumptionPresenter(
 					function, farmationGoodsConsumptionViewImpl, farmationGoodsConsumptionModel);
 		}
 		
 		gridLayout.addComponent(farmationGoodsConsumptionViewImpl, 1,0);
 
 		
 		if(ppkExpiredGoodsNonAcceptedPresenterSupport == null){
 			ppkExpiredGoodsNonAcceptedModelSupport =new PpkExpiredGoodsNonAcceptedModel(function);
 			ppkExpiredGoodsNonAcceptedViewImplSupport =new PpkExpiredGoodsNonAcceptedViewImpl(function);
 			ppkExpiredGoodsNonAcceptedPresenterSupport = new PpkExpiredGoodsNonAcceptedPresenter(
 					function, ppkExpiredGoodsNonAcceptedViewImplSupport, ppkExpiredGoodsNonAcceptedModelSupport);
 		}
 		gridLayout.addComponent(ppkExpiredGoodsNonAcceptedViewImplSupport,1,1);
 
 		if(supportGoodsProcurementSummaryPresenter == null){
 			ifrsGoodsProcurementModel =new IfrsGoodsProcurementModel(function);
 			ifrsGoodsProcurementViewImpl =new IfrsGoodsProcurementViewImpl(function);
 			supportGoodsProcurementSummaryPresenter = new SupportGoodsProcurementSummaryPresenter(
 					function, ifrsGoodsProcurementViewImpl, ifrsGoodsProcurementModel);
 		}
 		gridLayout.addComponent(ifrsGoodsProcurementViewImpl,0,2);
 		if(ifrsGoodsReceptionSummaryPresenter == null){
 			ifrsGoodsReceptionSummaryViewImpl =new IfrsGoodsReceptionSummaryViewImpl(function);
 			ifrsGoodsReceptionSummaryModel =new IfrsGoodsReceptionSummaryModel(function);
 			ifrsGoodsReceptionSummaryPresenter = new IfrsGoodsReceptionSummaryPresenter(
 					function, ifrsGoodsReceptionSummaryViewImpl, ifrsGoodsReceptionSummaryModel);
 		}
 
 		gridLayout.addComponent(ifrsGoodsReceptionSummaryViewImpl,1,2);
 		
 		
 
 		
 		if(supportGoodsWithIncreasingTrendPresenter == null){
 			farmationGoodsWithIncreasingTrendViewImpl =new FarmationGoodsWithIncreasingTrendViewImpl(function);
 			farmationGoodsWithIncreasingTrendModel =new FarmationGoodsWithIncreasingTrendModel(function);
 			supportGoodsWithIncreasingTrendPresenter = new SupportGoodsWithIncreasingTrendPresenter(
 					function, farmationGoodsWithIncreasingTrendViewImpl, farmationGoodsWithIncreasingTrendModel);
 		}
 		gridLayout.addComponent(farmationGoodsWithIncreasingTrendViewImpl, 0,4, 1, 4);
 		
 		
 		
 	}
 	
 }
