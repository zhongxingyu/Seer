 package org.picketlink.idm.test.support.hibernate;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import junit.framework.Assert;
 import org.hibernate.SessionFactory;
 import org.picketlink.idm.test.support.IdentityTestPOJO;
 import org.picketlink.idm.test.support.JNDISupport;
 
 public class HibernateTestPOJO extends IdentityTestPOJO
 {
 
    protected String dataSourceName = "hsqldb";
 
    protected String hibernateConfig = "datasources/hibernates.xml";
 
    protected String datasources = "datasources/datasources.xml";
 
    protected HibernateSupport hibernateSupport;
 
 
 
    public void start() throws Exception
    {
       overrideFromProperties();
 
       JNDISupport jndiSupport = new JNDISupport();
       jndiSupport.start();
 
 
       identityConfig = "hibernate-test-identity-config.xml";
 
       DataSourceConfig dataSourceConfig = DataSourceConfig.obtainConfig(datasources, dataSourceName);
 
       HibernateSupport.Config hibernateSupportConfig = HibernateSupport.getConfig(dataSourceName, hibernateConfig);
 
       hibernateSupport = new HibernateSupport();
       hibernateSupport.setConfig(hibernateSupportConfig);
       hibernateSupport.setDataSourceConfig(dataSourceConfig);
       hibernateSupport.setJNDIName("java:/jbossidentity/HibernateStoreSessionFactory");
 
       String prefix = "mappings/";
 
       //Sybase support hack
      if (dataSourceName.startsWith("sybase-"))
       {
          prefix = "sybase-mappings/";
       }
 
       List<String> mappings = new LinkedList<String>();
       mappings.add(prefix + "HibernateIdentityObject.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectCredentialBinaryValue.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectAttributeBinaryValue.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectAttribute.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectCredential.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectCredentialType.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectRelationship.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectRelationshipName.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectRelationshipType.hbm.xml");
       mappings.add(prefix + "HibernateIdentityObjectType.hbm.xml");
       mappings.add(prefix + "HibernateRealm.hbm.xml");
 
       hibernateSupport.setMappings(mappings);
 
       hibernateSupport.start();
 
 
    }
 
    public void stop() throws Exception
    {
       hibernateSupport.getSessionFactory().getStatistics().logSummary();
       hibernateSupport.stop();
 
 
    }
 
    public void overrideFromProperties() throws Exception
    {
       String dsName = System.getProperties().getProperty("dataSourceName");
 
       if (dsName != null && dsName.length() > 0 && !dsName.startsWith("$"))
       {
          setDataSourceName(dsName);
       }
 
    }
 
    public SessionFactory getSessionFactory()
    {
       return getHibernateSupport().getSessionFactory();
    }
 
    public void setDataSourceName(String dataSourceName)
    {
       this.dataSourceName = dataSourceName;
    }
 
    public void setHibernateConfig(String hibernateConfig)
    {
       this.hibernateConfig = hibernateConfig;
    }
 
    public void setDatasources(String datasources)
    {
       this.datasources = datasources;
    }
 
    public String getDataSourceName()
    {
       return dataSourceName;
    }
 
    public String getHibernateConfig()
    {
       return hibernateConfig;
    }
 
    public String getDatasources()
    {
       return datasources;
    }
 
    public HibernateSupport getHibernateSupport()
    {
       return hibernateSupport;
    }
 
    public void begin()
    {
       getHibernateSupport().getCurrentSession().getTransaction().begin();
    }
 
    public void commit()
    {
       Assert.assertTrue(getHibernateSupport().commitTransaction());
    }
 
 
 }
