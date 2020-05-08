 package org.consulo.google.protobuf.vfs;
 
 import org.consulo.google.protobuf.module.extension.GoogleProtobufModuleExtension;
 import org.consulo.vfs.backgroundTask.BackgroundTaskByVfsChangeProvider;
 import org.consulo.vfs.backgroundTask.BackgroundTaskByVfsParameters;
 import org.jetbrains.annotations.NotNull;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleUtilCore;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.openapi.vfs.VirtualFile;
 
 /**
  * @author VISTALL
  * @since 07.10.13.
  */
public class PbBackgroundTaskByVfsChangeProvider extends BackgroundTaskByVfsChangeProvider
 {
 	@NotNull
 	@Override
 	public String getName()
 	{
 		return "Google Protobuf";
 	}
 
 	@Override
 	public boolean validate(@NotNull Project project, @NotNull VirtualFile virtualFile)
 	{
 		Module moduleForFile = ModuleUtilCore.findModuleForFile(virtualFile, project);
 		if(moduleForFile == null)
 		{
 			return false;
 		}
 
 		return ModuleUtilCore.getExtension(moduleForFile, GoogleProtobufModuleExtension.class) != null;
 	}
 
 	@Override
 	public void setDefaultParameters(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull BackgroundTaskByVfsParameters backgroundTaskByVfsParameters)
 	{
 		Module moduleForFile = ModuleUtilCore.findModuleForFile(virtualFile, project);
 		assert moduleForFile != null;
 		GoogleProtobufModuleExtension extension = ModuleUtilCore.getExtension(moduleForFile, GoogleProtobufModuleExtension.class);
 		assert extension != null;
 
 		backgroundTaskByVfsParameters.setExePath(SystemInfo.isWindows ? "protoc.exe" : "protoc");
 		backgroundTaskByVfsParameters.setProgramParameters("--" + extension.getCompileParameter() + "=. $FileName$");
 		backgroundTaskByVfsParameters.setWorkingDirectory("$FileParentPath$");
 		backgroundTaskByVfsParameters.setOutPath("$FileParentPath$");
 	}
 }
