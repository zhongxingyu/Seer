 /*
  * Copyright (C) 2012  John May and Pablo Moreno
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package uk.ac.ebi.mdk.service;
 
 
 import uk.ac.ebi.mdk.domain.identifier.Identifier;
 import uk.ac.ebi.mdk.service.query.QueryService;
 
 import java.util.Collection;
 
 /**
  * ServiceManager - 29.02.2012 <br/>
  * <p/>
  * Class descriptions.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 public interface ServiceManager {
 
     /**
      * Determine whether a particular service is available in the manager. This method will
      * determine not only if the service is pressent in the manager but also whether the service
      * is available (via. {@see QueryService#startup()). <br/>
      * <p/>
      * <b>Example:</b>
      * <pre>
      * manager.hasService(ChEBIIdentifier.class,
      *                    CrossReferenceService.class)
      * </pre>
      *
      * @param identifierClass class of the identifier you want the service for
      * @param serviceClass    class of the
      * @param <S>             extends {@see QueryService}
      * @param <I>             extends {@see Identifier}
      *
      * @return
      */
     public <S extends QueryService<I>, I extends Identifier> boolean hasService(Class<? extends I> identifierClass,
                                                                                 Class<? extends S> serviceClass);
 
     public <S extends QueryService<I>, I extends Identifier> boolean hasService(I identifier,
                                                                                 Class<? extends S> serviceClass);
 
     /**
      * Access a service for a given identifier and service type. This method
      * is used to access a required service. Note that if multiple services
      * match the requirement then the first encountered will be returned. It
      * is therefore important to have a different service manager for each
      * service type (i.e. lucene, database and webservice).
      * <p/>
      * <b>Example:</b><br/>
      * <pre>{@code
      * // get the required service manager
      * ServiceManager manager = ...;
      * <p/>
      * ChEBIIdentifier identifier = new ChEBIIdentifier("CHEBI:15422");
      * <p/>
      * // get the cross-reference service for a given identifier
      * if(manager.hasService(identifier.getClass(), CrossReferenceService.class)){
      * <p/>
      *     CrossReferenceService service = manager.getService(identifier.getClass(),
      *                                                        CrossReferenceService.class);
      * <p/>
      *     // use the service
      *     Collection crossreferences = service.getCrossReferences(identifier);
      * }}
      * </pre>
      *
      * @param identifierClass class of the identifier you want the service for
      * @param serviceClass    class of the
      * @param <S>             extends {@see QueryService}
      * @param <I>             extends {@see Identifier}
      *
      * @return instance of the service that matches the criteria (if no service
      *         matches and invalid parameter exception is thrown)
      */
    public <S extends QueryService<I>, I extends Identifier> S getService(Class<? extends I> identifierClass,
                                                                             Class<? extends S> serviceClass);
 
     public <S extends QueryService<I>, I extends Identifier> S getService(I identifier,
                                                                             Class<? extends S> serviceClass);
 
     /**
      * Provides creation of a proxy service where multiple services can be combined
      * into a single instance. The proxy uses a the code generation library (cglib)
      * MethodInterceptor to dispatch methods to the sub-services. Methods in the super type
      * {@see QueryService} will be invoked across all delegates (e.g. setMaxResults(int))
      * whilst other method methods are invoked on a first come first served with the order
      * being determined by the priority given by the {@see ServiceType}. This means if you
      * have two services providing the IUPAC name only the highest priority service will be
      * invoked. This proxying has some overhead but method calls are cached the following benchmark
      * shoes the difference over 80,000 inovocations of two service calls.
      *
      * <table>
      *     <tr><td>Aggregated</td><td>19,368 ms</td></tr>
      *     <tr><td>Separated</td><td>19,643 ms</td></tr>
      * </table>
      *
      *
      * To create an custom aggregated service you need provide an interface describing your
      * requirements and pass it as the service class parameter. Currently the interface
      * does not need to public and it is possible to use inner-interfaces:
      *
      * <pre>{@code
      *
      * public class AggregatedServiceExample {
      *
      *    interface MyCustomService extends StructureService,
      *                                      PreferredNameService {
      *    }
      *
      *    public static void main(String[] args) {
      *        ServiceManager  manager = DefaultServiceManager.getInstance();
      *        MyCustomService service = manager.createService(ChEBIIdentifier.class,
      *                                                        MyCustomService.class);
      *
      *        service.getPreferredName(identifier); // in PreferredName
     *         service.getStructure(identifier);     // in StructureService
      *    }
      *
      * }
      * }
      * </pre>
      *
      * It is also possible to make the interface generically typed:
      * <pre>{@code
      * interface MyCustomService<I extends Identifier> extends StructureService<I>,
      *                                                         PreferredNameService<I> {
      * }
      * }</pre>
      *
      * or even:
      * <pre>{@code
      * interface MyChEBIService extends StructureService<ChEBIIdentifier>,
      *                                  PreferredNameService<ChEBIIdentifier> {
      * }
      * }</pre>
      *
      * If you are writing an algorithm to consuming the service you don't need to provide the interface
      * required. Instead you can use generic typing on methods. In the following example we create a new type
      * 'S' which we say must implement both {@see PreferredNameService} and {@see StructureService}. We
      * then provide 'S' as a parameter type. Subsequently this allows the user of the method to provide their
      * own implementation which could be via an aggregated service or for example a database connection.
      * The algorithm should not be thinking about how it will be retrieving the object data but rather
      * what it will be doing with the object data. In the below example we do not care how we get the name
      * and structure we just describe that we need and allow another class to provide it.
      *
      * <pre>{@code
      * public <S extends PreferredNameService & StructureService>
      *  void printNameAndAtomCount(S service, Identifier identifier) {
      *    System.out.println( service.getPreferredName(identifier) );
      *    System.out.println( service.getStructure(identifier).getAtomCount() );
      * }
      * }
      * </pre>
      *
      * @param identifierClass
      * @param serviceClass
      * @param <I>
      * @param <S>
      *
      * @return
      */
     public <I extends Identifier, S extends QueryService> S createService(Class<? extends I> identifierClass,
                                                                           Class<? extends S>           serviceClass);
 
     public Collection<Identifier> getIdentifiers(Class<? extends QueryService> c);
 
 }
