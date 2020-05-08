 package org.napile.playJava4idea.facet;
 
 import javax.swing.JComponent;
 
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.Nullable;
 import org.napile.playJava4idea.PlayJavaConstants;
 import org.napile.playJava4idea.PlayJavaUtil;
 import com.intellij.facet.ui.FacetConfigurationQuickFix;
 import com.intellij.facet.ui.FacetEditorContext;
 import com.intellij.facet.ui.FacetEditorTab;
 import com.intellij.facet.ui.FacetEditorValidator;
 import com.intellij.facet.ui.FacetValidatorsManager;
 import com.intellij.facet.ui.ValidationResult;
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.roots.ModifiableRootModel;
 import com.intellij.openapi.roots.OrderRootType;
 import com.intellij.openapi.roots.libraries.Library;
 import com.intellij.openapi.roots.libraries.LibraryTable;
 import com.intellij.openapi.util.text.StringUtil;
 
 /**
  * @author VISTALL
  * @since 15:34/17.03.13
  */
 public class PlayJavaFacetEditorTab extends FacetEditorTab
 {
 	private final PlayJavaFacet facet;
 	private PlayJavaFacetConfiguration configuration;
 	private PlayJavaFacetEditorPanel panel;
 
 	public PlayJavaFacetEditorTab(final FacetEditorContext facetEditorContext, final FacetValidatorsManager validatorsManager)
 	{
 		this.facet = (PlayJavaFacet) facetEditorContext.getFacet();
 		configuration = facet.getConfiguration();
 
 		validatorsManager.registerValidator(new FacetEditorValidator()
 		{
 			@Override
 			public ValidationResult check()
 			{
 				if(configuration.resolvePlayPath() == null)
 				{
 					return new ValidationResult("No playframework");
 				}
 
 				if(PlayJavaUtil.findClass(facet.getModule().getProject(), facet.getModule(), PlayJavaConstants.PLAY_PLAY) == null)
 				{
 					final String playName = facet.getConfiguration().getPlayName();
 					if(playName == null)
 					{
 						return new ValidationResult("No playframework");
 					}
 
 					return new ValidationResult("Play libraries is not in class path", new FacetConfigurationQuickFix()
 					{
 						@Override
 						public void run(JComponent place)
 						{
 							final ModifiableRootModel modifiableModel = facetEditorContext.getModifiableRootModel();
 
 							final LibraryTable moduleLibraryTable = modifiableModel.getModuleLibraryTable();
 							final LibraryTable.ModifiableModel libraryTableModifiableModel = moduleLibraryTable.getModifiableModel();
 
 							final Library library = libraryTableModifiableModel.createLibrary(playName);
 
 							final Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
							libraryModifiableModel.addRoot("jar://" + configuration.playPath + "/framework/" + playName + ".jar!/", OrderRootType.CLASSES);
							libraryModifiableModel.addJarDirectory("file://" + configuration.playPath + "/framework/src", true, OrderRootType.SOURCES);
							libraryModifiableModel.addJarDirectory("file://" + configuration.playPath + "/framework/lib", true, OrderRootType.CLASSES);
 
 							libraryModifiableModel.commit();
 							libraryTableModifiableModel.commit();
 						}
 					});
 				}
 
 				return ValidationResult.OK;
 			}
 		}, panel);
 	}
 
 	@Nls
 	@Override
 	public String getDisplayName()
 	{
 		return null;
 	}
 
 	@Nullable
 	@Override
 	public JComponent createComponent()
 	{
 		panel = new PlayJavaFacetEditorPanel(facet, configuration);
 
 		reset();
 
 		return panel;
 	}
 
 	@Override
 	public boolean isModified()
 	{
 		if(!StringUtil.equals(configuration.playPath, panel.getPlayPath()))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void apply() throws ConfigurationException
 	{
 		configuration.playPath = panel.getPlayPath();
 	}
 
 	@Override
 	public void reset()
 	{
 		panel.setPlayPath(configuration.playPath);
 	}
 
 	@Override
 	public void disposeUIResources()
 	{
 	}
 }
