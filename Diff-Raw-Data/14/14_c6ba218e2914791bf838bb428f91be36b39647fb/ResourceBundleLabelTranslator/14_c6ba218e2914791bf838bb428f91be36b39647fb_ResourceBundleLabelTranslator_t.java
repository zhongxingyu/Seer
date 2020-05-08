 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.i18n;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.PropertyResourceBundle;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.swtxml.resources.IDocumentResource;
 import com.swtxml.util.context.Context;
import com.swtxml.util.lang.ContractProof;
 import com.swtxml.util.lang.FilenameUtils;
 
 public class ResourceBundleLabelTranslator implements ILabelTranslator {
 
 	private Locale locale;
 
 	public ResourceBundleLabelTranslator(Locale locale) {
 		super();
 		this.locale = locale;
 	}
 
 	public String translate(String key) {
		String value = translateFromResourceBundles(key);
 		if (value == null) {
 			return "??? " + key + " ???";
 		}
 		return value;
 	}
 
	private String translateFromResourceBundles(String key) {
 		IDocumentResource document = Context.adaptTo(IDocumentResource.class);
		ContractProof.notNull(document, "document");
 
 		List<String> names = getResourceBundleNames(FilenameUtils.getBaseName(document
 				.getDocumentName()));
 		names.addAll(getResourceBundleNames("messages"));
 		names.addAll(getResourceBundleNames("bundle:messages"));
 		names.addAll(getResourceBundleNames("bundle:plugin"));
 
 		for (String name : names) {
 			InputStream resource = document.resolve(name + ".properties");
 			if (resource != null) {
 				try {
 					PropertyResourceBundle resourceBundle = new PropertyResourceBundle(resource);
 					String value = resourceBundle.getString(key);
 					if (value != null) {
 						return value;
 					}
 				} catch (MissingResourceException e) {
 					// ignore missing resources
 				} catch (IOException e) {
 					// ignore invalid resource bundles
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private List<String> getResourceBundleNames(String baseName) {
 		List<String> results = new ArrayList<String>(4);
 		if (StringUtils.isNotEmpty(locale.getLanguage())) {
 			if (StringUtils.isNotEmpty(locale.getCountry())) {
 				if (StringUtils.isNotEmpty(locale.getVariant())) {
 					results.add(baseName + "_" + locale.getLanguage() + "_" + locale.getCountry()
 							+ "_" + locale.getVariant());
 				}
 				results.add(baseName + "_" + locale.getLanguage() + "_" + locale.getCountry());
 			}
 			results.add(baseName + "_" + locale.getLanguage());
 		}
 		results.add(baseName);
 
 		return results;
 	}
 }
