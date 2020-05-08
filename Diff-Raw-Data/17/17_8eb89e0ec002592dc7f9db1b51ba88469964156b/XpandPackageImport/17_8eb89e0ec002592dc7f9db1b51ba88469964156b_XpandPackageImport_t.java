/*******************************************************************************
 * Copyright (c) 12 jun. 2013 NetXForge.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 * 
 * Contributors: Christophe Bouhier - initial API and implementation and/or
 * initial documentation
 *******************************************************************************/ 
 package com.netxforge.netxstudio.models.export.impl;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.xpand2.XpandFacade;
 import org.eclipse.xpand2.output.Output;
 
 import com.netxforge.netxstudio.geo.GeoPackage;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.services.ServicesPackage;
 
 public class XpandPackageImport extends XpandTemplateImpl {
 
 	public static String XPAND_IMPORT = "templates::import::Root";
 	
 	private EPackage[] metaModels = new EPackage[]{EcorePackage.eINSTANCE, LibraryPackage.eINSTANCE, MetricsPackage.eINSTANCE,
 			OperatorsPackage.eINSTANCE, GeoPackage.eINSTANCE, ServicesPackage.eINSTANCE};
 	
 	public XpandPackageImport() {
 	}
 
 	public String getTemplateDescription() {
 		return "Export a template Model to Excel";
 	}
 
 	public String getTemplateCall() {
 		return XPAND_IMPORT;
 	}
 
 	public XpandEmfRegistryMetaModelsImpl getEmfMetaModels() {
 		XpandEmfRegistryMetaModelsImpl reg =  XpandCallerService.addPackages(metaModels);
 		return reg;
 	}
 
 	@Override
 	public void xpand(IResource res) {
 		assert target != null;
 		Output output = XpandCallerService.defineOutput(res, true);
 		XpandFacade facade = XpandCallerService.createXpandFacade(getEmfMetaModels(), getGlobalVarsMap(),  output);
 		XpandCallerService.evaluate(facade, getTemplateCall(), target);
 	}
 
 	public boolean isMetaTarget() {
 		return true;
 	}
 
 	public String getExtension() {
 		return "xls";
 	}
 	
 }
