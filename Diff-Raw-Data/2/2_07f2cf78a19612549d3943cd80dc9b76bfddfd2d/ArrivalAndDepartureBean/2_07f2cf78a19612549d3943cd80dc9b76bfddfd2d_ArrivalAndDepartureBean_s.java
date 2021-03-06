 /*
  * Copyright 2008 Brian Ferris
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.onebusaway.transit_data.model;
 
 import org.onebusaway.transit_data.model.trips.TripBean;
 
 public class ArrivalAndDepartureBean extends ApplicationBean {
 
   private static final long serialVersionUID = 2L;
 
   private TripBean trip;
 
   private long serviceDate;
 
   private String stopId;
 
   private long predictedArrivalTime;
 
   private long scheduledArrivalTime;
 
   private long predictedDepartureTime;
 
   private long scheduledDepartureTime;
 
   private boolean predicted = false;
 
   private Long lastUpdateTime;
 
   private String status;
 
   private double distanceFromStop = Double.NaN;
 
   public TripBean getTrip() {
     return trip;
   }
 
   public void setTrip(TripBean trip) {
     this.trip = trip;
   }
 
   public long getServiceDate() {
     return serviceDate;
   }
 
   public void setServiceDate(long serviceDate) {
     this.serviceDate = serviceDate;
   }
 
   public String getStopId() {
     return stopId;
   }
 
   public void setStopId(String stopId) {
     this.stopId = stopId;
   }
 
   public long getPredictedArrivalTime() {
     return predictedArrivalTime;
   }
 
   public void setPredictedArrivalTime(long predictedArrivalTime) {
     this.predictedArrivalTime = predictedArrivalTime;
   }
 
   public long getScheduledArrivalTime() {
     return scheduledArrivalTime;
   }
 
   public void setScheduledArrivalTime(long scheduledArrivalTime) {
     this.scheduledArrivalTime = scheduledArrivalTime;
   }
 
   public long getPredictedDepartureTime() {
     return predictedDepartureTime;
   }
 
   public void setPredictedDepartureTime(long predictedDepartureTime) {
     this.predictedDepartureTime = predictedDepartureTime;
   }
 
   public long getScheduledDepartureTime() {
     return scheduledDepartureTime;
   }
 
   public void setScheduledDepartureTime(long scheduledDepartureTime) {
     this.scheduledDepartureTime = scheduledDepartureTime;
   }
 
   public boolean isPredicted() {
     return predicted;
   }
 
   public void setPredicted(boolean predicted) {
     this.predicted = predicted;
   }
 
   public Long getLastUpdateTime() {
     return lastUpdateTime;
   }
 
   public void setLastUpdateTime(Long lastUpdateTime) {
     this.lastUpdateTime = lastUpdateTime;
   }
 
   public String getStatus() {
     return status;
   }
 
   public void setStatus(String status) {
     this.status = status;
   }
 
   public boolean isDistanceFromStopSet() {
     return !Double.isNaN(distanceFromStop);
   }
 
   public double getDistanceFromStop() {
     return distanceFromStop;
   }
 
   public void setDistanceFromStop(double distanceFromStop) {
     this.distanceFromStop = distanceFromStop;
   }
 
   public boolean hasPredictedArrivalTime() {
     return this.predictedArrivalTime > 0;
   }
 
   public boolean hasPredictedDepartureTime() {
    return this.predictedArrivalTime > 0;
   }
 
   public long computeBestArrivalTime() {
     return hasPredictedArrivalTime() ? getPredictedArrivalTime()
         : getScheduledArrivalTime();
   }
 
   public long computeBestDepartureTime() {
     return hasPredictedDepartureTime() ? getPredictedDepartureTime()
         : getScheduledDepartureTime();
   }
 
   @Override
   public String toString() {
     return "route=" + trip.getRoute() + " scheduled=" + scheduledArrivalTime
         + " predicted=" + predictedArrivalTime;
   }
 }
