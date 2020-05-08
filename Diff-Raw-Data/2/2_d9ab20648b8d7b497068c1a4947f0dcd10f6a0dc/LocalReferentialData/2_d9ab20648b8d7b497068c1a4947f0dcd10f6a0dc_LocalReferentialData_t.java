 package fr.cg95.cvq.business.request;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import fr.cg95.cvq.xml.common.LocalReferentialDataType;
 
 @Entity
 @Table(name="local_referential_data")
 public class LocalReferentialData implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue(strategy=GenerationType.SEQUENCE)
     private Long id;
 
     private String name;
     private Integer priority;
     
     @Column(name="additional_information_label")
     private String additionalInformationLabel;
 
     @Column(name="additional_information_value")
     private String additionalInformationValue;
     
     // FIXME how to removeOrphan on many2many?
    @ManyToMany(cascade=CascadeType.REMOVE,fetch=FetchType.EAGER)
     @JoinTable(name="local_referential_association",
             joinColumns=@JoinColumn(name="local_referential_parent_data_id"),
             inverseJoinColumns=@JoinColumn(name="local_referential_child_data_id"))
     private Set<LocalReferentialData> children;
 
     @ManyToOne(fetch=FetchType.EAGER)
     @JoinColumn(name="local_referential_parent_data_id")
     private LocalReferentialData parent;
 
     public LocalReferentialData() {}
 
     public static LocalReferentialDataType modelToXml(LocalReferentialData localReferentialData) {
 
         LocalReferentialDataType localReferentialDataType =
             LocalReferentialDataType.Factory.newInstance();
         if (localReferentialData.getId() != null)
             localReferentialDataType.setId(localReferentialData.getId().longValue());
         localReferentialDataType.setName(localReferentialData.getName());
         if (localReferentialData.getPriority() != null)
             localReferentialDataType.setPriority(new BigInteger(localReferentialData.getPriority().toString()));
         if (localReferentialData.getAdditionalInformationLabel() != null)
             localReferentialDataType.setAdditionalInformationLabel(localReferentialData.getAdditionalInformationLabel());
         if (localReferentialData.getAdditionalInformationValue() != null)
             localReferentialDataType.setAdditionalInformationValue(localReferentialData.getAdditionalInformationValue());
         if (localReferentialData.getChildren() != null) {
             LocalReferentialDataType[] lrdTab =
                 new LocalReferentialDataType[localReferentialData.getChildren().size()];
             int i = 0;
             for (LocalReferentialData lrd : localReferentialData.getChildren()) {
                 lrdTab[i] = LocalReferentialData.modelToXml(lrd);
                 i++;
             }
             localReferentialDataType.setChildrenArray(lrdTab);
         }
         return localReferentialDataType;
     }
 
     public static LocalReferentialData xmlToModel(LocalReferentialDataType localReferentialDataType) {
 
         LocalReferentialData localReferentialData = new LocalReferentialData();
         if (localReferentialDataType.getId() != 0)
             localReferentialData.setId(new Long(localReferentialDataType.getId()));
         localReferentialData.setName(localReferentialDataType.getName());
         if (localReferentialDataType.getPriority() != null)
             localReferentialData.setPriority(new Integer(localReferentialDataType.getPriority().intValue()));
         if (localReferentialDataType.getAdditionalInformationLabel() != null)
             localReferentialData.setAdditionalInformationLabel(localReferentialDataType.getAdditionalInformationLabel());
         if (localReferentialDataType.getAdditionalInformationValue() != null)
             localReferentialData.setAdditionalInformationValue(localReferentialDataType.getAdditionalInformationValue());
         HashSet<LocalReferentialData> childrenSet = new HashSet<LocalReferentialData>();
         if (localReferentialDataType.sizeOfChildrenArray() > 0) {
             for (int i = 0; i < localReferentialDataType.getChildrenArray().length; i++) {
                 LocalReferentialData tempData =
                     LocalReferentialData.xmlToModel(localReferentialDataType.getChildrenArray(i));
                 tempData.setParent(localReferentialData);
                 childrenSet.add(tempData);
             }
         }
         localReferentialData.setChildren(childrenSet);
 
         return localReferentialData;
     }
 
     @Override
     public LocalReferentialData clone() {
         LocalReferentialData result = new LocalReferentialData();
         result.setAdditionalInformationLabel(getAdditionalInformationLabel());
         result.setAdditionalInformationValue(getAdditionalInformationValue());
         Set<LocalReferentialData> children = new HashSet<LocalReferentialData>();
         for (LocalReferentialData child : getChildren()) {
             children.add(child.clone());
         }
         result.setChildren(children);
         result.setName(getName());
         if (getParent() != null) result.setParent(getParent().clone());
         result.setPriority(getPriority());
         return result;
     }
 
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getPriority() {
         return this.priority;
     }
 
     public void setPriority(Integer priority) {
         this.priority = priority;
     }
 
     @Column(name="additional_information_label")
     public String getAdditionalInformationLabel() {
         return this.additionalInformationLabel;
     }
 
     public void setAdditionalInformationLabel(String additionalInformationLabel) {
         this.additionalInformationLabel = additionalInformationLabel;
     }
 
     @Column(name="additional_information_value")
     public String getAdditionalInformationValue() {
         return this.additionalInformationValue;
     }
 
     public void setAdditionalInformationValue(String additionalInformationValue) {
         this.additionalInformationValue = additionalInformationValue;
     }
 
     public Set<LocalReferentialData> getChildren() {
         return this.children;
     }
 
     public void setChildren(Set<LocalReferentialData> children) {
         this.children = children;
     }
 
     public LocalReferentialData getParent() {
         return this.parent;
     }
 
     public void setParent(LocalReferentialData parent) {
         this.parent = parent;
     }
     
     @Override
     public boolean equals(Object object) {
         if (object == null)
             return false;
         if (!(object instanceof LocalReferentialData))
             return false;
 
         LocalReferentialData toCompareWith = (LocalReferentialData) object;
         if (id != null) {
             return id.equals(toCompareWith.getId());
         } else {
             if (toCompareWith.getId() != null)
                 return false;
             else {
                 if (name != null)
                     return name.equals(toCompareWith.getName());
                 else {
                     if (toCompareWith.getName() != null)
                         return false;
                     else
                         return true;
                 }
             }
                 
         }
     }
     
     @Override
     public int hashCode() {
         if (id != null)
             return 42 * id.hashCode();
         else if (name != null)
             return 42 * name.hashCode();
         return 0;
     }
 }
