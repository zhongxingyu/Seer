 /*
  * @(#)GeographicConstants.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Geography Module for the MyOrg web application.
  *
  *   The Geography Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Geography Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Geography Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.geography.domain;
 
 /**
  * Acronyms, PartyTypes and Accountability names used on the Organization
  * Structure.
  * 
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt)
  */
 public interface GeographicConstants {
     public static final String GEOGRAPHIC_ACCOUNTABILITY_TYPE_NAME = "Geographic";
 
    public static final String PORTUGAL_UNIT_ACRONYM = "PT";
     public static final String EARTH_UNIT_ACRONYM = "EARTH";
     public static final String MILKY_WAY_UNIT_ACRONYM = "MILKYWAY";
     public static final String MULTIVERSE_UNIT_ACRONYM = "MULTIVERSEZERO";
 
     public static final String COUNTRY_SUBDIVISION_PARTYTYPE_NAME = "Country Subdivision";
     public static final String COUNTRY_PARTYTYPE_NAME = "Country";
     public static final String PLANET_PARTYTYPE_NAME = "Planet";
     public static final String GALAXY_PARTYTYPE_NAME = "Galaxy";
     public static final String UNIVERSE_PARTYTYPE_NAME = "Universe";
 }
