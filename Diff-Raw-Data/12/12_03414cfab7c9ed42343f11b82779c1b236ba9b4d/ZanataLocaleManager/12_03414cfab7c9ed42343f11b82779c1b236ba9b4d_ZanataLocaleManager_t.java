 package com.redhat.topicindex.zanata;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.zanata.common.LocaleId;
 
 import com.redhat.ecs.constants.CommonConstants;
 
 /**
  * The LocaleManager handles what locales per project should be used to check against the zanata REST interface . All functions lock the list of locales to
  * ensure thread safety since the locales maybe accessed from multiple concurrent threads.
  */
 public class ZanataLocaleManager
 {
 
 	private List<LocaleId> locales = new ArrayList<LocaleId>()
 	{
 		private static final long serialVersionUID = 5161718393080351107L;
 
 		{
 			for (final String locale : CommonConstants.LOCALES)
 			{
 				add(LocaleId.fromJavaName(locale));
 			}
 		}
 	};
 
 	private static final Map<String, ZanataLocaleManager> projectToLocales = new HashMap<String, ZanataLocaleManager>();
 
 	public static ZanataLocaleManager getInstance(final String project)
 	{
 		if (!projectToLocales.containsKey(project))
 			projectToLocales.put(project, new ZanataLocaleManager());
 		return projectToLocales.get(project);
 	}
 
 	private ZanataLocaleManager()
 	{
 
 	}
 
 	public List<LocaleId> getLocales()
 	{
 		synchronized (locales)
 		{
 			/*
 			 * return a read only copy of the list of locales as it stands now. we can't return a reference to the list, because it is possible that the
 			 * returned list will be looped over outside of a synchonization block, and edited at the same time.
 			 */
 			return Collections.unmodifiableList(new ArrayList<LocaleId>(locales));
 		}
 	}
 
 	public void setLocales(final List<LocaleId> locales)
 	{
 		synchronized (this.locales)
 		{
 			this.locales = locales;
 		}
 	}
 
 	public void removeLocale(final LocaleId locale)
 	{
		System.out.println("Removing " + locale + " from further sync requests.");
 		synchronized (locales)
 		{
 			if (locales.contains(locale))
 				locales.remove(locale);
 		}
 	}
 }
