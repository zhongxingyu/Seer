 /*
  * Copyright (C) 2013 TestUrSelf Project 
  *
  * Guillaume BALAS
  *
  * Licensed under the Creative Commons BY-NC-ND.
  * You may obtain a copy of the License at  * 
  *
  *      http://creativecommons.org/licenses/by-nc-nd/3.0/
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package fr.supinfo.testurselfserver.scheduler;
 
 import fr.supinfo.testurselfserver.controller.IndexController;
 import fr.supinfo.testurselfserver.dao.MySQLConnection;
 import fr.supinfo.testurselfserver.dao.PackageDao;
 import fr.supinfo.testurselfserver.entity.Package;
 import java.io.IOException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 public class GetPackageJob implements Job {
 
     @Override
     public void execute(JobExecutionContext executionContext) throws JobExecutionException {
         Logger.getLogger(MySQLConnection.class.getName()).log(Level.INFO, null, "Job executed");        
         IndexController.lastUpdate = new Date();        
         
         MySQLConnection mySQLConnection = new MySQLConnection();
         
         //get all quizz from db
         PackageDao p = new PackageDao(mySQLConnection.getInstance());
         p.getAllPackage(); 
         
         try {
          for(int i = 0; i <  Package.listPackage.size(); i++) {
               p.savePackageToZip(Package.listPackage.get(i));
          }
         } catch (IOException ex) {
             Logger.getLogger(GetPackageJob.class.getName()).log(Level.SEVERE, null, ex);
         }
         
     }
 }
