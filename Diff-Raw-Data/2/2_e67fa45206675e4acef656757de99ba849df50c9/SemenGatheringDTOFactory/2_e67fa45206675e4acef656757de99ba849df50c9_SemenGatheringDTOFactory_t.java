 /* Ara - capture species and specimen data
  *
  * Copyright (C) 2009  INBio (Instituto Nacional de Biodiversidad)
  *
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
 
 package org.inbio.ara.dto.germplasm;
 
 import org.inbio.ara.dto.BaseEntityOrDTOFactory;
 import org.inbio.ara.persistence.germplasm.SemenGathering;
 
 /**
  *
  * @author dasolano
  */
 public class SemenGatheringDTOFactory  extends BaseEntityOrDTOFactory<SemenGathering ,SemenGatheringDTO>{
 
     @Override
     public SemenGathering getEntityWithPlainValues(SemenGatheringDTO dto) {
         SemenGathering e = new SemenGathering();
 
         e.setSementalId(dto.getSementalId());
         e.setActiveDoses(dto.getActiveDoses());
         e.setCanisterNumber(dto.getCanisterNumber());
         e.setConcentration(dto.getConcentration());
         e.setSemenConsistencyId(dto.getSemenConsistencyId());
         e.setDilution(dto.getDilution());
         e.setGobletNumber(dto.getGobletNumber());
         e.setMassMotility(dto.getMassMotility());
         e.setMotility(dto.getMotility());
         e.setPh(dto.getPh());
         e.setPostThawMotility(dto.getPostThawMotility());
         e.setSemenColor(dto.getSemenColor());
         e.setSemenGatheringDate(dto.getSemenGatheringDate());
         e.setSemenGatheringTime(dto.getSemenGatheringTime());
         e.setSemenGatheringMethodId(dto.getSemenGatheringMethodId());
         e.setSolventId(dto.getSolventId());
         e.setStrawColor(dto.getStrawColor());
         e.setStrawQuantity(dto.getStrawQuantity());
        e.setCurrentStrawQuantity(dto.getStrawQuantity());
         e.setStrawSize(dto.getStrawSize());
         e.setTankNumber(dto.getTankNumber());
         e.setVolume(dto.getVolume());
         e.setTotalSpermConcentration(dto.getTotalSpermConcentration());
         e.setSpermConcentrationPerStraw(dto.getSpermConcentrationPerStraw());
 
 
         return e;
     }
 
     @Override
     public SemenGathering updateEntityWithPlainValues(SemenGatheringDTO dto, SemenGathering e) {
 
         e.setActiveDoses(dto.getActiveDoses());
         e.setCanisterNumber(dto.getCanisterNumber());
         e.setConcentration(dto.getConcentration());
         e.setSemenConsistencyId(dto.getSemenConsistencyId());
         e.setDilution(dto.getDilution());
         e.setGobletNumber(dto.getGobletNumber());
         e.setMassMotility(dto.getMassMotility());
         e.setMotility(dto.getMotility());
         e.setPh(dto.getPh());
         e.setPostThawMotility(dto.getPostThawMotility());
         e.setSemenColor(dto.getSemenColor());
         e.setSemenGatheringDate(dto.getSemenGatheringDate());
         e.setSemenGatheringTime(dto.getSemenGatheringTime());
         e.setSemenGatheringMethodId(dto.getSemenGatheringMethodId());
         e.setSolventId(dto.getSolventId());
         e.setStrawSize(dto.getStrawSize());
         e.setStrawColor(dto.getStrawColor());
         e.setStrawQuantity(dto.getStrawQuantity());
         e.setCurrentStrawQuantity(dto.getCurrentStrawQuantity());
         e.setTankNumber(dto.getTankNumber());
         e.setVolume(dto.getVolume());
         e.setTotalSpermConcentration(dto.getTotalSpermConcentration());
         e.setSpermConcentrationPerStraw(dto.getSpermConcentrationPerStraw());
 
         return e;
     }
 
     public SemenGatheringDTO createDTO(SemenGathering entity) {
         SemenGatheringDTO dto = new SemenGatheringDTO();
 
         dto.setSemenGatheringId(entity.getSemenGatheringId());
         dto.setSementalId(entity.getSementalId());
         dto.setActiveDoses(entity.getActiveDoses());
         dto.setCanisterNumber(entity.getCanisterNumber());
         dto.setConcentration(entity.getConcentration());
         dto.setSemenConsistencyId(entity.getSemenConsistencyId());
         dto.setDilution(entity.getDilution());
         dto.setGobletNumber(entity.getGobletNumber());
         dto.setMassMotility(entity.getMassMotility());
         dto.setMotility(entity.getMotility());
         dto.setPh(entity.getPh());
         dto.setPostThawMotility(entity.getPostThawMotility());
         dto.setSemenColor(entity.getSemenColor());
         dto.setSemenGatheringDate(entity.getSemenGatheringDate());
         dto.setSemenGatheringTime(entity.getSemenGatheringTime());
         dto.setSemenGatheringMethodId(entity.getSemenGatheringMethodId());
         dto.setStrawSize(entity.getStrawSize());
         dto.setSolventId(entity.getSolventId());
         dto.setStrawColor(entity.getStrawColor());
         dto.setStrawQuantity(entity.getStrawQuantity());
         dto.setCurrentStrawQuantity(entity.getCurrentStrawQuantity());
         dto.setTankNumber(entity.getTankNumber());
         dto.setVolume(entity.getVolume());
         dto.setTotalSpermConcentration(entity.getTotalSpermConcentration());
         dto.setSpermConcentrationPerStraw(entity.getSpermConcentrationPerStraw());
 
 
         return dto;
     }
 
 }
