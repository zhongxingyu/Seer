 /* 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.dudie.onebusaway.model;
 
import java.util.Date;

 import com.google.gson.annotations.Expose;
 import com.google.gson.annotations.SerializedName;
 
 /**
  * @author orgoz
  */
 public class ScheduleStopTime implements Comparable<ScheduleStopTime> {
 
     /** Arrival time. */
     @SerializedName("arrival")
     private Time arrivalTime;
 
     /** Departure time. */
     @SerializedName("departure")
     private Time departureTime;
 
     /** Service id. */
     private String serviceId;
 
     /** Trip id. */
     private String tripId;
 
     /** Route id. */
     private String routeId;
 
     /** Route. */
     @Expose(serialize = false)
     private Route route;
 
     private String headsign;
 
     /**
      * Gets the arrivalTime.
      * 
      * @return the arrivalTime
      */
     public Time getArrivalTime() {
 
         return arrivalTime;
     }
 
     /**
      * Sets the arrivalTime.
      * 
      * @param arrivalTime
      *            the arrivalTime to set
      */
     public void setArrivalTime(final Time arrivalTime) {
 
         this.arrivalTime = arrivalTime;
     }
 
     /**
      * Gets the departureTime.
      * 
      * @return the departureTime
      */
     public Time getDepartureTime() {
 
         return departureTime;
     }
 
     /**
      * Sets the departureTime.
      * 
      * @param departureTime
      *            the departureTime to set
      */
     public void setDepartureTime(final Time departureTime) {
 
         this.departureTime = departureTime;
     }
 
     /**
      * Gets the serviceId.
      * 
      * @return the serviceId
      */
     public String getServiceId() {
 
         return serviceId;
     }
 
     /**
      * Sets the serviceId.
      * 
      * @param serviceId
      *            the serviceId to set
      */
     public void setServiceId(final String serviceId) {
 
         this.serviceId = serviceId;
     }
 
     /**
      * Gets the tripId.
      * 
      * @return the tripId
      */
     public String getTripId() {
 
         return tripId;
     }
 
     /**
      * Sets the tripId.
      * 
      * @param tripId
      *            the tripId to set
      */
     public void setTripId(final String tripId) {
 
         this.tripId = tripId;
     }
 
     /**
      * Gets the route.
      * 
      * @return the route
      */
     public Route getRoute() {
 
         return route;
     }
 
     /**
      * Sets the route.
      * 
      * @param route
      *            the route to set
      */
     public void setRoute(final Route route) {
 
         this.route = route;
     }
 
     /**
      * Gets the route id.
      * 
      * @return the route id
      */
     public String getRouteId() {
         return routeId;
     }
 
     /**
      * Sets the route id.
      * 
      * @param routeId
      *            the route id
      */
     public void setRouteId(String routeId) {
         this.routeId = routeId;
     }
 
     /**
      * Gets the headsign.
      * 
      * @return the headsign
      */
     public String getHeadsign() {
 
         return headsign;
     }
 
     /**
      * Gets the route headsign truncated to not display the route shortname.
      * 
      * @return the route headsign truncated to not display the route shortname
      */
     public final String getSimpleHeadsign() {
 
         return headsign.replaceFirst("^.* \\| ", "");
     }
 
     /**
      * Sets the headsign.
      * 
      * @param headsign
      *            the headsign to set
      */
     public void setHeadsign(final String headsign) {
 
         this.headsign = headsign;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     @Override
     public int compareTo(final ScheduleStopTime scheduleStopTime2) {
 
         return getDepartureTime().compareTo(scheduleStopTime2.getDepartureTime());
     }
 
     /*
      * (non-javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
 
         final StringBuilder builder = new StringBuilder();
         builder.append("ScheduleStopTime [arrivalTime=");
         builder.append(arrivalTime);
         builder.append(", departureTime=");
         builder.append(departureTime);
         builder.append(", serviceId=");
         builder.append(serviceId);
         builder.append(", tripId=");
         builder.append(tripId);
         builder.append(", routeId=");
         builder.append(route);
         builder.append("]");
         return builder.toString();
     }
 
 }
