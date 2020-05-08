 /*******************************************************************************
  * Copyright (c) 2008-2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.core.launcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.eef.codegen.components.PropertiesEditionComponent;
 import org.eclipse.emf.eef.codegen.components.SubPropertiesEditionComponent;
 import org.eclipse.emf.eef.codegen.parts.FormPropertiesEditionPart;
 import org.eclipse.emf.eef.codegen.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.codegen.parts.PropertiesEditionPart;
 import org.eclipse.emf.eef.codegen.plugin.GMF_Plugin_xml;
 import org.eclipse.emf.eef.codegen.properties.PropertySection;
 import org.eclipse.emf.eef.codegen.providers.ContextMessages;
 import org.eclipse.emf.eef.codegen.providers.ContextMessagesProperties;
import org.eclipse.emf.eef.codegen.providers.PackagePropertiesEditionPolicyProvider;
 import org.eclipse.emf.eef.codegen.providers.PackagePropertiesEditionProvider;
 import org.eclipse.emf.eef.codegen.providers.PropertiesEditionProvider;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class EEFLauncher {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			if (args.length != 3) {
 				System.out.println("Arguments not valid : {model, folder, basePackage}.");
 			} else {
 				URI modelURI = URI.createFileURI(args[0]);
 				File folder = new File(args[1]);
 				BasicMonitor monitor = new BasicMonitor();
 				List<Object> arguments = new ArrayList<Object>();
 				arguments.add(args[2]);
 				PropertiesEditionComponent propertiesEditionComponent = new PropertiesEditionComponent(
 						modelURI, folder, arguments);
 				propertiesEditionComponent.doGenerate(monitor);
 				SubPropertiesEditionComponent subPropertiesEditionComponent = new SubPropertiesEditionComponent(
 						modelURI, folder, arguments);
 				subPropertiesEditionComponent.doGenerate(monitor);
 				IPropertiesEditionPart ipropertiesEditionPart = new IPropertiesEditionPart(modelURI, folder,
 						arguments);
 				ipropertiesEditionPart.doGenerate(monitor);
 				PropertiesEditionPart propertiesEditionPart = new PropertiesEditionPart(modelURI, folder,
 						arguments);
 				propertiesEditionPart.doGenerate(monitor);
 				FormPropertiesEditionPart formPropertiesEditionPart = new FormPropertiesEditionPart(modelURI,
 						folder, arguments);
 				formPropertiesEditionPart.doGenerate(monitor);
 				ContextMessages contextMessages = new ContextMessages(modelURI, folder, arguments);
 				contextMessages.doGenerate(monitor);
 				ContextMessagesProperties contextMessagesProperties = new ContextMessagesProperties(modelURI,
 						folder, arguments);
 				contextMessagesProperties.doGenerate(monitor);
 				PackagePropertiesEditionProvider packagePropertiesEditionProvider = new PackagePropertiesEditionProvider(
 						modelURI, folder, arguments);
 				packagePropertiesEditionProvider.doGenerate(monitor);
				PackagePropertiesEditionPolicyProvider packagePropertiesEditionPolicyProvider = new PackagePropertiesEditionPolicyProvider(
						modelURI, folder, arguments);
				packagePropertiesEditionPolicyProvider.doGenerate(monitor);
 				PropertiesEditionProvider propertiesEditionProvider = new PropertiesEditionProvider(modelURI,
 						folder, arguments);
 				propertiesEditionProvider.doGenerate(monitor);
 				GMF_Plugin_xml plugin_xml = new GMF_Plugin_xml(modelURI, folder, arguments);
 				plugin_xml.doGenerate(monitor);
 				PropertySection propertiesSection = new PropertySection(modelURI, folder, arguments);
 				propertiesSection.doGenerate(monitor);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
