 /*
  * @(#)PortugueseMunicipalitiesImport.java
  *
  * Copyright 2010 Instituto Superior Tecnico
  * Founding Authors: Pedro Santos
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Geography Module.
  *
  *   The Geography Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
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
 package module.geography.domain.task;
 
 import module.geography.domain.Country;
 import myorg.util.BundleUtil;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 /**
  * 
  * @author Pedro Santos
  * @author João Antunes
  * 
  */
public class PortugueseMunicipalitiesImport extends PortugueseMunicipalitiesImport_Base {
 
     private static final String CTT_MUNICIPALITIESFILE = "/concelhos.txt";
 
     Country portugal;
 
     protected int additions = 0;
 
     protected int deletions = 0;
 
     protected int modifications = 0;
 
     protected int touches = 0;
 
     private final MultiLanguageString municipalityLevelName = new MultiLanguageString().with(Language.pt, "Concelho").with(
 	    Language.en, "Municipality");
 
     public PortugueseMunicipalitiesImport() {
 	super();
     }
 
     @Override
     public String getLocalizedName() {
 	return BundleUtil.getStringFromResourceBundle("resources/GeographyResources",
 		"label.task.ctt.portugal.municipalities.import");
     }
 
     protected void auxLogInfo(String message) {
 	logInfo(message);
     }
 
     @Override
     @Service
     public void executeTask() {
 	// let's initialize the auxiliary class due to the the nasty injector
 	// errors
 	PortugueseMunicipalitiesImportAuxiliaryServices aux = PortugueseMunicipalitiesImportAuxiliaryServices.getInstance();
 	aux.executeTask(this);
     }
 
 }
