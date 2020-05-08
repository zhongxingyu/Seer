 package fr.cg95.cvq.business.request.urbanism;
 
 import fr.cg95.cvq.business.request.*;
 import fr.cg95.cvq.business.users.*;
 import fr.cg95.cvq.business.authority.*;
 import fr.cg95.cvq.xml.common.*;
 import fr.cg95.cvq.xml.request.urbanism.*;
 
 import org.apache.xmlbeans.XmlOptions;
 import org.apache.xmlbeans.XmlObject;
 
 import fr.cg95.cvq.xml.common.RequestType;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.*;
 
 /**
  * Generated class file, do not edit !
  *
  * @hibernate.joined-subclass
  *  table="alignment_numbering_connection_request"
  *  lazy="false"
  * @hibernate.joined-subclass-key
  *  column="id"
  */
 public class AlignmentNumberingConnectionRequest extends Request implements Serializable { 
 
     private static final long serialVersionUID = 1L;
 
 
 
     public AlignmentNumberingConnectionRequest() {
         super();
         requesterQuality = fr.cg95.cvq.business.request.urbanism.AncrRequesterQualityType.OWNER;
     }
 
 
     @Override
     public final String modelToXmlString() {
 
         AlignmentNumberingConnectionRequestDocument object = (AlignmentNumberingConnectionRequestDocument) this.modelToXml();
         XmlOptions opts = new XmlOptions();
         opts.setSavePrettyPrint();
         opts.setSavePrettyPrintIndent(4);
         opts.setUseDefaultNamespace();
         opts.setCharacterEncoding("UTF-8");
         return object.xmlText(opts);
     }
 
     @Override
     public final XmlObject modelToXml() {
 
         Calendar calendar = Calendar.getInstance();
         Date date = null;
         AlignmentNumberingConnectionRequestDocument alignmentNumberingConnectionRequestDoc = AlignmentNumberingConnectionRequestDocument.Factory.newInstance();
         AlignmentNumberingConnectionRequestDocument.AlignmentNumberingConnectionRequest alignmentNumberingConnectionRequest = alignmentNumberingConnectionRequestDoc.addNewAlignmentNumberingConnectionRequest();
         super.fillCommonXmlInfo(alignmentNumberingConnectionRequest);
         if (this.isNumbering != null)
             alignmentNumberingConnectionRequest.setIsNumbering(this.isNumbering.booleanValue());
         if (this.otherAddress != null)
             alignmentNumberingConnectionRequest.setOtherAddress(Address.modelToXml(this.otherAddress));
         alignmentNumberingConnectionRequest.setOwnerFirstNames(this.ownerFirstNames);
         if (this.number != null)
             alignmentNumberingConnectionRequest.setNumber(new BigInteger(this.number.toString()));
         if (this.area != null)
             alignmentNumberingConnectionRequest.setArea(new BigInteger(this.area.toString()));
         if (this.moreThanTwoYears != null)
             alignmentNumberingConnectionRequest.setMoreThanTwoYears(this.moreThanTwoYears.booleanValue());
         if (this.ownerAddress != null)
             alignmentNumberingConnectionRequest.setOwnerAddress(Address.modelToXml(this.ownerAddress));
         if (this.requesterQuality != null)
             alignmentNumberingConnectionRequest.setRequesterQuality(fr.cg95.cvq.xml.request.urbanism.AncrRequesterQualityType.Enum.forString(this.requesterQuality.toString()));
         alignmentNumberingConnectionRequest.setSection(this.section);
         alignmentNumberingConnectionRequest.setTransportationRoute(this.transportationRoute);
         alignmentNumberingConnectionRequest.setLocality(this.locality);
         if (this.isConnection != null)
             alignmentNumberingConnectionRequest.setIsConnection(this.isConnection.booleanValue());
         if (this.isAccountAddress != null)
             alignmentNumberingConnectionRequest.setIsAccountAddress(this.isAccountAddress.booleanValue());
         if (this.isAlignment != null)
             alignmentNumberingConnectionRequest.setIsAlignment(this.isAlignment.booleanValue());
         alignmentNumberingConnectionRequest.setOwnerLastName(this.ownerLastName);
         return alignmentNumberingConnectionRequestDoc;
     }
 
     @Override
     public RequestType modelToXmlRequest() {
         AlignmentNumberingConnectionRequestDocument alignmentNumberingConnectionRequestDoc =
             (AlignmentNumberingConnectionRequestDocument) modelToXml();
         return alignmentNumberingConnectionRequestDoc.getAlignmentNumberingConnectionRequest();
     }
 
     public static AlignmentNumberingConnectionRequest xmlToModel(AlignmentNumberingConnectionRequestDocument alignmentNumberingConnectionRequestDoc) {
 
         AlignmentNumberingConnectionRequestDocument.AlignmentNumberingConnectionRequest alignmentNumberingConnectionRequestXml = alignmentNumberingConnectionRequestDoc.getAlignmentNumberingConnectionRequest();
         Calendar calendar = Calendar.getInstance();
         List list = new ArrayList();
         AlignmentNumberingConnectionRequest alignmentNumberingConnectionRequest = new AlignmentNumberingConnectionRequest();
         alignmentNumberingConnectionRequest.fillCommonModelInfo(alignmentNumberingConnectionRequest,alignmentNumberingConnectionRequestXml);
         alignmentNumberingConnectionRequest.setIsNumbering(Boolean.valueOf(alignmentNumberingConnectionRequestXml.getIsNumbering()));
         if (alignmentNumberingConnectionRequestXml.getOtherAddress() != null)
             alignmentNumberingConnectionRequest.setOtherAddress(Address.xmlToModel(alignmentNumberingConnectionRequestXml.getOtherAddress()));
         alignmentNumberingConnectionRequest.setOwnerFirstNames(alignmentNumberingConnectionRequestXml.getOwnerFirstNames());
         alignmentNumberingConnectionRequest.setNumber(alignmentNumberingConnectionRequestXml.getNumber());
         alignmentNumberingConnectionRequest.setArea(alignmentNumberingConnectionRequestXml.getArea());
         alignmentNumberingConnectionRequest.setMoreThanTwoYears(Boolean.valueOf(alignmentNumberingConnectionRequestXml.getMoreThanTwoYears()));
         if (alignmentNumberingConnectionRequestXml.getOwnerAddress() != null)
             alignmentNumberingConnectionRequest.setOwnerAddress(Address.xmlToModel(alignmentNumberingConnectionRequestXml.getOwnerAddress()));
         if (alignmentNumberingConnectionRequestXml.getRequesterQuality() != null)
             alignmentNumberingConnectionRequest.setRequesterQuality(fr.cg95.cvq.business.request.urbanism.AncrRequesterQualityType.forString(alignmentNumberingConnectionRequestXml.getRequesterQuality().toString()));
         else
             alignmentNumberingConnectionRequest.setRequesterQuality(fr.cg95.cvq.business.request.urbanism.AncrRequesterQualityType.getDefaultAncrRequesterQualityType());
         alignmentNumberingConnectionRequest.setSection(alignmentNumberingConnectionRequestXml.getSection());
         alignmentNumberingConnectionRequest.setTransportationRoute(alignmentNumberingConnectionRequestXml.getTransportationRoute());
         alignmentNumberingConnectionRequest.setLocality(alignmentNumberingConnectionRequestXml.getLocality());
         alignmentNumberingConnectionRequest.setIsConnection(Boolean.valueOf(alignmentNumberingConnectionRequestXml.getIsConnection()));
         alignmentNumberingConnectionRequest.setIsAccountAddress(Boolean.valueOf(alignmentNumberingConnectionRequestXml.getIsAccountAddress()));
         alignmentNumberingConnectionRequest.setIsAlignment(Boolean.valueOf(alignmentNumberingConnectionRequestXml.getIsAlignment()));
         alignmentNumberingConnectionRequest.setOwnerLastName(alignmentNumberingConnectionRequestXml.getOwnerLastName());
         return alignmentNumberingConnectionRequest;
     }
 
     private Boolean isNumbering;
 
     public final void setIsNumbering(final Boolean isNumbering) {
         this.isNumbering = isNumbering;
     }
 
 
     /**
      * @hibernate.property
      *  column="is_numbering"
      */
     public final Boolean getIsNumbering() {
         return this.isNumbering;
     }
 
     private fr.cg95.cvq.business.users.Address otherAddress;
 
     public final void setOtherAddress(final fr.cg95.cvq.business.users.Address otherAddress) {
         this.otherAddress = otherAddress;
     }
 
 
     /**
      * @hibernate.many-to-one
      *  cascade="all"
      *  column="other_address_id"
      *  class="fr.cg95.cvq.business.users.Address"
      */
     public final fr.cg95.cvq.business.users.Address getOtherAddress() {
         return this.otherAddress;
     }
 
     private String ownerFirstNames;
 
     public final void setOwnerFirstNames(final String ownerFirstNames) {
         this.ownerFirstNames = ownerFirstNames;
     }
 
 
     /**
      * @hibernate.property
      *  column="owner_first_names"
      */
     public final String getOwnerFirstNames() {
         return this.ownerFirstNames;
     }
 
     private java.math.BigInteger number;
 
     public final void setNumber(final java.math.BigInteger number) {
         this.number = number;
     }
 
 
     /**
      * @hibernate.property
      *  column="number"
      *  type="serializable"
      */
     public final java.math.BigInteger getNumber() {
         return this.number;
     }
 
     private java.math.BigInteger area;
 
     public final void setArea(final java.math.BigInteger area) {
         this.area = area;
     }
 
 
     /**
      * @hibernate.property
      *  column="area"
      *  type="serializable"
      */
     public final java.math.BigInteger getArea() {
         return this.area;
     }
 
     private Boolean moreThanTwoYears;
 
     public final void setMoreThanTwoYears(final Boolean moreThanTwoYears) {
         this.moreThanTwoYears = moreThanTwoYears;
     }
 
 
     /**
      * @hibernate.property
      *  column="more_than_two_years"
      */
     public final Boolean getMoreThanTwoYears() {
         return this.moreThanTwoYears;
     }
 
     private fr.cg95.cvq.business.users.Address ownerAddress;
 
     public final void setOwnerAddress(final fr.cg95.cvq.business.users.Address ownerAddress) {
         this.ownerAddress = ownerAddress;
     }
 
 
     /**
      * @hibernate.many-to-one
      *  cascade="all"
      *  column="owner_address_id"
      *  class="fr.cg95.cvq.business.users.Address"
      */
     public final fr.cg95.cvq.business.users.Address getOwnerAddress() {
         return this.ownerAddress;
     }
 
     private fr.cg95.cvq.business.request.urbanism.AncrRequesterQualityType requesterQuality;
 
     public final void setRequesterQuality(final fr.cg95.cvq.business.request.urbanism.AncrRequesterQualityType requesterQuality) {
         this.requesterQuality = requesterQuality;
     }
 
 
     /**
      * @hibernate.property
      *  column="requester_quality"
      */
     public final fr.cg95.cvq.business.request.urbanism.AncrRequesterQualityType getRequesterQuality() {
         return this.requesterQuality;
     }
 
     private String section;
 
     public final void setSection(final String section) {
         this.section = section;
     }
 
 
     /**
      * @hibernate.property
      *  column="section"
      */
     public final String getSection() {
         return this.section;
     }
 
     private String transportationRoute;
 
     public final void setTransportationRoute(final String transportationRoute) {
         this.transportationRoute = transportationRoute;
     }
 
 
     /**
      * @hibernate.property
      *  column="transportation_route"
      */
     public final String getTransportationRoute() {
         return this.transportationRoute;
     }
 
     private String locality;
 
     public final void setLocality(final String locality) {
         this.locality = locality;
     }
 
 
     /**
      * @hibernate.property
      *  column="locality"
      */
     public final String getLocality() {
         return this.locality;
     }
 
     private Boolean isConnection;
 
     public final void setIsConnection(final Boolean isConnection) {
         this.isConnection = isConnection;
     }
 
 
     /**
      * @hibernate.property
      *  column="is_connection"
      */
     public final Boolean getIsConnection() {
         return this.isConnection;
     }
 
     private Boolean isAccountAddress;
 
     public final void setIsAccountAddress(final Boolean isAccountAddress) {
         this.isAccountAddress = isAccountAddress;
     }
 
 
     /**
      * @hibernate.property
      *  column="is_account_address"
      */
     public final Boolean getIsAccountAddress() {
         return this.isAccountAddress;
     }
 
     private Boolean isAlignment;
 
     public final void setIsAlignment(final Boolean isAlignment) {
         this.isAlignment = isAlignment;
     }
 
 
     /**
      * @hibernate.property
      *  column="is_alignment"
      */
     public final Boolean getIsAlignment() {
         return this.isAlignment;
     }
 
     private String ownerLastName;
 
     public final void setOwnerLastName(final String ownerLastName) {
         this.ownerLastName = ownerLastName;
     }
 
 
     /**
      * @hibernate.property
      *  column="owner_last_name"
      *  length="38"
      */
     public final String getOwnerLastName() {
         return this.ownerLastName;
     }
 
 }
