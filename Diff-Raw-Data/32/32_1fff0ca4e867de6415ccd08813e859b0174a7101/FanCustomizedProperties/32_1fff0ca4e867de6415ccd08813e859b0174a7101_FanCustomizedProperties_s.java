 /*
  * Thibaut Colar Aug 13, 2009
  */
 package net.colar.netbeans.fan.project;
 
 import java.awt.Dialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import net.colar.netbeans.fan.wizard.FanProjectPropertiesPanel;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.project.ProjectUtils;
 import org.netbeans.spi.project.ui.CustomizerProvider;
 import org.netbeans.spi.project.ui.support.ProjectCustomizer;
 import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
 
 /**
  * Project/Pod propeties
  * @author thibautc
  */
 public class FanCustomizedProperties implements CustomizerProvider
 {
 	private ProjectCustomizer.CategoryComponentProvider panelProvider;
 	private final FanProject project;
 	private Category[] categories;
	Map panels = new HashMap();
 
 	public FanCustomizedProperties(FanProject project)
 	{
 		this.project = project;
 	}
 
 	public void init()
 	{
 		Category settings = Category.create(
 			"Pod Settings",
 			"Pod Settings",
 			null,
			null);
 		// ! panel order used in actionPerformed !
 		categories = new Category[]
 		{
 			settings,
 		};
 
 		FanProjectProperties props=project.getLookup().lookup(FanProjectProperties.class);
 		FanProjectPropertiesPanel settingsPanel=new FanProjectPropertiesPanel();
 		if(props!=null)
 		{
 			settingsPanel.setMainMethod(props.getMainMethod());
 			settingsPanel.setBuildTarget(props.getBuildTarget());
 		}
 		
 		panels.put(settings, settingsPanel);
 
 		panelProvider = new PanelProvider(panels);
 	}
 
 	public void showCustomizer()
 	{
 		init();
 
 		OptionListener listener = new OptionListener(project);
 		Dialog dialog = ProjectCustomizer.createCustomizerDialog(categories, panelProvider,
 			null, listener, null);
 		dialog.addWindowListener(listener);
 
 		dialog.setTitle(ProjectUtils.getInformation(project).getDisplayName());
 
 		dialog.setVisible(true);
 	}
 
 	private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider
 	{
 
 		private Map panels;
 		private JPanel EMPTY_PANEL = new JPanel();
 
 		public PanelProvider(Map panels)
 		{
 			this.panels = panels;
 		}
 
 		public JComponent create(ProjectCustomizer.Category category)
 		{
 			JComponent panel = (JComponent) panels.get(category);
 			return panel == null ? EMPTY_PANEL : panel;
 		}
 	}
 
 	private class OptionListener extends WindowAdapter implements ActionListener
 	{
 
 		private Project project;
 
 		OptionListener(Project project)
 		{
 			this.project = project;
 		}
 
 		public void actionPerformed(ActionEvent e)
 		{
 			if(e.getActionCommand().equals("OK"))
 			{
 				FanProjectProperties props=project.getLookup().lookup(FanProjectProperties.class);
 				if(props != null)
 				{
 					FanProjectPropertiesPanel settingsPanel=(FanProjectPropertiesPanel)panels.get(categories[0]);
 					props.setMainMethod(settingsPanel.getMainMethod());
 					props.setBuildTarget(settingsPanel.getBuildTarget());
 					props.save();
 				}
 			}
 		}
 
 		public void windowClosed(WindowEvent e)
 		{
 		}
 
 		public void windowClosing(WindowEvent e)
 		{
 		}
 	}
 }
