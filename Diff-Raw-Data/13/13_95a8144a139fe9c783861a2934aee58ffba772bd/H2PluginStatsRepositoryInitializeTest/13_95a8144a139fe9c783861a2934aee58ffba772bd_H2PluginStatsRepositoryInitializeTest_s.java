 /**
  *
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 package org.apache.maven.eventspy.h2;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.sql.DataSource;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.mockito.Mockito.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class H2PluginStatsRepositoryInitializeTest {
     @Mock
     private H2DatabaseManager databaseManager;
     @Mock
     private DataSource dataSource;
     @InjectMocks
    private H2PluginStatsRepository repository = new H2PluginStatsRepository(null);
 
     @Test
     public void initialize_shouldNotCreateTheDatabaseIfItExistsAlready() {
         when(databaseManager.doesDatabaseNotExist()).thenReturn(false);
         when(databaseManager.load()).thenReturn(dataSource);
 
         repository.initialize(null);
 
         assertNotNull(repository.getJdbcTemplate());
         verify(databaseManager, never()).create();
     }
 
     @Test
     public void initialize_shouldCreateTheDatabaseIfItHasNotBeenCreatedYet() {
         when(databaseManager.doesDatabaseNotExist()).thenReturn(true);
         when(databaseManager.load()).thenReturn(dataSource);
 
         repository.initialize(null);
 
         verify(databaseManager).create();
         assertNotNull(repository.getJdbcTemplate());
     }
 }
