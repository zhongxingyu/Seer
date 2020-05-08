 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.domain.validator;
 
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.web.security.Security;
 
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

 /**
  * Validator for the {@link VirtualPort}. Validates that the
  * {@link VirtualPort#getManagerLabel()} is unique.
  *
  */
 @Component
 public class VirtualPortValidator implements Validator {
 
   @Override
   public boolean supports(Class<?> clazz) {
     return VirtualPort.class.isAssignableFrom(clazz);
   }
 
   @Override
   public void validate(Object objToValidate, Errors errors) {
     VirtualPort virtualPort = (VirtualPort) objToValidate;
 
     validatePhysicalPort(virtualPort, errors);
     validateBandwidth(virtualPort, errors);
     validateVlanRequired(virtualPort, errors);
   }
 
   private void validatePhysicalPort(VirtualPort virtualPort, Errors errors) {
     PhysicalResourceGroup prg = virtualPort.getPhysicalResourceGroup();
 
     if (!Security.isManagerMemberOf(prg)) {
       errors.rejectValue("physicalPort", "validation.virtualport.physicalport.security",
           "You do not have the right permissions for this port");
     }
   }
 
   private void validateBandwidth(VirtualPort virtualPort, Errors errors) {
     if (virtualPort.getMaxBandwidth() != null && virtualPort.getMaxBandwidth() < 1) {
       errors.rejectValue("maxBandwidth", "validation.low");
     }
   }
 
   private void validateVlanRequired(VirtualPort virtualPort, Errors errors) {
     if ((virtualPort.getPhysicalPort() == null || virtualPort.getPhysicalPort().isVlanRequired())
        && ((virtualPort.getVlanId() == null) || Integer.valueOf(0).equals(virtualPort.getVlanId()))) {
       errors.rejectValue("vlanId", "validation.virtualport.vlanid.required.because.physicalport.requires.it");
     }
 
     if ((virtualPort.getVlanId() != null) && (virtualPort.getVlanId().intValue() > 0)
         && ((virtualPort.getPhysicalPort() == null || !virtualPort.getPhysicalPort().isVlanRequired()))) {
       errors.rejectValue("vlanId", "validation.virtualport.vlanid.not.allowed.since.physicalport.does.not.require.it");
     }
   }
 
 }
