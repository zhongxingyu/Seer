 /*
  * Copyright 2013 Consulo.org
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
 
 package org.consulo.lombok.module.extension;
 
 import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
 import org.consulo.module.extension.MutableModuleExtension;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.*;
 
 /**
  * @author VISTALL
  * @since 25.05.13
  */
 public class LombokMutableModuleExtension extends LombokModuleExtension implements MutableModuleExtension<LombokModuleExtension> {
   private LombokModuleExtension myModuleExtension;
 
   public LombokMutableModuleExtension(@NotNull String id, @NotNull Module module, @NotNull LombokModuleExtension moduleExtension) {
     super(id, module);
     myModuleExtension = moduleExtension;
     commit(moduleExtension);
   }
 
   @Nullable
   @Override
  public JComponent createConfigurablePanel(@NotNull ModifiableRootModel modifiableRootModel, @Nullable Runnable runnable) {
     return null;
   }
 
   @Override
   public void setEnabled(boolean b) {
     myIsEnabled = b;
   }
 
   @Override
   public boolean isModified() {
    return myIsEnabled != myModuleExtension.isEnabled();
   }
 
   @Override
   public void commit() {
     myModuleExtension.commit(this);
   }
 }
