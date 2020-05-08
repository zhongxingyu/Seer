 package fnug.resource;
 
 import java.util.List;
 
 /*
  Copyright 2010 Martin Algesten
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 /**
  * Abstract superclass for aggregated resources.
  * 
  * @author Martin Algesten
  * 
  */
 public abstract class AbstractAggregatedResource extends AbstractResource implements AggregatedResource {
 
     /**
      * Constructs setting necessary fields.
      * 
     * @param owner
     *            The bundle owning this aggregated resource.
      * @param path
      *            The path of the resource. See {@link #getPath()}.
      */
     protected AbstractAggregatedResource(Bundle owner, String path) {
         super(owner.getName() + "/", path);
     }
 
     /**
      * Implements the abstract readEntry(), and uses {@link #readLastModified()} and {@link #buildAggregate()} to build
      * the {@link AbstractResource.Entry} .
      */
     @Override
     protected final Entry readEntry() {
         return new Entry(readLastModified(), buildAggregate());
     }
 
     /**
      * Must be implemented in subclasses to build the aggregated bytes.
      * 
      * @return the aggregated bytes, built from {@link #getAggregates()}.
      */
     protected abstract byte[] buildAggregate();
 
     /**
      * Loops over {@link #getAggregates()} and {@link #getDependencies()} to find the most recent last modified date.
      * 
      * @return the most recent last modified date.
      */
     @Override
     protected long readLastModified() {
         long lastModified = -1l;
         for (Resource res : getAggregates()) {
             lastModified = Math.max(lastModified, res.getLastModified());
         }
         for (Resource res : getDependencies()) {
             lastModified = Math.max(lastModified, res.getLastModified());
         }
         return lastModified;
     }
 
     /**
      * Calls {@link Resource#checkModified()} on all the underlying resources in {@link #getAggregates()} and
      * {@link #getDependencies()}. After that calls super class checkModified() which may trigger a rebuild of the
      * aggregate (via {@link #readEntry()}, {@link #buildAggregate()})
      * 
      * @return true if any of the resources were found to be newer.
      */
     @Override
     public boolean checkModified() {
         
         if (ResourceResolver.getInstance() != null &&
                 ResourceResolver.getInstance().getGlobalConfig().isNoModify()) {
             return false;
         }
 
         for (Resource res : getAggregates()) {
             res.checkModified();
         }
         for (Resource res : getDependencies()) {
             res.checkModified();
         }
     
         return super.checkModified();
 
     }
 
     /**
      * Always throws {@link UnsupportedOperationException}.
      */
     @Override
     public List<String> findRequiresTags() {
         throw new UnsupportedOperationException("Not possible to find @requires on aggregated resource");
     }
 
 }
