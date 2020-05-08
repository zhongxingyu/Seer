 package ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.model;
 
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.utils.ObjectUtil;
 
 import java.util.Date;
 
 /**
  * @author Denis Khurtin
  */
 public class PassportModel extends PersonModel {
 
     public boolean isActual() {
        return get("actual") == null ? false : (Boolean) get("actual");
     }
 
     public void setActual(final boolean actual) {
         set("actual", actual);
     }
 
     public String getSeries() {
         return get("series");
     }
 
     public void setSeries(final String series) {
         set("series", series);
     }
 
     public String getNumber() {
         return get("number");
     }
 
     public void setNumber(final String number) {
         set("number", number);
     }
 
     public String getIssuingOrganization() {
         return get("issuingOrganization");
     }
 
     public void setIssuingOrganization(final String issuingOrganization) {
         set("issuingOrganization", issuingOrganization);
     }
 
     public Date getIssuedDate() {
         return get("issuedDate");
     }
 
     public void setIssuedDate(final Date issuedDate) {
         set("issuedDate", issuedDate);
     }
 
     public String getCitizenship() {
         return get("citizenship");
     }
 
     public void setCitizenship(final String citizenship) {
         set("citizenship", citizenship);
     }
 
     @Override
     public boolean equals(final Object model) {
         if (this == model) {
             return true;
         }
         if (model == null || this.getClass() != model.getClass()) {
             return false;
         }
 
         final PassportModel that = (PassportModel) model;
 
         return super.equals(that) &&
                 ObjectUtil.equal(this.isActual(), that.isActual()) &&
                 ObjectUtil.equal(this.getSeries(), that.getSeries()) &&
                 ObjectUtil.equal(this.getNumber(), that.getNumber()) &&
                 ObjectUtil.equal(this.getIssuingOrganization(), that.getIssuingOrganization()) &&
                 ObjectUtil.equal(this.getIssuedDate(), that.getIssuedDate()) &&
                 ObjectUtil.equal(this.getCitizenship(), that.getCitizenship());
     }
 
     @Override
     public int hashCode() {
         return ObjectUtil.hashCode(
                 super.hashCode(),
                 isActual(),
                 getSeries(),
                 getNumber(),
                 getIssuingOrganization(),
                 getIssuedDate(),
                 getCitizenship());
     }
 }
