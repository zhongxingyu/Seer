 package fr.cg95.cvq.business.users;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 
 import net.sf.oval.constraint.NotNull;
 import fr.cg95.cvq.service.users.HasLegalResponsibles;
 import fr.cg95.cvq.xml.common.ChildType;
 
 @HasLegalResponsibles
 @Entity
 @Table(name="child")
 public class Child extends Individual {
 
     private static final long serialVersionUID = 1L;
 
     public static Child xmlToModel(ChildType childType) {
         if (childType == null) return null;
         Child child = new Child();
         child.fillCommonModelInfo(childType);
         child.setBorn(childType.getBorn());
         if (childType.getSex() != null)
             child.setSex(SexType.forString(childType.getSex().toString()));
         child.setExternalCapDematId(childType.getExternalCapdematId());
         return child;
     }
 
     @Column(name="born")
     private boolean born = true;
 
     @NotNull(message = "sex", when = "groovy:_this.born")
     @Enumerated(EnumType.STRING)
     private SexType sex;
 
     public ChildType modelToXml() {
         ChildType childType = ChildType.Factory.newInstance();
         fillCommonXmlInfo(childType);
         childType.setBorn(born);
         if (sex != null)
            childType.setSex(fr.cg95.cvq.xml.common.SexType.Enum.forString(sex.getLegacyLabel()));
         return childType;
     }
 
     public boolean isBorn() {
         return born;
     }
 
     public void setBorn(boolean born) {
         this.born = born;
     }
 
     public SexType getSex() {
         return this.sex;
     }
 
     public void setSex(SexType sex) {
         this.sex = sex;
     }
 }
