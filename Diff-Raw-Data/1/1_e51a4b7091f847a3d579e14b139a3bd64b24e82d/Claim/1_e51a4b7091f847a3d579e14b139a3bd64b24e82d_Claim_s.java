 package org.pillarone.riskanalytics.domain.pc.claims;
 
 import org.pillarone.riskanalytics.core.components.IComponentMarker;
 import org.pillarone.riskanalytics.core.packets.MultiValuePacket;
 import org.pillarone.riskanalytics.domain.pc.constants.ClaimType;
import org.pillarone.riskanalytics.domain.pc.generators.claims.PerilMarker;
 import org.pillarone.riskanalytics.domain.pc.generators.severities.Event;
 import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
 import org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.IReinsuranceContractMarker;
 import org.pillarone.riskanalytics.domain.pc.underwriting.ExposureInfo;
 
 import java.util.*;
 
 /**
  * Basic claim object not recommended for development and inflation
  */
 public class Claim extends MultiValuePacket {
 
     private double ultimate;
     private Claim originalClaim;
     private Event event;
     private Double fractionOfPeriod = 0d;
     private ClaimType claimType;
     /** contains the claims or reserve generator in which the claim object was instantiated */
     private IComponentMarker peril;
     private LobMarker lineOfBusiness;
     private IReinsuranceContractMarker reinsuranceContract;
     private ExposureInfo exposure;
 
     private static final String ULTIMATE = "ultimate";
 
     public Claim() {
     }
 
     public Claim copy() {
         Claim copy = ClaimPacketFactory.createPacket();
         copy.set(this);
         return copy;
     }
 
     public void set(Claim claim) {
         setOrigin(claim.getOrigin());
         setUltimate(claim.getUltimate());
         setOriginalClaim(claim.getOriginalClaim());
         setEvent(claim.getEvent());
         setFractionOfPeriod(claim.getFractionOfPeriod());
         setClaimType(claim.getClaimType());
         setPeril(claim.getPeril());
         setLineOfBusiness(claim.getLineOfBusiness());
         setReinsuranceContract(claim.getReinsuranceContract());
         setExposure(claim.getExposure());
     }
 
     public void plus(Claim claim) {
         ultimate += claim.getUltimate();
     }
 
     public void minus(Claim claim) {
         ultimate -= claim.getUltimate();
     }
 
     public void scale(double factor) {
         ultimate *= factor;
     }
 
     public Claim getNetClaim(Claim cededClaim) {
         Claim netClaim = copy();
         netClaim.ultimate -= cededClaim.ultimate;
         return netClaim;
     }
 
     @Override
     public Map<String, Number> getValuesToSave() throws IllegalAccessException {
         Map<String, Number> valuesToSave = new HashMap<String, Number>(1);
         valuesToSave.put(ULTIMATE, ultimate);
         return valuesToSave;
     }
 
     @Override
     public List<String> getFieldNames() {
         return Arrays.asList(ULTIMATE);
     }
 
     public String toString() {
         String separator = ", ";
         StringBuilder result = new StringBuilder();
         result.append(ultimate);
         result.append(separator);
         if (claimType != null) result.append(claimType).append(separator);
         if (origin != null) result.append(origin.getName()).append(separator);
         if (originalClaim != null) result.append(System.identityHashCode(originalClaim)).append(separator);
         if (lineOfBusiness != null) result.append(lineOfBusiness.getName()).append(separator);
         if (peril != null) result.append(peril.getName()).append(separator);
         if (reinsuranceContract != null) result.append(reinsuranceContract.getName()).append(separator);
         return result.toString();
     }
 
     public Double getFractionOfPeriod() {
         return fractionOfPeriod;
     }
 
     public void setFractionOfPeriod(Double fractionOfPeriod) {
         this.fractionOfPeriod = fractionOfPeriod;
     }
 
     public ClaimType getClaimType() {
         return claimType;
     }
 
     public void setClaimType(ClaimType claimType) {
         this.claimType = claimType;
     }
 
     public Claim getOriginalClaim() {
         return originalClaim;
     }
 
     public void setOriginalClaim(Claim originalClaim) {
         this.originalClaim = originalClaim;
     }
 
     public Event getEvent() {
         return event;
     }
 
     public void setEvent(Event event) {
         this.event = event;
     }
 
     public IComponentMarker getPeril() {
         return peril;
     }
 
     public void setPeril(IComponentMarker peril) {
         this.peril = peril;
     }
 
     public LobMarker getLineOfBusiness() {
         return lineOfBusiness;
     }
 
     public void setLineOfBusiness(LobMarker lineOfBusiness) {
         this.lineOfBusiness = lineOfBusiness;
     }
 
     public double getUltimate() {
         return ultimate;
     }
 
     public void setUltimate(double ultimate) {
         this.ultimate = ultimate;
     }
 
     /**
      * @deprecated use getIncurred()
      * @return
      */
     @Deprecated
     public double getValue() {
         return ultimate;
     }
 
     /**
      * @deprecated use setIncurred()
      * @param ultimate
      */
     @Deprecated
     public void setValue(double ultimate) {
         this.ultimate = ultimate;
     }
 
     public IReinsuranceContractMarker getReinsuranceContract() {
         return reinsuranceContract;
     }
 
     public void setReinsuranceContract(IReinsuranceContractMarker reinsuranceContract) {
         this.reinsuranceContract = reinsuranceContract;
     }
 
     public ExposureInfo getExposure() {
         return exposure;
     }
 
     public void setExposure(ExposureInfo exposure) {
         this.exposure = exposure;
     }
 
     public  boolean hasExposureInfo() {
         return this.exposure != null;
     }
 }
