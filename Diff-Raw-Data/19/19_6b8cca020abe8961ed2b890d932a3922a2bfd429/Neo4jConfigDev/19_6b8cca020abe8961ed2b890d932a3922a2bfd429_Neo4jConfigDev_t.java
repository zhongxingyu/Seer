 /*
  *   This file is part of Timelord.
  *
  *   Timelord is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Affero General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Timelord is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Affero General Public License for more details.
  *
  *   You should have received a copy of the Affero GNU General Public License
  *   along with Timemord.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.enhan.timelord.domain.config.data;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 import org.springframework.data.neo4j.config.Neo4jConfiguration;
 
 import eu.enhan.timelord.annotation.Dev;
 
 /**
  * @author Emmanuel Nhan
  *
  */
 @Configuration
 @Dev
 @PropertySource("classpath:neo4j-dev.properties")
@Import(Neo4jConfiguration.class)
public class Neo4jConfigDev implements Neo4jConfig {
 
     private static final Logger log = LoggerFactory.getLogger(Neo4jConfigDev.class);
     
    public static final String defaultLocation ="target/db";
    
     @Autowired
     Environment env;
     
     /**
      * 
      * @see eu.enhan.timelord.domain.config.data.Neo4jConfig#graphDatabaseService()
      */
     @Bean(destroyMethod="close")
     @Override
     public GraphDatabaseService graphDatabaseService() {
 	log.debug("Create Graph database.");
 	if (env == null){
	    log.warn("Fail injecting Environment. Using default location ('{}') for Neo4j", defaultLocation);
	    return new EmbeddedGraphDatabase(defaultLocation);
 	}
	log.debug("Using {} as location for Neo4j", env.getProperty(LOCATION_KEY));
	return new EmbeddedGraphDatabase(env.getProperty(LOCATION_KEY));
     }
 
 }
