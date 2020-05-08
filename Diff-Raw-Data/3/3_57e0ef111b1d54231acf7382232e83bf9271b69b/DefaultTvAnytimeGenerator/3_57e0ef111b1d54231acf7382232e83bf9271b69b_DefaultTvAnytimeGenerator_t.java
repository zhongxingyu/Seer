 package org.atlasapi.feeds.tvanytime;
 
 import static org.atlasapi.feeds.youview.LoveFilmOutputUtils.hasAsin;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.OutputStream;
 import java.util.Set;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.util.JAXBSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import tva.metadata._2010.GroupInformationTableType;
 import tva.metadata._2010.ObjectFactory;
 import tva.metadata._2010.OnDemandProgramType;
 import tva.metadata._2010.ProgramDescriptionType;
 import tva.metadata._2010.ProgramInformationTableType;
 import tva.metadata._2010.ProgramLocationTableType;
 import tva.metadata._2010.TVAMainType;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 public class DefaultTvAnytimeGenerator implements TvAnytimeGenerator {
 
     private final class JaxbErrorHandler implements ErrorHandler {
         
         private boolean hasErrors = false;
         
         @Override
         public void warning(SAXParseException e) throws SAXException {
             log.error("XML Validation warning: " + e.getMessage(), e);
             hasErrors = true;
         }
 
         @Override
         public void fatalError(SAXParseException e) throws SAXException {
             log.error("XML Validation fatal error: " + e.getMessage(), e);
             hasErrors = true;
         }
 
         @Override
         public void error(SAXParseException e) throws SAXException {
             log.error("XML Validation error: " + e.getMessage(), e);
             hasErrors = true;
         }
         
         public boolean hasErrors() {
             return hasErrors;
         }
     }
 
     private static final String TVA_LANGUAGE = "en-GB";
 
     private final ProgramInformationGenerator progInfoGenerator;
     private final GroupInformationGenerator groupInfoGenerator;
     private final OnDemandLocationGenerator progLocationGenerator;
     private final ContentResolver contentResolver;
     private final Logger log = LoggerFactory.getLogger(DefaultTvAnytimeGenerator.class);
     private final boolean performValidation;
     private final ObjectFactory factory = new ObjectFactory();
 
     public DefaultTvAnytimeGenerator(ProgramInformationGenerator progInfoGenerator, GroupInformationGenerator groupInfoGenerator, 
             OnDemandLocationGenerator progLocationGenerator, ContentResolver contentResolver, boolean performValidation) {
         this.progInfoGenerator = progInfoGenerator;
         this.groupInfoGenerator = groupInfoGenerator;
         this.progLocationGenerator = progLocationGenerator;
         this.contentResolver = contentResolver;
         this.performValidation = performValidation;
     }
     
     @Override
     public void generateXml(Iterable<Content> contents, OutputStream outStream) {
         try {
             JAXBContext context = JAXBContext.newInstance("tva.metadata._2010");
             Marshaller marshaller = context.createMarshaller();
             
             JAXBElement<TVAMainType> rootElem = createXml(contents);
 
             if (performValidation) {
                 JAXBSource source = new JAXBSource(context, rootElem);
 
                 SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
                 Schema schema = sf.newSchema(new File("../atlas-feeds/src/main/resources/tvanytime/youview/youview_metadata_2011-07-06.xsd")); 
 
                 Validator validator = schema.newValidator();
                 JaxbErrorHandler errorHandler = new JaxbErrorHandler();
                 validator.setErrorHandler(errorHandler);
 
                 validator.validate(source);
                 
                 if(errorHandler.hasErrors()) {
                     ByteArrayOutputStream os = new ByteArrayOutputStream();
                     marshaller.marshal(rootElem, os);
                     log.trace("Invalid xml was: {}", os.toString());
                     throw new RuntimeException("XML Validation against schema failed");
                 }
                 
             }
             
             marshaller.marshal(rootElem, outStream);
         } catch (Exception e) {
             Throwables.propagate(e);
         }
     }
 
     private JAXBElement<TVAMainType> createXml(Iterable<Content> contents) {
         
         Set<String> added = Sets.newHashSet();
         TVAMainType tvaMain = factory.createTVAMainType();
         tvaMain.setLang(TVA_LANGUAGE);
         
         ProgramDescriptionType progDescription = new ProgramDescriptionType();
 
         ProgramInformationTableType progInfoTable = factory.createProgramInformationTableType();
         GroupInformationTableType groupInfoTable = factory.createGroupInformationTableType();
         ProgramLocationTableType progLocTable = factory.createProgramLocationTableType();
 
         for (Content content : contents) {
             try {
                 if (content instanceof Item) {
                     if (!hasAsin(content)) {
                         continue;
                     }
                     Item item = (Item) content;
                     progInfoTable.getProgramInformation().add(progInfoGenerator.generate(item));
                     Optional<OnDemandProgramType> onDemand = progLocationGenerator.generate(item);
                     if (onDemand.isPresent()) {
                         progLocTable.getOnDemandProgram().add(onDemand.get());
                     }
                 }
 
 
                 if (content instanceof Film && added.add(content.getCanonicalUri())) {
                     groupInfoTable.getGroupInformation().add(groupInfoGenerator.generate((Film) content));
                 }
 
                 if (content instanceof Brand) {
                     if (added.add(content.getCanonicalUri())) {
                         groupInfoTable.getGroupInformation().add(groupInfoGenerator.generate((Brand) content, getFirstItem((Brand) content)));
                     }
                 }
 
                 if (content instanceof Series) {
                     Series series = (Series) content;
                     Optional<Brand> brand = getBrand(series);
                     if (added.add(series.getCanonicalUri())) {
                         // obtain parent if it exists
                         groupInfoTable.getGroupInformation().add(groupInfoGenerator.generate(series, brand, getFirstItem(series)));
                     }
                     if (brand.isPresent() && added.add(brand.get().getCanonicalUri())) {
                         groupInfoTable.getGroupInformation().add(groupInfoGenerator.generate(brand.get(), getFirstItem(brand.get())));
                     }
                 }
 
                 if (content instanceof Item) {
                     if (added.add(content.getCanonicalUri())) {
                         Item item = (Item) content;
                         Optional<Series> series = getSeries(item);
                         Optional<Brand> brand = getBrand(item);
 
                         groupInfoTable.getGroupInformation().add(groupInfoGenerator.generate(item, series, brand));
                         if (series.isPresent() && added.add(series.get().getCanonicalUri())) {
                             groupInfoTable.getGroupInformation().add(groupInfoGenerator.generate(series.get(), brand, getFirstItem(series.get())));
                         }
                         if (brand.isPresent() && added.add(brand.get().getCanonicalUri())) {
                             groupInfoTable.getGroupInformation().add(groupInfoGenerator.generate(brand.get(), getFirstItem(brand.get())));
                         }
                     }
                 }
             } catch (Exception e) {
                 log.error("Exception occurred while processing " + content.getCanonicalUri() + " " + e.getMessage(), e);
             }
         }
 
         progDescription.setProgramInformationTable(progInfoTable);
         progDescription.setGroupInformationTable(groupInfoTable);
         progDescription.setProgramLocationTable(progLocTable);
         
         tvaMain.setProgramDescription(progDescription);
         return factory.createTVAMain(tvaMain);
     }
 
     private Optional<Brand> getBrand(Item item) {
         ParentRef brandRef = item.getContainer();
         if (brandRef == null) {
             return Optional.absent();
         }
         ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(brandRef.getUri()));
         Identified identified = resolved.asResolvedMap().get(brandRef.getUri());
         if (!(identified instanceof Brand)) {
             return Optional.absent();
         }
         Brand brand = (Brand) identified;
        if (!hasAsin(brand)) {
            throw new RuntimeException("brand " + brand.getCanonicalUri() + " has no ASIN, while its item " + item.getCanonicalUri() + " does.");
        }
         return Optional.fromNullable(brand);
     }
 
     private Optional<Series> getSeries(Item item) {
         ParentRef seriesRef;
         if (item instanceof Episode) {
             Episode episode = (Episode) item;
             seriesRef = episode.getSeriesRef();
             if (seriesRef == null) {
                 seriesRef = episode.getContainer();
                 if (seriesRef == null) {
                     return Optional.absent();
                 }
             }
         } else {
             seriesRef = item.getContainer();
             if (seriesRef == null) {
                 return Optional.absent();
             }
         }
         ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(seriesRef.getUri()));
         Identified identified = resolved.asResolvedMap().get(seriesRef.getUri());
         if (!(identified instanceof Series)) {
             return Optional.absent();
         }
         Series series = (Series) identified;
         return Optional.fromNullable(series);
     }
 
     private Optional<Brand> getBrand(Series series) {
         ParentRef brandRef = series.getParent();
         if (brandRef == null) {
             return Optional.absent();
         }
         ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(brandRef.getUri()));
         Brand brand = (Brand) resolved.asResolvedMap().get(brandRef.getUri());
         return Optional.fromNullable(brand);
     }
 
     private Item getFirstItem(Series series) {
         ChildRef last = Iterables.getLast(series.getChildRefs());
         ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(last.getUri()));
         return (Item) resolved.asResolvedMap().get(last.getUri());
     }
 
     private Item getFirstItem(Brand brand) {
         ChildRef last = Iterables.getLast(brand.getChildRefs());
         ResolvedContent resolved = contentResolver.findByCanonicalUris(ImmutableList.of(last.getUri()));
         return (Item) resolved.asResolvedMap().get(last.getUri()); 
     }
 }
