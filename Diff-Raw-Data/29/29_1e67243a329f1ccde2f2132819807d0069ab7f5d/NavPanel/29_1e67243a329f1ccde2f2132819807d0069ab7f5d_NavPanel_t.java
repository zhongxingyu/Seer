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
 package fr.peralta.mycellar.interfaces.client.web.components.shared.nav;
 
 import java.util.List;
 
import org.apache.commons.lang3.StringUtils;
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.AbstractLink;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.StringResourceModel;
 import org.wicketstuff.security.checks.LinkSecurityCheck;
 import org.wicketstuff.security.components.SecureComponentHelper;
 
 import fr.peralta.mycellar.interfaces.client.web.pages.shared.BasePage;
 
 /**
  * @author speralta
  */
 public class NavPanel extends Panel {
 
     private static final long serialVersionUID = 201011122248L;
 
     /**
      * @param id
      * @param current
      * @param descriptors
      */
     public NavPanel(String id, final Class<? extends BasePage> current,
             List<NavDescriptor> descriptors) {
         this(id, current, descriptors, false);
     }
 
     /**
      * @param id
      * @param current
      * @param descriptors
      * @param horizontal
      */
     public NavPanel(String id, final Class<? extends BasePage> current,
             List<NavDescriptor> descriptors, final boolean horizontal) {
         super(id);
         add(new ListView<NavDescriptor>("navEntry", descriptors) {
             private static final long serialVersionUID = 201011122248L;
 
             @Override
             protected void populateItem(final ListItem<NavDescriptor> listItem) {
                 final AbstractLink link;
                 final Component subNav;
                 final Label header;
                String icon = null;
                 if (listItem.getModelObject() instanceof NavHeaderDescriptor) {
                     NavHeaderDescriptor descriptor = (NavHeaderDescriptor) listItem
                             .getModelObject();
                     link = new ExternalLink("link", "#");
                     if (horizontal) {
                         subNav = new SubNavPanel("subNav", current, descriptor.getPages());
                         header = new Label("header");
                         header.setVisibilityAllowed(false);
                         link.add(
                                 new AttributeAppender("class", "dropdown-toggle").setSeparator(" "))
                                 .add(new AttributeAppender("data-toggle", "dropdown")
                                         .setSeparator(" "));
                         link.add(new Label("label", new StringResourceModel(descriptor
                                .getHeaderKey(), this, null)).setOutputMarkupPlaceholderTag(false));

                        try {
                            icon = new StringResourceModel(descriptor.getHeaderKey() + "Icon",
                                    this, null).getString();
                        } catch (Exception e) {
                            icon = null;
                        }
 
                         // add check on first page in list
                         SecureComponentHelper.setSecurityCheck(link, new LinkSecurityCheck(link,
                                 descriptor.getPages().get(0).getPageClass()));
 
                         for (NavPageDescriptor page : descriptor.getPages()) {
                             if (current.equals(page.getPageClass())) {
                                 listItem.add(new AttributeAppender("class", "active")
                                         .setSeparator(" "));
                             }
                         }
 
                         listItem.add(new AttributeAppender("class", "dropdown").setSeparator(" "));
                     } else {
                         link.setVisibilityAllowed(false);
                         subNav = new WebMarkupContainer("subNav");
                         subNav.setVisibilityAllowed(false);
                         header = new Label("header", new StringResourceModel(
                                 descriptor.getHeaderKey(), this, null));
                         listItem.add(new AttributeAppender("class", "nav-header"));
                     }
                 } else if (listItem.getModelObject() instanceof NavPageDescriptor) {
                     NavPageDescriptor descriptor = (NavPageDescriptor) listItem.getModelObject();
                     link = new BookmarkablePageLink<Void>("link", descriptor.getPageClass());
                     link.add(new Label("label", new StringResourceModel(descriptor
                             .getPageTitleKey(), this, null)));
                    try {
                        icon = new StringResourceModel(descriptor.getPageTitleKey() + "Icon", this,
                                null).getString();
                    } catch (Exception e) {
                        icon = null;
                    }
                     subNav = new WebMarkupContainer("subNav");
                     subNav.setVisibilityAllowed(false);
                     header = new Label("header");
                     header.setVisibilityAllowed(false);
                     if (current.equals(descriptor.getPageClass())) {
                         listItem.add(new AttributeAppender("class", "active").setSeparator(" "));
                     }
                     SecureComponentHelper.setSecurityCheck(listItem, new LinkSecurityCheck(link,
                             descriptor.getPageClass()));
                 } else {
                     throw new IllegalStateException("Unknown nav descriptor "
                             + listItem.getModelObject().getClass() + ".");
                 }
                 Label caret = new Label("caret", "");
                 caret.setVisibilityAllowed(subNav.isVisibilityAllowed());
                 link.add(caret);
                if (StringUtils.isNotEmpty(icon)) {
                    link.add(new WebComponent("icon").add(new AttributeAppender("class", "icon-"
                            + icon).setSeparator(" ")));
                } else {
                    link.add(new WebComponent("icon").setVisibilityAllowed(false));
                }
                 listItem.add(link);
                 listItem.add(subNav);
                 listItem.add(header);
             }
         });
     }
 }
