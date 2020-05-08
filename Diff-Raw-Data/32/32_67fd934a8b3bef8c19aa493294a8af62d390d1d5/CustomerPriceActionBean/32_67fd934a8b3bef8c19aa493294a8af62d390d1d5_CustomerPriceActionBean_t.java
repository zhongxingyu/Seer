 package nz.ac.victoria.ecs.kpsmart.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.HandlesEvent;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.CustomerPriceDeleteEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.CustomerPriceUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.DomesticCustomerPriceUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.state.CustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Direction;
 import nz.ac.victoria.ecs.kpsmart.entities.state.DomesticCustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Priority;
 import nz.ac.victoria.ecs.kpsmart.resolutions.FormValidationResolution;
 
 @UrlBinding("/event/customerprice?{$event}")
 public class CustomerPriceActionBean extends FormActionBean {
 	private long priceId;
 	private String location;
 	private Direction direction;
 	private Priority priority;
 	private float weightPrice;
 	private float volumePrice;
 	private Priority domesticPriority;
 	
 	@DefaultHandler
 	public Resolution customerPricePage() {
 		return new ForwardResolution("/views/event/customerPrice.jsp");
 	}
 	
 	@HandlesEvent("addform")
 	public Resolution addCustomerPriceScreen() {
 		return new ForwardResolution("/views/event/customerPriceForm.jsp");
 	}
 	
 	@HandlesEvent("updateform")
 	public Resolution updateCustomerPriceScreen() {
 		disableFormField(new String[] {"direction", "location", "priority"});
 		
 		CustomerPrice price = getState().getCustomerPriceById(priceId);
 		location = price.getLocation().getName();
 		direction = price.getDirection().flip();
 		priority = price.getPriority();
 		weightPrice = price.getPricePerUnitWeight();
 		volumePrice = price.getPricePerUnitVolume();
 		return new ForwardResolution("/views/event/customerPriceForm.jsp");
 	}
 	
 	@HandlesEvent("updatedomesticform")
 	public Resolution updateDomesticCustomerPriceScreen() {
 		disableFormField(new String[] {"direction", "location", "priority"});
 		
 		DomesticCustomerPrice price = getState().getDomesticCustomerPrice(domesticPriority);
 		priority = price.getPriority();
 		weightPrice = price.getPricePerUnitWeight();
 		volumePrice = price.getPricePerUnitVolume();
 		return new ForwardResolution("/views/event/customerPriceDomesticForm.jsp");
 	}
 	
 	@HandlesEvent("new")
 	public Resolution newCustomerPrice() {
 		CustomerPrice exist = getExistingPrice();
 		
 		if(exist == null) {
 			CustomerPrice price = new CustomerPrice(
 					getState().getLocationForName(location), 
					direction, 
 					priority);
 			price.setPricePerUnitVolume(volumePrice);
 			price.setPricePerUnitWeight(weightPrice);
 			
 			Resolution validation = validatePrice(price);
 			if(validation != null) {
 				return validation;
 			}
 			
 			getEntityManager().performEvent(new CustomerPriceUpdateEvent(price));
 			
 			return new FormValidationResolution(true, null, null);
 		}
 		else {
 			return existsResolution();
 		}
 	}
 	
 	private Resolution existsResolution() {
 		return new FormValidationResolution(false, new String[]{"location"}, new String[]{"A customer price for this location, direction and priority already exists"});
 	}
 
 	private CustomerPrice getExistingPrice() {
 		CustomerPrice exist = null;
 		
 		switch(direction.flip()) {
 			case To:
 				exist = getState().getCustomerPrice(null, getState().getLocationForName(location), priority);
 				break;
 			case From:
 				exist = getState().getCustomerPrice(getState().getLocationForName(location), null, priority);
 		}
 		
 		return exist;
 	}
 
 	private Resolution validatePrice(CustomerPrice price) {
 		Map<String, String> errors = new HashMap<String, String>();
 		if(!price.getLocation().isInternational()) {
 			errors.put("location", "The location must be international");
 		}
 		if(!Priority.internationalPriorities().contains(price.getPriority())) {
 			errors.put("priority", "The priority must be international");
 		}
 		if(price.getPricePerUnitVolume() <= 0) {
 			errors.put("volumePrice", "The price per cm&sup3; must be greater than zero");
 		}
 		if(price.getPricePerUnitWeight() <= 0) {
 			errors.put("weightPrice", "The price per gram must be greater than zero");
 		}
 		
 		if(errors.size() > 0) {
 			return new FormValidationResolution(false, errors);
 		}
 		else {
 			return null;
 		}
 	}
 	
 	private Resolution validateDomesticPrice(DomesticCustomerPrice price) {
 		Map<String, String> errors = new HashMap<String, String>();
 		if(price.getPricePerUnitVolume() <= 0) {
 			errors.put("volumePrice", "The price per cm&sup3; must be greater than zero");
 		}
 		if(price.getPricePerUnitWeight() <= 0) {
 			errors.put("weightPrice", "The price per gram must be greater than zero");
 		}
 		
 		if(errors.size() > 0) {
 			return new FormValidationResolution(false, errors);
 		}
 		else {
 			return null;
 		}
 	}
 	
 	@HandlesEvent("update")
 	public Resolution updateCustomerPrice() {
 		CustomerPrice exist = getExistingPrice();
 		
 		if(exist == null || exist.getId() == priceId) {
 			CustomerPrice price = getState().getCustomerPriceById(priceId);
 			price.setPricePerUnitVolume(volumePrice);
 			price.setPricePerUnitWeight(weightPrice);
 			
 			Resolution validation = validatePrice(price);
 			if(validation != null) {
 				return validation;
 			}
 			
 			getEntityManager().performEvent(new CustomerPriceUpdateEvent(price));
 			
 			return new FormValidationResolution(true, null, null);
 		}
 		else {
 			return existsResolution();
 		}
 	}
 	
 	@HandlesEvent("updatedomestic")
 	public Resolution updateDomesticCustomerPrice() {
 		DomesticCustomerPrice price = getState().getDomesticCustomerPrice(domesticPriority);
 		price.setPricePerUnitVolume(volumePrice);
 		price.setPricePerUnitWeight(weightPrice);
 		
 		Resolution validation = validateDomesticPrice(price);
 		if(validation != null) {
 			return validation;
 		}
 		
 		getEntityManager().performEvent(new DomesticCustomerPriceUpdateEvent(price));
 		
 		return new FormValidationResolution(true, null, null);
 	}
 	
 	@HandlesEvent("delete")
 	public Resolution deletePrice() {
 		CustomerPrice price = getState().getCustomerPriceById(priceId);
 		if(price != null) {
 			getEntityManager().performEvent(
 					new CustomerPriceDeleteEvent(price));
 			return new FormValidationResolution(true, null, null);
 		}
 		else {
 			return new FormValidationResolution(false, null, null);
 		}
 	}
 	
 	@HandlesEvent("listfragment")
 	public Resolution listFragment() {
 		return new ForwardResolution("/views/event/customerPriceList.jsp");
 	}
 	
 	/**
 	 * Get the domestic customer price for Priority.Domestic_Standard
 	 * @return
 	 */
 	public DomesticCustomerPrice getDomesticStandardCustomerPrice() {
 		return this.getState().getDomesticCustomerPrice(Priority.Domestic_Standard);
 	}
 	
 	/**
 	 * Get the domestic customer price for Priority.Domestic_Air
 	 * @return
 	 */
 	public DomesticCustomerPrice getDomesticAirCustomerPrice() {
 		return this.getState().getDomesticCustomerPrice(Priority.Domestic_Air);
 	}
 
 	/**
 	 * @return the priceId
 	 */
 	public long getPriceId() {
 		return priceId;
 	}
 
 	/**
 	 * @param priceId the priceId to set
 	 */
 	public void setPriceId(long priceId) {
 		this.priceId = priceId;
 	}
 
 	/**
 	 * @return the priority
 	 */
 	public Priority getPriority() {
 		return priority;
 	}
 
 	/**
 	 * @param priority the priority to set
 	 */
 	public void setPriority(Priority priority) {
 		this.priority = priority;
 	}
 
 	/**
 	 * @return the weightPrice
 	 */
 	public float getWeightPrice() {
 		return weightPrice;
 	}
 
 	/**
 	 * @param weightPrice the weightPrice to set
 	 */
 	public void setWeightPrice(float weightPrice) {
 		this.weightPrice = weightPrice;
 	}
 
 	/**
 	 * @return the volumePrice
 	 */
 	public float getVolumePrice() {
 		return volumePrice;
 	}
 
 	/**
 	 * @param volumePrice the volumePrice to set
 	 */
 	public void setVolumePrice(float volumePrice) {
 		this.volumePrice = volumePrice;
 	}
 
 	/**
 	 * @return the direction
 	 */
 	public Direction getDirection() {
 		return direction;
 	}
 
 	/**
 	 * @param direction the direction to set
 	 */
 	public void setDirection(Direction direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * @return the location
 	 */
 	public String getLocation() {
 		return location;
 	}
 
 	/**
 	 * @param location the location to set
 	 */
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	/**
 	 * @return the domesticPriority
 	 */
 	public Priority getDomesticPriority() {
 		return domesticPriority;
 	}
 
 	/**
 	 * @param domesticPriority the domesticPriority to set
 	 */
 	public void setDomesticPriority(Priority domesticPriority) {
 		this.domesticPriority = domesticPriority;
 	}
 }
