 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
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
 package org.neo4j.data.generator.domains.medicalrecords.encounters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.LocalDate;
 import org.neo4j.data.generator.domains.medicalrecords.locations.HealthLocationPool;
 import org.neo4j.data.generator.domains.medicalrecords.professionals.HealthProfessionalPool;
 
 public class EncounterSequenceGenerator
 {
     public static final int MAX_YEARS_BETWEEN_ENCOUNTERS = 10;
     private EncounterGenerator encounterGenerator;
 
     public EncounterSequenceGenerator( HealthProfessionalPool professionalPool, HealthLocationPool locationPool )
     {
         encounterGenerator = new EncounterGenerator( professionalPool, locationPool );
     }
 
     public List<Encounter> encountersSince( LocalDate dateOfBirth )
     {
         ArrayList<Encounter> encounters = new ArrayList<Encounter>();
         LocalDate currentDate = dateOfBirth;
         LocalDate today = new LocalDate();
         while ( currentDate.isBefore( today ) )
         {
            encounters.add( encounterGenerator.nextEncounter( today ) );
             currentDate = currentDate.plusDays( (int) (Math.random() * 365 * MAX_YEARS_BETWEEN_ENCOUNTERS) );
         }
         return encounters;
     }
 }
