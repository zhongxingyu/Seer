 /*
  * Copyright 2009-2011 Carsten Hufe devproof.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License")
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.devproof.portal.module.otherpage.page;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.devproof.portal.core.config.ModulePage;
 import org.devproof.portal.core.config.Secured;
 import org.devproof.portal.core.module.common.CommonConstants;
 import org.devproof.portal.core.module.common.panel.AuthorPanel;
 import org.devproof.portal.module.otherpage.OtherPageConstants;
 import org.devproof.portal.module.otherpage.entity.OtherPage;
 import org.devproof.portal.module.otherpage.service.OtherPageService;
 
 /**
  * @author Carsten Hufe
  */
 @Secured(OtherPageConstants.AUTHOR_RIGHT)
 @ModulePage(mountPath = "/admin/others", registerGlobalAdminLink = true)
 public class OtherPagePage extends OtherPageBasePage {
 
     private static final long serialVersionUID = 1L;
     @SpringBean(name = "otherPageService")
     private OtherPageService otherPageService;
     @SpringBean(name = "otherPageDataProvider")
     private ISortableDataProvider<OtherPage> otherPageDataProvider;
 
     public OtherPagePage(PageParameters params) {
         super(params);
         add(createIdOrderHeader());
         add(createModifiedByOrderHeader());
         add(createRepeatingOtherPages());
     }
 
     private OrderByBorder createIdOrderHeader() {
        return new OrderByBorder("table_id", "subject", otherPageDataProvider);
     }
 
     private OrderByBorder createModifiedByOrderHeader() {
         return new OrderByBorder("table_modified_by", "modifiedBy", otherPageDataProvider);
     }
 
     private OtherPageDataView createRepeatingOtherPages() {
         return new OtherPageDataView("repeatingOtherPages");
     }
 
     private class OtherPageDataView extends DataView<OtherPage> {
         private static final long serialVersionUID = 1L;
 
         public OtherPageDataView(String id) {
             super(id, otherPageDataProvider);
         }
 
         @Override
         protected void populateItem(Item<OtherPage> item) {
             IModel<OtherPage> otherPageModel = item.getModel();
             item.add(createIdLabel(otherPageModel));
             item.add(createTeaserLabel(otherPageModel));
             item.add(createModifiedByLabel(otherPageModel));
             item.add(createViewLink(otherPageModel));
             item.add(createAuthorPanel(item));
             item.add(createEvenOddModifier(item));
             item.setOutputMarkupId(true);
         }
 
         private Label createTeaserLabel(final IModel<OtherPage> otherPageModel) {
             return new Label("teaser", new AbstractReadOnlyModel<String>() {
                 private static final long serialVersionUID = -7421302589639388804L;
 
                 @Override
                 public String getObject() {
                     String content = otherPageModel.getObject().getContent();
                     if(StringUtils.isNotBlank(content)) {
                         return StringUtils.abbreviate(content.replaceAll("\\<.*?>",""), 80) + " ...";
                     }
                     return "";
                 }
             });
         }
 
         private BookmarkablePageLink<OtherPageViewPage> createViewLink(IModel<OtherPage> otherPageModel) {
             OtherPage otherPage = otherPageModel.getObject();
             BookmarkablePageLink<OtherPageViewPage> viewLink = new BookmarkablePageLink<OtherPageViewPage>("viewLink", OtherPageViewPage.class);
             viewLink.add(createViewLinkImage());
             viewLink.setParameter("0", otherPage.getId());
             return viewLink;
         }
 
         private Image createViewLinkImage() {
             Image viewImage = new Image("viewImage", CommonConstants.REF_VIEW_IMG);
             viewImage.setEscapeModelStrings(false);
             return viewImage;
         }
 
         private AttributeModifier createEvenOddModifier(final Item<OtherPage> item) {
             return new AttributeModifier("class", true, new AbstractReadOnlyModel<String>() {
                 private static final long serialVersionUID = 1L;
 
                 @Override
                 public String getObject() {
                     return (item.getIndex() % 2 != 0) ? "even" : "odd";
                 }
             });
         }
 
         private AuthorPanel<OtherPage> createAuthorPanel(final Item<OtherPage> item) {
             final IModel<OtherPage> otherPageModel = item.getModel();
             return new AuthorPanel<OtherPage>("authorButtons", otherPageModel) {
                 private static final long serialVersionUID = 1L;
 
                 @Override
                 public void onDelete(AjaxRequestTarget target) {
                     otherPageService.delete(otherPageModel.getObject());
                     item.setVisible(false);
                     target.addComponent(item);
                     target.addComponent(getFeedback());
                     info(OtherPagePage.this.getString("msg.deleted"));
                 }
 
                 @Override
                 public void onEdit(AjaxRequestTarget target) {
                     setResponsePage(new OtherPageEditPage(otherPageModel));
                 }
 
                 @Override
                 protected MarkupContainer newHistorizationLink(String markupId) {
                     return new BookmarkablePageLink<OtherPageHistoryPage>(markupId, OtherPageHistoryPage.class) {
                         private static final long serialVersionUID = 1918205848493398092L;
 
                         @Override
                         public PageParameters getPageParameters() {
                             PageParameters params = new PageParameters();
                             params.put("id", otherPageModel.getObject().getId());
                             return params;
                         }
                     };
                 }
             };
         }
 
         private Label createModifiedByLabel(IModel<OtherPage> otherPageModel) {
             IModel<String> modifiedByModel = new PropertyModel<String>(otherPageModel, "modifiedBy");
             return new Label("modifiedBy", modifiedByModel);
         }
 
         private Label createIdLabel(IModel<OtherPage> otherPageModel) {
             IModel<String> idModel = new PropertyModel<String>(otherPageModel, "id");
             return new Label("id", idModel);
         }
     }
 }
