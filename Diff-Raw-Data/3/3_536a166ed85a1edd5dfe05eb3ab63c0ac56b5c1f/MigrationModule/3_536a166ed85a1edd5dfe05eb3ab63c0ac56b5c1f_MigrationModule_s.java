 /* 
  * Copyright 2008 The Corner Team.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package corner.services.migration;
 
 
 import org.apache.tapestry5.hibernate.HibernateConfigurer;
 import org.apache.tapestry5.hibernate.HibernateSessionManager;
 import org.apache.tapestry5.ioc.MappedConfiguration;
 import org.apache.tapestry5.ioc.ObjectLocator;
 import org.apache.tapestry5.ioc.OrderedConfiguration;
 import org.apache.tapestry5.ioc.ServiceBinder;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.annotations.Symbol;
 import org.apache.tapestry5.services.ApplicationInitializerFilter;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.dialect.H2Dialect;
 import org.hibernate.dialect.MySQL5InnoDBDialect;
 import org.hibernate.dialect.MySQLInnoDBDialect;
 
 import corner.services.migration.impl.ConnectionAdapterSourceImpl;
 import corner.services.migration.impl.DBMigrationInitializer;
 import corner.services.migration.impl.MigrationServiceImpl;
 import corner.services.migration.impl.SchemaInfo;
 import corner.services.migration.impl.adapter.H2ConnectionAdapter;
 import corner.services.migration.impl.adapter.MySQLConnectionAdapter;
 
 /**
  * 数据库升级的module
  * 
  * @author <a href="jun.tsai@ganshane.net">Jun Tsai</a>
  * @version $Revision: 5160 $
  * @since 0.0.2
  */
 public class MigrationModule {
     /** enable database auto upgrade **/
 	public static final String ENABLE_AUTO_UPGRADE="enable-db-upgrade";
 	public static void bind(ServiceBinder binder) {
 		binder.bind(MigrationService.class, MigrationServiceImpl.class);
 		binder.bind(ConnectionAdapterSource.class,ConnectionAdapterSourceImpl.class);
 	}
 	public static void contributeFactoryDefaults(
 			MappedConfiguration<String, String> configuration) {
 		configuration.add(ENABLE_AUTO_UPGRADE,"false");
 	}
 	/**
 	 * 初始化网站的数据
 	 * 
 	 * @param configuration
 	 *            配置
 	 * @param treeService
 	 *            tree service.
 	 */
 	public static void contributeApplicationInitializer(
 			OrderedConfiguration<ApplicationInitializerFilter> configuration,
 			@Inject
 			MigrationService migrationService,
 			@Inject
 			HibernateSessionManager sessionManager,
 			@Inject
 			@Symbol(ENABLE_AUTO_UPGRADE)
 			boolean enableAutoUpgrade,
             ObjectLocator locator
 	) {
 		if(enableAutoUpgrade){
//			configuration.add("dbmigraion",new DBMigrationInitializer(migrationService,sessionManager));
			configuration.add("dbmigraion",locator.autobuild(DBMigrationInitializer.class));
 		}
 
 	}
 	//加入数据库信息的实体类
 	public static void contributeHibernateSessionSource(OrderedConfiguration<HibernateConfigurer> config)
 	{
 	  config.add("SchemaInfo", new HibernateConfigurer(){
 
 		@Override
 		public void configure(Configuration configuration) {
 			((AnnotationConfiguration)configuration).addAnnotatedClass(SchemaInfo.class);
 		}});
 	}
 
 	/**
      * Contributes {@link ConnectionAdapter}s for types: <ul> <li>Object <li>String <li>Enum </ul>
      */
     public static void contributeConnectionAdapterSource(MappedConfiguration<Class, ConnectionAdapter> configuration)
     {
        configuration.add(H2Dialect.class, new H2ConnectionAdapter());
        configuration.add(MySQL5InnoDBDialect.class,new MySQLConnectionAdapter());
        configuration.add(MySQLInnoDBDialect.class,new MySQLConnectionAdapter());
     }
 }
