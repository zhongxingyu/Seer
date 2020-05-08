 package org.napile.idea.plugin.psi.impl;
 
 import org.consulo.module.extension.ModuleExtension;
 import org.consulo.psi.PsiPackage;
 import org.consulo.psi.PsiPackageManager;
 import org.consulo.psi.PsiPackageSupportProvider;
 import org.jetbrains.annotations.NotNull;
 import org.napile.idea.plugin.module.extension.NapileModuleExtension;
 import com.intellij.psi.PsiManager;
 
 /**
  * @author VISTALL
  * @since 12:37/27.05.13
  */
 public class NapilePackageSupportProvider implements PsiPackageSupportProvider
 {
	@NotNull
 	@Override
	public Class<? extends ModuleExtension> getSupportedModuleExtensionClass()
 	{
		return NapileModuleExtension.class;
 	}
 
 	@NotNull
 	@Override
 	public PsiPackage createPackage(@NotNull PsiManager psiManager, @NotNull PsiPackageManager packageManager, @NotNull Class<? extends ModuleExtension> aClass, @NotNull String s)
 	{
 		return new NapilePackage(psiManager, packageManager, aClass, s);
 	}
 }
