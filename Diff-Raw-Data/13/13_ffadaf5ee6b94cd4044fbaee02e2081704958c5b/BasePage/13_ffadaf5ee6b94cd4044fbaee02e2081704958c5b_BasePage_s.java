 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.interfaces.client.web.pages.shared;
 
 import org.apache.wicket.devutils.debugbar.DebugBar;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.wicketstuff.security.components.SecureWebPage;
 
 import fr.peralta.mycellar.interfaces.client.web.components.shared.login.LoginPanel;
 import fr.peralta.mycellar.interfaces.client.web.components.shared.nav.NavPanel;
 import fr.peralta.mycellar.interfaces.client.web.descriptors.DescriptorServiceFacade;
 import fr.peralta.mycellar.interfaces.client.web.resources.css.CssReferences;
 import fr.peralta.mycellar.interfaces.client.web.resources.js.JavaScriptReferences;
 
 /**
  * @author speralta
  */
 public abstract class BasePage extends SecureWebPage {
 
     private static final long serialVersionUID = 201117181723L;
 
     @SpringBean
     private DescriptorServiceFacade descriptorServiceFacade;
 
     /**
      * @param parameters
      */
     public BasePage(PageParameters parameters) {
         super(parameters);
         add(new BookmarkablePageLink<Void>("homeLink", WebApplication.get().getHomePage()));
         add(new NavPanel("menu", getMenuClass(), getSubMenuClass(),
                descriptorServiceFacade.getMenuPages()));
         add(new LoginPanel("login"));
         add(new DebugBar("debug"));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void renderHead(IHeaderResponse response) {
         response.renderCSSReference(CssReferences.getBootstrapCss());
         response.renderCSSReference(CssReferences.getBootstrapDatePickerCss());
         response.renderCSSReference(CssReferences.getMasterCss());
         response.renderCSSReference(CssReferences.getBootstrapResponsiveCss());
         response.renderCSSReference(CssReferences.getMasterResponsiveCss());
         response.renderJavaScriptReference(JavaScriptReferences.getJqueryJs());
         response.renderJavaScriptReference(JavaScriptReferences.getBootstrapJs());
         response.renderJavaScriptReference(JavaScriptReferences.getBootstrapDatePickerJs());
         response.renderJavaScriptReference(JavaScriptReferences.getMasterJs());
     }
 
     /**
      * @return the class of the menu page (for selecting it in {@link NavPanel}
      */
     protected abstract Class<? extends BasePage> getMenuClass();
 
     protected Class<? extends BasePage> getSubMenuClass() {
         return this.getClass();
     }
 
     /**
      * @return the descriptorServiceFacade
      */
     protected final DescriptorServiceFacade getDescriptorServiceFacade() {
         return descriptorServiceFacade;
     }
 
 }
