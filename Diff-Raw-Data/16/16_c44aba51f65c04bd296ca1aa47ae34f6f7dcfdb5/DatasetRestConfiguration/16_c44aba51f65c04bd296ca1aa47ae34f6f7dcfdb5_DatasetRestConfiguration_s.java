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
 package edu.dfci.cccb.mev.dataset.rest.configuration;
 
 import static edu.dfci.cccb.mev.dataset.rest.assembly.tsv.prototype.AbstractTsvHttpMessageConverter.TSV_EXTENSION;
 import static edu.dfci.cccb.mev.dataset.rest.assembly.tsv.prototype.AbstractTsvHttpMessageConverter.TSV_MEDIA_TYPE;
 import static edu.dfci.cccb.mev.dataset.rest.resolvers.AnalysisPathVariableMethodArgumentResolver.ANALYSIS_MAPPING_NAME;
 import static org.springframework.context.annotation.ScopedProxyMode.INTERFACES;
 import static org.springframework.context.annotation.ScopedProxyMode.NO;
 import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;
 import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;
 import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
 
import java.net.URL;
 import java.util.Map;
 
import javax.annotation.PostConstruct;

 import lombok.ToString;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Scope;
 import org.springframework.web.context.request.NativeWebRequest;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 
 import edu.dfci.cccb.mev.dataset.domain.contract.Analysis;
 import edu.dfci.cccb.mev.dataset.domain.contract.Dataset;
 import edu.dfci.cccb.mev.dataset.domain.contract.DatasetBuilder;
 import edu.dfci.cccb.mev.dataset.domain.contract.Dimension;
 import edu.dfci.cccb.mev.dataset.domain.contract.ParserFactory;
 import edu.dfci.cccb.mev.dataset.domain.contract.Selection;
 import edu.dfci.cccb.mev.dataset.domain.contract.SelectionBuilder;
 import edu.dfci.cccb.mev.dataset.domain.contract.ValueStoreBuilder;
 import edu.dfci.cccb.mev.dataset.domain.contract.Workspace;
 import edu.dfci.cccb.mev.dataset.domain.mock.MapBackedValueStoreBuilder;
 import edu.dfci.cccb.mev.dataset.domain.simple.ArrayListWorkspace;
 import edu.dfci.cccb.mev.dataset.domain.simple.SimpleDatasetBuilder;
 import edu.dfci.cccb.mev.dataset.domain.simple.SimpleSelectionBuilder;
 import edu.dfci.cccb.mev.dataset.domain.supercsv.SuperCsvComposerFactory;
 import edu.dfci.cccb.mev.dataset.domain.supercsv.SuperCsvParserFactory;
 import edu.dfci.cccb.mev.dataset.rest.assembly.json.simple.DimensionTypeJsonSerializer;
 import edu.dfci.cccb.mev.dataset.rest.assembly.json.simple.SimpleDatasetJsonSerializer;
 import edu.dfci.cccb.mev.dataset.rest.assembly.json.simple.SimpleDimensionJsonSerializer;
 import edu.dfci.cccb.mev.dataset.rest.assembly.json.simple.SimpleSelectionJsonSerializer;
 import edu.dfci.cccb.mev.dataset.rest.assembly.tsv.DatasetTsvMessageConverter;
 import edu.dfci.cccb.mev.dataset.rest.resolvers.DatasetPathVariableMethodArgumentResolver;
 import edu.dfci.cccb.mev.dataset.rest.resolvers.DimensionPathVariableMethodArgumentResolver;
 import edu.dfci.cccb.mev.dataset.rest.resolvers.SelectionPathVariableMethodArgumentResolver;
 
 /**
  * @author levk
  * 
  */
 @Configuration
 @ComponentScan (basePackages = "edu.dfci.cccb.mev.dataset.rest.controllers")
 @ToString
 public class DatasetRestConfiguration extends WebMvcConfigurerAdapter {
 
   // Domain conversational objects
 
   @Bean
   @Scope (value = SCOPE_SESSION, proxyMode = INTERFACES)
   public Workspace workspace () {
     return new ArrayListWorkspace ();
   }
 
   @Bean
   @Scope (value = SCOPE_REQUEST, proxyMode = NO)
   public Dataset dataset (NativeWebRequest request, DatasetPathVariableMethodArgumentResolver resolver) throws Exception {
     return resolver.resolveObject (request);
   }
 
   @Bean
   @Scope (value = SCOPE_REQUEST, proxyMode = NO)
   public Dimension dimension (NativeWebRequest request, DimensionPathVariableMethodArgumentResolver resolver) throws Exception {
     return resolver.resolveObject (request);
   }
 
   @Bean
   @Scope (value = SCOPE_REQUEST, proxyMode = NO)
   public Selection selection (NativeWebRequest request, SelectionPathVariableMethodArgumentResolver resolver) throws Exception {
     return resolver.resolveObject (request);
   }
 
   // TODO: this is a hack until type of analysis goes into the request mapping
   @SuppressWarnings ("unchecked")
   @Bean
   @Scope (value = SCOPE_REQUEST, proxyMode = NO)
   public Analysis analysis (NativeWebRequest request, Dataset dataset) throws Exception {
     return dataset.analyses ()
                   .get (((Map<String, String>) request.getAttribute (URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                                                                      RequestAttributes.SCOPE_REQUEST)).get (ANALYSIS_MAPPING_NAME));
   }
 
   // Domain builders
 
   @Bean
   @Scope (value = SCOPE_REQUEST, proxyMode = INTERFACES)
   public ValueStoreBuilder valueFactory () {
     return new MapBackedValueStoreBuilder ();
   }
 
   @Bean
   @Scope (value = SCOPE_REQUEST, proxyMode = INTERFACES)
   public DatasetBuilder datasetBuilder () {
     return new SimpleDatasetBuilder ();
   }
 
   @Bean
   @Scope (value = SCOPE_REQUEST, proxyMode = INTERFACES)
   public SelectionBuilder selectionBuilder () {
     return new SimpleSelectionBuilder ();
   }
 
   // Serialization
 
   @Bean
   public DimensionTypeJsonSerializer dimensionTypeJsonSerializer () {
     return new DimensionTypeJsonSerializer ();
   }
 
   @Bean
   public SimpleDatasetJsonSerializer datasetJsonSerializer () {
     return new SimpleDatasetJsonSerializer ();
   }
 
   @Bean
   public SimpleSelectionJsonSerializer selectionJsonSerializer () {
     return new SimpleSelectionJsonSerializer ();
   }
 
   @Bean
   public SimpleDimensionJsonSerializer simpleDimensionJsonSerializer () {
     return new SimpleDimensionJsonSerializer ();
   }
 
   @Bean
   public DatasetTsvMessageConverter datasetTsvMessageConverter () {
     return new DatasetTsvMessageConverter ();
   }
 
   @Bean
   public SuperCsvComposerFactory superCsvComposerFactory () {
     return new SuperCsvComposerFactory ();
   }
 
   @Bean
   public ParserFactory tsvParserFactory () {
     return new SuperCsvParserFactory ();
   }
 
   // Resolvers
 
   @Bean
   public DatasetPathVariableMethodArgumentResolver datasetPathVariableMethodArgumentResolver () {
     return new DatasetPathVariableMethodArgumentResolver ();
   }
 
   @Bean
   public DimensionPathVariableMethodArgumentResolver dimensionPathVariableMethodArgumentResolver () {
     return new DimensionPathVariableMethodArgumentResolver ();
   }
 
   @Bean
   public SelectionPathVariableMethodArgumentResolver selectionPathVariableMethodArgumentResolver () {
     return new SelectionPathVariableMethodArgumentResolver ();
   }
 
   /* (non-Javadoc)
    * @see
    * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
    * #configureContentNegotiation
    * (org.springframework.web.servlet.config.annotation
    * .ContentNegotiationConfigurer) */
   @Override
   public void configureContentNegotiation (ContentNegotiationConfigurer configurer) {
     configurer.mediaType (TSV_EXTENSION, TSV_MEDIA_TYPE);
   }
 }
