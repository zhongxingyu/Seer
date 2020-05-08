 package com.jdc.themis.dealer.web.domain;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class SaveDealerPostSalesLedgerRequest implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private Integer workOrderNo;
 	
 	private Integer dealerID;
 	
 	private String salesDate;
 	
 	private Double mileage;
 	
 	private String lpNumber;
 	
 	private String customerName;
 	
 	private String color;
 	
 	private String frameNo;
 	
 	private String model;
 	
 	private String enterFactoryDate;
 
 	private String exitFactoryDate;
 	
 	private String customerType;
 	
	private String insuranceAgengcy;
 	
 	private String insuranceDueDate;
 	
 	private Integer insuranceClaimNumber;
 	
 	private String maintenancePostSalesConsultant;
 	
 	private String maintenanceTechnician;
 	
 	private String maintenanceType;
 	
 	private String insuranceType;
 	
 	private String bodyShopPostSalesConsultant;
 	
 	private String sheetMetalTechinician;
 	
 	private String paintSprayTechinician;
 	
 	private Boolean mineralEngineOil;
 	
 	private Boolean synthesizedEngineOil;
 	   
 	private Boolean airFilterElement;
 	
 	private Boolean airConditionerFilterElement;
 	
 	private Boolean gasolineFilterElement;
 	
 	private Boolean brakeFluid;
 	
 	private Boolean sparkPlug;
 	
 	private Boolean directionEngineOil;
 	
 	private Boolean transmissionOil;
 	
 	private Boolean coolingFluid;
 	
 	private Boolean timingChain;
 	
 	private Boolean lubricationSystemClean;
 	
 	private Boolean transmissionClean;
 	
 	private Boolean directionSystemClean;
 	
 	private Boolean coolingSystemClean;
 	   
 	private Boolean airConditionerClean;
 	
 	private Boolean fuelSystemClean;
 	
 	private Boolean otherSystemClean;
 	
 	private Integer maintenanceProductAmount;
 	
 	private Integer tyreCount;
 	
 	private Integer batteryCount;
 	
 	private Integer sparkPlugCount;
 	
 	private Integer wiperBladeCount;
 
 	private Double maintenanceManHourRevenuePaidPart;
 	
 	private Double maintenanceManHourDiscountPaidPart;
 	
 	private Double maintenanceManHourCouponPaidPart;
 	
 	private Double maintenanceManHourCostPaidPart;
 	
 	private Double maintenanceManHourRevenueUnpaidPart;
 	
 	private Double maintenanceManHourCostUnpaidPart;
 	
 	private Double sheetMetalManHourRevenue;
 	
 	private Double paintSprayManHourRevenue;
 	
 	private Double bodyShopManHourDiscount;
 	
 	private Double bodyShopManHourCoupon;
 	
 	private Double bodyShopManHourCost;
 	
 	private Double accessoryManHourRevenue;
 	
 	private Double accessoryManHourDiscount;
 	
 	private Double accessoryManHourCoupon;
 	
 	private Double accessoryManHourCost;
 	
 	private Double otherManHourRevenue;
 	
 	private Double otherManHourDiscount;
 	
 	private Double otherManHourCoupon;
 	
 	private Double otherManHourCost;
 	
 	private Double maintenancePartsRevenue;
 	
 	private Double maintenancePartsDiscount;
 	
 	private Double maintenancePartsCoupon;
 	
 	private Double maintenancePartsCost;
 	
 	private Double accessoryPartsRevenue;
 	
 	private Double accessoryPartsDiscount;
 	
 	private Double accessoryPartsCoupon;
 	
 	private Double accessoryPartsCost;
 	
 	private Double maintenanceProductRevenue;
 	
 	private Double maintenanceProductDiscount;
 	
 	private Double maintenanceProductCoupon;
 	
 	private Double maintenanceProductCost;
 	
 	private Double vehicleDecorationRevenue;
 	
 	private Double vehicleDecorationDiscount;
 	
 	private Double vehicleDecorationCoupon;
 	
 	private Double vehicleDecorationCost;
 	
 	private String validDate;
 
 	private String updatedBy;
 
 	public Integer getWorkOrderNo() {
 		return workOrderNo;
 	}
 
 	public void setWorkOrderNo(Integer workOrderNo) {
 		this.workOrderNo = workOrderNo;
 	}
 
 	public Integer getDealerID() {
 		return dealerID;
 	}
 
 	public void setDealerID(Integer dealerID) {
 		this.dealerID = dealerID;
 	}
 
 	public String getSalesDate() {
 		return salesDate;
 	}
 
 	public void setSalesDate(String salesDate) {
 		this.salesDate = salesDate;
 	}
 
 	public Double getMileage() {
 		return mileage;
 	}
 
 	public void setMileage(Double mileage) {
 		this.mileage = mileage;
 	}
 
 	public String getLpNumber() {
 		return lpNumber;
 	}
 
 	public void setLpNumber(String lpNumber) {
 		this.lpNumber = lpNumber;
 	}
 
 	public String getCustomerName() {
 		return customerName;
 	}
 
 	public void setCustomerName(String customerName) {
 		this.customerName = customerName;
 	}
 
 	public String getColor() {
 		return color;
 	}
 
 	public void setColor(String color) {
 		this.color = color;
 	}
 
 	public String getFrameNo() {
 		return frameNo;
 	}
 
 	public void setFrameNo(String frameNo) {
 		this.frameNo = frameNo;
 	}
 
 	public String getModel() {
 		return model;
 	}
 
 	public void setModel(String model) {
 		this.model = model;
 	}
 
 	public String getEnterFactoryDate() {
 		return enterFactoryDate;
 	}
 
 	public void setEnterFactoryDate(String enterFactoryDate) {
 		this.enterFactoryDate = enterFactoryDate;
 	}
 
 	public String getExitFactoryDate() {
 		return exitFactoryDate;
 	}
 
 	public void setExitFactoryDate(String exitFactoryDate) {
 		this.exitFactoryDate = exitFactoryDate;
 	}
 
 	public String getCustomerType() {
 		return customerType;
 	}
 
 	public void setCustomerType(String customerType) {
 		this.customerType = customerType;
 	}
 
	public String getInsuranceAgengcy() {
		return insuranceAgengcy;
 	}
 
	public void setInsuranceAgengcy(String insuranceAgengcy) {
		this.insuranceAgengcy = insuranceAgengcy;
 	}
 
 	public String getInsuranceDueDate() {
 		return insuranceDueDate;
 	}
 
 	public void setInsuranceDueDate(String insuranceDueDate) {
 		this.insuranceDueDate = insuranceDueDate;
 	}
 
 	public Integer getInsuranceClaimNumber() {
 		return insuranceClaimNumber;
 	}
 
 	public void setInsuranceClaimNumber(Integer insuranceClaimNumber) {
 		this.insuranceClaimNumber = insuranceClaimNumber;
 	}
 
 	public String getMaintenancePostSalesConsultant() {
 		return maintenancePostSalesConsultant;
 	}
 
 	public void setMaintenancePostSalesConsultant(
 			String maintenancePostSalesConsultant) {
 		this.maintenancePostSalesConsultant = maintenancePostSalesConsultant;
 	}
 
 	public String getMaintenanceTechnician() {
 		return maintenanceTechnician;
 	}
 
 	public void setMaintenanceTechnician(String maintenanceTechnician) {
 		this.maintenanceTechnician = maintenanceTechnician;
 	}
 
 	public String getMaintenanceType() {
 		return maintenanceType;
 	}
 
 	public void setMaintenanceType(String maintenanceType) {
 		this.maintenanceType = maintenanceType;
 	}
 
 	public String getInsuranceType() {
 		return insuranceType;
 	}
 
 	public void setInsuranceType(String insuranceType) {
 		this.insuranceType = insuranceType;
 	}
 
 	public String getBodyShopPostSalesConsultant() {
 		return bodyShopPostSalesConsultant;
 	}
 
 	public void setBodyShopPostSalesConsultant(String bodyShopPostSalesConsultant) {
 		this.bodyShopPostSalesConsultant = bodyShopPostSalesConsultant;
 	}
 
 	public String getSheetMetalTechinician() {
 		return sheetMetalTechinician;
 	}
 
 	public void setSheetMetalTechinician(String sheetMetalTechinician) {
 		this.sheetMetalTechinician = sheetMetalTechinician;
 	}
 
 	public String getPaintSprayTechinician() {
 		return paintSprayTechinician;
 	}
 
 	public void setPaintSprayTechinician(String paintSprayTechinician) {
 		this.paintSprayTechinician = paintSprayTechinician;
 	}
 
 	public Boolean getMineralEngineOil() {
 		return mineralEngineOil;
 	}
 
 	public void setMineralEngineOil(Boolean mineralEngineOil) {
 		this.mineralEngineOil = mineralEngineOil;
 	}
 
 	public Boolean getSynthesizedEngineOil() {
 		return synthesizedEngineOil;
 	}
 
 	public void setSynthesizedEngineOil(Boolean synthesizedEngineOil) {
 		this.synthesizedEngineOil = synthesizedEngineOil;
 	}
 
 	public Boolean getAirFilterElement() {
 		return airFilterElement;
 	}
 
 	public void setAirFilterElement(Boolean airFilterElement) {
 		this.airFilterElement = airFilterElement;
 	}
 
 	public Boolean getAirConditionerFilterElement() {
 		return airConditionerFilterElement;
 	}
 
 	public void setAirConditionerFilterElement(Boolean airConditionerFilterElement) {
 		this.airConditionerFilterElement = airConditionerFilterElement;
 	}
 
 	public Boolean getGasolineFilterElement() {
 		return gasolineFilterElement;
 	}
 
 	public void setGasolineFilterElement(Boolean gasolineFilterElement) {
 		this.gasolineFilterElement = gasolineFilterElement;
 	}
 
 	public Boolean getBrakeFluid() {
 		return brakeFluid;
 	}
 
 	public void setBrakeFluid(Boolean brakeFluid) {
 		this.brakeFluid = brakeFluid;
 	}
 
 	public Boolean getSparkPlug() {
 		return sparkPlug;
 	}
 
 	public void setSparkPlug(Boolean sparkPlug) {
 		this.sparkPlug = sparkPlug;
 	}
 
 	public Boolean getDirectionEngineOil() {
 		return directionEngineOil;
 	}
 
 	public void setDirectionEngineOil(Boolean directionEngineOil) {
 		this.directionEngineOil = directionEngineOil;
 	}
 
 	public Boolean getTransmissionOil() {
 		return transmissionOil;
 	}
 
 	public void setTransmissionOil(Boolean transmissionOil) {
 		this.transmissionOil = transmissionOil;
 	}
 
 	public Boolean getCoolingFluid() {
 		return coolingFluid;
 	}
 
 	public void setCoolingFluid(Boolean coolingFluid) {
 		this.coolingFluid = coolingFluid;
 	}
 
 	public Boolean getTimingChain() {
 		return timingChain;
 	}
 
 	public void setTimingChain(Boolean timingChain) {
 		this.timingChain = timingChain;
 	}
 
 	public Boolean getLubricationSystemClean() {
 		return lubricationSystemClean;
 	}
 
 	public void setLubricationSystemClean(Boolean lubricationSystemClean) {
 		this.lubricationSystemClean = lubricationSystemClean;
 	}
 
 	public Boolean getTransmissionClean() {
 		return transmissionClean;
 	}
 
 	public void setTransmissionClean(Boolean transmissionClean) {
 		this.transmissionClean = transmissionClean;
 	}
 
 	public Boolean getDirectionSystemClean() {
 		return directionSystemClean;
 	}
 
 	public void setDirectionSystemClean(Boolean directionSystemClean) {
 		this.directionSystemClean = directionSystemClean;
 	}
 
 	public Boolean getCoolingSystemClean() {
 		return coolingSystemClean;
 	}
 
 	public void setCoolingSystemClean(Boolean coolingSystemClean) {
 		this.coolingSystemClean = coolingSystemClean;
 	}
 
 	public Boolean getAirConditionerClean() {
 		return airConditionerClean;
 	}
 
 	public void setAirConditionerClean(Boolean airConditionerClean) {
 		this.airConditionerClean = airConditionerClean;
 	}
 
 	public Boolean getFuelSystemClean() {
 		return fuelSystemClean;
 	}
 
 	public void setFuelSystemClean(Boolean fuelSystemClean) {
 		this.fuelSystemClean = fuelSystemClean;
 	}
 
 	public Boolean getOtherSystemClean() {
 		return otherSystemClean;
 	}
 
 	public void setOtherSystemClean(Boolean otherSystemClean) {
 		this.otherSystemClean = otherSystemClean;
 	}
 
 	public Integer getMaintenanceProductAmount() {
 		return maintenanceProductAmount;
 	}
 
 	public void setMaintenanceProductAmount(Integer maintenanceProductAmount) {
 		this.maintenanceProductAmount = maintenanceProductAmount;
 	}
 
 	public Integer getTyreCount() {
 		return tyreCount;
 	}
 
 	public void setTyreCount(Integer tyreCount) {
 		this.tyreCount = tyreCount;
 	}
 
 	public Integer getBatteryCount() {
 		return batteryCount;
 	}
 
 	public void setBatteryCount(Integer batteryCount) {
 		this.batteryCount = batteryCount;
 	}
 
 	public Integer getSparkPlugCount() {
 		return sparkPlugCount;
 	}
 
 	public void setSparkPlugCount(Integer sparkPlugCount) {
 		this.sparkPlugCount = sparkPlugCount;
 	}
 
 	public Integer getWiperBladeCount() {
 		return wiperBladeCount;
 	}
 
 	public void setWiperBladeCount(Integer wiperBladeCount) {
 		this.wiperBladeCount = wiperBladeCount;
 	}
 
 	public Double getMaintenanceManHourRevenuePaidPart() {
 		return maintenanceManHourRevenuePaidPart;
 	}
 
 	public void setMaintenanceManHourRevenuePaidPart(
 			Double maintenanceManHourRevenuePaidPart) {
 		this.maintenanceManHourRevenuePaidPart = maintenanceManHourRevenuePaidPart;
 	}
 
 	public Double getMaintenanceManHourDiscountPaidPart() {
 		return maintenanceManHourDiscountPaidPart;
 	}
 
 	public void setMaintenanceManHourDiscountPaidPart(
 			Double maintenanceManHourDiscountPaidPart) {
 		this.maintenanceManHourDiscountPaidPart = maintenanceManHourDiscountPaidPart;
 	}
 
 	public Double getMaintenanceManHourCouponPaidPart() {
 		return maintenanceManHourCouponPaidPart;
 	}
 
 	public void setMaintenanceManHourCouponPaidPart(
 			Double maintenanceManHourCouponPaidPart) {
 		this.maintenanceManHourCouponPaidPart = maintenanceManHourCouponPaidPart;
 	}
 
 	public Double getMaintenanceManHourCostPaidPart() {
 		return maintenanceManHourCostPaidPart;
 	}
 
 	public void setMaintenanceManHourCostPaidPart(
 			Double maintenanceManHourCostPaidPart) {
 		this.maintenanceManHourCostPaidPart = maintenanceManHourCostPaidPart;
 	}
 
 	public Double getMaintenanceManHourRevenueUnpaidPart() {
 		return maintenanceManHourRevenueUnpaidPart;
 	}
 
 	public void setMaintenanceManHourRevenueUnpaidPart(
 			Double maintenanceManHourRevenueUnpaidPart) {
 		this.maintenanceManHourRevenueUnpaidPart = maintenanceManHourRevenueUnpaidPart;
 	}
 
 	public Double getMaintenanceManHourCostUnpaidPart() {
 		return maintenanceManHourCostUnpaidPart;
 	}
 
 	public void setMaintenanceManHourCostUnpaidPart(
 			Double maintenanceManHourCostUnpaidPart) {
 		this.maintenanceManHourCostUnpaidPart = maintenanceManHourCostUnpaidPart;
 	}
 
 	public Double getSheetMetalManHourRevenue() {
 		return sheetMetalManHourRevenue;
 	}
 
 	public void setSheetMetalManHourRevenue(Double sheetMetalManHourRevenue) {
 		this.sheetMetalManHourRevenue = sheetMetalManHourRevenue;
 	}
 
 	public Double getPaintSprayManHourRevenue() {
 		return paintSprayManHourRevenue;
 	}
 
 	public void setPaintSprayManHourRevenue(Double paintSprayManHourRevenue) {
 		this.paintSprayManHourRevenue = paintSprayManHourRevenue;
 	}
 
 	public Double getBodyShopManHourDiscount() {
 		return bodyShopManHourDiscount;
 	}
 
 	public void setBodyShopManHourDiscount(Double bodyShopManHourDiscount) {
 		this.bodyShopManHourDiscount = bodyShopManHourDiscount;
 	}
 
 	public Double getBodyShopManHourCoupon() {
 		return bodyShopManHourCoupon;
 	}
 
 	public void setBodyShopManHourCoupon(Double bodyShopManHourCoupon) {
 		this.bodyShopManHourCoupon = bodyShopManHourCoupon;
 	}
 
 	public Double getBodyShopManHourCost() {
 		return bodyShopManHourCost;
 	}
 
 	public void setBodyShopManHourCost(Double bodyShopManHourCost) {
 		this.bodyShopManHourCost = bodyShopManHourCost;
 	}
 
 	public Double getAccessoryManHourRevenue() {
 		return accessoryManHourRevenue;
 	}
 
 	public void setAccessoryManHourRevenue(Double accessoryManHourRevenue) {
 		this.accessoryManHourRevenue = accessoryManHourRevenue;
 	}
 
 	public Double getAccessoryManHourDiscount() {
 		return accessoryManHourDiscount;
 	}
 
 	public void setAccessoryManHourDiscount(Double accessoryManHourDiscount) {
 		this.accessoryManHourDiscount = accessoryManHourDiscount;
 	}
 
 	public Double getAccessoryManHourCoupon() {
 		return accessoryManHourCoupon;
 	}
 
 	public void setAccessoryManHourCoupon(Double accessoryManHourCoupon) {
 		this.accessoryManHourCoupon = accessoryManHourCoupon;
 	}
 
 	public Double getAccessoryManHourCost() {
 		return accessoryManHourCost;
 	}
 
 	public void setAccessoryManHourCost(Double accessoryManHourCost) {
 		this.accessoryManHourCost = accessoryManHourCost;
 	}
 
 	public Double getOtherManHourRevenue() {
 		return otherManHourRevenue;
 	}
 
 	public void setOtherManHourRevenue(Double otherManHourRevenue) {
 		this.otherManHourRevenue = otherManHourRevenue;
 	}
 
 	public Double getOtherManHourDiscount() {
 		return otherManHourDiscount;
 	}
 
 	public void setOtherManHourDiscount(Double otherManHourDiscount) {
 		this.otherManHourDiscount = otherManHourDiscount;
 	}
 
 	public Double getOtherManHourCoupon() {
 		return otherManHourCoupon;
 	}
 
 	public void setOtherManHourCoupon(Double otherManHourCoupon) {
 		this.otherManHourCoupon = otherManHourCoupon;
 	}
 
 	public Double getOtherManHourCost() {
 		return otherManHourCost;
 	}
 
 	public void setOtherManHourCost(Double otherManHourCost) {
 		this.otherManHourCost = otherManHourCost;
 	}
 
 	public Double getMaintenancePartsRevenue() {
 		return maintenancePartsRevenue;
 	}
 
 	public void setMaintenancePartsRevenue(Double maintenancePartsRevenue) {
 		this.maintenancePartsRevenue = maintenancePartsRevenue;
 	}
 
 	public Double getMaintenancePartsDiscount() {
 		return maintenancePartsDiscount;
 	}
 
 	public void setMaintenancePartsDiscount(Double maintenancePartsDiscount) {
 		this.maintenancePartsDiscount = maintenancePartsDiscount;
 	}
 
 	public Double getMaintenancePartsCoupon() {
 		return maintenancePartsCoupon;
 	}
 
 	public void setMaintenancePartsCoupon(Double maintenancePartsCoupon) {
 		this.maintenancePartsCoupon = maintenancePartsCoupon;
 	}
 
 	public Double getMaintenancePartsCost() {
 		return maintenancePartsCost;
 	}
 
 	public void setMaintenancePartsCost(Double maintenancePartsCost) {
 		this.maintenancePartsCost = maintenancePartsCost;
 	}
 
 	public Double getAccessoryPartsRevenue() {
 		return accessoryPartsRevenue;
 	}
 
 	public void setAccessoryPartsRevenue(Double accessoryPartsRevenue) {
 		this.accessoryPartsRevenue = accessoryPartsRevenue;
 	}
 
 	public Double getAccessoryPartsDiscount() {
 		return accessoryPartsDiscount;
 	}
 
 	public void setAccessoryPartsDiscount(Double accessoryPartsDiscount) {
 		this.accessoryPartsDiscount = accessoryPartsDiscount;
 	}
 
 	public Double getAccessoryPartsCoupon() {
 		return accessoryPartsCoupon;
 	}
 
 	public void setAccessoryPartsCoupon(Double accessoryPartsCoupon) {
 		this.accessoryPartsCoupon = accessoryPartsCoupon;
 	}
 
 	public Double getAccessoryPartsCost() {
 		return accessoryPartsCost;
 	}
 
 	public void setAccessoryPartsCost(Double accessoryPartsCost) {
 		this.accessoryPartsCost = accessoryPartsCost;
 	}
 
 	public Double getMaintenanceProductRevenue() {
 		return maintenanceProductRevenue;
 	}
 
 	public void setMaintenanceProductRevenue(Double maintenanceProductRevenue) {
 		this.maintenanceProductRevenue = maintenanceProductRevenue;
 	}
 
 	public Double getMaintenanceProductDiscount() {
 		return maintenanceProductDiscount;
 	}
 
 	public void setMaintenanceProductDiscount(Double maintenanceProductDiscount) {
 		this.maintenanceProductDiscount = maintenanceProductDiscount;
 	}
 
 	public Double getMaintenanceProductCoupon() {
 		return maintenanceProductCoupon;
 	}
 
 	public void setMaintenanceProductCoupon(Double maintenanceProductCoupon) {
 		this.maintenanceProductCoupon = maintenanceProductCoupon;
 	}
 
 	public Double getMaintenanceProductCost() {
 		return maintenanceProductCost;
 	}
 
 	public void setMaintenanceProductCost(Double maintenanceProductCost) {
 		this.maintenanceProductCost = maintenanceProductCost;
 	}
 
 	public Double getVehicleDecorationRevenue() {
 		return vehicleDecorationRevenue;
 	}
 
 	public void setVehicleDecorationRevenue(Double vehicleDecorationRevenue) {
 		this.vehicleDecorationRevenue = vehicleDecorationRevenue;
 	}
 
 	public Double getVehicleDecorationDiscount() {
 		return vehicleDecorationDiscount;
 	}
 
 	public void setVehicleDecorationDiscount(Double vehicleDecorationDiscount) {
 		this.vehicleDecorationDiscount = vehicleDecorationDiscount;
 	}
 
 	public Double getVehicleDecorationCoupon() {
 		return vehicleDecorationCoupon;
 	}
 
 	public void setVehicleDecorationCoupon(Double vehicleDecorationCoupon) {
 		this.vehicleDecorationCoupon = vehicleDecorationCoupon;
 	}
 
 	public Double getVehicleDecorationCost() {
 		return vehicleDecorationCost;
 	}
 
 	public void setVehicleDecorationCost(Double vehicleDecorationCost) {
 		this.vehicleDecorationCost = vehicleDecorationCost;
 	}
 
 	public String getValidDate() {
 		return validDate;
 	}
 
 	public void setValidDate(String validDate) {
 		this.validDate = validDate;
 	}
 
 	public String getUpdatedBy() {
 		return updatedBy;
 	}
 
 	public void setUpdatedBy(String updatedBy) {
 		this.updatedBy = updatedBy;
 	}
 	
 }
