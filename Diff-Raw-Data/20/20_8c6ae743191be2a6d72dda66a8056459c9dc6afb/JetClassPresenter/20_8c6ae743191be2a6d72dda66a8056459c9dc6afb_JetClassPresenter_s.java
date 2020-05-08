 /*
  * Copyright 2010-2012 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.idea.plugin.presentation;
 
 import javax.swing.Icon;
 
 import org.napile.compiler.lang.psi.NapileClass;
 import org.napile.compiler.lang.psi.NapilePsiUtil;
 import org.napile.compiler.lang.resolve.name.FqName;
 import org.napile.idea.plugin.JetIconProvider;
 import com.intellij.navigation.ColoredItemPresentation;
 import com.intellij.navigation.ItemPresentation;
 import com.intellij.navigation.ItemPresentationProvider;
 import com.intellij.openapi.editor.colors.TextAttributesKey;
 
 /**
  * @author Nikolay Krasko
  */
 public class JetClassPresenter implements ItemPresentationProvider<NapileClass>
 {
 
 	@Override
 	public ItemPresentation getPresentation(final NapileClass item)
 	{
 		return new ColoredItemPresentation()
 		{
 			@Override
 			public TextAttributesKey getTextAttributesKey()
 			{
 				return null;
 			}
 
 			@Override
 			public String getPresentableText()
 			{
 				return item.getName();
 			}
 
 			@Override
 			public String getLocationString()
 			{
 				FqName name = NapilePsiUtil.getFQName(item);
 				if(name != null)
 					return "(" + name.toString() + ")";
 
 				return "";
 			}
 
 			@Override
 			public Icon getIcon(boolean open)
 			{
				return JetIconProvider.INSTANCE.getIcon(item, 0);
 			}
 		};
 	}
 }
