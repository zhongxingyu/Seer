 package net.ontopia.presto.jaxb;
 
 import java.util.Collection;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 @XmlRootElement
 @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
 @JsonIgnoreProperties(ignoreUnknown=true)
 public class FieldData {
 
     private String id;
     private String name;
     private Boolean readOnly;
     private Boolean embeddable;
 
     private String datatype;
     private String validation;
     private String interfaceControl;
    private Object extra;
     
     private Integer minCardinality;
     private Integer maxCardinality;
     
     private Collection<Link> links;
     private Collection<Value> values;
 
     private Collection<TopicType> valueTypes;
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getId() {
         return id;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setLinks(Collection<Link> links) {
         if (links.isEmpty()) {
             this.links = null;
         } else {
             this.links = links;
         }
     }
 
     public Collection<Link> getLinks() {
         return links;
     }
 
     public void setReadOnly(Boolean readOnly) {
         this.readOnly = readOnly;
     }
 
     public Boolean isReadOnly() {
         return readOnly;
     }
 
     public Boolean isEmbeddable() {
         return embeddable;
     }
 
     public void setEmbeddable(Boolean embeddable) {
         this.embeddable = embeddable;
     }
 
     public void setDatatype(String datatype) {
         this.datatype = datatype;
     }
 
     public String getDatatype() {
         return datatype;
     }
 
     public void setValidation(String validation) {
         this.validation = validation;
     }
 
     public String getValidation() {
         return validation;
     }
 
     public void setInterfaceControl(String interfaceControl) {
         this.interfaceControl = interfaceControl;
     }
 
     public String getInterfaceControl() {
         return interfaceControl;
     }
 
     public void setMinCardinality(Integer minCardinality) {
         this.minCardinality = minCardinality;
     }
 
     public Integer getMinCardinality() {
         return minCardinality;
     }
 
     public void setMaxCardinality(Integer maxCardinality) {
         this.maxCardinality = maxCardinality;
     }
 
     public Integer getMaxCardinality() {
         return maxCardinality;
     }
 
     public void setValues(Collection<Value> values) {
         this.values = values;
     }
 
     public Collection<Value> getValues() {
         return values;
     }
 
    public void setExtra(Object extra) {
         this.extra = extra;
     }
 
    public Object getExtra() {
         return extra;
     }
 
     public void setValueTypes(Collection<TopicType> valueTypes) {
         this.valueTypes = valueTypes;
     }
 
     public Collection<TopicType> getValueTypes() {
         return valueTypes;
     }
 
 }
