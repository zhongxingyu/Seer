 /*
  * Copyright (C) 2009
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Derived from Travian world" logo. If the display of the logo is not
  * reasonably feasible for technical reasons, the Appropriate Legal Notices must
  * display the words "Derived from Travian world".
  */
 
 package com.funambol.lanciadelta.rally;
 
 import com.funambol.lanciadelta.Constants;
 import com.funambol.lanciadelta.LanciaDeltaBeanMap;
import com.rallydev.webservice.v1_14.domain.Artifact;
 import com.rallydev.webservice.v1_14.domain.DomainObject;
 import com.rallydev.webservice.v1_14.domain.HierarchicalRequirement;
 import com.rallydev.webservice.v1_14.domain.Iteration;
 import com.rallydev.webservice.v1_14.domain.Project;
 import com.rallydev.webservice.v1_14.domain.QueryResult;
 import com.rallydev.webservice.v1_14.domain.Release;
 import com.rallydev.webservice.v1_14.domain.Workspace;
 import com.rallydev.webservice.v1_14.service.RallyService;
 import com.rallydev.webservice.v1_14.service.RallyServiceServiceLocator;
 import com.rallydev.webservice.v1_14.service.RallyServiceSoapBindingStub;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.xml.rpc.Service;
 import org.apache.axis.client.Stub;
 
 /**
  *
  * @author ste
  */
 public class LanciaDeltaService
 extends RallyServiceSoapBindingStub
 implements RallyService, Constants {
 
     private static LanciaDeltaService instance = null;
 
     private LanciaDeltaService(URL endpointURL, Service service)
     throws org.apache.axis.AxisFault {
          super(endpointURL, service);
     }
 
     // ---------------------------------------------------------- Public methods
 
     public Map<String, Object> object2Map(Iteration o) {
         return new LanciaDeltaBeanMap(o);
     }
 
     /**
      * Returns the iterations with the given name.
      *
      * @param name the iteration name
      *
      * @return the iterations with the given name
      *
      * @throws LanciaDeltaException in case of errors
      */
     public List<Iteration> getIterations(String name) 
     throws LanciaDeltaException{
         final int PAGE = 50;
         ArrayList<Iteration> iterations = new ArrayList();
 
         QueryResult rs = null;
         boolean cont = true;
         long i = 1, tot = 0;
         do {
             try {
                 rs = query(ITERATION, "(Name = " + name + ")", "", false, i, PAGE);
             
                 DomainObject[] results = rs.getResults();
                 tot = rs.getTotalResultCount();
 
                 for (DomainObject r: results) {
                     Iteration iteration = new Iteration();
                     iteration.setRef(r.getRef());
                     iteration = (Iteration)read(iteration);
                     iterations.add(iteration);
                     ++i;
                 }
             } catch (Exception e) {
                 throw new LanciaDeltaException("Error retrieving the iterations with name" + name, e);
             }
         } while (i<=tot);
 
         return iterations;
 
     }
 
     /**
      * Returns the public stories scheduled for the given release. A story is
      * public if the flag <code>public</code> is set true.
      *
      * @param releaseName the release name
      *
      * @return the public stories scheduled for the given releas
      *
      * @throws LanciaDeltaException in case of errors
      */
     public List<HierarchicalRequirement> getReleaseStories(String releaseName)
     throws LanciaDeltaException {
         return getReleaseStories(releaseName, true);
     }
 
     /**
      * Returns the stories scheduled for the given release
      *
      * @param releaseName the release name
      * @param publicOnly true to filter only public stories
      *
      * @return the iterations scheduled for the given release
      *
      * @throws LanciaDeltaException in case of errors
      */
     public List<HierarchicalRequirement> getReleaseStories(
                                              String  releaseName,
                                              boolean publicOnly)
     throws LanciaDeltaException {
         Release release = getRelease(releaseName);
 
         final String Q = (publicOnly ? "(" : "")
                        + "(Release = "
                        + release.getRef()
                        + ")"
                        + (publicOnly ? " and (Public = true))" : "")
                        ;
 
         try {
             List<HierarchicalRequirement> stories = getStories(Q);
 
             for(HierarchicalRequirement story: stories) {
                 Iteration iteration = story.getIteration();
                 if (iteration != null) {
                     story.setIteration((Iteration) read(iteration));
                 }
             }
 
             return stories;
         } catch (Exception e) {
             throw new LanciaDeltaException("Error retrieving the stories for the release " + releaseName, e);
         }
     }
 
     /**
      * Returns the release object given the release name
      *
      * @param name the release name
      *
      * @return the release object given the release name
      *
      * @throws LanciaDeltaException in case of errors
      */
     public Release getRelease(String name)
     throws LanciaDeltaException {
         final int PAGE = 20;
 
         Release release = null;
         
         QueryResult rs = null;
         try {
             rs = query(RELEASE, "(Name = " + name + ")", "", true, 1, PAGE);
 
             DomainObject[] results = rs.getResults();
 
             if (results.length>0) {
                 release = (Release)results[0];
             }
         } catch (Exception e) {
             throw new LanciaDeltaException("Error getting the iterations with name" + name, e);
         }
 
         return release;
 
     }
 
     /**
      * Returns the public stories of the given iteration.
      * <br/><b>
      * NOTE: we assume that there are not iterations with the same name
      * </b>
      *
      * @param name of the iteration reference id
      * @param publicOnly true to filter only public stories
      *
      * @return the user stories scheduled in the given iteration
      *
      * @throws LanciaDeltaException in case of errors
      */
     public List<HierarchicalRequirement> getIterationStories(String name)
     throws LanciaDeltaException{
         return getIterationStories(name, true);
     }
 
     /**
      * Returns the user stories scheduled in the given iteration.
      * <br/><b>
      * NOTE: we assume that there are not iterations with the same name
      * </b>
      *
      * @param name of the iteration reference id
      * @param publicOnly true to filter only public stories
      *
      * @return the user stories scheduled in the given iteration
      *
      * @throws LanciaDeltaException in case of errors
      */
     public List<HierarchicalRequirement> getIterationStories(String name, boolean publicOnly)
     throws LanciaDeltaException{
         List<Iteration> iterations = getIterations(name);
 
         if (iterations.size() == 0) {
             throw new LanciaDeltaException("Iteration '" + name + "' not found");
         }
 
         final String Q = (publicOnly ? "(" : "")
                        + "(Iteration = "
                        + iterations.get(0).getRef()
                        + ")"
                        + (publicOnly ? " and (Public = true))" : "")
                        ;
 
         try {
             return getStories(Q);
         } catch (Exception e) {
             throw new LanciaDeltaException("Error retrieving the stories for iteration " + name, e);
         }
     }
 
     /**
      * Returns the user story with the given id.
      *
      * @param id the user story id
      *
      * @return the user story with the given
      *
      * @throws LanciaDeltaException in case of errors
      */
     public HierarchicalRequirement getStory(String id)
     throws LanciaDeltaException{
         final int PAGE = 50;
 
         try {
             QueryResult rs = query(HIERARCHICAL_REQUIREMENT, "(FormattedID = " + id + ")", "", true, 1, PAGE);
             DomainObject[] results = rs.getResults();
 
             if ((results != null) && (results.length > 0)) {
                 HierarchicalRequirement story = (HierarchicalRequirement)(results[0]);
 
                 if (story.getParent() != null) {
                     story.setParent((HierarchicalRequirement)read(story.getParent()));
                 }
                 if (story.getProject() != null) {
                     story.setProject((Project)read(story.getProject()));
                 }
                 if (story.getIteration() != null) {
                     story.setIteration((Iteration)read(story.getIteration()));
                 }
                 if (story.getRelease() != null) {
                     story.setRelease((Release)read(story.getRelease()));
                 }
 
                 return story;
             }
         } catch (Exception e) {
             throw new LanciaDeltaException(e);
         }
 
         throw new LanciaDeltaException("No user story found with ID " + id);
 
     }
 
     /**
      * The same as query (null, ...)
      * 
      * @param artifactType
      * @param query
      * @param order
      * @param fetch
      * @param start
      * @param pageSize
      *
      * @return the same as query (null, ...)
      */
     public QueryResult query (
         String artifactType,
         String query,
         String order,
         boolean fetch,
         long start,
         long pageSize
     ) throws RemoteException {
         return query(
                    (Workspace)null,
                    artifactType,
                    query,
                    order,
                    fetch,
                    start,
                    pageSize
          );
     }
 
     // --------------------------------------------------------- Private methods
 
     private List<HierarchicalRequirement> getStories(String q) 
     throws RemoteException {
         final int PAGE = 50;
         ArrayList<HierarchicalRequirement> stories = new ArrayList<HierarchicalRequirement>();
 
         QueryResult rs = null;
         long i = 1;
         long tot = 0;
         do {
             rs = query(HIERARCHICAL_REQUIREMENT, q, "", true, i, PAGE);
             DomainObject[] results = rs.getResults();
             tot = rs.getTotalResultCount();
             for (DomainObject r : results) {
                 stories.add((HierarchicalRequirement) r);
                 ++i;
             }
         } while (i <= tot);
         
         return stories;
     }
 
     // --------------------------------------------------------------- Singleton
 
 
     /**
      *
      * @return the singleton insatnce of LanciaDeltaService
      *
      * @throws com.funambol.lanciadelta.rally.LanciaDeltaException
      */
     public synchronized static LanciaDeltaService getInstance()
     throws LanciaDeltaException {
         if (instance != null) {
             return instance;
         }
 
         return (instance = initializeRallyService());
     }
 
     /**
      * Initialize the session to access Rally services
      *
      * @throws java.lang.Exception
      */
     private static LanciaDeltaService initializeRallyService() throws LanciaDeltaException {
         try {
             URL url = new URL(
                           "https://"
                     +     System.getProperty(PROPERTY_HOST) 
                     +     RALLY_URL
                     +     LanciaDeltaService.RALLY_SERVICE_URL
             );
 
             RallyService service = (new LanciaDeltaServiceLocator()).getRallyService(url);
 
             // set authentication information on the service
             Stub stub = (Stub) service;
             stub.setUsername(System.getProperty(PROPERTY_USERNAME));
             stub.setPassword(System.getProperty(PROPERTY_PASSWORD));
             
             // Configure the service to maintain an HTTP session cookie
             stub.setMaintainSession(true);
 
             return (LanciaDeltaService)service;
         } catch (Exception e) {
             throw new LanciaDeltaException(e);
         }
     }
 
     // ----------------------------------------------- LanciaDeltaServiceLocator
 
     private static class LanciaDeltaServiceLocator extends RallyServiceServiceLocator {
 
         @Override
         public com.rallydev.webservice.v1_14.service.RallyService getRallyService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
             try {
                 LanciaDeltaService _stub =
                     new LanciaDeltaService(portAddress, LanciaDeltaServiceLocator.this);
                 _stub.setPortName(getRallyServiceWSDDServiceName());
                 return _stub;
             }
             catch (org.apache.axis.AxisFault e) {
                 return null;
             }
         }
     }
 
     
 }
