 package com.esn.idea.liquibaseejb;
 
 import com.esn.idea.liquibaseejb.facet.LiquibaseFacet;
 import com.esn.idea.liquibaseejb.model.database.DatabaseColumnModel;
 import com.esn.idea.liquibaseejb.model.database.DatabaseModel;
 import com.esn.idea.liquibaseejb.model.ejb.module.ModuleModel;
 import com.esn.idea.liquibaseejb.model.liquibase.LiquibaseModel;
 import com.esn.idea.liquibaseejb.model.liquibase.xml.ChangeSet;
 import com.esn.idea.liquibaseejb.model.liquibase.xml.DatabaseChangeLog;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleComponent;
 import com.intellij.openapi.module.ModuleUtil;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.xml.XmlFile;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Author: Marcus Nilsson
  * Date: 2008-okt-14
  * Time: 08:33:15
  */
 public class LiquibaseModuleComponent implements ModuleComponent
 {
 	private Module module;
 	private LiquibaseModel liquibaseModel;
     private AtomicLong nextId = new AtomicLong(System.currentTimeMillis() / 1000);
 
     public void updateLiquibaseToolWindow(DatabaseModel databaseModel)
 	{
         getProjectComponent().showLiquibaseDatabaseModel(databaseModel);
 	}
 
     private LiquibaseProjectComponent getProjectComponent()
     {
         return module.getProject().getComponent(LiquibaseProjectComponent.class);
     }
 
     public DatabaseModel findDatabaseModel()
 	{
 		LiquibaseModel model = findLiquibaseModel();
 
 		if (model != null)
 		{
 			DatabaseModel databaseModel = model.getDatabaseModel();
 			if (databaseModel != null)
 			{
 				// TODO: Listen to notifications instead
 				updateLiquibaseToolWindow(databaseModel);
 			}
 			return databaseModel;
 		}
         else
         {
             updateLiquibaseToolWindow(null);
         }
 
         return null;
 	}
 
 	public LiquibaseModel findLiquibaseModel()
 	{
 		if (liquibaseModel != null && !liquibaseModel.isValid())
 		{
             refreshLiquibaseModel();
         }
 
 		if (liquibaseModel == null)
 		{
             LiquibaseFacet facet = LiquibaseFacet.getInstance(module);
 
             if (facet != null)
             {
                 VirtualFile liquibaseFile = facet.getLiquibaseFile();
 
                 if (liquibaseFile == null) return null;
 
                 Project project = module.getProject();
                 PsiManager psiManager = PsiManager.getInstance(project);
 
                 PsiFile psiFile = psiManager.findFile(liquibaseFile);
                 if (!(psiFile instanceof XmlFile)) return null;
 
                 liquibaseModel = new LiquibaseModel((XmlFile) psiFile);
             }
         }
 
 		return liquibaseModel;
 	}
 
     public void refreshLiquibaseModel()
     {
         liquibaseModel = null;
         findDatabaseModel();
     }
 
     public LiquibaseModuleComponent(Module module)
 	{
 		this.module = module;
 	}
 
 	public void initComponent()
 	{
 		// TODO: insert component initialization logic here
 	}
 
 	public void disposeComponent()
 	{
 		// TODO: insert component disposal logic here
 	}
 
 	@NotNull
 	public String getComponentName()
 	{
 		return "LiquibaseModuleComponent";
 	}
 
 	public void projectOpened()
 	{
        updateEjbModel();
 	}
 
 	public void projectClosed()
 	{
 		// called when project is being closed
 	}
 
 	public void moduleAdded()
 	{
 		// Invoked when the module corresponding to this component instance has been completely
 		// loaded and added to the project.
 	}
 
 	@Nullable
 	public static LiquibaseModuleComponent getInstance(PsiElement psiElement)
 	{
         Module moduleForFile = ModuleUtil.findModuleForPsiElement(psiElement);
 
         return getInstance(moduleForFile);
 	}
 
     public static LiquibaseModuleComponent getInstance(Module module)
     {
         if (module == null) return null;
 
         return module.getComponent(LiquibaseModuleComponent.class);
     }
 
     public Module getModule()
 	{
 		return module;
 	}
 
 	public boolean isLiquibaseEnabled()
 	{
 		return findDatabaseModel() != null;
 	}
 
 	@Nullable
 	public DatabaseColumnModel getColumnModel(String tableName, String columnName)
 	{
 		DatabaseModel databaseModel = findDatabaseModel();
 
 		if (databaseModel == null) return null;
 
 		return databaseModel.getColumnModel(tableName, columnName);
 	}
 
 	public ChangeSet newChangeSet()
 	{
         DatabaseChangeLog changeLog = findLiquibaseModel().findDatabaseChangeLog();
         if (changeLog == null)
         {
             return null;
         }
         ChangeSet changeSet = changeLog.addChangeSet();
 		changeSet.getId().setStringValue(String.valueOf(nextId.getAndIncrement()));
 		String userName = System.getProperty("user.name");
 		changeSet.getAuthor().setStringValue(userName);
 
         liquibaseModel = null;
 
         return changeSet;
 	}
 
 	public boolean isTableMissing(String tableName)
 	{
 		return !findDatabaseModel().existsTable(tableName);
 	}
 
     public void updateEjbModel()
     {
         ModuleModel moduleModel = new ModuleModel(module);
 
         DatabaseModel databaseModel = moduleModel.databaseForAllContexts();
 
         getProjectComponent().showEjbDatabaseModel(databaseModel);
     }
 }
