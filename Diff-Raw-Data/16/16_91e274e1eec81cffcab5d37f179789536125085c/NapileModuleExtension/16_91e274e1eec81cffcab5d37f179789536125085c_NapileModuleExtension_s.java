 package org.napile.idea.plugin.module.extension;
 
 import org.consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
 import org.jetbrains.annotations.NotNull;
 import org.mustbe.consulo.roots.ContentFoldersSupport;
 import org.mustbe.consulo.roots.impl.ProductionContentFolderTypeProvider;
 import org.mustbe.consulo.roots.impl.ProductionResourceContentFolderTypeProvider;
 import org.mustbe.consulo.roots.impl.TestContentFolderTypeProvider;
 import org.mustbe.consulo.roots.impl.TestResourceContentFolderTypeProvider;
 import org.napile.idea.plugin.sdk.NapileSdkType;
 import com.intellij.openapi.projectRoots.SdkType;
 import com.intellij.openapi.roots.ModifiableRootModel;
 
 /**
  * @author VISTALL
  * @since 12:24/27.05.13
  */
 @ContentFoldersSupport(value = {
 		ProductionContentFolderTypeProvider.class,
 		ProductionResourceContentFolderTypeProvider.class,
 		TestContentFolderTypeProvider.class,
 		TestResourceContentFolderTypeProvider.class
 })
 public class NapileModuleExtension extends ModuleExtensionWithSdkImpl<NapileModuleExtension>
 {
 	public NapileModuleExtension(@NotNull String id, @NotNull ModifiableRootModel module)
 	{
 		super(id, module);
 	}
 
 	@Override
	protected Class<? extends SdkType> getSdkTypeClass()
 	{
 		return NapileSdkType.class;
 	}
 }
