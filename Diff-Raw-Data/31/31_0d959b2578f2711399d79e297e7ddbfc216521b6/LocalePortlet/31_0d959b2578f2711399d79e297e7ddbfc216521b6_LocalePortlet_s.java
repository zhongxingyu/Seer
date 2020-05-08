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
 
 public class LocalePortlet extends ActionPortlet {
 
     public static final String VIEW_JSP = "/jsp/locale/view.jsp";
 
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
 

        ListBoxItemBean bean_en = makeLocaleBean(Locale.US.getDisplayLanguage(new Locale("en","","")), "en", locale);
        ListBoxItemBean bean_cz = makeLocaleBean(new Locale("cs","","").getDisplayLanguage(new Locale("cs","","")), "cs", locale);
        ListBoxItemBean bean_ge = makeLocaleBean(Locale.GERMAN.getDisplayLanguage(new Locale("de","","")), "de", locale);
        ListBoxItemBean bean_fr = makeLocaleBean(Locale.FRENCH.getDisplayLanguage(new Locale("fr","","")), "fr", locale);
        ListBoxItemBean bean_hu = makeLocaleBean(new Locale("hu","","").getDisplayLanguage(new Locale("hu","","")), "hu", locale);
        ListBoxItemBean bean_pl = makeLocaleBean(new Locale("pl","","").getDisplayLanguage(new Locale("pl","","")), "pl", locale);
        ListBoxItemBean bean_it = makeLocaleBean(Locale.ITALIAN.getDisplayLanguage(new Locale("it","","")), "it", locale);

        localeSelector.addBean(bean_en);
        localeSelector.addBean(bean_cz);
        localeSelector.addBean(bean_ge);
        localeSelector.addBean(bean_fr);
        localeSelector.addBean(bean_hu);
        localeSelector.addBean(bean_pl);
        localeSelector.addBean(bean_it);

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
