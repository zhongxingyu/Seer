 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  * 
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package test.org.picketlink.as.subsystem.parser;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.picketlink.as.subsystem.model.ModelElement;
 import org.picketlink.as.subsystem.service.IdentityProviderService;
 import org.picketlink.identity.federation.core.config.IDPConfiguration;
 import org.picketlink.identity.federation.core.config.TrustType;
 
 /**
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  * 
  */
 public class IdentityProviderServiceTestCase extends AbstractPicketLinkSubsystemTestCase {
 
     /**
      * <p>
      * Tests if the Identity Provider services are properly installed.
      * </p>
      * 
      * @throws Exception
      */
     @Test
     public void testIdentityProviderServiceInstallation() throws Exception {
         Assert.assertNotNull(getIdentityProviderService());
        Assert.assertNotNull(getIdentityProviderService().getMetricsService());
     }
 
     /**
      * <p>
      * Tests if the PicketLink configurations for the Identity Provider were properly created.
      * </p>
      * 
      * @throws Exception
      */
     @Test
     public void testConfigureIdentityProvider() throws Exception {
         IdentityProviderService identityProviderService = getIdentityProviderService();
 
         IDPConfiguration idpSubsystemConfig = identityProviderService.getConfiguration();
         
         assertEquals(getIdentityProvider().asProperty().getValue().get(ModelElement.COMMON_ALIAS.getName()).asString(), idpSubsystemConfig.getAlias());
         assertEquals(getIdentityProvider().asProperty().getValue().get(ModelElement.COMMON_URL.getName()).asString(), idpSubsystemConfig.getIdentityURL());
         assertEquals(getIdentityProvider().asProperty().getValue().get(ModelElement.COMMON_SECURITY_DOMAIN.getName()).asString(), idpSubsystemConfig.getSecurityDomain());
         assertEquals(getIdentityProvider().asProperty().getValue().get(ModelElement.SUPPORTS_SIGNATURES.getName()).asBoolean(), idpSubsystemConfig.isSupportsSignature());
         assertEquals(getIdentityProvider().asProperty().getValue().get(ModelElement.STRICT_POST_BINDING.getName()).asBoolean(), idpSubsystemConfig.isStrictPostBinding());
         
         TrustType trustType = idpSubsystemConfig.getTrust();
         
         assertNotNull(trustType);
         assertNotNull(trustType.getDomains());
         Assert.assertFalse(trustType.getDomains().isEmpty());
         
         assertNotNull(identityProviderService.getPicketLinkType());
         assertNotNull(identityProviderService.getPicketLinkType().getStsType());
         
         assertEquals(identityProviderService.getPicketLinkType().getStsType().getTokenTimeout(), getFederationService().getSamlConfig().getTokenTimeout());
         assertEquals(identityProviderService.getPicketLinkType().getStsType().getClockSkew(), getFederationService().getSamlConfig().getClockSkew());
     }
     
 }
