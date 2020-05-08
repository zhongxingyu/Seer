 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.core.mixin.IMixinElement;
 import org.eclipse.dltk.core.mixin.MixinModel;
import org.eclipse.dltk.ruby.ast.RubyAliasExpression;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 
 public class RubyMixinModel {
 	
 	private static RubyMixinModel instance;
 
 	public static RubyMixinModel getInstance () {
 		if (instance == null)
 			instance = new RubyMixinModel ();
 		return instance;
 	}
 	
 	public static MixinModel getRawInstance () {
 		return getInstance().getRawModel();
 	}
 
 	private final MixinModel model;
 
 	private RubyMixinModel() {
 		model = new MixinModel(RubyLanguageToolkit.getDefault());
 	}
 
 	public MixinModel getRawModel() {
 		return model;
 	}
 
 	public RubyMixinClass createRubyClass(RubyClassType type) {
 		return (RubyMixinClass) createRubyElement(type.getModelKey());
 	}
 
 	public IRubyMixinElement createRubyElement(String key) {
 		if (key.equals("Object")) {
 			return new RubyObjectMixinClass(this, true);
 		} else if (key.equals("Object%")) {
 			return new RubyObjectMixinClass(this, false);
 		}
 		IMixinElement mixinElement = model.get(key);
 		if (mixinElement != null) {
 			return createRubyElement(mixinElement);
 		}
 		return null;
 	}
 
 	public IRubyMixinElement createRubyElement(IMixinElement element) {
 		Assert.isLegal(element != null);
 		if (element.getKey().equals("Object")) {
 			return new RubyObjectMixinClass(this, true);
 		} else if (element.getKey().equals("Object%")) {
 			return new RubyObjectMixinClass(this, false);
 		}		
 		Object[] objects = element.getAllObjects();
 		if (objects == null)
 			return null;
 		for (int i = 0; i < objects.length; i++) {
 			RubyMixinElementInfo obj = (RubyMixinElementInfo) objects[i];
 			if (obj == null || obj.getObject() == null)
 				continue;
 			switch (obj.getKind()) {
 				case RubyMixinElementInfo.K_CLASS:
 				case RubyMixinElementInfo.K_VIRTUAL:
 					return new RubyMixinClass(this, element.getKey(), false);
 				case RubyMixinElementInfo.K_MODULE:
 					return new RubyMixinClass(this, element.getKey(), true);
 				case RubyMixinElementInfo.K_METHOD:					
 					return new RubyMixinMethod(this, element.getKey());
 				case RubyMixinElementInfo.K_ALIAS:					
 					return new RubyMixinAlias(this, element.getKey());
 				case RubyMixinElementInfo.K_VARIABLE:
 					return new RubyMixinVariable(this, element.getKey());
 			}						
 		}
 		return null;
 	}
 }
