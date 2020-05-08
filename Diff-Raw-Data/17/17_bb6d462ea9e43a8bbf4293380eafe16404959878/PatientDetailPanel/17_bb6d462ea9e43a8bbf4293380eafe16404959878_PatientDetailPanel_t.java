 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.radar.web.panels;
 
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.datetime.PatternDateConverter;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 import org.patientview.model.Patient;
 import org.patientview.radar.web.RadarApplication;
 
 import java.util.Date;
 
 public class PatientDetailPanel extends Panel {
     public PatientDetailPanel(String id, final Patient patient, String title) {
         super(id);
 
         WebMarkupContainer details = new WebMarkupContainer("details", new CompoundPropertyModel<Object>(patient));
         details.setOutputMarkupId(true);
         details.setOutputMarkupPlaceholderTag(true);
         add(details);
 
         // Note: this panel is shown after initial enter new patient, and you may not have all patient data yet
 
         /**
          * Add components
          */
 
         // title
         details.add(new Label("title", title));
 
         // radar numbediagnosisManagerr
         TextField radarNumberField;
        radarNumberField = new TextField<Long>("id", new PropertyModel<Long>(patient, "radarNo"));

         radarNumberField.setOutputMarkupId(true);
         radarNumberField.setOutputMarkupPlaceholderTag(true);
         details.add(radarNumberField);
 
         // disease group
         if (patient.getDiseaseGroup() != null) {
             Label diseaseGroup = new Label("diseaseGroup", new PropertyModel<Object>(patient.getDiseaseGroup(),
                     "name"));
             details.add(diseaseGroup);
         }
 
         // forename
         Label nameLabel = new Label("nameLabel", "Patient Name") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getForename());
             }
         };
         nameLabel.setOutputMarkupId(true);
         nameLabel.setOutputMarkupPlaceholderTag(true);
         details.add(nameLabel);
 
         TextField<Long> forename = new TextField<Long>("forename") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getForename());
             }
         };
         forename.setOutputMarkupId(true);
         forename.setOutputMarkupPlaceholderTag(true);
         details.add(forename);
 
         // surname
         TextField<Long> surname = new TextField<Long>("surname") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getSurname());
             }
         };
         surname.setOutputMarkupId(true);
         surname.setOutputMarkupPlaceholderTag(true);
         details.add(surname);
 
         // date registered
         DateTextField dateRegistered = DateTextField.forDatePattern("dateReg", RadarApplication.DATE_PATTERN);
         details.add(dateRegistered);
 
         // date of birth
         Label dobLabel = new Label("dobLabel", "Patient DOB") {
             @Override
             public boolean isVisible() {
                 return patient.getDob() != null;
             }
         };
         dobLabel.setOutputMarkupId(true);
         dobLabel.setOutputMarkupPlaceholderTag(true);
         details.add(dobLabel);
 
         DateTextField dateOfBirthTextField = new DateTextField("dob",
                 new PropertyModel<Date>(patient, "dob"), new PatternDateConverter(
                 RadarApplication.DATE_PATTERN, true)) {
             @Override
             public boolean isVisible() {
                 return patient.getDob() != null;
             }
         };
         dateOfBirthTextField.setOutputMarkupId(true);
         dateOfBirthTextField.setOutputMarkupPlaceholderTag(true);
         details.add(dateOfBirthTextField);
     }
 }
