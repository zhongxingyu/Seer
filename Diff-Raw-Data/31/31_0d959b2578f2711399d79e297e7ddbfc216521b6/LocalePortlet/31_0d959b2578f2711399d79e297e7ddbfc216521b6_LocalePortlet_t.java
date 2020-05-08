 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.locale;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
 import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
 
 import javax.servlet.UnavailableException;
 import java.util.Locale;
import java.util.StringTokenizer;
 
 public class LocalePortlet extends ActionPortlet {
 
     public static final String VIEW_JSP = "/jsp/locale/view.jsp";
    public static final String DEFAULT_DISPLAY_LOCALES="en,cs,de,fr,hu,pl,it";
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         DEFAULT_VIEW_PAGE = "showLocale";
     }
 
 
     private ListBoxItemBean makeLocaleBean(String language, String name, Locale locale) {
         ListBoxItemBean bean = new ListBoxItemBean();
         String display = language;
         display = language.substring(0, 1).toUpperCase() + language.substring(1);
 
         bean.setValue(display);
         bean.setName(name);
 
         if (locale.getLanguage().equals(name)) {
             bean.setSelected(true);
         }
         return bean;
     }
 
     public void showLocale(FormEvent event) throws PortletException {
         PortletRequest request = event.getPortletRequest();
         Locale locale = request.getLocale();
         /*
         String locale = (String)request.getPortletSession(true).getAttribute(User.LOCALE);
         if (locale == null) {
             locale = Locale.ENGLISH.getLanguage();
             request.getPortletSession(true).setAttribute(User.LOCALE, locale);
         }
         Locale loc = new Locale(locale, "", "");
         */
         request.setAttribute("locale", locale);
 
         ListBoxBean localeSelector = event.getListBoxBean("localeLB");
         localeSelector.clear();
         localeSelector.setOnChange("GridSphere_SelectSubmit( this.form )");
         localeSelector.setSize(1);
 
        String displayLocales = getPortletSettings().getAttribute("display-locale");
        if (displayLocales == null) displayLocales = DEFAULT_DISPLAY_LOCALES;
        StringTokenizer localeTokenizer = new StringTokenizer(displayLocales,",");
        while (localeTokenizer.hasMoreTokens()) {
                String displayLocaleStr = localeTokenizer.nextToken();
                Locale displayLocale = new Locale(displayLocaleStr,"","");
                ListBoxItemBean localeBean = makeLocaleBean(displayLocale.getDisplayLanguage(displayLocale), displayLocaleStr, locale);
                localeSelector.addBean(localeBean);
        }
        
         setNextState(request, "locale/viewlocale.jsp");
     }
 
     public void selectLang(FormEvent event) throws PortletException {
         ListBoxBean localeSelector = event.getListBoxBean("localeLB");
         PortletSession session = event.getPortletRequest().getPortletSession(true);
         String loc = localeSelector.getSelectedValue();
         if (loc != null) {
             Locale locale = new Locale(loc, "", "");
             session.setAttribute(User.LOCALE, locale);
         }
     }
 
 }
