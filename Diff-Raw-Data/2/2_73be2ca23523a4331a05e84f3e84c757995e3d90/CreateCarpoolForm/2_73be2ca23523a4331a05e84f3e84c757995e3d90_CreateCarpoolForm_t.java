 package smartpool.web.form;
 
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import smartpool.common.Constants;
 import smartpool.domain.*;
 
 import java.util.ArrayList;
 
 public class CreateCarpoolForm {
     String from;
     String to;
     String capacity="0";
     String proposedStartDate;
     String pickupPoint;
     String pickupTime;
     String cabType;
     String officeArrivalTime;
     String officeDepartureTime;
     String routePoints;
 
     public CreateCarpoolForm() {
     }
 
     public CreateCarpoolForm(String from, String to, String proposedStartDate, String pickupPoint, String pickupTime, String cabType, String capacity, String officeArrivalTime, String officeDepartureTime, String routePoints) {
 
         this.from = from;
         this.to = to;
         this.capacity = capacity;
         this.proposedStartDate = proposedStartDate;
         this.pickupPoint = pickupPoint;
         this.pickupTime = pickupTime;
         this.cabType = cabType;
         this.officeArrivalTime = officeArrivalTime;
         this.officeDepartureTime = officeDepartureTime;
         this.routePoints = routePoints;
     }
 
     public Carpool getDomainObject(Buddy currentBuddy) {
         LocalDate proposedStartDate = Constants.DATE_FORMATTER.parseLocalDate(this.proposedStartDate);
         LocalTime officeArrivalTime = Constants.TIME_FORMATTER.parseLocalTime(this.officeArrivalTime);
         LocalTime officeDepartureTime = Constants.TIME_FORMATTER.parseLocalTime(this.officeDepartureTime);
         int capacity = Integer.parseInt(this.capacity);
         LocalTime pickupTime = Constants.TIME_FORMATTER.parseLocalTime(this.pickupTime);
         CabType cabType = CabType.valueOf(this.cabType);
 
         ArrayList<String> routePoints = new ArrayList<String>();
         for (String routePoint : this.routePoints.split(",")) {
             String trimmedRoutePoint = routePoint.trim();
            if (StringUtils.isNotBlank(trimmedRoutePoint) && !routePoints.contains(routePoint)) {
                 routePoints.add(trimmedRoutePoint);
             }
         }
 
         Carpool carpool = new Carpool(from + " - " + to, proposedStartDate, cabType, 0, officeArrivalTime, officeDepartureTime, Status.NOT_STARTED, null, capacity, routePoints);
 
         ArrayList<CarpoolBuddy> carpoolBuddies = new ArrayList<CarpoolBuddy>();
         carpoolBuddies.add(new CarpoolBuddy(currentBuddy, pickupPoint, pickupTime));
         carpool.setCarpoolBuddies(carpoolBuddies);
 
         return carpool;
     }
 
     public void setFrom(String from) {
         this.from = from;
     }
 
     public void setTo(String to) {
         this.to = to;
     }
 
     public void setPickupPoint(String pickupPoint) {
         this.pickupPoint = pickupPoint;
     }
 
     public void setPickupTime(String pickupTime) {
         this.pickupTime = pickupTime;
     }
 
     public void setCabType(String cabType) {
         this.cabType = cabType;
     }
 
     public void setOfficeArrivalTime(String officeArrivalTime) {
         this.officeArrivalTime = officeArrivalTime;
     }
 
     public void setOfficeDepartureTime(String officeDepartureTime) {
         this.officeDepartureTime = officeDepartureTime;
     }
 
     public void setRoutePoints(String routePoints) {
         this.routePoints = routePoints;
     }
 
     public void setProposedStartDate(String proposedStartDate) {
         this.proposedStartDate = proposedStartDate;
     }
 
     public void setCapacity(String capacity) {
         this.capacity = capacity;
     }
 
     public String getFrom() {
         return from;
     }
 
     public String getTo() {
         return to;
     }
 
     public String getCapacity() {
         return capacity;
     }
 
     public String getProposedStartDate() {
         return proposedStartDate;
     }
 
     public String getPickupPoint() {
         return pickupPoint;
     }
 
     public String getPickupTime() {
         return pickupTime;
     }
 
     public String getCabType() {
         return cabType;
     }
 
     public String getOfficeArrivalTime() {
         return officeArrivalTime;
     }
 
     public String getOfficeDepartureTime() {
         return officeDepartureTime;
     }
 
     public String getRoutePoints() {
         return routePoints;
     }
 }
