 package org.netvogue.customer.service.converters.input;
 
 import org.netvogue.customer.service.converter.Converter;
 import org.netvogue.customer.service.representations.UserRepresentation;
 import org.netvogue.ecommerce.domain.model.User;
 
 public class UserToUserRepresentationConverter implements Converter<User, UserRepresentation> {
 
   @Override
   public UserRepresentation convert(final User source) {
     UserRepresentation rep = new UserRepresentation();
     rep.setAboutUs(source.getAboutUs());
     rep.setAddress(source.getAddress());
     rep.setCity(source.getCity());
     rep.setCountry(source.getCountry());
     rep.setEmail(source.getEmail());
     rep.setFirstName(source.getFirstName());
     rep.setLastName(source.getLastName());
     rep.setMobileNo(source.getMobileNo());
     rep.setTelephoneNo1(source.getTelephoneNo1());
     rep.setTelephoneNo2(source.getTelephoneNo2());
     rep.setPrimarycontact(source.getPrimarycontact());
     rep.setProfilePicLink(source.getProfilePicLink());
     rep.setRole(source.getRole().toString());
     rep.setUsername(source.getUsername());
     rep.setSubscirptionType(source.getSubscirptionType().toString());
     rep.setZipCode(source.getZipCode());
     rep.setSalt(source.getSalt());
     return rep;
   }
 }
