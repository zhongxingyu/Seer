 package fr.mirumiru.utils;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.markup.head.CssReferenceHeaderItem;
 import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.CssResourceReference;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 
 import com.google.common.collect.Lists;
 
 import fr.mirumiru.MiruApplication;
 import fr.mirumiru.model.PageModel;
 import fr.mirumiru.pages.TemplatePage;
 
 public class WicketUtils {
 
 	public enum Language {
 		ENGLISH(Locale.ENGLISH, "flag-gb", "English"), FRENCH(Locale.FRENCH,
 				"flag-fr", "Fran\u00E7ais");
 
 		private Locale locale;
 		private String className;
 		private String displayName;
 
 		private Language(Locale locale, String className, String displayName) {
 			this.locale = locale;
 			this.className = className;
 			this.displayName = displayName;
 		}
 
 		public String getClassName() {
 			return className;
 		}
 
 		public Locale getLocale() {
 			return locale;
 		}
 
 		public String getDisplayName() {
 			return displayName;
 		}
 
 		public static boolean isLocaleSupported(Locale locale) {
			return locale == null ? false : isLanguageSupported(locale
 					.getLanguage());
 		}
 
 		public static boolean isLanguageSupported(String lang) {
 			boolean supported = false;
 			for (Language l : values()) {
 				if (StringUtils.equals(lang, l.getLocale().getLanguage())) {
 					supported = true;
 					break;
 				}
 			}
 			return supported;
 		}
 	}
 
 	private static List<PageModel> menuPages;
 
 	public static List<PageModel> getMenuPages() {
 		if (menuPages == null) {
 			List<PageModel> list = Lists.newArrayList();
 			List<Class<TemplatePage>> pageClasses = MiruApplication.get()
 					.getBeanClasses(TemplatePage.class, Mount.class);
 			for (Class<TemplatePage> klass : pageClasses) {
 				Mount mount = klass.getAnnotation(Mount.class);
 				String menu = mount.menu();
 				int menuOrder = mount.menuOrder();
 				if (StringUtils.isNotBlank(menu)) {
 					list.add(new PageModel(menu, menuOrder, klass));
 				}
 			}
 			Collections.sort(list, new Comparator<PageModel>() {
 				@Override
 				public int compare(PageModel o1, PageModel o2) {
 					return o1.getMenuOrder() - o2.getMenuOrder();
 				}
 			});
 			menuPages = list;
 		}
 		return menuPages;
 	}
 
 	public static PageParameters buildParams(String key, Object value) {
 		PageParameters params = new PageParameters();
 		if (value != null) {
 			params.add(key, value);
 		}
 		return params;
 	}
 
 	public static JavaScriptReferenceHeaderItem loadJS(Class<?> klass) {
 		return JavaScriptReferenceHeaderItem
 				.forReference(new JavaScriptResourceReference(klass, klass
 						.getSimpleName() + ".js"));
 	}
 
 	public static CssReferenceHeaderItem loadCSS(Class<?> klass) {
 		return CssReferenceHeaderItem.forReference(new CssResourceReference(
 				klass, klass.getSimpleName() + ".css"));
 	}
 }
