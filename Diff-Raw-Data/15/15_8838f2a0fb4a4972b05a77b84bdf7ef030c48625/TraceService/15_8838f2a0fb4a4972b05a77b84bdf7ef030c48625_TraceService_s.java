 /*
  *  OpenSDI Manager
  *  Copyright (C) 2013 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.opensdi.destination.service;
 
 import it.geosolutions.opensdi.destination.dto.TraceDto;
 import it.geosolutions.opensdi.dto.GeobatchRunInfo;
 
 import java.util.List;
 
 /**
  * Trace Service interface for destination project
  * 
  * @author adiaz
  */
 public interface TraceService {
 
 /**
  * Remove all run information for file contained in run information
  * 
  * @param runInfo
  */
 public void cleanByRun(GeobatchRunInfo runInfo);
 
 /**
  * Search closest trace element for a run information. This method uses dates
  * include in run information and trace elements to obtain the closest one
  * 
  * @param runInfo
  * @return trace element closest to a run information
  */
 public TraceDto searchClosestTraceByRun(GeobatchRunInfo runInfo);
 
 /**
  * Search trace elements for a run information.
  * 
  * @param runInfo
  * @return trace elements for a run information
  */
 public List<TraceDto> searchByRun(GeobatchRunInfo runInfo);
 
 }
