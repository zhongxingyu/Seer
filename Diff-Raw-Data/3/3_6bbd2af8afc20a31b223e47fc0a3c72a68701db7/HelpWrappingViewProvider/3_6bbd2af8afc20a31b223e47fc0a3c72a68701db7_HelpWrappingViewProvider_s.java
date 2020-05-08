 package au.org.scoutmaster.help;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import au.org.scoutmaster.application.ScoutmasterViewEnum;
 
 import com.google.gwt.thirdparty.guava.common.base.Preconditions;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewProvider;
 
 /**
  * View Provider, to replace the default one that Vaadin uses.
  * 
  * The list of views is automatically retrieved from the VaadinPageEnum enum
  * 
  * This one wraps the views in a HelpSplitPanel, so all our vaadin pages have a
  * help panel.
  * 
  * @author rsutton
  * 
  */
 public class HelpWrappingViewProvider implements ViewProvider
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1300354248455962125L;
 	private Map<String, Class<? extends View>> views = new HashMap<String, Class<? extends View>>();
 
 	private Map<String, Class<? extends View>> viewsWithoutHelp = new HashMap<String, Class<? extends View>>();
 
 	Logger logger = Logger.getLogger(HelpWrappingViewProvider.class);
 
 	/**
 	 * The list of views is automatically retrieved from the VaadinPageEnum enum
 	 */
 	public HelpWrappingViewProvider()
 	{
 		for (ScoutmasterViewEnum page : ScoutmasterViewEnum.values())
 		{
 			addView(page);
 		}
 
 	}
 
 	@Override
 	public String getViewName(String viewAndParameters)
 	{
 		logger.info(viewAndParameters);
 
 		if (viewAndParameters.contains("&"))
 			viewAndParameters = viewAndParameters.substring(0, viewAndParameters.indexOf("&"));
 
 		if (!views.containsKey(viewAndParameters)&& !viewsWithoutHelp.containsKey(viewAndParameters))
 		{
 			logger.error("Couldn't match view " + viewAndParameters);
 			return null;
 		}
 		return viewAndParameters;
 	}
 
 	@Override
 	public View getView(String viewName)
 	{
 		boolean noHelp = false;
 		View helpPanel = null;
 		try
 		{
 			Class<? extends View> viewClass = views.get(viewName);
 			if (viewClass == null)
 			{
 				noHelp = true;
 				viewClass = viewsWithoutHelp.get(viewName);
 			}
 			Preconditions.checkNotNull(viewClass, "Couldn't find the view " + viewName);
 
 			helpPanel = viewClass.newInstance();
 			if (!noHelp)
 			{
 				// wrap the view in a help panel
 				helpPanel = new HelpSplitPanel(helpPanel);
 			}
 
 		}
 		catch (InstantiationException e)
 		{
 			logger.error(e, e);
 		}
 		catch (IllegalAccessException e)
 		{
 			logger.error(e, e);
 		}
 		return helpPanel;
 	}
 
 	private void addView(ScoutmasterViewEnum view)
 	{
 		if (view.noHelp())
 		{
 			viewsWithoutHelp.put(view.getTitle(), view.getViewClass());
 		}
 		else
 		{
 			views.put(view.getTitle(), view.getViewClass());
 		}
 
 	}
 
 }
