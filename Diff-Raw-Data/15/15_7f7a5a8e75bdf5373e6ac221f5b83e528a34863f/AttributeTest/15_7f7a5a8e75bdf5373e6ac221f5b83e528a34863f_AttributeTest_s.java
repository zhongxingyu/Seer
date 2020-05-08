 /* CVS Header
    $
    $
 */
 
 package org.guanxi.idp.attribute;
 
 import org.guanxi.idp.IdPTest;
 
 /**
  * Base class for attribute tests
  */
 public abstract class AttributeTest extends IdPTest {
  protected static String[] attributorConfigFiles = new String[] {idpHome + "/WEB-INF/guanxi_idp/config/spring/farm/attributors.xml",
                                                                  idpHome + "/WEB-INF/guanxi_idp/config/spring/farm/mapper.xml",
                                                                  idpHome + "/WEB-INF/guanxi_idp/config/spring/farm/rules.xml",
                                                                  idpHome + "/WEB-INF/guanxi_idp/config/spring/farm/persistence.xml"};
 }
