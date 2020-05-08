 package net.micwin.elysium.view.jumpGates;
 
 /*
  (c) 2012 micwin.net
 
  This file is part of open-space.
 
  open-space is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  open-space is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero Public License for more details.
 
  You should have received a copy of the GNU Affero Public License
  along with open-space.  If not, see http://www.gnu.org/licenses.
 
  Diese Datei ist Teil von open-space.
 
  open-space ist Freie Software: Sie können es unter den Bedingungen
  der GNU Affero Public License, wie von der Free Software Foundation,
  Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
  veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 
  open-space wird in der Hoffnung, dass es nützlich sein wird, aber
  OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
  Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
  Siehe die GNU Affero Public License für weitere Details.
 
  Sie sollten eine Kopie der GNU Affero Public License zusammen mit diesem
  Programm erhalten haben. Wenn nicht, siehe http://www.gnu.org/licenses. 
 
  */
 
 import net.micwin.elysium.dao.DaoManager;
 import net.micwin.elysium.entities.ElysiumEntity;
 import net.micwin.elysium.entities.NaniteGroup;
 import net.micwin.elysium.entities.SysParam;
 import net.micwin.elysium.entities.characters.User;
 import net.micwin.elysium.view.BasePage;
 import net.micwin.elysium.view.ElysiumWicketModel;
 import net.micwin.elysium.view.collective.NaniteGroupListPage;
 import net.micwin.elysium.view.errors.EntityNotAccessiblePage;
 import net.micwin.elysium.view.homepage.HomePage;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class UsePlanetaryGatePage extends BasePage {
 
 
 	@SuppressWarnings("rawtypes")
 	Form targetAdressForm = null;
 	TextField<String> targetAdressField = null;
 
 	public UsePlanetaryGatePage() {
 		super(true);
 		ensureStoryShown();
 		ensureAvatarPresent(true);
 		ensureSessionEntityPresent(NaniteGroup.class, "naniteGroup");
 	}
 
 	@Override
 	protected void onInitialize() {
 		super.onInitialize();
 
 		NaniteGroup naniteGroup = getElysiumSession().getNamedEntity("naniteGroup");
 		addToContentBody(getTargetGateAdressForm());
 		addToContentBody(getPublicGatesLink());
 	}
 
 	private Component getPublicGatesLink() {
 		SysParam publicGatesList = DaoManager.I.getSysParamDao().findByKey("publicGates", null) ; 
 		String list = null ; 
 		
 		if (publicGatesList == null) {
 			list = "" ; 
 		} else {
 			list = publicGatesList.getValue()+",elysium" ; 
 			
 		}
 	   Label label = new Label("publicGates", list) ;  
 		
 		return label;
 	}
 
 	private Form getTargetGateAdressForm() {
 		if (targetAdressForm == null) {
 
 			Form form = new Form("targetAdressForm") {
 				@Override
 				protected void onInitialize() {
 					super.onInitialize();
 					targetAdressField = new TextField("targetAdressField", Model.of(""));
 					add(targetAdressField);
 				}
 
 				@Override
 				protected void onSubmit() {
 					super.onSubmit();
 					NaniteGroup naniteGroup = getElysiumSession().getNamedEntity("naniteGroup");
 					String targetAdress = targetAdressField.getValue();
 
 					if (getNanitesBPO().gateTravel(naniteGroup, targetAdress)) {
 
 						getElysiumSession().setNamedEntity("nanitegroup", null);
 						setResponsePage(NaniteGroupListPage.class);
 						return;
 					}
 
 					error("cannot jump to adress '" + targetAdress + "'");
 				}
 			};
 
 			targetAdressForm = form;
 
 		}
 		return targetAdressForm;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public static boolean userCanShow(User user) {
 		return user != null;
 	}
 }
