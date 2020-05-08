 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.client.views;
 
 import org.accesointeligente.client.presenters.UserProfileEditPresenter;
 import org.accesointeligente.client.presenters.UserProfileEditPresenterIface;
 import org.accesointeligente.model.*;
 import org.accesointeligente.shared.Country;
 import org.accesointeligente.shared.Gender;
 import org.accesointeligente.shared.NotificationEventType;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class UserProfileEditView extends Composite implements UserProfileEditPresenter.Display {
 	private static UserProfileEditViewUiBinder uiBinder = GWT.create(UserProfileEditViewUiBinder.class);
 	interface UserProfileEditViewUiBinder extends UiBinder<Widget, UserProfileEditView> {}
 
 	@UiField Label email;
 	@UiField HTMLPanel personPanel;
 	@UiField TextBox personFirstName;
 	@UiField TextBox personLastName;
 	@UiField RadioButton personGenderFemale;
 	@UiField RadioButton personGenderMale;
 	@UiField ListBox personActivity;
 	@UiField ListBox personAge;
 	@UiField HTMLPanel institutionPanel;
 	@UiField TextBox institutionName;
 	@UiField ListBox institutionType;
 	@UiField FlowPanel institutionActivities;
 	@UiField RadioButton countryChile;
 	@UiField RadioButton countryOther;
 	@UiField ListBox region;
 	@UiField PasswordTextBox oldPassword;
 	@UiField PasswordTextBox password1;
 	@UiField PasswordTextBox password2;
 	@UiField Button saveChanges;
 
 	UserProfileEditPresenterIface presenter;
 
 	public UserProfileEditView() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	public void setPresenter(UserProfileEditPresenterIface presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public void cleanPersonActivities() {
 		personActivity.clear();
 		personActivity.addItem("Selecciona tu actividad");
 	}
 
 	@Override
 	public void cleanInstitutionActivities() {
 		institutionActivities.clear();
 	}
 
 	@Override
 	public void cleanInstitutionTypes() {
 		institutionType.clear();
 		institutionType.addItem("Selecciona tu tipo");
 	}
 
 	@Override
 	public void cleanPersonAges() {
 		personAge.clear();
 		personAge.addItem("Selecciona tu edad");
 	}
 
 	@Override
 	public void cleanRegions() {
 		region.clear();
 		region.addItem("Selecciona tu region");
 	}
 
 	@Override
 	public void addPersonActivity(Activity activity) {
 		personActivity.addItem(activity.getName(), activity.getId().toString());
 	}
 
 	@Override
 	public void updatePersonActivity(Activity selectedActivity, List<Activity> activities) {
 		for(Activity activity : activities){
 			personActivity.addItem(activity.getName(), activity.getId().toString());
 		}
 		personActivity.setSelectedIndex(activities.indexOf(selectedActivity) + 1);
 	}
 
 	@Override
 	public void addInstitutionActivity(Activity activity, Boolean checked) {
 		CheckBox cb = new CheckBox();
 		cb.setFormValue(activity.getId().toString());
 		cb.setText(activity.getName());
 		if (checked) {
 			cb.setValue(true);
 		}
 		institutionActivities.add(cb);
 	}
 
 	@Override
 	public void addInstitutionType(InstitutionType institutionType) {
 		this.institutionType.addItem(institutionType.getName(), institutionType.getId().toString());
 	}
 
 	@Override
 	public void addPersonAge(Age age) {
 		personAge.addItem(age.getName(), age.getId().toString());
 	}
 
 	@Override
 	public void addRegion(Region region) {
 		this.region.addItem(region.getName(), region.getId().toString());
 	}
 
 	@Override
	public void updateRegion(Region selectedRegion, List<Region> regions) {
		for (Region region : regions) {
			this.region.addItem(region.getName(), region.getId().toString());
		}
		this.region.setSelectedIndex(regions.indexOf(selectedRegion) + 1);
	}

	@Override
 	public String getPersonFirstName() {
 		return personFirstName.getText();
 	}
 
 	@Override
 	public void setPersonFirstName(String name) {
 		personFirstName.setText(name);
 	}
 
 	@Override
 	public String getPersonLastName() {
 		return personLastName.getText();
 	}
 
 	@Override
 	public void setPersonLastName(String lastname) {
 		personLastName.setText(lastname);
 	}
 
 	@Override
 	public Gender getPersonGender() {
 		if (personGenderFemale.getValue()) {
 			return Gender.FEMALE;
 		} else if (personGenderMale.getValue()) {
 			return Gender.MALE;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void setPersonGender(Gender gender) {
 		if (gender.toString().equals(Gender.MALE.toString())) {
 			personGenderMale.setValue(true);
 			personGenderFemale.setValue(false);
 		} else {
 			personGenderMale.setValue(false);
 			personGenderFemale.setValue(true);
 		}
 	}
 
 	@Override
 	public Activity getPersonActivity() {
 		Activity activity = new Activity();
 		activity.setId(Integer.parseInt(personActivity.getValue(personActivity.getSelectedIndex())));
 		activity.setName(personActivity.getItemText(personActivity.getSelectedIndex()));
 		return activity;
 	}
 
 	@Override
 	public void setPersonActivity(Activity activity) {
 		personActivity.setSelectedIndex(activity.getId());
 	}
 
 	@Override
 	public Age getPersonAge() {
 		Age age = new Age();
 		age.setId(Integer.parseInt(personAge.getValue(personAge.getSelectedIndex())));
 		age.setName(personAge.getItemText(personAge.getSelectedIndex()));
 		return age;
 	}
 
 	@Override
 	public void setPersonAge(Age age) {
 		personAge.setSelectedIndex(age.getId());
 	}
 
 	@Override
 	public String getInstitutionName() {
 		return institutionName.getText();
 	}
 
 	@Override
 	public void setInstitutionName(String instName) {
 		institutionName.setText(instName);
 	}
 
 	@Override
 	public InstitutionType getInstitutionType() {
 		InstitutionType type = new InstitutionType();
 		type.setId(Integer.parseInt(institutionType.getValue(institutionType.getSelectedIndex())));
 		type.setName(institutionType.getItemText(institutionType.getSelectedIndex()));
 		return type;
 	}
 
 	@Override
 	public void setInstitutionType(InstitutionType type) {
 		institutionType.setSelectedIndex(type.getId());
 	}
 
 	@Override
 	public Set<Activity> getInstitutionActivities() {
 		Set<Activity> activities = new HashSet<Activity>();
 
 		for (int i = 0; i < institutionActivities.getWidgetCount(); i++) {
 			Widget widget = institutionActivities.getWidget(i);
 
 			if (widget instanceof CheckBox) {
 				CheckBox cb = (CheckBox) widget;
 
 				if (cb.getValue()) {
 					Activity activity = new Activity();
 					activity.setId(Integer.parseInt(cb.getFormValue()));
 					activity.setName(cb.getText());
 					activities.add(activity);
 				}
 			}
 		}
 
 		return activities;
 	}
 
 	@Override
 	public void setInstitutionActivities(List<Activity> activities) {
 		for (int i = 0; i < institutionActivities.getWidgetCount(); i++) {
 			Widget widget = institutionActivities.getWidget(i);
 
 			if (widget instanceof CheckBox) {
 				CheckBox cb = (CheckBox) widget;
 
 				if (cb.getValue()) {
 					Activity activity = new Activity();
 					activity.setId(Integer.parseInt(cb.getFormValue()));
 					activity.setName(cb.getText());
 					if (activities.contains(activity)) {
 						cb.setValue(true);
 					} else {
 						cb.setValue(false);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public Country getCountry() {
 		if (countryChile.getValue()) {
 			return Country.CHILE;
 		} else {
 			return Country.OTHER;
 		}
 	}
 
 	@Override
 	public void setCountry(Country country) {
 		if (Country.CHILE.toString().equals(country.toString())) {
 			countryChile.setValue(true);
 			countryOther.setValue(false);
 		} else {
 			countryChile.setValue(false);
 			countryOther.setValue(true);
 		}
 	}
 
 	@Override
 	public Region getRegion() {
 		Region reg = new Region();
 		reg.setId(Integer.parseInt(region.getValue(region.getSelectedIndex())));
 		reg.setNumber(region.getSelectedIndex() + 1);
 		reg.setName(region.getItemText(region.getSelectedIndex()));
 		return reg;
 	}
 
 	@Override
 	public void setRegion(Region reg) {
 		region.setSelectedIndex(reg.getId());
 	}
 
 	@Override
 	public void setEmail(String emailAddress) {
 		email.setText(emailAddress);
 	}
 
 	@Override
 	public String getPassword() {
 		return password1.getText();
 	}
 
 	@Override
 	public String getOldPassword() {
 		return oldPassword.getText();
 	}
 
 	@Override
 	public void setInstitutionPanelVisibility(Boolean visible) {
 		institutionPanel.setVisible(visible);
 	}
 
 	@Override
 	public void setPersonPanelVisibility(Boolean visible) {
 		personPanel.setVisible(visible);
 	}
 
 	@Override
 	public Boolean validateForm() {
 		if (presenter == null) {
 			return false;
 		}
 
 		presenter.setUpdatePassword(false);
 		if (presenter.getUser().getNaturalPerson()) {
 			if (!personFirstName.getText().matches(".*\\w+.*")) {
 				presenter.showNotification("Debe ingresar un nombre", NotificationEventType.ERROR);
 				return false;
 			} else if (!personLastName.getText().matches(".*\\w+.*")) {
 				presenter.showNotification("Debe ingresar un apellido", NotificationEventType.ERROR);
 				return false;
 			} else if (!personGenderFemale.getValue() && !personGenderMale.getValue()) {
 				presenter.showNotification("Debe seleccionar un genero", NotificationEventType.ERROR);
 				return false;
 			} else if (personActivity.getSelectedIndex() < 1) {
 				presenter.showNotification("Debe seleccionar una actividad", NotificationEventType.ERROR);
 				return false;
 			} else if (personAge.getSelectedIndex() < 1) {
 				presenter.showNotification("Debe seleccionar una edad", NotificationEventType.ERROR);
 				return false;
 			}
 		} else if (!presenter.getUser().getNaturalPerson()) {
 			if (!institutionName.getText().matches(".*\\w+.*")) {
 				presenter.showNotification("Debe ingresar la razon social", NotificationEventType.ERROR);
 				return false;
 			} else if (institutionType.getSelectedIndex() < 1) {
 				presenter.showNotification("Debe seleccionar un tipo de institución", NotificationEventType.ERROR);
 				return false;
 			}
 		} else {
 			return false;
 		}
 
 		if (!countryChile.getValue() && !countryOther.getValue()) {
 			presenter.showNotification("Debe seleccionar Chile o Resto del mundo", NotificationEventType.ERROR);
 			return false;
 		} else if (region.getSelectedIndex() < 1 && countryChile.getValue()) {
 			presenter.showNotification("Debe seleccionar una región", NotificationEventType.ERROR);
 			return false;
 		}
 
 		if (oldPassword.getText().matches("\\w+") || password1.getText().matches("\\w+") || password2.getText().matches("\\w+")) {
 			if (!oldPassword.getText().matches("\\w+") || !password1.getText().matches("\\w+") || !password2.getText().matches("\\w+")) {
 				presenter.showNotification("Para actualizar la contraseña debe completar todos los campos de contraseña", NotificationEventType.ERROR);
 				return false;
 			} else if (!presenter.getPasswordOk()) {
 				presenter.showNotification("La contraseña actual no es correcta", NotificationEventType.ERROR);
 				return false;
 			} else if (!password1.getText().equals(password2.getText())) {
 				presenter.showNotification("La contraseña nueva no coincide", NotificationEventType.ERROR);
 				return false;
 			}
 			presenter.setUpdatePassword(true);
 			return true;
 		}
 
 		return true;
 	}
 
 	@UiHandler("oldPassword")
 	public void onOldPasswordBlur(BlurEvent event) {
 		presenter.checkPassword(oldPassword.getText());
 	}
 
 	@UiHandler("countryChile")
 	public void onCountryChileValueChange(ValueChangeEvent<Boolean> event) {
 		region.setEnabled(event.getValue());
 	}
 
 	@UiHandler("countryOther")
 	public void onCountryOtherValueChange(ValueChangeEvent<Boolean> event) {
 		region.setEnabled(!event.getValue());
 	}
 
 	@UiHandler("saveChanges")
 	public void onRegisterClick(ClickEvent event) {
 		if (presenter != null) {
 			presenter.saveChanges();
 			oldPassword.setText("");
 			password1.setText("");
 			password2.setText("");
 			presenter.setPasswordOk(false);
 		}
 	}
 }
