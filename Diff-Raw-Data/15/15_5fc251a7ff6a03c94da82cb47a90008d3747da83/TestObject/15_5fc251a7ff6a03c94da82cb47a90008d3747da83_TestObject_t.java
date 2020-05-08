 package org.otherobjects.cms.binding;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.types.annotation.Property;
 import org.otherobjects.cms.types.annotation.PropertyType;
 import org.otherobjects.cms.types.annotation.Type;
 
@Type(labelProperty = "name", store="jackrabbit")
 public class TestObject extends BaseNode
 {
     // Simple properties
     private String name;
     private String testString;
     private String testText;
     private Date testDate;
     private Date testTime;
     private Date testTimestamp;
     private Long testNumber;
     private BigDecimal testDecimal;
     private Boolean testBoolean;
 
     // Reference properties
     private TestReferenceObject testReference;
     private TestReferenceObject testRequiredReference;
     private TestComponentObject testComponent;
 
     // Component properties
     private TestComponentObject testRequiredComponent;
 
     // List properties
     private List<String> testStringsList;
     private List<TestReferenceObject> testReferencesList;
     private List<TestComponentObject> testComponentsList;
 
     @Property
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     @Property
     public String getTestString()
     {
         return testString;
     }
 
     public void setTestString(String testString)
     {
         this.testString = testString;
     }
 
     @Property(type = PropertyType.TEXT)
     public String getTestText()
     {
         return testText;
     }
 
     public void setTestText(String testText)
     {
         this.testText = testText;
     }
 
     @Property
     public Date getTestDate()
     {
         return testDate;
     }
 
     public void setTestDate(Date testDate)
     {
         this.testDate = testDate;
     }
 
     @Property(type = PropertyType.TIME)
     public Date getTestTime()
     {
         return testTime;
     }
 
     public void setTestTime(Date testTime)
     {
         this.testTime = testTime;
     }
 
     @Property(type = PropertyType.TIMESTAMP)
     public Date getTestTimestamp()
     {
         return testTimestamp;
     }
 
     public void setTestTimestamp(Date testTimestamp)
     {
         this.testTimestamp = testTimestamp;
     }
 
     @Property
     public Long getTestNumber()
     {
         return testNumber;
     }
 
     public void setTestNumber(Long testNumber)
     {
         this.testNumber = testNumber;
     }
 
     @Property
     public BigDecimal getTestDecimal()
     {
         return testDecimal;
     }
 
     public void setTestDecimal(BigDecimal testDecimal)
     {
         this.testDecimal = testDecimal;
     }
 
     @Property
     public Boolean getTestBoolean()
     {
         return testBoolean;
     }
 
     public void setTestBoolean(Boolean testBoolean)
     {
         this.testBoolean = testBoolean;
     }
 
     @Property
     public TestReferenceObject getTestReference()
     {
         return testReference;
     }
 
     public void setTestReference(TestReferenceObject testReference)
     {
         this.testReference = testReference;
     }
 
     @Property(required = true)
     public TestReferenceObject getTestRequiredReference()
     {
         return testRequiredReference;
     }
 
     public void setTestRequiredReference(TestReferenceObject testRequiredReference)
     {
         this.testRequiredReference = testRequiredReference;
     }
 
     @Property(type = PropertyType.COMPONENT)
     public TestComponentObject getTestComponent()
     {
         return testComponent;
     }
 
     public void setTestComponent(TestComponentObject testComponent)
     {
         this.testComponent = testComponent;
     }
 
     @Property(type = PropertyType.COMPONENT, required = true)
     public TestComponentObject getTestRequiredComponent()
     {
         return testRequiredComponent;
     }
 
     public void setTestRequiredComponent(TestComponentObject testRequiredComponent)
     {
         this.testRequiredComponent = testRequiredComponent;
     }
 
     @Property
     public List<String> getTestStringsList()
     {
         return testStringsList;
     }
 
     public void setTestStringsList(List<String> testStringsList)
     {
         this.testStringsList = testStringsList;
     }
 
     @Property
     public List<TestReferenceObject> getTestReferencesList()
     {
         return testReferencesList;
     }
 
     public void setTestReferencesList(List<TestReferenceObject> testReferencesList)
     {
         this.testReferencesList = testReferencesList;
     }
 
     @Property(collectionElementType = PropertyType.COMPONENT)
     public List<TestComponentObject> getTestComponentsList()
     {
         return testComponentsList;
     }
 
     public void setTestComponentsList(List<TestComponentObject> testComponentsList)
     {
         this.testComponentsList = testComponentsList;
     }
 }
