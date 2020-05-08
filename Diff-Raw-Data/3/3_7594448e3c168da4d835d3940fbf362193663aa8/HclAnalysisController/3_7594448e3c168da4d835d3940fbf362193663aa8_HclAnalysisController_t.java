 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.hcl.rest.controllers;
 
 import static ch.lambdaj.Lambda.extract;
 import static ch.lambdaj.Lambda.on;
 import static edu.dfci.cccb.mev.dataset.rest.context.RestPathVariableDatasetRequestContextInjector.ANALYSIS;
 import static edu.dfci.cccb.mev.dataset.rest.context.RestPathVariableDatasetRequestContextInjector.ANALYSIS_URL_ELEMENT;
 import static edu.dfci.cccb.mev.dataset.rest.context.RestPathVariableDatasetRequestContextInjector.DATASET;
 import static edu.dfci.cccb.mev.dataset.rest.context.RestPathVariableDatasetRequestContextInjector.DATASET_URL_ELEMENT;
 import static edu.dfci.cccb.mev.dataset.rest.context.RestPathVariableDatasetRequestContextInjector.DIMENSION;
 import static edu.dfci.cccb.mev.dataset.rest.context.RestPathVariableDatasetRequestContextInjector.DIMENSION_URL_ELEMENT;
 import static edu.dfci.cccb.mev.hcl.rest.context.RestPathVariableHclRequestContextInjector.ALGORITHM;
 import static edu.dfci.cccb.mev.hcl.rest.context.RestPathVariableHclRequestContextInjector.ALGORITHM_URL_ELEMENT;
 import static edu.dfci.cccb.mev.hcl.rest.context.RestPathVariableHclRequestContextInjector.METRIC;
 import static edu.dfci.cccb.mev.hcl.rest.context.RestPathVariableHclRequestContextInjector.METRIC_URL_ELEMENT;
 import static org.springframework.http.HttpStatus.OK;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import lombok.Getter;
 import lombok.Setter;
 import lombok.ToString;
 import lombok.extern.log4j.Log4j;
 
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.bind.annotation.RestController;
 
 import edu.dfci.cccb.mev.dataset.domain.contract.Dataset;
 import edu.dfci.cccb.mev.dataset.domain.contract.DatasetException;
 import edu.dfci.cccb.mev.dataset.domain.contract.DatasetNotFoundException;
 import edu.dfci.cccb.mev.dataset.domain.contract.Dimension;
 import edu.dfci.cccb.mev.dataset.domain.contract.Dimension.Type;
 import edu.dfci.cccb.mev.dataset.domain.contract.InvalidDimensionTypeException;
 import edu.dfci.cccb.mev.dataset.domain.contract.Workspace;
 import edu.dfci.cccb.mev.hcl.domain.concrete.TwoDimensionalHcl;
 import edu.dfci.cccb.mev.hcl.domain.contract.Algorithm;
 import edu.dfci.cccb.mev.hcl.domain.contract.HclResult;
 import edu.dfci.cccb.mev.hcl.domain.contract.Metric;
 import edu.dfci.cccb.mev.hcl.domain.contract.Node;
 import edu.dfci.cccb.mev.hcl.domain.contract.NodeBuilder;
 
 /**
  * @author levk
  * 
  */
 @RestController
 @ToString
 @Log4j
 public class HclAnalysisController {
 
   private @Inject Workspace workspace;
   private @Inject NodeBuilder nodeBuilder;
 
   @RequestMapping (value = "/dataset/"
                            + DATASET_URL_ELEMENT + "/analyze/hcl/{name}(" + DIMENSION_URL_ELEMENT + ","
                            + METRIC_URL_ELEMENT + "," + ALGORITHM_URL_ELEMENT + ")",
                    method = POST)
   @ResponseStatus (OK)
   public void start (final @PathVariable ("name") String name,
                      final @PathVariable (DATASET) Dataset ds,
                      final @PathVariable (DIMENSION) Dimension dimension,
                      final @PathVariable (METRIC) Metric metric,
                      final @PathVariable (ALGORITHM) Algorithm algorithm) throws DatasetNotFoundException,
                                                                          InvalidDimensionTypeException {
     // HACKS FIXME: This is bad, fix scoping!!!
 
     final TwoDimensionalHcl hcl = new TwoDimensionalHcl ();
     final Dataset dataset = workspace.get (ds.name ());
     final Type type = dimension.type ();
     hcl.nodeBuilder (nodeBuilder);
     hcl.metric (metric);
     hcl.algorithm (algorithm);
 
     log.debug ("Running HCL on " + dataset + ", " + dimension + " of " + type);
 
     // END HACKS
     new Thread () {
       /* (non-Javadoc)
        * @see java.lang.Thread#run() */
       @Override
       public void run () {
         try {
           dataset.analyses ().put (hcl.name (name).dataset (dataset).dimension (dimension).build ());
         } catch (DatasetException e) {
           log.warn ("Could not cluster hierarchically", e);
         }
       }
     }.run (); // .start ();
   }
 
   @RequestMapping (value = "/dataset/" + DATASET_URL_ELEMENT + "/analysis/" + ANALYSIS_URL_ELEMENT,
                    method = POST)
   public List<String> apply (@PathVariable (DATASET) Dataset dataset,
                              @PathVariable (ANALYSIS) HclResult analysis) throws DatasetException {
     analysis.apply ();
     return analysis.dimension ().keys ();
   }
 
   @RequestMapping (value = "/dataset/" + DATASET_URL_ELEMENT + "/analysis/" + ANALYSIS_URL_ELEMENT + "/root",
                    method = GET)
  public Node root (@PathVariable (DATASET) Dataset dataset,
                    @PathVariable (ANALYSIS) HclResult analysis) {
     return analysis.root ();
   }
 
   private @Getter @Setter (onMethod = @_ (@Inject)) Collection<Metric> metrics;
   private @Getter @Setter (onMethod = @_ (@Inject)) Collection<Algorithm> algorithms;
 
   @RequestMapping (value = "/analysis/hcl/metrics", method = GET)
   public Collection<String> metrics () {
     return extract (metrics, on (Metric.class).name ());
   }
 
   @RequestMapping (value = "/analysis/hcl/algorithms", method = GET)
   public Collection<String> algorithms () {
     return extract (algorithms, on (Algorithm.class).name ());
   }
 }
