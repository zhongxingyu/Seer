 /*
  * @(#)VehiclItem.java
  *
  * Copyright 2010 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.mission.domain;
 
 import jvstm.cps.ConsistencyPredicate;
 import module.mission.domain.activity.ItemActivityInformation;
 import pt.ist.bennu.core.domain.exceptions.DomainException;
 import pt.ist.fenixWebFramework.services.Service;
 
 /**
  * 
  * @author Luis Cruz
  * 
  */
 public abstract class VehiclItem extends VehiclItem_Base {
 
     public VehiclItem() {
         super();
         new VehiclItemJustification(this);
     }
 
     @Override
     public void delete() {
         final VehiclItemJustification vehiclItemJustification = getVehiclItemJustification();
         if (vehiclItemJustification != null) {
             vehiclItemJustification.delete();
         }
        removeDriver();
         super.delete();
     }
 
     @Override
     public boolean isVehicleItem() {
         return true;
     }
 
     public boolean isAuthorized() {
         return getAuthorized() != null && getAuthorized();
     }
 
     @Service
     public void authorize() {
         setAuthorized(true);
     }
 
     @Service
     public void unauthorize() {
         setAuthorized(false);
     }
 
     @Override
     public void setInfo(final ItemActivityInformation itemActivityInformation) {
         final Mission mission = itemActivityInformation.getProcess().getMission();
         if (mission.getParticipantesCount() == 1) {
             super.setInfo(itemActivityInformation);
             setDriver(mission.getParticipantes().get(0));
         } else {
             if (itemActivityInformation.getDriver() == null) {
                 throw new DomainException("A vehicle item must have a driver");
             }
 
             super.setInfo(itemActivityInformation);
             setDriver(itemActivityInformation.getDriver());
             addPeople(itemActivityInformation.getDriver());
         }
     }
 
     @Override
     protected void setNewVersionInformation(final MissionItem missionItem) {
         super.setNewVersionInformation(missionItem);
         final VehiclItem vehiclItem = (VehiclItem) missionItem;
         vehiclItem.setVehiclItemJustification(getVehiclItemJustification());
     }
 
     @ConsistencyPredicate
     public boolean checkIsTemporaryXorHasDriver() {
         if (!isTemporary() && hasDriver()) {
             return true;
         }
         if (isTemporary() && !hasDriver()) {
             return true;
         }
         return false;
     }
 }
