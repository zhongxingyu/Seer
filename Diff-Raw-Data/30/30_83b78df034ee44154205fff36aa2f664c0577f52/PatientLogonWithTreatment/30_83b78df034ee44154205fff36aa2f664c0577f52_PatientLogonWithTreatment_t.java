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
 
 package org.patientview.patientview.logon;
 
 import org.apache.commons.lang.StringUtils;
 import org.patientview.ibd.Ibd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.text.ParseException;
import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class PatientLogonWithTreatment extends PatientLogon {
 
     private String treatment;
     private Date dateofbirth;
    private static final String DATE_FORMAT = "dd.MM.y";
    private static final String DATE_FORMAT_2 = "yyyy-MM-dd";
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientLogonWithTreatment.class);
 
     public PatientLogonWithTreatment() {
     }
 
     public String getTreatment() {
         return treatment;
     }
 
     public void setTreatment(String treatment) {
         this.treatment = StringUtils.defaultIfBlank(treatment, "");
     }
 
     public Date getDateofbirth() {
         return dateofbirth;
     }
 
    public void setDateofbirth(String dateofbirth) {
        if (dateofbirth != null) {
            // It seems that the Dob in the DB have different date formats.
            for (String dateFormat : new String[]{DATE_FORMAT, DATE_FORMAT_2}) {
                try {
                    this.dateofbirth = new SimpleDateFormat(dateFormat).parse(dateofbirth);
                } catch (ParseException e) {
                    LOGGER.debug("Could not parse date of birth {}", dateofbirth);
                }
            }
        }
     }
 
     public String getDateofbirthFormatted() {
         if (dateofbirth != null) {
             return Ibd.DATE_FORMAT.format(dateofbirth);
         }
 
         return "";
     }
 }
