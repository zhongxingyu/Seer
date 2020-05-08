 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2011 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok.pages.member;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.util.ListModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import wicket.contrib.tinymce.TinyMceBehavior;
 
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.beans.AchievementService;
 import com.tysanclan.site.projectewok.components.StoredImageResource;
 import com.tysanclan.site.projectewok.components.TysanTinyMCESettings;
 import com.tysanclan.site.projectewok.entities.Achievement;
 import com.tysanclan.site.projectewok.entities.AchievementRequest;
 import com.tysanclan.site.projectewok.util.ImageUtil;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public class RequestAchievementPage2 extends AbstractSingleAccordionMemberPage {
 
 	private static final long serialVersionUID = 1L;
 
 	private IModel<Achievement> achievementModel;
 
 	public RequestAchievementPage2(Achievement achievement) {
 		super("Request achievement " + achievement.getName());
 
 		achievementModel = ModelMaker.wrap(achievement);
 
 		final FileUploadField uploadField = new FileUploadField("screenshot",
 				new ListModel<FileUpload>());
 		final TextArea<String> evidenceField = new TextArea<String>("evidence",
 				new Model<String>());
 		evidenceField.add(new TinyMceBehavior(new TysanTinyMCESettings()));
 
 		Form<Achievement> form = new Form<Achievement>("evidenceForm") {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private AchievementService service;
 
 			@Override
 			protected void onSubmit() {
 				FileUpload upload = uploadField.getFileUpload();
 				String evidence = evidenceField.getModelObject();
 
 				if (upload != null) {
 					if (!ImageUtil.isGIFImage(upload.getBytes())
 							&& !ImageUtil.isPNGImage(upload.getBytes())
 							&& !ImageUtil.isJPEGImage(upload.getBytes())) {
 						error("Please supply a valid JPG, GIF or PNG image");
 						return;
 					}
 				} else {
 					if (evidence == null || evidence.isEmpty()) {
 						error("Please provide either a screenshot or a textual description of why you qualify for this achievement");
 						return;
 					}
 				}
 
 				AchievementRequest request = service.requestAchievement(
 						getUser(), achievementModel.getObject(),
 						upload != null ? upload.getBytes() : null, evidence);
 				if (request != null) {
 					setResponsePage(new RequestAchievementPage());
 				} else {
 					error("Failed to create request");
 				}
 
 			}
 		};
 
 		form.setMultiPart(true);
 
 		form.add(uploadField);
 		form.add(evidenceField);
 
 		getAccordion().add(form);
 
 		form.add(new Label("name", achievement.getName()));
 
		form.add(new Image("icon", new StoredImageResource(achievementModel
				.getObject().getIcon().getImage())));
		form.add(new Image("game", new StoredImageResource(achievementModel
				.getObject().getGame().getImage())).setVisible(achievement
				.getGame() != null));
 
 		boolean hasGroup = achievement.getGroup() != null;
 
 		form.add(new Label("group", hasGroup ? achievement.getGroup().getName()
 				: "-").setVisible(hasGroup));
 		form.add(new Label("description", achievement.getDescription())
 				.setEscapeModelStrings(false));
 	}
 
 	@Override
 	protected void onDetach() {
 		super.onDetach();
 
 		achievementModel.detach();
 	}
 }
